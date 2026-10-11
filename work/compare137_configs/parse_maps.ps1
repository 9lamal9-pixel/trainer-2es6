$ErrorActionPreference='Stop'
$taskBase='C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1'
$taskEnc=[Text.Encoding]::GetEncoding(1251)
$taskResults=@()
foreach($taskRel in @('Init\Hardware1.cbn','Init\Hardware1(готовый).cbn','Init\Hardware111.cbn','2ES6\1\Init\Hardware1.cbn','1\Init\Hardware1.cbn','1\Init\Hardware1 - копия.cbn')) {
  $taskPath=Join-Path $taskBase $taskRel
  $taskText=[IO.File]::ReadAllText($taskPath,$taskEnc)
  $taskItems=@()
  foreach($taskBlock in [regex]::Matches($taskText,'(?ms)^\t\{(?<name>\w+) Type=T_STRUCT\(\) Value=(?<body>.*?)^\t\}')) {
    $taskBody=$taskBlock.Groups['body'].Value
    $taskValues=@{}
    foreach($taskKey in @('FullName','DataClass','ConvertorClass','Existing')) {
      $taskVal=[regex]::Match($taskBody,'\{'+$taskKey+' Type=.*?Value=([^}\r\n]+)').Groups[1].Value.Trim('"')
      $taskValues[$taskKey]=$taskVal
    }
    $taskAddress=@{}
    foreach($taskVal in [regex]::Matches($taskBody,'\{(Buffer|Port|Byte|Bit|MaxValue|Bit_\d+) Type=T_INT\(int32\) Value=([^}]+)')) {
      $taskAddress[$taskVal.Groups[1].Value]=$taskVal.Groups[2].Value
    }
    $taskPairs=@()
    foreach($taskPair in [regex]::Matches($taskBody,'(?s)\{mark Type=.*?Value=([^}]+)\}.*?\{data Type=.*?Value=([^}]+)\}')) {
      $taskPairs+=@{mark=$taskPair.Groups[1].Value;data=$taskPair.Groups[2].Value}
    }
    $taskItems+=@{Name=$taskBlock.Groups['name'].Value;FullName=$taskValues['FullName'];DataClass=$taskValues['DataClass'];ConvertorClass=$taskValues['ConvertorClass'];Existing=$taskValues['Existing'];Address=$taskAddress;Pairs=$taskPairs;Line=([regex]::Matches($taskText.Substring(0,$taskBlock.Index),'\n').Count+1)}
  }
  $taskResults+=@{Path=$taskRel;Hash=(Get-FileHash -Algorithm SHA256 -LiteralPath $taskPath).Hash;ConnectType=[regex]::Match($taskText,'\{ConnectType.*?Value="([^"]+)"').Groups[1].Value;Items=$taskItems}
}
$taskHwdResults=@()
foreach($taskRel in @('Init\Hardware1.hdwprop','2ES6\1\Init\Hardware1.hdwprop','1\Init\Hardware1.hdwprop')) {
  $taskLines=[IO.File]::ReadAllLines((Join-Path $taskBase $taskRel),$taskEnc)
  $taskI=3
  $taskItems=@()
  $taskGroups=@{}
  while($taskI -lt $taskLines.Length -and $taskLines[$taskI] -like 'Настройки *') {
    $taskGroup=$taskLines[$taskI]
    $taskCount=[int]$taskLines[$taskI+1]
    $taskGroups[$taskGroup]=$taskCount
    $taskI+=2
    for($taskJ=0;$taskJ -lt $taskCount;$taskJ++) {
      $taskItems+=@{Group=$taskGroup;FullName=$taskLines[$taskI];Name=$taskLines[$taskI+1];Field2=$taskLines[$taskI+2];Index=[int]$taskLines[$taskI+3];Field4=$taskLines[$taskI+4];Field5=$taskLines[$taskI+5];Line=$taskI+1}
      $taskI+=6
    }
  }
  $taskHwdResults+=@{Path=$taskRel;Series=$taskLines[2];Groups=$taskGroups;Items=$taskItems;ConsumedLines=$taskI;TotalLines=$taskLines.Length}
}
@{CBN=$taskResults;HDWPROP=$taskHwdResults}|ConvertTo-Json -Depth 12|Set-Content -Encoding UTF8 -LiteralPath (Join-Path $PSScriptRoot 'maps.json')
foreach($taskMap in $taskResults) {
  $taskMap.Path+' count='+$taskMap.Items.Count+' existing1='+@($taskMap.Items|Where-Object {$_.Existing -eq '1'}).Count+' existing0='+@($taskMap.Items|Where-Object {$_.Existing -eq '0'}).Count+' hash='+$taskMap.Hash
}
foreach($taskMap in $taskHwdResults) {
  $taskMap.Path+' series='+$taskMap.Series+' groups='+($taskMap.Groups|ConvertTo-Json -Compress)+' parsed='+$taskMap.ConsumedLines+'/'+$taskMap.TotalLines
}
