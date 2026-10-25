"""Independent Capstone/control-flow review of emitted bytes, not CPU execution."""
from pathlib import Path
import json,hashlib
import capstone
from capstone.x86 import X86_OP_IMM,X86_OP_MEM,X86_REG_EDI
folder=Path(__file__).parent
bundle=folder/'managed-caves/generated-caves.json'
g=json.loads(bundle.read_text(encoding='utf-8-sig'))
source=folder/'TrainerContactVoltagePatch.cs';source_sha=hashlib.sha256(source.read_bytes()).hexdigest()
assert source_sha=='267d313214901309b98a6a0be00fd784d75c25def0dc2a473801009654dfae57'
assert g['source_sha256'].lower()==source_sha
md=capstone.Cs(capstone.CS_ARCH_X86,capstone.CS_MODE_32);md.detail=True
rows=[]
for site in g['sites']:
    data=(folder/'managed-caves'/(site['name']+'.bin')).read_bytes()
    assert data.hex().lower()==site['hex'].lower()
    assert hashlib.sha256(data).hexdigest()==site['binary_sha256'].lower()
    instructions=list(md.disasm(data,site['cave']));lookup={i.address:i for i in instructions};end=site['cave']+len(data)
    assert sum(i.size for i in instructions)==len(data)
    assert all(i.mnemonic not in ['call','ret','retf','int','int3','sysenter'] and not i.mnemonic.startswith('f') for i in instructions)
    assert instructions[-1].mnemonic=='jmp' and instructions[-1].operands[0].imm==site['continuation']
    for i in instructions:
        if i.group(capstone.CS_GRP_JUMP) and i.address!=instructions[-1].address:assert i.operands[0].type==X86_OP_IMM and i.operands[0].imm in lookup
    physical_stores=[i for i in instructions if i.operands and i.operands[0].type==X86_OP_MEM and i.operands[0].mem.base==X86_REG_EDI and i.operands[0].access&capstone.CS_AC_WRITE]
    assert len(physical_stores)==1 and physical_stores[0].mnemonic=='mov' and physical_stores[0].operands[0].mem.disp==site['member_offset']
    starts=list(lookup);paths=[]
    def walk(at,stack,visited):
        assert at not in visited
        i=lookup[at];depth=stack
        if i.mnemonic=='pushfd':depth-=4
        elif i.mnemonic=='pushal':depth-=32
        elif i.mnemonic=='popal':depth+=32
        elif i.mnemonic=='popfd':depth+=4
        assert depth<=0
        if i.address==instructions[-1].address:
            assert depth==0;paths.append(len(visited)+1);return
        if i.mnemonic=='jmp':walk(i.operands[0].imm,depth,visited|{at})
        elif i.group(capstone.CS_GRP_JUMP):
            walk(i.operands[0].imm,depth,visited|{at});walk(i.address+i.size,depth,visited|{at})
        else:walk(i.address+i.size,depth,visited|{at})
    walk(site['cave'],0,set())
    assert data.endswith(b'\x61\x9d'+instructions[-1].bytes)
    if site['sequential']:
        assert data.startswith(bytes.fromhex('8b45fc8b40049c60'))
        assert bytes.fromhex('807816010f87') in data
        assert bytes.fromhex('80ba00010000000f85') in data
        assert bytes.fromhex('80b830010000000f850200000033c9') in data
    else:assert data.startswith(bytes.fromhex('9c60'))
    rows.append({'site':site['name'],'sha256':hashlib.sha256(data).hexdigest(),'length':len(data),'decoded_bytes':sum(i.size for i in instructions),'instruction_count':len(instructions),'restoring_CFG_paths':len(paths),'continuation':hex(site['continuation']),'physical_store':{'address':hex(physical_stores[0].address),'asm':physical_stores[0].mnemonic+' '+physical_stores[0].op_str},'passed':True})
report={'kind':'independent_emitted_cave_CFG_and_ABI_review','source_sha256':source_sha,'native_executed':False,'native_DLL_loaded':False,'CPU_emulated':False,'passed':True,'sites':rows,'scope':'Each branch path balances pushfd/pushad and popad/popfd, reaches the exact continuation; no CALL/x87/SSE or extra physical output stores. TractionEntry preserves original MOV result and limits early publication to excitation0/1 and FFlagRecupfalse; original panto false produces float0. Runtime behavior, scheduling, native exceptions, protection restoration and actual component/operator acceptance remain untested by this static review.','probe_review':{'source':'work/pneumatic_live_fix/analysis/ContactNetworkVoltageProbe.cs','sha256_at_review':'900b880b1196cedf3c7251264ac6d1a95193c7ecafa5c4dd7197fe1f11ba1add','EDT_ABI':'Three native setters, complete CalcRecup and setter restoration order agree with fresh PE. No isolated MOV invocation.','sequential_limit':'This revision enters the sequential switch phase after genuine start has already returned to coast; entrycounter plus VozbType0 does not prove actual GetCurPosl reach. Root notified; genuine controller widget is form+41C TMultiPosTumbler.'}}
target=folder/'independent-cave-review.json';target.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'path':target.as_posix(),'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'passed':True,'sites':len(rows),'CFG_paths':sum(r['restoring_CFG_paths'] for r in rows)},ensure_ascii=False))
