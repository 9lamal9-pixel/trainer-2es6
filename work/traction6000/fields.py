import sys,contextlib,io,runpy,json,re,struct
from pathlib import Path
sample=Path(r'work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Tyga1.dll')
sys.argv=['rtti.py',str(sample),'5713d8','5712f8','571128','571240','571060','570c88','570c9c','572de0']
with contextlib.redirect_stdout(io.StringIO()):
    env=runpy.run_path(r'work\compare137_modules\rtti.py')
records=[env['parse'](int(a,16)) for a in sys.argv[2:]]
d=sample.read_bytes();fields=[]
for m in re.finditer(rb'[a-zA-Z_][a-zA-Z0-9_]{2,}',d):
    if not (0x172350<=m.start()<0x172603):continue
    if d[m.start()-1]!=len(m.group()):continue
    fields.append({'name':m.group().decode(),'offset':hex(struct.unpack_from('<I',d,m.start()-5)[0]),'typeva':hex(struct.unpack_from('<I',d,m.start()-9)[0]),'file_offset':hex(m.start())})
out={'records':records,'TTygaCalcClass':fields}
path=Path(__file__).parent/'fields.json';path.write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf8');print(path)
