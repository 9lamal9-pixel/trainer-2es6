using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// Read-only view of the actual bundled KLUB owner and shared native outputs.
public sealed class TrainerKlubSafetyBinding {
    const string Sha="671D3BDB1AD57BD8EA7AFC714B11BC14141A5B2750D72E8B5598C7BB0CFAF20F";
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void PointersFn(out IntPtr current,out IntPtr initial);
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {public IntPtr Base,AllocationBase;public uint AllocationProtect;public UIntPtr RegionSize;public uint State,Protect,Type;}
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module,StringBuilder path,int capacity);
    [DllImport("kernel32.dll",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module,string name);
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr p,out MemoryInfo info,UIntPtr size);
    readonly uint thread;readonly string path;readonly IntPtr module,slot,owner,vmt,output;
    readonly Func<bool> lifetime;readonly PointersFn pointers;
    public TrainerKlubSafetyBinding(IntPtr nativeModule,string expectedPath,IntPtr commonOutput,Func<bool> alive) {
        if(IntPtr.Size!=4||nativeModule==IntPtr.Zero||commonOutput==IntPtr.Zero||alive==null)throw new Exception("KLUB binding source absent");
        thread=GetCurrentThreadId();module=nativeModule;path=Path.GetFullPath(expectedPath);output=commonOutput;lifetime=alive;
        GuardModule();using(var hash=SHA256.Create())if(BitConverter.ToString(hash.ComputeHash(File.ReadAllBytes(path))).Replace("-","")!=Sha)throw new Exception("KLUB SHA guard");
        Range(IntPtr.Add(module,0x1d7028),4,module);slot=Marshal.ReadIntPtr(module,0x1d7028);Range(slot,4,IntPtr.Zero);
        owner=Marshal.ReadIntPtr(slot);Range(owner,0x132,IntPtr.Zero);vmt=Marshal.ReadIntPtr(owner);
        var entry=GetProcAddress(module,"GetParamInToKLUB");Range(entry,1,module);
        pointers=(PointersFn)Marshal.GetDelegateForFunctionPointer(entry,typeof(PointersFn));Guard();
    }
    public IntPtr Owner {get{Guard();return owner;}}
    public int PeriodSeconds {get{Guard();return Marshal.ReadInt32(owner,0x11c);}}
    public int Counter {get{Guard();return Marshal.ReadInt32(owner,0x85);}}
    public bool Alarm {get{Guard();return Marshal.ReadByte(output,0x15)==1;}}
    public bool Pisk {get{Guard();return Marshal.ReadByte(output,0x16)==1;}}
    public byte Signal {get{Guard();return Marshal.ReadByte(output,0x20);}}
    public string RequestedButton {get{Guard();return Marshal.ReadByte(owner,0x131)==0?"РБС":"РБ / РБС / РБП";}}
    public void Guard() {
        if(GetCurrentThreadId()!=thread||!lifetime())throw new Exception("KLUB owner lifetime/thread guard");GuardModule();
        Range(slot,4,IntPtr.Zero);if(Marshal.ReadIntPtr(module,0x1d7028)!=slot||Marshal.ReadIntPtr(slot)!=owner)throw new Exception("KLUB global owner guard");
        Range(owner,0x132,IntPtr.Zero);if(Marshal.ReadIntPtr(owner)!=vmt)throw new Exception("KLUB VMT changed");
        Range(IntPtr.Add(vmt,-56),60,module);var name=Marshal.ReadIntPtr(vmt,-56);Range(name,13,module);
        if(Marshal.ReadByte(name)!=12)throw new Exception("KLUB class size guard");var bytes=new byte[12];Marshal.Copy(IntPtr.Add(name,1),bytes,0,12);
        if(Encoding.ASCII.GetString(bytes)!="TProceedKLUB")throw new Exception("KLUB class guard");
        IntPtr current,initial;pointers(out current,out initial);
        if(current!=IntPtr.Add(owner,0x91)||initial!=IntPtr.Add(owner,8))throw new Exception("KLUB exported state guard");
        Range(output,106,IntPtr.Zero);var shared=Marshal.ReadIntPtr(owner,4);Range(shared,24,IntPtr.Zero);
        if(Marshal.ReadIntPtr(shared,12)!=output)throw new Exception("KLUB actual output producer alias guard");
    }
    void GuardModule(){if(GetModuleHandle("KLUB_U_LCD_XE2.dll")!=module)throw new Exception("KLUB module lifetime guard");var b=new StringBuilder(32768);uint n=GetModuleFileName(module,b,b.Capacity);
        if(n==0||n>=b.Capacity||!String.Equals(Path.GetFullPath(b.ToString()),path,StringComparison.OrdinalIgnoreCase))throw new Exception("KLUB loaded path guard");}
    static void Range(IntPtr p,int count,IntPtr allocation){MemoryInfo m;
        if(p==IntPtr.Zero||VirtualQuery(p,out m,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)throw new Exception("KLUB range absent");
        uint a=unchecked((uint)p.ToInt32()),b=unchecked((uint)m.Base.ToInt32()),protection=m.Protect&0xff;
        if(m.State!=0x1000||(m.Protect&0x101)!=0||a<b||(ulong)a+(uint)count>(ulong)b+m.RegionSize.ToUInt32()||
            !(protection==2||protection==4||protection==8||protection==0x20||protection==0x40||protection==0x80)||
            (allocation!=IntPtr.Zero&&(m.AllocationBase!=allocation||m.Type!=0x1000000)))throw new Exception("KLUB memory/module range guard");}
}
