from pathlib import Path
import json, sys
import pefile
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
from capstone import Cs, CS_ARCH_X86, CS_MODE_32
sys.stdout.reconfigure(encoding='utf-8')
out=Path(__file__).parent/'reverse_check';out.mkdir(exist_ok=True)
root=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1')
md=Cs(CS_ARCH_X86,CS_MODE_32);md.skipdata=True
for name in ['Communication1.dll','Tyga1.dll']:
    pe=pefile.PE(str(root/name));base=pe.OPTIONAL_HEADER.ImageBase
    procs=json.loads(Path('work/compare137_modules',name+'.procs.json').read_text(encoding='utf-8'))
    selected=[p for p in procs if any(t in p['name'] for t in ['TCommunicationBasisClass@SetCurV','TCommunicationBasisClass@Proceed$','TComm2ES6@Proceed$','ExecuteCommunication$','SetVCur','SetVcur'])]
    for p in selected:
        tag=p['name'].split('@')[-1].replace('$','_')
        if 'TCommunicationBasisClass' in p['name']:tag='Basis_'+tag
        if 'TComm2ES6' in p['name']:tag='2ES6_'+tag
        ins=list(md.disasm(pe.get_data(p['rva'],p['length']),base+p['rva']))
        (out/(name+'_'+tag+'.asm')).write_text(json.dumps(p)+'\n'+'\n'.join('%08x %-8s %s'%(i.address,i.mnemonic,i.op_str) for i in ins),encoding='utf-8')
        print(name,p['name'],hex(p['rva']),p['length'])
pe=pefile.PE('work/trenager_static/WORK_TRENAGER_3D/Trenager_3D.exe');base=pe.OPTIONAL_HEADER.ImageBase
for name,rva,length in [('SetV_223ca4',0x223ca4,0x80),('Loco_getV_parameter',0x223b60,0x60),('GetTyga_record',0x296a7c,0x30),('Loco_velocity_2203a6',0x220378,0x100),('Wagon_velocity_22f322',0x22f2f0,0x90),('Velocity_282bcf',0x282b98,0x120),('Velocity_322921',0x3228e0,0x160),('Possible_InputV_226571',0x226500,0x100),('Loco_parameters',0x21f920,0x340),('Loco_signed_InputV',0x21feb0,0x220),('Main_wrapper_InputV',0x2146bc,0x150),('Main_wrapper_safetyV',0x214934,0x110),('TTrain_ProceedTTrain',0x28a3b4,0x294),('TTrain_CopyInput',0x289d90,0x200),('GetVRecord_caller_3e2012',0x3e1f90,0x150),('Main_velocity_send_A',0x3def80,0x200),('Main_velocity_send_B',0x3e4f00,0x200),('Main_SetDisplayV',0x3e3e90,0xc0)]:
    srcpe=pe if name!='GetTyga_record' else pefile.PE(str(root/'Communication1.dll'))
    srcbase=srcpe.OPTIONAL_HEADER.ImageBase
    ins=list(md.disasm(srcpe.get_data(rva,length),srcbase+rva))
    (out/(name+'.asm')).write_text('\n'.join('%08x %-8s %s'%(i.address,i.mnemonic,i.op_str) for i in ins),encoding='utf-8')
