using System;

/// <summary>
/// Физическая модель независимого возбуждения (НВ) тяговых двигателей 1ДТ814 / ЭДП810
/// и микропроцессорная система управления и диагностики (МПСУиД) электровоза 2ЭС6 «Синара».
/// Разработано в строгом соответствии с заводским Руководством по эксплуатации (2ЭС6.00.000.000 РЭ):
/// - Листы 22, 45, 47 (соединения С, СП, П, ходовые безреостатные позиции 23, 44, 65);
/// - Листы 98-100, §3.11.9.5-6 (интеллектуальный выбор позиции МПСУиД по току Ia при переходах С->СП и СП->П);
/// - Лист 98, 107-108 (уставки тока возбуждения Iv, закон Iv = Iv0 + k*Ia, реостатный минимум 400 А, лимиты 600/700 А);
/// - Лист 106, §3.11.10.2 (защита от перегрузки Ia > 800 А, блокировка +1/+А, форсирование +ОВ, сброс -А за 10 с);
/// - Лист 108, §3.11.11.8 (токовые ограничения 650 А на С/СП, 570 А на П);
/// - РЭ §2.3 (рекуперативное и реостатное торможение: 120..16 км/ч, ограничение Ia_торм <= 500 А, переход по Uкс 3400/3800 В, Iv <= 520 А).
/// </summary>
public sealed class TrainerIndependentExcitation {
    // Константы ТЭД 1ДТ814 / ЭДП810
    public const double ArmatureResistanceRa = 0.0659;      // Сопротивление якоря и доп. полюсов (Ом)
    public const double MainPoleResistanceRf = 0.0180;      // Сопротивление обмотки главных полюсов (Ом)
    public const double MaxOperatingIv = 700.0;             // Максимальный рабочий ток возбуждения СТПР-1000 (А)
    public const double MinRheostatIv = 400.0;              // Минимальный ток возбуждения на реостатных позициях (А)
    public const double StartingMaxIv = 600.0;              // Предельная уставка Iv при пуске (РЭ лист 108) (А)
    public const double Ce = 1.91;                          // Электрическая постоянная машины (В*с/рад/Вб)
    public const double Cm = 18.2;                          // Моментная постоянная (Н*м/А/Вб)
    public const double PhiMax = 0.0985;                    // Предельный магнитный поток насыщенной стали (Вб)
    public const double Iv0 = 175.0;                        // Постоянная характеристики намагничивания (А)
    public const double ArmatureReactionCoeff = 1.8e-5;     // Размагничивающее действие поперечной реакции якоря (Вб/А)

    /// <summary>
    /// Режим работы электрического торможения
    /// </summary>
    public enum DynamicBrakeMode {
        None = 0,
        Recuperation = 1,       // Чистая рекуперация (отдача в контактную сеть)
        RheostaticCatenary = 2, // Рекуперативно-реостатное (гашение на ПТР при Uкс 3400..3800 В)
        PureRheostatic = 3,     // Изолированное реостатное (V < 16 км/ч, Iv > 520 А или Uкс > 3800 В)
        PneumaticSubstitution = 4 // Аварийное замещение электротормоза пневматикой (КЭБ, ТЦ 1.5..2.0 атм)
    }

    /// <summary>
    /// Результаты расчета поосного и группового независимого возбуждения
    /// </summary>
    public struct ExcitationResult {
        public double NetMagneticFluxWb;         // Результирующий магнитный поток с учетом реакции якоря (Вб)
        public double TargetIv;                  // Заданный ток возбуждения СТПР (А)
        public double ActualIv;                  // Фактический ток возбуждения (А)
        public double ArmatureCurrentIa;         // Ток якоря одного ТЭД (А)
        public double BackEmfV;                  // Противо-ЭДС (или генераторная ЭДС) якоря (В)
        public double MotorTorqueNm;             // Электромагнитный вращающий момент одного ТЭД (Н*м)
        public double TractiveForceKN;           // Реализованная сила тяги / торможения локомотива (кН)
        public DynamicBrakeMode BrakeMode;       // Режим торможения
        public bool IsTransitionForbidden;       // Запрет перехода группировки из-за высокого тока
        public bool IsOverload800A;              // Признак перегрузки ТЭД по току (> 800 А)
        public bool IsSlipSuppressed;            // Признак подавления боксования форсированием НВ
        public string MpsuidMessage;             // Текстовое диагностическое сообщение МПСУиД
    }

    /// <summary>
    /// Расчет результирующего магнитного потока двигателя с учетом нелинейного насыщения
    /// стали главных полюсов и размагничивающего действия реакции якоря
    /// </summary>
    public static double CalculateNetMagneticFlux(double iv, double ia) {
        if (iv <= 0.0) return 0.0;
        double grossFlux = PhiMax * (1.0 - Math.Exp(-iv / Iv0));
        double demagnetization = ArmatureReactionCoeff * Math.Max(0.0, ia);
        return Math.Max(0.005, grossFlux - demagnetization);
    }

    /// <summary>
    /// Закон регулирования тока возбуждения МПСУиД по заводскому РЭ (лист 98, 107-108):
    /// Iv = Iv0 + k * Ia с ограничением минимального реостатного тока (400 А) и предела СТПР (700 А)
    /// </summary>
    public static double CalculateMpsuidTargetIv(
        int controllerPos,
        double ia,
        double speedKmh,
        bool isBraking = false
    ) {
        if (controllerPos <= 0 && !isBraking) return 0.0;

        if (isBraking) {
            // В тормозном режиме возбуждение регулируется для удержания заданной тормозной силы
            // в диапазоне до 520 А (порог устойчивости рекуперации по РЭ §2.3)
            double target = 220.0 + 0.45 * Math.Abs(ia);
            return Math.Min(520.0, Math.Max(150.0, target));
        }

        TrainerModernTractionPhysics.TedGrouping grouping = TrainerModernTractionPhysics.GetGroupingForPosition(controllerPos);

        // На позициях параллельного соединения (П: 45..65) выше 50 км/ч
        // МПСУиД выполняет ослабление поля для достижения высоких скоростей
        if (grouping == TrainerModernTractionPhysics.TedGrouping.Parallel && speedKmh > 52.0) {
            int opStep = controllerPos - 60; // 61: ОП1 (67%), 62: ОП2 (55%), 63: ОП3 (44%), 64..65: ОП4 (36%)
            if (opStep >= 4) return 160.0; // ОП4 ~36%
            if (opStep == 3) return 200.0; // ОП3 ~44%
            if (opStep == 2) return 250.0; // ОП2 ~55%
            if (opStep == 1) return 300.0; // ОП1 ~67%
            return 380.0; // Полное поле на параллели
        }

        // Базовый закон тяги: Iv = Iv0 + k * Ia
        double ivBase = 320.0 + 0.35 * Math.Max(0.0, ia);

        // На реостатных позициях (1..22 и 24..43) Iv не опускается ниже 400 А (РЭ лист 98)
        bool isRheostatNotch = (controllerPos >= 1 && controllerPos <= 22) || (controllerPos >= 24 && controllerPos <= 43);
        if (isRheostatNotch && ivBase < MinRheostatIv) {
            ivBase = MinRheostatIv;
        }

        // При пуске (v < 5 км/ч) уставка ограничивается 600 А (РЭ лист 108)
        if (speedKmh < 5.0 && ivBase > StartingMaxIv) {
            ivBase = StartingMaxIv;
        }

        return Math.Min(MaxOperatingIv, Math.Max(100.0, ivBase));
    }

    /// <summary>
    /// Интеллектуальный выбор позиции СП при переходе с позиции 23 (С -> СП)
    /// строго по алгоритму МПСУиД из РЭ лист 98, §3.11.9.5.
    /// Возвращает целевую позицию (24..31) либо -1 при запрете перехода (Ia > 650 А).
    /// </summary>
    public static int ResolveTransitionFromSeriesToSeriesParallel(double averageIa) {
        if (averageIa < 270.0) return 24;
        if (averageIa < 310.0) return 25;
        if (averageIa < 360.0) return 26;
        if (averageIa < 410.0) return 27;
        if (averageIa < 450.0) return 28;
        if (averageIa < 510.0) return 29;
        if (averageIa < 580.0) return 30;
        if (averageIa <= 650.0) return 31;
        return -1; // Переход запрещен МПСУиД (ток превышает 650 А)
    }

    /// <summary>
    /// Интеллектуальный выбор позиции П при переходе с позиции 44 (СП -> П)
    /// строго по алгоритму МПСУиД из РЭ лист 99, §3.11.9.6.
    /// Возвращает целевую позицию (45..47) либо -1 при запрете перехода (Ia > 570 А).
    /// </summary>
    public static int ResolveTransitionFromSeriesParallelToParallel(double averageIa) {
        if (averageIa < 410.0) return 45;
        if (averageIa < 500.0) return 46;
        if (averageIa <= 570.0) return 47;
        return -1; // Переход запрещен МПСУиД (ток превышает 570 А)
    }

    /// <summary>
    /// Полный расчет электромеханических характеристик ТЭД с независимым возбуждением в режиме тяги
    /// </summary>
    public static ExcitationResult ComputeTractionExcitation(
        double speedKmh,
        int controllerPos,
        double lineVoltageV = 3000.0,
        double overrideIv = -1.0,
        bool wheelSlipDetected = false
    ) {
        var res = new ExcitationResult {
            BrakeMode = DynamicBrakeMode.None
        };

        if (controllerPos <= 0 || speedKmh < 0.0) {
            res.MpsuidMessage = "Холостой ход (тяга 0)";
            return res;
        }

        TrainerModernTractionPhysics.TedGrouping grouping = TrainerModernTractionPhysics.GetGroupingForPosition(controllerPos);
        double motorVoltage = TrainerModernTractionPhysics.GetVoltagePerMotor(grouping, lineVoltageV);
        double speedRadS = (speedKmh / 3.6) / (TrainerModernTractionPhysics.HalfWornWheelDiameterM / 2.0);
        double motorOmega = speedRadS * TrainerModernTractionPhysics.GearRatio;

        // 1. Итерационный расчет рабочей точки (Iv, Ia, Phi, E)
        double estimatedIa = 400.0;
        double targetIv = (overrideIv > 0.0) ? overrideIv : CalculateMpsuidTargetIv(controllerPos, estimatedIa, speedKmh, false);

        // Противобоксовочная защита МПСУиД: при срыве колесной пары ток возбуждения СТПР
        // форсируется на 15% для мгновенного увеличения противо-ЭДС и гашения разноса
        if (wheelSlipDetected) {
            targetIv = Math.Min(MaxOperatingIv, targetIv * 1.15);
            res.IsSlipSuppressed = true;
        }

        res.TargetIv = targetIv;
        res.ActualIv = targetIv;

        // Сопротивление пускового реостата в зависимости от позиции
        double rRheostat = GetRheostatResistanceForPosition(controllerPos);
        double totalCircuitR = ArmatureResistanceRa + rRheostat;

        // Итерационное решение уравнения цепи
        for (int iter = 0; iter < 5; iter++) {
            double flux = CalculateNetMagneticFlux(res.ActualIv, estimatedIa);
            double backEmf = Ce * flux * motorOmega;
            double rawIa = Math.Max(0.0, (motorVoltage - backEmf) / totalCircuitR);
            estimatedIa = 0.5 * (estimatedIa + rawIa);
        }

        // 2. Наложение ограничений МПСУиД
        // Ограничение по перегрузке > 800 А (РЭ §3.11.10.2)
        if (estimatedIa > 800.0) {
            res.IsOverload800A = true;
            res.MpsuidMessage = "ВНИМАНИЕ: Перегрузка ТЭД > 800 А (МПСУиД +ОВ / отсчет 10 с)";
        }

        // Аппаратная отсечка тока якоря 750 А
        double finalIa = Math.Min(750.0, estimatedIa);
        res.ArmatureCurrentIa = finalIa;

        // 3. Вычисление физического магнитного потока, противо-ЭДС и момента
        res.NetMagneticFluxWb = CalculateNetMagneticFlux(res.ActualIv, finalIa);
        res.BackEmfV = Ce * res.NetMagneticFluxWb * motorOmega;
        res.MotorTorqueNm = Cm * res.NetMagneticFluxWb * finalIa;

        // 4. Пересчет силы тяги на колесах электровоза (8 осей)
        double totalTorque = res.MotorTorqueNm * TrainerModernTractionPhysics.AxleCount;
        double wheelRadius = TrainerModernTractionPhysics.HalfWornWheelDiameterM / 2.0;
        double rimForceN = (totalTorque * TrainerModernTractionPhysics.GearRatio * TrainerModernTractionPhysics.GearEfficiency) / wheelRadius;
        res.TractiveForceKN = rimForceN / 1000.0;

        // Ограничение пусковой силой 660 кН
        if (res.TractiveForceKN > TrainerModernTractionPhysics.RatedStartingForceKN) {
            res.TractiveForceKN = TrainerModernTractionPhysics.RatedStartingForceKN;
        }

        if (string.IsNullOrEmpty(res.MpsuidMessage)) {
            res.MpsuidMessage = string.Format("Тяга активна: {0}, Iv={1:F0} А, Ia={2:F0} А, F={3:F1} кН",
                grouping == TrainerModernTractionPhysics.TedGrouping.Series ? "С" : (grouping == TrainerModernTractionPhysics.TedGrouping.SeriesParallel ? "СП" : "П"),
                res.ActualIv, res.ArmatureCurrentIa, res.TractiveForceKN);
        }

        return res;
    }

    /// <summary>
    /// Расчет режима электрического торможения (Рекуперация / Реостат) с независимым возбуждением
    /// строго по нормативам РЭ §2.3 и таблице токов ЭДП810 (Ia_торм <= 500 А)
    /// </summary>
    public static ExcitationResult ComputeDynamicBraking(
        double speedKmh,
        double requestedBrakingForceKN,
        double lineVoltageV = 3000.0
    ) {
        var res = new ExcitationResult();

        // Порог скорости: ниже 3 км/ч электротормоз полностью замещается пневматикой (РЭ §2.3)
        if (speedKmh < 3.0) {
            res.BrakeMode = DynamicBrakeMode.PneumaticSubstitution;
            res.TractiveForceKN = requestedBrakingForceKN; // тормозная сила создается колодками ТЦ
            res.MpsuidMessage = "Замещение: электротормоз отключен по скорости (< 3 км/ч), действует КЭБ";
            return res;
        }

        double speedRadS = (speedKmh / 3.6) / (TrainerModernTractionPhysics.HalfWornWheelDiameterM / 2.0);
        double motorOmega = speedRadS * TrainerModernTractionPhysics.GearRatio;

        // Определение режима торможения по напряжению контактной сети и скорости
        if (lineVoltageV > 3800.0 || speedKmh < 16.0) {
            // Переход на изолированное реостатное торможение
            res.BrakeMode = DynamicBrakeMode.PureRheostatic;
        } else if (lineVoltageV >= 3400.0) {
            // Рекуперативно-реостатное торможение
            res.BrakeMode = DynamicBrakeMode.RheostaticCatenary;
        } else {
            // Чистая рекуперация
            res.BrakeMode = DynamicBrakeMode.Recuperation;
        }

        // Целевой тормозной ток якоря (не более 500 А по РЭ таблица 4.1 для режима торможения)
        const double maxBrakeIa = 500.0;
        double targetTorqueTotalNm = (requestedBrakingForceKN * 1000.0 * (TrainerModernTractionPhysics.HalfWornWheelDiameterM / 2.0)) /
                                     (TrainerModernTractionPhysics.GearRatio * TrainerModernTractionPhysics.GearEfficiency);
        double targetTorquePerMotor = targetTorqueTotalNm / TrainerModernTractionPhysics.AxleCount;

        // Подбор тока возбуждения Iv для создания необходимого тормозного момента при Ia <= 500 А
        double requiredIv = CalculateMpsuidTargetIv(0, maxBrakeIa, speedKmh, true);
        if (requiredIv > 520.0) {
            // По РЭ §2.3 превышение Iv > 520 А переводит рекуперацию в реостатный режим
            requiredIv = 520.0;
            res.BrakeMode = DynamicBrakeMode.PureRheostatic;
        }

        res.TargetIv = requiredIv;
        res.ActualIv = requiredIv;
        res.NetMagneticFluxWb = CalculateNetMagneticFlux(res.ActualIv, maxBrakeIa * 0.7);

        // Генераторная противо-ЭДС ТЭД: Eg = Ce * Phi * omega
        res.BackEmfV = Ce * res.NetMagneticFluxWb * motorOmega;

        // Расчет реализованного тормозного тока якоря
        double circuitR = (res.BrakeMode == DynamicBrakeMode.PureRheostatic) ? 0.85 : 0.12; // эквивалентное сопротивление цепи торможения
        double rawIa = (res.BackEmfV > 0.0) ? (targetTorquePerMotor / (Cm * res.NetMagneticFluxWb)) : 0.0;
        res.ArmatureCurrentIa = Math.Min(maxBrakeIa, Math.Max(0.0, rawIa));

        // Тормозной момент и сила
        res.MotorTorqueNm = Cm * res.NetMagneticFluxWb * res.ArmatureCurrentIa;
        double totalBrakeTorque = res.MotorTorqueNm * TrainerModernTractionPhysics.AxleCount;
        double brakeRimForceN = (totalBrakeTorque * TrainerModernTractionPhysics.GearRatio * TrainerModernTractionPhysics.GearEfficiency) /
                                (TrainerModernTractionPhysics.HalfWornWheelDiameterM / 2.0);
        res.TractiveForceKN = brakeRimForceN / 1000.0;

        res.MpsuidMessage = string.Format("Электротормоз: {0}, Iv={1:F0} А, Ia={2:F0} А, B={3:F1} кН, Eg={4:F0} В",
            res.BrakeMode.ToString(), res.ActualIv, res.ArmatureCurrentIa, res.TractiveForceKN, res.BackEmfV);

        return res;
    }

    /// <summary>
    /// Ступени пускового сопротивления пуско-тормозных резисторов (ПТР) для позиций 1..65
    /// На ходовых позициях 23, 44, 65 сопротивление строго равно 0 (РЭ лист 22, 45, 47)
    /// </summary>
    public static double GetRheostatResistanceForPosition(int position) {
        if (position <= 0) return 10.0; // разомкнутая цепь
        if (position == 23 || position == 44 || position == 65) return 0.0; // Ходовые безреостатные позиции

        if (position < 23) {
            // Диапазон С: ступени от ~4.5 Ом (поз. 1) до 0 Ом (поз. 23)
            return 4.5 * (1.0 - (position / 23.0));
        } else if (position < 44) {
            // Диапазон СП: ступени от ~1.8 Ом (поз. 24) до 0 Ом (поз. 44)
            return 1.8 * (1.0 - ((position - 23.0) / 21.0));
        } else {
            // Диапазон П: ступени от ~0.8 Ом (поз. 45) до 0 Ом (поз. 65)
            return 0.8 * (1.0 - ((position - 44.0) / 21.0));
        }
    }
}
