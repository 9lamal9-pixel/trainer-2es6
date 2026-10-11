import sys,struct,json
from pathlib import Path
sys.stdout.reconfigure(encoding='utf8')
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
f=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/KLUB_U_LCD_XE2.dll')
p=pefile.PE(str(f)); d=f.read_bytes(); base=p.OPTIONAL_HEADER.ImageBase
out=Path('work/dos_route/klub');out.mkdir(parents=True,exist_ok=True)
print('base',hex(base), 'size',len(d))
md=Cs(CS_ARCH_X86,CS_MODE_32)
md.skipdata=True
for arg in sys.argv[1:]:
 r,n=arg.split(':');r=int(r,0);n=int(n,0)
 ins=list(md.disasm(p.get_data(r,n),base+r))
 (out/(hex(r)+'.asm')).write_text('\n'.join(f'{i.address:08x} {i.mnemonic:8} {i.op_str}' for i in ins),encoding='utf8')
ex={s.name.decode():s.address for s in p.DIRECTORY_ENTRY_EXPORT.symbols if s.name}
(out/'exports.json').write_text(json.dumps(ex,indent=2),encoding='utf8')
for name,rva in ex.items():
 print(name,hex(rva))
 if name in ['InitKlub_U_LCD','ProceedKlub','GetParamInToKLUB','CloseKlub','GetHBitmapKlub','GetHandleKLUB']:
  ins=list(md.disasm(p.get_data(rva,400),base+rva))
  (out/(name+'.asm')).write_text('\n'.join(f'{i.address:08x} {i.mnemonic:8} {i.op_str}' for i in ins),encoding='utf8')
helper=Path('work/compare137_modules/rtti.py').read_text().split('want=')[0]
sys.argv=['rtti',str(f),'0'];e={};exec(helper,e);arr=[]
for sec in p.sections:
 raw=sec.get_data();rva=sec.VirtualAddress
 for o in range(0,len(raw)-8,4):
  if struct.unpack_from('<I',raw,o)[0]!=base+rva+o+4 or raw[o+4]!=14:continue
  try:
   z=e['parse'](base+rva+o)
   if z.get('size',0)<1000000 and len(z.get('fields',[]))<1000:arr.append(z)
  except Exception:pass
(out/'rtti.json').write_text(json.dumps(arr,ensure_ascii=False,indent=2),encoding='utf8')
sys.path.insert(0,str(Path('work/brake6000').resolve()))
helperdfm=Path('work/brake6000/dfm.py').read_text().split('d=D(')[0];e2={};exec(helperdfm,e2)
for typ in p.DIRECTORY_ENTRY_RESOURCE.entries:
 for item in typ.directory.entries:
  for lng in item.directory.entries:
   dat=p.get_data(lng.data.struct.OffsetToData,lng.data.struct.Size)
   if not dat.startswith(b'TPF0'):continue
   try:
    form=e2['D'](dat).obj(); props=form['props']
    print('FORM',form['class'],{k:v for k,v in props.items() if k in ['Left','Top','ClientHeight','ClientWidth','Height','Width','OnShow']})
    (out/(form['class']+'.json')).write_text(json.dumps(form,ensure_ascii=False,indent=2),encoding='utf8')
   except Exception as err:print('DFM ERR',item.name, str(err))
for z in arr:
 if any(x in z['name'].lower() for x in ['klub','pribbez','bitmap','hndl','init','outparam']):print(z['name'],z['size'],[(a['name'],a['offset'],a['type']) for a in z['fields']])
