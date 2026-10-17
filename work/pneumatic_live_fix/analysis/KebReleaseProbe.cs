using System;using System.IO;using TrainerAir;using System.Web.Script.Serialization;
class KebReleaseProbe {
 static void Phase(TwoSectionAirChain c,int kvt,bool release,int count){for(int i=0;i<count;i++)c.StepControls(kvt,.1,true,release);}
 static int Main(){try{
  var c=new TwoSectionAirChain(new TwoSectionAirParameters(2000,100,100,20,1,1,1,12,6,.7,.001),8,5,8);
  // Explicit fixed initial automatic pilot charge, not a running native BVR or a full-train brake test.
  c.ApplyNativePressureChanges(0,2.7,2.7);Phase(c,2,false,300);double braked=c.Sections[0].Cylinder[0].Gauge;
  Phase(c,2,true,300);double released=c.Sections[0].Cylinder[0].Gauge,preserved=c.Automatic[0].Gauge;
  if(braked<2||released>.05||preserved<2)throw new Exception("KEB2 isolation/release: "+braked+" / "+released+" / "+preserved);
  Phase(c,6,true,300);double independent=c.Sections[0].Cylinder[0].Gauge;if(independent<3.8)throw new Exception("KEB2 incorrectly releases independent brake");
  Phase(c,2,true,300);double releasedAgain=c.Sections[1].Cylinder[1].Gauge;if(releasedAgain>.05)throw new Exception("Second section release");
  Phase(c,2,false,300);double reapplied=c.Sections[0].Cylinder[0].Gauge,second=c.Sections[1].Cylinder[1].Gauge;
  if(reapplied<2||second<2)throw new Exception("Automatic brake not restored");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_KEB2_port_validation.json"),new JavaScriptSerializer().Serialize(new{passed=true,installed=false,host_connected=false,braked_tc=braked,released_tc=released,preserved_bvr_pilot=preserved,independent_tc=independent,second_released_tc=releasedAgain,reapplied_tc=reapplied,second_reapplied_tc=second,mass_balance_error=c.MaximumBalanceError,scope="Ideal actuatedKEB2 ports, two sections/four TC groups. Fixed initial100l automatic source fixture, not actualBVR pilot volume; test geometry; PM-fed actuation, native button wiring, coil delays and train brake not verified."}));
  Console.WriteLine("PASS KEB2 ports: auto="+braked+",release="+released+",KVT="+independent+",restore="+reapplied);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}

