using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using TrainerAir;

// Executes the production managed air bridge with mock packed records.
// Never shows the host, invokes StartNative, or loads a trainer DLL.
class AirControlIsolationProbe {
 static readonly BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)]static extern IntPtr GetModuleHandle(string name);
 static int bridgeCycles,requestRestorationChecks,outputChecks;
 static void Assert(bool condition,string message){if(!condition)throw new Exception(message);}
 static void WriteFloat(IntPtr p,int offset,float value){Marshal.Copy(BitConverter.GetBytes(value),0,IntPtr.Add(p,offset),4);}
 static float ReadFloat(IntPtr p,int offset){var bytes=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),bytes,0,4);return BitConverter.ToSingle(bytes,0);}
 static double ReadDouble(IntPtr p,int offset){var bytes=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),bytes,0,8);return BitConverter.ToDouble(bytes,0);}
 static FieldInfo Field(string name){var field=typeof(TrainerIntegrationHost).GetField(name,flags);Assert(field!=null,"Missing host field "+name);return field;}
 static void Call(TrainerIntegrationHost host,string name){var method=typeof(TrainerIntegrationHost).GetMethod(name,flags);Assert(method!=null,"Missing production bridge method "+name);try{method.Invoke(host,null);}catch(TargetInvocationException error){throw error.InnerException??error;}}
 static void RequireNoTrainerModules(){foreach(var name in new[]{"Pult1.dll","Communication1.dll","TormModel.dll","POEZD.DLL"})Assert(GetModuleHandle(name)==IntPtr.Zero,"Fixture unexpectedly loaded "+name);}

 sealed class Fixture:IDisposable {
  public readonly TrainerIntegrationHost Host;
  public readonly TwoSectionAirChain Circuit;
  public readonly IntPtr Loc,Pult,FirstAutomatic,SecondAutomatic;
  readonly List<IntPtr> allocations=new List<IntPtr>();
  readonly byte[] compressorRequests={1,2,3,4,5,6,7,8};
  IntPtr Allocate(int bytes){var address=Marshal.AllocHGlobal(bytes);Marshal.Copy(new byte[bytes],0,address,bytes);allocations.Add(address);return address;}
  public Fixture(bool warmIndependent){
   RequireNoTrainerModules();
   Host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);
   // Constructor only: no Handle, Show, Application.Run, AllocateData or StartNative.
   Assert(!(bool)Field("physicsReady").GetValue(Host),"Constructor initialized native physics");
   Assert(!(bool)Field("commReady").GetValue(Host),"Constructor initialized Communication");
   Loc=Allocate(0x200);Pult=Allocate(0x300);FirstAutomatic=Allocate(0x200);SecondAutomatic=Allocate(0x200);
   var memory=(Dictionary<string,IntPtr>)Field("mem").GetValue(Host);
   memory["TLocParam"]=Loc;memory["TPult"]=Pult;
   Field("cars").SetValue(Host,new[]{FirstAutomatic});
   Field("secondBvr").SetValue(Host,SecondAutomatic);
   Circuit=new TwoSectionAirChain(new TwoSectionAirParameters(2000,100,1,20,1,1,1,12,6,.7,.001),8,5,8);
   Circuit.InitializeControlAir(5,5);Field("airCircuit").SetValue(Host,Circuit);
   if(warmIndependent){for(int n=0;n<300;n++)Circuit.StepControls(6,.1,true,false,true);Assert(Circuit.Independent.Gauge>3.8,"Independent warm-up failed");}
   RequireNoTrainerModules();
  }
  public void Cycle(int vcu,bool blocked,int kvt,double? automaticBoundaryPressure){
   Marshal.WriteByte(Loc,0x8f,(byte)vcu);Marshal.WriteByte(Loc,0x90,(byte)(blocked?1:0));Marshal.WriteByte(Loc,0x7e,(byte)kvt);
   Marshal.WriteByte(Loc,0x8c,0);Marshal.WriteByte(Loc,0x93,0);Marshal.WriteByte(Loc,0x12,0);
   Marshal.Copy(compressorRequests,0,IntPtr.Add(Loc,0x80),compressorRequests.Length);
   // Exercise the production temporary compressor-request mask and restoration.
   var delays=(double[])Field("compressorUnloadRemaining").GetValue(Host);delays[0]=.2;
   try{
    Call(Host,"BeforeNativeAirStep");
    Assert(Marshal.ReadByte(Loc,0x80)==0&&Marshal.ReadByte(Loc,0x84)==0,"Production request mask not exercised");
    if(automaticBoundaryPressure.HasValue){WriteFloat(FirstAutomatic,4,(float)automaticBoundaryPressure.Value);WriteFloat(SecondAutomatic,4,(float)automaticBoundaryPressure.Value);}
    // No native step: optional values above are explicit external BVR boundaries.
    Call(Host,"AfterNativeAirStep");bridgeCycles++;
   }finally{Call(Host,"RestoreNativeAirCompressorRequests");}
   for(int i=0;i<compressorRequests.Length;i++)Assert(Marshal.ReadByte(Loc,0x80+i)==compressorRequests[i],"Compressor request not restored at "+i);
   requestRestorationChecks++;
   Assert(Math.Abs(ReadDouble(Loc,0xec)-Circuit.Independent.Gauge)<1e-8,"Native impulse output does not follow physical state");
   Assert(Math.Abs(ReadFloat(Loc,13)-Circuit.Sections[0].Cylinder[0].Gauge)<1e-5,"Native TC output does not follow physical state");
   Assert(Circuit.MaximumBalanceError<=1e-8,"Managed circuit mass balance failed");outputChecks++;
  }
  public double MinimumCylinder(){double result=double.MaxValue;for(int s=0;s<2;s++)for(int b=0;b<2;b++)result=Math.Min(result,Circuit.Sections[s].Cylinder[b].Gauge);return result;}
  public double MaximumCylinder(){double result=0;for(int s=0;s<2;s++)for(int b=0;b<2;b++)result=Math.Max(result,Circuit.Sections[s].Cylinder[b].Gauge);return result;}
  public void Dispose(){if(Host!=null)Host.Dispose();foreach(var address in allocations)Marshal.FreeHGlobal(address);}
 }

 [STAThread]static int Main(){
  var rows=new List<object>();var failures=new List<string>();bool blockedGateDefect=false;
  try{
   Application.EnableVisualStyles();RequireNoTrainerModules();
   foreach(int vcu in new[]{1,2,3})using(var fixture=new Fixture(true)){
    bool blocked=vcu!=1;double initial=fixture.Circuit.Independent.Gauge,initialTc=fixture.MinimumCylinder();
    for(int n=0;n<300;n++)fixture.Cycle(vcu,blocked,2,null);
    double final=fixture.Circuit.Independent.Gauge,finalTc=fixture.MinimumCylinder();
    bool held=Math.Abs(final-initial)<=.01&&Math.Abs(finalTc-initialTc)<=.02;
    if(vcu==1){blockedGateDefect=!held;if(!held)failures.Add("VCU1/block0 released the isolated independent circuit");}
    else if(!held)failures.Add("VCU"+vcu+" failed to isolate independent control");
    rows.Add(new{scenario="isolated_independent",vcu=vcu,blocked=blocked,cycles=300,initial_independent=initial,final_independent=final,initial_min_tc=initialTc,final_min_tc=finalTc,held=held,max_balance_error=fixture.Circuit.MaximumBalanceError});
   }
   using(var fixture=new Fixture(true)){
    for(int n=0;n<300;n++)fixture.Cycle(1,true,2,null);
    bool released=fixture.Circuit.Independent.Gauge<=.01&&fixture.MaximumCylinder()<=.05;
    if(!released)failures.Add("Enabled KVT no longer releases the independent circuit");
    rows.Add(new{scenario="enabled_release",vcu=1,blocked=true,cycles=300,final_independent=fixture.Circuit.Independent.Gauge,final_max_tc=fixture.MaximumCylinder(),released=released,max_balance_error=fixture.Circuit.MaximumBalanceError});
   }
   using(var fixture=new Fixture(false)){
    double initialPm=fixture.Circuit.Pm.Gauge;
    for(int n=0;n<300;n++)fixture.Cycle(1,false,6,3.0);
    bool automaticWorks=fixture.Circuit.Independent.Gauge<=.01&&fixture.MinimumCylinder()>2.8&&fixture.Circuit.Pm.Gauge<initialPm;
    if(!automaticWorks)failures.Add("Isolated KVT interfered with automatic BVR/BTO path");
    rows.Add(new{scenario="automatic_bto_with_isolated_kvt",vcu=1,blocked=false,kvt=6,cycles=300,external_automatic_boundary_kgf=3.0,initial_pm=initialPm,final_pm=fixture.Circuit.Pm.Gauge,final_independent=fixture.Circuit.Independent.Gauge,final_min_tc=fixture.MinimumCylinder(),automatic_path_works=automaticWorks,max_balance_error=fixture.Circuit.MaximumBalanceError});
   }
   RequireNoTrainerModules();bool passed=failures.Count==0;
   File.WriteAllText(TrainerSession.ResultPath("Trainer_air_control_isolation_validation.json"),new JavaScriptSerializer().Serialize(new{passed=passed,blocked_gate_defect_reproduced=blockedGateDefect,conforms_to_isolation_requirement=passed,native_startup=false,native_dlls_loaded=false,actual_managed_bridge=true,bridge_cycles=bridgeCycles,compressor_request_restoration_checks=requestRestorationChecks,physical_output_checks=outputChecks,failures=failures,scope="Constructor-only host and mock packed records call production BeforeNativeAirStep/AfterNativeAirStep/RestoreNativeAirCompressorRequests. External automatic pilot pressure is an explicit diagnostic boundary; this verifies the managed bridge gate and continued BTO operation, not native startup, electrical actuation or RE calibration.",scenarios=rows}));
   Console.WriteLine((passed?"PASS":"FAIL")+" managed air-control isolation: "+bridgeCycles+" bridge cycles, "+requestRestorationChecks+" restored request sets");
   foreach(var failure in failures)Console.WriteLine(failure);return passed?0:1;
  }catch(Exception error){TrainerSession.RecordException("air-control-isolation",error);Console.WriteLine(error);return 1;}
 }
}
