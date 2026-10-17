using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using TrainerAir;
public static class TrainerNativeSandAirProbe {
 [STAThread] public static int Main(){return TrainerAutovedenieAdverseBrakeProbe.RunNativeSandAir();}
}
public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
 sealed class SandAirRow {
  public string phase;public int cycle,km;public byte accepted,input,compressorState;public bool fresh;
  public double pmBefore,pmAfter,dpIV,dpOther,pmPhysicalBefore,pmPhysicalAfter,massBefore,massAfter,expectedSandLoss;
 }
 sealed partial class Fixture {
  Delegate sandAirOriginalStep;FieldInfo sandAirStepField;readonly List<SandAirRow> sandAirRows=new List<SandAirRow>();
  SandAirRow sandAirPending;bool sandAirStale;public object sandAirProof;
  void SandAirObservedStep(IntPtr data) {
   var loc=mem["TLocParam"];var circuit=(TwoSectionAirChain)Field("airCircuit");
   var row=new SandAirRow {phase=phase,cycle=cycle,km=Marshal.ReadByte(loc,0x7d),accepted=Marshal.ReadByte(mem["TPult"],0xa5),input=Marshal.ReadByte(loc,0x7f),
    fresh=(bool)Field("nativeSandElectricalReady")&&(int)Field("nativeSandElectricalTick")==Convert.ToInt32(Field("ticks")),
    pmBefore=Float(loc,5),dpIV=SandAirDouble(loc,0x2f),dpOther=SandAirDouble(loc,0x1f),pmPhysicalBefore=circuit.Pm.Gauge,massBefore=circuit.Pm.Mass};
   if(Enumerable.Range(0,8).Any(n=>Marshal.ReadByte(loc,0x80+n)!=0))throw new Exception("Physical main compressors not OFF in sandaircase");
   sandAirOriginalStep.DynamicInvoke(data);
   row.pmAfter=Float(loc,5);row.compressorState=Marshal.ReadByte(loc,0x12);
   row.expectedSandLoss=row.input!=0?4*(row.km==4?row.dpIV:row.dpOther):0;
   if(row.compressorState!=0)throw new Exception("Native no-compressor branch failed to establish Loc12=0");
   if(row.input!=(row.accepted==1&&row.fresh?1:0))throw new Exception("Original acceptedAF/freshness did not match borrowed nativeinput");
   if(row.input!=0 && (row.expectedSandLoss<=0||row.pmBefore-row.pmAfter<row.expectedSandLoss-.000001))throw new Exception("Native sandair pressure loss did not include its4*actualDP law");
   sandAirPending=row;sandAirRows.Add(row);
  }
  static double SandAirDouble(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
  void SandAirPulse(string label,int km,byte cab,bool stale,int count){
   phase=label;sandAirStale=stale;Handle(0x3b0,km);Cab(0x3d4,cab);
   for(int n=0;n<count;n++)Step();
  }
  public void MeasureSandAir(){
   phase="sandair_prepare";Cab(0x3c8,0);Handle(0x3b0,4);Handle(0x3b4,2);Warm(10);
   Check("Initial native sandposition restored zero",Marshal.ReadByte(mem["TLocParam"],0x7f)==0,0,Marshal.ReadByte(mem["TLocParam"],0x7f));
   Check("No active drive while proving manual sandair",Field("avpController")==null,true,Field("avpUnavailable"));
   sandAirStepField=typeof(TrainerIntegrationHost).GetField("pneumoStep",Flags);sandAirOriginalStep=(Delegate)sandAirStepField.GetValue(host);
   var wrapper=Delegate.CreateDelegate(sandAirStepField.FieldType,this,GetType().GetMethod("SandAirObservedStep",Flags));sandAirStepField.SetValue(host,wrapper);
   beforeNativePneumatic=()=>{if(sandAirStale)SetField("nativeSandElectricalReady",false);};
   afterNativeMotion=()=>{
    var row=sandAirPending;if(row==null)throw new Exception("Sandair capture missing");var loc=mem["TLocParam"];var circuit=(TwoSectionAirChain)Field("airCircuit");
    row.pmPhysicalAfter=circuit.Pm.Gauge;row.massAfter=circuit.Pm.Mass;
    if(Marshal.ReadByte(loc,0x7f)!=0)throw new Exception("Sand input lease did not restore outside nativepneumaticcall");
    if(row.input!=0&&(row.pmPhysicalAfter>row.pmAfter+.000001||row.massAfter>=row.massBefore))throw new Exception("Sand native pressure loss did not reach finite physical PM mass");
    sandAirPending=null;
   };
   try {
    SandAirPulse("KMIV_OFF",4,0,false,8);SandAirPulse("KMIV_ON",4,1,false,8);SandAirPulse("KMIV_OFF_AGAIN",4,0,false,8);
    SandAirPulse("KMII_OFF",2,0,false,8);SandAirPulse("KMII_ON",2,1,false,8);SandAirPulse("KMII_STALE",2,1,true,2);SandAirPulse("KMII_OFF_AGAIN",2,0,false,8);
    foreach(int km in new[]{2,4}){
     var on=sandAirRows.Where(x=>x.km==km&&x.input==1).ToArray();var off=sandAirRows.Where(x=>x.km==km&&x.accepted==0).ToArray();
     double minOn=on.Min(x=>x.pmBefore-x.pmAfter),maxOff=off.Max(x=>x.pmBefore-x.pmAfter);
     Check("Actual sandair consumption exceeds OFF background KM"+km,on.Length==8&&off.Length>=16&&minOn>maxOff+.000001,"onloss>offloss",new{minOn,maxOff});
     Check("All sandair pulses reduce physical finitePM KM"+km,on.All(x=>x.massAfter<x.massBefore&&x.expectedSandLoss>0),true,on.Select(x=>x.massBefore-x.massAfter).ToArray());
    }
    Check("Freshness blocks actual held sand command",sandAirRows.Count(x=>x.accepted==1&&!x.fresh&&x.input==0)==2,2,sandAirRows.Count(x=>x.accepted==1&&!x.fresh&&x.input==0));
    // Production lease must also restore when the caller aborts before nativecall.
    Cab(0x3d4,1);Call("ElectricalCycle");bool caught=false;
    try{using((IDisposable)typeof(TrainerIntegrationHost).GetMethod("BorrowNativeSandAirInput",Flags).Invoke(host,null)){if(Marshal.ReadByte(mem["TLocParam"],0x7f)!=1)throw new Exception("Exceptioncase no borrowedinput");throw new InvalidOperationException("declaredcallerabort");}}
    catch(InvalidOperationException){caught=true;}
    Check("Sand input restored after declared caller exception",caught&&Marshal.ReadByte(mem["TLocParam"],0x7f)==0,true,caught);
    Cab(0x3d4,0);Call("ElectricalCycle");
    // Explicit alternative native input, not hardware acceptance: OFF/ON AF must
    // preserve a valve position already supplied by another producer.
    var valve=IntPtr.Add(mem["TLocParam"],0x7f);byte saved=Marshal.ReadByte(valve);
    if(saved!=0)throw new Exception("Alternative input fixture baseline changed");
    Marshal.WriteByte(valve,1);
    try {
     using((IDisposable)typeof(TrainerIntegrationHost).GetMethod("BorrowNativeSandAirInput",Flags).Invoke(host,null))
      Check("Native OFF source preserves existing valve input",Marshal.ReadByte(valve)==1,1,Marshal.ReadByte(valve));
     Cab(0x3d4,1);Call("ElectricalCycle");
     using((IDisposable)typeof(TrainerIntegrationHost).GetMethod("BorrowNativeSandAirInput",Flags).Invoke(host,null))
      Check("Native ON source preserves existing valve ownership",Marshal.ReadByte(valve)==1&&!(bool)Field("nativeSandAirLastAssigned"),"input1/noownership",Field("nativeSandAirLastAssigned"));
     Check("Alternative valve survives scoped restoration",Marshal.ReadByte(valve)==1,1,Marshal.ReadByte(valve));
    }finally{if(Marshal.ReadByte(valve)==1)Marshal.WriteByte(valve,saved);Cab(0x3d4,0);Call("ElectricalCycle");}
    sandAirProof=new{passed=true,steps=sandAirRows.Count,rows=sandAirRows,command_to_air_input_reconstructed=true,native_consumption_law_verified=true,finite_pm_mass_decreased=true,
     sand_adhesion_credit=false,scope="Original physical cabSand input produces AF+A5, scoped reconstructed input7F feeds originalTorm4*actualDP consumer inKMIV andKMII. Main compressor OFF via nativecab; nativeLoc12 branch observed, PM/force/protection outputs never assigned by probe. Stale successful-source flag explicitly invalidated only for guardtest. No sand adhesion or exact equipment calibration."};
   }finally{beforeNativePneumatic=null;afterNativeMotion=null;sandAirStepField.SetValue(host,sandAirOriginalStep);Cab(0x3d4,0);}
  }
 }
 public static int RunNativeSandAir(){
  Application.EnableVisualStyles();thread=GetCurrentThreadId();Watch.Restart();Checks.Clear();Cases.Clear();var f=new Fixture();int exit=0;string error=null;
  try{f.Start();f.MeasureSandAir();}catch(Exception e){error=e.ToString();f.error=error;exit=1;Console.WriteLine(e);}
  finally{f.beforeNativePneumatic=null;f.afterNativeMotion=null;f.Close();if(f.error!=null||!f.closed||!f.restored){exit=1;error=f.error;}}
  File.WriteAllText(TrainerSession.ResultPath("Trainer_native_sand_air.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(new{passed=exit==0,error,checks=Checks,air=f.sandAirProof,closed=f.closed,restored=f.restored,full_avp_accepted=false}));
  Console.WriteLine(exit==0?"PASS original sandair consumer and finite PM":"FAIL native sandair coupling");return exit;
 }
}
