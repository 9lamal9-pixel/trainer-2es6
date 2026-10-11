import json,sys
from pathlib import Path
import pefile
p=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll'); pe=pefile.PE(str(p)); ps=json.loads(Path('work/compare137_modules/Communication1.dll.procs.json').read_text(encoding='utf-8')); targets={x['rva']:x['name'] for x in ps if 'TArrowInPribor@SetValueAr' in x['name'] or 'TArrowInPribor@AddVAlue' in x['name']}
import struct
for f in ps:
 b=pe.get_data(f['rva'],f['length'])
 for i in range(len(b)-4):
  if b[i]==232:
   t=f['rva']+i+5+struct.unpack_from('<i',b,i+1)[0]
   if t in targets: print(f['name'],hex(f['rva']),f['length'],hex(f['rva']+i),targets[t])
