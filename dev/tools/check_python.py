"""Report the local trainer-analysis Python dependencies as one JSON line."""
from contextlib import redirect_stderr, redirect_stdout
import importlib
import importlib.metadata
import io
import json
from pathlib import Path
import platform
import sys


ROOT = Path(__file__).resolve().parents[2]
CONFIG_PATH = ROOT / "dev" / "config" / "python-tools.json"


def functional_check(module_name, module):
    """Exercise binary extensions used by analysis without changing any file."""
    if module_name == "capstone":
        decoder = module.Cs(module.CS_ARCH_X86, module.CS_MODE_32)
        instructions = list(decoder.disasm(b"\x90", 0x1000))
        if len(instructions) != 1 or instructions[0].mnemonic != "nop":
            raise RuntimeError("x86 decoder returned an unexpected NOP result")
    elif module_name == "pefile":
        if not callable(getattr(module, "PE", None)):
            raise RuntimeError("PE reader is unavailable")
    elif module_name == "PIL.Image":
        image = module.new("L", (1, 1), color=0)
        if image.getpixel((0, 0)) != 0:
            raise RuntimeError("Pillow image operation returned an unexpected result")
        image.close()
    elif module_name == "numpy":
        if float(module.asarray([1.0, 2.0]).sum()) != 3.0:
            raise RuntimeError("numpy array operation returned an unexpected result")
    elif module_name == "pypdf":
        if not callable(getattr(module, "PdfReader", None)):
            raise RuntimeError("PdfReader is unavailable")
    elif module_name == "pdfplumber":
        if not callable(getattr(module, "open", None)):
            raise RuntimeError("pdfplumber.open is unavailable")


def check_package(spec):
    result = {
        "distribution": spec["distribution"],
        "import": spec["import"],
        "status": "missing",
        "version": None,
        "path": None,
    }
    captured = io.StringIO()
    try:
        with redirect_stdout(captured), redirect_stderr(captured):
            module = importlib.import_module(spec["import"])
            result["path"] = str(Path(module.__file__).resolve()) if getattr(module, "__file__", None) else None
            try:
                result["version"] = importlib.metadata.version(spec["distribution"])
            except importlib.metadata.PackageNotFoundError:
                package = importlib.import_module(spec["import"].split(".")[0])
                result["version"] = str(getattr(package, "__version__", "unknown"))
            functional_check(spec["import"], module)
            result["status"] = "installed"
    except ModuleNotFoundError as exc:
        result["error"] = str(exc)
        if exc.name not in (spec["import"], spec["import"].split(".")[0]):
            result["status"] = "error"
    except Exception as exc:
        result["status"] = "error"
        result["error"] = type(exc).__name__ + ": " + str(exc)
    if captured.getvalue().strip():
        result["note"] = captured.getvalue().strip()[:500]
    return result


def main():
    try:
        config = json.loads(CONFIG_PATH.read_text(encoding="utf-8-sig"))
        vendor_paths = [ROOT / item for item in config["vendor_paths"]]
        for path in reversed(vendor_paths):
            if path.is_dir():
                sys.path.insert(0, str(path))
        groups = {"required_for_native_analysis": [], "optional": []}
        for spec in config["packages"]:
            groups[spec["group"]].append(check_package(spec))
        missing_required = [item["distribution"] for item in groups["required_for_native_analysis"] if item["status"] != "installed"]
        missing_optional = [item["distribution"] for item in groups["optional"] if item["status"] != "installed"]
        notes = []
        if missing_optional:
            notes.append("Optional tools unavailable: " + ", ".join(missing_optional))
        result = {
            "schema_version": 1,
            "ok": not missing_required,
            "status": "ready" if not missing_required else "required_tools_missing",
            "root": str(ROOT),
            "python": {"version": platform.python_version(), "path": sys.executable},
            "vendor_paths": [{"path": str(path), "exists": path.is_dir()} for path in vendor_paths],
            "groups": groups,
            "missing_required": missing_required,
            "missing_optional": missing_optional,
            "notes": notes,
        }
        print(json.dumps(result, ensure_ascii=True, separators=(",", ":")))
        return 0 if result["ok"] else 1
    except Exception as exc:
        print(json.dumps({"schema_version": 1, "ok": False, "status": "checker_error", "error": type(exc).__name__ + ": " + str(exc)}, ensure_ascii=True, separators=(",", ":")))
        return 1


if __name__ == "__main__":
    sys.exit(main())
