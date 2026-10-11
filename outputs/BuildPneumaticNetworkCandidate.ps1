$ErrorActionPreference='Stop'
& (Join-Path $PSScriptRoot '..\dev\TrainerDev.ps1') -Action Build -Profile candidate -Deploy