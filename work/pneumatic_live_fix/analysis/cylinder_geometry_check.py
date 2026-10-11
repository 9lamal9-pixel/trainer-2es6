import json, math
from pathlib import Path
from cylinder_geometry import swept_litres, working_litres
assert abs(swept_litres(200,100)-math.pi) < 1e-12
assert swept_litres(203,0,4) == 0
assert abs(working_litres(203,100,4,.2,.5)-swept_litres(203,100,4)-1.3) < 1e-12
for args in ((0,100,4),(203,-1,4),(203,100,0),(203,100,True)):
    try: swept_litres(*args)
    except ValueError: pass
    else: raise AssertionError('Invalid geometry accepted')
out=dict(passed=True,installed=False,revision_137_confirmed=False,
         source='https://zinref.ru/000_uchebniki/04600_raznie_6/358_Elektrovoz_2ES6_rukovodstvo/048.htm',
         reference='2ES6 RE5 table3.2 and individual cylinder per wheel description; mirror contains mixed neighbouring documents',
         bore_mm=203,working_stroke_mm=100,cylinders_per_bogie=4,
         cylinders_per_section=8,cylinders_total=16,
         swept_litres_per_cylinder=swept_litres(203,100),
         swept_litres_per_bogie=swept_litres(203,100,4),
         swept_litres_total=swept_litres(203,100,16),
         limitations=['Swept volume is not complete pneumatic volume',
                     'Dead space, connecting pipes and actual stroke law remain required',
                     'Do not substitute maximum screw extension for piston working stroke',
                     'Native brake force conversion still requires independent audit'])
Path('outputs/Trainer_TC_geometry_validation.json').write_text(json.dumps(out,indent=2),encoding='utf-8')
print('PASS cylinder geometry; bogie swept volume',out['swept_litres_per_bogie'],'L; not installed')
