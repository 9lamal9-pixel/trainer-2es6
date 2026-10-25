using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;

// Reconcile only the SA47 command producer. No pneumatic output is written.
// The host owns the native graph on this thread until this patch is disposed.
public sealed class TrainerSa47MemoryPatch : IDisposable {
    const int ProducerRva = 0x18c665, EntryRva = 0x18c7a6, ReturnRva = 0x18c7ac;
    const int GlobalRva = 0x1a2e60, CellRva = 0x1bc028;
    const int Magic = 0x37344153, PageSize = 4096;
    const int Enabled = 4, Memory = 8, Previous = 9, Initialized = 10, Error = 12;
    const int Entries = 16, LastRaw = 20, Candidate = 21, Final = 22, CancelReason = 23;
    const int LastTmBits = 24, ThresholdBits = 28;
    const string ProducerHex =
        "8b45fce8bff2ffff8b55fc8b52048b52040fb6928c0000008990040100008b45fce8a1f2ffff83b804010000007e2d8b45fc8b40048b400480787d04771e8b45fc8b40048b400480787e02770f8b45fce872f2ffffc68003010000018b45fc8b40048b400480787d05730f8b45fc8b40048b400480787e03760f8b45fce845f2ffffc68008010000008b45fce836f2ffff8b800401000083e80172077425487442eb6d8b45fce81cf2ffffc68009010000008b45fce80df2ffffc6800a01000000eb4d8b45fce8fcf1ffffc68009010000018b45fce8edf1ffffc6800a01000000eb2d8b45fce8dcf1ffffc68009010000018b45fce8cdf1ffffc6800a010000018b45fce8bef1ffffc68008010000018b45fce8aff1ffff80b8080100000074118b45fce89ef1ffff80b80901000000750f8b45fce88df1ffffc6800301000000";
    static readonly byte[] OriginalEntry = { 0x8b, 0x45, 0xfc, 0x8b, 0x40, 0x04 };
    static readonly byte[] ProducerBytes = Hex(ProducerHex);

    [StructLayout(LayoutKind.Sequential)]
    struct MemoryInfo {
        public IntPtr Base, Allocation;
        public uint AllocationProtect;
        public UIntPtr Size;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr address, out MemoryInfo info, UIntPtr size);
    [DllImport("kernel32.dll", SetLastError = true)] static extern IntPtr VirtualAlloc(IntPtr address, UIntPtr size, uint allocation, uint protection);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool VirtualFree(IntPtr address, UIntPtr size, uint kind);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool VirtualProtect(IntPtr address, UIntPtr size, uint protection, out uint previous);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool FlushInstructionCache(IntPtr process, IntPtr address, UIntPtr size);

    readonly IntPtr module, owner, pult, loc, headTm, cell, vmt, entry;
    readonly uint thread, thresholdBits;
    IntPtr state, cave;
    byte[] caveBytes, entryBytes;
    bool installed, disposed, protectionPending, flushPending;
    uint originalProtection;
    string lastFault = "";

    public TrainerSa47MemoryPatch(IntPtr pultModule, IntPtr owner, IntPtr pult, IntPtr loc, IntPtr headTm, float threshold) {
        if (IntPtr.Size != 4) throw new Exception("SA47 producer requires x86");
        if (float.IsNaN(threshold) || float.IsInfinity(threshold) || threshold <= 0)
            throw new ArgumentOutOfRangeException("threshold", "SA47 threshold must be positive and finite in native pressure units");
        thread = GetCurrentThreadId();
        module = pultModule; this.owner = owner; this.pult = pult; this.loc = loc; this.headTm = headTm;
        cell = IntPtr.Add(module, CellRva); entry = IntPtr.Add(module, EntryRva);
        thresholdBits = BitConverter.ToUInt32(BitConverter.GetBytes(threshold), 0);
        if (module == IntPtr.Zero || GetModuleHandle("Pult1.dll") != module) throw new Exception("SA47 producer module guard");
        vmt = Pointer(owner, 0);
        if (vmt == IntPtr.Zero || vmt != Pointer(module, 0x18b5c8)) throw new Exception("SA47 producer class guard");
        MemoryInfo info;
        if (VirtualQuery(vmt, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero || info.Allocation != module)
            throw new Exception("SA47 producer class allocation guard");
        GuardGraph();
        CheckBytes(IntPtr.Add(module, 0x18b92c), Hex("558bec83c4f88945fc8b45fc83c0188945f88b45f859595dc3"));
        CheckBytes(IntPtr.Add(module, ProducerRva), ProducerBytes);
        CheckBytes(entry, OriginalEntry);
        try {
            state = VirtualAlloc(IntPtr.Zero, (UIntPtr)PageSize, 0x3000, 0x04);
            if (state == IntPtr.Zero) throw new Exception("SA47 producer state allocation");
            Marshal.WriteInt32(state, 0, Magic);
            Marshal.WriteInt32(state, Enabled, 1);
            Marshal.WriteInt32(state, ThresholdBits, unchecked((int)thresholdBits));
            cave = VirtualAlloc(IntPtr.Zero, (UIntPtr)PageSize, 0x3000, 0x04);
            if (cave == IntPtr.Zero) throw new Exception("SA47 producer code allocation");
            caveBytes = BuildCave();
            if (caveBytes.Length > PageSize) throw new Exception("SA47 producer code size");
            Marshal.Copy(caveBytes, 0, cave, caveBytes.Length);
            uint previous;
            if (!VirtualProtect(cave, (UIntPtr)PageSize, 0x20, out previous)) throw new Exception("SA47 producer RX protection");
            Flush(cave, caveBytes.Length);
            entryBytes = Jump(entry, cave, OriginalEntry.Length);
            WriteEntry(OriginalEntry, entryBytes, true);
            Validate();
        } catch (Exception original) {
            try { Dispose(); }
            catch (Exception cleanup) {
                throw new Exception("SA47 producer installation failed; unsafe-to-free allocations retained", new AggregateException(original, cleanup));
            }
            throw;
        }
    }

    public string Fault {
        get {
            ThreadGuard();
            if (state != IntPtr.Zero) {
                CheckMemory(state, 32, false);
                lastFault = DescribeFault(Marshal.ReadInt32(state, Error));
            }
            return lastFault;
        }
    }

    public void Validate() {
        if (disposed) throw new ObjectDisposedException("TrainerSa47MemoryPatch");
        GuardGraph();
        if (!installed || state == IntPtr.Zero || cave == IntPtr.Zero) throw new Exception("SA47 producer hook absent");
        CheckMemory(state, 32, true);
        if (Marshal.ReadInt32(state, 0) != Magic || Marshal.ReadInt32(state, Enabled) != 1 ||
            unchecked((uint)Marshal.ReadInt32(state, ThresholdBits)) != thresholdBits)
            throw new Exception("SA47 producer private state guard");
        CheckBytes(IntPtr.Add(module, ProducerRva), ProducerBytes);
        CheckBytes(entry, entryBytes);
        CheckBytes(cave, caveBytes);
        string fault = Fault;
        if (fault.Length != 0) throw new Exception("SA47 producer fault: " + fault);
        if (Marshal.ReadByte(state, Memory) > 1 || Marshal.ReadByte(state, Previous) > 2 || Marshal.ReadByte(state, Initialized) > 1)
            throw new Exception("SA47 producer private domain guard");
    }

    public Dictionary<string, object> Snapshot() {
        Validate();
        return new Dictionary<string, object> {
            { "Entries", unchecked((uint)Marshal.ReadInt32(state, Entries)) },
            { "LastRaw", (int)Marshal.ReadByte(state, LastRaw) },
            { "Memory", Marshal.ReadByte(state, Memory) == 1 },
            { "PreviousLogicalRaw", (int)Marshal.ReadByte(state, Previous) },
            { "Initialized", Marshal.ReadByte(state, Initialized) == 1 },
            { "NativeCandidate", (int)Marshal.ReadByte(state, Candidate) },
            { "FinalRequest", (int)Marshal.ReadByte(state, Final) },
            { "CancelReason", (int)Marshal.ReadByte(state, CancelReason) },
            { "TMbits", unchecked((uint)Marshal.ReadInt32(state, LastTmBits)) },
            { "ThresholdBits", thresholdBits },
            { "Fault", Fault }
        };
    }

    void ThreadGuard() {
        if (GetCurrentThreadId() != thread) throw new Exception("SA47 producer thread guard");
    }

    void GuardGraph() {
        ThreadGuard();
        if (module == IntPtr.Zero || GetModuleHandle("Pult1.dll") != module || Pointer(module, GlobalRva) != cell ||
            Pointer(cell, 0) != owner || Pointer(owner, 0) != vmt || Pointer(owner, 4) != pult ||
            Pointer(pult, 4) != loc || Pointer(pult, 8) != headTm)
            throw new Exception("SA47 producer owner graph guard");
        CheckMemory(owner, 0x18 + 0x10b, false);
        CheckMemory(IntPtr.Add(loc, 0x7d), 0x10, false);
        CheckMemory(headTm, 4, false);
        CheckMemory(IntPtr.Add(owner, 0x18 + 0x103), 1, true);
        CheckMemory(IntPtr.Add(owner, 0x18 + 0x108), 1, true);
    }

    static IntPtr Pointer(IntPtr address, int offset) {
        IntPtr at = IntPtr.Add(address, offset); CheckMemory(at, 4, false); return Marshal.ReadIntPtr(at);
    }

    static void CheckMemory(IntPtr address, int count, bool writable) {
        MemoryInfo info;
        if (address == IntPtr.Zero || count < 1 || VirtualQuery(address, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero)
            throw new Exception("SA47 producer memory absent");
        ulong start = unchecked((uint)address.ToInt32()), first = unchecked((uint)info.Base.ToInt32());
        uint protect = info.Protect & 255;
        if (info.State != 0x1000 || (info.Protect & 0x101) != 0 || start < first || start + (uint)count > first + info.Size.ToUInt32() ||
            protect != 2 && protect != 4 && protect != 8 && protect != 0x20 && protect != 0x40 && protect != 0x80)
            throw new Exception("SA47 producer memory range guard");
        if (writable && protect != 4 && protect != 8 && protect != 0x40 && protect != 0x80)
            throw new Exception("SA47 producer memory is not writable");
    }

    static void CheckBytes(IntPtr address, byte[] expected) {
        CheckMemory(address, expected.Length, false);
        for (int i = 0; i < expected.Length; i++)
            if (Marshal.ReadByte(address, i) != expected[i]) throw new Exception("SA47 producer byte guard");
    }

    static void Flush(IntPtr address, int count) {
        if (!FlushInstructionCache(new IntPtr(-1), address, (UIntPtr)count)) throw new Exception("SA47 producer instruction cache flush");
    }

    void RestoreEntryProtection() {
        if (!protectionPending) return;
        uint ignored;
        if (!VirtualProtect(entry, (UIntPtr)OriginalEntry.Length, originalProtection, out ignored))
            throw new Exception("SA47 producer entry protection restoration");
        protectionPending = false;
    }

    void WriteEntry(byte[] expected, byte[] replacement, bool installing) {
        RestoreEntryProtection();
        CheckBytes(entry, expected);
        if (!VirtualProtect(entry, (UIntPtr)replacement.Length, 0x40, out originalProtection))
            throw new Exception("SA47 producer entry writable protection");
        protectionPending = true;
        try {
            // Remember attempted installation before a write: never free a cave
            // that might be referenced by a partially completed entry mutation.
            if (installing) installed = true;
            Marshal.Copy(replacement, 0, entry, replacement.Length);
            CheckBytes(entry, replacement);
            if (!installing) installed = false;
            flushPending = true;
        } finally { RestoreEntryProtection(); }
        Flush(entry, replacement.Length); flushPending = false;
    }

    public void Dispose() {
        if (disposed) return;
        ThreadGuard();
        if (state != IntPtr.Zero) {
            CheckMemory(state, 32, false);
            lastFault = DescribeFault(Marshal.ReadInt32(state, Error));
        }
        if (protectionPending) RestoreEntryProtection();
        if (flushPending) { Flush(entry, OriginalEntry.Length); flushPending = false; }
        if (installed) {
            if (GetModuleHandle("Pult1.dll") != module) throw new Exception("SA47 producer module changed before restoration; allocations retained");
            // A foreign entry is never overwritten, and its possible target is
            // never freed. This refusal intentionally leaves the object live.
            CheckBytes(entry, entryBytes);
            WriteEntry(entryBytes, OriginalEntry, false);
        }
        // Verify again even when a prior restoration changed installed=false
        // and then failed during protection/cache cleanup. Never infer the
        // absence of a reference into the cave from the Boolean flag alone.
        if (entryBytes != null) {
            if (GetModuleHandle("Pult1.dll") != module)
                throw new Exception("SA47 producer module changed before free; allocations retained");
            CheckBytes(entry, OriginalEntry);
        } else if (installed) throw new Exception("SA47 producer unpublished entry ownership guard");
        // entryBytes==null proves no entry installation was attempted: it is
        // assigned before WriteEntry and is never cleared during this lifetime.
        if (cave != IntPtr.Zero) {
            if (!VirtualFree(cave, UIntPtr.Zero, 0x8000)) throw new Exception("SA47 producer code free");
            cave = IntPtr.Zero;
        }
        if (state != IntPtr.Zero) {
            if (!VirtualFree(state, UIntPtr.Zero, 0x8000)) throw new Exception("SA47 producer state free");
            state = IntPtr.Zero;
        }
        disposed = true;
    }

    byte[] BuildCave() {
        var b = new Code();
        b.B(0x9c, 0x60, 0xbb); b.D(Ptr(state)); // pushfd; pushad; mov ebx,state
        b.B(0x81, 0x3b); b.D(Magic); b.Jcc(0x85, "privateFault");
        b.B(0x83, 0x7b, Enabled, 1); b.Jcc(0x85, "privateFault");
        // Windows x86 TEB.ClientId.UniqueThread; no helper call or FPU use.
        b.B(0x64, 0xa1); b.D(0x24); b.B(0x3d); b.D(thread); b.Jcc(0x85, "threadFault");
        b.B(0x8b, 0x45, 0xfc, 0x3d); b.D(Ptr(owner)); b.Jcc(0x85, "ownerFault");
        b.B(0x8b, 0x15); b.D(Ptr(IntPtr.Add(module, GlobalRva)));
        b.B(0x81, 0xfa); b.D(Ptr(cell)); b.Jcc(0x85, "ownerFault");
        b.B(0x39, 0x02); b.Jcc(0x85, "ownerFault");
        b.B(0x8b, 0x10, 0x81, 0xfa); b.D(Ptr(vmt)); b.Jcc(0x85, "ownerFault");
        b.B(0x8b, 0x50, 4, 0x81, 0xfa); b.D(Ptr(pult)); b.Jcc(0x85, "ownerFault");
        b.B(0x8b, 0x72, 4, 0x81, 0xfe); b.D(Ptr(loc)); b.Jcc(0x85, "ownerFault");
        b.B(0x8b, 0x52, 8, 0x81, 0xfa); b.D(Ptr(headTm)); b.Jcc(0x85, "ownerFault");
        b.B(0x8d, 0x78, 0x18, 0xff, 0x43, Entries); // edi=schema; inc entries
        b.B(0x83, 0x7b, Error, 0); b.Jcc(0x85, "neutralOwned");
        b.B(0x80, 0x7b, Memory, 1); b.Jcc(0x87, "ownedPrivateFault");
        b.B(0x80, 0x7b, Previous, 2); b.Jcc(0x87, "ownedPrivateFault");
        b.B(0x80, 0x7b, Initialized, 1); b.Jcc(0x87, "ownedPrivateFault");
        b.B(0x8b, 0x8f); b.D(0x104); b.B(0x83, 0xf9, 2); b.Jcc(0x87, "rawFault");
        b.B(0x0f, 0xb6, 0x86); b.D(0x8c); b.B(0x3b, 0xc8); b.Jcc(0x85, "rawFault");
        foreach (int offset in new[] { 0x103, 0x108, 0x109, 0x10a }) {
            b.B(0x80, 0xbf); b.D(offset); b.B(1); b.Jcc(0x87, "booleanFault");
        }
        b.B(0x88, 0x4b, LastRaw, 0x8a, 0x87); b.D(0x103); b.B(0x88, 0x43, Candidate);
        b.B(0xc6, 0x43, CancelReason, 0);
        // edx still points to headTm. Compare float representations as integers.
        b.B(0x8b, 0x02, 0x89, 0x43, LastTmBits, 0x8b, 0xd0, 0x81, 0xe2); b.D(0x7fffffff);
        b.B(0x81, 0xfa); b.D(0x7f800000); b.Jcc(0x83, "nonFiniteFault");
        b.B(0x85, 0xc0); b.Jcc(0x89, "positiveTm");
        b.B(0x81, 0xfa); b.D(BitConverter.ToUInt32(BitConverter.GetBytes(0.001f), 0));
        b.Jcc(0x87, "negativeTmFault"); b.B(0x33, 0xc0); b.Jmp("tmValid");
        b.Label("positiveTm"); b.B(0x8b, 0xc2);
        b.Label("tmValid"); b.B(0x33, 0xd2); // edx=edge flag, eax=nonnegative TM bits
        b.B(0x80, 0x7b, Initialized, 0); b.Jcc(0x85, "findEdge");
        b.B(0xc6, 0x43, Initialized, 1); b.Jmp("rememberRaw");
        b.Label("findEdge"); b.B(0x83, 0xf9, 2); b.Jcc(0x85, "rememberRaw");
        b.B(0x80, 0x7b, Previous, 1); b.Jcc(0x85, "rememberRaw"); b.B(0xb2, 1);
        b.Label("rememberRaw"); b.B(0x88, 0x4b, Previous, 0x83, 0xf9, 0); b.Jcc(0x84, "cancelZero");
        b.B(0x3d); b.D(thresholdBits); b.Jcc(0x87, "cancelTm");
        b.B(0x85, 0xd2); b.Jcc(0x84, "publish"); b.B(0xc6, 0x43, Memory, 1); b.Jmp("publish");
        b.Label("cancelZero"); b.B(0xc6, 0x43, CancelReason, 1); b.Jmp("clearMemory");
        b.Label("cancelTm"); b.B(0xc6, 0x43, CancelReason, 2);
        b.Label("clearMemory"); b.B(0xc6, 0x43, Memory, 0);
        b.Label("publish"); b.B(0x8a, 0x43, Memory, 0x88, 0x87); b.D(0x108);
        b.B(0x84, 0xc0); b.Jcc(0x84, "suppress");
        b.B(0x80, 0x7e, 0x7d, 4); b.Jcc(0x87, "suppress");
        b.B(0x80, 0x7e, 0x7e, 2); b.Jcc(0x87, "suppress");
        b.B(0x8a, 0x43, Candidate, 0x88, 0x87); b.D(0x103);
        b.B(0x88, 0x43, Final); b.Jmp("finish");
        b.Label("suppress"); b.B(0xc6, 0x87); b.D(0x103); b.B(0, 0xc6, 0x43, Final, 0); b.Jmp("finish");
        EmitFault(b, "ownerFault", 1, false);
        EmitFault(b, "rawFault", 2, true);
        EmitFault(b, "booleanFault", 3, true);
        EmitFault(b, "nonFiniteFault", 4, true);
        EmitFault(b, "negativeTmFault", 5, true);
        EmitFault(b, "threadFault", 6, false);
        EmitFault(b, "privateFault", 7, false);
        EmitFault(b, "ownedPrivateFault", 7, true);
        b.Label("neutralOwned");
        b.B(0xc6, 0x43, Memory, 0, 0xc6, 0x43, Final, 0, 0xc6, 0x43, CancelReason, 4);
        b.B(0xc6, 0x87); b.D(0x103); b.B(0, 0xc6, 0x87); b.D(0x108); b.B(0); b.Jmp("finish");
        b.Label("finish"); b.B(0x61, 0x9d); b.B(OriginalEntry);
        b.B(0xe9); int at = b.Count; b.D(unchecked(Ptr(IntPtr.Add(module, ReturnRva)) - (Ptr(cave) + (uint)at + 4)));
        return b.Finish();
    }

    static void EmitFault(Code b, string label, int value, bool owned) {
        b.Label(label); b.B(0x83, 0x7b, Error, 0); b.Jcc(0x85, label + "Recorded");
        b.B(0xc7, 0x43, Error); b.D(value); b.Label(label + "Recorded");
        if (owned) b.Jmp("neutralOwned");
        else { b.B(0xc6, 0x43, Memory, 0, 0xc6, 0x43, Final, 0); b.Jmp("finish"); }
    }

    static string DescribeFault(int value) {
        switch (value) {
            case 0: return "";
            case 1: return "native owner graph mismatch; foreign schema not written";
            case 2: return "logical raw/domain mismatch";
            case 3: return "native Boolean domain mismatch";
            case 4: return "head TM is NaN or infinity";
            case 5: return "head TM is below -0.001";
            case 6: return "native producer called on another thread; schema not written";
            case 7: return "private state/domain mismatch";
            default: return "unknown fault " + value;
        }
    }

    static uint Ptr(IntPtr value) { return unchecked((uint)value.ToInt32()); }
    static byte[] Hex(string value) {
        var result = new byte[value.Length / 2];
        for (int i = 0; i < result.Length; i++) result[i] = Convert.ToByte(value.Substring(i * 2, 2), 16);
        return result;
    }
    static byte[] Jump(IntPtr from, IntPtr to, int length) {
        var result = new byte[length]; result[0] = 0xe9;
        Array.Copy(BitConverter.GetBytes(unchecked(Ptr(to) - Ptr(from) - 5)), 0, result, 1, 4);
        for (int i = 5; i < result.Length; i++) result[i] = 0x90;
        return result;
    }
    sealed class Code {
        readonly List<byte> bytes = new List<byte>();
        readonly Dictionary<string, int> labels = new Dictionary<string, int>();
        readonly List<KeyValuePair<int, string>> references = new List<KeyValuePair<int, string>>();
        public int Count { get { return bytes.Count; } }
        public void B(params byte[] values) { bytes.AddRange(values); }
        public void D(int value) { bytes.AddRange(BitConverter.GetBytes(value)); }
        public void D(uint value) { bytes.AddRange(BitConverter.GetBytes(value)); }
        public void Label(string value) { labels.Add(value, Count); }
        void Reference(string value) { references.Add(new KeyValuePair<int, string>(Count, value)); D(0); }
        public void Jcc(byte condition, string label) { B(0x0f, condition); Reference(label); }
        public void Jmp(string label) { B(0xe9); Reference(label); }
        public byte[] Finish() {
            foreach (var reference in references) {
                int target;
                if (!labels.TryGetValue(reference.Value, out target)) throw new Exception("SA47 code label missing: " + reference.Value);
                byte[] offset = BitConverter.GetBytes(target - reference.Key - 4);
                for (int i = 0; i < 4; i++) bytes[reference.Key + i] = offset[i];
            }
            return bytes.ToArray();
        }
    }
}
