using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Text;

namespace Trainer2TE116
{
    public sealed class JournalInput
    {
        public double T;
        public double Dt;
        public double X = double.NaN;
        public double V;
        public double VSigned;
        public double Ur;
        public double Tm;
        public double LocoTc;
        public double TailTc;
        public double Charge = 0.49;
        public double TractionKn;
        public double MeanSlope;
        public double AllowedKmh = -1;
        public double AmbientC = 20;
        public double MassT;
        public double CouplerKn;
        public int CouplerAt;
        public int Crane = 2;
        public int Controller;
        public int Reverser = 1;
        public int Axles;
        public bool Sanding;
        public bool Empty;
        public bool EpkTripped;
        public bool BrakeTestActive;
        public int[] Aspects;
    }

    public sealed class JournalEntry
    {
        public double T;
        public double X;
        public double V;
        public string Code = "";
        public string Title = "";
        public string Detail = "";
        public string Basis = "";
        public string Place = "";
        public int Points;
        public bool Gross;

        public string Clock
        {
            get
            {
                int sec = Math.Max(0, (int)Math.Round(T));
                return string.Format(CultureInfo.InvariantCulture, "{0:00}:{1:00}:{2:00}", sec / 3600, (sec / 60) % 60, sec % 60);
            }
        }

        public override string ToString()
        {
            return Clock + "  " + Code + "  " + Title + " (" + Points.ToString(CultureInfo.InvariantCulture) + " балл.)";
        }
    }

    /// <summary>
    /// Автоматическая расшифровка параметров движения и управления тормозами в соответствии с:
    /// - «Правилами технического обслуживания тормозного оборудования и управления тормозами
    ///   железнодорожного подвижного состава» (утв. СЖТ СНГ, протокол № 60 / Приказ Минтранса РФ № 151,
    ///   в актуальной редакции Протокола 83-го заседания СЖТ СНГ от 25–26.11.2025 г. № 83, Раздел IX и Приложение № 3);
    /// - Распоряжением ОАО «РЖД» № 2849р (классификатор нарушений журнала формы ТУ-133).
    /// </summary>
    public static class Journal
    {
        private static readonly CultureInfo C = CultureInfo.InvariantCulture;
        public static readonly List<JournalEntry> Entries = new List<JournalEntry>();
        public static bool Enabled = true;
        public static Action<string> Logged;

        private static string dir;
        private static string path;
        private static int pointsGross = 20;
        private static int pointsOther = 5;
        private static readonly Dictionary<string, int> pointsByCode = new Dictionary<string, int>();

        private static double tolKmh = 1.0;
        private static double stageHoldS = 150.0;
        private static double calcSpeed = 24.7;
        private static double calcForce = 500.0;
        private static double criticalShare = 0.9;
        private static double locoMassT = 276.0;

        public static double WeightNormT;
        public static double RulingGrade;
        public static bool CriticalTrain;
        public static Route Route;

        private static bool brk;
        private static double refUr;
        private static double minUr;
        private static double depth;
        private static double tBrake;
        private static double lapSince = -1.0;
        private static double lapUr;
        private static double stageUr;
        private static double stageSince;
        private static double sandUntil = -1000000000.0;
        private static double lastT;
        private static int prevCrane = 2;
        private static int stages;
        private static bool firstChecked;
        private static bool emergency;
        private static bool holdReported;
        private static bool sandChecked;
        private static bool lapUsed;
        private static bool directRelease;
        private static bool iiiReported;

        private static bool relWatch;
        private static bool relMoving;
        private static bool relFromEmergency;
        private static bool relStopped;
        private static double relStart;
        private static double relUrMax;
        private static double relInIi = -1.0;
        private static double relDepth;
        private static bool tractionAfterRelChecked;

        private static bool moving;
        private static double vMaxRun;
        private static double lastBrakeX = double.NaN;
        private static bool stoppedWithBrakes;
        private static bool stoppedEmergency;
        private static bool stoppedFull;
        private static bool stopReleaseSeen;
        private static double stopRelT = -1.0;
        private static bool tractionPrev;

        private static bool over;
        private static double overStartX;
        private static double overStartT;
        private static double overMax;
        private static double overAllowed;
        private static double lastX = double.NaN;
        private static double rollDist;
        private static bool rollReported;
        private static double lastEmergencyT = -1000000000.0;
        private static int climbReported = -1;
        private static int climbStopReported = -1;
        private static double belowCalcT;

        public static readonly string[] Codes = new string[20]
        {
            "S1", "S2", "S3", "B1", "B2", "B3", "B4", "B5", "B6", "B7",
            "B8", "B9", "B10", "B11", "B12", "B13", "B14", "B15", "R1", "R2"
        };

        public static double CouplerLimitKn = 1275.0;
        private static double lastCouplerT = -1000000000.0;
        private static double stageUrHold;
        private static double tmMin = 1.0;
        private static double iiiSince;
        private static bool overDischargeReported;

        public static int Points
        {
            get
            {
                int sum = 0;
                lock (Entries)
                {
                    foreach (JournalEntry e in Entries)
                    {
                        sum += e.Points;
                    }
                }
                return sum;
            }
        }

        public static double CalcSpeed { get { return calcSpeed; } }

        public static void Configure(string logDir, int gross = 20, int other = 5, double calcSpeedKmh = 24.7, double calcForceKn = 500)
        {
            Enabled = true;
            pointsGross = gross;
            pointsOther = other;
            calcSpeed = calcSpeedKmh;
            calcForce = calcForceKn;
            dir = logDir;
            Reset();
        }

        public static void Reset()
        {
            lastCouplerT = -1000000000.0;
            lock (Entries)
            {
                Entries.Clear();
            }
            path = null;
            Route = null;
            CriticalTrain = false;
            WeightNormT = 0.0;
            RulingGrade = 0.0;
            brk = false;
            prevCrane = 2;
            lapSince = -1.0;
            relWatch = false;
            moving = false;
            vMaxRun = 0.0;
            lastBrakeX = double.NaN;
            stopReleaseSeen = (stoppedFull = (stoppedEmergency = (stoppedWithBrakes = false)));
            stopRelT = -1.0;
            tractionPrev = false;
            over = false;
            lastX = double.NaN;
            rollDist = 0.0;
            rollReported = false;
            lastEmergencyT = -1000000000.0;
            climbStopReported = (climbReported = -1);
            belowCalcT = 0.0;
            sandUntil = -1000000000.0;
            lastT = 0.0;
        }

        public static void SetRoute(Route route, double totalMassT, int wagonAxles)
        {
            Route = route;
            RulingGrade = (route != null) ? route.RulingGrade : 0.0;
            CriticalTrain = false;
            WeightNormT = 0.0;

            double trainMassT = totalMassT - locoMassT;
            if (route == null || RulingGrade < 2.0 || trainMassT <= 0.0 || wagonAxles <= 0)
            {
                return;
            }

            // Simulation passes the wagon axle count, including non-four-axle consists.
            double axleLoad = trainMassT / wagonAxles;
            double v = calcSpeed;
            double w0Loco = 1.9 + 0.01 * v + 0.0003 * v * v;
            double w0Cars = (axleLoad >= 6.0)
                ? (0.7 + (3.0 + 0.1 * v + 0.0025 * v * v) / axleLoad)
                : (1.0 + 0.044 * v + 0.00024 * v * v);

            const double g = 0.00981;
            WeightNormT = (calcForce - locoMassT * (w0Loco + RulingGrade) * g) / ((w0Cars + RulingGrade) * g);
            CriticalTrain = (WeightNormT > 0.0) && (trainMassT >= criticalShare * WeightNormT);
        }

        public static void Feed(JournalInput inp)
        {
            if (!Enabled) return;

            double vAbs = Math.Abs(inp.V);

            // Контроль продольной силы в автосцепке (ПТР)
            if (Math.Abs(inp.CouplerKn) > CouplerLimitKn && inp.T - lastCouplerT > 60.0)
            {
                lastCouplerT = inp.T;
                Add(inp, "D1", false,
                    string.Format(C, "Продольная сила в автосцепке {0:0} кН ({1}) за {2}-й единицей поезда",
                        Math.Abs(inp.CouplerKn), (inp.CouplerKn > 0.0) ? "растяжение" : "сжатие", inp.CouplerAt + 1),
                    string.Format(C, "наибольшая сила по прочности автосцепки {0:0} кН (130 тс)", CouplerLimitKn),
                    "ПТР: наибольшая продольная сила по прочности автосцепки", double.NaN, double.NaN);
            }

            bool hasTraction = inp.TractionKn > 5.0;
            int crane = inp.Crane;
            bool inBrakingPos = (crane == 5 || crane == 6 || crane == 7);

            if (inp.Sanding)
            {
                sandUntil = inp.T + 10.0;
            }
            if (crane == 6 || inp.EpkTripped)
            {
                lastEmergencyT = inp.T;
            }

            // Начало цикла торможения
            if (!brk && (inBrakingPos || (inp.Ur < inp.Charge - 0.03 && crane != 1 && crane != 2)))
            {
                brk = true;
                refUr = inBrakingPos ? inp.Ur : Math.Max(inp.Ur, inp.Charge);
                tmMin = inp.Tm;
                minUr = inp.Ur;
                tBrake = inp.T;
                stages = 0;
                iiiReported = (directRelease = (lapUsed = (sandChecked = (holdReported = (emergency = (firstChecked = false))))));
                lapSince = -1.0;
                stageUr = inp.Ur;
                stageSince = inp.T;
                relWatch = false;
                if (!double.IsNaN(inp.X))
                {
                    lastBrakeX = inp.X;
                }
            }

            if (brk)
            {
                minUr = Math.Min(minUr, inp.Ur);
                depth = refUr - minUr;
                tmMin = Math.Min(tmMin, inp.Tm);

                if (crane == 6 || inp.EpkTripped)
                {
                    emergency = true;
                }
                if (!double.IsNaN(inp.X))
                {
                    lastBrakeX = inp.X;
                }

                if (crane == 4 && prevCrane != 4)
                {
                    lapSince = inp.T;
                    lapUr = inp.Ur;
                }

                // Проверка первой ступени служебного торможения (Раздел IX / Прил. № 3 Правил в ред. Протокола № 83)
                if (crane == 4 && lapSince >= 0.0 && inp.T - lapSince >= 2.0 && !emergency)
                {
                    lapUsed = true;
                    if (!firstChecked)
                    {
                        firstChecked = true;
                        stages = 1;
                        CheckFirstStage(inp, refUr - inp.Ur);
                    }
                }

                // Проверка минимального времени выдержки ручки КМ в IV положении между ступенями и перед отпуском
                if (prevCrane == 4 && crane != 4 && crane != 3 && crane != 6 && lapSince >= 0.0 && !emergency)
                {
                    double minHoldS = (inp.Axles > 350) ? 8.0 : 6.0;
                    double heldS = inp.T - lapSince;
                    if (heldS < minHoldS - 0.3 && depth > 0.02)
                    {
                        Add(inp, "B3", false,
                            string.Format(C, "{0} через {1:0.0} с после перевода в IV положение",
                                (crane == 1 || crane == 2) ? "Отпуск" : "Следующая ступень", heldS),
                            string.Format(C, "нужно не менее {0:0} с для поезда {1} осей", minHoldS, inp.Axles),
                            "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                            double.NaN, double.NaN);
                    }
                    if (crane == 5 || crane == 7)
                    {
                        stageUr = inp.Ur;
                    }
                    lapSince = -1.0;
                }

                if (crane == 4 && prevCrane != 4 && firstChecked && !emergency)
                {
                    stages++;
                }

                // Проверка величины второй и последующих ступеней торможения (0,03–0,08 МПа / 0,3–0,8 кгс/см²)
                if (crane == 4 && lapSince >= 0.0 && inp.T - lapSince >= 2.0 && stages >= 2 && !emergency && stageUr > 0.0)
                {
                    double stepMPa = stageUr - inp.Ur;
                    if (stepMPa > 0.005 && (stepMPa < 0.025 || stepMPa > 0.085) && (refUr - inp.Ur) < 0.14)
                    {
                        Add(inp, "B2", false,
                            string.Format(C, "Ступень торможения {0:0.00} кгс/см²", stepMPa * 10.1971617),
                            "последующие ступени: 0,3–0,8 кгс/см²",
                            "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                            double.NaN, double.NaN);
                    }
                    stageUr = 0.0;
                }

                // Проверка применения III положения (перекрыша без питания) при служебном торможении
                if (crane == 3 && !emergency && !iiiReported && depth > 0.02)
                {
                    if (prevCrane != 3)
                    {
                        iiiSince = inp.T;
                    }
                    else if (inp.T - iiiSince > 3.0)
                    {
                        iiiReported = true;
                        Add(inp, "B4", false,
                            "Служебное торможение с перекрышей без питания (III положение)",
                            "все служебные торможения выполняются с переводом ручки в IV положение «Перекрыша с питанием»",
                            "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                            double.NaN, double.NaN);
                    }
                }

                if (Math.Abs(inp.Ur - stageUrHold) > 0.01)
                {
                    stageUrHold = inp.Ur;
                    stageSince = inp.T;
                }

                // Запрет следования с постоянной ступенью торможения без снижения скорости/увеличения разрядки
                if (vAbs > 1.0 && !holdReported && !emergency && depth >= 0.03 && inp.T - stageSince > stageHoldS)
                {
                    holdReported = true;
                    Add(inp, "B5", false,
                        string.Format(C, "Следование с постоянной ступенью {0:0.00} кгс/см² дольше {1:0} с",
                            depth * 10.1971617, stageHoldS),
                        "увеличить разрядку на следующую ступень, а после снижения скорости отпустить тормоза",
                        "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                        double.NaN, double.NaN);
                }

                // Подача песка при разрядке УР более 1,0 кгс/см²
                if (!sandChecked && depth > 0.1)
                {
                    sandChecked = true;
                    bool sandOk = (sandUntil >= tBrake - 0.5) || inp.Sanding;
                    if (!sandOk && vAbs > 10.0 && !emergency)
                    {
                        Add(inp, "B6", false,
                            "Разрядка УР более 1,0 кгс/см² без подачи песка",
                            "перед торможением с разрядкой УР более 1,0 кгс/см² привести в действие песочницы",
                            "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                            double.NaN, double.NaN);
                    }
                }

                // Завышение разрядки при полном служебном торможении (более 1,5–1,7 кгс/см²)
                if (!emergency && crane == 4 && lapSince >= 0.0 && inp.T - lapSince >= 2.0 && depth > 0.175 && !overDischargeReported)
                {
                    overDischargeReported = true;
                    Add(inp, "B7", false,
                        string.Format(C, "Разрядка УР служебным торможением на {0:0.00} кгс/см²", depth * 10.1971617),
                        "полное служебное торможение — разрядка 1,5–1,7 кгс/см²",
                        "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                        double.NaN, double.NaN);
                }

                // Переход к отпуску: засчитываем полный отпуск только тогда, когда давление в УР
                // действительно восстановилось до (Charge - 0.015 МПа), чтобы кратковременный импульс в I положение
                // без фактического отпуска не вызывал ложную ошибку B9 («завышение УР на 0.00 кгс/см²»)!
                bool releaseStarted = (crane == 1 || crane == 2) && (inp.Ur > minUr + 0.015) && (inp.Ur >= inp.Charge - 0.015);

                if ((crane == 1 || crane == 2) && (prevCrane == 5 || prevCrane == 7) && !emergency && depth < 0.14 && !directRelease)
                {
                    directRelease = true;
                    Add(inp, "B4", false,
                        "Отпуск непосредственно из положения V без выдержки в IV положении",
                        "служебное торможение завершать переводом ручки в IV положение перед отпуском",
                        "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                        double.NaN, double.NaN);
                }

                if (releaseStarted)
                {
                    if (tmMin < 0.34 && vAbs > 1.0 && inp.MeanSlope <= -2.0 && !emergency && inp.T - lastEmergencyT > 120.0)
                    {
                        Add(inp, "B12", false,
                            string.Format(C, "Отпуск на ходу по спуску {0:0.0} ‰ после снижения давления в ТМ до {1:0.00} кгс/см²",
                                inp.MeanSlope, tmMin * 10.1971617),
                            "при снижении давления в ТМ ниже 3,5 кгс/см² на спуске — остановить поезд, затормозить локомотив, отпустить и зарядить ТМ на стоянке",
                            "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                            double.NaN, double.NaN);
                    }

                    if (inp.Axles > 300 && vAbs > 0.5 && vAbs < 20.0 && !emergency)
                    {
                        Add(inp, "B8", false,
                            string.Format(C, "Отпуск автотормозов на скорости {0:0} км/ч до остановки", vAbs),
                            string.Format(C, "поезд {0} осей (> 300 осей): не отпускать автотормоза при скорости менее 20 км/ч до полной остановки", inp.Axles),
                            "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                            double.NaN, double.NaN);
                    }

                    brk = false;
                    relWatch = true;
                    relStart = inp.T;
                    relUrMax = inp.Ur;
                    relInIi = -1.0;
                    relFromEmergency = emergency;
                    relDepth = depth;
                    relMoving = (vAbs > 0.5);
                    relStopped = !relMoving;
                    tractionAfterRelChecked = false;
                    overDischargeReported = false;
                }
            }

            prevCrane = crane;

            // Контроль завышения давления в УР в I положении при отпуске (Rule B9)
            // с учётом нормативов Протокола № 83 и защиты плотной ТМ от перезарядки ВР № 483
            if (relWatch)
            {
                relUrMax = Math.Max(relUrMax, inp.Ur);
                if (crane == 2)
                {
                    if (relInIi < 0.0) relInIi = inp.T;
                }
                else
                {
                    relInIi = -1.0;
                }

                bool checkDone = brk || (relInIi >= 0.0 && inp.T - relInIi > 20.0) || (inp.T - relStart > 240.0);
                if (checkDone)
                {
                    relWatch = false;
                    if (inp.Axles > 100 && relDepth >= 0.03)
                    {
                        // Правила ТО тормозов (Протокол № 83), п. 10.3.1 м, н, с. 108:
                        // для поездов свыше 100 осей после служебного торможения +0,02–0,06 МПа,
                        // после экстренного +0,10–0,12 МПа. Граница 350 осей к этим диапазонам не относится.
                        double minOverMPa = relFromEmergency ? 0.10 : 0.02;
                        double maxOverMPa = relFromEmergency ? 0.12 : 0.06;
                        double overMPa = relUrMax - inp.Charge;

                        if (overMPa < minOverMPa - 0.008)
                        {
                            Add(inp, "B9", false,
                                string.Format(C, "Отпуск с завышением УР всего на {0:0.00} кгс/см² над зарядным",
                                    Math.Max(0.0, overMPa) * 10.1971617),
                                string.Format(C, "поезд {0} осей: норма {1:0.0#}–{2:0.0#} кгс/см² выше зарядного{3}",
                                    inp.Axles, minOverMPa * 10.0, maxOverMPa * 10.0,
                                    relFromEmergency ? " после экстренного торможения" : ""),
                                relFromEmergency ? "Правила ТО тормозов (Протокол № 83), п. 10.3.1 н" : "Правила ТО тормозов (Протокол № 83), п. 10.3.1 м",
                                double.NaN, double.NaN);
                        }
                        else if (overMPa > maxOverMPa + 0.015)
                        {
                            Add(inp, "B9", false,
                                string.Format(C, "Чрезмерное завышение УР при отпуске на {0:0.00} кгс/см² над зарядным (риск перезарядки РК ВР № 483)",
                                    overMPa * 10.1971617),
                                string.Format(C, "норма {0:0.0#}–{1:0.0#} кгс/см² выше зарядного",
                                    minOverMPa * 10.0, maxOverMPa * 10.0),
                                relFromEmergency ? "Правила ТО тормозов (Протокол № 83), п. 10.3.1 н" : "Правила ТО тормозов (Протокол № 83), п. 10.3.1 м",
                                double.NaN, double.NaN);
                        }
                    }
                }
            }

            // Контроль времени выдержки до набора тяги после отпуска на ходу (Rule B10)
            if (relMoving && !tractionAfterRelChecked && hasTraction && !tractionPrev && vAbs > 0.5)
            {
                tractionAfterRelChecked = true;
                double dtRel = inp.T - relStart;
                if (relDepth >= 0.03 && dtRel < 60.0)
                {
                    Add(inp, "B10", false,
                        string.Format(C, "Включение тяги через {0:0} с после начала отпуска автотормозов", dtRel),
                        "в грузовом поезде — не ранее полного отпуска (не менее 60 с после перевода ручки КМ в I положение)",
                        "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                        double.NaN, double.NaN);
                }
                else if (relDepth >= 0.03 && inp.TailTc > 0.03)
                {
                    Add(inp, "B10", false,
                        string.Format(C, "Включение тяги при неотпущенных тормозах хвостовых вагонов (ТЦ хвоста {0:0.00} кгс/см²)",
                            inp.TailTc * 10.1971617),
                        "выдержать время полного отпуска хвостовых вагонов согласно таблице/справке ВУ-45",
                        "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                        double.NaN, double.NaN);
                }
            }

            if (relMoving && vAbs < 0.3)
            {
                relMoving = false;
            }

            if (vAbs >= 3.0)
            {
                if (!moving)
                {
                    moving = true;
                    vMaxRun = 0.0;
                }
                vMaxRun = Math.Max(vMaxRun, vAbs);
            }

            // Контроль остановки поезда
            if (moving && vAbs < 0.3)
            {
                moving = false;
                bool usedAutoBrakes = brk
                    || (!double.IsNaN(lastBrakeX) && !double.IsNaN(inp.X) && inp.X - lastBrakeX < 600.0)
                    || (inp.Tm < inp.Charge - 0.08);

                if (vMaxRun >= 10.0 && !usedAutoBrakes)
                {
                    Add(inp, "B13", false,
                        "Остановка поезда без применения автоматических тормозов",
                        "остановка грузового поезда выполняется с применением автоматических тормозов",
                        "Распоряжение ОАО «РЖД» № 2849р, журнал ТУ-133 № 2А",
                        double.NaN, double.NaN);
                }

                stoppedWithBrakes = (brk && depth >= 0.03) || (emergency && brk);
                stoppedEmergency = brk && emergency;
                stoppedFull = brk && (depth >= 0.14);
                stopReleaseSeen = false;
                stopRelT = -1.0;
                CheckClimbStop(inp);
            }

            // Контроль времени отпуска и зарядки на стоянке перед троганием (Rule B11)
            if (!moving && vAbs < 0.5)
            {
                if (stoppedWithBrakes && !stopReleaseSeen && (crane == 1 || crane == 2) && !brk)
                {
                    stopReleaseSeen = true;
                    stopRelT = relStart;
                }

                if (stoppedWithBrakes && hasTraction && !tractionPrev)
                {
                    if (!stopReleaseSeen)
                    {
                        Add(inp, "B11", false,
                            "Включение тяги при заторможенном автотормозами поезде",
                            "после остановки — произвести отпуск автотормозов и выдержать нормативное время зарядки",
                            "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                            double.NaN, double.NaN);
                    }
                    else
                    {
                        double waitNormS = stoppedEmergency
                            ? ((inp.Axles > 100) ? 360.0 : 240.0)
                            : (stoppedFull ? 120.0 : 90.0);
                        if (inp.AmbientC < 0.0)
                        {
                            waitNormS *= 1.5;
                        }

                        double waitedS = inp.T - stopRelT;
                        if (waitedS < waitNormS)
                        {
                            string kindStr = stoppedEmergency ? "экстренного торможения" : (stoppedFull ? "полного служебного торможения" : "ступени торможения");
                            Add(inp, "B11", false,
                                string.Format(C, "Тяга через {0:0} с после отпуска на стоянке", waitedS),
                                string.Format(C, "после {0} — выдержка не менее {1:0} с{2}",
                                    kindStr, waitNormS, (inp.AmbientC < 0.0) ? " (при морозе ниже 0 °C: ×1,5)" : ""),
                                "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                                double.NaN, double.NaN);
                        }
                    }
                    stoppedWithBrakes = false;
                }
            }

            tractionPrev = hasTraction;

            // Контроль допустимой скорости движения (Rule S1)
            double limitKmh = (inp.AllowedKmh > 0.0)
                ? inp.AllowedKmh
                : ((Route != null && !double.IsNaN(inp.X)) ? (double)Route.StaticLimitAt(inp.X) : (-1.0));

            if (limitKmh > 0.0)
            {
                if (!over && vAbs > limitKmh + tolKmh)
                {
                    over = true;
                    overStartX = inp.X;
                    overStartT = inp.T;
                    overMax = vAbs - limitKmh;
                    overAllowed = limitKmh;
                }
                else if (over)
                {
                    overMax = Math.Max(overMax, vAbs - limitKmh);
                    overAllowed = Math.Min(overAllowed, limitKmh);
                    if (vAbs <= limitKmh)
                    {
                        over = false;
                        double distM = (!double.IsNaN(inp.X)) ? (inp.X - overStartX) : 0.0;
                        Add(inp, "S1", true,
                            string.Format(C, "Превышение допустимой скорости на {0:0} км/ч", overMax),
                            string.Format(C, "допустимая {0:0} км/ч, на протяжении {1:0} м и {2:0} с, начало {3}",
                                overAllowed, distM, inp.T - overStartT, Place(overStartX)),
                            "Распоряжение ОАО «РЖД» № 2849р, журнал ТУ-133 № 2",
                            overStartX, overStartT);
                    }
                }
            }

            // Контроль проезда запрещающего светофора (Rule S2)
            if (Route != null && inp.Aspects != null && !double.IsNaN(inp.X) && !double.IsNaN(lastX) && inp.X > lastX)
            {
                for (int i = 0; i < Route.Lights.Count && i < inp.Aspects.Length; i++)
                {
                    double lx = Route.Lights[i].X;
                    if (lastX < lx && inp.X >= lx && Trainer2TE116.Route.IsProhibiting(inp.Aspects[i]))
                    {
                        Add(inp, "S2", true,
                            "Проезд светофора " + Route.Lights[i].Name + " с запрещающим показанием",
                            Trainer2TE116.Route.AspectName(inp.Aspects[i]) + string.Format(C, ", скорость {0:0} км/ч", vAbs),
                            "ПТЭ РФ; Распоряжение ОАО «РЖД» № 2849р, журнал ТУ-133 № 2",
                            double.NaN, double.NaN);
                    }
                }
            }

            // Контроль самопроизвольного скатывания против направления реверсора (Rule S3)
            int revDir = (inp.Reverser == 2) ? (-1) : 1;
            if (inp.VSigned * revDir < -0.3)
            {
                rollDist += Math.Abs(inp.VSigned) / 3.6 * inp.Dt;
                if (rollDist > 1.0 && !rollReported)
                {
                    rollReported = true;
                    Add(inp, "S3", false,
                        string.Format(C, "Скатывание поезда против установленного направления движения ({0:0.0} км/ч)", Math.Abs(inp.VSigned)),
                        "",
                        "Распоряжение ОАО «РЖД» № 2849р, журнал ТУ-133 № 2А",
                        double.NaN, double.NaN);
                }
            }
            else if (inp.VSigned * revDir > 0.5 || vAbs < 0.05)
            {
                rollDist = 0.0;
                if (vAbs > 3.0)
                {
                    rollReported = false;
                }
            }

            // Контроль скорости следования на лимитирующем подъёме (Rule R1)
            if (CriticalTrain && Route != null && !double.IsNaN(inp.X))
            {
                int climbIdx = Route.LimitingClimbAt(inp.X);
                if (climbIdx >= 0 && vAbs > 0.5 && vAbs < calcSpeed - 0.5)
                {
                    belowCalcT += inp.Dt;
                }
                else
                {
                    belowCalcT = 0.0;
                }

                if (climbIdx >= 0 && belowCalcT > 10.0 && climbReported != climbIdx)
                {
                    climbReported = climbIdx;
                    Add(inp, "R1", false,
                        string.Format(C, "Скорость {0:0} км/ч на лимитирующем подъёме {1:+0.0} ‰ ниже расчётной {2:0.0} км/ч",
                            vAbs, Route.LimitSlope[climbIdx], calcSpeed),
                        string.Format(C, "поезд {0:0} т при весовой норме {1:0} т", inp.MassT - locoMassT, WeightNormT),
                        "Распоряжение ОАО «РЖД» № 2849р, журнал ТУ-133 № 4",
                        double.NaN, double.NaN);
                }
            }

            lastX = inp.X;
            lastT = inp.T;
        }

        private static void CheckFirstStage(JournalInput inp, double dropMPa)
        {
            if (inp.BrakeTestActive || dropMPa >= 0.14) return;

            double minStage = inp.Empty ? 0.04 : 0.05;
            double maxStage = inp.Empty ? 0.06 : 0.08;
            if (inp.MeanSlope <= 0.0 && inp.MeanSlope >= -8.0)
            {
                minStage = 0.03;
            }

            if (dropMPa < minStage - 0.005 || dropMPa > maxStage + 0.005)
            {
                Add(inp, "B1", false,
                    string.Format(C, "Первая ступень торможения {0:0.00} кгс/см²", dropMPa * 10.1971617),
                    string.Format(C, "{0} поезд: норма {1:0.0#}–{2:0.0#} кгс/см²",
                        inp.Empty ? "порожний" : "гружёный", minStage * 10.0, maxStage * 10.0),
                    "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                    double.NaN, double.NaN);
            }
        }

        private static void CheckClimbStop(JournalInput inp)
        {
            if (!CriticalTrain || Route == null || double.IsNaN(inp.X)) return;
            int climbIdx = Route.LimitingClimbAt(inp.X);
            if (climbIdx < 0 || climbStopReported == climbIdx) return;

            climbStopReported = climbIdx;
            Add(inp, "R2", false,
                string.Format(C, "Остановка поезда на лимитирующем подъёме {0:+0.0} ‰", Route.LimitSlope[climbIdx]),
                string.Format(C, "поезд {0:0} т при весовой норме {1:0} т", inp.MassT - locoMassT, WeightNormT),
                "Распоряжение ОАО «РЖД» № 2849р, журнал ТУ-133 № 4",
                double.NaN, double.NaN);
        }

        public static void AddBrakeTest(double t, double x, double v, bool skipped, string detail)
        {
            if (!Enabled) return;
            JournalEntry entry = new JournalEntry
            {
                T = t,
                X = x,
                V = v,
                Code = skipped ? "B14" : "B15",
                Gross = skipped,
                Title = skipped ? "Проба тормозов в пути следования не выполнена" : "Проба тормозов в пути следования: неудовлетворительно или с нарушением",
                Detail = detail,
                Basis = skipped
                    ? "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3; № 2849р, ТУ-133 № 2"
                    : "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3"
            };
            Commit(entry);
        }

        public static void AddExternal(string code, bool gross, string title, string detail, string basis, double t, double x, double v)
        {
            if (!Enabled) return;
            JournalEntry entry = new JournalEntry
            {
                T = t,
                X = x,
                V = v,
                Code = code,
                Gross = gross,
                Title = title,
                Detail = detail,
                Basis = basis
            };
            Commit(entry);
        }

        public static string Place(double x)
        {
            if (Route == null || double.IsNaN(x))
            {
                if (!double.IsNaN(x))
                {
                    return (x / 1000.0).ToString("0.000", C) + " км";
                }
                return "";
            }
            return Route.KmPk(x);
        }

        private static void Add(JournalInput inp, string code, bool gross, string title, string detail, string basis, double xOverride, double tOverride)
        {
            JournalEntry entry = new JournalEntry
            {
                T = (!double.IsNaN(tOverride)) ? tOverride : inp.T,
                X = (!double.IsNaN(xOverride)) ? xOverride : inp.X,
                V = Math.Abs(inp.V),
                Code = code,
                Gross = gross,
                Title = title,
                Detail = detail,
                Basis = basis
            };
            Commit(entry);
        }

        private static void Commit(JournalEntry entry)
        {
            int pts;
            if (!pointsByCode.TryGetValue(entry.Code, out pts))
            {
                pts = entry.Gross ? pointsGross : pointsOther;
            }
            entry.Points = pts;
            entry.Place = Place(entry.X);

            lock (Entries)
            {
                if (Entries.Count < 2000)
                {
                    Entries.Add(entry);
                }
            }

            string msg = string.Format(C, "Нарушение {0} ({1} балл.): {2}; {3}; {4} км/ч; {5}{6}",
                entry.Code, entry.Points, entry.Title, entry.Place, entry.V.ToString("0", C), entry.Detail,
                (entry.Basis.Length > 0) ? ("; " + entry.Basis) : "");

            if (Logged != null)
            {
                try { Logged(msg); } catch { }
            }

            WriteCsv(entry);
        }

        private static void WriteCsv(JournalEntry entry)
        {
            if (dir == null) return;
            try
            {
                if (path == null)
                {
                    Directory.CreateDirectory(dir);
                    path = Path.Combine(dir, "journal_" + DateTime.Now.ToString("yyyyMMdd_HHmmss", C) + ".csv");
                    File.WriteAllText(path, "время поездки;координата, м;км/пк;скорость, км/ч;код;грубое;баллы;нарушение;подробности;основание" + Environment.NewLine, new UTF8Encoding(true));
                }

                string[] cols = new string[10]
                {
                    entry.Clock,
                    (!double.IsNaN(entry.X)) ? entry.X.ToString("0", C) : "",
                    entry.Place,
                    entry.V.ToString("0.0", C),
                    entry.Code,
                    entry.Gross ? "да" : "нет",
                    entry.Points.ToString(C),
                    Clean(entry.Title),
                    Clean(entry.Detail),
                    Clean(entry.Basis)
                };
                File.AppendAllText(path, string.Join(";", cols) + Environment.NewLine, new UTF8Encoding(false));
            }
            catch
            {
            }
        }

        private static string Clean(string s)
        {
            return (s ?? "").Replace(";", ",").Replace("\r", " ").Replace("\n", " ");
        }
    }

}
