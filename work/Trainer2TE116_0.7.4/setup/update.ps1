<# Update only Trainer2TE116.exe. User data and settings are not part of the payload. #>
param([string]$Target)
$ErrorActionPreference = 'Stop'
try { [Console]::OutputEncoding = [Text.Encoding]::UTF8 } catch { }

$payload = Join-Path $PSScriptRoot 'program/Trainer2TE116.exe'
$manifestPath = Join-Path $PSScriptRoot 'manifest.json'
if (-not (Test-Path -LiteralPath $payload -PathType Leaf)) { throw 'Не найден program/Trainer2TE116.exe рядом с патчем.' }
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$payloadHash = (Get-FileHash -LiteralPath $payload -Algorithm SHA256).Hash
if ($payloadHash -ne $manifest.exe_sha256) { throw 'Контрольная сумма патча не совпадает. Распакуйте архив заново.' }
if ((Get-Item -LiteralPath $payload).VersionInfo.FileVersion -ne '0.7.4.0') { throw 'В патче неожиданная версия EXE.' }

if ([string]::IsNullOrWhiteSpace($Target)) {
    $answer = Read-Host 'Папка установленного Trainer2TE116 [C:\Trainer2TE116]'
    $Target = if ([string]::IsNullOrWhiteSpace($answer)) { 'C:\Trainer2TE116' } else { $answer.Trim().Trim('"') }
}
$targetRoot = [IO.Path]::GetFullPath($Target)
$installedExe = Join-Path $targetRoot 'Trainer2TE116.exe'
if (-not (Test-Path -LiteralPath $installedExe -PathType Leaf)) { throw ('В этой папке нет Trainer2TE116.exe: ' + $targetRoot) }
if ([IO.Path]::GetFullPath($installedExe) -eq [IO.Path]::GetFullPath($payload)) { throw 'Выберите папку установленной программы, а не папку патча.' }
$installed = Get-Item -LiteralPath $installedExe
if ($installed.VersionInfo.ProductName -ne 'Trainer2TE116' -or $installed.VersionInfo.FileVersion -notlike '0.7.*') {
    throw 'Этот патч предназначен для Trainer2TE116 серии 0.7. Используйте полный установочный пакет.'
}
foreach ($dependency in @('System.Data.SQLite.dll', 'x64/SQLite.Interop.dll', 'assets/scheme008/atlas.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $targetRoot $dependency) -PathType Leaf)) { throw ('Не найден ' + $dependency + '. Используйте полный установочный пакет.') }
}
$installedAtlas = Get-Content -LiteralPath (Join-Path $targetRoot 'assets/scheme008/atlas.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not $installedAtlas.modelSignature -or $installedAtlas.modelSignature -ne $manifest.atlas_model_signature) {
    throw 'Схема установленной программы отличается от базы патча. Используйте полный установочный пакет 0.7.4.'
}
$running = @(Get-Process -Name Trainer2TE116 -ErrorAction SilentlyContinue | Where-Object {
    try { [IO.Path]::GetFullPath($_.Path) -eq [IO.Path]::GetFullPath($installedExe) } catch { $true }
})
if ($running.Count -gt 0) { throw 'Закройте обновляемую программу инструктора и запустите патч снова.' }

$backupRoot = Join-Path $targetRoot ('backup/0.7.4_' + (Get-Date -Format 'yyyyMMdd_HHmmss_fff'))
New-Item -ItemType Directory -Path $backupRoot | Out-Null
$backupExe = Join-Path $backupRoot 'Trainer2TE116.exe'
Copy-Item -LiteralPath $installedExe -Destination $backupExe
$oldHash = (Get-FileHash -LiteralPath $installedExe -Algorithm SHA256).Hash
if ((Get-FileHash -LiteralPath $backupExe -Algorithm SHA256).Hash -ne $oldHash) { throw 'Не удалось проверить резервную копию. Обновление остановлено.' }
try {
    Copy-Item -LiteralPath $payload -Destination $installedExe -Force
    if ((Get-FileHash -LiteralPath $installedExe -Algorithm SHA256).Hash -ne $payloadHash) { throw 'Контрольная сумма после копирования не совпадает.' }
} catch {
    Copy-Item -LiteralPath $backupExe -Destination $installedExe -Force
    throw
}
Write-Host 'Trainer2TE116 обновлён до 0.7.4.' -ForegroundColor Green
Write-Host ('Резервная копия: ' + $backupExe)
Write-Host 'Настройки, сценарии и поездки сохранены.'
