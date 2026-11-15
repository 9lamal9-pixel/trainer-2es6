// Relay circuits of 2TE116 by the scheme 2TE116.70.01.008E3 ("8th variant", locomotives from No. 1152): sheets 3-8 of the
// coloured scheme (power, supply of the control circuits, excitation and rpm, start of the diesel, refrigerator, signal
// lamps) and the handbook "V pomoshch mashinistu i remontniku. Avariynye skhemy 2TE116 (variant 008)".
// Traction.cs keeps the physics; this file keeps what the scheme decides: which apparatus must be on for a circuit to
// work, what trips and latches, which lamp lights and what the damage indicator shows.
//
// Room  - apparatus that is not on the pult: circuit breakers above the cab door and in the cabinets, knife switches and
//         toggles of the high-voltage chamber. On the trainer the instructor operates them for the assistant driver.
// Chain - the three chains of the damage indicator UP (sheet 8, block BS.2): "start" (RU10 off), "excitation" (RU10 on,
//         RU8 off), "motion" (RU8 on); the taps of BS.2 follow the wire numbers of the scheme.
using System;
using System.Collections.Generic;

namespace Trainer2TE116
{
    public sealed class Room
    {
        // battery knife switch VB; breakers A1 exciter, A2 fuel pump, A3 diesel, A4 excitation control, A5 compressor,
        // A6 refrigerator, A12 BUV supply, AU common control (the leading section), brake interlock No. 367 (BU);
        // breakers of the fans of the rectifier (AVU) and of the traction motors (1AT, 2AT), of the compressor motor (AMK)
        public bool VB = true, A1 = true, A2 = true, A3 = true, A4 = true, A5 = true, A6 = true, A12 = true, AU = true, BU = true;
        public bool AVU = true, AT1 = true, AT2 = true, AMK = true;
        public bool A7 = true, A8 = true, A14 = true, A11, AK, OMN, ShaftUnlocked = true;
        readonly Dictionary<string, bool> extra = new Dictionary<string, bool>();
        public static readonly string[][] ExtraItems =
        {
            new[] { "ТПА", "Пожар: автоматическое тушение" }, new[] { "ТПС", "Пожар: проверка сигнализации" },
            new[] { "ТП1", "Тушение, пост 1" }, new[] { "ТП2", "Тушение, пост 2" }, new[] { "ТП3", "Тушение, пост 3" }, new[] { "ТГТ", "Газовое тушение ВВК" },
            new[] { "ТОП", "Подкузовное освещение" }, new[] { "ТПО", "Освещение пульта" }, new[] { "ТГ", "Освещение графика" }, new[] { "ТЗС", "Зелёный свет" },
            new[] { "ТОД", "Освещение дизельного помещения" }, new[] { "ТОХ", "Освещение холодильника" }, new[] { "ТОВ", "Освещение ВВК" }, new[] { "ТОК", "Освещение кабины" }, new[] { "ТПР", "Освещение проставки" },
            new[] { "ТПЛ_Б", "Передний левый белый" }, new[] { "ТПЛ_К", "Передний левый красный" }, new[] { "ТПП_Б", "Передний правый белый" }, new[] { "ТПП_К", "Передний правый красный" },
            new[] { "ТЗЛ_Б", "Задний левый белый" }, new[] { "ТЗЛ_К", "Задний левый красный" }, new[] { "ТЗП_Б", "Задний правый белый" }, new[] { "ТЗП_К", "Задний правый красный" }
        };
        // VRZ knife switch of the earthing relay, AP emergency excitation, TRK compressor relay toggle, TNA emergency
        // fuel pump, doors of the high-voltage chambers (BD), motor cut-outs OM1-OM6
        public bool VRZ = true, AP, TRK = true, TNA, Doors = true;
        public readonly bool[] OM = { true, true, true, true, true, true };
        // latched protections: earthing relay RZ, field break relay ROP, maximum current relay RM2 (reset: A4 off and on)
        public bool RZ, ROP, RM2;
        // hidden defects put by the instructor
        public int GroundTed = -1, FieldBreakTed = -1;
        public bool GroundMinus, Rm2Short, BuvBad, WaterLeak, Fire;
        public bool A4Prev = true, AUPrev = true;

        public int Motors { get { int n = 0; foreach (bool b in OM) if (b) n++; return n; } }
        public bool PowerProtection { get { return RZ || ROP || RM2; } }
        public bool FansOk { get { return AVU && AT1 && AT2; } }

        public static readonly string[][] Items =
        {
            new[] { "VB", "Рубильник АБ (ВБ)" }, new[] { "AU", "АУ «Управление общее»" }, new[] { "BU", "Блокировка № 367" },
            new[] { "A1", "А1 «Возбудитель»" }, new[] { "A2", "А2 «Топливный насос»" }, new[] { "A3", "А3 «Дизель»" },
            new[] { "A4", "А4 «Управление возбуждением»" }, new[] { "A5", "А5 «Компрессор»" }, new[] { "A6", "А6 «Холодильник»" },
            new[] { "A12", "А12 «Питание БУВ»" }, new[] { "AVU", "АВУ (обдув ВУ)" }, new[] { "AT1", "1АТ (обдув ТЭД 1–3)" },
            new[] { "AT2", "2АТ (обдув ТЭД 4–6)" }, new[] { "AMK", "АМК (двигатель компрессора)" }, new[] { "TRK", "ТРК «Реле компрессора»" },
            new[] { "VRZ", "ВРЗ (реле заземления)" }, new[] { "AP", "АП: аварийное возбуждение" }, new[] { "TNA", "ТНА: аварийный топливный насос" },
            new[] { "Doors", "Двери ВВК закрыты" }, new[] { "A7", "А7: пожарная сигнализация" }, new[] { "A8", "А8: прожектор" },
            new[] { "OMN", "ОМН: ручная прокачка масла" }, new[] { "ShaftUnlocked", "105: валоповорот выведен" },
            new[] { "OM1", "ОМ1" }, new[] { "OM2", "ОМ2" }, new[] { "OM3", "ОМ3" }, new[] { "OM4", "ОМ4" }, new[] { "OM5", "ОМ5" }, new[] { "OM6", "ОМ6" },
        };

        static Room()
        {
            var all = new List<string[]>(Items);
            all.Add(new[] { "A11", "А11: вентилятор кузова" }); all.Add(new[] { "AK", "АК: калорифер" }); all.Add(new[] { "A14", "А14: освещение" });
            foreach (var item in ExtraItems) all.Add(new[] { "X:" + item[0], item[1] });
            Items = all.ToArray();
        }
        public bool Get(string key)
        {
            if (key.StartsWith("X:")) { bool v; return extra.TryGetValue(key.Substring(2), out v) && v; }
            switch (key)
            {
                case "VB": return VB; case "AU": return AU; case "BU": return BU; case "A1": return A1; case "A2": return A2; case "A3": return A3;
                case "A4": return A4; case "A5": return A5; case "A6": return A6; case "A12": return A12; case "AVU": return AVU; case "AT1": return AT1;
                case "AT2": return AT2; case "AMK": return AMK; case "TRK": return TRK; case "VRZ": return VRZ; case "AP": return AP; case "TNA": return TNA;
                case "A7": return A7; case "A8": return A8; case "A11": return A11; case "AK": return AK; case "A14": return A14; case "OMN": return OMN; case "ShaftUnlocked": return ShaftUnlocked; case "Doors": return Doors;
            }
            if (key.StartsWith("OM")) return OM[key[2] - '1'];
            return false;
        }
        public void Set(string key, bool v)
        {
            if (key.StartsWith("X:"))
            {
                string n = key.Substring(2); extra[n] = v;
                if (v && (n.EndsWith("_Б") || n.EndsWith("_К"))) extra[n.Substring(0, n.Length - 1) + (n.EndsWith("_Б") ? "К" : "Б")] = false;
                return;
            }
            switch (key)
            {
                case "VB": VB = v; return; case "AU": AU = v; return; case "BU": BU = v; return; case "A1": A1 = v; return; case "A2": A2 = v; return;
                case "A3": A3 = v; return; case "A4": A4 = v; return; case "A5": A5 = v; return; case "A6": A6 = v; return; case "A12": A12 = v; return;
                case "AVU": AVU = v; return; case "AT1": AT1 = v; return; case "AT2": AT2 = v; return; case "AMK": AMK = v; return; case "TRK": TRK = v; return;
                case "VRZ": VRZ = v; return; case "AP": AP = v; return; case "TNA": TNA = v; return; case "A7": A7 = v; return; case "A8": A8 = v; return; case "A11": A11 = v; return; case "AK": AK = v; return; case "A14": A14 = v; return; case "OMN": OMN = v; return; case "ShaftUnlocked": ShaftUnlocked = v; return; case "Doors": Doors = v; return;
            }
            if (key.StartsWith("OM")) OM[key[2] - '1'] = v;
        }
    }

    // one link of a chain of the damage indicator: its name on the scheme and whether its contacts are closed
    public struct Link
    {
        public string Name; public bool Closed;
        public Link(string name, bool closed) { Name = name; Closed = closed; }
    }

    public static class Chain
    {
        // the pointer goes as far as the chain is closed from its supply; the first open link is the place to look at.
        // The scale of the real instrument is not published: 0-10 here is the share of the chain that is closed.
        public static double Reading(IList<Link> links)
        {
            int k = 0;
            while (k < links.Count && links[k].Closed) k++;
            return links.Count == 0 ? 0 : 10.0 * k / links.Count;
        }
        public static string FirstOpen(IList<Link> links)
        {
            foreach (var l in links) if (!l.Closed) return l.Name;
            return null;
        }
    }
}
