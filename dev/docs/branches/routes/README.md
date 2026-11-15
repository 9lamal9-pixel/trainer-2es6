# Маршрут, разметка и 3D-мир

Ветка: `codex/routes`. [Общая карта проекта](../../../../README.md#карта-веток).

Ветка содержит полный проект и общие зависимости. Ниже перечислена область изменений этой ветки.

## Канонические файлы и общие точки подключения

- [`outputs/TrainerRouteDOS.cs`](../../../../outputs/TrainerRouteDOS.cs)
- [`outputs/TrainerRouteView.cs`](../../../../outputs/TrainerRouteView.cs)
- [`outputs/TrainerMarkupHost.cs`](../../../../outputs/TrainerMarkupHost.cs)
- [`outputs/TrainerMarkupSignalSource.cs`](../../../../outputs/TrainerMarkupSignalSource.cs)
- [`outputs/Voynovka_Koshkarovsky_route.json`](../../../../outputs/Voynovka_Koshkarovsky_route.json)

## Материалы исследования и конфигурация

- [`dev/config/native-markup`](../../../../dev/config/native-markup)
- [`work/dos_route`](../../../../work/dos_route)
- [`work/modern_route/view`](../../../../work/modern_route/view)
- [`dev/tools/read_native_razmetka.py`](../../../../dev/tools/read_native_razmetka.py)
- [`dev/tools/investigate_world3d_manual.py`](../../../../dev/tools/investigate_world3d_manual.py)

Эти материалы включают диагностику и исторические копии; состав основной сборки определяют `.csproj` и `dev/config/environment.json`.

## Границы с другими направлениями

Сигнальные данные и ограничения используются codex/safety и codex/autovedenie. Профиль и кривые передаются общей моделью движения. Полные assets WORLD3DSERVER и штатного клиента восстанавливаются из Release.

## Документация

- [`dev/docs/WORLD3D_ARCHITECTURE.md`](../../../../dev/docs/WORLD3D_ARCHITECTURE.md)

## Проверки на Windows

После восстановления Release выполняйте команды из корня проекта, последовательно. Это перечень необходимых проверок, а не результат их выполнения при создании ветки.

```powershell
.\trainer.cmd -Action Test -Probe MarkupSafety
```

## Текущее состояние

Текущий C# стенд работает с локальным маршрутом. Явный мост к 3D-миру ещё не реализован; принятой отдельной проверки сетевого запуска и рендера нет. MarkupSafety проверяет взаимодействие разметки и безопасности.
