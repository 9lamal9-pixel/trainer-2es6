from pathlib import Path
import pefile, json, re, hashlib, datetime

root = Path(__file__).parent / 'launch_cam_static' / 'LAUNCH_CAM'
out = Path(__file__).parent / 'pe_main'
out.mkdir(exist_ok=True)
for name in ['LauncherClient.exe', 'UpdaterLauncherExe.exe']:
    path = root / name
    data = path.read_bytes()
    pe = pefile.PE(data=data)
    imports = {entry.dll.decode(errors='replace'): [i.name.decode(errors='replace') if i.name else f'ordinal:{i.ordinal}' for i in entry.imports] for entry in getattr(pe, 'DIRECTORY_ENTRY_IMPORT', [])}
    result = {
        'file': name, 'size': len(data), 'sha256': hashlib.sha256(data).hexdigest(),
        'machine': hex(pe.FILE_HEADER.Machine), 'timestamp_raw': pe.FILE_HEADER.TimeDateStamp,
        'timestamp_utc': str(datetime.datetime.fromtimestamp(pe.FILE_HEADER.TimeDateStamp, datetime.timezone.utc)),
        'subsystem': pe.OPTIONAL_HEADER.Subsystem, 'image_base': hex(pe.OPTIONAL_HEADER.ImageBase),
        'entry_point': hex(pe.OPTIONAL_HEADER.AddressOfEntryPoint),
        'sections': [{'name':s.Name.rstrip(b'\0').decode(errors='replace'), 'raw_size':s.SizeOfRawData, 'virtual_size':s.Misc_VirtualSize,'entropy':round(s.get_entropy(),2)} for s in pe.sections],
        'overlay_offset':pe.get_overlay_data_start_offset(), 'imports': imports,
    }
    strings=[]
    for match in re.finditer(rb'[\x20-\x7e\xa0-\xff]{5,}', data):
        value=match.group().decode('cp1251', errors='replace')
        strings.append((match.start(),'cp1251',value))
    for parity in [0,1]:
        decoded=data[parity:len(data)-((len(data)-parity)%2)].decode('utf-16le',errors='replace')
        for match in re.finditer(r'[\x20-\x7e\u0400-\u04ff]{5,}', decoded):
            strings.append((parity+match.start()*2,'utf16le',match.group()))
    strings.sort()
    (out / (name+'.json')).write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
    (out / (name+'.strings.txt')).write_text('\n'.join(f'0x{o:08x}\t{e}\t{s}' for o,e,s in strings),encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2))
    print('String count:',len(strings))
