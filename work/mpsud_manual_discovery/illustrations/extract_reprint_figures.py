"""Extract only image XObjects actually drawn on each secondary reprint page.

Exports lossless original-size PNGs without resampling. PDF image resources are
shared across all pages, so page.images iteration alone is not a page crosswalk.
"""

from datetime import datetime, timezone
import hashlib
import io
import json
from pathlib import Path
import re

from PIL import Image, ImageDraw, ImageFont
from pypdf import PdfReader
from pypdf.generic import ContentStream


WORKSPACE = Path(__file__).resolve().parents[3]
DESTINATION = Path(__file__).resolve().parent
PDF = WORKSPACE / "work/mpsud_manual_discovery/control/description_mpsud_2017.pdf"
PDF_SHA = "26972A1D4CAC81F35F940B6CB9313F1C55027871CC8F802DAD76382C08CFAA9E"


def digest(data):
    return hashlib.sha256(data).hexdigest().upper()


def relative(path):
    return path.relative_to(WORKSPACE).as_posix()


def main():
    source = PDF.read_bytes()
    assert digest(source) == PDF_SHA, "Unexpected source PDF"
    reader = PdfReader(io.BytesIO(source))
    assert len(reader.pages) == 46
    existing_review = {}
    review_path = DESTINATION / "visual_review.json"
    if review_path.exists():
        review = json.loads(review_path.read_text(encoding="utf-8"))
        if review.get("source_pdf_sha256") == PDF_SHA:
            existing_review = {x["id"]: x for x in review.get("items", [])}
    items = []
    for page_index in range(13, len(reader.pages)):
        page = reader.pages[page_index]
        operations = ContentStream(page.get_contents(), reader).operations
        drawn_names = [str(args[0]) for args, op in operations if op == b"Do"]
        if not drawn_names:
            continue
        text = page.extract_text() or ""
        captions = re.findall(r"Рисунок\s+(\d+)\s*[–—-]\s*([^\n]+)", text)
        assert len(captions) == len(drawn_names), (page_index + 1, captions, drawn_names)
        for (figure, caption), name in zip(captions, drawn_names):
            number = int(figure)
            assert number == len(items) + 1, "Unexpected figure order"
            ident = f"SR{number - 1:02d}"
            extracted = page.images[name]
            bitmap = page["/Resources"]["/XObject"][name].get_object()
            assert bitmap["/Subtype"] == "/Image"
            width, height = int(bitmap["/Width"]), int(bitmap["/Height"])
            data = extracted.data
            assert data[:8] == b"\x89PNG\r\n\x1a\n"
            exported = Image.open(io.BytesIO(data))
            exported.load()
            assert exported.size == (width, height)
            assert exported.size == extracted.image.size
            assert exported.mode == extracted.image.mode
            assert exported.tobytes() == extracted.image.tobytes()
            destination = DESTINATION / (ident + ".png")
            destination.write_bytes(data)
            white_directory = DESTINATION / "white"
            white_directory.mkdir(exist_ok=True)
            white_path = white_directory / (ident + ".png")
            if exported.mode == "RGBA":
                white = Image.new("RGBA", exported.size, "white")
                white.alpha_composite(exported)
                white = white.convert("RGB")
                white.save(white_path)
            else:
                white_path.write_bytes(data)
            items.append({
                "id": ident,
                "reprint_figure": number,
                "pdf_page": page_index + 1,
                "expected_primary_figure": "1." + str(number + 17),
                "primary_crosswalk_status": "SEQUENCE_AND_TITLE_MATCH_ONLY_NOT_IMAGE_AUTHENTICATION",
                "caption_first_line": caption.strip(),
                "image_path": relative(destination),
                "width": width,
                "height": height,
                "original_pixel_width": width,
                "original_pixel_height": height,
                "sha256": digest(data),
                "white_preview_path": relative(white_path),
                "white_preview_sha256": digest(white_path.read_bytes()),
                "white_preview_background": "#FFFFFF",
                "white_preview_resampled": False,
                "white_preview_is_derived": True,
                "decoded_pixel_sha256": digest(exported.tobytes()),
                "pdf_xobject_name": name,
                "pdf_xobject_reference": str(bitmap.indirect_reference),
                "pdf_decoded_stream_sha256": digest(bitmap.get_data()),
                "pdf_filter": str(bitmap.get("/Filter")),
                "pdf_color_space": str(bitmap.get("/ColorSpace")),
                "pdf_bits_per_component": int(bitmap.get("/BitsPerComponent", 0)),
                "has_pdf_soft_mask": "/SMask" in bitmap,
                "png_mode": exported.mode,
                "lossless_original_size_export": True,
                "resampled": False,
                "visual_checked": False,
                "readability": "PENDING_VISUAL_REVIEW",
                "source_kind": "SECONDARY_REPRINT_NOT_AUTHENTICATED_FACTORY_RE",
            })
    assert len(items) == 22
    assert len({x["sha256"] for x in items}) == 22
    for item in items:
        review = existing_review.get(item["id"], {})
        if review.get("sha256") == item["sha256"]:
            for key, value in review.items():
                if key in {"id", "sha256", "white_preview_sha256"}:
                    continue
                if key in {"white_preview_visual_checked", "pdf_like_white_background_checked"}:
                    if review.get("white_preview_sha256") != item["white_preview_sha256"]:
                        continue
                item[key] = value
    manifest = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "source_pdf": relative(PDF),
        "source_pdf_sha256": PDF_SHA,
        "source_pdf_pages": len(reader.pages),
        "source_metadata": {str(k): str(v) for k, v in (reader.metadata or {}).items()},
        "source_kind": "SECONDARY_REPRINT_NOT_AUTHENTICATED_FACTORY_RE",
        "factory_original_authentication": False,
        "items": items,
        "counts": {"figures": 22, "original_size_lossless_png": 22,
                   "visual_checked": sum(x["visual_checked"] is True for x in items)},
        "notes": [
            "Reprint figures1..22 correspond by title/order to RE2 figures1.18..1.39; pixel identity is unverified.",
            "Only ContentStream Do-used resources are exported; page.images exposes all shared resources on every page.",
            "PNG container is pypdf lossless export of embedded pixels, not the original PDF container bytes.",
            "Metadata Writer/LibreOffice2019 and absence of factory page stamps make this a secondary reproduction.",
            "No trainer-generated screenshot is included and no native trainer module is loaded.",
        ],
    }
    (DESTINATION / "illustrations.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    # Small labelled contact sheets are viewing aids; SRxx.png remain untouched.
    font = ImageFont.truetype("C:/Windows/Fonts/arial.ttf", 16)
    for start in range(0, 22, 6):
        group = items[start:start + 6]
        sheet = Image.new("RGB", (1476, 814), "#e5e5e5")
        draw = ImageDraw.Draw(sheet)
        draw.text((12, 5), "SECONDARY REPRINT — original RE provenance unverified", font=font, fill="#5c1a1a")
        for index, item in enumerate(group):
            x = 12 + (index % 3) * 488
            y = 35 + (index // 3) * 389
            source_image = Image.open(WORKSPACE / item["image_path"]).convert("RGBA")
            preview = Image.new("RGBA", source_image.size, "white")
            preview.alpha_composite(source_image)
            preview = preview.convert("RGB")
            preview.thumbnail((476, 356), Image.Resampling.LANCZOS)
            sheet.paste(preview, (x, y + 27))
            draw.text((x, y), f"{item['id']} / fig.{item['reprint_figure']} / PDF p.{item['pdf_page']}", font=font, fill="black")
        sheet.save(DESTINATION / f"contact_{start + 1:02d}_{start + len(group):02d}.png")
    print(json.dumps({"passed": True, "figures": len(items), "lossless_original_size_png": len(items), "manifest": str(DESTINATION / "illustrations.json"), "native_loaded": False}))


if __name__ == "__main__":
    main()
