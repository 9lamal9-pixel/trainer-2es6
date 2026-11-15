using System;
using System.Collections.Generic;

namespace Trainer2TE116
{
    /// <summary>
    /// Узел пневматической сети с точным балансом массы воздуха и учётом
    /// политропного (термического) отклонения давления при быстрых процессах.
    /// </summary>
    public class AirNode
    {
        public double V;
        public double M;

        /// <summary>
        /// Относительное температурное отклонение (T - T0) / T0 при политропном расширении/сжатии.
        /// При полном теплообмене со стенками стремится к 0, сохраняя точный баланс массы M.
        /// </summary>
        public double ThermalDelta;

        // Compatibility view: Wk stores the combined chamber mass, while its pressure readout shows Rk.
        // A pressure view must not be encoded as temperature and clipped by the physical thermal limits.
        internal AirNode PressureViewSource;

        public AirNode(double volumeM3, double gaugeMPa)
        {
            V = volumeM3;
            M = Air.Mass(gaugeMPa, volumeM3);
            ThermalDelta = 0.0;
        }

        public double Pa
        {
            get
            {
                if (PressureViewSource != null) return PressureViewSource.Pa;
                if (V <= 0.0) return Air.Patm;
                double kT = 1.0 + ThermalDelta;
                if (kT < 0.85) kT = 0.85;
                if (kT > 1.15) kT = 1.15;
                return (M * Air.RT / V) * kT;
            }
        }

        public double MPa { get { return (Pa - Air.Patm) / 1000000.0; } }

        public double Cap { get { return V / Air.RT; } }
    }

    /// <summary>
    /// Газодинамические функции истечения через дроссельные отверстия (Сен-Венан — Ванцель)
    /// и течения по трубам тормозной магистрали.
    /// </summary>
    public static class Air
    {
        public const double RT = 84148.7075; // R = 287.05 Дж/(кг·К) при T0 = 293.15 К (20 °C)
        public const double Patm = 101325.0; // Атмосферное давление, Па
        public const double Cd = 0.8;        // Коэффициент расхода отверстия
        private static readonly double sqrtRT = Math.Sqrt(RT);

        public static double Mass(double gaugeMPa, double volumeM3)
        {
            return (gaugeMPa * 1000000.0 + Patm) * volumeM3 / RT;
        }

        public static double Orifice(double p1, double p2, double area)
        {
            if (area <= 0.0 || p1 == p2) return 0.0;

            bool rev = p2 > p1;
            double pHigh = rev ? p2 : p1;
            double pLow = rev ? p1 : p2;
            if (pHigh <= 0.0) return 0.0;

            double r = pLow / pHigh;
            double psi;
            if (r < 0.528)
            {
                // Критическое (надкритическое) истечение при r < 0.528
                psi = 0.6847;
            }
            else if (r > 0.998)
            {
                // Линеаризация при малых перепадах во избежание бесконечной производной
                psi = Psi(0.998) * (1.0 - r) / 0.002;
            }
            else
            {
                psi = Psi(r);
            }

            double mDot = Cd * area * pHigh * psi / sqrtRT;
            return rev ? (-mDot) : mDot;
        }

        private static double Psi(double r)
        {
            double val = 7.0 * (Math.Pow(r, 1.4285714) - Math.Pow(r, 1.7142857));
            return val > 0.0 ? Math.Sqrt(val) : 0.0;
        }

        public static double Pipe(double p1, double p2, double area, double lossK)
        {
            double dp = p1 - p2;
            double pAvg = 0.5 * (p1 + p2);
            double absDp = Math.Abs(dp);
            double mDot;

            if (absDp < 1000.0)
            {
                mDot = area * Math.Sqrt(2.0 * pAvg * 1000.0 / (RT * lossK)) * (absDp / 1000.0);
            }
            else
            {
                mDot = area * Math.Sqrt(2.0 * pAvg * absDp / (RT * lossK));
            }

            return (dp < 0.0) ? (-mDot) : mDot;
        }

        public static void Move(AirNode from, AirNode to, double dm)
        {
            from.M -= dm;
            to.M += dm;
        }
    }

    /// <summary>
    /// Пневматические узлы одного вагона с воздухораспределителем усл. № 483-000 / 483М:
    /// магистральная часть № 483-010, главная часть № 270-023 и двухкамерный резервуар № 295-001
    /// (рабочая камера РК = 6,0 л и золотниковая камера ЗК = 4,5 л).
    /// </summary>
    public class WagonAir
    {
        public AirNode Tm;
        public AirNode Zr;

        /// <summary>
        /// Суммарный представительный узел двухкамерного резервуара (10,5 л) для обратной совместимости
        /// с экраном пневмосхемы AirScheme и расчётом баланса массы TotalMass().
        /// Масса Wk.M всегда равна Rk.M + Zk.M, а давление Wk.MPa отражает рабочую камеру РК.
        /// </summary>
        public AirNode Wk;

        /// <summary>Рабочая камера (РК) двухкамерного резервуара № 295-001 объёмом 6,0 л.</summary>
        public AirNode Rk;

        /// <summary>Золотниковая камера (ЗК) двухкамерного резервуара № 295-001 объёмом 4,5 л.</summary>
        public AirNode Zk;

        public AirNode Tc;

        public bool Braking;
        public bool Quick;
        public bool Emergency;
        public bool VrOn = true;
        public bool Stuck;
        public bool Mountain;
        public int Mode = 2; // 1 = Порожний (П), 2 = Средний (С), 3 = Гружёный (Г)
        public double TcMax = 0.3;

        public double QuickUntil;
        public double QuickFrom;
        public double TrigT;
        public double TmLevel = 9.0;
        public double TmRate;
        public double TmPrev;
        public double EmergencyUntil;

        /// <summary>Уровень перепада (РК - ЗК) на предыдущей ступени торможения для повторного подхвата КДР.</summary>
        public double PrevStageDropMPa;

        /// <summary>Таймер периодического подхвата клапана дополнительной разрядки 26 в хвосте длинного поезда.</summary>
        public double TailKdrCooldownUntil;

        public double LeakTm = 2.8e-07;
        public double LeakZr = 2e-09;
        public double ReleaseArea = 3e-06;

        /// <summary>Синхронизирует агрегированный узел Wk (10,5 л) с реальными камерами Rk (6,0 л) и Zk (4,5 л).</summary>
        public void SyncWkFromChambers()
        {
            if (Rk == null || Zk == null || Wk == null) return;
            Wk.M = Rk.M + Zk.M;
            // Wk is a compatibility readout, not a third physical reservoir. Keep its mass exact and
            // display the working chamber directly, even when Rk and Zk differ during deep braking.
            Wk.PressureViewSource = Rk;
            Wk.ThermalDelta = 0.0;
        }
    }

    /// <summary>
    /// Физическая модель пневматической тормозной сети тепловоза 2ТЭ116 и поезда:
    /// - Компрессоры КТ-7 с регулятором давления 3РД (7,5–9,0 кгс/см²);
    /// - Кран машиниста усл. № 395М с политропной термодинамикой УР (20 л), редуктором, стабилизатором и реле-повторителем;
    /// - Кран вспомогательного тормоза усл. № 254 с повторителем усл. № 304 и ВР локомотива усл. № 292-001;
    /// - Датчик целостности тормозной магистрали усл. № 418;
    /// - Одномерная газодинамическая модель тормозной магистрали d = 32 мм с реальным гидравлическим сопротивлением;
    /// - Воздухораспределители усл. № 483-000 / 483М с раздельными камерами РК (6 л) и ЗК (4,5 л).
    /// </summary>
    public class Pneumatics
    {
        public double ChargeMPa = 0.5;
        public double RegOnMPa = 0.735;     // Включение компрессоров 3РД: 7,5 кгс/см²
        public double RegOffMPa = 0.883;    // Отключение компрессоров 3РД: 9,0 кгс/см²
        public double CompressorKgS = 0.114; // Производительность КТ-7 одной секции, кг/с (~5,3 м³/мин)

        public double PipeD = 0.032;        // Внутренний диаметр магистральной трубы 1¼ дюйма (32 мм)
        // Retain the verified 0.7.2 damping until the new pipe resistance has an independent calibration.
        // The 0.40–0.42 kgf/cm2 screenshot was a charging transient, not the steady pressure drop.
        public double PipeF = 0.026;
        public double HoseLoss = 4.0;
        public double PipeLinKgS = 0.008;

        public double FeedI = 7e-05;        // Прямой канал золотника КМ № 395 в положении I
        public double FeedRelay = 6e-05;    // Питательный клапан реле-повторителя КМ № 395
        public double StabilizerMPaS = 0.0002; // Темп ликвидации сверхзарядки стабилизатором КМ № 395 (0,2 кгс/см² за 100 с)

        public double DensitySens = 0.013;  // Порог чувствительности ВР № 483 (перепад РК - ЗК ≈ 0,13 кгс/см²)
        public double WkChargeTau = 45.0;   // Постоянная времени зарядки РК из ЗК через отверстие 0,65 мм
        public double WkSoftTau = 60.0;     // Restricted chamber path; the open softness valve is handled separately below.
        // The open softness valve bypasses the restricted chamber charging path during a slow pipe fall.
        // These are explicit model calibration parameters; they are verified by the softness bench.
        public double SoftnessRateMPaS = 0.10 / 60.0, SoftnessRateToleranceMPaS = 0.0002, SoftnessValveTau = 2.0;
        public double WkOrifice = 3e-07;    // Калиброванное отверстие зарядки на равнинном режиме
        public double WkOrificeMountain = 2.2e-07; // Калиброванное отверстие на горном режиме
        public double EmergencyRate = 0.07; // Model calibration: separates service and emergency waves for 40/68/100 wagons; not a factory specification.

        public int Sections = 2;
        public AirNode Mr;
        public AirNode Ur;
        public AirNode TmLoco;
        public AirNode WkLoco;
        public AirNode[] TcLoco;
        public AirNode Pr;

        public double LocoTcLeak = 1.3e-07;
        public bool LocoTcCut;
        public int Crane = 2;
        public int Kvt = 2;
        public bool EpkTripped;
        public bool CompressorOn;
        public bool LocoAutoReleased;
        public bool ExternalCompressorControl;
        public bool ReleaseButton;
        public double LocoAutoTc;
        public double LocoTcMax = 0.4;
        public double[] RpmShare = new double[2] { 0.35, 0.35 };
        public bool[] CompressorWorks = new bool[2] { true, true };
        public double CompressorFactor = 1.0;

        private static readonly double[] KvtMPa = new double[7]
        {
            0.0, 0.0, 0.0, 0.11, 0.20, 0.29, 0.38
        };

        private bool locoBraking;
        private bool locoRel;
        private double locoWkMem;
        private double locoTrigT;
        private double locoTmLevel = 9.0;
        private double locoRelT;

        public WagonAir[] W = new WagonAir[0];
        public int EndValveClosedAfter = -1;
        public int BreakAt = -1;
        private double[] linkFlow = new double[0];
        private double[] linkLen = new double[0];
        public double LeakFactor = 1.0;

        public double AirIn;
        public double AirOut;
        public double StartMass;
        public double T;

        public bool Lamp418;
        public double LampFlowRatio = 1.4;
        public double LeakRefKgS;
        public double LampFeedKgS;
        private double feedKg;
        private double lampPrevP = -1.0;
        private double lampRate;
        private double lampOnT;
        private double chargeT;
        private double lowT;
        private int lampCranePrev = 2;
        private bool chargeBlock;
        public bool EmergencyVent;

        public double PipeArea { get { return 0.7853981633974483 * PipeD * PipeD; } }

        public bool LocoVrBraking { get { return locoBraking; } }

        public double Tc254MPa
        {
            get
            {
                if (Kvt < 3 || Kvt > 6) return 0.0;
                return Math.Min(KvtMPa[Kvt], LocoTcMPa);
            }
        }

        public double TailTmMPa
        {
            get
            {
                if (W.Length <= 0) return TmLoco.MPa;
                return W[W.Length - 1].Tm.MPa;
            }
        }

        public double TailTcMPa
        {
            get
            {
                if (W.Length <= 0) return 0.0;
                return W[W.Length - 1].Tc.MPa;
            }
        }

        public double LocoTcMPa
        {
            get
            {
                double maxP = 0.0;
                for (int i = 0; i < TcLoco.Length; i++)
                {
                    maxP = Math.Max(maxP, TcLoco[i].MPa);
                }
                return maxP;
            }
        }

        public void Build(Train cfg, double chargeFraction)
        {
            ChargeMPa = cfg.ChargeMPa;
            Sections = cfg.Sections;

            double initMainMPa = Math.Max(chargeFraction, RegOffMPa * ((chargeFraction > 0.0) ? 1.0 : 0.0));
            Mr = new AirNode(1.0 * Sections, initMainMPa);
            Pr = new AirNode(0.25 * Sections, Mr.MPa);

            double initPipeMPa = (chargeFraction > 0.0) ? ChargeMPa : 0.0;
            Ur = new AirNode(0.02, initPipeMPa);

            double locoPipeVol = PipeArea * cfg.SectionLengthM * Sections + 0.012;
            TmLoco = new AirNode(locoPipeVol, initPipeMPa);
            WkLoco = new AirNode(0.0105, initPipeMPa);
            locoWkMem = WkLoco.MPa;

            TcLoco = new AirNode[Sections];
            for (int i = 0; i < Sections; i++)
            {
                TcLoco[i] = new AirNode(0.06, 0.0);
            }

            W = new WagonAir[cfg.Wagons.Count];
            for (int j = 0; j < W.Length; j++)
            {
                Wagon spec = cfg.Wagons[j];
                double p0 = (chargeFraction > 0.0) ? ChargeMPa : 0.0;
                double vTm = PipeArea * (spec.Length + 0.6) + 0.0015;

                WagonAir wa = new WagonAir();
                wa.Tm = new AirNode(vTm, p0);
                wa.Zr = new AirNode(0.078, p0);

                // Двухкамерный резервуар № 295-001: рабочая камера РК = 6,0 л и золотниковая ЗК = 4,5 л
                wa.Rk = new AirNode(0.0060, p0);
                wa.Zk = new AirNode(0.0045, p0);
                wa.Wk = new AirNode(0.0105, p0);
                wa.SyncWkFromChambers();

                wa.Tc = new AirNode(0.01, 0.0);
                wa.VrOn = spec.VrOn;
                wa.Mode = spec.Mode;
                wa.Mountain = spec.Mountain;
                wa.TcMax = spec.ModeCylinderMPa;
                wa.ReleaseArea = (spec.Mode == 3) ? 2.2e-06 : ((spec.Mode == 2) ? 3e-06 : 4e-06);
                wa.TmPrev = p0;
                wa.PrevStageDropMPa = 0.0;
                wa.TailKdrCooldownUntil = 0.0;

                W[j] = wa;
            }

            linkFlow = new double[W.Length];
            linkLen = new double[W.Length];
            for (int k = 0; k < W.Length; k++)
            {
                linkLen[k] = (k == 0)
                    ? cfg.SectionLengthM
                    : ((cfg.Wagons[k - 1].Length + cfg.Wagons[k].Length) / 2.0);
            }

            AirIn = (AirOut = 0.0);
            StartMass = TotalMass();
            T = 0.0;

            LeakRefKgS = 0.0;
            for (int m = 0; m < W.Length; m++)
            {
                LeakRefKgS += Air.Orifice(Air.Patm + ChargeMPa * 1000000.0, Air.Patm, W[m].LeakTm);
            }

            Lamp418 = false;
            feedKg = 0.0;
            lampPrevP = -1.0;
            lampRate = 0.0;
            LampFeedKgS = LeakRefKgS;
            chargeBlock = false;
            lampCranePrev = Crane;
        }

        public double TotalMass()
        {
            double m = Mr.M + Pr.M + Ur.M + TmLoco.M + WkLoco.M;
            for (int i = 0; i < TcLoco.Length; i++)
            {
                m += TcLoco[i].M;
            }
            for (int j = 0; j < W.Length; j++)
            {
                WagonAir wa = W[j];
                wa.SyncWkFromChambers();
                m += wa.Tm.M + wa.Zr.M + wa.Wk.M + wa.Tc.M;
            }
            return m;
        }

        public double Balance()
        {
            return TotalMass() - (StartMass + AirIn - AirOut);
        }

        private void Vent(AirNode node, double area, double dt)
        {
            if (area <= 0.0) return;
            double dm = Air.Orifice(node.Pa, Air.Patm, area) * dt;
            if (dm > 0.0)
            {
                double maxDm = node.M - Air.Patm * node.Cap;
                dm = Math.Min(dm, maxDm);
                if (dm > 0.0)
                {
                    node.M -= dm;
                    AirOut += dm;
                }
            }
        }

        private double Flow(AirNode a, AirNode b, double area, double dt, bool oneWay = false)
        {
            double dm = Air.Orifice(a.Pa, b.Pa, area) * dt;
            if (oneWay && dm < 0.0)
            {
                return 0.0;
            }

            // Ограничение перетекания моментом выравнивания эффективных давлений a и b
            double kTa = Math.Max(0.85, Math.Min(1.15, 1.0 + a.ThermalDelta));
            double kTb = Math.Max(0.85, Math.Min(1.15, 1.0 + b.ThermalDelta));
            double eqDm = (a.M * kTa / a.V - b.M * kTb / b.V) / (kTa / a.V + kTb / b.V);

            if (dm > 0.0)
            {
                dm = Math.Min(dm, Math.Max(0.0, eqDm));
            }
            else
            {
                dm = Math.Max(dm, Math.Min(0.0, eqDm));
            }

            Air.Move(a, b, dm);
            return dm;
        }

        public void Step(double dt)
        {
            T += dt;

            // 1. Регулятор давления 3РД (7,5 - 9,0 кгс/см²) и компрессоры КТ-7
            double mrMPa = Mr.MPa;
            if (mrMPa <= RegOnMPa)
            {
                CompressorOn = true;
            }
            else if (mrMPa >= RegOffMPa)
            {
                CompressorOn = false;
            }

            if (CompressorOn || ExternalCompressorControl)
            {
                for (int i = 0; i < Sections && i < CompressorWorks.Length; i++)
                {
                    if (CompressorWorks[i])
                    {
                        double share = (i < RpmShare.Length) ? RpmShare[i] : 0.0;
                        // На 2ТЭ116 привод компрессора КТ-7 электрический (электродвигатель П2К):
                        // при работающем дизеле (share > 0.1) стабилизируется номинальная подача
                        double effShare = (share > 0.15) ? Math.Max(share, 0.85) : share;
                        double dmComp = CompressorKgS * CompressorFactor * effShare * dt;
                        Mr.M += dmComp;
                        AirIn += dmComp;
                    }
                }
            }

            // Утечка ГР и обратный клапан питания ПР от ГР
            Vent(Mr, 4e-08, dt);
            Flow(Mr, Pr, 5e-05, dt, oneWay: true);

            // 2. Кран машиниста усл. № 395М и термодинамика УР (20 л)
            double urMPaBefore = Ur.MPa;
            double urMassBefore = Ur.M;

            switch (Crane)
            {
                case 1:
                    // Положение I (зарядка и отпуск): зарядка УР из ГР через отв. 1,6 мм +
                    // прямой канал 22 мм в ТМ + параллельная работа реле-повторителя
                    Flow(Mr, Ur, 2e-06, dt, oneWay: true);
                    feedKg += Flow(Mr, TmLoco, FeedI, dt, oneWay: true);
                    Relay(dt);
                    break;

                case 2:
                    // Положение II (поездное): работа редуктора и стабилизатора УР
                    if (urMPaBefore < ChargeMPa)
                    {
                        double redOpen = Clamp((ChargeMPa - urMPaBefore) / 0.01);
                        Flow(Mr, Ur, 3e-06 * redOpen, dt, oneWay: true);
                    }
                    else if (urMPaBefore > ChargeMPa + 0.0002)
                    {
                        double dmStab = Math.Min(StabilizerMPaS * dt, urMPaBefore - ChargeMPa) * 1000000.0 * Ur.Cap;
                        Ur.M -= dmStab;
                        AirOut += dmStab;
                    }
                    Relay(dt);
                    break;

                case 3:
                    // Положение III (перекрыша без питания ТМ): обратный клапан сообщает УР -> ТМ
                    Flow(Ur, TmLoco, 4e-06, dt, oneWay: true);
                    break;

                case 4:
                    // Положение IV (перекрыша с питанием ТМ)
                    Relay(dt);
                    break;

                case 5:
                    // Положение V (служебное торможение): разрядка УР через отв. 1,6 мм (4-5 с на 0,1 МПа)
                    Vent(Ur, 4.6e-06, dt);
                    Relay(dt);
                    break;

                case 7:
                    // Положение Vа (торможение длинносоставных поездов): разрядка УР (15-20 с на 0,1 МПа)
                    Vent(Ur, 1.2e-06, dt);
                    Relay(dt);
                    break;

                case 6:
                    // Положение VI (экстренное торможение)
                    Vent(Ur, 2e-05, dt);
                    Vent(TmLoco, 0.0003, dt);
                    break;
            }

            // Политропный эффект в УР (20 л): при быстрой разрядке в V/Vа воздух охлаждается,
            // а после перевода в IV (перекрыша) за ~6 с прогревается от стенок (давая прирост ~0,01 МПа)
            if (Ur.M > 1e-5)
            {
                double dRelM = (Ur.M - urMassBefore) / Ur.M;
                // При расширении (dRelM < 0) показатель политропы n ≈ 1.22 -> dT/T ≈ 0.22 * dM/M
                Ur.ThermalDelta += 0.22 * dRelM;
                // Теплообмен со стенками резервуара (постоянная релаксации τ_wall = 6.0 с)
                Ur.ThermalDelta -= Ur.ThermalDelta * Math.Min(1.0, dt / 6.0);
                Ur.ThermalDelta = Math.Max(-0.04, Math.Min(0.04, Ur.ThermalDelta));
            }

            // Разрядка ТМ через срывной клапан ЭПК-150 или стоп-кран
            if (EpkTripped || EmergencyVent)
            {
                Vent(TmLoco, 0.00049, dt);
            }

            // 3. Датчик целостности тормозной магистрали усл. № 418
            Sensor418(dt);

            // 4. Газодинамика тормозной магистрали по длине поезда
            double area = PipeArea;
            int nW = W.Length;
            double rhoCharge = (Air.Patm + ChargeMPa * 1000000.0) / Air.RT;

            for (int k = 0; k < nW; k++)
            {
                if (k - 1 == EndValveClosedAfter && k > 0)
                {
                    linkFlow[k] = 0.0;
                    continue;
                }

                AirNode nodeA = (k == 0) ? TmLoco : W[k - 1].Tm;
                AirNode nodeB = W[k].Tm;
                double len = linkLen[k];
                double lossK = PipeF * len / PipeD + HoseLoss;
                double rhoAvg = 0.5 * (nodeA.M / nodeA.V + nodeB.M / nodeB.V);
                if (rhoAvg < 0.1) rhoAvg = 0.1;

                double flow = linkFlow[k];
                double accel = (area / len) * (nodeA.Pa - nodeB.Pa);
                double drag = (area / len) * lossK * (Math.Abs(flow) / rhoAvg + PipeLinKgS / rhoCharge) / (2.0 * area * area);

                flow = (flow + dt * accel) / (1.0 + dt * drag);
                linkFlow[k] = flow;
                Air.Move(nodeA, nodeB, flow * dt);
            }

            // Разрыв тормозной магистрали в составе
            if (BreakAt >= 0 && BreakAt < nW)
            {
                Vent(W[BreakAt].Tm, area, dt);
                if (BreakAt + 1 < nW)
                {
                    Vent(W[BreakAt + 1].Tm, area, dt);
                }
            }

            // 5. Воздухораспределители вагонов усл. № 483-000 / 483М
            for (int m = 0; m < nW; m++)
            {
                Distributor(W[m], m, nW, dt);
            }

            // 6. Автотормоз локомотива (ВР усл. № 292-001) и кран вспомогательного тормоза усл. № 254 (реле № 304)
            LocoAuto(dt);

            double kvtTarget = (Kvt >= 3 && Kvt <= 6) ? KvtMPa[Kvt] : 0.0;
            double targetTc = Math.Max(kvtTarget, LocoAutoReleased ? 0.0 : LocoAutoTc);
            bool autoDominates = !LocoAutoReleased && (LocoAutoTc > kvtTarget);

            for (int s = 0; s < TcLoco.Length; s++)
            {
                AirNode tcNode = TcLoco[s];
                Vent(tcNode, LocoTcLeak, dt);
                double tcP = tcNode.MPa;

                if (LocoTcCut) continue;

                if (tcP < targetTc - 0.003 && autoDominates)
                {
                    double dmAuto = Math.Min(targetTc - tcP, 0.05 * dt) * 1000000.0 * tcNode.Cap;
                    double eqMax = (Pr.M / Pr.V - tcNode.M / tcNode.V) / (1.0 / Pr.V + 1.0 / tcNode.V);
                    Air.Move(Pr, tcNode, Math.Max(0.0, Math.Min(dmAuto, eqMax)));
                }
                else if (tcP < targetTc - 0.003)
                {
                    Flow(Pr, tcNode, 6e-05 * Clamp((targetTc - tcP) / 0.03), dt, oneWay: true);
                }
                else if (tcP > targetTc + 0.003)
                {
                    Vent(tcNode, 5e-05 * Clamp((tcP - targetTc) / 0.03), dt);
                }
            }
        }

        private void Relay(double dt)
        {
            double diff = Ur.MPa - TmLoco.MPa;
            if (diff > 0.002)
            {
                feedKg += Flow(Mr, TmLoco, FeedRelay * Clamp(diff / 0.015), dt, oneWay: true);
            }
            else if (diff < -0.002)
            {
                Vent(TmLoco, 8e-05 * Clamp((-diff) / 0.01), dt);
            }
        }

        private void Sensor418(double dt)
        {
            double tmP = TmLoco.MPa;
            if (lampPrevP < 0.0)
            {
                lampPrevP = tmP;
            }

            lampRate += ((tmP - lampPrevP) / dt - lampRate) * Math.Min(1.0, dt / 0.5);
            lampPrevP = tmP;

            LampFeedKgS += (feedKg / dt - LampFeedKgS) * Math.Min(1.0, dt / 0.5);
            feedKg = 0.0;

            double thresholdKgS = LampFlowRatio * Math.Max(LeakRefKgS, 1e-07);
            bool inReleaseOrRun = (Crane == 1 || Crane == 2);

            if (Crane == 1 || (inReleaseOrRun && lampCranePrev != 1 && lampCranePrev != 2))
            {
                chargeBlock = true;
                chargeT = 0.0;
                lowT = 0.0;
            }

            if (chargeBlock)
            {
                chargeT += dt;
                lowT = (LampFeedKgS >= 0.8 * thresholdKgS) ? 0.0 : (lowT + dt);
            }

            if (!inReleaseOrRun || (chargeT > 5.0 && lowT > 10.0))
            {
                chargeBlock = false;
            }

            if (chargeBlock && Crane == 2 && (EpkTripped || EmergencyVent || Ur.MPa - tmP > 0.05))
            {
                chargeBlock = false;
            }

            lampCranePrev = Crane;

            if (LampFeedKgS > thresholdKgS && lampRate < 0.0005 && !chargeBlock)
            {
                Lamp418 = true;
                lampOnT = 0.0;
            }
            else if (Lamp418)
            {
                lampOnT += dt;
                if (LampFeedKgS < 0.8 * thresholdKgS && lampOnT >= 1.0)
                {
                    Lamp418 = false;
                }
            }
        }

        private void LocoAuto(double dt)
        {
            double tmP = TmLoco.MPa;
            if (!locoBraking)
            {
                if (tmP > WkLoco.MPa)
                {
                    Flow(TmLoco, WkLoco, WkOrifice, dt, oneWay: true);
                }
                else
                {
                    // Мягкость ВР № 292-001 (не срабатывает при темпе разрядки ТМ до 0,08 МПа/мин)
                    double dmSoft = (tmP - WkLoco.MPa) * 1000000.0 * WkLoco.Cap * Math.Min(1.0, dt / 8.0);
                    Air.Move(TmLoco, WkLoco, dmSoft);
                }

                if (WkLoco.MPa - tmP > DensitySens)
                {
                    locoBraking = true;
                    locoWkMem = WkLoco.MPa;
                    LocoAutoReleased = false;
                    locoTrigT = T;
                    locoTmLevel = 9.0;
                    locoRel = false;
                }
                LocoAutoTc = 0.0;
            }
            else
            {
                LocoAutoTc = LocoTcMax * Clamp((locoWkMem - tmP - 0.005) / 0.135);

                if (T - locoTrigT > 1.5)
                {
                    locoTmLevel = Math.Min(locoTmLevel, tmP);
                    // ВР усл. № 292-001 пассажирского типа имеет бесступенчатый отпуск при повышении ТМ на 0,015-0,02 МПа
                    if (tmP > locoTmLevel + 0.018 && tmP > 0.25)
                    {
                        locoRel = true;
                    }
                }

                if (locoRel && locoWkMem > tmP)
                {
                    locoWkMem = Math.Max(tmP, locoWkMem - Math.Max(0.015, (locoWkMem - tmP) * 0.45) * dt);
                }

                bool fullRelease = (tmP >= locoWkMem - 0.01) || (Crane == 1 && tmP > locoWkMem - 0.03) || locoRel;
                locoRelT = fullRelease ? (locoRelT + dt) : 0.0;

                if (fullRelease)
                {
                    LocoAutoTc = 0.0;
                    // На реальном 2ТЭ116 с ВР № 292-001 отпуск автотормоза происходит сразу по мере выпуска воздуха через реле № 304
                    // Preserve the baseline status latch until the cylinders release; the fallback timeout
                    // does not control their exhaust. A shorter 2.5 s status timeout has no verified calibration.
                    if (LocoTcMPa < 0.03 || (Kvt >= 3 && Kvt <= 6) || LocoAutoReleased || LocoTcCut || locoRelT > 20.0)
                    {
                        locoBraking = false;
                        locoRel = false;
                        LocoAutoTc = 0.0;
                        if (WkLoco.MPa > tmP)
                        {
                            Air.Move(WkLoco, TmLoco, (WkLoco.MPa - tmP) * 1000000.0 * WkLoco.Cap);
                        }
                    }
                }
            }

            if (Kvt == 1 || ReleaseButton)
            {
                LocoAutoReleased = true;
            }
        }

        private void Distributor(WagonAir w, double dt)
        {
            Distributor(w, 0, Math.Max(1, W.Length), dt);
        }

        /// <summary>
        /// Физическая модель воздухораспределителя усл. № 483-000 / 483М по РЭ:
        /// - Магистральная часть № 483-010 (диафрагма между МК и ЗК, плунжер, клапан мягкости, манжета КДР);
        /// - Главная часть № 270-023 (главный поршень под разностью давлений РК и ЗК, уравнительный поршень ТЦ);
        /// - Двухкамерный резервуар № 295-001 (РК = 6,0 л и ЗК = 4,5 л).
        /// </summary>
        private void Distributor(WagonAir w, int wagonIndex, int totalWagons, double dt)
        {
            if (w.Rk == null) w.Rk = new AirNode(0.0060, w.Wk.MPa);
            if (w.Zk == null) w.Zk = new AirNode(0.0045, w.Wk.MPa);

            double tmP = w.Tm.MPa;
            double zrP = w.Zr.MPa;
            double rkP = w.Rk.MPa;
            double zkP = w.Zk.MPa;
            double tcP = w.Tc.MPa;

            double instRate = (tmP - w.TmPrev) / dt;
            w.TmPrev = tmP;
            w.TmRate += (instRate - w.TmRate) * Math.Min(1.0, dt / 0.2);

            // Естественные утечки из ТМ и ЗР вагона
            Vent(w.Tm, w.LeakTm * LeakFactor, dt);
            Vent(w.Zr, w.LeakZr * LeakFactor, dt);

            if (!w.VrOn)
            {
                Vent(w.Tc, 4e-06, dt);
                w.SyncWkFromChambers();
                return;
            }

            if (!w.Braking)
            {
                // The relay's deadband produces small rate pulses at the nominal softness boundary.
                bool softnessOpen = w.TmRate >= -SoftnessRateMPaS - SoftnessRateToleranceMPaS;
                // 1. Зарядка золотниковой камеры (ЗК = 4,5 л) из ТМ и рабочей камеры (РК = 6,0 л) из ЗК
                double chargeOrifice = w.Mountain ? WkOrificeMountain : WkOrifice;
                if (tmP > zkP)
                {
                    Flow(w.Tm, w.Zk, chargeOrifice * 1.35, dt, oneWay: true);
                }
                else
                {
                    // Медленная разрядка ЗК в ТМ через каналы плунжера при темпе мягкости
                    double zkSoftTau = softnessOpen ? SoftnessValveTau : 8.0;
                    double dmZkSoft = (tmP - zkP) * 1000000.0 * w.Zk.Cap * Math.Min(1.0, dt / zkSoftTau);
                    Air.Move(w.Tm, w.Zk, dmZkSoft);
                }

                zkP = w.Zk.MPa;
                if (zkP > rkP)
                {
                    // Зарядка РК (6,0 л) из ЗК через дроссельное отверстие d = 0,65 мм
                    Flow(w.Zk, w.Rk, chargeOrifice * 0.85, dt, oneWay: true);
                }
                else if (rkP > zkP)
                {
                    // Свойство мягкости ВР № 483: перетекание из РК в ЗК через отверстие 0,65 мм (τ ≈ 60 с)
                    // и клапан мягкости при малом перепаде МК - ЗК (защита от срабатывания при темпе до 0,08-0,10 МПа/мин).
                    // При перезарядке РК в I положении и последующем сбросе ТМ редуктором/стабилизатором
                    // перепад rkP - zkP превышает DensitySens (0,013 МПа) и вызывает реальную затяжку тормозов!
                    double softTau = softnessOpen ? SoftnessValveTau : ((zkP - tmP < 0.008) ? 14.0 : WkSoftTau);
                    double dmRkSoft = (zkP - rkP) * 1000000.0 * w.Rk.Cap * Math.Min(1.0, dt / softTau);
                    Air.Move(w.Zk, w.Rk, dmRkSoft);
                }

                // Зарядка запасного резервуара ЗР (78 л) из ТМ через обратный клапан
                if (tmP > zrP)
                {
                    Flow(w.Tm, w.Zr, 1.1e-06, dt, oneWay: true);
                }

                // Выпуск воздуха из ТЦ в атмосферу в отпускном положении
                if (tcP > 0.0005)
                {
                    Vent(w.Tc, w.ReleaseArea, dt);
                }

                rkP = w.Rk.MPa;
                zkP = w.Zk.MPa;

                // Срабатывание на торможение: когда диафрагма смещается вправо (tmP < rkP) и перепад (РК - ТМ) превышает порог чувствительности
                if (rkP - tmP > DensitySens && tmP < rkP)
                {
                    w.Braking = true;
                    w.Quick = true;
                    w.QuickFrom = tmP;
                    w.QuickUntil = T + 1.5;
                    w.TrigT = T;
                    w.TmLevel = 9.0;
                    w.PrevStageDropMPa = rkP - tmP;
                    w.TailKdrCooldownUntil = T + 1.8;
                }
            }

            if (w.Braking)
            {
                // Во время торможения РК (6,0 л) отсечена плунжером и хранит зарядное давление rkP,
                // а ЗК (4,5 л) разряжается в КДР/атмосферу следом за ТМ:
                if (w.Zk.MPa > tmP + 0.003)
                {
                    double dmZkFollow = (tmP - w.Zk.MPa) * 1000000.0 * w.Zk.Cap * Math.Min(1.0, dt / 0.35);
                    Air.Move(w.Tm, w.Zk, dmZkFollow);
                }
                else if (tmP > w.Zk.MPa)
                {
                    Flow(w.Tm, w.Zk, w.Mountain ? WkOrificeMountain : WkOrifice, dt, oneWay: true);
                }

                rkP = w.Rk.MPa;
                zkP = w.Zk.MPa;
                double currentDrop = rkP - tmP;

                // The first stage is still developing while its additional discharge is active.
                // Remember its peak, rather than treating each further part of the same reduction as a new stage.
                if (w.Quick || T - w.TrigT < 3.0)
                    w.PrevStageDropMPa = Math.Max(w.PrevStageDropMPa, currentDrop);

                // 2. Повторная дополнительная разрядка ТМ (КДР) на 2-й и последующих ступенях торможения
                // и периодический подхват клапана 26 в хвостовой трети длинносоставного поезда (по РЭ ВР № 483-000):
                if (!w.Quick && currentDrop > w.PrevStageDropMPa + 0.018 && w.TmRate < -0.003)
                {
                    w.Quick = true;
                    w.QuickFrom = tmP;
                    w.QuickUntil = T + 1.2;
                    w.PrevStageDropMPa = currentDrop;
                }
                else if (!w.Quick && totalWagons >= 40 && wagonIndex >= (totalWagons / 2)
                         && T > w.TailKdrCooldownUntil && w.TmRate < -0.0012 && currentDrop > w.PrevStageDropMPa + 0.008)
                {
                    w.Quick = true;
                    w.QuickFrom = tmP;
                    w.QuickUntil = T + 0.8;
                    w.TailKdrCooldownUntil = T + 2.2;
                    w.PrevStageDropMPa = currentDrop;
                }

                if (w.Quick)
                {
                    // При дополнительной разрядке воздух из ТМ сбрасывается через КДР в ТЦ (при малом давлении в ТЦ) и атмосферу
                    if (tcP < 0.08 && tmP > tcP + 0.05)
                    {
                        Flow(w.Tm, w.Tc, 1.5e-06, dt, oneWay: true);
                        Vent(w.Tm, 1.0e-06, dt);
                    }
                    else
                    {
                        Vent(w.Tm, 2.5e-06, dt);
                    }

                    if (T > w.QuickUntil || tmP < w.QuickFrom - 0.03)
                    {
                        w.Quick = false;
                    }
                }

                // Экстренная разрядка при крутом темпе падения ТМ
                if (!w.Emergency && w.TmRate < -EmergencyRate)
                {
                    w.Emergency = true;
                    w.EmergencyUntil = T + 4.0;
                }

                if (w.Emergency && T < w.EmergencyUntil && tmP > 0.05)
                {
                    Vent(w.Tm, 2.5e-05, dt);
                }

                // 3. Наполнение тормозного цилиндра (ТЦ) главной частью № 270-023 под действием перепада (РК - ЗК)
                double effectiveDrop = Math.Max(rkP - zkP, rkP - tmP);
                double targetTc = w.Emergency
                    ? w.TcMax
                    : (w.TcMax * Clamp((effectiveDrop - 0.005) / 0.135));

                tcP = w.Tc.MPa;
                zrP = w.Zr.MPa;

                if (tcP < targetTc - 0.002 && zrP > tcP + 0.001)
                {
                    // Трёхступенчатая характеристика наполнения ТЦ по РЭ ВР № 483-000:
                    // I фаза («скачок» до 0,05 МПа): быстрое наполнение до прижатия колодок к колёсам;
                    // II фаза (0,05..0,15 МПа): свободный проход через дроссели втулки;
                    // III фаза (> 0,15 МПа на гружёном режиме Г): тарированное дросселирование (16-22 с до 0,35 МПа).
                    double fillRate = (tcP < 0.05)
                        ? 0.06
                        : ((w.Mode == 3 && tcP >= 0.15) ? 0.0136 : 0.04);

                    // Учёт увеличенного расхода воздуха из ЗР на фазе выбора выхода штока ТЦ (до касания колодок при 0,045 МПа)
                    double pistonSweepFactor = (tcP < 0.045) ? 1.25 : 1.0;
                    double dmFill = Math.Min(targetTc - tcP, fillRate * dt) * 1000000.0 * w.Tc.Cap * pistonSweepFactor;
                    double eqMax = (w.Zr.M / w.Zr.V - w.Tc.M / w.Tc.V) / (1.0 / w.Zr.V + 1.0 / w.Tc.V);
                    Air.Move(w.Zr, w.Tc, Math.Max(0.0, Math.Min(dmFill, eqMax)));
                }
                else if (w.Mountain && tcP > targetTc + 0.01)
                {
                    // На горном режиме возможен ступенчатый отпуск ТЦ пропорционально снижению перепада (РК - ЗК)
                    Vent(w.Tc, w.ReleaseArea, dt);
                }

                // 4. Работа магистральной диафрагмы и плунжера при отпуске (равнинный и горный режимы)
                if (T - w.TrigT > 3.0)
                {
                    w.TmLevel = Math.Min(w.TmLevel, tmP);
                }

                bool doRelease = false;
                if (w.Stuck)
                {
                    doRelease = (tmP > rkP + 0.03);
                }
                else if (w.Mountain)
                {
                    // На горном режиме РК и ЗК разобщены, отпуск завершается при восстановлении давления ТМ до уровня РК
                    doRelease = (tmP >= rkP - 0.01);
                }
                else
                {
                    // На равнинном режиме:
                    // - В головной части поезда при крутом подъёме давления в ТМ диафрагма прогибается влево до упора,
                    //   перекрывая сообщение РК с ЗК (замедленный отпуск головных вагонов);
                    // - В средней и хвостовой части поезда, когда давление в ТМ поднимается на 0,018-0,020 МПа над
                    //   минимальным уровнем разрядки TmLevel, диафрагма встаёт в среднее положение и сообщает РК (6 л)
                    //   с ЗК (4,5 л), сбрасывая воздух из РК в ЗК и вызывая быстрый бесступенчатый отпуск!
                    if (tmP >= rkP - 0.01)
                    {
                        doRelease = true;
                    }
                    else if (T - w.TrigT > 3.0 && tmP > w.TmLevel + 0.018)
                    {
                        // Перетекание из рабочей камеры РК (6 л) в золотниковую ЗК (4,5 л) через каналы плунжера
                        double headRetard = (wagonIndex < 12 && tmP - w.TmLevel > 0.08) ? 0.35 : 1.0;
                        double eqRkZk = (w.Rk.M / w.Rk.V - w.Zk.M / w.Zk.V) / (1.0 / w.Rk.V + 1.0 / w.Zk.V);
                        if (eqRkZk > 0.0)
                        {
                            double dmEqualize = eqRkZk * Math.Min(1.0, dt * 1.8 * headRetard);
                            Air.Move(w.Rk, w.Zk, dmEqualize);
                        }
                        if (w.Rk.MPa - w.Zk.MPa <= DensitySens * 0.85 || tmP >= w.Rk.MPa - 0.012)
                        {
                            doRelease = true;
                        }
                    }
                }

                if (w.Emergency && tmP < 0.25)
                {
                    doRelease = false;
                }

                if (doRelease)
                {
                    w.Braking = false;
                    w.Quick = false;
                    w.Emergency = false;
                    w.TmLevel = 9.0;
                    w.PrevStageDropMPa = 0.0;

                    if (!w.Mountain && !w.Stuck)
                    {
                        // Выравнивание давлений РК и ЗК при полном переходе на равнинный отпуск
                        double eqRkZk = (w.Rk.M / w.Rk.V - w.Zk.M / w.Zk.V) / (1.0 / w.Rk.V + 1.0 / w.Zk.V);
                        if (Math.Abs(eqRkZk) > 1e-9)
                        {
                            Air.Move(w.Rk, w.Zk, eqRkZk);
                        }
                        // Разрядка избытка давления двухкамерного резервуара в ТМ, если ТМ ещё ниже РК
                        if (w.Rk.MPa > tmP)
                        {
                            double dmRkToTm = (w.Rk.MPa - tmP) * 1000000.0 * w.Rk.Cap;
                            Air.Move(w.Rk, w.Tm, dmRkToTm);
                        }
                        if (w.Zk.MPa > tmP)
                        {
                            double dmZkToTm = (w.Zk.MPa - tmP) * 1000000.0 * w.Zk.Cap;
                            Air.Move(w.Zk, w.Tm, dmZkToTm);
                        }
                    }
                }
            }

            w.SyncWkFromChambers();
        }

        private static double Clamp(double x)
        {
            if (x < 0.0) return 0.0;
            if (x > 1.0) return 1.0;
            return x;
        }
    }
}
