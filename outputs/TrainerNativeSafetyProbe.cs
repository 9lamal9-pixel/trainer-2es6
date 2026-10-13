using System;
using System.IO;
using System.Diagnostics;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Prepared native integration test. The declared White CodeWay sensor is scoped
// to KLUB only; route occupancy/markup belongs to the separate markup probe.
// No protection, force, speed, pressure, contactor or permission output writes.
class TrainerNativeSafetyProbe {
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void NativeCall();
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
 [DllImport("kernel32.dll")] static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint kind,uint protection);
 [DllImport("kernel32.dll")] static extern bool VirtualFree(IntPtr address,UIntPtr size,uint kind);
 [DllImport("kernel32.dll")] static extern bool FlushInstructionCache(IntPtr process,IntPtr address,UIntPtr size);
 [DllImport("kernel32.dll")] static extern IntPtr GetCurrentProcess();
 [StructLayout(LayoutKind.Sequential)] struct MemoryInfo { public IntPtr Base,AllocationBase;public uint AllocationProtect;public UIntPtr RegionSize;public uint State,Protect,Type; }
 [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr p,out MemoryInfo info,UIntPtr length);
 static readonly BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static TrainerIntegrationHost host;static Dictionary<string,IntPtr> mem;static IntPtr[] cars;
 static TrainerNativeSafetyAdapter adapter;static IntPtr communication,cabForm,tumbaForm,klubModule,klubOwner,klubSlot;
 static readonly Dictionary<IntPtr,byte> cabChanges=new Dictionary<IntPtr,byte>();
 static readonly Dictionary<int,IntPtr> cabPointers=new Dictionary<int,IntPtr>();
 static readonly List<object> checks=new List<object>(),rows=new List<object>(),keyRows=new List<object>();
 static readonly Stopwatch watch=new Stopwatch();
 static int cycles,alarmCycle=-1,piskCycle=-1,coilCycle=-1,tripCycle=-1;
 static bool whiteScenario,uiSaved,nativeInputsSet,restored,adapterDetached,closed;
 static bool antiwedgingAccepted,allowedAckAccepted,missedAckAccepted,physicalBrakeAccepted,forceObserved,motionObserved;
 static bool oldVigilance,oldPermission,oldSoftwareTd,oldTd1,oldTd2;
 static decimal oldTarget;static int oldMonitor,oldSautChoice;static byte oldEpk,oldVcu,oldKm,oldKvt;
 static float requestPressure,minimumPressure,preTripTm,preTripTc;
 static IntPtr ownedCode;static string phase="initial",failure;
 static void Check(string name,bool pass,object expected,object actual){checks.Add(new{name=name,passed=pass,expected=expected,actual=actual});if(!pass)throw new Exception(name+" expected="+expected+" actual="+actual);}
 static object Field(string name){var f=typeof(TrainerIntegrationHost).GetField(name,flags);if(f==null)throw new Exception("Missing host field "+name);return f.GetValue(host);}
 static void SetField(string name,object value){typeof(TrainerIntegrationHost).GetField(name,flags).SetValue(host,value);}
 static void Call(string name,params object[] args){var method=typeof(TrainerIntegrationHost).GetMethod(name,flags);if(method==null)throw new Exception("Missing host method "+name);try{method.Invoke(host,args);}catch(TargetInvocationException e){throw new Exception(name+": "+e.InnerException,e.InnerException);}}
 static float F(IntPtr p,int offset){var b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
 static string Hash(string file){using(var hash=SHA256.Create())using(var stream=File.OpenRead(file))return BitConverter.ToString(hash.ComputeHash(stream)).Replace("-","");}
 static bool Readable(IntPtr p,int count){MemoryInfo info;if(p==IntPtr.Zero||VirtualQuery(p,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)return false;ulong a=unchecked((uint)p.ToInt32()),b=unchecked((uint)info.Base.ToInt32());return info.State==0x1000&&(info.Protect&0x101)==0&&a>=b&&a+(uint)count<=b+info.RegionSize.ToUInt32();}
 static string Name(IntPtr p){if(!Readable(p,4))throw new Exception("Object pointer guard");var vmt=Marshal.ReadIntPtr(p);if(!Readable(IntPtr.Add(vmt,-56),60))throw new Exception("VMT pointer guard");var n=Marshal.ReadIntPtr(vmt,-56);if(!Readable(n,1))throw new Exception("Class-name pointer guard");int length=Marshal.ReadByte(n);if(length<2||length>100||!Readable(n,length+1))throw new Exception("Class-name length guard");var bytes=new byte[length];Marshal.Copy(IntPtr.Add(n,1),bytes,0,length);return Encoding.ASCII.GetString(bytes);}
 static void GuardCode(int rva,string expected){var b=new byte[expected.Length/2];Marshal.Copy(IntPtr.Add(communication,rva),b,0,b.Length);string actual=BitConverter.ToString(b).Replace("-","");Check("Native UI code "+rva.ToString("x"),actual==expected,expected,actual);}
 static void Invoke(List<byte> code){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)128,0x3000,0x40);if(p==IntPtr.Zero)throw new Exception("Input thunk allocation");try{Marshal.Copy(code.ToArray(),0,p,code.Count);if(!FlushInstructionCache(GetCurrentProcess(),p,(UIntPtr)code.Count))throw new Exception("Input thunk flush");((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{if(!VirtualFree(p,UIntPtr.Zero,0x8000))throw new Exception("Input thunk free");}}
 static void Register(IntPtr self,int value,int rva){var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(self.ToInt32()));code.Add(0xba);code.AddRange(BitConverter.GetBytes(value));code.Add(0xb9);code.AddRange(BitConverter.GetBytes(IntPtr.Add(communication,rva).ToInt32()));code.AddRange(new byte[]{0xff,0xd1,0xc3});Invoke(code);}
 static void Position(IntPtr control,double value){Check("Native position control",Name(control)=="TMultiPosTumbler","TMultiPosTumbler",Name(control));var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(control.ToInt32()));var d=BitConverter.GetBytes(value);code.Add(0x68);code.AddRange(new byte[]{d[4],d[5],d[6],d[7]});code.Add(0x68);code.AddRange(new byte[]{d[0],d[1],d[2],d[3]});code.AddRange(new byte[]{0x6a,0,0x6a,0});code.Add(0xb9);code.AddRange(BitConverter.GetBytes(IntPtr.Add(communication,0x2a4944).ToInt32()));code.AddRange(new byte[]{0xff,0xd1,0xc3});Invoke(code);}
 static void Cab(int offset,byte value){IntPtr p;if(!cabPointers.TryGetValue(offset,out p)){var control=Marshal.ReadIntPtr(cabForm,offset);string expectedClass=offset==0x3bc?"TTumbler3Pos":"TTumbler";Check("Physical cab control class "+offset.ToString("x"),Name(control)==expectedClass,expectedClass,Name(control));p=IntPtr.Add(control,0x1d9);Check("Physical cab input "+offset.ToString("x"),Readable(p,1),true,Readable(p,1));cabPointers[offset]=p;cabChanges[p]=Marshal.ReadByte(p);}Marshal.WriteByte(p,value);}
 static TrainerAutovedenieInterface Ui(){return (TrainerAutovedenieInterface)Field("avpUi");}
 static TrainerAvpDecision Decision(){return (TrainerAvpDecision)Field("avpDecision");}
 static int WindowType(){var ms=(IntPtr[])typeof(TrainerAutovedenieInterface).GetField("monitors",flags).GetValue(Ui());return Marshal.ReadByte(ms[0],0x14);}
 static void Key(byte code,string label){Call("SendMpsudKey",(byte)0);Step(true);Call("SendMpsudKey",code);Step(true);Call("SendMpsudKey",(byte)0);Step(true);var s=Ui().ReadSettings();keyRows.Add(new{label=label,code=code,window=WindowType(),selected=s.SelectedField,editing=s.Editing,mode=s.Mode,intensity=s.Intensity,train=s.TrainNumber,mass=s.MassT,wagons=s.Wagons,empty=s.Empty,valid=s.Valid,reason=s.Reason});}
 static void Digit(int digit,string label){Key((byte)(16-digit),label);}
 static void MoveField(int field){for(int n=0;n<8&&Ui().ReadSettings().SelectedField!=field;n++){int current=Ui().ReadSettings().SelectedField;Key((byte)(current<field?5:6),"select_field_"+field);}Check("SC09 selected field "+field,Ui().ReadSettings().SelectedField==field,field,Ui().ReadSettings().SelectedField);}
 static void Number(string value,string label){foreach(char c in value)Digit(c-'0',label+"_"+c);Key(1,label+"_Enter");}
 static void Settings(){phase="native_settings";Check("Initial native monitor clock",WindowType()==1,1,WindowType());Digit(2,"Clock_to_Mash");Check("Native driver screen",WindowType()==2,2,WindowType());Digit(3,"Mash_to_AV_settings");Check("Native AV settings",WindowType()==10,10,WindowType());MoveField(0);Key(14,"ordinary_mode_native_key14");Check("Native ordinary mode selected",Ui().ReadSettings().Mode==0,0,Ui().ReadSettings().Mode);MoveField(1);Digit(2,"intensity_2");MoveField(2);Number("123","train");MoveField(3);Number("6000","mass");MoveField(4);Number("75","wagons");MoveField(5);Digit(0,"loaded_train");Key(1,"settings_finish_Enter");Check("Return to native driver screen",WindowType()==2,2,WindowType());var s=Ui().ReadSettings();Check("Native settings valid",s.Valid,true,s.Reason);Check("Ordinary mode and declared train",s.Mode==0&&s.Intensity==2&&s.TrainNumber==123&&s.MassT==6000&&s.Wagons==75&&!s.Empty,"mode0/intensity2/train123/6000t/75loaded",new{s.Mode,s.Intensity,s.TrainNumber,s.MassT,s.Wagons,s.Empty});}
 static float Speed(){return F(mem["TInputParamPult"],1);}
 static double D(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
 static void Held(int index,bool held){Call("SetVigilanceHeld",index,false,held);}
 static void Released(){for(int i=0;i<3;i++)Held(i,false);}
 static object Row(){var loc=mem["TLocParam"];var common=mem["TOutParamAllTRen"];var own=mem["StandaloneSautOutput"];return new{
  phase=phase,cycles=cycles,sim_seconds=cycles*.1,v_kmh=Speed(),force=F(mem["TResTyga"],0),position=Field("position"),
  epk_key=Marshal.ReadByte(loc,0x99),coil=Marshal.ReadByte(loc,0x9a),trip=Marshal.ReadByte(loc,0x9b),whistle=Marshal.ReadByte(loc,0x9c),kon=Marshal.ReadByte(loc,0x9d),
  pcamera=F(loc,0x9f),pm=F(loc,5),head_tm=F(cars[0],0),head_tc=F(loc,13),tail_tc=F(cars[cars.Length-1],4),
  rb_counter=Marshal.ReadInt32(mem["CommOwner"],0x278),rb_antiwedging=Marshal.ReadByte(common,0x30),
  klub_alarm=Marshal.ReadByte(common,0x15),klub_pisk=Marshal.ReadByte(common,0x16),klub_light=Marshal.ReadByte(common,0x20),
  klub_tmp_alsn=Marshal.ReadByte(klubOwner,0xef),klub_period=Marshal.ReadInt32(klubOwner,0x11c),klub_counter=Marshal.ReadInt32(klubOwner,0x85),klub_led=Marshal.ReadByte(klubOwner,0x12c),klub_ack_type=Marshal.ReadByte(klubOwner,0x131),
  source_pisk=new[]{Marshal.ReadByte(common,0x16),Marshal.ReadByte(own,0xe),Marshal.ReadByte(common,0x23),Marshal.ReadByte(common,0x29),Marshal.ReadByte(common,0x30)},
  envelope_pisk=new[]{Marshal.ReadByte(adapter.InputOutput,0x16),Marshal.ReadByte(adapter.InputOutput,0xe),Marshal.ReadByte(adapter.InputOutput,0x23),Marshal.ReadByte(adapter.InputOutput,0x29),Marshal.ReadByte(adapter.InputOutput,0x30)},
  state=Decision()==null?"null":Decision().State.ToString(),enabled=Decision()!=null&&Decision().DriveEnabled,reason=Decision()==null?"":Decision().Reason,electrical_faults=Field("electricalFaults")};}
 static void NoDiagnosticInputs(){
  if(Field("commandedKm")!=null||Field("fileKm")!=null||(bool)Field("safetyPower")||(bool)Field("momentaryCharge"))throw new Exception("Diagnostic control/pressure/safety override active");
  if(File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")))throw new Exception("Native safety test requires no command file");
 }
 static void Step(bool dispatch=false){
  if(watch.Elapsed.TotalSeconds>230||cycles>9000)throw new Exception("Native safety scenario bound reached");
  NoDiagnosticInputs();Call("PollCommands");Call("BeforeAutovedenieCycle");Call("ElectricalCycle");Call("AfterAutovedenieElectricalCycle");Call("ApplyTestInputs");
  Call("RouteInputCycle");Call("AuxCycle");Call("KlubCoordinateCycle");
  var input=mem["TInputParamPult"];byte saved=Marshal.ReadByte(input,0x1e);
  try {
   // Proven native encoding5=White. This is primary sensor input to one firmware
   // step, not an alarm/relay or a changed physical train/route state.
   if(whiteScenario)Marshal.WriteByte(input,0x1e,5);
   ((Delegate)Field("klubStep")).DynamicInvoke();
  }finally{if(whiteScenario&&Marshal.ReadByte(input,0x1e)==5)Marshal.WriteByte(input,0x1e,saved);}
  if(whiteScenario&&Marshal.ReadByte(input,0x1e)!=saved)throw new Exception("Declared KLUB sensor source restoration failed");
  if(Marshal.ReadByte(mem["StandaloneSautOutput"],0xe)!=0)throw new Exception("Explicit OFF software SAUT produced an unexpected request");
  Call("PneumaticCycle");Call("MotionCycle");Call("RouteInputCycle");
  SetField("ticks",Convert.ToInt32(Field("ticks"))+1);cycles++;
  if(F(mem["TResTyga"],0)>0)forceObserved=true;if(Speed()>1)motionObserved=true;
  if(Convert.ToInt32(Field("electricalFaults"))!=0)throw new Exception("Native electrical fault");
  if(Marshal.ReadIntPtr(klubModule,0x1d7028)!=klubSlot||Marshal.ReadIntPtr(klubSlot)!=klubOwner)throw new Exception("KLUB owner lifetime changed");
  if((bool)Field("routeFinished"))throw new Exception("Safety scenario unexpectedly reached route boundary");
  if(cycles%25==0)rows.Add(Row());if(dispatch||cycles%10==0)Application.DoEvents();
 }
 static void Warm(int n){for(int i=0;i<n;i++)Step();rows.Add(Row());}
 static void NativeHandle(int field,double value){Position(Marshal.ReadIntPtr(cabForm,field),value);}
 static void Setup(){
  communication=GetModuleHandle("Communication1.dll");
  Check("Communication module SHA",Hash(Path.Combine((string)Field("root"),@"RESOURCES\LOCOMOTIVES\1\Communication1.dll"))=="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F","verified",true);
  var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);Check("Real cab class",Name(cab)=="TPultMash2ES6","TPultMash2ES6",Name(cab));cabForm=Marshal.ReadIntPtr(cab,0xc);
  Check("Real cab shared Pult",Marshal.ReadIntPtr(cabForm,0x530)==mem["TPult"],mem["TPult"].ToString("X"),Marshal.ReadIntPtr(cabForm,0x530).ToString("X"));
  GuardCode(0x2ae994,"558BEC83C4F08855");GuardCode(0x2a4944,"558BEC83C4F08945");Register(cab,1,0x2ae994);
  var tumba=Marshal.ReadIntPtr(cab,0x10);Check("Real tumba class",Name(tumba)=="TPult_Tumba_2ES6","TPult_Tumba_2ES6",Name(tumba));tumbaForm=Marshal.ReadIntPtr(tumba,0xc);
  oldEpk=Marshal.ReadByte(mem["TLocParam"],0x99);oldVcu=Marshal.ReadByte(mem["TLocParam"],0x8f);oldKm=Marshal.ReadByte(mem["TLocParam"],0x7d);oldKvt=Marshal.ReadByte(mem["TLocParam"],0x7e);nativeInputsSet=true;
  Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),0);Position(Marshal.ReadIntPtr(tumbaForm,0x3a4),1);NativeHandle(0x3b0,2);NativeHandle(0x3b4,3);
  ((CheckBox)Field("avpSoftwareTd")).Checked=true;((CheckBox)Field("avpTd1")).Checked=true;((CheckBox)Field("avpTd2")).Checked=true;
  ((CheckBox)Field("avpMovementPermission")).Checked=true;((NumericUpDown)Field("avpTargetLimit")).Value=10;((ComboBox)Field("monitorCombo")).SelectedIndex=0;
  ((ComboBox)Field("standaloneSautChoice")).SelectedIndex=3;((CheckBox)Field("vigilanceSoftwareInputs")).Checked=true;Released();
  Cab(0x3a0,1);Cab(0x3a4,1);Cab(0x3dc,0);Cab(0x3c4,0);Cab(0x3c8,0);Cab(0x3bc,1);Cab(0x3fc,1);Cab(0x3e0,1);
  phase="prepared_pantographs";Warm(10);Cab(0x3e0,0);Warm(290);Cab(0x3dc,1);phase="prepared_bv";Warm(150);Cab(0x3dc,0);Warm(10);Cab(0x3dc,1);Warm(200);Cab(0x3c4,1);Warm(160);Cab(0x3c8,0);Warm(10);Cab(0x3c8,1);Warm(50);
  Check("Prepared native head brake holds rest",Speed()<1,"<1 km/h",Speed());
  rows.Add(Row());
  phase="prepared_real_RBS_startup_ack";Held(1,true);Warm(10);Released();Warm(1);
  phase="prepared_physical_EPK_key_cycle";Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),0);
  for(int i=0;i<200&&(F(mem["TLocParam"],0x9f)<=3.5||Marshal.ReadByte(mem["TLocParam"],0x9b)!=0);i++)Step();
  Check("Actual EPK chamber recharged before key ON",F(mem["TLocParam"],0x9f)>3.5&&Marshal.ReadByte(mem["TLocParam"],0x9b)==0,">3.5/trip0",Row());
  Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),1);Warm(1);
  phase="prepared_RBS_new_key_ON_request";Held(1,true);Warm(10);Released();Warm(1);Warm(40);rows.Add(Row());
  Check("Prepared chamber genuinely charged",F(mem["TLocParam"],0x9f)>3,">3 bar",F(mem["TLocParam"],0x9f));
  Check("Prepared relay energized and no trip",Marshal.ReadByte(mem["TLocParam"],0x9a)==1&&Marshal.ReadByte(mem["TLocParam"],0x9b)==0,"coil1/trip0",Row());
 }
 static void AdapterGuards(){
  adapter=(TrainerNativeSafetyAdapter)Field("nativeSafetyAdapter");Check("Production exact adapter ready",adapter!=null&&adapter.Ready,true,adapter!=null&&adapter.Ready);ownedCode=adapter.CodePointer;
  Check("Host owns exact adapter buffers",mem["NativeSafetyEnvelope"]==adapter.Envelope&&mem["NativeSafetyInputOutput"]==adapter.InputOutput&&mem["NativeSafetyCode"]==adapter.CodePointer,true,true);
  MemoryInfo info;VirtualQuery(adapter.CodePointer,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)));Check("Adapter code immutable RX",(info.Protect&0xff)==0x20&&info.AllocationBase==adapter.CodePointer,"private RX allocation",new{protect=info.Protect,type=info.Type});
  Check("Actual software KLUB presence in input envelope",Marshal.ReadIntPtr(adapter.Envelope,0xc)==klubModule,klubModule.ToString("X"),Marshal.ReadIntPtr(adapter.Envelope,0xc).ToString("X"));
  foreach(int offset in new[]{0,4,8,0x10,0x14})Check("Canonical envelope input alias "+offset.ToString("X"),Marshal.ReadIntPtr(adapter.Envelope,0x14+offset)==Marshal.ReadIntPtr(mem["TPribBezAllTren"],offset),true,true);
  Check("Isolated output snapshot envelope",Marshal.ReadIntPtr(adapter.Envelope,0x20)==adapter.InputOutput&&adapter.InputOutput!=mem["TOutParamAllTRen"]&&adapter.InputOutput!=mem["StandaloneSautOutput"],true,true);
  int[] calls={0xd,0x25,0x37,0x49,0x5c,0x6d,0x81,0x92,0xa3,0xb4,0xcb,0xdc,0xf0,0x107,0x119,0x13a,0x151,0x176,0x194,0x1ae,0x1ca,0x1db,0x1ef,0x201,0x218,0x22b,0x23d,0x24f,0x263,0x277,0x289,0x29b};
  foreach(int offset in calls)if(Marshal.ReadByte(adapter.BodyPointer,offset)!=0xe8||IntPtr.Add(adapter.BodyPointer,offset+5+Marshal.ReadInt32(adapter.BodyPointer,offset+1))!=adapter.GetterPointer)throw new Exception("Relocated getter CALL guard "+offset);
  Check("All 32 real getter calls relocated",calls.Length==32,32,calls.Length);
  Check("All three constants relocated",Marshal.ReadIntPtr(adapter.BodyPointer,0x129)==adapter.ConstantsPointer&&Marshal.ReadIntPtr(adapter.BodyPointer,0x1a1)==IntPtr.Add(adapter.ConstantsPointer,4)&&Marshal.ReadIntPtr(adapter.BodyPointer,0x1bb)==IntPtr.Add(adapter.ConstantsPointer,8),true,true);
 }
 static void SnapshotFlags(string label){var common=mem["TOutParamAllTRen"];bool match=true;foreach(int offset in new[]{0x16,0x23,0x29,0x30})match&=Marshal.ReadByte(adapter.InputOutput,offset)==Marshal.ReadByte(common,offset);match&=Marshal.ReadByte(adapter.InputOutput,0xe)==Marshal.ReadByte(mem["StandaloneSautOutput"],0xe);Check(label+" actual firmware snapshot",match,true,Row());}
 static void Antiwedging(){
  phase="real_RB_held";Check("RB counter initial released",Marshal.ReadInt32(mem["CommOwner"],0x278)==0,0,Marshal.ReadInt32(mem["CommOwner"],0x278));
  Held(0,true);for(int i=0;i<70;i++)Step();Check("No warning through first 70 calls",Marshal.ReadByte(mem["TOutParamAllTRen"],0x30)==0,0,Row());Step();
  Check("71st actual Communication call raises antiwedging",Marshal.ReadByte(mem["TOutParamAllTRen"],0x30)==1&&Marshal.ReadInt32(mem["CommOwner"],0x278)==70,"flag1/counter70",Row());SnapshotFlags("RB antiwedging");
  Check("Native adapter removes coil for held RB",Marshal.ReadByte(mem["TLocParam"],0x9a)==0&&Marshal.ReadByte(mem["TLocParam"],0x9c)==1,"coil0/whistle1",Row());
  Check("Single warning cycle precedes physical trip",Marshal.ReadByte(mem["TLocParam"],0x9b)==0,0,Row());
  Held(0,false);phase="real_RB_release";Step();Check("Release clears warning without another press",Marshal.ReadByte(mem["TOutParamAllTRen"],0x30)==0&&Marshal.ReadInt32(mem["CommOwner"],0x278)==0,"flag0/counter0",Row());
  Check("Native coil recovers after release before trip",Marshal.ReadByte(mem["TLocParam"],0x9a)==1&&Marshal.ReadByte(mem["TLocParam"],0x9b)==0,"coil1/trip0",Row());antiwedgingAccepted=true;rows.Add(Row());
 }
 static void ReleaseForMotion(){phase="actual_KVT_release";NativeHandle(0x3b4,2);for(int i=0;i<1800;i++){Step();if(F(mem["TLocParam"],13)<.1&&D(mem["TLocParam"],0xec)<.1)break;}Check("Actual native brake release before traction",F(mem["TLocParam"],13)<.1&&D(mem["TLocParam"],0xec)<.1,"TC/pilot<.1",Row());}
 static void AcknowledgeKlub(){
  phase="native_KLUB_allowed_RBS";Check("Real firmware request exists before ACK",Marshal.ReadByte(mem["TOutParamAllTRen"],0x16)==1,1,Row());
  Check("ACK issued before native physical trip",Marshal.ReadByte(mem["TLocParam"],0x9b)==0,0,Row());SnapshotFlags("KLUB request");
  Held(1,true);for(int i=0;i<10;i++)Step();Held(1,false);Step();
  Check("Actual RBS clears native request and LED",Marshal.ReadByte(mem["TOutParamAllTRen"],0x16)==0&&Marshal.ReadByte(klubOwner,0x12c)==0,"request0/LED0",Row());
  Check("ACK recovers coil with no trip",Marshal.ReadByte(mem["TLocParam"],0x9a)==1&&Marshal.ReadByte(mem["TLocParam"],0x9b)==0,"coil1/trip0",Row());allowedAckAccepted=true;rows.Add(Row());
 }
 static void AcquireNativeMotionAndAck(){
  whiteScenario=true;phase="real_native_motion";Digit(5,"safety_explicit_auto_key5");
  for(int i=0;i<2500;i++){
   Step();if(Marshal.ReadByte(mem["TOutParamAllTRen"],0x16)==1)AcknowledgeKlub();
   if(allowedAckAccepted&&Speed()>=3&&F(mem["TResTyga"],0)>0){Check("Native traction and motion without assignment",forceObserved&&motionObserved,true,Row());return;}
   if(allowedAckAccepted&&!Decision().DriveEnabled){phase="explicit_rearm_after_operator_ACK";Digit(5,"safety_rearm_after_RBS");}
  }throw new Exception("Real native motion and firmware allowed ACK not reached");
 }
 static void MissedAck(){
  phase="native_KLUB_missed_ACK";Released();var loc=mem["TLocParam"];requestPressure=F(loc,0x9f);preTripTm=F(cars[0],0);preTripTc=F(loc,13);
  Check("Missed-ACK phase begins without request/trip",Marshal.ReadByte(mem["TOutParamAllTRen"],0x16)==0&&Marshal.ReadByte(loc,0x9b)==0,"request0/trip0",Row());
  for(int i=0;i<2000;i++){
   Step();minimumPressure=minimumPressure==0?F(loc,0x9f):Math.Min(minimumPressure,F(loc,0x9f));
   if(alarmCycle<0&&Marshal.ReadByte(klubOwner,0x12c)==1){alarmCycle=cycles;rows.Add(Row());}
   if(piskCycle<0&&Marshal.ReadByte(mem["TOutParamAllTRen"],0x16)==1){piskCycle=cycles;requestPressure=F(loc,0x9f);SnapshotFlags("Missed KLUB ACK");rows.Add(Row());}
   if(coilCycle<0&&Marshal.ReadByte(loc,0x9a)==0){coilCycle=cycles;rows.Add(Row());}
   if(Marshal.ReadByte(loc,0x9b)==1){tripCycle=cycles;rows.Add(Row());break;}
  }
  Check("Native White signal reached its firmware consumer",Marshal.ReadByte(klubOwner,0xef)==5&&Marshal.ReadByte(mem["TOutParamAllTRen"],0x20)==5,"native White5",Row());
  Check("Native periodic LED then request",alarmCycle>=0&&piskCycle>=alarmCycle&&Marshal.ReadInt32(klubOwner,0x11c)==70,"period70/LED then Pisk",new{alarmCycle,piskCycle,period=Marshal.ReadInt32(klubOwner,0x11c)});
  Check("Only missed KLUB request removes coil",piskCycle>=0&&coilCycle>=piskCycle&&Marshal.ReadByte(mem["TOutParamAllTRen"],0x30)==0&&Marshal.ReadByte(mem["StandaloneSautOutput"],0xe)==0,"KLUB request only",Row());
  Check("Actual native chamber pressure fell below factory threshold",requestPressure>1.5&&minimumPressure<1.5&&F(loc,0x9f)<1.5,">1.5 to <1.5 bar",new{requestPressure,minimumPressure,actual=F(loc,0x9f)});
  Check("Exact native factory code produced latched EPK trip",tripCycle>=coilCycle&&tripCycle>0&&Marshal.ReadByte(loc,0x9b)==1&&Marshal.ReadByte(loc,0x9a)==0,"trip1/coil0",new{coilCycle,tripCycle,actual=Row()});missedAckAccepted=true;
  phase="native_trip_physical_braking";for(int i=0;i<200;i++){Step();if(F(cars[0],0)<preTripTm-.2&&F(loc,13)>preTripTc+.2)break;}
  Check("Native trip physically reduces TM and raises TC",F(cars[0],0)<preTripTm-.2&&F(loc,13)>preTripTc+.2,"TM decreases / TC increases",new{beforeTm=preTripTm,beforeTc=preTripTc,actual=Row()});physicalBrakeAccepted=true;
  phase="ACK_does_not_clear_native_trip";Held(1,true);for(int i=0;i<10;i++)Step();Held(1,false);Step();
  Check("Button acknowledgement cannot erase actual trip latch",Marshal.ReadByte(loc,0x9b)==1&&Marshal.ReadByte(loc,0x9a)==0,"trip1/coil0",Row());rows.Add(Row());
 }
 static void Save(bool passed){var json=new JavaScriptSerializer{MaxJsonLength=8000000};File.WriteAllText(TrainerSession.ResultPath("Trainer_native_safety.json"),json.Serialize(new{
  passed=passed,error=failure,checks=checks,rows=rows,native_keys=keyRows,cycles=cycles,elapsed_seconds=watch.Elapsed.TotalSeconds,
  native_operator_antiwedging_accepted=antiwedgingAccepted,native_firmware_allowed_ack_accepted=allowedAckAccepted,native_missed_ack_pressure_trip_accepted=missedAckAccepted,native_physical_braking_accepted=physicalBrakeAccepted,
  actual_native_force_observed=forceObserved,actual_native_motion_observed=motionObserved,input_guards_restored=restored,adapter_own_allocations_released=adapterDetached,native_closed=closed,
  stage_cycles=new{alarmCycle,piskCycle,coilCycle,tripCycle},native_pressure=new{requestPressure,minimumPressure},
  factory=new{exe_sha=TrainerNativeSafetyAdapter.MainSha256,method_rva="330EDC",method_bytes=687,method_sha=TrainerNativeSafetyAdapter.BodySha256,getter_sha=TrainerNativeSafetyAdapter.GetterSha256,constant_sha=TrainerNativeSafetyAdapter.ConstantsSha256},
  scope="Prepared-air controlled native integration; actual cab/GUI operator inputs, real traction and native physics. Scoped CodeWay5 White source goes only to KLUB; no live route markup/occupancy claim. No managed protection/force/speed/pressure/contactor output writes, initial-speed assignment, AutoACK production or full factory/RE137+ certification."}));}
 [STAThread]static int Main(){Application.EnableVisualStyles();watch.Start();int exit=1;
  try{
   Check("x86 safety ABI",IntPtr.Size==4,4,IntPtr.Size);if(File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")))throw new Exception("Command file present");
   host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);var handle=host.Handle;Call("StartNative");var timer=(Timer)Field("timer");if(timer==null)throw new Exception("Host failed native startup");timer.Stop();
   mem=(Dictionary<string,IntPtr>)Field("mem");cars=(IntPtr[])Field("cars");NoDiagnosticInputs();Check("Production AVP ready for physical fixture",(bool)Field("avpInitialized")&&Ui()!=null,true,Field("avpUnavailable"));
   klubModule=GetModuleHandle("KLUB_U_LCD_XE2.dll");klubSlot=Marshal.ReadIntPtr(klubModule,0x1d7028);klubOwner=Marshal.ReadIntPtr(klubSlot);Check("Native KLUB owner",Readable(klubOwner,0x132)&&Name(klubOwner)=="TProceedKLUB","TProceedKLUB",Name(klubOwner));
   oldVigilance=((CheckBox)Field("vigilanceSoftwareInputs")).Checked;oldPermission=((CheckBox)Field("avpMovementPermission")).Checked;oldSoftwareTd=((CheckBox)Field("avpSoftwareTd")).Checked;oldTd1=((CheckBox)Field("avpTd1")).Checked;oldTd2=((CheckBox)Field("avpTd2")).Checked;oldTarget=((NumericUpDown)Field("avpTargetLimit")).Value;oldMonitor=((ComboBox)Field("monitorCombo")).SelectedIndex;oldSautChoice=((ComboBox)Field("standaloneSautChoice")).SelectedIndex;uiSaved=true;
   AdapterGuards();Setup();Antiwedging();ReleaseForMotion();Settings();AcquireNativeMotionAndAck();MissedAck();
   Check("All mandatory native safety phases",antiwedgingAccepted&&allowedAckAccepted&&missedAckAccepted&&physicalBrakeAccepted,true,true);exit=0;
  }catch(Exception error){failure=error.ToString();Console.WriteLine(error);}
  finally{
   if(host!=null){
    try{
     if(mem!=null){whiteScenario=false;Released();Call("CloseAutovedenie");
      foreach(var change in cabChanges)Marshal.WriteByte(change.Key,change.Value);
      foreach(var change in cabChanges)Check("Cab input restored "+change.Key.ToString("X"),Marshal.ReadByte(change.Key)==change.Value,change.Value,Marshal.ReadByte(change.Key));
      if(nativeInputsSet){Position(Marshal.ReadIntPtr(cabForm,0x3b0),oldKm);Position(Marshal.ReadIntPtr(cabForm,0x3b4),oldKvt);Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),oldEpk);Position(Marshal.ReadIntPtr(tumbaForm,0x3a4),oldVcu);Check("Actual native handle sources restored",Marshal.ReadByte(mem["TLocParam"],0x7d)==oldKm&&Marshal.ReadByte(mem["TLocParam"],0x7e)==oldKvt&&Marshal.ReadByte(mem["TLocParam"],0x99)==oldEpk&&Marshal.ReadByte(mem["TLocParam"],0x8f)==oldVcu,"original KM/KVT/EPK/VCU",true);}
      if(uiSaved){((CheckBox)Field("vigilanceSoftwareInputs")).Checked=oldVigilance;((CheckBox)Field("avpMovementPermission")).Checked=oldPermission;((CheckBox)Field("avpSoftwareTd")).Checked=oldSoftwareTd;((CheckBox)Field("avpTd1")).Checked=oldTd1;((CheckBox)Field("avpTd2")).Checked=oldTd2;((NumericUpDown)Field("avpTargetLimit")).Value=oldTarget;((ComboBox)Field("monitorCombo")).SelectedIndex=oldMonitor;((ComboBox)Field("standaloneSautChoice")).SelectedIndex=oldSautChoice;}
      Call("RestoreVigilanceOperatorInputs");restored=true;Call("CloseNativeSafetyAdapter");MemoryInfo info;VirtualQuery(ownedCode,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)));adapterDetached=Field("nativeSafetyAdapter")==null&&info.State!=0x1000;
      Check("Owned native RX allocation released",adapterDetached,true,info.State);
     }
    }catch(Exception error){exit=1;failure=(failure??"")+"\nRESTORE: "+error;Console.WriteLine(error);}
    try{Save(false);}catch(Exception error){Console.WriteLine("Pre-close diagnostics: "+error.Message);}
    try{host.Close();host.Dispose();closed=true;}catch(Exception error){exit=1;failure=(failure??"")+"\nCLOSE: "+error;Console.WriteLine(error);}
   }
   try{Save(exit==0);}catch(Exception error){exit=1;Console.WriteLine(error);}
  }
  Console.WriteLine(exit==0?"PASS exact native safety request / acknowledgement / pressure trip":"FAIL native safety integration");return exit;
 }
}
