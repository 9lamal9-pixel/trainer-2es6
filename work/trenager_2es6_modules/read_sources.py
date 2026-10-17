from pathlib import Path
import sys
sys.stdout.reconfigure(encoding='utf8',errors='replace')
base=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1')
for rel in ['Init/ConstHardware.pas','Init/Калибровка МСУД_БЛОК.txt','2ES6/1/Init/ConstHardware.pas','1/Init/ConstHardware.pas']:
    f=base/rel
    data=f.read_bytes()
    print('\nFILE',rel)
    txt=data.decode('cp1251')
    for n,line in enumerate(txt.splitlines(),1):print(f'{n:03} {line}')
