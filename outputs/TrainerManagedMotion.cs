using System;
using System.IO;
using System.Runtime.InteropServices;
using RailwayDynamics.Simulation;
using RailwayDynamics.Couplers;

partial class TrainerIntegrationHost {
    // Transitional, explicitly uncalibrated profile: one 200t body and 75*80t.
    // No claim of factory rotating inertia or draft-gear identification.
    LongitudinalTrainDynamicsSolver managedMotion;
    TrainerNativeBrakeForceProvider managedBrakeProvider;
    IntPtr managedBrakeModule;
    double[] managedBrakeBase = new double[76];
    double[] managedBrakeNumerator = new double[76], managedBrakeDenominator = new double[76];
    long managedMotionFrames;
    const float MechanicalZeroGaugeTolerance=1e-7f; // 0.009807Pa, below .01Pa at the native gauge interface.
    double managedMinimumRawBrakeGauge;
    static float MechanicalBrakeGauge(float gauge,int index) {
        if(!Finite(gauge) || gauge < -MechanicalZeroGaugeTolerance)
            throw new InvalidOperationException("LTD BC gauge input index="+index+" kgf/cm2="+gauge.ToString("R",System.Globalization.CultureInfo.InvariantCulture));
        // Atmospheric subtraction can leave -8e-17kgf/cm2. Only this bounded
        // numerical zero is normalized for mechanics; pneumatic memory is untouched.
        return gauge<0 ? 0 : gauge;
    }
    double[][] managedGradeSegments, managedCurveSegments;
    partial void DescribeManagedBrakeOracleOverlay(ref IntPtr cave, ref IntPtr source);

    void InitializeManagedMotion(IntPtr module) { using(TrainerManagedFloatingScope.Enter()) InitializeManagedMotionCore(module); }
    void InitializeManagedMotionCore(IntPtr module) {
        if (managedMotion != null) throw new InvalidOperationException("Movement already owned");
        managedBrakeModule = module;
        Log("LTD init: route preparation");
        PrepareManagedRoute();
        Log("LTD init: kernel construction");
        managedMotion = new LongitudinalTrainDynamicsSolver(
            LongitudinalTrainDynamicsSolver.CreateFreightConsist(75, 0),
            FrictionDraftGearModel.CreateDefaultFreightDraftGear());
        Log("LTD init: providers");
        managedMotion.SetBrakeForceProvider(delegate(int i, double v) {
            return managedBrakeBase[i] * (Math.Abs(v) + managedBrakeNumerator[i]) /
                (Math.Abs(v) + managedBrakeDenominator[i]);
        });
        managedMotion.SetRouteForceProviders(ManagedGradeForce, ManagedCurveResistance);
        // Preserve the public host ABI, but route every consumer to the same owner.
        stateTrain = ResetManagedMotion;
        readTrain = ReadManagedMotion;
        motionStep = StepManagedMotion;
        Log("LTD init: reset");
        ResetManagedMotion(position, 0, 2, 76);
        Log("Managed LTD owns motion: 76 bodies, 6200t, 1126.18m; native electrical force / real pneumatic pressures; experimental couplers, gamma=0 compatibility profile");
    }

    void ResetManagedMotion(float front, float speedKmh, int model, int count) { using(TrainerManagedFloatingScope.Enter()) ResetManagedMotionCore(front,speedKmh,model,count); }
    void ResetManagedMotionCore(float front, float speedKmh, int model, int count) {
        if (managedMotion == null || count != 76 || model != 2 || !Finite(front) || !Finite(speedKmh))
            throw new InvalidOperationException("Managed movement reset contract");
        managedMotion.SetInitialState(front, speedKmh / 3.6);
        if(managedMotionFrames==0)Log("LTD reset: kernel ready");
        Array.Clear(managedBrakeBase, 0, 76);
        for(int i=0;i<76;i++) { managedMotion.SetVehicleForce(i,0); managedMotion.SetVehicleDynamicBrakeForceMagnitude(i,0); }
        PublishManagedMotion();
        if(managedMotionFrames==0)Log("LTD reset: publication ready");
    }

    void ReadManagedMotion(out float front, out float velocity, out float frontCoupler, out float accel, out float q, int index) { using(TrainerManagedFloatingScope.Enter()) ReadManagedMotionCore(out front,out velocity,out frontCoupler,out accel,out q,index); }
    void ReadManagedMotionCore(out float front, out float velocity, out float frontCoupler, out float accel, out float q, int index) {
        if (managedMotion == null) throw new InvalidOperationException("Managed movement absent");
        var node = managedMotion.GetVehicle(index);
        front=(float)managedMotion.GetVehicleFrontPosition(index); velocity=(float)node.VelocityV; accel=(float)node.AccelerationA;
        frontCoupler=index==0 ? 0 : (float)(-managedMotion.GetCouplerForceSignedNewtons(index-1)/10000);
        q=index==0 ? -front : (float)(managedMotion.GetVehicleFrontPosition(index-1)-managedMotion.GetVehicle(index-1).LengthM-front);
    }

    void StepManagedMotion(float force, float dt, int locomotive, float otherForce) { using(TrainerManagedFloatingScope.Enter()) StepManagedMotionCore(force,dt,locomotive,otherForce); }
    void StepManagedMotionCore(float force, float dt, int locomotive, float otherForce) {
        if (managedMotion == null || locomotive != 0 || otherForce != 0 || !Finite(force) || !Finite(dt) || dt<=0 || dt>.11f)
            throw new InvalidOperationException("Managed movement input contract");
        if (managedBrakeProvider == null) {
            managedBrakeProvider = new TrainerNativeBrakeForceProvider(managedBrakeModule,
                Path.Combine(resources,"POEZD.DLL"),
                delegate(int index, float pressure) { brakeTrain(pressure,76); },
                delegate { IntPtr cave=IntPtr.Zero,source=IntPtr.Zero;DescribeManagedBrakeOracleOverlay(ref cave,ref source);return new[]{cave,source}; });
            for(int i=0;i<76;i++) {
                managedBrakeNumerator[i]=managedBrakeProvider.GetSpeedNumerator(i);
                managedBrakeDenominator[i]=managedBrakeProvider.GetSpeedDenominator(i);
            }
        }
        float rawLocoGauge=FloatAt(mem["TLocParam"],13);
        float effectiveLocoGauge=MechanicalBrakeGauge(rawLocoGauge,0);
        managedMinimumRawBrakeGauge=Math.Min(managedMinimumRawBrakeGauge,rawLocoGauge);
        float locoPressure=effectiveLocoGauge*8;
        SelectNativeLocomotiveBrakeInput(ref locoPressure);
        var pressures=new float[76]; var velocities=new double[76];
        for(int i=0;i<76;i++) {
            float rawGauge=i==0 ? rawLocoGauge : FloatAt(cars[i],4);
            float effectiveGauge=MechanicalBrakeGauge(rawGauge,i);
            managedMinimumRawBrakeGauge=Math.Min(managedMinimumRawBrakeGauge,rawGauge);
            pressures[i]=i==0 ? locoPressure : effectiveGauge;
            velocities[i]=managedMotion.GetVehicle(i).VelocityV;
            // Gauge kgf/cm2 -> Pa. Loco value is an equivalent native sum,
            // therefore store the actual displayed cylinder gauge separately.
            managedMotion.SetBrakeCylinderPressure(i,effectiveGauge*98066.5);
        }
        var capacity=managedBrakeProvider.Evaluate(pressures,velocities);
        for(int i=0;i<76;i++) managedBrakeBase[i]=capacity[i]*(Math.Abs(velocities[i])+managedBrakeDenominator[i])/(Math.Abs(velocities[i])+managedBrakeNumerator[i]);
        // Reverse drive remains signed. EDT is dissipative and cannot pull a
        // stopped train backwards or supply a fictitious static holding force.
        bool electricalBrake=contactVoltagePatch!=null && contactVoltagePatch.NativeDynamicBrakingActive;
        managedMotion.SetVehicleForce(0,electricalBrake ? 0 : force*10000.0);
        managedMotion.SetVehicleDynamicBrakeForceMagnitude(0,electricalBrake ? Math.Abs(force)*10000.0 : 0);
        managedMotion.Step(dt);
        managedMotionFrames++;
    }

    double ManagedGradeForce(int index,double centre) {
        var node=managedMotion.GetVehicle(index);double rear=centre-node.LengthM/2,front=centre+node.LengthM/2,sum=0;
        if(route==null)throw new InvalidOperationException("Movement route absent");
        // During the final boundary frame use the nearest endpoint profile;
        // the existing route boundary policy pauses immediately afterwards.
        sum=ManagedOverlapIntegral(managedGradeSegments,rear,front);
        if(rear<0)sum+=Math.Max(0,Math.Min(front,0)-rear)*managedGradeSegments[0][2];
        if(front>route.Length)sum+=Math.Max(0,front-Math.Max(rear,route.Length))*managedGradeSegments[managedGradeSegments.Length-1][2];
        return -node.MassKg*9.80665*.001*sum/node.LengthM;
    }
    double ManagedCurveResistance(int index,double centre) {
        var node=managedMotion.GetVehicle(index);double rear=centre-node.LengthM/2,front=centre+node.LengthM/2,sum=0;
        // Retained native law: 0.7*m_t/R kN, weighted by body overlap.
        sum=ManagedOverlapIntegral(managedCurveSegments,rear,front);
        return .7*node.MassKg*sum/node.LengthM;
    }
    void PrepareManagedRoute() {
        if(route==null || route.profile.Count==0)throw new InvalidOperationException("Movement profile absent");
        var grades=new System.Collections.Generic.List<double[]>();
        var curves=new System.Collections.Generic.List<double[]>();
        Log("LTD route: collect grades");
        foreach(var p in route.profile)grades.Add(new[]{route.X(p.km_start),route.X(p.km_end),p.grade_permille});
        Log("LTD route: collect curves");
        foreach(var c in route.curves) {
            if(c.radius_m<=0 || Double.IsNaN(c.radius_m) || Double.IsInfinity(c.radius_m))throw new InvalidOperationException("Invalid curve radius");
            curves.Add(new[]{route.X(c.km_start),route.X(c.km_end),1/c.radius_m});
        }
        grades.Sort(delegate(double[] a,double[] b){return a[0].CompareTo(b[0]);});
        Log("LTD route: grades sorted");
        curves.Sort(delegate(double[] a,double[] b){return a[0].CompareTo(b[0]);});
        Log("LTD route: curves sorted");
        double end=0;
        foreach(var p in grades) {if(Math.Abs(p[0]-end)>.001 || p[1]<=p[0] || Double.IsNaN(p[2]) || Double.IsInfinity(p[2]))throw new InvalidOperationException("Route profile coverage guard");end=p[1];}
        if(Math.Abs(end-route.Length)>.001)throw new InvalidOperationException("Route full length guard");
        Log("LTD route: grade coverage ready");
        end=-Double.MaxValue;foreach(var c in curves) {if(c[0]<end-.001 || c[1]<=c[0])throw new InvalidOperationException("Route curve overlap guard");end=c[1];}
        managedGradeSegments=grades.ToArray();managedCurveSegments=curves.ToArray();
    }
    static double ManagedOverlapIntegral(double[][] segments,double rear,double front) {
        int lo=0,hi=segments.Length;
        while(lo<hi){int mid=lo+(hi-lo)/2;if(segments[mid][1]<=rear)lo=mid+1;else hi=mid;}
        double sum=0;
        for(int i=lo;i<segments.Length && segments[i][0]<front;i++)sum+=Math.Max(0,Math.Min(front,segments[i][1])-Math.Max(rear,segments[i][0]))*segments[i][2];
        return sum;
    }
    void PublishManagedMotion() { using(TrainerManagedFloatingScope.Enter()) PublishManagedMotionCore(); }
    void PublishManagedMotionCore() {
        double measuredMaximum=0;
        for(int n=0;n<76;n++) {
            float x,v,coupler,a,q;ReadManagedMotion(out x,out v,out coupler,out a,out q,n);
            if(!Finite(x)||!Finite(v)||!Finite(coupler)||!Finite(a)||!Finite(q))throw new InvalidOperationException("Invalid LTD publication "+n);
            measuredMaximum=Math.Max(measuredMaximum,Math.Abs((double)v)*3.6);
            F(cars[n],0xc,coupler);F(cars[n],0x10,a);F(cars[n],0x14,x);
            if(n==0) {position=x;signedVelocity=v;acceleration=a;F(mem["TInputParamPult"],1,Math.Abs(v)*3.6f);F(mem["TInputParamPult"],0xd,x);I(mem["TInputParamPult"],0x11,(int)x);F(mem["TInputParamPult"],0x19,a);}
        }
        maximumConsistSpeedKmh=measuredMaximum;
    }
    void CloseManagedMotion() {
        TrainerManagedFloatingScope.DisposeRetainedOwners();
        if(TrainerManagedFloatingScope.HasRetainedOwners)throw new InvalidOperationException("Native close refused while a floating-point environment remains owned");
        if(managedBrakeProvider!=null) {managedBrakeProvider.Dispose();managedBrakeProvider=null;}
        TrainerNativeBrakeForceProvider.DisposeRetainedOwners();
        if(TrainerNativeBrakeForceProvider.HasRetainedOwners)throw new InvalidOperationException("Native close refused while movement oracle ownership remains");
        managedMotion=null;stateTrain=null;readTrain=null;motionStep=null;
        TrainerManagedFloatingScope.Shutdown();
    }
}
