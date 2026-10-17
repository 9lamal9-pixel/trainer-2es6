import math,json
from pathlib import Path
R=287.05;T=293.15;G=1.4;ATM=101325.;UNIT=98066.5
def flow(up,down,diameter,cd=.7):
 if up<=down:return 0.
 ratio=down/up;critical=(2/(G+1))**(G/(G-1));area=math.pi*(diameter/1000)**2/4
 factor=math.sqrt(G/(R*T))*(2/(G+1))**((G+1)/(2*(G-1))) if ratio<=critical else math.sqrt(2*G/(R*T*(G-1))*(ratio**(2/G)-ratio**((G+1)/G)))
 return cd*area*up*factor
rows=[]
for litres in [20,30]:
 p=ATM;dt=.001;seconds=0
 while p<ATM+4.5*UNIT and seconds<200:
  p+=flow(ATM+5*UNIT,p,2.2)*R*T/(litres/1000)*dt;seconds+=dt
 q=ATM+5*UNIT;drop_time=0
 while q>ATM+4.5*UNIT:
  q-=flow(q,ATM,2.2)*R*T/(litres/1000)*dt;drop_time+=dt
 rows.append({'volume_l':litres,'hypothetical_inlet_diameter_mm':2.2,'hypothetical_outlet_diameter_mm':2.2,'assumed_discharge_coefficient':.7,'temperature_K':T,'fill_0_to_4_5_s':round(seconds,3),'discharge_5_to_4_5_s':round(drop_time,3)})
out={'scope':'Independent isothermal compressible-orifice calculation, NOT installed and NOT proof of stand plumbing or either flow diameter','rows':rows,'normative_fill_20l_s':[30,40],'documented_body_throttle_diameter_mm':2.2,'body_throttle_tolerance_mm':.01,'throttle_assigned_to_inlet_or_outlet':False,'source':'RZD3508/r 9.11.1 and table9.2; fig9.11 labels PM, TC and atmosphere but does not establish the internal throttle flow assignment or connected stand volume'}
Path('outputs/Trainer_KRU_orifice_reference.json').write_text(json.dumps(out,indent=2),encoding='utf-8');print(json.dumps(out))
