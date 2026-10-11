from pathlib import Path
import csv,json,math,hashlib
folder=Path(__file__).resolve().parent
def rows(kind,tag):
    lines=(folder/f'probe_{kind}_Tyga1.{tag}.csv').read_text(encoding='utf8').splitlines()
    if lines[0].startswith('init='):lines=lines[1:]
    return list(csv.DictReader([x for x in lines if not x.startswith('before,')]))
report={'dll_sha256':hashlib.sha256((folder/'Tyga1.patched.dll').read_bytes()).hexdigest(),'native_velocity_units':'signed metres/second; displayed speed_kmh converted by /3.6f'}
assert report['dll_sha256']==json.loads((folder/'patch_manifest.json').read_text())['patched_sha256']
for kind in ('valid','partial','validstrict'):
    a,b=rows(kind,'original'),rows(kind,'patched')
    assert len(a)==len(b)==21
    assert a==b
    assert all(not x['exception'] for x in b)
    report[kind]={'native_cases':len(b),'unchanged':True}
for kind in ('zero','zerostrict','percentzerostrict','indepzero'):
    b=rows(kind,'patched')
    assert len(b)==21
    assert all(not x['exception'] and all(float(x[v])==0 for v in ('I_return','force10kN','Ia','percent')) for x in b)
    report[kind]={'native_cases':len(b),'zero_force_current_percent':True,'exceptions':0}
for kind in ('zerostrict','percentzerostrict'):
    a=rows(kind,'original')
    assert all(x['exception'] for x in a)
    report[kind]['original_exceptions']=len(a)
for kind in ('indep','indeppartial','indepstrict'):
    a,b=rows(kind,'original'),rows(kind,'patched')
    assert len(a)==len(b)==21
    below=0;limited=0
    for old,new in zip(a,b):
        assert not new['exception']
        assert all(math.isfinite(float(new[k])) for k in ('I_return','force10kN','Ia','percent'))
        assert 0<=float(new['Ia'])<=750
        ia=float(old['Ia'])
        if ia<=750:
            assert old==new
            below+=1
        elif ia>750:
            assert float(new['Ia'])==750
            # With identical excitation all active pairs have identical moment.
            # The finite output must track the same capped current as the display.
            assert abs(float(new['force10kN'])/float(old['force10kN'])-750/ia)<2e-6
            limited+=1
    report[kind]={'native_cases':21,'below750_unchanged':below,'above750_current_and_force_consistent':limited}
for kind in ('cyclestrict','cyclezerostrict','cyclereversestrict','cyclezeroreversestrict'):
    a,b=rows(kind,'original'),rows(kind,'patched')
    assert len(a)==len(b)==72
    assert all(not x['exception'] and int(x['calls'])==100 and all(math.isfinite(float(x[k])) for k in ('force10kN','Ia','Iexc')) for x in b)
    if 'zero' not in kind:
        unchanged=sum(1 for old,new in zip(a,b) if old['case'] in ('coast','recup') and old==new)
        assert unchanged==48
        assert all(float(x['force10kN'])==0 for x in b if x['case']=='coast')
        assert all(float(x['force10kN'])*int(x['revers'])<=0 for x in b if x['case']=='recup')
        report[kind]={'full_native_scenarios':72,'native_calls':7200,'coast_and_recup_exactly_unchanged':48,'exceptions':0}
    else:
        assert all(float(x['force10kN'])==0 and float(x['Ia'])==0 for x in b)
        report[kind]={'full_native_scenarios':72,'native_calls':7200,'all_zero_active_motor_force_current':True,'exceptions':0,'original_exception_scenarios':sum(bool(x['exception']) for x in a)}
forward=rows('cyclestrict','patched')
reverse=rows('cyclereversestrict','patched')
for a,b in zip(forward,reverse):
    assert a['case']==b['case'] and a['mode']==b['mode'] and a['speed_kmh']==b['speed_kmh']
    assert float(a['force10kN'])==-float(b['force10kN'])
    assert a['Ia']==b['Ia'] and a['Iexc']==b['Iexc'] and a['percent']==b['percent']
    assert float(a['input_v_mps'])==-float(b['input_v_mps'])
    assert a['local_v_mps']==b['local_v_mps']
report['forward_reverse_symmetry']={'full_native_scenarios':72,'opposite_force_and_input_V':True,'same_motor_currents_excitation_percent_local_V':True}
wrong=rows('cyclewrongreversestrict','patched')
differences=[]
for good,bad in zip(reverse,wrong):
    if float(good['speed_kmh'])>0 and (good['Ia']!=bad['Ia'] or good['Iexc']!=bad['Iexc'] or good['force10kN']!=bad['force10kN'] or bad['exception']):
        differences.append({'case':good['case'],'connection':good['mode'],'speed_kmh':good['speed_kmh'],'correct_force10kN':good['force10kN'],'wrong_abs_V_force10kN':bad['force10kN'],'correct_Ia':good['Ia'],'wrong_abs_V_Ia':bad['Ia'],'correct_Iexc':good['Iexc'],'wrong_abs_V_Iexc':bad['Iexc'],'wrong_exception':bad['exception']})
assert differences
report['wrong_reverse_abs_V']={'different_native_scenarios':len(differences),'exceptions':sum(bool(x['exception']) for x in wrong),'examples':differences}
report['totals']={'direct_native_cases':210,'correct_full_native_scenarios':288,'correct_full_native_calls':28800,'wrong_reverse_diagnostic_scenarios':72,'wrong_reverse_diagnostic_native_calls':7200,'coast_and_recup_exactly_unchanged_cases':96,'independent_below_750_unchanged':sum(report[k]['below750_unchanged'] for k in ('indep','indeppartial','indepstrict')),'independent_above_750_force_consistent':sum(report[k]['above750_current_and_force_consistent'] for k in ('indep','indeppartial','indepstrict'))}
(folder/'traction_validation.json').write_text(json.dumps(report,indent=2),encoding='utf8')
print(json.dumps(report,indent=2))
