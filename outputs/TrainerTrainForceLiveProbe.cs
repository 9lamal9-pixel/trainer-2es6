using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web.Script.Serialization;
using System.Windows.Forms;

public sealed class TrainerTrainForceLiveProbe {
    [STAThread] public static int Main() { return TrainerAutovedenieAdverseBrakeProbe.RunTrainForceLive(); }
}

public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
    public static int RunTrainForceLive() {
        Application.EnableVisualStyles(); thread = GetCurrentThreadId(); Watch.Restart();
        Checks.Clear(); Cases.Clear(); failure = null;
        Fixture fixture = null; int exit = 1;
        try {
            fixture = new Fixture();
            fixture.afterNativeMotion = fixture.ObserveTrainForce;
            fixture.MeasureTrainForceScenario();
            fixture.VerifyTrainForceView(); exit = 0;
        } catch (Exception e) {
            failure = e.ToString(); Console.WriteLine(e);
            if (fixture != null) fixture.error = failure;
        } finally {
            if (fixture != null) {
                try { fixture.CloseManagedMotionProbe(); }
                catch (Exception e) { fixture.error = (fixture.error ?? "") + "\nFORCE RESTORE: " + e; exit = 1; }
                if (!TrainerNativeBrakeForceProvider.HasRetainedOwners) fixture.Close();
                if (fixture.error != null || !fixture.restored || !fixture.closed) exit = 1;
                Cases.Add(fixture.ManagedMotionResult());
                Cases.Add(fixture.ForceViewResult());
            }
        }
        File.WriteAllText(TrainerSession.ResultPath("Trainer_train_force_live_validation.json"),
            new JavaScriptSerializer { MaxJsonLength = 16000000 }.Serialize(new {
                passed = exit == 0, exit_code = exit, error = failure, checks = Checks, cases = Cases,
                scope = "Real native electrical start and physical KM V/IV service pneumatics feed accepted managed force snapshots. View, pause, replay and read-only state checks. Teaching signals/software RBS are declared fixture inputs. No release/stop, factory force/wave calibration or complete RE acceptance."
            }));
        Console.WriteLine(exit == 0 ? "PASS: live train force visualization" : "FAIL: train force visualization");
        return exit;
    }

    sealed partial class Fixture {
        readonly List<TrainForceSample> forceViewFrames = new List<TrainForceSample>();
        long forceViewFirstEpoch;
        int forceObserved, forceAccepted, forceBodyChecks;
        double forceMaxResidual, forceMaxLink, forceMaxTraction, forceMaxBrake;
        bool forceMapping = true, forceReadOnly = true, forceBalance = true;

        public void MeasureTrainForceScenario() {
            beforeNativePneumatic = ManagedBudget;
            Start(); ReadMotionOwner();
            motionTeachingSource = (TrainerMarkupSignalSource)Field("markupSource");
            foreach (var signal in motionTeachingSource.Signals) motionTeachingSource.SetTeachingSignalState(signal.Id, 0);
            Call("RouteInputCycle"); GenuineNativeStart();
            Check("Force-view scenario has genuine electrical origin", originForceSeen && preparedInitializations == 0, "no prepared motion", preparedInitializations);
            VerifyMotionPublication("force_view_genuine_coast");
            managedCoastEvidence = MotionSnapshot("force_view_genuine_coast");
            double ur = Float(mem["TLocParam"], 9);
            Handle(0x3b0, 5); phase = "force_view_physical_KMV";
            int start = cycle;
            while (ur - Float(mem["TLocParam"], 9) < .5 && cycle - start < 180) { ManagedBudget(); Step(); }
            Check("Force-view KMV reduces actual UR", ur - Float(mem["TLocParam"], 9) >= .5, ">=0.5kgf/cm2", ur - Float(mem["TLocParam"], 9));
            Handle(0x3b0, 4); phase = "force_view_IV_hold";
            for (int i = 0; i < 100; i++) { ManagedBudget(); Step(); }
            VerifyMotionPublication("force_view_service"); VerifyNativeBrakeCapacity("force_view_service", true);
            managedServiceEvidence = MotionSnapshot("force_view_service");
            // Stop the measurement after the service-force history. Release
            // and stopping are separate pneumatic/route acceptance scenarios;
            // a low-speed recoil can hit the existing forward-only signal guard.
            Check("Force view never prepares speed before measured phases", preparedInitializations == 0, 0, preparedInitializations);
            phase = "force_view_explicit_station_reset_component";
            Call("StartAtRouteStation", 0); ((Timer)Field("timer")).Stop(); managedPreparedStates++;
        }

        public void ObserveTrainForce() {
            using (TrainerManagedFloatingScope.Enter()) ObserveTrainForceCore();
        }
        void ObserveTrainForceCore() {
            var history = (TrainForceHistory)Field("trainForceHistory");
            var sample = history.Latest;
            if (sample == null) throw new Exception("Force history absent after publication");
            forceObserved++;
            if (!sample.HasAcceptedStep) return;
            forceAccepted++;
            if (forceViewFirstEpoch == 0) forceViewFirstEpoch = sample.Epoch;
            if (sample.Epoch == forceViewFirstEpoch) forceViewFrames.Add(sample);
            var kernel = (RailwayDynamics.Simulation.LongitudinalTrainDynamicsSolver)Field("managedMotion");
            double energy = kernel.TotalEnergyJ, x = kernel.GetVehicle(0).PositionX, time = kernel.SimulationTimeSeconds;
            var again = TrainForceSample.Capture(kernel, sample.Epoch);
            forceReadOnly &= kernel.TotalEnergyJ == energy && kernel.GetVehicle(0).PositionX == x && kernel.SimulationTimeSeconds == time;
            forceMapping &= sample.VehicleCount == 76 && sample.LinkCount == 75 && sample.TimeSeconds == time;
            for (int i = 0; i < sample.VehicleCount; i++) {
                var body = sample.GetVehicle(i); var node = kernel.GetVehicle(i);
                forceBodyChecks++;
                forceMaxResidual = Math.Max(forceMaxResidual, Math.Abs(body.BalanceResidualN));
                forceBalance &= Math.Abs(body.BalanceResidualN) <= body.BalanceToleranceN;
                forceMapping &= body.SpeedMps == node.VelocityV && body.CentrePositionM == node.PositionX;
                forceMapping &= body.BrakeCylinderPressurePa == node.BrakeCylinderPressurePa;
                forceMapping &= again.GetVehicle(i).NetForceN == body.NetForceN;
                forceMaxTraction = Math.Max(forceMaxTraction, Math.Abs(body.TractionN));
                forceMaxBrake = Math.Max(forceMaxBrake, Math.Abs(body.PneumaticBrakeN));
                if (i < 75) {
                    double tension = sample.GetLinkTensionN(i);
                    forceMaxLink = Math.Max(forceMaxLink, Math.Abs(tension));
                    forceMapping &= tension == -kernel.GetCouplerForceSignedNewtons(i);
                    forceMapping &= body.RearCouplerN == -tension && sample.GetVehicle(i + 1).FrontCouplerN == tension;
                }
            }
        }

        public void VerifyTrainForceView() {
            using (TrainerManagedFloatingScope.Enter()) VerifyTrainForceViewCore();
        }
        void VerifyTrainForceViewCore() {
            Check("Force graph observes actual accepted motion frames", forceAccepted > 100 && forceViewFrames.Count > 100, ">100", forceAccepted);
            Check("Force channels map all76 bodies/75 actual links", forceMapping, true, forceMapping);
            Check("Applied force sum equals effective mass times last-step acceleration", forceBalance, "per-body solver/rounding tolerance", forceMaxResidual);
            Check("Snapshot reads do not advance or change dynamics", forceReadOnly, true, forceReadOnly);
            Check("Native-driven start supplies nonzero traction and coupler stress", forceMaxTraction > 1000 && forceMaxLink > 1000, ">1000N", new { forceMaxTraction, forceMaxLink });
            Check("Physical service supplies nonzero actual applied brake force", forceMaxBrake > 1000, ">1000N", forceMaxBrake);
            var hostHistory = (TrainForceHistory)Field("trainForceHistory");
            Check("Station/speed resets clear old history and invalidate stale forces", hostHistory.Count == 1 && !hostHistory.Latest.HasAcceptedStep && hostHistory.Latest.Epoch > forceViewFirstEpoch, "newepoch/oneinvalidsample", hostHistory.Count);

            // Exercise the actual host entry point and close/reopen behaviour.
            using (var routeView = new DosRouteWindow(route)) {
                routeView.ShowTrainForces = delegate { Call("OpenTrainForceMode"); };
                routeView.CreateControl();
                var buttons = routeView.Controls.Find("TrainForces", true);
                if (buttons.Length != 1) throw new Exception("Route force button absent");
                // PerformClick requires a visible/selectable parent. This route
                // component shares the actual production callback contract.
                routeView.Show(); ((Button)buttons[0]).PerformClick();
                Check("Route force button dispatches opening", Field("trainForceView") != null, true, Field("trainForceView") != null);
            }
            var opened = (TrainForceForm)Field("trainForceView");
            Check("Host opens force window", opened != null && opened.Visible, true, opened != null);
            opened.Close(); Call("OpenTrainForceMode");
            opened = (TrainForceForm)Field("trainForceView");
            Check("Force window reopens", opened != null && opened.Visible, true, opened != null);
            opened.Close();

            // Render genuine measured history; no synthetic pressure or force is painted.
            using (var view = new TrainForceForm()) {
                view.Show();
                int peak = 0; double amplitude = -1;
                for (int k = 0; k < forceViewFrames.Count; k++) {
                    var s = forceViewFrames[k]; double a = 0;
                    for (int j = 0; j < s.LinkCount; j++) a = Math.Max(a, Math.Abs(s.GetLinkTensionN(j)));
                    if (a > amplitude) { amplitude = a; peak = k; }
                }
                foreach (var s in forceViewFrames.Take(peak + 1)) view.Push(s);
                Application.DoEvents();
                view.SaveRender(TrainerSession.ResultPath("Trainer_train_force_profile.png"), 0);
                var frozen = view.DisplayedSample;
                view.Paused = true;
                foreach (var s in forceViewFrames.Skip(peak + 1)) view.Push(s);
                view.SaveRender(TrainerSession.ResultPath("Trainer_train_force_paused.png"), 0);
                Check("Pause freezes displayed values while accepting physics history", view.Paused && Object.ReferenceEquals(frozen, view.DisplayedSample) && view.HistoryCount > 0, true, view.Paused);
                view.Paused = false;
                Check("Resume displays latest measured snapshot", Object.ReferenceEquals(view.DisplayedSample, forceViewFrames[forceViewFrames.Count - 1]), true, view.DisplayedSample.TimeSeconds);
                view.SaveRender(TrainerSession.ResultPath("Trainer_train_force_wave.png"), 1);
                view.SelectedVehicle = 75; view.SelectedLink = 74;
                Check("Tail body and rear link are selectable", view.SelectedVehicle == 75 && view.SelectedLink == 74, "75/74", view.SelectedVehicle);
                view.SaveRender(TrainerSession.ResultPath("Trainer_train_force_history.png"), 2);
                view.ClientSize = new System.Drawing.Size(1000, 700);
                view.SaveRender(TrainerSession.ResultPath("Trainer_train_force_minimum.png"), 0);
            }
            var kernel = (RailwayDynamics.Simulation.LongitudinalTrainDynamicsSolver)Field("managedMotion");
            double before = kernel.SimulationTimeSeconds;
            Check("Opening/replay/pause/render leave model time unchanged", before == hostHistory.Latest.TimeSeconds, before, hostHistory.Latest.TimeSeconds);
        }
        public object ForceViewResult() {
            return new { observed_frames = forceObserved, accepted_frames = forceAccepted, body_checks = forceBodyChecks,
                max_balance_residual_N = forceMaxResidual, max_link_absolute_N = forceMaxLink,
                max_traction_N = forceMaxTraction, max_actual_brake_N = forceMaxBrake,
                actual_history_frames = forceViewFrames.Count, mapping = forceMapping, read_only = forceReadOnly,
                png = new[] { "Trainer_train_force_profile.png", "Trainer_train_force_wave.png", "Trainer_train_force_history.png", "Trainer_train_force_minimum.png" } };
        }
    }
}
