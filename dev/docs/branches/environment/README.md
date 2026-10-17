# Среда разработки, сборка и перенос

Ветка: `codex/environment`. [Общая карта проекта](../../../../README.md#карта-веток).

Ветка содержит полный проект и общие зависимости. Ниже перечислена область изменений этой ветки.

## Канонические файлы и общие точки подключения

- [`dev/TrainerIntegrationHost.csproj`](../../../../dev/TrainerIntegrationHost.csproj)
- [`dev/Trainer.sln`](../../../../dev/Trainer.sln)
- [`dev/TrainerDev.ps1`](../../../../dev/TrainerDev.ps1)
- [`dev/scripts`](../../../../dev/scripts)
- [`dev/config/environment.json`](../../../../dev/config/environment.json)
- [`dev/config/probes.json`](../../../../dev/config/probes.json)
- [`dev/config/modules.json`](../../../../dev/config/modules.json)
- [`dev/config/native-lock.json`](../../../../dev/config/native-lock.json)
- [`dev/config/portable-release.json`](../../../../dev/config/portable-release.json)
- [`dev/config/native-distribution.json`](../../../../dev/config/native-distribution.json)
- [`dev/environment-context.json`](../../../../dev/environment-context.json)
- [`setup.cmd`](../../../../setup.cmd)
- [`trainer.cmd`](../../../../trainer.cmd)
- [`Trainer.code-workspace`](../../../../Trainer.code-workspace)
- [`AGENTS.md`](../../../../AGENTS.md)
- [`BOT_PROMPT.txt`](../../../../BOT_PROMPT.txt)
- [`outputs/TrainerSessionLogging.cs`](../../../../outputs/TrainerSessionLogging.cs)

## Материалы исследования и конфигурация

- [`dev/tools`](../../../../dev/tools)
- [`dev/config/python-tools.json`](../../../../dev/config/python-tools.json)
- [`dev/config/relocation.json`](../../../../dev/config/relocation.json)
- [`.vscode`](../../../../.vscode)
- [`.gitignore`](../../../../.gitignore)
- [`.gitattributes`](../../../../.gitattributes)
- [`.rgignore`](../../../../.rgignore)

Эти материалы включают диагностику и исторические копии; состав основной сборки определяют `.csproj` и `dev/config/environment.json`.

## Границы с другими направлениями

Все тематические ветки используют общие .csproj и environment.json. Добавление исходника требует обновить проект, затронутые профили и связанные реестры. Исходники отдельных проб относятся к своей подсистеме, а механизм их сборки/запуска — к этой ветке.

## Документация

- [`dev/docs/DEVELOPMENT.md`](../../../../dev/docs/DEVELOPMENT.md)
- [`dev/docs/PORTABLE_SETUP.md`](../../../../dev/docs/PORTABLE_SETUP.md)
- [`dev/docs/PORTABLE_RELEASE_NOTES.md`](../../../../dev/docs/PORTABLE_RELEASE_NOTES.md)
- [`dev/docs/MIGRATION.md`](../../../../dev/docs/MIGRATION.md)
- [`dev/docs/MODULES.md`](../../../../dev/docs/MODULES.md)

## Проверки на Windows

После восстановления Release выполняйте команды из корня проекта, последовательно. Это перечень необходимых проверок, а не результат их выполнения при создании ветки.

```powershell
.\trainer.cmd -Action Doctor
.\trainer.cmd -Action Build -Profile integration
.\trainer.cmd -Action Build -Profile candidate
.\trainer.cmd -Action Test -Suite smoke
.\trainer.cmd -Action Handoff
```

## Текущее состояние

Сборка и native-проверки требуют Windows/.NET Framework 4 x86 и данных из закреплённого Release. В Git отсутствуют штатные DLL/EXE и динамический context.json. Справочный modules.json пока отстаёт на TrainerTrainBrakeCycle.cs и TrainerSandAirInput.cs; реальные профили сборки содержат 26/33 файла.
