"""Historical partial transcription; never loads/executes DLL code.

Not current calibration evidence: v is the native m/s argument, not km/h;
nezav raw-Ia force and target-root tables omit the existing .tfix early750 limit.
See dev/docs/audit_parts/re_part1/getcur-retained-tfix-proof-20261025.json.
The calculations below are preserved as historical analysis, not repaired here.
"""
import json, math, struct
from pathlib import Path
import pefile
p=Path(__file__).parent
c=json.loads((p/'TConst.json').read_text()); k=c['fields']
f32=lambda x: struct.unpack('<f',struct.pack('<f',x))[0]
h=1+2*k['Rgp']/k['RShunt_R15_R16']
phi=lambda x:k['K4']*(1-math.exp(k['K5']*x))
force=lambda flux,ia: k['Kper']*8*flux*ia/(1000*k['Rkol']*1.3)
rp=lambda pos:c['arrays']['Rpusk']['rows'][pos-1][0]

def posl(v,u,pos):
    soed={23:'S',44:'SP',65:'P'}[pos]
    n={'S':8,'SP':4,'P':2}[soed]
    r={'S':rp(pos)+8*k['Rya']+4*k['R1'],
       'SP':rp(pos)+4*k['Rya']+2*k['R1'],
       'P':2*rp(pos)+2*k['Rya']+k['R1']}[soed]
    old,new=300.,320.
    count=0
    while abs(new-old)>0.1:
        old=new
        num=f32(u-n*phi(old)*v-old*r*h)
        den=f32(n*k['K4']*k['K5']*v*math.exp(k['K5']*old)-r*h)
        new=f32(old-num/den)
        count+=1
        if count>1000:raise ValueError('transcribed math did not converge')
    ia=f32(new/h)
    # SetCurPosl stores Ia also in Ivozb, force passes it through GetCurVozbReal again.
    flux=phi(f32(ia/h))
    return dict(v=v,u=u,pos=pos,soed=soed,Ia=ia,Iexc=ia,
                phi_force=flux,F_kN=force(flux,ia),iterations=count,
                Ia_gt700=ia>700)

def nezav(v,u,pos,exc):
    soed='S' if pos<=23 else 'SP' if pos<=44 else 'P'
    flux=phi(f32(exc/h))
    e=f32(2*flux*v);u2=f32(k['R2']*exc)
    # Full 2 sections, both pairs per section enabled. Different resistance
    # multiplicity in GetCur vs GetCurPosl is intentional in this transcription.
    n={'S':4,'SP':2,'P':1}[soed]
    r={'S':rp(pos)+8*k['Rya']+4*k['R2'],
       'SP':rp(pos)+4*k['Rya']+2*k['R2'],
       'P':2*rp(pos)+4*k['Rya']+2*k['R2']}[soed]
    ia=f32(max(0,(u-n*e+n*u2)/r))
    return dict(v=v,u=u,pos=pos,soed=soed,Iexc=exc,Ia_before_end_clamp=ia,
                Ia_reported_after_end_clamp=min(750,ia),phi_force=flux,
                F_kN=force(flux,ia),Ia_gt700=ia>700,Ia_clamp750=ia>750)

def exc_max(v,u,soed):
    threshold={'S':16,'SP':32,'P':60}[soed]
    if v<=threshold:return 800.
    n={'S':8,'SP':4,'P':2}[soed]
    c={'S':4,'SP':2,'P':1}[soed]
    # CalcIvozbMaxMin Newton's root, literal algebra reconstructed from code.
    q=320.
    for _ in range(1000):
        f=n*phi(q)*v-u-c*k['R2']*h*q
        df=-n*k['K4']*k['K5']*v*math.exp(k['K5']*q)-2*c*k['R2']*k['Rgp']/8
        q2=f32(q-f32(f)/f32(df))
        if abs(q2-q)<=.1:return q2
        q=q2
    raise ValueError('bound Newton did not converge')

def roots_target(v,u,pos,pct):
    soed='S' if pos<=23 else 'SP' if pos<=44 else 'P'
    maximum=exc_max(v,u,soed)*(1 if soed=='SP' else 1.05)
    target=(56.748-.139*v)*10*pct/100
    roots=[]
    prev_x=0.;prev_f=-target
    for i in range(1,4001):
        x=maximum*i/4000
        f=nezav(v,u,pos,x)['F_kN']-target
        if prev_f*f<0:
            lo,hi=prev_x,x;fl=prev_f
            for _ in range(40):
                mid=(lo+hi)/2; fm=nezav(v,u,pos,mid)['F_kN']-target
                if fl*fm<0:hi=mid
                else:lo=mid;fl=fm
            r=nezav(v,u,pos,(lo+hi)/2)
            r.update(percent=pct,target_kN=target,exc_upper=maximum,
                     ratio_Ia_exc=r['Ia_before_end_clamp']/r['Iexc'])
            roots.append(r)
        prev_x=x;prev_f=f
    return roots

velocities=[49.2,51,80,120]
posl_rows=[posl(v,u,pos) for u in [3200,3000] for v in velocities for pos in [23,44,65]]
nezav_rows=[nezav(v,u,pos,exc) for u in [3200,3000] for v in velocities
            for pos in [23,44,65] for exc in [100,200,390,660,700]]
dll=Path(r'work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\Tyga1.dll')
pe=pefile.PE(str(dll));raw=pe.get_offset_from_rva(0x19040c)
bits=dll.read_bytes()[raw:raw+12]
hod=[i for i in range(66) if bits[i//8]&(1<<(i%8))]
target_rows=[r for v in [20,40,49.2,51,80,120] for pos in range(1,66)
             for r in roots_target(v,3200,pos,99)]
target100_rows=[r for v,pos in [(20,23),(40,44),(49.2,44),(51,44),(80,65),(120,65)]
                for r in roots_target(v,3200,pos,100)]
out=dict(h=h,hod_positions=hod,hod_set_bytes=bits.hex(),posl=posl_rows,
         nezav_fixed_exc=nezav_rows,roots_99percent=target_rows,
         selected_hod_roots_100percent=target100_rows,
         limit_ratio5_P120_exc150=nezav(120,3200,65,150))
(p/'numeric.json').write_text(json.dumps(out,ensure_ascii=False,indent=2),encoding='utf8')
print('hod_positions',hod, 'set_bytes',bits.hex())
print('Posled: v pos Ia F_kN; U3200; no control transitions')
for row in posl_rows:
    if row['u']==3200:print(row['v'],row['pos'],round(row['Ia'],3),round(row['F_kN'],3))
print('Nezav: v pos Iexc Ia F_kN; U3200; fixed Iexc700; no regulator transitions')
for row in nezav_rows:
    if row['u']==3200 and row['Iexc']==700:print(row['v'],row['pos'],row['Iexc'],round(row['Ia_before_end_clamp'],3),round(row['F_kN'],3))
print('Target99% roots with Ia<=700, highest position per speed (algebra only)')
for v in [20,40,49.2,51,80,120]:
    ok=[r for r in target_rows if r['v']==v and r['Ia_before_end_clamp']<=700]
    if not ok:print(v,'NO roots with Ia<=700');continue
    best=max(r['pos'] for r in ok)
    for r in ok:
        if r['pos']==best:print(v,best,r['soed'],round(r['Iexc'],3),round(r['Ia_before_end_clamp'],3),round(r['F_kN'],3),'exc_upper',round(r['exc_upper'],3))
print('Hod position target99% roots: v pos exc Ia force')
for r in target_rows:
    if r['pos'] in [23,44,65]:print(r['v'],r['pos'],round(r['Iexc'],3),round(r['Ia_before_end_clamp'],3),round(r['F_kN'],3))
print('Selected hod roots100percent, higher excitation root')
for r in target100_rows:
    if r['Iexc']>100:print(r['v'],r['pos'],round(r['Iexc'],3),round(r['Ia_before_end_clamp'],3),round(r['F_kN'],3),'exc_upper',round(r['exc_upper'],3),'storedRatio',round(min(750,r['Ia_before_end_clamp'])/r['Iexc'],3))
print('P120exc150',nezav(120,3200,65,150))
