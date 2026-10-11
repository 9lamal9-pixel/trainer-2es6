import pefile,sys,json
from pathlib import Path
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/BLOK.dll');md=Cs(CS_ARCH_X86,CS_MODE_32)
ns={};exec(Path('work/brake6000/dfm.py').read_text().split('d=D(')[0],ns)
for t in p.DIRECTORY_ENTRY_RESOURCE.entries:
 if t.id!=10:continue
 for f in t.directory.entries:
  print('RESOURCE',str(f.name))
  a=f.directory.entries[0].data.struct;b=p.get_data(a.OffsetToData,a.Size)
  if b[:4]==b'TPF0':
   z=ns['D'](b).obj();z['children']=[];print(json.dumps(z))
for r,n in [(0x1af238,28),(0x1afc10,50),(0x1af10b,54)]:
 print(hex(r))
 for i in md.disasm(p.get_data(r,n),r+0x400000):print(hex(i.address),i.mnemonic,i.op_str)
# relevant bitmap flag/object fields
ins=list(md.disasm(p.get_data(0x1ada24,0x2490),0x5ada24))
for j,i in enumerate(ins):
 if any(s in i.op_str for s in ['+ 0xa8]','+ 0xa9]','+ 0x36]','+ 0x32]']):
  print('MATCH')
  for ii in ins[max(0,j-5):j+7]:print(hex(ii.address),ii.mnemonic,ii.op_str)
