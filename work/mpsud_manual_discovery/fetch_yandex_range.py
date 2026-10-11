import json, pathlib, urllib.request, urllib.parse, subprocess, hashlib, datetime
base = pathlib.Path(__file__).resolve().parent
api = 'https://cloud-api.yandex.net/v1/disk/public/resources?public_key=' + urllib.parse.quote('https://yadi.sk/d/BgsyV7dp3KwbS4', safe='')
result = {'time_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'source_public_key': 'https://yadi.sk/d/BgsyV7dp3KwbS4'}
try:
    with urllib.request.urlopen(api, timeout=20) as resp:
        meta = json.load(resp)
    (base/'yandex_meta_current.json').write_text(json.dumps(meta, ensure_ascii=False), encoding='utf-8')
    url = meta['file']
    result.update(size=meta['size'], download_host=urllib.parse.urlsplit(url).hostname)
    out = base/'part3_header_curl.bin'
    cmd = ['C:/Windows/System32/curl.exe', '--ipv4', '--location', '--silent', '--show-error', '--connect-timeout', '7', '--max-time', '12', '--range', '230611830-230677365', '--output', str(out), '--write-out', '%{http_code}', url]
    proc = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
    result.update(curl_exit=proc.returncode, http_code=proc.stdout.strip(), stderr=proc.stderr[-1500:])
    if out.exists():
        data = out.read_bytes()
        result.update(received_bytes=len(data), sha256=hashlib.sha256(data).hexdigest(), zip_header=data[:4].hex())
except Exception as exc:
    result['error'] = type(exc).__name__ + ': ' + str(exc)
(base/'yandex_curl_result.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False, indent=2))
