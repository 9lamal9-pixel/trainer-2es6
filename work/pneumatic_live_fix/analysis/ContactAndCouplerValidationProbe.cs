using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Couplers;
using RailwayDynamics.Traction;

// Pure managed component validation. No Host/native DLL or desktop interaction.
public sealed class ContactAndCouplerValidationProbe {
    static readonly List<object> checks=new List<object>();
    static int failed;
    static void Check(string name,bool ok,object actual){checks.Add(new{name,passed=ok,actual});if(!ok)failed++;}
    static void Near(string name,double actual,double expected,double tolerance){Check(name,Finite(actual)&&Math.Abs(actual-expected)<=tolerance,new{actual,expected,tolerance});}
    static bool Finite(double v){return !double.IsNaN(v)&&!double.IsInfinity(v);}
    static void Reject(string name,Action action){bool rejected=false;try{action();}catch(ArgumentException){rejected=true;}Check(name,rejected,rejected);}
    static void Contact(double speed,double slip,double lateral,PolachParameters p,out double fx,out double fy){
        PolachAdhesionSolver.ComputeCreepForces(122600,speed,(speed+slip)/.605,.605,lateral,.0065,.0055,4.12,3.65,p,out fx,out fy);
    }
    static void ContactChecks(){
        double fx,fy;
        Contact(20,0,0,PolachParameters.DryRail,out fx,out fy);Near("rolling has zero Fx",fx,0,0);Near("rolling has zero Fy",fy,0,0);
        // Independent fixed Kalker slope: .5*(1+.4)*8.4e10*.0065*.0055*4.12.
        const double smallCreep=1e-8,expectedSlope=8660652;
        Contact(20,20*smallCreep,0,PolachParameters.DryRail,out fx,out fy);
        Near("Polach Eq4/11 small-creep slope includes pi/4",fx/smallCreep,expectedSlope,expectedSlope*5e-6);
        foreach(double speed in new[]{-20.0,0.0,20.0})foreach(double slip in new[]{-.4,.4})foreach(double lateral in new[]{-.3,0,.3}){
            Contact(speed,slip,lateral,PolachParameters.DryRail,out fx,out fy);
            double w=Math.Sqrt(slip*slip+lateral*lateral);
            double mu=.55*(.6*Math.Exp(-.6*w)+.4);
            Check("contact passive and friction-bounded "+speed+"/"+slip+"/"+lateral,
                Finite(fx)&&Finite(fy)&&fx*slip-fy*lateral>=-1e-8&&Math.Sqrt(fx*fx+fy*fy)<=122600*mu*(1+1e-12),new{fx,fy,dissipation_W=fx*slip-fy*lateral});
        }
        Contact(20,.4,.2,PolachParameters.DryRail,out fx,out fy);double posX=fx,posY=fy;
        Contact(-20,-.4,-.2,PolachParameters.DryRail,out fx,out fy);Near("contact reversal Fx",fx,-posX,1e-7);Near("contact reversal Fy",fy,-posY,1e-7);
        Contact(20,.4,.2,new PolachParameters(0,.4,.6,1,.4),out fx,out fy);Near("zero mu Fx finite zero",fx,0,0);Near("zero mu Fy finite zero",fy,0,0);
        Contact(20,10,0,new PolachParameters(.5,0,1e4,1,.4),out fx,out fy);Near("underflow frictionless limit",fx,0,0);
        PolachAdhesionSolver.ComputeCreepForces(0,20,35,.605,0,.0065,.0055,4.12,3.65,PolachParameters.DryRail,out fx,out fy);Near("unloaded wheel zero force",fx,0,0);
        Contact(20,1e300,0,PolachParameters.DryRail,out fx,out fy);Check("large slip saturates without Inf/Inf",Finite(fx)&&Finite(fy)&&fx>=0&&fx<=122600*.55,new{fx,fy});
        Reject("reject NaN speed",delegate{double x,y;Contact(double.NaN,.4,0,PolachParameters.DryRail,out x,out y);});
        Reject("reject negative mu",delegate{double x,y;Contact(20,.4,0,new PolachParameters(-.1,.4,.6,1,.4),out x,out y);});
        Reject("reject Ks greater than Ka",delegate{double x,y;Contact(20,.4,0,new PolachParameters(.5,.4,.6,.1,.4),out x,out y);});
        Reject("reject zero contact geometry",delegate{double x,y;PolachAdhesionSolver.ComputeCreepForces(1,1,1,1,0,0,1,1,1,PolachParameters.DryRail,out x,out y);});
    }
    static void CouplerChecks(){
        var model=FrictionDraftGearModel.CreateDefaultFreightDraftGear();
        var state=model.InitializeState(0);
        foreach(double x in new[]{-.01,-.008,0,.008,.01}){double f=model.ComputeCouplerForce(x,0,ref state);Near("slack zero "+x,f,0,0);Near("slack energy zero "+x,model.GetStoredEnergy(state),0,0);}
        state=model.InitializeState(0);double loadingAt30=0,unloadingAt30=0,workEnd=0,workTrap=0,prevX=0,prevF=0,minStepD=double.PositiveInfinity;
        double[] turns={.03,.07,.03,.055,-.07,0};
        foreach(double end in turns){double start=prevX;int count=(int)Math.Ceiling(Math.Abs(end-start)/.0001);
            for(int n=1;n<=count;n++){
                double x=n==count?end:start+(end-start)*n/count;double before=model.GetStoredEnergy(state);
                double f=model.ComputeCouplerForce(x,Math.Sign(end-start)*.02,ref state),dx=x-prevX;
                workEnd+=f*dx;workTrap+=.5*(f+prevF)*dx;
                double stepD=f*dx-(model.GetStoredEnergy(state)-before);minStepD=Math.Min(minStepD,stepD);
                if(start==0&&end==.03&&n==count)loadingAt30=f;
                if(start==.07&&end==.03&&n==count)unloadingAt30=f;
                prevX=x;prevF=f;
            }
        }
        Check("same-deflection hysteresis has distinct branches",loadingAt30>unloadingAt30&&unloadingAt30>0,new{loadingAt30,unloadingAt30});
        Check("closed loop physical trapezoid work dissipative",Finite(workTrap)&&workTrap>0,workTrap);
        Check("every endpoint increment passive",minStepD>=-1e-6,minStepD);
        Near("closed loop releases stored energy",model.GetStoredEnergy(state),0,1e-8);
        Near("endpoint work equals dissipated plus final stored energy",workEnd,state.DissipatedEnergyJ+state.StoredEnergyJ-state.InitialStoredEnergyJ,1e-5*Math.Max(1,Math.Abs(workEnd)));
        // A displacement reversal initially unloads elastically; changing only
        // velocity does not jump force or introduce stored energy.
        state=model.InitializeState(0);model.ComputeCouplerForce(.07,.1,ref state);double forceAtTurn=state.PreviousForce,energyAtTurn=state.StoredEnergyJ;
        double held=model.ComputeCouplerForce(.07,-.1,ref state);Near("locked unloading no force jump at reversal",held,forceAtTurn,1e-8);Near("held reversal no generated energy",state.StoredEnergyJ,energyAtTurn,1e-8);
        double reverse=model.ComputeCouplerForce(.07-1e-7,-.1,ref state);Check("locked unloading finite stiffness",Math.Abs(reverse-held)<=model.GetTangentStiffness(.07)*1e-7*(1+1e-6),new{held,reverse});
        // Preloaded geometry has explicit initial potential, not a free loading
        // trajectory guessed from velocity on the first call.
        var preload=model.InitializeState(.04);Check("preload energy explicitly recorded",preload.InitialStoredEnergyJ>0&&preload.StoredEnergyJ==preload.InitialStoredEnergyJ&&preload.DissipatedEnergyJ==0,preload.StoredEnergyJ);
        double neutral=preload.PreviousForce;model.ComputeCouplerForce(.04,-1,ref preload);Near("preload stationary force not velocity-defined",preload.PreviousForce,neutral,1e-8);
        double gradient=(model.ComputePotentialEnergy(.0400001)-model.ComputePotentialEnergy(.0399999))/2e-7;
        Near("potential derivative equals neutral backbone",gradient,neutral,1e-5*Math.Max(1,neutral));
        // Fixed-history finite differences include contact and rigid regions.
        foreach(double x in new[]{-.12,-.04,-.0100001,0,.0100001,.04,.12}){
            var reference=model.InitializeState(x);var left=reference;var right=reference;
            double l=model.ComputeCouplerForce(x-1e-8,-.1,ref left),r=model.ComputeCouplerForce(x+1e-8,.1,ref right);
            double measured=Math.Abs(r-l)/2e-8,bound=model.GetTangentStiffness(x);
            Check("conservative tangent bound "+x,Finite(measured)&&measured<=bound*(1+1e-5),new{measured,bound});
        }
        Check("rigid extension included in tangent bound",model.GetTangentStiffness(.2)>=2.5e8,model.GetTangentStiffness(.2));
        var positive=model.InitializeState(0);var negative=model.InitializeState(0);
        foreach(double x in new[]{.02,.04,.07,.04,.02,0}){
            double a=model.ComputeCouplerForce(x,0,ref positive),b=model.ComputeCouplerForce(-x,0,ref negative);
            Near("gear sign symmetry "+x,b,-a,1e-7);Near("gear mirrored stored energy "+x,negative.StoredEnergyJ,positive.StoredEnergyJ,1e-7);
        }
        // Equal envelopes remove physical friction hysteresis. This is a
        // separate quadrature of F dx, not the model's bookkeeping field.
        var elastic=new FrictionDraftGearModel(new double[]{0,.08},new double[]{0,8e5},new double[]{0,8e5});
        var es=elastic.InitializeState(0);double elasticWork=0,elasticX=0,elasticF=0;
        foreach(double end in new[]{.05,0.0}){
            double start=elasticX;
            for(int n=1;n<=1000;n++){
                double x=n==1000?end:start+(end-start)*n/1000;
                double f=elastic.ComputeCouplerForce(x,0,ref es);
                elasticWork+=.5*(f+elasticF)*(x-elasticX);elasticX=x;elasticF=f;
            }
        }
        Near("equal-envelope closed work is conservative",elasticWork,0,1e-6);
        var nearSlack=model.InitializeState(.01);double contactForce=model.ComputeCouplerForce(.010000000001,.1,ref nearSlack);
        Check("continuous force at slack boundary",contactForce>=0&&contactForce<.01,contactForce);
        double[] xs={0,.08},up={0,1e6},down={0,2e5};var cloned=new FrictionDraftGearModel(xs,up,down);double potential=cloned.ComputePotentialEnergy(.04);up[1]=double.NaN;Near("tables defensively copied",cloned.ComputePotentialEnergy(.04),potential,0);
        Reject("reject crossed envelopes",delegate{new FrictionDraftGearModel(new double[]{0,.1},new double[]{0,1},new double[]{0,2});});
        Reject("reject unsorted strokes",delegate{new FrictionDraftGearModel(new double[]{0,.1,.05},new double[]{0,1,2},new double[]{0,0,1});});
        Reject("reject zero contact length",delegate{new FrictionDraftGearModel(new double[]{0,.1},new double[]{0,1},new double[]{0,0},.02,0);});
        Reject("reject nonfinite gear input",delegate{var s=model.InitializeState(0);model.ComputeCouplerForce(double.NaN,0,ref s);});
        Reject("prestrain cannot silently create initial energy",delegate{var s=new CouplerState();model.ComputeCouplerForce(.04,0,ref s);});
        Reject("reject foreign state",delegate{var other=FrictionDraftGearModel.CreateDefaultFreightDraftGear();var s=other.InitializeState(0);model.ComputeCouplerForce(.03,.1,ref s);});
        Reject("reject tampered energy state",delegate{var s=model.InitializeState(.03);s.StoredEnergyJ+=100;model.ComputeCouplerForce(.031,.1,ref s);});
    }
    public static int Main(){
        string error=null;try{ContactChecks();CouplerChecks();}catch(Exception ex){failed++;error=ex.ToString();}
        var result=new{passed=failed==0,error,failed_assertions=failed,checks_count=checks.Count,checks,
            native_executed=false,scope="Managed no-spin contact and passive illustrative gear only. Not factory Polach/SA3 calibration, thermal/spin physics, native/host integration or full train acceptance."};
        try{string dir=Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");if(String.IsNullOrEmpty(dir))dir=AppDomain.CurrentDomain.BaseDirectory;
            Directory.CreateDirectory(dir);File.WriteAllText(Path.Combine(dir,"Trainer_contact_and_coupler_validation.json"),new JavaScriptSerializer{MaxJsonLength=16000000}.Serialize(result));}
        catch(Exception ex){Console.WriteLine(ex);return 1;}
        Console.WriteLine((failed==0?"PASS":"FAIL")+": contact and coupler components; "+checks.Count+" assertions");return failed==0?0:1;
    }
}
