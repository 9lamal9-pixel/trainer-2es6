using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// Select the original native VirtPult configuration before InitCommunication.
// This owns one primary configuration byte, not a hardware/protection output.
// The caller must retain this scope until CloseCommunicationDll succeeds, then
// dispose it before the Communication module is unloaded. No registry API,
// native initialization, close, hardware pointer or executable memory is used.
public sealed class TrainerNativeVirtualPult : IDisposable {
    public const string CommunicationSha256 = "27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F";
    public const int VirtualPultOffset = 0x83;
    const int ConfigurationSize = 773;
    const int RegistryGlobalRva = 0x2c7530, RegistryCellRva = 0x2e1048;
    const int RegistryVmtRva = 0x1a7758, RegistryNameRva = 0x1a795c;
    const int CommunicationGlobalRva = 0x2c7068, CommunicationCellRva = 0x2e16b0;
    const int GetRitRva = 0x1aa0b0;
    static readonly byte[] GetRitBytes = Hex("558BEC83C4F88945FC8B45FC83C0048945F88B45F859595DC3");
    static readonly byte[] HardwareSelectorBytes = Hex("E8B733F1FF80B883000000000F852B010000");
    static readonly byte[] CabSelectorBytes = Hex("E8E39FEFFF80B883000000000F84A8000000");

    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {
        public IntPtr Base, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module, StringBuilder path, int capacity);
    [DllImport("kernel32.dll", CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module, string name);
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr p, out MemoryInfo info, UIntPtr size);

    readonly uint thread;
    readonly string path;
    readonly IntPtr module, registryCell, registryOwner, configuration, input;
    readonly Func<bool> communicationInactive;
    readonly Action<string> log;
    readonly byte original;
    bool owned, disposed;

    // communicationInactive must be true before the first InitCommunication,
    // false during initialization/operation, and true only after an actual
    // successful native close (or if no native initialization was attempted).
    public static TrainerNativeVirtualPult Attach(IntPtr communicationModule,
        string runtimeDir, Func<bool> communicationInactive, Action<string> log) {
        return new TrainerNativeVirtualPult(communicationModule, runtimeDir, communicationInactive, log);
    }

    TrainerNativeVirtualPult(IntPtr communicationModule, string runtimeDir,
        Func<bool> inactive, Action<string> logger) {
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Native virtual pult requires x86");
        if (communicationModule == IntPtr.Zero || String.IsNullOrWhiteSpace(runtimeDir) || inactive == null)
            throw new ArgumentException("Native virtual-pult configuration source is absent");
        thread=GetCurrentThreadId(); module=communicationModule;
        path=Path.GetFullPath(Path.Combine(runtimeDir,@"RESOURCES\LOCOMOTIVES\1\Communication1.dll"));
        communicationInactive=inactive; log=logger;
        GuardModule();
        using (var hash=SHA256.Create()) using (var stream=File.OpenRead(path))
            if (BitConverter.ToString(hash.ComputeHash(stream)).Replace("-","") != CommunicationSha256)
                throw new Exception("Virtual-pult Communication SHA guard");
        GuardCode();
        RequireModuleRange(IntPtr.Add(module,RegistryGlobalRva),4);
        registryCell=Marshal.ReadIntPtr(module,RegistryGlobalRva);
        if (registryCell != IntPtr.Add(module,RegistryCellRva))
            throw new Exception("Virtual-pult native registry cell guard");
        RequireModuleRange(registryCell,4);
        registryOwner=Marshal.ReadIntPtr(registryCell);
        // Exact native GetRIT is a 25-byte leaf returning Self+4. Its fixed
        // bytes above prove the inline record without executing a thunk.
        configuration=IntPtr.Add(registryOwner,4);
        input=IntPtr.Add(configuration,VirtualPultOffset);
        GuardSource();
        RequireInactive(); GuardBeforeInitialization();
        original=Marshal.ReadByte(input);
        try {
            owned=true;
            Marshal.WriteByte(input,1);
            if (Marshal.ReadByte(input)!=1) throw new Exception("Native VirtPult selection verification failed");
            SafeLog("Native virtual pult selected: actual TReestrTrenClass/GetRIT+83 input; hardware initialization branch skipped by original native code");
        } catch (Exception error) {
            try { RestoreInput(); disposed=true; }
            catch (Exception cleanup) { throw new AggregateException("Native VirtPult selection/restoration failed",error,cleanup); }
            throw;
        }
    }

    public bool Enabled { get { if(disposed)return false; GuardSource(); return owned && Marshal.ReadByte(input)==1; } }
    public bool IsDisposed { get { return disposed; } }
    public byte OriginalVirtualPult { get { ThreadGuard(); return original; } }
    public IntPtr RegistryOwner { get { ThreadGuard(); return registryOwner; } }
    public IntPtr Configuration { get { ThreadGuard(); return configuration; } }
    public IntPtr InputPointer { get { ThreadGuard(); return input; } }

    void GuardBeforeInitialization() {
        RequireModuleRange(IntPtr.Add(module,CommunicationGlobalRva),4);
        var cell=Marshal.ReadIntPtr(module,CommunicationGlobalRva);
        if(cell!=IntPtr.Add(module,CommunicationCellRva))
            throw new Exception("Virtual-pult Communication cell guard");
        RequireModuleRange(cell,4);
        if(Marshal.ReadIntPtr(cell)!=IntPtr.Zero)
            throw new Exception("VirtPult must be selected before native Communication owner creation");
    }
    void RequireInactive() {
        bool inactive;
        try { inactive=communicationInactive(); }
        catch(Exception error){throw new Exception("Virtual-pult Communication lifetime predicate failed",error);}
        if(!inactive)throw new Exception("VirtPult selection/restoration requires completed native Communication closure");
    }
    void GuardSource() {
        ThreadGuard(); GuardModule(); GuardCode();
        RequireModuleRange(IntPtr.Add(module,RegistryGlobalRva),4); RequireModuleRange(registryCell,4);
        if(Marshal.ReadIntPtr(module,RegistryGlobalRva)!=registryCell || Marshal.ReadIntPtr(registryCell)!=registryOwner)
            throw new Exception("Virtual-pult native registry owner changed");
        RequireRange(registryOwner,4,false,IntPtr.Zero);
        var vmt=Marshal.ReadIntPtr(registryOwner);
        if(vmt!=IntPtr.Add(module,RegistryVmtRva))throw new Exception("Virtual-pult registry VMT guard");
        RequireModuleRange(IntPtr.Add(vmt,-88),92);
        if(Marshal.ReadIntPtr(vmt,-88)!=vmt)throw new Exception("Virtual-pult registry VMT self guard");
        var name=Marshal.ReadIntPtr(vmt,-56);
        if(name!=IntPtr.Add(module,RegistryNameRva))throw new Exception("Virtual-pult registry class-name pointer guard");
        const string expected="TReestrTrenClass";
        RequireModuleRange(name,expected.Length+1);
        var bytes=new byte[expected.Length];
        if(Marshal.ReadByte(name)!=bytes.Length)throw new Exception("Virtual-pult registry class length guard");
        Marshal.Copy(IntPtr.Add(name,1),bytes,0,bytes.Length);
        if(Encoding.ASCII.GetString(bytes)!=expected)throw new Exception("Virtual-pult registry class guard");
        RequireRange(configuration,ConfigurationSize,true,IntPtr.Zero);
        if(Marshal.ReadByte(input)>1)throw new Exception("VirtPult Boolean encoding guard");
        // Native InitHDW still creates real IOKS/DA10 controls independently
        // of VirtPult. An exclusively virtual host must not claim those sources.
        byte ioks=Marshal.ReadByte(configuration,0x2e0),da10=Marshal.ReadByte(configuration,0x2e1);
        if(ioks>1||da10>1)throw new Exception("Virtual-pult external source Boolean encoding guard");
        if(ioks!=0||da10!=0)throw new Exception("Virtual-pult configuration selects external IOKS/DA10 controls");
    }
    void GuardCode() {
        CheckBytes(GetRitRva,GetRitBytes);
        CheckBytes(0x296cf4,HardwareSelectorBytes);
        CheckBytes(0x2b00c8,CabSelectorBytes);
        if(GetProcAddress(module,"InitCommunication")!=IntPtr.Add(module,0x2b1f64) ||
            GetProcAddress(module,"CloseCommunicationDll")!=IntPtr.Add(module,0x2b2078))
            throw new Exception("Virtual-pult native Communication export guard");
    }
    void CheckBytes(int rva,byte[] expected) {
        var address=IntPtr.Add(module,rva); RequireModuleRange(address,expected.Length);
        for(int i=0;i<expected.Length;i++)if(Marshal.ReadByte(address,i)!=expected[i])
            throw new Exception("Virtual-pult native code guard at "+rva.ToString("X"));
    }
    void GuardModule() {
        if(GetModuleHandle("Communication1.dll")!=module)throw new Exception("Virtual-pult Communication module lifetime guard");
        var buffer=new StringBuilder(32768); uint n=GetModuleFileName(module,buffer,buffer.Capacity);
        if(n==0||n>=buffer.Capacity||!String.Equals(Path.GetFullPath(buffer.ToString()),path,StringComparison.OrdinalIgnoreCase))
            throw new Exception("Virtual-pult Communication loaded path guard");
    }
    void ThreadGuard() {
        if(disposed)throw new ObjectDisposedException("TrainerNativeVirtualPult");
        if(IntPtr.Size!=4||GetCurrentThreadId()!=thread)throw new Exception("Virtual-pult owner-thread guard");
    }
    void RequireModuleRange(IntPtr address,int size){RequireRange(address,size,false,module);}
    static void RequireRange(IntPtr address,int size,bool writable,IntPtr allocation) {
        MemoryInfo info;
        if(address==IntPtr.Zero||size<1||VirtualQuery(address,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)
            throw new Exception("Virtual-pult memory range absent");
        ulong start=unchecked((uint)address.ToInt32()),baseAddress=unchecked((uint)info.Base.ToInt32());
        uint protection=info.Protect&0xff;
        bool accessible=writable ? protection==4||protection==8||protection==0x40||protection==0x80
            : protection==2||protection==4||protection==8||protection==0x20||protection==0x40||protection==0x80;
        if(info.State!=0x1000||(info.Protect&0x101)!=0||!accessible||start<baseAddress||
            start+(uint)size>baseAddress+info.RegionSize.ToUInt32()||
            (allocation!=IntPtr.Zero&&(info.AllocationBase!=allocation||info.Type!=0x1000000)))
            throw new Exception("Virtual-pult memory/protection/module range guard");
    }
    static byte[] Hex(string text){var bytes=new byte[text.Length/2];for(int i=0;i<bytes.Length;i++)bytes[i]=Convert.ToByte(text.Substring(i*2,2),16);return bytes;}
    void SafeLog(string message){if(log!=null)try{log(message);}catch{}}
    void RestoreInput() {
        GuardSource(); RequireInactive();
        if(!owned)return;
        // Keep a newer native/operator configuration value. This scope only
        // restores its own assignment and never saves a registry value.
        if(Marshal.ReadByte(input)==1){Marshal.WriteByte(input,original);if(Marshal.ReadByte(input)!=original)throw new Exception("Native VirtPult restoration verification failed");}
        owned=false;
    }
    public void Dispose() {
        if(disposed)return;
        RestoreInput(); disposed=true;
        SafeLog("Native virtual-pult configuration ownership released after Communication close");
    }
}
