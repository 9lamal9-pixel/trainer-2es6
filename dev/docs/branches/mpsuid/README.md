# МПСУиД, МСУЛ и экраны

Ветка: `codex/mpsuid`. [Общая карта проекта](../../../../README.md#карта-веток).

Ветка содержит полный проект и общие зависимости. Ниже перечислена область изменений этой ветки.

## Канонические файлы и общие точки подключения

- [`outputs/TrainerIntegrationHost.cs`](../../../../outputs/TrainerIntegrationHost.cs)
- [`outputs/TrainerAuxSystems.cs`](../../../../outputs/TrainerAuxSystems.cs)
- [`outputs/TrainerNativeFixes.cs`](../../../../outputs/TrainerNativeFixes.cs)

## Материалы исследования и конфигурация

- [`work/msul_emulator`](../../../../work/msul_emulator)
- [`work/mpsud_screen_values`](../../../../work/mpsud_screen_values)
- [`work/mpsud_screen_sources`](../../../../work/mpsud_screen_sources)
- [`work/mpsud_manual_discovery`](../../../../work/mpsud_manual_discovery)

Эти материалы включают диагностику и исторические копии; состав основной сборки определяют `.csproj` и `dev/config/environment.json`.

## Границы с другими направлениями

Штатные формы находятся в MSUL_MON.dll; общая оболочка подключает их и содержит защищённое исправление SC09. Экран редактора и привязки его полей относятся к МСУЛ; сохранение параметров АВП и их влияние на состав — к codex/autovedenie. Диагностический MSULNativeHost.cs не входит в текущую основную сборку.

## Документация

- [`dev/docs/MPSUID_2ES6_AUDIT.md`](../../../../dev/docs/MPSUID_2ES6_AUDIT.md)
- [`dev/docs/MPSUID_SCREENS_RE_AUDIT.md`](../../../../dev/docs/MPSUID_SCREENS_RE_AUDIT.md)
- [`dev/docs/MPSUID_RE_SCREENS_GUIDE.md`](../../../../dev/docs/MPSUID_RE_SCREENS_GUIDE.md)
- [`dev/docs/MPSUID_RE_SCREENS_GALLERY.html`](../../../../dev/docs/MPSUID_RE_SCREENS_GALLERY.html)
- [`dev/docs/audit_parts/MPSUD_SCREEN_REQUIREMENTS.md`](../../../../dev/docs/audit_parts/MPSUD_SCREEN_REQUIREMENTS.md)
- [`dev/docs/audit_parts/MPSUD_SCREEN_COMPARISON.md`](../../../../dev/docs/audit_parts/MPSUD_SCREEN_COMPARISON.md)

## Проверки на Windows

После восстановления Release выполняйте команды из корня проекта, последовательно. Это перечень необходимых проверок, а не результат их выполнения при создании ветки.

```powershell
.\trainer.cmd -Action Test -Probe MpsudWagonInput
.\trainer.cmd -Action Test -Probe MpsudAuditSnapshot,MpsudScreenRuntime
```

## Текущее состояние

Документированная приёмка SC09 ограничена отображением предварительного ввода числа вагонов. Остаются расхождения показаний и диагностики; полное соответствие РЭ не принято. MpsudDiagnosticArrows отключена и не используется для приёмки.
