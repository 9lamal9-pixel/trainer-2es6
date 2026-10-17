from pathlib import Path
import hashlib
import json
import re
from datetime import datetime, timezone

root = Path(__file__).resolve().parents[2]
target = root / 'dev/docs/audit_parts/MPSUD_SCREEN_COMPARISON.md'
document = target.read_text(encoding='utf-8')
mapping = json.loads((root / 'dev/docs/audit_parts/MPSUD_SCREEN_MAP.json').read_text(encoding='utf-8'))
fields = json.loads((root / 'dev/docs/audit_parts/MPSUD_SCREEN_FIELDS.json').read_text(encoding='utf-8'))
checks = {}

rows = [line for line in document.splitlines() if re.match(r'\| SR\d+ \|', line)]
checks['all_22_normative_ids'] = [re.match(r'\| (SR\d+) \|', line).group(1) for line in rows] == [f'SR{i:02d}' for i in range(22)]
checks['all_15_findings_present'] = all(f'### SC{i:02d} ' in document for i in range(1, 16))
windows = {item['enum']: item for item in mapping['windows']}
checks['factory_null_19_20_21'] = all(not windows[n]['factory_supported'] and windows[n]['frame_class'] is None for n in [19, 20, 21])
checks['clock_digit5_saut'] = windows[13]['path_digits_from_clock'] == [5] and windows[13]['frame_class'] == 'TwtSautFrame'
checks['stpr_has_four_published_grids'] = len([f for f in fields['TwtSTPRFrame']['published_fields'] if f['class'] == 'TDrawGrid']) == 4
checks['source_and_runtime_limits_explicit'] = all(s in document for s in ['SOURCE_GAP', 'NOT_EXECUTED', 'REVISION_DEPENDENT', 'PENDING_ROOT', 'Статический результат не является runtime PASS'])
checks['source_files_exist'] = all((root / name).is_file() for name in [
    'work/mpsud_screen_values/decode_manifest.json',
    'work/mpsud_screen_values/TwtMashinistFrame_ProceedCur.asm',
    'work/mpsud_screen_values/TwtCIFrame_UpdateData.asm',
    'work/mpsud_screen_values/TwtAutovedFrame_ProceedFrame.asm',
    'work/mpsud_screen_values/TwtSMErame_UpdateData.asm',
])
status = 'PASS' if all(checks.values()) else 'FAIL'
result = {
    'created_at': datetime.now(timezone.utc).isoformat(),
    'status': status,
    'kind': 'document_and_published_map_consistency_only',
    'native_run_executed': False,
    'runtime_screen_accepted': False,
    'production_modified': False,
    'document': str(target),
    'document_sha256': hashlib.sha256(target.read_bytes()).hexdigest().upper(),
    'prior_check_attempt': {
        'status': 'FAIL',
        'cause': 'validator initially included the Markdown header SR as a data row',
        'resolution': 'restricted selection to SR followed by digits; no document or production change required',
    },
    'checks': checks,
}
destination = root / 'dev/docs/audit_parts/MPSUD_SCREEN_COMPARISON_CHECK.json'
destination.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'{status}: {sum(checks.values())}/{len(checks)} document/map assertions; no native execution or normative acceptance.')
raise SystemExit(0 if status == 'PASS' else 1)
