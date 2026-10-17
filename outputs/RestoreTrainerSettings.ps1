$ErrorActionPreference = 'Stop'
$taskItems = @(Get-Content -LiteralPath (Join-Path $PSScriptRoot 'trainer_registry_backup.json') -Raw | ConvertFrom-Json)
foreach ($taskItem in $taskItems) {
    if ($taskItem.Existed) {
        if (-not (Test-Path -LiteralPath $taskItem.Path)) { New-Item -Path $taskItem.Path -Force | Out-Null }
        New-ItemProperty -LiteralPath $taskItem.Path -Name $taskItem.Name -Value $taskItem.Value -PropertyType $taskItem.Kind -Force | Out-Null
    } elseif (Test-Path -LiteralPath $taskItem.Path) {
        Remove-ItemProperty -LiteralPath $taskItem.Path -Name $taskItem.Name -ErrorAction SilentlyContinue
    }
}
foreach ($taskGroup in ($taskItems | Group-Object Path)) {
    if (($taskGroup.Group | Where-Object { -not $_.SectionExisted }) -and (Test-Path -LiteralPath $taskGroup.Name)) {
        $taskKey = Get-Item -LiteralPath $taskGroup.Name
        if ($taskKey.ValueCount -eq 0 -and $taskKey.SubKeyCount -eq 0) { Remove-Item -LiteralPath $taskGroup.Name }
    }
}
Write-Output 'Настройки, сохранённые до запуска стенда, восстановлены. Закройте стенд перед следующим запуском штатного тренажёра.'
