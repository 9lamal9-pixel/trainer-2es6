using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Text;

/// <summary>
/// Программа всесторонней валидации современной математической модели тяги 2ЭС6.
/// Проверяет соответствие паспортным точкам РЭ 2ЭС6, законам сцепления ПТР ВНИИЖТ,
/// балансу мощностей и динамике разгона тяжеловесного состава 6200 т.
/// </summary>
public sealed class ModernTractionValidationProbe {
    private static readonly List<Dictionary<string, object>> checks = new List<Dictionary<string, object>>();
    private static int totalPassed = 0;
    private static int totalFailed = 0;

    private static void AssertCheck(string name, bool condition, object expected, object actual) {
        var item = new Dictionary<string, object> {
            { "name", name },
            { "passed", condition },
            { "expected", (expected != null ? expected.ToString() : "null") },
            { "actual", (actual != null ? actual.ToString() : "null") }
        };
        checks.Add(item);
        if (condition) {
            totalPassed++;
            Console.WriteLine("[PASS] " + name + ": actual=" + actual);
        } else {
            totalFailed++;
            Console.WriteLine("[FAIL] " + name + ": expected=" + expected + ", actual=" + actual);
        }
    }

    [STAThread]
    public static int Main(string[] args) {
        Console.WriteLine("=== Старт валидации современной научной модели тяги 2ЭС6 ===");
        checks.Clear();
        totalPassed = 0;
        totalFailed = 0;

        try {
            // ТЕСТ 1: Паспортные контрольные точки РЭ 2ЭС6 (Часть 1, Таблица 1)
            // Условия испытаний: D = 1210 мм, Uкс = 3000 В, номинальное возбуждение
            Console.WriteLine("\n--- Тест 1: Паспортные точки РЭ 2ЭС6 ---");
            
            // 1.1 Часовой режим: v = 49.2 км/ч, F = 464 кН, P = 6440 кВт
            var resHourly = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 49.2,
                controllerPosition: 54, // Полное П-соединение
                excitationCurrentA: TrainerModernTractionPhysics.NominalExcitationCurrentA,
                lineVoltageV: 3000.0,
                wheelDiameterM: TrainerModernTractionPhysics.HalfWornWheelDiameterM
            );

            // Допуск 1.5% относительно паспортных 464 кН
            bool hourlyForceOk = Math.Abs(resHourly.TractiveForceKN - 464.0) <= 7.0;
            AssertCheck("Часовой режим 49.2 км/ч: сила тяги ~464 кН", hourlyForceOk, "464 +/- 7 кН", resHourly.TractiveForceKN.ToString("F2", CultureInfo.InvariantCulture) + " кН");

            // 1.2 Длительный режим: v = 51.0 км/ч, F = 418 кН, P = 6000 кВт
            var resContinuous = TrainerModernTractionPhysics.CalculateTraction(
                speedKmh: 51.0,
                controllerPosition: 54,
                excitationCurrentA: TrainerModernTractionPhysics.NominalExcitationCurrentA,
                lineVoltageV: 3000.0,
                wheelDiameterM: TrainerModernTractionPhysics.HalfWornWheelDiameterM
            );

            // Допуск 1.5% относительно паспортных 418 кН
            bool continuousForceOk = Math.Abs(resContinuous.TractiveForceKN - 418.0) <= 6.5;
            AssertCheck("Длительный режим 51.0 км/ч: сила тяги ~418 кН", continuousForceOk, "418 +/- 6.5 кН", resContinuous.TractiveForceKN.ToString("F2", CultureInfo.InvariantCulture) + " кН");

            // 1.3 Пересчет в нативные единицы тренажера (1 ед. = 10 кН)
            AssertCheck("Нативная сила часового режима ~46.4", Math.Abs(resHourly.TractiveForceNative10KN - 46.4) <= 0.7, "~46.4", resHourly.TractiveForceNative10KN.ToString("F2", CultureInfo.InvariantCulture));
            AssertCheck("Нативная сила длительного режима ~41.8", Math.Abs(resContinuous.TractiveForceNative10KN - 41.8) <= 0.65, "~41.8", resContinuous.TractiveForceNative10KN.ToString("F2", CultureInfo.InvariantCulture));

            // ТЕСТ 2: Закон сцепления колеса с рельсом (ПТР ВНИИЖТ)
            Console.WriteLine("\n--- Тест 2: Закон сцепления с рельсами (ПТР) ---");
            double psi0 = TrainerModernTractionPhysics.CalculateAdhesionCoefficient(0.0);
            double psi50 = TrainerModernTractionPhysics.CalculateAdhesionCoefficient(50.0);
            double psi100 = TrainerModernTractionPhysics.CalculateAdhesionCoefficient(100.0);

            AssertCheck("Сцепление при 0 км/ч psi = 0.42", Math.Abs(psi0 - 0.420) < 0.005, "0.420", psi0.ToString("F4", CultureInfo.InvariantCulture));
            AssertCheck("Сцепление при 50 км/ч psi ~ 0.327", Math.Abs(psi50 - 0.3267) < 0.005, "0.3267", psi50.ToString("F4", CultureInfo.InvariantCulture));
            AssertCheck("Сцепление при 100 км/ч psi ~ 0.308", Math.Abs(psi100 - 0.308) < 0.005, "0.308", psi100.ToString("F4", CultureInfo.InvariantCulture));
            AssertCheck("Монотонное убывание сцепления с ростом скорости", (psi0 > psi50 && psi50 > psi100), "psi0 > psi50 > psi100", true);

            // Сравнение состояния рельсов
            double psiWet = TrainerModernTractionPhysics.CalculateAdhesionCoefficient(20.0, TrainerModernTractionPhysics.RailCondition.WetSlippery);
            double psiDry = TrainerModernTractionPhysics.CalculateAdhesionCoefficient(20.0, TrainerModernTractionPhysics.RailCondition.DryClean);
            double psiSand = TrainerModernTractionPhysics.CalculateAdhesionCoefficient(20.0, TrainerModernTractionPhysics.RailCondition.Sanded);

            AssertCheck("Сцепление на мокрых рельсах ниже сухого", psiWet < psiDry, "< dry", psiWet.ToString("F3", CultureInfo.InvariantCulture));
            AssertCheck("Сцепление с песком выше сухого", psiSand > psiDry, "> dry", psiSand.ToString("F3", CultureInfo.InvariantCulture));

            // ТЕСТ 3: Группировки ТЭД (С, СП, П) и защитный clamp тока 750 А
            Console.WriteLine("\n--- Тест 3: Группировки и токовая отсечка ---");
            var resSeries = TrainerModernTractionPhysics.CalculateTraction(10.0, 20); // С-соединение
            var resSP = TrainerModernTractionPhysics.CalculateTraction(30.0, 40);     // СП-соединение
            var resP = TrainerModernTractionPhysics.CalculateTraction(50.0, 50);      // П-соединение

            AssertCheck("Позиция 20 -> Группировка С (Series)", resSeries.Grouping == TrainerModernTractionPhysics.TedGrouping.Series, "Series", resSeries.Grouping);
            AssertCheck("Позиция 40 -> Группировка СП (SeriesParallel)", resSP.Grouping == TrainerModernTractionPhysics.TedGrouping.SeriesParallel, "SeriesParallel", resSP.Grouping);
            AssertCheck("Позиция 50 -> Группировка П (Parallel)", resP.Grouping == TrainerModernTractionPhysics.TedGrouping.Parallel, "Parallel", resP.Grouping);

            // Проверка ограничения тока 750 А при трогании на высокой позиции
            var resHighCurrent = TrainerModernTractionPhysics.CalculateTraction(1.0, 54);
            AssertCheck("Ток якоря ограничен предельным значением 750 А", resHighCurrent.ArmatureCurrentA <= 750.0, "<= 750.0 A", resHighCurrent.ArmatureCurrentA.ToString("F1", CultureInfo.InvariantCulture) + " A");
            AssertCheck("Флаг ограничения тока IsCurrentLimited = true", resHighCurrent.IsCurrentLimited, true, resHighCurrent.IsCurrentLimited);

            // ТЕСТ 4: Энергетический баланс и КПД передачи
            Console.WriteLine("\n--- Тест 4: Энергетический баланс и КПД ---");
            var resEnergy = TrainerModernTractionPhysics.CalculateTraction(60.0, 54);
            AssertCheck("Касательная мощность положительна", resEnergy.PowerRimKW > 0, "> 0", resEnergy.PowerRimKW.ToString("F1", CultureInfo.InvariantCulture) + " кВт");
            AssertCheck("Мощность на валах выше мощности на ободе (КПД = 0.98)", resEnergy.PowerShaftKW > resEnergy.PowerRimKW, "> PowerRim", resEnergy.PowerShaftKW.ToString("F1", CultureInfo.InvariantCulture) + " кВт");
            AssertCheck("Электрическая мощность сети покрывает механическую", resEnergy.PowerElectricKW >= resEnergy.PowerShaftKW, ">= PowerShaft", resEnergy.PowerElectricKW.ToString("F1", CultureInfo.InvariantCulture) + " кВт");

            // ТЕСТ 5: Моделирование динамического разгона поезда 6200 т (2ЭС6 + 75 вагонов)
            Console.WriteLine("\n--- Тест 5: Динамический разгон тяжеловесного состава 6200 т ---");
            const double totalMassTonnes = 6200.0;
            const double g = 9.80665;
            const double gamma = 0.06; // Коэффициент инерции вращающихся масс
            double currentSpeedKmh = 0.0;
            double distanceM = 0.0;
            const double dt = 0.1; // Шаг 100 мс
            int steps = 600; // 60 секунд разгона

            double initialAcceleration = 0;
            double finalAcceleration = 0;

            for (int t = 0; t < steps; t++) {
                // Имитация переключения позиций машинистом: 1 позиция каждые 0.8 с
                int targetPos = Math.Min(54, (t / 8) + 1);
                var traction = TrainerModernTractionPhysics.CalculateTraction(currentSpeedKmh, targetPos);

                // Основное сопротивление состава (кН)
                double w0_loc = TrainerModernTractionPhysics.CalculateLocomotiveResistanceNPerKN(currentSpeedKmh);
                double w0_wag = TrainerModernTractionPhysics.CalculateWagonsResistanceNPerKN(currentSpeedKmh, 20.0);
                double totalResistanceKN = ((200.0 * w0_loc + 6000.0 * w0_wag) * g) / 1000.0;

                // Уравнение движения Ньютона: a = (Fk - W0) / (M * (1 + gamma))
                double netForceKN = traction.TractiveForceKN - totalResistanceKN;
                double acceleration = netForceKN / (totalMassTonnes * (1.0 + gamma));

                if (t == 10) initialAcceleration = acceleration;
                if (t == steps - 1) finalAcceleration = acceleration;

                // Интегрирование Эйлера
                double speedMs = (currentSpeedKmh / 3.6) + acceleration * dt;
                if (speedMs < 0) speedMs = 0;
                currentSpeedKmh = speedMs * 3.6;
                distanceM += speedMs * dt;
            }

            AssertCheck("Поезд 6200 т успешно стронулся с места и набрал скорость", currentSpeedKmh > 12.0, "> 12.0 км/ч", currentSpeedKmh.ToString("F2", CultureInfo.InvariantCulture) + " км/ч");
            AssertCheck("Пройденная дистанция за 60 с положительна", distanceM > 100.0, "> 100 м", distanceM.ToString("F1", CultureInfo.InvariantCulture) + " м");
            AssertCheck("Ускорение состава находится в допустимом диапазоне (0.02..0.18 м/с²)", (initialAcceleration >= 0.02 && initialAcceleration <= 0.18), "0.02..0.18 м/с²", initialAcceleration.ToString("F3", CultureInfo.InvariantCulture) + " м/с²");

            Console.WriteLine(string.Format("\nИтог: Проверок пройдено: {0}, Ошибок: {1}", totalPassed, totalFailed));

            // Сохранение итогового структурированного JSON
            string envResultDir = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");
            string targetDir = !string.IsNullOrEmpty(envResultDir) ? envResultDir : Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "..", "outputs"));
            if (!Directory.Exists(targetDir)) Directory.CreateDirectory(targetDir);
            string resultPath = Path.Combine(targetDir, "Trainer_modern_traction_validation.json");

            var jsonBuilder = new StringBuilder();
            jsonBuilder.AppendLine("{");
            jsonBuilder.AppendLine("  \"schema_version\": 1,");
            jsonBuilder.AppendLine("  \"passed\": " + (totalFailed == 0 ? "true" : "false") + ",");
            jsonBuilder.AppendLine("  \"status\": \"" + (totalFailed == 0 ? "passed" : "failed") + "\",");
            jsonBuilder.AppendLine("  \"total_passed\": " + totalPassed + ",");
            jsonBuilder.AppendLine("  \"total_failed\": " + totalFailed + ",");
            jsonBuilder.AppendLine("  \"timestamp_utc\": \"" + DateTime.UtcNow.ToString("O") + "\",");
            jsonBuilder.AppendLine("  \"locomotive\": \"2ES6 Sinara (2 sections, 8 axles, 200 t)\",");
            jsonBuilder.AppendLine("  \"train_mass_tonnes\": " + totalMassTonnes + ",");
            jsonBuilder.AppendLine("  \"hourly_mode_verification\": {");
            jsonBuilder.AppendLine("    \"speed_kmh\": 49.2,");
            jsonBuilder.AppendLine("    \"target_force_kn\": 464.0,");
            jsonBuilder.AppendLine("    \"calculated_force_kn\": " + resHourly.TractiveForceKN.ToString("F3", CultureInfo.InvariantCulture) + ",");
            jsonBuilder.AppendLine("    \"relative_error_percent\": " + (Math.Abs(resHourly.TractiveForceKN - 464.0) / 4.64).ToString("F2", CultureInfo.InvariantCulture));
            jsonBuilder.AppendLine("  },");
            jsonBuilder.AppendLine("  \"continuous_mode_verification\": {");
            jsonBuilder.AppendLine("    \"speed_kmh\": 51.0,");
            jsonBuilder.AppendLine("    \"target_force_kn\": 418.0,");
            jsonBuilder.AppendLine("    \"calculated_force_kn\": " + resContinuous.TractiveForceKN.ToString("F3", CultureInfo.InvariantCulture) + ",");
            jsonBuilder.AppendLine("    \"relative_error_percent\": " + (Math.Abs(resContinuous.TractiveForceKN - 418.0) / 4.18).ToString("F2", CultureInfo.InvariantCulture));
            jsonBuilder.AppendLine("  },");
            jsonBuilder.AppendLine("  \"checks\": [");
            for (int i = 0; i < checks.Count; i++) {
                var c = checks[i];
                jsonBuilder.Append("    { \"name\": \"" + c["name"] + "\", \"passed\": " + ((bool)c["passed"] ? "true" : "false") + ", \"actual\": \"" + c["actual"] + "\" }");
                if (i < checks.Count - 1) jsonBuilder.AppendLine(",");
                else jsonBuilder.AppendLine();
            }
            jsonBuilder.AppendLine("  ]");
            jsonBuilder.AppendLine("}");

            string jsonText = jsonBuilder.ToString();
            File.WriteAllText(resultPath, jsonText, Encoding.UTF8);
            Console.WriteLine("Отчет сохранен в: " + resultPath);

            string outputsDir = Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "..", "outputs"));
            if (Directory.Exists(outputsDir) && !string.Equals(targetDir.TrimEnd('\\', '/'), outputsDir.TrimEnd('\\', '/'), StringComparison.OrdinalIgnoreCase)) {
                try { File.WriteAllText(Path.Combine(outputsDir, "Trainer_modern_traction_validation.json"), jsonText, Encoding.UTF8); } catch {}
            }

            return totalFailed == 0 ? 0 : 1;
        } catch (Exception ex) {
            Console.WriteLine("Критическая ошибка выполнения пробы: " + ex);
            return 1;
        }
    }
}
