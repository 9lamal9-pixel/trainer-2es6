using System;using System.Collections.Generic;using System.Diagnostics;using System.Drawing;using System.Drawing.Imaging;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Security.Cryptography;using System.Web.Script.Serialization;using System.Windows.Forms;
class MpsudAuditSnapshotProbe {
 static readonly BindingFlags Flags=BindingFlags.Instance|BindingFlags.NonPublic;static TrainerIntegrationHost host;
 [DllImport("gdi32.dll")]static extern uint GetObjectType(IntPtr handle);
 static object Field(string name){return typeof(TrainerIntegrationHost).GetField(name,Flags).GetValue(host);}
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,Flags,null,Type.EmptyTypes,null).Invoke(host,null);}
 static string Hash(string path){using(var sha=SHA256.Create())using(var input=File.OpenRead(path))return BitConverter.ToString(sha.ComputeHash(input)).Replace("-","");}
 static object ObserveFrame(IntPtr ptr,string name){uint kind=ptr==IntPtr.Zero?0:GetObjectType(ptr);try{
  if(kind!=7)return new{saved=false,handle="0x"+ptr.ToInt64().ToString("X"),gdi_object_type=kind,reason="No live GDI bitmap at observation point; producer/consumer lifetime not accepted."};
  using(var frame=Image.FromHbitmap(ptr))frame.Save(TrainerSession.ResultPath(name),ImageFormat.Png);
  return new{saved=true,handle="0x"+ptr.ToInt64().ToString("X"),gdi_object_type=kind,reason="Single bitmap copied; screen navigation and values not accepted."};
 }catch(Exception error){return new{saved=false,handle="0x"+ptr.ToInt64().ToString("X"),gdi_object_type=kind,reason=error.GetType().FullName+": "+error.Message};}}
 [STAThread]static int Main(){Application.EnableVisualStyles();try{
  host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);host.Show();Application.DoEvents();
  var timer=(Timer)Field("timer");if(timer==null)throw new Exception("Host did not start: "+((Label)Field("status")).Text);timer.Stop();
  for(int n=0;n<100;n++){Call("ElectricalCycle");Call("ApplyTestInputs");Call("PneumaticCycle");Call("MotionCycle");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");((Delegate)Field("klubStep")).DynamicInvoke();Application.DoEvents();System.Threading.Thread.Sleep(20);}
  var mem=(Dictionary<string,IntPtr>)Field("mem");var modules=new List<object>();var expected=new Dictionary<string,string>(StringComparer.OrdinalIgnoreCase){
   {"Communication1.dll","27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F"},
   {"MSUL_MON.dll","95E3FB14CBC7FF2B5E9C61DC2215CFA7C2F23161FE21D075ADF04F45907B653B"},
   {"Pult1.dll","7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5"},
   {"Tyga1.dll","8EC6A82E618A1D75CBB31242784084B530BB87308E601DEFC5263D5279270491"}};
  var required=new HashSet<string>(expected.Keys,StringComparer.OrdinalIgnoreCase);
  foreach(ProcessModule module in Process.GetCurrentProcess().Modules){string name=module.ModuleName;if(required.Contains(name)||name=="Saut_Dll.dll"||name=="AVP.dll"||name=="KLUB_U_LCD_XE2.dll"||name=="KPD_3PV.dll"){
   string hash=Hash(module.FileName);if(expected.ContainsKey(name)&&hash!=expected[name])throw new Exception("Unexpected native SHA: "+name+" "+hash);
   modules.Add(new{name=name,path=module.FileName,base_address="0x"+module.BaseAddress.ToInt64().ToString("X"),sha256=hash});required.Remove(name);
  }}
  if(required.Count>0)throw new Exception("Required native modules absent: "+String.Join(",",required));
  int faults=(int)Field("electricalFaults");if(faults!=0)throw new Exception("Electrical faults in basic module observation");
  var left=ObserveFrame(Marshal.ReadIntPtr(mem["TPult"],0x105),"MPSUD_native_left_initial.png");var right=ObserveFrame(Marshal.ReadIntPtr(mem["TPult"],0x109),"MPSUD_native_right_initial.png");
  File.WriteAllText(TrainerSession.ResultPath("MPSUD_audit_snapshot.json"),new JavaScriptSerializer().Serialize(new{passed=true,module_identity_accepted=true,functional_mpsuid_accepted=false,screen_rendering_accepted=false,native_cycles=100,modules=modules,left_frame_observation=left,right_frame_observation=right,left_keyboard_byte=Marshal.ReadByte(mem["TPult"],0x53),right_keyboard_byte=Marshal.ReadByte(mem["TPult"],0x54),electrical_faults=faults,scope="Assertions: four actual loaded module paths/SHA,100 warm idle native cycles,electricalFaults=0. Frame copies optional observations only; no protection/navigation/SME/link/diagnostics/rendering acceptance."}));Console.WriteLine("PASS MPSUID module identity: four path/SHA guards;100 idle cycles; electricalFaults=0. Frame observations are not acceptance.");return 0;
 }catch(Exception error){Console.WriteLine(error);return 1;}finally{if(host!=null){host.Close();host.Dispose();}}}
}