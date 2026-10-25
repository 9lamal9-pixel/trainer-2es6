"""Independent literal-guard comparison; no native library loading."""
from pathlib import Path
import re,json,hashlib
import pefile
root=Path('.');out=Path(__file__).parent
source=out/'TrainerContactVoltagePatch.cs';text=source.read_text(encoding='utf-8-sig');source_sha=hashlib.sha256(source.read_bytes()).hexdigest()
assert source_sha=='267d313214901309b98a6a0be00fd784d75c25def0dc2a473801009654dfae57'
folder=root/'work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1'
modules={name:pefile.PE(str(folder/(name+'.dll'))) for name in ['Pult1','Tyga1']}
checks=[]
pattern=r'Make\("([^"]+)",(\d+),(pultModule|tygaModule),\w+,\w+,(0x[0-9a-f]+),(0x[0-9a-f]+),(0x[0-9a-f]+),(true|false),(true|false),"([0-9a-f]+)","([0-9a-f]+)"\)'
for match in re.finditer(pattern,text):
    name,index,module,rva,window,member,floating,entry,original,window_bytes=match.groups();pe=modules['Pult1' if module=='pultModule' else 'Tyga1']
    for label,at,data in [('entry',int(rva,16),bytes.fromhex(original)),('full_window',int(window,16),bytes.fromhex(window_bytes))]:
        actual=pe.get_data(at,len(data));checks.append({'site':name,'kind':label,'rva':hex(at),'length':len(data),'passed':actual==data,'sha256':hashlib.sha256(actual).hexdigest()})
retained=re.search(r'const string RetainedFixHex = "([0-9a-f]+)"',text).group(1)
data=bytes.fromhex(retained);checks.append({'site':'retained_tfix','length':len(data),'passed':len(data)==158 and modules['Tyga1'].get_data(0x856000,158)==data,'sha256':hashlib.sha256(data).hexdigest()})
assert len(checks)==11 and all(x['passed'] for x in checks)
report={'kind':'independent_fresh_PE_guard_comparison','native_executed':False,'source':source.as_posix(),'source_sha256':source_sha,'passed':True,'checks':checks,'entry_native_effect':'MOV EAX,[EBP-4]; MOV EAX,[EAX+4], no flags/FPU change before save; must restore EAX=TTygaParam at continuation17C352.','source_review':'5site counter/destination/value/generation arrays do not overlap within128bytes; ordinary MOV caves preserve all GP/flags, and cleanup does not depend on operational Fault validation.','remaining':'Emitted cave bytes and dynamic branch/lifecycle acceptance are separate evidence. Whole physical roof equality during recuperation remains out of accepted scope.'}
target=out/'independent-guard-review.json';target.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'path':target.as_posix(),'passed':True,'checks':len(checks),'sha256':hashlib.sha256(target.read_bytes()).hexdigest()}))
