from pathlib import Path
import struct,pefile,sys
sys.stdout.reconfigure(encoding='utf8',errors='replace')
f=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Communication1.dll')
d=f.read_bytes(); pe=pefile.PE(data=d)
sec=next(s for s in pe.sections if s.Name.startswith(b'.debug'))
base=sec.PointerToRawData+32; off=struct.unpack_from('<I',d,base+4)[0]
print('imagebase',hex(pe.OPTIONAL_HEADER.ImageBase),'debugdir',hex(base+off),len(d))
print('dir header',struct.unpack_from('<HHIII',d,base+off))
for guess in [base,sec.PointerToRawData]:
    pos=guess+off
    print('guess',hex(pos),struct.unpack_from('<HHIII',d,pos))
print('Proceedva',hex(struct.unpack_from('<I',d,0x2af2ec-5)[0]))
from collections import Counter
entries=[struct.unpack_from('<HHII',d,base+off+16+12*i) for i in range(454)]
print('types',Counter(hex(x[0]) for x in entries))
for typ,mod,lfo,cb in entries:
    if typ!=0x120:
        print(hex(typ),mod,hex(base+lfo),cb,d[base+lfo:base+lfo+50].hex())
