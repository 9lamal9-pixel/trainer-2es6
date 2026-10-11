"""Compatibility entry point; the first provisional implementation below is not executed."""
import pathlib, runpy
runpy.run_path(str(pathlib.Path(__file__).with_name('build_functions_v2.py')), run_name='__main__')
raise SystemExit(0)

import hashlib, json, pathlib

BASE = pathlib.Path('work/mpsud_manual_discovery/functions')
BASE.mkdir(parents=True, exist_ok=True)
DOC = pathlib.Path('dev/docs/audit_parts/MPSUD_RE_FUNCTIONS.md')

def field(name, unit=None, evidence='TEXT_CONFIRMED', note=None):
    return {'name': name, 'unit': unit, 'status': evidence, 'note': note}

def screen(n, section, pages, figure, title, entry, purpose, fields=None, behavior=None, refs=None, confirmed=True):
    return {'id': 'SR%02d' % n, 'figure': '1.%d' % figure,
            'printed_page': pages, 'printed_page_verified': False,
            'section': section, 'title': title, 'entry': entry,
            'exit': {'key': 1, 'destination': 'Заставка', 'status': 'TEXT_CONFIRMED' if confirmed else 'INDEX_ONLY'},
            'purpose': purpose, 'fields_groups': fields or [],
            'behavior_status': behavior or [], 'source_refs': refs or [],
            'status': 'TEXT_PARTIAL_FIGURE_UNVERIFIED' if confirmed else 'INDEX_ONLY_AWAITING_PRIMARY',
            'pixels_verified': False, 'figure_fields_verified': False,
            'pending': ['Оригинальный рисунок', 'Полный состав полей', 'Единицы в подписях', 'Печатный лист по оригиналу']}

R21A='fresh_1_021.json'; R21B='fresh_0_021.json'; R22A='fresh_2_022.json'; R22B='fresh_4_022.json'; R23='fresh_4_023.json'
screens = [
 screen(0,'1.7.2',[48],18,'Заставка',{'event':'Включение питания ИП-ЛЭ'},'Выбор режима и показ времени.',[field('Дата'),field('Время'),field('Нижнее меню режимов')],refs=[R21A]),
 screen(1,'1.7.3.1',[49,50,51,52,53],19,'Машинист',{'key':2,'from':'Заставка','status':'INDEX_ONLY'},'Наблюдение тяги и состояния секций.',[field('Аналоговые токи'),field('БВ/ПСН/МВ/МК/ДР/БК/ТМ/ВВК/ТЦ по секциям')],['Жёлтый ноль при отсутствии аналоговых данных.','Токи: тяга зелёная; рекуперация сине-фиолетовая; возбуждение тёмно-голубое.','Дискреты: связь/включение/авария различаются цветом текста и фона.'],[R21A]),
 screen(2,'1.7.3.2',[53],20,'Машинист с АВП',{'event':'Наличие блока АВП','status':'INDEX_ONLY'},'Показ фактического и расчётного режима.',[field('Режим: ф/р'),field('Позиция'),field('ФС/выбег')],['С/СП/П:1–23/24–44/45–65; рекуперация:0–1.','Повторный OCR-фрагмент ЭДТ П в кэше нельзя исправлять по тренажеру.'],[R21B]),
 screen(3,'1.7.3.2',[54],21,'Данные для автоведения',{'key':3,'from':'Машинист с АВП','status':'INDEX_ONLY'},'Ввод параметров АВП; полный текст пока отсутствует.',confirmed=False),
 screen(4,'1.7.3.2',[55],22,'Ограничения для автоведения',{'key':4,'from':'Машинист с АВП','status':'INDEX_ONLY'},'Ограничения АВП; полный текст пока отсутствует.',confirmed=False),
 screen(5,'1.7.3.2',[55,56,57],23,'Советчик и автоведение',{'key':5,'from':'АВП'},'Выбор подсказок либо автоведения.',[field('Индикация АВ'),field('Надпись клавиши5')],['Клавиша5 переключает режим обратно; имя команды меню изменяется.'],[R21B]),
 screen(6,'1.7.4.2',[58],24,'Вспомогательные машины и защита: состояние',{'key':3,'from':'Заставка'},'Контроль сигналов и связи.',[field('Столбец БУК'),field('Каналы/линии')],['2 переводит в контактный вариант.'],[R21A]),
 screen(7,'1.7.4.3',[59],25,'Вспомогательные машины и защита: контакт',{'key':2,'from':'ВМ: состояние'},'Привязка сигналов к блокам и входам.',[field('Номер блока'),field('Номер входа')],['Доступны режимы силовой схемы, управления, позиций, измерений, СМЕ и ПСН.'],[R21A]),
 screen(8,'1.7.5',[60,61,62],26,'Режим силовой схемы',{'key':4,'from':'Состояние МСУЛ'},'Команды и обратные связи силовой схемы.',[field('Головная секция'),field('Отключённые группы ТЭД'),field('БУК: реверсор, КЛУБ, возбуждение, К27–К40'),field('БВС: положение переключателей')],refs=[R21A,R22A]),
 screen(9,'1.7.6',[62,63,64,65],27,'Управление тягой и торможением',{'key':5,'from':'Состояние МСУЛ','status':'INDEX_ONLY'},'Просмотр прохождения команд управления.',[field('БСП: джойстики, выбег, ФС, ПБЗ, песок'),field('БСП: яркость, отпуск тормозов'),field('Контакт: блок/контакт БСП и БВС')],['Контактный вариант показывает те же сигналы с адресами.'],[R22B]),
 screen(10,'1.7.7',[65,66,67],28,'Реостатные контакторы силовой схемы',{'key':6,'from':'Состояние МСУЛ','status':'INDEX_ONLY'},'Позиции и контакторы; полное описание ожидается.',confirmed=False),
 screen(11,'1.7.8',[67,68],29,'Система измерений',{'key':7,'from':'Состояние МСУЛ','status':'INDEX_ONLY'},'Измерения и качество обмена.',[field('Ошибки межкузовных линий3/4'),field('Индекс секции'),field('Число секций')],['Быстро меняющийся счётчик ошибок указывает на плохую линию.','Единицы скорости из текущего текста не установлены.'],[R22B]),
 screen(12,'1.7.9',[69],30,'Межсекционный обмен',{'key':8,'from':'Состояние МСУЛ','status':'INDEX_ONLY'},'Межсекционные сигналы; полный текст ожидается.',confirmed=False),
 screen(13,'1.7.10',[70,71],31,'Связь ПСН',{'key':9,'from':'Состояние МСУЛ','status':'INDEX_ONLY'},'Обмен с ПСН; полный текст ожидается.',confirmed=False),
 screen(14,'1.7.11',[71,72],32,'Состояние РН3000',{'key':3,'from':'Связь ПСН','status':'INDEX_ONLY'},'Состояние регулятора; полный текст ожидается.',confirmed=False),
 screen(15,'1.7.12',[72,73],33,'Состояние СТПР',{'key':4,'from':'Связь ПСН','status':'INDEX_ONLY'},'Токи ТЭД и статусы преобразователя.',[field('БС-ПС: токи якорей/возбуждения'),field('Статусы БС-УВ1/2: СТПР1000')],refs=[R22B]),
 screen(16,'1.7.13',[73,74],34,'Состояние ПЧ ВО/ТК/ЗУ',{'key':5,'from':'Связь ПСН'},'Контроль преобразователей.',refs=[R22B]),
 screen(17,'1.7.14',[74,75],35,'Состояние калориферов',{'key':6,'from':'Связь ПСН'},'Отопление и температуры.',[field('Калориферы1/2'),field('Температура кабины'),field('Температура воздуховода')],refs=[R22B]),
 screen(18,'1.7.15',[75,76],36,'Состояние приточной установки',{'key':7,'from':'Связь ПСН','status':'INDEX_ONLY'},'Приточная установка; перечень полей ожидается.',refs=[R22B]),
 screen(19,'1.7.16',[76,77],37,'Диагностика связи',{'key':4,'from':'Заставка'},'Присутствие блоков и обмен.',[field('Блоки конфигурации'),field('Связь: каналы БЦВ/две линии')],refs=[R22B,R23]),
 screen(20,'1.7.16',[77,78],38,'Диагностика БУК',{'key':2,'from':'Диагностика','status':'INDEX_ONLY'},'Выходы БУК; полный текст ожидается.',confirmed=False),
 screen(21,'1.7.17',[78,79],39,'Настройка даты и времени',{'key':5,'from':'Заставка'},'Установка часов заставки.',[field('Дата'),field('Время')],refs=[R23]),
]

sources=[]
for p in sorted(BASE.glob('*.json')):
    if p.name in {'screens.json','source_manifest.json'}: continue
    data=json.loads(p.read_text(encoding='utf8'))
    sources.append({'path':p.as_posix(),'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),
                    'url':data.get('url'),'source_id':data.get('source_id'),
                    'accepted_for_claims': not p.name.startswith('source_'),
                    'note':'Bundled search response; may include unrelated results. NOT a primary-only text.' if p.name.startswith('source_') else 'Isolated primary result; OCR/cache may omit adjacent passages.'})

manifest={'version':1,'date':'2026-10-11','state':'PROVISIONAL_AWAITING_FULL_RE2_PDF',
          'legacy_index':'dev/docs/audit_parts/MPSUD_SCREEN_REQUIREMENTS.md',
          'legacy_index_role':'Only SR/section/figure/page candidates, not functional evidence.',
          'primary_sources':sources,'native_executed':False,'production_changed':False}
(BASE/'source_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
(BASE/'screens.json').write_text(json.dumps({'metadata':manifest,'screens':screens},ensure_ascii=False,indent=2)+'\n',encoding='utf8')

lines=['# РЭ2: функции экранов МПСУиД','',
'Дата: 2026-10-11. **Промежуточный справочник: ожидается полный оригинал РЭ2.** Production и native проверки не выполнялись. Основание функций - свежий первичный текст РЭ2 из search cache; старый REQUIREMENTS используется только как указатель. Поля тренажера сюда не переносились.', '',
'## Источники и правила чтения', '',
'Чистые фрагменты и SHA: `work/mpsud_manual_discovery/functions/source_manifest.json`; карточки галереи: `work/mpsud_manual_discovery/functions/screens.json`. Файлы `source_*` - необработанные поисковые пакеты; они исключены из доказательств. Файлы `fresh_*` содержат выделенные результаты первичного руководства. Кэш имеет пропуски, OCR и повторения: отсутствие текста не доказывает отсутствия функции.', '',
'[РЭ2, страницы зеркала021](https://zinref.ru/000_uchebniki/04600_raznie_6/358_Elektrovoz_2ES6_rukovodstvo/021.htm), [022](https://zinref.ru/000_uchebniki/04600_raznie_6/358_Elektrovoz_2ES6_rukovodstvo/022.htm), [023](https://zinref.ru/000_uchebniki/04600_raznie_6/358_Elektrovoz_2ES6_rukovodstvo/023.htm). Рисунки и печатные листы пока являются кандидатами из индекса; координаты пикселей, полный перечень строк, RGB и размеры шрифтов не подтверждены.', '',
'## Общие правила из §1.7.1 и §1.7.4.1', '',
'Два одинаковых монитора управляются своими клавиатурами; питание запускает монитор автоматически. Для дискретов применяются три канала, каждый разделён на две линии. Аварийный кворум - два элемента из разных столбцов. Аналогам соответствуют два либо три числовых поля. Детальное оформление требует оригинальных рисунков.', '',
'## Карточки окон', '']
for s in screens:
    lines += ['### '+s['id']+' · '+s['title'], '',
              '**'+s['section']+'; листы-кандидаты '+','.join(map(str,s['printed_page']))+'; рис.'+s['figure']+'.** Статус: `'+s['status']+'`.', '',
              s['purpose'], '',
              'Вход: '+json.dumps(s['entry'],ensure_ascii=False)+'; выход: '+json.dumps(s['exit'],ensure_ascii=False)+'.', '']
    if s['fields_groups']:
        lines += ['Поля/группы: '+'; '.join(f['name']+(' ['+f['unit']+']' if f['unit'] else ' [единица не подтверждена]') for f in s['fields_groups'])+'.', '']
    if s['behavior_status']:
        lines += ['Поведение: '+' '.join(s['behavior_status']), '']
    lines += ['Первичный фрагмент: '+(', '.join(s['source_refs']) if s['source_refs'] else 'не получен; функция/поля не утверждаются по тренажеру')+'.', '']
lines += ['## Проверка и следующий шаг', '',
'Сохранены22 карточки SR00–SR21, непрерывный индекс рисунков1.18–1.39 и явные статусы непроверенных полей. После получения полного PDF нужно проверить титул/редакцию, печатные листы, полные подписи и каждый рисунок; затем заменить промежуточные карточки. Нативная реализация этим документом не проверяется.', '']
DOC.write_text('\n'.join(lines),encoding='utf8')
print('saved',DOC,'screens',len(screens),'sources',len(sources))
