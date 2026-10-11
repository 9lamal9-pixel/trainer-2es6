// Two relays per section; valve geometry is explicit and still uncalibrated.
using System;
namespace TrainerAir {
 public sealed class BtoSection {
  public readonly Reservoir Feed;
  public readonly Reservoir[] Control=new Reservoir[2],Output=new Reservoir[2],Cylinder=new Reservoir[2];
  public readonly bool[] FeedCock=new[]{true,true},ControlCock=new[]{true,true},CylinderCock=new[]{true,true};
  public bool PmFeedOpen=true,ColdTransportOpen=false;
  readonly IdealRelay relay;readonly double line,feedLine,exhaust,cd;
  public BtoSection(double initialFeed,double controlLitres,double outputLitres,double cylinderLitres,
    double inletMm,double exhaustMm,double lineMm,double feedLineMm,double dischargeCoefficient,double deadbandKgf){
   Flow.Finite(lineMm);Flow.Finite(feedLineMm);if(lineMm<0||feedLineMm<0)throw new ArgumentException("BTO passages");
   relay=new IdealRelay(inletMm,exhaustMm,dischargeCoefficient,deadbandKgf);
   line=lineMm;feedLine=feedLineMm;exhaust=exhaustMm;cd=dischargeCoefficient;
   Feed=new Reservoir(150,initialFeed,293.15);
   for(int i=0;i<2;i++){Control[i]=new Reservoir(controlLitres,0,293.15);Output[i]=new Reservoir(outputLitres,0,293.15);Cylinder[i]=new Reservoir(cylinderLitres,0,293.15);}
  }
  public double TotalMass {get{double m=Feed.Mass;for(int i=0;i<2;i++)m+=Control[i].Mass+Output[i].Mass+Cylinder[i].Mass;return m;}}
  public double Step(Reservoir pm,Reservoir tm,Reservoir signal,double dt){
   Flow.Finite(dt);if(dt<0||pm==null||tm==null||signal==null)throw new ArgumentException("BTO input");
   if(PmFeedOpen)Flow.Transfer(pm,Feed,dt,feedLine,cd,true);
   if(ColdTransportOpen)Flow.Transfer(tm,Feed,dt,feedLine,cd,true);
   double vent=0;
   for(int i=0;i<2;i++){
    if(ControlCock[i])Flow.Transfer(signal,Control[i],dt,line,cd,false);
    vent+=relay.Step(Feed,Output[i],Control[i],dt,FeedCock[i]);
    // KR6/KR7: closed inlet is isolated and the cylinder side vents.
    if(CylinderCock[i])Flow.Transfer(Output[i],Cylinder[i],dt,line,cd,false);
    else vent+=Flow.Transfer(Cylinder[i],null,dt,exhaust,cd,true);
   }
   return vent;
  }
  public void DisconnectBogie(int bogie){if(bogie<1||bogie>2)throw new ArgumentException("Bogie1 or2");int i=bogie-1;FeedCock[i]=ControlCock[i]=CylinderCock[i]=false;}
 }
}
