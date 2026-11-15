// Original PDF pages are immutable backgrounds. Hotspots refer to model element ids without changing the drawing.
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

namespace Trainer2TE116
{
    public sealed class AtlasHit
    {
        public int page { get; set; }
        public string label { get; set; }
        public string kind { get; set; }
        public string title { get; set; }
        public string binding { get; set; }
        public double x { get; set; }
        public double y { get; set; }
        public double w { get; set; }
        public double h { get; set; }
        public int[] modelIds { get; set; }
        public string[] candidates { get; set; }
    }
    public sealed class AtlasPage
    {
        public int page { get; set; }
        public string title { get; set; }
        public string image { get; set; }
        public AtlasHit[] hits { get; set; }
        public AtlasHit[] hotspots { get; set; }
    }
    public sealed class SchemeAtlas
    {
        public string modelSignature { get; set; }
        public AtlasPage[] pages { get; set; }
        public AtlasHit[] hits { get; set; }
        public AtlasHit[] hotspots { get; set; }
    }

    // A numbered wire of the original drawing: the strokes of the PDF that carry this number (tools/build_scheme_wires.py).
    public sealed class WirePiece
    {
        public string n { get; set; }
        public string m { get; set; }   // the model wire this number continues through a clamp, when the model has no node of its own for it
        public int[] s { get; set; }
        internal float[] Pts; internal RectangleF Box; internal GraphicsPath Path;
    }
    public sealed class WirePage { public int page { get; set; } public WirePiece[] wires { get; set; } }
    public sealed class WireFile { public string modelSignature { get; set; } public WirePage[] pages { get; set; } }

    public sealed class OriginalSchemeCanvas : ScrollableControl
    {
        public Scheme Model;
        public SchemeAtlas Atlas { get; private set; }
        public string AssetDirectory { get; private set; }
        public string LoadError { get; private set; }
        public string BindingError { get; private set; }
        public int Section = 1;
        public Action<El> Selected;
        public Action<AtlasHit, string> HitSelected;
        public Action Changed;
        public Action<int> PageChanged;
        public int PageIndex { get; private set; }
        public float ImageScale { get; private set; }
        public AtlasHit FocusedHit { get; private set; }
        public string Query = "";
        public bool ShowOverlay = true;
        readonly ToolTip tooltip = new ToolTip { AutoPopDelay = 25000, InitialDelay = 250 };
        Image bitmap;
        AtlasHit hover;
        CircuitMap flow = new CircuitMap();
        WireFile wireFile; WirePiece[] pageWires = new WirePiece[0]; WirePiece hoverWire;
        public WirePiece FocusedWire { get; private set; }
        public Action<WirePiece> WireSelected;
        public IEnumerable<WirePiece> PageWires { get { return pageWires; } }
        public int LiveWires { get; private set; }
        ContextMenuStrip menu;
        Point drag, scroll; bool dragging, fitted = true;
        readonly Dictionary<AtlasHit, Dictionary<int, int>> assignments = new Dictionary<AtlasHit, Dictionary<int, int>>();
        readonly List<AtlasHit> allHits = new List<AtlasHit>();
        public IEnumerable<AtlasHit> AllHits { get { return allHits; } }
        public IEnumerable<AtlasHit> PageHits { get { return Atlas == null ? Enumerable.Empty<AtlasHit>() : allHits.Where(h => h.page == Atlas.pages[PageIndex].page); } }
        public bool Available { get { return Atlas != null && Atlas.pages != null && Atlas.pages.Length > 0; } }
        public OriginalSchemeCanvas()
        {
            DoubleBuffered = true; AutoScroll = true; BackColor = Th.Inset; TabStop = true;
            SetStyle(ControlStyles.Selectable, true); ImageScale = 1;
        }
        public void LoadAtlas(string directory)
        {
            AssetDirectory = Path.GetFullPath(directory); LoadError = null; allHits.Clear(); assignments.Clear();
            FocusedHit = hover = null; Query = ""; tooltip.SetToolTip(this, "");
            if (bitmap != null) { bitmap.Dispose(); bitmap = null; }
            Atlas = null;
            try
            {
                string json = File.ReadAllText(Path.Combine(AssetDirectory, "atlas.json"));
                Atlas = new JavaScriptSerializer { MaxJsonLength = 32 * 1024 * 1024 }.Deserialize<SchemeAtlas>(json);
                if (Atlas == null || Atlas.pages == null || Atlas.pages.Length == 0) throw new InvalidDataException("В атласе нет страниц.");
                string expected;
                using (var sha = SHA256.Create()) expected = BitConverter.ToString(sha.ComputeHash(Encoding.UTF8.GetBytes(SchemeData.Text))).Replace("-", "");
                BindingError = string.Equals(expected, Atlas.modelSignature, StringComparison.OrdinalIgnoreCase) ? null : "Привязки неактивны: атлас создан для другой версии данных модели.";
                foreach (var p in Atlas.pages)
                {
                    foreach (var hit in (p.hits ?? new AtlasHit[0]).Concat(p.hotspots ?? new AtlasHit[0])) { if (hit.page == 0) hit.page = p.page; allHits.Add(hit); }
                }
                allHits.AddRange(Atlas.hits ?? new AtlasHit[0]); allHits.AddRange(Atlas.hotspots ?? new AtlasHit[0]);
                allHits.RemoveAll(h => h.w <= 0 || h.h <= 0 || h.x < 0 || h.y < 0 || h.x + h.w > 1.001 || h.y + h.h > 1.001);
                wireFile = null;
                try
                {
                    string wires = Path.Combine(AssetDirectory, "wires.json");
                    if (File.Exists(wires)) wireFile = new JavaScriptSerializer { MaxJsonLength = 64 * 1024 * 1024 }.Deserialize<WireFile>(File.ReadAllText(wires));
                }
                catch { wireFile = null; }   // the sheet stays usable without the wire tracing
                SetPage(0); Fit();
            }
            catch (Exception e) { Atlas = null; LoadError = "Не удалось открыть оригинал схемы: " + e.Message; Invalidate(); }
        }
        public void ResetModel(Scheme model)
        { Model = model; assignments.Clear(); FocusedHit = hover = null; FocusedWire = hoverWire = null; tooltip.SetToolTip(this, ""); Invalidate(); }
        public void SetPage(int pageIndex)
        {
            if (!Available) return;
            PageIndex = Math.Max(0, Math.Min(Atlas.pages.Length - 1, pageIndex));
            if (bitmap != null) { bitmap.Dispose(); bitmap = null; }
            try
            {
                var page = Atlas.pages[PageIndex];
                string path = Path.GetFullPath(Path.Combine(AssetDirectory, string.IsNullOrEmpty(page.image) ? "page-" + page.page.ToString("00") + ".png" : page.image));
                if (!path.StartsWith(AssetDirectory + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase)) throw new InvalidDataException("Файл страницы вне папки атласа.");
                bitmap = Image.FromFile(path); LoadError = null;
            }
            catch (Exception e) { LoadError = "Страница недоступна: " + e.Message; }
            foreach (var w in pageWires) if (w.Path != null) { w.Path.Dispose(); w.Path = null; }
            pageWires = new WirePiece[0]; FocusedWire = hoverWire = null;
            if (bitmap != null && wireFile != null && wireFile.pages != null)
            {
                var wp = wireFile.pages.FirstOrDefault(q => q.page == Atlas.pages[PageIndex].page);
                if (wp != null && wp.wires != null) pageWires = wp.wires.Where(w => w.s != null && w.s.Length >= 4 && !string.IsNullOrEmpty(w.n)).ToArray();
                foreach (var w in pageWires)
                {
                    // strokes are kept in the pixels of the page picture, so one uniform scale draws them over it
                    w.Pts = new float[w.s.Length]; w.Path = new GraphicsPath();
                    float x0 = float.MaxValue, y0 = float.MaxValue, x1 = 0, y1 = 0;
                    for (int i = 0; i + 3 < w.s.Length; i += 4)
                    {
                        float ax = w.s[i] * bitmap.Width / 10000f, ay = w.s[i + 1] * bitmap.Height / 10000f, bx = w.s[i + 2] * bitmap.Width / 10000f, by = w.s[i + 3] * bitmap.Height / 10000f;
                        w.Pts[i] = ax; w.Pts[i + 1] = ay; w.Pts[i + 2] = bx; w.Pts[i + 3] = by;
                        w.Path.StartFigure(); w.Path.AddLine(ax, ay, bx, by);
                        x0 = Math.Min(x0, Math.Min(ax, bx)); y0 = Math.Min(y0, Math.Min(ay, by)); x1 = Math.Max(x1, Math.Max(ax, bx)); y1 = Math.Max(y1, Math.Max(ay, by));
                    }
                    w.Box = RectangleF.FromLTRB(x0, y0, x1, y1);
                }
            }
            AutoScrollPosition = Point.Empty; hover = null; tooltip.SetToolTip(this, "");
            if (fitted) Fit(); else Extent();
            if (PageChanged != null) PageChanged(PageIndex);
        }
        public void Fit()
        {
            fitted = true;
            if (bitmap != null) ImageScale = Math.Max(0.01f, Math.Min((ClientSize.Width - 24f) / bitmap.Width, (ClientSize.Height - 24f) / bitmap.Height));
            AutoScrollPosition = Point.Empty; Extent();
        }
        public void Zoom(float factor, Point anchor)
        {
            if (bitmap == null) return;
            float next = Math.Max(0.01f, Math.Min(3, ImageScale * factor)), k = next / ImageScale;
            Point old = AutoScrollPosition; fitted = false; ImageScale = next; Extent();
            AutoScrollPosition = new Point((int)((anchor.X - old.X - 12) * k - anchor.X + 12), (int)((anchor.Y - old.Y - 12) * k - anchor.Y + 12));
        }
        void Extent()
        {
            AutoScrollMinSize = bitmap == null ? Size.Empty : new Size((int)Math.Ceiling(bitmap.Width * ImageScale) + 24, (int)Math.Ceiling(bitmap.Height * ImageScale) + 24);
            Invalidate();
        }
        protected override void OnResize(EventArgs e) { base.OnResize(e); if (fitted && bitmap != null) Fit(); }
        public RectangleF HitBounds(AtlasHit hit)
        {
            if (bitmap == null) return RectangleF.Empty;
            return new RectangleF((float)(hit.x * bitmap.Width * ImageScale) + 12 + AutoScrollPosition.X, (float)(hit.y * bitmap.Height * ImageScale) + 12 + AutoScrollPosition.Y, Math.Max(3, (float)(hit.w * bitmap.Width * ImageScale)), Math.Max(3, (float)(hit.h * bitmap.Height * ImageScale)));
        }
        public AtlasHit Hit(Point point)
        {
            // Expanded hover targets keep tiny PDF labels usable while avoiding the assumption that a nearby line is the same contact.
            return PageHits.Select(h => new { H = h, R = HitBounds(h) }).Where(v => { var r = v.R; r.Inflate(2, 2); return r.Contains(point); }).OrderBy(v => v.R.Width * v.R.Height).Select(v => v.H).FirstOrDefault();
        }
        public List<El> Resolve(AtlasHit hit)
        {
            if (Model == null || hit == null || BindingError != null || (hit.binding != "symbol" && hit.binding != "wire-pair" && hit.binding != "label" && hit.binding != "wire-number" && hit.binding != "unverified")) return new List<El>();
            return (hit.modelIds ?? new int[0]).Where(id => id >= 0 && id < Model.Els.Count).Select(id => Model.Els[id]).Where(e => e.Rung.Cab || e.Sec == Section).Distinct().ToList();
        }
        public El Assigned(AtlasHit hit)
        {
            if (hit == null) return null;
            var options = Resolve(hit); Dictionary<int, int> map; int id;
            if (assignments.TryGetValue(hit, out map) && map.TryGetValue(Section, out id)) return options.FirstOrDefault(e => e.Id == id);
            return (hit.binding == "symbol" || hit.binding == "wire-pair") && options.Count == 1 ? options[0] : null;
        }
        public void Apply(AtlasHit hit, El element, int value)
        {
            if (!Resolve(hit).Contains(element)) throw new InvalidOperationException("Элемент не входит в привязку этого обозначения PDF.");
            Dictionary<int, int> map;
            if (!assignments.TryGetValue(hit, out map)) assignments[hit] = map = new Dictionary<int, int>();
            map[Section] = element.Id; Model.SetOverride(element, value);
            if (Selected != null) Selected(element);
            if (HitSelected != null) HitSelected(hit, Description(hit));
            if (Changed != null) Changed(); Invalidate();
        }
        // The node of the model a drawn wire number stands for in the chosen section; -1 when the model has no such wire.
        public int WireNode(WirePiece wire)
        {
            if (Model == null || wire == null) return -1;
            int n = Model.NodeIndex(wire.n + "@" + Section);
            if (n < 0) n = Model.NodeIndex(wire.n);
            if (n < 0 && !string.IsNullOrEmpty(wire.m)) { n = Model.NodeIndex(wire.m + "@" + Section); if (n < 0) n = Model.NodeIndex(wire.m); }
            return n;
        }
        public List<El> WireElements(WirePiece wire)
        {
            int n = WireNode(wire);
            return n < 0 ? new List<El>() : Model.Els.Where(e => e.Kind == ElKind.Wire && (e.A == n || e.B == n)).ToList();
        }
        public WirePiece WireAt(Point point)
        {
            if (bitmap == null || pageWires.Length == 0) return null;
            float x = (point.X - AutoScrollPosition.X - 12) / ImageScale, y = (point.Y - AutoScrollPosition.Y - 12) / ImageScale, reach = 5 / ImageScale;
            WirePiece best = null; float bestD = reach;
            foreach (var w in pageWires)
            {
                if (x < w.Box.Left - reach || x > w.Box.Right + reach || y < w.Box.Top - reach || y > w.Box.Bottom + reach) continue;
                for (int i = 0; i + 3 < w.Pts.Length; i += 4)
                {
                    float dx = w.Pts[i + 2] - w.Pts[i], dy = w.Pts[i + 3] - w.Pts[i + 1], len = dx * dx + dy * dy;
                    float t = len <= 0 ? 0 : Math.Max(0, Math.Min(1, ((x - w.Pts[i]) * dx + (y - w.Pts[i + 1]) * dy) / len));
                    float ex = x - w.Pts[i] - t * dx, ey = y - w.Pts[i + 1] - t * dy, d = (float)Math.Sqrt(ex * ex + ey * ey);
                    if (d < bestD) { bestD = d; best = w; }
                }
            }
            return best;
        }
        public string WireDescription(WirePiece wire)
        {
            if (wire == null) return "";
            string text = "Провод " + wire.n + " · лист " + Atlas.pages[PageIndex].page + " · секция " + Section + "\n";
            int n = WireNode(wire);
            if (n < 0) return text + "На чертеже провод есть, в модели цепей отдельного узла с этим номером нет: его состояние не показывается.";
            if (Model.NodeIndex(wire.n + "@" + Section) < 0 && Model.NodeIndex(wire.n) < 0) text += "В модели это продолжение провода " + wire.m + " через зажим.\n";
            var circuits = Model.Flow(Section).Circuits.Where(c => c.Plus.Contains(n) || c.Minus.Contains(n)).ToList();
            text += circuits.Count > 0 ? "По проводу идёт ток. Цепи: " + string.Join(", ", circuits.Take(6).Select(c => c.Load.Name)) + (circuits.Count > 6 ? " и ещё " + (circuits.Count - 6) : "")
                : Model.Live(n) ? "Под напряжением (+), тока нет: цепь дальше разомкнута" : Model.HasMinus(n) ? "Без напряжения, соединён с минусом" : "Без напряжения";
            var parts = WireElements(wire); int broken = parts.Count(e => e.Ovr == 1);
            text += "\nУчастков провода в модели: " + parts.Count + (broken > 0 ? " · оборвано: " + broken : "");
            return text + (parts.Count > 0 ? "\nПравая кнопка: оборвать участок или поставить перемычку." : "");
        }
        public void FocusWire(WirePiece wire)
        {
            FocusedWire = wire; FocusedHit = null;
            if (WireSelected != null) WireSelected(wire);
            Invalidate();
        }
        void ShowWireMenu(WirePiece wire, Point point)
        {
            if (menu != null) menu.Dispose(); menu = new ContextMenuStrip();
            var parts = WireElements(wire);
            if (parts.Count == 0) menu.Items.Add(WireNode(wire) < 0 ? "Провода " + wire.n + " нет в модели" : "У провода " + wire.n + " нет отдельных участков").Enabled = false;
            foreach (var element in parts)
            {
                var el = element;
                var node = new ToolStripMenuItem("Участок " + Model.NodeName(el.A) + " – " + Model.NodeName(el.B) + " · " + el.Rung.Name);
                string[] labels = { "Норма", "Обрыв", "Перемычка" };
                for (int i = 0; i < labels.Length; i++)
                {
                    int state = i; var item = new ToolStripMenuItem(labels[i]) { Checked = el.Ovr == i };
                    item.Click += (s, a) => { Model.SetOverride(el, state); if (Selected != null) Selected(el); if (WireSelected != null) WireSelected(wire); if (Changed != null) Changed(); Invalidate(); };
                    node.DropDownItems.Add(item);
                }
                menu.Items.Add(node);
            }
            menu.Show(this, point);
        }
        public string Description(AtlasHit hit)
        {
            if (hit == null) return "";
            var mapped = Resolve(hit); var current = Assigned(hit);
            string text = "Оригинал PDF · лист " + hit.page + " · секция " + Section + "\n" + (hit.title ?? hit.label ?? "Обозначение") + "\n";
            if (current == null)
            {
                if (mapped.Count == 0) return text + (BindingError ?? "Нет подтверждённой привязки к элементу модели. Вмешательство недоступно.");
                bool same = mapped.All(el => State(el) == State(mapped[0]));
                return text + (hit.binding == "label" || hit.binding == "wire-number" ? "Совпадение обозначения не подтверждает точное положение контакта. " : "Неоднозначная привязка. ") + mapped.Count + " элементов на выбранной секции. Выберите конкретный контакт или цепь в меню." + (same ? "\nВсе кандидаты: " + (State(mapped[0]) ? "включены / замкнуты" : "отключены / разомкнуты") : "\nСостояния кандидатов различаются.");
            }
            text += current.Title + "\nЦепь: " + current.Rung.Name + "\nПровода: " + Model.NodeName(current.A) + " → " + Model.NodeName(current.B) + "\n";
            text += current.IsLoad ? (current.App.On ? "Включён" : "Выключен: " + Model.WhyOff(current.App)) : current.Closed ? "Замкнут" : "Разомкнут";
            Dictionary<int, int> chosen;
            bool manual = !((hit.binding == "symbol" || hit.binding == "wire-pair") && mapped.Count == 1) && assignments.TryGetValue(hit, out chosen) && chosen.ContainsKey(Section);
            return text + "\n" + Scheme.OvrName(current) + (current.ExternalOpen ? " · дефект из списка" : "") + "\n" + (manual ? "Контакт выбран вручную" : "Привязка по символу / паре проводов");
        }
        public bool FindNext(string query)
        {
            if (!Available || string.IsNullOrWhiteSpace(query)) return false;
            Query = query.Trim();
            var matches = allHits.Where(h => (h.label ?? "").IndexOf(Query, StringComparison.OrdinalIgnoreCase) >= 0 || (h.title ?? "").IndexOf(Query, StringComparison.OrdinalIgnoreCase) >= 0).ToList();
            if (matches.Count == 0) return false;
            int pos = FocusedHit == null ? -1 : matches.IndexOf(FocusedHit);
            Focus(matches[(pos + 1) % matches.Count]); return true;
        }
        public bool Navigate(El element)
        {
            if (!Available) return false;
            var hit = allHits.FirstOrDefault(h => (h.modelIds ?? new int[0]).Contains(element.Id));
            if (hit == null) return false;
            Focus(hit); return true;
        }
        public void Focus(AtlasHit hit)
        {
            int page = Array.FindIndex(Atlas.pages, p => p.page == hit.page);
            if (page < 0) return;
            if (page != PageIndex) SetPage(page);
            FocusedHit = hit; FocusedWire = null;
            if (bitmap != null)
            {
                // Search zooms to a readable part of the supplied PDF rather than replacing its content with a redraw.
                if (ImageScale < 0.35f) { fitted = false; ImageScale = 0.35f; Extent(); }
                AutoScrollPosition = new Point(Math.Max(0, (int)((hit.x + hit.w / 2) * bitmap.Width * ImageScale) - ClientSize.Width / 2), Math.Max(0, (int)((hit.y + hit.h / 2) * bitmap.Height * ImageScale) - ClientSize.Height / 2));
            }
            if (HitSelected != null) HitSelected(hit, Description(hit)); Invalidate();
        }
        public void ShowHitMenu(AtlasHit hit, Point point)
        {
            if (menu != null) menu.Dispose(); menu = new ContextMenuStrip();
            var mapped = Resolve(hit); var assigned = Assigned(hit);
            if (mapped.Count == 0) menu.Items.Add("Нет подтверждённого элемента модели").Enabled = false;
            else if (assigned != null) AddStates(menu.Items, hit, assigned);
            else
                foreach (var element in mapped)
                {
                    var node = new ToolStripMenuItem((element.Rung.Cab ? "Кабина" : "С" + element.Sec) + " · " + element.Title + " · " + element.Rung.Name + " (" + Model.NodeName(element.A) + "–" + Model.NodeName(element.B) + ")");
                    AddStates(node.DropDownItems, hit, element); menu.Items.Add(node);
                }
            menu.Show(this, point);
        }
        void AddStates(ToolStripItemCollection items, AtlasHit hit, El element)
        {
            string[] labels = element.Kind == ElKind.Lamp ? new[] { "Исправно", "Перегорела" } : element.IsLoad ? new[] { "Норма", "Обрыв катушки", "Заклинен включённым", "Заклинен выключенным" } : new[] { "Норма", "Обрыв", "Перемычка" };
            for (int i = 0; i < labels.Length; i++) { int state = i; var item = new ToolStripMenuItem(labels[i]) { Checked = element.Ovr == i }; item.Click += (s, a) => Apply(hit, element, state); items.Add(item); }
        }
        protected override void OnMouseWheel(MouseEventArgs e) { Zoom(e.Delta > 0 ? 1.2f : 1 / 1.2f, e.Location); }
        protected override void OnMouseDown(MouseEventArgs e)
        {
            base.OnMouseDown(e); base.Focus(); var hit = Hit(e.Location);
            if (hit != null && e.Button != MouseButtons.Middle)
            {
                FocusedHit = hit; FocusedWire = null; var assigned = Assigned(hit);
                if (HitSelected != null) HitSelected(hit, Description(hit));
                if (e.Button == MouseButtons.Left && assigned != null) Apply(hit, assigned, Model.NextOverride(assigned));
                else if (e.Button == MouseButtons.Right || Resolve(hit).Count > 0) ShowHitMenu(hit, e.Location);
                Invalidate(); return;
            }
            var wire = hit == null && e.Button != MouseButtons.Middle && ShowOverlay ? WireAt(e.Location) : null;
            if (wire != null) { FocusWire(wire); if (e.Button == MouseButtons.Right) ShowWireMenu(wire, e.Location); return; }
            if (e.Button == MouseButtons.Left || e.Button == MouseButtons.Middle) { drag = e.Location; scroll = AutoScrollPosition; dragging = true; Capture = true; Cursor = Cursors.SizeAll; }
        }
        protected override void OnMouseMove(MouseEventArgs e)
        {
            base.OnMouseMove(e);
            if (dragging) { AutoScrollPosition = new Point(-scroll.X - e.X + drag.X, -scroll.Y - e.Y + drag.Y); return; }
            var hit = Hit(e.Location);
            var wire = hit == null && ShowOverlay ? WireAt(e.Location) : null;
            if (hit != hover || wire != hoverWire)
            {
                hover = hit; hoverWire = wire;
                tooltip.SetToolTip(this, hit != null ? Description(hit) : wire != null ? WireDescription(wire) : "");
                Cursor = hit == null && wire == null ? Cursors.Default : Cursors.Hand; Invalidate();
            }
        }
        protected override void OnMouseUp(MouseEventArgs e) { dragging = false; Capture = false; Cursor = Cursors.Default; base.OnMouseUp(e); }
        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e); var g = e.Graphics;
            if (bitmap == null) { Th.Txt(g, LoadError ?? "Оригинальная схема не загружена.", Th.F(11), Th.Muted, new RectangleF(20, 20, Width - 40, Height - 40), StringAlignment.Near, StringAlignment.Near, true); return; }
            g.InterpolationMode = ImageScale < 0.5f ? InterpolationMode.HighQualityBicubic : InterpolationMode.HighQualityBilinear;
            g.DrawImage(bitmap, new RectangleF(AutoScrollPosition.X + 12, AutoScrollPosition.Y + 12, bitmap.Width * ImageScale, bitmap.Height * ImageScale));
            if (!ShowOverlay) return;
            g.SmoothingMode = SmoothingMode.AntiAlias;
            flow = Model == null ? new CircuitMap() : Model.Flow(Section);
            PaintWires(g);
            Color cOn = Color.FromArgb(0, 158, 96), cOff = Color.FromArgb(120, 132, 148), cBreak = Color.FromArgb(222, 38, 60), cJump = Color.FromArgb(235, 150, 0), cSel = Color.FromArgb(36, 112, 255), cFind = Color.FromArgb(150, 60, 220);
            foreach (var hit in PageHits)
            {
                var bounds = HitBounds(hit); if (!bounds.IntersectsWith(ClientRectangle)) continue;
                bounds.Inflate(2.5f, 2.5f); float rad = Math.Min(6, bounds.Height / 3);
                var mapped = Resolve(hit); var assigned = Assigned(hit);
                bool search = Query.Length > 0 && (hit.label ?? "").IndexOf(Query, StringComparison.OrdinalIgnoreCase) >= 0;
                bool sameFault = mapped.Count > 0 && mapped.All(el => el.Ovr == mapped[0].Ovr && el.ExternalOpen == mapped[0].ExternalOpen);
                El shown = assigned ?? (sameFault ? mapped[0] : null);
                bool fault = shown != null && (shown.Ovr != 0 || shown.ExternalOpen);
                if (fault)
                {
                    bool jumper = shown.Ovr == 2 && !shown.IsLoad;
                    Color c = jumper ? cJump : cBreak;
                    Th.Fill(g, bounds, rad, Color.FromArgb(70, c), c, 2);
                    // a badge keeps a defect visible on a sheet shown whole
                    float bx = bounds.Right - 4, by = bounds.Y - 6;
                    using (var b = new SolidBrush(c)) g.FillEllipse(b, bx, by, 11, 11);
                    Th.Txt(g, jumper ? "=" : "!", Th.F(6.5f, true), Color.White, new RectangleF(bx, by - 0.5f, 11, 11), StringAlignment.Center);
                }
                else if (assigned != null)
                {
                    int cc; Color cOnHere = cOn;
                    if (flow.El.TryGetValue(assigned.Id, out cc)) cOnHere = CircuitColors.Of(cc);
                    if (State(assigned) || cOnHere != cOn)
                    {
                        if (assigned.IsLoad) { var halo = bounds; halo.Inflate(3, 3); Th.Fill(g, halo, rad + 3, Color.FromArgb(34, cOnHere), Color.FromArgb(90, cOnHere), 2.5f); }
                        Th.Fill(g, bounds, rad, Color.FromArgb(assigned.IsLoad ? 96 : 60, cOnHere), cOnHere, cOnHere != cOn ? 2.2f : 1.6f);
                    }
                    else Th.Fill(g, bounds, rad, Color.FromArgb(10, cOff), Color.FromArgb(150, cOff));
                }
                else if (mapped.Any(el => flow.El.ContainsKey(el.Id)))
                    // one of the contacts with this caption is in a closed circuit: marked in its colour, which contact it is stays the user's choice
                    using (var pen = new Pen(CircuitColors.Of(flow.El[mapped.First(el => flow.El.ContainsKey(el.Id)).Id]), 2.6f) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawLine(pen, bounds.X + 2, bounds.Bottom, bounds.Right - 2, bounds.Bottom);
                else if (mapped.Count > 0 && mapped.All(el => el.App != null && el.App == mapped[0].App) && mapped[0].App.On)
                    // a caption met at several contacts: only "the apparatus is on" is certain, so it is underlined, not filled
                    using (var pen = new Pen(Color.FromArgb(210, cOn), 2.2f) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawLine(pen, bounds.X + 2, bounds.Bottom, bounds.Right - 2, bounds.Bottom);
                if (search) using (var pen = new Pen(cFind, 1.8f) { DashStyle = DashStyle.Dash }) using (var path = Th.Round(RectangleF.Inflate(bounds, 2, 2), rad + 2)) g.DrawPath(pen, path);
                if (hit == FocusedHit)
                {
                    Th.Fill(g, RectangleF.Inflate(bounds, 5, 5), rad + 5, Color.FromArgb(20, cSel), Color.FromArgb(80, cSel), 3.5f);
                    Th.Fill(g, RectangleF.Inflate(bounds, 2, 2), rad + 2, Color.FromArgb(fault ? 0 : 30, cSel), cSel, 2);
                }
                else if (hit == hover) Th.Fill(g, RectangleF.Inflate(bounds, 1, 1), rad + 1, Color.FromArgb(34, cSel), Color.FromArgb(170, cSel), 1.2f);
            }
            if (flow.Circuits.Count > 0)
            {
                // the key of the closed circuits: the colour of each one and the apparatus it feeds
                var shown = flow.Circuits.GroupBy(c => c.Load.Name + "|" + c.Color).Select(q => q.First()).Take(12).ToList();
                float lw = 150, lh = 18, lx = ClientSize.Width - lw - 14, ly = 10;
                Th.Fill(g, new RectangleF(lx - 8, ly - 4, lw + 12, shown.Count * lh + 30), 9, Color.FromArgb(232, Th.Rail), Th.Line2);
                Th.Txt(g, "Замкнутые цепи: " + flow.Circuits.Count, Th.F(8.5f, true), Th.Text, new RectangleF(lx, ly, lw, lh));
                for (int i = 0; i < shown.Count; i++)
                {
                    float y = ly + 22 + i * lh;
                    using (var pen = new Pen(CircuitColors.Of(shown[i].Color), 3.2f) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawLine(pen, lx, y + lh / 2 - 1, lx + 16, y + lh / 2 - 1);
                    Th.Txt(g, shown[i].Load.Name + (i == shown.Count - 1 && flow.Circuits.Count > shown.Count ? "  …" : ""), Th.F(8.5f), Th.Muted, new RectangleF(lx + 24, y - 1, lw - 24, lh));
                }
            }
            string badge =(ImageScale * 100).ToString("0") + "% · стр. PDF " + Atlas.pages[PageIndex].page + (pageWires.Length > 0 && Model != null ? " · цепей " + flow.Circuits.Count + " · с током " + LiveWires + " из " + pageWires.Count(w => WireNode(w) >= 0) + " проводов" : "");
            float bw = g.MeasureString(badge, Th.F(9, true)).Width + 22;
            Th.Fill(g, new RectangleF(10, ClientSize.Height - 36, bw, 26), 13, Color.FromArgb(235, Th.Rail), Th.Line2);
            Th.Txt(g, badge, Th.F(9, true), Th.Text, new RectangleF(21, ClientSize.Height - 36, bw - 12, 26));
        }
        // Live wires are drawn over the sheet itself: a soft veil dims the drawing, the energised strokes glow through it.
        void PaintWires(Graphics g)
        {
            LiveWires = 0;
            if (pageWires.Length == 0 || Model == null) return;
            var area = new RectangleF(AutoScrollPosition.X + 12, AutoScrollPosition.Y + 12, bitmap.Width * ImageScale, bitmap.Height * ImageScale);
            using (var veil = new SolidBrush(Color.FromArgb(96, 255, 255, 255))) g.FillRectangle(veil, area);
            var state = g.Save();
            g.TranslateTransform(area.X, area.Y); g.ScaleTransform(ImageScale, ImageScale);
            float px = 1 / ImageScale; Color sel = Color.FromArgb(36, 112, 255);
            // a wire in a closed circuit takes the colour of the circuit; one that only has voltage is drawn thin
            var groups = new Dictionary<int, List<WirePiece>>(); var idle = new List<WirePiece>();
            foreach (var w in pageWires)
            {
                int n = WireNode(w), c; if (n < 0) continue;
                if (flow.Node.TryGetValue(n, out c)) { List<WirePiece> list; if (!groups.TryGetValue(c, out list)) groups[c] = list = new List<WirePiece>(); list.Add(w); LiveWires++; }
                else if (Model.Live(n)) idle.Add(w);
            }
            using (var pen = new Pen(Color.FromArgb(150, CircuitColors.Idle), Math.Max(1.4f, 3 * ImageScale) * px) { StartCap = LineCap.Round, EndCap = LineCap.Round })
                foreach (var w in idle) g.DrawPath(pen, w.Path);
            foreach (var pair in groups.OrderBy(q => q.Key))
            {
                Color hot = CircuitColors.Of(pair.Key);
                using (var glow = new Pen(Color.FromArgb(62, hot), Math.Max(7, 11 * ImageScale) * px) { StartCap = LineCap.Round, EndCap = LineCap.Round })
                    foreach (var w in pair.Value) g.DrawPath(glow, w.Path);
                using (var core = new Pen(hot, Math.Max(2.4f, 5.4f * ImageScale) * px) { StartCap = LineCap.Round, EndCap = LineCap.Round })
                    foreach (var w in pair.Value) g.DrawPath(core, w.Path);
            }
            if (hoverWire != null && hoverWire != FocusedWire)
                using (var pen = new Pen(Color.FromArgb(150, sel), Math.Max(3.5f, 7 * ImageScale) * px) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawPath(pen, hoverWire.Path);
            if (FocusedWire != null)
            {
                using (var pen = new Pen(Color.FromArgb(70, sel), Math.Max(9, 14 * ImageScale) * px) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawPath(pen, FocusedWire.Path);
                using (var pen = new Pen(sel, Math.Max(3, 6 * ImageScale) * px) { StartCap = LineCap.Round, EndCap = LineCap.Round }) g.DrawPath(pen, FocusedWire.Path);
            }
            g.Restore(state);
        }
        bool State(El element)
        {
            if (element.IsLoad) return element.App.On;
            if (element.Kind == ElKind.Wire) return element.Closed && (Model.Live(element.A) || Model.Live(element.B));
            return element.Closed;
        }
        protected override void Dispose(bool disposing)
        { if (disposing) { if (bitmap != null) bitmap.Dispose(); if (menu != null) menu.Dispose(); tooltip.Dispose(); foreach (var w in pageWires) if (w.Path != null) w.Path.Dispose(); } base.Dispose(disposing); }
    }
}
