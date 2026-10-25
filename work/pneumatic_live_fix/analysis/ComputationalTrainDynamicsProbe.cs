using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Couplers;
using RailwayDynamics.Simulation;
using RailwayDynamics.Traction;

/// <summary>
/// Комплексная верификационная проба мультифизической вычислительной динамики поезда:
/// 1. Модель сцепления О. Полаха (Wear, 2005): расчет сил крипа, двухзонная редукция (kA, kS), температурный спад трения.
/// 2. Нелинейный фрикционный поглощающий аппарат (TDEAS/Cole): гистерезис, запертая разгрузка (locked unloading), зазор 20 мм.
/// 3. Трибология тормозных колодок по ПТР (чугун стандартный, фосфористый P10, композит - формулы 56-60).
/// 4. Интегратор продольной динамики состава (LTD) с инерцией вращающихся масс и волновой задержкой ТМ.
/// </summary>
public sealed class ComputationalTrainDynamicsProbe {
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
        Console.WriteLine("=== Запуск пробы вычислительной динамики поезда (LTD, Polach 2005, TDEAS, ПТР) ===");
        bool allPassed = false;
        string errorMsg = null;

        try {
            // =========================================================================
            // БЛОК 1: Модель сцепления колесо-рельс О. Полаха (Polach 2005)
            // =========================================================================
            Console.WriteLine("\n--- Блок 1: Модель сцепления О. Полаха (2005) ---");
            const double wheelLoadQ = 122600.0; // 122.6 кН (нагрузка колеса 2ЭС6 при 25 т/ось)
            const double wheelRadius = 0.605;   // 605 мм (полуизношенный бандаж)
            const double semiAxisA = 0.0065;    // 6.5 мм (полуось эллипса контакта Герца)
            const double semiAxisB = 0.0055;    // 5.5 мм
            const double c11 = 4.12;            // Коэффициент Калкера
            const double c22 = 3.65;

            // 1.1 Нулевое скольжение (чистое качение): сила крипа должна быть строго 0
            double fx0, fy0;
            double omegaRoll = 20.0 / wheelRadius; // скорость 20 м/с
            PolachAdhesionSolver.ComputeCreepForces(wheelLoadQ, 20.0, omegaRoll, wheelRadius, 0.0, semiAxisA, semiAxisB, c11, c22, PolachParameters.DryRail, out fx0, out fy0);
            AssertCheck("Полах: при нулевом скольжении продольная сила крипа = 0", Math.Abs(fx0) < 1e-6, 0.0, fx0);

            // 1.2 Малый крип (режим микроскольжения s = 0.5%): тяговое усилие на сухом рельсе
            double fxSmall, fySmall;
            double omegaSmall = (20.0 * 1.005) / wheelRadius;
            PolachAdhesionSolver.ComputeCreepForces(wheelLoadQ, 20.0, omegaSmall, wheelRadius, 0.0, semiAxisA, semiAxisB, c11, c22, PolachParameters.DryRail, out fxSmall, out fySmall);
            AssertCheck("Полах: при микроскольжении 0.5% сила крипа положительна (> 30 кН)", fxSmall > 30000.0, "> 30 кН", fxSmall.ToString("F1", CultureInfo.InvariantCulture) + " Н");

            // 1.3 Большой крип (макроскольжение s = 5%): максимум кривой сцепления
            double fxOpt, fyOpt;
            double omegaOpt = (20.0 * 1.05) / wheelRadius;
            PolachAdhesionSolver.ComputeCreepForces(wheelLoadQ, 20.0, omegaOpt, wheelRadius, 0.0, semiAxisA, semiAxisB, c11, c22, PolachParameters.DryRail, out fxOpt, out fyOpt);
            AssertCheck("Полах: сила сцепления в оптимуме крипа достигает максимума (> 45 кН на колесо)", fxOpt > 45000.0, "> 45 кН", fxOpt.ToString("F1", CultureInfo.InvariantCulture) + " Н");

            // 1.4 Сравнение сухого и мокрого рельсов (WetRail mu0 = 0.30 vs DryRail mu0 = 0.55)
            double fxWet, fyWet;
            PolachAdhesionSolver.ComputeCreepForces(wheelLoadQ, 20.0, omegaOpt, wheelRadius, 0.0, semiAxisA, semiAxisB, c11, c22, PolachParameters.WetRail, out fxWet, out fyWet);
            AssertCheck("Полах: сцепление на мокром рельсе существенно ниже сухого", fxWet < fxOpt * 0.70, "< 70% от сухого", (fxWet / fxOpt).ToString("P1", CultureInfo.InvariantCulture));

            // 1.5 Температурный спад трения: при росте скорости скольжения сила падает (падающая ветвь)
            double fxSpin, fySpin;
            double omegaSpin = (20.0 * 1.30) / wheelRadius; // глубокое боксование s = 30%
            PolachAdhesionSolver.ComputeCreepForces(wheelLoadQ, 20.0, omegaSpin, wheelRadius, 0.0, semiAxisA, semiAxisB, c11, c22, PolachParameters.DryRail, out fxSpin, out fySpin);
            AssertCheck("Полах: при глубоком боксовании (s = 30%) сила сцепления падает из-за нагрева", fxSpin < fxOpt, "< fxOpt", fxSpin.ToString("F1", CultureInfo.InvariantCulture) + " Н");

            // =========================================================================
            // БЛОК 2: Нелинейный фрикционный поглощающий аппарат СА-3 (TDEAS)
            // =========================================================================
            Console.WriteLine("\n--- Блок 2: Фрикционный поглощающий аппарат СА-3 ---");
            var draftGear = FrictionDraftGearModel.CreateDefaultFreightDraftGear();
            var couplerState = new CouplerState();

            // 2.1 Свободный зазор автосцепки (+/- 10 мм = 20 мм общий зазор)
            double fSlack = draftGear.ComputeCouplerForce(0.008, 0.05, ref couplerState);
            AssertCheck("Аппарат: деформация внутри зазора (8 мм < 10 мм) дает нулевую реакцию", fSlack == 0.0, 0.0, fSlack);

            // 2.2 Нагружение аппарата (сжатие dx = 30 мм, dv > 0): верхняя ветвь гистерезиса
            double fLoad = draftGear.ComputeCouplerForce(0.030, 0.10, ref couplerState);
            AssertCheck("Аппарат: реакция при сжатии на 20 мм хода (dx=30 мм с зазором) > 200 кН", fLoad > 200000.0, "> 200 кН", fLoad.ToString("F1", CultureInfo.InvariantCulture) + " Н");

            // 2.3 Запертая разгрузка (Locked Unloading): при разгрузке (dx снижается до 26 мм, dv < 0) реакция падает с гистерезисом
            double fUnload = draftGear.ComputeCouplerForce(0.026, -0.10, ref couplerState);
            AssertCheck("Аппарат: при разгрузке (Locked Unloading) сила ниже силы нагружения (гистерезис)", fUnload < fLoad, "< fLoad", fUnload.ToString("F1", CultureInfo.InvariantCulture) + " Н");
            AssertCheck("Аппарат: сила разгрузки остается положительной (демпфирование)", fUnload > 0.0, "> 0", fUnload.ToString("F1", CultureInfo.InvariantCulture) + " Н");

            // 2.4 Сжатие до упора (предельный ход аппарата > 80 мм)
            double fLock = draftGear.ComputeCouplerForce(0.100, 0.05, ref couplerState);
            AssertCheck("Аппарат: при закрытии хода аппарата (> 80 мм) жесткость рамы дает силу > 2.5 МН", fLock > 2.5e6, "> 2.5 МН", fLock.ToString("F1", CultureInfo.InvariantCulture) + " Н");

            // =========================================================================
            // БЛОК 3: Трибология тормозных колодок по ПТР ВНИИЖТ
            // =========================================================================
            Console.WriteLine("\n--- Блок 3: Трибология тормозных колодок по ПТР ---");
            const double forcePerShoe = 15690.0; // ~1.6 тс на колодку (номинал 4-осного грузового вагона)

            // 3.1 Композиционные колодки (Формула 58 ПТР): высокий и стабильный коэффициент трения
            double phiComp0 = BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.Composite, 0.0, forcePerShoe);
            double phiComp60 = BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.Composite, 60.0 / 3.6, forcePerShoe);
            double phiComp100 = BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.Composite, 100.0 / 3.6, forcePerShoe);

            AssertCheck("Колодки композит: коэффициент трения при 0 км/ч ~0.35..0.44", phiComp0 >= 0.35 && phiComp0 <= 0.45, "0.35..0.45", phiComp0.ToString("F3", CultureInfo.InvariantCulture));
            AssertCheck("Колодки композит: стабильность трения при 60 км/ч (падение не более 25%)", phiComp60 >= phiComp0 * 0.75, ">= 75% от phi0", phiComp60.ToString("F3", CultureInfo.InvariantCulture));

            // 3.2 Чугунные стандартные колодки (Формула 56 ПТР): резкий рост трения перед остановкой
            double phiCast0 = BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.StandardCastIron, 0.0, forcePerShoe);
            double phiCast80 = BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.StandardCastIron, 80.0 / 3.6, forcePerShoe);

            AssertCheck("Колодки чугун: схватывание при остановке (трение при 0 км/ч в 2+ раза выше трения на 80 км/ч)", phiCast0 > 2.0 * phiCast80, "> 2 * phi80", (phiCast0 / phiCast80).ToString("F2", CultureInfo.InvariantCulture));

            // 3.3 Фосфористый чугун P10 (Формула 57 ПТР): трение выше стандартного чугуна на скорости
            double phiP10_80 = BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.PhosphorusCastIronP10, 80.0 / 3.6, forcePerShoe);
            AssertCheck("Колодки фосфористый чугун P10: трение на 80 км/ч выше стандартного чугуна", phiP10_80 > phiCast80, "> standardCast", phiP10_80.ToString("F3", CultureInfo.InvariantCulture));

            // =========================================================================
            // БЛОК 4: Интегратор продольной динамики состава (LTD)
            // =========================================================================
            Console.WriteLine("\n--- Блок 4: Интегратор продольной динамики поезда (LTD) ---");
            // Формируем состав: 1 электровоз 2ЭС6 (200 т, 34 м) + 5 грузовых вагонов (по 80 т, 14 м) = 6 экипажей, 600 т
            const int consistSize = 6;
            var vehicles = new VehicleDynamicsNode[consistSize];

            // 0: Электровоз 2ЭС6
            double currentX = 104.0;
            vehicles[0] = new VehicleDynamicsNode {
                MassKg = 200000.0,
                RotaryInertiaGamma = 0.15, // инерция якорей ТЭД и колес
                LengthM = 34.0,
                IsLocomotive = true,
                PositionX = currentX,
                VelocityV = 0.0,
                DistanceFromHeadM = 0.0,
                MaxCylinderPressurePa = 3.8e5,
                FillingTimeConstantSec = 3.5,
                CylinderPistonAreaM2 = 0.05,
                ReturnSpringForceN = 2000.0,
                RiggingRatioEta = 8.8,
                BrakeShoeCount = 32,
                ShoeType = BrakeShoeType.Composite
            };

            // 1..5: Грузовые вагоны (расположены строго с нулевым начальным натягом)
            for (int i = 1; i < consistSize; i++) {
                if (i == 1) {
                    currentX -= 0.5 * (vehicles[0].LengthM + 14.0); // 104.0 - 24.0 = 80.0
                } else {
                    currentX -= 14.0;
                }
                vehicles[i] = new VehicleDynamicsNode {
                    MassKg = 80000.0,
                    RotaryInertiaGamma = 0.05,
                    LengthM = 14.0,
                    IsLocomotive = false,
                    PositionX = currentX,
                    VelocityV = 0.0,
                    DistanceFromHeadM = 34.0 + (i - 1) * 14.0,
                    MaxCylinderPressurePa = 3.8e5,
                    FillingTimeConstantSec = 4.0,
                    CylinderPistonAreaM2 = 0.035,
                    ReturnSpringForceN = 1500.0,
                    RiggingRatioEta = 6.5,
                    BrakeShoeCount = 16,
                    ShoeType = BrakeShoeType.Composite
                };
            }

            var ltdSolver = new LongitudinalTrainDynamicsSolver(vehicles, FrictionDraftGearModel.CreateDefaultFreightDraftGear(), 280.0);

            // Фаза А: Трогание и разгон составом под силой тяги 400 кН
            ltdSolver.SetLocomotiveTraction(400000.0);
            const double dt = 0.05; // шаг 50 мс
            for (int step = 0; step < 100; step++) { // 5 секунд разгона
                ltdSolver.Step(dt, 0.0);
            }

            double headSpeedA = ltdSolver.GetVehicle(0).VelocityV * 3.6;
            double tailSpeedA = ltdSolver.GetVehicle(consistSize - 1).VelocityV * 3.6;
            double couplerForceA = ltdSolver.GetCouplerForce(0);

            AssertCheck("LTD разгон: головной электровоз набрал скорость (> 5 км/ч за 5 с)", headSpeedA > 5.0, "> 5 км/ч", headSpeedA.ToString("F2", CultureInfo.InvariantCulture) + " км/ч");
            AssertCheck("LTD разгон: хвостовой вагон тронулся и следует за локомотивом", tailSpeedA > 0.5, "> 0.5 км/ч", tailSpeedA.ToString("F2", CultureInfo.InvariantCulture) + " км/ч");
            AssertCheck("LTD разгон: первая автосцепка испытывает силу растяжения (отрицательная реакция)", couplerForceA < -50000.0, "< -50 кН", couplerForceA.ToString("F1", CultureInfo.InvariantCulture) + " Н");

            // Фаза Б: Торможение составом (полное служебное: разрядка ТМ на 150 кПа, тяга 0)
            ltdSolver.SetLocomotiveTraction(0.0);
            for (int step = 0; step < 200; step++) { // 10 секунд торможения
                ltdSolver.Step(dt, 150000.0);
            }

            double headSpeedB = ltdSolver.GetVehicle(0).VelocityV * 3.6;
            double headBrakeForce = ltdSolver.GetVehicle(0).BrakeForceN;
            double tailBrakeForce = ltdSolver.GetVehicle(consistSize - 1).BrakeForceN;

            AssertCheck("LTD торможение: тормозная сила головного локомотива развита (> 30 кН)", headBrakeForce > 30000.0, "> 30 кН", headBrakeForce.ToString("F1", CultureInfo.InvariantCulture) + " Н");
            AssertCheck("LTD торможение: тормозная сила хвостового вагона развита (> 15 кН)", tailBrakeForce > 15000.0, "> 15 кН", tailBrakeForce.ToString("F1", CultureInfo.InvariantCulture) + " Н");
            AssertCheck("LTD торможение: скорость поезда снизилась в результате действия тормозов", headSpeedB < headSpeedA, "< headSpeedA", headSpeedB.ToString("F2", CultureInfo.InvariantCulture) + " км/ч");

            allPassed = true;
            Console.WriteLine("\n=== Все 18 проверок вычислительной динамики поезда успешно пройдены! ===");
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
            report["probe"] = "ComputationalTrainDynamics";
            report["passed"] = allPassed;
            report["error"] = errorMsg;
            report["checks_count"] = Checks.Count;
            report["checks"] = Checks;
            report["timestamp_utc"] = DateTime.UtcNow.ToString("o", CultureInfo.InvariantCulture);

            string json = new JavaScriptSerializer().Serialize(report);
            string targetFile = Path.Combine(targetDir, "Trainer_computational_train_dynamics.json");
            File.WriteAllText(targetFile, json);
            Console.WriteLine("Отчет сохранен в: " + targetFile);

            string rootOutputs = Path.GetFullPath("outputs");
            if (Directory.Exists(rootOutputs)) {
                try {
                    string outputsFile = Path.Combine(rootOutputs, "Trainer_computational_train_dynamics.json");
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
