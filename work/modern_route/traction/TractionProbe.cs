using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
class TractionProbe {
 [DllImport("kernel32",CharSet=CharSet.Unicode,SetLastError=true)]static extern IntPtr LoadLibrary(string p);
 [DllImport("kernel32",SetLastError=true)]static extern IntPtr VirtualAlloc(IntPtr p,UIntPtr n,uint flags,uint protect);
 [DllImport("kernel32",CharSet=CharSet.Ansi)]static extern IntPtr GetProcAddress(IntPtr h,string name);
 [DllImport("msvcrt.dll",CallingConvention=CallingConvention.Cdecl,EntryPoint="_clearfp")]static extern uint ClearFp();
 [DllImport("msvcrt.dll",CallingConvention=CallingConvention.Cdecl,EntryPoint="_controlfp")]static extern uint ControlFp(uint value,uint mask);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte InitFn(IntPtr p);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void StepFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate float CalcFn(IntPtr self,float x);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int IntFn(IntPtr self);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void RegFn(IntPtr self,int value);
 static IntPtr A(int n){var p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);return p;}
 static void I(IntPtr p,int o,int v){Marshal.WriteInt32(p,o,v);}static void B(IntPtr p,int o,int v){Marshal.WriteByte(p,o,(byte)v);}static void F(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}static void D(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}static float R(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static T Export<T>(IntPtr h,string name){return (T)(object)Marshal.GetDelegateForFunctionPointer(GetProcAddress(h,name),typeof(T));}
 static T Wrap<T>(IntPtr address,bool argument,bool strict){var b=new System.Collections.Generic.List<byte>();b.AddRange(new byte[]{0x55,0x8b,0xec,0x83,0xec,0x04});if(strict)b.AddRange(new byte[]{0xd9,0x7d,0xfc,0x0f,0xb7,0x55,0xfc,0x81,0xe2,0xfa,0xff,0x00,0x00,0x66,0x89,0x55,0xfe,0xd9,0x6d,0xfe});b.AddRange(new byte[]{0x8b,0x45,0x08});if(argument)b.AddRange(new byte[]{0xff,0x75,0x0c});b.Add(0xba);b.AddRange(BitConverter.GetBytes(address.ToInt32()));b.AddRange(new byte[]{0xff,0xd2});if(strict)b.AddRange(new byte[]{0xdb,0xe2,0xd9,0x6d,0xfc});b.AddRange(new byte[]{0x8b,0xe5,0x5d,0xc2});b.AddRange(BitConverter.GetBytes((short)(argument?8:4)));var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)b.Count,0x3000,0x40);Marshal.Copy(b.ToArray(),0,p,b.Count);return (T)(object)Marshal.GetDelegateForFunctionPointer(p,typeof(T));}
 static RegFn WrapReg(IntPtr address){var b=new System.Collections.Generic.List<byte>(new byte[]{0x8b,0x44,0x24,0x04,0x8b,0x54,0x24,0x08,0xb9});b.AddRange(BitConverter.GetBytes(address.ToInt32()));b.AddRange(new byte[]{0xff,0xd1,0xc2,0x08,0x00});var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)b.Count,0x3000,0x40);Marshal.Copy(b.ToArray(),0,p,b.Count);return (RegFn)Marshal.GetDelegateForFunctionPointer(p,typeof(RegFn));}
 static void Main(string[] args){string kind=args.Length>1?args[1]:"valid";bool strict=kind.Contains("strict");var log=new StringBuilder();var h=LoadLibrary(Path.GetFullPath(args[0]));if(h==IntPtr.Zero)throw new Exception("load="+Marshal.GetLastWin32Error());var t=A(305);var r=A(26);Marshal.WriteIntPtr(t,0x27,r);I(t,8,2);F(t,0x17,1);I(t,0x1b,1);B(t,0x130,1);for(int n=0;n<4;n++){B(t,0x1f+n,1);B(t,0x23+n,1);B(t,0x109+n,1);}var init=Export<InitFn>(h,"Init_Tyga");log.AppendLine("init="+init(t));var slot=Marshal.ReadIntPtr(IntPtr.Add(h,0x19058c));var owner=Marshal.ReadIntPtr(slot);var self=Marshal.ReadIntPtr(owner,0x420);var sequential=Wrap<CalcFn>(IntPtr.Add(h,0x179d74),true,strict);var percent=Wrap<IntFn>(IntPtr.Add(h,0x174750),false,strict);var indep=Wrap<CalcFn>(IntPtr.Add(h,0x17823c),true,strict);
 var outfile=Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"probe_"+kind+"_"+Path.GetFileNameWithoutExtension(args[0])+".csv");log.AppendLine("case,mode,pos,speed_kmh,pairs,I_return,force10kN,Ia,percent,exception");
 if(kind.StartsWith("cycle")){
  var run=Wrap<IntFn>(IntPtr.Add(h,0x17c320),false,strict);var setPercent=WrapReg(IntPtr.Add(h,0x17e1cc));var close=Export<StepFn>(h,"CloseTygaDll");File.WriteAllText(outfile,"case,mode,speed_kmh,pairs,calls,force10kN,Ia,Iexc,percent,input_v_mps,revers,local_v_mps,exception"+Environment.NewLine);
  foreach(string scenario in new string[]{"coast","traction","recup"})foreach(int mm in new int[]{1,2,3})foreach(float vv in new float[]{0,0.1f,3,16,38,69,80,120}){
   close();init(t);owner=Marshal.ReadIntPtr(slot);self=Marshal.ReadIntPtr(owner,0x420);int count=kind.Contains("zero")?0:4;for(int n=0;n<8;n++){I(t,0x10d+n*4,n<count?1:0);F(t,0x7b+n*4,scenario=="coast"?0:250);F(t,0x3b+n*4,0);}for(int n=0;n<4;n++)B(t,0x1f+n,scenario=="coast"?0:1);
   // Native traction uses signed metres/second. KLUB/safety km/h inputs are
   // deliberately outside this isolated probe. A reverse run uses negative V
   // on the fixed physical route axis; SetVCur then maps it into local forward V.
   int revers=kind.Contains("reverse")?-1:1;float nativeSpeedMps=vv/3.6f;if(revers<0&&!kind.Contains("wrong"))nativeSpeedMps=-nativeSpeedMps;
   D(t,0,nativeSpeedMps);I(t,0x1b,revers);B(t,0x16,1);B(t,0x14,0);B(t,0x15,2);B(t,0x130,1);B(self,0x40,mm);B(self,0xb,scenario=="traction"?(mm==1?23:mm==2?44:65):0);F(self,0x104,revers*nativeSpeedMps);setPercent(self,scenario=="traction"?50:scenario=="recup"?-50:0);string error="";int calls=0;File.AppendAllText(outfile,"before,"+scenario+","+mm+","+vv+Environment.NewLine);try{for(int n=0;n<100;n++){run(self);calls++;}}catch(Exception e){ClearFp();ControlFp(0x8001f,0x8001f);error=e.GetType().Name;}File.AppendAllText(outfile,string.Format(System.Globalization.CultureInfo.InvariantCulture,"{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12}",scenario,mm,vv,count,calls,R(r,0),R(t,0x3b),R(t,0x7b),Marshal.ReadInt32(t,0xdd),nativeSpeedMps,revers,R(self,0x104),error)+Environment.NewLine);
  }close();return;
 }
 int[] mode={1,2,3};int[] pos={23,44,65};float[] speed={0,0.1f,5,20,40,80,120};
 File.WriteAllText(outfile,log.ToString());
 foreach(int mm in mode)foreach(float vv in speed){int pp=pos[Array.IndexOf(mode,mm)];int count=kind.Contains("zero")?0:kind.Contains("partial")?2:4;for(int n=0;n<8;n++){I(t,0x10d+n*4,n<count?1:0);F(t,0x7b+n*4,600);}B(self,0x40,mm);B(self,0xb,pp);F(self,0x104,vv/3.6f);F(r,0x11,3200);float current=0;int pct=0;string ex="";File.AppendAllText(outfile,"before,"+mm+","+pp+","+vv+","+count+Environment.NewLine);try{if(kind.StartsWith("percent"))current=0;else current=kind.StartsWith("indep")?indep(self,600):sequential(self,0);pct=percent(self);}catch(Exception e){ClearFp();ControlFp(0x8001f,0x8001f);ex=e.GetType().Name;}File.AppendAllText(outfile,string.Format(System.Globalization.CultureInfo.InvariantCulture,"{0},{1},{2},{3},{4},{5},{6},{7},{8},{9}",kind.StartsWith("indep")?"independent":"sequential",mm,pp,vv,count,current,R(r,0),R(t,0x3b),pct,ex)+Environment.NewLine);}
 Export<StepFn>(h,"CloseTygaDll")();
 }
}
