import sys,json,struct
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parent/'vendor'))
from capstone import *
from capstone.x86 import *
import pefile
f=Path(r'work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Tyga1.dll')
d=f.read_bytes();pe=pefile.PE(data=d)
procs=json.loads(Path(r'work\compare137_modules\Tyga1.dll.procs.json').read_text(encoding='utf8'))
p=next(x for x in procs if x['name']=='@Consttyga@TConst@InitConst$qqrp16Consttyga@TConst')
o=pe.get_offset_from_rva(p['rva']);md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True
storage=bytearray(0x418);written=set();writes=[]
for i in md.disasm(d[o:o+p['length']],p['rva']):
    assert not i.mnemonic.startswith('j') and i.mnemonic!='call', 'not straight line'
    if i.mnemonic!='mov' or len(i.operands)!=2:continue
    a,b=i.operands
    if a.type!=X86_OP_MEM or a.mem.base!=X86_REG_EAX or a.mem.index!=0:continue
    if b.type==X86_OP_IMM:v=b.imm&0xffffffff
    elif b.type==X86_OP_REG and b.reg==X86_REG_EDX:v=0
    else:raise ValueError(i.op_str)
    assert a.size==4
    off=a.mem.disp;storage[off:off+4]=struct.pack('<I',v);written.update(range(off,off+4))
    writes.append({'rva':hex(i.address),'offset':hex(off),'dword':hex(v),'single':struct.unpack('<f',struct.pack('<I',v))[0]})
assert len(written)==len(storage)
out=Path(__file__).parent
(out/'TConst.bin').write_bytes(storage)
(out/'TConst.writes.json').write_text(json.dumps(writes,indent=2),encoding='utf8')
sf=lambda off:struct.unpack_from('<f',storage,off)[0]
db=lambda off:struct.unpack_from('<d',storage,off)[0]
arrays={}
for nm,off,size,width in [('TygaS',0,0x90,16),('OgrSc',0x90,0x10,16),('OgrScRec',0xa0,0x10,16),('CurP',0xb0,0x140,16),('Rpusk',0x1f0,0x104,4),('R_EDT_P',0x2f4,0x54,4),('R_EDT_S',0x348,0x54,4),('CF',0x3c0,0xc,12)]:
    arrays[nm]={'offset':hex(off),'rows':[[sf(j+k) for k in range(0,width,4)] for j in range(off,off+size,width)]}
fields={nm:sf(off) for nm,off in [('Rya',0x39c),('Rgp',0x3a0),('Rkomp',0x3a4),('Rreak',0x3a8),('Lreak',0x3ac),('Rshunt_R5_R6',0x3b0),('Rshunt_R13_R14',0x3b4),('RShunt_R15_R16',0x3b8),('R11',0x3bc)]}
fields.update({nm:db(off) for nm,off in [('K5',0x3cc),('K4',0x3d4),('R2',0x3dc),('R1',0x3e4),('Kper',0x3ec),('Rkol',0x3f4),('Rnagr',0x400),('Rstan',0x408),('Rline',0x410)]})
fields['Eset']=struct.unpack_from('<i',storage,0x3fc)[0]
result={'arrays':arrays,'fields':fields}
(out/'TConst.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(result,ensure_ascii=False))
