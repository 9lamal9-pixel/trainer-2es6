// Links with the video system and with BLOK (the KLUB-U display of the cab), as the original instructor makes them
// (decompiled IWP ClientVideo / ClientVideoManager / VideoDataReceiveController and BLOKManager / BLOKWriteController /
// BLOKReadController / BLOKDataReceiveController). Addresses: Data\logic_config.json of the trainer (read only), or
// --video ip:port|off, --blok ip:port|off, --road n (index of the video route; default RTS\start_markup_data.json).
//
// Video (SmartVideo), TCP client to ip_video:port_video. Message: data length (2 bytes, high first), type (2 bytes, high
// first), data (little-endian):
//   0 start, byte route index (3 s pause before and after, as the original)       1 stop (bytes 00 00 01 00)
//   2 position: float coordinate m, float speed km/h, float allowed speed          3 signal: int index, byte aspect
//   4 weather: byte kind (0 night, 1 fog, 3 snow, 5 rain), byte on                 5 spotlight: byte on
//   6 sign: byte type, float coordinate                                            7 sound: int index, byte on
//   8 3D object (obstacle): int id, byte animation (1 for cars), byte visible
// After the start all the signals, the signs and the active obstacles of the scenario are sent, then only the changes,
// every 50 ms. A jump of the coordinate by more than 5 km is sent in steps of 5 km (as the original).
//
// BLOK, UDP to ip_blok:port_blok every 25 ms, 146 bytes, numbers high byte first, strings UTF-16 BE:
//   power, EPK key, RB, RBS, RBP, int GPS coordinate (km*1000 + pk*100 + m), picket, float TM, TC, UR, UR, GR (MPa),
//   acceleration, speed km/h, slope, braking coefficient 0.33, EK 1, cassette 1, traction, direction, long time ms,
//   key of the KLUB keyboard, station (16 bytes), target code 1, short distance to the target, to SAUT, target name (18),
//   allowed and target speed by the EK, distance to the next section, signal code (1 green, 2 yellow, 3 red, 0 white,
//   5 none), distance to the signal, ..., SAUT 0, TSKBM 0, signal name (18), valve opened by KON, by EPK, route index, int.
// BLOK answers 10 bytes: holding coil of the EPK, short allowed speed, short distance to the aiming point, signal code,
// take the traction off, service braking of SAUT, lamp "attention", lamp PSS.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Web.Script.Serialization;

namespace Trainer2TE116
{
    public static class LinkSettings
    {
        public static Dictionary<string, object> Logic(string releaseDir)
        {
            try
            {
                var f = Path.Combine(releaseDir, "Data", "logic_config.json");
                if (File.Exists(f)) return new JavaScriptSerializer().Deserialize<Dictionary<string, object>>(File.ReadAllText(f));
            }
            catch { }
            return new Dictionary<string, object>();
        }
        // ip:port and "enabled" of a link: the command line key wins over logic_config.json
        public static void Address(Dictionary<string, object> logic, Dictionary<string, string> a, string key, ref string ip, ref int port, ref bool enabled)
        {
            try
            {
                if (logic.ContainsKey("is_active_" + key)) enabled = Convert.ToInt32(logic["is_active_" + key], CultureInfo.InvariantCulture) != 0;
                if (logic.ContainsKey("ip_" + key)) ip = Convert.ToString(logic["ip_" + key], CultureInfo.InvariantCulture);
                if (logic.ContainsKey("port_" + key)) port = Convert.ToInt32(logic["port_" + key], CultureInfo.InvariantCulture);
            }
            catch { }
            if (a != null && a.ContainsKey(key))
            {
                if (a[key] == "off" || a[key] == "0") enabled = false;
                else if (a[key] != "auto") { var s = a[key].Split(':'); ip = s[0]; if (s.Length > 1) port = int.Parse(s[1], CultureInfo.InvariantCulture); enabled = true; }
            }
        }
    }

    public sealed class Video : IDisposable
    {
        public string Ip = "192.168.2.20"; public int Port = 12700, Road; public bool Enabled;
        public string State = "выключено";
        public long Sent; public volatile bool Started; public int PauseMs = 3000;   // pauses around the start of the route, as the original
        TcpClient tcp; Thread worker; volatile bool stop;
        readonly Queue<byte[]> queue = new Queue<byte[]>();
        // what the video system has been told
        int[] aspects; bool[] obstacles; readonly HashSet<Sign> signsSent = new HashSet<Sign>();
        bool? night, rain, snow, fog; int spot = -1; ulong sounds; bool soundsKnown;
        float lastPos = float.NaN, lastV = float.NaN, lastLim = float.NaN, stepPos = float.NaN;
        Route route;

        public bool Connected { get { var t = tcp; return Started && t != null && t.Connected; } }

        public static Video FromSettings(string releaseDir, Dictionary<string, string> a)
        {
            var v = new Video();
            LinkSettings.Address(LinkSettings.Logic(releaseDir), a, "video", ref v.Ip, ref v.Port, ref v.Enabled);
            try
            {
                var f = Path.Combine(releaseDir, "RTS", "start_markup_data.json");
                if (File.Exists(f))
                {
                    var d = new JavaScriptSerializer().Deserialize<Dictionary<string, object>>(File.ReadAllText(f));
                    if (d.ContainsKey("markup_index")) v.Road = Convert.ToInt32(d["markup_index"], CultureInfo.InvariantCulture);
                }
            }
            catch { }
            if (a != null && a.ContainsKey("video-pause")) v.PauseMs = int.Parse(a["video-pause"], CultureInfo.InvariantCulture);
            if (a != null && a.ContainsKey("road")) v.Road = int.Parse(a["road"], CultureInfo.InvariantCulture);
            return v;
        }

        public void Start()
        {
            if (!Enabled) { State = "выключено в настройках"; return; }
            State = "подключение к " + Ip + ":" + Port;
            worker = new Thread(Work) { IsBackground = true, Name = "video" }; worker.Start();
        }

        static byte[] Message(short type, byte[] data)
        {
            var m = new byte[data.Length + 4];
            m[0] = (byte)(data.Length >> 8); m[1] = (byte)data.Length; m[2] = (byte)(type >> 8); m[3] = (byte)type;
            Buffer.BlockCopy(data, 0, m, 4, data.Length);
            return m;
        }
        void Put(short type, byte[] data) { lock (queue) { if (queue.Count < 5000) queue.Enqueue(Message(type, data)); } }

        void Work()
        {
            while (!stop)
            {
                try
                {
                    Started = false;
                    var c = new TcpClient { NoDelay = true };
                    var ar = c.BeginConnect(Ip, Port, null, null);
                    if (!ar.AsyncWaitHandle.WaitOne(5000) || !c.Connected) { try { c.Close(); } catch { } State = "нет связи с " + Ip + ":" + Port; Wait(2000); continue; }
                    c.EndConnect(ar);
                    tcp = c; State = "запуск маршрута " + Road;
                    // as the original: 3 s, the start with the route index, 3 s
                    Wait(PauseMs); if (stop) break;
                    restart = false;
                    c.Client.Send(Message(0, new[] { (byte)Road })); Sent++;
                    Wait(PauseMs);
                    lock (queue) { queue.Clear(); Forget(); }
                    Started = true; State = "на связи";
                    while (!stop && c.Connected)
                    {
                        // another route for the new trip: the video is stopped and started again, as when the program is restarted
                        if (restart) { restart = false; Started = false; State = "смена маршрута на " + Road; c.Client.Send(new byte[] { 0, 0, 1, 0 }); Sent++; Wait(PauseMs); break; }
                        byte[] m = null;
                        lock (queue) if (queue.Count > 0) m = queue.Dequeue();
                        if (m == null) { Thread.Sleep(10); continue; }
                        c.Client.Send(m); Sent++;
                    }
                }
                catch (Exception ex) { State = "нет связи: " + ex.Message; }
                Started = false;
                try { if (tcp != null) tcp.Close(); } catch { }
                tcp = null;
                Wait(2000);
            }
        }
        void Wait(int ms) { for (int i = 0; i < ms / 50 && !stop; i++) Thread.Sleep(50); }
        // everything is sent again after a (re)connection or for a new trip
        void Forget() { aspects = null; obstacles = null; signsSent.Clear(); night = rain = snow = fog = null; spot = -1; soundsKnown = false; sounds = 0; lastPos = lastV = lastLim = stepPos = float.NaN; }
        public void Reset() { lock (queue) { queue.Clear(); Forget(); } }
        volatile bool restart;
        // the route of the next trip; a video already started with another one is restarted
        public void SetRoad(int road) { if (road == Road) return; Road = road; restart = true; }

        // called by the window every tick (0.1 s): the changes go to the queue
        public void Update(Simulation sim, Route r, double allowedKmh, bool paused)
        {
            if (!Enabled || !Started || sim == null || r == null) return;
            lock (queue)
            {
                if (!ReferenceEquals(r, route)) { Forget(); route = r; }
                if (aspects == null || aspects.Length != r.Lights.Count) { aspects = new int[r.Lights.Count]; for (int i = 0; i < aspects.Length; i++) aspects[i] = -1; }
                for (int i = 0; i < r.Lights.Count; i++)
                    if (aspects[i] != r.Lights[i].Aspect)
                    {
                        aspects[i] = r.Lights[i].Aspect;
                        var d = new byte[5]; BitConverter.GetBytes(r.Lights[i].Index).CopyTo(d, 0); d[4] = (byte)aspects[i];
                        Put(3, d);
                    }
                lock (r.Signs)
                    foreach (var s in r.Signs)
                        if (signsSent.Add(s)) { var d = new byte[5]; d[0] = (byte)s.Type; BitConverter.GetBytes((float)s.X).CopyTo(d, 1); Put(6, d); }
                if (obstacles == null || obstacles.Length != r.Obstacles.Count) obstacles = new bool[r.Obstacles.Count];
                for (int i = 0; i < r.Obstacles.Count; i++)
                {
                    var o = r.Obstacles[i];
                    if (o.Id < 0 || obstacles[i] == o.Active) continue;
                    obstacles[i] = o.Active;
                    var d = new byte[6]; BitConverter.GetBytes(o.Id).CopyTo(d, 0); d[4] = (byte)(o.Kind == "cars" ? 1 : 0); d[5] = (byte)(o.Active ? 1 : 0);
                    Put(8, d);
                }
                Weather(0, !r.Day, ref night); Weather(5, r.Rain, ref rain); Weather(3, r.Snow, ref snow); Weather(1, r.Fog, ref fog);
                int sp = sim.Tr.SpotlightOn ? 1 : 0;
                if (sp != spot) { spot = sp; Put(5, new[] { (byte)sp }); }
                // sounds: on a pause all the sounds stop, as the original
                ulong mask = paused ? 0 : sim.SoundMask;
                if (!soundsKnown || mask != sounds)
                {
                    for (int i = 0; i < 60; i++)
                    {
                        bool on = (mask >> i & 1) != 0, was = soundsKnown && (sounds >> i & 1) != 0;
                        if (on == was && soundsKnown) continue;
                        if (!soundsKnown && !on) continue;
                        var d = new byte[5]; BitConverter.GetBytes(i).CopyTo(d, 0); d[4] = (byte)(on ? 1 : 0);
                        Put(7, d);
                    }
                    sounds = mask; soundsKnown = true;
                }
                float pos = (float)sim.Dyn.HeadX, v = (float)sim.SpeedKmh, lim = (float)allowedKmh;
                if (float.IsNaN(stepPos)) stepPos = Math.Min(pos, 0);
                // a far start: the video is led there in steps of 5 km
                int guard = 0;
                while (pos - stepPos > 5000f && guard++ < 200) { stepPos += 5000f; Position(stepPos, v, lim); }
                stepPos = pos;
                if (pos != lastPos || v != lastV || lim != lastLim) Position(pos, v, lim);
            }
        }
        void Weather(byte kind, bool on, ref bool? known)
        {
            if (known.HasValue && known.Value == on) return;
            bool first = !known.HasValue; known = on;
            if (first && !on) return;
            Put(4, new[] { kind, (byte)(on ? 1 : 0) });
        }
        void Position(float pos, float v, float lim)
        {
            var d = new byte[12]; BitConverter.GetBytes(pos).CopyTo(d, 0); BitConverter.GetBytes(v).CopyTo(d, 4); BitConverter.GetBytes(lim).CopyTo(d, 8);
            Put(2, d); lastPos = pos; lastV = v; lastLim = lim;
        }

        public void Dispose()
        {
            // the sounds off and the stop of the video, then the socket
            try
            {
                var c = tcp;
                if (c != null && c.Connected && Started)
                {
                    for (int i = 0; i < 60; i++) if ((sounds >> i & 1) != 0) { var d = new byte[5]; BitConverter.GetBytes(i).CopyTo(d, 0); c.Client.Send(Message(7, d)); }
                    c.Client.Send(new byte[] { 0, 0, 1, 0 });
                }
            }
            catch { }
            stop = true;
            try { if (tcp != null) tcp.Close(); } catch { }
        }
    }

    public sealed class Blok : IDisposable
    {
        public const int OutSize = 146, InSize = 10;
        public string Ip = "192.168.2.20"; public int Port = 2002; public bool Enabled;
        public string State = "выключен";
        public long Sent, Received;
        UdpClient udp; IPEndPoint remote; Thread rx, tx; volatile bool stop;
        volatile byte[] outPacket = new byte[OutSize], lastIn; long lastRxTicks;
        double prevV = double.NaN, accel;
        static readonly Encoding Utf16Be = new UnicodeEncoding(true, false);

        public bool Connected { get { return Received > 0 && DateTime.UtcNow.Ticks - Interlocked.Read(ref lastRxTicks) < TimeSpan.TicksPerSecond; } }
        // answers of BLOK (valid while it is on line)
        public bool HoldingCoil { get { var b = lastIn; return b == null || b[0] != 0; } }
        public int AllowedKmh { get { var b = lastIn; return b == null ? -1 : (short)(b[1] << 8 | b[2]); } }
        public int AimDistance { get { var b = lastIn; return b == null ? -1 : (short)(b[3] << 8 | b[4]); } }
        public int SignalCode { get { var b = lastIn; return b == null ? -1 : b[5]; } }
        public bool TractionOff { get { var b = lastIn; return b != null && b[6] != 0; } }
        public bool LampAttention { get { var b = lastIn; return b != null && b[8] != 0; } }
        public bool LampPss { get { var b = lastIn; return b != null && b[9] != 0; } }

        public static Blok FromSettings(string releaseDir, Dictionary<string, string> a)
        {
            var b = new Blok();
            LinkSettings.Address(LinkSettings.Logic(releaseDir), a, "blok", ref b.Ip, ref b.Port, ref b.Enabled);
            return b;
        }

        public void Start()
        {
            if (!Enabled) { State = "выключен в настройках"; return; }
            try
            {
                remote = new IPEndPoint(IPAddress.Parse(Ip), Port);
                udp = new UdpClient(0);
                try { udp.Client.IOControl(-1744830452 /* SIO_UDP_CONNRESET */, new byte[] { 0 }, null); } catch { }
            }
            catch (Exception ex) { State = "не запущен: " + ex.Message; udp = null; Enabled = false; return; }
            State = "ждёт ответа от " + Ip + ":" + Port;
            rx = new Thread(Receive) { IsBackground = true, Name = "blok rx" }; rx.Start();
            tx = new Thread(Send) { IsBackground = true, Name = "blok tx" }; tx.Start();
        }
        void Receive()
        {
            var any = new IPEndPoint(IPAddress.Any, 0);
            while (!stop)
            {
                try
                {
                    var b = udp.Receive(ref any);
                    if (b.Length < InSize) continue;
                    lastIn = b; Interlocked.Exchange(ref lastRxTicks, DateTime.UtcNow.Ticks); Interlocked.Increment(ref Received);
                }
                catch (SocketException) { if (stop) return; }
                catch (ObjectDisposedException) { return; }
            }
        }
        void Send()
        {
            while (!stop)
            {
                try { var p = outPacket; udp.Send(p, p.Length, remote); Interlocked.Increment(ref Sent); }
                catch (ObjectDisposedException) { return; }
                catch (SocketException) { }
                Thread.Sleep(25);
            }
        }

        // keys of the KLUB keyboard: pult cell 46 -> code of BLOK (BLOKDataReceiveController.KeyboardFromPultToBlock)
        public static int Key(int k)
        {
            switch (k)
            {
                case 1: return 1; case 2: return 20; case 4: return 14; case 5: return 15; case 6: return 16; case 9: return 4; case 11: return 10; case 12: return 11;
                case 13: return 12; case 14: return 2; case 15: return 18; case 16: return 19; case 17: return 6; case 18: return 7; case 19: return 8; case 20: return 5;
                case 21: return 9; case 22: return 13; case 23: return 21; case 24: return 22; case 25: return 23; case 26: return 24; default: return 0;
            }
        }
        // aspect of the scenario -> code of BLOK (TrafficLight.GetMode)
        public static int SignalOf(int aspect)
        {
            switch (aspect)
            {
                case 0: case 6: case 7: case 8: case 11: case 18: return 1;
                case 2: case 9: case 10: case 12: case 13: case 16: case 17: return 2;
                case 1: case 14: case 15: return 3;
                case 3: case 19: case 20: return 5;
                default: return 0;
            }
        }

        // called by the window every tick: the packet for BLOK from the trip; pultIn = the last packet of the pult (or null)
        public void Publish(Simulation s, Route r, byte[] pultIn, double dt)
        {
            if (!Enabled || s == null || r == null) return;
            var o = new byte[OutSize]; int at = 0;
            Action<int> b1 = v => { o[at++] = (byte)v; };
            Action<int> i2 = v => { o[at++] = (byte)(v >> 8); o[at++] = (byte)v; };
            Action<int> i4 = v => { o[at++] = (byte)(v >> 24); o[at++] = (byte)(v >> 16); o[at++] = (byte)(v >> 8); o[at++] = (byte)v; };
            Action<double> f4 = v => { var x = BitConverter.GetBytes((float)v); o[at++] = x[3]; o[at++] = x[2]; o[at++] = x[1]; o[at++] = x[0]; };
            Action<string, int> str = (v, size) => { var x = Utf16Be.GetBytes(v ?? ""); Buffer.BlockCopy(x, 0, o, at, Math.Min(size, x.Length) / 2 * 2); at += size; };
            double head = s.Dyn.HeadX, spd = s.SpeedKmh;
            if (!double.IsNaN(prevV) && dt > 0) accel += ((spd - prevV) / 3.6 / dt - accel) * 0.3;
            prevV = spd;
            int li = r.NextLight(head); var light = li >= 0 ? r.Lights[li] : null;
            int dist = light != null ? (int)Math.Max(-32000, Math.Min(32000, light.X - head)) : 0;
            int st = r.NextStation(head); if (st < 0) st = r.StationName.Length - 1;
            int pk = -1; for (int i = 0; i < r.PiketX.Length; i++) { if (r.PiketX[i] <= head) pk = i; else break; }
            b1(1); b1(s.C.EpkKey ? 1 : 0); b1(pultIn != null && pultIn[0] != 0 ? 1 : 0); b1(pultIn != null && pultIn[1] != 0 ? 1 : 0); b1(0);
            i4(pk >= 0 ? r.PiketKm[pk] * 1000 + r.PiketPk[pk] * 100 + (int)(head - r.PiketX[pk]) : (int)head); b1(pk >= 0 ? r.PiketPk[pk] : 0);
            f4(s.Air.TmLoco.MPa); f4(s.Air.LocoTcMPa); f4(s.Air.Ur.MPa); f4(s.Air.Ur.MPa); f4(s.Air.Mr.MPa);
            f4(accel); f4(spd); f4(r.SlopeAt(head)); f4(0.33);
            b1(1); b1(1); b1(s.Tr.TotalForceKn > 0.5 ? 1 : 0); b1(Math.Abs(spd) < 0.01 ? 0 : s.C.Reverser);
            long ms = (long)DateTime.Now.Subtract(new DateTime(1970, 1, 1)).TotalMilliseconds;
            i4((int)(ms >> 32)); i4((int)ms);
            b1(pultIn != null ? Key(pultIn[46]) : 0);
            str(st >= 0 ? r.StationName[st].ToUpper() : "", 16);
            b1(1); i2(dist); i2(0); str(light != null ? light.Name : "", 18);
            // electronic map: the limit in force, and the limit of the next dangerous place ahead (within 2 km) as the target
            int target = 1000;
            lock (r.Signs) foreach (var sg in r.Signs) { if (sg.X <= head) continue; if (sg.X - head > 2000) break; if (Route.IsDangerStart(sg.Type) && sg.Limit > 0) { target = sg.Limit; break; } }
            i2(r.StaticLimitAt(head)); i2(target); i2(0);
            b1(light != null ? SignalOf(light.Aspect) : 0); i2(dist);
            b1(0); i2(0); b1(0); b1(0); i2(0); b1(0); b1(0);
            b1(0); b1(0); b1(0); b1(0); b1(0);
            str(light != null ? light.Name : "", 18);
            b1(0); b1(s.Air.EpkTripped ? 1 : 0); b1(0); i4(0);
            outPacket = o;
        }

        public void Dispose() { stop = true; try { if (udp != null) udp.Close(); } catch { } }
    }
}
