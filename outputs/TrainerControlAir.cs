// RE6 4.2 control-air topology. Passage sizes and KM2 flow are explicit caller inputs.
using System;
namespace TrainerAir {
 public sealed class ControlAirSection {
  public readonly Reservoir Rs6,Rs7;
  readonly Reservoir pm;readonly IdealRelay reducer;readonly double passage,cd,initialMass;
  double lastObservedPmMass;
  // For a combined network, count only this section's reservoirs and KM2 source.
  // Shared-PM boundary observations below are local diagnostics, not an extra air source.
  public double StoredMass {get{return Rs6.Mass+Rs7.Mass;}}
  public double AuxiliarySourceMass {get;private set;}
  public double SharedPmBoundaryMass {get;private set;}
  public double ExternalMass {get;private set;}
  public double ExhaustedMass {get;private set;}
  public double MaximumBalanceError {get;private set;}
  public bool PantographPressureReady {get{return Rs6.Gauge>=3.5;}}
  public double TotalMass {get{return pm.Mass+Rs6.Mass+Rs7.Mass;}}
  public ControlAirSection(Reservoir main,double initialControl,double initialReserve,double passageMm,double dischargeCoefficient){
   if(main==null)throw new ArgumentNullException("main");
   Flow.Finite(passageMm);Flow.Finite(dischargeCoefficient);
   if(passageMm<=0||dischargeCoefficient<=0||dischargeCoefficient>1)throw new ArgumentException("Control-air passage");
   pm=main;passage=passageMm;cd=dischargeCoefficient;
   Rs6=new Reservoir(55,initialControl,main.Temperature);Rs7=new Reservoir(150,initialReserve,main.Temperature);
   // A feed regulator does not empty a retained control-air reserve when PM drops.
   reducer=new IdealRelay(passageMm,0,dischargeCoefficient,.001);initialMass=TotalMass;lastObservedPmMass=pm.Mass;
  }
  // Normal-volume flow belongs to KM2, never the main-reservoir compressor.
  // This boundary input intentionally does not assume an equipment-specific KM2 rate.
  public void Step(double seconds,bool kn7Open,bool kep11Open,double auxiliaryNormalM3Min,double consumerDiameterMm){
   Flow.Finite(seconds);Flow.Finite(auxiliaryNormalM3Min);Flow.Finite(consumerDiameterMm);
   if(seconds<0||seconds>1||auxiliaryNormalM3Min<0||consumerDiameterMm<0)throw new ArgumentException("Control-air step");
   int count=(int)Math.Ceiling(seconds/.01);if(count==0)return;double dt=seconds/count;
   double sharedBoundary=pm.Mass-lastObservedPmMass;
   SharedPmBoundaryMass+=sharedBoundary;ExternalMass+=sharedBoundary;
   for(int i=0;i<count;i++){
    double input=auxiliaryNormalM3Min/60*dt*Flow.Atmosphere/(Flow.R*Rs6.Temperature);
    Rs6.ApplyBoundaryMass(input);ExternalMass+=input;AuxiliarySourceMass+=input;
    ExhaustedMass+=reducer.StepSetpoint(pm,Rs6,5,dt,kn7Open);
    // KEP11 connects the reserve to the control line in both directions;
    // closing it preserves RS7 instead of venting the reserve.
    if(kep11Open)Flow.Transfer(Rs6,Rs7,dt,passage,cd,false);
    ExhaustedMass+=Flow.Transfer(Rs6,null,dt,consumerDiameterMm,cd,true);
    if(Rs6.Gauge>7.5){double excess=(Rs6.Gauge-7.5)*Flow.Kgf*Rs6.Volume/(Flow.R*Rs6.Temperature);Rs6.ApplyBoundaryMass(-excess);ExhaustedMass+=excess;}
   }
   lastObservedPmMass=pm.Mass;
   MaximumBalanceError=Math.Max(MaximumBalanceError,Math.Abs(TotalMass+ExhaustedMass-initialMass-ExternalMass));
   if(MaximumBalanceError>1e-8)throw new InvalidOperationException("Control-air mass balance");
  }
 }
}
