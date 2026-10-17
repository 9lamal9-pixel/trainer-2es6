from pathlib import Path
from pypdf import PdfReader, PdfWriter

root = Path(__file__).resolve().parents[1]
reader = PdfReader(root / 'work/public_2es6_part1.pdf')
writer = PdfWriter()
for page_number in [9, 10, 11, 12, 21, 22, 23, 24, 49, 50, 51, 52]:
    page = reader.pages[page_number - 1]
    writer.add_page(page)
writer.add_metadata({
    '/Title': '2ES6 factory manual: specifications, traction, electric braking (excerpt)',
    '/Subject': 'Original printed sheets 9-12, 21-24, 49-52; serial 137 applicability not verified',
})
target = root / 'outputs/2ES6_RE_characteristics_excerpt.pdf'
with target.open('wb') as stream:
    writer.write(stream)
print(target.name, target.stat().st_size)
