import sys,json
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
sys.stdout.reconfigure(encoding='utf8')
ns={};exec(Path('work/brake6000/dfm.py').read_text().split('d=D(')[0],ns)
for dll in ['Communication1.dll','MSUL_MON.dll','BLOK.dll']:
 p=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES')/('LOCOMOTIVES/1/'+dll if dll=='Communication1.dll' else dll)
 pe=pefile.PE(str(p));md=Cs(CS_ARCH_X86,CS_MODE_32)
 if dll=='Communication1.dll':
  for r,n in [(0x2ae430,46),(0x2ae518,804)]:
   print('\nFUNCTION',hex(r))
   for i in md.disasm(pe.get_data(r,n),0x400000+r):print(hex(i.address),i.mnemonic,i.op_str)
 for t in pe.DIRECTORY_ENTRY_RESOURCE.entries:
  if t.id!=10:continue
  for f in t.directory.entries:
   name=str(f.name)
   if dll=='Communication1.dll' and any(x in name for x in ['PULT','MASH']):
    a=f.directory.entries[0].data.struct;b=pe.get_data(a.OffsetToData,a.Size)
    try:
     tree=ns['D'](b).obj();out=Path('work/msul_emulator/display_reference')/(name+'.json');out.write_text(json.dumps(tree,ensure_ascii=False,indent=2),encoding='utf8')
     def walk(z):
      props={k:v for k,v in z['props'].items() if k in ['Left','Top','Width','Height','ClientWidth','ClientHeight','Caption','Align']}
      print(name,z['class'],z['name'],props)
      for ch in z['children']:walk(ch)
     walk(tree)
    except Exception as e:print(name,e)
 if dll!='Communication1.dll':
  for e in pe.DIRECTORY_ENTRY_EXPORT.symbols:
   name=e.name.decode() if e.name else ''
   if any(x in name.lower() for x in ['handle','bitmap']):
    print(dll,name,hex(e.address))
    for i in md.disasm(pe.get_data(e.address,80),0x400000+e.address):
     print(hex(i.address),i.mnemonic,i.op_str)
     if i.mnemonic=='ret':break
