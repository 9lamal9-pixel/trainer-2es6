using System;
using System.IO;
using System.Collections.Generic;
using System.Web.Script.Serialization;

public static class AutovedenieControllerProbe {
    static readonly List<object> checks = new List<object>();
    static void Check(string name, bool passed) {
        checks.Add(new { name = name, passed = passed });
        if (!passed) throw new Exception(name);
    }
    static RouteModel Route() {
        return new RouteModel { map_start_km = 10, map_end_km = 0, initial_km = 8, direction = -1,
            profile = new List<RouteGrade> { new RouteGrade { km_start = 10, km_end = 0, grade_permille = 0 } },
            speed_limits = new List<RouteSpeed> {
                new RouteSpeed { km_start = 10, km_end = 6, speed_kmh = 80 },
                new RouteSpeed { km_start = 6, km_end = 3, speed_kmh = 30 },
                new RouteSpeed { km_start = 3, km_end = 0, speed_kmh = 80 }
            }
        };
    }
    static TrainerAvpInput Input(double x, double speed) {
        return new TrainerAvpInput { X = x, SpeedKmh = speed, TrainLengthM = 1000, TrainMassT = 6200,
            Seconds = 0, Ready = true, SettingsValid = true, OperationalLimitKmh = 80 };
    }
    public static int Main() {
        TrainerSession.EnsureStarted();
        bool passed = false; string error = null;
        try {
            bool nullNativeRejected = false;
            try { TrainerAutovedenieInterface.Attach(IntPtr.Zero, null); }
            catch (ArgumentException) { nullNativeRejected = true; }
            Check("interface rejects absent native pointer before any hook", nullNativeRejected);
            var controller = new TrainerAutovedenieController(Route(), .20, 6200);
            var input = Input(2000, 0);
            var advice = controller.Step(input);
            Check("advisor calculates without command ownership", !advice.DriveEnabled && advice.TractionCommand == 0 && advice.BrakePosition == 0);
            controller.RequestAutomatic(input);
            var acceleration = controller.Step(input);
            Check("explicit start requests native step and running KM", acceleration.DriveEnabled && acceleration.TractionCommand == 1 && acceleration.BrakePosition == 2);
            input.Seconds = .1;
            Check("controller step rate is limited", controller.Step(input).TractionCommand == 0);
            input.Seconds = 1;
            Check("next controller step after interval", controller.Step(input).TractionCommand == 1);
            input.X = 3000; input.SpeedKmh = 80;
            var preview = controller.Step(input);
            Check("lower limit is anticipated before its boundary", preview.TargetSpeedKmh < 80 && preview.PreviewLimitKmh == 30 && preview.PreviewDistanceM == 1000);
            Check("anticipatory service braking removes traction", preview.TractionCommand == 5 && preview.BrakePosition == 5);
            input.X = 7500; input.SpeedKmh = 20;
            Check("limit retained until whole train clears", controller.Step(input).CurrentLimitKmh == 30);
            input.SpeedKmh = 30.1;
            Check("hard current limit immediately requests service brake", controller.Step(input).BrakePosition == 5);
            input.SpeedKmh = 20;
            input.X = 8000;
            Check("limit released at rear boundary", controller.Step(input).CurrentLimitKmh == 80);
            input.ManualOverride = true;
            Check("manual action cancels ownership", controller.Step(input).State == TrainerAvpState.Cancelled && !controller.Step(input).DriveEnabled);
            input.ManualOverride = false;
            Check("return to neutral does not restart", !controller.Step(input).DriveEnabled && controller.State == TrainerAvpState.Cancelled);
            controller.RequestAutomatic(input);
            Check("explicit restart reacquires ownership", controller.Step(input).DriveEnabled);
            input.SafetyIntervention = true;
            Check("safety cancels without issuing commands", !controller.Step(input).DriveEnabled && controller.Step(input).TractionCommand == 0 && controller.Step(input).BrakePosition == 0);
            input.SafetyIntervention = false;
            Check("safety clear does not restart", !controller.Step(input).DriveEnabled);
            input.Ready = false; controller.RequestAutomatic(input);
            Check("unready start refused", !controller.Step(input).DriveEnabled);
            input.Ready = true; input.SettingsValid = false; controller.RequestAutomatic(input);
            Check("invalid consist settings refused", !controller.Step(input).DriveEnabled);
            input.SettingsValid = true; input.Operation = TrainerAvpOperation.SignalFollowing; controller.RequestAutomatic(input);
            Check("mode po udaleniyu requires live signal data", !controller.Step(input).DriveEnabled);
            input.HasSignalPreview = true; input.SignalStopX = input.X+100; input.SignalPermittedSpeedKmh = 0; input.SpeedKmh = 20; controller.RequestAutomatic(input);
            Check("confirmed signal target governs adapted mode po udaleniyu", controller.Step(input).DriveEnabled && controller.Step(input).BrakePosition == 5);
            input.Operation = TrainerAvpOperation.CatchUp; controller.RequestAutomatic(input);
            Check("undocumented catch-up algorithm refuses start", !controller.Step(input).DriveEnabled);
            input.Operation = (TrainerAvpOperation)99; controller.RequestAutomatic(input);
            Check("unknown operating mode refuses start", !controller.Step(input).DriveEnabled);
            input.Operation = TrainerAvpOperation.Ordinary; input.X = 9990; input.SpeedKmh = 0;
            controller.RequestAutomatic(input); var finish = controller.Step(input);
            Check("route end completes and holds service brake", finish.State == TrainerAvpState.Finished && finish.DriveEnabled && finish.TractionCommand == 5 && finish.BrakePosition == 5 && finish.TargetSpeedKmh == 0);
            input.X = 9950; input.Seconds = 1;
            var rolledBack = controller.Step(input);
            Check("completed stop never accelerates after coordinate correction", rolledBack.State == TrainerAvpState.Finished && rolledBack.TractionCommand == 5 && rolledBack.BrakePosition == 5);
            input.ManualOverride = true;
            Check("driver can take over completed stop", !controller.Step(input).DriveEnabled);
            var slope = Route(); slope.profile[0].grade_permille = -30;
            var downhill = new TrainerAutovedenieController(slope, .20, 6200); input = Input(2000, 30);
            downhill.RequestAutomatic(input); var refused = downhill.Step(input);
            Check("insufficient downhill braking refuses automatic commands", !refused.DriveEnabled && refused.EffectiveBrakeDeceleration < 0);
            var mass = new TrainerAutovedenieController(Route(), .20, 6200); input.TrainMassT = 12400;
            Check("heavier train reduces calibrated braking envelope", Math.Abs(mass.Step(input).EffectiveBrakeDeceleration - .10) < .0001);
            input.X = 500;
            mass.RequestAutomatic(input);
            Check("train outside source route cannot start", !mass.Step(input).DriveEnabled);
            foreach (int lowSpeed in new int[] { 1, 2 }) {
                var lowRoute = Route(); foreach (var limit in lowRoute.speed_limits) limit.speed_kmh = lowSpeed;
                var slow = new TrainerAutovedenieController(lowRoute, .20, 6200); var slowInput = Input(2000, 0);
                slow.RequestAutomatic(slowInput);
                Check("start under low limit " + lowSpeed, slow.Step(slowInput).TractionCommand == 1);
                slowInput.SpeedKmh = lowSpeed + 1; slowInput.Seconds = 1;
                Check("service braking under low limit " + lowSpeed, slow.Step(slowInput).BrakePosition == 5);
                slowInput.SpeedKmh = 0; slowInput.Seconds = 2;
                Check("braking releases under low limit " + lowSpeed, slow.Step(slowInput).BrakePosition == 2);
            }
            var waiting = new TrainerAutovedenieController(Route(), .20, 6200); var waitingInput = Input(2000, 0);
            waitingInput.BrakesReleased = false; waiting.RequestAutomatic(waitingInput);
            Check("traction waits for measured brake release", waiting.Step(waitingInput).TractionCommand == 5);
            waitingInput.BrakesReleased = true; waitingInput.Seconds = 1;
            Check("traction resumes after measured release", waiting.Step(waitingInput).TractionCommand == 1);
            var graduated = new TrainerAutovedenieController(Route(), .20, 6200);
            var brakeInput = Input(3000, 80); brakeInput.HasBrakeFeedback=true; brakeInput.EqualizingReservoirPressure=5;
            graduated.RequestAutomatic(brakeInput);
            Check("measured service step starts in V", graduated.Step(brakeInput).BrakePosition==5);
            brakeInput.EqualizingReservoirPressure=4.5; brakeInput.Seconds=1;
            Check("V continues until measured UR reduction", graduated.Step(brakeInput).BrakePosition==5);
            brakeInput.EqualizingReservoirPressure=4.3; brakeInput.Seconds=2;
            Check("native IV holds the measured first brake step", graduated.Step(brakeInput).BrakePosition==4);
            brakeInput.SpeedKmh=0; brakeInput.Seconds=3;
            Check("brake lap observes minimum documented duration", graduated.Step(brakeInput).BrakePosition==4);
            brakeInput.Seconds=8; brakeInput.BrakesReleased=false;
            var releasedStep=graduated.Step(brakeInput);
            Check("step releases in II and waits for physical cylinder release", releasedStep.BrakePosition==2 && releasedStep.TractionCommand==5);
            brakeInput.EqualizingReservoirPressure=-1; brakeInput.Seconds=9;
            Check("invalid measured UR cancels ownership", !graduated.Step(brakeInput).DriveEnabled);
            var holdController = new TrainerAutovedenieController(Route(), .20, 6200);
            var holdInput = Input(3000, 0); holdInput.HasBrakeFeedback=true; holdInput.EqualizingReservoirPressure=5;
            holdInput.AutomaticBrakesReleased=false; holdInput.BrakesReleased=false; holdController.RequestAutomatic(holdInput);
            Check("stopped locomotive holds native KVTIII while automatic brakes release", holdController.Step(holdInput).KvtPosition==3);
            holdInput.SpeedKmh=1; holdInput.Seconds=1;
            Check("independent hold is latched through longitudinal transient", holdController.Step(holdInput).KvtPosition==3);
            holdInput.AutomaticBrakesReleased=true; holdInput.Seconds=2;
            var kvtRelease=holdController.Step(holdInput);
            Check("automatic release requests KVTII but does not drive against cylinders", kvtRelease.KvtPosition==2 && kvtRelease.TractionCommand==5);
            holdInput.BrakesReleased=true; holdInput.Seconds=3;
            Check("native traction resumes only after independent release", holdController.Step(holdInput).TractionCommand==1);
            var noResponse=new TrainerAutovedenieController(Route(), .20, 6200); var stalled=Input(3000,80);
            stalled.HasBrakeFeedback=true; stalled.EqualizingReservoirPressure=5; noResponse.RequestAutomatic(stalled); noResponse.Step(stalled); stalled.Seconds=16;
            Check("missing native UR response cancels rather than continuing unverified braking", !noResponse.Step(stalled).DriveEnabled);
            var tmEntry=new TrainerAutovedenieController(Route(),.20,6200);var tmInput=Input(3000,0);tmInput.CanApplyTraction=false;tmEntry.RequestAutomatic(tmInput);
            Check("native TM interlock refuses new automatic entry",!tmEntry.Step(tmInput).DriveEnabled);
            tmInput.CanApplyTraction=true;tmEntry.RequestAutomatic(tmInput);tmEntry.Step(tmInput);tmInput.CanApplyTraction=false;tmInput.AutomaticBrakesReleased=false;tmInput.BrakesReleased=false;tmInput.Seconds=1;
            var blockedTm=tmEntry.Step(tmInput);Check("TM during own release retains brake ownership but forbids traction",blockedTm.DriveEnabled&&blockedTm.TractionCommand==5);
            tmInput.AutomaticBrakesReleased=true;tmInput.BrakesReleased=true;tmInput.Seconds=2;
            Check("unresolved TM after actual automatic release cancels explicitly",!tmEntry.Step(tmInput).DriveEnabled);
            tmInput.CanApplyTraction=true;tmInput.Seconds=3;
            Check("TM recovery alone never restarts automatic outputs",!tmEntry.Step(tmInput).DriveEnabled);
            passed = true;
        } catch (Exception exception) { error = exception.ToString(); }
        var result = new { passed = passed, checks = checks, error = error,
            scope = "Managed planning and ownership contract only. No native electrical, braking calibration or end-to-end AVP acceptance.",
            avp_controller_accepted = false };
        File.WriteAllText(TrainerSession.ResultPath("Autovedenie_controller_probe.json"),
            new JavaScriptSerializer().Serialize(result));
        TrainerSession.Complete("managed-controller-probe");
        return passed ? 0 : 1;
    }
}
