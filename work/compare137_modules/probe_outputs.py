from pathlib import Path
import pefile, struct, json
f=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Communication1.dll')
data=f.read_bytes(); pe=pefile.PE(data=data)
procs=json.loads(Path(r'work\trenager_2es6_modules\Communication1.procs.json').read_text(encoding='utf8'))
base=pe.OPTIONAL_HEADER.ImageBase
def readva(va, count):
    return data[pe.get_offset_from_rva(va-base):pe.get_offset_from_rva(va-base)+count]
def u32(va):
    return struct.unpack('<I',readva(va,4))[0]
for va in (0x6c7804,0x6c795c,0x6c7c4c,0x6c75e0,0x6c765c,0x6c72e4,0x6c7228):
    v=u32(va)
    print(hex(va),hex(v), readva(v,32).hex() if base<=v<base+pe.OPTIONAL_HEADER.SizeOfImage else 'outside')
for p in procs:
    if '@TComm2ES6@UpdatePriborsHW$' in p['name']:
        raw=pe.get_offset_from_rva(p['rva']); b=data[raw:raw+p['length']]
        print(p['name'],hex(p['rva']),b.hex())
for va in (0x5962e8,0x5955e4,0x5963d8,0x597ef4,0x5a18b0):
    print('TYPE',hex(va),repr(readva(va,200)))
for p in procs:
    if '@THardwares@SetDblValule$' in p['name']:
        raw=pe.get_offset_from_rva(p['rva']); b=data[raw:raw+p['length']]
        print(p['name'],hex(p['rva']),b.hex())
