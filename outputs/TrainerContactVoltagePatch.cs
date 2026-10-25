using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// One external whole-volt input. Native gates and the derived regenerative
// GetUSet path remain native. This is not a network-loss/protection model.
public sealed class TrainerContactVoltagePatch : IDisposable {
    public const int NominalVoltage = 3000, MaximumInputVoltage = 5040;
    const string PultSha = "7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5";
    const string TygaSha = "8EC6A82E618A1D75CBB31242784084B530BB87308E601DEFC5263D5279270491";
    const string RetainedFixHex = "837df0007f0633c08be55dc3db45f0d87dece950e891ff837ddc000f8fbb4292ff8b45fce85f6192ffd9ee5b8be55dc204008b45fc8b40048b55f4817cd03300803b447608c744d03300803b44817cd03700803b447608c744d03700803b44e9c23692ff8b45fcba01000000e8932192ff508b45fcba02000000e8852192ff5a03c285c0750c8b45fce8fa6092ff8be55dc38b45fc80780b00e90e0492ff";
    const int Magic = 0x53564b55, ValueOffset = 4, BitsOffset = 8, GenerationOffset = 12, ChecksumOffset = 16, FaultOffset = 20;
    const int CountOffset = 24, DestinationOffset = 44, LastValueOffset = 64, LastGenerationOffset = 84, DataSize = 128;
    static readonly object OwnerLock = new object();
    static readonly Dictionary<IntPtr, TrainerContactVoltagePatch> Owners = new Dictionary<IntPtr, TrainerContactVoltagePatch>();
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo { public IntPtr Base, Allocation; public uint AllocationProtect; public UIntPtr Size; public uint State, Protect, Type; }
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module, StringBuilder path, int capacity);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool GetModuleHandleEx(uint flags, IntPtr address, out IntPtr module);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool FreeLibrary(IntPtr module);
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr address, out MemoryInfo info, UIntPtr size);
    [DllImport("kernel32.dll", SetLastError = true)] static extern IntPtr VirtualAlloc(IntPtr address, UIntPtr size, uint allocation, uint protection);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool VirtualFree(IntPtr address, UIntPtr size, uint kind);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool VirtualProtect(IntPtr address, UIntPtr size, uint protection, out uint previous);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool FlushInstructionCache(IntPtr process, IntPtr address, UIntPtr size);

    readonly uint thread;
    readonly string pultPath, tygaPath;
    readonly IntPtr pultModule, tygaModule, pult, tyga;
    IntPtr pinnedPult, pinnedTyga, pultCell, pultOwner, schema, pultVmt, tygaCell, outer, outerVmt, calc, calcVmt, result, constants, eset, data;
    Site[] sites;
    int value, originalEset, expectedEset, pendingEset;
    uint generation;
    bool graphReady, esetOwned, esetWritePending, initialized, closing, disposed;

    sealed class Site {
        public string Name; public int Index, Rva, WindowRva, MemberOffset; public bool FloatStore, Sequential;
        public IntPtr Module, Owner, Destination, Cave; public byte[] Original, WindowOriginal, WindowExpected, Jump, Code;
        public bool MayExist, ProtectionPending, FlushPending; public uint PreviousProtection;
    }

    public static bool HasRetainedOwners { get { lock (OwnerLock) return Owners.Count != 0; } }
    public static bool HasRetainedOwner(IntPtr module) { lock (OwnerLock) return Owners.ContainsKey(module); }

    public TrainerContactVoltagePatch(string resources, IntPtr realPult, IntPtr realTyga) {
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Contact voltage requires x86");
        if (String.IsNullOrWhiteSpace(resources) || realPult == IntPtr.Zero || realTyga == IntPtr.Zero) throw new ArgumentException("Contact voltage inputs absent");
        thread = GetCurrentThreadId(); pult = realPult; tyga = realTyga;
        pultPath = Path.GetFullPath(Path.Combine(resources, @"LOCOMOTIVES\1\Pult1.dll"));
        tygaPath = Path.GetFullPath(Path.Combine(resources, @"LOCOMOTIVES\1\Tyga1.dll"));
        pultModule = GetModuleHandle("Pult1.dll"); tygaModule = GetModuleHandle("Tyga1.dll");
        if (pultModule == IntPtr.Zero || tygaModule == IntPtr.Zero) throw new InvalidOperationException("Contact voltage native modules absent");
        lock (OwnerLock) {
            if (Owners.ContainsKey(pultModule)) throw new InvalidOperationException("Contact voltage already owned");
            Owners.Add(pultModule, this);
            try { Install(); }
            catch (Exception error) {
                try { Dispose(); }
                catch (Exception cleanup) { throw new InvalidOperationException("Contact voltage attach and rollback failed; retained ownership forbids native close", new AggregateException(error, cleanup)); }
                throw;
            }
        }
    }

    void Install() {
        Pin(pultModule, ref pinnedPult); Pin(tygaModule, ref pinnedTyga);
        GuardModule(pultModule, "Pult1.dll", pultPath); GuardModule(tygaModule, "Tyga1.dll", tygaPath);
        GuardFile(pultPath, PultSha); GuardFile(tygaPath, TygaSha);
        pultCell = Pointer(pultModule, 0x1a2e60);
        if (pultCell != IntPtr.Add(pultModule, 0x1bc028)) throw new InvalidOperationException("Contact voltage Pult cell guard");
        pultOwner = Pointer(pultCell, 0); pultVmt = Pointer(pultOwner, 0); schema = IntPtr.Add(pultOwner, 0x18);
        if (pultVmt != Pointer(pultModule, 0x18b5c8)) throw new InvalidOperationException("Contact voltage Pult class guard");
        tygaCell = Pointer(tygaModule, 0x19058c);
        if (tygaCell != IntPtr.Add(tygaModule, 0x1aa058)) throw new InvalidOperationException("Contact voltage Tyga cell guard");
        outer = Pointer(tygaCell, 0); outerVmt = Pointer(outer, 0); calc = Pointer(outer, 0x420); calcVmt = Pointer(calc, 0);
        if (outerVmt != Pointer(tygaModule,0x17ebfc) || calcVmt != Pointer(tygaModule,0x172ef0)) throw new InvalidOperationException("Contact voltage exact Tyga class guard");
        constants = Pointer(calc, 0x10c); result = Pointer(tyga, 0x27); eset = IntPtr.Add(constants, 0x3fc);
        if (constants != IntPtr.Add(outer, 8)) throw new InvalidOperationException("Contact voltage constants alias guard");
        RequireModuleAllocation(pultVmt, pultModule); RequireModuleAllocation(outerVmt, tygaModule); RequireModuleAllocation(calcVmt, tygaModule);
        graphReady = true; GuardGraph();
        originalEset = expectedEset = Marshal.ReadInt32(eset);
        if (originalEset != 3200) throw new InvalidOperationException("Contact voltage original Eset guard");
        Guard(IntPtr.Add(pultModule, 0x18b92c), Hex("558bec83c4f88945fc8b45fc83c0188945f88b45f859595dc3"));
        Guard(IntPtr.Add(tygaModule, 0x17aeae), Hex("db80fc030000"));
        GuardRetainedFix();
        // Check the retained early-limit entry before any diagnostic observer
        // attaches. Its temporary read-only observer may later own this entry;
        // the immutable retained stub itself remains guarded every cycle.
        Guard(IntPtr.Add(tygaModule,0x17971d),Hex("e910c96d0090909090"));
        sites = new[] {
            Make("Pult",0,pultModule,pultOwner,schema,0x18e9ba,0x18e97a,0x125,false,false,"c780250100001c0c0000","8b45fce8aacfffff80785800752a8b45fce89ccfffff80785900751c8b45fce88ecfffff80785a00750e8b45fce880cfffff80785b0074148b45fce872cfffffc780250100001c0c0000eb108b45fce85ecfffff33d2899025010000"),
            Make("TygaEDT",1,tygaModule,calc,result,0x1748f4,0x1748b0,0x11,true,false,"c7401100004845","558bec83c4bc8945fc8b45fc80b8090100000075178b45fce8e37700008b45fc8b40048b4027d958119beb2f8b45fc8b400480b8300100000074128b45fc8b40048b4027c7401100004845eb0e8b45fc8b40048b402733d2895011"),
            Make("Nezav",2,tygaModule,calc,result,0x17650f,0x1764ed,0x11,true,false,"c7401100004845","8b45fcc68001010000008b45fc8b400480b8300100000074128b45fc8b40048b4027c7401100004845eb0e8b45fc8b40048b402733d2895011"),
            Make("Help",3,tygaModule,calc,result,0x17c1a9,0x17c188,0x11,true,false,"c7401100004845","558bec83c4f48945fc8b45fc8b400480b8300100000074128b45fc8b40048b4027c7401100004845eb0e8b45fc8b40048b402733d2895011"),
            Make("TractionEntry",4,tygaModule,calc,result,0x17c34c,0x17c320,0x11,true,true,"8b45fc8b4004","558bec83c4f88945fc8b45fc8b400483781b0075178b45fce84bfeffffb2058b45fce8c10a0000e9d90000008b45fc8b4004dd0083c4fcd91c249b8b45fce87d220000")
        };
        foreach (Site site in sites) { Guard(IntPtr.Add(site.Module,site.WindowRva),site.WindowOriginal); Guard(IntPtr.Add(site.Module,site.Rva),site.Original); RequireRx(IntPtr.Add(site.Module,site.Rva)); }
        data = Allocate(DataSize);
        value = NominalVoltage; generation = 1; PublishSource(); WriteEset(value);
        foreach (Site site in sites) {
            site.Cave = Allocate(4096); site.Code = BuildCave(site);
            if (site.Code.Length > 4096) throw new InvalidOperationException("Contact voltage cave exceeds page");
            Marshal.Copy(site.Code,0,site.Cave,site.Code.Length); uint ignored;
            if (!VirtualProtect(site.Cave,(UIntPtr)4096,0x20,out ignored)) throw WinError("Protect voltage cave RX");
            RequireRx(site.Cave); Flush(site.Cave,site.Code.Length);
            site.Jump = Jump(IntPtr.Add(site.Module,site.Rva),site.Cave,site.Original.Length);
            site.WindowExpected = (byte[])site.WindowOriginal.Clone();
            Array.Copy(site.Jump,0,site.WindowExpected,site.Rva-site.WindowRva,site.Jump.Length);
            WriteSite(site,site.Original,site.Jump,true);
        }
        initialized = true; Validate();
    }

    static Site Make(string name,int index,IntPtr module,IntPtr owner,IntPtr destination,int rva,int window,int member,bool floating,bool sequential,string original,string windowBytes) {
        return new Site { Name=name,Index=index,Module=module,Owner=owner,Destination=destination,Rva=rva,WindowRva=window,MemberOffset=member,FloatStore=floating,Sequential=sequential,Original=Hex(original),WindowOriginal=Hex(windowBytes) };
    }

    public void SetVoltage(int volts) {
        if (volts < 0 || volts > MaximumInputVoltage) throw new ArgumentOutOfRangeException("volts","Contact voltage input profile is 0..5040 whole volts");
        Validate();
        if (generation == UInt32.MaxValue) throw new InvalidOperationException("Contact voltage source generation exhausted");
        WriteEset(volts); value = volts; generation++; PublishSource(); Validate();
    }

    public string Fault { get { ThreadGuard(); if (data == IntPtr.Zero) return ""; Readable(data,DataSize); int error=Marshal.ReadInt32(data,FaultOffset); return error==0?"":"producer fault flags "+error; } }

    public void Validate() { GuardActive(true); }
    public bool NativeDynamicBrakingActive { get { GuardActive(true); byte flag=Marshal.ReadByte(calc,0x100); if(flag>1)throw new InvalidOperationException("Native brake mode flag guard"); return flag!=0; } }
    void GuardActive(bool operational) {
        ThreadGuard();
        if (disposed || closing || !initialized) throw new ObjectDisposedException("TrainerContactVoltagePatch");
        GuardGraph(); GuardSource(); GuardRetainedFix();
        foreach (Site site in sites) {
            if (!site.MayExist || site.Cave==IntPtr.Zero) throw new InvalidOperationException("Contact voltage site absent");
            Guard(IntPtr.Add(site.Module,site.WindowRva),site.WindowExpected); Guard(site.Cave,site.Code);
            RequireRx(IntPtr.Add(site.Module,site.Rva)); RequireRx(site.Cave);
        }
        if (operational && Fault.Length!=0) throw new InvalidOperationException("Contact voltage "+Fault);
    }

    public Dictionary<string,object> Snapshot() {
        GuardActive(false);
        var snapshot=new Dictionary<string,object> { {"Value",value},{"SingleBits",SingleBits(value)},{"Generation",generation},{"Eset",Marshal.ReadInt32(eset)},{"Fault",Fault},{"SourcePointer",data.ToString("X")},{"Sections",Marshal.ReadInt32(tyga,8)} };
        foreach(Site site in sites) {
            uint bits=unchecked((uint)Marshal.ReadInt32(data,LastValueOffset+site.Index*4));
            snapshot.Add(site.Name,new Dictionary<string,object> { {"Count",unchecked((uint)Marshal.ReadInt32(data,CountOffset+site.Index*4))},{"LastDestination",unchecked((uint)Marshal.ReadInt32(data,DestinationOffset+site.Index*4))},{"LastValue",site.FloatStore?(object)BitConverter.ToSingle(BitConverter.GetBytes(bits),0):(object)unchecked((int)bits)},{"LastBits",bits},{"LastGeneration",unchecked((uint)Marshal.ReadInt32(data,LastGenerationOffset+site.Index*4))},{"EntryRva",site.Rva},{"Jump",BitConverter.ToString(site.Jump).Replace("-","")} });
        }
        return snapshot;
    }

    public void VerifyOriginalWindowWithOwnedHooks(IntPtr module,int rva,byte[] original) {
        Validate();
        if (module!=pultModule && module!=tygaModule || rva<0 || original==null || original.Length==0) throw new ArgumentException("Contact voltage original window absent");
        byte[] expected=(byte[])original.Clone(); long end=(long)rva+original.Length;
        foreach(Site site in sites) {
            if(site.Module!=module) continue;
            long first=Math.Max((long)rva,site.Rva), last=Math.Min(end,(long)site.Rva+site.Original.Length);
            for(long at=first;at<last;at++) {
                int input=checked((int)(at-rva)), owned=checked((int)(at-site.Rva));
                if(original[input]!=site.Original[owned]) throw new InvalidOperationException("Caller baseline conflicts with owned voltage entry");
                expected[input]=site.Jump[owned];
            }
        }
        Guard(IntPtr.Add(module,rva),expected);
    }

    void GuardGraph() {
        ThreadGuard(); GuardModule(pultModule,"Pult1.dll",pultPath); GuardModule(tygaModule,"Tyga1.dll",tygaPath);
        if(!graphReady || Pointer(pultModule,0x1a2e60)!=pultCell || Pointer(pultCell,0)!=pultOwner || Pointer(pultOwner,0)!=pultVmt || Pointer(pultOwner,4)!=pult || Pointer(pultOwner,8)!=tyga || Pointer(tygaModule,0x19058c)!=tygaCell || Pointer(tygaCell,0)!=outer || Pointer(outer,0)!=outerVmt || Pointer(outer,0x420)!=calc || Pointer(calc,0)!=calcVmt || Pointer(calc,4)!=tyga || Pointer(calc,0x10c)!=constants || constants!=IntPtr.Add(outer,8) || Pointer(tyga,0x27)!=result) throw new InvalidOperationException("Contact voltage owner graph changed");
        Readable(schema,0x129); Readable(tyga,305); Readable(result,26); Readable(constants,1048);
        Writable(eset,4); Writable(IntPtr.Add(schema,0x125),4); Writable(IntPtr.Add(result,0x11),4);
        int sections=Marshal.ReadInt32(tyga,8); if(sections<1 || sections>4) throw new InvalidOperationException("Contact voltage section count domain");
    }
    void GuardSource() {
        Writable(data,DataSize);
        uint bits=SingleBits(value), checksum=unchecked((uint)value)^bits^generation^Magic;
        if(Marshal.ReadInt32(data)!=Magic || Marshal.ReadInt32(data,ValueOffset)!=value || unchecked((uint)Marshal.ReadInt32(data,BitsOffset))!=bits || unchecked((uint)Marshal.ReadInt32(data,GenerationOffset))!=generation || unchecked((uint)Marshal.ReadInt32(data,ChecksumOffset))!=checksum || Marshal.ReadInt32(eset)!=expectedEset || expectedEset!=value) throw new InvalidOperationException("Contact voltage source/owned Eset changed");
    }
    void GuardRetainedFix() {
        Guard(IntPtr.Add(tygaModule,0x856000),Hex(RetainedFixHex));
        Guard(IntPtr.Add(tygaModule,0x1764a5),Hex("e9bafb6d009090"));
    }
    void PublishSource() {
        Writable(data,DataSize); uint bits=SingleBits(value);
        Marshal.WriteInt32(data,Magic); Marshal.WriteInt32(data,ValueOffset,value); Marshal.WriteInt32(data,BitsOffset,unchecked((int)bits));
        Marshal.WriteInt32(data,GenerationOffset,unchecked((int)generation)); Marshal.WriteInt32(data,ChecksumOffset,unchecked((int)(unchecked((uint)value)^bits^generation^Magic)));
    }
    void WriteEset(int next) {
        GuardGraph();
        if(Marshal.ReadInt32(eset)!=expectedEset) throw new InvalidOperationException("Foreign live Eset write; not overwritten");
        pendingEset=next; esetWritePending=true; esetOwned=true;
        Marshal.WriteInt32(eset,next);
        if(Marshal.ReadInt32(eset)!=next) throw new InvalidOperationException("Contact voltage Eset write verification");
        expectedEset=next; esetWritePending=false;
    }

    byte[] BuildCave(Site site) {
        var b=new Code(); if(site.Sequential) b.B(site.Original);
        b.B(0x9c,0x60,0xbb); b.A(data);
        b.B(0x64,0x8b,0x15); b.D(0x24); b.B(0x81,0xfa); b.D(thread); b.Jcc(0x85,"threadFault");
        b.B(0x3d); b.A(site.Sequential?tyga:site.Destination); b.Jcc(0x85,"destinationFault");
        b.B(0x8b,0x55,0xfc,0x81,0xfa); b.A(site.Owner); b.Jcc(0x85,"ownerFault");
        b.B(0x8b,0x4a,4,0x81,0xf9); b.A(site.Module==pultModule?pult:tyga); b.Jcc(0x85,"ownerFault");
        if(site.Module==pultModule) { b.B(0x8b,0x4a,8,0x81,0xf9); b.A(tyga); b.Jcc(0x85,"ownerFault"); }
        else { b.B(0x81,0x79,0x27); b.A(result); b.Jcc(0x85,"ownerFault"); }
        if(site.Sequential) {
            // Both ordinary traction excitation modes need the new supply
            // before UpdateCommand/OV/SetNextPos trial current calculations.
            // Active regenerative operation retains its native derived Uset;
            // changing-voltage regenerative transitions are a separate scope.
            b.B(0x80,0x78,0x16,1); b.Jcc(0x87,"sourceFault");
            b.B(0x80,0xba); b.D(0x100); b.B(0); b.Jcc(0x85,"done");
            b.B(0xbf); b.A(result);
        }
        else b.B(0x8b,0xf8);
        b.B(0x81,0x3b); b.D(Magic); b.Jcc(0x85,"sourceFault");
        b.B(0x83,0x7b,FaultOffset,0); b.Jcc(0x85,"done");
        b.B(0x8b,0x53,ValueOffset,0x81,0xfa); b.D(MaximumInputVoltage); b.Jcc(0x87,"sourceFault");
        b.B(0x8b,0x4b,BitsOffset,0x8b,0xf2,0x33,0xf1,0x33,0x73,GenerationOffset,0x81,0xf6); b.D(Magic);
        b.B(0x3b,0x73,ChecksumOffset); b.Jcc(0x85,"sourceFault");
        b.B(0x83,0x7b,GenerationOffset,0); b.Jcc(0x84,"sourceFault");
        b.B(0x39,0x15); b.A(eset); b.Jcc(0x85,"sourceFault");
        if(site.Sequential) { b.B(0x80,0xb8); b.D(0x130); b.B(0); b.Jcc(0x85,"seqPowered"); b.B(0x33,0xc9); b.Label("seqPowered"); }
        // EDI is the proved owned destination; preserve all original registers.
        b.B(0x89,site.FloatStore?(byte)0x8f:(byte)0x97); b.D(site.MemberOffset);
        b.B(0xff,0x43,(byte)(CountOffset+site.Index*4)); b.Jcc(0x84,"counterFault");
        b.B(0x8d,0xb7); b.D(site.MemberOffset); b.B(0x89,0x73,(byte)(DestinationOffset+site.Index*4));
        b.B(0x89,site.FloatStore?(byte)0x4b:(byte)0x53,(byte)(LastValueOffset+site.Index*4));
        b.B(0x8b,0x73,GenerationOffset,0x89,0x73,(byte)(LastGenerationOffset+site.Index*4)); b.Jmp("done");
        EmitFault(b,"threadFault",1); EmitFault(b,"ownerFault",2); EmitFault(b,"sourceFault",4); EmitFault(b,"destinationFault",8); EmitFault(b,"counterFault",16);
        b.Label("done"); b.B(0x61,0x9d,0xe9);
        b.D(unchecked(U(IntPtr.Add(site.Module,site.Rva+site.Original.Length))-U(site.Cave)-(uint)b.Count-4));
        return b.Finish();
    }
    static void EmitFault(Code b,string name,int flags) { b.Label(name); b.B(0x81,0x4b,FaultOffset); b.D(flags); b.Jmp("done"); }

    void RestoreProtection(Site site) {
        if(!site.ProtectionPending)return;
        IntPtr address=IntPtr.Add(site.Module,site.Rva); MemoryInfo info; Query(address,out info);
        if((info.Protect&255)!=0x40) throw new InvalidOperationException("Foreign voltage-entry protection change; retained");
        uint ignored; if(!VirtualProtect(address,(UIntPtr)site.Original.Length,site.PreviousProtection,out ignored)) throw WinError("Restore voltage entry protection");
        site.ProtectionPending=false;
    }
    void WriteSite(Site site,byte[] expected,byte[] replacement,bool installing) {
        RestoreProtection(site); if(site.FlushPending){Flush(IntPtr.Add(site.Module,site.Rva),site.Original.Length);site.FlushPending=false;}
        IntPtr address=IntPtr.Add(site.Module,site.Rva); Guard(address,expected); RequireRx(address);
        if(!VirtualProtect(address,(UIntPtr)replacement.Length,0x40,out site.PreviousProtection)) throw WinError("Protect voltage entry writable");
        site.ProtectionPending=true;
        try { if(installing)site.MayExist=true; Marshal.Copy(replacement,0,address,replacement.Length);site.FlushPending=true;Guard(address,replacement);if(!installing)site.MayExist=false; }
        finally { RestoreProtection(site); }
        Flush(address,replacement.Length);site.FlushPending=false;
    }

    public void Dispose() {
        lock(OwnerLock) {
            if(disposed)return; ThreadGuard(); closing=true; var errors=new List<Exception>();
            if(sites!=null)foreach(Site site in sites)try {
                RestoreProtection(site);
                if(site.FlushPending){Flush(IntPtr.Add(site.Module,site.Rva),site.Original.Length);site.FlushPending=false;}
                if(site.MayExist) {
                    GuardModule(site.Module,site.Module==pultModule?"Pult1.dll":"Tyga1.dll",site.Module==pultModule?pultPath:tygaPath);
                    byte[] current=Read(IntPtr.Add(site.Module,site.Rva),site.Original.Length);
                    if(Equal(current,site.Jump))WriteSite(site,site.Jump,site.Original,false);
                    else if(Equal(current,site.Original))site.MayExist=false;
                    else throw new InvalidOperationException("Foreign voltage entry; not overwritten");
                }
                if(site.Jump!=null){Guard(IntPtr.Add(site.Module,site.Rva),site.Original);RequireRx(IntPtr.Add(site.Module,site.Rva));}
            } catch(Exception error){errors.Add(error);}
            if(esetOwned)try {
                GuardGraph(); int actual=Marshal.ReadInt32(eset);
                if(actual!=expectedEset && !(esetWritePending&&actual==pendingEset))throw new InvalidOperationException("Foreign Eset on close; not overwritten");
                Marshal.WriteInt32(eset,originalEset); if(Marshal.ReadInt32(eset)!=originalEset)throw new InvalidOperationException("Original Eset restoration failed");
                expectedEset=originalEset;esetOwned=false;esetWritePending=false;
            } catch(Exception error){errors.Add(error);}
            if(errors.Count!=0)throw new AggregateException("Contact voltage restoration incomplete; ownership retained",errors);
            // Repeat original-entry proof immediately before any allocation free,
            // including retry after an earlier protection/flush failure.
            if(sites!=null)foreach(Site site in sites)if(site.Jump!=null){GuardModule(site.Module,site.Module==pultModule?"Pult1.dll":"Tyga1.dll",site.Module==pultModule?pultPath:tygaPath);Guard(IntPtr.Add(site.Module,site.Rva),site.Original);RequireRx(IntPtr.Add(site.Module,site.Rva));}
            if(sites!=null)foreach(Site site in sites)Free(ref site.Cave);
            Free(ref data); Release(ref pinnedTyga); Release(ref pinnedPult); disposed=true;Owners.Remove(pultModule);
        }
    }

    void ThreadGuard(){if(GetCurrentThreadId()!=thread)throw new InvalidOperationException("Contact voltage thread guard");}
    static void Pin(IntPtr module,ref IntPtr retained){if(!GetModuleHandleEx(4,module,out retained)||retained!=module)throw WinError("Retain contact voltage native module");}
    static void Release(ref IntPtr module){if(module==IntPtr.Zero)return;if(!FreeLibrary(module))throw WinError("Release voltage module reference");module=IntPtr.Zero;}
    static void GuardModule(IntPtr module,string name,string path){if(GetModuleHandle(name)!=module)throw new InvalidOperationException("Voltage module lifetime changed");var actual=new StringBuilder(32768);uint count=GetModuleFileName(module,actual,actual.Capacity);if(count==0||count>=actual.Capacity||!String.Equals(Path.GetFullPath(actual.ToString()),path,StringComparison.OrdinalIgnoreCase))throw new InvalidOperationException("Voltage loaded path guard");}
    static void GuardFile(string path,string expected){using(var file=File.OpenRead(path))using(var hash=SHA256.Create())if(BitConverter.ToString(hash.ComputeHash(file)).Replace("-","")!=expected)throw new InvalidOperationException("Voltage source SHA guard");}
    static void RequireModuleAllocation(IntPtr at,IntPtr module){MemoryInfo info;Query(at,out info);if(info.Allocation!=module)throw new InvalidOperationException("Voltage native class allocation guard");}
    static void Query(IntPtr at,out MemoryInfo info){if(at==IntPtr.Zero||VirtualQuery(at,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)throw new InvalidOperationException("Voltage memory absent");}
    static void Readable(IntPtr at,int count){MemoryInfo info;Query(at,out info);ulong first=U(info.Base),start=U(at);uint p=info.Protect&255;if(count<1||info.State!=0x1000||(info.Protect&0x101)!=0||start<first||start+(uint)count>first+info.Size.ToUInt32()||p!=2&&p!=4&&p!=8&&p!=0x20&&p!=0x40&&p!=0x80)throw new InvalidOperationException("Voltage readable range guard");}
    static void Writable(IntPtr at,int count){Readable(at,count);MemoryInfo info;Query(at,out info);uint p=info.Protect&255;if(p!=4&&p!=8&&p!=0x40&&p!=0x80)throw new InvalidOperationException("Voltage writable range guard");}
    static void RequireRx(IntPtr at){MemoryInfo info;Query(at,out info);if(info.State!=0x1000||(info.Protect&0x101)!=0||(info.Protect&255)!=0x20)throw new InvalidOperationException("Voltage native code RX guard");}
    static IntPtr Pointer(IntPtr at,int offset){IntPtr p=IntPtr.Add(at,offset);Readable(p,4);return Marshal.ReadIntPtr(p);}
    static byte[] Read(IntPtr at,int count){Readable(at,count);var bytes=new byte[count];Marshal.Copy(at,bytes,0,count);return bytes;}
    static void Guard(IntPtr at,byte[] expected){if(!Equal(Read(at,expected.Length),expected))throw new InvalidOperationException("Voltage byte guard at "+at.ToString("X"));}
    static bool Equal(byte[] a,byte[] b){if(a==null||b==null||a.Length!=b.Length)return false;for(int i=0;i<a.Length;i++)if(a[i]!=b[i])return false;return true;}
    static IntPtr Allocate(int count){IntPtr at=VirtualAlloc(IntPtr.Zero,(UIntPtr)count,0x3000,4);if(at==IntPtr.Zero)throw WinError("Allocate voltage memory");return at;}
    static void Free(ref IntPtr at){if(at==IntPtr.Zero)return;if(!VirtualFree(at,UIntPtr.Zero,0x8000))throw WinError("Free voltage memory");at=IntPtr.Zero;}
    static void Flush(IntPtr at,int count){if(!FlushInstructionCache(new IntPtr(-1),at,(UIntPtr)count))throw WinError("Flush voltage instruction cache");}
    static uint SingleBits(int v){return BitConverter.ToUInt32(BitConverter.GetBytes((float)v),0);}
    static uint U(IntPtr at){return unchecked((uint)at.ToInt32());}
    static byte[] Hex(string text){var bytes=new byte[text.Length/2];for(int i=0;i<bytes.Length;i++)bytes[i]=Convert.ToByte(text.Substring(i*2,2),16);return bytes;}
    static byte[] Jump(IntPtr from,IntPtr to,int count){var bytes=new byte[count];bytes[0]=0xe9;Array.Copy(BitConverter.GetBytes(unchecked(U(to)-U(from)-5)),0,bytes,1,4);for(int i=5;i<count;i++)bytes[i]=0x90;return bytes;}
    static Exception WinError(string message){return new Win32Exception(Marshal.GetLastWin32Error(),message);}
    sealed class Code {
        readonly List<byte> bytes=new List<byte>();readonly Dictionary<string,int> labels=new Dictionary<string,int>();readonly List<KeyValuePair<int,string>> refs=new List<KeyValuePair<int,string>>();
        public int Count{get{return bytes.Count;}} public void B(params byte[] v){bytes.AddRange(v);}public void D(int v){bytes.AddRange(BitConverter.GetBytes(v));}public void D(uint v){bytes.AddRange(BitConverter.GetBytes(v));}public void A(IntPtr v){D(U(v));}
        public void Label(string name){labels.Add(name,Count);}public void Jcc(byte op,string name){B(0x0f,op);refs.Add(new KeyValuePair<int,string>(Count,name));D(0);}public void Jmp(string name){B(0xe9);refs.Add(new KeyValuePair<int,string>(Count,name));D(0);}
        public byte[] Finish(){foreach(var reference in refs){int target;if(!labels.TryGetValue(reference.Value,out target))throw new InvalidOperationException("Missing voltage cave label "+reference.Value);byte[] delta=BitConverter.GetBytes(target-reference.Key-4);for(int i=0;i<4;i++)bytes[reference.Key+i]=delta[i];}return bytes.ToArray();}
    }
}
