using System;using System.Linq;using System.IO;using System.Runtime.InteropServices;using System.Windows.Forms;
partial class TrainerIntegrationHost {
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)]struct SautConfig{public IntPtr Path;public byte TipDv;public uint Sound;public byte Bitmap,Green,Yellow,Red,White;public int MinDistance;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)]struct CalcConfig{public IntPtr Path;public byte TipDv;public uint Sound;public byte Green,Yellow,Red,White;public int MinDistance;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)]struct KpdConfig{public IntPtr Path;public int X,Y;public byte Type,Rotate,Visible;public int Green;public byte Cassette;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte SautInitFn(SautConfig c,IntPtr pb);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte CalcInitFn(CalcConfig c,IntPtr pb);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte AvpInitFn(IntPtr pb,KlubInit c);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte KpdInitFn(IntPtr pb,KpdConfig c);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte SoundsInitFn(IntPtr path,uint parent);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte AddRecordFn(IntPtr record);
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=52)]struct RouteLimit{public int Start,End,Pass,Freight,Suburban,Special;public byte Flag,Klub,Saut,StartFlag;public int Distance,Allowed,Target,AllowedSaut,TargetSaut;public byte SautOS;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void AvpLimitFn(RouteLimit record);
 string commandStamp="";int? commandedKm,fileKm;bool safetyPower,momentaryCharge;
 TrainerStandaloneSautBinding standaloneSautBinding;bool nativeKpdEnabled=false;
 void SautLifecycleLog(string message){try{Log(message);}catch{}}
 IntPtr standaloneSautSharedRecord,standaloneSautOutput;
 void AllocateStandaloneSautRecords(IntPtr canonical){
  if(standaloneSautSharedRecord!=IntPtr.Zero||standaloneSautOutput!=IntPtr.Zero||
     mem.ContainsKey("StandaloneSautSharedRecord")||mem.ContainsKey("StandaloneSautOutput"))
   throw new Exception("Standalone SAUT records already owned");
  try{
   // Full TOutParamAllTRen ABI, initially zero. Only native SAUT writes its
   // effective limit and protection outputs after initialization.
   standaloneSautOutput=Alloc(106);sautInputContext=CreateSafetyContext("Saut",standaloneSautOutput);
   standaloneSautSharedRecord=sautInputContext.SharedRecord;
   mem.Add("StandaloneSautSharedRecord",standaloneSautSharedRecord);
   mem.Add("StandaloneSautOutput",standaloneSautOutput);
  }catch{ReleaseStandaloneSautRecords();throw;}
 }
 void ReleaseStandaloneSautRecords(){
  // Call only before any native initialization attempt, or after native close
  // has returned successfully and the managed binding has detached.
  var shared=standaloneSautSharedRecord;var output=standaloneSautOutput;
  if(shared!=IntPtr.Zero){ReleaseSafetyContext("Saut",sautInputContext);sautInputContext=null;standaloneSautSharedRecord=IntPtr.Zero;
   if(mem.ContainsKey("StandaloneSautSharedRecord")&&mem["StandaloneSautSharedRecord"]==shared)mem.Remove("StandaloneSautSharedRecord");}
  if(output!=IntPtr.Zero){Marshal.FreeHGlobal(output);standaloneSautOutput=IntPtr.Zero;
   if(mem.ContainsKey("StandaloneSautOutput")&&mem["StandaloneSautOutput"]==output)mem.Remove("StandaloneSautOutput");}
  SautLifecycleLog("Standalone SAUT private record ownership released");
 }
 void AddStandaloneSaut(IntPtr module,IntPtr shared,SautConfig configuration){
  var nativeInit=Export<SautInitFn>(module,"Init_Saut_Dll");
  var nativeStep=Export<StepFn>(module,"Proceed_saut");var nativeClose=Export<StepFn>(module,"CloseSAUTDll");
  AllocateStandaloneSautRecords(shared);
  TrainerStandaloneSautBinding binding=null;bool closeStarted=false,nativeInitAttempted=false;
  // Register cleanup before initialization. A failed init/authority guard never
  // borrows hardware PB inputs, and the owned buffers survive a failed close.
  otherCloses.Add(delegate {
   if(closeStarted)return;closeStarted=true;Exception failure=null;bool nativeClosed=!nativeInitAttempted,bindingDetached=binding==null;
   try{
    if(binding!=null&&binding.Connected){
     SautLifecycleLog("Standalone SAUT close: operator input off begin");
     using(BorrowStandaloneSautOperatorInputs(true)){
      for(int n=0;n<20;n++){if(!binding.Connected)throw new Exception("Standalone SAUT authority unavailable during shutdown");sautInputContext.Sync();nativeStep();binding.CaptureOutputs();Application.DoEvents();}
     }
     SautLifecycleLog("Standalone SAUT close: 20 native off steps completed");
    }
   }catch(Exception error){failure=error;SautLifecycleLog("Standalone SAUT off failure: "+error);}
   try{
    if(nativeInitAttempted){SautLifecycleLog("Standalone SAUT native close begin");nativeClose();nativeClosed=true;SautLifecycleLog("Standalone SAUT native close completed");}
   }catch(Exception error){if(failure==null)failure=error;SautLifecycleLog("Standalone SAUT native close failure; private buffers retained: "+error);}
   finally{
    if(binding!=null)try{binding.Dispose();bindingDetached=true;}catch(Exception error){if(failure==null)failure=error;}
    if(bindingDetached&&Object.ReferenceEquals(standaloneSautBinding,binding))standaloneSautBinding=null;
    if(nativeClosed&&bindingDetached)try{ReleaseStandaloneSautRecords();}catch(Exception error){if(failure==null)failure=error;}
    else SautLifecycleLog("Standalone SAUT private buffers remain owned because native close/detach was not successful");
   }
   if(failure!=null)throw new Exception("Standalone SAUT shutdown failed",failure);
  });
  try{
   nativeInitAttempted=true;
   if(nativeInit(configuration,standaloneSautSharedRecord)!=1)throw new Exception("SAUT initialization failed");
   binding=TrainerStandaloneSautBinding.Attach(module,shared,mem["TOutParamAllTRen"],standaloneSautSharedRecord,
    standaloneSautOutput,mem["TLocParam"],mem["CommOwner"],root,Log,sautInputContext);
   binding.CaptureOutputs();standaloneSautBinding=binding;
   BindStandaloneSautK20ModeReader(delegate{return binding.NativeMode;});
  }catch(Exception error){SautLifecycleLog("Standalone SAUT initialization/authority refused; buffers retained until close: "+error);throw;}
  otherSteps.Add(delegate {
   if(closeStarted)throw new ObjectDisposedException("Standalone SAUT native cycle");
   if(!binding.Connected)throw new Exception("Standalone SAUT authority unavailable");
   sautInputContext.Sync();
   using(BorrowStandaloneSautOperatorInputs())using(BorrowVigilanceOperatorInputs()){try{nativeStep();}catch(Exception error){throw new Exception("Native SAUT step failed",error);}binding.CaptureOutputs();}
  });
  SautLifecycleLog("Standalone software SAUT connected: canonical inputs, isolated output producer, immediate snapshot");
 }

 void AddAux(IntPtr h,string step,string close,string label){var nativeStep=Export<StepFn>(h,step);otherSteps.Add(delegate{try{nativeStep();}catch(Exception error){throw new Exception(label+" native step failed",error);}});otherCloses.Add(Export<StepFn>(h,close));Log(label+" native initialization successful; connected to shared train records");}
 void InitializeAux(){
  var pb=mem["TPribBezAllTren"];var path=Marshal.StringToHGlobalUni(root+"\\");
  var sound=Library("SoundsTren.dll");if(Export<SoundsInitFn>(sound,"Init_SoundsTrenDLL")(IntPtr.Zero,(uint)Handle.ToInt32())!=1)throw new Exception("SoundsTren initialization failed");otherCloses.Add(Export<StepFn>(sound,"CloseSounds"));
  var talk=Library("TalkSound.dll");if(Export<SoundsInitFn>(talk,"Init_SoundsTrenDLL")(IntPtr.Zero,(uint)Handle.ToInt32())!=1)throw new Exception("TalkSound initialization failed");otherCloses.Add(Export<StepFn>(talk,"CloseSounds"));Log("Dispatcher sound channel initialized");
  var calc=Library("CalcOgr.dll");InitializeAuxContext(calc,"Calc","Proceed_CalcOgr","CloseCalcOgrDll",delegate(IntPtr input){return Export<CalcInitFn>(calc,"Init_CalcOgr_Dll")(new CalcConfig{Path=path,TipDv=1,Sound=(uint)sound.ToInt32(),Green=(byte)route.speed_limits.ConvertAll(x=>x.speed_kmh).Max(),Yellow=60,Red=20,White=40},input);});
  var saut=Library("Saut_Dll.dll");AddStandaloneSaut(saut,pb,new SautConfig{Path=path,TipDv=1,Sound=(uint)sound.ToInt32(),Green=(byte)route.speed_limits.ConvertAll(x=>x.speed_kmh).Max(),Yellow=60,Red=20,White=40});
  var avp=Library("AVP.dll");InitializeAuxContext(avp,"NativeAvp","ProceedAVP","CloseAVP",delegate(IntPtr input){return Export<AvpInitFn>(avp,"InitAVP")(input,new KlubInit{Path=path,Type=2,TipDv=1});});
  ConfigureAuxRoute(calc,saut,avp);
  // Original GeneralPB creates/cycles KPD only when its provider is selected.
  // Do not initialize an alternative recorder beside the selected KLUB.
  if(nativeKpdEnabled){var kpd=Library("KPD_3PV.dll");InitializeAuxContext(kpd,"Kpd","ProceedKPD","CloseKPD",delegate(IntPtr input){return Export<KpdInitFn>(kpd,"InitKPD")(input,new KpdConfig{Path=path,Type=2,Green=(byte)route.speed_limits.ConvertAll(x=>x.speed_kmh).Max(),Cassette=1});});}
 }
 void AuxCycle(){foreach(var step in otherSteps)step();RefreshStandaloneSautControls();RefreshVigilanceControls();}
 void CloseAux(){for(int n=otherCloses.Count-1;n>=0;n--)otherCloses[n]();}
 void PollCommands(){
  var file=Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt");if(!File.Exists(file)){if(commandStamp.Length>0){fileKm=null;safetyPower=false;SetContactNetworkVoltage(null);commandStamp="";Log("Command file removed: test overrides released");}return;}var text=File.ReadAllText(file);if(text!=commandStamp){commandStamp=text;fileKm=null;safetyPower=false;SetContactNetworkVoltage(null);foreach(var line in text.Split('\n')){var kv=line.Trim().Split('=');if(kv.Length!=2)continue;ReadContactNetworkCommand(kv[0],kv[1]);ReadModernTractionCommand(kv[0],kv[1]);float v;int k;if(kv[0]=="speed"&&float.TryParse(kv[1],System.Globalization.NumberStyles.Float,System.Globalization.CultureInfo.InvariantCulture,out v))SetInitialSpeed(v);if(kv[0]=="km"&&int.TryParse(kv[1],out k)&&k>=1&&k<=7)fileKm=k;if(kv[0]=="safety")safetyPower=kv[1]=="1";}Log("Applied test inputs: "+text.Replace('\n',' '));}
 }
 void ApplyTestInputs(){
  ApplyBrakeInput();
  if(safetyPower){B(mem["TPBBtn"],4,1);B(mem["TPBBtn"],5,1);B(mem["TPBBtn"],6,1);B(mem["TPBBtn"],7,0);B(mem["TPBBtn"],8,0);B(mem["TPBBtn"],9,1);B(mem["TLocParam"],0x99,1);B(mem["TLocParam"],0x9a,1);}
  // Explicit stand control for the selected display. No EPK feedback or ALSN output is overridden.
  if(klubPowerMode!=2)B(mem["TPBBtn"],4,klubPowerMode==0?1:0);
 }
 void ApplyBrakeInput(){int? km=commandedKm.HasValue?commandedKm:fileKm;if(!km.HasValue)return;
  // A KM command must not bypass the independent VCU/blocking/combined-cock inputs.
  // Communication has already applied the native emergency-button priority.
  if(Marshal.ReadByte(mem["TPult"],0x23)==1||Marshal.ReadByte(mem["TPult"],0x24)==1)return;
  B(mem["TLocParam"],0x7d,km.Value);
 }
 void BeginCharge(){if(momentaryCharge)return;momentaryCharge=true;TestBrake(1);}
 void EndCharge(){if(!momentaryCharge)return;momentaryCharge=false;if(commandedKm==1)TestBrake(2);}
 void TestBrake(int km){commandedKm=km;Log("Test brake input KM="+km);}
 void ReleaseTestInputs(){momentaryCharge=false;commandedKm=null;fileKm=null;safetyPower=false;Log("Test inputs released: native cab controls active");}
 [DllImport("msvcrt.dll",CallingConvention=CallingConvention.Cdecl,EntryPoint="_clearfp")]static extern uint ClearFloatingStatus();
 int electricalFaults;string electricalFault="";
 int nativeSandElectricalTick=-1;bool nativeSandElectricalReady;
 partial void AfterCommStepTractionHook();
 void ElectricalCycle(){bool completed=false;nativeSandElectricalReady=false;UpdateVcuInterlock();BeginVigilanceElectricalInputs();try{BeforeContactNetworkElectricalCycle();BeforeReleaseElectricalCycle();BeforeElectricalAirCycle();Dbl(mem["TTygaParam"],0,signedVelocity);commStep();AfterContactNetworkElectricalCycle();UpdateVcuInterlock();AfterCommStepTractionHook();electricalFault="";completed=true;}catch(ArithmeticException ex){ClearFloatingStatus();electricalFault=ex.GetType().Name;electricalFaults++;F(mem["TResTyga"],0,0);if(electricalFaults==1||ticks%100==0){var t=mem["TTygaParam"];var flags=new System.Text.StringBuilder();for(int n=0;n<8;n++)flags.Append(Marshal.ReadInt32(t,0x10d+n*4)+",");Log("Native electrical fault, traction handled: "+ex+"; sections="+Marshal.ReadInt32(t,8)+" pos="+Marshal.ReadInt32(t,0xc)+" soed="+Marshal.ReadByte(t,0xdc)+" mode="+Marshal.ReadInt32(t,0x1b)+" U="+FloatAt(mem["TResTyga"],0x11)+" TD flags="+flags);}}finally{try{EndReleaseElectricalCycle();}finally{EndVigilanceElectricalInputs();}}if(completed){AfterReleaseElectricalCycle();nativeSandElectricalTick=ticks;nativeSandElectricalReady=true;}}
}


