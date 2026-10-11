$ErrorActionPreference = 'Stop'
$taskNames = @('TrainerIntegrationHost','TrainerSessionLogging','TrainerNativeSystems','TrainerAuxSystems','TrainerRouteDOS','TrainerRouteView','TrainerNativeFixes','TrainerPneumaticTiming','TrainerAirNetwork','TrainerControlAir','TrainerBtoNetwork','TrainerAirSelector','TrainerKvtProfile','TrainerTwoSectionAirChain','TrainerPneumaticNetworkCandidate')
$taskSources = $taskNames | ForEach-Object { Join-Path $PSScriptRoot ($_ + '.cs') }
$taskProbe = Join-Path $PSScriptRoot '..\work\pneumatic_live_fix\analysis\AuxiliaryAirNativeProbe.cs'
$taskExe = Join-Path $PSScriptRoot 'AuxiliaryAirNativeProbe.exe'
& 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe' /nologo /platform:x86 /main:AuxiliaryAirNativeProbe /r:System.Windows.Forms.dll /r:System.Drawing.dll /r:System.Web.Extensions.dll "/out:$taskExe" $taskSources $taskProbe
if ($LASTEXITCODE -ne 0) { throw 'Сборка проверки пуска компрессоров не удалась.' }



