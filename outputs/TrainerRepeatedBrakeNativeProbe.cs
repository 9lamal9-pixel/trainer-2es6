using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
public static class TrainerRepeatedBrakeNativeProbe {
 [STAThread] public static int Main(){return TrainerAutovedenieAdverseBrakeProbe.RunRepeatedBrakeNative();}
}
public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
 sealed partial class Fixture {
  public object repeatProof;
  object[] RepeatChambers(){var rows=new List<object>();for(int n=1;n<=75;n++){var car=motionCars[n];double tm=Float(car,0),zk=RepeatDouble(car,0x69),rk=RepeatDouble(car,0x71);rows.Add(new{wagon=n,tm,tc=Float(car,4),zr=Float(car,8),zk,rk,pog=RepeatDouble(car,0x91),rk_minus_zk=rk-zk,zk_minus_tm=zk-tm});}return rows.ToArray();}
  static double RepeatDouble(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
  public void MeasureEarlyRepeat(){
   var source=(TrainerMarkupSignalSource)Field("markupSource");
   try {
    foreach(var signal in source.Signals)source.SetTeachingSignalState(signal.Id,0);Call("RouteInputCycle");
    GenuineNativeStart();phase="declared_native80_original_long_minus6";
    const float x=304400;
    ((Delegate)Field("stateTrain")).DynamicInvoke(x,80f,2,76);preparedInitializations++;Call("MotionCycle");
    SetField("routeFinished",false);SetField("klubBasisReady",false);source.ResetPosition(x);Call("RouteInputCycle");
    var initial=Capture();Check("Prepared80 actual76 vehicles on originalminus6",initial.vehicles.All(v=>Math.Abs(v.v_m_s-80/3.6)<.03&&v.grade_permille==-6),"actual80/original-6",initial.maximum_abs_v_m_s);
    Check("Initial release before repeat scenario",initial.fully_released,true,initial);
    measured=true;measurementComplete=true;
    var policy=new TrainerTrainBrakeCycle();TrainerTrainBrakeCycleDecision d=null;
    double iv1=-1,i1=-1,v2=-1,iv2=-1,previousFirst=-1,secondStartUr=-1,secondFirst=-1,secondTarget=-1;
    int previousEffectiveKm=2,applications=0;double latestIv=-1,latestV=-1;
    bool asked=false,done=false,responseFailed=false;object[] chambers=null;int samples=0,sandWaitSamples=0,acceptedSandBrakeFrames=0;var trace=new List<object>();
    beforeNativePneumatic=()=>{
     var f=Capture();double now=cycle*Dt;
     bool demand=iv1<0||now-iv1<6.2;
     if(i1>=0&&now-i1>=30){asked=true;demand=true;}
     var input=new TrainerTrainBrakeCycleInput {Seconds=now,UrBar=f.ur_bar,ChargeBar=RepeatDouble(mem["TLocParam"],0x13),ActualTmBar=f.tm_bar[0],
      HeadSpeedKmh=f.head_v_kmh,MaximumConsistSpeedKmh=f.maximum_abs_v_m_s*3.6,IndependentKvtPressureBar=RepeatDouble(mem["TLocParam"],0xec),
      MinBogiePressureBar=f.bogies_tc_bar.Min(),MaxBogiePressureBar=f.bogies_tc_bar.Max(),SensorsKnown=true,Ready=f.epk_trip==0&&f.kon_trip==0&&f.coil==1&&f.saut_request==0,
      AllAutoReleased=f.automatic_released,AllBrakesReleased=f.fully_released,WagonTcFeedbackKnown=true,MinimumWagonTcBar=f.tc_bar.Skip(2).Min(),OwnsCommands=true,BrakeDemand=demand,IsDownhill=Grade(f.head_x_m)<0,
      Axles=300,ApplicationKind=TrainerTrainBrakeKind.Service,HasAmbientTemperature=true,AmbientTemperatureC=20,
      ObservedCommandsKnown=(bool)Field("observedPneumaticCommandsKnown"),ObservedKm=(int)Field("observedPneumaticKm"),ObservedKvt=(int)Field("observedPneumaticKvt"),
      SandConfirmed=Marshal.ReadByte(mem["TPult"],0xa5)==1};
     d=policy.Step(input);samples++;
     if(d.State==TrainerTrainBrakeCycleState.SensorHold)throw new Exception("Early repeat sensor hold: "+d.Reason);
     if(d.CanApplyTraction)throw new Exception("Early repeat unexpectedly permits traction");
     if(!asked&&d.State==TrainerTrainBrakeCycleState.Holding)previousFirst=policy.PreviousFirstReductionBar;
     if(asked&&d.State==TrainerTrainBrakeCycleState.AwaitingSand){sandWaitSamples++;if(!d.RequestSand||d.Km!=4)throw new Exception("Deep repeat did not wait inIV for original sand acknowledgement");}
     if(asked&&v2<0&&d.Km==5){secondStartUr=input.UrBar;secondFirst=d.FirstReductionBar;secondTarget=d.TargetUrBar;Check("Repeat first step is at least previous measuredFIRST+.3",previousFirst>=.7&&secondFirst>=previousFirst+.3-.000001,previousFirst+.3,secondFirst);Check("Original sandack precedes repeated deep application",input.SandConfirmed,true,input.SandConfirmed);}
     // Scripted physical operator follows the production policy request. No AF,
     // native pressure, force, velocity or protection output is assigned.
     Cab(0x3d4,(byte)(d.RequestSand?1:0));
     if(Marshal.ReadByte(mem["TLocParam"],0x7d)!=d.Km)Handle(0x3b0,d.Km);
     if(Marshal.ReadByte(mem["TLocParam"],0x7e)!=d.Kvt)Handle(0x3b4,d.Kvt);
     if(samples%10==0||d.RequestSand)trace.Add(new{now,asked,d.State,d.Km,d.Kvt,d.RequestSand,input.SandConfirmed,d.FirstReductionBar,d.TargetUrBar,d.ReleaseStartedSeconds,f.ur_bar,f.head_v_kmh,minTC=f.tc_bar.Skip(2).Min(),maxTC=f.tc_bar.Skip(2).Max()});
    };
    afterNativeMotion=()=>{
     var f=Capture();if(f.vehicles.Any(v=>v.grade_permille!=-6))throw new Exception("Left original full-consist minus6 before earlyrepeat proof");
     if(f.maximum_abs_v_m_s*3.6>90)throw new Exception("Repeat fixture predictor admission speed exceeded90");
     if((bool)Field("observedPneumaticCommandsKnown")){
      int km=(int)Field("observedPneumaticKm");double actual=(double)Field("observedPneumaticCommandSeconds");
      if(!asked&&km==4&&iv1<0)iv1=actual;
      if(!asked&&km==1&&i1<0){i1=actual;Check("First release follows actualIVsix seconds",iv1>=0&&actual-iv1>=6,">=6",new{iv1,actual});}
      if(asked&&km==5&&previousEffectiveKm!=5){latestV=actual;applications++;}
      if(asked&&km==4&&previousEffectiveKm!=4)latestIv=actual;
      if(asked&&km==5&&v2<0){v2=actual;Check("Repeat begins before60s recharge",i1>=0&&actual-i1>=30&&actual-i1<60,"30..60",new{i1,actual});}
      if(v2>=0&&km==5&&Marshal.ReadByte(mem["TPult"],0xa5)==1&&(bool)Field("nativeSandAirLastAssigned"))acceptedSandBrakeFrames++;
      if(v2>=0&&km==4&&iv2<0){iv2=actual;double observedReduction=secondStartUr-f.ur_bar;Check("Actual repeatedUR reaches the increased first step",observedReduction>=secondFirst&&observedReduction<=secondFirst+.1,secondFirst,observedReduction);}
      if(applications>=3&&f.tc_bar.Skip(2).Min()<1){responseFailed=true;chambers=RepeatChambers();}
      if(latestIv>=0&&km==4&&cycle*Dt-latestIv>=6.2&&f.tc_bar.Skip(2).Min()>=1){done=true;Check("Real repeatedIV six seconds/all75TC without traction",km==4&&Math.Abs(f.force_native)<.001&&f.maximum_abs_v_m_s*3.6>.3,"IV/allTC/moving/coast",new{km,minTC=f.tc_bar.Skip(2).Min(),f.maximum_abs_v_m_s});}
      previousEffectiveKm=km;
     }
    };
    phase="production_early_repeat_policy_native_inputs";
    for(int n=0;n<1800&&!done&&!responseFailed;n++)Step();
    beforeNativePneumatic=null;afterNativeMotion=null;Cab(0x3d4,0);
    repeatProof=new{passed=done,initial,samples,first_effective_IV_seconds=iv1,first_effective_I_seconds=i1,second_effective_V_seconds=v2,second_effective_IV_seconds=iv2,
     repeat_after_I_seconds=v2-i1,previous_first_reduction=previousFirst,second_first_reduction=secondFirst,second_start_UR=secondStartUr,second_target_UR=secondTarget,
     sand_wait_samples=sandWaitSamples,accepted_sand_brake_frames=acceptedSandBrakeFrames,response_failed_after_three_applications=responseFailed,applications,latest_effective_V_seconds=latestV,latest_effective_IV_seconds=latestIv,chambers=chambers??RepeatChambers(),final=Capture(),trace,
     scope="Prepared80 at original304400m/wholeconsist-6 on full333033 route; scriptedoperator follows productionbrakecycle KM/KVT/sand request. ActualI+30 earlyrepeat, previousFIRST+.3, originalAF acknowledgement and actualsandair input, secondIV6/allTC. Teachinggreen/SAUTOFF/scriptedRBS diagnostic. No fullAVP or whole-route acceptance."};
    Check("Earlyrepeat completes actual second lap",done&&sandWaitSamples>0&&acceptedSandBrakeFrames>0,true,new{done,sandWaitSamples,acceptedSandBrakeFrames,iv2,final=Capture()});
    Check("Repeat retains one declared initialization",preparedInitializations==1,1,preparedInitializations);
   }finally{beforeNativePneumatic=null;afterNativeMotion=null;source.RestorePersistedSignalStates();Check("Repeat teaching states restored",source.Signals.All(s=>!s.TeachingOverride&&s.State==s.OriginalState),true,true);}
  }
 }
 public static int RunRepeatedBrakeNative(){
  Application.EnableVisualStyles();thread=GetCurrentThreadId();Watch.Restart();Checks.Clear();Cases.Clear();var f=new Fixture();int exit=0;string error=null;
  try{f.Start();f.MeasureEarlyRepeat();}catch(Exception e){error=e.ToString();f.error=error;exit=1;Console.WriteLine(e);}
  finally{f.beforeNativePneumatic=null;f.afterNativeMotion=null;f.Close();if(f.error!=null||!f.closed||!f.restored){exit=1;error=f.error;}}
  File.WriteAllText(TrainerSession.ResultPath("Trainer_repeated_brake_native.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(new{passed=exit==0,error,checks=Checks,cycle=f.repeatProof,closed=f.closed,restored=f.restored,full_avp_accepted=false}));
  Console.WriteLine(exit==0?"PASS early repeat native firststep/sand/pressure/lap":"FAIL early repeated native brake cycle");return exit;
 }
}
