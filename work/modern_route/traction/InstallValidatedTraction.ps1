[CmdletBinding()]
param([ValidateSet('Apply','Restore')][string]$Mode='Apply')
$ErrorActionPreference='Stop'
$repairDirectory=[IO.Path]::GetFullPath($PSScriptRoot)
$workspaceDirectory=[IO.Path]::GetFullPath((Join-Path $repairDirectory '..\..\..'))
$targetPath=[IO.Path]::GetFullPath((Join-Path $workspaceDirectory 'work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Tyga1.dll'))
$allowedDirectory=[IO.Path]::GetFullPath((Join-Path $workspaceDirectory 'work\trenager_static\WORK_TRENAGER_3D'))+'\'
if(-not $targetPath.StartsWith($allowedDirectory,[StringComparison]::OrdinalIgnoreCase)){throw 'Target is outside the copied simulator.'}
$patchManifest=Get-Content -LiteralPath (Join-Path $repairDirectory 'patch_manifest.json') -Raw | ConvertFrom-Json
$validationPath=Join-Path $repairDirectory 'traction_validation.json'
if(-not (Test-Path -LiteralPath $validationPath)){throw 'Native regression report is absent.'}
$originalPath=Join-Path $repairDirectory 'Tyga1.original.dll'
$patchedPath=Join-Path $repairDirectory 'Tyga1.patched.dll'
$backupPath=Join-Path $repairDirectory 'Tyga1.backup.dll'
if((Get-FileHash -LiteralPath $originalPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $patchManifest.original_sha256){throw 'Original backup hash differs.'}
if((Get-FileHash -LiteralPath $patchedPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $patchManifest.patched_sha256){throw 'Patched copy hash differs.'}
$currentHash=(Get-FileHash -LiteralPath $targetPath -Algorithm SHA256).Hash.ToLowerInvariant()
$expectedHash=if($Mode -eq 'Apply'){$patchManifest.original_sha256}else{$patchManifest.patched_sha256}
$desiredHash=if($Mode -eq 'Apply'){$patchManifest.patched_sha256}else{$patchManifest.original_sha256}
if($currentHash -eq $desiredHash){Write-Output ($Mode+' already satisfied: '+$targetPath);return}
if($currentHash -ne $expectedHash){throw ('Unknown target build: '+$currentHash)}
$targetBytes=[IO.File]::ReadAllBytes($targetPath)
foreach($patch in $patchManifest.patches){
 $hex=if($Mode -eq 'Apply'){$patch.original}else{$patch.patched}
 for($index=0;$index -lt $hex.Length/2;$index++){
  $expectedByte=[Convert]::ToByte($hex.Substring($index*2,2),16)
  if($targetBytes[[int]$patch.offset+$index] -ne $expectedByte){throw ('Instruction guard failed at RVA '+$patch.rva)}
 }
}
# A running native host keeps the target DLL locked. The caller must close its
# own integration host first; this script never stops any process.
if($Mode -eq 'Apply'){
 if(Test-Path -LiteralPath $backupPath){if((Get-FileHash -LiteralPath $backupPath).Hash.ToLowerInvariant() -ne $patchManifest.original_sha256){throw 'Existing backup differs.'}}
 else{Copy-Item -LiteralPath $targetPath -Destination $backupPath}
 Copy-Item -LiteralPath $patchedPath -Destination $targetPath -Force
}else{Copy-Item -LiteralPath $originalPath -Destination $targetPath -Force}
$installedHash=(Get-FileHash -LiteralPath $targetPath -Algorithm SHA256).Hash.ToLowerInvariant()
if($installedHash -ne $desiredHash){throw 'Installed hash failed verification.'}
[PSCustomObject]@{mode=$Mode;time=(Get-Date).ToString('O');target=$targetPath;sha256=$installedHash} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $repairDirectory 'installation_receipt.json') -Encoding UTF8
Write-Output ($Mode+' verified: '+$installedHash)
