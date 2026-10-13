"""Read-only topic index for the supplied native trainer manual."""
import argparse
import hashlib
import json
import re
from pathlib import Path
from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'work/native_distribution/WORK_TRENAGER_3D/HELP/instr_Тренажер 3D_2019.pdf'
REPORT = ROOT / 'dev/runs/world3d-investigation-20261013'
TOPICS = {
    'markup': r'разметк',
    'world': r'3D|тр[её]хмер|мир[а-я]*',
    'network': r'сервер|сетев|сети|сеть',
    'scenario': r'сценари',
    'route': r'маршрут',
}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--extract-pages', default='')
    parser.add_argument('--render-pages', default='')
    args = parser.parse_args()
    reader = PdfReader(SOURCE)
    pages = []
    for number, page in enumerate(reader.pages, 1):
        text = page.extract_text() or ''
        hits = {}
        for topic, pattern in TOPICS.items():
            matches = list(re.finditer(pattern, text, re.I))
            if matches:
                hits[topic] = {
                    'count': len(matches),
                    'snippets': [re.sub(r'\s+', ' ', text[max(0, m.start()-65):m.end()+150]) for m in matches[:3]],
                }
        if hits:
            pages.append({'pdf_page': number, 'text_chars': len(text), 'topics': hits})
    result = {
        'source': SOURCE.relative_to(ROOT).as_posix(),
        'sha256': hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
        'pdf_pages': len(reader.pages),
        'method': 'pypdf text topic index, not a rendered-layout check',
        'pages': pages,
    }
    REPORT.mkdir(parents=True, exist_ok=True)
    (REPORT / 'manual-index.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    if args.extract_pages:
        selected = [int(value) for value in args.extract_pages.split(',')]
        extracts = [{'pdf_page': number, 'text': reader.pages[number-1].extract_text() or ''} for number in selected]
        (REPORT / 'manual-pages.json').write_text(json.dumps(extracts, ensure_ascii=False, indent=2), encoding='utf-8')
    if args.render_pages:
        import pypdfium2 as pdfium
        document = pdfium.PdfDocument(str(SOURCE))
        try:
            for number in [int(value) for value in args.render_pages.split(',')]:
                page = document[number-1]
                bitmap = page.render(scale=1.6)
                try:
                    bitmap.to_pil().save(REPORT / f'manual-page-{number:02d}.png')
                finally:
                    bitmap.close()
                    page.close()
        finally:
            document.close()
    print(json.dumps({'pdf_pages': len(reader.pages), 'matching_pages': [{'pdf_page': p['pdf_page'], 'topics': list(p['topics'])} for p in pages]}, ensure_ascii=False))

if __name__ == '__main__':
    main()
