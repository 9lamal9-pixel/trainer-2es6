# Disassemble a small x64 PE (TrainModule.dll): .text with export labels, RIP-relative constants decoded as float/double.
import sys, struct, pefile, capstone
sys.stdout.reconfigure(encoding='utf-8')
path, out = sys.argv[1], sys.argv[2]
pe = pefile.PE(path)
base = pe.OPTIONAL_HEADER.ImageBase
img = pe.get_memory_mapped_image()
labels = {}
for e in pe.DIRECTORY_ENTRY_EXPORT.symbols:
    labels.setdefault(e.address, []).append(e.name.decode())
imports = {}
for d in pe.DIRECTORY_ENTRY_IMPORT:
    for i in d.imports:
        imports[i.address - base] = d.dll.decode() + '!' + (i.name.decode() if i.name else str(i.ordinal))
text = [s for s in pe.sections if s.Name.startswith(b'.text')][0]
code = img[text.VirtualAddress:text.VirtualAddress + text.Misc_VirtualSize]
md = capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_64)
md.detail = True
def const(rva, size):
    b = img[rva:rva + 8]
    s = ''
    try:
        f = struct.unpack('<f', b[:4])[0]; d = struct.unpack('<d', b[:8])[0]
        s = 'f32=%r d64=%r i32=%d' % (f, d, struct.unpack('<i', b[:4])[0])
    except Exception: pass
    return s
with open(out, 'w', encoding='utf-8') as o:
    for ins in md.disasm(code, text.VirtualAddress):
        if ins.address in labels:
            o.write('\n==== %s @ %#x\n' % (', '.join(labels[ins.address]), ins.address))
        note = ''
        if 'rip' in ins.op_str:
            for op in ins.operands:
                if op.type == capstone.x86.X86_OP_MEM and op.mem.base == capstone.x86.X86_REG_RIP:
                    rva = ins.address + ins.size + op.mem.disp
                    note = '  ; [%#x] %s %s' % (rva, imports.get(rva, ''), const(rva, op.size) if rva not in imports else '')
        o.write('%#07x: %-8s %s%s\n' % (ins.address, ins.mnemonic, ins.op_str, note))
print('done', len(code))
