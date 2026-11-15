<#
Удаление новой программы инструктора 2ТЭ116 («Удалить.cmd» в папке программы).
  uninstall.ps1 [-Target <папка программы>] [-Quiet] [-KeepData|-RemoveData]
Убирает ярлыки и правило брандмауэра из install.json и файлы программы. Поездки и свои поезда (data) и settings.json
по умолчанию остаются. Папку тренажёра и instructor.exe не трогает: в папке с instructor.exe удаление не работает.
#>
param([string]$Target = $PSScriptRoot, [switch]$Quiet, [switch]$KeepData, [switch]$RemoveData)
$ErrorActionPreference = 'Stop'
try { [Console]::OutputEncoding = [Text.Encoding]::UTF8 } catch { }
function Say($t, $c = 'Gray') { Write-Host $t -ForegroundColor $c }
function Yes($q, $def = $true) {
    if ($Quiet) { return $def }
    $a = Read-Host ("$q " + $(if ($def) { '(Д/н)' } else { '(д/Н)' }))
    if ([string]::IsNullOrWhiteSpace($a)) { return $def }
    return $a.Trim().ToLower() -in @('д', 'да', 'y', 'yes', 'l')
}
$Target = [IO.Path]::GetFullPath($Target)
if (-not (Test-Path (Join-Path $Target 'Trainer2TE116.exe'))) { Say "В $Target нет Trainer2TE116.exe — это не папка новой программы." 'Red'; exit 1 }
if (Test-Path (Join-Path $Target 'instructor.exe')) { Say "В $Target лежит instructor.exe — папка тренажёра не удаляется." 'Red'; exit 1 }
Say "Удаление новой программы инструктора из $Target" 'Cyan'
if (-not $Quiet -and -not (Yes 'Удалить?' $false)) { Say 'Отменено.'; exit 0 }
while (Get-Process Trainer2TE116 -ErrorAction SilentlyContinue | Where-Object { try { $_.Path.StartsWith($Target, [StringComparison]::OrdinalIgnoreCase) } catch { $false } }) {
    if (-not (Yes 'Программа запущена. Закройте её и нажмите Enter (н — отмена)')) { exit 1 }
}
$keep = if ($RemoveData) { $false } elseif ($KeepData) { $true } else { Yes 'Оставить поездки, кассеты и свои поезда (папка data) и настройки?' }

$info = $null
try { $info = Get-Content (Join-Path $Target 'install.json') -Raw -Encoding UTF8 | ConvertFrom-Json } catch { }
if ($info) {
    foreach ($l in $info.shortcuts) { if ($l -and (Test-Path -LiteralPath $l)) { Remove-Item -LiteralPath $l -Force; Say "Ярлык удалён: $l" } }
    if ($info.firewall_rule) {
        $cmd = "Remove-NetFirewallRule -DisplayName '$($info.firewall_rule)' -ErrorAction SilentlyContinue"
        $admin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
        try { if ($admin) { Invoke-Expression $cmd } else { Start-Process powershell -Verb RunAs -Wait -ArgumentList '-NoProfile', '-Command', $cmd } } catch { }
        if (Get-NetFirewallRule -DisplayName $info.firewall_rule -ErrorAction SilentlyContinue) { Say 'Правило брандмауэра осталось — удалите его вручную.' 'Yellow' } else { Say 'Правило брандмауэра удалено.' }
    }
}
Set-Location $env:TEMP
Get-ChildItem -LiteralPath $Target -Force | Where-Object { -not ($keep -and $_.Name -in @('data', 'settings.json')) } | ForEach-Object { Remove-Item -LiteralPath $_.FullName -Recurse -Force -ErrorAction SilentlyContinue }
if (-not $keep) { Remove-Item -LiteralPath $Target -Recurse -Force -ErrorAction SilentlyContinue }
Say $(if ($keep) { "Программа удалена, данные остались в $Target\data." } else { 'Программа и её данные удалены.' }) 'Green'
Say 'Прежняя программа instructor.exe не менялась.'
exit 0
