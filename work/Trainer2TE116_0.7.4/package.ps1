<#
Готовый билд новой программы инструктора для переноса на флешке: out\dist\Trainer2TE116_<версия>\ и такой же .zip.
  .\package.ps1                  # сборка программы, теста пульта и пакета
  .\package.ps1 -SkipTests       # без прогона test.ps1 перед упаковкой
  .\package.ps1 -NoTestCode      # проверки прогоняются, но в пакет идёт программа без проверочного кода и без PultTest.exe
В пакете:
  Установить.cmd            установка в C:\Trainer2TE116 (или свою папку), поиск папки тренажёра, ярлыки
  Запустить с флешки.cmd    работа без установки, данные пишутся на флешку
  ПРОЧТИ.txt                короткая инструкция
  program\                  программа, SQLite, settings.json, проверка пульта (PultTest.exe), удаление
  setup\                    install.ps1, uninstall.ps1
Установленная программа тренажёра (instructor.exe, его папки и база) не меняется: из неё только читается.
#>
param([string]$Release = 'C:\Users\sibir\Desktop\Макрос\Тренажер\фантомас\work\Release', [switch]$SkipTests, [switch]$NoTestCode)
$ErrorActionPreference = 'Stop'
$fw = 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319'
$root = $PSScriptRoot
$out = Join-Path $root 'out'
if ($SkipTests) { & (Join-Path $root 'build.ps1') -Release $Release | Out-Host }
else {
    $t = & (Join-Path $root 'test.ps1') -Release $Release
    $t | Select-String 'RESULT|PASS window|PULT|WALK|^exit' | Out-Host
    if (($t | Select-Object -Last 1) -ne 'exit 0') { throw 'проверки не прошли — пакет не собирается' }
}
& "$fw\csc.exe" /nologo /platform:x64 /optimize+ /codepage:65001 "/r:$out\Trainer2TE116.exe" "/out:$out\PultTest.exe" (Join-Path $root 'test\PultTest.cs')
if ($LASTEXITCODE -ne 0) { throw 'PultTest.cs did not compile' }
# the checks ran on the full program; the package gets the program compiled without the checking code
if ($NoTestCode) { & (Join-Path $root 'build.ps1') -Release $Release -Define NOTEST | Out-Host }

$ver = [regex]::Match((Get-Content (Join-Path $root 'src\Program.cs') -Raw -Encoding UTF8), 'Version = "([0-9.]+)').Groups[1].Value
$name = "Trainer2TE116_$ver"
$dist = Join-Path $out 'dist'
$pkg = Join-Path $dist $name
if (Test-Path -LiteralPath $pkg) {
    $resolvedPackage = (Resolve-Path -LiteralPath $pkg).Path
    $resolvedDist = [IO.Path]::GetFullPath($dist).TrimEnd('\') + '\'
    if (-not $resolvedPackage.StartsWith($resolvedDist, [StringComparison]::OrdinalIgnoreCase)) { throw 'Package path outside dist' }
    Remove-Item -LiteralPath $resolvedPackage -Recurse -Force
}
$prog = Join-Path $pkg 'program'
New-Item -ItemType Directory -Force $prog, (Join-Path $prog 'x64'), (Join-Path $pkg 'setup') | Out-Null

Copy-Item "$out\Trainer2TE116.exe", "$out\System.Data.SQLite.dll" $prog
Copy-Item "$out\x64\SQLite.Interop.dll" (Join-Path $prog 'x64')
if (-not $NoTestCode) { Copy-Item "$out\PultTest.exe" $prog }
Copy-Item -LiteralPath (Join-Path $out 'assets') -Destination $prog -Recurse -Force
Copy-Item (Join-Path $root 'README.md') $prog
foreach ($note in @('RELEASE_NOTES_0.7.4.md', 'module_integration_notes.md', 'traction_integration_notes.md', 'pult_pulse_integration_notes.md', 'VALIDATION_0.7.4.md')) {
    $notePath = Join-Path $root $note
    if (Test-Path -LiteralPath $notePath) { Copy-Item -LiteralPath $notePath -Destination $prog }
}
$schemes = Join-Path $root '..\..\docs\fantomas\SCHEMES_2TE116.md'
if (Test-Path $schemes) { Copy-Item $schemes $prog }
$schemeGuide = Join-Path $root '..\..\docs\fantomas\SCHEME_IMPLEMENTATION.md'
if (Test-Path -LiteralPath $schemeGuide) { Copy-Item -LiteralPath $schemeGuide -Destination $prog }
Copy-Item (Join-Path $root 'setup\install.ps1'), (Join-Path $root 'setup\uninstall.ps1') (Join-Path $pkg 'setup')
Copy-Item (Join-Path $root 'setup\uninstall.ps1') $prog
# settings: the folder of the trainer is found at the install or at the first start; the pult from logic_config.json
$utf8 = New-Object System.Text.UTF8Encoding($false)
[IO.File]::WriteAllText((Join-Path $prog 'settings.json'), "{`r`n  `"release_dir`": `"`",`r`n  `"pult`": `"auto`",`r`n  `"pult_local`": 2001,`r`n  `"pult_swap`": 1,`r`n  `"video`": `"auto`",`r`n  `"blok`": `"auto`",`r`n  `"road`": `"auto`",`r`n  `"diesel_condition`": `"normal`",`r`n  `"log`": `"on`"`r`n}", $utf8)

$oem = [Text.Encoding]::GetEncoding(866)
function Cmd($file, $text) { [IO.File]::WriteAllText((Join-Path $pkg $file), $text, $oem) }
Cmd 'Установить.cmd' "@echo off`r`npowershell -NoProfile -ExecutionPolicy Bypass -File `"%~dp0setup\install.ps1`" %*`r`npause`r`n"
Cmd 'Запустить с флешки.cmd' "@echo off`r`nstart `"`" `"%~dp0program\Trainer2TE116.exe`"`r`n"
if (-not $NoTestCode) { [IO.File]::WriteAllText((Join-Path $prog 'Проверка пульта.cmd'), "@echo off`r`nchcp 65001 >nul`r`n`"%~dp0PultTest.exe`" `"%~dp0Trainer2TE116.exe`" auto `"%TEMP%\Trainer2TE116_check`"`r`necho.`r`necho Report: %TEMP%\Trainer2TE116_check\pult_report.txt`r`npause`r`n", $oem) }
[IO.File]::WriteAllText((Join-Path $prog 'Удалить.cmd'), "@echo off`r`npowershell -NoProfile -ExecutionPolicy Bypass -File `"%~dp0uninstall.ps1`"`r`npause`r`n", $oem)

$readme = @"
НОВАЯ ПРОГРАММА ИНСТРУКТОРА 2ТЭ116, версия $ver

ОБНОВЛЕНИЕ 0.7.4
Интеграция модулей тяги, пневматики и журнала из предоставленного архива.
Исправлены совместимость с проектом, расчёт числа осей и состояния диагностики.
Изменения и ограничения: program\RELEASE_NOTES_0.7.4.md.
Это инженерная модель; паспортная точность и работа с живым оборудованием не подтверждены.

Работает вместо прежней программы instructor.exe, но ничего в ней не меняет:
сценарии, составы и картинки знаков только читаются из папки тренажёра.
Свои поезда, поездки, кассеты, журналы и документы пишутся в папку программы (data).

УСТАНОВКА С ФЛЕШКИ
1. Закройте прежнюю программу инструктора (instructor.exe).
2. Запустите «Установить.cmd» и ответьте на вопросы (Enter — вариант по умолчанию):
   - папка установки, по умолчанию C:\Trainer2TE116;
   - папка тренажёра ищется сама (где instructor.exe и RTS), при нескольких — выбор;
   - правило брандмауэра для пакетов пульта (нужны права администратора).
3. На рабочем столе появится ярлык «Инструктор 2ТЭ116 (новый)».
Повторная установка обновляет программу и сохраняет поездки и настройки.

БЕЗ УСТАНОВКИ
«Запустить с флешки.cmd». При первом запуске программа сама найдёт папку тренажёра
или спросит её. Данные будут писаться на флешку.

ПУЛЬТ КАБИНЫ
Адрес пульта берётся из Data\logic_config.json тренажёра (как у instructor.exe), свой порт 2001.
Пульт может работать только с одной программой: пока открыт instructor.exe, новая программа
предупредит и предложит его закрыть. Состояние связи — в шапке окна («Пульт: на связи»).
Другие настройки — program\settings.json: pult = auto | off | ip:port, pult_local, pult_swap,
log = on | raw | off (журнал работы, см. ниже).

ПРОВЕРКА
«Проверка пульта.cmd» в папке программы запускает программу с эмулятором пульта на этом
компьютере (настоящие пульт, видео и БЛОК не трогаются) и проверяет 48 реакций: краны, ЭПК, тягу,
дизели, обмен с видеосистемой и БЛОК.
Должно быть «PULT PASS».

ПНЕВМАТИКА, ОБНОВЛЕНИЕ 13.11.2026
- Главные резервуары — 4 по 250 л на секцию (2000 л на тепловоз), как на схеме воздухопровода.
- Питательный резервуар 250 л на секцию за обратным клапаном 17: от него через реле давления 304
  наполняются тормозные цилиндры тепловоза. При упавшем давлении в ГР он воздух назад не отдаёт,
  поэтому тепловоз можно затормозить краном 254 и при пустых ГР. Давление — раздел «Локомотив».
- Плотность ТЦ тепловоза (Правила, п. 4.1, смена кабин): кран 254 в последнем тормозном положении,
  ТМ разряжена краном 395 до нуля, кран к ТЦ перекрыт — давление в ТЦ снижается примерно
  на 0,1 кгс/см² за минуту (норма — не более 0,2). При открытом питании реле давления утечку восполняет.
- В журнал нарушений пишется запись D1, если продольная сила в автосцепке больше 1275 кН (130 тс).

ДАВЛЕНИЯ (ВЕРСИЯ 0.7.2)
Все давления на экране инструктора, в схемах, графиках, журнале нарушений, протоколе и журнале работы показаны
в кгс/см² (атмосферах), как на манометрах пульта: ТМ, УР, ГР, ПР, ТЦ, ЗР, давление масла.

ЖУРНАЛ РАБОТЫ ПРОГРАММЫ И СВЯЗИ С ПУЛЬТОМ (ВЕРСИЯ 0.7.2)
При каждом запуске программа пишет текстовый журнал: data\logs\trainer_<дата>_<время>.log в папке программы
(после установки — C:\Trainer2TE116\data\logs). Его можно открыть Блокнотом, в том числе во время работы.
В журнале:
- ПРОГРАММА — версия, ключи запуска, settings.json, logic_config.json, папки, сетевые адреса компьютера,
  каждые 10 с — число тактов и наибольшая длительность такта;
- ПУЛЬТ — настройки и открытие порта 2001, первый пакет и адрес отправителя, отброшенные пакеты (не та длина),
  появление и потеря связи, ошибки приёма и отправки, каждые 10 с — пакетов в секунду и наибольший перерыв приёма;
- ПУЛЬТ ВХОД — каждое изменение органа на пульте: номер ячейки, название, было → стало;
- ПУЛЬТ ВЫХ — что программа отправляет на приборы: зажигание и гашение каждой лампы, коды стрелок (раз в 5 с);
- ВИДЕО, БЛОК — состояние связи, ответы БЛОК;
- ПОЕЗДКА, СОБЫТИЕ, МОДЕЛЬ — начало и конец поездки, события поездки, каждые 10 с скорость, органы, давления, дизели;
- ТОРМОЗА — каждое изменение положения кранов 395 и 254, ЭПК, компрессоров, лампы «ТМ»; состояние каждого вагона
  (заряжен, торможение, экстренное, отпуск, ВР выключен) при его изменении; время тормозной волны и отпуска по поезду;
  итог каждого торможения (скорость, путь, время, глубина разрядки, давление в ТЦ, тормозная сила, силы в автосцепках);
  давления ГР, ПМ, УР, ТМ в голове и хвосте, ТЦ, подпитка ТМ — каждые 2 с при действии тормозов и каждые 30 с в покое;
  таблица всех вагонов ТМ/ЗР/ТЦ — каждые 6 с при действии тормозов и раз в минуту в покое; пробы тормозов;
- НЕШТАТКА — каждая введённая и снятая инструктором неисправность: что она делает в модели и состояние поезда
  в этот момент; каждые 10 с — список действующих неисправностей, дизели и лампы пульта; обрыв ТМ и автосцепки,
  срыв ЭПК, аварийная остановка, срабатывание защит, дефекты электрической схемы, препятствия, столкновения;
- НАРУШЕНИЕ — записи журнала нарушений с баллами;
- ОШИБКА — непредвиденные ошибки программы (они же в data\errors.log).
settings.json, "log": "on" — как описано; "raw" — ещё и пакеты пульта целиком в hex при каждом изменении;
"off" — журнал не ведётся. Хранятся 20 последних журналов. Если пульт «не на связи» — приложите журнал к обращению.

ДИЗЕЛЬ, ОБНОВЛЕНИЕ 14.11.2026 (ВЕРСИЯ 0.7.2)
- Мощность по позициям — по реостатным испытаниям 2ТЭ116: на 2–9-й позициях тяга заметно больше прежней
  (генератор 280 кВт на 2-й, 550 на 5-й, 1100 на 9-й, 1920 на 15-й).
- Расход топлива: 200 г/кВт·ч на полной мощности (около 450 кг/ч на секцию), 15 кг/ч на холостом ходу.
- Дизель набирает обороты как серийный с регулятором 4-7РС2: с 350 до 1000 об/мин около 40 с,
  полная мощность через 55 с после набора 15-й позиции, сброс до 350 об/мин около 19 с.
  Позиции набирать с выдержкой: на одну позицию обороты выходят за 3–5 с.

ЦЕПИ ПО СХЕМЕ 2ТЭ116 (ВАРИАНТ 008), НОВОЕ В ВЕРСИИ 0.6
Цепи пуска, возбуждения, тяги, холодильника, компрессора и ламп работают по схеме.
Описание каждой схемы и сверка с пультом — program\SCHEMES_2TE116.md.
Что заметит машинист:
- реверсивная рукоятка должна стоять «Вперёд» или «Назад»: в нейтрали не горят лампы «Заторможен»,
  «Давление масла», «Уровень воды», «РЗ», не действуют аварийная кнопка и холодильник;
- аварийная остановка сбрасывается реверсивной рукояткой в нейтраль, а не тумблером «Управление тепловозом»;
- пуск дизеля около 72 с (60 с прокачка, 12 с прокрутка);
- в ручном режиме холодильника вентилятор 2 включается только после 1-го, 3-й — после 4-го;
- после срабатывания защиты по скорости (110 км/ч) контроллер нужно поставить на ноль.
- с краном 254 в 3-м и 4-м положении (до 2,2 кгс/см² в ТЦ) тяга набирается — для плавного трогания; с 5-го снимается;
- «Контроль ламп» зажигает три лампы: «Отпуск тормозов», «Давление масла», «Уровень воды».
Лампы пульта показаны в окне инструктора (раздел «Локомотив», карточка «Кабина»). Щелчок по лампе
«пережигает» её: машинист должен найти это проверкой ламп. Повторный щелчок — лампа заменена.
Автоматы, рубильники, отключатели моторов и двери ВВК — в окне инструктора, раздел «Локомотив»,
карточка «ВВК и автоматы»: инструктор работает ими за помощника машиниста.

УДАЛЕНИЕ
«Удалить.cmd» в папке программы: ярлыки, правило брандмауэра и папка; поездки можно оставить.
Прежняя программа instructor.exe остаётся как была — к ней можно вернуться в любой момент.

ВИДЕО И БЛОК
Адреса берутся из Data\logic_config.json тренажёра. Номер видеомаршрута подбирается под выбранный
сценарий (по разметкам RTS\scenaries\root), если не найден — из RTS\start_markup_data.json.
Поездка начинается с места start_coord сценария. Тумблеры пульта, включая «Топливный насос»,
берутся с пульта с первого пакета: при выключенном насосе дизель в начале поездки не запущен.
Свои значения — в settings.json: video, blok = auto | off | ip:port, road = номер маршрута.
Состояние дизелей: diesel_condition = nominal | normal | worn | random | процент.

ЧТО НЕ ПРОВЕРЕНО
Обмен с пультом, видеосистемой и БЛОК проверен эмуляторами на компьютере разработки.
На живом оборудовании — проверить при первой установке: в шапке окна должно быть
«Пульт: есть · Видео: есть · БЛОК: есть».
Нужны Windows 64 бит и .NET Framework 4.5 или новее (на Windows 10/11 есть).
"@
if ($NoTestCode) {
    $readme = [regex]::Replace($readme, '(?s)ПРОВЕРКА\r?\n«Проверка пульта\.cmd».*?Должно быть «PULT PASS»\.\r?\n\r?\n', '')
    $readme = $readme.Replace('программа, SQLite, settings.json, проверка пульта (PultTest.exe), удаление', 'программа, SQLite, settings.json, удаление')
}
[IO.File]::WriteAllText((Join-Path $pkg 'ПРОЧТИ.txt'), $readme, (New-Object System.Text.UTF8Encoding($true)))

$zip = Join-Path $dist "$name.zip"
if (Test-Path $zip) { Remove-Item $zip }
Add-Type -AssemblyName System.IO.Compression.FileSystem
[IO.Compression.ZipFile]::CreateFromDirectory($pkg, $zip, [IO.Compression.CompressionLevel]::Optimal, $true)
Get-ChildItem -Recurse $pkg | Where-Object { -not $_.PSIsContainer } | ForEach-Object { '{0,-60} {1,9}' -f $_.FullName.Substring($pkg.Length + 1), $_.Length }
"$zip  $((Get-Item $zip).Length) bytes"
