// Shared two-section air circuit. Geometry remains an explicit caller input.
using System;
namespace TrainerAir {
 public sealed class TwoSectionAirParameters {
  public readonly double PmLitres,TmLitres,AutomaticLitres,IndependentLitres,SignalLitres,ControlLitres,OutputLitres,CylinderLitres,PassageMm,Cd,Deadband;
  public TwoSectionAirParameters(double pmLitres,double tmLitres,double automaticLitres,double independentLitres,
   double signalLitres,double controlLitres,double outputLitres,double cylinderLitres,double passageMm,double cd,double deadband){
   foreach(double v in new[]{pmLitres,tmLitres,automaticLitres,independentLitres,signalLitres,controlLitres,outputLitres,cylinderLitres}){
    Flow.Finite(v);if(v<=0)throw new ArgumentException("Air circuit volume");
   }
   Flow.Finite(passageMm);Flow.Finite(cd);Flow.Finite(deadband);
   if(passageMm<=0||cd<=0||cd>1||deadband<0)throw new ArgumentException("Air circuit valve parameters");
   PmLitres=pmLitres;TmLitres=tmLitres;AutomaticLitres=automaticLitres;IndependentLitres=independentLitres;
   SignalLitres=signalLitres;ControlLitres=controlLitres;OutputLitres=outputLitres;CylinderLitres=cylinderLitres;
   PassageMm=passageMm;Cd=cd;Deadband=deadband;
  }
 }
 public sealed class TwoSectionAirChain {
  public readonly Reservoir Pm,Tm,Independent;
  public readonly Reservoir[] Automatic=new Reservoir[2],Signal=new Reservoir[2];
  public readonly BtoSection[] Sections=new BtoSection[2];
  readonly PressureSelector[] selectors=new PressureSelector[2];readonly IdealRelay independentValve;
  readonly Reservoir[] releasePorts=new Reservoir[2];readonly double passage,discharge;
  double initialMass;bool stepped;
  public ControlAirSection[] ControlAir {get;private set;}
  public double ExternalMass {get;private set;}
  public double ExhaustedMass {get;private set;}
  public double MaximumBalanceError {get;private set;}
  public TwoSectionAirChain(TwoSectionAirParameters p,double initialPm,double initialTm,double initialFeed){
   if(p==null)throw new ArgumentNullException("p");
   Pm=new Reservoir(p.PmLitres,initialPm,293.15);Tm=new Reservoir(p.TmLitres,initialTm,293.15);
   Independent=new Reservoir(p.IndependentLitres,0,293.15);
   independentValve=new IdealRelay(p.PassageMm,p.PassageMm,p.Cd,p.Deadband);
   passage=p.PassageMm;discharge=p.Cd;
   for(int s=0;s<2;s++){
    Automatic[s]=new Reservoir(p.AutomaticLitres,0,293.15);Signal[s]=new Reservoir(p.SignalLitres,0,293.15);
    Sections[s]=new BtoSection(initialFeed,p.ControlLitres,p.OutputLitres,p.CylinderLitres,p.PassageMm,p.PassageMm,p.PassageMm,p.PassageMm,p.Cd,p.Deadband);
    selectors[s]=new PressureSelector(p.PassageMm,p.Cd);
    releasePorts[s]=new Reservoir(p.SignalLitres,0,293.15);
   }
   initialMass=TotalMass;
  }
  public double TotalMass {get{double m=Pm.Mass+Tm.Mass+Independent.Mass;for(int s=0;s<2;s++){m+=Automatic[s].Mass+Signal[s].Mass+Sections[s].TotalMass+releasePorts[s].Mass;if(ControlAir!=null)m+=ControlAir[s].StoredMass;}return m;}}
  public void InitializeControlAir(double initialControl,double initialReserve){
   if(ControlAir!=null||stepped||ExternalMass!=0||ExhaustedMass!=0)throw new InvalidOperationException("Control-air initialization must precede network steps");
   var first=new ControlAirSection(Pm,initialControl,initialReserve,passage,discharge);
   var second=new ControlAirSection(Pm,initialControl,initialReserve,passage,discharge);
   ControlAir=new[]{first,second};initialMass+=first.StoredMass+second.StoredMass;CheckBalance();
  }
  // Caller supplies verified electrical/cock states; there is no forced KEP11 state.
  public void StepControlAir(double seconds,bool[] feedOpen,bool[] reserveOpen,double[] auxiliaryNormalM3Min,double[] consumerDiameterMm){
   if(ControlAir==null)throw new InvalidOperationException("Control air not initialized");
   if(feedOpen==null||reserveOpen==null||auxiliaryNormalM3Min==null||consumerDiameterMm==null||feedOpen.Length!=2||reserveOpen.Length!=2||auxiliaryNormalM3Min.Length!=2||consumerDiameterMm.Length!=2)throw new ArgumentException("Two-section control inputs");
   Flow.Finite(seconds);if(seconds<0||seconds>1)throw new ArgumentException("Control-air time step");
   for(int s=0;s<2;s++){Flow.Finite(auxiliaryNormalM3Min[s]);Flow.Finite(consumerDiameterMm[s]);if(auxiliaryNormalM3Min[s]<0||consumerDiameterMm[s]<0)throw new ArgumentException("Control-air flow input");}
   stepped=true;
   for(int s=0;s<2;s++){
    double source=ControlAir[s].AuxiliarySourceMass,exhaust=ControlAir[s].ExhaustedMass;
    ControlAir[s].Step(seconds,feedOpen[s],reserveOpen[s],auxiliaryNormalM3Min[s],consumerDiameterMm[s]);
    ExternalMass+=ControlAir[s].AuxiliarySourceMass-source;ExhaustedMass+=ControlAir[s].ExhaustedMass-exhaust;
   }
   CheckBalance();
  }
  // Delta is the native step's pressure change, not replacement of the physical state.
  // The caller owns the external BVR/compressor model and its separate mass accounting.
  public void ApplyNativePressureChanges(double pmDeltaKgf,double firstAutomaticDeltaKgf,double secondAutomaticDeltaKgf){
   Flow.Finite(pmDeltaKgf);Flow.Finite(firstAutomaticDeltaKgf);Flow.Finite(secondAutomaticDeltaKgf);
   double pm=MassForPressureDelta(Pm,pmDeltaKgf),a=MassForPressureDelta(Automatic[0],firstAutomaticDeltaKgf),b=MassForPressureDelta(Automatic[1],secondAutomaticDeltaKgf);
   // Validate all boundaries before modifying any reservoir.
   CheckBoundary(Pm,pm);CheckBoundary(Automatic[0],a);CheckBoundary(Automatic[1],b);
   stepped=true;Pm.ApplyBoundaryMass(pm);Automatic[0].ApplyBoundaryMass(a);Automatic[1].ApplyBoundaryMass(b);ExternalMass+=pm+a+b;
   CheckBalance();
  }
  static double MassForPressureDelta(Reservoir r,double delta){return delta*Flow.Kgf*r.Volume/(Flow.R*r.Temperature);}
  static void CheckBoundary(Reservoir r,double delta){if(r.Mass+delta<Flow.Atmosphere*r.Volume/(Flow.R*r.Temperature)-1e-12)throw new ArgumentException("Native boundary air deficit");}
  // This method controls only independent pressure. Automatic-release valve logic belongs to a separate control path.
  public void StepIndependent(int nativeCode,double seconds,bool feedOpen){
   StepControls(nativeCode,seconds,feedOpen,false);
  }
  // Actuated KEB2 blocks BVR supply and opens the downstream automatic-control branch to atmosphere.
  // This is valve-port logic, not the coil/pneumatic-actuator model; caller must establish actuation separately.
  public void StepControls(int nativeCode,double seconds,bool feedOpen,bool automaticReleaseActuated){StepControls(nativeCode,seconds,feedOpen,automaticReleaseActuated,true);}
  public void StepControls(int nativeCode,double seconds,bool feedOpen,bool automaticReleaseActuated,bool independentControlEnabled){
   double setpoint=KvtProfile.IndependentSetpoint(nativeCode);Flow.Finite(seconds);
   if(seconds<0||seconds>1)throw new ArgumentException("Air circuit time step");
   int steps=(int)Math.Ceiling(seconds/.01);if(steps==0)return;double dt=seconds/steps;
   stepped=true;
   for(int n=0;n<steps;n++){
    // Disabled KVT isolates both inlet and exhaust; automatic BVR/relay paths still step.
    if(independentControlEnabled)ExhaustedMass+=independentValve.StepSetpoint(Pm,Independent,setpoint,dt,feedOpen);
    for(int s=0;s<2;s++){
     ExhaustedMass+=Flow.Transfer(releasePorts[s],null,dt,passage,discharge,true);
     selectors[s].Step(automaticReleaseActuated?releasePorts[s]:Automatic[s],Independent,Signal[s],dt);
     ExhaustedMass+=Sections[s].Step(Pm,Tm,Signal[s],dt);
    }
   }
   CheckBalance();
  }
  void CheckBalance(){MaximumBalanceError=Math.Max(MaximumBalanceError,Math.Abs(TotalMass+ExhaustedMass-initialMass-ExternalMass));
   if(MaximumBalanceError>1e-8)throw new InvalidOperationException("Two-section air mass balance");
  }
 }
}

