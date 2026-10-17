from pathlib import Path
import sys, json
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import Cs, CS_ARCH_X86, CS_MODE_32
sys.stdout.reconfigure(encoding='utf-8')
out=Path(__file__).parent
cases=[('main_constructor',Path('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe'),0x222cfc,0x430),('main_poezd_caller',Path('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe'),0x329380,0x270),('poezd_resistance',Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/POEZD.DLL'),0x1ee8,0x100),('poezd_exports',Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/POEZD.DLL'),0x22cc,0x370)]
md=Cs(CS_ARCH_X86,CS_MODE_32);md.skipdata=True
for name,path,rva,length in cases:
    p=pefile.PE(str(path));base=p.OPTIONAL_HEADER.ImageBase
    ins=list(md.disasm(p.get_data(rva,length),base+rva))
    (out/(name+'.asm')).write_text('\n'.join('%08x %-8s %s'%(i.address,i.mnemonic,i.op_str) for i in ins),encoding='utf-8')
    print(name,'bytes',length,'instructions',len(ins))
