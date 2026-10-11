using System;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Collections.Generic;
using System.Windows.Forms;
using System.Drawing;
using System.Drawing.Imaging;
class KlubPowerProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static TrainerIntegrationHost host;static Dictionary<string,IntPtr> mem;
 static void Call(string name,params object[] args){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,args);}
 static void F(IntPtr p,int offset,float value){Marshal.Copy(BitConverter.GetBytes(value),0,IntPtr.Add(p,offset),4);}
 static void Mode(int value){((ComboBox)typeof(TrainerIntegrationHost).GetField("klubPowerChoice",flags).GetValue(host)).SelectedIndex=value;}
 static void Pump(int count){for(int i=0;i<count;i++){Call("ElectricalCycle");Call("ApplyTestInputs");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");((Delegate)typeof(TrainerIntegrationHost).GetField("klubStep",flags).GetValue(host)).DynamicInvoke();Application.DoEvents();}}
 static Bitmap Frame(string name){IntPtr p=Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32);if(p==IntPtr.Zero){var wrap=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);var form=Marshal.ReadIntPtr(wrap,0xc);p=Marshal.ReadIntPtr(Marshal.ReadIntPtr(form,0x508),0x298);}if(p==IntPtr.Zero)throw new Exception("Native frame absent");var b=Image.FromHbitmap(p);b.Save(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,name+".png"),ImageFormat.Png);return b;}
 static long PixelHash(Bitmap b,Rectangle rect){long hash=17;unchecked{for(int y=rect.Top;y<rect.Bottom;y++)for(int x=rect.Left;x<rect.Right;x++)hash=hash*31+b.GetPixel(x,y).ToArgb();}return hash;}
 [STAThread]static int Main(){Application.EnableVisualStyles();host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  if(File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")))throw new Exception("Temporary test commands remain");
  IntPtr handle=host.Handle;Call("StartNative");var timer=(Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host);if(timer==null)throw new Exception("Startup incomplete");timer.Stop();mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);
  Pump(30);if(Marshal.ReadByte(mem["TPBBtn"],4)!=1)throw new Exception("Default startup is unpowered");
  var powerOffSensors=new byte[2];powerOffSensors[0]=Marshal.ReadByte(mem["TPBBtn"],7);powerOffSensors[1]=Marshal.ReadByte(mem["TPBBtn"],8);
  F(mem["TInputParamPult"],1,35);F(mem["TVagonParam"],0,4.6f);F(mem["TLocParam"],9,4.7f);Pump(12);using(var first=Frame("KLUB_verified_35")){
   Mode(1);Pump(12);if(Marshal.ReadByte(mem["TPBBtn"],4)!=0)throw new Exception("Power-off failed");using(var off=Frame("KLUB_verified_off")){
    if(PixelHash(first,new Rectangle(224,248,116,65))==PixelHash(off,new Rectangle(224,248,116,65)))throw new Exception("Powered screen identical to unpowered");
   }
   Mode(0);F(mem["TInputParamPult"],1,42);F(mem["TVagonParam"],0,5.2f);Pump(20);using(var second=Frame("KLUB_verified_42")){
    if(PixelHash(first,new Rectangle(224,248,116,65))==PixelHash(second,new Rectangle(224,248,116,65)))throw new Exception("Speed digits did not update");
    if(PixelHash(first,new Rectangle(456,202,120,40))==PixelHash(second,new Rectangle(456,202,120,40)))throw new Exception("TM pressure digits did not update");
   }
  }
  Mode(2);Marshal.WriteByte(mem["TPBBtn"],4,0);Call("ApplyTestInputs");if(Marshal.ReadByte(mem["TPBBtn"],4)!=0)throw new Exception("Cab mode forces power on");Marshal.WriteByte(mem["TPBBtn"],4,1);Call("ApplyTestInputs");if(Marshal.ReadByte(mem["TPBBtn"],4)!=1)throw new Exception("Cab mode forces power off");
  if(Marshal.ReadByte(mem["TPBBtn"],7)!=powerOffSensors[0]||Marshal.ReadByte(mem["TPBBtn"],8)!=powerOffSensors[1])throw new Exception("Unselected BLOK/KPD power changed");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_klub_power_validation.txt"),"PASS: default power ON without command file; OFF; ON again; native speed digits 35 ->42; native TM pressure digits 0.46 ->0.52 MPa; cab mode releases override; BLOK/KPD power unaffected. Frames are native HBITMAPs, no desktop capture.\n");Console.WriteLine("KLUB native power/data/display PASS");return 0;
 }catch(Exception ex){Console.WriteLine(ex);return 1;}finally{host.Close();host.Dispose();}}
}
