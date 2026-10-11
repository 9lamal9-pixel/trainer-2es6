import json,sys
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
md=Cs(CS_ARCH_X86,CS_MODE_32)
for dll,rs in [('MSUL_MON.dll',[(0x1eeb10,110),(0x1f1a48,57)]),('BLOK.dll',[]),('Communication1.dll',[(0x2aefdc,68)])]:
 f=next(Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES').rglob(dll));p=pefile.PE(str(f))
 if dll=='BLOK.dll':
  for e in p.DIRECTORY_ENTRY_EXPORT.symbols:
   if e.name and e.name.decode() in ['InitBLOK','ProceedBLOK']:rs.append((e.address,1000 if e.name.decode()=='InitBLOK' else 240))
 for r,n in rs:
  print(dll,hex(r))
  for i in md.disasm(p.get_data(r,n),r+0x400000):print(hex(i.address),i.mnemonic,i.op_str)
