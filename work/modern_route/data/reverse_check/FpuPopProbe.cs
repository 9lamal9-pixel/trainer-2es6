using System;
using System.Runtime.InteropServices;

class FpuPopProbe {
    [DllImport("kernel32.dll",SetLastError=true)]
    static extern IntPtr VirtualAlloc(IntPtr address, UIntPtr size,uint type,uint protect);
    [DllImport("kernel32.dll",SetLastError=true)]
    static extern bool VirtualFree(IntPtr address,UIntPtr size,uint type);
    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    delegate void Run(IntPtr values);
    static void WriteDouble(IntPtr p,int offset,double value){Marshal.Copy(BitConverter.GetBytes(value),0,IntPtr.Add(p,offset),8);}
    static double ReadDouble(IntPtr p,int offset){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
    static int Test(bool discard,int count){
        // Same two-byte x87 instruction as Communication1 RVA 0x2975EF;
        // simulate its preceding load and inspect the x87 stack TOP around it.
        byte[] code={0x55,0x8b,0xec,0x53,0x8b,0x5d,0x08,
                     0xdf,0xe0,0x89,0x03,0xdd,0x43,0x08,
                     0x8b,0xc3,0x83,0xc0,0x10,
                     0xdd,(byte)(discard?0xd8:0x18),0x9b,
                     0xdf,0xe0,0x89,0x43,0x04,0x5b,0x8b,0xe5,0x5d,0xc3};
        IntPtr executable=VirtualAlloc(IntPtr.Zero,(UIntPtr)code.Length,0x3000,0x40);
        if(executable==IntPtr.Zero)throw new Exception("VirtualAlloc failed");
        IntPtr values=Marshal.AllocHGlobal(24);
        try {
            Marshal.Copy(code,0,executable,code.Length);
            Run run=(Run)Marshal.GetDelegateForFunctionPointer(executable,typeof(Run));
            int errors=0;
            for(int i=0;i<count;i++){
                double source=6.25+(i%100)*0.125, signed=-source;
                WriteDouble(values,8,source);WriteDouble(values,16,signed);
                run(values);
                int before=Marshal.ReadInt32(values)&0xffff, after=Marshal.ReadInt32(values,4)&0xffff;
                if(((before>>11)&7)!=((after>>11)&7))errors++;
                if((after&0x41)!=0)errors++;
                if(ReadDouble(values,16)!=(discard?signed:source))errors++;
            }
            Console.WriteLine("instruction="+(discard?"DD D8 fstp st(0)":"DD 18 fstp qword [eax]")+", loops="+count+", mismatches="+errors);
            return errors;
        }finally {Marshal.FreeHGlobal(values);VirtualFree(executable,UIntPtr.Zero,0x8000);}
    }
    static int Main(){try{return Test(false,100000)+Test(true,100000)==0?0:1;}catch(Exception ex){Console.WriteLine(ex);return 2;}}
}
