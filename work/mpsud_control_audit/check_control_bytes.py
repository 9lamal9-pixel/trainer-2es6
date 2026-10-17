import hashlib
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

workspace = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(workspace / "work/traction6000/vendor"))
import pefile
from capstone import Cs, CS_ARCH_X86, CS_MODE_32

module = workspace / "work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Pult1.dll"
data = module.read_bytes()
actual_sha = hashlib.sha256(data).hexdigest().upper()
expected_sha = "7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5"
assert actual_sha == expected_sha, "Pult1 identity mismatch"
pe = pefile.PE(data=data, fast_load=True)
disassembler = Cs(CS_ARCH_X86, CS_MODE_32)

# Expected encodings follow the operands already inspected in the saved ASM.
# Reads only: no native process, hooks, writes to DLL or ExecuteCommunication.
specifications = [
    ("C07/native_discrete_58", 0x18E982, "80785800"),
    ("C07/constant_voltage_3100", 0x18E9BA, "c780250100001c0c0000"),
    ("C07/otherwise_voltage_zero_from_edx", 0x18E9D0, "899025010000"),
    ("C21/read_raw_position_8c", 0x18C676, "0fb6928c000000"),
    ("C21/store_position_104", 0x18C67D, "899004010000"),
    ("C21/clear_latch_108", 0x18C6E7, "c6800801000000"),
    ("C21/position1_branch", 0x18C701, "7425"),
    ("C21/position2_branch", 0x18C703, "487442"),
    ("C21/set_latch_108", 0x18C76E, "c6800801000001"),
]
guards = []
for identifier, rva, expected in specifications:
    encoded = bytes.fromhex(expected)
    actual = pe.get_data(rva, len(encoded))
    guards.append({"id": identifier, "rva": hex(rva), "file_offset": hex(pe.get_offset_from_rva(rva)), "expected_hex": expected, "actual_hex": actual.hex(), "passed": actual == encoded})

ranges = []
for identifier, start, stop in [("C07", 0x18E97A, 0x18E9D6), ("C21", 0x18C665, 0x18C7A6)]:
    block = pe.get_data(start, stop - start)
    instructions = [{"rva": hex(ins.address), "hex": ins.bytes.hex(), "instruction": f"{ins.mnemonic} {ins.op_str}"} for ins in disassembler.disasm(block, start)]
    ranges.append({"id": identifier, "start_rva": hex(start), "end_rva_exclusive": hex(stop), "range_sha256": hashlib.sha256(block).hexdigest().upper(), "instructions": instructions})

result = {"checked_utc": datetime.now(timezone.utc).isoformat(), "module_path": str(module), "expected_module_sha256": expected_sha, "actual_module_sha256": actual_sha, "identity_passed": actual_sha == expected_sha, "all_byte_guards_passed": all(g["passed"] for g in guards), "functional_acceptance": False, "mode": "read-only on-disk PE bytes; loaded identity independently verified by MPSUD_BASELINE.json", "guards": guards, "ranges": ranges}
output = workspace / "dev/docs/audit_parts/MPSUD_CONTROL_BYTE_GUARDS.json"
output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(json.dumps({"path": str(output), "module_sha256": actual_sha, "guards": len(guards), "all_byte_guards_passed": result["all_byte_guards_passed"], "functional_acceptance": False}, ensure_ascii=False))
assert result["all_byte_guards_passed"], "Native byte guard mismatch"
