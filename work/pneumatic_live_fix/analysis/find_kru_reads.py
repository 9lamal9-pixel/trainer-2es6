import sys,json,bisect
from pathlib import Path
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
from capstone.x86 import X86_OP_MEM
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll')
procs=sorted(json.loads(Path('work/compare137_modules/Communication1.dll.procs.json').read_text()),key=lambda x:x['rva'])
starts=[x['rva'] for x in procs];m=Cs(CS_ARCH_X86,CS_MODE_32);m.detail=True;m.skipdata=True
for ins in m.disasm(p.get_data(0x290000,0x25000),0x290000):
 if ins.id and any(o.type==X86_OP_MEM and o.mem.disp==0x71 for o in ins.operands):
  i=bisect.bisect_right(starts,ins.address)-1
  print(hex(ins.address),ins.mnemonic,ins.op_str,procs[i]['name'])

