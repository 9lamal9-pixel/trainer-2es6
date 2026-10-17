from pathlib import Path
import sys, struct, json
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import Cs, CS_ARCH_X86, CS_MODE_32
sys.stdout.reconfigure(encoding='utf-8')
out=Path(__file__).parent
target=Path('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe')
binary=target.read_bytes();pe=pefile.PE(str(target));base=pe.OPTIONAL_HEADER.ImageBase
md=Cs(CS_ARCH_X86,CS_MODE_32);md.skipdata=True
all_info=[]
for text in ['KLUBUTYPE','BLOKPB','KLUB_U_PB','БИЛ-УТ','БИЛ-УП']:
    start=0
    while True:
        at=binary.find(text.encode('utf-16le'),start)
        if at<0:break
        start=at+1;va=base+pe.get_rva_from_offset(at)
        xrefs=[];x=0
        while True:
            q=binary.find(struct.pack('<I',va),x)
            if q<0:break
            x=q+1
            rva=pe.get_rva_from_offset(q)
            if rva>=0x590000:continue
            xrefs.append(hex(base+rva))
            begin=max(0x1000,rva-90)
            ins=list(md.disasm(pe.get_data(begin,260),base+begin))
            (out/('xref_%s_%x.asm'%(text.replace('-','_'),rva))).write_text('\n'.join('%08x %-8s %s'%(i.address,i.mnemonic,i.op_str) for i in ins),encoding='utf-8')
        all_info.append({'text':text,'string_va':hex(va),'xrefs':xrefs})
(out/'native_original_config_xrefs.json').write_text(json.dumps(all_info,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(all_info,ensure_ascii=False,indent=2))
