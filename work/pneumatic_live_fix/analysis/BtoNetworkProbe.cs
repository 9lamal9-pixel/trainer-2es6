using System;using System.IO;using TrainerAir;
class BtoNetworkProbe {
 static void Check(bool b,string m){if(!b)throw new Exception(m);}
 static int Main(){try{
  var sections=new[]{new BtoSection(0,1,1,12,6,6,6,6,.7,.001),new BtoSection(0,1,1,12,6,6,6,6,.7,.001)};
  var pm=new Reservoir(2000,8,293.15);var tm=new Reservoir(100,5,293.15);
  var signals=new[]{new Reservoir(20,3,293.15),new Reservoir(20,3,293.15)};
  double initial=pm.Mass+tm.Mass+signals[0].Mass+signals[1].Mass+sections[0].TotalMass+sections[1].TotalMass,vent=0;
  for(int n=0;n<6000;n++){
   if(n==3000){sections[0].DisconnectBogie(1);Console.WriteLine("TC before disconnect="+sections[0].Cylinder[0].Gauge);}
   for(int i=0;i<2;i++)vent+=sections[i].Step(pm,tm,signals[i],.01);
   double mass=pm.Mass+tm.Mass+signals[0].Mass+signals[1].Mass+sections[0].TotalMass+sections[1].TotalMass;
   Check(Math.Abs(mass+vent-initial)<1e-10,"BTO mass balance");
  }
  Check(sections[0].Cylinder[0].Gauge<.001,"Disconnected bogie release");
  Check(sections[0].Cylinder[1].Gauge>2.5&&sections[1].Cylinder[0].Gauge>2.5&&sections[1].Cylinder[1].Gauge>2.5,"Other bogies retain brake");
  Check(pm.Gauge<8,"PM consumption");Check(Math.Abs(tm.Gauge-5)<1e-10,"Cold transport path closed");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_CSharp_BTO_validation.json"),"{\"passed\":true,\"installed\":false,\"calibrated_to_RE\":false,\"scope\":\"two sections, four TC groups, selective isolation, PM debit, mass balance; test geometry\"}");
  Console.WriteLine("PASS CSharp BTO; PM="+pm.Gauge+"; not installed");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
