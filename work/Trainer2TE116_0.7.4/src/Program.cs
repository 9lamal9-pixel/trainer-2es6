// Entry: the instructor window, or a run without a window for the benches:
//   Trainer2TE116.exe                                   instructor window
//   Trainer2TE116.exe [--pult ip:port|off] [--pult-local 2001] [--pult-swap 1|0] [--data <dir>]
//                     [--start --scenario <json> --train <spec>] [--speed 1|2|5|10] [--hidden]
//       pult of the cab (default: Data\logic_config.json of the trainer), folder of own trains and trips, a trip started
//       at once (used by the native pult test, test\PultTest.cs), the window not shown (tests).
//   Trainer2TE116.exe --run --scenario <json> --train builtin:<n>|db:<id>|<file.json> --out <dir> [--db <Program_data.db>] [--cruise 75]
// The run drives the whole route with the automatic driver, writes the cassette, its CSV, VU-45, DU-61, the protocol and the
// tape, and prints a summary line ("RUN ...") for the benches.
// Standalone build (package.ps1, installed from a flash drive): settings.json beside the exe holds the folder of the trainer
// (release_dir) and the pult (pult: auto | off | ip:port, pult_local, pult_swap); the command line wins over it. The folder of
// the trainer is found by: --release, settings.json, the folders around the exe, the running instructor.exe, the folder of
// the development machine; else the instructor is asked once and the answer saved. The window refuses a second copy, warns
// while instructor.exe holds the pult, and writes unexpected errors to data\errors.log.
// Build without the checking code (csc /define:NOTEST, package.ps1 -NoTestCode): the keys of the benches --run, --start,
// --scenario, --train, --speed, --hidden, --out, --db, --cruise are ignored, the bench run is not compiled and the folder
// of the development machine is not searched.
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Windows.Forms;

[assembly: System.Reflection.AssemblyTitle("Тренажёр 2ТЭ116 — программа инструктора")]
[assembly: System.Reflection.AssemblyProduct("Trainer2TE116")]
[assembly: System.Reflection.AssemblyVersion("0.7.4.0")]
[assembly: System.Reflection.AssemblyFileVersion("0.7.4.0")]

namespace Trainer2TE116
{
    static class Program
    {
        public const string Version = "0.7.4 (15.11.2026)";
        public static Dictionary<string, string> Arguments = new Dictionary<string, string>();
        public static string ReleaseDir = "";
#if !NOTEST
        const string DevRelease = @"C:\Users\sibir\Desktop\Макрос\Тренажер\фантомас\work\Release";
#endif
        public static string BaseDir { get { return AppDomain.CurrentDomain.BaseDirectory; } }
        public static string SettingsPath { get { return Path.Combine(BaseDir, "settings.json"); } }
        public static string DataDir { get { return Arguments.ContainsKey("data") ? Arguments["data"] : Path.Combine(BaseDir, "data"); } }

        [STAThread]
        static int Main(string[] args)
        {
            var a = Args(args); Arguments = a;
#if NOTEST
            foreach (var k in new[] { "run", "start", "scenario", "train", "speed", "hidden", "out", "db", "cruise" }) a.Remove(k);
#endif
            var settings = LoadSettings();
            // settings.json under the command line
            foreach (var kv in new[] { new[] { "pult", "pult" }, new[] { "pult_local", "pult-local" }, new[] { "pult_swap", "pult-swap" }, new[] { "video", "video" }, new[] { "blok", "blok" }, new[] { "road", "road" }, new[] { "diesel_condition", "condition" } })
                if (!a.ContainsKey(kv[1]) && settings.ContainsKey(kv[0]))
                {
                    string v = Convert.ToString(settings[kv[0]], CultureInfo.InvariantCulture);
                    if (v.Length > 0 && v != "auto") a[kv[1]] = v;
                }
            if (!a.ContainsKey("data") && settings.ContainsKey("data_dir") && Convert.ToString(settings["data_dir"]).Length > 0) a["data"] = Convert.ToString(settings["data_dir"]);
            ReleaseDir = a.ContainsKey("release") ? a["release"] : FindRelease(settings);
#if !NOTEST
            if (a.ContainsKey("run")) return Run(a);
#endif
            AppLog.Open(Path.Combine(DataDir, "logs"), a.ContainsKey("log") ? a["log"] : settings.ContainsKey("log") ? Convert.ToString(settings["log"], CultureInfo.InvariantCulture) : "on");
            LogStart(args, settings);

            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            bool test = a.ContainsKey("hidden");
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);
            Application.ThreadException += (s, e) => Crash(e.Exception, test);
            AppDomain.CurrentDomain.UnhandledException += (s, e) => Crash(e.ExceptionObject as Exception, true);

            System.Threading.Mutex one = null;
            if (!test)
            {
                bool created;
                one = new System.Threading.Mutex(true, "Trainer2TE116_instructor", out created);
                if (!created) { AppLog.Write("ПРОГРАММА", "уже запущена другая копия программы — выход"); MessageBox.Show("Программа инструктора 2ТЭ116 уже запущена.", "Тренажёр 2ТЭ116", MessageBoxButtons.OK, MessageBoxIcon.Information); return 0; }
                if (!IsTrainerDir(ReleaseDir))
                {
                    ReleaseDir = AskRelease();
                    AppLog.Write("ПРОГРАММА", "папка тренажёра указана вручную: " + (ReleaseDir ?? "отказ, выход"));
                    if (ReleaseDir == null) return 0;
                    settings["release_dir"] = ReleaseDir; SaveSettings(settings);
                }
                while (Process.GetProcessesByName("instructor").Length > 0)
                {
                    var r = MessageBox.Show("Запущена прежняя программа инструктора (instructor.exe). Она занимает пульт кабины (порт 2001).\n\n" +
                        "Закройте её и нажмите «Повторить».\n«Пропустить» — открыть новую программу без пульта.\n«Прервать» — выйти.",
                        "Тренажёр 2ТЭ116", MessageBoxButtons.AbortRetryIgnore, MessageBoxIcon.Warning);
                    AppLog.Write("ПРОГРАММА", "запущена прежняя программа instructor.exe (держит порт пульта 2001); ответ инструктора: " + (r == DialogResult.Abort ? "выход" : r == DialogResult.Ignore ? "работать без пульта" : "повторить"));
                    if (r == DialogResult.Abort) return 0;
                    if (r == DialogResult.Ignore) { a["pult"] = "off"; break; }
                }
            }
            Application.Run(new MainForm());
            AppLog.Write("ПРОГРАММА", "работа закончена"); AppLog.Close();
            GC.KeepAlive(one);
            return 0;
        }

        // ---- settings and the folder of the trainer
        public static bool IsTrainerDir(string d)
        {
            try { return !string.IsNullOrEmpty(d) && Directory.Exists(Path.Combine(d, "RTS", "scenaries")); } catch { return false; }
        }
        public static Dictionary<string, object> LoadSettings()
        {
            try
            {
                if (File.Exists(SettingsPath))
                    return new System.Web.Script.Serialization.JavaScriptSerializer().Deserialize<Dictionary<string, object>>(File.ReadAllText(SettingsPath, System.Text.Encoding.UTF8)) ?? new Dictionary<string, object>();
            }
            catch { }
            return new Dictionary<string, object>();
        }
        public static void SaveSettings(Dictionary<string, object> s)
        {
            try
            {
                var json = new System.Web.Script.Serialization.JavaScriptSerializer().Serialize(s).Replace(",\"", ",\r\n  \"").Replace("{\"", "{\r\n  \"").Replace("}", "\r\n}");
                File.WriteAllText(SettingsPath, json, new System.Text.UTF8Encoding(false));
            }
            catch { }
        }
        static string FindRelease(Dictionary<string, object> settings)
        {
            var c = new List<string>();
            if (settings.ContainsKey("release_dir")) c.Add(Convert.ToString(settings["release_dir"]));
            var b = BaseDir.TrimEnd('\\');
            c.Add(b); c.Add(Path.Combine(b, "..")); c.Add(Path.Combine(b, "..", "Release")); c.Add(Path.Combine(b, "..", "work", "Release")); c.Add(Path.Combine(b, "..", "..", "work", "Release"));
            try { foreach (var p in Process.GetProcessesByName("instructor")) { try { c.Add(Path.GetDirectoryName(p.MainModule.FileName)); } catch { } } } catch { }
#if !NOTEST
            c.Add(DevRelease);
#endif
            foreach (var d in c) if (IsTrainerDir(d)) return Path.GetFullPath(d);
            return "";
        }
        static string AskRelease()
        {
            while (true)
            {
                using (var dlg = new FolderBrowserDialog { Description = "Укажите папку тренажёра 2ТЭ116: в ней instructor.exe и папка RTS (обычно ...\\work\\Release). Программа только читает из неё сценарии и составы.", ShowNewFolderButton = false })
                {
                    if (dlg.ShowDialog() != DialogResult.OK) return null;
                    if (IsTrainerDir(dlg.SelectedPath)) return dlg.SelectedPath;
                    var up = Path.Combine(dlg.SelectedPath, "work", "Release");
                    if (IsTrainerDir(up)) return up;
                    MessageBox.Show("В этой папке нет RTS\\scenaries. Выберите папку, где лежит instructor.exe.", "Тренажёр 2ТЭ116", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                }
            }
        }
        // the head of the journal: what was started, with which keys and settings, where the data are, the addresses of the computer
        static void LogStart(string[] args, Dictionary<string, object> settings)
        {
            if (!AppLog.Enabled) return;
            try
            {
                AppLog.Write("ПРОГРАММА", "Тренажёр 2ТЭ116, программа инструктора, версия " + Version
#if NOTEST
                    + ", сборка без проверочного кода"
#endif
                    + "; журнал " + AppLog.FilePath + (AppLog.Raw ? " (режим raw: пакеты пульта в hex)" : ""));
                AppLog.Write("ПРОГРАММА", "файл " + Application.ExecutablePath + "; ключи запуска: " + (args.Length > 0 ? string.Join(" ", args) : "(нет)"));
                var sb = new System.Text.StringBuilder();
                foreach (var kv in settings) sb.Append(sb.Length > 0 ? ", " : "").Append(kv.Key).Append('=').Append(Convert.ToString(kv.Value, CultureInfo.InvariantCulture));
                AppLog.Write("ПРОГРАММА", "settings.json (" + SettingsPath + "): " + (sb.Length > 0 ? sb.ToString() : "(нет или пуст)"));
                AppLog.Write("ПРОГРАММА", "папка тренажёра: " + (IsTrainerDir(ReleaseDir) ? ReleaseDir : "не найдена (" + ReleaseDir + ")") + "; папка данных: " + DataDir);
                var logic = Path.Combine(ReleaseDir ?? "", "Data", "logic_config.json");
                AppLog.Write("ПРОГРАММА", "logic_config.json: " + (File.Exists(logic) ? logic + ": " + File.ReadAllText(logic).Replace("\r", "").Replace("\n", " ").Replace("\t", " ") : "нет файла " + logic));
                AppLog.Write("ПРОГРАММА", "Windows " + Environment.OSVersion.Version + (Environment.Is64BitOperatingSystem ? " x64" : " x86") + ", .NET " + Environment.Version + ", процессоров " + Environment.ProcessorCount + ", компьютер " + Environment.MachineName);
                sb.Length = 0;
                foreach (var ni in System.Net.NetworkInformation.NetworkInterface.GetAllNetworkInterfaces())
                {
                    if (ni.NetworkInterfaceType == System.Net.NetworkInformation.NetworkInterfaceType.Loopback) continue;
                    string ips = "";
                    foreach (var ua in ni.GetIPProperties().UnicastAddresses)
                        if (ua.Address.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork) ips += (ips.Length > 0 ? ", " : "") + ua.Address;
                    if (ips.Length == 0) continue;
                    sb.Append(sb.Length > 0 ? "; " : "").Append(ni.Name).Append(" (").Append(ni.OperationalStatus == System.Net.NetworkInformation.OperationalStatus.Up ? "включён" : "выключен").Append("): ").Append(ips);
                }
                AppLog.Write("ПРОГРАММА", "сетевые адреса компьютера: " + (sb.Length > 0 ? sb.ToString() : "нет"));
            }
            catch (Exception ex) { AppLog.Write("ПРОГРАММА", "начало журнала записано не полностью: " + ex.Message); }
        }

        static void Crash(Exception ex, bool quiet)
        {
            if (ex == null) return;
            AppLog.Write("ОШИБКА", ex.ToString());
            try { Directory.CreateDirectory(DataDir); File.AppendAllText(Path.Combine(DataDir, "errors.log"), DateTime.Now.ToString("s") + "  " + Version + "\r\n" + ex + "\r\n\r\n", System.Text.Encoding.UTF8); } catch { }
            if (!quiet) MessageBox.Show("Ошибка программы: " + ex.Message + "\n\nПодробности записаны в data\\errors.log. Поездка и кассета продолжают писаться.", "Тренажёр 2ТЭ116", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }

        static Dictionary<string, string> Args(string[] args)
        {
            var d = new Dictionary<string, string>();
            for (int i = 0; i < args.Length; i++)
                if (args[i].StartsWith("--")) { string k = args[i].Substring(2); string v = i + 1 < args.Length && !args[i + 1].StartsWith("--") ? args[++i] : "1"; d[k] = v; }
            return d;
        }

        public static string DbPath { get { return Path.Combine(ReleaseDir, "RTS", "database", "Program_data.db"); } }
        public static string ScenarioDir { get { return Path.Combine(ReleaseDir, "RTS", "scenaries"); } }

        public static Train MakeTrain(string spec, string db)
        {
            if (spec.StartsWith("builtin:")) return Catalog.BuiltIn()[int.Parse(spec.Substring(8), CultureInfo.InvariantCulture)];
            if (spec.StartsWith("db:")) { Catalog.LoadTypes(db); return Catalog.FromDatabase(db, spec.Substring(3)); }
            return Train.Load(spec);
        }

        // start of the head: the start coordinate of the scenario, at least the length of the train from the start of the profile
        public static double StartHead(Route r, Train t) { return Math.Max(r.StartX, t.LengthM + 60); }

        // The route of the video system for a scenario, as the launcher of the original gives it (markup_index = the area):
        // the root markup (RTS\scenaries\root\N.json) with the same end stations, else the area of the scenario in the
        // database, else -1 (the video has no such route).
        public static int RoadOf(string scenario, Route r)
        {
            try
            {
                int best = -1, bestScore = -1;
                string root = Path.Combine(Path.GetDirectoryName(scenario), "root");
                if (Directory.Exists(root) && r.StationName.Length >= 2)
                    foreach (var f in Directory.GetFiles(root, "*.json"))
                    {
                        int n; if (!int.TryParse(Path.GetFileNameWithoutExtension(f), NumberStyles.None, CultureInfo.InvariantCulture, out n)) continue;
                        Route q; try { q = Route.LoadScenario(f); } catch { continue; }
                        if (q.StationName.Length < 2 || q.StationName[0] != r.StationName[0] || q.StationName[q.StationName.Length - 1] != r.StationName[r.StationName.Length - 1]) continue;
                        int score = (Math.Abs(q.Length - r.Length) < 1 ? 2 : 0) + (q.Lights.Count == r.Lights.Count ? 1 : 0);
                        if (score > bestScore || (score == bestScore && n < best)) { best = n; bestScore = score; }
                    }
                if (best >= 0) return best;
                return Catalog.ScenarioArea(DbPath, Path.GetFileName(scenario));
            }
            catch { return -1; }
        }

#if !NOTEST
        static int Run(Dictionary<string, string> a)
        {
            // the program is a window application: the output of a run goes to its own UTF-8 stream (redirected by the benches)
            try { Console.SetOut(new StreamWriter(Console.OpenStandardOutput(), new System.Text.UTF8Encoding(false)) { AutoFlush = true }); } catch { }
            var sw = Stopwatch.StartNew();
            var route = Route.LoadScenario(a["scenario"]);
            string db = a.ContainsKey("db") ? a["db"] : DbPath;
            var train = MakeTrain(a.ContainsKey("train") ? a["train"] : "builtin:0", db);
            string outDir = a.ContainsKey("out") ? a["out"] : "run";
            Directory.CreateDirectory(outDir);
            foreach (var l in route.Lights) l.Aspect = 0;           // the instructor opens the way for the bench run
            var sim = new Simulation { DieselCondition = a.ContainsKey("condition") ? a["condition"] : "normal", DieselSeed = 1, Student = "Автомашинист (стенд)", Instructor = "—", TrainNumber = train.Number };
            Journal.Configure(outDir);
            sim.Start(route, train, StartHead(route, train), true);
            sim.Cassette = new Cassette(Path.Combine(outDir, "trip.kr2"), new Dictionary<string, object> { { "route", route.Name }, { "train", train.ToString() }, { "start", DateTime.Now.ToString("s") } });
            var drv = new AutoDriver();
            if (a.ContainsKey("cruise")) drv.Cruise = double.Parse(a["cruise"], CultureInfo.InvariantCulture);
            double nextKm = 0, maxBalance = 0;
            while (!drv.Done && !sim.Finished && sim.T < 8 * 3600)
            {
                drv.Step(sim);
                sim.Step(0.1);
                maxBalance = Math.Max(maxBalance, Math.Abs(sim.Air.Balance()));
                if (sim.Distance / 1000 >= nextKm) { Console.WriteLine(string.Format(CultureInfo.InvariantCulture, "  {0,6:0.0} km  t {1,6:0} s  v {2,5:0.0}  {3}", sim.Distance / 1000, sim.T, sim.SpeedKmh, drv.State)); nextKm += 20; }
            }
            sim.Cassette.Dispose();
            var rec = Cassette.Read(Path.Combine(outDir, "trip.kr2"));
            Cassette.ExportCsv(rec, Path.Combine(outDir, "trip.csv"));
            var chk = Documents.Measure(train);
            Documents.Save(outDir, "vu45.html", Documents.Vu45(train, sim.TrainNumber, chk));
            Documents.Save(outDir, "du61.html", Documents.Du61(route, sim.TrainNumber, 0));
            Documents.Save(outDir, "protocol.html", Documents.Protocol(sim, 1));
            Documents.Save(outDir, "tape.html", Documents.Tape(rec, route));
            double mt = sim.Dyn.Cp.Length > 0 ? sim.Dyn.Cp.Max(c => c.MaxTension) / 1000 : 0, mc = sim.Dyn.Cp.Length > 0 ? sim.Dyn.Cp.Max(c => c.MaxCompression) / 1000 : 0;
            List<JournalEntry> list; lock (Journal.Entries) list = Journal.Entries.ToList();
            Console.WriteLine(string.Format(CultureInfo.InvariantCulture,
                "RUN train=\"{0}\" done={1} stop_km={2:0.000} time_s={3:0} dist_km={4:0.0} fuel_kg={5:0} max_tension_kn={6:0} max_compression_kn={7:0} violations={8} points={9} codes={10} brake_tests={11} air_balance_kg={12:0.0000} density_s={13:0} tail_release_s={14:0} blocks={15} bad_blocks={16} wall_s={17:0}",
                train.Name, drv.Done, sim.Dyn.HeadX / 1000, sim.T, sim.Distance / 1000, sim.Tr.FuelKg, mt, mc, list.Count, list.Sum(e => e.Points), string.Join(",", list.Select(e => e.Code).ToArray()),
                BrakeTest.Satisfactory + "/" + (BrakeTest.Satisfactory + BrakeTest.Unsatisfactory + BrakeTest.Skipped), maxBalance, chk.DensityS, chk.ReleaseTailS, rec.Blocks, rec.BadBlocks, sw.Elapsed.TotalSeconds));
            foreach (var e in list) Console.WriteLine("  VIOLATION " + e.Code + " " + e.Clock + " " + e.Place + " v=" + e.V.ToString("0", CultureInfo.InvariantCulture) + ": " + e.Title + " (" + e.Detail + ")");
            return 0;
        }
#endif
    }
}
