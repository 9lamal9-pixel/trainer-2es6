// Native test of the instructor with the cab pult emulated over UDP, the way the real pult talks to the program:
//  - the emulator binds a free UDP port on 127.0.0.1 as the pult and sends the 48 cells every 25 ms to the program;
//  - Trainer2TE116.exe is started as its own process with that pult (--pult 127.0.0.1:<port> --pult-local <port>), a trip
//    started from the command line, the window transparent, speed x5, its folder of trips under <out>\pult_test;
//  - the driver's actions go only through the cells of the pult: EPK key, 254, reverser, whistle, controller, crane 395
//    (II, V, IV, I, II), the EPK key off in motion, emergency stop and START of the diesels, fuel pump off; every reaction is read back from the 43 bytes the program sends to the
//    pult (needles of TM, UR, GR, TC, generator current, lamps "braked" and "load shedding") and from the cassette of the trip;
//  - a damaged (short) packet is sent on the way and must be ignored;
//  - the video system and BLOK are emulated too (TCP listener and UDP socket on 127.0.0.1): the video must get the start
//    with the route index, every signal and sign of the scenario, the position of the train, the sounds and the stop at
//    the end; BLOK must get its 146-byte packets (pressures, speed, signal ahead), its lamp "attention" must reach the
//    pult, and its holding coil taken off must make the EPK whistle and trip after 7 s;
//  - the window is closed as by the instructor (WM_CLOSE): the cassette must close with good CRC, hold the handles as the
//    pult moved them, the events "Пульт кабины: на связи" and the EPK braking, and the journal the violation O4.
// Build: csc /platform:x64 /r:Trainer2TE116.exe PultTest.cs; run: PultTest.exe <Trainer2TE116.exe> <scenario.json|auto> <outdir>
// (auto: the route 1_1.json of the trainer named in settings.json beside the program; the check of an installed build)
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Trainer2TE116;

static class PultTest
{
    static readonly CultureInfo C = CultureInfo.InvariantCulture;
    static readonly object gate = new object();
    static readonly byte[] cells = new byte[Pult.InSize];
    static byte[] lastOut; static int outCount, badOut; static volatile bool stop;
    static UdpClient sock; static IPEndPoint program;
    static int pass, fail; static StringBuilder report = new StringBuilder();
    static Process proc; static string dataDir;
    const double Kgf = 10.197162;

    static int Main(string[] args)
    {
        string exe = Path.GetFullPath(args[0]), outDir = Path.GetFullPath(args.Length > 2 ? args[2] : ".");
        string scenario = args.Length > 1 && args[1] != "auto" ? Path.GetFullPath(args[1]) : ScenarioFromSettings(exe);
        if (scenario == null) { Console.WriteLine("FAIL no scenario: settings.json beside the program has no folder of the trainer (release_dir)"); return 1; }
        dataDir = Path.Combine(outDir, "pult_test");
        if (Directory.Exists(dataDir)) Directory.Delete(dataDir, true);
        Directory.CreateDirectory(dataDir);
        Console.OutputEncoding = Encoding.UTF8;
        var sw = Stopwatch.StartNew();
        try
        {
            sock = new UdpClient(new IPEndPoint(IPAddress.Loopback, 0));
            try { sock.Client.IOControl(-1744830452, new byte[] { 0 }, null); } catch { }
            int pultPort = ((IPEndPoint)sock.Client.LocalEndPoint).Port, progPort;
            using (var probe = new UdpClient(new IPEndPoint(IPAddress.Loopback, 0))) progPort = ((IPEndPoint)probe.Client.LocalEndPoint).Port;
            program = new IPEndPoint(IPAddress.Loopback, progPort);
            Line("pult emulator 127.0.0.1:" + pultPort + ", program port " + progPort);
            new Thread(Rx) { IsBackground = true }.Start();
            // emulators of the video system (TCP) and of BLOK (UDP)
            videoListener = new TcpListener(IPAddress.Loopback, 0); videoListener.Start();
            int videoPort = ((IPEndPoint)videoListener.LocalEndpoint).Port;
            new Thread(VideoServer) { IsBackground = true }.Start();
            blokSock = new UdpClient(new IPEndPoint(IPAddress.Loopback, 0));
            try { blokSock.Client.IOControl(-1744830452, new byte[] { 0 }, null); } catch { }
            int blokPort = ((IPEndPoint)blokSock.Client.LocalEndPoint).Port;
            new Thread(BlokServer) { IsBackground = true }.Start();
            Line("video emulator 127.0.0.1:" + videoPort + ", BLOK emulator 127.0.0.1:" + blokPort);

            string argsLine = string.Format(C, "--pult 127.0.0.1:{0} --pult-local {1} --pult-swap 1 --video 127.0.0.1:{4} --video-pause 200 --road 3 --blok 127.0.0.1:{5} --data \"{2}\" --start --scenario \"{3}\" --train builtin:0 --speed 10 --hidden --student \"Тест пульта\" --number 3001",
                pultPort, progPort, dataDir, scenario, videoPort, blokPort);
            proc = Process.Start(new ProcessStartInfo(exe, argsLine) { UseShellExecute = false, WorkingDirectory = Path.GetDirectoryName(exe) });
            Line("started " + Path.GetFileName(exe) + " pid " + proc.Id);

            // 1. the program sends to the pult before it hears from it
            Check("program sends 43-byte packets to the pult", WaitFor(30, () => outCount > 5), "packets " + outCount + ", wrong length " + badOut);
            Check("start of the trip without error", !File.Exists(Path.Combine(dataDir, "start_error.txt")) && !proc.HasExited, proc.HasExited ? "exited with " + proc.ExitCode : "");

            WaitFor(20, () => videoRoad == 3 && videoLights.Count > 0);     // the video is started (3 s + 3 s) before the trip goes on
            // 2. the pult comes on line: EPK on, reverser neutral, controller 0, crane II, 254 in 6, spotlight, control and motion on
            Set(Pult.CEpk, 1); Set(Pult.CReverser, 2); Set(Pult.CController, 0); Set(Pult.CCrane, 2); Set(Pult.CKvtAnalog, 90);   // reverse handle forward: the lamps are fed through it
            Set(Pult.CSpotBright, 1); Set(Pult.CLocoControl, 1); Set(Pult.CMotion, 1); Set(Pult.CConversion, 1); Set(Pult.CRefrigerator, 1);
            new Thread(Tx) { IsBackground = true }.Start();
            // 2a. the fuel pump toggles of the pult are off in its first packet: the trip begins with the diesels not started;
            //     pumps on and START: oil pumping 60 s, cranking 12 s
            Check("pult with the fuel pumps off: the trip begins with both diesels stopped", WaitFor(15, () => Oil(Pult.NOil1) < 0.3 && Oil(Pult.NOil23) < 0.3 && !Lamp(Pult.LDiesel2)), "oil " + Oil(Pult.NOil1).ToString("0.0", C) + " / " + Oil(Pult.NOil23).ToString("0.0", C));
            Set(Pult.CPump1, 1); Set(Pult.CPump2, 1); Thread.Sleep(300);
            Set(Pult.CLaunch1, 1); Set(Pult.CLaunch2, 1); Thread.Sleep(250); Set(Pult.CLaunch1, 0); Set(Pult.CLaunch2, 0);
            Check("pumps on and START from the pult: both diesels run", Fast(60, () => Oil(Pult.NOil1) > 2 && Oil(Pult.NOil23) > 2 && Lamp(Pult.LDiesel2)), "oil " + Oil(Pult.NOil1).ToString("0.0", C) + " / " + Oil(Pult.NOil23).ToString("0.0", C));
            Check("TM charged, read on the GR needle (pult wired GR <-> TM)", WaitFor(30, () => Near(Tm(), 0.50, 0.03)), Show());
            Check("UR charged", Near(Ur(), 0.50, 0.03), Show());
            Check("GR (on the TM needle) 0.70-0.95 MPa", Gr() >= 0.70 && Gr() <= 0.95, Show());
            Check("254 in 6: TC of the locomotive above 0.30 and lamp 'braked'", WaitFor(40, () => Tc() > 0.30 && Lamp(Pult.LBraked)), Show());
            Check("idle: oil pressure of both diesels 2-4 kgf/cm2, lamp 'diesel 2', no 'load shedding' at 0", Oil(Pult.NOil1) > 2 && Oil(Pult.NOil23) > 2 && Lamp(Pult.LDiesel2) && !Lamp(Pult.LShed1), "oil " + Oil(Pult.NOil1).ToString("0.0", C) + " / " + Oil(Pult.NOil23).ToString("0.0", C));

            // 3. 254 to the train position: the locomotive releases
            Set(Pult.CKvtAnalog, 148);
            Check("254 in 2: TC of the locomotive released, lamp off", WaitFor(40, () => Tc() < 0.02 && !Lamp(Pult.LBraked)), Show());

            // 4. moving off: whistle, reverser forward, controller up to 6
            Set(Pult.CWhistle, 1); Thread.Sleep(250); Set(Pult.CWhistle, 0);
            Set(Pult.CReverser, 2); Thread.Sleep(250);   // the reverse handle first: it is locked while the controller is off zero
            for (int k = 1; k <= 6; k++) { Set(Pult.CController, (byte)k); Thread.Sleep(200); }
            Check("traction: generator current, lamp 'load shedding 1' off", WaitFor(30, () => Needle(Pult.NCurrent) > 20 && !Lamp(Pult.LShed1)), Show());
            // a damaged packet on the way: must be ignored
            sock.Send(new byte[10], 10, program);
            Check("train moves (cassette above 8 km/h)", Fast(240, () => MaxSpeed() > 8), "v max " + MaxSpeed().ToString("0.0", C));
            Check("program alive after the short packet", !proc.HasExited && WaitFor(5, () => outCount > 0), "");

            // 5. service braking: controller 0, crane V until UR is 0.06 lower, then IV (lap)
            Set(Pult.CController, 0); Thread.Sleep(500);
            double ur0 = Ur();
            Set(Pult.CCrane, 5);
            bool staged = WaitFor(30, () => Ur() <= ur0 - 0.06);
            Set(Pult.CCrane, 4);
            Check("crane V: UR lowered by 0.06", staged, Show());
            Check("TM follows UR, locomotive brakes by its distributor", WaitFor(40, () => Tm() <= ur0 - 0.045 && Tc() > 0.05 && Lamp(Pult.LBraked)), Show());
            // UDP input and gauge output are asynchronous. A low TM can already satisfy the check above while the
            // new IV command is still in transit; do not count the last part of V discharge as a leak in IV.
            bool lapApplied = WaitFor(10, () => { var frames = Frames(); return frames.Count > 0 && frames[frames.Count - 1].Crane == 4; });
            double urLap = Ur(); Thread.Sleep(1000);
            Check("crane IV holds UR", lapApplied && Math.Abs(Ur() - urLap) < 0.012, "IV applied " + lapApplied + ", UR " + urLap.ToString("0.000", C) + " -> " + Ur().ToString("0.000", C));

            // 6. release: crane I, then II
            Set(Pult.CCrane, 1);
            bool up = WaitFor(40, () => Tm() >= ur0 - 0.01);
            Set(Pult.CCrane, 2);
            Check("crane I: TM back to the charge", up, Show());
            Check("locomotive released after the train brake", Fast(60, () => Tc() < 0.02), Show());

            // 7. EPK key off in motion: emergency braking; the driver puts the crane to VI at once (instruction 151)
            for (int k = 1; k <= 6; k++) { Set(Pult.CController, (byte)k); Thread.Sleep(200); }      // the circuit assembles only from the 1st position
            Fast(90, () => LastSpeed() > 3);
            Set(Pult.CController, 0); Set(Pult.CEpk, 0);
            Check("EPK off in motion: TM falls below 0.30, lamps 'TM' and 'braked'", WaitFor(30, () => Tm() < 0.30 && Lamp(Pult.LTm) && Lamp(Pult.LBraked)), Show());
            Set(Pult.CCrane, 6);
            Check("crane VI: TM below 0.03, TC of the locomotive full", WaitFor(30, () => Tm() < 0.03 && Tc() > 0.30), Show());
            Check("GR on the TM needle stays high (the needles are swapped)", Gr() - Tm() > 0.4, Show());
            Check("train stops", Fast(240, () => LastSpeed() < 0.1 && FramesAfterStop() > 20), "v " + LastSpeed().ToString("0.0", C));
            // EPK restored at a standstill with the key on and the crane in VI, then the train is charged again in II
            Set(Pult.CEpk, 1); Thread.Sleep(500);
            Set(Pult.CCrane, 2);
            Check("after the EPK: key on, VI, then II recharges TM above 0.45", Fast(120, () => Tm() > 0.45), Show());

            // 8a. the video system: start, signals, signs, position, sounds
            Check("video: start with the route index 3", WaitFor(15, () => videoRoad == 3), "route " + videoRoad);
            Check("video: signals and signs of the scenario sent", WaitFor(20, () => videoLights.Count >= 100 && videoSigns >= 100), videoLights.Count + " signals, " + videoSigns + " signs");
            Check("video: position of the train follows the trip", WaitFor(10, () => videoPositions > 20 && videoMaxV > 5 && videoPos > 200 && videoPos < 2500), string.Format(C, "{0} messages, coordinate {1:0} m, v max {2:0.0} km/h, allowed {3:0}", videoPositions, videoPos, videoMaxV, videoLimit));
            Check("video: sounds of the trip (diesel, whistle, crane V, crane VI)", new[] { 6, 0, 55, 56 }.All(i => videoSoundsEver.Contains(i)), string.Join(",", videoSoundsEver.OrderBy(x => x)));
            // 8b. BLOK: its packets, its lamp to the pult, its holding coil
            Check("BLOK: 146-byte packets with the trip", WaitFor(10, () => blokCount > 20 && blokBad == 0 && BlokF(10) > 0.3 && BlokF(26) > 0.5), string.Format(C, "{0} packets, TM {1:0.000} GR {2:0.000} MPa, signal code {3}, name '{4}', station '{5}'", blokCount, BlokF(10), BlokF(26), BlokB(104), BlokS(121, 18), BlokS(59, 16)));
            Check("BLOK: signal ahead and station named, EK limit 80", BlokS(121, 18).Length > 0 && BlokS(59, 16).Length > 0 && (BlokB(98) << 8 | BlokB(99)) == 80, "");
            blokAttention = 1;
            Check("BLOK lamp 'attention' reaches the pult", WaitFor(10, () => Lamp(Pult.LVigilance)), Show());
            blokAttention = 0;
            // the pipe is charged again after step 7; the holding coil off: whistle 7 s, then the trip
            blokCoil = 0;
            Check("BLOK holding coil off: EPK whistles (video sound 57), then trips and vents TM", WaitFor(30, () => videoSoundsEver.Contains(57) && Tm() < 0.25), Show());
            blokCoil = 1;
            Set(Pult.CCrane, 6); Thread.Sleep(500); Set(Pult.CCrane, 2);      // EPK restored at a standstill in VI
            Check("EPK restored after the coil is back: TM recharges", Fast(120, () => Tm() > 0.40), Show());

            // 8. diesels from the pult: fuel pumps on, emergency stop, START of both sections, pump 2 off
            Set(Pult.CPump1, 1); Set(Pult.CPump2, 1); Thread.Sleep(200);
            Set(Pult.CEmergencyStop, 1); Thread.Sleep(250); Set(Pult.CEmergencyStop, 0);
            Check("emergency button: both diesels stopped (oil below 0.3, lamp 'diesel 2' off), TM vented", Fast(30, () => Oil(Pult.NOil1) < 0.3 && Oil(Pult.NOil23) < 0.3 && !Lamp(Pult.LDiesel2) && Tm() < 0.1), Show());
            // reset of the emergency button as on the scheme: the reverse handle to neutral drops RU3; then START: oil pumping 60 s, cranking 12 s
            Set(Pult.CController, 0); Thread.Sleep(250); Set(Pult.CReverser, 1); Thread.Sleep(250); Set(Pult.CReverser, 2); Thread.Sleep(250);
            Set(Pult.CLaunch1, 1); Set(Pult.CLaunch2, 1); Thread.Sleep(250); Set(Pult.CLaunch1, 0); Set(Pult.CLaunch2, 0);
            Check("START 1 and 2 after the reset: oil pumping, then both diesels run (oil above 2, lamp 'diesel 2')", Fast(60, () => Oil(Pult.NOil1) > 2 && Oil(Pult.NOil23) > 2 && Lamp(Pult.LDiesel2)), "oil " + Oil(Pult.NOil1).ToString("0.0", C) + " / " + Oil(Pult.NOil23).ToString("0.0", C));
            Set(Pult.CPump2, 0);
            Check("fuel pump 2 off stops diesel 2 (post-stop oil pumping below 1)", Fast(30, () => !Lamp(Pult.LDiesel2) && Oil(Pult.NOil23) < 1), Show());

            // 9. the instructor closes the window
            proc.CloseMainWindow();
            bool closed = proc.WaitForExit(20000);
            if (!closed) { try { proc.Kill(); } catch { } }
            Check("window closed by WM_CLOSE", closed, closed ? "exit " + proc.ExitCode : "killed");
            Check("video: stop sent at the end", WaitFor(3, () => videoStopped), "");
            stop = true;

            // 10. the cassette and the journal of the trip
            string kr2 = Directory.GetFiles(Path.Combine(dataDir, "trips"), "trip.kr2", SearchOption.AllDirectories).FirstOrDefault();
            Check("cassette written", kr2 != null, kr2 ?? "");
            if (kr2 != null)
            {
                var rec = Cassette.Read(kr2);
                Check("cassette CRC good", rec.HeaderOk && rec.BadBlocks == 0, "blocks " + rec.Blocks + ", bad " + rec.BadBlocks + ", frames " + rec.Frames.Count);
                var cranes = new HashSet<int>(rec.Frames.Select(f => (int)f.Crane));
                Check("handles as moved on the pult: crane II, V, IV, I, VI", new[] { 2, 5, 4, 1, 6 }.All(cranes.Contains), string.Join(",", cranes.OrderBy(x => x)));
                Check("controller up to 6, reverser forward, 254 in 6 and 2", rec.Frames.Max(f => f.Position) == 6 && rec.Frames.Any(f => f.Reverser == 1) && rec.Frames.Any(f => f.Kvt == 6) && rec.Frames.Any(f => f.Kvt == 2), "");
                Check("whistle and EPK trip recorded", rec.Frames.Any(f => (f.Flags & 8) != 0) && rec.Frames.Any(f => (f.Flags & 32) != 0), "");
                var ev = rec.Events.Select(e => e.Item3).ToList();
                Check("event 'Пульт кабины: на связи'", ev.Any(e => e.Contains("Пульт кабины: на связи")), "");
                Check("event of the EPK braking", ev.Any(e => e.Contains("ЭПК")), string.Join(" | ", ev.Where(e => e.Contains("ЭПК")).Take(2)));
                Check("events of the diesels: emergency button, oil pumping, start, stop by the fuel pump", ev.Any(e => e.Contains("Аварийная кнопка")) && ev.Any(e => e.Contains("прокачка масла 60 с")) && ev.Any(e => e.Contains("Секция 1: дизель запущен")) && ev.Any(e => e.Contains("остановлен топливным насосом")), string.Join(" | ", ev.Where(e => e.Contains("Секция") || e.Contains("Аварийная")).Take(6)));
                Check("events of the links: video, BLOK, EPK whistle", ev.Any(e => e.Contains("Видеосистема: на связи")) && ev.Any(e => e.Contains("БЛОК: на связи")) && ev.Any(e => e.Contains("свисток 7 с")), "");
                Check("event of closing the window", ev.Any(e => e.Contains("Окно инструктора закрыто")), "");
                Line(string.Format(C, "  trip: {0} frames, {1:0} s, v max {2:0.0} km/h, {3:0} m", rec.Frames.Count, rec.Frames.Last().T, rec.Frames.Max(f => f.V), rec.Frames.Last().X - rec.Frames.First().X));
            }
            string journal = Directory.GetFiles(Path.Combine(dataDir, "trips"), "journal_*.csv", SearchOption.AllDirectories).FirstOrDefault();
            string jt = journal != null ? File.ReadAllText(journal, Encoding.UTF8) : "";
            Check("journal: O4 'EPK off in motion'", jt.Contains(";O4;"), journal != null ? Path.GetFileName(journal) : "no journal");
        }
        catch (Exception ex) { Check("no exception", false, ex.ToString()); }
        finally
        {
            stop = true;
            if (proc != null && !proc.HasExited) { try { proc.Kill(); } catch { } }
            if (sock != null) sock.Close();
            try { if (videoListener != null) videoListener.Stop(); } catch { }
            try { if (blokSock != null) blokSock.Close(); } catch { }
        }
        Line(string.Format(C, "PULT {0}: {1} passed, {2} failed, packets from the program {3}, {4:0} s", fail == 0 ? "PASS" : "FAIL", pass, fail, outCount, sw.Elapsed.TotalSeconds));
        File.WriteAllText(Path.Combine(outDir, "pult_report.txt"), report.ToString(), new UTF8Encoding(true));
        return fail == 0 ? 0 : 1;
    }

    // installed build: the folder of the trainer from settings.json beside the program, route 1_1.json or the first one
    static string ScenarioFromSettings(string exe)
    {
        try
        {
            var text = File.ReadAllText(Path.Combine(Path.GetDirectoryName(exe), "settings.json"), Encoding.UTF8);
            var m = System.Text.RegularExpressions.Regex.Match(text, @"""release_dir""\s*:\s*""((?:[^""\\]|\\.)*)""");
            if (!m.Success) return null;
            var dir = Path.Combine(System.Text.RegularExpressions.Regex.Unescape(m.Groups[1].Value), "RTS", "scenaries");
            var one = Path.Combine(dir, "1_1.json");
            return File.Exists(one) ? one : Directory.GetFiles(dir, "*.json").OrderBy(x => x).FirstOrDefault();
        }
        catch { return null; }
    }

    // ---- emulator of the video system: reads the messages [length 2][type 2][data]
    static TcpListener videoListener; static volatile int videoRoad = -1, videoSigns, videoPositions; static volatile bool videoStopped;
    static readonly Dictionary<int, int> videoLights = new Dictionary<int, int>(); static readonly HashSet<int> videoSoundsEver = new HashSet<int>();
    static volatile float videoPos, videoMaxV, videoLimit;
    static void VideoServer()
    {
        while (!stop)
        {
            try
            {
                using (var c = videoListener.AcceptTcpClient())
                {
                    var st = c.GetStream(); var h = new byte[4];
                    while (!stop)
                    {
                        if (!ReadAll(st, h, 4)) break;
                        int len = h[0] << 8 | h[1], type = h[2] << 8 | h[3];
                        if (len == 0 && type == 256) { videoStopped = true; continue; }      // stop: 00 00 01 00
                        var d = new byte[len]; if (!ReadAll(st, d, len)) break;
                        lock (videoLights)
                        {
                            if (type == 0) videoRoad = d[0];
                            else if (type == 2) { videoPositions++; videoPos = BitConverter.ToSingle(d, 0); videoMaxV = Math.Max(videoMaxV, BitConverter.ToSingle(d, 4)); videoLimit = BitConverter.ToSingle(d, 8); }
                            else if (type == 3) videoLights[BitConverter.ToInt32(d, 0)] = d[4];
                            else if (type == 6) videoSigns++;
                            else if (type == 7 && d[4] != 0) videoSoundsEver.Add(BitConverter.ToInt32(d, 0));
                        }
                    }
                }
            }
            catch { if (stop) return; }
        }
    }
    static bool ReadAll(NetworkStream st, byte[] b, int n) { int at = 0; while (at < n) { int k = st.Read(b, at, n - at); if (k <= 0) return false; at += k; } return true; }

    // ---- emulator of BLOK: takes the 146-byte packets, answers 10 bytes
    static UdpClient blokSock; static volatile byte[] blokLast; static volatile int blokCount, blokBad, blokCoil = 1, blokAttention;
    static void BlokServer()
    {
        var from = new IPEndPoint(IPAddress.Any, 0);
        blokSock.Client.ReceiveTimeout = 500;
        while (!stop)
        {
            try
            {
                var b = blokSock.Receive(ref from);
                if (b.Length == Blok.OutSize) { blokLast = b; blokCount++; } else blokBad++;
                var a = new byte[Blok.InSize];
                a[0] = (byte)blokCoil; a[1] = 0; a[2] = 80; a[5] = b.Length > 104 ? b[104] : (byte)0; a[8] = (byte)blokAttention;
                blokSock.Send(a, a.Length, from);
            }
            catch (SocketException) { }
            catch (ObjectDisposedException) { return; }
        }
    }
    static double BlokF(int at) { var b = blokLast; return b == null ? 0 : BitConverter.ToSingle(new[] { b[at + 3], b[at + 2], b[at + 1], b[at] }, 0); }
    static int BlokB(int at) { var b = blokLast; return b == null ? 0 : b[at]; }
    static string BlokS(int at, int size) { var b = blokLast; return b == null ? "" : new UnicodeEncoding(true, false).GetString(b, at, size).TrimEnd('\0'); }

    static void Rx()
    {
        var any = new IPEndPoint(IPAddress.Any, 0);
        sock.Client.ReceiveTimeout = 500;
        while (!stop)
        {
            try
            {
                var b = sock.Receive(ref any);
                lock (gate) { if (b.Length == Pult.OutSize) { lastOut = b; outCount++; } else badOut++; }
            }
            catch (SocketException) { }
            catch (ObjectDisposedException) { return; }
        }
    }
    static void Tx()
    {
        while (!stop)
        {
            byte[] p; lock (gate) p = (byte[])cells.Clone();
            try { sock.Send(p, p.Length, program); } catch (ObjectDisposedException) { return; } catch (SocketException) { }
            Thread.Sleep(25);
        }
    }
    static void Set(int cell, byte v) { lock (gate) cells[cell] = v; }

    static byte[] Out() { lock (gate) return lastOut ?? new byte[Pult.OutSize]; }
    static double Needle(int n) { return Pult.Decode(Out(), n); }
    static bool Lamp(int l) { return Out()[Pult.Needles * 2 + l] != 0; }
    // the pult is wired GR <-> TM: the program puts TM on the GR needle and GR on the TM needle (--pult-swap 1)
    static double Oil(int needle) { return Needle(needle) * 15 / 999; }
    static double Tm() { return Needle(Pult.NGr) * 16 / 999 / Kgf; }
    static double Gr() { return Needle(Pult.NTm) * 16 / 999 / Kgf; }
    static double Ur() { return Needle(Pult.NUr) * 10 / 999 / Kgf; }
    static double Tc() { return Needle(Pult.NTc) * 10 / 999 / Kgf; }
    static bool Near(double v, double want, double tol) { return Math.Abs(v - want) <= tol; }
    static string Show() { return string.Format(C, "TM {0:0.000} UR {1:0.000} GR {2:0.000} TC {3:0.000} I {4:0} lamps {5}", Tm(), Ur(), Gr(), Tc(), Needle(Pult.NCurrent), string.Join("", Out().Skip(Pult.Needles * 2).Select(b => b.ToString(C)))); }

    static List<CassetteFrame> Frames()
    {
        try
        {
            var dir = Path.Combine(dataDir, "trips");
            if (!Directory.Exists(dir)) return new List<CassetteFrame>();
            var f = Directory.GetFiles(dir, "trip.kr2", SearchOption.AllDirectories).FirstOrDefault();
            return f == null ? new List<CassetteFrame>() : Cassette.Read(f).Frames;
        }
        catch { return new List<CassetteFrame>(); }
    }
    static double MaxSpeed() { var f = Frames(); return f.Count > 0 ? f.Max(x => Math.Abs(x.V)) : 0; }
    static double LastSpeed() { var f = Frames(); return f.Count > 0 ? Math.Abs(f[f.Count - 1].V) : -1; }
    static int FramesAfterStop() { var f = Frames(); int n = 0; for (int i = f.Count - 1; i >= 0 && Math.Abs(f[i].V) < 0.1; i--) n++; return n; }

    // a wait where only the result matters (run-up, release, recharge, oil pumping): the trip runs at x40 meanwhile (spare
    // cell 43 of the pult sets the speed of a program started with --hidden), then back at x10 for the timed steps
    static bool Fast(double seconds, Func<bool> cond) { Set(43, 40); bool ok = WaitFor(seconds, cond); Set(43, 0); Thread.Sleep(120); return ok; }
    static bool WaitFor(double seconds, Func<bool> cond)
    {
        var sw = Stopwatch.StartNew();
        while (sw.Elapsed.TotalSeconds < seconds)
        {
            if (cond()) return true;
            if (proc != null && proc.HasExited) return false;
            Thread.Sleep(50);
        }
        return cond();
    }
    static readonly Stopwatch clock = Stopwatch.StartNew();
    static void Check(string name, bool ok, string detail)
    {
        if (ok) pass++; else fail++;
        Line((ok ? "PASS " : "FAIL ") + clock.Elapsed.TotalSeconds.ToString("0", C).PadLeft(3) + " s  " + name + (string.IsNullOrEmpty(detail) ? "" : "  [" + detail + "]"));
    }
    static void Line(string s) { Console.WriteLine(s); report.AppendLine(s); }
}
