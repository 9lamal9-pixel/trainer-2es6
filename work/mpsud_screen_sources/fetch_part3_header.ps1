$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSCommandPath
$taskMeta = Get-Content -LiteralPath (Join-Path $taskRoot 'yandex_meta.json') -Raw | ConvertFrom-Json
$taskUri = [Uri]$taskMeta.file
if ($taskUri.Scheme -ne 'https' -or $taskUri.Host -ne 'downloader.disk.yandex.ru') { throw 'Unexpected public download host' }
$taskRequest = [System.Net.HttpWebRequest]::Create($taskUri)
$taskRequest.Timeout = 15000
$taskRequest.ReadWriteTimeout = 15000
$taskRequest.AddRange(230611830L, 230677365L)
$taskResponse = $null
try {
    $taskResponse = $taskRequest.GetResponse()
    if ([int]$taskResponse.StatusCode -ne 206) { throw 'Range response was not 206' }
    if ($taskResponse.Headers['Content-Range'] -notmatch '^bytes 230611830-230677365/') { throw 'Unexpected Content-Range' }
    $taskStream = $taskResponse.GetResponseStream()
    $taskBuffer = New-Object byte[] 65536
    $taskRead = 0
    while ($taskRead -lt $taskBuffer.Length) {
        $taskCount = $taskStream.Read($taskBuffer, $taskRead, $taskBuffer.Length - $taskRead)
        if ($taskCount -le 0) { break }
        $taskRead += $taskCount
    }
    if ($taskRead -ne 65536) { throw 'Incomplete range' }
    [IO.File]::WriteAllBytes((Join-Path $taskRoot 'part3_header_range.bin'), $taskBuffer)
    Write-Output 'PASS: 206, matching Content-Range, 65536 bytes saved'
} catch {
    Write-Output ('FAIL: ' + $_.Exception.GetType().Name)
    exit 1
} finally {
    if ($null -ne $taskResponse) { $taskResponse.Close() }
}
