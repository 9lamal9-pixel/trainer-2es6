// Contract tests for the netlist/physics boundary. No DLLs, hardware or instructor UI are needed.
// Compile against out/Trainer2TE116.exe and run from out. Each scenario starts with a fresh prepared locomotive.
using System;
using System.Globalization;
using System.Linq;
using Trainer2TE116;

static class SchemeIntegrationBench
{
    static int passed, failed;
    static void Check(string name, bool value, string detail = "")
    {
        if (value) passed++; else failed++;
        Console.WriteLine((value ? "PASS " : "FAIL ") + name + (detail.Length == 0 ? "" : ": " + detail));
    }
    static void Scenario(string name, Action test)
    {
        try { test(); }
        catch (Exception e) { Check(name + " threw", false, e.ToString()); }
    }
    static Traction New()
    {
        var tr = new Traction(); tr.Init(2, "nominal", 7); Run(tr, 3); return tr;
    }
    static void Run(Traction tr, double seconds, double speed = 0)
    { for (int i = 0; i < (int)Math.Round(seconds / 0.1); i++) tr.Step(0.1, speed); }
    static bool On(Traction tr, string name, int section = 1) { return tr.Electric.On(name, section); }
    static void Drive(Traction tr, int position = 8, double speed = 20)
    {
        tr.Position = 0; Run(tr, 2, speed);
        for (int p = 1; p <= position; p++) { tr.Position = p; Run(tr, 1, speed); }
        Run(tr, 10, speed);
    }
    static string State(Traction tr, int i)
    {
        return tr.S[i].State + ", oil=" + tr.S[i].OilP.ToString("0.000", CultureInfo.InvariantCulture) +
            ", RU23=" + On(tr, "РУ23", i + 1) + ", KMN=" + On(tr, "КМН", i + 1) +
            ", D3=" + On(tr, "Д3", i + 1) + ", why=" + tr.Electric.WhyOff(tr.Electric.Get("МР6", i + 1));
    }
    static int Main()
    {
        Console.OutputEncoding = System.Text.Encoding.UTF8;
        Scenario("traction intervention", () =>
        {
            var tr = New(); Drive(tr);
            Check("prepared locomotive produces traction on both sections", tr.S.All(s => s.Assembled && s.Force > 10));
            var kv = tr.Electric.Get("КВ", 1).Coils[0]; tr.Electric.SetOverride(kv, 1); Run(tr, 2, 20);
            Check("broken KV coil removes force only from its section", !tr.S[0].Assembled && tr.S[0].Force < 0.1 && tr.S[1].Assembled && tr.S[1].Force > 10);
            Check("broken KV coil has an explanatory cause", tr.Electric.WhyOff(tr.Electric.Get("КВ", 1)).Contains("обрыв"));
            tr.Electric.SetOverride(kv, 0); Drive(tr);
            Check("repair and proper assembly restore section traction", tr.S[0].Assembled && tr.S[0].Force > 10);
            tr.Rm[0].Doors = false; Run(tr, 2, 20);
            var bd = tr.Electric.Els.First(e => e.Key == "БД@1");
            Check("open high voltage door removes KV and VV", !On(tr, "КВ") && !On(tr, "ВВ") && tr.S[0].Force < 0.1);
            tr.Electric.SetOverride(bd, 2); Drive(tr);
            Check("jumper over door interlock restores excitation and traction", On(tr, "КВ") && tr.S[0].Assembled && tr.S[0].Force > 10);
            tr.Electric.ClearOverrides(); Run(tr, 2, 20);
            Check("clearing overrides restores the actual open-door condition", !On(tr, "КВ") && !tr.S[0].Assembled);
        });
        Scenario("compressor", () =>
        {
            var tr = New(); tr.CompressorRunning = true; Run(tr, 0.5);
            Check("compressor starts through the resistor", On(tr, "КРН") && On(tr, "КДК") && !On(tr, "КУДК") && tr.CompShare(0) == 0.5);
            Run(tr, 3); Check("compressor resistor is shunted after RV1", On(tr, "КУДК") && tr.CompShare(0) == 1);
            tr.Electric.SetOverride(tr.Electric.Get("КРН", 1).Coils[0], 1); Run(tr, 1);
            Check("broken KRN coil stops only its compressor", tr.CompShare(0) == 0 && tr.CompShare(1) == 1);
            tr.Electric.ClearOverrides(); Run(tr, 3); tr.Rm[0].AMK = false; Run(tr, 0.2);
            Check("AMK breaker interrupts the compressor motor feed", tr.CompShare(0) == 0 && On(tr, "КДК"));
        });
        Scenario("refrigerator", () =>
        {
            var tr = New(); tr.RefrigSw = false; tr.FanSw[3] = true; Run(tr, 1);
            Check("manual T4 operates fan 4 alone", tr.S[0].FanOn[3] && tr.S[0].FanOn.Count(b => b) == 1);
            tr.FanSw[2] = true; Run(tr, 0.3);
            Check("manual T3 is supplied through T4", tr.S[0].FanOn[2] && tr.S[0].FanOn[3]);
            tr.FanSw[3] = false; Run(tr, 0.3);
            Check("opening T4 removes both oil fans", !tr.S[0].FanOn[2] && !tr.S[0].FanOn[3]);
            tr.RefrigSw = true; tr.S[0].Water = 85; tr.S[0].Oil = 74; Run(tr, 0.5);
            Check("water and oil thermoswitches operate all cooling fans", tr.S[0].FanOn.All(b => b) && tr.S[0].ShutW && tr.S[0].ShutO);
            tr.Electric.SetOverride(tr.Electric.Get("К1", 1).Coils[0], 1); Run(tr, 0.3);
            Check("broken fan contactor affects only the chosen fan", !tr.S[0].FanOn[0] && tr.S[0].FanOn.Skip(1).All(b => b) && tr.S[1].FanOn.Count(b => b) == 0);
            tr.Electric.ClearOverrides(); tr.S[0].Water = 65; tr.S[0].Oil = 55; Run(tr, 0.5);
            Check("cooling falls back below thermoswitch hysteresis", tr.S[0].FanOn.All(b => !b) && !tr.S[0].ShutW && !tr.S[0].ShutO);
        });
        Scenario("neutral controller", () =>
        {
            var tr = New(); tr.Reverser = 0; Run(tr, 0.3); tr.Position = 15; Run(tr, 20);
            Check("neutral reverse handle blocks MR1-MR4 and rpm rise", tr.S.All(s => Math.Abs(s.Rpm - 350) < 1) && !On(tr, "МР1") && !On(tr, "МР2") && !On(tr, "МР3") && !On(tr, "МР4"));
        });
        Scenario("physical reversing direction", () =>
        {
            var tr = New();
            tr.Electric.SetOverride(tr.Electric.Get("ПР_Н", 1).Coils[0], 1);
            tr.Reverser = 2; Drive(tr, 5);
            Check("broken reversing valve leaves only its section in the old direction", tr.Direction(0) == 1 && tr.Direction(1) == -1);
            Check("reverse contact disagreement prevents traction on the affected section", !tr.S[0].Assembled && tr.S[1].Assembled);

            var route = new Route { Name = "direction bench", Length = 30000, ProfX = new[] { 0.0 }, ProfLen = new[] { 30000.0 }, ProfSlope = new[] { 0.0 }, ProfH = new[] { 0.0 } };
            var train = Train.Uniform("direction bench", Catalog.Type(600), 1, 69, false);
            Journal.Configure(null); var sim = new Simulation { DieselCondition = "nominal" }; sim.Start(route, train, 3000, true); sim.C.Kvt = 2;
            sim.Tr.Electric.SetOverride(sim.Tr.Electric.Get("ПР_Н", 1).Coils[0], 1);
            sim.Tr.Electric.SetOverride(sim.Tr.Electric.Els.First(e => e.Key == "ПР.Н@1"), 2);
            sim.C.Reverser = 2; for (int j = 0; j < 10; j++) sim.Step(0.1);
            sim.C.Position = 1; for (int j = 0; j < 30; j++) sim.Step(0.1);
            Check("simulation uses each actual reverser direction for its force sign", sim.Tr.Direction(0) == 1 && sim.Tr.Direction(1) == -1 && sim.Dyn.B[0].Traction > 100 && sim.Dyn.B[1].Traction < -100,
                "forces=" + sim.Dyn.B[0].Traction.ToString("0", CultureInfo.InvariantCulture) + "/" + sim.Dyn.B[1].Traction.ToString("0", CultureInfo.InvariantCulture));
        });
        Scenario("battery circuits", () =>
        {
            var tr = New(); Check("healthy charge circuit gives a positive ammeter reading", tr.BatteryA > 0 && On(tr, "Заряд_АБ"));
            var fuse = tr.Electric.Els.First(e => e.Name == "ПР4" && e.Sec == 1);
            tr.Electric.SetOverride(fuse, 1); Run(tr, 0.3);
            Check("broken charge fuse removes the positive charge reading", !On(tr, "Заряд_АБ") && tr.BatteryA <= 0);
            tr.Electric.SetOverride(fuse, 0); Run(tr, 0.3);
            Check("repairing the charge fuse restores positive charging", On(tr, "Заряд_АБ") && tr.BatteryA > 0);
            tr.FuelSw[0] = tr.FuelSw[1] = false; Run(tr, 70); tr.Rm[0].OMN = true; Run(tr, 0.5);
            Check("actual manual oil pump load discharges the battery", tr.OilPumpOn(0) && !tr.OilPumpOn(1) && tr.BatteryA < 0);
            tr.Electric.SetOverride(tr.Electric.Get("МН", 1).Coils[0], 1); Run(tr, 0.5);
            Check("broken pump motor removes its battery load despite picked KMN", On(tr, "КМН") && !tr.OilPumpOn(0) && tr.BatteryA == 0);
        });
        Scenario("sand direction", () =>
        {
            var tr = New(); tr.SandBtn = true; Run(tr, 0.3);
            Check("forward sand reaches only forward valves", On(tr, "1КП1") && On(tr, "2КП2") && !On(tr, "1КП2") && !On(tr, "2КП1") && tr.Sanding);
            tr.SandBtn = false; tr.SandAxle = true; Run(tr, 0.3);
            Check("first axle button supplies only first axle valves", On(tr, "1КП1") && !On(tr, "2КП2") && !On(tr, "1КП2") && !On(tr, "2КП1"));
            tr.SandAxle = false; tr.Reverser = 2; Run(tr, 0.3); tr.Position = 1; Run(tr, 1); tr.Position = 0; Run(tr, 1);
            tr.SandBtn = true; Run(tr, 0.3);
            Check("reverse direction routes sand to reverse valves", !On(tr, "1КП1") && !On(tr, "2КП2") && On(tr, "1КП2") && On(tr, "2КП1"));
            tr.Rm[0].BU = false; Run(tr, 0.3);
            Check("brake interlock 367 removes sand control feed", !tr.Sanding);
        });
        Scenario("successful restart", () =>
        {
            var tr = New(); tr.FuelSw[0] = tr.FuelSw[1] = false; Run(tr, 70);
            Check("complete stop and 60 s post-pumping finish on both sections", tr.S.All(s => s.State == DieselState.Off) && !On(tr, "КМН", 1) && !On(tr, "КМН", 2));
            tr.FuelSw[0] = tr.FuelSw[1] = true; Run(tr, 0.5);
            tr.LaunchPress[0] = tr.LaunchPress[1] = true; Run(tr, 58);
            Check("restart holds the 60 s oil-pumping stage", tr.S.All(s => s.State == DieselState.Pumping) && !On(tr, "Д3", 1) && !On(tr, "Д3", 2), State(tr, 0) + " | " + State(tr, 1));
            Run(tr, 4); Check("after 60 s both starters crank", tr.S.All(s => s.State == DieselState.Cranking) && On(tr, "Д2", 1) && On(tr, "Д2", 2));
            Run(tr, 13);
            Check("both diesels fire and starter contactors drop", tr.S.All(s => s.Running) && !On(tr, "Д3", 1) && !On(tr, "Д3", 2) && On(tr, "КРН", 1) && On(tr, "КРН", 2), State(tr, 0) + " | " + State(tr, 1));
            Check("KRN stops electric fuel pumps after successful start", !tr.FuelPumpOn(0) && !tr.FuelPumpOn(1));
        });
        Scenario("failed restart", () =>
        {
            var tr = New(); tr.FuelSw[1] = false; Run(tr, 70);
            var noFire = Traction.FaultList.First(f => f.Kind == 11); tr.SetFault(noFire, true);
            tr.FuelSw[1] = true; Run(tr, 0.5); tr.LaunchPress[1] = true; Run(tr, 80);
            Check("no-fire failure times out the starter", !tr.S[1].Running && !On(tr, "Д3", 2) && !On(tr, "Д2", 2), State(tr, 1));
            tr.LaunchPress[1] = true; Run(tr, 1);
            Check("failed start cannot immediately repeat", !On(tr, "Д3", 2) && On(tr, "РУ23", 2), State(tr, 1));
            tr.SetFault(noFire, false); tr.FuelSw[1] = false; Run(tr, 70); tr.FuelSw[1] = true; Run(tr, 0.5); tr.LaunchPress[1] = true; Run(tr, 76);
            Check("reset by fuel toggle and complete pumping permits successful retry", tr.S[1].Running && On(tr, "КРН", 2), State(tr, 1));
        });
        Scenario("emergency stop", () =>
        {
            var tr = New(); tr.EmergencyHeld = true; Run(tr, 0.3);
            Check("emergency button operates RU3 and typhon", tr.EstopLatch && On(tr, "РУ3", 1) && On(tr, "РУ3", 2) && On(tr, "ВТ", 1));
            tr.EmergencyHeld = false; Run(tr, 8);
            // The confirmed 17/2--1636 connection also holds the typhon supply after KA is released.
            Check("after emergency release RU3 and typhon hold while post-pumping runs", tr.EstopLatch && tr.S.All(s => !s.Running) && tr.OilPumpOn(0) && tr.OilPumpOn(1) && On(tr, "ВТ", 1), State(tr, 0));
            Run(tr, 65); Check("emergency post-pumping finishes", !tr.OilPumpOn(0) && !tr.OilPumpOn(1));
            tr.Reverser = 0; Run(tr, 0.3); Check("neutral handle resets emergency self-hold and typhon", !tr.EstopLatch && !On(tr, "ВТ", 1) && !On(tr, "ВТ", 2));
        });
        Scenario("lamp and DDR contacts", () =>
        {
            var tr = New(); tr.AlarmMon = false; Run(tr, 0.3);
            var lamps = tr.PultLamps(false);
            Check("TKS lights water, oil and brake lamps through their scheme loads", lamps[2] && lamps[3] && lamps[6]);
            tr.LampBurnt[3] = true; Run(tr, 0.3);
            Check("burnt lamp is dark in the model and the pult", !tr.PultLamps(false)[3] && !tr.Electric.On("ЛДМ"));
            tr.AlarmMon = true; tr.Rm[0].WaterLeak = true; Run(tr, 0.3);
            Check("own section water fault reaches common lamp", tr.LampWaterLevel);
            tr.DamageSel = 1; Run(tr, 0.3); Check("RU12 isolates own section from water lamp", !tr.LampWaterLevel);
            tr.Rm[1].WaterLeak = true; Run(tr, 0.3); Check("second section still reaches common lamp with RU12", tr.LampWaterLevel);
            tr.Ddr = true; tr.LocoTcMPa = 0; Run(tr, 0.2);
            Check("DDR operates RU1 and brake pipe lamp", tr.Sot && tr.LampBrakePipe(false));
            tr.Ddr = false; Run(tr, 0.3); Check("RU1 holds after DDR pulse", tr.Sot);
            tr.LocoTcMPa = 0.06; Run(tr, 0.3); Check("DTC opens RU1 self-holding circuit", !tr.Sot && !tr.LampBrakePipe(false));
        });
        Scenario("pickup interrupted", () =>
        {
            var s = new Scheme("P P1 0.3\n== Test\n@cab\nP: +A <SW> (P1) -", 1);
            s.Set("SW", true); s.Step(0.1); s.Step(0.1);
            Check("contactor does not pick before its travel time", !s.On("P1"));
            s.Set("SW", false); s.Step(0.1); s.Set("SW", true); s.Step(0.1); s.Step(0.1);
            Check("interruption resets the contactor travel timer", !s.On("P1"));
            s.Step(0.1); s.Step(0.1); Check("uninterrupted contactor feed eventually picks", s.On("P1"));
        });
        Scenario("body ventilation and heater", () =>
        {
            var tr = New(); tr.Rm[0].A11 = true; tr.Rm[1].AK = true; Run(tr, 0.3);
            Check("A11 feeds the body fan and shutters only in its section", On(tr, "МВК", 1) && On(tr, "ВВК", 1) && !On(tr, "МВК", 2) && !On(tr, "ВВК", 2));
            Check("AK feeds the heater motor only in its section", On(tr, "МК", 2) && !On(tr, "МК", 1));
            tr.Rm[0].A11 = false; tr.Rm[1].AK = false; Run(tr, 0.3);
            Check("auxiliary switches interrupt their actual loads", !On(tr, "МВК", 1) && !On(tr, "ВВК", 1) && !On(tr, "МК", 2));
        });
        Scenario("drying alternation", () =>
        {
            var tr = New(); bool previous = On(tr, "ВО1");
            Check("one drying valve is active before compressor cycling", On(tr, "ВО1") != On(tr, "ВО2"));
            for (int cycle = 1; cycle <= 4; cycle++)
            {
                tr.CompressorRunning = true; Run(tr, 0.8);
                bool current = On(tr, "ВО1");
                Check("compressor start alternates drying adsorber, cycle " + cycle,
                    On(tr, "ВО1") != On(tr, "ВО2") && current != previous && On(tr, "ВО1", 2) == current,
                    "VO1=" + current + ", VO2=" + On(tr, "ВО2") + ", VO-input=" + tr.Electric.Get("ВО@1"));
                previous = current; tr.CompressorRunning = false; Run(tr, 0.8);
                Check("drying relay retains the selected adsorber after compressor stop, cycle " + cycle, On(tr, "ВО1") == current && On(tr, "ВО2") != current);
            }
        });
        Scenario("fire detector groups", () =>
        {
            var tr = New();
            Check("healthy fire detectors keep both monitoring relays picked", On(tr, "РУП1") && On(tr, "РУП2") && !tr.Electric.On("ЛПС"));
            for (int sensor = 1; sensor <= 17; sensor++)
            {
                var e = tr.Electric.Els.First(el => el.Key == "ДТ" + sensor + "@1");
                tr.Electric.SetOverride(e, 1); Run(tr, 0.3);
                Check("DT" + sensor + " opens only its monitoring relay and lights the fire lamp",
                    (sensor >= 13 ? !On(tr, "РУП1") && On(tr, "РУП2") : On(tr, "РУП1") && !On(tr, "РУП2")) && tr.Electric.On("ЛПС") && !On(tr, "ВА") && tr.S[0].Running);
                tr.Electric.SetOverride(e, 0); Run(tr, 0.3);
            }
            Check("repair of all detectors restores normal fire monitoring", On(tr, "РУП1") && On(tr, "РУП2") && !tr.Electric.On("ЛПС"));
            tr.Rm[0].Set("X:ТПА", true); tr.Rm[0].Fire = true; Run(tr, 0.5);
            Check("automatic fire mode energizes powder valve and fuel cutoff", On(tr, "ВПТ") && On(tr, "ВА") && !tr.S[0].Running && tr.S[1].Running && tr.Electric.On("ЛПС"));
            tr = New(); tr.Rm[1].Set("X:ТП1", true); Run(tr, 0.3);
            Check("manual fire station operates its section without a detector alarm", On(tr, "ВПТ", 2) && On(tr, "ВА", 2) && !tr.S[1].Running && tr.S[0].Running && !On(tr, "ВПТ", 1));
        });
        Scenario("lighting and buffers", () =>
        {
            var tr = New(); tr.Rm[0].Set("X:ТОК", true); Run(tr, 0.3);
            Check("A14 supplies selected cab illumination", tr.Electric.On("С1") && tr.Electric.On("С2"));
            string[] switches = { "ТПЛ", "ТПП", "ТЗЛ", "ТЗП" };
            for (int k = 0; k < switches.Length; k++)
            {
                string sw = switches[k], white = (k + 1) + "ЛБ", red = (k + 1) + "ЛК";
                tr.Rm[0].Set("X:" + sw + "_Б", true); Run(tr, 0.3);
                Check(sw + " white buffer light is selected alone", tr.Electric.On(white) && !tr.Electric.On(red));
                tr.Rm[0].Set("X:" + sw + "_К", true); Run(tr, 0.3);
                Check(sw + " red selection clears white at the room switch and the lamp", tr.Rm[0].Get("X:" + sw + "_К") && !tr.Rm[0].Get("X:" + sw + "_Б") && tr.Electric.On(red) && !tr.Electric.On(white));
            }
            tr.Rm[0].A14 = false; Run(tr, 0.3);
            Check("opening A14 extinguishes the cab and all buffer lights", !tr.Electric.On("С1") && !tr.Electric.On("С2") && Enumerable.Range(1, 4).All(k => !tr.Electric.On(k + "ЛК") && !tr.Electric.On(k + "ЛБ")));
        });
        Scenario("slip RV4 output delay", () =>
        {
            var tr = New(); Drive(tr);
            var rb2 = tr.Electric.Els.First(e => e.Key == "РБ2@1");
            Check("before slip the second selective-unit reduction is off", !On(tr, "РВ4") && !On(tr, "СУ_СБ2"));
            tr.Electric.SetOverride(rb2, 2); Run(tr, 0.3, 20);
            Check("slip sensor energizes RV4 and the second selective-unit reduction", On(tr, "РВ4") && On(tr, "СУ_СБ2") && !On(tr, "СУ_СБ2", 2));
            tr.Electric.SetOverride(rb2, 0); Run(tr, 1.2, 20);
            Check("RV4 drops while its output holds for 1.5 seconds", !On(tr, "РВ4") && On(tr, "СУ_СБ2"));
            Run(tr, 0.5, 20); Check("selective-unit reduction releases after RV4 delay", !On(tr, "СУ_СБ2"));
        });
        Scenario("service brake release and reassembly", () =>
        {
            var route = new Route { Name = "DDR release regression", Length = 30000, ProfX = new[] { 0.0 }, ProfLen = new[] { 30000.0 }, ProfSlope = new[] { 0.0 }, ProfH = new[] { 0.0 } };
            var train = Train.Uniform("DDR release regression", Catalog.Type(600), 20, 69, false);
            Journal.Configure(null); var sim = new Simulation { DieselCondition = "nominal" }; sim.Start(route, train, 3000, true); sim.C.Kvt = 2;
            Action<double> run = seconds => { for (int k = 0; k < (int)Math.Round(seconds / 0.1); k++) sim.Step(0.1); };
            run(60);
            double chargedUr = sim.Air.Ur.MPa;
            sim.C.Crane = 5;
            for (int k = 0; k < 200 && sim.Air.Ur.MPa > chargedUr - 0.07; k++) sim.Step(0.1);
            sim.C.Crane = 4; run(30);
            Check("service application fills locomotive and all wagon cylinders", sim.Air.LocoTcMPa > 0.10 && sim.Air.W.All(w => w.Tc.MPa > 0.08));
            Check("DTC has reset RU1 after service cylinder filling", !sim.Tr.Sot);
            // A short I followed by II deliberately releases through the low-cylinder-pressure part
            // of the distributor's braking state. Recomputing DDR from that state used to retrigger RU1.
            sim.C.Crane = 1; run(0.3); sim.C.Crane = 2;
            bool releaseWindow = false, falseDdr = false, relatch = false;
            for (int k = 0; k < 1800; k++)
            {
                bool atWindow = sim.Air.LocoVrBraking && sim.Air.LocoTcMPa < 0.05;
                sim.Step(0.1);
                if (atWindow) { releaseWindow = true; falseDdr |= sim.Tr.Ddr; relatch |= sim.Tr.Sot; }
                if (k > 100 && !sim.Air.LocoVrBraking && sim.Air.LocoTcMPa < 0.005 && sim.Air.W.All(w => !w.Braking && w.Tc.MPa < 0.005)) break;
            }
            Check("release exercises low cylinder pressure while the distributor still reports braking", releaseWindow);
            Check("release does not generate a new DDR pulse or relatch RU1", !falseDdr && !relatch && !sim.Tr.Sot);
            Check("all brakes complete release after I and II", !sim.Air.LocoVrBraking && sim.Air.LocoTcMPa < 0.005 && sim.Air.W.All(w => !w.Braking && w.Tc.MPa < 0.005));
            sim.C.Position = 1; run(3);
            Check("first-position reassembly after brake release restores both traction forces", sim.Tr.S.All(s => s.Assembled && s.Force > 1) && sim.Dyn.B[0].Traction > 100 && sim.Dyn.B[1].Traction > 100,
                "RU1=" + sim.Tr.Sot + ", forces=" + sim.Dyn.B[0].Traction.ToString("0", CultureInfo.InvariantCulture) + "/" + sim.Dyn.B[1].Traction.ToString("0", CultureInfo.InvariantCulture));
        });
        Console.WriteLine("INTEGRATION " + (failed == 0 ? "PASS" : "FAIL") + " " + passed + " passed, " + failed + " failed");
        return failed == 0 ? 0 : 1;
    }
}
