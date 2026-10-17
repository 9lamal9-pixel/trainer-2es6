from pathlib import Path
import json,sys,hashlib,struct
sys.stdout.reconfigure(encoding='utf-8')
sys.path.insert(0,str(Path('work/traction6000/vendor').resolve()))
import pefile
from capstone import Cs,CS_ARCH_X86,CS_MODE_32,CS_OP_MEM
out=Path(__file__).parent
names=['TLocParam','TPnevmo','TPnevmoSend','TPositionLoc','TBlocked367','TEPK_Param','TEPKPos','TEPKSend','TVagonParam','TVagonSend','TPribors_2ES6','TPult']
records={}
for src in ['work/main_rtti.json','work/klub_live_fix/evidence/Communication1.dll.rtti.json','work/klub_live_fix/evidence/Pult1.dll.rtti.json','work/airbrake6000/TormModel.rtti.json']:
    records[src]=[r for r in json.loads(Path(src).read_text(encoding='utf-8')) if r['name'] in names]
(out/'selected_pressure_records.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
src=Path('work/brake6000/main_disasm/LocoConstructor.txt')
lines=src.read_text(encoding='utf-8').splitlines()
(out/'original_loco_defaults_excerpt.txt').write_text('\n'.join(lines[0:5]+lines[30:178])+'\n',encoding='utf-8')
md=Cs(CS_ARCH_X86,CS_MODE_32);md.detail=True
report=[]
for name in ['Communication1.dll','Pult1.dll']:
    f=Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1')/name
    pe=pefile.PE(str(f));base=pe.OPTIONAL_HEADER.ImageBase
    item={'file':str(f),'sha256':hashlib.sha256(f.read_bytes()).hexdigest(),'writes_disp_91':[],'native_cock_named_procs':[]}
    for p in json.loads(Path('work/compare137_modules',name+'.procs.json').read_text(encoding='utf-8')):
        if any(k in p['name'].lower() for k in ['komby','blocked367','bloked367','kran367']): item['native_cock_named_procs'].append(p)
        if p['length']>50000: continue
        for ins in md.disasm(pe.get_data(p['rva'],p['length']),base+p['rva']):
            if ins.mnemonic.startswith('mov') and ins.operands and ins.operands[0].type==CS_OP_MEM and ins.operands[0].mem.disp==0x91:
                item['writes_disp_91'].append({'proc':p['name'],'rva':hex(ins.address-base),'bytes':ins.bytes.hex(),'asm':ins.mnemonic+' '+ins.op_str})
    if name=='Communication1.dll':
        d=pe.get_data(0x2b1ee4,10)
        mant,expo=struct.unpack('<QH',d)
        item['tc2_subtraction_extended']={'rva':'0x2b1ee4','bytes':d.hex(),'value':mant/(1<<63)*2.0**((expo&0x7fff)-16383)*(-1 if expo&0x8000 else 1)}
    report.append(item)
(out/'code_reference_scan.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
for r in report: print(r['file'],'KK direct disp91 writes',len(r['writes_disp_91']),r['writes_disp_91'],r.get('tc2_subtraction_extended'))
