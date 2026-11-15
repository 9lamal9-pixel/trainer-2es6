// The relay netlist is the authority for electrical decisions; Traction.cs keeps the continuous physics.
using System;
using System.Collections.Generic;
using System.Linq;

namespace Trainer2TE116
{
    public sealed partial class Traction
    {
        public Scheme Electric { get; private set; }
        public bool ReleaseButton, SandAxle, CallButton, Maneuver, AutoStop = true, SpotOn = true;
        public bool EmergencyHeld, StartHeld1, StartHeld2;
        public bool Ddr; // pulse from the additional discharge of the locomotive distributor
        readonly int[] prDirection = { 1, 1 };
        readonly bool[] compWas = new bool[2];
        readonly Dictionary<string, bool> thermal = new Dictionary<string, bool>();
        public bool FuelPumpOn(int i) { return Electric.On("ТН", i + 1); }
        public bool OilPumpOn(int i) { return Electric.On("МН", i + 1); }
        public bool SpotlightOn { get { return Electric.On("Прожектор"); } }
        public int Direction(int i) { return i >= Sections ? 0 : prDirection[i] == 2 ? -1 : 1; }
        public bool SandingSection(int i) { return E("1КП1", i) || E("1КП2", i) || E("2КП1", i) || E("2КП2", i); }
        bool E(string name, int i) { return Electric.On(name, i + 1); }

        void InitElectric()
        {
            // The notes of the scheme give pressures in MPa; the instructor sees kgf/cm2 as on the gauges of the pult. SchemeData.Text itself
            // stays as it is: its hash signs the bindings of the PDF atlas.
            Electric = new Scheme(SchemeData.Text.Replace("0,025 МПа", "0,25 кгс/см²").Replace("0,06 МПа", "0,6 кгс/см²").Replace("0,75 включает, 0,90 МПа", "7,5 включает, 9,0 кгс/см²")
                .Replace("0,22 МПа", "2,2 кгс/см²").Replace("0,04 МПа", "0,4 кгс/см²"));
            if (Electric.Errors.Count > 0) throw new InvalidOperationException(string.Join("\n", Electric.Errors));
            Electric.Log = text => Event(text);
            foreach (string b in new[] { "А1", "А2", "А3", "А4", "А5", "А6", "А7", "А8", "А11", "А12", "А14", "АК", "АУ", "АВУ", "1АТ", "2АТ", "АМК" }) Electric.Breakers.Add(b);
            Electric.Trip = key =>
            {
                int sec = key.EndsWith("@2") ? 1 : 0;
                string field = RoomKey(Scheme.Bare(key));
                if (field != null) Rm[sec].Set(field, false);
            };
            thermal.Clear(); compWas[0] = compWas[1] = false; prDirection[0] = prDirection[1] = revEff == 2 ? 2 : 1;
            // A prepared locomotive starts with the pressure-held relays already picked up.
            for (int i = 0; i < Sections; i++)
                foreach (string name in new[] { "РУ9", "РУ23", "РУ10", "КРН" })
                { var a = Electric.Get(name, i + 1); a.On = a.Fed = true; a.TOn = a.FedT = 100; }
            ElectricInputs(0);
            Electric.Step(0);
        }

        // true for an apparatus outside the pult that the scheme has once for the whole locomotive (no input of its own in
        // the second section): it is read from the first section
        readonly Dictionary<string, bool> roomCommon = new Dictionary<string, bool>();
        public bool RoomCommon(string roomKey)
        {
            bool c;
            if (roomCommon.TryGetValue(roomKey, out c)) return c;
            bool any = false, sectional = false;
            foreach (string key in Electric.Inputs)
                if (RoomKey(Scheme.Bare(key)) == roomKey) { any = true; if (key.IndexOf('@') >= 0) sectional = true; }
            return roomCommon[roomKey] = any && !sectional;
        }
        static string RoomKey(string name)
        {
            switch (name)
            {
                case "ВБ": return "VB"; case "АУ": return "AU"; case "БУ": return "BU";
                case "АВУ": return "AVU"; case "1АТ": return "AT1"; case "2АТ": return "AT2";
                case "АМК": return "AMK"; case "БД": return "Doors"; case "ВРЗ": return "VRZ";
                case "ТРК": return "TRK"; case "ТНА": return "TNA"; case "ОМН": return "OMN";
                case "105": return "ShaftUnlocked";
                case "АК": return "AK";
            }
            if (name.Length >= 2 && name[0] == 'А' && char.IsDigit(name[1])) return "A" + name.Substring(1);
            if (name.StartsWith("ОМ")) return "OM" + name.Substring(2);
            foreach (var item in Room.ExtraItems) if (item[0] == name) return "X:" + name;
            return null;
        }
        bool Therm(string key, double temp, double threshold)
        {
            bool on; thermal.TryGetValue(key, out on);
            if (temp >= threshold) on = true; else if (temp < threshold - hyst) on = false;
            thermal[key] = on; return on;
        }
        bool SpeedRelay(string key, double v, double onAt, double offAt)
        {
            bool on; thermal.TryGetValue(key, out on);
            if (v >= onAt) on = true; else if (v < offAt) on = false;
            thermal[key] = on; return on;
        }
        void ElectricInputs(double v)
        {
            foreach (string key in Electric.Inputs)
            {
                bool section = key.IndexOf('@') >= 0;
                int i = key.EndsWith("@2") ? 1 : 0;
                var s = S[i]; var r = Rm[i]; string n = Scheme.Bare(key);
                bool value = false;
                string room = RoomKey(n);
                if (room != null) value = r.Get(room);
                else if (n.StartsWith("ДТ") && n.Length > 2 && char.IsDigit(n[2])) value = r.Fire;
                else switch (n)
                {
                    case "АБ": value = true; break;
                    case "СГ": value = s.Running && E("КРН", i) && !Fault(3, s); break;
                    case "АП": value = r.AP; break;
                    case "БУВ": value = !r.BuvBad; break;
                    case "СВ": value = s.Running && E("Возбудитель", i); break;
                    case "ВУ": value = s.Running && E("КВ", i) && E("ВВ", i); break;
                    case "СУ": value = s.Running; break;
                    case "ВО": value = E("КДК", i) && !compWas[i]; break;
                    case "РР": value = revEff != 0; break;
                    case "РР.В": value = revEff == 1; break;
                    case "РР.Н": value = revEff == 2; break;
                    case "КМ4": value = Position == 0; break;
                    case "КМ1": value = Position >= 1; break;
                    case "КМ5": value = Position >= 2; break;
                    case "КМ6": value = Position >= 3; break;
                    case "КМ7": value = Position >= 4; break;
                    case "КМ10": value = ((Position / 2) & 1) != 0; break;
                    case "КМ9": value = ((Position / 4) & 1) != 0; break;
                    case "КМ8": value = Position >= 8; break;
                    case "КМ2": value = Position >= 3 && (Position & 1) != 0; break;
                    case "ТН1": value = FuelSw[0]; break;
                    case "ТН2": value = FuelSw[1] && Sections > 1; break;
                    case "ПД1": value = LaunchPress[0] || StartHeld1; break;
                    case "ПД2": value = LaunchPress[1] || StartHeld2; break;
                    case "КА": value = EstopPress || EmergencyHeld; break;
                    case "УТ": value = LocoControl; break;
                    case "ТД": value = MotionSw; break;
                    case "ЭПК": value = EpkKey && !EpkTripped; break;
                    case "ТА": value = AutoStop; break;
                    case "ТУП": value = ConvSw; break;
                    case "ТОБ": value = TobSw; break;
                    case "ТХ": value = RefrigSw; break;
                    case "Т1": case "Т2": case "Т3": case "Т4": value = FanSw[n[1] - '1']; break;
                    case "ТКС": value = !AlarmMon; break;
                    case "ТП": value = DamageTp; break;
                    case "ПН": value = SandBtn; break;
                    case "КПП1": case "КПП2": value = SandAxle; break;
                    case "КЗ": value = CallButton; break;
                    case "КОТ": value = ReleaseButton; break;
                    case "КМР": value = Maneuver; break;
                    case "ТЯ": value = SpotOn; break;
                    case "ТТ": value = false; break;
                    case "РДК": value = CompressorRunning; break;
                    case "РДВ": value = rdvOk; break;
                    case "ДПТ": value = Tc254MPa > dptMPa; break;
                    case "РДМ3": value = s.OilP >= 0.025; break;
                    case "РДМ4": value = s.Running && s.OilP >= 0.06; break;
                    case "РДМ1": value = Rdm1(s); break;
                    case "ТРВ": value = s.OverheatW; break;
                    case "ТРМ": value = s.OverheatO || s.OilUnload; break;
                    case "0В": value = Therm(key, s.Water, wShut); break;
                    case "1В": value = Therm(key, s.Water, wFan1); break;
                    case "2В": value = Therm(key, s.Water, wFan2); break;
                    case "0М": value = Therm(key, s.Oil, oShut); break;
                    case "1М": value = Therm(key, s.Oil, oFan4); break;
                    case "2М": value = Therm(key, s.Oil, oFan3); break;
                    case "ПР.В": value = prDirection[i] == 1 && s.RevState == 0; break;
                    case "ПР.Н": value = prDirection[i] == 2 && s.RevState == 0; break;
                    case "РП1": value = SpeedRelay(key, v, fw1On, fw1Off); break;
                    case "РП2": value = SpeedRelay(key, v, fw2On, fw2Off); break;
                    case "РП3": value = v >= overspeed; break;
                    case "РМ1": value = s.Rm1; break;
                    case "РМ2": value = r.RM2; break;
                    case "РБ1": value = s.Slipping; break;
                    case "РБ2": value = s.Slipping && s.SlipLongT > 1; break;
                    case "РБ3": value = s.Slipping && s.FwStage > 0; break;
                    case "РУВ": value = r.WaterLeak; break;
                    case "ДТ": value = r.Fire; break;
                    case "Пробой": value = r.RZ; break;
                    case "ОбрывПоля": value = r.ROP; break;
                    case "ДДР": value = Ddr; break;
                    case "ДТЦ": value = LocoTcMPa >= 0.05; break;
                    case "ДОТ": value = LocoTcMPa > 0.04; break;
                    case "ЭПК_срыв": value = EpkTripped; break;
                    case "РКБ1": value = v > 10; break;
                    case "БКРМ": value = Crane == 6; break;
                }
                if (section && i >= Sections) value = false;
                Electric.Set(key, value);
            }
            // Legacy fault selectors affect the actual elements or sensor inputs, never bypass the solver.
            foreach (var s in S)
            {
                int i = s.Index;
                LegacyCoil("КТН", i, Fault(1, s) || s.KtnOff);
                LegacyCoil("РУ22", i, Fault(4, s) || Fault(5, s));
                foreach (string name in new[] { "МР1", "МР2", "МР3", "МР4" }) LegacyCoil(name, i, Fault(6, s));
                LegacyCoil("ВШ1", i, Fault(7, s)); LegacyCoil("ВШ2", i, Fault(7, s));
                if (Fault(8, s)) Electric.Set("А6@" + (i + 1), false);
                if (Fault(14, s) && RefrigSw)
                    foreach (string n in new[] { "0В", "1В", "2В", "0М", "1М", "2М" }) Electric.Set(n + "@" + (i + 1), false);
            }
            for (int k = 0; k < LampKeys.Length; k++)
                if (LampKeys[k] != null)
                    foreach (var c in Electric.Apps[LampKeys[k]].Coils) c.ExternalOpen = LampBurnt[k];
        }
        void LegacyCoil(string name, int i, bool open)
        { foreach (var c in Electric.Get(name, i + 1).Coils) c.ExternalOpen = open; }

        void ElectricStep(double dt, double v)
        {
            if (TmMPa < rdvOff) rdvOk = false; if (TmMPa >= rdvOn) rdvOk = true;
            bool previousComp0 = E("КДК", 0), previousComp1 = E("КДК", 1);
            ElectricInputs(v); Electric.Step(dt);
            bool was = EstopLatch;
            EstopLatch = E("РУ3", 0) || (Sections > 1 && E("РУ3", 1));
            if (was != EstopLatch) Event(EstopLatch ? "Аварийная кнопка: РУ3 — дизели остановлены, ТМ разряжается" : "РУ3: аварийная остановка сброшена");
            sot = E("РУ1", 0);
            for (int i = 0; i < Sections; i++)
            {
                if (E("ПР_В", i)) prDirection[i] = 1;
                if (E("ПР_Н", i)) prDirection[i] = 2;
            }
            compWas[0] = previousComp0; compWas[1] = previousComp1;
            EstopPress = false;
        }
        int SchemePosition(int i)
        {
            int p = (E("МР1", i) ? 2 : 0) + (E("МР2", i) ? 4 : 0) + (E("МР3", i) ? 8 : 0) + (E("МР4", i) ? 1 : 0);
            return p == 0 ? Math.Min(Position, 1) : Math.Min(15, p);
        }
        void SchemeDiesel(Section s, double dt)
        {
            int i = s.Index; bool pump = E("МН", i);
            s.StateT += dt;
            bool fuel = E("МР6", i) && !E("ВА", i) && !Fault(1, s);
            bool crank = E("Д1", i) && E("Д2", i) && E("Д3", i) && E("ВП7", i);
            if (s.Running)
            {
                RdmChance(s, dt);
                if (!fuel || (s.RandomStopAt > 0 && T >= s.RandomStopAt))
                {
                    string reason = !FuelSw[i] ? "топливным насосом" : EstopLatch ? "аварийной кнопкой" : E("ВА", i) ? "вентилем ВА" : "цепью подачи топлива";
                    SetState(s, DieselState.Stopping);
                    Event("Секция " + (i + 1) + ": дизель остановлен " + reason + "; " + Electric.WhyOff(Electric.Get("МР6", i + 1)));
                }
            }
            else if (s.State == DieselState.Stopping)
            {
                s.Rpm = Math.Max(0, s.Rpm - 80 * dt); if (s.Rpm <= 0) SetState(s, DieselState.Off);
            }
            else if (crank && fuel)
            {
                if (s.State != DieselState.Cranking) SetState(s, DieselState.Cranking);
                s.Rpm = Math.Min(220, s.Rpm + 110 * dt);
                s.OilP += (0.15 - s.OilP) * Math.Min(1, dt / 2);
                // Combustion is physics; RVP2 cuts the starter at 12 s whether or not the diesel fires.
                if (s.StateT >= 11.5 && !Fault(11, s) && E("ТН", i))
                {
                    SetState(s, DieselState.Running); s.ChargeA = 42;
                    if (Fault(12, s)) s.RandomStopAt = T + 20 + rnd.NextDouble() * 160;
                    Event("Секция " + (i + 1) + ": дизель запущен");
                }
            }
            else if (s.State == DieselState.Cranking) SetState(s, DieselState.Stopping);
            else if (pump && !E("РУ23", i))
            {
                if (s.State != DieselState.Pumping)
                {
                    SetState(s, DieselState.Pumping);
                    Event("Секция " + (i + 1) + (Rm[i].OMN ? ": ручная прокачка масла" : ": пуск дизеля — прокачка масла 60 с"));
                }
            }
            else if (s.State == DieselState.Pumping) SetState(s, DieselState.Off);
            if (!s.Running && s.State != DieselState.Cranking)
                s.OilP += ((pump ? 0.10 : 0) - s.OilP) * Math.Min(1, dt / 3);
            s.PostPumpT = pump && E("РУ23", i) ? Math.Max(0.01, 60 - Electric.Get("РВП1", i + 1).TOn) : 0;
        }

        static readonly string[] LampKeys = { "ЛН1", "ЛН2", "ЛУВ", "ЛДМ", null, "ЛРТ", "ЛОТ", "ЛПС", "ЛД2", "ЛО", "ЛРЗ", null, null };
        public List<Link> SchemeDamageChain(int i, out string name)
        {
            name = !E("РУ10", i) ? "Пуск" : !E("РУ8", i) ? "Возбуждение" : "Движение";
            var list = new List<Link>();
            Action<string, bool> add = (title, closed) => list.Add(new Link(title, closed));
            if (name == "Пуск")
            {
                add("А3: провод 1148", Electric.Live("1148@" + (i + 1)));
                add("КТН: " + Electric.WhyOff(Electric.Get("КТН", i + 1)), E("КТН", i));
                add("РВП1: выдержка 60 с", Electric.Get("РВП1", i + 1).DOn);
            }
            else if (name == "Возбуждение")
            {
                add("А4: провод 1307", Electric.Live("1307@" + (i + 1)));
                add("РКВ", E("РКВ", i)); add("КРН", E("КРН", i));
                foreach (var e in Electric.Rungs.First(r => r.Name == "КВ и ВВ" && r.Sec == i + 1).Els.Where(e => !e.IsLoad))
                    add(e.Title, e.Closed);
                add("А1 «Возбудитель»", E("Возбудитель", i));
                add("БУВ, А12 или АП", E("ОВ_генератора", i));
            }
            else
            {
                add("Шина контроллера 1696", Electric.Live("1696@1"));
                add("РУ22", E("РУ22", i)); add("РУ1", !E("РУ1", i)); add("РУ2", !E("РУ2", i));
                add("РВ3", E("РВ3", i)); add("РУ5, П1–П6", E("РУ5", i)); add("КВ и ВВ", E("КВ", i) && E("ВВ", i));
            }
            return list;
        }
    }
}
