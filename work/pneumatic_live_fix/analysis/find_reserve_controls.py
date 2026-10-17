import sys;sys.stdout.reconfigure(errors="backslashreplace")
from pathlib import Path
paths=[Path('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe'),Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Communication1.dll'),Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Pult1.dll')]
for p in paths:
 d=p.read_bytes();print(p.name)
 for s in ['УКТОЛ','КПР','КрП','КРУ','UKTOL','KPR','KRU']:
  for enc in ['cp1251','utf-16le']:
   needle=s.encode(enc);start=0;count=0
   while count<4:
    i=d.find(needle,start)
    if i<0:break
    print(s,enc,hex(i),repr(d[max(0,i-20):i+70].decode(enc,errors='replace')))
    start=i+len(needle);count+=1


