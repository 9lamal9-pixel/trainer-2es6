using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class VcuAirSequenceProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
[DllImport("kernel32.dll")]static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint kind,uint protection);[DllImport("kernel32.dll")]static extern bool VirtualFree(IntPtr address,UIntPtr size,uint kind);[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void RegisterCall();
[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int NativeCall();
static int InvokeRegister(IntPtr self,int edx,IntPtr target){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)32,0x3000,0x40);if(p==IntPtr.Zero)throw new Exception("Thunk allocation");try{var b=new List<byte>();b.Add(0xb8);b.AddRange(BitConverter.GetBytes(self.ToInt32()));b.Add(0xba);b.AddRange(BitConverter.GetBytes(edx));b.Add(0xb9);b.AddRange(BitConverter.GetBytes(target.ToInt32()));b.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(b.ToArray(),0,p,b.Count);return ((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{VirtualFree(p,UIntPtr.Zero,0x8000);}}static void SetPosition(IntPtr self,IntPtr target,double value){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)64,0x3000,0x40);try{var b=new List<byte>();b.Add(0xb8);b.AddRange(BitConverter.GetBytes(self.ToInt32()));byte[] d=BitConverter.GetBytes(value);b.Add(0x68);b.AddRange(new byte[]{d[4],d[5],d[6],d[7]});b.Add(0x68);b.AddRange(new byte[]{d[0],d[1],d[2],d[3]});b.AddRange(new byte[]{0x6a,0,0x6a,0});b.Add(0xb9);b.AddRange(BitConverter.GetBytes(target.ToInt32()));b.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(b.ToArray(),0,p,b.Count);((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{VirtualFree(p,UIntPtr.Zero,0x8000);}}
 static BindingFlags f=BindingFlags.Instance|BindingFlags.NonPublic;
 static void Call(object h,string n){typeof(TrainerIntegrationHost).GetMethod(n,f).Invoke(h,null);}
 static float Read(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 [STAThread]static int Main(){Application.EnableVisualStyles();var h=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  var handle=h.Handle;Call(h,"StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",f).GetValue(h)).Stop();
  var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",f).GetValue(h);var loc=mem["TLocParam"];
  var chain=(TwoSectionAirChain)typeof(TrainerIntegrationHost).GetField("airCircuit",f).GetValue(h);
  var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);var form=Marshal.ReadIntPtr(cab,0xc);
  if(Marshal.ReadIntPtr(Marshal.ReadIntPtr(form,0x530),4)!=loc)throw new Exception("Cab layout guard");
  // Communication RVA2af4b0..2af4c5: form+3c8.state -> TPult+28.
  // form+3bc drives TPult.Pos_Revers(+81), not compressors.
  var control=Marshal.ReadIntPtr(form,0x3c8);if(control==IntPtr.Zero)throw new Exception("Compressor control absent");
  foreach(int field in new[]{0x3a0,0x3a4})Marshal.WriteByte(Marshal.ReadIntPtr(form,field),0x1d9,1); var rows=new List<object>();
  var pultModule=GetModuleHandle("Pult1.dll");if(pultModule==IntPtr.Zero)throw new Exception("Pult1 absent");
  var pultOwner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(pultModule,0x1a2e60));
  if(pultOwner==IntPtr.Zero||Marshal.ReadIntPtr(pultOwner,4)!=mem["TPult"])throw new Exception("Native electrical owner guard");
  var schema=IntPtr.Add(pultOwner,0x18);
  var communicationModule=GetModuleHandle("Communication1.dll");int auxiliaryHardwareId=Marshal.ReadInt32(Marshal.ReadIntPtr(communicationModule,0x2c74c0));if(auxiliaryHardwareId!=48)throw new Exception("Auxiliary hardware identifier mismatch");
  for(int i=0;i<100;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}
  var communication=GetModuleHandle("Communication1.dll");InvokeRegister(cab,1,IntPtr.Add(communication,0x2ae994));var tumba=Marshal.ReadIntPtr(cab,0x10);var tumbaForm=Marshal.ReadIntPtr(tumba,0xc);var vcu=Marshal.ReadIntPtr(tumbaForm,0x3a4);
  var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",f).GetValue(h);
  Call(h,"UpdateVcuInterlock");SetPosition(vcu,IntPtr.Add(communication,0x2a4944),3);
  if(Marshal.ReadByte(loc,0x8f)!=1)throw new Exception("Unsafe key removal accepted");
  typeof(TrainerIntegrationHost).GetMethod("TestBrake",f).Invoke(h,new object[]{6});int chargingCycles=0;
  do{Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Marshal.WriteByte(loc,0x7e,6);Call(h,"PneumaticCycle");chargingCycles++;}while((Read(cars[0],0)>=.5||Read(cars[75],0)>=.5||chain.Independent.Gauge<=3)&&chargingCycles<4000);
  Console.WriteLine("Emergency drain: head="+Read(cars[0],0)+" tail="+Read(cars[75],0));if(chargingCycles>=4000)throw new Exception("Real TM/IM key conditions not reached");
  // Current user consist is composite R2/AR1. The former R1/4.0..4.5 check
  // described another configuration. This VCU probe verifies application and
  // key interlocks; it does not calibrate AR pressure against wagon load.
  double minWagonTc=double.MaxValue,maxWagonTc=0;for(int w=1;w<76;w++){
   if(Marshal.ReadByte(cars[w],0x18)!=2||Marshal.ReadByte(cars[w],0x1a)!=1||Marshal.ReadInt32(cars[w],0x25)!=80)throw new Exception("Composite R2/AR1 80t configuration mismatch");
   double tc=Read(cars[w],4);if(double.IsNaN(tc)||double.IsInfinity(tc))throw new Exception("Non-finite wagon brake pressure");minWagonTc=Math.Min(minWagonTc,tc);maxWagonTc=Math.Max(maxWagonTc,tc);
  }
  if(minWagonTc<.5||maxWagonTc>4.5)throw new Exception("Wagon application diagnostic bound failed (not normative AR calibration): "+minWagonTc+"/"+maxWagonTc);double imBefore=chain.Independent.Gauge,tmBefore=Read(cars[0],0);
  SetPosition(vcu,IntPtr.Add(communication,0x2a4944),2);
  for(int i=0;i<200;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Marshal.WriteByte(loc,0x7e,2);Call(h,"PneumaticCycle");}
  double imAfter=chain.Independent.Gauge;
  if(imAfter<imBefore-.01)throw new Exception("Inactive VCU2 independent brake pilot exhausted by KVT release");
  Call(h,"UpdateVcuInterlock");SetPosition(vcu,IntPtr.Add(communication,0x2a4944),3);for(int i=0;i<100;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Marshal.WriteByte(loc,0x7e,2);Call(h,"PneumaticCycle");}
  double imKeyRemoved=chain.Independent.Gauge;double tcKeyRemoved=chain.Sections[0].Cylinder[0].Gauge;Console.WriteLine("VCU3 diagnostic: vcu="+Marshal.ReadByte(loc,0x8f)+" key="+Marshal.ReadByte(schema,0x37)+" IM="+imKeyRemoved+" TC="+tcKeyRemoved+" TM="+Read(cars[0],0));if(Marshal.ReadByte(loc,0x8f)!=3||Marshal.ReadByte(schema,0x37)!=0||imKeyRemoved<imBefore-.01||tcKeyRemoved<3)throw new Exception("Actual key removal did not preserve braking");
  SetPosition(vcu,IntPtr.Add(communication,0x2a4944),1);for(int i=0;i<200;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Marshal.WriteByte(loc,0x7e,2);Call(h,"PneumaticCycle");}if(chain.Independent.Gauge>.01)throw new Exception("Working KVT release failed after VCU return");
  var result=new{composite_R2_AR1_80t_wagons_verified=75,wagon_application_diagnostic_only=true,normative_AR_pressure_accepted=false,min_observed_wagon_tc=minWagonTc,max_observed_wagon_tc=maxWagonTc,im_after_key_removed=imKeyRemoved,tc_after_key_removed=tcKeyRemoved,im_after_working_release=chain.Independent.Gauge,safe_initial_key_blocked=true,charging_cycles=chargingCycles,tm_before_off=tmBefore,im_before_off=imBefore,im_after_20_seconds_vcu2=imAfter,vcu=Marshal.ReadByte(loc,0x8f),candidate_only=true,scope="Actual native VCU SetPosition/event on current composite R2/AR1 80t consist; KM emergency and KVT6/2 are diagnostic control inputs; pressures generated by pneumatic network, no pressure fixtures. All75 wagon applications checked by broad0.5..4.5 diagnostic bound; normative AR/load response is NOT accepted by this probe."};
  string text=new JavaScriptSerializer().Serialize(result);File.WriteAllText(TrainerSession.ResultPath("Trainer_vcu_air_sequence.json"),text);Console.WriteLine(text);
  if(imAfter<imBefore-.01)throw new Exception("Inactive VCU2 independent brake pilot exhausted by KVT release");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}

















