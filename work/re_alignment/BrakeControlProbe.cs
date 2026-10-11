using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;
class BrakeControlProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;static TrainerIntegrationHost host;static Dictionary<string,IntPtr> mem;static List<IntPtr> owned=new List<IntPtr>();
 static void Call(string n,params object[] a){typeof(TrainerIntegrationHost).GetMethod(n,flags).Invoke(host,a);}
 static IntPtr A(int n){var p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);owned.Add(p);return p;}
 static void Check(bool b,string m){if(!b)throw new Exception(m);}
 static Button Button(string label){foreach(Control c in host.Controls)if(c is Button&&c.Text==label)return (Button)c;throw new Exception("Button missing: "+label);}
 static void Event(Button b,string method,object args){typeof(Button).GetMethod(method,flags).Invoke(b,new object[]{args});}
 static void Apply(int expected){Call("ApplyTestInputs");Check(Marshal.ReadByte(mem["TLocParam"],0x7d)==expected,"KM expected "+expected);Check(Marshal.ReadByte(mem["TLocParam"],0x8e)==0&&Marshal.ReadByte(mem["TLocParam"],0x8f)==2&&Marshal.ReadByte(mem["TLocParam"],0x90)==0&&Marshal.ReadByte(mem["TLocParam"],0x91)==0,"Brake command bypassed VCU/blocking/combined cock");}
 [STAThread]static int Main(){host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);mem["TLocParam"]=A(264);mem["TPult"]=A(269);mem["TPBBtn"]=A(26);mem["TInputParamPult"]=A(52);Marshal.WriteByte(mem["TLocParam"],0x8f,2);
  var b=Button("Удерживать I");Event(b,"OnMouseDown",new MouseEventArgs(MouseButtons.Left,1,0,0,0));Apply(1);Event(b,"OnMouseUp",new MouseEventArgs(MouseButtons.Left,1,0,0,0));Apply(2);
  Event(b,"OnKeyDown",new KeyEventArgs(Keys.Space));Apply(1);Event(b,"OnKeyUp",new KeyEventArgs(Keys.Space));Apply(2);Event(b,"OnKeyDown",new KeyEventArgs(Keys.Enter));Apply(1);Event(b,"OnKeyUp",new KeyEventArgs(Keys.Enter));Apply(2);
  Call("BeginCharge");Apply(1);Event(b,"OnLostFocus",EventArgs.Empty);Apply(2);Call("BeginCharge");Call("TestBrake",5);Call("EndCharge");Apply(5);
  Event(Button("Поездное (II)"),"OnClick",EventArgs.Empty);Apply(2);
  foreach(int offset in new int[]{0x23,0x24}){Marshal.WriteByte(mem["TPult"],offset,1);Marshal.WriteByte(mem["TLocParam"],0x7d,6);Apply(6);Marshal.WriteByte(mem["TPult"],offset,0);}
  Call("ReleaseTestInputs");Marshal.WriteByte(mem["TLocParam"],0x7d,4);Apply(4);
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_RE_brake_controls_validation.txt"),"PASS: actual managed button mouse down/up and Space/Enter implement momentary I -> II; lost focus returns II; later V is preserved; II button selects running position; neither KM command nor emergency priority bypasses VCU/Blocked367/combined cock; both emergency buttons preserve native VI; release permits native KM. No desktop automation.\n");Console.WriteLine("RE brake control contracts PASS");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{host.Dispose();foreach(var p in owned)Marshal.FreeHGlobal(p);}}
}
