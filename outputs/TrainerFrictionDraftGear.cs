using System;
using System.Threading;

namespace RailwayDynamics.Couplers {
    // Copy state for trial evaluation; commit only an accepted step.
    public struct CouplerState {
        public double PreviousDeflection, PreviousForce, PreviousDisplacement, InternalFrictionForce;
        public double InitialStoredEnergyJ, StoredEnergyJ, DissipatedEnergyJ, LastIncrementWorkJ;
        public bool Initialized;
        internal int ModelId;
    }

    /// <summary>
    /// Passive illustrative tabular gear: convex spring plus elastic/plastic stop.
    /// NOT a calibrated SA-3/Sh-2-V/PMK-110, nor the unverified exponential TDEAS law.
    /// dx>0 means compression. Returned F acts forward on the leading vehicle,
    /// backward on the trailing one. Mechanical input work is F*d(dx).
    /// </summary>
    public sealed class FrictionDraftGearModel {
        public const string CalibrationScope="Illustrative envelopes only; no factory calibration";
        readonly double[] stroke,loading,unloading;
        readonly double halfSlack,contactLength,rigidStiffness,stopStiffness,stiffnessBound;
        readonly int modelId;
        static int nextModelId;
        public FrictionDraftGearModel(double[] strokeTable,double[] loadingEnvelope,double[] unloadingEnvelope,
            double totalCouplerSlack=.020,double transitionLengthXtr=.0015,double smoothingVelocityV0=.005,double lockedRigidStiffness=2.5e8) {
            if(strokeTable==null||loadingEnvelope==null||unloadingEnvelope==null||strokeTable.Length<2||loadingEnvelope.Length!=strokeTable.Length||unloadingEnvelope.Length!=strokeTable.Length)
                throw new ArgumentException("Gear tables must have equal lengths >= 2");
            Nonnegative(totalCouplerSlack,"totalCouplerSlack");Positive(transitionLengthXtr,"transitionLengthXtr");
            Nonnegative(smoothingVelocityV0,"smoothingVelocityV0");Nonnegative(lockedRigidStiffness,"lockedRigidStiffness");
            // Legacy smoothingVelocityV0 is compatibility-only: this passive law is rate independent.
            stroke=(double[])strokeTable.Clone();loading=(double[])loadingEnvelope.Clone();unloading=(double[])unloadingEnvelope.Clone();
            if(stroke[0]!=0)throw new ArgumentException("Gear stroke table must start at zero");
            double maxSlope=lockedRigidStiffness,maxGap=0;
            for(int i=0;i<stroke.Length;i++) {
                Nonnegative(stroke[i],"stroke");Nonnegative(loading[i],"loading");Nonnegative(unloading[i],"unloading");
                if(unloading[i]>loading[i])throw new ArgumentException("Unloading must not exceed loading");
                maxGap=Math.Max(maxGap,loading[i]-unloading[i]);
                if(i>0) {
                    if(stroke[i]<=stroke[i-1]||loading[i]<loading[i-1]||unloading[i]<unloading[i-1])throw new ArgumentException("Increasing strokes and monotone envelopes required");
                    maxSlope=Math.Max(maxSlope,Math.Max((loading[i]-loading[i-1])/(stroke[i]-stroke[i-1]),(unloading[i]-unloading[i-1])/(stroke[i]-stroke[i-1])));
                }
            }
            halfSlack=totalCouplerSlack*.5;contactLength=transitionLengthXtr;rigidStiffness=lockedRigidStiffness;
            // Reversal is elastic (locked unloading) until the opposite envelope is reached.
            stopStiffness=Math.Max(1,Math.Max(maxSlope,maxGap/contactLength));
            stiffnessBound=2*maxSlope+loading[0]/contactLength+stopStiffness;
            if(!Finite(stiffnessBound))throw new ArgumentException("Gear stiffness bound overflow");
            modelId=Interlocked.Increment(ref nextModelId);
        }
        public static FrictionDraftGearModel CreateDefaultFreightDraftGear() {
            // Original example values retained, NOT validated device data.
            return new FrictionDraftGearModel(new double[]{0,.010,.025,.045,.065,.080},
                new double[]{8e4,1.8e5,3.8e5,7.5e5,1.4e6,2.2e6},new double[]{2e4,5e4,1e5,2.2e5,4.5e5,7e5});
        }
        // Initial prestrain and neutral stop history; its potential is explicitly initial energy.
        public CouplerState InitializeState(double displacementM) {
            CheckFinite(displacementM,"displacementM");double fu,fl;Envelopes(Deflection(displacementM),out fu,out fl);
            double energy=ComputePotentialEnergy(displacementM);
            return new CouplerState{Initialized=true,ModelId=modelId,PreviousDisplacement=displacementM,
                PreviousDeflection=Deflection(displacementM),PreviousForce=Math.Sign(displacementM)*.5*(fu+fl),InitialStoredEnergyJ=energy,StoredEnergyJ=energy};
        }
        // Conservative global N/m bound includes contact regularization and rigid extension.
        public double GetTangentStiffness(double displacementM){CheckFinite(displacementM,"displacementM");return stiffnessBound;}
        public double GetStoredEnergy(CouplerState state){if(!state.Initialized)return 0;ValidateState(state);return state.StoredEnergyJ;}
        // Convex backbone potential in joules; stop energy is additional history state.
        public double ComputePotentialEnergy(double displacementM) {
            CheckFinite(displacementM,"displacementM");double x=Deflection(displacementM),sum=0;
            for(int i=0;i<stroke.Length-1&&x>stroke[i];i++) {
                double end=Math.Min(x,stroke[i+1]),f0=.5*(loading[i]+unloading[i]);
                double slope=.5*((loading[i+1]-loading[i])+(unloading[i+1]-unloading[i]))/(stroke[i+1]-stroke[i]);
                sum+=Integrate(stroke[i],end,f0,slope);
            }
            int last=stroke.Length-1;
            if(x>stroke[last])sum+=Integrate(stroke[last],x,.5*(loading[last]+unloading[last]),rigidStiffness);
            if(!Finite(sum))throw new ArithmeticException("Gear potential overflow");return sum;
        }
        public double ComputeCouplerForce(double relativeDisplacementDx,double relativeVelocityDv,ref CouplerState state) {
            CheckFinite(relativeDisplacementDx,"relativeDisplacementDx");CheckFinite(relativeVelocityDv,"relativeVelocityDv");
            if(!state.Initialized){
                if(Deflection(relativeDisplacementDx)>0)throw new ArgumentException("Preloaded geometry requires explicit InitializeState so initial energy is accounted");
                state=InitializeState(relativeDisplacementDx);return state.PreviousForce;
            }
            ValidateState(state);
            double dx=relativeDisplacementDx-state.PreviousDisplacement;
            if(!Finite(dx))throw new ArithmeticException("Gear displacement increment overflow");
            double x=Deflection(relativeDisplacementDx),fu,fl;Envelopes(x,out fu,out fl);
            double yield=.5*(fu-fl),trial=state.InternalFrictionForce+stopStiffness*dx;
            if(!Finite(trial))throw new ArithmeticException("Gear force overflow");
            double internalForce=Math.Max(-yield,Math.Min(yield,trial));
            double force=Math.Sign(relativeDisplacementDx)*.5*(fu+fl)+internalForce;
            double energy=ComputePotentialEnergy(relativeDisplacementDx)+internalForce*internalForce/(2*stopStiffness);
            double work=force*dx,dissipation=work-(energy-state.StoredEnergyJ);
            if(!Finite(energy)||!Finite(work)||!Finite(dissipation))throw new ArithmeticException("Gear energy overflow");
            // Convex end-point spring work plus projected-stop work is passive.
            // Tolerance covers cancellation only; it never changes the returned force.
            double tolerance=1e-10*Math.Max(1,Math.Max(Math.Abs(work),Math.Max(energy,state.StoredEnergyJ)));
            if(dissipation < -tolerance)throw new ArithmeticException("Gear passivity invariant failed");
            state.PreviousDisplacement=relativeDisplacementDx;state.PreviousDeflection=x;state.PreviousForce=force;
            state.InternalFrictionForce=internalForce;state.StoredEnergyJ=energy;state.LastIncrementWorkJ=work;
            state.DissipatedEnergyJ+=Math.Max(0,dissipation);return force;
        }
        void ValidateState(CouplerState state) {
            if(state.ModelId!=modelId)throw new ArgumentException("Coupler state belongs to another gear");
            CheckFinite(state.PreviousDisplacement,"state displacement");CheckFinite(state.InternalFrictionForce,"state force");
            Nonnegative(state.StoredEnergyJ,"state energy");Nonnegative(state.DissipatedEnergyJ,"state dissipation");
            double expected=ComputePotentialEnergy(state.PreviousDisplacement)+state.InternalFrictionForce*state.InternalFrictionForce/(2*stopStiffness);
            if(Math.Abs(expected-state.StoredEnergyJ)>1e-9*Math.Max(1,expected))throw new ArgumentException("Coupler energy state inconsistent");
        }
        double Deflection(double displacement){return Math.Max(0,Math.Abs(displacement)-halfSlack);}
        void Envelopes(double x,out double fu,out double fl) {
            if(x==0){fu=fl=0;return;}int last=stroke.Length-1;
            if(x>=stroke[last]){fu=loading[last]+rigidStiffness*(x-stroke[last]);fl=unloading[last]+rigidStiffness*(x-stroke[last]);}
            else{int i=0;while(x>stroke[i+1])i++;double t=(x-stroke[i])/(stroke[i+1]-stroke[i]);fu=loading[i]+t*(loading[i+1]-loading[i]);fl=unloading[i]+t*(unloading[i+1]-unloading[i]);}
            // Continuous contact, instead of an 80kN jump at the slack boundary.
            double ramp=Math.Min(1,x/contactLength);fu*=ramp;fl*=ramp;
            if(!Finite(fu)||!Finite(fl))throw new ArithmeticException("Gear envelope overflow");
        }
        double Integrate(double left,double right,double fLeft,double slope) {
            if(right<=left)return 0;double sum=0;
            if(left<contactLength){double end=Math.Min(right,contactLength),d=end-left;
                sum=(fLeft*left*d+.5*(fLeft+slope*left)*d*d+slope*d*d*d/3)/contactLength;
                fLeft+=slope*d;left=end;}
            double rest=right-left;return sum+fLeft*rest+.5*slope*rest*rest;
        }
        static bool Finite(double v){return !double.IsNaN(v)&&!double.IsInfinity(v);}
        static void CheckFinite(double v,string name){if(!Finite(v))throw new ArgumentOutOfRangeException(name);}
        static void Nonnegative(double v,string name){CheckFinite(v,name);if(v<0)throw new ArgumentOutOfRangeException(name);}
        static void Positive(double v,string name){CheckFinite(v,name);if(v<=0)throw new ArgumentOutOfRangeException(name);}
    }
}
