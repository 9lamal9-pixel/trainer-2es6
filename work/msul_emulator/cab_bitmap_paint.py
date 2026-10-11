import json,sys
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll');md=Cs(CS_ARCH_X86,CS_MODE_32)
for i in md.disasm(p.get_data(0x2a8eb8,340),0x6a8eb8):print(hex(i.address),i.mnemonic,i.op_str)
z=json.loads(Path('work/msul_emulator/display_reference/TPULTMASHINDEXFORM.json').read_text())
def walk(z):
 if z['class']=='TDrawAlienBitmab':print(z)
 for x in z['children']:walk(x)
walk(z)
