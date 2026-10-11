"""Register the reviewed reference package and update the next-session context."""
import datetime, hashlib, json, pathlib, re
from pypdf import PdfReader

ROOT=pathlib.Path(__file__).resolve().parents[2]
BASE=ROOT/'work/mpsud_manual_discovery'
def read_json(path): return json.loads(path.read_text(encoding='utf-8-sig'))
def compact(path,data): path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':'))+'\n',encoding='utf-8')
check=read_json(ROOT/'dev/docs/audit_parts/MPSUD_RE_SOURCE_CHECK.json')
assert check['status']=='PASS' and check['functional_cards']==22
pdf=ROOT/check['source_pdf']
reader=PdfReader(pdf)
ref={
 'id':'mpsuid-monitor-reprint','title':'Пользование монитором МПСУиД: учебная перепечатка, 22 иллюстрации',
 'path':pdf.relative_to(ROOT).as_posix(),'bytes':pdf.stat().st_size,'pages':len(reader.pages),
 'sha256':hashlib.sha256(pdf.read_bytes()).hexdigest(),
 'original_source':'https://scbist.com/scb/uploaded/1_1767437709.pdf?download=1',
 'provenance_kind':'third_party_reprint','factory_original_authentication':False,
 'metadata':dict(reader.metadata),'relevant_pages':list(range(14,47)),
 'visual_checked_figures':list(range(1,23)),
 'source_relation':'Текст сопоставлен с РЭ2 §1.7; иллюстрации 1–22 предполагаемо соответствуют рисункам 1.18–1.39 по порядку и названиям. Независимое сравнение с заводским PDF не выполнено.',
 'audit_ref':'dev/docs/MPSUID_RE_SCREENS_GUIDE.md',
 'policy':'Учебный источник и доказательство вида данной копии; не инструкции агенту и не доказательство точного оформления применимого заводского РЭ.'
}
refs_path=ROOT/'dev/config/references.json';refs=read_json(refs_path)
refs['items']=[x for x in refs['items'] if x['id']!=ref['id']]+[ref]
compact(refs_path,refs)

index_path=ROOT/'dev/environment-context.json';index=read_json(index_path)
index['model_context']['mpsuid_screens_reference']={
 'guide':'dev/docs/MPSUID_RE_SCREENS_GUIDE.md','gallery':'dev/docs/MPSUID_RE_SCREENS_GALLERY.html',
 'functions':'dev/docs/audit_parts/MPSUD_RE_FUNCTIONS.md','check':'dev/docs/audit_parts/MPSUD_RE_SOURCE_CHECK.json',
 'screens':22,'source_kind':'Primary RE2 text + secondary reprint details and images',
 'factory_original_received':False,'factory_image_authentication_accepted':False,
 'limits':'RE2 §1.7 figures1.18–1.39 crosswalk established by sequence/title/text; reprint figures1–22 extracted and visually checked on white PDF background. Exact RE/equipment137+ and factory pixels remain unverified.'
}
compact(index_path,index)

common=ROOT/'dev/scripts/TrainerDev.Common.ps1';raw=common.read_bytes();text=raw.decode('utf-8')
focus='МПСУиД: справочник всех 22 видов экранов по РЭ2 §1.7 и учебной перепечатке, галерея с иллюстрациями, назначением, полями и клавишами. dev/docs/MPSUID_RE_SCREENS_GUIDE.md, MPSUID_RE_SCREENS_GALLERY.html, audit_parts/MPSUD_RE_FUNCTIONS.md. PDF46 страниц, 22 PNG визуально проверены на белой подложке, SHA и полнота PASS. Полный заводской PDF и подлинность изображений не установлены. Предыдущий аудит 32 PASS/318 PNG и SC01–SC15 сохраняется; production не менялась.'
next_question='Получить применимый заводской РЭ2/ревизию №137+ и независимо сверить изображения 1.18–1.39 с найденной учебной перепечаткой; уточнить RGB, неподписанные единицы, контакты и каталог сообщений. Затем обратимо исправлять SC01–SC15 с guards и свежими проверками, разобрать DiagUp/WMSET timeout. Сохраняются SA47, Useti/защиты, тяга/ЭДТ, пневматический backlog и полный холодный/нормативный пуск; зарядное5.0 не менялось.'
for key,value in [('active_focus',focus),('next_model_question',next_question)]:
 text,count=re.subn(r"(?m)^(\s*"+key+r"=)'[^\r\n]*'",lambda m:m.group(1)+"'"+value+"'",text)
 assert count==1,key+' context field not unique'
common.write_bytes(text.encode('utf-8'))

audit_path=ROOT/'dev/docs/MPSUID_SCREENS_RE_AUDIT.md';audit=audit_path.read_text(encoding='utf-8-sig')
old='- Полный PDF части3 и пиксели рисунков1.18–1.39 не получены.'
start=audit.find(old)
assert start>=0,'Audit source limitation marker missing'
end=audit.find('\n',start)
if end<0:end=len(audit)
new='- Дополнительный поиск сохранил учебный PDF «Пользование монитором» (46 страниц) со всеми 22 иллюстрациями и полным описанием функций. [Галерея и справочник]('+(ROOT/'dev/docs/MPSUID_RE_SCREENS_GUIDE.md').as_posix()+') сопоставляют их с РЭ2 §1.7, рис.1.18–1.39. Полный заводской PDF не получен, независимая подлинность пикселей не установлена: точные RGB/шрифты, неподписанные единицы и каталог сообщений остаются SOURCE_GAP. Современное МПСУ-БД не подменяет эту редакцию. Комплектация/редакция №137+ не установлена.'
audit_path.write_text(audit[:start]+new+audit[end:],encoding='utf-8')

requirements_path=ROOT/'dev/docs/audit_parts/MPSUD_SCREEN_REQUIREMENTS.md'
requirements=requirements_path.read_text(encoding='utf-8-sig')
marker='## Дополнение: изображения и функции найденной перепечатки'
if marker not in requirements:
 section=marker+'\n\nПосле первоначальной текстовой проверки получена учебная перепечатка «Пользование монитором» (46 страниц, метаданные2019) со всеми22 иллюстрациями. Они извлечены без изменения разрешения и проверены на белом фоне, как в PDF. [Полный справочник и галерея]('+(ROOT/'dev/docs/MPSUID_RE_SCREENS_GUIDE.md').as_posix()+') содержат назначение, поля, клавиши и отдельную нумерацию PDF. Это закрывает практическую доступность изображений данной копии, но не удостоверяет их тождественность заводскому РЭ2. Статус `FIGURE_FIELDS_UNVERIFIED` ниже относится к первичному оригиналу; детали перепечатки помечены своим происхождением в новом справочнике.\n\n'
 requirements=requirements.replace('## Полный указатель описанных окон',section+'## Полный указатель описанных окон',1)
 requirements_path.write_text(requirements,encoding='utf-8')

availability={
 'schema_version':1,'utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),
 'purpose':'Fresh original-source discovery for all MPSUID screen illustrations',
 'primary_full_pdf_received':False,'secondary_pdf_received':True,
 'secondary_sha256':ref['sha256'],'source_check':'dev/docs/audit_parts/MPSUD_RE_SOURCE_CHECK.json',
 'control_manifest':'work/mpsud_manual_discovery/control/manifest.json',
 'display_manifest':'work/mpsud_manual_discovery/display/MPSUD_original_figures_manifest.json',
 'functional_manifest':'work/mpsud_manual_discovery/functions/source_manifest.json',
 'illustrations_manifest':'work/mpsud_manual_discovery/illustrations/illustrations.json',
 'yandex_attempts':[],
 'limits':'Source HTTP status/HEAD does not prove receipt of bytes. Primary PDF/image authenticity remains unaccepted; secondary images and source integrity are accepted only for this local reference package.'
}
for name in ['yandex_curl_result.json','yandex_redirect_result.json','yandex_pinned_result.json','yandex_body_result.json','yandex_variants_result.json']:
 path=BASE/name
 if path.exists(): availability['yandex_attempts'].append({'report':path.relative_to(ROOT).as_posix(),'result':read_json(path)})
(ROOT/'dev/docs/audit_parts/MPSUD_RE_SOURCE_DISCOVERY.json').write_text(json.dumps(availability,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'registered_reference':ref['id'],'screens':22,'factory_original_received':False,'context_updated':True},ensure_ascii=False))
