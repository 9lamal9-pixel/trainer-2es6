import sys,json,struct,re
from pathlib import Path
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
from capstone.x86 import *
import pefile
f=Path(r'work/trenager_static/WORK_TRENAGER_3D/RESOURCES/TormModel.dll') if len(sys.argv)<4 else Path(sys.argv[3])
d=f.read_bytes();pe=pefile.PE(data=d);base=pe.OPTIONAL_HEADER.ImageBase
md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True
exports={x.address:x.name.decode() for x in pe.DIRECTORY_ENTRY_EXPORT.symbols} if hasattr(pe,'DIRECTORY_ENTRY_EXPORT') else {}
def const(va,n):
 try:
  o=pe.get_offset_from_rva(va-base); b=d[o:o+n]
  if n==4:return str(struct.unpack('<f',b)[0])
  if n==8:return str(struct.unpack('<d',b)[0])
  if n==10:
   m,e=struct.unpack('<QH',b);return str((-1 if e>>15 else 1)*m/(1<<63)*2.**((e&0x7fff)-16383))
 except Exception:return None
start=int(sys.argv[1],0);length=int(sys.argv[2],0)
code=pe.get_data(start,length);lines=[]
for ins in md.disasm(code,start):
 note=[]
 if ins.mnemonic in ('call','jmp') and ins.operands[0].type==X86_OP_IMM:
  if ins.operands[0].imm in exports:note.append(exports[ins.operands[0].imm])
 for op in ins.operands:
  if op.type==X86_OP_MEM and op.mem.base==0 and op.mem.index==0:
   v=const(op.mem.disp,op.size)
   if v is not None:note.append('mem'+hex(op.mem.disp)+' '+str(op.size)+'B float='+v)
 lines.append(f'{ins.address:08x} {ins.bytes.hex():24} {ins.mnemonic:8} {ins.op_str}'+(' ; '+' | '.join(note) if note else ''))
out=Path('work/pneumatic_live_fix/analysis')/(hex(start)+'.txt');out.write_text('\n'.join(lines),encoding='utf8');print(out)

