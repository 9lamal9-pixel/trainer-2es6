using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Actual candidate host and 77 native air nodes. Only stand KM/KVT inputs and
// end-cock/sleeve states change; no pressure reset, new hook or repeated InitNew.
class TrainIsolationProbe {
 static readonly BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static readonly List<object> snapshots=new List<object>();
 static readonly List<string> failures=new List<string>();
 const int split=37,window=600;
 // TVagonSend.State+0x18: KKS+8 is TKonKranState; RC+0xA is TRukavConnect.
 const int frontCockOffset=0x20,rearCockOffset=0x21,frontSleeveOffset=0x22,rearSleeveOffset=0x23;
 static TrainerIntegrationHost host;
 static Dictionary<string,IntPtr> mem;
 static IntPtr loc,secondBvr,trainData;
 static IntPtr[] cars;
 static byte[] savedBits;
 static string phase="startup";
 static int cycles,phaseCycles,pressureChecks,progressSequence;
 static double pzar,numericalAllowance;
 static bool started,layoutVerified,restored,passed;
 static double[] baselineLoss,emergencyLoss,connectedLoss,disconnectedLoss,ruptureLoss;
 static void Progress(string checkpoint){
  // Integers/bools/strings only: no pressure math or snapshot graph. Flush each
  // checkpoint before the next native/managed group so a fatal exception leaves
  // a fresh compact location even when normal exception handling cannot run.
  progressSequence++;Console.WriteLine("PROGRESS "+phase+" "+checkpoint+" cycles="+cycles+" phase_cycles="+phaseCycles);Console.Out.Flush();
  var state=new{passed=false,in_progress=true,phase=phase,checkpoint=checkpoint,sequence=progressSequence,total_cycles=cycles,phase_cycles=phaseCycles,native_started=started,layout_verified=layoutVerified,boundary_bits_restored=restored,pressure_checks=pressureChecks,failure_reasons=failures.ToArray()};
  File.WriteAllText(TrainerSession.ResultPath("Trainer_train_isolation_progress.json"),new JavaScriptSerializer().Serialize(state));
 }
 static object Field(string name){var f=typeof(TrainerIntegrationHost).GetField(name,flags);if(f==null)throw new Exception("Missing field "+name);return f.GetValue(host);}
 static void Call(string name){var m=typeof(TrainerIntegrationHost).GetMethod(name,flags);if(m==null)throw new Exception("Missing method "+name);m.Invoke(host,null);}
 static float Single(IntPtr p,int offset){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
 static double Double(IntPtr p,int offset){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
 static bool Finite(double v){return !double.IsNaN(v)&&!double.IsInfinity(v);}
 static double[] Vector(int offset){var result=new double[75];for(int i=1;i<76;i++)result[i-1]=Single(cars[i],offset);return result;}
 static double? SafePressure(IntPtr p,int offset){if(p==IntPtr.Zero)return null;double v=Single(p,offset);return Finite(v)?(double?)v:null;}
 static double?[] SafeVector(int offset){if(cars==null||cars.Length!=76)return null;var result=new double?[75];for(int i=1;i<76;i++)result[i-1]=SafePressure(cars[i],offset);return result;}
 static double Min(double[] v,int first,int last){double r=v[first-1];for(int i=first+1;i<=last;i++)r=Math.Min(r,v[i-1]);return r;}
 static double Max(double[] v,int first,int last){double r=v[first-1];for(int i=first+1;i<=last;i++)r=Math.Max(r,v[i-1]);return r;}
 static bool ChargeReady(){
  // Diagnostic convergence only: the head approaches the actual regulator
  // setpoint; the other wagons converge to that observed head. The 0.15
  // tolerances are not a normative RE limit for tail TM or reservoir pressure.
  double head=Single(cars[1],0);if(Math.Abs(head-pzar)>.15||Single(loc,9)<pzar-.15)return false;
  for(int i=1;i<76;i++){double tm=Single(cars[i],0);if(Math.Abs(tm-head)>.15||Single(cars[i],8)<tm-.15||Single(cars[i],4)>=.05)return false;}
  return true;
 }
 static double[] Loss(double[] before,double[] after){var result=new double[75];for(int i=0;i<75;i++)result[i]=before[i]-after[i];return result;}
 static byte[] Bits(){if(cars==null||cars.Length<=split+1||cars[split]==IntPtr.Zero||cars[split+1]==IntPtr.Zero)return null;return new[]{Marshal.ReadByte(cars[split],rearCockOffset),Marshal.ReadByte(cars[split+1],frontCockOffset),Marshal.ReadByte(cars[split],rearSleeveOffset),Marshal.ReadByte(cars[split+1],frontSleeveOffset)};}
 static void SetBits(byte rearCock,byte frontCock,byte rearSleeve,byte frontSleeve){Marshal.WriteByte(cars[split],rearCockOffset,rearCock);Marshal.WriteByte(cars[split+1],frontCockOffset,frontCock);Marshal.WriteByte(cars[split],rearSleeveOffset,rearSleeve);Marshal.WriteByte(cars[split+1],frontSleeveOffset,frontSleeve);}
 static void Snapshot(string label){
  Progress("snapshot_begin_"+label);var row=new Dictionary<string,object>();row["phase"]=phase;row["label"]=label;row["cycles"]=cycles;row["phase_cycles"]=phaseCycles;
  Progress("snapshot_before_seconds");row["seconds"]=cycles*.1;
  Progress("snapshot_before_tm");row["tm"]=SafeVector(0);
  Progress("snapshot_before_tc");row["tc"]=SafeVector(4);
  Progress("snapshot_before_zr");row["zr"]=SafeVector(8);
  Progress("snapshot_before_boundary_bits");row["boundary_bits"]=Bits();
  Progress("snapshot_before_pm");row["pm"]=SafePressure(loc,5);
  Progress("snapshot_before_ur");row["ur"]=SafePressure(loc,9);
  Progress("snapshot_before_km");row["km"]=loc==IntPtr.Zero?(byte?)null:Marshal.ReadByte(loc,0x7d);
  snapshots.Add(row);Progress("snapshot_complete");
 }
 static void Save(){var result=new{passed=passed&&failures.Count==0,failure_reasons=failures,phase=phase,native_started=started,layout_verified=layoutVerified,boundary_bits_restored=restored,air_nodes=77,wagons=75,split_after_wagon=split,charging_setpoint=Finite(pzar)?(double?)pzar:null,cycles=cycles,wagon_pressure_checks=pressureChecks,matched_window_cycles=window,comparison_roundoff_allowance=numericalAllowance,baseline_rear_loss=baselineLoss,emergency_rear_loss=emergencyLoss,connected_rear_loss=connectedLoss,disconnected_rear_loss=disconnectedLoss,open_disconnected_rear_loss=ruptureLoss,snapshots=snapshots,scope="Actual candidate77-node prepared network, native finite initialPM supply and stand KM/KVT. Charging readiness is diagnostic convergence: head TM within0.15ofactualPzar, every wagon TM within0.15ofobservedhead, ZR at least ownTM-0.15 andTC<0.05; these are not normative RE tail-pressure limits. Closed paired cocks: measure rear natural decay, front emergency with rear isolation. Matched KM4 windows compare connected versus disconnected sleeves while cocks stay closed; then open both disconnected ends and check all rear wagons brake. Every75wagonTM/TC/ZR finite/nonnegative with1e-5 numerical tolerance. No PM reset, output forcing, InitNew repeat, extra native hook, normative timing or global mass acceptance. Roundoff allowance=64 Single ULPs at max(1,Pzar) per native host cycle; physical leak allowance is measured.",};File.WriteAllText(TrainerSession.ResultPath("Trainer_train_isolation_validation.json"),new JavaScriptSerializer().Serialize(result));}
 static void Require(bool ok,string reason){if(ok)return;failures.Add(reason);passed=false;Snapshot("assertion_failure");Save();throw new Exception(reason);}
 static void Select(int km){phaseCycles=0;typeof(TrainerIntegrationHost).GetMethod("TestBrake",flags).Invoke(host,new object[]{km});}
 static void Step(){bool checkpoint=phaseCycles==0||phaseCycles%1000==0;if(checkpoint)Progress("step_before_electrical");Call("ElectricalCycle");if(checkpoint)Progress("step_after_electrical");Call("ApplyTestInputs");Marshal.WriteByte(loc,0x7e,2);if(checkpoint)Progress("step_before_pneumatic");Call("PneumaticCycle");if(checkpoint)Progress("step_after_pneumatic");cycles++;phaseCycles++;if(checkpoint)Progress("step_before_pressure_checks");for(int i=1;i<76;i++)foreach(int offset in new[]{0,4,8}){double value=Single(cars[i],offset);pressureChecks++;Require(Finite(value)&&value>=-1e-5,"Invalid wagon pressure: phase="+phase+" wagon="+i+" offset="+offset+" value="+value);}foreach(int offset in new[]{5,9,13})Require(Finite(Single(loc,offset))&&Single(loc,offset)>=-1e-5,"Invalid locomotive pressure "+offset);if(checkpoint)Progress("step_complete");if(phaseCycles%1000==0){Snapshot("progress");Save();Progress("bounded_snapshot_saved");}}
 static void RunWindow(){for(int i=0;i<window;i++)Step();}
 static void CheckLoss(double[] loss,double[] reference,string label){for(int i=split+1;i<76;i++)Require(loss[i-1]<=Math.Max(0,reference[i-1])+numericalAllowance,label+": extra rear TM loss at wagon "+i+" observed="+loss[i-1]+" matched="+reference[i-1]);}
 static void Layout(){Progress("layout_begin");Require(cars!=null&&cars.Length==76,"Expected76 motion records");Require(Marshal.ReadInt32(trainData,0)==77,"Expected77 air records");var array=Marshal.ReadIntPtr(trainData,0x42);var unique=new HashSet<IntPtr>();for(int i=0;i<77;i++){var air=Marshal.ReadIntPtr(array,i*8+4);Require(air!=IntPtr.Zero&&unique.Add(air),"Duplicate/null air record "+i);var expected=i==0?cars[0]:i==1?secondBvr:cars[i-1];Require(air==expected,"Air-to-motion record order mismatch "+i);Require(Marshal.ReadIntPtr(array,i*8)==(i==0?loc:IntPtr.Zero),"Unexpected locomotive pointer "+i);}Progress("layout_before_connectivity");Require(Marshal.ReadByte(cars[0],frontCockOffset)==0&&Marshal.ReadByte(cars[75],rearCockOffset)==0,"Exterior cocks not closed");for(int i=1;i<76;i++)Require(Marshal.ReadByte(cars[i],frontCockOffset)==1&&Marshal.ReadByte(cars[i],frontSleeveOffset)==1&&(i==75||(Marshal.ReadByte(cars[i],rearCockOffset)==1&&Marshal.ReadByte(cars[i],rearSleeveOffset)==1)),"Initial wagon connectivity mismatch "+i);savedBits=Bits();Require(savedBits[0]==1&&savedBits[1]==1&&savedBits[2]==1&&savedBits[3]==1,"Split boundary not initially open/connected");layoutVerified=true;Progress("layout_complete");}
 [STAThread]static int Main(){TrainerSession.EnsureStarted();Progress("session_started");try{
  Progress("before_visual_styles");Application.EnableVisualStyles();Progress("before_host_constructor");host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);Progress("before_host_handle");var handle=host.Handle;Progress("before_start_native");Call("StartNative");Progress("after_start_native");var timer=(Timer)Field("timer");if(timer!=null)timer.Stop();Require((bool)Field("physicsReady"),"Native initialization did not reach physicsReady");started=true;Progress("native_ready");
  mem=(Dictionary<string,IntPtr>)Field("mem");loc=mem["TLocParam"];cars=(IntPtr[])Field("cars");secondBvr=(IntPtr)Field("secondBvr");trainData=(IntPtr)Field("trainData");Progress("memory_fields_ready");Layout();Progress("before_pzar_read");pzar=Double(loc,0x13);Progress("after_pzar_read_before_validation");Require(Finite(pzar)&&pzar>0,"Invalid default charging setpoint");Progress("before_roundoff_reference");float reference=(float)Math.Max(1,pzar);Progress("before_roundoff_bits");int bits=BitConverter.ToInt32(BitConverter.GetBytes(reference),0);Progress("before_roundoff_ulp");double ulp=BitConverter.ToSingle(BitConverter.GetBytes(bits+1),0)-reference;Progress("before_roundoff_allowance");numericalAllowance=64*ulp*window;Progress("roundoff_allowance_ready");
  // Turn the native cab main-compressor command off. Electrical outputs are
  // generated normally: prepared PM remains finite and is never replenished.
  Progress("before_compressor_command_off");var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);Require(cab!=IntPtr.Zero,"Cab absent");var form=Marshal.ReadIntPtr(cab,0xc);Require(form!=IntPtr.Zero,"Cab form absent");var main=Marshal.ReadIntPtr(form,0x3c8);Require(main!=IntPtr.Zero,"Main-compressor command absent");Marshal.WriteByte(main,0x1d9,0);Progress("compressor_command_off");
  phase="charge_ready";Select(2);Snapshot("start");Progress("charge_ready_before_first_step");int ready=0;for(;ready<8000;ready++){Step();if(ready>=100&&ChargeReady())break;}Snapshot("end");Require(ready<8000,"Prepared finite supply did not converge all75wagon TM/ZR and release TC within diagnostic cap");for(int i=0x80;i<=0x87;i++)Require(Marshal.ReadByte(loc,i)==0,"Native compressor remained requested despite OFF command");
  SetBits(0,0,1,1);phase="closed_connected_leak_baseline";Select(2);var before=Vector(0);Snapshot("start");RunWindow();baselineLoss=Loss(before,Vector(0));double baselineRearTc=Max(Vector(4),split+1,75);Snapshot("end");
  phase="closed_connected_front_emergency";Select(6);before=Vector(0);Snapshot("start");RunWindow();var after=Vector(0);emergencyLoss=Loss(before,after);Snapshot("end");Require(Max(after,1,split)<Min(before,1,split)-.5,"Front segment did not lose TM under emergency");Require(Min(Vector(4),1,split)>.2,"Not all front wagons braked");CheckLoss(emergencyLoss,baselineLoss,"Closed cocks did not isolate rear from emergency");Require(Max(Vector(4),split+1,75)<=baselineRearTc+numericalAllowance,"Rear brakes changed beyond measured closed-cock baseline");
  phase="closed_connected_matched";Select(4);before=Vector(0);Snapshot("start");RunWindow();connectedLoss=Loss(before,Vector(0));Snapshot("end");
  SetBits(0,0,0,0);phase="closed_disconnected_matched";Select(4);before=Vector(0);Snapshot("start");RunWindow();disconnectedLoss=Loss(before,Vector(0));Snapshot("end");CheckLoss(disconnectedLoss,connectedLoss,"Closed disconnected sleeves created extra rear exhaust");
  SetBits(1,1,0,0);phase="open_disconnected_rupture";Select(4);before=Vector(0);var beforeTc=Vector(4);Snapshot("start");RunWindow();after=Vector(0);var afterTc=Vector(4);ruptureLoss=Loss(before,after);Snapshot("end");for(int i=split+1;i<76;i++){Require(ruptureLoss[i-1]>Math.Max(0,disconnectedLoss[i-1])+.5,"Open disconnected sleeve did not vent rear wagon "+i);Require(afterTc[i-1]>beforeTc[i-1]+.2,"Open disconnected sleeve did not brake rear wagon "+i);}passed=true;
 }catch(Exception e){passed=false;string reason=e.GetBaseException().Message;if(!failures.Contains(reason))failures.Add(reason);Progress("catch_before_snapshot");try{Snapshot("exception");Progress("catch_before_save");Save();Progress("catch_after_save");}catch(Exception saveError){Console.WriteLine("Result save failure: "+saveError.Message);Console.Out.Flush();}Console.WriteLine(e);Console.Out.Flush();}finally{
  Progress("finally_before_restoration");
  if(savedBits!=null&&cars!=null){try{SetBits(savedBits[0],savedBits[1],savedBits[2],savedBits[3]);restored=true;Snapshot("restored");}catch(Exception e){passed=false;failures.Add("Boundary restoration failed: "+e.Message);}}
  Progress("finally_before_save");try{Save();Progress("finally_after_save");}catch(Exception e){passed=false;Console.WriteLine("Result save failure: "+e.Message);Console.Out.Flush();}if(host!=null){try{Progress("before_host_close");host.Close();host.Dispose();Progress("after_host_close");}catch(Exception e){passed=false;failures.Add("Host close failed: "+e.Message);try{Save();}catch{}}}TrainerSession.Complete("train-isolation-finally");
 }Console.WriteLine((passed&&failures.Count==0?"PASS":"FAIL")+" TrainIsolation cycles="+cycles+" pressure_checks="+pressureChecks);Console.Out.Flush();return passed&&failures.Count==0?0:1;}
}
