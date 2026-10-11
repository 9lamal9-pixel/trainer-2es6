import sys,json,bisect
from pathlib import Path
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
from capstone.x86 import X86_OP_MEM
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe')
procs=sorted(json.loads(Path('work/brake6000/main.procs.json').read_text()),key=lambda x:x['rva'])
starts=[x['rva'] for x in procs];m=Cs(CS_ARCH_X86,CS_MODE_32);m.detail=True;m.skipdata=True
for ins in m.disasm(p.get_data(0x200000,0x140000),0x200000):
 if ins.id and any(o.type==X86_OP_MEM and o.mem.disp==0x71 for o in ins.operands):
  i=bisect.bisect_right(starts,ins.address)-1
  print(hex(ins.address),ins.mnemonic,ins.op_str,procs[i]['name'])


