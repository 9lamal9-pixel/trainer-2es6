using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
public static class TrainerTrainBrakeNativeProbe {
 [STAThread] public static int Main(){return TrainerAutovedenieAdverseBrakeProbe.RunNativeBrakeCycle();}
}
public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
 sealed partial class Fixture {
  public object cycleProof;
  public void MeasureNormativeCycle(){
   GenuineNativeStart();phase="declared_native60_for_moving_release";
   ((Delegate)Field("stateTrain")).DynamicInvoke((float)StartX,60f,2,76);preparedInitializations++;Call("MotionCycle");
   SetField("routeFinished",false);SetField("klubBasisReady",false);((TrainerMarkupSignalSource)Field("markupSource")).ResetPosition(StartX);Call("RouteInputCycle");
   var initial=Capture();Check("Declared60 is actual76vehicle velocity",initial.vehicles.All(x=>Math.Abs(x.v_m_s-60/3.6)<.03),60/3.6,initial.maximum_abs_v_m_s);
   measured=true;measurementComplete=true;
   var policy=new TrainerTrainBrakeCycle();TrainerTrainBrakeCycleDecision decision=null;
   double firstIv=-1,firstI=-1,band=-1,firstKvtRelease=-1,permission=-1;int samples=0;bool demand=true,done=false,stopSeen=false;
   var trace=new List<object>();
   beforeNativePneumatic=()=>{
    var f=Capture();double seconds=cycle*Dt;
    if(firstIv>=0 && seconds-firstIv>=6.2)demand=false;
    bool ready=f.epk_trip==0&&f.kon_trip==0&&f.coil==1&&f.saut_request==0;
    bool restart=decision!=null&&decision.State==TrainerTrainBrakeCycleState.StoppedHold&&!demand;
    var input=new TrainerTrainBrakeCycleInput {Seconds=seconds,UrBar=f.ur_bar,ChargeBar=DoubleAtLocal(mem["TLocParam"],0x13),ActualTmBar=f.tm_bar[0],
     HeadSpeedKmh=f.head_v_kmh,MaximumConsistSpeedKmh=f.maximum_abs_v_m_s*3.6,IndependentKvtPressureBar=DoubleAtLocal(mem["TLocParam"],0xec),
     MinBogiePressureBar=f.bogies_tc_bar.Min(),MaxBogiePressureBar=f.bogies_tc_bar.Max(),SensorsKnown=true,Ready=ready,
     AllAutoReleased=f.automatic_released,AllBrakesReleased=f.fully_released,WagonTcFeedbackKnown=true,MinimumWagonTcBar=f.tc_bar.Skip(2).Min(),OwnsCommands=true,BrakeDemand=demand,IsDownhill=Grade(f.head_x_m)<0,
     ExplicitRestart=restart,Axles=300,HasAmbientTemperature=true,AmbientTemperatureC=20,
     ObservedCommandsKnown=(bool)Field("observedPneumaticCommandsKnown"),ObservedKm=(int)Field("observedPneumaticKm"),ObservedKvt=(int)Field("observedPneumaticKvt"),
     ApplicationKind=TrainerTrainBrakeKind.Service,SandConfirmed=Marshal.ReadByte(mem["TPult"],0xa5)==1};
    decision=policy.Step(input);samples++;
    if(decision.Kvt==3 && input.IndependentKvtPressureBar>=1 && input.IndependentKvtPressureBar<=2.2 && input.MinBogiePressureBar>=1 && band<0)band=seconds;
    if(firstI>=0 && !decision.CanApplyTraction && decision.Kvt==2 && firstKvtRelease<0){firstKvtRelease=seconds;Check("KVT release follows35s and all actual automatic TC",band>=0&&seconds-band>=35&&f.automatic_released,">=35 / allautoTC",new{seconds,band,f.automatic_released});}
    if(decision.CanApplyTraction){permission=seconds;done=true;Check("No native traction permission beforeI+60",firstI>=0&&seconds-firstI>=60&&f.fully_released&&decision.Kvt==2,"I+60 / allTC",new{seconds,firstI,f.fully_released});}
    if(f.maximum_abs_v_m_s*3.6<.3)stopSeen=true;
    if(decision.State==TrainerTrainBrakeCycleState.SensorHold||decision.State==TrainerTrainBrakeCycleState.AwaitingSand)throw new Exception("Native release cycle fault: "+decision.Reason);
    if(Marshal.ReadByte(mem["TLocParam"],0x7d)!=decision.Km)Handle(0x3b0,decision.Km);
    if(Marshal.ReadByte(mem["TLocParam"],0x7e)!=decision.Kvt)Handle(0x3b4,decision.Kvt);
    if(samples%10==0||done)trace.Add(new {seconds,decision.State,decision.Km,decision.Kvt,decision.CanApplyTraction,decision.ReleaseStartedSeconds,decision.TractionAllowedAtSeconds,decision.Reason,f.head_v_kmh,f.maximum_abs_v_m_s,f.ur_bar,minAuto=f.tc_bar.Min(),maxAuto=f.tc_bar.Max(),f.bogies_tc_bar});
   };
   afterNativeMotion=()=>{
    double actual=(double)Field("observedPneumaticCommandSeconds");
    if((bool)Field("observedPneumaticCommandsKnown")){
     int km=(int)Field("observedPneumaticKm");
     if(km==4&&firstIv<0)firstIv=actual;
     if(km==1&&firstI<0){firstI=actual;Check("NativeI follows effectiveIV six seconds",firstIv>=0&&actual-firstIv>=6,">=6",new{actual,firstIv});}
    }
   };
   phase="native_observed_brake_release_cycle";
   for(int n=0;n<5000&&!done;n++)Step();
   beforeNativePneumatic=null;afterNativeMotion=null;
   Check("Native regulatory cycle reaches actual released permission",done,true,new{firstIv,firstI,band,firstKvtRelease,permission,samples,stopSeen});
   Check("Moving case remains moving through release",!stopSeen,true,stopSeen);
   Check("No additional prepared initialization",preparedInitializations==1,1,preparedInitializations);
   cycleProof=new {passed=true,initial,samples,first_effective_IV_seconds=firstIv,first_effective_I_seconds=firstI,kvt_band_seconds=band,independent_release_seconds=firstKvtRelease,traction_permission_seconds=permission,stop_seen=stopSeen,final=Capture(),trace,
    scope="Prepared60kmh on original full route; real KM/KVT inputs from production brake-cycle class. Actual native I anchor, six-secondIV, KVT35+allauto release and60s+allTC gates. No traction applied after permission and no productionAVP acceptance."};
  }
  static double DoubleAtLocal(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
 }
 public static int RunNativeBrakeCycle(){
  Application.EnableVisualStyles();thread=GetCurrentThreadId();Watch.Start();var f=new Fixture();int exit=0;string error=null;
  try{f.Start();f.MeasureNormativeCycle();}catch(Exception e){error=e.ToString();f.error=error;exit=1;Console.WriteLine(e);}
  finally{f.beforeNativePneumatic=null;f.afterNativeMotion=null;f.Close();if(f.error!=null||!f.closed||!f.restored){exit=1;error=f.error;}}
  File.WriteAllText(TrainerSession.ResultPath("Trainer_train_brake_native.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(new{passed=exit==0,error,checks=Checks,cycle=f.cycleProof,closed=f.closed,restored=f.restored,full_avp_accepted=false}));
  Console.WriteLine(exit==0?"PASS native observed regulatory brake cycle":"FAIL native regulatory brake cycle");return exit;
 }
}
