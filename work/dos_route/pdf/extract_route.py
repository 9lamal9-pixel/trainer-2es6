import json, re
from pathlib import Path

HERE = Path(__file__).parent
STEP = 805.86
SCALE = 28.3465  # PDF points per kilometre, 1 cm per kilometre
ORIGIN_X = 112.791
MAP_START_KM = 2149.5

def chainage(x):
    return round(MAP_START_KM - (x-ORIGIN_X)/SCALE, 3)

def globalx(x, page):
    return x + STEP*(page-3)

data = {page: json.loads((HERE / ('vectors%02d.json' % page)).read_text(encoding='utf-8')) for page in range(3,11)}
profile = {}
speed_raw = []
empty_speed_raw = []
rec_lines = {8000: [], 6300: [], 2500: []}
rec_labels = {8000: [], 6300: [], 2500: []}
axes = []
row_y = {8000: 187.08, 6300: 209.22, 2500: 229.62}
label_y = {8000: (176, 182), 6300: (196, 203), 2500: (217, 226)}

for page,d in data.items():
    words = d['words']
    grades = [w for w in words if 444 < w['top'] < 452 and re.fullmatch(r'\d+(?:,\d+)?', w['text'])]
    for line in d['lines']:
        if 436.8 <= line['top'] <= 440.7 and 440.5 <= line['bottom'] <= 444.3 and line['width'] > 0:
            a,b = globalx(line['x0'],page),globalx(line['x1'],page)
            key = next((k for k,old in profile.items() if abs(a-old['global_x_start'])<.2 and abs(b-old['global_x_end'])<.2), (round(a,1),round(b,1)))
            points = sorted(line['pts'], key=lambda pt:pt[0])
            ascending = points[-1][1] < points[0][1]
            flat = abs(points[-1][1]-points[0][1]) < .01
            candidates = [w for w in grades if line['x0']-.1 <= (w['x0']+w['x1'])/2 <= line['x1']+.1]
            # A boundary label clipped by a page is ignored when a complete decimal label exists.
            candidates.sort(key=lambda w:(',' not in w['text'], abs((w['x0']+w['x1'])/2 - (line['x0']+line['x1'])/2)))
            grade = 0.0 if flat else (float(candidates[0]['text'].replace(',','.')) * (1 if ascending else -1) if candidates else None)
            old = profile.get(key)
            if old is None or (grade is not None and (old['grade_permille'] is None or (candidates and ',' in candidates[0]['text'] and not old.get('decimal_label')))):
                profile[key] = {'global_x_start':a,'global_x_end':b,'km_start':chainage(a),'km_end':chainage(b),'grade_permille':grade,'page':page,'decimal_label':bool(candidates and ',' in candidates[0]['text'])}

    for item in d['lines'] + d['curves']:
        color = item.get('stroking_color')
        if color not in ([1,0,0], [0,0.43922,0.75294]) or not item.get('stroke'):
            continue
        pts = item['pts']
        for p,q in zip(pts,pts[1:]):
            if abs(p[1]-q[1]) > .01 or abs(p[0]-q[0]) < .01:
                continue
            a,b = sorted([p[0],q[0]])
            if b < 0 or a > 842:
                continue
            ga,gb = globalx(a,page),globalx(b,page)
            y = p[1]
            if 57.8 <= y < 164:
                v = int(round((163.08-y)/11.67)*10)
                target = speed_raw if color == [1,0,0] else empty_speed_raw
                target.append((ga,gb,v,page))
            for weight,ry in row_y.items():
                if abs(y-ry) < 1.5:
                    rec_lines[weight].append((ga,gb,'traction' if color == [1,0,0] else 'electric_braking',page))

    for weight,(lo,hi) in label_y.items():
        rw = sorted([w for w in words if lo < w['top'] < hi and w['x0'] > 0], key=lambda w:w['x0'])
        groups = []
        for word in rw:
            if not groups or word['x0']-groups[-1][-1]['x1'] > 7:
                groups.append([])
            groups[-1].append(word)
        for group in groups:
            nums = re.findall(r'\d+', ''.join(w['text'] for w in group))
            if len(nums) != 2:
                continue
            startx = group[0]['x0']
            connection = 'СП' if len(group[0]['text'])==2 else 'П'
            rec_labels[weight].append({'global_x':globalx(startx,page),'connection':connection,'current_min_A':int(nums[0]),'current_max_A':int(nums[1]),'page':page})

    local_axes = sorted(set(round(c['pts'][0][0],3) for c in d['curves'] if abs(c['height']-15.24)<.08 and abs(c['width']-7.62)<.08 and 290<c['top']<400 and 0<c['x0']<842 and c['stroke']))
    for x in local_axes:
        axes.append((globalx(x,page),page))

def merge(records):
    out=[]
    for a,b,value,page in sorted(records):
        if out and value == out[-1][2] and a <= out[-1][1]+.11:
            out[-1]=(out[-1][0],max(b,out[-1][1]),value,out[-1][3])
        else:
            out.append((a,b,value,page))
    return out

sorted_profile = sorted(profile.values(), key=lambda x:x['global_x_start'])
map_end_x = max(p['global_x_end'] for p in sorted_profile)
map_end_km = chainage(map_end_x)
for p in sorted_profile:
    p.pop('global_x_start');p.pop('global_x_end');p.pop('decimal_label')
for previous,current in zip(sorted_profile,sorted_profile[1:]):
    if abs(previous['km_end']-current['km_start'])<=.003:
        current['km_start']=previous['km_end']

speeds=[]
for a,b,v,page in merge(speed_raw):
    a,b=max(a,ORIGIN_X),min(b,map_end_x)
    if b > a:
        speeds.append({'km_start':chainage(a),'km_end':chainage(b),'speed_kmh':v,'page':page,'kind':'mandatory_as_shown_in_2018_map'})
if speeds and abs(speeds[0]['km_start']-MAP_START_KM)<.03:
    speeds[0]['km_start']=MAP_START_KM

empty_speeds=[]
for a,b,v,page in merge(empty_speed_raw):
    a,b=max(a,ORIGIN_X),min(b,map_end_x)
    if b > a:
        empty_speeds.append({'km_start':chainage(a),'km_end':chainage(b),'speed_kmh':v,'page':page,'kind':'empty_wagons_as_shown_in_2018_map'})

recommendations=[]
for weight,lines in rec_lines.items():
    lines=merge(lines)
    labels=sorted(rec_labels[weight], key=lambda x:x['global_x'])
    dedup=[]
    for label in labels:
        if not dedup or label['global_x']-dedup[-1]['global_x'] > 1:
            dedup.append(label)
    labels=dedup
    for a,b,mode,page in lines:
        a,b=max(a,ORIGIN_X),min(b,map_end_x)
        if b<=a:continue
        near=[l for l in labels if a-35 <= l['global_x'] < b]
        for i,label in enumerate(near):
            start=a if i==0 else max(a,label['global_x'])
            end=min(b,near[i+1]['global_x']) if i+1<len(near) else b
            if end<=start:continue
            recommendations.append({'km_start':chainage(start),'km_end':chainage(end),'weight_t':weight,'text':('%s %d–%d А; %s' % (label['connection'],label['current_min_A'],label['current_max_A'],'тяга' if mode=='traction' else 'электрическое торможение')),'mode':mode,'connection':label['connection'],'current_min_A':label['current_min_A'],'current_max_A':label['current_max_A'],'annotation_km':chainage(label['global_x']),'page':label['page'],'boundary_basis':'line extent; changes at next current annotation, approximate'})

names=['Войновка','Тюмень','Утяшево','Подъём','Кармак','Тугулым','Бахметское','Юшала','Чупино','Талица','Ощепково','Аксариха','Камышлов','Кокшаровский']
assert len(axes)==len(names),(len(axes),axes)
stations=[{'km':chainage(x),'name':name,'page':page,'chainage_kind':'graphical_digitization'} for name,(x,page) in zip(names,axes)]

route={'title':'Войновка — Кокшаровский, 2ЭС6','source_pdf':r'C:/Users/sibir/Downloads/Telegram Desktop/Войновка - Кокшаровский 2ЭС6.pdf','source_year':2018,'source_pages':10,'direction':-1,'map_start_km':MAP_START_KM,'map_end_km':map_end_km,'initial_km':stations[0]['km'],'route_origin_km':stations[0]['km'],'recommendation_weight_t':6300,'simulation_weight_t':6000,'locomotive_mass_t':200,'units':{'chainage':'kilometres','grade':'permille, positive uphill toward Koksharovsky','speed':'km/h','current':'ampere','weight':'tonne of trailing wagons'},'coordinate_note':'Graphical digitization; PDF cells used at their centres. No station pickets or exact segment coordinates are stated. Intervals derived from vector geometry (1 cm per km). Use as training map, not surveyed track passport. Current changes have text anchors on continuous strokes; those boundaries are approximate.','warnings':['The source is a 2018 regime card; no claim that speeds are valid today.','The source contains weight rows 8000, 6300 and 2500 t; no 6000 t row. Display 6300 t recommendations as comparison for the 6000 t simulation, without interpolation.','Station name on cover differs: Кошкаровский; graph labels the terminal station Кокшаровский.','Signal drawings show a scheme with multiple coloured circles, not a current ALSN indication. Do not automatically power safety code from their colours.','An unmarked gap in the recommendation stroke does not specify a traction current.','Empty-wagon dashed speed limits do not apply to the loaded 6000 t consist.'],'stations':stations,'profile':sorted_profile,'speed_limits':speeds,'empty_wagon_speed_limits':empty_speeds,'recommendations':sorted(recommendations,key=lambda r:(r['weight_t'],-r['km_start']))}
(HERE/'route.json').write_text(json.dumps(route,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'profile':len(sorted_profile),'missing_grade':[p for p in sorted_profile if p['grade_permille'] is None],'speeds':len(speeds),'recommendations6300':len([r for r in recommendations if r['weight_t']==6300]),'stations':stations,'map_end_km':map_end_km},ensure_ascii=False))
