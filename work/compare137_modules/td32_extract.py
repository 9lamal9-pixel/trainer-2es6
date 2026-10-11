from pathlib import Path
import pefile,struct,json,sys
f=Path(sys.argv[1]);d=f.read_bytes();pe=pefile.PE(data=d)
sec=next(s for s in pe.sections if s.Name.startswith(b'.debug'))
base=sec.PointerToRawData+32;off=struct.unpack_from('<I',d,base+4)[0]
count=struct.unpack_from('<I',d,base+off+4)[0]
entries=[struct.unpack_from('<HHII',d,base+off+16+12*i) for i in range(count)]
_,_,lfo,cb=next(x for x in entries if x[0]==0x130)
p=base+lfo;n=struct.unpack_from('<I',d,p)[0];p+=4;names=['']
for _ in range(n):
    ln=d[p];p+=1;names.append(d[p:p+ln].decode('cp1251',errors='replace'));p+=ln+1
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
                procs.append({'name':names[dw[9]],'offset':dw[6],'rva':pe.sections[0].VirtualAddress+dw[6],'length':dw[3],'module':mod,'record_offset':p})
        p+=ln+2
out=Path(__file__).parent/(f.name+'.procs.json')
out.write_text(json.dumps(procs,ensure_ascii=False,indent=2),encoding='utf8')
print(out,len(procs))
if len(sys.argv)>2:
    by_addr={x['rva']:x['name'] for x in procs}
    for x in procs:
        if sys.argv[2].lower() not in x['name'].lower():continue
        print(x)
        o=pe.get_offset_from_rva(x['rva']);code=d[o:o+x['length']]
        print(code.hex())
        for i in range(len(code)-4):
            if code[i]!=0xe8:continue
            to=x['rva']+i+5+struct.unpack_from('<i',code,i+1)[0]
            if to in by_addr:print(hex(x['rva']+i),'CALL',by_addr[to])
