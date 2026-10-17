using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class Sf3ControlAirHostProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
[DllImport("kernel32.dll")]static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint kind,uint protection);[DllImport("kernel32.dll")]static extern bool VirtualFree(IntPtr address,UIntPtr size,uint kind);[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void RegisterCall();
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
  for(int sample=0;sample<100;sample++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}if(Marshal.ReadByte(schema,0x1aa)!=1||Marshal.ReadByte(schema,0x1b6)!=1||Marshal.ReadByte(schema,0x1ae)!=1||Marshal.ReadByte(schema,0x1ba)!=1)throw new Exception("Joint KM10/KM11 start failed");Console.WriteLine("Main section="+Marshal.ReadByte(schema,0x36)+" key="+Marshal.ReadByte(schema,0x37)+" coils="+Marshal.ReadByte(schema,0x1aa)+","+Marshal.ReadByte(schema,0x1b6));Console.WriteLine("KM10/11 contacts="+Marshal.ReadByte(schema,0x1ae)+","+Marshal.ReadByte(schema,0x1b2)+","+Marshal.ReadByte(schema,0x1ba)+","+Marshal.ReadByte(schema,0x1be));var sf3=(bool[])typeof(TrainerIntegrationHost).GetField("controlSf3",f).GetValue(h);var feed=(bool[])typeof(TrainerIntegrationHost).GetField("controlFeedOpen",f).GetValue(h);var demand=(double[])typeof(TrainerIntegrationHost).GetField("controlConsumers",f).GetValue(h);
  Marshal.WriteByte(mem["TPult"],0x46,1);double sourceBefore=chain.ControlAir[0].AuxiliarySourceMass;Call(h,"StepHostControlAir");if(chain.ControlAir[0].AuxiliarySourceMass<=sourceBefore||Marshal.ReadByte(mem["TPult"],0xbc)!=1)throw new Exception("Auxiliary produces air but native process flag is off");sf3[0]=false;Call(h,"StepHostControlAir");if(Marshal.ReadByte(mem["TPult"],0xbc)!=0)throw new Exception("Auxiliary flag ignores SF3");sf3[0]=true;Marshal.WriteByte(mem["TPult"],0x46,0);Call(h,"StepHostControlAir");if(Marshal.ReadByte(mem["TPult"],0xbc)!=0)throw new Exception("Auxiliary flag ignores command off");
  feed[0]=false;feed[1]=false;demand[0]=1;sf3[0]=false;sf3[1]=false;
  double reserve=chain.ControlAir[0].Rs7.Gauge;
  for(int i=0;i<1000;i++)Call(h,"StepHostControlAir");double depleted=chain.ControlAir[0].Rs6.Gauge;
  if(Math.Abs(chain.ControlAir[0].Rs7.Gauge-reserve)>1e-6||depleted>4.5)throw new Exception("SF3 isolation failed");
  sf3[0]=true;demand[0]=0;for(int i=0;i<1000;i++)Call(h,"StepHostControlAir");
  if(chain.ControlAir[0].Rs6.Gauge<depleted+.1||chain.ControlAir[0].Rs7.Gauge>=reserve-.01||Math.Abs(chain.ControlAir[1].Rs7.Gauge-5)>1e-6)throw new Exception("SF3 reconnect failed");
  var observation=new{auxiliary_process_flag_verified=true,passed=true,electrical_cycles=100,stand_control_air_steps=2000,native_vcu=Marshal.ReadByte(loc,0x8f),native_control_key=Marshal.ReadByte(schema,0x37),main_section=Marshal.ReadByte(schema,0x36),km10_coil=Marshal.ReadByte(schema,0x1aa),km11_coil=Marshal.ReadByte(schema,0x1b6),contacts=new byte[]{Marshal.ReadByte(schema,0x1ae),Marshal.ReadByte(schema,0x1b2),Marshal.ReadByte(schema,0x1ba),Marshal.ReadByte(schema,0x1be)},mass_error=chain.MaximumBalanceError,power_gate_verified=false};string observationText=new JavaScriptSerializer().Serialize(observation);File.WriteAllText(TrainerSession.ResultPath("Trainer_sf3_native_power_observations.json"),observationText);Console.WriteLine(observationText);
  byte priorVcu=Marshal.ReadByte(loc,0x8f);Marshal.WriteByte(loc,0x8f,0);var thunk=VirtualAlloc(IntPtr.Zero,(UIntPtr)64,0x3000,0x40);
  if(thunk==IntPtr.Zero)throw new Exception("Off test thunk allocation");try{var code=new List<byte>();foreach(var pair in new[]{new[]{pultOwner,IntPtr.Add(pultModule,0x18be58)},new[]{schema,IntPtr.Add(pultModule,0x188c50)}}){code.Add(0xb8);code.AddRange(BitConverter.GetBytes(pair[0].ToInt32()));code.Add(0xb9);code.AddRange(BitConverter.GetBytes(pair[1].ToInt32()));code.AddRange(new byte[]{0xff,0xd1});}code.Add(0xc3);typeof(TrainerIntegrationHost).GetMethod("WriteNativeBytes",BindingFlags.Static|BindingFlags.NonPublic).Invoke(null,new object[]{thunk,code.ToArray()});((RegisterCall)Marshal.GetDelegateForFunctionPointer(thunk,typeof(RegisterCall)))();if(Marshal.ReadByte(schema,0x1aa)!=0||Marshal.ReadByte(schema,0x1b6)!=0||Marshal.ReadByte(schema,0x1ae)!=0||Marshal.ReadByte(schema,0x1ba)!=0)throw new Exception("Joint control power removal failed");Console.WriteLine("Joint KM10/KM11 off command PASS; diagnostic input VCU0, not physical cab off validation");}finally{Marshal.WriteByte(loc,0x8f,priorVcu);VirtualFree(thunk,UIntPtr.Zero,0x8000);}
  Call(h,"CloseNativeAirCircuit");foreach(int site in new[]{0x18bfd1,0x18bffc})if(Marshal.ReadByte(pultModule,site)!=0xc6||Marshal.ReadByte(pultModule,site+3)!=0xa9)throw new Exception("Control power hook restore failed");byte[] vcuOriginal={0x80,0xb8,0x8f,0,0,0,0,0x76,0x2b};for(int i=0;i<vcuOriginal.Length;i++)if(Marshal.ReadByte(pultModule,0x18bfa7+i)!=vcuOriginal[i])throw new Exception("VCU power hook restore failed");Console.WriteLine("Control power hook restoration PASS");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}
















