from pathlib import Path
import sys,re,json,struct
sys.stdout.reconfigure(encoding='utf-8')
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
out=Path(__file__).parent
helper=Path('work/compare137_modules/rtti.py').read_text().split('want=')[0]
md=Cs(CS_ARCH_X86,CS_MODE_32);md.skipdata=True
for name in ['Pult1.dll','Communication1.dll']:
    f=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1')/name
    sys.argv=['rtti',str(f),'0'];e={};exec(helper,e)
    d=e['d'];pe=e['pe'];base=e['base'];records=[]
    for m in re.finditer(rb'[A-Za-z_][A-Za-z0-9_]{2,}',d):
        n=len(m.group());o=m.start()
        if o<6 or n>255 or d[o-1]!=n or d[o-2]!=14:continue
        try:
            va=base+pe.get_rva_from_offset(o-6)
            if struct.unpack_from('<I',d,o-6)[0]!=va+4:continue
            z=e['parse'](va)
            if z.get('size',0)<1000000 and len(z.get('fields',[]))<1000:records.append(z)
        except Exception:pass
    (out/(name+'.rtti.json')).write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
    for z in records:
        if any(x in z['name'] for x in ['TPultPositions','TPult','TPBBtn','TPositions','TButton_2ES6']):
            print(name,z['name'],z.get('size'),[(a['name'],a['offset'],a['type']) for a in z['fields']])
    procs=json.loads(Path('work/compare137_modules',name+'.procs.json').read_text(encoding='utf-8'))
    for p in procs:
        if any(x in p['name'] for x in ['UpdateButton$','UpdateButtonsHW','Init$','UpdatePultMash','Klub_K_BtnChange','Tumbler47','Tumbler3Change','SetNullBtnKLUB']) and any(c in p['name'] for c in ['TPult2ES6','TComm2ES6','TPultMashIndexForm']):
            tag=p['name'].replace('@','_').replace('$','_')
            ins=list(md.disasm(pe.get_data(p['rva'],p['length']),base+p['rva']))
            (out/(name+tag+'.asm')).write_text(json.dumps(p)+'\n'+'\n'.join('%08x %-8s %s'%(i.address,i.mnemonic,i.op_str) for i in ins),encoding='utf-8')
            print(name,p['name'],hex(p['rva']),p['length'])
