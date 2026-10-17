import contextlib, io, json, re, runpy, sys, struct
from pathlib import Path

root=Path(__file__).resolve().parents[1]
sample=root/'work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe'
sys.argv=['rtti.py', str(sample), '0']
# Load helpers without the command-line reporting loop.
source=(root/'work/compare137_modules/rtti.py').read_text(encoding='utf8').split('want=')[0]
env={}
exec(compile(source, 'rtti_helpers', 'exec'), env)
d=env['d']; pe=env['pe']; base=env['base']
records=[]
for m in re.finditer(rb'[A-Za-z_][A-Za-z0-9_]{2,}', d):
    n=len(m.group()); o=m.start()
    if o<6 or n>255 or d[o-1]!=n or d[o-2]!=14: continue
    try:
        va=base+pe.get_rva_from_offset(o-6)
        if struct.unpack_from('<I', d, o-6)[0]!=va+4: continue
        r=env['parse'](va)
        if r.get('size', 0)>1_000_000 or len(r.get('fields', []))>500: continue
        records.append(r)
    except (ValueError, IndexError, struct.error, OverflowError): pass
target=root/'work/main_rtti.json'
target.write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf8')
print(target.name, len(records))
for r in records:
    if any(k in r['name'].lower() for k in ['vagon','pnevm','poezd','torm','locparam','engineparam']):
        print(r['name'], r.get('size'), r['va'])
