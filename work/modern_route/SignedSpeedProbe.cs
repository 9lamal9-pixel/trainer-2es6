using System;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Collections.Generic;
using System.Windows.Forms;
class SignedSpeedProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static void Call(object host,string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,null);}
 static double D(IntPtr p){var bytes=new byte[8];Marshal.Copy(p,bytes,0,8);return BitConverter.ToDouble(bytes,0);}
 [STAThread]static int Main(){
  Application.EnableVisualStyles();var host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);
  var type=typeof(TrainerIntegrationHost);IntPtr patchSite=IntPtr.Zero;int checkedCases=0;
  try{
   IntPtr handle=host.Handle;Call(host,"StartNative");var timer=(Timer)type.GetField("timer",flags).GetValue(host);if(timer==null)throw new Exception("Host initialization did not reach timer");timer.Stop();
   var mem=(Dictionary<string,IntPtr>)type.GetField("mem",flags).GetValue(host);patchSite=(IntPtr)type.GetField("signedSpeedCopySite",flags).GetValue(host);
   if(patchSite==IntPtr.Zero||Marshal.ReadByte(patchSite,1)!=0xd8)throw new Exception("Runtime speed guard absent");
   foreach(float kmh in new float[]{0,.1f,3,16,38,69,80,120})foreach(int direction in new int[]{1,-1}){
    float axis=direction*kmh/3.6f;type.GetField("signedVelocity",flags).SetValue(host,axis);
    var speedBytes=BitConverter.GetBytes(kmh);Marshal.Copy(speedBytes,0,IntPtr.Add(mem["TInputParamPult"],1),4);
    for(int n=0;n<30;n++){
     Call(host,"ElectricalCycle");double actual=D(mem["TTygaParam"]);var absolute=new byte[4];Marshal.Copy(IntPtr.Add(mem["TInputParamPult"],1),absolute,0,4);
     if(actual!=(double)axis||BitConverter.ToSingle(absolute,0)!=kmh)throw new Exception("Signed/absolute speed diverged: "+kmh+" "+direction+" native="+actual);
     if((int)type.GetField("electricalFaults",flags).GetValue(host)!=0)throw new Exception("Electrical exception in integration");checkedCases++;
    }
   }
   Call(host,"RestoreSignedTractionSpeed");if(Marshal.ReadByte(patchSite,1)!=0x18)throw new Exception("Instruction restore failed");
   File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_signed_speed_validation.txt"),"PASS: "+checkedCases+" actual Communication.Execute cycles; signed native traction V retained; safety/display InputV remains absolute; 0 electrical exceptions; runtime instruction restored; disk DLL untouched.\n");
   Console.WriteLine("Signed speed native integration PASS "+checkedCases);return 0;
  }catch(Exception ex){Console.WriteLine(ex);return 1;}
  finally{host.Close();host.Dispose();}
 }
}
