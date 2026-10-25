function Resolve-TrainerPath([string]$Relative) {
    $path = [IO.Path]::GetFullPath((Join-Path $script:TrainerRoot $Relative))
    if (-not $path.StartsWith($script:TrainerRoot + [IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw "Путь вне рабочего проекта: $Relative" }
    return $path
}
function New-TrainerId([string]$Prefix) { return $Prefix + '-' + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ') + '-' + [Guid]::NewGuid().ToString('N').Substring(0,8) }
function Write-TrainerJson($Object,[string]$Path,[switch]$Compact) {
    $directory = Split-Path -Parent $Path
    [IO.Directory]::CreateDirectory($directory) | Out-Null
    $temp = $Path + '.' + [Guid]::NewGuid().ToString('N') + '.tmp'
    [IO.File]::WriteAllText($temp,($Object | ConvertTo-Json -Depth 40 -Compress:$Compact),[Text.UTF8Encoding]::new($false))
    Move-Item -LiteralPath $temp -Destination $Path -Force
}
function Get-TrainerHash([string]$Path) {
    # Avoid shell-specific availability of the Windows PowerShell hash function.
    $stream=[IO.File]::OpenRead($Path)
    $sha=[Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-','') }
    finally { $sha.Dispose(); $stream.Dispose() }
}
function Get-TrainerGit {
    $command = Get-Command git -ErrorAction SilentlyContinue
    if (-not $command) { throw 'Git не найден. Doctor показывает доступные зависимости.' }
    return $command.Source
}
function Get-TrainerHead {
    $git = Get-TrainerGit
    $value = & $git -C $script:TrainerRoot rev-parse --verify --quiet HEAD
    if ($LASTEXITCODE -ne 0) { return 'uncommitted' }
    return ($value -join '').Trim()
}
function Get-TrainerSources([string]$Profile) {
    $names = @($script:TrainerConfig.profiles.$Profile.sources)
    if ($names.Count -eq 0) { throw "Нет исходников профиля $Profile" }
    return $names
}
function Get-TrainerNativeStatus {
    $lock = Get-Content -LiteralPath (Join-Path $script:TrainerDevRoot 'config\native-lock.json') -Encoding UTF8 -Raw | ConvertFrom-Json
    foreach ($item in $lock.files) {
        $path = Resolve-TrainerPath $item.path
        $exists = Test-Path -LiteralPath $path -PathType Leaf
        $hash = if ($exists) { Get-TrainerHash $path } else { $null }
        [pscustomobject]@{path=$item.path; exists=$exists; expected=$item.sha256; actual=$hash; matches=($exists -and $hash -eq $item.sha256)}
    }
}
function Assert-TrainerNative {
    $status = @(Get-TrainerNativeStatus)
    $bad = @($status | Where-Object { -not $_.matches })
    if ($bad.Count) { throw ('Изменены/отсутствуют штатные модули: ' + (($bad | ForEach-Object {$_.path}) -join ', ')) }
    return $status
}
function Invoke-TrainerDoctor {
    $id = New-TrainerId 'doctor'
    $folder = Resolve-TrainerPath ('dev/runs/' + $id)
    [IO.Directory]::CreateDirectory($folder) | Out-Null
    $compiler = $script:TrainerConfig.compiler
    $required = @()
    $required += [pscustomobject]@{name='C# .NET Framework 4 compiler';path=$compiler;ok=(Test-Path -LiteralPath $compiler)}
    $required += [pscustomobject]@{name='WinForms x86 runtime';path=$script:TrainerConfig.framework;ok=(Test-Path -LiteralPath $script:TrainerConfig.framework)}
    foreach ($relative in @($script:TrainerConfig.required_files)) {
        $required += [pscustomobject]@{name=$relative;path=(Resolve-TrainerPath $relative);ok=(Test-Path -LiteralPath (Resolve-TrainerPath $relative))}
    }
    $git = Get-Command git -ErrorAction SilentlyContinue
    $required += [pscustomobject]@{name='Git';path=$(if($git){$git.Source}else{$null});ok=($null -ne $git)}
    $native = @(Get-TrainerNativeStatus)
    $optional = @()
    foreach ($tool in @('code','dotnet','python','pwsh')) {
        $command = Get-Command $tool -ErrorAction SilentlyContinue
        $optional += [pscustomobject]@{name=$tool;available=($null -ne $command);path=$(if($command){$command.Source}else{$null})}
    }
    $pythonStatus = $null
    if (Test-Path -LiteralPath $script:TrainerConfig.python) {
        $pythonOutput = & $script:TrainerConfig.python (Join-Path $script:TrainerDevRoot 'tools\check_python.py') 2> (Join-Path $folder 'python-stderr.log')
        $pythonExit = $LASTEXITCODE
        $pythonStatus = ($pythonOutput -join '') | ConvertFrom-Json
        $required += [pscustomobject]@{name='Python native analysis (pefile/capstone)';path=$script:TrainerConfig.python;ok=($pythonExit -eq 0 -and $pythonStatus.ok)}
    } else {
        $required += [pscustomobject]@{name='Python native analysis';path=$script:TrainerConfig.python;ok=$false}
    }
    $distributionPresent = Test-Path -LiteralPath (Resolve-TrainerPath 'work/native_distribution/WORLD3DSERVER/DATA/WORLDS') -PathType Container
    $report = [ordered]@{schema_version=1;id=$id;utc=[DateTime]::UtcNow.ToString('O');required=$required;native_modules=$native;optional_tools=$optional;python_tools=$pythonStatus;passed=(@($required|Where-Object{-not $_.ok}).Count -eq 0 -and @($native|Where-Object{-not $_.matches}).Count -eq 0);target='net40 / WinForms / x86';visualization_3d=$(if($distributionPresent){'data_received_not_integrated'}else{'absent'});manual_137_revision='unverified'}
    Write-TrainerJson $report (Join-Path $folder 'doctor.json')
    $required | Format-Table name,ok,path -AutoSize | Out-Host
    Write-Host ('Штатные DLL: ' + @($native|Where-Object{$_.matches}).Count + '/' + $native.Count + '. Отчёт: ' + $folder)
    if (-not $report.passed) { throw 'Среда неполна или контрольные суммы штатных DLL изменились.' }
}
function New-TrainerBuild {
    param([string]$Profile,[string]$MainClass='TrainerIntegrationHost',[string[]]$ExtraSources=@(),[switch]$Console)
    $id = New-TrainerId 'build'
    $folder = Resolve-TrainerPath ('dev/builds/' + $id)
    [IO.Directory]::CreateDirectory($folder) | Out-Null
    $manifest = [ordered]@{schema_version=1;build_id=$id;utc=[DateTime]::UtcNow.ToString('O');profile=$Profile;main_class=$MainClass;git_head=(Get-TrainerHead);compiler=$script:TrainerConfig.compiler;compiler_sha256=(Get-TrainerHash $script:TrainerConfig.compiler);sources=@();native_modules=@();status='building'}
    $manifestPath = Join-Path $folder 'build.json'
    try {
        $manifest.native_modules = @(Assert-TrainerNative)
        $relativeSources = @()
        if ($Profile -ne 'standalone') { $relativeSources += @(Get-TrainerSources $Profile) }
        $relativeSources += $ExtraSources
        $relativeSources = @($relativeSources | Select-Object -Unique)
        $snapshotSources = @()
        foreach ($relative in $relativeSources) {
            $source = Resolve-TrainerPath $relative
            $snapshot = Join-Path $folder ('source/' + $relative)
            [IO.Directory]::CreateDirectory((Split-Path -Parent $snapshot)) | Out-Null
            Copy-Item -LiteralPath $source -Destination $snapshot
            $manifest.sources += [pscustomobject]@{path=$relative;sha256=(Get-TrainerHash $snapshot)}
            $snapshotSources += $snapshot
        }
        $exeName = if ($Console) { $MainClass + '.exe' } elseif ($Profile -eq 'candidate') { 'TrainerIntegrationHost.pneumatic_candidate.exe' } else { 'TrainerIntegrationHost.exe' }
        $exe = Join-Path $folder $exeName
        $target = if ($Console) {'exe'} else {'winexe'}
        $arguments = @('/nologo','/codepage:65001','/platform:x86',('/target:'+$target),('/main:'+$MainClass),'/debug:pdbonly','/optimize+','/r:System.Windows.Forms.dll','/r:System.Drawing.dll','/r:System.Web.Extensions.dll',('/out:'+$exe)) + $snapshotSources
        $manifest.compiler_arguments = $arguments
        Write-TrainerJson $manifest $manifestPath
        & $script:TrainerConfig.compiler @arguments 2>&1 | Tee-Object -FilePath (Join-Path $folder 'compiler.log') | Out-Host
        $compilerExit = $LASTEXITCODE
        $manifest.compiler_exit_code = $compilerExit
        if ($compilerExit -ne 0 -or -not (Test-Path -LiteralPath $exe)) { throw "Ошибка компиляции ($compilerExit), см. $folder" }
        $manifest.status = 'built'
        $manifest.executable = $exeName
        $manifest.executable_sha256 = Get-TrainerHash $exe
        $manifest.finished_utc = [DateTime]::UtcNow.ToString('O')
        Write-TrainerJson $manifest $manifestPath
        if (-not $Console) { Write-TrainerJson @{id=$id;path=$folder;profile=$Profile;status='built';executable_sha256=$manifest.executable_sha256} (Resolve-TrainerPath 'dev/state/latest-build.json') }
        return [pscustomobject]@{id=$id;folder=$folder;exe=$exe;manifest=$manifest}
    } catch {
        $manifest.status='failed';$manifest.error=$_.ToString();$manifest.finished_utc=[DateTime]::UtcNow.ToString('O')
        Write-TrainerJson $manifest $manifestPath
        throw
    }
}
function Get-TrainerLease {
    $path = Resolve-TrainerPath 'dev/state/native-run.lock'
    [IO.Directory]::CreateDirectory((Split-Path -Parent $path)) | Out-Null
    try { return [IO.File]::Open($path,[IO.FileMode]::OpenOrCreate,[IO.FileAccess]::ReadWrite,[IO.FileShare]::None) }
    catch { throw 'Другой запуск среды выполняет проверки/развёртывание. Дождитесь завершения.' }
}
function Set-TrainerProcessEnvironment($StartInfo,[string]$Folder,[string]$BuildId,[string]$Kind) {
    $StartInfo.EnvironmentVariables['TRAINER_SESSION_DIR'] = Join-Path $Folder 'sessions'
    $StartInfo.EnvironmentVariables['TRAINER_SOURCE_BUILD_ID'] = $BuildId
    $StartInfo.EnvironmentVariables['TRAINER_RUN_KIND'] = $Kind
    $StartInfo.EnvironmentVariables['TRAINER_RESULT_DIR'] = Join-Path $Folder 'results'
}
function Set-TrainerRuntimeRegistry([string]$Folder) {
    # Communication1 unit initialization writes TypeLoc.klub using this HKCU32
    # path before InitCommunication. Rebase it to the guarded working copy.
    $runtime = Resolve-TrainerPath 'work/trenager_static/WORK_TRENAGER_3D'
    $typeFile = Join-Path $runtime 'DATA\LIB\ENGINE\TypeLoc.klub'
    if (-not (Test-Path -LiteralPath $typeFile -PathType Leaf)) { throw 'Рабочая копия TypeLoc.klub отсутствует; WorkPath не изменён.' }
    $base = [Microsoft.Win32.RegistryKey]::OpenBaseKey([Microsoft.Win32.RegistryHive]::CurrentUser,[Microsoft.Win32.RegistryView]::Registry32)
    $key = $null
    $record = [ordered]@{schema_version=1;view='Registry32';key='HKCU\Software\SPECTR\TRENAGER\Path';name='WorkPath';utc=[DateTime]::UtcNow.ToString('O');value=$runtime;value_kind='String';type_file=$typeFile;type_file_sha256_before=(Get-TrainerHash $typeFile);status='preparing'}
    $recordPath = Join-Path $Folder 'registry-runtime-path.json'
    try {
        $key = $base.OpenSubKey('Software\SPECTR\TRENAGER\Path',$true)
        $record.existed = ($null -ne $key -and @($key.GetValueNames()) -contains 'WorkPath')
        $record.previous = if ($record.existed) { $key.GetValue('WorkPath',$null,[Microsoft.Win32.RegistryValueOptions]::DoNotExpandEnvironmentNames) } else { $null }
        $record.previous_kind = if ($record.existed) { $key.GetValueKind('WorkPath').ToString() } else { $null }
        $record.changed = (-not $record.existed -or $record.previous_kind -ne 'String' -or $record.previous -cne $runtime)
        # Save the original value before making the persistent relocation repair.
        Write-TrainerJson $record $recordPath
        if ($null -eq $key) { $key = $base.CreateSubKey('Software\SPECTR\TRENAGER\Path') }
        if ($record.changed) { $key.SetValue('WorkPath',$runtime,[Microsoft.Win32.RegistryValueKind]::String) }
        if ($key.GetValue('WorkPath',$null,[Microsoft.Win32.RegistryValueOptions]::DoNotExpandEnvironmentNames) -cne $runtime -or $key.GetValueKind('WorkPath') -ne [Microsoft.Win32.RegistryValueKind]::String) { throw 'WorkPath не соответствует рабочей копии после записи.' }
        $record.status = 'ready'
        Write-TrainerJson $record $recordPath
    } catch {
        $record.status='failed';$record.error=$_.ToString()
        Write-TrainerJson $record $recordPath
        throw
    } finally {
        if ($key) { $key.Dispose() }
        $base.Dispose()
    }
}
function Deploy-TrainerBuild($Build,[string]$ExeName) {
    $destination = Resolve-TrainerPath ('outputs/' + $ExeName)
    if (Test-Path -LiteralPath $destination) {
        $previous=Join-Path $Build.folder ('previous/'+$ExeName)
        [IO.Directory]::CreateDirectory((Split-Path -Parent $previous)) | Out-Null
        Copy-Item -LiteralPath $destination -Destination $previous
        if(Test-Path -LiteralPath ($destination+'.build.json')){Copy-Item -LiteralPath ($destination+'.build.json') -Destination ($previous+'.build.json')}
        $previousPdb=[IO.Path]::ChangeExtension($destination,'.pdb')
        if(Test-Path -LiteralPath $previousPdb){Copy-Item -LiteralPath $previousPdb -Destination ([IO.Path]::ChangeExtension($previous,'.pdb'))}
        Write-TrainerJson @{path=$destination;sha256=(Get-TrainerHash $previous);replaced_by_build=$Build.id} (Join-Path $Build.folder 'previous/deployment.json')
    }
    Copy-Item -LiteralPath $Build.exe -Destination $destination -Force
    $pdb = [IO.Path]::ChangeExtension($Build.exe,'.pdb')
    if (Test-Path -LiteralPath $pdb) { Copy-Item -LiteralPath $pdb -Destination ([IO.Path]::ChangeExtension($destination,'.pdb')) -Force }
    Write-TrainerJson $Build.manifest ($destination + '.build.json')
    return $destination
}
function Assert-TrainerProbeResult([string]$Id,$Json) {
    if ($null -ne $Json.PSObject.Properties['passed'] -and -not $Json.passed) { throw "Проверка $Id вернула passed=false" }
    if ($Id -eq 'SectionPantographPressure') {
        $expected = @('0,0,0,0','0,1,0,0','1,0,0,0','1,1,0,0')
        if (@($Json.rows).Count -ne 4) { throw 'Неполные rising rows токоприёмников.' }
        for ($i=0;$i -lt 4;$i++) {
            if (($Json.rows[$i].feedbacks -join ',') -ne $expected[$i]) { throw "Неверная секционная связь RS6 в rising row $i" }
            $voltage = if ($i -eq 0) {0} else {3100}
            if ([double]$Json.rows[$i].native_line_voltage -ne $voltage) { throw "Неверное напряжение в rising row $i" }
        }
    }
}
function Invoke-TrainerTests {
    param([string]$Suite,[string[]]$Ids)
    $catalog = Get-Content -LiteralPath (Join-Path $script:TrainerDevRoot 'config\probes.json') -Encoding UTF8 -Raw | ConvertFrom-Json
    $isSelected=($Ids -and $Ids.Count -gt 0)
    if (-not $Ids -or $Ids.Count -eq 0) { $Ids = @($script:TrainerConfig.suites.$Suite) }
    $Ids=@($Ids | ForEach-Object { $_.Split(',') | ForEach-Object {$_.Trim()} } | Where-Object {$_} | Select-Object -Unique)
    $chosen = @()
    foreach ($id in $Ids) {
        $entry = @($catalog.probes | Where-Object {$_.id -eq $id})
        if ($entry.Count -ne 1) { throw "Неизвестная проверка: $id" }
        if (-not $entry[0].enabled -or $entry[0].kind -in @('unsafe','obsolete')) { throw "Проверка $id отключена: $($entry[0].scope)" }
        $chosen += $entry[0]
    }
    $lease = Get-TrainerLease
    $runId = New-TrainerId 'test'
    $folder = Resolve-TrainerPath ('dev/runs/' + $runId)
    [IO.Directory]::CreateDirectory($folder) | Out-Null
    $report = [ordered]@{schema_version=1;run_id=$runId;utc=[DateTime]::UtcNow.ToString('O');suite=$(if($isSelected){'selected'}else{$Suite});git_head=(Get-TrainerHead);status='running';results=@();limitations='Regression success does not establish exact RE137 applicability or complete pneumatic calibration.'}
    try {
        foreach ($entry in $chosen) {
            $probeFolder = Join-Path $folder $entry.id
            [IO.Directory]::CreateDirectory($probeFolder) | Out-Null
            $result = [ordered]@{id=$entry.id;scope=$entry.scope;status='running';artifacts=@()}
            $process = $null
            try {
                $sources = @($entry.dependencies) + @($entry.source)
                $build = New-TrainerBuild -Profile $entry.profile -MainClass $entry.main_class -ExtraSources $sources -Console
                $result.build_id=$build.id
                # Unique deployed name keeps concurrent legacy launches from replacing this binary.
                $exe = Deploy-TrainerBuild $build ($entry.id + '.' + $runId + '.exe')
                $requiresNative = ($entry.profile -ne 'standalone' -and ($null -eq $entry.PSObject.Properties['requires_native_initialization'] -or $entry.requires_native_initialization))
                if ($requiresNative) { Set-TrainerRuntimeRegistry $probeFolder }
                $start = New-Object Diagnostics.ProcessStartInfo
                $start.FileName=$exe;$start.WorkingDirectory=(Resolve-TrainerPath 'outputs');$start.UseShellExecute=$false;$start.CreateNoWindow=$true
                $start.RedirectStandardOutput=$true;$start.RedirectStandardError=$true
                $captureEncoding=if($entry.profile -eq 'standalone'){[Text.Encoding]::GetEncoding([Globalization.CultureInfo]::CurrentCulture.TextInfo.OEMCodePage)}else{[Text.UTF8Encoding]::new($false)}
                $start.StandardOutputEncoding=$captureEncoding;$start.StandardErrorEncoding=$captureEncoding
                $start.Arguments=(@($entry.arguments) -join ' ')
                Set-TrainerProcessEnvironment $start $probeFolder $build.id 'probe'
                if (-not ('TrainerDevelopment.ConsoleCapture' -as [type])) { Add-Type -Path (Join-Path $script:TrainerDevRoot 'tools\ConsoleCapture.cs') }
                $started = [DateTime]::UtcNow
                $process = New-Object Diagnostics.Process
                $process.StartInfo=$start
                if (-not $process.Start()) { throw "Не удалось запустить $($entry.id)" }
                $result.pid=$process.Id
                $stdout=[TrainerDevelopment.ConsoleCapture]::Start($process.StandardOutput,(Join-Path $probeFolder 'stdout.log'))
                $stderr=[TrainerDevelopment.ConsoleCapture]::Start($process.StandardError,(Join-Path $probeFolder 'stderr.log'))
                $completed=$process.WaitForExit([int]$entry.timeout_seconds * 1000)
                if (-not $completed) { $process.Kill();$process.WaitForExit();$result.timed_out=$true }
                if (-not [Threading.Tasks.Task]::WaitAll([Threading.Tasks.Task[]]@($stdout,$stderr),5000)) {
                    $process.StandardOutput.Close();$process.StandardError.Close()
                    throw 'Потоки вывода не закрылись за 5 с после окончания процесса; сохранён частичный вывод.'
                }
                $result.exit_code=$process.ExitCode
                foreach ($relative in $entry.result_files) {
                    $source=Join-Path $probeFolder ('results/'+[IO.Path]::GetFileName($relative))
                    if (-not (Test-Path -LiteralPath $source)) { throw "Не создан результат: $relative" }
                    if ((Get-Item -LiteralPath $source).LastWriteTimeUtc -lt $started) { throw "Устаревший результат, не создан текущей проверкой: $relative" }
                    $target=Join-Path $probeFolder ([IO.Path]::GetFileName($relative))
                    Copy-Item -LiteralPath $source -Destination $target
                    $result.artifacts += [pscustomobject]@{path=[IO.Path]::GetFileName($target);sha256=(Get-TrainerHash $target)}
                    if ([IO.Path]::GetExtension($target) -eq '.json') { Assert-TrainerProbeResult $entry.id (Get-Content -LiteralPath $target -Encoding UTF8 -Raw | ConvertFrom-Json) }
                }
                if (-not $completed) { throw "Таймаут $($entry.timeout_seconds) с" }
                if ($result.exit_code -ne 0) { throw "Процесс вернул код $($result.exit_code)" }
                if ($entry.profile -ne 'standalone') {
                    $sessions=@(Get-ChildItem -LiteralPath (Join-Path $probeFolder 'sessions') -Recurse -File -Filter 'session.json')
                    if ($sessions.Count -ne 1) { throw 'Проверка не сохранила ровно один манифест сессии.' }
                    $session=Get-Content -LiteralPath $sessions[0].FullName -Encoding UTF8 -Raw | ConvertFrom-Json
                    if ($session.state -ne 'closed' -or $session.exceptionCount -ne 0 -or $session.loggingFailureCount -ne 0) { throw "Ошибки выполнения/логирования сессии: $($session.state)" }
                    if ($session.pid -ne $result.pid -or $session.sourceBuildId -ne $build.id -or $session.executableSha256 -ne $build.manifest.executable_sha256) { throw 'Манифест сессии не соответствует запущенной сборке/PID.' }
                    $result.session=[ordered]@{id=$session.sessionId;state=$session.state;exceptions=$session.exceptionCount;logging_failures=$session.loggingFailureCount;source_build_id=$session.sourceBuildId}
                }
                $result.status='passed'
                Write-Host ($entry.id + ': PASS')
            } catch {
                $result.status='failed';$result.error=$_.ToString()
                Write-Host ($entry.id + ': FAIL — ' + $_.Exception.Message)
            } finally {
                if ($process) {
                    try { if ($process.Id -gt 0 -and -not $process.HasExited) {$process.Kill();$process.WaitForExit(5000)|Out-Null} } catch { $result.cleanup_error=$_.ToString() }
                    $process.Dispose()
                }
                $result.finished_utc=[DateTime]::UtcNow.ToString('O')
                Write-TrainerJson $result (Join-Path $probeFolder 'result.json')
                $report.results += $result
                Write-TrainerJson $report (Join-Path $folder 'report.json')
            }
        }
        $report.status=if (@($report.results|Where-Object{$_.status -ne 'passed'}).Count) {'failed'} else {'passed'}
        $report.finished_utc=[DateTime]::UtcNow.ToString('O')
        Write-TrainerJson $report (Join-Path $folder 'report.json')
        $compactReport=@{id=$runId;path=$folder;status=$report.status;suite=$report.suite;probes=@($report.results|ForEach-Object{$_.id});failed=@($report.results|Where-Object{$_.status -ne 'passed'}|ForEach-Object{$_.id})}
        Write-TrainerJson $compactReport (Resolve-TrainerPath 'dev/state/latest-test.json')
        if (-not $isSelected -and $Suite -eq 'regression') { Write-TrainerJson $compactReport (Resolve-TrainerPath 'dev/state/latest-regression.json') }
        Write-Host ('Результаты и логи: ' + $folder)
        if ($report.status -ne 'passed') { throw 'Регрессионный набор завершился с ошибками. Все результаты сохранены.' }
    } finally { $lease.Dispose() }
}
function Start-TrainerDevelopment {
    param([string]$Profile)
    $lease=Get-TrainerLease
    try {
        $live=@(Get-Process -ErrorAction SilentlyContinue | Where-Object { try {$_.Path -like ((Resolve-TrainerPath 'outputs')+'\TrainerIntegrationHost*.exe')} catch {$false} })
        if ($live.Count) { throw ('Стенд уже работает, закройте его перед Run: PID ' + (($live|ForEach-Object{$_.Id}) -join ', ')) }
        $build=New-TrainerBuild -Profile $Profile
        $exe=Deploy-TrainerBuild $build ([IO.Path]::GetFileName($build.exe))
        $runId=New-TrainerId 'interactive'
        $folder=Resolve-TrainerPath ('dev/runs/'+$runId)
        [IO.Directory]::CreateDirectory($folder) | Out-Null
        Set-TrainerRuntimeRegistry $folder
        $key='HKCU:\Software\SPECTR\TRENAGER\LOCOPARAM\1'
        $registry=@()
        foreach ($item in @(@{name='GETBMPMSUD_';value='1'},@{name='BLOKPB';value='0'},@{name='KLUBUTYPE';value='БИЛ-УТ'})) {
            $existing=Get-ItemProperty -LiteralPath $key -Name $item.name -ErrorAction SilentlyContinue
            $registry += [pscustomobject]@{path=$key;name=$item.name;existed=($null -ne $existing);previous=$(if($existing){$existing.($item.name)}else{$null});value=$item.value}
        }
        Write-TrainerJson $registry (Join-Path $folder 'registry-before.json')
        foreach ($item in $registry) { New-ItemProperty -LiteralPath $key -Name $item.name -Value $item.value -PropertyType String -Force | Out-Null }
        $start=New-Object Diagnostics.ProcessStartInfo
        $start.FileName=$exe;$start.WorkingDirectory=(Resolve-TrainerPath 'outputs');$start.UseShellExecute=$false;$start.CreateNoWindow=$true;$start.WindowStyle='Hidden'
        Set-TrainerProcessEnvironment $start $folder $build.id 'interactive'
        $process=[Diagnostics.Process]::Start($start)
        Write-TrainerJson @{schema_version=1;id=$runId;build_id=$build.id;pid=$process.Id;utc=[DateTime]::UtcNow.ToString('O');exe=$exe;status='started'} (Join-Path $folder 'run.json')
        Write-Host ('Запущен PID '+$process.Id+'. Логи: '+$folder)
        $process.Dispose()
    } finally { $lease.Dispose() }
}
function Save-TrainerCheckpoint {
    param([string]$Name,[string]$Note)
    if ($Name -notmatch '^[\p{L}\p{N}_ .-]{1,100}$') { throw 'Имя: 1–100 букв, цифр, пробелов, . _ -' }
    $lease=Get-TrainerLease
    try {
        $git=Get-TrainerGit
        & $git -C $script:TrainerRoot -c core.safecrlf=false add --all
        if ($LASTEXITCODE -ne 0) { throw 'Git add не выполнен.' }
        $id=New-TrainerId 'checkpoint'
        $folder=Resolve-TrainerPath ('dev/checkpoints/'+$id)
        [IO.Directory]::CreateDirectory($folder) | Out-Null
        & $git -C $script:TrainerRoot diff --cached --binary ('--output='+ (Join-Path $folder 'changes.patch'))
        if ($LASTEXITCODE -ne 0) { throw 'Не удалось сохранить diff.' }
        $details=[ordered]@{schema_version=1;id=$id;utc=[DateTime]::UtcNow.ToString('O');name=$Name;note=$Note;previous_head=(Get-TrainerHead);sources=@()}
        $changeLog=Join-Path $script:TrainerDevRoot 'CHANGELOG.jsonl'
        [IO.File]::AppendAllText($changeLog,((@{id=$id;utc=$details.utc;name=$Name;note=$Note;previous_head=$details.previous_head}|ConvertTo-Json -Compress)+[Environment]::NewLine),[Text.UTF8Encoding]::new($false))
        & $git -C $script:TrainerRoot add -- dev/CHANGELOG.jsonl
        & $git -C $script:TrainerRoot commit -m ("Тренажёр: $Name") 2>&1 | Tee-Object -FilePath (Join-Path $folder 'git.log') | Select-Object -Last 4 | Out-Host
        if ($LASTEXITCODE -ne 0) { throw 'Git commit не выполнен; diff и запись журнала сохранены.' }
        $details.commit=Get-TrainerHead
        if ($details.previous_head -eq 'uncommitted') {
            & $git -C $script:TrainerRoot show --format= --binary ('--output='+(Join-Path $folder 'changes.patch')) $details.commit
        } else {
            & $git -C $script:TrainerRoot diff $details.previous_head $details.commit --binary ('--output='+(Join-Path $folder 'changes.patch'))
        }
        if ($LASTEXITCODE -ne 0) { throw 'Commit сохранён, но его patch не создан.' }
        # Blob IDs describe the committed bytes, even if an editor changes the working tree concurrently.
        foreach ($line in (& $git -C $script:TrainerRoot -c core.quotePath=false ls-tree -r $details.commit)) {
            if ($line -match '^\d+ blob ([0-9a-f]+)\t(.+)$') { $details.sources += [pscustomobject]@{path=$Matches[2];git_blob=$Matches[1]} }
        }
        Write-TrainerJson $details (Join-Path $folder 'checkpoint.json')
        Write-TrainerJson @{id=$id;commit=$details.commit;path=$folder} (Resolve-TrainerPath 'dev/state/latest-checkpoint.json')
        Write-Host ('Сохранено: '+$details.commit+'; '+$folder)
    } finally { $lease.Dispose() }
}
function Get-TrainerStatus {
    $git=Get-TrainerGit
    Write-Host ('Проект: '+$script:TrainerRoot)
    Write-Host ('Git: '+(Get-TrainerHead))
    & $git -C $script:TrainerRoot status --short
    foreach ($name in @('latest-test.json','latest-checkpoint.json')) {
        $path=Resolve-TrainerPath ('dev/state/'+$name)
        if (Test-Path -LiteralPath $path) { Get-Content -LiteralPath $path | Out-Host }
    }
}
function Save-TrainerArchive {
    param([switch]$IncludeRuntime)
    $lease=Get-TrainerLease
    try {
        Add-Type -AssemblyName System.IO.Compression
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        $id=New-TrainerId 'archive'
        $folder=Resolve-TrainerPath ('dev/archives/'+$id)
        [IO.Directory]::CreateDirectory($folder) | Out-Null
        $git=Get-TrainerGit
        & $git -C $script:TrainerRoot bundle create (Join-Path $folder 'history.bundle') --all
        if ($LASTEXITCODE -ne 0) { throw 'Не удалось сохранить историю Git.' }
        & $git -C $script:TrainerRoot diff HEAD --binary ('--output='+ (Join-Path $folder 'uncommitted.patch'))
        if ($LASTEXITCODE -ne 0) { throw 'Не удалось сохранить текущие изменения.' }
        $files=@()
        foreach ($relative in (& $git -C $script:TrainerRoot ls-files --cached --others --exclude-standard)) {
            $source=Resolve-TrainerPath $relative
            if (Test-Path -LiteralPath $source -PathType Leaf) { $files += Get-Item -LiteralPath $source }
        }
        foreach ($relative in @('dev/runs','dev/builds','dev/checkpoints','dev/history','dev/state','outputs')) {
            $directory=Resolve-TrainerPath $relative
            if (Test-Path -LiteralPath $directory) { $files += @(Get-ChildItem -LiteralPath $directory -Recurse -File) }
        }
        if ($IncludeRuntime) {
            $files += @(Get-ChildItem -LiteralPath (Resolve-TrainerPath 'work') -Recurse -File)
        }
        $archive=Join-Path $folder 'artifacts.zip'
        $zip=[IO.Compression.ZipFile]::Open($archive,[IO.Compression.ZipArchiveMode]::Create)
        $inventory=@()
        try {
            foreach ($file in @($files|Sort-Object FullName -Unique)) {
                $relative=$file.FullName.Substring($script:TrainerRoot.Length+1).Replace('\','/')
                if ($relative -eq 'dev/state/native-run.lock') { continue }
                $inventory += Add-TrainerZipSnapshot $zip $file.FullName $relative
            }
        } finally { $zip.Dispose() }
        Write-TrainerJson @{schema_version=1;id=$id;utc=[DateTime]::UtcNow.ToString('O');git_head=(Get-TrainerHead);includes_runtime=[bool]$IncludeRuntime;files=$inventory;archive_sha256=(Get-TrainerHash $archive);bundle_sha256=(Get-TrainerHash (Join-Path $folder 'history.bundle'));live_files_can_change=$true} (Join-Path $folder 'archive.json')
        Write-Host ('Резервная копия: '+$folder)
    } finally { $lease.Dispose() }
}
function Add-TrainerZipSnapshot($Zip,[string]$Path,[string]$Relative) {
    $before=(Get-Item -LiteralPath $Path).LastWriteTimeUtc.Ticks
    $input=[IO.File]::Open($Path,[IO.FileMode]::Open,[IO.FileAccess]::Read,([IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete))
    $output=$null;$digest=$null
    try {
        # Copy exactly the length observed on open. A live append cannot extend the snapshot indefinitely.
        $remaining=$input.Length
        $length=$remaining
        $entry=$Zip.CreateEntry($Relative,[IO.Compression.CompressionLevel]::Optimal)
        $output=$entry.Open()
        $digest=[Security.Cryptography.SHA256]::Create()
        $buffer=New-Object byte[] 131072
        while ($remaining -gt 0) {
            $count=$input.Read($buffer,0,[int][Math]::Min($remaining,$buffer.Length))
            if ($count -eq 0) { throw "Файл уменьшился при архивировании: $Path" }
            $output.Write($buffer,0,$count)
            $digest.TransformBlock($buffer,0,$count,$buffer,0) | Out-Null
            $remaining-=$count
        }
        $digest.TransformFinalBlock([byte[]]@(),0,0) | Out-Null
        $hash=[BitConverter]::ToString($digest.Hash).Replace('-','')
        return [pscustomobject]@{path=$Relative;bytes=$length;sha256=$hash;changed_during_copy=((Get-Item -LiteralPath $Path).LastWriteTimeUtc.Ticks -ne $before)}
    } finally { if($digest){$digest.Dispose()};if($output){$output.Dispose()};$input.Dispose() }
}
function Save-TrainerContext {
    param([string]$Reason='handoff')
    $git=Get-TrainerGit
    $dirty=@(& $git -C $script:TrainerRoot status --porcelain)
    $latest=[ordered]@{}
    foreach ($name in @('build','test','regression','checkpoint')) {
        $path=Resolve-TrainerPath ('dev/state/latest-'+$name+'.json')
        $latest[$name]=if(Test-Path -LiteralPath $path){Get-Content -LiteralPath $path -Encoding UTF8 -Raw | ConvertFrom-Json}else{$null}
        if ($null -ne $latest[$name] -and $latest[$name].path.StartsWith($script:TrainerRoot + '\',[StringComparison]::OrdinalIgnoreCase)) {
            $latest[$name].path=$latest[$name].path.Substring($script:TrainerRoot.Length+1).Replace('\','/')
        }
    }
    $context=[ordered]@{
        schema_version=1
        updated_utc=[DateTime]::UtcNow.ToString('O')
        reason=$Reason
        environment_index='dev/environment-context.json'
        git_head=(Get-TrainerHead)
        working_tree=[ordered]@{changed_files=$dirty.Count;first_files=@($dirty|Select-Object -First 20);more=($dirty.Count -gt 20)}
        latest=$latest
        active_focus='РЭ1: 171/171страниц, текущаяветкаcodex/re-part1. Приоритетпользователя—внутренниеDLLикалибровки. ActualTyga8EC6 ужеимеетretained.tfixearly750ДОM/F; старыйlate-onlydefectотозван. NativeTyga750Clamp035939967-f0224872PASS117checks/1211cycles:raw766.32→750,ненулеваяM/Fсогласована,4hooksrestored. NativeV=ms,старыеkmh/raw-forceтаблицынеприменимы. SA47новыйкодраньше19/19PASS, модельещёpartial;АВПотключено.'
        next_model_question='Далееactualnativeкалибровки:единыйаналогUкс(Pult3100/Tyga3200), паспортныйF(v)стендпри3kV/D1210/η.98, затемконкретныеCF/R/силовыепараметры послеevidence; 750нормойРЭнепринят. НовыйтормознойкалибровочныйконтрактFTk0/R2/AR1позагрузке. СохраненыbacklogSA47fastclick/supplyKR22/cold,С→СП400/410А,Riso,повторныеторможения/150s,TM<3.4,АВПfullroute/exact137+.'
        read_next=@('dev/environment-context.json','dev/state/context.json')
        avoid_reading=@('whole telemetry.csv','all historical logs','all disassembly dumps')
        source_of_truth='outputs/*.cs; versioned snapshots dev/builds/<id>/source; Git checkpoints'

        native_distribution=[ordered]@{path='work/native_distribution';catalog='dev/config/native-distribution.json';integration_status='not_validated'}
    }
    Write-TrainerJson $context (Resolve-TrainerPath 'dev/state/context.json') -Compact
}

