from pathlib import Path
import pefile,struct,re,json,sys
sys.stdout.reconfigure(encoding='utf8',errors='replace')
f=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Communication1.dll')
d=f.read_bytes(); pe=pefile.PE(data=d); sec=next(s for s in pe.sections if s.Name.startswith(b'.debug'))
base=sec.PointerToRawData+32; off=struct.unpack_from('<I',d,base+4)[0];count=struct.unpack_from('<I',d,base+off+4)[0]
entries=[struct.unpack_from('<HHII',d,base+off+16+12*i) for i in range(count)]
_,_,lfo,cb=next(x for x in entries if x[0]==0x130)
p=base+lfo; n=struct.unpack_from('<I',d,p)[0];p+=4
names=['']
for i in range(n):
    ln=d[p];p+=1;names.append(d[p:p+ln].decode('cp1251',errors='replace'));p+=ln+1
print('names',len(names), 'lastposition',hex(p))
target={i:s for i,s in enumerate(names) if re.search(r'@Communicationclassindex@TComm2ES6@|@Communicationbasisclass@TCommunicationBasisClass@|@Structurehardwarexe2@THardwares@',s,re.I)}
procs=[]
for typ,mod,lfo,cb in entries:
    if typ!=0x125:continue
    p=base+lfo+4;end=base+lfo+cb
    while p+4<=end:
        ln,rect=struct.unpack_from('<HH',d,p)
        if ln<2:break
        raw=d[p:p+ln+2]
        if rect in (0x204,0x205):
            dw=struct.unpack_from('<'+str((len(raw)-4)//4)+'I',raw,4)
            if len(dw)>=10 and 0<dw[9]<len(names) and dw[7]==1:
                name=names[dw[9]]
                procs.append({'name':name,'offset':dw[6],'rva':pe.sections[0].VirtualAddress+dw[6],'length':dw[3],'module':mod,'record_offset':p})
        p+=ln+2
(Path(__file__).parent/'Communication1.procs.json').write_text(json.dumps(procs,ensure_ascii=False,indent=2),encoding='utf8')
by_addr={x['rva']:x['name'] for x in procs}
for x in procs:
    if x['name'] not in target.values(): continue
    if 'TComm2ES6' not in x['name'] and '@Proceed$' not in x['name'] and '@InitPultDll$' not in x['name']:continue
    print('\nPROC',x['name'], 'rva',hex(x['rva']),'len',hex(x['length']))
    code=d[pe.get_offset_from_rva(x['rva']):pe.get_offset_from_rva(x['rva'])+x['length']]
    for i in range(len(code)-4):
        if code[i]==0xe8:
            to=x['rva']+i+5+struct.unpack_from('<i',code,i+1)[0]
            if to in by_addr:
                print(hex(x['rva']+i),'CALL',by_addr[to])
    if '@UpdateLightsHW$' in x['name'] or '@UpdatePriborsHW$' in x['name']:
        print('hex',code.hex())
