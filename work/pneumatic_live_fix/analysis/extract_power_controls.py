import sys,json
from pathlib import Path
sys.path.insert(0,'work/traction6000/vendor')
import pefile
ns={}
source=Path('work/brake6000/dfm.py').read_text(encoding='utf-8')
exec(source.split('d=D(Path(sys.argv[1])')[0],ns)
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll')
rows=[]
def visit(node,form):
    props=node['props']
    text=' '.join(v for v in props.values() if isinstance(v,str) and len(v)<300)
    if props.get('Tag') in (0x1f,0x20,0x21,0x22,0x25,0x28) or any(k in (node['name']+' '+text).lower() for k in ['компресс','токопр','преобраз','быстрод','псн','бв','psn','tokop','kompr']):
        rows.append(dict(form=form,name=node['name'],class_name=node['class'],
                         properties={k:v for k,v in props.items() if (isinstance(v,str) and len(v)<300) or k in ('State','Value','Tag')}))
    for child in node['children']:visit(child,form)
for t in p.DIRECTORY_ENTRY_RESOURCE.entries:
    for n in t.directory.entries:
        if n.name and str(n.name) in ('TPULTMASHINDEXFORM','TPULT_POM_2ES6FORM','TTUMBAFORM'):
            a=n.directory.entries[0].data.struct
            decoder=ns['D'](p.get_data(a.OffsetToData,a.Size));obj=decoder.obj()
            if decoder.p!=len(decoder.d):raise ValueError('Incomplete DFM parsing')
            visit(obj,str(n.name))
Path('outputs/Trainer_native_power_controls.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
for row in rows:print(row['form'],row['name'],row['properties'])
