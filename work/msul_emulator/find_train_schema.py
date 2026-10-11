import json
from pathlib import Path
for p in [Path('work/main_rtti.json'),Path('work/airbrake6000/TormModel.rtti.json')]:
 d=json.loads(p.read_text(encoding='utf8'))
 print(p, type(d).__name__)
 if isinstance(d,dict):print(list(d)[:10])
 else:
  for x in d:
   if any(a in str(x.get('name','')).lower() for a in ['sostav','variable','engine','tren','train']):print(x)
