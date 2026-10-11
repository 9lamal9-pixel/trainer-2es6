from pathlib import Path
import pefile,struct,json,sys
f=Path(sys.argv[1]) if len(sys.argv)>1 else Path(r'work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Communication1.dll')
d=f.read_bytes(); pe=pefile.PE(data=d);base=pe.OPTIONAL_HEADER.ImageBase
def raw(va): return pe.get_offset_from_rva(va-base)
def byte(va): return d[raw(va)]
def u32(va): return struct.unpack_from('<I',d,raw(va))[0]
def u16(va): return struct.unpack_from('<H',d,raw(va))[0]
def typename(va):
    if va==0: return 'anonymous'
    a=va+4
    if u32(va)!=a: return '?'
    n=byte(a+1)
    return d[raw(a+2):raw(a+2)+n].decode('cp1251')
def parse(va):
    a=va+4;kind=byte(a);name=typename(va);q=a+2+byte(a+1)
    out={'va':hex(va),'name':name,'kind':kind}
    if kind==0x14:out['target']=hex(u32(q))
    elif kind==0x0e:
        out['size']=u32(q);q+=4
        managed=u32(q);q+=4+managed*8+1
        count=u32(q);q+=4
        fields=[]
        for _ in range(count):
            ty,off=u32(q),u32(q+4);q+=8;vis=byte(q);ln=byte(q+1);q+=2
            nm=d[raw(q):raw(q)+ln].decode('cp1251');q+=ln
            attr=u16(q);q+=attr
            fields.append({'offset':hex(off),'name':nm,'type':typename(ty),'typeva':hex(ty)})
        out['fields']=fields
    return out
want=[int(a,16) for a in sys.argv[2:]] if len(sys.argv)>2 else [0x5a18ac,0x5963d8,0x5962e8,0x596300,0x595f60,0x595e94,0x5955e4,0x5955fc,0x5954b8,0x5a1128]
for va in want:
    print(json.dumps(parse(va),ensure_ascii=False))

