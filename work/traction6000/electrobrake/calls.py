exec(open(r'work\traction6000\electrobrake\disasm.py',encoding='utf8').read().split('for term in sys.argv[1:]:')[0])
want=sys.argv[1:]
for x in procs:
 o=pe.get_offset_from_rva(x['rva'])
 for i in md.disasm(d[o:o+x['length']],base+x['rva']):
  if i.mnemonic=='call' and i.operands[0].type==X86_OP_IMM:
   target=byaddr.get(i.operands[0].imm,'')
   if any(q.lower() in target.lower() for q in want):print(hex(i.address-base),x['name'],'->',target)
