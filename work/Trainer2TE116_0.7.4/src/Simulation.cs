// One trip: route, train, the new pneumatics and longitudinal dynamics, traction, controls of the cab, signals, faults,
// the journal of violations, the brake test in motion and the cassette. Frames of the instructor (0.1 s) are split into
// substeps of 2 ms for the air and the couplers.
// Braking force of a body by PTR (as the earlier model, checked against Rules app. 4 within 1-2 %):
//   F = g Kp phi_kp(v) S(x), x = cylinder / cylinder of the mode, S(x) = x phi_k(x Kact) / phi_k(Kact), not above 0.18 of the weight.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;

namespace Trainer2TE116
{
    public sealed class Controls
    {
        public int Position, Reverser = 1, Crane = 2, Kvt = 6;
        public bool EpkKey = true, Sand, Typhon, Whistle, Spotlight = true, Vigilance;
        // switches of the pult: "locomotive control", "motion", field weakening (conversion), refrigerator auto mode, alarm
        // monitoring; manual cooling fans 1-4; damage indicator (section 1/2, damage 1/2)
        public bool LocoControl = true, Motion = true, Conversion = true, Refrigerator = true, AlarmMonitoring = true;
        public bool[] FanSw = new bool[4];
        public int DamageSel = 2, DamageSw;
        public bool Tob;       // toggle "slip limitation" (TOB, relay RU18)
        // diesels from the pult: fuel pump of each section (off stops the diesel), requests "start" and "emergency stop"
        public bool[] FuelPump = { true, true };
        public bool[] Launch = { false, false };
        public bool EmergencyStop;
        public bool PultFirst;      // set by the first packet of the pult of a trip
        public bool EmergencyHeld, StartHeld1, StartHeld2, SandAxle, ReleaseButton, CallButton, Maneuver, AutoStop = true;
    }

    public static class PtrBrake
    {
        public static double PhiCast(double v) { v = Math.Abs(v); return 0.27 * (v + 100) / (5 * v + 100); }
        public static double PhiComposite(double v) { v = Math.Abs(v); return 0.36 * (v + 150) / (2 * v + 150); }
        static double PadFactor(bool comp, double k) { return comp ? 0.44 * (k + 20) / (4 * k + 20) / 0.36 : 0.6 * (16 * k + 100) / (80 * k + 100) / 0.27; }
        public static double ActualPad(bool comp, double kpPad)
        {
            if (kpPad <= 0) return 0;
            double lo = 0, hi = 50;
            for (int it = 0; it < 60; it++) { double mid = 0.5 * (lo + hi); if (mid * PadFactor(comp, mid) < kpPad) lo = mid; else hi = mid; }
            return 0.5 * (lo + hi);
        }
        public static double Share(bool comp, double kact, double x)
        {
            if (x <= 0 || kact <= 0) return 0;
            x = Math.Min(x, 1.25);
            return x * PadFactor(comp, x * kact) / PadFactor(comp, kact);
        }
        // N
        public static double Force(double tcMPa, double refMPa, double kpTotalTf, double kactPad, bool comp, double massT, double vKmh)
        {
            if (tcMPa <= 0.005 || refMPa <= 0) return 0;
            double phi = comp ? PhiComposite(vKmh) : PhiCast(vKmh);
            double kn = 9.81 * kpTotalTf * phi * Share(comp, kactPad, tcMPa / refMPa);
            return Math.Min(kn, 0.18 * massT * 9.81) * 1000.0;
        }
    }

    public sealed class Fault
    {
        public string Group, Name; public int Code; public bool On;
        public override string ToString() { return Name; }
    }

    public sealed class Simulation
    {
        public Route Route; public Train Train;
        public readonly Pneumatics Air = new Pneumatics();
        public readonly Dynamics Dyn = new Dynamics();
        public readonly Traction Tr = new Traction();
        public readonly Controls C = new Controls();
        public Cassette Cassette;
        public double T, Distance, SubStep = 0.002, Allowed = 80, TargetAhead = -1;
        public bool Paused, Finished;
        public string Student = "", Instructor = "", TrainNumber = "2001";
        // technical condition of the diesels (nominal | normal | worn | random | percent) and the outside air
        public string DieselCondition = "normal"; public int DieselSeed; public double AmbientC = 20;
        public DateTime Started;
        public readonly List<string> Events = new List<string>();
        public readonly List<Fault> Faults = new List<Fault>();
        double[] kact; int obstacleHit = -1; bool locoVrPrev;
        // rules of the present instructor (LogManager, Te116Logic): signal before the start, spotlight, whistle at the sign
        // "С", EPK off in motion, emergency braking, stop at a permissive signal
        bool stoppedFlag = true, emergencyLogged, epkLogged; double lastHornT = -1e9, lastHornX = double.NaN, lastHead = double.NaN, prevV;
        double charge0;

        public static readonly string[] BrakeFaultNames =
        {
            "Прекращение подпитки ТМ в IV положении крана", "Повышение давления в УР во II положении крана", "Увеличение времени ликвидации сверхзарядного давления",
            "Занижение давления в УР в IV положении крана", "Ложное срабатывание СОТ", "Обрыв ТМ в голове поезда", "Обрыв ТМ в хвосте поезда",
            "Недостаточная производительность компрессоров", "Завышенное зарядное давление", "Заниженное зарядное давление",
            "Перекрыт концевой кран в середине состава", "Выключены воздухораспределители у четверти вагонов", "Неотпуск тормоза вагона (неисправный ВР)",
            "Повышенная утечка в тормозной сети состава"
        };
        public void Start(Route r, Train t, double headX, bool charged)
        {
            Route = r; Train = t;
            Dyn.Build(t, headX);
            Air.Build(t, charged ? t.ChargeMPa : 0);
            Air.ExternalCompressorControl = true;
            charge0 = Air.ChargeMPa;
            Tr.SectionMassT = t.SectionMassT; Tr.Reverser = C.Reverser; Tr.Ambient = AmbientC;
            Tr.Log = text => Log(text);
            Tr.Init(t.Sections, DieselCondition, DieselSeed);
            kact = new double[Dyn.B.Length];
            for (int i = 0; i < Dyn.B.Length; i++)
            {
                if (i < t.Sections) kact[i] = PtrBrake.ActualPad(false, t.LocoKpAxleTf * t.SectionAxles / 24.0);
                else { var w = t.Wagons[i - t.Sections]; kact[i] = PtrBrake.ActualPad(w.Composite, w.KpAxleTf * w.Axles / 8.0); }
            }
            Faults.Clear();
            for (int i = 0; i < BrakeFaultNames.Length; i++) Faults.Add(new Fault { Group = "Тормоза", Name = BrakeFaultNames[i], Code = i + 1 });
            foreach (var d in Traction.FaultList) Faults.Add(new Fault { Group = "Электрика", Name = d.Name, Code = d.Code });
            Dyn.CouplerBroken += k => { Air.BreakAt = Math.Max(0, k - t.Sections); Log(string.Format(CultureInfo.InvariantCulture, "Обрыв автосцепки за {0}", k < t.Sections ? "секцией " + (k + 1) : "вагоном " + (k - t.Sections + 1))); };
            T = 0; Distance = 0; Finished = false; Started = DateTime.Now; Events.Clear(); obstacleHit = -1; locoVrPrev = false;
            Journal.Reset();
            Journal.SetRoute(r, t.MassT, t.WagonAxles);
            BrakeTest.Init();
            BrakeTest.EmptyTrain = t.IsEmpty; BrakeTest.Axles = t.WagonAxles; BrakeTest.Charge = Air.ChargeMPa;
            BrakeTest.SignCoord = r.Signs.Select(s => (int)s.X).ToArray(); BrakeTest.SignType = r.Signs.Select(s => s.Type).ToArray();
            Tr.AdhesionFactor = r.Snow ? 0.8 : r.Rain ? 0.85 : 1;
            Log("Поездка: " + r.Name + ", " + t + ", зарядное давление " + (Air.ChargeMPa * Traction.KgfPerMPa).ToString("0.0", CultureInfo.InvariantCulture) + " кгс/см²");
            Log("Состояние дизелей: " + Tr.ConditionNote);
            TripLog = AppLog.Enabled ? new TripLog(this) : null;
        }

        public void Log(string text)
        {
            string line = TimeSpan.FromSeconds(T).ToString(@"hh\:mm\:ss") + "  " + text;
            lock (Events) Events.Add(line);
            if (AppLog.Enabled) AppLog.Write(Section(text), line + string.Format(CultureInfo.InvariantCulture, "  [v={0:0.0} км/ч, голова {1:0} м]", Dyn.Speed * 3.6, Dyn.HeadX));
            if (Cassette != null) Cassette.Event(T, Dyn.HeadX, text);
        }

        // the section of the journal of the program for an event of the trip
        static string Section(string text)
        {
            if (text.StartsWith("Нарушение")) return "НАРУШЕНИЕ";
            foreach (var k in new[] { "Неисправность", "Обрыв", "ЭПК", "Столкновение", "Экстренное", "Препятствие", "Короткое замыкание", "ожар", "Схема", "варийн", "автомат", "остановлен", "Боксование", "не запус", "защит", "сброс нагрузки" })
                if (text.IndexOf(k, StringComparison.OrdinalIgnoreCase) >= 0) return "НЕШТАТКА";
            if (text.IndexOf("тормоз", StringComparison.OrdinalIgnoreCase) >= 0) return "ТОРМОЗА";
            return "СОБЫТИЕ";
        }
        public TripLog TripLog;

        public void SetFault(Fault f, bool on)
        {
            f.On = on;
            int n = Air.W.Length;
            switch (f.Code)
            {
                case 6: Air.BreakAt = on ? 0 : -1; break;
                case 7: Air.BreakAt = on ? Math.Max(0, n - 2) : -1; break;
                case 8: Air.CompressorFactor = on ? 0.45 : 1; break;
                case 9: Air.ChargeMPa = on ? charge0 + 0.03 : charge0; break;
                case 10: Air.ChargeMPa = on ? charge0 - 0.03 : charge0; break;
                case 11: Air.EndValveClosedAfter = on ? n / 2 : -1; break;
                case 12: for (int i = 0; i < n; i++) Air.W[i].VrOn = on ? i % 4 != 1 : Train.Wagons[i].VrOn; break;
                case 13: if (n > 0) Air.W[n / 3].Stuck = on; break;
                case 14: Air.LeakFactor = on ? 3 : 1; break;
                case 103:
                    foreach (var e in Tr.Electric.Get("Компрессор", 1).Coils) e.ExternalOpen = on;
                    break;
                default:
                    foreach (var d in Traction.FaultList) if (d.Code == f.Code) Tr.SetFault(d, on);
                    break;
            }
            Log((on ? "Неисправность введена: " : "Неисправность снята: ") + f.Name);
            if (TripLog != null) TripLog.OnFault(f, on);
        }

        // allowed speed for the driver now: the map, the aspect of the next signal, and a braking curve to a red one
        public double AllowedAt(double head, double v)
        {
            double lim = Route.StaticLimitAt(head);
            int nl = Route.NextLight(head);
            if (nl >= 0)
            {
                var l = Route.Lights[nl];
                int al = Route.AspectLimit(l.Aspect);
                double d = l.X - head - 50;
                if (al == 0) lim = Math.Min(lim, d <= 0 ? 0 : Math.Min(lim, 3.6 * Math.Sqrt(2 * 0.25 * d)));
                else if (al < lim && l.X - head < 1500) lim = Math.Min(lim, 3.6 * Math.Sqrt(al * al / 12.96 + 2 * 0.25 * Math.Max(0, d)));
            }
            return Math.Max(0, lim);
        }

        public void Step(double frame)
        {
            if (Paused || Finished) return;
            // the first packet of the pult at the very start: the diesels stand as the fuel pump toggles of the pult say;
            // a pult that comes on line later stops them the usual way, through the scheme
            if (C.PultFirst) { C.PultFirst = false; if (T < 2) for (int i = 0; i < 2; i++) if (!C.FuelPump[i]) Tr.BeginStopped(i); }
            int n = Math.Max(1, (int)Math.Round(frame / SubStep));
            double dt = frame / n;
            Air.Crane = C.Crane;
            Air.Kvt = C.Kvt;
            Controls();
            if (!C.EpkKey && Math.Abs(Dyn.Speed) > 0.5 && !Air.EpkTripped) { Air.EpkTripped = true; Log("ЭПК: ключ выключен на ходу — экстренное торможение"); }
            if (C.EpkKey && Air.EpkTripped && Math.Abs(Dyn.Speed) < 0.1 && C.Crane == 6) { Air.EpkTripped = false; epkT = 7; }
            // holding coil of the EPK (from BLOK): without it the EPK whistles 7 s, then trips
            if (C.EpkKey && !Air.EpkTripped && !HoldingCoil)
            {
                EpkWhistle = true; epkT -= frame;
                if (epkT <= 0) { Air.EpkTripped = true; EpkWhistle = false; Log("ЭПК: свисток 7 с без подтверждения бдительности — срыв, экстренное торможение"); }
            }
            else { EpkWhistle = false; if (HoldingCoil) epkT = 7; }
            double vKmh = Dyn.Speed * 3.6;
            Tr.Step(frame, vKmh);
            Air.EmergencyVent = Tr.EstopLatch;
            Air.ReleaseButton = Tr.Electric.On("ВОТ", 1) || Tr.Electric.On("ВОТ", 2);
            for (int s = 0; s < Tr.S.Length && s < Air.RpmShare.Length; s++) Air.RpmShare[s] = Tr.CompShare(s);   // the compressor motor is fed by the starter-generator, not by the shaft
            int rv = Tr.ReverserEff, dir = rv == 2 ? -1 : rv == 1 ? 1 : 0;
            for (int k = 0; k < n; k++)
            {
                Air.Step(dt);
                for (int i = 0; i < Dyn.B.Length; i++)
                {
                    var b = Dyn.B[i];
                    double v = b.V * 3.6;
                    if (i < Train.Sections)
                    {
                        b.Traction = Tr.Direction(i) * (i < Tr.S.Length ? Tr.S[i].Force : 0) * 1000.0;
                        b.Brake = PtrBrake.Force(Air.TcLoco[i].MPa, Air.LocoTcMax, Train.LocoKpAxleTf * Train.SectionAxles, kact[i], false, Train.SectionMassT, v);
                    }
                    else
                    {
                        var w = Train.Wagons[i - Train.Sections];
                        b.Brake = PtrBrake.Force(Air.W[i - Train.Sections].Tc.MPa, w.ModeCylinderMPa, w.KpAxleTf * w.Axles, kact[i], w.Composite, w.MassT, v);
                    }
                }
                if (k % 5 == 0) Dyn.SetTrackForces(Route, i => i < Train.Sections && i < Tr.S.Length && Tr.S[i].Force > 1);
                double before = Dyn.HeadX;
                Dyn.Step(dt);
                Distance += Math.Abs(Dyn.HeadX - before);
            }
            T += frame;
            double head = Dyn.HeadX, vNow = Dyn.Speed * 3.6;
            Allowed = AllowedAt(head, Math.Abs(vNow));
            // obstacles the instructor made active
            for (int i = 0; i < Route.Obstacles.Count; i++)
            {
                var o = Route.Obstacles[i];
                if (o.Active && obstacleHit != i && !double.IsNaN(lastHead) && lastHead < o.X && head >= o.X)
                { obstacleHit = i; Log("Столкновение: " + o.Name + " на " + Route.KmPk(o.X)); Journal.AddExternal("S4", true, "Столкновение с препятствием: " + o.Name, Route.KmPk(o.X), "ПТЭ", T, head, Math.Abs(vNow)); }
            }
            OriginalRules(head, Math.Abs(vNow));
            lastHead = head;
            if (head >= Route.Length - 5) { Finished = true; Log("Конец маршрута"); }
            // journal and brake test
            BrakeTest.Step(frame, vNow, head, Air.Ur.MPa, C.Crane);
            if (TripLog != null) TripLog.Step();
            var tailTc = Air.TailTcMPa;
            int cAt; double cT = MaxCouplerKn(out cAt, true); int cAtC; double cC = MaxCouplerKn(out cAtC, false);
            Journal.Feed(new JournalInput
            {
                T = T, Dt = frame, X = head, V = Math.Abs(vNow), VSigned = vNow * (dir == 0 ? 1 : dir), Ur = Air.Ur.MPa, Tm = Air.TmLoco.MPa, LocoTc = Air.LocoTcMPa,
                TailTc = tailTc, Charge = Air.ChargeMPa, TractionKn = Tr.TotalForceKn, MeanSlope = Route.MeanSlope(head, Dyn.LengthM), AllowedKmh = Allowed,
                AmbientC = AmbientC, MassT = Train.MassT, Crane = C.Crane, Controller = C.Position, Reverser = C.Reverser == 0 ? 1 : C.Reverser, Axles = Train.WagonAxles,
                Sanding = Tr.Sanding, Empty = Train.IsEmpty, EpkTripped = Air.EpkTripped, BrakeTestActive = BrakeTest.Active, Aspects = Route.Aspects,
                CouplerKn = cT >= cC ? cT : -cC, CouplerAt = cT >= cC ? cAt : cAtC
            });
            if (Cassette != null) Cassette.Frame(this);
        }

        // BLOK: holding coil of the EPK (true without BLOK), its lamps for the pult
        public bool HoldingCoil = true, EpkWhistle, LampVigilance, LampPss; double epkT = 7, tc254Prev;
        // sounds for the video system, bit = index of the sound (VideoDataReceiveController.SoundIdx), as the package model
        public ulong SoundMask
        {
            get
            {
                ulong m = 0; Action<int, bool> bit = (k, on) => { if (on) m |= 1UL << k; };
                var a = Air; double tm = a.TmLoco.MPa, ur = a.Ur.MPa;
                bool comp = false, pump = false;
                for (int i = 0; i < Tr.Sections; i++) { if (Tr.CompShare(i) > 0) comp = true; if (Tr.OilPumpOn(i)) pump = true; }
                bit(0, C.Whistle); bit(1, C.Typhon || Tr.Electric.On("ВТ", 1) || Tr.Electric.On("ВТ", 2)); bit(2, Tr.Sanding); bit(3, comp); bit(4, Tr.FuelPumpOn(0)); bit(5, pump);
                bool sec2Heard = !Tr.S[0].Running;
                for (int i = 0; i < Tr.Sections; i++)
                {
                    var s = Tr.S[i];
                    if (i > 0 && !sec2Heard) continue;
                    if (s.Running) bit((i == 0 ? 6 : 22) + Tr.SoundPos(i), true);
                    if (s.State == DieselState.Cranking) bit(i == 0 ? 43 : 45, true);
                }
                bit(39, Tr.S[0].ShutW); bit(41, Tr.S[0].ShutO);
                bit(47, a.Tc254MPa > tc254Prev + 1e-5 && C.Kvt >= 3); bit(48, C.Kvt == 1);
                bit(49, C.Crane == 1 && a.Mr.MPa > ur); bit(50, C.Crane == 2 && ur > 0.45);
                double deficit = a.ChargeMPa - ur;
                bit(51, C.Crane == 2 && deficit > 0.05 && deficit <= 0.1); bit(52, C.Crane == 2 && deficit > 0.1 && deficit <= 0.3); bit(53, C.Crane == 2 && deficit > 0.3);
                bit(54, C.Crane == 7 && tm > 0.05); bit(55, C.Crane == 5 && tm > 0.05);
                bit(56, (C.Crane == 6 || a.EpkTripped || Tr.EstopLatch) && tm > 0.05);
                bit(57, EpkWhistle); bit(58, EpkWhistle); bit(59, a.EpkTripped && tm > 0.05);
                tc254Prev = a.Tc254MPa;
                return m;
            }
        }

        // the cab controls and the air to the model of the diesels and the transmission
        void Controls()
        {
            Tr.Position = C.Position; Tr.Reverser = C.Reverser; Tr.Crane = C.Crane; Tr.Kvt = C.Kvt;
            Tr.EpkKey = C.EpkKey; Tr.LocoControl = C.LocoControl; Tr.MotionSw = C.Motion; Tr.ConvSw = C.Conversion; Tr.RefrigSw = C.Refrigerator;
            Tr.TobSw = C.Tob; Tr.AlarmMon = C.AlarmMonitoring; Tr.SandBtn = C.Sand; Tr.DamageSel = C.DamageSel; Tr.DamageSw = C.DamageSw;
            for (int i = 0; i < 4; i++) Tr.FanSw[i] = C.FanSw[i];
            for (int i = 0; i < 2; i++) { Tr.FuelSw[i] = C.FuelPump[i]; if (C.Launch[i]) { Tr.LaunchPress[i] = true; C.Launch[i] = false; } }
            if (C.EmergencyStop) { Tr.EstopPress = true; C.EmergencyStop = false; }
            Tr.TmMPa = Air.TmLoco.MPa; Tr.LocoTcMPa = Air.LocoTcMPa; Tr.Tc254MPa = Air.Tc254MPa; Tr.EpkTripped = Air.EpkTripped;
            Tr.CompressorRunning = Air.CompressorOn; Tr.LocoVrBraking = Air.LocoVrBraking;
            Tr.Ddr = Air.LocoVrBraking && !locoVrPrev;
            locoVrPrev = Air.LocoVrBraking;
            Tr.ReleaseButton = C.ReleaseButton; Tr.SandAxle = C.SandAxle; Tr.CallButton = C.CallButton;
            Tr.Maneuver = C.Maneuver; Tr.AutoStop = C.AutoStop; Tr.SpotOn = C.Spotlight;
            Tr.EmergencyHeld = C.EmergencyHeld; Tr.StartHeld1 = C.StartHeld1; Tr.StartHeld2 = C.StartHeld2;
        }

        void OriginalRules(double head, double v)
        {
            if (C.Typhon || C.Whistle) { lastHornT = T; lastHornX = head; }
            // start: a sound signal within 15 s and the spotlight on
            if (stoppedFlag && v > 1)
            {
                stoppedFlag = false;
                if (T - lastHornT > 15) Journal.AddExternal("O1", false, "Трогание без подачи звукового сигнала", "", "ПТЭ, приложение 6; правило инструктора RTS", T, head, v);
                if (!Tr.SpotlightOn) Journal.AddExternal("O2", false, "Движение без включённого прожектора", "", "ПТЭ; правило инструктора RTS", T, head, v);
            }
            if (v < 0.3 && prevV >= 0.3)
            {
                stoppedFlag = true;
                int nl = Route.NextLight(head);
                if (nl >= 0 && Route.Lights[nl].X - head < 400 && !Route.IsProhibiting(Route.Lights[nl].Aspect) && Route.AspectLimit(Route.Lights[nl].Aspect) >= 60 && !Finished)
                    Journal.AddExternal("O6", false, "Остановка у светофора " + Route.Lights[nl].Name + " с разрешающим показанием", Route.AspectName(Route.Lights[nl].Aspect), "правило инструктора RTS", T, head, v);
            }
            prevV = v;
            // whistle at the sign "С": between 300 m before it and the sign
            if (!double.IsNaN(lastHead) && head > lastHead)
                lock (Route.Signs) foreach (var sg in Route.Signs)
                    if (sg.Type == Route.SignWhistle && lastHead < sg.X && head >= sg.X && v > 3)
                        if (double.IsNaN(lastHornX) || lastHornX < sg.X - 300)
                            Journal.AddExternal("O3", false, "Не подан свисток у знака «С»", Route.KmPk(sg.X), "ПТЭ, приложение 6; правило инструктора RTS", T, head, v);
            if (!C.EpkKey && v > 0.5 && !epkLogged) { epkLogged = true; Journal.AddExternal("O4", true, "Выключение ЭПК на ходу", "", "Инструкция Л230; правило инструктора RTS", T, head, v); }
            if (C.EpkKey) epkLogged = false;
            if (C.Crane == 6 && v > 1 && !emergencyLogged) { emergencyLogged = true; Log("Экстренное торможение краном машиниста"); Journal.AddExternal("O5", false, "Экстренное торможение", "кран 395 в VI положении на " + v.ToString("0", CultureInfo.InvariantCulture) + " км/ч", "правило инструктора RTS", T, head, v); }
            if (C.Crane != 6) emergencyLogged = false;
        }

        // ---- readings for the windows and the documents
        public double SpeedKmh { get { return Dyn.Speed * 3.6; } }
        public double MaxCouplerKn(out int at, bool tension)
        {
            at = -1; double m = 0;
            for (int i = 0; i < Dyn.Cp.Length; i++) { double f = tension ? Dyn.Cp[i].Force : -Dyn.Cp[i].Force; if (f > m) { m = f; at = i; } }
            return m / 1000.0;
        }
    }
}
