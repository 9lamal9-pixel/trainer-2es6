// Look of the instructor window (the design canvas "Инструктор 2ТЭ116 — интерфейс"): dark ground, rounded cards, one teal
// accent, amber for climbs and warnings, blue for descents, red for limits and violations; Segoe UI with Consolas numbers.
// Controls drawn here: cards, buttons, segmented switches, chips, the rail of sections, the strip of wagons, the scheme of
// the air brakes, key/value lists, the list of violations.
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Globalization;
using System.Linq;
using System.Windows.Forms;

namespace Trainer2TE116
{
    public static class Th
    {
        public static readonly Color Back = C(0x0A1019), Rail = C(0x0D1520), Card = C(0x121C2B), Inset = C(0x0D1520), Raised = C(0x18263A), Line = C(0x1E2B3C), Line2 = C(0x22324A),
            Text = C(0xE8F0FA), Muted = C(0x9DB0C7), Dim = C(0x6F8299), Accent = C(0x5DE1CB), AccentInk = C(0x062420), Warn = C(0xFF8796), WarnBack = C(0x2A1820), WarnLine = C(0x5A2A38),
            Amber = C(0xFFC45C), Up = C(0xFFAA5A), UpBack = C(0x2A2219), Down = C(0x75AEFF), Green = C(0x33C46B), Red = C(0xE64545);
        static Color C(int rgb) { return Color.FromArgb((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255); }
        static readonly Dictionary<string, Font> fonts = new Dictionary<string, Font>();
        public static Font F(float size, bool bold = false) { return Get("Segoe UI", size, bold); }
        public static Font Mono(float size, bool bold = true) { return Get("Consolas", size, bold); }
        static Font Get(string fam, float size, bool bold)
        {
            string k = fam + size + bold; Font f;
            if (!fonts.TryGetValue(k, out f)) { f = new Font(fam, size, bold ? FontStyle.Bold : FontStyle.Regular); fonts[k] = f; }
            return f;
        }
        public static GraphicsPath Round(RectangleF r, float rad)
        {
            var p = new GraphicsPath(); rad = Math.Max(1, Math.Min(rad, Math.Min(r.Width, r.Height) / 2)); float d = rad * 2;
            p.AddArc(r.X, r.Y, d, d, 180, 90); p.AddArc(r.Right - d, r.Y, d, d, 270, 90); p.AddArc(r.Right - d, r.Bottom - d, d, d, 0, 90); p.AddArc(r.X, r.Bottom - d, d, d, 90, 90); p.CloseFigure();
            return p;
        }
        public static void Fill(Graphics g, RectangleF r, float rad, Color fill, Color border, float bw = 1)
        {
            using (var p = Round(r, rad)) { using (var b = new SolidBrush(fill)) g.FillPath(b, p); if (border.A > 0) using (var pen = new Pen(border, bw)) g.DrawPath(pen, p); }
        }
        public static void Txt(Graphics g, string s, Font f, Color c, RectangleF r, StringAlignment h = StringAlignment.Near, StringAlignment v = StringAlignment.Center, bool wrap = false)
        {
            using (var b = new SolidBrush(c)) using (var sf = new StringFormat { Alignment = h, LineAlignment = v, Trimming = StringTrimming.EllipsisCharacter, FormatFlags = wrap ? 0 : StringFormatFlags.NoWrap })
                g.DrawString(s ?? "", f, b, r, sf);
        }
        public static void Smooth(Graphics g) { g.SmoothingMode = SmoothingMode.AntiAlias; g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit; }
        // a pressure of the model (MPa) as the gauges of the pult show it, kgf/cm2
        public static string Kg(double mpa, string f = "0.00") { return N(mpa * Traction.KgfPerMPa, f); }
        public static string N(double v, string f) { return v.ToString(f, CultureInfo.InvariantCulture).Replace('.', ','); }

        public static void Style(DataGridView g)
        {
            g.BackgroundColor = Card; g.BorderStyle = BorderStyle.None; g.GridColor = Line; g.EnableHeadersVisualStyles = false;
            g.ColumnHeadersDefaultCellStyle.BackColor = Card; g.ColumnHeadersDefaultCellStyle.ForeColor = Muted; g.ColumnHeadersDefaultCellStyle.Font = F(9f);
            g.ColumnHeadersDefaultCellStyle.SelectionBackColor = Card; g.ColumnHeadersBorderStyle = DataGridViewHeaderBorderStyle.None; g.ColumnHeadersHeight = 34;
            g.DefaultCellStyle.BackColor = Card; g.DefaultCellStyle.ForeColor = Text; g.DefaultCellStyle.Font = F(9.5f);
            g.DefaultCellStyle.SelectionBackColor = Raised; g.DefaultCellStyle.SelectionForeColor = Text; g.RowTemplate.Height = 30;
            g.CellBorderStyle = DataGridViewCellBorderStyle.SingleHorizontal; g.RowHeadersVisible = false;
            g.AlternatingRowsDefaultCellStyle.BackColor = Color.FromArgb(0x14, 0x1F, 0x30);
        }
        public static void Style(TextBox t) { t.BackColor = Inset; t.ForeColor = Text; t.BorderStyle = BorderStyle.FixedSingle; t.Font = F(10.5f); }
    }

    // card: rounded panel with a title
    public class Card : Panel
    {
        public string Title = ""; public string Note = "";
        public Card(string title) { Title = title; DoubleBuffered = true; BackColor = Th.Back; Padding = new Padding(14, title.Length > 0 ? 40 : 14, 14, 14); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g);
            Th.Fill(g, new RectangleF(0.5f, 0.5f, Width - 1.5f, Height - 1.5f), 12, Th.Card, Th.Line);
            if (Title.Length > 0) Th.Txt(g, Title, Th.F(11f, true), Th.Text, new RectangleF(14, 8, Width - 28, 26));
            if (Note.Length > 0) Th.Txt(g, Note, Th.F(8.5f), Th.Muted, new RectangleF(14, 8, Width - 28, 26), StringAlignment.Far);
        }
        protected override void OnResize(EventArgs e) { base.OnResize(e); Invalidate(); }
    }

    public enum Look { Secondary, Primary, Danger, Selected, Ghost }
    // button: a real Button (keyboard, focus, accessibility) drawn in the theme
    public class Btn : Button
    {
        public Look Look; bool hover;
        public Btn(string text, Look look = Look.Secondary, int w = 120, int h = 38) { Text = text; Look = look; Size = new Size(w, h); FlatStyle = FlatStyle.Flat; FlatAppearance.BorderSize = 0; Cursor = Cursors.Hand; Font = Th.F(10f, true); Margin = new Padding(4); BackColor = Th.Card; }
        protected override void OnMouseEnter(EventArgs e) { hover = true; Invalidate(); base.OnMouseEnter(e); }
        protected override void OnMouseLeave(EventArgs e) { hover = false; Invalidate(); base.OnMouseLeave(e); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g); g.Clear(Parent != null ? (Parent is Card ? Th.Card : Parent.BackColor) : Th.Back);
            Color fill, border, ink;
            switch (Look)
            {
                case Look.Primary: fill = hover ? Color.FromArgb(0x7C, 0xEA, 0xD8) : Th.Accent; border = fill; ink = Th.AccentInk; break;
                case Look.Danger: fill = hover ? Th.WarnBack : Color.Transparent; border = Th.Warn; ink = Th.Warn; break;
                case Look.Selected: fill = Color.FromArgb(0x14, 0x2A, 0x2C); border = Th.Accent; ink = Th.Text; break;
                case Look.Ghost: fill = hover ? Th.Raised : Color.Transparent; border = Color.Transparent; ink = Th.Muted; break;
                default: fill = hover ? Th.Line2 : Th.Raised; border = Th.Line2; ink = Enabled ? Th.Text : Th.Dim; break;
            }
            Th.Fill(g, new RectangleF(0.5f, 0.5f, Width - 1.5f, Height - 1.5f), 9, fill, border, Look == Look.Selected ? 2 : 1);
            Th.Txt(g, Text, Font, ink, new RectangleF(6, 0, Width - 12, Height), TextAlign == ContentAlignment.MiddleLeft ? StringAlignment.Near : StringAlignment.Center);
            if (Focused && ShowFocusCues) using (var p = new Pen(Th.Accent, 1) { DashStyle = DashStyle.Dot }) g.DrawRectangle(p, 3, 3, Width - 7, Height - 7);
        }
    }

    // segmented switch of several options
    public class Seg : Control
    {
        public string[] Items; int sel; public event Action<int> Changed;
        public int Selected { get { return sel; } set { sel = value; Invalidate(); } }
        public Seg(params string[] items) { Items = items; DoubleBuffered = true; Height = 36; Font = Th.F(9.5f, true); Cursor = Cursors.Hand; TabStop = true; }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g); g.Clear(Parent is Card ? Th.Card : Th.Back);
            Th.Fill(g, new RectangleF(0.5f, 0.5f, Width - 1.5f, Height - 1.5f), 9, Th.Inset, Th.Line2);
            float w = (Width - 6f) / Items.Length;
            for (int i = 0; i < Items.Length; i++)
            {
                var r = new RectangleF(3 + i * w, 3, w, Height - 6);
                if (i == sel) Th.Fill(g, r, 7, Th.Line2, Color.Transparent);
                Th.Txt(g, Items[i], Font, i == sel ? Th.Text : Th.Muted, r, StringAlignment.Center);
            }
        }
        protected override void OnMouseClick(MouseEventArgs e) { int i = (int)((e.X - 3) / ((Width - 6f) / Items.Length)); Pick(i); base.OnMouseClick(e); }
        protected override bool IsInputKey(Keys k) { return k == Keys.Left || k == Keys.Right || base.IsInputKey(k); }
        protected override void OnKeyDown(KeyEventArgs e) { if (e.KeyCode == Keys.Left) Pick(sel - 1); if (e.KeyCode == Keys.Right) Pick(sel + 1); base.OnKeyDown(e); }
        void Pick(int i) { if (i < 0 || i >= Items.Length || i == sel) return; sel = i; Invalidate(); if (Changed != null) Changed(i); }
    }

    // chip of the status row
    public class Chip : Control
    {
        public string Caption = "", Value = ""; public Color ValueColor = Th.Text; public bool Alert; public float ValueSize = 17f;
        public Chip(string caption) { Caption = caption; DoubleBuffered = true; Height = 66; }
        public void Set(string value, Color color, bool alert = false) { if (value == Value && color == ValueColor && alert == Alert) return; Value = value; ValueColor = color; Alert = alert; Invalidate(); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g); g.Clear(Th.Back);
            Th.Fill(g, new RectangleF(0.5f, 0.5f, Width - 1.5f, Height - 1.5f), 11, Th.Card, Alert ? Th.WarnLine : Th.Line);
            Th.Txt(g, Caption, Th.F(8.5f), Th.Muted, new RectangleF(12, 6, Width - 20, 18));
            Th.Txt(g, Value, Th.Mono(ValueSize), ValueColor, new RectangleF(10, 24, Width - 16, Height - 28));
        }
    }

    // section button of the rail, with a drawn icon
    public class NavBtn : Button
    {
        public int Icon; public bool On;
        public NavBtn(string text, int icon) { Text = text; Icon = icon; Size = new Size(72, 62); FlatStyle = FlatStyle.Flat; FlatAppearance.BorderSize = 0; Cursor = Cursors.Hand; Margin = new Padding(6, 2, 6, 2); Font = Th.F(8f); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g); g.Clear(Th.Rail);
            if (On) Th.Fill(g, new RectangleF(0, 0, Width, Height), 10, Th.Raised, Color.Transparent);
            var c = On ? Th.Accent : Th.Muted;
            using (var p = new Pen(c, 1.8f) { LineJoin = LineJoin.Round, StartCap = LineCap.Round, EndCap = LineCap.Round })
            {
                float x = Width / 2f - 11, y = 9;
                switch (Icon)
                {
                    case 0: g.DrawLines(p, new[] { new PointF(x, y + 16), new PointF(x + 6, y + 9), new PointF(x + 10, y + 12), new PointF(x + 16, y + 3), new PointF(x + 22, y + 9) }); g.DrawLine(p, x, y + 20, x + 22, y + 20); break;
                    case 1: g.DrawRectangle(p, x + 1, y + 5, 20, 11); g.DrawEllipse(p, x + 4, y + 17, 3, 3); g.DrawEllipse(p, x + 15, y + 17, 3, 3); g.DrawLines(p, new[] { new PointF(x + 5, y + 5), new PointF(x + 5, y + 1), new PointF(x + 12, y + 1), new PointF(x + 12, y + 5) }); break;
                    case 2: for (int k = 0; k < 3; k++) g.DrawRectangle(p, x + k * 7.5f, y + 6, 6, 9); break;
                    case 3: g.DrawEllipse(p, x + 2, y + 1, 18, 18); g.DrawLine(p, x + 11, y + 10, x + 16, y + 5); g.DrawLine(p, x + 7, y + 15, x + 15, y + 15); break;
                    case 4: g.DrawRectangle(p, x + 7, y, 8, 16); g.DrawEllipse(p, x + 9.5f, y + 2.5f, 3, 3); g.DrawEllipse(p, x + 9.5f, y + 9, 3, 3); g.DrawLine(p, x + 11, y + 16, x + 11, y + 21); break;
                    case 5: g.DrawRectangle(p, x + 3, y, 14, 19); g.DrawLine(p, x + 6, y + 7, x + 14, y + 7); g.DrawLine(p, x + 6, y + 11, x + 14, y + 11); g.DrawLine(p, x + 6, y + 15, x + 11, y + 15); break;
                    case 7: g.DrawLine(p, x, y + 6, x + 8, y + 6); g.DrawLine(p, x + 8, y + 2, x + 8, y + 10); g.DrawLine(p, x + 14, y + 2, x + 14, y + 10); g.DrawLine(p, x + 14, y + 6, x + 22, y + 6); g.DrawLine(p, x, y + 17, x + 6, y + 17); g.DrawEllipse(p, x + 6, y + 13, 10, 8); g.DrawLine(p, x + 16, y + 17, x + 22, y + 17); break;
                    default: g.DrawLine(p, x + 11, y + 2, x + 11, y + 20); g.DrawLine(p, x + 2, y + 11, x + 20, y + 11); break;
                }
            }
            Th.Txt(g, Text, Font, c, new RectangleF(0, 36, Width, 22), StringAlignment.Center);
        }
    }

    // key / value rows drawn in a card
    public class ParamList : Control
    {
        public List<Tuple<string, string, Color>> Rows = new List<Tuple<string, string, Color>>();
        public ParamList() { DoubleBuffered = true; }
        public void Set(List<Tuple<string, string, Color>> rows) { Rows = rows; Invalidate(); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g); g.Clear(Th.Card);
            float y = 0, h = 28;
            foreach (var r in Rows)
            {
                if (r.Item1 == "—") { using (var p = new Pen(Th.Line)) g.DrawLine(p, 0, y + 8, Width, y + 8); y += 16; continue; }
                Th.Txt(g, r.Item1, Th.F(9.5f), Th.Muted, new RectangleF(0, y, Width * 0.55f, h));
                Th.Txt(g, r.Item2, Th.Mono(11f), r.Item3, new RectangleF(Width * 0.4f, y, Width * 0.6f, h), StringAlignment.Far);
                y += h;
            }
        }
    }

    // strip of the wagons: one cell per wagon coloured by a value; hover shows the wagon, click selects it
    public class WagonStrip : Control
    {
        public double[] Values = new double[0]; public double Max = 0.45; public bool Signed; public string[] Tips = new string[0];
        public int Rows = 1; public event Action<int> Picked;
        readonly ToolTip tip = new ToolTip(); int lastTip = -1;
        public WagonStrip() { DoubleBuffered = true; Height = 30; Cursor = Cursors.Hand; }
        public void Set(double[] v, double max, bool signed, string[] tips) { Values = v; Max = max; Signed = signed; Tips = tips; Invalidate(); }
        int PerRow { get { return Math.Max(1, (int)Math.Ceiling(Values.Length / (double)Rows)); } }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g); g.Clear(Parent is Card ? Th.Card : Th.Back);
            int n = Values.Length; if (n == 0) return;
            int per = PerRow; float gap = n > 80 ? 1.5f : 3f, w = (Width - gap * (per - 1)) / per, h = (Height - gap * (Rows - 1)) / Rows;
            for (int i = 0; i < n; i++)
            {
                int row = i / per, col = i % per;
                var r = new RectangleF(col * (w + gap), row * (h + gap), w, h);
                Th.Fill(g, r, Math.Min(4, w / 3), Shade(Values[i]), Color.Transparent);
            }
        }
        Color Shade(double v)
        {
            if (double.IsNaN(v)) return Th.Red;
            double x = Math.Max(0, Math.Min(1, Math.Abs(v) / Max));
            Color lo = Color.FromArgb(0x1B, 0x3A, 0x3A), hi = Signed && v < 0 ? Th.Down : Color.FromArgb(0xD9, 0x7A, 0x2A);
            return Color.FromArgb((int)(lo.R + (hi.R - lo.R) * x), (int)(lo.G + (hi.G - lo.G) * x), (int)(lo.B + (hi.B - lo.B) * x));
        }
        int At(Point p)
        {
            int n = Values.Length; if (n == 0) return -1;
            int per = PerRow; float cw = Width / (float)per, ch = Height / (float)Rows;
            int i = (int)(p.Y / ch) * per + (int)(p.X / cw);
            return i >= 0 && i < n ? i : -1;
        }
        protected override void OnMouseMove(MouseEventArgs e) { int i = At(e.Location); if (i != lastTip) { lastTip = i; tip.SetToolTip(this, i >= 0 && i < Tips.Length ? Tips[i] : ""); } base.OnMouseMove(e); }
        protected override void OnMouseClick(MouseEventArgs e) { int i = At(e.Location); if (i >= 0 && Picked != null) Picked(i); base.OnMouseClick(e); }
    }

    // list of violations as cards
    public class ViolationList : ListBox
    {
        public ViolationList() { DrawMode = DrawMode.OwnerDrawVariable; BorderStyle = BorderStyle.None; BackColor = Th.Card; ForeColor = Th.Text; IntegralHeight = false; }
        protected override void OnMeasureItem(MeasureItemEventArgs e) { e.ItemHeight = Items[e.Index] is JournalEntry ? 74 : 22; }
        protected override void OnDrawItem(DrawItemEventArgs e)
        {
            if (e.Index < 0) return;
            var g = e.Graphics; Th.Smooth(g);
            using (var b = new SolidBrush(Th.Card)) g.FillRectangle(b, e.Bounds);
            var it = Items[e.Index];
            var je = it as JournalEntry;
            if (je == null) { Th.Txt(g, it.ToString(), Th.Mono(9f, false), Th.Muted, new RectangleF(e.Bounds.X + 4, e.Bounds.Y, e.Bounds.Width - 8, e.Bounds.Height)); return; }
            var r = new RectangleF(e.Bounds.X + 2, e.Bounds.Y + 3, e.Bounds.Width - 6, e.Bounds.Height - 7);
            Th.Fill(g, r, 10, je.Gross ? Th.WarnBack : Th.Inset, je.Gross ? Th.WarnLine : Color.Transparent);
            Th.Txt(g, je.Clock, Th.Mono(9.5f), je.Gross ? Th.Warn : Th.Amber, new RectangleF(r.X + 10, r.Y + 6, 70, 18));
            Th.Txt(g, je.Code + " · " + je.Title + "   " + (je.Gross ? "грубое · " : "") + je.Points, Th.F(10f, true), Th.Text, new RectangleF(r.X + 84, r.Y + 4, r.Width - 92, 20));
            Th.Txt(g, je.Place + " · " + Th.N(je.V, "0") + " км/ч · " + je.Detail, Th.F(9f), Th.Muted, new RectangleF(r.X + 84, r.Y + 24, r.Width - 92, 18));
            Th.Txt(g, je.Basis, Th.F(8.5f), Th.Dim, new RectangleF(r.X + 84, r.Y + 42, r.Width - 92, 18));
        }
    }

    // scheme of the air brakes with the pressures now and the pipe along the train
    public class AirScheme : Control
    {
        public Simulation Sim;
        public AirScheme() { DoubleBuffered = true; }
        void Box(Graphics g, float x, float y, float w, float h, string title, string big, string small, bool accent = false, Color? bigColor = null)
        {
            Th.Fill(g, new RectangleF(x, y, w, h), 12, accent ? Color.FromArgb(0x14, 0x2A, 0x2C) : Th.Raised, accent ? Th.Accent : Th.Line2, accent ? 2 : 1);
            Th.Txt(g, title, Th.F(10f, true), Th.Text, new RectangleF(x + 12, y + 8, w - 20, 20));
            if (big != null) Th.Txt(g, big, Th.Mono(17f), bigColor ?? Th.Text, new RectangleF(x + 10, y + 28, w - 16, 30));
            if (small != null) Th.Txt(g, small, Th.F(8.5f), Th.Muted, new RectangleF(x + 12, y + h - 24, w - 20, 18));
        }
        void Pipe(Graphics g, float x1, float y1, float x2, float y2, bool on, float w = 2) { using (var p = new Pen(on ? Th.Accent : Th.Line2, w)) g.DrawLine(p, x1, y1, x2, y2); }
        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics; Th.Smooth(g); g.Clear(Th.Card);
            var s = Sim; if (s == null) { Th.Txt(g, "Начните поездку", Th.F(12f), Th.Muted, new RectangleF(20, 20, 400, 30)); return; }
            var a = s.Air; float W = Width;
            float k = Math.Min(1.25f, W / 1500f);
            Func<float, float> X = v => v * k;
            string[] cn = { "", "I", "II", "III", "IV", "V", "VI", "Vа" };
            Box(g, X(20), 20, X(170), 84, "Компрессоры КТ-7", null, a.CompressorOn ? "работают" : "выключены (регулятор)");
            Pipe(g, X(190), 60, X(230), 60, a.CompressorOn);
            Box(g, X(230), 20, X(200), 84, "Главные резервуары", Th.Kg(a.Mr.MPa), "кгс/см² · 8 × 250 л · ПР " + Th.Kg(a.Pr.MPa, "0.0") + " · 7,5/9,0", false, Th.Accent);
            Pipe(g, X(430), 60, X(460), 60, true);
            Box(g, X(460), 14, X(250), 96, "Кран машиниста 395: " + cn[s.C.Crane], null, "зарядное " + Th.Kg(a.ChargeMPa, "0.0") + " кгс/см² · стабилизатор · реле", true);
            for (int i = 1; i <= 7; i++)
            {
                int pos = new[] { 0, 1, 2, 3, 4, 5, 7, 6 }[i];
                var r = new RectangleF(X(482) + (i - 1) * X(29), 44, X(26), 24);
                Th.Fill(g, r, 5, pos == s.C.Crane ? Th.Accent : Th.Line2, Color.Transparent);
                Th.Txt(g, cn[pos], Th.F(8.5f, true), pos == s.C.Crane ? Th.AccentInk : Th.Text, r, StringAlignment.Center);
            }
            Pipe(g, X(580), 110, X(580), 150, true);
            Box(g, X(500), 150, X(160), 70, "УР 20 л", Th.Kg(a.Ur.MPa), null);
            Pipe(g, X(710), 60, X(760), 60, true);
            Box(g, X(760), 20, X(180), 84, "ТМ локомотива", Th.Kg(a.TmLoco.MPa), a.EpkTripped ? "ЭПК: СРЫВ" : s.C.EpkKey ? "ЭПК: ключ включён" : "ЭПК: ключ выключен", false, a.EpkTripped ? Th.Warn : (Color?)null);
            Pipe(g, X(940), 62, W - 20, 62, true, 4);
            Th.Txt(g, "тормозная магистраль · " + a.W.Length + " вагонов · " + Th.N(s.Dyn.LengthM, "0") + " м", Th.F(9f), Th.Muted, new RectangleF(X(960), 32, X(500), 20));
            if (a.W.Length > 0)
            {
                WagonBox(g, X(980), 90, X(240), a.W[0], "Вагон 1");
                WagonBox(g, W - X(260), 90, X(240), a.W[a.W.Length - 1], "Вагон " + a.W.Length + " · хвост");
            }
            Box(g, X(240), 250, X(190), 70, "Кран 254: " + s.C.Kvt, null, s.C.Kvt == 1 ? "отпуск автотормоза локомотива" : s.C.Kvt == 2 ? "поездное" : "торможение локомотива");
            Pipe(g, X(430), 285, X(500), 285, s.C.Kvt >= 3);
            Box(g, X(500), 250, X(190), 70, "ВР локомотива", null, a.LocoAutoTc > 0 ? "тормозит до " + Th.N(a.LocoAutoTc, "0.00") : "не тормозит");
            Pipe(g, X(690), 285, X(760), 285, a.LocoTcMPa > 0.01);
            Box(g, X(760), 240, X(180), 84, "ТЦ секций", a.TcLoco.Length > 1 ? Th.Kg(a.TcLoco[0].MPa) + " / " + Th.Kg(a.TcLoco[1].MPa) : Th.Kg(a.LocoTcMPa), "кгс/см² · 6 ТЦ на секцию");
            Th.Txt(g, "Баланс массы воздуха: " + Th.N(a.Balance(), "0.0000") + " кг · в сети " + Th.N(a.TotalMass(), "0.0") + " кг", Th.F(9f), Th.Muted, new RectangleF(X(980), 250, X(500), 20));
            // along the train
            float top = 350, h = Height - top - 30, left = 50, w = W - left - 20;
            if (h < 60 || a.W.Length == 0) return;
            Th.Txt(g, "По длине поезда: ТМ, ЗР, ТЦ каждого вагона, кгс/см²", Th.F(10.5f, true), Th.Text, new RectangleF(20, top - 4, 600, 22));
            top += 24; h -= 24;
            Func<double, float> Y = p => top + h - (float)(Math.Max(0, Math.Min(7 / Traction.KgfPerMPa, p)) / (7 / Traction.KgfPerMPa) * h);
            for (int gi = 0; gi <= 7; gi++) { float y = Y(gi / Traction.KgfPerMPa); using (var p = new Pen(Th.Line)) g.DrawLine(p, left, y, left + w, y); Th.Txt(g, Th.N(gi, "0"), Th.F(8f), Th.Muted, new RectangleF(4, y - 9, 40, 18), StringAlignment.Far); }
            int n = a.W.Length;
            Action<Func<WagonAir, double>, Color, float> series = (f, c, wd) =>
            {
                var pts = new PointF[n];
                for (int i = 0; i < n; i++) pts[i] = new PointF(left + (n == 1 ? 0 : w * i / (n - 1f)), Y(f(a.W[i])));
                if (n > 1) using (var p = new Pen(c, wd)) g.DrawLines(p, pts);
            };
            series(x => x.Zr.MPa, Th.Down, 1.5f); series(x => x.Tc.MPa, Th.Up, 2f); series(x => x.Tm.MPa, Th.Accent, 2.4f);
            Th.Txt(g, "ТМ", Th.F(9f, true), Th.Accent, new RectangleF(left + w - 160, top, 40, 18)); Th.Txt(g, "ЗР", Th.F(9f, true), Th.Down, new RectangleF(left + w - 110, top, 40, 18)); Th.Txt(g, "ТЦ", Th.F(9f, true), Th.Up, new RectangleF(left + w - 60, top, 40, 18));
            Th.Txt(g, "вагон 1", Th.F(8f), Th.Muted, new RectangleF(left, top + h + 2, 80, 16)); Th.Txt(g, "вагон " + n, Th.F(8f), Th.Muted, new RectangleF(left + w - 80, top + h + 2, 80, 16), StringAlignment.Far);
        }
        void WagonBox(Graphics g, float x, float y, float w, WagonAir v, string title)
        {
            Th.Fill(g, new RectangleF(x, y, w, 130), 12, Th.Raised, Th.Line2);
            Th.Txt(g, title + " · ВР 483", Th.F(10f, true), Th.Text, new RectangleF(x + 12, y + 8, w - 20, 20));
            string[] k = { "ТМ", "ЗР", "РК", "ТЦ" }; double[] val = { v.Tm.MPa, v.Zr.MPa, v.Wk.MPa, v.Tc.MPa };
            for (int i = 0; i < 4; i++)
            {
                Th.Txt(g, k[i], Th.F(9f), Th.Muted, new RectangleF(x + 12 + (i % 2) * w / 2, y + 32 + (i / 2) * 24, 40, 20));
                Th.Txt(g, Th.Kg(val[i]), Th.Mono(10.5f), i == 3 && val[i] > 0.02 ? Th.Up : Th.Text, new RectangleF(x + 44 + (i % 2) * w / 2, y + 32 + (i / 2) * 24, 70, 20));
            }
            string st = !v.VrOn ? "ВР выключен" : v.Emergency ? "экстренное" : v.Braking ? "торможение" : v.Tc.MPa > 0.02 ? "отпуск" : "отпущен";
            Th.Txt(g, st + " · " + (v.Mode == 1 ? "порожний" : v.Mode == 3 ? "гружёный" : "средний") + (v.Mountain ? ", горный" : ""), Th.F(9f), v.Braking ? Th.Up : !v.VrOn ? Th.Warn : Th.Green, new RectangleF(x + 12, y + 100, w - 20, 20));
        }
    }
}
