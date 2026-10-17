using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

/// <summary>
/// Diagnostic observer of the guarded POEZD force producer. Attach, ResetFrame,
/// the synchronous Motion calls, ReadSnapshot and Dispose must use one thread.
/// It is not a brake actuator or a calibration acceptance decision.
/// </summary>
public sealed class TrainerNativeBrakeObserver : IDisposable
{
    public const string SourceSha256 = "822B6D17CE45F80179A512EE6C70A35D5AE29BF187530BDA5EBD11446CFB3553";
    public const int VehicleCount = 76;
    public const int NativeArrayCapacity = 150;
    public const int StoreRva = 0x1ec1;
    public const int BrakeForceRva = 0x1d0ac;
    public const int PredictorVelocityRva = 0x18f04;
    public const int BrakeInputRva = 0x1a674;
    const int ElementCountRva = 0x25658, GroupCountRva = 0x2565c, GroupSizeRva = 0x25664;
    const int MassRva = 0x19abc, GroupMassRva = 0x19f6c, KindRva = 0x1ab24, MaterialRva = 0x1ad7c;
    const int HeaderSize = 16, RecordSize = 64, BufferSize = HeaderSize + VehicleCount * RecordSize;
    const uint PageReadWrite = 4, PageExecuteRead = 0x20, PageExecuteReadWrite = 0x40;
    const uint MemCommitReserve = 0x3000, MemRelease = 0x8000;
    const int BadCounts = 1, BadIndex = 2, BadGroup = 4, BadVelocity = 8;
    const int BadFactor = 16, BadPressure = 32, BadForce = 64, CountOverflow = 128, ChangedModel = 256;
    static readonly object OwnerLock = new object();
    // A reachable jump must never outlive its buffers, even after a failed restore.
    static readonly Dictionary<IntPtr, TrainerNativeBrakeObserver> Owners = new Dictionary<IntPtr, TrainerNativeBrakeObserver>();
    readonly IntPtr module;
    readonly string modulePath;
    readonly uint threadId;
    readonly byte[] originalStore;
    IntPtr pinnedModule, data, cave;
    byte[] caveBytes, patchBytes;
    bool patchMayExist, disposed;
    long frameNumber;
    bool frameStarted;
    readonly int[] material = new int[VehicleCount], kind = new int[VehicleCount];
    readonly uint[] mass = new uint[VehicleCount], groupMass = new uint[VehicleCount];

    TrainerNativeBrakeObserver(IntPtr nativeModule, string expectedPath)
    {
        module = nativeModule;
        modulePath = Path.GetFullPath(expectedPath);
        threadId = GetCurrentThreadId();
        originalStore = AddressInstruction(new byte[] { 0xd9, 0x1c, 0x95 }, At(BrakeForceRva));
    }

    public static TrainerNativeBrakeObserver Attach(IntPtr nativeModule, string expectedModulePath)
    {
        if (IntPtr.Size != 4) throw new InvalidOperationException("Brake observer requires an x86 process.");
        if (nativeModule == IntPtr.Zero || String.IsNullOrWhiteSpace(expectedModulePath))
            throw new ArgumentException("A loaded POEZD module and its exact source path are required.");
        lock (OwnerLock)
        {
            if (Owners.ContainsKey(nativeModule)) throw new InvalidOperationException("A brake observer already owns this POEZD store site.");
            var observer = new TrainerNativeBrakeObserver(nativeModule, expectedModulePath);
            Owners.Add(nativeModule, observer);
            try { observer.Install(); return observer; }
            catch (Exception attachError)
            {
                try { observer.Dispose(); }
                catch (Exception restoreError)
                {
                    throw new InvalidOperationException("Brake observer attach failed and guarded restoration also failed. Native buffers remain retained; do not unload or reuse this module.", new AggregateException(attachError, restoreError));
                }
                throw;
            }
        }
    }

    public static TrainerNativeBrakeObserver Attach(string expectedModulePath)
    {
        return Attach(GetModuleHandle("POEZD.DLL"), expectedModulePath);
    }

    void Install()
    {
        CheckThread();
        if (GetModuleHandle("POEZD.DLL") != module) throw new InvalidOperationException("POEZD loaded-module identity guard failed.");
        IntPtr retained;
        if (!GetModuleHandleEx(4, module, out retained) || retained != module)
            throw WinError("Cannot retain the POEZD module lifetime");
        pinnedModule = retained;
        CheckModule();
        using (var sha = SHA256.Create())
        using (var file = File.OpenRead(modulePath))
        {
            string actual = BitConverter.ToString(sha.ComputeHash(file)).Replace("-", "");
            if (!String.Equals(actual, SourceSha256, StringComparison.Ordinal))
                throw new InvalidOperationException("Brake observer POEZD source SHA guard failed.");
        }
        CheckImage();
        CheckProducer();
        CheckCounts();
        GuardBytes(At(StoreRva), originalStore, "Original FSTP store");
        RequireRx(At(StoreRva));
        data = VirtualAlloc(IntPtr.Zero, new UIntPtr((uint)BufferSize), MemCommitReserve, PageReadWrite);
        if (data == IntPtr.Zero) throw WinError("Cannot allocate brake observer storage");
        cave = VirtualAlloc(IntPtr.Zero, new UIntPtr(4096), MemCommitReserve, PageReadWrite);
        if (cave == IntPtr.Zero) throw WinError("Cannot allocate brake observer code");
        caveBytes = BuildCave();
        if (caveBytes.Length > 4096) throw new InvalidOperationException("Brake observer code exceeds its bounded allocation.");
        Marshal.Copy(caveBytes, 0, cave, caveBytes.Length);
        uint oldProtect;
        if (!VirtualProtect(cave, new UIntPtr(4096), PageExecuteRead, out oldProtect))
            throw WinError("Cannot protect brake observer code RX");
        RequireRx(cave);
        Flush(cave, caveBytes.Length);
        patchBytes = JumpBytes(At(StoreRva), cave, originalStore.Length);
        // No call, delegate or managed callback is installed in the native producer.
        patchMayExist = true;
        WriteCode(At(StoreRva), patchBytes);
        GuardBytes(At(StoreRva), patchBytes, "Installed brake observer jump");
    }

    /// <summary>Start one bounded diagnostic frame before synchronous native Motion calls.</summary>
    public void ResetFrame()
    {
        CheckAttached();
        CheckCounts();
        Marshal.WriteInt32(data, 12, 0);
        var zeros = new byte[BufferSize];
        Marshal.Copy(zeros, 0, data, zeros.Length);
        for (int i = 0; i < VehicleCount; i++)
        {
            material[i] = Marshal.ReadInt32(At(MaterialRva), i * 4);
            kind[i] = Marshal.ReadInt32(At(KindRva), i * 4);
            mass[i] = ReadBits(At(MassRva), i * 4);
            groupMass[i] = ReadBits(At(GroupMassRva), i * 4);
            if (!PositiveFinite(mass[i]) || !PositiveFinite(groupMass[i]))
                throw new InvalidOperationException("Brake observer native mass guard failed at vehicle " + i + ".");
            if (material[i] < 0 || material[i] > 1 || kind[i] < 0 || kind[i] > 2)
                throw new InvalidOperationException("Brake observer native material/geometry index guard failed at vehicle " + i + ".");
        }
        if (frameNumber == Int64.MaxValue) throw new InvalidOperationException("Brake observer frame counter exhausted.");
        frameNumber++;
        frameStarted = true;
        Marshal.WriteInt32(data, 12, 1);
    }

    /// <summary>
    /// Stop capture and return per-vehicle extrema over every captured inner force pass.
    /// BrakeForce is the signed native value (10 kN units); NativeMass is in 10 t units.
    /// PredictorVelocity is m/s at FSTP, not the post-corrector GetCurVal velocity.
    /// Minima of different vehicles need not belong to the same inner pass.
    /// </summary>
    public Snapshot ReadSnapshot()
    {
        CheckAttached();
        CheckCounts();
        if (!frameStarted) throw new InvalidOperationException("ResetFrame must precede ReadSnapshot.");
        Marshal.WriteInt32(data, 12, 0);
        int errors = Marshal.ReadInt32(data, 8);
        int attempts = Marshal.ReadInt32(data, 0), valid = Marshal.ReadInt32(data, 4);
        var vehicles = new VehicleSample[VehicleCount];
        bool complete = attempts > 0 && valid == attempts, negative = true;
        int uniformCount = -1;
        uint minimumFactor = 0, maximumVelocity = 0;
        double nativeMass = 0;
        long summedAttempts = 0, summedValid = 0;
        for (int i = 0; i < VehicleCount; i++)
        {
            int offset = HeaderSize + i * RecordSize;
            int count = Marshal.ReadInt32(data, offset), good = Marshal.ReadInt32(data, offset + 4);
            int rowErrors = Marshal.ReadInt32(data, offset + 56);
            if (Marshal.ReadInt32(At(MaterialRva), i * 4) != material[i] || Marshal.ReadInt32(At(KindRva), i * 4) != kind[i]
                || ReadBits(At(MassRva), i * 4) != mass[i] || ReadBits(At(GroupMassRva), i * 4) != groupMass[i])
            { errors |= ChangedModel; rowErrors |= ChangedModel; }
            uint minFactor = ReadBits(data, offset + 24), maxVelocity = ReadBits(data, offset + 16);
            uint minForceMagnitude = ReadBits(data, offset + 48), maxForceMagnitude = ReadBits(data, offset + 52);
            var sample = new VehicleSample(i, Marshal.ReadInt32(data, offset + 8), Marshal.ReadInt32(data, offset + 60),
                count, good, rowErrors, material[i], kind[i], Float(mass[i]), Float(groupMass[i]),
                ReadBits(data, offset + 12), maxVelocity, ReadBits(data, offset + 20), minFactor, ReadBits(data, offset + 28),
                ReadBits(data, offset + 32), ReadBits(data, offset + 36), ReadBits(data, offset + 40),
                ReadBits(data, offset + 44), minForceMagnitude, maxForceMagnitude);
            vehicles[i] = sample;
            summedAttempts += count; summedValid += good;
            nativeMass += sample.NativeMass;
            if (uniformCount < 0) uniformCount = good;
            if (good <= 0 || good != count || good != uniformCount || rowErrors != 0
                || sample.LastGroupIndex != i || sample.LastElementIndex != i) complete = false;
            if (good <= 0 || minForceMagnitude == 0) negative = false;
            if (good > 0 && (minimumFactor == 0 || minFactor < minimumFactor)) minimumFactor = minFactor;
            if (maxVelocity > maximumVelocity) maximumVelocity = maxVelocity;
        }
        if (errors != 0 || summedAttempts != attempts || summedValid != valid || uniformCount <= 0) complete = false;
        return new Snapshot(frameNumber, attempts, valid, errors, complete, complete && negative,
            complete ? uniformCount : 0, Float(minimumFactor), Float(maximumVelocity), nativeMass, vehicles);
    }

    public sealed class VehicleSample
    {
        public readonly int Index, LastGroupIndex, LastElementIndex, CaptureCount, ValidCaptureCount, ErrorFlags, MaterialIndex, GeometryIndex;
        public readonly float NativeMass, NativeGroupMass, LastPredictorVelocity, MaximumAbsPredictorVelocity;
        public readonly float LastSpeedFactor, MinimumSpeedFactor, MaximumSpeedFactor;
        public readonly float LastBrakeInput, MinimumBrakeInput, MaximumBrakeInput;
        public readonly float LastBrakeForce, MinimumBrakeForce, MaximumBrakeForce;
        public readonly uint LastPredictorVelocityBits, LastSpeedFactorBits, LastBrakeInputBits, LastBrakeForceBits;
        internal VehicleSample(int index, int group, int element, int count, int good, int errors, int ftk, int geometry,
            float nativeMass, float nativeGroupMass, uint velocity, uint maxVelocity, uint factor, uint minFactor, uint maxFactor,
            uint pressure, uint minPressure, uint maxPressure, uint force, uint minForceMagnitude, uint maxForceMagnitude)
        {
            Index = index; LastGroupIndex = group; LastElementIndex = element; CaptureCount = count; ValidCaptureCount = good;
            ErrorFlags = errors; MaterialIndex = ftk; GeometryIndex = geometry; NativeMass = nativeMass; NativeGroupMass = nativeGroupMass;
            LastPredictorVelocityBits = velocity; LastSpeedFactorBits = factor; LastBrakeInputBits = pressure; LastBrakeForceBits = force;
            LastPredictorVelocity = Float(velocity); MaximumAbsPredictorVelocity = Float(maxVelocity);
            LastSpeedFactor = Float(factor); MinimumSpeedFactor = Float(minFactor); MaximumSpeedFactor = Float(maxFactor);
            LastBrakeInput = Float(pressure); MinimumBrakeInput = Float(minPressure); MaximumBrakeInput = Float(maxPressure);
            LastBrakeForce = Float(force);
            MinimumBrakeForce = -Float(maxForceMagnitude); MaximumBrakeForce = -Float(minForceMagnitude);
        }
    }

    public sealed class Snapshot
    {
        public readonly long FrameNumber;
        public readonly int CaptureCount, ValidCaptureCount, ErrorFlags, NativeForcePassCount;
        public readonly bool Complete, AllStrictlyNegative;
        public readonly float MinimumSpeedFactor, MaximumAbsPredictorVelocity;
        public readonly double NativeTotalMass;
        readonly VehicleSample[] samples;
        public VehicleSample[] Vehicles { get { return (VehicleSample[])samples.Clone(); } }
        internal Snapshot(long frame, int count, int valid, int errors, bool complete, bool negative, int passes,
            float factor, float velocity, double nativeMass, VehicleSample[] vehicles)
        {
            FrameNumber = frame; CaptureCount = count; ValidCaptureCount = valid; ErrorFlags = errors;
            Complete = complete; AllStrictlyNegative = negative; NativeForcePassCount = passes;
            MinimumSpeedFactor = factor; MaximumAbsPredictorVelocity = velocity; NativeTotalMass = nativeMass; samples = vehicles;
        }
    }

    public void Dispose()
    {
        lock (OwnerLock)
        {
            if (disposed) return;
            CheckThread();
            if (patchMayExist)
            {
                CheckModule();
                byte[] current = ReadBytes(At(StoreRva), originalStore.Length);
                if (Equal(current, patchBytes)) WriteCode(At(StoreRva), originalStore);
                else if (!Equal(current, originalStore))
                    throw new InvalidOperationException("Brake observer restore rejected: the store site belongs to another patch. Code/storage remain retained.");
                GuardBytes(At(StoreRva), originalStore, "Restored brake force store");
                RequireRx(At(StoreRva));
                patchMayExist = false;
            }
            FreeAllocation(ref cave);
            FreeAllocation(ref data);
            if (pinnedModule != IntPtr.Zero)
            {
                if (!FreeLibrary(pinnedModule)) throw WinError("Cannot release retained POEZD lifetime");
                pinnedModule = IntPtr.Zero;
            }
            disposed = true;
            Owners.Remove(module);
        }
    }

    void CheckAttached()
    {
        CheckThread();
        if (disposed || !patchMayExist || data == IntPtr.Zero || cave == IntPtr.Zero)
            throw new ObjectDisposedException("TrainerNativeBrakeObserver");
        CheckModule();
        CheckProducer();
        GuardBytes(At(StoreRva), patchBytes, "Brake observer jump ownership");
        GuardBytes(cave, caveBytes, "Brake observer code ownership");
        RequireRx(At(StoreRva)); RequireRx(cave);
    }
    void CheckThread()
    {
        if (GetCurrentThreadId() != threadId) throw new InvalidOperationException("Brake observer must remain on its synchronous native Motion owner thread.");
    }
    void CheckModule()
    {
        if (GetModuleHandle("POEZD.DLL") != module) throw new InvalidOperationException("Brake observer module lifetime/identity guard failed.");
        var name = new StringBuilder(32768);
        uint length = GetModuleFileName(module, name, name.Capacity);
        if (length == 0 || length >= name.Capacity) throw WinError("Cannot resolve the loaded POEZD path");
        if (!String.Equals(Path.GetFullPath(name.ToString()), modulePath, StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("Brake observer loaded POEZD path differs from the pinned source path.");
    }
    void CheckImage()
    {
        if (Marshal.ReadInt16(module) != 0x5a4d) throw new InvalidOperationException("Brake observer image MZ guard failed.");
        int pe = Marshal.ReadInt32(module, 0x3c);
        if (pe < 0x40 || pe > 0x1000 || Marshal.ReadInt32(module, pe) != 0x4550
            || Marshal.ReadInt16(module, pe + 4) != 0x14c || Marshal.ReadInt16(module, pe + 24) != 0x10b
            || Marshal.ReadInt32(module, pe + 24 + 56) != 0x33000)
            throw new InvalidOperationException("Brake observer pinned i386 image layout guard failed.");
    }
    void CheckProducer()
    {
        GuardBytes(At(0x1dbc), new byte[] { 0x55, 0x8b, 0xec, 0x83, 0xc4, 0xe8 }, "Brake force producer frame");
        GuardBytes(At(0x1dd0), AddressInstruction(new byte[] { 0xf7, 0x3d }, At(GroupSizeRva)), "Brake group divisor");
        GuardBytes(At(0x1ded), AddressInstruction(new byte[] { 0xd8, 0x0c, 0x95 }, At(BrakeInputRva)), "Brake pressure input");
        GuardBytes(At(0x1e21), AddressInstruction(new byte[] { 0xd9, 0x04, 0x85 }, At(PredictorVelocityRva)), "Predictor velocity first load");
        GuardBytes(At(0x1e4d), AddressInstruction(new byte[] { 0xd9, 0x04, 0x95 }, At(PredictorVelocityRva)), "Predictor velocity second load");
        GuardBytes(At(0x1e76), new byte[] { 0xd9, 0x5d, 0xf4 }, "Actual speed factor stack slot");
        var tail = new List<byte>(new byte[] { 0xff, 0x45, 0xf0, 0x8b, 0x4d, 0xf0, 0x3b, 0x0d });
        tail.AddRange(BitConverter.GetBytes(At(ElementCountRva).ToInt32()));
        GuardBytes(At(0x1ec8), tail.ToArray(), "Brake element loop boundary");
        // The independently owned per-pad hook at 1EBB is deliberately not touched.
    }
    void CheckCounts()
    {
        if (Marshal.ReadInt32(At(ElementCountRva)) != VehicleCount || Marshal.ReadInt32(At(GroupCountRva)) != VehicleCount
            || Marshal.ReadInt32(At(GroupSizeRva)) != 1)
            throw new InvalidOperationException("Brake observer supports only the proved 76 elements / 76 groups / one element per group ABI.");
    }

    byte[] BuildCave()
    {
        var b = new CodeBuilder();
        b.Emit(originalStore); // Execute the original FSTP exactly once, before observing.
        b.Emit(0x9c, 0x60); // PUSHFD, PUSHAD. No x87/SSE instruction follows the original store.
        b.CompareAbsolute(IntPtr.Add(data, 12), 1); b.Jcc(0x85, "done");
        b.IncrementAbsolute(data); b.Jcc(0x88, "overflowGlobal");
        b.CompareAbsolute(At(ElementCountRva), VehicleCount); b.Jcc(0x85, "badCounts");
        b.CompareAbsolute(At(GroupCountRva), VehicleCount); b.Jcc(0x85, "badCounts");
        b.CompareAbsolute(At(GroupSizeRva), 1); b.Jcc(0x85, "badCounts");
        b.Emit(0x8b, 0x45, 0xf0, 0x83, 0xf8, VehicleCount); b.Jcc(0x83, "badIndex");
        b.Emit(0x39, 0xc2); b.Jcc(0x85, "badIndex"); // Original store EDX must equal the element index.
        b.Emit(0x8b, 0x4d, 0xfc, 0x39, 0xc1); b.Jcc(0x85, "badGroup");
        b.Emit(0x6b, 0xd0, RecordSize, 0xbb); b.Address(IntPtr.Add(data, HeaderSize)); b.Emit(0x01, 0xd3);
        b.Emit(0xff, 0x03); b.Jcc(0x88, "overflowRow");
        b.Emit(0x89, 0x4b, 8, 0x89, 0x43, 60);
        b.Emit(0x8b, 0x14, 0x8d); b.Address(At(PredictorVelocityRva)); b.Emit(0x89, 0x53, 12);
        b.Emit(0x8b, 0x55, 0xf4, 0x89, 0x53, 20);
        b.Emit(0x8b, 0x14, 0x85); b.Address(At(BrakeInputRva)); b.Emit(0x89, 0x53, 32);
        b.Emit(0x8b, 0x14, 0x85); b.Address(At(BrakeForceRva)); b.Emit(0x89, 0x53, 44);
        CheckFiniteCode(b, 12, "badVelocity");
        CheckFiniteCode(b, 20, "badFactor");
        b.Emit(0x85, 0xd2); b.Jcc(0x8e, "badFactor"); // Strictly positive finite factor.
        CheckFiniteCode(b, 32, "badPressure");
        b.Emit(0x85, 0xd2); b.Jcc(0x89, "pressureOk");
        b.Emit(0x81, 0xe2); b.Int32(0x7fffffff); b.Jcc(0x85, "badPressure");
        b.Label("pressureOk");
        CheckFiniteCode(b, 44, "badForce");
        b.Emit(0x85, 0xd2); b.Jcc(0x88, "forceOk");
        b.Emit(0x81, 0xe2); b.Int32(0x7fffffff); b.Jcc(0x85, "badForce");
        b.Label("forceOk");
        b.Emit(0x83, 0x7b, 4, 0); b.Jcc(0x85, "update");
        CopyMagnitude(b, 12, 16);
        CopyField(b, 20, 24); CopyField(b, 20, 28);
        CopyMagnitude(b, 32, 36); CopyMagnitude(b, 32, 40);
        CopyMagnitude(b, 44, 48); CopyMagnitude(b, 44, 52);
        b.Jmp("commit");
        b.Label("update");
        UpdateExtrema(b, 12, -1, 16, true, "velocity");
        UpdateExtrema(b, 20, 24, 28, false, "factor");
        UpdateExtrema(b, 32, 36, 40, true, "pressure");
        UpdateExtrema(b, 44, 48, 52, true, "force");
        b.Label("commit");
        b.Emit(0xff, 0x43, 4); b.Jcc(0x88, "overflowRow");
        b.IncrementAbsolute(IntPtr.Add(data, 4)); b.Jcc(0x88, "overflowGlobal");
        b.Jmp("done");
        RowError(b, "badVelocity", BadVelocity); RowError(b, "badFactor", BadFactor);
        RowError(b, "badPressure", BadPressure); RowError(b, "badForce", BadForce);
        RowError(b, "overflowRow", CountOverflow);
        GlobalError(b, "badCounts", BadCounts); GlobalError(b, "badIndex", BadIndex);
        GlobalError(b, "badGroup", BadGroup); GlobalError(b, "overflowGlobal", CountOverflow);
        b.Label("done"); b.Emit(0x61, 0x9d); // POPAD, POPFD: integer flags/registers restored.
        b.ExternalJump(cave, At(StoreRva + originalStore.Length));
        return b.Finish();
    }
    static void CheckFiniteCode(CodeBuilder b, byte offset, string failure)
    {
        b.Emit(0x8b, 0x53, offset, 0x89, 0xd0, 0x25); b.Int32(0x7f800000);
        b.Emit(0x3d); b.Int32(0x7f800000); b.Jcc(0x84, failure);
    }
    static void CopyField(CodeBuilder b, byte source, byte target)
    { b.Emit(0x8b, 0x43, source, 0x89, 0x43, target); }
    static void CopyMagnitude(CodeBuilder b, byte source, byte target)
    { b.Emit(0x8b, 0x43, source, 0x25); b.Int32(0x7fffffff); b.Emit(0x89, 0x43, target); }
    static void UpdateExtrema(CodeBuilder b, byte source, int minimum, byte maximum, bool magnitude, string name)
    {
        b.Emit(0x8b, 0x43, source);
        if (magnitude) { b.Emit(0x25); b.Int32(0x7fffffff); }
        if (minimum >= 0)
        {
            b.Emit(0x3b, 0x43, (byte)minimum); b.Jcc(0x83, name + "MinDone");
            b.Emit(0x89, 0x43, (byte)minimum); b.Label(name + "MinDone");
        }
        b.Emit(0x3b, 0x43, maximum); b.Jcc(0x86, name + "MaxDone");
        b.Emit(0x89, 0x43, maximum); b.Label(name + "MaxDone");
    }
    void RowError(CodeBuilder b, string label, int flag)
    { b.Label(label); b.Emit(0x81, 0x4b, 56); b.Int32(flag); b.OrAbsolute(IntPtr.Add(data, 8), flag); b.Jmp("done"); }
    void GlobalError(CodeBuilder b, string label, int flag)
    { b.Label(label); b.OrAbsolute(IntPtr.Add(data, 8), flag); b.Jmp("done"); }

    sealed class CodeBuilder
    {
        readonly List<byte> bytes = new List<byte>();
        readonly Dictionary<string, int> labels = new Dictionary<string, int>();
        readonly List<KeyValuePair<int, string>> fixups = new List<KeyValuePair<int, string>>();
        public void Emit(params byte[] value) { bytes.AddRange(value); }
        public void Int32(int value) { bytes.AddRange(BitConverter.GetBytes(value)); }
        public void Address(IntPtr value) { Int32(value.ToInt32()); }
        public void Label(string name) { labels.Add(name, bytes.Count); }
        public void Jcc(byte condition, string label) { Emit(0x0f, condition); Fixup(label); }
        public void Jmp(string label) { Emit(0xe9); Fixup(label); }
        void Fixup(string label) { fixups.Add(new KeyValuePair<int, string>(bytes.Count, label)); Int32(0); }
        public void CompareAbsolute(IntPtr address, int value) { Emit(0x81, 0x3d); Address(address); Int32(value); }
        public void IncrementAbsolute(IntPtr address) { Emit(0xff, 0x05); Address(address); }
        public void OrAbsolute(IntPtr address, int value) { Emit(0x81, 0x0d); Address(address); Int32(value); }
        public void ExternalJump(IntPtr origin, IntPtr target)
        { Emit(0xe9); Int32(unchecked(target.ToInt32() - (origin.ToInt32() + bytes.Count + 4))); }
        public byte[] Finish()
        {
            var result = bytes.ToArray();
            foreach (var fix in fixups)
            {
                int target;
                if (!labels.TryGetValue(fix.Value, out target)) throw new InvalidOperationException("Missing observer code label " + fix.Value);
                byte[] displacement = BitConverter.GetBytes(target - fix.Key - 4);
                Buffer.BlockCopy(displacement, 0, result, fix.Key, 4);
            }
            return result;
        }
    }
    IntPtr At(int rva) { return IntPtr.Add(module, rva); }
    static uint ReadBits(IntPtr address, int offset) { return unchecked((uint)Marshal.ReadInt32(address, offset)); }
    static bool PositiveFinite(uint bits) { return bits > 0 && bits < 0x7f800000; }
    static float Float(uint bits)
    {
        // Never materialize a NaN/infinity while the native FPU has trapping enabled.
        if ((bits & 0x7f800000) == 0x7f800000) return 0;
        return BitConverter.ToSingle(BitConverter.GetBytes(bits), 0);
    }
    static byte[] AddressInstruction(byte[] prefix, IntPtr address)
    {
        var bytes = new List<byte>(prefix); bytes.AddRange(BitConverter.GetBytes(address.ToInt32())); return bytes.ToArray();
    }
    static byte[] JumpBytes(IntPtr site, IntPtr target, int length)
    {
        var result = new byte[length]; result[0] = 0xe9;
        byte[] displacement = BitConverter.GetBytes(unchecked(target.ToInt32() - site.ToInt32() - 5));
        Buffer.BlockCopy(displacement, 0, result, 1, 4);
        for (int i = 5; i < length; i++) result[i] = 0x90;
        return result;
    }
    static byte[] ReadBytes(IntPtr address, int count) { var value = new byte[count]; Marshal.Copy(address, value, 0, count); return value; }
    static bool Equal(byte[] first, byte[] second)
    {
        if (first == null || second == null || first.Length != second.Length) return false;
        for (int i = 0; i < first.Length; i++) if (first[i] != second[i]) return false;
        return true;
    }
    static void GuardBytes(IntPtr address, byte[] expected, string reason)
    { if (expected == null || !Equal(ReadBytes(address, expected.Length), expected)) throw new InvalidOperationException(reason + " byte guard failed."); }
    static void RequireRx(IntPtr address)
    {
        MemoryBasicInformation info;
        if (VirtualQuery(address, out info, new UIntPtr((uint)Marshal.SizeOf(typeof(MemoryBasicInformation)))) == UIntPtr.Zero)
            throw WinError("Cannot inspect native code protection");
        if (info.State != 0x1000 || info.Protect != PageExecuteRead)
            throw new InvalidOperationException("Brake observer code page must have committed RX protection.");
    }
    static void WriteCode(IntPtr address, byte[] bytes)
    {
        RequireRx(address);
        uint previous;
        if (!VirtualProtect(address, new UIntPtr((uint)bytes.Length), PageExecuteReadWrite, out previous))
            throw WinError("Cannot open guarded brake observer code page");
        try
        {
            if (previous != PageExecuteRead) throw new InvalidOperationException("Brake observer code protection changed concurrently.");
            Marshal.Copy(bytes, 0, address, bytes.Length);
        }
        finally
        {
            uint ignored;
            if (!VirtualProtect(address, new UIntPtr((uint)bytes.Length), previous, out ignored))
                throw WinError("Cannot restore brake observer code page protection");
        }
        RequireRx(address); Flush(address, bytes.Length);
    }
    static void Flush(IntPtr address, int count)
    { if (!FlushInstructionCache(GetCurrentProcess(), address, new UIntPtr((uint)count))) throw WinError("Cannot flush brake observer code"); }
    static void FreeAllocation(ref IntPtr address)
    {
        if (address == IntPtr.Zero) return;
        if (!VirtualFree(address, UIntPtr.Zero, MemRelease)) throw WinError("Cannot release brake observer allocation");
        address = IntPtr.Zero;
    }
    static Exception WinError(string operation) { return new Win32Exception(Marshal.GetLastWin32Error(), operation); }
    [StructLayout(LayoutKind.Sequential)]
    struct MemoryBasicInformation
    {
        public IntPtr BaseAddress, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern IntPtr GetModuleHandle(string moduleName);
    [DllImport("kernel32.dll", EntryPoint = "GetModuleHandleExW", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)] static extern bool GetModuleHandleEx(uint flags, IntPtr address, out IntPtr handle);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] static extern uint GetModuleFileName(IntPtr handle, StringBuilder filename, int size);
    [DllImport("kernel32.dll", SetLastError = true)] [return: MarshalAs(UnmanagedType.Bool)] static extern bool FreeLibrary(IntPtr module);
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll")] static extern IntPtr GetCurrentProcess();
    [DllImport("kernel32.dll", SetLastError = true)] static extern IntPtr VirtualAlloc(IntPtr address, UIntPtr size, uint allocationType, uint protect);
    [DllImport("kernel32.dll", SetLastError = true)] [return: MarshalAs(UnmanagedType.Bool)] static extern bool VirtualProtect(IntPtr address, UIntPtr size, uint protect, out uint oldProtect);
    [DllImport("kernel32.dll", SetLastError = true)] [return: MarshalAs(UnmanagedType.Bool)] static extern bool VirtualFree(IntPtr address, UIntPtr size, uint freeType);
    [DllImport("kernel32.dll", SetLastError = true)] static extern UIntPtr VirtualQuery(IntPtr address, out MemoryBasicInformation info, UIntPtr size);
    [DllImport("kernel32.dll", SetLastError = true)] [return: MarshalAs(UnmanagedType.Bool)] static extern bool FlushInstructionCache(IntPtr process, IntPtr address, UIntPtr size);
}
