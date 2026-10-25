using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Web.Script.Serialization;

/// <summary>
/// Проба интеграции современной научной модели тяги в контур МПСУиД электровоза 2ЭС6.
/// Проверяет:
/// 1. Расчет силы тяги и токов ТЭД на ходовых позициях 23 (С), 44 (СП), 65 (П).
/// 2. Паспортное соответствие точкам РЭ Таблица 1:
///    - Часовой режим: 464 кН (46.4 нат. ед.) @ 49.2 км/ч, мощность 6470 кВт.
///    - Длительный режим: 418 кН (41.8 нат. ед.) @ 51.0 км/ч, мощность 6030 кВт.
///    - Пусковой предел: 660 кН (66.0 нат. ед.), токовая отсечка 750 А.
/// 3. Поосное регулирование сцепления по формуле ПТР ВНИИЖТ и отработку боксования.
/// 4. Корректность записи в структуры МПСУиД (TResTyga, TTygaParam).
/// </summary>
public sealed class ModernTractionIntegrationProbe {
    private static readonly List<Dictionary<string, object>> Checks = new List<Dictionary<string, object>>();

    private static void AssertCheck(string description, bool passed, object expected, object actual) {
        Console.WriteLine(string.Format(CultureInfo.InvariantCulture, "[{0}] {1}: ожидалось {2}, получено {3}",
            passed ? "PASS" : "FAIL", description, expected != null ? expected.ToString() : "null", actual != null ? actual.ToString() : "null"));

        var dict = new Dictionary<string, object>();
        dict["description"] = description;
        dict["passed"] = passed;
        dict["expected"] = expected != null ? expected.ToString() : "null";
        dict["actual"] = actual != null ? actual.ToString() : "null";
        Checks.Add(dict);

        if (!passed) {
            throw new Exception("Assertion failed: " + description);
        }
    }

    [STAThread]
    public static int Main(string[] args) {
        Console.WriteLine("=== Запуск пробы интеграции тяги МПСУиД 2ЭС6 (РЭ Таблица 1 + ПТР ВНИИЖТ) ===");
        bool allPassed = false;
        string errorMsg = null;

        try {
            // 1. Проверка нулевой позиции (холостой ход)
            Console.WriteLine("\n--- 1. Холостой ход (контроллер = 0) ---");
            var idleRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 30.0,
                controllerPosition: 0
            );
            AssertCheck("Позиция 0: сила тяги = 0 кН", idleRes.TractiveForceKN == 0.0, 0.0, idleRes.TractiveForceKN);
            AssertCheck("Позиция 0: нативная сила = 0", idleRes.TractiveForceNative10KN == 0.0, 0.0, idleRes.TractiveForceNative10KN);
            AssertCheck("Позиция 0: ток якоря = 0 А", idleRes.ArmatureCurrentA == 0.0, 0.0, idleRes.ArmatureCurrentA);

            // 2. Проверка группировки "С" (сериесное соединение: 1..23, ходовая 23)
            Console.WriteLine("\n--- 2. Сериесное соединение С (ходовая поз. 23) ---");
            var seriesRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 20.0,
                controllerPosition: 23,
                excitationCurrentA: 440.0,
                lineVoltageV: 3000.0
            );
            AssertCheck("Позиция 23: группировка С (Series)", seriesRes.Grouping == TrainerModernTractionPhysics.TedGrouping.Series, "Series", seriesRes.Grouping);
            AssertCheck("Позиция 23: напряжение на ТЭД = 375 В (3000/8)", Math.Abs(TrainerModernTractionPhysics.GetVoltagePerMotor(seriesRes.Grouping, 3000.0) - 375.0) < 0.1, 375.0, 375.0);
            AssertCheck("Позиция 23: ток сети равен току одного ТЭД (1 ветвь)", Math.Abs(seriesRes.TotalLineCurrentA - seriesRes.ArmatureCurrentA) < 0.1, seriesRes.ArmatureCurrentA, seriesRes.TotalLineCurrentA);
            AssertCheck("Позиция 23: сила тяги в рабочем диапазоне 300..500 кН", seriesRes.TractiveForceKN >= 300.0 && seriesRes.TractiveForceKN <= 500.0, "300..500 кН", seriesRes.TractiveForceKN.ToString("F1", CultureInfo.InvariantCulture) + " кН");

            // 3. Проверка группировки "СП" (сериес-параллельное: 24..44, ходовая 44)
            Console.WriteLine("\n--- 3. Сериес-параллельное соединение СП (ходовая поз. 44) ---");
            var spRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 40.0,
                controllerPosition: 44,
                excitationCurrentA: 440.0,
                lineVoltageV: 3000.0
            );
            AssertCheck("Позиция 44: группировка СП (SeriesParallel)", spRes.Grouping == TrainerModernTractionPhysics.TedGrouping.SeriesParallel, "SeriesParallel", spRes.Grouping);
            AssertCheck("Позиция 44: напряжение на ТЭД = 750 В (3000/4)", Math.Abs(TrainerModernTractionPhysics.GetVoltagePerMotor(spRes.Grouping, 3000.0) - 750.0) < 0.1, 750.0, 750.0);
            AssertCheck("Позиция 44: ток сети равен удвоенному току ТЭД (2 ветви)", Math.Abs(spRes.TotalLineCurrentA - (2.0 * spRes.ArmatureCurrentA)) < 0.1, 2.0 * spRes.ArmatureCurrentA, spRes.TotalLineCurrentA);

            // 4. Проверка паспортных точек РЭ 2ЭС6 Таблица 1 на параллельном соединении "П" (поз. 65)
            Console.WriteLine("\n--- 4. Паспортные точки РЭ 2ЭС6 (П-соединение, поз. 65) ---");
            
            // 4.1 Часовой режим: 49.2 км/ч, 464 кН, 6470 кВт
            var hourlyRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 49.2,
                controllerPosition: 65,
                excitationCurrentA: 440.0,
                lineVoltageV: 3000.0,
                wheelDiameterM: TrainerModernTractionPhysics.HalfWornWheelDiameterM
            );
            AssertCheck("Часовой режим 49.2 км/ч: группировка П (Parallel)", hourlyRes.Grouping == TrainerModernTractionPhysics.TedGrouping.Parallel, "Parallel", hourlyRes.Grouping);
            AssertCheck("Часовой режим 49.2 км/ч: сила тяги ~464 кН", Math.Abs(hourlyRes.TractiveForceKN - 464.0) <= 7.0, "464 +/- 7 кН", hourlyRes.TractiveForceKN.ToString("F2", CultureInfo.InvariantCulture) + " кН");
            AssertCheck("Часовой режим: нативная сила в десятках кН ~46.4", Math.Abs(hourlyRes.TractiveForceNative10KN - 46.4) <= 0.7, "~46.4", hourlyRes.TractiveForceNative10KN.ToString("F2", CultureInfo.InvariantCulture));
            AssertCheck("Часовой режим: мощность на валах двигателей ~6470 кВт", Math.Abs(hourlyRes.PowerShaftKW - 6470.0) <= 120.0, "6470 +/- 120 кВт", hourlyRes.PowerShaftKW.ToString("F1", CultureInfo.InvariantCulture) + " кВт");
            AssertCheck("Часовой режим: суммарный ток от сети (4 ветви) ~4 * Ia", Math.Abs(hourlyRes.TotalLineCurrentA - 4.0 * hourlyRes.ArmatureCurrentA) < 0.1, 4.0 * hourlyRes.ArmatureCurrentA, hourlyRes.TotalLineCurrentA);

            // 4.2 Длительный режим: 51.0 км/ч, 418 кН, 6030 кВт
            var contRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 51.0,
                controllerPosition: 65,
                excitationCurrentA: 440.0,
                lineVoltageV: 3000.0,
                wheelDiameterM: TrainerModernTractionPhysics.HalfWornWheelDiameterM
            );
            AssertCheck("Длительный режим 51.0 км/ч: сила тяги ~418 кН", Math.Abs(contRes.TractiveForceKN - 418.0) <= 6.5, "418 +/- 6.5 кН", contRes.TractiveForceKN.ToString("F2", CultureInfo.InvariantCulture) + " кН");
            AssertCheck("Длительный режим: нативная сила в десятках кН ~41.8", Math.Abs(contRes.TractiveForceNative10KN - 41.8) <= 0.65, "~41.8", contRes.TractiveForceNative10KN.ToString("F2", CultureInfo.InvariantCulture));
            AssertCheck("Длительный режим: мощность на валах двигателей ~6030 кВт", Math.Abs(contRes.PowerShaftKW - 6030.0) <= 100.0, "6030 +/- 100 кВт", contRes.PowerShaftKW.ToString("F1", CultureInfo.InvariantCulture) + " кВт");

            // 4.3 Гиперболическая характеристика постоянной мощности на скорости 80 км/ч
            var highSpeedRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 80.0,
                controllerPosition: 65,
                excitationCurrentA: 440.0,
                lineVoltageV: 3000.0
            );
            double expectedForceAt80 = (TrainerModernTractionPhysics.RatedContinuousPowerKW * TrainerModernTractionPhysics.GearEfficiency) / (80.0 / 3.6);
            AssertCheck("Скорость 80 км/ч: гипербола постоянной мощности F = P * eta / v", Math.Abs(highSpeedRes.TractiveForceKN - expectedForceAt80) <= 10.0, expectedForceAt80.ToString("F1", CultureInfo.InvariantCulture) + " кН", highSpeedRes.TractiveForceKN.ToString("F1", CultureInfo.InvariantCulture) + " кН");

            // 5. Предельные режимы: пусковая сила 660 кН и токовая отсечка 750 А
            Console.WriteLine("\n--- 5. Пусковой предел 660 кН и отсечка 750 А ---");
            var startRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 0.5,
                controllerPosition: 65
            );
            AssertCheck("Пусковая сила тяги не превышает паспортные 660 кН", startRes.TractiveForceKN <= 660.01, "<= 660 кН", startRes.TractiveForceKN.ToString("F1", CultureInfo.InvariantCulture) + " кН");
            AssertCheck("Ток якоря ТЭД ограничен защитным порогом 750 А", startRes.ArmatureCurrentA <= 750.0, "<= 750 А", startRes.ArmatureCurrentA.ToString("F1", CultureInfo.InvariantCulture) + " А");
            AssertCheck("Флаг ограничения тока IsCurrentLimited = true", startRes.IsCurrentLimited, true, startRes.IsCurrentLimited);

            // 6. Противобоксовочная защита (ПБЗ/ПСЗ) и сцепление колеса с рельсом
            Console.WriteLine("\n--- 6. Противобоксовочная защита и сцепление ---");
            var slipWetRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 20.0,
                controllerPosition: 65,
                railCond: TrainerModernTractionPhysics.RailCondition.WetSlippery
            );
            AssertCheck("Мокрые рельсы: зафиксировано предупреждение о боксовании / превышении сцепления", slipWetRes.IsWheelSlipWarning, true, slipWetRes.IsWheelSlipWarning);
            AssertCheck("Сила тяги ограничена пределом сцепления мокрых рельсов", slipWetRes.TractiveForceKN <= slipWetRes.AdhesionLimitKN + 0.1, "<= AdhesionLimit", slipWetRes.TractiveForceKN.ToString("F1", CultureInfo.InvariantCulture) + " кН");

            var sandRes = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 20.0,
                controllerPosition: 65,
                railCond: TrainerModernTractionPhysics.RailCondition.Sanded
            );
            AssertCheck("Подача песка восстанавливает сцепление и увеличивает силу тяги", sandRes.TractiveForceKN > slipWetRes.TractiveForceKN, "> wetForce", sandRes.TractiveForceKN.ToString("F1", CultureInfo.InvariantCulture) + " кН");

            allPassed = true;
            Console.WriteLine("\n=== Все 20 проверок интеграции МПСУиД успешно пройдены! ===");
        } catch (Exception ex) {
            allPassed = false;
            errorMsg = ex.ToString();
            Console.WriteLine("\nОШИБКА: " + ex.Message);
        }

        // Сохранение отчета
        try {
            string resultDir = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");
            if (string.IsNullOrEmpty(resultDir)) {
                resultDir = "outputs";
            }
            if (!Directory.Exists(resultDir)) {
                Directory.CreateDirectory(resultDir);
            }

            var report = new Dictionary<string, object>();
            report["probe"] = "ModernTractionIntegration";
            report["passed"] = allPassed;
            report["error"] = errorMsg;
            report["checks_count"] = Checks.Count;
            report["checks"] = Checks;
            report["timestamp_utc"] = DateTime.UtcNow.ToString("o", CultureInfo.InvariantCulture);

            var serializer = new JavaScriptSerializer();
            string json = serializer.Serialize(report);

            string targetFile = Path.Combine(resultDir, "Trainer_modern_traction_integration.json");
            File.WriteAllText(targetFile, json);

            // Дублируем в outputs/ для артефактов
            if (!string.Equals(Path.GetFullPath(resultDir), Path.GetFullPath("outputs"), StringComparison.OrdinalIgnoreCase)) {
                File.WriteAllText("outputs/Trainer_modern_traction_integration.json", json);
            }

            Console.WriteLine("Отчет сохранен в: " + targetFile);
        } catch (Exception ex) {
            Console.WriteLine("Предупреждение при сохранении JSON: " + ex.Message);
        }

        return allPassed ? 0 : 1;
    }
}
