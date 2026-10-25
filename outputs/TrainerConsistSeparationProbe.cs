using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using RailwayDynamics.Simulation;

public sealed class TrainerConsistSeparationProbe {
    [STAThread] public static int Main(){return TrainerAutovedenieAdverseBrakeProbe.RunConsistSeparation();}
}
public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
    public static int RunConsistSeparation(){
        Application.EnableVisualStyles();thread=GetCurrentThreadId();Watch.Restart();Checks.Clear();Cases.Clear();failure=null;
        Fixture f=null;int exit=1;
        try{f=new Fixture();f.MeasureConsistSeparation();exit=0;}
        catch(Exception ex){failure=ex.ToString();Console.WriteLine(ex);if(f!=null)f.error=failure;}
        finally{
            if(f!=null){
                try{f.CloseManagedMotionProbe();f.VerifySeparationRestored();}
                catch(Exception ex){f.error=(f.error??"")+"\nSEPARATION RESTORE: "+ex;exit=1;}
                if(!TrainerNativeBrakeForceProvider.HasRetainedOwners)f.Close();
                if(f.error!=null||!f.restored||!f.closed)exit=1;
                Cases.Add(f.SeparationResult());
            }
        }
        File.WriteAllText(TrainerSession.ResultPath("Trainer_consist_separation_validation.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(new{
            passed=exit==0,exit_code=exit,error=failure,checks=Checks,cases=Cases,
            scope="Prepared40km/h/prestrained selected SA3 link37 exceeds nominal4MN; actual native charged/released air, then native pneumatics plus managed motion. No prepared TC/TM/brake forces. Demonstrates two-sided vent/automatic braking, mapping/guards/restore. Not genuine cab-generated rupture, full safety/route/derailment or universal SA3 ultimate acceptance."
        }));
        Console.WriteLine(exit==0?"PASS native consist separation":"FAIL native consist separation");return exit;
    }
    sealed partial class Fixture {
        readonly List<object> separationRows=new List<object>();
        byte[] separationOriginal;
        object separationBefore,separationAfter;
        bool separationRestored;
        int separationCycles;
        double maxSeparationResidual;
        byte[] SeparationSleeves(){
            var b=(IntPtr)Field("secondBvr");
            return new[]{Marshal.ReadByte(motionCars[37],0x23),Marshal.ReadByte(motionCars[38],0x22),Marshal.ReadByte(b,0x23),Marshal.ReadByte(motionCars[1],0x22)};
        }
        object SeparationPressure(string label){
            return new{label,cycles=separationCycles,head_tm=Float(motionCars[0],0),left_tm=Float(motionCars[37],0),right_tm=Float(motionCars[38],0),tail_tm=Float(motionCars[75],0),
                head_tc=Float(mem["TLocParam"],13),left_tc=Float(motionCars[37],4),right_tc=Float(motionCars[38],4),tail_tc=Float(motionCars[75],4),
                loco_vr1_tc=Float(motionCars[0],4),loco_vr2_tc=Float((IntPtr)Field("secondBvr"),4),first_wagon_tc=Float(motionCars[1],4),km=Marshal.ReadByte(mem["TLocParam"],0x7d),kvt=Marshal.ReadByte(mem["TLocParam"],0x7e),vcu=Marshal.ReadByte(mem["TLocParam"],0x8f),automatic_release=Field("nativeAutomaticReleaseForStep"),
                bogies_tc=bogiePressure.Select(p=>p()).ToArray(),
                head_v=motionKernelUnderTest.GetVehicle(0).VelocityV,tail_v=motionKernelUnderTest.GetVehicle(75).VelocityV};
        }
        public void MeasureConsistSeparation(){
            beforeNativePneumatic=ManagedBudget;Start();ReadMotionOwner();
            Check("Production SA3 rupture profile enabled",motionKernelUnderTest.FractureTensionLimitN==4000000,4000000,motionKernelUnderTest.FractureTensionLimitN);
            separationOriginal=SeparationSleeves();
            var baseline=Enumerable.Range(0,76).Select(i=>Float(motionCars[i],0)).ToArray();
            Check("Freshly released wagon brakes before rupture",Enumerable.Range(1,75).All(i=>Float(motionCars[i],4)<.01),"<.01kgf/cm2",new[]{Float(motionCars[37],4),Float(motionCars[38],4),Float(motionCars[75],4)});
            Check("Open cocks at selected fracture boundary",Marshal.ReadByte(motionCars[37],0x21)==1&&Marshal.ReadByte(motionCars[38],0x20)==1,"open/open",true);
            Check("Both sleeves initially connected",separationOriginal[0]==1&&separationOriginal[1]==1,"1/1",separationOriginal);
            using(TrainerManagedFloatingScope.Enter()){
                // Explicit prepared overload: account initial strain energy in
                // the constructor; never move a live node behind the solver.
                var nodes=new VehicleDynamicsNode[76];
                for(int i=0;i<76;i++){
                    nodes[i]=motionKernelUnderTest.GetVehicle(i);nodes[i].VelocityV=40/3.6;nodes[i].AccelerationA=0;
                    if(i>=38)nodes[i].PositionX-=.50;
                }
                var previous=motionKernelUnderTest;
                var profile=(TrainerScientificMechanicsProfile)Field("scientificMechanics");
                var prepared=new LongitudinalTrainDynamicsSolver(nodes,profile.CreateCouplers());
                prepared.SetBrakeForceProvider((Func<int,double,double>)typeof(LongitudinalTrainDynamicsSolver).GetField("_brakeForceProvider",Flags).GetValue(previous));
                prepared.SetRouteForceProviders((Func<int,double,double>)typeof(LongitudinalTrainDynamicsSolver).GetField("_gradeForceProvider",Flags).GetValue(previous),
                    (Func<int,double,double>)typeof(LongitudinalTrainDynamicsSolver).GetField("_curveForceProvider",Flags).GetValue(previous));
                prepared.ConfigureCouplerIntegrity(profile.coupler_integrity.nominal_tensile_fracture_N,profile.coupler_integrity.design_compression_N);
                SetField("managedMotion",prepared);motionKernelUnderTest=prepared;
                SetField("trainForceEpoch",(long)Field("trainForceEpoch")+1);
                Call("PublishManagedMotion");separationBefore=SeparationPressure("prepared_charged_before_overload");
                Call("MotionCycle");separationCycles++;
                Check("Actual profile threshold is nominal4MN",prepared.FractureTensionLimitN==4000000,4000000,prepared.FractureTensionLimitN);
                Check("Prepared overload breaks intended SA3 only",prepared.BrokenCouplerCount==1&&prepared.IsCouplerBroken(37)&&prepared.ComponentCount==2,"link37/two parts",prepared.BrokenCouplerCount);
                Check("Recorded demanded force exceeds real configured threshold",prepared.GetCouplerFailureTensionNewtons(37)>4000000,">4MN",prepared.GetCouplerFailureTensionNewtons(37));
                Check("Mechanical tension is removed at fracture",prepared.GetCouplerForceSignedNewtons(37)==0,0,prepared.GetCouplerForceSignedNewtons(37));
            }
            var separated=SeparationSleeves();
            Check("Committed mechanical break disconnects both actual sleeves",separated[0]==0&&separated[1]==0,"0/0",separated);
            Check("Fracture never changes cock positions",Marshal.ReadByte(motionCars[37],0x21)==1&&Marshal.ReadByte(motionCars[38],0x20)==1,"open/open",true);
            bool noTension=true;
            phase="prepared_split_native_pressure_and_motion";
            for(int n=0;n<300;n++){
                ManagedBudget();Call("ElectricalCycle");Call("AfterAutovedenieElectricalCycle");Call("PneumaticCycle");Call("MotionCycle");separationCycles++;
                SetField("ticks",Convert.ToInt32(Field("ticks"))+1);
                using(TrainerManagedFloatingScope.Enter()){
                    var sample=TrainForceSample.Capture(motionKernelUnderTest,0);
                    for(int i=0;i<76;i++){var v=sample.GetVehicle(i);maxSeparationResidual=Math.Max(maxSeparationResidual,Math.Abs(v.BalanceResidualN));if(Math.Abs(v.BalanceResidualN)>v.BalanceToleranceN)throw new Exception("Separated force balance failed "+i);}
                    noTension&=motionKernelUnderTest.GetCouplerForceSignedNewtons(37)>=0;
                }
                if(n==19||n==99||n==299)separationRows.Add(SeparationPressure("after_"+(n+1)+"_native_cycles"));
            }
            Check("Both exposed TM ends lose pressure",baseline[37]-Float(motionCars[37],0)>.2&&baseline[38]-Float(motionCars[38],0)>.2,">.2kgf/cm2 each",new[]{baseline[37]-Float(motionCars[37],0),baseline[38]-Float(motionCars[38],0)});
            Check("Both adjacent wagons automatically apply brakes",Float(motionCars[37],4)>.1&&Float(motionCars[38],4)>.1,"TC>.1 both",new[]{Float(motionCars[37],4),Float(motionCars[38],4)});
            Check("Detached tail automatically applies brakes",Float(motionCars[75],4)>.1,"TC>.1 tail",Float(motionCars[75],4));
            separationRows.Add(SeparationPressure("automatic_split_braking_with_KMII_feed"));
            // Explicit operator response after the automatically braked wagons:
            // stop KMII feeding the broken front pipe. Do not claim this phase
            // as an automatic locomotive response with KMII unchanged.
            Handle(0x3b0,4);phase="operator_IV_after_detected_rupture";
            for(int n=0;n<300&&Float(mem["TLocParam"],13)<=.1;n++){
                ManagedBudget();Call("ElectricalCycle");Call("AfterAutovedenieElectricalCycle");Call("PneumaticCycle");Call("MotionCycle");separationCycles++;
                SetField("ticks",Convert.ToInt32(Field("ticks"))+1);
            }
            separationRows.Add(SeparationPressure("after_operator_KMIV"));
            Check("Second native locomotive BVR reacts to split pipe",Float((IntPtr)Field("secondBvr"),4)>.1,"BVR2>.1",Float((IntPtr)Field("secondBvr"),4));
            Check("Both split groups contain automatically braked wagons",Float(motionCars[37],4)>.1&&Float(motionCars[75],4)>.1,"front part and detached tail",new[]{Float(motionCars[37],4),Float(motionCars[75],4)});
            Check("Detached parts retain irreversible rupture and no tensile link",noTension&&motionKernelUnderTest.IsCouplerBroken(37)&&motionKernelUnderTest.ComponentCount==2,true,noTension);
            separationAfter=typeof(TrainerIntegrationHost).GetMethod("NativeConsistSeparationSnapshot",Flags).Invoke(host,null);
            using(TrainerManagedFloatingScope.Enter())using(var view=new TrainForceForm()){
                foreach(var s in ((TrainForceHistory)Field("trainForceHistory")).ToArray())view.Push(s);
                view.SelectedLink=37;view.SelectedVehicle=38;view.Show();
                view.SaveRender(TrainerSession.ResultPath("Trainer_consist_separation.png"),0);
            }
            // Isolated adapter component: the first train boundary uses BVR2,
            // never the inner locomotive BVR-to-BVR connection.
            var bvr=(IntPtr)Field("secondBvr");byte inner=Marshal.ReadByte(motionCars[0],0x23),cock=Marshal.ReadByte(bvr,0x21);
            try{
                Marshal.WriteByte(bvr,0x21,0);
                var args=new object[]{0,false};typeof(TrainerIntegrationHost).GetMethod("ApplyNativeConsistSeparation",Flags).Invoke(host,args);
                Check("Head boundary maps BVR2 rear and wagon1 front",Marshal.ReadByte(bvr,0x23)==0&&Marshal.ReadByte(motionCars[1],0x22)==0&&(bool)args[1],true,args[1]);
                Check("Head boundary preserves internal locomotive hose and closed cock",Marshal.ReadByte(motionCars[0],0x23)==inner&&Marshal.ReadByte(bvr,0x21)==0,true,true);
            }finally{Marshal.WriteByte(bvr,0x21,cock);}
            bool rejected=false;
            try{Marshal.WriteByte(motionCars[37],0x23,1);Call("ValidateNativeConsistSeparation");}
            catch(Exception){rejected=true;}
            finally{Marshal.WriteByte(motionCars[37],0x23,0);}
            Check("Foreign hose reconnection is rejected before another pneumatic step",rejected,true,rejected);
            Call("ValidateNativeConsistSeparation");
        }
        public void VerifySeparationRestored(){
            if(separationOriginal==null)return;
            separationRestored=separationOriginal.SequenceEqual(SeparationSleeves());
            Check("Owned sleeve inputs restored before native close",separationRestored,true,separationRestored);
        }
        public object SeparationResult(){return new{error,cycles=cycle,measurement_cycles=separationCycles,prepared_speed_kmh=40,prepared_extra_gap_m=.5,before=separationBefore,after=separationAfter,pressure_rows=separationRows,max_force_balance_residual_N=maxSeparationResidual,sleeves_restored=separationRestored,native_inputs_restored=restored,native_closed=closed};}
    }
}
