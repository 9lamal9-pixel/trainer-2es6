using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// Read-only authority binding for the already initialized software SAUT channel.
// The host owns initialization, Proceed_saut, shutdown and all input adapters.
// This class never invokes a SAUT function or writes a native record.
public sealed class TrainerStandaloneSautBinding : IDisposable {
    const string SautSha = "977B4B8BD3D3EB98BF9E2F08A8C413525D85F42085D34ABBE842F2BB79384A92";
    const string CommunicationSha = "27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F";
    const int OwnerGlobalRva = 0x1c70c8;
    const int ProceedRva = 0x1b4a74;

    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)]
    static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Ansi)]
    static extern IntPtr GetProcAddress(IntPtr module, string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
    static extern uint GetModuleFileName(IntPtr module, StringBuilder path, int capacity);
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {
        public IntPtr Base, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll")]
    static extern UIntPtr VirtualQuery(IntPtr address, out MemoryInfo info, UIntPtr size);

    readonly uint thread;
    readonly string sautPath, communicationPath;
    IntPtr saut, communication, canonicalPb, commonOut, pb, loc, outp, commOwner;
    IntPtr klubButton, avpButton, inputParam, wagonParam, commonButtons, pbButtons;
    static readonly int[] InputOffsets = { 0, 4, 8, 0x10, 0x14 };
    IntPtr ownerSlot, owner, ownerVmt, commVmt, communicationRecord, proceed;
    Action<string> log;
    bool disposed, outputsCaptured, brakeRequested, lapRequested, tractionCutRequested;
    int effectiveLimit;

    // runtimeDir is the WORK_TRENAGER_3D runtime root, not its RESOURCES folder.
    public static TrainerStandaloneSautBinding Attach(IntPtr module, IntPtr canonicalPB,
        IntPtr commonOutput, IntPtr privatePB, IntPtr privateOutput, IntPtr loc,
        IntPtr commOwner, string runtimeDir, Action<string> log) {
        return new TrainerStandaloneSautBinding(module, canonicalPB, commonOutput,
            privatePB, privateOutput, loc, commOwner, runtimeDir, log);
    }

    TrainerStandaloneSautBinding(IntPtr module, IntPtr shared, IntPtr sharedOutput,
        IntPtr privateShared, IntPtr privateOutput, IntPtr location,
        IntPtr communicationOwner, string runtimeDir, Action<string> writer) {
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Standalone SAUT binding requires x86");
        if (module == IntPtr.Zero || shared == IntPtr.Zero || sharedOutput == IntPtr.Zero ||
            privateShared == IntPtr.Zero || privateOutput == IntPtr.Zero ||
            location == IntPtr.Zero || communicationOwner == IntPtr.Zero)
            throw new ArgumentException("Standalone SAUT native pointer absent");
        if (String.IsNullOrWhiteSpace(runtimeDir)) throw new ArgumentException("Standalone SAUT runtime root absent", "runtimeDir");
        thread = GetCurrentThreadId(); log = writer;
        string resources = Path.Combine(Path.GetFullPath(runtimeDir), "RESOURCES");
        sautPath = Path.GetFullPath(Path.Combine(resources, "Saut_Dll.dll"));
        communicationPath = Path.GetFullPath(Path.Combine(resources, @"LOCOMOTIVES\1\Communication1.dll"));
        saut = module; canonicalPb = shared; commonOut = sharedOutput;
        pb = privateShared; outp = privateOutput; loc = location; commOwner = communicationOwner;
        communication = GetModuleHandle("Communication1.dll");
        CheckModule(saut, "Saut_Dll.dll", sautPath, SautSha);
        CheckModule(communication, "Communication1.dll", communicationPath, CommunicationSha);

        ownerSlot = ReadPointer(saut, OwnerGlobalRva);
        owner = ReadPointer(ownerSlot, 0);
        ownerVmt = RequireObjectVmt(owner, saut, "TUpdateSAUTClass");
        commVmt = RequireObjectVmt(commOwner, communication, "TComm2ES6");
        communicationRecord = ReadPointer(commOwner, 4);
        klubButton = ReadPointer(canonicalPb, 0); avpButton = ReadPointer(canonicalPb, 4);
        inputParam = ReadPointer(canonicalPb, 8); wagonParam = ReadPointer(canonicalPb, 0x14);
        commonButtons = ReadPointer(communicationRecord, 0);
        pbButtons = ReadPointer(klubButton, 0x58);
        proceed = GetProcAddress(saut, "Proceed_saut");
        GuardGraph();
        SafeLog("Standalone SAUT authority verified; private producer output, shared real inputs and native brake requests connected read-only");
    }

    public bool Connected {
        get {
            if (disposed) return false;
            GuardGraph(); return true;
        }
    }
    public bool SnapshotAvailable {
        get { if (disposed) return false; GuardGraph(); return outputsCaptured; }
    }
    public bool BrakeRequested {
        get { RequireSnapshot(); return brakeRequested; }
    }
    public bool LapRequested {
        get { RequireSnapshot(); return lapRequested; }
    }
    public bool TractionCutRequested {
        get { RequireSnapshot(); return tractionCutRequested; }
    }
    public int EffectiveLimitKmh { get { RequireSnapshot(); return effectiveLimit; } }
    // The native ALS/SAUT mode is read, never selected by this binding.
    public int NativeMode {
        get { GuardGraph(); RequireReadable(IntPtr.Add(owner, 0x151), 1); return Marshal.ReadByte(owner, 0x151); }
    }
    // Call immediately after this DLL's own Proceed_saut. Its effective limit and
    // cut requests belong to the private output; brake/lap remain real shared Loc outputs.
    // The initial host attachment may capture a baseline.
    public void CaptureOutputs() {
        try {
            GuardGraph();
            bool nextBrake = ReadFlag(loc, 0x8a), nextLap = ReadFlag(loc, 0x8b);
            bool nextCut = ReadFlag(outp, 0), nextTractionCut = ReadFlag(outp, 0xf);
            brakeRequested = nextBrake; lapRequested = nextLap;
            tractionCutRequested = nextCut || nextTractionCut;
            effectiveLimit=Marshal.ReadInt32(outp,0xa);
            outputsCaptured = true;
        } catch { outputsCaptured = false; throw; }
    }
    void RequireSnapshot() {
        GuardGraph();
        if (!outputsCaptured) throw new Exception("Standalone SAUT output snapshot unavailable");
    }

    void ThreadGuard() {
        if (disposed) throw new ObjectDisposedException("TrainerStandaloneSautBinding");
        if (GetCurrentThreadId() != thread)
            throw new Exception("Standalone SAUT binding must run on its native owner thread");
    }
    void GuardGraph() {
        ThreadGuard();
        CheckModuleLifetime(saut, "Saut_Dll.dll", sautPath);
        CheckModuleLifetime(communication, "Communication1.dll", communicationPath);
        RequireReadable(canonicalPb, 24); RequireReadable(commonOut, 106);
        RequireReadable(pb, 24); RequireReadable(loc, 0x8c); RequireReadable(outp, 106);
        RequireReadable(commOwner, 0x278); RequireReadable(communicationRecord, 0x18);
        if (ReadPointer(saut, OwnerGlobalRva) != ownerSlot || ReadPointer(ownerSlot, 0) != owner ||
            RequireObjectVmt(owner, saut, "TUpdateSAUTClass") != ownerVmt)
            throw new Exception("Standalone SAUT owner lifetime guard");
        RequireDisjoint(pb, 24, canonicalPb, 24); RequireDisjoint(outp, 106, commonOut, 106);
        RequireDisjoint(pb, 24, outp, 106);
        if (ReadPointer(owner, 0x18) != pb || ReadPointer(pb, 0x10) != loc ||
            ReadPointer(pb, 0xc) != outp || ReadPointer(canonicalPb, 0x10) != loc ||
            ReadPointer(canonicalPb, 0xc) != commonOut)
            throw new Exception("Standalone SAUT isolated output authority guard");
        foreach (int offset in InputOffsets)
            if (ReadPointer(pb, offset) != ReadPointer(canonicalPb, offset))
                throw new Exception("Standalone SAUT canonical input alias guard");
        if (ReadPointer(canonicalPb, 0) != klubButton || ReadPointer(canonicalPb, 4) != avpButton ||
            ReadPointer(canonicalPb, 8) != inputParam || ReadPointer(canonicalPb, 0x14) != wagonParam ||
            ReadPointer(communicationRecord, 0) != commonButtons ||
            ReadPointer(communicationRecord, 0x10) != inputParam || ReadPointer(communicationRecord, 0xc) != wagonParam ||
            ReadPointer(klubButton, 0x58) != pbButtons || ReadPointer(commonButtons, 0xf) != pbButtons)
            throw new Exception("Standalone SAUT real safety input graph guard");
        RequireReadable(klubButton, 92); RequireReadable(avpButton, 19);
        RequireReadable(inputParam, 52); RequireReadable(pbButtons, 26);
        if (RequireObjectVmt(commOwner, communication, "TComm2ES6") != commVmt ||
            ReadPointer(commOwner, 4) != communicationRecord ||
            ReadPointer(communicationRecord, 8) != loc || ReadPointer(communicationRecord, 0x14) != commonOut)
            throw new Exception("Standalone SAUT communication graph guard");
        // Communication treats this object as the separate hardware SAUT channel.
        // A software adapter must not compete with that channel.
        if (ReadPointer(commOwner, 0x274) != IntPtr.Zero)
            throw new Exception("Standalone SAUT refuses a duplicate hardware channel");
        if (proceed == IntPtr.Zero || proceed != IntPtr.Add(saut, ProceedRva) ||
            GetProcAddress(saut, "Proceed_saut") != proceed)
            throw new Exception("Standalone SAUT Proceed_saut export guard");
        RequireModuleRange(proceed, 4, saut);
        if (Marshal.ReadByte(proceed, 0) != 0x55 || Marshal.ReadByte(proceed, 1) != 0x8b ||
            Marshal.ReadByte(proceed, 2) != 0xec || Marshal.ReadByte(proceed, 3) != 0x51)
            throw new Exception("Standalone SAUT Proceed_saut instruction guard");
        // Request bytes are validated only when capturing this producer's result.
        // Getters preserve that result even if another DLL updates the common bytes.
    }

    static void RequireDisjoint(IntPtr first, int firstCount, IntPtr second, int secondCount) {
        ulong a = unchecked((uint)first.ToInt32()), b = unchecked((uint)second.ToInt32());
        if (a < b + (uint)secondCount && b < a + (uint)firstCount)
            throw new Exception("Standalone SAUT private/common record overlap guard");
    }
    static bool ReadFlag(IntPtr record, int offset) {
        IntPtr address = IntPtr.Add(record, offset); RequireReadable(address, 1);
        byte value = Marshal.ReadByte(address);
        if (value > 1) throw new Exception("Standalone SAUT output Boolean encoding guard");
        return value == 1;
    }
    static IntPtr ReadPointer(IntPtr record, int offset) {
        IntPtr address = IntPtr.Add(record, offset); RequireReadable(address, 4);
        return Marshal.ReadIntPtr(address);
    }
    static void RequireReadable(IntPtr address, int count) {
        MemoryInfo info;
        if (address == IntPtr.Zero || count < 1 ||
            VirtualQuery(address, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero)
            throw new Exception("Standalone SAUT readable range guard");
        ulong start = unchecked((uint)address.ToInt32()), region = unchecked((uint)info.Base.ToInt32());
        uint protection = info.Protect & 0xff;
        if (info.State != 0x1000 || (info.Protect & 0x101) != 0 || start < region ||
            start + (uint)count > region + info.RegionSize.ToUInt32() ||
            !(protection == 0x02 || protection == 0x04 || protection == 0x08 ||
              protection == 0x20 || protection == 0x40 || protection == 0x80))
            throw new Exception("Standalone SAUT readable range guard");
    }
    static void RequireModuleRange(IntPtr address, int count, IntPtr module) {
        RequireReadable(address, count); MemoryInfo info;
        if (VirtualQuery(address, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero ||
            info.AllocationBase != module || info.Type != 0x1000000)
            throw new Exception("Standalone SAUT module address guard");
    }
    static IntPtr RequireObjectVmt(IntPtr obj, IntPtr module, string expectedClass) {
        IntPtr vmt = ReadPointer(obj, 0); RequireModuleRange(IntPtr.Add(vmt, -56), 60, module);
        IntPtr name = ReadPointer(vmt, -56); RequireModuleRange(name, 1, module);
        int count = Marshal.ReadByte(name);
        if (count < 1 || count > 150) throw new Exception("Standalone SAUT VMT class length guard");
        RequireModuleRange(name, count + 1, module); byte[] bytes = new byte[count];
        Marshal.Copy(IntPtr.Add(name, 1), bytes, 0, count);
        foreach (byte value in bytes) if (value < 32 || value > 126)
            throw new Exception("Standalone SAUT VMT class encoding guard");
        if (expectedClass != null && Encoding.ASCII.GetString(bytes) != expectedClass)
            throw new Exception("Standalone SAUT native class guard: " + expectedClass);
        return vmt;
    }
    static void CheckModuleLifetime(IntPtr module, string name, string expectedPath) {
        if (module == IntPtr.Zero || GetModuleHandle(name) != module)
            throw new Exception("Standalone SAUT module lifetime guard: " + name);
        StringBuilder path = new StringBuilder(32768);
        uint count = GetModuleFileName(module, path, path.Capacity);
        if (count == 0 || count >= path.Capacity ||
            !String.Equals(Path.GetFullPath(path.ToString()), expectedPath, StringComparison.OrdinalIgnoreCase))
            throw new Exception("Standalone SAUT loaded path guard: " + name);
    }
    static void CheckModule(IntPtr module, string name, string expectedPath, string expectedSha) {
        CheckModuleLifetime(module, name, expectedPath);
        string actual;
        using (SHA256 hash = SHA256.Create()) using (FileStream file = File.OpenRead(expectedPath))
            actual = BitConverter.ToString(hash.ComputeHash(file)).Replace("-", "");
        if (actual != expectedSha) throw new Exception("Standalone SAUT loaded module SHA guard: " + name);
    }
    void SafeLog(string text) { try { if (log != null) log(text); } catch { } }

    public void Dispose() {
        if (disposed) return;
        if (GetCurrentThreadId() != thread)
            throw new Exception("Standalone SAUT detach must run on its owner thread");
        // No native resource belongs to this binding, so detach remains safe after
        // the host has closed the native owner. No native memory is read or written.
        Action<string> writer = log;
        disposed = true; outputsCaptured = false; log = null;
        saut = communication = canonicalPb = commonOut = pb = loc = outp = commOwner = IntPtr.Zero;
        klubButton = avpButton = inputParam = wagonParam = commonButtons = pbButtons = IntPtr.Zero;
        ownerSlot = owner = ownerVmt = commVmt = communicationRecord = proceed = IntPtr.Zero;
        try { if (writer != null) writer("Standalone SAUT read-only authority detached"); } catch { }
    }
}
