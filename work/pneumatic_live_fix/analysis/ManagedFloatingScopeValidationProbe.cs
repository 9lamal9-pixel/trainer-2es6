using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Threading;
using System.Web.Script.Serialization;

// Component-only x86 machine helpers. Does not load or execute trainer DLLs.
public sealed class ManagedFloatingScopeValidationProbe {
    static readonly List<object> checks=new List<object>();
    static int failed;
    static void Trace(string stage){Console.WriteLine("FP_SCOPE "+stage);Console.Out.Flush();}
    static void Check(string name,bool passed,object actual) {checks.Add(new{name,passed,actual});if(!passed)failed++;}
    static void Reject(string name,Action action) {bool rejected=false;try{action();}catch(InvalidOperationException){rejected=true;}Check(name,rejected,rejected);}
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void BitsThunk(IntPtr state);
    sealed class Raw : IDisposable {
        readonly byte[] expected=HelperBytes();
        static byte[] HelperBytes(){var b=new byte[64];for(int i=0;i<b.Length;i++)b[i]=0xcc;var r=Hex("8B442404D938DD78040FAE5808C20400");var w=Hex("8B442404DBE2D9280FAE5008C20400");Buffer.BlockCopy(r,0,b,0,r.Length);Buffer.BlockCopy(w,0,b,32,w.Length);return b;}
        IntPtr code,buffer;BitsThunk read,write;
        internal Raw() {
            if(IntPtr.Size!=4)throw new InvalidOperationException("x86 component only");
            buffer=Marshal.AllocHGlobal(16);code=VirtualAlloc(IntPtr.Zero,new UIntPtr((uint)expected.Length),0x3000,4);
            if(code==IntPtr.Zero)throw new InvalidOperationException("raw helper allocation");
            Marshal.Copy(expected,0,code,expected.Length);uint old;
            if(!VirtualProtect(code,new UIntPtr((uint)expected.Length),0x20,out old)||!FlushInstructionCache(GetCurrentProcess(),code,new UIntPtr((uint)expected.Length)))throw new InvalidOperationException("raw helper RX");
            read=(BitsThunk)Marshal.GetDelegateForFunctionPointer(code,typeof(BitsThunk));
            write=(BitsThunk)Marshal.GetDelegateForFunctionPointer(IntPtr.Add(code,32),typeof(BitsThunk));
        }
        void Guard(){var actual=new byte[expected.Length];Marshal.Copy(code,actual,0,actual.Length);for(int i=0;i<actual.Length;i++)if(actual[i]!=expected[i])throw new InvalidOperationException("raw helper guard");}
        internal uint[] Read(){Guard();read(buffer);return new[]{unchecked((uint)(ushort)Marshal.ReadInt16(buffer)),unchecked((uint)(ushort)Marshal.ReadInt16(buffer,4)),unchecked((uint)Marshal.ReadInt32(buffer,8))};}
        internal void Write(uint cw,uint mxcsr){Guard();Marshal.WriteInt16(buffer,unchecked((short)cw));Marshal.WriteInt32(buffer,8,unchecked((int)mxcsr));write(buffer);}
        public void Dispose(){if(code!=IntPtr.Zero){Guard();if(!VirtualFree(code,UIntPtr.Zero,0x8000))throw new InvalidOperationException("raw helper free");code=IntPtr.Zero;}if(buffer!=IntPtr.Zero){Marshal.FreeHGlobal(buffer);buffer=IntPtr.Zero;}}
    }
    static void Pair(string name,uint[] actual,uint cw,uint mxcsr){Check(name,actual[0]==cw&&actual[2]==mxcsr,new{cw=actual[0],sw=actual[1],mxcsr=actual[2],expected_cw=cw,expected_mxcsr=mxcsr});}
    // MXCSR bits0..5 are sticky exception status. CLR work may legitimately set
    // inexact after Enter; rounding/exception masks/DAZ/FTZ remain strict.
    static void ManagedPair(string name,uint[] actual){const uint status=0x3f;Check(name,actual[0]==0x027f&&(actual[2]&~status)==0x1f80,new{cw=actual[0],sw=actual[1],mxcsr=actual[2],expected_cw=0x027f,expected_mxcsr_controls=0x1f80,ignored_sticky_status_mask=status});}
    static void Scenarios(Raw raw){
        // Deliberately different x87/SSE rounding AND exception masks. No FP
        // arithmetic is executed in these native fixtures; status is cleared.
        const uint nativeCw=0x0b7b,nativeMxcsr=0x5f00,updatedCw=0x077f,updatedMxcsr=0x3f80;
        Trace("before fixture native Write");
        raw.Write(nativeCw,nativeMxcsr);Trace("after fixture native Write");Pair("fixture native raw settings",raw.Read(),nativeCw,nativeMxcsr);
        Trace("before outer Enter");
        var outer=TrainerManagedFloatingScope.Enter();
        uint[] expectedAfterThrow=new[]{nativeCw,0U,nativeMxcsr};
        Trace("after outer Enter");
        try {
            ManagedPair("Enter gives managed 53-bit environment",raw.Read());
            var initial=TrainerManagedFloatingScope.Snapshot();
            Check("Snapshot preserves native raw x87 CW",Convert.ToUInt32(initial["NativeControlWord"])==nativeCw,initial);
            Check("Snapshot preserves independently masked native MXCSR",Convert.ToUInt32(initial["NativeMxcsr"])==nativeMxcsr,initial);
            Check("Snapshot includes complete native image SHA",((string)initial["NativeImageSha256"]).Length==64,initial["NativeImageSha256"]);
            Trace("before nested Enter");var inner=TrainerManagedFloatingScope.Enter();
            try {
                ManagedPair("nested Enter keeps managed environment",raw.Read());
                Check("nested Enter shares original native image",Object.Equals(initial["NativeImageSha256"],TrainerManagedFloatingScope.Snapshot()["NativeImageSha256"]),TrainerManagedFloatingScope.Snapshot());
                Reject("out of order outer Dispose rejected",delegate{outer.Dispose();});
            } finally {inner.Dispose();}
            Reject("Shutdown refuses active owner",TrainerManagedFloatingScope.Shutdown);
            bool wrongThreadRejected=false;
            var other=new Thread(delegate(){try{outer.Dispose();}catch(InvalidOperationException){wrongThreadRejected=true;}});other.Start();other.Join();
            Check("cross-thread Dispose rejected",wrongThreadRejected,wrongThreadRejected);
            Trace("before RunNative");TrainerManagedFloatingScope.RunNative(delegate {
                Trace("inside RunNative");
                Pair("RunNative lends original native CW/MXCSR",raw.Read(),nativeCw,nativeMxcsr);
                raw.Write(updatedCw,updatedMxcsr);
                Trace("before native-to-managed reentry");using(TrainerManagedFloatingScope.Enter()) {
                    ManagedPair("managed callback inside native callback reset",raw.Read());
                    Check("reentrant owner captured updated native CW",Convert.ToUInt32(TrainerManagedFloatingScope.Snapshot()["NativeControlWord"])==updatedCw,TrainerManagedFloatingScope.Snapshot());
                }
                Pair("reentrant scope returns to updated native state",raw.Read(),updatedCw,updatedMxcsr);
                TrainerManagedFloatingScope.RunNative(delegate{Pair("nested RunNative stays native",raw.Read(),updatedCw,updatedMxcsr);});
            });
            Trace("after RunNative");ManagedPair("RunNative returns to managed environment",raw.Read());
            var latest=TrainerManagedFloatingScope.Snapshot();
            Check("RunNative captures native CW changes",Convert.ToUInt32(latest["NativeControlWord"])==updatedCw,latest);
            Check("RunNative captures native MXCSR changes",Convert.ToUInt32(latest["NativeMxcsr"])==updatedMxcsr,latest);
            bool callbackException=false;
            uint[] callbackUnwindObserved=null;
            Trace("before throwing RunNative");try {TrainerManagedFloatingScope.RunNative(delegate {
                raw.Write(nativeCw,nativeMxcsr);
                try {throw new ApplicationException("native callback fixture");}
                finally {callbackUnwindObserved=raw.Read();}
            });}
            catch(ApplicationException ex){callbackException=ex.Message=="native callback fixture";}
            Check("native callback exception propagated",callbackException,callbackException);
            ManagedPair("native callback exception finally resets managed environment",raw.Read());
            var unwindImage=TrainerManagedFloatingScope.Snapshot();
            Check("native callback exception captures actual post-unwind native controls",callbackUnwindObserved!=null&&Convert.ToUInt32(unwindImage["NativeControlWord"])==callbackUnwindObserved[0]&&Convert.ToUInt32(unwindImage["NativeMxcsr"])==callbackUnwindObserved[2],new{before_throw_cw=nativeCw,before_throw_mxcsr=nativeMxcsr,observed_in_callback_finally=callbackUnwindObserved,captured=unwindImage,note="CLR exception unwinding can change native masks; require the actual latest state, not pre-throw bitwise constancy."});
            if(callbackUnwindObserved==null)throw new InvalidOperationException("Missing native callback finally observation.");
            expectedAfterThrow=callbackUnwindObserved;
            using(TrainerManagedFloatingScope.Enter())ManagedPair("scope usable after callback exception",raw.Read());
        } finally {Trace("before outer Dispose");outer.Dispose();Trace("after outer Dispose");}
        Pair("outer Dispose restores latest native raw environment",raw.Read(),expectedAfterThrow[0],expectedAfterThrow[2]);
        bool managedException=false;
        Trace("before managed exception");try {using(TrainerManagedFloatingScope.Enter()){throw new ApplicationException("managed fixture");}}
        catch(ApplicationException ex){managedException=ex.Message=="managed fixture";}
        Check("managed exception propagated",managedException,managedException);
        Pair("managed exception restores original native environment",raw.Read(),expectedAfterThrow[0],expectedAfterThrow[2]);
        Check("all owner buffers released",!TrainerManagedFloatingScope.HasRetainedOwners,TrainerManagedFloatingScope.HasRetainedOwners);
        Trace("before first Shutdown");TrainerManagedFloatingScope.DisposeRetainedOwners();TrainerManagedFloatingScope.Shutdown();
        Trace("before recreate Enter");
        using(TrainerManagedFloatingScope.Enter())ManagedPair("helpers can be recreated after complete Shutdown",raw.Read());
        Pair("recreated scope restores native environment",raw.Read(),expectedAfterThrow[0],expectedAfterThrow[2]);
        TrainerManagedFloatingScope.Shutdown();
    }
    public static int Main(){
        string error=null,cleanupError=null;Raw raw=null;uint[] original=null;
        try {Trace("start");raw=new Raw();Trace("raw helpers ready");original=raw.Read();Trace("original captured");Scenarios(raw);}
        catch(Exception ex){failed++;error=ex.ToString();}
        finally {
            try {Trace("before final cleanup");TrainerManagedFloatingScope.DisposeRetainedOwners();TrainerManagedFloatingScope.Shutdown();if(raw!=null&&original!=null){raw.Write(original[0],original[2]);Pair("probe restores caller raw CW/MXCSR",raw.Read(),original[0],original[2]);}if(raw!=null)raw.Dispose();Trace("final cleanup done");}
            catch(Exception ex){failed++;cleanupError=ex.ToString();}
        }
        var result=new{passed=failed==0,error,cleanup_error=cleanupError,checks_count=checks.Count,failed_assertions=failed,checks,
            trainer_native_dlls_executed=false,owned_machine_helpers_executed=true,scope="x86 FP environment component only; no trainer DLLs, Host, physics or calibration acceptance."};
        try{string dir=Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");if(String.IsNullOrEmpty(dir))dir=AppDomain.CurrentDomain.BaseDirectory;Directory.CreateDirectory(dir);File.WriteAllText(Path.Combine(dir,"Trainer_managed_floating_scope_validation.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(result));}
        catch(Exception ex){Console.WriteLine(ex);return 1;}
        Console.WriteLine((failed==0?"PASS":"FAIL")+": floating environment component; "+checks.Count+" assertions");return failed==0?0:1;
    }
    static byte[] Hex(string text){var b=new byte[text.Length/2];for(int i=0;i<b.Length;i++)b[i]=Convert.ToByte(text.Substring(i*2,2),16);return b;}
    [DllImport("kernel32.dll")]static extern IntPtr GetCurrentProcess();
    [DllImport("kernel32.dll")]static extern IntPtr VirtualAlloc(IntPtr p,UIntPtr size,uint type,uint protection);
    [DllImport("kernel32.dll")]static extern bool VirtualProtect(IntPtr p,UIntPtr size,uint protection,out uint previous);
    [DllImport("kernel32.dll")]static extern bool VirtualFree(IntPtr p,UIntPtr size,uint type);
    [DllImport("kernel32.dll")]static extern bool FlushInstructionCache(IntPtr process,IntPtr p,UIntPtr size);
}
