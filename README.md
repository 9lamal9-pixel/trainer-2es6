# Среда разработки тренажёра 2ЭС6

## Карта веток

`main` содержит общую базу проекта. Тематические ветки созданы для работы над отдельными подсистемами; в каждой сохранён полный набор зависимостей и оба профиля сборки. Подробный список файлов и проверок находится в README соответствующей области.

| Ветка | Что к ней относится | Файлы и проверки |
|---|---|---|
| [`main`](https://github.com/9lamal9-pixel/trainer-2es6/tree/main) | Общая интеграция всех подсистем | Этот README, [модули](dev/docs/MODULES.md), [состав сборки](dev/TrainerIntegrationHost.csproj) |
| [`codex/traction`](https://github.com/9lamal9-pixel/trainer-2es6/tree/codex/traction) | Штатная тяга, команды контроллера, электрический цикл и НВ | [README области](https://github.com/9lamal9-pixel/trainer-2es6/blob/codex/traction/dev/docs/branches/traction/README.md) |
| [`codex/mpsuid`](https://github.com/9lamal9-pixel/trainer-2es6/tree/codex/mpsuid) | Показания, меню, диагностика и исправления экранов МСУЛ | [README области](https://github.com/9lamal9-pixel/trainer-2es6/blob/codex/mpsuid/dev/docs/branches/mpsuid/README.md) |
| [`codex/pneumatics`](https://github.com/9lamal9-pixel/trainer-2es6/tree/codex/pneumatics) | Воздушная сеть, компрессоры, тормоза локомотива и вагонов, масса состава | [README области](https://github.com/9lamal9-pixel/trainer-2es6/blob/codex/pneumatics/dev/docs/branches/pneumatics/README.md) |
| [`codex/safety`](https://github.com/9lamal9-pixel/trainer-2es6/tree/codex/safety) | Защитные системы, приоритеты торможения и операторские органы | [README области](https://github.com/9lamal9-pixel/trainer-2es6/blob/codex/safety/dev/docs/branches/safety/README.md) |
| [`codex/routes`](https://github.com/9lamal9-pixel/trainer-2es6/tree/codex/routes) | Режимная карта, профиль пути, ограничения, сигналы и материалы 3D | [README области](https://github.com/9lamal9-pixel/trainer-2es6/blob/codex/routes/dev/docs/branches/routes/README.md) |
| [`codex/autovedenie`](https://github.com/9lamal9-pixel/trainer-2es6/tree/codex/autovedenie) | Контроллер АВП, управление тягой/торможением, интерфейс и калибровки | [README области](https://github.com/9lamal9-pixel/trainer-2es6/blob/codex/autovedenie/dev/docs/branches/autovedenie/README.md) |
| [`codex/environment`](https://github.com/9lamal9-pixel/trainer-2es6/tree/codex/environment) | Настройка Windows, сборка, запуск проб, упаковка Release и документация | [README области](https://github.com/9lamal9-pixel/trainer-2es6/blob/codex/environment/dev/docs/branches/environment/README.md) |

### Общие файлы и пересечения

`outputs/TrainerIntegrationHost.cs`, `TrainerNativeSystems.cs` и `TrainerAuxSystems.cs` соединяют подсистемы и общий цикл. `TrainerNativeFixes.cs` содержит исправления тяги и МСУЛ; `TrainerNativeVirtualPult.cs` связывает пульт с native-модулями; `TrainerSessionLogging.cs` ведёт журналы. Изменения этих файлов согласуются между затронутыми направлениями.

Канонические исходники остаются в `outputs`. Исторические копии в `work/*/backup` и диагностические стенды сохраняют своё назначение. Общими также остаются `.csproj`, `dev/config/environment.json`, каталоги проб/модулей и native-манифесты. При добавлении C#-файла обновляются проект и профили сборки.

### Как работать с ветками

Выберите область и переключитесь на её ветку, например:

```bash
git fetch origin
git switch codex/traction
```

Вносите связанные изменения в выбранной ветке и выполняйте её проверки. Завершённые изменения объединяйте в `main`; затем обновляйте тематические ветки из `main`. Общие зависимости остаются на месте, поэтому расположение исходников и runtime-пути одинаковы во всех ветках.

### Состояние общей базы

Организация веток выполнена от снимка `a04ed4b` (`trainer-environment-2026-10-16`) с сохранением истории исходной ветки `codex/autovedenie` и первоначального `main`. Изменения касаются структуры веток и документации.

- Фактический состав пока 75 вагонов / 6000 т вагонов + 200 т локомотива; сценарий 70 вагонов / 280 вагонных осей / 5700 т вагонов ещё требует настройки.
- Автоведение и полное соответствие МПСУиД РЭ не приняты; калибровка торможения помечена недействительной. Подробнее: [состояние снимка](dev/docs/GITHUB_SNAPSHOT.md).
- Для запуска нужны Windows/.NET Framework 4 x86 и штатные DLL/данные из отдельного Release. Создание веток не является новым прогоном native-проверок.

## Настройка и расположение файлов

Полное развёртывание на другом компьютере: [инструкция](dev/docs/PORTABLE_SETUP.md). Склонируйте ветку `main` и запустите `setup.cmd`: он скачает закреплённый приватный Release, проверит архивы и подготовит Windows-среду.

Рабочая папка: `C:\Users\sibir\Desktop\Макрос\Тренажер`. Готовый промпт для бота: [BOT_PROMPT.txt](BOT_PROMPT.txt). Вставьте его в новый сеанс и допишите задачу. Бот начинает с [AGENTS.md](AGENTS.md) и двух компактных JSON, затем читает только файлы нужной области.

| Где | Для чего |
|---|---|
| `outputs` | Канонические C# исходники, рабочие EXE и карта маршрута |
| `dev/config` | Состав модулей, пробы, зависимости, SHA и документы |
| `dev/state` | Короткий контекст последнего состояния |
| `dev/builds`, `dev/runs` | Версии сборок, результаты, журналы и телеметрия |
| `dev/checkpoints`, `dev/archives`, `.git` | Сохранённые исправления и резервные копии |
| `work` | Копия штатных DLL, источники диагностик и материалы анализа |
| `work/native_distribution` | Полученный комплект штатных клиентов, редакторов и 3D-сервера |
| `work/reference_inputs` | Оригиналы режимки и DOS-архива |

Короткий вход для всех команд: `trainer.cmd -Action …`. Без параметров показывает Status. Задачи редактора также доступны в `Trainer.code-workspace`. Архивы, журналы, зависимости и дампы исключены из обычного поиска; история сохранена.

Открыть `Trainer.code-workspace` или `dev/Trainer.sln`. Канонические исходники доработок находятся в `outputs`, нативные модули — в копии `work/trenager_static/WORK_TRENAGER_3D`.

В `work/native_distribution` размещены шесть соседних папок полученного комплекта: `LAUNCH_CAM` — камера, `LAUNCH_DSP` — диспетчерский пульт, `LAUNCH_INSTR` — инструктор и редакторы сценариев, `LAUNCH_TREN` — сетевой тренажёр, `WORK_TRENAGER_3D` — полный клиент, `WORLD3DSERVER` — сервер 3D-мира. Состав и сравнение с прежней средой описаны в [`dev/config/native-distribution.json`](dev/config/native-distribution.json). Канонический runtime остаётся в `work/trenager_static/WORK_TRENAGER_3D`; комплект содержит другие версии DLL, старые пути и настройки локальной сети. Данные 3D-мира получены, но подключение, рендер и нативный запуск новых компонентов не проверены.

Карта разметки и взаимодействия — [`WORLD3D_ARCHITECTURE.md`](dev/docs/WORLD3D_ARCHITECTURE.md). В сетевом режиме сервер рассчитывает движение и тормоза, клиент передаёт пульт и тягу; видеомаршрут с наложенными объектами использует отдельный `VideoHandle.dll`. Серверные графы и сценарии присутствуют, но зарегистрированные приложения `Kornilcevo/main.exe` и `WorldSkils/main.exe` не найдены среди распакованных файлов. Текущий C# стенд имеет локальный расчёт по PDF-маршруту; явный мост к миру пока не реализован.

Вся среда описана в [`dev/environment-context.json`](dev/environment-context.json). Короткое текущее состояние для продолжения работы: локальный `dev/state/context.json`, создаваемый командой `trainer.cmd -Action Handoff` после восстановления среды; в Git этот файл не хранится. Команды автоматически обновляют контекст, включая ошибки.

Одна команда для сохранения проверенного исправления:

```powershell
.\dev\TrainerDev.ps1 -Action Fix -Name "имя-исправления" -Note "Причина и изменение" -Probe ColdNoSource,ColdTrainAuxiliary
```

`Fix` собирает версию, выполняет выбранные проверки и сохраняет исходники, описание исправления и контекст. Если проверка не прошла, её логи сохраняются, а commit исправления не создаётся. Без `-Probe` выполняется smoke-набор.

```powershell
.\dev\TrainerDev.ps1 -Action Doctor
.\dev\TrainerDev.ps1 -Action Run
.\dev\TrainerDev.ps1 -Action Archive -IncludeRuntime
```

`Run` требует закрыть уже работающий стенд и запускает проверяемый кандидат. Сборки с исходниками и SHA хранятся в `dev/builds`, журналы/телеметрия/результаты — в `dev/runs`, история исправлений — в Git и `dev/CHANGELOG.jsonl`. Старые накопленные файлы сохранены; автоматического удаления нет. `Archive -IncludeRuntime` сохраняет Git, код, результаты и весь `work` с нативными модулями и материалами анализа.

Для бота достаточно прочитать два JSON, выбрать нужные файлы и проверки. Все дампы и большие CSV читать заново не требуется. Обновить контекст вручную: `-Action Handoff`.

Подробности: [разработка](dev/docs/DEVELOPMENT.md), [модули и проверки](dev/docs/MODULES.md). Среда позволяет редактировать исходники оболочки и доработок; исходный код всех штатных Delphi DLL отсутствует, их поведение исследуется и исправляется защищёнными патчами в памяти.

Сведения о переносе и старых путях: [MIGRATION.md](dev/docs/MIGRATION.md). Старые файлы удалены по указанию пользователя; прежняя пустая папка пока занята процессом Windows. Основная работа теперь ведётся здесь; исходная установка `E:\WORK_TRENAGER_3D` не изменялась.

Регрессия подтверждает отдельные сценарии. Полная холодная зарядка ПМ, расход воздуха приводами и точная комплектация по РЭ №137+ ещё исследуются.
