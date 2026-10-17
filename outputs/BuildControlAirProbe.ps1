$ErrorActionPreference='Stop'
$taskSources=@('TrainerAirNetwork.cs','TrainerControlAir.cs','TrainerSessionLogging.cs') | ForEach-Object {Join-Path $PSScriptRoot $_}
$taskProbe=Join-Path $PSScriptRoot '..\work\pneumatic_live_fix\analysis\ControlAirProbe.cs'
$taskExe=Join-Path $PSScriptRoot 'ControlAirProbe.exe'
& 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe' /nologo /r:System.Windows.Forms.dll /r:System.Web.Extensions.dll "/out:$taskExe" $taskSources $taskProbe
if($LASTEXITCODE -ne 0){throw 'Control-air probe build failed'}
& $taskExe
if($LASTEXITCODE -ne 0){throw 'Control-air validation failed'}
