from pathlib import Path
import pefile
p=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll')
for t in p.DIRECTORY_ENTRY_RESOURCE.entries:
 for n in t.directory.entries:
  if n.name and 'TUMBA' in str(n.name).upper():
   a=n.directory.entries[0].data.struct
   out=Path('work/pneumatic_live_fix/analysis')/(str(n.name)+'.dfm')
   out.write_bytes(p.get_data(a.OffsetToData,a.Size));print(out,a.Size)
