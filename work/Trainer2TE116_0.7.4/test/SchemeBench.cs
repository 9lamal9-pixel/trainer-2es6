// Bench of the scheme alone (no physics): the text is read without errors, every apparatus with contacts has a coil,
// the start of the diesel goes by the times of the scheme, the compressor, the assembly of the traction from the 1st
// position, the protections, interventions (a break, a jumper), the stop with the oil pumping, the emergency button.
using System;
using System.Collections.Generic;
using System.Linq;
using Trainer2TE116;

static class SchemeBench
{
    static int fails;
    static void Check(string name, bool ok, string detail = "") { if (!ok) fails++; Console.WriteLine((ok ? "ok   " : "FAIL ") + name + (!ok && detail.Length > 0 ? "  [" + detail + "]" : "")); }
    static Scheme New()
    {
        var s = new Scheme(SchemeData.Text);
        // everything switched on as on a prepared locomotive; sensors as on a stopped diesel
        var on = new HashSet<string> { "ВБ", "А1", "А2", "А3", "А4", "А5", "А6", "А7", "А8", "АУ", "БУ", "АВУ", "1АТ", "2АТ", "АМК", "ТРК", "БД", "ВРЗ", "105", "ОМ1", "ОМ2", "ОМ3", "ОМ4", "ОМ5", "ОМ6",
            "РР", "РР.В", "КМ4", "ЭПК", "УТ", "ТД", "ТУП", "ТХ", "ТА", "РДВ", "АБ" };
        foreach (var k in s.Inputs.ToList()) s.Set(k, on.Contains(Scheme.Bare(k)));
        return s;
    }
    static void Run(Scheme s, double sec) { for (int i = 0; i < (int)Math.Round(sec / 0.1); i++) s.Step(0.1); }
    static void Pos(Scheme s, int p)
    {
        s.Set("КМ4", p == 0); s.Set("КМ1", p >= 1); s.Set("КМ5", p >= 2); s.Set("КМ6", p >= 3); s.Set("КМ7", p >= 4);
        s.Set("КМ10", p >= 2 && ((p / 2) & 1) == 1); s.Set("КМ9", p >= 2 && ((p / 4) & 1) == 1); s.Set("КМ8", p >= 8); s.Set("КМ2", p >= 3 && (p & 1) == 1);
    }
    static string Why(Scheme s, params string[] keys) { return string.Join(" | ", keys.Select(k => k + ": " + s.WhyOff(s.Apps[k]))); }
    static int Main()
    {
        Console.OutputEncoding = System.Text.Encoding.UTF8;
        var s = New();
        foreach (var e in s.Errors) Console.WriteLine("ERR " + e);
        Check("схема разобрана без ошибок", s.Errors.Count == 0, s.Errors.Count + " ошибок");
        var noCoil = s.Apps.Values.Where(a => a.Coils.Count == 0).Select(a => a.Key).ToList();
        Check("у каждого аппарата с контактами есть катушка", noCoil.Count == 0, string.Join(" ", noCoil));
        Console.WriteLine("листов " + s.Sheets.Count + ", цепей " + s.Rungs.Count + ", элементов " + s.Els.Count + ", аппаратов " + s.Apps.Count + ", узлов " + s.Nodes.Count + ", входов " + s.Inputs.Count());
        Console.WriteLine("входы: " + string.Join(" ", s.Inputs.Where(k => k.EndsWith("@1") || k.IndexOf('@') < 0)));

        // ---- start of the diesel of section 1
        Run(s, 1);
        Check("до пуска: шина 1232 под напряжением, КН включён", s.Live("1232@1") && s.On("КН@1"), Why(s, "КН@1"));
        Check("до пуска: КТН выключен", !s.On("КТН@1"));
        s.Set("ТН1", true); Run(s, 0.5);
        Check("тумблер ТН1: КТН включён, насос работает", s.On("КТН@1") && s.On("ТН@1"), Why(s, "КТН@1", "ТН@1"));
        s.Set("ПД1", true); Run(s, 0.3); s.Set("ПД1", false); Run(s, 0.3);
        Check("кнопка ПД1 отпущена: КМН на самопитании, РВП1 включено", s.On("КМН@1") && s.On("РВП1@1") && s.On("МН@1"), Why(s, "КМН@1", "РВП1@1"));
        s.Set("РДМ3@1", true);
        Run(s, 58);
        Check("58 с: Д3 ещё выключен", !s.On("Д3@1"));
        Run(s, 3);
        Check("61 с: Д3, МР6, Д2, ВП7, Д1 обеих секций включены", s.On("Д3@1") && s.On("МР6@1") && s.On("Д2@1") && s.On("ВП7@1") && s.On("Д1@1") && s.On("Д1@2"), Why(s, "Д3@1", "Д2@1", "Д1@2"));
        s.Set("РДМ4@1", true);      // the diesel fired and built the oil pressure
        Run(s, 10);
        Check("71 с: РУ9 ещё выключено", !s.On("РУ9@1"));
        Run(s, 2.5);
        Check("после 12 с прокрутки: РУ9, РУ23, РУ10, КРН включены", s.On("РУ9@1") && s.On("РУ23@1") && s.On("РУ10@1") && s.On("КРН@1"), Why(s, "РУ9@1", "РУ10@1", "КРН@1"));
        Check("после пуска: КМН, Д1, Д2, Д3 отключены, МР6 включён", !s.On("КМН@1") && !s.On("Д1@1") && !s.On("Д2@1") && !s.On("Д3@1") && s.On("МР6@1"), Why(s, "МР6@1"));
        Check("после пуска: КТН отключён контактом КРН", !s.On("КТН@1"));
        s.Set("СГ@1", true); s.Set("СВ@1", true); Run(s, 1);
        Check("холостой ход: РКВ, КВ, ВВ включены", s.On("РКВ@1") && s.On("КВ@1") && s.On("ВВ@1"), Why(s, "РКВ@1", "КВ@1"));
        Check("лампа «Сброс нагрузки 1» не горит на холостом ходу", !s.On("ЛН1"));

        // ---- compressor
        s.Set("РДК", true); Run(s, 1);
        Check("РДК: РУ24, КДК включены, КУДК ещё нет", s.On("РУ24@1") && s.On("КДК@1") && !s.On("КУДК@1") && s.On("Компрессор@1"), Why(s, "РУ24@1", "КДК@1", "Компрессор@1"));
        Run(s, 2);
        Check("через 2 с: КУДК включён", s.On("КУДК@1"), Why(s, "КУДК@1"));
        s.Set("РДК", false); Run(s, 0.5);
        Check("РДК отключил: компрессор остановлен", !s.On("КДК@1") && !s.On("КУДК@1") && !s.On("Компрессор@1"));

        // ---- traction
        Pos(s, 1); Run(s, 0.2); s.Set("ПР.В@1", s.On("ПР_В@1")); Run(s, 1.5);
        Check("1-я позиция: реверсор, РУ22, РВ3, П1–П6, РУ5, КВ включены", s.On("ПР_В@1") && s.On("РУ22@1") && s.On("РВ3@1") && s.On("П1@1") && s.On("П6@1") && s.On("РУ5@1") && s.On("КВ@1"),
            Why(s, "РУ22@1", "РВ3@1", "П1@1", "РУ5@1", "РКВ@1"));
        Pos(s, 2); Run(s, 0.5);
        Check("2-я позиция: РУ8 включено, МР1 включён, схема держится", s.On("РУ8@1") && s.On("МР1@1") && s.On("РВ3@1") && s.On("КВ@1"), Why(s, "РВ3@1", "РУ8@1", "МР1@1"));
        Pos(s, 5); Run(s, 0.5);
        Check("5-я позиция: МР2 и МР4", !s.On("МР1@1") && s.On("МР2@1") && !s.On("МР3@1") && s.On("МР4@1"));
        s.Set("РМ1@1", true); Run(s, 0.2); s.Set("РМ1@1", false); Run(s, 1.5);
        Check("РМ1: РУ2 на самопитании, П отключены, возбуждение холостого хода, горит «Сброс нагрузки 1»", s.On("РУ2@1") && s.On("КВ@1") && !s.On("П1@1") && !s.On("РУ5@1") && s.On("ЛН1"), Why(s, "РУ2@1", "ЛН1", "КВ@1"));
        Pos(s, 0); Run(s, 1.5);
        Check("контроллер на ноль: РУ2 отпало, холостой ход восстановлен", !s.On("РУ2@1") && s.On("КВ@1") && !s.On("ЛН1"), Why(s, "КВ@1"));
        Pos(s, 2); Run(s, 1.5);
        Check("сразу 2-я позиция: схема не собирается (РУ8)", !s.On("РВ3@1") && !s.On("П1@1"));
        Pos(s, 0); Run(s, 1.5); Pos(s, 1); Run(s, 1.5);
        Check("снова с 1-й позиции: собрана", s.On("П1@1") && s.On("КВ@1"), Why(s, "РВ3@1", "П1@1", "КВ@1"));
        Pos(s, 0); Run(s, 0.5);
        Check("сброс на ноль, 0,5 с: поездные контакторы ещё включены (РВ3 0,8 с), КВ отключён", s.On("П1@1") && !s.On("КВ@1"));
        Run(s, 1);
        Check("через 1,5 с: П отключены, холостой ход", !s.On("П1@1") && s.On("КВ@1"), Why(s, "КВ@1"));

        // ---- interventions: a break of the coil KV, an open door and a jumper over its interlock
        var kv = s.Apps["КВ@1"].Coils[0]; s.SetOverride(kv, 1); Run(s, 0.3);
        Check("обрыв катушки КВ: КВ выключен, причина названа", !s.On("КВ@1") && s.WhyOff(s.Apps["КВ@1"]) == "обрыв катушки", Why(s, "КВ@1"));
        s.SetOverride(kv, 0); Run(s, 0.3);
        s.Set("БД@1", false); Run(s, 0.3);
        Check("открыта дверь ВВК: КВ и ВВ выключены, причина — БД", !s.On("КВ@1") && !s.On("ВВ@1") && s.WhyOff(s.Apps["КВ@1"]).Contains("БД"), Why(s, "КВ@1"));
        Check("горит «Сброс нагрузки 1»", s.On("ЛН1"), Why(s, "ЛН1", "РУ11@1"));
        var bd = s.Els.First(e => e.Kind == ElKind.Switch && e.Key == "БД@1"); s.SetOverride(bd, 2); Run(s, 0.3);
        Check("перемычка на блокировке двери: возбуждение восстановлено", s.On("КВ@1") && s.On("ВВ@1"));
        s.SetOverride(bd, 0); s.Set("БД@1", true); Run(s, 0.3);

        // ---- stop by the fuel pump toggle and the oil pumping after it
        s.Set("ТН1", false); Run(s, 0.3);
        Check("ТН1 выключен: РУ10, КРН, МР6 отключены", !s.On("РУ10@1") && !s.On("КРН@1") && !s.On("МР6@1"));
        s.Set("РДМ4@1", false); s.Set("СГ@1", false); Run(s, 0.5);
        Check("давление масла упало: РУ9 отпало, КМН включён (прокачка)", !s.On("РУ9@1") && s.On("КМН@1") && s.On("РУ23@1"), Why(s, "КМН@1"));
        Run(s, 61);
        Check("через 60 с: РУ23 и КМН отключены", !s.On("РУ23@1") && !s.On("КМН@1"));

        // ---- emergency button
        s.Set("КА", true); Run(s, 0.2);
        Check("кнопка КА: РУ3 обеих секций, тифон", s.On("РУ3@1") && s.On("РУ3@2") && s.On("ВТ@1"), Why(s, "РУ3@1"));
        s.Set("КА", false); Run(s, 0.5);
        Check("кнопка отпущена: РУ3 на самопитании, ВА включён", s.On("РУ3@1") && s.On("ВА@1"));
        s.Set("РР", false); s.Set("РР.В", false); Run(s, 0.3);
        Check("реверсивная рукоятка в нейтрали: РУ3 отпало", !s.On("РУ3@1") && !s.On("РУ3@2"));

        // ---- a short circuit: a jumper from the plus of the coil KTN to the minus trips the breaker A3
        s.Set("РР", true); s.Set("РР.В", true); s.Set("ТН1", true); Run(s, 0.3);
        string tripped = null; s.Trip = k => tripped = k; s.Breakers.Add("А3");
        var coil = s.Apps["КТН@1"].Coils[0]; s.SetOverride(coil, 0);
        var tn = s.Els.First(e => e.Kind == ElKind.Switch && e.Key == "ТН1");
        var wire = s.Els.First(e => e.Rung.Name == "КТН" && e.Sec == 1 && e.Kind == ElKind.Wire);
        Check("до замыкания КТН включён", s.On("КТН@1"));
        Console.WriteLine(fails == 0 ? "SCHEME PASS" : "SCHEME FAIL " + fails);
        return fails == 0 ? 0 : 1;
    }
}
