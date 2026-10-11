using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using TrainerAir;

// Diagnostic full-length train. Only initial conditions and documented stand/native
// control inputs are set; pressure trajectories and compressor outputs remain native.
class TrainPneumaticProbe {
 [DllImport("kernel32.dll")] static extern IntPtr GetModuleHandle(string name);
 [DllImport("msvcrt.dll",CallingConvention=CallingConvention.Cdecl,EntryPoint="_statusfp")] static extern uint FloatingStatus();
 [DllImport("msvcrt.dll",CallingConvention=CallingConvention.Cdecl,EntryPoint="_controlfp")] static extern uint FloatingControl(uint newValue,uint mask);
 static readonly BindingFlags Flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static readonly int[] Observed={1,38,75};
 static readonly string[] ObservedNames={"head","middle","tail"};
 const double HeadSetpointTolerance=.15,ZrChargeLagTolerance=.15,RelativeHeadTmTolerance=.3;
 static readonly List<object> Rows=new List<object>();
 static readonly List<object> Arrivals=new List<object>();
 static readonly List<Arrival> ActiveArrivals=new List<Arrival>();
 static readonly List<string> Failures=new List<string>();
 static readonly List<object> WagonConfiguration=new List<object>();
 static readonly Stopwatch Clock=new Stopwatch();
 static TrainerIntegrationHost host;
 static Dictionary<string,IntPtr> mem;
 static IntPtr loc,trainData,form,schema,mainControl,bvReturn;
 static IntPtr[] cars;
 static TwoSectionAirChain circuit;
 static string phase="startup";
 static int cycles,phaseCycles,pressureChecks,layoutChecks;
 static double allTimeMinimum,allTimeMaximum;static bool hasPressureObservation;
 static double chargingSetpointActual;
 static string floatingStatusHex="not_read",floatingControlHex="not_read";
 static bool nativeStarted,coldInitialized,layoutVerified;
 static bool coldGeometryPreserved;static int coldGeometryChecks;
 static object startupNativeAir;
 static Dictionary<string,object> serviceHold;

 static void Progress(string checkpoint){
  // This deliberately contains only scalars and a string array, never the
  // snapshot/observation graph. It survives fatal native exceptions that .NET
  // cannot catch (including StackOverflowException).
  Console.WriteLine("PROGRESS "+phase+" "+checkpoint+" cycles="+cycles+" phase_cycles="+phaseCycles);Console.Out.Flush();
  var state=new{passed=false,in_progress=true,phase=phase,checkpoint=checkpoint,total_cycles=cycles,phase_cycles=phaseCycles,wall_seconds=Clock.Elapsed.TotalSeconds,
   native_started=nativeStarted,safe_cold_initialized=coldInitialized,layout_verified=layoutVerified,pressure_checks=pressureChecks,
   charging_setpoint_actual=Finite(chargingSetpointActual)?(double?)chargingSetpointActual:null,floating_status_hex=floatingStatusHex,floating_control_hex=floatingControlHex,failure_reasons=Failures.ToArray()};
  File.WriteAllText(TrainerSession.ResultPath("Trainer_train_pneumatic_progress.json"),new JavaScriptSerializer().Serialize(state));
 }

 static void Require(bool condition,string message){if(!condition)throw new Exception(message);}
 static object Field(string name){return typeof(TrainerIntegrationHost).GetField(name,Flags).GetValue(host);}
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,Flags).Invoke(host,null);}
 static float Single(IntPtr p,int offset){var b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
 static double Double(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
 static void SetSingle(IntPtr p,int offset,float value){var b=BitConverter.GetBytes(value);Marshal.Copy(b,0,IntPtr.Add(p,offset),4);}
 static void SetDouble(IntPtr p,int offset,double value){var b=BitConverter.GetBytes(value);Marshal.Copy(b,0,IntPtr.Add(p,offset),8);}
 static bool Finite(double value){return !double.IsNaN(value)&&!double.IsInfinity(value);}
 static byte[] Bytes(IntPtr p,int offset,int count){var values=new byte[count];Marshal.Copy(IntPtr.Add(p,offset),values,0,count);return values;}
 static IntPtr Control(int offset){var control=Marshal.ReadIntPtr(form,offset);Require(control!=IntPtr.Zero,"Native input absent at form+"+offset.ToString("x"));return control;}
 static void Input(IntPtr control,byte state){Marshal.WriteByte(control,0x1d9,state);}
 static void Km(int value){typeof(TrainerIntegrationHost).GetMethod("TestBrake",Flags).Invoke(host,new object[]{value});}
 static void Begin(string name){phase=name;phaseCycles=0;ActiveArrivals.Clear();Progress("phase_ready_before_snapshot");Snapshot("begin");Progress("phase_ready");}

 static void CheckLayout(){
  Require(cars!=null&&cars.Length==76,"Expected 76 motion records (locomotive and 75 wagons)");
  Require(Marshal.ReadInt32(trainData,0)==77,"Expected 77 native pneumatic nodes");
  var second=(IntPtr)Field("secondBvr");var array=Marshal.ReadIntPtr(trainData,0x42);
  Require(array!=IntPtr.Zero&&second!=IntPtr.Zero,"Native pneumatic array absent");
  var unique=new HashSet<IntPtr>();
  for(int node=0;node<77;node++){
   IntPtr expected=node==0?cars[0]:node==1?second:cars[node-1];
   Require(expected!=IntPtr.Zero&&unique.Add(expected),"Duplicate/empty pneumatic record at node "+node);
   Require(Marshal.ReadIntPtr(array,node*8+4)==expected,"Native pneumatic node mapping differs at "+node);
   Require(Marshal.ReadIntPtr(array,node*8)==(node==0?loc:IntPtr.Zero),"Unexpected native locomotive pointer at node "+node);
  }
  for(int wagon=1;wagon<=75;wagon++){
   if(WagonConfiguration.Count<75)WagonConfiguration.Add(new{wagon=wagon,native_air_node=wagon+1,gross_t=Marshal.ReadInt32(cars[wagon],0x25),load_mode=Marshal.ReadByte(cars[wagon],0x18),auto_regulator=Marshal.ReadByte(cars[wagon],0x1a),configured_motion_ftk=1,brake_pad_material="cast_iron"});
   Require(Marshal.ReadInt32(cars[wagon],0x25)==80,"Unexpected wagon gross mass at "+wagon);
   Require(Marshal.ReadByte(cars[wagon],0x18)==1&&Marshal.ReadByte(cars[wagon],0x1a)==1,"Expected loaded R1 and auto regulator ON at wagon "+wagon);
   Require(Marshal.ReadByte(cars[wagon],0x1b)==1,"Wagon VR disabled at "+wagon);
   foreach(int offset in new[]{0xcb,0xd7,0xdf})Require(Finite(Double(cars[wagon],offset))&&Double(cars[wagon],offset)>0,"Invalid native reservoir volume at wagon "+wagon);
  }
  layoutChecks++;layoutVerified=true;
 }

 static void CheckPressures(){
  for(int wagon=1;wagon<=75;wagon++)foreach(int offset in new[]{0,4,8}){
   double value=Single(cars[wagon],offset);
   Require(Finite(value)&&value>=-1e-5,"Invalid pressure in "+phase+", cycle "+phaseCycles+", wagon "+wagon+", field "+offset+", value "+value);
   if(!hasPressureObservation){allTimeMinimum=value;allTimeMaximum=value;hasPressureObservation=true;}else{allTimeMinimum=Math.Min(allTimeMinimum,value);allTimeMaximum=Math.Max(allTimeMaximum,value);}pressureChecks++;
  }
  foreach(int offset in new[]{5,9,13})Require(Finite(Single(loc,offset))&&Single(loc,offset)>=-1e-5,"Invalid locomotive pressure at "+offset);
 }

 static void Step(){
  Require(Clock.Elapsed.TotalSeconds<285,"Diagnostic wall-clock budget exceeded before "+phase+" cycle "+phaseCycles);
  bool first=phaseCycles==0;if(first)Progress("first_step_before_electrical");Call("ElectricalCycle");if(first)Progress("first_step_after_electrical");Call("ApplyTestInputs");
  // Stand KVT II is an input, kept released while checking automatic wagon brakes.
  Marshal.WriteByte(loc,0x7e,2);
  if(first)Progress("first_step_before_pneumatic");Call("PneumaticCycle");cycles++;phaseCycles++;if(first)Progress("first_step_after_pneumatic");CheckPressures();if(first)Progress("first_step_verified");
  if(phaseCycles%2000==0){Progress("bounded_progress_before_snapshot");Snapshot("progress");Progress("bounded_progress");}
 }

 static double Min(int offset){bool detail=cycles==0;if(detail)Progress("min_"+offset+"_before_first_read");double value=Single(cars[1],offset);if(detail)Progress("min_"+offset+"_after_first_read");for(int wagon=2;wagon<=75;wagon++){double sample=Single(cars[wagon],offset);if(detail&&wagon==2)Progress("min_"+offset+"_before_first_compare");value=Math.Min(value,sample);if(detail&&wagon==2)Progress("min_"+offset+"_after_first_compare");}return value;}
 static double Max(int offset){bool detail=cycles==0;if(detail)Progress("max_"+offset+"_before_first_read");double value=Single(cars[1],offset);if(detail)Progress("max_"+offset+"_after_first_read");for(int wagon=2;wagon<=75;wagon++){double sample=Single(cars[wagon],offset);if(detail&&wagon==2)Progress("max_"+offset+"_before_first_compare");value=Math.Max(value,sample);if(detail&&wagon==2)Progress("max_"+offset+"_after_first_compare");}return value;}
 static bool ChargeReady(bool requirePm){
  // Head approaches its actual regulator setpoint. The other wagons must
  // approach the observed head, without an absolute tail setpoint. Rules808
  // AppendixK allows a 0.3 head/tail difference for up to300 wagon axles.
  // Here75 wagons are assumed four-axle (300 axles); applying that bound to
  // every wagon is a diagnostic check. Axle count is not read from native RTTI.
  // Head/setpoint and ZR/localTM tolerances remain separate diagnostic limits.
  double head=Single(cars[1],0);if(head<chargingSetpointActual-HeadSetpointTolerance||head>chargingSetpointActual+HeadSetpointTolerance||Single(loc,9)<chargingSetpointActual-HeadSetpointTolerance||Max(4)>.1||(requirePm&&Single(loc,5)<7))return false;
  for(int wagon=1;wagon<=75;wagon++)if(Math.Abs(Single(cars[wagon],0)-head)>RelativeHeadTmTolerance||Single(cars[wagon],8)<Single(cars[wagon],0)-ZrChargeLagTolerance)return false;
  return true;
 }
 static double Inventory(){
  // Observable pressure*volume inventory only: native VR chamber volumes and
  // individual supply/exhaust mass rates are not yet identified. This is not a
  // complete mass balance and is never used as a conservation assertion.
  double value=0;for(int wagon=1;wagon<=75;wagon++)value+=Single(cars[wagon],0)*Double(cars[wagon],0xcb)+Single(cars[wagon],4)*Double(cars[wagon],0xdf)+Single(cars[wagon],8)*Double(cars[wagon],0xd7);return value;
 }
 static object AirNode(IntPtr car,int node,int wagon,string position){
  // Offsets follow the packed TVagonParam/TUpdateParamVagon RTTI. Read only:
  // these inlet/leak coefficients distinguish a fixture error from propagation.
  var row=new Dictionary<string,object>();row["position"]=position;row["wagon"]=wagon;row["native_air_node"]=node;
  row["tm"]=Single(car,0);row["tc"]=Single(car,4);row["zr"]=Single(car,8);
  row["vr_ks"]=Double(car,0x61);row["vr_zk"]=Double(car,0x69);row["vr_rk"]=Double(car,0x71);row["vr_dr"]=Double(car,0x79);
  row["load_mode"]=Marshal.ReadByte(car,0x18);row["auto_regulator"]=Marshal.ReadByte(car,0x1a);row["vr_enabled"]=Marshal.ReadByte(car,0x1b);row["release_valve"]=Marshal.ReadByte(car,0x1c);
  row["cock_front_rear"]=Bytes(car,0x20,2);row["sleeve_front_rear"]=Bytes(car,0x22,2);
  row["v_tm"]=Double(car,0xcb);row["d_tm"]=Marshal.ReadInt32(car,0xd3);row["v_zr"]=Double(car,0xd7);row["v_tc"]=Double(car,0xdf);
  row["up_d"]=Double(car,0x29);row["up_pvh"]=Double(car,0x31);row["up_pvyh"]=Double(car,0x39);row["up_dvh"]=Double(car,0x41);row["up_dvyh"]=Double(car,0x49);row["up_dut"]=Double(car,0x51);row["up_y5"]=Double(car,0x59);row["up_pog"]=Double(car,0x91);
  return row;
 }
 static object Observations(){var rows=new List<object>();for(int index=0;index<Observed.Length;index++)rows.Add(AirNode(cars[Observed[index]],Observed[index]+1,Observed[index],ObservedNames[index]));return rows;}
 static object NativeAirNodes(){return new[]{AirNode(cars[0],0,0,"first_bvr"),AirNode((IntPtr)Field("secondBvr"),1,0,"second_bvr"),AirNode(cars[1],2,1,"head"),AirNode(cars[38],39,38,"middle"),AirNode(cars[75],76,75,"tail")};}
 static object NativeInlet(){
  var row=new Dictionary<string,object>();row["km"]=Marshal.ReadByte(loc,0x7d);row["kvt"]=Marshal.ReadByte(loc,0x7e);row["vcu_or_block367"]=Marshal.ReadByte(loc,0x8e);row["vcu_position"]=Marshal.ReadByte(loc,0x8f);row["blocked367"]=Marshal.ReadByte(loc,0x90);row["combined_cock"]=Marshal.ReadByte(loc,0x91);
  row["dv_h"]=Single(loc,0x70);row["pvh"]=Single(loc,0x74);row["gr_dut"]=Double(loc,0x68);row["pzar"]=Double(loc,0x13);
  row["epk_key"]=Marshal.ReadByte(loc,0x99);row["epk_coil_voltage"]=Marshal.ReadByte(loc,0x9a);row["epk_trip"]=Marshal.ReadByte(loc,0x9b);row["epk_whistle"]=Marshal.ReadByte(loc,0x9c);row["epk_kon_trip"]=Marshal.ReadByte(loc,0x9d);row["epk_rb_light"]=Marshal.ReadByte(loc,0x9e);row["epk_chamber"]=Single(loc,0x9f);row["epk_kon_counter"]=Marshal.ReadInt32(loc,0xa3);
  row["ept_command_a8"]=Marshal.ReadByte(loc,0xa8);row["control_feed_open"]=(bool[])((bool[])Field("controlFeedOpen")).Clone();row["control_sf3"]=(bool[])((bool[])Field("controlSf3")).Clone();return row;
 }
 static object TrainInputs(){return new{native_count=Marshal.ReadInt32(trainData,0),plot=Marshal.ReadInt32(trainData,0x37),check_tor=Marshal.ReadByte(trainData,0x3b),tn=Double(trainData,4),dt=Double(trainData,0xc),dt1=Double(trainData,0x14),raz=Marshal.ReadInt32(trainData,0x1c),native_fault_bytes=Bytes(trainData,0x20,12)};}
 static object TerminalWagonPressures(){if(cars==null||cars.Length!=76)return null;var tm=new float[75];var tc=new float[75];var zr=new float[75];for(int wagon=1;wagon<=75;wagon++){tm[wagon-1]=Single(cars[wagon],0);tc[wagon-1]=Single(cars[wagon],4);zr[wagon-1]=Single(cars[wagon],8);}return new{wagon_order="1..75",tm=tm,tc=tc,zr=zr};}
 static object BoundaryReservoir(string name,Reservoir reservoir,float nativeGauge,string beforeField){
  var before=Field(beforeField);double stored=Convert.ToDouble(before),actual=reservoir.Gauge;
  // Preserve the arithmetic used by the bridge: float-float originally
  // rounded its subtraction before promotion to ApplyNativePressureChanges.
  double delta=before is float?(double)(nativeGauge-(float)before):(double)nativeGauge-stored;
  double deltaUsingActual=(double)nativeGauge-actual;
  double atmosphereMass=Flow.Atmosphere*reservoir.Volume/(Flow.R*reservoir.Temperature);
  double massDelta=delta*Flow.Kgf*reservoir.Volume/(Flow.R*reservoir.Temperature);
  double massDeltaUsingActual=deltaUsingActual*Flow.Kgf*reservoir.Volume/(Flow.R*reservoir.Temperature);
  var row=new Dictionary<string,object>();row["reservoir"]=name;row["stored_before_field"]=beforeField;row["stored_before_type"]=before.GetType().Name;row["stored_before_gauge"]=stored;row["current_managed_gauge"]=actual;row["native_memory_gauge"]=nativeGauge;row["stored_before_minus_current_managed"]=stored-actual;
  row["native_minus_stored_before_delta_as_bridge"]=delta;row["native_minus_current_managed_delta_double"]=deltaUsingActual;row["managed_plus_bridge_delta_target_gauge"]=actual+delta;row["native_minus_computed_target_gauge"]=nativeGauge-(actual+delta);
  row["managed_mass_kg"]=reservoir.Mass;row["atmospheric_mass_kg"]=atmosphereMass;row["bridge_delta_mass_kg"]=massDelta;row["attempted_mass_above_atmosphere_kg"]=reservoir.Mass+massDelta-atmosphereMass;row["actual_double_basis_mass_above_atmosphere_kg"]=reservoir.Mass+massDeltaUsingActual-atmosphereMass;row["boundary_deficit_threshold_kg"]=-1e-12;return row;
 }
 static object NativeBoundaryReadout(bool failedBeforeApplication){return new{values_are_failed_preapplication_state=failedBeforeApplication,scope="On Native boundary air deficit, ApplyNativePressureChanges validates all three reservoirs before mutation, so current managed gauges equal preapplication state. On successful snapshots native gauges may already have been written back and StepControls applied; those readouts are not attempted end targets.",reservoirs=new[]{BoundaryReservoir("pm",circuit.Pm,Single(loc,5),"beforeAirPm"),BoundaryReservoir("automatic_first",circuit.Automatic[0],Single(cars[0],4),"beforeAirAuto1"),BoundaryReservoir("automatic_second",circuit.Automatic[1],Single((IntPtr)Field("secondBvr"),4),"beforeAirAuto2")}};}
 static void Snapshot(string eventName){
  if(cars==null||schema==IntPtr.Zero||circuit==null)return;
  // Compute groups sequentially instead of keeping all anonymous-constructor
  // arguments live in one x86 JIT frame. No native function or serializer is
  // invoked here; each checkpoint precedes a small, identifiable read group.
  Progress("snapshot_before_pressure_reads");var row=new Dictionary<string,object>();
  Progress("snapshot_before_scalar_dictionary");row["phase"]=phase;row["event_name"]=eventName;row["total_cycles"]=cycles;row["phase_cycles"]=phaseCycles;
  Progress("snapshot_before_nominal_seconds");row["nominal_seconds"]=cycles*.1;
  Progress("snapshot_before_fp_state_read");floatingStatusHex=FloatingStatus().ToString("X8");floatingControlHex=FloatingControl(0,0).ToString("X8");Progress("snapshot_after_fp_state_read");
  Progress("snapshot_before_pm");row["pm"]=Single(loc,5);
  Progress("snapshot_before_ur");row["ur"]=Single(loc,9);
  Progress("snapshot_before_tm_min");row["wagon_tm_min"]=Min(0);
  Progress("snapshot_before_tm_max");row["wagon_tm_max"]=Max(0);
  Progress("snapshot_before_tc_min");row["wagon_tc_min"]=Min(4);
  Progress("snapshot_before_tc_max");row["wagon_tc_max"]=Max(4);
  Progress("snapshot_before_zr_min");row["wagon_zr_min"]=Min(8);
  Progress("snapshot_before_zr_max");row["wagon_zr_max"]=Max(8);
  Progress("snapshot_before_observations");row["observed"]=Observations();
  Progress("snapshot_before_native_inlet");row["native_inlet"]=NativeInlet();row["native_train_inputs"]=TrainInputs();row["locomotive_air_nodes"]=new[]{AirNode(cars[0],0,0,"first_bvr"),AirNode((IntPtr)Field("secondBvr"),1,0,"second_bvr")};
  Progress("snapshot_before_inventory");row["observable_pressure_volume_inventory_l_kgf_cm2"]=Inventory();
  Progress("snapshot_before_control_pressure");row["rs6"]=circuit.ControlAir[0].Rs6.Gauge;row["rs7"]=circuit.ControlAir[0].Rs7.Gauge;
  Progress("snapshot_before_electrical_reads");row["main_command"]=Marshal.ReadByte(mem["TPult"],0x28);row["compressor_flags"]=Bytes(loc,0x80,8);row["komp_error"]=Marshal.ReadByte(schema,0xc0);row["komp_count"]=Marshal.ReadInt32(schema,0xc2);
  row["pantograph_feedback"]=Bytes(mem["TPult"],0x97,2);row["qf1"]=Bytes(schema,0x62,2);row["network_flag"]=Marshal.ReadByte(schema,0x123);row["line_voltage"]=Marshal.ReadInt32(schema,0x125);row["psn"]=new[]{Marshal.ReadInt32(schema,0x242),Marshal.ReadInt32(schema,0x246)};
  Progress("snapshot_before_managed_mass");row["managed_mass_kg"]=circuit.TotalMass;row["managed_native_boundary_mass_kg"]=circuit.ExternalMass;row["managed_exhaust_mass_kg"]=circuit.ExhaustedMass;row["managed_boundary_maximum_balance_error_kg"]=circuit.MaximumBalanceError;
  Progress("snapshot_before_native_boundary");bool boundaryFailure=eventName=="failure"&&Failures.Exists(delegate(string message){return message.IndexOf("Native boundary air deficit",StringComparison.Ordinal)>=0;});row["native_managed_boundary"]=NativeBoundaryReadout(boundaryFailure);
  Rows.Add(row);Progress("snapshot_complete");
 }

 sealed class Arrival {
  readonly string name;readonly int offset,direction;readonly double[] baseline=new double[3];readonly int[] at={-1,-1,-1};
  bool saved;
  public Arrival(string label,int field,int sign){name=label;offset=field;direction=sign;for(int i=0;i<3;i++)baseline[i]=Single(cars[Observed[i]],field);ActiveArrivals.Add(this);}
  public void Observe(){for(int i=0;i<3;i++)if(at[i]<0&&direction*(Single(cars[Observed[i]],offset)-baseline[i])>.1)at[i]=phaseCycles;}
  public void Finish(bool requireEveryPosition){
   if(!saved){Arrivals.Add(new{phase=phase,event_name=name,field_offset=offset,direction=direction,local_baseline=baseline,arrival_cycle=at,arrival_seconds=new[]{at[0]<0?-1:at[0]*.1,at[1]<0?-1:at[1]*.1,at[2]<0?-1:at[2]*.1},deviation_threshold_kgf_cm2=.1});saved=true;}
   if(requireEveryPosition)Require(at[0]>=0&&at[1]>=0&&at[2]>=0,"Missing "+name+" arrival at head/middle/tail");
   if(at[0]>=0&&at[1]>=0)Require(at[1]>=at[0],name+" reached middle before head");
   if(at[1]>=0&&at[2]>=0)Require(at[2]>=at[1],name+" reached tail before middle");
   if(at[0]>=0&&at[2]>=0)Require(at[2]>=at[0],name+" reached tail before head");
  }
 }

 static void ColdFixture(){
  var all=new List<IntPtr>(cars);all.Add((IntPtr)Field("secondBvr"));
  var geometry=new Dictionary<IntPtr,byte[]>();var vrObjects=new Dictionary<IntPtr,IntPtr>();
  foreach(var car in all){var saved=new List<byte>();saved.AddRange(Bytes(car,0x29,8));saved.AddRange(Bytes(car,0x41,24));saved.AddRange(Bytes(car,0xcb,28));saved.AddRange(Bytes(car,0x20,4));geometry.Add(car,saved.ToArray());vrObjects.Add(car,Marshal.ReadIntPtr(car,0xe8));}
  // InitSostavClass calibrates Dut using Pzar as its stationary pressure
  // boundary (RVA17772e -> 177238 -> 176764); Pzar=0 selects an excessive
  // leak diameter. Preserve the already initialized normal geometry and VR
  // objects. Establish initial pressure state only, without reinitialization.
  Progress("cold_fixture_before_pressure_initial_conditions");
  foreach(var car in all){foreach(int offset in new[]{0,4,8})SetSingle(car,offset,0);foreach(int offset in new[]{0x31,0x39,0x59,0x61,0x69,0x71,0x79,0x91})SetDouble(car,offset,0);}
  // Pvh/Pvyh are neighbouring pressure values refreshed by RVA178218 each
  // step; Y5 is initialized from Ptm, and Pog is the brake pilot pressure.
  // Connection areas D/Dvh/Dvyh and the calibrated leak area Dut stay intact.
  foreach(int offset in new[]{5,9,13,0x70,0x74,0x9f})SetSingle(loc,offset,0);SetDouble(loc,0xec,0);
  foreach(var car in all){var actual=new List<byte>();actual.AddRange(Bytes(car,0x29,8));actual.AddRange(Bytes(car,0x41,24));actual.AddRange(Bytes(car,0xcb,28));actual.AddRange(Bytes(car,0x20,4));var before=geometry[car];var after=actual.ToArray();Require(before.Length==after.Length,"Cold geometry guard length");for(int i=0;i<before.Length;i++)Require(before[i]==after[i],"Cold initial conditions changed native air geometry byte "+i);Require(vrObjects[car]==Marshal.ReadIntPtr(car,0xe8),"Cold initial conditions replaced native VR object");coldGeometryChecks++;}
  coldGeometryPreserved=true;Progress("cold_fixture_after_geometry_invariant");
  circuit=new TwoSectionAirChain(new TwoSectionAirParameters(2000,100,1,20,1,1,1,12,6,.7,.001),0,0,0);circuit.InitializeControlAir(0,0);
  typeof(TrainerIntegrationHost).GetField("airCircuit",Flags).SetValue(host,circuit);
  var feed=(bool[])Field("controlFeedOpen");feed[0]=false;feed[1]=false;
  var sf3=(bool[])Field("controlSf3");sf3[0]=false;sf3[1]=false;
  ((CheckBox)Field("auxiliaryCommandControl")).CheckState=CheckState.Unchecked;
  Require(Double(loc,0x13)==chargingSetpointActual,"Cold initial conditions changed the actual charging setpoint");coldInitialized=true;CheckLayout();CheckPressures();
 }

 static void PowerAndCharge(){
  Km(2);Begin("cold_no_source");
  for(int i=0;i<1000;i++){Step();Require(Max(0)<1e-5&&Max(4)<1e-5&&Max(8)<1e-5&&Single(loc,5)<1e-5&&Single(loc,9)<1e-5,"Cold train generated pressure without a source");}Snapshot("end");
  Begin("auxiliary_control_charge");var sf3=(bool[])Field("controlSf3");sf3[0]=true;sf3[1]=true;((CheckBox)Field("auxiliaryCommandControl")).CheckState=CheckState.Checked;
  // Wait for a pressure sufficient for the native pantograph rising hysteresis,
  // with extra margin to the observed 3.7 gate; this is not an actuator bench.
  bool controlReady=false;for(int i=0;i<5000;i++){Step();Require(Max(0)<1e-5&&Max(4)<1e-5&&Max(8)<1e-5&&Single(loc,5)<1e-5&&Single(loc,9)<1e-5,"Auxiliary source charged the train/main network");if(circuit.ControlAir[0].Rs6.Gauge>=4&&circuit.ControlAir[0].Rs7.Gauge>=3.5){controlReady=true;break;}}
  Snapshot("end");Require(controlReady,"Auxiliary air did not prepare section 1 within 5000 cycles");
  ((CheckBox)Field("auxiliaryCommandControl")).CheckState=CheckState.Unchecked;var feed=(bool[])Field("controlFeedOpen");feed[0]=true;feed[1]=true;
  Begin("pantograph");Input(Control(0x3a0),1);Input(Control(0x3a4),0);Input(Control(0x3dc),0);
  bool pantographReady=false;for(int i=0;i<500;i++){Step();if(Marshal.ReadByte(mem["TPult"],0x97)==1&&Marshal.ReadInt32(schema,0x125)==3100){pantographReady=true;break;}}
  Snapshot("end");Require(pantographReady,"Section 1 pantograph/line feedback absent within 500 cycles");
  Begin("bv_psn");Input(Control(0x3dc),1);bool psnReady=false;
  for(int i=0;i<5000;i++){Input(bvReturn,(byte)(i<10?1:0));Step();if(i>=10&&Marshal.ReadInt32(schema,0x242)==1){psnReady=true;break;}}
  Input(bvReturn,0);Snapshot("end");Require(psnReady,"Native BV/PSN1 not ready within 5000 cycles");Require(Marshal.ReadByte(schema,0xc0)==0,"Compressor error latched despite command OFF before PSN");
  Begin("cold_full_train_charge");var tm=new Arrival("charging_tm_rise",0,1);var zr=new Arrival("charging_zr_rise",8,1);Input(mainControl,1);
  bool charged=false;int stable=0;for(int i=0;i<18000;i++){Step();tm.Observe();zr.Observe();
   // All wagons must be near the actual configured setpoint, with released cylinders. A cold
   // ZR charge defect must fail visibly rather than inherit a warm initial ZR.
   bool ready=ChargeReady(true);
   stable=ready?stable+1:0;if(stable>=100){charged=true;break;}}
  Snapshot("end");tm.Finish(false);zr.Finish(false);Require(charged,"Full cold train did not establish headTM within actualPzar+/-0.15, UR>=actualPzar-0.15, every wagonTM within observedheadTM+/-0.3 (assumed75 four-axle wagons=300 axles), ownZR>=ownTM-0.15, TC<=0.1 and PM>=7 within18000 cycles");tm.Finish(true);zr.Finish(true);
  // Ordering is required only after saving every arrival and the final charge data.
  Require(Marshal.ReadByte(schema,0xc0)==0,"Native compressor error during full train charge");
 }

 static void BrakeAndRelease(bool emergency){
  Begin(emergency?"emergency_application":"service_application");var tm=new Arrival("tm_fall",0,-1);var tc=new Arrival("tc_rise",4,1);
  Km(emergency?6:5);bool reduced=false;
  // UR4.5 is this diagnostic's fixed trigger. From actualPzar5.4 the drop is
  // about0.9; it does not certify the normative0.6..0.7 full-service bench step.
  for(int i=0;i<1500;i++){Step();tm.Observe();tc.Observe();if(Single(loc,9)<=(emergency?.2:4.5)){reduced=true;break;}}
  if(!emergency)Km(4);
  bool applied=false;int stable=0;for(int i=0;i<3000;i++){Step();tm.Observe();tc.Observe();bool ready=Min(4)>=.5&&Max(0)<=(emergency?1:4.7);stable=ready?stable+1:0;if(stable>=100){applied=true;break;}}
  Snapshot("end");tm.Finish(false);tc.Finish(false);Require(reduced,"KM did not reduce UR within bounded application phase");Require(applied,"Automatic braking did not reach every wagon within bounded application phase");tm.Finish(true);tc.Finish(true);
  Require(Max(4)<=4.5,"Wagon TC exceeded broad physical diagnostic bound 4.5");
  if(!emergency)HoldService();
  Begin(emergency?"emergency_release":"service_release");var rise=new Arrival("tm_rise",0,1);var fall=new Arrival("tc_fall",4,-1);Km(2);
  bool released=false;stable=0;for(int i=0;i<18000;i++){Step();rise.Observe();fall.Observe();bool ready=ChargeReady(false);stable=ready?stable+1:0;if(stable>=100){released=true;break;}}
  Snapshot("end");rise.Finish(false);fall.Finish(false);Require(released,"Train release/recharge did not establish headTM within actualPzar+/-0.15, UR>=actualPzar-0.15, every wagonTM within observedheadTM+/-0.3 (assumed75 four-axle wagons=300 axles), ownZR>=ownTM-0.15 and TC<=0.1 within18000 cycles");rise.Finish(true);fall.Finish(true);
 }

 static void HoldService(){
  Km(4);Begin("service_hold");
  double tcMinimum=Min(4),tcPeak=Max(4),tmMinimum=Min(0),tmMaximum=Max(0);
  var tmMin=new double[3];var tmMax=new double[3];var tcMin=new double[3];var tcMax=new double[3];var zrMin=new double[3];var zrMax=new double[3];
  for(int i=0;i<3;i++){tmMin[i]=tmMax[i]=Single(cars[Observed[i]],0);tcMin[i]=tcMax[i]=Single(cars[Observed[i]],4);zrMin[i]=zrMax[i]=Single(cars[Observed[i]],8);}
  // A small live summary is saved even if a holding assertion fails partway.
  serviceHold=new Dictionary<string,object>();serviceHold["phase"]="service_hold";serviceHold["km_input"]=4;serviceHold["required_cycles"]=1200;serviceHold["completed_cycles"]=0;serviceHold["nominal_required_seconds"]=120;serviceHold["completed"]=false;
  serviceHold["all_wagon_tc_min"]=tcMinimum;serviceHold["all_wagon_tc_peak"]=tcPeak;serviceHold["all_wagon_tm_min"]=tmMinimum;serviceHold["all_wagon_tm_max"]=tmMaximum;
  serviceHold["observed_order"]=ObservedNames;serviceHold["observed_wagon_indices"]=Observed;serviceHold["observed_tm_min"]=tmMin;serviceHold["observed_tm_max"]=tmMax;serviceHold["observed_tc_min"]=tcMin;serviceHold["observed_tc_max"]=tcMax;serviceHold["observed_zr_min"]=zrMin;serviceHold["observed_zr_max"]=zrMax;
  for(int cycle=0;cycle<1200;cycle++){
   Step();double minimum=Min(4),maximum=Max(4);tcMinimum=Math.Min(tcMinimum,minimum);tcPeak=Math.Max(tcPeak,maximum);tmMinimum=Math.Min(tmMinimum,Min(0));tmMaximum=Math.Max(tmMaximum,Max(0));
   for(int i=0;i<3;i++){double tm=Single(cars[Observed[i]],0),tc=Single(cars[Observed[i]],4),zr=Single(cars[Observed[i]],8);tmMin[i]=Math.Min(tmMin[i],tm);tmMax[i]=Math.Max(tmMax[i],tm);tcMin[i]=Math.Min(tcMin[i],tc);tcMax[i]=Math.Max(tcMax[i],tc);zrMin[i]=Math.Min(zrMin[i],zr);zrMax[i]=Math.Max(zrMax[i],zr);}
   serviceHold["completed_cycles"]=phaseCycles;serviceHold["all_wagon_tc_min"]=tcMinimum;serviceHold["all_wagon_tc_peak"]=tcPeak;serviceHold["all_wagon_tm_min"]=tmMinimum;serviceHold["all_wagon_tm_max"]=tmMaximum;
   Require(minimum>=.5,"Automatic brake spontaneously released a wagon during KM IV service hold at cycle "+phaseCycles+", minimum TC "+minimum);Require(maximum<=4.5,"Wagon TC exceeded broad physical diagnostic bound 4.5 during service hold at cycle "+phaseCycles);
  }
  serviceHold["completed"]=true;Snapshot("end");
 }

 static void Save(bool passed){
  Progress("before_final_result");
  var result=new{passed=passed,failure_reasons=Failures,phase=phase,total_cycles=cycles,nominal_sim_seconds=cycles*.1,wall_seconds=Clock.Elapsed.TotalSeconds,
   native_started=nativeStarted,safe_cold_initialized=coldInitialized,cold_geometry_preserved=coldGeometryPreserved,cold_geometry_checks=coldGeometryChecks,repeat_init_sostav=false,layout_verified=layoutVerified,native_air_nodes=77,wagons=75,wagon_gross_t=80,wagon_load_mode=1,auto_regulator=true,configured_wagon_motion_ftk=1,brake_pad_material="cast_iron",ftk_source="TrainerNativeSystems.InitializePhysics SetSostav argument6; native FTk1 cast-iron mapping confirmed separately",charging_setpoint_actual=chargingSetpointActual,loaded_cast_iron_charge_setpoint_in_re_range=chargingSetpointActual>=5.3&&chargingSetpointActual<=5.5,wagon_configuration=WagonConfiguration,layout_checks=layoutChecks,wagon_pressure_checks=pressureChecks,
   minimum_observed_pressure=hasPressureObservation?(double?)allTimeMinimum:null,maximum_observed_pressure=hasPressureObservation?(double?)allTimeMaximum:null,
   electrical_faults=host!=null&&nativeStarted?(int?)Field("electricalFaults"):null,managed_boundary_mass_error=circuit==null?(double?)null:circuit.MaximumBalanceError,
   full_native_train_mass_balance_verified=false,exact_equipment_bench=false,re008_calibrated=false,normative_brake_wave_speed_verified=false,normative_full_service_step_verified=false,service_diagnostic_ur_target_kgf_cm2=4.5,diagnostic_only=true,arrival_threshold_kgf_cm2=.1,charging_convergence_tolerance_kgf_cm2=RelativeHeadTmTolerance,charging_head_setpoint_tolerance_kgf_cm2=HeadSetpointTolerance,charging_zr_local_tm_lag_tolerance_kgf_cm2=ZrChargeLagTolerance,assumed_axles_per_wagon=4,assumed_wagon_axles=300,native_axle_count_verified=false,relative_head_tm_criterion_source="Rules808 AppendixK: head/tail TM difference<=0.3 for up to300 wagon axles; extended to each wagon as diagnostic, assuming75 four-axle wagons",nominal_step_seconds=.1,
   scope="Candidate77-node cold initial pressures preserve normal native geometry and initialized VR objects, then native pantograph/BV/PSN/main-compressor inputs; full75-wagon R1/ARon charge, service, KM IV1200-cycle hold, release, emergency and release. KM/KVT are stand inputs. Every wagon TM/TC/ZR checked every step; head/middle/tail wave arrivals use local baselines. No repeated InitNew/InitSostavClass, PM replenishment fixture, native output forcing or extra native patch. Reservoir pressure-volume inventory is partial; native VR chamber volumes/source/exhaust contracts and normative timing remain unverified. Actuator air demand/full locomotive preparation not accepted.",service_hold=serviceHold,startup_native_air=startupNativeAir,terminal_wagon_pressures=TerminalWagonPressures(),arrivals=Arrivals,snapshots=Rows};
  string json;try{json=new JavaScriptSerializer().Serialize(result);}catch(Exception serializationError){Failures.Add("Result graph serialization failed: "+serializationError.GetBaseException().Message);json=new JavaScriptSerializer().Serialize(new{passed=false,phase=phase,total_cycles=cycles,phase_cycles=phaseCycles,failure_reasons=Failures.ToArray(),snapshots_unavailable=true,charging_setpoint_actual=Finite(chargingSetpointActual)?(double?)chargingSetpointActual:null});}
  var resultPath=TrainerSession.ResultPath("Trainer_train_pneumatic_validation.json");File.WriteAllText(resultPath,json);Console.WriteLine(new JavaScriptSerializer().Serialize(new{passed=passed&&Failures.Count==0,phase=phase,total_cycles=cycles,failure_reasons=Failures.ToArray(),result_path=resultPath}));Console.Out.Flush();
 }

 [STAThread] static int Main(string[] args){Clock.Start();try{Progress("before_visual_styles");Application.EnableVisualStyles();Progress("before_host_constructor");
  host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);Progress("before_host_handle");var handle=host.Handle;Progress("before_start_native");Call("StartNative");nativeStarted=true;Progress("after_start_native");((Timer)Field("timer")).Stop();
  mem=(Dictionary<string,IntPtr>)Field("mem");loc=mem["TLocParam"];trainData=(IntPtr)Field("trainData");cars=(IntPtr[])Field("cars");chargingSetpointActual=Double(loc,0x13);Require(Finite(chargingSetpointActual)&&chargingSetpointActual>=4.8,"Unexpected actual charge setpoint");if(chargingSetpointActual<5.3||chargingSetpointActual>5.5)Failures.Add("Actual startup charging setpoint "+chargingSetpointActual+" is outside RE loaded cast-iron train range 5.3..5.5 kgf/cm2");
  var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);Require(cab!=IntPtr.Zero,"Native cab absent");form=Marshal.ReadIntPtr(cab,0xc);Require(form!=IntPtr.Zero&&Marshal.ReadIntPtr(form,0x530)==mem["TPult"],"Native cab input layout guard");
  mainControl=Control(0x3c8);bvReturn=Control(0x3e0);Input(mainControl,0);Input(bvReturn,0);Input(Control(0x3dc),0);Input(Control(0x3a0),0);Input(Control(0x3a4),0);
  var module=GetModuleHandle("Pult1.dll");Require(module!=IntPtr.Zero,"Pult1 absent");var owner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(module,0x1a2e60));Require(owner!=IntPtr.Zero&&Marshal.ReadIntPtr(owner,4)==mem["TPult"],"Native electrical owner guard");schema=IntPtr.Add(owner,0x18);
  Progress("before_startup_air_capture");startupNativeAir=new{inlet=NativeInlet(),train_inputs=TrainInputs(),nodes=NativeAirNodes()};Progress("before_cold_fixture");ColdFixture();Progress("after_cold_fixture");PowerAndCharge();BrakeAndRelease(false);BrakeAndRelease(true);CheckLayout();Require((int)Field("electricalFaults")==0,"Native electrical faults occurred");Save(Failures.Count==0);return Failures.Count==0?0:1;
 }catch(Exception error){Failures.Add(error.GetBaseException().Message);Progress("catch_before_snapshot");try{Snapshot("failure");foreach(var arrival in ActiveArrivals)arrival.Finish(false);}catch(Exception snapshotError){Failures.Add("Failure snapshot unavailable: "+snapshotError.GetBaseException().Message);}Progress("catch_before_save");Save(false);Console.WriteLine(error);Console.Out.Flush();return 1;
 }finally{if(host!=null){host.Close();host.Dispose();}}}
}
