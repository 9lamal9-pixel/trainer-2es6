"""Build a self-contained screen gallery from verified local source extracts."""
import base64, datetime, hashlib, html, json, pathlib, re, shutil
from PIL import Image
from pypdf import PdfReader

ROOT = pathlib.Path(__file__).resolve().parents[2]
BASE = ROOT/'work/mpsud_manual_discovery'
DOC = ROOT/'dev/docs'
ASSETS = DOC/'assets/mpsuid_monitor_reprint'
PDF = BASE/'control/description_mpsud_2017.pdf'
EXPECTED_PDF_SHA = '26972A1D4CAC81F35F940B6CB9313F1C55027871CC8F802DAD76382C08CFAA9E'
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest().upper()
def absolute(path): return (ROOT/path).resolve().as_posix()
def esc(value): return html.escape(str(value), quote=True)
def prose(value):
    if isinstance(value, str): return value
    if isinstance(value, list): return '; '.join(prose(v) for v in value)
    if isinstance(value, dict):
        return '; '.join(str(v) for k,v in value.items() if k not in ('status','source','source_refs','evidence','confidence') and v is not None)
    return str(value)
def transition(value):
    if isinstance(value, list): return '; '.join(transition(v) for v in value)
    if not isinstance(value,dict): return prose(value).rstrip(' .')
    bits=[]
    if value.get('from'): bits.append('из окна «'+value['from']+'»')
    if value.get('key') is not None: bits.append('клавиша '+str(value['key']))
    if value.get('destination'): bits.append('в окно «'+value['destination']+'»')
    if value.get('event'): bits.append(value['event'])
    if value.get('note'): bits.append(value['note'])
    return ', '.join(bits)
def field_text(field):
    if isinstance(field,str): return field.rstrip(' .')
    text=field.get('name','')
    if field.get('unit'): text += ' ('+str(field['unit'])+')'
    if field.get('note'): text += ': '+str(field['note'])
    return text.rstrip(' .')

def main():
    cards=json.loads((BASE/'functions/screens.json').read_text(encoding='utf-8'))
    figures=json.loads((BASE/'illustrations/illustrations.json').read_text(encoding='utf-8'))
    screens=cards['screens']; images={x['id']:x for x in figures['items']}
    ids=[f'SR{i:02d}' for i in range(22)]
    assert [s['id'] for s in screens] == ids, 'Screen sequence is incomplete'
    assert sorted(images) == ids, 'Image sequence is incomplete'
    assert digest(PDF) == EXPECTED_PDF_SHA, 'Source PDF changed'
    assert len(PdfReader(PDF).pages)==46
    assert figures['factory_original_authentication'] is False
    assert all(x['visual_checked'] for x in images.values()), 'Visual review is incomplete'
    assert all(s['fields_groups'] and s['purpose'] for s in screens), 'Functional cards are incomplete'
    ASSETS.mkdir(parents=True,exist_ok=True)
    packaged_pdf=ASSETS/'source.pdf'
    shutil.copyfile(PDF,packaged_pdf)
    assert digest(packaged_pdf)==EXPECTED_PDF_SHA
    snapshots={}
    for name,data in [('MPSUD_RE_FUNCTIONS.json',cards),('MPSUD_RE_ILLUSTRATIONS.json',figures)]:
        path=DOC/'audit_parts'/name
        path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        snapshots[path.relative_to(ROOT).as_posix()]=digest(path)
    checked=[]
    for i,s in enumerate(screens):
        f=images[s['id']]; source=(ROOT/f['image_path']).resolve()
        assert source.is_relative_to(ROOT)
        assert digest(source)==f['sha256'].upper(), s['id']+' image hash mismatch'
        assert f['reprint_figure']==i+1 and f['expected_primary_figure']==f'1.{18+i}'
        with Image.open(source) as im:
            im.verify()
        with Image.open(source) as im:
            assert im.size==(f['width'],f['height'])
        preview=(ROOT/f['white_preview_path']).resolve()
        assert preview.is_relative_to(ROOT) and digest(preview)==f['white_preview_sha256'].upper()
        with Image.open(preview) as im: assert im.size==(f['width'],f['height'])
        target=ASSETS/(s['id']+'.png'); shutil.copyfile(preview,target)
        assert digest(target)==f['white_preview_sha256'].upper()
        f['doc_image_path']=target.relative_to(ROOT).as_posix()
        checked.append({'id':s['id'],'re_figure':s['figure'],'reprint_figure':f['reprint_figure'],'pdf_page':f['pdf_page'],'path':f['doc_image_path'],'sha256':digest(target),'original_sha256':f['sha256'],'preview_kind':'PDF_WHITE_BACKGROUND_COMPOSITE','width':f['width'],'height':f['height'],'visual_checked':f['visual_checked']})

    title='МПСУиД 2ЭС6: виды экранов и их функции'
    source_note='22 иллюстрации извлечены из учебной перепечатки «Пользование монитором» (PDF, 46 страниц; метаданные создания 14.03.2019). Их происхождение из заводского РЭ пока не удостоверено. Текстовые требования сопоставлены с 2ЭС6.00.000.000 РЭ2, §1.7. Подробности без полного первичного подтверждения приведены как описание учебной копии. Печатные листы и номера рисунков РЭ приведены по его текстовому зеркалу; страницы найденного PDF указаны отдельно.'
    gallery_path=DOC/'MPSUID_RE_SCREENS_GALLERY.html'
    guide_path=DOC/'MPSUID_RE_SCREENS_GUIDE.md'
    source_url='https://scbist.com/scb/uploaded/1_1767437709.pdf?download=1'
    md=['# '+title,'','Дата: 2026-10-11. '+source_note,'',
        '[Открыть галерею всех 22 экранов]('+gallery_path.as_posix()+') · [Подробный функциональный справочник]('+(DOC/'audit_parts/MPSUD_RE_FUNCTIONS.md').as_posix()+')','',
        'Первичный текст: [РЭ2, листы 47–60](https://zinref.ru/000_uchebniki/04600_raznie_6/358_Elektrovoz_2ES6_rukovodstvo/021.htm), [61–76](https://zinref.ru/000_uchebniki/04600_raznie_6/358_Elektrovoz_2ES6_rukovodstvo/022.htm), [77–79](https://zinref.ru/000_uchebniki/04600_raznie_6/358_Elektrovoz_2ES6_rukovodstvo/023.htm). Иллюстрации: [учебная копия]('+source_url+'). SHA-256 PDF: `'+EXPECTED_PDF_SHA+'`.','',
        'Точная применимость редакции к комплектации электровоза №137+ ещё не установлена. Значения на рисунках — примеры; точные RGB, шрифты, полный каталог сообщений и единицы неподписанных полей не приняты как нормативные. Галерея описывает интерфейс и не подтверждает правильность модели тренажера.','',
        'Два монитора имеют независимые клавиатуры. Нижняя строка показывает доступные команды текущего окна. В основных окнах клавиша 1 возвращает заставку. «Контакт» является дополнительным вариантом таблиц; сообщения и редакторы также добавляют состояния, поэтому 22 рисунка не равны полному числу состояний интерфейса.','',
        '| Экран | Назначение | РЭ2 | Найденный PDF |','|---|---|---|---|']
    nav=[]; articles=[]
    for s in screens:
        f=images[s['id']]; sid=s['id']; fields=[field_text(v) for v in s['fields_groups']]
        behavior=[prose(v) for v in s.get('behavior_status',[])]
        entry=transition(s['entry']); exit_text=transition(s['exit'])
        pages=', '.join(map(str,s['printed_page']))
        md.append('| ['+s['title']+'](#'+sid.lower()+') | '+s['purpose'].replace('|','/')+' | §'+s['section']+', рис. '+s['figure']+', листы '+pages+' | рис. '+str(f['reprint_figure'])+', стр. '+str(f['pdf_page'])+' |')
        nav.append('<a href="#'+sid+'">'+esc(sid+' · '+s['title'])+'</a>')
        encoded=base64.b64encode((ROOT/f['doc_image_path']).read_bytes()).decode('ascii')
        details='<p><b>Вход:</b> '+esc(entry)+'. <b>Выход:</b> '+esc(exit_text)+'.</p>'
        details+='<h3>Показания и поля</h3><ul>'+''.join('<li>'+esc(v)+'</li>' for v in fields)+'</ul>'
        if behavior: details+='<h3>Как работает</h3><ul>'+''.join('<li>'+esc(v)+'</li>' for v in behavior)+'</ul>'
        articles.append('<article id="'+sid+'" data-search="'+esc(sid+' '+s['title']+' '+' '.join(fields))+'"><header><small>'+esc('РЭ2 §'+s['section']+' · рис. '+s['figure']+' · учебная копия: рис. '+str(f['reprint_figure'])+', PDF стр. '+str(f['pdf_page']))+'</small><h2>'+esc(s['title'])+'</h2><p>'+esc(s['purpose'])+'</p></header><button class="image-button" title="Увеличить изображение" aria-label="Увеличить '+esc(s['title'])+'"><img loading="lazy" src="data:image/png;base64,'+encoded+'" alt="'+esc(s['title'])+'" width="'+str(f['width'])+'" height="'+str(f['height'])+'"></button>'+details+'<p class="source"><a href="'+esc(packaged_pdf.as_uri()+'#page='+str(f['pdf_page']))+'">Открыть страницу источника</a> · Иллюстрация из учебной перепечатки; заводской оригинал не удостоверен.</p></article>')
    md+=['','## Виды и работа окон','']
    for s in screens:
        f=images[s['id']]
        md+=['<a id="'+s['id'].lower()+'"></a>','', '### '+s['id']+' · '+s['title'],'',
            'РЭ2 §'+s['section']+', рис. '+s['figure']+'; иллюстрация учебной копии '+str(f['reprint_figure'])+', PDF стр. '+str(f['pdf_page'])+'.','',
            '!['+s['title']+']('+(ROOT/f['doc_image_path']).as_posix()+')','',s['purpose'],'',
            '**Вход:** '+transition(s['entry'])+'. **Выход:** '+transition(s['exit'])+'.','',
            '**Поля:** '+'; '.join(field_text(v) for v in s['fields_groups'])+'.','']
        if s.get('behavior_status'): md+=['**Работа:** '+' '.join(prose(v) for v in s['behavior_status']),'']
    md+=['## Проверка и ограничения','',
        'Проверены SHA исходного PDF, последовательность 22 иллюстраций, соответствие фактически нарисованных PDF XObject их страницам, размеры и SHA PNG, наличие всех карточек и визуальная читаемость. Исходные PNG сохранены без увеличения разрешения. Для галереи прозрачность сведена на белую подложку, как при печати PDF; размеры сохранены. Мелкие подписи местами размыты; кодовые поля диагностики БУК на рисунке пустые, поэтому коды 0–5 берутся из текста.','',
        'Первичный PDF РЭ2 не получен. Сопоставление перепечатки с рисунками 1.18–1.39 опирается на порядок, названия и текст, без независимого сравнения пикселей. Серверы источников и точные исходы чтения записаны в отчёте поиска. Production, нативные DLL и основной EXE в этой работе не изменялись.','',
        '[Проверка пакета]('+(DOC/'audit_parts/MPSUD_RE_SOURCE_CHECK.json').as_posix()+') · [Предыдущий аудит тренажера]('+(DOC/'MPSUID_SCREENS_RE_AUDIT.md').as_posix()+')','']
    guide_path.write_text('\n'.join(md),encoding='utf-8')
    css='''*{box-sizing:border-box}body{margin:0;background:#121820;color:#edf1f6;font:16px/1.55 system-ui,Segoe UI,sans-serif}a{color:#88caff}main{max-width:1250px;margin:auto;padding:28px}h1{font-size:32px;line-height:1.15}h2{font-size:25px;margin:10px 0}h3{font-size:18px}.intro,.source,small{color:#bcc8d7}.intro{max-width:1000px}.tools{position:sticky;top:0;background:#121820eb;padding:14px 0;z-index:2;display:flex;gap:12px;align-items:center;backdrop-filter:blur(8px)}input{width:min(600px,100%);padding:12px;border:1px solid #42546c;border-radius:8px;background:#202c3c;color:white;font:inherit}nav{display:flex;flex-wrap:wrap;gap:8px;margin:15px 0 28px}nav a{border:1px solid #374a62;border-radius:6px;padding:6px 10px;text-decoration:none}article{background:#1b2532;border:1px solid #35465d;border-radius:12px;margin:0 0 28px;padding:24px;scroll-margin-top:85px}.image-button{display:block;padding:0;border:0;background:#fff;cursor:zoom-in;margin:20px auto;max-width:100%;border-radius:3px;overflow:hidden}.image-button img{display:block;width:min(100%,801px);height:auto;background:white}li{margin:7px 0}.source{font-size:13px;border-top:1px solid #35465d;padding-top:12px}dialog{max-width:96vw;max-height:96vh;overflow:auto;background:white;padding:40px 12px 12px;border:0;border-radius:8px}dialog::backdrop{background:#000b}dialog img{display:block;width:min(1500px,90vw);height:auto;background:white}#close{position:absolute;right:8px;top:5px;padding:5px 12px;font:inherit;cursor:pointer}[hidden]{display:none!important}@media(max-width:640px){main{padding:15px}article{padding:15px}h1{font-size:25px}}'''
    js='''const q=document.querySelector("input");q.addEventListener("input",()=>{const value=q.value.toLocaleLowerCase("ru");let n=0;document.querySelectorAll("article").forEach(a=>{a.hidden=!a.dataset.search.toLocaleLowerCase("ru").includes(value);if(!a.hidden)n++});document.getElementById("count").textContent=n+" / 22"});const d=document.querySelector("dialog"),z=d.querySelector("img");document.querySelectorAll(".image-button").forEach(b=>b.addEventListener("click",()=>{const im=b.querySelector("img");z.src=im.src;z.alt=im.alt;d.showModal()}));document.getElementById("close").addEventListener("click",()=>d.close());d.addEventListener("click",e=>{if(e.target===d)d.close()});'''
    gallery='<!doctype html><html lang="ru"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'+esc(title)+'</title><style>'+css+'</style><main><h1>'+esc(title)+'</h1><p class="intro">'+esc(source_note)+'</p><p class="intro">Выберите экран или найдите нужное показание. Нажатие на изображение открывает его крупнее. Значения на картинках являются примерами.</p><p><a href="'+esc(guide_path.as_uri())+'">Справочник и источники</a> · <a href="'+esc(packaged_pdf.as_uri())+'">Учебный PDF</a></p><div class="tools"><input type="search" aria-label="Поиск экранов и показаний" placeholder="Поиск: ПСН, ток, автоведение, диагностика…"><span id="count">22 / 22</span></div><nav>'+''.join(nav)+'</nav>'+''.join(articles)+'</main><dialog><button id="close">Закрыть</button><img alt="Увеличенный экран"></dialog><script>'+js+'</script></html>'
    gallery_path.write_text(gallery,encoding='utf-8')
    assert gallery.count('<article ')==22 and gallery.count('src="data:image/png;base64,')==22
    assert len(re.findall(r'^### SR\d\d',guide_path.read_text(encoding='utf-8'),re.M))==22
    production_baseline=json.loads((DOC/'audit_parts/MPSUD_SCREEN_PRODUCTION_CHECK.json').read_text(encoding='utf-8-sig'))
    production_sources=[]
    for item in production_baseline['sources']:
        path=ROOT/item['path']; sha=digest(path)
        assert sha==item['sha256'].upper(), 'Production source changed: '+item['path']
        production_sources.append({'path':item['path'],'sha256':sha,'unchanged':True})
    exe=ROOT/'outputs/TrainerIntegrationHost.pneumatic_candidate.exe'
    exe_sha=digest(exe)
    assert exe_sha==production_baseline['executable_sha256'].upper(), 'Production EXE changed'
    result={'schema_version':1,'utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'status':'PASS','source_pdf':packaged_pdf.relative_to(ROOT).as_posix(),'source_pdf_sha256':EXPECTED_PDF_SHA,'source_pdf_pages':46,'source_kind':'SECONDARY_REPRINT','factory_original_received':False,'factory_image_authentication_accepted':False,'normative_complete_conformity_accepted':False,'source_integrity_checks':22,'functional_cards':22,'visually_checked_illustrations':22,'guide':guide_path.relative_to(ROOT).as_posix(),'guide_sha256':digest(guide_path),'gallery':gallery_path.relative_to(ROOT).as_posix(),'gallery_sha256':digest(gallery_path),'self_contained_html_images':True,'production_sources_unchanged':len(production_sources),'executable_sha256':exe_sha,'production_sources':production_sources,'snapshots':snapshots,'images':checked}
    (DOC/'audit_parts/MPSUD_RE_SOURCE_CHECK.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({k:v for k,v in result.items() if k not in ('images','production_sources')},ensure_ascii=False,indent=2))

if __name__=='__main__': main()
