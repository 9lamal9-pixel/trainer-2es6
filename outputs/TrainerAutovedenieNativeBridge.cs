using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// The verified TPultDLLClass callback is the only interception point. Each call
// invokes the original zero-argument stdcall ProceedPult once. Temporary operator
// requests are restored before returning; native commands and outputs are untouched.
public sealed class TrainerAutovedenieNativeBridge : IDisposable {
    const string PultSha = "7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5";
    const string CommunicationSha = "27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F";

    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void NativeStep();
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module, string name);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
    static extern uint GetModuleFileName(IntPtr module, StringBuilder name, int capacity);
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {
        public IntPtr Base, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr address, out MemoryInfo info, UIntPtr size);

    // A failed detach must not leave a native callback pointing to a collected
    // delegate. Remove the owner only after the callback slot is confirmed original.
    static readonly List<TrainerAutovedenieNativeBridge> ActiveOwners = new List<TrainerAutovedenieNativeBridge>();
    readonly uint thread;
    readonly IntPtr communication, pultModule, commOwner, pult, loc, tyga;
    readonly IntPtr commVmt, provider, providerVmt, callbackSlot, originalPointer;
    readonly IntPtr globalSlot, pultOwner, pultOwnerVmt, schema, updater;
    readonly Action<string> cancellation;
    readonly NativeStep original;
    Func<IntPtr, IDisposable> operatorInputSource;
    Func<string> additionalSafetySource;
    NativeStep observer;
    IntPtr observerPointer;
    bool rooted, disposed, insideCallback, enabled, cancellationLatched, releaseCoastQueued;
    int requestedCommand;
    string fault = "", cancellationReason = "";
    bool ownsSelector, ownsCoast;
    int savedSelector, assignedSelector;
    byte savedCoast, assignedCoast;

    public string Fault { get { return fault; } }
    public bool OwnsCommands { get { return enabled && !disposed && !cancellationLatched && fault.Length == 0; } }
    public bool CancellationLatched { get { return cancellationLatched; } }
    public string ReadyReason {
        get {
            ThreadGuard();
            if (fault.Length != 0) return fault;
            if (cancellationLatched) return cancellationReason;
            try { GuardGraph(true); using (BorrowOperatorInputs()) { return ReadinessReason(); } }
            catch (Exception error) { RecordFault("Проверка моста АВП: " + error.Message); return fault; }
        }
    }

    public static TrainerAutovedenieNativeBridge Attach(IntPtr commOwner, IntPtr realPult,
        IntPtr loc, Action<string> cancel) {
        return new TrainerAutovedenieNativeBridge(commOwner, realPult, loc, cancel);
    }

    TrainerAutovedenieNativeBridge(IntPtr owner, IntPtr realPult, IntPtr location, Action<string> cancel) {
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("AVP native bridge requires x86");
        if (owner == IntPtr.Zero || realPult == IntPtr.Zero || location == IntPtr.Zero)
            throw new ArgumentException("AVP native record pointer absent");
        if (cancel == null) throw new ArgumentNullException("cancel");
        thread = GetCurrentThreadId(); cancellation = cancel;
        commOwner = owner; pult = realPult; loc = location;
        communication = GetModuleHandle("Communication1.dll");
        pultModule = GetModuleHandle("Pult1.dll");
        CheckModule(communication, "Communication1.dll", CommunicationSha);
        CheckModule(pultModule, "Pult1.dll", PultSha);
        RequireReadable(commOwner, 0x268); RequireReadable(pult, 0xc7); RequireReadable(loc, 0x9e);
        commVmt = RequireObjectVmt(commOwner, communication, null);
        if (pult != IntPtr.Add(commOwner, 0x24)) throw new Exception("AVP embedded TPult guard");
        provider = ReadPointer(commOwner, 0x264);
        providerVmt = RequireObjectVmt(provider, communication, "TPultDLLClass");
        callbackSlot = IntPtr.Add(provider, 8);
        RequireWritable(callbackSlot, 4);
        originalPointer = ReadPointer(provider, 8);
        if (originalPointer == IntPtr.Zero || originalPointer != GetProcAddress(pultModule, "ProceedPult"))
            throw new Exception("AVP ProceedPult export guard");
        original = (NativeStep)Marshal.GetDelegateForFunctionPointer(originalPointer, typeof(NativeStep));
        globalSlot = ReadPointer(pultModule, 0x1a2e60);
        pultOwner = ReadPointer(globalSlot, 0);
        pultOwnerVmt = RequireObjectVmt(pultOwner, pultModule, null);
        RequireReadable(pultOwner, 0x13c);
        if (ReadPointer(pultOwner, 4) != pult) throw new Exception("AVP Pult owner record guard");
        schema = IntPtr.Add(pultOwner, 0x18);
        updater = ReadPointer(pultOwner, 0xc);
        RequireObjectVmt(updater, pultModule, "TUpdateTygaDllClass");
        tyga = ReadPointer(pult, 0x10);
        RequireReadable(tyga, 0x17);
        if (tyga != IntPtr.Add(commOwner, 0x131) || ReadPointer(pultOwner, 8) != tyga || ReadPointer(pult, 4) != loc)
            throw new Exception("AVP shared Tyga/Loc pointer guard");
        RequireWritable(IntPtr.Add(pult, 0x69), 4);
        RequireWritable(IntPtr.Add(pult, 0x26), 1);
        observer = OnNativeStep;
        observerPointer = Marshal.GetFunctionPointerForDelegate(observer);
        GuardGraph(false);
        lock (ActiveOwners) { ActiveOwners.Add(this); rooted = true; }
        try {
            if (ReadPointer(provider, 8) != originalPointer) throw new Exception("AVP callback changed before attach");
            Marshal.WriteIntPtr(callbackSlot, observerPointer);
            if (ReadPointer(provider, 8) != observerPointer) throw new Exception("AVP callback install failed");
        } catch {
            // Dispose retains this owner if it cannot establish safe restoration.
            Dispose();
            throw;
        }
    }

    public void SubmitDecision(TrainerAvpDecision decision) {
        ThreadGuard();
        if (decision == null) { RecordFault("Решение АВП отсутствует"); return; }
        if (!decision.DriveEnabled) {
            enabled = false; requestedCommand = 0;
            // A cancelled controller must first publish its non-driving state.
            // Later ownership requires its explicit RequestAutomatic path.
            cancellationLatched = false; cancellationReason = "";
            return;
        }
        if (fault.Length != 0 || cancellationLatched) return;
        if (decision.State != TrainerAvpState.Automatic && decision.State != TrainerAvpState.Finished) {
            RecordFault("Несогласованное состояние владения АВП"); return;
        }
        int command = decision.TractionCommand;
        if (command != 0 && command != 1 && command != 5) {
            RecordFault("Неподдерживаемая native-команда АВП"); return;
        }
        requestedCommand = command; enabled = true;
    }

    // An explicit software operator panel may supply the same input cells as the
    // absent hardware panel. It is independent of AVP decisions and disabled by
    // default. The source must own and restore only its selected operator inputs.
    public void SetOperatorInputSource(Func<IntPtr, IDisposable> source) {
        ThreadGuard();
        if (enabled) throw new InvalidOperationException("Cancel AVP before changing its operator input source");
        operatorInputSource = source;
    }
    public void SetAdditionalSafetySource(Func<string> source) {
        ThreadGuard();
        if (enabled) throw new InvalidOperationException("Cancel AVP before changing its safety source");
        additionalSafetySource = source;
    }
    string AdditionalSafetyReason() {
        if (additionalSafetySource == null) return "";
        try { return additionalSafetySource() ?? ""; }
        catch (Exception error) { string reason="Источник защиты САУТ: "+error.Message; RecordFault(reason); return reason; }
    }
    // An explicit exit or loss of the AVP's data/permission releases its last
    // traction position through the native coast input. A new physical request
    // or protection command always takes precedence over this single handoff.
    public void ReleaseCommands() {
        ThreadGuard(); enabled = false; requestedCommand = 0; releaseCoastQueued = true;
    }
    IDisposable BorrowOperatorInputs() {
        return operatorInputSource == null ? null : operatorInputSource(pult);
    }

    void OnNativeStep() {
        if (insideCallback) {
            RecordFault("Повторный вход в native-мост АВП");
            try { RestoreTemporaryInputs(); } catch (Exception error) { RecordFault("Восстановление входов АВП: " + error.Message); }
            InvokeOriginalContained();
            return;
        }
        insideCallback = true;
        IDisposable inputScope = null;
        try {
            try {
                if (disposed || GetCurrentThreadId() != thread) throw new Exception("AVP callback lifetime/thread guard");
                GuardGraph(true);
                // Retry a previously failed scoped restoration before the next
                // native call; a disabled bridge must not replay its old pulse.
                if (ownsSelector || ownsCoast) RestoreTemporaryInputs();
                inputScope = BorrowOperatorInputs();
                // Sample all physical requests BEFORE borrowing any input cell.
                int selector = Marshal.ReadInt32(pult, 0x69), excitation = Marshal.ReadInt32(pult, 0x65);
                byte coast = Marshal.ReadByte(pult, 0x26), emergency = Marshal.ReadByte(pult, 0x23), emergency2 = Marshal.ReadByte(pult, 0x24);
                int reversor = Marshal.ReadInt32(pult, 0x81);
                string externalSafety=AdditionalSafetyReason();
                if (externalSafety.Length!=0) {
                    Relinquish(externalSafety); releaseCoastQueued=false;
                    // A real protection request outranks manual and AVP traction.
                    // Borrow only the native operator coast/selector inputs.
                    savedSelector=selector; assignedSelector=0; ownsSelector=true;
                    Marshal.WriteInt32(pult,0x69,0);
                    savedCoast=coast; assignedCoast=1; ownsCoast=true;
                    Marshal.WriteByte(pult,0x26,1);
                } else if (releaseCoastQueued) {
                    releaseCoastQueued = false;
                    bool protectedRequest = Marshal.ReadByte(loc, 0x9b) != 0 || Marshal.ReadByte(loc, 0x9d) != 0 ||
                        Marshal.ReadByte(pult, 0xc6) != 0 || (Marshal.ReadByte(loc, 0x89) != 0 &&
                        (Marshal.ReadByte(loc, 0x8a) != 0 || Marshal.ReadByte(loc, 0x8b) != 0));
                    if (selector == 0 && excitation == 0 && coast == 0 && emergency == 0 && emergency2 == 0 &&
                        Marshal.ReadByte(loc,0x7d)==2 && Marshal.ReadByte(loc,0x7e)==2 && !protectedRequest) {
                        savedCoast = coast; assignedCoast = 1; ownsCoast = true;
                        Marshal.WriteByte(pult, 0x26, assignedCoast);
                    }
                }
                if (enabled) {
                    string physical = emergency != 0 || emergency2 != 0 ? "Экстренное торможение машиниста" :
                        Marshal.ReadByte(loc,0x7d)!=2 || Marshal.ReadByte(loc,0x7e)!=2 ? "Ручное управление тормозами" :
                        selector != 0 || excitation != 0 || coast != 0 ? "Ручное управление" :
                        reversor != 1 ? "Изменено направление движения" : "";
                    if (physical.Length != 0) Relinquish(physical);
                    else {
                        string notReady = ReadinessReason();
                        if (notReady.Length != 0) Relinquish(notReady);
                        else {
                            int command = requestedCommand;
                            // A late native TM interlock must not increase RK.
                            // Preserve own pneumatic control; never clear the lamp.
                            if (command==1 && Marshal.ReadByte(loc,0x11)!=0) command=5;
                            // Consume a +1 pulse before native execution. A later
                            // callback cannot replay it without a fresh decision.
                            requestedCommand = 0;
                            savedSelector = selector; assignedSelector = command == 1 ? 1 : 0;
                            ownsSelector = true;
                            Marshal.WriteInt32(pult, 0x69, assignedSelector);
                            if (Marshal.ReadInt32(pult, 0x69) != assignedSelector) throw new Exception("AVP selector write guard");
                            if (command == 5) {
                                savedCoast = coast; assignedCoast = 1; ownsCoast = true;
                                Marshal.WriteByte(pult, 0x26, assignedCoast);
                                if (Marshal.ReadByte(pult, 0x26) != assignedCoast) throw new Exception("AVP coast write guard");
                            }
                        }
                    }
                }
            } catch (Exception error) {
                RecordFault("Native-мост АВП: " + error.Message);
                // A preparation failure must not leave a partially borrowed input
                // visible to the original callback.
                try { RestoreTemporaryInputs(); } catch (Exception restore) { RecordFault("Восстановление входов АВП: " + restore.Message); }
            }
            InvokeOriginalContained();
        } finally {
            try { RestoreTemporaryInputs(); } catch (Exception error) { RecordFault("Восстановление входов АВП: " + error.Message); }
            try { if (inputScope != null) inputScope.Dispose(); } catch (Exception error) { RecordFault("Восстановление операторских входов: " + error.Message); }
            insideCallback = false;
            GC.KeepAlive(observer); GC.KeepAlive(original);
        }
    }

    void InvokeOriginalContained() {
        try {
            if (GetModuleHandle("Pult1.dll") != pultModule) {
                RecordFault("Исходный ProceedPult больше не загружен"); return;
            }
            original();
        }
        catch (Exception error) { RecordFault("Исходный ProceedPult: " + error.Message); }
    }

    void RestoreTemporaryInputs() {
        // Native code may consume a pulse, or a reentrant physical action may
        // replace it. Restore only our value; preserve any new physical request.
        if (!ownsSelector && !ownsCoast) return;
        GuardRecordGraph();
        Exception failure = null;
        if (ownsCoast) {
            try {
                RequireWritable(IntPtr.Add(pult, 0x26), 1);
                byte value = Marshal.ReadByte(pult, 0x26);
                if (value == assignedCoast) Marshal.WriteByte(pult, 0x26, savedCoast);
                else if (value != savedCoast) Relinquish("Выбег изменён физическим органом управления");
                ownsCoast = false;
            } catch (Exception error) { failure = error; }
        }
        if (ownsSelector) {
            try {
                RequireWritable(IntPtr.Add(pult, 0x69), 4);
                int value = Marshal.ReadInt32(pult, 0x69);
                if (value == assignedSelector) Marshal.WriteInt32(pult, 0x69, savedSelector);
                else if (value != savedSelector) Relinquish("Тяга изменена физическим органом управления");
                ownsSelector = false;
            } catch (Exception error) { if (failure == null) failure = error; }
        }
        if (failure != null) throw failure;
    }

    string ReadinessReason() {
        string external=AdditionalSafetyReason(); if(external.Length!=0)return external;
        if (Marshal.ReadByte(pult, 0x23) != 0 || Marshal.ReadByte(pult, 0x24) != 0) return "Экстренное торможение";
        if (Marshal.ReadByte(loc, 0x99) != 1 || Marshal.ReadByte(loc, 0x9b) != 0 || Marshal.ReadByte(loc, 0x9d) != 0) return "ЭПК не готов";
        if (Marshal.ReadByte(pult, 0xc6) != 0 ||
            (Marshal.ReadByte(loc, 0x89) != 0 && (Marshal.ReadByte(loc, 0x8a) != 0 || Marshal.ReadByte(loc, 0x8b) != 0)))
            return "Команда САУТ";
        if (Marshal.ReadByte(loc, 0x8f) != 1) return "ВЦУ не включён";
        if (Marshal.ReadInt32(pult, 0x81) != 1) return "Реверсор должен быть ВПЕРЕД";
        if (Marshal.ReadByte(pult, 0x34) != 1 || Marshal.ReadByte(pult, 0x38) != 1) return "Тяговые двигатели не включены физическими входами";
        if (Marshal.ReadByte(pult, 0x2c) != 1 || Marshal.ReadByte(tyga, 0x16) != 1) return "Независимое возбуждение не подтверждено";
        if (Marshal.ReadByte(schema, 0x62) == 0 || Marshal.ReadByte(schema, 0x63) == 0 || Marshal.ReadByte(schema, 0x123) == 0)
            return "Питание и БВ секций не подтверждены native-схемой";
        if (Marshal.ReadByte(schema, 0xac) == 0 || Marshal.ReadByte(schema, 0xad) == 0) return "Вентиляторы секций не готовы";
        return "";
    }

    void Relinquish(string reason) {
        bool notify = enabled || !cancellationLatched;
        enabled = false; requestedCommand = 0; cancellationLatched = true;
        cancellationReason = reason;
        if (notify) {
            try { cancellation(reason); }
            catch (Exception error) { if (fault.Length == 0) fault = "Отмена АВП: " + error.Message; }
        }
    }
    void RecordFault(string reason) {
        if (fault.Length == 0) fault = reason;
        Relinquish(reason);
    }
    void ThreadGuard() {
        if (disposed) throw new ObjectDisposedException("TrainerAutovedenieNativeBridge");
        if (GetCurrentThreadId() != thread) throw new Exception("AVP bridge must run on its native owner thread");
    }
    void GuardGraph(bool attached) {
        GuardRecordGraph();
        if (ReadPointer(provider, 8) != (attached ? observerPointer : originalPointer))
            throw new Exception("AVP callback pointer ownership guard");
    }
    void GuardRecordGraph() {
        if (GetModuleHandle("Communication1.dll") != communication || GetModuleHandle("Pult1.dll") != pultModule)
            throw new Exception("AVP module lifetime guard");
        RequireReadable(commOwner, 0x268); RequireReadable(pult, 0xc7); RequireReadable(loc, 0x9e);
        RequireReadable(pultOwner, 0x13c); RequireReadable(tyga, 0x17);
        if (ReadPointer(commOwner, 0) != commVmt || ReadPointer(commOwner, 0x264) != provider || ReadPointer(provider, 0) != providerVmt)
            throw new Exception("AVP provider owner guard");
        if (ReadPointer(pultModule, 0x1a2e60) != globalSlot || ReadPointer(globalSlot, 0) != pultOwner ||
            ReadPointer(pultOwner, 0) != pultOwnerVmt || ReadPointer(pultOwner, 4) != pult || ReadPointer(pultOwner, 0xc) != updater)
            throw new Exception("AVP Pult owner graph guard");
        if (ReadPointer(pult, 4) != loc || ReadPointer(pult, 0x10) != tyga || ReadPointer(pultOwner, 8) != tyga)
            throw new Exception("AVP shared record guard");
    }

    static IntPtr ReadPointer(IntPtr address, int offset) {
        IntPtr target = IntPtr.Add(address, offset); RequireReadable(target, 4); return Marshal.ReadIntPtr(target);
    }
    static bool Accessible(IntPtr address, int count, bool write) {
        MemoryInfo info;
        if (address == IntPtr.Zero || count < 1 || VirtualQuery(address, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero)
            return false;
        ulong start = unchecked((uint)address.ToInt32()), region = unchecked((uint)info.Base.ToInt32());
        uint protection = info.Protect & 0xff;
        if (info.State != 0x1000 || (info.Protect & 0x101) != 0 || start < region || start + (uint)count > region + info.RegionSize.ToUInt32())
            return false;
        if (write) return protection == 0x04 || protection == 0x08 || protection == 0x40 || protection == 0x80;
        return protection == 0x02 || protection == 0x04 || protection == 0x08 || protection == 0x20 || protection == 0x40 || protection == 0x80;
    }
    static void RequireReadable(IntPtr address, int count) {
        if (!Accessible(address, count, false)) throw new Exception("AVP readable data-range guard");
    }
    static void RequireWritable(IntPtr address, int count) {
        if (!Accessible(address, count, true)) throw new Exception("AVP writable data-range guard");
    }
    static IntPtr RequireObjectVmt(IntPtr obj, IntPtr module, string expectedClass) {
        RequireReadable(obj, 4); IntPtr vmt = Marshal.ReadIntPtr(obj);
        RequireReadable(IntPtr.Add(vmt, -56), 60);
        MemoryInfo info;
        if (VirtualQuery(vmt, out info, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) == UIntPtr.Zero || info.AllocationBase != module)
            throw new Exception("AVP object VMT module guard");
        IntPtr name = Marshal.ReadIntPtr(vmt, -56); RequireReadable(name, 1);
        int count = Marshal.ReadByte(name);
        if (count < 1 || count > 150) throw new Exception("AVP VMT class-name length guard");
        RequireReadable(name, count + 1); byte[] bytes = new byte[count]; Marshal.Copy(IntPtr.Add(name, 1), bytes, 0, count);
        foreach (byte value in bytes) if (value < 32 || value > 126) throw new Exception("AVP VMT class-name encoding guard");
        if (expectedClass != null && Encoding.ASCII.GetString(bytes) != expectedClass) throw new Exception("AVP native class guard: " + expectedClass);
        return vmt;
    }
    static void CheckModule(IntPtr module, string name, string expectedSha) {
        if (module == IntPtr.Zero) throw new Exception("AVP module absent: " + name);
        StringBuilder loaded = new StringBuilder(32768);
        uint length = GetModuleFileName(module, loaded, loaded.Capacity);
        if (length == 0 || length >= loaded.Capacity) throw new Exception("AVP loaded module path unavailable");
        string expected = Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,
            @"..\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1", name));
        if (!String.Equals(Path.GetFullPath(loaded.ToString()), expected, StringComparison.OrdinalIgnoreCase))
            throw new Exception("AVP unexpected loaded module path: " + name);
        string actual;
        using (SHA256 hash = SHA256.Create()) using (FileStream file = File.OpenRead(loaded.ToString()))
            actual = BitConverter.ToString(hash.ComputeHash(file)).Replace("-", "");
        if (actual != expectedSha) throw new Exception("AVP loaded module SHA guard: " + name);
    }

    public void Dispose() {
        if (disposed) return;
        ThreadGuard(); enabled = false; requestedCommand = 0;
        if (insideCallback) throw new InvalidOperationException("Detach AVP bridge after ProceedPult returns");
        RestoreTemporaryInputs();
        if (rooted) {
            if (GetModuleHandle("Communication1.dll") != communication || GetModuleHandle("Pult1.dll") != pultModule)
                throw new Exception("AVP refuses detach after native module lifetime changed");
            RequireReadable(provider, 12);
            if (ReadPointer(provider, 0) != providerVmt) throw new Exception("AVP detach provider identity guard");
            IntPtr current = ReadPointer(provider, 8);
            if (current != originalPointer && current != observerPointer)
                throw new Exception("AVP refuses to restore a callback it does not own");
            if (current == observerPointer) { RequireWritable(callbackSlot, 4); Marshal.WriteIntPtr(callbackSlot, originalPointer); }
            if (ReadPointer(provider, 8) != originalPointer) throw new Exception("AVP callback restoration guard");
            lock (ActiveOwners) { ActiveOwners.Remove(this); rooted = false; }
        }
        disposed = true;
        GC.KeepAlive(observer); GC.KeepAlive(original);
        observer = null; observerPointer = IntPtr.Zero;
    }
}
