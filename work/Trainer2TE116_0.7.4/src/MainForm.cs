// Window of the instructor (design canvas "Инструктор 2ТЭ116 — интерфейс", 6 screens). Rail of sections on the left, header
// with the trip and its controls, a row of status chips, and the pages:
//  - Карта: regime card with the train; click on a signal changes its aspect (the combinations of the scenario), the right
//    button puts a sign, a warning or an obstacle at the place or 1000 m ahead (as the context menu of PathProfileForm);
//    the strip of the wagon cylinders and the last violations below;
//  - Локомотив: every parameter of both sections and of the air of the locomotive, the cab (handles of the instructor when
//    the pult of the cab is not on line, or to move a handle over the pult);
//  - Поезд: strip of the wagons by TC / TM / coupler force / braking force and the table of every wagon;
//  - Пневмо: scheme of the air brakes with the pressures now and the pipe along the train;
//  - Управление: signals ahead, obstacles of the scenario, warnings (DU-61), signs, day / night, weather, faults;
//  - Журнал: violations with the points and the mark, events, documents (VU-45, DU-61, protocol, tape), cassette, archive;
//  - Поездка: route, train (own, built-in, database of the instructor, editor), crew, checks before the start.
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Trainer2TE116
{
    public sealed class MainForm : Form
    {
        static readonly CultureInfo C = CultureInfo.InvariantCulture;
        // the pult of the cab (Pult.cs); the automatic driver is not in the window, only in the runs (--run) and the tests
        readonly string dataDir = Program.DataDir;
        Simulation sim; Route route; Train train; readonly Pult pult; readonly Video video; readonly Blok blok;
        readonly Timer timer = new Timer { Interval = 100 };
        readonly List<double[]> trace = new List<double[]>();
        double lastTraceX = double.NaN, speedUp = 1, nextUi;
        string tripDir; int tripNo; bool night, fog, pultOnLine, videoOnLine, blokOnLine;

        // frame
        readonly Panel host = new Panel { Dock = DockStyle.Fill, BackColor = Th.Back, Padding = new Padding(16, 6, 16, 14) };
        readonly List<NavBtn> navs = new List<NavBtn>(); readonly List<Control> pages = new List<Control>();
        Label lbTitle, lbSub, lbClock, lbPult, lbPultTrip; Card cabCard; Btn btPause; Seg segSpeed; readonly Chip[] chips = new Chip[8];
        // pages
        readonly RegimeView map = new RegimeView { Dock = DockStyle.Fill };
        WagonStrip stripTc, stripBig; ViolationList miniJournal, journalList; ListBox events, archive; ParamList plS1, plS2, plAir, plCassette, plSummary;
        TrackBar tbPos; Label lbPos; Seg segRev, segMetric; readonly Btn[] craneBtn = new Btn[8], kvtBtn = new Btn[7]; CheckBox chEpk, chSand, chTyphon, chWhistle, chSpot;
        DataGridView gridTrain; AirScheme scheme; SchemeView electricView; FlowLayoutPanel lightsFlow, obstaclesFlow, faultsFlow; NumericUpDown nFrom, nLen, nV; Seg segWhere, segDay;
        CheckBox chRain, chSnow, chFog; Label lbMark;
        CardList routes, trains; Seg segSource; TextBox tbStudent, tbTab, tbInstructor, tbNumber; CheckBox chCharged;
        List<Train> trainItems = new List<Train>();

        public MainForm()
        {
            Text = "Тренажёр 2ТЭ116 — инструктор"; BackColor = Th.Back; ForeColor = Th.Text; Font = Th.F(9.5f);
            ClientSize = new Size(1600, 960); MinimumSize = new Size(1280, 800); StartPosition = FormStartPosition.CenterScreen; DoubleBuffered = true;
            Directory.CreateDirectory(Path.Combine(dataDir, "trains"));
            map.Icons = RegimeIcons.Load(Path.Combine(Program.ReleaseDir, "instructor.exe"));
            var main = new Panel { Dock = DockStyle.Fill, BackColor = Th.Back };
            main.Controls.Add(host); main.Controls.Add(ChipsRow()); main.Controls.Add(Header());
            Controls.Add(main); Controls.Add(Rail());
            pages.Add(MapPage()); pages.Add(LocoPage()); pages.Add(TrainPage()); pages.Add(AirPage()); pages.Add(ControlPage()); pages.Add(JournalPage()); pages.Add(TripPage());
            electricView = new SchemeView(); var prepared = new Traction(); prepared.Init(2, "nominal"); electricView.Bind(prepared.Electric, false); pages.Add(electricView);
            foreach (var p in pages) { p.Dock = DockStyle.Fill; p.Visible = false; host.Controls.Add(p); }
            Show(6);
            pult = Pult.FromSettings(Program.ReleaseDir, Program.Arguments); pult.Start();
            video = Video.FromSettings(Program.ReleaseDir, Program.Arguments); video.Start();
            blok = Blok.FromSettings(Program.ReleaseDir, Program.Arguments); blok.Start();
            AppLog.Write("ВИДЕО", "настройки: {0}, адрес {1}:{2} (TCP), маршрут {3}; {4}", video.Enabled ? "включено" : "выключено", video.Ip, video.Port, video.Road, video.State);
            AppLog.Write("БЛОК", "настройки: {0}, адрес {1}:{2} (UDP), выход {3} байт каждые 25 мс, ответ {4} байт; {5}", blok.Enabled ? "включён" : "выключен", blok.Ip, blok.Port, Blok.OutSize, Blok.InSize, blok.State);
            if (Program.Arguments.ContainsKey("hidden")) { Opacity = 0; StartPosition = FormStartPosition.Manual; Location = new Point(0, 0); }
            Load += (s, e) => AutoStart();
            Journal.Logged = l => AppLog.Write("НАРУШЕНИЕ", l); BrakeTest.Logged = l => AppLog.Write("ТОРМОЗА", l);
            timer.Tick += (s, e) => TickLogged(); timer.Start();
            FormClosing += (s, e) =>
            {
                timer.Stop();
                AppLog.Write("ПРОГРАММА", "окно инструктора закрывается" + (sim != null && !sim.Finished ? "; поездка не была закончена" : ""));
                if (sim != null && sim.Cassette != null) { sim.Log("Окно инструктора закрыто"); sim.Cassette.Dispose(); sim.Cassette = null; }
                pult.Dispose(); video.Dispose(); blok.Dispose();
            };
            KeyPreview = true;
            KeyDown += (s, e) => { if (e.KeyCode == Keys.Space && sim != null && !(ActiveControl is TextBox)) { TogglePause(); e.Handled = true; } };
        }

        // ---------- frame
        Control Rail()
        {
            var rail = new FlowLayoutPanel { Dock = DockStyle.Left, Width = 86, BackColor = Th.Rail, FlowDirection = FlowDirection.TopDown, WrapContents = false, Padding = new Padding(0, 12, 0, 0) };
            var logo = new Label { Text = "2ТЭ", Size = new Size(48, 48), Margin = new Padding(19, 0, 19, 14), BackColor = Th.Accent, ForeColor = Th.AccentInk, Font = Th.F(11f, true), TextAlign = ContentAlignment.MiddleCenter };
            rail.Controls.Add(logo);
            string[] names = { "Карта", "Локомотив", "Поезд", "Пневмо", "Управление", "Журнал", "Поездка", "Схема" };
            for (int i = 0; i < names.Length; i++) { int k = i; var b = new NavBtn(names[i], i); b.Click += (s, e) => Show(k); navs.Add(b); rail.Controls.Add(b); }
            rail.Paint += (s, e) => { using (var p = new Pen(Th.Line)) e.Graphics.DrawLine(p, rail.Width - 1, 0, rail.Width - 1, rail.Height); };
            return rail;
        }
        void Show(int k) { for (int i = 0; i < pages.Count; i++) { pages[i].Visible = i == k; navs[i].On = i == k; navs[i].Invalidate(); } if (k == 5) RefreshArchive(); }

        Control Header()
        {
            var h = new Panel { Dock = DockStyle.Top, Height = 64, BackColor = Th.Back, Padding = new Padding(18, 0, 16, 0) };
            lbTitle = new Label { AutoSize = false, Location = new Point(18, 8), Size = new Size(520, 26), Font = Th.F(13.5f, true), ForeColor = Th.Text, Text = "Новая поездка" };
            lbSub = new Label { AutoSize = false, Location = new Point(18, 34), Size = new Size(560, 22), Font = Th.F(9f), ForeColor = Th.Muted, Text = "Выберите участок, поезд и бригаду на вкладке «Поездка»" };
            var right = new FlowLayoutPanel { Dock = DockStyle.Right, Width = 930, FlowDirection = FlowDirection.RightToLeft, WrapContents = false, Padding = new Padding(0, 12, 0, 0), BackColor = Th.Back };
            var btFinish = new Btn("Закончить поездку", Look.Danger, 170); btFinish.Click += (s, e) => FinishTrip();
            lbPult = new Label { AutoSize = false, Size = new Size(290, 38), Font = Th.F(9f, true), ForeColor = Th.Muted, TextAlign = ContentAlignment.MiddleCenter, Text = "Пульт: —", Margin = new Padding(4, 4, 8, 4) };
            segSpeed = new Seg("×1", "×2", "×5", "×10") { Width = 180, Margin = new Padding(4, 5, 4, 4) }; segSpeed.Changed += i => speedUp = new[] { 1.0, 2, 5, 10 }[i];
            btPause = new Btn("Пауза", Look.Primary, 100); btPause.Click += (s, e) => TogglePause();
            lbClock = new Label { AutoSize = false, Size = new Size(150, 38), Font = Th.Mono(16f), ForeColor = Th.Text, TextAlign = ContentAlignment.MiddleRight, Text = "00:00:00", Margin = new Padding(4, 4, 10, 4) };
            right.Controls.AddRange(new Control[] { btFinish, lbPult, segSpeed, btPause, lbClock });
            h.Controls.Add(right); h.Controls.Add(lbTitle); h.Controls.Add(lbSub); right.BringToFront();
            h.Paint += (s, e) => { using (var p = new Pen(Th.Line)) e.Graphics.DrawLine(p, 0, h.Height - 1, h.Width, h.Height - 1); };
            return h;
        }
        void TogglePause() { if (sim == null) return; sim.Paused = !sim.Paused; btPause.Text = sim.Paused ? "Продолжить" : "Пауза"; btPause.Invalidate(); if (sim.Paused) sim.Log("Пауза"); else AppLog.Write("ПОЕЗДКА", "пауза снята"); }

        Control ChipsRow()
        {
            var t = new TableLayoutPanel { Dock = DockStyle.Top, Height = 84, ColumnCount = 8, RowCount = 1, Padding = new Padding(14, 10, 14, 4), BackColor = Th.Back };
            string[] caps = { "Скорость, км/ч", "Допустимая, км/ч", "Место", "ТМ / УР, кгс/см²", "ТЦ лок. / хвост, кгс/см²", "ГР, кгс/см²", "Позиция · 395 · 254", "Нарушения · баллы" };
            for (int i = 0; i < 8; i++) { t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 12.5f)); chips[i] = new Chip(caps[i]) { Dock = DockStyle.Fill, Margin = new Padding(4) }; t.Controls.Add(chips[i], i, 0); }
            chips[2].ValueSize = 11.5f;
            return t;
        }

        // ---------- map
        Control MapPage()
        {
            var p = new Panel { BackColor = Th.Back };
            var mapCard = new Card("") { Dock = DockStyle.Fill, Padding = new Padding(2) };
            mapCard.Controls.Add(map);
            map.MouseClick += MapClick;
            var bottom = new TableLayoutPanel { Dock = DockStyle.Bottom, Height = 132, ColumnCount = 2, BackColor = Th.Back, Padding = new Padding(0, 10, 0, 0) };
            bottom.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 60)); bottom.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 40));
            var c1 = new Card("Тормозные цилиндры по составу") { Dock = DockStyle.Fill, Margin = new Padding(0, 0, 6, 0), Note = "голова → хвост · наведите на вагон" };
            stripTc = new WagonStrip { Dock = DockStyle.Fill }; stripTc.Picked += i => { Show(2); };
            c1.Controls.Add(stripTc);
            var c2 = new Card("Журнал") { Dock = DockStyle.Fill, Margin = new Padding(6, 0, 0, 0), Padding = new Padding(10, 36, 10, 6) };
            miniJournal = new ViolationList { Dock = DockStyle.Fill }; c2.Controls.Add(miniJournal);
            bottom.Controls.Add(c1, 0, 0); bottom.Controls.Add(c2, 1, 0);
            p.Controls.Add(mapCard); p.Controls.Add(bottom);
            return p;
        }

        void MapClick(object sender, MouseEventArgs e)
        {
            if (sim == null) return;
            double x = map.RouteXAt(e.X);
            // a signal near the click: its aspect
            int best = -1; float bd = 14;
            for (int i = 0; i < route.Lights.Count; i++) { float d = Math.Abs(map.PxAt(route.Lights[i].X) - e.X); if (d < bd && e.Y >= map.LightsTop - 6 && e.Y <= map.LightsBottom + 6) { bd = d; best = i; } }
            var m = MapMenu = new ContextMenuStrip { Renderer = new DarkRenderer(), ShowImageMargin = false, Font = Th.F(10f) };
            if (best >= 0 && e.Button == MouseButtons.Left)
            {
                var l = route.Lights[best];
                m.Items.Add(new ToolStripLabel("Светофор " + l.Name + " · " + route.KmPk(l.X)) { ForeColor = Th.Muted });
                var codes = l.Combinations.Length > 0 ? l.Combinations : new[] { 0, 2, 1, 8, 10, 4, 19 };
                foreach (int c in codes) { int cc = c; var it = new ToolStripMenuItem((cc == l.Aspect ? "● " : "   ") + Route.AspectName(cc)); it.Click += (s, a) => SetAspect(best, cc); m.Items.Add(it); }
                m.Show(map, e.Location); return;
            }
            if (e.Button != MouseButtons.Right) return;
            m.Items.Add(new ToolStripLabel(route.KmPk(x)) { ForeColor = Th.Muted });
            foreach (var ahead in new[] { 0, 1000 })
            {
                double at = x + ahead;
                var sub = new ToolStripMenuItem(ahead == 0 ? "Поставить знак в эту точку" : "Поставить знак через 1000 м");
                foreach (var t in new[] { 5, 2, 4, 6, 17, 18, 9 }) { int tt = t; var it = new ToolStripMenuItem(Route.SignName(tt)); it.Click += (s, a) => AddSign(at, tt, 0); sub.DropDownItems.Add(it); }
                var danger = new ToolStripMenuItem("Начало опасного места");
                foreach (var v in new[] { 15, 25, 40, 60 }) { int vv = v; var it = new ToolStripMenuItem("ограничение " + vv + " км/ч"); it.Click += (s, a) => AddSign(at, 8, vv); danger.DropDownItems.Add(it); }
                sub.DropDownItems.Add(danger);
                m.Items.Add(sub);
            }
            var warn = new ToolStripMenuItem("Предупреждение отсюда, 1000 м");
            foreach (var v in new[] { 15, 25, 40, 60 }) { int vv = v; var it = new ToolStripMenuItem(vv + " км/ч"); it.Click += (s, a) => Warning(x, 1000, vv); warn.DropDownItems.Add(it); }
            m.Items.Add(warn);
            var obst = new ToolStripMenuItem("Препятствие здесь"); obst.Click += (s, a) => { route.Obstacles.Add(new Obstacle { X = x, Kind = "liters", Active = true }); sim.Log("Препятствие на " + route.KmPk(x)); FillObstacles(); };
            m.Items.Add(obst);
            m.Show(map, e.Location);
        }
        void SetAspect(int i, int code) { var l = route.Lights[i]; if (l.Aspect == code) return; l.Aspect = code; sim.Log("Светофор " + l.Name + ": " + Route.AspectName(code)); FillLights(); }
        void AddSign(double x, int type, int limit) { route.AddSign(new Sign { X = x, Type = type, Limit = limit, Added = true }); sim.Log("Знак «" + Route.SignName(type) + "»" + (limit > 0 ? " " + limit + " км/ч" : "") + " на " + route.KmPk(x)); }
        void Warning(double x, double len, int v) { route.AddSign(new Sign { X = x, Type = 8, Limit = v, Added = true }); route.AddSign(new Sign { X = x + len, Type = 9, Added = true }); sim.Log("Выдано предупреждение " + v + " км/ч: " + route.KmPk(x) + ", " + len.ToString("0", C) + " м"); }

        // ---------- locomotive and the cab
        Control LocoPage()
        {
            var t = new TableLayoutPanel { ColumnCount = 3, RowCount = 2, BackColor = Th.Back };
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 33)); t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 33)); t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 34));
            t.RowStyles.Add(new RowStyle(SizeType.Percent, 58)); t.RowStyles.Add(new RowStyle(SizeType.Percent, 42));
            var c1 = new Card("Секция 1") { Dock = DockStyle.Fill, Margin = new Padding(0, 0, 6, 6) }; plS1 = new ParamList { Dock = DockStyle.Fill }; c1.Controls.Add(plS1);
            var c2 = new Card("Секция 2") { Dock = DockStyle.Fill, Margin = new Padding(6, 0, 6, 6) }; plS2 = new ParamList { Dock = DockStyle.Fill }; c2.Controls.Add(plS2);
            var c3 = new Card("Тормоза локомотива") { Dock = DockStyle.Fill, Margin = new Padding(6, 0, 0, 6) }; plAir = new ParamList { Dock = DockStyle.Fill }; c3.Controls.Add(plAir);
            t.Controls.Add(c1, 0, 0); t.Controls.Add(c2, 1, 0); t.Controls.Add(c3, 2, 0);
            var cab = cabCard = new Card("Кабина") { Dock = DockStyle.Fill, Margin = new Padding(0, 6, 0, 0), Note = "органы управления инструктора; пульт кабины не на связи" };
            var f = new FlowLayoutPanel { Dock = DockStyle.Fill, BackColor = Th.Card, FlowDirection = FlowDirection.LeftToRight, WrapContents = true };
            lbPos = new Label { Text = "Позиция 0", AutoSize = false, Size = new Size(100, 40), ForeColor = Th.Text, Font = Th.F(10.5f, true), TextAlign = ContentAlignment.MiddleLeft };
            tbPos = new TrackBar { Minimum = 0, Maximum = 15, Width = 360, TickFrequency = 1, BackColor = Th.Card }; tbPos.ValueChanged += (s, e) => { if (sim != null) sim.C.Position = tbPos.Value; lbPos.Text = "Позиция " + tbPos.Value; };
            segRev = new Seg("Нейтраль", "Вперёд", "Назад") { Width = 280, Selected = 1, Margin = new Padding(12, 4, 12, 4) }; segRev.Changed += i => { if (sim != null) sim.C.Reverser = i; };
            f.Controls.AddRange(new Control[] { lbPos, tbPos, new Label { Text = "Реверсор", AutoSize = true, ForeColor = Th.Muted, Margin = new Padding(12, 12, 0, 0) }, segRev });
            f.SetFlowBreak(segRev, true);
            f.Controls.Add(new Label { Text = "Кран 395", AutoSize = false, Size = new Size(100, 40), ForeColor = Th.Muted, TextAlign = ContentAlignment.MiddleLeft });
            string[] cn = { "", "I", "II", "III", "IV", "V", "VI", "Vа" };
            foreach (int k in new[] { 1, 2, 3, 4, 5, 7, 6 }) { int kk = k; craneBtn[k] = new Btn(cn[k], Look.Secondary, 54); craneBtn[k].Click += (s, e) => { if (sim != null) sim.C.Crane = kk; }; f.Controls.Add(craneBtn[k]); }
            f.Controls.Add(new Label { Text = "Кран 254", AutoSize = false, Size = new Size(90, 40), ForeColor = Th.Muted, TextAlign = ContentAlignment.MiddleRight, Margin = new Padding(20, 4, 6, 4) });
            for (int k = 1; k <= 6; k++) { int kk = k; kvtBtn[k] = new Btn(k == 1 ? "1 отп." : k == 2 ? "2" : k.ToString(C), Look.Secondary, k == 1 ? 70 : 46); kvtBtn[k].Click += (s, e) => { if (sim != null) sim.C.Kvt = kk; }; f.Controls.Add(kvtBtn[k]); }
            f.SetFlowBreak(kvtBtn[6], true);
            chEpk = Chk("Ключ ЭПК", true, v => { if (sim != null) sim.C.EpkKey = v; }); chSand = Chk("Песок", false, v => { if (sim != null) sim.C.Sand = v; });
            chTyphon = Chk("Тифон", false, v => { if (sim != null) sim.C.Typhon = v; }); chWhistle = Chk("Свисток", false, v => { if (sim != null) sim.C.Whistle = v; }); chSpot = Chk("Прожектор", true, v => { if (sim != null) sim.C.Spotlight = v; });
            f.Controls.AddRange(new Control[] { chEpk, chSand, chTyphon, chWhistle, chSpot });
            // switches of the pult that had no place on the screen: traction, refrigerator, diesels, damage indicator
            Action<string, Func<bool>, Action<bool>> sw = (text, get, set) =>
            {
                var c = Chk(text, false, v => { if (sim != null && !cabSync) set(v); }); c.Margin = new Padding(8, 4, 10, 2); c.Font = Th.F(9f);
                cabSw.Add(Tuple.Create(c, get)); f.Controls.Add(c);
            };
            f.SetFlowBreak(chSpot, true);
            sw("Управление тепловозом", () => sim.C.LocoControl, v => sim.C.LocoControl = v);
            sw("Движение", () => sim.C.Motion, v => sim.C.Motion = v);
            sw("Управление переходом", () => sim.C.Conversion, v => sim.C.Conversion = v);
            sw("Ограничение боксования", () => sim.C.Tob, v => sim.C.Tob = v);
            sw("Холодильник: автомат", () => sim.C.Refrigerator, v => sim.C.Refrigerator = v);
            for (int k = 0; k < 4; k++) { int kk = k; sw("Вент. " + (k + 1), () => sim.C.FanSw[kk], v => sim.C.FanSw[kk] = v); }
            sw("Топливный насос 1", () => sim.C.FuelPump[0], v => sim.C.FuelPump[0] = v);
            sw("Топливный насос 2", () => sim.C.FuelPump[1], v => sim.C.FuelPump[1] = v);
            sw("Контроль ламп", () => !sim.C.AlarmMonitoring, v => sim.C.AlarmMonitoring = !v);
            sw("Песок 1 ось", () => sim.C.SandAxle, v => sim.C.SandAxle = v);
            sw("Отпуск тормозов", () => sim.C.ReleaseButton, v => sim.C.ReleaseButton = v);
            sw("Вызов", () => sim.C.CallButton, v => sim.C.CallButton = v);
            sw("Маневры", () => sim.C.Maneuver, v => sim.C.Maneuver = v);
            sw("Автостоп", () => sim.C.AutoStop, v => sim.C.AutoStop = v);
            sw("Повреждение (лампы 2-й секции)", () => sim.C.DamageSel == 1, v => sim.C.DamageSel = v ? 1 : 2);
            for (int k = 0; k < 2; k++) { int kk = k; var b = new Btn("Пуск " + (k + 1), Look.Secondary, 76); b.Click += (s, e) => { if (sim != null) sim.C.Launch[kk] = true; }; f.Controls.Add(b); }
            var bStop = new Btn("Аварийная остановка", Look.Secondary, 170); bStop.Click += (s, e) => { if (sim != null) sim.C.EmergencyStop = true; }; f.Controls.Add(bStop);
            segUp = new Seg("УП выкл", "УП: секция 1", "УП: секция 2") { Width = 330, Selected = 0, Margin = new Padding(12, 4, 12, 4) }; segUp.Changed += i => { if (sim != null) sim.C.DamageSw = i; };
            f.Controls.Add(segUp);
            // signal lamps as on the pult; a click burns the lamp out (it is found by the lamp test and by the events)
            for (int k = 0; k < Traction.LampCount; k++)
            {
                if (Traction.LampNames[k] == null) continue; int kk = k;
                var lb = new Label { Text = Traction.LampNames[k], AutoSize = false, Size = new Size(k == 5 ? 172 : 132, 24), Font = Th.F(8.5f), TextAlign = ContentAlignment.MiddleCenter,
                    BackColor = Th.Inset, ForeColor = Th.Dim, Margin = new Padding(8, 4, 0, 2), Cursor = Cursors.Hand };
                lb.Click += (s, e) => { if (sim == null) return; sim.Tr.LampBurnt[kk] = !sim.Tr.LampBurnt[kk];
                    sim.Tr.Event("Лампа «" + Traction.LampNames[kk] + "»: " + (sim.Tr.LampBurnt[kk] ? "перегорела" : "заменена")); };
                lampLb[k] = lb; f.Controls.Add(lb);
            }
            { int n = 0; Label last = null; foreach (var lb in lampLb) if (lb != null) { f.Controls.SetChildIndex(lb, n++); last = lb; } f.SetFlowBreak(last, true); }
            f.AutoScroll = true;
            cab.Controls.Add(f);
            t.Controls.Add(cab, 0, 1); t.SetColumnSpan(cab, 2);
            // apparatus outside the pult (Circuits.cs, Room): the instructor works for the assistant driver
            var room = roomCard = new Card("ВВК и автоматы") { Dock = DockStyle.Fill, Margin = new Padding(6, 6, 0, 0), Note = "аппараты вне пульта: за помощника машиниста" };
            var rf = new FlowLayoutPanel { Dock = DockStyle.Fill, BackColor = Th.Card, FlowDirection = FlowDirection.LeftToRight, WrapContents = true, AutoScroll = true };
            segRoom = new Seg("Секция 1", "Секция 2") { Width = 220, Selected = 0, Margin = new Padding(4, 2, 4, 2) }; segRoom.Changed += i => { roomSec = i; if (sim != null) SyncCab(); };
            rf.Controls.Add(segRoom); rf.SetFlowBreak(segRoom, true);
            lbUp = new Label { AutoSize = false, Size = new Size(430, 54), ForeColor = Th.Text, Font = Th.F(9f), Margin = new Padding(6, 2, 4, 4) };
            rf.Controls.Add(lbUp); rf.SetFlowBreak(lbUp, true);
            foreach (var it in Room.Items)
            {
                string key = it[0];
                var c = Chk(it[1], true, v => { if (sim != null && !cabSync) sim.Tr.Rm[RoomIndex(key)].Set(key, v); }); c.Margin = new Padding(6, 2, 8, 2); c.Font = Th.F(9f);
                roomChk[key] = c; rf.Controls.Add(c);
            }
            room.Controls.Add(rf);
            t.Controls.Add(room, 2, 1);
            return t;
        }
        readonly List<Tuple<CheckBox, Func<bool>>> cabSw = new List<Tuple<CheckBox, Func<bool>>>();
        readonly Dictionary<string, CheckBox> roomChk = new Dictionary<string, CheckBox>();
        Card roomCard;
        // an apparatus that the scheme has once for the locomotive is kept in the first section whatever section is shown
        int RoomIndex(string key) { return sim.Tr.RoomCommon(key) ? 0 : Math.Min(roomSec, sim.Tr.Sections - 1); }
        Seg segUp, segRoom; Label lbUp; int roomSec; bool cabSync; readonly Label[] lampLb = new Label[Traction.LampCount];
        static CheckBox Chk(string text, bool on, Action<bool> set)
        {
            var c = new CheckBox { Text = text, Checked = on, AutoSize = true, ForeColor = Th.Text, BackColor = Th.Card, FlatStyle = FlatStyle.Flat, Margin = new Padding(8, 10, 16, 4), Font = Th.F(10f) };
            c.CheckedChanged += (s, e) => set(c.Checked); return c;
        }

        // ---------- train
        Control TrainPage()
        {
            var p = new Panel { BackColor = Th.Back };
            var top = new Card("Состав") { Dock = DockStyle.Top, Height = 170, Note = "голова → хвост · щелчок — вагон в таблице" };
            segMetric = new Seg("ТЦ", "ТМ", "Силы в сцепках", "Тормозная сила") { Dock = DockStyle.Top, Height = 36, Width = 520 };
            stripBig = new WagonStrip { Dock = DockStyle.Fill, Rows = 2 };
            stripBig.Picked += i => { if (gridTrain.Rows.Count > i) { gridTrain.ClearSelection(); gridTrain.Rows[i].Selected = true; gridTrain.FirstDisplayedScrollingRowIndex = Math.Max(0, i - 3); } };
            var spacer = new Panel { Dock = DockStyle.Top, Height = 10, BackColor = Th.Card };
            top.Controls.Add(stripBig); top.Controls.Add(spacer); top.Controls.Add(segMetric);
            var tableCard = new Card("Вагоны") { Dock = DockStyle.Fill, Padding = new Padding(10, 40, 10, 10) };
            gridTrain = new DataGridView { Dock = DockStyle.Fill, ReadOnly = true, AllowUserToAddRows = false, AllowUserToResizeRows = false, AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill, SelectionMode = DataGridViewSelectionMode.FullRowSelect };
            Th.Style(gridTrain);
            foreach (var c in new[] { "№", "Вагон", "Масса, т", "Режим · колодки", "ТМ, кгс/см²", "ЗР, кгс/см²", "РК, кгс/см²", "ТЦ, кгс/см²", "Сила торм., кН", "Сцепка сзади, кН", "Состояние" }) gridTrain.Columns.Add(c, c);
            gridTrain.Columns[0].FillWeight = 30; gridTrain.Columns[1].FillWeight = 150; gridTrain.Columns[3].FillWeight = 140; gridTrain.Columns[10].FillWeight = 120;
            tableCard.Controls.Add(gridTrain);
            var gap = new Panel { Dock = DockStyle.Top, Height = 12, BackColor = Th.Back };
            p.Controls.Add(tableCard); p.Controls.Add(gap); p.Controls.Add(top);
            return p;
        }
        void FillTrainGrid()
        {
            gridTrain.Rows.Clear();
            for (int i = 0; i < train.Wagons.Count; i++)
            {
                var w = train.Wagons[i];
                gridTrain.Rows.Add(i + 1, w.TypeName + " " + w.Number, Th.N(w.MassT, "0.0"), (w.Mode == 1 ? "порожний" : w.Mode == 3 ? "гружёный" : "средний") + (w.Mountain ? ", горный" : "") + " · " + (w.Composite ? "композит" : "чугун"), "", "", "", "", "", "", "");
            }
        }

        // ---------- air
        Control AirPage() { var c = new Card("Пневмосхема — тормозная сеть локомотива и поезда") { Padding = new Padding(4, 40, 4, 4) }; scheme = new AirScheme { Dock = DockStyle.Fill }; c.Controls.Add(scheme); return c; }

        // ---------- control of the trip
        Control ControlPage()
        {
            var t = new TableLayoutPanel { ColumnCount = 3, RowCount = 1, BackColor = Th.Back };
            for (int i = 0; i < 3; i++) t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 33.3f));
            var c1 = new Card("Светофоры впереди и препятствия") { Dock = DockStyle.Fill, Margin = new Padding(0, 0, 6, 0) };
            var col1 = new FlowLayoutPanel { Dock = DockStyle.Fill, FlowDirection = FlowDirection.TopDown, WrapContents = false, AutoScroll = true, BackColor = Th.Card };
            lightsFlow = new FlowLayoutPanel { FlowDirection = FlowDirection.TopDown, WrapContents = false, AutoSize = true, BackColor = Th.Card };
            obstaclesFlow = new FlowLayoutPanel { FlowDirection = FlowDirection.TopDown, WrapContents = false, AutoSize = true, BackColor = Th.Card };
            col1.Controls.Add(lightsFlow); col1.Controls.Add(new Label { Text = "Препятствия сценария", AutoSize = true, Font = Th.F(11f, true), ForeColor = Th.Text, Margin = new Padding(0, 14, 0, 6) }); col1.Controls.Add(obstaclesFlow);
            c1.Controls.Add(col1);
            var c2 = new Card("Предупреждение (ДУ-61), знаки, условия") { Dock = DockStyle.Fill, Margin = new Padding(6, 0, 6, 0) };
            var col2 = new FlowLayoutPanel { Dock = DockStyle.Fill, FlowDirection = FlowDirection.LeftToRight, WrapContents = true, BackColor = Th.Card, AutoScroll = true };
            nFrom = Num(0, 5000, 2, 1); nLen = Num(100, 20000, 1000, 100); nV = Num(5, 80, 40, 5);
            col2.Controls.AddRange(new Control[] { Cap("Через, км"), nFrom, Cap("Длина, м"), nLen, Cap("Скорость, км/ч"), nV });
            var bw = new Btn("Выдать предупреждение", Look.Primary, 380, 42); bw.Click += (s, e) => { if (sim != null) Warning(sim.Dyn.HeadX + (double)nFrom.Value * 1000, (double)nLen.Value, (int)nV.Value); };
            col2.Controls.Add(bw); col2.SetFlowBreak(bw, true);
            col2.Controls.Add(Section("Поставить знак"));
            segWhere = new Seg("в текущую точку", "через 1000 м") { Width = 380, Margin = new Padding(4, 2, 4, 6) };
            foreach (var tp in new[] { 5, 2, 4, 6, 17, 18 }) { int tt = tp; var b = new Btn(Route.SignName(tt), Look.Secondary, 186, 40) { Font = Th.F(8.5f, true) }; b.Click += (s, e) => { if (sim != null) AddSign(sim.Dyn.HeadX + (segWhere.Selected == 1 ? 1000 : 0), tt, 0); }; col2.Controls.Add(b); }
            col2.Controls.Add(segWhere); col2.SetFlowBreak(segWhere, true);
            col2.Controls.Add(Section("Время суток и погода"));
            segDay = new Seg("День", "Ночь") { Width = 380, Margin = new Padding(4, 2, 4, 6) }; segDay.Changed += i => { night = i == 1; if (sim != null) sim.Log(night ? "Ночь" : "День"); };
            col2.Controls.Add(segDay); col2.SetFlowBreak(segDay, true);
            chRain = Chk("Дождь · сцепление ×0,85", false, v => Weather()); chSnow = Chk("Снег · ×0,80", false, v => Weather()); chFog = Chk("Туман", false, v => { fog = v; if (sim != null) sim.Log(v ? "Туман" : "Туман рассеялся"); });
            col2.Controls.AddRange(new Control[] { chRain, chSnow, chFog });
            c2.Controls.Add(col2);
            var c3 = new Card("Неисправности") { Dock = DockStyle.Fill, Margin = new Padding(6, 0, 0, 0) };
            faultsFlow = new FlowLayoutPanel { Dock = DockStyle.Fill, FlowDirection = FlowDirection.TopDown, WrapContents = false, AutoScroll = true, BackColor = Th.Card };
            c3.Controls.Add(faultsFlow);
            t.Controls.Add(c1, 0, 0); t.Controls.Add(c2, 1, 0); t.Controls.Add(c3, 2, 0);
            return t;
        }
        static Label Cap(string s) { return new Label { Text = s, AutoSize = false, Size = new Size(120, 34), ForeColor = Th.Muted, TextAlign = ContentAlignment.MiddleLeft, Margin = new Padding(4, 4, 4, 0) }; }
        static Label Section(string s) { var l = new Label { Text = s, AutoSize = false, Size = new Size(380, 32), Font = Th.F(11f, true), ForeColor = Th.Text, TextAlign = ContentAlignment.BottomLeft, Margin = new Padding(4, 10, 4, 4) }; return l; }
        static NumericUpDown Num(int min, int max, int val, int step) { return new NumericUpDown { Minimum = min, Maximum = max, Value = val, Increment = step, Width = 250, BackColor = Th.Inset, ForeColor = Th.Text, BorderStyle = BorderStyle.FixedSingle, Font = Th.F(11f), Margin = new Padding(4, 6, 4, 4) }; }
        void Weather()
        {
            if (sim == null) return;
            route.Rain = chRain.Checked; route.Snow = chSnow.Checked;
            sim.Tr.AdhesionFactor = route.Snow ? 0.8 : route.Rain ? 0.85 : 1;
            sim.Log("Погода: " + (route.Snow ? "снег" : route.Rain ? "дождь" : "сухо"));
        }
        void FillLights()
        {
            if (sim == null) return;
            lightsFlow.SuspendLayout(); lightsFlow.Controls.Clear();
            double head = sim.Dyn.HeadX; int shown = 0;
            for (int i = 0; i < route.Lights.Count && shown < 6; i++)
            {
                var l = route.Lights[i]; if (l.X < head) continue; shown++;
                int idx = i;
                var row = new FlowLayoutPanel { AutoSize = true, MaximumSize = new Size(420, 0), BackColor = Th.Inset, Padding = new Padding(8), Margin = new Padding(0, 0, 0, 8), WrapContents = true };
                var name = new Label { Text = l.Name + " · " + Th.N(l.X - head, "0") + " м", AutoSize = false, Size = new Size(200, 26), Font = Th.F(10f, true), ForeColor = Th.Text, TextAlign = ContentAlignment.MiddleLeft };
                var asp = new Label { Text = Route.AspectName(l.Aspect), AutoSize = false, Size = new Size(190, 26), ForeColor = Route.IsProhibiting(l.Aspect) ? Th.Warn : Route.AspectLimit(l.Aspect) < 80 ? Th.Amber : Th.Green, TextAlign = ContentAlignment.MiddleRight };
                row.Controls.Add(name); row.Controls.Add(asp); row.SetFlowBreak(asp, true);
                var codes = l.Combinations.Length > 0 ? l.Combinations : new[] { 0, 2, 1, 8, 10, 4, 19 };
                foreach (int c in codes.Take(8)) { int cc = c; var b = new Btn(Short(cc), cc == l.Aspect ? Look.Selected : Look.Secondary, 48, 32) { Font = Th.F(8.5f, true) }; b.Click += (s, e) => SetAspect(idx, cc); row.Controls.Add(b); }
                lightsFlow.Controls.Add(row);
            }
            lightsFlow.ResumeLayout();
        }
        static string Short(int c)
        {
            switch (c) { case 0: return "З"; case 1: return "К"; case 2: return "Ж"; case 4: return "Б"; case 6: return "З м"; case 7: return "Зм Ж"; case 8: return "Ж м"; case 9: return "Жм Б"; case 10: return "2Ж"; case 11: return "2З"; case 12: return "3Ж"; case 13: return "2Ж м"; case 14: return "КБ"; case 15: return "КБм"; case 16: return "ЖЗ"; case 17: return "ЖБ"; case 18: return "ЗБ"; case 19: return "нет"; default: return "все"; }
        }
        void FillObstacles()
        {
            obstaclesFlow.Controls.Clear();
            if (route == null) return;
            if (route.Obstacles.Count == 0) obstaclesFlow.Controls.Add(new Label { Text = "в сценарии нет; правая кнопка на карте — поставить", AutoSize = true, ForeColor = Th.Muted });
            foreach (var o in route.Obstacles)
            {
                var ob = o;
                var c = Chk(o.Name + " · " + route.KmPk(o.X), o.Active, v => { ob.Active = v; if (sim != null) sim.Log((v ? "Препятствие включено: " : "Препятствие снято: ") + ob.Name + " на " + route.KmPk(ob.X)); });
                c.BackColor = Th.Card; obstaclesFlow.Controls.Add(c);
            }
        }
        void FillFaults()
        {
            faultsFlow.Controls.Clear();
            string group = "";
            foreach (var f in sim.Faults)
            {
                if (f.Group != group) { group = f.Group; faultsFlow.Controls.Add(new Label { Text = group == "Тормоза" ? "Тормозное оборудование" : "Электрические цепи", AutoSize = true, ForeColor = Th.Muted, Margin = new Padding(0, 8, 0, 4) }); }
                var ff = f;
                var c = Chk(f.Name, false, v => sim.SetFault(ff, v)); c.MaximumSize = new Size(440, 0); c.Margin = new Padding(0, 4, 0, 4);
                faultsFlow.Controls.Add(c);
            }
        }

        // ---------- journal, documents, cassette, archive
        Control JournalPage()
        {
            var t = new TableLayoutPanel { ColumnCount = 2, RowCount = 1, BackColor = Th.Back };
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 66)); t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 34));
            var left = new Card("Нарушения") { Dock = DockStyle.Fill, Margin = new Padding(0, 0, 6, 0), Padding = new Padding(10, 40, 10, 10) };
            lbMark = new Label { Dock = DockStyle.Top, Height = 28, ForeColor = Th.Muted, Font = Th.F(10f), BackColor = Th.Card };
            var split = new SplitContainer { Dock = DockStyle.Fill, Orientation = Orientation.Horizontal, BackColor = Th.Line, SplitterWidth = 2 };
            journalList = new ViolationList { Dock = DockStyle.Fill };
            events = new ListBox { Dock = DockStyle.Fill, BackColor = Th.Card, ForeColor = Th.Muted, BorderStyle = BorderStyle.None, Font = Th.Mono(9f, false), HorizontalScrollbar = true };
            split.Panel1.BackColor = Th.Card; split.Panel2.BackColor = Th.Card;
            split.Panel1.Controls.Add(journalList); split.Panel2.Controls.Add(events); split.Panel2.Controls.Add(new Label { Text = "События поездки", Dock = DockStyle.Top, Height = 26, ForeColor = Th.Text, Font = Th.F(10.5f, true) });
            left.Controls.Add(split); left.Controls.Add(lbMark);
            split.HandleCreated += (s, e) => { try { split.SplitterDistance = (int)(split.Height * 0.62); } catch { } };
            var right = new TableLayoutPanel { Dock = DockStyle.Fill, RowCount = 3, ColumnCount = 1, BackColor = Th.Back, Margin = new Padding(6, 0, 0, 0) };
            right.RowStyles.Add(new RowStyle(SizeType.Absolute, 330)); right.RowStyles.Add(new RowStyle(SizeType.Absolute, 170)); right.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
            var docs = new Card("Документы") { Dock = DockStyle.Fill, Margin = new Padding(0, 0, 0, 6) };
            var df = new FlowLayoutPanel { Dock = DockStyle.Fill, FlowDirection = FlowDirection.TopDown, WrapContents = false, BackColor = Th.Card };
            var d1 = new Btn("Справка ВУ-45 · нажатие, плотность, отпуск хвоста", Look.Secondary, 460, 44) { TextAlign = ContentAlignment.MiddleLeft }; d1.Click += (s, e) => Doc("vu45.html", () => Documents.Vu45(train, sim.TrainNumber, Documents.Measure(train)));
            var d2 = new Btn("Предупреждения ДУ-61", Look.Secondary, 460, 44) { TextAlign = ContentAlignment.MiddleLeft }; d2.Click += (s, e) => Doc("du61.html", () => Documents.Du61(route, sim.TrainNumber, sim.Dyn.HeadX));
            var d3 = new Btn("Протокол поездки с оценкой", Look.Secondary, 460, 44) { TextAlign = ContentAlignment.MiddleLeft }; d3.Click += (s, e) => Doc("protocol.html", () => Documents.Protocol(sim, tripNo));
            var d4 = new Btn("Расшифровка кассеты — лента", Look.Primary, 460, 44) { TextAlign = ContentAlignment.MiddleLeft }; d4.Click += (s, e) => OpenTape(tripDir != null ? Path.Combine(tripDir, "trip.kr2") : null);
            var d5 = new Btn("Открыть кассету из файла…", Look.Ghost, 460, 36) { TextAlign = ContentAlignment.MiddleLeft }; d5.Click += (s, e) => OpenTape(null);
            df.Controls.AddRange(new Control[] { d1, d2, d3, d4, d5 });
            docs.Controls.Add(df);
            var cas = new Card("Кассета") { Dock = DockStyle.Fill, Margin = new Padding(0, 6, 0, 6) }; plCassette = new ParamList { Dock = DockStyle.Fill }; cas.Controls.Add(plCassette);
            var arc = new Card("Архив поездок") { Dock = DockStyle.Fill, Margin = new Padding(0, 6, 0, 0), Note = "двойной щелчок — протокол" };
            archive = new ListBox { Dock = DockStyle.Fill, BackColor = Th.Card, ForeColor = Th.Text, BorderStyle = BorderStyle.None, Font = Th.F(10f), IntegralHeight = false };
            archive.DoubleClick += (s, e) => { var it = archive.SelectedItem as ArchiveItem; if (it == null) return; var pr = Path.Combine(it.Dir, "protocol.html"); try { Open(File.Exists(pr) ? pr : it.Dir); } catch { } };
            arc.Controls.Add(archive);
            right.Controls.Add(docs, 0, 0); right.Controls.Add(cas, 0, 1); right.Controls.Add(arc, 0, 2);
            t.Controls.Add(left, 0, 0); t.Controls.Add(right, 1, 0);
            return t;
        }
        sealed class ArchiveItem { public string Dir, Text; public override string ToString() { return Text; } }
        void RefreshArchive()
        {
            archive.Items.Clear();
            var root = Path.Combine(dataDir, "trips"); if (!Directory.Exists(root)) return;
            foreach (var d in Directory.GetDirectories(root).OrderByDescending(x => x))
            {
                var n = Path.GetFileName(d); string when = n.Length >= 15 ? n.Substring(6, 2) + "." + n.Substring(4, 2) + " " + n.Substring(9, 2) + ":" + n.Substring(11, 2) : n;
                archive.Items.Add(new ArchiveItem { Dir = d, Text = when + (File.Exists(Path.Combine(d, "protocol.html")) ? " · протокол" : "") + (File.Exists(Path.Combine(d, "trip.kr2")) ? " · кассета" : "") });
            }
        }
        // documents and folders are opened by the system; in the hidden window of the tests they are only noted
        public readonly List<string> Opened = new List<string>(); public ContextMenuStrip MapMenu;
        void Open(string path) { Opened.Add(path); if (!Program.Arguments.ContainsKey("hidden")) Process.Start(path); }
        void Doc(string name, Func<string> make)
        {
            if (sim == null) { MessageBox.Show(this, "Сначала начните поездку"); return; }
            try { Cursor = Cursors.WaitCursor; Open(Documents.Save(tripDir, name, make())); }
            catch (Exception ex) { MessageBox.Show(this, ex.Message, "Документ не создан"); }
            finally { Cursor = Cursors.Default; }
        }
        void OpenTape(string path)
        {
            if (path == null || !File.Exists(path))
                using (var d = new OpenFileDialog { Filter = "Кассета 2ТЭ116 (*.kr2)|*.kr2", InitialDirectory = Path.Combine(dataDir, "trips") })
                { if (d.ShowDialog(this) != DialogResult.OK) return; path = d.FileName; }
            try
            {
                if (sim != null && sim.Cassette != null && sim.Cassette.Path == path) sim.Cassette.Flush();
                var rec = Cassette.Read(path);
                var outp = Path.Combine(Path.GetDirectoryName(path), "tape.html");
                File.WriteAllText(outp, Documents.Tape(rec, route), new System.Text.UTF8Encoding(true));
                Cassette.ExportCsv(rec, Path.ChangeExtension(path, ".csv"));
                Open(outp);
            }
            catch (Exception ex) { MessageBox.Show(this, ex.Message, "Кассета не читается"); }
        }

        // ---------- trip
        Control TripPage()
        {
            var t = new TableLayoutPanel { ColumnCount = 3, RowCount = 1, BackColor = Th.Back };
            t.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 380)); t.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100)); t.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 400));
            var c1 = new Card("1. Участок") { Dock = DockStyle.Fill, Margin = new Padding(0, 0, 6, 0) };
            routes = new CardList { Dock = DockStyle.Fill };
            c1.Controls.Add(routes);
            LoadRoutes();
            var c2 = new Card("2. Поезд") { Dock = DockStyle.Fill, Margin = new Padding(6, 0, 6, 0) };
            segSource = new Seg("Встроенные", "Свои", "База инструктора") { Dock = DockStyle.Top, Height = 36 }; segSource.Changed += i => LoadTrains();
            trains = new CardList { Dock = DockStyle.Fill }; trains.SelectedIndexChanged += (s, e) => ShowTrain();
            plSummary = new ParamList { Dock = DockStyle.Bottom, Height = 200 };
            var bar = new FlowLayoutPanel { Dock = DockStyle.Bottom, Height = 52, BackColor = Th.Card, Padding = new Padding(0, 6, 0, 0) };
            var bNew = new Btn("Создать поезд…", Look.Secondary, 170); bNew.Click += (s, e) => EditTrain(null);
            var bEdit = new Btn("Изменить выбранный…", Look.Secondary, 200); bEdit.Click += (s, e) => EditTrain(SelectedTrain());
            bar.Controls.AddRange(new Control[] { bNew, bEdit });
            c2.Controls.Add(trains); c2.Controls.Add(new Panel { Dock = DockStyle.Top, Height = 10, BackColor = Th.Card }); c2.Controls.Add(segSource); c2.Controls.Add(bar); c2.Controls.Add(plSummary);
            var c3 = new Card("3. Бригада и отправление") { Dock = DockStyle.Fill, Margin = new Padding(6, 0, 0, 0) };
            var f = new FlowLayoutPanel { Dock = DockStyle.Fill, FlowDirection = FlowDirection.TopDown, WrapContents = false, BackColor = Th.Card };
            tbStudent = Field(f, "Машинист (ФИО)", ""); tbTab = Field(f, "Табельный номер", ""); tbInstructor = Field(f, "Инструктор", ""); tbNumber = Field(f, "Номер поезда", "2001");
            chCharged = Chk("Поезд заряжен к отправлению", true, v => { }); f.Controls.Add(chCharged);
            lbPultTrip = new Label { Text = "Пульт кабины: —", AutoSize = false, Size = new Size(360, 100), ForeColor = Th.Muted, Margin = new Padding(0, 6, 0, 6) };
            f.Controls.Add(lbPultTrip);
            var start = new Btn("Начать поездку", Look.Primary, 360, 54) { Font = Th.F(12f, true) }; start.Click += (s, e) => StartTrip();
            f.Controls.Add(start);
            c3.Controls.Add(f);
            t.Controls.Add(c1, 0, 0); t.Controls.Add(c2, 1, 0); t.Controls.Add(c3, 2, 0);
            LoadTrains();
            return t;
        }
        static TextBox Field(FlowLayoutPanel f, string caption, string value)
        {
            f.Controls.Add(new Label { Text = caption, AutoSize = true, ForeColor = Th.Muted, Margin = new Padding(0, 8, 0, 4) });
            var t = new TextBox { Text = value, Width = 360 }; Th.Style(t); f.Controls.Add(t); return t;
        }
        void LoadRoutes()
        {
            string[] files;
            try { files = Directory.GetFiles(Program.ScenarioDir, "*.json").OrderBy(x => x).ToArray(); } catch { files = new string[0]; }
            foreach (var file in files) routes.Items.Add(new CardItem { Title = Path.GetFileNameWithoutExtension(file), Sub = "читается…", Tag = file });
            int pick = Array.FindIndex(files, x => Path.GetFileName(x) == "1_1.json"); if (routes.Items.Count > 0) routes.SelectedIndex = Math.Max(0, pick);
            Task.Run(() =>
            {
                foreach (var file in files)
                {
                    string title, sub;
                    try { var r = Route.LoadScenario(file); title = r.Name; sub = string.Format(C, "{0} · {1:0} км · {2} станций · {3} светофоров · руководящий {4} ‰", Path.GetFileNameWithoutExtension(file), r.Length / 1000, r.StationX.Length, r.Lights.Count, Th.N(r.RulingGrade, "+0.0")); }
                    catch (Exception ex) { title = Path.GetFileName(file); sub = "не читается: " + ex.Message; }
                    string f2 = file;
                    try { BeginInvoke(new Action(() => { foreach (CardItem it in routes.Items) if ((string)it.Tag == f2) { it.Title = title; it.Sub = sub; } routes.Invalidate(); })); } catch { }
                }
            });
        }
        void LoadTrains()
        {
            trains.Items.Clear(); trainItems.Clear();
            if (segSource.Selected == 0) trainItems.AddRange(Catalog.BuiltIn());
            else if (segSource.Selected == 1) foreach (var file in Directory.GetFiles(Path.Combine(dataDir, "trains"), "*.json")) { try { trainItems.Add(Train.Load(file)); } catch { } }
            else if (File.Exists(Program.DbPath)) { Catalog.LoadTypes(Program.DbPath); foreach (var id in Catalog.Compositions(Program.DbPath)) { try { trainItems.Add(Catalog.FromDatabase(Program.DbPath, id)); } catch { } } }
            foreach (var t in trainItems) trains.Items.Add(new CardItem { Title = t.Name, Sub = string.Format(C, "{0} ваг. · {1:0} т · {2} осей · {3:0} м · нажатие {4} тс/100 т", t.Wagons.Count, t.MassT, t.Axles, t.LengthM, Th.N(t.PressingPer100t, "0.0")), Tag = t });
            if (trains.Items.Count > 0) trains.SelectedIndex = 0; else plSummary.Set(new List<Tuple<string, string, Color>> { Tuple.Create(segSource.Selected == 1 ? "Своих поездов пока нет — «Создать поезд…»" : "Нет составов", "", Th.Muted) });
        }
        Train SelectedTrain() { int i = trains.SelectedIndex; return i >= 0 && i < trainItems.Count ? trainItems[i] : null; }
        void ShowTrain()
        {
            var t = SelectedTrain(); if (t == null) return;
            bool ok = t.PressingPer100t >= t.NormPer100t;
            plSummary.Set(new List<Tuple<string, string, Color>>
            {
                Tuple.Create("Масса вагонов / с локомотивом", Th.N(t.WagonsMassT, "0") + " / " + Th.N(t.MassT, "0") + " т", Th.Text),
                Tuple.Create("Вагонов · осей · длина", t.Wagons.Count + " · " + t.WagonAxles + " · " + Th.N(t.LengthM, "0") + " м", Th.Text),
                Tuple.Create("Нажатие на 100 т (норма " + Th.N(t.NormPer100t, "0") + ")", Th.N(t.PressingPer100t, "0.0") + " тс", ok ? Th.Green : Th.Warn),
                Tuple.Create("Зарядное давление (табл. 1)", Th.N(t.ChargeMPa * Traction.KgfPerMPa, "0.0") + " кгс/см²", Th.Text),
                Tuple.Create("Порожних вагонов", Th.N(t.EmptyShare * 100, "0") + " %", Th.Text),
                Tuple.Create("Выключенных ВР", t.Wagons.Count(w => !w.VrOn).ToString(C), t.Wagons.Any(w => !w.VrOn) ? Th.Amber : Th.Text),
            });
        }
        void EditTrain(Train from)
        {
            using (var dlg = new TrainEditor(from))
                if (dlg.ShowDialog(this) == DialogResult.OK && dlg.Result != null)
                {
                    string name = string.Join("_", dlg.Result.Name.Split(Path.GetInvalidFileNameChars()));
                    dlg.Result.Save(Path.Combine(dataDir, "trains", name + ".json"));
                    segSource.Selected = 1; LoadTrains();
                    for (int i = 0; i < trainItems.Count; i++) if (trainItems[i].Name == dlg.Result.Name) trains.SelectedIndex = i;
                }
        }

        void StartTrip()
        {
            var ri = routes.SelectedItem as CardItem; var t0 = SelectedTrain();
            if (ri == null || t0 == null) { MessageBox.Show(this, "Выберите участок и поезд"); return; }
            StartTripWith((string)ri.Tag, t0);
        }
        // a trip given by the command line (--start --scenario --train), for the tests and a prepared stand
        void AutoStart()
        {
            var a = Program.Arguments;
            if (a.ContainsKey("speed")) { int i = Array.IndexOf(new[] { "1", "2", "5", "10" }, a["speed"]); if (i >= 0) { segSpeed.Selected = i; speedUp = new[] { 1.0, 2, 5, 10 }[i]; } else { int k; if (int.TryParse(a["speed"], out k) && k >= 1 && k <= 50) speedUp = k; } }   // other speeds up to x50: the stands
            if (!a.ContainsKey("start")) return;
            try
            {
                if (a.ContainsKey("student")) tbStudent.Text = a["student"];
                if (a.ContainsKey("number")) tbNumber.Text = a["number"];
                StartTripWith(a["scenario"], Program.MakeTrain(a.ContainsKey("train") ? a["train"] : "builtin:0", a.ContainsKey("db") ? a["db"] : Program.DbPath));
            }
            catch (Exception ex) { TripError(ex); }
        }
        void TripError(Exception ex)
        {
            AppLog.Write("ПОЕЗДКА", "не начата: " + ex);
            if (!Program.Arguments.ContainsKey("hidden")) { MessageBox.Show(this, ex.Message, "Поездка не начата", MessageBoxButtons.OK, MessageBoxIcon.Warning); return; }
            try { Directory.CreateDirectory(dataDir); File.WriteAllText(Path.Combine(dataDir, "start_error.txt"), ex.ToString()); } catch { }
            Environment.Exit(3);
        }
        void StartTripWith(string scenario, Train t0)
        {
            try
            {
                if (sim != null && sim.Cassette != null) sim.Cassette.Dispose();
                AppLog.Write("ПОЕЗДКА", "начинается: сценарий " + scenario);
                route = Route.LoadScenario(scenario);
                train = Train.Load(SaveTemp(t0)); train.Number = tbNumber.Text;
                tripNo = Directory.Exists(Path.Combine(dataDir, "trips")) ? Directory.GetDirectories(Path.Combine(dataDir, "trips")).Length + 1 : 1;
                tripDir = Path.Combine(dataDir, "trips", DateTime.Now.ToString("yyyyMMdd_HHmmss", C) + "_" + tripNo);
                Directory.CreateDirectory(tripDir);
                sim = new Simulation { DieselCondition = Program.Arguments.ContainsKey("condition") ? Program.Arguments["condition"] : "normal", Student = tbStudent.Text + (tbTab.Text.Length > 0 ? ", таб. № " + tbTab.Text : ""), Instructor = tbInstructor.Text, TrainNumber = tbNumber.Text };
                Journal.Configure(tripDir);
                sim.Start(route, train, Program.StartHead(route, train), chCharged.Checked);
                electricView.Bind(sim.Tr.Electric, true);
                sim.Cassette = new Cassette(Path.Combine(tripDir, "trip.kr2"), new Dictionary<string, object> { { "route", route.Name }, { "train", train.ToString() }, { "number", tbNumber.Text }, { "start", DateTime.Now.ToString("s") } });
                // the video shows the route of the chosen scenario (a route given by --road stays)
                if (!Program.Arguments.ContainsKey("road"))
                {
                    int road = Program.RoadOf(scenario, route);
                    if (road >= 0) { video.SetRoad(road); sim.Log("Видеосистема: маршрут " + road); }
                    else sim.Log("Видеосистема: для этого участка нет маршрута видео, оставлен маршрут " + video.Road);
                }
                pult.Reset(); pultOnLine = false; video.Reset(); btPause.Text = "Пауза"; btPause.Invalidate();
                trace.Clear(); lastTraceX = double.NaN; map.Trace.Clear();
                chRain.Checked = route.Rain; chSnow.Checked = route.Snow; chFog.Checked = route.Fog; segDay.Selected = route.Day ? 0 : 1;
                scheme.Sim = sim;
                FillFaults(); FillLights(); FillObstacles(); FillTrainGrid();
                lbTitle.Text = route.Name;
                lbSub.Text = string.Format(C, "Поезд {0} · {1} вагонов, {2:0} т, {3} осей, {4:0} м · машинист {5}", tbNumber.Text, train.Wagons.Count, train.MassT, train.WagonAxles, train.LengthM, tbStudent.Text.Length > 0 ? tbStudent.Text : "—");
                SyncCab();
                Show(0);
                AppLog.Write("ПОЕЗДКА", "№ {0} начата: участок «{1}», поезд № {2}: {3} вагонов, {4:0} т, {5:0} м; секций {6}, состояние дизелей «{7}», {8}; машинист «{9}», инструктор «{10}»; папка {11}",
                    tripNo, route.Name, tbNumber.Text, train.Wagons.Count, train.MassT, train.LengthM, sim.Tr.Sections, sim.DieselCondition, chCharged.Checked ? "тормоза заряжены" : "тормоза не заряжены", tbStudent.Text, tbInstructor.Text, tripDir);
            }
            catch (Exception ex) { TripError(ex); }
        }
        string SaveTemp(Train t) { var p = Path.Combine(Path.GetTempPath(), "trainer2te116_train.json"); t.Save(p); return p; }

        void FinishTrip()
        {
            if (sim == null || sim.Finished) return;
            // the button stands next to "Pause": an accidental click must not end the trip
            if (!Program.Arguments.ContainsKey("hidden") && MessageBox.Show(this, "Закончить поездку? Продолжить её будет нельзя: кассета закрывается, записывается протокол.", "Закончить поездку", MessageBoxButtons.YesNo, MessageBoxIcon.Question, MessageBoxDefaultButton.Button2) != DialogResult.Yes) return;
            sim.Finished = true; sim.Log("Поездка закончена инструктором");
            if (sim.Cassette != null) { sim.Cassette.Dispose(); sim.Cassette = null; }
            try { Documents.Save(tripDir, "protocol.html", Documents.Protocol(sim, tripNo)); AppLog.Write("ПОЕЗДКА", "№ " + tripNo + " закончена, протокол записан: " + Path.Combine(tripDir, "protocol.html")); }
            catch (Exception ex) { AppLog.Write("ПОЕЗДКА", "протокол не записан: " + ex.Message); }
            Show(5);
        }

        // ---------- the journal (AppLog): the duration of the ticks, the state of the links and of the model every 10 s
        readonly System.Diagnostics.Stopwatch logClock = System.Diagnostics.Stopwatch.StartNew();
        long logNext = 10000, tickMax; int tickCount; string videoStateLog, blokStateLog, blokAnswerLog; long videoSentLog, blokSentLog, blokRecvLog;
        void TickLogged()
        {
            long t0 = logClock.ElapsedMilliseconds;
            Tick();
            if (!AppLog.Enabled) return;
            long d = logClock.ElapsedMilliseconds - t0; if (d > tickMax) tickMax = d; tickCount++;
            LinksLog();
            if (t0 < logNext) return;
            logNext = t0 + 10000;
            AppLog.Write("ПРОГРАММА", "за 10 с: тактов {0} (шаг таймера {1} мс), наибольшая длительность такта {2} мс; память {3:0} МБ", tickCount, timer.Interval, tickMax, GC.GetTotalMemory(false) / 1048576.0);
            tickMax = 0; tickCount = 0;
            if (video.Enabled) { AppLog.Write("ВИДЕО", "{0}; отправлено сообщений {1} (за 10 с {2})", video.Connected ? "на связи" : video.State, video.Sent, video.Sent - videoSentLog); videoSentLog = video.Sent; }
            if (blok.Enabled) { AppLog.Write("БЛОК", "{0}; за 10 с отправлено {1}, принято {2}", blok.Connected ? "на связи" : blok.State, blok.Sent - blokSentLog, blok.Received - blokRecvLog); blokSentLog = blok.Sent; blokRecvLog = blok.Received; }
            if (sim == null) return;
            try
            {
                var c = sim.C; var a = sim.Air; var sb = new System.Text.StringBuilder();
                sb.AppendFormat(C, "t={0:0} с{1}, v={2:0.0} км/ч (допустимая {3:0}), голова {4:0} м; органы: реверсор {5}, позиция {6}, кран 395 {7}, кран 254 {8}, ЭПК {9}; ТМ {10:0.00}, УР {11:0.00}, ГР {12:0.00}, ТЦ {13:0.00} кгс/см²",
                    sim.T, sim.Finished ? " (закончена)" : sim.Paused ? " (пауза)" : "", sim.SpeedKmh, sim.Allowed, sim.Dyn.HeadX, c.Reverser == 1 ? "вперёд" : c.Reverser == 2 ? "назад" : "нейтраль", c.Position, c.Crane, c.Kvt, c.EpkKey ? 1 : 0,
                    a.TmLoco.MPa * Traction.KgfPerMPa, a.Ur.MPa * Traction.KgfPerMPa, a.Mr.MPa * Traction.KgfPerMPa, a.LocoTcMPa * Traction.KgfPerMPa);
                for (int i = 0; i < sim.Tr.Sections && i < sim.Tr.S.Length; i++)
                {
                    var s = sim.Tr.S[i];
                    sb.AppendFormat(C, "; дизель {0}: {1:0} об/мин, масло {2:0.0} кгс/см², ток ГГ {3:0} А, U {4:0} В", i + 1, s.Rpm, s.OilP * Traction.KgfPerMPa, s.Current, s.VoltageKv * 1000);
                }
                AppLog.Write("МОДЕЛЬ", sb.ToString());
            }
            catch (Exception ex) { AppLog.Write("МОДЕЛЬ", "состояние не записано: " + ex.Message); }
        }
        void LinksLog()
        {
            if (video.State != videoStateLog) { videoStateLog = video.State; AppLog.Write("ВИДЕО", "состояние: " + videoStateLog + " (" + video.Ip + ":" + video.Port + ", маршрут " + video.Road + ", отправлено сообщений " + video.Sent + ")"); }
            string bs = blok.Connected ? "на связи" : blok.State;
            if (bs != blokStateLog) { blokStateLog = bs; AppLog.Write("БЛОК", "состояние: " + bs + " (" + blok.Ip + ":" + blok.Port + ", отправлено " + blok.Sent + ", принято " + blok.Received + ")"); }
            if (blok.Connected)
            {
                string an = "катушка ЭПК " + (blok.HoldingCoil ? 1 : 0) + ", допустимая " + blok.AllowedKmh + " км/ч, код сигнала " + blok.SignalCode + ", отключение тяги " + (blok.TractionOff ? 1 : 0) + ", лампа «внимание» " + (blok.LampAttention ? 1 : 0) + ", лампа ПСС " + (blok.LampPss ? 1 : 0);
                if (an != blokAnswerLog) { blokAnswerLog = an; AppLog.Write("БЛОК", "ответ: " + an); }
            }
        }

        // ---------- loop
        void Tick()
        {
            PultState();
            if (sim == null) return;
            // time is not sped up while the pult of the cab is on line (TZ, section 7), except a stand started with --speed
            int frames = pult.Connected && !Program.Arguments.ContainsKey("speed") ? 1 : (int)Math.Round(speedUp);
            // the native test (--hidden) sets the speed by the spare cell 43 of its emulated pult
            if (Program.Arguments.ContainsKey("hidden") && pult.LastInput != null && pult.LastInput[43] > 0) frames = Math.Min(50, (int)pult.LastInput[43]);
            for (int i = 0; i < frames && !sim.Paused && !sim.Finished; i++)
            {
                if (pult.Apply(sim.C)) SyncCab();
                sim.Step(0.1);
            }
            if (pult.Apply(sim.C)) SyncCab();
            // BLOK: the holding coil of the EPK and the lamps while it answers; the video: the changes of the trip
            bool bl = blok.Connected;
            sim.HoldingCoil = !bl || blok.HoldingCoil; sim.LampVigilance = bl && blok.LampAttention; sim.LampPss = bl && blok.LampPss;
            blok.Publish(sim, route, pult.Connected ? pult.LastInput : null, 0.1 * Math.Max(1, frames));
            route.Day = !night; route.Fog = fog;
            video.Update(sim, route, bl && blok.AllowedKmh > 0 ? blok.AllowedKmh : sim.Allowed, sim.Paused || sim.Finished);
            pult.Publish(sim);
            double head = sim.Dyn.HeadX;
            if (double.IsNaN(lastTraceX) || Math.Abs(head - lastTraceX) >= 5) { lastTraceX = head; trace.Add(new[] { head, Math.Abs(sim.SpeedKmh), sim.Allowed }); map.Trace.Add(trace[trace.Count - 1]); }
            map.RouteNow = route; map.LiveNow = Live();
            if (pages[0].Visible) map.Invalidate();
            UpdateChips();
            // the switches on the screen always show the state of the model, with the pult and without it
            if (sim.T >= nextUi || sim.Paused) { nextUi = sim.T + 0.5; UpdatePages(); SyncCab(); }
        }
        // state of the link with the pult: header, trip page, the cab card, and an event of the trip when it changes
        void PultState()
        {
            bool on = pult.Connected;
            string pt = on ? "есть" : !pult.Enabled ? "выкл" : !pult.Bound ? "порт занят" : pult.Received > 0 ? "нет связи" : "ждём";
            string vt = video.Connected ? "есть" : video.Enabled ? "нет" : "выкл", bt = blok.Connected ? "есть" : blok.Enabled ? "нет" : "выкл";
            string text = "Пульт: " + pt + " · Видео: " + vt + " · БЛОК: " + bt;
            bool allOn = (on || !pult.Enabled) && (video.Connected || !video.Enabled) && (blok.Connected || !blok.Enabled), anyEnabled = pult.Enabled || video.Enabled || blok.Enabled;
            if (lbPult.Text != text) { lbPult.Text = text; lbPult.ForeColor = !anyEnabled ? Th.Muted : allOn ? Th.Green : Th.Warn; }
            string full = "Пульт " + pult.Ip + ":" + pult.Port + ": " + (on ? "на связи" : pult.State) + "\nВидео " + video.Ip + ":" + video.Port + ", маршрут " + video.Road + ": " + video.State
                + "\nБЛОК " + blok.Ip + ":" + blok.Port + ": " + (blok.Connected ? "на связи" : blok.State);
            if (sim != null && video.Connected != videoOnLine) { videoOnLine = video.Connected; sim.Log(videoOnLine ? "Видеосистема: на связи" : "Видеосистема: нет связи"); }
            if (sim != null && blok.Connected != blokOnLine) { blokOnLine = blok.Connected; sim.Log(blokOnLine ? "БЛОК: на связи" : "БЛОК: нет связи"); }
            if (lbPultTrip.Text != full) lbPultTrip.Text = full;
            string note = on ? "пульт кабины на связи; кнопки на экране двигают органы поверх пульта" : "органы управления инструктора; пульт кабины не на связи";
            if (cabCard.Note != note) { cabCard.Note = note; cabCard.Invalidate(); }
            if (sim != null && on != pultOnLine) { pultOnLine = on; sim.Log(on ? "Пульт кабины: на связи" : "Пульт кабины: нет связи"); }
        }
        void SyncCab()
        {
            if (tbPos.Value != sim.C.Position) tbPos.Value = sim.C.Position;
            if (segRev.Selected != sim.C.Reverser) segRev.Selected = sim.C.Reverser;
            chSand.Checked = sim.C.Sand; chWhistle.Checked = sim.C.Whistle; chTyphon.Checked = sim.C.Typhon; chSpot.Checked = sim.C.Spotlight; chEpk.Checked = sim.C.EpkKey;
            cabSync = true;
            try
            {
                foreach (var p in cabSw) { bool v = p.Item2(); if (p.Item1.Checked != v) p.Item1.Checked = v; }
                if (segUp.Selected != sim.C.DamageSw) segUp.Selected = sim.C.DamageSw;
                var lit = sim.Tr.PultLamps(sim.Air.Lamp418);
                for (int k = 0; k < lampLb.Length; k++)
                {
                    var lb = lampLb[k]; if (lb == null) continue; bool burnt = sim.Tr.LampBurnt[k];
                    Color bc = burnt ? Th.WarnBack : lit[k] ? (k == 8 ? Th.Green : Th.Red) : Th.Inset, fc = burnt ? Th.Warn : lit[k] ? Color.White : Th.Dim;
                    string tx = Traction.LampNames[k] + (burnt ? " ✕" : "");
                    if (lb.BackColor != bc) lb.BackColor = bc; if (lb.ForeColor != fc) lb.ForeColor = fc; if (lb.Text != tx) lb.Text = tx;
                }
                var tr = sim.Tr; int rs = Math.Min(roomSec, tr.Sections - 1); var rm = tr.Rm[rs];
                foreach (var kv in roomChk)
                {
                    bool common = tr.RoomCommon(kv.Key), v = tr.Rm[RoomIndex(kv.Key)].Get(kv.Key);
                    if (kv.Value.Checked != v) kv.Value.Checked = v;
                    Color fc = common ? Th.Muted : Th.Text; if (kv.Value.ForeColor != fc) kv.Value.ForeColor = fc;
                }
                string rn = tr.Sections < 2 ? "одна секция" : "секция " + (rs + 1) + "; серые — общие на обе секции";
                if (roomCard.Note != rn) { roomCard.Note = rn; roomCard.Invalidate(); }
                string chain; var links = tr.DamageChain(rs, out chain); string open = Chain.FirstOpen(links);
                string lamps = (tr.LampPowerProtection ? "«Силовая защита» " : "") + (tr.LampCooling ? "«Обдув» " : "") + (tr.LampFire ? "«Пожар» " : "") + (tr.LampWaterLevel ? "«Уровень воды» " : "") + (tr.LampOilPressure ? "«Давление масла» " : "");
                string text = "Указатель повреждений, цепь «" + chain + "»: " + Chain.Reading(links).ToString("0.0", C) + " из 10" + (open != null ? "\nразомкнуто: " + open : "\nцепь собрана")
                    + "\nЛампы: " + (lamps.Length > 0 ? lamps : "не горят") + (rm.RZ ? " · РЗ на защёлке" : "") + (rm.ROP ? " · РОП на защёлке" : "") + (rm.RM2 ? " · РМ2 на защёлке" : "");
                if (lbUp.Text != text) lbUp.Text = text;
            }
            finally { cabSync = false; }
        }

        RegimeLive Live()
        {
            var l = new RegimeLive
            {
                T = sim.T, Head = sim.Dyn.HeadX, V = Math.Abs(sim.SpeedKmh), Allowed = sim.Allowed, TrainLen = sim.Dyn.LengthM, MassT = train.MassT, Tm = sim.Air.TmLoco.MPa, Ur = sim.Air.Ur.MPa,
                Tc = sim.Air.LocoTcMPa, Pm = sim.Air.Mr.MPa, Charge = sim.Air.ChargeMPa, TractionKn = sim.Tr.TotalForceKn, Km = sim.C.Position, Crane = sim.C.Crane, Kvt = sim.C.Kvt,
                Axles = train.WagonAxles, Wagons = train.Wagons.Count, Aspects = route.Aspects, Trip = tripNo,
                Consist = string.Format(C, "2ТЭ116 + {0} ваг., {1:0} т, {2} осей, длина {3:0} м", train.Wagons.Count, train.MassT, train.WagonAxles, train.LengthM)
            };
            lock (Journal.Entries) { int n = Journal.Entries.Count, k = Math.Min(8, n); l.Journal = Journal.Entries.GetRange(n - k, k).ToArray(); l.JournalCount = n; }
            l.Points = Journal.Points;
            return l;
        }

        void UpdateChips()
        {
            var a = sim.Air; double v = sim.SpeedKmh;
            lbClock.Text = TimeSpan.FromSeconds(sim.T).ToString(@"hh\:mm\:ss");
            chips[0].Set(Th.N(Math.Abs(v), "0.0"), Math.Abs(v) > sim.Allowed + 1 ? Th.Warn : Th.Accent);
            chips[1].Set(Th.N(sim.Allowed, "0"), Th.Warn, true);
            chips[2].Set(route.KmPk(sim.Dyn.HeadX), Th.Text);
            chips[3].Set(Th.Kg(a.TmLoco.MPa) + " / " + Th.Kg(a.Ur.MPa), a.EpkTripped ? Th.Warn : Th.Text);
            chips[4].Set(Th.Kg(a.LocoTcMPa) + " / " + Th.Kg(a.TailTcMPa), a.LocoTcMPa > 0.02 || a.TailTcMPa > 0.02 ? Th.Up : Th.Text);
            chips[5].Set(Th.Kg(a.Mr.MPa), a.Mr.MPa < 0.70 ? Th.Warn : Th.Text);
            chips[6].Set(sim.C.Position + " · " + new[] { "", "I", "II", "III", "IV", "V", "VI", "Vа" }[sim.C.Crane] + " · " + sim.C.Kvt, Th.Text);
            int n; lock (Journal.Entries) n = Journal.Entries.Count;
            chips[7].Set(n + " · " + Journal.Points, n > 0 ? Th.Warn : Th.Green, n > 0);
        }

        void UpdatePages()
        {
            var a = sim.Air; int nw = a.W.Length;
            // strips
            var tc = new double[nw]; var tips = new string[nw];
            for (int i = 0; i < nw; i++) { var w = a.W[i]; tc[i] = w.VrOn ? w.Tc.MPa : double.NaN; tips[i] = string.Format(C, "Вагон {0} {1}: ТМ {2:0.00}, ТЦ {3:0.00} кгс/см²{4}", i + 1, train.Wagons[i].Number, w.Tm.MPa * Traction.KgfPerMPa, w.Tc.MPa * Traction.KgfPerMPa, w.VrOn ? "" : ", ВР выключен"); }
            if (pages[0].Visible) stripTc.Set(tc, 0.42, false, tips);
            if (pages[0].Visible || pages[5].Visible) RefreshJournal();
            if (pages[1].Visible) UpdateLoco();
            if (pages[2].Visible) UpdateTrain(tips);
            if (pages[3].Visible) scheme.Invalidate();
            if (pages[7].Visible) electricView.RefreshState();
            if (pages[4].Visible && (int)sim.T % 4 == 0) FillLights();
            if (pages[5].Visible)
                plCassette.Set(new List<Tuple<string, string, Color>>
                {
                    Tuple.Create("Запись", sim.Cassette != null ? "идёт · " + sim.Cassette.Frames + " кадров" : "закончена", sim.Cassette != null ? Th.Green : Th.Muted),
                    Tuple.Create("Событий", sim.Cassette != null ? sim.Cassette.EventsCount.ToString(C) : "—", Th.Text),
                    Tuple.Create("Файл", tripDir != null ? Path.GetFileName(tripDir) + "\\trip.kr2" : "—", Th.Muted),
                });
        }
        int journalSeen = -1, eventsSeen;
        void RefreshJournal()
        {
            List<JournalEntry> list; lock (Journal.Entries) list = Journal.Entries.ToList();
            if (list.Count != journalSeen)
            {
                journalSeen = list.Count;
                miniJournal.Items.Clear(); foreach (var e in Enumerable.Reverse(list).Take(6)) miniJournal.Items.Add(e);
                if (list.Count == 0) miniJournal.Items.Add("нарушений нет");
                journalList.Items.Clear(); foreach (var e in list) journalList.Items.Add(e);
                if (list.Count == 0) journalList.Items.Add("нарушений нет");
                lbMark.Text = list.Count + " нарушений · " + list.Sum(e => e.Points) + " баллов · оценка: " + Documents.Mark(list);
                lbMark.ForeColor = list.Any(e => e.Gross) ? Th.Warn : list.Count > 0 ? Th.Amber : Th.Green;
            }
            lock (sim.Events) { for (int i = eventsSeen; i < sim.Events.Count; i++) events.Items.Add(sim.Events[i]); eventsSeen = sim.Events.Count; }
        }
        void UpdateLoco()
        {
            var tr = sim.Tr; var a = sim.Air;
            for (int s = 0; s < 2; s++)
            {
                var pl = s == 0 ? plS1 : plS2;
                if (s >= tr.S.Length) { pl.Set(new List<Tuple<string, string, Color>>()); continue; }
                var x = tr.S[s];
                pl.Set(new List<Tuple<string, string, Color>>
                {
                    Tuple.Create("Дизель · состояние машины", x.StateName + " · " + Th.N(x.Cond * 100, "0") + " %", x.Running ? Th.Green : Th.Warn),
                    Tuple.Create("Обороты · позиция", Th.N(x.Rpm, "0") + " об/мин · " + sim.C.Position.ToString(C), Th.Text),
                    Tuple.Create("Генератор: ток · напряжение", Th.N(x.Current, "0") + " А · " + Th.N(x.VoltageKv * 1000, "0") + " В", Th.Text),
                    Tuple.Create("Сила тяги · мощность на ободе", Th.N(x.Force, "0") + " кН · " + Th.N(x.PowerKw, "0") + " кВт", Th.Accent),
                    Tuple.Create("Схема тяги", x.Assembled ? "собрана" + (x.FwStage > 0 ? ", ОП" + x.FwStage : "") : x.Running && sim.C.Position > 0 ? "не собрана (ЛН)" : "—", x.Assembled ? Th.Green : sim.C.Position > 0 && x.Running ? Th.Amber : Th.Text),
                    Tuple.Create("ИД регулятора · перегрузка дизеля", Th.N(x.IdK, "0.00") + " · " + Th.N(x.Overload * 100, "0") + " %", x.Overload > 0.02 ? Th.Amber : Th.Text),
                    Tuple.Create("Давление масла, кгс/см²", Th.N(x.OilP * Traction.KgfPerMPa, "0.0"), x.Running && x.OilP < 0.10 ? Th.Warn : Th.Text),
                    Tuple.Create("Вода · масло, °C", Th.N(x.Water, "0") + " · " + Th.N(x.Oil, "0"), x.OverheatW || x.OverheatO ? Th.Warn : x.Water > 88 ? Th.Amber : Th.Text),
                    Tuple.Create("Холодильник: жалюзи, М11 М12 М14 М13", (x.ShutW ? "В" : "-") + (x.ShutO ? "М" : "-") + "  " + string.Join(" ", x.FanOn.Select(o => o ? "●" : "○")), Th.Text),
                    Tuple.Create("Вспомогательные на валу, кВт", Th.N(x.AuxKw, "0"), Th.Text),
                    Tuple.Create("Боксование · РДМ", (x.Slipping ? "ДА" : "нет") + " · " + x.RdmUnloads + "/" + x.RdmStops, x.Slipping ? Th.Warn : Th.Text),
                    Tuple.Create("Компрессор", s < a.CompressorWorks.Length && a.CompressorWorks[s] ? (a.CompressorOn && x.Running ? "работает" : "выключен") : "неисправен", s < a.CompressorWorks.Length && a.CompressorWorks[s] ? Th.Text : Th.Warn),
                    Tuple.Create("Топливо израсходовано, кг", Th.N(x.FuelKg, "0.0"), Th.Text),
                });
            }
            int at; double mt = sim.MaxCouplerKn(out at, true), mc = sim.MaxCouplerKn(out at, false);
            plAir.Set(new List<Tuple<string, string, Color>>
            {
                Tuple.Create("ГР, кгс/см²", Th.Kg(a.Mr.MPa), Th.Accent), Tuple.Create("Питательный резервуар, кгс/см²", Th.Kg(a.Pr.MPa), Th.Text), Tuple.Create("УР, кгс/см²", Th.Kg(a.Ur.MPa), Th.Text), Tuple.Create("ТМ, кгс/см²", Th.Kg(a.TmLoco.MPa), Th.Text),
                Tuple.Create("Зарядное давление", Th.N(a.ChargeMPa * Traction.KgfPerMPa, "0.0") + " кгс/см²", Th.Text), Tuple.Create("Кран 395 / 254", new[] { "", "I", "II", "III", "IV", "V", "VI", "Vа" }[sim.C.Crane] + " / " + sim.C.Kvt, Th.Text),
                Tuple.Create("ЭПК", a.EpkTripped ? "СРЫВ" : sim.C.EpkKey ? "ключ включён" : "ключ выключен", a.EpkTripped ? Th.Warn : Th.Text),
                Tuple.Create("ТМ / ТЦ хвоста, кгс/см²", Th.Kg(a.TailTmMPa) + " / " + Th.Kg(a.TailTcMPa), Th.Text),
                Tuple.Create("Лампа ТМ (датчик 418) · РДВ", (a.Lamp418 || sim.Tr.Sot ? "горит" : "не горит") + " · " + (sim.Tr.RdvOk ? "норма" : "тяга снята"), a.Lamp418 || sim.Tr.Sot ? Th.Warn : Th.Text),
                Tuple.Create("Сцепки: растяжение / сжатие, кН", Th.N(mt, "0") + " / " + Th.N(mc, "0"), Th.Text),
                Tuple.Create("Баланс массы воздуха, кг", Th.N(a.Balance(), "0.0000"), Th.Muted),
                Tuple.Create("Пройдено", Th.N(sim.Distance / 1000, "0.00") + " км", Th.Text),
            });
            foreach (int k in new[] { 1, 2, 3, 4, 5, 6, 7 }) if (craneBtn[k] != null) { var lk = k == sim.C.Crane ? Look.Selected : Look.Secondary; if (craneBtn[k].Look != lk) { craneBtn[k].Look = lk; craneBtn[k].Invalidate(); } }
            for (int k = 1; k <= 6; k++) { var lk = k == sim.C.Kvt ? Look.Selected : Look.Secondary; if (kvtBtn[k].Look != lk) { kvtBtn[k].Look = lk; kvtBtn[k].Invalidate(); } }
        }
        void UpdateTrain(string[] tips)
        {
            var a = sim.Air; int nw = a.W.Length; var vals = new double[nw]; double max; bool signed = false;
            for (int i = 0; i < nw; i++)
            {
                int b = train.Sections + i;
                switch (segMetric.Selected)
                {
                    case 1: vals[i] = a.W[i].Tm.MPa * Traction.KgfPerMPa; break;
                    case 2: vals[i] = b < sim.Dyn.Cp.Length ? sim.Dyn.Cp[b].Force / 1000 : 0; break;
                    case 3: vals[i] = sim.Dyn.B[b].Brake / 1000; break;
                    default: vals[i] = a.W[i].VrOn ? a.W[i].Tc.MPa * Traction.KgfPerMPa : double.NaN; break;
                }
            }
            max = segMetric.Selected == 1 ? 6.0 : segMetric.Selected == 2 ? Math.Max(200, vals.Select(Math.Abs).DefaultIfEmpty(0).Max()) : segMetric.Selected == 3 ? Math.Max(50, vals.DefaultIfEmpty(0).Max()) : 4.2;
            signed = segMetric.Selected == 2;
            stripBig.Set(vals, max, signed, tips);
            for (int i = 0; i < nw && i < gridTrain.Rows.Count; i++)
            {
                var w = a.W[i]; var cells = gridTrain.Rows[i].Cells; int b = train.Sections + i;
                cells[4].Value = Th.Kg(w.Tm.MPa); cells[5].Value = Th.Kg(w.Zr.MPa); cells[6].Value = Th.Kg(w.Wk.MPa); cells[7].Value = Th.Kg(w.Tc.MPa);
                cells[8].Value = Th.N(sim.Dyn.B[b].Brake / 1000, "0.0");
                cells[9].Value = b < sim.Dyn.Cp.Length ? (sim.Dyn.Cp[b].Broken ? "ОБРЫВ" : Th.N(sim.Dyn.Cp[b].Force / 1000, "0")) : "—";
                string st = !w.VrOn ? "ВР выключен" : w.Emergency ? "экстренное" : w.Braking ? "торможение" : w.Tc.MPa > 0.02 ? "отпуск" : "отпущен";
                cells[10].Value = st; cells[10].Style.ForeColor = !w.VrOn ? Th.Warn : w.Braking ? Th.Up : Th.Green;
                cells[7].Style.ForeColor = w.Tc.MPa > 0.02 ? Th.Up : Th.Text;
            }
        }
    }

    // list of cards (routes, trains)
    public sealed class CardItem { public string Title = "", Sub = ""; public object Tag; public override string ToString() { return Title; } }
    public sealed class CardList : ListBox
    {
        public CardList() { DrawMode = DrawMode.OwnerDrawFixed; ItemHeight = 70; BorderStyle = BorderStyle.None; BackColor = Th.Card; IntegralHeight = false; }
        protected override void OnDrawItem(DrawItemEventArgs e)
        {
            if (e.Index < 0) return;
            var g = e.Graphics; Th.Smooth(g); var it = Items[e.Index] as CardItem; bool sel = (e.State & DrawItemState.Selected) != 0;
            using (var b = new SolidBrush(Th.Card)) g.FillRectangle(b, e.Bounds);
            var r = new RectangleF(e.Bounds.X + 2, e.Bounds.Y + 4, e.Bounds.Width - 6, e.Bounds.Height - 8);
            Th.Fill(g, r, 10, sel ? Color.FromArgb(0x14, 0x2A, 0x2C) : Th.Inset, sel ? Th.Accent : Th.Line2, sel ? 2 : 1);
            Th.Txt(g, it != null ? it.Title : Items[e.Index].ToString(), Th.F(10.5f, true), Th.Text, new RectangleF(r.X + 12, r.Y + 6, r.Width - 20, 22));
            if (it != null) Th.Txt(g, it.Sub, Th.F(8.8f), Th.Muted, new RectangleF(r.X + 12, r.Y + 30, r.Width - 20, 22));
        }
    }

    public sealed class DarkRenderer : ToolStripProfessionalRenderer
    {
        public DarkRenderer() : base(new DarkColors()) { }
        protected override void OnRenderItemText(ToolStripItemTextRenderEventArgs e) { e.TextColor = e.Item is ToolStripLabel ? Th.Muted : Th.Text; base.OnRenderItemText(e); }
        sealed class DarkColors : ProfessionalColorTable
        {
            public override Color ToolStripDropDownBackground { get { return Th.Raised; } }
            public override Color MenuItemSelected { get { return Th.Line2; } }
            public override Color MenuItemBorder { get { return Th.Accent; } }
            public override Color MenuBorder { get { return Th.Line2; } }
            public override Color ImageMarginGradientBegin { get { return Th.Raised; } }
            public override Color ImageMarginGradientMiddle { get { return Th.Raised; } }
            public override Color ImageMarginGradientEnd { get { return Th.Raised; } }
            public override Color MenuItemSelectedGradientBegin { get { return Th.Line2; } }
            public override Color MenuItemSelectedGradientEnd { get { return Th.Line2; } }
        }
    }

    // editor of a train: groups of wagons of one type, load, pads, mode
    public sealed class TrainEditor : Form
    {
        public Train Result;
        readonly DataGridView g; readonly TextBox name;
        public TrainEditor(Train from)
        {
            Text = "Поезд: создать или изменить"; ClientSize = new Size(980, 520); Font = Th.F(10f); StartPosition = FormStartPosition.CenterParent; BackColor = Th.Back; ForeColor = Th.Text; Padding = new Padding(16);
            var top = new Panel { Dock = DockStyle.Top, Height = 64, BackColor = Th.Back };
            top.Controls.Add(new Label { Text = "Название поезда", AutoSize = true, ForeColor = Th.Muted, Location = new Point(0, 4) });
            name = new TextBox { Text = from != null ? from.Name + " (изм.)" : "Мой поезд", Location = new Point(0, 26), Width = 600 }; Th.Style(name); top.Controls.Add(name);
            g = new DataGridView { Dock = DockStyle.Fill, AllowUserToAddRows = true, AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill, EditMode = DataGridViewEditMode.EditOnEnter };
            Th.Style(g);
            var type = new DataGridViewComboBoxColumn { HeaderText = "Тип вагона", Name = "type", FlatStyle = FlatStyle.Flat }; foreach (var t in Catalog.Types) type.Items.Add(t.Name);
            g.Columns.Add(type); g.Columns.Add("count", "Количество"); g.Columns.Add("cargo", "Груз, т на вагон");
            var pads = new DataGridViewComboBoxColumn { HeaderText = "Колодки", Name = "pads", FlatStyle = FlatStyle.Flat }; pads.Items.AddRange("чугун", "композит"); g.Columns.Add(pads);
            var mode = new DataGridViewComboBoxColumn { HeaderText = "Режим ВР", Name = "mode", FlatStyle = FlatStyle.Flat }; mode.Items.AddRange("по загрузке", "порожний", "средний", "гружёный"); g.Columns.Add(mode);
            var mt = new DataGridViewComboBoxColumn { HeaderText = "Отпуск", Name = "mt", FlatStyle = FlatStyle.Flat }; mt.Items.AddRange("равнинный", "горный"); g.Columns.Add(mt);
            var vr = new DataGridViewComboBoxColumn { HeaderText = "ВР", Name = "vr", FlatStyle = FlatStyle.Flat }; vr.Items.AddRange("включён", "выключен"); g.Columns.Add(vr);
            if (from != null)
                for (int i = 0; i < from.Wagons.Count; )
                {
                    var w = from.Wagons[i]; int j = i;
                    while (j < from.Wagons.Count && from.Wagons[j].TypeId == w.TypeId && from.Wagons[j].Cargo == w.Cargo && from.Wagons[j].Composite == w.Composite && from.Wagons[j].Mode == w.Mode && from.Wagons[j].Mountain == w.Mountain && from.Wagons[j].VrOn == w.VrOn) j++;
                    var tp = Catalog.Types.FirstOrDefault(t => t.Id == w.TypeId);
                    g.Rows.Add(tp != null ? tp.Name : Catalog.Types[0].Name, j - i, w.Cargo.ToString(CultureInfo.InvariantCulture), w.Composite ? "композит" : "чугун", new[] { "", "порожний", "средний", "гружёный" }[w.Mode], w.Mountain ? "горный" : "равнинный", w.VrOn ? "включён" : "выключен");
                    i = j;
                }
            else g.Rows.Add(Catalog.Types[0].Name, 50, "69", "чугун", "по загрузке", "равнинный", "включён");
            var bottom = new FlowLayoutPanel { Dock = DockStyle.Bottom, Height = 56, FlowDirection = FlowDirection.RightToLeft, BackColor = Th.Back, Padding = new Padding(0, 10, 0, 0) };
            var ok = new Btn("Сохранить поезд", Look.Primary, 200, 40);
            ok.Click += (s, e) => { try { Result = Build(); DialogResult = DialogResult.OK; Close(); } catch (Exception ex) { MessageBox.Show(this, ex.Message, "Поезд не собран"); } };
            var cancel = new Btn("Отмена", Look.Secondary, 120, 40); cancel.Click += (s, e) => Close();
            bottom.Controls.Add(ok); bottom.Controls.Add(cancel);
            Controls.Add(g); Controls.Add(top); Controls.Add(bottom);
        }
        Train Build()
        {
            var t = new Train { Name = name.Text.Trim().Length > 0 ? name.Text.Trim() : "Мой поезд" };
            foreach (DataGridViewRow r in g.Rows)
            {
                if (r.IsNewRow) continue;
                var tp = Catalog.Types.First(x => x.Name == (string)r.Cells[0].Value);
                int n = int.Parse(Convert.ToString(r.Cells[1].Value), CultureInfo.InvariantCulture);
                double cargo = double.Parse(Convert.ToString(r.Cells[2].Value).Replace(',', '.'), CultureInfo.InvariantCulture);
                if (n < 1 || n > 200) throw new Exception("Количество вагонов в строке — от 1 до 200");
                if (cargo < 0 || cargo > tp.MaxCargo + 5) throw new Exception("Груз вагона «" + tp.Name + "» — от 0 до " + tp.MaxCargo + " т");
                string mode = (string)r.Cells[4].Value ?? "по загрузке";
                for (int k = 0; k < n; k++)
                {
                    var w = new Wagon { TypeId = tp.Id, TypeName = tp.Name, Tare = tp.Tare, Length = tp.Length, Axles = tp.Axles, Cargo = cargo, Composite = (string)r.Cells[3].Value == "композит", Mountain = (string)r.Cells[5].Value == "горный", VrOn = (string)r.Cells[6].Value != "выключен" };
                    w.Mode = mode == "порожний" ? 1 : mode == "средний" ? 2 : mode == "гружёный" ? 3 : w.ProperMode();
                    t.Wagons.Add(w);
                }
            }
            if (t.Wagons.Count == 0) throw new Exception("В поезде нет вагонов");
            if (t.Wagons.Count > 150) throw new Exception("Больше 150 вагонов модель не рассчитывает");
            t.NumberWagons();
            return t;
        }
    }
}
