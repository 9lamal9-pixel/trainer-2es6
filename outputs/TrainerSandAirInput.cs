using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// Reconstructed virtual-host input publication, not a discovered original-main
// AF publisher. The original Pult produces AF.Pesok; Torm consumes Pos.Pesok.
// Only the latter input is borrowed. Native pressure/force outputs are untouched.
partial class TrainerIntegrationHost {
    const string SandAirCommSha="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F";
    const string SandAirPultSha="7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5";
    const string SandAirTormSha="F871640C5B7699901E3AFBF755BB2B797B659524C29BAA046B311CA6ECC875B9";
    IntPtr nativeSandAirCommModule,nativeSandAirPultModule,nativeSandAirTormModule;
    IntPtr nativeSandAirOwner,nativeSandAirPult,nativeSandAirLoc,nativeSandAirPultOwner;
    uint nativeSandAirThread;
    bool nativeSandAirVerified;
    NativeSandAirLease nativeSandAirActive;
    // Diagnostic metadata describes the last fresh source, never a model output.
    int nativeSandAirLastTick=-1;
    byte nativeSandAirLastAcceptedAf;
    bool nativeSandAirLastAssigned;

    [StructLayout(LayoutKind.Sequential)] struct SandAirMemoryInfo {
        public IntPtr Base,AllocationBase;public uint AllocationProtect;
        public UIntPtr RegionSize;public uint State,Protect,Type;
    }
    [DllImport("kernel32.dll",EntryPoint="GetCurrentThreadId")] static extern uint SandAirGetCurrentThreadId();
    [DllImport("kernel32.dll",EntryPoint="GetModuleHandleW",CharSet=CharSet.Unicode)] static extern IntPtr SandAirGetModuleHandle(string name);
    [DllImport("kernel32.dll",EntryPoint="GetModuleFileNameW",CharSet=CharSet.Unicode)] static extern uint SandAirGetModuleFileName(IntPtr module,StringBuilder path,int size);
    [DllImport("kernel32.dll",EntryPoint="VirtualQuery")] static extern UIntPtr SandAirVirtualQuery(IntPtr p,out SandAirMemoryInfo info,UIntPtr size);

    sealed class NativeSandAirNoInput : IDisposable {public void Dispose(){}}
    static readonly IDisposable NativeSandAirNoInputLease=new NativeSandAirNoInput();
    sealed class NativeSandAirLease : IDisposable {
        readonly TrainerIntegrationHost host;readonly byte saved;
        bool owns,disposed;
        internal NativeSandAirLease(TrainerIntegrationHost owner) {
            host=owner;saved=Marshal.ReadByte(host.nativeSandAirLoc,0x7f);
            if(saved>1)throw new Exception("Sand air input encoding guard");
            host.nativeSandAirActive=this;
            try {
                // A pre-existing valve input belongs to its other producer.
                // It is never cleared, even when this source later turns off.
                if(saved==0){owns=true;Marshal.WriteByte(host.nativeSandAirLoc,0x7f,1);}
                if(Marshal.ReadByte(host.nativeSandAirLoc,0x7f)!=1)throw new Exception("Sand air input assignment guard");
                host.nativeSandAirLastAssigned=owns;
            } catch {Dispose();throw;}
        }
        public void Dispose() {
            if(disposed)return;
            host.GuardNativeSandAirGraph();
            if(owns) {
                SandAirRequire(IntPtr.Add(host.nativeSandAirLoc,0x7f),1,true,IntPtr.Zero);
                // Retain a newer foreign/native input value. This scope restores
                // only its own assignment, and has no valve/protection output.
                if(Marshal.ReadByte(host.nativeSandAirLoc,0x7f)==1) {
                    Marshal.WriteByte(host.nativeSandAirLoc,0x7f,saved);
                    if(Marshal.ReadByte(host.nativeSandAirLoc,0x7f)!=saved)throw new Exception("Sand air restoration guard");
                }
                owns=false;
            }
            disposed=true;
            if(Object.ReferenceEquals(host.nativeSandAirActive,this))host.nativeSandAirActive=null;
        }
    }

    // Caller invalidates nativeSandElectricalReady before each ElectricalCycle.
    // Only after commStep+UpdateVcuInterlock succeed and electricalFault="":
    // nativeSandElectricalTick=ticks; nativeSandElectricalReady=true.
    // Borrow around original pneumoStep AND its normal AfterNativeAirStep.
    IDisposable BorrowNativeSandAirInput() {
        if(!nativeSandElectricalReady || nativeSandElectricalTick!=ticks || !String.IsNullOrEmpty(electricalFault) ||
            avpNative!=null && !String.IsNullOrEmpty(avpNative.Fault))
            return NativeSandAirNoInputLease;
        if(nativeSandAirActive!=null)throw new Exception("Nested sand air input ownership");
        if(IntPtr.Size!=4 || !commReady || !physicsReady || nativeCommunicationInactive || IsDisposed || Disposing || !IsHandleCreated || InvokeRequired)
            throw new Exception("Sand air input host lifetime/UI thread guard");
        IntPtr pult;
        if(!mem.TryGetValue("TPult",out pult))throw new Exception("Sand electrical source absent");
        SandAirRequire(IntPtr.Add(pult,0xa5),1,false,IntPtr.Zero);
        byte accepted=Marshal.ReadByte(pult,0xa5);
        if(accepted>1)throw new Exception("Native sand AF Boolean encoding guard");
        nativeSandAirLastTick=ticks;nativeSandAirLastAcceptedAf=accepted;nativeSandAirLastAssigned=false;
        // Fresh OFF never clears a valve input supplied by another producer.
        // Stale/faulted electrical frames never replay the previous AF either.
        if(accepted==0)return NativeSandAirNoInputLease;
        EnsureNativeSandAirBinding();GuardNativeSandAirGraph();
        SandAirRequire(IntPtr.Add(nativeSandAirLoc,0x7f),1,true,IntPtr.Zero);
        return new NativeSandAirLease(this);
    }
    void RestoreNativeSandAirInputs() {
        if(nativeSandAirActive!=null)nativeSandAirActive.Dispose();
        nativeSandElectricalReady=false;
    }
    void EnsureNativeSandAirBinding() {
        if(nativeSandAirVerified)return;
        if(IntPtr.Size!=4)throw new PlatformNotSupportedException("Native sand air input requires x86");
        nativeSandAirThread=SandAirGetCurrentThreadId();
        nativeSandAirCommModule=SandAirGetModuleHandle("Communication1.dll");
        nativeSandAirPultModule=SandAirGetModuleHandle("Pult1.dll");
        nativeSandAirTormModule=SandAirGetModuleHandle("TormModel.dll");
        SandAirGuardModule(nativeSandAirCommModule,"Communication1.dll",SandAirCommSha,true);
        SandAirGuardModule(nativeSandAirPultModule,"Pult1.dll",SandAirPultSha,true);
        SandAirGuardModule(nativeSandAirTormModule,"TormModel.dll",SandAirTormSha,true);
        nativeSandAirOwner=mem["CommOwner"];nativeSandAirPult=mem["TPult"];nativeSandAirLoc=mem["TLocParam"];
        var slot=SandAirReadPointer(nativeSandAirPultModule,0x1a2e60);
        SandAirRequire(slot,4,false,nativeSandAirPultModule);
        nativeSandAirPultOwner=SandAirReadPointer(slot,0);
        GuardNativeSandAirGraph();nativeSandAirVerified=true;
        Log("Native sand air input connected: actual fresh Pult AF+A5 -> borrowed Loc.Pos.Pesok+7F; native Torm computes air loss. No sand adhesion/force credit.");
    }
    void GuardNativeSandAirGraph() {
        if(IntPtr.Size!=4 || SandAirGetCurrentThreadId()!=nativeSandAirThread || IsDisposed || !IsHandleCreated || InvokeRequired ||
            !commReady || !physicsReady || nativeCommunicationInactive)
            throw new Exception("Sand air input lifetime/thread guard");
        SandAirGuardModule(nativeSandAirCommModule,"Communication1.dll",null,false);
        SandAirGuardModule(nativeSandAirPultModule,"Pult1.dll",null,false);
        SandAirGuardModule(nativeSandAirTormModule,"TormModel.dll",null,false);
        GuardNativeSandAirCode();
        if(nativeVirtualPult==null || !nativeVirtualPult.Enabled)throw new Exception("Sand air source requires verified native VirtPult authority");
        var rit=nativeVirtualPult.Configuration;SandAirRequire(rit,773,false,IntPtr.Zero);
        if(Marshal.ReadByte(rit,0x83)!=1)throw new Exception("Sand air native VirtPult source mode guard");
        if(mem["CommOwner"]!=nativeSandAirOwner || mem["TPult"]!=nativeSandAirPult || mem["TLocParam"]!=nativeSandAirLoc)
            throw new Exception("Sand air host record identity changed");
        SandAirRequire(nativeSandAirOwner,0x268,false,IntPtr.Zero);
        SandAirRequire(nativeSandAirPult,269,false,IntPtr.Zero);SandAirRequire(nativeSandAirLoc,264,false,IntPtr.Zero);
        SandAirGuardClass(nativeSandAirOwner,nativeSandAirCommModule,0x2afd84,"TComm2ES6");
        if(nativeSandAirPult!=IntPtr.Add(nativeSandAirOwner,0x24) ||
            Marshal.ReadIntPtr(nativeSandAirOwner,0x10)!=IntPtr.Zero || Marshal.ReadIntPtr(nativeSandAirOwner,0x14)!=IntPtr.Zero || Marshal.ReadIntPtr(nativeSandAirOwner,0x18)!=IntPtr.Zero)
            throw new Exception("Sand air external hardware input authority guard");
        var cell=SandAirReadPointer(nativeSandAirCommModule,0x2c7068);
        if(cell!=IntPtr.Add(nativeSandAirCommModule,0x2e16b0))throw new Exception("Sand air Communication global cell guard");
        SandAirRequire(cell,4,false,nativeSandAirCommModule);
        if(SandAirReadPointer(cell,0)!=nativeSandAirOwner)throw new Exception("Sand air native Communication owner changed");
        var rec=SandAirReadPointer(nativeSandAirOwner,4);SandAirRequire(rec,0x29,false,IntPtr.Zero);
        if(SandAirReadPointer(rec,8)!=nativeSandAirLoc || SandAirReadPointer(nativeSandAirPult,4)!=nativeSandAirLoc ||
            SandAirReadPointer(nativeSandAirPult,0x10)!=IntPtr.Add(nativeSandAirOwner,0x131))
            throw new Exception("Sand air shared Loc/Pult/Tyga alias guard");
        var slot=SandAirReadPointer(nativeSandAirPultModule,0x1a2e60);SandAirRequire(slot,4,false,nativeSandAirPultModule);
        if(SandAirReadPointer(slot,0)!=nativeSandAirPultOwner)throw new Exception("Sand air Pult owner changed");
        SandAirRequire(nativeSandAirPultOwner,0x18,false,IntPtr.Zero);
        SandAirGuardClass(nativeSandAirPultOwner,nativeSandAirPultModule,0x18b620,"TPult2ES6");
        if(SandAirReadPointer(nativeSandAirPultOwner,4)!=nativeSandAirPult ||
            SandAirReadPointer(nativeSandAirPultOwner,8)!=IntPtr.Add(nativeSandAirOwner,0x131))throw new Exception("Sand air native Pult producer alias guard");
        SandAirRequire(trainData,70,false,IntPtr.Zero);var array=SandAirReadPointer(trainData,0x42);SandAirRequire(array,8,false,IntPtr.Zero);
        if(SandAirReadPointer(array,0)!=nativeSandAirLoc)throw new Exception("Sand air actual train pneumatic Loc alias guard");
    }
    void GuardNativeSandAirCode() {
        SandAirCheckCode(nativeSandAirCommModule,0x2af458,"8B45FC8B80D40300008A80D90100008B55FC8B9230050000884240");
        SandAirCheckCode(nativeSandAirPultModule,0x18e934,"80784001");
        SandAirCheckCode(nativeSandAirPultModule,0x18e964,"C680A500000001");
        SandAirCheckCode(nativeSandAirPultModule,0x18e973,"C680A500000000");
        SandAirCheckCode(nativeSandAirTormModule,0x177db4,"80787F007419");
        SandAirCheckCode(nativeSandAirTormModule,0x177dc3,"DC482F");SandAirCheckCode(nativeSandAirTormModule,0x177dcf,"D95805");
        SandAirCheckCode(nativeSandAirTormModule,0x177ee9,"80787F007419");
        SandAirCheckCode(nativeSandAirTormModule,0x177ef8,"DC481F");SandAirCheckCode(nativeSandAirTormModule,0x177f04,"D95805");
        SandAirCheckCode(nativeSandAirTormModule,0x1780d8,"00008040");
    }
    void SandAirGuardModule(IntPtr module,string name,string sha,bool hash) {
        if(module==IntPtr.Zero || SandAirGetModuleHandle(name)!=module)throw new Exception("Sand air module lifetime guard: "+name);
        var loaded=new StringBuilder(32768);uint count=SandAirGetModuleFileName(module,loaded,loaded.Capacity);
        string expected=Path.GetFullPath(Path.Combine(root,name=="TormModel.dll"?@"RESOURCES\TormModel.dll":@"RESOURCES\LOCOMOTIVES\1\"+name));
        if(count==0 || count>=loaded.Capacity || !String.Equals(Path.GetFullPath(loaded.ToString()),expected,StringComparison.OrdinalIgnoreCase))
            throw new Exception("Sand air loaded native path guard: "+name);
        if(hash)using(var digest=SHA256.Create())using(var file=File.OpenRead(expected))
            if(BitConverter.ToString(digest.ComputeHash(file)).Replace("-","")!=sha)throw new Exception("Sand air native SHA guard: "+name);
    }
    static void SandAirGuardClass(IntPtr owner,IntPtr module,int vmtRva,string expected) {
        var vmt=SandAirReadPointer(owner,0);
        if(vmt!=IntPtr.Add(module,vmtRva))throw new Exception("Sand air native VMT guard: "+expected);
        SandAirRequire(IntPtr.Add(vmt,-88),92,false,module);
        if(SandAirReadPointer(vmt,-88)!=vmt)throw new Exception("Sand air native VMT self guard");
        var name=SandAirReadPointer(vmt,-56);SandAirRequire(name,expected.Length+1,false,module);
        if(Marshal.ReadByte(name)!=expected.Length)throw new Exception("Sand air native class-name length guard");
        var bytes=new byte[expected.Length];Marshal.Copy(IntPtr.Add(name,1),bytes,0,bytes.Length);
        if(Encoding.ASCII.GetString(bytes)!=expected)throw new Exception("Sand air native class guard: "+expected);
    }
    static IntPtr SandAirReadPointer(IntPtr p,int offset) {var field=IntPtr.Add(p,offset);SandAirRequire(field,4,false,IntPtr.Zero);return Marshal.ReadIntPtr(field);}
    static void SandAirCheckCode(IntPtr module,int rva,string hex) {
        var p=IntPtr.Add(module,rva);SandAirRequire(p,hex.Length/2,false,module);
        for(int n=0;n<hex.Length/2;n++)if(Marshal.ReadByte(p,n)!=Convert.ToByte(hex.Substring(n*2,2),16))throw new Exception("Sand air native byte guard: "+rva.ToString("X"));
    }
    static void SandAirRequire(IntPtr p,int size,bool writable,IntPtr module) {
        SandAirMemoryInfo info;
        if(p==IntPtr.Zero || size<1 || SandAirVirtualQuery(p,out info,(UIntPtr)Marshal.SizeOf(typeof(SandAirMemoryInfo)))==UIntPtr.Zero)
            throw new Exception("Sand air memory range absent");
        ulong a=unchecked((uint)p.ToInt32()),b=unchecked((uint)info.Base.ToInt32());uint protect=info.Protect&0xff;
        bool accessible=writable?protect==4||protect==8||protect==0x40||protect==0x80:
            protect==2||protect==4||protect==8||protect==0x20||protect==0x40||protect==0x80;
        if(info.State!=0x1000 || (info.Protect&0x101)!=0 || !accessible || a<b || a+(uint)size>b+info.RegionSize.ToUInt32() ||
            module!=IntPtr.Zero && (info.AllocationBase!=module || info.Type!=0x1000000))throw new Exception("Sand air memory/module/protection guard");
    }
}
