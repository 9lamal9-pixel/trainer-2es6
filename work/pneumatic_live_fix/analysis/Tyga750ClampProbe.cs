using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Diagnostic extension of the existing genuine-start fixture, not a copy of
// its prepared-80 initial-state measurement. Compile with the canonical candidate
// source set, TrainerAutovedenieAdverseBrakeProbe.cs and Tyga750ClampObserver.cs;
// explicitly select /main:Tyga750ClampProbe. No native call has been run by author.
public sealed class Tyga750ClampProbe {
    [STAThread] public static int Main(string[] args) {
        return TrainerAutovedenieAdverseBrakeProbe.RunTyga750Clamp(args);
    }
}

public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
    public static int RunTyga750Clamp(string[] args) {
        Application.EnableVisualStyles(); thread = GetCurrentThreadId();
        Watch.Restart(); Checks.Clear(); Cases.Clear(); failure = null;
        int exit = 1; Fixture fixture = null; double target = 40; int maximumAdditionalFrames = 4000;
        try {
            if (args != null && args.Length > 0) target = Double.Parse(args[0], CultureInfo.InvariantCulture);
            if (args != null && args.Length > 1) maximumAdditionalFrames = Int32.Parse(args[1], CultureInfo.InvariantCulture);
            if (args != null && args.Length > 2 || target < 20 || target > 40 || maximumAdditionalFrames < 1 || maximumAdditionalFrames > 5000)
                throw new ArgumentException("Arguments: real target 20..40 km/h, additional frames 1..5000; base fixture also bounds total time/cycles");
            Check("Tyga diagnostic x86", IntPtr.Size == 4, 4, IntPtr.Size);
            fixture = new Fixture { speed = target, reduction = .7 };
            fixture.MeasureTygaClamp(target, maximumAdditionalFrames);
            exit = fixture.TygaConfirmed ? 0 : 2;
        } catch (Exception error) {
            failure = error.ToString(); if (fixture != null) fixture.error = error.ToString();
            Console.WriteLine(error); exit = 1;
        } finally {
            if (fixture != null) {
                try { fixture.CloseTygaObserver(); }
                catch (Exception error) { fixture.error = (fixture.error ?? "") + "\nOBSERVER RESTORE: " + error; exit = 1; }
                if (fixture.TygaNativeCloseAllowed) fixture.Close();
                else { fixture.error = (fixture.error ?? "") + "\nNative close refused while observer ownership remains retained"; exit = 1; }
                if (fixture.error != null || !fixture.restored || !fixture.closed) exit = 1;
                Cases.Add(fixture.TygaResult());
            }
        }
        try {
            string file = TrainerSession.ResultPath("Trainer_tyga750_clamp_diagnostic.json");
            File.WriteAllText(file, new JavaScriptSerializer { MaxJsonLength = 32000000 }.Serialize(new {
                passed = exit == 0, retained_early_limit_verified = exit == 0,
                status = exit == 0 ? "native_retained_early_limit_verified_before_nonzero_force" : exit == 2 ? "bounded_real_command_sweep_inconclusive" : "diagnostic_failed",
                exit_code = exit, error = failure, checks = Checks, cases = Cases,
                target_speed_kmh = target, maximum_additional_frames = maximumAdditionalFrames,
                evidence_type = "whole native host path with declared diagnostic operator/signal inputs",
                additional_prepared_speed_initializations = 0, output_current_field_torque_force_voltage_assignments = false,
                native_velocity_unit = "m/s; both Param.V Double and Calc.FVCur captured",
                observer_only_writes = "owned RW diagnostic records and four guarded/restored process-memory entry jumps; existing .tfix behavior is chained unchanged",
                native_module_source_sha256 = Tyga750ClampObserver.SourceSha256,
                scope = "Existing real-operator start and full native electrical/pneumatic/motion/route/aux/KLUB cycle. Teaching green and scripted fixture RBS are explicit inputs. No SetInitState prepared-speed jump, no AVP calibration admission, no forced Ia/Iv/M/F/Uset. A non-hit is inconclusive; component fixture is a separate plan B.",
                model_correctness_accepted = false, factory_calibration_accepted = false, whole_route_accepted = false
            }));
        } catch (Exception saveError) {
            Console.WriteLine(saveError); exit = 1;
            // An invalid numeric observation must not suppress the error JSON.
            // The fallback intentionally serializes strings/scalars only.
            try {
                File.WriteAllText(TrainerSession.ResultPath("Trainer_tyga750_clamp_diagnostic.json"), new JavaScriptSerializer().Serialize(new {
                    passed = false, status = "diagnostic_result_serialization_failed", error = failure,
                    serialization_error = saveError.ToString(), model_correctness_accepted = false,
                    fixture_error = fixture == null ? null : fixture.error,
                    native_close_allowed = fixture == null || fixture.TygaNativeCloseAllowed
                }));
            } catch (Exception fallbackError) { Console.WriteLine("Fallback result also failed: " + fallbackError); }
        }
        Console.WriteLine(exit == 0 ? "Diagnostic verified the retained early750 limit before nonzero force; full model conformity is not accepted"
            : exit == 2 ? "INCONCLUSIVE: no qualified clamp in this bounded real command path" : "FAIL: diagnostic or restoration failed");
        return exit;
    }

    sealed partial class Fixture {
        Tyga750ClampObserver tygaObserver;
        IntPtr tygaModule;
        Action tygaOldBefore;
        TrainerMarkupSignalSource tygaSource;
        bool tygaFrameOpen, tygaObserverRestored, tygaSignalsRestored;
        bool tygaConfirmed, tygaSweepFinished;
        int tygaFrames, tygaZeroProducerFrames, tygaInvocations, tygaAffectedInvocations;
        double tygaMaximumSpeed, tygaMaximumRawIa, tygaMinimumObservedField = Double.MaxValue;
        readonly List<object> tygaSamples = new List<object>();
        readonly List<object> tygaInputHistory = new List<object>();
        public bool TygaConfirmed { get { return tygaConfirmed; } }
        public bool TygaNativeCloseAllowed { get { return tygaModule == IntPtr.Zero || !Tyga750ClampObserver.HasRetainedOwner(tygaModule); } }

        public void MeasureTygaClamp(double target, int additionalFrames) {
            tygaOldBefore = beforeNativePneumatic;
            if (tygaOldBefore != null || afterNativeMotion != null) throw new Exception("Fresh fixture already has diagnostic callbacks");
            beforeNativePneumatic = ObserveTygaBeforePneumatic;
            Start();
            tygaSource = (TrainerMarkupSignalSource)Field("markupSource");
            foreach (var signal in tygaSource.Signals) tygaSource.SetTeachingSignalState(signal.Id, 0);
            Check("Tyga fixture explicit teaching green", tygaSource.Signals.All(s => s.State == 0 && s.TeachingOverride), "all source signals teaching green", tygaSource.Signals.Length);
            Call("RouteInputCycle");
            GenuineNativeStart(); // Genuine >=3 km/h; this method then coasts.
            Check("No prepared-speed initialization used", preparedInitializations == 0, 0, preparedInitializations);
            if (!tygaConfirmed && Capture().head_v_kmh < target) {
                var inactive = (TrainerAutovedenieNativeBridge)Field("avpNative");
                if (Field("avpController") != null || inactive != null && inactive.OwnsCommands) throw new Exception("AVP unexpectedly owns drive");
                operatorBridgeOwned = inactive == null;
                operatorBridge = inactive ?? TrainerAutovedenieNativeBridge.Attach(mem["CommOwner"], mem["TPult"], mem["TLocParam"], reason => { throw new Exception("Diagnostic operator cancelled: " + reason); });
                operatorBridge.SetOperatorInputSource(ptr => (IDisposable)typeof(TrainerIntegrationHost).GetMethod("AutovedenieOperatorInputScope", Flags).Invoke(host, new object[] { ptr }));
                operatorBridge.SetAdditionalSafetySource(() => (string)typeof(TrainerIntegrationHost).GetMethod("StandaloneSautSafetyReason", Flags).Invoke(host, null));
                Cab(0x3e0, 0); phase = "bounded_genuine_traction_commands_to_target_or_first_qualified_clamp";
                for (int n = 0; n < additionalFrames && !tygaConfirmed && Capture().head_v_kmh < target; n++) {
                    operatorCommand = n % 10 == 0 ? 1 : 0;
                    if (operatorCommand != 0) tygaInputHistory.Add(new { cycle, command = "PlusOdin", source = "existing guarded diagnostic native operator bridge" });
                    Step(); var actual = Capture();
                    if (operatorBridge.Fault.Length != 0 || operatorBridge.CancellationLatched) throw new Exception("Diagnostic native operator path interrupted: " + operatorBridge.Fault);
                    if (actual.epk_trip != 0 || actual.kon_trip != 0) throw new Exception("Actual protection interrupted bounded acceleration");
                }
            }
            tygaSweepFinished = true;
            // Native selector coast through the same source, not force assignment.
            if (operatorBridge != null) {
                operatorCommand = 5; Step(); operatorBridge.ReleaseCommands();
                if (operatorBridgeOwned) operatorBridge.Dispose(); operatorBridge = null;
            }
            Cab(0x3e0, 1); phase = "diagnostic_native_coast_before_observer_restoration"; Warm(3);
        }

        void ObserveTygaBeforePneumatic() {
            if (tygaObserver == null) {
                tygaModule = GetModuleHandle("Tyga1.dll");
                string expectedPath = Path.GetFullPath(Path.Combine((string)Field("resources"), @"LOCOMOTIVES\1\Tyga1.dll"));
                if (tygaModule == IntPtr.Zero) throw new Exception("Tyga loaded module absent");
                tygaObserver = Tyga750ClampObserver.Attach(tygaModule, expectedPath, mem["TTygaParam"], (TrainerContactVoltagePatch)Field("contactVoltagePatch"));
            } else if (tygaFrameOpen) {
                var observation = tygaObserver.ReadSnapshot(); tygaFrameOpen = false; tygaFrames++;
                if (observation.ErrorFlags != 0 || observation.PendingIndex != 0 || observation.BeforeCalls != observation.AfterCalls ||
                    observation.EarlyErrorFlags != 0 || observation.EarlyPendingIndex != 0 || !observation.EarlyComplete)
                    { tygaSamples.Add(new { cycle, phase, incomplete_observation = observation }); throw new Exception("Tyga observer incomplete pairing/error " + observation.ErrorFlags); }
                if (observation.BeforeCalls == 0) tygaZeroProducerFrames++;
                else if (!observation.Complete) throw new Exception("Tyga observer declared incomplete frame");
                foreach (var early in observation.EarlyRecords) for (int i = 0; i < 2; i++) {
                    float rawIa = Tyga750ClampObserver.Single(early.IaBefore[i]);
                    if (!Single.IsNaN(rawIa) && !Single.IsInfinity(rawIa)) tygaMaximumRawIa = Math.Max(tygaMaximumRawIa, rawIa);
                }
                int previousEarlyEnd = 0;
                foreach (var record in observation.Records) {
                    tygaInvocations++;
                    var analysis = Tyga750ClampObserver.Analyze(record, observation, previousEarlyEnd);
                    previousEarlyEnd = record.EarlyEndSequence;
                    for (int i = 0; i < 4; i++) {
                        float ia = Tyga750ClampObserver.Single(record.IaBefore[i]);
                        float iv = Tyga750ClampObserver.Single(record.IvBefore[i]);
                        if (!Single.IsNaN(ia) && !Single.IsInfinity(ia)) tygaMaximumRawIa = Math.Max(tygaMaximumRawIa, ia);
                        if (iv > 0 && !Single.IsNaN(iv) && !Single.IsInfinity(iv)) tygaMinimumObservedField = Math.Min(tygaMinimumObservedField, iv);
                    }
                    if (analysis.AffectedPairs > 0) tygaAffectedInvocations++;
                    bool confirmed = analysis.Status == "retained_early750_before_nonzero_force_and_late_noop_verified";
                    if (confirmed) tygaConfirmed = true;
                    if (confirmed || analysis.AffectedPairs > 0 || tygaInvocations % 100 == 0) {
                        if (tygaSamples.Count < 256) tygaSamples.Add(new {
                            cycle, phase, generation = observation.Generation, analysis, record, early_records = observation.EarlyRecords,
                            averaged_Ia_at_native_return = observation.AveragedIaAtReturn,
                            averaged_Iv_at_native_return = observation.AveragedIvAtReturn,
                            output_velocity_kmh = Float(mem["TInputParamPult"], 1),
                            late_native_force = Float(mem["TResTyga"], 0),
                            timing = "Record is from the completed preceding ElectricalCycle; late averages read before the next pneumatic call"
                        });
                    }
                }
            }
            tygaMaximumSpeed = Math.Max(tygaMaximumSpeed, Math.Abs(Float(mem["TInputParamPult"], 1)));
            tygaObserver.ResetFrame(); tygaFrameOpen = true;
        }

        public void CloseTygaObserver() {
            beforeNativePneumatic = tygaOldBefore;
            try {
                if (tygaObserver != null) {
                    tygaObserver.Dispose(); tygaObserver = null; tygaFrameOpen = false;
                    Guard(tygaModule, Tyga750ClampObserver.BeforeRva, "C745F801000000");
                    Guard(tygaModule, Tyga750ClampObserver.AfterRva, "FF45F8837DF805");
                    Guard(tygaModule, Tyga750ClampObserver.EarlyBeforeRva, "E910C96D00");
                    Guard(tygaModule, Tyga750ClampObserver.EarlyAfterRva, "807C102200");
                    tygaObserverRestored = !Tyga750ClampObserver.HasRetainedOwner(tygaModule);
                }
            } finally {
                if (tygaSource != null) {
                    tygaSource.RestorePersistedSignalStates();
                    tygaSignalsRestored = tygaSource.Signals.All(s => !s.TeachingOverride && s.State == s.OriginalState);
                }
            }
        }

        public object TygaResult() {
            return new {
                error, sweep_finished = tygaSweepFinished, diagnostic_retained_limit_verified = tygaConfirmed,
                full_cycles = cycle, observed_electrical_frames = tygaFrames, no_independent_producer_frames = tygaZeroProducerFrames,
                observed_producer_invocations = tygaInvocations, affected_invocations = tygaAffectedInvocations,
                maximum_actual_speed_kmh = tygaMaximumSpeed, maximum_raw_Ia_A = tygaMaximumRawIa,
                minimum_nonzero_observed_field_A = tygaMinimumObservedField == Double.MaxValue ? (double?)null : tygaMinimumObservedField,
                prepared_initializations = preparedInitializations, genuine_native_origin = originEvidence,
                observer_restored_before_native_close = tygaObserverRestored, teaching_signals_restored = tygaSignalsRestored,
                native_close_allowed = TygaNativeCloseAllowed, native_inputs_restored = restored, native_closed = closed,
                input_history = tygaInputHistory, observations = tygaSamples,
                classification_limit = "A finite non-hit does not prove the branch unreachable. Apparatus outputs, current, field, torque, force and Uset were never assigned by this diagnostic."
            };
        }
    }
}
