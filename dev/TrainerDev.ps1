[CmdletBinding()]
param(
    [ValidateSet('Doctor','Build','Test','Run','Checkpoint','Status','Archive','Fix','Handoff')][string]$Action = 'Status',
    [ValidateSet('candidate','integration')][string]$Profile = 'candidate',
    [ValidateSet('smoke','regression')][string]$Suite = 'smoke',
    [string[]]$Probe,
    [string]$Name = 'checkpoint',
    [string]$Note = '',
    [switch]$IncludeRuntime
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2
. (Join-Path $PSScriptRoot 'scripts\TrainerDev.Common.ps1')
$script:TrainerRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$script:TrainerDevRoot = $PSScriptRoot
$script:TrainerConfig = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'config\environment.json') -Encoding UTF8 -Raw | ConvertFrom-Json
$commandId=New-TrainerId 'command'
$commandFolder=Resolve-TrainerPath ('dev/runs/'+$commandId)
[IO.Directory]::CreateDirectory($commandFolder) | Out-Null
$commandReport=[ordered]@{schema_version=1;id=$commandId;utc=[DateTime]::UtcNow.ToString('O');action=$Action;profile=$Profile;suite=$Suite;probes=@($Probe);name=$Name;note=$Note;status='running'}
Write-TrainerJson $commandReport (Join-Path $commandFolder 'command.json')
$transcriptStarted=$false
try {
    Start-Transcript -LiteralPath (Join-Path $commandFolder 'console.log') | Out-Null
    $transcriptStarted=$true
    switch ($Action) {
        Doctor { Invoke-TrainerDoctor }
        Build { $build=New-TrainerBuild -Profile $Profile; Write-Host ('Сборка: '+$build.folder) }
        Test { Invoke-TrainerTests -Suite $Suite -Ids $Probe }
        Run { Start-TrainerDevelopment -Profile $Profile }
        Checkpoint { Save-TrainerCheckpoint -Name $Name -Note $Note }
        Status { Get-TrainerStatus }
        Archive { Save-TrainerArchive -IncludeRuntime:$IncludeRuntime }
        Handoff { Save-TrainerContext -Reason 'handoff'; Write-Host (Resolve-TrainerPath 'dev/state/context.json') }
        Fix {
            if ([string]::IsNullOrWhiteSpace($Note) -or $Name -eq 'checkpoint') { throw 'Fix требует -Name и -Note: кратко опишите исправление.' }
            $build=New-TrainerBuild -Profile $Profile
            Invoke-TrainerTests -Suite $Suite -Ids $Probe
            Save-TrainerCheckpoint -Name $Name -Note $Note
        }
    }
    Save-TrainerContext -Reason $Action
    $commandReport.status='completed';$commandReport.finished_utc=[DateTime]::UtcNow.ToString('O')
    Write-TrainerJson $commandReport (Join-Path $commandFolder 'command.json')
} catch {
    $commandReport.status='failed';$commandReport.error=$_.ToString();$commandReport.finished_utc=[DateTime]::UtcNow.ToString('O')
    try { Write-TrainerJson $commandReport (Join-Path $commandFolder 'command.json') } catch { Write-Warning ('Не удалось сохранить ошибку команды: '+$_.Exception.Message) }
    try { Save-TrainerContext -Reason ($Action+' failed') } catch { Write-Warning ('Не удалось сохранить контекст: '+$_.Exception.Message) }
    Write-Error -ErrorRecord $_ -ErrorAction Continue
    exit 1
} finally {
    if($transcriptStarted){Stop-Transcript | Out-Null}
}
