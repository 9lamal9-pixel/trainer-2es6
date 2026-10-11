import sys,struct,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parent.parent/'traction6000'/'vendor'))
from capstone import *
from capstone.x86 import *
import pefile
f=Path(r'work\trenager_static\WORK_TRENAGER_3D\RESOURCES\POEZD.dll');d=f.read_bytes();p=pefile.PE(data=d);base=p.OPTIONAL_HEADER.ImageBase
named={base+s.address:s.name.decode() for s in p.DIRECTORY_ENTRY_EXPORT.symbols}
md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True
def number(va,n):
 try:
  b=d[p.get_offset_from_rva(va-base):][:n]
  if n==4:return str(struct.unpack('<f',b)[0])
  if n==8:return str(struct.unpack('<d',b)[0])
  if n==10:
   m,e=struct.unpack('<QH',b);return str((-1 if e>>15 else 1)*m/(1<<63)*2.**((e&0x7fff)-16383))
 except:return ''
start=int(sys.argv[1],0) if len(sys.argv)>1 else 0x1000;end=int(sys.argv[2],0) if len(sys.argv)>2 else 0x2a00
lines=[]
for ins in md.disasm(d[p.get_offset_from_rva(start):p.get_offset_from_rva(end)],base+start):
 if ins.address in named:lines.append('\n'+named[ins.address]+':')
 notes=[]
 if ins.mnemonic in ('call','jmp') and ins.operands[0].type==X86_OP_IMM and ins.operands[0].imm in named:notes.append(named[ins.operands[0].imm])
 for op in ins.operands:
  if op.type==X86_OP_MEM and op.mem.base==0 and op.mem.index==0 and op.size in (4,8,10):notes.append(f'{op.size}B floating={number(op.mem.disp,op.size)}')
 lines.append(f'{ins.address:08x} {ins.mnemonic:8} {ins.op_str}'+(' ; '+' | '.join(notes) if notes else ''))
out=Path(__file__).parent/f'poezd_{start:x}_{end:x}.txt';out.write_text('\n'.join(lines),encoding='utf8');print(out)
