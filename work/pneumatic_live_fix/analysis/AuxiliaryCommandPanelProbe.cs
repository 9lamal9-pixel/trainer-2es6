using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class AuxiliaryCommandPanelProbe {
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
  var command=(CheckBox)typeof(TrainerIntegrationHost).GetField("auxiliaryCommandControl",f).GetValue(h);var observed=new List<object>();
  foreach(var phase in new[]{new{state=CheckState.Checked,sf=true,expected=true},new{state=CheckState.Checked,sf=false,expected=false},new{state=CheckState.Unchecked,sf=true,expected=false}}){command.CheckState=phase.state;sf3[0]=phase.sf;double source=chain.ControlAir[0].AuxiliarySourceMass;for(int i=0;i<10;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}bool running=Marshal.ReadByte(mem["TPult"],0xbc)==1;double delta=chain.ControlAir[0].AuxiliarySourceMass-source;if(running!=phase.expected||(delta>1e-10)!=phase.expected)throw new Exception("SB1 panel control and physical source mismatch");observed.Add(new{state=phase.state.ToString(),sf3=phase.sf,running=running,source_mass=delta});}
  command.CheckState=CheckState.Indeterminate;Call(h,"ElectricalCycle");byte native=Marshal.ReadByte(mem["TPult"],0x46);Call(h,"PneumaticCycle");if(Marshal.ReadByte(mem["TPult"],0x46)!=native)throw new Exception("Hardware mode overwrites native command");var result=new{passed=true,phases=observed,hardware_mode_preserves_native=true,scope="WinForms SB1 CheckState event, native electrical and pneumatic cycles; no mouse clicks, pixels or external hardware receiver verification",mass_error=chain.MaximumBalanceError};string text=new JavaScriptSerializer().Serialize(result);File.WriteAllText(TrainerSession.ResultPath("Trainer_auxiliary_command_panel_validation.json"),text);Console.WriteLine(text);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}
















