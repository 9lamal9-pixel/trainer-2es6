$ErrorActionPreference = 'Stop'
$taskCompiler = 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe'
$taskExe = Join-Path $PSScriptRoot 'TrainerIntegrationHost.exe'
$taskSources = @('TrainerIntegrationHost.cs', 'TrainerNativeSystems.cs', 'TrainerAuxSystems.cs', 'TrainerRouteDOS.cs', 'TrainerRouteView.cs', 'TrainerNativeFixes.cs') | ForEach-Object { Join-Path $PSScriptRoot $_ }
& $taskCompiler /nologo /platform:x86 /target:winexe /r:System.Windows.Forms.dll /r:System.Drawing.dll /r:System.Web.Extensions.dll "/out:$taskExe" $taskSources
if ($LASTEXITCODE -ne 0) { throw 'Не удалось собрать стенд. Закройте его перед сборкой.' }
