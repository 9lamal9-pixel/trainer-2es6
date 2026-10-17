from pathlib import Path
import json,sys,struct
sys.stdout.reconfigure(encoding='utf-8')
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32
out=Path(__file__).parent
md=Cs(CS_ARCH_X86,CS_MODE_32);md.skipdata=True
for name in ['Pult1.dll','Communication1.dll']:
 f=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1')/name
 pe=pefile.PE(str(f));base=pe.OPTIONAL_HEADER.ImageBase
 procs=json.loads(Path('work/compare137_modules',name+'.procs.json').read_text(encoding='utf-8'))
 for p in procs:
  if any(k in p['name'] for k in ['TPult2ES6@UpdatePribors','TPult2ES6@UpdateOborud','TComm2ES6@UpdatePriborsHW','Pos_Kran130ChangeState','Pos_Kran215ChangeState','EPK_Key_BtnChangeState','VCU_BtnChangeState','UpdateButtonsHW','KRU_btnChangeState','Tumbler4Click']):
   tag=p['name'].replace('@','_').replace('$','_')
   ins=list(md.disasm(pe.get_data(p['rva'],p['length']),base+p['rva']))
   (out/(name+tag+'.asm')).write_text(json.dumps(p)+'\n'+'\n'.join('%08x %s %-8s %s'%(i.address,i.bytes.hex(),i.mnemonic,i.op_str) for i in ins),encoding='utf-8')
   print(name,p['name'],hex(p['rva']),p['length'])
 for p in procs:
  if any(k in p['name'] for k in ['UpdatePribors','P_tc','PTc','PtcLoc']):print(p['name'],hex(p['rva']),p['length'])
