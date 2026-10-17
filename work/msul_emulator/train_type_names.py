from pathlib import Path
import re,pefile,sys
sys.stdout.reconfigure(encoding='utf8')
for f in [Path('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe'),Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/TormModel.dll')]:
 b=f.read_bytes();p=pefile.PE(data=b,fast_load=True)
 print(f.name)
 for m in re.finditer(rb'[A-Za-z][A-Za-z_0-9]{4,80}',b):
  if any(x in m.group().lower() for x in [b'trainptr',b'traindata',b'tsostav',b'tposkran',b'tvar',b'ttrain']):
   print(hex(p.get_rva_from_offset(m.start())),m.group().decode())
