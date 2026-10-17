$ErrorActionPreference='Stop'
$taskSources=@('TrainerAirNetwork','TrainerControlAir','TrainerBtoNetwork','TrainerAirSelector','TrainerKvtProfile','TrainerTwoSectionAirChain','TrainerSessionLogging') | ForEach-Object {Join-Path $PSScriptRoot ($_+'.cs')}
$taskProbe=Join-Path $PSScriptRoot '..\work\pneumatic_live_fix\analysis\UnifiedAirProbe.cs'
$taskExe=Join-Path $PSScriptRoot 'UnifiedAirProbe.exe'
& 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe' /nologo /r:System.Windows.Forms.dll /r:System.Web.Extensions.dll "/out:$taskExe" $taskSources $taskProbe
if($LASTEXITCODE -ne 0){throw 'Unified air build failed'}
& $taskExe
if($LASTEXITCODE -ne 0){throw 'Unified air validation failed'}
