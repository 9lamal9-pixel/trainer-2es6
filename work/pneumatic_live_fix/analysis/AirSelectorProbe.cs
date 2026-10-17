using System;using System.IO;using TrainerAir;
class AirSelectorProbe {
 static void Check(bool b,string m){if(!b)throw new Exception(m);}
 static int Main(){try{
  var a=new Reservoir(20,3,293.15);var b=new Reservoir(20,2,293.15);var c=new Reservoir(1,0,293.15);var valve=new PressureSelector(6,.7);
  double total=a.Mass+b.Mass+c.Mass,isolated=b.Mass;
  for(int n=0;n<3000;n++)valve.Step(a,b,c,.01);
  Check(valve.Selected==0&&c.Gauge>2.8,"First source selected");Check(b.Mass==isolated,"Low source isolated");
  Check(Math.Abs(a.Mass+b.Mass+c.Mass-total)<1e-11,"Fill mass");
  double vent=0;for(int n=0;n<3000;n++)vent+=Flow.Transfer(a,null,.01,6,.7,true);
  double isolatedA=a.Mass;
  for(int n=0;n<3000;n++)valve.Step(a,b,c,.01);
  Check(valve.Selected==1&&c.Gauge>2&&c.Gauge<2.1,"Second source takeover and return flow");Check(a.Mass==isolatedA,"First source remains isolated");
  Check(Math.Abs(a.Mass+b.Mass+c.Mass+vent-total)<1e-10,"Switch mass");
  // With both inputs exhausted, connected control chamber can release through its selected source.
  for(int n=0;n<30000;n++){vent+=Flow.Transfer(a,null,.01,6,.7,true);vent+=Flow.Transfer(b,null,.01,6,.7,true);valve.Step(a,b,c,.01);}
  Check(c.Gauge<.001,"Control release");Check(Math.Abs(a.Mass+b.Mass+c.Mass+vent-total)<1e-10,"Release mass");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_CSharp_selector_validation.json"),"{\"passed\":true,\"installed\":false,\"calibrated_to_RE\":false,\"scope\":\"ideal shuttle, inlet isolation, takeover, return release and mass balance\"}");
  Console.WriteLine("PASS pressure selector; not installed");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
