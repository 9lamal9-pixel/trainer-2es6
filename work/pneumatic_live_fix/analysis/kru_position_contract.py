import json,struct,math
from pathlib import Path
p=Path('work/pneumatic_live_fix/analysis/TTUMBAFORM.json')
def find(n):
 if n['name']=='KRU_btn':return n
 for c in n.get('children',[]):
  r=find(c)
  if r:return r
node=find(json.loads(p.read_text(encoding='utf-8-sig')))
a=node['props']['Rukoyat.ItemsPosPos'];positions=[]
for title in ['Отпуск','Перекрыша','Торможение']:
 i=a.index(title);b=bytes.fromhex(a[i+3]);m,e=struct.unpack('<QH',b)
 value=0 if m==0 else (-1 if e>>15 else 1)*(m/(1<<63))*2.**((e&32767)-16383)
 positions.append({'label':title,'native_value':int(value),'re_number':len(positions)+1})
assert [x['native_value'] for x in positions]==[0,1,2]
out={'source':str(p.resolve()),'control':'KRU_btn','event':'KRU_btnChangeState','native_input':'TPult+0x71 (Int32)','positions':positions,'pressure_branch_integrated':False,'activation_requirements':['locomotive stopped','UKTOL off in both sections','CPR switched to reserve','UBT enabled in working cab'],'source_manual':'https://rcit.su/techinfoV1.html, sections 5.2.3 and 7.4'}
Path('outputs/Trainer_KRU_position_contract.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf-8')
print('PASS native KRU labels and values extracted from actual form resource')
