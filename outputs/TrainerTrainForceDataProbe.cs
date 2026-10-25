using System;
using System.Collections.Generic;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Simulation;
using RailwayDynamics.Couplers;

// Pure managed force accounting and history checks; no host/native calls.
public static class TrainerTrainForceDataProbe {
    static readonly List<object> checks = new List<object>();
    static void Check(string id, bool passed, object detail) {
        checks.Add(new { id, passed, detail }); if (!passed) throw new InvalidOperationException(id);
    }
    static void Near(string id, double actual, double expected, double tolerance) {
        Check(id, Math.Abs(actual - expected) <= tolerance, new { actual, expected, tolerance });
    }
    static void Reject<T>(string id, Action action) where T : Exception {
        bool rejected = false; try { action(); } catch (T) { rejected = true; }
        Check(id, rejected, typeof(T).Name);
    }
    static VehicleDynamicsNode Body(double mass, double gamma, double centre) {
        return new VehicleDynamicsNode { MassKg = mass, RotaryInertiaGamma = gamma, LengthM = 10,
            PositionX = centre, AxleCount = 4, ShoeType = BrakeShoeType.Composite };
    }
    static LongitudinalTrainDynamicsSolver Kernel(VehicleDynamicsNode[] nodes) {
        var solver = new LongitudinalTrainDynamicsSolver(nodes, FrictionDraftGearModel.CreateDefaultFreightDraftGear());
        solver.RunningResistanceEnabled = false; solver.MaximumSubstepSeconds = .05;
        for (int i = 0; i < nodes.Length; i++) solver.SetVehicleBrakeForceMagnitude(i, 0);
        return solver;
    }
    static void Balance(string id, TrainForceSample sample) {
        Check(id + "_accepted", sample.HasAcceptedStep && sample.AcceptedSubstepSeconds > 0, sample.TimeSeconds);
        double couplerSum = 0, externalSum = 0, net = 0, inertia = 0;
        for (int i = 0; i < sample.VehicleCount; i++) {
            var body = sample.GetVehicle(i);
            Near(id + "_body" + i, body.NetForceN, body.InertiaForceN, body.BalanceToleranceN);
            Near(id + "_channel_sum" + i, body.NetForceN, body.TractionN + body.DynamicBrakeN +
                body.PneumaticBrakeN + body.RollingN + body.CurveN + body.GradeN + body.FrontCouplerN + body.RearCouplerN, 1e-9);
            couplerSum += body.FrontCouplerN + body.RearCouplerN;
            externalSum += body.TractionN + body.DynamicBrakeN + body.PneumaticBrakeN + body.RollingN + body.CurveN + body.GradeN;
            net += body.NetForceN; inertia += body.InertiaForceN;
        }
        Near(id + "_internal_pair_forces_cancel", couplerSum, 0, 1e-8);
        Near(id + "_global_external_equals_net", externalSum, net, 1e-8);
        Check(id + "_inertia_is_finite", !Double.IsNaN(inertia) && !Double.IsInfinity(inertia), inertia);
    }
    static void SingleBodyAndEdt() {
        var solver = Kernel(new[] { Body(200000, .25, 0) }); solver.SetInitialState(1000, 10);
        solver.SetVehicleForce(0, 3000); solver.SetVehicleBrakeForceMagnitude(0, 500);
        solver.SetRouteForces(0, -300, 200); solver.Step(.02);
        var sample = TrainForceSample.Capture(solver, 1); var body = sample.GetVehicle(0);
        Near("analytic_physical_mass", body.PhysicalMassKg, 200000, 0);
        Near("analytic_effective_mass", body.EffectiveMassKg, 250000, 0);
        Near("analytic_traction", body.TractionN, 3000, 0);
        Near("analytic_grade", body.GradeN, -300, 0);
        Near("analytic_pneumatic_applied", body.PneumaticBrakeN, -500, 1e-6);
        Near("analytic_curve_applied", body.CurveN, -200, 1e-6);
        Near("analytic_net", body.NetForceN, 2000, 1e-6);
        Near("analytic_acceleration", body.AccelerationMps2, .008, 1e-10); Balance("analytic", sample);
        double frozenTime = sample.TimeSeconds, frozenPressure = body.BrakeCylinderPressurePa;
        solver.SetVehicleForce(0, 999999); solver.SetRouteForces(0, 1234, 999);
        solver.SetBrakeCylinderPressure(0, 300000);
        var frozen = TrainForceSample.Capture(solver, 1);
        Near("pending_traction_is_not_actual", frozen.GetVehicle(0).TractionN, body.TractionN, 0);
        Near("pending_grade_is_not_actual", frozen.GetVehicle(0).GradeN, body.GradeN, 0);
        Near("pending_pressure_does_not_relabel_accepted_frame", frozen.GetVehicle(0).BrakeCylinderPressurePa, frozenPressure, 0);
        Near("pending_inputs_keep_accepted_time", frozen.TimeSeconds, frozenTime, 0);
        var copy = frozen.Vehicles; copy[0] = null;
        Check("sample_array_is_cloned", frozen.GetVehicle(0) != null, null);
        VehicleDynamicsNode[] cacheNodes; double[] cacheRear; double cacheDt, cacheTime;
        solver.TryGetLastAcceptedForceStep(out cacheNodes, out cacheRear, out cacheDt, out cacheTime);
        cacheNodes[0].TractiveForceN = -123;
        Near("kernel_endpoint_getter_is_cloned", TrainForceSample.Capture(solver, 1).GetVehicle(0).TractionN, 3000, 0);

        var edt = Kernel(new[] { Body(1000, 0, 0) }); edt.SetInitialState(0, .01);
        edt.SetVehicleDynamicBrakeForceMagnitude(0, 1000); edt.SetVehicleForce(0, 600); edt.Step(.05);
        var first = TrainForceSample.Capture(edt, 2);
        Near("EDT_uses_consumed_impulse_not_capacity", first.GetVehicle(0).DynamicBrakeN, -200, 1e-10);
        Near("EDT_split_then_drive_acceleration", first.GetVehicle(0).AccelerationMps2, .4, 1e-12);
        Near("EDT_split_end_velocity", first.GetVehicle(0).SpeedMps, .03, 1e-12); Balance("EDT_first", first);
        edt.Step(.05); var second = TrainForceSample.Capture(edt, 2);
        Near("last_substep_EDT_reaction", second.GetVehicle(0).DynamicBrakeN, -600, 1e-9);
        Near("last_substep_acceleration_not_outer_average", second.GetVehicle(0).AccelerationMps2, 0, 1e-12); Balance("EDT_second", second);
        Near("frozen_sample_does_not_change_on_next_step", first.GetVehicle(0).DynamicBrakeN, -200, 1e-10);

        var rest = Kernel(new[] { Body(1000, 0, 0) }); rest.SetInitialState(0, 0);
        rest.SetVehicleDynamicBrakeForceMagnitude(0, 1000); rest.SetVehicleForce(0, 600); rest.Step(.05);
        var noHold = TrainForceSample.Capture(rest, 3);
        Near("EDT_has_no_fictitious_static_holding", noHold.GetVehicle(0).DynamicBrakeN, 0, 0);
        Near("EDT_does_not_hide_drive_from_rest", noHold.GetVehicle(0).NetForceN, 600, 1e-9); Balance("EDT_rest", noHold);

        var reverse = Kernel(new[] { Body(1000, 0, 0) }); reverse.SetInitialState(0, -.01);
        reverse.SetVehicleDynamicBrakeForceMagnitude(0, 1000); reverse.SetVehicleForce(0, -600); reverse.Step(.05);
        var backward = TrainForceSample.Capture(reverse, 4);
        Near("reverse_EDT_applies_positiveX", backward.GetVehicle(0).DynamicBrakeN, 200, 1e-10);
        Near("reverse_EDT_split_acceleration", backward.GetVehicle(0).AccelerationMps2, -.4, 1e-12); Balance("EDT_reverse", backward);

        var stat = Kernel(new[] { Body(1000, 0, 0) }); stat.SetInitialState(0, 0);
        stat.RunningResistanceEnabled = true; stat.SetVehicleBrakeForceMagnitude(0, 50);
        stat.SetRouteForces(0, 0, 400); stat.SetVehicleForce(0, 300); stat.Step(.05);
        var held = TrainForceSample.Capture(stat, 5).GetVehicle(0);
        double rollingCapacity = 1000 * 9.80665 * .001 * (.7 + 3 / .25);
        Near("static_actual_brake_uses_first50N", held.PneumaticBrakeN, -50, 1e-10);
        Near("static_actual_rolling_uses_available_reaction", held.RollingN, -rollingCapacity, 1e-10);
        Near("static_curve_is_remainder_not400N_capacity", held.CurveN, -(300 - 50 - rollingCapacity), 1e-10);
        Near("static_net_is_zero", held.NetForceN, 0, 1e-10); Balance("static_hold", TrainForceSample.Capture(stat, 5));
    }
    static void CouplersAndRollback() {
        foreach (double secondCentre in new[] { -9.95, -10.05 }) {
            var solver = Kernel(new[] { Body(100000, .2, 0), Body(80000, .035, secondCentre) });
            solver.Step(1e-6); var sample = TrainForceSample.Capture(solver, 6);
            double rear = solver.GetCouplerForceSignedNewtons(0);
            Near("rear_signed_applied_" + secondCentre, sample.GetVehicle(0).RearCouplerN, rear, 0);
            Near("front_opposite_reaction_" + secondCentre, sample.GetVehicle(1).FrontCouplerN, -rear, 0);
            Near("link_tension_is_negative_kernel_rear_" + secondCentre, sample.GetLinkTensionN(0), -rear, 0);
            Check("compression_tension_direction_" + secondCentre, secondCentre > -10 ? rear > 0 : rear < 0, rear);
            Near("free_head_front_boundary_" + secondCentre, sample.GetVehicle(0).FrontCouplerN, 0, 0);
            Near("free_tail_rear_boundary_" + secondCentre, sample.GetVehicle(1).RearCouplerN, 0, 0);
            var links = sample.LinkTensionN; links[0] = 0;
            Near("link_array_is_cloned_" + secondCentre, sample.GetLinkTensionN(0), -rear, 0); Balance("coupler_" + secondCentre, sample);
        }
        var failure = Kernel(new[] { Body(1000, 0, 0) }); failure.MaximumSubstepSeconds = .01;
        failure.SetInitialState(0, 1); failure.SetVehicleForce(0, 100); failure.Step(.02);
        var before = TrainForceSample.Capture(failure, 7); bool progressedInsideFailedStep = false;
        double failAt = before.TimeSeconds + .015;
        failure.SetBrakeForceProvider(delegate(int index, double velocity) {
            if (failure.SimulationTimeSeconds > before.TimeSeconds) progressedInsideFailedStep = true;
            if (failure.SimulationTimeSeconds >= failAt) throw new InvalidOperationException("Explicit provider fault after accepted substeps.");
            return 0;
        });
        Reject<InvalidOperationException>("later_substep_failure_is_reported", delegate { failure.Step(.03); });
        var after = TrainForceSample.Capture(failure, 7);
        Check("fault_fixture_did_reach_later_substep", progressedInsideFailedStep, null);
        Near("failed_outer_step_restores_snapshot_time", after.TimeSeconds, before.TimeSeconds, 0);
        Near("failed_outer_step_restores_actual_force", after.GetVehicle(0).TractionN, before.GetVehicle(0).TractionN, 0);
        Near("failed_outer_step_restores_endpoint_velocity", after.GetVehicle(0).SpeedMps, before.GetVehicle(0).SpeedMps, 0);
        Near("failed_outer_step_restores_endpoint_geometry", after.GetVehicle(0).FrontPositionM, before.GetVehicle(0).FrontPositionM, 0);
        Near("failed_outer_step_restores_energy_context", after.TotalEnergyJ, before.TotalEnergyJ, 0);
        failure.SetBrakeForceProvider(null); failure.SetInitialState(200, 3);
        var reset = TrainForceSample.Capture(failure, 8);
        Check("reset_invalidates_last_accepted_force", !reset.HasAcceptedStep && reset.AcceptedSubstepSeconds == 0, reset.TimeSeconds);
        Near("reset_geometry_remains_available", reset.GetVehicle(0).FrontPositionM, 200, 0);
        Near("reset_speed_remains_available", reset.GetVehicle(0).SpeedMps, 3, 0);
        Near("reset_does_not_show_pending_traction_as_actual", reset.GetVehicle(0).TractionN, 0, 0);
        Near("reset_zero_net_does_not_claim_static_reaction", reset.GetVehicle(0).NetForceN, 0, 0);
    }
    static void FullGeometryAndHistory() {
        var nodes = LongitudinalTrainDynamicsSolver.CreateFreightConsist();
        nodes[0].RotaryInertiaGamma = .25;
        for (int i = 1; i < nodes.Length; i++) nodes[i].RotaryInertiaGamma = .035;
        var full = Kernel(nodes); full.SetInitialState(5000, 2);
        for (int i = 0; i < full.VehicleCount; i++) full.SetBrakeCylinderPressure(i, i * 1000);
        full.Step(.001); var sample = TrainForceSample.Capture(full, 9);
        Check("actual76bodies75links", sample.VehicleCount == 76 && sample.LinkCount == 75, sample.VehicleCount);
        Near("full_physical6200t", sample.TotalPhysicalMassKg, 6200000, 0);
        Near("full_effective6460t", sample.TotalEffectiveMassKg, 6460000, 1e-7);
        Near("sample_time_is_last_accepted_end", sample.TimeSeconds, full.SimulationTimeSeconds, 0);
        Check("sample_h_is_last_adaptive_substep", sample.AcceptedSubstepSeconds > 0 && sample.AcceptedSubstepSeconds <= .001, sample.AcceptedSubstepSeconds);
        for (int i = 0; i < 76; i++) {
            Near("measured_pressure_metadata_" + i, sample.GetVehicle(i).BrakeCylinderPressurePa, i * 1000, 0);
            Near("head_relative_geometry_" + i, sample.GetVehicle(i).DistanceBehindHeadM, sample.GetVehicle(0).FrontPositionM - sample.GetVehicle(i).FrontPositionM, 0);
        }
        Balance("full_coast", sample);
        var moved = Kernel(nodes); moved.SetInitialState(15000, 2); moved.Step(.001);
        var translated = TrainForceSample.Capture(moved, 10);
        for (int i = 0; i < 76; i++) {
            Near("uniform_translation_preserves_relative_geometry_" + i, translated.GetVehicle(i).DistanceBehindHeadM, sample.GetVehicle(i).DistanceBehindHeadM, 1e-7);
            Near("uniform_translation_preserves_actual_forces_" + i, translated.GetVehicle(i).NetForceN, sample.GetVehicle(i).NetForceN, 1e-6);
        }
        var timelineKernel = Kernel(new[] { Body(1000, 0, 0) }); timelineKernel.SetInitialState(0, 1);
        var history = new TrainForceHistory(.0625, 3); history.Add(TrainForceSample.Capture(timelineKernel, 11));
        for (int i = 0; i < 4; i++) { timelineKernel.Step(.03125); history.Push(TrainForceSample.Capture(timelineKernel, 11)); }
        Check("history_bounded_by_time_and_count", history.Count == 3 && history.Latest.TimeSeconds - history.ToArray()[0].TimeSeconds <= .0625, history.Count);
        var duplicate = TrainForceSample.Capture(timelineKernel, 11); history.Add(duplicate);
        Check("duplicate_time_replaces_latest", history.Count == 3 && Object.ReferenceEquals(history.Latest, duplicate), history.Count);
        var exported = history.Samples; exported[0] = null;
        Check("history_array_is_cloned", history.ToArray()[0] != null, null);
        timelineKernel.SetInitialState(10, 0);
        Reject<InvalidOperationException>("time_reset_needs_new_epoch", delegate { history.Add(TrainForceSample.Capture(timelineKernel, 11)); });
        history.Add(TrainForceSample.Capture(timelineKernel, 12));
        Check("new_epoch_clears_history_without_invented_transition", history.Count == 1 && history.Latest.Epoch == 12 && !history.Latest.HasAcceptedStep, history.Count);
        history.Clear(); Check("history_clear_removes_latest", history.Count == 0 && history.Latest == null, null);
        Reject<ArgumentNullException>("null_history_sample_rejected", delegate { history.Add(null); });
        Reject<ArgumentOutOfRangeException>("negative_sample_epoch_rejected", delegate { TrainForceSample.Capture(timelineKernel, -1); });
        Reject<ArgumentOutOfRangeException>("zero_history_limit_rejected", delegate { new TrainForceHistory(1, 0); });
    }
    public static int Main() {
        bool passed = false; string error = null;
        try { SingleBodyAndEdt(); CouplersAndRollback(); FullGeometryAndHistory(); passed = true; }
        catch (Exception ex) { error = ex.ToString(); Console.WriteLine(error); }
        string dir = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");
        if (String.IsNullOrWhiteSpace(dir)) dir = AppDomain.CurrentDomain.BaseDirectory;
        Directory.CreateDirectory(dir);
        File.WriteAllText(Path.Combine(dir, "Trainer_train_force_data_validation.json"),
            new JavaScriptSerializer { MaxJsonLength = 8000000 }.Serialize(new {
                passed, exit_code = passed ? 0 : 1, assertions = checks.Count, error, checks,
                scope = "Pure managed actual last-substep force accounting, EDT split, static reaction allocation, cached endpoint rollback/reset, immutable76body/75link samples and bounded noninterpolating history. No native or UI execution.",
                sign = "Applied body forces positive in fixed+X toward head; link tension positive=-kernel rear force. A head-left chart has+X left.",
                limits = "Force residual is displayed with its solver-derived Newton bound, not forced to zero. History records actual samples only and makes no claim of wave speed/direction."
            }));
        Console.WriteLine((passed ? "PASS " : "FAIL ") + checks.Count + " train force data assertions");
        return passed ? 0 : 1;
    }
}
