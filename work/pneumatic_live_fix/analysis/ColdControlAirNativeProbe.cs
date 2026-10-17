using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class ColdControlAirNativeProbe {
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
  Call(h,"ElectricalCycle"); // installs guarded candidate hooks first
  var site=IntPtr.Add(pultModule,0x18be9d);byte[] original={0x8b,0x45,0xfc,0x8b,0x40,0x04};
  for(int i=0;i<6;i++)if(Marshal.ReadByte(site,i)!=original[i])throw new Exception("Auxiliary algorithm guard");
  var writer=typeof(TrainerIntegrationHost).GetMethod("WriteNativeBytes",BindingFlags.Static|BindingFlags.NonPublic);
  byte[] jump=new byte[6];jump[0]=0xe9;Array.Copy(BitConverter.GetBytes(0x18bf9e-0x18be9d-5),0,jump,1,4);jump[5]=0x90;
  writer.Invoke(null,new object[]{site,jump});
  try{
   chain.ApplyNativePressureChanges(-chain.Pm.Gauge,0,0);Marshal.Copy(BitConverter.GetBytes(0f),0,IntPtr.Add(loc,5),4);
   var physical=new ControlAirSection(chain.Pm,0,0,6,.7);int readyTick=0;double maxError=0;
   for(int tick=0;tick<4200;tick++){
    // KEP11 is a stand input here: SF3 has not been identified in the native record.
    physical.Step(.1,false,true,.105,0);
    Marshal.Copy(BitConverter.GetBytes(Math.Max(0,physical.Rs6.Gauge)),0,IntPtr.Add(mem["TPult"],0xbd),8);
    Call(h,"ElectricalCycle");byte[] bytes=new byte[8];Marshal.Copy(IntPtr.Add(mem["TPult"],0xbd),bytes,0,8);
    maxError=Math.Max(maxError,Math.Abs(BitConverter.ToDouble(bytes,0)-physical.Rs6.Gauge));
    if(readyTick==0&&physical.PantographPressureReady)readyTick=tick+1;
    if(Math.Abs(Read(loc,5))>1e-6||maxError>1e-9)throw new Exception("Native control pressure bridge mismatch");
   }
   int faults=(int)typeof(TrainerIntegrationHost).GetField("electricalFaults",f).GetValue(h);if(faults!=0||readyTick==0)throw new Exception("Cold control-air native cycle failed");
   var result=new{passed=true,prototype_only=true,cycles=4200,ready_seconds=readyTick*.1,physical_control=physical.Rs6.Gauge,physical_reserve=physical.Rs7.Gauge,native_main_pressure=Read(loc,5),bridge_max_error=maxError,electrical_faults=faults,source="RE6 table3.3 nominal105l/min and4.2 reservoirs",scope="Physical pressure consumed by native electrical DLL; bypassed native additive-pressure algorithm; KEP11 open is a diagnostic stand input, not validated SF3 wiring; no actuator consumption or delivery curve"};
   string text=new JavaScriptSerializer().Serialize(result);File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_cold_control_native_validation.json"),text);Console.WriteLine(text);
  }finally{writer.Invoke(null,new object[]{site,original});for(int i=0;i<6;i++)if(Marshal.ReadByte(site,i)!=original[i])throw new Exception("Cold pressure hook restore");}
  return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}









