import sys,json
from pathlib import Path
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
from capstone.x86 import X86_OP_MEM
p=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Pult1.dll');pe=pefile.PE(str(p));md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True;md.skipdata=True
rows=[]
for sec in pe.sections:
 if not sec.Characteristics & 0x20000000:continue
 for ins in md.disasm(sec.get_data(),sec.VirtualAddress):
  if ins.id==0:continue
  for j,op in enumerate(ins.operands):
   if op.type==X86_OP_MEM and op.mem.disp in (0x1a9,0x1b5,0x1ad,0x1b1,0x1b9,0x1bd):
    rows.append(dict(rva=hex(ins.address),asm=ins.mnemonic+' '+ins.op_str,operand=j,access=op.access,displacement=hex(op.mem.disp)))
out={'module':str(p.resolve()),'scope':'Linear executable-section Capstone decode; address-displacement hits are candidates, not a complete semantic proof of field accesses','hits':rows}
Path('outputs/Trainer_km_contact_static_audit.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf8')
for r in rows:print(r)
