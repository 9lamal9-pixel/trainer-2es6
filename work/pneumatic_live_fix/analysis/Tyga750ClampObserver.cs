using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// Diagnostic only. Four hooks copy existing values into owned memory and
// preserve the retained early750 clamp already present in this exact DLL.
// Attach/reset/native call/read/dispose must remain on the same native thread.
public sealed class Tyga750ClampObserver : IDisposable {
    public const string SourceSha256 = "8EC6A82E618A1D75CBB31242784084B530BB87308E601DEFC5263D5279270491";
    public const int BeforeRva = 0x177c29, AfterRva = 0x177c84;
    public const int EarlyBeforeRva = 0x17971d, EarlyAfterRva = 0x179726;
    const int GlobalRva = 0x19058c, CellRva = 0x1aa058, Header = 64, RecordSize = 512, Capacity = 16;
    const int Enabled = 4, BeforeCount = 8, AfterCount = 12, Errors = 16, Written = 20, Pending = 24;
    const int BufferSize = Header + RecordSize * Capacity;
    const int EarlyRecordSize = 128, EarlyCapacity = 128, EarlyBufferSize = Header + EarlyRecordSize * EarlyCapacity;
    static readonly byte[] BeforeOriginal = { 0xc7, 0x45, 0xf8, 1, 0, 0, 0 };
    static readonly byte[] AfterOriginal = { 0xff, 0x45, 0xf8, 0x83, 0x7d, 0xf8, 5 };
    static readonly byte[] EarlyBeforeOriginal = { 0xe9, 0x10, 0xc9, 0x6d, 0 };
    static readonly byte[] EarlyAfterOriginal = { 0x80, 0x7c, 0x10, 0x22, 0 };
    const string RetainedFixHex = "837df0007f0633c08be55dc3db45f0d87dece950e891ff837ddc000f8fbb4292ff8b45fce85f6192ffd9ee5b8be55dc204008b45fc8b40048b55f4817cd03300803b447608c744d03300803b44817cd03700803b447608c744d03700803b44e9c23692ff8b45fcba01000000e8932192ff508b45fcba02000000e8852192ff5a03c285c0750c8b45fce8fa6092ff8be55dc38b45fc80780b00e90e0492ff";
    static readonly object OwnerLock = new object();
    static readonly Dictionary<IntPtr, Tyga750ClampObserver> Owners = new Dictionary<IntPtr, Tyga750ClampObserver>();
    readonly IntPtr module, cell, outer, calc, param, result, outerVmt, calcVmt;
    readonly string path;
    readonly TrainerContactVoltagePatch contactVoltage;
    readonly uint thread;
    IntPtr pinned, data, beforeCave, afterCave, earlyData, earlyBeforeCave, earlyAfterCave;
    byte[] beforeCode, afterCode, beforeJump, afterJump, earlyBeforeCode, earlyAfterCode, earlyBeforeJump, earlyAfterJump;
    bool beforeMayExist, afterMayExist, earlyBeforeMayExist, earlyAfterMayExist, disposed, frame;
    long generation;

    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {
        public IntPtr Base, Allocation; public uint AllocationProtect; public UIntPtr Size;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module, StringBuilder name, int length);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool GetModuleHandleEx(uint flags, IntPtr address, out IntPtr module);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool FreeLibrary(IntPtr module);
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr address, out MemoryInfo info, UIntPtr size);
    [DllImport("kernel32.dll", SetLastError = true)] static extern IntPtr VirtualAlloc(IntPtr address, UIntPtr size, uint allocation, uint protection);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool VirtualFree(IntPtr address, UIntPtr size, uint kind);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool VirtualProtect(IntPtr address, UIntPtr size, uint protection, out uint previous);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool FlushInstructionCache(IntPtr process, IntPtr address, UIntPtr size);

    Tyga750ClampObserver(IntPtr module, string expectedPath, IntPtr tygaParam, TrainerContactVoltagePatch contactVoltage) {
        this.contactVoltage = contactVoltage;
        this.module = module; path = Path.GetFullPath(expectedPath); thread = GetCurrentThreadId(); param = tygaParam;
        cell = Pointer(At(GlobalRva), 0);
        if (cell != At(CellRva)) throw new InvalidOperationException("Tyga global cell guard");
        outer = Pointer(cell, 0); outerVmt = Pointer(outer, 0);
        calc = Pointer(outer, 0x420); calcVmt = Pointer(calc, 0); result = Pointer(param, 0x27);
    }

    public static Tyga750ClampObserver Attach(IntPtr nativeModule, string expectedPath, IntPtr tygaParam, TrainerContactVoltagePatch contactVoltage = null) {
        if (IntPtr.Size != 4) throw new InvalidOperationException("Tyga observer requires x86");
        if (nativeModule == IntPtr.Zero || tygaParam == IntPtr.Zero) throw new ArgumentException("Loaded Tyga and initialized input records are required");
        lock (OwnerLock) {
            if (Owners.ContainsKey(nativeModule)) throw new InvalidOperationException("Another observer owns the Tyga clamp sites");
            var observer = new Tyga750ClampObserver(nativeModule, expectedPath, tygaParam, contactVoltage);
            Owners.Add(nativeModule, observer);
            try { observer.Install(); return observer; }
            catch (Exception error) {
                try { observer.Dispose(); }
                catch (Exception restore) { throw new InvalidOperationException("Observer attach/restore failed; module and allocations remain retained", new AggregateException(error, restore)); }
                throw;
            }
        }
    }

    public static bool HasRetainedOwner(IntPtr nativeModule) {
        lock (OwnerLock) return Owners.ContainsKey(nativeModule);
    }

    void Install() {
        CheckThread(); CheckModule();
        IntPtr retained;
        if (!GetModuleHandleEx(4, module, out retained) || retained != module) throw WinError("Retain Tyga lifetime");
        pinned = retained;
        using (var file = File.OpenRead(path)) using (var hash = SHA256.Create())
            if (BitConverter.ToString(hash.ComputeHash(file)).Replace("-", "") != SourceSha256) throw new InvalidOperationException("Tyga source SHA guard");
        CheckGraph(); CheckProducer();
        Guard(At(BeforeRva), BeforeOriginal); Guard(At(AfterRva), AfterOriginal);
        Guard(At(EarlyBeforeRva), EarlyBeforeOriginal); Guard(At(EarlyAfterRva), EarlyAfterOriginal);
        RequireRx(At(BeforeRva)); RequireRx(At(AfterRva));
        RequireRx(At(EarlyBeforeRva)); RequireRx(At(EarlyAfterRva));
        data = Allocate(BufferSize); beforeCave = Allocate(4096); afterCave = Allocate(4096);
        earlyData = Allocate(EarlyBufferSize); earlyBeforeCave = Allocate(4096); earlyAfterCave = Allocate(4096);
        beforeCode = BuildCave(beforeCave, true); afterCode = BuildCave(afterCave, false);
        earlyBeforeCode = BuildEarlyCave(earlyBeforeCave, true); earlyAfterCode = BuildEarlyCave(earlyAfterCave, false);
        PublishCave(beforeCave, beforeCode); PublishCave(afterCave, afterCode);
        PublishCave(earlyBeforeCave, earlyBeforeCode); PublishCave(earlyAfterCave, earlyAfterCode);
        beforeJump = Jump(At(BeforeRva), beforeCave, BeforeOriginal.Length);
        afterJump = Jump(At(AfterRva), afterCave, AfterOriginal.Length);
        earlyBeforeJump = Jump(At(EarlyBeforeRva), earlyBeforeCave, EarlyBeforeOriginal.Length);
        earlyAfterJump = Jump(At(EarlyAfterRva), earlyAfterCave, EarlyAfterOriginal.Length);
        earlyBeforeMayExist = true; WriteCode(At(EarlyBeforeRva), earlyBeforeJump); Guard(At(EarlyBeforeRva), earlyBeforeJump);
        earlyAfterMayExist = true; WriteCode(At(EarlyAfterRva), earlyAfterJump); Guard(At(EarlyAfterRva), earlyAfterJump);
        beforeMayExist = true; WriteCode(At(BeforeRva), beforeJump); Guard(At(BeforeRva), beforeJump);
        afterMayExist = true; WriteCode(At(AfterRva), afterJump); Guard(At(AfterRva), afterJump);
    }

    public void ResetFrame() {
        CheckAttached();
        if (frame) throw new InvalidOperationException("Read the preceding diagnostic frame first");
        if (Marshal.ReadInt32(param, 8) != 2) throw new InvalidOperationException("This diagnostic profile requires two sections");
        if (generation == Int64.MaxValue) throw new InvalidOperationException("Observer generation exhausted");
        var zeros = new byte[BufferSize]; Marshal.Copy(zeros, 0, data, zeros.Length);
        var earlyZeros = new byte[EarlyBufferSize]; Marshal.Copy(earlyZeros, 0, earlyData, earlyZeros.Length);
        Marshal.WriteInt32(earlyData, Enabled, 1);
        generation++; frame = true; Marshal.WriteInt32(data, Enabled, 1);
    }

    public Frame ReadSnapshot() {
        CheckAttached();
        if (!frame) throw new InvalidOperationException("ResetFrame must precede reading");
        Marshal.WriteInt32(data, Enabled, 0); frame = false;
        Marshal.WriteInt32(earlyData, Enabled, 0);
        int before = Marshal.ReadInt32(data, BeforeCount), after = Marshal.ReadInt32(data, AfterCount);
        int count = Marshal.ReadInt32(data, Written), errors = Marshal.ReadInt32(data, Errors), pending = Marshal.ReadInt32(data, Pending);
        if (count < 0 || count > Capacity) throw new InvalidOperationException("Observer record count corrupted");
        var records = new Capture[count]; bool complete = count > 0 && before == count && after == count && pending == 0 && errors == 0;
        for (int i = 0; i < count; i++) {
            int at = Header + i * RecordSize; records[i] = new Capture(data, at);
            if (!records[i].Complete || records[i].Sequence != i + 1) complete = false;
        }
        int earlyBefore = Marshal.ReadInt32(earlyData, BeforeCount), earlyAfter = Marshal.ReadInt32(earlyData, AfterCount);
        int earlyCount = Marshal.ReadInt32(earlyData, Written), earlyErrors = Marshal.ReadInt32(earlyData, Errors), earlyPending = Marshal.ReadInt32(earlyData, Pending);
        if (earlyCount < 0 || earlyCount > EarlyCapacity) throw new InvalidOperationException("Early observer record count corrupted");
        var early = new EarlyCapture[earlyCount];
        bool earlyComplete = earlyBefore == earlyAfter && earlyBefore == earlyCount && earlyErrors == 0 && earlyPending == 0;
        for (int i = 0; i < early.Length; i++) { early[i] = new EarlyCapture(earlyData, Header + i * EarlyRecordSize); if (!early[i].Complete) earlyComplete = false; }
        // These are late, averaged channels, not the raw GetCur inputs.
        return new Frame(generation, before, after, errors, pending, complete, records, ReadArray(param, 0x5b), ReadArray(param, 0x9b), early, earlyErrors, earlyPending, earlyComplete, U(At(0x177a4c)), U(At(0x177bdb)));
    }

    public sealed class Capture {
        public readonly int Sequence, Sections, ControllerPosition, InternalPosition, Connection, Percent, SectionInputBits;
        public readonly bool Complete;
        public readonly double VelocityInputMetresPerSecond;
        public readonly float InternalVelocityMetresPerSecond, ForceBefore, ForceAfter, UsetBefore, UsetAfter;
        public readonly uint ForceBeforeBits, ForceAfterBits;
        public readonly uint NativeFramePointer;
        public readonly int EarlyEndSequence;
        public readonly uint[] IaBefore, IaAfter, IvBefore, IvAfter, MomentBefore, MomentAfter, PairForceBefore, PairForceAfter;
        public readonly uint[] AveragedIaBefore, AveragedIvBefore, EdsBefore, U2Before;
        public readonly int[] ApparatusInputs;
        public float[] IaBeforeA { get { return Decode(IaBefore); } }
        public float[] IaAfterA { get { return Decode(IaAfter); } }
        public float[] IvBeforeA { get { return Decode(IvBefore); } }
        public float[] IvAfterA { get { return Decode(IvAfter); } }
        public float[] NativeMomentBefore { get { return Decode(MomentBefore); } }
        public float[] NativeMomentAfter { get { return Decode(MomentAfter); } }
        public float[] NativePairForceBefore { get { return Decode(PairForceBefore); } }
        public float[] NativePairForceAfter { get { return Decode(PairForceAfter); } }
        public float[] NativeEdsBefore { get { return Decode(EdsBefore); } }
        public float[] NativeU2Before { get { return Decode(U2Before); } }
        internal Capture(IntPtr source, int at) {
            Sequence = Marshal.ReadInt32(source, at); Complete = Marshal.ReadInt32(source, at + 4) == 1;
            Sections = Marshal.ReadInt32(source, at + 8); VelocityInputMetresPerSecond = Double(source, at + 16);
            ControllerPosition = Marshal.ReadInt32(source, at + 24); InternalPosition = Marshal.ReadInt32(source, at + 28);
            Connection = Marshal.ReadInt32(source, at + 32); Percent = Marshal.ReadInt32(source, at + 36);
            ForceBeforeBits = Bits(source, at + 40); ForceAfterBits = Bits(source, at + 44);
            ForceBefore = Single(ForceBeforeBits); ForceAfter = Single(ForceAfterBits);
            UsetBefore = Single(Bits(source, at + 48)); UsetAfter = Single(Bits(source, at + 52));
            InternalVelocityMetresPerSecond = Single(Bits(source, at + 56)); SectionInputBits = Marshal.ReadInt32(source, at + 60);
            IaBefore = ReadArray(source, at + 64); IvBefore = ReadArray(source, at + 96); MomentBefore = ReadArray(source, at + 128); PairForceBefore = ReadArray(source, at + 160);
            IaAfter = ReadArray(source, at + 192); IvAfter = ReadArray(source, at + 224); MomentAfter = ReadArray(source, at + 256); PairForceAfter = ReadArray(source, at + 288);
            ApparatusInputs = new int[8]; for (int i = 0; i < 8; i++) ApparatusInputs[i] = Marshal.ReadInt32(source, at + 320 + 4 * i);
            AveragedIaBefore = ReadArray(source, at + 352); AveragedIvBefore = ReadArray(source, at + 384);
            EdsBefore = ReadArray(source, at + 416); U2Before = ReadArray(source, at + 448);
            NativeFramePointer = Bits(source, at + 480); EarlyEndSequence = Marshal.ReadInt32(source, at + 484);
        }
    }

    public sealed class EarlyCapture {
        public readonly int Sequence, Section, Percent, ControllerPosition, VozbType, Pair0Enabled, Pair1Enabled;
        public readonly bool Complete;
        public readonly uint ReturnAddress, CallerFramePointer, NativeFramePointer;
        public readonly double VelocityInputMetresPerSecond;
        public readonly float InternalVelocityMetresPerSecond;
        public readonly uint[] IaBefore, IaAfter, IvBefore, IvAfter;
        public float[] RawIaA { get { return Decode(IaBefore); } }
        public float[] BoundedIaA { get { return Decode(IaAfter); } }
        public float[] IvBeforeA { get { return Decode(IvBefore); } }
        public float[] IvAfterA { get { return Decode(IvAfter); } }
        internal EarlyCapture(IntPtr p, int at) {
            Sequence = Marshal.ReadInt32(p, at); Complete = Marshal.ReadInt32(p, at + 4) == 1; Section = Marshal.ReadInt32(p, at + 8);
            ReturnAddress = Bits(p, at + 12); CallerFramePointer = Bits(p, at + 16); NativeFramePointer = Bits(p, at + 20);
            VelocityInputMetresPerSecond = Double(p, at + 24); InternalVelocityMetresPerSecond = Single(Bits(p, at + 32));
            Percent = Marshal.ReadInt32(p, at + 36); IaBefore = new[] { Bits(p, at + 40), Bits(p, at + 44) }; IaAfter = new[] { Bits(p, at + 48), Bits(p, at + 52) };
            IvBefore = new[] { Bits(p, at + 56), Bits(p, at + 60) }; IvAfter = new[] { Bits(p, at + 64), Bits(p, at + 68) };
            ControllerPosition = Marshal.ReadInt32(p, at + 72); Pair0Enabled = Marshal.ReadInt32(p, at + 76); Pair1Enabled = Marshal.ReadInt32(p, at + 80); VozbType = Marshal.ReadInt32(p, at + 84);
        }
    }

    public sealed class Frame {
        public readonly long Generation;
        public readonly int BeforeCalls, AfterCalls, ErrorFlags, PendingIndex;
        public readonly bool Complete;
        public readonly Capture[] Records;
        public readonly EarlyCapture[] EarlyRecords;
        public readonly int EarlyErrorFlags, EarlyPendingIndex;
        public readonly bool EarlyComplete;
        public readonly uint CalkReturnAddress1, CalkReturnAddress2;
        public readonly uint[] AveragedIaAtReturn, AveragedIvAtReturn;
        internal Frame(long generation, int before, int after, int errors, int pending, bool complete, Capture[] records, uint[] ia, uint[] iv, EarlyCapture[] early, int earlyErrors, int earlyPending, bool earlyComplete, uint return1, uint return2) {
            Generation = generation; BeforeCalls = before; AfterCalls = after; ErrorFlags = errors; PendingIndex = pending; Complete = complete;
            Records = records; AveragedIaAtReturn = ia; AveragedIvAtReturn = iv;
            EarlyRecords = early; EarlyErrorFlags = earlyErrors; EarlyPendingIndex = earlyPending; EarlyComplete = earlyComplete; CalkReturnAddress1 = return1; CalkReturnAddress2 = return2;
        }
    }

    public sealed class Finding {
        public string Status;
        public int AffectedPairs;
        public int MatchedEarlySections;
        public bool EarlyCurrentMatchesLateInput, EarlyExcitationUnchanged;
        public bool ExcitationUnchanged, MomentUnchanged, PairForceUnchanged, TotalForceUnchanged;
        public double CounterfactualForceFromStoredCurrent, CapturedForce, CounterfactualDifference;
        public string Scope = "One observed native producer invocation; no train trajectory or calibration acceptance";
    }

    static Finding AnalyzeLateState(Capture capture) {
        var finding = new Finding { Status = "inconclusive", ExcitationUnchanged = true, MomentUnchanged = true, PairForceUnchanged = true, TotalForceUnchanged = capture.ForceBeforeBits == capture.ForceAfterBits };
        if (!capture.Complete || capture.Sections != 2) return finding;
        if (capture.Percent == 0 && (capture.InternalPosition == 23 || capture.InternalPosition == 44 || capture.InternalPosition == 65)) {
            finding.Status = "zero_percent_hod_special_path_requires_separate_call_coverage"; return finding;
        }
        double rawMoment = 0, storedMoment = 0; bool positiveAffectedMoment = false;
        for (int i = 0; i < 4; i++) {
            if (capture.ApparatusInputs[i] != 1) { finding.Status = "unsupported_active_pair_mask"; return finding; }
            float before = Single(capture.IaBefore[i]), after = Single(capture.IaAfter[i]), moment = Single(capture.MomentBefore[i]);
            if (!Finite(before) || !Finite(after) || !Finite(moment) || !Finite(Single(capture.MomentAfter[i])) ||
                !Finite(Single(capture.IvBefore[i])) || !Finite(Single(capture.IvAfter[i])) ||
                !Finite(Single(capture.PairForceBefore[i])) || !Finite(Single(capture.PairForceAfter[i])) || before < 0 || after < 0)
                { finding.Status = "invalid_observed_domain"; return finding; }
            rawMoment += moment;
            if (before > 0) storedMoment += moment * ((double)after / before);
            else if (moment != 0) { finding.Status = "invalid_zero_current_moment"; return finding; }
            bool affected = before > 750.01f && capture.IaAfter[i] == 0x443b8000u;
            if (affected) { finding.AffectedPairs++; if (moment > 0) positiveAffectedMoment = true; }
            finding.ExcitationUnchanged &= capture.IvBefore[i] == capture.IvAfter[i];
            finding.MomentUnchanged &= capture.MomentBefore[i] == capture.MomentAfter[i];
            finding.PairForceUnchanged &= capture.PairForceBefore[i] == capture.PairForceAfter[i];
        }
        finding.CapturedForce = capture.ForceBefore;
        if (rawMoment != 0) finding.CounterfactualForceFromStoredCurrent = capture.ForceBefore * storedMoment / rawMoment;
        finding.CounterfactualDifference = finding.CapturedForce - finding.CounterfactualForceFromStoredCurrent;
        if (finding.AffectedPairs == 0) { finding.Status = "clamp_not_reached_in_this_invocation"; return finding; }
        if (!positiveAffectedMoment || !Finite(capture.ForceBefore) || capture.ForceBefore == 0) { finding.Status = "clamp_reached_zero_or_invalid_force_inconclusive"; return finding; }
        finding.Status = finding.ExcitationUnchanged && finding.MomentUnchanged && finding.PairForceUnchanged && finding.TotalForceUnchanged
            ? "same_call_current_force_inconsistency_confirmed" : "clamp_reached_outputs_changed_requires_consistency_review";
        return finding;
    }

    public static Finding Analyze(Capture capture, Frame frame, int previousEarlyEnd) {
        Finding finding = AnalyzeLateState(capture);
        if (finding.Status.StartsWith("invalid_") || finding.Status.StartsWith("unsupported_")) return finding;
        if (!frame.EarlyComplete || !capture.Complete) { finding.Status = "early_pairing_incomplete"; return finding; }
        bool[] sections = new bool[2]; bool foundLimit = false, positiveMoment = false;
        finding.AffectedPairs = 0; finding.EarlyCurrentMatchesLateInput = true; finding.EarlyExcitationUnchanged = true;
        foreach (EarlyCapture early in frame.EarlyRecords) {
            if (early.Sequence <= previousEarlyEnd || early.Sequence > capture.EarlyEndSequence || early.CallerFramePointer != capture.NativeFramePointer ||
                early.ReturnAddress != frame.CalkReturnAddress1 && early.ReturnAddress != frame.CalkReturnAddress2 || early.Section < 1 || early.Section > 2) continue;
            int section = early.Section - 1;
            if (sections[section]) { finding.Status = "multiple_direct_GetCur_calls_require_event_review"; return finding; }
            sections[section] = true; finding.MatchedEarlySections++;
            for (int pair = 0; pair < 2; pair++) {
                int at = section * 2 + pair, enabled = pair == 0 ? early.Pair0Enabled : early.Pair1Enabled;
                if (enabled != 1) { finding.Status = "unsupported_active_pair_mask"; return finding; }
                float raw = Single(early.IaBefore[pair]), limited = Single(early.IaAfter[pair]);
                if (!Finite(raw) || !Finite(limited) || raw < 0 || limited < 0) { finding.Status = "invalid_early_current_domain"; return finding; }
                finding.EarlyCurrentMatchesLateInput &= early.IaAfter[pair] == capture.IaBefore[at];
                finding.EarlyExcitationUnchanged &= early.IvBefore[pair] == early.IvAfter[pair] && early.IvAfter[pair] == capture.IvBefore[at];
                if (raw > 750.01f) {
                    foundLimit = true; finding.AffectedPairs++;
                    if (early.IaAfter[pair] != 0x443b8000u) { finding.Status = "retained_early_limit_did_not_bound_positive_current"; return finding; }
                    if (Single(capture.MomentBefore[at]) > 0) positiveMoment = true;
                }
            }
        }
        if (!sections[0] || !sections[1]) { finding.Status = "no_unique_direct_Calk_GetCur_pairing"; return finding; }
        if (!foundLimit) { finding.Status = "raw_early_limit_not_reached_in_this_invocation"; return finding; }
        if (!positiveMoment || !Finite(capture.ForceBefore) || capture.ForceBefore <= 0) { finding.Status = "early_limit_reached_without_qualified_nonzero_force"; return finding; }
        bool lateNoOp = true;
        for (int i = 0; i < 4; i++) lateNoOp &= capture.IaBefore[i] == capture.IaAfter[i];
        finding.Status = finding.EarlyCurrentMatchesLateInput && finding.EarlyExcitationUnchanged && lateNoOp && finding.ExcitationUnchanged && finding.MomentUnchanged && finding.PairForceUnchanged && finding.TotalForceUnchanged
            ? "retained_early750_before_nonzero_force_and_late_noop_verified" : "retained_limit_event_captured_requires_numeric_review";
        // The old raw-current force counterfactual is inapplicable to this DLL:
        // GetCur's actual early clamp precedes its Moment/force calculation.
        finding.CounterfactualForceFromStoredCurrent = capture.ForceBefore;
        finding.CounterfactualDifference = 0;
        return finding;
    }

    void CheckThread() { if (GetCurrentThreadId() != thread) throw new InvalidOperationException("Tyga observer thread guard"); }
    void CheckModule() {
        if (GetModuleHandle("Tyga1.dll") != module) throw new InvalidOperationException("Tyga loaded module guard");
        var name = new StringBuilder(32768); uint size = GetModuleFileName(module, name, name.Capacity);
        if (size == 0 || size >= name.Capacity || !String.Equals(Path.GetFullPath(name.ToString()), path, StringComparison.OrdinalIgnoreCase)) throw new InvalidOperationException("Tyga loaded path guard");
    }
    void CheckGraph() {
        CheckThread();
        if (Pointer(At(GlobalRva), 0) != cell || cell != At(CellRva) || Pointer(cell, 0) != outer || Pointer(outer, 0) != outerVmt || Pointer(outer, 0x420) != calc || Pointer(calc, 0) != calcVmt || Pointer(calc, 4) != param || Pointer(param, 0x27) != result)
            throw new InvalidOperationException("Tyga native owner graph changed");
        Readable(param, 305); Readable(calc, 0x110); Readable(result, 26);
        if (Marshal.ReadInt32(param, 8) != 2) throw new InvalidOperationException("Tyga observer is scoped to two sections");
    }
    void CheckProducer() {
        // Both GetCur call paths are covered by the common pre-clamp site.
        Guard(At(0x177a47), Hex("e8f0070000"));
        Guard(At(0x177bd6), Hex("e861060000")); Guard(At(0x177bdb), Hex("ddd8"));
        Guard(At(0x177c52), Hex("c744d03300803b44")); Guard(At(0x177c7c), Hex("c744d03700803b44"));
        Guard(At(0x177c8b), Hex("75a3")); Guard(At(0x177c8d), Hex("8be55dc3"));
        Guard(At(0x1764a5), Hex("e9bafb6d009090"));
        Guard(At(0x179722), Hex("90909090"));
        Guard(At(0x856000), Hex(RetainedFixHex));
        Guard(At(0x178204), Hex("558bec83c4f48955f88945fc8b45fc8b40048b55f88b84d0050100008b55fc8b52048b4df80384ca090100008945f48b45f48be55dc3"));
        GuardCompatibleProducer(0x17c188, Hex("558bec83c4f48945fc8b45fc8b400480b8300100000074128b45fc8b40048b4027c7401100004845eb0e8b45fc8b40048b402733d28950118b45fc8b40048b402733d289108b45fc8b400483c02b8945f4c745f8010000008b45f88b55f4c64442fe008b45f88b55f4c64442ff008b45f88b55f433c9894cc2088b45f88b55f433c9894cc20c8b45f88b55f433c9898cc2880000008b45f88b55f433c9898cc28c000000ff45f8837df80575ab8b45f433d289500889500c8be55dc3"));
    }
    void GuardCompatibleProducer(int rva, byte[] original) {
        if (contactVoltage == null) Guard(At(rva), original);
        else contactVoltage.VerifyOriginalWindowWithOwnedHooks(module, rva, original);
    }

    void CheckAttached() {
        CheckThread();
        if (disposed || !beforeMayExist || !afterMayExist || !earlyBeforeMayExist || !earlyAfterMayExist || data == IntPtr.Zero) throw new ObjectDisposedException("Tyga750ClampObserver");
        CheckModule(); CheckGraph(); CheckProducer();
        Guard(At(BeforeRva), beforeJump); Guard(At(AfterRva), afterJump); Guard(beforeCave, beforeCode); Guard(afterCave, afterCode);
        Guard(At(EarlyBeforeRva), earlyBeforeJump); Guard(At(EarlyAfterRva), earlyAfterJump); Guard(earlyBeforeCave, earlyBeforeCode); Guard(earlyAfterCave, earlyAfterCode);
        RequireRx(At(BeforeRva)); RequireRx(At(AfterRva)); RequireRx(beforeCave); RequireRx(afterCave);
        RequireRx(At(EarlyBeforeRva)); RequireRx(At(EarlyAfterRva)); RequireRx(earlyBeforeCave); RequireRx(earlyAfterCave);
    }

    byte[] BuildCave(IntPtr address, bool before) {
        var b = new Code(); b.B(before ? BeforeOriginal : AfterOriginal); b.B(0x9c, 0x60);
        if (!before) { b.B(0x83, 0x7d, 0xf8, 5); b.Jcc(0x85, "done"); }
        b.B(0x83, 0x3d); b.A(IntPtr.Add(data, Enabled)); b.B(1); b.Jcc(0x85, "done");
        b.B(0x64, 0xa1); b.D(0x24); b.B(0x3d); b.D(thread); b.Jcc(0x85, "badThread");
        b.B(0x8b, 0x45, 0xfc, 0x3d); b.A(calc); b.Jcc(0x85, "badOwner");
        b.B(0x8b, 0x70, 4, 0x81, 0xfe); b.A(param); b.Jcc(0x85, "badOwner");
        b.B(0x8b, 0x56, 0x27, 0x81, 0xfa); b.A(result); b.Jcc(0x85, "badOwner");
        b.B(0x83, 0x7e, 8, 2); b.Jcc(0x85, "badSections");
        if (before) {
            b.Increment(IntPtr.Add(data, BeforeCount));
            b.B(0x83, 0x3d); b.A(IntPtr.Add(data, Pending)); b.B(0); b.Jcc(0x85, "nested");
            b.B(0x8b, 0x15); b.A(IntPtr.Add(data, Written)); b.B(0x83, 0xfa, Capacity); b.Jcc(0x83, "overflow");
            b.Increment(IntPtr.Add(data, Written)); b.B(0x42, 0x89, 0x15); b.A(IntPtr.Add(data, Pending)); b.B(0x4a);
            RecordPointer(b);
            b.B(0xa1); b.A(IntPtr.Add(data, BeforeCount)); b.B(0x89, 0x07);
            Copy(b, param, 8, 8); CopyByte(b, param, 0x16, 12);
            Copy(b, param, 0, 16); Copy(b, param, 4, 20); Copy(b, param, 0x0c, 24);
            CopyByte(b, calc, 0x0b, 28); CopyByte(b, calc, 0x40, 32); Copy(b, calc, 0x44, 36);
            Copy(b, result, 0, 40); Copy(b, result, 0x11, 48); Copy(b, calc, 0x104, 56); Copy(b, param, 0x109, 60);
            CopyArray(b, param, 0x3b, 64); CopyArray(b, param, 0x7b, 96); CopyArray(b, param, 0xe9, 128); CopyArray(b, param, 0xbb, 160);
            CopyArray(b, param, 0x10d, 320); CopyArray(b, param, 0x5b, 352); CopyArray(b, param, 0x9b, 384);
            CopyArray(b, calc, 0x9d, 416); CopyArray(b, calc, 0xbd, 448);
            b.B(0x8b, 0xc5, 0x89, 0x87); b.D(480); Copy(b, earlyData, Written, 484);
        } else {
            b.Increment(IntPtr.Add(data, AfterCount));
            b.B(0x8b, 0x15); b.A(IntPtr.Add(data, Pending)); b.B(0x83, 0xfa, 1); b.Jcc(0x82, "unmatched");
            b.B(0x83, 0xfa, Capacity); b.Jcc(0x87, "unmatched"); b.B(0x4a); RecordPointer(b);
            Copy(b, result, 0, 44); Copy(b, result, 0x11, 52);
            CopyArray(b, param, 0x3b, 192); CopyArray(b, param, 0x7b, 224); CopyArray(b, param, 0xe9, 256); CopyArray(b, param, 0xbb, 288);
            b.B(0xc7, 0x47, 4); b.D(1); b.B(0xc7, 0x05); b.A(IntPtr.Add(data, Pending)); b.D(0);
        }
        b.Jmp("done");
        Fault(b, "badThread", 1); Fault(b, "badOwner", 2); Fault(b, "nested", 4); Fault(b, "overflow", 8); Fault(b, "unmatched", 16); Fault(b, "badSections", 32);
        b.Label("done"); b.B(0x61, 0x9d, 0xe9); b.D(unchecked(U(At((before ? BeforeRva : AfterRva) + 7)) - U(address) - (uint)b.Count - 4));
        return b.Finish();
    }
    void RecordPointer(Code b) { b.B(0x69, 0xd2); b.D(RecordSize); b.B(0xbf); b.A(IntPtr.Add(data, Header)); b.B(0x03, 0xfa); }
    static void Copy(Code b, IntPtr source, int sourceOffset, int targetOffset) { b.B(0xa1); b.A(IntPtr.Add(source, sourceOffset)); b.B(0x89, 0x87); b.D(targetOffset); }
    static void CopyByte(Code b, IntPtr source, int sourceOffset, int targetOffset) { b.B(0x0f, 0xb6, 0x05); b.A(IntPtr.Add(source, sourceOffset)); b.B(0x89, 0x87); b.D(targetOffset); }
    static void CopyArray(Code b, IntPtr source, int sourceOffset, int targetOffset) { for (int i = 0; i < 8; i++) Copy(b, source, sourceOffset + 4 * i, targetOffset + 4 * i); }
    void Fault(Code b, string label, int mask) { b.Label(label); b.B(0x81, 0x0d); b.A(IntPtr.Add(data, Errors)); b.D(mask); b.Jmp("done"); }

    byte[] BuildEarlyCave(IntPtr address, bool before) {
        var b = new Code();
        // The existing prelimit JMP is chained after observing; it is not copied
        // verbatim into a new address. The postlimit CMP is replayed before
        // PUSHFD so the following original JE sees exactly its original flags.
        if (!before) b.B(EarlyAfterOriginal);
        b.B(0x9c, 0x60, 0x83, 0x3d); b.A(IntPtr.Add(earlyData, Enabled)); b.B(1); b.Jcc(0x85, "done");
        b.B(0x64, 0xa1); b.D(0x24); b.B(0x3d); b.D(thread); b.Jcc(0x85, "badThread");
        b.B(0x8b, 0x45, 0xfc, 0x3d); b.A(calc); b.Jcc(0x85, "badOwner");
        b.B(0x8b, 0x70, 4, 0x81, 0xfe); b.A(param); b.Jcc(0x85, "badOwner");
        b.B(0x8b, 0x4d, 0xf4, 0x83, 0xf9, 1); b.Jcc(0x82, "badSection"); b.B(0x83, 0xf9, 4); b.Jcc(0x87, "badSection");
        if (before) {
            b.Increment(IntPtr.Add(earlyData, BeforeCount));
            b.B(0x83, 0x3d); b.A(IntPtr.Add(earlyData, Pending)); b.B(0); b.Jcc(0x85, "nested");
            b.B(0x8b, 0x15); b.A(IntPtr.Add(earlyData, Written)); b.B(0x81, 0xfa); b.D(EarlyCapacity); b.Jcc(0x83, "overflow");
            b.Increment(IntPtr.Add(earlyData, Written)); b.B(0x42, 0x89, 0x15); b.A(IntPtr.Add(earlyData, Pending)); b.B(0x4a);
            EarlyRecordPointer(b);
            b.B(0xa1); b.A(IntPtr.Add(earlyData, BeforeCount)); b.B(0x89, 0x07, 0x89, 0x4f, 8);
            b.B(0x8b, 0x45, 4, 0x89, 0x47, 12, 0x8b, 0x45, 0, 0x89, 0x47, 16, 0x8b, 0xc5, 0x89, 0x47, 20);
            Copy(b, param, 0, 24); Copy(b, param, 4, 28); Copy(b, calc, 0x104, 32); Copy(b, calc, 0x44, 36);
            EarlyPair(b, 0x33, 40); EarlyPair(b, 0x37, 44); EarlyPair(b, 0x73, 56); EarlyPair(b, 0x77, 60);
            Copy(b, param, 0x0c, 72); EarlyPair(b, 0x105, 76); EarlyPair(b, 0x109, 80); CopyByte(b, param, 0x16, 84);
        } else {
            b.Increment(IntPtr.Add(earlyData, AfterCount));
            b.B(0x8b, 0x15); b.A(IntPtr.Add(earlyData, Pending)); b.B(0x83, 0xfa, 1); b.Jcc(0x82, "unmatched");
            b.B(0x81, 0xfa); b.D(EarlyCapacity); b.Jcc(0x87, "unmatched"); b.B(0x4a); EarlyRecordPointer(b);
            b.B(0x39, 0x4f, 8); b.Jcc(0x85, "unmatched"); b.B(0x8b, 0xc5, 0x39, 0x47, 20); b.Jcc(0x85, "unmatched");
            EarlyPair(b, 0x33, 48); EarlyPair(b, 0x37, 52); EarlyPair(b, 0x73, 64); EarlyPair(b, 0x77, 68);
            b.B(0xc7, 0x47, 4); b.D(1); b.B(0xc7, 0x05); b.A(IntPtr.Add(earlyData, Pending)); b.D(0);
        }
        b.Jmp("done");
        foreach (var pair in new[] { new KeyValuePair<string, int>("badThread",1), new KeyValuePair<string,int>("badOwner",2), new KeyValuePair<string,int>("nested",4), new KeyValuePair<string,int>("overflow",8), new KeyValuePair<string,int>("unmatched",16), new KeyValuePair<string,int>("badSection",32) }) {
            b.Label(pair.Key); b.B(0x81, 0x0d); b.A(IntPtr.Add(earlyData, Errors)); b.D(pair.Value); b.Jmp("done");
        }
        b.Label("done"); b.B(0x61, 0x9d, 0xe9);
        int target = before ? 0x856032 : EarlyAfterRva + 5;
        b.D(unchecked(U(At(target)) - U(address) - (uint)b.Count - 4));
        return b.Finish();
    }
    void EarlyRecordPointer(Code b) { b.B(0x69, 0xd2); b.D(EarlyRecordSize); b.B(0xbf); b.A(IntPtr.Add(earlyData, Header)); b.B(0x03, 0xfa); }
    static void EarlyPair(Code b, int sourceOffset, int targetOffset) { b.B(0x8b, 0x84, 0xce); b.D(sourceOffset); b.B(0x89, 0x87); b.D(targetOffset); }

    public void Dispose() {
        lock (OwnerLock) {
            if (disposed) return; CheckThread();
            if (data != IntPtr.Zero) Marshal.WriteInt32(data, Enabled, 0);
            if (earlyData != IntPtr.Zero) Marshal.WriteInt32(earlyData, Enabled, 0);
            if (beforeMayExist || afterMayExist || earlyBeforeMayExist || earlyAfterMayExist) {
                CheckModule();
                Restore(At(BeforeRva), BeforeOriginal, beforeJump, beforeMayExist);
                Restore(At(AfterRva), AfterOriginal, afterJump, afterMayExist);
                Restore(At(EarlyBeforeRva), EarlyBeforeOriginal, earlyBeforeJump, earlyBeforeMayExist);
                Restore(At(EarlyAfterRva), EarlyAfterOriginal, earlyAfterJump, earlyAfterMayExist);
                Guard(At(BeforeRva), BeforeOriginal); Guard(At(AfterRva), AfterOriginal);
                Guard(At(EarlyBeforeRva), EarlyBeforeOriginal); Guard(At(EarlyAfterRva), EarlyAfterOriginal);
                RequireRx(At(BeforeRva)); RequireRx(At(AfterRva));
                RequireRx(At(EarlyBeforeRva)); RequireRx(At(EarlyAfterRva));
                beforeMayExist = afterMayExist = earlyBeforeMayExist = earlyAfterMayExist = false;
            }
            Free(ref beforeCave); Free(ref afterCave); Free(ref earlyBeforeCave); Free(ref earlyAfterCave); Free(ref data); Free(ref earlyData);
            if (pinned != IntPtr.Zero) { if (!FreeLibrary(pinned)) throw WinError("Release retained Tyga"); pinned = IntPtr.Zero; }
            disposed = true; Owners.Remove(module);
        }
    }
    static void Restore(IntPtr at, byte[] original, byte[] jump, bool attempted) {
        if (!attempted) return;
        byte[] actual = Read(at, original.Length);
        if (Equal(actual, jump)) WriteCode(at, original);
        else if (!Equal(actual, original)) throw new InvalidOperationException("Foreign Tyga entry replacement: allocations retained");
        Guard(at, original);
    }
    IntPtr At(int rva) { return IntPtr.Add(module, rva); }
    static IntPtr Allocate(int size) { IntPtr p = VirtualAlloc(IntPtr.Zero, (UIntPtr)size, 0x3000, 4); if (p == IntPtr.Zero) throw WinError("Allocate observer memory"); return p; }
    static void Free(ref IntPtr p) { if (p == IntPtr.Zero) return; if (!VirtualFree(p, UIntPtr.Zero, 0x8000)) throw WinError("Free observer memory"); p = IntPtr.Zero; }
    static void PublishCave(IntPtr address, byte[] code) { if (code.Length > 4096) throw new InvalidOperationException("Observer cave exceeds page"); Marshal.Copy(code, 0, address, code.Length); uint old; if (!VirtualProtect(address, (UIntPtr)4096, 0x20, out old)) throw WinError("Protect observer RX"); RequireRx(address); Flush(address, code.Length); }
    static void WriteCode(IntPtr address, byte[] code) { uint old; if (!VirtualProtect(address, (UIntPtr)code.Length, 0x40, out old)) throw WinError("Protect Tyga entry"); try { Marshal.Copy(code, 0, address, code.Length); Guard(address, code); } finally { uint ignored; if (!VirtualProtect(address, (UIntPtr)code.Length, old, out ignored)) throw WinError("Restore Tyga entry protection"); } Flush(address, code.Length); }
    static void Flush(IntPtr address, int count) { if (!FlushInstructionCache(new IntPtr(-1), address, (UIntPtr)count)) throw WinError("Flush observer code"); }
    static void RequireRx(IntPtr address) { MemoryInfo info; if (VirtualQuery(address, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero || info.State != 0x1000 || (info.Protect & 255) != 0x20 || (info.Protect & 0x101) != 0) throw new InvalidOperationException("Observer RX guard"); }
    static void Readable(IntPtr address, int count) { MemoryInfo info; if (address == IntPtr.Zero || VirtualQuery(address, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero) throw new InvalidOperationException("Observer memory absent"); ulong start = U(address), first = U(info.Base); uint protect = info.Protect & 255; if (count < 1 || info.State != 0x1000 || (info.Protect & 0x101) != 0 || start < first || start + (uint)count > first + info.Size.ToUInt32() || protect != 2 && protect != 4 && protect != 8 && protect != 0x20 && protect != 0x40 && protect != 0x80) throw new InvalidOperationException("Observer memory range guard"); }
    static IntPtr Pointer(IntPtr source, int offset) { IntPtr p = IntPtr.Add(source, offset); Readable(p, 4); return Marshal.ReadIntPtr(p); }
    static byte[] Read(IntPtr address, int count) { Readable(address, count); var bytes = new byte[count]; Marshal.Copy(address, bytes, 0, count); return bytes; }
    static void Guard(IntPtr address, byte[] expected) { if (!Equal(Read(address, expected.Length), expected)) throw new InvalidOperationException("Tyga observer byte guard at " + address.ToString("x")); }
    static bool Equal(byte[] a, byte[] b) { if (a == null || b == null || a.Length != b.Length) return false; for (int i = 0; i < a.Length; i++) if (a[i] != b[i]) return false; return true; }
    static uint U(IntPtr p) { return unchecked((uint)p.ToInt32()); }
    static byte[] Jump(IntPtr source, IntPtr target, int count) { var bytes = new byte[count]; bytes[0] = 0xe9; Array.Copy(BitConverter.GetBytes(unchecked(U(target) - U(source) - 5)), 0, bytes, 1, 4); for (int i = 5; i < count; i++) bytes[i] = 0x90; return bytes; }
    static byte[] Hex(string text) { var bytes = new byte[text.Length / 2]; for (int i = 0; i < bytes.Length; i++) bytes[i] = Convert.ToByte(text.Substring(i * 2, 2), 16); return bytes; }
    static uint Bits(IntPtr source, int offset) { return unchecked((uint)Marshal.ReadInt32(source, offset)); }
    static uint[] ReadArray(IntPtr source, int offset) { var values = new uint[8]; for (int i = 0; i < 8; i++) values[i] = Bits(source, offset + 4 * i); return values; }
    public static float Single(uint value) { return BitConverter.ToSingle(BitConverter.GetBytes(value), 0); }
    public static float[] Decode(uint[] values) { var result = new float[values.Length]; for (int i = 0; i < result.Length; i++) result[i] = Single(values[i]); return result; }
    static double Double(IntPtr source, int offset) { return BitConverter.ToDouble(Read(IntPtr.Add(source, offset), 8), 0); }
    static bool Finite(float value) { return !float.IsNaN(value) && !float.IsInfinity(value); }
    static Exception WinError(string action) { return new Win32Exception(Marshal.GetLastWin32Error(), action); }
    sealed class Code {
        readonly List<byte> bytes = new List<byte>(); readonly Dictionary<string, int> labels = new Dictionary<string, int>();
        readonly List<KeyValuePair<int, string>> fixups = new List<KeyValuePair<int, string>>();
        public int Count { get { return bytes.Count; } }
        public void B(params byte[] value) { bytes.AddRange(value); }
        public void D(int value) { bytes.AddRange(BitConverter.GetBytes(value)); }
        public void D(uint value) { bytes.AddRange(BitConverter.GetBytes(value)); }
        public void A(IntPtr address) { D(U(address)); }
        public void Increment(IntPtr address) { B(0xff, 0x05); A(address); }
        public void Label(string name) { labels.Add(name, Count); }
        public void Jcc(byte condition, string name) { B(0x0f, condition); fixups.Add(new KeyValuePair<int, string>(Count, name)); D(0); }
        public void Jmp(string name) { B(0xe9); fixups.Add(new KeyValuePair<int, string>(Count, name)); D(0); }
        public byte[] Finish() { foreach (var fixup in fixups) { int target; if (!labels.TryGetValue(fixup.Value, out target)) throw new InvalidOperationException("Missing observer label " + fixup.Value); byte[] delta = BitConverter.GetBytes(target - fixup.Key - 4); for (int i = 0; i < 4; i++) bytes[fixup.Key + i] = delta[i]; } return bytes.ToArray(); }
    }
}
