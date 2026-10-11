using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;
class FourPantographPressureProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
[DllImport("kernel32.dll")]static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint kind,uint protection);[DllImport("kernel32.dll")]static extern bool VirtualFree(IntPtr address,UIntPtr size,uint kind);[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void RegisterCall();
[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int NativeCall();
static int InvokeRegister(IntPtr self,int edx,IntPtr target){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)32,0x3000,0x40);if(p==IntPtr.Zero)throw new Exception("Thunk allocation");try{var b=new List<byte>();b.Add(0xb8);b.AddRange(BitConverter.GetBytes(self.ToInt32()));b.Add(0xba);b.AddRange(BitConverter.GetBytes(edx));b.Add(0xb9);b.AddRange(BitConverter.GetBytes(target.ToInt32()));b.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(b.ToArray(),0,p,b.Count);return ((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{VirtualFree(p,UIntPtr.Zero,0x8000);}}static void SetPosition(IntPtr self,IntPtr target,double value){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)64,0x3000,0x40);try{var b=new List<byte>();b.Add(0xb8);b.AddRange(BitConverter.GetBytes(self.ToInt32()));byte[] d=BitConverter.GetBytes(value);b.Add(0x68);b.AddRange(new byte[]{d[4],d[5],d[6],d[7]});b.Add(0x68);b.AddRange(new byte[]{d[0],d[1],d[2],d[3]});b.AddRange(new byte[]{0x6a,0,0x6a,0});b.Add(0xb9);b.AddRange(BitConverter.GetBytes(target.ToInt32()));b.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(b.ToArray(),0,p,b.Count);((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{VirtualFree(p,UIntPtr.Zero,0x8000);}}

static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
static void Call(object h,string n){typeof(TrainerIntegrationHost).GetMethod(n,flags).Invoke(h,null);}
static void WriteDouble(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}
[STAThread]static int Main(){Application.EnableVisualStyles();var h=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
var handle=h.Handle;Call(h,"StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(h)).Stop();
var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(h);
Call(h,"ElectricalCycle");var module=GetModuleHandle("Pult1.dll");var owner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(module,0x1a2e60));
if(Marshal.ReadIntPtr(owner,4)!=mem["TPult"])throw new Exception("Owner guard");
var schema=IntPtr.Add(owner,0x18);var rows=new List<object>(); var fallingRows=new List<object>();
foreach(double pm in new[]{0.0,8.0})foreach(double air in new[]{0.0,3.5,3.8,5.0}){
Marshal.Copy(BitConverter.GetBytes((float)pm),0,IntPtr.Add(mem["TLocParam"],5),4);WriteDouble(mem["TPult"],0xbd,air);
Marshal.WriteByte(mem["TPult"],0x1f,1);Marshal.WriteByte(mem["TPult"],0x20,1);Marshal.WriteByte(mem["TPult"],0x21,1);Marshal.WriteByte(mem["TPult"],0x22,1);Marshal.WriteByte(mem["TPult"],0xf7,0);Marshal.WriteByte(mem["TPult"],0xf4,0);Marshal.WriteByte(mem["TPult"],0x25,0); Marshal.WriteByte(schema,0x37,1); for(int off=0x38;off<=0x42;off++)Marshal.WriteByte(schema,off,1);
for(int i=0;i<4;i++){foreach(int off in new[]{0x47,0x53,0x4c})Marshal.WriteByte(schema,off+i,1);Marshal.WriteByte(schema,0x57+i,0);}
for(int i=0;i<4;i++){Marshal.WriteByte(owner,0x62485+i,0);Marshal.WriteInt32(owner,i==0?0x62481:0x62489+(i-1)*4,100);Marshal.WriteByte(schema,0x58+i,0);}Marshal.WriteByte(owner,0x62485,0);Marshal.WriteInt32(owner,0x62481,100);Marshal.WriteByte(schema,0x58,0);InvokeRegister(owner,0,IntPtr.Add(module,0x18be58)); int initialRequest=Marshal.ReadByte(owner,0x62485); for(int cycle=0;cycle<110;cycle++)InvokeRegister(owner,0,IntPtr.Add(module,0x18be58));
rows.Add(new{pm=pm,control_air=air,permit=initialRequest,delayed_contact=Marshal.ReadByte(schema,0x58),native_pantograph_feedback=Marshal.ReadByte(mem["TPult"],0x97),native_line_voltage=Marshal.ReadInt32(schema,0x125),feedbacks=new[]{Marshal.ReadByte(mem["TPult"],0x97),Marshal.ReadByte(mem["TPult"],0x98),Marshal.ReadByte(mem["TPult"],0x99),Marshal.ReadByte(mem["TPult"],0x9a)},delay_remaining=Marshal.ReadInt32(owner,0x62481),schema47=Marshal.ReadByte(schema,0x47),schema53=Marshal.ReadByte(schema,0x53),schema4c=Marshal.ReadByte(schema,0x4c),schema4b=Marshal.ReadByte(schema,0x4b),schema4f=Marshal.ReadByte(schema,0x4f),s4c2=Marshal.ReadByte(schema,0x4d),s4b2=Marshal.ReadByte(schema,0x4c),s4f2=Marshal.ReadByte(schema,0x50),s532=Marshal.ReadByte(schema,0x54)});
}
WriteDouble(mem["TPult"],0xbd,2.8); for(int cycle=0;cycle<5;cycle++)InvokeRegister(owner,0,IntPtr.Add(module,0x18be58)); fallingRows.Add(new{pm=8.0,control_air=2.8,feedback=Marshal.ReadByte(mem["TPult"],0x97),line_voltage=Marshal.ReadInt32(schema,0x125)}); var result=new{falling_rows=fallingRows,scope="Native UpdateButton invocation; diagnostic electrical contact fixtures; no actual pantograph actuator or hardware",rows=rows};var json=new JavaScriptSerializer().Serialize(result);File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_four_pantograph_pressure_probe.json"),json);Console.WriteLine(json);return 0;
}catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}






