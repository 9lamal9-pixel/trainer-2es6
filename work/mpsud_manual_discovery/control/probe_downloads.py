from pathlib import Path
from datetime import datetime, timezone
import concurrent.futures
import hashlib
import json
import urllib.request
import time

folder = Path(__file__).resolve().parent
urls = {
    'scbist_alternative': 'https://scbist.com/scb/uploaded/1_2es6-re.zip',
    'scbist_mpsud': 'https://scbist.com/scb/uploaded/1_1767437709.pdf',
}
def fetch(item):
    key,url=item
    row={'id':key,'url':url,'date':datetime.now(timezone.utc).isoformat()}
    t=time.monotonic()
    try:
        request=urllib.request.Request(url,headers={'User-Agent':'Mozilla/5.0','Range':'bytes=0-65535'})
        with urllib.request.urlopen(request,timeout=15) as response:
            b=response.read(65536)
            row.update(status=response.status,final_url=response.url,content_type=response.headers.get('Content-Type'),length=response.headers.get('Content-Length'),range=response.headers.get('Content-Range'),bytes=len(b),prefix=b[:12].hex())
        path=folder/(key+'.prefix.bin')
        path.write_bytes(b)
        row.update(path=str(path),sha256=hashlib.sha256(b).hexdigest().upper())
    except Exception as error:
        row.update(status='ERROR',error=type(error).__name__+': '+str(error))
    row['seconds']=round(time.monotonic()-t,2)
    return row
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
    results=list(pool.map(fetch,urls.items()))
(folder/'probe_downloads_results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for row in results:
    print(json.dumps(row,ensure_ascii=False))
