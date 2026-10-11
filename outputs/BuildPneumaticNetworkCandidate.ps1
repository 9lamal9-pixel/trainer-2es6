$ErrorActionPreference = 'Stop'
$taskNames = @('TrainerIntegrationHost','TrainerSessionLogging','TrainerNativeSystems','TrainerAuxSystems','TrainerRouteDOS','TrainerRouteView','TrainerNativeFixes','TrainerPneumaticTiming','TrainerAirNetwork','TrainerControlAir','TrainerBtoNetwork','TrainerAirSelector','TrainerKvtProfile','TrainerTwoSectionAirChain','TrainerPneumaticNetworkCandidate')
$taskSources = $taskNames | ForEach-Object { Join-Path $PSScriptRoot ($_ + '.cs') }
$taskExe = Join-Path $PSScriptRoot 'TrainerIntegrationHost.pneumatic_candidate.exe'
& 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe' /nologo /platform:x86 /target:winexe /r:System.Windows.Forms.dll /r:System.Drawing.dll /r:System.Web.Extensions.dll "/out:$taskExe" $taskSources
if ($LASTEXITCODE -ne 0) { throw 'Сборка кандидата пневмосети не удалась.' }

