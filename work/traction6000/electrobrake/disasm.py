from pathlib import Path
import sys,struct,json
sys.path.insert(0,str(Path(__file__).parent/'pydeps'))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
from capstone.x86 import X86_OP_MEM,X86_OP_IMM
f=Path(r'work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Tyga1.dll')
d=f.read_bytes();pe=pefile.PE(data=d);base=pe.OPTIONAL_HEADER.ImageBase
procs=json.loads(Path(r'work\compare137_modules\Tyga1.dll.procs.json').read_text(encoding='utf8'))
byaddr={base+x['rva']:x['name'] for x in procs}
md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True
def vals(a):
 try:
  o=pe.get_offset_from_rva(a-base)
  mant,exp=struct.unpack_from('<QH',d,o)
  try:e80=(-1 if exp&0x8000 else 1)*mant*2.0**((exp&0x7fff)-16383-63)
  except OverflowError:e80=float('inf')
  return f'bytes={d[o:o+10].hex()} f32={struct.unpack_from("<f",d,o)[0]:.8g} f64={struct.unpack_from("<d",d,o)[0]:.8g} f80={e80:.8g}'
 except Exception:return ''
def show(x):
 print('\n'+x['name']+' RVA='+hex(x['rva'])+' len='+hex(x['length']))
 o=pe.get_offset_from_rva(x['rva'])
 for i in md.disasm(d[o:o+x['length']],base+x['rva']):
  anno=[]
  if i.mnemonic=='call' and i.operands[0].type==X86_OP_IMM:
   a=i.operands[0].imm
   if a in byaddr:anno.append(byaddr[a])
  for op in i.operands:
   if op.type==X86_OP_MEM and not op.mem.base and not op.mem.index and op.mem.disp>=base:
    anno.append(hex(op.mem.disp)+':'+vals(op.mem.disp))
  print(f'{i.address-base:08x} {i.bytes.hex():24s} {i.mnemonic:9s} {i.op_str:40s}'+' ; '+' | '.join(anno))
for term in sys.argv[1:]:
 for x in procs:
  if term.lower() in x['name'].lower():show(x)
