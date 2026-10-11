// Ideal pressure-operated shuttle. Mechanical switching delays remain uncalibrated.
using System;
namespace TrainerAir {
 public sealed class PressureSelector {
  readonly double diameter,cd;
  public int Selected {get;private set;}
  public PressureSelector(double diameterMm,double dischargeCoefficient){
   Flow.Finite(diameterMm);Flow.Finite(dischargeCoefficient);
   if(diameterMm<0||dischargeCoefficient<0||dischargeCoefficient>1)throw new ArgumentException("Selector geometry");
   diameter=diameterMm;cd=dischargeCoefficient;Selected=0;
  }
  public double Step(Reservoir first,Reservoir second,Reservoir outlet,double dt){
   if(first==null||second==null||outlet==null||ReferenceEquals(first,second)||ReferenceEquals(first,outlet)||ReferenceEquals(second,outlet))throw new ArgumentException("Selector ports");
   Flow.Finite(dt);if(dt<0)throw new ArgumentException("Selector time");
   // Retain the last seat at equal inlet pressures, avoiding arbitrary tie changes.
   if(first.Pressure>second.Pressure)Selected=0;
   else if(second.Pressure>first.Pressure)Selected=1;
   // Connected port permits both filling and return during upstream release.
   return Flow.Transfer(Selected==0?first:second,outlet,dt,diameter,cd,false);
  }
 }
}
