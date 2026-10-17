using System;using System.IO;using System.Collections.Generic;using System.Web.Script.Serialization;using TrainerAir;
class CombinedBrakeProbe {
 static void Check(bool b,string m){if(!b)throw new Exception(m);}
 static int Main(){try{
  var pm=new Reservoir(2000,8,293.15);var tm=new Reservoir(100,5,293.15);var independent=new Reservoir(20,0,293.15);var independentValve=new IdealRelay(6,6,.7,.001);
  var sections=new[]{new BtoSection(8,1,1,12,6,6,6,6,.7,.001),new BtoSection(8,1,1,12,6,6,6,6,.7,.001)};
  var reserve=new[]{new Reservoir(20,5,293.15),new Reservoir(20,5,293.15)};
  var automatic=new[]{new Reservoir(1,0,293.15),new Reservoir(1,0,293.15)};
  var automaticValve=new[]{new IdealRelay(6,6,.7,.001),new IdealRelay(6,6,.7,.001)};
  var signal=new[]{new Reservoir(1,0,293.15),new Reservoir(1,0,293.15)};
  var selector=new[]{new PressureSelector(6,.7),new PressureSelector(6,.7)};
  Func<double> mass=()=>pm.Mass+tm.Mass+independent.Mass+sections[0].TotalMass+sections[1].TotalMass+reserve[0].Mass+reserve[1].Mass+automatic[0].Mass+automatic[1].Mass+signal[0].Mass+signal[1].Mass;
  double initial=mass(),vent=0;var rows=new List<object>();
  for(int phase=0;phase<4;phase++){
   // Automatic commands are test fixtures fed from finite ZR; this is not a483A valve law.
   double autoTarget=phase<3?2.7:0,kvtTarget=phase==1?KvtProfile.IndependentSetpoint(6):0;
   for(int n=0;n<6000;n++){
    vent+=independentValve.StepSetpoint(pm,independent,kvtTarget,.01,true);
    for(int s=0;s<2;s++){
     vent+=automaticValve[s].StepSetpoint(reserve[s],automatic[s],autoTarget,.01,true);
     selector[s].Step(automatic[s],independent,signal[s],.01);
     vent+=sections[s].Step(pm,tm,signal[s],.01);
    }
    Check(Math.Abs(mass()+vent-initial)<1e-9,"Combined mass balance");
   }
   foreach(var section in sections)foreach(var cylinder in section.Cylinder){
    double expected=phase==1?3.9:phase==3?0:2.7;
    Check(Math.Abs(cylinder.Gauge-expected)<.03,"Wrong combined brake pressure at phase"+phase);
   }
   rows.Add(new{phase=phase,automatic_target=autoTarget,independent_target=kvtTarget,tc=sections[0].Cylinder[0].Gauge,pm=pm.Gauge,zr=reserve[0].Gauge,selected=selector[0].Selected});
  }
  Check(reserve[0].Gauge<5&&pm.Gauge<8,"Separate sources must debit");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_combined_brake_validation.json"),new JavaScriptSerializer().Serialize(new{passed=true,installed=false,calibrated_to_RE=false,scope="Automatic command fixture and KVT pilot through shuttle to four TC groups; auto hold during KVT release; finite sources; actual483A logic absent",rows=rows,mass_error=Math.Abs(mass()+vent-initial)}));
  Console.WriteLine("PASS combined brake: auto -> stronger KVT -> auto remains after KVT release -> full release; not installed");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
