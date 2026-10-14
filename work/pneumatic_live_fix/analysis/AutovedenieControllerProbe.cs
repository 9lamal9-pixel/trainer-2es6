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
            Seconds = 0, Ready = true, SettingsValid = true, OperationalLimitKmh = 80,
            HasBrakeFeedback=true,EqualizingReservoirPressure=5,ChargingPressureBar=5,BrakePipePressureBar=5,
            MaximumConsistSpeedKmh=Math.Abs(speed),IndependentKvtPressureBar=0,MinBogiePressureBar=0,MaxBogiePressureBar=0,
            AmbientTemperatureKnown=true,AmbientTemperatureC=20,BrakeCommandsKnown=true };
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
        var initial=controller.ProbeStep(start);
        Check("full 333km route retained by local planner",Math.Abs(full.Length-333033)<.001);
        Check("distant native minus8 does not refuse start at2600",initial.DriveEnabled&&initial.TractionCommand==1&&initial.State==TrainerAvpState.Automatic);
        Check("distant native minus8 remains an explicit unconfirmed boundary",initial.UnconfirmedGradeStartM==56725&&!initial.HoldingForUnconfirmedGrade);
        Check("start horizon is bounded ahead of complete train",initial.BrakeHorizonEndM>2600&&initial.BrakeHorizonEndM<56725&&Math.Abs(initial.EffectiveBrakeDeceleration-Baseline)<1e-12);
        var approach=NativeInput(55000,35);approach.HasBrakeFeedback=true;approach.EqualizingReservoirPressure=5;
        var approached=controller.ProbeStep(approach);
        Check("approach to unconfirmed slope removes traction and applies brake",approached.DriveEnabled&&approached.TractionCommand==5&&approached.BrakePosition==5&&approached.TargetSpeedKmh<35);
        Check("approach horizon excludes the forbidden entry rather than shortening route",approached.BrakeHorizonEndM<=56724&&approached.WorstGradePermille<0&&approached.EffectiveBrakeDeceleration>0&&full.Length==333033);
        var held=NativeInput(56695,0);held.HasBrakeFeedback=true;held.EqualizingReservoirPressure=5;
        var hold=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);hold.RequestAutomatic(held);var h=hold.ProbeStep(held);
        Check("standstill within hazard reserve initiates service step",h.DriveEnabled&&h.TargetSpeedKmh==0&&h.TractionCommand==5&&h.BrakePosition==5);
        Check("unconfirmed grade hold is automatic with full native KVT VI",h.State==TrainerAvpState.Automatic&&h.HoldingForUnconfirmedGrade&&h.KvtPosition==6);
        held.EqualizingReservoirPressure=4.3;held.Seconds=1;h=hold.ProbeStep(held);
        Check("hazard hold enters measured IV and retains full native KVT VI",h.BrakePosition==4&&h.KvtPosition==6&&h.DriveEnabled);
        held.Seconds=20;h=hold.ProbeStep(held);
        Check("hazard hold does not release after ordinary lap timeout",h.BrakePosition==4&&h.TractionCommand==5&&h.State==TrainerAvpState.Automatic);
        held.ManualOverride=true;h=hold.ProbeStep(held);
        Check("manual takeover remains above unconfirmed grade hold",!h.DriveEnabled&&h.BrakePosition==0&&h.KvtPosition==0);
        held.ManualOverride=false;Check("neutral cannot rearm a cancelled grade hold",!hold.ProbeStep(held).DriveEnabled);
        hold.RequestAutomatic(held);held.SafetyIntervention=true;h=hold.ProbeStep(held);
        Check("native safety takes grade brake ownership immediately",!h.DriveEnabled&&h.TractionCommand==0&&h.BrakePosition==0);
        var millimetre=NativeInput(56725-.001,0);millimetre.HasBrakeFeedback=true;millimetre.EqualizingReservoirPressure=5;
        var edge=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);edge.RequestAutomatic(millimetre);var e=edge.ProbeStep(millimetre);
        Check("one millimetre before hazard never requests acceleration",e.DriveEnabled&&e.TractionCommand==5&&e.HoldingForUnconfirmedGrade&&e.State!=TrainerAvpState.Finished);
        foreach(double x in new double[]{56725,56725+.001,57800}) {
            var occupied=NativeInput(x,0);var denied=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);
            denied.RequestAutomatic(occupied);var d=denied.ProbeStep(occupied);
            Check("occupied native minus8 refuses new entry at"+x,!d.DriveEnabled&&d.EffectiveBrakeDeceleration<=0&&d.BrakePlanningFault);
        }
        // This simulates an unexpected source/position transition, not a valid
        // passage. The diagnostic hold keeps established native brake ownership.
        approach.Seconds=1;approach.X=56725.001;approach.SpeedKmh=20;var fault=controller.ProbeStep(approach);
        Check("unexpected occupied hazard retains coast and service ownership",fault.DriveEnabled&&fault.BrakePlanningFault&&fault.TractionCommand==5&&fault.BrakePosition==5&&fault.State==TrainerAvpState.Automatic);
        Check("occupied negative bound is reported without positive clamp",fault.EffectiveBrakeDeceleration<0&&fault.WorstGradePermille==-8);
        approach.EqualizingReservoirPressure=4.3;approach.Seconds=2;fault=controller.ProbeStep(approach);
        Check("unexpected entry fault holds actual measured service lap",fault.DriveEnabled&&fault.BrakePosition==4);
        approach.X=60000;approach.SpeedKmh=0;approach.Seconds=3;fault=controller.ProbeStep(approach);
        Check("leaving bad gradient alone retains full stop hold",fault.DriveEnabled&&fault.BrakePlanningFault&&fault.TargetSpeedKmh==0&&fault.TractionCommand==5&&fault.KvtPosition==6);
        approach.ManualOverride=true;Check("manual takeover remains above occupied planning fault",!controller.ProbeStep(approach).DriveEnabled);
        var end=NativeInput(333023,0);var terminal=new TrainerAutovedenieController(FullNativeRoute(),Baseline,6200);terminal.RequestAutomatic(end);var last=terminal.ProbeStep(end);
        Check("actual complete 333km terminal finishes with full stop hold",last.State==TrainerAvpState.Finished&&last.DriveEnabled&&last.BrakePosition==5&&last.KvtPosition==6);
        var body=HorizonRoute(1800,-4,333033,0);var bodyPlan=new TrainerAutovedenieController(body,Baseline,6200).ProbeStep(NativeInput(2600,0));
        Check("head flat does not ignore downhill still under train",bodyPlan.WorstGradePermille==-4&&Math.Abs(bodyPlan.EffectiveBrakeDeceleration-(Baseline-4*9.81/1000))<1e-12);
        var aiding=HorizonRoute(333033,6);var aided=new TrainerAutovedenieController(aiding,Baseline,6200).ProbeStep(NativeInput(2600,0));
        Check("positive grade does not add unverified braking credit",Math.Abs(aided.EffectiveBrakeDeceleration-Baseline)<1e-12);
        var prospect=HorizonRoute(2800,0,3300,-2,3500,0,3600,-6,333033,0);
        var prospective=new TrainerAutovedenieController(prospect,Baseline,6200).ProbeStep(NativeInput(2600,0));
        Check("standstill uses prospective allowed velocity",prospective.BrakeHorizonEndM>3500&&prospective.WorstGradePermille==-6);
        Check("fixed point expands through successive adverse slopes",Math.Abs(prospective.EffectiveBrakeDeceleration-(Baseline-6*9.81/1000))<1e-12);
        var gap=HorizonRoute(2800,0,333033,0);var gapController=new TrainerAutovedenieController(gap,Baseline,6200);
        gap.profile[1].km_start=2.81;gapController.RequestAutomatic(NativeInput(2600,0));var invalid=gapController.ProbeStep(NativeInput(2600,0));
        Check("runtime gap refuses planning instead of assuming flat",!invalid.DriveEnabled&&invalid.BrakePlanningFault);
        var overlap=HorizonRoute(2800,0,333033,0);var overlapController=new TrainerAutovedenieController(overlap,Baseline,6200);
        overlap.profile[1].km_start=2.79;overlapController.RequestAutomatic(NativeInput(2600,0));
        Check("runtime profile overlap refuses entry",!overlapController.ProbeStep(NativeInput(2600,0)).DriveEnabled);
        var missing=HorizonRoute(2800,0,333033,0);var missingController=new TrainerAutovedenieController(missing,Baseline,6200);
        missing.profile[1].km_end=333;missingController.RequestAutomatic(NativeInput(2600,0));
        Check("full-route profile coverage cannot silently be truncated",!missingController.ProbeStep(NativeInput(2600,0)).DriveEnabled);
        var nonfinite=HorizonRoute(333033,0);var nonfiniteController=new TrainerAutovedenieController(nonfinite,Baseline,6200);
        nonfinite.profile[0].grade_permille=double.NaN;nonfiniteController.RequestAutomatic(NativeInput(2600,0));
        Check("nonfinite profile refuses entry",!nonfiniteController.ProbeStep(NativeInput(2600,0)).DriveEnabled);
    }
    static void ControllerBrakeIntegration() {
        var c=new TrainerAutovedenieController(Route(),.20,6200);var i=Input(2000,0);
        Check("advisor has no command ownership",!c.ProbeStep(i).DriveEnabled);
        c.RequestAutomatic(i);var d=c.ProbeStep(i);Check("explicit initial start with released real sensors",d.DriveEnabled&&d.TractionCommand==1&&d.BrakePosition==2);
        i.Seconds=.1;Check("native plus pulse rate remains bounded",c.ProbeStep(i).TractionCommand==0);
        i.Seconds=1;Check("next genuine command interval",c.ProbeStep(i).TractionCommand==1);
        i.X=3000;i.SpeedKmh=80;i.Seconds=2;d=c.ProbeStep(i);
        Check("80 to30 route preview retained",d.PreviewLimitKmh==30&&d.PreviewDistanceM==1000&&d.TargetSpeedKmh<80);
        Check("braking begins with coast and measured first step",d.TractionCommand==5&&d.BrakePosition==5);
        i.EqualizingReservoirPressure=4.3;i.AutomaticBrakesReleased=false;i.BrakesReleased=false;i.Seconds=5;d=c.ProbeStep(i);
        Check("actual target enters native IV",d.BrakePosition==4);
        i.X=7500;i.SpeedKmh=20;i.Seconds=5.1;d=c.ProbeStep(i);
        Check("whole train current restriction retained",d.CurrentLimitKmh==30);
        i.Seconds=11.09;d=c.ProbeStep(i);Check("effective IV holds full six seconds",d.BrakePosition==4&&d.KvtPosition==2);
        i.Seconds=11.1;d=c.ProbeStep(i);Check("release waits for actual independent pressure",d.BrakePosition==4&&d.KvtPosition==3);
        i.IndependentKvtPressureBar=1.15;i.MinBogiePressureBar=1.4;i.MaxBogiePressureBar=1.4;i.Seconds=12;d=c.ProbeStep(i);
        Check("confirmed KVT permits I without artificial pre35 delay",d.BrakePosition==1&&d.KvtPosition==3&&d.TractionCommand==5);
        i.Seconds=12.1;d=c.ProbeStep(i);Check("I remains until measured overcharge target",d.BrakePosition==1);
        i.EqualizingReservoirPressure=5.3;i.Seconds=13;d=c.ProbeStep(i);Check("measured charge plus0.3 returns II",d.BrakePosition==2&&d.KvtPosition==3);
        i.AutomaticBrakesReleased=true;i.MinBogiePressureBar=1.15;i.MaxBogiePressureBar=1.15;i.Seconds=46.99;d=c.ProbeStep(i);
        Check("KVT cannot release before35 seconds",d.KvtPosition==3);
        i.Seconds=47;d=c.ProbeStep(i);Check("actual automatic release plus35 permits KVT II",d.KvtPosition==2&&d.TractionCommand==5);
        i.BrakesReleased=true;i.IndependentKvtPressureBar=0;i.MinBogiePressureBar=0;i.MaxBogiePressureBar=0;i.X=8000;i.Seconds=72.09;d=c.ProbeStep(i);
        Check("all TC released does not bypass60 second traction delay",d.TractionCommand==5&&d.CurrentLimitKmh==80);
        c.Cancel("operator handoff");Check("cancel issues no controls",!c.ProbeStep(i).DriveEnabled);
        c.RequestAutomatic(i);d=c.ProbeStep(i);Check("explicit rearm preserves release history before60 seconds",d.DriveEnabled&&d.TractionCommand==5);
        i.Seconds=72.11;d=c.ProbeStep(i);Check("after60 seconds actual release allows native plus",d.DriveEnabled&&d.TractionCommand==1);
        i.ManualOverride=true;d=c.ProbeStep(i);Check("manual action remains highest owner",!d.DriveEnabled&&d.BrakePosition==0&&d.KvtPosition==0);
        i.ManualOverride=false;i.Seconds=73;Check("neutral does not rearm",!c.ProbeStep(i).DriveEnabled);
        c.RequestAutomatic(i);i.SafetyIntervention=true;d=c.ProbeStep(i);Check("safety cannot be masked by regulatory cycle",!d.DriveEnabled&&d.TractionCommand==0&&d.BrakePosition==0);
    }
    static void EntryAndBodyChecks() {
        foreach(int failure in new[]{0,1,2,3,4,5}) {
            var c=new TrainerAutovedenieController(Route(),.20,6200);var i=Input(2000,0);
            if(failure==0)i.Ready=false; if(failure==1)i.SettingsValid=false;if(failure==2)i.HasBrakeFeedback=false;
            if(failure==3)i.MaximumConsistSpeedKmh=Double.NaN;if(failure==4)i.Operation=TrainerAvpOperation.CatchUp;if(failure==5)i.Operation=TrainerAvpOperation.SignalFollowing;
            c.RequestAutomatic(i);Check("invalid initial contract refuses case"+failure,!c.Step(i).DriveEnabled);
        }
        var ready=Input(2000,0);var missing=new TrainerAutovedenieController(Route(),.20,6200);missing.RequestAutomatic(ready);missing.Step(ready);
        ready.Seconds=1;ready.MaximumConsistSpeedKmh=Double.NaN;var hold=missing.Step(ready);
        Check("loss of whole-train sensor retains owned coast and fault IV",hold.DriveEnabled&&hold.TractionCommand==5&&hold.BrakePosition==4);
        var body=Input(9990,0);body.MaximumConsistSpeedKmh=.4;var end=new TrainerAutovedenieController(Route(),.20,6200);end.RequestAutomatic(body);var b=end.Step(body);
        Check("stationary head cannot declare whole-train terminal stop",b.State!=TrainerAvpState.Finished&&b.KvtPosition!=6);
        body.Seconds=.1;body.MaximumConsistSpeedKmh=0;b=end.Step(body);
        Check("all76 stationary confirms terminal full VI",b.State==TrainerAvpState.Finished&&b.KvtPosition==6&&b.TractionCommand==5);
        body.Seconds=4;body.EqualizingReservoirPressure=4.3;body.BrakeCommandsKnown=true;body.ObservedKm=5;b=end.Step(body);
        Check("terminal enters actual IV full VI after step",b.BrakePosition==4&&b.KvtPosition==6);
        body.Seconds=5;body.ObservedKm=4;body.AutomaticBrakesReleased=true;body.BrakesReleased=true;b=end.Step(body);
        Check("external automatic release cannot clear terminal brake owner",b.KvtPosition==6&&b.TractionCommand==5&&b.State==TrainerAvpState.Finished);
        body.Seconds=20;body.EqualizingReservoirPressure=5;b=end.Step(body);
        Check("unready terminal UR does not silently return traction",b.KvtPosition==6&&b.TractionCommand==5);
        body.ManualOverride=true;b=end.Step(body);Check("explicit manual terminal takeover owns no AVP brake",!b.DriveEnabled&&b.KvtPosition==0);
        var sig=Input(2000,30);sig.Operation=TrainerAvpOperation.SignalFollowing;sig.HasSignalPreview=true;sig.SignalStopX=2100;sig.SignalPermittedSpeedKmh=0;
        var follow=new TrainerAutovedenieController(Route(),.20,6200);follow.RequestAutomatic(sig);Check("confirmed signal envelope still brakes",follow.ProbeStep(sig).BrakePosition==5);
    }
    static void PneumaticDelayEnvelopeChecks() {
        var body=Input(2000,30);body.MaximumConsistSpeedKmh=81;
        var owner=new TrainerAutovedenieController(Route(),.2,6200);owner.RequestAutomatic(body);
        var brake=owner.Step(body);
        Check("Whole-consist overspeed requests braking despite slower head",brake.DriveEnabled&&brake.BrakePosition==5&&brake.TractionCommand==5);
        const double forceBound=.11914565807615227;
        foreach(double grade in new[]{0.0,-8.0}) {
            var route=HorizonRoute(333033,grade);route.speed_limits.Clear();
            route.speed_limits.Add(new RouteSpeed{km_start=0,km_end=6,speed_kmh=80});
            route.speed_limits.Add(new RouteSpeed{km_start=6,km_end=333.033,speed_kmh=30});
            var i=NativeInput(2600,80);i.OperationalLimitKmh=80;
            var c=new TrainerAutovedenieController(route,forceBound,6200);var d=c.Step(i);
            double coastA=Math.Max(0,-grade*9.81/1000),v=d.TargetSpeedKmh/3.6,u=30/3.6,dt=35;
            double traveled=v*dt+.5*coastA*dt*dt+Math.Max(0,Math.Pow(v+coastA*dt,2)-u*u)/(2*d.EffectiveBrakeDeceleration)+30;
            Check("Target reaches future30 after real35s coast on grade"+grade,traveled<=3400+.000001);
            Check("Future horizon includes complete delayed stopping trajectory"+grade,
                d.BrakeHorizonEndM>=i.X+35*(80/3.6)+.5*coastA*35*35+Math.Pow(80/3.6+coastA*35,2)/(2*d.EffectiveBrakeDeceleration)+30-.000001);
            if(grade<0)Check("Downhill delay lowers80 before future restriction",d.TargetSpeedKmh<80);
        }
    }
    public static int Main() {
        TrainerSession.EnsureStarted();bool passed=false;string error=null;
        try {
            bool rejected=false;try{TrainerAutovedenieInterface.Attach(IntPtr.Zero,null);}catch(ArgumentException){rejected=true;}
            Check("absent native pointer rejected before hook",rejected);
            FullRouteHorizonChecks();ControllerBrakeIntegration();EntryAndBodyChecks();PneumaticDelayEnvelopeChecks();passed=true;
        }catch(Exception e){error=e.ToString();}
        File.WriteAllText(TrainerSession.ResultPath("Autovedenie_controller_probe.json"),new JavaScriptSerializer{MaxJsonLength=8000000}.Serialize(new{passed,error,checks,
            scope="Managed full-route planner and regulatory observed-command integration; supplied native-like sensor/time observations, not native physics or brake calibration."}));
        Console.WriteLine((passed?"PASS":"FAIL")+" managed AVP regulatory integration "+error);return passed?0:1;
    }
}
public static class TrainerControllerProbeObservations {
    static readonly Dictionary<TrainerAutovedenieController,TrainerAvpDecision> previous=new Dictionary<TrainerAutovedenieController,TrainerAvpDecision>();
    // This fixture declares that each preceding output command was executed.
    // Pressure and time are supplied explicitly by the tests; they are never
    // generated by the production controller. Native acceptance is separate.
    public static TrainerAvpDecision ProbeStep(this TrainerAutovedenieController c,TrainerAvpInput i) {
        i.MaximumConsistSpeedKmh=Math.Abs(i.SpeedKmh);
        TrainerAvpDecision p;if(previous.TryGetValue(c,out p)&&p.DriveEnabled){i.ObservedKm=p.BrakePosition;i.ObservedKvt=p.KvtPosition;}
        i.BrakeCommandsKnown=true;var d=c.Step(i);previous[c]=d;return d;
    }
}
