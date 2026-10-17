import struct,json
from pathlib import Path
root=Path(r'work\trenager_static\WORK_TRENAGER_3D\DATA\LIB\ENGINE')
class R:
 def __init__(self,p):self.d=p.read_bytes();self.p=0
 def get(self,f):
  n=struct.calcsize('<'+f);v=struct.unpack_from('<'+f,self.d,self.p);self.p+=n;return v[0] if len(v)==1 else v
 def skip(self,n):self.p+=n
 def fields(self,spec):
  out={}
  for name,f in spec:out[name]={'offset':hex(self.p),'value':self.get(f)}
  return out
 def string(self):n=self.get('i');assert 0<=n<1000,(self.p,n);s=self.d[self.p:self.p+n*2].decode('utf-16le');self.skip(n*2);return s
 def pic(self):
  if self.get('B'):n=self.get('q');assert 0<n<3000000;n and self.skip(n)
 def vr(self,t):
  n={0:136,1:136,2:144,3:112}[t];p=self.p;raw=self.d[p:p+n];self.skip(n);return {'offset':hex(p),'size':n,'raw':raw.hex(),'doubles':struct.unpack('<'+str(n//8)+'d',raw)}
 def tb(self,loco=False):
  version=self.get('i');assert version>=2,(self.p,version)
  if loco:
   out=self.fields([('CountTC','i'),('CompQ','f'),('CompQMax','f'),('VGR','i'),('MaxPGr','f'),('MinPGr','f'),('Vtm','d'),('D_truba','i'),('Vzr','d'),('Vtc','d'),('VRType','B')])
   if version>=3:out.update(self.fields([('TLoc','i')]))
   if version>=5:out.update(self.fields([('BreakeToAxis_raw','3i')]))
  else:
   out=self.fields([('Vtm','d'),('D_truba','i'),('Vzr','d'),('Vtc','d'),('VRType','B')])
   if version>=3:out.update(self.fields([('BreakeToAxis_raw','3i')]))
  out['version']=version;out['VR']=self.vr(out['VRType']['value']);return out
def wagons():
 r=R(root/'WagonsLibrary.dat');version,count=r.get('i'),r.get('i');out=[]
 for i in range(count):
  b={'start':hex(r.p)};v=r.get('i');b['version']=v;b['name']=r.string();b.update(r.fields([('TypeWagon','i'),('TipDvig','B'),('WightTara','i'),('MaxCapacity','i'),('CountOsi','i'),('Length','d'),('FirstCifraOnVagon','i')]))
  vd=r.get('i');assert vd>=1,(i,hex(r.p),vd); r.skip(32)
  b['Torm']=r.tb();r.pic()
  if v>=4:b.update(r.fields([('NominalLength','d')]))
  if v>=5:b.update(r.fields([('WagonType','B')]))
  if v>=6:r.skip(12)
  b['end']=hex(r.p);out.append(b)
 assert r.p==len(r.d),(r.p,len(r.d));return out
def locos():
 r=R(root/'LocoLibrary.dat');version,count=r.get('i'),r.get('i');out=[]
 for i in range(count):
  b={'start':hex(r.p)};b['Tyga']=r.fields([('MaxF','d'),('MinF','d'),('Koef','d'),('MaxUskor','d'),('MaxRaslhod','f')]);b['Physics']=r.fields([('WightOneSec','i'),('CountOsi','i'),('LLoc','f'),('VKonstr','i'),('EPTValid','B'),('Recup','B'),('EnergoType','B'),('BlockedBreake','B')])
  b['Torm']=r.tb(True)
  ps=r.get('i');assert ps>=4;b['safety_raw']=r.get('7B')
  v3=r.get('i');assert v3>=7;r.skip(32+4*41+(1 if v3>=10 else 0))
  b['Eltorm']=r.fields([('8','d'),('10','d'),('20','d'),('28','d'),('18','d'),('30','d'),('38','B')])
  b['name']=r.string();b.update(r.fields([('IndexLoco','i'),('Direction','B'),('CameraFront','B'),('CameraLeft','B'),('CameraRight','B'),('CameraBack','B'),('TipDvig','B'),('ID3DMin','i'),('UseToTrenager','B'),('Sec','i')]))
  r.pic();b['end']=hex(r.p);out.append(b)
 assert r.p==len(r.d),(r.p,len(r.d));return out
out={'wagons':wagons(),'locomotives':locos()};Path(__file__).with_name('libraries.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf8')
for b in out['wagons']:print(b['name'],*[b[k]['value'] for k in ['WightTara','MaxCapacity','CountOsi','Length']],b['Torm'])
for b in out['locomotives']:
 if b['name']=='2ЭС6':print(b)
