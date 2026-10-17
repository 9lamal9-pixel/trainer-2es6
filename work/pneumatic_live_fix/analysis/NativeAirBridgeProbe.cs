using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class NativeAirBridgeProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static void Call(object h,string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(h,null);}
 static float Read(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static void Write(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 [STAThread]static int Main(string[] args){Application.EnableVisualStyles();var host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  int kvt=args.Length>0?int.Parse(args[0]):6;double target=KvtProfile.IndependentSetpoint(kvt);if(target<=0)throw new Exception("Use braking position3..6");
  var handle=host.Handle;Call(host,"StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host)).Stop();
  var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);var loc=mem["TLocParam"];
  Marshal.WriteByte(loc,0x7d,4);Marshal.WriteByte(loc,0x7e,(byte)kvt);for(int o=0x80;o<=0x87;o++)Marshal.WriteByte(loc,o,0);
  var pm=new Reservoir(2000,Read(loc,5),293.15);var tm=new Reservoir(100,5,293.15);var pilot=new Reservoir(20,0,293.15);var valve=new IdealRelay(6,6,.7,.001);
  var sections=new[]{new BtoSection(8,1,1,12,6,6,6,6,.7,.001),new BtoSection(8,1,1,12,6,6,6,6,.7,.001)};
  Func<double> total=()=>pm.Mass+tm.Mass+pilot.Mass+sections[0].TotalMass+sections[1].TotalMass;
  double initial=total(),boundary=0,vent=0,maxError=0,chargedTc=0,chargedPm=0,disconnectedTc=0,otherTc=0;var rows=new List<object>();
  for(int n=0;n<900;n++){
   if(n==300){chargedTc=sections[0].Cylinder[0].Gauge;chargedPm=pm.Gauge;sections[0].DisconnectBogie(1);}
   if(n==600){disconnectedTc=sections[0].Cylinder[0].Gauge;otherTc=sections[1].Cylinder[0].Gauge;Marshal.WriteByte(loc,0x7e,2);}
   // Native GR delta represents the external TM/compressor circuit; do not reapply it as TC supply.
   Write(loc,5,(float)pm.Gauge);float before=Read(loc,5);Call(host,"PneumaticCycle");float after=Read(loc,5);
   double delta=(after-before)*Flow.Kgf*pm.Volume/(Flow.R*pm.Temperature);pm.ApplyBoundaryMass(delta);boundary+=delta;
   for(int sub=0;sub<10;sub++){vent+=valve.StepSetpoint(pm,pilot,n<600?target:0,.01,true);foreach(var section in sections)vent+=section.Step(pm,tm,pilot,.01);}
   Write(loc,5,(float)pm.Gauge);double error=Math.Abs(total()+vent-initial-boundary);maxError=Math.Max(maxError,error);
   if(error>1e-9)throw new Exception("Bridge mass balance");
   if(n%10==0)rows.Add(new{seconds=(n+1)*.1,pm=pm.Gauge,native_tc=Read(loc,13),physical_tc=sections[0].Cylinder[0].Gauge,other_tc=sections[1].Cylinder[0].Gauge,pilot=pilot.Gauge,native_boundary_mass=boundary});
  }
  if(chargedPm>=7.99||Math.Abs(chargedTc-target)>.03||disconnectedTc>.001||Math.Abs(otherTc-target)>.03)throw new Exception("Bridge failed pressure/debit/selective disconnect check");
  foreach(var section in sections)foreach(var cylinder in section.Cylinder)if(cylinder.Gauge>.02)throw new Exception("Bridge failed TC release");
  if(pm.Gauge>chargedPm+.001)throw new Exception("Released air incorrectly returned to PM");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_native_air_bridge_"+kvt+".json"),new JavaScriptSerializer().Serialize(new{passed=true,kvt=kvt,target_kgf=target,installed=false,calibrated_to_RE=false,scope="Diagnostic GR boundary bridge to native DLL; RE stage setpoint, brake, selective disconnect, release; native TC still distinct; test geometry; no automatic BVR integration",charged_pm=chargedPm,charged_tc=chargedTc,disconnected_tc=disconnectedTc,other_tc=otherTc,final_pm=pm.Gauge,max_mass_error=maxError,external_native_mass=boundary,exhaust_mass=vent,rows=rows}));
  Console.WriteLine("PASS diagnostic native air bridge; PM="+pm.Gauge+" massError="+maxError);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{host.Close();host.Dispose();}}
}
