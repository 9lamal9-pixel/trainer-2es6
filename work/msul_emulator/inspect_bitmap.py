import json,sys
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
md=Cs(CS_ARCH_X86,CS_MODE_32)
for dll, regions in [('Pult1.dll',[(0x18b1ec,182),(0x18b30c,240),(0x18b570,60)]),('Communication1.dll',[(0x2af020,140),(0xa05f4,70)])]:
 p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/'+dll)
 for r,n in regions:
  print(dll,hex(r))
  for i in md.disasm(p.get_data(r,n),r+0x400000):print(hex(i.address),i.mnemonic,i.op_str)
for dll in ['MSUL_MON.dll','BLOK.dll','KLUB_U_LCD_XE2.dll']:
 f=next(Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES').rglob(dll));p=pefile.PE(str(f))
 for e in p.DIRECTORY_ENTRY_EXPORT.symbols:
  name=e.name.decode() if e.name else ''
  if any(x in name.lower() for x in ['handle','bitmap']):
   print(dll,name,hex(e.address))
   for i in md.disasm(p.get_data(e.address,150),0x400000+e.address):
    print(hex(i.address),i.mnemonic,i.op_str)
    if i.mnemonic=='ret':break
