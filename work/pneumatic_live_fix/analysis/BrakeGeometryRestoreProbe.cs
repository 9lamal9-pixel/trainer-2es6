using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;
class BrakeGeometryRestoreProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static float Read(IntPtr p,int offset){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
 [STAThread]static int Main(){Application.EnableVisualStyles();var h=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);bool closed=false;try{
  var handle=h.Handle;typeof(TrainerIntegrationHost).GetMethod("StartNative",flags).Invoke(h,null);((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(h)).Stop();
  typeof(TrainerIntegrationHost).GetMethod("ElectricalCycle",flags).Invoke(h,null);
  var p=GetModuleHandle("POEZD.DLL");var c=GetModuleHandle("Communication1.dll");if(p==IntPtr.Zero||c==IntPtr.Zero)throw new Exception("Native modules absent");
  float area=Read(p,0x151c8),lever0=Read(p,0x151d4),lever1=Read(p,0x151e0);
  if(Math.Abs(area-Math.PI*20.3*20.3/4)>1e-4||Math.Abs(lever0-8.8)>1e-5||Math.Abs(lever1-8.8)>1e-5)throw new Exception("Geometry not connected");
  h.Close();h.Dispose();closed=true;
  if(Read(p,0x151c8)!=1257f||Read(p,0x151d4)!=8.245f||Read(p,0x151e0)!=12.915f)throw new Exception("Native geometry not restored");
  if(Marshal.ReadByte(c,0x2ae6e8)!=0xdb||Marshal.ReadByte(c,0x2ae6e9)!=0x2d||Marshal.ReadByte(c,0x2ae6fa)!=0xd8||Marshal.ReadByte(c,0x2ae6fb)!=0x68||Marshal.ReadByte(c,0x2ae6fc)!=0x0d||Marshal.ReadByte(c,0x2b1e75)!=0xd9)throw new Exception("Native TC2 hooks not restored");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_brake_geometry_restore_validation.json"),new JavaScriptSerializer().Serialize(new{passed=true,candidate_only=true,area_cm2=area,lever0=lever0,lever1=lever1,native_geometry_restored=true,native_gauge_hook_restored=true,limitations="Validates table geometry and restoration, not pad type, friction, return spring or complete braking-force acceptance."}));
  Console.WriteLine("PASS brake geometry connection/restoration: area="+area+",levers="+lever0+"/"+lever1);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{if(!closed){h.Close();h.Dispose();}}}
}
