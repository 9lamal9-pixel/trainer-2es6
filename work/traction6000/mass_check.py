import sys,struct,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parent/'vendor'))
from capstone import *
import pefile
f=Path(r'work\trenager_static\WORK_TRENAGER_3D\Trenager_3D.exe')
d=f.read_bytes();pe=pefile.PE(data=d);base=pe.OPTIONAL_HEADER.ImageBase
def raw(va,n):
    o=pe.get_offset_from_rva(va-base);return d[o:o+n]
lines=[]
for va,n in [(0x61d758,4),(0x61d7b0,32)]:
    data=raw(va,n)
    lines.append(f'VA{va:08x} bytes {data.hex()} dwords '+','.join(hex(x) for x in struct.unpack('<'+'I'*(n//4),data)))
md=Cs(CS_ARCH_X86,CS_MODE_32)
for va,n in [(0x62dc94,28),(0x62dcb0,28),(0x62dde8,80),(0x630500,50),(0x633adc,50)]:
    lines.append(f'CODE VA{va:08x}')
    for ins in md.disasm(raw(va,n),va):
        lines.append(f'{ins.address:08x} {ins.bytes.hex():22} {ins.mnemonic} {ins.op_str}')
        if ins.mnemonic=='ret':break
out=Path(__file__).parent/'mass_check.txt';out.write_text('\n'.join(lines),encoding='utf8');print('\n'.join(lines))
