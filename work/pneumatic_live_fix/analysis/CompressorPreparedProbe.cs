using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class CompressorPreparedProbe {
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
  foreach(byte state in new byte[]{0,1}){
   // Diagnostic boundary reset must be included in the network's external mass ledger.
   chain.ApplyNativePressureChanges(7-chain.Pm.Gauge,0,0); if(state==1)Marshal.WriteByte(Marshal.ReadIntPtr(form,0x3dc),0x1d9,1);
   for(int i=0;i<1200;i++){
    Marshal.WriteByte(control,0x1d9,state);Marshal.WriteByte(Marshal.ReadIntPtr(form,0x3e0),0x1d9,(byte)(state==1&&i<10?1:0));Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");
    byte[] flags=new byte[8];Marshal.Copy(IntPtr.Add(loc,0x80),flags,0,8);
    
    int command=Marshal.ReadByte(mem["TPult"],0x28);int expectedCommand=state;
    if(command!=expectedCommand)throw new Exception("Native compressor switch mapping mismatch");
    Call(h,"PneumaticCycle");
    if(i%100==0||i==1199)rows.Add(new{state=state,cycles=i+1,pm=Read(loc,5),compressor_flags=flags,control_command=command,native_pressure_state=Marshal.ReadByte(loc,0x12),control_key=Marshal.ReadByte(schema,0x37),sf1=Marshal.ReadInt32(schema,0x38),observed_ab_voltage=Marshal.ReadInt32(schema,0),network_voltage_flag=Marshal.ReadByte(schema,0x123),rp1_contacts=new byte[]{Marshal.ReadByte(schema,0x3c),Marshal.ReadByte(schema,0x3d)},rp1_coils=new byte[]{Marshal.ReadByte(schema,0x40),Marshal.ReadByte(schema,0x41)},u1=new byte[]{Marshal.ReadByte(schema,0x44),Marshal.ReadByte(schema,0x45)},vup1=new byte[]{Marshal.ReadByte(schema,0x48),Marshal.ReadByte(schema,0x49)},any_XA1=Marshal.ReadByte(schema,0x60),pantograph_commands=new byte[]{Marshal.ReadByte(schema,0x54),Marshal.ReadByte(schema,0x55)},qf1=new byte[]{Marshal.ReadByte(schema,0x62),Marshal.ReadByte(schema,0x63)},sf2=new byte[]{Marshal.ReadByte(schema,0xa8),Marshal.ReadByte(schema,0xa9)},bv_contacts=new byte[]{Marshal.ReadByte(schema,0x58),Marshal.ReadByte(schema,0x59)},psn=new int[]{Marshal.ReadInt32(schema,0x242),Marshal.ReadInt32(schema,0x246),Marshal.ReadInt32(schema,0x24a),Marshal.ReadInt32(schema,0x24e)}});
   }
  }
  double chargedPm=chain.Pm.Gauge;
  if(chargedPm<8.8||Marshal.ReadByte(loc,0x80)!=1||Marshal.ReadByte(loc,0x81)!=1||Marshal.ReadInt32(schema,0x242)!=1||Marshal.ReadInt32(schema,0x246)!=1)throw new Exception("Native powered compressor start not reached");
  var pressureEvents=new List<object>();int autoCycles=0;
  for(int repeat=0;repeat<2;repeat++){
   byte previous=Marshal.ReadByte(loc,0x12);bool started=false,stopped=false;
   for(int i=0;i<1000;i++){
    // Controlled bleed is an explicit mass boundary, not a relay-state override.
    chain.ApplyNativePressureChanges(-.005,0,0);double before=chain.Pm.Gauge;
    Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");autoCycles++;
    byte next=Marshal.ReadByte(loc,0x12);
    if(previous==0&&next==1){if(before<7.45||before>7.51)throw new Exception("Compressor start pressure outside native7.5 threshold");pressureEvents.Add(new{repeat=repeat,event_name="start",before_pm=before,after_pm=chain.Pm.Gauge});started=true;break;}previous=next;
   }
   if(!started)throw new Exception("Automatic compressor restart absent");
   previous=1;
   for(int i=0;i<1000;i++){
    double before=chain.Pm.Gauge;Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");autoCycles++;
    byte next=Marshal.ReadByte(loc,0x12);
    if(previous==1&&next==0){if(before<8.98||before>9.03)throw new Exception("Compressor stop pressure outside native9 threshold");pressureEvents.Add(new{repeat=repeat,event_name="stop",before_pm=before,after_pm=chain.Pm.Gauge});stopped=true;break;}previous=next;
   }
   if(!stopped)throw new Exception("Automatic compressor stop absent");
  }
  // BB RE6 2.3: unload removes readiness for12s. Observe an early demand without overriding permission.
  chain.ApplyNativePressureChanges(7.4-chain.Pm.Gauge,0,0);double rapidBefore=chain.Pm.Gauge;
  for(int i=0;i<3;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}
  double rapidAfter=chain.Pm.Gauge; if(rapidAfter>rapidBefore+.00001)throw new Exception("BB compressor charged during early unload");
  for(int i=3;i<120;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");if(chain.Pm.Gauge>rapidBefore+.00001)throw new Exception("BB compressor charged before12s");}
  double delayedPm=chain.Pm.Gauge;for(int i=0;i<10;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}double resumedPm=chain.Pm.Gauge;if(resumedPm<delayedPm+.02)throw new Exception("BB compressor failed to resume after12s");
  // Remove BV input, keeping compressor command on; native relays must remove permission.
  Marshal.WriteByte(Marshal.ReadIntPtr(form,0x3dc),0x1d9,0);chain.ApplyNativePressureChanges(7-chain.Pm.Gauge,0,0);
  for(int i=0;i<100;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}
  byte[] removedFlags=new byte[8];Marshal.Copy(IntPtr.Add(loc,0x80),removedFlags,0,8);
  foreach(byte flag in removedFlags)if(flag!=0)throw new Exception("Compressor permission remains after BV removed");
  if(chain.Pm.Gauge>7.1)throw new Exception("PM continues charging after BV removed");
  int faults=(int)typeof(TrainerIntegrationHost).GetField("electricalFaults",f).GetValue(h);if(faults!=0)throw new Exception("Electrical faults");
  var result=new{passed=true,candidate_only=true,cycles=2630+autoCycles,supersedes_incorrect_revers_control_probe=true,positive_powered_start_verified=true,automatic_pressure_cycles=2,automatic_pressure_events=pressureEvents,rapid_restart_before_pm=rapidBefore,rapid_restart_after_pm=rapidAfter,rapid_restart_elapsed_seconds=.3,BB_12_second_readiness_verified=true,unload_12_seconds_pm=delayedPm,post_unload_resumed_pm=resumedPm,charged_pm=chargedPm,bv_removed_pm=chain.Pm.Gauge,bv_removed_compressor_flags=removedFlags,diagnostic_input="Native compressor control form+3c8 state -> TPult+28; no permission flags overridden; Sequential native inputs: pantographs1f/20 first, then BV25 with return26 pulse, compressor28; BB physical-source12s readiness verified; remaining equipment timing and complete RE preparation remain unverified",electrical_faults=faults,rows=rows};
  File.WriteAllText(TrainerSession.ResultPath("Trainer_compressor_prepared_observations.json"),new JavaScriptSerializer().Serialize(result));
  Console.WriteLine(new JavaScriptSerializer().Serialize(result));return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}





