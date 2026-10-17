import json,sys,hashlib
from pathlib import Path
root=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(root/'work/traction6000/vendor'))
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
import pefile
out=root/'dev/runs/autovedenie-development-20261013/native-selected.json'
modules={'pult':('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Pult1.dll',[(0x178ca0,0x19),(0x187378,0x1c0),(0x178b04,0x19c)]),'msul':('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/MSUL_MON.dll',[(0x1f0470,0x100)]),'tyga':('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Tyga1.dll',[(0x17c320,0x80)])}
md=Cs(CS_ARCH_X86,CS_MODE_32)
result={}
for name,(path,blocks) in modules.items():
 data=(root/path).read_bytes();pe=pefile.PE(data=data);base=pe.OPTIONAL_HEADER.ImageBase
 entry={'path':path,'sha256':hashlib.sha256(data).hexdigest(),'image_base':hex(base),'exports':[],'blocks':[]}
 if hasattr(pe,'DIRECTORY_ENTRY_EXPORT'):
  entry['exports']=[{'name':e.name.decode('ascii',errors='replace') if e.name else None,'rva':hex(e.address)} for e in pe.DIRECTORY_ENTRY_EXPORT.symbols if e.name and any(s in e.name.decode('ascii',errors='replace').lower() for s in ['proceed','param','tyga','init','close'])]
 for rva,length in blocks:
  raw=pe.get_data(rva,length);entry['blocks'].append({'rva':hex(rva),'bytes':raw.hex(),'instructions':[{'rva':hex(i.address),'bytes':i.bytes.hex(),'text':i.mnemonic+' '+i.op_str} for i in md.disasm(raw,rva)]})
 result[name]=entry
out.write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'output':str(out),'modules':{k:{'sha256':v['sha256'],'blocks':len(v['blocks'])} for k,v in result.items()}}))
