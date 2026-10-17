import hashlib,json,pathlib,sys,re,struct
sys.path.insert(0,str(pathlib.Path('work/traction6000/vendor').resolve()))
import pefile,capstone
root=pathlib.Path('work/mpsud_screen_values')
root.mkdir(parents=True,exist_ok=True)
dll=pathlib.Path('work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/MSUL_MON.dll')
b=dll.read_bytes(); pe=pefile.PE(data=b)
procs=json.loads(pathlib.Path('work/msul_emulator/display_reference/MSUL_MON.dll.procs.json').read_text(encoding='utf8'))
by_addr={x['rva']:x['name'] for x in procs}
selected={
'TwtMashinistFrame':{'GetNameRezhim','GetColorOfDef','ProceedCur','ProceedFrame','SetPosition','SetRecuperatia','UpdatePos','UksChangeState','UpdateMessages','Ija1_12ChangeState','Iv1_12ChangeState'},
'TwtSautFrame':{'UpdateSAUTPrib','ProceedFrame'},
'TwtCIFrame':{'UpdateData','Init','ProceedFrame','DrawGrid1DrawCell','DrawGrid2DrawCell','DrawGrid3DrawCell','DrawGrid4DrawCell','DrawGrid5DrawCell','DrawGrid6DrawCell'},
'TwtSMErame':{'UpdateData','Init','PaintContacts','ProceedFrame','DrawGrid1DrawCell','DrawGrid2DrawCell'},
'TwtAutovedFrame':{'ProceedFrame','InitParam'},
'TDataCaption':{'SetDataStr','SetState','SetValue','SetOnOffFlag','Paint'},
'TIndicatorPribor':{'SetValue'},
'TControl':{'GetText','GetTextBuf','GetTextLen','SetText','SetTextBuf','DefaultHandler'},
'TSAUTArrowPribor':{'SetValue','SetValueSec'},
'TIndicator':{'SetIValue','SetIValue2'},
'TPriborLabel':{'SetPLCaption'},
}
md=capstone.Cs(capstone.CS_ARCH_X86,capstone.CS_MODE_32)
md.skipdata=True
def ustr(va):
 try:
  if va<pe.OPTIONAL_HEADER.ImageBase:return ''
  rva=va-pe.OPTIONAL_HEADER.ImageBase
  size=struct.unpack('<I',pe.get_data(rva-4,4))[0]
  if not 0<size<=120:return ''
  text=pe.get_data(rva,size*2).decode('utf-16-le')
  if any(ord(c)<32 for c in text):return ''
  return repr(text)
 except Exception:return ''
meta=[]
for x in procs:
 n=x['name']; found=None
 for cls,methods in selected.items():
  for meth in methods:
   if f'@{cls}@{meth}$' in n: found=(cls,meth)
 if not found:continue
 lines=[n+' RVA='+hex(x['rva'])+' size='+hex(x['length'])]
 for ins in md.disasm(pe.get_data(x['rva'],x['length']),x['rva']):
  note=''
  if ins.mnemonic=='call' and ins.op_str.startswith('0x'):
   note=' ; '+by_addr.get(int(ins.op_str,16),'')
  elif ins.mnemonic in ('mov','push'):
   m=re.search(r'(?:, |^)(0x[0-9a-f]+)$',ins.op_str)
   if m:
    s=ustr(int(m[1],16))
    if s:note=' ; UnicodeString '+s
  lines.append(f'{ins.address:08x} {ins.bytes.hex():24} {ins.mnemonic:8} {ins.op_str}{note}')
 suffix = '_'+format(x['rva'],'x') if found[0] in {'TDataCaption','TControl'} else ''
 out=root/('_'.join(found)+suffix+'.asm')
 out.write_text('\n'.join(lines)+'\n',encoding='utf8')
 meta.append({'name':n,'rva':hex(x['rva']),'length':hex(x['length']),'path':out.as_posix(),'instructions':len(lines)-1})
result={'path':dll.as_posix(),'sha256':hashlib.sha256(b).hexdigest(),'procedures':meta}
(root/'decode_manifest.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8')
print('sha256',result['sha256'])
for x in meta:print(x['rva'],x['instructions'],x['path'])
