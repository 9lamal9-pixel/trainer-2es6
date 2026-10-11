import pefile
from pathlib import Path
for f in Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES').glob('*.dll'):
 if f.name.startswith(('D3D','av','sw','lib')):continue
 p=pefile.PE(str(f),fast_load=True);p.parse_data_directories(directories=[0])
 names=[x.name.decode() for x in p.DIRECTORY_ENTRY_EXPORT.symbols if x.name] if hasattr(p,'DIRECTORY_ENTRY_EXPORT') else []
 print(f.name, names)
