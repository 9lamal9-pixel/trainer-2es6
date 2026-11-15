// Route of a trip: an RTS scenario (RTS/scenaries/<file>.json, the format of the present instructor.exe) read without
// changing it. Axis: metres from the start of the profile, the profile coordinates are cumulative (as
// RailRoadDataUtilities.LoadFromJsonConfig makes them). Slopes in per mille, positive uphill in the direction of travel.
// The instructor may change signal aspects and add signs (speed limits) during the trip: Aspects and AddSign.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Web.Script.Serialization;

namespace Trainer2TE116
{
    public sealed class Sign { public double X; public int Type, Limit; public bool Added; }
    public sealed class Light { public int Index; public double X; public string Name = ""; public int Aspect; public int[] Combinations = new int[0]; }
    // obstacles of the scenario ("prep": cars, rocks, threes, vagons, liters), the instructor makes them active
    public sealed class Obstacle { public int Id = -1; public double X; public string Kind = ""; public bool Active; public string Name { get { return Kind == "cars" ? "Автомобиль на переезде" : Kind == "rocks" ? "Обвал камней" : Kind == "threes" ? "Упавшее дерево" : Kind == "vagons" ? "Вагоны на пути" : Kind == "liters" ? "Посторонние предметы" : "Препятствие"; } } }

    public sealed class Route
    {
        public string Name = "", File = "";
        public double[] ProfX = new double[0], ProfLen = new double[0], ProfSlope = new double[0], ProfH = new double[0];
        public double[] CurveX = new double[0], CurveLen = new double[0], CurveR = new double[0];
        public double[] StationX = new double[0]; public string[] StationName = new string[0];
        public List<Light> Lights = new List<Light>();
        public List<Sign> Signs = new List<Sign>();
        public List<Obstacle> Obstacles = new List<Obstacle>();
        public double[] PiketX = new double[0]; public int[] PiketKm = new int[0], PiketPk = new int[0];
        public double[] LimitX = new double[0], LimitLen = new double[0], LimitSlope = new double[0];
        public double Length, StartX, RulingGrade, DefaultLimitKmh = 80;
        public bool Day = true, Rain, Snow, Fog;

        public const int SignWhistle = 6, SignBrakeStart = 17, SignBrakeEnd = 18;
        public static bool IsDangerStart(int t) { return t == 7 || t == 8; }
        public static bool IsDangerEnd(int t) { return t == 3 || t == 9; }

        // ---- loading
        public static Route LoadScenario(string path, double minLimitLen = 800, double limitBand = 1.0)
        {
            var js = new JavaScriptSerializer { MaxJsonLength = 100000000 };
            var d = (Dictionary<string, object>)js.DeserializeObject(System.IO.File.ReadAllText(path));
            Func<object, string, double> num = (o, k) => { var m = o as Dictionary<string, object>; return m != null && m.ContainsKey(k) && m[k] != null ? Convert.ToDouble(m[k], CultureInfo.InvariantCulture) : 0; };
            Func<object, string, string> str = (o, k) => { var m = o as Dictionary<string, object>; return m != null && m.ContainsKey(k) && m[k] != null ? m[k].ToString() : ""; };
            Func<string, object[]> list = k => d.ContainsKey(k) && d[k] is object[] ? (object[])d[k] : new object[0];
            var r = new Route { File = path };
            var prof = list("profil");
            int n = prof.Length;
            r.ProfX = new double[n]; r.ProfLen = new double[n]; r.ProfSlope = new double[n]; r.ProfH = new double[n];
            double x = 0, h = 0;
            for (int i = 0; i < n; i++)
            {
                r.ProfX[i] = x; r.ProfLen[i] = Math.Max(0, num(prof[i], "dist")); r.ProfSlope[i] = num(prof[i], "uklon"); r.ProfH[i] = h;
                h += r.ProfLen[i] * r.ProfSlope[i] / 1000.0; x += r.ProfLen[i];
            }
            r.Length = x;
            var plan = list("plan").Select(e => new[] { num(e, "coord"), num(e, "length"), num(e, "radius") }).OrderBy(a => a[0]).ToList();
            r.CurveX = plan.Select(a => a[0]).ToArray(); r.CurveLen = plan.Select(a => a[1]).ToArray(); r.CurveR = plan.Select(a => a[2]).ToArray();
            var st = list("stations").Select(e => new KeyValuePair<double, string>(num(e, "coord"), str(e, "name").Trim())).OrderBy(a => a.Key).ToList();
            r.StationX = st.Select(a => a.Key).ToArray(); r.StationName = st.Select(a => a.Value).ToArray();
            foreach (var e in list("lights"))
            {
                var l = new Light { Index = (int)num(e, "index"), X = num(e, "coord"), Name = str(e, "name"), Aspect = (int)num(e, "light") };
                var m = e as Dictionary<string, object>;
                if (m != null && m.ContainsKey("lights_combinations") && m["lights_combinations"] is object[])
                    l.Combinations = ((object[])m["lights_combinations"]).Select(o => Convert.ToInt32(o, CultureInfo.InvariantCulture)).ToArray();
                r.Lights.Add(l);
            }
            foreach (var e in list("signals")) r.Signs.Add(new Sign { X = num(e, "coord"), Type = (int)num(e, "type"), Limit = (int)num(e, "speed_limit") });
            r.Signs.Sort((a, b) => a.X.CompareTo(b.X));
            var pk = new List<int[]>();
            var pks = list("pikets");
            for (int i = 0; i < pks.Length; i++) { int c = (int)num(pks[i], "coord"); if (i == 0 || c > 0) pk.Add(new[] { c, (int)num(pks[i], "km"), (int)num(pks[i], "piket") }); }
            pk.Sort((a, b) => a[0].CompareTo(b[0]));
            r.PiketX = pk.Select(a => (double)a[0]).ToArray(); r.PiketKm = pk.Select(a => a[1]).ToArray(); r.PiketPk = pk.Select(a => a[2]).ToArray();
            var prep = d.ContainsKey("prep") ? d["prep"] as Dictionary<string, object> : null;
            if (prep != null)
                foreach (var kv in prep)
                    if (kv.Value is object[])
                        foreach (var e in (object[])kv.Value) r.Obstacles.Add(new Obstacle { Id = (int)num(e, "id"), Kind = kv.Key, X = num(e, "coord"), Active = str(e, "actively") == "True" });
            r.Obstacles.Sort((a, b) => a.X.CompareTo(b.X));
            r.StartX = num(d, "start_coord");
            r.Day = str(d, "day") != "False"; r.Rain = str(d, "raining") == "True"; r.Snow = str(d, "snow") == "True"; r.Fog = str(d, "fog") == "True";
            r.Name = r.StationName.Length >= 2 ? r.StationName[0] + " — " + r.StationName[r.StationName.Length - 1] : Path.GetFileNameWithoutExtension(path);
            r.FindLimitingClimbs(minLimitLen, limitBand);
            return r;
        }

        // Ruling grade: the steepest mean slope of a stretch of consecutive climbs at least minLen long. Limiting climbs: the
        // elements covered by stretches not flatter than the ruling grade minus the band, merged into runs.
        public void FindLimitingClimbs(double minLen, double band)
        {
            int n = ProfX.Length; double ruling = 0;
            for (int i = 0; i < n; i++)
            {
                double len = 0, rise = 0;
                for (int j = i; j < n && ProfSlope[j] > 0; j++) { len += ProfLen[j]; rise += ProfLen[j] * ProfSlope[j] / 1000; if (len >= minLen) { ruling = Math.Max(ruling, rise / len * 1000); break; } }
            }
            RulingGrade = ruling;
            var mark = new bool[n];
            if (ruling >= 2)
                for (int i = 0; i < n; i++)
                {
                    double len = 0, rise = 0;
                    for (int j = i; j < n && ProfSlope[j] > 0; j++) { len += ProfLen[j]; rise += ProfLen[j] * ProfSlope[j] / 1000; if (len >= minLen) { if (rise / len * 1000 >= ruling - band - 1e-9) for (int k = i; k <= j; k++) mark[k] = true; break; } }
                }
            var lx = new List<double>(); var ll = new List<double>(); var ls = new List<double>();
            for (int i = 0; i < n; )
            {
                if (!mark[i]) { i++; continue; }
                int j = i; double len = 0, rise = 0;
                while (j < n && mark[j]) { len += ProfLen[j]; rise += ProfLen[j] * ProfSlope[j] / 1000; j++; }
                lx.Add(ProfX[i]); ll.Add(len); ls.Add(rise / len * 1000); i = j;
            }
            LimitX = lx.ToArray(); LimitLen = ll.ToArray(); LimitSlope = ls.ToArray();
        }

        // ---- arrays for drawing (copies, safe to use while the instructor changes aspects or adds signs)
        double[] lightX, signX; string[] lightName; int[] signType; int signsVersion = -1, signsSeen;
        public double[] LightX { get { if (lightX == null || lightX.Length != Lights.Count) { lightX = Lights.ConvertAll(l => l.X).ToArray(); lightName = Lights.ConvertAll(l => l.Name).ToArray(); } return lightX; } }
        public string[] LightName { get { var x = LightX; return lightName; } }
        public int[] Aspects { get { return Lights.ConvertAll(l => l.Aspect).ToArray(); } }
        void SignArrays() { lock (Signs) if (signsVersion != signsSeen || signX == null) { signX = Signs.ConvertAll(s => s.X).ToArray(); signType = Signs.ConvertAll(s => s.Type).ToArray(); signsVersion = signsSeen; } }
        public double[] SignX { get { SignArrays(); return signX; } }
        public int[] SignType { get { SignArrays(); return signType; } }
        public int NextIndex(double[] xs, double x) { int k = Floor(xs, x); return k + 1 < xs.Length ? k + 1 : -1; }

        // ---- queries
        static int Floor(double[] xs, double x)
        {
            int lo = 0, hi = xs.Length - 1;
            if (hi < 0 || x < xs[0]) return -1;
            while (lo < hi) { int mid = (lo + hi + 1) / 2; if (xs[mid] <= x) lo = mid; else hi = mid - 1; }
            return lo;
        }
        public int ElementAt(double x) { int k = Floor(ProfX, x); return k < 0 ? (ProfX.Length > 0 ? 0 : -1) : k; }
        public double SlopeAt(double x) { if (x < 0 || x > Length) return 0; int k = ElementAt(x); return k < 0 ? 0 : ProfSlope[k]; }
        public double HeightAt(double x)
        {
            int k = ElementAt(x); if (k < 0) return 0;
            double d = Math.Max(0, Math.Min(ProfLen[k], x - ProfX[k]));
            return ProfH[k] + d * ProfSlope[k] / 1000.0;
        }
        public double MeanSlope(double head, double length) { return length < 1 ? SlopeAt(head) : (HeightAt(head) - HeightAt(head - length)) / length * 1000.0; }
        public double CurveRadiusAt(double x) { int k = Floor(CurveX, x); return k >= 0 && x < CurveX[k] + CurveLen[k] ? CurveR[k] : 0; }
        public string KmPk(double x)
        {
            int k = Floor(PiketX, x);
            if (k < 0) return (x / 1000.0).ToString("0.000", CultureInfo.InvariantCulture) + " км";
            return string.Format(CultureInfo.InvariantCulture, "{0} км {1} пк +{2:0} м", PiketKm[k], PiketPk[k], x - PiketX[k]);
        }
        public int KmAt(double x) { int k = Floor(PiketX, x); return k < 0 ? (int)(x / 1000) : PiketKm[k]; }
        public int PkAt(double x) { int k = Floor(PiketX, x); return k < 0 ? 0 : PiketPk[k]; }
        public int NextStation(double x) { int k = Floor(StationX, x); return k + 1 < StationX.Length ? k + 1 : -1; }
        public int NextLight(double x) { int best = -1; for (int i = 0; i < Lights.Count; i++) if (Lights[i].X > x && (best < 0 || Lights[i].X < Lights[best].X)) best = i; return best; }
        public int LimitingClimbAt(double x) { for (int i = 0; i < LimitX.Length; i++) if (x >= LimitX[i] && x < LimitX[i] + LimitLen[i]) return i; return -1; }
        // speed limit of the map at x: 80 km/h (KLUB electronic map, as BLOKDataReceiveController), inside a dangerous place
        // the limit of its start sign when one is given
        public int StaticLimitAt(double x)
        {
            int limit = (int)DefaultLimitKmh;
            lock (Signs) foreach (var s in Signs)
            {
                if (s.X > x) break;
                if (IsDangerStart(s.Type) && s.Limit > 0) limit = s.Limit; else if (IsDangerEnd(s.Type)) limit = (int)DefaultLimitKmh;
            }
            return limit;
        }
        public void AddSign(Sign s) { lock (Signs) { Signs.Add(s); Signs.Sort((a, b) => a.X.CompareTo(b.X)); signsSeen++; } }
        // warnings for form DU-61: the dangerous places with their limits ahead of x
        public List<KeyValuePair<Sign, Sign>> Warnings(double from)
        {
            var res = new List<KeyValuePair<Sign, Sign>>();
            Sign open = null;
            foreach (var s in Signs)
            {
                if (IsDangerStart(s.Type) && s.Limit > 0) open = s;
                else if (IsDangerEnd(s.Type) && open != null) { if (s.X >= from) res.Add(new KeyValuePair<Sign, Sign>(open, s)); open = null; }
            }
            if (open != null) res.Add(new KeyValuePair<Sign, Sign>(open, null));
            return res;
        }

        public static string AspectName(int code)
        {
            switch (code)
            {
                case 0: return "Зелёный"; case 1: return "Красный"; case 2: return "Жёлтый"; case 4: return "Белый";
                case 6: return "Зелёный миг."; case 7: return "Зелёный миг. и жёлтый"; case 8: return "Жёлтый миг."; case 9: return "Жёлтый миг. и белый";
                case 10: return "2 жёлтых"; case 11: return "2 зелёных"; case 12: return "3 жёлтых"; case 13: return "2 жёлтых, верхний миг.";
                case 14: return "Красный и белый"; case 15: return "Красный и белый миг."; case 16: return "Жёлтый и зелёный"; case 17: return "Жёлтый и белый";
                case 18: return "Зелёный и белый"; case 19: return "Не горит"; case 20: return "Все горят";
                default: return "Непонятный (" + code + ")";
            }
        }
        public static bool IsProhibiting(int code) { return code == 1 || code == 19; }
        // allowed speed by the aspect of the next signal (simplified ALSN/KLUB for a freight train): green 80, yellow 60,
        // two yellows 50 (turnout), red: stop before it (handled by the braking curve of the driver / KLUB)
        public static int AspectLimit(int code)
        {
            switch (code)
            {
                case 1: case 19: return 0;
                case 2: case 8: case 9: case 17: return 60;
                case 10: case 12: case 13: return 50;
                case 14: case 15: return 20;
                default: return 80;
            }
        }
        public static string SignName(int t)
        {
            switch (t)
            {
                case 0: return "Жёлтый диск"; case 1: return "Зелёный диск"; case 2: return "Жёлтый щит"; case 4: return "Зелёный щит";
                case 5: return "Красный щит"; case 6: return "«С» — подача свистка"; case 7: case 8: return "Начало опасного места";
                case 3: case 9: return "Конец опасного места"; case 14: return "Начало карстоопасного участка"; case 15: return "Конец карстоопасного участка";
                case 16: return "Красный диск"; case 17: return "«НТ» — начало торможения"; case 18: return "«КТ» — конец торможения";
                default: return "Знак " + t;
            }
        }
    }
}
