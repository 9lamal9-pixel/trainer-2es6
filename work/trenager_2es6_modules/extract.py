from pathlib import Path
import pefile,re,json,sys
sys.stdout.reconfigure(encoding='utf8',errors='replace')
base=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1')
out=Path(__file__).parent
for name in ['Communication1.dll','Tyga1.dll','Pult1.dll','MSUL_MON.dll']:
    data=(base/name).read_bytes()
    pe=pefile.PE(data=data)
    debug=next((s.PointerToRawData for s in pe.sections if s.Name.startswith(b'.debug')),len(data))
    found=[]
    for m in re.finditer(rb'[\x20-\x7e]{4,}',data[:debug]):
        s=m.group().decode('ascii')
        if len(s)<500:found.append((m.start(),'ascii',s))
    for m in re.finditer(rb'(?:(?:[\x20-\x7e]\x00)|(?:[\x00-\x5f]\x04)){5,}',data[:debug]):
        s=m.group().decode('utf-16-le',errors='replace')
        if len(s)<500:found.append((m.start(),'utf16',s))
    found.sort()
    (out/(name+'.strings.txt')).write_text('\n'.join(f'{off:08x} {enc} {s}' for off,enc,s in found),encoding='utf8')
    # Retain compiler debug symbol names only, without normal RTL-heavy output.
    symbols=[]
    for m in re.finditer(rb'[\x20-\x7e]{5,}',data[debug:]):
        s=m.group().decode('ascii')
        if re.search(r'@(Communicationclassindex|Tygabasisclass|Tyga_2es6|Pultdllclassindex|Msul2es6class|Mainmsulformunit|Structurepult|Structura_tyga|Msulstructure|Shema|Updatebil2klubhw|Updatesauthw)@',s,re.I):
            symbols.append((m.start()+debug,s))
    (out/(name+'.symbols.txt')).write_text('\n'.join(f'{off:08x} {s}' for off,s in symbols),encoding='utf8')
    print(name,'strings',len(found),'symbols',len(symbols),'exports',[(e.name.decode() if e.name else '#'+str(e.ordinal),hex(e.address)) for e in pe.DIRECTORY_ENTRY_EXPORT.symbols])
