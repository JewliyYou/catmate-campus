"""Import the two WPS/Excel cat tables into website-ready JSON and images."""

from __future__ import annotations

import json
import re
import sys
import zipfile
from io import BytesIO
from pathlib import Path
from xml.etree import ElementTree as ET

from PIL import Image, ImageOps


ROOT = Path(__file__).resolve().parents[1]
ALL_BOOK = ROOT / "(all猫猫数据表)all表格视图.xlsx"
ON_CAMPUS_BOOK = ROOT / "(在校猫猫)在校猫猫表格视图.xlsx"
DATA_OUTPUT = ROOT / "backend/src/main/resources/cat-data.json"
IMAGE_OUTPUT = ROOT / "frontend/public/cat-images"

MAIN_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
REL_NS = "http://schemas.openxmlformats.org/package/2006/relationships"
DRAWING_NS = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing"
OFFICE_REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"


def cell_column(reference: str) -> str:
    return re.match(r"[A-Z]+", reference).group(0)


def read_rows(book: Path) -> tuple[list[dict[str, str]], dict[str, str]]:
    with zipfile.ZipFile(book) as archive:
        shared_root = ET.fromstring(archive.read("xl/sharedStrings.xml"))
        shared = ["".join(item.itertext()) for item in shared_root.findall(f"{{{MAIN_NS}}}si")]
        sheet_root = ET.fromstring(archive.read("xl/worksheets/sheet1.xml"))
        rows: list[dict[str, str]] = []
        for row in sheet_root.findall(f".//{{{MAIN_NS}}}sheetData/{{{MAIN_NS}}}row"):
            values: dict[str, str] = {}
            for cell in row.findall(f"{{{MAIN_NS}}}c"):
                value_node = cell.find(f"{{{MAIN_NS}}}v")
                value = "" if value_node is None else (value_node.text or "")
                if cell.get("t") == "s" and value:
                    value = shared[int(value)]
                values[cell_column(cell.get("r", ""))] = value.strip()
            rows.append(values)

        image_names: dict[str, str] = {}
        if "xl/cellimages.xml" in archive.namelist():
            rel_root = ET.fromstring(archive.read("xl/_rels/cellimages.xml.rels"))
            rels = {
                rel.get("Id"): "xl/" + rel.get("Target", "")
                for rel in rel_root.findall(f"{{{REL_NS}}}Relationship")
            }
            image_root = ET.fromstring(archive.read("xl/cellimages.xml"))
            for picture in image_root.findall(f".//{{{DRAWING_NS}}}pic"):
                props = picture.find(f".//{{{DRAWING_NS}}}cNvPr")
                blip = picture.find(".//{http://schemas.openxmlformats.org/drawingml/2006/main}blip")
                if props is not None and blip is not None:
                    image_id = props.get("name")
                    rel_id = blip.get(f"{{{OFFICE_REL_NS}}}embed")
                    if image_id and rel_id in rels:
                        image_names[image_id] = rels[rel_id]
        return rows, image_names


def image_id(value: str) -> str | None:
    match = re.search(r'DISPIMG\("([^"]+)"', value or "")
    return match.group(1) if match else None


def image_ids(row: dict[str, str], columns: list[str], source: str) -> list[str]:
    images: list[str] = []
    for column in columns:
        found = image_id(row.get(column, ""))
        qualified = f"{source}:{found}" if found else ""
        if qualified and qualified not in images:
            images.append(qualified)
    return images[:6]


def clean(value: str | None) -> str:
    return re.sub(r"\s+", " ", value or "").strip()


def normalize_sex(value: str | None) -> str:
    sex = clean(value)
    if sex.startswith("公"):
        return "公"
    if sex.startswith("母"):
        return "母"
    return sex or "未知"


def make_record(row: dict[str, str], on_campus: bool, compact: bool) -> dict[str, object]:
    if compact:
        return {
            "name": clean(row.get("A")),
            "sex": normalize_sex(row.get("B")),
            "enrollmentTime": clean(row.get("C")),
            "health": clean(row.get("D")),
            "personality": clean(row.get("E")),
            "area": clean(row.get("F")),
            "appearance": clean(row.get("G")),
            "friendliness": int(row["H"]) if row.get("H", "").isdigit() else None,
            "notes": clean(row.get("M")),
            "schoolStatus": "在校",
            "sourceImageIds": image_ids(row, ["I", "J", "K", "L"], "campus"),
        }
    return {
        "name": clean(row.get("A")),
        "sex": normalize_sex(row.get("B")),
        "enrollmentTime": clean(row.get("C")),
        "health": clean(row.get("X")),
        "personality": clean(row.get("AB")),
        "area": clean(row.get("Z")),
        "appearance": clean(row.get("AC")),
        "friendliness": int(row["Y"]) if row.get("Y", "").isdigit() else None,
        "notes": clean(row.get("AD")),
        "schoolStatus": clean(row.get("AA")) or ("在校" if on_campus else ""),
        "sourceImageIds": image_ids(row, [chr(code) for code in range(ord("D"), ord("W") + 1)], "all"),
    }


def save_thumbnail(archive: zipfile.ZipFile, media_path: str, destination: Path) -> None:
    with Image.open(BytesIO(archive.read(media_path))) as source:
        image = ImageOps.exif_transpose(source).convert("RGB")
        image.thumbnail((960, 720), Image.Resampling.LANCZOS)
        image.save(destination, "WEBP", quality=78, method=6)


def main() -> None:
    all_rows, all_images = read_rows(ALL_BOOK)
    campus_rows, campus_images = read_rows(ON_CAMPUS_BOOK)

    records = {
        clean(row.get("A")): make_record(row, False, False)
        for row in all_rows[1:]
        if clean(row.get("A"))
    }
    campus_records = {
        clean(row.get("A")): make_record(row, True, True)
        for row in campus_rows[1:]
        if clean(row.get("A"))
    }
    for name, campus in campus_records.items():
        if name in records:
            original_images = list(records[name].get("sourceImageIds", []))
            campus_images_for_cat = list(campus.get("sourceImageIds", []))
            records[name].update({key: value for key, value in campus.items() if value not in ("", None)})
            records[name]["sourceImageIds"] = list(dict.fromkeys(original_images + campus_images_for_cat))[:6]
        else:
            records[name] = campus

    IMAGE_OUTPUT.mkdir(parents=True, exist_ok=True)
    for old_image in IMAGE_OUTPUT.glob("cat-*.webp"):
        old_image.unlink()

    output: list[dict[str, object]] = []
    with zipfile.ZipFile(ALL_BOOK) as all_archive, zipfile.ZipFile(ON_CAMPUS_BOOK) as campus_archive:
        for index, record in enumerate(records.values(), start=1):
            source_ids = list(record.pop("sourceImageIds", []))
            image_urls: list[str] = []
            for photo_index, qualified_id in enumerate(source_ids, start=1):
                source, source_id = qualified_id.split(":", 1)
                archive = all_archive if source == "all" else campus_archive
                media_path = (all_images if source == "all" else campus_images).get(source_id)
                if not media_path:
                    continue
                filename = f"cat-{index:03d}-{photo_index:02d}.webp"
                save_thumbnail(archive, media_path, IMAGE_OUTPUT / filename)
                image_urls.append(f"/cat-images/{filename}")
            record["imageUrls"] = image_urls
            record["imageUrl"] = image_urls[0] if image_urls else ""

            school_status = str(record.get("schoolStatus", ""))
            if "在校" in school_status:
                status = "校园生活中"
            else:
                status = school_status or "已离校"
            record["status"] = status
            record["code"] = f"CAT-DATA-{index:03d}"
            output.append(record)

    DATA_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    DATA_OUTPUT.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Imported {len(output)} cats ({len(campus_records)} listed on campus).")
    print(f"Generated {sum(len(row['imageUrls']) for row in output)} web photos for {sum(1 for row in output if row['imageUrl'])} cats.")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"Import failed: {exc}", file=sys.stderr)
        raise
