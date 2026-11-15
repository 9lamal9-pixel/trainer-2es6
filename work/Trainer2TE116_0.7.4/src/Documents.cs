// Documents of the trip, HTML for printing:
//  - VU-45 "Справка об обеспеченности поезда тормозами и исправном их действии": pressing by PTR in cast iron terms, the
//    required one (freight loaded 33 tf / 100 t, empty 55), axles by pressing, tail wagon, its pipe pressure, the density of
//    the brake pipe and the release time of the tail measured on a copy of the pneumatics of this train;
//  - DU-61 warnings to the train: the dangerous places with their limits (scenario and the ones the instructor added);
//  - the protocol of the trip: violations of the journal with the points, events, faults, brake tests;
//  - the decoded tape of the cassette: speed and allowed speed, pressures, force along the way, events.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;

namespace Trainer2TE116
{
    public static class Documents
    {
        static readonly CultureInfo C = CultureInfo.InvariantCulture;
        static string H(string s) { return WebUtility.HtmlEncode(s ?? ""); }
        static string N(double v, string f) { return v.ToString(f, C).Replace('.', ','); }
        const string Css = "<style>body{font-family:'Times New Roman',serif;margin:24px;color:#111}h1{font-size:18px;text-align:center}h2{font-size:15px;margin-top:18px}table{border-collapse:collapse;width:100%;margin:8px 0}td,th{border:1px solid #333;padding:3px 6px;font-size:13px;vertical-align:top}th{background:#eee}.r{text-align:right}.s{font-size:12px;color:#444}.sig{margin-top:28px}</style>";

        // ---- measurements on a copy of the pneumatics: density (pipe charged, crane II, compressors off: time for the main
        // reservoirs to fall by 0.05 MPa) and the release of the tail after a stage of 0.06 MPa
        public sealed class BrakeCheck { public double DensityS, ReleaseTailS, TailMPa, ChargeMPa; }
        public static BrakeCheck Measure(Train t)
        {
            var res = new BrakeCheck();
            var p = new Pneumatics(); p.Build(t, t.ChargeMPa); res.ChargeMPa = p.ChargeMPa;
            double dt = 0.002;
            p.Crane = 2; p.Kvt = 6;
            for (double s = 0; s < 120; s += dt) p.Step(dt);          // settle
            res.TailMPa = p.TailTmMPa;
            p.CompressorWorks = new[] { false, false };
            double mr0 = p.Mr.MPa, time = 0;
            while (p.Mr.MPa > mr0 - 0.05 && time < 900) { p.Step(dt); time += dt; }
            res.DensityS = time;
            p.CompressorWorks = new[] { true, true }; p.RpmShare = new[] { 0.6, 0.6 };
            for (double s = 0; s < 120; s += dt) p.Step(dt);
            double ur0 = p.Ur.MPa;
            p.Crane = 5; while (p.Ur.MPa > ur0 - 0.06) p.Step(dt);
            p.Crane = 4; for (double s = 0; s < 40; s += dt) p.Step(dt);
            p.Crane = 1; time = 0;
            while (time < 600)
            {
                if (p.Crane == 1 && p.Ur.MPa >= p.ChargeMPa + (t.WagonAxles > 100 ? 0.04 : 0)) p.Crane = 2;
                p.Step(dt); time += dt;
                if (p.TailTcMPa < 0.02 && time > 2) break;
            }
            res.ReleaseTailS = time;
            return res;
        }

        public static string Vu45(Train t, string trainNumber, BrakeCheck chk)
        {
            var sb = new StringBuilder("<!doctype html><html><head><meta charset='utf-8'><title>ВУ-45</title>" + Css + "</head><body>");
            sb.Append("<h1>Справка об обеспеченности поезда тормозами и исправном их действии<br><span class='s'>форма ВУ-45 (учебная, тренажёр 2ТЭ116)</span></h1>");
            double need = t.NormPer100t * t.WagonsMassT / 100.0, have = t.PressingCastTf(false);
            sb.Append("<table>");
            Row(sb, "Поезд №", trainNumber); Row(sb, "Локомотив", "2ТЭ116, " + t.Sections + " секции, " + N(t.Sections * t.SectionMassT, "0") + " т");
            Row(sb, "Вес поезда (брутто вагонов)", N(t.WagonsMassT, "0") + " т; с локомотивом " + N(t.MassT, "0") + " т");
            Row(sb, "Вагонов / осей", t.Wagons.Count + " / " + t.WagonAxles + " (с локомотивом " + t.Axles + ")");
            Row(sb, "Длина поезда", N(t.LengthM, "0") + " м");
            Row(sb, "Требуемое нажатие", N(t.NormPer100t, "0") + " тс на 100 т (" + (t.IsEmpty ? "порожний" : "гружёный") + ") — " + N(need, "0") + " тс");
            Row(sb, "Фактическое нажатие (в пересчёте на чугунные колодки)", N(have, "0") + " тс — " + N(t.PressingPer100t, "0.0") + " тс на 100 т" + (have >= need ? "" : " — <b>НЕДОСТАТОЧНО</b>"));
            Row(sb, "Зарядное давление", N(chk.ChargeMPa * 10.197, "0.0") + " кгс/см²");
            var tail = t.Wagons.Count > 0 ? t.Wagons[t.Wagons.Count - 1] : null;
            Row(sb, "Хвостовой вагон №", tail != null ? tail.Number : "—");
            Row(sb, "Давление в ТМ хвостового вагона", N(chk.TailMPa * 10.197, "0.0") + " кгс/см²");
            Row(sb, "Плотность ТМ (снижение давления в ГР на 0,5 кгс/см² при выключенных компрессорах)", N(chk.DensityS, "0") + " с");
            Row(sb, "Время отпуска хвостовых вагонов (после ступени 0,6 кгс/см²)", N(chk.ReleaseTailS, "0") + " с");
            sb.Append("</table><h2>Оси по расчётному нажатию на ось (в пересчёте на чугун)</h2><table><tr><th>Нажатие на ось, тс</th><th>Вагонов</th><th>Осей</th><th>Нажатие, тс</th></tr>");
            foreach (var g in t.Wagons.Where(w => w.VrOn).GroupBy(w => w.KpCastAxleTf).OrderBy(g => g.Key))
                sb.Append("<tr><td>" + N(g.Key, "0.0") + "</td><td class=r>" + g.Count() + "</td><td class=r>" + g.Sum(w => w.Axles) + "</td><td class=r>" + N(g.Sum(w => w.Axles * w.KpCastAxleTf), "0") + "</td></tr>");
            int off = t.Wagons.Count(w => !w.VrOn);
            if (off > 0) sb.Append("<tr><td>выключены ВР</td><td class=r>" + off + "</td><td class=r>" + t.Wagons.Where(w => !w.VrOn).Sum(w => w.Axles) + "</td><td class=r>0</td></tr>");
            sb.Append("</table><p class='s'>Нажатие — ПТР и приложение 2 Правил ТО тормозов 2025. Плотность и время отпуска измерены на модели пневматики этого поезда.</p>");
            sb.Append("<p class='sig'>Осмотрщик вагонов ______________ &nbsp;&nbsp; Машинист ______________</p></body></html>");
            return sb.ToString();
        }

        public static string Du61(Route r, string trainNumber, double fromX)
        {
            var sb = new StringBuilder("<!doctype html><html><head><meta charset='utf-8'><title>ДУ-61</title>" + Css + "</head><body>");
            sb.Append("<h1>Предупреждение на поезд № " + H(trainNumber) + "<br><span class='s'>форма ДУ-61 (учебная)</span></h1>");
            sb.Append("<p>Станция " + H(r.StationName.Length > 0 ? r.StationName[0] : "") + " &nbsp; «__» ________ 20__ г.</p>");
            sb.Append("<table><tr><th>1. Место действия (км, пикет)</th><th>2. Время действия</th><th>3. Скорость не более, км/ч</th><th>4. Остановиться у красного сигнала, далее не более, км/ч</th><th>5. Особые условия</th></tr>");
            var list = r.Warnings(fromX);
            if (list.Count == 0) sb.Append("<tr><td colspan=5>Предупреждений нет</td></tr>");
            foreach (var w in list)
                sb.Append("<tr><td>" + H(r.KmAt(w.Key.X) + " км " + r.PkAt(w.Key.X) + " пк — " + (w.Value != null ? r.KmAt(w.Value.X) + " км " + r.PkAt(w.Value.X) + " пк" : "до конца")) + "</td><td>до отмены</td><td class=r>" + w.Key.Limit + "</td><td>—</td><td>" + (w.Key.Added ? "выдано инструктором" : "опасное место") + "</td></tr>");
            sb.Append("</table><p class='sig'>Дежурный по станции ______________ &nbsp;&nbsp; Получил машинист ______________</p></body></html>");
            return sb.ToString();
        }

        public static string Protocol(Simulation s, int number)
        {
            var sb = new StringBuilder("<!doctype html><html><head><meta charset='utf-8'><title>Протокол поездки</title>" + Css + "</head><body>");
            sb.Append("<h1>Протокол поездки № " + number + " от " + s.Started.ToString("dd.MM.yyyy", C) + "</h1><table>");
            Row(sb, "Участок", s.Route.Name); Row(sb, "Поезд", s.TrainNumber + " — " + s.Train);
            Row(sb, "Локомотив", "2ТЭ116"); Row(sb, "Машинист (обучаемый)", s.Student); Row(sb, "Инструктор", s.Instructor);
            Row(sb, "Начало поездки", s.Started.ToString("HH:mm:ss", C));
            Row(sb, "Продолжительность", TimeSpan.FromSeconds(s.T).ToString(@"hh\:mm\:ss"));
            Row(sb, "Пройдено", N(s.Distance / 1000, "0.0") + " км; расход топлива " + N(s.Tr.FuelKg, "0") + " кг");
            int at; double mt = 0, mc = 0;
            foreach (var c in s.Dyn.Cp) { mt = Math.Max(mt, c.MaxTension); mc = Math.Max(mc, c.MaxCompression); }
            Row(sb, "Наибольшие силы в автосцепках", "растяжение " + N(mt / 1000, "0") + " кН, сжатие " + N(mc / 1000, "0") + " кН");
            sb.Append("</table>");
            List<JournalEntry> list; lock (Journal.Entries) list = Journal.Entries.ToList();
            int pts = list.Sum(e => e.Points);
            sb.Append("<h2>Нарушения: " + list.Count + ", баллов " + pts + " — оценка: " + Mark(list) + "</h2><table><tr><th>№</th><th>Время</th><th>Место</th><th>V, км/ч</th><th>Нарушение</th><th>Подробности</th><th>Основание</th><th>Баллы</th></tr>");
            int k = 1;
            foreach (var e in list)
                sb.Append("<tr><td>" + k++ + "</td><td>" + e.Clock + "</td><td>" + H(e.Place) + "</td><td class=r>" + N(e.V, "0") + "</td><td>" + (e.Gross ? "<b>" : "") + H(e.Title) + (e.Gross ? " (грубое)</b>" : "") + "</td><td>" + H(e.Detail) + "</td><td class=s>" + H(e.Basis) + "</td><td class=r>" + e.Points + "</td></tr>");
            if (list.Count == 0) sb.Append("<tr><td colspan=8>Нарушений нет</td></tr>");
            sb.Append("</table><h2>Проба тормозов в пути</h2><ul>");
            foreach (var r in BrakeTest.Results) sb.Append("<li>" + H(r) + "</li>");
            if (BrakeTest.Results.Count == 0) sb.Append("<li>не выполнялась</li>");
            sb.Append("</ul><h2>События поездки</h2><table>");
            lock (s.Events) foreach (var e in s.Events) sb.Append("<tr><td class=s>" + H(e) + "</td></tr>");
            sb.Append("</table><p class='sig'>Инструктор ______________ / ______________</p></body></html>");
            return sb.ToString();
        }
        // grade by the points: 0 — "отлично", up to 10 — "хорошо", up to 25 without gross ones — "удовлетворительно", else "неудовлетворительно"
        public static string Mark(List<JournalEntry> list)
        {
            int p = list.Sum(e => e.Points);
            if (list.Any(e => e.Gross) || p > 25) return "неудовлетворительно";
            return p == 0 ? "отлично" : p <= 10 ? "хорошо" : "удовлетворительно";
        }

        public static string Tape(Cassette.Record rec, Route r)
        {
            var f = rec.Frames;
            var sb = new StringBuilder("<!doctype html><html><head><meta charset='utf-8'><title>Расшифровка кассеты</title>" + Css + "</head><body>");
            sb.Append("<h1>Расшифровка кассеты регистрации</h1><p class='s'>" + H(rec.Header.ContainsKey("route") ? rec.Header["route"].ToString() : "") + " · " + H(rec.Header.ContainsKey("train") ? rec.Header["train"].ToString() : "") +
                " · кадров " + f.Count + ", блоков " + rec.Blocks + (rec.BadBlocks > 0 ? ", <b>повреждённых или изменённых блоков: " + rec.BadBlocks + "</b>" : ", контрольные суммы сходятся") + "</p>");
            if (f.Count > 1)
            {
                double x0 = f[0].X, x1 = f[f.Count - 1].X; if (x1 <= x0) x1 = x0 + 1;
                int w = 1600, step = Math.Max(1, f.Count / 4000);
                Func<double, double> X = x => 50 + (x - x0) / (x1 - x0) * (w - 60);
                sb.Append(Panel("Скорость и допустимая скорость, км/ч", w, 220, 0, 100, X, f, step, new[] { "#c0392b|Allowed", "#0b7a75|V" }, r));
                sb.Append(Panel("Давление, кгс/см²: ТМ, УР, ТЦ локомотива, ТЦ хвоста", w, 200, 0, 7, X, f, step, new[] { "#1f5fbf|Tm", "#7a4fbf|Ur", "#c0392b|TcLoco", "#e67e22|TailTc" }, r));
                sb.Append(Panel("Сила тяги, кН; наибольшие силы в автосцепках, кН ×0,1", w, 180, 0, 600, X, f, step, new[] { "#2c3e50|Force", "#16a085|Tension", "#8e44ad|Compression" }, r));
            }
            sb.Append("<h2>События</h2><table><tr><th>Время</th><th>Место</th><th>Событие</th></tr>");
            foreach (var e in rec.Events) sb.Append("<tr><td>" + TimeSpan.FromSeconds(e.Item1).ToString(@"hh\:mm\:ss") + "</td><td>" + H(r != null ? r.KmPk(e.Item2) : N(e.Item2, "0")) + "</td><td>" + H(e.Item3) + "</td></tr>");
            sb.Append("</table></body></html>");
            return sb.ToString();
        }

        static string Panel(string title, int w, int h, double lo, double hi, Func<double, double> X, List<CassetteFrame> f, int step, string[] series, Route r)
        {
            var sb = new StringBuilder("<h2>" + H(title) + "</h2><svg width='" + w + "' height='" + (h + 20) + "' style='background:#fafafa;border:1px solid #ccc'>");
            Func<double, double> Y = v => 10 + (h - 10) * (1 - (Math.Max(lo, Math.Min(hi, v)) - lo) / (hi - lo));
            for (int g = 0; g <= 4; g++) { double v = lo + (hi - lo) * g / 4; sb.Append("<line x1='50' x2='" + (w - 10) + "' y1='" + N(Y(v), "0.0").Replace(',', '.') + "' y2='" + N(Y(v), "0.0").Replace(',', '.') + "' stroke='#ddd'/><text x='4' y='" + N(Y(v) + 4, "0").Replace(',', '.') + "' font-size='11'>" + N(v, hi < 2 ? "0.00" : "0") + "</text>"); }
            foreach (var sr in series)
            {
                var parts = sr.Split('|'); var pts = new StringBuilder();
                for (int i = 0; i < f.Count; i += step)
                {
                    var fr = f[i];
                    double v = parts[1] == "V" ? fr.V : parts[1] == "Allowed" ? fr.Allowed : parts[1] == "Tm" ? fr.Tm * Traction.KgfPerMPa : parts[1] == "Ur" ? fr.Ur * Traction.KgfPerMPa : parts[1] == "TcLoco" ? fr.TcLoco * Traction.KgfPerMPa : parts[1] == "TailTc" ? fr.TailTc * Traction.KgfPerMPa
                        : parts[1] == "Force" ? fr.Force : parts[1] == "Tension" ? fr.Tension / 10 : fr.Compression / 10;
                    pts.Append(X(fr.X).ToString("0.0", C)).Append(',').Append(Y(v).ToString("0.0", C)).Append(' ');
                }
                sb.Append("<polyline fill='none' stroke='" + parts[0] + "' stroke-width='1.3' points='" + pts + "'/>");
            }
            sb.Append("</svg>");
            return sb.ToString();
        }

        static void Row(StringBuilder sb, string k, string v) { sb.Append("<tr><th style='text-align:left;width:42%'>" + H(k) + "</th><td>" + v + "</td></tr>"); }
        public static string Save(string dir, string name, string html) { Directory.CreateDirectory(dir); var p = Path.Combine(dir, name); File.WriteAllText(p, html, new UTF8Encoding(true)); return p; }
    }
}
