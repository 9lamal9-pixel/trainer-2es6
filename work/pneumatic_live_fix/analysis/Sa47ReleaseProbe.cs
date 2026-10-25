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
    static IntPtr loc,releaseControl,schema,cabForm;
    static int lastKm=-1,lastKvt=-1;
    static TwoSectionAirChain air;
    static int ticks,raw;
    static bool nativeButtonMode;
    static bool zeroResetMode;
    static bool tmResetMode,priorityMode,operatorMode,producerFaultMode;
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
    static void Cycle(int km,int kvt,int? expectedRawOverride=null){
        Set("commandedKm",null);
        if(km!=lastKm){NativePosition(Marshal.ReadIntPtr(cabForm,0x3b0),km);lastKm=km;}
        if(kvt!=lastKvt){NativePosition(Marshal.ReadIntPtr(cabForm,0x3b4),kvt);lastKvt=kvt;}
        // Operator source only. No cylinder/relay/processed-SA47 output is written.
        var selected=typeof(TrainerIntegrationHost).GetMethod("Sa47OperatorPosition",Flags).Invoke(host,null);
        byte widgetBefore=Marshal.ReadByte(releaseControl,0x1d9);
        // This changes an assertion only, never the native input/output. A held
        // physical button on source handover is deliberately mapped to ready1.
        int expectedRaw=expectedRawOverride.HasValue?expectedRawOverride.Value:selected==null?(widgetBefore==0?1:2):(int)selected;
        // The adapter first publishes readiness1 when an impulse starts from
        // explicit cancellation0; strict producer1-to2 follows next frame.
        if(expectedRaw==2&&Marshal.ReadByte(loc,0x8c)==0)expectedRaw=1;
        Call("ElectricalCycle");Call("ApplyTestInputs");
        if(Marshal.ReadByte(loc,0x8c)!=expectedRaw)throw new Exception("Native SA47 operator-to-raw input differs");
        if(Marshal.ReadByte(releaseControl,0x1d9)!=widgetBefore)throw new Exception("Native SA47 widget input was not restored after electrical call");
        if(Marshal.ReadByte(loc,0x7d)!=km||Marshal.ReadByte(loc,0x7e)!=kvt)throw new Exception("Native KM/KVT handle-to-input mismatch");
        for(int i=0x80;i<=0x87;i++)Marshal.WriteByte(loc,i,0);
        Call("PneumaticCycle");Call("MotionCycle");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");
        if(Marshal.ReadByte(loc,0x8c)!=expectedRaw)throw new Exception("Native SA47 raw input was not restored after pneumatic call");
        ((Delegate)Field("klubStep")).DynamicInvoke();
        ticks++;Set("ticks",ticks);
    }
    static void NativePosition(IntPtr control,double value){
        var module=GetModuleHandle("Communication1.dll");var target=IntPtr.Add(module,0x2a4944);
        var vmt=Marshal.ReadIntPtr(control);var name=Marshal.ReadIntPtr(vmt,-56);int count=Marshal.ReadByte(name);var label=new byte[count];Marshal.Copy(IntPtr.Add(name,1),label,0,count);
        if(System.Text.Encoding.ASCII.GetString(label)!="TMultiPosTumbler"||Marshal.ReadByte(target)!=0x55||Marshal.ReadByte(target,1)!=0x8b||Marshal.ReadByte(target,2)!=0xec)throw new Exception("Native KM/KVT position class/code guard");
        var thunk=VirtualAlloc(IntPtr.Zero,(UIntPtr)64,0x3000,0x40);if(thunk==IntPtr.Zero)throw new Exception("Native position thunk allocation");
        try{var bytes=new List<byte>();var argument=BitConverter.GetBytes(value);bytes.Add(0xb8);bytes.AddRange(BitConverter.GetBytes(control.ToInt32()));bytes.Add(0x68);bytes.AddRange(new byte[]{argument[4],argument[5],argument[6],argument[7]});bytes.Add(0x68);bytes.AddRange(new byte[]{argument[0],argument[1],argument[2],argument[3]});bytes.AddRange(new byte[]{0x6a,0,0x6a,0});bytes.Add(0xb9);bytes.AddRange(BitConverter.GetBytes(target.ToInt32()));bytes.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(bytes.ToArray(),0,thunk,bytes.Count);((RegisterCall)Marshal.GetDelegateForFunctionPointer(thunk,typeof(RegisterCall)))();}
        finally{if(!VirtualFree(thunk,UIntPtr.Zero,0x8000))throw new Exception("Native position thunk free");}
    }
    static void Phase(string name,int count,int km,int kvt){
        if(!nativeButtonMode)Position(raw);
        RecordPhase(name,count,km,kvt);
    }
    static void RecordPhase(string name,int count,int km,int kvt){
        for(int i=0;i<count;i++)Cycle(km,kvt);
        phases.Add(new{name=name,seconds=ticks*.1,raw=Marshal.ReadByte(loc,0x8c),widget=Marshal.ReadByte(releaseControl,0x1d9),native_position=Marshal.ReadInt32(schema,0x104),native_sa47=Marshal.ReadByte(schema,0x103),native_latch=Marshal.ReadByte(schema,0x108),producer=ProducerSnapshot(),physical_km=Marshal.ReadByte(loc,0x7d),physical_kvt=Marshal.ReadByte(loc,0x7e),pm=air.Pm.Gauge,independent=air.Independent.Gauge,ur=Read(loc,9),tm=Read(mem["TVagonParam"],0),tc=Pressures()});
    }
    static object ProducerSnapshot(){
        var binding=Field("nativeBrakeRelease");if(binding==null)return null;
        var patch=binding.GetType().GetField("memoryPatch",Flags).GetValue(binding);
        return patch.GetType().GetMethod("Snapshot",BindingFlags.Instance|BindingFlags.Public).Invoke(patch,null);
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
        Check(Marshal.ReadByte(loc,0x8c)==1&&Marshal.ReadByte(schema,0x103)==1,"physical0_returns_to_readiness1_with_memory",Marshal.ReadByte(loc,0x8c));
        Check(AllBelow(Pressures(),.05),"native_button_up_retains_release",Pressures());
        Position(0);RecordPhase("native_source_explicit_cancel0",300,4,2);
        Check(AllAbove(Pressures(),.5)&&Marshal.ReadByte(schema,0x108)==0,"explicit0_cancels_native_button_memory",Pressures());
        Position(null);RecordPhase("native_source_ready_after_cancel",20,4,2);
        Check(Marshal.ReadByte(loc,0x8c)==1&&Marshal.ReadByte(schema,0x103)==0&&AllAbove(Pressures(),.5),"native_idle1_does_not_revive_cancelled_memory",Pressures());
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
        Position(null);Cycle(4,2);Check(Marshal.ReadByte(loc,0x8c)==1&&Marshal.ReadByte(schema,0x103)==0,"return_to_native_source_uses_readiness1_without_old_memory",Marshal.ReadByte(loc,0x8c));
    }
    static void ZeroResetChecks(){
        raw=1;Phase("zero_reset_initial_ready1",20,4,2);
        Check(Marshal.ReadByte(schema,0x108)==0&&Marshal.ReadByte(schema,0x103)==0,"initial1_has_no_manual_memory",Marshal.ReadByte(schema,0x108));
        raw=2;Phase("zero_reset_real_impulse2",300,4,2);
        Check(Marshal.ReadByte(schema,0x108)==1&&Marshal.ReadByte(schema,0x103)==1&&AllBelow(Pressures(),.05),"impulse_latches_and_physically_releases",Pressures());
        raw=1;Phase("zero_reset_return1",20,4,2);
        Check(Marshal.ReadByte(schema,0x103)==1&&AllBelow(Pressures(),.05),"return1_retains_release_before_cancel",Pressures());
        raw=0;Phase("zero_reset_cancel0",300,4,2);
        Check(Marshal.ReadByte(schema,0x108)==0,"logical0_clears_manual_memory",Marshal.ReadByte(schema,0x108));
        Check(Marshal.ReadByte(schema,0x103)==0&&AllAbove(Pressures(),.5),"logical0_reapplies_four_cylinders",Pressures());
        raw=1;Phase("zero_reset_ready1_without_new_impulse",300,4,2);
        Check(Marshal.ReadByte(schema,0x103)==0,"ready1_after_cancel_does_not_revive_old_request",Marshal.ReadByte(schema,0x103));
        Check(AllAbove(Pressures(),.5),"ready1_after_cancel_retains_four_cylinders",Pressures());
        raw=2;Phase("zero_reset_new_impulse2",300,4,2);
        Check(Marshal.ReadByte(schema,0x103)==1&&AllBelow(Pressures(),.05),"new_impulse_can_release_after_cancel",Pressures());
    }
    static void ChargeAcrossReset(string label){
        int n=0;while(Read(mem["TVagonParam"],0)<=5.01f&&n++<3000)Cycle(2,2);
        Check(n<3000,label+"_actual_head_TM_exceeds5",Read(mem["TVagonParam"],0));
        RecordPhase(label+"_above_threshold",3,2,2);
        Check(Marshal.ReadByte(schema,0x108)==0&&Marshal.ReadByte(schema,0x103)==0,label+"_head_TM_cancels_memory_and_request",new{tm=Read(mem["TVagonParam"],0),memory=Marshal.ReadByte(schema,0x108),request=Marshal.ReadByte(schema,0x103)});
    }
    static void ServiceAgain(string label){
        int n=0;while(Read(loc,9)>4.4&&n++<200)Cycle(5,2);
        Check(n<200,label+"_actual_service_step",Read(loc,9));RecordPhase(label+"_automatic_hold",600,4,2);
    }
    static void TmResetChecks(){
        raw=1;Phase("tm_reset_ready1",20,4,2);raw=2;Phase("tm_reset_impulse2",300,4,2);
        Check(AllBelow(Pressures(),.05)&&Marshal.ReadByte(schema,0x103)==1,"TM_test_initial_release_real",Pressures());
        raw=1;Phase("tm_reset_memory_in1",20,4,2);ChargeAcrossReset("TM_in1");
        ServiceAgain("TM_in1_next_brake");
        Check(Marshal.ReadByte(schema,0x103)==0&&AllAbove(Pressures(),.5),"after_TM_cancel_no_release_without_new2",Pressures());
        raw=2;Phase("tm_reset_new_impulse2",300,4,2);Check(Marshal.ReadByte(schema,0x103)==1&&AllBelow(Pressures(),.05),"new2_after_TM_cancel_releases",Pressures());
        ChargeAcrossReset("TM_held2");ServiceAgain("TM_held2_next_brake");
        Check(Marshal.ReadByte(schema,0x108)==0&&Marshal.ReadByte(schema,0x103)==0&&AllAbove(Pressures(),.5),"held2_does_not_rearm_after_TM_cancel",Pressures());
        raw=1;Phase("tm_reset_end_old_hold",3,4,2);raw=2;Phase("tm_reset_new_press_after_hold",300,4,2);
        Check(Marshal.ReadByte(schema,0x103)==1&&AllBelow(Pressures(),.05),"release_then_new2_rearms_after_TM_cancel",Pressures());
    }
    static void PriorityChecks(){
        raw=1;Phase("priority_ready1",20,4,2);raw=2;Phase("priority_impulse2",300,4,2);raw=1;Phase("priority_latched1",20,4,2);
        Check(AllBelow(Pressures(),.05),"priority_initial_release",Pressures());
        Phase("KVT_III_blocks_release",20,4,3);
        Check(Marshal.ReadByte(schema,0x108)==1&&Marshal.ReadByte(schema,0x103)==0,"KVT_III_blocks_execution_preserves_memory",new{memory=Marshal.ReadByte(schema,0x108),request=Marshal.ReadByte(schema,0x103)});
        Phase("KVT_IV_applies_independent",300,4,4);double pilot=air.Independent.Gauge;
        Check(Marshal.ReadByte(schema,0x108)==1&&Marshal.ReadByte(schema,0x103)==0&&pilot>1&&AllAbove(Pressures(),1),"KVT_IV_applies_four_cylinders_despite_SA47_memory",new{pilot=pilot,tc=Pressures()});
        // Nativecode3 is the first braking stage (215-1 RE table1.2.3:
        // 1.0..1.3kgf/cm2), not a lap position or physical third brake stage.
        Phase("native_KVT_code3_first_brake_stage",100,4,3);
        Check(Marshal.ReadByte(schema,0x103)==0&&air.Independent.Gauge>=1.0&&air.Independent.Gauge<=1.3&&air.Independent.Gauge<pilot-.3&&AllAbove(Pressures(),1),"native_KVT_code3_selects_first_stage_without_SA47_release",new{beforeSecondStage=pilot,firstStage=air.Independent.Gauge,tc=Pressures()});
        Phase("KVT_II_release_with_memory",400,4,2);
        Check(Marshal.ReadByte(schema,0x108)==1&&Marshal.ReadByte(schema,0x103)==1&&AllBelow(Pressures(),.05),"KVT_II_returns_to_retained_manual_release",Pressures());
        Phase("KM_V_blocks_execution",3,5,2);Check(Marshal.ReadByte(schema,0x108)==1&&Marshal.ReadByte(schema,0x103)==0,"KM_V_blocks_execution_preserves_memory",new{memory=Marshal.ReadByte(schema,0x108),request=Marshal.ReadByte(schema,0x103)});
        Phase("KM_IV_returns_to_memory",300,4,2);Check(Marshal.ReadByte(schema,0x103)==1&&AllBelow(Pressures(),.05),"KM_IV_returns_to_retained_manual_release",Pressures());
        raw=0;Phase("priority_explicit_cancel0",300,4,2);Check(Marshal.ReadByte(schema,0x108)==0&&AllAbove(Pressures(),.5),"priority_memory_still_cancels_only_explicit0_or_TM",Pressures());
    }
    static void OperatorEvent(Control control,string method,EventArgs args){
        var type=method=="OnDeactivate"?typeof(Form):typeof(Control);
        type.GetMethod(method,Flags).Invoke(control,new object[]{args});
    }
    static void OperatorState(string name,int expectedRaw,bool memory,bool requested,bool released){
        var pressure=Pressures();
        Check(Marshal.ReadByte(loc,0x8c)==expectedRaw&&Marshal.ReadInt32(schema,0x104)==expectedRaw,name+"_logical_position",new{raw=Marshal.ReadByte(loc,0x8c),processed=Marshal.ReadInt32(schema,0x104)});
        Check(Marshal.ReadByte(schema,0x108)==(memory?1:0)&&Marshal.ReadByte(schema,0x103)==(requested?1:0),name+"_memory_and_request",new{memory=Marshal.ReadByte(schema,0x108),request=Marshal.ReadByte(schema,0x103)});
        Check(released?AllBelow(pressure,.05):AllAbove(pressure,.5),name+"_four_physical_cylinders",pressure);
    }
    static void OperatorCancelAndReady(ComboBox source,string name){
        // Select the real operator control; its SelectedIndexChanged handler
        // supplies the logical command. No processed state or pressure is set.
        source.SelectedIndex=1;RecordPhase(name+"_cancel0",300,4,2);
        OperatorState(name+"_cancel0",0,false,false,false);
        source.SelectedIndex=2;RecordPhase(name+"_ready1",300,4,2);
        OperatorState(name+"_ready1_no_revive",1,false,false,false);
    }
    static void OperatorResetChecks(){
        var source=(ComboBox)Field("sa47Source");var button=(Button)Field("sa47ImpulseButton");
        var leftDown=new MouseEventArgs(MouseButtons.Left,1,2,2,0);var leftUp=new MouseEventArgs(MouseButtons.Left,1,2,2,0);
        OperatorCancelAndReady(source,"operator_initial");

        OperatorEvent(button,"OnMouseDown",leftDown);RecordPhase("operator_capture_down",300,4,2);
        OperatorState("operator_capture_down",2,true,true,true);
        button.Capture=false;OperatorEvent(button,"OnMouseCaptureChanged",EventArgs.Empty);
        RecordPhase("operator_capture_lost",20,4,2);OperatorState("operator_capture_returns1",1,true,true,true);
        OperatorEvent(button,"OnMouseUp",leftUp);RecordPhase("operator_capture_late_up",20,4,2);
        OperatorState("operator_capture_late_up_idempotent",1,true,true,true);
        OperatorCancelAndReady(source,"operator_after_capture");

        OperatorEvent(button,"OnMouseDown",leftDown);RecordPhase("operator_focus_down",300,4,2);
        OperatorState("operator_focus_down",2,true,true,true);
        OperatorEvent(button,"OnLostFocus",EventArgs.Empty);RecordPhase("operator_focus_lost",20,4,2);
        OperatorState("operator_focus_returns1",1,true,true,true);
        OperatorEvent(button,"OnMouseUp",leftUp);RecordPhase("operator_focus_late_up",20,4,2);
        OperatorState("operator_focus_late_up_idempotent",1,true,true,true);
        OperatorCancelAndReady(source,"operator_after_focus");

        OperatorEvent(button,"OnMouseDown",leftDown);RecordPhase("operator_deactivate_down",300,4,2);
        OperatorState("operator_deactivate_down",2,true,true,true);
        OperatorEvent(host,"OnDeactivate",EventArgs.Empty);RecordPhase("operator_form_deactivated",20,4,2);
        OperatorState("operator_deactivate_returns1",1,true,true,true);
        OperatorEvent(button,"OnMouseUp",leftUp);RecordPhase("operator_deactivate_late_up",20,4,2);
        OperatorState("operator_deactivate_late_up_idempotent",1,true,true,true);
        OperatorCancelAndReady(source,"operator_after_deactivate");

        OperatorEvent(button,"OnKeyDown",new KeyEventArgs(Keys.Enter));RecordPhase("operator_enter_down",300,4,2);
        OperatorState("operator_enter_down",2,true,true,true);
        OperatorEvent(button,"OnKeyDown",new KeyEventArgs(Keys.Enter));RecordPhase("operator_enter_repeat",3,4,2);
        OperatorState("operator_enter_repeat_held",2,true,true,true);
        OperatorEvent(button,"OnKeyUp",new KeyEventArgs(Keys.Enter));RecordPhase("operator_enter_up",20,4,2);
        OperatorState("operator_enter_up_returns1",1,true,true,true);
        OperatorEvent(button,"OnKeyUp",new KeyEventArgs(Keys.Enter));RecordPhase("operator_enter_late_up",20,4,2);
        OperatorState("operator_enter_late_up_idempotent",1,true,true,true);
        OperatorCancelAndReady(source,"operator_after_enter");

        // The native button is already held while the explicit source remains
        // selected. Selecting native must not turn that old hold into a press.
        NativeButton(true);Check(Marshal.ReadByte(releaseControl,0x1d9)==1,"operator_handover_physical_button_held",Marshal.ReadByte(releaseControl,0x1d9));
        RecordPhase("operator_native_held_while_explicit_ready",20,4,2);
        OperatorState("operator_explicit_ready_ignores_native_hold",1,false,false,false);
        source.SelectedIndex=0;
        for(int i=0;i<300;i++)Cycle(4,2,1);
        RecordPhase("operator_handover_held_native_ready",0,4,2);
        OperatorState("operator_handover_does_not_issue_new2",1,false,false,false);
        Check(Marshal.ReadByte(releaseControl,0x1d9)==1,"operator_handover_preserves_physical_hold",Marshal.ReadByte(releaseControl,0x1d9));
        NativeButton(false);RecordPhase("operator_native_handover_up",20,4,2);
        OperatorState("operator_native_up_arms_without_release",1,false,false,false);
        Check(Marshal.ReadByte(releaseControl,0x1d9)==0,"operator_native_up_physical0",Marshal.ReadByte(releaseControl,0x1d9));
        NativeButton(true);RecordPhase("operator_native_new_down",300,4,2);
        OperatorState("operator_native_new_press_releases",2,true,true,true);
        NativeButton(false);RecordPhase("operator_native_new_up",20,4,2);
        OperatorState("operator_native_new_up_retains_memory",1,true,true,true);
        OperatorCancelAndReady(source,"operator_final");
        source.SelectedIndex=0;RecordPhase("operator_final_native_idle",20,4,2);
        OperatorState("operator_final_native_idle_no_revive",1,false,false,false);
    }
    static void AdapterChecks(){
        var binding=(TrainerNativeBrakeRelease)Field("nativeBrakeRelease");bool selected;
        Call("ElectricalCycle");using(binding.Borrow(ticks,false,out selected)){Check(selected&&Marshal.ReadByte(loc,0x8c)==1,"fresh_processed_request_is_native_boolean",selected);}
        Check(Marshal.ReadByte(loc,0x8c)==2,"borrow_restores_enum2",Marshal.ReadByte(loc,0x8c));
        using(binding.Borrow(ticks,false,out selected)){Check(!selected&&Marshal.ReadByte(loc,0x8c)==0,"one_capture_not_reused",selected);}
        Call("ElectricalCycle");using(binding.Borrow(ticks+1,false,out selected)){Check(!selected,"wrong_tick_is_neutral",selected);}
        Call("ElectricalCycle");binding.Invalidate();using(binding.Borrow(ticks,false,out selected)){Check(!selected,"invalidated_snapshot_is_neutral",selected);}
        Call("ElectricalCycle");bool repeatedCaptureRejected=false;
        try{binding.Capture(ticks);}catch(Exception ex){repeatedCaptureRejected=ex.Message.Contains("producer did not execute");}
        Check(repeatedCaptureRejected,"capture_without_new_electrical_epoch_rejected",repeatedCaptureRejected);
        using(binding.Borrow(ticks,false,out selected)){Check(!selected&&Marshal.ReadByte(loc,0x8c)==0,"repeated_capture_invalidates_old_true",new{selected=selected,raw=Marshal.ReadByte(loc,0x8c)});}

        Call("ElectricalCycle");var commField=typeof(TrainerIntegrationHost).GetField("commStep",Flags);object originalComm=commField.GetValue(host);
        byte widgetBefore=Marshal.ReadByte(releaseControl,0x1d9);bool noProducerRejected=false;
        Check(Marshal.ReadByte(schema,0x103)==1,"no_op_fixture_starts_with_real_true_request",Marshal.ReadByte(schema,0x103));
        try{
            commField.SetValue(host,Delegate.CreateDelegate(commField.FieldType,typeof(Sa47ReleaseProbe).GetMethod("NoOpElectrical",BindingFlags.Static|BindingFlags.NonPublic)));
            try{Call("ElectricalCycle");}catch(TargetInvocationException ex){noProducerRejected=ex.InnerException!=null&&ex.InnerException.Message.Contains("producer did not execute");}
            Check(noProducerRejected,"no_op_electrical_rejected_without_producer_entry",noProducerRejected);
            Check(Field("nativeReleaseCabInput")==null&&Marshal.ReadByte(releaseControl,0x1d9)==widgetBefore,"no_op_electrical_restores_widget_scope",Marshal.ReadByte(releaseControl,0x1d9));
            using(binding.Borrow(ticks,false,out selected)){Check(!selected&&Marshal.ReadByte(loc,0x8c)==0,"no_op_electrical_cannot_reuse_old_true",new{selected=selected,raw=Marshal.ReadByte(loc,0x8c)});}
        }finally{commField.SetValue(host,originalComm);}
        Check(Marshal.ReadByte(loc,0x8c)==2,"no_op_electrical_neutral_borrow_restores_raw2",Marshal.ReadByte(loc,0x8c));

        Call("ElectricalCycle");bool rejected=false;Marshal.WriteByte(loc,0x8c,3);
        try{binding.Capture(ticks);}catch(Exception){rejected=true;}finally{Marshal.WriteByte(loc,0x8c,2);}
        Check(rejected,"invalid_position_capture_rejected",rejected);
        using(binding.Borrow(ticks,false,out selected)){Check(!selected,"failed_capture_drops_previous_request",selected);}
        Call("ElectricalCycle");byte km=Marshal.ReadByte(loc,0x7d);Marshal.WriteByte(loc,0x7d,5);
        try{using(binding.Borrow(ticks,false,out selected)){Check(!selected,"late_km5_is_neutral",selected);}}finally{Marshal.WriteByte(loc,0x7d,km);}
        Call("ElectricalCycle");byte kvt=Marshal.ReadByte(loc,0x7e);
        Check(kvt==2&&Marshal.ReadByte(schema,0x103)==1,"late_KVTIII_fixture_starts_from_fresh_KVTII_request",new{kvt=kvt,request=Marshal.ReadByte(schema,0x103)});
        Marshal.WriteByte(loc,0x7e,3);
        try{using(binding.Borrow(ticks,false,out selected)){Check(!selected&&Marshal.ReadByte(loc,0x8c)==0,"late_KVTIII_is_neutral_native_input",new{selected=selected,raw=Marshal.ReadByte(loc,0x8c)});}}finally{Marshal.WriteByte(loc,0x7e,kvt);}
        Check(Marshal.ReadByte(loc,0x8c)==2&&Marshal.ReadByte(loc,0x7e)==kvt,"late_KVTIII_restores_original_inputs",new{raw=Marshal.ReadByte(loc,0x8c),kvt=Marshal.ReadByte(loc,0x7e)});
        Call("ElectricalCycle");using(binding.Borrow(ticks,true,out selected)){Check(!selected,"safety_priority_is_neutral",selected);}
        Call("ElectricalCycle");bool bodyException=false;
        try{using(binding.Borrow(ticks,false,out selected)){throw new InvalidOperationException("scope-body-fixture");}}
        catch(InvalidOperationException ex){bodyException=ex.Message=="scope-body-fixture";}
        Check(bodyException&&Marshal.ReadByte(loc,0x8c)==2,"body_exception_restores_enum2",Marshal.ReadByte(loc,0x8c));
        Call("ElectricalCycle");var scope=binding.Borrow(ticks,false,out selected);Marshal.WriteByte(loc,0x8c,0);bool foreignRejected=false;
        try{scope.Dispose();}catch(Exception){foreignRejected=true;}
        Check(foreignRejected&&Marshal.ReadByte(loc,0x8c)==0,"foreign_write_is_preserved",Marshal.ReadByte(loc,0x8c));scope.Dispose();Marshal.WriteByte(loc,0x8c,2);
        Call("ElectricalCycle");var ownerField=typeof(TrainerNativeBrakeRelease).GetField("owner",BindingFlags.Instance|BindingFlags.NonPublic);object oldOwner=ownerField.GetValue(binding);ownerField.SetValue(binding,IntPtr.Zero);bool ownerRejected=false;
        try{binding.Borrow(ticks,false,out selected);}catch(Exception){ownerRejected=true;}finally{ownerField.SetValue(binding,oldOwner);}
        Check(ownerRejected&&Marshal.ReadByte(loc,0x8c)==2,"foreign_expected_owner_rejected_before_write",ownerRejected);
        Call("ElectricalCycle");bool wrongThreadRejected=false;
        var other=new System.Threading.Thread(delegate(){try{binding.Borrow(ticks,false,out selected);}catch(Exception){wrongThreadRejected=true;}});other.Start();other.Join();
        Check(wrongThreadRejected&&Marshal.ReadByte(loc,0x8c)==2,"wrong_thread_rejected_before_write",wrongThreadRejected);
    }
    static void NoOpElectrical(){}
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
    static void ProducerFaultCleanup(){
        Call("ElectricalCycle");var binding=(TrainerNativeBrakeRelease)Field("nativeBrakeRelease");
        var patch=(TrainerSa47MemoryPatch)typeof(TrainerNativeBrakeRelease).GetField("memoryPatch",Flags).GetValue(binding);
        var state=(IntPtr)typeof(TrainerSa47MemoryPatch).GetField("state",Flags).GetValue(patch);
        byte widgetBefore=Marshal.ReadByte(releaseControl,0x1d9),rawBefore=Marshal.ReadByte(loc,0x8c);bool selected;
        var cabScope=binding.BorrowCabInput(mem["CommOwner"],2);var inputScope=binding.Borrow(ticks,false,out selected);
        Check(Marshal.ReadByte(loc,0x8c)==0,"fault_fixture_starts_with_borrowed_neutral0",Marshal.ReadByte(loc,0x8c));
        // Fault fixture in owned private adapter state only; no live pressure,
        // protection, relay or cylinder output is modified.
        Marshal.WriteInt32(state,12,4);
        inputScope.Dispose();cabScope.Dispose();
        Check(Marshal.ReadByte(loc,0x8c)==rawBefore,"producer_fault_restores_owned_raw_input",Marshal.ReadByte(loc,0x8c));
        Check(Marshal.ReadByte(releaseControl,0x1d9)==widgetBefore,"producer_fault_restores_owned_widget",Marshal.ReadByte(releaseControl,0x1d9));
        bool refused=false;try{binding.Capture(ticks);}catch(Exception ex){refused=ex.Message.Contains("producer fault");}
        Check(refused,"producer_fault_cannot_publish_fresh_release",refused);
        binding.Dispose();
        var entry=IntPtr.Add(GetModuleHandle("Pult1.dll"),0x18c7a6);byte[] original={0x8b,0x45,0xfc,0x8b,0x40,0x04};bool restored=true;
        for(int i=0;i<original.Length;i++)restored&=Marshal.ReadByte(entry,i)==original[i];
        Check(restored,"producer_fault_restores_original_entry_before_native_close",restored);
        Check((IntPtr)typeof(TrainerSa47MemoryPatch).GetField("cave",Flags).GetValue(patch)==IntPtr.Zero&&(IntPtr)typeof(TrainerSa47MemoryPatch).GetField("state",Flags).GetValue(patch)==IntPtr.Zero,"producer_fault_frees_owned_allocations_after_restoration",true);
    }
    [STAThread] static int Main(string[] args){
        nativeButtonMode=args.Length>0&&args[0]=="native-button";
        zeroResetMode=args.Length>0&&args[0]=="zero-reset";
        tmResetMode=args.Length>0&&args[0]=="tm-reset";priorityMode=args.Length>0&&args[0]=="priorities";
        producerFaultMode=args.Length>0&&args[0]=="producer-fault";
        operatorMode=args.Length>0&&args[0]=="operator-reset";
        Application.EnableVisualStyles();host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);
        string error=null;
        try{
            var handle=host.Handle;Call("StartNative");var timer=Field("timer") as Timer;if(timer==null)throw new Exception("Native startup did not produce a timer");timer.Stop();
            mem=(Dictionary<string,IntPtr>)Field("mem");loc=mem["TLocParam"];air=(TwoSectionAirChain)Field("airCircuit");
            var module=GetModuleHandle("Pult1.dll");var owner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(module,0x1a2e60));
            if(owner==IntPtr.Zero||Marshal.ReadIntPtr(owner,4)!=mem["TPult"])throw new Exception("Pult owner guard");schema=IntPtr.Add(owner,0x18);
            var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);InstantiateTumba(cab);
            var form=Marshal.ReadIntPtr(cab,0xc);cabForm=form;releaseControl=Marshal.ReadIntPtr(form,0x3e4);
            if(releaseControl==IntPtr.Zero||Marshal.ReadIntPtr(Marshal.ReadIntPtr(form,0x530),4)!=loc)throw new Exception("Cab release wiring guard");
            if(producerFaultMode){ProducerFaultCleanup();}
            else if(args.Length>0&&args[0]=="first-fault"){FirstElectricalFault();}
            else{
            raw=0;Phase("charge",1000,2,2);
            int count=0;while(Read(loc,9)>4.4&&count++<200)Cycle(5,2);
            Check(count<200,"service_step_reached",Read(loc,9));
            Phase("automatic_hold",600,4,2);
            var before=Pressures();Check(AllAbove(before,.5),"four_cylinders_automatically_applied",before);
            Check(Marshal.ReadByte(schema,0x108)==0,"initial_manual_latch_clear",Marshal.ReadByte(schema,0x108));
            if(zeroResetMode){ZeroResetChecks();}
            else if(tmResetMode){TmResetChecks();}
            else if(priorityMode){PriorityChecks();}
            else if(operatorMode){OperatorResetChecks();}
            else if(nativeButtonMode){NativeButtonChecks();}
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
            var result=new{passed=failed==0,failed_assertions=failed,cycles=ticks,assertions=assertions,phases=phases,error=error,scope=producerFaultMode?"Owned private-state fault fixture only. No live TM, relay, protection or cylinder output forced; input scopes and original code restoration tested before native close.":operatorMode?"Prepared warm network and full native cycles. Programmatic invocation of actual managed mouse/capture/focus/deactivate/Enter handlers, ComboBox source selection and guarded native DwnButton/UpBtn calls; not an OS desktop input test. Explicit0-to1 cancellation memory, held-button source handover and all four physical cylinders. No pressure or processed outputs forced; TM calibration, cold actuator behavior and full RE conformity remain outside this scenario.":"Fresh native warm electrical/pneumatic/motion/aux/KLUB cycles; guarded original NativePosition KM/KVT before producer, explicit SA47 input controls or native Dwn/Up. No pressure or processed outputs forced. Selected head-TM cancellation profile is strictly >5.0kgf/cm2. KR22 supply/actuator calibration, fast between-tick clicks and full RE conformity remain outside this scenario."};
            File.WriteAllText(TrainerSession.ResultPath(producerFaultMode?"Trainer_sa47_producer_fault_validation.json":args.Length>0&&args[0]=="first-fault"?"Trainer_sa47_release_fault_validation.json":operatorMode?"Trainer_sa47_operator_reset_validation.json":tmResetMode?"Trainer_sa47_tm_reset_validation.json":priorityMode?"Trainer_sa47_priority_validation.json":zeroResetMode?"Trainer_sa47_zero_reset_validation.json":nativeButtonMode?"Trainer_sa47_native_button_validation.json":"Trainer_sa47_release_validation.json"),new JavaScriptSerializer().Serialize(result));
        }
        Console.WriteLine("SA47 release: "+(failed==0?"PASS":"FAIL")+", cycles="+ticks+", failed="+failed);
        return failed==0?0:1;
    }
}
