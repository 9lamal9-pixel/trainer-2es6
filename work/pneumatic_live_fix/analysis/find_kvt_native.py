from pathlib import Path
import sys
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs, CS_ARCH_X86, CS_MODE_32
from capstone.x86 import X86_OP_MEM
pe=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/TormModel.dll')
md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True;md.skipdata=True
matches=[]
for ins in md.disasm(pe.get_data(0x176000,0x5000),0x176000):
    if ins.id and any(op.type==X86_OP_MEM and op.mem.disp in (0xec,0x7e) for op in ins.operands):
        matches.append(f'{ins.address:08x} {ins.mnemonic} {ins.op_str}')
Path('outputs/Trainer_KVT_native_references.txt').write_text('\n'.join(matches),encoding='utf-8')
print('\n'.join(matches))
