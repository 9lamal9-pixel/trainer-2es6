from pathlib import Path
src=Path(r'work/traction6000/electrobrake/disasm.py').read_text(encoding='utf8').split('for term in sys.argv[1:]:')[0]
exec(src.replace('Tyga1.dll','Pult1.dll'))
floats={3.0,120.0,3400.0,3800.0,3.4,3.8,3200.0}
bits={struct.unpack('<I',struct.pack('<f',n))[0]:n for n in floats}
for x in procs:
 if not any(q in x['name'] for q in ['@Pultdllclassindex@','@Shemapultstruct@']):continue
 o=pe.get_offset_from_rva(x['rva'])
 for i in md.disasm(d[o:o+x['length']],base+x['rva']):
  anno=[]
  for op in i.operands:
   if op.type==X86_OP_IMM and ((op.imm&0xffffffff) in bits or op.imm in (3400,3800,3200)):
    anno.append('imm '+str(op.imm)+' float '+str(bits.get(op.imm&0xffffffff)))
   if op.type==X86_OP_MEM and not op.mem.base and not op.mem.index and op.mem.disp>=base:
    try:
     ro=pe.get_offset_from_rva(op.mem.disp-base);n=struct.unpack_from('<f',d,ro)[0]
     if n in floats:anno.append('f32 '+str(n))
    except Exception:pass
  if anno:print(hex(i.address-base),x['name'],i.mnemonic,i.op_str,anno)
