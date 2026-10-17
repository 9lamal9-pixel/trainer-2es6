from pathlib import Path
import json,pefile
for p in Path('work').rglob('*.procs.json'):print(p)
for p in [Path('work/main_rtti.json'),Path('work/airbrake6000/TormModel.rtti.json')]:
 print('\nSCHEMAS',p)
 for z in json.loads(p.read_text()):
  if z.get('kind')==14 and any(x in z.get('name','').lower() for x in ['train','sost','pbbtn','pnevmo','positionloc','wagon','natr','init']):print(z['name'],z['size'],[(x['name'],x['offset'],x['type']) for x in z['fields']])
p=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES')
for f in p.glob('*.dll'):
 pe=pefile.PE(str(f),fast_load=False)
 names=[x.name.decode() for x in pe.DIRECTORY_ENTRY_EXPORT.symbols if x.name] if hasattr(pe,'DIRECTORY_ENTRY_EXPORT') else []
 print(f.name, names)
