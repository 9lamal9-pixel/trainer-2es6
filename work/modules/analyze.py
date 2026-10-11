from pathlib import Path
import pefile, re, json, datetime
base=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\launch_cam_static\LAUNCH_CAM\RESOURCES')
out=Path(__file__).parent
for f in [base/'Camera3D.dll',base/'KONTROL'/'BusinessPlay.dll',base/'RADIO'/'Radio.dll',base/'RADIO'/'RadioClient.exe']:
    data=f.read_bytes()
    pe=pefile.PE(data=data)
    meta={'name':f.name,'size':len(data),'magic':data[:32].hex(),'machine':hex(pe.FILE_HEADER.Machine),'timestamp':datetime.datetime.fromtimestamp(pe.FILE_HEADER.TimeDateStamp,datetime.timezone.utc).isoformat(),'entrypoint':hex(pe.OPTIONAL_HEADER.AddressOfEntryPoint),'dll':bool(pe.FILE_HEADER.Characteristics&0x2000),'subsystem':pe.OPTIONAL_HEADER.Subsystem,'security_directory_size':pe.OPTIONAL_HEADER.DATA_DIRECTORY[4].Size,'sections':[{'name':s.Name.rstrip(b'\0').decode(errors='replace'),'size':s.SizeOfRawData,'entropy':s.get_entropy()} for s in pe.sections]}
    meta['exports']=[{'name':(e.name or b'').decode(errors='replace'),'ordinal':e.ordinal,'address':hex(e.address)} for e in getattr(pe,'DIRECTORY_ENTRY_EXPORT',type('x',(),{'symbols':[]})()).symbols]
    meta['imports']={i.dll.decode(errors='replace'):[(x.name or b'').decode(errors='replace') if x.name else '#'+str(x.ordinal) for x in i.imports] for i in getattr(pe,'DIRECTORY_ENTRY_IMPORT',[])}
    meta['version']={}
    for group in getattr(pe,'FileInfo',[]):
        for obj in group:
            for st in getattr(obj,'StringTable',[]):
                meta['version'].update({k.decode(errors='replace'):v.decode(errors='replace') for k,v in st.entries.items()})
    (out/(f.name+'.json')).write_text(json.dumps(meta,ensure_ascii=False,indent=2),encoding='utf8')
    strings=[]
    for pat,enc in [(rb'[\x20-\x7e\xc0-\xff]{5,}','cp1251'),(rb'(?:[\x20-\x7e\xc0-\xff]\x00){5,}','utf-16-le')]:
        for m in re.finditer(pat,data):
            strings.append((m.start(),enc,m.group().decode(enc,errors='replace')))
    # Unicode Cyrillic may have high byte 04, and ascii has high byte 00.
    for m in re.finditer(rb'(?:(?:[\x20-\x7e]\x00)|(?:[\x00-\x5f]\x04)){5,}',data):
        strings.append((m.start(),'utf16ru',m.group().decode('utf-16-le',errors='replace')))
    strings.sort()
    (out/(f.name+'.strings.txt')).write_text('\n'.join(f'{offset:08x} {enc} {s}' for offset,enc,s in strings),encoding='utf8')
    print(json.dumps({k:meta[k] for k in ['name','size','machine','timestamp','dll','security_directory_size','version','exports']},ensure_ascii=False,indent=2))
