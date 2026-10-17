"""Minimal zero-active-motor fixes for this exact binary, in a private copy.

No current formula, voltage, motor-selection state, or force scale changes.
Both injected branches return the physical open-circuit result for zero active pairs.
The third patch applies the existing independent-traction 750 A state limit before
moment is computed, so force and displayed current describe the same current.
The new section uses relative branches only and is valid when the DLL is relocated.
"""
from pathlib import Path
import hashlib, struct, json, pefile

folder=Path(__file__).resolve().parent
source=folder/'Tyga1.original.dll'
data=bytearray(source.read_bytes())
original_sha=hashlib.sha256(data).hexdigest()
assert original_sha=='c56d401d7d0e38c137a0fd70604f416e1a9b1addc9776a585e0e6d4397d588ed','Unsupported Tyga1.dll build'
pe=pefile.PE(data=data)

def align(v,a):return (v+a-1)//a*a
last=pe.sections[-1]
section_rva=align(last.VirtualAddress+last.Misc_VirtualSize,pe.OPTIONAL_HEADER.SectionAlignment)
raw_offset=align(len(data),pe.OPTIONAL_HEADER.FileAlignment)
cave=bytearray()
patches=[]
def reljump(src,dst):return b'\xe9'+struct.pack('<i',dst-src-5)
def call(src,dst):return b'\xe8'+struct.pack('<i',dst-src-5)
def guard_patch(rva,expected,body):
    offset=pe.get_offset_from_rva(rva)
    assert bytes(data[offset:offset+len(expected)])==expected,(hex(rva),'unexpected original bytes')
    start=section_rva+len(cave)
    cave.extend(body(start))
    replacement=reljump(rva,start)+b'\x90'*(len(expected)-5)
    data[offset:offset+len(expected)]=replacement
    patches.append(dict(rva=hex(rva),offset=offset,original=expected.hex(),patched=replacement.hex(),cave_rva=hex(start)))

def percent_guard(start):
    # Local active-pair count <= 0: zero percent; otherwise original two FP ops.
    b=bytearray.fromhex('83 7d f0 00 7f 06 33 c0 8b e5 5d c3')
    b.extend(bytes.fromhex('db 45 f0 d8 7d ec'))
    b.extend(reljump(start+len(b),0x174867))
    return b
guard_patch(0x174861,bytes.fromhex('db 45 f0 d8 7d ec'),percent_guard)

def sequential_guard(start):
    # GetCountVklTED has already summed the two sections to local -0x24.
    b=bytearray.fromhex('83 7d dc 00 0f 8f')
    b.extend(struct.pack('<i',0x17a2dc-(start+10)))
    b.extend(bytes.fromhex('8b 45 fc'))
    b.extend(call(start+len(b),0x17c188)) # native Help: zero force/current/moment.
    b.extend(bytes.fromhex('d9 ee 5b 8b e5 5d c2 04 00')) # fldz, restore EBX/frame, ret 4.
    return b
guard_patch(0x179db7,bytes.fromhex('e9 20 05 00 00'),sequential_guard)

def current_guard(start):
    # Ia has already been floored to zero by the original code. Positive IEEE
    # Singles have monotonically ordered unsigned bit patterns; 750f=0x443B8000.
    b=bytearray.fromhex('8b 45 fc 8b 40 04 8b 55 f4')
    for offset in (0x33,0x37):
        b.extend(bytes([0x81,0x7c,0xd0,offset])+struct.pack('<I',0x443b8000))
        b.extend(bytes.fromhex('76 08'))
        b.extend(bytes([0xc7,0x44,0xd0,offset])+struct.pack('<I',0x443b8000))
    b.extend(reljump(start+len(b),0x179726))
    return b
guard_patch(0x17971d,bytes.fromhex('8b 45 fc 8b 40 04 8b 55 f4'),current_guard)

def independent_guard(start):
    # Independent excitation calculates Ivozb limits before GetCur is reached.
    # That calculation also assumes at least one active pair in S mode.
    b=bytearray.fromhex('8b 45 fc ba 01 00 00 00')
    b.extend(call(start+len(b),0x178204));b.extend(b'\x50')
    b.extend(bytes.fromhex('8b 45 fc ba 02 00 00 00'))
    b.extend(call(start+len(b),0x178204));b.extend(bytes.fromhex('5a 03 c2 85 c0 75 0c'))
    b.extend(bytes.fromhex('8b 45 fc'))
    b.extend(call(start+len(b),0x17c188))
    b.extend(bytes.fromhex('8b e5 5d c3'))
    b.extend(bytes.fromhex('8b 45 fc 80 78 0b 00'))
    b.extend(reljump(start+len(b),0x1764ac))
    return b
guard_patch(0x1764a5,bytes.fromhex('8b 45 fc 80 78 0b 00'),independent_guard)

headers_offset=pe.sections[-1].get_file_offset()+40
assert headers_offset+40<=pe.OPTIONAL_HEADER.SizeOfHeaders
assert not any(data[headers_offset:headers_offset+40])
raw_size=align(len(cave),pe.OPTIONAL_HEADER.FileAlignment)
section_header=struct.pack('<8sIIIIIIHHI',b'.tfix\0\0\0',len(cave),section_rva,raw_size,raw_offset,0,0,0,0,0x60000020)
data[headers_offset:headers_offset+40]=section_header
struct.pack_into('<H',data,pe.FILE_HEADER.get_field_absolute_offset('NumberOfSections'),pe.FILE_HEADER.NumberOfSections+1)
struct.pack_into('<I',data,pe.OPTIONAL_HEADER.get_field_absolute_offset('SizeOfImage'),align(section_rva+len(cave),pe.OPTIONAL_HEADER.SectionAlignment))
struct.pack_into('<I',data,pe.OPTIONAL_HEADER.get_field_absolute_offset('SizeOfCode'),pe.OPTIONAL_HEADER.SizeOfCode+raw_size)
data.extend(b'\0'*(raw_offset-len(data)))
data.extend(cave+b'\0'*(raw_size-len(cave)))
newpe=pefile.PE(data=data)
struct.pack_into('<I',data,newpe.OPTIONAL_HEADER.get_field_absolute_offset('CheckSum'),newpe.generate_checksum())
target=folder/'Tyga1.patched.dll'
target.write_bytes(data)
manifest=dict(original_sha256=original_sha,patched_sha256=hashlib.sha256(data).hexdigest(),original_size=len(source.read_bytes()),patched_size=len(data),patches=patches,cave_hex=cave.hex(),cave_rva=hex(section_rva))
(folder/'patch_manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf8')
print(json.dumps(manifest,indent=2))
