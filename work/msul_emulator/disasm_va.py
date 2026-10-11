import sys,json
from pathlib import Path
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import *
md=Cs(CS_ARCH_X86,CS_MODE_32)
f=Path(sys.argv[1]);p=pefile.PE(str(f),fast_load=True)
for arg in sys.argv[2:]:
 a,n=arg.split(':');r=int(a,0);n=int(n,0);ins=list(md.disasm(p.get_data(r,n),0x400000+r));lines=[f'{i.address:08x} {i.mnemonic:8} {i.op_str}' for i in ins]
 out=Path('work/msul_emulator')/(f.stem+'_'+hex(r)+'.asm');out.write_text('\n'.join(lines),encoding='utf8');print(out)
