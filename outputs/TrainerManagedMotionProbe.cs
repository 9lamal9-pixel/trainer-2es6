using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using RailwayDynamics.Simulation;

public sealed class TrainerManagedMotionProbe {
    [STAThread] public static int Main() { return TrainerAutovedenieAdverseBrakeProbe.RunManagedMotion(); }
}

public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
    public static int RunManagedMotion() {
        Application.EnableVisualStyles(); thread = GetCurrentThreadId(); Watch.Restart();
        Checks.Clear(); Cases.Clear(); failure = null;
        int exit = 1; Fixture fixture = null;
        try { fixture = new Fixture(); fixture.MeasureManagedMotion(); exit = 0; }
        catch (Exception e) { failure = e.ToString(); if (fixture != null) fixture.error = failure; Console.WriteLine(e); }
        finally {
            if (fixture != null) {
                try { fixture.CloseManagedMotionProbe(); }
                catch (Exception e) { fixture.error = (fixture.error ?? "") + "\nLTD RESTORE: " + e; exit = 1; }
                if (!TrainerNativeBrakeForceProvider.HasRetainedOwners) fixture.Close();
                else { fixture.error = (fixture.error ?? "") + "\nNative close refused: brake oracle ownership retained"; exit = 1; }
                if (fixture.error != null || !fixture.restored || !fixture.closed) exit = 1;
                Cases.Add(fixture.ManagedMotionResult());
            }
        }
        File.WriteAllText(TrainerSession.ResultPath("Trainer_managed_motion_validation.json"), new JavaScriptSerializer { MaxJsonLength = 16000000 }.Serialize(new {
            passed = exit == 0, exit_code = exit, error = failure, checks = Checks, cases = Cases,
            scope = "Genuine native electrical/pneumatic start drives the managed 76-body movement owner. Physical KM V/IV/II service/release, actual pressure/native brake-capacity oracle, front/centre/velocity/coupler publication and borrowed-buffer restoration. Prepared station/signed-speed components are separate. Teaching green and scripted RBS are explicit fixture inputs. No force/current/pressure/permission output preparation. Full route, factory LTD/adhesion/rotary/draft-gear calibration and genuine cab EDT remain unaccepted.",
            maximum_wall_seconds = 280, maximum_fixture_cycles = 3500, factory_calibration_accepted = false
        }));
        Console.WriteLine(exit == 0 ? "PASS: genuine managed movement integration" : "FAIL: managed movement integration");
        return exit;
    }

    sealed partial class Fixture {
        LongitudinalTrainDynamicsSolver motionKernelUnderTest;
        TrainerMarkupSignalSource motionTeachingSource;
        readonly List<object> managedMotionRows = new List<object>();
        object managedCoastEvidence, managedServiceEvidence, managedReleaseEvidence;
        bool managedOwnerClosed, managedTeachingRestored;
        int managedPreparedStates;

        void ManagedBudget() { if (cycle >= 3500 || Watch.Elapsed.TotalSeconds > 280) throw new Exception("Managed movement fixture budget exceeded"); }
        void ReadMotionOwner() {
            motionKernelUnderTest = Field("managedMotion") as LongitudinalTrainDynamicsSolver;
            if (motionKernelUnderTest == null) throw new Exception("Managed movement owner absent");
        }
        object MotionSnapshot(string name) {
            ReadMotionOwner(); var head = motionKernelUnderTest.GetVehicle(0); var tail = motionKernelUnderTest.GetVehicle(75);
            return new { name, cycle, prepared_component = managedPreparedStates != 0, front_m = motionKernelUnderTest.GetVehicleFrontPosition(0),
                centre_m = head.PositionX, signed_v_m_s = head.VelocityV, tail_v_m_s = tail.VelocityV,
                kinetic_and_coupler_J = motionKernelUnderTest.TotalEnergyJ, external_work_J = motionKernelUnderTest.CumulativeExternalWorkJ,
                dissipated_J = motionKernelUnderTest.CumulativeDissipatedEnergyJ, substeps = motionKernelUnderTest.LastSubstepCount,
                rejected_substeps = motionKernelUnderTest.LastRejectedSubstepCount, native_force_10kN = Float(mem["TResTyga"], 0),
                actual_tc_kgf_cm2 = new[] { Float(mem["TLocParam"], 13), Float(motionCars[1], 4), Float(motionCars[75], 4) } };
        }
        void VerifyMotionPublication(string name) {
            ReadMotionOwner(); double mass = 0, maxV = 0; bool positions = true, coupling = true, pressure = true, finite = true;
            for (int i = 0; i < 76; i++) {
                var v = motionKernelUnderTest.GetVehicle(i); mass += v.MassKg; maxV = Math.Max(maxV, Math.Abs(v.VelocityV) * 3.6);
                float expectedX = (float)motionKernelUnderTest.GetVehicleFrontPosition(i);
                float expectedCoupler = i == 0 ? 0 : (float)(-motionKernelUnderTest.GetCouplerForceSignedNewtons(i - 1) / 10000);
                positions &= Float(motionCars[i], 0x14) == expectedX;
                coupling &= Float(motionCars[i], 0xc) == expectedCoupler;
                double expectedPressure = (i == 0 ? Float(mem["TLocParam"], 13) : Float(motionCars[i], 4)) * 98066.5;
                pressure &= Math.Abs(v.BrakeCylinderPressurePa - expectedPressure) <= .01;
                finite &= Finite(v.PositionX) && Finite(v.VelocityV) && Finite(v.AccelerationA);
            }
            Check(name + " 76 real masses", motionKernelUnderTest.VehicleCount == 76 && mass == 6200000 && Math.Abs(motionKernelUnderTest.TotalLengthM - 1126.18) < 1e-8, "76/6200t/1126.18m", mass);
            Check(name + " front coordinates for every body", positions, true, positions);
            Check(name + " front-link 10kN sign and free head", coupling && Float(motionCars[0], 0xc) == 0, true, coupling);
            Check(name + " actual measured BC reaches kernel", pressure, true, pressure);
            Check(name + " finite state", finite, true, finite);
            Check(name + " same movement delegates", ((Delegate)Field("stateTrain")).Method.Name == "ResetManagedMotion" && ((Delegate)Field("readTrain")).Method.Name == "ReadManagedMotion" && ((Delegate)Field("motionStep")).Method.Name == "StepManagedMotion", "one managed owner", true);
            Check(name + " absolute speed sensor", Math.Abs(Float(mem["TInputParamPult"], 1) - Math.Abs(motionKernelUnderTest.GetVehicle(0).VelocityV) * 3.6) < 1e-4, "abs(v)*3.6", Float(mem["TInputParamPult"], 1));
            Check(name + " maximum actual body speed", Math.Abs((double)Field("maximumConsistSpeedKmh") - maxV) < 1e-4, maxV, Field("maximumConsistSpeedKmh"));
            managedMotionRows.Add(MotionSnapshot(name));
        }

        byte[] CaptureOracleBuffers() {
            var m = GetModuleHandle("POEZD.DLL"); if (m == IntPtr.Zero) throw new Exception("Brake oracle module absent");
            var bytes = new List<byte>();
            foreach (int rva in new[] { TrainerNativeBrakeForceProvider.VelocityRva, TrainerNativeBrakeForceProvider.BrakeForceRva, TrainerNativeBrakeForceProvider.PressureRva }) {
                IntPtr at = IntPtr.Add(m, rva); Require(at, TrainerNativeBrakeForceProvider.BufferBytes, false);
                byte[] copy = new byte[TrainerNativeBrakeForceProvider.BufferBytes]; Marshal.Copy(at, copy, 0, copy.Length); bytes.AddRange(copy);
            }
            bytes.AddRange(BitConverter.GetBytes(Marshal.ReadInt32(m, TrainerNativeBrakeForceProvider.CursorRva)));
            return bytes.ToArray();
        }
        void VerifyNextVelocityAndOracle(string name) {
            ReadMotionOwner(); double expectedNextInput = (float)motionKernelUnderTest.GetVehicle(0).VelocityV;
            Action previousBefore = beforeNativePneumatic, previousAfter = afterNativeMotion;
            byte[] before = null; double actualInput = 0; bool inputRead = false;
            try {
                beforeNativePneumatic = delegate {
                    if (previousBefore != null) previousBefore();
                    byte[] input = new byte[8]; Marshal.Copy(mem["TTygaParam"], input, 0, input.Length);
                    actualInput = BitConverter.ToDouble(input, 0); inputRead = true;
                    before = CaptureOracleBuffers();
                };
                afterNativeMotion = delegate {
                    if (previousAfter != null) previousAfter();
                    Check(name + " borrowed oracle buffers restored", before != null && before.SequenceEqual(CaptureOracleBuffers()), true, before != null);
                };
                ManagedBudget(); Step();
            } finally { beforeNativePneumatic = previousBefore; afterNativeMotion = previousAfter; }
            Check(name + " previous signed speed supplied to next electrical frame", inputRead && Math.Abs(actualInput - expectedNextInput) < 1e-7, expectedNextInput, actualInput);
            var p = Field("managedBrakeProvider") as TrainerNativeBrakeForceProvider; if (p == null) throw new Exception("Brake provider absent");
            p.Validate(); var snapshot = p.Snapshot();
            Check(name + " native oracle restoration verified", Convert.ToBoolean(snapshot["LastRestoreVerified"]) && !Convert.ToBoolean(snapshot["PendingRestore"]) && Convert.ToInt64(snapshot["SuccessfulEvaluations"]) > 0, true, snapshot);
        }

        void VerifyNativeBrakeCapacity(string name, bool requirePositive) {
            var p = Field("managedBrakeProvider") as TrainerNativeBrakeForceProvider; if (p == null) throw new Exception("Brake provider absent");
            ReadMotionOwner(); var pressures = new float[76]; var velocities = new double[76];
            // The candidate's real four-bogie sum is the native locomotive
            // oracle input. Do not reconstruct it from the displayed maximum
            // gauge, which would erase unequal bogie pressures during release.
            double bogieSum = 0; for (int i = 0; i < 4; i++) bogieSum += Math.Max(0, bogiePressure[i]());
            for (int i = 0; i < 76; i++) { pressures[i] = i == 0 ? (float)(bogieSum * 4) : Float(motionCars[i], 4); velocities[i] = motionKernelUnderTest.GetVehicle(i).VelocityV; }
            byte[] before = CaptureOracleBuffers(); var expected = p.Evaluate(pressures, velocities);
            Check(name + " independent oracle borrow restored", before.SequenceEqual(CaptureOracleBuffers()), true, true);
            var bases = (double[])Field("managedBrakeBase"); var a = (double[])Field("managedBrakeNumerator"); var b = (double[])Field("managedBrakeDenominator");
            double maxError = 0, maxTolerance = 0; bool match = true; double positive = 0;
            for (int i = 0; i < 76; i++) {
                double actual = bases[i] * (Math.Abs(velocities[i]) + a[i]) / (Math.Abs(velocities[i]) + b[i]);
                double tolerance = .1 + Math.Abs(expected[i]) * 1e-5; // stated native f32-order tolerance
                maxError = Math.Max(maxError, Math.Abs(actual - expected[i])); maxTolerance = Math.Max(maxTolerance, tolerance);
                match &= Math.Abs(actual - expected[i]) <= tolerance; positive = Math.Max(positive, expected[i]);
            }
            Check(name + " native pressure law retained over signed velocity", match, "0.1N+10ppm", new { max_error_N = maxError, maximum_tolerance_N = maxTolerance });
            if (requirePositive) Check(name + " nontrivial real brake capacity", positive > 100, ">100N", positive);
        }

        public void MeasureManagedMotion() {
            beforeNativePneumatic = ManagedBudget;
            Start(); ReadMotionOwner();
            Check("No declared speed preparation before genuine start", preparedInitializations == 0, 0, preparedInitializations);
            motionTeachingSource = (TrainerMarkupSignalSource)Field("markupSource");
            foreach (var signal in motionTeachingSource.Signals) motionTeachingSource.SetTeachingSignalState(signal.Id, 0);
            Call("RouteInputCycle"); GenuineNativeStart();
            Check("Genuine origin uses actual force and reaches3kmh", originForceSeen && Capture().head_v_kmh >= 2 && preparedInitializations == 0, "genuine3 then coast", Capture());
            VerifyMotionPublication("genuine_coast"); VerifyNextVelocityAndOracle("genuine_next_frame");
            double e = motionKernelUnderTest.TotalEnergyJ, w = motionKernelUnderTest.CumulativeExternalWorkJ;
            phase = "genuine_coast_mechanical_balance"; Warm(10);
            Check("Coast cannot create energy beyond actual route work", motionKernelUnderTest.TotalEnergyJ - e <= motionKernelUnderTest.CumulativeExternalWorkJ - w + .1, "dE<=route work", motionKernelUnderTest.TotalEnergyJ - e);
            managedCoastEvidence = MotionSnapshot("genuine_coast_after10frames");
            Call("ReadModernTractionCommand", "moderntraction", "1");
            Check("Unaccepted modern traction cannot be enabled", !(bool)Field("modernTractionEnabled"), false, Field("modernTractionEnabled"));
            float nativeForce = Float(mem["TResTyga"], 0); Call("ApplyModernTractionPhysics");
            Check("Disabled modern hook cannot overwrite native force", Float(mem["TResTyga"], 0) == nativeForce, nativeForce, Float(mem["TResTyga"], 0));

            double ur = Float(mem["TLocParam"], 9); Handle(0x3b0, 5); phase = "genuine_physical_KMV_service";
            int serviceStart = cycle; while (ur - Float(mem["TLocParam"], 9) < .5 && cycle - serviceStart < 180) { ManagedBudget(); Step(); }
            Check("Physical KMV reduces real UR", ur - Float(mem["TLocParam"], 9) >= .5, ">=0.5kgf/cm2", ur - Float(mem["TLocParam"], 9));
            Handle(0x3b0, 4); phase = "genuine_IV_hold_real_pneumatics";
            for (int i = 0; i < 100; i++) { ManagedBudget(); Step(); }
            Check("Real service charges locomotive and wagon BC", Float(mem["TLocParam"], 13) > .01 && Float(motionCars[1], 4) > .01, "BC>0.01", new[] { Float(mem["TLocParam"], 13), Float(motionCars[1], 4), Float(motionCars[75], 4) });
            VerifyMotionPublication("genuine_service"); VerifyNativeBrakeCapacity("genuine_service", true); VerifyNextVelocityAndOracle("service_oracle_and_velocity");
            managedServiceEvidence = MotionSnapshot("genuine_service_hold");
            double bcBeforeRelease = Float(motionCars[1], 4); Handle(0x3b0, 2); phase = "genuine_physical_II_release";
            for (int i = 0; i < 150; i++) { ManagedBudget(); Step(); }
            Check("Physical II reduces real wagon BC", Float(motionCars[1], 4) < bcBeforeRelease, "BC falls", new[] { (double)bcBeforeRelease, Float(motionCars[1], 4) });
            VerifyMotionPublication("genuine_release"); VerifyNativeBrakeCapacity("genuine_release", false);
            managedReleaseEvidence = MotionSnapshot("genuine_release_after150frames");
            Check("Genuine movement/braking phases had no speed jump", preparedInitializations == 0 && managedPreparedStates == 0, 0, preparedInitializations);

            // These resets are deliberately separate prepared ABI components.
            phase = "prepared_station_reset_component";
            int station = route.stations.Count > 1 ? 1 : 0; if (route.stations.Count == 0) throw new Exception("No route station for reset component");
            Call("StartAtRouteStation", station); ((Timer)Field("timer")).Stop(); managedPreparedStates++;
            float stationFront = (float)route.X(route.stations[station].km);
            Check("Station reset publishes front and every zero velocity", Math.Abs(motionKernelUnderTest.GetVehicleFrontPosition(0) - stationFront) < 1e-6 && Enumerable.Range(0, 76).All(i => motionKernelUnderTest.GetVehicle(i).VelocityV == 0), stationFront, motionKernelUnderTest.GetVehicleFrontPosition(0));
            phase = "prepared_positive_UI_speed_component"; Call("SetInitialSpeed", 3f); managedPreparedStates++;
            Check("UI speed reset converts kmh to mps", Math.Abs(motionKernelUnderTest.GetVehicle(0).VelocityV - 3 / 3.6) < 1e-7 && Float(mem["TInputParamPult"], 1) == 3, "3kmh/3.6", motionKernelUnderTest.GetVehicle(0).VelocityV);
            Call("MotionCycle"); VerifyMotionPublication("prepared_positive_motion");
            phase = "prepared_reverse_delegate_component"; ((Delegate)Field("stateTrain")).DynamicInvoke(stationFront, -3f, 2, 76); managedPreparedStates++;
            Check("Signed reset delegate preserves reverse velocity", motionKernelUnderTest.GetVehicle(0).VelocityV < 0 && Math.Abs(motionKernelUnderTest.GetVehicle(0).VelocityV + 3 / 3.6) < 1e-7, "-3kmh/3.6", motionKernelUnderTest.GetVehicle(0).VelocityV);
            e = motionKernelUnderTest.TotalEnergyJ; w = motionKernelUnderTest.CumulativeExternalWorkJ;
            Call("MotionCycle");
            Check("Reverse component remains reverse and passive under actual pressures", motionKernelUnderTest.GetVehicle(0).VelocityV < 0 && motionKernelUnderTest.TotalEnergyJ - e <= motionKernelUnderTest.CumulativeExternalWorkJ - w + .1, true, motionKernelUnderTest.GetVehicle(0).VelocityV);
            VerifyMotionPublication("prepared_reverse_motion"); VerifyNativeBrakeCapacity("prepared_reverse_oracle", false);
            ((Delegate)Field("stateTrain")).DynamicInvoke(stationFront, 0f, 2, 76);
        }

        public void CloseManagedMotionProbe() {
            beforeNativePneumatic = afterNativeMotion = null;
            if (motionTeachingSource != null) { foreach (var signal in motionTeachingSource.Signals) motionTeachingSource.ClearTeachingSignalState(signal.Id); managedTeachingRestored = true; }
            if (host != null && Field("managedMotion") != null) Call("CloseManagedMotion");
            managedOwnerClosed = host == null || Field("managedMotion") == null;
            Check("Managed motion/oracle disposed before native air close", managedOwnerClosed && !TrainerNativeBrakeForceProvider.HasRetainedOwners, true, managedOwnerClosed);
        }
        public object ManagedMotionResult() {
            return new { passed = error == null && restored && closed && managedOwnerClosed, error, cycles = cycle,
                genuine_origin = originEvidence, genuine_coast = managedCoastEvidence, genuine_service = managedServiceEvidence, genuine_release = managedReleaseEvidence,
                prepared_components = managedPreparedStates, prepared_initializations_during_genuine = preparedInitializations,
                managed_owner_closed_before_air = managedOwnerClosed, teaching_inputs_restored = managedTeachingRestored,
                native_inputs_restored = restored, native_closed = closed, rows = managedMotionRows,
                limitation = "Prepared reverse is a signed reset/actual-pressure mechanical component. It is not a genuine reverse driving journey or cab EDT acceptance. Native brake speed dependence is retained by pure per-substep provider; pressures are sampled from the actual native outer pneumatic cycle." };
        }
    }
}
