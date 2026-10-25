using System;
using System.Collections.Generic;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Simulation;
using RailwayDynamics.Couplers;

public sealed class TrainerCouplerFractureProbe {
    static readonly List<object> checks=new List<object>();
    static void Check(string name,bool pass,object actual) {
        checks.Add(new{name,passed=pass,actual});
        if(!pass)throw new Exception(name+" actual="+actual);
    }
    static LongitudinalTrainDynamicsSolver Make(int n,double displacement,double relativeSpeed,double threshold) {
        var nodes=new VehicleDynamicsNode[n];
        for(int i=0;i<n;i++)nodes[i]=new VehicleDynamicsNode{MassKg=80000,LengthM=10,PositionX=-i*(10-displacement),VelocityV=i==0?relativeSpeed:0,AxleCount=4};
        var s=new LongitudinalTrainDynamicsSolver(nodes,FrictionDraftGearModel.CreateScientificSh2VPairProfile());
        s.RunningResistanceEnabled=false;s.MaximumSubstepSeconds=.001;
        s.SetBrakeForceProvider(delegate(int i,double v){return 0;});
        s.ConfigureCouplerIntegrity(threshold,2500000);return s;
    }
    static void Balance(LongitudinalTrainDynamicsSolver s,string name) {
        var f=TrainForceSample.Capture(s,0);bool ok=true;double residual=0;
        for(int i=0;i<f.VehicleCount;i++){var v=f.GetVehicle(i);residual=Math.Max(residual,Math.Abs(v.BalanceResidualN));ok&=Math.Abs(v.BalanceResidualN)<=v.BalanceToleranceN;}
        Check(name+" last accepted force balance",ok,residual);
    }
    static double DynamicFailureTime(double maxDt) {
        var s=Make(2,0,30,4000000);s.MaximumSubstepSeconds=maxDt;
        for(int i=0;i<100&&!s.IsCouplerBroken(0);i++)s.Step(.001);
        Check("Dynamic slack takeup ruptures at "+maxDt,s.IsCouplerBroken(0),s.SimulationTimeSeconds);
        return s.GetCouplerFailureTimeSeconds(0);
    }
    public static int Main() {
        int exit=1;string error=null;double coarse=0,fine=0;
        try {
            var reference=Make(2,-.30,0,0);reference.Step(.001);
            double demand=-reference.GetCouplerForceSignedNewtons(0);
            Check("Controlled intact tensile demand above 4MN",demand>4000000,demand);
            var exact=Make(2,-.30,0,demand);exact.Step(.001);
            Check("Equality at threshold does not rupture",!exact.IsCouplerBroken(0),exact.BrokenCouplerCount);
            var lower=Make(2,-.30,0,demand-1);double e=lower.TotalEnergyJ,p=lower.EffectiveMomentumKgMps;
            var beforeSample=TrainForceSample.Capture(lower,0);
            lower.Step(.001);
            Check("Strict exceedance ruptures",lower.IsCouplerBroken(0),lower.GetCouplerFailureTensionNewtons(0));
            Check("Recorded demand and time are actual converged trial",lower.GetCouplerFailureTensionNewtons(0)==demand&&lower.GetCouplerFailureTimeSeconds(0)==.001,lower.GetCouplerFailureTensionNewtons(0));
            Check("Failed tensile link transmits zero",lower.GetCouplerForceSignedNewtons(0)==0,lower.GetCouplerForceSignedNewtons(0));
            Check("Fracture does not invent an impulse",lower.EffectiveMomentumKgMps==p&&lower.GetVehicle(0).VelocityV==0&&lower.GetVehicle(1).VelocityV==0,lower.EffectiveMomentumKgMps);
            Check("Stored elastic energy accounted as dissipation",Math.Abs(lower.TotalEnergyJ+lower.CumulativeDissipatedEnergyJ-e)<1e-6&&lower.TotalEnergyJ==0,lower.CumulativeDissipatedEnergyJ);
            Check("Components separate without deleting mass",lower.ComponentCount==2&&lower.GetVehicleComponentIndex(0)==0&&lower.GetVehicleComponentIndex(1)==1&&lower.VehicleCount==2,lower.ComponentCount);
            var brokenSample=TrainForceSample.Capture(lower,0);
            var flags=brokenSample.BrokenCouplers;flags[0]=false;
            var loads=brokenSample.CouplerFailureTensionN;loads[0]=0;
            var times=brokenSample.CouplerFailureTimeSeconds;times[0]=999;
            var components=brokenSample.VehicleComponentIndices;components[1]=0;
            var warnings=brokenSample.CompressionOverloads;warnings[0]=true;
            Check("Fracture DTO arrays are independent copies",brokenSample.IsCouplerBroken(0)&&brokenSample.GetCouplerFailureTensionNewtons(0)==demand&&brokenSample.GetCouplerFailureTimeSeconds(0)==.001&&brokenSample.GetVehicleComponentIndex(1)==1&&!brokenSample.HasCompressionOverload(0),true);
            Check("Old force snapshot never acquires a later fracture",!beforeSample.IsCouplerBroken(0)&&beforeSample.ComponentCount==1,beforeSample.ComponentCount);
            Balance(lower,"fracture");
            lower.SetVehicleForce(0,100000);lower.Step(.01);
            Check("Detached tail cannot receive locomotive pull",lower.GetVehicle(0).VelocityV>0&&Math.Abs(lower.GetVehicle(1).VelocityV)<1e-8,lower.GetVehicle(1).VelocityV);
            Balance(lower,"separated pull");
            lower.SetVehicleForce(0,0);lower.SetInitialState(0,0);
            Check("Position/speed reset cannot repair a broken coupler",lower.IsCouplerBroken(0)&&lower.ComponentCount==2,lower.ComponentCount);
            Check("Existing fracture belongs to start of reset time epoch",lower.GetCouplerFailureTimeSeconds(0)==0,lower.GetCouplerFailureTimeSeconds(0));
            Check("Reset cannot rewrite an archived event timestamp",brokenSample.GetCouplerFailureTimeSeconds(0)==.001,brokenSample.GetCouplerFailureTimeSeconds(0));
            lower.SetVehicleForce(0,-1000000);lower.SetVehicleForce(1,1000000);
            bool contact=false,neverTensile=true;
            for(int i=0;i<70;i++){lower.Step(.01);double f=lower.GetCouplerForceSignedNewtons(0);contact|=f>100;neverTensile&=f>=0;}
            Check("Recontact resists compression without relatching",contact&&neverTensile&&lower.IsCouplerBroken(0),new{contact,neverTensile});
            Balance(lower,"unilateral compression");
            lower.SetVehicleForce(0,1000000);lower.SetVehicleForce(1,-1000000);
            for(int i=0;i<100;i++){lower.Step(.01);neverTensile&=lower.GetCouplerForceSignedNewtons(0)>=0;}
            Check("Reopening contact still cannot transmit tension",neverTensile&&lower.GetCouplerForceSignedNewtons(0)==0&&lower.IsCouplerBroken(0),lower.GetCouplerForceSignedNewtons(0));
            var multi=Make(3,-.30,0,4000000);multi.Step(.001);
            Check("Multiple overloads produce three components",multi.BrokenCouplerCount==2&&multi.ComponentCount==3,multi.BrokenCouplerCount);
            Balance(multi,"multiple fracture");
            var compression=Make(2,.30,0,4000000);compression.Step(.001);
            Check("Compression overload warns, never tears touching train apart",compression.HasCompressionOverload(0)&&!compression.IsCouplerBroken(0)&&compression.GetCouplerForceSignedNewtons(0)>2500000,compression.GetCouplerForceSignedNewtons(0));
            Balance(compression,"compression warning");
            var safe=Make(2,0,0,4000000);safe.SetVehicleForce(0,100000);
            for(int i=0;i<100;i++)safe.Step(.01);
            Check("Ordinary sublimit traction keeps train intact",safe.BrokenCouplerCount==0,safe.GetCouplerForceSignedNewtons(0));
            var rollback=Make(2,-.30,0,4000000);double oldE=rollback.TotalEnergyJ;
            rollback.SetRouteForceProviders(delegate(int i,double x){if(rollback.SimulationTimeSeconds>0)throw new InvalidOperationException("intentional after accepted rupture");return 0;},null);
            bool threw=false;try{rollback.Step(.02);}catch(InvalidOperationException){threw=true;}
            Check("Failed outer step rolls back accepted fracture",threw&&rollback.SimulationTimeSeconds==0&&!rollback.IsCouplerBroken(0)&&rollback.GetCouplerFailureTensionNewtons(0)==0&&rollback.TotalEnergyJ==oldE&&rollback.CumulativeDissipatedEnergyJ==0,rollback.BrokenCouplerCount);
            var reset=TrainForceSample.Capture(rollback,0);
            Check("Rollback also restores force endpoint availability",!reset.HasAcceptedStep,reset.HasAcceptedStep);
            coarse=DynamicFailureTime(.001);fine=DynamicFailureTime(.00025);
            Check("Dynamic event timing converges with substep",Math.Abs(coarse-fine)<=.002,new{coarse,fine});
            var dyn=Make(2,0,30,4000000);double initialE=dyn.TotalEnergyJ,initialP=dyn.EffectiveMomentumKgMps;
            dyn.Step(.1);
            Check("Dynamic rupture preserves total momentum",Math.Abs(dyn.EffectiveMomentumKgMps-initialP)<.001,dyn.EffectiveMomentumKgMps-initialP);
            Check("Dynamic rupture remains passive",dyn.TotalEnergyJ<=initialE+.01&&Math.Abs(dyn.TotalEnergyJ+dyn.CumulativeDissipatedEnergyJ-initialE)<.1,dyn.TotalEnergyJ+dyn.CumulativeDissipatedEnergyJ-initialE);
            Balance(dyn,"dynamic rupture");exit=0;
        }catch(Exception ex){error=ex.ToString();Console.WriteLine(error);}
        string dir=Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");if(String.IsNullOrEmpty(dir))dir=AppDomain.CurrentDomain.BaseDirectory;
        Directory.CreateDirectory(dir);File.WriteAllText(Path.Combine(dir,"Trainer_coupler_fracture_validation.json"),new JavaScriptSerializer().Serialize(new{passed=exit==0,exit_code=exit,assertions=checks.Count,checks,error,coarse_event_s=coarse,fine_event_s=fine,scope="Assumed nominal4MN tensile failure; topology/passivity/rollback/contact components. No universal OEM ultimate strength or native pressure-wave certification."}));
        Console.WriteLine(exit==0?"PASS coupler fracture":"FAIL coupler fracture");return exit;
    }
}
