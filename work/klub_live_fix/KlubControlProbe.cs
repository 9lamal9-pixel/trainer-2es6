using System;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Collections.Generic;
using System.Windows.Forms;
class KlubControlProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static TrainerIntegrationHost host; static Dictionary<string,IntPtr> mem;
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,null);}
 static void Mode(int value){((ComboBox)typeof(TrainerIntegrationHost).GetField("klubPowerChoice",flags).GetValue(host)).SelectedIndex=value;}
 static bool Basis(){return (bool)typeof(TrainerIntegrationHost).GetField("klubBasisReady",flags).GetValue(host);}
 static void Check(bool condition,string message){if(!condition)throw new Exception(message);}
 static void Pump(int count){for(int i=0;i<count;i++){Call("ElectricalCycle");Call("ApplyTestInputs");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");((Delegate)typeof(TrainerIntegrationHost).GetField("klubStep",flags).GetValue(host)).DynamicInvoke();Application.DoEvents();}}
 [STAThread]static int Main(){Application.EnableVisualStyles();host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  Check(!File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")),"Temporary test commands remain");
  IntPtr handle=host.Handle;Call("StartNative");var timer=(Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host);Check(timer!=null,"Startup incomplete");timer.Stop();mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);
  Pump(20);Check(Marshal.ReadByte(mem["TPBBtn"],4)==1&&Basis(),"Default startup is unpowered or coordinate basis absent");
  byte blok=Marshal.ReadByte(mem["TPBBtn"],7),kpd=Marshal.ReadByte(mem["TPBBtn"],8);
  Mode(1);Pump(10);Check(Marshal.ReadByte(mem["TPBBtn"],4)==0&&!Basis(),"Power OFF failed");
  Mode(0);Pump(10);Check(Marshal.ReadByte(mem["TPBBtn"],4)==1&&Basis(),"Power ON again failed");
  Call("ReleaseTestInputs");Call("ApplyTestInputs");Check(Marshal.ReadByte(mem["TPBBtn"],4)==1,"Brake input release unexpectedly powers down KLUB");
  Mode(2);Marshal.WriteByte(mem["TPBBtn"],4,0);Call("ApplyTestInputs");Check(Marshal.ReadByte(mem["TPBBtn"],4)==0,"Cab mode forces power ON");Marshal.WriteByte(mem["TPBBtn"],4,1);Call("ApplyTestInputs");Check(Marshal.ReadByte(mem["TPBBtn"],4)==1,"Cab mode forces power OFF");
  Check(Marshal.ReadByte(mem["TPBBtn"],7)==blok&&Marshal.ReadByte(mem["TPBBtn"],8)==kpd,"Unselected BLOK/KPD power changed");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_klub_power_validation.txt"),"PASS: integrated native systems; default KLUB ON with no command file; OFF clears coordinate basis; ON again restores basis; releasing brake input preserves KLUB power choice; cab mode releases power override; BLOK/KPD power unchanged. Native bitmap/data test recorded separately. No computer use or desktop capture.\n");Console.WriteLine("KLUB integrated power control PASS");return 0;
 }catch(Exception ex){Console.WriteLine(ex);return 1;}finally{host.Close();host.Dispose();}}
}
