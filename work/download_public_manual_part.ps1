$taskMeta = Invoke-RestMethod -Uri 'https://cloud-api.yandex.net/v1/disk/public/resources?public_key=https%3A%2F%2Fyadi.sk%2Fd%2FBgsyV7dp3KwbS4'
$taskUrl = $taskMeta.file
$taskBase = (Get-Location).Path
$taskOffset = 50959827L
$taskSize = 65272666L
$taskBlock = 2097152L
$taskN = [int][Math]::Ceiling($taskSize / $taskBlock)
0..($taskN-1) | ForEach-Object -Parallel {
    $taskIndex = $_
    $taskStart = $using:taskOffset + $taskIndex * $using:taskBlock
    $taskEnd = [Math]::Min($using:taskOffset+$using:taskSize-1, $taskStart+$using:taskBlock-1)
    $taskBytes = Invoke-WebRequest -Uri $using:taskUrl -Headers @{Range="bytes=$taskStart-$taskEnd"} -TimeoutSec 30
    $taskFile = Join-Path $using:taskBase ("work/public_part1_chunk_{0:d2}.bin" -f $taskIndex)
    [System.IO.File]::WriteAllBytes($taskFile, $taskBytes.Content)
    "Downloaded part $taskIndex ($($taskBytes.RawContentLength) bytes)"
} -ThrottleLimit 4
