# Пневматика, тормоза и физический состав

Ветка: `codex/pneumatics`. [Общая карта проекта](../../../../README.md#карта-веток).

Ветка содержит полный проект и общие зависимости. Ниже перечислена область изменений этой ветки.

## Канонические файлы и общие точки подключения

- [`outputs/TrainerAirNetwork.cs`](../../../../outputs/TrainerAirNetwork.cs)
- [`outputs/TrainerControlAir.cs`](../../../../outputs/TrainerControlAir.cs)
- [`outputs/TrainerBtoNetwork.cs`](../../../../outputs/TrainerBtoNetwork.cs)
- [`outputs/TrainerAirSelector.cs`](../../../../outputs/TrainerAirSelector.cs)
- [`outputs/TrainerKvtProfile.cs`](../../../../outputs/TrainerKvtProfile.cs)
- [`outputs/TrainerTwoSectionAirChain.cs`](../../../../outputs/TrainerTwoSectionAirChain.cs)
- [`outputs/TrainerPneumaticNetworkCandidate.cs`](../../../../outputs/TrainerPneumaticNetworkCandidate.cs)
- [`outputs/TrainerPneumaticTiming.cs`](../../../../outputs/TrainerPneumaticTiming.cs)
- [`outputs/TrainerTrainPressureView.cs`](../../../../outputs/TrainerTrainPressureView.cs)
- [`outputs/TrainerSandAirInput.cs`](../../../../outputs/TrainerSandAirInput.cs)
- [`outputs/TrainerNativeSystems.cs`](../../../../outputs/TrainerNativeSystems.cs)

## Материалы исследования и конфигурация

- [`work/pneumatic_live_fix/analysis`](../../../../work/pneumatic_live_fix/analysis)
- [`dev/config/brake-rules-compliance.json`](../../../../dev/config/brake-rules-compliance.json)

Эти материалы включают диагностику и исторические копии; состав основной сборки определяют `.csproj` и `dev/config/environment.json`.

## Границы с другими направлениями

Первые семь файлов — дополнение профиля candidate. TrainerNativeSystems.cs общий для движения, электрического и пневматического циклов. TrainerTrainBrakeCycle.cs находится в области автоведения; изменения тормозной модели требуют согласования калибровки с codex/autovedenie.

## Документация

- [`dev/docs/PNEUMATICS_RE_AUDIT.md`](../../../../dev/docs/PNEUMATICS_RE_AUDIT.md)
- [`dev/docs/TRAIN_PNEUMATICS_AUDIT.md`](../../../../dev/docs/TRAIN_PNEUMATICS_AUDIT.md)

## Проверки на Windows

После восстановления Release выполняйте команды из корня проекта, последовательно. Это перечень необходимых проверок, а не результат их выполнения при создании ветки.

```powershell
.\trainer.cmd -Action Test -Suite regression
.\trainer.cmd -Action Test -Probe TrainPneumatic,TrainIsolation,KmOvercharge,TrainBrakeCycle,TrainBrakeNative
.\trainer.cmd -Action Test -Probe NativeSandAir,NativeBrakeBound,RepeatedBrakeNative
```

## Текущее состояние

Текущий код задаёт 75 вагонов по 80 т и локомотив 200 т. Сценарий 70 вагонов / 280 вагонных осей / 5700 т вагонов ещё нужно согласованно настроить. Проверка повторного торможения и ряд параметров холодной зарядки остаются незавершёнными.
