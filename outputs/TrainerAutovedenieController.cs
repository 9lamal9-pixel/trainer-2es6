using System;
using System.Collections.Generic;

// Simulator controller. The start/cancellation contract follows USAVP-G RE 2.3;
// the braking envelope and traction regulation below are our model adaptation.
// This class never writes native force, velocity, electrical permissions or handles.
public enum TrainerAvpState { Advisor, Automatic, Cancelled, Finished }
public enum TrainerAvpOperation { Ordinary, SignalFollowing, CatchUp }
public sealed class TrainerAvpInput {
    public double X, SpeedKmh, TrainLengthM, TrainMassT, Seconds;
    public double OperationalLimitKmh = 80;
    public int TractionPosition;
    public bool Ready, SettingsValid, ManualOverride, SafetyIntervention;
    public bool BrakesReleased = true;
    public bool CanApplyTraction = true;
    public bool AutomaticBrakesReleased = true;
    public bool HasBrakeFeedback, HasWagonTcFeedback;
    public double MinimumWagonTcBar=Double.NaN;
    public double EqualizingReservoirPressure;
    public double ChargingPressureBar=Double.NaN, BrakePipePressureBar=Double.NaN;
    public double MaximumConsistSpeedKmh=Double.NaN;
    public double IndependentKvtPressureBar=Double.NaN, MinBogiePressureBar=Double.NaN, MaxBogiePressureBar=Double.NaN;
    public int WagonAxles=300, ObservedKm=2, ObservedKvt=2;
    public bool BrakeCommandsKnown, AmbientTemperatureKnown, SandCommandAccepted;
    public double AmbientTemperatureC;
    public string NotReadyReason;
    public TrainerAvpOperation Operation;
    // Live signal preview requires a confirmed signalling source. Static
    // route symbols do not supply aspects or authority to move.
    public bool HasSignalPreview;
    public double SignalStopX, SignalPermittedSpeedKmh;
}
public sealed class TrainerAvpDecision {
    public TrainerAvpState State;
    public string Reason;
    public double CurrentLimitKmh, TargetSpeedKmh, PreviewLimitKmh, PreviewDistanceM;
    public double EffectiveBrakeDeceleration;
    public double BrakeHorizonEndM = -1, WorstGradePermille, UnconfirmedGradeStartM = -1;
    public bool HoldingForUnconfirmedGrade, BrakePlanningFault;
    public bool DriveEnabled;
    public bool SuggestedSandRequest;
    public int SuggestedTractionCommand, SuggestedBrakePosition, SuggestedKvtPosition=2;
    public int TractionCommand { get { return DriveEnabled ? SuggestedTractionCommand : 0; } }
    public int BrakePosition { get { return DriveEnabled ? SuggestedBrakePosition : 0; } }
    public int KvtPosition { get { return DriveEnabled ? SuggestedKvtPosition : 0; } }
}
public sealed class TrainerAutovedenieController {
    readonly RouteModel route;
    readonly double serviceDeceleration, referenceMassT;
    TrainerAvpState state = TrainerAvpState.Advisor;
    string reason = "Советчик";
    double nextTractionChange;
    bool planningFaultHold, explicitBrakeRestart;
    string planningFaultReason;
    readonly TrainerTrainBrakeCycle brakeCycle = new TrainerTrainBrakeCycle();
    TrainerTrainBrakeCycleDecision lastBrakeCycle;
    public const double FirstBrakeReduction = .7, MinimumBrakeLapSeconds = 6, TerminalPositionToleranceM = 1;
    // Original POEZD native gravity constant at RVA218C, used by slope forces.
    const double NativeGravity = 9.81;
    // Engineering reserve exceeds the single measured warm80 onset16.5s.
    // Cold/repeated applications still require native validation before AVP admission.
    public const double PneumaticResponseReserveSeconds = 35;
    const double PositionReserveM = 30;
    static double CoastAcceleration(double grade) { return Math.Max(0,-grade*NativeGravity/1000); }
    static double RequiredDistance(double velocity,double decel,double grade) {
        double a=CoastAcceleration(grade),t=PneumaticResponseReserveSeconds,v=velocity+a*t;
        return PositionReserveM+velocity*t+.5*a*t*t+v*v/(2*decel);
    }
    public TrainerAvpState State { get { return state; } }
    // serviceDeceleration is a conservative, verified native service-braking
    // calibration at referenceMassT. No factory/proprietary AVP tuning is implied.
    public TrainerAutovedenieController(RouteModel route, double serviceDeceleration, double referenceMassT) {
        if (route == null) throw new ArgumentNullException("route");
        route.Validate();
        if (!Finite(serviceDeceleration) || serviceDeceleration <= 0 || serviceDeceleration > 1 ||
            !Finite(referenceMassT) || referenceMassT <= 0) throw new ArgumentException("Invalid braking calibration");
        this.route = route; this.serviceDeceleration = serviceDeceleration; this.referenceMassT = referenceMassT;
    }
    public void RequestAutomatic(TrainerAvpInput input) {
        string refusal = Refusal(input);
        if (refusal==null && (!input.HasBrakeFeedback || !Finite(input.MaximumConsistSpeedKmh) ||
            !Finite(input.ChargingPressureBar) || !Finite(input.BrakePipePressureBar) ||
            !Finite(input.IndependentKvtPressureBar) || !Finite(input.MinBogiePressureBar) || !Finite(input.MaxBogiePressureBar)))
            refusal="Нет подтверждённых датчиков нормативного тормозного цикла";
        if (refusal == null) {
            var plan = PlanBraking(input, CurrentLimit(input));
            if (!plan.Valid) refusal = plan.Error;
            else if (plan.OccupiedUnconfirmed) refusal = "Торможение на уклоне под составом не подтверждено";
        }
        if (refusal == null && !input.CanApplyTraction &&
            !(input.HasBrakeFeedback && Finite(input.MaximumConsistSpeedKmh) && input.MaximumConsistSpeedKmh<.3))
            refusal="Сигнал ТМ: включение тяги запрещено";
        if (refusal != null) {
            // A refused new start must not drop an already owned stop hold.
            if (lastBrakeCycle==null || lastBrakeCycle.Kvt!=6 || state != TrainerAvpState.Automatic && state != TrainerAvpState.Finished)
                state = TrainerAvpState.Advisor;
            reason = refusal; return;
        }
        state = TrainerAvpState.Automatic; reason = "Автоведение";
        nextTractionChange = input.Seconds; explicitBrakeRestart=true; planningFaultHold = false; planningFaultReason = null;
    }
    public void RequestAdvisor() { state = TrainerAvpState.Advisor; reason = "Советчик"; planningFaultHold = false; explicitBrakeRestart=false; }
    public void Cancel(string cause) { state = TrainerAvpState.Cancelled; reason = cause; planningFaultHold = false; explicitBrakeRestart=false; }
    static bool Finite(double value) { return !double.IsNaN(value) && !double.IsInfinity(value); }
    string Refusal(TrainerAvpInput input) {
        if (input == null || !Finite(input.X) || !Finite(input.SpeedKmh) || input.SpeedKmh < 0 ||
            !Finite(input.Seconds) || !Finite(input.TrainLengthM) || input.TrainLengthM <= 0 ||
            !Finite(input.TrainMassT) || input.TrainMassT <= 0 ||
            !Finite(input.OperationalLimitKmh) || input.OperationalLimitKmh <= 0 || input.OperationalLimitKmh > 120)
            return "Недопустимые данные движения";
        if (input.HasBrakeFeedback && (!Finite(input.EqualizingReservoirPressure) || input.EqualizingReservoirPressure < 0 || input.EqualizingReservoirPressure > 10))
            return "Недопустимое давление уравнительного резервуара";
        if (input.X - input.TrainLengthM < -0.01 || input.X > route.Length + 0.01)
            return "Состав вне подтверждённого маршрута";
        if (!input.SettingsValid) return "Проверьте данные поезда";
        if (!Enum.IsDefined(typeof(TrainerAvpOperation), input.Operation)) return "Неизвестный режим автоведения";
        if (input.ManualOverride) return "Ручное управление";
        if (input.SafetyIntervention) return "Команда системы безопасности";
        if (!input.Ready) return String.IsNullOrEmpty(input.NotReadyReason) ? "Электровоз не готов" : input.NotReadyReason;
        if (input.Operation == TrainerAvpOperation.SignalFollowing && (!input.HasSignalPreview ||
            !Finite(input.SignalStopX) || input.SignalStopX <= input.X ||
            !Finite(input.SignalPermittedSpeedKmh) || input.SignalPermittedSpeedKmh < 0 || input.SignalPermittedSpeedKmh > 120))
            return "Нет достоверного сигнального задания для режима по удалению";
        if (input.Operation == TrainerAvpOperation.CatchUp)
            return "Алгоритм режима нагон не подтверждён документацией";
        return null;
    }
    sealed class BrakePlan {
        public bool Valid, OccupiedUnconfirmed, GoalIsRouteEnd;
        public string Error;
        public double Deceleration, WorstGrade, HorizonEnd, StopX, HazardStart = -1;
    }
    double CurrentLimit(TrainerAvpInput input) {
        double current = input.OperationalLimitKmh;
        foreach (RouteSpeed limit in route.speed_limits) {
            double start = route.X(limit.km_start), end = route.X(limit.km_end);
            if (end > input.X - input.TrainLengthM && start <= input.X) current = Math.Min(current, limit.speed_kmh);
        }
        return current;
    }
    BrakePlan PlanBraking(TrainerAvpInput input, double currentLimit) {
        var plan = new BrakePlan { StopX = route.Length, HorizonEnd = input.X };
        double baseValue = serviceDeceleration * Math.Min(1, referenceMassT / input.TrainMassT);
        if (!Finite(route.Length) || route.Length <= 0 || route.profile == null || route.profile.Count == 0 ||
            !Finite(currentLimit) || currentLimit <= 0 || !Finite(baseValue) || baseValue <= 0) {
            plan.Error = "Нет подтверждённого профиля торможения"; return plan;
        }
        // Validate the full source coverage, not a shortened route. Grades are
        // already sorted by RouteModel.Validate; a later source change is refused.
        double covered = 0, occupiedWorst = 0;
        double rear = Math.Max(0, input.X - input.TrainLengthM);
        foreach (RouteGrade grade in route.profile) {
            double start = route.X(grade.km_start), end = route.X(grade.km_end);
            if (!Finite(start) || !Finite(end) || !Finite(grade.grade_permille) || Math.Abs(grade.grade_permille) > 100 ||
                end <= start || start < -0.001 || end > route.Length + 0.001 || Math.Abs(start - covered) > 0.001) {
                plan.Error = "Разрыв, перекрытие или недопустимый профиль торможения"; return plan;
            }
            covered = end;
            if (end > rear && start <= input.X) occupiedWorst = Math.Min(occupiedWorst, grade.grade_permille);
            double segmentBound = baseValue + Math.Min(0, grade.grade_permille) * NativeGravity / 1000;
            if (start > input.X && segmentBound <= 0 && (plan.HazardStart < 0 || start < plan.HazardStart))
                plan.HazardStart = start;
        }
        if (Math.Abs(covered - route.Length) > 0.001) {
            plan.Error = "Неполный профиль торможения"; return plan;
        }
        plan.Valid = true;
        plan.Deceleration = baseValue + occupiedWorst * NativeGravity / 1000;
        plan.WorstGrade = occupiedWorst;
        if (plan.Deceleration <= 0) { plan.OccupiedUnconfirmed = true; return plan; }
        // A future slope without a positive bound is an explicit stop before
        // entry. It is not completion of the course or permission to enter it.
        if (plan.HazardStart >= 0) plan.StopX = Math.Min(plan.StopX, plan.HazardStart - TerminalPositionToleranceM);
        if (input.Operation == TrainerAvpOperation.SignalFollowing && input.HasSignalPreview &&
            Finite(input.SignalStopX) && input.SignalStopX > input.X && input.SignalPermittedSpeedKmh == 0)
            plan.StopX = Math.Min(plan.StopX, input.SignalStopX);
        plan.GoalIsRouteEnd = plan.StopX == route.Length;
        // Prospective speed includes the whole-train current speed cap: a
        // standstill must not permit acceleration toward an unseen bad slope.
        double velocity = Math.Max(Math.Max(input.SpeedKmh,Finite(input.MaximumConsistSpeedKmh)?input.MaximumConsistSpeedKmh:input.SpeedKmh), currentLimit) / 3.6;
        double endX = Math.Max(input.X, Math.Min(plan.StopX, input.X + RequiredDistance(velocity,plan.Deceleration,occupiedWorst)));
        // Expansion is monotone. Each increase in the worst adverse grade comes
        // from a finite source segment, so Count+1 iterations suffice.
        for (int iteration = 0; iteration <= route.profile.Count; iteration++) {
            double worst = occupiedWorst;
            foreach (RouteGrade grade in route.profile) {
                // Half-open intervals exclude a grade starting exactly at the
                // future stop boundary; current head occupancy was checked above.
                if (route.X(grade.km_end) > rear && route.X(grade.km_start) < endX)
                    worst = Math.Min(worst, grade.grade_permille);
            }
            double value = baseValue + worst * NativeGravity / 1000;
            plan.Deceleration = value; plan.WorstGrade = worst; plan.HorizonEnd = endX;
            if (!Finite(value) || value <= 0) {
                plan.Valid = false; plan.Error = "Нет положительной границы замедления в тормозном горизонте"; return plan;
            }
            double expanded = Math.Max(input.X, Math.Min(plan.StopX, input.X + RequiredDistance(velocity,value,worst)));
            if (expanded <= endX + 0.000001) return plan;
            endX = expanded;
        }
        plan.Valid = false; plan.Error = "Тормозной горизонт не сошёлся"; return plan;
    }
    static TrainerTrainBrakeCycleInput BrakeObservation(TrainerAvpInput i,bool owns,bool demand,bool stop,bool ended,bool restart,bool downhill) {
        if(i==null)return null;
        bool sensors=i.HasBrakeFeedback && Finite(i.MaximumConsistSpeedKmh) &&
            Finite(i.ChargingPressureBar) && Finite(i.BrakePipePressureBar) &&
            Finite(i.IndependentKvtPressureBar) && Finite(i.MinBogiePressureBar) && Finite(i.MaxBogiePressureBar);
        TrainerTrainBrakeKind kind=i.BrakeCommandsKnown && i.ObservedKm>=6?TrainerTrainBrakeKind.Emergency:
            i.ChargingPressureBar-i.EqualizingReservoirPressure>=1.5?TrainerTrainBrakeKind.FullService:TrainerTrainBrakeKind.Service;
        return new TrainerTrainBrakeCycleInput {Seconds=i.Seconds,UrBar=i.EqualizingReservoirPressure,ChargeBar=i.ChargingPressureBar,
            ActualTmBar=i.BrakePipePressureBar,HeadSpeedKmh=i.SpeedKmh,MaximumConsistSpeedKmh=i.MaximumConsistSpeedKmh,
            IndependentKvtPressureBar=i.IndependentKvtPressureBar,MinBogiePressureBar=i.MinBogiePressureBar,MaxBogiePressureBar=i.MaxBogiePressureBar,
            SensorsKnown=sensors,Ready=sensors&&!i.SafetyIntervention,AllAutoReleased=i.AutomaticBrakesReleased,AllBrakesReleased=i.BrakesReleased,
            WagonTcFeedbackKnown=i.HasWagonTcFeedback,MinimumWagonTcBar=i.MinimumWagonTcBar,
            OwnsCommands=owns,BrakeDemand=demand,StopRequired=stop,Ended=ended,ExplicitRestart=restart,IsDownhill=downhill,
            ObservedCommandsKnown=i.BrakeCommandsKnown,ObservedKm=i.ObservedKm,ObservedKvt=i.ObservedKvt,ApplicationKind=kind,Axles=i.WagonAxles,
            HasAmbientTemperature=i.AmbientTemperatureKnown,AmbientTemperatureC=i.AmbientTemperatureC,SandConfirmed=i.SandCommandAccepted};
    }
    public TrainerAvpDecision Step(TrainerAvpInput input) {
        string refusal = Refusal(input);
        if ((state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished) && refusal != null) Cancel(refusal);
        bool owned=state==TrainerAvpState.Automatic || state==TrainerAvpState.Finished;
        if (!owned) brakeCycle.Observe(BrakeObservation(input,false,false,false,false,false,false));
        var result = new TrainerAvpDecision { State = state, Reason = refusal ?? reason,
            SuggestedBrakePosition = 2, PreviewDistanceM = -1 };
        if (input == null || !Finite(input.X) || !Finite(input.SpeedKmh) || !Finite(input.TrainLengthM) ||
            input.TrainLengthM <= 0 || !Finite(input.TrainMassT) || input.TrainMassT <= 0) return result;
        double current = CurrentLimit(input);
        if (!Finite(current) || current <= 0) return result;
        var plan = PlanBraking(input, current);
        double decel = plan.Deceleration;
        result.EffectiveBrakeDeceleration = decel;
        result.CurrentLimitKmh = current;
        result.BrakeHorizonEndM = plan.HorizonEnd; result.WorstGradePermille = plan.WorstGrade;
        result.UnconfirmedGradeStartM = plan.HazardStart;
        if (!plan.Valid) {
            if (state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished) Cancel(plan.Error);
            result.State = state; result.Reason = refusal ?? plan.Error; result.BrakePlanningFault = true; return result;
        }
        if (plan.OccupiedUnconfirmed && (state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished)) {
            // Unexpected entry must not release KM ownership into physical II.
            // Keep the last established service step/coast until the operator or
            // native safety takes over. This is a diagnostic fault, not acceptance.
            planningFaultHold = true;
            if (state == TrainerAvpState.Finished) state = TrainerAvpState.Automatic;
            planningFaultReason = "Неподтверждённый уклон под составом: торможение сохраняется";
        }
        bool planningFault = plan.OccupiedUnconfirmed || planningFaultHold;
        result.BrakePlanningFault = planningFault;
        double target = current;
        // Include adverse acceleration while pneumatic force propagates, then
        // the conditional net brake bound. Admission remains independently gated.
        double reserve = PositionReserveM;
        if (!planningFault) {
            foreach (RouteSpeed limit in route.speed_limits) {
                double start = route.X(limit.km_start);
                if (start <= input.X) continue;
                ApplyEnvelope(ref target, result, limit.speed_kmh, start - input.X, reserve, decel, plan.WorstGrade);
            }
            ApplyEnvelope(ref target, result, 0, Math.Max(0, route.Length - input.X), reserve, decel, plan.WorstGrade);
            if (input.Operation == TrainerAvpOperation.SignalFollowing && input.HasSignalPreview && Finite(input.SignalStopX)) {
                ApplyEnvelope(ref target, result, Math.Max(0, input.SignalPermittedSpeedKmh),
                    input.SignalStopX - input.X, reserve, decel, plan.WorstGrade);
            }
            if (plan.HazardStart >= 0)
                ApplyEnvelope(ref target, result, 0, Math.Max(0, plan.HazardStart - TerminalPositionToleranceM - input.X), reserve, decel, plan.WorstGrade);
        } else target = 0;
        bool hazardStop = plan.HazardStart >= 0 &&
            plan.HazardStart - TerminalPositionToleranceM - input.X <= reserve + TerminalPositionToleranceM && input.SpeedKmh < .3 && Finite(input.MaximumConsistSpeedKmh) && input.MaximumConsistSpeedKmh < .3;
        bool mandatoryHold = hazardStop || planningFault;
        if (mandatoryHold) target = 0;
        result.HoldingForUnconfirmedGrade = mandatoryHold;
        result.TargetSpeedKmh = Math.Max(0, target);
        // One metre accounts for native longitudinal settling around the planned stop.
        bool endStop = plan.GoalIsRouteEnd && !planningFault && route.Length - input.X <= reserve + TerminalPositionToleranceM && input.SpeedKmh < 0.3 &&
            Finite(input.MaximumConsistSpeedKmh) && input.MaximumConsistSpeedKmh<.3;
        if (state == TrainerAvpState.Automatic && endStop) {
            state = TrainerAvpState.Finished; reason = "Конец маршрута";
        }
        if (!owned) return result;
        double controlBand = Math.Min(2, Math.Max(.1, target*.2));
        double controlSpeed=Finite(input.MaximumConsistSpeedKmh)?Math.Max(input.SpeedKmh,input.MaximumConsistSpeedKmh):input.SpeedKmh;
        bool needBrake=controlSpeed>current+.01 || controlSpeed>target+Math.Min(.7,Math.Max(.05,target*.1));
        bool mustStop=state==TrainerAvpState.Finished || endStop || mandatoryHold;
        var brake=brakeCycle.Step(BrakeObservation(input,true,needBrake||mustStop,mustStop,
            state==TrainerAvpState.Finished,explicitBrakeRestart&&!needBrake&&!mustStop,plan.WorstGrade<0));
        explicitBrakeRestart=false;lastBrakeCycle=brake;
        result.SuggestedBrakePosition=brake.Km;result.SuggestedKvtPosition=brake.Kvt;result.SuggestedSandRequest=brake.RequestSand;
        if (brake.State==TrainerTrainBrakeCycleState.StoppedHold || state==TrainerAvpState.Finished || mandatoryHold)
            result.TargetSpeedKmh=0;
        if (!brake.CanApplyTraction || !input.CanApplyTraction || controlSpeed>=Math.Max(0,target-controlBand) || target<=.01)
            result.SuggestedTractionCommand=5;
        else if(input.Seconds>=nextTractionChange && input.TractionPosition<65) {
            result.SuggestedTractionCommand=1;nextTractionChange=input.Seconds+1;
        }
        result.State=state;
        result.Reason=planningFaultHold?planningFaultReason:hazardStop?"Остановка перед неподтверждённым уклоном":brake.CanApplyTraction?reason:brake.Reason;
        if(brake.CanApplyTraction&&!input.CanApplyTraction)result.Reason="АВ: штатный сигнал ТМ запрещает тягу, выдержки и тормозная история сохранены";
        // Hold the stop after route completion; a driver's action releases
        // ownership through the same cancellation path as during the journey.
        result.DriveEnabled = (state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished) && refusal == null;
        result.HoldingForUnconfirmedGrade = result.HoldingForUnconfirmedGrade && result.DriveEnabled;
        return result;
    }
    static void ApplyEnvelope(ref double target, TrainerAvpDecision result, double limitKmh,
        double distance, double reserve, double decel, double grade) {
        double limitMs=limitKmh/3.6,a=CoastAcceleration(grade),t=PneumaticResponseReserveSeconds;
        // Invert s=v*t+a*t²/2+((v+a*t)²-u²)/(2*d). Uphill gives no credit.
        double permitted=Math.Max(0,-t*(decel+a)+Math.Sqrt(Math.Max(0,
            t*t*decel*(decel+a)+limitMs*limitMs+2*decel*Math.Max(0,distance-reserve))))*3.6;
        if (permitted < target) {
            target = permitted; result.PreviewLimitKmh = limitKmh; result.PreviewDistanceM = Math.Max(0, distance);
        }
    }
}
