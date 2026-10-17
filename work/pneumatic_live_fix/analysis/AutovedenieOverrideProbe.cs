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

// Dedicated prepared production-host override scenario, not complete AVP acceptance.
// No fixture provider, speed/force/protection-output writes or initial-motion assignment.
// Manual handles use the original Communication SetPosition/event. TD remains the
// explicit default-off production software operator source selected through its UI.
class AutovedenieOverrideProbe {
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void NativeCall();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void AddRecord(IntPtr record);
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module,StringBuilder path,int size);
 [DllImport("kernel32.dll",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module,string name);
 [DllImport("kernel32.dll")] static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint kind,uint protection);
 [DllImport("kernel32.dll")] static extern bool VirtualFree(IntPtr address,UIntPtr size,uint kind);
 [DllImport("kernel32.dll")] static extern bool FlushInstructionCache(IntPtr process,IntPtr address,UIntPtr size);
 [DllImport("kernel32.dll")] static extern IntPtr GetCurrentProcess();
 [StructLayout(LayoutKind.Sequential)] struct MemoryInfo { public IntPtr Base,AllocationBase;public uint AllocationProtect;public UIntPtr RegionSize;public uint State,Protect,Type; }
 [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr p,out MemoryInfo info,UIntPtr length);
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=52)] struct SautLimit {
  public int Start,End,Pass,Freight,Suburban,Special;public byte Flag,Klub,Saut,StartFlag;
  public int Distance,Allowed,Target,AllowedSaut,TargetSaut;public byte SautOS;
 }
 static readonly BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static TrainerIntegrationHost host;static Dictionary<string,IntPtr> mem;static IntPtr[] cars;
 static IntPtr communication,cabForm,tumbaForm,provider,originalPult,saut,sautOwner;
 static readonly Dictionary<IntPtr,byte> cabChanges=new Dictionary<IntPtr,byte>();
 static readonly Dictionary<int,IntPtr> cabPointers=new Dictionary<int,IntPtr>();
 static readonly Dictionary<int,byte> pbOld=new Dictionary<int,byte>(),pbAssigned=new Dictionary<int,byte>();
 static readonly List<IntPtr> sautRecords=new List<IntPtr>();
 static readonly List<object> checks=new List<object>(),rows=new List<object>(),keyRows=new List<object>();
 static RouteModel routeSnapshot;static bool nativeInputsSet,sautDataChanged,sautRestored,restored;
 static byte oldEpk,oldVcu,oldKm,oldKvt;static string phase="initial",failure;
 static int cycles,acks,ackRemaining;static bool previousPisk,forceObserved,motionObserved;
 static bool permissionAccepted,km3Accepted,km5Accepted,kvtAccepted,epkAccepted,sautAccepted,epkRearmed,epkProtectedRefusal;
 static bool oldPermission,oldSoftwareTd,oldTd1,oldTd2,uiSaved;static decimal oldTarget;static int oldMonitor,oldSautChoice;
 static bool scenarioCodeActive; static byte savedScenarioCode,scenarioCodeValue=3;static int scenarioSignalX,scenarioLastSignalX;
 static Stopwatch watch=new Stopwatch();
 static object Field(string name){var f=typeof(TrainerIntegrationHost).GetField(name,flags);if(f==null)throw new Exception("Missing host field "+name);return f.GetValue(host);}
 static void SetField(string name,object value){typeof(TrainerIntegrationHost).GetField(name,flags).SetValue(host,value);}
 static void Call(string name,params object[] args){var method=typeof(TrainerIntegrationHost).GetMethod(name,flags);if(method==null)throw new Exception("Missing host method "+name);try{method.Invoke(host,args);}catch(TargetInvocationException e){throw new Exception(name+": "+e.InnerException,e.InnerException);}}
 static float F(IntPtr p,int offset){var b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
 static string Hash(string file){using(var hash=SHA256.Create())using(var stream=File.OpenRead(file))return BitConverter.ToString(hash.ComputeHash(stream)).Replace("-","");}
 static bool Readable(IntPtr p,int count){MemoryInfo info;if(p==IntPtr.Zero||VirtualQuery(p,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)return false;ulong a=unchecked((uint)p.ToInt32()),b=unchecked((uint)info.Base.ToInt32());return info.State==0x1000&&(info.Protect&0x101)==0&&a>=b&&a+(uint)count<=b+info.RegionSize.ToUInt32();}
 static string Name(IntPtr p){if(!Readable(p,4))throw new Exception("Object pointer guard");var vmt=Marshal.ReadIntPtr(p);if(!Readable(IntPtr.Add(vmt,-56),60))throw new Exception("VMT pointer guard");var n=Marshal.ReadIntPtr(vmt,-56);if(!Readable(n,1))throw new Exception("Class-name pointer guard");int length=Marshal.ReadByte(n);if(length<2||length>100||!Readable(n,length+1))throw new Exception("Class-name length guard");var bytes=new byte[length];Marshal.Copy(IntPtr.Add(n,1),bytes,0,length);return Encoding.ASCII.GetString(bytes);}
 static void Check(string name,bool pass,object expected,object actual){checks.Add(new{name=name,passed=pass,expected=expected,actual=actual});if(!pass)throw new Exception(name+" expected="+expected+" actual="+actual);}
 static void GuardCode(int rva,string expected){var b=new byte[expected.Length/2];Marshal.Copy(IntPtr.Add(communication,rva),b,0,b.Length);string actual=BitConverter.ToString(b).Replace("-","");Check("Native UI code "+rva.ToString("x"),actual==expected,expected,actual);}
 static void Invoke(List<byte> code){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)128,0x3000,0x40);if(p==IntPtr.Zero)throw new Exception("Input thunk allocation");try{Marshal.Copy(code.ToArray(),0,p,code.Count);if(!FlushInstructionCache(GetCurrentProcess(),p,(UIntPtr)code.Count))throw new Exception("Input thunk flush");((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{if(!VirtualFree(p,UIntPtr.Zero,0x8000))throw new Exception("Input thunk free");}}
 static void Register(IntPtr self,int value,int rva){var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(self.ToInt32()));code.Add(0xba);code.AddRange(BitConverter.GetBytes(value));code.Add(0xb9);code.AddRange(BitConverter.GetBytes(IntPtr.Add(communication,rva).ToInt32()));code.AddRange(new byte[]{0xff,0xd1,0xc3});Invoke(code);}
 static void Position(IntPtr control,double value){Check("Native position control",Name(control)=="TMultiPosTumbler","TMultiPosTumbler",Name(control));var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(control.ToInt32()));var d=BitConverter.GetBytes(value);code.Add(0x68);code.AddRange(new byte[]{d[4],d[5],d[6],d[7]});code.Add(0x68);code.AddRange(new byte[]{d[0],d[1],d[2],d[3]});code.AddRange(new byte[]{0x6a,0,0x6a,0});code.Add(0xb9);code.AddRange(BitConverter.GetBytes(IntPtr.Add(communication,0x2a4944).ToInt32()));code.AddRange(new byte[]{0xff,0xd1,0xc3});Invoke(code);}
 static void Cab(int offset,byte value){IntPtr p;if(!cabPointers.TryGetValue(offset,out p)){var control=Marshal.ReadIntPtr(cabForm,offset);string expectedClass=offset==0x3bc?"TTumbler3Pos":"TTumbler";Check("Physical cab control class "+offset.ToString("x"),Name(control)==expectedClass,expectedClass,Name(control));p=IntPtr.Add(control,0x1d9);Check("Physical cab input "+offset.ToString("x"),Readable(p,1),true,Readable(p,1));cabPointers[offset]=p;cabChanges[p]=Marshal.ReadByte(p);}Marshal.WriteByte(p,value);}
 static TrainerAutovedenieInterface Ui(){return (TrainerAutovedenieInterface)Field("avpUi");}
 static TrainerAvpDecision Decision(){return (TrainerAvpDecision)Field("avpDecision");}
 static int WindowType(){var ms=(IntPtr[])typeof(TrainerAutovedenieInterface).GetField("monitors",flags).GetValue(Ui());return Marshal.ReadByte(ms[0],0x14);}
 static double X(){return Convert.ToDouble(Field("position"));}
 static double Speed(){return F(mem["TInputParamPult"],1);}

 static double D(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
 static TrainerAutovedenieNativeBridge Bridge(){return (TrainerAutovedenieNativeBridge)Field("avpNative");}
 static void Module(IntPtr module,string relative,string sha){
  Check("Module present "+relative,module!=IntPtr.Zero,true,module.ToString("X"));
  var path=new StringBuilder(32768);uint count=GetModuleFileName(module,path,path.Capacity);
  string expected=Path.GetFullPath(Path.Combine((string)Field("root"),relative));
  Check("Loaded module path "+relative,count>0&&count<path.Capacity&&String.Equals(Path.GetFullPath(path.ToString()),expected,StringComparison.OrdinalIgnoreCase),expected,path.ToString());
  Check("Loaded module SHA "+relative,Hash(path.ToString())==sha,sha,Hash(path.ToString()));
 }
 static void NoDiagnosticInputs(){
  if(File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")))throw new Exception("Override probe requires no command file");
  if(Field("commandedKm")!=null||Field("fileKm")!=null||(bool)Field("safetyPower")||(bool)Field("momentaryCharge"))throw new Exception("Diagnostic KM/safety/charging override is active");
 }
 static object ReadTmProtection(){var module=GetModuleHandle("TormModel.dll");if(module==IntPtr.Zero)throw new Exception("Native TM module absent");var math=Marshal.ReadIntPtr(module,0x1ac024);if(!Readable(IntPtr.Add(math,0x7c),0x28))throw new Exception("Native TM chamber diagnostic guard");return new{computed_7c=D(math,0x7c),computed_9c=D(math,0x9c),signal_latch=Marshal.ReadByte(mem["TLocParam"],0xf4),fault_byte=Marshal.ReadByte(mem["TLocParam"],0x103)};}
 static object SautSnapshot(){if(sautOwner==IntPtr.Zero)return null;return new{input_code=Marshal.ReadByte(sautOwner,0x150),native_mode=Marshal.ReadByte(sautOwner,0x151),native_fop=Marshal.ReadByte(sautOwner,0x1c7),native_ramp=Marshal.ReadInt32(sautOwner,0x188),epk_seen=Marshal.ReadByte(sautOwner,0x143),pb6_seen=Marshal.ReadByte(sautOwner,0x145),ceilings=new[]{Marshal.ReadInt32(sautOwner,0x190),Marshal.ReadInt32(sautOwner,0x194),Marshal.ReadInt32(sautOwner,0x198),Marshal.ReadInt32(sautOwner,0x19c),Marshal.ReadInt32(sautOwner,0x1a8)}};}
 static object Row(){var d=Decision();var b=Bridge();var l=mem["TLocParam"];return new{
  phase=phase,cycles=cycles,x_m=X(),v_kmh=Speed(),native_force=F(mem["TResTyga"],0),native_rk=Marshal.ReadInt32(mem["TTygaParam"],0xc),
  state=d==null?"null":d.State.ToString(),enabled=d!=null&&d.DriveEnabled,reason=d==null?"":d.Reason,
  physical_km=Marshal.ReadByte(l,0x7d),physical_kvt=Marshal.ReadByte(l,0x7e),physical_epk_key=Marshal.ReadByte(l,0x99),vcu=Marshal.ReadByte(l,0x8f),blocked367=Marshal.ReadByte(l,0x90),combined_cock=Marshal.ReadByte(l,0x91),kvt_limit=Marshal.ReadByte(l,0x93),
  epk_trip=Marshal.ReadByte(l,0x9b),kon_trip=Marshal.ReadByte(l,0x9d),saut_enabled=Marshal.ReadByte(l,0x89),
  saut_brake=Marshal.ReadByte(l,0x8a),saut_hold=Marshal.ReadByte(l,0x8b),saut_cut=Marshal.ReadByte(mem["TOutParamAllTRen"],0xf),saut_zapret=Marshal.ReadByte(mem["TOutParamAllTRen"],0),saut_limit=Marshal.ReadInt32(mem["TOutParamAllTRen"],0xa),saut_captured_limit=((TrainerStandaloneSautBinding)Field("standaloneSautBinding")).EffectiveLimitKmh,saut_input_code=Marshal.ReadByte(mem["TInputParamPult"],0x1e),saut_input_alsn=Marshal.ReadByte(mem["TInputParamPult"],0x1d),
  native_pb_inputs=new[]{Marshal.ReadByte(mem["TPBBtn"],5),Marshal.ReadByte(mem["TPBBtn"],6)},saut_operator_scope=pbAssigned.Count>0,native_saut_snapshot=SautSnapshot(),native_tc=F(l,13),head_tc=F(cars[0],4),independent_pilot=D(l,0xec),tm=F(cars[0],0),
  physical_controller=Marshal.ReadInt32(mem["TPult"],0x69),physical_coast=Marshal.ReadByte(mem["TPult"],0x26),
  bridge_owns=b!=null&&b.OwnsCommands,bridge_fault=b==null?"":b.Fault,bridge_ready=b==null?"lazy":b.ReadyReason,
  native_command=Marshal.ReadByte(mem["TTygaParam"],0x14),native_excitation_type=Marshal.ReadByte(mem["TTygaParam"],0x16),native_rec_position=Marshal.ReadInt32(mem["TTygaParam"],0x10),native_ia=F(mem["TTygaParam"],0x5b),native_iv=F(mem["TTygaParam"],0x9b),native_traction_permissions=new[]{Marshal.ReadByte(mem["TTygaParam"],0x1f),Marshal.ReadByte(mem["TTygaParam"],0x20),Marshal.ReadByte(mem["TTygaParam"],0x21),Marshal.ReadByte(mem["TTygaParam"],0x22)},native_td_groups=new[]{Marshal.ReadInt32(mem["TTygaParam"],0x10d),Marshal.ReadInt32(mem["TTygaParam"],0x111),Marshal.ReadInt32(mem["TTygaParam"],0x115),Marshal.ReadInt32(mem["TTygaParam"],0x119)},schema_traction=new[]{Marshal.ReadByte(Ui().NativeSchema,0xee),Marshal.ReadByte(Ui().NativeSchema,0xef),Marshal.ReadByte(Ui().NativeSchema,0xf0),Marshal.ReadByte(Ui().NativeSchema,0xf1)},tm_interlock=Marshal.ReadByte(mem["TLocParam"],0x11),tm_protection=ReadTmProtection(),permission=((CheckBox)Field("avpMovementPermission")).Checked,klub_ack_count=acks,electrical_faults=Convert.ToInt32(Field("electricalFaults"))};}
 static void Step(bool dispatch=false){
  if(watch.Elapsed.TotalSeconds>300||cycles>22000)throw new Exception("Override scenario bound reached");NoDiagnosticInputs();
  bool pisk=Marshal.ReadByte(mem["TOutParamAllTRen"],0x16)!=0||Marshal.ReadByte(mem["StandaloneSautOutput"],0xe)!=0;if(pisk&&!previousPisk){ackRemaining=10;acks++;}previousPisk=pisk;
  Cab(0x50c,(byte)(ackRemaining>0?1:0));if(ackRemaining>0)ackRemaining--;
  Call("PollCommands");Call("BeforeAutovedenieCycle");Call("ElectricalCycle");Call("AfterAutovedenieElectricalCycle");
  Call("ApplyTestInputs");Call("PneumaticCycle");Call("MotionCycle");Call("RouteInputCycle");
   // Explicit, declared test signal sensor input; native CodeWay3 maps to
   // mode3 in this SHA. It is not a live ALSN source or a safety output.
   var light=mem["TCurLighterRec"];
   if(Marshal.ReadIntPtr(mem["TInputParamPult"],0x1f)!=light || !Readable(light,59))throw new Exception("Declared signal input pointer guard");
   byte savedCode=Marshal.ReadByte(mem["TInputParamPult"],0x1e),savedSend=Marshal.ReadByte(light,0x21);
   int savedIndex=Marshal.ReadInt32(light,0),savedX=Marshal.ReadInt32(light,0x19),savedLast=Marshal.ReadInt32(light,0x1d);
   try {
   if(scenarioCodeActive){Marshal.WriteByte(mem["TInputParamPult"],0x1e,scenarioCodeValue);Marshal.WriteInt32(light,0,1);Marshal.WriteInt32(light,0x19,scenarioSignalX);Marshal.WriteInt32(light,0x1d,scenarioLastSignalX);Marshal.WriteByte(light,0x21,1);}
   // Explicit fixture operator switches: Communication may project hardware
   // defaults each cycle. Borrow only confirmed PB input bytes for native Aux.
   if(pbAssigned.Count>0){var saved=new Dictionary<int,byte>();try{foreach(var input in pbAssigned){saved[input.Key]=Marshal.ReadByte(mem["TPBBtn"],input.Key);Marshal.WriteByte(mem["TPBBtn"],input.Key,input.Value);}Call("AuxCycle");}finally{foreach(var input in pbAssigned)if(Marshal.ReadByte(mem["TPBBtn"],input.Key)==input.Value)Marshal.WriteByte(mem["TPBBtn"],input.Key,saved[input.Key]);}}else Call("AuxCycle");
   } finally {if(scenarioCodeActive){
    if(Marshal.ReadByte(mem["TInputParamPult"],0x1e)==scenarioCodeValue)Marshal.WriteByte(mem["TInputParamPult"],0x1e,savedCode);
    if(Marshal.ReadInt32(light,0)==1)Marshal.WriteInt32(light,0,savedIndex);
    if(Marshal.ReadInt32(light,0x19)==scenarioSignalX)Marshal.WriteInt32(light,0x19,savedX);
    if(Marshal.ReadInt32(light,0x1d)==scenarioLastSignalX)Marshal.WriteInt32(light,0x1d,savedLast);
    if(Marshal.ReadByte(light,0x21)==1)Marshal.WriteByte(light,0x21,savedSend);
   }}
   Call("KlubCoordinateCycle");
  ((Delegate)Field("klubStep")).DynamicInvoke();SetField("ticks",Convert.ToInt32(Field("ticks"))+1);cycles++;
  if(dispatch||cycles%10==0)Application.DoEvents();
  if(F(mem["TResTyga"],0)>0)forceObserved=true;if(Speed()>.1)motionObserved=true;
  if(Convert.ToInt32(Field("electricalFaults"))!=0)throw new Exception("Native electrical fault");
  if(Bridge()!=null&&Bridge().Fault.Length!=0)throw new Exception("Production native bridge fault: "+Bridge().Fault);
  if((bool)Field("routeFinished"))throw new Exception("Override scenario unexpectedly reached route boundary");
  if(cycles%50==0)rows.Add(Row());
 }
 static void Warm(int n){for(int i=0;i<n;i++)Step();rows.Add(Row());}
 static void Dormant(string label,int count){
  for(int i=0;i<count;i++){Step();if(Decision().DriveEnabled||Decision().State==TrainerAvpState.Automatic||Bridge()!=null&&Bridge().OwnsCommands)throw new Exception(label+": automatic ownership restarted without a key");}
  Check(label+" stays cancelled without key",!Decision().DriveEnabled&&Decision().State!=TrainerAvpState.Automatic,true,new{Decision().State,Decision().Reason});rows.Add(Row());
 }
 static void Arm(string label){
  phase=label+"_explicit_key5";Digit(5,label+"_key5");
  Check(label+" explicit key5 enters Auto",Decision().State==TrainerAvpState.Automatic&&Decision().DriveEnabled,true,new{Decision().State,Decision().Reason});
  Check(label+" owns guarded production callback",Bridge()!=null&&Bridge().OwnsCommands,true,Bridge()==null?"null":Bridge().Fault);
 }
 static void Powered(string label,double minimumSpeed){
  Arm(label);for(int i=0;i<1800;i++){
   Step();if(!Decision().DriveEnabled)throw new Exception(label+": unexpected cancellation "+Decision().Reason);
   if(Speed()>=minimumSpeed&&F(mem["TResTyga"],0)>0){Check(label+" real native force and motion",true,true,Row());rows.Add(Row());return;}
  }throw new Exception(label+": native force/speed threshold not reached");
 }
 static void Cancelled(string label){
  for(int i=0;i<5;i++){Step(true);if(!Decision().DriveEnabled&&Decision().State!=TrainerAvpState.Automatic)break;}
  Check(label+" cancels host Auto",!Decision().DriveEnabled&&Decision().State==TrainerAvpState.Cancelled,true,new{Decision().State,Decision().Reason});
  Check(label+" relinquishes bridge",Bridge()!=null&&!Bridge().OwnsCommands,true,Bridge()==null?"null":Bridge().OwnsCommands.ToString());rows.Add(Row());
 }
 static void Handle(int field,double position,int nativeOffset,string label){
  var control=Marshal.ReadIntPtr(cabForm,field);Position(control,position);
  Check(label+" native handle input",Marshal.ReadByte(mem["TLocParam"],nativeOffset)==(byte)position,(byte)position,Marshal.ReadByte(mem["TLocParam"],nativeOffset));
 }
 static void NeutralAndRelease(string label){
  Handle(0x3b0,2,0x7d,label+"_KMII");Handle(0x3b4,2,0x7e,label+"_KVTII");Dormant(label+" restored handles",25);
  // Let the actual pneumatic model release; no pressure or protection state is assigned.
  for(int i=0;i<600;i++){Step();if(F(mem["TLocParam"],13)<.1&&D(mem["TLocParam"],0xec)<.1)break;}
  Check(label+" actual native independent release",F(mem["TLocParam"],13)<.1&&D(mem["TLocParam"],0xec)<.1,"native TC and pilot<.1",Row());
  Check(label+" release without auto restart",!Decision().DriveEnabled,true,Decision().State.ToString());
 }
 static void Permission(){
  Powered("permission",1);phase="permission_off";((CheckBox)Field("avpMovementPermission")).Checked=false;Cancelled("Permission off");
  for(int i=0;i<12&&Math.Abs(F(mem["TResTyga"],0))>.01;i++)Step();
  Check("Permission loss produces native coast",Math.Abs(F(mem["TResTyga"],0))<=.01,"native force <=.01",F(mem["TResTyga"],0));
  Dormant("Permission absent",20);phase="permission_restore";((CheckBox)Field("avpMovementPermission")).Checked=true;Dormant("Permission recovered",25);
  Arm("permission_rearm");permissionAccepted=true;
 }
 static void ManualKm(int position,string label){
  if(!Decision().DriveEnabled)Powered(label,1);else Warm(10);int rk=Marshal.ReadInt32(mem["TTygaParam"],0xc);float initial=F(mem["TLocParam"],13);double urStart=F(mem["TLocParam"],9);
  phase=label+"_physical_handle";Handle(0x3b0,position,0x7d,label);Cancelled(label);Dormant(label+" held",25);
  Check(label+" preserves physical handle",Marshal.ReadByte(mem["TLocParam"],0x7d)==position,position,Marshal.ReadByte(mem["TLocParam"],0x7d));
  Check(label+" has no further automatic RK increase",Marshal.ReadInt32(mem["TTygaParam"],0xc)<=rk,"<= "+rk,Marshal.ReadInt32(mem["TTygaParam"],0xc));
  if(position==5){float max=F(mem["TLocParam"],13);for(int i=0;i<300&&max<initial+.05;i++){Step();max=Math.Max(max,F(mem["TLocParam"],13));}
   Check("KM V causes native cylinder response",max>initial+.05,"> initial+.05",new{initial,max});
   phase="manual_V_documented_UR_step";for(int i=0;i<200&&urStart-F(mem["TLocParam"],9)<.7;i++)Step();
   Check("Manual V reaches measured first UR step",urStart-F(mem["TLocParam"],9)>=.7,">=.7",urStart-F(mem["TLocParam"],9));
   Handle(0x3b0,4,0x7d,"manual_IV_feed_lap");phase="manual_IV_documented_lap";Dormant("Manual IV feed lap",60);rows.Add(Row());km5Accepted=true;
  }else km3Accepted=true;
  NeutralAndRelease(label);Arm(label+"_rearm");
 }
 static void ManualKvt(){
  if(!Decision().DriveEnabled)Powered("KVT",1);double initial=D(mem["TLocParam"],0xec);
  phase="KVTIII_physical_handle";Handle(0x3b4,3,0x7e,"KVT III");Cancelled("KVT III");Dormant("KVT III held",20);
  double max=D(mem["TLocParam"],0xec);for(int i=0;i<300&&max<initial+.05;i++){Step();max=Math.Max(max,D(mem["TLocParam"],0xec));}
  Check("KVT III native independent pressure",max>initial+.05,"> initial+.05",new{initial,max});
  Check("KVT III preserves physical handle",Marshal.ReadByte(mem["TLocParam"],0x7e)==3,3,Marshal.ReadByte(mem["TLocParam"],0x7e));
  NeutralAndRelease("KVT");Arm("KVT_rearm");kvtAccepted=true;
 }
 static void Epk(){
  if(!Decision().DriveEnabled)Arm("EPK");phase="EPK_key_off";Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),0);
  Check("EPK physical key off",Marshal.ReadByte(mem["TLocParam"],0x99)==0,0,Marshal.ReadByte(mem["TLocParam"],0x99));Cancelled("EPK key off");Dormant("EPK key off held",25);
  phase="EPK_key_restore";Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),1);
  Check("EPK physical key restored",Marshal.ReadByte(mem["TLocParam"],0x99)==1,1,Marshal.ReadByte(mem["TLocParam"],0x99));Dormant("EPK key recovered",40);
  // If the real native EPK trip remains active, explicit rearm must refuse;
  // this probe never clears loc+9B. Recovery must then use a verified physical procedure.
  string ready=Bridge().ReadyReason;
  if(ready.Length==0){Arm("EPK_rearm");epkRearmed=true;}
  else{phase="EPK_explicit_key_refused_while_protection_active";Digit(5,"EPK_rearm_with_active_protection");
   Check("EPK active protection refuses explicit rearm",!Decision().DriveEnabled&&Decision().State!=TrainerAvpState.Automatic,true,new{Decision().State,Decision().Reason});
   Check("EPK refusal has actual native protection",Marshal.ReadByte(mem["TLocParam"],0x9b)!=0||Marshal.ReadByte(mem["TLocParam"],0x9d)!=0,true,new{epk=Marshal.ReadByte(mem["TLocParam"],0x9b),kon=Marshal.ReadByte(mem["TLocParam"],0x9d),ready});epkProtectedRefusal=true;}
  epkAccepted=true;
 }
 static void Key(byte code,string label){Call("SendMpsudKey",(byte)0);Step(true);Call("SendMpsudKey",code);Step(true);Call("SendMpsudKey",(byte)0);Step(true);var s=Ui().ReadSettings();keyRows.Add(new{label=label,code=code,window=WindowType(),selected=s.SelectedField,editing=s.Editing,mode=s.Mode,intensity=s.Intensity,train=s.TrainNumber,mass=s.MassT,wagons=s.Wagons,empty=s.Empty,valid=s.Valid,reason=s.Reason});}
 static void Digit(int digit,string label){Key((byte)(16-digit),label);}
 static void MoveField(int field){for(int n=0;n<8&&Ui().ReadSettings().SelectedField!=field;n++){int current=Ui().ReadSettings().SelectedField;Key((byte)(current<field?5:6),"select_field_"+field);}Check("SC09 selected field "+field,Ui().ReadSettings().SelectedField==field,field,Ui().ReadSettings().SelectedField);}
 static void Number(string value,string label){foreach(char c in value)Digit(c-'0',label+"_"+c);Key(1,label+"_Enter");}
 // Current MSUL GetWindowType RVA1f0663 dispatches mode keys14/15/16 to Mode0/1/2.
 // Mode selection uses its native enum keys, independently of the numeric digit decoder.
 static void Settings(){phase="native_settings";Check("Initial native monitor clock",WindowType()==1,1,WindowType());Digit(2,"Clock_to_Mash");Check("Native driver screen",WindowType()==2,2,WindowType());Digit(3,"Mash_to_AV_settings");Check("Native AV settings",WindowType()==10,10,WindowType());MoveField(0);Key(14,"ordinary_mode_native_key14");Check("Native ordinary mode selected",Ui().ReadSettings().Mode==0,0,Ui().ReadSettings().Mode);MoveField(1);Digit(2,"intensity_2");MoveField(2);Number("123","train");MoveField(3);Number("6000","mass");MoveField(4);Number("75","wagons");MoveField(5);Digit(0,"loaded_train");Key(1,"settings_finish_Enter");Check("Return to native driver screen",WindowType()==2,2,WindowType());var s=Ui().ReadSettings();Check("Native settings valid",s.Valid,true,s.Reason);Check("Ordinary mode and declared train",s.Mode==0&&s.Intensity==2&&s.TrainNumber==123&&s.MassT==6000&&s.Wagons==75&&!s.Empty,"mode0/intensity2/train123/6000t/75loaded",new{s.Mode,s.Intensity,s.TrainNumber,s.MassT,s.Wagons,s.Empty});}
 static void Setup(){((ComboBox)Field("standaloneSautChoice")).SelectedIndex=3;communication=GetModuleHandle("Communication1.dll");Module(communication,@"RESOURCES\LOCOMOTIVES\1\Communication1.dll","27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F");Module(GetModuleHandle("Pult1.dll"),@"RESOURCES\LOCOMOTIVES\1\Pult1.dll","7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5");var folder=(string)Field("root");Check("Communication SHA",Hash(Path.Combine(folder,@"RESOURCES\LOCOMOTIVES\1\Communication1.dll"))=="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F","verified",Hash(Path.Combine(folder,@"RESOURCES\LOCOMOTIVES\1\Communication1.dll")));Check("Pult SHA",Hash(Path.Combine(folder,@"RESOURCES\LOCOMOTIVES\1\Pult1.dll"))=="7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5","verified",Hash(Path.Combine(folder,@"RESOURCES\LOCOMOTIVES\1\Pult1.dll")));var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);Check("Cab class",Name(cab)=="TPultMash2ES6","TPultMash2ES6",Name(cab));cabForm=Marshal.ReadIntPtr(cab,0xc);Check("Cab form class",Name(cabForm)=="TPultMashIndexForm","TPultMashIndexForm",Name(cabForm));Check("Cab shared Pult",Marshal.ReadIntPtr(cabForm,0x530)==mem["TPult"],true,Marshal.ReadIntPtr(cabForm,0x530)==mem["TPult"]);provider=Marshal.ReadIntPtr(mem["CommOwner"],0x264);Check("Pult provider class",Name(provider)=="TPultDLLClass","TPultDLLClass",Name(provider));originalPult=Marshal.ReadIntPtr(provider,8);Check("Original Pult export",originalPult==GetProcAddress(GetModuleHandle("Pult1.dll"),"ProceedPult"),true,originalPult.ToString("X"));GuardCode(0x2ae994,"558BEC83C4F08855");GuardCode(0x2a4944,"558BEC83C4F08945");Register(cab,1,0x2ae994);var tumba=Marshal.ReadIntPtr(cab,0x10);Check("Tumba class",Name(tumba)=="TPult_Tumba_2ES6","TPult_Tumba_2ES6",Name(tumba));tumbaForm=Marshal.ReadIntPtr(tumba,0xc);Check("Tumba shared Pult",Marshal.ReadIntPtr(tumbaForm,0x3b4)==mem["TPult"],true,true);oldEpk=Marshal.ReadByte(mem["TLocParam"],0x99);oldVcu=Marshal.ReadByte(mem["TLocParam"],0x8f);oldKm=Marshal.ReadByte(mem["TLocParam"],0x7d);oldKvt=Marshal.ReadByte(mem["TLocParam"],0x7e);nativeInputsSet=true;Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),1);Position(Marshal.ReadIntPtr(tumbaForm,0x3a4),1);
  ((CheckBox)Field("avpSoftwareTd")).Checked=true;((CheckBox)Field("avpTd1")).Checked=true;((CheckBox)Field("avpTd2")).Checked=true;((CheckBox)Field("avpMovementPermission")).Checked=true;((NumericUpDown)Field("avpTargetLimit")).Value=20;((ComboBox)Field("monitorCombo")).SelectedIndex=0;
  Cab(0x3a0,1);Cab(0x3a4,1);Cab(0x3dc,0);Cab(0x3c4,0);Cab(0x3c8,0);Cab(0x3bc,1);Cab(0x3fc,1);Cab(0x3e0,1);phase="pantographs_before_bv";Warm(10);Cab(0x3e0,0);Warm(290);Cab(0x3dc,1);phase="bv_before_fans";Warm(150);Cab(0x3dc,0);phase="bv_operator_reset";Warm(10);Cab(0x3dc,1);phase="bv_after_reset";Warm(200);Cab(0x3c4,1);phase="fans_after_bv";Warm(160);phase="compressor_explicit_off_reset";Cab(0x3c8,0);Warm(10);phase="compressor_after_live_bv";Cab(0x3c8,1);Warm(50);var pultSlot=Marshal.ReadIntPtr(GetModuleHandle("Pult1.dll"),0x1a2e60);var pultOwner=Marshal.ReadIntPtr(pultSlot);var scheme=IntPtr.Add(pultOwner,0x18);Check("Physical compressor startup has no native latch",Marshal.ReadByte(scheme,0xc0)==0,0,new{error=Marshal.ReadByte(scheme,0xc0),count=Marshal.ReadByte(scheme,0xc2)});Check("Native electrical model fault free",Convert.ToInt32(Field("electricalFaults"))==0,0,Field("electricalFaults"));Check("Bridge is lazy before explicit key5",Field("avpNative")==null,"null",Field("avpNative")==null);}

 static IntPtr SautExport(string name,int rva){var p=GetProcAddress(saut,name);Check("SAUT export "+name,p==IntPtr.Add(saut,rva),IntPtr.Add(saut,rva).ToString("X"),p.ToString("X"));return p;}
 static void SautCode(int rva,string bytes){var b=new byte[bytes.Length/2];Marshal.Copy(IntPtr.Add(saut,rva),b,0,b.Length);string actual=BitConverter.ToString(b).Replace("-","");Check("SAUT code "+rva.ToString("X"),actual==bytes,bytes,actual);}
 static void GuardSaut(){
  saut=GetModuleHandle("Saut_Dll.dll");Module(saut,@"RESOURCES\Saut_Dll.dll","977B4B8BD3D3EB98BF9E2F08A8C413525D85F42085D34ABBE842F2BB79384A92");
  var slot=Marshal.ReadIntPtr(saut,0x1c70c8);Check("SAUT owner slot readable",Readable(slot,4),true,Readable(slot,4));sautOwner=Marshal.ReadIntPtr(slot);
  Check("SAUT native class",Name(sautOwner)=="TUpdateSAUTClass","TUpdateSAUTClass",Name(sautOwner));
  var dedicated=mem["StandaloneSautSharedRecord"];var dedicatedOut=mem["StandaloneSautOutput"];
  Check("SAUT dedicated authority record",Marshal.ReadIntPtr(sautOwner,0x18)==dedicated,dedicated.ToString("X"),Marshal.ReadIntPtr(sautOwner,0x18).ToString("X"));
  Check("SAUT output isolation",Marshal.ReadIntPtr(dedicated,12)==dedicatedOut && dedicatedOut!=mem["TOutParamAllTRen"],true,dedicatedOut.ToString("X"));
  foreach(int offset in new[]{0,4,16,20})Check("SAUT shared input alias "+offset,Marshal.ReadIntPtr(dedicated,offset)==Marshal.ReadIntPtr(mem["TPribBezAllTren"],offset),true,Marshal.ReadIntPtr(dedicated,offset).ToString("X"));
  Check("SAUT private input context",Marshal.ReadIntPtr(dedicated,8)==mem["SautSafetyInput"] && mem["SautSafetyInput"]!=mem["TInputParamPult"],true,Marshal.ReadIntPtr(dedicated,8).ToString("X"));
  Check("SAUT private light context",Marshal.ReadIntPtr(mem["SautSafetyInput"],0x1f)==mem["SautSafetyLight"] && mem["SautSafetyLight"]!=mem["TCurLighterRec"],true,mem["SautSafetyLight"].ToString("X"));
  Check("SAUT button actual consumer alias",Marshal.ReadIntPtr(Marshal.ReadIntPtr(dedicated,0),0x58)==mem["TPBBtn"],true,Marshal.ReadIntPtr(Marshal.ReadIntPtr(dedicated,0),0x58).ToString("X"));
  Check("SAUT KLUB input record",Marshal.ReadIntPtr(mem["TPribBezAllTren"],0)==mem["TKLUBButton"],true,Marshal.ReadIntPtr(mem["TPribBezAllTren"],0).ToString("X"));
  Check("SAUT PB button input record",Marshal.ReadIntPtr(mem["TKLUBButton"],88)==mem["TPBBtn"],true,Marshal.ReadIntPtr(mem["TKLUBButton"],88).ToString("X"));
  Check("SAUT input record ABI size",Readable(mem["TPBBtn"],26),26,Readable(mem["TPBBtn"],26));
  Check("SAUT limitation ABI",Marshal.SizeOf(typeof(SautLimit))==52,52,Marshal.SizeOf(typeof(SautLimit)));
  SautExport("AddOgranSaut",0x1b49f4);SautExport("AddProfilSaut",0x1b49ac);SautExport("ClearDataSAUT",0x1b4aa0);
  SautCode(0x1b49f4,"558BEC51");SautCode(0x1b4a0c,"E81B6BFFFF");SautCode(0x1b4ab4,"E88379FFFF");
 }
 static void PbInput(int offset,byte value){
  if(offset!=5&&offset!=6)throw new Exception("Only declared SAUT_ALS_Btn/SAUTVKL_BTN source inputs are allowed");
  MemoryInfo info;var address=IntPtr.Add(mem["TPBBtn"],offset);
  Check("SAUT source writable "+offset,VirtualQuery(address,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))!=UIntPtr.Zero&&info.State==0x1000&&(info.Protect&0x101)==0&&((info.Protect&0xff)==4||(info.Protect&0xff)==8||(info.Protect&0xff)==0x40||(info.Protect&0xff)==0x80),true,"writable input");
  if(!pbOld.ContainsKey(offset))pbOld[offset]=Marshal.ReadByte(address);pbAssigned[offset]=value;Marshal.WriteByte(address,value);
  Check("SAUT operator source applied "+offset,Marshal.ReadByte(address)==value,value,Marshal.ReadByte(address));
 }
 static void RestorePb(){
  foreach(var pair in pbOld){byte assigned=pbAssigned[pair.Key];if(Marshal.ReadByte(mem["TPBBtn"],pair.Key)==assigned)Marshal.WriteByte(mem["TPBBtn"],pair.Key,pair.Value);
   Check("SAUT operator source restored "+pair.Key,Marshal.ReadByte(mem["TPBBtn"],pair.Key)==pair.Value,pair.Value,Marshal.ReadByte(mem["TPBBtn"],pair.Key));}
  pbOld.Clear();pbAssigned.Clear();
 }
 static void AddSautProfile(RouteGrade p){
  var record=Marshal.AllocHGlobal(12);sautRecords.Add(record);var bytes=new byte[12];
  Buffer.BlockCopy(BitConverter.GetBytes((float)routeSnapshot.X(p.km_start)),0,bytes,0,4);
  Buffer.BlockCopy(BitConverter.GetBytes((float)(routeSnapshot.X(p.km_end)-routeSnapshot.X(p.km_start))),0,bytes,4,4);
  Buffer.BlockCopy(BitConverter.GetBytes((float)p.grade_permille),0,bytes,8,4);Marshal.Copy(bytes,0,record,12);
  ((AddRecord)Marshal.GetDelegateForFunctionPointer(SautExport("AddProfilSaut",0x1b49ac),typeof(AddRecord)))(record);
 }
 static void AddSautLimit(int start,int end,int speed){
  var limit=new SautLimit{Start=start,End=end,Pass=speed,Freight=speed,Suburban=speed,Special=speed,Flag=1,Klub=1,Saut=1,Allowed=speed,Target=speed,AllowedSaut=speed,TargetSaut=speed};
  var record=Marshal.AllocHGlobal(52);sautRecords.Add(record);Marshal.StructureToPtr(limit,record,false);
  ((AddRecord)Marshal.GetDelegateForFunctionPointer(SautExport("AddOgranSaut",0x1b49f4),typeof(AddRecord)))(record);
 }
 static void SautOffForClose(){if(saut==IntPtr.Zero)return;RestorePb();var pb=mem["TPBBtn"];byte a=Marshal.ReadByte(pb,5),b=Marshal.ReadByte(pb,6);try{Marshal.WriteByte(pb,5,0);Marshal.WriteByte(pb,6,0);for(int n=0;n<20;n++){Call("AuxCycle");Application.DoEvents();}}finally{if(Marshal.ReadByte(pb,5)==0)Marshal.WriteByte(pb,5,a);if(Marshal.ReadByte(pb,6)==0)Marshal.WriteByte(pb,6,b);}}
 static void RestoreSautRoute(){
  if(!sautDataChanged)return;GuardSaut();
  ((NativeCall)Marshal.GetDelegateForFunctionPointer(SautExport("ClearDataSAUT",0x1b4aa0),typeof(NativeCall)))();
  foreach(var p in routeSnapshot.profile)AddSautProfile(p);
  foreach(var p in routeSnapshot.speed_limits)AddSautLimit((int)Math.Round(routeSnapshot.X(p.km_start)),(int)Math.Round(routeSnapshot.X(p.km_end)),p.speed_kmh);
  sautDataChanged=false;sautRestored=true;
  Check("Original SAUT route inputs restored through API",true,true,new{profiles=routeSnapshot.profile.Count,limits=routeSnapshot.speed_limits.Count});
 }
 static void Saut(){
  // Source inputs and a declared native-route restriction, never safety-output injection.
  if(Decision().DriveEnabled){Digit(5,"SAUT_prepare_advisor");Dormant("SAUT setup advisor",15);}
  GuardSaut();scenarioSignalX=(int)Math.Ceiling(Convert.ToDouble(Field("position"))+1500);scenarioLastSignalX=scenarioSignalX-2500;
  scenarioCodeValue=1;scenarioCodeActive=true;((ComboBox)Field("standaloneSautChoice")).SelectedIndex=1;Warm(10);
  Check("Declared admitting signal reaches native SAUT",Marshal.ReadByte(sautOwner,0x151)==1,1,Marshal.ReadByte(sautOwner,0x151));
  ((NumericUpDown)Field("avpTargetLimit")).Value=30;Powered("SAUT",25);phase="declared_SAUT_5kmh_CodeWay3_overspeed";
  float initialTc=F(mem["TLocParam"],13);double initialV=Speed();Check("Real speed above declared SAUT limit",initialV>=25,">=25",initialV);
  sautDataChanged=true;
  ((NativeCall)Marshal.GetDelegateForFunctionPointer(SautExport("ClearDataSAUT",0x1b4aa0),typeof(NativeCall)))();
  foreach(var p in routeSnapshot.profile)AddSautProfile(p);AddSautLimit(0,6500,5);
  scenarioCodeValue=3;savedScenarioCode=Marshal.ReadByte(mem["TInputParamPult"],0x1e);scenarioSignalX=(int)Math.Ceiling(Convert.ToDouble(Field("position"))+1000);scenarioLastSignalX=scenarioSignalX-1250;scenarioCodeActive=true;
  bool request=false,brake=false;float maximumTc=initialTc;
  for(int i=0;i<600;i++){
   Step();var loc=mem["TLocParam"];var binding=(TrainerStandaloneSautBinding)Field("standaloneSautBinding");
   CheckSautSnapshotMode();request|=binding.BrakeRequested||binding.LapRequested||binding.TractionCutRequested;
   maximumTc=Math.Max(maximumTc,F(loc,13));brake|=maximumTc>initialTc+.05;
   if(request&&!Decision().DriveEnabled&&brake)break;
  }
  Check("Declared restriction produced actual native SAUT request",request,true,Row());
  Check("Actual SAUT request cancels production Auto",!Decision().DriveEnabled&&Decision().State==TrainerAvpState.Cancelled,true,new{Decision().State,Decision().Reason});
  Check("SAUT has native cylinder response",brake,"> initial+.05",new{initialTc,maximumTc});
  for(int i=0;i<12&&Math.Abs(F(mem["TResTyga"],0))>.01;i++)Step();
  Check("Real SAUT removes native traction",Math.Abs(F(mem["TResTyga"],0))<=.01,"native force<=.01",F(mem["TResTyga"],0));
  Check("Software SAUT never impersonates hardware flag",Marshal.ReadByte(mem["TLocParam"],0x89)==0,0,Marshal.ReadByte(mem["TLocParam"],0x89));
  Check("SAUT preserves physical KMII",Marshal.ReadByte(mem["TLocParam"],0x7d)==2,2,Marshal.ReadByte(mem["TLocParam"],0x7d));
  Check("SAUT preserves physical KVTII",Marshal.ReadByte(mem["TLocParam"],0x7e)==2,2,Marshal.ReadByte(mem["TLocParam"],0x7e));
  Check("SAUT relinquishes bridge",Bridge()!=null&&!Bridge().OwnsCommands,true,Bridge()==null?"null":Bridge().OwnsCommands.ToString());Dormant("SAUT active",25);
  scenarioCodeActive=false;((ComboBox)Field("standaloneSautChoice")).SelectedIndex=3;
  RestoreSautRoute();RestorePb();phase="SAUT_inputs_and_route_restored";Dormant("SAUT recovered",40);
  if(Marshal.ReadByte(mem["TLocParam"],0x9b)==1){
   phase="SAUT_native_EPK_stop_and_key_recovery";for(int i=0;i<1800&&Speed()>.01;i++)Step();
   Check("SAUT EPK stop before physical reset",Speed()<=.01,"<=.01",Speed());
   Check("SAUT EPK recovery has no KON trip",Marshal.ReadByte(mem["TLocParam"],0x9d)==0,0,Marshal.ReadByte(mem["TLocParam"],0x9d));
   Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),0);Warm(20);
   Check("SAUT EPK trip reset by actual key OFF",Marshal.ReadByte(mem["TLocParam"],0x9b)==0,0,Marshal.ReadByte(mem["TLocParam"],0x9b));
   Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),1);Warm(50);
   Check("SAUT EPK recovered through physical key",Marshal.ReadByte(mem["TLocParam"],0x99)==1&&Marshal.ReadByte(mem["TLocParam"],0x9b)==0,true,Row());
  }
  NeutralAndRelease("SAUT");
  phase="SAUT_physical_fans_restart";Cab(0x3c4,0);Warm(10);Cab(0x3c4,1);Warm(160);
  Arm("SAUT_rearm");sautAccepted=true;
 }

 static bool standaloneControlExists(){return host!=null && Field("standaloneSautChoice")!=null;}
 static void CheckSautSnapshotMode(){if(Marshal.ReadByte(sautOwner,0x151)!=3)throw new Exception("Declared CodeWay3 did not reach native SAUT mode3");}
 static void Save(bool passed){
  File.WriteAllText(TrainerSession.ResultPath("Autovedenie_override.json"),new JavaScriptSerializer{MaxJsonLength=8000000}.Serialize(new{
   passed=passed,error=failure,checks=checks,rows=rows,native_keys=keyRows,cycles=cycles,elapsed_seconds=watch.Elapsed.TotalSeconds,
   permission_loss_and_explicit_rearm=permissionAccepted,km_iii_manual_override=km3Accepted,km_v_manual_brake_override=km5Accepted,
   kvt_iii_manual_override=kvtAccepted,epk_key_loss_and_explicit_rearm_policy=epkAccepted,epk_rearm_accepted=epkRearmed,epk_rearm_refused_while_native_protection_active=epkProtectedRefusal,native_saut_declared_limit_override=sautAccepted,
   native_saut_inputs_and_route_restored=sautRestored,guards_restored=restored,positive_native_force_observed=forceObserved,native_motion_observed=motionObserved,
   scenario=new{startup="Prepared physical cab inputs via original native UI events; production software TD operator UI explicitly opted in",route="Original host RouteModel/profile untouched",saut_limit=new{declared_test_only=true,start_m=0,end_m=6500,limit_kmh=5,entry_native_speed_kmh=25,declared_test_codeway=3,initial_declared_admitting_codeway=1,declared_signal_x_m=scenarioSignalX,declared_last_signal_x_m=scenarioLastSignalX,signal_source="Scoped native sensor input1E; not live signal permission"},recovery="Original SAUT profile/speed inputs replayed through ClearDataSAUT/AddProfilSaut/AddOgranSaut before native close"},
   scope="Production host/controller/interface/native bridge and native pneumatic/POEZD cycles; input-only manual events, permission loss, real SAUT producer from a declared native restriction, cancellation and explicit key5 rearm. No fixture provider, direct TD writes, force/speed/pressure/contactor/permission/protection-output writes, initial speed assignment or claimed factory AVP/full cold-start acceptance."}));
 }
 [STAThread]static int Main(){
  Application.EnableVisualStyles();watch.Start();int exit=1;bool nativeClosed=false;
  try{
   Check("x86 input ABI",IntPtr.Size==4,4,IntPtr.Size);
   if(File.Exists(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt")))throw new Exception("Test requires no command file");
   host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);var handle=host.Handle;Call("StartNative");
   var timer=(Timer)Field("timer");Check("Native host started",timer!=null,true,timer!=null);timer.Stop();
   mem=(Dictionary<string,IntPtr>)Field("mem");cars=(IntPtr[])Field("cars");NoDiagnosticInputs();
   Check("Production AVP initialized",(bool)Field("avpInitialized"),true,Field("avpInitialized"));
   Check("Native interface available",Ui()!=null,true,Ui()!=null);Check("Calibration available",String.IsNullOrEmpty((string)Field("avpUnavailable")),"",Field("avpUnavailable"));
   var serializer=new JavaScriptSerializer{MaxJsonLength=8000000};routeSnapshot=serializer.Deserialize<RouteModel>(serializer.Serialize((RouteModel)Field("route")));routeSnapshot.Validate();
   Check("Initial speed originates from native rest",Math.Abs(Speed())<.001,"<.001",Speed());Check("Native consist inside unchanged route",X()>=1126.18&&X()<6500,"1126.18..6500",X());
   oldPermission=((CheckBox)Field("avpMovementPermission")).Checked;oldSoftwareTd=((CheckBox)Field("avpSoftwareTd")).Checked;
   oldTd1=((CheckBox)Field("avpTd1")).Checked;oldTd2=((CheckBox)Field("avpTd2")).Checked;oldTarget=((NumericUpDown)Field("avpTargetLimit")).Value;oldMonitor=((ComboBox)Field("monitorCombo")).SelectedIndex;oldSautChoice=((ComboBox)Field("standaloneSautChoice")).SelectedIndex;uiSaved=true;
   Check("Permission default off",!oldPermission,false,oldPermission);Check("Software TD default off",!oldSoftwareTd&&!oldTd1&&!oldTd2,false,new{oldSoftwareTd,oldTd1,oldTd2});
   Setup();Module(communication,@"RESOURCES\LOCOMOTIVES\1\Communication1.dll","27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F");
   GuardCode(0x2aeeab,"8B80B0030000");GuardCode(0x2aeee8,"88427D");GuardCode(0x2aef03,"8B80B4030000");GuardCode(0x2aef40,"88427E");
   Settings();Permission();ManualKm(3,"KMIII");ManualKm(5,"KMV");ManualKvt();Saut();Epk();
   phase="explicit_finish_advisor";if(Decision().DriveEnabled)Digit(5,"finish_advisor");Dormant("Final advisor",25);
   Check("All mandatory native override phases",permissionAccepted&&km3Accepted&&km5Accepted&&kvtAccepted&&epkAccepted&&sautAccepted,true,true);exit=0;
  }catch(Exception error){failure=error.ToString();try{phase="failure_"+phase;rows.Add(Row());}catch{}Console.WriteLine(error);}
  finally{
   if(host!=null){
    try{
     if(mem!=null){
      scenarioCodeActive=false;if(standaloneControlExists())((ComboBox)Field("standaloneSautChoice")).SelectedIndex=3;SautOffForClose();if(sautDataChanged)RestoreSautRoute();if(pbOld.Count>0)RestorePb();Call("CloseAutovedenie");
      if(provider!=IntPtr.Zero&&originalPult!=IntPtr.Zero)Check("Production callback restored",Marshal.ReadIntPtr(provider,8)==originalPult,originalPult.ToString("X"),Marshal.ReadIntPtr(provider,8).ToString("X"));
      foreach(var change in cabChanges)Marshal.WriteByte(change.Key,change.Value);
      foreach(var change in cabChanges)Check("Physical cab source restored "+change.Key.ToString("X"),Marshal.ReadByte(change.Key)==change.Value,change.Value,Marshal.ReadByte(change.Key));
      if(nativeInputsSet){Position(Marshal.ReadIntPtr(cabForm,0x3b0),oldKm);Position(Marshal.ReadIntPtr(cabForm,0x3b4),oldKvt);Position(Marshal.ReadIntPtr(tumbaForm,0x3ac),oldEpk);Position(Marshal.ReadIntPtr(tumbaForm,0x3a4),oldVcu);
       Check("Original KM source restored",Marshal.ReadByte(mem["TLocParam"],0x7d)==oldKm,oldKm,Marshal.ReadByte(mem["TLocParam"],0x7d));
       Check("Original KVT source restored",Marshal.ReadByte(mem["TLocParam"],0x7e)==oldKvt,oldKvt,Marshal.ReadByte(mem["TLocParam"],0x7e));}
      if(uiSaved){((CheckBox)Field("avpMovementPermission")).Checked=oldPermission;((CheckBox)Field("avpSoftwareTd")).Checked=oldSoftwareTd;((CheckBox)Field("avpTd1")).Checked=oldTd1;((CheckBox)Field("avpTd2")).Checked=oldTd2;((NumericUpDown)Field("avpTargetLimit")).Value=oldTarget;((ComboBox)Field("monitorCombo")).SelectedIndex=oldMonitor;((ComboBox)Field("standaloneSautChoice")).SelectedIndex=oldSautChoice;}
      restored=true;
     }
    }catch(Exception error){exit=1;failure=(failure??"")+"\nRESTORATION: "+error;Console.WriteLine(error);}
    try{Save(false);}catch(Exception interim){Console.WriteLine("Pre-close diagnostic save: "+interim.Message);}
    try{host.Close();host.Dispose();nativeClosed=true;}catch(Exception error){exit=1;failure=(failure??"")+"\nCLOSE: "+error;Console.WriteLine(error);}
    // Keep all added record buffers alive until native Saut close, even on failed restore.
    if(nativeClosed)foreach(var p in sautRecords)Marshal.FreeHGlobal(p);
   }
   try{Save(exit==0);}catch(Exception error){exit=1;Console.WriteLine(error);}
  }
  Console.WriteLine(exit==0?"PASS production host native manual/safety/permission overrides":"FAIL production host native overrides");return exit;
 }
}
