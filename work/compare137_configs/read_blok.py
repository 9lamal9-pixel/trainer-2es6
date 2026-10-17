from pathlib import Path
from pypdf import PdfReader
base = Path(__file__).parent
for part in (1,2):
    path = base / f'blok_re_part{part}.pdf'
    reader = PdfReader(path)
    pages = []
    for i, page in enumerate(reader.pages):
        text = page.extract_text() or ''
        pages.append(text)
        if i < 3 or any(term in text.lower() for term in ('клавиатур', 'бил-пом', 'монитор', 'ввод информации', 'органов управления')):
            print(f'PART {part}, PDF PAGE {i+1}, LENGTH {len(text)}: ' + text[:400].replace('\n', ' '))
    (base / f'blok_re_part{part}_text.txt').write_text('\n'.join(f'\n[PDF PAGE {i+1}]\n{t}' for i,t in enumerate(pages)), encoding='utf-8')
    print(f'PART {part}: PAGES={len(pages)}, CHARACTERS={sum(map(len,pages))}')
