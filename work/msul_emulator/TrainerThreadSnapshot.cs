using System;using System.Runtime.InteropServices;
class Snapshot {
 [DllImport("kernel32")]static extern IntPtr OpenProcess(uint a,bool b,uint p);
 [DllImport("kernel32")]static extern IntPtr OpenThread(uint a,bool b,uint p);
 [DllImport("kernel32")]static extern uint SuspendThread(IntPtr h);
 [DllImport("kernel32")]static extern uint ResumeThread(IntPtr h);
 [DllImport("kernel32")]static extern bool Wow64GetThreadContext(IntPtr h,IntPtr p);
 [DllImport("kernel32")]static extern bool ReadProcessMemory(IntPtr h,IntPtr a,byte[] b,int n,out IntPtr count);
 [DllImport("kernel32")]static extern bool CloseHandle(IntPtr h);
 static uint Read(IntPtr p,uint a){byte[] b=new byte[4];IntPtr n;if(!ReadProcessMemory(p,new IntPtr(a),b,4,out n))return 0;return BitConverter.ToUInt32(b,0);}
 static void Main(string[] args){uint pid=uint.Parse(args[0]),tid=uint.Parse(args[1]);var p=OpenProcess(0x1010,false,pid);var h=OpenThread(0x1a,false,tid);var ctx=Marshal.AllocHGlobal(716);Marshal.Copy(new byte[716],0,ctx,716);Marshal.WriteInt32(ctx,0,0x10007);uint suspend=SuspendThread(h);if(suspend==0xffffffff)throw new Exception("Cannot suspend requested trainer thread");try{if(!Wow64GetThreadContext(h,ctx))throw new Exception("Cannot read trainer context");uint ip=(uint)Marshal.ReadInt32(ctx,184),sp=(uint)Marshal.ReadInt32(ctx,196),bp=(uint)Marshal.ReadInt32(ctx,180);Console.WriteLine("EIP={0:X8} ESP={1:X8} EBP={2:X8}",ip,sp,bp);for(int i=0;i<80&&bp!=0;i++){uint next=Read(p,bp),ret=Read(p,bp+4);Console.WriteLine("frame {0}: return={1:X8} next={2:X8}",i,ret,next);if(next<=bp||next-bp>1048576)break;bp=next;}}finally{ResumeThread(h);CloseHandle(h);CloseHandle(p);Marshal.FreeHGlobal(ctx);}}
}