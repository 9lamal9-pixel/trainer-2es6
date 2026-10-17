from pathlib import Path
import pefile, json, re, hashlib, datetime, sys
sys.stdout.reconfigure(encoding='utf-8')
root=Path(__file__).parent / 'trenager_static' / 'WORK_TRENAGER_3D'
out=Path(__file__).parent / 'trenager_pe'
out.mkdir(exist_ok=True)
paths=list(root.glob('*.exe'))+list((root/'OPTIONS').glob('*.exe'))+[root/'Trenager_3D.exetmpVideo.dat']
for path in paths:
    data=path.read_bytes()
    name=path.name
    if not data.startswith(b'MZ'):
        print(name,'Not PE, prefix',data[:32].hex());continue
    pe=pefile.PE(data=data)
    imports={}
    for e in getattr(pe,'DIRECTORY_ENTRY_IMPORT',[]):
        imports.setdefault(e.dll.decode(errors='replace'),[]).extend(i.name.decode(errors='replace') if i.name else 'ordinal:'+str(i.ordinal) for i in e.imports)
    info={'name':str(path.relative_to(root)),'sha256':hashlib.sha256(data).hexdigest(),'machine':hex(pe.FILE_HEADER.Machine),'timestamp_utc':str(datetime.datetime.fromtimestamp(pe.FILE_HEADER.TimeDateStamp,datetime.timezone.utc)),'image_base':hex(pe.OPTIONAL_HEADER.ImageBase),'subsystem':pe.OPTIONAL_HEADER.Subsystem,'sections':[{'name':s.Name.rstrip(b'\0').decode(errors='replace'),'raw_size':s.SizeOfRawData,'entropy':round(s.get_entropy(),2)} for s in pe.sections],'imports':imports}
    (out/(name+'.json')).write_text(json.dumps(info,ensure_ascii=False,indent=2),encoding='utf-8')
    strings=[]
    for m in re.finditer(rb'[\x20-\x7e]{5,}',data):
        strings.append((m.start(),'ascii',m.group().decode()))
    for parity in [0,1]:
        dec=data[parity:len(data)-((len(data)-parity)%2)].decode('utf-16le',errors='replace')
        for m in re.finditer(r'[\x20-\x7e\u0400-\u04ff]{5,}',dec):
            strings.append((parity+m.start()*2,'utf16le',m.group()))
    strings.sort()
    (out/(name+'.strings.txt')).write_text('\n'.join(f'0x{o:08x}\t{e}\t{s}' for o,e,s in strings),encoding='utf-8')
    print(json.dumps({k:info[k] for k in ['name','machine','timestamp_utc','subsystem']},ensure_ascii=False))
    print('Nonstandard imports:',[k for k in imports if k.lower() not in ['kernel32.dll','user32.dll','gdi32.dll','advapi32.dll','ole32.dll','oleaut32.dll','comctl32.dll','comdlg32.dll','shell32.dll','version.dll','winspool.drv','msimg32.dll','msvcrt.dll','mpr.dll']])
