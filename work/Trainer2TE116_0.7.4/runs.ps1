<#
Прогоны новой программы по участку с автомашинистом: 4 встроенных поезда параллельно, отчёты out\runs\<n>\ (кассета, CSV, ВУ-45,
ДУ-61, протокол, лента) и сводка out\runs\summary.txt.
#>
param([string]$Release = 'C:\Users\sibir\Desktop\Макрос\Тренажер\фантомас\work\Release', [string]$Scenario = '1_1.json', [int[]]$Trains = @(0, 1, 2, 3))
$ErrorActionPreference = 'Stop'
$exe = Join-Path $PSScriptRoot 'out\Trainer2TE116.exe'
$runs = Join-Path $PSScriptRoot 'out\runs'
New-Item -ItemType Directory -Force $runs | Out-Null
$scen = Join-Path $Release ('RTS\scenaries\' + $Scenario)
$procs = foreach ($n in $Trains) {
    $dir = Join-Path $runs $n
    New-Item -ItemType Directory -Force $dir | Out-Null
    Start-Process -FilePath $exe -ArgumentList @('--run', '--scenario', "`"$scen`"", '--train', "builtin:$n", '--out', "`"$dir`"", '--release', "`"$Release`"") `
        -RedirectStandardOutput (Join-Path $dir 'run.txt') -NoNewWindow -PassThru
}
$procs | Wait-Process
$summary = foreach ($n in $Trains) { Get-Content (Join-Path $runs "$n\run.txt") -Encoding utf8 | Where-Object { $_ -like 'RUN *' -or $_ -like '  VIOLATION*' } }
$summary | Set-Content -Encoding utf8 (Join-Path $runs 'summary.txt')
$summary
