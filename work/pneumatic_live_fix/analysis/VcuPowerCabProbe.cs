using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class VcuPowerCabProbe {
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
  var samples=new List<object>();
  foreach(int position in new[]{1,2,1,3}){
   // Explicit pressure fixture permits physical native position3. No pneumatic cycle runs during this fixture.
   if(position==3){var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",f).GetValue(h);Marshal.Copy(BitConverter.GetBytes(.7f),0,cars[0],4);Marshal.Copy(BitConverter.GetBytes(3.5d),0,IntPtr.Add(loc,0xec),8);Call(h,"UpdateVcuInterlock");}
   SetPosition(vcu,IntPtr.Add(communication,0x2a4944),position);
   for(int i=0;i<100;i++)Call(h,"ElectricalCycle");
   if(Marshal.ReadByte(loc,0x8f)!=position)throw new Exception("Native VCU event did not accept position");
   int expectedPower=position==3?0:1;if(Marshal.ReadByte(schema,0x37)!=expectedPower||Marshal.ReadByte(schema,0x1aa)!=expectedPower||Marshal.ReadByte(schema,0x1b6)!=expectedPower)throw new Exception("VCU power state mismatch");
   double beforeSource=chain.ControlAir[0].AuxiliarySourceMass;Marshal.WriteByte(mem["TPult"],0x46,1);Call(h,"StepHostControlAir");double sourceDelta=chain.ControlAir[0].AuxiliarySourceMass-beforeSource;if((position==3&&sourceDelta!=0)||(position!=3&&sourceDelta<=0))throw new Exception("Auxiliary source VCU power mismatch");
   samples.Add(new{position=position,auxiliary_source_delta=sourceDelta,key=Marshal.ReadByte(schema,0x37),km10=Marshal.ReadByte(schema,0x1aa),km11=Marshal.ReadByte(schema,0x1b6),contacts=new byte[]{Marshal.ReadByte(schema,0x1ae),Marshal.ReadByte(schema,0x1b2),Marshal.ReadByte(schema,0x1ba),Marshal.ReadByte(schema,0x1be)}});
  }
  string text=new JavaScriptSerializer().Serialize(new{passed=true,samples=samples,scope="Native SetPosition/event,100 electrical cycles per phase; position3 allowed by explicit TM0.7/IM3.5 fixture; complete pneumatic cab change not validated"});File.WriteAllText(TrainerSession.ResultPath("Trainer_vcu_native_power_positions.json"),text);Console.WriteLine(text);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}
















