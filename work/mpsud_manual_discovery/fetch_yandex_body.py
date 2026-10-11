import json,pathlib,subprocess,urllib.parse,datetime,hashlib
base=pathlib.Path(__file__).resolve().parent
url=(base/'yandex_redirect_url.txt').read_text(encoding='utf-8')
host=urllib.parse.urlsplit(url).hostname
out=base/'part3_header_verified.bin'
p=subprocess.run(['C:/Windows/System32/curl.exe','--ipv4','--http1.1','--resolve',f'{host}:443:77.88.0.160','--silent','--show-error','--connect-timeout','3','--max-time','30','--range','230611830-230677365','--output',str(out),'--write-out','%{http_code}',url],capture_output=True,text=True,timeout=33)
result={'time_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'host':host,'range':'230611830-230677365','curl_exit':p.returncode,'http_code':p.stdout.strip(),'stderr':p.stderr[-900:]}
if out.exists():
 data=out.read_bytes();result.update(bytes=len(data),prefix_hex=data[:4].hex(),sha256=hashlib.sha256(data).hexdigest())
(base/'yandex_body_result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False,indent=2))
