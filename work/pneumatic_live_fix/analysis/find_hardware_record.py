import struct,pefile
from pathlib import Path
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll');d=p.__data__
for i in range(0x19ff00,0x1a060a):
 if struct.unpack_from('<I',d,i)[0]==p.OPTIONAL_HEADER.ImageBase+p.get_rva_from_offset(i)+4:
  n=d[i+5];print(hex(p.OPTIONAL_HEADER.ImageBase+p.get_rva_from_offset(i)),d[i+6:i+6+n].decode('cp1251'))

