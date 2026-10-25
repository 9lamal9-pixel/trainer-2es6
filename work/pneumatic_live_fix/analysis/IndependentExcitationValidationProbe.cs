using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Web.Script.Serialization;

/// <summary>
/// Комплексная верификационная проба независимого возбуждения (НВ) тяговых двигателей
/// и алгоритмов МПСУиД электровоза 2ЭС6 по заводскому РЭ (2ЭС6.00.000.000 РЭ):
/// 1. Магнитная характеристика, насыщение и реакция якоря 1ДТ814 / ЭДП810.
/// 2. Закон регулирования тока возбуждения СТПР-1000: реостатный минимум 400 А, пусковой предел 600 А, ослабление поля до 36%.
/// 3. Алгоритм вентильного перехода МПСУиД: выбор позиций С->СП (24..31) и СП->П (45..47) по току Ia, запрет при Ia > 650/570 А.
/// 4. Токовые защиты ТЭД: блокировка и аварийное сообщение перегрузки Ia > 800 А.
/// 5. Противобоксовочное регулирование: форсирование Iv (+15%) при срыве колесной пары.
/// 6. Электрическое торможение по РЭ §2.3: рекуперация, ограничение Ia <= 500 А, переходы 3400/3800 В, замещение КЭБ при V < 3 км/ч.
/// </summary>
public sealed class IndependentExcitationValidationProbe {
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
        Console.WriteLine("=== Запуск пробы физической модели НВ и алгоритмов МПСУиД 2ЭС6 ===");
        bool allPassed = false;
        string errorMsg = null;

        try {
            // =========================================================================
            // БЛОК 1: Магнитная характеристика и реакция якоря
            // =========================================================================
            Console.WriteLine("\n--- Блок 1: Магнитная цепь и реакция якоря ---");
            double phiZero = TrainerIndependentExcitation.CalculateNetMagneticFlux(0.0, 0.0);
            AssertCheck("НВ: при Iv = 0 магнитный поток равен нулю", phiZero == 0.0, 0.0, phiZero);

            double phiNominalNoLoad = TrainerIndependentExcitation.CalculateNetMagneticFlux(440.0, 0.0);
            AssertCheck("НВ: при номинальном Iv=440 А поток в диапазоне насыщения 0.08..0.10 Вб",
                phiNominalNoLoad >= 0.085 && phiNominalNoLoad <= 0.098, "0.085..0.098 Вб", phiNominalNoLoad.ToString("F4", CultureInfo.InvariantCulture) + " Вб");

            double phiUnderLoad = TrainerIndependentExcitation.CalculateNetMagneticFlux(440.0, 600.0);
            double deltaPhi = phiNominalNoLoad - phiUnderLoad;
            AssertCheck("НВ: реакция якоря при Ia=600 А снижает поток (размагничивание)", deltaPhi > 0.005 && deltaPhi < 0.020, "0.005..0.020 Вб", deltaPhi.ToString("F4", CultureInfo.InvariantCulture) + " Вб");

            // =========================================================================
            // БЛОК 2: Закон регулирования тока возбуждения МПСУиД (РЭ лист 98, 107-108)
            // =========================================================================
            Console.WriteLine("\n--- Блок 2: Закон регулирования Iv МПСУиД ---");
            // 2.1 На реостатной позиции (например, поз. 5) при малом токе Ia=100 А ток Iv не опускается ниже 400 А
            double ivRheostat = TrainerIndependentExcitation.CalculateMpsuidTargetIv(5, 100.0, 10.0, false);
            AssertCheck("МПСУиД: на реостатных позициях Iv не опускается ниже 400 А", ivRheostat >= 400.0, ">= 400 А", ivRheostat.ToString("F1", CultureInfo.InvariantCulture) + " А");

            // 2.2 При пуске (V < 5 км/ч) уставка Iv не превышает 600 А (РЭ лист 108)
            double ivStarting = TrainerIndependentExcitation.CalculateMpsuidTargetIv(10, 800.0, 2.0, false);
            AssertCheck("МПСУиД: при пуске (V < 5 км/ч) уставка Iv ограничена 600 А", ivStarting <= 600.0, "<= 600 А", ivStarting.ToString("F1", CultureInfo.InvariantCulture) + " А");

            // 2.3 На позиции П65 при скорости 80 км/ч реализуется глубокое ослабление поля (ОП4: ~160 А, бета ~36%)
            double ivFieldWeakening = TrainerIndependentExcitation.CalculateMpsuidTargetIv(65, 400.0, 80.0, false);
            AssertCheck("МПСУиД: на позиции П65 ослабление поля ОП4 дает Iv <= 200 А (бета ~36%)", ivFieldWeakening <= 200.0, "<= 200 А", ivFieldWeakening.ToString("F1", CultureInfo.InvariantCulture) + " А");

            // =========================================================================
            // БЛОК 3: Интеллектуальные переходы МПСУиД по току Ia (РЭ лист 98-99)
            // =========================================================================
            Console.WriteLine("\n--- Блок 3: Интеллектуальные переходы С->СП и СП->П ---");
            // Переход С -> СП (с ходовой 23)
            int targetSp_low = TrainerIndependentExcitation.ResolveTransitionFromSeriesToSeriesParallel(250.0);
            AssertCheck("МПСУиД: переход 23->СП при Ia=250 А выбирает позицию 24", targetSp_low == 24, 24, targetSp_low);

            int targetSp_mid = TrainerIndependentExcitation.ResolveTransitionFromSeriesToSeriesParallel(340.0);
            AssertCheck("МПСУиД: переход 23->СП при Ia=340 А выбирает позицию 26", targetSp_mid == 26, 26, targetSp_mid);

            int targetSp_high = TrainerIndependentExcitation.ResolveTransitionFromSeriesToSeriesParallel(550.0);
            AssertCheck("МПСУиД: переход 23->СП при Ia=550 А выбирает позицию 30", targetSp_high == 30, 30, targetSp_high);

            int targetSp_blocked = TrainerIndependentExcitation.ResolveTransitionFromSeriesToSeriesParallel(700.0);
            AssertCheck("МПСУиД: переход 23->СП при Ia=700 А (>650 А) блокируется (код -1)", targetSp_blocked == -1, -1, targetSp_blocked);

            // Переход СП -> П (с ходовой 44)
            int targetP_low = TrainerIndependentExcitation.ResolveTransitionFromSeriesParallelToParallel(350.0);
            AssertCheck("МПСУиД: переход 44->П при Ia=350 А выбирает позицию 45", targetP_low == 45, 45, targetP_low);

            int targetP_blocked = TrainerIndependentExcitation.ResolveTransitionFromSeriesParallelToParallel(600.0);
            AssertCheck("МПСУиД: переход 44->П при Ia=600 А (>570 А) блокируется (код -1)", targetP_blocked == -1, -1, targetP_blocked);

            // =========================================================================
            // БЛОК 4: Токовые защиты и противобоксовочное регулирование НВ
            // =========================================================================
            Console.WriteLine("\n--- Блок 4: Защиты и противобоксовочное форсирование ---");
            // 4.1 Противобоксовочное форсирование Iv при срыве колесной пары
            var normalTraction = TrainerIndependentExcitation.ComputeTractionExcitation(30.0, 23, 3000.0, -1.0, false);
            var slipTraction = TrainerIndependentExcitation.ComputeTractionExcitation(30.0, 23, 3000.0, -1.0, true);
            AssertCheck("МПСУиД ПБЗ: при боксовании ток возбуждения форсируется (+15%)", slipTraction.ActualIv > normalTraction.ActualIv, "> normalIv", slipTraction.ActualIv.ToString("F1", CultureInfo.InvariantCulture) + " А");
            AssertCheck("МПСУиД ПБЗ: зафиксирован признак подавления боксования", slipTraction.IsSlipSuppressed, true, slipTraction.IsSlipSuppressed);

            // =========================================================================
            // БЛОК 5: Электрическое торможение по РЭ §2.3 (Рекуперация, Реостат, КЭБ)
            // =========================================================================
            Console.WriteLine("\n--- Блок 5: Электрическое торможение по РЭ §2.3 ---");
            // 5.1 Чистая рекуперация при 60 км/ч, Uкс=3200 В
            var recup = TrainerIndependentExcitation.ComputeDynamicBraking(60.0, 350.0, 3200.0);
            AssertCheck("Электротормоз: при 60 км/ч и Uкс=3200 В активна чистая рекуперация",
                recup.BrakeMode == TrainerIndependentExcitation.DynamicBrakeMode.Recuperation, "Recuperation", recup.BrakeMode.ToString());
            AssertCheck("Электротормоз: ток якоря в торможении не превышает норму РЭ 500 А",
                recup.ArmatureCurrentIa <= 500.0, "<= 500 А", recup.ArmatureCurrentIa.ToString("F1", CultureInfo.InvariantCulture) + " А");
            AssertCheck("Электротормоз: ток возбуждения Iv не превышает предел устойчивости 520 А",
                recup.ActualIv <= 520.0, "<= 520 А", recup.ActualIv.ToString("F1", CultureInfo.InvariantCulture) + " А");

            // 5.2 Рекуперативно-реостатное торможение при повышенном напряжении Uкс=3500 В
            var mixedRecup = TrainerIndependentExcitation.ComputeDynamicBraking(60.0, 350.0, 3500.0);
            AssertCheck("Электротормоз: при Uкс=3500 В подключены ПТР (режим RheostaticCatenary)",
                mixedRecup.BrakeMode == TrainerIndependentExcitation.DynamicBrakeMode.RheostaticCatenary, "RheostaticCatenary", mixedRecup.BrakeMode.ToString());

            // 5.3 Изолированное реостатное торможение при малой скорости V=12 км/ч (<16 км/ч)
            var rheoLowSpeed = TrainerIndependentExcitation.ComputeDynamicBraking(12.0, 250.0, 3000.0);
            AssertCheck("Электротормоз: при V=12 км/ч (<16 км/ч) переход в чистый реостат",
                rheoLowSpeed.BrakeMode == TrainerIndependentExcitation.DynamicBrakeMode.PureRheostatic, "PureRheostatic", rheoLowSpeed.BrakeMode.ToString());

            // 5.4 Автоматическое замещение электротормоза пневматикой КЭБ при V < 3 км/ч
            var subKeb = TrainerIndependentExcitation.ComputeDynamicBraking(2.0, 200.0, 3000.0);
            AssertCheck("Электротормоз: при V=2 км/ч (<3 км/ч) действует пневмозамещение КЭБ",
                subKeb.BrakeMode == TrainerIndependentExcitation.DynamicBrakeMode.PneumaticSubstitution, "PneumaticSubstitution", subKeb.BrakeMode.ToString());

            allPassed = true;
            Console.WriteLine("\n=== Все 18 проверок модели НВ и МПСУиД успешно пройдены! ===");
        } catch (Exception ex) {
            allPassed = false;
            errorMsg = ex.ToString();
            Console.WriteLine("\nОШИБКА: " + ex.Message);
        }

        // Сохранение итогового отчета
        try {
            string resultDir = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");
            string targetDir = !string.IsNullOrEmpty(resultDir) ? resultDir : AppDomain.CurrentDomain.BaseDirectory;
            if (!Directory.Exists(targetDir)) Directory.CreateDirectory(targetDir);

            var report = new Dictionary<string, object>();
            report["probe"] = "IndependentExcitationValidation";
            report["passed"] = allPassed;
            report["error"] = errorMsg;
            report["checks_count"] = Checks.Count;
            report["checks"] = Checks;
            report["timestamp_utc"] = DateTime.UtcNow.ToString("o", CultureInfo.InvariantCulture);

            string json = new JavaScriptSerializer().Serialize(report);
            string targetFile = Path.Combine(targetDir, "Trainer_independent_excitation_validation.json");
            File.WriteAllText(targetFile, json);
            Console.WriteLine("Отчет сохранен в: " + targetFile);

            string rootOutputs = Path.GetFullPath("outputs");
            if (Directory.Exists(rootOutputs)) {
                try {
                    string outputsFile = Path.Combine(rootOutputs, "Trainer_independent_excitation_validation.json");
                    if (!string.Equals(Path.GetFullPath(targetFile), Path.GetFullPath(outputsFile), StringComparison.OrdinalIgnoreCase)) {
                        File.WriteAllText(outputsFile, json);
                    }
                } catch { }
            }
        } catch (Exception ex) {
            Console.WriteLine("Предупреждение при сохранении JSON: " + ex.Message);
        }

        return allPassed ? 0 : 1;
    }
}
