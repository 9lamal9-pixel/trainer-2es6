import sys
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/BLOK.dll');md=Cs(CS_ARCH_X86,CS_MODE_32)
for r,n in [(0x1aeb48,780),(0x1af454,90),(0x1ad790,100)]:
 print(hex(r))
 for i in md.disasm(p.get_data(r,n),r+0x400000):print(hex(i.address),i.mnemonic,i.op_str)
