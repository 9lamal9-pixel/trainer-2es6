using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class AuxiliaryAirNativeProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
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
  chain.ApplyNativePressureChanges(-chain.Pm.Gauge,0,0);Marshal.Copy(BitConverter.GetBytes(0f),0,IntPtr.Add(loc,5),4);
  Marshal.Copy(BitConverter.GetBytes(0d),0,IntPtr.Add(mem["TPult"],0xbd),8);
  double initialPhysicalPm=chain.Pm.Gauge;
  for(int probeTick=0;probeTick<100;probeTick++){Marshal.WriteByte(mem["TPult"],0x46,1);Call(h,"ElectricalCycle");}
  byte[] auxiliaryBytes=new byte[8];Marshal.Copy(IntPtr.Add(mem["TPult"],0xbd),auxiliaryBytes,0,8);
  double auxiliaryPressure=BitConverter.ToDouble(auxiliaryBytes,0);double writtenPm=Read(loc,5);
  if(auxiliaryPressure<.9||Math.Abs(writtenPm)>1e-6||Math.Abs(chain.Pm.Gauge-initialPhysicalPm)>1e-9)throw new Exception("Auxiliary-source alias removal failed");
  var diagnostic=new{passed=true,auxiliary_hardware_id=auxiliaryHardwareId,cycles=100,source="Pult1 UpdateButton RVA18bec1..18bef5",diagnostic_auxiliary_input="TPult+46 stand input",auxiliary_pressure=auxiliaryPressure,native_pm_written=writtenPm,physical_pm=chain.Pm.Gauge,defect="Auxiliary pressure copied to Loc.GR without transfer of mass; electrical-cycle write aliases auxiliary and main circuits",not_fixed=false,auxiliary_pressure_preserved=true};
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_auxiliary_native_air_alias.json"),new JavaScriptSerializer().Serialize(diagnostic));Console.WriteLine(new JavaScriptSerializer().Serialize(diagnostic));Call(h,"CloseNativeAirCircuit");if(Marshal.ReadByte(pultModule,0x18bef5)!=0xd9||Marshal.ReadByte(pultModule,0x18bef6)!=0x58||Marshal.ReadByte(pultModule,0x18bef7)!=5)throw new Exception("Auxiliary hook restoration failed");Console.WriteLine("Hook restoration PASS");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}









