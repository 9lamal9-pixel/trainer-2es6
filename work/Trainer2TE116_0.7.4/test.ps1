<#
Fresh sequential checks of the instructor, scheme kernel, physical integration and UI.
#>
param([string]$Release = 'C:\Users\sibir\Desktop\Макрос\Тренажер\фантомас\work\Release')
$ErrorActionPreference = 'Stop'
$fw = 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319'
$sw = [Diagnostics.Stopwatch]::StartNew()
& (Join-Path $PSScriptRoot 'build.ps1') -Release $Release | Out-Host
$out = Join-Path $PSScriptRoot 'out'
$scn = Join-Path $Release 'RTS\scenaries\1_1.json'
$report = [Collections.Generic.List[string]]::new()
$code = 0
$cases = @(
    @{ Name='SchemeBench'; Args=@(); Refs=@(); Report='scheme_report.txt'; Window=$false },
    @{ Name='SchemeIntegrationBench'; Args=@(); Refs=@(); Report='scheme_integration_report.txt'; Window=$false },
    @{ Name='SchemeUi'; Args=@($out); Refs=@('System.Windows.Forms.dll','System.Drawing.dll','System.Web.Extensions.dll'); Report='scheme_ui_stdout.txt'; Window=$true },
    @{ Name='Bench'; Args=@($scn); Refs=@(); Report='bench_only.txt'; Window=$false },
    @{ Name='Release074Bench'; Args=@(); Refs=@(); Report='release074_report.txt'; Window=$false },
    @{ Name='Pneumatic074Bench'; Args=@(); Refs=@(); Report='pneumatic074_report.txt'; Window=$false },
    @{ Name='PultPulseBench'; Args=@(); Refs=@(); Report='pult_pulse_report.txt'; Window=$false },
    @{ Name='UiSmoke'; Args=@($out,$Release); Refs=@('System.Windows.Forms.dll','System.Drawing.dll'); Report='ui_report.txt'; Window=$true },
    @{ Name='PultTest'; Args=@((Join-Path $out 'Trainer2TE116.exe'),$scn,$out); Refs=@(); Report='pult_stdout.txt'; Window=$false },
    @{ Name='UiWalk'; Args=@($out,$Release); Refs=@('System.Windows.Forms.dll','System.Drawing.dll'); Report='walk_stdout.txt'; Window=$true }
)
foreach ($case in $cases) {
    $exe = Join-Path $out ($case.Name + '.exe')
    $source = Join-Path $PSScriptRoot ('test\' + $case.Name + '.cs')
    $refs = @('/r:' + (Join-Path $out 'Trainer2TE116.exe')) + @($case.Refs | ForEach-Object { '/r:' + $_ })
    $target = if ($case.Window) { '/target:winexe' } else { '/target:exe' }
    & "$fw\csc.exe" /nologo /platform:x64 /optimize+ /codepage:65001 $target @refs "/out:$exe" $source
    if ($LASTEXITCODE -ne 0) { throw ($case.Name + ' did not compile') }
    $stdout = Join-Path $out $case.Report
    Remove-Item -LiteralPath $stdout -Force -ErrorAction SilentlyContinue
    if ($case.Name -eq 'PultTest') { Remove-Item -LiteralPath (Join-Path $out 'pult_report.txt') -Force -ErrorAction SilentlyContinue }
    if ($case.Name -eq 'UiWalk') { Remove-Item -LiteralPath (Join-Path $out 'ui_walk.txt') -Force -ErrorAction SilentlyContinue }
    $processArgs = @{ FilePath=$exe; WindowStyle='Hidden'; PassThru=$true; RedirectStandardOutput=$stdout }
    if ($case.Args.Count -gt 0) { $processArgs.ArgumentList = @($case.Args | ForEach-Object { '"' + $_ + '"' }) }
    $p = Start-Process @processArgs
    $null = $p.Handle
    if (-not $p.WaitForExit(300000)) { $p.Kill(); throw ($case.Name + ' timeout 300 s') }
    $p.WaitForExit()
    $report.Add($case.Name + ' exit ' + $p.ExitCode)
    if ($p.ExitCode -ne 0) { $code = 1 }
    if (Test-Path -LiteralPath $stdout) { foreach ($line in Get-Content -LiteralPath $stdout -Encoding utf8) { $report.Add($line) } }
    if ($case.Name -eq 'PultTest' -and (Test-Path -LiteralPath (Join-Path $out 'pult_report.txt'))) { Get-Content (Join-Path $out 'pult_report.txt') -Encoding utf8 | ForEach-Object { $report.Add($_) } }
    if ($case.Name -eq 'UiWalk' -and (Test-Path -LiteralPath (Join-Path $out 'ui_walk.txt'))) { Get-Content (Join-Path $out 'ui_walk.txt') -Encoding utf8 | Select-Object -Last 2 | ForEach-Object { $report.Add($_) } }
}
$report | Out-File -Encoding utf8 (Join-Path $out 'bench_report.txt')
$report
"time $([int]$sw.Elapsed.TotalSeconds) s"
"exit $code"
