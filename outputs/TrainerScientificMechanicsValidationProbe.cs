using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Web.Script.Serialization;
using RailwayDynamics.Simulation;
using RailwayDynamics.Couplers;

// Constant-pressure mechanics benchmark. No host, native module, DLL call,
// pneumatic wave, service-brake timing or installed-equipment identification.
public static class TrainerScientificMechanicsValidationProbe {
    const double GaugePa = 300000, InitialSpeed = 80.0 / 3.6;
    const double StaticMass = 6200000, EffectiveMass = 6460000;
    static readonly List<object> checks = new List<object>();
    static readonly List<object> forceTable = new List<object>();
    static readonly List<object> stopTable = new List<object>();
    static string profilePath, profileHash;
    static object quadratureEvidence, sensitivityEvidence;

    sealed class StopResult {
        public double mass_effective_kg, step_s, initial_speed_m_s, distance_m;
        public double grid_stop_time_s, interpolated_stop_time_s, initial_energy_J, final_energy_J;
        public double independent_brake_work_J, backward_euler_loss_J, energy_residual_J;
        public double reported_dissipation_J, external_work_J;
        public double solver_residual_energy_bound_J, floating_roundoff_bound_J, energy_acceptance_bound_J;
        public double energy_relative_acceptance_bound, independent_momentum_energy_residual_J;
        public double maximum_normalized_velocity_residual, maximum_velocity_residual_bound_ratio;
        public bool independent_force_agreement;
        public int maximum_accepted_substeps_per_outer_step;
        public double reference_distance_m, reference_time_s, distance_error_m, time_error_s;
        public bool no_velocity_reversal, energy_nonincreasing;
        public int steps;
    }
    // IEEE754 binary64 unit roundoff; Double.Epsilon is a subnormal, not this value.
    const double UnitRoundoff = 1.1102230246251565e-16;
    static double GammaRoundoff(int operations) {
        double nu = operations * UnitRoundoff; return nu / (1 - nu);
    }
    static bool Finite(double value) {
        return (unchecked((ulong)BitConverter.DoubleToInt64Bits(value)) & 0x7ff0000000000000UL) != 0x7ff0000000000000UL;
    }
    static void Check(string id, bool passed, object detail) {
        checks.Add(new { id, passed, detail });
        if (!passed) throw new InvalidOperationException("Scientific mechanics assertion: " + id);
    }
    static void Near(string id, double actual, double expected, double tolerance) {
        Check(id, Finite(actual) && Finite(expected) && Math.Abs(actual - expected) <= tolerance, new { actual, expected, tolerance });
    }
    static void Reject<T>(string id, Action action) where T : Exception {
        bool rejected = false; try { action(); } catch (T) { rejected = true; }
        Check(id, rejected, new { expected = typeof(T).Name, rejected });
    }
    static double[] Pressures(int count, double pressure) {
        var result = new double[count]; for (int i = 0; i < count; i++) result[i] = pressure; return result;
    }
    static void SetUniform(TrainerScientificBrakeFrame frame, double pressure) {
        frame.SetPressures(Pressures(76, pressure), Pressures(4, pressure));
    }
    static double FrameTotal(TrainerScientificBrakeFrame frame, double velocity) {
        double sum = 0; for (int i = 0; i < 76; i++) sum += frame.CapacityNewtons(i, velocity); return sum;
    }

    // Independent SI reference: literal source scenario parameters. Neither
    // ScientificBrakeForces nor BrakeTribologyPtr is called in these equations.
    static double ReferenceWagon(double pressurePa, double signedSpeedMps) {
        double pistonArea = Math.PI * .254 * .254 / 4;
        double rod = Math.Max(0, .98 * pressurePa * pistonArea - 1032.5 - 512.77);
        double totalNormal = rod * 5.70 * .95 * 2;
        double shoeKn = totalNormal / 8 / 1000;
        double vKmh = Math.Abs(signedSpeedMps) * 3.6;
        double mu = .44 * (.1 * shoeKn + 20) / (.4 * shoeKn + 20) * (vKmh + 150) / (2 * vKmh + 150);
        return totalNormal * mu;
    }
    static double ReferenceBogie(double pressurePa, double signedSpeedMps) {
        double pistonArea = Math.PI * .203 * .203 / 4;
        double rod = Math.Max(0, .98 * pressurePa * pistonArea - 1200);
        double totalNormal = rod * 8.8 * .95 * 4;
        double shoeKn = totalNormal / 8 / 1000;
        double vKmh = Math.Abs(signedSpeedMps) * 3.6;
        double mu = .50 * (1.6 * shoeKn + 100) / (5.2 * shoeKn + 100) * (vKmh + 100) / (5 * vKmh + 100); // Typical locomotive type-M source curve, BelGUT2017.
        return totalNormal * mu;
    }
    static double ReferenceTotal(double speedMps) {
        return 75 * ReferenceWagon(GaugePa, speedMps) + 4 * ReferenceBogie(GaugePa, speedMps);
    }
    static double Simpson(double mass, double startSpeed, int intervals, bool distance) {
        if (intervals < 2 || intervals % 2 != 0) throw new ArgumentOutOfRangeException("intervals");
        double max = Math.Abs(startSpeed), h = max / intervals, sum = 0;
        for (int i = 0; i <= intervals; i++) {
            double v = h * i, force = ReferenceTotal(v);
            if (!Finite(force) || force <= 0) throw new InvalidOperationException("Independent brake integral is undefined.");
            double term = mass * (distance ? v : 1) / force;
            sum += (i == 0 || i == intervals ? 1 : i % 2 == 0 ? 2 : 4) * term;
        }
        return sum * h / 3;
    }

    static LongitudinalTrainDynamicsSolver RigidKernel(FrictionDraftGearModel gear, double effectiveMass, double maximumStep) {
        // One equivalent rigid body deliberately removes coupler oscillations.
        // Actual physical mass remains6200t, with only inertia in the denominator.
        var body = new VehicleDynamicsNode { MassKg = StaticMass, LengthM = 1126.18,
            RotaryInertiaGamma = effectiveMass / StaticMass - 1, AxleCount = 308 };
        var solver = new LongitudinalTrainDynamicsSolver(new[] { body }, gear);
        solver.RunningResistanceEnabled = false; solver.MaximumSubstepSeconds = maximumStep;
        return solver;
    }
    static StopResult Stop(TrainerScientificBrakeFrame frame, FrictionDraftGearModel gear, double mass, double speed, double h) {
        var solver = RigidKernel(gear, mass, h);
        solver.SetInitialState(5000, speed);
        solver.SetBrakeForceProvider(delegate(int index, double velocity) { return FrameTotal(frame, velocity); });
        double x0 = solver.GetVehicleFrontPosition(0), initialEnergy = .5 * mass * speed * speed;
        double work = 0, numericalLoss = 0, timeAtLastPositive = 0, lastPositiveSpeed = Math.Abs(speed);
        bool signSafe = true, monotonic = true, forceAgreement = true;
        double residualBound = 0, roundoffBound = 0, momentumEnergyResidual = 0;
        double maxNormalizedResidual = 0, maxResidualRatio = 0;
        int maximumAcceptedSubsteps = 0;
        int steps = 0;
        while (Math.Abs(solver.GetVehicle(0).VelocityV) > 1e-12) {
            if (steps >= 100000 || solver.SimulationTimeSeconds >= 240) throw new InvalidOperationException("Constant-pressure stop budget exceeded.");
            double before = solver.GetVehicle(0).VelocityV, oldX = solver.GetVehicleFrontPosition(0);
            double oldEnergy = solver.TotalEnergyJ;
            timeAtLastPositive = solver.SimulationTimeSeconds; lastPositiveSpeed = Math.Abs(before);
            solver.Step(h); steps++;
            maximumAcceptedSubsteps = Math.Max(maximumAcceptedSubsteps, solver.LastSubstepCount);
            // The derivation below applies to ONE accepted BE step. A retry
            // would require observing each accepted endpoint, so refuse it.
            if (solver.LastSubstepCount != 1) throw new InvalidOperationException("Energy oracle requires one accepted substep per recorded endpoint.");
            double after = solver.GetVehicle(0).VelocityV, newX = solver.GetVehicleFrontPosition(0);
            double dx = Math.Abs(newX - oldX);
            signSafe &= speed > 0 ? after >= -1e-12 : after <= 1e-12;
            monotonic &= solver.TotalEnergyJ <= oldEnergy + Math.Max(1e-6, initialEnergy * 1e-12);
            // This force is evaluated by the independent source equations,
            // not the callback under test. BE splits exact energy loss into
            // endpoint friction work plus .5*m*(delta v)^2 numerical loss.
            double referenceForce = ReferenceTotal(after), frameForce = FrameTotal(frame, after);
            // The longest positive-arithmetic dependency chain is bounded by
            //256 operations: cached rigging, friction and76-source summation.
            // This admits floating evaluation differences, not force-law drift.
            double forceRoundoff = GammaRoundoff(256) * Math.Max(Math.Abs(referenceForce), Math.Abs(frameForce));
            forceAgreement &= Math.Abs(frameForce - referenceForce) <= forceRoundoff;
            double workIncrement = referenceForce * dx;
            double lossIncrement = .5 * mass * (after - before) * (after - before);
            work += workIncrement; numericalLoss += lossIncrement;
            double speedAfter = Math.Abs(after), speedBefore = Math.Abs(before);
            if (speedAfter > 0) {
                double velocityResidual = speedBefore - speedAfter - h * referenceForce / mass;
                double solverVelocityBound = 1e-10 * Math.Max(1, speedAfter);
                double velocityRoundoff = GammaRoundoff(12) * (speedBefore + speedAfter + h * referenceForce / mass)
                    + h * forceRoundoff / mass;
                maxNormalizedResidual = Math.Max(maxNormalizedResidual, Math.Abs(velocityResidual) / Math.Max(1, speedAfter));
                maxResidualRatio = Math.Max(maxResidualRatio, Math.Abs(velocityResidual) / (solverVelocityBound + velocityRoundoff));
                // q=m(v_old-v_end)-h*B(v_end). Energy defect=q*v_end.
                // Solver accepts |q/m|<=1e-10*max(1,|v_end|), hence no h
                // factor belongs in this energy bound. At v_end=0 it is zero.
                residualBound += mass * solverVelocityBound * speedAfter;
                momentumEnergyResidual += mass * velocityResidual * speedAfter;
            }
            // Position addition, centre->front conversion and subtraction:
            // |observed dx-h*v_end| is bounded by gamma8 times coordinate scale.
            // Further terms cover energy products and independent force rounding.
            roundoffBound += GammaRoundoff(8) * referenceForce *
                (Math.Abs(oldX) + Math.Abs(newX) + 1126.18 + Math.Abs(h * after))
                + GammaRoundoff(16) * mass * (before * before + after * after)
                + h * speedAfter * forceRoundoff
                + GammaRoundoff(4) * (Math.Abs(workIncrement) + Math.Abs(lossIncrement));
        }
        double referenceDistance = Simpson(mass, speed, 4096, true), referenceTime = Simpson(mass, speed, 4096, false);
        double interpolatedTime = timeAtLastPositive + mass * lastPositiveSpeed / ReferenceTotal(0);
        // Sequential sums and final subtraction have at most steps+8 binary64
        // operations along each positive-accumulation path.
        roundoffBound += GammaRoundoff(steps + 8) * (initialEnergy + solver.TotalEnergyJ + Math.Abs(work)
            + Math.Abs(numericalLoss) + Math.Abs(solver.CumulativeDissipatedEnergyJ));
        double acceptanceBound = residualBound + roundoffBound;
        var result = new StopResult {
            mass_effective_kg = mass, step_s = h, initial_speed_m_s = speed,
            distance_m = Math.Abs(solver.GetVehicleFrontPosition(0) - x0), grid_stop_time_s = solver.SimulationTimeSeconds,
            interpolated_stop_time_s = interpolatedTime, initial_energy_J = initialEnergy, final_energy_J = solver.TotalEnergyJ,
            independent_brake_work_J = work, backward_euler_loss_J = numericalLoss,
            energy_residual_J = initialEnergy - solver.TotalEnergyJ - work - numericalLoss,
            reported_dissipation_J = solver.CumulativeDissipatedEnergyJ, external_work_J = solver.CumulativeExternalWorkJ,
            reference_distance_m = referenceDistance, reference_time_s = referenceTime,
            no_velocity_reversal = signSafe, energy_nonincreasing = monotonic, steps = steps,
            solver_residual_energy_bound_J = residualBound, floating_roundoff_bound_J = roundoffBound,
            energy_acceptance_bound_J = acceptanceBound, energy_relative_acceptance_bound = acceptanceBound / initialEnergy,
            independent_momentum_energy_residual_J = momentumEnergyResidual,
            maximum_normalized_velocity_residual = maxNormalizedResidual,
            maximum_velocity_residual_bound_ratio = maxResidualRatio, independent_force_agreement = forceAgreement,
            maximum_accepted_substeps_per_outer_step = maximumAcceptedSubsteps
        };
        result.distance_error_m = result.distance_m - referenceDistance;
        result.time_error_s = interpolatedTime - referenceTime;
        stopTable.Add(result);
        Check("stop_passive_sign_h" + h + "_m" + mass + "_v" + speed, signSafe && monotonic && result.final_energy_J < 1e-8, result);
        Check("stop_independent_force_roundoff_agreement_h" + h + "_m" + mass + "_v" + speed, forceAgreement, result);
        Check("stop_derived_velocity_residual_bound_h" + h + "_m" + mass + "_v" + speed, maxResidualRatio <= 1, result);
        Check("stop_energy_bound_is_stricter_than_one_ppm_h" + h + "_m" + mass + "_v" + speed,
            Finite(acceptanceBound) && acceptanceBound / initialEnergy < 1e-6, result);
        Near("stop_independent_energy_balance_h" + h + "_m" + mass + "_v" + speed, result.energy_residual_J, 0, acceptanceBound);
        Near("stop_momentum_predicts_energy_defect_h" + h + "_m" + mass + "_v" + speed,
            result.energy_residual_J, momentumEnergyResidual, roundoffBound);
        Near("stop_reported_dissipation_h" + h + "_m" + mass + "_v" + speed, result.reported_dissipation_J, work + numericalLoss, acceptanceBound);
        Near("stop_no_external_work_h" + h + "_m" + mass + "_v" + speed, result.external_work_J, 0, 0);
        Check("stop_bounded_distance_error_h" + h + "_m" + mass + "_v" + speed,
            Math.Abs(result.distance_error_m) <= 2 * Math.Abs(speed) * h + .001, result);
        Check("stop_bounded_time_error_h" + h + "_m" + mass + "_v" + speed,
            Math.Abs(result.time_error_s) <= 2 * h + 1e-5 && Math.Abs(result.grid_stop_time_s - referenceTime) <= 3 * h + 1e-5, result);
        Check("stop_has_no_legacy_pneumatic_fixture_h" + h + "_m" + mass, !solver.LegacyPneumaticFixtureUsed, false);
        return result;
    }

    static void ProfileAndForces(TrainerScientificMechanicsProfile profile, FrictionDraftGearModel gear) {
        var nodes = profile.CreateVehicles(); double physical = 0, effective = 0;
        for (int i = 0; i < nodes.Length; i++) {
            physical += nodes[i].MassKg; effective += nodes[i].MassKg * (1 + nodes[i].RotaryInertiaGamma);
        }
        Check("reference_profile_76vehicles", nodes.Length == 76, nodes.Length);
        Near("physical_mass_6200t", physical, StaticMass, 0);
        Near("effective_mass_6460t", effective, EffectiveMass, 1e-7);
        Near("separate_loco_gamma", nodes[0].RotaryInertiaGamma, .25, 1e-14);
        Near("separate_loaded_wagon_gamma", nodes[75].RotaryInertiaGamma, .035, 1e-14);
        Check("selected_scientific_pair_factory", gear.ProfileId == "sh2v-pair-published-2023", gear.ProfileId);
        Near("two90mm_gears_are180mm_pair", gear.MaximumWorkingStrokeM, .180, 1e-14);
        nodes[0].MassKg = 1; nodes[1].RotaryInertiaGamma = 9;
        var freshNodes = profile.CreateVehicles();
        Check("profile_vehicle_arrays_are_independent", freshNodes[0].MassKg == 200000 && Math.Abs(freshNodes[1].RotaryInertiaGamma - .035) < 1e-14,
            new { freshNodes[0].MassKg, freshNodes[1].RotaryInertiaGamma });
        var pressureFrame = new TrainerScientificBrakeFrame(profile);
        Reject<InvalidOperationException>("unpublished_pressure_frame_refused", delegate { pressureFrame.CapacityNewtons(0, 0); });
        SetUniform(pressureFrame, 0);
        double zero = FrameTotal(pressureFrame, 80 / 3.6);
        Near("zero_BC_has_zero_capacity", zero, 0, 0);
        var zeroKernel = RigidKernel(gear, EffectiveMass, .01);
        zeroKernel.SetInitialState(0, 1);
        zeroKernel.SetBrakeForceProvider(delegate(int index, double velocity) { return FrameTotal(pressureFrame, velocity); });
        zeroKernel.Step(.1);
        Near("zero_BC_no_mechanical_deceleration", zeroKernel.GetVehicle(0).VelocityV, 1, 1e-12);

        foreach (double p in new[] { 130000.0, 160000, 300000, 340000 }) {
            SetUniform(pressureFrame, p);
            foreach (double kmh in new[] { 0.0, 40, 80, 120 }) {
                double v = kmh / 3.6, wagon = pressureFrame.CapacityNewtons(1, v);
                double loco = pressureFrame.CapacityNewtons(0, v), total = FrameTotal(pressureFrame, v);
                double expectedWagon = ReferenceWagon(p, v), expectedLoco = 4 * ReferenceBogie(p, v);
                string id = p + "Pa_" + kmh + "kmh";
                Near("SI_wagon_" + id, wagon, expectedWagon, 1e-7);
                Near("SI_four_bogies_" + id, loco, expectedLoco, 1e-7);
                Near("SI_total76_" + id, total, expectedLoco + 75 * expectedWagon, 1e-5);
                Near("reverse_capacity_" + id, FrameTotal(pressureFrame, -v), total, 1e-7);
                forceTable.Add(new { pressure_Pa = p, speed_kmh = kmh, wagon_force_N = wagon, locomotive_force_N = loco,
                    total76_force_N = total, independent_wagon_force_N = expectedWagon, independent_total_force_N = expectedLoco + 75 * expectedWagon });
            }
        }

        var vehicleP = Pressures(76, GaugePa); var bogieP = new[] { 0.0, 100000, 200000, 300000 };
        pressureFrame.SetPressures(vehicleP, bogieP); double expectedUnequal = 0;
        for (int i = 0; i < 4; i++) expectedUnequal += ReferenceBogie(bogieP[i], 10);
        double actualUnequal = pressureFrame.CapacityNewtons(0, 10);
        Near("four_unequal_bogies_sum_independently", actualUnequal, expectedUnequal, 1e-7);
        Check("nonlinear_bogies_not_replaced_by_average", Math.Abs(actualUnequal - 4 * ReferenceBogie(150000, 10)) > 1,
            new { actualUnequal, mean_pressure_surrogate_N = 4 * ReferenceBogie(150000, 10) });
        double wagonBefore = pressureFrame.CapacityNewtons(1, 10), locoBefore = actualUnequal;
        long generation = pressureFrame.Generation;
        vehicleP[1] = 0; bogieP[3] = 0;
        var copy = pressureFrame.VehiclePressuresPa; copy[1] = 0;
        var bogieCopy = pressureFrame.BogiePressuresPa; bogieCopy[3] = 0;
        Check("pressure_input_and_output_arrays_are_cloned", pressureFrame.VehiclePressuresPa[1] == GaugePa && pressureFrame.BogiePressuresPa[3] == 300000, null);
        Near("cloned_frame_keeps_wagon_force", pressureFrame.CapacityNewtons(1, 10), wagonBefore, 0);
        Near("cloned_frame_keeps_bogie_force", pressureFrame.CapacityNewtons(0, 10), locoBefore, 0);
        var invalidP = pressureFrame.VehiclePressuresPa; invalidP[75] = Double.NaN;
        Reject<ArgumentOutOfRangeException>("NaN_last_vehicle_transaction_refused", delegate { pressureFrame.SetPressures(invalidP, pressureFrame.BogiePressuresPa); });
        var invalidB = pressureFrame.BogiePressuresPa; invalidB[3] = -1;
        Reject<ArgumentOutOfRangeException>("negative_last_bogie_transaction_refused", delegate { pressureFrame.SetPressures(pressureFrame.VehiclePressuresPa, invalidB); });
        var hugeB = pressureFrame.BogiePressuresPa; hugeB[3] = Double.MaxValue;
        Reject<ArgumentOutOfRangeException>("overflowing_force_transaction_refused", delegate { pressureFrame.SetPressures(pressureFrame.VehiclePressuresPa, hugeB); });
        Reject<ArgumentException>("wrong_pressure_array_length_refused", delegate { pressureFrame.SetPressures(new double[75], new double[4]); });
        Reject<ArgumentOutOfRangeException>("invalid_capacity_index_refused", delegate { pressureFrame.CapacityNewtons(76, 0); });
        Reject<ArgumentOutOfRangeException>("nonfinite_speed_refused", delegate { pressureFrame.CapacityNewtons(0, Double.PositiveInfinity); });
        Check("failed_pressure_transactions_do_not_publish_generation", pressureFrame.Generation == generation, pressureFrame.Generation);
        Near("failed_pressure_transactions_preserve_wagon_force", pressureFrame.CapacityNewtons(1, 10), wagonBefore, 0);
        Near("failed_pressure_transactions_preserve_bogie_force", pressureFrame.CapacityNewtons(0, 10), locoBefore, 0);
        Check("failed_pressure_transactions_preserve_all_pressure_arrays",
            Same(pressureFrame.VehiclePressuresPa, Pressures(76, GaugePa)) &&
            Same(pressureFrame.BogiePressuresPa, new[] { 0.0, 100000, 200000, 300000 }), null);

        var gradeKernel = RigidKernel(gear, EffectiveMass, .01);
        gradeKernel.SetInitialState(0, 0);
        gradeKernel.SetBrakeForceProvider(delegate(int index, double velocity) { return 0; });
        double gravity = -StaticMass * 9.80665 * .01;
        gradeKernel.SetRouteForces(0, gravity, 0); gradeKernel.Step(.1);
        Near("grade_uses_real_mass_inertia_uses_effective_mass", gradeKernel.GetVehicle(0).AccelerationA, gravity / EffectiveMass, 1e-10);
        Near("grade_preserves_6200t_physical_mass", gradeKernel.GetVehicle(0).MassKg, StaticMass, 0);
    }
    static bool Same(double[] a, double[] b) {
        if (a.Length != b.Length) return false;
        for (int i = 0; i < a.Length; i++) if (a[i] != b[i]) return false;
        return true;
    }
    static void Benchmark(TrainerScientificMechanicsProfile profile, FrictionDraftGearModel gear) {
        var frame = new TrainerScientificBrakeFrame(profile); SetUniform(frame, GaugePa);
        double distance2048 = Simpson(EffectiveMass, InitialSpeed, 2048, true);
        double distance4096 = Simpson(EffectiveMass, InitialSpeed, 4096, true);
        double time2048 = Simpson(EffectiveMass, InitialSpeed, 2048, false);
        double time4096 = Simpson(EffectiveMass, InitialSpeed, 4096, false);
        Near("independent_Simpson_distance_refinement", distance2048, distance4096, 1e-8);
        Near("independent_Simpson_time_refinement", time2048, time4096, 1e-9);
        quadratureEvidence = new { method = "Composite Simpson in velocity; t=integral(m/B(v))dv, s=integral(m*v/B(v))dv",
            independent_capacity = "75 literal composite-wagon equations plus four literal locomotive type-M bogie equations",
            distance_2048_m = distance2048, distance_4096_m = distance4096,
            time_2048_s = time2048, time_4096_s = time4096, pressure_Pa = GaugePa, initial_speed_kmh = 80 };
        var coarse = Stop(frame, gear, EffectiveMass, InitialSpeed, .04);
        var middle = Stop(frame, gear, EffectiveMass, InitialSpeed, .02);
        var finer = Stop(frame, gear, EffectiveMass, InitialSpeed, .01);
        var fine = Stop(frame, gear, EffectiveMass, InitialSpeed, .005);
        Check("stop_distance_converges_with_timestep_refinement",
            Math.Abs(middle.distance_error_m) < .65 * Math.Abs(coarse.distance_error_m) &&
            Math.Abs(finer.distance_error_m) < .65 * Math.Abs(middle.distance_error_m) &&
            Math.Abs(fine.distance_error_m) < .65 * Math.Abs(finer.distance_error_m), new { coarse, middle, finer, fine });
        Check("fine_stop_distance_relative_error_below_0_1percent", Math.Abs(fine.distance_error_m) <= .001 * distance4096, fine);
        double massLow = 200000 * 1.20 + 75 * 80000 * 1.03;
        double massHigh = 200000 * 1.30 + 75 * 80000 * 1.04;
        Near("low_inertia_bound_6420t", massLow, 6420000, 1e-7);
        Near("high_inertia_bound_6500t", massHigh, 6500000, 1e-7);
        var low = Stop(frame, gear, massLow, InitialSpeed, .005);
        var high = Stop(frame, gear, massHigh, InitialSpeed, .005);
        double referenceLow = Simpson(massLow, InitialSpeed, 4096, true), referenceHigh = Simpson(massHigh, InitialSpeed, 4096, true);
        Near("quadrature_distance_exactly_scales_with_effective_mass", referenceHigh / referenceLow, massHigh / massLow, 1e-12);
        Check("measured_inertia_sensitivity_matches_independent_mass_scaling",
            Math.Abs(high.distance_m / low.distance_m - massHigh / massLow) < 2e-5, new { low, high });
        sensitivityEvidence = new { profile_gamma_loco_low = .20, profile_gamma_loco_high = .30,
            profile_gamma_wagon_low = .03, profile_gamma_wagon_high = .04,
            low, high, expected_distance_ratio = massHigh / massLow,
            exact_proportionality_scope = "Same constant pressure, same speed law, no grade/rolling/coupler forces; not a general train-braking distance rule." };
        Stop(frame, gear, EffectiveMass, -10 / 3.6, .005);
        Check("constant_pressure_fixture_does_not_evolve_air", Same(frame.VehiclePressuresPa, Pressures(76, GaugePa)) && Same(frame.BogiePressuresPa, Pressures(4, GaugePa)), null);
    }

    static string FindProfile(string[] args) {
        if (args.Length > 0) return Path.GetFullPath(args[0]);
        foreach (string start in new[] { Environment.CurrentDirectory, AppDomain.CurrentDomain.BaseDirectory }) {
            var dir = new DirectoryInfo(start);
            for (; dir != null; dir = dir.Parent) {
                string candidate = Path.Combine(dir.FullName, "dev", "config", "train-mechanics.json");
                if (File.Exists(candidate)) return candidate;
            }
        }
        throw new FileNotFoundException("dev/config/train-mechanics.json not found; supply its path as the first argument.");
    }
    public static int Main(string[] args) {
        bool passed = false; string error = null;
        try {
            profilePath = FindProfile(args);
            using (var hash = SHA256.Create()) using (var stream = File.OpenRead(profilePath)) profileHash = BitConverter.ToString(hash.ComputeHash(stream)).Replace("-", "");
            var profile = TrainerScientificMechanicsProfile.Load(profilePath); var gear = profile.CreateCouplers();
            ProfileAndForces(profile, gear); Benchmark(profile, gear); passed = true;
        } catch (Exception ex) { error = ex.ToString(); Console.WriteLine(error); }
        string resultDir = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");
        if (String.IsNullOrWhiteSpace(resultDir)) resultDir = AppDomain.CurrentDomain.BaseDirectory;
        Directory.CreateDirectory(resultDir);
        File.WriteAllText(Path.Combine(resultDir, "Trainer_scientific_mechanics_validation.json"),
            new JavaScriptSerializer { MaxJsonLength = 16000000 }.Serialize(new {
                passed, exit_code = passed ? 0 : 1, error, assertions = checks.Count, checks,
                profile_path = profilePath, profile_sha256 = profileHash, force_table = forceTable,
                quadrature = quadratureEvidence, stop_table = stopTable, inertia_sensitivity = sensitivityEvidence,
                scope = "Standalone scientific-reference mechanics only: direct SI pressure-to-capacity, immutable transactional pressure frames and rigid aggregate stopping against independent velocity quadrature.",
                fixture = "75 equal loaded80t wagon force sources plus four individually pressured bogie force sources, rigid effective6460t body; constant300kPa during stopping, zero rolling/grade, no coupler oscillation.",
                not_accepted = "No native DLL or pressure-wave execution; not a real KM/SAUT/EPK brake path, wheel-slide/thermal/adhesion calibration, exact installed equipment or whole-train longitudinal-force acceptance.",
                energy_balance = "Independent endpoint brake work plus explicit BE numerical loss. Acceptance is sum(m*1e-10*max(1,abs(v_end))*abs(v_end))+documented IEEE754 rounding, separately required below1e-6 of initial energy. Independent velocity residual and momentum-to-energy identity are also checked; reported dissipation is not calibrated brake heat."
            }));
        Console.WriteLine((passed ? "PASS: " : "FAIL: ") + checks.Count + " scientific mechanics assertions");
        return passed ? 0 : 1;
    }
}
