from pathlib import Path
import pefile,sys
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/BLOK.dll');md=Cs(CS_ARCH_X86,CS_MODE_32)
# disassemble each compiler prologue independently to survive interleaved resource data
raw=p.get_data(0x1a0000,0x10000)
for r in range(len(raw)-3):
 if raw[r:r+3]!=bytes.fromhex('558bec'):continue
 ins=list(md.disasm(raw[r:r+1800],0x5a0000+r));ins=ins[:next((j+1 for j,i in enumerate(ins) if i.mnemonic=='ret'),len(ins))]
 if any('+ 0xa5]' in i.op_str or '+ 0x36]' in i.op_str for i in ins):
  print('FUNCTION',hex(r+0x1a0000))
  for j,i in enumerate(ins):
   if any(x in i.op_str for x in ['+ 0xa5]','+ 0x36]','+ 0x32]']):
    for a in ins[max(0,j-6):j+16]:print(hex(a.address),a.mnemonic,a.op_str)
