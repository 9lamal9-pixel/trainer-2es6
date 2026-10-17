import json, pathlib, urllib.parse, socket, subprocess, datetime
base=pathlib.Path(__file__).resolve().parent
meta=json.loads((base/'yandex_meta_current.json').read_text(encoding='utf-8'))
url=meta['file']
result={'time_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'attempts':[]}
def fetch(url, index):
    host=urllib.parse.urlsplit(url).hostname
    ips=sorted(set(x[4][0] for x in socket.getaddrinfo(host,443,socket.AF_INET,socket.SOCK_STREAM)))
    for ip in ips[:2]:
        header=base/f'pinned_{index}_{ip}_headers.txt'
        out=base/f'pinned_{index}_{ip}_range.bin'
        p=subprocess.run(['C:/Windows/System32/curl.exe','--ipv4','--http1.1','--resolve',f'{host}:443:{ip}','--silent','--show-error','--connect-timeout','3','--max-time','7','--range','230611830-230677365','--dump-header',str(header),'--output',str(out),'--write-out','%{http_code}',url],capture_output=True,text=True,timeout=10)
        item={'host':host,'ip':ip,'curl_exit':p.returncode,'http_code':p.stdout.strip(),'stderr':p.stderr[-600:]}
        if out.exists(): item['bytes']=out.stat().st_size
        loc=next((x.split(':',1)[1].strip() for x in header.read_text(encoding='utf-8',errors='replace').splitlines() if x.lower().startswith('location:')),None) if header.exists() else None
        if loc: item['redirect_host']=urllib.parse.urlsplit(loc).hostname
        result['attempts'].append(item)
        if loc or item.get('bytes',0)==65536: return loc
    return None
try:
    loc=fetch(url,0)
    if loc:
        (base/'yandex_redirect_url.txt').write_text(loc,encoding='utf-8')
        fetch(loc,1)
except Exception as exc: result['error']=type(exc).__name__+': '+str(exc)
(base/'yandex_pinned_result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False,indent=2))
