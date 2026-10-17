# Электрические цепи, тяга и независимое возбуждение

Ветка: `codex/traction`. [Общая карта проекта](../../../../README.md#карта-веток).

Ветка содержит полный проект и общие зависимости. Ниже перечислена область изменений этой ветки.

## Канонические файлы и общие точки подключения

- [`outputs/TrainerIntegrationHost.cs`](../../../../outputs/TrainerIntegrationHost.cs)
- [`outputs/TrainerNativeSystems.cs`](../../../../outputs/TrainerNativeSystems.cs)
- [`outputs/TrainerAuxSystems.cs`](../../../../outputs/TrainerAuxSystems.cs)
- [`outputs/TrainerNativeFixes.cs`](../../../../outputs/TrainerNativeFixes.cs)
- [`outputs/TrainerNativeVirtualPult.cs`](../../../../outputs/TrainerNativeVirtualPult.cs)

## Материалы исследования и конфигурация

- [`work/modern_route/traction`](../../../../work/modern_route/traction)

Эти материалы включают диагностику и исторические копии; состав основной сборки определяют `.csproj` и `dev/config/environment.json`.

## Границы с другими направлениями

Тягу и НВ рассчитывают штатные Communication1/Pult1/Tyga1 DLL. C# связывает их с пультом и моделью движения. Изменения команд автоведения согласуются с codex/autovedenie; давление токоприёмников и компрессоры — с codex/pneumatics.

## Документация

- [`dev/docs/audit_parts/MPSUD_TRACTION.md`](../../../../dev/docs/audit_parts/MPSUD_TRACTION.md)
- [`dev/docs/audit_parts/MPSUD_CONTROL.md`](../../../../dev/docs/audit_parts/MPSUD_CONTROL.md)
- [`dev/docs/MPSUID_2ES6_AUDIT.md`](../../../../dev/docs/MPSUID_2ES6_AUDIT.md)

## Проверки на Windows

После восстановления Release выполняйте команды из корня проекта, последовательно. Это перечень необходимых проверок, а не результат их выполнения при создании ветки.

```powershell
.\trainer.cmd -Action Doctor
.\trainer.cmd -Action Build -Profile candidate
.\trainer.cmd -Action Test -Suite smoke
```

## Текущее состояние

Расчёт токов якоря/возбуждения и силы тяги по всем позициям при 3 кВ ещё требует измеряемых прогонов и сверки с применимой редакцией РЭ. Smoke проверяет общую интеграцию и пневматику; он не принимает тяговые характеристики. Старые Install/Apply-скрипты из материалов исследования не запускать.
