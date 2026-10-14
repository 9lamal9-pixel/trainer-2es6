using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Separate diagnostic executable. This partial extension reuses the genuine
// operator preparation and restoration of the adverse fixture. No production
// AVP admission or calibration artifact is changed by this measurement.
public sealed class TrainerNativeBrakeBoundProbe
{
    [STAThread]
    public static int Main(string[] args)
    {
        return TrainerAutovedenieAdverseBrakeProbe.RunNativeBrakeBound(args);
    }
}

public sealed partial class TrainerAutovedenieAdverseBrakeProbe
{
    const double BoundPreparedSpeed = 80, BoundSpeedGuard = 90, BoundPressureFloor = 1.0;
    const int BoundSteadyFrames = 30;
    const double BoundPressureSpread = .05, BoundMinimumIvSeconds = 6;
    const double BoundNativeMass = 620, BoundWorstGrade = .008, BoundGravity = 9.81;

    public static int RunNativeBrakeBound(string[] args)
    {
        Application.EnableVisualStyles();
        thread = GetCurrentThreadId();
        Watch.Restart(); Checks.Clear(); Cases.Clear(); failure = null;
        int exit = 0;
        Fixture fixture = null;
        try
        {
            if (args != null && args.Length != 0 && (args.Length != 1 || Double.Parse(args[0], CultureInfo.InvariantCulture) != BoundPreparedSpeed))
                throw new Exception("This diagnostic has one declared prepared 80km/h case only.");
            Check("Bound probe x86 ABI", IntPtr.Size == 4, 4, IntPtr.Size);
            fixture = new Fixture { speed = BoundPreparedSpeed, reduction = .7 };
            fixture.MeasureBoundCase();
        }
        catch (Exception ex)
        {
            exit = 1; failure = ex.ToString();
            if (fixture != null) fixture.error = ex.ToString();
            Console.WriteLine(ex);
        }
        finally
        {
            if (fixture != null)
            {
                fixture.Close();
                Cases.Add(fixture.BoundResult());
                if (!fixture.BoundPassed)
                {
                    exit = 1;
                    failure = (failure ?? "") + "\nBOUND: " + (fixture.error ?? "Measurement or restoration incomplete.");
                }
            }
        }
        try { SaveNativeBrakeBound(exit == 0); }
        catch (Exception ex) { Console.WriteLine(ex); exit = 1; }
        Console.WriteLine(exit == 0
            ? "PASS declared prepared80 native first-step force and observed pressure-floor delay; full AVP/whole-route stop not accepted"
            : "FAIL native brake bound; inspect actual predictor/pressure/safety/restoration evidence");
        return exit;
    }

    static void SaveNativeBrakeBound(bool passed)
    {
        double analytical = BoundWagonMagnitude(BoundPressureFloor, BoundSpeedGuard / 3.6);
        double numerical = Math.Max(0, analytical - BoundForceTolerance(analytical));
        File.WriteAllText(TrainerSession.ResultPath("Autovedenie_native_brake_bound.json"),
            new JavaScriptSerializer { MaxJsonLength = 16000000 }.Serialize(new
            {
                passed, error = failure, checks = Checks, cases = Cases,
                declared_case = new { head_x_m = StartX, initial_speed_kmh = BoundPreparedSpeed, native_SetInitState_argument_kmh = BoundPreparedSpeed,
                    mass_t = 6200, native_scaled_mass = BoundNativeMass, freight_wagons = 75, motion_elements = 76, air_nodes = 77,
                    all_wagons = "Composite FTk0 / medium R2 / AR1", charge_setpoint_kgf_cm2 = 5.1, first_UR_reduction_kgf_cm2 = .7,
                    predictor_and_corrector_speed_guard_kmh = BoundSpeedGuard, pressure_floor_kgf_cm2 = BoundPressureFloor },
                conditional_formula = new
                {
                    material_table = 0, wagon_geometry = 1, area = 994, lever = (double)5.79f, efficiency = .00085,
                    phi0 = (double).055f, c1 = 20, c2 = 5, c3_m_s = (double)41.7f, c4_m_s = (double)20.85f,
                    native_law = "K=Single(area*P*lever*0.00085); |B|=phi0*K*(K+20)/(K+5)*(|predictorV|+41.7)/(|predictorV|+20.85)",
                    proof = "Pressure factor derivative=(K^2+10K+100)/(K+5)^2>0 for K>=0; speed factor derivative=(20.85-41.7)/(v+20.85)^2<0 for v>=0",
                    wagon_force_at_floor_and_guard_native = analytical,
                    wagon_force_with_explicit_roundoff_allowance_native = numerical,
                    wagon_only_brake_bound_m_s2 = 75 * analytical / BoundNativeMass,
                    wagon_only_numerically_guarded_bound_m_s2 = 75 * numerical / BoundNativeMass,
                    raw_formula_net_at_worst_minus8_m_s2 = 75 * analytical / BoundNativeMass - BoundGravity * BoundWorstGrade,
                    numerically_guarded_net_at_worst_minus8_m_s2 = 75 * numerical / BoundNativeMass - BoundGravity * BoundWorstGrade,
                    credits = "No locomotive brake, grade benefit, curvature, rolling resistance, coupler force or sanding credit",
                    applicability = "Conditional on all75 actual wagon inputs remaining >=1.0, all predictor speeds <=90km/h, unchanged native FTk0 tables/masses and no traction/protection interruption. It is not a moving apply-delay assumption."
                },
                calibrated_configuration_written = false, calibration_approved = false, full_avp_accepted = false,
                acceleration_to80_accepted = false, whole_train_stop_from80_accepted = false, whole_route_acceptance = false,
                scope = "One explicit initial 80km/h native state after a genuine >=3km/h native traction origin. Real physical KM V->IV, observed native pressure-floor delay and 30 settled frames; original full333033m profile/177 curves retained. All181 signal states explicitly changed to teaching green through managed source API and restored. Explicit software SAUT OFF and scripted fixture RBS acknowledgement. Force observations at each original FSTP include all native inner passes; no writes to native forces/speeds/TC/protection outputs and no coast subtraction. Initial full-consist -8 condition is checked; later geography is reported, not constrained to that short segment."
            }));
    }

    // Match the native rounded K slot, then use the captured/native speed factor.
    static double BoundPressureLaw(double pressure)
    {
        double k = (double)(float)(994d * pressure * (double)5.79f * .00085);
        return (double).055f * k * (k + 20) / (k + 5);
    }
    static double BoundFactor(double velocityMps)
    {
        double v = Math.Abs(velocityMps);
        return (v + (double)41.7f) / (v + (double)20.85f);
    }
    static double BoundWagonMagnitude(double pressure, double velocityMps)
    {
        return BoundPressureLaw(pressure) * BoundFactor(velocityMps);
    }
    static double BoundForceTolerance(double magnitude) { return Math.Max(.000001, Math.Abs(magnitude) * .000002); }

    public sealed class BrakeBoundChargeSample
    {
        public string label, phase;
        public int cycle;
        public double configured_setpoint, ur, pm;
        public double[] tc, tm, bogies;
    }
    sealed class BoundForceFrame
    {
        public Frame physical;
        public TrainerNativeBrakeObserver.Snapshot forces;
        public double native_end_seconds, pressure_floor, minimum_wagon_factor, minimum_wagon_force_magnitude;
        public int effective_km, effective_kvt;
    }

    sealed partial class Fixture
    {
        TrainerNativeBrakeObserver boundObserver;
        TrainerMarkupSignalSource boundSource;
        Action boundOldBefore, boundOldAfter;
        IntPtr boundPoezd;
        byte[] boundStoreOriginal, boundPerPadOriginal;
        string boundRouteFingerprint, boundSourceHash;
        long boundSourceGeneration;
        bool boundCaptureActive, boundMeasured, boundObserverRestored, boundSignalsRestored, boundDelayObserved;
        int boundExpectedKm = 5, boundFrameCount, boundQualifiedCount;
        double boundPendingStartSeconds, boundFirstVSeconds = -1, boundFirstIvSeconds = -1;
        double boundFirstFloorSeconds = -1, boundContinuousFloorSeconds = -1, boundFinalSeconds = -1, boundMaximumSpeedKmh;
        double boundMaximumFactorError, boundMaximumForceError, boundMinimumObservedMagnitude = Double.MaxValue;
        double boundPendingPhysicalKm, boundPressureAtV, boundActualReduction;
        BrakeBoundChargeSample boundColdCharge, boundWarmCharge;
        BoundForceFrame boundInitialForces, boundFirstFloor, boundLast;
        readonly Queue<BoundForceFrame> boundSteady = new Queue<BoundForceFrame>();
        readonly List<object> boundSummaries = new List<object>();
        object boundStableEvidence;

        public bool BoundPassed { get { return error == null && boundMeasured && boundDelayObserved && boundObserverRestored && boundSignalsRestored && restored && closed; } }

        public void MeasureBoundCase()
        {
            boundOldBefore = beforeNativePneumatic; boundOldAfter = afterNativeMotion;
            if (boundOldBefore != null || boundOldAfter != null) throw new Exception("Pre-existing diagnostic callbacks in fresh bound fixture.");
            beforeNativePneumatic = BoundBeforePneumatic;
            afterNativeMotion = BoundAfterMotion;
            try
            {
                Start();
                Check("Cold native charge observed before first prepared pneumatic step", boundColdCharge != null, true, boundColdCharge);
                Check("Cold configured charge5.1 and released physical TC", Math.Abs(boundColdCharge.configured_setpoint - 5.1) < .000001
                    && boundColdCharge.tc.All(p => p <= .1) && boundColdCharge.bogies.All(p => p <= .1), "setpoint5.1/allTC<=.1", boundColdCharge);
                boundSource = (TrainerMarkupSignalSource)Field("markupSource");
                Check("Full native course geometry retained", route.native_axis && route.profile.Count == 224 && route.curves.Count == 177
                    && Math.Abs(route.Length - 333033) < .001 && Math.Abs(boundSource.NativeCourseEnd - 333033) < .001,
                    "224profiles/177curves/full333033m", new { route.Length, profile_count = route.profile.Count, curve_count = route.curves.Count, source_end = boundSource.NativeCourseEnd });
                Check("Full source signal count", boundSource.Signals.Length == 181, 181, boundSource.Signals.Length);
                Check("Original full-profile gradient range", route.profile.Min(p => p.grade_permille) == -8 && route.profile.Max(p => p.grade_permille) == 8, "-8..+8", new { min = route.profile.Min(p => p.grade_permille), max = route.profile.Max(p => p.grade_permille) });
                boundRouteFingerprint = BoundRouteHash(); boundSourceHash = boundSource.SourceSha256;
                foreach (var signal in boundSource.Signals) boundSource.SetTeachingSignalState(signal.Id, 0);
                Check("Explicit teaching green for all signals", boundSource.Signals.All(s => s.State == 0 && s.TeachingOverride), "181 explicit SlGreen overrides; saved send-code flags retained", boundSource.Signals.Length);
                Call("RouteInputCycle");
                GenuineNativeStart();
                boundWarmCharge = BoundCharge("warm_released_after_genuine_traction_origin");
                // Rules83 table1 note2: charging pressure is measured at locomotive TM.
                // Section9.3.3 PDF67 permits head/tail spread0.3 for300 wagon axes;
                // extending that bound to each of77 nodes is diagnostic.
                Check("Warm locomotive TM5.0..5.2 and all-node head spread<=.3 with released brakes", Math.Abs(boundWarmCharge.configured_setpoint - 5.1) < .000001
                    && boundWarmCharge.ur >= 5.0 && boundWarmCharge.ur <= 5.2 && boundWarmCharge.tm[0] >= 5.0 && boundWarmCharge.tm[0] <= 5.2 && boundWarmCharge.tm.All(p => Math.Abs(p-boundWarmCharge.tm[0])<=.3)
                    && boundWarmCharge.tc.All(p => p <= .1) && boundWarmCharge.bogies.All(p => p <= .1), "locTM/UR5.0..5.2;headspread<=.3;allTC<=.1", boundWarmCharge);
                boundPoezd = GetModuleHandle("POEZD.DLL");
                Check("Per-pad observer attached after warm hook installation", (bool)Field("perPadConnected") && (bool)Field("locomotiveGeometryConnected"), true, Field("perPadConnected"));
                var modulePath = new StringBuilder(32768);
                if (boundPoezd == IntPtr.Zero || GetModuleFileName(boundPoezd, modulePath, modulePath.Capacity) == 0) throw new Exception("Loaded bound POEZD path absent.");
                boundStoreOriginal = BoundBytes(IntPtr.Add(boundPoezd, TrainerNativeBrakeObserver.StoreRva), 7);
                boundPerPadOriginal = BoundBytes(IntPtr.Add(boundPoezd, 0x1ebb), 6);
                boundObserver = TrainerNativeBrakeObserver.Attach(boundPoezd, modulePath.ToString());
                BoundCheckModel();
                phase = "declared_prepared80_before_first_measured_brake";
                ((Delegate)Field("stateTrain")).DynamicInvoke((float)StartX, (float)BoundPreparedSpeed, 2, 76);
                preparedInitializations++;
                // The single explicit SetInitState argument is km/h. No ongoing state assignments follow.
                boundObserver.ResetFrame(); Call("MotionCycle");
                var initialSnapshot = boundObserver.ReadSnapshot();
                SetField("routeFinished", false); SetField("klubBasisReady", false);
                boundSource.ResetPosition(StartX); Call("RouteInputCycle");
                boundSourceGeneration = boundSource.Generation;
                var initial = Capture(); initialEvidence = initial; rows.Add(initial);
                boundInitialForces = BoundValidate(initialSnapshot, initial, -1, 2, 2);
                Check("One declared80 initialization", preparedInitializations == 1, 1, preparedInitializations);
                Check("All76 native initial velocities are80km/h", initial.vehicles.All(v => Math.Abs(v.v_m_s - BoundPreparedSpeed / 3.6) < .02), BoundPreparedSpeed / 3.6, initial.vehicles.Select(v => v.v_m_s).ToArray());
                Check("First native80 publication has actual forward position", initial.head_x_m > StartX && initial.head_x_m < StartX + 4, "57900..57904", initial.head_x_m);
                Check("Only initial full consist required in original-8 interval", initial.fully_minus8, true, initial);
                Check("All automatic and independent brakes released at declared80", initial.fully_released, true, initial);
                Check("Actual native80 speed sensor published", Math.Abs(Float(mem["TInputParamPult"], 1) - Math.Abs(initial.head_v_kmh)) < .001, initial.head_v_kmh, Float(mem["TInputParamPult"], 1));
                measured = true;
                // Old adverse fixture's short full-minus8 window is not a force-array constraint.
                measurementComplete = true;
                boundCaptureActive = true;
                boundPressureAtV = initial.ur_bar;
                phase = "native80_physical_first_KMV"; boundExpectedKm = 5; Handle(0x3b0, 5);
                for (int n = 0; n < 600 && boundPressureAtV - Float(mem["TLocParam"], 9) < .7; n++) Step();
                boundActualReduction = boundPressureAtV - Float(mem["TLocParam"], 9);
                Check("Native80 actual first UR reduction achieved", boundFirstVSeconds >= 0 && boundActualReduction >= .7, "actual effectiveKMV and deltaUR>=.7", new { boundFirstVSeconds, boundActualReduction });
                phase = "native80_physical_KMIV_waiting_for_measured_wagon_floor";
                boundExpectedKm = 4; Handle(0x3b0, 4);
                bool settled = false;
                for (int n = 0; n < 1200; n++)
                {
                    Step();
                    if (BoundStable()) { settled = true; break; }
                }
                Check("All75 native TC floor and30 stable IV frames observed", settled, ">=1.0/all75/30frames/range<=.05/IV>=6s", new { stable_frames = boundSteady.Count, first_floor_seconds = boundFirstFloorSeconds, last = BoundSummary(boundLast) });
                var final = Capture(); rows.Add(final);
                Check("Bound observation ends while actual train is still moving", final.maximum_abs_v_m_s > Standstill, "moving; no80-stop claim", final.maximum_abs_v_m_s);
                Check("Original route geometry fingerprint unchanged", BoundRouteHash() == boundRouteFingerprint, boundRouteFingerprint, BoundRouteHash());
                Check("Original source artifact hash unchanged", boundSource.SourceSha256 == boundSourceHash, boundSourceHash, boundSource.SourceSha256);
                Check("Positive native force-only bound and net at worst-8", 75 * BoundWagonMagnitude(1, 25) / BoundNativeMass - BoundGravity * BoundWorstGrade > 0, ">0", 75 * BoundWagonMagnitude(1, 25) / BoundNativeMass - BoundGravity * BoundWorstGrade);
                Check("Observed native pressure-floor delay", boundFirstFloorSeconds >= boundFirstVSeconds && boundFirstVSeconds >= 0, "measured timestamps; no guessed35s admission", new { first_effective_v = boundFirstVSeconds, first_all75_floor = boundFirstFloorSeconds, delay_seconds = boundFirstFloorSeconds - boundFirstVSeconds });
                Check("Predictor and corrector max speed guard", boundMaximumSpeedKmh <= BoundSpeedGuard, "<=90km/h", boundMaximumSpeedKmh);
                Check("Native force frames captured", boundFrameCount > 0 && boundQualifiedCount >= BoundSteadyFrames, ">0 complete76/all75proofs; >=30 floor-qualified", new { boundFrameCount, boundQualifiedCount, boundMaximumFactorError, boundMaximumForceError });
                boundDelayObserved = true; boundMeasured = true;
            }
            finally
            {
                boundCaptureActive = false;
                beforeNativePneumatic = boundOldBefore; afterNativeMotion = boundOldAfter;
                try
                {
                    if (boundObserver != null)
                    {
                        boundObserver.Dispose();
                        Check("Original force store restored before native close", BoundBytes(IntPtr.Add(boundPoezd, TrainerNativeBrakeObserver.StoreRva), 7).SequenceEqual(boundStoreOriginal), BitConverter.ToString(boundStoreOriginal), BitConverter.ToString(BoundBytes(IntPtr.Add(boundPoezd, TrainerNativeBrakeObserver.StoreRva), 7)));
                        Check("Existing per-pad hook preserved", BoundBytes(IntPtr.Add(boundPoezd, 0x1ebb), 6).SequenceEqual(boundPerPadOriginal), BitConverter.ToString(boundPerPadOriginal), BitConverter.ToString(BoundBytes(IntPtr.Add(boundPoezd, 0x1ebb), 6)));
                        MemoryInfo protection;
                        bool rx = VirtualQuery(IntPtr.Add(boundPoezd, TrainerNativeBrakeObserver.StoreRva), out protection, (UIntPtr)Marshal.SizeOf(typeof(MemoryInfo))) != UIntPtr.Zero && protection.Protect == 0x20;
                        Check("Restored native force code isRX", rx, 0x20, protection.Protect);
                        boundObserverRestored = true; boundObserver = null;
                    }
                }
                finally
                {
                    if (boundSource != null)
                    {
                        boundSource.RestorePersistedSignalStates();
                        bool same = boundSource.Signals.All(s => !s.TeachingOverride && s.State == s.OriginalState);
                        Check("Teaching green signal states restored", same, "saved original states; native protection not reset", same);
                        boundSignalsRestored = true;
                    }
                }
            }
        }

        void BoundBeforePneumatic()
        {
            if (boundColdCharge == null) boundColdCharge = BoundCharge("first_prepared_pneumatic_step_before_native_apply");
            if (!boundCaptureActive) return;
            BoundCheckModel();
            var loc = mem["TLocParam"];
            boundPendingPhysicalKm = Marshal.ReadByte(loc, 0x7d);
            boundPendingStartSeconds = Convert.ToInt32(Field("ticks")) * Dt;
            if (boundPendingPhysicalKm != boundExpectedKm || Marshal.ReadByte(loc, 0x7e) != 2)
                throw new Exception("Physical KM/KVT changed during bound measurement.");
            // No POEZD force producer is called in PneumaticCycle. This reset precedes
            // exactly the one following synchronous MotionCycle in Fixture.Step.
            boundObserver.ResetFrame();
        }

        void BoundAfterMotion()
        {
            if (!boundCaptureActive) return;
            var snapshot = boundObserver.ReadSnapshot();
            bool known = Convert.ToBoolean(Field("observedPneumaticCommandsKnown"));
            int effectiveKm = Convert.ToInt32(Field("observedPneumaticKm")), effectiveKvt = Convert.ToInt32(Field("observedPneumaticKvt"));
            double appliedTime = Convert.ToDouble(Field("observedPneumaticCommandSeconds"));
            if (!known || !Finite(appliedTime) || Math.Abs(appliedTime - boundPendingStartSeconds) > .000001
                || effectiveKm != boundPendingPhysicalKm || effectiveKm != boundExpectedKm || effectiveKvt != 2)
                throw new Exception("Native pneumatic consumer did not confirm the observed physical KM/KVT source.");
            if (effectiveKm == 5 && boundFirstVSeconds < 0) boundFirstVSeconds = boundPendingStartSeconds;
            if (effectiveKm == 4 && boundFirstIvSeconds < 0) boundFirstIvSeconds = boundPendingStartSeconds;
            var f = Capture(); f.cycle = cycle + 1; f.seconds = appliedTime + Dt;
            boundLast = BoundValidate(snapshot, f, appliedTime + Dt, effectiveKm, effectiveKvt);
            boundFinalSeconds = appliedTime + Dt;
            bool floor = boundLast.pressure_floor >= BoundPressureFloor;
            if (floor)
            {
                if (boundFirstFloorSeconds < 0) { boundFirstFloorSeconds = appliedTime + Dt; boundFirstFloor = boundLast; }
                if (boundContinuousFloorSeconds < 0) boundContinuousFloorSeconds = appliedTime + Dt;
                boundQualifiedCount++;
                double requirement = BoundWagonMagnitude(BoundPressureFloor, BoundSpeedGuard / 3.6);
                double safeRequirement = requirement - BoundForceTolerance(requirement);
                foreach (var v in snapshot.Vehicles.Skip(1))
                    if (-v.MaximumBrakeForce < safeRequirement) throw new Exception("Actual native wagon force fell below the pressure/speed conditional bound at " + v.Index + ".");
            }
            else boundContinuousFloorSeconds = -1;
            if (floor && effectiveKm == 4)
            {
                boundSteady.Enqueue(boundLast);
                while (boundSteady.Count > BoundSteadyFrames) boundSteady.Dequeue();
            }
            else boundSteady.Clear();
            if (boundFrameCount % 10 == 0 || ReferenceEquals(boundFirstFloor, boundLast)) boundSummaries.Add(BoundSummary(boundLast));
        }

        BoundForceFrame BoundValidate(TrainerNativeBrakeObserver.Snapshot snapshot, Frame physical, double endSeconds, int effectiveKm, int effectiveKvt)
        {
            if (!snapshot.Complete || snapshot.ErrorFlags != 0 || snapshot.NativeForcePassCount <= 0 || snapshot.CaptureCount != 76 * snapshot.NativeForcePassCount
                || Math.Abs(snapshot.NativeTotalMass - BoundNativeMass) > .000001)
                throw new Exception("Native force observer incomplete/invalid76-frame: " + snapshot.ErrorFlags + "/" + snapshot.CaptureCount + "/" + snapshot.NativeForcePassCount);
            if (physical.epk_trip != 0 || physical.kon_trip != 0 || Math.Abs(physical.force_native) > .001 || Convert.ToInt32(Field("electricalFaults")) != 0)
                throw new Exception("Protection/electrical/traction interruption contaminated native brake bound.");
            var privateSaut = mem["StandaloneSautOutput"]; var loc = mem["TLocParam"];
            if (Marshal.ReadByte(privateSaut, 0xe) != 0 || Marshal.ReadByte(privateSaut, 0xf) != 0
                || (Marshal.ReadByte(loc, 0x89) != 0 && (Marshal.ReadByte(loc, 0x8a) != 0 || Marshal.ReadByte(loc, 0x8b) != 0)))
                throw new Exception("SAUT protection interrupted explicitly declared software-OFF bound case.");
            double maximum = Math.Max(snapshot.MaximumAbsPredictorVelocity, physical.maximum_abs_v_m_s) * 3.6;
            boundMaximumSpeedKmh = Math.Max(boundMaximumSpeedKmh, maximum);
            if (!Finite(maximum) || maximum > BoundSpeedGuard) throw new Exception("Actual predictor/corrector speed exceeded the90km/h calibration admission guard.");
            var vehicles = snapshot.Vehicles;
            if (vehicles.Length != 76 || vehicles[0].MaterialIndex != 1 || vehicles[0].GeometryIndex != 2 || Math.Abs(vehicles[0].NativeMass - 20) > .000001)
                throw new Exception("Separate locomotive model identity changed.");
            double floor = Double.MaxValue, minimumFactor = Double.MaxValue, minimumForce = Double.MaxValue;
            for (int n = 1; n < 76; n++)
            {
                var v = vehicles[n];
                if (v.Index != n || v.LastElementIndex != n || v.LastGroupIndex != n || v.MaterialIndex != 0 || v.GeometryIndex != 1
                    || Math.Abs(v.NativeMass - 8) > .000001 || Math.Abs(v.NativeGroupMass - 8) > .000001
                    || v.CaptureCount != snapshot.NativeForcePassCount || v.ValidCaptureCount != v.CaptureCount)
                    throw new Exception("Wagon force/mass/index mapping changed at " + n + ".");
                double pressure = physical.tc_bar[n + 1];
                if (Math.Abs(v.LastBrakeInput - pressure) > .000002 || Math.Abs(v.MinimumBrakeInput - v.MaximumBrakeInput) > .000002)
                    throw new Exception("Native wagon pressure input is not the actual mapped cylinder pressure at " + n + ".");
                double expectedFactor = (double)(float)BoundFactor(v.LastPredictorVelocity);
                double expectedMinFactor = (double)(float)BoundFactor(v.MaximumAbsPredictorVelocity);
                double factorError = Math.Max(Math.Abs(v.LastSpeedFactor - expectedFactor), Math.Abs(v.MinimumSpeedFactor - expectedMinFactor));
                boundMaximumFactorError = Math.Max(boundMaximumFactorError, factorError);
                if (factorError > .000002 || !Finite(v.LastSpeedFactor) || v.MinimumSpeedFactor <= 0)
                    throw new Exception("Native composite predictor speed factor mismatch at " + n + ".");
                double expected = (double)(float)(BoundPressureLaw(v.LastBrakeInput) * v.LastSpeedFactor);
                double actual = -v.LastBrakeForce, forceError = Math.Abs(actual - expected);
                boundMaximumForceError = Math.Max(boundMaximumForceError, forceError);
                if (forceError > BoundForceTolerance(expected) || actual < 0 || !Finite(actual))
                    throw new Exception("Native composite wagon force formula mismatch at " + n + ": expected=" + expected.ToString("R", CultureInfo.InvariantCulture) + ",actual=" + actual.ToString("R", CultureInfo.InvariantCulture));
                double extremaRequirement = BoundPressureLaw(v.MinimumBrakeInput) * v.MinimumSpeedFactor;
                if (-v.MaximumBrakeForce + BoundForceTolerance(extremaRequirement) < extremaRequirement)
                    throw new Exception("Native inner-step minimum force violates observed pressure/factor envelope at " + n + ".");
                floor = Math.Min(floor, Math.Min(pressure, v.MinimumBrakeInput));
                minimumFactor = Math.Min(minimumFactor, v.MinimumSpeedFactor);
                minimumForce = Math.Min(minimumForce, -v.MaximumBrakeForce);
            }
            boundFrameCount++;
            boundMinimumObservedMagnitude = Math.Min(boundMinimumObservedMagnitude, minimumForce);
            return new BoundForceFrame { physical = physical, forces = snapshot, native_end_seconds = endSeconds, effective_km = effectiveKm,
                effective_kvt = effectiveKvt, pressure_floor = floor, minimum_wagon_factor = minimumFactor, minimum_wagon_force_magnitude = minimumForce };
        }

        bool BoundStable()
        {
            if (boundSteady.Count != BoundSteadyFrames || boundFirstIvSeconds < 0 || boundFinalSeconds - boundFirstIvSeconds < BoundMinimumIvSeconds) return false;
            var frames = boundSteady.ToArray();
            var minima = Enumerable.Repeat(Double.MaxValue, 75).ToArray();
            var maxima = new double[75]; var forceMinima = Enumerable.Repeat(Double.MaxValue, 75).ToArray();
            var factorMinima = Enumerable.Repeat(Double.MaxValue, 75).ToArray(); var maximumSpeeds = new double[75];
            foreach (var f in frames)
            {
                foreach (var v in f.forces.Vehicles.Skip(1))
                {
                    int n = v.Index - 1;
                    minima[n] = Math.Min(minima[n], v.MinimumBrakeInput); maxima[n] = Math.Max(maxima[n], v.MaximumBrakeInput);
                    forceMinima[n] = Math.Min(forceMinima[n], -v.MaximumBrakeForce); factorMinima[n] = Math.Min(factorMinima[n], v.MinimumSpeedFactor);
                    maximumSpeeds[n] = Math.Max(maximumSpeeds[n], v.MaximumAbsPredictorVelocity * 3.6);
                }
            }
            double spread = maxima.Zip(minima, (a, b) => a - b).Max();
            if (minima.Min() < BoundPressureFloor || spread > BoundPressureSpread) return false;
            double formula = BoundWagonMagnitude(1, 25), safe = formula - BoundForceTolerance(formula);
            if (forceMinima.Any(f => f < safe)) throw new Exception("Stable native wagon force below conditional bound.");
            boundStableEvidence = new
            {
                start = BoundSummary(frames[0]), end = BoundSummary(frames[frames.Length - 1]), frames = frames.Length,
                simulated_seconds = frames.Length * Dt, KMIV_seconds = boundFinalSeconds - boundFirstIvSeconds,
                all75_native_pressure_min = minima, all75_native_pressure_max = maxima, maximum_per_wagon_pressure_spread = spread,
                all75_native_brake_magnitude_min = forceMinima, all75_native_speed_factor_min = factorMinima, all75_max_predictor_speed_kmh = maximumSpeeds,
                actual_wagon_only_force_bound_m_s2 = forceMinima.Sum() / BoundNativeMass,
                raw_formula_wagon_only_bound_m_s2 = 75 * formula / BoundNativeMass,
                numerical_wagon_only_bound_m_s2 = 75 * safe / BoundNativeMass,
                locomotive_force_credited = false, grade_curve_rolling_or_coast_credit = false,
                entire_original_minus8_required = false, original_profile_fingerprint = boundRouteFingerprint,
                full_force_snapshots_start = frames[0].forces, full_force_snapshots_end = frames[frames.Length - 1].forces
            };
            return true;
        }

        void BoundCheckModel()
        {
            if (Field("avpController") != null || Convert.ToInt32(Field("electricalFaults")) != 0) throw new Exception("Production controller/electrical fault present in diagnostic bound case.");
            if (((ComboBox)Field("standaloneSautChoice")).SelectedIndex != 3) throw new Exception("Explicit software SAUT OFF changed.");
            if (boundSource != null && boundCaptureActive && (boundSource.Generation != boundSourceGeneration || boundSource.PassedStop))
                throw new Exception("Teaching source changed or latched passed stop during bound measurement.");
            if (Marshal.ReadInt32((IntPtr)Field("trainData")) != 77 || motionCars.Length != 76 || !route.native_axis || route.profile.Count != 224 || route.curves.Count != 177)
                throw new Exception("Full original consist/course model changed.");
            var coeffRva = new[] { 0x151c4, 0x151d0, 0x151e4, 0x151ec, 0x151f4, 0x151fc, 0x15204 };
            var coeff = new[] { 994f, 5.79f, .055f, 20f, 5f, 41.7f, 20.85f };
            for (int n = 0; n < coeff.Length; n++)
                if (Marshal.ReadInt32(boundPoezd, coeffRva[n]) != BitConverter.ToInt32(BitConverter.GetBytes(coeff[n]), 0))
                    throw new Exception("Native composite coefficient table changed at " + coeffRva[n].ToString("X") + ".");
            for (int n = 1; n < 76; n++)
            {
                if (Marshal.ReadIntPtr(airArray, (n + 1) * 8 + 4) != motionCars[n] || Marshal.ReadInt32(boundPoezd, 0x1ad7c + n * 4) != 0
                    || Marshal.ReadInt32(boundPoezd, 0x1ab24 + n * 4) != 1 || Marshal.ReadByte(motionCars[n], 0x18) != 2
                    || Marshal.ReadByte(motionCars[n], 0x19) != 0 || Marshal.ReadByte(motionCars[n], 0x1a) != 1 || Marshal.ReadInt32(motionCars[n], 0x25) != 80)
                    throw new Exception("Wagon composite/R2/AR1/mass/air mapping changed at " + n + ".");
            }
        }
        BrakeBoundChargeSample BoundCharge(string label)
        {
            var loc = mem["TLocParam"];
            var result = new BrakeBoundChargeSample { label = label, phase = phase, cycle = cycle, configured_setpoint = BoundDouble(loc, 0x13),
                ur = Float(loc, 9), pm = Float(loc, 5), tc = new double[77], tm = new double[77], bogies = new double[4] };
            for (int n = 0; n < 77; n++)
            {
                var p = Marshal.ReadIntPtr(airArray, n * 8 + 4); Require(p, 8, false);
                result.tm[n] = Float(p, 0); result.tc[n] = Float(p, 4); Pressure(result.tm[n]); Pressure(result.tc[n]);
            }
            for (int n = 0; n < 4; n++) { result.bogies[n] = bogiePressure[n](); Pressure(result.bogies[n]); }
            Pressure(result.ur); Pressure(result.pm);
            return result;
        }
        string BoundRouteHash()
        {
            var text = new JavaScriptSerializer { MaxJsonLength = 1000000 }.Serialize(new { route.native_axis, route.map_start_km, route.map_end_km, route.profile, route.curves });
            using (var sha = SHA256.Create()) return BitConverter.ToString(sha.ComputeHash(Encoding.UTF8.GetBytes(text))).Replace("-", "");
        }
        static byte[] BoundBytes(IntPtr p, int count) { Require(p, count, false); var bytes = new byte[count]; Marshal.Copy(p, bytes, 0, count); return bytes; }
        static double BoundDouble(IntPtr p, int offset) { var bytes = BoundBytes(IntPtr.Add(p, offset), 8); return BitConverter.ToDouble(bytes, 0); }
        object BoundSummary(BoundForceFrame f)
        {
            if (f == null) return null;
            return new { cycle = f.physical.cycle, f.native_end_seconds, f.effective_km, f.effective_kvt,
                f.pressure_floor, f.minimum_wagon_factor, f.minimum_wagon_force_magnitude,
                predictor_max_speed_kmh = f.forces.MaximumAbsPredictorVelocity * 3.6,
                corrector_max_speed_kmh = f.physical.maximum_abs_v_m_s * 3.6,
                f.physical.head_x_m, f.physical.head_v_kmh, f.physical.ur_bar, f.physical.force_native,
                f.physical.epk_trip, f.physical.kon_trip, f.physical.saut_request,
                f.physical.fully_minus8, head_grade_permille = f.physical.vehicles[0].grade_permille, tail_grade_permille = f.physical.vehicles[75].grade_permille,
                force_passes_per_vehicle = f.forces.NativeForcePassCount, captured_stores = f.forces.CaptureCount, error_flags = f.forces.ErrorFlags };
        }
        public object BoundResult()
        {
            double delay = boundFirstFloorSeconds >= 0 && boundFirstVSeconds >= 0 ? boundFirstFloorSeconds - boundFirstVSeconds : -1;
            return new
            {
                passed = BoundPassed, error, genuine_native_traction_origin = originEvidence, cold_charge = boundColdCharge, warm_charge = boundWarmCharge,
                initial = initialEvidence, initial_force_snapshot = boundInitialForces == null ? null : boundInitialForces.forces,
                prepared_initializations = preparedInitializations, requested_UR_reduction = .7, measured_UR_reduction = boundActualReduction,
                first_effective_KMV_seconds = boundFirstVSeconds >= 0 ? (double?)boundFirstVSeconds : null,
                first_effective_KMIV_seconds = boundFirstIvSeconds >= 0 ? (double?)boundFirstIvSeconds : null,
                first_all75_TC_floor_seconds = boundFirstFloorSeconds >= 0 ? (double?)boundFirstFloorSeconds : null,
                observed_delay_to_all75_floor_seconds = delay >= 0 ? (double?)delay : null,
                hypothesis_delay_below35_seconds = delay >= 0 ? (bool?)(delay < 35) : null,
                continuous_all75_floor_start_seconds = boundContinuousFloorSeconds >= 0 ? (double?)boundContinuousFloorSeconds : null,
                observed_delay_to_continuous_all75_floor_seconds = boundContinuousFloorSeconds >= 0 && boundFirstVSeconds >= 0 ? (double?)(boundContinuousFloorSeconds - boundFirstVSeconds) : null,
                verified_settlement_after_first_KMV_seconds = boundMeasured ? (double?)(boundFinalSeconds - boundFirstVSeconds) : null,
                first_floor = BoundSummary(boundFirstFloor), final = BoundSummary(boundLast), stable_native_force_window = boundStableEvidence,
                maximum_actual_predictor_or_corrector_speed_kmh = boundMaximumSpeedKmh,
                native_complete_frames = boundFrameCount, floor_qualified_frames = boundQualifiedCount,
                maximum_observed_factor_error = boundMaximumFactorError, maximum_observed_force_formula_error = boundMaximumForceError,
                first_original_minus8_exit_cycle = firstGradeExit, original_profile_curve_fingerprint = boundRouteFingerprint, original_markup_artifact_sha256 = boundSourceHash,
                observer_restored_before_native_close = boundObserverRestored, teaching_signal_states_restored = boundSignalsRestored, restored, closed,
                scripted_operator_RBS_count = ackCount, scripted_operator_RBS_events = ackEvents,
                summaries = boundSummaries, startup_and_periodic_physical_rows = rows,
                pressure_floor_is_measured_not_assumed = boundDelayObserved, calibration_approved = false, full_AVP_accepted = false,
                accelerated_to80 = false, whole_consist_stop_from80_accepted = false, complete_route_run_accepted = false
            };
        }
    }
}
