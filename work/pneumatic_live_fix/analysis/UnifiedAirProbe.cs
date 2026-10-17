using System;using System.IO;using System.Web.Script.Serialization;using TrainerAir;
class UnifiedAirProbe {
 static void Check(bool value,string message){if(!value)throw new Exception(message);}
 static int Main(){try{
  var p=new TwoSectionAirParameters(2000,100,1,20,1,1,1,12,6,.7,.001);
  var air=new TwoSectionAirChain(p,8,5,8);air.InitializeControlAir(0,0);
  for(int i=0;i<3000;i++){
   air.ApplyNativePressureChanges(.0001,0,0);
   air.StepControls(i<1500?6:2,.1,true,false);
   air.StepControlAir(.1,new[]{true,true},new[]{true,false},new[]{0.0,0.0},new[]{0.0,0.0});
  }
  Check(air.ControlAir[0].Rs7.Gauge>4.99,"First reserve charge");Check(Math.Abs(air.ControlAir[1].Rs7.Gauge)<1e-6,"Second closed KEP11 leaked");
  Check(air.Sections[0].Cylinder[0].Gauge<.05,"Locomotive brake release");
  Check(air.MaximumBalanceError<1e-8,"Combined air balance");
  double retained=air.ControlAir[0].Rs7.Gauge;
  for(int i=0;i<1000;i++){air.StepControls(2,.1,true,false);air.StepControlAir(.1,new[]{false,false},new[]{false,false},new[]{.105,0.0},new[]{1.0,1.0});}
  Check(Math.Abs(air.ControlAir[0].Rs7.Gauge-retained)<1e-6,"SF3-off reserve not retained");
  Check(air.ControlAir[0].AuxiliarySourceMass>0&&air.ControlAir[1].AuxiliarySourceMass==0,"Independent section auxiliary source");
  bool rejected=false;try{air.StepControlAir(.1,new[]{true,true},new[]{true,true},new[]{.105,-1.0},new[]{0.0,0.0});}catch(ArgumentException){rejected=true;}Check(rejected,"Invalid second-section input accepted");
  var result=new{passed=true,cycles=4000,host_connected=false,final_pm=air.Pm.Gauge,max_combined_mass_error=air.MaximumBalanceError,first_reserve=air.ControlAir[0].Rs7.Gauge,second_isolated_reserve=air.ControlAir[1].Rs7.Gauge,scope="Unified finite circuit: independent brakes, two control sections, native main-pressure boundary, one KM2 source, isolated reserve and consumer tests; electrical states are explicit stand inputs"};string text=new JavaScriptSerializer().Serialize(result);File.WriteAllText(TrainerSession.ResultPath("Trainer_unified_air_validation.json"),text);Console.WriteLine(text);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
