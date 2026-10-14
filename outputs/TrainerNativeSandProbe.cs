using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Command acceptance only. Native airflow and adhesion require a separate
// source/valve integration and physical measurement; AF.Pesok is no force credit.
public static class TrainerNativeSandProbe {
    [STAThread] public static int Main(string[] args) {
        return TrainerAutovedenieAdverseBrakeProbe.RunSandValidation();
    }
}

public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
    public sealed class SandFrame {
        public string phase;
        public int cycle;
        public bool requested,inside_parent_callback,avp_owns_commands;
        public byte source_input_before,borrowed_input,input_before_scope_restore,native_af_before_restore,input_after_cycle,native_af_after_cycle;
        public byte pedal_source,emergency_source,emergency2_source,pbz_source,automatic_emergency_source,loc_sand_input;
        public int schema_sand_source,live_scope_count;
        public double native_force,maximum_abs_velocity_m_s;
    }

    sealed partial class Fixture {
        public readonly List<SandFrame> sandRows=new List<SandFrame>();
        public bool sandComplete,sandSourceRestored,sandPhysicalTested;
        public string sandPhysicalReason,sandObserverError;
        public int sandCallbackCount,sandOutsideBorrowCount;
        TrainerAutovedenieNativeBridge sandBridge;
        Func<IntPtr,IDisposable> sandPreviousSource;
        FieldInfo sandInsideField;
        IntPtr sandSchema;
        bool sandEnabled;
        int sandCycleCallbackStart;

        sealed class SandEmptyLease : IDisposable { public void Dispose() {} }
        sealed class SandObservedLease : IDisposable {
            readonly Fixture owner;
            readonly IDisposable source;
            readonly SandFrame row;
            bool disposed;
            public SandObservedLease(Fixture fixture,IDisposable lease,SandFrame frame) {owner=fixture;source=lease;row=frame;}
            public void Dispose() {
                if(disposed)return;disposed=true;
                try {
                    if(row!=null) {
                        // Original ProceedPult has returned. Observe its actual AF
                        // before the real operator scope restores its input byte.
                        var pult=owner.mem["TPult"];
                        Require(pult,0xc7,false);Require(owner.sandSchema,0x17d,false);
                        row.input_before_scope_restore=Marshal.ReadByte(pult,0x40);
                        row.native_af_before_restore=Marshal.ReadByte(pult,0xa5);
                        row.pedal_source=Marshal.ReadByte(pult,0x1c);
                        row.emergency_source=Marshal.ReadByte(pult,0x23);
                        row.emergency2_source=Marshal.ReadByte(pult,0x24);
                        row.schema_sand_source=Marshal.ReadInt32(owner.sandSchema,0x179);
                        row.pbz_source=Marshal.ReadByte(owner.sandSchema,0x101);
                        row.automatic_emergency_source=Marshal.ReadByte(owner.sandSchema,0x102);
                        owner.sandRows.Add(row);
                    }
                } catch(Exception ex) {owner.sandObserverError=(owner.sandObserverError??"")+ex.ToString();}
                finally {source.Dispose();}
            }
        }
        object SandConstruct(string className,params object[] arguments) {
            var type=typeof(TrainerIntegrationHost).GetNestedType(className,BindingFlags.Public|BindingFlags.NonPublic);
            if(type==null)throw new Exception("Production operator scope missing: "+className);
            return Activator.CreateInstance(type,BindingFlags.Instance|BindingFlags.Public|BindingFlags.NonPublic,null,arguments,CultureInfo.InvariantCulture);
        }
        int SandScopes() {
            var list=Field("avpSandScopes") as ICollection;
            if(list==null)throw new Exception("Production sand scope list guard");
            return list.Count;
        }
        IDisposable SandOperatorSource(IntPtr pult) {
            if(pult!=mem["TPult"] || sandBridge==null)throw new Exception("Sand operator source owner guard");
            Require(pult,0xc7,false);
            bool inside=(bool)sandInsideField.GetValue(sandBridge);
            byte previous=Marshal.ReadByte(pult,0x40);
            IDisposable original=null,extra=null,combined=null;
            try {
                original=sandPreviousSource(pult);
                if(original==null)throw new Exception("Existing operator source returned no lease");
                extra=sandEnabled ? (IDisposable)SandConstruct("AutovedenieSandScope",Field("avpSandScopes"),pult) : new SandEmptyLease();
                combined=(IDisposable)SandConstruct("SafetyOperatorInputScope",original,extra);
                var row=inside ? new SandFrame {phase=phase,cycle=cycle+1,requested=sandEnabled,
                    inside_parent_callback=true,source_input_before=previous,borrowed_input=Marshal.ReadByte(pult,0x40)} : null;
                if(inside)sandCallbackCount++;else sandOutsideBorrowCount++;
                return new SandObservedLease(this,combined,row);
            } catch {
                if(combined!=null)combined.Dispose();
                else {try {if(extra!=null)extra.Dispose();} finally {if(original!=null)original.Dispose();}}
                throw;
            }
        }
        void SandStep(string label,byte expectedAf,byte expectedPhysicalInput) {
            phase=label;sandCycleCallbackStart=sandCallbackCount;
            Step();
            Check(label+" one parent input callback",sandCallbackCount-sandCycleCallbackStart==1,1,sandCallbackCount-sandCycleCallbackStart);
            Check(label+" observer guard",sandObserverError==null,null,sandObserverError);
            var row=sandRows[sandRows.Count-1];
            Check(label+" fresh callback row",row.cycle==cycle && row.inside_parent_callback,cycle,row.cycle);
            var pult=mem["TPult"];var loc=mem["TLocParam"];
            Require(pult,0xc7,false);Require(loc,0xa7,false);Require(sandSchema,0x17d,false);
            row.input_after_cycle=Marshal.ReadByte(pult,0x40);
            row.native_af_after_cycle=Marshal.ReadByte(pult,0xa5);
            row.loc_sand_input=Marshal.ReadByte(loc,0x7f);
            row.live_scope_count=SandScopes();row.avp_owns_commands=sandBridge.OwnsCommands;
            var frame=Capture();row.native_force=frame.force_native;row.maximum_abs_velocity_m_s=frame.maximum_abs_v_m_s;
            var decision=Field("avpDecision") as TrainerAvpDecision;
            Check(label+" source borrowed in original callback",row.borrowed_input==(sandEnabled?(byte)1:expectedPhysicalInput),sandEnabled?(byte)1:expectedPhysicalInput,row.borrowed_input);
            Check(label+" operator input held through original call",row.input_before_scope_restore==row.borrowed_input,row.borrowed_input,row.input_before_scope_restore);
            Check(label+" original native sand AF",row.native_af_before_restore==expectedAf,expectedAf,row.native_af_before_restore);
            Check(label+" native AF retained after electrical",row.native_af_after_cycle==expectedAf,expectedAf,row.native_af_after_cycle);
            Check(label+" physical input restored",row.input_after_cycle==expectedPhysicalInput,expectedPhysicalInput,row.input_after_cycle);
            Check(label+" scope ownership released",row.live_scope_count==0,0,row.live_scope_count);
            Check(label+" other native sand sources absent",row.pedal_source==0 && row.emergency_source==0 && row.emergency2_source==0 && row.schema_sand_source==0,
                "pedal0 / emergency0 / emergency2=0 / schema179=0",new {row.pedal_source,row.emergency_source,row.emergency2_source,row.schema_sand_source});
            Check(label+" original Pult bridge healthy",String.IsNullOrEmpty(sandBridge.Fault),"no native callback fault",sandBridge.Fault);
            Check(label+" AVP remains inactive",!row.avp_owns_commands && decision!=null && !decision.DriveEnabled,false,new {row.avp_owns_commands,drive_enabled=decision!=null&&decision.DriveEnabled});
            Check(label+" no native traction",Math.Abs(row.native_force)<.001,"abs(force)<.001",row.native_force);
            Check(label+" native consist remains within standstill",row.maximum_abs_velocity_m_s<=Standstill,"all vehicles <=0.3km/h",row.maximum_abs_velocity_m_s);
            Check(label+" no additional speed initialization",preparedInitializations==0,0,preparedInitializations);
            Check(label+" native protection remains healthy",frame.epk_trip==0 && frame.kon_trip==0,"EPK0 / KON0",new {frame.epk_trip,frame.kon_trip});
        }
        public void SandMeasure() {
            Check("Sand uses no diagnostic traction bridge",operatorBridge==null,true,operatorBridge==null);
            Check("Sand source AVP disabled",Field("avpController")==null && !((TrainerAvpDecision)Field("avpDecision")).DriveEnabled,
                "no AVP controller / drive disabled",Field("avpUnavailable"));
            var pultModule=GetModuleHandle("Pult1.dll");
            Guard(communication,0x2af458,"8B45FC8B80D40300008A80D90100008B55FC8B9230050000884240");
            Guard(pultModule,0x18e934,"80784001");
            Guard(pultModule,0x18e964,"C680A500000001");
            Guard(pultModule,0x18e973,"C680A500000000");
            Check("Original native sand input/readback byte guards",true,"Communication2AF458 / Pult18E934,18E964,18E973","exact loaded bytes");
            Require(cabForm,0x534,false);var control=Marshal.ReadIntPtr(cabForm,0x3d4);
            Require(control,0x1da,false);sandPhysicalReason=ClassName(control);
            Check("Physical sand cab control class",sandPhysicalReason=="TTumbler" || sandPhysicalReason=="TTumbler3Pos" || sandPhysicalReason=="TButton2ES6",
                "TTumbler / TTumbler3Pos / TButton2ES6",sandPhysicalReason);
            Cab(0x3d4,0);
            Call("EnsureAutovedenieNativeBridge");sandBridge=Field("avpNative") as TrainerAutovedenieNativeBridge;
            Check("Host-owned inactive native bridge attached",sandBridge!=null && !sandBridge.OwnsCommands,true,sandBridge!=null);
            sandBridge.SubmitDecision(new TrainerAvpDecision {State=TrainerAvpState.Advisor,DriveEnabled=false,SuggestedTractionCommand=0});
            var sourceField=typeof(TrainerAutovedenieNativeBridge).GetField("operatorInputSource",Flags);
            sandInsideField=typeof(TrainerAutovedenieNativeBridge).GetField("insideCallback",Flags);
            var schemaField=typeof(TrainerAutovedenieNativeBridge).GetField("schema",Flags);
            if(sourceField==null || sandInsideField==null || schemaField==null)throw new Exception("Native bridge observer contract missing");
            sandPreviousSource=sourceField.GetValue(sandBridge) as Func<IntPtr,IDisposable>;
            if(sandPreviousSource==null)throw new Exception("Existing host operator source absent");
            sandSchema=(IntPtr)schemaField.GetValue(sandBridge);Require(sandSchema,0x17d,false);
            Check("No leftover operator sand scopes",SandScopes()==0,0,SandScopes());
            sandBridge.SetOperatorInputSource(SandOperatorSource);
            try {
                sandEnabled=false;SandStep("scope_off_baseline",0,0);
                sandEnabled=true;SandStep("scope_on_single_cycle",1,0);
                sandEnabled=false;SandStep("scope_off_after_single_cycle",0,0);
                sandEnabled=true;for(int n=1;n<=10;n++)SandStep("scope_on_ten_cycles_"+n.ToString(CultureInfo.InvariantCulture),1,0);
                sandEnabled=false;SandStep("scope_off_after_ten_cycles",0,0);
                Cab(0x3d4,1);SandStep("physical_cab_sand_on",1,1);
                sandEnabled=true;SandStep("scope_over_physical_sand_on",1,1);
                sandEnabled=false;SandStep("physical_sand_preserved_after_scope",1,1);
                Cab(0x3d4,0);SandStep("physical_cab_sand_off",0,0);
                sandPhysicalTested=true;
                Check("Native sand accepted and recovered",sandCallbackCount==18,18,sandCallbackCount);
                sandComplete=true;
            } finally {
                sandEnabled=false;
                sandBridge.SetOperatorInputSource(sandPreviousSource);
                Check("Original host operator source restored",Object.ReferenceEquals(sourceField.GetValue(sandBridge),sandPreviousSource),true,Object.ReferenceEquals(sourceField.GetValue(sandBridge),sandPreviousSource));
                Check("Production sand scope list empty after scenario",SandScopes()==0,0,SandScopes());
                sandSourceRestored=true;
            }
        }
    }

    static void SaveSand(bool passed,Fixture fixture) {
        File.WriteAllText(TrainerSession.ResultPath("Trainer_native_sand.json"),new JavaScriptSerializer {MaxJsonLength=4000000}.Serialize(new {
            passed,error=failure,checks=Checks,rows=fixture.sandRows,
            native_command_accepted=fixture.sandComplete,physical_cab_tested=fixture.sandPhysicalTested,
            physical_cab_class=fixture.sandPhysicalReason,observer_error=fixture.sandObserverError,
            parent_input_callbacks=fixture.sandCallbackCount,outside_callback_borrows=fixture.sandOutsideBorrowCount,
            operator_source_restored=fixture.sandSourceRestored,fixture_inputs_restored=fixture.restored,native_closed=fixture.closed,
            additional_prepared_initializations=fixture.preparedInitializations,scripted_operator_RBS_events=fixture.ackEvents,
            airflow_acceptance_performed=false,adhesion_acceptance_performed=false,automatic_drive_enabled=false,
            native_evidence=new {communication_sha256="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F",
                pult_sha256="7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5",
                cab_publisher_rva="2AF458",pult_consumer_rva="18E934",pult_af_on_rva="18E964",pult_af_off_rva="18E973",
                operator_input="TPult+40",native_confirmation="TPult+A5 (TAF.Pesok_AF)",loc_air_input="TLocParam+7F observed only"},
            scope="Native input acceptance and recovery on the full candidate fixture with AVP inactive. Real production sand operator scope is borrowed around original ProceedPult, and actual native AF is observed before restoration. Physical cab source and overlapping scope preserve manual state. All other OR command sources are checked absent. No force, speed, pressure, protection or valve output is assigned by this probe. Common prepared charging startup and explicit scripted RBS are fixture inputs only. AF acceptance does not establish Loc7F publication, air consumption or adhesion response."
        }));
    }
    public static int RunSandValidation() {
        Application.EnableVisualStyles();thread=GetCurrentThreadId();Watch.Reset();Watch.Start();Checks.Clear();Cases.Clear();failure=null;
        var fixture=new Fixture {speed=0,reduction=.7};int exit=1;
        try {
            Check("Sand x86 ABI",IntPtr.Size==4,4,IntPtr.Size);
            Check("Sand STA thread",Thread.CurrentThread.GetApartmentState()==ApartmentState.STA,"STA",Thread.CurrentThread.GetApartmentState().ToString());
            fixture.Start();fixture.SandMeasure();
        } catch(Exception ex) {fixture.error=ex.ToString();failure=fixture.error;Console.WriteLine(ex);}
        finally {
            // Save failure evidence before native teardown so a close failure
            // cannot turn a partial observation into an accepted result.
            try {SaveSand(false,fixture);}catch(Exception ex) {fixture.error=(fixture.error??"")+"\nPRE-CLOSE RESULT: "+ex;}
            fixture.Close();
        }
        try {
            Check("Sand operator source restoration completed",fixture.sandSourceRestored,true,fixture.sandSourceRestored);
            Check("Sand fixture physical inputs restored",fixture.restored,true,fixture.restored);
            Check("Sand fixture native lifetime closed",fixture.closed,true,fixture.closed);
            Check("Sand additional initializations absent",fixture.preparedInitializations==0,0,fixture.preparedInitializations);
            if(fixture.error==null && fixture.sandComplete && fixture.sandSourceRestored && fixture.restored && fixture.closed)exit=0;
        } catch(Exception ex) {fixture.error=(fixture.error??"")+"\nFINAL CHECK: "+ex;Console.WriteLine(ex);}
        failure=fixture.error;
        try {SaveSand(exit==0,fixture);}catch(Exception ex) {exit=1;Console.WriteLine(ex);}
        Console.WriteLine(exit==0?"PASS native sand command acceptance, recovery and operator input restoration; airflow and adhesion not tested":"FAIL native sand command acceptance; inspect fresh result");
        return exit;
    }
}
