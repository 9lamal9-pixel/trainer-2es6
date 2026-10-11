import sys
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/BLOK.dll');md=Cs(CS_ARCH_X86,CS_MODE_32)
for i in md.disasm(p.get_data(0x1afda4,212),0x5afda4):print(hex(i.address),i.mnemonic,i.op_str)
