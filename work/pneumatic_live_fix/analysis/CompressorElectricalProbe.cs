using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class CompressorElectricalProbe {
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
  var rows=new List<object>();
  var pultModule=GetModuleHandle("Pult1.dll");if(pultModule==IntPtr.Zero)throw new Exception("Pult1 absent");
  var pultOwner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(pultModule,0x1a2e60));
  if(pultOwner==IntPtr.Zero||Marshal.ReadIntPtr(pultOwner,4)!=mem["TPult"])throw new Exception("Native electrical owner guard");
  var schema=IntPtr.Add(pultOwner,0x18);
  foreach(byte state in new byte[]{0,1}){
   // Diagnostic boundary reset must be included in the network's external mass ledger.
   chain.ApplyNativePressureChanges(7-chain.Pm.Gauge,0,0);
   for(int i=0;i<300;i++){
    Marshal.WriteByte(control,0x1d9,state);Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");
    byte[] flags=new byte[8];Marshal.Copy(IntPtr.Add(loc,0x80),flags,0,8);
    foreach(byte permission in flags)if(permission!=0)throw new Exception("Unexpected compressor permission in initial electrical state");
    int command=Marshal.ReadByte(mem["TPult"],0x28);int expectedCommand=state;
    if(command!=expectedCommand)throw new Exception("Native compressor switch mapping mismatch");
    Call(h,"PneumaticCycle");if(Read(loc,5)>7.001)throw new Exception("PM charging without initial electrical permission");
    if(i%100==0||i==299)rows.Add(new{state=state,cycles=i+1,pm=Read(loc,5),compressor_flags=flags,control_command=command,native_pressure_state=Marshal.ReadByte(loc,0x12),control_key=Marshal.ReadByte(schema,0x37),sf1=Marshal.ReadInt32(schema,0x38),observed_ab_voltage=Marshal.ReadInt32(schema,0),psn=new int[]{Marshal.ReadInt32(schema,0x242),Marshal.ReadInt32(schema,0x246),Marshal.ReadInt32(schema,0x24a),Marshal.ReadInt32(schema,0x24e)}});
   }
  }
  int faults=(int)typeof(TrainerIntegrationHost).GetField("electricalFaults",f).GetValue(h);if(faults!=0)throw new Exception("Electrical faults");
  var result=new{passed=true,candidate_only=true,cycles=600,supersedes_incorrect_revers_control_probe=true,positive_powered_start_verified=false,diagnostic_input="Native compressor control form+3c8 state -> TPult+28; no permission flags overridden; initial electrical state only",electrical_faults=faults,rows=rows};
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_compressor_electrical_observations.json"),new JavaScriptSerializer().Serialize(result));
  Console.WriteLine(new JavaScriptSerializer().Serialize(result));return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}
