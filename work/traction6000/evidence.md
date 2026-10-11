# 2ЭС6 Tyga1.dll: numerical model recovered statically

Sample: `work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/Tyga1.dll`.
Only PE/RTTI/TD32 parsing and disassembly were used. The sample was not loaded or executed.
All addresses below are RVA (PE image base is 0x400000) unless explicitly marked VA.

## Types and constant extraction

`TConst` RTTI VA0x5713d8 describes the 1048-byte record. `InitConst` RVA0x171aec is a straight-line initializer: `constants.py` recovered all 1048 bytes, with instruction-byte provenance in `TConst.writes.json`. These are initialized values, not values read from a displayed configuration.

Relevant fields: Rya+0x39c=0.065899998; Rgp+0x3a0=0.017999999; Rshunt_R15_R16+0x3b8=8; K5+0x3cc=-1/260; K4+0x3d4=29; R2+0x3dc=.005443; R1+0x3e4=.035839; Kper+0x3ec=3.44; Rkol+0x3f4=.625; Eset+0x3fc=3200. Thus the modeled wheel diameter is1250mm.

`TKontroller.VozbType` is passed at TygaParam+0x16. `ProceedTyga` RVA0x17c3d5 selects 0→GetCurPosl,1→CalkNezavVozb or CalcRecup. Up to4sections are supported in arrays; the following numbers assume2sections, all4pairs/8motors enabled. GetCountVklTED counts enabled pairs, not individual motors. Both members of a pair share modeled values.

`TygaParam.ResT` pointer+0x27→TResTyga: Tyga+0, Cur+4, CurLoc+8, Box+0xc, CurVozb+0xd, Uset+0x11, Raskhod+0x15, RecupToTormModFlag+0x19. `ResTIT` contains8Single Tok/Ivozb/Moment/TygaTED slots. Mass is not an input to this traction module; train physics feeds speed back to it.

## Active flux and force formula

CalcClass.GetCF RVA0x1781b4: `φ(x)=29*(1-exp(-x/260))`.
GetCurVozbReal RVA0x17a750: `x=Iexc/h`, `h=1+2*Rgp/8=1.004499999806285`.
GetCur force loop RVA0x179731–0x1797c4 calls these two functions, multiplies by Ia and literal2 for each enabled pair, storing Moment. GetCurPosl has the same force loop at0x17a32b–0x17a3bb, using Ia as Iexc.

For all8motors at equal currents:

```
φ=29*(1-exp(-(Iexc/h)/260))
M_pair=2*φ*Ia
F_kN=3.44*ΣM_pair/(1000*.625*1.3)
    =3.44*8*φ*Ia/(1000*.625*1.3)
```

Evidence: GetCur total-force store RVA0x1798f2–0x17992c; `fld Kper`, `fmul ΣM`, denominator literal10000 *Rkol*literal1.3, store ResT.Tyga. The factor1000 above includes the10kN/unit conversion established separately by POEZD/caller analysis. ProceedTyga RVA0x17c4bc–0x17c4dc multiplies traction by Revers. Electric braking instead receives an additional×1.3 at0x17c492–0x17c4b7, undoing its earlier divisor.

TConst.GetCF RVA0x171a58 is a different hyperbolic approximation `.798*Hyper(I,41.550159,-1131.285400,158.076431)`. The active GetCur/CalcRecup force chain calls the exponential CalcClass.GetCF, not this function. Thus `.798` must not be used as a compensation factor in the force comparison.

Same-current calibration, with the existing1250mm wheel and÷1.3 already included:

|Ia=Iexc,A|φ|F,kN|
|--:|--:|--:|
|580|25.852778|507.879|
|540|25.331888|463.326|

Compared with parent-supplied factory464/418kN points, these are+9.46%/+10.84%. This compares the algebra at identical motor currents; it does not assert those currents are attained at factory rated speeds by the trainer. Identification of the exact modeled motor and applicability to locomotive137 are unresolved.

## Independent excitation current calculation

CalkNezavVozb RVA0x17650f explicitly writes ResT.Uset3200 if pantograph flag is true, otherwise0. A3000V value is not an external TygaParam input. GetCur reads the stored output Uset. Therefore3200 is the primary algebra table;3000 is only an optional substituted-voltage calculation.

GetEDS_Ya_El RVA0x17a8b4 sets each enabled pair's `E=2*φ(Iexc/h)*v`. SetVCur RVA0x17e5e0 changes sign according to Revers and does no3.6 conversion. GetU2 RVA0x17b82c sets each enabled pair's `u2=R2*Iexc`.

For2sections/all8motors/equal excitation, GetCur reduces to:

```
S : Ia=max(0,(U-4E+4u2)/(Rp(pos)+8Rya+4R2))
SP: Ia=max(0,(U-2E+2u2)/(Rp(pos)+4Rya+2R2))
P : Ia=max(0,(U- E+ u2)/(2Rp(pos)+4Rya+2R2))
```

The P denominator is explicitly4Rya+2R2 with both pairs enabled per section (RVA0x178d73–0x178ddd), despite differing from the sequential model. Disabled-motor branches have other denominators and position offsets. Do not substitute the simpler single-pair equation for the8motor case. Negative Ia is cleared at0x1796cd–0x17971d.

Rheostat tables:65Single Rp entries, all extracted toTConst.json. Rp23=Rp44=Rp65=0. Control grouping:positions1..23S,24..44SP,45..65P; commanded SetSoedTED can select intermediate transition positions according to current thresholds. There is no demonstrated automatic traction mode selection solely from speed.

The Delphi set at VA0x59040c has bytes`010080000010000002000000`: exactlypositions0,23,44,65. GetFlagHodPos RVA0x17ac0c performs BT on that set. These are the modeled non-rheostat/hod positions.

## Regulator limits and order matters

- SetProcentTyga RVA0x17e1cc accepts integer`-99..119` (greater than-100 and less than120). Positive target is GetMaxTyga(v)*percent/100.100 is valid; SetCurCommandOV repeatedly increments it, with no explicit99 ceiling in that method.119 is an internal accepted upper value; actual physical-input availability is a separate question.
- GetMaxTyga(|v|) RVA0x1718f8: below15 uses Hyper of OgrSc; from15 uses`56.748-.139|v|` in10kN units. This is the percent-reference/adhesion threshold, not a hard output-force clamp.
- GetProcentTygaTED/GetProcentTygaReal divide each pair's force by GetMaxTyga/4, round and compare to requested percent. The force states are independent of the raw controller position number.
- CalcIvozbMaxMin RVA0x1739bc sets max excitation800 at low speeds. AboveS16,SP32,P60 it Newton-solves a voltage equation for max excitation. CalkNezav global caps each enabled pair to1.05*max forS/P, ormax forSP (RVA0x1776ac–0x177a33). Min excitation resulting from the argument0 is0. Some non-hod correction branches additionally keep excitation390..700 when appropriate; these are not a universal hod-position lower limit.
- GetTempIVozb steps per call areS0.6/1.6/5,SP0.3/0.8/2.4,P0.15/0.4/1.2 at excitation<200/200..350/>350. Clock/per-call behavior is not a full transient simulation.
- DivIvozbIya RVA0x1780f8 returns true for stored Ia/Iexc>5, reversing field-step direction. Crucially, it reads currents from the preceding call after the750 clamp below.
- SetPos RVA0x17df17–0x17df33 refuses an increase if any stored current>700. It does not remove the present position or clamp existing force.
- **CalkNezav writes force through GetCur first, then clamps stored Tok to750** at RVA0x177c29–0x177c84. There is no force/Moment recalculation after this clamp. Thus750 limits the reported/state current, not the force that has already been written in the same call. With a mathematical raw current>750, output force and the stored current can disagree.
- CheckBox RVA0x177d68 compares individual pair force to GetMaxTyga*(KoefSc-randomDelta)/4, then sets randomly chosen section/pair box flags. DeltaKfc israndom signed0..0.10; no direct hard-force clamp was identified here. Subsequent traction decisions depend on these flags. Caller/POEZD force caps were excluded separately by configs agent, not inferred from Tyga alone.

##100% target roots,3200V,8motors

These are roots of the reconstructed equations, not DLL execution or a promise of complete dynamic reachability. Modes/positions are selected cases; the speed column does not itself choose traction connection. Higher-excitation roots are used, avoiding the low-field/high-current branch. The source numeric.py is a transcription, with Single rounding at the important stored fields; displayed values are rounded.

|v,km/h|mode/position|Iexc,A|raw Ia,A|stored Ia,A|Ftarget,kN|global excitation bound,A|stored Ia/Iexc|
|--:|:--|--:|--:|--:|--:|--:|--:|
|20|S23|225.515|950.066|750|539.680|320.699|3.326|
|40|SP44|268.490|811.364|750|511.880|304.821|2.793|
|49.2|SP44|188.157|989.578|750|499.092|214.109|3.986|
|51|SP44|177.771|1023.979|750|496.590|202.622|4.219|
|80|P65|272.351|717.371|717.371|456.280|319.745|2.634|
|120|P65|143.049|967.239|750|400.680|168.166|5.243|

All pass the global excitation bound. All stored currents exceed700 at the target, so further position increase is prohibited there. This does not prove the positions cannot have been reached earlier at a different field/current. Establishing the whole switching history requires a transient controller reconstruction or authorized runtime experiment.

The120km/h root additionally triggers the ratio>5 direction reversal, so its stable retention is unverified. At P65,v120 the direction-reversal boundary solves`min(750,Ia)/Iexc=5`:Iexc≈147.202,Ia≈736.012,F≈311.489kN. This is a branch boundary, not an identified hard-force cap. Avoid presenting400.68kN as a verified sustainable top-speed traction result.

99% roots and every examined position are in numeric.json, along with raw fixed-excitation and sequential-mode equations. Those auxiliary tables must not be used as a rated F(v)curve.

## Sequential mode

GetCurPosl RVA0x179d74 Newton-solves with start300/320, stopping at change≤.1A. With4enabled pairs:

```
S : U -8*φ(q)*v -q*(Rp+8Rya+4R1)*h =0
SP: U -4*φ(q)*v -q*(Rp+4Rya+2R1)*h =0
P : U -2*φ(q)*v -q*(2Rp+2Rya+R1)*h=0
Ia=q/h; SetCurPosl sets Iexc=Ia;
force then again usesφ(Ia/h).
```

Its real activation comes from Kont.VozbType0; the selected training configuration's mode state has not been proven here. Its arithmetic is fully distinct from the independent model and should not be mixed with that model's automatic percentage regulator.

Electric braking is documented separately in electrobrake/evidence.md: its final×1.3 creates a1.287*Bcurve algebraic target at−99%, and modes switch at entry thresholds16/38/69 rather than demonstrated continuous reselection. Braking current/network equations are not the positive traction equations above.
