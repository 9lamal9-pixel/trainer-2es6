from pathlib import Path
import struct
import json
import hashlib

folder=Path(__file__).resolve().parent
path=folder/'scbist_alt_tail.bin'
b=path.read_bytes()
entries=[]
pos=b.find(b'PK\x01\x02')
while pos>=0 and pos+46<=len(b):
    header=struct.unpack_from('<4s6H3I5H2I',b,pos)
    fnlen,extralen,commentlen=header[10:13]
    raw=b[pos+46:pos+46+fnlen]
    flag=header[3]
    for encoding in (['utf-8'] if flag&0x800 else ['cp866','cp1251','utf-8']):
        try:
            name=raw.decode(encoding)
            break
        except UnicodeDecodeError:
            pass
    entries.append({'name':name,'raw_name_hex':raw.hex(),'compressed':header[8],'uncompressed':header[9],'compression':header[4],'crc32':f'{header[7]:08X}','offset':header[16],'flag':flag})
    pos=b.find(b'PK\x01\x02',pos+46+fnlen+extralen+commentlen)
out={'zip_url':'https://scbist.com/scb/uploaded/1_2es6-re.zip','total_length':18064798,'tail_start':17999262,'tail_bytes':len(b),'tail_sha256':hashlib.sha256(b).hexdigest().upper(),'entries':entries,'eocd_present':b'PK\x05\x06' in b,'download_crc_verified':False}
(folder/'scbist_zip_directory.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(out,ensure_ascii=False,indent=2))
