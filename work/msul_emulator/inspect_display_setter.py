import json,sys
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
md=Cs(CS_ARCH_X86,CS_MODE_32)
for dll,rs in [('Communication1.dll',[(0x2a9160,600)]),('MSUL_MON.dll',[(0x1f3810,300)]),('Pult1.dll',[(0x18b2f0,28)])]:
 f=next(Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES').rglob(dll));p=pefile.PE(str(f))
 syms=json.loads(Path('work/compare137_modules/'+dll+'.procs.json').read_text()) if dll!='MSUL_MON.dll' else json.loads(Path('work/msul_emulator/display_reference/MSUL_MON.dll.procs.json').read_text())
 for z in syms:
  if (dll=='MSUL_MON.dll' and any(x in z['name'].lower() for x in ['initmsul','proceedmsul','gethandle','getbmp','bitmap','formcreate'])) or (dll=='Communication1.dll' and 'alien' in z['name'].lower()):print('SYMBOL',z['name'],hex(z['rva']),z['length'])
 for r,n in rs:
  print(dll,hex(r))
  for i in md.disasm(p.get_data(r,n),0x400000+r):print(hex(i.address),i.mnemonic,i.op_str)
 for x in p.DIRECTORY_ENTRY_EXPORT.symbols:
  if x.name:print('EXPORT',dll,x.name.decode(),hex(x.address))
