// Walk of the instructor window: every section is opened and every control in it is operated (buttons, toggles, segment
// switches, lists, sliders, numbers, the strips of wagons, the context menus of the map, the lamps of the cab, the train
// editor). For each control the report says what happened: an exception, a dialog, a change of the state, or nothing.
// The window is shown transparent; the pult, video and BLOK are off; documents are not opened in the browser.
// Build: csc /target:winexe /r:Trainer2TE116.exe UiWalk.cs; run: UiWalk.exe <outdir> <folder of the trainer>
// Report: <outdir>\ui_walk.txt, the last line "WALK PASS" or "WALK FAIL".
using System;
using System.Collections;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;
using Trainer2TE116;

static class UiWalk
{
    const BindingFlags All = BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance;
    static MainForm f; static Type t = typeof(MainForm);
    static readonly List<string> rep = new List<string>();
    static readonly List<string> dialogs = new List<string>();
    static int errors, noEffect, done; static volatile string stage = "start";
    static string editorPlan = "cancel";
    static readonly System.Windows.Forms.Timer guard = new System.Windows.Forms.Timer { Interval = 120 };

    [DllImport("user32.dll")] static extern bool EnumThreadWindows(uint id, EnumProc cb, IntPtr p);
    [DllImport("user32.dll")] static extern bool EnumChildWindows(IntPtr h, EnumProc cb, IntPtr p);
    [DllImport("user32.dll", CharSet = CharSet.Unicode)] static extern int GetClassName(IntPtr h, StringBuilder s, int n);
    [DllImport("user32.dll", CharSet = CharSet.Unicode)] static extern int GetWindowText(IntPtr h, StringBuilder s, int n);
    [DllImport("user32.dll")] static extern bool IsWindowVisible(IntPtr h);
    [DllImport("user32.dll")] static extern bool PostMessage(IntPtr h, int msg, IntPtr w, IntPtr l);
    [DllImport("kernel32.dll")] static extern uint GetCurrentThreadId();
    delegate bool EnumProc(IntPtr h, IntPtr p);

    static object Get(string n) { return t.GetField(n, All).GetValue(f); }
    static object Call(string n, params object[] a) { return t.GetMethods(All).First(m => m.Name == n && m.DeclaringType == t && m.GetParameters().Length == a.Length).Invoke(f, a.Length == 0 ? null : a); }
    static Simulation Sim { get { return (Simulation)Get("sim"); } }

    [STAThread]
    static int Main(string[] args)
    {
        try { Console.OutputEncoding = Encoding.UTF8; } catch { }
        string outDir = args.Length > 0 ? args[0] : ".";
        new System.Threading.Thread(() => { System.Threading.Thread.Sleep(240000); rep.Add("FAIL timeout 240 s at: " + stage); Flush(outDir, false); Environment.Exit(2); }) { IsBackground = true }.Start();
        try
        {
            var prog = typeof(MainForm).Assembly.GetType("Trainer2TE116.Program");
            var pa = (Dictionary<string, string>)prog.GetField("Arguments").GetValue(null);
            pa["pult"] = "off"; pa["video"] = "off"; pa["blok"] = "off"; pa["hidden"] = "1"; pa["data"] = Path.Combine(outDir, "walk_data");
            if (args.Length > 1) prog.GetField("ReleaseDir").SetValue(null, args[1]);
            Application.ThreadException += (s, e) => { errors++; rep.Add("  ОШИБКА (в цикле сообщений): " + e.Exception.Message); };
            f = new MainForm(); f.Size = new Size(1600, 960); f.ShowInTaskbar = false; f.Opacity = 0; f.StartPosition = FormStartPosition.Manual; f.Location = new Point(0, 0);
            f.Show(); Application.DoEvents();
            ((System.Windows.Forms.Timer)Get("timer")).Stop();
            guard.Tick += (s, e) => Guard(); guard.Start();
            System.Threading.Thread.Sleep(1500); Application.DoEvents();     // the list of the routes is read in the background

            // ---- before the trip
            Head("Шапка окна до поездки"); Walk(Header(), false);
            Head("Раздел «Поездка» (до начала поездки)"); Call("Show", 6); Application.DoEvents();
            Walk(Page(6), false, c => c is Button && c.Text == "Начать поездку");
            Head("Редактор поезда");
            editorPlan = "save"; Act("кнопка «Создать поезд…» → «Сохранить поезд»", () => Find(Page(6), "Создать поезд…").PerformClick());
            editorPlan = "bad"; Act("кнопка «Изменить выбранный…» → неверное количество → «Сохранить поезд»", () => Find(Page(6), "Изменить выбранный…").PerformClick());
            editorPlan = "cancel";
            var seg = (Seg)Get("segSource"); Click(seg, 0);
            Head("Разделы до начала поездки (поездки нет)");
            for (int k = 0; k < 6; k++) { Call("Show", k); Application.DoEvents(); Act("раздел «" + Names[k] + "» открыт без поездки", () => { Call("Tick"); }, false); }
            Call("Show", 6);
            ((TextBox)Get("tbStudent")).Text = "Проверка И. И."; ((TextBox)Get("tbTab")).Text = "1234"; ((TextBox)Get("tbInstructor")).Text = "Инструктор";
            Head("Начало поездки");
            Act("кнопка «Начать поездку»", () => Find(Page(6), "Начать поездку").PerformClick());
            if (Sim == null) { rep.Add("FAIL поездка не началась"); Flush(outDir, false); return 1; }
            for (int i = 0; i < 50; i++) Call("Tick");

            // ---- in the trip: every section
            for (int k = 0; k < 6; k++)
            {
                stage = "page " + k; Head("Раздел «" + Names[k] + "»"); Call("Show", k); Call("Tick"); Application.DoEvents();
                Walk(Page(k), true);
                if (k == 0) MapMenus();
                Snap(Path.Combine(outDir, "walk_" + k + ".png"));
            }
            Head("Шапка окна в поездке");
            Walk(Header(), true, c => c is Button && c.Text == "Закончить поездку");
            Act("клавиша «Пробел» — пауза", () => { typeof(Form).GetMethod("OnKeyDown", All).Invoke(f, new object[] { new KeyEventArgs(Keys.Space) }); });
            if (Sim.Paused) Call("TogglePause");
            Head("Переходы между разделами (кнопки слева)");
            foreach (var nb in ((IEnumerable)Get("navs")).Cast<Button>().ToList()) { var b = nb; Act("раздел «" + b.Text + "»", () => b.PerformClick()); }

            // ---- the end of the trip and a second trip
            Head("Конец поездки и новая поездка");
            Act("кнопка «Закончить поездку»", () => Find(Header(), "Закончить поездку").PerformClick());
            Check(Sim.Finished, "поездка закончена, протокол записан: " + File.Exists(Path.Combine((string)Get("tripDir"), "protocol.html")));
            Act("кнопка «Закончить поездку» повторно", () => Find(Header(), "Закончить поездку").PerformClick(), false);
            Act("документы после конца поездки: «Протокол поездки с оценкой»", () => Find(Page(5), "Протокол поездки с оценкой").PerformClick());
            Act("«Расшифровка кассеты — лента» после конца поездки", () => Find(Page(5), "Расшифровка кассеты — лента").PerformClick());
            var arch = (ListBox)Get("archive");
            Check(arch.Items.Count > 0, "архив поездок: записей " + arch.Items.Count);
            if (arch.Items.Count > 0) { arch.SelectedIndex = 0; Act("архив: двойной щелчок по поездке", () => typeof(Control).GetMethod("OnDoubleClick", All).Invoke(arch, new object[] { EventArgs.Empty })); }
            for (int i = 0; i < 20; i++) Call("Tick");
            Call("Show", 6);
            var old = Sim;
            Act("вторая поездка: «Начать поездку»", () => Find(Page(6), "Начать поездку").PerformClick());
            Check(Sim != null && Sim != old && !Sim.Finished, "вторая поездка началась");
            for (int i = 0; i < 100; i++) Call("Tick");
            Check(Sim.T > 0, "вторая поездка идёт: " + Sim.T.ToString("0") + " с");
            Act("закрытие окна", () => f.Close(), false);

            rep.Add(""); rep.Add("Диалоги за проход: " + dialogs.Count); foreach (var d in dialogs) rep.Add("  " + d);
            rep.Add("Открытые документы и папки: " + f.Opened.Count); foreach (var o in f.Opened) rep.Add("  " + Path.GetFileName(o) + (File.Exists(o) ? " (" + new FileInfo(o).Length + " байт)" : Directory.Exists(o) ? " (папка)" : " — ФАЙЛА НЕТ"));
            foreach (var o in f.Opened) if (!File.Exists(o) && !Directory.Exists(o)) errors++;
            Flush(outDir, errors == 0);
            return errors == 0 ? 0 : 1;
        }
        catch (Exception ex) { rep.Add("FAIL " + stage + ": " + (ex.InnerException ?? ex)); Flush(outDir, false); return 1; }
    }
    static readonly string[] Names = { "Карта", "Локомотив", "Поезд", "Пневмо", "Управление", "Журнал", "Поездка" };
    static void Flush(string outDir, bool ok)
    {
        rep.Add(""); rep.Add("Проверено органов: " + done + ", ошибок: " + errors + ", без видимого действия: " + noEffect);
        rep.Add(ok ? "WALK PASS" : "WALK FAIL");
        File.WriteAllLines(Path.Combine(outDir, "ui_walk.txt"), rep.ToArray(), new UTF8Encoding(true));
        Console.WriteLine(rep[rep.Count - 2]); Console.WriteLine(rep[rep.Count - 1]);
    }
    static void Head(string s) { rep.Add(""); rep.Add("== " + s); stage = s; }
    static void Check(bool ok, string text) { rep.Add((ok ? "  ок      " : "  ОШИБКА  ") + text); if (!ok) errors++; }
    static void Snap(string path) { using (var b = new Bitmap(f.Width, f.Height)) { f.DrawToBitmap(b, new Rectangle(0, 0, f.Width, f.Height)); b.Save(path); } }

    static Control Page(int k) { return ((IList)Get("pages")).Cast<Control>().ElementAt(k); }
    static Control Header() { return ((Control)Get("btPause")).Parent.Parent; }
    static Button Find(Control root, string text) { var b = Flat(root).OfType<Button>().FirstOrDefault(x => x.Text == text); if (b == null) throw new Exception("нет кнопки «" + text + "»"); return b; }
    static IEnumerable<Control> Flat(Control c) { foreach (Control x in c.Controls) { yield return x; foreach (var y in Flat(x)) yield return y; } }

    // ---- modal windows: the train editor is operated by the plan, the message boxes and file dialogs are noted and closed
    static void Guard()
    {
        bool found = false;
        EnumThreadWindows(GetCurrentThreadId(), (h, p) =>
        {
            var cls = new StringBuilder(64); GetClassName(h, cls, 64);
            if (cls.ToString() != "#32770" || !IsWindowVisible(h)) return true;
            var title = new StringBuilder(256); GetWindowText(h, title, 256); var text = new StringBuilder();
            EnumChildWindows(h, (c, q) => { var cc = new StringBuilder(64); GetClassName(c, cc, 64); if (cc.ToString() == "Static") { var s = new StringBuilder(512); GetWindowText(c, s, 512); if (s.Length > 0) text.Append(s).Append(' '); } return true; }, IntPtr.Zero);
            found = true; dialogs.Add("окно «" + title + "»: " + text.ToString().Trim());
            lastDialog = "«" + title + "» " + text.ToString().Trim();
            PostMessage(h, 0x0111, (IntPtr)2, IntPtr.Zero);     // WM_COMMAND IDCANCEL: closes a message box with OK and a file dialog
            PostMessage(h, 0x0010, IntPtr.Zero, IntPtr.Zero);   // WM_CLOSE
            return true;
        }, IntPtr.Zero);
        if (found) return;      // a message box over the editor is closed first
        var ed = Application.OpenForms.OfType<TrainEditor>().FirstOrDefault();
        if (ed != null && ed.Visible)
        {
            string plan = editorPlan; editorPlan = "cancel";
            var g = Flat(ed).OfType<DataGridView>().First();
            if (plan == "bad") { g.Rows[0].Cells[1].Value = "0"; }
            if (plan != "cancel") { dialogs.Add("редактор поезда: «Сохранить поезд» (" + plan + ")"); Flat(ed).OfType<Button>().First(b => b.Text == "Сохранить поезд").PerformClick(); }
            else { dialogs.Add("редактор поезда: «Отмена»"); Flat(ed).OfType<Button>().First(b => b.Text == "Отмена").PerformClick(); }
            return;
        }
    }
    static string lastDialog;

    // ---- the state that an action may change: the simulation and what the window shows
    static void Dump(StringBuilder sb, object o, int depth)
    {
        if (o == null) { sb.Append("null;"); return; }
        var tp = o.GetType();
        if (tp.IsPrimitive || o is string || tp.IsEnum) { sb.Append(o is double ? ((double)o).ToString("0.###") : o.ToString()).Append(';'); return; }
        if (o is Array) { foreach (var x in (Array)o) Dump(sb, x, depth); return; }
        if (depth <= 0) return;
        foreach (var fi in tp.GetFields(BindingFlags.Public | BindingFlags.Instance)) { if (fi.FieldType == typeof(double)) continue; sb.Append(fi.Name).Append('='); Dump(sb, fi.GetValue(o), depth - 1); }
    }
    static string State()
    {
        var sb = new StringBuilder(); var sim = Sim;
        sb.Append("page=").Append(((IList)Get("pages")).Cast<Control>().ToList().FindIndex(p => p.Visible)).Append(";speed=").Append(Get("speedUp")).Append(";night=").Append(Get("night")).Append(";fog=").Append(Get("fog")).Append(";opened=").Append(f.Opened.Count).Append(";dlg=").Append(dialogs.Count).Append(';');
        if (sim != null)
        {
            sb.Append("ev=").Append(sim.Events.Count).Append(";paused=").Append(sim.Paused).Append(";fin=").Append(sim.Finished).Append(';');
            Dump(sb, sim.C, 2); foreach (var r in sim.Tr.Rm) Dump(sb, r, 1); Dump(sb, sim.Tr.LampBurnt, 1); Dump(sb, sim.Tr.FaultOn, 1);
            sb.Append("faults=").Append(string.Join("", sim.Faults.Select(x => x.On ? "1" : "0").ToArray())).Append(';');
            var route = (Route)Get("route");
            sb.Append("signs=").Append(route.Signs.Count).Append(";obst=").Append(string.Join("", route.Obstacles.Select(x => x.Active ? "1" : "0").ToArray())).Append(";asp=").Append(string.Join(",", route.Lights.Take(40).Select(x => x.Aspect.ToString()).ToArray())).Append(";rain=").Append(route.Rain).Append(route.Snow).Append(';');
        }
        foreach (var c in Flat(f))
        {
            if (c is Seg) sb.Append("seg").Append(((Seg)c).Selected).Append(';');
            else if (c is CheckBox) sb.Append(((CheckBox)c).Checked ? '1' : '0');
            else if (c is ListBox) sb.Append("lb").Append(((ListBox)c).SelectedIndex).Append('/').Append(((ListBox)c).Items.Count).Append(';');
            else if (c is DataGridView) { var g = (DataGridView)c; sb.Append("grid").Append(g.Rows.Count).Append('/').Append(g.SelectedRows.Count > 0 ? g.SelectedRows[0].Index : -1).Append(';'); }
            else if (c is TrackBar) sb.Append("tb").Append(((TrackBar)c).Value).Append(';');
            else if (c is NumericUpDown) sb.Append("n").Append(((NumericUpDown)c).Value).Append(';');
            else if (c is Button) sb.Append(c.Text).Append(';');
            else if (c is Label && c.Cursor == Cursors.Hand) sb.Append(c.Text).Append(c.BackColor.ToArgb()).Append(';');
        }
        return sb.ToString();
    }

    static bool Act(string what, Action a, bool expectEffect = true)
    {
        done++; lastDialog = null; string before = State(); int ev0 = Sim != null ? Sim.Events.Count : 0; string err = null;
        try { a(); Application.DoEvents(); if (Sim != null) { Call("Tick"); Call("SyncCab"); } Application.DoEvents(); }
        catch (Exception ex) { var e = ex; while (e.InnerException != null) e = e.InnerException; err = e.GetType().Name + ": " + e.Message; }
        string after = State(); string note = "";
        if (Sim != null && Sim.Events.Count > ev0) note = " — событие: " + Sim.Events[Sim.Events.Count - 1];
        if (lastDialog != null) note += " — диалог " + lastDialog;
        if (err != null) { errors++; rep.Add("  ОШИБКА  " + what + " — " + err); return false; }
        if (before == after && expectEffect) { noEffect++; rep.Add("  НЕТ ДЕЙСТВИЯ  " + what); return false; }
        rep.Add("  ок      " + what + note); return true;
    }

    static string Label(Control c)
    {
        foreach (var fi in t.GetFields(All)) { var v = fi.GetValue(f); if (ReferenceEquals(v, c)) return fi.Name; }
        return c.GetType().Name;
    }
    static void Click(Control c, int segIndex)
    {
        var s = (Seg)c; float w = (s.Width - 6f) / s.Items.Length;
        typeof(Control).GetMethod("OnMouseClick", All).Invoke(s, new object[] { new MouseEventArgs(MouseButtons.Left, 1, (int)(3 + w * segIndex + w / 2), s.Height / 2, 0) });
    }
    static void Mouse(Control c, MouseButtons b, int x, int y) { typeof(Control).GetMethod("OnMouseClick", All).Invoke(c, new object[] { new MouseEventArgs(b, 1, x, y, 0) }); }

    // every control under the root: operated and put back as it was
    static void Walk(Control root, bool inTrip, Func<Control, bool> skip = null)
    {
        var seen = new HashSet<Control>();
        for (int pass = 0; pass < 3; pass++)       // lists of buttons are rebuilt by some actions (signals, obstacles)
        {
            foreach (var c in Flat(root).ToList())
            {
                if (seen.Contains(c) || c.IsDisposed || c.Parent == null) continue; seen.Add(c);
                if (skip != null && skip(c)) continue;
                if (!c.Enabled) { if (c is Button || c is CheckBox) rep.Add("  выкл.   " + c.Text); continue; }
                if (c is Seg)
                {
                    var s = (Seg)c; int was = s.Selected;
                    for (int i = 0; i < s.Items.Length; i++) { if (i == s.Selected) continue; int ii = i; Act("переключатель " + string.Join(" / ", s.Items) + " → «" + s.Items[i] + "»", () => Click(s, ii)); }
                    if (s.Selected != was) Act("переключатель → обратно «" + s.Items[was] + "»", () => Click(s, was));
                }
                else if (c is CheckBox)
                {
                    var cb = (CheckBox)c; bool was = cb.Checked;
                    Act("флажок «" + cb.Text + "» → " + (was ? "выкл" : "вкл"), () => cb.Checked = !was);
                    Act("флажок «" + cb.Text + "» → обратно", () => cb.Checked = was);
                }
                else if (c is Button)
                {
                    var b = (Button)c; if (b.IsDisposed) continue; string txt = b.Text;
                    Act((c is NavBtn ? "раздел «" : "кнопка «") + txt + "»", () => b.PerformClick(), inTrip || c is NavBtn);
                }
                else if (c is TrackBar)
                {
                    var tb = (TrackBar)c; int was = tb.Value;
                    Act("ползунок " + Label(c) + " → " + tb.Maximum, () => tb.Value = tb.Maximum);
                    Act("ползунок " + Label(c) + " → " + tb.Minimum + " и обратно", () => { tb.Value = tb.Minimum; tb.Value = was; }, false);
                }
                else if (c is NumericUpDown)
                {
                    var n = (NumericUpDown)c; decimal was = n.Value;
                    Act("число " + Label(c) + ": вверх, до предела, обратно", () => { n.UpButton(); n.Value = n.Maximum; n.Value = n.Minimum; n.Value = was; }, false);
                }
                else if (c is DataGridView)
                {
                    var g = (DataGridView)c;
                    if (g.Rows.Count > 1) Act("таблица " + Label(c) + ": выбор строки 2 из " + g.Rows.Count, () => { g.ClearSelection(); g.Rows[1].Selected = true; });
                    else rep.Add("  пусто   таблица " + Label(c));
                }
                else if (c is ListBox)
                {
                    var lb = (ListBox)c; int was = lb.SelectedIndex;
                    if (lb.Items.Count == 0) { rep.Add("  пусто   список " + Label(c)); continue; }
                    if (lb.SelectionMode == SelectionMode.None) { rep.Add("  показ   список " + Label(c) + ": строк " + lb.Items.Count); continue; }
                    int other = lb.Items.Count > 1 ? (was == 0 ? 1 : 0) : 0;
                    Act("список " + Label(c) + ": выбор строки " + (other + 1) + " из " + lb.Items.Count, () => lb.SelectedIndex = other, lb.Items.Count > 1);
                    Act("список " + Label(c) + ": двойной щелчок", () => typeof(Control).GetMethod("OnDoubleClick", All).Invoke(lb, new object[] { EventArgs.Empty }), false);
                    if (was >= 0 && was < lb.Items.Count) lb.SelectedIndex = was;
                }
                else if (c is WagonStrip)
                {
                    Act("полоса вагонов " + Label(c) + ": щелчок по вагону", () => { Mouse(c, MouseButtons.Left, c.Width / 3, c.Height / 2); Mouse(c, MouseButtons.Left, 30, c.Height / 2); }, false);
                }
                else if (c is Label && c.Cursor == Cursors.Hand)
                {
                    var l = c; string name = l.Text;
                    Act("лампа «" + name + "»: щелчок — перегорела", () => typeof(Control).GetMethod("OnClick", All).Invoke(l, new object[] { EventArgs.Empty }));
                    Act("лампа «" + name + "»: щелчок — заменена", () => typeof(Control).GetMethod("OnClick", All).Invoke(l, new object[] { EventArgs.Empty }));
                }
                else if (c is TextBox) { var tb = (TextBox)c; Act("поле ввода (" + (tb.Text.Length > 0 ? tb.Text : "пусто") + ")", () => { string w = tb.Text; tb.Text = "тест"; tb.Text = w; }, false); }
            }
        }
    }

    // the context menus of the map: every item
    static void MapMenus()
    {
        var map = (RegimeView)Get("map"); var route = (Route)Get("route");
        Act("карта: левый щелчок мимо светофора", () => Mouse(map, MouseButtons.Left, map.Width / 2, 8), false);
        Act("карта: правый щелчок — меню", () => Mouse(map, MouseButtons.Right, map.Width / 2, map.Height / 2), false);
        var m = f.MapMenu; if (m == null) { Check(false, "меню карты по правой кнопке не появилось"); return; }
        int n = 0; foreach (var it in Leaves(m.Items)) { var item = it; n++; Act("меню карты: " + MenuPath(item), () => item.PerformClick()); }
        Check(n >= 20, "пунктов в меню карты по правой кнопке: " + n);
        m.Close();
        // a signal on the map: the aspects
        int li = route.NextLight(Sim.Dyn.HeadX);
        if (li < 0) { rep.Add("  нет     светофора впереди"); return; }
        float px = map.PxAt(route.Lights[li].X);
        int top = (int)map.LightsTop;
        f.MapMenu = null;
        Act("карта: левый щелчок по светофору " + route.Lights[li].Name, () => Mouse(map, MouseButtons.Left, (int)px, top + 2), false);
        m = f.MapMenu; if (m == null) { rep.Add("  НЕТ ДЕЙСТВИЯ  меню светофора на карте не появилось (светофор вне видимой части карты?) px=" + px); noEffect++; return; }
        foreach (var it in Leaves(m.Items)) { var item = it; if (item.Text.StartsWith("●")) continue; Act("меню светофора: " + item.Text.Trim(), () => item.PerformClick()); }
        m.Close();
    }
    static IEnumerable<ToolStripMenuItem> Leaves(ToolStripItemCollection items)
    {
        foreach (ToolStripItem i in items) { var mi = i as ToolStripMenuItem; if (mi == null) continue; if (mi.DropDownItems.Count == 0) yield return mi; else foreach (var x in Leaves(mi.DropDownItems)) yield return x; }
    }
    static string MenuPath(ToolStripItem i) { var s = i.Text; var o = i.OwnerItem; while (o != null) { s = o.Text + " → " + s; o = o.OwnerItem; } return s; }
}
