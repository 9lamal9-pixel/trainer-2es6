using System;using System.IO;using System.Web.Script.Serialization;using TrainerAir;
class ControlAirProbe {
 static void Check(bool value,string message){if(!value)throw new Exception(message);}
 static int Main(){try{
  var pm=new Reservoir(2000,8,293.15);var air=new ControlAirSection(pm,0,0,6,.7);
  for(int i=0;i<3000;i++)air.Step(.1,true,true,0,0);
  Check(Math.Abs(air.Rs6.Gauge-5)<.003&&Math.Abs(air.Rs7.Gauge-5)<.003,"Regulated charge");
  double chargedPm=pm.Gauge;Check(Math.Abs(chargedPm-7.4875)<.001,"Charging must consume finite PM air");
  // PM collapse is a declared test boundary; use a new circuit with already charged control reserves.
  var lowPm=new Reservoir(2000,0,293.15);var retained=new ControlAirSection(lowPm,5,5,6,.7);
  for(int i=0;i<1000;i++)retained.Step(.1,true,false,0,1);
  Check(lowPm.Gauge<.00001,"Reverse flow through KO2");Check(Math.Abs(retained.Rs7.Gauge-5)<.00001,"Closed KEP11 reserve loss");
  double depleted=retained.Rs6.Gauge;Check(depleted<4.5,"Control consumer did not use finite reserve");
  for(int i=0;i<1000;i++)retained.Step(.1,true,true,0,0);
  Check(retained.Rs6.Gauge>depleted+.1&&retained.Rs7.Gauge<4.99,"RS7 must feed control line through open KEP11");
  var auxPm=new Reservoir(2000,0,293.15);var aux=new ControlAirSection(auxPm,0,0,6,.7);
  for(int i=0;i<3000;i++)aux.Step(.1,false,false,.1,0);
  Check(aux.Rs6.Gauge<=7.500001&&aux.Rs6.Gauge>7.49,"KM2 relief pressure");Check(auxPm.Gauge<.00001&&aux.Rs7.Gauge<.00001,"KM2 wrongly charged isolated circuits");
  // RE6 table3.3 D-100 nominal105l/min. This is a nominal-rate test,
  // not a pressure-dependent delivery curve or a validated complete startup.
  var coldPm=new Reservoir(2000,0,293.15);var cold=new ControlAirSection(coldPm,0,0,6,.7);
  int readyTicks=0;while(!cold.PantographPressureReady&&readyTicks<5000){cold.Step(.1,false,true,.105,0);readyTicks++;}
  Check(readyTicks>3500&&readyTicks<4500,"D-100 cold charge duration inconsistent with205l reserve");
  Check(coldPm.Gauge<.00001,"D-100 cold startup must not charge PM");
  // Both locomotive sections share a finite PM with an independently accounted
  // main compressor and a brake consumer. Do not double-count PM or its boundary.
  var sharedPm=new Reservoir(2000,8,293.15);var section1=new ControlAirSection(sharedPm,0,0,6,.7);var section2=new ControlAirSection(sharedPm,0,0,6,.7);
  var consumer=new Reservoir(40,0,293.15);double initialMass=sharedPm.Mass+section1.StoredMass+section2.StoredMass+consumer.Mass;
  double supplied=0,exhausted=0,globalError=0;
  for(int i=0;i<4000;i++){
   double source=.0001;sharedPm.ApplyBoundaryMass(source);supplied+=source;
   Flow.Transfer(sharedPm,consumer,.1,1,.7,true);exhausted+=Flow.Transfer(consumer,null,.1,.5,.7,true);
   section1.Step(.1,true,true,0,0);section2.Step(.1,true,true,0,0);
   globalError=Math.Max(globalError,Math.Abs(sharedPm.Mass+section1.StoredMass+section2.StoredMass+consumer.Mass+exhausted+section1.ExhaustedMass+section2.ExhaustedMass-initialMass-supplied-section1.AuxiliarySourceMass-section2.AuxiliarySourceMass));
  }
  Check(globalError<1e-8&&section1.MaximumBalanceError<1e-8&&section2.MaximumBalanceError<1e-8,"Shared PM mass accounting");
  Check(section1.AuxiliarySourceMass==0&&section2.AuxiliarySourceMass==0,"Main compressor counted as KM2 source");
  var result=new{passed=true,host_connected=false,geometry_calibrated=false,source="https://rcit.su/techinfoV1.html RE6 4.2 and table3.3",charged_pm=chargedPm,rs6=air.Rs6.Gauge,rs7=air.Rs7.Gauge,isolated_control_after_consumption=depleted,auxiliary_limited_pressure=aux.Rs6.Gauge,d100_nominal_l_min=105,cold_205l_ready_seconds=readyTicks*.1,cold_main_pressure=coldPm.Gauge,max_mass_error=Math.Max(cold.MaximumBalanceError,Math.Max(air.MaximumBalanceError,Math.Max(retained.MaximumBalanceError,aux.MaximumBalanceError))),test_assumptions="6mm passages, Cd0.7, 1mm consumer, isolated safety test flow0.1m3/min; D-100105l/min treated as constant intake delivery for nominal-rate test, no pressure delivery curve or actuator air; not equipment calibration"};
  string text=new JavaScriptSerializer().Serialize(result);File.WriteAllText(TrainerSession.ResultPath("Trainer_control_air_validation.json"),text);Console.WriteLine(text);
  var sharedResult=new{passed=true,cycles=4000,global_mass_error=globalError,first_local_mass_error=section1.MaximumBalanceError,second_local_mass_error=section2.MaximumBalanceError,main_source_mass=supplied,auxiliary_source_mass=section1.AuxiliarySourceMass+section2.AuxiliarySourceMass,final_pm=sharedPm.Gauge,scope="Two control-air sections, shared PM, external main source and brake-consumer test; no native wiring or equipment flow calibration"};
  text=new JavaScriptSerializer().Serialize(sharedResult);File.WriteAllText(TrainerSession.ResultPath("Trainer_control_air_shared_pm_validation.json"),text);Console.WriteLine(text);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
