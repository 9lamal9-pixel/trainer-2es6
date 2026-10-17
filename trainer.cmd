@echo off
setlocal
rem Windows PowerShell must not inherit PowerShell 7 modules from the bot shell.
set "PSModulePath=%USERPROFILE%\Documents\WindowsPowerShell\Modules;%ProgramFiles%\WindowsPowerShell\Modules;%SystemRoot%\System32\WindowsPowerShell\v1.0\Modules"
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0dev\TrainerDev.ps1" %*
exit /b %errorlevel%
