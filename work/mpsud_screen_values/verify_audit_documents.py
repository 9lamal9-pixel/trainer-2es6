import json, re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
main = root/'dev/docs/MPSUID_SCREENS_RE_AUDIT.md'
text = main.read_text(encoding='utf-8-sig')
links = re.findall(r'\]\((C:/[^)]+)\)', text)
missing = [s for s in links if not Path(s).is_file()]
runtime = json.loads((root/'dev/docs/audit_parts/MPSUD_SCREEN_RUNTIME.json').read_text(encoding='utf-8-sig'))
verified = json.loads((root/'dev/docs/audit_parts/MPSUD_SCREEN_RUNTIME_CHECK.json').read_text(encoding='utf-8-sig'))
production = json.loads((root/'dev/docs/audit_parts/MPSUD_SCREEN_PRODUCTION_CHECK.json').read_text(encoding='utf-8-sig'))
catalog = json.loads((root/'dev/config/probes.json').read_text(encoding='utf-8-sig'))
ids = [p['id'] for p in catalog['probes']]
arrow = next(p for p in catalog['probes'] if p['id']=='MpsudDiagnosticArrows')
checks = {
    'all_local_links_exist': not missing,
    'runtime_coverage_verified': verified['passed'] and verified['counts']['screen_routes_verified']==34 and verified['counts']['extra_view_routes_verified']==8,
    'counts_match': runtime['captures']==verified['counts']['captures'] and runtime['runtime_assertions']+runtime['observations']==verified['counts']['checks'],
    'normative_not_overstated': not runtime['normative_mpsuid_accepted'] and not verified['normative_mpsuid_accepted'],
    'production_unchanged': production['passed'] and len(production['sources'])==16,
    'arrow_not_acceptance': not arrow['enabled'] and arrow['unsafe'],
    'catalog_ids_unique': len(ids)==len(set(ids)),
    'full_view_table': len(re.findall(r'^\| \d\d \|',text,re.M))==21,
    'failed_run_retained': (root/runtime['failed_arrow_run']/'stdout.log').is_file(),
}
result={'passed':all(checks.values()),'checks':checks,'local_links':len(links),'missing':missing}
(root/'dev/docs/audit_parts/MPSUD_SCREEN_DOCUMENT_CHECK.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
raise SystemExit(0 if result['passed'] else 1)
