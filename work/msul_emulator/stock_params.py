import json,pefile,sys
from pathlib import Path
sys.stdout.reconfigure(encoding='utf8')
p=Path('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe');pe=pefile.PE(str(p),fast_load=True);print(p.stat().st_size,[(s.Name,s.SizeOfRawData) for s in pe.sections])
d=json.loads(Path('work/brake6000/libraries.json').read_text(encoding='utf-8'))
for k,i in [('locomotives',12),('wagons',2)]:print(k,i,json.dumps(d[k][i],ensure_ascii=False))
r=json.loads(Path('work/airbrake6000/TormModel.rtti.json').read_text())
for z in r:
 if z['name'] in ['TPnevmo','TLocParam','TVagonParam','TGRParam','TVagonSend','TStateVagon']:
  print(z['name'],z.get('size'),[(x['name'],x['offset'],x['type']) for x in z.get('fields',[])])
