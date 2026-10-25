using System;

/// <summary>
/// Современная научно обоснованная математическая модель тяги электровоза 2ЭС6 («Синара»).
/// Построена на основе «Правил тяговых расчетов для поездной работы» (ПТР),
/// Руководства по эксплуатации 2ЭС6 (Часть 1, Таблица 1) и исследований ВНИИЖТ / LTD.
/// </summary>
public sealed class TrainerModernTractionPhysics {
    // Паспортные константы двухсекционного грузового электровоза 2ЭС6
    public const double LocomotiveMassTonnes = 200.0;     // 2 секции по 100 т (25 тс/ось)
    public const int AxleCount = 8;                       // 8 колесных пар, 8 ТЭД 1ДТ814
    public const double GearRatio = 3.44;                 // Передаточное число редуктора (86/25)
    public const double NominalWheelDiameterM = 1.250;    // Номинальный диаметр нового бандажа (м)
    public const double HalfWornWheelDiameterM = 1.210;   // Диаметр полуизношенного бандажа для паспортных испытаний (м)
    public const double GearEfficiency = 0.98;            // КПД зубчатой передачи
    public const double NominalLineVoltage = 3000.0;      // Номинальное напряжение контактной сети (В)
    public const double MaxArmatureCurrentA = 750.0;      // Предельно допустимый ток якоря ТЭД (А)
    public const double RatedContinuousPowerKW = 6030.0;  // Номинальная длительная мощность на валах ТЭД (кВт, РЭ Таблица 1)
    public const double RatedHourlyPowerKW = 6470.0;      // Номинальная часовая мощность на валах ТЭД (кВт, РЭ Таблица 1)
    public const double RatedStartingForceKN = 660.0;     // Предельная пусковая сила тяги (кН, ~67.3 тс)

    // Параметры тягового двигателя 1ДТ814
    public const double ArmatureResistanceOhm = 0.0659;   // Сопротивление обмотки якоря (Ом)
    public const double MainPoleResistanceOhm = 0.0180;   // Сопротивление главных полюсов (Ом)
    public const double NominalExcitationCurrentA = 440.0;// Номинальный ток возбуждения СТПР (А)

    /// <summary>
    /// Группировки тяговых двигателей электровоза 2ЭС6 по заводскому РЭ (Лист 22, 45, 47):
    /// С: 1..23 (ходовая безреостатная 23),
    /// СП: 24..44 (ходовая безреостатная 44),
    /// П: 45..65 (ходовая безреостатная 65).
    /// </summary>
    public enum TedGrouping {
        Series = 1,          // "С": 8 ТЭД последовательно (поз. 1..23)
        SeriesParallel = 2,  // "СП": 2 ветви по 4 ТЭД (поз. 24..44)
        Parallel = 3         // "П": 4 ветви по 2 ТЭД (поз. 45..65)
    }

    /// <summary>
    /// Состояние поверхности рельсов
    /// </summary>
    public enum RailCondition {
        DryClean = 1,   // Сухие чистые рельсы (номинал)
        WetSlippery = 2,// Мокрые / загрязненные рельсы
        Sanded = 3      // Подача песка под колеса
    }

    /// <summary>
    /// Результаты расчета тяги
    /// </summary>
    public struct TractionResult {
        public double SpeedKmh;
        public double TractiveForceKN;         // Сила тяги на ободе колес (кН)
        public double TractiveForceNative10KN; // Сила в единицах нативного тренажера (1 ед. = 10 кН)
        public double AdhesionLimitKN;         // Предел силы тяги по сцеплению (кН)
        public double ArmatureCurrentA;        // Ток якоря одного ТЭД (А)
        public double ExcitationCurrentA;      // Ток возбуждения ТЭД (А)
        public double TotalLineCurrentA;       // Суммарный ток от контактной сети (А)
        public double BackEmfV;                // Противо-ЭДС якоря одного ТЭД (В)
        public double PowerShaftKW;            // Суммарная мощность на валах двигателей (кВт)
        public double PowerRimKW;              // Полезная касательная мощность тяги на ободе колес (кВт)
        public double PowerElectricKW;         // Потребляемая мощность от контактной сети (кВт)
        public TedGrouping Grouping;           // Группировка ТЭД (С, СП, П)
        public bool IsWheelSlipWarning;        // Предупреждение о приближении к пределу сцепления (боксованию)
        public bool IsCurrentLimited;          // Признак ограничения тока пределом 750 А
    }

    /// <summary>
    /// Расчет коэффициента сцепления колеса с рельсом по формуле ПТР ВНИИЖТ для электровозов с поосным регулированием:
    /// psi_k(v) = 0.28 + 0.14 / (1 + 0.04 * v)
    /// </summary>
    public static double CalculateAdhesionCoefficient(double speedKmh, RailCondition condition = RailCondition.DryClean) {
        if (speedKmh < 0) speedKmh = 0;
        double basePsi = 0.28 + (0.14 / (1.0 + 0.04 * speedKmh));

        switch (condition) {
            case RailCondition.WetSlippery:
                return basePsi * 0.72; // Снижение сцепления на мокрых рельсах на 28%
            case RailCondition.Sanded:
                return Math.Min(0.44, basePsi * 1.15); // Подача песка увеличивает сцепление на 15%
            default:
                return basePsi;
        }
    }

    /// <summary>
    /// Расчет максимальной силы тяги по условию сцепления с рельсами: F_psi = M_лок * g * psi_k
    /// </summary>
    public static double CalculateAdhesionForceLimitKN(double speedKmh, RailCondition condition = RailCondition.DryClean) {
        double psi = CalculateAdhesionCoefficient(speedKmh, condition);
        const double g = 9.80665;
        return LocomotiveMassTonnes * g * psi; // кН
    }

    /// <summary>
    /// Расчет магнитного потока Ф(I_v) ТЭД 1ДТ814 по характеристике намагничивания (ВНИИЖТ)
    /// </summary>
    public static double CalculateMagneticFluxWb(double excitationCurrentA) {
        if (excitationCurrentA <= 0) return 0.0;
        const double phiMax = 0.0985; // Вб
        const double i0 = 175.0;      // А
        return phiMax * (1.0 - Math.Exp(-excitationCurrentA / i0));
    }

    /// <summary>
    /// Определение группировки ТЭД по позиции контроллера (заводской РЭ лист 22)
    /// </summary>
    public static TedGrouping GetGroupingForPosition(int controllerPosition) {
        if (controllerPosition <= 23) return TedGrouping.Series;
        if (controllerPosition <= 44) return TedGrouping.SeriesParallel;
        return TedGrouping.Parallel;
    }

    /// <summary>
    /// Напряжение на одном ТЭД в зависимости от группировки
    /// </summary>
    public static double GetVoltagePerMotor(TedGrouping grouping, double lineVoltageV) {
        switch (grouping) {
            case TedGrouping.Series:
                return lineVoltageV / 8.0; // 8 ТЭД последовательно (375 В при 3000 В)
            case TedGrouping.SeriesParallel:
                return lineVoltageV / 4.0; // 2 ветви по 4 ТЭД (750 В при 3000 В)
            case TedGrouping.Parallel:
                return lineVoltageV / 2.0; // 4 ветви по 2 ТЭД (1500 В при 3000 В)
            default:
                return lineVoltageV / 8.0;
        }
    }

    /// <summary>
    /// Основное удельное сопротивление движению электровоза 2ЭС6 в режиме тяги по ПТР (Н/кН или кгс/т)
    /// w0' = 1.9 + 0.01 * v + 0.0003 * v^2
    /// </summary>
    public static double CalculateLocomotiveResistanceNPerKN(double speedKmh) {
        if (speedKmh < 0) speedKmh = 0;
        return 1.9 + 0.01 * speedKmh + 0.0003 * speedKmh * speedKmh;
    }

    /// <summary>
    /// Основное удельное сопротивление 4-осных грузовых вагонов на роликовых подшипниках по ПТР (q0 = 20 т/ось)
    /// w0'' = 0.7 + (3 + 0.1 * v + 0.0025 * v^2) / q0
    /// </summary>
    public static double CalculateWagonsResistanceNPerKN(double speedKmh, double axleLoadTonnes = 20.0) {
        if (speedKmh < 0) speedKmh = 0;
        return 0.7 + (3.0 + 0.1 * speedKmh + 0.0025 * speedKmh * speedKmh) / axleLoadTonnes;
    }

    /// <summary>
    /// Полный расчет электромеханических и тяговых параметров электровоза 2ЭС6
    /// </summary>
    public static TractionResult CalculateTraction(
        double speedKmh,
        int controllerPosition,
        double excitationCurrentA = NominalExcitationCurrentA,
        double lineVoltageV = NominalLineVoltage,
        double wheelDiameterM = NominalWheelDiameterM,
        RailCondition railCond = RailCondition.DryClean
    ) {
        var res = new TractionResult {
            SpeedKmh = speedKmh,
            ExcitationCurrentA = excitationCurrentA
        };

        if (controllerPosition <= 0 || speedKmh < 0) {
            return res; // Холостой ход / нулевая позиция
        }

        res.Grouping = GetGroupingForPosition(controllerPosition);
        double speedMs = speedKmh / 3.6;

        // 1. Определение доли мощности / силы в зависимости от позиции контроллера
        // С: 1..23 (ходовая 23), СП: 24..44 (ходовая 44), П: 45..65 (ходовая 65)
        double controllerFactor;
        if (res.Grouping == TedGrouping.Series) {
            // Позиции 1..23: сила плавно нарастает от ~180 кН (поз. 1) до ~470 кН (поз. 23)
            controllerFactor = 0.28 + (0.44 * (Math.Min(23, controllerPosition) / 23.0));
        } else if (res.Grouping == TedGrouping.SeriesParallel) {
            // Позиции 24..44: сила в диапазоне 0.72..0.90 от максимальной
            controllerFactor = 0.72 + (0.18 * ((Math.Min(44, controllerPosition) - 23) / 21.0));
        } else {
            // Позиции 45..65: выход на 100% мощности (при поз. >= 54..65 коэффициент 1.0)
            double p = Math.Max(0.0, Math.Min(10.0, controllerPosition - 44));
            controllerFactor = 0.90 + (0.10 * (p / 10.0));
        }

        // Влияние напряжения контактной сети (номинал 3000 В)
        double voltageFactor = Math.Max(0.5, Math.Min(1.35, lineVoltageV / NominalLineVoltage));

        // 2. Расчет базовой силы тяги на скоростной кривой
        double baseForceKN;

        // Скорости паспортных точек: 49.2 км/ч (часовой режим) и 51.0 км/ч (длительный режим)
        if (speedKmh <= 49.2) {
            // Зона пуска и набора скорости: сила плавно переходит от пускового максимума к часовому режиму
            double t = speedKmh / 49.2;
            double fStart = RatedStartingForceKN * controllerFactor * voltageFactor;
            double fAt49 = (RatedHourlyPowerKW * GearEfficiency / (49.2 / 3.6)) * controllerFactor * voltageFactor;
            baseForceKN = fStart - (fStart - fAt49) * Math.Pow(t, 1.4);
        } else if (speedKmh <= 51.0) {
            // Переход от часового режима (464 кН) к длительному (418 кН)
            double t = (speedKmh - 49.2) / (51.0 - 49.2);
            double f49 = (RatedHourlyPowerKW * GearEfficiency / (49.2 / 3.6)) * controllerFactor * voltageFactor;
            double f51 = (RatedContinuousPowerKW * GearEfficiency / (51.0 / 3.6)) * controllerFactor * voltageFactor;
            baseForceKN = f49 - (f49 - f51) * t;
        } else {
            // Зона автоматической характеристики выше 51 км/ч: гипербола постоянной мощности F = P * eta / v
            double availablePowerKW = RatedContinuousPowerKW * controllerFactor * voltageFactor;
            baseForceKN = (availablePowerKW * GearEfficiency) / speedMs;
        }

        // 3. Ограничение предельной пусковой силы тяги
        baseForceKN = Math.Min(baseForceKN, RatedStartingForceKN);

        // 4. Ограничение по сцеплению с рельсами
        double adhesionLimitKN = CalculateAdhesionForceLimitKN(speedKmh, railCond);
        res.AdhesionLimitKN = adhesionLimitKN;

        if (baseForceKN > adhesionLimitKN) {
            res.TractiveForceKN = adhesionLimitKN;
            res.IsWheelSlipWarning = true;
        } else {
            res.TractiveForceKN = baseForceKN;
            res.IsWheelSlipWarning = (baseForceKN >= adhesionLimitKN * 0.95);
        }

        // 5. Расчет электрических параметров
        res.TractiveForceNative10KN = res.TractiveForceKN / 10.0;
        res.PowerRimKW = res.TractiveForceKN * speedMs;
        res.PowerShaftKW = GearEfficiency > 0 ? (res.PowerRimKW / GearEfficiency) : 0;

        // Расчет тока якоря ТЭД по развиваемому моменту
        double wheelRadius = wheelDiameterM / 2.0;
        double totalTorqueNm = (res.TractiveForceKN * 1000.0 * wheelRadius) / (GearRatio * GearEfficiency);
        double torquePerMotor = totalTorqueNm / AxleCount;

        double flux = CalculateMagneticFluxWb(excitationCurrentA);
        const double Cm = 18.2;
        double calculatedIa = flux > 0 ? (torquePerMotor / (Cm * flux)) : 0;

        if (calculatedIa > MaxArmatureCurrentA) {
            res.ArmatureCurrentA = MaxArmatureCurrentA;
            res.IsCurrentLimited = true;
        } else {
            res.ArmatureCurrentA = calculatedIa;
            res.IsCurrentLimited = false;
        }

        // Противо-ЭДС ТЭД: E = U_тэд - Ia * R_цепи
        double motorVoltage = GetVoltagePerMotor(res.Grouping, lineVoltageV);
        double totalR = ArmatureResistanceOhm + MainPoleResistanceOhm;
        res.BackEmfV = Math.Max(0.0, motorVoltage - (res.ArmatureCurrentA * totalR));

        // Суммарный ток от контактной сети
        int parallelBranches = (res.Grouping == TedGrouping.Series) ? 1 :
                              (res.Grouping == TedGrouping.SeriesParallel) ? 2 : 4;
        res.TotalLineCurrentA = res.ArmatureCurrentA * parallelBranches;
        res.PowerElectricKW = (lineVoltageV * res.TotalLineCurrentA) / 1000.0;

        return res;
    }
}
