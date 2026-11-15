// Smoke test of the instructor window: build MainForm (shown transparent), start a trip on the selected route and train,
// run it through the window loop with the automatic driver of the tests (not in the window: it steps the controls before
// every tick), the pult off, add a warning, change a signal, put a fault, make the documents,
// and take a picture of every section (out\ui_<n>.png). Any exception fails.
// Build: csc /target:winexe /r:Trainer2TE116.exe UiSmoke.cs; run: UiSmoke.exe <outdir> <folder of the trainer>
using System;
using System.Drawing;
using System.IO;
using System.Reflection;
using System.Windows.Forms;
using Trainer2TE116;

static class UiSmoke
{
    static volatile string stage = "start";
    [STAThread]
    static int Main(string[] args)
    {
        try { Console.OutputEncoding = System.Text.Encoding.UTF8; } catch { }
        string outDir = args.Length > 0 ? args[0] : ".";
        // a watchdog: a hang (a modal message of the window, an endless loop) fails the test with the stage it stopped at
        new System.Threading.Thread(() => { System.Threading.Thread.Sleep(60000); Console.WriteLine("FAIL timeout 60 s at the stage: " + stage); Console.Out.Flush(); Environment.Exit(2); }) { IsBackground = true }.Start();
        try
        {
            // the pult of the cab off for this test (the native pult test is test\PultTest.cs)
            var progArgs = (System.Collections.Generic.Dictionary<string, string>)typeof(MainForm).Assembly.GetType("Trainer2TE116.Program").GetField("Arguments").GetValue(null);
            progArgs["pult"] = "off"; progArgs["video"] = "off"; progArgs["blok"] = "off"; progArgs["hidden"] = "1"; progArgs["data"] = System.IO.Path.Combine(outDir, "data");
            // folder of the trainer: the second argument (the window has no fixed path in the standalone build)
            if (args.Length > 1) typeof(MainForm).Assembly.GetType("Trainer2TE116.Program").GetField("ReleaseDir").SetValue(null, args[1]);
            stage = "window"; var f = new MainForm();
            f.Size = new Size(1600, 960); f.ShowInTaskbar = false; f.Opacity = 0; f.StartPosition = FormStartPosition.Manual; f.Location = new Point(0, 0);
            f.Show(); Application.DoEvents();
            var t = typeof(MainForm);
            const BindingFlags all = BindingFlags.NonPublic | BindingFlags.Instance;
            Func<string, object> get = n => t.GetField(n, all).GetValue(f);
            Action<string, object[]> call = (n, a) => t.GetMethod(n, all).Invoke(f, a);
            // the test steps the window itself: with the timer of the window running, a loaded computer never empties the
            // message queue in DoEvents
            ((System.Windows.Forms.Timer)get("timer")).Stop();
            Snap(f, Path.Combine(outDir, "ui_6_trip.png"));
            stage = "start of the trip"; call("StartTrip", null);
            var sim = (Simulation)get("sim");
            if (sim == null) { Console.WriteLine("FAIL trip not started"); return 1; }
            if (t.GetField("autoOn", all) != null || t.GetField("btAuto", all) != null) { Console.WriteLine("FAIL the automatic driver is still in the window"); return 1; }
            stage = "ticks"; var drv = new AutoDriver();
            for (int i = 0; i < 900; i++) { drv.Step(sim); call("Tick", null); if (i % 100 == 0) Application.DoEvents(); }
            var route = (Route)get("route"); var train = (Train)get("train");
            call("Warning", new object[] { sim.Dyn.HeadX + 2000, 1000.0, 40 });
            int nl = route.NextLight(sim.Dyn.HeadX); if (nl >= 0) call("SetAspect", new object[] { nl, 2 });
            sim.SetFault(sim.Faults[12], true);
            for (int i = 0; i < 300; i++) { drv.Step(sim); call("Tick", null); }
            call("SyncCab", null);   // the handles of the cab as the driver of the test left them
            stage = "pages"; string[] names = { "map", "loco", "train", "air", "control", "journal" };
            for (int k = 0; k < 6; k++)
            {
                call("Show", new object[] { k }); sim.Paused = true; call("Tick", null); Application.DoEvents();
                Snap(f, Path.Combine(outDir, "ui_" + k + "_" + names[k] + ".png"));
                sim.Paused = false;
            }
            stage = "original scheme page";
            call("Show", new object[] { 7 }); sim.Paused = true; call("Tick", null); Application.DoEvents();
            Snap(f, Path.Combine(outDir, "ui_7_scheme.png")); sim.Paused = false;
            stage = "documents"; var tripDir = (string)get("tripDir");
            Documents.Save(tripDir, "vu45.html", Documents.Vu45(train, sim.TrainNumber, Documents.Measure(train)));
            Documents.Save(tripDir, "du61.html", Documents.Du61(route, sim.TrainNumber, 0));
            Documents.Save(tripDir, "protocol.html", Documents.Protocol(sim, 1));
            Console.WriteLine(string.Format(System.Globalization.CultureInfo.InvariantCulture, "PASS window: 8 sections drawn, trip {0}, {1:0} s, head {2}, {3:0.0} km/h, documents in {4}", route.Name, sim.T, route.KmPk(sim.Dyn.HeadX), sim.SpeedKmh, tripDir));
            f.Dispose();
            return 0;
        }
        catch (Exception ex) { Console.WriteLine("FAIL " + (ex.InnerException ?? ex)); return 1; }
    }
    static void Snap(Form f, string path) { using (var b = new Bitmap(f.Width, f.Height)) { f.DrawToBitmap(b, new Rectangle(0, 0, f.Width, f.Height)); b.Save(path); } }
}
