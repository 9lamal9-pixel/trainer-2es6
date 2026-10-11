$ErrorActionPreference = 'Stop'
$probe = Join-Path $PSScriptRoot 'BrakeProbe.exe'
$result = @()
foreach ($padIndex in @(0,1)) {
  foreach ($gradeValue in @(0,6,-6)) {
    $gradeName = if ($gradeValue -eq 0) {'flat'} elseif ($gradeValue -gt 0) {'up'} else {'down'}
    foreach ($pressureValue in @(0,1,3)) {
      $testName = '{0}_pad{1}_p{2}_v60' -f $gradeName,$padIndex,$pressureValue
      $watch = [System.Diagnostics.Stopwatch]::StartNew()
      $output = & $probe $testName $padIndex $gradeValue $pressureValue 60
      $result += [pscustomobject]@{Name=$testName;ExitCode=$LASTEXITCODE;Seconds=$watch.Elapsed.TotalSeconds}
      Write-Output ('{0} exit={1}' -f $testName,$LASTEXITCODE)
      if ($LASTEXITCODE -ne 0) {Write-Output $output}
    }
    foreach ($pressureValue in @(1,3)) {
      $testName = '{0}_pad{1}_p{2}_v0' -f $gradeName,$padIndex,$pressureValue
      $watch = [System.Diagnostics.Stopwatch]::StartNew()
      $output = & $probe $testName $padIndex $gradeValue $pressureValue 0
      $result += [pscustomobject]@{Name=$testName;ExitCode=$LASTEXITCODE;Seconds=$watch.Elapsed.TotalSeconds}
      Write-Output ('{0} exit={1}' -f $testName,$LASTEXITCODE)
      if ($LASTEXITCODE -ne 0) {Write-Output $output}
    }
  }
}
foreach ($pressureValue in @(0,1,3)) {
  $testName = 'old_wrong_pad8_4_p{0}_v60' -f $pressureValue
  $watch = [System.Diagnostics.Stopwatch]::StartNew()
  $output = & $probe $testName 8 0 $pressureValue 60
  $result += [pscustomobject]@{Name=$testName;ExitCode=$LASTEXITCODE;Seconds=$watch.Elapsed.TotalSeconds}
  Write-Output ('{0} exit={1}' -f $testName,$LASTEXITCODE)
  if ($LASTEXITCODE -ne 0) {Write-Output $output}
}
$result | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'matrix_exit_codes.json') -Encoding UTF8
