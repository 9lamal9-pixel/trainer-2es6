# Автоведение и алгоритмы ведения поезда

Ветка: `codex/autovedenie`. [Общая карта проекта](../../../../README.md#карта-веток).

Ветка содержит полный проект и общие зависимости. Ниже перечислена область изменений этой ветки.

## Канонические файлы и общие точки подключения

- [`outputs/TrainerAutovedenieController.cs`](../../../../outputs/TrainerAutovedenieController.cs)
- [`outputs/TrainerTrainBrakeCycle.cs`](../../../../outputs/TrainerTrainBrakeCycle.cs)
- [`outputs/TrainerAutovedenieHost.cs`](../../../../outputs/TrainerAutovedenieHost.cs)
- [`outputs/TrainerAutovedenieInterface.cs`](../../../../outputs/TrainerAutovedenieInterface.cs)
- [`outputs/TrainerAutovedenieNativeBridge.cs`](../../../../outputs/TrainerAutovedenieNativeBridge.cs)

## Материалы исследования и конфигурация

- [`dev/config/autovedenie-requirements.json`](../../../../dev/config/autovedenie-requirements.json)
- [`dev/config/autovedenie-brake-calibration.json`](../../../../dev/config/autovedenie-brake-calibration.json)
- [`dev/config/autovedenie-calibration-evidence`](../../../../dev/config/autovedenie-calibration-evidence)
- [`outputs/TrainerAutovedenieAdverseBrakeProbe.cs`](../../../../outputs/TrainerAutovedenieAdverseBrakeProbe.cs)
- [`outputs/TrainerRepeatedBrakeNativeProbe.cs`](../../../../outputs/TrainerRepeatedBrakeNativeProbe.cs)
- [`outputs/TrainerStoppedBrakeNativeProbe.cs`](../../../../outputs/TrainerStoppedBrakeNativeProbe.cs)
- [`outputs/TrainerNativeBrakeObserver.cs`](../../../../outputs/TrainerNativeBrakeObserver.cs)

Эти материалы включают диагностику и исторические копии; состав основной сборки определяют `.csproj` и `dev/config/environment.json`.

## Границы с другими направлениями

NativeBridge подаёт команды штатной тяге и тормозам; он зависит от codex/traction, codex/pneumatics и codex/safety. Изменение физического состава требует одновременно обновить параметры интерфейса и тормозную калибровку. TrainerNativeBrakeObserver.cs — поддержка проб, не основной Compile-файл.

## Документация

- [`dev/docs/AUTOVEDENIE_REQUIREMENTS_AUDIT.md`](../../../../dev/docs/AUTOVEDENIE_REQUIREMENTS_AUDIT.md)
- [`dev/docs/GITHUB_SNAPSHOT.md`](../../../../dev/docs/GITHUB_SNAPSHOT.md)

## Проверки на Windows

После восстановления Release выполняйте команды из корня проекта, последовательно. Это перечень необходимых проверок, а не результат их выполнения при создании ветки.

```powershell
.\trainer.cmd -Action Test -Probe AutovedenieNativeBridgeGuard,AutovedenieOverride,AutovedenieAdverseBrake
.\trainer.cmd -Action Test -Probe TrainBrakeCycle,RepeatedBrakeNative
```

## Текущее состояние

Историческая ветка codex/autovedenie сохранена и продолжена без переписывания истории. Калибровка помечена accepted=false; успешное завершение проверки последнего исправления повторного торможения не подтверждено. В коде остаётся привязка к 75 вагонам и 6200 т полной массы.
