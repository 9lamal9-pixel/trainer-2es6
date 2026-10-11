from pathlib import Path
import re,json,datetime,hashlib,sys
import pefile
sys.stdout.reconfigure(encoding='utf8',errors='replace')
base=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES')
out=Path(__file__).parent
metas=[]
interesting=re.compile(r'(?:2ES6|2ЭС6|МСУЛ|СИНАРА|Электровоз|локомотив|Тормоз|тормоз|Давлен|давлен|скорост|тяги|тяга|сцеплен|масса|поезд|Пульт|COM[0-9]|RS.?485|CreateFile|ReadFile|WriteFile|DeviceIoControl|SetComm|GetComm|PurgeComm|ClearComm|COMMTIMEOUTS|Communication|Communicat|Tyga|Torm|Poezd|GetProcAddress|LoadLibrary|USB|usbh|board|cb[A-Z]|udp|tcp|localhost|\d+\.\d+\.\d+\.\d+|https?://)',re.I)
for f in base.rglob('*.dll'):
    rel=str(f.relative_to(base))
    if '\\2ES6\\' in rel: continue # duplicate candidate, compare separately
    data=f.read_bytes()
    try: pe=pefile.PE(data=data)
    except Exception as e: print(rel,e);continue
    d={'path':rel,'size':len(data),'sha256':hashlib.sha256(data).hexdigest(),'machine':hex(pe.FILE_HEADER.Machine),'timestamp':datetime.datetime.fromtimestamp(pe.FILE_HEADER.TimeDateStamp,datetime.timezone.utc).isoformat(),'certificate_size':pe.OPTIONAL_HEADER.DATA_DIRECTORY[4].Size}
    d['exports']=[(x.name or b'').decode(errors='replace') or '#'+str(x.ordinal) for x in getattr(pe,'DIRECTORY_ENTRY_EXPORT',type('x',(),{'symbols':[]})()).symbols]
    d['imports']={i.dll.decode(errors='replace'):[x.name.decode(errors='replace') if x.name else '#'+str(x.ordinal) for x in i.imports] for i in getattr(pe,'DIRECTORY_ENTRY_IMPORT',[])}
    d['version']={}
    for group in getattr(pe,'FileInfo',[]):
        for obj in group:
            for st in getattr(obj,'StringTable',[]):
                d['version'].update({k.decode(errors='replace'):v.decode(errors='replace') for k,v in st.entries.items()})
    d['debug_size']=sum(s.SizeOfRawData for s in pe.sections if s.Name.rstrip(b'\0')==b'.debug')
    metas.append(d)
    found=[]
    for pat,enc in [(rb'[\x20-\x7e\xc0-\xff]{5,}','cp1251'),(rb'(?:(?:[\x20-\x7e]\x00)|(?:[\x00-\x5f]\x04)){5,}','utf-16-le')]:
        for m in re.finditer(pat,data):
            s=m.group().decode(enc,errors='replace')
            if len(s)<500 and interesting.search(s): found.append((m.start(),enc,s))
    found.sort()
    key=rel.replace('\\','__')
    (out/(key+'.strings.txt')).write_text('\n'.join(f'{offset:08x} {enc} {s}' for offset,enc,s in found),encoding='utf8')
    print(rel,'size',d['size'],'debug',d['debug_size'],'exports',d['exports'],'version',d['version'])
(out/'metadata.json').write_text(json.dumps(metas,ensure_ascii=False,indent=2),encoding='utf8')
