import json
from pathlib import Path
for z in json.loads(Path('work/msul_emulator/native_record_schema.json').read_text()):
 if z['name'] in ['TPnevmo','TGRParam','TUpdateParamVagon','TApparatFlags']:print(z['name'],z['size'],[(x['name'],x['offset'],x['type']) for x in z['fields']])
for z in json.loads(Path('work/main_rtti.json').read_text()):
 if any(x in z['name'].lower() for x in ['saut','ogrcalc','saveparam','kpd','loghead']):print(z['name'],z.get('size'),[(x['name'],x['offset'],x['type']) for x in z.get('fields',[])])
