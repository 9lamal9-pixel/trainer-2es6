using System;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Collections.Generic;
using System.Windows.Forms;
using System.Web.Script.Serialization;
using TrainerAir;

class Sa47ReleaseProbe {
    const BindingFlags Flags=BindingFlags.Instance|BindingFlags.NonPublic;
    static TrainerIntegrationHost host;
    static Dictionary<string,IntPtr> mem;
    static IntPtr loc,releaseControl,schema;
    static TwoSectionAirChain air;
    static int ticks,raw;
    static bool nativeButtonMode;
    static readonly List<object> phases=new List<object>(),assertions=new List<object>();
    static int failed;
    static Delegate faultOriginalPneumo;
    static int faultObservedRaw=-1;
    [DllImport("kernel32.dll")] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll")] static extern IntPtr VirtualAlloc(IntPtr p,UIntPtr size,uint flags,uint protection);
    [DllImport("kernel32.dll")] static extern bool VirtualFree(IntPtr p,UIntPtr size,uint flags);
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int RegisterCall();
    static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,Flags).Invoke(host,null);}
    static void Position(int? value){typeof(TrainerIntegrationHost).GetMethod("SetSa47OperatorPosition",Flags).Invoke(host,new object[]{value});}
    static object Field(string name){return typeof(TrainerIntegrationHost).GetField(name,Flags).GetValue(host);}
    static void Set(string name,object value){typeof(TrainerIntegrationHost).GetField(name,Flags).SetValue(host,value);}
    static float Read(IntPtr p,int offset){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
    static void Check(bool passed,string name,object actual){assertions.Add(new {name=name,passed=passed,actual=actual});if(!passed)failed++;}
    static double[] Pressures(){return new[]{air.Sections[0].Cylinder[0].Gauge,air.Sections[0].Cylinder[1].Gauge,air.Sections[1].Cylinder[0].Gauge,air.Sections[1].Cylinder[1].Gauge};}
    static bool AllAbove(double[] values,double minimum){foreach(double v in values)if(Double.IsNaN(v)||v<minimum)return false;return true;}
    static bool AllBelow(double[] values,double maximum){foreach(double v in values)if(Double.IsNaN(v)||v>maximum)return false;return true;}
    static void InstantiateTumba(IntPtr cab){
        var module=GetModuleHandle("Communication1.dll");if(module==IntPtr.Zero)throw new Exception("Cab module absent");
        string file=Path.Combine(Directory.GetCurrentDirectory(),@"RESOURCES\LOCOMOTIVES\1\Communication1.dll");
        using(var s=File.OpenRead(file))using(var h=SHA256.Create())if(BitConverter.ToString(h.ComputeHash(s)).Replace("-","")!="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F")throw new Exception("Cab SHA guard");
        var target=IntPtr.Add(module,0x2ae994);
        if(Marshal.ReadByte(target)!=0x55||Marshal.ReadByte(target,1)!=0x8b||Marshal.ReadByte(target,2)!=0xec)throw new Exception("Cab constructor code guard");
        var thunk=VirtualAlloc(IntPtr.Zero,(UIntPtr)32,0x3000,0x40);if(thunk==IntPtr.Zero)throw new Exception("Cab thunk allocation");
        try{var bytes=new List<byte>();bytes.Add(0xb8);bytes.AddRange(BitConverter.GetBytes(cab.ToInt32()));bytes.Add(0xba);bytes.AddRange(BitConverter.GetBytes(1));bytes.Add(0xb9);bytes.AddRange(BitConverter.GetBytes(target.ToInt32()));bytes.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(bytes.ToArray(),0,thunk,bytes.Count);((RegisterCall)Marshal.GetDelegateForFunctionPointer(thunk,typeof(RegisterCall)))();}
        finally{if(!VirtualFree(thunk,UIntPtr.Zero,0x8000))throw new Exception("Cab thunk free");}
    }
    static void Cycle(int km,int kvt){
        Set("commandedKm",(int?)km);
        // Operator source only. No cylinder/relay/processed-SA47 output is written.
        var selected=typeof(TrainerIntegrationHost).GetMethod("Sa47OperatorPosition",Flags).Invoke(host,null);
        byte widgetBefore=Marshal.ReadByte(releaseControl,0x1d9);
        int expectedRaw=selected==null?(widgetBefore==1?2:widgetBefore):(int)selected;
        Call("ElectricalCycle");Call("ApplyTestInputs");
        if(Marshal.ReadByte(loc,0x8c)!=expectedRaw)throw new Exception("Native SA47 operator-to-raw input differs");
        if(Marshal.ReadByte(releaseControl,0x1d9)!=widgetBefore)throw new Exception("Native SA47 widget input was not restored after electrical call");
        Marshal.WriteByte(loc,0x7e,(byte)kvt);
        for(int i=0x80;i<=0x87;i++)Marshal.WriteByte(loc,i,0);
        Call("PneumaticCycle");Call("MotionCycle");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");
        if(Marshal.ReadByte(loc,0x8c)!=expectedRaw)throw new Exception("Native SA47 raw input was not restored after pneumatic call");
        ((Delegate)Field("klubStep")).DynamicInvoke();
        ticks++;Set("ticks",ticks);
    }
    static void Phase(string name,int count,int km,int kvt){
        if(!nativeButtonMode)Position(raw);
        RecordPhase(name,count,km,kvt);
    }
    static void RecordPhase(string name,int count,int km,int kvt){
        for(int i=0;i<count;i++)Cycle(km,kvt);
        phases.Add(new{name=name,seconds=ticks*.1,raw=Marshal.ReadByte(loc,0x8c),widget=Marshal.ReadByte(releaseControl,0x1d9),native_position=Marshal.ReadInt32(schema,0x104),native_sa47=Marshal.ReadByte(schema,0x103),native_latch=Marshal.ReadByte(schema,0x108),pm=air.Pm.Gauge,ur=Read(loc,9),tm=Read(mem["TVagonParam"],0),tc=Pressures()});
    }
    static void NativeButton(bool down){
        var module=GetModuleHandle("Communication1.dll");var vmt=Marshal.ReadIntPtr(releaseControl);
        int rva=down?0x2a1960:0x2a2674,slot=down?0xb4:0xb8;
        var target=IntPtr.Add(module,rva);
        if(vmt!=IntPtr.Add(module,0x29ea70)||Marshal.ReadIntPtr(vmt,slot)!=target||Marshal.ReadByte(releaseControl,0x1d8)!=0)throw new Exception("Native SA47 event class/slot/Fixed guard");
        string prefix=down?"558bec83c4f88855fb8945fc":"558bec51";
        for(int i=0;i<prefix.Length/2;i++)if(Marshal.ReadByte(target,i)!=Convert.ToByte(prefix.Substring(i*2,2),16))throw new Exception("Native SA47 event code guard");
        var thunk=VirtualAlloc(IntPtr.Zero,(UIntPtr)32,0x3000,0x40);if(thunk==IntPtr.Zero)throw new Exception("Native SA47 event thunk allocation");
        try{var bytes=new List<byte>();bytes.Add(0xb8);bytes.AddRange(BitConverter.GetBytes(releaseControl.ToInt32()));bytes.Add(0xba);bytes.AddRange(BitConverter.GetBytes(down?1:0));bytes.Add(0xb9);bytes.AddRange(BitConverter.GetBytes(target.ToInt32()));bytes.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(bytes.ToArray(),0,thunk,bytes.Count);((RegisterCall)Marshal.GetDelegateForFunctionPointer(thunk,typeof(RegisterCall)))();}
        finally{if(!VirtualFree(thunk,UIntPtr.Zero,0x8000))throw new Exception("Native SA47 event thunk free");}
    }
    static void NativeButtonChecks(){
        Check(Marshal.ReadByte(releaseControl,0x1d8)==0,"native_release_button_is_momentary",Marshal.ReadByte(releaseControl,0x1d8));
        NativeButton(true);Check(Marshal.ReadByte(releaseControl,0x1d9)==1,"native_DwnButton_sets_physical1",Marshal.ReadByte(releaseControl,0x1d9));
        Phase("native_button_down",300,4,2);
        Check(Marshal.ReadByte(loc,0x8c)==2&&Marshal.ReadByte(schema,0x103)==1,"physical1_publishes_impulse2",Marshal.ReadByte(loc,0x8c));
        Check(AllBelow(Pressures(),.05),"native_button_releases_four_cylinders",Pressures());
        NativeButton(false);Check(Marshal.ReadByte(releaseControl,0x1d9)==0,"native_UpBtn_restores_physical0",Marshal.ReadByte(releaseControl,0x1d9));
        Phase("native_button_up",300,4,2);
        Check(Marshal.ReadByte(loc,0x8c)==0&&Marshal.ReadByte(schema,0x103)==0,"physical0_cancels_request",Marshal.ReadByte(loc,0x8c));
        Check(AllAbove(Pressures(),.5),"native_button_up_restores_automatic_brake",Pressures());
    }
    static void ManagedButtonChecks(){
        Position(0);var button=(Button)Field("sa47ImpulseButton");
        typeof(Control).GetMethod("OnMouseDown",Flags).Invoke(button,new object[]{new MouseEventArgs(MouseButtons.Left,1,2,2,0)});
        Check((int)typeof(TrainerIntegrationHost).GetMethod("Sa47OperatorPosition",Flags).Invoke(host,null)==2,"managed_mouse_down_selects2",Field("sa47Momentary"));
        RecordPhase("managed_mouse_down",300,4,2);Check(AllBelow(Pressures(),.05),"managed_mouse_releases_four_cylinders",Pressures());
        typeof(Control).GetMethod("OnMouseUp",Flags).Invoke(button,new object[]{new MouseEventArgs(MouseButtons.Left,1,2,2,0)});
        RecordPhase("managed_mouse_up",20,4,2);Check(Marshal.ReadByte(loc,0x8c)==1&&AllBelow(Pressures(),.05),"managed_mouse_up_returns1_with_latch",Marshal.ReadByte(loc,0x8c));
        typeof(Control).GetMethod("OnKeyDown",Flags).Invoke(button,new object[]{new KeyEventArgs(Keys.Space)});
        Cycle(4,2);Check(Marshal.ReadByte(loc,0x8c)==2,"managed_space_down_selects2",Marshal.ReadByte(loc,0x8c));
        typeof(Control).GetMethod("OnKeyUp",Flags).Invoke(button,new object[]{new KeyEventArgs(Keys.Space)});
        Cycle(4,2);Check(Marshal.ReadByte(loc,0x8c)==1,"managed_space_up_returns1",Marshal.ReadByte(loc,0x8c));
        Position(0);RecordPhase("managed_cancel0",300,4,2);Check(AllAbove(Pressures(),.5),"managed0_restores_automatic_brake",Pressures());
        Position(null);Cycle(4,2);Check(Marshal.ReadByte(loc,0x8c)==0,"return_to_native_source_uses_physical0",Marshal.ReadByte(loc,0x8c));
    }
    static void AdapterChecks(){
        var binding=(TrainerNativeBrakeRelease)Field("nativeBrakeRelease");bool selected;
        binding.Capture(ticks);using(binding.Borrow(ticks,false,out selected)){Check(selected&&Marshal.ReadByte(loc,0x8c)==1,"fresh_processed_request_is_native_boolean",selected);}
        Check(Marshal.ReadByte(loc,0x8c)==2,"borrow_restores_enum2",Marshal.ReadByte(loc,0x8c));
        using(binding.Borrow(ticks,false,out selected)){Check(!selected&&Marshal.ReadByte(loc,0x8c)==0,"one_capture_not_reused",selected);}
        binding.Capture(ticks);using(binding.Borrow(ticks+1,false,out selected)){Check(!selected,"wrong_tick_is_neutral",selected);}
        binding.Capture(ticks);binding.Invalidate();using(binding.Borrow(ticks,false,out selected)){Check(!selected,"invalidated_snapshot_is_neutral",selected);}
        binding.Capture(ticks);bool rejected=false;Marshal.WriteByte(loc,0x8c,3);
        try{binding.Capture(ticks);}catch(Exception){rejected=true;}finally{Marshal.WriteByte(loc,0x8c,2);}
        Check(rejected,"invalid_position_capture_rejected",rejected);
        using(binding.Borrow(ticks,false,out selected)){Check(!selected,"failed_capture_drops_previous_request",selected);}
        binding.Capture(ticks);byte km=Marshal.ReadByte(loc,0x7d);Marshal.WriteByte(loc,0x7d,5);
        try{using(binding.Borrow(ticks,false,out selected)){Check(!selected,"late_km5_is_neutral",selected);}}finally{Marshal.WriteByte(loc,0x7d,km);}
        binding.Capture(ticks);using(binding.Borrow(ticks,true,out selected)){Check(!selected,"safety_priority_is_neutral",selected);}
        binding.Capture(ticks);bool bodyException=false;
        try{using(binding.Borrow(ticks,false,out selected)){throw new InvalidOperationException("scope-body-fixture");}}
        catch(InvalidOperationException ex){bodyException=ex.Message=="scope-body-fixture";}
        Check(bodyException&&Marshal.ReadByte(loc,0x8c)==2,"body_exception_restores_enum2",Marshal.ReadByte(loc,0x8c));
        binding.Capture(ticks);var scope=binding.Borrow(ticks,false,out selected);Marshal.WriteByte(loc,0x8c,0);bool foreignRejected=false;
        try{scope.Dispose();}catch(Exception){foreignRejected=true;}
        Check(foreignRejected&&Marshal.ReadByte(loc,0x8c)==0,"foreign_write_is_preserved",Marshal.ReadByte(loc,0x8c));scope.Dispose();Marshal.WriteByte(loc,0x8c,2);
        binding.Capture(ticks);var ownerField=typeof(TrainerNativeBrakeRelease).GetField("owner",BindingFlags.Instance|BindingFlags.NonPublic);object oldOwner=ownerField.GetValue(binding);ownerField.SetValue(binding,IntPtr.Zero);bool ownerRejected=false;
        try{binding.Borrow(ticks,false,out selected);}catch(Exception){ownerRejected=true;}finally{ownerField.SetValue(binding,oldOwner);}
        Check(ownerRejected&&Marshal.ReadByte(loc,0x8c)==2,"foreign_expected_owner_rejected_before_write",ownerRejected);
        binding.Capture(ticks);bool wrongThreadRejected=false;
        var other=new System.Threading.Thread(delegate(){try{binding.Borrow(ticks,false,out selected);}catch(Exception){wrongThreadRejected=true;}});other.Start();other.Join();
        Check(wrongThreadRejected&&Marshal.ReadByte(loc,0x8c)==2,"wrong_thread_rejected_before_write",wrongThreadRejected);
    }
    static void ThrowFirstElectrical(){throw new ArithmeticException("SA47 first electrical fault fixture");}
    static void ObservePneumaticThenThrow(IntPtr train){faultObservedRaw=Marshal.ReadByte(loc,0x8c);faultOriginalPneumo.DynamicInvoke(train);throw new InvalidOperationException("SA47 pneumatic tail fixture");}
    static void FirstElectricalFault(){
        Check(Field("nativeBrakeRelease")==null,"before_first_electrical_binding_absent",Field("nativeBrakeRelease")==null);
        var commField=typeof(TrainerIntegrationHost).GetField("commStep",Flags);object originalComm=commField.GetValue(host);
        var pneumoField=typeof(TrainerIntegrationHost).GetField("pneumoStep",Flags);faultOriginalPneumo=(Delegate)pneumoField.GetValue(host);
        Marshal.WriteByte(loc,0x8c,1);
        try{
            commField.SetValue(host,Delegate.CreateDelegate(commField.FieldType,typeof(Sa47ReleaseProbe).GetMethod("ThrowFirstElectrical",BindingFlags.Static|BindingFlags.NonPublic)));
            Call("ElectricalCycle");Check((int)Field("electricalFaults")==1,"first_electrical_error_captured",Field("electricalFaults"));
            Check(Field("nativeBrakeRelease")!=null,"binding_exists_despite_first_failure",Field("nativeBrakeRelease")!=null);
            Check(Marshal.ReadByte(releaseControl,0x1d9)==0&&Field("nativeReleaseCabInput")==null,"first_failure_restores_cab_input",Marshal.ReadByte(releaseControl,0x1d9));
            pneumoField.SetValue(host,Delegate.CreateDelegate(pneumoField.FieldType,typeof(Sa47ReleaseProbe).GetMethod("ObservePneumaticThenThrow",BindingFlags.Static|BindingFlags.NonPublic)));
            bool expected=false;try{Call("PneumaticCycle");}catch(TargetInvocationException ex){expected=ex.InnerException is InvalidOperationException&&ex.InnerException.Message=="SA47 pneumatic tail fixture";}
            Check(expected,"native_tail_exception_observed",expected);
            Check(faultObservedRaw==0,"first_failed_electrical_uses_neutral_native_input",faultObservedRaw);
            Check(Marshal.ReadByte(loc,0x8c)==1,"native_exception_restores_original_raw1",Marshal.ReadByte(loc,0x8c));
        }finally{commField.SetValue(host,originalComm);pneumoField.SetValue(host,faultOriginalPneumo);Marshal.WriteByte(loc,0x8c,0);}
    }
    [STAThread] static int Main(string[] args){
        nativeButtonMode=args.Length>0&&args[0]=="native-button";
        Application.EnableVisualStyles();host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);
        string error=null;
        try{
            var handle=host.Handle;Call("StartNative");var timer=Field("timer") as Timer;if(timer==null)throw new Exception("Native startup did not produce a timer");timer.Stop();
            mem=(Dictionary<string,IntPtr>)Field("mem");loc=mem["TLocParam"];air=(TwoSectionAirChain)Field("airCircuit");
            var module=GetModuleHandle("Pult1.dll");var owner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(module,0x1a2e60));
            if(owner==IntPtr.Zero||Marshal.ReadIntPtr(owner,4)!=mem["TPult"])throw new Exception("Pult owner guard");schema=IntPtr.Add(owner,0x18);
            var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);InstantiateTumba(cab);
            var form=Marshal.ReadIntPtr(cab,0xc);releaseControl=Marshal.ReadIntPtr(form,0x3e4);
            if(releaseControl==IntPtr.Zero||Marshal.ReadIntPtr(Marshal.ReadIntPtr(form,0x530),4)!=loc)throw new Exception("Cab release wiring guard");
            if(args.Length>0&&args[0]=="first-fault"){FirstElectricalFault();}
            else{
            raw=0;Phase("charge",1000,2,2);
            int count=0;while(Read(loc,9)>4.4&&count++<200)Cycle(5,2);
            Check(count<200,"service_step_reached",Read(loc,9));
            Phase("automatic_hold",600,4,2);
            var before=Pressures();Check(AllAbove(before,.5),"four_cylinders_automatically_applied",before);
            Check(Marshal.ReadByte(schema,0x108)==0,"initial_manual_latch_clear",Marshal.ReadByte(schema,0x108));
            if(nativeButtonMode){NativeButtonChecks();}
            else{
            raw=1;Phase("position1_without_position2",300,4,2);
            var position1=Pressures();
            Check(Marshal.ReadByte(schema,0x103)==0,"native_position1_does_not_request_release",Marshal.ReadByte(schema,0x103));
            Check(AllAbove(position1,.5),"position1_retains_four_cylinders",position1);
            raw=2;Phase("position2_impulse",300,4,2);
            Check(Marshal.ReadByte(schema,0x103)==1,"position2_native_request",Marshal.ReadByte(schema,0x103));
            Check(AllBelow(Pressures(),.05),"position2_releases_four_cylinders",Pressures());
            AdapterChecks();
            raw=1;Phase("return_to_position1",300,4,2);
            Check(Marshal.ReadByte(schema,0x103)==1,"return_to1_keeps_native_latch",Marshal.ReadByte(schema,0x103));
            Check(AllBelow(Pressures(),.05),"return_to1_keeps_release",Pressures());
            raw=0;Phase("position0_cancels",300,4,2);
            Check(Marshal.ReadByte(schema,0x103)==0,"position0_native_request_off",Marshal.ReadByte(schema,0x103));
            Check(AllAbove(Pressures(),.5),"position0_restores_automatic_brake",Pressures());
            ManagedButtonChecks();
            }
            Check(air.Pm.Gauge>6,"prepared_supply_remains_available",air.Pm.Gauge);
            Check((int)Field("electricalFaults")==0,"no_electrical_faults",Field("electricalFaults"));
            }
        }catch(Exception ex){error=ex.ToString();failed++;Console.WriteLine(ex);}
        finally{
            try{host.Close();host.Dispose();}catch(Exception ex){error=(error??"")+"\nClose: "+ex;failed++;}
            var result=new{passed=failed==0,failed_assertions=failed,cycles=ticks,assertions=assertions,phases=phases,error=error,scope="Fresh native warm electrical/pneumatic/motion/aux/KLUB cycles; native widget states and KM/KVT diagnostic inputs only. No pressure or processed outputs forced. Initial test isolates SA47 raw0-to1 under steady KMIV/KVTII. KR22 actuator calibration and full RE conformity remain outside this scenario."};
            File.WriteAllText(TrainerSession.ResultPath(args.Length>0&&args[0]=="first-fault"?"Trainer_sa47_release_fault_validation.json":nativeButtonMode?"Trainer_sa47_native_button_validation.json":"Trainer_sa47_release_validation.json"),new JavaScriptSerializer().Serialize(result));
        }
        Console.WriteLine("SA47 release: "+(failed==0?"PASS":"FAIL")+", cycles="+ticks+", failed="+failed);
        return failed==0?0:1;
    }
}
