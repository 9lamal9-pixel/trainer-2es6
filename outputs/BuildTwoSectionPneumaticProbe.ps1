$ErrorActionPreference = 'Stop'
$taskCompiler = 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe'
$taskNames = @('TrainerIntegrationHost','TrainerSessionLogging','TrainerNativeSystems','TrainerAuxSystems','TrainerRouteDOS','TrainerRouteView','TrainerNativeFixes','TrainerPneumaticTiming','TrainerAirNetwork','TrainerControlAir','TrainerBtoNetwork','TrainerAirSelector','TrainerKvtProfile','TrainerTwoSectionAirChain')
$taskSources = $taskNames | ForEach-Object { Join-Path $PSScriptRoot ($_ + '.cs') }
$taskProbe = Join-Path $PSScriptRoot '..\work\pneumatic_live_fix\analysis\NativeTwoBvrBtoProbe.cs'
$taskOutput = Join-Path $PSScriptRoot 'NativeTwoBvrBtoProbe.exe'
& $taskCompiler /nologo /platform:x86 /target:exe /main:NativeTwoBvrBtoProbe /r:System.Windows.Forms.dll /r:System.Drawing.dll /r:System.Web.Extensions.dll "/out:$taskOutput" $taskSources $taskProbe
if ($LASTEXITCODE -ne 0) { throw 'Не удалось собрать диагностическую пневмосеть.' }

