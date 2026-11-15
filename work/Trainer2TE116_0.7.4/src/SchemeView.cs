using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Linq;
using System.Windows.Forms;

namespace Trainer2TE116
{
    // View and hit testing use the coordinates produced by the netlist parser.
    public sealed class SchemeView : UserControl
    {
        public Scheme Model { get; private set; }
        public readonly SchemeCanvas Canvas = new SchemeCanvas();
        public readonly OriginalSchemeCanvas OriginalCanvas = new OriginalSchemeCanvas();
        readonly ComboBox mode = new ComboBox { Width = 150, DropDownStyle = ComboBoxStyle.DropDownList };
        readonly ComboBox sheets = new ComboBox { Width = 340, DropDownStyle = ComboBoxStyle.DropDownList };
        readonly ComboBox sections = new ComboBox { Width = 115, DropDownStyle = ComboBoxStyle.DropDownList };
        readonly TextBox search = new TextBox { Width = 100 };
        readonly Label detail = new Label { AutoSize = true, MaximumSize = new Size(278, 0), Padding = new Padding(8), ForeColor = Th.Text };
        readonly Card detailCard = new Card("Выбрано"), faultCard = new Card("Дефекты схемы"), contactCard = new Card("Катушка и контакты");
        WirePiece selectedWire;
        readonly ListBox faults = new ListBox { Dock = DockStyle.Fill, BackColor = Th.Inset, ForeColor = Th.Warn, BorderStyle = BorderStyle.None, HorizontalScrollbar = true };
        readonly ListBox contacts = new ListBox { Dock = DockStyle.Bottom, Height = 180, BackColor = Th.Inset, ForeColor = Th.Muted, BorderStyle = BorderStyle.None, HorizontalScrollbar = true };
        List<El> defectItems = new List<El>(), contactItems = new List<El>();
        public readonly SchemeStatus Status = new SchemeStatus { Dock = DockStyle.Top, Height = 40 };
        El selected; string defectStamp; AtlasHit selectedPdf; bool fillingSheets;
        public bool OriginalMode { get { return mode.SelectedIndex == 0; } }
        public SchemeView()
        {
            BackColor = Th.Back; Font = Th.F(9); ForeColor = Th.Text;
            var top = new FlowLayoutPanel { Dock = DockStyle.Top, Height = 44, Padding = new Padding(8, 7, 6, 5), WrapContents = false, BackColor = Th.Rail };
            sections.Items.AddRange(new object[] { "Секция 1", "Секция 2" }); sections.SelectedIndex = 0;
            mode.Items.AddRange(new object[] { "Оригинал PDF", "Цепи модели" }); mode.SelectedIndex = 0;
            foreach (var box in new[] { mode, sheets, sections }) { box.FlatStyle = FlatStyle.Flat; box.BackColor = Th.Raised; box.ForeColor = Th.Text; box.Font = Th.F(9.5f); box.Margin = new Padding(3, 3, 5, 0); }
            search.BackColor = Th.Inset; search.ForeColor = Th.Text; search.BorderStyle = BorderStyle.FixedSingle; search.Font = Th.F(10); search.Margin = new Padding(8, 3, 3, 0);
            var find = new Btn("Найти", Look.Primary, 74) { Height = 28, Margin = new Padding(2, 1, 12, 0) };
            var minus = new Btn("−", Look.Secondary, 32) { Height = 28, Margin = new Padding(2, 1, 2, 0) };
            var plus = new Btn("+", Look.Secondary, 32) { Height = 28, Margin = new Padding(2, 1, 2, 0) };
            var fit = new Btn("Вписать", Look.Secondary, 78) { Height = 28, Margin = new Padding(2, 1, 8, 0) };
            var overlay = new CheckBox { Text = "Состояние цепей", Checked = true, AutoSize = true, ForeColor = Th.Text, Margin = new Padding(6, 6, 0, 0) };
            overlay.CheckedChanged += (s, e) => { OriginalCanvas.ShowOverlay = overlay.Checked; OriginalCanvas.Invalidate(); };
            top.Controls.AddRange(new Control[] { mode, sheets, sections, search, find, minus, plus, fit, overlay });
            var right = new TableLayoutPanel { Dock = DockStyle.Right, Width = 310, Padding = new Padding(6, 8, 8, 4), ColumnCount = 1, RowCount = 4, BackColor = Th.Back };
            right.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
            right.RowStyles.Add(new RowStyle(SizeType.Absolute, 236)); right.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
            right.RowStyles.Add(new RowStyle(SizeType.Absolute, 176)); right.RowStyles.Add(new RowStyle(SizeType.Absolute, 42));
            var clear = new Btn("Снять все дефекты схемы", Look.Secondary, 270) { Dock = DockStyle.Fill, Height = 34 };
            contacts.Dock = DockStyle.Fill;
            foreach (var list in new[] { faults, contacts })
            {
                list.BackColor = Th.Card; list.DrawMode = DrawMode.OwnerDrawFixed; list.ItemHeight = 22; list.IntegralHeight = false; list.HorizontalScrollbar = false;
                var box = list; bool red = list == faults;
                list.DrawItem += (s, e) =>
                {
                    if (e.Index < 0) return;
                    bool on = (e.State & DrawItemState.Selected) != 0; var r = e.Bounds;
                    using (var b = new SolidBrush(on ? Th.Raised : Th.Card)) e.Graphics.FillRectangle(b, r);
                    e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
                    using (var b = new SolidBrush(red ? Th.Red : Th.Down)) e.Graphics.FillEllipse(b, r.X + 3, r.Y + 8, 6, 6);
                    using (var f = new StringFormat { Trimming = StringTrimming.EllipsisCharacter, FormatFlags = StringFormatFlags.NoWrap, LineAlignment = StringAlignment.Center })
                    using (var b = new SolidBrush(red ? Th.Warn : on ? Th.Text : Th.Muted)) e.Graphics.DrawString(box.Items[e.Index].ToString(), Th.F(8.8f), b, new RectangleF(r.X + 14, r.Y, r.Width - 16, r.Height), f);
                };
            }
            detail.BackColor = Th.Card; detail.MaximumSize = new Size(250, 0); detail.Padding = new Padding(0);
            var detailHost = new Panel { Dock = DockStyle.Fill, AutoScroll = true, BackColor = Th.Card }; detailHost.Controls.Add(detail);
            detailCard.Controls.Add(detailHost); faultCard.Controls.Add(faults); contactCard.Controls.Add(contacts);
            foreach (var card in new[] { detailCard, faultCard, contactCard }) { card.Dock = DockStyle.Fill; card.Margin = new Padding(0, 0, 0, 8); card.Padding = new Padding(12, 38, 10, 10); }
            contactCard.Note = "двойной щелчок";
            right.Controls.Add(detailCard, 0, 0); right.Controls.Add(faultCard, 0, 1); right.Controls.Add(contactCard, 0, 2); right.Controls.Add(clear, 0, 3);
            var hint = new SchemeLegend { Dock = DockStyle.Bottom, Height = 30 };
            Canvas.Dock = OriginalCanvas.Dock = DockStyle.Fill;
            Canvas.Visible = false;
            Controls.Add(Canvas); Controls.Add(OriginalCanvas); Controls.Add(right); Controls.Add(Status); Controls.Add(top); Controls.Add(hint);
            Status.Go = el => Navigate(el);
            mode.SelectedIndexChanged += (s, e) => { Canvas.Visible = !OriginalMode; OriginalCanvas.Visible = OriginalMode; overlay.Visible = OriginalMode; hint.Original = OriginalMode; hint.Invalidate(); if (OriginalMode) selected = selectedPdf == null ? null : OriginalCanvas.Assigned(selectedPdf); FillSheets(); RefreshState(); };
            sheets.SelectedIndexChanged += (s, e) => { if (fillingSheets || sheets.SelectedIndex < 0) return; if (OriginalMode) OriginalCanvas.SetPage(sheets.SelectedIndex); else { Canvas.Sheet = sheets.SelectedIndex; Canvas.AutoScrollPosition = Point.Empty; Canvas.ResetExtent(); } };
            sections.SelectedIndexChanged += (s, e) => { Canvas.Section = OriginalCanvas.Section = Status.Section = sections.SelectedIndex + 1; if (OriginalMode && selectedPdf != null) selected = OriginalCanvas.Assigned(selectedPdf); Canvas.ResetExtent(); RefreshState(); };
            find.Click += (s, e) => FindNext(search.Text);
            search.KeyDown += (s, e) => { if (e.KeyCode == Keys.Enter) { FindNext(search.Text); e.SuppressKeyPress = true; } };
            minus.Click += (s, e) => { if (OriginalMode) OriginalCanvas.Zoom(0.8f, Point.Empty); else Canvas.Zoom(0.8f, Point.Empty); };
            plus.Click += (s, e) => { if (OriginalMode) OriginalCanvas.Zoom(1.25f, Point.Empty); else Canvas.Zoom(1.25f, Point.Empty); };
            fit.Click += (s, e) => { if (OriginalMode) OriginalCanvas.Fit(); else Canvas.Fit(); };
            Canvas.Selected = el => { selected = el; RefreshState(); };
            Canvas.Changed = () => RefreshState();
            OriginalCanvas.Selected = el => { selected = el; RefreshState(); };
            OriginalCanvas.WireSelected = wire => { selectedWire = wire; selectedPdf = null; selected = null; RefreshState(); };
            OriginalCanvas.HitSelected = (hit, text) => { selectedWire = null; selectedPdf = hit; selected = OriginalCanvas.Assigned(hit); RefreshState(); };
            OriginalCanvas.Changed = () => RefreshState();
            OriginalCanvas.PageChanged = index => { selectedWire = null; selectedPdf = null; selected = null; if (OriginalMode && index >= 0 && index < sheets.Items.Count) { fillingSheets = true; sheets.SelectedIndex = index; fillingSheets = false; } RefreshState(); };
            faults.DoubleClick += (s, e) => { if (faults.SelectedIndex >= 0) Navigate(defectItems[faults.SelectedIndex]); };
            contacts.DoubleClick += (s, e) => { if (contacts.SelectedIndex >= 0) Navigate(contactItems[contacts.SelectedIndex]); };
            clear.Click += (s, e) => { if (Model != null) { Model.ClearOverrides(); RefreshState(); } };
            OriginalCanvas.LoadAtlas(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "assets", "scheme008"));
            if (OriginalCanvas.Available && OriginalCanvas.Atlas.pages.Length > 2) OriginalCanvas.SetPage(2);
        }
        public void SetOriginalMode(bool original) { mode.SelectedIndex = original ? 0 : 1; }
        public void LoadOriginalAtlas(string directory) { selectedPdf = null; selected = null; contactItems.Clear(); contacts.Items.Clear(); OriginalCanvas.LoadAtlas(directory); FillSheets(); RefreshState(); }
        void FillSheets()
        {
            fillingSheets = true;
            sheets.Items.Clear();
            if (OriginalMode && OriginalCanvas.Available) foreach (var page in OriginalCanvas.Atlas.pages) sheets.Items.Add(page.title ?? "Оригинал PDF · лист " + page.page);
            else if (!OriginalMode && Model != null) foreach (var name in Model.Sheets) sheets.Items.Add(name);
            int index = OriginalMode ? OriginalCanvas.PageIndex : Canvas.Sheet;
            if (sheets.Items.Count > 0) sheets.SelectedIndex = Math.Max(0, Math.Min(sheets.Items.Count - 1, index));
            fillingSheets = false;
        }
        public void Bind(Scheme model, bool retainDefects)
        {
            if (retainDefects && Model != null)
                foreach (var e in Model.Els.Where(e => e.Ovr != 0))
                    if (e.Id < model.Els.Count) model.SetOverride(model.Els[e.Id], e.Ovr);
            Model = Canvas.Model = Status.Model = model; selected = null; defectStamp = null;
            OriginalCanvas.ResetModel(model); selectedPdf = null; selectedWire = null;
            Canvas.FocusedElement = null; Canvas.Query = ""; Canvas.ResetSelection();
            contactItems.Clear(); contacts.Items.Clear();
            FillSheets();
            Canvas.ResetExtent(); RefreshState();
        }
        public void RefreshState()
        {
            if (Model == null) return;
            string stamp = string.Join(",", Model.Els.Where(e => e.Ovr != 0 || e.ExternalOpen).Select(e => e.Id + ":" + e.Ovr + ":" + e.ExternalOpen));
            if (stamp != defectStamp)
            {
                defectStamp = stamp; defectItems = Model.Els.Where(e => e.Ovr != 0 || e.ExternalOpen).ToList();
                faults.Items.Clear();
                foreach (var e in defectItems) faults.Items.Add((e.Rung.Cab ? "Кабина" : "С" + e.Sec) + ": " + e.Title + " — " + (e.Ovr != 0 ? Scheme.OvrName(e) : "неисправность из списка / лампа"));
            }
            string note = defectItems.Count == 0 ? "нет" : "активных: " + defectItems.Count;
            if (faultCard.Note != note) { faultCard.Note = note; faultCard.Invalidate(); }
            if (OriginalMode && selectedWire != null) detail.Text = OriginalCanvas.WireDescription(selectedWire);
            else if (OriginalMode && selectedPdf != null) detail.Text = OriginalCanvas.Description(selectedPdf);
            else if (selected == null) detail.Text = OriginalMode ? "Оригинальная схема 2ТЭ116\n\nКарта цепей: щёлкните по любому обозначению или проводу.\n\nОранжевым светятся провода под напряжением, зелёным — включённые аппараты и замкнутые контакты.\n\n" + (OriginalCanvas.LoadError ?? OriginalCanvas.BindingError ?? "Поиск — по всем 7 страницам PDF.") : "Электросхема 2ТЭ116 · 008\n\nВыберите элемент для сведений. Поиск показывает катушку и контакты на всех листах. Повторное «Найти» — следующее совпадение.\n\nДефекты до поездки переносятся в новую поездку. На паузе они применяются при продолжении.";
            else detail.Text = Canvas.Description(selected);
            if (selected != null || (OriginalMode && (selectedPdf != null || selectedWire != null)))
            {
                var list = OriginalMode && selectedWire != null ? OriginalCanvas.WireElements(selectedWire) : OriginalMode && selectedPdf != null && selected == null ? OriginalCanvas.Resolve(selectedPdf) : Model.Els.Where(e => e.Key != null && e.Key == selected.Key).ToList();
                if (!list.SequenceEqual(contactItems))
                {
                    contactItems = list; contacts.Items.Clear();
                    foreach (var e in list) contacts.Items.Add("Л" + (e.Rung.Sheet + 1) + ": " + e.Title + " · " + e.Rung.Name);
                }
            }
            Canvas.Invalidate();
            OriginalCanvas.Invalidate(); Status.Invalidate();
        }
        public void FindNext(string text)
        {
            if (Model == null || string.IsNullOrWhiteSpace(text)) return;
            if (OriginalMode) { if (!OriginalCanvas.FindNext(text)) detail.Text = "Не найдено в PDF: " + text; return; }
            Canvas.Query = text.Trim();
            var found = Model.Els.Where(e => (e.Name ?? "").IndexOf(Canvas.Query, StringComparison.OrdinalIgnoreCase) >= 0 && (e.Rung.Cab || e.Sec == Canvas.Section)).ToList();
            if (found.Count == 0) { detail.Text = "Не найдено: " + text; return; }
            int at = selected == null ? -1 : found.IndexOf(selected);
            Navigate(found[(at + 1) % found.Count]);
        }
        public void Navigate(El el)
        {
            if (OriginalMode)
            {
                if (!el.Rung.Cab) sections.SelectedIndex = el.Sec - 1;
                if (OriginalCanvas.Navigate(el)) { selected = el; RefreshState(); return; }
                SetOriginalMode(false);
            }
            selected = el; sheets.SelectedIndex = el.Rung.Sheet;
            if (!el.Rung.Cab) sections.SelectedIndex = el.Sec - 1;
            Canvas.FocusElement(el); RefreshState();
        }
    }

    // Colours of the closed circuits: one for each circuit, two for the parts common to several of them.
    public static class CircuitColors
    {
        static readonly Color[] own = { Color.FromArgb(214, 51, 132), Color.FromArgb(124, 77, 255), Color.FromArgb(30, 136, 229), Color.FromArgb(0, 172, 193), Color.FromArgb(0, 137, 123), Color.FromArgb(175, 180, 43), Color.FromArgb(161, 106, 76), Color.FromArgb(240, 98, 146) };
        public static readonly Color Plus = Color.FromArgb(255, 98, 0), Minus = Color.FromArgb(84, 110, 160), Idle = Color.FromArgb(255, 170, 110);
        public static Color Of(int index) { return index == CircuitMap.SharedPlus ? Plus : index == CircuitMap.SharedMinus ? Minus : own[index % own.Length]; }
    }

    // The key of the map: what each colour on the sheet means.
    public sealed class SchemeLegend : Control
    {
        public bool Original = true;
        public SchemeLegend() { DoubleBuffered = true; BackColor = Th.Rail; }
        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e); var g = e.Graphics; g.SmoothingMode = SmoothingMode.AntiAlias;
            var items = Original
                ? new[] { new { C = CircuitColors.Of(0), T = "цепь от + до −, у каждой свой цвет", L = true }, new { C = CircuitColors.Plus, T = "общий плюс", L = true }, new { C = CircuitColors.Minus, T = "общий минус", L = true }, new { C = Color.FromArgb(0, 158, 96), T = "замкнут, тока нет", L = false }, new { C = Color.FromArgb(222, 38, 60), T = "обрыв", L = false }, new { C = Color.FromArgb(235, 150, 0), T = "перемычка", L = false }, new { C = Color.FromArgb(36, 112, 255), T = "выбрано", L = false }, new { C = Color.Empty, T = "щелчок — действие · правая кнопка — меню", L = false } }
                : new[] { new { C = CircuitColors.Of(0), T = "цепь от + до −, у каждой свой цвет", L = true }, new { C = CircuitColors.Plus, T = "общий плюс", L = true }, new { C = CircuitColors.Minus, T = "общий минус", L = true }, new { C = Th.Red, T = "обрыв", L = false }, new { C = Th.Amber, T = "перемычка", L = false }, new { C = Th.Down, T = "выбрано", L = false }, new { C = Color.Empty, T = "щелчок: норма → обрыв → перемычка · правая кнопка — меню · номер провода — весь узел", L = false } };
            float x = 12; var font = Th.F(8.5f);
            foreach (var it in items)
            {
                if (it.C != Color.Empty)
                {
                    if (it.L) using (var pen = new Pen(it.C, 3) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawLine(pen, x, Height / 2f, x + 16, Height / 2f);
                    else Th.Fill(g, new RectangleF(x + 2, Height / 2f - 6, 12, 12), 3, Color.FromArgb(90, it.C), it.C, 1.4f);
                    x += 22;
                }
                float w = g.MeasureString(it.T, font).Width + 4;
                Th.Txt(g, it.T, font, it.C == Color.Empty ? Th.Dim : Th.Muted, new RectangleF(x, 0, w + 300, Height));
                x += w + 14;
            }
        }
    }

    // What is assembled right now in the chosen section: the instructor sees the result of the driver's actions on the
    // pult without tracing the circuits. A click on an indicator goes to its apparatus.
    public sealed class SchemeStatus : Control
    {
        public Scheme Model; public int Section = 1; public Action<El> Go;
        sealed class Chip { public string Text; public int State; public string App; public RectangleF R; }
        readonly List<Chip> chips = new List<Chip>();
        public SchemeStatus() { DoubleBuffered = true; BackColor = Th.Back; Cursor = Cursors.Hand; }
        bool On(string name) { return Model.On(name, Section); }
        void Add(string text, int state, string app) { chips.Add(new Chip { Text = text, State = state, App = app }); }
        // state: 0 off, 1 on, 2 in progress, 3 protection tripped
        public List<string> Build()
        {
            chips.Clear(); if (Model == null) return new List<string>();
            Add("Питание управления", Model.Live("1686@1") ? 1 : 0, "КТН");
            if (!On("РУ10")) Add("Топливный насос", On("ТН") || On("КТН") ? 1 : 0, On("ТН") ? "ТН" : "КТН");
            if (On("РУ10")) Add("Дизель работает", 1, "РУ10");
            else if (On("Д2")) Add("Пуск: стартёр", 2, "Д2");
            else if (On("КМН")) Add("Пуск: прокачка масла", 2, "КМН");
            else Add("Дизель остановлен", 0, "РУ10");
            bool fwd = Model.Get("ПР.В@" + Section), back = Model.Get("ПР.Н@" + Section);
            Add(fwd ? "Реверсор: вперёд" : back ? "Реверсор: назад" : "Реверсор не установлен", fwd || back ? 1 : 0, fwd || !back ? "ПР_В" : "ПР_Н");
            bool kv = On("КВ"), vv = On("ВВ");
            Add(kv && vv ? "Возбуждение включено" : kv || vv ? "Возбуждение: " + (kv ? "нет ВВ" : "нет КВ") : "Возбуждение снято", kv && vv ? 1 : kv || vv ? 2 : 0, kv ? "ВВ" : "КВ");
            int n = 0; for (int k = 1; k <= 6; k++) if (On("П" + k)) n++;
            bool ru5 = On("РУ5");
            Add(n > 0 && ru5 ? "Тяга собрана · П " + n + "/6" : n > 0 ? "Сборка тяги · П " + n + "/6" : "Тяга разобрана", n > 0 && ru5 ? 1 : n > 0 ? 2 : 0, "РУ5");
            if (On("ВШ2")) Add("Ослабление поля 2", 1, "ВШ2"); else if (On("ВШ1")) Add("Ослабление поля 1", 1, "ВШ1");
            if (On("КДК")) Add("Компрессор", 1, "КДК");
            var trip = new List<string>(); string first = null;
            foreach (var p in new[] { new[] { "РУ3", "аварийная остановка" }, new[] { "РЗ", "РЗ" }, new[] { "РОП", "РОП" }, new[] { "Р4", "РМ2" }, new[] { "РУ2", "РУ2" }, new[] { "РУ1", "обрыв ТМ" } })
                if (On(p[0])) { trip.Add(p[1]); first = first ?? p[0]; }
            Add(trip.Count > 0 ? "Защита: " + string.Join(", ", trip) : "Защиты в норме", trip.Count > 0 ? 3 : 1, first ?? "РЗ");
            int faults = Model.Els.Count(e => e.Ovr != 0 || e.ExternalOpen);
            if (faults > 0) Add("Дефектов: " + faults, 3, null);
            return chips.Select(c => c.Text).ToList();
        }
        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e); var g = e.Graphics; g.SmoothingMode = SmoothingMode.AntiAlias; Build();
            float x = 8; var font = Th.F(8.5f, true);
            foreach (var c in chips)
            {
                Color col = c.State == 1 ? Th.Green : c.State == 2 ? Th.Amber : c.State == 3 ? Th.Red : Th.Dim;
                float w = g.MeasureString(c.Text, font).Width + 30;
                c.R = new RectangleF(x, 7, w, 26);
                Th.Fill(g, c.R, 13, c.State == 0 ? Th.Inset : Color.FromArgb(34, col), c.State == 0 ? Th.Line2 : Color.FromArgb(170, col));
                if (c.State != 0) using (var glow = new SolidBrush(Color.FromArgb(60, col))) g.FillEllipse(glow, x + 6, 13, 14, 14);
                using (var dot = new SolidBrush(col)) g.FillEllipse(dot, x + 9, 16, 8, 8);
                Th.Txt(g, c.Text, font, c.State == 0 ? Th.Muted : Th.Text, new RectangleF(x + 22, 7, w - 24, 26));
                x += w + 6;
            }
        }
        protected override void OnMouseDown(MouseEventArgs e)
        {
            base.OnMouseDown(e); if (Model == null || Go == null) return;
            foreach (var c in chips)
                if (c.R.Contains(e.Location) && c.App != null) { var a = Model.Get(c.App, Section); if (a != null && a.Coils.Count > 0) Go(a.Coils[0]); return; }
        }
    }

    public sealed class SchemeCanvas : ScrollableControl
    {
        public Scheme Model; public int Sheet = 1, Section = 1; public string Query = "";
        public Action<El> Selected; public Action Changed;
        public float ScaleUnit = 38;
        public El FocusedElement;
        readonly ToolTip tip = new ToolTip { AutoPopDelay = 20000 };
        El hovered; Point drag, scroll; bool dragging;
        ContextMenuStrip menu;
        IEnumerable<Rung> VisibleRungs { get { return Model == null ? Enumerable.Empty<Rung>() : Model.Rungs.Where(r => r.Sheet == Sheet && (r.Cab || r.Sec == Section)); } }
        public SchemeCanvas()
        {
            DoubleBuffered = true; AutoScroll = true; BackColor = Th.Inset;
            SetStyle(ControlStyles.Selectable, true); TabStop = true;
        }
        protected override void Dispose(bool disposing) { if (disposing) { tip.Dispose(); if (menu != null) menu.Dispose(); } base.Dispose(disposing); }
        public void ResetSelection() { hovered = null; highlightedNode = -1; tip.SetToolTip(this, ""); }
        public void ResetExtent()
        {
            float w = 1, h = 1;
            foreach (var r in VisibleRungs) { w = Math.Max(w, r.W); h = Math.Max(h, r.Y + r.H + 1); }
            AutoScrollMinSize = new Size((int)(w * ScaleUnit + 32), (int)(h * ScaleUnit + 32)); Invalidate();
        }
        public void Fit()
        {
            float w = VisibleRungs.Select(r => r.W).DefaultIfEmpty(1).Max();
            ScaleUnit = Math.Max(15, Math.Min(60, (ClientSize.Width - 40) / w)); AutoScrollPosition = Point.Empty; ResetExtent();
        }
        public void Zoom(float factor, Point anchor)
        {
            float next = Math.Max(15, Math.Min(80, ScaleUnit * factor)), k = next / ScaleUnit;
            Point p = AutoScrollPosition; ScaleUnit = next; ResetExtent();
            AutoScrollPosition = new Point((int)((anchor.X - p.X) * k - anchor.X), (int)((anchor.Y - p.Y) * k - anchor.Y));
        }
        public Point ElementPoint(El e) { return Point.Round(new PointF(e.X * ScaleUnit + 16 + AutoScrollPosition.X, e.Y * ScaleUnit + 28 + AutoScrollPosition.Y)); }
        public void FocusElement(El e)
        {
            FocusedElement = e;
            AutoScrollPosition = new Point(Math.Max(0, (int)(e.X * ScaleUnit) - ClientSize.Width / 2), Math.Max(0, (int)(e.Y * ScaleUnit) - ClientSize.Height / 2)); Invalidate();
        }
        public El Hit(Point p)
        {
            float x = (p.X - 16 - AutoScrollPosition.X) / ScaleUnit, y = (p.Y - 28 - AutoScrollPosition.Y) / ScaleUnit;
            return VisibleRungs.SelectMany(r => r.Els).FirstOrDefault(e => Math.Abs(e.Y - y) < 0.24 && Math.Abs(e.X - x) < (e.Kind == ElKind.Wire ? e.W / 2 : 0.48));
        }
        public string Description(El e)
        {
            return (e.Rung.Cab ? "Кабина" : "Секция " + e.Sec) + " · лист " + (e.Rung.Sheet + 1) + "\n" + e.Title + "\n" + Model.TitleOf(e.Name) +
                "\nПровода: " + Model.NodeName(e.A) + " → " + Model.NodeName(e.B) + "\n" +
                (e.IsLoad ? (e.App.On ? "Включён" : "Выключен: " + Model.WhyOff(e.App)) : e.Closed ? "Замкнут" : "Разомкнут") +
                "\n" + Scheme.OvrName(e) + (e.ExternalOpen ? " · дефект из списка" : "") + "\nЦепь: " + e.Rung.Name + "\nИсточник: " + e.Rung.Src;
        }
        protected override void OnMouseWheel(MouseEventArgs e) { Zoom(e.Delta > 0 ? 1.15f : 1 / 1.15f, e.Location); }
        protected override void OnMouseDown(MouseEventArgs e)
        {
            base.OnMouseDown(e); Focus(); var el = Hit(e.Location);
            if (el != null)
            {
                FocusedElement = el; if (Selected != null) Selected(el);
                if (e.Button == MouseButtons.Left) { Model.SetOverride(el, Model.NextOverride(el)); if (Changed != null) Changed(); }
                if (e.Button == MouseButtons.Right)
                {
                    if (menu != null) menu.Dispose();
                    menu = new ContextMenuStrip();
                    string[] labels = el.Kind == ElKind.Lamp ? new[] { "Исправно", "Перегорела" } : el.IsLoad ? new[] { "Норма", "Обрыв катушки", "Заклинен включённым", "Заклинен выключенным" } : new[] { "Норма", "Обрыв", "Перемычка" };
                    for (int i = 0; i < labels.Length; i++) { int v = i; var item = menu.Items.Add(labels[i]); item.Click += (s, a) => { Model.SetOverride(el, v); if (Changed != null) Changed(); }; }
                    menu.Show(this, e.Location);
                }
                Invalidate(); return;
            }
            // Clicking a wire number highlights its full electrical node on this sheet.
            foreach (var r in VisibleRungs)
                foreach (var l in r.Labs)
                    if (Math.Abs(e.X - (l.X * ScaleUnit + 16 + AutoScrollPosition.X)) < 18 && Math.Abs(e.Y - (l.Y * ScaleUnit + 14 + AutoScrollPosition.Y)) < 9) { highlightedNode = l.Node; Invalidate(); return; }
            if (e.Button == MouseButtons.Left || e.Button == MouseButtons.Middle)
            { dragging = true; drag = e.Location; scroll = AutoScrollPosition; Capture = true; Cursor = Cursors.SizeAll; }
        }
        protected override void OnMouseMove(MouseEventArgs e)
        {
            base.OnMouseMove(e);
            if (dragging) { AutoScrollPosition = new Point(-scroll.X - e.X + drag.X, -scroll.Y - e.Y + drag.Y); return; }
            var el = Hit(e.Location);
            if (el != hovered) { hovered = el; tip.SetToolTip(this, el == null ? "" : Description(el)); Cursor = el == null ? Cursors.Default : Cursors.Hand; Invalidate(); }
        }
        protected override void OnMouseLeave(EventArgs e) { base.OnMouseLeave(e); if (hovered != null) { hovered = null; Invalidate(); } }
        protected override void OnMouseUp(MouseEventArgs e) { dragging = false; Capture = false; Cursor = Cursors.Default; base.OnMouseUp(e); }
        int highlightedNode = -1;
        CircuitMap flow = new CircuitMap();
        Color NodeColor(int n) { int c; return n == highlightedNode ? Th.Amber : flow.Node.TryGetValue(n, out c) ? Bright(CircuitColors.Of(c)) : Model.Live(n) ? Color.FromArgb(70, 150, 140) : Th.Dim; }
        // the circuit colours are chosen for the white sheet; on the dark canvas they are lightened
        static Color Bright(Color c) { return Color.FromArgb(c.R + (255 - c.R) * 3 / 10, c.G + (255 - c.G) * 3 / 10, c.B + (255 - c.B) * 3 / 10); }
        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e); if (Model == null) return;
            var g = e.Graphics; g.SmoothingMode = SmoothingMode.AntiAlias;
            flow = Model.Flow(Section);
            g.TranslateTransform(AutoScrollPosition.X + 16, AutoScrollPosition.Y + 28);
            float u = ScaleUnit;
            foreach (var r in VisibleRungs)
            {
                float y = r.Y * u;
                if (y + r.H * u + AutoScrollPosition.Y < -30 || y + AutoScrollPosition.Y > Height) continue;
                using (var pen = new Pen(Th.Line)) g.DrawLine(pen, 0, y - 0.48f * u, Math.Max(Width, r.W * u), y - 0.48f * u);
                // a rung whose apparatus is on is "assembled": a bar on the left and a bright name
                bool active = r.Els.Any(el => el.IsLoad && el.App.On);
                if (active) using (var bar = new SolidBrush(Th.Green)) g.FillRectangle(bar, -10, y - 0.40f * u, 3, (r.H - 0.1f) * u);
                Th.Txt(g, r.Name, Th.F(Math.Max(7, u * 0.22f), active), r.Src.Contains("В") ? Th.Amber : active ? Th.Text : Th.Muted, new RectangleF(0, y - 0.35f * u, (Scheme.NameW - 0.2f) * u, (r.H + 0.5f) * u), StringAlignment.Near, StringAlignment.Near, true);
                foreach (var s in r.Segs)
                {
                    bool lit = s.Node == highlightedNode || flow.Node.ContainsKey(s.Node);
                    if (lit) using (var glow = new Pen(Color.FromArgb(60, NodeColor(s.Node)), 6.5f) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawLine(glow, s.X1 * u, s.Y1 * u, s.X2 * u, s.Y2 * u);
                    using (var pen = new Pen(NodeColor(s.Node), lit ? 2.4f : 1.2f)) g.DrawLine(pen, s.X1 * u, s.Y1 * u, s.X2 * u, s.Y2 * u);
                }
                foreach (var l in r.Labs) Th.Txt(g, l.Text, Th.Mono(Math.Max(6, u * 0.17f), false), NodeColor(l.Node), new RectangleF((l.X - 0.5f) * u, (l.Y - 0.46f) * u, u, 0.4f * u), StringAlignment.Center);
                foreach (var el in r.Els) DrawElement(g, el);
            }
        }
        void DrawElement(Graphics g, El e)
        {
            float u = ScaleUnit, x = e.X * u, y = e.Y * u, w = e.W * u / 2;
            bool found = Query.Length > 0 && e.Name.IndexOf(Query, StringComparison.OrdinalIgnoreCase) >= 0;
            var box = new RectangleF(x - w + 2, y - 0.40f * u, w * 2 - 4, 0.96f * u);
            if (e == FocusedElement)
            {
                var halo = box; halo.Inflate(3, 3);
                Th.Fill(g, halo, 9, Color.FromArgb(22, Th.Down), Color.FromArgb(70, Th.Down), 3);
                Th.Fill(g, box, 7, Color.FromArgb(48, Th.Down), Th.Down, 1.6f);
            }
            else if (e == hovered) Th.Fill(g, box, 7, Color.FromArgb(26, Th.Text), Color.FromArgb(90, Th.Text));
            if (found && e != FocusedElement)
                using (var p = new Pen(Th.Amber, 1.4f) { DashStyle = DashStyle.Dash }) using (var path = Th.Round(box, 7)) g.DrawPath(p, path);
            Color c = e.Ovr == 1 || e.Ovr == 3 || e.ExternalOpen ? Th.Warn : e.Ovr == 2 ? Th.Amber : e.IsLoad ? (flow.El.ContainsKey(e.Id) ? Bright(CircuitColors.Of(flow.El[e.Id])) : e.App.On ? Th.Accent : Th.Dim) : flow.El.ContainsKey(e.Id) ? Bright(CircuitColors.Of(flow.El[e.Id])) : e.Closed ? NodeColor(e.A) : Th.Dim;
            using (var p = new Pen(c, 1.8f))
            {
                float left = x - 0.28f * u, right = x + 0.28f * u, half = 0.20f * u;
                g.DrawLine(p, x - w, y, e.Kind == ElKind.Wire ? x - 0.08f * u : left, y);
                g.DrawLine(p, e.Kind == ElKind.Wire ? x + 0.08f * u : right, y, x + w, y);
                if (e.Kind == ElKind.Wire) { if (e.Ovr != 1) g.DrawLine(p, x - 0.08f * u, y, x + 0.08f * u, y); }
                else if (e.IsLoad)
                {
                    if (e.App.On)
                    {
                        using (var glow = new Pen(Color.FromArgb(55, c), 5)) g.DrawEllipse(glow, left - 2, y - half - 2, right - left + 4, 2 * half + 4);
                        using (var b = new SolidBrush(Color.FromArgb(110, c))) g.FillEllipse(b, left, y - half, right - left, 2 * half);
                    }
                    g.DrawEllipse(p, left, y - half, right - left, 2 * half);
                    if (e.Kind == ElKind.Lamp) { g.DrawLine(p, left + 3, y - half + 3, right - 3, y + half - 3); g.DrawLine(p, left + 3, y + half - 3, right - 3, y - half + 3); }
                    if (e.Kind == ElKind.Motor) Th.Txt(g, "М", Th.F(Math.Max(7, u * 0.22f)), c, new RectangleF(left, y - half, right - left, 2 * half), StringAlignment.Center);
                }
                else if (e.Kind == ElKind.Diode)
                {
                    float sign = e.Rev ? -1 : 1;
                    g.DrawPolygon(p, new[] { new PointF(x - sign * half, y - half), new PointF(x - sign * half, y + half), new PointF(x + sign * half, y) });
                    g.DrawLine(p, x + sign * half, y - half, x + sign * half, y + half);
                }
                else if (e.Kind == ElKind.Res) g.DrawRectangle(p, left, y - half / 2, right - left, half);
                else
                {
                    g.DrawLine(p, left, y - half, left, y + half); g.DrawLine(p, right, y - half, right, y + half);
                    if (e.Closed) g.DrawLine(p, left, y, right, y);
                    if (e.Nc) g.DrawLine(p, left - 2, y + half, right + 2, y - half);
                }
                if (e.Ovr == 1 || e.ExternalOpen) { g.DrawLine(p, x - half, y - half, x + half, y + half); g.DrawLine(p, x - half, y + half, x + half, y - half); }
                // a jumper is drawn as a bridge over the element
                if (e.Ovr == 2 && !e.IsLoad) g.DrawArc(p, left - 0.06f * u, y - 0.36f * u, right - left + 0.12f * u, 0.5f * u, 180, 180);
            }
            if (e.Ovr != 0 || e.ExternalOpen)
            {
                string tag = e.Kind == ElKind.Lamp ? "перегорела" : e.IsLoad ? (e.Ovr == 2 ? "заклинен вкл." : e.Ovr == 3 ? "заклинен выкл." : "обрыв") : e.Ovr == 2 ? "перемычка" : "обрыв";
                Th.Txt(g, tag, Th.F(Math.Max(6, u * 0.16f), true), c, new RectangleF(x - 1.2f * u, y - 0.74f * u, 2.4f * u, 0.3f * u), StringAlignment.Center);
            }
            if (e.Kind != ElKind.Wire) Th.Txt(g, e.Name, Th.F(Math.Max(6, u * 0.20f), true), c, new RectangleF(x - w, y + 0.22f * u, w * 2, 0.38f * u), StringAlignment.Center);
        }
    }
}
