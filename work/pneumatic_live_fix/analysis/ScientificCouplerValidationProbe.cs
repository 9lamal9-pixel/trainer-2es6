using System;
using System.Collections.Generic;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Couplers;

// Pure managed verification of a published CALCULATED pair profile. No OEM,
// installed-wagon, impact certification or native trainer acceptance is implied.
public sealed class ScientificCouplerValidationProbe {
    static readonly List<object> checks=new List<object>();static int failed;
    static void Check(string name,bool passed,object actual){checks.Add(new{name,passed,actual});if(!passed)failed++;}
    static void Near(string name,double actual,double expected,double tolerance){Check(name,!Double.IsNaN(actual)&&!Double.IsInfinity(actual)&&Math.Abs(actual-expected)<=tolerance,new{actual,expected,tolerance});}
    static double Advance(FrictionDraftGearModel model,ref CouplerState state,double end,int count,ref double work){
        double start=state.PreviousDisplacement,last=start,force=state.PreviousForce;
        for(int i=1;i<=count;i++){
            double x=i==count?end:start+(end-start)*i/count,oldEnergy=model.GetStoredEnergy(state),oldForce=force;
            force=model.ComputeCouplerForce(x,x-last,ref state);
            double increment=x-last,newEnergy=model.GetStoredEnergy(state),endpointWork=force*increment;
            if(endpointWork<newEnergy-oldEnergy-1e-7*Math.Max(1,oldEnergy))throw new InvalidOperationException("Scientific pair generated energy during a displacement increment.");
            double slope=increment==0?0:Math.Abs((force-oldForce)/increment);
            if(slope>model.GetTangentStiffness(x)*(1+1e-8))throw new InvalidOperationException("Scientific pair exceeded the supplied conservative tangent bound.");
            work+=.5*(oldForce+force)*increment;last=x;
        }
        return force;
    }
    static void SourceCurve(){
        var model=FrictionDraftGearModel.CreateScientificSh2VPairProfile();double slack=.5*model.TotalCouplerSlackM,work=0;
        var state=model.InitializeState(0);
        Near("source total deadband65mm, not plus/minus65mm",model.TotalCouplerSlackM,.065,0);
        Near("source two90mm gears provide180mm pair stroke",model.MaximumWorkingStrokeM,.180,1e-14);
        Near("centred initial slack stores no energy",model.GetStoredEnergy(state),0,0);
        Check("scientific profile identity and explicit scope",model.ProfileId=="sh2v-pair-published-2023"&&model.ProfileCalibrationScope.Contains("no OEM")&&model.SourceReference.Contains("10.20295/2223-9987-2023-2-54-70"),new{model.ProfileId,model.ProfileCalibrationScope,model.SourceReference});
        // Independent Fig.2 raster readings, printed58: xPair in m and F in N.
        // Calibrating points are deliberately not copied from the factory knots.
        double[] q={.030,.060,.120,.150,.180};
        double[] force={500000,885000,1667000,2058000,2450000};
        double[] uncertainty={30000,30000,40000,50000,120000};
        for(int i=0;i<q.Length;i++){double f=Advance(model,ref state,slack+q[i],300,ref work);Near("published loading point qPair="+q[i],f,force[i],uncertainty[i]);}
        double loadingWork=work,peak=state.PreviousForce;
        // Independently read locked-unloading branch is approximately100MN/m.
        double early=Advance(model,ref state,slack+.179,50,ref work);
        Near("published locked-unloading tangent",(peak-early)/.001,1e8,2e7);
        double[] uq={.150,.120,.060};double[] uf={186000,147000,71000};
        for(int i=0;i<uq.Length;i++){double f=Advance(model,ref state,slack+uq[i],300,ref work);Near("published lower branch qPair="+uq[i],f,uf[i],18000);}
        Advance(model,ref state,0,600,ref work);
        double returnedWork=loadingWork-work;
        // Independent areas of the fitted published polygon. The locked branch
        // contributes a triangle: (2.45e6-.225e6)^2/[2*(1e8-1.25e6)].
        const double expectedLoad=229500,expectedReturn=45317.40506329114,expectedDissipation=184182.59493670886;
        Near("published major-loop loading area J",loadingWork,expectedLoad,15000);
        Near("published major-loop return area including locked unloading J",returnedWork,expectedReturn,12000);
        Near("published major-loop dissipated area J",work,expectedDissipation,20000);
        Check("closed loop dissipates positive work",work>0&&returnedWork>=0&&returnedWork<loadingWork,new{loadingWork,returnedWork,loopWork=work});
        Near("major loop ends with zero stored energy",model.GetStoredEnergy(state),0,1e-7);
        Near("major loop ends with zero force in slack",state.PreviousForce,0,0);
        // Post-closure is a finite mathematical structural compliance. It is not
        // permission for a physical gear to exceed its documented working stroke.
        var extended=model.InitializeState(0);double ew=0;Advance(model,ref extended,slack+.180,1800,ref ew);double closed=extended.PreviousForce;
        double over=Advance(model,ref extended,slack+.183,100,ref ew);
        Near("published post-closure structural slope",(over-closed)/.003,1e8,2e7);
    }
    static void SeriesAndReversal(){
        var pair=FrictionDraftGearModel.CreateScientificSh2VPairProfile();
        // An independently specified SINGLE apparatus corresponding to the
        // published pair fit. Single-device force is not doubled in a series link.
        const double pairBackbone=.5*((2450000.0-100000)/.18+225000/.18);
        var single=new FrictionDraftGearModel(new[]{0.0,.090},new[]{100000.0,2450000},new[]{0.0,225000},0,.0005,0,2e8,2*(1e8-pairBackbone));
        var ps=pair.InitializeState(pair.TotalCouplerSlackM/2);var ss=single.InitializeState(0);
        double pw=0,sw=0;
        foreach(double end in new[]{.01,.03,.06,.09,.08,.04,0.0}){
            Advance(pair,ref ps,pair.TotalCouplerSlackM/2+2*end,150,ref pw);
            Advance(single,ref ss,end,150,ref sw);
            Near("series force unchanged at xPair=2*xSingle end="+end,ps.PreviousForce,ss.PreviousForce,1e-5);
            Near("series stored energy doubles end="+end,pair.GetStoredEnergy(ps),2*single.GetStoredEnergy(ss),1e-5);
            Near("series integrated work doubles end="+end,pw,2*sw,1e-5);
        }
        var pos=pair.InitializeState(0);var neg=pair.InitializeState(0);double wp=0,wn=0;
        foreach(double end in new[]{.04,.08,.16,.21,.15,.04,0.0}){
            Advance(pair,ref pos,end,100,ref wp);Advance(pair,ref neg,-end,100,ref wn);
            Near("declared odd force extension end="+end,pos.PreviousForce,-neg.PreviousForce,1e-5);
            Near("declared energy symmetry end="+end,pair.GetStoredEnergy(pos),pair.GetStoredEnergy(neg),1e-5);
        }
        double reversalWork=0;var reversal=pair.InitializeState(0);
        foreach(double end in new[]{.17,.10,.15,.05,-.18,-.07,0.0})Advance(pair,ref reversal,end,300,ref reversalWork);
        Check("slack crossings and partial reversals remain passive",reversalWork>=0&&reversal.DissipatedEnergyJ>=0,new{reversalWork,reversal.DissipatedEnergyJ});
        Near("reversals return to neutral energy",pair.GetStoredEnergy(reversal),0,1e-7);
        Check("historical factory remains distinct",FrictionDraftGearModel.CreateDefaultFreightDraftGear().ProfileId=="illustrative-default",FrictionDraftGearModel.CreateDefaultFreightDraftGear().ProfileId);
    }
    public static int Main(){
        string error=null;try{SourceCurve();SeriesAndReversal();}catch(Exception ex){failed++;error=ex.ToString();}
        var result=new{passed=failed==0,error,checks_count=checks.Count,failed_assertions=failed,checks,native_executed=false,
            profile_id="sh2v-pair-published-2023",scope="Typical published CALCULATED two-gear pair, graphic fit uncertainty and assumed odd extension. Not an OEM curve, installed consist identification, dynamic impact certification, or whole Host acceptance."};
        try{string dir=Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");if(String.IsNullOrEmpty(dir))dir=AppDomain.CurrentDomain.BaseDirectory;Directory.CreateDirectory(dir);File.WriteAllText(Path.Combine(dir,"Trainer_scientific_coupler_validation.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(result));}
        catch(Exception ex){Console.WriteLine(ex);return 1;}
        Console.WriteLine((failed==0?"PASS":"FAIL")+": scientific published pair; "+checks.Count+" assertions");return failed==0?0:1;
    }
}
