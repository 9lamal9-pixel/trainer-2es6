using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Measurement fixture, not an AVP journey or a factory calibration approval.
// Every case has one declared POEZD initial condition before its measured cycle.
// Afterwards only genuine operator inputs are changed. The original profile,
// curves, signal states, native force, pressures and safety outputs are retained.
public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
    const double StartX=57900, GradeStart=56725, GradeEnd=58325, Length=1126.18;
    const double Dt=.1, Standstill=.3/3.6;
    static readonly BindingFlags Flags=BindingFlags.Instance|BindingFlags.NonPublic;
    static readonly List<object> Checks=new List<object>(), Cases=new List<object>();
    static readonly Stopwatch Watch=new Stopwatch();
    static string failure;
    static uint thread;
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void NativeCall();
    [DllImport("kernel32.dll",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module,string name);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module,StringBuilder name,int size);
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    [DllImport("kernel32.dll")] static extern IntPtr GetCurrentProcess();
    [DllImport("kernel32.dll")] static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint type,uint protect);
    [DllImport("kernel32.dll")] static extern bool VirtualProtect(IntPtr address,UIntPtr size,uint protect,out uint previous);
    [DllImport("kernel32.dll")] static extern bool VirtualFree(IntPtr address,UIntPtr size,uint type);
    [DllImport("kernel32.dll")] static extern bool FlushInstructionCache(IntPtr process,IntPtr address,UIntPtr size);
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo { public IntPtr Base,AllocationBase; public uint AllocationProtect; public UIntPtr RegionSize; public uint State,Protect,Type; }
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr address,out MemoryInfo info,UIntPtr size);

    public sealed class Vehicle {
        public int index; public double mass_t,length_m,x_m,v_m_s,a_m_s2,coupler,deformation,grade_permille,curve_radius_m;
    }
    public sealed class Frame {
        public string phase; public int cycle; public double seconds,head_x_m,head_v_kmh,com_v_m_s,maximum_abs_v_m_s;
        public double head_metadata_rear_m,force_native,ur_bar,pm_bar,pcam_bar,loc_tc_bar;
        public byte km,kvt,epk_trip,kon_trip,coil,klub_request,saut_request,rb_hold_warning;
        public int klub_allowed_kmh,klub_target_kmh,klub_period,klub_counter;
        public byte klub_signal,klub_requested_button,klub_first_start,klub_uncoded,klub_ack_in_motion,klub_uncontrolled_motion,source_traction_origin,source_controller_null;
        public bool fully_minus8,automatic_released,fully_released;
        public Vehicle[] vehicles; public double[] tc_bar,tm_bar,bogies_tc_bar;
    }
    sealed partial class Fixture {
        public TrainerIntegrationHost host;
        public Action beforeNativePneumatic,afterNativeMotion;
        TrainerAutovedenieNativeBridge operatorBridge;
        bool operatorBridgeOwned;
        int operatorCommand;
        public Dictionary<string,IntPtr> mem;
        public IntPtr communication,cabForm,tumbaForm,airArray,provider,originalCallback;
        public IntPtr[] motionCars;
        public Func<double>[] bogiePressure;
        public RouteModel route;
        public int cycle,ackRemaining,ackReleaseCycles,ackCount;
        public bool measured,measurementComplete,restored,closed,inputsSet,uiSaved;
        public string phase="startup",error;
        public double speed,reduction,coastRaw,minimumRaw=Double.MaxValue,minimumComponent=Double.MaxValue;
        public int qualifyingWindows,firstGradeExit=-1,preparedInitializations;
        public bool stopped,securedStopComplete;
        public byte oldKm,oldKvt,oldEpk,oldVcu;
        public bool oldRbs,driverUiSaved,oldPermission,oldTdSource,oldTd1,oldTd2,originForceSeen;
        public decimal oldTarget;
        public object originEvidence;
        public int oldSaut;
        public readonly Dictionary<IntPtr,byte> cabChanges=new Dictionary<IntPtr,byte>();
        public readonly List<Frame> rows=new List<Frame>();
        public readonly List<object> windows=new List<object>(),ackEvents=new List<object>();
        public object coastEvidence,brakeEvidence,releaseEvidence,initialEvidence;

        public object Field(string name) {
            var field=typeof(TrainerIntegrationHost).GetField(name,Flags);
            if(field==null)throw new Exception("Missing host field "+name);
            return field.GetValue(host);
        }
        void SetField(string name,object value) { typeof(TrainerIntegrationHost).GetField(name,Flags).SetValue(host,value); }
        void Call(string name,params object[] args) {
            var method=typeof(TrainerIntegrationHost).GetMethod(name,Flags);
            if(method==null)throw new Exception("Missing host method "+name);
            try { method.Invoke(host,args); }
            catch(TargetInvocationException ex) { throw new Exception(name+": "+ex.InnerException,ex.InnerException); }
        }
        void Held(bool value) { Call("SetVigilanceHeld",1,false,value); }
        void Cab(int offset,byte value) {
            var control=Marshal.ReadIntPtr(cabForm,offset); Require(control,0x1da,true);
            string name=ClassName(control);
            if(name!="TTumbler" && name!="TTumbler3Pos" && name!="TButton2ES6")throw new Exception("Unknown native cab input class "+name);
            var address=IntPtr.Add(control,0x1d9);
            if(!cabChanges.ContainsKey(address))cabChanges.Add(address,Marshal.ReadByte(address));
            Marshal.WriteByte(address,value);
        }
        void Handle(int field,double value) { Position(communication,Marshal.ReadIntPtr(cabForm,field),value); }
        void Key(double value) { Position(communication,Marshal.ReadIntPtr(tumbaForm,0x3ac),value); }
        void NoOverrides() {
            if(Field("commandedKm")!=null || Field("fileKm")!=null || (bool)Field("safetyPower") || (bool)Field("momentaryCharge"))
                throw new Exception("Diagnostic input override present");
            if(File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")))throw new Exception("Command file present");
        }
        void Step() {
            if(GetCurrentThreadId()!=thread || Watch.Elapsed.TotalSeconds>280 || cycle>=9000)throw new Exception("Fixture time/thread bound");
            NoOverrides();
            // Explicit scripted operator RBS belongs to this calibration fixture only.
            // Production controls contain no automated acknowledgement.
            if(ackRemaining==0 && ackReleaseCycles>0) { ackReleaseCycles--;Held(false); }
            else if(ackRemaining==0 && Marshal.ReadByte(mem["TOutParamAllTRen"],0x16)==1) {
                ackRemaining=10;ackCount++;Held(true);
                ackEvents.Add(new { cycle,phase,kind="scripted_operator_RBS_request",request=1 });
            }
            Call("PollCommands");Call("BeforeAutovedenieCycle");
            if(operatorBridge!=null)operatorBridge.SubmitDecision(new TrainerAvpDecision {State=TrainerAvpState.Automatic,DriveEnabled=true,SuggestedTractionCommand=operatorCommand});
            Call("ElectricalCycle");Call("AfterAutovedenieElectricalCycle");
            Call("ApplyTestInputs");Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");
            ObservedKlubStep();if(beforeNativePneumatic!=null)beforeNativePneumatic();
            Call("PneumaticCycle");Call("MotionCycle");if(afterNativeMotion!=null)afterNativeMotion();Call("RouteInputCycle");
            SetField("ticks",Convert.ToInt32(Field("ticks"))+1);cycle++;
            if(ackRemaining>0 && --ackRemaining==0) {Held(false);ackReleaseCycles=1;}
            if(Convert.ToInt32(Field("electricalFaults"))!=0)throw new Exception("Native electrical exception");
            if(Marshal.ReadByte(mem["StandaloneSautOutput"],0xe)!=0 || Marshal.ReadByte(mem["StandaloneSautOutput"],0xf)!=0)
                throw new Exception("Declared software SAUT OFF produced a protection request");
            if((bool)Field("routeFinished"))throw new Exception("Route boundary reached");
            if(measured) {
                var frame=Capture();
                if(!frame.fully_minus8 && firstGradeExit<0)firstGradeExit=cycle;
                if(!measurementComplete && !frame.fully_minus8) {
                    rows.Add(frame);throw new Exception("Left the full-consist -8 permille interval before the measurement was complete");
                }
                if(frame.epk_trip!=0 || frame.kon_trip!=0) {
                    rows.Add(frame);throw new Exception("Actual native protection interrupted the physical brake measurement");
                }
                if(Math.Abs(frame.force_native)>.001)throw new Exception("Native traction contaminated coast/brake measurement");
            }
            if(cycle%25==0)rows.Add(Capture());
            if(cycle%10==0)Application.DoEvents();
        }
        void ObservedKlubStep() {
            // Diagnostic replay of the production wrapper, one original firmware step.
            // Only read inside its ordinary borrowed operator-input scopes.
            var context=(TrainerSafetyInputContext)Field("klubInputContext");context.Sync();
            var method=typeof(TrainerIntegrationHost).GetMethod("BorrowStandaloneSautModeInputs",Flags);
            using(var sautLease=(IDisposable)method.Invoke(host,null))
            using(var rbLease=(IDisposable)typeof(TrainerIntegrationHost).GetMethod("BorrowVigilanceOperatorInputs",Flags).Invoke(host,null)) {
                var klubButtons=Marshal.ReadIntPtr(context.SharedRecord,0);Require(klubButtons,92,false);
                var pb=Marshal.ReadIntPtr(klubButtons,0x58);var buttons=Marshal.ReadIntPtr(pb,0);Require(buttons,5,false);
                if(buttons!=mem["TPBBtnPos"]||pb!=mem["TPBBtn"])throw new Exception("Actual KLUB/PB/operator source alias mismatch");
                var kb=(TrainerKlubSafetyBinding)Field("klubSafetyBinding");
                int prior=kb.Pisk?1:0;byte rbs=Marshal.ReadByte(buttons,1);int request=Marshal.ReadByte(kb.Owner,0x131);
                var proc=GetProcAddress((IntPtr)Field("klubModule"),"ProceedKlub");
                if(proc==IntPtr.Zero)throw new Exception("Actual original ProceedKlub absent");
                ((NativeCall)Marshal.GetDelegateForFunctionPointer(proc,typeof(NativeCall)))();
                if(ackRemaining>0 || prior!=0)ackEvents.Add(new {cycle,phase,kind="observed_native_KLUB_input_scope",rb=Marshal.ReadByte(buttons,0),rbs,rbp=Marshal.ReadByte(buttons,2),traction_origin=Marshal.ReadByte(buttons,3),controller_null=Marshal.ReadByte(buttons,4),ack_in_motion=Marshal.ReadByte(kb.Owner,0x130),uncontrolled_motion=Marshal.ReadByte(kb.Owner,0x11b),previous_v=Marshal.ReadInt32(kb.Owner,0xde),request,before_pisk=prior,after_pisk=kb.Pisk?1:0,period=kb.PeriodSeconds,counter=kb.Counter});
            }
            Call("PublishNativeKlubAlsn");
        }
        void Warm(int count) { for(int n=0;n<count;n++)Step();rows.Add(Capture()); }
        Frame Capture() {
            var frame=new Frame {phase=phase,cycle=cycle,seconds=cycle*Dt,vehicles=new Vehicle[76],tc_bar=new double[77],tm_bar=new double[77],bogies_tc_bar=new double[4]};
            var read=(Delegate)Field("readTrain");double momentum=0,max=0;
            bool full=true;
            for(int n=0;n<76;n++) {
                object[] args={0f,0f,0f,0f,0f,n};read.DynamicInvoke(args);
                double x=Convert.ToDouble(args[0]),v=Convert.ToDouble(args[1]),a=Convert.ToDouble(args[3]);
                double grade=Grade(x),radius=Radius(x),mass=n==0?200:80;
                if(!Finite(x)||!Finite(v)||!Finite(a)||!Finite(Convert.ToDouble(args[2]))||!Finite(Convert.ToDouble(args[4])))throw new Exception("Invalid native vehicle state "+n);
                frame.vehicles[n]=new Vehicle {index=n,mass_t=mass,length_m=n==0?31.93:14.59,x_m=x,v_m_s=v,a_m_s2=a,coupler=Convert.ToDouble(args[2]),deformation=Convert.ToDouble(args[4]),grade_permille=grade,curve_radius_m=radius};
                momentum+=mass*v;max=Math.Max(max,Math.Abs(v));
                if(x<GradeStart || x>=GradeEnd || Math.Abs(grade+8)>.000001)full=false;
            }
            frame.head_x_m=frame.vehicles[0].x_m;frame.head_v_kmh=frame.vehicles[0].v_m_s*3.6;frame.com_v_m_s=momentum/6200;frame.maximum_abs_v_m_s=max;
            frame.head_metadata_rear_m=frame.head_x_m-Length;
            frame.fully_minus8=full && frame.head_metadata_rear_m>=GradeStart && frame.head_x_m<GradeEnd;
            bool autoReleased=true;
            for(int n=0;n<77;n++) {
                var p=Marshal.ReadIntPtr(airArray,n*8+4);Require(p,8,false);
                frame.tc_bar[n]=Float(p,4);frame.tm_bar[n]=Float(p,0);Pressure(frame.tc_bar[n]);Pressure(frame.tm_bar[n]);
                if(frame.tc_bar[n]>.1)autoReleased=false;
            }
            for(int n=0;n<4;n++) {frame.bogies_tc_bar[n]=bogiePressure[n]();Pressure(frame.bogies_tc_bar[n]);}
            var loc=mem["TLocParam"];frame.loc_tc_bar=Float(loc,13);Pressure(frame.loc_tc_bar);
            frame.ur_bar=Float(loc,9);frame.pm_bar=Float(loc,5);frame.pcam_bar=Float(loc,0x9f);
            Pressure(frame.ur_bar);Pressure(frame.pm_bar);Pressure(frame.pcam_bar);
            frame.force_native=Float(mem["TResTyga"],0);if(!Finite(frame.force_native))throw new Exception("Invalid native traction");
            frame.km=Marshal.ReadByte(loc,0x7d);frame.kvt=Marshal.ReadByte(loc,0x7e);frame.epk_trip=Marshal.ReadByte(loc,0x9b);frame.kon_trip=Marshal.ReadByte(loc,0x9d);frame.coil=Marshal.ReadByte(loc,0x9a);
            var kb=(TrainerKlubSafetyBinding)Field("klubSafetyBinding");var ko=kb.Owner;
            frame.klub_allowed_kmh=Marshal.ReadInt32(mem["TOutParamAllTRen"],0x17);frame.klub_target_kmh=Marshal.ReadInt32(mem["TOutParamAllTRen"],0x1b);
            frame.klub_ack_in_motion=Marshal.ReadByte(ko,0x130);frame.klub_uncontrolled_motion=Marshal.ReadByte(ko,0x11b);frame.source_traction_origin=Marshal.ReadByte(mem["TPBBtnPos"],3);frame.source_controller_null=Marshal.ReadByte(mem["TPBBtnPos"],4);
            frame.klub_signal=kb.Signal;frame.klub_period=kb.PeriodSeconds;frame.klub_counter=kb.Counter;frame.klub_requested_button=Marshal.ReadByte(ko,0x131);frame.klub_first_start=Marshal.ReadByte(ko,0x12f);frame.klub_uncoded=Marshal.ReadByte(ko,0xa4);
            frame.klub_request=Marshal.ReadByte(mem["TOutParamAllTRen"],0x16);frame.saut_request=Marshal.ReadByte(mem["StandaloneSautOutput"],0xe);frame.rb_hold_warning=Marshal.ReadByte(mem["TOutParamAllTRen"],0x30);
            frame.automatic_released=autoReleased;
            frame.fully_released=autoReleased && frame.loc_tc_bar<=.1 && frame.bogies_tc_bar.All(p=>p<=.1);
            return frame;
        }
        double Grade(double x) { var p=route.Grade(x);if(p==null)throw new Exception("Actual native route profile is missing at "+x);return p.grade_permille; }
        double Radius(double x) { var p=route.curves.FirstOrDefault(c=>x>=route.X(c.km_start)-.000001 && x<route.X(c.km_end)-.000001);return p==null?10000000:Math.Abs(p.radius_m); }
        void GuardModules() {
            string runtime=(string)Field("root");
            string[] names={"Communication1.dll","Pult1.dll","Tyga1.dll","TormModel.dll","POEZD.DLL"};
            string[] sha={"27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F","7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5","8EC6A82E618A1D75CBB31242784084B530BB87308E601DEFC5263D5279270491","F871640C5B7699901E3AFBF755BB2B797B659524C29BAA046B311CA6ECC875B9","822B6D17CE45F80179A512EE6C70A35D5AE29BF187530BDA5EBD11446CFB3553"};
            for(int n=0;n<names.Length;n++) {
                var module=GetModuleHandle(names[n]);var path=new StringBuilder(1024);
                if(module==IntPtr.Zero || GetModuleFileName(module,path,path.Capacity)==0)throw new Exception("Actual module absent "+names[n]);
                string expected=Path.GetFullPath(Path.Combine(runtime,n<3?@"RESOURCES\LOCOMOTIVES\1\"+names[n]:@"RESOURCES\"+names[n]));
                Check("Loaded module guard "+names[n],String.Equals(expected,path.ToString(),StringComparison.OrdinalIgnoreCase)&&Hash(path.ToString())==sha[n],sha[n],path.ToString());
            }
            communication=GetModuleHandle("Communication1.dll");Guard(communication,0x2ae994,"558BEC83C4F08855");Guard(communication,0x2a4944,"558BEC83C4F08945");
        }
        public void Start() {
            host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);
            SetField("nativeMarkupEnabled",true);var handle=host.Handle;Call("StartNative");
            var timer=(Timer)Field("timer");if(timer==null)throw new Exception("Native host startup failed");timer.Stop();
            mem=(Dictionary<string,IntPtr>)Field("mem");motionCars=(IntPtr[])Field("cars");route=(RouteModel)Field("route");
            Check("Original native profile selected",route.native_axis && Field("markupSource")!=null,true,route.source_name);
            var g=route.Grade(StartX);Check("Original -8 interval",g!=null&&Math.Abs(route.X(g.km_start)-GradeStart)<.000001&&Math.Abs(route.X(g.km_end)-GradeEnd)<.000001&&g.grade_permille==-8,"56725..58325/-8",g);
            Check("Confirmed candidate model",motionCars.Length==76 && Marshal.ReadInt32((IntPtr)Field("trainData"))==77,"76motion/77air/6200t",Field("avpUnavailable"));
            airArray=Marshal.ReadIntPtr((IntPtr)Field("trainData"),0x42);Require(airArray,77*8,false);
            double mass=0;for(int n=0;n<77;n++) {var p=Marshal.ReadIntPtr(airArray,n*8+4);Require(p,0x29,false);mass+=Marshal.ReadInt32(p,0x25);}
            Check("Actual full pneumatic mass",mass==6200,6200,mass);
            bogiePressure=(Func<double>[])Field("avpBogiePressures");Check("Four measured bogie pressures",bogiePressure!=null&&bogiePressure.Length==4,4,bogiePressure==null?0:bogiePressure.Length);
            GuardModules();NoOverrides();
            var poezd=GetModuleHandle("POEZD.DLL");
            for(int n=1;n<76;n++)Check("Composite wagon configuration "+n,Marshal.ReadInt32(poezd,0x1ad7c+n*4)==0 && Marshal.ReadByte(motionCars[n],0x18)==2 && Marshal.ReadByte(motionCars[n],0x19)==0 && Marshal.ReadByte(motionCars[n],0x1a)==1,"FTk0/R2/Ro0/AR1",new {ftk=Marshal.ReadInt32(poezd,0x1ad7c+n*4),r=Marshal.ReadByte(motionCars[n],0x18),ro=Marshal.ReadByte(motionCars[n],0x19),ar=Marshal.ReadByte(motionCars[n],0x1a)});
            Check("Legacy AVP calibration refused",Field("avpController")==null && ((string)Field("avpUnavailable")).Contains("композиционные"),"controller absent / new calibration required",Field("avpUnavailable"));
            var owner=mem["CommOwner"];Require(owner,0x2c0,false);var cab=Marshal.ReadIntPtr(owner,0x2bc);
            Check("Actual cab owner",ClassName(cab)=="TPultMash2ES6","TPultMash2ES6",ClassName(cab));cabForm=Marshal.ReadIntPtr(cab,0xc);Require(cabForm,0x534,false);
            Check("Actual cab input alias",Marshal.ReadIntPtr(cabForm,0x530)==mem["TPult"],true,true);
            Register(communication,cab,1,0x2ae994);var tumba=Marshal.ReadIntPtr(cab,0x10);Check("Actual tumba owner",ClassName(tumba)=="TPult_Tumba_2ES6","TPult_Tumba_2ES6",ClassName(tumba));
            tumbaForm=Marshal.ReadIntPtr(tumba,0xc);Require(tumbaForm,0x3b8,false);Check("Actual tumba alias",Marshal.ReadIntPtr(tumbaForm,0x3b4)==mem["TPult"],true,true);
            provider=Marshal.ReadIntPtr(owner,0x264);Require(provider,12,false);originalCallback=Marshal.ReadIntPtr(provider,8);
            var loc=mem["TLocParam"];oldKm=Marshal.ReadByte(loc,0x7d);oldKvt=Marshal.ReadByte(loc,0x7e);oldEpk=Marshal.ReadByte(loc,0x99);oldVcu=Marshal.ReadByte(loc,0x8f);inputsSet=true;
            oldRbs=((CheckBox)Field("vigilanceSoftwareInputs")).Checked;oldSaut=((ComboBox)Field("standaloneSautChoice")).SelectedIndex;uiSaved=true;
            ((ComboBox)Field("standaloneSautChoice")).SelectedIndex=3;((CheckBox)Field("vigilanceSoftwareInputs")).Checked=true;
            Check("Explicit software RBS source available",((CheckBox)Field("vigilanceSoftwareInputs")).Checked,true,true);
            Key(1);Position(communication,Marshal.ReadIntPtr(tumbaForm,0x3a4),1);Handle(0x3b0,2);Handle(0x3b4,3);
            Cab(0x3a0,1);Cab(0x3a4,1);Cab(0x3dc,0);Cab(0x3c4,0);Cab(0x3c8,0);Cab(0x3bc,1);Cab(0x3fc,1);Cab(0x3e0,1);
            phase="prepared_pantographs";Warm(10);Cab(0x3e0,0);Warm(290);
            Cab(0x3dc,1);phase="prepared_BV";Warm(150);Cab(0x3dc,0);Warm(10);Cab(0x3dc,1);Warm(200);
            Cab(0x3c4,1);phase="prepared_fans";Warm(160);Cab(0x3c8,0);Warm(10);Cab(0x3c8,1);phase="prepared_compressors";Warm(50);
            phase="prepared_EPK_recharge";Key(0);
            for(int n=0;n<200&&(Float(loc,0x9f)<=3.5||Marshal.ReadByte(loc,0x9b)!=0);n++)Step();
            Check("Actual EPK recharged before key ON",Float(loc,0x9f)>3.5&&Marshal.ReadByte(loc,0x9b)==0,">3.5/trip0",Capture());
            Key(1);Warm(1);ackRemaining=10;Held(true);Warm(10);Held(false);ackRemaining=0;ackReleaseCycles=1;Warm(40);
            Check("Actual relay available",Marshal.ReadByte(loc,0x9a)==1&&Marshal.ReadByte(loc,0x9b)==0,"coil1/trip0",Capture());
            Handle(0x3b4,2);Cab(0x3e0,1);phase="all_brakes_released_before_declared_initial_state";
            for(int n=0;n<1200&&!Capture().fully_released;n++)Step();
            var released=Capture();Check("All TC including KVT genuinely released before initialization",released.fully_released,true,released);
            Check("Charged native UR before initialization",released.ur_bar>4.8,">4.8",released.ur_bar);
            Check("No traction before initialization",Math.Abs(released.force_native)<.001,"abs(force)<.001",released.force_native);
            var bridge=(TrainerAutovedenieNativeBridge)Field("avpNative");bool ownsDrive=(bool)Field("avpInitialized") && bridge!=null && bridge.OwnsCommands;
            Check("Calibration does not own AVP drive",!ownsDrive,false,ownsDrive);
        }
        TrainerAutovedenieInterface DriverUi() {return (TrainerAutovedenieInterface)Field("avpUi");}
        int DriverWindow() {var monitors=(IntPtr[])typeof(TrainerAutovedenieInterface).GetField("monitors",Flags).GetValue(DriverUi());return Marshal.ReadByte(monitors[0],0x14);}
        void ScreenKey(byte code) {Call("SendMpsudKey",(byte)0);Step();Call("SendMpsudKey",code);Step();Call("SendMpsudKey",(byte)0);Step();}
        void ScreenDigit(int digit) {ScreenKey((byte)(16-digit));}
        void SelectField(int field) {for(int n=0;n<8&&DriverUi().ReadSettings().SelectedField!=field;n++){int current=DriverUi().ReadSettings().SelectedField;ScreenKey((byte)(current<field?5:6));}if(DriverUi().ReadSettings().SelectedField!=field)throw new Exception("SC09 field selection failed");}
        void ScreenNumber(string value) {foreach(char c in value)ScreenDigit(c-'0');ScreenKey(1);}
        void GenuineNativeStart() {
            phase="genuine_native_traction_origin_preparation";
            oldPermission=((CheckBox)Field("avpMovementPermission")).Checked;oldTdSource=((CheckBox)Field("avpSoftwareTd")).Checked;
            oldTd1=((CheckBox)Field("avpTd1")).Checked;oldTd2=((CheckBox)Field("avpTd2")).Checked;oldTarget=((NumericUpDown)Field("avpTargetLimit")).Value;driverUiSaved=true;
            ((CheckBox)Field("avpMovementPermission")).Checked=true;((CheckBox)Field("avpSoftwareTd")).Checked=true;
            ((CheckBox)Field("avpTd1")).Checked=true;((CheckBox)Field("avpTd2")).Checked=true;((NumericUpDown)Field("avpTargetLimit")).Value=20;
            Check("Native driver initial clock screen",DriverWindow()==1,1,DriverWindow());ScreenDigit(2);ScreenDigit(3);
            Check("Native settings screen for traction origin",DriverWindow()==10,10,DriverWindow());
            SelectField(0);ScreenKey(14);SelectField(1);ScreenDigit(2);SelectField(2);ScreenNumber("123");
            SelectField(3);ScreenNumber("6000");SelectField(4);ScreenNumber("75");SelectField(5);ScreenDigit(0);ScreenKey(1);
            var settings=DriverUi().ReadSettings();Check("Native prepared train settings committed",settings.Valid&&settings.Mode==0&&settings.MassT==6000&&settings.Wagons==75,true,settings.Reason);
            Cab(0x3e0,0);Warm(1);
            ((NumericUpDown)Field("avpTargetLimit")).Value=80;ScreenDigit(5);
            var highSpeed=(TrainerAvpDecision)Field("avpDecision");
            Check("Changed-consist AVP admission refused",highSpeed!=null&&!highSpeed.DriveEnabled&&highSpeed.Reason.Contains("композиционные"),"legacy calibration unavailable",highSpeed==null?"null":highSpeed.Reason);
            ((NumericUpDown)Field("avpTargetLimit")).Value=20;
            var inactive=(TrainerAutovedenieNativeBridge)Field("avpNative");
            Check("No production AVP bridge owns diagnostic commands",Field("avpController")==null && (inactive==null || !inactive.OwnsCommands),"controller absent / bridge inactive",new {controller=Field("avpController")!=null,bridge=inactive!=null,owns=inactive!=null&&inactive.OwnsCommands});
            operatorBridgeOwned=inactive==null;
            operatorBridge=inactive ?? TrainerAutovedenieNativeBridge.Attach(mem["CommOwner"],mem["TPult"],mem["TLocParam"],reason=>{throw new Exception("Diagnostic native operator path cancelled: "+reason);});
            operatorBridge.SetOperatorInputSource(ptr=>(IDisposable)typeof(TrainerIntegrationHost).GetMethod("AutovedenieOperatorInputScope",Flags).Invoke(host,new object[]{ptr}));
            operatorBridge.SetAdditionalSafetySource(()=>(string)typeof(TrainerIntegrationHost).GetMethod("StandaloneSautSafetyReason",Flags).Invoke(host,null));
            Cab(0x3e0,0);phase="genuine_native_operator_start_on_full_markup";
            int start=cycle;
            // LTD admits different body speeds during the start wave. Reaching
            // 3km/h at the locomotive alone is not a settled consist origin.
            // Strengthen this fixture's physical start condition; do not prepare
            // velocities or tune the production couplers to an old POEZD trace.
            for(int n=0;n<3000&&!GenuineOriginSpeedReached(Capture());n++) {
                operatorCommand=n%10==0?1:0;Step();var f=Capture();if(f.force_native>0)originForceSeen=true;
                if(operatorBridge.Fault.Length!=0 || operatorBridge.CancellationLatched)throw new Exception("Native operator source failed: "+operatorBridge.Fault);
                if(f.epk_trip!=0||f.kon_trip!=0)throw new Exception("Protection rejected genuine native traction start");
            }
            var moving=Capture();Check("Actual native traction start reached 3km/h",originForceSeen&&moving.head_v_kmh>=3,true,moving);
            Check("Native uncontrolled-start protection not triggered",moving.klub_uncontrolled_motion==0,0,moving.klub_uncontrolled_motion);
            operatorCommand=5;Step();operatorBridge.ReleaseCommands();if(operatorBridgeOwned)operatorBridge.Dispose();operatorBridge=null;
            Cab(0x3e0,1);phase="actual_native_coast_after_qualified_start";Warm(30);var coast=Capture();
            Check("Actual native force removed before declared scenario",Math.Abs(coast.force_native)<.001&&coast.head_v_kmh>2,"force0 / speed>2",coast);
            originEvidence=new {start_cycle=start,end_cycle=cycle,moving,coast,positive_native_force_observed=originForceSeen,source="Diagnostic operator pulses through guarded original bridge/Pult/Tyga/POEZD; production AVP disabled, full source; no RK/permission/force output assignments"};
        }
        bool GenuineOriginSpeedReached(Frame frame) {
            return Field("managedMotion")!=null ? frame.vehicles.All(v=>v.v_m_s*3.6>=5) : frame.head_v_kmh>=3;
        }
        public void Measure() {
            GenuineNativeStart();
            phase="declared_prepared_initial_state";
            // Single prepared initial condition, not the UI SetInitialSpeed helper.
            // POEZD SetInitState consumes km/h (native multiply by 1/3.6); GetCurVal returns m/s.
            ((Delegate)Field("stateTrain")).DynamicInvoke((float)StartX,(float)speed,2,76);preparedInitializations++;
            // GetCurVal position is published by the first real model step.
            // Host MotionCycle then publishes only measured native position/velocity.
            Call("MotionCycle");
            SetField("routeFinished",false);SetField("klubBasisReady",false);
            var source=(TrainerMarkupSignalSource)Field("markupSource");source.ResetPosition(StartX);Call("RouteInputCycle");
            var initial=Capture();initialEvidence=initial;rows.Add(initial);
            Check("One declared initialization",preparedInitializations==1,1,preparedInitializations);
            Check("Declared native speed in all 76 vehicles",initial.vehicles.All(v=>Math.Abs(v.v_m_s-speed/3.6)<.02),speed/3.6,initial.vehicles.Select(v=>v.v_m_s).ToArray());
            Check("First native position publication",initial.head_x_m>StartX&&initial.head_x_m<StartX+1,"57900..57901",initial.head_x_m);
            Check("Initial full consist in -8",initial.fully_minus8,true,initial);
            Check("Initial pneumatic and KVT release",initial.fully_released,true,initial);
            measured=true;phase="full_minus8_native_coast";Step();var coastStart=Capture();
            Check("First MotionCycle preserves the declared actual km/h speed",Math.Abs(coastStart.head_v_kmh-speed)<.2,speed,coastStart.head_v_kmh);
            Check("First MotionCycle speed sensor is the actual native velocity",Math.Abs(Float(mem["TInputParamPult"],1)-Math.Abs(coastStart.head_v_kmh))<.001,Math.Abs(coastStart.head_v_kmh),Float(mem["TInputParamPult"],1));
            for(int n=0;n<100;n++) {Step();if(!Capture().fully_released)throw new Exception("Brake pressure contaminated the coast baseline");}
            var coastEnd=Capture();double seconds=(coastEnd.cycle-coastStart.cycle)*Dt;
            coastRaw=(coastStart.com_v_m_s-coastEnd.com_v_m_s)/seconds;
            Check("Finite full-grade coast sample",Finite(coastRaw)&&coastStart.fully_minus8&&coastEnd.fully_minus8,true,coastRaw);
            coastEvidence=new {start=coastStart,end=coastEnd,seconds,raw_com_deceleration_m_s2=coastRaw,gravity_sign="Negative deceleration means downhill acceleration; never clamp to zero"};
            rows.Add(coastEnd);
            double urStart=coastEnd.ur_bar;Handle(0x3b0,5);phase="physical_KMV_to_declared_UR_reduction";
            int vStart=cycle;for(int n=0;n<600&&urStart-Float(mem["TLocParam"],9)<reduction;n++)Step();
            var atHold=Capture();double actualReduction=urStart-atHold.ur_bar;
            Check("Actual UR reduction achieved",actualReduction>=reduction,reduction,actualReduction);
            Handle(0x3b0,4);phase="physical_KMIV_full_minus8_settled_windows";
            int holdStart=cycle,windowStart=cycle,still=0;
            var windowFirst=Capture();var pressureMin=Pressures(windowFirst);var pressureMax=(double[])pressureMin.Clone();
            bool entireWindowFull=true;Frame end=windowFirst;
            for(int n=0;n<3000;n++) {
                Step();end=Capture();var values=Pressures(end);
                for(int p=0;p<values.Length;p++) {pressureMin[p]=Math.Min(pressureMin[p],values[p]);pressureMax[p]=Math.Max(pressureMax[p],values[p]);}
                entireWindowFull &= end.fully_minus8;
                still=end.maximum_abs_v_m_s<=Standstill?still+1:0;
                if(cycle-windowStart>=100) {
                    double dt=(cycle-windowStart)*Dt,raw=(windowFirst.com_v_m_s-end.com_v_m_s)/dt,component=raw-coastRaw;
                    double spread=pressureMax.Zip(pressureMin,(a,b)=>a-b).Max();
                    bool settled=pressureMin.All(p=>p>.2)&&spread<=.05;
                    bool accepted=entireWindowFull&&windowFirst.fully_minus8&&end.fully_minus8&&settled&&Finite(raw)&&Finite(component)&&raw>0&&component>0;
                    windows.Add(new {requested_ur_reduction_bar=reduction,start=windowFirst,end,seconds=dt,all_82_pressure_min=pressureMin,all_82_pressure_max=pressureMax,maximum_pressure_range_bar=spread,all_pressures_settled=settled,entire_window_full_minus8=entireWindowFull,raw_com_deceleration_m_s2=raw,coast_subtracted_brake_component_m_s2=component,raw_head_deceleration_m_s2=(windowFirst.vehicles[0].v_m_s-end.vehicles[0].v_m_s)/dt,raw_tail_deceleration_m_s2=(windowFirst.vehicles[75].v_m_s-end.vehicles[75].v_m_s)/dt,accepted});
                    if(accepted) {qualifyingWindows++;minimumRaw=Math.Min(minimumRaw,raw);minimumComponent=Math.Min(minimumComponent,component);}
                    windowStart=cycle;windowFirst=end;pressureMin=Pressures(end);pressureMax=(double[])pressureMin.Clone();entireWindowFull=true;
                }
                if(still>=20) {stopped=true;break;}
            }
            brakeEvidence=new {requested_ur_reduction_bar=reduction,measured_ur_reduction_bar=actualReduction,ur_start_bar=urStart,at_hold=atHold,V_seconds=(holdStart-vStart)*Dt,IV_seconds=(cycle-holdStart)*Dt,stop=end,stop_distance_from_brake_start_m=end.head_x_m-coastEnd.head_x_m,qualifyingWindows};
            Check("Native whole-consist stop inside the full -8 interval",stopped&&end.fully_minus8,true,end);
            Check("At least one all-vehicle settled full -8 brake window",qualifyingWindows>0,">0",qualifyingWindows);
            measurementComplete=true;rows.Add(end);
            // Explicit independent locomotive brake during automatic brake release.
            // It is a physical operator action, not a pressure/force assignment.
            phase="physical_KVTVI_full_charge_before_KMII_release";Handle(0x3b4,6);
            for(int n=0;n<300&&Capture().bogies_tc_bar.Min()<3.8;n++)Step();
            var independentCharged=Capture();Check("Actual four-bogie KVTVI fully charged before automatic release",independentCharged.bogies_tc_bar.Min()>=3.8,">=3.8bar / native nominal3.9",independentCharged);
            // Accepted secured-stop scope keeps the genuine train step AND full auxiliary brake.
            phase="physical_KMIV_and_KVTVI_secured_hold";
            var firstHold=Capture();for(int n=0;n<600;n++){Step();var f=Capture();if(Math.Abs(f.com_v_m_s)>=Standstill||f.maximum_abs_v_m_s>=Standstill)throw new Exception("Train moved during first IV+VI secured hold");}
            var securedEnd=Capture();Check("Whole-consist secured stop with IV and full VI for60s",securedEnd.maximum_abs_v_m_s<Standstill&&Math.Abs(securedEnd.com_v_m_s)<Standstill,"allvelocity<0.3km/h for60s",securedEnd);
            phase="diagnostic_KVTVI_during_KMII_automatic_release";Handle(0x3b0,2);
            int releaseStart=cycle;Frame movement=null;var releaseEnd=Capture();
            for(int n=0;n<1400;n++) {
                Step();releaseEnd=Capture();
                if(Math.Abs(releaseEnd.com_v_m_s)>=Standstill||releaseEnd.maximum_abs_v_m_s>=Standstill){movement=releaseEnd;break;}
                if(releaseEnd.automatic_released)break;
            }
            bool independentHoldConfirmed=movement==null&&releaseEnd.automatic_released;
            if(independentHoldConfirmed) {
                phase="diagnostic_full_VI_only_hold";
                for(int n=0;n<600;n++){Step();releaseEnd=Capture();if(Math.Abs(releaseEnd.com_v_m_s)>=Standstill||releaseEnd.maximum_abs_v_m_s>=Standstill){movement=releaseEnd;independentHoldConfirmed=false;break;}}
            }
            Frame repeatStop=null;
            if(movement!=null) {
                // Rules16.1.1: real operator reapplication after movement during release.
                // This is a second operator action, not an automatic AVP acceptance.
                phase="physical_reapplication_after_release_movement";double repeatUrStart=Float(mem["TLocParam"],9);Handle(0x3b0,5);
                for(int n=0;n<600&&repeatUrStart-Float(mem["TLocParam"],9)<.7;n++)Step();
                Check("Actual repeat UR reduction after movement",repeatUrStart-Float(mem["TLocParam"],9)>=.7,">=0.7",repeatUrStart-Float(mem["TLocParam"],9));Handle(0x3b0,4);
                phase="physical_KMIV_and_KVTVI_after_reapplication";int stable=0;
                for(int n=0;n<2000;n++){Step();var f=Capture();stable=f.maximum_abs_v_m_s<Standstill&&Math.Abs(f.com_v_m_s)<Standstill?stable+1:0;if(stable>=20){repeatStop=f;break;}}
                Check("Reapplication stops all actual vehicle motion",repeatStop!=null,true,repeatStop);
                for(int n=0;n<600;n++){Step();var f=Capture();if(Math.Abs(f.com_v_m_s)>=Standstill||f.maximum_abs_v_m_s>=Standstill)throw new Exception("Train moved during repeated IV+VI secured hold");}
                repeatStop=Capture();Check("Repeated whole-consist secured hold for60s",repeatStop.maximum_abs_v_m_s<Standstill&&Math.Abs(repeatStop.com_v_m_s)<Standstill,"allvelocity<0.3km/h for60s",repeatStop);
            } else Check("Full VI-only hold observed for60s",independentHoldConfirmed,true,releaseEnd);
            // Keep secured brakes through native close. Complete pressure release is a separate pneumatic bench case.
            securedStopComplete=true;
            releaseEvidence=new {start_cycle=releaseStart,end_cycle=cycle,seconds=(cycle-releaseStart)*Dt,first_secured_hold_start=firstHold,first_secured_hold_end=securedEnd,release_observation_end=releaseEnd,movement_detected=movement!=null,movement,independent_hold_confirmed=independentHoldConfirmed,repeat_stop=repeatStop,secured_stop_confirmed=true,full_pressure_release_accepted=false,independent_operator_hold_used=true,independent_hold_native_position=6,independent_hold_nominal_bar=3.9,hold_charged=independentCharged,grade_exit_cycle=firstGradeExit};rows.Add(Capture());
        }
        public void Close() {
            if(host==null)return;
            try {
                if(mem!=null) {
                    if(operatorBridge!=null){operatorBridge.ReleaseCommands();if(operatorBridgeOwned)operatorBridge.Dispose();operatorBridge=null;}
                    Held(false);ackRemaining=0;Call("CloseAutovedenie");
                    foreach(var item in cabChanges)Marshal.WriteByte(item.Key,item.Value);
                    foreach(var item in cabChanges)Check("Cab input restored "+item.Key.ToString("X"),Marshal.ReadByte(item.Key)==item.Value,item.Value,Marshal.ReadByte(item.Key));
                    if(inputsSet) {Handle(0x3b0,oldKm);Handle(0x3b4,oldKvt);Key(oldEpk);Position(communication,Marshal.ReadIntPtr(tumbaForm,0x3a4),oldVcu);}
                    if(driverUiSaved) {((CheckBox)Field("avpMovementPermission")).Checked=oldPermission;((CheckBox)Field("avpSoftwareTd")).Checked=oldTdSource;((CheckBox)Field("avpTd1")).Checked=oldTd1;((CheckBox)Field("avpTd2")).Checked=oldTd2;((NumericUpDown)Field("avpTargetLimit")).Value=oldTarget;}
                    if(uiSaved) {((CheckBox)Field("vigilanceSoftwareInputs")).Checked=oldRbs;((ComboBox)Field("standaloneSautChoice")).SelectedIndex=oldSaut;}
                    Call("RestoreVigilanceOperatorInputs");
                    if(provider!=IntPtr.Zero)Check("Production Pult callback restored",Marshal.ReadIntPtr(provider,8)==originalCallback,originalCallback.ToString("X"),Marshal.ReadIntPtr(provider,8).ToString("X"));
                    restored=true;
                }
            } catch(Exception ex) {error=(error??"")+"\nRESTORATION: "+ex;}
            try {host.Close();host.Dispose();closed=true;}catch(Exception ex){error=(error??"")+"\nCLOSE: "+ex;}
        }
        public object Result() {
            return new {declared_case=new {head_x_m=StartX,speed_kmh=speed,native_velocity_argument_kmh=speed,expected_actual_velocity_m_s=speed/3.6,requested_ur_reduction_bar=reduction,prepared_initializations=preparedInitializations},passed=error==null&&stopped&&qualifyingWindows>0&&securedStopComplete&&restored&&closed,error,genuine_native_origin_start=originEvidence,coast=coastEvidence,brake=brakeEvidence,secured_stop_and_release_observation=releaseEvidence,initial=initialEvidence,qualifying_windows=qualifyingWindows,minimum_raw_minus8_deceleration_m_s2=qualifyingWindows>0?(double?)minimumRaw:null,minimum_coast_subtracted_brake_component_m_s2=qualifyingWindows>0?(double?)minimumComponent:null,first_grade_exit_cycle=firstGradeExit,restored,closed,scripted_operator_RBS_count=ackCount,scripted_operator_RBS_events=ackEvents,windows,rows};
        }
    }
    static double[] Pressures(Frame f) { return f.tc_bar.Concat(new[]{f.loc_tc_bar}).Concat(f.bogies_tc_bar).ToArray(); }
    static bool Finite(double v) {return !Double.IsNaN(v)&&!Double.IsInfinity(v);}
    static void Pressure(double v) {if(!Finite(v)||v<-.001||v>20)throw new Exception("Invalid measured pressure "+v);}
    static float Float(IntPtr p,int offset) {var bytes=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),bytes,0,4);return BitConverter.ToSingle(bytes,0);}
    static string Hash(string path) {using(var h=SHA256.Create())using(var stream=File.OpenRead(path))return BitConverter.ToString(h.ComputeHash(stream)).Replace("-","");}
    static void Check(string name,bool passed,object expected,object actual) {Checks.Add(new{name,passed,expected,actual});if(!passed)throw new Exception(name+" expected="+expected+" actual="+actual);}
    static void Require(IntPtr p,int count,bool writable) {
        MemoryInfo m;if(p==IntPtr.Zero||VirtualQuery(p,out m,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)throw new Exception("Native pointer absent");
        ulong a=unchecked((uint)p.ToInt32()),b=unchecked((uint)m.Base.ToInt32());uint protection=m.Protect&0xff;
        if(m.State!=0x1000||(m.Protect&0x101)!=0||a<b||a+(uint)count>b+m.RegionSize.ToUInt32()||(writable&&protection!=4&&protection!=8&&protection!=0x40&&protection!=0x80))throw new Exception("Native memory range/protection guard");
    }
    static string ClassName(IntPtr p) {Require(p,4,false);var vmt=Marshal.ReadIntPtr(p);Require(IntPtr.Add(vmt,-56),60,false);var name=Marshal.ReadIntPtr(vmt,-56);Require(name,1,false);int length=Marshal.ReadByte(name);if(length<2||length>100)throw new Exception("Native class-name length");Require(name,length+1,false);var bytes=new byte[length];Marshal.Copy(IntPtr.Add(name,1),bytes,0,length);return Encoding.ASCII.GetString(bytes);}
    static void Guard(IntPtr module,int rva,string expected) {var bytes=new byte[expected.Length/2];var address=IntPtr.Add(module,rva);Require(address,bytes.Length,false);Marshal.Copy(address,bytes,0,bytes.Length);if(BitConverter.ToString(bytes).Replace("-","")!=expected)throw new Exception("Native ABI bytes "+rva.ToString("X"));}
    static void Invoke(List<byte> code) {
        var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)128,0x3000,4);if(p==IntPtr.Zero)throw new Exception("Private UI thunk allocation");
        try {Marshal.Copy(code.ToArray(),0,p,code.Count);uint old;if(!VirtualProtect(p,(UIntPtr)128,0x20,out old)||!FlushInstructionCache(GetCurrentProcess(),p,(UIntPtr)code.Count))throw new Exception("UI thunk RX/cache guard");((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}
        finally {if(!VirtualFree(p,UIntPtr.Zero,0x8000))throw new Exception("UI thunk restoration/free");}
    }
    static void Register(IntPtr module,IntPtr self,int value,int rva) {var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(self.ToInt32()));code.Add(0xba);code.AddRange(BitConverter.GetBytes(value));code.Add(0xb9);code.AddRange(BitConverter.GetBytes(IntPtr.Add(module,rva).ToInt32()));code.AddRange(new byte[]{0xff,0xd1,0xc3});Invoke(code);}
    static void Position(IntPtr module,IntPtr control,double value) {
        Require(control,0x229,true);if(ClassName(control)!="TMultiPosTumbler")throw new Exception("Physical handle class guard");
        var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(control.ToInt32()));var d=BitConverter.GetBytes(value);code.Add(0x68);code.AddRange(new[]{d[4],d[5],d[6],d[7]});code.Add(0x68);code.AddRange(new[]{d[0],d[1],d[2],d[3]});code.AddRange(new byte[]{0x6a,0,0x6a,0});code.Add(0xb9);code.AddRange(BitConverter.GetBytes(IntPtr.Add(module,0x2a4944).ToInt32()));code.AddRange(new byte[]{0xff,0xd1,0xc3});Invoke(code);
    }
    static void Save(bool passed,double speed) {
        File.WriteAllText(TrainerSession.ResultPath("Autovedenie_adverse_brake.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(new {passed,error=failure,checks=Checks,cases=Cases,declared_profile=new {start_m=GradeStart,end_m=GradeEnd,grade_permille=-8,train_length_m=Length,mass_t=6200,wagons=75,motion_vehicles=76,pneumatic_elements=77},tested_speed_kmh=speed,calibration_approved=false,scope="Original full native markup profile and curves; declared prepared speed before measured cycles; real KM/KVT/RBS operator inputs. Same full-consist -8 coast and brake samples. Raw COM deceleration and coast-subtracted component are measurements, not an automatic config update or a uniform speed/curve/whole-route bound. All75 wagons use composite FTk0/medium R2/AR1; this low-speed case does not prove 80km/h braking. Original signals retained, explicit software SAUT OFF; no AVP journey claim. Secured-stop IV+VI60s is accepted separately from diagnosticII release and real reapplication after movement; no independent-only hold or normativeI→II release acceptance. Release geography is reported separately; no +8 window can qualify."}));
    }
    [STAThread] public static int Main(string[] args) {
        Application.EnableVisualStyles();thread=GetCurrentThreadId();Watch.Start();int exit=0;double speed=20;
        try {if(args.Length>0)speed=Double.Parse(args[0],CultureInfo.InvariantCulture);if(speed!=20)throw new Exception("This first measurement fixture supports the declared 20km/h prepared case only");Check("x86 ABI",IntPtr.Size==4,4,IntPtr.Size);}
        catch(Exception ex){failure=ex.ToString();Save(false,speed);Console.WriteLine(ex);return 1;}
        // First evidence: one fresh 20km/h native case at the .7bar service reduction.
        foreach(double reduction in new[]{.7}) {
            var fixture=new Fixture{speed=speed,reduction=reduction};
            try {fixture.Start();fixture.Measure();}
            catch(Exception ex){fixture.error=ex.ToString();Console.WriteLine(ex);}
            finally {fixture.Close();Cases.Add(fixture.Result());if(fixture.error!=null||!fixture.restored||!fixture.closed){exit=1;failure=(failure??"")+"\nUR"+reduction.ToString(CultureInfo.InvariantCulture)+": "+fixture.error;}}
        }
        try {Save(exit==0,speed);}catch(Exception ex){Console.WriteLine(ex);exit=1;}
        Console.WriteLine(exit==0?"PASS native composite -8 braking, secured stop and restoration; release observations are separate":"FAIL adverse native braking; inspect full-grade windows");return exit;
    }
}
