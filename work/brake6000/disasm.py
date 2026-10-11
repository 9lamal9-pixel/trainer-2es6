import sys,json,struct,re
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parent.parent/'traction6000'/'vendor'))
from capstone import *
from capstone.x86 import *
import pefile
f=Path(r'work\trenager_static\WORK_TRENAGER_3D\EditLibrary.exe')
d=f.read_bytes();pe=pefile.PE(data=d);base=pe.OPTIONAL_HEADER.ImageBase
procs=json.loads(Path(r'work\brake6000\EditLibrary.exe.procs.json').read_text(encoding='utf8'))
by_addr={x['rva']:x['name'] for x in procs}
md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True
def const(va,n):
    try:
        raw=pe.get_offset_from_rva(va-base);b=d[raw:raw+n]
        if n==4:return str(struct.unpack('<f',b)[0])
        if n==8:return str(struct.unpack('<d',b)[0])
        if n==10:
            m,e=struct.unpack('<QH',b);sign=-1 if e>>15 else 1
            return str(sign*m/(1<<63)*2.**((e&0x7fff)-16383))
    except Exception:return None
out=Path(__file__).parent/'disasm';out.mkdir(exist_ok=True)
selected=[p for p in procs if any(a.lower() in p['name'].lower() for a in sys.argv[1:])]
for p in selected:
    o=pe.get_offset_from_rva(p['rva']);code=d[o:o+p['length']]
    lines=[json.dumps(p,ensure_ascii=False)]
    for ins in md.disasm(code,p['rva']):
        note=[]
        if ins.mnemonic in ('call','jmp') and ins.operands[0].type==X86_OP_IMM:
            if ins.operands[0].imm in by_addr:note.append(by_addr[ins.operands[0].imm])
        for op in ins.operands:
            if op.type==X86_OP_MEM and op.mem.base==0 and op.mem.index==0:
                v=const(op.mem.disp,op.size)
                if v is not None:note.append('mem'+hex(op.mem.disp)+' '+str(op.size)+'B float='+v)
        lines.append(f'{ins.address:08x} {ins.bytes.hex():24} {ins.mnemonic:8} {ins.op_str}' + (' ; '+' | '.join(note) if note else ''))
    tag=re.sub('[^A-Za-z0-9_-]','_',p['name'])[:180]
    dest=out/(tag+'.txt');dest.write_text('\n'.join(lines),encoding='utf8')
    print(dest)
