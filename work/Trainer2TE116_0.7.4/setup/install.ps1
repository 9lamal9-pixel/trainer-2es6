<#
Установка новой программы инструктора 2ТЭ116 с флешки (запускается из «Установить.cmd»).
  install.ps1 [-Target C:\Trainer2TE116] [-Release <папка тренажёра>] [-Quiet] [-NoShortcuts] [-NoFirewall] [-NoStart]
Что делает:
  - проверяет Windows 64 бит и .NET Framework 4.5+;
  - копирует программу из ..\program в папку установки; при обновлении сохраняет data (поездки, свои поезда) и settings.json;
  - находит папку тренажёра (instructor.exe + RTS\scenaries): запущенный instructor.exe, прежние настройки, поиск по дискам;
  - показывает адрес пульта из Data\logic_config.json тренажёра (файл только читается);
  - по согласию добавляет правило брандмауэра для пакетов пульта (UDP 2001, только для этой программы; спросит права);
  - создаёт ярлыки на рабочем столе и в меню «Пуск», записывает install.json для удаления.
Папку тренажёра и instructor.exe установка не меняет и в неё ничего не пишет.
#>
param([string]$Target, [string]$Release, [switch]$Quiet, [switch]$NoShortcuts, [switch]$NoFirewall, [switch]$NoStart)
$ErrorActionPreference = 'Stop'
try { [Console]::OutputEncoding = [Text.Encoding]::UTF8 } catch { }
$RuleName = 'Trainer2TE116 - пульт кабины (UDP 2001)'
$LinkName = 'Инструктор 2ТЭ116 (новый).lnk'

function Say($t, $c = 'Gray') { Write-Host $t -ForegroundColor $c }
function Ask($q, $def) { if ($Quiet) { return $def }; $a = Read-Host "$q [$def]"; if ([string]::IsNullOrWhiteSpace($a)) { $def } else { $a.Trim().Trim('"') } }
function Yes($q, $def = $true) {
    if ($Quiet) { return $def }
    $a = Read-Host ("$q " + $(if ($def) { '(Д/н)' } else { '(д/Н)' }))
    if ([string]::IsNullOrWhiteSpace($a)) { return $def }
    return $a.Trim().ToLower() -in @('д', 'да', 'y', 'yes', 'l')
}
function IsTrainer($d) { try { return $d -and (Test-Path -LiteralPath (Join-Path $d 'RTS\scenaries')) } catch { return $false } }
function Stop-Here($t) { Say $t 'Red'; exit 1 }

$src = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\program'))
if (-not (Test-Path (Join-Path $src 'Trainer2TE116.exe'))) { Stop-Here "Не найдена папка program рядом с setup: $src" }
$ver = (Get-Item (Join-Path $src 'Trainer2TE116.exe')).VersionInfo.FileVersion
Say ''
Say '=== Установка новой программы инструктора 2ТЭ116 ===' 'Cyan'
Say 'Прежняя программа instructor.exe и папка тренажёра не меняются.'
Say ''

# ---- system
if (-not [Environment]::Is64BitOperatingSystem) { Stop-Here 'Нужна 64-битная Windows.' }
$net = 0
try { $net = (Get-ItemProperty 'HKLM:\SOFTWARE\Microsoft\NET Framework Setup\NDP\v4\Full' -ErrorAction Stop).Release } catch { }
if ($net -lt 378389) { Stop-Here 'Нужен .NET Framework 4.5 или новее (на Windows 10/11 он уже есть). Установите его и повторите.' }
Say "Windows 64 бит, .NET Framework 4 (выпуск $net) — подходит." 'Green'

# ---- target
if (-not $Target) { $Target = Ask 'Папка установки' 'C:\Trainer2TE116' }
$Target = [IO.Path]::GetFullPath($Target)
if (Test-Path (Join-Path $Target 'instructor.exe')) { Stop-Here "В папке $Target лежит instructor.exe — это папка тренажёра. Выберите отдельную папку." }
if ($Target.StartsWith($src, [StringComparison]::OrdinalIgnoreCase)) { Stop-Here 'Папка установки не может быть внутри пакета.' }
$running = Get-Process Trainer2TE116 -ErrorAction SilentlyContinue | Where-Object { try { $_.Path -and $_.Path.StartsWith($Target, [StringComparison]::OrdinalIgnoreCase) } catch { $false } }
while ($running) {
    if (-not (Yes 'Программа из этой папки запущена. Закройте её и нажмите Enter (н — отмена)')) { Stop-Here 'Установка отменена.' }
    $running = Get-Process Trainer2TE116 -ErrorAction SilentlyContinue | Where-Object { try { $_.Path -and $_.Path.StartsWith($Target, [StringComparison]::OrdinalIgnoreCase) } catch { $false } }
}
$update = Test-Path (Join-Path $Target 'Trainer2TE116.exe')
$old = @{}
$settingsFile = Join-Path $Target 'settings.json'
if (Test-Path $settingsFile) { try { (Get-Content $settingsFile -Raw -Encoding UTF8 | ConvertFrom-Json).psobject.Properties | ForEach-Object { $old[$_.Name] = $_.Value } } catch { } }

# ---- folder of the trainer
$found = New-Object System.Collections.Generic.List[string]
function Add-Found($d) { if ($d -and (IsTrainer $d)) { $f = [IO.Path]::GetFullPath($d); if (-not ($found | Where-Object { $_ -ieq $f })) { $found.Add($f) } } }
if ($Release) { if (-not (IsTrainer $Release)) { Stop-Here "В $Release нет RTS\scenaries." }; Add-Found $Release }
else {
    if ($old.ContainsKey('release_dir')) { Add-Found $old['release_dir'] }
    Get-Process instructor -ErrorAction SilentlyContinue | ForEach-Object { try { Add-Found (Split-Path $_.Path) } catch { } }
    if ($found.Count -eq 0) {
        Say 'Ищу папку тренажёра (instructor.exe и RTS) на дисках, это займёт до пары минут…'
        # breadth-first, up to 8 levels, without the system and the heavy folders
        $skip = @('Windows', '$Recycle.Bin', 'System Volume Information', 'Recovery', 'PerfLogs', 'AppData', 'WindowsApps', 'node_modules', '.git', 'Microsoft', 'Temp', 'Config.Msi', 'ProgramData', 'MSOCache', 'Intel', 'AMD', 'NVIDIA', 'Package Cache', '.claude', 'DECOMPILED')
        $deadline = (Get-Date).AddMinutes(3)
        foreach ($drv in [IO.DriveInfo]::GetDrives() | Where-Object { $_.IsReady -and $_.DriveType -in 'Fixed', 'Removable' }) {
            $level = @($drv.RootDirectory.FullName)
            for ($depth = 0; $depth -le 8 -and $level.Count -gt 0 -and (Get-Date) -lt $deadline; $depth++) {
                $next = New-Object System.Collections.Generic.List[string]
                foreach ($d in $level) {
                    if ([IO.File]::Exists((Join-Path $d 'instructor.exe'))) { Add-Found $d }
                    try { foreach ($c in [IO.Directory]::EnumerateDirectories($d)) { if ((Split-Path $c -Leaf) -notin $skip) { $next.Add($c) } } } catch { }
                }
                $level = $next
            }
        }
        if ((Get-Date) -ge $deadline) { Say 'Поиск остановлен через 3 минуты.' 'Yellow' }
    }
}
$rel = ''
if ($found.Count -eq 1) { $rel = $found[0]; Say "Папка тренажёра: $rel" 'Green' }
elseif ($found.Count -gt 1) {
    Say 'Найдено несколько папок тренажёра:'
    for ($i = 0; $i -lt $found.Count; $i++) { Say ("  {0}. {1}" -f ($i + 1), $found[$i]) }
    $n = [int](Ask 'Номер нужной' '1')
    if ($n -lt 1 -or $n -gt $found.Count) { $n = 1 }
    $rel = $found[$n - 1]
}
else {
    Say 'Папка тренажёра не найдена.' 'Yellow'
    $p = Ask 'Укажите её (где instructor.exe и RTS), Enter — спросить при первом запуске' ''
    if ($p) { if (IsTrainer $p) { $rel = [IO.Path]::GetFullPath($p) } elseif (IsTrainer (Join-Path $p 'work\Release')) { $rel = [IO.Path]::GetFullPath((Join-Path $p 'work\Release')) } else { Say "В $p нет RTS\scenaries — папка будет спрошена при первом запуске." 'Yellow' } }
}

# ---- copy
New-Item -ItemType Directory -Force $Target | Out-Null
Get-ChildItem -LiteralPath $src -Force | Where-Object { $_.Name -ne 'settings.json' -and $_.Name -ne 'data' } | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $Target -Recurse -Force }
$s = [ordered]@{ release_dir = ''; pult = 'auto'; pult_local = 2001; pult_swap = 1 }
try { (Get-Content (Join-Path $src 'settings.json') -Raw -Encoding UTF8 | ConvertFrom-Json).psobject.Properties | ForEach-Object { $s[$_.Name] = $_.Value } } catch { }
foreach ($k in @($old.Keys)) { $s[$k] = $old[$k] }
if ($rel) { $s['release_dir'] = $rel }
[IO.File]::WriteAllText($settingsFile, ($s | ConvertTo-Json), (New-Object System.Text.UTF8Encoding($false)))
New-Item -ItemType Directory -Force (Join-Path $Target 'data') | Out-Null
Say ("Программа {0} в {1}." -f $(if ($update) { 'обновлена' } else { 'установлена' }), $Target) 'Green'

# ---- pult
$pultAddr = ''
if ($rel -and (Test-Path (Join-Path $rel 'Data\logic_config.json'))) {
    try {
        $lc = Get-Content (Join-Path $rel 'Data\logic_config.json') -Raw | ConvertFrom-Json
        $pultAddr = "$($lc.ip_pult):$($lc.port_pult)"
        Say ("Пульт кабины (из logic_config.json тренажёра): {0}, {1}" -f $pultAddr, $(if ($lc.is_active_pult -eq 1) { 'включён' } else { 'выключен в настройках тренажёра' }))
    } catch { Say 'logic_config.json тренажёра не читается — пульт можно задать в settings.json.' 'Yellow' }
}
if ($s['pult'] -ne 'auto') { Say "В settings.json пульт задан явно: $($s['pult'])" }

# ---- firewall
$fw = $false
if (-not $NoFirewall -and (Yes 'Разрешить в брандмауэре пакеты пульта для новой программы (UDP 2001)?')) {
    $exe = Join-Path $Target 'Trainer2TE116.exe'
    $cmd = "Remove-NetFirewallRule -DisplayName '$RuleName' -ErrorAction SilentlyContinue; New-NetFirewallRule -DisplayName '$RuleName' -Direction Inbound -Program '$exe' -Protocol UDP -LocalPort 2001 -Action Allow -Profile Any | Out-Null"
    $admin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    try {
        if ($admin) { Invoke-Expression $cmd }
        else { Say 'Нужны права администратора — Windows спросит разрешение.'; Start-Process powershell -Verb RunAs -Wait -ArgumentList '-NoProfile', '-Command', $cmd }
        $fw = [bool](Get-NetFirewallRule -DisplayName $RuleName -ErrorAction SilentlyContinue)
    } catch { }
    if ($fw) { Say 'Правило брандмауэра добавлено.' 'Green' } else { Say 'Правило не добавлено. При первом запуске Windows может спросить разрешение для сети — разрешите.' 'Yellow' }
}

# ---- shortcuts
$links = @()
if (-not $NoShortcuts) {
    $sh = New-Object -ComObject WScript.Shell
    foreach ($dir in @([Environment]::GetFolderPath('Desktop'), [Environment]::GetFolderPath('Programs'))) {
        $lnk = Join-Path $dir $LinkName
        $l = $sh.CreateShortcut($lnk)
        $l.TargetPath = Join-Path $Target 'Trainer2TE116.exe'; $l.WorkingDirectory = $Target
        $l.Description = 'Новая программа инструктора тренажёра 2ТЭ116'; $l.IconLocation = (Join-Path $Target 'Trainer2TE116.exe') + ',0'
        $l.Save(); $links += $lnk
    }
    Say 'Ярлыки: рабочий стол и меню «Пуск» — «Инструктор 2ТЭ116 (новый)».' 'Green'
}

$info = [ordered]@{ version = $ver; installed = (Get-Date).ToString('s'); target = $Target; release_dir = $rel; shortcuts = $links; firewall_rule = $(if ($fw) { $RuleName } else { '' }) }
[IO.File]::WriteAllText((Join-Path $Target 'install.json'), ($info | ConvertTo-Json), (New-Object System.Text.UTF8Encoding($false)))

Say ''
Say 'Готово.' 'Cyan'
Say ' - Перед работой с пультом закройте instructor.exe: пульт работает только с одной программой.'
Say " - Проверка без пульта: «Проверка пульта.cmd» в $Target."
Say " - Удаление: «Удалить.cmd» в $Target."
if (-not $rel) { Say ' - Папку тренажёра программа спросит при первом запуске.' 'Yellow' }
if (-not $NoStart -and (Yes 'Запустить программу сейчас?' $false)) { Start-Process (Join-Path $Target 'Trainer2TE116.exe') -WorkingDirectory $Target }
exit 0
