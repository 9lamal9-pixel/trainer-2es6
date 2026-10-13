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
    bool braking, locomotiveHeld;
    double brakeApplicationStarted;
    double brakeReservoirTarget, brakeLapSince = -1;
    public const double FirstBrakeReduction = .7, MinimumBrakeLapSeconds = 6, TerminalPositionToleranceM = 1;
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
        if (refusal == null && !input.CanApplyTraction) refusal="Сигнал ТМ: включение тяги запрещено";
        if (refusal != null) { state = TrainerAvpState.Advisor; reason = refusal; return; }
        state = TrainerAvpState.Automatic; reason = "Автоведение";
        nextTractionChange = input.Seconds; braking = false; locomotiveHeld = false;
    }
    public void RequestAdvisor() { state = TrainerAvpState.Advisor; reason = "Советчик"; braking = false; }
    public void Cancel(string cause) { state = TrainerAvpState.Cancelled; reason = cause; braking = false; }
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
    double Deceleration(TrainerAvpInput input) {
        double baseValue = serviceDeceleration * Math.Min(1, referenceMassT / input.TrainMassT);
        // Evaluate the most adverse gradient ahead and under the consist. A
        // downhill segment reduces available deceleration; never clip it up.
        double worstGrade = 0;
        foreach (RouteGrade grade in route.profile) {
            if (route.X(grade.km_end) >= input.X - input.TrainLengthM && route.X(grade.km_start) <= route.Length)
                worstGrade = Math.Min(worstGrade, grade.grade_permille);
        }
        return baseValue + worstGrade * 9.80665 / 1000;
    }
    public TrainerAvpDecision Step(TrainerAvpInput input) {
        string refusal = Refusal(input);
        if ((state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished) && refusal != null) Cancel(refusal);
        var result = new TrainerAvpDecision { State = state, Reason = refusal ?? reason,
            SuggestedBrakePosition = 2, PreviewDistanceM = -1 };
        if (input == null || !Finite(input.X) || !Finite(input.SpeedKmh) || !Finite(input.TrainLengthM) ||
            input.TrainLengthM <= 0 || !Finite(input.TrainMassT) || input.TrainMassT <= 0) return result;
        double current = input.OperationalLimitKmh;
        if (!Finite(current) || current <= 0) return result;
        foreach (RouteSpeed limit in route.speed_limits) {
            double start = route.X(limit.km_start), end = route.X(limit.km_end);
            if (end > input.X - input.TrainLengthM && start <= input.X) current = Math.Min(current, limit.speed_kmh);
        }
        double decel = Deceleration(input);
        result.EffectiveBrakeDeceleration = decel;
        result.CurrentLimitKmh = current;
        if (decel <= 0) {
            if (state == TrainerAvpState.Automatic) Cancel("Торможение на уклоне не подтверждено");
            result.State = state; result.Reason = "Торможение на уклоне не подтверждено"; return result;
        }
        double target = current;
        // Initial engineering allowance for pneumatic propagation. Native
        // journey probes must verify this value before operational acceptance.
        double reserve = Math.Max(30, input.SpeedKmh / 3.6 * 15);
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
        result.TargetSpeedKmh = Math.Max(0, target);
        // One metre accounts for native longitudinal settling around the planned stop.
        bool endStop = route.Length - input.X <= reserve + TerminalPositionToleranceM && input.SpeedKmh < 0.3;
        if (state == TrainerAvpState.Automatic && endStop) {
            state = TrainerAvpState.Finished; reason = "Конец маршрута";
        }
        // Keep the locomotive on native independent stage III while a stopped
        // consist finishes automatic brake release. Never assign cylinder pressure.
        if (input.HasBrakeFeedback && !input.AutomaticBrakesReleased && input.SpeedKmh < .5) locomotiveHeld=true;
        if (input.AutomaticBrakesReleased) locomotiveHeld=false;
        result.SuggestedKvtPosition=locomotiveHeld || state==TrainerAvpState.Finished ? 3 : 2;
        if (state == TrainerAvpState.Finished) result.TargetSpeedKmh=0;
        double controlBand = Math.Min(2, Math.Max(.1, target * .2));
        bool needBrake = input.SpeedKmh > current + .01 ||
            input.SpeedKmh > target + Math.Min(.7, Math.Max(.05, target * .1));
        if ((needBrake || state == TrainerAvpState.Finished || endStop) && !braking) {
            braking = true; brakeLapSince = -1; brakeApplicationStarted = input.Seconds;
            // First loaded-train service step, then native IV lap with feed.
            // This uses measured UR; no reservoir/pipe pressure is assigned.
            brakeReservoirTarget = Math.Max(0, input.EqualizingReservoirPressure - FirstBrakeReduction);
        }
        bool lapHeld = !input.HasBrakeFeedback || brakeLapSince >= 0 && input.Seconds - brakeLapSince >= MinimumBrakeLapSeconds;
        if (braking && !needBrake && state != TrainerAvpState.Finished && !endStop && lapHeld &&
            input.SpeedKmh < Math.Max(0, target - controlBand)) braking = false;
        if (state == TrainerAvpState.Finished || braking || endStop) {
            result.SuggestedTractionCommand = 5; // native Vybeg, never synthetic force
            if (input.HasBrakeFeedback && input.EqualizingReservoirPressure <= brakeReservoirTarget + .01) {
                if (brakeLapSince < 0) brakeLapSince = input.Seconds;
                result.SuggestedBrakePosition = 4; // native IV, lap with feed
            } else result.SuggestedBrakePosition = 5; // native V until measured step
        } else if (!input.CanApplyTraction || !input.BrakesReleased || input.SpeedKmh >= Math.Max(0, target - controlBand) || target <= .01) {
            result.SuggestedTractionCommand = 5;
        } else if (input.Seconds >= nextTractionChange && input.TractionPosition < 65) {
            result.SuggestedTractionCommand = 1; // one native controller step
            nextTractionChange = input.Seconds + 1;
        }
        if (braking && input.HasBrakeFeedback && input.EqualizingReservoirPressure > brakeReservoirTarget + .01 &&
            input.Seconds - brakeApplicationStarted > 15) Cancel("Нет подтверждённого снижения давления УР при команде V");
        if (state == TrainerAvpState.Automatic && !input.CanApplyTraction && !braking && input.AutomaticBrakesReleased)
            Cancel("Сигнал ТМ не снят после отпуска; повторное включение только после готовности");
        result.State = state; result.Reason = refusal ?? reason;
        if (state == TrainerAvpState.Automatic && !input.CanApplyTraction && refusal == null)
            result.Reason="АВ: тяга заблокирована сигналом ТМ, управление тормозами сохраняется";
        // Hold the stop after route completion; a driver's action releases
        // ownership through the same cancellation path as during the journey.
        result.DriveEnabled = (state == TrainerAvpState.Automatic || state == TrainerAvpState.Finished) && refusal == null;
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
