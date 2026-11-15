// Hidden WinForms validation of the netlist view. No desktop automation or native trainer connections.
using System;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using Trainer2TE116;

static class SchemeUi
{
    const BindingFlags Instance = BindingFlags.Instance | BindingFlags.NonPublic;
    static int checks;
    static void Check(bool ok, string message) { checks++; if (!ok) throw new Exception(message); Console.WriteLine("PASS " + message); }
    static T Field<T>(object target, string name) { return (T)target.GetType().GetField(name, Instance).GetValue(target); }
    static void Mouse(SchemeCanvas c, string action, MouseButtons button, Point p)
    { typeof(SchemeCanvas).GetMethod(action, Instance).Invoke(c, new object[] { new MouseEventArgs(button, 1, p.X, p.Y, 0) }); }
    static void Snap(Control c, string path) { using (var image = new Bitmap(c.Width, c.Height)) { c.DrawToBitmap(image, new Rectangle(Point.Empty, c.Size)); image.Save(path); } }
    [STAThread]
    static int Main(string[] args)
    {
        string output = args.Length > 0 ? args[0] : AppDomain.CurrentDomain.BaseDirectory;
        Directory.CreateDirectory(output);
        try
        {
            Application.EnableVisualStyles();
            var tr = new Traction(); tr.Init(2, "nominal", 7);
            for (int i = 0; i < 30; i++) tr.Step(0.1, 0);
            var view = new SchemeView { Dock = DockStyle.Fill };
            using (var form = new Form { Size = new Size(1500, 900), Opacity = 0, ShowInTaskbar = false })
            {
                form.Controls.Add(view); form.Show(); Application.DoEvents(); view.Bind(tr.Electric, false);
                Check(view.OriginalMode, "original PDF is the default view"); view.SetOriginalMode(false);
                var sheet = Field<ComboBox>(view, "sheets"); var section = Field<ComboBox>(view, "sections");
                Check(sheet.Items.Count == 11, "all 11 sheets listed");
                for (int sec = 0; sec < 2; sec++)
                    for (int sh = 0; sh < 11; sh++)
                    {
                        section.SelectedIndex = sec; sheet.SelectedIndex = sh; view.Canvas.AutoScrollPosition = Point.Empty; view.Canvas.ScaleUnit = 38; view.Canvas.ResetExtent();
                        view.RefreshState(); Application.DoEvents();
                        Snap(view, Path.Combine(output, "scheme_s" + (sec + 1) + "_" + (sh + 1).ToString("00") + ".png"));
                        Check(view.Canvas.Section == sec + 1 && view.Canvas.Sheet == sh, "draw section " + (sec + 1) + " sheet " + (sh + 1));
                    }
                section.SelectedIndex = 0; view.FindNext("КВ"); var first = view.Canvas.FocusedElement;
                Check(first != null && first.Name.Contains("КВ"), "search selects matching apparatus");
                view.FindNext("КВ"); Check(view.Canvas.FocusedElement != first, "repeat search advances across contacts");
                var coil = view.Model.Get("КВ", 1).Coils[0]; view.Navigate(coil); Application.DoEvents();
                Point point = view.Canvas.ElementPoint(coil);
                Check(view.Canvas.Hit(point) == coil, "hit test matches netlist coil after navigation");
                for (int value = 1; value <= 3; value++)
                {
                    Mouse(view.Canvas, "OnMouseDown", MouseButtons.Left, point); Mouse(view.Canvas, "OnMouseUp", MouseButtons.Left, point);
                    Check(coil.Ovr == value % 3, "left click override cycle " + value);
                }
                Mouse(view.Canvas, "OnMouseDown", MouseButtons.Right, point); Application.DoEvents();
                Check(coil.Ovr == 0, "right click does not change override");
                var menu = Field<ContextMenuStrip>(view.Canvas, "menu");
                Check(menu.Items.Count == 4, "coil menu offers all four states");
                menu.Items[3].PerformClick(); menu.Close();
                Check(coil.Ovr == 3 && coil.App.Force == 3, "context menu sticks armature off");
                view.Model.SetOverride(coil, 2); view.RefreshState();
                var old = view.Model; var fresh = new Scheme(SchemeData.Text); view.Bind(fresh, true);
                Check(view.Model != old && fresh.Els[coil.Id].Ovr == 2, "defects retained on rebind");
                Check(Field<ListBox>(view, "faults").Items.Count == 1, "fault list reports retained defect");
                Check(Field<ListBox>(view, "contacts").Items.Count == 0 && view.Canvas.FocusedElement == null, "rebind clears old contact references");
                var clear = view.Controls.Cast<Control>().SelectMany(c => c.Controls.Cast<Control>()).OfType<Btn>().First(b => b.Text == "Снять все дефекты схемы");
                clear.PerformClick(); Check(fresh.Overrides == 0, "clear button removes defects");
                view.FindNext("КВ"); view.Canvas.Fit(); Application.DoEvents();
                Snap(view, Path.Combine(output, "scheme_fitted.png"));
                float scale = view.Canvas.ScaleUnit; view.Canvas.Zoom(1.25f, new Point(200, 200)); Check(view.Canvas.ScaleUnit > scale, "zoom increases scale");
                view.Canvas.Zoom(0.001f, Point.Empty); Check(view.Canvas.ScaleUnit >= 15, "zoom clamps minimum");
                view.Canvas.Zoom(1000f, Point.Empty); Check(view.Canvas.ScaleUnit <= 80, "zoom clamps maximum");
                form.ClientSize = new Size(1140, 630); view.Canvas.ScaleUnit = 38; view.Canvas.ResetExtent(); view.Navigate(fresh.Get("КВ", 1).Coils[0]); Application.DoEvents();
                Snap(view, Path.Combine(output, "scheme_compact.png"));
                Check(view.Canvas.Width > 700 && view.Canvas.Height > 500, "compact layout preserves usable canvas");
                OriginalTests(view, fresh, output);
            }
            Console.WriteLine("SCHEME UI PASS " + checks); return 0;
        }
        catch (Exception e) { Console.WriteLine("SCHEME UI FAIL " + (e.InnerException ?? e)); return 1; }
    }
    static void OriginalTests(SchemeView view, Scheme model, string output)
    {
        string directory = Path.Combine(output, "scheme_ui_review", "fixture"); Directory.CreateDirectory(directory);
        using (var background = new Bitmap(600, 400))
        using (var g = Graphics.FromImage(background))
        {
            g.Clear(Color.White); g.FillRectangle(Brushes.Blue, 10, 10, 80, 40);
            for (int i = 1; i <= 7; i++) background.Save(Path.Combine(directory, "page-" + i.ToString("00") + ".png"));
        }
        string signature; using (var sha = SHA256.Create()) signature = BitConverter.ToString(sha.ComputeHash(Encoding.UTF8.GetBytes(SchemeData.Text))).Replace("-", "");
        var coil = model.Get("КВ", 1).Coils[0]; var contact = model.Get("КВ", 1).Contacts[0];
        var single = new AtlasHit { page = 1, label = "TEST КВ", kind = "coil", binding = "symbol", x = 0.2, y = 0.2, w = 0.08, h = 0.08, modelIds = new[] { coil.Id } };
        var ambiguous = new AtlasHit { page = 2, label = "TEST КВ", kind = "contact", binding = "label", x = 0.4, y = 0.4, w = 0.08, h = 0.08, modelIds = new[] { coil.Id, contact.Id } };
        var unbound = new AtlasHit { page = 3, label = "UNBOUND", kind = "wire", x = 0.4, y = 0.4, w = 0.08, h = 0.08, modelIds = new int[0] };
        var atlas = new SchemeAtlas { modelSignature = signature, pages = Enumerable.Range(1, 7).Select(p => new AtlasPage { page = p, title = "PDF " + p, image = "page-" + p.ToString("00") + ".png", hits = p == 1 ? new[] { single } : p == 2 ? new[] { ambiguous } : p == 3 ? new[] { unbound } : new AtlasHit[0] }).ToArray() };
        var json = new JavaScriptSerializer(); File.WriteAllText(Path.Combine(directory, "atlas.json"), json.Serialize(atlas));
        view.LoadOriginalAtlas(directory); view.SetOriginalMode(true); Application.DoEvents();
        var pdf = view.OriginalCanvas;
        Check(pdf.Available && pdf.Atlas.pages.Length == 7 && pdf.BindingError == null, "signed PDF atlas loads 7 original pages");
        for (int i = 0; i < 7; i++) { pdf.SetPage(i); using (var image = new Bitmap(pdf.Width, pdf.Height)) pdf.DrawToBitmap(image, new Rectangle(Point.Empty, pdf.Size)); }
        Check(pdf.PageIndex == 6, "all PDF pages render without redraw");
        pdf.Section = 1; Check(pdf.FindNext("TEST"), "PDF search finds label across pages");
        var bothSections = new AtlasHit { binding = "symbol", modelIds = new[] { coil.Id, model.Get("КВ", 2).Coils[0].Id } };
        Check(pdf.Assigned(bothSections) == coil, "symbol pair resolves only after section filtering");
        var labelOnly = new AtlasHit { binding = "label", modelIds = new[] { coil.Id } };
        Check(pdf.Resolve(labelOnly).Count == 1 && pdf.Assigned(labelOnly) == null, "single label match still requires explicit choice");
        var unknown = new AtlasHit { binding = "unknown", modelIds = new[] { coil.Id } };
        Check(pdf.Resolve(unknown).Count == 0, "unknown binding cannot change model even with an id");
        var a = pdf.FocusedHit; Check(pdf.Assigned(a) == coil, "single explicit id resolves exact model element");
        var rect = pdf.HitBounds(a); var point = Point.Round(new PointF(rect.X + rect.Width / 2, rect.Y + rect.Height / 2));
        Check(pdf.Hit(point) == a, "PDF hotspot hit follows image scaling and scroll");
        int before = coil.Ovr;
        typeof(OriginalSchemeCanvas).GetMethod("OnMouseDown", Instance).Invoke(pdf, new object[] { new MouseEventArgs(MouseButtons.Left, 1, point.X, point.Y, 0) });
        Check(coil.Ovr == (before + 1) % 3, "unambiguous PDF click changes graph override");
        model.ClearOverrides(); pdf.FindNext("TEST"); var b = pdf.FocusedHit;
        Check(b != a && pdf.Assigned(b) == null, "ambiguous label never guesses a contact");
        rect = pdf.HitBounds(b); point = Point.Round(new PointF(rect.X + rect.Width / 2, rect.Y + rect.Height / 2));
        typeof(OriginalSchemeCanvas).GetMethod("OnMouseDown", Instance).Invoke(pdf, new object[] { new MouseEventArgs(MouseButtons.Left, 1, point.X, point.Y, 0) });
        Check(model.Overrides == 0, "ambiguous click leaves graph unchanged before choice");
        var menu = Field<ContextMenuStrip>(pdf, "menu"); Check(menu.Items.Count == 2, "ambiguous menu names exact candidate elements");
        ((ToolStripMenuItem)menu.Items[0]).DropDownItems[1].PerformClick(); menu.Close();
        Check(coil.Ovr == 1 && pdf.Assigned(b) == coil, "explicit candidate and defect apply only chosen element");
        pdf.Section = 2; Check(pdf.Resolve(a).Count == 0, "PDF section filter prevents changing other section"); pdf.Section = 1;
        pdf.FindNext("UNBOUND"); Check(pdf.Resolve(pdf.FocusedHit).Count == 0, "unbound PDF label has no synthetic model operation");
        pdf.Zoom(1000, Point.Empty); Check(pdf.ImageScale == 3, "PDF zoom reaches and clamps 300 percent");
        pdf.Zoom(0.00001f, Point.Empty); Check(pdf.ImageScale >= 0.01f, "PDF minimum zoom remains bounded");
        atlas.modelSignature = "different-model"; File.WriteAllText(Path.Combine(directory, "atlas.json"), json.Serialize(atlas));
        view.LoadOriginalAtlas(directory); Check(pdf.BindingError != null && pdf.Resolve(pdf.AllHits.First()).Count == 0, "changed model signature disables all stale ids");
        // Real supplied assets, when available, are also rendered for visual inspection.
        string real = Path.GetFullPath(Path.Combine(output, "..", "assets", "scheme008"));
        if (File.Exists(Path.Combine(real, "atlas.json")))
        {
            view.LoadOriginalAtlas(real); Check(pdf.Available && pdf.BindingError == null, "supplied PDF atlas matches current model signature");
            for (int i = 0; i < pdf.Atlas.pages.Length; i++) { pdf.SetPage(i); pdf.Fit(); Application.DoEvents(); Snap(view, Path.Combine(output, "scheme_original_" + (i + 1).ToString("00") + ".png")); }
            pdf.FindNext("КВ"); Snap(view, Path.Combine(output, "scheme_original_search.png"));
            model.ClearOverrides();
            var realSymbol = pdf.AllHits.First(h => h.binding == "symbol" && pdf.Resolve(h).Count == 1 && pdf.Resolve(h)[0].IsLoad);
            pdf.Focus(realSymbol); var exact = pdf.Assigned(realSymbol); rect = pdf.HitBounds(realSymbol); point = Point.Round(new PointF(rect.X + rect.Width / 2, rect.Y + rect.Height / 2));
            Check(exact != null && pdf.Hit(point) == realSymbol, "real PDF symbol hit selects unique section element");
            typeof(OriginalSchemeCanvas).GetMethod("OnMouseDown", Instance).Invoke(pdf, new object[] { new MouseEventArgs(MouseButtons.Left, 1, point.X, point.Y, 0) });
            Check(exact.Ovr == 1 && model.Overrides == 1, "real PDF symbol click introduces exactly one graph defect");
            Application.DoEvents(); Snap(view, Path.Combine(output, "scheme_original_click.png")); model.ClearOverrides();
            var realLabel = pdf.AllHits.First(h => h.binding == "label" && pdf.Resolve(h).Count > 0);
            pdf.Focus(realLabel); rect = pdf.HitBounds(realLabel); point = Point.Round(new PointF(rect.X + rect.Width / 2, rect.Y + rect.Height / 2));
            typeof(OriginalSchemeCanvas).GetMethod("OnMouseDown", Instance).Invoke(pdf, new object[] { new MouseEventArgs(MouseButtons.Left, 1, point.X, point.Y, 0) });
            Check(model.Overrides == 0 && pdf.Assigned(realLabel) == null, "real PDF label click requires explicit menu choice");
            menu = Field<ContextMenuStrip>(pdf, "menu"); Check(menu.Items.Count == pdf.Resolve(realLabel).Count, "real PDF menu enumerates model candidates exactly"); menu.Close();
            var sandLabels = pdf.AllHits.Where(h => h.page == 5 && h.label == "2КП1" && h.kind == "coil").OrderBy(h => h.y).ToList();
            Check(sandLabels.Count == 2, "original PDF preserves both duplicated 2KP1 captions");
            var upperSand = sandLabels[0]; var lowerSand = sandLabels[1];
            Check(lowerSand.binding == "unverified" && Math.Abs(lowerSand.y - 0.1685) < 0.005 && lowerSand.modelIds.Length == 4, "lower branch 1651 caption remains explicitly unverified");
            for (int section = 1; section <= 2; section++)
            {
                pdf.Section = section;
                Check(pdf.Assigned(lowerSand) == null, "lower duplicated caption has no automatic assignment in section " + section);
                Check(upperSand.binding == "symbol" && pdf.Assigned(upperSand) != null && pdf.Assigned(upperSand).Key == "2КП1@" + section, "upper 2KP1 symbol retains exact assignment in section " + section);
            }
            pdf.Section = 1;
        }
    }
}
