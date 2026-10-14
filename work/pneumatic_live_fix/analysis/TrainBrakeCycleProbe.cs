using System;
using System.Collections.Generic;
using System.IO;
using System.Web.Script.Serialization;

// Pure state/clock acceptance. No host, DLL, native pointer or WinForms startup.
class TrainBrakeCycleProbe {
    static readonly List<object> checks=new List<object>();
    static readonly List<object> cases=new List<object>();
    static int failures;
    static void Check(string name,bool pass,object expected,object actual) {
        checks.Add(new {name,passed=pass,expected,actual});
        if(!pass)throw new Exception(name+" expected="+expected+" actual="+actual);
    }
    static void Near(string name,double actual,double expected) { Check(name,Math.Abs(actual-expected)<.00000001,expected,actual); }
    static TrainerTrainBrakeCycleInput Sample(double seconds,double ur=5,double speed=20) {
        return new TrainerTrainBrakeCycleInput {Seconds=seconds,UrBar=ur,ChargeBar=5,ActualTmBar=5,
            HeadSpeedKmh=speed,MaximumConsistSpeedKmh=Math.Abs(speed),SensorsKnown=true,Ready=true,
            AllAutoReleased=true,AllBrakesReleased=true,OwnsCommands=true,Axles=300,
            ApplicationKind=TrainerTrainBrakeKind.Service,HasAmbientTemperature=true,AmbientTemperatureC=20};
    }
    static TrainerTrainBrakeCycleDecision Held(TrainerTrainBrakeCycle c,double seconds,double ur,bool demand=true) {
        var i=Sample(seconds,ur);i.BrakeDemand=demand;i.AllAutoReleased=false;i.AllBrakesReleased=false;i.ObservedCommandsKnown=true;i.ObservedKm=4;
        return c.Step(i);
    }
    static void BeginService(TrainerTrainBrakeCycle c) {
        var i=Sample(0);i.BrakeDemand=true;var a=c.Step(i);
        Check("First request uses real UR application",a.Km==5&&!a.CanApplyTraction,5,a.Km);Near("First measured target",a.TargetUrBar,4.3);
        var h=Held(c,1,4.3);Check("Reached step goes to IV",h.Km==4,4,h.Km);
    }
    static TrainerTrainBrakeCycleDecision ReleaseFrame(TrainerTrainBrakeCycle c,double seconds,double ur,int observedKm,bool auto,bool full,bool band=true) {
        var i=Sample(seconds,ur);i.AllAutoReleased=auto;i.AllBrakesReleased=full;i.ObservedCommandsKnown=true;i.ObservedKm=observedKm;
        if(band){i.IndependentKvtPressureBar=1.5;i.MinBogiePressureBar=1.2;i.MaxBogiePressureBar=1.8;}
        return c.Step(i);
    }
    static void MovingRelease() {
        var c=new TrainerTrainBrakeCycle();BeginService(c);
        Check("5.999s lap cannot release",Held(c,6.999,4.3,false).Km==4,4,"before6s");
        var pre=Held(c,7,4.3,false);Check("At6s pressure confirmation precedes I",pre.Km==4&&pre.Kvt==3, "IV/III",new{pre.Km,pre.Kvt});
        var start=ReleaseFrame(c,7.1,4.3,4,false,false);Check("Actual KVT permits I without pre35s delay",start.Km==1,1,start.Km);
        Near("Requested I does not fabricate release anchor",start.ReleaseStartedSeconds,-1);
        var observed=ReleaseFrame(c,7.2,4.3,1,false,false);Near("Effective I starts interval",observed.ReleaseStartedSeconds,7.2);
        var boost=ReleaseFrame(c,8,5.299,1,false,false);Check("Below charge+.3 keeps I",boost.Km==1,1,boost.Km);
        var lap=ReleaseFrame(c,8.1,5.3,2,false,false);Check("At charge+.3 uses II",lap.Km==2,2,lap.Km);
        var beforeHold=ReleaseFrame(c,42.099,5,2,true,false);Check("KVT retains34.999s",beforeHold.Kvt==3,3,beforeHold.Kvt);
        var hold=ReleaseFrame(c,42.1,5,2,true,false);Check("35s and all-auto release permit KVT II",hold.Kvt==2,2,hold.Kvt);
        var stillPressure=ReleaseFrame(c,67.2,5,2,true,false);Check("Time alone cannot grant traction",!stillPressure.CanApplyTraction,false,stillPressure.CanApplyTraction);
        var before=ReleaseFrame(c,67.199,5,2,true,true,false);
        // A backwards sample is deliberately a fault, never an earlier grant.
        Check("Backwards clock is a hold",before.State==TrainerTrainBrakeCycleState.SensorHold&&!before.CanApplyTraction,"SensorHold",before.State);
        var clean=new TrainerTrainBrakeCycle();BeginService(clean);Held(clean,7,4.3,false);
        ReleaseFrame(clean,7.1,4.3,4,false,false);ReleaseFrame(clean,7.2,4.3,1,false,false);
        ReleaseFrame(clean,8.1,5.3,2,false,false);ReleaseFrame(clean,42.1,5,2,true,false);
        Check("59.999s no traction",!ReleaseFrame(clean,67.199,5,2,true,true,false).CanApplyTraction,false,"before60s");
        var allowed=ReleaseFrame(clean,67.2,5,2,true,true,false);
        Check("60s plus every release allows traction",allowed.CanApplyTraction&&!allowed.RequestCoast,true,allowed.CanApplyTraction);
        var repeated=ReleaseFrame(clean,67.2,5,2,true,true,false);Near("Same tick keeps anchor",repeated.ReleaseStartedSeconds,7.2);
        Check("Repeated tick does not add time",repeated.CanApplyTraction,true,repeated.CanApplyTraction);
    }
    static void BandAndLateRelease() {
        var rearmed=new TrainerTrainBrakeCycle();BeginService(rearmed);Held(rearmed,7,4.3,false);
        ReleaseFrame(rearmed,7.1,4.3,4,false,false);ReleaseFrame(rearmed,7.2,4.3,1,false,false);
        var r=Sample(8,5.3);r.ExplicitRestart=true;r.ObservedCommandsKnown=true;r.ObservedKm=2;
        r.AllAutoReleased=false;r.AllBrakesReleased=false;r.IndependentKvtPressureBar=1.5;r.MinBogiePressureBar=1.2;r.MaxBogiePressureBar=1.8;
        var d=rearmed.Step(r);Check("Moving rearm preserves KVT35s and automatic-release gate",d.Kvt==3&&!d.CanApplyTraction,3,d.Kvt);
        Check("Rearmed moving release still holds before35s",ReleaseFrame(rearmed,42.099,5,2,true,false).Kvt==3,3,"hold");
        Check("Rearmed moving release permits KVTII after35s/allauto",ReleaseFrame(rearmed,42.1,5,2,true,false).Kvt==2,2,"release");
        var c=new TrainerTrainBrakeCycle();BeginService(c);Held(c,7,4.3,false);
        ReleaseFrame(c,7.1,4.3,4,false,false);ReleaseFrame(c,7.2,4.3,1,false,false);
        var bad=Sample(20,5.3);bad.AllAutoReleased=false;bad.AllBrakesReleased=false;bad.ObservedCommandsKnown=true;bad.ObservedKm=2;
        bad.IndependentKvtPressureBar=1.5;bad.MinBogiePressureBar=.9;bad.MaxBogiePressureBar=1.8;
        var reset=c.Step(bad);Near("One low bogie resets confirmed hold",reset.KvtBandStartedSeconds,-1);
        ReleaseFrame(c,21,5.3,2,false,false);var late=ReleaseFrame(c,60,5,2,false,false);
        Check("40s elapsed does not release KVT with train applied",late.Kvt==3&&!late.CanApplyTraction,3,late.Kvt);
        var all=ReleaseFrame(c,61,5,2,true,false);Check("Delayed automatic release still needs all bogies clear for traction",all.Kvt==2&&!all.CanApplyTraction,"KVTII/no traction",all.CanApplyTraction);
        Check("Cleared bogies plus60s permits traction",ReleaseFrame(c,67.2,5,2,true,true,false).CanApplyTraction,true,"allclear");
        var small=new TrainerTrainBrakeCycle();BeginService(small);var s=Sample(7,4.3);s.Axles=100;s.AllAutoReleased=false;s.AllBrakesReleased=false;
        Check("100axes has no moving pre-release KVT rule",small.Step(s).Km==1,1,"I");
        var large=new TrainerTrainBrakeCycle();BeginService(large);s.Axles=101;
        Check("101axes requires measured KVT",large.Step(s).Km==4,4,"IV until actual KVT");
    }
    static void StrengtheningAndSand() {
        var c=new TrainerTrainBrakeCycle();BeginService(c);
        Check("149.999s continuous IV does not deepen",Held(c,150.999,4.3).Km==4,4,"before150s");
        var first=c.Step(new TrainerTrainBrakeCycleInput{Seconds=151,UrBar=4.3,ChargeBar=5,ActualTmBar=4.3,HeadSpeedKmh=20,MaximumConsistSpeedKmh=20,SensorsKnown=true,Ready=true,OwnsCommands=true,BrakeDemand=true,Axles=300,ApplicationKind=TrainerTrainBrakeKind.Service,HasAmbientTemperature=true,AmbientTemperatureC=20});
        Check("150s requires an additional step",first.Km==5,5,first.Km);Near("Added0.3 target",first.TargetUrBar,4);
        Held(c,152,4);var pending=Held(c,302,4);
        Check("Next150s cannot silently hold without sand",pending.State==TrainerTrainBrakeCycleState.AwaitingSand&&pending.RequestSand&&pending.Km==4&&!pending.CanApplyTraction,"AwaitingSand/IV/coast",pending.State);
        var requestOnly=Held(c,303,4);Check("RequestSand is not confirmation",requestOnly.RequestSand&&requestOnly.Km==4,true,requestOnly.RequestSand);
        var sand=Sample(304,4);sand.BrakeDemand=true;sand.AllAutoReleased=false;sand.AllBrakesReleased=false;sand.SandConfirmed=true;
        var deep=c.Step(sand);Check("Confirmed sand permits deeper stage",deep.Km==5&&!deep.RequestSand,5,deep.Km);Near("Cumulative1.3 target",deep.TargetUrBar,3.7);
        Check("A1.3 reduction is not full service",deep.LastApplicationKind==TrainerTrainBrakeKind.Service,TrainerTrainBrakeKind.Service,deep.LastApplicationKind);
        Held(c,305,3.7);Near("First stage remains0.7 after additions",c.PreviousFirstReductionBar,.7);
        var passive=Sample(306,3.7);passive.ObservedCommandsKnown=true;passive.ObservedKm=4;passive.AllAutoReleased=false;passive.AllBrakesReleased=false;
        c.Observe(passive);Near("Advisor does not mistake total1.3 for first0.7",c.PreviousFirstReductionBar,.7);
        var early=new TrainerTrainBrakeCycle();BeginService(early);Held(early,7,4.3,false);ReleaseFrame(early,7.1,4.3,4,false,false);ReleaseFrame(early,7.2,4.3,1,false,false);
        var i=Sample(20);i.BrakeDemand=true;i.IsDownhill=true;var next=early.Step(i);
        Check("Timely brake is not blocked by recharge timer",next.Km==5,5,next.Km);Near("Under60s next FIRST is previousFIRST+.3",next.FirstReductionBar,1);Near("No extra0.3 from later total",next.TargetUrBar,4);
        var recovery=new TrainerTrainBrakeCycle();BeginService(recovery);Held(recovery,7,4.3,false);ReleaseFrame(recovery,7.1,4.3,4,false,false);ReleaseFrame(recovery,7.2,4.3,1,false,false);
        i.HoldRecoveryOnDescent=true;var secured=recovery.Step(i);Near("Separate descent hold-recovery uses prescribed0.7",secured.FirstReductionBar,.7);
    }
    static TrainerTrainBrakeCycle PrepareStopped(TrainerTrainBrakeKind kind,bool known,bool cold) {
        var c=new TrainerTrainBrakeCycle();double applied=kind==TrainerTrainBrakeKind.Emergency?.2:kind==TrainerTrainBrakeKind.FullService?3.5:4.3;
        var a=Sample(0,5,0);a.ApplicationKind=kind;a.ObservedCommandsKnown=true;a.ObservedKm=kind==TrainerTrainBrakeKind.Emergency?6:5;a.AllAutoReleased=false;a.AllBrakesReleased=false;
        var passive=c.Observe(a);Check("Physical brake observer has no outputs",passive.Km==0&&passive.Kvt==0&&!passive.RequestCoast,"zero outputs",new{passive.Km,passive.Kvt});
        a.Seconds=1;a.UrBar=applied;a.ObservedKm=kind==TrainerTrainBrakeKind.Emergency?6:4;c.Observe(a);
        var i=Sample(7,applied,0);i.ApplicationKind=kind;i.AllAutoReleased=false;i.AllBrakesReleased=false;i.ExplicitRestart=true;i.HasAmbientTemperature=known;i.ColdScenario=cold;i.AmbientTemperatureC=cold?-10:20;
        var request=c.Step(i);Check("Stopped explicit restart releases with full KVT",request.Km==1&&request.Kvt==6,"I/VI",new{request.Km,request.Kvt});
        i.Seconds=7.1;i.ExplicitRestart=false;i.ObservedCommandsKnown=true;i.ObservedKm=1;
        var confirmed=c.Step(i);Near("Stopped timer is actual I",confirmed.ReleaseStartedSeconds,7.1);
        i.Seconds=8;i.UrBar=5.3;i.ObservedKm=2;
        var recharge=c.Step(i);Check("Stopped release holds VI during wait",recharge.Km==2&&recharge.Kvt==6&&!recharge.CanApplyTraction,"II/VI",new{recharge.Km,recharge.Kvt});
        return c;
    }
    static void StoppedMatrix() {
        foreach(var kind in new[]{TrainerTrainBrakeKind.Service,TrainerTrainBrakeKind.FullService,TrainerTrainBrakeKind.Emergency})
        foreach(int scenario in new[]{0,1,2}) {
            bool known=scenario!=2,cold=scenario==1;double factor=scenario==0?1:1.5;
            double baseDelay=kind==TrainerTrainBrakeKind.Service?90:kind==TrainerTrainBrakeKind.FullService?120:360;
            var c=PrepareStopped(kind,known,cold);double at=7.1+baseDelay*factor;
            var i=Sample(at-.001,5,0);i.ApplicationKind=kind;i.HasAmbientTemperature=known;i.ColdScenario=cold;i.AmbientTemperatureC=cold?-10:20;
            i.AllAutoReleased=true;i.AllBrakesReleased=false;i.ObservedCommandsKnown=true;i.ObservedKm=2;
            var before=c.Step(i);Check(kind+"/temp"+scenario+" before deadline retains VI",before.Kvt==6&&!before.CanApplyTraction,6,before.Kvt);
            Near("Correct90/120/360 factor deadline",before.TractionAllowedAtSeconds,at);
            i.Seconds=at;var eligible=c.Step(i);Check("Deadline may release KVT but not ignore TC",eligible.Kvt==2&&!eligible.CanApplyTraction,"KVTII/no traction",new{eligible.Kvt,eligible.CanApplyTraction});
            i.Seconds=at+.1;i.AllBrakesReleased=true;var ready=c.Step(i);Check("Stopped restart requires all released brakes",ready.CanApplyTraction,true,ready.CanApplyTraction);
            if(!known)Check("Unknown temperature is explicit conservative reason",ready.ColdDelayFactor==1.5&&ready.Reason.Contains("температура неизвестна"),"factor1.5/reason",ready.Reason);
        }
    }
    static void StopAndHistory() {
        foreach(int demand in new[]{0,1,2}) {
            var c=new TrainerTrainBrakeCycle();var i=Sample(0,5,0);i.BrakeDemand=true;c.Step(i);
            i.Seconds=1;i.UrBar=4.3;i.AllAutoReleased=false;i.AllBrakesReleased=false;var stopped=c.Step(i);
            Check("Stopped brake keeps IV and VI",stopped.Km==4&&stopped.Kvt==6&&!stopped.CanApplyTraction,"IV/VI",new{stopped.Km,stopped.Kvt});
            i.Seconds=1000;i.ExplicitRestart=true;i.BrakeDemand=demand==0;i.StopRequired=demand==1;i.Ended=demand==2;
            var refused=c.Step(i);Check("Explicit restart cannot erase stop demand"+demand,refused.Km==4&&refused.Kvt==6&&!refused.CanApplyTraction,"hold",new{refused.Km,refused.Kvt});
            Near("Refused restart cannot invent release",refused.ReleaseStartedSeconds,-1);
        }
        var early=new TrainerTrainBrakeCycle();var e=Sample(0,5,0);e.BrakeDemand=true;early.Step(e);e.Seconds=1;e.UrBar=4.3;e.BrakeDemand=false;e.AllAutoReleased=false;e.AllBrakesReleased=false;early.Step(e);
        e.Seconds=2;e.ExplicitRestart=true;var lap=early.Step(e);Check("Explicit restart respects first six-second lap",lap.Km==4&&lap.Kvt==6,"IV/VI",new{lap.Km,lap.Kvt});
        var h=PrepareStopped(TrainerTrainBrakeKind.Service,true,false);double anchor=h.ReleaseStartedSeconds;
        var passive=Sample(40,5,0);passive.ObservedCommandsKnown=true;passive.ObservedKm=2;passive.AllAutoReleased=false;passive.AllBrakesReleased=false;
        var observation=h.Observe(passive);Check("Advisor observes without commands",observation.Km==0&&observation.Kvt==0&&!observation.RequestCoast,"0/0",new{observation.Km,observation.Kvt});Near("Advisor keeps release history",h.ReleaseStartedSeconds,anchor);
        passive.Seconds=41;passive.ExplicitRestart=true;var rearm=h.Step(passive);Near("Rearm never resets release anchor",rearm.ReleaseStartedSeconds,anchor);Check("Rearm does not bypass remaining interval",!rearm.CanApplyTraction,false,rearm.CanApplyTraction);
        var moving=new TrainerTrainBrakeCycle();BeginService(moving);Held(moving,7,4.3,false);ReleaseFrame(moving,7.1,4.3,4,false,false);ReleaseFrame(moving,7.2,4.3,1,false,false);
        var stop=Sample(20,5.3,0);stop.ObservedCommandsKnown=true;stop.ObservedKm=2;stop.AllAutoReleased=false;stop.AllBrakesReleased=false;moving.Observe(stop);
        stop.Seconds=21;stop.ExplicitRestart=true;var extended=moving.Step(stop);Near("Stop during release keeps earlier I",extended.ReleaseStartedSeconds,7.2);Near("Stop extends deadline to90 without reset",extended.TractionAllowedAtSeconds,97.2);
        stop.Seconds=67.2;stop.ExplicitRestart=false;stop.AllAutoReleased=true;stop.AllBrakesReleased=true;Check("Old moving deadline cannot grant stopped traction",!moving.Step(stop).CanApplyTraction,false,"extended stop gate");
    }
    static void Faults() {
        foreach(int fault in new[]{0,1,2,3,4,5,6,7}) {
            var c=new TrainerTrainBrakeCycle();var i=Sample(0);
            if(fault==0)i.SensorsKnown=false;if(fault==1)i.Ready=false;if(fault==2)i.UrBar=Double.NaN;
            if(fault==3)i.MinBogiePressureBar=2;if(fault==4)i.MaximumConsistSpeedKmh=0;
            if(fault==5){i.ObservedCommandsKnown=true;i.ObservedKm=9;}if(fault==6)i.AdditionalStepBar=.9;if(fault==7){i.HasAmbientTemperature=true;i.AmbientTemperatureC=Double.NaN;}
            var d=c.Step(i);Check("Sensor/readiness fault"+fault+" holds/coasts without traction",d.Km==4&&d.RequestCoast&&!d.CanApplyTraction,"IV/coast",new{d.Km,d.CanApplyTraction});
            Check("Fault requires a diagnostic reason"+fault,!String.IsNullOrEmpty(d.Reason),true,d.Reason);
            var seen=c.Observe(i);Check("Passive fault cannot assign native commands"+fault,seen.Km==0&&seen.Kvt==0&&!seen.RequestCoast,"0/0",new{seen.Km,seen.Kvt});
        }
        var backward=new TrainerTrainBrakeCycle();backward.Step(Sample(10));var b=backward.Step(Sample(9));Check("Backward time denies traction",!b.CanApplyTraction&&b.State==TrainerTrainBrakeCycleState.SensorHold,"hold",b.State);
        var unknown=new TrainerTrainBrakeCycle();var n=unknown.Step(null);Check("Missing observation denies traction",!n.CanApplyTraction,false,n.CanApplyTraction);
    }
    static void ObservedLapTimeoutAndTemperature() {
        var c=new TrainerTrainBrakeCycle();var i=Sample(0);i.BrakeDemand=true;c.Step(i);
        i.Seconds=1;i.UrBar=4.3;i.BrakeDemand=false;i.AllAutoReleased=false;i.AllBrakesReleased=false;i.ObservedCommandsKnown=true;i.ObservedKm=5;
        var reached=c.Step(i);Check("Target reached before actual IV does not release",reached.Km==4,4,reached.Km);
        i.Seconds=10;var unseen=c.Step(i);Check("Requested IV alone cannot start six seconds",unseen.Km==4&&unseen.ReleaseStartedSeconds<0,"IV/no release",unseen.Km);
        i.Seconds=10.1;i.ObservedKm=4;c.Step(i);i.Seconds=16.099;Check("5.999s since EFFECTIVE IV remains lap",c.Step(i).Km==4,4,"strict lap");
        i.Seconds=16.1;i.IndependentKvtPressureBar=1.5;i.MinBogiePressureBar=1.5;i.MaxBogiePressureBar=3.4;
        var merged=c.Step(i);Check("Automatic pressure above2.2 does not deadlock confirmed KVT release",merged.Km==1,1,merged.Km);
        i.Seconds=16.2;i.ObservedKm=1;c.Step(i);i.Seconds=60;i.UrBar=5.3;i.ObservedKm=2;i.AllAutoReleased=true;
        Check("After automatic release upper KVT band remains required",c.Step(i).Kvt==3,3,"target feedback");
        var timeout=new TrainerTrainBrakeCycle();var t=Sample(0);t.BrakeDemand=true;timeout.Step(t);t.Seconds=14.999;
        Check("UR target may still apply before15s",timeout.Step(t).Km==5,5,"application");t.Seconds=15;
        var failed=timeout.Step(t);Check("15s timeout retains IV VI and coast",failed.Km==4&&failed.Kvt==6&&failed.RequestCoast&&!failed.CanApplyTraction,"IV/VI/coast",new{failed.Km,failed.Kvt});
        var ghost=new TrainerTrainBrakeCycle();var unknown=Sample(0);unknown.SensorsKnown=false;ghost.Step(unknown);
        var recovered=Sample(1);recovered.ExplicitRestart=true;var idle=ghost.Step(recovered);Check("Virgin sensor recovery has no ghost release I",idle.Km==2&&idle.ReleaseStartedSeconds<0,2,idle.Km);
        var passive=new TrainerTrainBrakeCycle();var p=Sample(0,4.3);p.AllAutoReleased=false;p.AllBrakesReleased=false;p.ObservedCommandsKnown=true;p.ObservedKm=4;p.IsDownhill=true;
        passive.Observe(p);p.Seconds=1;p.BrakeDemand=true;var continued=passive.Step(p);
        Check("Taking ownership of an existing native brake keeps IV",continued.Km==4,4,continued.Km);
        var movingCold=new TrainerTrainBrakeCycle();BeginService(movingCold);Held(movingCold,7,4.3,false);
        var m=Sample(7.1,4.3);m.AllAutoReleased=false;m.AllBrakesReleased=false;m.IndependentKvtPressureBar=1.5;m.MinBogiePressureBar=1.5;m.MaxBogiePressureBar=1.5;m.HasAmbientTemperature=false;movingCold.Step(m);
        m.Seconds=7.2;m.ObservedCommandsKnown=true;m.ObservedKm=1;var mc=movingCold.Step(m);
        Near("Moving wait remains60 even with unknown temperature",mc.TractionAllowedAtSeconds,67.2);Near("Moving multiplier is1",mc.ColdDelayFactor,1);
        var stop=PrepareStopped(TrainerTrainBrakeKind.Service,false,false);var warm=Sample(100,5,0);warm.AllAutoReleased=true;warm.AllBrakesReleased=true;warm.ObservedCommandsKnown=true;warm.ObservedKm=2;
        var kept=stop.Step(warm);Near("Warm edit cannot lower an unknown-temperature deadline",kept.TractionAllowedAtSeconds,142.1);Check("Warm edit cannot grant traction at90s",!kept.CanApplyTraction,false,kept.CanApplyTraction);
        foreach(double speed in new[]{.299,.3}) {var boundary=new TrainerTrainBrakeCycle();var b=Sample(0,5,speed);b.BrakeDemand=true;var d=boundary.Step(b);Check("Strict standstill boundary"+speed,d.Kvt==(speed<.3?6:2),speed<.3?6:2,d.Kvt);}
        var full=new TrainerTrainBrakeCycle();BeginService(full);var a=Sample(7,4.3);a.BrakeDemand=true;a.AdditionalStepDemand=true;a.AdditionalStepBar=.8;a.SandConfirmed=true;a.AllAutoReleased=false;a.AllBrakesReleased=false;a.ObservedCommandsKnown=true;a.ObservedKm=4;
        var depth=full.Step(a);Near("Upper0.8 additional step accepted",depth.TargetUrBar,3.5);a.Seconds=8;a.UrBar=3.5;var actual=full.Step(a);
        Check("Measured1.5 is full-service class",actual.LastApplicationKind==TrainerTrainBrakeKind.FullService,TrainerTrainBrakeKind.FullService,actual.LastApplicationKind);
    }
    static void Run(string name,Action test) {try{test();cases.Add(new{name,passed=true});}catch(Exception e){failures++;cases.Add(new{name,passed=false,error=e.ToString()});}}
    static int Main() {
        TrainerSession.EnsureStarted();
        Run("moving_release_lap_and_all_pressure_gates",MovingRelease);Run("kvt_actual_band_and_late_automatic_release",BandAndLateRelease);
        Run("continuous_hold_recharge_and_sand",StrengtheningAndSand);Run("stopped_class_and_temperature_matrix",StoppedMatrix);
        Run("mandatory_stop_and_passive_history",StopAndHistory);Run("sensor_readiness_and_clock_faults",Faults);Run("observed_lap_timeout_cold_merge_and_recovery",ObservedLapTimeoutAndTemperature);
        bool passed=failures==0;File.WriteAllText(TrainerSession.ResultPath("Trainer_train_brake_cycle.json"),new JavaScriptSerializer{MaxJsonLength=8000000}.Serialize(new{passed,checks,cases,
            scope="Pure deterministic operator-command state tests with supplied observations and clock. No native host/physics, no output writes, no full normative certification. Effective KM I anchors release; pressure feedback and confirmed sand are separate from requests."}));
        Console.WriteLine((passed?"PASS":"FAIL")+" pure train brake cycle; checks="+checks.Count+" failed_cases="+failures);return passed?0:1;
    }
}
