using System;
using System.IO;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Drawing;
using System.Drawing.Imaging;
using System.Security.Cryptography;
using System.Threading;

class KlubBitmapProbe {
 [DllImport("kernel32",CharSet=CharSet.Unicode,SetLastError=true)] static extern IntPtr LoadLibrary(string path);
 [DllImport("kernel32",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module,string name);
 [DllImport("kernel32",CharSet=CharSet.Unicode)] static extern bool SetDllDirectory(string path);
 [DllImport("gdi32.dll",SetLastError=true)] static extern bool DeleteObject(IntPtr handle);
 [DllImport("gdi32.dll")] static extern uint GetObjectType(IntPtr handle);
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)] struct KlubInit {public int X,Y;public IntPtr Path;public byte Type,Rotate,Show,TipDv,HandleFlag;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=16)] struct KlubState {public byte Map;public int MapNumber,X,InitTime;public byte Cassette;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate byte InitFn(IntPtr shared,KlubInit config,KlubState state);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate byte StepFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void CloseFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void PointersFn(out IntPtr current,out IntPtr state);
 static List<IntPtr> allocations=new List<IntPtr>();static IntPtr button,pbButton,input,outRec,loc,wagon,pb;
 static string owned,logPath;static int failures,frames,freed;static IntPtr klubOwner;
 static void Log(string value){File.AppendAllText(logPath,DateTime.Now.ToString("O")+" "+value+Environment.NewLine);Console.WriteLine(value);}
 static IntPtr Alloc(int n){IntPtr p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);allocations.Add(p);return p;}
 static void I(IntPtr p,int offset,int v){Marshal.WriteInt32(p,offset,v);}
 static void B(IntPtr p,int offset,int v){Marshal.WriteByte(p,offset,(byte)v);}
 static void P(IntPtr p,int offset,IntPtr v){Marshal.WriteIntPtr(p,offset,v);}
 static void F(IntPtr p,int offset,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,offset),4);}
 static void D(IntPtr p,int offset,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,offset),8);}
 static float ReadFloat(IntPtr p,int offset){byte[] raw=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),raw,0,4);return BitConverter.ToSingle(raw,0);}
 static string SpeedPixels(Bitmap bitmap){
  // Native green factual-speed rectangle only. No time, pressure or orange limit.
  List<byte> pixels=new List<byte>();for(int y=264;y<307;y++)for(int x=215;x<332;x++){Color c=bitmap.GetPixel(x,y);pixels.Add(c.R);pixels.Add(c.G);pixels.Add(c.B);}
  using(SHA256 sha=SHA256.Create())return BitConverter.ToString(sha.ComputeHash(pixels.ToArray())).Replace("-","");
 }
 static T Export<T>(IntPtr module,string name){IntPtr p=GetProcAddress(module,name);if(p==IntPtr.Zero)throw new Exception("Export absent "+name);return (T)(object)Marshal.GetDelegateForFunctionPointer(p,typeof(T));}
 static void Basis(IntPtr current,IntPtr state,int railMeters,int physicalMeters){
  IntPtr owner=IntPtr.Subtract(state,8);if(current!=IntPtr.Add(owner,0x91))throw new Exception("Pointer guard failed");
  IntPtr coord=IntPtr.Add(state,0x34);B(state,0,0);I(coord,0,railMeters);I(coord,4,railMeters);I(coord,8,physicalMeters);B(coord,12,1);I(coord,13,physicalMeters);D(coord,17,0);I(coord,25,railMeters);I(owner,0xfd,1);B(owner,0x101,1);
 }
 static Bitmap Consume(){
  IntPtr handle=Marshal.ReadIntPtr(outRec,0x32);if(handle==IntPtr.Zero)return null;
  P(outRec,0x32,IntPtr.Zero);
  try{
   if(GetObjectType(handle)!=7)throw new Exception("Shared frame is not HBITMAP");
   using(Image native=Image.FromHbitmap(handle)){frames++;return new Bitmap(native);}
  }finally{if(!DeleteObject(handle))throw new Exception("DeleteObject failed="+Marshal.GetLastWin32Error());freed++;}
 }
 static string Phase(StepFn step,string name,int loops,IntPtr coord,int expected){
  Bitmap last=null;int start=frames;
  try{
   for(int i=0;i<loops;i++){
    byte result=step();if(result!=1)throw new Exception("ProceedKlub returned "+result);
    Application.DoEvents();Bitmap frame=Consume();if(frame!=null){if(last!=null)last.Dispose();last=frame;}Thread.Sleep(100);
   }
   int x=Marshal.ReadInt32(coord,4);if(x!=expected)failures++;
   if(last==null){failures++;Log(name+" FAIL no native frame");return null;}
   string dest=Path.Combine(owned,name+".png");last.Save(dest,ImageFormat.Png);
   string hash;using(SHA256 sha=SHA256.Create())hash=BitConverter.ToString(sha.ComputeHash(File.ReadAllBytes(dest))).Replace("-","");
   string speedHash=SpeedPixels(last);File.WriteAllText(Path.Combine(owned,name+"_speed_region_sha256.txt"),"rectangle x=215 y=264 width=117 height=43 RGB; excludes clocks/pressures/orange limit\n"+speedHash+"\ninputV="+ReadFloat(input,1)+" nativeVfactLast="+Marshal.ReadInt32(klubOwner,0xde)+"\n");
   Log(name+" power="+Marshal.ReadByte(pbButton,4)+" coord="+x+" expected="+expected+" frames="+(frames-start)+" size="+last.Width+"x"+last.Height+" sha256="+hash+" speed_region="+speedHash+" inputV="+ReadFloat(input,1)+" nativeVfactLast="+Marshal.ReadInt32(klubOwner,0xde));
   if(last.Width!=599||last.Height!=802)failures++;
   return speedHash;
  }finally{if(last!=null)last.Dispose();}
 }
 [STAThread] static int Main(){
  owned=AppDomain.CurrentDomain.BaseDirectory;logPath=Path.Combine(owned,"bitmap_probe_log.txt");CloseFn close=null;bool ready=false;
  try {
   File.WriteAllText(logPath,"");Log("Isolated hidden native Type2 BIL-UT; HandleFlag=1; no Communication/cab consumer; own HBITMAP only");
   string resources=Path.GetFullPath(Path.Combine(owned,@"..\..\trenager_static\WORK_TRENAGER_3D\RESOURCES"));
   string dataRoot=Path.Combine(owned,"probe_root")+"\\";Directory.SetCurrentDirectory(dataRoot);SetDllDirectory(resources);
   button=Alloc(92);pbButton=Alloc(26);P(button,88,pbButton);P(pbButton,0,Alloc(16));B(pbButton,4,1);
   input=Alloc(52);F(input,1,35);I(input,5,80);I(input,9,80);F(input,13,2068);I(input,17,2068);P(input,31,Alloc(59));F(input,35,1);I(input,44,75);I(input,48,6000);
   outRec=Alloc(106);loc=Alloc(264);I(loc,0,1);B(loc,4,2);F(loc,5,8);F(loc,9,5);B(loc,0x99,1);wagon=Alloc(243);I(wagon,0x25,200);F(wagon,0,4.6f);F(wagon,8,5);
   pb=Alloc(24);P(pb,0,button);P(pb,4,Alloc(19));P(pb,8,input);P(pb,12,outRec);P(pb,16,loc);P(pb,20,wagon);
   IntPtr module=LoadLibrary(Path.Combine(resources,"KLUB_U_LCD_XE2.dll"));if(module==IntPtr.Zero)throw new Exception("LoadLibrary error="+Marshal.GetLastWin32Error());
   byte result=Export<InitFn>(module,"InitKlub_U_LCD")(pb,new KlubInit{Path=Marshal.StringToHGlobalUni(dataRoot),Type=2,Show=0,TipDv=1,HandleFlag=1},new KlubState{Map=0,X=2147432,Cassette=1});Log("Init="+result);if(result!=1)throw new Exception("Init failed");ready=true;close=Export<CloseFn>(module,"CloseKlub");StepFn step=Export<StepFn>(module,"ProceedKlub");
   IntPtr current,state;Export<PointersFn>(module,"GetParamInToKLUB")(out current,out state);if(current==IntPtr.Zero||state==IntPtr.Zero)throw new Exception("GetParam pointers missing");IntPtr coord=IntPtr.Add(state,0x34);klubOwner=IntPtr.Subtract(state,8);
   Basis(current,state,2147432,2068);string a=Phase(step,"11_REALTIME_ON_V35_TM46",20,coord,2147432);if(Marshal.ReadInt32(klubOwner,0xde)!=35)failures++;
   F(input,1,42);F(wagon,0,5.2f);string b=Phase(step,"12_REALTIME_ON_V42_TM52",30,coord,2147432);if(a==b||Marshal.ReadInt32(klubOwner,0xde)!=42)failures++;
   B(pbButton,4,0);string off=Phase(step,"13_REALTIME_OFF",3,coord,0);if(off==b)failures++;
   B(pbButton,4,1);Basis(current,state,2147432,2068);string on=Phase(step,"14_REALTIME_ON_AGAIN_V42_TM52",15,coord,2147432);if(on==off||Marshal.ReadInt32(klubOwner,0xde)!=42)failures++;
   if(frames!=freed||Marshal.ReadIntPtr(outRec,0x32)!=IntPtr.Zero)failures++;
   Log("COMPLETED failures="+failures+" HBITMAPs="+frames+" deleted="+freed+" pending="+Marshal.ReadIntPtr(outRec,0x32));return failures==0?0:2;
  }catch(Exception ex){Log(ex.ToString());return 1;}finally{if(ready&&close!=null){try{using(Bitmap last=Consume()){}close();Log("Native closed");}catch(Exception ex){Log("Close exception="+ex);}}}
 }
}
