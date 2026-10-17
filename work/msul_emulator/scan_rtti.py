import sys,struct,json,re
from pathlib import Path
sys.stdout.reconfigure(encoding='utf8')
helper=Path('work/compare137_modules/rtti.py').read_text().split('want=')[0]
for name in ['TormModel.dll','Saut_Dll.dll','CalcOgr.dll','AVP.dll','KPD_3PV.dll','SaveParam.dll','SoundsTren.dll']:
 f=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES')/name
 sys.argv=['rtti',str(f),'0'];e={};exec(helper,e);d=e['d'];p=e['pe'];base=e['base'];arr=[]
 for sec in p.sections:
  raw=sec.get_data();rva=sec.VirtualAddress;start=sec.PointerToRawData
  for o in range(0,len(raw)-8,4):
   if struct.unpack_from('<I',raw,o)[0]!=base+rva+o+4 or raw[o+4]!=14:continue
   try:
    z=e['parse'](base+rva+o)
    if z.get('size',0)<1000000 and len(z.get('fields',[]))<1000:arr.append(z)
   except Exception:pass
 dest=Path('work/msul_emulator')/(name+'.rtti.json');dest.write_text(json.dumps(arr,ensure_ascii=False,indent=2),encoding='utf8')
 print('\n',name,len(arr))
 for z in arr:
  if any(x in z['name'].lower() for x in ['saut','init','pribbez','calcogr','nesht','trainparamtm','lastparam','kpd','save','loghead','avp']):print(z['name'],z['size'],[(a['name'],a['offset'],a['type']) for a in z['fields']])
