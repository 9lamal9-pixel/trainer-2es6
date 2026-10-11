using System;using System.IO;using System.Collections.Generic;using System.Web.Script.Serialization;using TrainerAir;
class KvtBenchProbe {
 static double Mass(Reservoir source,Reservoir outlet){return source.Mass+outlet.Mass;}
 static int Main(){try{
  const double dt=.001;var rows=new List<object>();
  foreach(double sourcePressure in new[]{7.0,8.0,10.0}){
   var source=new Reservoir(2000,sourcePressure,293.15);var outlet=new Reservoir(8,0,293.15);
   // Effective flow passages, not measured diameters of215 valve seats.
   var valve=new IdealRelay(6,6,.7,.001);double initial=Mass(source,outlet),vent=0,fill=-1,release=-1;
   for(int n=0;n<10000;n++){vent+=valve.StepSetpoint(source,outlet,KvtProfile.IndependentSetpoint(6),dt,true);if(outlet.Gauge>=3.5){fill=(n+1)*dt;break;}}
   if(fill<0||fill>3)throw new Exception("215 stand fill at PM"+sourcePressure);
   double sourceDrop=sourcePressure-source.Gauge;
   // Start the normative release measurement at exactly3.5 kgf/cm2.
   var releaseSource=new Reservoir(2000,source.Gauge,293.15);var releaseOut=new Reservoir(8,3.5,293.15);
   double releaseInitial=Mass(releaseSource,releaseOut),released=0;
   for(int n=0;n<15000;n++){released+=valve.StepSetpoint(releaseSource,releaseOut,0,dt,true);if(releaseOut.Gauge<=.5){release=(n+1)*dt;break;}}
   if(release<0||release>10)throw new Exception("215 stand release");
   if(Math.Abs(Mass(source,outlet)+vent-initial)>1e-9||Math.Abs(Mass(releaseSource,releaseOut)+released-releaseInitial)>1e-9)throw new Exception("215 balance");
   rows.Add(new{pm=sourcePressure,fill_seconds=fill,release_seconds=release,source_drop=sourceDrop});
  }
  var leaks=new List<object>();
  foreach(int code in new[]{3,4,5,6})foreach(double leak in new[]{.5,.6}){
   var source=new Reservoir(2000,8,293.15);var outlet=new Reservoir(8,0,293.15);var valve=new IdealRelay(6,6,.7,.001);
   double initial=Mass(source,outlet),vent=0,target=KvtProfile.IndependentSetpoint(code);
   for(int n=0;n<10000;n++)vent+=valve.StepSetpoint(source,outlet,target,dt,true);
   double charged=outlet.Gauge,minimum=charged,before=source.Mass;
   //180s is a diagnostic observation interval, not a duration inferred from table1.2.3.
   for(int n=0;n<180000;n++){vent+=valve.StepSetpoint(source,outlet,target,dt,true);vent+=Flow.Transfer(outlet,null,dt,leak,.7,true);minimum=Math.Min(minimum,outlet.Gauge);}
   if(charged-minimum>.15||before<=source.Mass||Math.Abs(Mass(source,outlet)+vent-initial)>1e-8)throw new Exception("215 leak compensation");
   leaks.Add(new{native_code=code,target=target,charged=charged,minimum=minimum,leak_mm=leak,source_mass_used=before-source.Mass});
  }
  foreach(double pressure in new[]{0.0,2.0}){
   var source=new Reservoir(2000,pressure,293.15);var outlet=new Reservoir(8,0,293.15);var valve=new IdealRelay(6,6,.7,.001);
   for(int n=0;n<10000;n++)valve.StepSetpoint(source,outlet,3.9,dt,true);
   if(outlet.Gauge>pressure+.0001)throw new Exception("KVT source limit");
  }
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_KVT_215_bench_validation.json"),new JavaScriptSerializer().Serialize(new{passed=true,installed=false,full_norm_acceptance=false,revision137_proven=false,source="215.000-1RE table1.2.3 page5",stand_volume_litres=8,fill_limit_seconds=3,release_limit_seconds=10,leak_limit_drop_kgf=.15,assumed_effective_inlet_mm=6,assumed_effective_exhaust_mm=6,assumed_cd=.7,assumed_source_litres=2000,leak_observation_seconds=180,timing=rows,leaks=leaks,limitations="Ideal regulating valve; mechanical diaphragm/friction/hysteresis absent.8l is stand volume, not actual locomotive impulse-line volume. Exact215 vs215-1 applicability to137 not established."}));
  Console.WriteLine("PASS215 bench:3 supply pressures,8 leak/position cases,2 source limits; incomplete norm acceptance");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
