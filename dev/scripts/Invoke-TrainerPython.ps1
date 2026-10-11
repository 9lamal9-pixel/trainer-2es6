[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$Script,
    [string[]]$Arguments = @()
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$taskId = 'analysis-' + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ') + '-' + [Guid]::NewGuid().ToString('N')
$taskFolder = Join-Path $taskRoot ('dev\runs\' + $taskId)
[IO.Directory]::CreateDirectory($taskFolder) | Out-Null
$taskStdoutPath = Join-Path $taskFolder 'stdout.log'
$taskStderrPath = Join-Path $taskFolder 'stderr.log'
$taskResultPath = Join-Path $taskFolder 'result.json'
$taskUtf8 = New-Object Text.UTF8Encoding($false)
$taskResult = [ordered]@{
    schema_version = 1; id = $taskId; status = 'running'; started_utc = [DateTime]::UtcNow.ToString('O')
    cwd = $taskRoot; requested_script = $Script; script = $null; script_sha256 = $null
    python = $null; argv = @(); pythonpath = @(); process_id = $null; exit_code = $null
    stdout = 'stdout.log'; stderr = 'stderr.log'; capture_drained = $false; error = $null
}
function Save-AnalysisResult {
    [IO.File]::WriteAllText($taskResultPath, ($taskResult | ConvertTo-Json -Depth 8), $taskUtf8)
}
function ConvertTo-ProcessArgument([string]$Value) {
    # Windows CRT argv rules: double runs of backslashes before quotes and at the end.
    $taskEscaped = [Regex]::Replace($Value, '(\\*)"', '$1$1\"')
    $taskEscaped = [Regex]::Replace($taskEscaped, '(\\+)$', '$1$1')
    return '"' + $taskEscaped + '"'
}
Save-AnalysisResult
$taskProcess = $null
$taskStarted = $false
$taskCaptures = @()
$taskCode = 1
try {
    if ([IO.Path]::IsPathRooted($Script) -or $Script.Contains(':')) { throw '-Script должен быть путём относительно корня проекта.' }
    $taskScriptPath = [IO.Path]::GetFullPath((Join-Path $taskRoot $Script))
    $taskRootPrefix = $taskRoot.TrimEnd('\') + '\'
    if (-not $taskScriptPath.StartsWith($taskRootPrefix, [StringComparison]::OrdinalIgnoreCase)) { throw 'Скрипт находится за пределами проекта.' }
    if (-not (Test-Path -LiteralPath $taskScriptPath -PathType Leaf) -or [IO.Path]::GetExtension($taskScriptPath) -ne '.py') { throw 'Не найден Python-скрипт проекта: ' + $Script }
    # Reject links inside the requested path so a local link cannot escape this root.
    $taskAncestor = Get-Item -LiteralPath $taskScriptPath -Force
    while ($null -ne $taskAncestor -and $taskAncestor.FullName -ne $taskRoot) {
        if (($taskAncestor.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) { throw 'Путь скрипта содержит ссылку/reparse point; укажите обычный файл проекта.' }
        $taskAncestor = Get-Item -LiteralPath (Split-Path -Parent $taskAncestor.FullName) -Force
    }
    $taskConfig = Get-Content -LiteralPath (Join-Path $taskRoot 'dev\config\python-tools.json') -Encoding UTF8 -Raw | ConvertFrom-Json
    if (-not (Test-Path -LiteralPath $taskConfig.interpreter -PathType Leaf)) { throw 'Python из python-tools.json не найден.' }
    $taskVendor = @($taskConfig.vendor_paths | ForEach-Object {
        $taskPath = [IO.Path]::GetFullPath((Join-Path $taskRoot $_))
        if (-not $taskPath.StartsWith($taskRootPrefix, [StringComparison]::OrdinalIgnoreCase)) { throw 'Каталог Python выходит за пределы проекта.' }
        if (-not (Test-Path -LiteralPath $taskPath -PathType Container)) { throw 'Не найден локальный каталог Python: ' + $taskPath }
        $taskPath
    })
    $taskResult.script = $taskScriptPath
    $taskResult.script_sha256 = (Get-FileHash -LiteralPath $taskScriptPath -Algorithm SHA256).Hash
    $taskResult.python = $taskConfig.interpreter
    $taskResult.argv = @($taskScriptPath) + @($Arguments)
    $taskResult.pythonpath = $taskVendor
    Save-AnalysisResult
    if (-not ('TrainerDevelopment.ConsoleCapture' -as [type])) { Add-Type -Path (Join-Path $taskRoot 'dev\tools\ConsoleCapture.cs') }
    $taskPsi = New-Object Diagnostics.ProcessStartInfo
    $taskPsi.FileName = $taskConfig.interpreter
    $taskPsi.Arguments = (@($taskResult.argv | ForEach-Object { ConvertTo-ProcessArgument $_ }) -join ' ')
    $taskPsi.WorkingDirectory = $taskRoot
    $taskPsi.UseShellExecute = $false
    $taskPsi.CreateNoWindow = $true
    $taskPsi.RedirectStandardOutput = $true
    $taskPsi.RedirectStandardError = $true
    $taskPsi.StandardOutputEncoding = $taskUtf8
    $taskPsi.StandardErrorEncoding = $taskUtf8
    $taskPsi.EnvironmentVariables['PYTHONPATH'] = $taskVendor -join [IO.Path]::PathSeparator
    $taskPsi.EnvironmentVariables['PYTHONUTF8'] = '1'
    $taskPsi.EnvironmentVariables['PYTHONIOENCODING'] = 'utf-8'
    $taskPsi.EnvironmentVariables['PYTHONDONTWRITEBYTECODE'] = '1'
    $taskProcess = New-Object Diagnostics.Process
    $taskProcess.StartInfo = $taskPsi
    if (-not $taskProcess.Start()) { throw 'Не удалось запустить выбранный Python.' }
    $taskStarted = $true
    $taskResult.process_id = $taskProcess.Id
    Save-AnalysisResult
    $taskCaptures = @(
        [TrainerDevelopment.ConsoleCapture]::Start($taskProcess.StandardOutput, $taskStdoutPath),
        [TrainerDevelopment.ConsoleCapture]::Start($taskProcess.StandardError, $taskStderrPath)
    )
    while (-not $taskProcess.WaitForExit(200)) {
        if (@($taskCaptures | Where-Object { $_.IsFaulted }).Count -gt 0) { throw 'Не удалось сохранять поток вывода анализа.' }
    }
    $taskResult.exit_code = $taskProcess.ExitCode
    $taskResult.capture_drained = [Threading.Tasks.Task]::WaitAll([Threading.Tasks.Task[]]$taskCaptures, 5000)
    if (-not $taskResult.capture_drained) { throw 'Превышены 5 секунд ожидания завершения потоков вывода.' }
    $taskCode = $taskProcess.ExitCode
    $taskResult.status = if ($taskCode -eq 0) { 'completed' } else { 'failed' }
} catch {
    $taskResult.status = 'failed'
    $taskResult.error = $_.Exception.Message
    if ($taskStarted -and $null -ne $taskProcess) {
        try {
            if (-not $taskProcess.HasExited) { $taskProcess.Kill(); [void]$taskProcess.WaitForExit(5000) }
            if ($taskProcess.HasExited) { $taskResult.exit_code = $taskProcess.ExitCode }
        } catch { $taskResult.error += '; own-process cleanup: ' + $_.Exception.Message }
    }
} finally {
    if ($null -ne $taskProcess) { $taskProcess.Dispose() }
    foreach ($taskLogPath in @($taskStdoutPath, $taskStderrPath)) {
        if (-not [IO.File]::Exists($taskLogPath)) { [IO.File]::WriteAllText($taskLogPath, '', $taskUtf8) }
    }
    $taskResult.finished_utc = [DateTime]::UtcNow.ToString('O')
    Save-AnalysisResult
}
Write-Host ('Анализ: ' + $taskResult.status + '; логи: ' + $taskFolder)
if ($taskResult.error) { Write-Host $taskResult.error }
exit $taskCode
