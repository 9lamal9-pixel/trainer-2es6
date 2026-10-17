#!/usr/bin/env python3
"""Export the imported native course as source data, without executing native code.

The supported input is the SHA-guarded 2019 Voiniovka - Ekat distribution.
This tool writes only its two JSON artifacts under the assigned discovery run.
It does not project the course onto the PDF route, infer occupancy, publish a
live signal, or write trainer state. Unknown scenario fields remain raw.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import struct
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path


COURSE = "RAZMETKA/Voiniovka - Ekat"
STEM = "Tumen - Ekaterinburg"
RUN = "dev/runs/safety-markup-discovery-20261013"
MANIFEST_RUN = "dev/runs/native-distribution-20261013T054100Z/manifests"
EXE_SHA = "e217148c24427cae5dab46309676854c0e73274f79381ae62613d9d63b3c1612"
EXPECTED = {
    "light": 181,
    "profil": 224,
    "pikets": 3273,
    "stan": 27,
    "sautpnt": 464,
    "plan": 177,
    "signs": 381,
    "arrow": 208,
    "pereezd": 18,
    "prep": 18,
    "othobj": 111,
    "ogran": 0,
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def finite(value: float) -> float:
    require(math.isfinite(value), "Non-finite source number")
    return value


def child(base: Path, relative: str) -> Path:
    candidate = (base / relative).resolve()
    require(candidate.is_relative_to(base.resolve()), "Path outside assigned base")
    return candidate


class Reader:
    def __init__(self, data: bytes):
        self.data = data
        self.offset = 0

    def take(self, n: int) -> bytes:
        require(n >= 0 and self.offset + n <= len(self.data), "Truncated source")
        result = self.data[self.offset : self.offset + n]
        self.offset += n
        return result

    def unpack(self, fmt: str):
        values = struct.unpack("<" + fmt, self.take(struct.calcsize("<" + fmt)))
        return values[0] if len(values) == 1 else values

    def unicode_string(self) -> str:
        count = self.unpack("I")
        require(count <= 4096, "Invalid Unicode string length")
        return self.take(count * 2).decode("utf-16le", errors="strict")

    def done(self) -> None:
        require(self.offset == len(self.data), "Unconsumed binary source bytes")


class PEReader:
    """Small read-only PE/RVA reader; no native library is loaded."""

    def __init__(self, data: bytes):
        self.data = data
        require(data[:2] == b"MZ", "Expected PE executable metadata")
        pe = struct.unpack_from("<I", data, 0x3C)[0]
        require(data[pe : pe + 4] == b"PE\0\0", "Invalid PE signature")
        machine, count = struct.unpack_from("<HH", data, pe + 4)
        require(machine == 0x14C, "Expected i386 source build")
        optional_size = struct.unpack_from("<H", data, pe + 20)[0]
        optional = pe + 24
        require(struct.unpack_from("<H", data, optional)[0] == 0x10B, "Expected PE32")
        self.image_base = struct.unpack_from("<I", data, optional + 28)[0]
        self.header_size = struct.unpack_from("<I", data, optional + 60)[0]
        self.sections = []
        for index in range(count):
            at = optional + optional_size + 40 * index
            virtual_size, rva, raw_size, raw_at = struct.unpack_from("<IIII", data, at + 8)
            self.sections.append((rva, virtual_size, raw_at, raw_size))

    def at(self, rva: int, size: int) -> bytes:
        if rva < self.header_size:
            require(rva + size <= self.header_size, "PE header read overrun")
            return self.data[rva : rva + size]
        for section_rva, virtual_size, raw_at, raw_size in self.sections:
            if section_rva <= rva < section_rva + max(virtual_size, raw_size):
                relative = rva - section_rva
                require(relative + size <= raw_size, "PE read outside on-disk section")
                return self.data[raw_at + relative : raw_at + relative + size]
        raise ValueError("Unmapped source RVA: " + hex(rva))

    def enum(self, kind_rva: int, expected_name: str, expected_count: int) -> dict:
        data = self.at(kind_rva, 1024)
        require(data[0] == 3, "Expected Delphi tkEnumeration")
        name_length = data[1]
        name = data[2 : 2 + name_length].decode("ascii")
        require(name == expected_name, "Unexpected native enum name")
        at = 2 + name_length
        ordinal_type = data[at]
        minimum, maximum, base_type_pointer = struct.unpack_from("<iiI", data, at + 1)
        at += 13
        require(maximum - minimum + 1 == expected_count, "Native enum count changed")
        names = []
        for value in range(minimum, maximum + 1):
            length = data[at]
            at += 1
            member = data[at : at + length].decode("ascii")
            at += length
            names.append({"value": value, "name": member})
        return {
            "native_name": name,
            "rtti_kind_rva": f"0x{kind_rva:08X}",
            "ordinal_type": ordinal_type,
            "base_type_pointer": f"0x{base_type_pointer:08X}",
            "values": names,
            "rtti_bytes": data[:at].hex(),
        }


class SourceSet:
    def __init__(self, workspace: Path):
        self.workspace = workspace
        self.pack = workspace / "work/native_distribution/WORK_TRENAGER_3D"
        self.course = child(self.pack, COURSE)
        self.manifest_path = workspace / MANIFEST_RUN / "WORK_TRENAGER_3D.json"
        manifest_bytes = self.manifest_path.read_bytes()
        manifest = json.loads(manifest_bytes.decode("utf-8-sig"))
        self.manifest_sha = sha(manifest_bytes)
        self.entries = {row["path"]: row for row in manifest["files"]}
        self.sources = {}

    def metadata(self, relative: str, *, read: bool = False):
        path = child(self.pack, relative)
        require(relative in self.entries, "Source absent from imported manifest: " + relative)
        expected = self.entries[relative]
        require(path.stat().st_size == expected["bytes"], "Source size changed: " + relative)
        if not read and relative in self.sources:
            return self.sources[relative], None
        if read:
            data = path.read_bytes()
            digest = sha(data)
            require(digest == expected["sha256"].lower(), "Source SHA changed: " + relative)
            hash_status = "measured_now_and_matches_import_manifest"
        else:
            data = None
            digest = expected["sha256"].lower()
            hash_status = "import_manifest_only_content_not_read"
        meta = {
            "path": path.as_posix(),
            "workspace_relative_path": path.relative_to(self.workspace).as_posix(),
            "import_manifest_relative_path": relative,
            "bytes": expected["bytes"],
            "sha256": digest,
            "hash_status": hash_status,
        }
        self.sources[relative] = meta
        return meta, data

    def course_data(self, extension: str):
        return self.metadata(f"{COURSE}/{STEM}.{extension}", read=True)


def source_section(meta: dict, count: int, records: list, consumed: int, **extra) -> dict:
    return {
        "source": meta,
        "count": count,
        "full_consumed_size": consumed,
        "records": records,
        **extra,
    }


def text_lines(data: bytes):
    raw_lines = data.decode("cp1251", errors="strict").splitlines(keepends=True)
    lines, offsets = [], []
    offset = 0
    for raw in raw_lines:
        lines.append(raw.rstrip("\r\n"))
        offsets.append(offset)
        offset += len(raw.encode("cp1251"))
    require(offset == len(data), "Text source not fully consumed")
    return lines, offsets


def fixed_text(data: bytes, *, header_lines: int, width: int, expected: int):
    lines, offsets = text_lines(data)
    count = int(lines[header_lines - 1])
    require(count == expected, "Unexpected text record count")
    require(len(lines) == header_lines + count * width, "Unexpected text record width/tail")
    groups = []
    for index in range(count):
        start = header_lines + width * index
        stop = start + width
        groups.append({
            "index": index,
            "line_start": start + 1,
            "byte_offset": offsets[start],
            "byte_length": (offsets[stop] if stop < len(lines) else len(data)) - offsets[start],
            "raw_fields": lines[start:stop],
        })
    return count, groups, lines[:header_lines]


def binary_header(reader: Reader, expected_count: int, *, byte_version: bool = False):
    require(reader.take(6) == "New".encode("utf-16le"), "Unknown binary source magic")
    version = reader.unpack("B" if byte_version else "I")
    require(version == 1, "Unsupported binary header version")
    count = reader.unpack("I")
    require(count == expected_count, "Unexpected binary record count")
    return {"magic": "New", "version": version, "count": count,
            "header_bytes": reader.offset, "endianness": "little"}


def native_schema(pe: PEReader, exe_meta: dict) -> dict:
    enums = {
        "light_type": pe.enum(0x2422C8, "TLightType", 14),
        "lens_type": pe.enum(0x242388, "TLensType", 18),
        "light_state": pe.enum(0x242534, "TStateLight", 26),
        "autoblocking": pe.enum(0x24273C, "TAutoBloked", 3),
        "availability": pe.enum(0x242790, "TLighterAvalibe", 3),
        "direction": pe.enum(0x165C8C, "TDirection", 2),
        "saut_target_type": pe.enum(0x164ADC, "TSAUTType", 20),
    }
    encoder_start = pe.at(0x24AF20, 50)
    require(encoder_start.hex() ==
            "558bec83c4f88855fb8945fcc645fa008b45fc807816000f842301000033c08a45fb83f8190f8715010000ff248552af6400",
            "Native SendCode/state encoder byte guard changed")
    table = struct.unpack("<26I", pe.at(0x24AF52, 104))
    code_map = []
    for state, target_va in enumerate(table):
        target_rva = target_va - pe.image_base
        branch = pe.at(target_rva, 4)
        require(branch[:3] == bytes.fromhex("c645fa"), "Unexpected native code encoder branch")
        code_map.append({"state": state,
                         "state_name": enums["light_state"]["values"][state]["name"],
                         "code_way": branch[3],
                         "branch_rva": f"0x{target_rva:08X}",
                         "guard_bytes": branch.hex()})
    proofs = [
        ("TLighterBasis.LoadFromStream", 0x24B114, 80,
         "Base version i32, type u8, lens u8, state u8, length-prefixed UTF16 name, FLAvalibe u8, FAB u8, FSendCode u8."),
        ("TLighterBasis named fields", 0x24A58A, 246,
         "FLType+04, FLensType+05, FCurState+06, FLName+08, FLAvalibe+0C, FAB+0D, FSendCode+16."),
        ("TLighterTren.LoadFromStream", 0x24C4B0, 103,
         "Base loader followed by extension version u8, FID i32, FCord i32, FBadWay u8."),
        ("TLighterTren named fields", 0x24B5BC, 90,
         "FID+1C, FCord+20, FDefaultStLight+24, FBadWay+30."),
        ("TLighterBasis.LighterCod property", 0x24AB0F, 49,
         "Computed virtual getter slot 0C, not a serialized code byte."),
        ("TLighterBasis.GetCurrentCode", 0x24B068, 55,
         "Reads FCurState+06 and calls virtual state encoder slot08."),
        ("TLighterTren state encoder", 0x24C1F8, 36,
         "Calls base encoder24AF20."),
        ("TLighterTren.GetCurrentCode", 0x24C21C, 64,
         "Further runtime nonstandard/fault state overrides exist; they are not reimplemented here."),
        ("TProfE RTTI", 0x1DB0F4, 79,
         "12-byte record; Nach f32+00, Len f32+04, Uklon f32+08."),
        ("TProfE.LoadFromStream", 0x1DCBF8, 81,
         "Reads three 4-byte fields into record offsets00/04/08."),
        ("TPicetBaseClass named fields", 0x279D60, 89,
         "FKM i32+04, FPicet i32+08, FOffset i32+0C, FNaprav TDirection+10, FMeterPlus i32+14."),
        ("TPicetBaseClass.LoadFromProfil", 0x27AC78, 110,
         "Per-entry version i32; version>=1 reads KM, Picet, Offset i32 then Direction byte."),
        ("TPicetManagerBaseClass.GetPicMeter", 0x27B208, 130,
         "Brackets the supplied Meter against FOffset; FMeterPlus = Meter - FOffset."),
        ("TPicetBaseClass.RealKMDbl", 0x27AC38, 64,
         "This exact source build computes KM + Picet/100 + MeterPlus/1000. Export preserves raw values; no silent correction."),
        ("TStationE named fields", 0x1DBA8C, 291,
         "Name+04, Nach+38, IdVhodLight+3C, IdVyhodLight+40, ID+4C, IdVhodArrow+50, IdVyhodArrow+54, WayNumber+58, WayTyp+64."),
        ("Native text .stan loader", 0x26533C, 572,
         "Reads header and count then8 lines: Name, Nach, IdVhodLight, IdVyhodLight, IdVhodArrow, IdVyhodArrow, WayNumber, WayTyp. Generates ID from index; light meters come from linked FCord."),
        ("TSAUT_PointE RTTI", 0x1DB674, 108,
         "SP_Type TSAUTType+00, Meter i32+01, Tag i32+05; remaining runtime fields not supplied by the3-column text file."),
        ("TPlanE RTTI", 0x1DB1E4, 84,
         "Nach, Long, Radius, Hkr are four i32 fields."),
        ("TSignE RTTI", 0x1DB4F4, 111,
         "id i32+00, frame i32+04, meter f32+08, parameter i32+0C, Direction enum+10; legacy text contains only first4 fields."),
        ("TCurLighterRec RTTI", 0x242838, 225,
         "Size3B. Source fields Index+00/Name+04/Xlight+19/XlastLight+1D/SendCode+21. Dist+23/Uklon+27/DeltaS+2B/Vpr+2F/Vcel+37 are separate derived fields."),
        ("Native current-light publisher", 0x2E322C, 213,
         "Current TLighterTren.FID+1C -> Index; FLName+08 -> Name; FCord+20 -> Xlight; getter262574 -> XlastLight; FSendCode+16 -> SendCode. BlocUch+22 is zeroed in this routine."),
        ("Native previous-light coordinate", 0x262574, 80,
         "Looks up current light in collection, gets preceding item.FCord+20. Returns0 if first item or no current light."),
        ("Native current-light code wrapper", 0x261BE8, 49,
         "Current pointer+80; calls virtual LighterCod getter slot0C. Defaults0 if no current light."),
    ]
    return {
        "primary_source": exe_meta,
        "method": "static_on_disk_PE_RTTI_and_machine_code; no native execution",
        "native_image_base": f"0x{pe.image_base:08X}",
        "enums": enums,
        "basic_state_to_code_way": {
            "encoder_rva": "0x0024AF20", "jump_table_rva": "0x0024AF52",
            "guard_bytes": encoder_start.hex(), "send_code_false_result": 0,
            "states": code_map,
            "applicability": "Clean TLighterBasis states only. Runtime fault overrides, current-light selection, restrictive between-object logic and direction gates remain separate native responsibilities.",
        },
        "proofs": [{"name": name, "rva": f"0x{rva:08X}",
                    "bytes": pe.at(rva, size).hex(), "interpretation": explanation}
                   for name, rva, size, explanation in proofs],
        "signal_publisher_evidence": {
            "native_curr_light_wrapper_rva": "0x00261BE8",
            "wrapper_contract": "Current TLighterTren pointer+80; calls virtual LighterCod getter slot0C.",
            "course_current_light_publisher_rva_range": ["0x002E3200", "0x002E33C0"],
            "published_source_fields": ["Xlight", "XlastLight", "SendCode"],
            "current_light_publisher_rva": "0x002E322C",
            "source_field_offsets": {"Index": "0x00", "Name": "0x04", "Xlight": "0x19",
                                     "XlastLight": "0x1D", "SendCode": "0x21"},
            "source_contract": {"Index": "TLighterTren.FID, not a fabricated counter",
                                "Xlight": "Current TLighterTren.FCord",
                                "XlastLight": "Previous collection item.FCord;0 at first item",
                                "SendCode": "Current TLighterTren.FSendCode"},
            "derived_fields_excluded_from_source_contract": {
                "Dist": "0x23", "Uklon": "0x27", "DeltaS": "0x2B", "Vpr": "0x2F", "Vcel": "0x37"},
            "general_scen_net_fields": {"FCurrLightParam": "0x40", "FVideoRazm": "0x7B", "FCodeWay": "0x7C"},
            "main_code_way_publisher_rva": "0x003E3B68",
            "main_code_way_contract": "Direction/Reversor-gated FCodeWay/FCodeOdd is written to InputParam+1E, not to a derived protection output.",
            "main_current_alsn_writer_rva": "0x003E3B24",
            "current_alsn_contract": "Writes InputParam+1D from its argument. The actual upstream ALSN encoder/source has not been proved by this reader.",
            "code_way_is_not_always_final_alsn_color": True,
        },
        "corrections_to_earlier_inferences": [
            "The first .light post-name byte is FLAvalibe, not rawALSN or codeByte.",
            "The second .light post-name byte is FAB/TAutoBloked, not a Boolean availability flag.",
            "The first .pikets integer is per-record version, not a track identifier.",
            "The fourth .pikets integer is FOffset in native Meter context, not video-frame index.",
            "TProfE field names are now confirmed by RTTI/load code; Uklon unit still needs a primary unit definition.",
        ],
    }


def enum_lookup(schema: dict, key: str):
    return {row["value"]: row["name"] for row in schema["enums"][key]["values"]}


def lights(source: SourceSet, schema: dict):
    meta, data = source.course_data("light")
    reader = Reader(data)
    header = binary_header(reader, EXPECTED["light"], byte_version=True)
    lookups = {key: enum_lookup(schema, key) for key in
               ("light_type", "lens_type", "light_state", "availability", "autoblocking")}
    codes = {row["state"]: row["code_way"] for row in schema["basic_state_to_code_way"]["states"]}
    records = []
    for index in range(header["count"]):
        start = reader.offset
        base_version = reader.unpack("i")
        require(base_version == 1, "Unsupported TLighterBasis stream version")
        light_type, lens, state = reader.unpack("BBB")
        name = reader.unicode_string()
        availability, autoblocking, send = reader.unpack("BBB")
        extension_version, identifier, meter, bad_way = reader.unpack("BiiB")
        require(extension_version == 1, "Unsupported TLighterTren extension version")
        for key, value in [("light_type", light_type), ("lens_type", lens), ("light_state", state),
                           ("availability", availability), ("autoblocking", autoblocking)]:
            require(value in lookups[key], "Unknown native enum value")
        require(send in (0, 1) and bad_way in (0, 1), "Invalid serialized Boolean")
        records.append({
            "index": index, "byte_offset": start, "byte_length": reader.offset - start,
            "base_version": base_version, "extension_version": extension_version,
            "id": identifier, "meter": meter, "name": name,
            "type": light_type, "type_name": lookups["light_type"][light_type],
            "lens": lens, "lens_name": lookups["lens_type"][lens],
            "state": state, "state_name": lookups["light_state"][state],
            "availability": availability, "availability_name": lookups["availability"][availability],
            "autoblocking": autoblocking, "autoblocking_name": lookups["autoblocking"][autoblocking],
            "send_code": bool(send), "bad_way": bool(bad_way),
            "native_basic_code_way": codes[state] if send else 0,
            "raw_values": [base_version, light_type, lens, state, name, availability,
                           autoblocking, send, extension_version, identifier, meter, bad_way],
            "raw_bytes": data[start:reader.offset].hex(),
        })
    reader.done()
    require(len({row["id"] for row in records}) == len(records), "Duplicate light IDs")
    require([row["id"] for row in records] == list(range(len(records))), "Nonsequential light IDs")
    require(all(a["meter"] <= b["meter"] for a, b in zip(records, records[1:])), "Unsorted light meters")
    return source_section(meta, len(records), records, reader.offset, header=header,
                          raw_unknown_fields=[],
                          state_counts=dict(Counter(str(row["state"]) for row in records)),
                          code_semantics="native_basic_code_way is derived from the SHA-guarded native state encoder; no raw ALSN/code byte is stored in this file.")


def profiles(source: SourceSet):
    meta, data = source.course_data("profil")
    reader = Reader(data)
    header = binary_header(reader, EXPECTED["profil"])
    rows = []
    for index in range(header["count"]):
        at = reader.offset
        values = [finite(value) for value in reader.unpack("fff")]
        require(values[1] > 0, "Nonpositive native profile length")
        rows.append({"index": index, "byte_offset": at, "byte_length": 12,
                     "Nach": values[0], "Len": values[1], "Uklon": values[2],
                     "raw_tuple": values, "raw_bytes": data[at:reader.offset].hex()})
    reader.done()
    require(all(a["Nach"] <= b["Nach"] for a, b in zip(rows, rows[1:])), "Unsorted profile starts")
    return source_section(meta, len(rows), rows, reader.offset, header=header,
                          native_record="TProfE", native_fields_confirmed=True,
                          units={"Nach_and_Len": "Native route Meter/length context", "Uklon": "Unit not established from RTTI alone"},
                          extent=[rows[0]["Nach"], max(r["Nach"] + r["Len"] for r in rows)])


def pikets(source: SourceSet, schema: dict):
    meta, data = source.course_data("pikets")
    reader = Reader(data)
    header = binary_header(reader, EXPECTED["pikets"])
    directions = enum_lookup(schema, "direction")
    rows = []
    for index in range(header["count"]):
        at = reader.offset
        version, km, picket, offset, direction = reader.unpack("iiiiB")
        require(version == 1 and direction in directions, "Unsupported picket entry")
        rows.append({"index": index, "byte_offset": at, "byte_length": 17,
                     "record_version": version, "KM": km, "Picet": picket,
                     "Offset": offset, "Direction": direction,
                     "Direction_name": directions[direction],
                     "raw_tuple": [version, km, picket, offset, direction],
                     "raw_bytes": data[at:reader.offset].hex()})
    reader.done()
    anomalies = [{"previous_index": a["index"], "previous_offset": a["Offset"],
                  "index": b["index"], "offset": b["Offset"]}
                 for a, b in zip(rows, rows[1:]) if a["Offset"] >= b["Offset"]]
    ordered_prefix_count = anomalies[0]["index"] if anomalies else len(rows)
    return source_section(meta, len(rows), rows, reader.offset, header=header,
                          native_record="TPicetBaseClass.LoadFromProfil",
                          offset_semantics="FOffset used by native GetPicMeter against supplied Meter; not video frame",
                          offsets_strictly_increasing=not anomalies,
                          offset_order_anomalies=anomalies,
                          ordered_prefix_count=ordered_prefix_count,
                          ordered_prefix_last_offset=rows[ordered_prefix_count - 1]["Offset"],
                          anomaly_policy="Preserve original order and all records. Do not sort, invent the terminal offset or silently discard its record. Native end-of-course bracketing needs separate verification.",
                          km_display_policy="Raw KM/Picet/Direction exported. Standard km+picket/10 projection and the native RealKMDbl getter are not silently substituted for each other.")


def stations(source: SourceSet, light_rows: list):
    meta, data = source.course_data("stan")
    count, rows, header = fixed_text(data, header_lines=2, width=8, expected=EXPECTED["stan"])
    light_by_id = {row["id"]: row for row in light_rows}
    for row in rows:
        f = row["raw_fields"]
        row.update({"id": row["index"], "name": f[0], "meter": int(f[1]),
                    "IdVhodLight": int(f[2]), "IdVyhodLight": int(f[3]),
                    "IdVhodArrow": int(f[4]), "IdVyhodArrow": int(f[5]),
                    "WayNumber": f[6], "WayTyp": int(f[7])})
        for field in ("IdVhodLight", "IdVyhodLight"):
            identifier = row[field]
            require(identifier == -1 or identifier in light_by_id, "Invalid station light reference")
            row[field + "_meter"] = None if identifier == -1 else light_by_id[identifier]["meter"]
        for field in ("IdVhodArrow", "IdVyhodArrow"):
            require(-1 <= row[field] < EXPECTED["arrow"], "Invalid station arrow reference")
    return source_section(meta, count, rows, len(data), raw_header=header,
                          native_loader_rva="0x0026533C", schema_confirmed=True)


def saut_points(source: SourceSet, schema: dict, light_rows: list):
    meta, data = source.course_data("sautpnt")
    count, rows, header = fixed_text(data, header_lines=2, width=3, expected=EXPECTED["sautpnt"])
    types = enum_lookup(schema, "saut_target_type")
    light_by_id = {row["id"]: row for row in light_rows}
    linked = []
    for row in rows:
        typ, meter, tag = map(int, row["raw_fields"])
        require(typ in types, "Unknown SAUT target type")
        row.update({"SP_Type": typ, "SP_Type_name": types[typ], "Meter": meter, "Tag": tag})
        if typ == 0:
            require(tag in light_by_id, "SAUT target has nonexistent light ID")
            require(meter == light_by_id[tag]["meter"], "SAUT light target meter mismatch")
            row["linked_light_id"] = tag
            row["linked_light_name"] = light_by_id[tag]["name"]
            linked.append(tag)
    require(sorted(linked) == sorted(light_by_id), "SAUT targets do not cover each light exactly once")
    return source_section(meta, count, rows, len(data), raw_header=header,
                          native_record="TSAUT_PointE", light_target_links_validated=len(linked),
                          type_counts=dict(Counter(str(row["SP_Type"]) for row in rows)),
                          tag_semantics="Light_ST Tag is corroborated as light ID by all181 exact meter/ID links. Other types retain native raw Tag; not converted into speed permissions.")


def additional_text_tables(source: SourceSet):
    result = {}
    for extension, header_lines, width in [
        ("plan", 2, 4), ("signs", 1, 4), ("arrow", 2, 8),
        ("pereezd", 2, 2), ("prep", 2, 4), ("othobj", 2, 6),
    ]:
        meta, data = source.course_data(extension)
        count, rows, header = fixed_text(data, header_lines=header_lines, width=width,
                                         expected=EXPECTED[extension])
        section = source_section(meta, count, rows, len(data), raw_header=header)
        if extension == "plan":
            for row in rows:
                row.update(dict(zip(("Nach", "Long", "Radius", "Hkr"), map(int, row["raw_fields"]))))
            section["native_record"] = "TPlanE"
            section["schema_status"] = "Native field names/types confirmed by RTTI; text column order matches these4 fields; text loader not independently decoded."
        elif extension == "signs":
            for row in rows:
                f = row["raw_fields"]
                row.update({"id": int(f[0]), "frame": int(f[1]), "meter": finite(float(f[2])),
                            "parameter": int(f[3])})
            section["native_record"] = "TSignE"
            section["schema_status"] = "Native field names/types confirmed by RTTI; text column order has not been independently traced. Direction is absent from this legacy text format."
            section["speed_limit_semantics_confirmed"] = False
            section["id_counts"] = dict(Counter(str(row["id"]) for row in rows))
        else:
            section["schema_status"] = "Fixed record width/count validated; individual field semantics preserved as raw until loader proof."
        result[extension] = section
    return result


def scenarios(source: SourceSet, light_rows: list):
    prefix = COURSE + "/SCENDATA/"
    scenario_names = sorted(row[len(prefix):] for row in source.entries
                            if row.startswith(prefix) and "/" not in row[len(prefix):]
                            and row.lower().endswith(".scen"))
    result = []
    for name in scenario_names:
        meta, data = source.metadata(prefix + name, read=True)
        lines, offsets = text_lines(data)
        require(len(lines) >= 10, "Truncated .scen header")
        count = int(lines[9])
        require(count == len(light_rows), "Scenario light count mismatch")
        limit = 10 + count * 6
        require(limit <= len(lines), "Truncated scenario light section")
        groups = []
        for index in range(count):
            start = 10 + index * 6
            groups.append({"index": index, "line_start": start + 1,
                           "byte_offset": offsets[start], "raw_fields": lines[start:start + 6]})
        second_column = [int(row["raw_fields"][1]) for row in groups]
        result.append({
            "source": meta, "full_read_size": len(data), "text_line_count": len(lines),
            "raw_header": lines[:10], "light_count": count,
            "raw_light_records": groups, "light_record_width_lines": 6,
            "second_light_column_matches_definition_states": second_column == [r["state"] for r in light_rows],
            "remaining_line_start": limit + 1,
            "remaining_raw_lines": lines[limit:],
            "fully_preserved": True, "schema_confirmed": False,
            "usage_limit": "Header and light-count/6-line shape validated. Field meanings and remaining sections await native scenario-loader proof; this is not occupancy or a live signal-state source.",
        })
    binary = []
    for extension in ("defscen", "iscen"):
        meta, data = source.course_data(extension)
        version = struct.unpack_from("<I", data, 0)[0]
        details = {"source": meta, "version_candidate": version,
                   "full_read_size": len(data), "header_hex": data[:64].hex(),
                   "schema_confirmed": False, "decoded_bytes": 4}
        if extension == "defscen":
            count = struct.unpack_from("<I", data, 4)[0]
            require(version == 1 and count == len(light_rows), "Unexpected defscen prefix")
            details.update({"light_count_prefix": count, "decoded_bytes": 8})
        else:
            reader = Reader(data)
            reader.unpack("I")
            details.update({"course_name_prefix": reader.unicode_string(), "decoded_bytes": reader.offset})
        details["unparsed_bytes"] = len(data) - details["decoded_bytes"]
        binary.append(details)
    info_meta, _ = source.metadata(prefix + "ScenData.info", read=True)
    return {"text": result, "binary_metadata": binary, "scenario_catalog_metadata": info_meta}


def manifest_metadata(source: SourceSet):
    meta, data = source.course_data("nastr")
    lines, _ = text_lines(data)
    require(len(lines) == 14, "Unexpected .nastr record count")
    dependencies = []
    for line_number, name in enumerate(lines[2:], 3):
        require(Path(name).name == name and name.startswith(STEM + "."), "Unsafe course dependency")
        dependency, _ = source.metadata(COURSE + "/" + name)
        dependencies.append({"line": line_number, "name": name, "source": dependency})
    project_meta, project_data = source.course_data("vidproj")
    project_lines, _ = text_lines(project_data)
    stats = []
    for index in range(13, 35, 2):
        stats.append({"label_line": index + 1, "label": project_lines[index],
                      "count_line": index + 2, "count": int(project_lines[index + 1])})
    require(project_lines[1] == "PRVID_002", "Unexpected video project version")
    return {
        "source": meta, "full_consumed_size": len(data), "course_name": lines[1],
        "dependencies": dependencies,
        "video_project": {"source": project_meta, "full_consumed_size": len(project_data),
                          "version": project_lines[1], "raw_lines": project_lines,
                          "declared_statistics": stats,
                          "known_stale_statistic": "Project profile count219; actual parsed .profil count224."},
    }


def mirror_metadata(source: SourceSet):
    mirrors = []
    relative = "DATA/" + COURSE + "/"
    manifest_path = source.workspace / MANIFEST_RUN / "WORLD3DSERVER.json"
    raw = manifest_path.read_bytes()
    manifest = json.loads(raw.decode("utf-8-sig"))
    for row in manifest["files"]:
        if row["path"].startswith(relative) and "/" not in row["path"][len(relative):]:
            source_relative = row["path"][5:]
            original = source.entries.get(source_relative)
            mirrors.append({"relative_path": row["path"], "bytes": row["bytes"],
                            "sha256": row["sha256"].lower(),
                            "matches_work_pack_manifest": bool(original and original["bytes"] == row["bytes"]
                                                                   and original["sha256"].lower() == row["sha256"].lower())})
    return {"manifest_path": manifest_path.as_posix(), "manifest_sha256": sha(raw),
            "verification": "Metadata comparison only; mirrored source contents were not read this run.",
            "course_files": mirrors}


def sign_library_headers(source: SourceSet):
    """Read only the selected library's names/IDs; seek past embedded PNG blobs."""
    relative = "DATA/LIB/SIGNALS/SigalsLiberty.dat"
    meta, _ = source.metadata(relative)
    path = child(source.pack, relative)
    headers = []
    digest = hashlib.sha256()
    bytes_read = 0
    with path.open("rb") as stream:
        def read_exact(size: int):
            nonlocal bytes_read
            raw = stream.read(size)
            require(len(raw) == size, "Truncated sign-library header")
            digest.update(raw)
            bytes_read += size
            return raw

        header = read_exact(8)
        raw_header_values = list(struct.unpack("<II", header))
        require(raw_header_values == [36, 36], "Unreviewed sign-library header")
        for index in range(raw_header_values[1]):
            start = stream.tell()
            length = struct.unpack("<I", read_exact(4))[0]
            require(length <= 256, "Invalid sign-library name length")
            name = read_exact(length * 2).decode("utf-16le", errors="strict")
            identifier, raw_flag, png_bytes = struct.unpack("<iBI", read_exact(9))
            png_offset = stream.tell()
            require(png_offset + png_bytes <= meta["bytes"], "Invalid embedded image span")
            headers.append({"index": index, "byte_offset": start, "name": name,
                            "id": identifier, "raw_flag": raw_flag,
                            "skipped_image_byte_offset": png_offset, "skipped_image_bytes": png_bytes})
            stream.seek(png_bytes, 1)
        accounted = stream.tell()
        require(accounted == meta["bytes"], "Unaccounted sign-library tail")
    require([row["id"] for row in headers] == list(range(36)), "Nonsequential sign-library IDs")
    return {"source": meta, "raw_header_values": raw_header_values, "count": len(headers),
            "records": headers, "bytes_read": bytes_read,
            "full_source_spans_accounted": accounted,
            "embedded_image_bytes_read": 0, "selected_header_bytes_sha256": digest.hexdigest(),
            "schema_status": "Header layout/count/ID/name spans verified; original native library loader not independently decoded. raw_flag semantics unestablished.",
            "legacy_sign_id_mapping_confirmed": False,
            "usage_limit": "This library names id8 as automatic train brake control and id9 as lowering the pantograph. A legacy course id8/parameter40 is not automatically a speed limit. Library/course remapping must be proved separately."}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--workspace", type=Path, default=Path(__file__).resolve().parents[2])
    args = parser.parse_args()
    workspace = args.workspace.resolve()
    source = SourceSet(workspace)
    exe_meta, exe_data = source.metadata("Trenager_3D.exe", read=True)
    require(exe_meta["sha256"] == EXE_SHA, "Unreviewed native source build")
    schema = native_schema(PEReader(exe_data), exe_meta)
    light_data = lights(source, schema)
    profile_data = profiles(source)
    picket_data = pikets(source, schema)
    station_data = stations(source, light_data["records"])
    saut_data = saut_points(source, schema, light_data["records"])
    other_data = additional_text_tables(source)
    scenario_data = scenarios(source, light_data["records"])
    manifest_data = manifest_metadata(source)
    sign_library_data = sign_library_headers(source)
    ogr_meta, ogr_bytes = source.course_data("ogran")
    ogr_reader = Reader(ogr_bytes)
    ogr_header = binary_header(ogr_reader, EXPECTED["ogran"])
    ogr_reader.done()
    destination = child(workspace, RUN)
    destination.mkdir(parents=True, exist_ok=True)
    generation = datetime.now(timezone.utc).isoformat()
    reader_path = Path(__file__).resolve()
    identity = {"path": reader_path.as_posix(), "sha256": sha(reader_path.read_bytes())}
    course = {
        "schema_version": 1, "artifact_kind": "native_course_source_data",
        "generated_utc": generation, "reader": identity,
        "course": {"name": manifest_data["course_name"], "source_directory": source.course.as_posix(),
                   "coordinate_axis": "Native route Meter/FCord/FOffset axis, increasing toward Екатеринбург",
                   "same_as_current_pdf_axis": False,
                   "koksharovsky_station_meter": next(r["meter"] for r in station_data["records"]
                                                        if r["name"] == "Кокшаровский"),
                   "live_occupancy_source_available": False, "raw_current_alsn_source_proved": False},
        "manifest": manifest_data, "lights": light_data, "profile": profile_data,
        "pikets": picket_data, "stations": station_data, "saut_points": saut_data,
        "additional_tables": other_data,
        "sign_library_header_metadata": sign_library_data,
        "persistent_limits": {"source": ogr_meta, "header": ogr_header,
                              "full_consumed_size": ogr_reader.offset, "count": 0, "records": []},
        "scenarios": scenario_data,
        "schema_evidence_file": "schema-evidence.json",
        "limitations": [
            "Source files are data. No instructions embedded in them are executed.",
            "The course starts at Тюмень-депо and reaches Кокшаровский at191500 native meters. It does not supply Войновка coverage or a proved projection onto Voynovka_Koshkarovsky_route.json.",
            "Saved light states are initial course data; occupancy and instructor state changes require an explicit separate source. They are not live hardware measurements.",
            "Basic CodeWay is computed from state and SendCode by the guarded source encoder. Runtime faults, BadWay behavior and restrictive-between-object logic are not simulated by this reader.",
            "No stored codeByte/rawALSN exists in .light. CurrentALSN InputParam+1D requires a separate upstream source proof.",
            "The .ogran count is0. Sign IDs/parameters are exported as source values; start/end speed-limit semantics are not asserted.",
            "Profile Uklon units and exact locomotive/manual revision remain unestablished.",
            "Picket record3272 has terminal Offset0 after record3271 Offset327120. The3272-record prefix is ordered; the anomalous final record is preserved. No terminal-offset repair is inferred.",
            "No native DLL/EXE is run, no current trainer/model/protection outputs are written, and no canonical runtime course is copied or changed.",
        ],
    }
    evidence = {
        "schema_version": 1, "generated_utc": generation, "reader": identity,
        "native_schema": schema,
        "import_manifest": {"path": source.manifest_path.as_posix(), "sha256": source.manifest_sha},
        "mirror_comparison": mirror_metadata(source),
        "counts": {"lights": light_data["count"], "stations": station_data["count"],
                   "profiles": profile_data["count"], "pikets": picket_data["count"],
                   "saut_targets": saut_data["count"], "linked_light_targets": saut_data["light_target_links_validated"],
                   "persistent_limits": 0, **{key: val["count"] for key, val in other_data.items()}},
        "checks": {"all_read_sources_match_import_sha": True,
                   "all_supported_binary_formats_fully_consumed": True,
                   "all_fixed_text_table_counts_and_widths_valid": True,
                   "native_encoder_26_states_guarded": True,
                   "saut_light_targets_exactly_cover_light_id_and_meter": True,
                   "station_light_and_arrow_references_valid": True,
                   "piket_offsets_strictly_increasing": picket_data["offsets_strictly_increasing"],
                   "native_execution_performed": False,
                   "trainer_runtime_or_config_modified": False},
        "source_inventory": list(source.sources.values()),
        "course_artifact_file": "native-course-inputs.json",
    }
    for filename, document in [("native-course-inputs.json", course), ("schema-evidence.json", evidence)]:
        target = child(destination, filename)
        target.write_text(json.dumps(document, ensure_ascii=False, indent=2, allow_nan=False) + "\n", encoding="utf-8")
    print(json.dumps({"ok": True, "files": [str(destination / "native-course-inputs.json"),
                                                str(destination / "schema-evidence.json")],
                      "counts": evidence["counts"], "native_execution_performed": False}, ensure_ascii=False))


if __name__ == "__main__":
    main()
