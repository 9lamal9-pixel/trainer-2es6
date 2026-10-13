[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][ValidateSet('Audit','Organize','Verify')][string]$Action,
    [Parameter(Mandatory=$true)][string]$ReportDirectory
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..')).TrimEnd('\')
$taskNames = @('LAUNCH_CAM','LAUNCH_DSP','LAUNCH_INSTR','LAUNCH_TREN','WORK_TRENAGER_3D','WORLD3DSERVER')

function WorkspacePath([string]$Relative) {
    if ([IO.Path]::IsPathRooted($Relative)) { throw 'Use a workspace-relative path.' }
    $path = [IO.Path]::GetFullPath((Join-Path $taskRoot $Relative))
    if (-not $path.StartsWith($taskRoot+'\',[StringComparison]::OrdinalIgnoreCase)) { throw "Path outside workspace: $Relative" }
    $ancestor = $path
    while ($ancestor.Length -ge $taskRoot.Length) {
        if (Test-Path -LiteralPath $ancestor) {
            if ((Get-Item -LiteralPath $ancestor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Reparse path: $ancestor" }
        }
        if ($ancestor -eq $taskRoot) { break }
        $ancestor = [IO.Path]::GetDirectoryName($ancestor)
    }
    return $path
}
function SaveJson($Value,[string]$Path) {
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($Path)) | Out-Null
    [IO.File]::WriteAllText($Path,($Value | ConvertTo-Json -Depth 15),[Text.UTF8Encoding]::new($false))
}
function FileHash([string]$Path) {
    $stream = [IO.FileStream]::new($Path,[IO.FileMode]::Open,[IO.FileAccess]::Read,[IO.FileShare]::Read,1048576,[IO.FileOptions]::SequentialScan)
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-','') }
    finally { $sha.Dispose(); $stream.Dispose() }
}
function Inventory([string]$Relative) {
    $path = WorkspacePath $Relative
    if (-not (Test-Path -LiteralPath $path -PathType Container)) { throw "Missing folder: $Relative" }
    $rootItem = Get-Item -LiteralPath $path -Force
    if ($rootItem.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Reparse root: $Relative" }
    $items = @(Get-ChildItem -LiteralPath $path -Recurse -Force | Sort-Object FullName)
    if (@($items | Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint }).Count) { throw "Reparse entry: $Relative" }
    $files = @(foreach ($item in $items) {
        if ($item.PSIsContainer) { continue }
        $length = $item.Length
        $ticks = $item.LastWriteTimeUtc.Ticks
        $hash = FileHash $item.FullName
        $item.Refresh()
        if ($item.Length -ne $length -or $item.LastWriteTimeUtc.Ticks -ne $ticks) { throw "File changed during audit: $($item.FullName)" }
        [pscustomobject]@{path=$item.FullName.Substring($path.Length+1).Replace('\','/');bytes=$length;sha256=$hash;last_write_utc_ticks=$ticks}
    })
    return [pscustomobject]@{
        schema_version=1;root=$Relative;utc=[DateTime]::UtcNow.ToString('O')
        file_count=$files.Count;bytes=[long](($files | Measure-Object bytes -Sum).Sum)
        directories=@($items | Where-Object PSIsContainer | ForEach-Object { $_.FullName.Substring($path.Length+1).Replace('\','/') })
        files=$files
    }
}
function CompareInventory($Source,$Baseline) {
    $sourceMap = @{}; foreach ($file in $Source.files) { $sourceMap[$file.path] = $file }
    $baselineMap = @{}; foreach ($file in $Baseline.files) { $baselineMap[$file.path] = $file }
    $same = @(); $different = @(); $added = @(); $missing = @()
    foreach ($file in $Source.files) {
        if (-not $baselineMap.ContainsKey($file.path)) { $added += $file.path; continue }
        $old = $baselineMap[$file.path]
        if ($old.bytes -eq $file.bytes -and $old.sha256 -eq $file.sha256) { $same += $file.path }
        else { $different += [pscustomobject]@{path=$file.path;source_bytes=$file.bytes;baseline_bytes=$old.bytes;source_sha256=$file.sha256;baseline_sha256=$old.sha256} }
    }
    foreach ($file in $Baseline.files) { if (-not $sourceMap.ContainsKey($file.path)) { $missing += $file.path } }
    return [pscustomobject]@{source=$Source.root;baseline=$Baseline.root;same_count=$same.Count;different_count=$different.Count;added_count=$added.Count;missing_count=$missing.Count;different=$different;added=$added;missing=$missing}
}
function AssertLayout($Manifest,[string]$Relative,[switch]$Hash) {
    $path = WorkspacePath $Relative
    $items = @(Get-ChildItem -LiteralPath $path -Recurse -Force)
    if ((Get-Item -LiteralPath $path -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw "Reparse root: $Relative" }
    if (@($items | Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint }).Count) { throw "Reparse entry: $Relative" }
    $files = @($items | Where-Object { -not $_.PSIsContainer })
    $directories = @($items | Where-Object PSIsContainer | ForEach-Object { $_.FullName.Substring($path.Length+1).Replace('\','/') })
    if ($files.Count -ne $Manifest.file_count -or $directories.Count -ne @($Manifest.directories).Count) { throw "Entry count mismatch: $Relative" }
    $map = @{}; foreach ($file in $files) { $map[$file.FullName.Substring($path.Length+1).Replace('\','/')] = $file }
    $dirMap = @{}; foreach ($directory in $directories) { $dirMap[$directory] = $true }
    foreach ($directory in $Manifest.directories) { if (-not $dirMap.ContainsKey($directory)) { throw "Missing directory: $Relative/$directory" } }
    foreach ($entry in $Manifest.files) {
        if (-not $map.ContainsKey($entry.path)) { throw "Missing file: $Relative/$($entry.path)" }
        $file = $map[$entry.path]
        if ($file.Length -ne $entry.bytes -or $file.LastWriteTimeUtc.Ticks -ne $entry.last_write_utc_ticks) { throw "Metadata changed: $Relative/$($entry.path)" }
        if ($Hash -and (FileHash $file.FullName) -ne $entry.sha256) { throw "SHA mismatch: $Relative/$($entry.path)" }
    }
    return [pscustomobject]@{folder=$Relative;files=$files.Count;directories=$directories.Count;bytes=$Manifest.bytes;sha256_verified=[bool]$Hash;passed=$true}
}
$reportPath = WorkspacePath $ReportDirectory
if (-not $reportPath.StartsWith((WorkspacePath 'dev/runs')+'\',[StringComparison]::OrdinalIgnoreCase)) { throw 'Report must be inside dev/runs.' }
$manifestDirectory = Join-Path $reportPath 'manifests'
if ($Action -eq 'Audit' -and (Test-Path -LiteralPath $reportPath)) { throw 'Audit report folder already exists; choose a fresh run.' }
if ($Action -eq 'Organize' -and (Test-Path -LiteralPath (Join-Path $reportPath 'organize.json'))) { throw 'Organization report already exists; use Verify to check the saved layout.' }
$result = [ordered]@{schema_version=1;action=$Action;started_utc=[DateTime]::UtcNow.ToString('O');status='running';destination='work/native_distribution';assertions=@();limitations='Byte integrity and layout only; launch, network, 3D rendering and functional compatibility are not validated.'}
$moved = New-Object 'Collections.Generic.List[string]'
try {
    if ($Action -eq 'Audit') {
        [IO.Directory]::CreateDirectory($manifestDirectory) | Out-Null
        $baselineRuntime = Inventory 'work/trenager_static/WORK_TRENAGER_3D'
        SaveJson $baselineRuntime (Join-Path $manifestDirectory 'baseline-runtime.json')
        $baselineCam = Inventory 'work/launch_cam_static/LAUNCH_CAM'
        SaveJson $baselineCam (Join-Path $manifestDirectory 'baseline-cam.json')
        $summary = @(); $comparisons = @(); $sourceRuntime = $null
        foreach ($name in $taskNames) {
            $inventory = Inventory $name
            $manifestPath = Join-Path $manifestDirectory ($name+'.json')
            SaveJson $inventory $manifestPath
            $summary += [pscustomobject]@{name=$name;files=$inventory.file_count;directories=@($inventory.directories).Count;bytes=$inventory.bytes;manifest=$ReportDirectory+'/manifests/'+$name+'.json';manifest_sha256=(FileHash $manifestPath)}
            Write-Host ("Audited {0}: {1} files, {2} bytes" -f $name,$inventory.file_count,$inventory.bytes)
            if ($name -eq 'LAUNCH_CAM') { $comparisons += CompareInventory $inventory $baselineCam }
            if ($name -eq 'WORK_TRENAGER_3D') { $sourceRuntime=$inventory; $comparisons += CompareInventory $inventory $baselineRuntime }
        }
        $lock = Get-Content -LiteralPath (WorkspacePath 'dev/config/native-lock.json') -Encoding UTF8 -Raw | ConvertFrom-Json
        $runtimeMap = @{}; foreach ($file in $sourceRuntime.files) { $runtimeMap[$file.path] = $file }
        $lockChecks = @(foreach ($file in $lock.files) {
            $relative = $file.path.Substring('work/trenager_static/WORK_TRENAGER_3D/'.Length)
            $actual = $runtimeMap[$relative]
            [pscustomobject]@{path=$relative;exists=($null -ne $actual);matches=($null -ne $actual -and $actual.bytes -eq $file.bytes -and $actual.sha256 -eq $file.sha256);expected_sha256=$file.sha256;actual_sha256=$(if($actual){$actual.sha256}else{$null})}
        })
        $result.folders=$summary; $result.comparisons=$comparisons; $result.native_lock=$lockChecks
        $result.assertions=@('All six folders inventoried with SHA256','No reparse points','Files stable while hashed','Baseline inventories saved')
    } else {
        $audit = Get-Content -LiteralPath (Join-Path $reportPath 'audit.json') -Encoding UTF8 -Raw | ConvertFrom-Json
        if ($audit.status -ne 'passed') { throw 'Successful audit required.' }
        $manifests = @{}
        foreach ($entry in $audit.folders) {
            $manifestPath = WorkspacePath $entry.manifest
            if ((FileHash $manifestPath) -ne $entry.manifest_sha256) { throw "Manifest changed: $($entry.name)" }
            $manifests[$entry.name] = Get-Content -LiteralPath $manifestPath -Encoding UTF8 -Raw | ConvertFrom-Json
        }
        if ($Action -eq 'Organize') {
            foreach ($name in $taskNames) {
                $target = WorkspacePath ('work/native_distribution/'+$name)
                if (Test-Path -LiteralPath $target) { throw "Destination exists: $target" }
                AssertLayout $manifests[$name] $name | Out-Null
            }
            [IO.Directory]::CreateDirectory((WorkspacePath 'work/native_distribution')) | Out-Null
            foreach ($name in $taskNames) {
                # Both exact absolute paths are confined to the workspace; no merge or overwrite.
                $source = WorkspacePath $name
                $target = WorkspacePath ('work/native_distribution/'+$name)
                [IO.Directory]::Move($source,$target)
                $moved.Add($name)
                Write-Host ('Moved '+$name)
            }
        }
        $checks = @(foreach ($name in $taskNames) { AssertLayout $manifests[$name] ('work/native_distribution/'+$name) -Hash })
        foreach ($name in $taskNames) { if (Test-Path -LiteralPath (WorkspacePath $name)) { throw "Unexpected old root folder: $name" } }
        $baselineRuntime = Get-Content -LiteralPath (Join-Path $manifestDirectory 'baseline-runtime.json') -Encoding UTF8 -Raw | ConvertFrom-Json
        $baselineCam = Get-Content -LiteralPath (Join-Path $manifestDirectory 'baseline-cam.json') -Encoding UTF8 -Raw | ConvertFrom-Json
        $result.folders=$checks
        $result.baseline_checks=@((AssertLayout $baselineRuntime $baselineRuntime.root -Hash),(AssertLayout $baselineCam $baselineCam.root -Hash))
        $result.assertions=@('All six destinations exist','All relative paths, file sizes, timestamps and SHA256 match audit','All directories including empty directories preserved','Old root folders absent','Canonical runtime and prior CAM unchanged')
    }
    $result.status='passed'
} catch {
    $result.status='failed'; $result.error=$_.ToString()
    if ($moved.Count) {
        $rollback = @()
        for ($i=$moved.Count-1;$i -ge 0;$i--) {
            $name=$moved[$i]
            try {
                $source=WorkspacePath ('work/native_distribution/'+$name); $target=WorkspacePath $name
                if (Test-Path -LiteralPath $target) { throw 'Rollback destination exists.' }
                [IO.Directory]::Move($source,$target)
                $rollback += [pscustomobject]@{folder=$name;restored=$true}
            } catch { $rollback += [pscustomobject]@{folder=$name;restored=$false;error=$_.ToString()} }
        }
        $result.rollback=$rollback
    }
} finally {
    $result.finished_utc=[DateTime]::UtcNow.ToString('O')
    SaveJson $result (Join-Path $reportPath ($Action.ToLowerInvariant()+'.json'))
}
Write-Host ($Action+': '+$result.status+'; '+$ReportDirectory)
if ($result.status -ne 'passed') { Write-Error $result.error -ErrorAction Continue; exit 1 }
