import pefile,sys
from pathlib import Path
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
md=Cs(CS_ARCH_X86,CS_MODE_32)
for name in ['Saut_Dll.dll','CalcOgr.dll','AVP.dll','KPD_3PV.dll','SaveParam.dll','SoundsTren.dll']:
 p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/'+name,fast_load=True);p.parse_data_directories(directories=[0]);out=[]
 for e in p.DIRECTORY_ENTRY_EXPORT.symbols:
  if e.name and any(x in e.name.decode().lower() for x in ['init','proceed','filllog','savelog']):
   out.append(e.name.decode()+' '+hex(e.address))
   for i in md.disasm(p.get_data(e.address,1000),e.address+0x400000):
    out.append(f'{i.address:08x} {i.mnemonic} {i.op_str}')
    if i.mnemonic=='ret':break
 dest=Path('work/msul_emulator')/(name+'.exports.asm');dest.write_text('\n'.join(out));print(dest)
