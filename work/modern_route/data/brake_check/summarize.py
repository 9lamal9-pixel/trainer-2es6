import csv,json,sys
from pathlib import Path
sys.stdout.reconfigure(encoding='utf-8')
root=Path(__file__).parent
matrix=json.loads((root/'matrix_exit_codes.json').read_text(encoding='utf-8-sig'))
rows=[json.loads((root/(m['Name']+'.json')).read_text(encoding='utf-8')) for m in matrix]
fields=['case','pad_index','grade_permille','tc_pressure_input','initial_speed_kmh','first_step_a_m_s2','first_below015_kmh_seconds','last_head_v_kmh','last_head_x_m','negative_head_steps','head_velocity_sign_changes','all_76_outputs_finite']
with (root/'matrix_summary.csv').open('w',encoding='utf-8',newline='') as f:
    writer=csv.DictWriter(f,fieldnames=fields,extrasaction='ignore');writer.writeheader();writer.writerows(rows)
summary={'cases':len(rows),'duration_seconds_per_case':120,'all_cases_finite':all(r.get('all_76_outputs_finite',False) for r in rows),'all_exit_codes_zero':all(m['ExitCode']==0 for m in matrix),'corrected_cases':sum(r['pad_index']in(0,1) for r in rows),'corrected_cases_negative_velocity':sum(r['negative_head_steps']>0 for r in rows if r['pad_index']in(0,1)),'corrected_cases_sign_changes':sum(r['head_velocity_sign_changes']>0 for r in rows if r['pad_index']in(0,1)),'static_cases_with_motion':sum(r['max_abs_head_v_last30sec_kmh']>.001 for r in rows if r['initial_speed_kmh']==0),'old_wrong_p3':next(r for r in rows if r['case']=='old_wrong_pad8_4_p3_v60'),'corrected_pad0_p3':next(r for r in rows if r['case']=='flat_pad0_p3_v60')}
(root/'validation.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False,indent=2))
