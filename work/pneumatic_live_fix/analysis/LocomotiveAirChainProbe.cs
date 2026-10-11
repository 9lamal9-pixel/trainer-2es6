using System;using System.IO;using System.Collections.Generic;using TrainerAir;using System.Web.Script.Serialization;
class LocomotiveAirChainProbe {
 static void Check(bool b,string m){if(!b)throw new Exception(m);}
 static int Main(){try{var results=new List<object>();
  foreach(double initialPm in new[]{0.0,2.0,8.0}){
   var pm=new Reservoir(2000,initialPm,293.15);var tm=new Reservoir(100,5,293.15);var pilot=new Reservoir(20,0,293.15);
   var regulator=new IdealRelay(6,6,.7,.001);
   var sections=new[]{new BtoSection(initialPm,1,1,12,6,6,6,6,.7,.001),new BtoSection(initialPm,1,1,12,6,6,6,6,.7,.001)};
   var automatic=new[]{new Reservoir(20,0,293.15),new Reservoir(20,0,293.15)};
   var signals=new[]{new Reservoir(1,0,293.15),new Reservoir(1,0,293.15)};
   var selectors=new[]{new PressureSelector(6,.7),new PressureSelector(6,.7)};
   Func<double> mass=()=>pm.Mass+tm.Mass+pilot.Mass+automatic[0].Mass+automatic[1].Mass+signals[0].Mass+signals[1].Mass+sections[0].TotalMass+sections[1].TotalMass;
   double initialMass=mass(),vent=0,firstTc=0,lastTc=0;var cycles=new List<object>();
   for(int cycle=0;cycle<10;cycle++){
    for(int phase=0;phase<2;phase++){
     for(int n=0;n<3000;n++){
      vent+=regulator.StepSetpoint(pm,pilot,phase==0?4.1:0,.01,true);
      for(int s=0;s<2;s++){selectors[s].Step(automatic[s],pilot,signals[s],.01);vent+=sections[s].Step(pm,tm,signals[s],.01);}
      Check(Math.Abs(mass()+vent-initialMass)<1e-9,"Full chain mass conservation");
     }
     double tc=sections[0].Cylinder[0].Gauge;
     if(phase==0){if(cycle==0)firstTc=tc;lastTc=tc;cycles.Add(new{cycle=cycle+1,pm=pm.Gauge,pilot=pilot.Gauge,tc=tc});
      if(initialPm==0)Check(Math.Abs(tc)<1e-9,"Empty system cannot brake");
      if(initialPm==2)Check(tc<=2.001,"Low supply limits brake");
      if(initialPm==8)Check(tc>4,"Charged system reaches commanded brake");
     }else foreach(var section in sections)foreach(var cylinder in section.Cylinder)Check(cylinder.Gauge<.02,"All groups release");
    }
   }
   if(initialPm>0)Check(pm.Gauge<initialPm,"Repeated cycle air debit");
   results.Add(new{initial_pm=initialPm,final_pm=pm.Gauge,first_tc=firstTc,last_tc=lastTc,exhaust_mass=vent,mass_error=Math.Abs(mass()+vent-initialMass),cycles=cycles});
  }
  var output=new{passed=true,installed=false,calibrated_to_RE=false,scope="PM-fed pilot, two selectors and two BTO sections, ten brake/release cycles, no compressor; explicit test volumes and passages",results=results};
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_CSharp_locomotive_chain_validation.json"),new JavaScriptSerializer().Serialize(output));
  Console.WriteLine("PASS assembled locomotive air chain, empty/low/charged supply, repeated cycles and mass conservation; not installed");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
