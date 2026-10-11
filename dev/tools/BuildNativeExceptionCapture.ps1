$ErrorActionPreference = 'Stop'
$captureCompiler = Join-Path $env:WINDIR 'Microsoft.NET\Framework\v4.0.30319\csc.exe'
$captureSource = Join-Path $PSScriptRoot 'NativeExceptionCapture.cs'
$captureExe = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\outputs\NativeExceptionCapture.exe'))
& $captureCompiler /nologo /platform:x86 /target:exe /optimize+ /debug:pdbonly /r:System.Web.Extensions.dll (('/out:') + $captureExe) $captureSource
if ($LASTEXITCODE -ne 0) { throw "Native exception capture compiler exit code $LASTEXITCODE" }
Write-Output $captureExe
