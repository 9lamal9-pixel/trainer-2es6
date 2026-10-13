using System;
using System.IO;
using System.Collections.Generic;
using System.Security.Cryptography;
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
    const double Baseline = .07624053955078124;
    static RouteModel FullNativeRoute() {
        string path=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,
            @"..\dev\config\native-markup\Tumen_Ekaterinburg.source.json"));
        byte[] bytes=File.ReadAllBytes(path);
        using(var hash=SHA256.Create())
            Check("full native markup source SHA",BitConverter.ToString(hash.ComputeHash(bytes)).Replace("-","")==
                "BF11DB8D35391B7A3CBF3DC02F96C8AB08B0B6237A7DD5AEE4049C093F809728");
        var data=(Dictionary<string,object>)new JavaScriptSerializer {MaxJsonLength=8000000}.DeserializeObject(System.Text.Encoding.UTF8.GetString(bytes));
        var rows=(object[])((Dictionary<string,object>)data["profile"])["records"];
        var route=new RouteModel {native_axis=true,direction=1,map_start_km=0,map_end_km=333.033,initial_km=2.6};
        foreach(var item in rows){var row=(Dictionary<string,object>)item;
            double start=Convert.ToDouble(row["Nach"]),length=Convert.ToDouble(row["Len"]);
            route.profile.Add(new RouteGrade {km_start=start/1000,km_end=(start+length)/1000,grade_permille=Convert.ToDouble(row["Uklon"])});
        }
        route.speed_limits.Add(new RouteSpeed {km_start=0,km_end=333.033,speed_kmh=80});
        route.Validate();return route;
    }
    static TrainerAvpInput NativeInput(double x,double speed) {
        var value=Input(x,speed);value.TrainLengthM=1126.18;value.OperationalLimitKmh=35;return value;
    }
    static RouteModel HorizonRoute(params double[] endsAndGrades) {
        var value=new RouteModel {native_axis=true,direction=1,map_start_km=0,map_end_km=333.033,initial_km=2.6};
        double start=0;
        for(int i=0;i<endsAndGrades.Length;i+=2){double end=endsAndGrades[i];value.profile.Add(new RouteGrade {km_start=start/1000,km_end=end/1000,grade_permille=endsAndGrades[i+1]});start=end;}
        value.speed_limits.Add(new RouteSpeed {km_start=0,km_end=333.033,speed_kmh=80});return value;
    }
    static void FullRouteHorizonChecks() {
        var full=FullNativeRoute();var start=NativeInput(2600,0);
        var controller=new TrainerAutovedenieController(full,Baseline,6200);controller.RequestAutomatic(start);
        var initial=controller.Step(start);
        Check("full 333km route retained by local planner",Math.Abs(full.Length-333033)<.001);
        Check("distant native minus8 does not refuse start at2600",initial.DriveEnabled&&initial.TractionCommand==1&&initial.State==TrainerAvpState.Automatic);
        Check("distant native minus8 remains an explicit unconfirmed boundary",initial.UnconfirmedGradeStartM==56725&&!initial.HoldingForUnconfirmedGrade);
        Check("start horizon is bounded ahead of complete train",initial.BrakeHorizonEndM>2600&&initial.BrakeHorizonEndM<56725&&Math.Abs(initial.EffectiveBrakeDeceleration-Baseline)<1e-12);
        var approach=NativeInput(55000,35);approach.HasBrakeFeedback=true;approach.EqualizingReservoirPressure=5;
        var approached=controller.Step(approach);
        Check("approach to unconfirmed slope removes traction and applies brake",approached.DriveEnabled&&approached.TractionCommand==5&&approached.BrakePosition==5&&approached.TargetSpeedKmh<35);
        Check("approach horizon excludes the forbidden entry rather than shortening route",approached.BrakeHorizonEndM<=56724&&approached.WorstGradePermille<0&&approached.EffectiveBrakeDeceleration>0&&full.Length==333033);
        var held=NativeInput(56695,0);held.HasBrakeFeedback=true;held.EqualizingReservoirPressure=5;
        var hold=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);hold.RequestAutomatic(held);var h=hold.Step(held);
        Check("standstill within hazard reserve initiates service step",h.DriveEnabled&&h.TargetSpeedKmh==0&&h.TractionCommand==5&&h.BrakePosition==5);
        Check("unconfirmed grade hold is automatic with full native KVT VI",h.State==TrainerAvpState.Automatic&&h.HoldingForUnconfirmedGrade&&h.KvtPosition==6);
        held.EqualizingReservoirPressure=4.3;held.Seconds=1;h=hold.Step(held);
        Check("hazard hold enters measured IV and retains full native KVT VI",h.BrakePosition==4&&h.KvtPosition==6&&h.DriveEnabled);
        held.Seconds=20;h=hold.Step(held);
        Check("hazard hold does not release after ordinary lap timeout",h.BrakePosition==4&&h.TractionCommand==5&&h.State==TrainerAvpState.Automatic);
        held.ManualOverride=true;h=hold.Step(held);
        Check("manual takeover remains above unconfirmed grade hold",!h.DriveEnabled&&h.BrakePosition==0&&h.KvtPosition==0);
        held.ManualOverride=false;Check("neutral cannot rearm a cancelled grade hold",!hold.Step(held).DriveEnabled);
        hold.RequestAutomatic(held);held.SafetyIntervention=true;h=hold.Step(held);
        Check("native safety takes grade brake ownership immediately",!h.DriveEnabled&&h.TractionCommand==0&&h.BrakePosition==0);
        var millimetre=NativeInput(56725-.001,0);millimetre.HasBrakeFeedback=true;millimetre.EqualizingReservoirPressure=5;
        var edge=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);edge.RequestAutomatic(millimetre);var e=edge.Step(millimetre);
        Check("one millimetre before hazard never requests acceleration",e.DriveEnabled&&e.TractionCommand==5&&e.HoldingForUnconfirmedGrade&&e.State!=TrainerAvpState.Finished);
        foreach(double x in new double[]{56725,56725+.001,57800}) {
            var occupied=NativeInput(x,0);var denied=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);
            denied.RequestAutomatic(occupied);var d=denied.Step(occupied);
            Check("occupied native minus8 refuses new entry at"+x,!d.DriveEnabled&&d.EffectiveBrakeDeceleration<=0&&d.BrakePlanningFault);
        }
        // This simulates an unexpected source/position transition, not a valid
        // passage. The diagnostic hold keeps established native brake ownership.
        approach.Seconds=1;approach.X=56725.001;approach.SpeedKmh=20;var fault=controller.Step(approach);
        Check("unexpected occupied hazard retains coast and service ownership",fault.DriveEnabled&&fault.BrakePlanningFault&&fault.TractionCommand==5&&fault.BrakePosition==5&&fault.State==TrainerAvpState.Automatic);
        Check("occupied negative bound is reported without positive clamp",fault.EffectiveBrakeDeceleration<0&&fault.WorstGradePermille==-8);
        approach.EqualizingReservoirPressure=4.3;approach.Seconds=2;fault=controller.Step(approach);
        Check("unexpected entry fault holds actual measured service lap",fault.DriveEnabled&&fault.BrakePosition==4);
        approach.X=60000;approach.SpeedKmh=0;approach.Seconds=3;fault=controller.Step(approach);
        Check("leaving bad gradient alone retains full stop hold",fault.DriveEnabled&&fault.BrakePlanningFault&&fault.TargetSpeedKmh==0&&fault.TractionCommand==5&&fault.KvtPosition==6);
        approach.ManualOverride=true;Check("manual takeover remains above occupied planning fault",!controller.Step(approach).DriveEnabled);
        var end=NativeInput(333023,0);var terminal=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);terminal.RequestAutomatic(end);var last=terminal.Step(end);
        Check("actual complete 333km terminal finishes with full stop hold",last.State==TrainerAvpState.Finished&&last.DriveEnabled&&last.BrakePosition==5&&last.KvtPosition==6);
        var body=HorizonRoute(1800,-4,333033,0);var bodyPlan=new TrainerAutovedenieController(body,Baseline,6200).Step(NativeInput(2600,0));
        Check("head flat does not ignore downhill still under train",bodyPlan.WorstGradePermille==-4&&Math.Abs(bodyPlan.EffectiveBrakeDeceleration-(Baseline-4*9.81/1000))<1e-12);
        var aiding=HorizonRoute(333033,6);var aided=new TrainerAutovedenieController(aiding,Baseline,6200).Step(NativeInput(2600,0));
        Check("positive grade does not add unverified braking credit",Math.Abs(aided.EffectiveBrakeDeceleration-Baseline)<1e-12);
        var prospect=HorizonRoute(2800,0,3300,-2,3500,0,3600,-6,333033,0);
        var prospective=new TrainerAutovedenieController(prospect,Baseline,6200).Step(NativeInput(2600,0));
        Check("standstill uses prospective allowed velocity",prospective.BrakeHorizonEndM>3500&&prospective.WorstGradePermille==-6);
        Check("fixed point expands through successive adverse slopes",Math.Abs(prospective.EffectiveBrakeDeceleration-(Baseline-6*9.81/1000))<1e-12);
        var gap=HorizonRoute(2800,0,333033,0);var gapController=new TrainerAutovedenieController(gap,Baseline,6200);
        gap.profile[1].km_start=2.81;gapController.RequestAutomatic(NativeInput(2600,0));var invalid=gapController.Step(NativeInput(2600,0));
        Check("runtime gap refuses planning instead of assuming flat",!invalid.DriveEnabled&&invalid.BrakePlanningFault);
        var overlap=HorizonRoute(2800,0,333033,0);var overlapController=new TrainerAutovedenieController(overlap,Baseline,6200);
        overlap.profile[1].km_start=2.79;overlapController.RequestAutomatic(NativeInput(2600,0));
        Check("runtime profile overlap refuses entry",!overlapController.Step(NativeInput(2600,0)).DriveEnabled);
        var missing=HorizonRoute(2800,0,333033,0);var missingController=new TrainerAutovedenieController(missing,Baseline,6200);
        missing.profile[1].km_end=333;missingController.RequestAutomatic(NativeInput(2600,0));
        Check("full-route profile coverage cannot silently be truncated",!missingController.Step(NativeInput(2600,0)).DriveEnabled);
        var nonfinite=HorizonRoute(333033,0);var nonfiniteController=new TrainerAutovedenieController(nonfinite,Baseline,6200);
        nonfinite.profile[0].grade_permille=double.NaN;nonfiniteController.RequestAutomatic(NativeInput(2600,0));
        Check("nonfinite profile refuses entry",!nonfiniteController.Step(NativeInput(2600,0)).DriveEnabled);
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
            Check("route end completes and holds full independent brake", finish.State == TrainerAvpState.Finished && finish.DriveEnabled && finish.TractionCommand == 5 && finish.BrakePosition == 5 && finish.KvtPosition == 6 && finish.TargetSpeedKmh == 0);
            input.X = 9950; input.Seconds = 1;
            var rolledBack = controller.Step(input);
            Check("completed stop retains full hold after coordinate correction", rolledBack.State == TrainerAvpState.Finished && rolledBack.TractionCommand == 5 && rolledBack.BrakePosition == 5 && rolledBack.KvtPosition == 6);
            input.ManualOverride = true;
            var manualFinish = controller.Step(input);
            Check("driver can take over completed stop without owned KVT command", !manualFinish.DriveEnabled && manualFinish.KvtPosition == 0);
            var terminalReleaseController=new TrainerAutovedenieController(Route(),.20,6200);
            var terminalReleaseInput=Input(9990,0);terminalReleaseInput.HasBrakeFeedback=true;
            terminalReleaseInput.EqualizingReservoirPressure=5;terminalReleaseInput.AutomaticBrakesReleased=false;terminalReleaseInput.BrakesReleased=false;
            terminalReleaseController.RequestAutomatic(terminalReleaseInput);
            Check("terminal initially owns full VI with measured automatic brakes applied",terminalReleaseController.Step(terminalReleaseInput).KvtPosition==6);
            terminalReleaseInput.EqualizingReservoirPressure=4.3;terminalReleaseInput.AutomaticBrakesReleased=true;
            terminalReleaseInput.BrakesReleased=true;terminalReleaseInput.CanApplyTraction=false;terminalReleaseInput.Seconds=20;
            var terminalReleased=terminalReleaseController.Step(terminalReleaseInput);
            Check("terminal full VI survives complete automatic release and traction interlock",terminalReleased.State==TrainerAvpState.Finished&&terminalReleased.DriveEnabled&&terminalReleased.KvtPosition==6&&terminalReleased.TractionCommand==5);
            var terminalTimeoutController=new TrainerAutovedenieController(Route(),.20,6200);
            var terminalTimeoutInput=Input(9990,0);terminalTimeoutInput.HasBrakeFeedback=true;terminalTimeoutInput.EqualizingReservoirPressure=5;
            terminalTimeoutController.RequestAutomatic(terminalTimeoutInput);terminalTimeoutController.Step(terminalTimeoutInput);
            terminalTimeoutInput.Seconds=16;var terminalTimeout=terminalTimeoutController.Step(terminalTimeoutInput);
            Check("stopped UR timeout reports fault while retaining full VI",terminalTimeout.State==TrainerAvpState.Finished&&terminalTimeout.DriveEnabled&&terminalTimeout.KvtPosition==6&&terminalTimeout.TractionCommand==5&&terminalTimeout.Reason.Contains("полным КВТ"));
            terminalTimeoutInput.SafetyIntervention=true;terminalTimeoutInput.Seconds=17;
            Check("native safety remains above terminal timeout hold",!terminalTimeoutController.Step(terminalTimeoutInput).DriveEnabled&&terminalTimeoutController.Step(terminalTimeoutInput).KvtPosition==0);
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
            var stoppedHold = holdController.Step(holdInput);
            Check("stopped locomotive requests full native KVT VI", stoppedHold.KvtPosition==6 && stoppedHold.TractionCommand==5 && stoppedHold.TargetSpeedKmh==0);
            holdInput.SpeedKmh=1; holdInput.Seconds=1;
            Check("full independent hold is latched through longitudinal transient", holdController.Step(holdInput).KvtPosition==6);
            holdInput.AutomaticBrakesReleased=true; holdInput.CanApplyTraction=false; holdInput.Seconds=2;
            var kvtRelease=holdController.Step(holdInput);
            Check("automatic release and TM interlock do not remove full stop hold", kvtRelease.DriveEnabled && kvtRelease.KvtPosition==6 && kvtRelease.TractionCommand==5);
            holdInput.BrakesReleased=true; holdInput.SpeedKmh=0; holdInput.Seconds=3;
            var cylindersClear=holdController.Step(holdInput);
            Check("measured cylinder release alone cannot restart or release VI", cylindersClear.KvtPosition==6 && cylindersClear.TractionCommand==5 && cylindersClear.TargetSpeedKmh==0);
            holdController.RequestAutomatic(holdInput);
            var refusedRestart=holdController.Step(holdInput);
            Check("refused explicit start preserves an already owned full hold", refusedRestart.DriveEnabled && refusedRestart.KvtPosition==6 && refusedRestart.TractionCommand==5);
            holdInput.CanApplyTraction=true; holdInput.BrakesReleased=false; holdInput.Seconds=4;
            holdController.RequestAutomatic(holdInput);
            var explicitRelease=holdController.Step(holdInput);
            Check("accepted explicit start requests II and waits for measured release", explicitRelease.DriveEnabled && explicitRelease.KvtPosition==2 && explicitRelease.TractionCommand==5);
            holdInput.BrakesReleased=true; holdInput.Seconds=5;
            Check("traction resumes after explicit start and measured independent release", holdController.Step(holdInput).TractionCommand==1);
            holdInput.AutomaticBrakesReleased=false; holdInput.BrakesReleased=false; holdInput.Seconds=6;
            Check("a new stopped automatic release acquires a new full hold", holdController.Step(holdInput).KvtPosition==6);
            holdInput.ManualOverride=true; holdInput.Seconds=7;
            var manualHold=holdController.Step(holdInput);
            Check("manual priority cancels full hold without replacing physical commands", !manualHold.DriveEnabled && manualHold.KvtPosition==0 && manualHold.TractionCommand==0);
            holdInput.ManualOverride=false; holdInput.AutomaticBrakesReleased=true; holdInput.BrakesReleased=true; holdInput.Seconds=8;
            Check("neutral after manual takeover cannot restart", !holdController.Step(holdInput).DriveEnabled);
            holdController.RequestAutomatic(holdInput); holdInput.AutomaticBrakesReleased=false; holdInput.BrakesReleased=false; holdInput.Seconds=9;
            Check("explicit rearm can own a fresh stop hold", holdController.Step(holdInput).KvtPosition==6);
            holdInput.SafetyIntervention=true; holdInput.Seconds=10;
            var safetyHold=holdController.Step(holdInput);
            Check("native safety remains above full independent hold", !safetyHold.DriveEnabled && safetyHold.KvtPosition==0 && safetyHold.BrakePosition==0);
            holdInput.SafetyIntervention=false; holdInput.AutomaticBrakesReleased=true; holdInput.BrakesReleased=true; holdInput.Seconds=11;
            Check("safety clear cannot restart", !holdController.Step(holdInput).DriveEnabled);
            holdController.RequestAutomatic(holdInput); holdInput.AutomaticBrakesReleased=false; holdInput.BrakesReleased=false; holdInput.Seconds=12;
            Check("hold can be acquired before explicit advisor cancellation", holdController.Step(holdInput).KvtPosition==6);
            holdController.RequestAdvisor(); holdInput.AutomaticBrakesReleased=true; holdInput.BrakesReleased=true; holdInput.Seconds=13;
            var advisorHold=holdController.Step(holdInput);
            Check("explicit advisor cancellation releases ownership without silent restart", advisorHold.State==TrainerAvpState.Advisor && !advisorHold.DriveEnabled && advisorHold.KvtPosition==0);
            var thresholdController=new TrainerAutovedenieController(Route(),.20,6200);
            var thresholdInput=Input(3000,.3);thresholdInput.HasBrakeFeedback=true;
            thresholdInput.EqualizingReservoirPressure=5;thresholdInput.AutomaticBrakesReleased=false;thresholdInput.BrakesReleased=false;
            thresholdController.RequestAutomatic(thresholdInput);
            Check("moving release does not acquire the stopped full-hold latch",thresholdController.Step(thresholdInput).KvtPosition==2);
            thresholdInput.SpeedKmh=.299;thresholdInput.Seconds=1;
            Check("below stopped threshold acquires full VI",thresholdController.Step(thresholdInput).KvtPosition==6);
            thresholdController.Cancel("Explicit operator cancellation");thresholdInput.AutomaticBrakesReleased=true;thresholdInput.BrakesReleased=true;thresholdInput.Seconds=2;
            var cancelledHold=thresholdController.Step(thresholdInput);
            Check("explicit cancellation relinquishes full hold without restart",cancelledHold.State==TrainerAvpState.Cancelled&&!cancelledHold.DriveEnabled&&cancelledHold.KvtPosition==0&&cancelledHold.TractionCommand==0);
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
            FullRouteHorizonChecks();
            passed = true;
        } catch (Exception exception) { error = exception.ToString(); }
        var result = new { passed = passed, checks = checks, error = error,
            scope = "Managed planning and ownership contract with the complete pinned333km native profile. Stopped full KVT VI remains latched through automatic release and requires explicit start/cancellation; manual/native safety remain above it. Future unconfirmed grades stop/hold before entry. No native pressure, moving pre-release timing, adverse-grade,80kmh, electrical or end-to-end AVP acceptance.",
            avp_controller_accepted = false };
        File.WriteAllText(TrainerSession.ResultPath("Autovedenie_controller_probe.json"),
            new JavaScriptSerializer().Serialize(result));
        TrainerSession.Complete("managed-controller-probe");
        return passed ? 0 : 1;
    }
}
