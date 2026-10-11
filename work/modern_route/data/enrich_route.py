import json, math, re, sys
from pathlib import Path
from collections import Counter

HERE = Path(__file__).parent
ROOT = HERE.parents[2]
VECTOR_DIR = ROOT / 'work/dos_route/pdf'
route = json.loads((ROOT / 'outputs/Voynovka_Koshkarovsky_route.json').read_text(encoding='utf-8'))
STEP, SCALE, ORIGIN_X, MAP_START = 805.86, 28.3465, 112.791, 2149.5
def globalx(x, page): return x + STEP * (page - 3)
def chainage(x, page): return round(MAP_START-(globalx(x,page)-ORIGIN_X)/SCALE, 3)
def rgb(value, color): return isinstance(value, (list,tuple)) and len(value)==3 and all(abs(a-b)<0.002 for a,b in zip(value,color))
def approx(value, target, tol=.1): return abs(value-target)<=tol
raw_curves=[]
signals=[]
events=[]
missing=[]
data={page:json.loads((VECTOR_DIR/('vectors%02d.json'%page)).read_text(encoding='utf-8')) for page in range(3,11)}
def event(x,page,typ,label):
    if not -0.05<=x<=842.05:return
    events.append({'km':chainage(x,page),'type':typ,'label':label,'page':page,'chainage_kind':'graphical_digitization','static_scheme':True})
for page,d in data.items():
    words=d['words']
    radii=[w for w in words if not w['upright'] and ((468<w['top']<490) or (505<w['top']<530)) and re.fullmatch(r'\d+',w['text']) and int(w['text'])>=100]
    for path in d['curves']+d['lines']:
        if not path.get('stroke'):continue
        if 491.4<=path['top']<=503.6 and path['bottom']<=503.6 and rgb(path.get('stroking_color'),[0,0,0]):
            for p,q in zip(path['pts'],path['pts'][1:]):
                if abs(p[1]-q[1])>.01 or abs(p[0]-q[0])<.01 or abs(p[1]-497.46)<.1:continue
                a,b=sorted([p[0],q[0]])
                if b<0 or a>842:continue
                side='above' if p[1]<497.46 else 'below'
                candidates=[w for w in radii if ('above' if w['top']<490 else 'below')==side and a-4<=(w['x0']+w['x1'])/2<=b+4]
                candidates.sort(key=lambda w:abs((w['x0']+w['x1'])/2-(a+b)/2))
                raw_curves.append({'a':globalx(a,page),'b':globalx(b,page),'radius':int(candidates[0]['text']) if candidates else None,'side':side,'page':page,'candidate_labels':[{'text':w['text'],'gx':globalx((w['x0']+w['x1'])/2,page)} for w in candidates]})
        color=path.get('stroking_color'); nc=path.get('non_stroking_color')
        if 270<path['top']<410 and 10.5<path['width']<12 and 9.5<path['height']<11 and rgb(color,[0,0,0]) and any(rgb(nc,c) for c in [[0,1,0],[1,0,0],[1,1,0]]):
            sx=(path['x0']+path['x1'])/2
            if 0<=sx<=842:signals.append({'x':sx,'top':path['top'],'color':nc,'page':page})
        if 270<path['top']<410 and 10.5<path['width']<12 and 8<path['height']<9 and rgb(color,[0,0,0]) and rgb(nc,[1,0,0]):
            event((path['x0']+path['x1'])/2,page,'blocking_signal','Заградительный светофор')
        if 350<path['top']<415 and 14<path['width']<15 and 10<path['height']<11:
            if rgb(color,[1,0,0]): event((path['x0']+path['x1'])/2,page,'unguarded_crossing','Неохраняемый переезд')
            elif rgb(color,[0,.6902,.31373]):event((path['x0']+path['x1'])/2,page,'guarded_crossing','Охраняемый переезд')
        if 310<path['top']<410 and 15<path['width']<16 and 13<path['height']<14 and rgb(color,[0,0,0]) and rgb(nc,[0,0,0]):
            event((path['x0']+path['x1'])/2,page,'ktsm','КТСМ')
        if 330<path['top']<405 and 6.2<path['width']<6.8 and 5.4<path['height']<5.9 and rgb(color,[1,0,0]) and rgb(nc,[1,0,0]):
            event((path['x0']+path['x1'])/2,page,'steep_drop','Обрывное место')
        if 530<path['top']<554 and 12.5<path['width']<13.5 and 12.5<path['height']<13.5 and rgb(color,[0,0,0]) and rgb(nc,[0,0,0]):
            event((path['x0']+path['x1'])/2,page,'inspection_crew','Осмотр поезда локомотивной бригадой')
        if 400<path['top']<413 and 20<path['width']<22 and 7<path['height']<8 and rgb(color,[0,0,0]):
            event((path['x0']+path['x1'])/2,page,'bridge','Мост')
        if 530<path['top']<565 and 9<path['width']<12 and 10<path['height']<15 and path['bottom']<566:
            if rgb(color,[0,.43922,.75294]) and len(path['pts'])>6:
                event((path['x0']+path['x1'])/2,page,'inspection_assistant','Осмотр поезда помощником машиниста')
            elif rgb(color,[0,0,0]) and not rgb(nc,[0,0,0]) and len(path['pts'])>6:
                event((path['x0']+path['x1'])/2,page,'inspection_driver','Осмотр поезда машинистом')
    for w in words:
        if w['text'].upper()=='У' and 360<w['top']<415:event((w['x0']+w['x1'])/2,page,'uksps','УКСПС')

# A curve's plateau span is its graphical length. Numbers are radius in metres;
# the source does not print separate surveyed length or start/end coordinates.
curve_groups=[]
for c in sorted(raw_curves,key=lambda x:(x['a'],x['b'])):
    same=next((old for old in curve_groups if abs(old['a']-c['a'])<.13 and abs(old['b']-c['b'])<.13),None)
    if same:
        if same['radius'] is None and c['radius'] is not None:same.update(c)
    else:curve_groups.append(c)
for c in curve_groups:
    if c['radius'] is None:missing.append(c)
curves=[]
for c in curve_groups:
    if c['radius'] is None:continue
    curves.append({'km_start':round(MAP_START-(c['a']-ORIGIN_X)/SCALE,3),'km_end':round(MAP_START-(c['b']-ORIGIN_X)/SCALE,3),'radius_m':c['radius'],'length_m':round((c['b']-c['a'])/SCALE*1000,1),'page':c['page'],'chainage_kind':'graphical_digitization','length_kind':'graphical_plateau_span','drawing_side':c['side']})

signal_groups=[]
for c in sorted(signals,key=lambda x:(globalx(x['x'],x['page']),x['top'])):
    gx=globalx(c['x'],c['page'])
    same=next((old for old in signal_groups if abs(old['gx']-gx)<.2),None)
    if same:same['circles'].append(c)
    else:signal_groups.append({'gx':gx,'circles':[c]})
signals=[]
for group in signal_groups:
    circles=group['circles'];c=circles[0];page=c['page'];x=c['x'];top=min(z['top'] for z in circles)
    wds=[w for w in data[page]['words'] if 0<(top-w['bottom'])<15 and abs((w['x0']+w['x1'])/2-x)<8]
    wds.sort(key=lambda w:abs((w['x0']+w['x1'])/2-x))
    typ='station_signal' if any(rgb(z['color'],[1,1,0]) for z in circles) else 'passing_signal'
    signals.append({'km':round(MAP_START-(group['gx']-ORIGIN_X)/SCALE,3),'type':typ,'label':wds[0]['text'] if wds else '', 'page':page,'chainage_kind':'graphical_digitization','static_scheme':True})

# Rectangles were omitted in the earlier vector dump. Read them from the same PDF.
try:
    import pdfplumber
    with pdfplumber.open(route['source_pdf']) as pdf:
        for page in range(3,11):
            for r in pdf.pages[page-1].rects:
                x=(r['x0']+r['x1'])/2
                if 360<r['top']<400 and 18<r['width']<23 and 15<r['height']<20 and rgb(r.get('stroking_color'),[1,0,0]):
                    event(x,page,'brake_test','Проверка действия тормозов')
                elif r['stroke'] and 300<r['top']<385 and 7<r['width']<7.2 and 14<r['height']<14.3 and rgb(r.get('stroking_color'),[0,0,0]) and rgb(r.get('non_stroking_color'),[1,1,1]):
                    event(r['x1'],page,'suburban_stop','Остановочный пункт пригородных поездов')
except ImportError:
    print('WARNING: pdfplumber unavailable, rectangles not read')

dedup=[]
for e in sorted(events,key=lambda x:(-x['km'],x['type'])):
    if not any(e['type']==old['type'] and abs(e['km']-old['km'])<.03 for old in dedup):dedup.append(e)
route['curves']=curves
route['unknown_curves']=[{'km_start':round(MAP_START-(c['a']-ORIGIN_X)/SCALE,3),'km_end':round(MAP_START-(c['b']-ORIGIN_X)/SCALE,3),'radius_m':None,'length_m':round((c['b']-c['a'])/SCALE*1000,1),'page':c['page'],'chainage_kind':'graphical_digitization','reason':'The PDF draws the curve span but prints no radius for it.'} for c in missing]
route['signals']=signals
route['events']=dedup
route['curve_coordinate_note']='Radius is printed in the PDF. Curve start/end and length are measured from the stepped horizontal vector drawing at 1 cm/km; they are approximate. Above/below distinguishes the graphic side only; no geographical left/right direction is claimed.'
route['event_coordinate_note']='Static infrastructure and instruction symbols are digitized from the PDF, not a live signal state or ALSN input. No operational signal schedule or electronic map is supplied.'
route['warnings'].append('The PDF draws one curve on page 9 without a radius label (roughly 1966.106–1965.107 km). Its curvature resistance cannot be verified from this source; no radius is invented.')
(HERE/'route_enriched.json').write_text(json.dumps(route,ensure_ascii=False,indent=2),encoding='utf-8')
summary={'curve_count':len(curves),'missing_radius':missing,'multiple_radius_labels':[c for c in curve_groups if len(c['candidate_labels'])>1],'signal_count':len(signals),'unlabelled_signals':[s for s in signals if not s['label']],'events':dict(Counter(e['type'] for e in dedup)),'shortest_curve_m':min(c['length_m'] for c in curves),'min_radius_m':min(c['radius_m'] for c in curves)}
(HERE/'extraction_validation.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
sys.stdout.reconfigure(encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False,indent=2))
