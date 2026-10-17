"""Rebuild the Windows x64 Python analysis supplement from verified upstream files."""
import argparse
import hashlib
import json
import pathlib
import subprocess
import sys
import urllib.parse
import urllib.request
import zipfile

PYTHON_VERSION = "3.13.16"
PYTHON_SHA256 = "97dae5274cc54867065e8d5a3226e48c35017ed332a0fdb0e27d5b5821961297"
PYTHON_URL = "https://www.python.org/ftp/python/3.13.16/python-3.13.16-embed-amd64.zip"
PYTHON_RELEASE = "https://www.python.org/downloads/release/python-31316/"
PACKAGES = {"pefile": "2024.8.26", "capstone": "5.0.6", "pip": "26.2.1"}
ASSET_NAME = "trainer-python-tools-001.zip"


def sha(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def fetch(url, target):
    if urllib.parse.urlparse(url).scheme != "https":
        raise ValueError("Only HTTPS downloads are allowed")
    request = urllib.request.Request(url, headers={"User-Agent": "trainer-portable-python/1"})
    with urllib.request.urlopen(request, timeout=120) as response:
        parsed = urllib.parse.urlparse(response.geturl())
        if parsed.scheme != "https" or parsed.hostname not in {"www.python.org", "pypi.org", "files.pythonhosted.org"}:
            raise ValueError("Unexpected upstream download host")
        with target.open("xb") as stream:
            for block in iter(lambda: response.read(1024 * 1024), b""):
                stream.write(block)


def run(args, cwd=None):
    result = subprocess.run(args, check=True, text=True, capture_output=True, cwd=cwd)
    return result.stdout.strip()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    root = pathlib.Path(__file__).resolve().parents[2]
    out = pathlib.Path(args.out).resolve()
    if root not in out.parents or out.exists():
        raise ValueError("Use a new output directory inside this workspace")
    out.mkdir(parents=True)
    downloads = out / "downloads"
    downloads.mkdir()
    runtime = out / "stage/work/portable_python"
    runtime.mkdir(parents=True)
    embed = downloads / pathlib.PurePosixPath(urllib.parse.urlparse(PYTHON_URL).path).name
    print("DOWNLOAD official CPython " + PYTHON_VERSION, flush=True)
    fetch(PYTHON_URL, embed)
    if sha(embed) != PYTHON_SHA256:
        raise ValueError("Official Python SHA256 mismatch")
    with zipfile.ZipFile(embed) as archive:
        if archive.testzip() is not None:
            raise ValueError("Python ZIP CRC mismatch")
        for item in archive.infolist():
            rel = pathlib.PurePosixPath(item.filename)
            if rel.is_absolute() or ".." in rel.parts or "\\" in item.filename or ":" in item.filename:
                raise ValueError("Unsafe upstream ZIP entry")
        archive.extractall(runtime)
    (runtime / "python313._pth").write_text("python313.zip\n.\nLib/site-packages\nimport site\n", encoding="utf-8")
    site = runtime / "Lib/site-packages"
    site.mkdir(parents=True)
    (site / "sitecustomize.py").write_text("import os, sys\n_arg = sys.argv[0] if sys.argv else ''\n_script_dir = os.path.dirname(os.path.abspath(_arg)) if _arg and not _arg.startswith('-') else os.getcwd()\nif _script_dir not in sys.path: sys.path.insert(0, _script_dir)\n", encoding="utf-8")
    sources = [{"component": "CPython", "version": PYTHON_VERSION, "platform": "Windows x64", "url": PYTHON_URL, "sha256": PYTHON_SHA256, "checksum_source": PYTHON_RELEASE}]
    wheels = []
    for package, version in PACKAGES.items():
        metadata_url = "https://pypi.org/pypi/" + package + "/" + version + "/json"
        metadata_path = downloads / (package + "-metadata.json")
        fetch(metadata_url, metadata_path)
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        if metadata["info"]["version"] != version:
            raise ValueError("PyPI version mismatch")
        candidates = [item for item in metadata["urls"] if item["packagetype"] == "bdist_wheel" and not item.get("yanked") and (item["filename"].endswith("py3-none-win_amd64.whl") if package == "capstone" else item["filename"].endswith("-none-any.whl"))]
        if len(candidates) != 1:
            raise ValueError("Expected one compatible wheel for " + package)
        item = candidates[0]
        filename = item["filename"]
        if pathlib.PurePath(filename).name != filename:
            raise ValueError("Unsafe wheel name")
        wheel = downloads / filename
        print("DOWNLOAD " + filename, flush=True)
        fetch(item["url"], wheel)
        if sha(wheel) != item["digests"]["sha256"]:
            raise ValueError("Wheel SHA256 mismatch for " + package)
        wheels.append(str(wheel))
        sources.append({"component": package, "version": version, "url": item["url"], "sha256": item["digests"]["sha256"], "metadata_url": metadata_url, "filename": filename, "license": metadata["info"].get("license_expression") or metadata["info"].get("license"), "project_urls": metadata["info"].get("project_urls")})
    print("INSTALL pinned verified wheels into portable target", flush=True)
    print(run([sys.executable, "-m", "pip", "--isolated", "install", "--no-index", "--no-deps", "--no-compile", "--disable-pip-version-check", "--target", str(site)] + wheels), flush=True)
    # Target-generated wrappers name the builder interpreter; use python.exe -m pip instead.
    scripts = site / "Scripts"
    if scripts.is_dir():
        for file in scripts.iterdir():
            if file.is_file():
                file.unlink()
        scripts.rmdir()
    (runtime / "upstream-sources.json").write_text(json.dumps({"schema_version": 1, "purpose": "Pinned redistribution sources and SHA256 checks", "sources": sources}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    licenses = sorted(p.relative_to(runtime).as_posix() for p in runtime.rglob("*") if p.is_file() and ("license" in p.name.casefold() or "copying" in p.name.casefold()))
    if not any("license" in p.casefold() and "/" not in p for p in licenses):
        raise ValueError("Missing CPython redistribution license")
    (runtime / "REDISTRIBUTION.txt").write_text("Portable Windows x64 Python analysis tools for the 2ES6 development workspace.\nCPython 3.13.16: Python Software Foundation License.\npefile 2024.8.26: MIT. Capstone 5.0.6: BSD. pip 26.2.1: MIT (vendored components retain their own licenses).\nUpstream license notices remain at their original paths listed below. Source URLs and verified wheel/download checksums are in upstream-sources.json.\nRun python.exe -m pip; builder-specific pip.exe wrappers are intentionally omitted.\nThe train host itself remains a separate x86 .NET application. Python x64 is only for analysis tools.\n\nLicense files:\n" + "\n".join(licenses) + "\n", encoding="utf-8")
    python = runtime / "python.exe"
    check = "import sys, json, pefile, capstone, pip; assert sys.version_info[:3] == (3,13,16); assert pefile.__version__ == '2024.8.26'; assert capstone.__version__ == '5.0.6'; assert pip.__version__ == '26.2.1'; ins=list(capstone.Cs(capstone.CS_ARCH_X86,capstone.CS_MODE_32).disasm(b'\\x90',4096)); assert len(ins)==1 and ins[0].mnemonic=='nop'; print(json.dumps({'python':sys.version.split()[0], 'pefile':pefile.__version__, 'capstone':capstone.__version__, 'pip':pip.__version__, 'nop':ins[0].mnemonic}))"
    result = json.loads(run([str(python), "-B", "-c", check]))
    result["pip_command"] = run([str(python), "-B", "-m", "pip", "--version"])
    fixture = out / "проверка импортов с пробелами"
    fixture.mkdir()
    (fixture / "neighbor.py").write_text("VALUE = 'portable-unicode-neighbor-ok'\n", encoding="utf-8")
    (fixture / "check.py").write_text("import neighbor; print(neighbor.VALUE)\n", encoding="utf-8")
    result["neighbor_import"] = run([str(python), "-B", str(fixture / "check.py")])
    if result["neighbor_import"] != "portable-unicode-neighbor-ok":
        raise ValueError("Neighbor script import failed")
    result["cwd_c_import"] = run([str(python), "-B", "-c", "import neighbor; print(neighbor.VALUE)"], cwd=fixture)
    result["cwd_m_import"] = run([str(python), "-B", "-m", "check"], cwd=fixture)
    if result["cwd_c_import"] != result["neighbor_import"] or result["cwd_m_import"] != result["neighbor_import"]:
        raise ValueError("Working-directory imports failed")
    (out / "validation.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print("VALIDATED " + json.dumps(result), flush=True)
    inventory = []
    archive_path = out / ASSET_NAME
    with zipfile.ZipFile(archive_path, "x", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for file in sorted(runtime.rglob("*")):
            if not file.is_file() or "__pycache__" in file.parts:
                continue
            relative = "work/portable_python/" + file.relative_to(runtime).as_posix()
            archive.write(file, relative)
            inventory.append({"path": relative, "bytes": file.stat().st_size, "sha256": sha(file), "asset": ASSET_NAME})
    with zipfile.ZipFile(archive_path) as archive:
        if archive.testzip() is not None:
            raise ValueError("Supplement ZIP CRC mismatch")
    asset = {"name": ASSET_NAME, "sha256": sha(archive_path), "size_bytes": archive_path.stat().st_size, "files": len(inventory), "unpacked_bytes": sum(item["bytes"] for item in inventory)}
    (out / "asset.json").write_text(json.dumps(asset, indent=2) + "\n", encoding="utf-8")
    (out / "files.json").write_text(json.dumps(inventory, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("READY " + json.dumps(asset), flush=True)


if __name__ == "__main__":
    main()
