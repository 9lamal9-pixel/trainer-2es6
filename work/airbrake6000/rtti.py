import json,re,sys,struct
from pathlib import Path
f=Path(r'work/trenager_static/WORK_TRENAGER_3D/RESOURCES/TormModel.dll')
sys.argv=['rtti.py',str(f),'0']
source=Path('work/compare137_modules/rtti.py').read_text(encoding='utf8').split('want=')[0]
env={};exec(compile(source,'rtti_helpers','exec'),env)
d=env['d'];pe=env['pe'];base=env['base']; records=[]
for m in re.finditer(rb'[A-Za-z_][A-Za-z0-9_]{2,}',d):
 n=len(m.group());o=m.start()
 if o<6 or n>255 or d[o-1]!=n or d[o-2]!=14:continue
 try:
  va=base+pe.get_rva_from_offset(o-6)
  if struct.unpack_from('<I',d,o-6)[0]!=va+4:continue
  r=env['parse'](va)
  if r.get('size',0)>1000000 or len(r.get('fields',[]))>500:continue
  records.append(r)
 except(ValueError,IndexError,struct.error,OverflowError):pass
Path('work/airbrake6000/TormModel.rtti.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf8')
for r in records:
 if any(k in r['name'].lower() for k in ['vagon','pnevm','poezd','torm','locparam','vr483']):print(r['name'],r.get('size'),r['va'])
