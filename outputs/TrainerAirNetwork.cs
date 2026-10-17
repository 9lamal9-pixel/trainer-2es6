// Isothermal physical primitives. Not yet connected to the host or calibrated to RE.
using System;
namespace TrainerAir {
 public sealed class Reservoir {
  public readonly double Volume,Temperature; internal double mass;
  public Reservoir(double litres,double gauge,double temperature){
   Flow.Finite(litres);Flow.Finite(gauge);Flow.Finite(temperature);
   if(litres<=0||gauge<0||temperature<=0)throw new ArgumentException("Reservoir parameters");
   Volume=litres/1000;Temperature=temperature;mass=(Flow.Atmosphere+gauge*Flow.Kgf)*Volume/(Flow.R*Temperature);
  }
  public double Mass {get{return mass;}}
  public double Pressure {get{return mass*Flow.R*Temperature/Volume;}}
  public double Gauge {get{return (Pressure-Flow.Atmosphere)/Flow.Kgf;}}
  // Explicit boundary mass from an external compressor/pipe model; caller must account it.
  public void ApplyBoundaryMass(double delta){Flow.Finite(delta);double next=mass+delta;
   double minimum=Flow.Atmosphere*Volume/(Flow.R*Temperature);
   if(next<minimum-1e-12)throw new ArgumentException("Boundary removes more air than available");mass=next;
  }
 }
 public static class Flow {
  public const double Atmosphere=101325,Kgf=98066.5,R=287.05,Gamma=1.4;
  public static void Finite(double x){if(double.IsNaN(x)||double.IsInfinity(x))throw new ArgumentException("Nonfinite air parameter");}
  public static double Nozzle(double up,double down,double diameter,double cd,double temperature){
   foreach(double x in new[]{up,down,diameter,cd,temperature})Finite(x);
   if(up<=0||down<=0||diameter<0||cd<0||cd>1||temperature<=0)throw new ArgumentException("Nozzle parameters");
   if(up<=down||diameter==0||cd==0)return 0;
   double ratio=down/up,area=Math.PI*Math.Pow(diameter/1000,2)/4;
   double critical=Math.Pow(2/(Gamma+1),Gamma/(Gamma-1)),factor;
   if(ratio<=critical)factor=Math.Sqrt(Gamma/(R*temperature))*Math.Pow(2/(Gamma+1),(Gamma+1)/(2*(Gamma-1)));
   else factor=Math.Sqrt(Math.Max(0,2*Gamma/(R*temperature*(Gamma-1))*(Math.Pow(ratio,2/Gamma)-Math.Pow(ratio,(Gamma+1)/Gamma))));
   return cd*area*up*factor;
  }
  // null target means atmosphere. Reverse transfer requires an ordinary open line.
  public static double Transfer(Reservoir source,Reservoir target,double dt,double diameter,double cd,bool checkValve){
   Finite(dt);if(dt<0||source==null||ReferenceEquals(source,target))throw new ArgumentException("Transfer parameters");
   double down=target==null?Atmosphere:target.Pressure;
   if(source.Pressure<down&&!checkValve&&target!=null)return -Transfer(target,source,dt,diameter,cd,true);
   double rate=Nozzle(source.Pressure,down,diameter,cd,source.Temperature);
   double compliance=R*source.Temperature/source.Volume+(target==null?0:R*target.Temperature/target.Volume);
   double amount=Math.Min(rate*dt,Math.Max(0,(source.Pressure-down)/compliance));
   source.mass-=amount;if(target!=null)target.mass+=amount;return amount;
  }
 }
 public sealed class IdealRelay {
  readonly double inlet,exhaust,cd,deadband;
  public IdealRelay(double inletMm,double exhaustMm,double dischargeCoefficient,double deadbandKgf){
   foreach(double x in new[]{inletMm,exhaustMm,dischargeCoefficient,deadbandKgf})Flow.Finite(x);
   if(inletMm<0||exhaustMm<0||dischargeCoefficient<0||dischargeCoefficient>1||deadbandKgf<0)throw new ArgumentException("Relay parameters");
   inlet=inletMm;exhaust=exhaustMm;cd=dischargeCoefficient;deadband=deadbandKgf;
  }
  // Returns atmospheric exhaust mass; control mass is never used as TC supply.
  public double Step(Reservoir feed,Reservoir cylinder,Reservoir control,double dt,bool feedCock){
   Flow.Finite(dt);if(dt<0||feed==null||cylinder==null||control==null||ReferenceEquals(feed,cylinder)||ReferenceEquals(feed,control)||ReferenceEquals(control,cylinder))throw new ArgumentException("Relay ports");
   return StepTarget(feed,cylinder,Math.Max(Flow.Atmosphere,control.Pressure),dt,feedCock);
  }
  // Mechanical setpoint for regulator/KVT pilot; does not supply pneumatic mass.
  public double StepSetpoint(Reservoir feed,Reservoir outlet,double gaugeSetpoint,double dt,bool feedCock){
   Flow.Finite(gaugeSetpoint);if(gaugeSetpoint<0)throw new ArgumentException("Negative setpoint");
   return StepTarget(feed,outlet,Flow.Atmosphere+gaugeSetpoint*Flow.Kgf,dt,feedCock);
  }
  double StepTarget(Reservoir feed,Reservoir cylinder,double target,double dt,bool feedCock){
   Flow.Finite(dt);Flow.Finite(target);if(dt<0||feed==null||cylinder==null||ReferenceEquals(feed,cylinder))throw new ArgumentException("Regulator ports");
   double delta=target-cylinder.Pressure;
   if(Math.Abs(delta)<=deadband*Flow.Kgf)return 0;
   if(delta>0){if(!feedCock)return 0;double rate=Flow.Nozzle(feed.Pressure,cylinder.Pressure,inlet,cd,feed.Temperature);
    double need=delta*cylinder.Volume/(Flow.R*cylinder.Temperature);
    Flow.Transfer(feed,cylinder,rate>0?Math.Min(dt,need/rate):0,inlet,cd,true);return 0;}
   double outRate=Flow.Nozzle(cylinder.Pressure,Flow.Atmosphere,exhaust,cd,cylinder.Temperature);
   double outNeed=-delta*cylinder.Volume/(Flow.R*cylinder.Temperature);
   return Flow.Transfer(cylinder,null,outRate>0?Math.Min(dt,outNeed/outRate):0,exhaust,cd,true);
  }
 }
}
