using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// Exact guarded native EPK/KON adapter from TGeneralPB, not an EXE loader.
// The 44-byte allocation is an input envelope; it has no fabricated VMT.
// Managed code writes only this envelope and its input snapshot. The copied
// factory method alone writes protection fields using real chamber pressure.
public sealed class TrainerNativeSafetyAdapter : IDisposable {
    public const string MainSha256 = "E217148C24427CAE5DAB46309676854C0E73274F79381AE62613D9D63B3C1612";
    public const string BodySha256 = "EB8119FFE9DB2819DC4FA1F9CE0F31904545F9F775A0F9AC71DB1358218BB79A";
    public const string GetterSha256 = "6156D97C55AF704294C6B12C9DE4BF7FAB7C9C10AAC6D0B8C7532C7B3F7E56FF";
    public const string ConstantsSha256 = "4BD88013F90AD15B46CF7AB37AB31E33DF3BD17BDE5B1FC9B0719C5784CC858F";
    const string KlubSha = "671D3BDB1AD57BD8EA7AFC714B11BC14141A5B2750D72E8B5598C7BB0CFAF20F";
    const string CommunicationSha = "27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F";
    const string SautSha = "977B4B8BD3D3EB98BF9E2F08A8C413525D85F42085D34ABBE842F2BB79384A92";
    const int BodyRva=0x330edc, BodyLength=687, GetterRva=0x330c00, GetterLength=25;
    const int ConstantsRva=0x33118c, ConstantsLength=18;
    const int BodyOffset=0, GetterOffset=0x300, ConstantsOffset=0x320, ThunkOffset=0x340, CodeSize=0x1000;
    static readonly int[] CallOffsets={0xd,0x25,0x37,0x49,0x5c,0x6d,0x81,0x92,0xa3,0xb4,0xcb,0xdc,
        0xf0,0x107,0x119,0x13a,0x151,0x176,0x194,0x1ae,0x1ca,0x1db,0x1ef,0x201,0x218,0x22b,
        0x23d,0x24f,0x263,0x277,0x289,0x29b};
    static readonly int[] ConstantDisplacements={0x129,0x1a1,0x1bb};
    static readonly int[] ConstantSourceAddresses={0x73118c,0x731190,0x731194};
    static readonly int[] ConstantOffsets={0,4,8};
    static readonly int[] PointerOffsets={0,4,8,0x10,0x14};
    static readonly int[] CommonFlags={0x16,0x23,0x29,0x30};

    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void InvokeFn(IntPtr envelope);
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {
        public IntPtr Base, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll")] static extern IntPtr GetCurrentProcess();
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode,SetLastError=true)]
    static extern uint GetModuleFileName(IntPtr module,StringBuilder path,int capacity);
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr address,out MemoryInfo info,UIntPtr size);
    [DllImport("kernel32.dll",SetLastError=true)] static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint type,uint protect);
    [DllImport("kernel32.dll",SetLastError=true)] static extern bool VirtualFree(IntPtr address,UIntPtr size,uint type);
    [DllImport("kernel32.dll",SetLastError=true)] static extern bool VirtualProtect(IntPtr address,UIntPtr size,uint protect,out uint previous);
    [DllImport("kernel32.dll",SetLastError=true)] static extern bool FlushInstructionCache(IntPtr process,IntPtr address,UIntPtr size);

    readonly uint thread;
    readonly string mainPath,klubPath,communicationPath,sautPath;
    readonly long mainLength;
    readonly DateTime mainModified;
    readonly IntPtr canonicalPb,privateSautOutput,commOwner,klub,communication,saut;
    readonly IntPtr communicationRecord,commVmt,commonOutput,loc,input,klubButton,avpButton,wagon,commonButtons,pbButtons;
    readonly IntPtr sautOwnerSlot,sautOwner,sautVmt,sautPb;
    Func<bool> lifetimeGuard;
    Action<string> log;
    IntPtr envelope,inputOutput,code;
    InvokeFn invoke;
    byte[] expectedCode;
    readonly byte[] inputSnapshot=new byte[106];
    readonly byte[] observedCode=new byte[CodeSize];
    bool disposed,inInvoke;

    public static TrainerNativeSafetyAdapter Attach(string mainExe,IntPtr canonicalPB,IntPtr privateSautOutput,
        IntPtr commOwner,IntPtr klubModule,Func<bool> lifetimeGuard,Action<string> log) {
        return new TrainerNativeSafetyAdapter(mainExe,canonicalPB,privateSautOutput,commOwner,klubModule,lifetimeGuard,log);
    }
    TrainerNativeSafetyAdapter(string source,IntPtr shared,IntPtr privateOutput,IntPtr communicationOwner,
        IntPtr klubModule,Func<bool> lifetime,Action<string> writer) {
        if(IntPtr.Size!=4)throw new PlatformNotSupportedException("Native safety adapter requires x86");
        if(String.IsNullOrWhiteSpace(source))throw new ArgumentException("Native safety EXE source absent","mainExe");
        if(shared==IntPtr.Zero||privateOutput==IntPtr.Zero||communicationOwner==IntPtr.Zero||klubModule==IntPtr.Zero)
            throw new ArgumentException("Native safety source pointer absent");
        if(lifetime==null)throw new ArgumentNullException("lifetimeGuard");
        thread=GetCurrentThreadId();lifetimeGuard=lifetime;log=writer;
        mainPath=Path.GetFullPath(source);
        string resources=Path.Combine(Path.GetDirectoryName(mainPath),"RESOURCES");
        klubPath=Path.GetFullPath(Path.Combine(resources,"KLUB_U_LCD_XE2.dll"));
        communicationPath=Path.GetFullPath(Path.Combine(resources,@"LOCOMOTIVES\1\Communication1.dll"));
        sautPath=Path.GetFullPath(Path.Combine(resources,"Saut_Dll.dll"));
        byte[] sourceBytes=File.ReadAllBytes(mainPath);
        RequireHash(sourceBytes,MainSha256,"main EXE");
        var info=new FileInfo(mainPath);mainLength=info.Length;mainModified=info.LastWriteTimeUtc;
        byte[] body=SourceSlice(sourceBytes,BodyRva,0x3302dc,BodyLength,BodySha256);
        byte[] getter=SourceSlice(sourceBytes,GetterRva,0x330000,GetterLength,GetterSha256);
        byte[] constants=SourceSlice(sourceBytes,ConstantsRva,0x33058c,ConstantsLength,ConstantsSha256);
        canonicalPb=shared;privateSautOutput=privateOutput;commOwner=communicationOwner;klub=klubModule;
        communication=GetModuleHandle("Communication1.dll");saut=GetModuleHandle("Saut_Dll.dll");
        CheckModule(klub,"KLUB_U_LCD_XE2.dll",klubPath,KlubSha);
        CheckModule(communication,"Communication1.dll",communicationPath,CommunicationSha);
        CheckModule(saut,"Saut_Dll.dll",sautPath,SautSha);
        commVmt=RequireObjectVmt(commOwner,communication,"TComm2ES6");
        communicationRecord=ReadPointer(commOwner,4);
        klubButton=ReadPointer(canonicalPb,0);avpButton=ReadPointer(canonicalPb,4);
        input=ReadPointer(canonicalPb,8);commonOutput=ReadPointer(canonicalPb,0xc);
        loc=ReadPointer(canonicalPb,0x10);wagon=ReadPointer(canonicalPb,0x14);
        commonButtons=ReadPointer(communicationRecord,0);pbButtons=ReadPointer(klubButton,0x58);
        sautOwnerSlot=ReadPointer(saut,0x1c70c8);sautOwner=ReadPointer(sautOwnerSlot,0);
        sautVmt=RequireObjectVmt(sautOwner,saut,"TUpdateSAUTClass");sautPb=ReadPointer(sautOwner,0x18);
        GuardSources();
        try {
            envelope=AllocateZero(44);inputOutput=AllocateZero(106);
            // Presence is the actual loaded software KLUB handle. The native body
            // only compares +C with zero and never dereferences it as an object.
            Marshal.WriteIntPtr(envelope,0xc,klub);
            foreach(int offset in PointerOffsets)
                Marshal.WriteIntPtr(envelope,0x14+offset,ReadPointer(canonicalPb,offset));
            Marshal.WriteIntPtr(envelope,0x20,inputOutput);
            code=VirtualAlloc(IntPtr.Zero,(UIntPtr)CodeSize,0x3000,0x04);
            if(code==IntPtr.Zero)throw NativeFailure("VirtualAlloc");
            expectedCode=new byte[CodeSize];
            Array.Copy(body,0,expectedCode,BodyOffset,body.Length);
            Array.Copy(getter,0,expectedCode,GetterOffset,getter.Length);
            Array.Copy(constants,0,expectedCode,ConstantsOffset,constants.Length);
            foreach(int offset in CallOffsets) {
                if(body[offset]!=0xe8||BodyRva+offset+5+BitConverter.ToInt32(body,offset+1)!=GetterRva)
                    throw new Exception("Native safety original CALL guard");
                PutInt32(expectedCode,offset+1,GetterOffset-(offset+5));
            }
            for(int i=0;i<ConstantDisplacements.Length;i++) {
                int offset=ConstantDisplacements[i];
                if(BitConverter.ToInt32(body,offset)!=ConstantSourceAddresses[i])
                    throw new Exception("Native safety original constant address guard");
                PutInt32(expectedCode,offset,IntPtr.Add(code,ConstantsOffset+ConstantOffsets[i]).ToInt32());
            }
            // stdcall(IntPtr): transfer Self to Delphi EAX, call copied body, RET 4.
            byte[] thunk={0x8b,0x44,0x24,0x04,0xe8,0,0,0,0,0xc2,0x04,0x00};
            PutInt32(thunk,5,BodyOffset-(ThunkOffset+9));
            Array.Copy(thunk,0,expectedCode,ThunkOffset,thunk.Length);
            VerifyRelocation(body,getter,constants,expectedCode);
            Marshal.Copy(expectedCode,0,code,expectedCode.Length);
            uint previous;
            if(!VirtualProtect(code,(UIntPtr)CodeSize,0x20,out previous))throw NativeFailure("VirtualProtect RX");
            if(!FlushInstructionCache(GetCurrentProcess(),code,(UIntPtr)CodeSize))throw NativeFailure("FlushInstructionCache");
            GuardCode();
            invoke=(InvokeFn)Marshal.GetDelegateForFunctionPointer(IntPtr.Add(code,ThunkOffset),typeof(InvokeFn));
            GuardEnvelope();
            SafeLog("Native safety adapter connected: exact TGeneralPB 330EDC/687 SHA "+BodySha256+"; 32 getter CALLs and 3 constants relocated; immutable RX; actual firmware input envelope");
        } catch { ReleaseOwnAllocations();throw; }
    }

    public bool Ready { get { if(disposed)return false;GuardSources();GuardEnvelope();GuardCode();return invoke!=null; } }
    public IntPtr Envelope { get { ThreadGuard();return envelope; } }
    public IntPtr InputOutput { get { ThreadGuard();return inputOutput; } }
    public IntPtr CodePointer { get { ThreadGuard();return code; } }
    public IntPtr BodyPointer { get { ThreadGuard();return code; } }
    public IntPtr GetterPointer { get { ThreadGuard();return IntPtr.Add(code,GetterOffset); } }
    public IntPtr ConstantsPointer { get { ThreadGuard();return IntPtr.Add(code,ConstantsOffset); } }

    // Caller: after real firmware steps, within the actual operator input scope,
    // before the 10 Hz pneumatic step. No native DLL initialization or close here.
    public void InterfaceInvoke() {
        ThreadGuard();
        if(inInvoke)throw new Exception("Native safety reentrant invocation refused");
        inInvoke=true;
        try {
            GuardSources();GuardEnvelope();GuardCode();
            Array.Clear(inputSnapshot,0,inputSnapshot.Length);
            Marshal.Copy(commonOutput,inputSnapshot,0,50);
            Marshal.Copy(privateSautOutput,inputSnapshot,0,20);
            foreach(int offset in CommonFlags)if(inputSnapshot[offset]>1)
                throw new Exception("Native safety firmware Boolean snapshot guard");
            if(inputSnapshot[0xe]>1)throw new Exception("Native safety SAUT Boolean snapshot guard");
            Marshal.Copy(inputSnapshot,0,inputOutput,inputSnapshot.Length);
            invoke(envelope);
            // Reads validate results; managed code never assigns or clears them.
            foreach(int offset in new int[]{0x9a,0x9b,0x9c,0x9d})ReadFlag(loc,offset);
        } finally { inInvoke=false; }
    }
    void ThreadGuard() {
        if(disposed)throw new ObjectDisposedException("TrainerNativeSafetyAdapter");
        if(GetCurrentThreadId()!=thread)throw new Exception("Native safety adapter owner thread guard");
    }
    void GuardSources() {
        ThreadGuard();
        if(!lifetimeGuard())throw new Exception("Native safety source lifetime refused");
        CheckModuleLifetime(klub,"KLUB_U_LCD_XE2.dll",klubPath);
        CheckModuleLifetime(communication,"Communication1.dll",communicationPath);
        CheckModuleLifetime(saut,"Saut_Dll.dll",sautPath);
        RequireRange(canonicalPb,24,false);RequireRange(commonOutput,106,false);
        RequireRange(privateSautOutput,106,false);RequireRange(loc,264,true);RequireRange(input,52,false);
        RequireRange(commOwner,0x278,false);RequireRange(communicationRecord,0x25,false);
        RequireRange(klubButton,92,false);RequireRange(avpButton,19,false);
        RequireRange(commonButtons,19,false);RequireRange(pbButtons,26,false);
        if(RequireObjectVmt(commOwner,communication,"TComm2ES6")!=commVmt||ReadPointer(commOwner,4)!=communicationRecord||
            ReadPointer(canonicalPb,0)!=klubButton||ReadPointer(canonicalPb,4)!=avpButton||
            ReadPointer(canonicalPb,8)!=input||ReadPointer(canonicalPb,0xc)!=commonOutput||
            ReadPointer(canonicalPb,0x10)!=loc||ReadPointer(canonicalPb,0x14)!=wagon||
            ReadPointer(communicationRecord,0)!=commonButtons||ReadPointer(communicationRecord,8)!=loc||
            ReadPointer(communicationRecord,0x10)!=input||ReadPointer(communicationRecord,0x14)!=commonOutput||
            ReadPointer(communicationRecord,0xc)!=wagon||ReadPointer(klubButton,0x58)!=pbButtons||
            ReadPointer(commonButtons,0xf)!=pbButtons)
            throw new Exception("Native safety canonical communication/PB input graph guard");
        if(ReadPointer(commOwner,0x274)!=IntPtr.Zero)throw new Exception("Native safety duplicate hardware SAUT channel refused");
        if(ReadPointer(saut,0x1c70c8)!=sautOwnerSlot||ReadPointer(sautOwnerSlot,0)!=sautOwner||
            RequireObjectVmt(sautOwner,saut,"TUpdateSAUTClass")!=sautVmt||ReadPointer(sautOwner,0x18)!=sautPb||
            ReadPointer(sautPb,0xc)!=privateSautOutput||ReadPointer(sautPb,0x10)!=loc)
            throw new Exception("Native safety actual private SAUT producer guard");
        RequireDisjoint(commonOutput,106,privateSautOutput,106);
        foreach(int offset in CommonFlags)ReadFlag(commonOutput,offset);
        ReadFlag(privateSautOutput,0xe);ReadFlag(pbButtons,4);
        IntPtr positions=ReadPointer(pbButtons,0);RequireRange(positions,6,false);
        for(int i=0;i<3;i++)ReadFlag(positions,i);
        foreach(int offset in new int[]{0x99,0x9a,0x9b,0x9c,0x9d})ReadFlag(loc,offset);
        RequireFiniteSingle(input,1);RequireFiniteSingle(loc,0xd);RequireFiniteSingle(loc,0x9f);
        if(Marshal.ReadInt32(loc,0xa3)<0||Marshal.ReadInt32(loc,0xa3)==Int32.MaxValue)
            throw new Exception("Native safety KON counter encoding guard");
        var sourceInfo=new FileInfo(mainPath);
        if(sourceInfo.Length!=mainLength||sourceInfo.LastWriteTimeUtc!=mainModified)
            throw new Exception("Native safety guarded source EXE changed after attachment");
    }
    void GuardEnvelope() {
        RequireRange(envelope,44,true);RequireRange(inputOutput,106,true);
        RequireDisjoint(envelope,44,inputOutput,106);
        foreach(IntPtr source in new IntPtr[]{canonicalPb,commonOutput,privateSautOutput,loc,input,pbButtons}) {
            RequireDisjoint(envelope,44,source,source==loc?264:source==input?52:source==pbButtons?26:source==canonicalPb?24:106);
            RequireDisjoint(inputOutput,106,source,source==loc?264:source==input?52:source==pbButtons?26:source==canonicalPb?24:106);
        }
        if(ReadPointer(envelope,0xc)!=klub||ReadPointer(envelope,0x20)!=inputOutput)
            throw new Exception("Native safety input envelope ownership guard");
        foreach(int offset in PointerOffsets)
            if(ReadPointer(envelope,0x14+offset)!=ReadPointer(canonicalPb,offset))
                throw new Exception("Native safety real input envelope alias guard");
    }
    void GuardCode() {
        RequireRange(code,CodeSize,false);MemoryInfo info;
        if(VirtualQuery(code,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero||
            info.AllocationBase!=code||(info.Protect&0xff)!=0x20||info.Type!=0x20000)
            throw new Exception("Native safety immutable RX island guard");
        Marshal.Copy(code,observedCode,0,observedCode.Length);
        for(int i=0;i<expectedCode.Length;i++)if(observedCode[i]!=expectedCode[i])
            throw new Exception("Native safety relocated code byte guard");
    }
    static void VerifyRelocation(byte[] body,byte[] getter,byte[] constants,byte[] island) {
        bool[] changed=new bool[BodyLength];
        foreach(int offset in CallOffsets) {
            for(int i=1;i<=4;i++)changed[offset+i]=true;
            if(island[offset]!=0xe8||offset+5+BitConverter.ToInt32(island,offset+1)!=GetterOffset)
                throw new Exception("Native safety relocated CALL target guard");
        }
        foreach(int offset in ConstantDisplacements)for(int i=0;i<4;i++)changed[offset+i]=true;
        for(int i=0;i<BodyLength;i++)if(!changed[i]&&island[i]!=body[i])
            throw new Exception("Native safety unchanged instruction byte guard");
        for(int i=0;i<GetterLength;i++)if(island[GetterOffset+i]!=getter[i])throw new Exception("Native safety leaf copy guard");
        for(int i=0;i<ConstantsLength;i++)if(island[ConstantsOffset+i]!=constants[i])throw new Exception("Native safety constants copy guard");
    }
    static byte[] SourceSlice(byte[] bytes,int rva,int expectedOffset,int count,string sha) {
        int offset=RvaToFile(bytes,rva,count);
        if(offset!=expectedOffset)throw new Exception("Native safety PE source slice offset guard");
        byte[] part=new byte[count];Array.Copy(bytes,offset,part,0,count);RequireHash(part,sha,"native slice "+rva.ToString("X"));return part;
    }
    static int RvaToFile(byte[] bytes,int rva,int count) {
        if(bytes.Length<0x100||bytes[0]!=0x4d||bytes[1]!=0x5a)throw new Exception("Native safety DOS header guard");
        int pe=BitConverter.ToInt32(bytes,0x3c);
        if(pe<0x40||pe>bytes.Length-0x100||BitConverter.ToUInt32(bytes,pe)!=0x00004550||
            BitConverter.ToUInt16(bytes,pe+4)!=0x14c||BitConverter.ToUInt16(bytes,pe+24)!=0x10b||
            BitConverter.ToUInt32(bytes,pe+52)!=0x400000)throw new Exception("Native safety x86 PE header guard");
        int sections=BitConverter.ToUInt16(bytes,pe+6),table=pe+24+BitConverter.ToUInt16(bytes,pe+20);
        if(sections<1||sections>96||table<pe||table+(long)sections*40>bytes.Length)throw new Exception("Native safety PE section table guard");
        for(int i=0;i<sections;i++) {
            int row=table+i*40;uint va=BitConverter.ToUInt32(bytes,row+12),size=BitConverter.ToUInt32(bytes,row+16),raw=BitConverter.ToUInt32(bytes,row+20);
            if((long)rva>=va&&(long)rva+count<=(long)va+size) {
                long offset=(long)raw+rva-va;
                if(offset<0||offset+count>bytes.Length)throw new Exception("Native safety PE raw slice guard");
                return (int)offset;
            }
        }
        throw new Exception("Native safety PE RVA absent");
    }
    static void RequireHash(byte[] data,string expected,string label) {
        using(SHA256 hash=SHA256.Create())if(BitConverter.ToString(hash.ComputeHash(data)).Replace("-","")!=expected)
            throw new Exception("Native safety SHA guard: "+label);
    }
    static void PutInt32(byte[] bytes,int offset,int value) { Array.Copy(BitConverter.GetBytes(value),0,bytes,offset,4); }
    static IntPtr AllocateZero(int size) {
        IntPtr memory=Marshal.AllocHGlobal(size);
        try { Marshal.Copy(new byte[size],0,memory,size);return memory; }
        catch { Marshal.FreeHGlobal(memory);throw; }
    }
    static byte ReadFlag(IntPtr record,int offset) {
        RequireRange(IntPtr.Add(record,offset),1,false);byte value=Marshal.ReadByte(record,offset);
        if(value>1)throw new Exception("Native safety Boolean encoding guard");return value;
    }
    static void RequireFiniteSingle(IntPtr record,int offset) {
        RequireRange(IntPtr.Add(record,offset),4,false);
        if((unchecked((uint)Marshal.ReadInt32(record,offset))&0x7f800000U)==0x7f800000U)
            throw new Exception("Native safety non-finite physical input guard");
    }
    static IntPtr ReadPointer(IntPtr record,int offset) { RequireRange(IntPtr.Add(record,offset),4,false);return Marshal.ReadIntPtr(record,offset); }
    static void RequireDisjoint(IntPtr first,int aCount,IntPtr second,int bCount) {
        ulong a=unchecked((uint)first.ToInt32()),b=unchecked((uint)second.ToInt32());
        if(a<b+(uint)bCount&&b<a+(uint)aCount)throw new Exception("Native safety input/native record overlap guard");
    }
    static void RequireRange(IntPtr address,int count,bool writable) {
        MemoryInfo info;
        if(address==IntPtr.Zero||count<1||VirtualQuery(address,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)
            throw new Exception("Native safety memory range guard");
        ulong start=unchecked((uint)address.ToInt32()),region=unchecked((uint)info.Base.ToInt32());uint protect=info.Protect&0xff;
        bool readable=protect==2||protect==4||protect==8||protect==0x20||protect==0x40||protect==0x80;
        bool write=protect==4||protect==8||protect==0x40||protect==0x80;
        if(info.State!=0x1000||(info.Protect&0x101)!=0||start<region||start+(uint)count>region+info.RegionSize.ToUInt32()||
            !readable||(writable&&!write))throw new Exception("Native safety readable/writable range guard");
    }
    static void RequireModuleRange(IntPtr address,int count,IntPtr module) {
        RequireRange(address,count,false);MemoryInfo info;
        if(VirtualQuery(address,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero||info.AllocationBase!=module||info.Type!=0x1000000)
            throw new Exception("Native safety module range guard");
    }
    static IntPtr RequireObjectVmt(IntPtr obj,IntPtr module,string expectedClass) {
        IntPtr vmt=ReadPointer(obj,0);RequireModuleRange(IntPtr.Add(vmt,-56),60,module);
        IntPtr name=ReadPointer(vmt,-56);RequireModuleRange(name,1,module);int size=Marshal.ReadByte(name);
        if(size<1||size>150)throw new Exception("Native safety VMT class length guard");
        RequireModuleRange(name,size+1,module);byte[] data=new byte[size];Marshal.Copy(IntPtr.Add(name,1),data,0,size);
        if(Encoding.ASCII.GetString(data)!=expectedClass)throw new Exception("Native safety native class guard: "+expectedClass);
        return vmt;
    }
    static void CheckModuleLifetime(IntPtr module,string name,string expectedPath) {
        if(module==IntPtr.Zero||GetModuleHandle(name)!=module)throw new Exception("Native safety module lifetime guard: "+name);
        var path=new StringBuilder(32768);uint size=GetModuleFileName(module,path,path.Capacity);
        if(size==0||size>=path.Capacity||!String.Equals(Path.GetFullPath(path.ToString()),expectedPath,StringComparison.OrdinalIgnoreCase))
            throw new Exception("Native safety loaded path guard: "+name);
    }
    static void CheckModule(IntPtr module,string name,string expectedPath,string sha) {
        CheckModuleLifetime(module,name,expectedPath);RequireHash(File.ReadAllBytes(expectedPath),sha,name);
    }
    static Exception NativeFailure(string operation) { return new Exception("Native safety "+operation+" failed="+Marshal.GetLastWin32Error()); }
    void SafeLog(string text) { try { if(log!=null)log(text); } catch { } }
    void ReleaseOwnAllocations() {
        if(code!=IntPtr.Zero) {
            if(!VirtualFree(code,UIntPtr.Zero,0x8000))throw NativeFailure("VirtualFree");
            code=IntPtr.Zero;
        }
        if(inputOutput!=IntPtr.Zero){Marshal.FreeHGlobal(inputOutput);inputOutput=IntPtr.Zero;}
        if(envelope!=IntPtr.Zero){Marshal.FreeHGlobal(envelope);envelope=IntPtr.Zero;}
        invoke=null;expectedCode=null;
    }
    public void Dispose() {
        if(disposed)return;ThreadGuard();
        if(inInvoke)throw new Exception("Native safety detach during invocation refused");
        // Host stops invoking before native source owners close. No native close,
        // original EXE entry point, record restoration or output reset is performed.
        ReleaseOwnAllocations();disposed=true;lifetimeGuard=null;
        SafeLog("Native safety adapter detached; owned RX and input envelope released");log=null;
    }
}
