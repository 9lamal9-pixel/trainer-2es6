import json,sys
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
md=Cs(CS_ARCH_X86,CS_MODE_32)
f=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/MSUL_MON.dll');p=pefile.PE(str(f));ss=json.loads(Path('work/msul_emulator/display_reference/MSUL_MON.dll.procs.json').read_text())
for z in ss:
 if ('TMSULClass' in z['name'] or 'TWindowMonitor' in z['name'] or ('TMainMSULForm' in z['name'] and any(x in z['name'] for x in ['GetBitmapFlag','FormCreate']))) and not 'Set' in z['name']:
  print(z['name'],hex(z['rva']),z['length'])
for r,n in [(0x1ef890,58),(0x1ef718,68),(0x1ef960,68),(0x1f38c8,68),(0x1eeb80,102)]:
 print('FUNCTION',hex(r))
 for i in md.disasm(p.get_data(r,n),r+0x400000):print(hex(i.address),i.mnemonic,i.op_str)
