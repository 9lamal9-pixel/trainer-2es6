$ErrorActionPreference = 'Stop'
$taskExe = Join-Path $PSScriptRoot 'TrainerIntegrationHost.exe'
$taskExisting = Get-Process TrainerIntegrationHost -ErrorAction SilentlyContinue | Where-Object { $_.Path -eq $taskExe }
if ($taskExisting) { Write-Output ('Стенд уже работает: PID ' + ($taskExisting.Id -join ', ')); exit }
$taskKey = 'HKCU:\Software\SPECTR\TRENAGER\LOCOPARAM\1'
foreach ($taskName in @('GETBMPMSUD_', 'GETBMPBLOK')) {
    New-ItemProperty -LiteralPath $taskKey -Name $taskName -Value '1' -PropertyType String -Force | Out-Null
}
$taskProc = Start-Process -FilePath $taskExe -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru
$taskProc.Id | Set-Content -LiteralPath (Join-Path $PSScriptRoot '..\work\msul_emulator\integration_pid.txt')
Write-Output ('Стенд запущен: PID ' + $taskProc.Id)
