# КЛУБ-У, САУТ и бдительность

Ветка: `codex/safety`. [Общая карта проекта](../../../../README.md#карта-веток).

Ветка содержит полный проект и общие зависимости. Ниже перечислена область изменений этой ветки.

## Канонические файлы и общие точки подключения

- [`outputs/TrainerSafetyInputContext.cs`](../../../../outputs/TrainerSafetyInputContext.cs)
- [`outputs/TrainerSafetyContexts.cs`](../../../../outputs/TrainerSafetyContexts.cs)
- [`outputs/TrainerNativeSafetyAdapter.cs`](../../../../outputs/TrainerNativeSafetyAdapter.cs)
- [`outputs/TrainerSafetyHost.cs`](../../../../outputs/TrainerSafetyHost.cs)
- [`outputs/TrainerNativeVirtualPult.cs`](../../../../outputs/TrainerNativeVirtualPult.cs)
- [`outputs/TrainerKlubSafetyBinding.cs`](../../../../outputs/TrainerKlubSafetyBinding.cs)
- [`outputs/TrainerStandaloneSautBinding.cs`](../../../../outputs/TrainerStandaloneSautBinding.cs)
- [`outputs/TrainerSautOperatorControls.cs`](../../../../outputs/TrainerSautOperatorControls.cs)
- [`outputs/TrainerVigilanceOperatorControls.cs`](../../../../outputs/TrainerVigilanceOperatorControls.cs)

## Материалы исследования и конфигурация

- [`outputs/TrainerNativeSafetyProbe.cs`](../../../../outputs/TrainerNativeSafetyProbe.cs)
- [`outputs/TrainerVigilanceProbe.cs`](../../../../outputs/TrainerVigilanceProbe.cs)
- [`outputs/TrainerMarkupSafetyProbe.cs`](../../../../outputs/TrainerMarkupSafetyProbe.cs)

Эти материалы включают диагностику и исторические копии; состав основной сборки определяют `.csproj` и `dev/config/environment.json`.

## Границы с другими направлениями

Разметка и сигнальные данные поступают из codex/routes. Общие TrainerAuxSystems.cs и TrainerIntegrationHost.cs управляют жизненным циклом устройств. Приоритет ручного управления и защит согласуется с мостом автоведения.

## Документация

- [`dev/docs/SAFETY_MARKUP_RB_AUDIT.md`](../../../../dev/docs/SAFETY_MARKUP_RB_AUDIT.md)

## Проверки на Windows

После восстановления Release выполняйте команды из корня проекта, последовательно. Это перечень необходимых проверок, а не результат их выполнения при создании ветки.

```powershell
.\trainer.cmd -Action Test -Probe NativeSafetyAdapter,VigilanceOperator,MarkupSafety,AutovedenieOverride
```

## Текущее состояние

Проверки относятся к описанным сценариям адаптеров и операторских входов. Наличие интеграции КЛУБ/САУТ не подтверждает полное соответствие всей системы безопасности РЭ.
