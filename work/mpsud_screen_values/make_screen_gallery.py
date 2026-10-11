import argparse, json, re
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

parser = argparse.ArgumentParser()
parser.add_argument('run')
parser.add_argument('--output', required=True)
args = parser.parse_args()
run, out = Path(args.run), Path(args.output)
out.mkdir(parents=True, exist_ok=True)
font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 18)
tiles, manifest = [], []
results = [Path(x) for x in json.loads(run.read_text(encoding='utf-8-sig'))] if run.is_file() else run.glob('*/results/MPSUD_screens_runtime.json')
seen = set()
for result in results:
    data = json.loads(result.read_text(encoding='utf-8-sig'))
    if not data.get('passed'): continue
    selected = [c for c in data['captures'] if c['side'] == 'left'
                and re.match(r'route(?:[0-9]+)_(?:key[0-9]+_)?side1$', c['label'])]
    if not selected: continue
    c = selected[-1]
    index = int(re.match(r'route([0-9]+)', c['label']).group(1))
    if index > 20 or index in seen: continue
    seen.add(index)
    path = result.parent / c['bitmap']['file']
    im = Image.open(path).convert('RGB').resize((600, 450))
    tile = Image.new('RGB', (620, 495), '#202020')
    tile.paste(im, (10, 35))
    ImageDraw.Draw(tile).text((10, 6), f'{index:02d} enum {c["window_type"]}: {c["frame_class"]}', font=font, fill='white')
    tiles.append((index, tile))
    manifest.append({'route_index': index, 'result': str(result), 'source_png': str(path)})
tiles.sort(key=lambda item: item[0])
pages = []
for start in range(0, len(tiles), 4):
    page = Image.new('RGB', (1240, 990), '#202020')
    for n, (_, tile) in enumerate(tiles[start:start+4]):
        page.paste(tile, ((n % 2)*620, (n // 2)*495))
    name = f'screens_{start//4+1:02d}.png'
    page.save(out / name)
    pages.append(name)
(out / 'gallery.json').write_text(json.dumps({'selected': manifest, 'pages': pages}, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({'tiles': len(tiles), 'pages': pages}, ensure_ascii=False))
