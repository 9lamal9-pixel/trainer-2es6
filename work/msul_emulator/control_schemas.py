import json
from pathlib import Path
for z in json.loads(Path('work/msul_emulator/native_record_schema.json').read_text()):
 if z['name'] in ['TInputParamPult','TEPKPos','TEPKSend','TDopParam','TButton_2ES6','TPosition_2ES6']:print(z)
