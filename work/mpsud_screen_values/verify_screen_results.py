"""Verify saved MSUL screen coverage and PNG integrity without loading native DLLs.

Usage: python verify_screen_results.py RESULT.json [RESULT2.json ...]
       [--coverage full|partial] [--output VERIFICATION.json]
This verifies the saved claims/artifacts, not normative accuracy of their pixels.
"""

from __future__ import annotations

import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import struct
import sys
import zlib


MSUL_SHA256 = "95E3FB14CBC7FF2B5E9C61DC2215CFA7C2F23161FE21D075ADF04F45907B653B"
SCREENS = {
    1: ("TwtClockFrame", 0),
    2: ("TwtMashinistFrame", 1),
    3: ("TwtVspomMashineFrame", 1),
    4: ("TwtVspomMashineFrame", 2),
    5: ("TwtVspomMashineFrame", 2),
    6: ("TwtVspomMashineFrame", 2),
    7: ("TwtContactorsFrame", 2),
    8: ("TwtCIFrame", 2),
    9: ("TwtDiagnostFrame", 1),
    10: ("TwtAutovedFrame", 2),
    11: ("TwtOgranFrame", 2),
    13: ("TwtSautFrame", 1),
    14: ("TwtOborudovanieFrame", 1),
    15: ("TwtSMErame", 2),
    16: ("TwtPCConnectFrame", 2),
    17: ("TwtRN3000Frame", 3),
    18: ("TwtSTPRFrame", 3),
}
EXTRA_VIEWS = {
    "contactors_alternate": (4, "TwtContactorsFrame", 3),
    "mode_alternate": (4, "TwtVspomMashineFrame", 3),
    "control_alternate": (4, "TwtVspomMashineFrame", 3),
    "driver_messages": (2, "TwtMashinistFrame", 2),
}
FIXTURE_CAPTURE_PREFIXES = {
    "driver": ("driver_known_values",),
    "ci": ("ci_known_values",),
    "av": ("av_selected", "av_edit_wagons"),
    "sme": ("sme_VklTD",),
}


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest().upper()


def png_info(data: bytes) -> tuple[int, int]:
    """Validate PNG framing, IHDR dimensions and every chunk CRC."""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("PNG signature mismatch")
    offset = 8
    size = None
    has_idat = False
    ended = False
    index = 0
    while offset < len(data):
        if offset + 12 > len(data):
            raise ValueError("truncated PNG chunk")
        length = struct.unpack_from(">I", data, offset)[0]
        chunk_type = data[offset + 4:offset + 8]
        end = offset + 12 + length
        if end > len(data):
            raise ValueError("PNG chunk extends past file")
        payload = data[offset + 8:offset + 8 + length]
        stored_crc = struct.unpack_from(">I", data, offset + 8 + length)[0]
        actual_crc = zlib.crc32(chunk_type + payload) & 0xFFFFFFFF
        if stored_crc != actual_crc:
            raise ValueError("PNG chunk CRC mismatch: " + repr(chunk_type))
        if index == 0 and chunk_type != b"IHDR":
            raise ValueError("IHDR is not first PNG chunk")
        if chunk_type == b"IHDR":
            if size is not None or length != 13:
                raise ValueError("invalid or duplicate IHDR")
            size = struct.unpack_from(">II", payload, 0)
        elif chunk_type == b"IDAT":
            has_idat = True
        elif chunk_type == b"IEND":
            if length != 0 or end != len(data):
                raise ValueError("invalid IEND or data after IEND")
            ended = True
        offset = end
        index += 1
        if ended:
            break
    if size is None or not has_idat or not ended:
        raise ValueError("PNG missing IHDR, IDAT or IEND")
    return size


def fixture_statuses(value, path="fixture", assertions=False):
    """Inspect assertion containers, avoiding ordinary expected/actual booleans."""
    if type(value) is bool and assertions:
        yield path, value
    elif isinstance(value, list):
        for index, item in enumerate(value):
            yield from fixture_statuses(item, f"{path}[{index}]", assertions)
    elif isinstance(value, dict):
        is_check_object = any(key.lower() in {"passed", "ok", "success", "accepted", "expected", "actual"}
                              for key in value)
        for key, item in value.items():
            lowered = key.lower()
            if lowered in {"passed", "ok", "success", "accepted"}:
                yield path + "." + key, item is True
            elif lowered.endswith("checks") or lowered in {"assertions", "results"}:
                yield from fixture_statuses(item, path + "." + key, True)
            elif isinstance(item, (dict, list)) and lowered not in {"actual", "expected"}:
                yield from fixture_statuses(item, path + "." + key, False)
            elif assertions and not is_check_object and type(item) is bool:
                yield path + "." + key, item


def verify(source: Path, require_full_coverage: bool = True) -> dict:
    errors = []
    notes = []
    raw = source.read_bytes()
    report = json.loads(raw.decode("utf-8-sig"))
    if not isinstance(report, dict):
        raise ValueError("result root must be an object")
    if report.get("passed") is not True:
        errors.append("result.passed is not true")
    if report.get("error") not in (None, ""):
        errors.append("result.error is nonempty")
    if str(report.get("module_sha256", "")).upper() != MSUL_SHA256:
        errors.append("MSUL module SHA guard mismatch/missing")
    if report.get("electrical_faults") != 0:
        errors.append("electrical_faults is not zero")

    checks = report.get("checks")
    if not isinstance(checks, list) or not checks:
        errors.append("result.checks must be a nonempty array")
        checks = []
    named_checks = {}
    failed_checks = []
    for index, check in enumerate(checks):
        if not isinstance(check, dict) or check.get("passed") is not True:
            name = check.get("name", f"checks[{index}]") if isinstance(check, dict) else f"checks[{index}]"
            failed_checks.append(str(name))
            continue
        name = check.get("name")
        if not isinstance(name, str) or not name:
            errors.append(f"checks[{index}] has no name")
        elif name in named_checks:
            errors.append("duplicate check name: " + name)
        else:
            named_checks[name] = check
    if failed_checks:
        errors.append("checks not true: " + ", ".join(failed_checks))

    captures = report.get("captures")
    if not isinstance(captures, list) or not captures:
        errors.append("result.captures must be a nonempty array")
        captures = []
    capture_index = {}
    image_cache = {}
    good_images = 0
    image_failures = []
    for index, capture in enumerate(captures):
        prefix = f"captures[{index}]"
        if not isinstance(capture, dict):
            errors.append(prefix + " is not an object")
            continue
        label = capture.get("label")
        side = capture.get("side")
        if not isinstance(label, str) or side not in {"left", "right"}:
            errors.append(prefix + " label/side invalid")
            continue
        capture_index.setdefault((label, side), []).append(capture)
        enum = capture.get("window_type")
        frame_class = capture.get("frame_class")
        valid_classes = {SCREENS[enum][0]} if type(enum) is int and enum in SCREENS else set()
        if enum == 4:
            valid_classes.add("TwtContactorsFrame")
        if frame_class not in valid_classes:
            errors.append(prefix + f" invalid enum/class: {enum}/{frame_class}")
        bitmap = capture.get("bitmap")
        try:
            if not isinstance(bitmap, dict):
                raise ValueError("bitmap is not an object")
            if bitmap.get("width") != 800 or bitmap.get("height") != 600:
                raise ValueError("declared bitmap size is not 800x600")
            filename = bitmap.get("file")
            if not isinstance(filename, str) or not filename:
                raise ValueError("bitmap file missing")
            image_path = (source.parent / filename).resolve()
            try:
                image_path.relative_to(source.parent.resolve())
            except ValueError:
                raise ValueError("bitmap file outside result directory") from None
            if image_path not in image_cache:
                data = image_path.read_bytes()
                if len(data) > 16 * 1024 * 1024:
                    raise ValueError("unexpected PNG size exceeds 16MiB")
                image_cache[image_path] = (sha256(data), png_info(data))
            digest, dimensions = image_cache[image_path]
            if dimensions != (800, 600):
                raise ValueError(f"IHDR size is {dimensions}, expected800x600")
            declared_sha = bitmap.get("sha256")
            if not isinstance(declared_sha, str) or not re.fullmatch(r"[0-9A-Fa-f]{64}", declared_sha):
                raise ValueError("invalid/missing bitmap SHA")
            if digest != declared_sha.upper():
                raise ValueError("bitmap SHA mismatch")
            good_images += 1
        except (OSError, ValueError, struct.error) as error:
            image_failures.append(prefix + ": " + str(error))
    errors.extend(image_failures)

    missing_routes = []
    missing_extra = []

    available_screen_routes = []
    available_extra_routes = []

    def route(name, alias, side_index, enum, cls, key_count, missing, available):
        side = ("left", "right")[side_index]
        route_key = f"{name}/{side}"
        for candidate in (name, alias):
            label = f"{candidate}_side{side_index}" if key_count == 0 else f"{candidate}_key{key_count - 1}_side{side_index}"
            hits = capture_index.get((label, side), [])
            if len(hits) != 1 or hits[0].get("window_type") != enum or hits[0].get("frame_class") != cls:
                continue
            valid = True
            for suffix, expected in (("type", enum), ("class", cls)):
                check = named_checks.get(f"{candidate} {suffix} side{side_index}")
                valid = valid and check is not None and check.get("expected") == expected and check.get("actual") == expected
            independent = named_checks.get(f"{candidate} independent side{side_index}")
            valid = valid and independent is not None and "actual" in independent and "expected" in independent and independent["actual"] == independent["expected"]
            if valid:
                available.append(route_key)
                return
        missing.append(route_key + ": terminal capture and type/class/independence assertions missing or mismatch")

    for side_index in range(2):
        for index, (enum, (cls, count)) in enumerate(SCREENS.items()):
            route("screen" + str(enum), "route" + str(index), side_index, enum, cls, count, missing_routes, available_screen_routes)
        for index, (name, (enum, cls, count)) in enumerate(EXTRA_VIEWS.items(), 17):
            route(name, "route" + str(index), side_index, enum, cls, count, missing_extra, available_extra_routes)
    if require_full_coverage:
        errors.extend(missing_routes)
        errors.extend(missing_extra)

    fixture_keys = [key for key in report if "fixture" in key.lower()]
    fixture_checks = []
    for key in fixture_keys:
        fixture_checks.extend(fixture_statuses(report[key], key, key.lower().endswith("checks")))
    named_fixture_checks = {name for name in named_checks if "fixture" in name.lower()}
    detected_fixture_families = []
    for family, prefixes in FIXTURE_CAPTURE_PREFIXES.items():
        if not any(label.startswith(prefixes) for label, _ in capture_index):
            continue
        detected_fixture_families.append(family)
        quantitative_checks = {
            name for name, check in named_checks.items()
            if name.lower().startswith(family + " ")
            and "actual" in check and "expected" in check
            and check["expected"] != "observation"
        }
        if not quantitative_checks:
            errors.append("fixture captures present without quantitative assertions: " + family)
        named_fixture_checks.update(quantitative_checks)
    bad_fixture_checks = [name for name, passed in fixture_checks if not passed]
    if bad_fixture_checks:
        errors.append("fixture checks not true: " + ", ".join(bad_fixture_checks))
    if fixture_keys and not fixture_checks and not named_fixture_checks:
        errors.append("fixture data present without verifiable fixture assertions")
    if not fixture_keys and not named_fixture_checks:
        notes.append("No quantitative fixture assertions supplied; values are not accepted by this verifier.")
    notes.append("Coverage and saved PNG integrity only; pixel semantics/RE compliance/real-time are not certified.")
    return {
        "passed": not errors,
        "verified_at_utc": datetime.now(timezone.utc).isoformat(),
        "source": str(source.resolve()),
        "source_sha256": sha256(raw),
        "scope": "saved_screen_coverage_and_artifact_integrity",
        "normative_mpsuid_accepted": False,
        "counts": {
            "checks": len(checks),
            "checks_true": len(named_checks),
            "captures": len(captures),
            "png_records_verified": good_images,
            "unique_png_files": len(image_cache),
            "required_screen_routes": 34,
            "required_extra_view_routes": 8,
            "fixture_statuses": len(fixture_checks),
            "named_fixture_checks": len(named_fixture_checks),
            "screen_routes_verified": len(available_screen_routes),
            "extra_view_routes_verified": len(available_extra_routes),
        },
        "coverage_required": require_full_coverage,
        "detected_fixture_families": detected_fixture_families,
        "available_screen_routes": available_screen_routes,
        "available_extra_view_routes": available_extra_routes,
        "missing_screen_routes": missing_routes,
        "missing_extra_view_routes": missing_extra,
        "errors": errors,
        "notes": notes,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("result", type=Path, nargs="+")
    parser.add_argument("--coverage", choices=("full", "partial"), default="full",
                        help="full requires all17 states and4 extra views on both sides across supplied files")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    sources = list(dict.fromkeys(path.resolve() for path in args.result))
    destination = args.output or sources[0].with_name("MPSUD_screens_verification.json")
    reports = []
    for source in sources:
        try:
            reports.append(verify(source, require_full_coverage=False))
        except (OSError, ValueError, TypeError, KeyError) as error:
            reports.append({"passed": False, "source": str(source), "errors": [str(error)]})
    errors = [str(report["source"]) + ": " + message for report in reports for message in report.get("errors", [])]
    screen_routes = set()
    extra_routes = set()
    for report in reports:
        # Failed reports cannot provide accepted coverage for the aggregate.
        if report["passed"]:
            screen_routes.update(report.get("available_screen_routes", []))
            extra_routes.update(report.get("available_extra_view_routes", []))
    expected_screens = {f"screen{enum}/{side}" for enum in SCREENS for side in ("left", "right")}
    expected_extras = {f"{name}/{side}" for name in EXTRA_VIEWS for side in ("left", "right")}
    missing_screens = sorted(expected_screens - screen_routes)
    missing_extras = sorted(expected_extras - extra_routes)
    if args.coverage == "full":
        errors.extend("Missing screen route: " + route for route in missing_screens)
        errors.extend("Missing extra view route: " + route for route in missing_extras)
    count_names = ("checks", "checks_true", "captures", "png_records_verified", "unique_png_files", "fixture_statuses", "named_fixture_checks")
    counts = {name: sum(report.get("counts", {}).get(name, 0) for report in reports) for name in count_names}
    counts.update({"result_files": len(reports), "required_screen_routes": 34, "required_extra_view_routes": 8,
                   "screen_routes_verified": len(screen_routes), "extra_view_routes_verified": len(extra_routes)})
    verification = {
        "passed": not errors,
        "verified_at_utc": datetime.now(timezone.utc).isoformat(),
        "scope": "saved_screen_coverage_and_artifact_integrity",
        "coverage_mode": args.coverage,
        "normative_mpsuid_accepted": False,
        "counts": counts,
        "sources": [{key: report[key] for key in ("source", "source_sha256", "passed") if key in report} for report in reports],
        "missing_screen_routes": missing_screens,
        "missing_extra_view_routes": missing_extras,
        "errors": errors,
        "notes": ["Sources from independent native processes may be combined; each PNG is verified against its own result directory.",
                  "Coverage, saved assertions and PNG integrity only; pixel semantics/RE compliance/real-time are not certified.",
                  "Fixture statuses are checked if present; absence of fixture assertions does not accept quantitative values."],
    }
    destination.write_text(json.dumps(verification, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"passed": verification["passed"], "verification": str(destination.resolve()),
                      "error_count": len(verification.get("errors", [])),
                      "counts": verification.get("counts", {})}, ensure_ascii=False))
    return 0 if verification["passed"] else 1


if __name__ == "__main__":
    sys.exit(main())
