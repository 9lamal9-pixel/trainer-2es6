import base64, datetime, hashlib, json, pathlib, subprocess
from html.parser import HTMLParser
ROOT=pathlib.Path(__file__).resolve().parents[2]
DOC=ROOT/'dev/docs'
checks=[]
def check(ok,name):
    checks.append({'name':name,'passed':bool(ok)})
    if not ok: raise AssertionError(name)
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest().upper()
def read(path): return json.loads(path.read_text(encoding='utf-8-sig'))
class GalleryParser(HTMLParser):
    def __init__(self):
        super().__init__(); self.ids=[];self.articles=[];self.links=[];self.images=[];self.scripts=[];self.in_script=False
    def handle_starttag(self,tag,attributes):
        attrs=dict(attributes)
        if 'id' in attrs:self.ids.append(attrs['id'])
        if tag=='article':self.articles.append(attrs)
        if tag=='a':self.links.append(attrs.get('href',''))
        if tag=='img' and attrs.get('src','').startswith('data:image/png;base64,'):self.images.append(attrs)
        if tag=='script':self.in_script=True;self.scripts.append('')
    def handle_endtag(self,tag):
        if tag=='script':self.in_script=False
    def handle_data(self,data):
        if self.in_script:self.scripts[-1]+=data

source=read(DOC/'audit_parts/MPSUD_RE_SOURCE_CHECK.json')
guide=ROOT/source['guide'];gallery=ROOT/source['gallery'];pdf=ROOT/source['source_pdf']
check(sha(guide)==source['guide_sha256'],'guide SHA')
check(sha(gallery)==source['gallery_sha256'],'gallery SHA')
check(sha(pdf)==source['source_pdf_sha256'],'packaged PDF SHA')
cards=read(DOC/'audit_parts/MPSUD_RE_FUNCTIONS.json')['screens']
check(len(cards)==22,'22 functional cards')
for path,expected in source['snapshots'].items():check(sha(ROOT/path)==expected,path+' SHA')
parsed=GalleryParser();parsed.feed(gallery.read_text(encoding='utf-8'))
check(len(parsed.articles)==22,'22 gallery articles')
check(len(parsed.images)==22,'22 embedded PNG')
check(len(parsed.ids)==len(set(parsed.ids)),'unique DOM IDs')
check([a['id'] for a in parsed.articles]==[c['id'] for c in cards],'DOM/card order')
for index,item in enumerate(source['images']):
    embedded=base64.b64decode(parsed.images[index]['src'].split(',',1)[1],validate=True)
    check(hashlib.sha256(embedded).hexdigest().upper()==item['sha256'],item['id']+' embedded image SHA')
    check(sha(ROOT/item['path'])==item['sha256'],item['id']+' PNG file SHA')
    check('#'+item['id'] in parsed.links,item['id']+' navigation')
    check(cards[index]['figure']==f'1.{18+index}',item['id']+' crosswalk')
    check(item['pdf_page'] in range(14,47),item['id']+' source page range')
check(not any(url.startswith(('http:','https:')) for url in parsed.links),'offline gallery links')
check(len(parsed.scripts)==1,'one inline script')
script=ROOT/'work/mpsud_manual_discovery/gallery_syntax.js';script.write_text(parsed.scripts[0],encoding='utf-8')
node=pathlib.Path('C:/Users/sibir/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node.exe')
proc=subprocess.run([str(node),'--check',str(script)],capture_output=True,text=True,timeout=10)
check(proc.returncode==0,'JavaScript syntax')
refs=read(ROOT/'dev/config/references.json')
registered=next(x for x in refs['items'] if x['id']=='mpsuid-monitor-reprint')
check(registered['path']==source['source_pdf'],'registered source path')
check(registered['sha256'].upper()==source['source_pdf_sha256'],'registered source SHA')
check(registered['factory_original_authentication'] is False,'source type retained')
check(source['factory_original_received'] is False and source['factory_image_authentication_accepted'] is False,'factory/source gap retained')
check(source['production_sources_unchanged']==16,'16 production source guards')
check(source['normative_complete_conformity_accepted'] is False,'full normative conformity not asserted')
result={'utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'status':'PASS','assertions':len(checks),'checks':checks,'scope':'Document/source integrity, crosswalk, packaged assets, offline HTML structure and JS syntax. Browser interaction was not exercised.','factory_original_received':False,'native_tests_executed':False}
(DOC/'audit_parts/MPSUD_RE_PACKAGE_CHECK.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k!='checks'},ensure_ascii=False,indent=2))
