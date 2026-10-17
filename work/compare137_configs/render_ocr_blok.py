from pathlib import Path
import sys, subprocess
import pypdfium2 as pdfium
base = Path(__file__).parent
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
part = int(sys.argv[1])
pages = [int(x) for x in sys.argv[2:]]
pdf = pdfium.PdfDocument(base / f'blok_re_part{part}.pdf')
for number in pages:
    path = base / f'blok{part}_p{number:03}.png'
    page = pdf[number-1]
    page.render(scale=1.5).to_pil().save(path)
    subprocess.run([r'C:\Program Files\Tesseract-OCR\tesseract.exe',str(path),str(path.with_suffix('')),'--tessdata-dir',str(base / 'tessdata'),'-l','rus','--psm','3'],check=True,capture_output=True)
    text=path.with_suffix('.txt').read_text(encoding='utf-8')
    print(f'PART {part}, PDF PAGE {number}:\n{text[:11000]}')
