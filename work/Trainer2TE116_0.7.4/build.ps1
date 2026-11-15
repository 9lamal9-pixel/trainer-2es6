<#
Сборка новой программы инструктора 2ТЭ116: out\Trainer2TE116.exe (.NET Framework 4, x64, WinForms).
  .\build.ps1                  # сборка
  .\build.ps1 -Release <папка> # папка work\Release тренажёра (SQLite, сценарии, картинки знаков)
  .\build.ps1 -Define NOTEST   # без проверочного кода: ключи стендов и прогон --run отключены
SQLite берётся из установки инструктора и копируется рядом с программой; сама установка не меняется.
#>
param([string]$Release = 'C:\Users\sibir\Desktop\Макрос\Тренажер\фантомас\work\Release', [string]$Out = (Join-Path $PSScriptRoot 'out'), [string]$Define = '')
$ErrorActionPreference = 'Stop'
$fw = 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319'
New-Item -ItemType Directory -Force $Out, (Join-Path $Out 'x64') | Out-Null
Copy-Item (Join-Path $Release 'System.Data.SQLite.dll') $Out -Force
Copy-Item (Join-Path $Release 'x64\SQLite.Interop.dll') (Join-Path $Out 'x64') -Force
$extra = @(); if ($Define) { $extra += "/define:$Define" }
$src = Get-ChildItem (Join-Path $PSScriptRoot 'src') -Filter *.cs | ForEach-Object { $_.FullName }
& "$fw\csc.exe" /nologo /preferreduilang:en-US /target:winexe /platform:x64 /optimize+ /codepage:65001 /out:"$Out\Trainer2TE116.exe" `
    "/r:$Out\System.Data.SQLite.dll" /r:System.Web.Extensions.dll /r:System.Windows.Forms.dll /r:System.Drawing.dll @extra $src
if ($LASTEXITCODE -ne 0) { throw 'csc failed' }
$schemeAssets = Join-Path $PSScriptRoot 'assets\scheme008'
$schemeOut = Join-Path $Out 'assets\scheme008'
New-Item -ItemType Directory -Force $schemeOut | Out-Null
Copy-Item -LiteralPath (Join-Path $schemeAssets 'atlas.json'), (Join-Path $schemeAssets 'wires.json'), (Join-Path $schemeAssets 'original.pdf') -Destination $schemeOut -Force
Get-ChildItem -LiteralPath $schemeAssets -Filter 'page-*.png' | Copy-Item -Destination $schemeOut -Force
'Trainer2TE116.exe  ' + (Get-Item "$Out\Trainer2TE116.exe").Length + '  ' + (Get-FileHash "$Out\Trainer2TE116.exe").Hash.Substring(0, 16)
