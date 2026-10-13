using System;
using System.Runtime.InteropServices;

// Private input and signal state for one native safety consumer. The caller owns
// native initialization/closure, module SHA guards and the output record. It must
// call Dispose only after that consumer has closed successfully (or before Init).
// This helper never invokes a DLL and never writes a canonical/source record.
public sealed class TrainerSafetyInputContext : IDisposable {
    const int SharedSize = 24, InputSize = 52, LightSize = 59, OutputSize = 106;
    static readonly int[] SharedOffsets = { 0, 4, 8, 12, 16, 20 };

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
    readonly Func<bool> lifetimeGuard;
    readonly IntPtr originalSharedRecord, sourceInput, sourceLight, output;
    readonly IntPtr[] originalAliases = new IntPtr[6];
    readonly byte[] inputHead = new byte[31], inputTail = new byte[17], lightSource = new byte[35];
    IntPtr sharedRecord, input, light;
    bool initialized, disposed;

    public IntPtr SharedRecord { get { ThreadGuard(); return sharedRecord; } }
    public IntPtr Input { get { ThreadGuard(); return input; } }
    public IntPtr Light { get { ThreadGuard(); return light; } }
    public IntPtr OriginalSharedRecord { get { ThreadGuard(); return originalSharedRecord; } }
    public IntPtr SourceInput { get { ThreadGuard(); return sourceInput; } }
    public IntPtr SourceLight { get { ThreadGuard(); return sourceLight; } }
    public IntPtr Output { get { ThreadGuard(); return output; } }

    public static TrainerSafetyInputContext Create(IntPtr canonicalPB, IntPtr output,
        Func<bool> lifetimeGuard) {
        return new TrainerSafetyInputContext(canonicalPB, output, lifetimeGuard);
    }

    TrainerSafetyInputContext(IntPtr canonicalPB, IntPtr producerOutput, Func<bool> guard) {
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Safety input context requires x86");
        if (canonicalPB == IntPtr.Zero || producerOutput == IntPtr.Zero)
            throw new ArgumentException("Safety input source/output is absent");
        if (guard == null) throw new ArgumentNullException("lifetimeGuard");
        thread = GetCurrentThreadId(); lifetimeGuard = guard;
        originalSharedRecord = canonicalPB; output = producerOutput;
        GuardLifetime();
        RequireReadable(originalSharedRecord, SharedSize);
        for (int n = 0; n < SharedOffsets.Length; n++) {
            originalAliases[n] = Marshal.ReadIntPtr(originalSharedRecord, SharedOffsets[n]);
            if (originalAliases[n] == IntPtr.Zero)
                throw new Exception("Safety source shared pointer absent at " + SharedOffsets[n]);
        }
        sourceInput = originalAliases[2];
        RequireReadable(sourceInput, InputSize);
        sourceLight = Marshal.ReadIntPtr(sourceInput, 0x1f);
        RequireReadable(sourceLight, LightSize);
        GuardSourceGraph();

        // Nothing is published or passed to native code until Create returns.
        // Allocate all three buffers before clearing or writing any of them.
        try {
            sharedRecord = Allocate(SharedSize);
            input = Allocate(InputSize);
            light = Allocate(LightSize);
            GuardOwnedRanges(); GuardDisjointRanges();
            Clear(sharedRecord, SharedSize); Clear(input, InputSize); Clear(light, LightSize);
            for (int n = 0; n < SharedOffsets.Length; n++) {
                IntPtr value = SharedOffsets[n] == 8 ? input : SharedOffsets[n] == 12 ? output : originalAliases[n];
                Marshal.WriteIntPtr(sharedRecord, SharedOffsets[n], value);
            }
            Marshal.WriteIntPtr(input, 0x1f, light);
            initialized = true;
            Sync();
        } catch {
            // The caller cannot have initialized its native consumer yet.
            FreeOwnedMemory(); disposed = true;
            throw;
        }
    }

    // The supplied lifetime predicate must be read-only and run on the owner
    // thread. It checks the caller's actual native module/owner identity.
    void GuardLifetime() {
        bool alive;
        try { alive = lifetimeGuard(); }
        catch (Exception error) { throw new Exception("Safety source lifetime guard failed", error); }
        if (!alive) throw new Exception("Safety source native lifetime guard refused");
    }
    void ThreadGuard() {
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Safety input context requires x86");
        if (GetCurrentThreadId() != thread) throw new Exception("Safety input context owner-thread guard");
        if (disposed) throw new ObjectDisposedException("TrainerSafetyInputContext");
    }
    void GuardSourceGraph() {
        RequireReadable(originalSharedRecord, SharedSize);
        for (int n = 0; n < SharedOffsets.Length; n++) {
            if (Marshal.ReadIntPtr(originalSharedRecord, SharedOffsets[n]) != originalAliases[n])
                throw new Exception("Safety canonical shared pointer changed at " + SharedOffsets[n]);
            RequireReadable(originalAliases[n], 1);
        }
        RequireReadable(sourceInput, InputSize); RequireReadable(sourceLight, LightSize);
        RequireReadable(originalAliases[3], OutputSize); RequireReadable(output, OutputSize);
        RequireWritable(output, OutputSize);
        if (Marshal.ReadIntPtr(sourceInput, 0x1f) != sourceLight)
            throw new Exception("Safety canonical signal pointer changed");
    }
    void GuardOwnedRanges() {
        RequireWritable(sharedRecord, SharedSize);
        RequireWritable(input, InputSize);
        RequireWritable(light, LightSize);
    }
    void GuardDisjointRanges() {
        RequireDisjoint(sharedRecord, SharedSize, input, InputSize);
        RequireDisjoint(sharedRecord, SharedSize, light, LightSize);
        RequireDisjoint(input, InputSize, light, LightSize);
        IntPtr[] owned = { sharedRecord, input, light };
        int[] sizes = { SharedSize, InputSize, LightSize };
        for (int n = 0; n < owned.Length; n++) {
            RequireDisjoint(owned[n], sizes[n], originalSharedRecord, SharedSize);
            RequireDisjoint(owned[n], sizes[n], sourceInput, InputSize);
            RequireDisjoint(owned[n], sizes[n], sourceLight, LightSize);
            RequireDisjoint(owned[n], sizes[n], originalAliases[3], OutputSize);
            RequireDisjoint(owned[n], sizes[n], output, OutputSize);
            // Other native pointers stay borrowed and are never freed or copied.
            for (int a = 0; a < originalAliases.Length; a++)
                RequireDisjoint(owned[n], sizes[n], originalAliases[a], 1);
        }
    }

    // Read-only verification of both source ownership and the private pointer graph.
    public void SourceGuard() {
        ThreadGuard(); GuardLifetime(); GuardSourceGraph();
        if (!initialized) throw new Exception("Safety private context is not initialized");
        GuardOwnedRanges(); GuardDisjointRanges();
        for (int n = 0; n < SharedOffsets.Length; n++) {
            IntPtr expected = SharedOffsets[n] == 8 ? input : SharedOffsets[n] == 12 ? output : originalAliases[n];
            if (Marshal.ReadIntPtr(sharedRecord, SharedOffsets[n]) != expected)
                throw new Exception("Safety private shared alias changed at " + SharedOffsets[n]);
        }
        if (Marshal.ReadIntPtr(input, 0x1f) != light)
            throw new Exception("Safety private signal pointer changed");
    }

    public void Sync() {
        SourceGuard();
        // Read every declared source range before the first private write.
        Marshal.Copy(sourceInput, inputHead, 0, inputHead.Length);
        Marshal.Copy(IntPtr.Add(sourceInput, 35), inputTail, 0, inputTail.Length);
        Marshal.Copy(sourceLight, lightSource, 0, lightSource.Length);
        // Input+1F is a pointer hole: it always refers to this consumer's signal.
        Marshal.Copy(inputHead, 0, input, inputHead.Length);
        Marshal.Copy(inputTail, 0, IntPtr.Add(input, 35), inputTail.Length);
        Marshal.WriteIntPtr(input, 0x1f, light);
        // Index/name/Xlight/XlastLight/SendCode/BlocUch are source inputs.
        // Dist/Uklon/DeltaS/Vpr/Vcel (23..3A) remain the native owner's state.
        Marshal.Copy(lightSource, 0, light, lightSource.Length);
        SourceGuard();
    }

    static IntPtr Allocate(int size) {
        IntPtr pointer = Marshal.AllocHGlobal(size);
        if (pointer == IntPtr.Zero) throw new OutOfMemoryException("Safety private record allocation failed");
        return pointer;
    }
    static void Clear(IntPtr pointer, int size) { Marshal.Copy(new byte[size], 0, pointer, size); }
    static void RequireDisjoint(IntPtr first, int firstSize, IntPtr second, int secondSize) {
        ulong a = unchecked((uint)first.ToInt32()), b = unchecked((uint)second.ToInt32());
        if (a < b + (uint)secondSize && b < a + (uint)firstSize)
            throw new Exception("Safety private/source record overlap guard");
    }
    static bool Accessible(IntPtr address, int count, bool write) {
        MemoryInfo info;
        if (address == IntPtr.Zero || count < 1 ||
            VirtualQuery(address, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero) return false;
        ulong start = unchecked((uint)address.ToInt32()), region = unchecked((uint)info.Base.ToInt32());
        uint protection = info.Protect & 0xff;
        if (info.State != 0x1000 || (info.Protect & 0x101) != 0 || start < region ||
            start + (uint)count > region + info.RegionSize.ToUInt32()) return false;
        if (write) return protection == 4 || protection == 8 || protection == 0x40 || protection == 0x80;
        return protection == 2 || protection == 4 || protection == 8 || protection == 0x20 || protection == 0x40 || protection == 0x80;
    }
    static void RequireReadable(IntPtr address, int count) {
        if (!Accessible(address, count, false)) throw new Exception("Safety readable record-range guard");
    }
    static void RequireWritable(IntPtr address, int count) {
        if (!Accessible(address, count, true)) throw new Exception("Safety writable record-range guard");
    }

    void FreeOwnedMemory() {
        // Only the three buffers allocated above belong to this helper.
        // Source records, borrowed native pointers and Output are never freed.
        if (sharedRecord != IntPtr.Zero) { Marshal.FreeHGlobal(sharedRecord); sharedRecord = IntPtr.Zero; }
        if (input != IntPtr.Zero) { Marshal.FreeHGlobal(input); input = IntPtr.Zero; }
        if (light != IntPtr.Zero) { Marshal.FreeHGlobal(light); light = IntPtr.Zero; }
    }
    public void Dispose() {
        if (disposed) return;
        ThreadGuard();
        // The native owner may already be gone, so do not dereference its graph or
        // invoke its lifetime predicate here. Caller proves successful native close.
        FreeOwnedMemory(); disposed = true; initialized = false;
    }
}
