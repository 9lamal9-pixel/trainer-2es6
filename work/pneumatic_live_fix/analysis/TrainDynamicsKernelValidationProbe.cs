using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Couplers;
using RailwayDynamics.Simulation;

// Standalone managed kernel checks: no host, P/Invoke or trainer DLL loading.
public static class TrainDynamicsKernelValidationProbe {
    static readonly List<object> Checks = new List<object>();
    static void Check(string name, bool passed, object detail) {
        Checks.Add(new { name = name, passed = passed, actual = detail });
        if (!passed) throw new Exception(name + ": " + detail);
    }
    static bool Near(double a, double b, double tolerance) { return Math.Abs(a - b) <= tolerance; }
    static void Reject(string name, Action action) {
        bool rejected = false; try { action(); } catch (ArgumentException) { rejected = true; }
        Check(name, rejected, rejected);
    }
    static VehicleDynamicsNode Node(double mass, double x, double speed) {
        return new VehicleDynamicsNode { MassKg = mass, LengthM = 1, PositionX = x, VelocityV = speed,
            AxleCount = 4, ShoeType = BrakeShoeType.Composite };
    }
    static LongitudinalTrainDynamicsSolver Solver(params VehicleDynamicsNode[] nodes) {
        var solver = new LongitudinalTrainDynamicsSolver(nodes, FrictionDraftGearModel.CreateDefaultFreightDraftGear());
        solver.RunningResistanceEnabled = false; return solver;
    }

    static void GeometryAndInputs() {
        var nodes = LongitudinalTrainDynamicsSolver.CreateFreightConsist();
        var solver = Solver(nodes); solver.SetInitialState(1000, 0);
        Check("76 real bodies and 6200t", solver.VehicleCount == 76 && SumMass(solver) == 6200000, SumMass(solver));
        Check("Neutral total coupler-axis length1126.18m", Near(solver.TotalLengthM, 1126.18, 1e-9), solver.TotalLengthM);
        Check("Head front maps to centre and back", Near(solver.GetVehicle(0).PositionX, 984.035, 1e-9) && Near(solver.GetVehicleFrontPosition(0), 1000, 1e-9), solver.GetVehicle(0).PositionX);
        Check("Wagon1 front is behind whole31.93m locomotive", Near(solver.GetVehicleFrontPosition(1), 968.07, 1e-9), solver.GetVehicleFrontPosition(1));
        double tailRear = solver.GetVehicle(75).PositionX - solver.GetVehicle(75).LengthM / 2;
        Check("Tail rear matches full geometry", Near(tailRear, 1000 - 1126.18, 1e-8), tailRear);
        nodes[0].MassKg = 1;
        Check("Constructor owns a defensive node copy", solver.GetVehicle(0).MassKg == 200000, solver.GetVehicle(0).MassKg);
        solver.SetBrakeCylinderPressure(0, 123456); solver.SetBrakeCylinderPressure(75, 234567); solver.Step(.02);
        Check("Production Step preserves measured BC exactly", solver.GetVehicle(0).BrakeCylinderPressurePa == 123456 && solver.GetVehicle(75).BrakeCylinderPressurePa == 234567, solver.GetVehicle(75).BrakeCylinderPressurePa);
        Check("One BC input cannot inflate neighbours", solver.GetVehicle(1).BrakeCylinderPressurePa == 0, solver.GetVehicle(1).BrakeCylinderPressurePa);
        Check("Neutral geometry stores no fictitious preload", solver.TotalEnergyJ < 1e-8, solver.TotalEnergyJ);
        Check("Adaptive stiffness/mass substeps are used", solver.LastSubstepCount > 1, solver.LastSubstepCount);
        Check("Last rear coupler is a free boundary", solver.GetCouplerForceSignedNewtons(75) == 0, solver.GetCouplerForceSignedNewtons(75));
        Reject("Negative pressure rejected", delegate { solver.SetBrakeCylinderPressure(0, -1); });
        Reject("NaN force rejected", delegate { solver.SetVehicleForce(0, double.NaN); });
        Reject("Negative brake capacity rejected", delegate { solver.SetVehicleBrakeForceMagnitude(0, -1); });
        Reject("Negative curve resistance rejected", delegate { solver.SetRouteForces(0, 0, -1); });
        Reject("Invalid dt rejected", delegate { solver.Step(double.NaN); });
        Check("Rejected inputs did not mutate BC", solver.GetVehicle(0).BrakeCylinderPressurePa == 123456, solver.GetVehicle(0).BrakeCylinderPressurePa);
    }
    static double SumMass(LongitudinalTrainDynamicsSolver solver) { double sum = 0; for (int i = 0; i < solver.VehicleCount; i++) sum += solver.GetVehicle(i).MassKg; return sum; }

    static void PassiveStops() {
        foreach (double direction in new[] { -1.0, 1.0 }) {
            var s = Solver(Node(1000, 0, direction * .001)); s.SetVehicleBrakeForceMagnitude(0, 1e6);
            double oldEnergy = s.TotalEnergyJ, x0 = s.GetVehicle(0).PositionX;
            for (int i = 0; i < 8; i++) { s.Step(.2); Check("Brake never reverses direction" + direction + "/" + i, s.GetVehicle(0).VelocityV * direction >= -1e-14, s.GetVehicle(0).VelocityV); Check("Brake never creates kinetic energy" + direction + "/" + i, s.TotalEnergyJ <= oldEnergy + 1e-10, s.TotalEnergyJ); oldEnergy = s.TotalEnergyJ; }
            Check("Both-direction exact rest without creep" + direction, s.GetVehicle(0).VelocityV == 0 && Math.Abs(s.GetVehicle(0).PositionX - x0) < 1e-10, s.GetVehicle(0).PositionX);
            s.SetVehicleForce(0, direction * 100); s.Step(.1);
            Check("Friction brake holds against signed small drive" + direction, s.GetVehicle(0).VelocityV == 0, s.GetVehicle(0).BrakeForceN);
            var rolling = Solver(Node(80000, 0, direction * 1e-7)); rolling.RunningResistanceEnabled = true;
            rolling.Step(1); rolling.Step(1);
            Check("Rolling resistance stops without reverse" + direction, rolling.GetVehicle(0).VelocityV == 0, rolling.GetVehicle(0).VelocityV);
        }
        var threshold = Solver(Node(1000, 0, 0)); threshold.SetVehicleBrakeForceMagnitude(0, 100); threshold.SetVehicleForce(0, 110); threshold.Step(.1);
        Check("Drive above static capacity starts normally", Near(threshold.GetVehicle(0).VelocityV, .001, 1e-10), threshold.GetVehicle(0).VelocityV);
        var dynamic = Solver(Node(1000, 0, 0)); dynamic.SetVehicleDynamicBrakeForceMagnitude(0, 10000); dynamic.SetVehicleForce(0, 100); dynamic.Step(.1);
        Check("EDT is not a static holding brake", dynamic.GetVehicle(0).VelocityV > 0, dynamic.GetVehicle(0).VelocityV);
        Check("EDT plus external work is passive", dynamic.TotalEnergyJ <= dynamic.CumulativeExternalWorkJ + 1e-9, dynamic.TotalEnergyJ);
        var slope = Solver(Node(1000, 0, 0)); slope.SetRouteForces(0, -100, 50); slope.Step(.1);
        Check("Signed grade drives reverse; curve opposes it", Near(slope.GetVehicle(0).VelocityV, -.005, 1e-10) && slope.GetVehicle(0).CurveResistanceN < 0, slope.GetVehicle(0).VelocityV);
    }

    static void BrakeInputsAndInertia() {
        var n = Node(80000, 0, 2); n.CylinderPistonAreaM2 = .035; n.ReturnSpringForceN = 1500;
        n.RiggingRatioEta = 6.5; n.BrakeShoeCount = 16;
        var s = Solver(n); s.SetBrakeCylinderPressure(0, 40000); s.Step(.1);
        Check("Spring force determines dead pressure, not hardcoded0.4bar", s.GetVehicle(0).BrakeForceN == 0 && s.GetVehicle(0).VelocityV == 2, s.GetVehicle(0).BrakeForceN);
        s.SetBrakeCylinderPressure(0, 350000); s.Step(.1);
        Check("Measured BC produces finite opposing friction", s.GetVehicle(0).BrakeForceN > 0 && s.GetVehicle(0).VelocityV < 2, s.GetVehicle(0).BrakeForceN);
        double v = s.GetVehicle(0).VelocityV;
        s.SetVehicleBrakeForceMagnitude(0, 0); s.Step(.1);
        Check("External validated force has priority over pressure-law fallback", Near(s.GetVehicle(0).VelocityV, v, 1e-12) && s.GetVehicle(0).BrakeCylinderPressurePa == 350000, s.GetVehicle(0).VelocityV);
        s.ClearVehicleBrakeForceMagnitude(0); s.Step(.1);
        Check("Explicit clear restores pressure-derived fallback", s.GetVehicle(0).VelocityV < v, s.GetVehicle(0).VelocityV);
        var a = Solver(Node(1000, 0, 0)); var inertiaNode = Node(1000, 0, 0); inertiaNode.RotaryInertiaGamma = .5; var b = Solver(inertiaNode);
        a.SetVehicleForce(0, 1500); b.SetVehicleForce(0, 1500); a.Step(.1); b.Step(.1);
        Check("Explicit rotary profile scales effective acceleration", Near(a.GetVehicle(0).VelocityV, .15, 1e-10) && Near(b.GetVehicle(0).VelocityV, .10, 1e-10), b.GetVehicle(0).VelocityV);
        Check("External work bounds mechanical energy", a.TotalEnergyJ <= a.CumulativeExternalWorkJ + 1e-9 && b.TotalEnergyJ <= b.CumulativeExternalWorkJ + 1e-9, a.TotalEnergyJ);
        int evaluations = 0;
        var provider = Solver(Node(1000, 0, -1));
        provider.SetBrakeForceProvider(delegate(int index, double signedV) { evaluations++; return 1000 * Math.Abs(signedV); });
        provider.Step(1);
        Check("Speed-dependent callback is sampled inside substeps", evaluations > provider.LastSubstepCount, evaluations);
        Check("Pure velocity brake follows passive exponential decay both signs", provider.GetVehicle(0).VelocityV < -.367 && provider.GetVehicle(0).VelocityV > -.370, provider.GetVehicle(0).VelocityV);
        double centreSeen = double.NaN;
        var route = Solver(Node(1000, 5, 0));
        route.SetRouteForceProviders(delegate(int index, double centre) { centreSeen = centre; return 100; }, delegate(int index, double centre) { return 20; });
        route.Step(1);
        Check("Route provider uses centre-coordinate endpoint", Near(centreSeen, route.GetVehicle(0).PositionX, 1e-10) && Near(route.GetVehicle(0).VelocityV, .08, 1e-9), centreSeen);
        double savedTime = route.SimulationTimeSeconds, savedX = route.GetVehicle(0).PositionX;
        route.SetBrakeForceProvider(delegate(int index, double signedV) { return double.NaN; });
        Reject("Nonfinite force provider rejected", delegate { route.Step(.1); });
        Check("Provider failure rolls back whole outer step", route.SimulationTimeSeconds == savedTime && route.GetVehicle(0).PositionX == savedX, route.SimulationTimeSeconds);
    }

    static void CouplerPassivityAndFullConsist() {
        var s = Solver(Node(1000, 0, .1), Node(2000, -1, -.1));
        double momentum = s.EffectiveMomentumKgMps, energy = s.TotalEnergyJ;
        for (int i = 0; i < 10; i++) {
            s.Step(.02);
            Check("Coupler dissipates or stores existing energy" + i, s.TotalEnergyJ <= energy + 1e-6, s.TotalEnergyJ);
            energy = s.TotalEnergyJ;
        }
        Check("Internal action/reaction preserves generalized momentum", Math.Abs(s.EffectiveMomentumKgMps - momentum) < 1e-4, s.EffectiveMomentumKgMps - momentum);
        Check("Tension acts rearward on front vehicle", s.GetCouplerForceSignedNewtons(0) <= 0, s.GetCouplerForceSignedNewtons(0));
        var compressed = Solver(Node(80000, 0, 0), Node(80000, -.85, 0));
        double preloaded = compressed.TotalEnergyJ; compressed.Step(.00001);
        Check("Compression initially pushes front vehicle in+X", compressed.GetCouplerForceSignedNewtons(0) >= 0 && compressed.GetVehicle(0).VelocityV > 0 && compressed.GetVehicle(1).VelocityV < 0, compressed.GetCouplerForceSignedNewtons(0));
        compressed.Step(.05);
        Check("Initial rigid-stop preload is accounted, not created", preloaded > 0 && compressed.TotalEnergyJ <= preloaded + 1e-4, compressed.TotalEnergyJ);
        var full = Solver(LongitudinalTrainDynamicsSolver.CreateFreightConsist()); full.SetInitialState(50000, 0); full.SetVehicleForce(0, 400000);
        for (int i = 0; i < 10; i++) full.Step(.1);
        Check("76-body genuine mechanical force starts head", full.GetVehicle(0).VelocityV > 0 && full.GetVehicleFrontPosition(0) > 50000, full.GetVehicle(0).VelocityV);
        Check("76-body work/energy inequality", full.TotalEnergyJ <= full.CumulativeExternalWorkJ + .01, full.TotalEnergyJ);
        full.SetInitialState(50000, -1); full.SetVehicleForce(0, 0);
        for (int i = 0; i < full.VehicleCount; i++) full.SetVehicleBrakeForceMagnitude(i, full.GetVehicle(i).MassKg * 2);
        for (int i = 0; i < 10; i++) full.Step(.1);
        bool allStopped = true; for (int i = 0; i < full.VehicleCount; i++) allStopped &= Math.Abs(full.GetVehicle(i).VelocityV) < 1e-9;
        Check("76-body reverse braking stops every body without inversion", allStopped, full.GetVehicle(75).VelocityV);
    }

    static void LegacyBoundary() {
        var first = Node(80000, 0, 0); first.LengthM = 100; first.MaxCylinderPressurePa = 380000; first.FillingTimeConstantSec = 1;
        var second = first; second.PositionX = -100;
        var s = new LongitudinalTrainDynamicsSolver(new[] { first, second }, FrictionDraftGearModel.CreateDefaultFreightDraftGear(), 10); s.RunningResistanceEnabled = false;
#pragma warning disable 618
        s.Step(1, 150000);
#pragma warning restore 618
        Check("Legacy fixture advertises its non-production status", s.LegacyPneumaticFixtureUsed, s.LegacyPneumaticFixtureUsed);
        Check("Legacy head reduction has a real transport delay", s.GetVehicle(0).BrakeCylinderPressurePa > 0 && s.GetVehicle(1).BrakeCylinderPressurePa == 0, s.GetVehicle(1).BrakeCylinderPressurePa);
        s.SetBrakeCylinderPressure(1, 12345); s.Step(.1);
        Check("Returning to production Step never evolves BC", s.GetVehicle(1).BrakeCylinderPressurePa == 12345, s.GetVehicle(1).BrakeCylinderPressurePa);
    }

    public static int Main() {
        bool passed = false; string error = null;
        try { GeometryAndInputs(); PassiveStops(); BrakeInputsAndInertia(); CouplerPassivityAndFullConsist(); LegacyBoundary(); passed = true; }
        catch (Exception e) { error = e.ToString(); Console.WriteLine(error); }
        var report = new { passed = passed, checks = Checks, checks_count = Checks.Count, error = error,
            native_loaded = false, factory_calibration_accepted = false,
            scope = "Managed mechanics and measured-pressure boundary only. Geometry76/6200t; work/energy, reverse stops, static friction, distinct EDT, coupling, adaptive integration, input ownership. Pressure/hardware example is a fixture; production native brake law and pressure propagation require separate native integration acceptance." };
        string dir = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR"); if (String.IsNullOrEmpty(dir)) dir = AppDomain.CurrentDomain.BaseDirectory;
        Directory.CreateDirectory(dir); File.WriteAllText(Path.Combine(dir, "Trainer_train_dynamics_kernel_validation.json"), new JavaScriptSerializer { MaxJsonLength = 16000000 }.Serialize(report));
        Console.WriteLine((passed ? "PASS" : "FAIL") + " managed train kernel: " + Checks.Count + " checks");
        return passed ? 0 : 1;
    }
}
