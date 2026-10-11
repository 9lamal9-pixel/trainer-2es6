$ErrorActionPreference = 'Stop'
$taskExe = Join-Path $PSScriptRoot 'TrainerIntegrationHost.pneumatic_candidate.exe'
$taskExisting = Get-Process TrainerIntegrationHost.pneumatic_candidate -ErrorAction SilentlyContinue | Where-Object { $_.Path -eq $taskExe }
if ($taskExisting) { Write-Output ('Стенд уже работает: PID ' + ($taskExisting.Id -join ', ')); exit }
$taskKey = 'HKCU:\Software\SPECTR\TRENAGER\LOCOPARAM\1'
foreach ($taskName in @('GETBMPMSUD_')) {
    New-ItemProperty -LiteralPath $taskKey -Name $taskName -Value '1' -PropertyType String -Force | Out-Null
}
New-ItemProperty -LiteralPath $taskKey -Name 'BLOKPB' -Value '0' -PropertyType String -Force | Out-Null
New-ItemProperty -LiteralPath $taskKey -Name 'KLUBUTYPE' -Value 'БИЛ-УТ' -PropertyType String -Force | Out-Null
$taskProc = Start-Process -FilePath $taskExe -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -PassThru
$taskProc.Id | Set-Content -LiteralPath (Join-Path $PSScriptRoot '..\work\msul_emulator\pneumatic_candidate_pid.txt')
Write-Output ('Стенд запущен: PID ' + $taskProc.Id)

