// Experimental host coupling. Build separately: uncalibrated geometry must not silently replace the default model.
using System;using System.IO;using System.Security.Cryptography;using System.Collections.Generic;using System.Runtime.InteropServices;using TrainerAir;
partial class TrainerIntegrationHost {
 IntPtr nativeInitialPressureCave;IntPtr pantographPressureSources,pantographPressureCave;TwoSectionAirChain airCircuit;IntPtr secondBvr;double beforeAirPm,beforeAirAuto1,beforeAirAuto2;
 IntPtr controlPowerCave;bool controlPowerCommandsConnected;IntPtr tc2Source;bool airGaugeHooks;readonly List<KeyValuePair<IntPtr,byte[]>> airGaugeOriginal=new List<KeyValuePair<IntPtr,byte[]>>();
 bool locomotiveGeometryConnected;IntPtr perPadSource,perPadCave;bool perPadConnected;
 readonly double[] compressorUnloadRemaining=new double[4];readonly byte[] savedCompressorRequests=new byte[8];
 readonly bool[] controlSf3={true,true};readonly bool[] controlFeedOpen={true,true};readonly double[] controlConsumers={0,0};
 System.Windows.Forms.CheckBox auxiliaryCommandControl;bool? auxiliaryCommandOverride;bool auxiliaryPmAliasPatched;bool compressorRequestsMasked;byte beforeCompressorPressureState;
 partial void PrepareNativeAirCircuit(){
  ConnectNativeInitialPressureBounds(Library("TormModel.dll"));
  // RE6 table2.1 BB-3.5/10 nominal profile: per-section normal-volume rate.
  // DEN-30MO has a different 3.0 profile; exact equipment revision is still pending.
  Dbl(mem["TLocParam"],0x4f,3.5);Dbl(mem["TLocParam"],0x5f,3.5);
  // Native R codes:1 loaded,2 medium,3 empty. Scenario has75wagons at80t gross.
  // Keep the existing auto regulator enabled; its full load response is checked separately.
  // Loaded R1/cast-iron train: brake rules table V.1 specifies 5.3–5.5 kgf/cm2.
  const double trainCharge=5.0; // User-selected charging pressure; loaded-profile rules require 5.3–5.5.
  Dbl(mem["TLocParam"],0x13,trainCharge);F(mem["TLocParam"],9,(float)trainCharge);
  for(int w=0;w<76;w++){F(cars[w],0,(float)trainCharge);F(cars[w],8,(float)trainCharge);if(w>0)B(cars[w],0x18,1);}
  // Preserve total locomotive mass and existing total native TM volume while separating two BVR states.
  secondBvr=Alloc(243);byte[] record=new byte[243];Marshal.Copy(cars[0],record,0,243);Marshal.Copy(record,0,secondBvr,243);
  var vr=Alloc(136);record=new byte[136];Marshal.Copy(Marshal.ReadIntPtr(cars[0],0xe8),record,0,136);Marshal.Copy(record,0,vr,136);Ptr(secondBvr,0xe8,vr);
  foreach(var c in new[]{cars[0],secondBvr}){I(c,0x25,100);Dbl(c,0xcb,10.4);Dbl(c,0xd7,20);Dbl(c,0xdf,1);B(c,0x18,1);B(c,0x1a,0);}
  B(secondBvr,0x20,1);B(secondBvr,0x22,1);
  var array=Alloc(77*8);Ptr(array,0,mem["TLocParam"]);Ptr(array,4,cars[0]);Ptr(array,12,secondBvr);
  for(int i=1;i<76;i++)Ptr(array,(i+1)*8+4,cars[i]);I(trainData,0,77);Ptr(trainData,0x42,array);
  airCircuit=new TwoSectionAirChain(new TwoSectionAirParameters(2000,100,1,20,1,1,1,12,6,.7,.001),8,trainCharge,8);
  airCircuit.InitializeControlAir(5,5);
  ClientSize=new System.Drawing.Size(ClientSize.Width,355);for(int s=0;s<2;s++){int section=s;var sf3=new System.Windows.Forms.CheckBox{Left=10+s*330,Top=240,Width=320,Text="SF3 — вспом. компрессор, секция "+(s+1),Checked=true};sf3.CheckedChanged+=(sender,args)=>{controlSf3[section]=sf3.Checked;Log("SF3 section="+(section+1)+" on="+sf3.Checked);};Controls.Add(sf3);}
  auxiliaryCommandControl=new System.Windows.Forms.CheckBox{Left=10,Top=275,Width=650,ThreeState=true,CheckState=System.Windows.Forms.CheckState.Indeterminate,Text="SB1 — вспомогательный компрессор: аппаратный пульт"};auxiliaryCommandControl.CheckStateChanged+=(sender,args)=>{auxiliaryCommandOverride=auxiliaryCommandControl.CheckState==System.Windows.Forms.CheckState.Indeterminate?(bool?)null:auxiliaryCommandControl.Checked;auxiliaryCommandControl.Text="SB1 — вспомогательный компрессор: "+(auxiliaryCommandOverride.HasValue?(auxiliaryCommandOverride.Value?"включён":"выключен"):"аппаратный пульт");};Controls.Add(auxiliaryCommandControl);
  tc2Source=Alloc(8);
  Text+=" [ПНЕВМОСЕТЬ: ПРОВЕРОЧНАЯ ГЕОМЕТРИЯ]";
  Log("Experimental two-section air circuit: geometry is a diagnostic assumption, not accepted RE calibration");
 }
 partial void BeforeElectricalAirCycle(){
  if(airCircuit==null)return;Dbl(tc2Source,0,Math.Max(0,airCircuit.Sections[0].Cylinder[1].Gauge));
  if(!auxiliaryPmAliasPatched){
   var auxiliaryModule=GetModuleHandle("Pult1.dll");if(auxiliaryModule==IntPtr.Zero)throw new Exception("Auxiliary air module absent");
   string auxiliaryHash;using(var digest=SHA256.Create())using(var file=File.OpenRead(Path.Combine(resources,@"LOCOMOTIVES\1\Pult1.dll")))auxiliaryHash=BitConverter.ToString(digest.ComputeHash(file)).Replace("-","");
   if(auxiliaryHash!="7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5")throw new Exception("Unknown auxiliary-air native build");
   ConnectControlPowerCommands(auxiliaryModule);
   ConnectSectionPantographPressure(auxiliaryModule);
   // Native pressure-switch timing is retained. Main-reservoir pressure cannot
   // bypass an isolated control-air circuit. TPult.BD is supplied by physical RS6.
   // Section-specific control pressure routing is still a separate integration task.
   int[] risingPressureBypasses={0x18ce1c,0x18ce86,0x18cee2,0x18cf38};
   foreach(int site in risingPressureBypasses){var address=IntPtr.Add(auxiliaryModule,site);GuardAirGauge(address,Hex("7218"));WriteNativeBytes(address,Hex("9090"));}
   var fallingPressureBypass=IntPtr.Add(auxiliaryModule,0x18cfd6);GuardAirGauge(fallingPressureBypass,Hex("764a"));WriteNativeBytes(fallingPressureBypass,Hex("9090"));
   var aliasSite=IntPtr.Add(auxiliaryModule,0x18bef5);GuardAirGauge(aliasSite,Hex("d95805"));
   var algorithmSite=IntPtr.Add(auxiliaryModule,0x18be9d);GuardAirGauge(algorithmSite,Hex("8b45fc8b4004"));var skip=new byte[6];skip[0]=0xe9;Array.Copy(BitConverter.GetBytes(0x18bf9e-0x18be9d-5),0,skip,1,4);skip[5]=0x90;WriteNativeBytes(algorithmSite,skip);
   // Physical control-air pressure replaces the additive native pressure algorithm.
   // RS6/RS7 finite-volume coupling and actuator flows remain separate pending work.
   WriteNativeBytes(aliasSite,Hex("ddd890"));auxiliaryPmAliasPatched=true;
  }
  Dbl(mem["TPult"],0xbd,Math.Max(0,airCircuit.ControlAir[0].Rs6.Gauge));
  if(pantographPressureSources!=IntPtr.Zero)for(int i=0;i<4;i++)Dbl(pantographPressureSources,i*8,i<2?Math.Max(0,airCircuit.ControlAir[i].Rs6.Gauge):0);
  ConnectLocomotiveBrakeGeometry();
  ConnectPerPadFriction();
  if(airGaugeHooks)return;var module=GetModuleHandle("Communication1.dll");if(module==IntPtr.Zero)throw new Exception("TC2 communication module absent");
  string hash;using(var a=SHA256.Create())using(var f=File.OpenRead(Path.Combine(resources,@"LOCOMOTIVES\1\Communication1.dll")))hash=BitConverter.ToString(a.ComputeHash(f)).Replace("-","");
  if(hash!="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F")throw new Exception("TC2 unknown native build");
  var original=new List<byte>();original.AddRange(Hex("db2d"));original.AddRange(BitConverter.GetBytes(IntPtr.Add(module,0x2ae830).ToInt32()));
  GuardAirGauge(IntPtr.Add(module,0x2ae6e8),original.ToArray());GuardAirGauge(IntPtr.Add(module,0x2ae6fa),Hex("d8680d"));
  original.Clear();original.AddRange(Hex("d9400ddb2d"));original.AddRange(BitConverter.GetBytes(IntPtr.Add(module,0x2b1ee4).ToInt32()));original.AddRange(Hex("dee9"));
  GuardAirGauge(IntPtr.Add(module,0x2b1e75),original.ToArray());
  var load=new List<byte>();load.AddRange(Hex("dd05"));load.AddRange(BitConverter.GetBytes(tc2Source.ToInt32()));
  try{WriteNativeBytes(IntPtr.Add(module,0x2ae6e8),load.ToArray());WriteNativeBytes(IntPtr.Add(module,0x2ae6fa),Hex("909090"));
   load.AddRange(Hex("9090909090"));WriteNativeBytes(IntPtr.Add(module,0x2b1e75),load.ToArray());airGaugeHooks=true;
  }catch{CloseNativeAirCircuit();throw;}
  Log("TC2 native gauge/hardware output uses physical second bogie pressure; old TC1-minus0.1 removed at runtime");
 }
 void GuardAirGauge(IntPtr address,byte[] expected){for(int i=0;i<expected.Length;i++)if(Marshal.ReadByte(address,i)!=expected[i])throw new Exception("Candidate native layout guard at "+address.ToString("x"));airGaugeOriginal.Add(new KeyValuePair<IntPtr,byte[]>(address,expected));}
 void ConnectLocomotiveBrakeGeometry(){
  if(locomotiveGeometryConnected)return;var module=GetModuleHandle("POEZD.DLL");if(module==IntPtr.Zero)throw new Exception("Brake geometry motion module absent");
  string hash;using(var a=SHA256.Create())using(var f=File.OpenRead(Path.Combine(resources,"POEZD.DLL")))hash=BitConverter.ToString(a.ComputeHash(f)).Replace("-","");
  if(hash!="822B6D17CE45F80179A512EE6C70A35D5AE29BF187530BDA5EBD11446CFB3553")throw new Exception("Brake geometry unknown native build");
  // Only this locomotive uses native geometry index2. Do not change a shared table if any wagon also uses it.
  if(Marshal.ReadInt32(module,0x1ab24)!=2)throw new Exception("Unexpected locomotive brake geometry index");
  for(int i=1;i<76;i++)if(Marshal.ReadInt32(module,0x1ab24+i*4)==2)throw new Exception("Brake geometry index shared with wagon");
  var area=IntPtr.Add(module,0x151c8);var firstLever=IntPtr.Add(module,0x151d4);var secondLever=IntPtr.Add(module,0x151e0);
  GuardAirGauge(area,BitConverter.GetBytes(1257f));GuardAirGauge(firstLever,BitConverter.GetBytes(8.245f));GuardAirGauge(secondLever,BitConverter.GetBytes(12.915f));
  // RE5 section3.11:203mm bore and total8.8 lever ratio;4 cylinders per bogie,16 across two sections.
  // Pad1 coefficients identify cast iron; exact flanged-pad law, spring and .85 efficiency remain unaccepted.
  WriteNativeBytes(area,BitConverter.GetBytes((float)(Math.PI*20.3*20.3/4)));
  WriteNativeBytes(firstLever,BitConverter.GetBytes(8.8f));WriteNativeBytes(secondLever,BitConverter.GetBytes(8.8f));
  locomotiveGeometryConnected=true;Log("Locomotive brake geometry:203mm cylinder,8.8 total lever ratio,16 cylinders; native pad/friction law still pending RE acceptance");
 }
 partial void SelectNativeLocomotiveBrakeInput(ref float input){
  if(airCircuit==null)return;double sum=0,part=0;for(int s=0;s<2;s++)for(int b=0;b<2;b++){
   double pressure=Math.Max(0,airCircuit.Sections[s].Cylinder[b].Gauge);sum+=pressure;
   // Retain native numeric units and .85 efficiency. Each bogie has eight pads.
   // Material-specific calibration and return spring are still pending.
   double k=Math.PI*20.3*20.3/4*4.4*.00085*pressure;
   part+=8*.031*k*(k+6.25)/(k+1.92);
  }
  if(perPadSource!=IntPtr.Zero)Dbl(perPadSource,0,-part);
  // Four individual wheel cylinders per bogie. Correct area and lever ratio are connected separately.
  input=(float)(sum*4);
 }
 void ConnectPerPadFriction(){
  if(perPadConnected)return;var module=GetModuleHandle("POEZD.DLL");
  if(!locomotiveGeometryConnected||module==IntPtr.Zero)throw new Exception("Per-pad module geometry guard");
  if(Marshal.ReadInt32(module,0x1ad7c)!=1)throw new Exception("Per-pad cast iron branch guard");
  var site=IntPtr.Add(module,0x1ebb);byte[] original=Hex("d84df48b55f0");GuardAirGauge(site,original);
  perPadSource=Marshal.AllocHGlobal(8);Dbl(perPadSource,0,0);
  perPadCave=VirtualAlloc(IntPtr.Zero,(UIntPtr)64,0x3000,0x40);if(perPadCave==IntPtr.Zero)throw new Exception("Per-pad cave allocation failed");
  var code=new List<byte>();
  // Preserve flags. Replace x87 load part only for vehicle0; wagon branches stay native.
  code.AddRange(Hex("9c837df0007508ddd8dd05"));code.AddRange(BitConverter.GetBytes(perPadSource.ToInt32()));code.Add(0x9d);
  code.AddRange(original);AddRel(code,0xe9,perPadCave,IntPtr.Add(module,0x1ec1));WriteNativeBytes(perPadCave,code.ToArray());
  var jump=new List<byte>();AddRel(jump,0xe9,site,perPadCave);jump.Add(0x90);WriteNativeBytes(site,jump.ToArray());perPadConnected=true;
  Log("Candidate friction sums 32 individual pads using four bogie pressures; native speed factor retained; force/material calibration pending");
 }
 partial void CloseNativeAirCircuit(){foreach(var site in airGaugeOriginal)WriteNativeBytes(site.Key,site.Value);airGaugeOriginal.Clear();if(nativeInitialPressureCave!=IntPtr.Zero){VirtualFree(nativeInitialPressureCave,UIntPtr.Zero,0x8000);nativeInitialPressureCave=IntPtr.Zero;}if(pantographPressureCave!=IntPtr.Zero){VirtualFree(pantographPressureCave,UIntPtr.Zero,0x8000);pantographPressureCave=IntPtr.Zero;}if(pantographPressureSources!=IntPtr.Zero){Marshal.FreeHGlobal(pantographPressureSources);pantographPressureSources=IntPtr.Zero;}auxiliaryPmAliasPatched=false;airGaugeHooks=false;locomotiveGeometryConnected=false;
  controlPowerCommandsConnected=false;if(controlPowerCave!=IntPtr.Zero){VirtualFree(controlPowerCave,UIntPtr.Zero,0x8000);controlPowerCave=IntPtr.Zero;}
  perPadConnected=false;if(perPadCave!=IntPtr.Zero){VirtualFree(perPadCave,UIntPtr.Zero,0x8000);perPadCave=IntPtr.Zero;}
  if(perPadSource!=IntPtr.Zero){Marshal.FreeHGlobal(perPadSource);perPadSource=IntPtr.Zero;}
  if(tc2Source!=IntPtr.Zero){Marshal.FreeHGlobal(tc2Source);tc2Source=IntPtr.Zero;}
 }
 partial void BeforeNativeAirStep(){
  if(airCircuit==null)return;var loc=mem["TLocParam"];
  if(auxiliaryCommandOverride.HasValue)B(mem["TPult"],0x46,(byte)(auxiliaryCommandOverride.Value?1:0));
  beforeCompressorPressureState=Marshal.ReadByte(loc,0x12);Marshal.Copy(IntPtr.Add(loc,0x80),savedCompressorRequests,0,8);compressorRequestsMasked=true;
  // BB profile only: RE6 2.3 removes readiness for12s after unloading.
  // Keep electrical requests intact outside the physical air-source call, including exceptions.
  for(int i=0;i<4;i++)if(compressorUnloadRemaining[i]>1e-8){B(loc,0x80+i,0);B(loc,0x84+i,0);}
  F(loc,5,(float)airCircuit.Pm.Gauge);F(cars[0],4,(float)airCircuit.Automatic[0].Gauge);F(secondBvr,4,(float)airCircuit.Automatic[1].Gauge);
  // Use the physical pressure as the mass-delta basis. A rounded Single input
  // can exceed it: native release to zero would then remove more than available.
  // Native output quantization remains included in the signed boundary balance.
  beforeAirPm=airCircuit.Pm.Gauge;beforeAirAuto1=airCircuit.Automatic[0].Gauge;beforeAirAuto2=airCircuit.Automatic[1].Gauge;
 }
 partial void AfterNativeAirStep(){
  if(airCircuit==null)return;var loc=mem["TLocParam"];
  bool unloaded=beforeCompressorPressureState==1&&Marshal.ReadByte(loc,0x12)==0;
  for(int i=0;i<4;i++){
   bool wasReady=compressorUnloadRemaining[i]<=1e-8;
   compressorUnloadRemaining[i]=Math.Max(0,compressorUnloadRemaining[i]-.1);
   if(unloaded&&wasReady&&(savedCompressorRequests[i]!=0||savedCompressorRequests[i+4]!=0))compressorUnloadRemaining[i]=12;
  }
  airCircuit.ApplyNativePressureChanges(FloatAt(loc,5)-beforeAirPm,FloatAt(cars[0],4)-beforeAirAuto1,FloatAt(secondBvr,4)-beforeAirAuto2);
  int kvt=Marshal.ReadByte(loc,0x93)!=0?4:Marshal.ReadByte(loc,0x7e);
  // OtpTorm is produced by the native cab's release button; the normal independent path remains available.
  // KEB2 actuator air consumption and minimum operating pressure remain uncalibrated.
  airCircuit.StepControls(kvt,.1,Marshal.ReadByte(loc,0x90)==1,Marshal.ReadByte(loc,0x8c)!=0,Marshal.ReadByte(loc,0x8f)==1&&Marshal.ReadByte(loc,0x90)==1);
  StepHostControlAir();
  F(loc,5,(float)airCircuit.Pm.Gauge);F(cars[0],4,(float)airCircuit.Automatic[0].Gauge);F(secondBvr,4,(float)airCircuit.Automatic[1].Gauge);
  Dbl(loc,0xec,airCircuit.Independent.Gauge);
  // Existing single Loc.TC field is the cab's first bogie; TC2 has its own runtime source.
  F(loc,13,(float)airCircuit.Sections[0].Cylinder[0].Gauge);
 }
 void ConnectControlPowerCommands(IntPtr module){
  if(controlPowerCommandsConnected)return;controlPowerCave=VirtualAlloc(IntPtr.Zero,(UIntPtr)128,0x3000,0x40);if(controlPowerCave==IntPtr.Zero)throw new Exception("Control power cave allocation");
  for(int i=0;i<2;i++){int rva=i==0?0x18bfd1:0x18bffc;byte state=(byte)(i==0?1:0);var original=Hex("c68418a9010000"+state.ToString("x2"));var site=IntPtr.Add(module,rva);GuardAirGauge(site,original);var start=IntPtr.Add(controlPowerCave,i*32);var code=new List<byte>();code.AddRange(original);code.AddRange(Hex("c68418b5010000"+state.ToString("x2")));AddRel(code,0xe9,start,IntPtr.Add(site,8));WriteNativeBytes(start,code.ToArray());var jump=new List<byte>();AddRel(jump,0xe9,site,start);jump.AddRange(Hex("909090"));WriteNativeBytes(site,jump.ToArray());}
  var vcuSite=IntPtr.Add(module,0x18bfa7);GuardAirGauge(vcuSite,Hex("80b88f00000000762b"));var vcuStart=IntPtr.Add(controlPowerCave,64);var vcuCode=new List<byte>();vcuCode.AddRange(Hex("80b88f00000001"));vcuCode.AddRange(Hex("0f82"));vcuCode.AddRange(BitConverter.GetBytes(IntPtr.Add(module,0x18bfdb).ToInt32()-vcuStart.ToInt32()-vcuCode.Count-4));vcuCode.AddRange(Hex("80b88f00000002"));vcuCode.AddRange(Hex("0f86"));vcuCode.AddRange(BitConverter.GetBytes(IntPtr.Add(module,0x18bfb0).ToInt32()-vcuStart.ToInt32()-vcuCode.Count-4));AddRel(vcuCode,0xe9,vcuStart,IntPtr.Add(module,0x18bfdb));WriteNativeBytes(vcuStart,vcuCode.ToArray());var vcuJump=new List<byte>();AddRel(vcuJump,0xe9,vcuSite,vcuStart);vcuJump.AddRange(Hex("90909090"));WriteNativeBytes(vcuSite,vcuJump.ToArray());controlPowerCommandsConnected=true;
 }


 void ConnectNativeInitialPressureBounds(IntPtr module){
  string hash;using(var digest=SHA256.Create())using(var file=File.OpenRead(Path.Combine(resources,"TormModel.dll")))hash=BitConverter.ToString(digest.ComputeHash(file)).Replace("-","");
  if(hash!="F871640C5B7699901E3AFBF755BB2B797B659524C29BAA046B311CA6ECC875B9")throw new Exception("Unknown cold pressure initializer");
  nativeInitialPressureCave=VirtualAlloc(IntPtr.Zero,(UIntPtr)192,0x3000,0x40);if(nativeInitialPressureCave==IntPtr.Zero)throw new Exception("Initial pressure bounds allocation");
  int[] sites={0x177aeb,0x177b33,0x177b5b};for(int i=0;i<3;i++){
   var site=IntPtr.Add(module,sites[i]);var original=new List<byte>();original.AddRange(Hex("d900db2d"));original.AddRange(BitConverter.GetBytes(IntPtr.Add(module,i==0?0x177ccc:0x177cb4).ToInt32()));original.AddRange(Hex("dee9"));GuardAirGauge(site,original.ToArray());
   var start=IntPtr.Add(nativeInitialPressureCave,i*64);var code=new List<byte>();code.AddRange(original);code.AddRange(Hex("509cd9e49bdfe09e7304ddd8d9ee9d58"));AddRel(code,0xe9,start,IntPtr.Add(site,10));WriteNativeBytes(start,code.ToArray());var jump=new List<byte>();AddRel(jump,0xe9,site,start);jump.AddRange(Hex("9090909090"));WriteNativeBytes(site,jump.ToArray());
  }
 }
 void ConnectSectionPantographPressure(IntPtr module){
  pantographPressureSources=Marshal.AllocHGlobal(32);for(int i=0;i<4;i++)Dbl(pantographPressureSources,i*8,0);
  // Hardware contract: Tokopr_1..4 buttons are section1..4, not two pantographs per section.
  int[] comparisons={0x18ce2a,0x18ce94,0x18cef0,0x18cf46};
  for(int i=0;i<4;i++){var site=IntPtr.Add(module,comparisons[i]);GuardAirGauge(site,Hex("dc98bd000000"));var code=new List<byte>();code.AddRange(Hex("dc1d"));code.AddRange(BitConverter.GetBytes(IntPtr.Add(pantographPressureSources,i*8).ToInt32()));WriteNativeBytes(site,code.ToArray());}
  var fall=IntPtr.Add(module,0x18cfe4);GuardAirGauge(fall,Hex("dc98bd000000"));
  pantographPressureCave=VirtualAlloc(IntPtr.Zero,(UIntPtr)64,0x3000,0x40);if(pantographPressureCave==IntPtr.Zero)throw new Exception("Section pressure cave allocation");
  var body=new List<byte>();body.AddRange(Hex("518b4df849dc1ccd"));body.AddRange(BitConverter.GetBytes(pantographPressureSources.ToInt32()));body.Add(0x59);AddRel(body,0xe9,pantographPressureCave,IntPtr.Add(fall,6));WriteNativeBytes(pantographPressureCave,body.ToArray());var jump=new List<byte>();AddRel(jump,0xe9,fall,pantographPressureCave);jump.Add(0x90);WriteNativeBytes(fall,jump.ToArray());
 }
 bool NativeControlPowerReady(){var module=GetModuleHandle("Pult1.dll");if(module==IntPtr.Zero)return false;var owner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(module,0x1a2e60));if(owner==IntPtr.Zero||Marshal.ReadIntPtr(owner,4)!=mem["TPult"])throw new Exception("Control power owner guard");var schema=IntPtr.Add(owner,0x18);int section=Marshal.ReadByte(schema,0x36)-1;if(section<0||section>3)throw new Exception("Control power section guard");return Marshal.ReadByte(schema,0x1ae+section)!=0&&Marshal.ReadByte(schema,0x1b2+section)!=0&&Marshal.ReadByte(schema,0x1ba+section)!=0&&Marshal.ReadByte(schema,0x1be+section)!=0;}
 void StepHostControlAir(){int vcuPosition=Marshal.ReadByte(mem["TLocParam"],0x8f);bool key=(vcuPosition==1||vcuPosition==2)&&NativeControlPowerReady();bool command=Marshal.ReadByte(mem["TPult"],0x46)!=0;var valves=new[]{key&&controlSf3[0],key&&controlSf3[1]};
  // Nominal D100 delivery; complete DC supply/contactors, automatic pressure control and actuator demand remain unverified.
  bool auxiliaryRunning=valves[0]&&command;
  airCircuit.StepControlAir(.1,controlFeedOpen,valves,new[]{auxiliaryRunning?.105:0.0,0.0},controlConsumers);B(mem["TPult"],0xbc,(byte)(auxiliaryRunning?1:0));Dbl(mem["TPult"],0xbd,Math.Max(0,airCircuit.ControlAir[0].Rs6.Gauge));
 }
 partial void RestoreNativeAirCompressorRequests(){
  if(!compressorRequestsMasked)return;Marshal.Copy(savedCompressorRequests,0,IntPtr.Add(mem["TLocParam"],0x80),8);compressorRequestsMasked=false;
 }
}








