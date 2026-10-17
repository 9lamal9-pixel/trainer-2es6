import json, pathlib
base=pathlib.Path(__file__).parent
route=json.loads((base.parent/'data'/'route_enriched.json').read_text(encoding='utf-8'))
errors=[]
for station in route['stations']:
    page=station['page'];d=json.loads((base.parents[1]/'dos_route'/'pdf'/('vectors%02d.json'%page)).read_text(encoding='utf-8'))
    axes=[c['pts'][0][0] for c in d['curves'] if abs(c['height']-15.24)<.08 and abs(c['width']-7.62)<.08 and 290<c['top']<400 and 0<c['x0']<842 and c['stroke']]
    local=112.791+(route['map_start_km']-station['km'])*28.3465-805.86*(page-3)
    error=min(abs(local-x) for x in axes)
    errors.append({'station':station['name'],'page':page,'predicted_x_pt':local,'nearest_axis_error_pt':error})
    assert error < .02,(station,error)
for page in range(3,10):
    boundary=((page-2)*805.86-112.791)/28.3465*1000
    before=112.791+(boundary-.001)/1000*28.3465
    after=112.791+(boundary+.001)/1000*28.3465
    assert 3+int(before//805.86)==page
    assert 3+int(after//805.86)==page+1
out={'result':'PASS','pdf_render_width_pt':842,'page_step_pt':805.86,'overlap_pt':842-805.86,'page_boundary_tests':7,'station_axis_tests':len(errors),'max_axis_error_pt':max(x['nearest_axis_error_pt'] for x in errors),'stations':errors}
(base/'pdf_overlay_validation.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in out.items() if k!='stations'},ensure_ascii=False))
