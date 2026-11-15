// Targeted integration checks; compile separately with /r:Trainer2TE116.exe (C# 5 / .NET Framework).
using System;
using System.Globalization;
using Trainer2TE116;

static class Release074Bench
{
    static int pass, fail, checks;
    static void Need(bool ok, string message)
    {
        checks++;
        if (!ok) throw new InvalidOperationException(message);
    }
    static bool Finite(double x) { return !double.IsNaN(x) && !double.IsInfinity(x); }
    static void Group(string name, Action test)
    {
        try { test(); pass++; Console.WriteLine("PASS " + name); }
        catch (Exception ex) { fail++; Console.WriteLine("FAIL " + name + ": " + ex.Message); }
    }
    static void States(Traction t)
    {
        foreach (Section s in t.S)
        {
            foreach (double x in new[] { s.Current, s.VoltageKv, s.Field, s.ExciterField, s.IdK,
                s.IdAnchorMm, s.IdImpedanceOhm, s.FuelRack, s.Force, s.PowerKw, s.ShaftKw, s.FuelKg })
                Need(Finite(x), "non-finite state at t=" + t.T + ", section=" + s.Index);
            Need(s.Current >= 0 && s.Current <= 20000 && s.VoltageKv >= 0 && s.VoltageKv < 2,
                "electrical state outside solver range");
            Need(s.Field >= 0 && s.Field < 4 && s.ExciterField >= 0 && s.ExciterField < 4,
                "excitation escaped bounded filter");
            Need(s.IdK >= 0.05 && s.IdK <= 1.15 && s.IdAnchorMm >= 0 && s.IdAnchorMm <= 65 &&
                s.IdImpedanceOhm >= 5.5 && s.IdImpedanceOhm <= 70 && s.FuelRack >= 0 && s.FuelRack <= 1,
                "ID / fuel-rack range");
            Need(s.SuChannel >= 0 && s.SuChannel <= 4, "unknown SU channel");
        }
    }
    static void Run(Traction t, double seconds, double dt, double speed)
    {
        for (int k = 0, n = (int)Math.Ceiling(seconds / dt); k < n; k++)
        { t.Step(dt, speed); States(t); }
    }
    static Traction Prepared(double dt)
    {
        var t = new Traction(); t.Init(2, "nominal", 7);
        Need(t.Electric != null && t.Electric.Errors.Count == 0, "relay adapter failed to initialize");
        Run(t, 15, dt, 0);
        Need(t.S[0].Running && t.S[1].Running && t.S[0].VoltageKv > 0.05 && t.S[0].VoltageKv < 0.12,
            "prepared idle lost running diesel / excited generator");
        Need(t.TotalForceKn < 0.01 && t.S[0].PowerKw == 0, "idle produces traction");
        return t;
    }
    static void Drive(Traction t, double dt, double speed)
    {
        t.Position = 0; Run(t, 3, dt, speed);
        t.Position = 1; Run(t, 2, dt, speed);
        Need(t.S[0].Assembled, "first position did not assemble the power circuit");
        t.Position = 13; Run(t, 90, dt, speed);
        Need(t.S[0].Assembled && t.S[0].Current > 500 && t.S[0].Current < 7000,
            "loaded circuit did not remain active / lost current regulation");
        Need(Math.Abs(t.S[0].Rpm - 910) < 30, "13th-position RPM failed to settle");
    }
    static void Energy(Traction t, double dt, double speed)
    {
        double wheel = 0, electric = 0, shaft = 0;
        for (int k = 0, n = (int)Math.Ceiling(20 / dt); k < n; k++)
        {
            t.Step(dt, speed); States(t);
            foreach (Section s in t.S)
            { wheel += s.PowerKw * dt; electric += s.Current * s.VoltageKv * dt; shaft += s.ShaftKw * dt; }
        }
        Need(wheel > 1000 && wheel <= electric * 1.05 && electric <= shaft * 1.05,
            "sustained electrical / mechanical energy gain at speed=" + speed);
        Need(wheel > shaft * 0.35, "loaded energy test became vacuous");
    }
    static void StopsAndReset(Traction t, double dt)
    {
        Need(t.S[0].ExciterField > 0.01, "reset test has no prior excitation");
        t.BeginStopped(0);
        Need(t.S[0].State == DieselState.Off && t.S[0].Field == 0 && t.S[0].ExciterField == 0 &&
            t.S[0].Current == 0 && t.S[0].SuChannel == 0 && t.S[0].FuelRack == 0,
            "stop retained excitation or load state");
        Section old = t.S[0]; t.Position = 0; t.Init(1, "nominal", 7);
        Need(!object.ReferenceEquals(old, t.S[0]) && t.S[0].ExciterField == 0 &&
            t.S[0].Field == 0 && t.S[0].FuelKg == 0 && !t.S[1].Running, "Init retained trip state");
        Run(t, 10, dt, 0);
        Need(t.S[1].Current == 0 && t.S[1].Force == 0 && t.S[1].ExciterField == 0,
            "absent section acquired excitation / traction");
    }
    static void EmergencyExcitation()
    {
        const double dt = 0.1; var t = Prepared(dt); Drive(t, dt, 24.7);
        double normal = t.S[0].PowerKw; t.Rm[0].A12 = false; Run(t, 12, dt, 24.7);
        Need(!t.S[0].Assembled && t.S[0].Current < 1 && t.S[1].PowerKw > 300,
            "A12 cut failed or incorrectly cut both sections");
        t.Rm[0].AP = true; Drive(t, dt, 24.7);
        Need(t.S[0].SuChannel == 4 && t.S[0].PowerKw > normal * 0.2 &&
            t.S[0].PowerKw < normal * 1.1, "AP did not restore a bounded reduced-power path around A12");
        t.Rm[0].A1 = false; Run(t, 15, dt, 24.7);
        Need(!t.S[0].Assembled && t.S[0].Current < 1 && t.S[0].VoltageKv < 0.01,
            "AP bypassed the exciter supply breaker A1");
    }
    static void MotorCutouts()
    {
        const double dt = 0.1; var t = Prepared(dt); Drive(t, dt, 24.7);
        double six = t.S[0].Force; t.Rm[0].OM[0] = false; Run(t, 60, dt, 24.7);
        Need(t.Rm[0].Motors == 5 && !t.Electric.On("ТЭД_1", 1) && t.S[0].Assembled &&
            t.S[0].Force > six * 0.3 && t.S[0].Force <= six * 1.05 && t.S[0].Current < 5100,
            "one motor cutout lost circuit or ignored the reduced current envelope");
        for (int k = 0; k < 6; k++) t.Rm[0].OM[k] = false;
        Run(t, 12, dt, 24.7);
        Need(!t.S[0].Assembled && t.S[0].Force < 0.01 && t.S[0].Current < 1 && t.S[1].PowerKw > 300,
            "all motor cutouts leave phantom traction / affect the other section");
    }
    static void WagonAxes()
    {
        var train = Train.Uniform("68 loaded", Catalog.Type(600), 68, 69.6, true);
        var route = new Route { Name = "synthetic grade", Length = 20000, RulingGrade = 7.4,
            ProfX = new[] { 0.0 }, ProfLen = new[] { 20000.0 }, ProfSlope = new[] { 7.4 }, ProfH = new[] { 0.0 } };
        Journal.Configure(null);
        var sim = new Simulation(); sim.Start(route, train, 2000, true);
        double axleLoad = train.WagonsMassT / train.WagonAxles;
        Need(train.WagonAxles == 272 && train.Axles == 284 && axleLoad > 23 && axleLoad < 24,
            "wagon axles confused with wagon count / locomotive axles");
        Need(Journal.WeightNormT > 5600 && Journal.WeightNormT < 5900 && Journal.CriticalTrain,
            "Simulation.SetRoute dropped the wagon-axle argument or produced an empty norm");
        Need(BrakeTest.Axles == 272, "brake recorder received total train axles");
        Journal.Reset();
    }
    static int Main()
    {
        System.Threading.Thread.CurrentThread.CurrentCulture = CultureInfo.InvariantCulture;
        double coarse = 0, fine = 0;
        Group("traction dt=0.1", () => { var t = Prepared(0.1); Drive(t, 0.1, 24.7); Energy(t, 0.1, 24.7); coarse = t.TotalForceKn; StopsAndReset(t, 0.1); });
        Group("traction dt=0.02", () => { var t = Prepared(0.02); Drive(t, 0.02, 24.7); Energy(t, 0.02, 24.7); fine = t.TotalForceKn; StopsAndReset(t, 0.02); });
        Group("step-size consistency", () => Need(coarse > 0 && fine > 0 && Math.Abs(coarse - fine) / fine < 0.05, "settled traction depends strongly on step size"));
        Group("stationary motor energy", () => { var t = Prepared(0.1); Drive(t, 0.1, 0); Need(t.TotalForceKn > 5 && t.S[0].PowerKw == 0 && t.S[1].PowerKw == 0, "zero-speed traction creates mechanical work"); });
        Group("AP / A12 / A1", EmergencyExcitation);
        Group("motor cutouts", MotorCutouts);
        Group("wagon-axle integration", WagonAxes);
        Console.WriteLine("RESULT pass " + pass + " fail " + fail + " checks " + checks);
        return fail == 0 ? 0 : 1;
    }
}
