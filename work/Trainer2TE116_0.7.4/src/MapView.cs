// Regime card of the new instructor (the view of the regime card window of the present package, RegimeCard.cs): the route
// with the train, gradients in per mille on every element and always on the previous, current and next ones, signals with
// their aspects, stations, signs, curves, speed and limits, journal. Pictures of the signs from instructor.exe when given.
using System;
using System.Collections;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Globalization;
using System.IO;
using System.Reflection;
using System.Resources;
using System.Threading;
using System.Windows.Forms;

namespace Trainer2TE116
{
    public sealed class RegimeLive
    {
        public double T, Head = double.NaN, V, Allowed = -1, TrainLen, MassT, Tm, Ur, Tc, Pm, Charge, TractionKn, CurrentA;
        public int Axles, Wagons, Km, Crane = 2, Kvt = 2, Trip;
        public bool AllowedByBlok;
        public int[] Aspects;
        public JournalEntry[] Journal = new JournalEntry[0];
        public int JournalCount, Points;
        public string Consist = "";
    }

    // pictures of the signs and signals from the resources of instructor.exe (IWP.PathProfileForm.resources)
    public sealed class RegimeIcons
    {
        public Bitmap[] Signs = new Bitmap[0];
        public readonly Dictionary<int, Bitmap> Lights = new Dictionary<int, Bitmap>();
        static readonly int[] LightKeys = { 0, 1, 2, 4, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20 };

        public static RegimeIcons Load(string fallbackExe)
        {
            var r = new RegimeIcons();
            try
            {
                Stream st = null;
                var entry = Assembly.GetEntryAssembly();
                if (entry != null) st = entry.GetManifestResourceStream("IWP.PathProfileForm.resources");
                if (st == null && fallbackExe != null && File.Exists(fallbackExe))
                    st = Assembly.ReflectionOnlyLoadFrom(fallbackExe).GetManifestResourceStream("IWP.PathProfileForm.resources");
                if (st == null) return r;
                using (st)
                using (var rr = new ResourceReader(st))
                {
                    foreach (DictionaryEntry e in rr)
                    {
                        var key = e.Key as string;
                        var ims = e.Value as ImageListStreamer;
                        if (ims == null) continue;
                        using (var il = new ImageList())
                        {
                            il.ImageStream = ims;
                            if (key == "SignsImageList.ImageStream")
                            {
                                r.Signs = new Bitmap[il.Images.Count];
                                for (int i = 0; i < il.Images.Count; i++) r.Signs[i] = new Bitmap(il.Images[i]);
                            }
                            else if (key == "TrafficLightsImageList.ImageStream")
                                for (int i = 0; i < il.Images.Count && i < LightKeys.Length; i++) r.Lights[LightKeys[i]] = new Bitmap(il.Images[i]);
                        }
                    }
                }
            }
            catch { }
            return r;
        }
    }

    public sealed class RegimePalette
    {
        public Color Back, Card, Inset, Grid, Text, Muted, Accent, Up, Down, Level, Limit, Blok, Warn, Band;
        public static RegimePalette For(bool light)
        {
            if (light) return new RegimePalette { Back = Color.FromArgb(236, 241, 247), Card = Color.White, Inset = Color.FromArgb(242, 246, 251), Grid = Color.FromArgb(210, 220, 232), Text = Color.FromArgb(25, 38, 56), Muted = Color.FromArgb(92, 110, 132), Accent = Color.FromArgb(0, 120, 112), Up = Color.FromArgb(178, 64, 20), Down = Color.FromArgb(28, 98, 196), Level = Color.FromArgb(96, 106, 120), Limit = Color.FromArgb(200, 40, 60), Blok = Color.FromArgb(214, 128, 0), Warn = Color.FromArgb(196, 30, 58), Band = Color.FromArgb(40, 0, 120, 112) };
            return new RegimePalette { Back = Color.FromArgb(10, 16, 26), Card = Color.FromArgb(18, 28, 43), Inset = Color.FromArgb(25, 37, 55), Grid = Color.FromArgb(46, 62, 82), Text = Color.FromArgb(234, 242, 252), Muted = Color.FromArgb(150, 170, 194), Accent = Color.FromArgb(93, 225, 203), Up = Color.FromArgb(255, 170, 90), Down = Color.FromArgb(120, 178, 255), Level = Color.FromArgb(170, 180, 194), Limit = Color.FromArgb(255, 102, 122), Blok = Color.FromArgb(255, 196, 92), Warn = Color.FromArgb(255, 92, 112), Band = Color.FromArgb(46, 93, 225, 203) };
        }
    }

    public sealed class RegimeView : Control
    {
        public RegimeIcons Icons = new RegimeIcons();
        public readonly List<double[]> Trace = new List<double[]>();   // x, v, allowed
        public double Span = 10000, Pan;
        public bool Follow = true, Light;
        public Route RouteNow; public RegimeLive LiveNow;
        public double CenterX;
        // last drawn mapping, for clicks of the instructor on the map
        public float MapLeft, MapRight, LightsTop, LightsBottom; public double MapStart, MapEnd = 1;
        public double RouteXAt(float px) { return MapStart + (px - MapLeft) / Math.Max(1f, MapRight - MapLeft) * (MapEnd - MapStart); }
        public float PxAt(double x) { return MapLeft + (float)((x - MapStart) / Math.Max(1e-6, MapEnd - MapStart) * (MapRight - MapLeft)); }
        RegimePalette p = RegimePalette.For(false);
        public RegimePalette P { get { p = RegimePalette.For(Light); return p; } }
        static readonly CultureInfo C = CultureInfo.InvariantCulture;
        readonly Font fTitle = new Font("Segoe UI", 13, FontStyle.Bold), fHead = new Font("Segoe UI", 10, FontStyle.Bold), fBody = new Font("Segoe UI", 9.5f),
            fSmall = new Font("Segoe UI", 8f), fGrade = new Font("Segoe UI", 9f, FontStyle.Bold), fGradeBig = new Font("Segoe UI", 12f, FontStyle.Bold), fMetric = new Font("Segoe UI", 26, FontStyle.Bold);

        public RegimeView()
        {
            SetStyle(ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint | ControlStyles.OptimizedDoubleBuffer | ControlStyles.ResizeRedraw, true);
        }
        protected override void OnPaint(PaintEventArgs e)
        {
            try { Draw(e.Graphics, ClientRectangle, RouteNow, LiveNow); }
            catch (Exception ex) { e.Graphics.Clear(Color.Black); TextRenderer.DrawText(e.Graphics, "Ошибка рисования: " + ex.Message, Font, new Point(10, 10), Color.White); }
        }

        // ---------- drawing helpers
        void Txt(Graphics g, string s, Font f, Color c, RectangleF r, StringAlignment h, StringAlignment v = StringAlignment.Center)
        {
            using (var b = new SolidBrush(c)) using (var sf = new StringFormat { Alignment = h, LineAlignment = v, Trimming = StringTrimming.EllipsisCharacter, FormatFlags = StringFormatFlags.NoWrap })
                g.DrawString(s ?? "", f, b, r, sf);
        }
        void Line(Graphics g, Color c, float w, float x1, float y1, float x2, float y2, DashStyle d = DashStyle.Solid)
        { using (var pen = new Pen(c, w) { DashStyle = d }) g.DrawLine(pen, x1, y1, x2, y2); }
        static GraphicsPath Round(RectangleF r, float rad)
        {
            var path = new GraphicsPath(); rad = Math.Max(1, Math.Min(rad, Math.Min(r.Width, r.Height) / 2)); float d = rad * 2;
            path.AddArc(r.X, r.Y, d, d, 180, 90); path.AddArc(r.Right - d, r.Y, d, d, 270, 90); path.AddArc(r.Right - d, r.Bottom - d, d, d, 0, 90); path.AddArc(r.X, r.Bottom - d, d, d, 90, 90); path.CloseFigure();
            return path;
        }
        void Card(Graphics g, RectangleF r, Color fill, Color border, float rad = 8)
        {
            using (var path = Round(r, rad)) { using (var b = new SolidBrush(fill)) g.FillPath(b, path); using (var pen = new Pen(border, 1f)) g.DrawPath(pen, path); }
        }
        Color GradeColor(double s) { return s > 0.5 ? p.Up : s < -0.5 ? p.Down : p.Level; }
        static string Grade(double s) { return Math.Abs(s) < 0.05 ? "0,0" : s.ToString("+0.0;−0.0", C).Replace('.', ','); }
        static string Num(double v, string f) { return v.ToString(f, C).Replace('.', ','); }
        static int First(double[] xs, double x) { int lo = 0, hi = xs.Length; while (lo < hi) { int m = (lo + hi) / 2; if (xs[m] < x) lo = m + 1; else hi = m; } return lo; }
        static string CraneName(int c) { switch (c) { case 1: return "I"; case 2: return "II"; case 3: return "III"; case 4: return "IV"; case 5: return "V"; case 6: return "VI"; case 7: return "Vа"; default: return "?"; } }

        // ---------- the picture
        public void Draw(Graphics g, Rectangle rc, Route r, RegimeLive l)
        {
            p = RegimePalette.For(Light);
            g.SmoothingMode = SmoothingMode.AntiAlias; g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;
            g.Clear(p.Back);
            if (r == null || r.ProfX.Length == 0) { Txt(g, "Режимная карта: ждём маршрут поездки от инструктора", fTitle, p.Text, new RectangleF(rc.X + 20, rc.Y + 20, rc.Width - 40, 40), StringAlignment.Near); return; }
            float m = 10, hudW = Math.Max(250, Math.Min(330, rc.Width * 0.24f));
            var map = new RectangleF(rc.X + m, rc.Y + m, rc.Width - 3 * m - hudW, rc.Height - 2 * m);
            var hud = new RectangleF(map.Right + m, rc.Y + m, hudW, rc.Height - 2 * m);
            Card(g, map, p.Card, p.Grid); Card(g, hud, p.Card, p.Grid);
            DrawMap(g, map, r, l);
            DrawHud(g, hud, r, l);
        }

        void DrawMap(Graphics g, RectangleF box, Route r, RegimeLive l)
        {
            double head = l != null && !double.IsNaN(l.Head) ? l.Head : 0;
            double span = Math.Max(1500, Math.Min(Math.Max(1500, r.Length), Span));
            double center = Follow ? head + span * 0.25 : Pan;
            double start = Math.Max(0, Math.Min(r.Length - span, center - span / 2)), end = Math.Min(r.Length, start + span);
            CenterX = (start + end) / 2;
            float left = box.X + 54, right = box.Right - 12, w = right - left;
            MapLeft = left; MapRight = right; MapStart = start; MapEnd = end;
            Func<double, float> X = x => left + (float)((x - start) / (end - start) * w);

            // title
            Txt(g, "Режимная карта · " + (r.Name.Length > 0 ? r.Name : "маршрут поездки"), fTitle, p.Text, new RectangleF(box.X + 12, box.Y + 6, box.Width - 24, 24), StringAlignment.Near);
            string sub = l != null ? l.Consist : "";
            if (r.RulingGrade >= 2) sub += (sub.Length > 0 ? " · " : "") + "руководящий подъём " + Grade(r.RulingGrade) + " ‰" + (Journal.WeightNormT > 0 ? ", весовая норма ≈ " + Num(Journal.WeightNormT, "0") + " т" + (Journal.CriticalTrain ? " (поезд критичного веса)" : "") : "");
            Txt(g, sub, fSmall, p.Muted, new RectangleF(box.X + 12, box.Y + 30, box.Width - 24, 16), StringAlignment.Near);

            float top = box.Y + 52, kmH = 20, lightsH = 46, stH = 20, gradeH = 40, curveH = 20, signsH = 30;
            float avail = box.Bottom - 10 - top - kmH - lightsH - stH - gradeH - curveH - signsH;
            float speedH = Math.Max(90, avail * 0.48f), profH = Math.Max(80, avail - speedH - 8);
            float yKm = top, ySpeed = yKm + kmH + 2, yLights = ySpeed + speedH + 4, ySt = yLights + lightsH, yProf = ySt + stH, yGrade = yProf + profH, yCurve = yGrade + gradeH, ySigns = yCurve + curveH;

            // limiting climbs: shaded columns over the profile
            for (int i = 0; i < r.LimitX.Length; i++)
            {
                double a = Math.Max(start, r.LimitX[i]), b = Math.Min(end, r.LimitX[i] + r.LimitLen[i]);
                if (b <= a) continue;
                using (var br = new SolidBrush(Color.FromArgb(Light ? 40 : 34, p.Up))) g.FillRectangle(br, X(a), yProf, X(b) - X(a), profH);
                if (X(b) - X(a) > 70) Txt(g, "лимит. подъём " + Grade(r.LimitSlope[i]) + " ‰", fSmall, p.Up, new RectangleF(X(a), yProf + 2, X(b) - X(a), 14), StringAlignment.Center);
            }

            // km row: posts with picket 0
            using (var br = new SolidBrush(p.Inset)) g.FillRectangle(br, left, yKm, w, kmH);
            Txt(g, "км", fSmall, p.Muted, new RectangleF(box.X + 8, yKm, 40, kmH), StringAlignment.Near);
            int k0 = First(r.PiketX, start); float lastLabel = -1000;
            for (int k = k0; k < r.PiketX.Length && r.PiketX[k] <= end; k++)
            {
                float x = X(r.PiketX[k]);
                if (r.PiketPk[k] == 0)
                {
                    Line(g, p.Grid, 0.8f, x, yKm + kmH, x, yGrade + gradeH);
                    if (x - lastLabel > 34) { Txt(g, r.PiketKm[k].ToString(C), fSmall, p.Text, new RectangleF(x - 22, yKm, 44, kmH), StringAlignment.Center); lastLabel = x; }
                }
                else if (span <= 20000) Line(g, p.Grid, 0.6f, x, yKm + kmH - 5, x, yKm + kmH);
            }

            // speed panel
            Func<double, float> VY = v => ySpeed + speedH - (float)(Math.Max(0, Math.Min(100, v)) / 100.0 * speedH);
            for (int v = 0; v <= 100; v += 20) { float y = VY(v); Line(g, p.Grid, 0.7f, left, y, right, y); Txt(g, v.ToString(C), fSmall, p.Muted, new RectangleF(box.X + 8, y - 8, 40, 16), StringAlignment.Far); }
            Txt(g, "км/ч", fSmall, p.Muted, new RectangleF(box.X + 8, ySpeed + speedH * 0.5f - 7, 44, 14), StringAlignment.Near);
            // static limits of the map
            double step = Math.Max(10, (end - start) / 600);
            PointF prev = PointF.Empty; bool has = false;
            for (double x = start; x <= end + 0.1; x += step)
            {
                var pt = new PointF(X(x), VY(r.StaticLimitAt(x)));
                if (has) { Line(g, p.Limit, 2f, prev.X, prev.Y, pt.X, prev.Y); if (Math.Abs(pt.Y - prev.Y) > 0.5) Line(g, p.Limit, 2f, pt.X, prev.Y, pt.X, pt.Y); }
                prev = pt; has = true;
            }
            // calculated speed on the limiting climbs of a train of critical weight
            if (Journal.CriticalTrain)
                for (int i = 0; i < r.LimitX.Length; i++)
                {
                    double a = Math.Max(start, r.LimitX[i]), b = Math.Min(end, r.LimitX[i] + r.LimitLen[i]);
                    if (b > a) { float y = VY(Journal.CalcSpeed); Line(g, p.Up, 1.5f, X(a), y, X(b), y, DashStyle.Dot); Txt(g, "Vр", fSmall, p.Up, new RectangleF(X(a), y - 15, 30, 14), StringAlignment.Near); }
                }
            // the trip: allowed speed of BLOK and the speed of the train
            DrawTrace(g, X, VY, start, end, 2, p.Blok, 1.6f, DashStyle.Dash);
            DrawTrace(g, X, VY, start, end, 1, p.Accent, 2.2f, DashStyle.Solid);

            // signals
            LightsTop = yLights; LightsBottom = yLights + lightsH;
            Txt(g, "св.", fSmall, p.Muted, new RectangleF(box.X + 8, yLights + 4, 44, 14), StringAlignment.Near);
            float lastLx = -1000;
            for (int k = 0; k < r.LightX.Length; k++)
            {
                if (r.LightX[k] < start || r.LightX[k] > end) continue;
                float x = X(r.LightX[k]);
                int a = l != null && l.Aspects != null && k < l.Aspects.Length ? l.Aspects[k] : -1;
                Bitmap bmp;
                if (a >= 0 && Icons.Lights.TryGetValue(a, out bmp)) g.DrawImage(bmp, x - 6, yLights + 2, 12, 22);
                else { using (var br = new SolidBrush(AspectColor(a))) g.FillEllipse(br, x - 5, yLights + 6, 10, 10); using (var pen = new Pen(p.Muted)) g.DrawEllipse(pen, x - 5, yLights + 6, 10, 10); }
                if (x - lastLx > 34) { Txt(g, r.LightName[k], fSmall, p.Text, new RectangleF(x - 24, yLights + 26, 48, 14), StringAlignment.Center); lastLx = x; }
                Line(g, p.Grid, 0.6f, x, yLights + 40, x, yProf);
            }
            // stations
            for (int k = 0; k < r.StationX.Length; k++)
            {
                if (r.StationX[k] < start || r.StationX[k] > end) continue;
                float x = X(r.StationX[k]);
                using (var br = new SolidBrush(p.Text)) g.FillRectangle(br, x - 1, ySt + 2, 3, stH - 4);
                Txt(g, r.StationName[k], fHead, p.Text, new RectangleF(x + 4, ySt, 180, stH), StringAlignment.Near);
            }

            // profile
            double lo = double.MaxValue, hi = double.MinValue;
            int e0 = Math.Max(0, r.ElementAt(start)), e1 = Math.Max(0, r.ElementAt(end));
            for (int e = e0; e <= e1 + 1 && e < r.ProfX.Length; e++) { double hh = r.ProfH[e]; lo = Math.Min(lo, hh); hi = Math.Max(hi, hh); }
            lo = Math.Min(lo, r.HeightAt(end)); hi = Math.Max(hi, r.HeightAt(end)); hi = Math.Max(hi, lo + 6);
            Func<double, float> HY = hgt => yProf + profH - 8 - (float)((hgt - lo) / (hi - lo) * (profH - 26));
            var pts = new List<PointF> { new PointF(X(start), HY(r.HeightAt(start))) };
            for (int e = e0 + 1; e <= e1 && e < r.ProfX.Length; e++) pts.Add(new PointF(X(r.ProfX[e]), HY(r.ProfH[e])));
            pts.Add(new PointF(X(end), HY(r.HeightAt(end))));
            var area = new List<PointF>(pts) { new PointF(X(end), yProf + profH), new PointF(X(start), yProf + profH) };
            using (var br = new SolidBrush(Color.FromArgb(Light ? 40 : 30, p.Level))) g.FillPolygon(br, area.ToArray());
            using (var pen = new Pen(p.Text, 2.2f)) g.DrawLines(pen, pts.ToArray());
            Txt(g, "путь", fSmall, p.Muted, new RectangleF(box.X + 8, yProf + 4, 44, 14), StringAlignment.Near);

            // the train on the profile and the speed: band from the tail to the head
            int cur = l != null && !double.IsNaN(l.Head) ? r.ElementAt(head) : -1;
            if (l != null && !double.IsNaN(l.Head) && head >= start - l.TrainLen && head <= end + 10)
            {
                float xh = X(head), xt = X(Math.Max(start, head - l.TrainLen));
                using (var br = new SolidBrush(p.Band)) { g.FillRectangle(br, xt, ySpeed, xh - xt, speedH); g.FillRectangle(br, xt, yProf, xh - xt, profH); }
                Line(g, p.Accent, 2f, xh, yKm, xh, ySigns + signsH);
                using (var br = new SolidBrush(p.Accent)) g.FillPolygon(br, new[] { new PointF(xh, yKm + 8), new PointF(xh - 6, yKm - 2), new PointF(xh + 6, yKm - 2) });
                using (var br = new SolidBrush(p.Accent)) g.FillEllipse(br, xh - 5, VY(l.V) - 5, 10, 10);
            }

            // gradients: every element where it fits, the previous / current / next always (callouts when narrow)
            using (var br = new SolidBrush(p.Inset)) g.FillRectangle(br, left, yGrade, w, gradeH);
            Txt(g, "‰", fGrade, p.Muted, new RectangleF(box.X + 8, yGrade, 40, gradeH), StringAlignment.Near);
            for (int e = e0; e <= e1 && e < r.ProfX.Length; e++)
            {
                float x1 = X(Math.Max(start, r.ProfX[e])), x2 = X(Math.Min(end, r.ProfX[e] + r.ProfLen[e]));
                Line(g, p.Grid, 0.9f, x1, yProf + 10, x1, yGrade + gradeH);
                bool special = cur >= 0 && (e == cur || e == cur - 1 || e == cur + 1);
                if (special) continue;
                if (x2 - x1 >= 30) Txt(g, Grade(r.ProfSlope[e]), fGrade, GradeColor(r.ProfSlope[e]), new RectangleF(x1, yGrade + 2, x2 - x1, 18), StringAlignment.Center);
                if (x2 - x1 >= 52) Txt(g, Num(r.ProfLen[e], "0") + " м", fSmall, p.Muted, new RectangleF(x1, yGrade + 20, x2 - x1, 16), StringAlignment.Center);
            }
            if (cur >= 0)
            {
                // the three boxes: centred on their elements when wide enough, otherwise laid side by side around the head
                float xh = X(head), bw = 74, bh = gradeH - 4;
                var slots = new[] { cur - 1, cur, cur + 1 };
                var tags = new[] { "пред.", "тек.", "след." };
                var centres = new float[3];
                for (int s = 0; s < 3; s++)
                {
                    int e = slots[s];
                    if (e < 0 || e >= r.ProfX.Length) { centres[s] = float.NaN; continue; }
                    float x1 = X(Math.Max(start, r.ProfX[e])), x2 = X(Math.Min(end, r.ProfX[e] + r.ProfLen[e]));
                    centres[s] = (x1 + x2) / 2;
                }
                // keep them apart and in order
                if (!float.IsNaN(centres[1])) centres[1] = Math.Max(left + bw * 1.5f, Math.Min(right - bw * 1.5f, centres[1]));
                if (!float.IsNaN(centres[0])) centres[0] = Math.Min(centres[0], (float.IsNaN(centres[1]) ? xh : centres[1]) - bw - 4);
                if (!float.IsNaN(centres[2])) centres[2] = Math.Max(centres[2], (float.IsNaN(centres[1]) ? xh : centres[1]) + bw + 4);
                for (int s = 0; s < 3; s++)
                {
                    int e = slots[s];
                    if (float.IsNaN(centres[s]) || e < 0 || e >= r.ProfX.Length) continue;
                    float cx = Math.Max(left + bw / 2, Math.Min(right - bw / 2, centres[s]));
                    var rr = new RectangleF(cx - bw / 2, yGrade + 2, bw, bh);
                    Color col = GradeColor(r.ProfSlope[e]);
                    Card(g, rr, s == 1 ? Color.FromArgb(Light ? 50 : 60, col) : p.Card, col, 5);
                    Txt(g, Grade(r.ProfSlope[e]) + " ‰", s == 1 ? fGradeBig : fGrade, col, new RectangleF(rr.X, rr.Y + 1, rr.Width, 20), StringAlignment.Center);
                    Txt(g, tags[s] + " · " + Num(r.ProfLen[e], "0") + " м", fSmall, p.Muted, new RectangleF(rr.X - 6, rr.Y + 19, rr.Width + 12, 14), StringAlignment.Center);
                    // leader to the element when the box was moved off it
                    float ex1 = X(Math.Max(start, r.ProfX[e])), ex2 = X(Math.Min(end, r.ProfX[e] + r.ProfLen[e]));
                    float target = Math.Max(ex1, Math.Min(ex2, cx));
                    if (Math.Abs(target - cx) > 3) Line(g, col, 1f, cx, yGrade + 2, target, yProf + profH - 2, DashStyle.Dot);
                    if (s == 1) using (var br = new SolidBrush(Color.FromArgb(Light ? 34 : 40, col))) g.FillRectangle(br, ex1, yProf, Math.Max(2, ex2 - ex1), profH);
                }
            }

            // curves
            Txt(g, "R, м", fSmall, p.Muted, new RectangleF(box.X + 8, yCurve, 44, curveH), StringAlignment.Near);
            Line(g, p.Grid, 0.8f, left, yCurve + curveH - 4, right, yCurve + curveH - 4);
            float lastC = -1000;
            for (int k = First(r.CurveX, start - 3000); k < r.CurveX.Length && r.CurveX[k] <= end; k++)
            {
                double a = Math.Max(start, r.CurveX[k]), b = Math.Min(end, r.CurveX[k] + r.CurveLen[k]);
                if (b <= a) continue;
                float x1 = X(a), x2 = X(b);
                using (var pen = new Pen(p.Down, 1.4f)) g.DrawLines(pen, new[] { new PointF(x1, yCurve + curveH - 4), new PointF(x1, yCurve + curveH - 10), new PointF(x2, yCurve + curveH - 10), new PointF(x2, yCurve + curveH - 4) });
                float mid = (x1 + x2) / 2;
                if (mid - lastC > 44) { Txt(g, Num(Math.Abs(r.CurveR[k]), "0"), fSmall, p.Down, new RectangleF(mid - 24, yCurve - 2, 48, 13), StringAlignment.Center); lastC = mid; }
            }

            // signs
            Txt(g, "знаки", fSmall, p.Muted, new RectangleF(box.X + 8, ySigns + 6, 44, 14), StringAlignment.Near);
            float lastS = -1000; int row = 0;
            for (int k = First(r.SignX, start); k < r.SignX.Length && r.SignX[k] <= end; k++)
            {
                float x = X(r.SignX[k]);
                row = x - lastS < 14 ? 1 - row : 0; lastS = x;
                int t = r.SignType[k];
                float y = ySigns + 2 + row * 12;
                if (t >= 0 && t < Icons.Signs.Length) g.DrawImage(Icons.Signs[t], x - 8, y, 16, 16);
                else
                {
                    Color c = t == Route.SignBrakeStart || t == Route.SignBrakeEnd ? p.Blok : Route.IsDangerStart(t) || Route.IsDangerEnd(t) ? p.Limit : p.Muted;
                    using (var pen = new Pen(c, 1.3f)) g.DrawRectangle(pen, x - 6, y + 1, 12, 12);
                    string tag = t == Route.SignBrakeStart ? "НТ" : t == Route.SignBrakeEnd ? "КТ" : t == Route.SignWhistle ? "С" : Route.IsDangerStart(t) ? "!" : "";
                    if (tag.Length > 0) Txt(g, tag, fSmall, c, new RectangleF(x - 10, y, 20, 14), StringAlignment.Center);
                }
            }

            // legend
            float ly = box.Bottom - 8;
            DrawLegend(g, left, ly);
        }

        void DrawLegend(Graphics g, float x, float y)
        {
            Line(g, p.Accent, 2.2f, x, y - 4, x + 18, y - 4); Txt(g, "скорость поезда", fSmall, p.Muted, new RectangleF(x + 22, y - 11, 110, 14), StringAlignment.Near);
            Line(g, p.Blok, 1.6f, x + 130, y - 4, x + 148, y - 4, DashStyle.Dash); Txt(g, "Vдоп", fSmall, p.Muted, new RectangleF(x + 152, y - 11, 80, 14), StringAlignment.Near);
            Line(g, p.Limit, 2f, x + 236, y - 4, x + 254, y - 4); Txt(g, "ограничения карты", fSmall, p.Muted, new RectangleF(x + 258, y - 11, 120, 14), StringAlignment.Near);
            using (var br = new SolidBrush(p.Band)) g.FillRectangle(br, x + 384, y - 10, 18, 12); Txt(g, "поезд", fSmall, p.Muted, new RectangleF(x + 406, y - 11, 50, 14), StringAlignment.Near);
            Txt(g, "подъём", fSmall, p.Up, new RectangleF(x + 460, y - 11, 60, 14), StringAlignment.Near);
            Txt(g, "спуск", fSmall, p.Down, new RectangleF(x + 512, y - 11, 60, 14), StringAlignment.Near);
        }

        void DrawTrace(Graphics g, Func<double, float> X, Func<double, float> VY, double start, double end, int field, Color c, float width, DashStyle dash)
        {
            if (Trace.Count < 2) return;
            using (var pen = new Pen(c, width) { DashStyle = dash })
            {
                double[] old = null;
                int i0 = 0, lo = 0, hi = Trace.Count;
                while (lo < hi) { int m2 = (lo + hi) / 2; if (Trace[m2][0] < start) lo = m2 + 1; else hi = m2; }
                i0 = Math.Max(0, lo - 1);
                for (int i = i0; i < Trace.Count; i++)
                {
                    var t = Trace[i];
                    if (t[0] > end) { if (old != null && old[0] <= end) { } break; }
                    if (field == 2 && t[2] <= 0) { old = null; continue; }
                    if (old != null && t[0] - old[0] < 400 && t[0] >= old[0])
                    {
                        if (field == 2) { g.DrawLine(pen, X(old[0]), VY(old[2]), X(t[0]), VY(old[2])); if (Math.Abs(t[2] - old[2]) > 0.5) g.DrawLine(pen, X(t[0]), VY(old[2]), X(t[0]), VY(t[2])); }
                        else g.DrawLine(pen, X(old[0]), VY(old[1]), X(t[0]), VY(t[1]));
                    }
                    old = t;
                }
            }
        }

        Color AspectColor(int a)
        {
            switch (a)
            {
                case 0: case 6: case 11: case 18: return Color.FromArgb(40, 200, 90);
                case 1: case 14: case 15: return Color.FromArgb(230, 40, 40);
                case 2: case 8: case 10: case 12: case 13: case 16: case 17: case 7: case 9: return Color.FromArgb(240, 200, 30);
                case 4: return Color.White;
                default: return p.Grid;
            }
        }

        // ---------- side panel
        void DrawHud(Graphics g, RectangleF box, Route r, RegimeLive l)
        {
            float x = box.X + 14, w = box.Width - 28, y = box.Y + 10;
            if (l == null || double.IsNaN(l.Head)) { Txt(g, "Ждём данные поездки", fHead, p.Muted, new RectangleF(x, y, w, 22), StringAlignment.Near); return; }
            double head = l.Head;
            bool overSpeed = l.Allowed > 0 && l.V > l.Allowed + 1;
            Txt(g, Num(l.V, "0"), fMetric, overSpeed ? p.Warn : p.Accent, new RectangleF(x, y, w * 0.55f, 46), StringAlignment.Near);
            Txt(g, "км/ч", fBody, p.Muted, new RectangleF(x + w * 0.4f, y + 18, 50, 22), StringAlignment.Near);
            var pill = new RectangleF(x + w * 0.56f, y + 8, w * 0.44f, 30);
            Card(g, pill, Color.FromArgb(Light ? 26 : 40, p.Limit), p.Limit, 6);
            Txt(g, "Vдоп " + (l.Allowed > 0 ? Num(l.Allowed, "0") : "—") + (l.AllowedByBlok ? " БЛОК" : " карта"), fHead, p.Limit, pill, StringAlignment.Center);
            y += 50;
            Txt(g, r.KmPk(head), fHead, p.Text, new RectangleF(x, y, w, 20), StringAlignment.Near); y += 24;

            // gradients
            Line(g, p.Grid, 1f, x, y, x + w, y); y += 6;
            Txt(g, "Уклоны", fHead, p.Text, new RectangleF(x, y, w, 20), StringAlignment.Near); y += 22;
            int cur = r.ElementAt(head);
            GradeRow(g, r, cur - 1, "◀ предыдущий", x, ref y, w, cur - 1 >= 0 ? "пройден " + Num(Math.Max(0, head - (r.ProfX[cur - 1] + r.ProfLen[cur - 1])), "0") + " м назад" : "");
            if (cur >= 0) GradeRow(g, r, cur, "● текущий", x, ref y, w, "до конца " + Num(Math.Max(0, r.ProfX[cur] + r.ProfLen[cur] - head), "0") + " м");
            GradeRow(g, r, cur + 1, "▶ следующий", x, ref y, w, cur + 1 < r.ProfX.Length && cur >= 0 ? "через " + Num(Math.Max(0, r.ProfX[cur + 1] - head), "0") + " м" : "");
            double mean = r.MeanSlope(head, l.TrainLen);
            Txt(g, "под поездом в среднем: " + Grade(mean) + " ‰", fBody, GradeColor(mean), new RectangleF(x, y, w, 18), StringAlignment.Near); y += 22;
            int climb = r.LimitingClimbAt(head);
            if (climb >= 0) { Txt(g, "лимитирующий подъём " + Grade(r.LimitSlope[climb]) + " ‰, ещё " + Num(r.LimitX[climb] + r.LimitLen[climb] - head, "0") + " м", fSmall, p.Up, new RectangleF(x, y, w, 16), StringAlignment.Near); y += 18; }

            // ahead
            Line(g, p.Grid, 1f, x, y, x + w, y); y += 6;
            int nl = r.NextLight(head);
            if (nl >= 0)
            {
                int a = l.Aspects != null && nl < l.Aspects.Length ? l.Aspects[nl] : -1;
                Txt(g, "Светофор " + r.LightName[nl] + " через " + Num(r.LightX[nl] - head, "0") + " м", fBody, p.Text, new RectangleF(x, y, w, 18), StringAlignment.Near); y += 18;
                Txt(g, a >= 0 ? Route.AspectName(a) : "показание неизвестно", fSmall, Route.IsProhibiting(a) ? p.Warn : AspectColor(a) == p.Grid ? p.Muted : AspectColor(a), new RectangleF(x + 10, y, w - 10, 16), StringAlignment.Near); y += 18;
            }
            int ns = r.NextIndex(r.StationX, head);
            if (ns >= 0) { Txt(g, "Станция " + r.StationName[ns] + " через " + Num((r.StationX[ns] - head) / 1000.0, "0.0") + " км", fBody, p.Text, new RectangleF(x, y, w, 18), StringAlignment.Near); y += 20; }
            for (int k = First(r.SignX, head + 0.1); k < r.SignX.Length && r.SignX[k] < head + 5000; k++)
            {
                int t = r.SignType[k];
                if (t != Route.SignBrakeStart && !Route.IsDangerStart(t)) continue;
                Txt(g, Route.SignName(t) + " через " + Num(r.SignX[k] - head, "0") + " м", fBody, t == Route.SignBrakeStart ? p.Blok : p.Limit, new RectangleF(x, y, w, 18), StringAlignment.Near); y += 20;
                break;
            }

            // brakes and traction
            Line(g, p.Grid, 1f, x, y, x + w, y); y += 6;
            Row(g, "ТМ / УР, кгс/см²", Num(l.Tm * Traction.KgfPerMPa, "0.00") + " / " + Num(l.Ur * Traction.KgfPerMPa, "0.00"), x, ref y, w);
            Row(g, "ТЦ лок. / ПМ, кгс/см²", Num(l.Tc * Traction.KgfPerMPa, "0.00") + " / " + Num(l.Pm * Traction.KgfPerMPa, "0.00"), x, ref y, w);
            Row(g, "Кран 395 / 254", CraneName(l.Crane) + " / " + l.Kvt, x, ref y, w);
            Row(g, "Позиция / тяга", l.Km + " / " + Num(l.TractionKn, "0") + " кН", x, ref y, w);

            // journal
            Line(g, p.Grid, 1f, x, y, x + w, y); y += 6;
            Txt(g, "Нарушения: " + l.JournalCount + " · баллов " + l.Points, fHead, l.JournalCount > 0 ? p.Warn : p.Text, new RectangleF(x, y, w, 20), StringAlignment.Near); y += 22;
            var j = l.Journal;
            for (int i = j.Length - 1; i >= 0 && y < box.Bottom - 30; i--)
            {
                var e = j[i];
                Txt(g, e.Clock + "  " + e.Code + (e.Gross ? " грубое" : ""), fSmall, e.Gross ? p.Warn : p.Muted, new RectangleF(x, y, w, 14), StringAlignment.Near); y += 14;
                using (var b = new SolidBrush(p.Text)) using (var sf = new StringFormat { Trimming = StringTrimming.EllipsisWord })
                {
                    var rr = new RectangleF(x, y, w, Math.Min(32, box.Bottom - 8 - y));
                    g.DrawString(e.Title, fSmall, b, rr, sf); y += 32;
                }
            }
        }

        void GradeRow(Graphics g, Route r, int e, string tag, float x, ref float y, float w, string note)
        {
            if (e < 0 || e >= r.ProfX.Length) { Txt(g, tag + ": —", fBody, p.Muted, new RectangleF(x, y, w, 18), StringAlignment.Near); y += 22; return; }
            Color c = GradeColor(r.ProfSlope[e]);
            Txt(g, tag, fSmall, p.Muted, new RectangleF(x, y, w * 0.5f, 16), StringAlignment.Near);
            Txt(g, Grade(r.ProfSlope[e]) + " ‰", tag.StartsWith("●") ? fGradeBig : fGrade, c, new RectangleF(x + w * 0.45f, y - 2, w * 0.55f, 20), StringAlignment.Far);
            y += 17;
            Txt(g, Num(r.ProfLen[e], "0") + " м · " + note, fSmall, p.Muted, new RectangleF(x + 10, y, w - 10, 15), StringAlignment.Near);
            y += 19;
        }
        void Row(Graphics g, string label, string value, float x, ref float y, float w)
        {
            Txt(g, label, fSmall, p.Muted, new RectangleF(x, y, w * 0.55f, 18), StringAlignment.Near);
            Txt(g, value, fBody, p.Text, new RectangleF(x + w * 0.45f, y, w * 0.55f, 18), StringAlignment.Far);
            y += 20;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) { fTitle.Dispose(); fHead.Dispose(); fBody.Dispose(); fSmall.Dispose(); fGrade.Dispose(); fGradeBig.Dispose(); fMetric.Dispose(); }
            base.Dispose(disposing);
        }
    }
}
