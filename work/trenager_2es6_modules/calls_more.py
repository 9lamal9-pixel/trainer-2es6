from pathlib import Path
import json,pefile,struct,re,sys
sys.stdout.reconfigure(encoding='utf8',errors='replace')
out=Path(__file__).parent
f=Path(r'C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Communication1.dll')
d=f.read_bytes();pe=pefile.PE(data=d);base=pe.OPTIONAL_HEADER.ImageBase
procs=json.loads((out/'Communication1.procs.json').read_text(encoding='utf8'));by_addr={x['rva']:x['name'] for x in procs}
for method in ['@UpdatePultHW$','@UpdatePriborsHW$','@UpdateLightsHW$']:
    x=next(x for x in procs if '@Communicationclassindex@TComm2ES6' in x['name'] and method in x['name'])
    needle=struct.pack('<I',base+x['rva']);p=0
    print('\nPOINTERS TO',method)
    while True:
        p=d.find(needle,p)
        if p<0 or p>0x13f1a00:break
        print('at',hex(p),'va',hex(base+pe.get_rva_from_offset(p)))
        for off in range(p-32,p+40,4):
            ptr=struct.unpack_from('<I',d,off)[0]
            print(hex(off),hex(ptr),by_addr.get(ptr-base,''))
        p+=4
for x in procs:
    if any(q in x['name'] for q in ['@Updatepultdllclass@TPultDLLClass@ProceedPultDll$','@Communication1@ExecuteCommunication$','@Communicationbasisclass@TCommunicationBasisClass@Proceed$']):
        code=d[pe.get_offset_from_rva(x['rva']):pe.get_offset_from_rva(x['rva'])+x['length']]
        print('\nRAW',x['name'],hex(x['rva']),code.hex())
