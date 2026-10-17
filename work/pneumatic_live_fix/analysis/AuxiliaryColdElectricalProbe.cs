using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class AuxiliaryColdElectricalProbe {
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
  int initialKm10Contact=Marshal.ReadByte(schema,0x1ae),initialKm11Contact=Marshal.ReadByte(schema,0x1ba);var sf3=(bool[])typeof(TrainerIntegrationHost).GetField("controlSf3",f).GetValue(h);var feed=(bool[])typeof(TrainerIntegrationHost).GetField("controlFeedOpen",f).GetValue(h);var demand=(double[])typeof(TrainerIntegrationHost).GetField("controlConsumers",f).GetValue(h);
  // Cold CONTROL-AIR subsystem initialization only; other native train reservoirs are not reset.
  chain=new TwoSectionAirChain(new TwoSectionAirParameters(2000,100,1,20,1,1,1,12,6,.7,.001),0,0,0);chain.InitializeControlAir(0,0);typeof(TrainerIntegrationHost).GetField("airCircuit",f).SetValue(h,chain);feed[0]=false;feed[1]=false;sf3[0]=true;sf3[1]=true;
  var command=(CheckBox)typeof(TrainerIntegrationHost).GetField("auxiliaryCommandControl",f).GetValue(h);command.CheckState=CheckState.Checked;
  double ready=-1;for(int i=0;i<5000;i++){Call(h,"ElectricalCycle");Call(h,"BeforeNativeAirStep");Call(h,"RestoreNativeAirCompressorRequests");Call(h,"StepHostControlAir");if(ready<0&&chain.ControlAir[0].PantographPressureReady)ready=(i+1)*.1;}
  if(ready<0||chain.ControlAir[0].Rs7.Gauge<3.5||chain.ControlAir[1].Rs6.Gauge>1e-6||chain.Pm.Gauge>1e-6)throw new Exception("Cold control-air topology mismatch");
  double source=chain.ControlAir[0].AuxiliarySourceMass;command.CheckState=CheckState.Unchecked;Call(h,"BeforeNativeAirStep");Call(h,"RestoreNativeAirCompressorRequests");for(int i=0;i<100;i++)Call(h,"StepHostControlAir");if(chain.ControlAir[0].AuxiliarySourceMass!=source||Marshal.ReadByte(mem["TPult"],0xbc)!=0)throw new Exception("Cold auxiliary shutdown source mismatch");
  int faults=(int)typeof(TrainerIntegrationHost).GetField("electricalFaults",f).GetValue(h);if(faults!=0)throw new Exception("Native cold electrical faults");var result=new{electrical_cycles=5000,prewarmed_electrical_cycles=0,initial_km10_contact=initialKm10Contact,initial_km11_contact=initialKm11Contact,final_km10_contact=Marshal.ReadByte(schema,0x1ae),final_km11_contact=Marshal.ReadByte(schema,0x1ba),electrical_faults=faults,passed=true,nominal_flow_l_min=105,rs6_l=55,rs7_l=150,pressure_ready_seconds=ready,rs6=chain.ControlAir[0].Rs6.Gauge,rs7=chain.ControlAir[0].Rs7.Gauge,second_rs6=chain.ControlAir[1].Rs6.Gauge,pm=chain.Pm.Gauge,mass_error=chain.MaximumBalanceError,full_native_cold_start_verified=false,scope="Cold control-air subsystem only, native electrical cycles from unprewarmed startup contacts, KN7closed, KEP11SF3open, SB1event; nominalconstantdelivery, no actuator demand; pantograph actuator/electrical interlocks not accepted"};string text=new JavaScriptSerializer().Serialize(result);File.WriteAllText(TrainerSession.ResultPath("Trainer_auxiliary_cold_electrical_validation.json"),text);Console.WriteLine(text);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}
















