"""Independent arithmetic from recovered formulas. Does not load simulator code."""
from pathlib import Path
import json, math
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np

root=Path(__file__).resolve().parents[1]
out=root/'outputs'
out.mkdir(exist_ok=True)
phi=lambda i:29*(1-math.exp(-i/1.0045/260))
force=lambda ia,exc:3.44*8*phi(exc)*ia/(1000*.625*1.3)
reference=[('Часовой',580,464,49.2),('Продолжительный',540,418,51)]
electric=json.loads((root/'work/traction6000/electrobrake/numeric.json').read_text())
resistance=lambda v:51.82+.304*v+.0076*v*v
grade=lambda mass,i:.00981*mass*i
def pneumatic_force(v,pressure,pad):
    lever,phi0,c1,c2,c3,c4=[(5.79,.055,20,5,41.7,20.85),(9.03,.031,6.25,1.92,27.78,5.56)][pad]
    press=994*pressure*lever*.00085
    return 10*phi0*press*(press+c1)/(press+c2)*(v/3.6+c3)/(v/3.6+c4)
values={
    'calibration':[dict(mode=name,Ia_A=i,Iexc_A=i,F_model_kN=force(i,i),
                        F_reference_kN=f,delta_pct=100*(force(i,i)/f-1),v_kmh=v)
                   for name,i,f,v in reference],
    'mass_real_t':6200,'mass_POEZD_t':6100,
    'wagon_count':75,'wagon_gross_t':80,'wagon_length_m':14.59,
    'length_from_full_library_sections_m':75*14.59+2*15.965,
    'length_POEZD_m':75*14.59+15.965,
    'mass_accel_bias_pct':100*(6200/6100-1),
    'resistance_kN':{str(v):resistance(v) for v in [20,40,51,80,120]},
    'grade_kN':{str(i):grade(6200,i) for i in [0,1,3,6,10]},
    'calibration_51_a_level_reference_mass_same_model_R':(418-resistance(51))/6200,
    'calibration_51_a_level_model':(force(540,540)-resistance(51))/6100,
    'calibration_51_a_6permil_reference_mass_same_model_R':(418-resistance(51)-grade(6200,6))/6200,
    'calibration_51_a_6permil_model':(force(540,540)-resistance(51)-grade(6100,6))/6100,
    'electric_brake':electric,
    'pneumatic_at_uniform_Ptc3_5':[dict(v_kmh=v,pad=pad,B_one_wagon_kN=pneumatic_force(v,3.5,pad),
                                       B_75_wagons_kN=75*pneumatic_force(v,3.5,pad))
                                  for v in [51,80,120] for pad in [0,1]],
    'kinetic_energy_at80_kWh':.5*6200*1000*(80/3.6)**2/3.6e6,
}
(root/'work/6000_calculations.json').write_text(json.dumps(values,ensure_ascii=False,indent=2),encoding='utf8')

plt.rcParams.update({'font.family':'DejaVu Sans','font.size':11,'axes.spines.top':False,
                     'axes.spines.right':False,'axes.titleweight':'bold'})
fig,axes=plt.subplots(1,2,figsize=(13,5.9),gridspec_kw={'width_ratios':[1.05,1]})
fig.patch.set_facecolor('#f6f8fb')
for ax in axes:
    ax.set_facecolor('#f6f8fb')
    ax.grid(axis='y',color='#dbe1ea',linewidth=.8,zorder=0)
x=np.arange(2)
factory=np.array([r[2] for r in reference])
model=np.array([force(r[1],r[1]) for r in reference])
axes[0].bar(x-.17,factory,.32,color='#486581',label='РЭ: заводская точка',zorder=3)
axes[0].bar(x+.17,model,.32,color='#dd8c35',label='Формула тренажёра',zorder=3)
axes[0].set_xticks(x,['580 А\nчасовой','540 А\nпродолжительный'])
axes[0].set_ylim(0,600)
axes[0].set_ylabel('Сила тяги, кН')
axes[0].set_title('Одинаковые токи якоря и возбуждения\nСопоставление с ЭДП810 из РЭ',fontsize=12)
for j in x:
    axes[0].text(j-.17,factory[j]+9,f'{factory[j]:.0f}',ha='center',fontsize=11)
    axes[0].text(j+.17,model[j]+9,f'{model[j]:.1f}',ha='center',fontsize=11)
    axes[0].text(j,560,f'+{(model[j]/factory[j]-1)*100:.1f}%',ha='center',color='#a55012',weight='bold')
axes[0].legend(loc='upper left',bbox_to_anchor=(0,-.17),frameon=False,fontsize=10)

vs=[r['V_kmh'] for r in electric]
ia=[abs(r['Ia_A']) for r in electric]
bx=np.arange(len(vs))
axes[1].bar(bx,ia,.55,color='#b95545',zorder=3,label='Алгебраическая цель −99%')
axes[1].axhline(500,color='#486581',ls='--',lw=1.5,label='РЭ ЭДП810: не более 500 А')
for j,i in zip(bx,ia):
    axes[1].text(j,i+10,f'{i:.0f} А',ha='center',fontsize=10)
axes[1].set_ylim(0,675)
axes[1].set_xticks(bx,['49,2','51','80','120'])
axes[1].set_xlabel('Скорость, км/ч')
axes[1].set_ylabel('Модуль тока якоря в электротормозе, А')
axes[1].set_title('Электротормоз: токи по формулам модели\nЧетыре включённые группы, восемь ТЭД',fontsize=12)
axes[1].legend(loc='upper left',bbox_to_anchor=(0,-.17),frameon=False,fontsize=10)
fig.suptitle('2ЭС6 + 6000 т вагонов: проверка калибровки',fontsize=18,weight='bold',x=.065,ha='left',y=.98)
fig.text(.065,.035,'Статическое восстановление кода; не прогон тренажёра. РЭ для выпуска с №137 не подтверждено.\n'
         'Слева — формула при заданных токах, справа — алгебраические состояния регулятора; устойчивость не проверена.',
         fontsize=10,color='#526273',va='bottom')
fig.subplots_adjust(left=.065,right=.965,top=.79,bottom=.27,wspace=.34)
fig.savefig(out/'2ES6_6000t_comparison.png',dpi=160,facecolor=fig.get_facecolor())
plt.close(fig)
for c in values['calibration']: print(c)
print('R51=',resistance(51),'R80=',resistance(80))
print('grade6200_6=',grade(6200,6),'length=',values['length_POEZD_m'])
print('a51 reference/model level:',values['calibration_51_a_level_reference_mass_same_model_R'],values['calibration_51_a_level_model'])
print('a51 reference/model grade6:',values['calibration_51_a_6permil_reference_mass_same_model_R'],values['calibration_51_a_6permil_model'])
