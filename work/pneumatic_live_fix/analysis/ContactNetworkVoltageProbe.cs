using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;

public sealed class ContactNetworkVoltageProbe {
    [STAThread] public static int Main() { return TrainerAutovedenieAdverseBrakeProbe.RunContactNetworkVoltage(); }
}

public sealed partial class TrainerAutovedenieAdverseBrakeProbe {
    public static int RunContactNetworkVoltage() {
        Application.EnableVisualStyles(); thread = GetCurrentThreadId(); Watch.Restart();
        Checks.Clear(); Cases.Clear(); failure = null;
        int exit = 1; Fixture fixture = null;
        try {
            fixture = new Fixture(); fixture.MeasureContactNetworkVoltage(); exit = 0;
        } catch (Exception error) {
            failure = error.ToString(); if (fixture != null) fixture.error = failure; Console.WriteLine(error);
        } finally {
            if (fixture != null) {
                try { fixture.CloseContactVoltageProbe(); }
                catch (Exception error) { fixture.error = (fixture.error ?? "") + "\nVOLTAGE RESTORE: " + error; exit = 1; }
                if (!TrainerContactVoltagePatch.HasRetainedOwners) fixture.Close();
                else { fixture.error = (fixture.error ?? "") + "\nNative close refused while voltage patch ownership remains"; exit = 1; }
                if (fixture.error != null || !fixture.restored || !fixture.closed) exit = 1;
                Cases.Add(fixture.ContactVoltageResult());
            }
        }
        File.WriteAllText(TrainerSession.ResultPath("Trainer_contact_voltage_validation.json"),
            new JavaScriptSerializer { MaxJsonLength = 16000000 }.Serialize(new {
                passed = exit == 0, exit_code = exit, error = failure, checks = Checks, cases = Cases,
                scope = "Shared voltage source in original native producers. Genuine start from rest and explicit coast input sweeps; declared teaching green and scripted operator RBS. No current, force, pressure or protection output assignments. EDT is a labeled native component call only; cab EDT, PSN, derating, protective thresholds and factory traction/braking calibration remain unaccepted.",
                additional_prepared_speed_initializations = 0, complete_RE_conformity_accepted = false
            }));
        Console.WriteLine(exit == 0 ? "PASS: shared native contact voltage and retained supply gates" : "FAIL: contact voltage validation");
        return exit;
    }

    sealed partial class Fixture {
        TrainerMarkupSignalSource voltageSignals;
        IntPtr voltagePultModule, voltageTygaModule, voltageSchema, voltageConstants;
        TrainerContactVoltagePatch voltagePatch;
        readonly List<object> voltageRows = new List<object>();
        bool voltageRestored, voltageSignalsRestored;
        object voltageLastSnapshot;

        void VoltageInput(int? value) { Call("SetContactNetworkVoltage", value); }
        int RoofContactCount() {
            int count = 0;
            for (int n = 0; n < 4; n++) if (Marshal.ReadByte(voltageSchema, 0x58 + n) != 0) count++;
            return count;
        }
        float[] SolverCurrents() {
            // Tok[section,pair] uses 1-based section indexing in the native
            // producer. Absolute slots for the two sections are 3B..47.
            // +5B is the separate Tok_av reporting channel, not this frame's
            // four instantaneous electrical solver outputs.
            return new[] { Float(mem["TTygaParam"], 0x3b), Float(mem["TTygaParam"], 0x3f),
                Float(mem["TTygaParam"], 0x43), Float(mem["TTygaParam"], 0x47) };
        }

        void VoltageSample(string name, int requested, bool expectContact) {
            phase = name; VoltageInput(requested); Warm(2);
            int roof = RoofContactCount();
            int pult = Marshal.ReadInt32(voltageSchema, 0x125);
            float tyga = Float(mem["TResTyga"], 0x11);
            int expectedPult = roof > 0 ? requested : 0;
            bool tygaContact = Marshal.ReadByte(mem["TTygaParam"], 0x130) != 0;
            int expectedTyga = expectContact ? requested : 0;
            Check(name + " roof gate", expectContact ? roof > 0 : roof == 0, expectContact, roof);
            Check(name + " Tyga supply gate", tygaContact == expectContact, expectContact, tygaContact);
            Check(name + " Pult native voltage", pult == expectedPult, expectedPult, pult);
            Check(name + " Tyga native voltage", tyga == expectedTyga, expectedTyga, tyga);
            Check(name + " native Eset input", Marshal.ReadInt32(voltageConstants, 0x3fc) == requested, requested, Marshal.ReadInt32(voltageConstants, 0x3fc));
            voltagePatch.Validate();
            var snapshot = voltagePatch.Snapshot(); voltageLastSnapshot = snapshot;
            var entry = (Dictionary<string, object>)snapshot["TractionEntry"];
            Check(name + " fresh pre-command supply", Convert.ToUInt32(entry["LastGeneration"]) == Convert.ToUInt32(snapshot["Generation"]) &&
                Convert.ToSingle(entry["LastValue"]) == expectedTyga, expectedTyga, entry);
            if (expectContact) {
                var pultEntry = (Dictionary<string, object>)snapshot["Pult"];
                Check(name + " fresh Pult producer", Convert.ToUInt32(pultEntry["LastGeneration"]) == Convert.ToUInt32(snapshot["Generation"]), snapshot["Generation"], pultEntry);
            }
            voltageRows.Add(new { name, cycle, requested_V = requested, roof_contacts = roof,
                native_Useti_V = pult, native_Uset_V = tyga, tyga_contact = tygaContact,
                BV = new[] { Marshal.ReadByte(mem["TPult"], 0x89), Marshal.ReadByte(mem["TPult"], 0x8a) },
                actual_speed_kmh = Float(mem["TInputParamPult"], 1), force_native = Float(mem["TResTyga"], 0),
                patch = voltageLastSnapshot });
        }

        public void MeasureContactNetworkVoltage() {
            Start();
            voltagePultModule = GetModuleHandle("Pult1.dll"); voltageTygaModule = GetModuleHandle("Tyga1.dll");
            var pultOwner = Marshal.ReadIntPtr(Marshal.ReadIntPtr(voltagePultModule, 0x1a2e60));
            if (Marshal.ReadIntPtr(pultOwner, 4) != mem["TPult"]) throw new Exception("Voltage Pult owner alias");
            voltageSchema = IntPtr.Add(pultOwner, 0x18);
            var tygaOwner = Marshal.ReadIntPtr(Marshal.ReadIntPtr(voltageTygaModule, 0x19058c));
            voltageConstants = IntPtr.Add(tygaOwner, 8); Require(voltageConstants, 1048, false);
            voltagePatch = (TrainerContactVoltagePatch)Field("contactVoltagePatch");
            if (voltagePatch == null) throw new Exception("Production voltage adapter absent");
            VoltageSample("nominal_3000_before_traction", 3000, true);
            voltageSignals = (TrainerMarkupSignalSource)Field("markupSource");
            foreach (var signal in voltageSignals.Signals) voltageSignals.SetTeachingSignalState(signal.Id, 0);
            Call("RouteInputCycle"); GenuineNativeStart();
            Check("Voltage fixture has no prepared speed jump", preparedInitializations == 0, 0, preparedInitializations);
            // Genuine excitation switch: the cab widget publishes TPult+2C,
            // then Pult1 publishes Kont.VozbType+16 in this same whole cycle.
            Cab(0x3fc, 0); Step();
            Check("Genuine sequential excitation input", Marshal.ReadByte(mem["TPult"], 0x2c) == 0 && Marshal.ReadByte(mem["TTygaParam"], 0x16) == 0,
                new[] { 0, 0 }, new[] { (int)Marshal.ReadByte(mem["TPult"], 0x2c), (int)Marshal.ReadByte(mem["TTygaParam"], 0x16) });
            int savedSelector = Marshal.ReadInt32(mem["TPult"], 0x69);
            if (savedSelector < 0 || savedSelector > 5) throw new Exception("Native controller selector domain");
            try {
                Cab(0x3e0, 0); Handle(0x41c, 1); Step();
                Check("Genuine manual +1 controller input", Marshal.ReadInt32(mem["TPult"], 0x69) == 1 && Marshal.ReadByte(mem["TPult"], 0x26) == 0,
                    new[] { 1, 0 }, new[] { Marshal.ReadInt32(mem["TPult"], 0x69), (int)Marshal.ReadByte(mem["TPult"], 0x26) });
                Handle(0x41c, 0); Step();
                int heldPosition = Marshal.ReadInt32(mem["TTygaParam"], 0xc);
                Check("Sequential native position engaged", heldPosition > 0 && SolverCurrents().All(i => i > 0) && Float(mem["TResTyga"], 0) > 0,
                    "positive position/current/force", new { position = heldPosition, currents = SolverCurrents(), force = Float(mem["TResTyga"], 0) });
                var snapshot = voltagePatch.Snapshot();
                uint helpBefore = Convert.ToUInt32(((Dictionary<string, object>)snapshot["Help"])["Count"]);
                float highCurrent = 0, lowCurrent = 0;
                foreach (int value in new[] { 3200, 2800, 3000 }) {
                    VoltageSample("sustained_sequential_source_" + value, value, true);
                    var currentSnapshot = voltagePatch.Snapshot();
                    uint helpNow = Convert.ToUInt32(((Dictionary<string, object>)currentSnapshot["Help"])["Count"]);
                    int position = Marshal.ReadInt32(mem["TTygaParam"], 0xc);
                    float[] rawCurrents = SolverCurrents();
                    float current = rawCurrents.Average(), reportedCurrent = Float(mem["TTygaParam"], 0x5b), force = Float(mem["TResTyga"], 0);
                    Check("Sequential solve without intermediate Help " + value, position == heldPosition && helpNow == helpBefore &&
                        Marshal.ReadByte(mem["TTygaParam"], 0x16) == 0 && rawCurrents.All(i => i > 0 && !Single.IsInfinity(i)) && force > 0,
                        "held position/type0/current>0/force>0/no Help", new { position, helpBefore, helpNow, current, force });
                    voltageRows.Add(new { name = "sequential_solve_" + value, position, solver_currents_A = rawCurrents,
                        current_A = current, reported_Tok_av_A = reportedCurrent, force_native = force,
                        type = Marshal.ReadByte(mem["TTygaParam"], 0x16), helpBefore, helpNow, patch = currentSnapshot });
                    if (value == 3200) highCurrent = current; if (value == 2800) lowCurrent = current;
                }
                Check("Sequential current responds to native supply input", highCurrent > lowCurrent && lowCurrent > 0,
                    "I(3200)>I(2800)>0 at held position", new { highCurrent, lowCurrent });
            } finally {
                Handle(0x41c, savedSelector); Cab(0x3e0, 1); VoltageInput(3000); Step();
            }
            Cab(0x3fc, 1); Step();
            Check("Genuine independent excitation input", Marshal.ReadByte(mem["TPult"], 0x2c) == 1 && Marshal.ReadByte(mem["TTygaParam"], 0x16) == 1,
                new[] { 1, 1 }, new[] { (int)Marshal.ReadByte(mem["TPult"], 0x2c), (int)Marshal.ReadByte(mem["TTygaParam"], 0x16) });
            foreach (int value in new[] { 3200, 3100, 3000, 2900, 2700, 2200, 0, 3000, 3400, 3800, 4000, 4100, 4101, 3000 })
                VoltageSample("coast_source_" + value, value, true);
            ComponentEdtVoltage();
            Cab(0x3dc, 0); phase = "BV_off_with_live_roof_bus"; Warm(30);
            VoltageSample("live_bus_measured_before_open_BV", 3000, true);
            Check("Both native BV contacts are open", Marshal.ReadByte(mem["TPult"], 0x89) == 0 && Marshal.ReadByte(mem["TPult"], 0x8a) == 0,
                new[] { 0, 0 }, new[] { (int)Marshal.ReadByte(mem["TPult"], 0x89), (int)Marshal.ReadByte(mem["TPult"], 0x8a) });
            Cab(0x3a0, 0); Cab(0x3a4, 0); phase = "both_pantographs_lowered"; Warm(130);
            VoltageSample("no_roof_contact_retains_zero", 3200, false);
            Cab(0x3a0, 1); phase = "only_first_pantograph_raised"; Warm(130);
            VoltageSample("one_pantograph_feeds_shared_bus", 3000, true);
            Check("Exactly one roof contact", RoofContactCount() == 1, 1, RoofContactCount());
            Check("First pantograph is raised", Marshal.ReadByte(mem["TPult"], 0x97) == 1, 1, Marshal.ReadByte(mem["TPult"], 0x97));
            Check("Other pantograph stays lowered", Marshal.ReadByte(mem["TPult"], 0x98) == 0, 0, Marshal.ReadByte(mem["TPult"], 0x98));
            Check("Last explicit line input is nominal", (int?)Field("contactVoltageOverride") == 3000, 3000, Field("contactVoltageOverride"));
            VoltageInput(null); Step();
            Check("Removing scenario voltage selects nominal", Marshal.ReadInt32(voltageSchema, 0x125) == 3000, 3000, Marshal.ReadInt32(voltageSchema, 0x125));
            voltageLastSnapshot = voltagePatch.Snapshot();
            var finalSnapshot = (Dictionary<string, object>)voltageLastSnapshot;
            foreach (string producer in new[] { "Pult", "TygaEDT", "Nezav", "Help", "TractionEntry" }) {
                var observed = (Dictionary<string, object>)finalSnapshot[producer];
                Check("Executed native producer " + producer, Convert.ToUInt32(observed["Count"]) > 0, ">0", observed);
            }
        }

        void ComponentEdtVoltage() {
            // A separate component phase: actual native command setters and the
            // complete CalcRecup method. It does not prove the cab's EDT entry.
            var cell = Marshal.ReadIntPtr(voltageTygaModule, 0x19058c);
            Require(cell, 4, false); var owner = Marshal.ReadIntPtr(cell);
            Require(owner, 0x424, false); var calc = Marshal.ReadIntPtr(owner, 0x420);
            Require(calc, 0x110, false);
            if (Marshal.ReadIntPtr(calc, 4) != mem["TTygaParam"]) throw new Exception("EDT component owner alias");
            Guard(voltageTygaModule, 0x17d824, "558BEC83C4F88855FB8945FC8A45FB8B55FC3A8209010000742E8A45FB8B55FC8882090100008B45FC80B80901000000740CB2028B45FCE8F4090000EB0A33D28B45FCE8E809000059595DC3");
            Guard(voltageTygaModule, 0x17d870, "558BEC83C4F88855FB8945FC8B45FC8A80000100003A45FB740C8A45FB8B55FC88820001000059595DC3");
            Guard(voltageTygaModule, 0x17e254, "558BEC83C4F88855FB8945FC8A45FB8B55FC3A820A0100007412807DFB16730C8A45FB8B55FC88820A01000059595DC3");
            Guard(voltageTygaModule, 0x1748b0, "558BEC83C4BC8945FC8B45FC80B80901000000");
            byte edt = Marshal.ReadByte(calc, 0x109), position = Marshal.ReadByte(calc, 0x10a), recup = Marshal.ReadByte(calc, 0x100);
            if (edt > 1 || recup > 1 || position >= 22) throw new Exception("EDT command input domain");
            try {
                foreach (int value in new[] { 3000, 3200, 0, 3000 }) {
                    phase = "component_EDT_producer_" + value;
                    VoltageInput(value); Call("BeforeContactNetworkElectricalCycle");
                    Register(voltageTygaModule, calc, 1, 0x17d824);
                    Register(voltageTygaModule, calc, 1, 0x17d870);
                    Register(voltageTygaModule, calc, 0, 0x1748b0);
                    float actual = Float(mem["TResTyga"], 0x11);
                    Check(phase + " native voltage", actual == value, value, actual);
                    voltageRows.Add(new { name = phase, component_only = true, requested_V = value,
                        native_Uset_V = actual, patch = voltagePatch.Snapshot() });
                }
            } finally {
                Register(voltageTygaModule, calc, edt, 0x17d824);
                Register(voltageTygaModule, calc, position, 0x17e254);
                Register(voltageTygaModule, calc, recup, 0x17d870);
                Guard(voltageTygaModule, 0x17c188, "558BEC83C4F48945FC");
                Register(voltageTygaModule, calc, 0, 0x17c188);
            }
            Check("EDT component commands restored", Marshal.ReadByte(calc, 0x109) == edt &&
                Marshal.ReadByte(calc, 0x10a) == position && Marshal.ReadByte(calc, 0x100) == recup,
                new[] { edt, position, recup }, new[] { Marshal.ReadByte(calc, 0x109), Marshal.ReadByte(calc, 0x10a), Marshal.ReadByte(calc, 0x100) });
            Step();
        }

        public void CloseContactVoltageProbe() {
            if (host == null) return;
            try {
                if (mem != null && voltagePatch != null) {
                    // Restore is attempted even if resetting the optional input fails.
                    try { VoltageInput(null); }
                    finally { Call("CloseContactNetwork"); }
                    Guard(voltagePultModule, 0x18e9ba, "C780250100001C0C0000");
                    foreach (int rva in new[] { 0x17650f, 0x1748f4, 0x17c1a9 }) Guard(voltageTygaModule, rva, "C7401100004845");
                    Guard(voltageTygaModule, 0x17c34c, "8B45FC8B4004");
                    Check("Native Eset initializer restored", Marshal.ReadInt32(voltageConstants, 0x3fc) == 3200, 3200, Marshal.ReadInt32(voltageConstants, 0x3fc));
                    voltageRestored = Field("contactVoltagePatch") == null && !TrainerContactVoltagePatch.HasRetainedOwners;
                    Check("Voltage five native producers restored", voltageRestored, true, voltageRestored);
                }
            } finally {
                if (voltageSignals != null) {
                    voltageSignals.RestorePersistedSignalStates();
                    voltageSignalsRestored = voltageSignals.Signals.All(s => !s.TeachingOverride && s.State == s.OriginalState);
                    Check("Voltage teaching inputs restored", voltageSignalsRestored, true, voltageSignalsRestored);
                }
            }
        }

        public object ContactVoltageResult() {
            return new { error, cycles = cycle, prepared_initializations = preparedInitializations,
                genuine_native_positive_force_seen = originForceSeen, source_values = voltageRows,
                final_patch_snapshot = voltageLastSnapshot, native_patch_restored_before_close = voltageRestored,
                teaching_inputs_restored = voltageSignalsRestored, native_inputs_restored = restored, native_closed = closed };
        }
    }
}
