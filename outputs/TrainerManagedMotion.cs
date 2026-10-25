using System;
using System.IO;
using System.Runtime.InteropServices;
using RailwayDynamics.Simulation;
using RailwayDynamics.Couplers;

partial class TrainerIntegrationHost {
    // User-selected scientific reference scenario; equipment identity is explicit.
    LongitudinalTrainDynamicsSolver managedMotion;
    TrainerScientificMechanicsProfile scientificMechanics;
    TrainerScientificBrakeFrame scientificBrakeFrame;
    double managedEffectiveMassKg;
    bool scientificBogieFallbackLogged;
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
    partial void ReadScientificBogiePressures(double[] gaugeKgfCm2,ref bool complete);
    partial void SupportsNativeConsistSeparation(ref bool supported);
    partial void ApplyNativeConsistSeparation(int linkIndex,ref bool applied);
    partial void ValidateNativeConsistSeparation();
    partial void RestoreNativeConsistSeparation();
    readonly bool[] managedSeparatedAirLinks=new bool[75];
    void SyncManagedConsistSeparation() {
        ValidateNativeConsistSeparation();
        for(int i=0;i<managedSeparatedAirLinks.Length;i++)if(managedMotion.IsCouplerBroken(i)&&!managedSeparatedAirLinks[i]) {
            bool applied=false;ApplyNativeConsistSeparation(i,ref applied);
            if(!applied)throw new InvalidOperationException("Broken SA-3 has no pneumatic separation adapter");
            managedSeparatedAirLinks[i]=true;
            Log("SA-3 fracture link="+(i+1)+" demanded_tension_N="+managedMotion.GetCouplerFailureTensionNewtons(i).ToString("R",System.Globalization.CultureInfo.InvariantCulture)+" mechanical_t="+managedMotion.GetCouplerFailureTimeSeconds(i).ToString("R",System.Globalization.CultureInfo.InvariantCulture)+"; hoses separated; actual cocks retained; parts="+managedMotion.ComponentCount);
        }
    }

    void InitializeManagedMotion(IntPtr module) { using(TrainerManagedFloatingScope.Enter()) InitializeManagedMotionCore(module); }
    void InitializeManagedMotionCore(IntPtr module) {
        if (managedMotion != null) throw new InvalidOperationException("Movement already owned");
        Log("LTD init: route preparation");
        PrepareManagedRoute();
        Log("LTD init: kernel construction");
        scientificMechanics=TrainerScientificMechanicsProfile.Load(Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,@"..\dev\config\train-mechanics.json")));
        scientificBrakeFrame=new TrainerScientificBrakeFrame(scientificMechanics);
        var vehicles=scientificMechanics.CreateVehicles();managedEffectiveMassKg=0;
        foreach(var vehicle in vehicles)managedEffectiveMassKg+=vehicle.MassKg*(1+vehicle.RotaryInertiaGamma);
        managedMotion = new LongitudinalTrainDynamicsSolver(vehicles,scientificMechanics.CreateCouplers());
        bool separationSupported=false;SupportsNativeConsistSeparation(ref separationSupported);
        if(scientificMechanics.coupler_integrity.enabled&&separationSupported)
            managedMotion.ConfigureCouplerIntegrity(scientificMechanics.coupler_integrity.nominal_tensile_fracture_N,scientificMechanics.coupler_integrity.design_compression_N);
        else if(scientificMechanics.coupler_integrity.enabled)
            Log("SA-3 fracture disabled in integration-only profile: native hose separation adapter unavailable");
        Log("LTD init: providers");
        managedMotion.SetBrakeForceProvider(delegate(int i, double v) {
            return scientificBrakeFrame.CapacityNewtons(i,v);
        });
        managedMotion.SetRouteForceProviders(ManagedGradeForce, ManagedCurveResistance);
        // Preserve the public host ABI, but route every consumer to the same owner.
        stateTrain = ResetManagedMotion;
        readTrain = ReadManagedMotion;
        motionStep = StepManagedMotion;
        Log("LTD init: reset");
        ResetManagedMotion(position, 0, 2, 76);
        Log("Managed LTD scientific reference: 76 bodies, physical6200t/effective"+(managedEffectiveMassKg/1000).ToString("F3",System.Globalization.CultureInfo.InvariantCulture)+"t,1126.18m; published two-gear Sh2V pair; direct SI brake rigging/friction from actual pressures");
    }

    void ResetManagedMotion(float front, float speedKmh, int model, int count) { using(TrainerManagedFloatingScope.Enter()) ResetManagedMotionCore(front,speedKmh,model,count); }
    void ResetManagedMotionCore(float front, float speedKmh, int model, int count) {
        if (managedMotion == null || count != 76 || model != 2 || !Finite(front) || !Finite(speedKmh))
            throw new InvalidOperationException("Managed movement reset contract");
        trainForceEpoch++;
        managedMotion.SetInitialState(front, speedKmh / 3.6);
        if(managedMotionFrames==0)Log("LTD reset: kernel ready");
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
        float rawLocoGauge=FloatAt(mem["TLocParam"],13);
        float effectiveLocoGauge=MechanicalBrakeGauge(rawLocoGauge,0);
        double minimumRawGauge=Math.Min(managedMinimumRawBrakeGauge,rawLocoGauge);
        var pressuresPa=new double[76];var bogieGauge=new double[4];bool fourMeasured=false;
        ReadScientificBogiePressures(bogieGauge,ref fourMeasured);
        if(!fourMeasured) {
            // Integration-only fallback: equal four-bogie reference pressure.
            // The candidate's accepted path always supplies four actual gauges.
            for(int b=0;b<4;b++)bogieGauge[b]=effectiveLocoGauge;
            if(!scientificBogieFallbackLogged){Log("Integration reference fallback: four equal bogie pressures inferred from displayed TC; candidate requires four actual gauges");scientificBogieFallbackLogged=true;}
        }
        var bogiePa=new double[4];
        for(int b=0;b<4;b++) {
            if(Double.IsNaN(bogieGauge[b])||Double.IsInfinity(bogieGauge[b])||bogieGauge[b]<-MechanicalZeroGaugeTolerance)
                throw new InvalidOperationException("Invalid scientific bogie gauge "+b);
            bogiePa[b]=Math.Max(0,bogieGauge[b])*98066.5;
        }
        for(int i=0;i<76;i++) {
            float rawGauge=i==0 ? rawLocoGauge : FloatAt(cars[i],4);
            float effectiveGauge=MechanicalBrakeGauge(rawGauge,i);
            minimumRawGauge=Math.Min(minimumRawGauge,rawGauge);
            pressuresPa[i]=effectiveGauge*98066.5;
            // Displayed loco BC is the first bogie's actual gauge. Forces use
            // all four bogies separately; no nonlinear pressure averaging.
        }
        scientificBrakeFrame.SetPressures(pressuresPa,bogiePa);
        // All source gauges and force arithmetic have passed before publishing
        // either the frame or per-body pressure metadata.
        for(int i=0;i<76;i++)managedMotion.SetBrakeCylinderPressure(i,pressuresPa[i]);
        managedMinimumRawBrakeGauge=minimumRawGauge;
        // Reverse drive remains signed. EDT is dissipative and cannot pull a
        // stopped train backwards or supply a fictitious static holding force.
        bool electricalBrake=contactVoltagePatch!=null && contactVoltagePatch.NativeDynamicBrakingActive;
        managedMotion.SetVehicleForce(0,electricalBrake ? 0 : force*10000.0);
        managedMotion.SetVehicleDynamicBrakeForceMagnitude(0,electricalBrake ? Math.Abs(force)*10000.0 : 0);
        managedMotion.Step(dt);
        SyncManagedConsistSeparation();
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
        CaptureTrainForces();
    }
    void CloseManagedMotion() {
        CloseTrainForceMode();
        RestoreNativeConsistSeparation();
        TrainerManagedFloatingScope.DisposeRetainedOwners();
        if(TrainerManagedFloatingScope.HasRetainedOwners)throw new InvalidOperationException("Native close refused while a floating-point environment remains owned");
        TrainerNativeBrakeForceProvider.DisposeRetainedOwners();
        if(TrainerNativeBrakeForceProvider.HasRetainedOwners)throw new InvalidOperationException("Native close refused while movement oracle ownership remains");
        managedMotion=null;scientificBrakeFrame=null;scientificMechanics=null;stateTrain=null;readTrain=null;motionStep=null;
        TrainerManagedFloatingScope.Shutdown();
    }
}
