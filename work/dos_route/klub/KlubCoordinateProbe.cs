using System;
using System.IO;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Windows.Forms;

class KlubCoordinateProbe {
 [DllImport("kernel32",CharSet=CharSet.Unicode,SetLastError=true)] static extern IntPtr LoadLibrary(string path);
 [DllImport("kernel32",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module,string name);
 [DllImport("kernel32",CharSet=CharSet.Unicode)] static extern bool SetDllDirectory(string path);
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)] struct KlubInit {public int X,Y;public IntPtr Path;public byte Type,Rotate,Show,TipDv,HandleFlag;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=16)] struct KlubState {public byte Map;public int MapNumber,X,InitTime;public byte Cassette;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate byte InitFn(IntPtr shared,KlubInit config,KlubState state);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate byte StepFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void CloseFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void PointersFn(out IntPtr current,out IntPtr state);
 static List<IntPtr> allocations=new List<IntPtr>();static IntPtr button,pbButton,input,outRec,loc,wagon,pb;
 static string logPath;static int failures;
 static void Log(string value){File.AppendAllText(logPath,DateTime.Now.ToString("O")+" "+value+Environment.NewLine);}
 static IntPtr Alloc(int n){IntPtr p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);allocations.Add(p);return p;}
 static void I(IntPtr p,int offset,int v){Marshal.WriteInt32(p,offset,v);}
 static void B(IntPtr p,int offset,int v){Marshal.WriteByte(p,offset,(byte)v);}
 static void P(IntPtr p,int offset,IntPtr v){Marshal.WriteIntPtr(p,offset,v);}
 static void F(IntPtr p,int offset,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,offset),4);}
 static void D(IntPtr p,int offset,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,offset),8);}
 static T Export<T>(IntPtr module,string name){IntPtr p=GetProcAddress(module,name);if(p==IntPtr.Zero)throw new Exception("Export absent "+name);return (T)(object)Marshal.GetDelegateForFunctionPointer(p,typeof(T));}
 static void Assert(string phase,IntPtr coord,int expected){int actual=Marshal.ReadInt32(coord,4);Log(phase+" expected="+expected+" actual="+actual+" physicalX="+Marshal.ReadInt32(coord,8)+" bitmap="+Marshal.ReadIntPtr(outRec,0x32).ToString("X"));if(actual!=expected)failures++;}
 static void Basis(IntPtr current,IntPtr state,int railMeters,int physicalMeters){
  IntPtr owner=IntPtr.Subtract(state,8);if(current!=IntPtr.Add(owner,0x91))throw new Exception("Pointer guard failed");
  IntPtr coord=IntPtr.Add(state,0x34);B(state,0,0);I(coord,0,railMeters);I(coord,4,railMeters);I(coord,8,physicalMeters);B(coord,12,1);I(coord,13,physicalMeters);D(coord,17,0);I(coord,25,railMeters);I(owner,0xfd,1);B(owner,0x101,1);
 }
 static void Pump(StepFn step,int n){for(int i=0;i<n;i++){byte result=step();if(result!=1)throw new Exception("ProceedKlub returned "+result);Application.DoEvents();}}
 [STAThread] static int Main(string[] args){
  string owned=AppDomain.CurrentDomain.BaseDirectory;logPath=Path.Combine(owned,"coordinate_probe_log.txt");CloseFn close=null;bool ready=false;
  try {
   Log("Probe start; hidden native Type2; no bitmap; no attachment to live host");
   string resources=Path.GetFullPath(Path.Combine(owned,@"..\..\trenager_static\WORK_TRENAGER_3D\RESOURCES"));
   string dataRoot=Path.Combine(owned,"probe_root")+"\\";Directory.SetCurrentDirectory(dataRoot);SetDllDirectory(resources);
   button=Alloc(92);pbButton=Alloc(26);P(button,88,pbButton);P(pbButton,0,Alloc(16));B(pbButton,4,1);
   input=Alloc(52);F(input,1,0);I(input,5,80);I(input,9,80);F(input,13,2068);I(input,17,2068);P(input,31,Alloc(59));F(input,35,1);I(input,44,75);I(input,48,6000);
   outRec=Alloc(106);loc=Alloc(264);I(loc,0,1);B(loc,4,2);F(loc,5,8);F(loc,9,5);B(loc,0x99,1);wagon=Alloc(243);I(wagon,0x25,200);F(wagon,0,5);F(wagon,8,5);
   pb=Alloc(24);P(pb,0,button);P(pb,4,Alloc(19));P(pb,8,input);P(pb,12,outRec);P(pb,16,loc);P(pb,20,wagon);
   IntPtr module=LoadLibrary(Path.Combine(resources,"KLUB_U_LCD_XE2.dll"));if(module==IntPtr.Zero)throw new Exception("LoadLibrary error="+Marshal.GetLastWin32Error());
   byte result=Export<InitFn>(module,"InitKlub_U_LCD")(pb,new KlubInit{Path=Marshal.StringToHGlobalUni(dataRoot),Type=2,Show=0,TipDv=1,HandleFlag=0},new KlubState{Map=0,X=2147432,Cassette=1});Log("Init result="+result);if(result!=1)throw new Exception("Init failed");ready=true;close=Export<CloseFn>(module,"CloseKlub");StepFn step=Export<StepFn>(module,"ProceedKlub");
   IntPtr current,state;Export<PointersFn>(module,"GetParamInToKLUB")(out current,out state);if(current==IntPtr.Zero||state==IntPtr.Zero)throw new Exception("GetParam pointers missing");IntPtr coord=IntPtr.Add(state,0x34);
   Log("Exported pointer guard="+(current==IntPtr.Add(IntPtr.Subtract(state,8),0x91)));
   Basis(current,state,2147432,2068);Pump(step,3);Assert("initial",coord,2147432);
   F(input,13,2078);I(input,17,2078);Pump(step,3);Assert("forward10m",coord,2147422);
   B(pbButton,4,0);Pump(step,2);Assert("poweroff",coord,0);
   B(pbButton,4,1);Basis(current,state,2147422,2078);Pump(step,3);Assert("repowerbasis",coord,2147422);
   F(input,13,2088);I(input,17,2088);Pump(step,3);Assert("repowerforward10m",coord,2147412);
   F(input,13,30000);I(input,17,30000);Basis(current,state,2119500,30000);Pump(step,3);Assert("stationjumpbasis",coord,2119500);
   F(input,13,30010);I(input,17,30010);Pump(step,3);Assert("stationjumpforward10m",coord,2119490);
   Log("Probe completed failures="+failures);return failures==0?0:2;
  }catch(Exception ex){Log(ex.ToString());return 1;}finally{if(ready&&close!=null){try{close();Log("Native closed");}catch(Exception ex){Log("Close exception="+ex);}}}
 }
}
