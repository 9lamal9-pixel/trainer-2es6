import json,pathlib,subprocess,urllib.parse,datetime,hashlib,concurrent.futures
base=pathlib.Path(__file__).resolve().parent
url=(base/'yandex_redirect_url.txt').read_text(encoding='utf-8')
host=urllib.parse.urlsplit(url).hostname
def task(name, target, port):
 out=base/f'yandex_{name}.bin';head=base/f'yandex_{name}_headers.txt'
 p=subprocess.run(['C:/Windows/System32/curl.exe','--ipv4','--http1.1','--resolve',f'{host}:{port}:77.88.0.160','--user-agent','Mozilla/5.0','--silent','--show-error','--connect-timeout','3','--max-time','16','--range','230611830-230677365','--dump-header',str(head),'--output',str(out),'--write-out','%{http_code}',target],capture_output=True,text=True,timeout=19)
 r={'variant':name,'host':host,'curl_exit':p.returncode,'http_code':p.stdout.strip(),'stderr':p.stderr[-700:]}
 if out.exists():
  b=out.read_bytes();r.update(bytes=len(b),prefix_hex=b[:4].hex(),sha256=hashlib.sha256(b).hexdigest())
 return r
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
 futures=[pool.submit(task,'cachequery',url+'&download=1',443),pool.submit(task,'http',url.replace('https://','http://',1),80)]
 results=[x.result() for x in futures]
r={'time_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'attempts':results}
(base/'yandex_variants_result.json').write_text(json.dumps(r,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(r,ensure_ascii=False,indent=2))
