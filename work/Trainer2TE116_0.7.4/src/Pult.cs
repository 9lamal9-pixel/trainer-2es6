// Exchange with the cab pult, as the original instructor does it (IWP PultAsyncClient / PultManager / Te116Logic,
// decoded in dev/docs/fantomas/IO_2TE116.md): one UDP socket bound to the local port 2001 receives the packets of the
// pult and sends the gauges and lamps to ip_pult:port_pult of Data/logic_config.json (read only) every 25 ms.
//  in  48 bytes, byte i = cell i: 2 EPK key, 3 pedal (sand), 4 sand, 5 typhon, 6 whistle, 7 reverser (2 forward, 1 neutral,
//      0 back), 8 controller 0-15, 13/14 spotlight bright/dull, 15 sand on the first axle, 16 release button (254 in 1),
//      18 vigilance, 22 locomotive control, 23 motion, 36 emergency stop of the diesels (front), 37/39 fuel pump 1/2 (off
//      stops the diesel; off in the first packet of a trip: the diesel is not started), 38/40 START 1/2 (front, with the pump on), 44 crane 395 (1-7, 6 VI, 7 Va), 45 crane 254 analog
//      (152 -> 1, 145-151 -> 2, 132-144 -> 3, 119-131 -> 4, 101-118 -> 5, below 101 -> 6);
//  out 15 needles, 2 bytes big-endian code 0-999: oil 1, oil 2-3 (kgf/cm2 / 15), oil and water temperatures (degC / 120),
//      TC 1, TC 2, UR (kgf/cm2 / 10), TM, GR (kgf/cm2 / 16), battery current ((150 + A) / 300), generator voltage (kV),
//      generator current (A / 6000), damage indicator (/ 10); then 13 lamps: load shedding 1, 2, water level, oil pressure,
//      (none), TM (brake pipe), braked (TC above 0.04), fire, diesel 2 works, cooling, earthing relay, vigilance, PSS.
// The needles of TM and GR are swapped by default (pult_swap: the pult is wired GR <-> TM, the same correction as the
// brake package). Oil, water, battery, generator and damage indicator come from the model of the diesels (Traction.cs),
// the lamp TM from the sensor 418 (Pneumatics.Sensor418) or RU1; fire, cooling, earthing, vigilance and PSS stay off.
// A cell of the pult is applied when it changes (and all of them on the first packet), so the instructor can still move
// a handle on the screen while the pult stands still.
using System;
using System.Globalization;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Web.Script.Serialization;

namespace Trainer2TE116
{
    public sealed class Pult : IDisposable
    {
        public const int InSize = 48, Needles = 15, Lamps = 13, OutSize = Needles * 2 + Lamps;
        // output indexes
        public const int NOil1 = 0, NOil23 = 1, NOil1T = 2, NOil23T = 3, NWater1T = 4, NWater23T = 5, NTc = 6, NTc2 = 7, NUr = 8, NTm = 9, NGr = 10, NBattery = 11, NVoltage = 12, NCurrent = 13, NDamage = 14;
        public const int LShed1 = 0, LShed2 = 1, LWater = 2, LOil = 3, LNone = 4, LTm = 5, LBraked = 6, LFire = 7, LDiesel2 = 8, LCooling = 9, LEarth = 10, LVigilance = 11, LPss = 12;
        // input cells
        public const int CEpk = 2, CPedal = 3, CSand = 4, CTyphon = 5, CWhistle = 6, CReverser = 7, CController = 8, CSpotBright = 13, CSpotDull = 14, CSandAxle = 15,
            CRelease = 16, CVigilance = 18, CLocoControl = 22, CMotion = 23, CConversion = 24, CRefrigerator = 25, CFan1 = 26, CDamageSel = 31, CBoxing = 35, CDamage1 = 32, CDamage2 = 33, CAlarm = 34, CEmergencyStop = 36, CPump1 = 37, CLaunch1 = 38, CPump2 = 39, CLaunch2 = 40, CCrane = 44, CKvtAnalog = 45;

        public string Ip = "192.168.0.176"; public int Port = 2000, LocalPort = 2001; public bool Swap = true, Enabled, Bound;
        public string State = "выключен";
        public long Received, Sent, Rejected;
        UdpClient udp; IPEndPoint remote; Thread rx, tx; volatile bool stop;
        volatile byte[] lastIn, outPacket = new byte[OutSize];
        byte[] applied; long lastRxTicks;
        readonly object inputGate = new object();
        byte[] previousRx;
        int pendingButtons; // 1 emergency stop, 2 START 1, 4 START 2; consumed once by Apply
        // the journal (AppLog): the sender, the gaps, the errors and what was written last
        IPEndPoint from; long gapMaxMs, statNext = 10000, statAt, statR, statS, statJ; int sendErr, rxErrors, quiet; bool wasOn, longSeen;
        byte kvtLogged; byte[] outLogged; long outLoggedAt; readonly System.Diagnostics.Stopwatch clock = System.Diagnostics.Stopwatch.StartNew();
        static readonly string[] NeedleNames = { "масло 1", "масло 2", "t масла 1", "t масла 2", "t воды 1", "t воды 2", "ТЦ 1", "ТЦ 2", "УР", "ТМ", "ГР", "ток АБ", "U ген.", "I ген.", "указ. повр." };
        static readonly string[] LampNames = { "сброс нагрузки 1", "сброс нагрузки 2", "уровень воды", "давление масла", "(нет)", "ТМ", "заторможено", "пожар", "дизель 2 работает", "охлаждение", "реле заземления", "бдительность", "ПСС" };

        public bool Connected { get { return Received > 0 && DateTime.UtcNow.Ticks - Interlocked.Read(ref lastRxTicks) < TimeSpan.TicksPerSecond; } }
        public byte[] LastInput { get { return lastIn; } }

        // settings: command line --pult ip:port [--pult-local n] [--pult-swap 0|1] [--pult off], else logic_config.json
        public static Pult FromSettings(string releaseDir, System.Collections.Generic.Dictionary<string, string> a)
        {
            var p = new Pult();
            try
            {
                var file = Path.Combine(releaseDir, "Data", "logic_config.json");
                if (File.Exists(file))
                {
                    var d = new JavaScriptSerializer().Deserialize<System.Collections.Generic.Dictionary<string, object>>(File.ReadAllText(file));
                    if (d.ContainsKey("is_active_pult")) p.Enabled = Convert.ToInt32(d["is_active_pult"], CultureInfo.InvariantCulture) != 0;
                    if (d.ContainsKey("ip_pult")) p.Ip = Convert.ToString(d["ip_pult"], CultureInfo.InvariantCulture);
                    if (d.ContainsKey("port_pult")) p.Port = Convert.ToInt32(d["port_pult"], CultureInfo.InvariantCulture);
                }
            }
            catch { }
            if (a != null)
            {
                if (a.ContainsKey("pult"))
                {
                    if (a["pult"] == "off" || a["pult"] == "0") p.Enabled = false;
                    else { var s = a["pult"].Split(':'); p.Ip = s[0]; if (s.Length > 1) p.Port = int.Parse(s[1], CultureInfo.InvariantCulture); p.Enabled = true; }
                }
                if (a.ContainsKey("pult-local")) p.LocalPort = int.Parse(a["pult-local"], CultureInfo.InvariantCulture);
                if (a.ContainsKey("pult-swap")) p.Swap = a["pult-swap"] != "0";
            }
            return p;
        }

        public void Start()
        {
            AppLog.Write("ПУЛЬТ", "настройки: {0}, адрес пульта {1}:{2} (UDP), порт приёма {3}, приборы каждые 25 мс, вход {4} байт, выход {5} байт, pult_swap={6}{7}",
                Enabled ? "включён" : "выключен", Ip, Port, LocalPort, InSize, OutSize, Swap ? 1 : 0, Swap ? " (в ячейку стрелки ТМ идёт ГР, в ячейку ГР — ТМ)" : "");
            if (!Enabled) { State = "выключен в настройках"; return; }
            try
            {
                remote = new IPEndPoint(IPAddress.Parse(Ip), Port);
                udp = new UdpClient(LocalPort);
                try { udp.Client.IOControl(-1744830452 /* SIO_UDP_CONNRESET */, new byte[] { 0 }, null); } catch { }
            }
            catch (Exception ex) { State = "порт " + LocalPort + " занят или недоступен: " + ex.Message; udp = null; AppLog.Write("ПУЛЬТ", "обмен не запущен: " + State + " (" + ex.GetType().Name + ")"); return; }
            Bound = true; State = "ждёт пакетов от " + Ip + ":" + Port;
            AppLog.Write("ПУЛЬТ", "порт " + LocalPort + " открыт (" + udp.Client.LocalEndPoint + "), " + State);
            rx = new Thread(Receive) { IsBackground = true, Name = "pult rx" }; rx.Start();
            tx = new Thread(Send) { IsBackground = true, Name = "pult tx" }; tx.Start();
        }

        void Receive()
        {
            var any = new IPEndPoint(IPAddress.Any, 0);
            while (!stop)
            {
                try
                {
                    var b = udp.Receive(ref any);
                    if (b.Length < InSize)
                    {
                        long rj = Interlocked.Increment(ref Rejected);
                        if (rj <= 5 || rj % 400 == 0) AppLog.Write("ПУЛЬТ", "отброшен пакет от " + any + ": " + b.Length + " байт вместо " + InSize + " (всего отброшено " + rj + "): " + AppLog.Hex(b));
                        continue;
                    }
                    if (b.Length > InSize)
                    {
                        if (!longSeen) { longSeen = true; AppLog.Write("ПУЛЬТ", "пакет от " + any + " длиннее ожидаемого: " + b.Length + " байт, берутся первые " + InSize + ": " + AppLog.Hex(b)); }
                        Array.Resize(ref b, InSize);
                    }
                    long now = DateTime.UtcNow.Ticks, prev = Interlocked.Read(ref lastRxTicks);
                    if (from == null || !from.Equals(any))
                    {
                        AppLog.Write("ПУЛЬТ", (from == null ? "первый пакет" : "пакеты пошли с другого адреса (был " + from + ")") + ": от " + any + ", " + b.Length + " байт"
                            + (any.Address.ToString() != Ip ? "; в настройках адрес пульта " + Ip + " — приборы отправляются туда" : "") + "; пакет: " + AppLog.Hex(b));
                        from = new IPEndPoint(any.Address, any.Port);
                    }
                    if (prev != 0) { long gap = (now - prev) / TimeSpan.TicksPerMillisecond; if (gap > Interlocked.Read(ref gapMaxMs)) Interlocked.Exchange(ref gapMaxMs, gap); }
                    // UI/model processing can pause longer than a physical button press. Keep its rising edge even
                    // when a later released packet replaces lastIn before Apply reads the controls.
                    lock (inputGate)
                    {
                        if (previousRx != null)
                        {
                            if (previousRx[CEmergencyStop] == 0 && b[CEmergencyStop] != 0) pendingButtons |= 1;
                            if (previousRx[CLaunch1] == 0 && b[CLaunch1] != 0) pendingButtons |= 2;
                            if (previousRx[CLaunch2] == 0 && b[CLaunch2] != 0) pendingButtons |= 4;
                        }
                        previousRx = b;
                        lastIn = b;
                    }
                    Interlocked.Exchange(ref lastRxTicks, now); Interlocked.Increment(ref Received);
                }
                catch (SocketException ex) { if (stop) return; if (rxErrors++ < 5 || rxErrors % 1000 == 0) AppLog.Write("ПУЛЬТ", "ошибка приёма: " + ex.SocketErrorCode + " — " + ex.Message + " (всего ошибок " + rxErrors + ")"); }
                catch (ObjectDisposedException) { return; }
            }
        }

        void Send()
        {
            var sw = System.Diagnostics.Stopwatch.StartNew(); long next = 0;
            while (!stop)
            {
                try
                {
                    var p = outPacket; udp.Send(p, p.Length, remote); Interlocked.Increment(ref Sent);
                    if (sendErr != 0) { sendErr = 0; AppLog.Write("ПУЛЬТ", "отправка на " + remote + " восстановлена"); }
                }
                catch (ObjectDisposedException) { return; }
                catch (SocketException ex) { if (sendErr != (int)ex.SocketErrorCode) { sendErr = (int)ex.SocketErrorCode; AppLog.Write("ПУЛЬТ", "ошибка отправки на " + remote + ": " + ex.SocketErrorCode + " — " + ex.Message); } }
                if (AppLog.Enabled) Watch(sw.ElapsedMilliseconds);
                next += 25;
                int wait = (int)(next - sw.ElapsedMilliseconds);
                if (wait > 0) Thread.Sleep(wait); else next = sw.ElapsedMilliseconds;
            }
        }

        // the cells of the pult that changed since the last call go to the controls; true when something changed
        public bool Apply(Controls c)
        {
            byte[] b; int buttons;
            lock (inputGate)
            {
                b = lastIn; if (b == null) return false;
                buttons = pendingButtons;
                pendingButtons = 0;
            }
            if (AppLog.Enabled) LogInput(b);
            bool first = applied == null, any = false;
            Func<int, bool> ch = i => first || applied[i] != b[i];
            if (ch(CEpk)) { c.EpkKey = b[CEpk] != 0; any = true; }
            if (ch(CSand) || ch(CPedal)) { c.Sand = b[CSand] != 0 || b[CPedal] != 0; any = true; }
            if (ch(CSandAxle)) { c.SandAxle = b[CSandAxle] != 0; any = true; }
            if (ch(CTyphon)) { c.Typhon = b[CTyphon] != 0; any = true; }
            if (ch(CWhistle)) { c.Whistle = b[CWhistle] != 0; any = true; }
            if (ch(CReverser)) { c.Reverser = ReverserOf(b[CReverser]); any = true; }
            if (ch(CController)) { c.Position = Math.Min(15, (int)b[CController]); any = true; }
            if (ch(CSpotBright) || ch(CSpotDull)) { c.Spotlight = b[CSpotBright] != 0 || b[CSpotDull] != 0; any = true; }
            if (ch(CVigilance)) { c.Vigilance = b[CVigilance] != 0; any = true; }
            if (ch(CLocoControl)) { c.LocoControl = b[CLocoControl] != 0; any = true; }
            if (ch(CMotion)) { c.Motion = b[CMotion] != 0; any = true; }
            if (ch(CConversion)) { c.Conversion = b[CConversion] != 0; any = true; }
            if (ch(CRefrigerator)) { c.Refrigerator = b[CRefrigerator] != 0; any = true; }
            for (int k = 0; k < 4; k++) if (ch(CFan1 + k)) { c.FanSw[k] = b[CFan1 + k] != 0; any = true; }
            if (ch(CBoxing)) { c.Tob = b[CBoxing] != 0; any = true; }
            if (ch(CAlarm)) { c.AlarmMonitoring = b[CAlarm] != 1; any = true; }     // as Te116Logic: 1 -> monitoring off
            if (ch(CDamageSel) || ch(CDamage1) || ch(CDamage2)) { c.DamageSel = b[CDamageSel] == 1 ? 1 : 2; c.DamageSw = b[CDamage1] == 1 ? 1 : b[CDamage2] == 1 ? 2 : 0; any = true; }
            if (ch(CCrane) && b[CCrane] >= 1 && b[CCrane] <= 7) { c.Crane = b[CCrane]; any = true; }
            if (ch(CRelease)) { c.ReleaseButton = b[CRelease] == 1; any = true; }
            if (ch(CKvtAnalog)) { c.Kvt = KvtOf(b[CKvtAnalog]); any = true; }
            c.EmergencyHeld = b[CEmergencyStop] != 0;
            c.StartHeld1 = b[CLaunch1] != 0; c.StartHeld2 = b[CLaunch2] != 0;
            // everything that stands on the pult is taken from it, the fuel pump toggles too: a trip starts with the diesel
            // of a section running only when its toggle is on. The buttons (START, emergency stop) act by their fronts.
            if (first) { c.FuelPump[0] = b[CPump1] != 0; c.FuelPump[1] = b[CPump2] != 0; c.PultFirst = true; any = true; }
            else
            {
                if (applied[CPump1] != b[CPump1]) { c.FuelPump[0] = b[CPump1] != 0; any = true; }
                if (applied[CPump2] != b[CPump2]) { c.FuelPump[1] = b[CPump2] != 0; any = true; }
            }
            // These are received-packet edges, including pulses between the first receive and the first Apply.
            // Their held levels above still come from the newest packet, independently of the one-shot commands.
            if ((buttons & 1) != 0) { c.EmergencyStop = true; any = true; }
            if ((buttons & 2) != 0) { c.Launch[0] = true; any = true; }
            if ((buttons & 4) != 0) { c.Launch[1] = true; any = true; }
            if (buttons != 0 && AppLog.Enabled)
                AppLog.Write("ПУЛЬТ ВХОД", "приняты кнопочные фронты: аварийная остановка {0}, ПУСК 1 {1}, ПУСК 2 {2}",
                    (buttons & 1) != 0 ? 1 : 0, (buttons & 2) != 0 ? 1 : 0, (buttons & 4) != 0 ? 1 : 0);
            applied = (byte[])b.Clone();
            return any;
        }
        // a new trip: every cell of the next packet goes to the new controls
        public void Reset()
        {
            lock (inputGate) { applied = null; lastIn = null; previousRx = null; pendingButtons = 0; }
            AppLog.Write("ПУЛЬТ", "новая поездка: все органы берутся из следующего пакета пульта");
        }
        // pult reverser 2 forward, 1 neutral, 0 back -> controls 1 forward, 0 neutral, 2 back (as Te116Logic)
        public static int ReverserOf(byte v) { return v == 2 ? 1 : v == 1 ? 0 : v == 0 ? 2 : 0; }
        public static int KvtOf(byte v) { return v >= 152 ? 1 : v >= 145 ? 2 : v >= 132 ? 3 : v >= 119 ? 4 : v >= 101 ? 5 : 6; }

        public void Publish(Simulation s) { var o = Encode(s, Swap); outPacket = o; if (AppLog.Enabled) LogOutput(o); }

        // ---------- the journal of the exchange
        public static string CellName(int i)
        {
            switch (i)
            {
                case CEpk: return "ключ ЭПК"; case CPedal: return "педаль песка"; case CSand: return "песок"; case CTyphon: return "тифон"; case CWhistle: return "свисток";
                case CReverser: return "реверсор"; case CController: return "контроллер"; case CSpotBright: return "прожектор ярко"; case CSpotDull: return "прожектор тускло";
                case CSandAxle: return "песок под первую ось"; case CRelease: return "кнопка отпуска (254)"; case CVigilance: return "рукоятка бдительности";
                case CLocoControl: return "управление тепловозом"; case CMotion: return "движение"; case CConversion: return "управление переходами"; case CRefrigerator: return "холодильник";
                case CFan1: return "вентилятор 1"; case CFan1 + 1: return "вентилятор 2"; case CFan1 + 2: return "вентилятор 3"; case CFan1 + 3: return "вентилятор 4";
                case CDamageSel: return "переключатель указателя повреждений"; case CDamage1: return "указатель повреждений 1"; case CDamage2: return "указатель повреждений 2";
                case CAlarm: return "сигнализация"; case CBoxing: return "ТОБ (боксование)"; case CEmergencyStop: return "аварийная остановка дизелей";
                case CPump1: return "топливный насос 1"; case CLaunch1: return "ПУСК дизеля 1"; case CPump2: return "топливный насос 2"; case CLaunch2: return "ПУСК дизеля 2";
                case CCrane: return "кран 395"; case CKvtAnalog: return "кран 254 (аналог)";
            }
            return "не используется";
        }
        static string CellValue(int i, byte v)
        {
            switch (i)
            {
                case CReverser: return v + " (" + (v == 2 ? "вперёд" : v == 1 ? "нейтраль" : v == 0 ? "назад" : "неизвестно") + ")";
                case CController: return v + (v > 15 ? " (больше 15, берётся 15)" : "");
                case CCrane: return v + (v >= 1 && v <= 7 ? " (" + new[] { "I", "II", "III", "IV", "V", "VI", "Vа" }[v - 1] + ")" : " (вне 1–7, не применяется)");
                case CKvtAnalog: return v + " (положение " + KvtOf(v) + ")";
            }
            return v.ToString(CultureInfo.InvariantCulture);
        }
        void LogInput(byte[] b)
        {
            if (applied == null)
            {
                kvtLogged = b[CKvtAnalog];
                AppLog.Write("ПУЛЬТ ВХОД", "органы пульта приняты целиком (первый пакет или начало поездки): реверсор " + CellValue(CReverser, b[CReverser]) + ", контроллер " + b[CController]
                    + ", кран 395 " + CellValue(CCrane, b[CCrane]) + ", кран 254 " + CellValue(CKvtAnalog, b[CKvtAnalog]) + ", ключ ЭПК " + b[CEpk] + ", топливные насосы " + b[CPump1] + "/" + b[CPump2]
                    + ", управление тепловозом " + b[CLocoControl] + ", движение " + b[CMotion] + "; пакет: " + AppLog.Hex(b));
                return;
            }
            System.Text.StringBuilder sb = null;
            for (int i = 0; i < InSize; i++)
            {
                if (applied[i] == b[i]) continue;
                byte old = applied[i];
                if (i == CKvtAnalog)
                {   // the analog crane 254 trembles by a unit or two: a line when the position changes or the code has gone by 4
                    if (KvtOf(b[i]) == KvtOf(kvtLogged) && Math.Abs(b[i] - kvtLogged) < 4) continue;
                    old = kvtLogged; kvtLogged = b[i];
                }
                if (sb == null) sb = new System.Text.StringBuilder(); else sb.Append("; ");
                sb.Append("яч. ").Append(i).Append(" ").Append(CellName(i)).Append(": ").Append(CellValue(i, old)).Append(" → ").Append(CellValue(i, b[i]));
            }
            if (sb == null) return;
            if (AppLog.Raw) sb.Append("; пакет: ").Append(AppLog.Hex(b));
            AppLog.Write("ПУЛЬТ ВХОД", sb.ToString());
        }
        static string OutText(byte[] o)
        {
            var sb = new System.Text.StringBuilder("стрелки (код 0–999): ");
            for (int i = 0; i < Needles; i++) sb.Append(i > 0 ? ", " : "").Append(NeedleNames[i]).Append(' ').Append((int)Decode(o, i));
            sb.Append("; лампы ");
            for (int i = 0; i < Lamps; i++) sb.Append(o[Needles * 2 + i] != 0 ? '1' : '0');
            return sb.ToString();
        }
        void LogOutput(byte[] o)
        {
            long ms = clock.ElapsedMilliseconds;
            if (outLogged == null) { outLogged = o; outLoggedAt = ms; AppLog.Write("ПУЛЬТ ВЫХ", "первый пакет приборов: " + OutText(o) + "; пакет: " + AppLog.Hex(o)); return; }
            System.Text.StringBuilder sb = null; bool differs = false;
            for (int i = 0; i < OutSize; i++)
            {
                if (o[i] == outLogged[i]) continue;
                differs = true;
                if (i < Needles * 2) continue;
                if (sb == null) sb = new System.Text.StringBuilder("лампы: "); else sb.Append(", ");
                sb.Append("«").Append(LampNames[i - Needles * 2]).Append(o[i] != 0 ? "» зажглась" : "» погасла");
            }
            // a change of a lamp at once, the needles not more often than once in 5 s and only when they have moved
            if (sb != null) { AppLog.Write("ПУЛЬТ ВЫХ", sb + "; " + OutText(o) + (AppLog.Raw ? "; пакет: " + AppLog.Hex(o) : "")); outLogged = o; outLoggedAt = ms; }
            else if (differs && ms - outLoggedAt >= 5000) { AppLog.Write("ПУЛЬТ ВЫХ", OutText(o) + (AppLog.Raw ? "; пакет: " + AppLog.Hex(o) : "")); outLogged = o; outLoggedAt = ms; }
        }
        // the sending thread watches the link: a line when it appears or is lost and the statistics every 10 s
        void Watch(long ms)
        {
            bool on = Connected;
            if (on != wasOn)
            {
                wasOn = on;
                AppLog.Write("ПУЛЬТ", on ? "связь есть: пакеты идут от " + from : "связь потеряна: пакетов нет больше 1 с (всего принято " + Interlocked.Read(ref Received) + ")");
            }
            if (ms < statNext) return;
            long r = Interlocked.Read(ref Received), s = Interlocked.Read(ref Sent), j = Interlocked.Read(ref Rejected), gap = Interlocked.Exchange(ref gapMaxMs, 0);
            double sec = Math.Max(0.001, (ms - statAt) / 1000.0);
            if (on || r != statR || j != statJ)
            {
                quiet = 0;
                AppLog.Write("ПУЛЬТ", "за {0:0} с: принято {1} ({2:0.0}/с), отправлено {3} ({4:0.0}/с), отброшено {5}, наибольший перерыв приёма {6} мс; связь {7}; всего принято {8}, отправлено {9}",
                    sec, r - statR, (r - statR) / sec, s - statS, (s - statS) / sec, j - statJ, gap, on ? "есть" : "нет", r, s);
            }
            else if (quiet++ % 6 == 0)   // silence: once a minute
                AppLog.Write("ПУЛЬТ", "пакетов от пульта нет (всего принято {0}, отброшено {1}); на {2} отправлено {3} пакетов ({4:0.0}/с); порт приёма {5}", r, j, remote, s, (s - statS) / sec, LocalPort);
            statAt = ms; statNext = ms + 10000; statR = r; statS = s; statJ = j;
        }

        public static byte[] Encode(Simulation s, bool swap)
        {
            var o = new byte[OutSize];
            if (s == null) return o;
            var a = s.Air; var tr = s.Tr;
            Section s1 = tr.S[0], s2 = tr.S.Length > 1 && tr.Sections > 1 ? tr.S[1] : null;
            const double kgf = Traction.KgfPerMPa;
            var n = new double[Needles];
            n[NOil1] = 999 * s1.OilP * kgf / 15; n[NOil23] = s2 != null ? 999 * s2.OilP * kgf / 15 : 0;
            n[NOil1T] = s1.Oil / 120 * 999; n[NOil23T] = s2 != null ? s2.Oil / 120 * 999 : 0;
            n[NWater1T] = s1.Water / 120 * 999; n[NWater23T] = s2 != null ? s2.Water / 120 * 999 : 0;
            n[NTc] = 999 * a.LocoTcMPa * kgf / 10; n[NTc2] = n[NTc];
            n[NUr] = 999 * a.Ur.MPa * kgf / 10;
            double tm = 999 * a.TmLoco.MPa * kgf / 16, gr = 999 * a.Mr.MPa * kgf / 16;
            n[NTm] = swap ? gr : tm; n[NGr] = swap ? tm : gr;
            n[NBattery] = (150 + tr.BatteryA) / 300.0 * 999;
            n[NVoltage] = s1.VoltageKv * 999; n[NCurrent] = Math.Min(6000, s1.Current) / 6000 * 999; n[NDamage] = tr.DamageIndicator / 10 * 999;
            for (int i = 0; i < Needles; i++)
            {
                int code = (int)Math.Round(Math.Max(0, Math.Min(999, double.IsNaN(n[i]) ? 0 : n[i])));
                o[2 * i] = (byte)(code >> 8); o[2 * i + 1] = (byte)(code & 0xFF);
            }
            var l = tr.PultLamps(a.Lamp418);     // sheet 8 of the scheme; the cells are in the same order
            l[LVigilance] = s.C.EpkKey && s.LampVigilance; l[LPss] = s.C.EpkKey && s.LampPss;
            for (int i = 0; i < Lamps; i++) o[Needles * 2 + i] = (byte)(l[i] ? 1 : 0);
            return o;
        }
        public static double Decode(byte[] o, int needle) { return o[2 * needle] << 8 | o[2 * needle + 1]; }

        public void Dispose()
        {
            stop = true;
            if (Bound) AppLog.Write("ПУЛЬТ", "обмен остановлен: всего принято " + Interlocked.Read(ref Received) + ", отправлено " + Interlocked.Read(ref Sent) + ", отброшено " + Interlocked.Read(ref Rejected));
            try { if (udp != null) udp.Close(); } catch { }
        }
    }
}
