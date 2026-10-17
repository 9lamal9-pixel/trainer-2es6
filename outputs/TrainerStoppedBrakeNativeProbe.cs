using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
public static class TrainerStoppedBrakeNativeProbe {
 [STAThread] public static int Main(string[] args) { return TrainerAutovedenieAdverseBrakeProbe.RunStoppedBrakeCycle(args); }
}
public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
 sealed partial class Fixture {
  public object stoppedCycleProof;
  TrainerTrainBrakeCycleInput StopObservation(Frame f,TrainerTrainBrakeKind kind,bool cold,bool known,bool owns,bool restart) {
   return new TrainerTrainBrakeCycleInput { Seconds=cycle*Dt,UrBar=f.ur_bar,ChargeBar=StopDouble(mem["TLocParam"],0x13),ActualTmBar=f.tm_bar[0],
    HeadSpeedKmh=f.head_v_kmh,MaximumConsistSpeedKmh=f.maximum_abs_v_m_s*3.6,IndependentKvtPressureBar=StopDouble(mem["TLocParam"],0xec),
    MinBogiePressureBar=f.bogies_tc_bar.Min(),MaxBogiePressureBar=f.bogies_tc_bar.Max(),SensorsKnown=true,
    Ready=f.epk_trip==0&&f.kon_trip==0&&f.coil==1&&f.saut_request==0,AllAutoReleased=f.automatic_released,AllBrakesReleased=f.fully_released,WagonTcFeedbackKnown=true,MinimumWagonTcBar=f.tc_bar.Skip(2).Min(),
    OwnsCommands=owns,ExplicitRestart=restart,Axles=300,ApplicationKind=kind,HasAmbientTemperature=known,AmbientTemperatureC=cold?-10:20,ColdScenario=cold,
    ObservedCommandsKnown=(bool)Field("observedPneumaticCommandsKnown"),ObservedKm=(int)Field("observedPneumaticKm"),ObservedKvt=(int)Field("observedPneumaticKvt"),
    SandConfirmed=Marshal.ReadByte(mem["TPult"],0xa5)==1 };
  }
  static double StopDouble(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
  public void MeasureStoppedCycle(TrainerTrainBrakeKind kind,bool cold,bool known) {
   GenuineNativeStart();phase="declared_prepared_zero_on_original_flat";
   const float flatX=16000;
   ((Delegate)Field("stateTrain")).DynamicInvoke(flatX,0f,2,76);preparedInitializations++;Call("MotionCycle");
   SetField("routeFinished",false);SetField("klubBasisReady",false);((TrainerMarkupSignalSource)Field("markupSource")).ResetPosition(flatX);Call("RouteInputCycle");
   var initial=Capture();Check("All76 prepared stopped vehicles on original flat",initial.vehicles.All(v=>Math.Abs(v.v_m_s)*3.6<.3&&v.grade_permille==0),"actual76 stopped/flat",initial.maximum_abs_v_m_s);
   measured=true;measurementComplete=true;
   var policy=new TrainerTrainBrakeCycle();TrainerTrainBrakeCycleDecision decision=null;
   var trace=new List<object>();bool owner=false,restart=false,done=false;
   double firstIv=-1,firstI=-1,firstKvtII=-1,permission=-1;int ownedSamples=0,earlyDenied=0,allAutoSeen=-1;
   double baseDelay=kind==TrainerTrainBrakeKind.Service?90:kind==TrainerTrainBrakeKind.FullService?120:360;
   double required=baseDelay*(cold||!known?1.5:1);
   beforeNativePneumatic=()=>{
    var f=Capture();var input=StopObservation(f,kind,cold,known,owner,restart);restart=false;
    if(!owner){policy.Observe(input);return;}
    decision=policy.Step(input);ownedSamples++;
    if(decision.State==TrainerTrainBrakeCycleState.SensorHold||decision.State==TrainerTrainBrakeCycleState.AwaitingSand)
     throw new Exception("Stopped native cycle held: "+decision.Reason);
    if(f.maximum_abs_v_m_s*3.6>=.3)throw new Exception("Declared flat stopped case developed actual motion: "+f.maximum_abs_v_m_s);
    if(firstI>=0&&Math.Abs(decision.ReleaseStartedSeconds-firstI)>.100001)throw new Exception("Release anchor differs from actual preceding native I");
    if(firstI>=0&&input.Seconds-firstI<required){earlyDenied++;if(decision.CanApplyTraction||decision.Kvt!=6)throw new Exception("Premature stopped traction/fullKVT release");}
    if(firstI>=0&&f.automatic_released&&allAutoSeen<0)allAutoSeen=cycle;
    if(firstI>=0&&decision.Kvt==2&&firstKvtII<0){firstKvtII=input.Seconds;Check("StoppedKVTII only after actualI deadline/allauto",input.Seconds-firstI>=required&&f.automatic_released,required,input.Seconds-firstI);}
    if(decision.CanApplyTraction){permission=input.Seconds;done=true;Check("Stopped traction permission after timer and every actual TC release",firstI>=0&&input.Seconds-firstI>=required&&f.fully_released&&decision.Kvt==2,"deadline/allTC",new {firstI,permission,f.fully_released});}
    if(Marshal.ReadByte(mem["TLocParam"],0x7d)!=decision.Km)Handle(0x3b0,decision.Km);
    if(Marshal.ReadByte(mem["TLocParam"],0x7e)!=decision.Kvt)Handle(0x3b4,decision.Kvt);
    if(ownedSamples%10==0||done)trace.Add(new {seconds=input.Seconds,decision.State,decision.Km,decision.Kvt,decision.CanApplyTraction,decision.ReleaseStartedSeconds,decision.TractionAllowedAtSeconds,
     decision.LastApplicationKind,f.head_v_kmh,f.maximum_abs_v_m_s,f.ur_bar,maxAuto=f.tc_bar.Max(),maxBogie=f.bogies_tc_bar.Max()});
   };
   afterNativeMotion=()=>{
    if((bool)Field("observedPneumaticCommandsKnown")){
     int km=(int)Field("observedPneumaticKm");double actual=(double)Field("observedPneumaticCommandSeconds");
     if(km==4&&firstIv<0)firstIv=actual;
     if(owner&&km==1&&firstI<0){firstI=actual;Check("Stopped release begins after actualIV six seconds",firstIv>=0&&actual-firstIv>=6,">=6",new{firstIv,actual});}
    }
   };
   var loc=mem["TLocParam"];double before=initial.ur_bar,requested=kind==TrainerTrainBrakeKind.FullService?1.6:.7;
   Handle(0x3b4,6);
   // Physical cab sand command goes through original Communication/Pult before
   // any full-service reduction>1.0. This proves command, not air/adhesion.
   if(kind==TrainerTrainBrakeKind.FullService){Cab(0x3d4,1);Step();Check("Original sand acknowledgement precedes fullservice",Marshal.ReadByte(mem["TPult"],0xa5)==1,1,Marshal.ReadByte(mem["TPult"],0xa5));}
   phase="physical_stopped_brake_application";Handle(0x3b0,kind==TrainerTrainBrakeKind.Emergency?6:5);
   for(int n=0;n<600&&(kind==TrainerTrainBrakeKind.Emergency?Float(loc,9)>.1:before-Float(loc,9)<requested);n++)Step();
   double actualReduction=before-Float(loc,9);
   Check("Stopped actual native UR application",kind==TrainerTrainBrakeKind.Emergency?Float(loc,9)<=.1:actualReduction>=requested&&actualReduction<=(kind==TrainerTrainBrakeKind.FullService?1.7:.8),requested,actualReduction);
   Handle(0x3b0,4);phase="stopped_fullKVT_actualIV_lap";
   for(int n=0;n<1500;n++) {Step();var f=Capture();if(firstIv>=0&&cycle*Dt-firstIv>=6.2&&f.tc_bar.Skip(2).Min()>=1&&f.bogies_tc_bar.Min()>=3.8)break;}
   var applied=Capture();Check("Applied all75 TC and fullKVT at actual standstill",applied.tc_bar.Skip(2).Min()>=1&&applied.bogies_tc_bar.Min()>=3.8&&applied.maximum_abs_v_m_s*3.6<.3,"allTC>=1/fullVI/stopped",new {minAuto=applied.tc_bar.Skip(2).Min(),minBogie=applied.bogies_tc_bar.Min(),applied.maximum_abs_v_m_s});
   if(kind==TrainerTrainBrakeKind.FullService)Cab(0x3d4,0);
   owner=true;restart=true;phase="production_stopped_release_cycle";
   for(int n=0;n<7000&&!done;n++)Step();
   beforeNativePneumatic=null;afterNativeMotion=null;
   Check("Stopped release reaches actual permitted condition",done&&earlyDenied>0&&firstKvtII>=0,true,new {done,earlyDenied,firstI,firstKvtII,permission});
   Check("Observed kind determines stopped interval",decision.LastApplicationKind==kind,kind,decision.LastApplicationKind);
   Check("One prepared initialization only",preparedInitializations==1,1,preparedInitializations);
   stoppedCycleProof=new {passed=true,kind,declared_temperature_known=known,declared_temperature_c=known?(double?)(cold?-10:20):null,required_delay_seconds=required,
    native_thermal_temperature_modified=false,initial,applied,actual_reduction=actualReduction,first_effective_IV_seconds=firstIv,first_effective_I_seconds=firstI,first_KVTII_request_seconds=firstKvtII,
    traction_permission_seconds=permission,early_denied_samples=earlyDenied,owned_samples=ownedSamples,all_auto_released_cycle=allAutoSeen,final=Capture(),trace,
    scope="One explicit prepared stopped initial state on original flat, original native realKM/KVT/TC; production policy after physical brake, timers from actualI. Winter tests declare policy scenario only; native thermal calibration unchanged. No actual postpermissiontraction/fullAVP acceptance."};
  }
 }
 public static int RunStoppedBrakeCycle(string[] args) {
  Application.EnableVisualStyles();thread=GetCurrentThreadId();Watch.Restart();Checks.Clear();Cases.Clear();var f=new Fixture();int exit=0;string error=null;
  try {
   if(args==null||args.Length!=2)throw new Exception("Expected service|fullservice|emergency andwarm|cold|unknown");
   var kind=(TrainerTrainBrakeKind)Enum.Parse(typeof(TrainerTrainBrakeKind),args[0],true);if(kind==TrainerTrainBrakeKind.Unknown)throw new Exception("Explicit kind required");
   if(args[1]!="warm"&&args[1]!="cold"&&args[1]!="unknown")throw new Exception("Explicit temperature scenario required");
   f.Start();f.MeasureStoppedCycle(kind,args[1]=="cold",args[1]!="unknown");
  }catch(Exception e){error=e.ToString();f.error=error;exit=1;Console.WriteLine(e);}
  finally{f.beforeNativePneumatic=null;f.afterNativeMotion=null;f.Close();if(f.error!=null||!f.closed||!f.restored){exit=1;error=f.error;}}
  File.WriteAllText(TrainerSession.ResultPath("Trainer_stopped_brake_native.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(new{passed=exit==0,error,checks=Checks,cycle=f.stoppedCycleProof,closed=f.closed,restored=f.restored,full_avp_accepted=false}));
  Console.WriteLine(exit==0?"PASS stopped native release deadline and actualTC":"FAIL stopped native brake cycle");return exit;
 }
}
