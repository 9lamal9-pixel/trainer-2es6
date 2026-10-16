@echo off
setlocal
rem Keep Windows PowerShell 5 isolated from inherited PowerShell 7 modules.
set "PSModulePath=%USERPROFILE%\Documents\WindowsPowerShell\Modules;%ProgramFiles%\WindowsPowerShell\Modules;%SystemRoot%\System32\WindowsPowerShell\v1.0\Modules"
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0dev\scripts\Restore-TrainerEnvironment.ps1" %*
exit /b %errorlevel%
