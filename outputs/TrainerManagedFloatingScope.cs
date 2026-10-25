using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Security.Cryptography;

// Thread-owned bridge between Delphi's retained x87 environment and CLR maths.
// The only executable writes are to this class's private allocation, never a DLL.
internal sealed class TrainerManagedFloatingScope : IDisposable {
    const int BufferBytes=128, ImageBytes=112, CodeBytes=128, RestoreOffset=64;
    static readonly object Gate=new object();
    static readonly List<Owner> Owners=new List<Owner>();
    // EAX=[ESP+4]; FNSAVE [EAX]; STMXCSR [EAX+108]; managed MXCSR=1F80;
    // managed x87 CW=027F (53-bit, nearest, all exceptions masked); RET 4.
    static readonly byte[] SaveCode=Hex("8B442404DD300FAE586CC74070801F00000FAE507066C740747F02D96874C20400");
    static readonly byte[] RestoreCode=Hex("8B442404DD200FAE506CC20400");
    static IntPtr code;
    static EnvironmentThunk save,restore;
    [ThreadStatic] static Owner current;
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void EnvironmentThunk(IntPtr buffer);
    sealed class Owner {
        internal uint Thread; internal IntPtr Buffer; internal Owner Previous;
        internal int Depth; internal bool Saved, Native, Released;
        internal string Fault="", ImageSha="";
    }
    readonly Owner owner;
    readonly int level;
    bool disposed;
    TrainerManagedFloatingScope(Owner value,int depth) {owner=value;level=depth;}

    public static IDisposable Enter() {
        RequireX86(); uint thread=GetCurrentThreadId();
        if(current!=null) {
            CheckOwner(current);
            if(current.Fault.Length!=0)throw new InvalidOperationException("Floating-point scope is retained after a failure: "+current.Fault);
        }
        if(current!=null && !current.Native) {
            if(current.Depth==Int32.MaxValue)throw new InvalidOperationException("Floating-point scope nesting exhausted.");
            int depth=++current.Depth; return new TrainerManagedFloatingScope(current,depth);
        }
        EnsureHelpers();
        var next=new Owner {Thread=thread,Buffer=Marshal.AllocHGlobal(BufferBytes),Previous=current,Depth=1};
        for(int i=0;i<BufferBytes;i++)Marshal.WriteByte(next.Buffer,i,0);
        lock(Gate)Owners.Add(next);
        current=next;
        try {SaveNative(next);return new TrainerManagedFloatingScope(next,1);}
        catch(Exception error) {next.Fault=error.Message;throw;}
    }

    // Outside a managed scope this is an ordinary native call. Inside one it
    // lends the latest native environment, then captures its updated state.
    public static void RunNative(Action action) {
        if(action==null)throw new ArgumentNullException("action");
        var value=current;
        if(value==null) {action();return;}
        CheckOwner(value);
        if(value.Fault.Length!=0)throw new InvalidOperationException("Cannot lend a faulted floating-point scope.");
        if(value.Native) {action();return;}
        RestoreNative(value); value.Native=true;
        try {action();}
        finally {
            try {
                if(!Object.ReferenceEquals(current,value))throw new InvalidOperationException("A native callback left a nested floating-point scope open.");
                SaveNative(value);value.Native=false;
            } catch(Exception error) {value.Fault=error.Message;throw;}
        }
    }

    // Snapshots report the saved native image; they do not execute floating-point
    // instructions to inspect the active CLR environment.
    public static Dictionary<string,object> Snapshot() {
        var value=current;
        if(value==null)return new Dictionary<string,object>{{"Active",false},{"RetainedOwners",OwnerCount()}};
        CheckOwner(value);
        if(value.Native)throw new InvalidOperationException("Snapshot requires the managed side of the scope.");
        var result=new Dictionary<string,object> {
            {"Active",true},{"Thread",value.Thread},{"Depth",value.Depth},{"Saved",value.Saved},
            {"NativeRunning",value.Native},{"Fault",value.Fault},{"RetainedOwners",OwnerCount()},
            {"ManagedControlWord",0x027f},{"ManagedMxcsr",0x1f80},{"NativeImageSha256",value.ImageSha}
        };
        if(value.Saved) {
            VerifyImage(value);
            result.Add("NativeControlWord",unchecked((ushort)Marshal.ReadInt16(value.Buffer,0)));
            result.Add("NativeStatusWord",unchecked((ushort)Marshal.ReadInt16(value.Buffer,4)));
            result.Add("NativeTagWord",unchecked((ushort)Marshal.ReadInt16(value.Buffer,8)));
            result.Add("NativeMxcsr",unchecked((uint)Marshal.ReadInt32(value.Buffer,108)));
        }
        return result;
    }

    public void Dispose() {
        if(disposed)return;
        CheckOwner(owner);
        if(!Object.ReferenceEquals(current,owner) || owner.Depth!=level || owner.Native)
            throw new InvalidOperationException("Floating-point scopes must close on their owner thread in reverse order, outside native callbacks.");
        if(level>1) {owner.Depth--;disposed=true;return;}
        CloseOwner(owner);disposed=true;
    }
    public static bool HasRetainedOwners {get {lock(Gate)return Owners.Count!=0;}}
    public static void DisposeRetainedOwners() {
        RequireX86();
        while(current!=null) {
            var value=current;CheckOwner(value);
            if(value.Depth!=1 || value.Native)throw new InvalidOperationException("Cannot clean up an active nested/native floating-point scope.");
            CloseOwner(value);
        }
        if(HasRetainedOwners)throw new InvalidOperationException("Floating-point owners on another thread remain retained.");
    }
    // Called only at final host shutdown, not on motion reset.
    public static void Shutdown() {
        RequireX86();lock(Gate) {
            if(Owners.Count!=0)throw new InvalidOperationException("Cannot free floating-point helpers while any environment is retained.");
            if(code==IntPtr.Zero)return;
            GuardHelpers();
            if(!VirtualFree(code,UIntPtr.Zero,0x8000))throw WinError("Cannot free owned floating-point helpers");
            code=IntPtr.Zero;save=null;restore=null;
        }
    }

    static void CloseOwner(Owner value) {
        try {
            RestoreNative(value);
            // No FP operations are performed after FRSTOR. Native state is now
            // live again; release bookkeeping uses integer/reference operations.
            Marshal.FreeHGlobal(value.Buffer);value.Buffer=IntPtr.Zero;value.Released=true;
            current=value.Previous;lock(Gate)Owners.Remove(value);
        } catch(Exception error) {value.Fault=error.Message;throw;}
    }
    static void SaveNative(Owner value) {
        CheckOwner(value);GuardHelpers();
        save(value.Buffer);value.Saved=true;
        value.ImageSha=ImageHash(value.Buffer);
    }
    static void RestoreNative(Owner value) {
        CheckOwner(value);
        if(!value.Saved)throw new InvalidOperationException("Native environment was not captured; buffer remains retained.");
        VerifyImage(value);GuardHelpers();restore(value.Buffer);
    }
    static void VerifyImage(Owner value) {
        if(!String.Equals(value.ImageSha,ImageHash(value.Buffer),StringComparison.Ordinal))
            throw new InvalidOperationException("Owned native floating-point image changed; buffer remains retained.");
    }
    static string ImageHash(IntPtr buffer) {
        var bytes=new byte[ImageBytes];Marshal.Copy(buffer,bytes,0,bytes.Length);
        using(var sha=SHA256.Create())return BitConverter.ToString(sha.ComputeHash(bytes)).Replace("-","");
    }
    static void CheckOwner(Owner value) {
        RequireX86();
        if(value==null || value.Released || value.Buffer==IntPtr.Zero || value.Thread!=GetCurrentThreadId())
            throw new InvalidOperationException("Floating-point environment owner/thread guard failed.");
    }
    static int OwnerCount() {lock(Gate)return Owners.Count;}
    static void RequireX86() {if(IntPtr.Size!=4)throw new InvalidOperationException("Floating-point scope requires Windows x86.");}
    static byte[] ExpectedCode() {
        var bytes=new byte[CodeBytes];for(int i=0;i<bytes.Length;i++)bytes[i]=0xcc;
        Buffer.BlockCopy(SaveCode,0,bytes,0,SaveCode.Length);
        Buffer.BlockCopy(RestoreCode,0,bytes,RestoreOffset,RestoreCode.Length);return bytes;
    }
    static void EnsureHelpers() {
        lock(Gate) {
            if(code!=IntPtr.Zero) {
                GuardHelpers();
                if(!FlushInstructionCache(GetCurrentProcess(),code,new UIntPtr(CodeBytes)))throw WinError("Cannot retry floating-point helper flush");
                BindHelpers();return;
            }
            code=VirtualAlloc(IntPtr.Zero,new UIntPtr(CodeBytes),0x3000,4);
            if(code==IntPtr.Zero)throw WinError("Cannot allocate owned floating-point helpers");
            // Publish the allocation immediately. A failed protection/flush/guard
            // never frees potentially live code and may be retried at Shutdown.
            var bytes=ExpectedCode();Marshal.Copy(bytes,0,code,bytes.Length);
            uint previous;
            if(!VirtualProtect(code,new UIntPtr(CodeBytes),0x20,out previous))throw WinError("Cannot protect floating-point helpers RX");
            if(!FlushInstructionCache(GetCurrentProcess(),code,new UIntPtr(CodeBytes)))throw WinError("Cannot flush floating-point helpers");
            GuardHelpers();
            BindHelpers();
        }
    }
    static void BindHelpers() {
        if(save==null)save=(EnvironmentThunk)Marshal.GetDelegateForFunctionPointer(code,typeof(EnvironmentThunk));
        if(restore==null)restore=(EnvironmentThunk)Marshal.GetDelegateForFunctionPointer(IntPtr.Add(code,RestoreOffset),typeof(EnvironmentThunk));
    }
    static void GuardHelpers() {
        if(code==IntPtr.Zero)throw new InvalidOperationException("Floating-point helper allocation absent.");
        MemoryInformation info;
        if(VirtualQuery(code,out info,new UIntPtr((uint)Marshal.SizeOf(typeof(MemoryInformation))))==UIntPtr.Zero)
            throw WinError("Cannot inspect floating-point helpers");
        if(info.AllocationBase!=code || info.State!=0x1000 || info.Protect!=0x20 || info.RegionSize.ToUInt64()<CodeBytes)
            throw new InvalidOperationException("Floating-point helpers lost owned RX protection.");
        var expected=ExpectedCode();var actual=new byte[CodeBytes];Marshal.Copy(code,actual,0,actual.Length);
        for(int i=0;i<CodeBytes;i++)if(actual[i]!=expected[i])throw new InvalidOperationException("Floating-point helper byte guard failed.");
    }
    static byte[] Hex(string text) {var bytes=new byte[text.Length/2];for(int i=0;i<bytes.Length;i++)bytes[i]=Convert.ToByte(text.Substring(i*2,2),16);return bytes;}
    static Exception WinError(string text) {return new Win32Exception(Marshal.GetLastWin32Error(),text);}
    [StructLayout(LayoutKind.Sequential)] struct MemoryInformation {internal IntPtr BaseAddress,AllocationBase;internal uint AllocationProtect;internal UIntPtr RegionSize;internal uint State,Protect,Type;}
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll")] static extern IntPtr GetCurrentProcess();
    [DllImport("kernel32.dll",SetLastError=true)] static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint type,uint protection);
    [DllImport("kernel32.dll",SetLastError=true)] [return:MarshalAs(UnmanagedType.Bool)] static extern bool VirtualProtect(IntPtr address,UIntPtr size,uint protection,out uint previous);
    [DllImport("kernel32.dll",SetLastError=true)] [return:MarshalAs(UnmanagedType.Bool)] static extern bool VirtualFree(IntPtr address,UIntPtr size,uint type);
    [DllImport("kernel32.dll",SetLastError=true)] [return:MarshalAs(UnmanagedType.Bool)] static extern bool FlushInstructionCache(IntPtr process,IntPtr address,UIntPtr size);
    [DllImport("kernel32.dll",SetLastError=true)] static extern UIntPtr VirtualQuery(IntPtr address,out MemoryInformation info,UIntPtr size);
}
