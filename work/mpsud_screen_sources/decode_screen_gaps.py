from pathlib import Path
import hashlib
import json
import sys
from datetime import datetime, timezone

root = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(root / 'work/traction6000/vendor'))
import pefile
from capstone import Cs, CS_ARCH_X86, CS_MODE_32

dll = root / 'work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/MSUL_MON.dll'
sha = hashlib.sha256(dll.read_bytes()).hexdigest().upper()
assert sha == '95E3FB14CBC7FF2B5E9C61DC2215CFA7C2F23161FE21D075ADF04F45907B653B'
pe = pefile.PE(str(dll), fast_load=True)
procs = json.loads((root / 'work/msul_emulator/display_reference/MSUL_MON.dll.procs.json').read_text(encoding='utf-8'))
targets = [
    ('TwtSTPRFrame', 'Init'),
    ('TwtSTPRFrame', 'DrawGrid2DrawCell'),
    ('TwtSTPRFrame', 'UpdateData'),
    ('TwtSTPRFrame', 'ProceedFrame'),
    ('TwtRN3000Frame', 'UpdateData'),
    ('TwtPCConnectFrame', 'UpdateData'),
    ('TwtVspomMashineFrame', 'DrawGrid1DrawCell'),
    ('TwtVspomMashineFrame', 'UpdateData'),
    ('TwtVspomMashineFrame', 'GetNameCels'),
    ('TwtVspomMashineFrame', 'UpdateRezhimCepi'),
    ('TwtVspomMashineFrame', 'UpdateUpravCepi'),
    ('TwtRN3000Frame', 'DrawGrid1DrawCell'),
    ('TwtPCConnectFrame', 'DrawGrid1DrawCell'),
    ('TwtRN3000Frame', 'DrawGrid2DrawCell'),
    ('TwtRN3000Frame', 'Init'),
    ('TwtRN3000Frame', 'ProceedFrame'),
    ('TwtRN3000Frame', 'SetView'),
    ('TwtPCConnectFrame', 'DrawGrid2DrawCell'),
    ('TwtPCConnectFrame', 'DrawGrid3DrawCell'),
    ('TwtPCConnectFrame', 'DrawGrid4DrawCell'),
    ('TwtPCConnectFrame', 'Init'),
    ('TwtPCConnectFrame', 'ProceedFrame'),
    ('TwtPCConnectFrame', 'SetView'),
    ('TwtRN3000Window', 'Proceed'),
    ('TwtRN3000Window', 'SetView'),
    ('TwtPSConnectWindow', 'Proceed'),
    ('TwtPSConnectWindow', 'SetView'),
    ('TwtRN3000Window', '$bctr'),
    ('TwtPSConnectWindow', '$bctr'),
    ('TWindowWizard', 'Proceed'),
]
folder = root / 'work/mpsud_screen_sources/static_gaps'
folder.mkdir(exist_ok=True)
decoder = Cs(CS_ARCH_X86, CS_MODE_32)
result = {'created_at': datetime.now(timezone.utc).isoformat(), 'dll_sha256': sha, 'kind': 'read_only_selected_current_PE_disassembly', 'native_executed': False, 'procedures': []}
for cls, method in targets:
    proc = next(p for p in procs if f'@{cls}@{method}$' in p['name'])
    code = pe.get_data(proc['rva'], proc['length'])
    out = folder / f'{cls}_{method}.asm'
    lines = [f"; {proc['name']}", f"; RVA={proc['rva']:X}, bytes={len(code)}, currentDLL={sha}"]
    lines.extend(f'{i.address:08X} {i.bytes.hex():20} {i.mnemonic:8} {i.op_str}' for i in decoder.disasm(code, proc['rva']))
    out.write_text('\n'.join(lines)+'\n', encoding='utf-8')
    result['procedures'].append({'class': cls, 'method': method, 'rva': hex(proc['rva']), 'length': proc['length'], 'code_sha256': hashlib.sha256(code).hexdigest().upper(), 'path': str(out)})
    print(cls, method, hex(proc['rva']), proc['length'])
(folder / 'manifest.json').write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
