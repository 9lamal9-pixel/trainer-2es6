import json, pathlib, subprocess, urllib.parse, datetime, socket, urllib.request
base = pathlib.Path(__file__).resolve().parent
meta = json.loads((base/'yandex_meta_current.json').read_text(encoding='utf-8'))
result = {'time_utc': datetime.datetime.now(datetime.timezone.utc).isoformat()}
for name, url in [('meta_file', meta['file'])]:
    headers = base/(name+'_headers.txt')
    out = base/(name+'_body.bin')
    p = subprocess.run(['C:/Windows/System32/curl.exe', '--ipv4', '--silent', '--show-error', '--connect-timeout','5','--max-time','8','--range','230611830-230677365','--dump-header',str(headers),'--output',str(out),'--write-out','%{http_code}',url], capture_output=True, text=True, timeout=10)
    item = {'curl_exit':p.returncode,'http_code':p.stdout.strip(),'stderr':p.stderr[-1000:]}
    location = next((x.split(':',1)[1].strip() for x in headers.read_text(encoding='utf-8',errors='replace').splitlines() if x.lower().startswith('location:')), None) if headers.exists() else None
    if location:
        (base/'yandex_redirect_url.txt').write_text(location,encoding='utf-8')
        parsed = urllib.parse.urlsplit(location)
        item['redirect_host']=parsed.hostname
        item['redirect_scheme']=parsed.scheme
        try: item['redirect_ipv4']=sorted(set(x[4][0] for x in socket.getaddrinfo(parsed.hostname,443,socket.AF_INET,socket.SOCK_STREAM)))
        except Exception as exc: item['dns_error']=str(exc)
    result[name]=item
try:
    api = 'https://cloud-api.yandex.net/v1/disk/public/resources/download?public_key=' + urllib.parse.quote('https://yadi.sk/d/BgsyV7dp3KwbS4',safe='')
    with urllib.request.urlopen(api,timeout=10) as resp: download=json.load(resp)
    (base/'yandex_download_meta.json').write_text(json.dumps(download),encoding='utf-8')
    result['download_api_host']=urllib.parse.urlsplit(download.get('href','')).hostname
except Exception as exc: result['download_api_error']=str(exc)
(base/'yandex_redirect_result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False,indent=2))
