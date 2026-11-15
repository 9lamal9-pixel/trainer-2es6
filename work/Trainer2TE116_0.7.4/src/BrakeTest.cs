using System;
using System.Collections.Generic;
using System.Globalization;

namespace Trainer2TE116
{
    /// <summary>
    /// Контроль выполнения проверки действия автотормозов в пути следования по знакам
    /// «Начало торможения» и «Конец торможения» согласно Разделу IX и Приложению № 3
    /// Правил ТО тормозов (в ред. Протокола СЖТ СНГ № 83 от 25–26.11.2025).
    /// </summary>
    public static class BrakeTest
    {
        public const int SignStart = 17;
        public const int SignEnd = 18;

        private static int st;
        private static bool enabled = true;
        private static double clock;
        private static double lastHead = double.NaN;
        private static double xStart;
        private static double xEnd1;
        private static double xEnd2;
        private static double urRef;
        private static double v0;
        private static double x0;
        private static double t0;
        private static double urMin;
        private static double tEffect;
        private static double tDrop;
        private static double xDropHead;
        private static double vArm;
        private static bool dropReached;
        private static bool releasedEarly;
        private static bool emergency;
        private static bool noEffect;

        private static readonly CultureInfo C = CultureInfo.InvariantCulture;
        public static readonly List<string> Results = new List<string>();
        public static int Satisfactory;
        public static int Unsatisfactory;
        public static int Skipped;
        public static Action<string> Logged;

        public static bool EmptyTrain;
        public static int Axles;
        public static double Charge = 0.5;
        public static int[] SignCoord = new int[0];
        public static int[] SignType = new int[0];

        public static bool Active { get { return st != 0; } }

        public static void Init()
        {
            enabled = true;
            st = 0;
            clock = 0.0;
            lastHead = double.NaN;
            Results.Clear();
            Skipped = (Unsatisfactory = (Satisfactory = 0));
        }

        public static void Step(double dt, double vSigned, double headX, double urMPa, int crane)
        {
            clock += dt;
            if (!enabled)
            {
                lastHead = double.NaN;
                return;
            }

            double vAbs = Math.Abs(vSigned);
            if (double.IsNaN(lastHead))
            {
                lastHead = headX;
                return;
            }

            int[] coords = SignCoord;
            int[] types = SignType;

            if (st == 0)
            {
                for (int i = 0; i < coords.Length && i < types.Length; i++)
                {
                    if (types[i] == SignStart && lastHead < coords[i] && headX >= coords[i])
                    {
                        Arm(i, coords, types, urMPa, vAbs);
                        break;
                    }
                }
            }

            if (st == 1)
            {
                if (urMPa <= urRef - 0.015 || crane == 5 || crane == 6 || crane == 7)
                {
                    st = 2;
                    t0 = clock;
                    x0 = headX;
                    v0 = vAbs;
                    urMin = urMPa;
                    tEffect = (tDrop = -1.0);
                    xDropHead = 0.0;
                    dropReached = (releasedEarly = (emergency = (noEffect = false)));
                }
                else
                {
                    double limitX = ((xEnd1 > 0.0) ? Math.Max(xEnd1, xEnd2) : (xStart + 1500.0)) + 200.0;
                    if (headX > limitX)
                    {
                        Skipped++;
                        string endNote = (xEnd1 <= 0.0)
                            ? "не найден"
                            : ((xEnd1 - xStart).ToString("0", C) + " м после него");
                        string msg = string.Format(C, "знак «Начало торможения» {0:0} м проследован на {1:0} км/ч без пробы тормозов (знак «Конец торможения» {2})",
                            xStart, vArm, endNote);
                        Write(msg);
                        Journal.AddBrakeTest(clock, xStart, vArm, true, msg);
                        st = 0;
                    }
                }
            }

            if (st == 2)
            {
                urMin = Math.Min(urMin, urMPa);
                // По Приложению № 3 Правил ТО тормозов (ред. Протокола № 83 от 25–26.11.2025):
                // снижение скорости после первой ступени — на 10 км/ч для гружёного и на 4–6 км/ч для порожнего поезда
                double reqDropKmh = EmptyTrain ? 4.0 : 10.0;
                double maxEffectWaitS = (!EmptyTrain || Axles > 400) ? 30.0 : 20.0;

                if (tEffect < 0.0 && vAbs <= v0 - 1.0)
                {
                    tEffect = clock - t0;
                }

                if (!dropReached && vAbs <= v0 - reqDropKmh)
                {
                    dropReached = true;
                    tDrop = clock - t0;
                    xDropHead = headX;
                }

                bool releasing = (crane == 1 || crane == 2 || urMPa > urMin + 0.01);
                if (!dropReached && releasing && !emergency)
                {
                    releasedEarly = true;
                }

                if (tEffect < 0.0 && clock - t0 > maxEffectWaitS)
                {
                    noEffect = true;
                }

                if (crane == 6)
                {
                    emergency = true;
                }

                bool done = ((dropReached || releasedEarly) && releasing && !emergency)
                    || (emergency && clock - t0 > 5.0)
                    || (vAbs < 1.0)
                    || (headX - x0 > 3000.0);

                if (done)
                {
                    Finish(reqDropKmh, maxEffectWaitS, vAbs, headX);
                    st = 0;
                }
            }

            lastHead = headX;
        }

        private static void Arm(int startIdx, int[] coords, int[] types, double urMPa, double vAbs)
        {
            xStart = coords[startIdx];
            xEnd1 = (xEnd2 = -1.0);
            for (int i = startIdx + 1; i < coords.Length && i < types.Length; i++)
            {
                if (types[i] == SignStart && coords[i] > xStart)
                {
                    break;
                }
                if (types[i] == SignEnd && coords[i] > xStart)
                {
                    if (xEnd1 < 0.0)
                    {
                        xEnd1 = coords[i];
                    }
                    else if (xEnd2 < 0.0)
                    {
                        xEnd2 = coords[i];
                        break;
                    }
                }
            }
            urRef = Math.Max(urMPa, Charge - 0.005);
            vArm = vAbs;
            st = 1;
        }

        private static void Finish(double reqDropKmh, double maxEffectWaitS, double vNow, double headX)
        {
            bool empty = EmptyTrain;
            double stageMPa = urRef - urMin;
            double minStage = empty ? 0.04 : 0.06;
            double maxStage = empty ? 0.06 : 0.08;

            List<string> notes = new List<string>();
            notes.Add(string.Format(C, "знак «Начало торможения» {0:0} м, {1} поезд",
                xStart, empty ? "порожний" : "гружёный"));

            string stageStatus;
            if (stageMPa < minStage - 0.005)
            {
                stageStatus = "меньше нормы " + (minStage * 10.0).ToString("0.0#", C) + "–" + (maxStage * 10.0).ToString("0.0#", C) + ", Раздел IX / Прил. № 3";
            }
            else if (stageMPa > maxStage + 0.005)
            {
                stageStatus = "больше нормы " + (minStage * 10.0).ToString("0.0#", C) + "–" + (maxStage * 10.0).ToString("0.0#", C) + ", Раздел IX / Прил. № 3";
            }
            else
            {
                stageStatus = "норма " + (minStage * 10.0).ToString("0.0#", C) + "–" + (maxStage * 10.0).ToString("0.0#", C);
            }

            notes.Add(string.Format(C, "ступень {0:0.00} кгс/см² ({1})", stageMPa * 10.1971617, stageStatus));

            if (stageMPa < minStage - 0.005 || stageMPa > maxStage + 0.005)
            {
                Journal.AddExternal("B1", false,
                    string.Format(C, "Ступень при пробе тормозов в пути {0:0.00} кгс/см²", stageMPa * 10.1971617),
                    string.Format(C, "{0} поезд: норма {1:0.0#}–{2:0.0#} кгс/см²",
                        empty ? "порожний" : "гружёный", minStage * 10.0, maxStage * 10.0),
                    "Правила ТО тормозов (ред. Протокола № 83 от 25–26.11.2025), Раздел IX, Прил. № 3",
                    clock, xStart, v0);
            }

            notes.Add((tEffect < 0.0)
                ? string.Format(C, "тормозного эффекта нет за {0:0} с", Math.Max(maxEffectWaitS, clock - t0))
                : string.Format(C, "тормозной эффект через {0:0} с", tEffect));

            bool ok = false;
            if (dropReached)
            {
                double distFromSign = xDropHead - xStart;
                bool beforeSign = (xEnd1 > 0.0) && (xDropHead <= xEnd1);
                ok = beforeSign && !releasedEarly;

                notes.Add(string.Format(C, "скорость {0:0} → {1:0} км/ч (норма {2}) за {3:0} м и {4:0} с от начала торможения, {5:0} м от знака",
                    v0, v0 - reqDropKmh, empty ? "4–6 км/ч" : "10 км/ч", xDropHead - x0, tDrop, distFromSign));

                if (xEnd1 > 0.0)
                {
                    if (beforeSign)
                    {
                        notes.Add(string.Format(C, "до знака «Конец торможения» ({0:0} м от начала) уложились", xEnd1 - xStart));
                    }
                    else
                    {
                        string sign2Note = (xEnd2 > 0.0 && xDropHead <= xEnd2)
                            ? string.Format(C, ", до второго знака ({0:0} м) уложились", xEnd2 - xStart)
                            : "";
                        notes.Add(string.Format(C, "за знаком «Конец торможения» ({0:0} м от начала){1}", xEnd1 - xStart, sign2Note));
                    }
                }
            }
            else
            {
                notes.Add(string.Format(C, "снижения скорости на {0} нет: {1:0} → {2:0} км/ч за {3:0} м",
                    empty ? "4–6 км/ч" : "10 км/ч", v0, vNow, headX - x0));
            }

            if (releasedEarly)
            {
                notes.Add(string.Format(C, "отпуск до снижения скорости на {0} — нарушение Раздела IX / Прил. № 3 Правил (ред. Протокола № 83)",
                    empty ? "4–6 км/ч" : "10 км/ч"));
            }

            if (noEffect)
            {
                notes.Add(emergency
                    ? "при отсутствии эффекта применено экстренное торможение (Раздел IX / Прил. № 3)"
                    : string.Format(C, "нет эффекта за {0:0} с, экстренное торможение не применено — нарушение Раздела IX / Прил. № 3", maxEffectWaitS));
            }
            else if (emergency)
            {
                notes.Add("применено экстренное торможение");
            }

            if (!ok && dropReached && !releasedEarly)
            {
                notes.Add("тормоза действуют неудовлетворительно: по Приложению № 3 Правил (ред. Протокола № 83) требуется экстренное торможение и остановка поезда");
            }

            if (ok) Satisfactory++;
            else Unsatisfactory++;

            string summary = (ok ? "удовлетворительно" : "НЕУДОВЛЕТВОРИТЕЛЬНО") + ": " + string.Join("; ", notes.ToArray());
            Write(summary);

            if (!ok)
            {
                Journal.AddBrakeTest(clock, xStart, v0, false, string.Join("; ", notes.ToArray()));
            }
        }

        private static void Write(string s)
        {
            string line = "Проба тормозов в пути: " + s;
            if (Results.Count < 200)
            {
                Results.Add(line);
            }
            if (Logged != null)
            {
                Logged(line);
            }
        }
    }
}
