using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

// Candidate-only pneumatic bridge. A committed mechanical break disconnects
// the two native sleeve INPUTS. Cock positions and all pressures remain actual.
partial class TrainerIntegrationHost {
    const int SeparationFrontCock=0x20,SeparationRearCock=0x21,SeparationFrontSleeve=0x22,SeparationRearSleeve=0x23;
    const string SeparationCommunicationSha="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F";
    const string SeparationTormSha="F871640C5B7699901E3AFBF755BB2B797B659524C29BAA046B311CA6ECC875B9";
    readonly Dictionary<int,NativeSeparationBoundary> consistSeparationBoundaries=new Dictionary<int,NativeSeparationBoundary>();
    uint consistSeparationThread;
    IntPtr consistSeparationCommunication,consistSeparationTorm;
    string consistSeparationFault="";
    sealed class NativeSeparationBoundary {
        internal int LinkIndex;
        internal IntPtr Left,Right;
        internal byte OriginalLeftSleeve,OriginalRightSleeve,ExpectedLeftSleeve,ExpectedRightSleeve;
        internal byte CockLeftAtBreak,CockRightAtBreak;
        internal bool Applied;
    }

    partial void SupportsNativeConsistSeparation(ref bool supported){supported=true;}
    partial void ApplyNativeConsistSeparation(int linkIndex,ref bool applied){
        applied=false;CheckNativeSeparationReady();ValidateNativeSeparationGraph();ValidateNativeSeparationEntries();
        if(linkIndex<0||linkIndex>=75)throw new ArgumentOutOfRangeException("linkIndex");
        NativeSeparationBoundary existing;
        if(consistSeparationBoundaries.TryGetValue(linkIndex,out existing)){
            if(!existing.Applied)throw new InvalidOperationException("A pneumatic separation remains retained after failure.");
            applied=true;return;
        }
        // Mechanical link0 joins the complete two-section locomotive to wagon1.
        // Air node1 is its second BVR; cars[0].rear joins the two BVRs internally.
        IntPtr left=linkIndex==0?secondBvr:cars[linkIndex],right=cars[linkIndex+1];
        GuardSeparationEnd(left);GuardSeparationEnd(right);
        var entry=new NativeSeparationBoundary {LinkIndex=linkIndex,Left=left,Right=right,
            OriginalLeftSleeve=Marshal.ReadByte(left,SeparationRearSleeve),OriginalRightSleeve=Marshal.ReadByte(right,SeparationFrontSleeve),
            CockLeftAtBreak=Marshal.ReadByte(left,SeparationRearCock),CockRightAtBreak=Marshal.ReadByte(right,SeparationFrontCock)};
        entry.ExpectedLeftSleeve=entry.OriginalLeftSleeve;entry.ExpectedRightSleeve=entry.OriginalRightSleeve;
        // Both endpoints and their original inputs are staged before any write.
        // Retain the entry first so even partial failure has an explicit owner.
        consistSeparationBoundaries.Add(linkIndex,entry);
        try {
            WriteSeparationSleeve(entry.Left,SeparationRearSleeve,ref entry.ExpectedLeftSleeve,0);
            WriteSeparationSleeve(entry.Right,SeparationFrontSleeve,ref entry.ExpectedRightSleeve,0);
            GuardNativeSeparationEntry(entry);entry.Applied=true;applied=true;
        }catch(Exception operation){
            try {RestoreNativeSeparationEntry(entry);consistSeparationBoundaries.Remove(linkIndex);}
            catch(Exception rollback){consistSeparationFault="Pneumatic separation rollback incomplete; owned inputs retained.";throw new InvalidOperationException(consistSeparationFault,new AggregateException(operation,rollback));}
            throw;
        }
    }
    partial void ValidateNativeConsistSeparation(){
        if(consistSeparationBoundaries.Count==0)return;
        CheckNativeSeparationThread();ValidateNativeSeparationGraph();ValidateNativeSeparationEntries();
        if(consistSeparationFault.Length!=0)throw new InvalidOperationException(consistSeparationFault);
    }
    // Explicit scenario repair or host shutdown only. Ordinary movement/speed
    // reset must not call this function and silently reconnect a broken train.
    partial void RestoreNativeConsistSeparation(){
        if(consistSeparationBoundaries.Count==0){consistSeparationFault="";return;}
        CheckNativeSeparationThread();ValidateNativeSeparationGraph();
        // Preflight every owned sleeve before restoring any boundary. Cock bits
        // are not owned: a legitimate operator cock change is preserved.
        ValidateNativeSeparationEntries();
        var entries=new List<NativeSeparationBoundary>(consistSeparationBoundaries.Values);
        foreach(var entry in entries){RestoreNativeSeparationEntry(entry);consistSeparationBoundaries.Remove(entry.LinkIndex);}
        consistSeparationFault="";
    }
    Dictionary<string,object> NativeConsistSeparationSnapshot(){
        if(consistSeparationBoundaries.Count!=0){CheckNativeSeparationThread();ValidateNativeSeparationGraph();}
        var entries=new List<object>();
        foreach(var entry in consistSeparationBoundaries.Values){GuardSeparationEnd(entry.Left);GuardSeparationEnd(entry.Right);
            entries.Add(new Dictionary<string,object>{{"link_index",entry.LinkIndex},{"left_vehicle",entry.LinkIndex},{"right_vehicle",entry.LinkIndex+1},
                {"left_is_second_locomotive_bvr",entry.LinkIndex==0},{"applied",entry.Applied},
                {"original_sleeves",new[]{entry.OriginalLeftSleeve,entry.OriginalRightSleeve}},
                {"expected_sleeves",new[]{entry.ExpectedLeftSleeve,entry.ExpectedRightSleeve}},
                {"actual_sleeves",new[]{Marshal.ReadByte(entry.Left,SeparationRearSleeve),Marshal.ReadByte(entry.Right,SeparationFrontSleeve)}},
                {"cocks_at_break",new[]{entry.CockLeftAtBreak,entry.CockRightAtBreak}},
                {"actual_cocks",new[]{Marshal.ReadByte(entry.Left,SeparationRearCock),Marshal.ReadByte(entry.Right,SeparationFrontCock)}}});
        }
        return new Dictionary<string,object>{{"boundaries",entries},{"owned_boundary_count",consistSeparationBoundaries.Count},{"fault",consistSeparationFault},
            {"pressure_outputs_written",false},{"cock_inputs_written",false},{"native_code_patched",false},
            {"scope","Actual native sleeve inputs only; open cock ends vent through the existing native pneumatic cycle. No forced pressure, KM, brake force or automatic repair."}};
    }

    void CheckNativeSeparationReady(){
        if(IntPtr.Size!=4||!physicsReady||pneumoStep==null)throw new InvalidOperationException("Pneumatic separation requires the initialized candidate x86 train.");
        uint thread=SeparationGetCurrentThreadId();
        if(consistSeparationThread==0){
            IntPtr communication=GuardSeparationModule("Communication1.dll",SeparationCommunicationSha);
            IntPtr torm=GuardSeparationModule("TormModel.dll",SeparationTormSha);
            // Read-only live Delphi RTTI guards agree with the pinned source PE:
            // TVagonParam243 -> VSend37 -> State13 -> KKS2 / RC2.
            GuardSeparationRecord(communication,0x1955fc,"TVagonParam",243);
            GuardSeparationRecord(communication,0x1954b8,"TVagonSend",37);
            GuardSeparationRecord(communication,0x1953ac,"TStateVagon",13);
            GuardSeparationRecord(communication,0x195168,"TKonKranState",2);
            GuardSeparationRecord(communication,0x1951b0,"TRukavConnect",2);
            consistSeparationCommunication=communication;consistSeparationTorm=torm;consistSeparationThread=thread;
        }
        CheckNativeSeparationThread();
        if(consistSeparationFault.Length!=0)throw new InvalidOperationException(consistSeparationFault);
        if(GetModuleHandle("Communication1.dll")!=consistSeparationCommunication||GetModuleHandle("TormModel.dll")!=consistSeparationTorm)
            throw new InvalidOperationException("Pneumatic separation native module identity changed.");
        if(GetProcAddress(consistSeparationTorm,"ProceedTormClass")!=Marshal.GetFunctionPointerForDelegate(pneumoStep))
            throw new InvalidOperationException("Pneumatic separation producer does not match the initialized native export.");
    }
    void CheckNativeSeparationThread(){if(IntPtr.Size!=4||consistSeparationThread==0||SeparationGetCurrentThreadId()!=consistSeparationThread)throw new InvalidOperationException("Pneumatic separation owner thread/x86 guard failed.");}
    void ValidateNativeSeparationGraph(){
        if(cars==null||cars.Length!=76||secondBvr==IntPtr.Zero||trainData==IntPtr.Zero)throw new InvalidOperationException("Pneumatic separation requires76 mechanical and77 air records.");
        GuardSeparationMemory(trainData,70,false);
        if(Marshal.ReadInt32(trainData,0)!=77)throw new InvalidOperationException("Pneumatic separation air count changed.");
        IntPtr array=Marshal.ReadIntPtr(trainData,0x42);GuardSeparationMemory(array,77*8,false);var unique=new HashSet<IntPtr>();
        for(int i=0;i<77;i++){
            IntPtr expected=i==0?cars[0]:i==1?secondBvr:cars[i-1],actual=Marshal.ReadIntPtr(array,i*8+4);
            if(actual==IntPtr.Zero||actual!=expected||!unique.Add(actual)||Marshal.ReadIntPtr(array,i*8)!=(i==0?mem["TLocParam"]:IntPtr.Zero))
                throw new InvalidOperationException("Pneumatic separation air/mechanical graph mismatch at"+i);
        }
    }
    void ValidateNativeSeparationEntries(){foreach(var entry in consistSeparationBoundaries.Values)GuardNativeSeparationEntry(entry);}
    void GuardNativeSeparationEntry(NativeSeparationBoundary entry){
        if(entry.LinkIndex<0||entry.LinkIndex>=75||entry.Left!=(entry.LinkIndex==0?secondBvr:cars[entry.LinkIndex])||entry.Right!=cars[entry.LinkIndex+1])
            throw new InvalidOperationException("Owned pneumatic separation endpoints changed.");
        GuardSeparationEnd(entry.Left);GuardSeparationEnd(entry.Right);
        if(Marshal.ReadByte(entry.Left,SeparationRearSleeve)!=entry.ExpectedLeftSleeve||Marshal.ReadByte(entry.Right,SeparationFrontSleeve)!=entry.ExpectedRightSleeve)
            throw new InvalidOperationException("A separated sleeve changed outside its owned scope; restoration refused.");
        if(entry.Applied&&(entry.ExpectedLeftSleeve!=0||entry.ExpectedRightSleeve!=0))throw new InvalidOperationException("Applied separation is not disconnected at both ends.");
    }
    static void GuardSeparationEnd(IntPtr record){GuardSeparationMemory(record,0x24,true);for(int offset=0x20;offset<=0x23;offset++)if(Marshal.ReadByte(record,offset)>1)throw new InvalidOperationException("Cock/sleeve native byte domain guard failed.");}
    static void WriteSeparationSleeve(IntPtr record,int offset,ref byte expected,byte value){
        GuardSeparationEnd(record);byte before=expected;
        if(Marshal.ReadByte(record,offset)!=before)throw new InvalidOperationException("Pneumatic sleeve changed before its owned write.");
        try{Marshal.WriteByte(record,offset,value);}
        finally{byte actual=Marshal.ReadByte(record,offset);if(actual==before||actual==value)expected=actual;else throw new InvalidOperationException("Pneumatic sleeve write has an unknown owner/value.");}
        if(expected!=value)throw new InvalidOperationException("Pneumatic sleeve write verification failed.");
    }
    void RestoreNativeSeparationEntry(NativeSeparationBoundary entry){
        GuardNativeSeparationEntry(entry);
        entry.Applied=false;
        WriteSeparationSleeve(entry.Left,SeparationRearSleeve,ref entry.ExpectedLeftSleeve,entry.OriginalLeftSleeve);
        // Mark no longer applied before restoring the other side so a guarded
        // retry can distinguish an intentional partial restore from a live split.
        WriteSeparationSleeve(entry.Right,SeparationFrontSleeve,ref entry.ExpectedRightSleeve,entry.OriginalRightSleeve);
    }
    IntPtr GuardSeparationModule(string name,string expectedSha){
        IntPtr module=GetModuleHandle(name);if(module==IntPtr.Zero)throw new InvalidOperationException("Separation module absent: "+name);
        string expected=Path.GetFullPath(Path.Combine(resources,name=="Communication1.dll"?@"LOCOMOTIVES\1\"+name:name));var actual=new StringBuilder(32768);
        uint length=SeparationGetModuleFileName(module,actual,actual.Capacity);
        if(length==0||length>=actual.Capacity||!String.Equals(Path.GetFullPath(actual.ToString()),expected,StringComparison.OrdinalIgnoreCase))throw new InvalidOperationException("Separation module path guard failed: "+name);
        using(var sha=SHA256.Create())using(var file=File.OpenRead(expected))if(BitConverter.ToString(sha.ComputeHash(file)).Replace("-","")!=expectedSha)throw new InvalidOperationException("Separation source SHA guard failed: "+name);
        GuardSeparationMemory(module,0x40,false);if(Marshal.ReadInt16(module)!=0x5a4d)throw new InvalidOperationException("Separation module PE guard failed.");
        return module;
    }
    static void GuardSeparationRecord(IntPtr module,int rva,string name,int size){
        IntPtr record=IntPtr.Add(module,rva);byte[] text=Encoding.ASCII.GetBytes(name);GuardSeparationMemory(record,10+text.Length,false);
        if(Marshal.ReadIntPtr(record)!=IntPtr.Add(record,4)||Marshal.ReadByte(record,4)!=14||Marshal.ReadByte(record,5)!=text.Length||Marshal.ReadInt32(record,6+text.Length)!=size)
            throw new InvalidOperationException("Separation live record type/size guard failed: "+name);
        for(int i=0;i<text.Length;i++)if(Marshal.ReadByte(record,6+i)!=text[i])throw new InvalidOperationException("Separation record name guard failed.");
    }
    [StructLayout(LayoutKind.Sequential)] struct SeparationMemoryInformation {internal IntPtr BaseAddress,AllocationBase;internal uint AllocationProtect;internal UIntPtr RegionSize;internal uint State,Protect,Type;}
    static void GuardSeparationMemory(IntPtr pointer,int length,bool writable){
        if(pointer==IntPtr.Zero||length<=0)throw new InvalidOperationException("Invalid pneumatic separation memory range.");
        ulong start=unchecked((uint)pointer.ToInt32()),end=start+(uint)length;if(end>0x100000000UL)throw new InvalidOperationException("Pneumatic separation memory wraps.");
        while(start<end){SeparationMemoryInformation info;if(SeparationVirtualQuery(new IntPtr(unchecked((int)(uint)start)),out info,new UIntPtr((uint)Marshal.SizeOf(typeof(SeparationMemoryInformation))))==UIntPtr.Zero)throw new Win32Exception(Marshal.GetLastWin32Error(),"Cannot inspect pneumatic separation memory");
            uint p=info.Protect&0xff;bool read=p==2||p==4||p==8||p==0x20||p==0x40||p==0x80,write=p==4||p==8||p==0x40||p==0x80;
            if(info.State!=0x1000||(info.Protect&0x100)!=0||!read||writable&&!write)throw new InvalidOperationException("Pneumatic separation memory protection guard failed.");
            ulong next=unchecked((uint)info.BaseAddress.ToInt32())+info.RegionSize.ToUInt64();if(next<=start)throw new InvalidOperationException("Pneumatic separation memory range does not advance.");start=Math.Min(end,next);
        }
    }
    [DllImport("kernel32.dll",EntryPoint="GetCurrentThreadId")]static extern uint SeparationGetCurrentThreadId();
    [DllImport("kernel32.dll",EntryPoint="GetModuleFileNameW",CharSet=CharSet.Unicode,SetLastError=true)]static extern uint SeparationGetModuleFileName(IntPtr module,StringBuilder name,int size);
    [DllImport("kernel32.dll",EntryPoint="VirtualQuery",SetLastError=true)]static extern UIntPtr SeparationVirtualQuery(IntPtr address,out SeparationMemoryInformation information,UIntPtr size);
}
