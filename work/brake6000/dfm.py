import struct,json,sys
from pathlib import Path
class D:
 def __init__(self,d):self.d=d;self.p=4
 def get(self,f):n=struct.calcsize('<'+f);a=struct.unpack_from('<'+f,self.d,self.p)[0];self.p+=n;return a
 def short(self):n=self.get('B');a=self.d[self.p:self.p+n];self.p+=n;return a.decode('cp1251')
 def value(self):
  t=self.get('B')
  if t in (0,13):return None
  if t==1:
   a=[]
   while self.d[self.p]:a.append(self.value())
   self.p+=1;return a
  if t in (2,3,4,15,19):return self.get({2:'b',3:'h',4:'i',15:'f',19:'q'}[t])
  if t==5:a=self.d[self.p:self.p+10];self.p+=10;return a.hex()
  if t in (6,7):return self.short()
  if t in (8,9):return t==9
  if t in (10,12,20):
   n=self.get('I');a=self.d[self.p:self.p+n];self.p+=n;return a.hex() if t==10 else a.decode('utf8' if t==20 else 'cp1251')
  if t==11:
   a=[]
   while self.d[self.p]:a.append(self.short())
   self.p+=1;return a
  if t in (16,17):return self.get('d')
  if t==18:
   n=self.get('I');a=self.d[self.p:self.p+n*2];self.p+=n*2;return a.decode('utf-16le')
  raise Exception((hex(self.p-1),t))
 def obj(self):
  a={'class':self.short(),'name':self.short(),'props':{},'children':[]}
  while self.d[self.p]:
   key=self.short();a['props'][key]=self.value()
  self.p+=1
  while self.d[self.p]:a['children'].append(self.obj())
  self.p+=1;return a
d=D(Path(sys.argv[1]).read_bytes());a=d.obj();dest=Path(sys.argv[1]).with_suffix('.json');dest.write_text(json.dumps(a,ensure_ascii=False,indent=2),encoding='utf8');print(dest,d.p,len(d.d))
