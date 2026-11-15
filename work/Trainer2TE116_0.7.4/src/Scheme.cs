// The electric scheme of the locomotive as a working model: a netlist read from the text of SchemeData.cs and solved
// every frame. Nodes are wires (by their numbers on the scheme), elements are contacts, switches, sensors, coils, lamps,
// diodes and plain wires between two numbered nodes. The same text gives the picture of the tab "Схема" (one rung of the
// text is one horizontal circuit from the plus to the minus), so the drawing cannot differ from what is computed.
//
// Text of a rung:  Name: node element node element ... node
//   1159  +АБ  -  ~44      node: wire number, source (battery, starter-generator), common minus, wire between the sections
//   [РУ9] [/РУ9]           closing / opening contact of the apparatus;  [РВП1+] delay on closing, [РВ3-] delay on opening
//   <ТН1> </ОМН>           contact of a control (toggle, button, breaker, contact of the controller)
//   {РДМ3} {/ТРВ1}         contact of a sensor (pressure, temperature, current, speed)
//   (КМН) (*ЛДМ) (=ТН)     coil, lamp, motor or another load
//   >|  |<  %СПК           diode along / against the rung, resistor or fuse
//   [[ a || b ]]           parallel branches
// Two nodes in a row are a wire that can be broken. "$" in a name is the number of the section.
// Scopes: "@cab" - the rung exists once, in the leading section; "@sec" - in every section. Names of apparatus and
// inputs of a "@sec" rung belong to the section (key "РУ9@1"), of a "@cab" rung to the cab (key "ТН1"); nodes of both
// belong to the section, except the wires between the sections ("~...") and the minus.
// Solving: reach from the live sources through the closed elements (a diode one way), reach from the minus, a load is
// on when one end is fed and the other has the minus; repeated until the relays settle; then the time relays count.
using System;
using System.Collections.Generic;
using System.Linq;

namespace Trainer2TE116
{
    public enum ElKind { Wire, Contact, Switch, Sensor, Coil, Lamp, Motor, Diode, Res }

    public sealed class App
    {
        public string Key, Name; public int Sec;
        public bool On, Fed;                 // state of the armature, voltage on a coil
        public double Delay, TOn, TOff = 1e9, Pick, FedT;   // Pick: travel time of the armature (pneumatic contactors)
        public int Force;                    // 0 by the coil, 2 stuck on, 3 stuck off
        public readonly List<El> Coils = new List<El>(), Contacts = new List<El>();
        public bool DOn { get { return On && TOn >= Delay; } }         // closes with a delay, opens at once
        public bool DOff { get { return On || TOff < Delay; } }        // closes at once, opens with a delay
    }

    public sealed class El
    {
        public int Id, A = -1, B = -1, Sec, In = -1;
        public ElKind Kind; public string Name, Key; public bool Nc, Rev; public int Delay;
        public int Ovr;                      // 0 as it is, 1 open (broken), 2 closed (jumper); for a load 1 broken, 2 stuck on, 3 stuck off
        public bool Closed, On; public App App; public Rung Rung;
        public float X, Y, W;
        public bool ExternalOpen; // legacy selector or burnt-lamp control, independent of the instructor override
        public bool IsLoad { get { return Kind == ElKind.Coil || Kind == ElKind.Lamp || Kind == ElKind.Motor; } }
        public string Title
        {
            get
            {
                switch (Kind)
                {
                    case ElKind.Wire: return "провод";
                    case ElKind.Contact: return (Nc ? "размыкающий" : "замыкающий") + " контакт " + Name + (Delay == 1 ? ", с выдержкой на замыкание" : Delay == 2 ? ", с выдержкой на размыкание" : "");
                    case ElKind.Switch: return "контакт " + Name + (Nc ? " (размыкающий)" : "");
                    case ElKind.Sensor: return "датчик " + Name + (Nc ? " (размыкающий контакт)" : "");
                    case ElKind.Coil: return "катушка " + Name;
                    case ElKind.Lamp: return "лампа " + Name;
                    case ElKind.Motor: return Name;
                    case ElKind.Diode: return "диод" + (Name.Length > 0 ? " " + Name : "");
                    default: return Name;
                }
            }
        }
    }

    public sealed class Circuit
    {
        public El Load; public int Color;
        public readonly List<int> Plus = new List<int>(), Minus = new List<int>();   // nodes from the load to the source and to the minus
        public readonly HashSet<int> Els = new HashSet<int>();
    }
    public sealed class CircuitMap
    {
        public const int Colors = 8, SharedPlus = -1, SharedMinus = -2;
        public readonly List<Circuit> Circuits = new List<Circuit>();
        public readonly Dictionary<int, int> Node = new Dictionary<int, int>(), El = new Dictionary<int, int>();   // colour of a circuit or Shared*
    }

    public struct SchemeSeg { public float X1, Y1, X2, Y2; public int Node; }
    public struct Lab { public float X, Y; public string Text; public int Node; }

    public sealed class Rung
    {
        public string Name, Src = ""; public int Sheet, Sec; public bool Cab; public float Y, H, W;
        public readonly List<El> Els = new List<El>(); public readonly List<SchemeSeg> Segs = new List<SchemeSeg>(); public readonly List<Lab> Labs = new List<Lab>();
    }

    public sealed class Scheme
    {
        public const float NameW = 3.2f;
        public readonly List<string> Sheets = new List<string>();
        public readonly List<Rung> Rungs = new List<Rung>();
        public readonly List<El> Els = new List<El>();
        public readonly List<string> Nodes = new List<string>();
        public readonly Dictionary<string, App> Apps = new Dictionary<string, App>();
        public readonly List<string> Errors = new List<string>();
        readonly Dictionary<string, int> nodeIx = new Dictionary<string, int>(), inIx = new Dictionary<string, int>();
        readonly List<string> inNames = new List<string>(); bool[] inVal = new bool[0];
        readonly List<int> sources = new List<int>(), sourceIn = new List<int>();
        List<int>[] adj; bool[] plus, minus; int[] parent, mparent; int[] queue;
        public int Sections = 2, Minus;
        public Action<string> Log = s => { };
        public Action<string> Trip;          // a short circuit: the key of the breaker that feeds it
        public double T;

        // ------------------------------------------------------------ building
        public Scheme(string text, int sections = 2)
        {
            Sections = sections;
            Minus = Node("-");
            Parse(text);
            adj = new List<int>[Nodes.Count];
            for (int i = 0; i < adj.Length; i++) adj[i] = new List<int>();
            foreach (var e in Els) { adj[e.A].Add(e.Id); adj[e.B].Add(e.Id); }
            plus = new bool[Nodes.Count]; minus = new bool[Nodes.Count]; parent = new int[Nodes.Count]; mparent = new int[Nodes.Count]; queue = new int[Nodes.Count];
            inVal = new bool[inNames.Count];
            foreach (int i in sourceIn) inVal[i] = true;                  // a source nobody set is live; a control nobody set is open
        }

        int Node(string name) { int i; if (!nodeIx.TryGetValue(name, out i)) { i = Nodes.Count; Nodes.Add(name); nodeIx[name] = i; } return i; }
        int Input(string key) { int i; if (!inIx.TryGetValue(key, out i)) { i = inNames.Count; inNames.Add(key); inIx[key] = i; } return i; }
        App AppOf(string key, string name, int sec) { App a; if (!Apps.TryGetValue(key, out a)) { a = new App { Key = key, Name = name, Sec = sec }; Apps[key] = a; } return a; }
        public static string Bare(string name) { int k = name.IndexOf('@'); return (k < 0 ? name : name.Substring(0, k)).TrimEnd('!'); }

        sealed class Item { public El El; public int Node = -1; public List<List<Item>> Par; public float W, H; }
        sealed class Ctx { public int Cur = -1, Sec; public bool Cab; public El Pend; public Rung R; public int Anon; }

        void Parse(string text)
        {
            bool cab = false; string src = ""; int sheet = -1; string acc = null;
            var lines = new List<string>();
            foreach (var raw in text.Replace("\r", "").Split('\n'))
            {
                if (raw.Trim().Length == 0 || raw.TrimStart().StartsWith("#")) continue;
                if (acc != null && (raw[0] == ' ' || raw[0] == '\t')) { acc += " " + raw.Trim(); continue; }
                if (acc != null) lines.Add(acc);
                acc = raw.Trim();
            }
            if (acc != null) lines.Add(acc);
            foreach (var line in lines)
            {
                if (line.StartsWith("==")) { Sheets.Add(line.Substring(2).Trim()); sheet = Sheets.Count - 1; continue; }
                if (line == "@cab") { cab = true; continue; }
                if (line == "@sec") { cab = false; continue; }
                if (line.StartsWith("!")) { src = line.Substring(1).Trim(); continue; }
                if (line.StartsWith("N ")) { int q = line.IndexOf('='); Titles[line.Substring(2, q - 2).Trim()] = line.Substring(q + 1).Trim(); continue; }
                if (line.StartsWith("T ") || line.StartsWith("P ")) { var p = line.Split(new[] { ' ' }, StringSplitOptions.RemoveEmptyEntries); (line[0] == 'T' ? delays : picks)[p[1]] = double.Parse(p[2], System.Globalization.CultureInfo.InvariantCulture); continue; }
                int c = line.IndexOf(": ");
                if (c < 0 || sheet < 0) { Errors.Add("не разобрано: " + line); continue; }
                for (int sec = 1; sec <= (cab ? 1 : Sections); sec++)
                {
                    var r = new Rung { Name = line.Substring(0, c).Replace("$", sec.ToString()), Src = src, Sheet = sheet, Sec = sec, Cab = cab };
                    try { Build(r, line.Substring(c + 2).Replace("$", sec.ToString()).Split(new[] { ' ' }, StringSplitOptions.RemoveEmptyEntries)); Rungs.Add(r); }
                    catch (Exception ex) { Errors.Add(r.Name + ": " + ex.Message); }
                }
            }
            foreach (var a in Apps.Values) { double d; if (delays.TryGetValue(a.Name, out d)) a.Delay = d; if (picks.TryGetValue(a.Name, out d)) a.Pick = d; }
            // rungs of a sheet one under another, the same for both sections
            for (int s = 0; s < Sheets.Count; s++)
                for (int sec = 1; sec <= Sections; sec++)
                {
                    float y = 1;
                    foreach (var r in Rungs) if (r.Sheet == s && (r.Cab || r.Sec == sec)) { Shift(r, y); y += r.H + 0.55f; }
                }
        }
        readonly Dictionary<string, double> delays = new Dictionary<string, double>(), picks = new Dictionary<string, double>();
        public readonly Dictionary<string, string> Titles = new Dictionary<string, string>();   // full names of the apparatus
        public string TitleOf(string name) { string t; return Titles.TryGetValue(name, out t) ? t : ""; }
        static void Shift(Rung r, float y)
        {
            float d = y - r.Y; if (d == 0) return; r.Y = y;
            foreach (var e in r.Els) e.Y += d;
            for (int i = 0; i < r.Segs.Count; i++) { var s = r.Segs[i]; s.Y1 += d; s.Y2 += d; r.Segs[i] = s; }
            for (int i = 0; i < r.Labs.Count; i++) { var l = r.Labs[i]; l.Y += d; r.Labs[i] = l; }
        }

        static bool IsNodeTok(string k) { char c = k[0]; return c != '[' && c != '<' && c != '{' && c != '(' && c != '%' && k != ">|" && k != "|<" && k != "||" && k != "]]"; }
        int NodeOf(string k, Ctx x)
        {
            if (k == "-") return Minus;
            if (k[0] == '~' || k.IndexOf('@') >= 0) return Node(k);
            int n = Node(k + "@" + x.Sec);
            if (k[0] == '+' && !sources.Contains(n)) { sources.Add(n); sourceIn.Add(Input(k.Substring(1) + "@" + x.Sec)); }
            return n;
        }
        // "X!" in a rung of a section is an apparatus or a control of the cab
        string KeyOf(string name, Ctx x) { return name.EndsWith("!") ? name.TrimEnd('!') : name.IndexOf('@') >= 0 || x.Cab ? name : name + "@" + x.Sec; }

        El NewEl(Ctx x, ElKind kind, string name)
        {
            var e = new El { Id = Els.Count, Kind = kind, Name = name, Sec = x.Sec, Rung = x.R, A = x.Cur };
            Els.Add(e); x.R.Els.Add(e); return e;
        }
        El MakeEl(string k, Ctx x)
        {
            if (k == ">|" || k == "|<") { var d = NewEl(x, ElKind.Diode, ""); d.Rev = k == "|<"; return d; }
            if (k[0] == '%') return NewEl(x, ElKind.Res, k.Substring(1));
            string s = k.Substring(1, k.Length - 2);
            if (k[0] == '(')
            {
                var kind = s[0] == '*' ? ElKind.Lamp : s[0] == '=' ? ElKind.Motor : ElKind.Coil;
                if (kind != ElKind.Coil) s = s.Substring(1);
                var e = NewEl(x, kind, Bare(s).Replace('_', ' ')); e.Key = KeyOf(s, x); e.App = AppOf(e.Key, e.Name, x.Cab ? 0 : x.Sec); e.App.Coils.Add(e); return e;
            }
            bool nc = s[0] == '/'; if (nc) s = s.Substring(1);
            if (k[0] == '[')
            {
                int delay = s.EndsWith("+") ? 1 : s.EndsWith("-") ? 2 : 0; if (delay > 0) s = s.Substring(0, s.Length - 1);
                var e = NewEl(x, ElKind.Contact, Bare(s)); e.Nc = nc; e.Delay = delay; e.Key = KeyOf(s, x);
                e.App = AppOf(e.Key, e.Name, e.Key.IndexOf('@') < 0 ? 0 : e.Key[e.Key.Length - 1] - '0'); e.App.Contacts.Add(e); return e;
            }
            var sw = NewEl(x, k[0] == '<' ? ElKind.Switch : ElKind.Sensor, Bare(s).Replace('_', ' ')); sw.Nc = nc; sw.Key = KeyOf(s, x); sw.In = Input(sw.Key); return sw;
        }

        void Build(Rung r, string[] t)
        {
            var x = new Ctx { Sec = r.Sec, Cab = r.Cab, R = r };
            int i = 0;
            var items = Series(t, ref i, x, false);
            if (x.Pend != null) throw new Exception("цепь должна кончаться узлом");
            if (i < t.Length) throw new Exception("лишнее: " + t[i]);
            float w, h; Measure(items, out w, out h);
            r.W = NameW + w + 0.6f; r.H = h; r.Y = 0;
            Place(items, NameW, 0, r);
        }
        int Anon(Ctx x) { return Node("#" + Rungs.Count + "/" + x.Sec + "/" + (x.Anon++)); }
        List<Item> Series(string[] t, ref int i, Ctx x, bool inPar)
        {
            var items = new List<Item>();
            while (i < t.Length)
            {
                string k = t[i];
                if (k == "||" || k == "]]") { if (!inPar) throw new Exception("лишняя скобка"); break; }
                i++;
                if (k == "[[")
                {
                    if (x.Pend != null) { int a = Anon(x); x.Pend.B = a; x.Pend = null; x.Cur = a; }
                    if (x.Cur < 0) throw new Exception("цепь должна начинаться с узла");
                    int s = x.Cur, depth = 1, j = i;
                    for (; j < t.Length; j++) { if (t[j] == "[[") depth++; if (t[j] == "]]" && --depth == 0) break; }
                    if (j >= t.Length) throw new Exception("нет закрывающей скобки ]]");
                    bool named = j + 1 < t.Length && IsNodeTok(t[j + 1]);
                    int e = named ? NodeOf(t[j + 1], x) : Anon(x);
                    var par = new Item { Par = new List<List<Item>>() };
                    while (true)
                    {
                        x.Cur = s; x.Pend = null;
                        var br = Series(t, ref i, x, true);
                        if (x.Pend != null) { x.Pend.B = e; x.Pend = null; }
                        else if (x.Cur != e) { var wre = NewEl(x, ElKind.Wire, ""); wre.B = e; br.Add(new Item { El = wre }); }
                        par.Par.Add(br);
                        if (t[i++] == "]]") break;
                    }
                    items.Add(par); x.Cur = e;
                    if (named) { i++; items.Add(new Item { Node = e }); }
                    continue;
                }
                if (IsNodeTok(k))
                {
                    int n = NodeOf(k, x);
                    if (x.Pend != null) { x.Pend.B = n; x.Pend = null; }
                    else if (x.Cur >= 0 && x.Cur != n) { var wre = NewEl(x, ElKind.Wire, ""); wre.B = n; items.Add(new Item { El = wre }); }
                    x.Cur = n; items.Add(new Item { Node = n });
                }
                else
                {
                    if (x.Pend != null) { int a = Anon(x); x.Pend.B = a; x.Pend = null; x.Cur = a; }
                    if (x.Cur < 0) throw new Exception("цепь должна начинаться с узла");
                    var el = MakeEl(k, x); x.Pend = el; items.Add(new Item { El = el });
                }
            }
            return items;
        }

        // ------------------------------------------------------------ the picture: units of the grid
        string LabelOf(int node) { string n = Nodes[node]; if (n[0] == '#') return ""; return n == "-" ? "−" : Bare(n); }
        void Measure(List<Item> s, out float w, out float h)
        {
            w = 0; h = 1;
            foreach (var it in s)
            {
                if (it.Par != null)
                {
                    float mw = 0, sh = 0;
                    foreach (var b in it.Par) { float bw, bh; Measure(b, out bw, out bh); mw = Math.Max(mw, bw); sh += bh; }
                    it.W = mw + 0.5f; it.H = sh;
                }
                else if (it.El != null) { it.W = it.El.Kind == ElKind.Wire ? 0.45f : Math.Max(1.25f, 0.5f + 0.2f * it.El.Name.Length); it.H = 1; }
                else { string l = LabelOf(it.Node); it.W = l.Length == 0 ? 0.15f : 0.35f + 0.17f * l.Length; it.H = 1; }
                w += it.W; h = Math.Max(h, it.H);
            }
        }
        void Place(List<Item> s, float x, float y, Rung r)
        {
            foreach (var it in s)
            {
                if (it.Par != null)
                {
                    float yy = y; int a = -1, b = -1;
                    foreach (var br in it.Par)
                    {
                        float bw, bh; Measure(br, out bw, out bh);
                        var first = FirstEl(br); var last = LastEl(br);
                        if (first != null) a = first.A; if (last != null) b = last.B;
                        r.Segs.Add(new SchemeSeg { X1 = x, Y1 = yy, X2 = x + 0.25f, Y2 = yy, Node = a });
                        Place(br, x + 0.25f, yy, r);
                        r.Segs.Add(new SchemeSeg { X1 = x + 0.25f + bw, Y1 = yy, X2 = x + it.W, Y2 = yy, Node = b });
                        yy += bh;
                    }
                    float yl = yy - LastH(it);
                    r.Segs.Add(new SchemeSeg { X1 = x, Y1 = y, X2 = x, Y2 = yl, Node = a });
                    r.Segs.Add(new SchemeSeg { X1 = x + it.W, Y1 = y, X2 = x + it.W, Y2 = yl, Node = b });
                }
                else if (it.El != null) { it.El.W = it.W; it.El.X = x + it.W / 2; it.El.Y = y; }
                else
                {
                    r.Segs.Add(new SchemeSeg { X1 = x, Y1 = y, X2 = x + it.W, Y2 = y, Node = it.Node });
                    string l = LabelOf(it.Node);
                    if (l.Length > 0) r.Labs.Add(new Lab { X = x + it.W / 2, Y = y, Text = l, Node = it.Node });
                }
                x += it.W;
            }
        }
        float LastH(Item par) { float bw, bh; Measure(par.Par[par.Par.Count - 1], out bw, out bh); return bh; }
        static El FirstEl(List<Item> s) { foreach (var it in s) { if (it.El != null) return it.El; if (it.Par != null) return FirstEl(it.Par[0]); } return null; }
        static El LastEl(List<Item> s) { for (int i = s.Count - 1; i >= 0; i--) { if (s[i].El != null) return s[i].El; if (s[i].Par != null) return LastEl(s[i].Par[0]); } return null; }
        public float WidthOf(El e) { return e.Kind == ElKind.Wire ? 0.45f : Math.Max(1.25f, 0.5f + 0.2f * e.Name.Length); }

        // ------------------------------------------------------------ inputs and outputs
        public void Set(string key, bool v) { int i; if (inIx.TryGetValue(key, out i)) inVal[i] = v; }
        public bool Get(string key) { int i; return inIx.TryGetValue(key, out i) && inVal[i]; }
        public bool HasInput(string key) { return inIx.ContainsKey(key); }
        public IEnumerable<string> Inputs { get { return inNames; } }
        public bool On(string key) { App a; return Apps.TryGetValue(key, out a) && a.On; }
        public bool On(string name, int sec) { return On(name + "@" + sec); }
        public App Get(string name, int sec) { App a; Apps.TryGetValue(sec > 0 ? name + "@" + sec : name, out a); return a; }
        public bool Live(int node) { return plus[node]; }
        public bool HasMinus(int node) { return minus[node]; }
        public int NodeIndex(string node) { int i; return nodeIx.TryGetValue(node, out i) ? i : -1; }
        public bool Live(string node) { int i; return nodeIx.TryGetValue(node, out i) && plus[i]; }
        // Closed circuits: every load that carries current, with its path from the source of the plus to the minus.
        // The path is the shortest one over closed elements, the same the solver found; a node or an element met in
        // several circuits is their common part.
        public CircuitMap Flow(int section)
        {
            var map = new CircuitMap(); int rank = 0;
            foreach (var e in Els)
            {
                if (!e.IsLoad) continue;
                rank++;
                if (e.Ovr == 1 || e.ExternalOpen || (!e.Rung.Cab && e.Sec != section)) continue;
                int p, m;
                if (plus[e.A] && minus[e.B]) { p = e.A; m = e.B; } else if (plus[e.B] && minus[e.A]) { p = e.B; m = e.A; } else continue;
                var c = new Circuit { Load = e, Color = rank % CircuitMap.Colors };
                c.Els.Add(e.Id);
                for (int n = p, guard = 0; guard < Nodes.Count; guard++) { c.Plus.Add(n); int id = parent[n]; if (id < 0) break; c.Els.Add(id); n = Els[id].A == n ? Els[id].B : Els[id].A; }
                for (int n = m, guard = 0; guard < Nodes.Count; guard++) { c.Minus.Add(n); int id = mparent[n]; if (id < 0) break; c.Els.Add(id); n = Els[id].A == n ? Els[id].B : Els[id].A; }
                map.Circuits.Add(c);
                foreach (int n in c.Plus) map.Node[n] = map.Node.ContainsKey(n) && map.Node[n] != c.Color ? CircuitMap.SharedPlus : c.Color;
                foreach (int n in c.Minus) map.Node[n] = map.Node.ContainsKey(n) && map.Node[n] != c.Color ? CircuitMap.SharedMinus : c.Color;
                foreach (int id in c.Els) map.El[id] = map.El.ContainsKey(id) && map.El[id] != c.Color ? (plus[Els[id].A] ? CircuitMap.SharedPlus : CircuitMap.SharedMinus) : c.Color;
            }
            return map;
        }
        public int Overrides { get { int n = 0; foreach (var e in Els) if (e.Ovr != 0) n++; return n; } }

        // ------------------------------------------------------------ solving
        bool ClosedNow(El e)
        {
            if (e.IsLoad) return false;
            if (e.Ovr == 1 || e.ExternalOpen) return false;
            if (e.Ovr == 2) return true;
            switch (e.Kind)
            {
                case ElKind.Contact: { var a = e.App; bool on = e.Delay == 1 ? a.DOn : e.Delay == 2 ? a.DOff : a.On; return on != e.Nc; }
                case ElKind.Switch: case ElKind.Sensor: return inVal[e.In] != e.Nc;
                default: return true;
            }
        }
        void Reach()
        {
            Array.Clear(plus, 0, plus.Length); Array.Clear(minus, 0, minus.Length);
            int head = 0, tail = 0;
            for (int i = 0; i < sources.Count; i++) if (inVal[sourceIn[i]]) { plus[sources[i]] = true; parent[sources[i]] = -1; queue[tail++] = sources[i]; }
            while (head < tail)
            {
                int n = queue[head++];
                foreach (int id in adj[n])
                {
                    var e = Els[id]; if (!e.Closed) continue;
                    int o = e.A == n ? e.B : e.A;
                    if (e.Kind == ElKind.Diode && e.Ovr != 2 && (e.Rev ? e.B != n : e.A != n)) continue;
                    if (plus[o] || o == Minus) { if (o == Minus) shortAt = n; continue; }
                    plus[o] = true; parent[o] = id; queue[tail++] = o;
                }
            }
            head = tail = 0; minus[Minus] = true; mparent[Minus] = -1; queue[tail++] = Minus;
            while (head < tail)
            {
                int n = queue[head++];
                foreach (int id in adj[n])
                {
                    var e = Els[id]; if (!e.Closed) continue;
                    int o = e.A == n ? e.B : e.A;
                    if (e.Kind == ElKind.Diode && e.Ovr != 2 && (e.Rev ? e.A != n : e.B != n)) continue;      // the current goes to the minus along the diode
                    if (minus[o] || plus[o]) continue;
                    minus[o] = true; mparent[o] = id; queue[tail++] = o;
                }
            }
        }
        int shortAt = -1;

        public void Step(double dt)
        {
            T += dt;
            for (int iter = 0; iter < 24; iter++)
            {
                foreach (var e in Els) e.Closed = ClosedNow(e);
                shortAt = -1;
                Reach();
                if (shortAt >= 0 && ShortCircuit()) continue;
                bool changed = false;
                foreach (var a in Apps.Values)
                {
                    bool fed = false;
                    foreach (var c in a.Coils)
                    {
                        c.On = c.Ovr != 1 && !c.ExternalOpen && ((plus[c.A] && minus[c.B]) || (plus[c.B] && minus[c.A]));
                        fed |= c.On;
                    }
                    if (!fed) a.FedT = 0;
                    a.Fed = fed;
                    bool on = a.Force == 2 || (a.Force != 3 && fed && a.FedT >= a.Pick);
                    if (on == a.On) continue;
                    a.On = on; changed = true;
                    if (on) a.TOn = 0; else a.TOff = 0;
                }
                if (!changed) break;
            }
            foreach (var a in Apps.Values) { if (a.On) a.TOn += dt; else a.TOff += dt; a.FedT = a.Fed ? a.FedT + dt : 0; }
        }
        // a path of closed elements from the plus to the minus: the nearest breaker to the source on it switches off
        bool ShortCircuit()
        {
            int n = shortAt; El br = null, last = null;
            for (int guard = 0; guard < Nodes.Count && parent[n] >= 0; guard++)
            {
                var e = Els[parent[n]]; last = last ?? e;
                if (e.Kind == ElKind.Switch && e.Ovr == 0 && Breakers.Contains(Bare(e.Key))) br = e;
                n = e.A == n ? e.B : e.A;
            }
            if (br != null) { inVal[br.In] = false; Log("Короткое замыкание в цепи «" + br.Rung.Name + "»: отключился автомат " + br.Name + (br.Sec > 0 && br.Key.IndexOf('@') > 0 ? ", секция " + br.Sec : "")); if (Trip != null) Trip(br.Key); return true; }
            return false;
        }
        public readonly HashSet<string> Breakers = new HashSet<string>();

        // ------------------------------------------------------------ interventions of the instructor
        public void SetOverride(El e, int ovr)
        {
            if (e.Ovr == ovr) return;
            e.Ovr = ovr;
            if (e.IsLoad && e.Kind != ElKind.Lamp) { e.App.Force = 0; foreach (var c in e.App.Coils) if (c.Ovr >= 2) e.App.Force = c.Ovr; }
            Log("Схема" + (e.Rung.Cab ? "" : ", секция " + e.Sec) + ": " + e.Title + " в цепи «" + e.Rung.Name + "» — " + OvrName(e));
        }
        public static string OvrName(El e)
        {
            if (e.Ovr == 0) return "исправно";
            if (e.Kind == ElKind.Lamp) return "перегорела";
            if (e.IsLoad) return e.Ovr == 1 ? "обрыв" : e.Ovr == 2 ? "заклинен включённым" : "заклинен выключенным";
            return e.Ovr == 1 ? "обрыв" : "замкнут перемычкой";
        }
        public int NextOverride(El e) { int n = e.Kind == ElKind.Lamp ? 2 : 3; return (e.Ovr + 1) % n; }
        public void ClearOverrides() { foreach (var e in Els) if (e.Ovr != 0) SetOverride(e, 0); }

        // why a load is off: the open element next to the live part of its circuit
        public string WhyOff(App a)
        {
            if (a.On) return "";
            if (a.Force == 3) return "заклинен выключенным";
            foreach (var c in a.Coils)
            {
                if (c.Ovr == 1 || c.ExternalOpen) return "обрыв катушки";
                string s = Side(c.A, true) ?? Side(c.B, false);
                if (s != null) return s;
            }
            return a.Coils.Count == 0 ? "нет катушки на схеме" : "нет питания";
        }
        string Side(int node, bool plusSide)
        {
            var live = plusSide ? plus : minus;
            if (live[node]) return null;
            // all the nodes joined to the terminal by closed elements; an open element from them to a live node is the cause
            var seen = new HashSet<int> { node }; var q = new Queue<int>(); q.Enqueue(node); El far = null;
            while (q.Count > 0)
            {
                int n = q.Dequeue();
                foreach (int id in adj[n])
                {
                    var e = Els[id]; if (e.IsLoad) continue;
                    int o = e.A == n ? e.B : e.A;
                    if (e.Closed) { if (seen.Add(o)) q.Enqueue(o); continue; }
                    if (live[o]) return (plusSide ? "нет плюса: " : "нет минуса: ") + Open(e);
                    if (far == null && e.Kind != ElKind.Diode) far = e;
                }
            }
            return far == null ? (plusSide ? "нет плюса" : "нет минуса") : (plusSide ? "нет плюса: " : "нет минуса: ") + Open(far) + " и цепь до него";
        }
        string Open(El e) { return (e.Ovr == 1 ? "обрыв — " : "разомкнут ") + e.Title + " (" + LabelOf(e.A) + "–" + LabelOf(e.B) + ", цепь «" + e.Rung.Name + "»)"; }
        public string NodeName(int n) { return LabelOf(n); }
    }
}
