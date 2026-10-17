using System;using System.IO;using TrainerAir;using System.Web.Script.Serialization;
class RelayBenchProbe {
 static int Main(string[] args){try{
  double leakMm=args.Length>0?double.Parse(args[0],System.Globalization.CultureInfo.InvariantCulture):1;
  // Exact stand volumes55/20l; PM pressure8kgf, pilot/plumbing1l and6mm passages are explicit test assumptions.
  var upstream=new Reservoir(2000,8,293.15);var supply=new Reservoir(55,8,293.15);
  var downstream=new Reservoir(1,0,293.15);var pilot=new Reservoir(1,0,293.15);var output=new Reservoir(20,0,293.15);
  var reducer=new IdealRelay(6,6,.7,.001);var relay=new IdealRelay(6,6,.7,.001);
  Func<double> mass=()=>upstream.Mass+supply.Mass+downstream.Mass+pilot.Mass+output.Mass;
  double initial=mass(),vent=0,fill=-1,release=-1,step=.001;int n=0;
  // Cock2 stays open. Fully open cock5 feeds both relay inlet and reducer inlet.
  for(;n<20000;n++){
   Flow.Transfer(upstream,supply,step,6,.7,false);Flow.Transfer(supply,downstream,step,6,.7,false);
   vent+=reducer.StepSetpoint(downstream,pilot,5,step,true);vent+=relay.Step(downstream,output,pilot,step,true);
   if(Math.Abs(mass()+vent-initial)>1e-9)throw new Exception("Bench fill balance");
   if(output.Gauge>=3.5){fill=(n+1)*step;break;}
  }
  double initialRelease=output.Gauge;
  // Cock5 closed: upstream isolated, downstream vent open. Reducer pilot can vent back via a released control path.
  // This ideal pilot discharge models the boundary action, not verified reducer211 internal mechanics.
  for(n=0;n<20000;n++){
   Flow.Transfer(upstream,supply,step,6,.7,false);
   vent+=Flow.Transfer(downstream,null,step,6,.7,true);
   vent+=reducer.StepSetpoint(downstream,pilot,0,step,false);
   vent+=relay.Step(downstream,output,pilot,step,false);
   if(Math.Abs(mass()+vent-initial)>1e-9)throw new Exception("Bench release balance");
   if(output.Gauge<=.4){release=(n+1)*step;break;}
  }
  // Reopen cock5 and establish charge before the separate leak test.
  for(n=0;n<30000;n++){
   Flow.Transfer(upstream,supply,step,6,.7,false);Flow.Transfer(supply,downstream,step,6,.7,false);
   vent+=reducer.StepSetpoint(downstream,pilot,5,step,true);vent+=relay.Step(downstream,output,pilot,step,true);
  }
  double charge=output.Gauge,min=charge,max=charge,sourceBefore=upstream.Mass+supply.Mass;
  for(n=0;n<180000;n++){
   Flow.Transfer(upstream,supply,step,6,.7,false);Flow.Transfer(supply,downstream,step,6,.7,false);
   vent+=reducer.StepSetpoint(downstream,pilot,5,step,true);vent+=relay.Step(downstream,output,pilot,step,true);
   vent+=Flow.Transfer(output,null,step,leakMm,.7,true);
   min=Math.Min(min,output.Gauge);max=Math.Max(max,output.Gauge);
   if(Math.Abs(mass()+vent-initial)>1e-9)throw new Exception("Bench leak balance");
  }
  double deviation=Math.Max(Math.Abs(min-charge),Math.Abs(max-charge));
  var result=new{executed=true,installed=false,calibrated_to_RE=false,full_norm_acceptance=false,
   scope="042 stand topology numerical timing and180s leak probe; pilot/plumbing/PM/valve assumptions explicit; sealing tests absent",
   fill_seconds=fill,fill_limit_seconds=4,fill_within_limit=fill>=0&&fill<=4,
   release_seconds=release,release_limit_seconds=10,release_within_limit=release>=0&&release<=10,
   initial_release_kgf=initialRelease,final_output_kgf=output.Gauge,mass_error=Math.Abs(mass()+vent-initial),
   leak_mm=leakMm,leak_duration_seconds=180,charge_kgf=charge,leak_min_kgf=min,leak_max_kgf=max,leak_deviation_kgf=deviation,
   leak_within_tolerance=deviation<=.15,leak_source_mass_consumed=sourceBefore-upstream.Mass-supply.Mass,
   assumed_supply_kgf=8,assumed_pilot_litres=1,assumed_downstream_litres=1,assumed_passage_mm=6,assumed_cd=.7};
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_relay_bench_"+leakMm.ToString(System.Globalization.CultureInfo.InvariantCulture)+".json"),new JavaScriptSerializer().Serialize(result));
  Console.WriteLine("Bench timing: fill="+fill+"s release="+release+"s leak="+leakMm+"mm deviation="+deviation+"kgf; incomplete norm acceptance");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}}
}
