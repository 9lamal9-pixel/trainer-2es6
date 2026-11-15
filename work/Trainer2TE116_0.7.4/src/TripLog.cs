// The journal (AppLog) of the brakes of the train and of the abnormal situations of a trip. One object per trip, Step is
// called after the steps of the model. Sections of the journal:
//  ТОРМОЗА  - every change of the cranes 395 / 254, of the EPK, the compressors, the lamp "ТМ", the air distributor of the
//             locomotive; the state of every wagon (charged, braking, emergency, releasing, distributor off) when it
//             changes; the brake wave and the release along the train with their times; every braking with its speed,
//             path, time, depth of the discharge and the highest cylinder pressure; the pressures, the feed of the pipe,
//             the brake force and the forces in the couplers every 2 s while the brakes act and every 30 s otherwise,
//             with the table of all the wagons (pipe / reservoir / cylinder);
//  НЕШТАТКА - the faults put in and taken off by the instructor with what they do in the model and the state of the
//             train at that moment, the breaks of the pipe and of the couplers, the closed end valve, and every 10 s the
//             list of the acting faults with the state of the diesels and the lamps of the pult.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;

namespace Trainer2TE116
{
    public sealed class TripLog
    {
        static readonly CultureInfo C = CultureInfo.InvariantCulture;
        // This state observes the cylinder, not full recharge of the supply and working chambers.
        static readonly string[] StateNames = { "отпущен", "торможение", "экстренное", "отпуск", "ВР выключен" };
        static readonly string[] CraneNames = { "?", "I (отпуск и зарядка)", "II (поездное)", "III (перекрыша без питания)", "IV (перекрыша с питанием)", "V (служебное)", "VI (экстренное)", "Vа (замедленное служебное)" };
        static readonly string[] KvtNames = { "?", "1 (отпуск тормоза локомотива)", "2 (поездное)", "3 (1-я ступень)", "4 (2-я ступень)", "5 (3-я ступень)", "6 (полное)" };

        readonly Simulation sim;
        bool first = true;
        int crane, kvt, breakAt, endValve; bool epk, vent, comp, lamp, locoVr, locoRel, release, tcCut, epkKey;
        double leak, charge, compFactor;
        byte[] st = new byte[0]; double statesAt = -1; readonly List<int>[] changed = { new List<int>(), new List<int>(), new List<int>(), new List<int>(), new List<int>() };
        double waveT = -1, releaseT = -1; bool waveDone, releaseDone;
        // a braking: from the first cylinder filled to the release or the stop
        bool braking; double b0T, b0X, b0V, b0Tm, b0Ur, bUrMin, bTmMin, bTcMax, bTcLocoMax, bForceMax, bPushMax, bPullMax; bool bEmergency, bStopped;
        double tableAt = -1e9, wagonsAt = -1e9, faultsAt = -1e9; bool wasActive; double lastT = -1;
        readonly Dictionary<int, double> faultSince = new Dictionary<int, double>();
        bool[] broken = new bool[0];

        public TripLog(Simulation s) { sim = s; }

        static string P(double mpa) { return (mpa * Traction.KgfPerMPa).ToString("0.00", C); }   // kgf/cm2, as the gauges of the pult
        string Place() { try { return sim.Route.KmPk(sim.Dyn.HeadX); } catch { return sim.Dyn.HeadX.ToString("0", C) + " м"; } }
        string Now() { return string.Format(C, "t={0:0.0} с, {1}, v={2:0.0} км/ч", sim.T, Place(), sim.SpeedKmh); }
        static int StateOf(WagonAir w) { return !w.VrOn ? 4 : w.Braking ? (w.Emergency ? 2 : 1) : w.Tc.MPa > 0.02 ? 3 : 0; }
        // 1,2,3,7,9,10 -> "1–3, 7, 9–10" (numbers of the wagons from 1)
        static string Ranges(List<int> idx)
        {
            var sb = new StringBuilder();
            for (int i = 0; i < idx.Count; i++)
            {
                int j = i; while (j + 1 < idx.Count && idx[j + 1] == idx[j] + 1) j++;
                if (sb.Length > 0) sb.Append(", ");
                sb.Append(idx[i] + 1); if (j > i) sb.Append('–').Append(idx[j] + 1);
                i = j;
            }
            return sb.ToString();
        }
        string Pressures()
        {
            var a = sim.Air; var sb = new StringBuilder();
            sb.AppendFormat(C, "ГР {0}, ПР {1}, УР {2}, ТМ лок. {3}, ТМ хвост {4} (перепад {5}), ТЦ лок.", P(a.Mr.MPa), P(a.Pr.MPa), P(a.Ur.MPa), P(a.TmLoco.MPa), P(a.TailTmMPa), P(a.TmLoco.MPa - a.TailTmMPa));
            for (int i = 0; i < a.TcLoco.Length; i++) sb.Append(i == 0 ? " " : "/").Append(P(a.TcLoco[i].MPa));
            sb.AppendFormat(C, ", ТЦ хвост {0} кгс/см²", P(a.TailTcMPa));
            return sb.ToString();
        }

        // ---------- the faults (called by Simulation.SetFault after the fault is applied)
        public void OnFault(Fault f, bool on)
        {
            if (!AppLog.Enabled) return;
            var a = sim.Air; int n = a.W.Length; string what;
            switch (f.Code)
            {
                case 1: case 2: case 3: case 4: case 5: what = "в модели действия нет: неисправность только отмечается в журнале и протоколе"; break;
                case 6: what = on ? "ТМ открыта в атмосферу у вагона 1 (полное сечение трубы)" : "обрыв убран"; break;
                case 7: what = on ? "ТМ открыта в атмосферу у вагона " + (Math.Max(0, n - 2) + 1) + " из " + n : "обрыв убран"; break;
                case 8: what = "подача компрессоров ×" + a.CompressorFactor.ToString("0.00", C); break;
                case 9: case 10: what = "зарядное давление (уставка УР во II положении) " + P(a.ChargeMPa) + " кгс/см²"; break;
                case 11: what = on ? "концевой кран перекрыт за вагоном " + (n / 2 + 1) + " из " + n + ": дальше ТМ не сообщается с головой" : "концевой кран открыт"; break;
                case 12: { var off = new List<int>(); for (int i = 0; i < n; i++) if (!a.W[i].VrOn) off.Add(i); what = "ВР выключены у вагонов: " + (off.Count > 0 ? Ranges(off) + " (всего " + off.Count + " из " + n + ")" : "нет"); break; }
                case 13: what = n > 0 ? "вагон " + (n / 3 + 1) + ": ВР " + (on ? "не отпускает" : "исправен") : "вагонов нет"; break;
                case 14: what = "утечки ТМ и ЗР вагонов ×" + a.LeakFactor.ToString("0.#", C); break;
                case 103: what = on ? "цепь компрессора секции 1 разомкнута" : "цепь компрессора секции 1 восстановлена"; break;
                default: what = "электрическая неисправность, код " + f.Code; break;
            }
            if (on) faultSince[f.Code] = sim.T; else faultSince.Remove(f.Code);
            AppLog.Write("НЕШТАТКА", (on ? "ВВЕДЕНА" : "СНЯТА") + " [" + f.Group + ", код " + f.Code + "] «" + f.Name + "»: " + what + "; " + Now() + "; кран 395 " + a.Crane + ", 254 " + a.Kvt + ", позиция " + sim.C.Position + "; " + Pressures());
            faultsAt = sim.T + 2;   // the state two seconds later
        }

        void Faults()
        {
            var on = new List<string>();
            foreach (var f in sim.Faults) if (f.On) { double t0; on.Add("«" + f.Name + "» (код " + f.Code + (faultSince.TryGetValue(f.Code, out t0) ? ", " + (sim.T - t0).ToString("0", C) + " с" : "") + ")"); }
            var a = sim.Air;
            bool pipe = a.BreakAt >= 0 || a.EndValveClosedAfter >= 0;
            if (on.Count == 0 && !pipe) return;
            var sb = new StringBuilder();
            sb.Append("действуют: ").Append(on.Count > 0 ? string.Join("; ", on.ToArray()) : "нет введённых инструктором");
            if (a.BreakAt >= 0) sb.Append("; ТМ разорвана у вагона ").Append(a.BreakAt + 1);
            if (a.EndValveClosedAfter >= 0) sb.Append("; концевой кран перекрыт за вагоном ").Append(a.EndValveClosedAfter + 1);
            sb.Append("; ").Append(Now()).Append("; ").Append(Pressures());
            try
            {
                for (int i = 0; i < sim.Tr.Sections && i < sim.Tr.S.Length; i++)
                {
                    var s = sim.Tr.S[i];
                    sb.AppendFormat(C, "; секция {0}: дизель {1}, {2:0} об/мин, масло {3:0.0} кгс/см², ток ГГ {4:0} А", i + 1, new[] { "остановлен", "прокачка масла", "прокрутка", "работает", "останавливается" }[(int)s.State], s.Rpm, s.OilP * Traction.KgfPerMPa, s.Current);
                }
                var l = sim.Tr.PultLamps(a.Lamp418); var names = new[] { "сброс нагрузки 1", "сброс нагрузки 2", "уровень воды", "давление масла", "", "ТМ", "заторможено", "пожар", "дизель 2 работает", "охлаждение", "реле заземления", "", "" };
                var lit = new List<string>(); for (int i = 0; i < l.Length && i < names.Length; i++) if (l[i] && names[i].Length > 0) lit.Add(names[i]);
                sb.Append("; лампы пульта: ").Append(lit.Count > 0 ? string.Join(", ", lit.ToArray()) : "не горят");
            }
            catch { }
            AppLog.Write("НЕШТАТКА", sb.ToString());
        }

        // ---------- every tick of the window
        public void Step()
        {
            if (!AppLog.Enabled || sim == null || sim.Air == null) return;
            if (sim.T == lastT) return;   // the pause
            lastT = sim.T;
            try { Changes(); Wagons(); Braking(); Couplers(); Periodic(); }
            catch (Exception ex) { AppLog.Write("ТОРМОЗА", "запись пропущена: " + ex.Message); }
            first = false;
        }

        void Changes()
        {
            var a = sim.Air;
            if (first)
            {
                AppLog.Write("ТОРМОЗА", string.Format(C, "начало поездки: вагонов {0}, секций {1}, зарядное давление {2} кгс/см², компрессор включается при ГР {3} и выключается при {4} кгс/см²; кран 395 {5}, кран 254 {6}; {7}",
                    a.W.Length, a.Sections, P(a.ChargeMPa), P(a.RegOnMPa), P(a.RegOffMPa), CraneNames[Math.Max(0, Math.Min(7, a.Crane))], KvtNames[Math.Max(0, Math.Min(6, a.Kvt))], Pressures()));
                var off = new List<int>(); var mode = new int[4];
                for (int i = 0; i < a.W.Length; i++) { if (!a.W[i].VrOn) off.Add(i); mode[Math.Max(0, Math.Min(3, a.W[i].Mode))]++; }
                AppLog.Write("ТОРМОЗА", "состав: режимы ВР — порожний " + mode[1] + ", средний " + mode[2] + ", гружёный " + mode[3] + "; ВР выключены: " + (off.Count > 0 ? "вагоны " + Ranges(off) : "нет"));
            }
            else
            {
                string at = "; " + Now() + "; " + Pressures();
                if (a.Crane != crane) AppLog.Write("ТОРМОЗА", "кран 395: " + CraneNames[Math.Max(0, Math.Min(7, crane))] + " → " + CraneNames[Math.Max(0, Math.Min(7, a.Crane))] + at);
                if (a.Kvt != kvt) AppLog.Write("ТОРМОЗА", "кран 254: " + KvtNames[Math.Max(0, Math.Min(6, kvt))] + " → " + KvtNames[Math.Max(0, Math.Min(6, a.Kvt))] + at);
                if (a.ReleaseButton != release) AppLog.Write("ТОРМОЗА", "кнопка отпуска тормоза локомотива " + (a.ReleaseButton ? "нажата" : "отпущена") + at);
                if (sim.C.EpkKey != epkKey) AppLog.Write("ТОРМОЗА", "ключ ЭПК " + (sim.C.EpkKey ? "включён" : "выключен") + at);
                if (a.EpkTripped != epk) AppLog.Write(a.EpkTripped ? "НЕШТАТКА" : "ТОРМОЗА", (a.EpkTripped ? "срыв ЭПК: ТМ разряжается через срывной клапан" : "ЭПК восстановлен") + at);
                if (a.EmergencyVent != vent) AppLog.Write(a.EmergencyVent ? "НЕШТАТКА" : "ТОРМОЗА", (a.EmergencyVent ? "аварийная остановка: ТМ разряжается темпом экстренного торможения" : "аварийная разрядка ТМ прекращена") + at);
                if (a.CompressorOn != comp) AppLog.Write("ТОРМОЗА", "регулятор давления: компрессоры " + (a.CompressorOn ? "включены" : "выключены") + string.Format(C, " при ГР {0} кгс/см² (подача ×{1:0.00}); t={2:0.0} с", P(a.Mr.MPa), a.CompressorFactor, sim.T));
                if (a.Lamp418 != lamp) AppLog.Write("ТОРМОЗА", string.Format(C, "лампа «ТМ» (датчик 418) {0}: подпитка ТМ {1:0.0} г/с при утечках состава {2:0.0} г/с", a.Lamp418 ? "зажглась" : "погасла", a.LampFeedKgS * 1000, a.LeakRefKgS * 1000) + at);
                if (a.LocoVrBraking != locoVr) AppLog.Write("ТОРМОЗА", "ВР локомотива: " + (a.LocoVrBraking ? "торможение" : "отпуск") + at);
                if (a.LocoAutoReleased != locoRel) AppLog.Write("ТОРМОЗА", "тормоз локомотива " + (a.LocoAutoReleased ? "отпущен краном 254 при заторможенном поезде" : "снова следует за ВР") + at);
                if (a.LocoTcCut != tcCut) AppLog.Write("ТОРМОЗА", "ТЦ локомотива " + (a.LocoTcCut ? "отключены от реле давления (проверка плотности)" : "подключены к реле давления") + at);
                if (a.BreakAt != breakAt) AppLog.Write("НЕШТАТКА", (a.BreakAt >= 0 ? "ТМ разорвана у вагона " + (a.BreakAt + 1) + " из " + a.W.Length : "разрыв ТМ устранён") + at);
                if (a.EndValveClosedAfter != endValve) AppLog.Write("НЕШТАТКА", (a.EndValveClosedAfter >= 0 ? "концевой кран перекрыт за вагоном " + (a.EndValveClosedAfter + 1) : "концевой кран открыт") + at);
                if (a.LeakFactor != leak) AppLog.Write("ТОРМОЗА", "утечки тормозной сети состава ×" + a.LeakFactor.ToString("0.#", C) + at);
                if (a.ChargeMPa != charge) AppLog.Write("ТОРМОЗА", "зарядное давление: " + P(charge) + " → " + P(a.ChargeMPa) + " кгс/см²" + at);
                if (a.CompressorFactor != compFactor) AppLog.Write("ТОРМОЗА", "подача компрессоров: ×" + compFactor.ToString("0.00", C) + " → ×" + a.CompressorFactor.ToString("0.00", C) + at);
            }
            crane = a.Crane; kvt = a.Kvt; release = a.ReleaseButton; epkKey = sim.C.EpkKey; epk = a.EpkTripped; vent = a.EmergencyVent; comp = a.CompressorOn; lamp = a.Lamp418; locoVr = a.LocoVrBraking;
            locoRel = a.LocoAutoReleased; tcCut = a.LocoTcCut; breakAt = a.BreakAt; endValve = a.EndValveClosedAfter; leak = a.LeakFactor; charge = a.ChargeMPa; compFactor = a.CompressorFactor;
        }

        // the state of every wagon; the changes are gathered for half a second of the model so that a wave is a few lines
        void Wagons()
        {
            var a = sim.Air; int n = a.W.Length;
            if (st.Length != n) { st = new byte[n]; for (int i = 0; i < n; i++) st[i] = (byte)StateOf(a.W[i]); return; }
            var count = new int[5]; bool any = false;
            for (int i = 0; i < n; i++)
            {
                int s = StateOf(a.W[i]); count[s]++;
                if (s != st[i]) { st[i] = (byte)s; changed[s].Add(i); any = true; }
            }
            if (any && statesAt < 0) statesAt = sim.T;
            int on = n - count[4], brakingNow = count[1] + count[2];
            // the brake wave: from the first wagon that brakes to the last one
            if (brakingNow > 0 && waveT < 0) { waveT = sim.T; waveDone = false; releaseT = -1; }
            if (waveT >= 0 && !waveDone && on > 0 && brakingNow == on)
            {
                waveDone = true; double dt = sim.T - waveT, len = sim.Dyn.LengthM;
                AppLog.Write("ТОРМОЗА", string.Format(C, "тормозная волна дошла до хвоста: все {0} включённых ВР сработали за {1:0.0} с от первого (длина поезда {2:0} м{3}); {4}", on, dt, len, dt > 0.05 ? ", скорость волны " + (len / dt).ToString("0", C) + " м/с" : "", Pressures()));
            }
            if (waveT >= 0 && brakingNow < on && releaseT < 0 && count[3] > 0 && (waveDone || brakingNow == 0)) { releaseT = sim.T; releaseDone = false; AppLog.Write("ТОРМОЗА", "начался отпуск тормозов вагонов; " + Now() + "; " + Pressures()); }
            if (waveT >= 0 && brakingNow == 0 && count[3] == 0)
            {
                if (releaseT >= 0 && !releaseDone) AppLog.Write("ТОРМОЗА", string.Format(C, "тормоза всех вагонов отпущены (ТЦ ниже 0,2 кгс/см²) через {0:0.0} с после начала отпуска; {1}; {2}", sim.T - releaseT, Now(), Pressures()));
                releaseDone = true; waveT = -1; releaseT = -1;
            }
            if (statesAt >= 0 && sim.T - statesAt >= 0.5)
            {
                var sb = new StringBuilder("вагоны: ");
                for (int s = 0; s < 5; s++) if (changed[s].Count > 0) { changed[s].Sort(); sb.Append(Ranges(changed[s])).Append(" → ").Append(StateNames[s]).Append("; "); changed[s].Clear(); }
                sb.AppendFormat(C, "итого: тормозят {0}, из них экстренно {1}, отпускают {2}, отпущены {3}, ВР выключен {4}; t={5:0.0} с, ТМ лок. {6}, ТМ хвост {7} кгс/см²", brakingNow, count[2], count[3], count[0], count[4], sim.T, P(a.TmLoco.MPa), P(a.TailTmMPa));
                AppLog.Write("ТОРМОЗА", sb.ToString());
                statesAt = -1;
            }
        }

        double BrakeForceKn(out double loco)
        {
            double sum = 0; loco = 0;
            foreach (var b in sim.Dyn.B) { sum += b.Brake; if (b.Loco) loco += b.Brake; }
            loco /= 1000; return sum / 1000;
        }

        void Braking()
        {
            var a = sim.Air; double v = Math.Abs(sim.SpeedKmh);
            double tcMax = 0; foreach (var w in a.W) if (w.VrOn) tcMax = Math.Max(tcMax, w.Tc.MPa);
            double tcLoco = a.LocoTcMPa; bool act = tcMax > 0.03 || tcLoco > 0.03;
            if (!braking)
            {
                if (!act || first) { braking = act; if (act) Begin(v); return; }
                Begin(v);
                AppLog.Write("ТОРМОЗА", "торможение началось: " + Now() + "; кран 395 " + a.Crane + ", 254 " + a.Kvt + "; " + Pressures());
                return;
            }
            double loco, f = BrakeForceKn(out loco);
            bUrMin = Math.Min(bUrMin, a.Ur.MPa); bTmMin = Math.Min(bTmMin, a.TmLoco.MPa); bTcMax = Math.Max(bTcMax, tcMax); bTcLocoMax = Math.Max(bTcLocoMax, tcLoco); bForceMax = Math.Max(bForceMax, f);
            foreach (var w in a.W) if (w.Emergency) { bEmergency = true; break; }
            foreach (var c in sim.Dyn.Cp) { bPullMax = Math.Max(bPullMax, c.Force); bPushMax = Math.Max(bPushMax, -c.Force); }
            if (!bStopped && b0V > 1 && v < 0.05)
            {
                bStopped = true;
                AppLog.Write("ТОРМОЗА", string.Format(C, "остановка: с {0:0.0} км/ч за {1:0.0} с, тормозной путь {2:0} м ({3}); {4}", b0V, sim.T - b0T, Math.Abs(sim.Dyn.HeadX - b0X), Summary(), Now()));
            }
            if (act) return;
            braking = false;
            AppLog.Write("ТОРМОЗА", string.Format(C, "торможение закончено (ТЦ пусты): длилось {0:0.0} с, скорость {1:0.0} → {2:0.0} км/ч, путь {3:0} м ({4}); {5}", sim.T - b0T, b0V, v, Math.Abs(sim.Dyn.HeadX - b0X), Summary(), Now()));
        }
        void Begin(double v)
        {
            var a = sim.Air;
            braking = true; b0T = sim.T; b0X = sim.Dyn.HeadX; b0V = v; b0Tm = a.TmLoco.MPa; b0Ur = a.Ur.MPa; bUrMin = b0Ur; bTmMin = b0Tm; bTcMax = 0; bTcLocoMax = 0; bForceMax = 0; bPushMax = 0; bPullMax = 0; bEmergency = false; bStopped = false;
        }
        string Summary()
        {
            return string.Format(C, "разрядка УР {0} → {1} кгс/см² (на {2}), ТМ до {3}, наибольшее ТЦ вагонов {4}, локомотива {5} кгс/см², тормозная сила до {6:0} кН, в автосцепках растяжение до {7:0}, сжатие до {8:0} кН{9}",
                P(b0Ur), P(bUrMin), P(b0Ur - bUrMin), P(bTmMin), P(bTcMax), P(bTcLocoMax), bForceMax, bPullMax / 1000, bPushMax / 1000, bEmergency ? ", было экстренное" : "");
        }

        void Couplers()
        {
            var cp = sim.Dyn.Cp;
            if (broken.Length != cp.Length) { broken = new bool[cp.Length]; for (int i = 0; i < cp.Length; i++) broken[i] = cp[i].Broken; return; }
            for (int i = 0; i < cp.Length; i++)
                if (cp[i].Broken != broken[i])
                {
                    broken[i] = cp[i].Broken;
                    if (cp[i].Broken) AppLog.Write("НЕШТАТКА", string.Format(C, "обрыв автосцепки № {0} (между единицами {0} и {1} от головы): наибольшее растяжение {2:0} кН, сжатие {3:0} кН при пределе {4:0} кН; {5}; {6}", i + 1, i + 2, cp[i].MaxTension / 1000, cp[i].MaxCompression / 1000, sim.Dyn.BreakKn, Now(), Pressures()));
                }
        }

        void Periodic()
        {
            var a = sim.Air;
            bool active = braking || waveT >= 0 || a.EpkTripped || a.EmergencyVent || a.BreakAt >= 0 || a.Crane != 2 || Math.Abs(a.Ur.MPa - a.TmLoco.MPa) > 0.01 || Math.Abs(a.TmLoco.MPa - a.TailTmMPa) > 0.01;
            if (sim.T >= faultsAt) { faultsAt = sim.T + 10; Faults(); }
            if (sim.T - tableAt < (active ? 2 : 30) && active == wasActive) return;
            wasActive = active; tableAt = sim.T;
            double loco, f = BrakeForceKn(out loco); double pull = 0, push = 0; int ipull = -1, ipush = -1;
            for (int i = 0; i < sim.Dyn.Cp.Length; i++) { double c = sim.Dyn.Cp[i].Force; if (c > pull) { pull = c; ipull = i; } if (-c > push) { push = -c; ipush = i; } }
            AppLog.Write("ТОРМОЗА", string.Format(C, "{0}; кран 395 {1}, 254 {2}; {3}; подпитка ТМ {4:0.0} г/с (утечки состава {5:0.0}), компрессоры {6}, лампа «ТМ» {7}; тормозная сила {8:0} кН (локомотив {9:0}), замедление {10:0.00} м/с²; автосцепки: растяжение {11:0} кН{12}, сжатие {13:0} кН{14}",
                Now(), a.Crane, a.Kvt, Pressures(), a.LampFeedKgS * 1000, a.LeakRefKgS * 1000, a.CompressorOn ? "вкл" : "выкл", a.Lamp418 ? "горит" : "нет", f, loco, sim.Dyn.B.Length > 0 ? -sim.Dyn.B[0].A : 0,
                pull / 1000, ipull >= 0 ? " (№ " + (ipull + 1) + ")" : "", push / 1000, ipush >= 0 ? " (№ " + (ipush + 1) + ")" : ""));
            // the table of the wagons: number ТМ/ЗР/ТЦ and the state, 20 wagons a line; every 6 s while the brakes act, else once a minute
            if (sim.T - wagonsAt < (active ? 6 : 60)) return;
            wagonsAt = sim.T;
            for (int from = 0; from < a.W.Length; from += 20)
            {
                var sb = new StringBuilder("вагоны № ТМ/ЗР/ТЦ, кгс/см² (Т торможение, Э экстренное, О отпуск, × ВР выключен, ! не отпускает):");
                for (int i = from; i < a.W.Length && i < from + 20; i++)
                {
                    var w = a.W[i]; int s = StateOf(w);
                    sb.Append(' ').Append(i + 1).Append(' ').Append(P(w.Tm.MPa)).Append('/').Append(P(w.Zr.MPa)).Append('/').Append(P(w.Tc.MPa)).Append(s == 0 ? "" : s == 1 ? "Т" : s == 2 ? "Э" : s == 3 ? "О" : "×").Append(w.Stuck ? "!" : "").Append(';');
                }
                AppLog.Write("ТОРМОЗА", sb.ToString());
            }
        }
    }
}
