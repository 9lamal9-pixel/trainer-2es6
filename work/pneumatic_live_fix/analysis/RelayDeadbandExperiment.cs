using System;using System.Runtime.InteropServices;using System.Collections.Generic;
// Diagnostic experiment only: redirects three relay deadband operands in this process.
// Not a production calibration or claim that 0.03 is a physical specification.
class RelayDeadbandExperiment:IDisposable {
[DllImport("kernel32.dll",CharSet=CharSet.Unicode)]static extern IntPtr GetModuleHandle(string n);
[DllImport("kernel32.dll")]static extern bool VirtualProtect(IntPtr p,UIntPtr n,uint prot,out uint previous);
[DllImport("kernel32.dll")]static extern IntPtr GetCurrentProcess();
[DllImport("kernel32.dll")]static extern bool FlushInstructionCache(IntPtr process,IntPtr p,UIntPtr n);
IntPtr value;List<IntPtr> sites=new List<IntPtr>();List<byte[]> originals=new List<byte[]>();
static void Write(IntPtr p,byte[] b){uint old;if(!VirtualProtect(p,(UIntPtr)b.Length,0x40,out old))throw new Exception("Protect failed");try{Marshal.Copy(b,0,p,b.Length);if(!FlushInstructionCache(GetCurrentProcess(),p,(UIntPtr)b.Length))throw new Exception("Flush failed");}finally{uint ignored;VirtualProtect(p,(UIntPtr)b.Length,old,out ignored);}}
public RelayDeadbandExperiment(double threshold,bool coherent=false){if(threshold<=0||threshold>=.06)throw new Exception("Diagnostic threshold range");var module=GetModuleHandle("TormModel.dll");if(module==IntPtr.Zero)throw new Exception("Missing module");int exponent=(int)Math.Floor(Math.Log(threshold,2));ulong mantissa=(ulong)(threshold/Math.Pow(2,exponent)*Math.Pow(2,63));byte[] extended=new byte[10];Array.Copy(BitConverter.GetBytes(mantissa),extended,8);Array.Copy(BitConverter.GetBytes((ushort)(exponent+16383)),0,extended,8,2);value=Marshal.AllocHGlobal(20);Marshal.Copy(extended,0,value,10);Array.Copy(BitConverter.GetBytes((ushort)(exponent+16384)),0,extended,8,2);Marshal.Copy(extended,0,IntPtr.Add(value,10),10);try{foreach(int rva in (coherent?new int[]{0x179f2a,0x179f88,0x179fb6,0x179f67,0x179f9f}:new int[]{0x179f2a,0x179f88,0x179fb6})){bool upper=rva==0x179f67||rva==0x179f9f;var instruction=IntPtr.Add(module,rva);if(Marshal.ReadByte(instruction)!=0xdb||Marshal.ReadByte(instruction,1)!=0x2d||Marshal.ReadInt32(instruction,2)!=IntPtr.Add(module,upper?0x17a8c8:0x17a8bc).ToInt32())throw new Exception("Relay operand guard");var site=IntPtr.Add(instruction,2);byte[] old=new byte[4];Marshal.Copy(site,old,0,4);sites.Add(site);originals.Add(old);Write(site,BitConverter.GetBytes(IntPtr.Add(value,upper?10:0).ToInt32()));}}catch{Dispose();throw;}}
public void Dispose(){for(int n=sites.Count-1;n>=0;n--)Write(sites[n],originals[n]);sites.Clear();if(value!=IntPtr.Zero){Marshal.FreeHGlobal(value);value=IntPtr.Zero;}}
}


