using System;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Collections.Generic;
using System.Windows.Forms;
using System.Web.Script.Serialization;
class FullSystemProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static void Call(object host,string name,params object[] args){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,args);}
 static float F(IntPtr p,int offset){var bytes=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),bytes,0,4);return BitConverter.ToSingle(bytes,0);}
 static bool Finite(float x){return !float.IsNaN(x)&&!float.IsInfinity(x);}
 [STAThread]static int Main(){
  Application.EnableVisualStyles();var host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);
  try{
   if(File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")))throw new Exception("Test requires no command file");
   IntPtr handle=host.Handle;Call(host,"StartNative");var type=typeof(TrainerIntegrationHost);var timer=(Timer)type.GetField("timer",flags).GetValue(host);if(timer==null)throw new Exception("Native initialization incomplete");timer.Stop();
   var mem=(Dictionary<string,IntPtr>)type.GetField("mem",flags).GetValue(host);var cars=(IntPtr[])type.GetField("cars",flags).GetValue(host);
   Call(host,"TestBrake",1);for(int n=0;n<300;n++)Cycle(host,mem,cars);
   float chargedHead=F(cars[0],0),chargedTail=F(cars[75],0);Call(host,"SetInitialSpeed",60f);Call(host,"TestBrake",5);
   float maxHead=0,maxTail=0,maximumAfterStop=0;int stop=-1;float initialX=(float)type.GetField("position",flags).GetValue(host);
   for(int n=0;n<1200;n++){
    Cycle(host,mem,cars);maxHead=Math.Max(maxHead,F(cars[0],4));maxTail=Math.Max(maxTail,F(cars[75],4));float velocity=F(mem["TInputParamPult"],1);
    if(stop<0&&velocity<.01f)stop=n;if(stop>=0)maximumAfterStop=Math.Max(maximumAfterStop,velocity);
   }
   if(maxHead<.5f||maxTail<.3f||stop<0||maximumAfterStop>.1f)throw new Exception("Native brake propagation/stop failed: head="+maxHead+" tail="+maxTail+" stop="+stop+" residual="+maximumAfterStop);
   if((int)type.GetField("electricalFaults",flags).GetValue(host)!=0)throw new Exception("Electrical faults during full-system cycle");
   var result=new Dictionary<string,object>{{"passed",true},{"cycles",1500},{"vehicles",76},{"command_file_absent",true},{"charged_head_tm_bar",chargedHead},{"charged_tail_tm_bar",chargedTail},{"max_head_tc_bar",maxHead},{"max_tail_tc_bar",maxTail},{"stop_time_s",(stop+1)*.1},{"stop_distance_m",(float)type.GetField("position",flags).GetValue(host)-initialX},{"max_speed_after_stop_kmh",maximumAfterStop},{"native_electrical_faults",0},{"native_curves",103}};
   File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_full_system_validation.json"),new JavaScriptSerializer().Serialize(result));Console.WriteLine(new JavaScriptSerializer().Serialize(result));return 0;
  }catch(Exception ex){Console.WriteLine(ex);return 1;}finally{host.Close();host.Dispose();}
 }
 static void Cycle(object host,Dictionary<string,IntPtr> mem,IntPtr[] cars){
  Call(host,"PollCommands");Call(host,"ElectricalCycle");Call(host,"ApplyTestInputs");Call(host,"PneumaticCycle");Call(host,"MotionCycle");Call(host,"RouteInputCycle");Call(host,"AuxCycle");Call(host,"KlubCoordinateCycle");
  ((Delegate)typeof(TrainerIntegrationHost).GetField("klubStep",flags).GetValue(host)).DynamicInvoke();
  foreach(var car in cars)foreach(int offset in new int[]{0,4,8,0xc,0x10,0x14})if(!Finite(F(car,offset)))throw new Exception("Nonfinite native vehicle state");
 }
}
