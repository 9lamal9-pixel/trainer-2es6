using System;
using System.Linq;
using System.Globalization;
using Trainer2TE116;

static class Pneumatic074Bench
{
    static int passed, failed;
    static readonly CultureInfo C = CultureInfo.InvariantCulture;
    static void Check(bool ok, string name, string detail)
    {
        Console.WriteLine((ok ? "PASS " : "FAIL ") + name + ": " + detail);
        if (ok) passed++; else failed++;
    }
    static void Run(Pneumatics p, double seconds)
    {
        for (int i = 0; i < (int)Math.Round(seconds / .002); i++) p.Step(.002);
    }
    static Pneumatics Charged(int wagons)
    {
        var t = Train.Uniform("Pneumatic074", Catalog.Type(600), wagons, 71, false, 3);
        var p = new Pneumatics(); p.Build(t, t.ChargeMPa); p.Kvt = 2; p.RpmShare = new[] { .6, .6 };
        Run(p, 180);
        return p;
    }
    static void JournalFrame(ref double time, int crane, double ur, bool emergency)
    {
        Journal.Feed(new JournalInput { T = time, Dt = .1, X = 1000 + time * 10, V = 36, VSigned = 36,
            Ur = ur, Tm = ur, Charge = .53, Axles = 272, Crane = crane, Reverser = 1,
            EpkTripped = emergency, AllowedKmh = 80, MassT = 6573 });
        time += .1;
    }
    static bool B9(double over, bool emergency)
    {
        Journal.Configure(null); double t = 0;
        for (int i = 0; i < 10; i++) JournalFrame(ref t, 2, .53, false);
        double low = emergency ? .1 : .46;
        int brakeCrane = emergency ? 6 : 5;
        for (int i = 0; i < 30; i++) JournalFrame(ref t, brakeCrane, .53 - (.53 - low) * i / 29, emergency);
        for (int i = 0; i < 80; i++) JournalFrame(ref t, emergency ? 6 : 4, low, emergency);
        for (int i = 0; i < 50; i++) JournalFrame(ref t, 1, low + (.53 + over - low) * i / 49, false);
        for (int i = 0; i < 250; i++) JournalFrame(ref t, 2, .53 + over, false);
        return Journal.Entries.Any(x => x.Code == "B9");
    }
    static int Main()
    {
        Console.OutputEncoding = new System.Text.UTF8Encoding(false);
        var defaultInput = new JournalInput();
        Check(double.IsNaN(defaultInput.X) && defaultInput.AmbientC == 20 && defaultInput.Crane == 2 && defaultInput.Reverser == 1,
            "JournalInput compatibility defaults", "NaN coordinate, warm ambient, train brake position, forward");
        var entry = new JournalEntry { T = 60, Code = "B9", Title = "Проверка отображения", Points = 5 };
        Check(entry.ToString().Contains("B9") && entry.ToString().Contains(entry.Title), "JournalEntry readable UI", entry.ToString());
        foreach (bool emergency in new[] { false, true })
        foreach (double over in emergency ? new[] { .06, .10, .12 } : new[] { .02, .06, .09 })
        {
            bool expect = emergency ? over < .10 : over > .06;
            bool actual = B9(over, emergency);
            Check(actual == expect, "B9 actual Feed cycle", string.Format(C, "emergency={0}, over={1:F2} MPa, B9={2}, expected={3}", emergency, over, actual, expect));
        }
        var w = new WagonAir { Tm = new AirNode(.012, .1), Rk = new AirNode(.006, .529), Zk = new AirNode(.0045, 0), Wk = new AirNode(.0105, 0), Tc = new AirNode(.01, 0), Zr = new AirNode(.078, .529) };
        w.SyncWkFromChambers();
        Check(Math.Abs(w.Wk.M - w.Rk.M - w.Zk.M) < 1e-12 && Math.Abs(w.Wk.MPa - w.Rk.MPa) < 1e-12,
            "Wk aggregate mass and deep-brake RK readout", string.Format(C, "RK={0:F6}, ZK={1:F6}, display={2:F6}", w.Rk.MPa, w.Zk.MPa, w.Wk.MPa));
        foreach (double rate in new[] { .08 / 60, .10 / 60 })
        {
            var p = Charged(68); p.Crane = 4; bool triggered = false; double maxTc = 0;
            double initialMass = p.TotalMass();
            for (int i = 0; i < 30000; i++)
            {
                double dm = rate * 1e6 * p.Ur.Cap * .002; p.Ur.M -= dm; p.AirOut += dm;
                p.Step(.002); triggered |= p.W.Any(x => x.Braking); maxTc = Math.Max(maxTc, p.W.Max(x => x.Tc.MPa));
            }
            Check(!triggered && maxTc < .005, "softness without false brake", string.Format(C, "{0:F2} MPa/min, triggered={1}, maxTC={2:F5}", rate * 60, triggered, maxTc));
            Check(Math.Abs(p.Balance()) < initialMass * 1e-6, "softness air mass", p.Balance().ToString("G6", C));
        }
        foreach (int n in new[] { 40, 68, 100 })
        {
            var p = Charged(n); double ur = p.Ur.MPa; p.Crane = 5; bool[] began = new bool[n]; bool falseRelease = false;
            for (int i = 0; i < 20000; i++)
            {
                if (p.Crane == 5 && p.Ur.MPa <= ur - .06) p.Crane = 4;
                p.Step(.002);
                for (int j = 0; j < n; j++) { if (began[j] && !p.W[j].Braking) falseRelease = true; began[j] |= p.W[j].Braking; }
            }
            double firstTc = p.TailTcMPa; double stageUr = p.Ur.MPa; p.Crane = 5;
            for (int i = 0; i < 20000; i++) { if (p.Crane == 5 && p.Ur.MPa <= stageUr - .04) p.Crane = 4; p.Step(.002); }
            Check(!falseRelease && p.W.All(x => x.Braking) && p.W.All(x => !x.Emergency) && p.TailTcMPa > firstTc + .02,
                "service lap and repeated stage", string.Format(C, "n={0}, falseRelease={1}, tailTC={2:F4}->{3:F4}", n, falseRelease, firstTc, p.TailTcMPa));
            Check(Math.Abs(p.Balance()) < 1e-5, "repeated-stage air mass", p.Balance().ToString("G6", C));

            foreach (bool emergency in new[] { false, true })
            {
                var e = Charged(n); double startUr = e.Ur.MPa; e.Crane = emergency ? 6 : 5;
                for (int i = 0; i < 10000; i++)
                {
                    if (!emergency && e.Crane == 5 && e.Ur.MPa <= startUr - .06) e.Crane = 4;
                    e.Step(.002);
                }
                int flags = e.W.Count(x => x.Emergency);
                Check(flags == (emergency ? n : 0), "service/emergency positive and negative controls",
                    string.Format(C, "n={0}, crane={1}, emergency flags={2}", n, emergency ? "VI" : "V->IV", flags));
            }
        }
        Console.WriteLine("RESULT " + passed + " passed, " + failed + " failed");
        return failed == 0 ? 0 : 1;
    }
}
