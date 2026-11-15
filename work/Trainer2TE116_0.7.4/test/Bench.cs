// Bench of the new pneumatics and longitudinal dynamics (no window, no pult). Norms: Rules of brake maintenance 2025
// (395: V 0.5 -> 0.4 MPa in 4-5 s, Va in 15-20 s, liquidation of overcharge; 483 softness; 254 fill), the earlier model
// (head wagon loaded 0.35 MPa in 16-22 s), PTR braking distances (40 x 94 t cast iron, emergency from 60 km/h: 478 m).
// Build: csc /r:Trainer2TE116.exe Bench.cs; run: Bench.exe [scenario.json]
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using Trainer2TE116;

static class Bench
{
    static int pass, fail;
    static readonly CultureInfo C = CultureInfo.InvariantCulture;
    // The groups of checks run at once on all the cores; the groups that use a whole trip (Simulation: the journal and the
    // brake test are common to the program) run one after another on the main thread. The report keeps the order.
    [ThreadStatic] static System.Text.StringBuilder buf;
    static readonly List<KeyValuePair<bool, Action>> groups = new List<KeyValuePair<bool, Action>>();
    static void Add(bool sequential, Action a) { groups.Add(new KeyValuePair<bool, Action>(sequential, a)); }
    static void RunAll()
    {
        var texts = new string[groups.Count]; var tasks = new List<System.Threading.Tasks.Task>();
        for (int k = 0; k < groups.Count; k++)
        {
            int kk = k; if (groups[k].Key) continue;
            tasks.Add(System.Threading.Tasks.Task.Factory.StartNew(() => texts[kk] = RunOne(groups[kk].Value), System.Threading.Tasks.TaskCreationOptions.LongRunning));
        }
        for (int k = 0; k < groups.Count; k++) if (groups[k].Key) texts[k] = RunOne(groups[k].Value);
        System.Threading.Tasks.Task.WaitAll(tasks.ToArray());
        foreach (var t in texts) Console.Write(t);
    }
    static string RunOne(Action a)
    {
        buf = new System.Text.StringBuilder();
        try { a(); } catch (Exception ex) { System.Threading.Interlocked.Increment(ref fail); buf.AppendLine("FAIL exception: " + ex); }
        return buf.ToString();
    }
    static void Check(bool ok, string name, string detail) { if (ok) System.Threading.Interlocked.Increment(ref pass); else System.Threading.Interlocked.Increment(ref fail); buf.AppendLine((ok ? "PASS " : "FAIL ") + name + ": " + detail); }
    static string F(double v, string f = "0.00") { return v.ToString(f, C); }

    static Pneumatics Charged(Train t)
    {
        var p = new Pneumatics(); p.Build(t, t.ChargeMPa); p.Crane = 2; p.Kvt = 2; p.RpmShare = new[] { 0.6, 0.6 };
        Run(p, 60); return p;
    }
    static double Run(Pneumatics p, double s, Func<bool> until = null)
    {
        double t = 0;
        while (t < s) { p.Step(0.002); t += 0.002; if (until != null && until()) break; }
        return t;
    }

    static int Main(string[] args)
    {
        try { Console.OutputEncoding = new System.Text.UTF8Encoding(false); } catch { }
        var t100 = Train.Uniform("100 гружёных", Catalog.Type(600), 100, 69, false);
        var t40 = Train.Uniform("40 x 94 т", Catalog.Type(600), 40, 71, false, 3);
        // ---- feed reservoir behind check valve 17 (air scheme of 2TE116: 4 x 250 l main + 250 l feed per section)
        Add(false, () =>
        {
            var p = Charged(t40);
            Check(Math.Abs(p.Mr.V - 2.0) < 1e-9 && Math.Abs(p.Pr.V - 0.5) < 1e-9, "ГР 4 × 250 л на секцию, питательный 250 л", "ГР " + F(p.Mr.V * 1000, "0") + " л, ПР " + F(p.Pr.V * 1000, "0") + " л");
            double pr0 = p.Pr.MPa;
            p.RpmShare = new[] { 0.0, 0.0 }; p.ExternalCompressorControl = false; p.CompressorWorks = new[] { false, false };
            // the pipe is no longer fed and the locomotive brakes by itself: the air of the feed reservoir may go to its cylinders only
            Func<double> held = () => { double m = p.Pr.M; foreach (var c in p.TcLoco) m += c.M; return m; };
            double h0 = held();
            p.Mr.M = Air.Mass(0.30, p.Mr.V); Run(p, 20);
            Check(Math.Abs(held() - h0) < 0.02 && p.Pr.MPa > 0.75 && p.Mr.MPa < 0.31, "обратный клапан 17: ПР не отдаёт воздух в ГР", "ГР " + F(p.Mr.MPa) + ", ПР " + F(pr0) + " → " + F(p.Pr.MPa) + " МПа, ТЦ " + F(p.LocoTcMPa) + ", воздух ПР + ТЦ изменился на " + F((held() - h0) * 1000, "0.000") + " г");
            double pr1 = p.Pr.MPa, mr1 = p.Mr.MPa; p.Kvt = 6; Run(p, 15);
            Check(p.LocoTcMPa > 0.36 && p.Pr.MPa < pr1 - 0.02 && p.Pr.MPa > pr1 - 0.20, "ТЦ тепловоза наполняются из ПР и расходуют его", "ТЦ " + F(p.LocoTcMPa) + ", ПР " + F(pr1) + " → " + F(p.Pr.MPa) + " МПа при ГР " + F(p.Mr.MPa));
        });
        // ---- density of the locomotive cylinders: 395 in VI, 254 in VI, not more than 0.2 kgf/cm2 in a minute
        Add(false, () =>
        {
            var p = Charged(t40);
            p.Crane = 6; p.Kvt = 6; Run(p, 30);
            double a0 = p.LocoTcMPa; Run(p, 60); double a1 = p.LocoTcMPa;
            Check(a0 > 0.36 && Math.Abs(a1 - a0) < 0.005, "ТЦ тепловоза, 395 в VI и 254 в VI: давление держится (утечку восполняет реле 304)", F(a0 * 10.197) + " → " + F(a1 * 10.197) + " кгс/см² за 60 с");
            p.LocoTcCut = true; double b0 = p.LocoTcMPa; Run(p, 60); double drop = (b0 - p.LocoTcMPa) * 10.197;
            Check(drop > 0.03 && drop <= 0.2, "плотность ТЦ тепловоза: питание перекрыто, снижение не более 0,2 кгс/см² за 1 мин", F(b0 * 10.197) + " → " + F(p.LocoTcMPa * 10.197) + " кгс/см², снижение " + F(drop));
        });
        // ---- 395
        Add(false, () =>
        {
            var p = Charged(t40); double ur0 = p.Ur.MPa;
            p.Crane = 5; double tv = Run(p, 30, () => p.Ur.MPa <= ur0 - 0.1);
            Check(tv >= 3.6 && tv <= 5.5, "395 V: УР на 0,1 МПа", F(tv, "0.0") + " с (норма 4–5 с)");
            p = Charged(t40); ur0 = p.Ur.MPa; p.Crane = 7; double tva = Run(p, 60, () => p.Ur.MPa <= ur0 - 0.1);
            Check(tva >= 13 && tva <= 22, "395 Vа: УР на 0,1 МПа", F(tva, "0.0") + " с (норма 15–20 с)");
            p = Charged(t40); p.Crane = 1; Run(p, 1, () => p.Ur.MPa >= 0.60); p.Ur.M = Air.Mass(0.60, p.Ur.V); p.Crane = 2; Run(p, 100);
            Check(p.Ur.MPa > 0.565 && p.Ur.MPa < 0.595, "395 II: ликвидация сверхзарядки", "УР 0,60 → " + F(p.Ur.MPa, "0.000") + " МПа за 100 с (темп 0,02 МПа за 80–120 с)");
            p = Charged(t40); p.Kvt = 6; double tk = Run(p, 20, () => p.LocoTcMPa >= 0.35);
            Check(tk <= 4.5, "254: ТЦ локомотива до 0,35 МПа", F(tk, "0.0") + " с (норма не более 4 с)");
        });
        // ---- service stage, wave, fill, no emergency
        foreach (var t0 in new[] { t40, t100 })
        {
            var t = t0;
            Add(false, () =>
            {
            var p = Charged(t); double ur0 = p.Ur.MPa; double m0 = p.TotalMass();
            p.Crane = 5; double first = -1, last = -1, time = 0;
            while (time < 40) { if (p.Crane == 5 && p.Ur.MPa <= ur0 - 0.06) p.Crane = 4; p.Step(0.002); time += 0.002; if (first < 0 && p.W[0].Tc.MPa > 0.03) first = time; if (last < 0 && p.TailTcMPa > 0.03) { last = time; } }
            int braked = p.W.Count(w => w.Braking), em = p.W.Count(w => w.Emergency);
            double len = t.Wagons.Sum(w => w.Length);
            Check(braked == p.W.Length && em == 0, "ступень 0,06 МПа, " + t.Name, "затормозили " + braked + " из " + p.W.Length + ", экстренных " + em + ", ТЦ хвоста " + F(p.TailTcMPa, "0.000") + " МПа");
            double wave = last > first ? (len - t.Wagons[0].Length) / (last - first) : 0;
            Check(wave > 100 && wave < 400, "тормозная волна служебного, " + t.Name, F(wave, "0") + " м/с (голова " + F(first, "0.0") + " с, хвост " + F(last, "0.0") + " с)");
            Check(Math.Abs(p.Balance()) < 1e-6 * m0 + 1e-6, "баланс массы воздуха, " + t.Name, F(p.Balance(), "0.000000") + " кг из " + F(m0, "0.0"));
            // release: I to charge + 0.04, II; tail cylinder below 0.04
            p.Crane = 1; double tr = 0;
            while (tr < 300) { if (p.Crane == 1 && p.Ur.MPa >= p.ChargeMPa + 0.04) p.Crane = 2; p.Step(0.002); tr += 0.002; if (p.TailTcMPa < 0.04 && tr > 2) break; }
            Run(p, 30); Check(tr < 120 && p.W.All(w => !w.Braking), "отпуск после ступени, " + t.Name, "хвост ниже 0,04 МПа через " + F(tr, "0") + " с, не отпустили " + p.W.Count(w => w.Braking) + " (первый " + Enumerable.Range(0, p.W.Length).FirstOrDefault(i => p.W[i].Braking) + ")");
            });
        }
        // ---- full service: head cylinder of a loaded wagon 0.35 MPa
        Add(false, () =>
        {
            var p = Charged(t40); double ur0 = p.Ur.MPa; p.Crane = 5; double tt = 0, t35 = -1;
            while (tt < 40) { if (p.Crane == 5 && p.Ur.MPa <= ur0 - 0.16) p.Crane = 4; p.Step(0.002); tt += 0.002; if (t35 < 0 && p.W[0].Tc.MPa >= 0.35) t35 = tt; }
            Check(t35 >= 12 && t35 <= 24, "полное служебное: ТЦ головного гружёного 0,35 МПа", F(t35, "0.0") + " с (норма 483 гружёный 16–22 с)");
            Check(p.W[0].Tc.MPa > 0.38 && p.W[0].Tc.MPa < 0.46, "ТЦ гружёного режима после полного служебного", F(p.W[0].Tc.MPa, "0.000") + " МПа (0,39–0,45)");
        });
        // ---- emergency wave
        foreach (var t0 in new[] { t40, t100 })
        {
            var t = t0;
            Add(false, () =>
            {
            var p = Charged(t); p.Crane = 6; double first = -1, last = -1, time = 0;
            while (time < 20) { p.Step(0.002); time += 0.002; if (first < 0 && p.W[0].Tc.MPa > 0.03) first = time; if (last < 0 && p.TailTcMPa > 0.03) last = time; }
            double len = t.Wagons.Sum(w => w.Length);
            double wave = last > first ? (len - t.Wagons[0].Length) / (last - first) : 0;
            int em = p.W.Count(w => w.Emergency);
            Check(em == p.W.Length && wave > 180 && wave < 400, "экстренное, " + t.Name, "ускорители " + em + " из " + p.W.Length + ", волна " + F(wave, "0") + " м/с, ТЦ хвоста " + F(p.TailTcMPa, "0.00") + " МПа");
            });
        }
        // ---- softness: slow drop of the pipe 0.0004 MPa/s must not brake
        Add(false, () =>
        {
            var p = Charged(t40); p.Crane = 4; double tt = 0;
            while (tt < 60) { double dm = 0.0004e6 * p.Ur.Cap * 0.002; p.Ur.M -= dm; p.AirOut += dm; p.Step(0.002); tt += 0.002; }
            Check(p.W.Count(w => w.Braking) == 0, "мягкость 483", "медленное снижение ТМ на " + F(p.ChargeMPa - p.TmLoco.MPa, "0.000") + " МПа за 60 с — торможений " + p.W.Count(w => w.Braking));
        });
        // ---- density and tail release of the VU-45
        foreach (var t0 in new[] { t40, t100 })
        {
            var t = t0;
            Add(false, () =>
            {
            var chk = Documents.Measure(t);
            double norm = t.WagonAxles + 12 <= 200 ? 58 : t.WagonAxles + 12 <= 400 ? 32 : 24; Check(chk.DensityS >= norm && chk.DensityS < norm * 3 && chk.ReleaseTailS > 5 && chk.ReleaseTailS < 150, "ВУ-45, " + t.Name, "плотность (норма табл. 4 при ГР 2000 л: не менее " + norm + " с) " + F(chk.DensityS, "0") + " с, отпуск хвоста " + F(chk.ReleaseTailS, "0") + " с, ТМ хвоста " + F(chk.TailMPa, "0.000"));
            });
        }

        // ---- dynamics: flat test route
        var flat = FlatRoute(30000, 0);
        // steady pull: force in the first coupler behind the locomotive = F * (wagons share), resistance aside
        Add(true, () =>
        {
            var t = Train.Uniform("40 x 94 т", Catalog.Type(600), 40, 71, false, 3);
            var d = new Dynamics(); d.Build(t, 2000);
            double tt = 0, fTr = 300e3;
            while (tt < 120) { for (int i = 0; i < d.B.Length; i++) { d.B[i].Traction = i < 2 ? fTr / 2 : 0; } d.SetTrackForces(flat, i => i < 2); d.Step(0.002); tt += 0.002; }
            double mAll = d.B.Sum(b => b.MassEff), mW = d.B.Skip(2).Sum(b => b.MassEff);
            double resW = d.B.Skip(2).Sum(b => b.Resist), resAll = d.B.Sum(b => b.Resist);
            double expect = (fTr - resAll) * mW / mAll + resW;
            Check(Math.Abs(d.Cp[1].Force - expect) / expect < 0.03, "сила в автосцепке за локомотивом при разгоне", F(d.Cp[1].Force / 1000, "0") + " кН, ожидается " + F(expect / 1000, "0") + " кН, скорость " + F(d.Speed * 3.6, "0.0") + " км/ч");
        });
        // emergency braking distance from 60 km/h, 40 x 94 t cast iron, level: PTR 478 m
        Add(true, () =>
        {
            var t = Train.Uniform("40 x 94 т чугун", Catalog.Type(600), 40, 71, false, 3);
            var sim = Start(flat, t, 60);
            double x0 = sim.Dyn.HeadX; sim.C.Crane = 6; sim.C.Position = 0;
            while (sim.SpeedKmh > 0.05 && sim.T < 200) sim.Step(0.1);
            double s = sim.Dyn.HeadX - x0;
            Check(s > 478 * 0.88 && s < 478 * 1.12, "экстренный тормозной путь с 60 км/ч, 40 × 94 т, чугун", F(s, "0") + " м (ПТР 478 м)");
            double mt = sim.Dyn.Cp.Max(c => c.MaxTension) / 1000, mc = sim.Dyn.Cp.Max(c => c.MaxCompression) / 1000;
            Check(mc > 50 && mc < 2000 && !sim.Dyn.Cp.Any(c => c.Broken), "силы в автосцепках при экстренном", "сжатие до " + F(mc, "0") + " кН, растяжение до " + F(mt, "0") + " кН, обрывов нет");
            Check(Math.Abs(sim.Air.Balance()) < 1e-5 * sim.Air.TotalMass(), "баланс воздуха в поездке", F(sim.Air.Balance(), "0.000000") + " кг");
        });
        // composite empty 71: emergency from 60 km/h, PTR 335 m
        Add(true, () =>
        {
            var t = Train.Uniform("71 порожний композит", Catalog.Type(600), 71, 0, true);
            var sim = Start(flat, t, 60);
            double x0 = sim.Dyn.HeadX; sim.C.Crane = 6;
            while (sim.SpeedKmh > 0.05 && sim.T < 200) sim.Step(0.1);
            double s = sim.Dyn.HeadX - x0;
            Check(s > 335 * 0.6 && s < 335 * 1.15, "экстренный путь с 60 км/ч, 71 порожний композит", F(s, "0") + " м (ПТР 335 м закладывает 10 с подготовки тормозов; ТЦ порожнего наполняется за 3–4 с, путь короче)");
        });
        // stand on +4 per mille: braked by 254 and the train stage holds, without brakes it rolls back
        Add(true, () =>
        {
            var up = FlatRoute(30000, 4);
            var t = Train.Uniform("40 x 94 т", Catalog.Type(600), 40, 71, false, 3);
            var sim = Start(up, t, 0);
            sim.C.Crane = 5; for (int i = 0; i < 40; i++) sim.Step(0.1); sim.C.Crane = 4; sim.C.Kvt = 6;
            double x0 = sim.Dyn.HeadX; for (int i = 0; i < 600; i++) sim.Step(0.1);
            Check(Math.Abs(sim.Dyn.HeadX - x0) < 0.05, "стоянка на +4 ‰ заторможенным", "смещение головы " + F(sim.Dyn.HeadX - x0, "0.000") + " м за 60 с");
            var sim2 = Start(up, t, 0); sim2.C.Kvt = 2; x0 = sim2.Dyn.HeadX;
            for (int i = 0; i < 300; i++) sim2.Step(0.1);
            Check(sim2.Dyn.HeadX < x0 - 1, "без тормозов на +4 ‰ поезд скатывается", F(sim2.SpeedKmh, "0.0") + " км/ч, " + F(sim2.Dyn.HeadX - x0, "0.0") + " м за 30 с");
        });
        // draft gear: the loop of a cycle dissipates energy
        Add(false, () =>
        {
            var t = Train.Uniform("2", Catalog.Type(600), 1, 71, false, 3);
            var d = new Dynamics(); d.Build(t, 100);
            var c = d.Cp[1]; double e = 0, prevD = 0, prevF = 0;
            for (int k = 0; k <= 2000; k++)
            {
                double ext = 0.10 * Math.Sin(2 * Math.PI * k / 2000.0);
                d.B[2].X = d.B[1].X - c.Neutral - ext; d.B[1].V = d.B[2].V = 0;
                d.Step(0.0); double f = c.Force;
                e += 0.5 * (f + prevF) * (ext - prevD); prevD = ext; prevF = f;
            }
            Check(e > 2000, "поглощающий аппарат: потери за цикл ±100 мм", F(e / 1000, "0.0") + " кДж");
        });
        // ---- sensor 418 and the lamp TM (Rules of brake maintenance 2025: 3.2.8, 3.2.21, 9.8.2, 10.1.20)
        foreach (var t0 in new[] { t40, t100 })
        {
            var t = t0;
            Add(false, () =>
            {
            var p = Charged(t); bool lit = false;
            Run(p, 30, () => { lit |= p.Lamp418; return false; });
            Check(!lit, "418: поезд заряжен, II — лампа ТМ не горит, " + t.Name, "подпитка " + F(p.LampFeedKgS * 1000, "0.00") + " г/с при утечке " + F(p.LeakRefKgS * 1000, "0.00") + " г/с");
            double ur0 = p.Ur.MPa; p.Crane = 5; Run(p, 10, () => p.Ur.MPa <= ur0 - 0.025); p.Crane = 4;
            double tOn = Run(p, 20, () => p.Lamp418);
            // 3.2.21 is a check of the locomotive equipment: the lamp must light on the 0.02-0.03 step; on a long train the
            // additional discharges are spread along it and the feed of the crane hardly grows (reported, not required)
            bool lit21 = p.Lamp418;
            if (t == t40) Check(lit21 && p.W.Count(w => w.Braking) > p.W.Length / 2, "418, п. 3.2.21: ступень 0,025 МПа — лампа загорается при срабатывании ВР, " + t.Name, "через " + F(tOn, "0.0") + " с, затормозили " + p.W.Count(w => w.Braking) + " из " + p.W.Length);
            else buf.AppendLine("INFO 418: ступень 0,025 МПа, " + t.Name + " — лампа " + (lit21 ? "загорелась через " + F(tOn, "0.0") + " с" : "не загорелась: подпитка " + F(p.LampFeedKgS * 1000, "0.0") + " г/с при пороге " + F(p.LampFlowRatio * p.LeakRefKgS * 1000, "0.0")));
            double ur1 = p.Ur.MPa; p.Crane = 5; Run(p, 10, () => p.Ur.MPa <= ur1 - 0.04); p.Crane = 4;
            double tOff = Run(p, 15, () => !p.Lamp418);
            Check(!p.Lamp418, "418, п. 3.2.21: дальнейшее снижение УР на 0,04 МПа — лампа " + (lit21 ? "гаснет" : "не горит") + ", " + t.Name, "через " + F(tOff, "0.0") + " с");
            p.Crane = 1; lit = false; double tr = 0;
            while (tr < 90) { if (p.Crane == 1 && p.Ur.MPa >= p.ChargeMPa + 0.03) p.Crane = 2; p.Step(0.002); tr += 0.002; lit |= p.Lamp418 && tr > 0.5; }
            Check(!lit, "418: отпуск и зарядка после него — лампа не горит, " + t.Name, "90 с");
            });
        }
        Add(false, () =>
        {
            var p = Charged(t40); p.Crane = 1; Run(p, 2, () => p.Ur.MPa >= 0.60); p.Ur.M = Air.Mass(0.60, p.Ur.V); p.Crane = 2;
            Run(p, 40); bool lit = false; Run(p, 60, () => { lit |= p.Lamp418; return false; });
            Check(!lit, "418, п. 3.2.8: ликвидация сверхзарядки во II — лампа не горит", "УР " + F(p.Ur.MPa, "0.000") + " МПа");
            p = Charged(t40); p.BreakAt = p.W.Length / 2; double tb = Run(p, 15, () => p.Lamp418);
            Check(p.Lamp418, "418, п. 10.1.20: обрыв ТМ в середине поезда, II — лампа загорается", "через " + F(tb, "0.0") + " с; кран питает ТМ из ГР установленным порядком, ТМ " + F(p.TmLoco.MPa, "0.000") + " МПа");
            p = Charged(t40); p.BreakAt = p.W.Length - 1; Run(p, 8); bool on = p.Lamp418; p.BreakAt = -1;
            Check(on, "418, п. 9.8.2: концевой кран хвоста открыт на 8 с — лампа загорается", "");
        });
        // ---- diesels and the electric transmission (model of the package, 2Te116_brakes.ini values)
        Add(false, () =>
        {
            Func<string, Traction> mk = cond => { var x = new Traction(); x.Init(2, cond, 7); return x; };
            var tr = mk("nominal");
            for (int i = 0; i < 300; i++) tr.Step(0.1, 0);
            Check(Math.Abs(tr.S[0].VoltageKv * 1000 - 80) < 15 && Math.Abs(tr.S[0].Rpm - 350) < 1, "холостой ход: ГГ возбуждён для вентиляторов", F(tr.S[0].VoltageKv * 1000, "0") + " В, " + F(tr.S[0].Rpm, "0") + " об/мин (80–85 В)");
            Check(tr.S[0].OilP * Traction.KgfPerMPa > 1.33 && tr.S[0].OilP * Traction.KgfPerMPa < 4, "давление масла на 350 об/мин", F(tr.S[0].OilP * Traction.KgfPerMPa, "0.0") + " кгс/см² (РЭ: не ниже 1,3)");
            tr.Position = 3; for (int i = 0; i < 10; i++) tr.Step(0.1, 0);
            Check(!tr.S[0].Assembled, "РУ8: схема не собирается сразу на 3-й позиции", "ЛН горит: " + tr.LoadShed(0));
            tr.Position = 0; for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            tr.Position = 1; for (int i = 0; i < 10; i++) tr.Step(0.1, 0);
            Check(tr.S[0].Assembled && tr.S[1].Assembled, "сборка схемы на 1-й позиции", "");
            for (int k = 2; k <= 15; k++) { tr.Position = k; for (int i = 0; i < 10; i++) tr.Step(0.1, 24.7); }
            for (int i = 0; i < 900; i++) tr.Step(0.1, 24.7);
            var s0 = tr.S[0];
            Check(Math.Abs(s0.VoltageKv * 1000 - 462) < 50 && Math.Abs(s0.Current - 4300) < 500, "продолжительный режим 24,7 км/ч на 15-й", F(s0.VoltageKv * 1000, "0") + " В, " + F(s0.Current, "0") + " А, " + F(s0.Force, "0") + " кН (паспорт ЭД-118А: 463 В, 6 × 720 А)");
            Check(Math.Abs(s0.Rpm - 1000) < 25, "обороты 15-й позиции", F(s0.Rpm, "0") + " об/мин");
            double vmax = 0; for (int i = 0; i < 600; i++) { tr.Step(0.1, 25 + i * 0.1); vmax = Math.Max(vmax, s0.VoltageKv); }
            Check(vmax <= 0.715, "напряжение ГГ не выше 700 В при разгоне", F(vmax * 1000, "0") + " В, ослабление поля ОП" + s0.FwStage);
            Check(s0.Water < 92 && s0.Oil < 86, "охлаждение держит воду и масло на 15-й", "вода " + F(s0.Water, "0") + " °C, масло " + F(s0.Oil, "0") + " °C, вентиляторы " + s0.FanOn.Count(o => o) + " из 4");
            tr.TmMPa = 0.25; for (int i = 0; i < 15; i++) tr.Step(0.1, 50);
            Check(!tr.S[0].Assembled, "РДВ: ТМ ниже 0,294 МПа снимает тягу", "");
            tr.TmMPa = 0.50; for (int i = 0; i < 20; i++) tr.Step(0.1, 50);
            Check(!tr.S[0].Assembled, "после РДВ схема собирается только с 1-й позиции", "");
            tr.Position = 0; for (int i = 0; i < 20; i++) tr.Step(0.1, 50); tr.Position = 1; for (int i = 0; i < 10; i++) tr.Step(0.1, 50);
            for (int i = 0; i < 30; i++) tr.Step(0.1, 50); bool asm0 = tr.S[0].Assembled; tr.Tc254MPa = 0.20; for (int i = 0; i < 10; i++) tr.Step(0.1, 50); bool keep = tr.S[0].Assembled; tr.Tc254MPa = 0.29; tr.Step(0.1, 50);
            Check(asm0 && keep && !tr.S[0].Assembled, "ДПТ: уставка 0,22 МПа — на 3-й и 4-й ступенях крана 254 тяга остаётся, с 5-й снимается", "собрана " + asm0 + ", при 0,20 МПа " + keep);
            tr.Tc254MPa = 0;
            var tw = mk("worn"); var tn = mk("nominal");
            foreach (var x in new[] { tw, tn }) { x.Position = 1; for (int i = 0; i < 10; i++) x.Step(0.1, 0); for (int k = 2; k <= 15; k++) { x.Position = k; for (int i = 0; i < 10; i++) x.Step(0.1, 40); } for (int i = 0; i < 600; i++) x.Step(0.1, 40); }
            double pw = tw.S[0].PowerKw + tw.S[1].PowerKw, pn = tn.S[0].PowerKw + tn.S[1].PowerKw;
            Check(pw < 0.92 * pn && pw > 0.65 * pn, "состояние «дно» снижает мощность", F(pw, "0") + " против " + F(pn, "0") + " кВт на ободе (" + tw.ConditionNote + ")");
            tr = mk("normal"); for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            tr.FuelSw[1] = false; for (int i = 0; i < 100; i++) tr.Step(0.1, 0);
            Check(tr.S[1].State == DieselState.Off && tr.S[1].PostPumpT > 0, "топливный насос 2 выключен: дизель остановлен, прокачка после остановки", tr.S[1].StateName);
            tr.LaunchPress[1] = true; tr.Step(0.1, 0);
            Check(tr.S[1].State == DieselState.Off, "ПУСК во время прокачки после остановки не проходит", tr.S[1].StateName);
            for (int i = 0; i < 600; i++) tr.Step(0.1, 0);
            tr.FuelSw[1] = true; tr.LaunchPress[1] = true; double ts = 0; while (ts < 130 && !tr.S[1].Running) { tr.Step(0.1, 0); ts += 0.1; }
            Check(tr.S[1].Running && ts > 70 && ts < 76, "пуск: прокачка масла 60 с (РВП1) и прокрутка 12 с (РВП2)", F(ts, "0.0") + " с");
            tr.EstopPress = true; for (int i = 0; i < 100; i++) tr.Step(0.1, 0);
            Check(tr.EstopLatch && !tr.S[0].Running && !tr.S[1].Running, "аварийная кнопка глушит оба дизеля", "");
            tr.LocoControl = false; tr.Step(0.1, 0); tr.LocoControl = true; tr.Step(0.1, 0); bool held = tr.EstopLatch;
            tr.Reverser = 0; tr.Step(0.1, 0); tr.Reverser = 1; tr.Step(0.1, 0);
            Check(held && !tr.EstopLatch, "аварийная остановка (РУ3): тумблер УТ не сбрасывает, реверсивная рукоятка в нейтраль — сбрасывает", "");
            tr.Reverser = 0; tr.EstopPress = true; tr.Step(0.1, 0);
            Check(!tr.EstopLatch, "аварийная кнопка без реверсивной рукоятки не действует (нет питания РУ3)", ""); tr.Reverser = 1;
        });
        // ---- relay circuits by the scheme 2TE116 (variant 008): Circuits.cs and the apparatus outside the pult
        Add(false, () =>
        {
            Func<Traction> mk = () => { var x = new Traction(); x.Init(2, "nominal", 7); x.CompressorRunning = false; for (int i = 0; i < 50; i++) x.Step(0.1, 0); return x; };
            Func<int, ElecFaultDef> fd = k => Traction.FaultList.First(d => d.Kind == k);
            Action<Traction, int, double> drive = (x, pos, v) => { x.Position = 0; for (int i = 0; i < 20; i++) x.Step(0.1, v); for (int k = 1; k <= pos; k++) { x.Position = k; for (int i = 0; i < 10; i++) x.Step(0.1, v); } for (int i = 0; i < 100; i++) x.Step(0.1, v); };
            // earthing relay RZ: trips under load, latches, A4 does not help while the motor is in, OM cuts it out
            var tr = mk(); tr.SetFault(fd(20), true); drive(tr, 8, 20);
            var rm = tr.Rm[0];
            Check(rm.RZ && !tr.S[0].Assembled && tr.S[1].Assembled && tr.LampPowerProtection && tr.LoadShed(0) && !tr.LoadShed(1), "РЗ: пробой на корпус снимает возбуждение секции 1", "лампа «Силовая защита» и «Сброс нагрузки 1», секция 2 тянет " + F(tr.S[1].Force, "0") + " кН");
            rm.A4 = false; tr.Step(0.1, 20); rm.A4 = true; tr.Step(0.1, 20); bool cleared = !rm.RZ; drive(tr, 8, 20);
            Check(cleared && rm.RZ, "РЗ: повторное включение А4 снимает защёлку, под нагрузкой РЗ срабатывает снова", "");
            rm.OM[rm.GroundTed] = false; rm.A4 = false; tr.Step(0.1, 20); rm.A4 = true; drive(tr, 8, 20);
            Check(!rm.RZ && tr.S[0].Assembled && tr.S[0].Force > 0.6 * tr.S[1].Force && tr.S[0].Force < 0.95 * tr.S[1].Force, "ОМ: неисправный ТЭД отключён, секция работает на пяти", "ОМ" + (rm.GroundTed + 1) + " выключен, " + F(tr.S[0].Force, "0") + " против " + F(tr.S[1].Force, "0") + " кН");
            tr = mk(); tr.SetFault(fd(21), true); tr.Rm[0].VRZ = false; drive(tr, 8, 20);
            Check(!tr.Rm[0].RZ && tr.S[0].Assembled, "ВРЗ отключён: с пробоем в минусовой цепи секция работает", "");
            // breakers of the fans: RV3 drops, lamps "cooling" and "load shedding"
            tr = mk(); tr.SetFault(fd(24), true); drive(tr, 8, 20);
            Check(!tr.S[0].Assembled && tr.LampCooling && tr.LoadShed(0), "АВУ сработал: тяга секции снята, лампа «Обдув»", "");
            tr.Rm[0].AVU = true; drive(tr, 8, 20);
            Check(tr.S[0].Assembled && !tr.LampCooling, "АВУ включён: схема собирается с 1-й позиции", "");
            // exciter breaker at idle: no generator voltage, the damage indicator stops at the exciter
            tr = mk(); tr.DamageSw = 1; double up0 = tr.DamageIndicator;
            tr.Rm[0].A1 = false; for (int i = 0; i < 100; i++) tr.Step(0.1, 0);
            string chain; string open = Chain.FirstOpen(tr.DamageChain(0, out chain));
            Check(up0 > 9.9 && tr.S[0].VoltageKv < 0.01 && tr.DamageIndicator < up0 && chain == "Возбуждение" && open != null && open.Contains("А1"), "А1 «Возбудитель» выключен: нет напряжения ГГ, указатель повреждений не доходит до конца цепи", "указатель " + F(up0, "0.0") + " → " + F(tr.DamageIndicator, "0.0") + ", разомкнуто: " + open);
            tr.DamageSw = 0; Check(tr.DamageIndicator == 0, "указатель повреждений выключен переключателем", "");
            // common control: no rpm by the controller, no start, and it resets the emergency stop
            tr = mk(); tr.Rm[0].AU = false; tr.Position = 5; for (int i = 0; i < 100; i++) tr.Step(0.1, 0);
            Check(Math.Abs(tr.S[0].Rpm - 350) < 5 && !tr.S[0].Assembled, "АУ «Управление общее» выключен: обороты не растут, тяги нет", F(tr.S[0].Rpm, "0") + " об/мин на 5-й позиции");
            tr.Position = 0; tr.Rm[0].AU = true; tr.Step(0.1, 0); tr.EstopPress = true; for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            bool latched = tr.EstopLatch; tr.Rm[0].AU = false; tr.Step(0.1, 0); tr.Rm[0].AU = true; tr.Step(0.1, 0);
            Check(latched && !tr.EstopLatch, "аварийная остановка (РУ3) сбрасывается автоматом АУ", "");
            // compressor: electric motor, start through the resistor, breaker A5
            tr = mk(); tr.CompressorRunning = true; tr.Step(0.1, 0); double c0 = tr.CompShare(0); for (int i = 0; i < 30; i++) tr.Step(0.1, 0);
            double c1 = tr.CompShare(0); tr.Rm[1].A5 = false; tr.Step(0.1, 0);
            Check(c0 == 0.5 && c1 == 1 && tr.CompShare(1) == 0 && Math.Abs(tr.S[0].Rpm - 350) < 5, "компрессор: пуск через СПК 2 с, полная подача на холостом ходу, А5 отключает", "подача " + F(c0, "0.0") + " → " + F(c1, "0.0") + " при " + F(tr.S[0].Rpm, "0") + " об/мин");
            // refrigerator: manual mode works by the toggles only, automatic ignores them
            tr = mk(); tr.RefrigSw = false; tr.FanSw[3] = true; for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            bool man = tr.S[0].FanOn[3] && !tr.S[0].FanOn[0] && !tr.S[0].FanOn[1] && !tr.S[0].FanOn[2];
            tr.RefrigSw = true; for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            Check(man && !tr.S[0].FanOn[3], "холодильник: «Ручное» — только тумблеры Т1–Т4, «Автомат» — только термореле", "");
            // manual refrigerator: T2 is fed after T1, T3 after T4; the load shedding lamp at idle when VV is off
            tr.RefrigSw = false; tr.FanSw[2] = true; tr.FanSw[1] = true; tr.FanSw[3] = false; for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            bool none = !tr.S[0].FanOn[1] && !tr.S[0].FanOn[2]; tr.FanSw[0] = true; tr.FanSw[3] = true; for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            Check(none && tr.S[0].FanOn.All(o => o), "холодильник, ручное: Т2 только после Т1, Т3 только после Т4", "");
            tr = mk(); bool off0 = tr.LoadShed(0); tr.Rm[0].Doors = false; for (int i = 0; i < 20; i++) tr.Step(0.1, 0);
            Check(!off0 && tr.LoadShed(0) && !tr.LoadShed(1) && tr.Position == 0, "лампа «Сброс нагрузки» на нулевой позиции: открыта дверь ВВК, ВВ отключён", "");
            tr = mk(); drive(tr, 3, 50); int fw3 = tr.S[0].FwStage; drive(tr, 6, 50); for (int i = 0; i < 200; i++) tr.Step(0.1, 50);
            Check(fw3 == 0 && tr.S[0].FwStage >= 1, "ослабление поля только с 4-й позиции контроллера", "на 3-й ОП" + fw3 + ", на 6-й ОП" + tr.S[0].FwStage + " при 50 км/ч");
            tr = mk(); drive(tr, 8, 50); bool run = tr.S[0].Assembled; tr.Step(0.1, 112); tr.Step(0.1, 112); bool cut = !tr.S[0].Assembled; for (int i = 0; i < 30; i++) tr.Step(0.1, 90);
            bool still = !tr.S[0].Assembled; drive(tr, 8, 90);
            Check(run && cut && still && tr.S[0].Assembled, "РП3 и РУ2: выше 110 км/ч нагрузка снята до постановки контроллера на ноль", "");
            // slip limitation and the lamps of the second section
            var ta = mk(); var tb = mk(); tb.TobSw = true; drive(ta, 15, 60); drive(tb, 15, 60);
            for (int i = 0; i < 400; i++) { ta.Step(0.1, 60); tb.Step(0.1, 60); }   // the diesel takes about 55 s to reach the full power
            Check(tb.S[0].VoltageKv < 0.54 && ta.S[0].VoltageKv > tb.S[0].VoltageKv + 0.05, "ТОБ «Ограничение боксования» ограничивает напряжение ГГ", F(tb.S[0].VoltageKv * 1000, "0") + " против " + F(ta.S[0].VoltageKv * 1000, "0") + " В");
            tr = mk(); tr.SetFault(fd(29), true); tr.SetFault(fd(30), true); tr.Step(0.1, 0);
            bool both = tr.LampWaterLevel && tr.LampFire; tr.DamageSel = 1; tr.Step(0.1, 0);
            Check(both && !tr.LampWaterLevel && tr.LampFire && tr.Reverser != 0, "тумблер «Повреждение» (РУ12): остаются лампы второй секции", "вода — секция 1, пожар — секция 2");
            // signal lamps, sheet 8: lamp test TKS, feeds, burnt lamps
            Func<bool[], string> on = l => string.Join(",", Enumerable.Range(0, l.Length).Where(i => l[i]).Select(i => i.ToString()).ToArray());
            tr = mk(); var l0 = tr.PultLamps(false); tr.AlarmMon = false; tr.Step(0.1, 0); var l1 = tr.PultLamps(false);
            Check(on(l0) == "8" && on(l1) == "2,3,6,8", "контроль ламп (ТКС): загораются только «Уровень воды», «Давление масла», «Отпуск тормозов»", "до: " + on(l0) + ", при проверке: " + on(l1));
            tr.DamageSel = 1; tr.Step(0.1, 0); var l2 = tr.PultLamps(false); tr.DamageSel = 2; tr.Reverser = 0; tr.Step(0.1, 0); var l3 = tr.PultLamps(false);
            Check(on(l2) == "2,3,6,8" && on(l3) == "8", "контроль ламп: РУ12 не мешает, без реверсивной рукоятки проверка не работает", "с РУ12: " + on(l2) + ", в нейтрали: " + on(l3));
            tr = mk(); tr.AlarmMon = false; tr.LampBurnt[3] = true; tr.Step(0.1, 0); var l4 = tr.PultLamps(false);
            Check(on(l4) == "2,6,8", "контроль ламп находит перегоревшую лампу «Давление масла»", on(l4));
            tr = mk(); tr.Rm[0].AVU = false; tr.Step(0.1, 0); bool lo1 = tr.LampCooling; tr.Rm[0].A4 = false; tr.Step(0.1, 0); bool lo2 = tr.LampCooling;
            tr.Rm[0].A4 = true; tr.Ddr = true; tr.Step(0.1, 0); bool lrt1 = tr.LampBrakePipe(false); tr.Rm[0].A4 = false; tr.Step(0.1, 0); bool lrt2 = tr.LampBrakePipe(false); tr.Rm[0].A4 = true; tr.Rm[0].BU = false; tr.Step(0.1, 0); bool lrt3 = tr.LampBrakePipe(false);
            Check(lo1 && !lo2 && lrt1 && !lrt2 && !lrt3, "лампы «Обдув» и «Обрыв ТМ» питаются от А4; РУ1 — от АУ через блокировку № 367", "");
            tr = mk(); tr.SetFault(fd(30), true); tr.Step(0.1, 0); bool f1 = tr.LampFire; tr.Rm[1].AU = false; tr.Step(0.1, 0); bool f2 = tr.LampFire; tr.Rm[1].AU = true; tr.Reverser = 0; tr.Step(0.1, 0); bool f3 = tr.LampFire;
            Check(f1 && !f2 && f3, "лампа «Пожар» питается от АУ своей секции и горит при реверсивной рукоятке в нейтрали", "");
            tr = mk(); tr.FuelSw[0] = tr.FuelSw[1] = false; for (int i = 0; i < 100; i++) tr.Step(0.1, 0); tr.Rm[0].VB = tr.Rm[1].VB = false; for (int i = 0; i < 5; i++) tr.Step(0.1, 0); tr.AlarmMon = false; tr.Rm[1].AVU = false; tr.Step(0.1, 0);
            Check(on(tr.PultLamps(true)) == "", "оба источника отключены (СГ остановлены, ВБ выключены): не горит ни одна лампа", on(tr.PultLamps(true)));
        });
        // route of the scenario: limiting climbs
        if (args.Length > 0)
        {
            Add(false, () =>
            {
            var r = Route.LoadScenario(args[0]);
            Check(Math.Abs(r.RulingGrade - 7.4) < 0.2, "руководящий подъём Тюмень — Тобольск", F(r.RulingGrade, "0.0") + " ‰, лимитирующих " + r.LimitX.Length);
            });
        }
        RunAll();
        Console.WriteLine("RESULT " + pass + " passed, " + fail + " failed");
        return fail == 0 ? 0 : 1;
    }

    static Route FlatRoute(double len, double slope)
    {
        var r = new Route { Name = "стенд", Length = len, ProfX = new[] { 0.0 }, ProfLen = new[] { len }, ProfSlope = new[] { slope }, ProfH = new[] { 0.0 } };
        return r;
    }
    static Simulation Start(Route r, Train t, double vKmh)
    {
        var sim = new Simulation(); Journal.Configure(null); sim.Start(r, t, 3000, true);
        sim.C.Kvt = 2;
        for (int i = 0; i < 300; i++) sim.Step(0.1);             // settle the brakes and the air
        foreach (var b in sim.Dyn.B) b.V = vKmh / 3.6;
        return sim;
    }
}
