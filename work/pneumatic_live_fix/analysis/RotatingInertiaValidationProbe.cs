using System;
using System.Collections.Generic;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Inertia;
using RailwayDynamics.Simulation;
using RailwayDynamics.Couplers;

/// <summary>Pure managed analytic checks. No DLL loads, host, pressure fixture or P/Invoke.</summary>
public static class RotatingInertiaValidationProbe {
    static readonly List<object> checks = new List<object>();
    static int assertions;
    static void Check(string name, bool passed, object expected, object actual) {
        assertions++; checks.Add(new { name, passed, expected, actual });
        if (!passed) throw new Exception(name + ": expected " + expected + ", actual " + actual);
    }
    static void Near(string name, double actual, double expected, double tolerance) {
        Check(name, Math.Abs(actual - expected) <= tolerance, expected, actual);
    }
    static void Reject<T>(string name, Action action) where T : Exception {
        bool rejected = false; try { action(); } catch (T) { rejected = true; }
        Check(name, rejected, typeof(T).Name, rejected);
    }
    static VehicleDynamicsNode Body(double mass, double gamma) {
        return new VehicleDynamicsNode { MassKg = mass, RotaryInertiaGamma = gamma, LengthM = 10, AxleCount = 4 };
    }
    static LongitudinalTrainDynamicsSolver Kernel(VehicleDynamicsNode[] nodes) {
        var kernel = new LongitudinalTrainDynamicsSolver(nodes, FrictionDraftGearModel.CreateDefaultFreightDraftGear());
        kernel.RunningResistanceEnabled = false; kernel.SetInitialState(1000, 0);
        kernel.SetBrakeForceProvider(delegate(int index, double velocity) { return 0; });
        return kernel;
    }
    public static int Main() {
        bool passed = false; string error = null;
        try {
            var wheel = RotatingComponent.Wheel(4, .5, 1);
            var rotor = RotatingComponent.Motor(2, 3, .5, 1);
            var parts = new[] { wheel, rotor }; var measured = RotatingInertiaProfile.FromComponents(parts);
            Near("Rolling wheel: J/r2 is16kg", wheel.EquivalentRotatingMassKg, 16, 1e-12);
            Near("Geared rotor: J*(g/r)2 is72kg", rotor.EquivalentRotatingMassKg, 72, 1e-12);
            Near("Independent components add88kg", measured.EquivalentRotatingMassKg, 88, 1e-12);
            Near("Effective mass includes physical mass once", measured.EffectiveMassKg(10), 98, 1e-12);
            Near("Gamma is additional/physical, not multiplier", measured.Gamma(10), 8.8, 1e-12);
            Near("Energy equals .5Jwheel*(v/r)2+.5Jrotor*(gv/r)2", measured.RotationalKineticEnergyJ(2), .5*4*16 + .5*2*144, 1e-12);
            Near("Total energy additionally counts translation", measured.TotalKineticEnergyJ(10, 2), 196, 1e-12);
            Near("Reverse velocity has same rotational energy", measured.RotationalKineticEnergyJ(-2), 176, 1e-12);
            Near("Rest energy is zero without dividing by speed", measured.RotationalKineticEnergyJ(0), 0, 0);
            Near("Doubling gear multiplies reflected rotor inertia by4", RotatingComponent.Motor(2, 6, .5, 1).EquivalentRotatingMassKg / rotor.EquivalentRotatingMassKg, 4, 1e-12);
            Near("Doubling wheel radius divides reflected inertia by4", RotatingComponent.Wheel(4, 1, 1).EquivalentRotatingMassKg, 4, 1e-12);
            Near("Eight permanently coupled rotors add eight times", RotatingComponent.Motor(2, 3, .5, 8).EquivalentRotatingMassKg, 576, 1e-12);
            parts[0] = RotatingComponent.Wheel(400, .5, 1);
            var returned = measured.Components; returned[1] = wheel;
            Near("Input and returned array changes cannot change immutable profile", measured.EquivalentRotatingMassKg, 88, 0);
            Check("Physical-component origin is distinct", !measured.IsReferenceEstimate && measured.Components.Length == 2, false, measured.IsReferenceEstimate);
            var loco = RotatingInertiaProfile.FromReferenceMass(200000, .25);
            var wagon = RotatingInertiaProfile.FromReferenceMass(80000, .035);
            Near("Reference estimate creates fixed rotary mass, not invented J", RotatingInertiaProfile.ReferenceScaledRotaryMass(80000, .035), 2800, 1e-10);
            Check("Reference profile honestly contains no invented components", wagon.IsReferenceEstimate && wagon.Components.Length == 0 && wagon.ReferenceGrossMassKg == 80000 && wagon.ReferenceGamma == .035, true, wagon.IsReferenceEstimate);
            Near("Changing payload retains rotary mass and changes gamma", wagon.Gamma(40000), .07, 1e-12);
            Near("Payload mass plus fixed rotating equivalent", wagon.EffectiveMassKg(40000), 42800, 1e-9);
            Near("Separate loco+wagon effective train mass", loco.EffectiveMassKg(200000) + 75 * wagon.EffectiveMassKg(80000), 6460000, 1e-7);
            Near("Separate loco+wagon rotary mass", loco.EquivalentRotatingMassKg + 75 * wagon.EquivalentRotatingMassKg, 260000, 1e-7);
            Check("Estimated coefficients do not manufacture wheelset/rotor data", loco.Components.Length == 0 && wagon.Components.Length == 0, 0, loco.Components.Length);
            Near("Known2ES6g/r kinematics, without claiming motor J", RotatingComponent.Motor(1, 3.44, .625, 1).EquivalentRotatingMassKg, 30.294016, 1e-12);
            var tiny = RotatingInertiaProfile.FromComponents(new RotatingComponent(Double.Epsilon, 1, 1));
            Near("Tiny representable inertia survives energy scaling", tiny.RotationalKineticEnergyJ(2), 2*Double.Epsilon, 0);
            var noParts = RotatingInertiaProfile.FromComponents();
            Near("Empty components give physical mass only", noParts.EffectiveMassKg(80000), 80000, 0);
            Near("Zero count is a disabled component", new RotatingComponent(4, 2, 0).EquivalentRotatingMassKg, 0, 0);
            Near("Huge finite ratio with compensating inertia need not overflow", new RotatingComponent(1e-300, 1e300, 1).EquivalentRotatingMassKg / 1e300, 1, 1e-14);

            Reject<ArgumentOutOfRangeException>("NegativeJ rejected", delegate { new RotatingComponent(-1, 2, 1); });
            Reject<ArgumentOutOfRangeException>("NonfiniteJ rejected", delegate { new RotatingComponent(Double.NaN, 2, 1); });
            Reject<ArgumentOutOfRangeException>("Infinite ratio rejected", delegate { new RotatingComponent(1, Double.PositiveInfinity, 1); });
            Reject<ArgumentOutOfRangeException>("Negative ratio magnitude rejected", delegate { new RotatingComponent(1, -1, 1); });
            Reject<ArgumentOutOfRangeException>("Negative count rejected", delegate { new RotatingComponent(1, 1, -1); });
            Reject<ArgumentOutOfRangeException>("Zero radius rejected", delegate { RotatingComponent.Wheel(1, 0, 1); });
            Reject<ArgumentOutOfRangeException>("Negative radius rejected", delegate { RotatingComponent.Wheel(1, -1, 1); });
            Reject<ArgumentOutOfRangeException>("Zero gear ratio rejected", delegate { RotatingComponent.Motor(1, 0, .5, 1); });
            Reject<ArgumentOutOfRangeException>("Nonfinite gear rejected", delegate { RotatingComponent.Motor(1, Double.PositiveInfinity, .5, 1); });
            Reject<ArgumentNullException>("Null component array rejected", delegate { RotatingInertiaProfile.FromComponents((RotatingComponent[])null); });
            Reject<ArgumentException>("Null component element rejected", delegate { RotatingInertiaProfile.FromComponents(new RotatingComponent[] { null }); });
            Reject<ArgumentOutOfRangeException>("Zero physical mass rejected", delegate { wagon.Gamma(0); });
            Reject<ArgumentOutOfRangeException>("Negative reference gamma rejected", delegate { RotatingInertiaProfile.ReferenceScaledRotaryMass(80000, -.1); });
            Reject<ArgumentOutOfRangeException>("Nonfinite speed rejected", delegate { wagon.RotationalKineticEnergyJ(Double.NaN); });
            Reject<OverflowException>("Rotating mass product overflow rejected", delegate { new RotatingComponent(Double.MaxValue, 2, 1); });
            Reject<OverflowException>("Rotating mass sum overflow rejected", delegate { RotatingInertiaProfile.FromComponents(new RotatingComponent(Double.MaxValue, 1, 1), new RotatingComponent(Double.MaxValue, 1, 1)); });
            Reject<OverflowException>("Reference mass product overflow rejected", delegate { RotatingInertiaProfile.ReferenceScaledRotaryMass(Double.MaxValue, 2); });
            Reject<OverflowException>("Kinematic quotient overflow rejected", delegate { RotatingComponent.Motor(Double.MaxValue, Double.MaxValue, Double.Epsilon, 1); });
            Reject<OverflowException>("Effective mass sum overflow rejected", delegate { RotatingInertiaProfile.FromReferenceMass(Double.MaxValue, 1).EffectiveMassKg(Double.MaxValue); });
            Reject<OverflowException>("Energy overflow rejected", delegate { wagon.RotationalKineticEnergyJ(Double.MaxValue); });

            var single = Kernel(new[] { Body(200000, loco.Gamma(200000)) });
            single.SetVehicleForce(0, 100000); single.Step(.2);
            Near("Kernel gamma=.25 gives analytic a=.4", single.GetVehicle(0).AccelerationA, .4, 1e-10);
            Near("Kernel speed integrates analytic effective mass", single.GetVehicle(0).VelocityV, .08, 1e-10);
            Near("Kernel kinetic energy uses same effective mass", single.TotalEnergyJ, loco.TotalKineticEnergyJ(200000, .08), 1e-8);
            var reverse = Kernel(new[] { Body(80000, wagon.Gamma(80000)) });
            reverse.SetVehicleForce(0, -80000); reverse.Step(.1);
            Near("Kernel reverse force preserves inertia symmetry", reverse.GetVehicle(0).VelocityV, -.1/1.035, 1e-10);
            var grade = Kernel(new[] { Body(200000, loco.Gamma(200000)) });
            double physicalGradeForce = -200000 * 9.80665 * .01;
            grade.SetRouteForces(0, physicalGradeForce, 0); grade.Step(.1);
            Near("Gravity uses physical mass; inertia uses effective mass", grade.GetVehicle(0).AccelerationA, physicalGradeForce / 250000, 1e-10);
            Check("Gamma does not increase actual weight", Math.Abs(grade.GetVehicle(0).AccelerationA + 9.80665*.01) > .01, "different from pseudo-weight acceleration", grade.GetVehicle(0).AccelerationA);

            var nodes = LongitudinalTrainDynamicsSolver.CreateFreightConsist();
            nodes[0].RotaryInertiaGamma = loco.Gamma(nodes[0].MassKg);
            for (int i = 1; i < nodes.Length; i++) nodes[i].RotaryInertiaGamma = wagon.Gamma(nodes[i].MassKg);
            var consist = Kernel(nodes); double staticMass = 0, effectiveMass = 0;
            for (int i = 0; i < nodes.Length; i++) { staticMass += consist.GetVehicle(i).MassKg; effectiveMass += nodes[i].MassKg * (1 + nodes[i].RotaryInertiaGamma); }
            Near("Kernel76 retains physical6200t", staticMass, 6200000, 0);
            Near("Kernel76 profile effective6460t", effectiveMass, 6460000, 1e-7);
            consist.SetInitialState(2000, 2);
            Near("Kernel76 moving energy matches profile", consist.TotalEnergyJ, .5*6460000*4, .01);
            Near("Kernel76 generalized momentum includes rotor equivalence", consist.EffectiveMomentumKgMps, 6460000*2, 1e-6);
            nodes[0].RotaryInertiaGamma = 100; // input mutation cannot change copied state
            Near("Kernel input mutation cannot silently alter effective mass", consist.GetVehicle(0).RotaryInertiaGamma, .25, 0);
            passed = true;
        } catch (Exception e) { error = e.ToString(); Console.WriteLine(error); }
        string resultDirectory=Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR"); if(String.IsNullOrEmpty(resultDirectory))resultDirectory=AppDomain.CurrentDomain.BaseDirectory; Directory.CreateDirectory(resultDirectory); string path=Path.Combine(resultDirectory,"Trainer_rotating_inertia_validation.json");
        File.WriteAllText(path, new JavaScriptSerializer { MaxJsonLength = 4000000 }.Serialize(new {
            passed, assertions, checks, error,
            scope = "Pure managed analytic rotational energy/mass and existing LTD integration; no native execution. Reference gamma=.25/.035 is a literature-based loaded80t scenario, not OEM identification. No brake-force calibration is accepted here."
        }));
        Console.WriteLine((passed ? "PASS " : "FAIL ") + assertions + " rotating inertia assertions");
        return passed ? 0 : 1;
    }
}
