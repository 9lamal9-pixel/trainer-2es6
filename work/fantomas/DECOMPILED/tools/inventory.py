# Inventory of PE binaries under a folder: managed/native, machine, size, sha256.
import sys, os, hashlib, json, pefile
sys.stdout.reconfigure(encoding='utf-8')
root = sys.argv[1]
rows = []
for d, _, files in os.walk(root):
    for f in files:
        if not f.lower().endswith(('.exe', '.dll')): continue
        p = os.path.join(d, f)
        try:
            pe = pefile.PE(p, fast_load=True)
            clr = pe.OPTIONAL_HEADER.DATA_DIRECTORY[14]
            managed = clr.VirtualAddress != 0 and clr.Size != 0
            mach = {0x14c: 'x86', 0x8664: 'x64'}.get(pe.FILE_HEADER.Machine, hex(pe.FILE_HEADER.Machine))
            pe.close()
        except Exception as e:
            managed, mach = None, 'ERR ' + str(e)[:40]
        h = hashlib.sha256(open(p, 'rb').read()).hexdigest().upper()
        rows.append(dict(path=os.path.relpath(p, root).replace(os.sep, '/'), managed=managed, machine=mach, size=os.path.getsize(p), sha256=h))
json.dump(rows, open(sys.argv[2], 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print(len(rows))
