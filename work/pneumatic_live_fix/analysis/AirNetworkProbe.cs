using System;using System.IO;using TrainerAir;
class AirNetworkProbe {
 static void Check(bool b,string m){if(!b)throw new Exception(m);}
 static int Main(){try{
  foreach(double dt in new[]{.001,.01,.1,10.0}){
   var a=new Reservoir(150,8,293.15);var b=new Reservoir(20,0,293.15);double mass=a.Mass+b.Mass;
   for(int i=0;i<100000;i++)Flow.Transfer(a,b,dt,6,.7,true);
   Check(Math.Abs(a.Gauge-1200.0/170)<1e-8,"Equalisation");Check(Math.Abs(a.Mass+b.Mass-mass)<1e-11,"Mass balance");
   double old=a.Mass;Flow.Transfer(b,a,1,6,.7,true);Check(Math.Abs(a.Mass-old)<1e-11,"Nonreturn");
  }
  var feed=new Reservoir(150,8,293.15);var tc=new Reservoir(12,0,293.15);var signal=new Reservoir(20,3,293.15);var relay=new IdealRelay(6,6,.7,.001);
  double initial=feed.Mass+tc.Mass,controlMass=signal.Mass;
  for(int i=0;i<3000;i++)relay.Step(feed,tc,signal,.01,true);
  Check(tc.Gauge>2.99&&tc.Gauge<=3.001,"TC target");Check(feed.Gauge<8,"Feed consumption");Check(signal.Mass==controlMass,"Control is not supply");
  Check(Math.Abs(feed.Mass+tc.Mass-initial)<1e-11,"Relay balance");
  var empty=new Reservoir(150,0,293.15);var emptyTc=new Reservoir(12,0,293.15);
  for(int i=0;i<3000;i++)relay.Step(empty,emptyTc,signal,.01,true);
  Check(Math.Abs(emptyTc.Gauge)<1e-10,"Empty feed creates pressure");
  var release=new Reservoir(20,0,293.15);double vent=0;
  for(int i=0;i<3000;i++)vent+=relay.Step(feed,tc,release,.01,false);
  Check(tc.Gauge<.0011,"Release with closed feed");Check(Math.Abs(feed.Mass+tc.Mass+vent-initial)<1e-10,"Exhaust balance");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_CSharp_air_network_validation.json"),"{\"passed\":true,\"installed\":false,\"calibrated_to_RE\":false,\"scope\":\"isothermal CSharp primitives, analytic equalisation, relay source limits and mass balance\"}");
  Console.WriteLine("PASS CSharp air network; not installed");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
