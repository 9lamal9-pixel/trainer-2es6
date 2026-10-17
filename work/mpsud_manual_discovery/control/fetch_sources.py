from pathlib import Path
import concurrent.futures
import hashlib
import json
import re
import time
import urllib.request
from datetime import datetime, timezone

folder = Path(__file__).resolve().parent
sources = {
    'scbist_print': 'https://scbist.com/tyagovyi-podvizhnoi-sostav/3271-elektrovoz-2es6-print.html',
    'ucoz_manual': 'https://instructionsrzd.ucoz.ru/load/vse_po_ehlektrovozam/ehlektrovoz_gruzovoj_2ehs6_quot_sinara_quot/7-1-0-192',
}

def fetch(item):
    key, url = item
    row = {'id': key, 'url': url, 'started_at': datetime.now(timezone.utc).isoformat()}
    started = time.monotonic()
    try:
        request = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(request, timeout=15) as response:
            data = response.read(2 * 1024 * 1024)
            row.update(status=response.status, final_url=response.url, bytes=len(data), content_type=response.headers.get('Content-Type'))
        path = folder / f'{key}.html'
        path.write_bytes(data)
        row.update(path=str(path), sha256=hashlib.sha256(data).hexdigest().upper())
        for encoding in ['utf-8', 'cp1251']:
            try:
                html = data.decode(encoding)
                break
            except UnicodeDecodeError:
                pass
        else:
            html = data.decode('utf-8', errors='replace')
        anchors = re.findall(r'<a\b[^>]*href=[\"\']([^\"\']+)[\"\'][^>]*>(.*?)</a>', html, re.I | re.S)
        row['candidate_links'] = [{'href': href, 'label': re.sub('<[^>]*>', '', label).strip()} for href, label in anchors if re.search(r'РЭ|измерени|3.?част|скач|download|2es6|2es-6|part.?3|\.pdf|\.rar|\.zip', href+' '+label, re.I)]
    except Exception as error:
        row.update(status='ERROR', error=type(error).__name__+': '+str(error))
    row['seconds'] = round(time.monotonic() - started, 2)
    return row

with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
    results = list(pool.map(fetch, sources.items()))
(folder / 'fetch_sources_results.json').write_text(json.dumps(results, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
for row in results:
    print(json.dumps(row, ensure_ascii=False))
