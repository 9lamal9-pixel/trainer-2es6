# Разработка тренажёра 2ЭС6

Все команды ниже выполняются из корня `e-launch-cam`. Редактируемые C# файлы остаются в `outputs`: проект использует ссылки на них, поэтому сборка проекта и командная сборка работают с одним набором исходников.

## Открыть проект

`Trainer.code-workspace` открывает рабочую папку в VS Code, если он установлен. В меню **Терминал → Выполнить задачу** доступны проверка зависимостей, сборка, тесты, запуск, сохранение исправления и архивирование. `dev/Trainer.sln` и `dev/TrainerIntegrationHost.csproj` позволяют открыть те же исходники как классический C# проект.

Проект предназначен для .NET Framework 4.0, WinForms, платформы **x86**. Нативные DLL тренажёра имеют 32-разрядный интерфейс. Ссылки проекта: System, System.Core, System.Windows.Forms, System.Drawing, System.Web.Extensions. Расширения редактора и отдельный SDK из сети автоматически не устанавливаются.

## Командная работа

```powershell
# Проверить компилятор, нативные модули и структуру файлов
.\dev\TrainerDev.ps1 -Action Doctor

# Собрать исправляемый кандидат пневмосети
.\dev\TrainerDev.ps1 -Action Build -Profile candidate

# Быстрая проверка и затем полный выбранный набор проверок
.\dev\TrainerDev.ps1 -Action Test -Suite smoke
.\dev\TrainerDev.ps1 -Action Test -Suite regression

# Запустить кандидат с отдельной сессией логирования
.\dev\TrainerDev.ps1 -Action Run -Profile candidate

# Сохранить конкретное исправление с пояснением
.\dev\TrainerDev.ps1 -Action Checkpoint -Name "tm-charge" -Note "Исправлена зарядка ТМ; указать результат проверок"

# Просмотреть состояние среды и создать переносимый архив
.\dev\TrainerDev.ps1 -Action Status
.\dev\TrainerDev.ps1 -Action Archive
```

Если запуск `.ps1` запрещён локальной политикой, используйте установленный Windows PowerShell:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\dev\TrainerDev.ps1 -Action Doctor
```

`-Profile integration` выбирает базовую сборку без экспериментальной сети; `candidate` является профилем по умолчанию. Идентификаторы отдельных проб и их ограничения находятся в каталоге проверок среды. Для изменения модели сначала сохраните исходное состояние через `Checkpoint`, затем внесите правку, соберите, выполните нужные проверки и сохраните новое состояние с описанием результата.

## Сборка из редактора

Классический проект можно проверить имеющимся MSBuild:

```powershell
& "$env:WINDIR\Microsoft.NET\Framework\v4.0.30319\MSBuild.exe" .\dev\Trainer.sln /t:Build /p:Configuration=Debug /p:Platform=x86 /m:1 /nr:false
& "$env:WINDIR\Microsoft.NET\Framework\v4.0.30319\MSBuild.exe" .\dev\TrainerIntegrationHost.csproj /t:Build /p:Configuration=Release /p:Platform=x86 /p:TrainerProfile=integration /m:1 /nr:false
```

Редактор пишет двоичные файлы и PDB в `dev/builds/editor/<profile>/<configuration>`, промежуточные файлы — в отдельную папку `obj` под `dev/builds/editor`. Рабочий запуск выполняйте через `TrainerDev.ps1 -Action Run`: путь загрузки нативных ресурсов рассчитан на размещение запускаемого EXE в `outputs`. Файлы в `dev/builds/editor` нужны для компиляции и навигации по проекту; журналируемые версии создаёт основной CLI в `dev/builds/<id>`.

На текущем компьютере проверены сборки `candidate Debug` и `integration Release`. MSBuild сообщает предупреждение MSB3644: отдельные справочные сборки SDK .NET Framework 4.0 отсутствуют, поэтому ссылки разрешаются из установленного GAC. Это позволяет собрать приложение здесь, но не подтверждает совместимость с чистой установкой исходного .NET 4.0. Основная сборка CLI использует уже имеющийся `csc.exe` и те же локальные библиотеки. Для переноса среды сверяйте результат `Doctor` на другом компьютере.

Конфигурация F5 не добавлена: наличие редактора и подходящего отладчика .NET Framework не проверено. Для диагностики среды доступны сохранённые журналы, телеметрия, результаты проб и PDB сборки редактора. Ошибка нативной DLL требует проверки разрядности, пути ресурсов, контрольной суммы DLL и последней записи журнала.

## Состав проекта

Общие восемь файлов: `TrainerIntegrationHost`, `TrainerNativeSystems`, `TrainerAuxSystems`, `TrainerRouteDOS`, `TrainerRouteView`, `TrainerNativeFixes`, `TrainerPneumaticTiming`, `TrainerSessionLogging`.

Кандидат добавляет семь файлов: `TrainerAirNetwork`, `TrainerControlAir`, `TrainerBtoNetwork`, `TrainerAirSelector`, `TrainerKvtProfile`, `TrainerTwoSectionAirChain`, `TrainerPneumaticNetworkCandidate`. Всего в кандидате **15 C# файлов**. `TrainerDebug.cs`, `TrainerStart.cs` и самостоятельные исходники диагностических проб не являются модулями этого приложения.

Исправления оригинальных DLL выполняются в памяти с проверкой ожидаемых байтов/контрольных сумм. Не подменяйте оригинальные DLL непроверенными версиями. Текущий кандидат содержит проверочную геометрию пневмосети; успешная компиляция и прохождение проб подтверждают описанные сценарии, а точность всех характеристик по РЭ для электровоза №137 и выше ещё проверяется.

## Python для анализа

`dev/config/python-tools.json` задаёт существующий интерпретатор и два локальных каталога библиотек: `work/traction6000/vendor` и `work/traction6000/electrobrake/pydeps`. `dev/tools/check_python.py` находит корень через собственный путь и работает из любой текущей папки.

```powershell
& 'C:\Users\sibir\AppData\Local\Programs\Python\Python313\python.exe' .\dev\tools\check_python.py
```

Вывод — одна JSON-строка с версией Python, версиями/путями пакетов и результатом проверки. `pefile` и `capstone` обязательны для анализа нативных DLL; проверка Capstone действительно декодирует x86-инструкцию. `Pillow`, `numpy`, `pypdf`, `pdfplumber` используются отдельными скриптами графики, численных расчётов и чтения PDF и отмечены как дополнительные. Проверка возвращает код 0, когда обязательные пакеты доступны, и код 1 при их отсутствии или неисправности; отсутствие дополнительного пакета только добавляет замечание.

Ни C# сборка, ни работа тренажёра не требуют Python. Проверка не устанавливает библиотеки из сети. Старый `work/compare137_configs/render_ocr_blok.py` использует отдельный `pypdfium2`; OCR и PDF-рендереры не включены в обязательные зависимости анализа DLL. Некоторые исторические Python-скрипты имеют пути от текущей папки: их запускайте из корня проекта, даже если сам проверяющий скрипт работает из любого места.
