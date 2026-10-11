from pathlib import Path
import math,json
c=json.loads(Path(r'work/traction6000/TConst.json').read_text())['fields']
# Algebra reconstructed from read-only disassembly; this does not load/run the sample DLL.
def point(v,iex):
 conn='S' if 16<v<=38 else 'SP' if 38<v<=69 else 'P'
 q={'S':1,'SP':2,'P':4}[conn]
 k13=c['Eset']*c['Rnagr']/(c['Rnagr']+c['Rstan'])
 k14=c['Rnagr']*c['Rstan']/(c['Rnagr']+c['Rstan'])
 ks=q*(k14+c['Rline'])
 cf=c['K4']*(1-math.exp(c['K5']*iex/(1+2*c['Rgp']/c['RShunt_R15_R16'])))
 e=2*cf*v; u=iex*c['R2']
 m={'S':4,'SP':2,'P':1}[conn]
 if conn=='S':den=ks+8*c['Rya']+4*c['R2']
 else:den=ks+4*c['Rya']+2*c['R2']
 ia=min(0,(k13-m*e+m*u)/den)
 # CalcRecup group's force carries /1.3. ProceedTyga's common force multiplies 1.3.
 b=-4*2*cf*ia*c['Kper']/(10000*c['Rkol'])*10
 uset=k13-k14*q*ia
 return {'connection':conn,'Ivozb_A':iex,'CF':cf,'Ia_A':ia,'B_kN':b,'Uset_V':uset}
rows=[]
for v in [49.2,51,80,120]:
 curve=413.4286-1.1429*v
 target=curve*.99*1.3
 lo,hi=0,520
 for _ in range(100):
  mid=(lo+hi)/2
  if point(v,mid)['B_kN']<target:lo=mid
  else:hi=mid
 p=point(v,(lo+hi)/2)
 p.update(V_kmh=v,Bcurve_kN=curve,Btarget_kN=target,mechanical_MW=target*v/3600)
 rows.append(p)
out=Path(__file__).parent/'numeric.json';out.write_text(json.dumps(rows,indent=2),encoding='utf8')
print(json.dumps(rows,indent=2))
