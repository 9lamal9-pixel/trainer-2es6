using System;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Simulation;
using RailwayDynamics.Inertia;
using RailwayDynamics.Couplers;
using RailwayDynamics.Braking;

// Explicit literature/standard reference scenario, selected by the user.
// Equipment identity is not inferred from a vehicle's mass or the native AR bit.
internal sealed class TrainerScientificMechanicsProfile {
    public ScientificCouplerIntegrity coupler_integrity {get;set;}
    public TrainerScientificMechanicsProfile() {}
    public int schema_version {get;set;}
    public int wagon_count {get;set;}
    public string profile_id {get;set;}
    public string description {get;set;}
    public string coupler_profile {get;set;}
    public double locomotive_mass_kg {get;set;}
    public double wagon_mass_kg {get;set;}
    public double locomotive_reference_gamma {get;set;}
    public double wagon_reference_gamma {get;set;}
    public ScientificBrakeEquipment wagon_brakes {get;set;}
    public ScientificBrakeEquipment locomotive_bogie_brakes {get;set;}
    public static TrainerScientificMechanicsProfile Load(string path) {
        var profile=new JavaScriptSerializer {MaxJsonLength=262144}.Deserialize<TrainerScientificMechanicsProfile>(File.ReadAllText(path));
        if(profile==null || profile.schema_version!=1 || profile.profile_id!="2es6-75x80t-scientific-reference-v1" ||
            profile.wagon_count!=75 || profile.locomotive_mass_kg!=200000 || profile.wagon_mass_kg!=80000 ||
            profile.coupler_profile!="sh2v-pair-published-2023" || profile.wagon_brakes==null || profile.locomotive_bogie_brakes==null)
            throw new InvalidOperationException("Unsupported scientific mechanics profile");
        PositiveFinite(profile.locomotive_reference_gamma,"locomotive_reference_gamma");
        PositiveFinite(profile.wagon_reference_gamma,"wagon_reference_gamma");
        if(profile.locomotive_reference_gamma<.20 || profile.locomotive_reference_gamma>.30 ||
            profile.wagon_reference_gamma<.03 || profile.wagon_reference_gamma>.04)
            throw new InvalidOperationException("Rotating inertia is outside the documented reference scenario");
        profile.wagon_brakes.CreateRigging();profile.locomotive_bogie_brakes.CreateRigging();
        if(profile.coupler_integrity==null)throw new InvalidOperationException("Missing explicit SA-3 integrity profile");
        profile.coupler_integrity.Validate();
        if(profile.wagon_brakes.ParsedShoeType!=BrakeShoeType.Composite || profile.wagon_brakes.cylinders!=2 ||
            profile.wagon_brakes.shoes!=8 || profile.locomotive_bogie_brakes.cylinders!=4 ||
            profile.locomotive_bogie_brakes.shoes!=8)
            throw new InvalidOperationException("Scientific reference equipment count/material mismatch");
        return profile;
    }
    public VehicleDynamicsNode[] CreateVehicles() {
        var vehicles=LongitudinalTrainDynamicsSolver.CreateFreightConsist(wagon_count);
        var loco=RotatingInertiaProfile.FromReferenceMass(locomotive_mass_kg,locomotive_reference_gamma);
        var wagon=RotatingInertiaProfile.FromReferenceMass(wagon_mass_kg,wagon_reference_gamma);
        for(int i=0;i<vehicles.Length;i++) {
            if(vehicles[i].MassKg!=(i==0?locomotive_mass_kg:wagon_mass_kg))throw new InvalidOperationException("Physical mass mismatch");
            vehicles[i].RotaryInertiaGamma=(i==0?loco:wagon).Gamma(vehicles[i].MassKg);
            vehicles[i].ShoeType=i==0?locomotive_bogie_brakes.ParsedShoeType:wagon_brakes.ParsedShoeType;
        }
        return vehicles;
    }
    public FrictionDraftGearModel CreateCouplers() { return FrictionDraftGearModel.CreateScientificSh2VPairProfile(); }
    static void PositiveFinite(double value,string name) {if(Double.IsNaN(value)||Double.IsInfinity(value)||value<=0)throw new ArgumentOutOfRangeException(name);}
}

internal sealed class ScientificCouplerIntegrity {
    public bool enabled {get;set;}
    public string id {get;set;}
    public double design_tension_N {get;set;}
    public double design_compression_N {get;set;}
    public double nominal_tensile_fracture_N {get;set;}
    public string fracture_basis {get;set;}
    public void Validate() {
        if(id!="sa3-freight-assumed-integrity-v1"||fracture_basis!="explicit_engineering_scenario_not_normative_ultimate")throw new InvalidOperationException("Unidentified SA-3 fracture scenario");
        foreach(double x in new[]{design_tension_N,design_compression_N,nominal_tensile_fracture_N})
            if(Double.IsNaN(x)||Double.IsInfinity(x)||x<=0)throw new InvalidOperationException("Invalid SA-3 load limit");
        if(design_tension_N!=2000000||design_compression_N!=2500000||nominal_tensile_fracture_N<design_tension_N)
            throw new InvalidOperationException("SA-3 design/fracture limits confused");
    }
}

internal sealed class ScientificBrakeEquipment {
    public ScientificBrakeEquipment() {}
    public string id {get;set;}
    public string material {get;set;}
    public double cylinder_diameter_m {get;set;}
    public double ratio_total_per_cylinder {get;set;}
    public double cylinder_efficiency {get;set;}
    public double rigging_efficiency {get;set;}
    public double return_spring_force_per_cylinder_N {get;set;}
    public double slack_adjuster_force_per_cylinder_N {get;set;}
    public int cylinders {get;set;}
    public int shoes {get;set;}
    public BrakeShoeType ParsedShoeType {
        get {
            if(material=="composite")return BrakeShoeType.Composite;
            if(material=="standard-cast-iron")return BrakeShoeType.StandardCastIron;
            if(material=="phosphorus-cast-iron")return BrakeShoeType.PhosphorusCastIronP10;
            throw new InvalidOperationException("Unsupported brake material "+material);
        }
    }
    public BrakeRigging CreateRigging() {
        var ignored=ParsedShoeType;
        return new BrakeRigging(id,cylinder_diameter_m,cylinders,shoes,ratio_total_per_cylinder,
            cylinder_efficiency,rigging_efficiency,return_spring_force_per_cylinder_N,slack_adjuster_force_per_cylinder_N);
    }
}

// Actual gauge-pressure snapshot at the outer pneumatic frame. The mechanical
// substeps recompute friction at each body's signed speed, without synthesizing BC.
internal sealed class TrainerScientificBrakeFrame {
    readonly BrakeRigging wagon,bogie;
    readonly BrakeShoeType wagonMaterial,bogieMaterial;
    readonly double[] normalPerShoe=new double[76], totalNormal=new double[76];
    readonly double[] bogieNormalPerShoe=new double[4], bogieTotalNormal=new double[4];
    double[] pressurePa=new double[76], bogiePressurePa=new double[4];
    public long Generation {get;private set;}
    public TrainerScientificBrakeFrame(TrainerScientificMechanicsProfile profile) {
        wagon=profile.wagon_brakes.CreateRigging();bogie=profile.locomotive_bogie_brakes.CreateRigging();
        wagonMaterial=profile.wagon_brakes.ParsedShoeType;bogieMaterial=profile.locomotive_bogie_brakes.ParsedShoeType;
    }
    public void SetPressures(double[] vehicleGaugePa,double[] actualBogieGaugePa) {
        if(vehicleGaugePa==null || vehicleGaugePa.Length!=76 || actualBogieGaugePa==null || actualBogieGaugePa.Length!=4)
            throw new ArgumentException("76 vehicle and four bogie pressures are required");
        var p=(double[])vehicleGaugePa.Clone();var b=(double[])actualBogieGaugePa.Clone();
        var ns=new double[76];var nt=new double[76];var bs=new double[4];var bt=new double[4];
        for(int i=0;i<76;i++)Nonnegative(p[i]);
        for(int i=0;i<4;i++) {
            Nonnegative(b[i]);var value=ScientificBrakeForces.Compute(bogie,bogieMaterial,b[i],0);
            bs[i]=value.NormalForcePerShoeN;bt[i]=value.TotalNormalForceN;
        }
        for(int i=1;i<76;i++) {
            var value=ScientificBrakeForces.Compute(wagon,wagonMaterial,p[i],0);
            ns[i]=value.NormalForcePerShoeN;nt[i]=value.TotalNormalForceN;
        }
        if(Generation==Int64.MaxValue)throw new InvalidOperationException("Scientific brake frame exhausted");
        Array.Copy(ns,normalPerShoe,76);Array.Copy(nt,totalNormal,76);
        Array.Copy(bs,bogieNormalPerShoe,4);Array.Copy(bt,bogieTotalNormal,4);
        pressurePa=p;bogiePressurePa=b;Generation++;
    }
    public double CapacityNewtons(int index,double signedSpeedMps) {
        if(index<0 || index>=76)throw new ArgumentOutOfRangeException("index");
        if(Generation==0)throw new InvalidOperationException("No current pneumatic pressure frame");
        if(index==0) {
            double sum=0;
            for(int b=0;b<4;b++)sum+=bogieTotalNormal[b]*BrakeTribologyPtr.ComputeBrakeShoeFriction(bogieMaterial,signedSpeedMps,bogieNormalPerShoe[b]);
            return sum;
        }
        return totalNormal[index]*BrakeTribologyPtr.ComputeBrakeShoeFriction(wagonMaterial,signedSpeedMps,normalPerShoe[index]);
    }
    public double[] VehiclePressuresPa {get{return (double[])pressurePa.Clone();}}
    public double[] BogiePressuresPa {get{return (double[])bogiePressurePa.Clone();}}
    public BrakeForceResult WagonResult(int index,double speed) {
        if(index<1||index>=76||Generation==0)throw new InvalidOperationException("Wagon frame unavailable");
        return ScientificBrakeForces.Compute(wagon,wagonMaterial,pressurePa[index],speed);
    }
    public BrakeForceResult BogieResult(int index,double speed) {
        if(index<0||index>=4||Generation==0)throw new InvalidOperationException("Bogie frame unavailable");
        return ScientificBrakeForces.Compute(bogie,bogieMaterial,bogiePressurePa[index],speed);
    }
    static void Nonnegative(double p){if(Double.IsNaN(p)||Double.IsInfinity(p)||p<0)throw new ArgumentOutOfRangeException("pressurePa");}
}
