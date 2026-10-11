from pathlib import Path
import re,sys,json
sys.stdout.reconfigure(encoding='utf8',errors='replace')
p=Path(__file__).parent
for f in p.glob('*.strings.txt'):
    print('\n'+f.name)
    for x in f.read_text(encoding='utf8').splitlines():
        if 'utf16ru' in x and re.search('[\u0410-\u044f]{4}',x) and len(x)<350:
            print(x)
for f in p.glob('*.json'):
    d=json.loads(f.read_text(encoding='utf8'))
    print('\n'+f.name)
    for dll,imports in d['imports'].items():
        print(dll,', '.join(imports))
