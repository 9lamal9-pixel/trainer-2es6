#requires -Version 5.1
[CmdletBinding()]
param(
    [string]$AssetDirectory,
    [string]$GitExecutable,
    [string]$PythonExecutable,
    [switch]$SkipValidation
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..')).TrimEnd('\')
$repository = '9lamal9-pixel/trainer-2es6'
$releaseTag = 'trainer-environment-2026-10-16'
$utf8 = New-Object Text.UTF8Encoding($false)
$stage = $null
$token = $null
$headers = $null
function Read-Json([string]$Path) { Get-Content -LiteralPath $Path -Encoding UTF8 -Raw | ConvertFrom-Json }
function Write-Json($Object, [string]$Path) { [IO.File]::WriteAllText($Path, ($Object | ConvertTo-Json -Depth 50), $utf8) }
function File-Hash([string]$Path) {
    $stream = [IO.File]::OpenRead($Path)
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-','') }
    finally { $sha.Dispose(); $stream.Dispose() }
}
function Download-PrivateAsset([string]$ApiUri, [string]$Destination, [string]$AccessToken, [long]$ExpectedSize, [string]$ExpectedHash) {
    for ($attempt = 1; $attempt -le 3; $attempt++) {
        try {
            $uri = [Uri]$ApiUri
            if ($uri.Scheme -cne 'https' -or $uri.Host -ine 'api.github.com') { throw 'Unexpected GitHub API download URL.' }
            $authenticated = $true
            for ($redirect = 0; $redirect -le 5; $redirect++) {
                $request = [Net.HttpWebRequest]::Create($uri)
                $request.AllowAutoRedirect = $false
                $request.Method = 'GET'; $request.Accept = 'application/octet-stream'
                $request.UserAgent = 'trainer-portable-setup'
                $request.Timeout = 120000; $request.ReadWriteTimeout = 120000
                if ($authenticated) {
                    $request.Headers['Authorization'] = 'Bearer ' + $AccessToken
                    $request.Headers['X-GitHub-Api-Version'] = '2022-11-28'
                }
                $response = $null
                try {
                    $response = [Net.HttpWebResponse]$request.GetResponse()
                    $status = [int]$response.StatusCode
                    if ($status -in @(301,302,303,307,308)) {
                        if ($redirect -eq 5) { throw 'Too many release download redirects.' }
                        $next = New-Object Uri($uri, $response.Headers['Location'])
                        $safeHost = $next.Host -ieq 'githubusercontent.com' -or $next.Host.EndsWith('.githubusercontent.com',[StringComparison]::OrdinalIgnoreCase)
                        if ($next.Scheme -cne 'https' -or -not $safeHost -or $next.UserInfo -or -not $next.IsDefaultPort) { throw 'Unexpected release download redirect host.' }
                        # Every signed-storage request is new and carries no GitHub authorization header.
                        $uri = $next; $authenticated = $false
                        continue
                    }
                    if ($status -ne 200) { throw 'Unexpected release download status.' }
                    Assert-NoReparse $Destination
                    $inputStream = $response.GetResponseStream()
                    $outputStream = [IO.File]::Open($Destination,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write,[IO.FileShare]::None)
                    try { $inputStream.CopyTo($outputStream) } finally { $outputStream.Dispose(); $inputStream.Dispose() }
                    if (([IO.FileInfo]::new($Destination)).Length -ne $ExpectedSize -or (File-Hash $Destination) -ine $ExpectedHash) { throw 'Downloaded SHA256/size mismatch.' }
                    return
                } finally { if ($null -ne $response) { $response.Dispose() } }
            }
            throw 'Release asset was not downloaded.'
        } catch {
            # Destination belongs to this invocation's unique staging directory only.
            Assert-NoReparse $Destination
            if ([IO.File]::Exists($Destination)) { [IO.File]::Delete($Destination) }
            if ($attempt -eq 3) { throw 'Download failed after three attempts. Check GitHub access and network; credentials were not saved.' }
            Start-Sleep -Seconds ($attempt * 2)
        }
    }
}
# Checked ancestors may be reused within one phase. Clear between extraction, preflight and copy.
$script:pathChecks = New-Object 'Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
function Reset-PathChecks { $script:pathChecks.Clear() }
function Assert-NoReparse([string]$Path) {
    $current = [IO.Path]::GetFullPath($Path)
    while ($current) {
        if ($script:pathChecks.Contains($current)) { break }
        try {
            $attributes = [IO.File]::GetAttributes($current)
            if (($attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) { throw ('Link/reparse point is not allowed: ' + $current) }
        } catch [IO.FileNotFoundException] {
            # A missing leaf is expected during a fresh restore; still inspect its ancestors.
        } catch [IO.DirectoryNotFoundException] {
        }
        [void]$script:pathChecks.Add($current)
        $parent = [IO.Path]::GetDirectoryName($current)
        if ($parent -eq $current) { break }
        $current = $parent
    }
}
function Safe-Relative([string]$Name) {
    $normalized = $Name.Replace('\','/')
    if ([string]::IsNullOrWhiteSpace($normalized) -or $normalized.StartsWith('/') -or $normalized.Contains(':') -or $normalized.IndexOf([char]0) -ge 0) { throw ('Unsafe archive path: ' + $Name) }
    $parts = $normalized.TrimEnd('/').Split('/')
    foreach ($part in $parts) {
        if ($part -eq '' -or $part -eq '.' -or $part -eq '..' -or $part -match '[<>"|?*\x00-\x1f]' -or $part.EndsWith('.') -or $part.EndsWith(' ') -or $part -match '^(?i:CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\.|$)') { throw ('Unsafe archive component: ' + $Name) }
    }
    if ($parts[0] -in @('.git','.codex','.agents','.aws') -or $normalized -match '^dev/state/portable-') { throw ('Reserved archive path: ' + $Name) }
    return ($parts -join '/')
}
function Under-Root([string]$Root, [string]$Relative) {
    $result = [IO.Path]::GetFullPath((Join-Path $Root $Relative.Replace('/','\')))
    if (-not $result.StartsWith($Root.TrimEnd('\') + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Path escapes destination.' }
    return $result
}
function Invoke-Captured([string]$Exe, [string[]]$Arguments, [string]$InputText = '') {
    $psi = New-Object Diagnostics.ProcessStartInfo
    $psi.FileName = $Exe
    $quoted = foreach ($argument in $Arguments) {
        $escaped = [Regex]::Replace($argument, '(\\*)"', '$1$1\"')
        $escaped = [Regex]::Replace($escaped, '(\\+)$', '$1$1')
        '"' + $escaped + '"'
    }
    $psi.Arguments = $quoted -join ' '
    $psi.WorkingDirectory = $projectRoot
    $psi.UseShellExecute = $false; $psi.CreateNoWindow = $true
    $psi.RedirectStandardOutput = $true; $psi.RedirectStandardError = $true; $psi.RedirectStandardInput = $true
    $psi.StandardOutputEncoding = $utf8; $psi.StandardErrorEncoding = $utf8
    $psi.EnvironmentVariables['PYTHONUTF8'] = '1'
    $psi.EnvironmentVariables['PYTHONIOENCODING'] = 'utf-8'
    $psi.EnvironmentVariables['GIT_TERMINAL_PROMPT'] = '0'
    $process = New-Object Diagnostics.Process
    $process.StartInfo = $psi
    try {
        if (-not $process.Start()) { throw 'Cannot start dependency.' }
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.StandardInput.Write($InputText); $process.StandardInput.Close()
        $process.WaitForExit()
        return [pscustomobject]@{ ExitCode = $process.ExitCode; Output = $stdout.Result; ErrorText = $stderr.Result }
    } finally { $process.Dispose() }
}
function Find-Tool([string]$Explicit, [string]$Name) {
    if ($Explicit) {
        if (-not (Test-Path -LiteralPath $Explicit -PathType Leaf)) { throw ('Tool does not exist: ' + $Explicit) }
        return [IO.Path]::GetFullPath($Explicit)
    }
    $command = Get-Command $Name -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($command) {
        if ($Name -eq 'python' -and $command.Source -match '(?i)[\\/]Microsoft[\\/]WindowsApps[\\/]python(?:3(?:\.\d+)?)?\.exe$') { return $null }
        return $command.Source
    }
    return $null
}
function Get-GitHubToken {
    if ($env:GH_TOKEN) { return $env:GH_TOKEN }
    if ($env:GITHUB_TOKEN) { return $env:GITHUB_TOKEN }
    $gh = Find-Tool '' 'gh'
    if ($gh) {
        $result = Invoke-Captured $gh @('auth','token','--hostname','github.com')
        if ($result.ExitCode -eq 0 -and $result.Output.Trim()) { return $result.Output.Trim() }
    }
    if ($script:gitPath) {
        $result = Invoke-Captured $script:gitPath @('-c','credential.interactive=never','credential','fill') "protocol=https`nhost=github.com`n`n"
        if ($result.ExitCode -eq 0) {
            foreach ($line in ($result.Output -split '\r?\n')) { if ($line.StartsWith('password=')) { return $line.Substring(9) } }
        }
    }
    throw 'Private release authentication missing. Run gh auth login or configure Git Credential Manager for github.com; alternatively set GH_TOKEN for this process. Offline use: setup.cmd -AssetDirectory <folder>.'
}
function Configure-Machine {
    $frameworkDir = Join-Path $env:WINDIR 'Microsoft.NET\Framework\v4.0.30319'
    $compiler = Join-Path $frameworkDir 'csc.exe'
    $winforms = Join-Path $frameworkDir 'System.Windows.Forms.dll'
    if (-not (Test-Path -LiteralPath $compiler -PathType Leaf) -or -not (Test-Path -LiteralPath $winforms -PathType Leaf)) { throw 'Install Windows .NET Framework 4.x (including its 32-bit Framework compiler), then run setup.cmd again.' }
    if (-not $script:gitPath) { throw 'Install Git for Windows and restart the terminal, or pass -GitExecutable <full path to git.exe>.' }
    $env:PATH = (Split-Path -Parent $script:gitPath) + ';' + $env:PATH
    $usingBundledPython = $false
    $bundlePath = Join-Path $projectRoot 'work/portable_python/python.exe'
    if ($PythonExecutable) {
        $python = Find-Tool $PythonExecutable 'python'
    } elseif ([IO.File]::Exists($bundlePath)) {
        Assert-NoReparse $bundlePath
        $osArchitecture = $env:PROCESSOR_ARCHITEW6432
        if (-not $osArchitecture) { $osArchitecture = $env:PROCESSOR_ARCHITECTURE }
        if (-not [Environment]::Is64BitOperatingSystem -or $osArchitecture -ine 'AMD64') { throw 'The bundled analysis Python requires Windows x64 (AMD64). Use Windows x64, or provide a compatible -PythonExecutable explicitly.' }
        $python = $bundlePath
        $usingBundledPython = $true
    } else {
        $python = Find-Tool '' 'python'
    }
    if ($python) {
        $versionCheck = 'import sys; assert sys.version_info >= (3,9); print(sys.executable)'
        if ($usingBundledPython) { $versionCheck = 'import sys, struct, platform; assert sys.version_info >= (3,9); assert sys.platform == "win32" and struct.calcsize("P") == 8 and platform.machine().lower() in ("amd64", "x86_64"); print(sys.executable)' }
        $result = Invoke-Captured $python @('-X','utf8','-c',$versionCheck)
        if ($result.ExitCode -eq 0) { $python = $result.Output.Trim() } else {
            if ($usingBundledPython) { throw 'Bundled Python cannot run as Windows x64 Python 3.9+. Verify the release archives and Windows x64 compatibility.' }
            if ($PythonExecutable) { throw 'The explicit -PythonExecutable cannot run Python 3.9 or newer.' }
            $python = $null
        }
    }
    if (-not $python) {
        $py = Find-Tool '' 'py'
        if ($py) {
            $result = Invoke-Captured $py @('-3','-X','utf8','-c','import sys; assert sys.version_info >= (3,9); print(sys.executable)')
            if ($result.ExitCode -eq 0) { $python = $result.Output.Trim() }
        }
    }
    if (-not $python) { throw 'Bundled work/portable_python/python.exe is missing and no Python 3.9+ fallback was found. Restore all release archives, or supply -PythonExecutable.' }
    $configPath = Join-Path $projectRoot 'dev/config/environment.json'
    $toolsPath = Join-Path $projectRoot 'dev/config/python-tools.json'
    $indexPath = Join-Path $projectRoot 'dev/environment-context.json'
    $relocationPath = Join-Path $projectRoot 'dev/config/relocation.json'
    $config = Read-Json $configPath; $tools = Read-Json $toolsPath; $index = Read-Json $indexPath
    $relocation = Read-Json $relocationPath
    # The portable runtime uses its own Lib/site-packages, never the historical vendor copies.
    if ($usingBundledPython) { $tools.vendor_paths = @() }
    $vendorPaths = @($tools.vendor_paths | ForEach-Object { Under-Root $projectRoot (Safe-Relative $_) })
    $check = 'import sys; sys.path[:0] = ' + (ConvertTo-Json -Compress -InputObject $vendorPaths) + '; import pefile, capstone; assert list(capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_32).disasm(b"\x90",0))[0].mnemonic == "nop"'
    $result = Invoke-Captured $python @('-X','utf8','-c',$check)
    if ($result.ExitCode -ne 0) {
        if ($usingBundledPython) { throw 'Bundled Python pefile/capstone import or x86 decoder check failed. Restore the complete verified Python supplement; no network installation was attempted.' }
        $venv = Join-Path $projectRoot 'dev/state/python-venv'
        Assert-NoReparse $venv
        $result = Invoke-Captured $python @('-X','utf8','-m','venv',$venv)
        if ($result.ExitCode -ne 0) { throw 'Cannot create analysis Python venv; install Python with venv/ensurepip support.' }
        $python = Join-Path $venv 'Scripts/python.exe'
        # Incompatible bundled binary extensions must not shadow the repaired venv.
        $tools.vendor_paths = @()
        $check = 'import pefile, capstone; assert list(capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_32).disasm(b"\x90",0))[0].mnemonic == "nop"'
        $result = Invoke-Captured $python @('-X','utf8','-m','pip','--isolated','install','--index-url','https://pypi.org/simple','--disable-pip-version-check','pefile==2024.8.26','capstone==5.0.6')
        if ($result.ExitCode -ne 0) { throw 'Cannot install pinned pefile/capstone from PyPI. Check network or provide the bundled compatible vendor packages and rerun setup.' }
        $result = Invoke-Captured $python @('-X','utf8','-c',$check)
        if ($result.ExitCode -ne 0) { throw 'Python native-analysis import/decoder check failed.' }
    }
    $backup = Join-Path $projectRoot ('dev/state/portable-config-backup-' + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ'))
    Assert-NoReparse $backup
    [IO.Directory]::CreateDirectory($backup) | Out-Null
    foreach ($path in @($configPath,$toolsPath,$indexPath,$relocationPath)) { Assert-NoReparse $path; [IO.File]::Copy($path,(Join-Path $backup ([IO.Path]::GetFileName($path))),$false) }
    $config.compiler = $compiler; $config.framework = $winforms; $config.python = $python
    $tools.interpreter = $python
    $index.workspace.root = $projectRoot.Replace('\','/')
    $index.toolchain.compiler = $compiler.Replace('\','/')
    $index.toolchain.git = $script:gitPath.Replace('\','/')
    $index.toolchain.python = $python.Replace('\','/')
    $relocation.workspace_root = $projectRoot.Replace('\','/')
    Write-Json $config $configPath; Write-Json $tools $toolsPath; Write-Json $index $indexPath
    Write-Json $relocation $relocationPath
    Write-Host ('Machine paths configured. Previous configs: ' + $backup)
}
try {
    Assert-NoReparse $projectRoot
    $script:gitPath = Find-Tool $GitExecutable 'git'
    if (-not $script:gitPath) {
        $index = Read-Json (Join-Path $projectRoot 'dev/environment-context.json')
        if ($index.toolchain.git -and (Test-Path -LiteralPath $index.toolchain.git -PathType Leaf)) { $script:gitPath = [IO.Path]::GetFullPath($index.toolchain.git) }
    }
    $manifestPath = Join-Path $projectRoot 'dev/config/portable-release.json'
    $manifest = Read-Json $manifestPath
    if ($manifest.schema_version -ne 1 -or $manifest.repository -cne $repository -or $manifest.tag -cne $releaseTag -or @($manifest.assets).Count -eq 0) { throw 'Unsupported portable manifest or repository/tag mismatch.' }
    $assetNames = New-Object 'Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
    foreach ($asset in $manifest.assets) {
        if ($asset.name -notmatch '^[A-Za-z0-9][A-Za-z0-9._-]*\.zip$' -or -not $assetNames.Add($asset.name) -or $asset.sha256 -notmatch '^[A-Fa-f0-9]{64}$' -or [long]$asset.size_bytes -le 0 -or [long]$asset.files -lt 0 -or [long]$asset.unpacked_bytes -lt 0) { throw 'Invalid archive metadata.' }
    }
    $stage = Join-Path $projectRoot ('dev/state/portable-stage-' + [Guid]::NewGuid().ToString('N'))
    Assert-NoReparse $stage
    [IO.Directory]::CreateDirectory($stage) | Out-Null
    if ($AssetDirectory) {
        $cache = [IO.Path]::GetFullPath($AssetDirectory)
        if (-not (Test-Path -LiteralPath $cache -PathType Container)) { throw 'Offline asset directory does not exist.' }
        Assert-NoReparse $cache
    } else {
        $cache = Join-Path $projectRoot 'dev/state/portable-downloads'
        Assert-NoReparse $cache
        [IO.Directory]::CreateDirectory($cache) | Out-Null
        $token = Get-GitHubToken
        $headers = @{ Authorization = 'Bearer ' + $token; Accept = 'application/vnd.github+json'; 'X-GitHub-Api-Version' = '2022-11-28'; 'User-Agent' = 'trainer-portable-setup' }
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        try { $release = Invoke-RestMethod -Uri ('https://api.github.com/repos/' + $repository + '/releases/tags/' + $releaseTag) -Headers $headers } catch { throw 'Cannot read the private release. Verify GitHub authentication, repository access and network connection.' }
    }
    foreach ($asset in $manifest.assets) {
        $archivePath = Join-Path $cache $asset.name
        Assert-NoReparse $archivePath
        if (-not (Test-Path -LiteralPath $archivePath -PathType Leaf)) {
            if ($AssetDirectory) { throw ('Missing offline archive: ' + $asset.name) }
            $remote = @($release.assets | Where-Object { $_.name -ceq $asset.name })
            if ($remote.Count -ne 1 -or [long]$remote[0].size -ne [long]$asset.size_bytes) { throw ('Release asset missing or wrong size: ' + $asset.name) }
            $partial = Join-Path $stage ($asset.name + '.download')
            Write-Host ('Downloading ' + $asset.name)
            Download-PrivateAsset ('https://api.github.com/repos/' + $repository + '/releases/assets/' + [long]$remote[0].id) $partial $token ([long]$asset.size_bytes) $asset.sha256
            if (([IO.FileInfo]::new($partial)).Length -ne [long]$asset.size_bytes -or (File-Hash $partial) -ine $asset.sha256) { throw ('Downloaded archive failed SHA256/size: ' + $asset.name) }
            [IO.File]::Move($partial,$archivePath)
        }
        if (([IO.FileInfo]::new($archivePath)).Length -ne [long]$asset.size_bytes -or (File-Hash $archivePath) -ine $asset.sha256) { throw ('Archive SHA256/size mismatch: ' + $asset.name + '. Remove/replace this cache file and retry.') }
    }
    # All archive hashes pass before any archive is opened or any runtime file is copied.
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $files = New-Object 'Collections.Generic.List[object]'
    $directories = New-Object 'Collections.Generic.List[object]'
    $directoryPaths = New-Object 'Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
    $allPaths = New-Object 'Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
    $extractRoot = Join-Path $stage 'files'
    [IO.Directory]::CreateDirectory($extractRoot) | Out-Null
    Reset-PathChecks
    foreach ($asset in $manifest.assets) {
        $archive = [IO.Compression.ZipFile]::OpenRead((Join-Path $cache $asset.name))
        try {
            $count = 0L; $bytes = 0L
            foreach ($entry in $archive.Entries) {
                $relative = Safe-Relative $entry.FullName
                $unixType = (($entry.ExternalAttributes -shr 16) -band 61440)
                if ($unixType -eq 40960 -or ($entry.ExternalAttributes -band 1024) -ne 0) { throw ('Archive link/reparse entry refused: ' + $relative) }
                if ($entry.FullName.EndsWith('/')) {
                    if ($entry.Length -ne 0 -or $allPaths.Contains($relative)) { throw ('Archive file/directory conflict: ' + $relative) }
                    if ($directoryPaths.Add($relative)) {
                        $destination = Under-Root $projectRoot $relative
                        Assert-NoReparse $destination
                        if (([IO.File]::Exists($destination) -or [IO.Directory]::Exists($destination)) -and -not [IO.Directory]::Exists($destination)) { throw ('Existing file occupies archive directory: ' + $relative) }
                        $directories.Add([pscustomobject]@{ Destination = $destination; Relative = $relative })
                    }
                    continue
                }
                if ($directoryPaths.Contains($relative)) { throw ('Archive file/directory conflict: ' + $relative) }
                if (-not $allPaths.Add($relative)) { throw ('Duplicate archive destination: ' + $relative) }
                $count++; $bytes += $entry.Length
                if ($count -gt [long]$asset.files -or $bytes -gt [long]$asset.unpacked_bytes) { throw ('Archive exceeds manifest totals: ' + $asset.name) }
                $stagedPath = Join-Path $extractRoot ($files.Count.ToString('D8'))
                Assert-NoReparse $stagedPath
                [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($stagedPath)) | Out-Null
                $sourceStream = $entry.Open(); $targetStream = [IO.File]::Open($stagedPath,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write,[IO.FileShare]::None)
                try { $sourceStream.CopyTo($targetStream) } finally { $targetStream.Dispose(); $sourceStream.Dispose() }
                if (([IO.FileInfo]::new($stagedPath)).Length -ne $entry.Length) { throw 'Extracted length mismatch.' }
                $destination = Under-Root $projectRoot $relative
                Assert-NoReparse $destination
                if ([IO.File]::Exists($destination) -or [IO.Directory]::Exists($destination)) {
                    if (-not [IO.File]::Exists($destination) -or (File-Hash $destination) -ine (File-Hash $stagedPath)) { throw ('Existing file differs; nothing will be overwritten. Move your version aside or use a fresh clone: ' + $relative) }
                }
                $files.Add([pscustomobject]@{ Source = $stagedPath; Destination = $destination; Relative = $relative })
                if (($files.Count % 1000) -eq 0) { Write-Host ('Staged and checked ' + $files.Count + ' files...') }
            }
            if ($count -ne [long]$asset.files -or $bytes -ne [long]$asset.unpacked_bytes) { throw ('Archive totals do not match manifest: ' + $asset.name) }
        } finally { $archive.Dispose() }
    }
    Reset-PathChecks
    # Preflight explicit directories and all planned ancestors, including directory-only archives.
    $preflightAncestors = New-Object 'Collections.Generic.HashSet[string]' ([StringComparer]::OrdinalIgnoreCase)
    foreach ($item in @($directories.ToArray()) + @($files.ToArray())) {
        $ancestorRelative = $item.Relative
        while ($ancestorRelative.Contains('/')) {
            $ancestorRelative = $ancestorRelative.Substring(0,$ancestorRelative.LastIndexOf('/'))
            if (-not $preflightAncestors.Add($ancestorRelative)) { break }
            if ($allPaths.Contains($ancestorRelative)) { throw ('Archive file occupies a destination ancestor: ' + $ancestorRelative) }
            $ancestorPath = Under-Root $projectRoot $ancestorRelative
            Assert-NoReparse $ancestorPath
            if (([IO.File]::Exists($ancestorPath) -or [IO.Directory]::Exists($ancestorPath)) -and -not [IO.Directory]::Exists($ancestorPath)) { throw ('Existing file occupies a destination ancestor: ' + $ancestorRelative) }
        }
    }
    foreach ($directory in $directories) {
        Assert-NoReparse $directory.Destination
        if (([IO.File]::Exists($directory.Destination) -or [IO.Directory]::Exists($directory.Destination)) -and -not [IO.Directory]::Exists($directory.Destination)) { throw ('Directory target changed during preflight: ' + $directory.Relative) }
    }
    # Preflight every target before copying. File.Copy overwrite=false also closes the race with new user files.
    foreach ($file in $files) {
        Assert-NoReparse $file.Destination
        if ([IO.File]::Exists($file.Destination) -or [IO.Directory]::Exists($file.Destination)) {
            if (-not [IO.File]::Exists($file.Destination) -or (File-Hash $file.Destination) -ine (File-Hash $file.Source)) { throw ('Target changed during preflight: ' + $file.Relative) }
        }
    }
    Reset-PathChecks
    foreach ($directory in $directories) {
        Assert-NoReparse $directory.Destination
        [IO.Directory]::CreateDirectory($directory.Destination) | Out-Null
    }
    $copied = 0
    $processed = 0
    Write-Host ('Copying/preserving ' + $files.Count + ' verified files...')
    foreach ($file in $files) {
        Assert-NoReparse $file.Destination
        if (-not ([IO.File]::Exists($file.Destination) -or [IO.Directory]::Exists($file.Destination))) {
            [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($file.Destination)) | Out-Null
            [IO.File]::Copy($file.Source,$file.Destination,$false)
            $copied++
        }
        $processed++
        if (($processed % 1000) -eq 0) { Write-Host ('Restored/preserved ' + $processed + '/' + $files.Count + ' files...') }
    }
    Write-Host ('Verified environment restored: ' + $copied + ' new files; ' + ($files.Count - $copied) + ' identical files preserved.')
    if ($SkipValidation) {
        Write-Host 'SkipValidation: restore only. Machine configuration, Doctor, Build and Handoff were not run.'
    } else {
        Configure-Machine
        foreach ($action in @('Doctor','Build','Handoff')) {
            Write-Host ('TrainerDev: ' + $action)
            & (Join-Path $PSHOME 'powershell.exe') -NoProfile -ExecutionPolicy Bypass -File (Join-Path $projectRoot 'dev/TrainerDev.ps1') -Action $action
            if ($LASTEXITCODE -ne 0) { throw ('TrainerDev ' + $action + ' failed. See dev/runs and rerun setup after fixing the prerequisite.') }
        }
        Write-Host 'Ready: open dev/Trainer.sln or Trainer.code-workspace. Start the trainer with trainer.cmd -Action Run.'
    }
} catch {
    Write-Error -Message $_.Exception.Message -ErrorAction Continue
    exit 1
} finally {
    $token = $null; $headers = $null
    if ($stage -and [IO.Directory]::Exists($stage)) {
        $safePrefix = $projectRoot + '\dev\state\portable-stage-'
        if ($stage.StartsWith($safePrefix,[StringComparison]::OrdinalIgnoreCase)) { Assert-NoReparse $stage; Remove-Item -LiteralPath $stage -Recurse -Force }
    }
}
