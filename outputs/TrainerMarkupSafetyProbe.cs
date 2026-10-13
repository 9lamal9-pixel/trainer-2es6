using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Original markup -> actual production native consumers. The boundary cases are
// explicitly declared independent initial conditions, not in-motion position
// overrides. Only native stateTrain and its measured initial readback establish
// physical sensor inputs. No force, speed output, protection, Dist/Vcel/Vpr,
// permission, pressure, fixture callback or native code bytes are assigned.
// RB acknowledgement, pneumatic EPK/KON priority and factory compliance are
// covered separately; this probe tests signal source and consumer ownership.
class TrainerMarkupSafetyProbe {
    static readonly BindingFlags InstanceFlags = BindingFlags.Instance | BindingFlags.NonPublic;
    static readonly string[] ContextLabels = { "Calc", "Saut", "NativeAvp", "Klub" };
    static readonly int[] ExpectedCodes = {
        1,3,2,0,0,0,1,1,1,1,2,1,2,2,3,3,1,2,1,0,0,0,0,3,3,0
    };
    static readonly List<object> checks = new List<object>(), rows = new List<object>();
    static readonly Stopwatch clock = new Stopwatch();
    static TrainerIntegrationHost host;
    static Dictionary<string, IntPtr> mem;
    static TrainerMarkupSignalSource source;
    static TrainerMarkupSignalSnapshot signal;
    static RouteModel route;
    static List<Delegate> auxiliarySteps;
    static Delegate klubStep;
    static ComboBox klubChoice, sautChoice;
    static int oldKlubSelection, oldSautSelection;
    static bool uiSaved, stoppedTimer, closed, inputsRestored;
    static string phase = "initial", failure;
    static int nativeCycles, initialScenarioCount, greenLamp, yellowLamp, approachLamp, passedLamp;
    static IntPtr communication, epkControl, klubModule, klubSlot, klubOwner;
    static byte oldEpkKey;
    static bool physicalKeySaved, physicalKeyRestored;

    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void NativeCall();
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module, StringBuilder path, int size);
    [DllImport("kernel32.dll")] static extern IntPtr VirtualAlloc(IntPtr address, UIntPtr size, uint kind, uint protection);
    [DllImport("kernel32.dll")] static extern bool VirtualFree(IntPtr address, UIntPtr size, uint kind);
    [DllImport("kernel32.dll")] static extern bool FlushInstructionCache(IntPtr process, IntPtr address, UIntPtr size);
    [DllImport("kernel32.dll")] static extern IntPtr GetCurrentProcess();

    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {
        public IntPtr Base, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll")]
    static extern UIntPtr VirtualQuery(IntPtr address, out MemoryInfo info, UIntPtr size);

    static object Field(string name) {
        var field = typeof(TrainerIntegrationHost).GetField(name, InstanceFlags);
        if (field == null) throw new Exception("Missing host field " + name);
        return field.GetValue(host);
    }
    static void SetField(string name, object value) {
        var field = typeof(TrainerIntegrationHost).GetField(name, InstanceFlags);
        if (field == null) throw new Exception("Missing host field " + name);
        field.SetValue(host, value);
    }
    static void Call(string name, params object[] args) {
        var method = typeof(TrainerIntegrationHost).GetMethod(name, InstanceFlags);
        if (method == null) throw new Exception("Missing host method " + name);
        try { method.Invoke(host, args); }
        catch (TargetInvocationException error) {
            throw new Exception(name + " failed in phase " + phase, error.InnerException ?? error);
        }
    }
    static void Invoke(Delegate function, string label) {
        try { function.DynamicInvoke(); }
        catch (TargetInvocationException error) {
            throw new Exception(label + " failed in phase " + phase, error.InnerException ?? error);
        }
    }
    static void Check(string name, bool passed, object expected, object actual) {
        checks.Add(new { name = name, passed = passed, expected = expected, actual = actual, phase = phase });
        if (!passed) throw new Exception(name + ": expected " + expected + "; actual " + actual);
    }
    static bool Finite(double value) {
        return (((ulong)BitConverter.DoubleToInt64Bits(value)) & 0x7FF0000000000000UL) != 0x7FF0000000000000UL;
    }
    static bool Readable(IntPtr pointer, int count) {
        MemoryInfo info;
        if (pointer == IntPtr.Zero || count < 1 ||
            VirtualQuery(pointer, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero) return false;
        ulong at = unchecked((uint)pointer.ToInt32()), start = unchecked((uint)info.Base.ToInt32());
        return info.State == 0x1000 && (info.Protect & 0x101) == 0 && at >= start &&
            at + (uint)count <= start + info.RegionSize.ToUInt32();
    }
    static string NativeName(IntPtr pointer) {
        if (!Readable(pointer, 4)) throw new Exception("Native object pointer guard");
        var vmt = Marshal.ReadIntPtr(pointer);
        if (!Readable(IntPtr.Add(vmt, -56), 60)) throw new Exception("Native VMT pointer guard");
        var name = Marshal.ReadIntPtr(vmt, -56);
        if (!Readable(name, 1)) throw new Exception("Native class-name pointer guard");
        int length = Marshal.ReadByte(name);
        if (length < 2 || length > 100 || !Readable(name, length + 1)) throw new Exception("Native class-name length guard");
        return Encoding.ASCII.GetString(Bytes(name, 1, length));
    }
    static string Hash(string path) {
        using (var hasher = SHA256.Create()) using (var stream = File.OpenRead(path)) return Hex(hasher.ComputeHash(stream));
    }
    static void GuardModule(IntPtr module, string expectedPath, string expectedSha, string label) {
        if (module == IntPtr.Zero) throw new Exception(label + " module absent");
        var path = new StringBuilder(32768);
        uint length = GetModuleFileName(module, path, path.Capacity);
        Check(label + " actual loaded path", length > 0 && length < path.Capacity &&
            String.Equals(Path.GetFullPath(path.ToString()), Path.GetFullPath(expectedPath), StringComparison.OrdinalIgnoreCase), expectedPath, path.ToString());
        Check(label + " actual loaded SHA", Hash(path.ToString()) == expectedSha, expectedSha, Hash(path.ToString()));
    }
    static void PhysicalPosition(IntPtr control, double position) {
        Check("Physical EPK native control class", NativeName(control) == "TMultiPosTumbler", "TMultiPosTumbler", NativeName(control));
        string expected = "558BEC83C4F08945";
        string actual = Hex(Bytes(communication, 0x2a4944, expected.Length / 2));
        Check("Guarded native SetPosition event ABI", actual == expected, expected, actual);
        // Same proven Delphi register/stack call used by TrainerNativeSafetyProbe.
        // The original widget event alone updates the physical key source.
        var code = new List<byte>();
        code.Add(0xb8); code.AddRange(BitConverter.GetBytes(control.ToInt32()));
        var number = BitConverter.GetBytes(position);
        code.Add(0x68); code.AddRange(new byte[] { number[4], number[5], number[6], number[7] });
        code.Add(0x68); code.AddRange(new byte[] { number[0], number[1], number[2], number[3] });
        code.AddRange(new byte[] { 0x6a, 0, 0x6a, 0 });
        code.Add(0xb9); code.AddRange(BitConverter.GetBytes(IntPtr.Add(communication, 0x2a4944).ToInt32()));
        code.AddRange(new byte[] { 0xff, 0xd1, 0xc3 });
        var thunk = VirtualAlloc(IntPtr.Zero, (UIntPtr)128, 0x3000, 0x40);
        if (thunk == IntPtr.Zero) throw new Exception("Physical input thunk allocation failed");
        try {
            Marshal.Copy(code.ToArray(), 0, thunk, code.Count);
            if (!FlushInstructionCache(GetCurrentProcess(), thunk, (UIntPtr)code.Count)) throw new Exception("Physical input thunk flush failed");
            ((NativeCall)Marshal.GetDelegateForFunctionPointer(thunk, typeof(NativeCall)))();
        } finally {
            if (!VirtualFree(thunk, UIntPtr.Zero, 0x8000)) throw new Exception("Physical input thunk release failed");
        }
    }
    static void ShowPhysicalTumba(IntPtr cab) {
        string expected="558BEC83C4F08855";
        Check("Native ShowTumba ABI guard",Hex(Bytes(communication,0x2ae994,8))==expected,expected,Hex(Bytes(communication,0x2ae994,8)));
        var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(cab.ToInt32()));
        code.Add(0xba);code.AddRange(BitConverter.GetBytes(1));
        code.Add(0xb9);code.AddRange(BitConverter.GetBytes(IntPtr.Add(communication,0x2ae994).ToInt32()));
        code.AddRange(new byte[]{0xff,0xd1,0xc3});
        var thunk=VirtualAlloc(IntPtr.Zero,(UIntPtr)128,0x3000,0x40);if(thunk==IntPtr.Zero)throw new Exception("ShowTumba input thunk allocation");
        try{Marshal.Copy(code.ToArray(),0,thunk,code.Count);if(!FlushInstructionCache(GetCurrentProcess(),thunk,(UIntPtr)code.Count))throw new Exception("ShowTumba input thunk flush");
            ((NativeCall)Marshal.GetDelegateForFunctionPointer(thunk,typeof(NativeCall)))();}
        finally{if(!VirtualFree(thunk,UIntPtr.Zero,0x8000))throw new Exception("ShowTumba input thunk release");}
    }
    static void SetupPhysicalKey() {
        phase = "physical_epk_key_setup";
        string nativeRoot = (string)Field("root");
        communication = GetModuleHandle("Communication1.dll");
        GuardModule(communication, Path.Combine(nativeRoot, @"RESOURCES\LOCOMOTIVES\1\Communication1.dll"),
            "27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F", "Communication");
        var commOwner = Memory("CommOwner", 0x2c0);
        var cab = Marshal.ReadIntPtr(commOwner, 0x2bc);
        Check("Physical EPK cab owner", NativeName(cab) == "TPultMash2ES6", "TPultMash2ES6", NativeName(cab));
        var cabForm = Marshal.ReadIntPtr(cab, 0x0c);
        Check("Real cab shared TPult", Readable(cabForm, 0x534) && Marshal.ReadIntPtr(cabForm, 0x530) == Memory("TPult", 1), true, true);
        ShowPhysicalTumba(cab);
        var tumba = Marshal.ReadIntPtr(cab, 0x10);
        Check("Physical EPK tumba owner", NativeName(tumba) == "TPult_Tumba_2ES6", "TPult_Tumba_2ES6", NativeName(tumba));
        var tumbaForm = Marshal.ReadIntPtr(tumba, 0x0c);
        if (!Readable(tumbaForm, 0x3b0)) throw new Exception("Physical EPK tumba form range guard");
        epkControl = Marshal.ReadIntPtr(tumbaForm, 0x3ac);
        Check("Actual physical EPK source control", NativeName(epkControl) == "TMultiPosTumbler", "TMultiPosTumbler", NativeName(epkControl));
        oldEpkKey = Marshal.ReadByte(Memory("TLocParam", 0xa0), 0x99);
        Check("Physical key Boolean source", oldEpkKey <= 1, "0/1", oldEpkKey);
        physicalKeySaved = true;
        PhysicalPosition(epkControl, 1);
        Check("Original widget event enables actual EPK input", Marshal.ReadByte(Memory("TLocParam", 0xa0), 0x99) == 1,
            1, Marshal.ReadByte(Memory("TLocParam", 0xa0), 0x99));
        klubModule = GetModuleHandle("KLUB_U_LCD_XE2.dll");
        GuardModule(klubModule, Path.Combine(nativeRoot, @"RESOURCES\KLUB_U_LCD_XE2.dll"),
            "671D3BDB1AD57BD8EA7AFC714B11BC14141A5B2750D72E8B5598C7BB0CFAF20F", "KLUB");
        if (!Readable(IntPtr.Add(klubModule, 0x1d7028), 4)) throw new Exception("KLUB owner-slot range guard");
        klubSlot = Marshal.ReadIntPtr(klubModule, 0x1d7028);
        if (!Readable(klubSlot, 4)) throw new Exception("KLUB singleton range guard");
        klubOwner = Marshal.ReadIntPtr(klubSlot);
        Check("Actual decoder owner class", Readable(klubOwner, 0x1d6) && NativeName(klubOwner) == "TProceedKLUB", "TProceedKLUB", NativeName(klubOwner));
        rows.Add(Snapshot("physical_epk_key_enabled"));
    }
    static void GuardDecoderOwner() {
        if (klubOwner == IntPtr.Zero || GetModuleHandle("KLUB_U_LCD_XE2.dll") != klubModule ||
            Marshal.ReadIntPtr(klubModule, 0x1d7028) != klubSlot || Marshal.ReadIntPtr(klubSlot) != klubOwner || !Readable(klubOwner, 0x1d6))
            throw new Exception("KLUB decoder owner lifetime guard");
    }
    static object DecoderSnapshot() {
        if (klubOwner == IntPtr.Zero) return new { initialized = false };
        GuardDecoderOwner();
        return new { initialized = true, owner = klubOwner.ToString("X"),
            cnt_vkl_epk_61 = Marshal.ReadInt32(klubOwner, 0x61), first_vkl_12f = Marshal.ReadByte(klubOwner, 0x12f),
            last_power_d7 = Marshal.ReadInt32(klubOwner, 0xd7), mapped_alsn_1d4 = Marshal.ReadByte(klubOwner, 0x1d4),
            last_code_way_1d5 = Marshal.ReadByte(klubOwner, 0x1d5), move_nekod_a4 = Marshal.ReadByte(klubOwner, 0xa4),
            regim_dvig_95 = Marshal.ReadByte(klubOwner, 0x95), tmp_alsn_ef = Marshal.ReadByte(klubOwner, 0xef),
            pb_power4 = Marshal.ReadByte(Memory("TPBBtn", 26), 4), epk_key99 = Marshal.ReadByte(Memory("TLocParam", 0xa0), 0x99) };
    }
    static bool DecoderQualified() {
        GuardDecoderOwner();
        return Marshal.ReadInt32(klubOwner, 0x61) == -1 && Marshal.ReadByte(klubOwner, 0x12f) == 1;
    }
    static IntPtr Memory(string name, int count) {
        IntPtr pointer;
        if (!mem.TryGetValue(name, out pointer) || !Readable(pointer, count))
            throw new Exception("Native record range unavailable: " + name);
        return pointer;
    }
    static byte[] Bytes(IntPtr pointer, int offset, int count) {
        if (!Readable(IntPtr.Add(pointer, offset), count)) throw new Exception("Snapshot range guard");
        var result = new byte[count]; Marshal.Copy(IntPtr.Add(pointer, offset), result, 0, count); return result;
    }
    static string Hex(byte[] value) { return BitConverter.ToString(value).Replace("-", ""); }
    static bool Equal(byte[] first, byte[] second) { return first.SequenceEqual(second); }
    static double Position() { return Convert.ToDouble(Field("position")); }
    static TrainerMarkupSourceFrame Frame() { return (TrainerMarkupSourceFrame)Field("markupFrame"); }
    static int ActualKlubLamp() { return Marshal.ReadByte(Memory("TOutParamAllTRen", 106), 0x20); }
    static int ActualSautMode() { return ((TrainerStandaloneSautBinding)Field("standaloneSautBinding")).NativeMode; }

    static void NoDiagnosticOverrides() {
        Check("No command-file input", !File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Trainer_commands.txt")), false,
            File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Trainer_commands.txt")));
        Check("No safety-output diagnostic overrides", !(bool)Field("safetyPower"), false, Field("safetyPower"));
        Check("No fixture KM input", Field("commandedKm") == null && Field("fileKm") == null && !(bool)Field("momentaryCharge"), true,
            Field("commandedKm") == null && Field("fileKm") == null && !(bool)Field("momentaryCharge"));
    }

    static void CheckRoute() {
        phase = "full_native_course";
        route = (RouteModel)Field("route"); source = (TrainerMarkupSignalSource)Field("markupSource");
        Check("Original markup selected before StartNative", source != null && (bool)Field("nativeMarkupEnabled"), true, source != null);
        Check("Native positive coordinate axis", route.native_axis && route.direction == 1, true, route.native_axis);
        Check("Native initial railway coordinate", Math.Abs(route.X(2.6) - 2600) < .000001, 2600, route.X(2.6));
        Check("Native inverse coordinate", Math.Abs(route.Km(2774) - 2.774) < .000001, 2.774, route.Km(2774));
        Check("Full native profiles", source.Profiles.Length == 224 && route.profile.Count == 224, 224, route.profile.Count);
        Check("Full native signal set", source.Signals.Length == 181 && route.signals.Count == 181, 181, route.signals.Count);
        Check("Full native plan curves", route.curves.Count == 177, 177, route.curves.Count);
        Check("Full native stations and raw pikets", source.Stations.Length == 27 && source.Pikets.Length == 3273, "27 /3273",
            source.Stations.Length + " /" + source.Pikets.Length);
        Check("Native profile end retained", source.NativeCourseEnd == 333033 && Math.Abs(route.Length - 333033) < .000001, 333033, route.Length);
        Check("Koksharovsky prefix separate from full end", source.StationKokPrefixEnd == 191500 && source.StationKokPrefixEnd < source.NativeCourseEnd,
            191500, source.StationKokPrefixEnd);
        var profileRows = source.Profiles;
        for (int i = 0; i < profileRows.Length; i++) {
            var original = profileRows[i]; var loaded = route.profile[i];
            Check("Original native profile " + i,
                Math.Abs(route.X(loaded.km_start) - original.Nach) < .000001 &&
                Math.Abs(route.X(loaded.km_end) - original.Nach - original.Len) < .000001 && loaded.grade_permille == original.Uklon,
                "Same Nach/Len/Uklon", "x=" + route.X(loaded.km_start) + ", length=" + (route.X(loaded.km_end) - route.X(loaded.km_start)));
        }
        var signalRows = source.Signals;
        for (int i = 0; i < signalRows.Length; i++)
            Check("Original native signal coordinate " + i,
                Math.Abs(route.X(route.signals[i].km) - signalRows[i].Meter) < .000001 && route.signals[i].label == signalRows[i].Name,
                signalRows[i].Name + " @" + signalRows[i].Meter,
                route.signals[i].label + " @" + route.X(route.signals[i].km));
        Check("Explicit teaching operational cap", route.speed_limits.Count == 1 && route.speed_limits[0].speed_kmh == 80, 80,
            route.speed_limits.Count == 1 ? route.speed_limits[0].speed_kmh : -1);
        Check("Full source metadata", source.LightMetadata.FullConsumedSize == 4863 && source.ProfileMetadata.FullConsumedSize == 2702 &&
            source.StationMetadata.FullConsumedSize == 1171 && source.PiketMetadata.FullConsumedSize == 55655, "4863/2702/1171/55655",
            source.LightMetadata.FullConsumedSize + "/" + source.ProfileMetadata.FullConsumedSize + "/" +
            source.StationMetadata.FullConsumedSize + "/" + source.PiketMetadata.FullConsumedSize);
        Check("No declared live occupancy or raw ALSN", !source.HasOccupancySource && !source.IsLiveHardwareSource, false, source.IsLiveHardwareSource);
        Check("Unrepaired terminal picket anomaly retained", source.OrderedPiketPrefixCount == 3272 && source.Pikets[3272].Offset == 0, "3272 prefix; final0",
            source.OrderedPiketPrefixCount + " prefix; final" + source.Pikets[3272].Offset);
        signal = source.Signals.Single(row => row.Id == 1);
        Check("Original forward N2 target", signal.Name == "Н2" && signal.Meter == 2774 && signal.OriginalState == 10, "Н2 @2774; state10",
            signal.Name + " @" + signal.Meter + "; state" + signal.OriginalState);
    }

    static void PureMapChecks() {
        phase = "pure_native_encoder_contract";
        long originalGeneration = source.Generation;
        var copy = source.Clone(); copy.ResetPosition(signal.Meter - 1);
        for (int state = 0; state < ExpectedCodes.Length; state++) {
            copy.SetTeachingSignalState(signal.Id, state);
            var frame = copy.Sample(signal.Meter - 1);
            Check("Pinned native clean-state map " + state, frame.CodeWay == ExpectedCodes[state], ExpectedCodes[state], frame.CodeWay);
            Check("No raw ALSN inference " + state, !frame.HasRawAlsn, false, frame.HasRawAlsn);
        }
        bool invalidStateRefused = false;
        try { copy.SetTeachingSignalState(signal.Id, 26); } catch (ArgumentOutOfRangeException) { invalidStateRefused = true; }
        Check("Unproved state refused", invalidStateRefused, true, invalidStateRefused);
        var snapshots = source.Signals; snapshots[1].State = 1;
        Check("Diagnostic DTO cannot modify original source", source.Signals[1].State == signal.OriginalState && source.Generation == originalGeneration,
            signal.OriginalState, source.Signals[1].State);
        Check("Clone changes independent of production generation", source.Generation == originalGeneration, originalGeneration, source.Generation);
        bool hashRefused = false;
        try { TrainerMarkupSignalSource.Load(source.SourcePath, new string('0', 64)); } catch (InvalidDataException) { hashRefused = true; }
        Check("Exact normalized JSON SHA pin required", hashRefused, true, hashRefused);
    }

    static void CheckContextGraph() {
        phase = "native_consumer_contexts";
        var input = Memory("TInputParamPult", 52); var light = Memory("TCurLighterRec", 59);
        var privatePointers = new HashSet<IntPtr>();
        foreach (string label in ContextLabels) {
            var privateInput = Memory(label + "SafetyInput", 52);
            var privateLight = Memory(label + "SafetyLight", 59);
            var shared = Memory(label + "SafetySharedRecord", 24);
            Check(label + " private input/light", privateInput != input && privateLight != light && privatePointers.Add(privateInput) && privatePointers.Add(privateLight), true, true);
            Check(label + " shared -> own input", Marshal.ReadIntPtr(shared, 8) == privateInput, privateInput.ToString("X"), Marshal.ReadIntPtr(shared, 8).ToString("X"));
            Check(label + " input -> own signal", Marshal.ReadIntPtr(privateInput, 0x1f) == privateLight, privateLight.ToString("X"), Marshal.ReadIntPtr(privateInput, 0x1f).ToString("X"));
        }
        auxiliarySteps = new List<Delegate>();
        foreach (object entry in (IEnumerable)Field("otherSteps")) {
            var step = entry as Delegate;
            if (step == null) throw new Exception("Unknown auxiliary native step");
            auxiliarySteps.Add(step);
        }
        Check("Three selected auxiliary consumer steps", auxiliarySteps.Count == 3, 3, auxiliarySteps.Count);
        Check("Alternative KPD not initialized beside selected KLUB", !mem.ContainsKey("KpdSafetyInput") && Marshal.ReadByte(mem["TOutParamAllTRen"],0x23)==0, true, !mem.ContainsKey("KpdSafetyInput"));
        klubStep = (Delegate)Field("klubStep");
        Check("Production KLUB step present", klubStep != null, true, klubStep != null);
    }

    // The only initial physical sensor writes in this fixture are measured
    // readTrain results immediately after the permitted new-scenario stateTrain.
    // This method is never called by a running motion step.
    static void InitialScenario(double nativeX, float initialSpeedKmh, string label) {
        phase = "declared_initial_" + label;
        Call("CancelAutovedenie", "Изменены начальные условия проверки разметки");
        var initialize = (Delegate)Field("stateTrain");
        initialize.DynamicInvoke(new object[] { (float)nativeX, initialSpeedKmh/3.6f, 2, 76 });
        // POEZD publishes its current output vector on the first real model step.
        Call("MotionCycle");
        var nativeRead = (Delegate)Field("readTrain");
        var measured = new object[] { 0f, 0f, 0f, 0f, 0f, 0 };
        nativeRead.DynamicInvoke(measured);
        float x = Convert.ToSingle(measured[0]), velocity = Convert.ToSingle(measured[1]), acceleration = Convert.ToSingle(measured[3]);
        Check("Native initial readback " + label, Finite(x) && Finite(velocity) && Finite(acceleration) && Math.Abs(x - nativeX) < (initialSpeedKmh==0?.001:initialSpeedKmh/3.6*.11),
            nativeX, x);
        SetField("position", x); SetField("signedVelocity", velocity); SetField("acceleration", acceleration);
        var input = Memory("TInputParamPult", 52);
        InitialSensorFloat(input, 1, Math.Abs(velocity) * 3.6f);
        InitialSensorFloat(input, 0x0d, x);
        Marshal.WriteInt32(input, 0x11, (int)x);
        InitialSensorFloat(input, 0x19, acceleration);
        source.ResetPosition(x);
        SetField("klubBasisReady", false);
        Call("ApplyTestInputs"); Call("RouteInputCycle");
        initialScenarioCount++;
        var frame = Frame();
        Check("Declared forward target " + label, frame.Index == signal.Id && frame.Xlight == signal.Meter && !frame.PassedStop,
            signal.Id, frame.Index);
        rows.Add(Snapshot(label + "_initial"));
    }
    static void InitialSensorFloat(IntPtr input, int offset, float value) {
        if (!phase.StartsWith("declared_initial_", StringComparison.Ordinal)) throw new Exception("Initial sensor writes outside declared scenario");
        if (input != Memory("TInputParamPult", 52) || !(offset == 1 || offset == 0x0d || offset == 0x19)) throw new Exception("Initial sensor field guard");
        Marshal.Copy(BitConverter.GetBytes(value), 0, IntPtr.Add(input, offset), 4);
    }

    static void PublishedSourceCheck() {
        var frame = Frame(); var input = Memory("TInputParamPult", 52); var light = Memory("TCurLighterRec", 59);
        Check("Source input alias", Marshal.ReadIntPtr(input, 0x1f) == light, light.ToString("X"), Marshal.ReadIntPtr(input, 0x1f).ToString("X"));
        Check("Primary Index/coordinates from markup", Marshal.ReadInt32(light, 0) == frame.Index &&
            Marshal.ReadInt32(light, 0x19) == frame.Xlight && Marshal.ReadInt32(light, 0x1d) == frame.XlastLight,
            frame.Index + "/" + frame.Xlight + "/" + frame.XlastLight,
            Marshal.ReadInt32(light, 0) + "/" + Marshal.ReadInt32(light, 0x19) + "/" + Marshal.ReadInt32(light, 0x1d));
        Check("Fixed native Name length byte within20", Marshal.ReadByte(light, 4) <= 20, "<=20", Marshal.ReadByte(light, 4));
        Check("Primary SendCode/BlocUch", Marshal.ReadByte(light, 0x21) == (frame.SendCode ? 1 : 0) && Marshal.ReadByte(light, 0x22) == 0, true, true);
        Check("CodeWay primary source only", Marshal.ReadByte(input, 0x1e) == frame.CodeWay && !frame.HasRawAlsn, frame.CodeWay, Marshal.ReadByte(input, 0x1e));
    }

    static void NativeConsumer(Delegate step, string label) {
        var canonicalLight = Memory("TCurLighterRec", 59); var canonicalInput = Memory("TInputParamPult", 52);
        byte[] canonicalSource = Bytes(canonicalLight, 0, 35), canonicalDerived = Bytes(canonicalLight, 35, 24);
        byte alsnSource = Marshal.ReadByte(canonicalInput, 0x1d);
        var otherDerived = new Dictionary<string, byte[]>();
        foreach (string other in ContextLabels)
            if (other != label) otherDerived.Add(other, Bytes(Memory(other + "SafetyLight", 59), 35, 24));
        Invoke(step, label);
        if (label == "Klub") Call("PublishNativeKlubAlsn");
        Check(label + " cannot change source35", Equal(canonicalSource, Bytes(canonicalLight, 0, 35)), Hex(canonicalSource), Hex(Bytes(canonicalLight, 0, 35)));
        Check(label + " cannot change canonical derived24", Equal(canonicalDerived, Bytes(canonicalLight, 35, 24)), Hex(canonicalDerived), Hex(Bytes(canonicalLight, 35, 24)));
        if (label == "Klub")
            Check("Original native KLUB output publishes ALSN1D", Marshal.ReadByte(canonicalInput, 0x1d) == ActualKlubLamp(),
                ActualKlubLamp(), Marshal.ReadByte(canonicalInput, 0x1d));
        else
            Check(label + " does not fabricate InputALSN1D", Marshal.ReadByte(canonicalInput, 0x1d) == alsnSource,
                alsnSource, Marshal.ReadByte(canonicalInput, 0x1d));
        var ownInput = Memory(label + "SafetyInput", 52); var ownLight = Memory(label + "SafetyLight", 59);
        Check(label + " receives exact primary source35", Equal(canonicalSource, Bytes(ownLight, 0, 35)), Hex(canonicalSource), Hex(Bytes(ownLight, 0, 35)));
        Check(label + " receives primary CodeWay", Marshal.ReadByte(ownInput, 0x1e) == Frame().CodeWay, Frame().CodeWay, Marshal.ReadByte(ownInput, 0x1e));
        Check(label + " next Sync receives actual previous native ALSN", Marshal.ReadByte(ownInput, 0x1d) == alsnSource,
            alsnSource, Marshal.ReadByte(ownInput, 0x1d));
        Check(label + " owns signal pointer", Marshal.ReadIntPtr(ownInput, 0x1f) == ownLight, ownLight.ToString("X"), Marshal.ReadIntPtr(ownInput, 0x1f).ToString("X"));
        foreach (var pair in otherDerived)
            Check(label + " preserves " + pair.Key + " derived state", Equal(pair.Value, Bytes(Memory(pair.Key + "SafetyLight", 59), 35, 24)),
                Hex(pair.Value), Hex(Bytes(Memory(pair.Key + "SafetyLight", 59), 35, 24)));
        Check(label + " derived Uklon/Vpr finite", DerivedFinite(ownLight), true, DerivedFinite(ownLight));
    }
    static bool DerivedFinite(IntPtr light) {
        uint uklon = unchecked((uint)Marshal.ReadInt32(light, 0x27));
        ulong vpr = unchecked((ulong)Marshal.ReadInt64(light, 0x2f));
        return (uklon & 0x7f800000U) != 0x7f800000U && (vpr & 0x7ff0000000000000UL) != 0x7ff0000000000000UL;
    }
    static void SensorCycle() {
        if (clock.Elapsed.TotalSeconds > 90) throw new Exception("Markup probe wall-clock bound");
        Call("ApplyTestInputs");
        byte originalAlsn = Marshal.ReadByte(Memory("TInputParamPult", 52), 0x1d);
        Call("RouteInputCycle"); PublishedSourceCheck();
        Check("Markup publisher does not guess ALSN1D", Marshal.ReadByte(Memory("TInputParamPult", 52), 0x1d) == originalAlsn,
            originalAlsn, Marshal.ReadByte(Memory("TInputParamPult", 52), 0x1d));
        for (int i = 0; i < auxiliarySteps.Count; i++) {
            rows.Add(Snapshot("before_" + ContextLabels[i]));
            NativeConsumer(auxiliarySteps[i], ContextLabels[i]);
        }
        Call("KlubCoordinateCycle"); NativeConsumer(klubStep, "Klub");
        nativeCycles++;
    }

    static void WaitLamp(int expected, string label) {
        phase = label;
        for (int i = 0; i < 120; i++) {
            SensorCycle();
            if (ActualKlubLamp() == expected && DecoderQualified() && nativeCycles > 50) break;
            if (i == 0 || i % 10 == 0) rows.Add(Snapshot(label + "_decoder_wait_" + i));
            Application.DoEvents();
            System.Threading.Thread.Sleep(10);
        }
        Check("Actual native decoder qualification " + label, DecoderQualified() && nativeCycles > 50,
            "counter-1 /firstVkl1 />50 steps", DecoderSnapshot());
        Check("Actual native KLUB Out20 " + label, ActualKlubLamp() == expected, expected, ActualKlubLamp());
        rows.Add(Snapshot(label));
    }

    static void SignalsAndBoundaryCases() {
        source.SetTeachingSignalState(signal.Id, 0);
        InitialScenario(signal.Meter - 71, 0, "green_71"); WaitLamp(1, "green_native"); greenLamp = ActualKlubLamp();
        source.SetTeachingSignalState(signal.Id, 2);
        Call("RouteInputCycle"); WaitLamp(2, "yellow_native"); yellowLamp = ActualKlubLamp();
        source.SetTeachingSignalState(signal.Id, 1);
        Call("RouteInputCycle"); WaitLamp(3, "red_approach_native"); approachLamp = ActualKlubLamp();
        foreach (int distance in new[] { 71, 70, 69, 1, 0 }) {
            InitialScenario(signal.Meter - distance, 0, "red_distance_" + distance);
            Check("Exact declared red source distance " + distance, Frame().Xlight - Frame().NativeX == distance, distance, Frame().Xlight - Frame().NativeX);
            WaitLamp(3, "red_distance_" + distance + "_native");
            Check("No premature passed-stop latch " + distance, !Frame().PassedStop && Frame().CodeWay == 3, "false /3", Frame().PassedStop + " /" + Frame().CodeWay);
        }

        // One moving scenario. Subsequent position/speed updates come ONLY from
        // the original MotionCycle/POEZD. No stateTrain calls occur in the loop.
        InitialScenario(signal.Meter - 1, 8, "red_physical_crossing"); WaitLamp(3, "red_before_physical_pass");
        phase = "physical_red_pass";
        bool reachedOneMeterPast = false;
        for (int n = 0; n < 100; n++) {
            Call("MotionCycle"); Call("RouteInputCycle"); SensorCycle();
            if (Position() >= signal.Meter + 1) { reachedOneMeterPast = true; break; }
        }
        Check("Native physical motion reached distance<=-1", reachedOneMeterPast, true, reachedOneMeterPast);
        int primaryIntegerDistance = signal.Meter - Marshal.ReadInt32(Memory("TInputParamPult", 52), 0x11);
        Check("Native integer sensor passed -1 boundary", primaryIntegerDistance <= -1, "<=-1", primaryIntegerDistance);
        var passed = Frame();
        Check("Passed stop source latch", passed.PassedStop && source.PassedStop, true, passed.PassedStop);
        Check("Blocked original light target retained", passed.Index == signal.Id && passed.Xlight == signal.Meter, signal.Id, passed.Index);
        Check("Passed CodeWay0 instead of following green", passed.CodeWay == 0, 0, passed.CodeWay);
        WaitLamp(4, "passed_red_native"); passedLamp = ActualKlubLamp();
        source.SetTeachingSignalState(signal.Id, 0); Call("RouteInputCycle");
        Check("Changing to green does not clear passed-stop latch", Frame().PassedStop && Frame().CodeWay == 0 && Frame().Index == signal.Id,
            "latched /0 /originalId", Frame().PassedStop + " /" + Frame().CodeWay + " /" + Frame().Index);
        WaitLamp(4, "passed_red_remains_after_teaching_change");
        InitialScenario(signal.Meter - 71, 0, "explicit_new_scenario_after_pass");
        Check("Explicit scenario reset clears source latch only", !source.PassedStop && !Frame().PassedStop, false, source.PassedStop);
        WaitLamp(1, "green_after_explicit_source_reset");
    }

    static object Snapshot(string label) {
        if (mem == null) return new { label = label, phase = phase, initialized = false };
        var frame = Frame();
        var contexts = new List<object>();
        foreach (string name in ContextLabels) {
            IntPtr input, light;
            if (mem.TryGetValue(name + "SafetyInput", out input) && mem.TryGetValue(name + "SafetyLight", out light) && Readable(input, 52) && Readable(light, 59))
                contexts.Add(new { consumer = name, input = input.ToString("X"), light = light.ToString("X"),
                    code_way = Marshal.ReadByte(input, 0x1e), actual_alsn_source_1d = Marshal.ReadByte(input, 0x1d),
                    source35 = Hex(Bytes(light, 0, 35)), derived24 = Hex(Bytes(light, 35, 24)),
                    derived_finite = DerivedFinite(light) });
        }
        return new { label = label, phase = phase, native_cycles = nativeCycles, initial_scenarios = initialScenarioCount,
            head_native_x = Position(), source_index = frame == null ? -1 : frame.Index, source_name = frame == null ? "" : frame.Name,
            source_state = frame == null ? -1 : frame.State, source_code_way = frame == null ? -1 : frame.CodeWay,
            source_target_m = frame == null ? -1 : frame.Xlight,
            source_distance_m = frame == null ? 0 : frame.Xlight - Position(), passed_stop = frame != null && frame.PassedStop,
            generation = frame == null ? -1 : frame.Generation, actual_klub_out20 = ActualKlubLamp(),
            actual_input_alsn_1d = Marshal.ReadByte(Memory("TInputParamPult", 52), 0x1d), actual_saut_mode = ActualSautMode(),
            actual_decoder = DecoderSnapshot(),
            canonical_source35 = Hex(Bytes(Memory("TCurLighterRec", 59), 0, 35)),
            canonical_derived24 = Hex(Bytes(Memory("TCurLighterRec", 59), 35, 24)), consumer_contexts = contexts };
    }
    static void Save(bool passed) {
        var serializer = new JavaScriptSerializer { MaxJsonLength = 8000000 };
        File.WriteAllText(TrainerSession.ResultPath("Markup_safety.json"), serializer.Serialize(new {
            passed = passed, error = failure, phase = phase, checks = checks, rows = rows,
            cycles = nativeCycles, declared_initial_scenarios = initialScenarioCount,
            elapsed_seconds = clock.Elapsed.TotalSeconds, stopped_timer = stoppedTimer,
            operator_inputs_restored = inputsRestored, host_closed = closed,
            physical_epk_key_saved = physicalKeySaved, physical_epk_key_restored = physicalKeyRestored,
            actual_lamps = new { green = greenLamp, yellow = yellowLamp, approach_kz = approachLamp, passed_red = passedLamp },
            source_json_sha256 = source == null ? "" : source.SourceSha256,
            scope = "Original SHA-pinned native course, managed teaching state channel, selected original Calc/SAUT/nativeAVP/KLUB steps and private contexts. Boundary positions are declared initial stateTrain conditions; the passed-stop case moves through original POEZD only. No derived/protection/output writes, callback fixtures, FPU masking or failed-calculation substitution. EPK/KON/RB/pneumatic priority is a separate acceptance scope."
        }), Encoding.UTF8);
    }

    [STAThread] static int Main() {
        Application.EnableVisualStyles(); clock.Start(); int exit = 1;
        try {
            if (IntPtr.Size != 4) throw new Exception("Markup native probe requires x86");
            host = (TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost), true);
            var handle = host.Handle;
            SetField("nativeMarkupEnabled", true); // before any native initialization
            phase = "start_native_markup"; Call("StartNative");
            var timer = (Timer)Field("timer"); Check("Production native host started", timer != null, true, timer != null);
            timer.Stop(); stoppedTimer = true;
            mem = (Dictionary<string, IntPtr>)Field("mem");
            Check("Native physics initialized", (bool)Field("physicsReady"), true, Field("physicsReady"));
            Check("Native KLUB initialized", (bool)Field("klubReady"), true, Field("klubReady"));
            NoDiagnosticOverrides(); CheckRoute(); PureMapChecks(); CheckContextGraph();
            klubChoice = (ComboBox)Field("klubPowerChoice"); sautChoice = (ComboBox)Field("standaloneSautChoice");
            oldKlubSelection = klubChoice.SelectedIndex; oldSautSelection = sautChoice.SelectedIndex; uiSaved = true;
            klubChoice.SelectedIndex = 0; sautChoice.SelectedIndex = 1; // explicit production operator input channels
            Call("ApplyTestInputs"); SetupPhysicalKey(); SignalsAndBoundaryCases();
            Check("Same full physical route after cases", Object.ReferenceEquals(route, Field("route")) && Math.Abs(route.Length - 333033) < .000001 &&
                route.profile.Count == 224 && route.signals.Count == 181, true, true);
            exit = 0;
        } catch (Exception error) {
            failure = error.ToString(); Console.WriteLine(error);
            try { rows.Add(Snapshot("failure")); } catch (Exception snapshot) { failure += "\nSnapshot: " + snapshot; }
        } finally {
            if (host != null) {
                try {
                    if (source != null) source.RestorePersistedSignalStates();
                    if (uiSaved) { sautChoice.SelectedIndex = oldSautSelection; klubChoice.SelectedIndex = oldKlubSelection; }
                    if (physicalKeySaved) {
                        phase = "restore_physical_epk_key";
                        PhysicalPosition(epkControl, oldEpkKey);
                        Check("Original physical EPK key restored before native close", Marshal.ReadByte(Memory("TLocParam", 0xa0), 0x99) == oldEpkKey,
                            oldEpkKey, Marshal.ReadByte(Memory("TLocParam", 0xa0), 0x99));
                        physicalKeyRestored = true;
                    }
                    inputsRestored = true;
                } catch (Exception restore) { exit = 1; failure = (failure ?? "") + "\nRESTORE: " + restore; Console.WriteLine(restore); }
                try { host.Close(); host.Dispose(); closed = true; }
                catch (Exception close) { exit = 1; failure = (failure ?? "") + "\nCLOSE: " + close; Console.WriteLine(close); }
            }
            try { Save(exit == 0); } catch (Exception save) { exit = 1; Console.WriteLine(save); }
        }
        Console.WriteLine(exit == 0 ? "PASS original markup native safety source" : "FAIL original markup native safety source");
        return exit;
    }
}
