param([string]$PatchDirectory = (Join-Path $PSScriptRoot '../out/dist/Trainer2TE116_Patch_0.7.4'))
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$patch = [IO.Path]::GetFullPath($PatchDirectory)
$fixture = Join-Path $root 'validation/update_fixture'
if (Test-Path -LiteralPath $fixture) { throw 'Update fixture already exists; keep its results and choose a fresh run.' }
New-Item -ItemType Directory -Path $fixture,(Join-Path $fixture 'x64'),(Join-Path $fixture 'assets/scheme008'),(Join-Path $fixture 'data/trips') | Out-Null
Copy-Item -LiteralPath (Join-Path $root 'validation/baseline/Trainer2TE116.exe') -Destination $fixture
Copy-Item -LiteralPath (Join-Path $root 'validation/baseline/System.Data.SQLite.dll') -Destination $fixture
Copy-Item -LiteralPath (Join-Path $root 'validation/baseline/x64/SQLite.Interop.dll') -Destination (Join-Path $fixture 'x64')
Copy-Item -LiteralPath (Join-Path $root 'assets/scheme008/atlas.json') -Destination (Join-Path $fixture 'assets/scheme008')
[IO.File]::WriteAllText((Join-Path $fixture 'settings.json'), '{"release_dir":"user-settings-sentinel"}')
[IO.File]::WriteAllText((Join-Path $fixture 'data/trips/keep.txt'), 'trip-data-sentinel')
$before = @{}
foreach ($name in @('Trainer2TE116.exe','settings.json','data/trips/keep.txt','System.Data.SQLite.dll','x64/SQLite.Interop.dll')) {
    $before[$name] = (Get-FileHash -LiteralPath (Join-Path $fixture $name) -Algorithm SHA256).Hash
}
& (Join-Path $patch 'update.ps1') -Target $fixture
$expected = (Get-FileHash -LiteralPath (Join-Path $patch 'program/Trainer2TE116.exe') -Algorithm SHA256).Hash
if ((Get-FileHash -LiteralPath (Join-Path $fixture 'Trainer2TE116.exe') -Algorithm SHA256).Hash -ne $expected) { throw 'Installed EXE differs from payload' }
foreach ($name in @('settings.json','data/trips/keep.txt','System.Data.SQLite.dll','x64/SQLite.Interop.dll')) {
    if ((Get-FileHash -LiteralPath (Join-Path $fixture $name) -Algorithm SHA256).Hash -ne $before[$name]) { throw ('User file changed: ' + $name) }
}
$backups = @(Get-ChildItem -LiteralPath (Join-Path $fixture 'backup') -Filter Trainer2TE116.exe -Recurse -File)
if ($backups.Count -ne 1 -or (Get-FileHash -LiteralPath $backups[0].FullName -Algorithm SHA256).Hash -ne $before['Trainer2TE116.exe']) { throw 'Backup does not match the old EXE' }

# Bad manifest must fail before changing the target or creating another backup.
$tampered = Join-Path $root 'validation/tampered_update_fixture'
Copy-Item -LiteralPath $patch -Destination $tampered -Recurse
$manifest = Get-Content -LiteralPath (Join-Path $tampered 'manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$manifest.exe_sha256 = '0000000000000000000000000000000000000000000000000000000000000000'
$manifest | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $tampered 'manifest.json') -Encoding UTF8
$rejected = $false
try { & (Join-Path $tampered 'update.ps1') -Target $fixture } catch { $rejected = $true }
if (-not $rejected) { throw 'Tampered update was accepted' }
if ((Get-FileHash -LiteralPath (Join-Path $fixture 'Trainer2TE116.exe') -Algorithm SHA256).Hash -ne $expected) { throw 'Failed update changed the EXE' }
if (@(Get-ChildItem -LiteralPath (Join-Path $fixture 'backup') -Filter Trainer2TE116.exe -Recurse -File).Count -ne 1) { throw 'Failed checksum created a spurious backup' }
$summary = 'PASS update payload, old EXE backup, settings/data/dependencies preserved, checksum rejection before writes'
$summary | Set-Content -LiteralPath (Join-Path $root 'validation/update_report.txt') -Encoding UTF8
Write-Output $summary
