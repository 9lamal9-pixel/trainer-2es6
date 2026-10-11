using System;using System.IO;using TrainerAir;
class PilotFeedProbe {
 static void Check(bool b,string m){if(!b)throw new Exception(m);}
 static int Main(){try{
  var valve=new IdealRelay(6,6,.7,.001);var pm=new Reservoir(2000,8,293.15);var pilot=new Reservoir(20,0,293.15);
  double mass=pm.Mass+pilot.Mass;
  for(int n=0;n<3000;n++)valve.StepSetpoint(pm,pilot,4.1,.01,true);
  Check(pilot.Gauge>4.09&&pilot.Gauge<=4.101,"Pilot setpoint");Check(pm.Gauge<8,"Pilot debit from PM");Check(Math.Abs(pm.Mass+pilot.Mass-mass)<1e-10,"Pilot mass");
  var low=new Reservoir(2000,2,293.15);var lowPilot=new Reservoir(20,0,293.15);
  for(int n=0;n<3000;n++)valve.StepSetpoint(low,lowPilot,4.1,.01,true);
  Check(lowPilot.Gauge<=2&&lowPilot.Gauge<=low.Gauge+.001,"Supply limit");
  var empty=new Reservoir(2000,0,293.15);var emptyPilot=new Reservoir(20,0,293.15);
  for(int n=0;n<3000;n++)valve.StepSetpoint(empty,emptyPilot,4.1,.01,true);
  Check(Math.Abs(emptyPilot.Gauge)<1e-10,"No pilot pressure from empty PM");
  double trapped=pilot.Mass;for(int n=0;n<1000;n++)valve.StepSetpoint(pm,pilot,5,.01,false);
  Check(pilot.Mass==trapped,"Feed isolation");
  double vent=0;for(int n=0;n<3000;n++)vent+=valve.StepSetpoint(pm,pilot,0,.01,false);
  Check(pilot.Gauge<.0011,"Pilot release");Check(Math.Abs(pm.Mass+pilot.Mass+vent-mass)<1e-10,"Pilot exhaust accounting");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_CSharp_pilot_validation.json"),"{\"passed\":true,\"installed\":false,\"calibrated_to_RE\":false,\"scope\":\"mechanical setpoint, PM-fed pilot, finite supply, isolation, release and mass conservation; test geometry\"}");
  Console.WriteLine("PASS PM-fed pilot; charged PM="+pm.Gauge+" low-pressure pilot="+lowPilot.Gauge+"; not installed");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
