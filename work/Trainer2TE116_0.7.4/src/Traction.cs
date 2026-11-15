// Diesels and the electric transmission of the two sections of 2TE116, ported from the model of the brake package
// (dev/fantomas/brake_proxy/LocoModel.cs, checked there by the LocoHarness bench) with the calibrated values of its
// 2Te116_brakes.ini. Classic relay scheme, no microprocessor control:
//  - start (BPD): fuel pump on, controller 0, reverser in a direction, short press of START: oil pumping 60 s, cranking with
//    the fire at the 5th second (RVP2 limit 12 s); stop by the fuel pump switch, then 60 s of oil pumping;
//  - governor: rpm of the position (RE table 6.2, 350-1000), boost lag; an overloaded diesel sags ("chokes") until the
//    inductive sensor ID of the power regulator takes the load off (down fast, up slowly);
//  - auxiliaries on the shaft: SG (control, battery, excitation, compressor motor) and straight from the generator the
//    asynchronous fans of the traction motors and of the rectifier and the four cooling fans (fan law, cube of the rpm);
//    their power goes off the traction (TPT/TPN measure the motor circuit only);
//  - excitation of the generator: BZV setting by the actual rpm, selective unit SU (position current, 700 V, power
//    hyperbola of ID), BUV forcing within the exciter ceiling, field winding with its time constants; at idle (RU5 off)
//    the generator stays excited for the fans: 80 V at 350 rpm, 500 V at 1000 rpm;
//  - assembly on the 1st position (RV3, RU8), field weakening by speed 42/62 on, 35/55 off with 9 s delay;
//  - six ED-118A in parallel, series field with saturation, adhesion by PTR, slip relay (half excitation, MR5 after 2 s);
//  - cooling (RE 2TE116U part 1, 9.9): water shutters 70, M11 80, M12 84 degC; oil shutters 65, M14 72, M13 75 degC;
//    radiators by the air flow^0.7; TRV 96 / TRM 88 unload the generator;
//  - protections: RDV (pipe below 0.294 MPa, back at 0.441), DPT (254 above 0.22 MPa: traction is kept on steps 3 and 4 of the 254 for a smooth start), RU1 (pipe discharged not by the
//    crane), EPK, emergency button (fuel cut, the pipe vented, sand; reset: "locomotive control" off and on), 103 km/h,
//    oil pressure (unload below 0.10, stop below 0.07 MPa);
//  - technical condition of each diesel (nominal 100 %, normal 88 %, worn 70 %, sections 5-10 % apart): power and heat
//    first, below 90 % oil pressure and random RDM drops;
//  - faults of the instructor (electric list), applied to their section.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;

namespace Trainer2TE116
{
    public enum DieselState { Off, Pumping, Cranking, Running, Stopping }

    public sealed class Section
    {
        public int Index;
        public DieselState State = DieselState.Running;
        public double StateT, Rpm = 350, Boost, Exc, PickT, DropT;
        public bool Assembled;
        public double Force, Current, VoltageKv, PowerKw, AuxKw, CoolFanKw, FuelKg, Ambient = 20;
        public readonly double[] FanSpeed = new double[4];
        public readonly bool[] FanOn = new bool[4];
        public double IdK = 1, Field, Overload, ShaftKw, AvailKw, SlipLongT;
        // Derived readouts from the incoming patch; these scales are model estimates, not calibrated measurements.
        public double IdAnchorMm, IdImpedanceOhm, FuelRack;
        // The exciter has its own state before the existing main-generator field winding.
        public double ExciterField;
        public int SuChannel; // 0 idle/off, 1 current, 2 power, 3 voltage, 4 emergency excitation
        public double Water = 60, Oil = 58, OilP = 0.25;
        public bool OverheatW, OverheatO, ShutW, ShutO;
        public readonly bool[] Fan = new bool[4];
        public int FwStage, PosHeld;
        public double FwT, SlipUnloadT, SandT, PostPumpT, OilLowT, ChargeA = 8, RandomStopAt = -1, OilFaultK = 1;
        public bool Slipping, OilUnload;
        public double Cond = 1, PowerK = 1, BoostK = 1, HeatK = 1, CoolK = 1, OilK = 1, RdmK, RdmDipT, RdmDipP;
        public int RdmUnloads, RdmStops, RevState, Rm1Trips, SlipCount;
        public bool Rm1, KtnOff, KtnPending, Vv;      // Vv: contactors KV and VV are on (the generator is excited)
        public double KtnDropAt = -1;
        public bool Running { get { return State == DieselState.Running; } set { State = value ? DieselState.Running : DieselState.Off; if (!value) Rpm = 0; } }
        public bool OilPump { get { return State == DieselState.Pumping || PostPumpT > 0; } }
        public string StateName
        {
            get
            {
                switch (State)
                {
                    case DieselState.Pumping: return "прокачка масла";
                    case DieselState.Cranking: return "прокрутка";
                    case DieselState.Running: return "работает";
                    case DieselState.Stopping: return "останавливается";
                    default: return PostPumpT > 0 ? "остановлен, прокачка" : "остановлен";
                }
            }
        }
    }

    public sealed class ElecFaultDef { public int Code, Kind, Section; public string Name; }

    public sealed partial class Traction
    {
        static readonly CultureInfo C = CultureInfo.InvariantCulture;
        static readonly double[] RpmTable = { 350, 350, 395, 445, 490, 535, 580, 630, 675, 720, 770, 815, 860, 910, 955, 1000 };   // "Lokomotiv" 1/2017, table 7
        static readonly double[] CurrentLimit = { 0, 1200, 1900, 2100, 2750, 3000, 3700, 4500, 4700, 4900, 5150, 5400, 5600, 5800, 6100, 6400 };
        public const double KgfPerMPa = 10.1971617;
        // locomotive characteristic of 1A-9DG-2: share of the full shaft power (2250 kW) by the rpm of positions 1-15. From the
        // rheostat tests of 2TE116 (Bondar, DonIZhT 2010: generator 130, 280, 370 ... 1720, 1920 kW) plus the auxiliaries of
        // the model; the former power law n^2.7 gave 25-45 % less on positions 2-9
        static readonly double[] ShareRpm = { 300, 350, 395, 445, 490, 535, 580, 630, 675, 720, 770, 815, 860, 910, 955, 1000 };
        static readonly double[] ShareKw = { 0.050, 0.070, 0.142, 0.186, 0.226, 0.275, 0.324, 0.398, 0.470, 0.547, 0.642, 0.705, 0.771, 0.842, 0.894, 1.000 };
        static double PowerShare(double rpm)
        {
            if (rpm < 300) return 0;
            if (rpm >= 1000) return 1;
            int i = 1; while (rpm > ShareRpm[i]) i++;
            return ShareKw[i - 1] + (ShareKw[i] - ShareKw[i - 1]) * (rpm - ShareRpm[i - 1]) / (ShareRpm[i] - ShareRpm[i - 1]);
        }

        // ---- values (2Te116_brakes.ini of the package)
        // rates of the serial diesel-generator with the regulator 4-7RS2 (tests of 2TE116 No. 1359/1360): the shaft reaches
        // 850 rpm in 29 s and 1000 rpm in about 40 s from the minimum, the full power comes in about 55 s; the drop to
        // the minimum takes 17-20 s
        double rpmUp = 17, rpmUpTop = 14, rpmDown = 35, boostTau = 7;
        double excUp = 1.0, excDown = 0.45, rimKw = 1750, forcePerA = 0.059;
        public double SectionMassT = 138, AdhesionFactor = 1;
        double sandGain = 1.2, slipUnloadS = 1.5, fw1On = 42, fw1Off = 35, fw2On = 62, fw2Off = 55, beta1 = 0.60, beta2 = 0.37, overspeed = 110;
        double rCircuitKvPerA, emfK, uLimKv = 0.7, fluxKneeA = 450, fluxNorm, motorTauS = 0.1;
        double dieselKw = 2250, sgEff = 0.85, ggEff = 0.96, tedFansKw = 105, vuFanKw = 8.8, coolFanKw = 35.6, compMotorKw = 37, sgBaseKw = 8, excKw = 15, thermalInertia = 3.0;
        double idDownS = 2.0, idUpS = 8.0, sagDroop = 0.5, idDownRate = 0.12, idUpRate = 0.05, fanStartK = 3.0;
        double egNomKv = 0.75, rGgKvPerA = 0.003 / 1000.0, fieldTauS = 1.0, fieldTauDownS = 0.5, fieldForce = 1.5, fieldUfMax = 2.5, fieldMax = 1.5, fieldKnee = 0.9, fieldNorm;
        double svTauS = 0.18, tkCompoundK = 0.12;
        const double IdStrokeMm = 65, IdZMinOhm = 5.5, IdZMaxOhm = 70;
        double uIdle1000 = 0.5, idleExp;
        double uaWater = 0.0025, uaOil = 0.0024, airExp = 0.7, qNatural = 0.15, qRam = 0.2, fanRunUpS = 8, fanCoastS = 25, closedLeak = 0.25;
        double rdvOn = 0.441, rdvOff = 0.294, dptMPa = 0.22, waterTrip = 96, waterReset = 91, oilTrip = 88, oilReset = 84;
        public double Ambient = 20;
        double oilUnloadMPa = 0.10, heatWIdle = 0.03, heatWLoad = 0.22, heatOIdle = 0.028, heatOLoad = 0.175, kBase = 0.0008;
        double wShut = 75, wFan1 = 79, wFan2 = 83, oShut = 62, oFan4 = 67, oFan3 = 72, hyst = 4;
        double powerLoss = 0.40, boostSlow = 1.5, heatGain = 0.30, coolLoss = 0.30, oilLoss = 0.45, stage1FullPct = 40, stage2StartPct = 90, stage2FullPct = 55;
        double rdmRate = 2.0, rdmStopShare = 0.25, rm1FaultA = 5000, slipFaultK = 0.7, ktnDropMinS = 10, ktnDropMaxS = 40;

        // ---- inputs, set by the simulation every frame
        public int Position, Reverser = 1, Crane = 2, Kvt = 2;      // reverser: 1 forward, 0 neutral, 2 back
        public bool EpkKey = true, LocoControl = true, MotionSw = true, ConvSw = true, RefrigSw = true, AlarmMon = true, SandBtn, HornOn;
        public readonly bool[] FanSw = new bool[4];
        public readonly bool[] FuelSw = { true, true };
        public readonly bool[] LaunchPress = new bool[2];
        public bool EstopPress;
        public bool TobSw;                 // toggle TOB "slip limitation" (relay RU18)
        public int DamageSel = 2, DamageSw;
        public double TmMPa = 0.5, Tc254MPa, LocoTcMPa;
        public bool EpkTripped, CompressorRunning, LocoVrBraking;
        public Action<string> Log = s => { };

        // ---- state
        public Section[] S = { new Section { Index = 0 }, new Section { Index = 1 } };
        public Room[] Rm = { new Room(), new Room() };   // apparatus outside the pult, by sections (Circuits.cs)
        readonly double[] compT = new double[2]; double mot = 6, uEff = 0.7, tobK = 0.75, apK = 0.75, kavK = 0.8;
        public int Sections = 2;
        int revEff = 1; bool rdvOk = true, sot;
        public bool EstopLatch, Sanding;
        public double BatteryA, T;
        public string Condition = "normal", ConditionNote = "";
        readonly Random rnd = new Random(); Random condRnd = new Random();
        public readonly int[] FaultOn = new int[40];   // by the kind of fault: 0 off, 1/2 section, 3 both
        readonly int[] faultPrev = new int[40];

        // the electric faults the instructor can put (kind = the fault number of the original instructor list and of the
        // package: 1 no fuel, 3 no battery charge, 4 section not loaded, 5 no traction, 6 only the 1st position, 7 no field
        // weakening, 8 cooling, 11 no fire, 12 random stop, 13 oil pressure falls, 14 refrigerator auto mode, 15 reverser,
        // 16 RM1, 17 KTN, 18 poor adhesion)
        public static readonly ElecFaultDef[] FaultList =
        {
            new ElecFaultDef { Code = 101, Kind = 1, Section = 2, Name = "Нет подачи топлива: дизель второй секции остановился" },
            new ElecFaultDef { Code = 102, Kind = 4, Section = 2, Name = "Секция 2 не нагружается" },
            new ElecFaultDef { Code = 103, Kind = 0, Section = 1, Name = "Компрессор первой секции не работает" },
            new ElecFaultDef { Code = 104, Kind = 13, Section = 1, Name = "Падение давления масла дизеля секции 1" },
            new ElecFaultDef { Code = 105, Kind = 18, Section = 3, Name = "Боксование: плохое сцепление колёс с рельсами" },
            new ElecFaultDef { Code = 106, Kind = 5, Section = 3, Name = "Нет тяги обеих секций" },
            new ElecFaultDef { Code = 107, Kind = 6, Section = 1, Name = "Секция 1 работает только как на 1-й позиции" },
            new ElecFaultDef { Code = 108, Kind = 7, Section = 1, Name = "Не включается ослабление поля секции 1" },
            new ElecFaultDef { Code = 109, Kind = 8, Section = 1, Name = "Не работает охлаждение секции 1 (перегрев)" },
            new ElecFaultDef { Code = 110, Kind = 14, Section = 1, Name = "Не работает авторежим холодильника секции 1" },
            new ElecFaultDef { Code = 111, Kind = 15, Section = 1, Name = "Не перебросился поездной реверсор секции 1" },
            new ElecFaultDef { Code = 112, Kind = 16, Section = 1, Name = "Срабатывание РМ1 при трогании, секция 1" },
            new ElecFaultDef { Code = 113, Kind = 17, Section = 1, Name = "Отпадание КТН, секция 1" },
            new ElecFaultDef { Code = 114, Kind = 12, Section = 2, Name = "Самопроизвольная остановка дизеля секции 2" },
            new ElecFaultDef { Code = 115, Kind = 11, Section = 2, Name = "Дизель секции 2 не запускается (нет вспышки)" },
            new ElecFaultDef { Code = 116, Kind = 3, Section = 1, Name = "Нет заряда аккумуляторной батареи" },
            // by the scheme (variant 008) and the list of faults of the order 671r: found by the lamps and the damage
            // indicator, repaired in the high-voltage chamber and at the breakers (card "ВВК и автоматы")
            new ElecFaultDef { Code = 117, Kind = 20, Section = 1, Name = "Сработало РЗ: пробой на корпус в цепи одного ТЭД, секция 1" },
            new ElecFaultDef { Code = 118, Kind = 21, Section = 1, Name = "Сработало РЗ: пробой в минусовой части силовой цепи, секция 1" },
            new ElecFaultDef { Code = 119, Kind = 22, Section = 1, Name = "Сработало РОП: обрыв цепи возбуждения одного ТЭД, секция 1" },
            new ElecFaultDef { Code = 120, Kind = 23, Section = 2, Name = "Сработало РМ2: короткое замыкание в выпрямительной установке, секция 2" },
            new ElecFaultDef { Code = 121, Kind = 24, Section = 1, Name = "Сработал автомат АВУ (обдув выпрямителя), секция 1" },
            new ElecFaultDef { Code = 122, Kind = 25, Section = 2, Name = "Сработал автомат 1АТ (обдув ТЭД), секция 2" },
            new ElecFaultDef { Code = 123, Kind = 26, Section = 1, Name = "Сработал автомат А4 «Управление возбуждением», секция 1" },
            new ElecFaultDef { Code = 124, Kind = 27, Section = 2, Name = "Сработал автомат А1 «Возбудитель», секция 2" },
            new ElecFaultDef { Code = 125, Kind = 28, Section = 1, Name = "Неисправность БУВ: нет возбуждения генератора, секция 1" },
            new ElecFaultDef { Code = 126, Kind = 29, Section = 1, Name = "Утечка воды: снижение уровня в расширительном баке, секция 1" },
            new ElecFaultDef { Code = 127, Kind = 30, Section = 2, Name = "Пожар в дизельном помещении, секция 2" },
            new ElecFaultDef { Code = 128, Kind = 31, Section = 1, Name = "Сработал автомат А5 «Компрессор», секция 1" },
            new ElecFaultDef { Code = 129, Kind = 32, Section = 1, Name = "Сработал автомат АУ «Управление общее»" },
            new ElecFaultDef { Code = 130, Kind = 33, Section = 2, Name = "Сработал автомат А3 «Дизель», секция 2" },
            new ElecFaultDef { Code = 131, Kind = 34, Section = 1, Name = "Открыта дверь ВВК (блокировка БД), секция 1" },
        };

        public Traction()
        {
            double rMotor = 0.028, ratedA = 720, uRated = 0.463, vCont = 24.7 / 3.6;
            rCircuitKvPerA = rMotor / 6.0 / 1000.0;
            emfK = (uRated - ratedA * rMotor / 1000.0) / vCont;
            fluxNorm = Math.Tanh(ratedA / fluxKneeA);
            fieldNorm = Math.Tanh(1.0 / fieldKnee);
            idleExp = Math.Log(uIdle1000 * 1000.0 / 80.0) / Math.Log(1000.0 / 350.0);
            foreach (var s in S) UpdateIdReadings(s);
        }

        // a new trip: both diesels running and warm (the locomotive is prepared), condition by the settings
        public void Init(int sections, string condition, int seed = 0)
        {
            Sections = Math.Max(1, Math.Min(2, sections));
            S = new[] { new Section { Index = 0 }, new Section { Index = 1 } };
            Rm = new[] { new Room(), new Room() }; compT[0] = compT[1] = 0;
            if (Sections < 2) S[1].Running = false;
            Array.Clear(FaultOn, 0, FaultOn.Length); Array.Clear(faultPrev, 0, faultPrev.Length);
            EstopLatch = false; rdvOk = true; sot = false; T = 0; revEff = Reverser;
            Condition = string.IsNullOrEmpty(condition) ? "normal" : condition;
            condRnd = seed != 0 ? new Random(seed) : new Random();
            AssignCondition();
            foreach (var s in S) { s.Ambient = Ambient; s.Water = 62; s.Oil = 60; s.Boost = s.Running ? PowerShare(350) : 0; UpdateIdReadings(s); }
            InitElectric();
        }

        void AssignCondition()
        {
            string mode = Condition.ToLowerInvariant().Trim();
            double num; bool numeric = double.TryParse(mode.TrimEnd('%'), NumberStyles.Float, C, out num);
            if (mode == "random") mode = new[] { "nominal", "normal", "worn" }[condRnd.Next(3)];
            if (mode == "номинал") mode = "nominal"; if (mode == "норма" || mode == "норм") mode = "normal"; if (mode == "дно") mode = "worn";
            if (!numeric && mode != "nominal" && mode != "worn") mode = "normal";
            double basePct = numeric ? Math.Max(40, Math.Min(100, num)) : mode == "nominal" ? 100 : mode == "worn" ? 70 : 88;
            double spread = 5 + condRnd.NextDouble() * 5;
            double hi = Math.Min(100, basePct + spread / 2), lo = hi - spread;
            bool firstBetter = condRnd.Next(2) == 0;
            double[] c = { firstBetter ? hi : lo, firstBetter ? lo : hi };
            for (int i = 0; i < 2; i++)
            {
                var x = S[i];
                x.Cond = Math.Max(0.4, Math.Min(1.0, c[i] / 100.0));
                var k = ConditionCurve(x.Cond * 100);
                x.PowerK = k[0]; x.BoostK = k[1]; x.HeatK = k[2]; x.CoolK = k[3]; x.OilK = k[4]; x.RdmK = k[5];
            }
            ConditionNote = string.Format(C, "{0}: секция 1 — {1:0} %, секция 2 — {2:0} %", numeric ? basePct.ToString("0", C) + " %" : mode == "nominal" ? "номинал" : mode == "worn" ? "«дно»" : "норма", S[0].Cond * 100, S[1].Cond * 100);
        }
        public double[] ConditionCurve(double pct)
        {
            double a = Stage1(pct), b = Stage2(pct), b70 = Stage2(70);
            return new[] { 1 - powerLoss * a, 1 + boostSlow * a, 1 + heatGain * a, 1 - coolLoss * a, 1 - oilLoss * b, b70 > 0 ? b / b70 : 0 };
        }
        double Stage1(double pct) { double x = Clamp01((100 - pct) / Math.Max(1, 100 - stage1FullPct)); return 1 - (1 - x) * (1 - x); }
        double Stage2(double pct) { double x = Clamp01((stage2StartPct - pct) / Math.Max(1, stage2StartPct - stage2FullPct)); return x * x * (3 - 2 * x); }
        static double Clamp01(double x) { return x < 0 ? 0 : (x > 1 ? 1 : x); }

        bool Fault(int kind, Section s) { int f = FaultOn[kind]; return f == 3 || f == s.Index + 1; }
        public void Event(string text) { Log(text); }

        public void SetFault(ElecFaultDef d, bool on)
        {
            if (d.Kind <= 0) return;
            FaultOn[d.Kind] = on ? d.Section : 0;
            // defects of the scheme: a breaker trips (the crew puts it back at the cabinet), a damaged circuit stays
            // damaged until the fault is taken off; taking the fault off also puts the breaker back
            if (d.Kind < 20) return;
            for (int i = 0; i < 2; i++)
            {
                if (d.Section != 3 && d.Section != i + 1) continue;
                var r = Rm[i];
                switch (d.Kind)
                {
                    case 20: r.GroundTed = on ? rnd.Next(6) : -1; break;
                    case 21: r.GroundMinus = on; break;
                    case 22: r.FieldBreakTed = on ? rnd.Next(6) : -1; break;
                    case 23: r.Rm2Short = on; break;
                    case 24: r.AVU = !on; break;
                    case 25: r.AT1 = !on; break;
                    case 26: r.A4 = !on; break;
                    case 27: r.A1 = !on; break;
                    case 28: r.BuvBad = on; break;
                    case 29: r.WaterLeak = on; break;
                    case 30: r.Fire = on; if (on) Event("Секция " + (i + 1) + ": сработала пожарная сигнализация (РУ6), лампа «Пожар»"); break;
                    case 31: r.A5 = !on; break;
                    case 32: Rm[0].AU = !on; break;
                    case 33: r.A3 = !on; break;
                    case 34: r.Doors = !on; break;
                }
            }
        }

        // a fault selected by the instructor happens once (15, 17); unticking it repairs
        void FaultsChanged()
        {
            foreach (var s in S)
            {
                if (Fault(15, s)) { if (s.RevState == 0) { s.RevState = revEff != 0 ? 1 : 3; Event("Секция " + (s.Index + 1) + ": не перебросился поездной реверсор"); } }
                else s.RevState = 0;
                if (Fault(17, s)) { if (!s.KtnPending && !s.KtnOff) { s.KtnPending = true; s.KtnDropAt = -1; } }
                else { s.KtnPending = false; s.KtnOff = false; }
                if (!Fault(16, s)) s.Rm1 = false;
            }
        }
        void CabFaults()
        {
            foreach (var s in S)
            {
                if (s.RevState == 3 && revEff != 0) s.RevState = 1;
                else if (s.RevState == 1 && revEff == 0) s.RevState = 2;
                else if (s.RevState == 2 && revEff != 0) { s.RevState = 0; Event("Секция " + (s.Index + 1) + ": поездной реверсор переброшен"); }
                if (s.Rm1 && Position == 0) s.Rm1 = false;
                if (s.KtnOff && !FuelSw[s.Index]) { s.KtnOff = false; Event("Секция " + (s.Index + 1) + ": топливный насос выключен, КТН готов"); }
            }
        }

        // ================================================================== step
        public void Step(double dt, double vKmh)
        {
            if (!(dt > 0)) return;
            T += dt;
            double v = Math.Abs(vKmh);
            if (Position == 0) revEff = Reverser;      // the reverse handle is locked while the main handle is off zero
            bool changed = false;
            for (int k = 0; k < FaultOn.Length; k++) if (FaultOn[k] != faultPrev[k]) { faultPrev[k] = FaultOn[k]; changed = true; }
            if (changed) FaultsChanged();
            CabFaults();
            // AU "common control" switched off and on drops the self-holding relay RU3 of the emergency stop (the real
            // way to reset it); A4 "excitation control" switched off and on releases the latched RZ, ROP and RM2
            for (int i = 0; i < 2; i++)
            {
                var r = Rm[i];
                if (r.A4 && !r.A4Prev && r.PowerProtection) { r.RZ = r.ROP = r.RM2 = false; Event("Секция " + (i + 1) + ": автомат А4 включён повторно, защита силовой цепи восстановлена"); }
                r.A4Prev = r.A4;
            }
            ElectricStep(dt, v);
            for (int i = 0; i < 2; i++) SectionStep(S[i], dt, v);
            CompressorStep(dt);
            for (int i = 0; i < 2; i++) LaunchPress[i] = false;
            Sanding = Electric.Apps.Values.Any(a => a.On && (a.Name == "1КП1" || a.Name == "1КП2" || a.Name == "2КП1" || a.Name == "2КП2"));
            Battery(dt);
        }

        public bool Sot { get { return sot; } }
        public bool RdvOk { get { return rdvOk; } }

        void SectionStep(Section s, double dt, double v)
        {
            bool exists = s.Index < Sections;
            if (s.KtnPending && s.Running)
            {
                if (s.KtnDropAt < 0) s.KtnDropAt = T + ktnDropMinS + rnd.NextDouble() * Math.Max(0, ktnDropMaxS - ktnDropMinS);
                else if (T >= s.KtnDropAt) { s.KtnPending = false; s.KtnDropAt = -1; s.KtnOff = true; Event("Секция " + (s.Index + 1) + ": отпал КТН, топливный насос остановлен"); }
            }
            var rm = Rm[s.Index]; var lead = Rm[0];
            SchemeDiesel(s, dt);

            // ---- rpm and boost
            int kmP = SchemePosition(s.Index);   // MR1-MR4 are fed from AU through the controller
            if (s.Running)
            {
                double target = RpmTable[kmP];
                if (s.Overload > 0) target = Math.Max(0.75 * target, target * (1 - sagDroop * s.Overload));
                if (s.Rpm < target) s.Rpm = Math.Min(target, s.Rpm + (s.Rpm < 850 ? rpmUp : rpmUpTop) * dt);
                else s.Rpm = Math.Max(target, s.Rpm - rpmDown * dt);
                double visc = Math.Max(0.85, Math.Min(1.3, 1 + 0.004 * (80 - s.Oil)));
                if (Fault(13, s)) s.OilFaultK = Math.Max(0.15, s.OilFaultK - dt / 120.0); else s.OilFaultK = 1;
                double pTarget = (0.25 + 0.35 * (s.Rpm - 350) / 650.0) * visc * s.OilFaultK * s.OilK;
                if (s.RdmDipT > 0) pTarget = Math.Min(pTarget, s.RdmDipP);
                s.OilP += (pTarget - s.OilP) * Math.Min(1, dt / (s.RdmDipT > 0 ? 0.5 : 1.5));
            }
            double fr = PowerShare(s.Rpm);
            s.Boost += ((s.Running ? fr : 0) - s.Boost) * Math.Min(1, dt / (boostTau * s.BoostK));
            bool unloadWas = s.OilUnload;
            s.OilUnload = s.Running && s.OilP < oilUnloadMPa;
            if (s.OilUnload && !unloadWas) { if (s.RdmDipT > 0) s.RdmUnloads++; Event(string.Format(C, "Секция {0}: РДМ — сброс нагрузки, масло {1:0.0} кгс/см²", s.Index + 1, s.OilP * KgfPerMPa)); }

            // ---- assembly (RV3, train contactors); RU8 blocks it above the 1st position
            // excitation of the generator (sheets 3 and 5): A1 feeds the exciter, A4 the coils of KV and VV through RKV, the
            // contacts of RZ, ROP, RM2 and the door interlocks BD; BUV (A12) drives the thyristors, without it the switch AP
            // gives the emergency excitation
            int connected = 0;
            for (int m = 1; m <= 6; m++) if (E("П" + m, s.Index) && E("ТЭД_" + m, s.Index)) connected++;
            mot = Math.Max(1, connected);
            bool excSupply = E("Возбудитель", s.Index) && E("ОВ_генератора", s.Index);
            s.Assembled = exists && s.Running && E("РУ5", s.Index) && connected > 0 && E("КВ", s.Index) && E("ВВ", s.Index) && excSupply;
            s.PickT = 0;
            s.FwStage = E("ВШ2", s.Index) ? 2 : E("ВШ1", s.Index) ? 1 : 0;
            double beta = s.FwStage == 2 ? beta2 : (s.FwStage == 1 ? beta1 : 1.0);

            // ---- power regulation and excitation
            if (s.Assembled && kmP > 0) s.PosHeld = kmP;
            if (!s.Assembled) s.PosHeld = 0;
            int pos = s.Assembled ? (kmP > 0 ? kmP : s.PosHeld) : 0;
            double nr = s.Running ? s.Rpm / 1000.0 : 0, n3 = nr * nr * nr;
            bool compThis = compT[s.Index] > 0;
            bool ggLive = s.Running && s.Field > 0.02;
            double ggAux = ggLive ? (tedFansKw + vuFanKw) * n3 + s.CoolFanKw : 0;
            double sgShaft = s.Running ? (sgBaseKw + (compThis ? compMotorKw * Math.Min(1, nr) : 0) + excKw * Math.Min(1, s.Field / Math.Max(0.1, fieldMax))) / sgEff : 0;
            s.AuxKw = ggAux / ggEff + sgShaft;
            double pProg = s.Running ? dieselKw * s.PowerK * fr : 0;
            s.AvailKw = s.Running ? dieselKw * s.PowerK * Math.Min(fr, 0.35 * fr + 0.65 * s.Boost) : 0;
            double uiKw = s.VoltageKv * s.Current;
            s.ShaftKw = s.Running ? (uiKw + ggAux) / ggEff + sgShaft : 0;
            s.Overload = s.Running && s.AvailKw > 1 ? Math.Max(0, s.ShaftKw / s.AvailKw - 1) : 0;
            s.FuelRack = s.Running ? Clamp01(0.10 + 0.90 * s.ShaftKw / Math.Max(150, dieselKw * nr)) : 0;
            double idErr = pProg > 1 ? (s.AvailKw - s.ShaftKw) / pProg : 0;
            if (s.Assembled && Position > 0)
            {
                double dId = idErr * dt / (idErr < 0 ? idDownS : idUpS);
                s.IdK += Math.Max(-idDownRate * dt, Math.Min(idUpRate * dt, dId));
            }
            else s.IdK += (1 - s.IdK) * Math.Min(1, dt / idUpS);
            if (s.Slipping) s.SlipLongT += dt; else s.SlipLongT = Math.Max(0, s.SlipLongT - 2 * dt);
            if (E("МР5", s.Index)) s.IdK -= dt / 0.5;
            s.IdK = Math.Max(0.05, Math.Min(1.15, s.IdK));
            UpdateIdReadings(s);
            bool excTr = s.Assembled && E("РУ5", s.Index);
            bool excIdle = !E("РУ5", s.Index) && s.Running && E("КВ", s.Index) && E("ВВ", s.Index) && excSupply;
            bool excOn = excTr || excIdle;
            s.Vv = excOn;
            s.Exc += ((excOn ? 1.0 : 0) - s.Exc) * Math.Min(1, dt / (excOn ? excUp : excDown));
            if (s.SlipUnloadT > 0) s.SlipUnloadT -= dt;
            if (s.SandT > 0) s.SandT -= dt;
            double vk = Math.Max(v, 0.3), vm = v / 3.6;
            double pHyp = s.IdK * pProg * ggEff;
            // SU resistor strengths are an engineering approximation; the RV4 hold time comes from the circuit.
            if (E("СУ_СБ1", s.Index)) pHyp *= 0.75;
            if (E("СУ_СБ2", s.Index)) pHyp *= 0.75;
            // emergency excitation (AP): the natural characteristic of the exciter, on positions 1-3 the contactor KAV adds
            // a step of the resistor SAV; TOB (RU18) puts the resistor SSU2.5 into the selective unit and holds the voltage
            if (rm.AP) pHyp *= apK * (pos <= 3 ? kavK : 1.0);
            pHyp *= mot / 6.0;      // a cut-out OM shunts the potentiometer of the inductive sensor: less power for the rest
            uEff = uLimKv * (E("РУ18", s.Index) ? tobK : 1.0);   // RU18 through contact 6 of the controller: positions 3-15
            int suChannel = 0;
            double iStar = excTr && pos > 0 ? OperatingCurrent(vm, beta, pHyp, CurrentLimit[pos] * mot / 6.0, out suChannel) * (E("РУ17", s.Index) ? 0.5 : 1.0) : 0;
            s.SuChannel = iStar > 0 && rm.AP ? 4 : suChannel;
            double uStar = iStar > 0 ? MotorEmf(iStar, beta, vm) + iStar * RCirc : 0;
            double egPerField = egNomKv * nr;
            double fieldT = 0;
            if (excTr && iStar > 0 && egPerField > 0.01) fieldT = FieldFor((uStar + iStar * rGgKvPerA) / egPerField);
            else if (excIdle && egPerField > 0.01) fieldT = FieldFor(IdleVoltageKv(nr) / egPerField);
            double ufCeiling = fieldUfMax * nr * (1 + tkCompoundK * s.Field);
            double uf = excOn ? Math.Max(0, Math.Min(ufCeiling, fieldT + fieldForce * (fieldT - s.Field))) : 0;
            if (s.VoltageKv > 0.99 * uEff) uf = Math.Min(uf, s.Field * 0.99 * uEff / s.VoltageKv);
            s.ExciterField += (uf - s.ExciterField) * Math.Min(1, dt / svTauS);
            s.Field += (s.ExciterField - s.Field) * Math.Min(1, dt / (s.ExciterField >= s.Field ? fieldTauS : fieldTauDownS));
            if (s.Field < 0.001 && !excOn) s.Field = 0;
            double eg = egPerField * GenFlux(s.Field);
            double iCircuit = s.Assembled ? CurrentAtEmf(eg, beta, vm) : 0;
            s.Current += (iCircuit - s.Current) * Math.Min(1, dt / motorTauS);
            if (s.Current < 0.5 && iCircuit == 0) s.Current = 0;
            s.VoltageKv = s.Assembled ? Math.Max(0, eg - s.Current * rGgKvPerA) : eg;
            if (Fault(16, s) && s.Assembled && s.Current > rm1FaultA && !s.Rm1)
            {
                s.Rm1 = true; s.Rm1Trips++;
                Event(string.Format(C, "Секция {0}: сработало РМ1 при {1:0} А — контроллер на ноль", s.Index + 1, s.Current));
            }
            // protections of the power circuit: earthing relay RZ (20 A, knife switch VRZ), field break relay ROP, maximum
            // current relay RM2; each latches, takes KV and VV off and lights the lamp "power protection"
            if (s.Assembled && s.VoltageKv > 0.1)
            {
                bool ground = (rm.GroundTed >= 0 && rm.OM[rm.GroundTed]) || rm.GroundMinus;
                if (ground && rm.VRZ && !rm.RZ) { rm.RZ = true; Event("Секция " + (s.Index + 1) + ": сработало реле заземления РЗ — возбуждение снято, лампа «Силовая защита»"); }
                if (rm.FieldBreakTed >= 0 && rm.OM[rm.FieldBreakTed] && s.Current > 300 && !rm.ROP) { rm.ROP = true; Event("Секция " + (s.Index + 1) + ": сработало реле обрыва полюсов РОП — возбуждение снято"); }
                if (rm.Rm2Short && s.Current > 800 && !rm.RM2) { rm.RM2 = true; Event("Секция " + (s.Index + 1) + ": сработало реле максимального тока РМ2 — возбуждение снято"); }
            }
            double f = forcePerA * Flux(beta * s.Current / mot) * s.Current;
            double psi = 0.118 + 5.0 / (27.5 + vk);
            double fAdh = psi * SectionMassT * 9.81 * (mot / 6.0) * AdhesionFactor * (SandingSection(s.Index) ? sandGain : 1.0) * (Fault(18, s) ? slipFaultK : 1.0);
            bool slipWas = s.Slipping;
            s.Slipping = s.Assembled && f > fAdh;
            if (s.Slipping && !slipWas) s.SlipCount++;
            if (s.Slipping) { f = 0.8 * fAdh; s.SlipUnloadT = slipUnloadS; s.SandT = 3; }
            s.Force = f;
            s.PowerKw = f * vm;

            // ---- cooling
            // refrigerator (sheet 7): breaker A6, toggle TH of the leading cab. "Automatic": the temperature relays 0V, 1V, 2V
            // (water) and 0M, 1M, 2M (oil) open the side shutters (VP5, VP6) and switch the contactors K1, K2 (fans 1, 2) and
            // K4, K3 (fans 4, 3) with their upper shutters. "Manual": only the toggles T1-T4, each its fan and shutters.
            s.ShutW = E("ВП5", s.Index); s.ShutO = E("ВП6", s.Index);
            for (int i = 0; i < 4; i++) s.FanOn[i] = E("К" + (i + 1), s.Index) && E("ВП" + (i + 1), s.Index);
            bool shW = s.ShutW, shO = s.ShutO;
            double fGg = s.Running && s.Field > 0.02 ? s.Rpm / 1000.0 : 0;
            double rho = 293.0 / (273.0 + Ambient);
            s.CoolFanKw = 0;
            for (int i = 0; i < 4; i++)
            {
                double target = s.FanOn[i] ? fGg : 0, sp0 = s.FanSpeed[i];
                s.FanSpeed[i] += (target - s.FanSpeed[i]) * Math.Min(1, dt / (target > s.FanSpeed[i] ? fanRunUpS : fanCoastS));
                if (s.FanOn[i])
                {
                    s.CoolFanKw += coolFanKw * s.FanSpeed[i] * s.FanSpeed[i] * s.FanSpeed[i] * rho;
                    double acc = Math.Max(0, s.FanSpeed[i] - sp0) / Math.Max(1e-6, dt);
                    s.CoolFanKw += fanStartK * coolFanKw * s.FanSpeed[i] * acc * fanRunUpS;
                }
            }
            double ram = qNatural + qRam * Math.Min(1.2, v / 100.0);
            double airW = (shW ? 1 : closedLeak) * (ram + s.FanSpeed[0] + s.FanSpeed[1]) * rho;
            double airO = (shO ? 1 : closedLeak) * (ram + s.FanSpeed[2] + s.FanSpeed[3]) * rho;
            double load = s.ShaftKw / dieselKw, hk = s.HeatK * (rm.WaterLeak ? 1.35 : 1.0);
            double heatW = s.Running ? (heatWIdle + heatWLoad * load) * hk : 0, heatO = s.Running ? (heatOIdle + heatOLoad * load) * hk : 0;
            double kW = kBase + uaWater * s.CoolK * Math.Pow(airW, airExp), kO = kBase + uaOil * s.CoolK * Math.Pow(airO, airExp);
            s.Water += (heatW - kW * (s.Water - Ambient)) * dt / thermalInertia;
            s.Oil += (heatO - kO * (s.Oil - Ambient)) * dt / thermalInertia;
            bool ow = s.OverheatW, oo = s.OverheatO;
            if (s.Water > waterTrip) s.OverheatW = true; else if (s.Water < waterReset) s.OverheatW = false;
            if (s.Oil > oilTrip) s.OverheatO = true; else if (s.Oil < oilReset) s.OverheatO = false;
            if (s.OverheatW && !ow) Event(string.Format(C, "Секция {0}: ТРВ — перегрев воды {1:0} °C, нагрузка снята", s.Index + 1, s.Water));
            if (s.OverheatO && !oo) Event(string.Format(C, "Секция {0}: ТРМ — перегрев масла {1:0} °C, нагрузка снята", s.Index + 1, s.Oil));

            // fuel: 192 g/kWh of the shaft power plus the friction of the running diesel. At the full power it is 200 g/kWh
            // (passport of 1A-9DG-2: 204 +- 10.2), at idle 15 kg/h (rheostat tests of 2TE116: 0.25 kg per minute)
            if (s.Running) s.FuelKg += 0.192 * (s.ShaftKw + 45 + 50 * nr) * dt / 3600.0;
        }

        void RdmChance(Section s, double dt)
        {
            if (s.RdmDipT > 0) { s.RdmDipT -= dt; return; }
            double wear = s.RdmK;
            if (wear <= 0) return;
            double load = s.PowerKw / rimKw;
            double perHour = rdmRate * wear * (0.4 + 0.6 * load) * (s.Oil > 80 ? 1.3 : 1.0);
            if (condRnd.NextDouble() >= perHour * dt / 3600.0) return;
            bool deep = condRnd.NextDouble() < rdmStopShare * Math.Min(1.5, wear);
            s.RdmDipP = deep ? 0.04 : 0.08;
            s.RdmDipT = deep ? 5.5 + 3 * condRnd.NextDouble() : 2 + 4 * condRnd.NextDouble();
        }

        double Flux(double iField) { return Math.Tanh(Math.Max(0, iField) / fluxKneeA) / fluxNorm; }
        double MotorEmf(double i, double beta, double vm) { return emfK * Flux(beta * i / mot) * vm; }
        void UpdateIdReadings(Section s)
        {
            double idNorm = Clamp01((s.IdK - 0.05) / 1.10);
            s.IdAnchorMm = idNorm * IdStrokeMm;
            s.IdImpedanceOhm = IdZMinOhm + (IdZMaxOhm - IdZMinOhm) * Math.Pow(1 - idNorm, 1.6);
        }
        double OperatingCurrent(double vm, double beta, double pKw, double iLim, out int activeChannel)
        {
            double lo = 0, hi = iLim;
            if (CircuitOk(hi, vm, beta, pKw)) { activeChannel = 1; return hi; }
            for (int k = 0; k < 40; k++) { double mid = 0.5 * (lo + hi); if (CircuitOk(mid, vm, beta, pKw)) lo = mid; else hi = mid; }
            activeChannel = MotorEmf(lo, beta, vm) + lo * RCirc >= 0.995 * uEff ? 3 : 2;
            return lo;
        }
        double RCirc { get { return rCircuitKvPerA * 6.0 / mot; } }
        bool CircuitOk(double i, double vm, double beta, double pKw) { double u = MotorEmf(i, beta, vm) + i * RCirc; return u * i <= pKw && u <= uEff; }
        double FieldFor(double e) { double need = Math.Max(0, e) * fieldNorm; return need >= 0.999 ? fieldMax : Math.Min(fieldMax, fieldKnee * 0.5 * Math.Log((1 + need) / (1 - need))); }
        double IdleVoltageKv(double nr) { return nr <= 0 ? 0 : uIdle1000 * Math.Pow(nr, idleExp); }
        double GenFlux(double f) { return Math.Tanh(Math.Max(0, f) / fieldKnee) / fieldNorm; }
        double CurrentAtEmf(double eg, double beta, double vm)
        {
            if (eg <= 0) return 0;
            double lo = 0, hi = 20000;
            for (int k = 0; k < 40; k++) { double mid = 0.5 * (lo + hi); if (MotorEmf(mid, beta, vm) + mid * (RCirc + rGgKvPerA) < eg) lo = mid; else hi = mid; }
            return lo;
        }
        bool Stage(bool auto, bool on, double t, double onC) { if (!auto) return false; if (t >= onC) return true; if (t <= onC - hyst) return false; return on; }

        // the trip begins with the fuel pump toggle of the pult off: the diesel of the section is not started
        public void BeginStopped(int i)
        {
            if (i >= Sections || !S[i].Running) return;
            SetState(S[i], DieselState.Off); S[i].OilP = 0;
            Event("Секция " + (i + 1) + ": тумблер «Топливный насос» на пульте выключен — дизель не запущен");
        }
        void SetState(Section s, DieselState st)
        {
            s.State = st; s.StateT = 0;
            if (st != DieselState.Running) { s.Assembled = false; s.Exc = 0; s.Force = 0; s.Current = 0; s.VoltageKv = 0; s.PowerKw = 0; s.Field = 0; s.ExciterField = 0; s.SuChannel = 0; s.FuelRack = 0; s.Overload = 0; }
            if (st == DieselState.Off) { s.Rpm = 0; s.RandomStopAt = -1; }
        }

        void Battery(double dt)
        {
            var s1 = S[0];
            bool p2 = Sections > 1 && OilPumpOn(1);
            if (E("Заряд_АБ", 0)) { s1.ChargeA = Math.Max(8, s1.ChargeA - 0.5 * dt); BatteryA = s1.ChargeA; }
            else if (s1.State == DieselState.Cranking) BatteryA = 0;
            else if (OilPumpOn(0)) BatteryA = p2 ? -30 : -20;
            else BatteryA = p2 ? -10 : 0;
        }

        // ---- readings
        // sound of the diesel of section i: index of the position whose rpm is the nearest
        public int SoundPos(int i)
        {
            var s = S[i];
            if (s.Rpm < 410) return Position >= 1 && s.Assembled ? 1 : 0;
            int best = 2;
            for (int k = 3; k < 16; k++) if (Math.Abs(RpmTable[k] - s.Rpm) < Math.Abs(RpmTable[best] - s.Rpm)) best = k;
            return best;
        }
        public double TotalForceKn { get { double f = 0; for (int i = 0; i < Sections; i++) f += S[i].Force; return f; } }
        public double FuelKg { get { double f = 0; foreach (var s in S) f += s.FuelKg; return f; } }
        public double RpmShare(int i) { return i < Sections && S[i].Running ? S[i].Rpm / 1000.0 : 0; }
        public int ReverserEff { get { return revEff; } }
        // control supply of the leading cab: AU - brake interlock - contacts of the reverse handle
        public bool ControlFeed { get { return Rm[0].VB && Rm[0].AU && Rm[0].BU && Reverser != 0; } }
        // LN "load shedding": from A4 through RU10 and RU11 (RU11 is on while VV is off, and at a deep slip), and from AU
        // through the opening contacts of RU11 and RU5 on positions 1-15 (no change from idle to traction)
        public bool LoadShed(int i)
        {
            return i < Sections && Electric.On("ЛН" + (i + 1));
        }
        // RDM1 by the rpm ("Lokomotiv" 3/2017, fig. 41): the lamp from 1.3 kgf/cm2 at 350 rpm to 5.5 at 1000
        bool Rdm1(Section s) { return s.Running && s.OilP * KgfPerMPa < 1.3 + 4.2 * Math.Max(0, s.Rpm - 350) / 650.0; }
        // ---- compressor (sheet 4): the pressure relay RDK (7.5 on / 9.0 off) switches RU24 of both sections through the
        // toggle TRK of the leading section; RU24 - RV1 - KDK start the motor 2P2K through the resistor SPK, in 2 s KUDK
        // shunts it. The motor is fed by the starter-generator (110 V), so it does not follow the rpm of the diesel.
        void CompressorStep(double dt)
        {
            for (int i = 0; i < 2; i++) compT[i] = E("Компрессор", i) ? (E("КУДК", i) ? 2 : 0.1) : 0;
        }
        // share of the delivery of the compressor of section i: half while the starting resistor is in (RV1, 2 s)
        public double CompShare(int i) { return i >= Sections || compT[i] <= 0 ? 0 : compT[i] < 2 ? 0.5 : 1; }

        // ---- signal lamps (sheet 8). The common lamps (oil pressure, water level, cooling, fire) show both sections;
        // the toggle "damage" (relay RU12) takes the own section off them, what stays lit belongs to the second one.
        public bool DamageTp { get { return DamageSel == 1; } }
        bool Common(Func<int, bool> f) { bool own = !DamageTp && f(0), other = Sections > 1 && f(1); return own || other; }
        // LOT, LDM, LUV and LRZ are fed from AU through the reverse handle; LO and LRT from A4
        // The toggle TKS "lamp test" has three contacts (wires 2754, 2840, 2841): it feeds LOT, LDM and LUV straight from
        // wire 2992 (AU - brake interlock - reverse handle), past the sensors and RU12. The other lamps are not tested by it.
        public bool LampTest { get { return ControlFeed && !AlarmMon; } }
        public bool LampWaterLevel { get { return Electric.On("ЛУВ"); } }   // LUV: RUV
        public bool LampOilPressure { get { return Electric.On("ЛДМ"); } }       // LDM: RDM1, RU10
        public bool LampBraked { get { return Electric.On("ЛОТ"); } }                   // LOT: DOT, 0.4 kgf/cm2
        // LD2: contact of RU9 of the second section, fed there from A3 (wire 1159), through the intersection wire 5
        public bool LampDiesel2 { get { return Electric.On("ЛД2"); } }
        // LO: A4 of the section - opening auxiliary contacts of AVU, 1AT, 2AT - RU12 (own section) - lamp
        public bool LampCooling { get { return Electric.On("ЛО"); } }
        // LPS: relays RUP1, RUP2 of the fire alarm give the voltage of AU of their section to the lamp and the siren
        public bool LampFire { get { return Electric.On("ЛПС"); } }
        bool FireOn(int i) { return Rm[i].Fire && Rm[i].VB && Rm[i].AU; }
        // LRT: A4 - closing contact of RU1 - lamp; RU1 itself is fed from AU through the brake interlock (no reverse handle)
        public bool LampBrakePipe(bool sensor418) { return Electric.On("ЛРТ"); }

        // ---- the lamps of the pult as they are lit: order of the cells of the exchange. A burnt lamp stays dark
        public const int LampCount = 13;
        public static readonly string[] LampNames = { "Сброс нагрузки 1", "Сброс нагрузки 2", "Уровень воды", "Давление масла", null, "Обрыв торм. магистрали",
            "Отпуск тормозов", "Пожар", "Дизель 2", "Обдув ВУ и ТЭД", "Силовая защита", null, null };
        public bool[] LampBurnt = new bool[LampCount];
        public bool[] PultLamps(bool sensor418)
        {
            var l = new bool[LampCount];
            l[0] = LoadShed(0); l[1] = LoadShed(1); l[2] = LampWaterLevel; l[3] = LampOilPressure; l[5] = LampBrakePipe(sensor418);
            l[6] = LampBraked; l[7] = LampFire; l[8] = LampDiesel2; l[9] = LampCooling; l[10] = LampPowerProtection;
            for (int i = 0; i < LampCount; i++) if (LampBurnt[i]) l[i] = false;
            return l;
        }
        public bool LampPowerProtection { get { return Electric.On("ЛРЗ"); } }   // LRZ: RZ, ROP

        // ---- damage indicator UP (sheet 8, block BS.2): the selector PU takes section 1 or 2, the circuit goes through
        // the reverse handle of the controller. Which chain is measured is decided by RU10 and RU8, as on the scheme.
        public List<Link> DamageChain(int i, out string name)
        {
            return SchemeDamageChain(i, out name);
        }
        public double DamageIndicator
        {
            get
            {
                if (DamageSw == 0 || revEff == 0) return 0;
                int i = DamageSw == 2 ? 1 : 0;
                if (i >= Sections) return 0;
                string name;
                return Chain.Reading(DamageChain(i, out name));
            }
        }
    }
}
