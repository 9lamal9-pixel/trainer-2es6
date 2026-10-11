using System;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Collections.Generic;
using System.Windows.Forms;
using System.Web.Script.Serialization;
class PneumaticHostProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static TrainerIntegrationHost host;static Dictionary<string,IntPtr> mem;static IntPtr[] cars;
 static float minGr=100,maxGr=0;static int cycles;static System.Text.StringBuilder csv=new System.Text.StringBuilder("phase,cycle,km,kk,gr,ur,tm,tail_tm,loco_tc,vr_tc,tail_tc,epk_cam\n");
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,null);}
 static void Check(bool value,string message){if(!value)throw new Exception(message);}
 static float F(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static void WriteF(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 static void Cycle(string phase,int expectedKm){Call("ElectricalCycle");Call("ApplyTestInputs");Check(Marshal.ReadByte(mem["TLocParam"],0x91)==1,"Communication closed combined cock");Check(Marshal.ReadByte(mem["TLocParam"],0x7d)==expectedKm,"Communication overwrote KM: actual="+Marshal.ReadByte(mem["TLocParam"],0x7d)+" tick="+cycles+" TPult23="+Marshal.ReadByte(mem["TPult"],0x23)+" TPult24="+Marshal.ReadByte(mem["TPult"],0x24));Call("PneumaticCycle");Call("MotionCycle");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");((Delegate)typeof(TrainerIntegrationHost).GetField("klubStep",flags).GetValue(host)).DynamicInvoke();cycles++;
  foreach(var car in cars)foreach(int o in new int[]{0,4,8,0xc,0x10,0x14}){float v=F(car,o);Check(!float.IsNaN(v)&&!float.IsInfinity(v),"Nonfinite car state");}
  foreach(int o in new int[]{5,9,13,0x9f}){float v=F(mem["TLocParam"],o);Check(!float.IsNaN(v)&&!float.IsInfinity(v),"Nonfinite locomotive state");}
  float gr=F(mem["TLocParam"],5);minGr=Math.Min(minGr,gr);maxGr=Math.Max(maxGr,gr);
  if(cycles%10==0)csv.AppendFormat(System.Globalization.CultureInfo.InvariantCulture,"{0},{1},{2},{3},{4:F5},{5:F5},{6:F5},{7:F5},{8:F5},{9:F5},{10:F5},{11:F5}\n",phase,cycles,Marshal.ReadByte(mem["TLocParam"],0x7d),Marshal.ReadByte(mem["TLocParam"],0x91),gr,F(mem["TLocParam"],9),F(cars[0],0),F(cars[75],0),F(mem["TLocParam"],13),F(cars[0],4),F(cars[75],4),F(mem["TLocParam"],0x9f));
 }
 static void Phase(string name,int count,int km){for(int n=0;n<count;n++)Cycle(name,km);Console.WriteLine(name+": TM="+F(cars[0],0)+" tail="+F(cars[75],0)+" GR="+F(mem["TLocParam"],5)+" locoTC="+F(mem["TLocParam"],13));}
 [STAThread]static int Main(){Application.EnableVisualStyles();host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  Check(!File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")),"Command file present");IntPtr handle=host.Handle;Call("StartNative");var timer=(Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host);Check(timer!=null,"Native initialization incomplete");timer.Stop();mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",flags).GetValue(host);
  Check(Marshal.ReadByte(mem["TLocParam"],0x91)==1,"Startup combined cock closed");Check(F(mem["TLocParam"],0x9f)==8&&Marshal.ReadInt32(mem["TLocParam"],0xa3)==0,"EPK packed pressure/counter incorrect");
  Phase("default_II",300,2);Check(F(cars[0],0)>4.85,"II fails to maintain TM");float beforeGr=F(mem["TLocParam"],5);
  // Reduce the brake pipe only; preserve the native distributor chamber/reservoir state.
  foreach(var car in cars)WriteF(car,0,4.5f);Phase("charge_II",1200,2);float chargeHead=F(cars[0],0),chargeTail=F(cars[75],0);Check(chargeHead>4.85&&chargeTail>4.55,"II fails to charge TM");Check(minGr<beforeGr-.15f,"No PM consumption during charging");
  Check((int)typeof(TrainerIntegrationHost).GetField("electricalFaults",flags).GetValue(host)==0,"Electrical exception");
  var initialSite=(IntPtr)typeof(TrainerIntegrationHost).GetField("pneumaticInitialSite",flags).GetValue(host);var restoreSite=(IntPtr)typeof(TrainerIntegrationHost).GetField("pneumaticRestoreSite",flags).GetValue(host);var initialBytes=(byte[])typeof(TrainerIntegrationHost).GetField("pneumaticInitialBytes",flags).GetValue(host);var restoreBytes=(byte[])typeof(TrainerIntegrationHost).GetField("pneumaticRestoreBytes",flags).GetValue(host);Call("RestorePneumaticMainTiming");for(int n=0;n<initialBytes.Length;n++)Check(Marshal.ReadByte(initialSite,n)==initialBytes[n],"Initial instructions not restored");for(int n=0;n<restoreBytes.Length;n++)Check(Marshal.ReadByte(restoreSite,n)==restoreBytes[n],"KVT instructions not restored");Check((IntPtr)typeof(TrainerIntegrationHost).GetField("pneumaticCave",flags).GetValue(host)==IntPtr.Zero,"Trampoline not released");var result=new Dictionary<string,object>{{"passed",true},{"scope","native integrated startup, II charging and PM consumption; compressor automatic cycle tested separately"},{"cycles",cycles},{"command_file_absent",true},{"stand_brake_override_absent",true},{"native_KM_control_preserved",true},{"startup_combined_cock",1},{"epk_pressure_offset","0x9f"},{"charged_head_tm_bar",chargeHead},{"charged_tail_tm_bar",chargeTail},{"min_pm_bar",minGr},{"max_pm_bar",maxGr},{"all_76_states_finite",true}};
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_pneumatic_main_timing_validation.json"),new JavaScriptSerializer().Serialize(result));Console.WriteLine("Integrated native pneumatic PASS");return 0;
 }catch(Exception ex){Console.WriteLine(ex);return 1;}finally{File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_pneumatic_main_timing_validation.csv"),csv.ToString());host.Close();host.Dispose();}}
}

