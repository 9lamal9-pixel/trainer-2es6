import json
from pathlib import Path
for z in json.loads(Path('work/brake6000/main.procs.json').read_text()):
 if z['name'] in ['LocoConstructor','LocoGetWeight','TrainCalcParam','WagonGetWeightTrue']:print(z['name'],hex(z['rva']),z['length'])
for z in json.loads(Path('work/msul_emulator/native_record_schema.json').read_text()).get('records',[]):
 if z['name'] in ['TPnevmo','TGRParam']:print(z)
