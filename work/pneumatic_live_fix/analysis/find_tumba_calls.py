import sys,struct
from pathlib import Path
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll')
m=Cs(CS_ARCH_X86,CS_MODE_32)
for s in p.sections:
 if not s.Characteristics&0x20000000:continue
 d=s.get_data();r=s.VirtualAddress
 for i in range(len(d)-5):
  if d[i]==0xe8 and r+i+5+struct.unpack_from('<i',d,i+1)[0]==2792612:
   print('CALL',hex(r+i))
   for ins in m.disasm(d[max(0,i-35):i+45],r+max(0,i-35)):print(hex(ins.address),ins.mnemonic,ins.op_str)
