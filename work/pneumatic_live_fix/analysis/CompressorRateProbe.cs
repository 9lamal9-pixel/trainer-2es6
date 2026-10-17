using System;using System.IO;using System.Text;using System.Runtime.InteropServices;using System.Globalization;
class PneumaticProbe {
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)]static extern IntPtr LoadLibrary(string p);
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)]static extern bool SetDllDirectory(string p);
 [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetProcAddress(IntPtr h,string n);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte Init();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte InitTrain(IntPtr train);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void Step(IntPtr train);
 static T Ex<T>(IntPtr h,string name)where T:class{return Marshal.GetDelegateForFunctionPointer(GetProcAddress(h,name),typeof(T))as T;}
 static IntPtr A(int n){var p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);return p;}
 static void B(IntPtr p,int o,int v){Marshal.WriteByte(p,o,(byte)v);}
 static void I(IntPtr p,int o,int v){Marshal.WriteInt32(p,o,v);}
 static void F(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 static void D(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}
 static float R(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static byte[] Hex(string h){byte[] b=new byte[h.Length/2];for(int i=0;i<b.Length;i++)b[i]=Convert.ToByte(h.Substring(i*2,2),16);return b;}
 static int Main(string[]args){try{
  string root=@"C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES";SetDllDirectory(root);IntPtr h=LoadLibrary(Path.Combine(root,"TormModel.dll"));if(h==IntPtr.Zero)throw new Exception("Load failed");
  int km=int.Parse(args[1]),compressor=int.Parse(args[2]);float tm=float.Parse(args[3],CultureInfo.InvariantCulture),ur=float.Parse(args[4],CultureInfo.InvariantCulture);int cycles=int.Parse(args[5]);
  IntPtr train=A(70),loc=A(264),array=A(76*8);IntPtr[]cars=new IntPtr[76];I(train,0,76);I(train,0x37,80);Marshal.WriteIntPtr(train,0x42,array);Marshal.WriteIntPtr(array,0,loc);
  byte[]vr=Hex("cdccccccccccf43f000000000000e03f333333333333e33f0000000000001640cdccccccccccec3f666666666666e63f14ae47e17a141240666666666666e63f66666666666606409a9999999999e93fcdccccccccccec3f000000000000004033333333333305400000000000001240b81e85eb51b81040333333333333fb3f6666666666660640");
  for(int n=0;n<76;n++){IntPtr w=cars[n]=A(243);Marshal.WriteIntPtr(array,n*8+4,w);F(w,0,tm);F(w,4,0);F(w,8,5);I(w,0x25,n==0?200:80);B(w,0x18,2);B(w,0x1a,1);B(w,0x1b,1);B(w,0x20,n==0?0:1);B(w,0x21,n==75?0:1);B(w,0x22,n==0?0:1);B(w,0x23,n==75?0:1);D(w,0xcb,n==0?20.8:12.8);I(w,0xd3,29);D(w,0xd7,78);D(w,0xdf,12.52);IntPtr vp=A(vr.Length);Marshal.Copy(vr,0,vp,vr.Length);Marshal.WriteIntPtr(w,0xe8,vp);B(w,0xf1,1);B(w,0xf2,n==0?0:1);}
  I(loc,0,1);B(loc,4,2);F(loc,5,7);F(loc,9,ur);D(loc,0x13,5);I(loc,0x1b,100);D(loc,0x3f,9);D(loc,0x47,7.5);D(loc,0x4f,double.Parse(args[9],CultureInfo.InvariantCulture));D(loc,0x57,2000);D(loc,0x5f,double.Parse(args[9],CultureInfo.InvariantCulture));I(loc,0x79,150);B(loc,0x7d,km);B(loc,0x7e,2);B(loc,0x80,0);B(loc,0x81,0);B(loc,0x8e,1);B(loc,0x8f,1);B(loc,0x90,1);B(loc,0xa8,1);F(loc,0x9f,8);
  if(args.Length>6)B(loc,int.Parse(args[6],NumberStyles.HexNumber),compressor);
  if(Ex<Init>(h,"InitNew")()!=1||Ex<InitTrain>(h,"InitSostavClass")(train)!=1)throw new Exception("Init failed");
  if(args.Length>7){float disturbed=float.Parse(args[7],CultureInfo.InvariantCulture);foreach(IntPtr w in cars)F(w,0,disturbed);}
  if(args.Length>8)B(loc,0x7e,int.Parse(args[8]));
  Step step=Ex<Step>(h,"ProceedTormClass");var csv=new StringBuilder("cycle,km,gr,ur,head_tm,head_tc,head_zr,tail_tm,tail_tc,p_tc_loc,gr_prog\n");
  for(int n=0;n<=cycles;n++){if(n%100==0||n<5)csv.AppendFormat(CultureInfo.InvariantCulture,"{0},{1},{2:R},{3:R},{4:R},{5:R},{6:R},{7:R},{8:R},{9:R},{10}\n",n,Marshal.ReadByte(loc,0x7d),R(loc,5),R(loc,9),R(cars[0],0),R(cars[0],4),R(cars[0],8),R(cars[75],0),R(cars[75],4),R(loc,13),Marshal.ReadByte(loc,0x12));if(n<cycles)step(train);}
  string path=Path.Combine(AppDomain.CurrentDomain.BaseDirectory,args[0]+".csv");File.WriteAllText(path,csv.ToString());Console.WriteLine("Final PM="+R(loc,5));return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}



