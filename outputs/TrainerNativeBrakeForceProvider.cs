using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

/// <summary>
/// Uses only the pinned POEZD brake-capacity producer. It never advances native motion.
/// Borrowed input/output arrays are restored before an evaluated capacity is returned.
/// </summary>
internal sealed class TrainerNativeBrakeForceProvider : IDisposable {
    public const string SourceSha256 = "822B6D17CE45F80179A512EE6C70A35D5AE29BF187530BDA5EBD11446CFB3553";
    public const int VehicleCount = 76, NativeCapacity = 150, BufferBytes = NativeCapacity * 4;
    public const int VelocityRva = 0x18f04, BrakeForceRva = 0x1d0ac, PressureRva = 0x1a674, CursorRva = 0x15234;
    const int ProducerRva = 0x1dbc, ImageBytes = 0x33000, PreferredBase = 0x400000;
    static readonly byte[] OriginalBody = Hex("558bec83c4e833c08945f0e9ff0000008b45f099f73d645642008945fc8b4df08b048d24ab4100d90485c05141008b55f0d80c9574a641008b4df08b048d24ab41008b55f08b0c957cad41008d0c498d148dcc514100d80c82dc0de01e4000d95df88b45fcd90485048f410083c4f8dd1c24e89df6000083c4088b4df08b048d7cad4100d80485fc514100dd5de88b55fcd90495048f410083c4f8dd1c24e871f6000083c4088b4df08b048d7cad4100d8048504524100dc7de8d95df48b55f08b0c957cad4100d9048de4514100d9e0d84df8d945f88b45f08b14857cad4100d80495ec514100dec9d945f88b4df08b048d7cad4100d80485f4514100def9d84df48b55f0d91c95acd04100ff45f08b4df03b0d585642000f8cf2feffff8be55dc3");
    static readonly int[] BodyRelocations = {22,35,42,52,62,72,82,91,104,128,135,148,172,179,195,202,220,227,242,249,264,276};
    static readonly byte[] OriginalSetTorm = Hex("558beca1345241008b550889148574a64100ff05345241008b0d345241003b4d0c750733c0a3345241005dc20800");
    static readonly int[] SetTormRelocations = {4,14,20,26,38};
    static readonly object OwnerGate = new object();
    static readonly Dictionary<IntPtr, TrainerNativeBrakeForceProvider> Owners = new Dictionary<IntPtr, TrainerNativeBrakeForceProvider>();
    readonly IntPtr module;
    readonly string expectedPath;
    readonly uint thread;
    readonly Action<int,float> setPressure;
    readonly Func<IntPtr[]> ownedOverlay;
    IntPtr pinned;
    ProducerCall producer;
    Borrowed pending;
    bool evaluating, closing, disposed, lastRestoreVerified;
    long evaluationCount, successfulEvaluations;
    string fault = "";

    [UnmanagedFunctionPointer(CallingConvention.Cdecl)] delegate void ProducerCall();
    sealed class Overlay { public IntPtr Cave, Source; }
    sealed class Borrowed {
        public byte[] V, B, P, ExpectedV, ExpectedB, ExpectedP;
        public int Cursor, ExpectedCursor;
        public bool VDone, BDone, PDone, CursorDone;
        public Overlay Overlay;
    }

    public TrainerNativeBrakeForceProvider(IntPtr nativeModule, string fullPath, Action<int,float> pressureSetter, Func<IntPtr[]> overlayOwner) {
        if (IntPtr.Size != 4) throw new InvalidOperationException("Native brake capacity provider requires x86.");
        if (nativeModule == IntPtr.Zero || String.IsNullOrWhiteSpace(fullPath) || pressureSetter == null || overlayOwner == null)
            throw new ArgumentException("A loaded module, independent full path, native pressure setter and overlay owner are required.");
        module = nativeModule; expectedPath = Path.GetFullPath(fullPath); thread = GetCurrentThreadId();
        setPressure = pressureSetter; ownedOverlay = overlayOwner;
        lock (OwnerGate) {
            if (Owners.ContainsKey(module)) throw new InvalidOperationException("A brake capacity provider already owns this module.");
            Owners.Add(module, this);
        }
        try {
            IntPtr handle;
            bool retained=GetModuleHandleEx(4, module, out handle);
            if(retained) pinned=handle;
            if (!retained || handle != module) throw WinError("Cannot retain brake capacity module");
            CheckModule();
            using (var sha = SHA256.Create()) using (var file = File.OpenRead(expectedPath))
                if (!String.Equals(BitConverter.ToString(sha.ComputeHash(file)).Replace("-", ""), SourceSha256, StringComparison.Ordinal))
                    throw new InvalidOperationException("Brake capacity source SHA guard failed.");
            CheckImage(); ValidateCore();
            producer = (ProducerCall)Marshal.GetDelegateForFunctionPointer(At(ProducerRva), typeof(ProducerCall));
        } catch (Exception error) {
            closing = true; fault = error.Message;
            try { ReleasePin(); RemoveOwner(); disposed = true; }
            catch (Exception cleanup) { throw new InvalidOperationException("Brake capacity initialization and pin cleanup failed; owner retained.", new AggregateException(error, cleanup)); }
            throw;
        }
    }

    public static bool HasRetainedOwners { get { lock (OwnerGate) return Owners.Count != 0; } }
    public static void DisposeRetainedOwners() {
        TrainerNativeBrakeForceProvider[] all;
        lock (OwnerGate) { all = new TrainerNativeBrakeForceProvider[Owners.Count]; Owners.Values.CopyTo(all, 0); }
        var errors = new List<Exception>();
        foreach (var owner in all) try { owner.Dispose(); } catch (Exception error) { errors.Add(error); }
        if (errors.Count != 0) throw new AggregateException("Brake capacity owners remain retained.", errors);
    }
    public string Fault { get { return fault; } }
    public void Validate() { CheckReady(); ValidateCore(); }
    public Dictionary<string,object> Snapshot() {
        CheckThread();
        return new Dictionary<string,object> {
            {"EvaluationCount",evaluationCount},{"SuccessfulEvaluations",successfulEvaluations},
            {"LastRestoreVerified",lastRestoreVerified},{"PendingRestore",pending!=null},
            {"Evaluating",evaluating},{"Disposed",disposed},{"Fault",fault}
        };
    }

    public double GetSpeedNumerator(int index) { return SpeedCoefficient(index, true); }
    public double GetSpeedDenominator(int index) { return SpeedCoefficient(index, false); }
    double SpeedCoefficient(int index, bool numerator) {
        CheckReady(); CheckModule(); CheckCounts();
        if (index < 0 || index >= VehicleCount) throw new ArgumentOutOfRangeException("index");
        CheckVehicle(index);
        int material = Marshal.ReadInt32(At(0x1ad7c), index*4);
        int rva = (numerator ? 0x151fc : 0x15204) + material*4;
        float expected = numerator ? (material==0 ? 41.7f : 27.78f) : (material==0 ? 20.85f : 5.56f);
        RequireRange(At(rva), 4, false, false, module); GuardBytes(At(rva), BitConverter.GetBytes(expected), "Speed coefficient");
        return expected;
    }

    /// <summary>Returns positive brake capacities in newtons. Input pressure is native gauge kgf/cm², velocity m/s.</summary>
    public double[] Evaluate(float[] pressures, double[] velocities) { using(TrainerManagedFloatingScope.Enter()) return EvaluateCore(pressures,velocities); }
    double[] EvaluateCore(float[] pressures, double[] velocities) {
        if (pressures == null || velocities == null || pressures.Length != VehicleCount || velocities.Length != VehicleCount)
            throw new ArgumentException("Exactly76 pressures and velocities are required.");
        var stagedV = new byte[VehicleCount*4];
        for (int i=0;i<VehicleCount;i++) {
            uint p = Bits(pressures[i]);
            if (!Finite(p) || (p&0x80000000U)!=0 && (p&0x7fffffffU)!=0) throw new ArgumentException("Invalid brake pressure at"+i);
            ulong v = BitConverter.ToUInt64(BitConverter.GetBytes(velocities[i]),0);
            if ((v&0x7ff0000000000000UL)==0x7ff0000000000000UL || Math.Abs(velocities[i])>Single.MaxValue)
                throw new ArgumentException("Invalid brake velocity at"+i);
            float single = (float)velocities[i];
            if (!Finite(Bits(single))) throw new ArgumentException("Velocity cannot be represented as native Single.");
            Buffer.BlockCopy(BitConverter.GetBytes(single),0,stagedV,i*4,4);
        }
        CheckReady();
        if (evaluating) throw new InvalidOperationException("Nested brake capacity evaluation.");
        evaluating = true; lastRestoreVerified = false;
        Exception operationError = null, restoreError = null; double[] result = null;
        try {
            Overlay overlay = ValidateCore();
            if (evaluationCount == Int64.MaxValue) throw new InvalidOperationException("Brake capacity sequence exhausted.");
            evaluationCount++;
            var state = new Borrowed { V=Read(At(VelocityRva),BufferBytes),B=Read(At(BrakeForceRva),BufferBytes),P=Read(At(PressureRva),BufferBytes),Cursor=Marshal.ReadInt32(At(CursorRva)),Overlay=overlay };
            state.ExpectedV=(byte[])state.V.Clone(); state.ExpectedB=(byte[])state.B.Clone(); state.ExpectedP=(byte[])state.P.Clone(); state.ExpectedCursor=state.Cursor;
            if (state.Cursor != 0) throw new InvalidOperationException("SetTorm cursor must be0 before evaluation.");
            pending=state;
            for (int i=0;i<VehicleCount;i++) {
                // The only host callback is the original ordered SetTorm API. Capture
                // its owned input writes even if the callback reports a failure.
                try { TrainerManagedFloatingScope.RunNative(delegate { setPressure(i,pressures[i]); }); }
                finally { CapturePressureWrites(state,i,pressures[i]); }
                if (state.ExpectedCursor != (i+1)%VehicleCount || ReadBits(state.ExpectedP,i*4)!=Bits(pressures[i]))
                    throw new InvalidOperationException("Native SetTorm input/cursor contract failed at"+i);
                var expectedP=(byte[])state.P.Clone();
                for (int j=0;j<=i;j++) Buffer.BlockCopy(BitConverter.GetBytes(pressures[j]),0,expectedP,j*4,4);
                if (!Equal(state.ExpectedP,expectedP)) throw new InvalidOperationException("SetTorm changed an unrelated pressure slot.");
            }
            Buffer.BlockCopy(stagedV,0,state.ExpectedV,0,stagedV.Length);
            Write(At(VelocityRva),state.ExpectedV);
            GuardBytes(At(VelocityRva),state.ExpectedV,"Staged velocities");
            SameOverlay(state.Overlay,ValidateCore());
            try { TrainerManagedFloatingScope.RunNative(delegate { producer(); }); }
            finally {
                // B is an output of this exact producer; restoration expects the
                // complete measured output, not the pre-call snapshot.
                CheckModule(); RequireRange(At(BrakeForceRva),BufferBytes,true,false,module);
                // Only configured output elements belong to the invoked producer.
                // Padding remains guarded by its original snapshot; never adopt
                // a foreign write beyond the76-element native loop.
                byte[] produced=Read(At(BrakeForceRva),VehicleCount*4);
                AcceptOwnedBrakeOutputs(state,produced);
            }
            SameOverlay(state.Overlay,ValidateCore());
            GuardBytes(At(BrakeForceRva),state.ExpectedB,"Producer output and untouched brake padding");
            GuardBytes(At(VelocityRva),state.ExpectedV,"Producer must not change velocities");
            GuardBytes(At(PressureRva),state.ExpectedP,"Producer must not change pressures");
            if (Marshal.ReadInt32(At(CursorRva))!=0) throw new InvalidOperationException("Producer changed pressure cursor.");
            result=new double[VehicleCount];
            for(int i=0;i<VehicleCount;i++) {
                uint b=ReadBits(state.ExpectedB,i*4);
                if(!Finite(b) || (b&0x80000000U)==0 && (b&0x7fffffffU)!=0) throw new InvalidOperationException("Native brake capacity is nonfinite or positive at"+i);
                result[i]=-(double)BitConverter.ToSingle(state.ExpectedB,i*4)*10000.0;
            }
        } catch(Exception error) { operationError=error; }
        finally {
            try { RestorePending(); lastRestoreVerified=pending==null; }
            catch(Exception error) { restoreError=error; }
            evaluating=false;
        }
        if(operationError!=null || restoreError!=null) {
            Exception error=operationError!=null && restoreError!=null ? new AggregateException(operationError,restoreError) : operationError??restoreError;
            fault=error.Message; throw new InvalidOperationException("Native brake capacity evaluation failed; no capacity is published.",error);
        }
        successfulEvaluations++; return result;
    }

    void CapturePressureWrites(Borrowed state,int index,float requested) {
        CheckModule(); RequireRange(At(PressureRva),BufferBytes,true,false,module); RequireRange(At(CursorRva),4,true,false,module);
        // A failed callback may have executed none or a prefix of this one
        // SetTorm operation. Only its exact slot and next cursor are ours.
        // Other slots (especially the unused tail) keep their prior expected
        // values, so guarded cleanup refuses to overwrite a foreign change.
        uint actual=unchecked((uint)Marshal.ReadInt32(At(PressureRva),index*4));
        int cursor=Marshal.ReadInt32(At(CursorRva));
        AcceptOwnedPressureWrite(state,index,requested,actual,cursor);
    }
    static void AcceptOwnedPressureWrite(Borrowed state,int index,float requested,uint actual,int cursor) {
        uint before=ReadBits(state.ExpectedP,index*4),after=Bits(requested);
        int next=(index+1)%VehicleCount;
        bool knownPressure=actual==before || actual==after;
        bool knownCursor=cursor==state.ExpectedCursor || cursor==next;
        if(knownPressure)Buffer.BlockCopy(BitConverter.GetBytes(actual),0,state.ExpectedP,index*4,4);
        if(knownCursor)state.ExpectedCursor=cursor;
        // Retain each independently proved write for cleanup even when the other
        // field contains a foreign value which must not be overwritten.
        if(!knownPressure)throw new InvalidOperationException("Pressure callback wrote an unexpected value to its owned slot.");
        if(!knownCursor)throw new InvalidOperationException("Pressure callback moved cursor outside its owned operation.");
    }
    static void AcceptOwnedBrakeOutputs(Borrowed state,byte[] produced) {
        if(produced==null || produced.Length!=VehicleCount*4)throw new InvalidOperationException("Brake producer ownership is exactly76 elements.");
        Buffer.BlockCopy(produced,0,state.ExpectedB,0,produced.Length);
    }
    void RestorePending() {
        var state=pending; if(state==null)return;
        CheckThread(); CheckModule();
        var errors=new List<Exception>();
        TryRestore(state.V, ref state.ExpectedV, VelocityRva, ref state.VDone, errors);
        TryRestore(state.B, ref state.ExpectedB, BrakeForceRva, ref state.BDone, errors);
        TryRestore(state.P, ref state.ExpectedP, PressureRva, ref state.PDone, errors);
        if(!state.CursorDone) try {
            RequireRange(At(CursorRva),4,true,false,module);
            if(Marshal.ReadInt32(At(CursorRva))!=state.ExpectedCursor) throw new InvalidOperationException("Pressure cursor changed outside its borrow scope.");
            Marshal.WriteInt32(At(CursorRva),state.Cursor);
            state.ExpectedCursor=state.Cursor;
            if(Marshal.ReadInt32(At(CursorRva))!=state.Cursor)throw new InvalidOperationException("Pressure cursor restoration verification failed.");
            state.CursorDone=true;
        } catch(Exception error) {errors.Add(error);}
        if(errors.Count!=0)throw new AggregateException("Borrowed brake buffers remain retained for guarded cleanup.",errors);
        pending=null;
    }
    void TryRestore(byte[] original,ref byte[] expected,int rva,ref bool done,List<Exception> errors) {
        if(done)return;
        try {
            RequireRange(At(rva),BufferBytes,true,false,module); GuardBytes(At(rva),expected,"Borrowed buffer ownership at"+rva.ToString("X"));
            Write(At(rva),original); expected=(byte[])original.Clone();
            GuardBytes(At(rva),original,"Restored buffer at"+rva.ToString("X")); done=true;
        } catch(Exception error) {errors.Add(error);}
    }

    Overlay ValidateCore() {
        CheckModule(); CheckCounts();
        foreach(int rva in new[]{VelocityRva,BrakeForceRva,PressureRva})RequireRange(At(rva),BufferBytes,true,false,module);
        RequireRange(At(CursorRva),4,true,false,module);
        for(int i=0;i<VehicleCount;i++)CheckVehicle(i);
        Overlay overlay=ReadOverlay();
        byte[] expected=Relocate(OriginalBody,BodyRelocations,module.ToInt32());
        if(overlay.Cave!=IntPtr.Zero) {
            RequireRange(overlay.Cave,27,false,true,IntPtr.Zero); RequireRange(overlay.Source,8,false,false,IntPtr.Zero);
            byte[] cave=BuildOverlayCave(overlay.Cave,overlay.Source,module);
            GuardBytes(overlay.Cave,cave,"Owned per-pad cave");
            byte[] source=Read(overlay.Source,8); ulong bits=BitConverter.ToUInt64(source,0);
            if((bits&0x7ff0000000000000UL)==0x7ff0000000000000UL || (bits&0x8000000000000000UL)==0 && (bits&0x7fffffffffffffffUL)!=0)
                throw new InvalidOperationException("Owned per-pad source must be finite and nonpositive.");
            Buffer.BlockCopy(Jump(At(0x1ebb),overlay.Cave,6),0,expected,0x1ebb-ProducerRva,6);
        }
        RequireRange(At(ProducerRva),expected.Length,false,true,module); GuardBytes(At(ProducerRva),expected,"Complete relocated brake producer");
        RequireRange(At(0x114d0),13,false,true,module); GuardBytes(At(0x114d0),Hex("558BEC9B80650F7FDD45085DC3"),"Native fabs helper");
        RequireRange(At(0x229e),OriginalSetTorm.Length,false,true,module); GuardBytes(At(0x229e),Relocate(OriginalSetTorm,SetTormRelocations,module.ToInt32()),"Native SetTorm");
        CheckTables(overlay.Cave!=IntPtr.Zero); return overlay;
    }
    void CheckVehicle(int index) {
        foreach(int rva in new[]{0x1ab24,0x1ad7c,0x19abc,0x19f6c})RequireRange(At(rva+index*4),4,false,false,module);
        if(Marshal.ReadInt32(At(0x1ab24),index*4)!=(index==0?2:1) || Marshal.ReadInt32(At(0x1ad7c),index*4)!=(index==0?1:0) ||
            Marshal.ReadInt32(At(0x19abc),index*4)!=unchecked((int)Bits(index==0?20f:8f)) || Marshal.ReadInt32(At(0x19f6c),index*4)!=unchecked((int)Bits(index==0?20f:8f)))
            throw new InvalidOperationException("Brake capacity configuration differs from76-unit200t+75x80t FTk1/FTk0 profile at"+index);
    }
    void CheckTables(bool perPad) {
        float[] table={994f,994f,perPad?(float)(Math.PI*20.3*20.3/4):1257f,5.79f,5.79f,perPad?8.8f:8.245f,9.03f,9.03f,perPad?8.8f:12.915f,.055f,.031f,20f,6.25f,5f,1.92f,41.7f,27.78f,20.85f,5.56f};
        var bytes=new byte[table.Length*4];Buffer.BlockCopy(table,0,bytes,0,bytes.Length);
        RequireRange(At(0x151c0),bytes.Length,false,false,module);GuardBytes(At(0x151c0),bytes,"Native brake geometry/material tables");
        RequireRange(At(0x1ee0),8,false,false,module);GuardBytes(At(0x1ee0),BitConverter.GetBytes(.00085),"Native pressure conversion factor");
    }
    Overlay ReadOverlay() {
        IntPtr[] pair=ownedOverlay();
        if(pair==null || pair.Length!=2 || (pair[0]==IntPtr.Zero)!=(pair[1]==IntPtr.Zero))throw new InvalidOperationException("Per-pad owner must provide exactly cave/source or two zeros.");
        return new Overlay {Cave=pair[0],Source=pair[1]};
    }
    static void SameOverlay(Overlay first,Overlay second) {if(first.Cave!=second.Cave || first.Source!=second.Source)throw new InvalidOperationException("Per-pad owner changed during brake evaluation.");}
    void CheckCounts() {
        RequireRange(At(0x25658),16,false,false,module);
        if(Marshal.ReadInt32(At(0x25658))!=VehicleCount || Marshal.ReadInt32(At(0x2565c))!=VehicleCount || Marshal.ReadInt32(At(0x25664))!=1)
            throw new InvalidOperationException("Brake capacity requires76 elements/76 groups/one element per group.");
    }
    void CheckReady() {
        CheckThread();if(disposed || closing || pinned==IntPtr.Zero)throw new ObjectDisposedException("TrainerNativeBrakeForceProvider");
        if(pending!=null || fault.Length!=0)throw new InvalidOperationException("Brake capacity provider is faulted: "+fault);
    }
    void CheckThread() {if(IntPtr.Size!=4 || GetCurrentThreadId()!=thread)throw new InvalidOperationException("Brake capacity owner thread/x86 guard failed.");}
    void CheckModule() {
        CheckThread();
        if(pinned!=module || GetModuleHandle("POEZD.DLL")!=module)throw new InvalidOperationException("Pinned brake capacity module lifetime changed.");
        var name=new StringBuilder(32768);uint length=GetModuleFileName(module,name,name.Capacity);
        if(length==0 || length>=name.Capacity || !String.Equals(Path.GetFullPath(name.ToString()),expectedPath,StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException("Loaded brake capacity path differs from the independent expected path.");
        RequireRange(module,0x40,false,false,module);
    }
    void CheckImage() {
        if(Marshal.ReadInt16(module)!=0x5a4d)throw new InvalidOperationException("Brake capacity DOS header guard failed.");
        int pe=Marshal.ReadInt32(module,0x3c);
        if(pe<0x40 || pe>0x1000)throw new InvalidOperationException("Brake capacity PE header offset guard failed.");
        RequireRange(At(pe),0x60,false,false,module);
        if(Marshal.ReadInt32(At(pe))!=0x4550 || Marshal.ReadInt16(At(pe+4))!=0x14c || Marshal.ReadInt16(At(pe+24))!=0x10b || Marshal.ReadInt32(At(pe+80))!=ImageBytes)
            throw new InvalidOperationException("Brake capacity PE32 image guard failed.");
    }
    public void Dispose() {
        CheckThread();if(disposed)return;if(evaluating)throw new InvalidOperationException("Cannot close brake capacity provider during evaluation.");
        closing=true;
        try {RestorePending();ReleasePin();RemoveOwner();disposed=true;producer=null;}
        catch(Exception error) {fault=error.Message;throw;}
    }
    void ReleasePin() {if(pinned!=IntPtr.Zero){if(!FreeLibrary(pinned))throw WinError("Cannot release brake capacity module pin");pinned=IntPtr.Zero;}}
    void RemoveOwner() {lock(OwnerGate){TrainerNativeBrakeForceProvider owner;if(Owners.TryGetValue(module,out owner)&&Object.ReferenceEquals(owner,this))Owners.Remove(module);}}

    static byte[] Relocate(byte[] original,int[] offsets,int actualBase) {
        var result=(byte[])original.Clone();int delta=unchecked(actualBase-PreferredBase);
        foreach(int offset in offsets)Buffer.BlockCopy(BitConverter.GetBytes(unchecked(BitConverter.ToInt32(result,offset)+delta)),0,result,offset,4);
        return result;
    }
    static byte[] BuildOverlayCave(IntPtr cave,IntPtr source,IntPtr nativeModule) {
        var b=new List<byte>(Hex("9C837DF0007508DDD8DD05"));b.AddRange(BitConverter.GetBytes(source.ToInt32()));b.Add(0x9d);
        b.AddRange(Hex("D84DF48B55F0"));b.Add(0xe9);b.AddRange(BitConverter.GetBytes(unchecked(nativeModule.ToInt32()+0x1ec1-cave.ToInt32()-27)));return b.ToArray();
    }
    static byte[] Jump(IntPtr site,IntPtr target,int length) {var b=new byte[length];b[0]=0xe9;Buffer.BlockCopy(BitConverter.GetBytes(unchecked(target.ToInt32()-site.ToInt32()-5)),0,b,1,4);for(int i=5;i<length;i++)b[i]=0x90;return b;}
    static byte[] Hex(string value) {var b=new byte[value.Length/2];for(int i=0;i<b.Length;i++)b[i]=Convert.ToByte(value.Substring(i*2,2),16);return b;}
    static uint Bits(float value) {return BitConverter.ToUInt32(BitConverter.GetBytes(value),0);}
    static bool Finite(uint bits) {return (bits&0x7f800000U)!=0x7f800000U;}
    static uint ReadBits(byte[] data,int offset) {return BitConverter.ToUInt32(data,offset);}
    IntPtr At(int rva) {return IntPtr.Add(module,rva);}
    static byte[] Read(IntPtr address,int count) {var bytes=new byte[count];Marshal.Copy(address,bytes,0,count);return bytes;}
    static void Write(IntPtr address,byte[] bytes) {Marshal.Copy(bytes,0,address,bytes.Length);}
    static bool Equal(byte[] a,byte[] b) {if(a==null||b==null||a.Length!=b.Length)return false;for(int i=0;i<a.Length;i++)if(a[i]!=b[i])return false;return true;}
    static void GuardBytes(IntPtr address,byte[] bytes,string reason) {if(bytes==null||!Equal(Read(address,bytes.Length),bytes))throw new InvalidOperationException(reason+" byte guard failed.");}
    static void RequireRange(IntPtr address,int length,bool writable,bool executable,IntPtr allocation) {
        if(address==IntPtr.Zero || length<=0)throw new InvalidOperationException("Invalid brake capacity memory range.");
        ulong start=unchecked((uint)address.ToInt32()),end=start+(uint)length;
        if(end>0x100000000UL)throw new InvalidOperationException("Brake capacity memory range wraps.");
        while(start<end) {
            MemoryInformation info;
            if(VirtualQuery(new IntPtr(unchecked((int)(uint)start)),out info,new UIntPtr((uint)Marshal.SizeOf(typeof(MemoryInformation))))==UIntPtr.Zero)throw WinError("Cannot inspect brake capacity memory");
            uint protection=info.Protect&0xff;
            bool read=protection==2||protection==4||protection==8||protection==0x20||protection==0x40||protection==0x80;
            bool write=protection==4||protection==8||protection==0x40||protection==0x80;
            bool execute=protection==0x20||protection==0x40||protection==0x80;
            if(info.State!=0x1000 || (info.Protect&0x100)!=0 || !read || writable&&!write || executable&&!execute || allocation!=IntPtr.Zero&&info.AllocationBase!=allocation)
                throw new InvalidOperationException("Brake capacity memory protection/allocation guard failed.");
            ulong next=unchecked((uint)info.BaseAddress.ToInt32())+info.RegionSize.ToUInt64();
            if(next<=start)throw new InvalidOperationException("Brake capacity memory region does not advance.");start=Math.Min(next,end);
        }
    }
    static Exception WinError(string text) {return new Win32Exception(Marshal.GetLastWin32Error(),text);}
    [StructLayout(LayoutKind.Sequential)] struct MemoryInformation {public IntPtr BaseAddress,AllocationBase;public uint AllocationProtect;public UIntPtr RegionSize;public uint State,Protect,Type;}
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll",EntryPoint="GetModuleHandleExW",SetLastError=true)] [return:MarshalAs(UnmanagedType.Bool)] static extern bool GetModuleHandleEx(uint flags,IntPtr address,out IntPtr handle);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode,SetLastError=true)] static extern uint GetModuleFileName(IntPtr module,StringBuilder name,int count);
    [DllImport("kernel32.dll",SetLastError=true)] [return:MarshalAs(UnmanagedType.Bool)] static extern bool FreeLibrary(IntPtr module);
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll",SetLastError=true)] static extern UIntPtr VirtualQuery(IntPtr address,out MemoryInformation information,UIntPtr size);
}
