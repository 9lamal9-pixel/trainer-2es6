using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class NativeBvrBtoProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static object host;static IntPtr loc,car;static Reservoir pm,tm,automatic,pilot,signal;static BtoSection bto;static PressureSelector selector;static IdealRelay kvt;
 static double initial,boundary,vent,maxError;static int count;static List<object> rows=new List<object>();
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,null);}
 static float Read(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static double ReadDouble(IntPtr p,int o){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,o),b,0,8);return BitConverter.ToDouble(b,0);}
 static void Write(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 static void Double(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}
 static double Mass(){return pm.Mass+tm.Mass+automatic.Mass+pilot.Mass+signal.Mass+bto.TotalMass;}
 static void Cycle(int km,int code){
  Marshal.WriteByte(loc,0x7d,(byte)km);Marshal.WriteByte(loc,0x7e,(byte)code);
  Write(loc,5,(float)pm.Gauge);Write(car,4,(float)automatic.Gauge);
  float beforePm=Read(loc,5),beforeAuto=Read(car,4);Call("PneumaticCycle");
  double pmDelta=(Read(loc,5)-beforePm)*Flow.Kgf*pm.Volume/(Flow.R*pm.Temperature);
  double autoDelta=(Read(car,4)-beforeAuto)*Flow.Kgf*automatic.Volume/(Flow.R*automatic.Temperature);
  pm.ApplyBoundaryMass(pmDelta);automatic.ApplyBoundaryMass(autoDelta);boundary+=pmDelta+autoDelta;
  for(int s=0;s<10;s++){
   vent+=kvt.StepSetpoint(pm,pilot,KvtProfile.IndependentSetpoint(code),.01,true);
   selector.Step(automatic,pilot,signal,.01);vent+=bto.Step(pm,tm,signal,.01);
  }
  // Feedback of the loaded pilot outlet to the native483A branch, not locomotive TC.
  Write(car,4,(float)automatic.Gauge);Write(loc,5,(float)pm.Gauge);
  maxError=Math.Max(maxError,Math.Abs(Mass()+vent-initial-boundary));if(maxError>1e-8)throw new Exception("BVR boundary mass balance");
  count++;if(count%100==0)rows.Add(new{seconds=count*.1,km=km,kvt=code,pm=pm.Gauge,ur=Read(loc,9),native_zr=Read(car,8),auto_pilot=automatic.Gauge,independent_pilot=pilot.Gauge,tc1=bto.Cylinder[0].Gauge,tc2=bto.Cylinder[1].Gauge});
 }
 static void Phase(int cycles,int km,int code){for(int n=0;n<cycles;n++)Cycle(km,code);}
 [STAThread]static int Main(){Application.EnableVisualStyles();host=Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  var h=(TrainerIntegrationHost)host;var handle=h.Handle;Call("StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host)).Stop();
  var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);loc=mem["TLocParam"];car=((IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",flags).GetValue(host))[0];
  Double(car,0xd7,20);Double(car,0xdf,1);for(int o=0x80;o<=0x87;o++)Marshal.WriteByte(loc,o,0);
  pm=new Reservoir(2000,8,293.15);tm=new Reservoir(100,5,293.15);automatic=new Reservoir(1,0,293.15);pilot=new Reservoir(20,0,293.15);signal=new Reservoir(1,0,293.15);
  bto=new BtoSection(8,1,1,12,6,6,6,6,.7,.001);selector=new PressureSelector(6,.7);kvt=new IdealRelay(6,6,.7,.001);initial=Mass();
  Phase(1000,2,2);int brake=0;while(Read(loc,9)>4.5&&brake++<100)Cycle(5,2);Phase(600,4,2);double autoTc=bto.Cylinder[0].Gauge;
  Phase(300,4,6);double combinedTc=bto.Cylinder[0].Gauge;
  Phase(300,4,2);double retainedTc=bto.Cylinder[0].Gauge;
  Phase(600,2,2);double releasedTc=bto.Cylinder[0].Gauge;
  if(autoTc<.5||combinedTc<3.8||retainedTc<.5||releasedTc>.05)throw new Exception("BVR/BTO combination failed: "+autoTc+" / "+combinedTc+" / "+retainedTc+" / "+releasedTc);
  var result=new{passed=true,installed=false,calibrated_to_RE=false,scope="One native483A BVR with20l ZR and1l test outlet feeding one BTO section with pressure feedback; finite native BVR boundary exchange; two-section implementation and electrical cycle absent",native_zr_volume=ReadDouble(car,0xd7),native_outlet_volume=ReadDouble(car,0xdf),automatic_tc=autoTc,combined_tc=combinedTc,tc_after_kvt_release=retainedTc,released_tc=releasedTc,final_pm=pm.Gauge,max_boundary_mass_error=maxError,rows=rows};
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_native_BVR_BTO_validation.json"),new JavaScriptSerializer().Serialize(result));
  Console.WriteLine("PASS native BVR->BTO: "+autoTc+" / "+combinedTc+" / "+retainedTc+" / "+releasedTc);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{((Form)host).Close();((Form)host).Dispose();}}
}
