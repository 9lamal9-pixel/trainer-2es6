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
    public bool HasBrakeFeedback;
    public double EqualizingReservoirPressure;
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
    bool braking, locomotiveHeld, planningFaultHold;
    string planningFaultReason;
    double brakeApplicationStarted;
    double brakeReservoirTarget, brakeLapSince = -1;
    public const double FirstBrakeReduction = .7, MinimumBrakeLapSeconds = 6, TerminalPositionToleranceM = 1;
    // Original POEZD native gravity constant at RVA218C, used by slope forces.
    const double NativeGravity = 9.81;
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
        if (refusal == null) {
            var plan = PlanBraking(input, CurrentLimit(input));
            if (!plan.Valid) refusal = plan.Error;
            else if (plan.OccupiedUnconfirmed) refusal = "Торможение на уклоне под составом не подтверждено";
        }
        if (refusal == null && !input.CanApplyTraction) refusal="Сигнал ТМ: включение тяги запрещено";
        if (refusal != null) {
            // A refused new start must not drop an already owned stop hold.
            if (!locomotiveHeld || state != TrainerAvpState.Automatic && state != TrainerAvpState.Finished)
                state = TrainerAvpState.Advisor;
            reason = refusal; return;
        }
        state = TrainerAvpState.Automatic; reason = "Автоведение";
        nextTractionChange = input.Seconds; braking = false; locomotiveHeld = false; planningFaultHold = false; planningFaultReason = null;
    }
    public void RequestAdvisor() { state = TrainerAvpState.Advisor; reason = "Советчик"; braking = false; locomotiveHeld = false; planningFaultHold = false; }
    public void Cancel(string cause) { state = TrainerAvpState.Cancelled; reason = cause; braking = false; locomotiveHeld = false; planningFaultHold = false; }
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
        double velocity = Math.Max(input.SpeedKmh, currentLimit) / 3.6;
        double reserve = Math.Max(30, velocity * 15);
        double endX = Math.Max(input.X, Math.Min(plan.StopX, input.X + reserve + velocity * velocity / (2 * plan.Deceleration)));
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
            double expanded = Math.Max(input.X, Math.Min(plan.StopX, input.X + reserve + velocity * velocity / (2 * value)));
            if (expanded <= endX + 0.000001) return plan;
            endX = expanded;
        }
        plan.Valid = false; plan.Error = "Тормозной горизонт не сошёлся"; return plan;
    }
    public TrainerAvpDecision Step(TrainerAvpInput input) {
        string refusal = Refusal(input);
        if ((state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished) && refusal != null) Cancel(refusal);
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
        // Initial engineering allowance for pneumatic propagation. Native
        // journey probes must verify this value before operational acceptance.
        double reserve = Math.Max(30, input.SpeedKmh / 3.6 * 15);
        if (!planningFault) {
            foreach (RouteSpeed limit in route.speed_limits) {
                double start = route.X(limit.km_start);
                if (start <= input.X) continue;
                ApplyEnvelope(ref target, result, limit.speed_kmh, start - input.X, reserve, decel);
            }
            ApplyEnvelope(ref target, result, 0, Math.Max(0, route.Length - input.X), reserve, decel);
            if (input.Operation == TrainerAvpOperation.SignalFollowing && input.HasSignalPreview && Finite(input.SignalStopX)) {
                ApplyEnvelope(ref target, result, Math.Max(0, input.SignalPermittedSpeedKmh),
                    input.SignalStopX - input.X, reserve, decel);
            }
            if (plan.HazardStart >= 0)
                ApplyEnvelope(ref target, result, 0, Math.Max(0, plan.HazardStart - TerminalPositionToleranceM - input.X), reserve, decel);
        } else target = 0;
        bool hazardStop = plan.HazardStart >= 0 &&
            plan.HazardStart - TerminalPositionToleranceM - input.X <= reserve + TerminalPositionToleranceM && input.SpeedKmh < .3;
        bool mandatoryHold = hazardStop || planningFault;
        if (mandatoryHold) target = 0;
        result.HoldingForUnconfirmedGrade = mandatoryHold;
        result.TargetSpeedKmh = Math.Max(0, target);
        // One metre accounts for native longitudinal settling around the planned stop.
        bool endStop = plan.GoalIsRouteEnd && !planningFault && route.Length - input.X <= reserve + TerminalPositionToleranceM && input.SpeedKmh < 0.3;
        if (state == TrainerAvpState.Automatic && endStop) {
            state = TrainerAvpState.Finished; reason = "Конец маршрута";
        }
        // A stopped train needs the full native independent brake (VI). The
        // stop hold survives automatic-brake release and longitudinal motion;
        // only an explicit accepted start or cancellation releases ownership.
        // Moving pre-release stage III is a separate, unvalidated procedure.
        bool ownsAutomatic = state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished;
        if (ownsAutomatic && input.SpeedKmh < .3 &&
            (state == TrainerAvpState.Finished || mandatoryHold || input.HasBrakeFeedback && !input.AutomaticBrakesReleased))
            locomotiveHeld = true;
        result.SuggestedKvtPosition = ownsAutomatic && locomotiveHeld ? 6 : 2;
        if (state == TrainerAvpState.Finished || locomotiveHeld) result.TargetSpeedKmh=0;
        double controlBand = Math.Min(2, Math.Max(.1, target * .2));
        bool needBrake = input.SpeedKmh > current + .01 ||
            input.SpeedKmh > target + Math.Min(.7, Math.Max(.05, target * .1));
        if ((needBrake || state == TrainerAvpState.Finished || endStop || mandatoryHold) && !braking) {
            braking = true; brakeLapSince = -1; brakeApplicationStarted = input.Seconds;
            // First loaded-train service step, then native IV lap with feed.
            // This uses measured UR; no reservoir/pipe pressure is assigned.
            brakeReservoirTarget = Math.Max(0, input.EqualizingReservoirPressure - FirstBrakeReduction);
        }
        bool lapHeld = !input.HasBrakeFeedback || brakeLapSince >= 0 && input.Seconds - brakeLapSince >= MinimumBrakeLapSeconds;
        if (braking && !needBrake && state != TrainerAvpState.Finished && !endStop && !mandatoryHold && lapHeld &&
            input.SpeedKmh < Math.Max(0, target - controlBand)) braking = false;
        if (state == TrainerAvpState.Finished || braking || endStop || mandatoryHold) {
            result.SuggestedTractionCommand = 5; // native Vybeg, never synthetic force
            if (input.HasBrakeFeedback && input.EqualizingReservoirPressure <= brakeReservoirTarget + .01) {
                if (brakeLapSince < 0) brakeLapSince = input.Seconds;
                result.SuggestedBrakePosition = 4; // native IV, lap with feed
            } else result.SuggestedBrakePosition = 5; // native V until measured step
        } else if (locomotiveHeld || !input.CanApplyTraction || !input.BrakesReleased || input.SpeedKmh >= Math.Max(0, target - controlBand) || target <= .01) {
            result.SuggestedTractionCommand = 5;
        } else if (input.Seconds >= nextTractionChange && input.TractionPosition < 65) {
            result.SuggestedTractionCommand = 1; // one native controller step
            nextTractionChange = input.Seconds + 1;
        }
        if (braking && input.HasBrakeFeedback && input.EqualizingReservoirPressure > brakeReservoirTarget + .01 &&
            input.Seconds - brakeApplicationStarted > 15) {
            if (planningFaultHold) planningFaultReason = "Неподтверждённый уклон и нет снижения УР: команда остановки сохраняется";
            else if (locomotiveHeld) reason = "Нет снижения УР: остановка удерживается полным КВТ, требуется действие машиниста";
            else Cancel("Нет подтверждённого снижения давления УР при команде V");
        }
        if (state == TrainerAvpState.Automatic && !input.CanApplyTraction && !braking && !locomotiveHeld && input.AutomaticBrakesReleased)
            Cancel("Сигнал ТМ не снят после отпуска; повторное включение только после готовности");
        result.State = state; result.Reason = refusal ?? (planningFaultHold ? planningFaultReason : hazardStop ? "Остановка перед неподтверждённым уклоном" : reason);
        if (state == TrainerAvpState.Automatic && !input.CanApplyTraction && refusal == null)
            result.Reason="АВ: тяга заблокирована сигналом ТМ, управление тормозами сохраняется";
        // Hold the stop after route completion; a driver's action releases
        // ownership through the same cancellation path as during the journey.
        result.DriveEnabled = (state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished) && refusal == null;
        result.HoldingForUnconfirmedGrade = result.HoldingForUnconfirmedGrade && result.DriveEnabled;
        return result;
    }
    static void ApplyEnvelope(ref double target, TrainerAvpDecision result, double limitKmh,
        double distance, double reserve, double decel) {
        double limitMs = limitKmh / 3.6;
        double permitted = Math.Sqrt(Math.Max(0, limitMs * limitMs + 2 * decel * Math.Max(0, distance - reserve))) * 3.6;
        if (permitted < target) {
            target = permitted; result.PreviewLimitKmh = limitKmh; result.PreviewDistanceM = Math.Max(0, distance);
        }
    }
}
