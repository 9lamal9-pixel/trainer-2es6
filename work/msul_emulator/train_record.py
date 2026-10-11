import json
from pathlib import Path
for f in ['work/main_rtti.json','work/airbrake6000/TormModel.rtti.json']:
 d=json.loads(Path(f).read_text())
 for z in d:
  if z['name'] in ['TTrainParamTMF','TKVT','TKVTParam','TTrainBtn','TTrainLast','TBlocked367','TEPK_Param','TEPT_Param'] or (z.get('size')==70):print(z)
