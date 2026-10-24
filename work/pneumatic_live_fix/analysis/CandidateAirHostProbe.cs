using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class CandidateAirHostProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
 [DllImport("kernel32.dll")]static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint allocation,uint protection);
 [DllImport("kernel32.dll")]static extern bool VirtualFree(IntPtr address,UIntPtr size,uint type);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int RegisterCall();
 static void InstantiateNativeTumba(IntPtr cab){var module=GetModuleHandle("Communication1.dll");if(module==IntPtr.Zero)throw new Exception("Native cab module missing");var target=IntPtr.Add(module,0x2ae994);
  var thunk=VirtualAlloc(IntPtr.Zero,(UIntPtr)32,0x3000,0x40);if(thunk==IntPtr.Zero)throw new Exception("Native cab call allocation");
  try{var bytes=new List<byte>();bytes.Add(0xb8);bytes.AddRange(BitConverter.GetBytes(cab.ToInt32()));bytes.Add(0xba);bytes.AddRange(BitConverter.GetBytes(1));bytes.Add(0xb9);bytes.AddRange(BitConverter.GetBytes(target.ToInt32()));bytes.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(bytes.ToArray(),0,thunk,bytes.Count);((RegisterCall)Marshal.GetDelegateForFunctionPointer(thunk,typeof(RegisterCall)))();}finally{VirtualFree(thunk,UIntPtr.Zero,0x8000);}
 }
 [StructLayout(LayoutKind.Sequential)]struct MemoryInfo {public IntPtr Base,Allocation;public uint AllocationProtect,Size,State,Protect,Type;}
 [DllImport("kernel32.dll")]static extern UIntPtr VirtualQuery(IntPtr address,out MemoryInfo info,UIntPtr length);
 static IntPtr CheckedPtr(IntPtr p,int offset){var address=IntPtr.Add(p,offset);MemoryInfo info;
  if(p==IntPtr.Zero||VirtualQuery(address,out info,(UIntPtr)28)==UIntPtr.Zero||info.State!=0x1000||(info.Protect&0x101)!=0)throw new Exception("Unreadable native pointer at "+p.ToString("x")+"+"+offset.ToString("x"));
  return Marshal.ReadIntPtr(p,offset);
 }
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;static TrainerIntegrationHost host;static IntPtr loc;static TwoSectionAirChain circuit;
 static int cycles;static List<object> rows=new List<object>();static IntPtr releaseControl;static bool releaseButton;static int releaseSignalChecks;
 static IntPtr tc1Arrow,tc2Arrow,pmArrow,tmArrow,poezdModule;static double maximumGaugeError;
 static double ReadDouble(IntPtr p,int offset){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,null);}
 static float Read(IntPtr p,int offset){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
 static void Cycle(int km,int kvt){
  typeof(TrainerIntegrationHost).GetField("commandedKm",flags).SetValue(host,(int?)km);
  // Set the native control's state as a diagnostic input, then let Communication produce OtpTorm itself.
  Marshal.WriteByte(releaseControl,0x1d9,(byte)(releaseButton?2:0));
  double expectedPm=Read(loc,5);double expectedTc1=circuit.Sections[0].Cylinder[0].Gauge,expectedTc2=circuit.Sections[0].Cylinder[1].Gauge;
  Call("ElectricalCycle");Call("ApplyTestInputs");
  maximumGaugeError=Math.Max(maximumGaugeError,Math.Max(Math.Abs(ReadDouble(tc1Arrow,0x28)-expectedTc1),Math.Abs(ReadDouble(tc2Arrow,0x28)-expectedTc2)));
  if(Math.Abs(ReadDouble(pmArrow,0x28)-expectedPm)>1e-5)throw new Exception("Native PM gauge differs from physical PM");
  if(maximumGaugeError>1e-5)throw new Exception("Native TC gauge source mismatch: "+maximumGaugeError);
  if(Marshal.ReadByte(loc,0x8c)!=(releaseButton?2:0))throw new Exception("Native release-button output mismatch");releaseSignalChecks++;
  // Explicit test inputs after electrical processing; these do not validate the native handle widgets.
  Marshal.WriteByte(loc,0x7e,(byte)kvt);for(int i=0x80;i<=0x87;i++)Marshal.WriteByte(loc,i,0);
  Call("PneumaticCycle");Call("MotionCycle");double pressureSum=0;for(int s=0;s<2;s++)for(int b=0;b<2;b++)pressureSum+=Math.Max(0,circuit.Sections[s].Cylinder[b].Gauge);if(Math.Abs(Read(poezdModule,0x1a674)-pressureSum*4)>1e-5)throw new Exception("Native motion brake input ignores bogie pressure");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");
  ((Delegate)typeof(TrainerIntegrationHost).GetField("klubStep",flags).GetValue(host)).DynamicInvoke();
  int tick=(int)typeof(TrainerIntegrationHost).GetField("ticks",flags).GetValue(host);typeof(TrainerIntegrationHost).GetField("ticks",flags).SetValue(host,tick+1);
  if(Math.Abs(Read(loc,13)-circuit.Sections[0].Cylinder[0].Gauge)>1e-5)throw new Exception("Host TC does not use physical air circuit");
  cycles++;if(cycles%100==0)rows.Add(new{seconds=cycles*.1,km=km,kvt=kvt,pm=Read(loc,5),ur=Read(loc,9),tc1=circuit.Sections[0].Cylinder[0].Gauge,tc2=circuit.Sections[0].Cylinder[1].Gauge,tc3=circuit.Sections[1].Cylinder[0].Gauge,tc4=circuit.Sections[1].Cylinder[1].Gauge});
 }
 static void Phase(int count,int km,int kvt){for(int n=0;n<count;n++)Cycle(km,kvt);}
 [STAThread]static int Main(string[] args){bool baseline=args.Length>0&&args[0]=="baseline";Application.EnableVisualStyles();host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  var handle=host.Handle;Call("StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host)).Stop();
  var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);loc=mem["TLocParam"];
  poezdModule=GetModuleHandle("POEZD.DLL");if(poezdModule==IntPtr.Zero)throw new Exception("Motion module absent");
  circuit=(TwoSectionAirChain)typeof(TrainerIntegrationHost).GetField("airCircuit",flags).GetValue(host);
  var cab=CheckedPtr(mem["CommOwner"],0x2bc);InstantiateNativeTumba(cab);var tumba=CheckedPtr(cab,0x10);var form=CheckedPtr(cab,0xc);
  Console.WriteLine("Native cab pointers: cab="+cab.ToString("x")+" tumba="+tumba.ToString("x")+" form="+form.ToString("x")+" +530="+CheckedPtr(form,0x530).ToString("x"));
  if(CheckedPtr(CheckedPtr(form,0x530),4)!=loc)throw new Exception("Main cab release wiring layout guard");
  releaseControl=CheckedPtr(form,0x3e4);if(releaseControl==IntPtr.Zero)throw new Exception("Native release control missing");
  pmArrow=CheckedPtr(CheckedPtr(form,0x398),0x214);tmArrow=CheckedPtr(CheckedPtr(form,0x398),0x1f8);var tcGauge=CheckedPtr(form,0x39c);tc1Arrow=CheckedPtr(tcGauge,0x1f8);tc2Arrow=CheckedPtr(tcGauge,0x214);
  Phase(1000,2,2);int n=0;while(Read(loc,9)>4.5&&n++<100)Cycle(5,2);if(n>=100)throw new Exception("KM service step not reached");
  Phase(600,4,2);double automatic=circuit.Sections[0].Cylinder[0].Gauge,automatic2=circuit.Sections[1].Cylinder[0].Gauge;
  Phase(300,4,baseline?2:6);double combined=circuit.Sections[0].Cylinder[0].Gauge,combined2=circuit.Sections[1].Cylinder[0].Gauge;
  Phase(300,4,2);double retained=circuit.Sections[0].Cylinder[0].Gauge,retained2=circuit.Sections[1].Cylinder[0].Gauge;
  releaseButton=true;Phase(300,4,2);double buttonReleased=circuit.Sections[0].Cylinder[0].Gauge,buttonReleased2=circuit.Sections[1].Cylinder[1].Gauge;
  Phase(300,4,6);double buttonIndependent=circuit.Sections[0].Cylinder[0].Gauge;
  Phase(300,4,2);releaseButton=false;Phase(300,4,2);double buttonRestored=circuit.Sections[0].Cylinder[0].Gauge;
  if(buttonReleased>.05||buttonReleased2>.05||buttonIndependent<3.8||buttonRestored<.5)throw new Exception("Native release button/independent/restore scenario");
  Phase(600,2,2);double released=circuit.Sections[0].Cylinder[0].Gauge,released2=circuit.Sections[1].Cylinder[0].Gauge;
  if(automatic<.5||automatic2<.5||(!baseline&&(combined<3.8||combined2<3.8))||retained<.5||retained2<.5||released>.05||released2>.05)throw new Exception("Host automatic/independent/release scenario");
  Phase(300,2,6);double allBogiesInput=Read(poezdModule,0x1a674),allBogiesForce=Math.Abs(Read(poezdModule,0x1d0ac));
  circuit.Sections[0].DisconnectBogie(2);Phase(300,2,6);
  double isolated=circuit.Sections[0].Cylinder[1].Gauge,working=circuit.Sections[0].Cylinder[0].Gauge;
  if(isolated>.05||working<3.8)throw new Exception("Separate bogie pressure test failed");
  double isolatedInput=Read(poezdModule,0x1a674),isolatedForce=Math.Abs(Read(poezdModule,0x1d0ac));
  if(isolatedInput>=allBogiesInput||isolatedForce>=allBogiesForce)throw new Exception("Motion braking did not decrease after bogie isolation");
  Phase(300,2,2);
  int faults=(int)typeof(TrainerIntegrationHost).GetField("electricalFaults",flags).GetValue(host);if(faults!=0)throw new Exception("Electrical model faults");
  var result=new{passed=true,baseline=baseline,candidate_host=true,default_host_updated=false,calibrated_to_RE=false,cycles=cycles,electrical_faults=faults,final_pm=circuit.Pm.Gauge,automatic_tc=automatic,second_automatic_tc=automatic2,combined_tc=combined,second_combined_tc=combined2,retained_tc=retained,second_retained_tc=retained2,released_tc=released,second_released_tc=released2,button_released_tc=buttonReleased,button_second_released_tc=buttonReleased2,button_independent_tc=buttonIndependent,button_restored_tc=buttonRestored,native_button_signal_checks=releaseSignalChecks,all_bogies_native_input=allBogiesInput,isolated_native_input=isolatedInput,all_bogies_native_brake=allBogiesForce,isolated_native_brake=isolatedForce,tc_gauge_max_error=maximumGaugeError,isolated_bogie_tc=isolated,working_bogie_tc=working,native_pm_gauge_checks=cycles,rendered_pixels_verified=false,max_boundary_mass_error=circuit.MaximumBalanceError,scope="Candidate host electrical/motion/route/aux/KLUB/pneumatic steps; native button state set through memory as diagnostic input, native output checked every cycle; stand KM/KVT inputs and compressors off. UI clicks, KEB actuator air/thresholds, TC gauge objects checked; rendered pixels, geometry, KRU/EPK flows not accepted.",rows=rows};
  File.WriteAllText(TrainerSession.ResultPath(baseline?"Trainer_air_candidate_host_baseline.json":"Trainer_air_candidate_host_validation.json"),new JavaScriptSerializer().Serialize(result));
  Console.WriteLine("PASS candidate host: "+cycles+" cycles,PM="+circuit.Pm.Gauge+", TC="+combined+"/"+combined2+", faults="+faults);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{host.Close();host.Dispose();}}
}







