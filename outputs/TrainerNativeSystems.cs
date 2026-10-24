using System;using System.IO;using System.Runtime.InteropServices;using System.Collections.Generic;
partial class TrainerIntegrationHost {
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte BoolInitFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte TrainInitFn(IntPtr train);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void TrainStepFn(IntPtr train);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void SostavFn(float mass,float length,float elasticity,int k1,int k2,int brakePad,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void ProfFn(float length,float grade,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void PlanFn(float start,float length,float radius,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void StateFn(float x,float speed,int model,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void PoezdFn(float force,float dt,int locoIndex,float otherForce);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void BrakeFn(float force,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void ValuesFn(out float coupler,out float velocity,out float accel,out float brake,out float x,int index);
 TrainStepFn pneumoStep;StateFn stateTrain;PoezdFn motionStep;BrakeFn brakeTrain;ValuesFn readTrain;IntPtr trainData;IntPtr[] cars;
 List<StepFn> otherSteps=new List<StepFn>(),otherCloses=new List<StepFn>();
 bool physicsReady;float position,acceleration,signedVelocity;
 double maximumConsistSpeedKmh=Double.NaN;
 bool observedPneumaticCommandsKnown;int observedPneumaticKm=2,observedPneumaticKvt=2;double observedPneumaticCommandSeconds=Double.NaN;
 static bool Finite(float v){return !float.IsNaN(v)&&!float.IsInfinity(v);}
 static void Dbl(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}
 static double DoubleAt(IntPtr p,int o){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,o),b,0,8);return BitConverter.ToDouble(b,0);}
 partial void SelectNativeLocomotiveBrakeInput(ref float input); partial void BeforeElectricalAirCycle(); partial void CloseNativeAirCircuit(); partial void PrepareNativeAirCircuit(); partial void BeforeNativeAirStep(); partial void AfterNativeAirStep(); partial void RestoreNativeAirCompressorRequests();
 partial void BeforeReleaseElectricalCycle(); partial void AfterReleaseElectricalCycle(); partial void EndReleaseElectricalCycle(); partial void BorrowNativeReleaseInput(bool inhibit,ref IDisposable scope);
 void InitializePhysics(){
  // TTrainParamTM, recovered from native RTTI: packed 70-byte record, not a Delphi object.
  trainData=Alloc(70);I(trainData,0,76);I(trainData,0x37,80);var array=Alloc(76*8);Ptr(trainData,0x42,array);
  cars=new IntPtr[76];cars[0]=mem["TVagonParam"];Ptr(array,0,mem["TLocParam"]);Ptr(array,4,cars[0]);
  for(int n=1;n<76;n++){cars[n]=Marshal.ReadIntPtr(mem["PWagonArray"],(n-1)*8+4);Ptr(array,n*8+4,cars[n]);}
  // Volumes and VR-483 constants from the trainer's own 2ES6 and platform library records.
  byte[] vr=Hex("cdccccccccccf43f000000000000e03f333333333333e33f0000000000001640cdccccccccccec3f666666666666e63f14ae47e17a141240666666666666e63f66666666666606409a9999999999e93fcdccccccccccec3f000000000000004033333333333305400000000000001240b81e85eb51b81040333333333333fb3f6666666666660640");
  for(int n=0;n<76;n++){
   var w=cars[n];F(w,0,5);F(w,4,0);F(w,8,5);I(w,0x25,n==0?200:80);
   B(w,0x18,2);B(w,0x19,0);B(w,0x1a,1);B(w,0x1b,1);
   // Native constructor defaults: connected sleeves and open end cocks, except the two exterior ends.
   B(w,0x20,n==0?0:1);B(w,0x21,n==75?0:1);B(w,0x22,n==0?0:1);B(w,0x23,n==75?0:1);
   Dbl(w,0xcb,n==0?20.8:12.8);I(w,0xd3,29);Dbl(w,0xd7,78);Dbl(w,0xdf,12.52);
   var vp=Alloc(vr.Length);Marshal.Copy(vr,0,vp,vr.Length);B(w,0xe7,n==0?1:0);Ptr(w,0xe8,vp);B(w,0xf1,1);B(w,0xf2,n==0?0:1);
  }
  var loc=mem["TLocParam"];F(loc,5,8);F(loc,9,5);F(loc,13,0);Dbl(loc,0x13,5);I(loc,0x1b,100);
  Dbl(loc,0x3f,9);Dbl(loc,0x47,7.5);Dbl(loc,0x4f,5.300000190734863);Dbl(loc,0x57,2000);Dbl(loc,0x5f,5.300000190734863);I(loc,0x79,150);
  // Original TLoco constructor starts with the combined cock in working position.
  // EPK.Pkam is a packed Single at +0x9f; +0xa1 would overwrite part of cntKON.
  B(loc,0x7d,2);B(loc,0x7e,2);B(loc,0x80,1);B(loc,0x81,1);B(loc,0x8e,1);B(loc,0x8f,1);B(loc,0x90,1);B(loc,0x91,1);B(loc,0xa8,1);F(loc,0x9f,8);
  PrepareNativeAirCircuit(); var th=Library("TormModel.dll");EnablePneumaticMainTiming(th);byte init=Export<BoolInitFn>(th,"InitNew")();byte trainInit=Export<TrainInitFn>(th,"InitSostavClass")(trainData);Log("Pneumatics InitNew="+init+" InitSostavClass="+trainInit+" vehicles="+Marshal.ReadInt32(trainData));if(init!=1||trainInit!=1)throw new Exception("Pneumatic initialization failed");pneumoStep=Export<TrainStepFn>(th,"ProceedTormClass");
  var ph=Library("POEZD.DLL");var set=Export<SostavFn>(ph,"SetSostav");set(200,31.93f,.065f,2,2,1,76);for(int n=1;n<76;n++)set(80,14.59f,.065f,1,1,0,76);
  // SetSostav argument 6 is FTk, a two-entry brake-pad table index, not axle count.
  // FTk=0 matches the composite-pad friction law; FTk=1 remains separate for the locomotive.
  // The consist has 300 wagon axles; total physical train including locomotive has 308.
  ConfigureNativeRoute(ph);
  stateTrain=Export<StateFn>(ph,"SetInitState");stateTrain(position,0,2,76);motionStep=Export<PoezdFn>(ph,"Poezd");brakeTrain=Export<BrakeFn>(ph,"SetTorm");readTrain=Export<ValuesFn>(ph,"GetCurVal");physicsReady=true;
  Log("POEZD native model initialized: 6000t wagons + 200t two-section locomotive, 76 vehicles, 308 axles, wagon composite-pad table FTk=0 / medium R2 / AR enabled; locomotive FTk=1, route profile/curves from user PDF");
 }
 static byte[] Hex(string x){var b=new byte[x.Length/2];for(int n=0;n<b.Length;n++)b[n]=Convert.ToByte(x.Substring(n*2,2),16);return b;}
 // RE6 section 7.3: arbitrate SAUT commands without moving the physical KM handle. Native EPK trip and emergency KM remain above ordinary commands.
  // Native +EC is the independent KVT pressure before VR/TC merging at 17B518.
 byte acceptedVcuPosition=1;IntPtr synchronizedTumba;
[DllImport("kernel32.dll",CharSet=CharSet.Unicode)]static extern IntPtr GetModuleHandle(string name);
[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void VcuSyncCall();
void SyncVcuNative(IntPtr tumba){var module=GetModuleHandle("Communication1.dll");var target=IntPtr.Add(module,0x2aa074);if(module==IntPtr.Zero||Marshal.ReadByte(target)!=0x55||Marshal.ReadByte(target,1)!=0x8b||Marshal.ReadByte(target,2)!=0xec)throw new Exception("VCU sync guard");var cave=VirtualAlloc(IntPtr.Zero,(UIntPtr)32,0x3000,0x40);if(cave==IntPtr.Zero)throw new Exception("VCU sync allocation");try{var code=new List<byte>();code.Add(0xb8);code.AddRange(BitConverter.GetBytes(tumba.ToInt32()));code.Add(0xb9);code.AddRange(BitConverter.GetBytes(target.ToInt32()));code.AddRange(Hex("ffd1c3"));WriteNativeBytes(cave,code.ToArray());((VcuSyncCall)Marshal.GetDelegateForFunctionPointer(cave,typeof(VcuSyncCall)))();}finally{if(!VirtualFree(cave,UIntPtr.Zero,0x8000))throw new Exception("VCU sync free");}}void UpdateVcuInterlock(){if(!physicsReady)return;var loc=mem["TLocParam"];float tm=FloatAt(cars[0],0);double impulse=DoubleAt(loc,0xec);bool allowed=Finite(tm)&&!double.IsNaN(impulse)&&!double.IsInfinity(impulse)&&tm<.8f&&impulse>3;byte requested=Marshal.ReadByte(loc,0x8f);bool rejected=requested==3&&acceptedVcuPosition!=3&&!allowed;if(rejected){B(loc,0x8f,acceptedVcuPosition);}else if(requested>=1&&requested<=3){acceptedVcuPosition=requested;}var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);if(cab==IntPtr.Zero)return;var tumba=Marshal.ReadIntPtr(cab,0x10);if(tumba==IntPtr.Zero)return;var form=Marshal.ReadIntPtr(tumba,0xc);if(form==IntPtr.Zero)return;var control=Marshal.ReadIntPtr(form,0x3a4);if(control==IntPtr.Zero)return;Marshal.WriteInt32(control,0x224,1);Marshal.WriteByte(control,0x228,1);Marshal.WriteByte(control,0x220,(byte)(allowed?0:1));if(rejected||tumba!=synchronizedTumba){SyncVcuNative(tumba);synchronizedTumba=tumba;}}void PneumaticCycle(){
  if(!physicsReady)return;NativeSafetyInterfaceCycle();UpdateVcuInterlock();Dbl(trainData,0x2f,FloatAt(mem["TInputParamPult"],1));
  var loc=mem["TLocParam"];byte selected=Marshal.ReadByte(loc,0x7d),selectedKvt=Marshal.ReadByte(loc,0x7e),saut=Marshal.ReadByte(loc,0x89),savedBlock=Marshal.ReadByte(loc,0x90);
  try{
   bool softwareSaut=standaloneSautBinding!=null && (standaloneSautBinding.BrakeRequested || standaloneSautBinding.LapRequested);
   bool nativeBrakePriority=softwareSaut||selected>=6||Marshal.ReadByte(loc,0x9b)==1||(saut==1&&(Marshal.ReadByte(loc,0x8a)==1||Marshal.ReadByte(loc,0x8b)==1));
   int? avpKm=nativeBrakePriority?null:AutovedenieBrakeRequest(),avpKvt=nativeBrakePriority?null:AutovedenieKvtRequest();
   if(selected<6&&Marshal.ReadByte(loc,0x9b)==1){B(loc,0x7d,2);B(loc,0x89,0);}
   else if(selected<6&&softwareSaut){B(loc,0x7d,standaloneSautBinding.BrakeRequested || selected==5 ? 5 : 4);}
   else if(selected<6&&saut==1){if(Marshal.ReadByte(loc,0x8a)==1)B(loc,0x7d,2);else if(Marshal.ReadByte(loc,0x8b)==1)B(loc,0x7d,4);}
   if(!nativeBrakePriority){if(avpKm.HasValue)B(loc,0x7d,avpKm.Value);if(avpKvt.HasValue&&selectedKvt==2)B(loc,0x7e,avpKvt.Value);}
   if(Marshal.ReadByte(loc,0x8f)==2)B(loc,0x90,0);
   BeforeNativeAirStep();
   int effectiveKm=Marshal.ReadByte(loc,0x7d),effectiveKvt=Marshal.ReadByte(loc,0x93)!=0?4:Marshal.ReadByte(loc,0x7e);
   bool directlyObserved=effectiveKm>=6 || Marshal.ReadByte(loc,0x9b)==0 && !softwareSaut && !(saut==1&&(Marshal.ReadByte(loc,0x8a)!=0||Marshal.ReadByte(loc,0x8b)!=0));
   observedPneumaticCommandsKnown=false;
   IDisposable releaseInput=null;BorrowNativeReleaseInput(nativeBrakePriority,ref releaseInput);
   using(releaseInput)using(BorrowNativeSandAirInput()){pneumoStep(trainData);AfterNativeAirStep();}
   observedPneumaticKm=effectiveKm;observedPneumaticKvt=effectiveKvt;observedPneumaticCommandSeconds=ticks*.1;observedPneumaticCommandsKnown=directlyObserved;
  }finally{try{RestoreNativeAirCompressorRequests();}finally{B(loc,0x7d,selected);B(loc,0x7e,selectedKvt);B(loc,0x89,saut);B(loc,0x90,savedBlock);}}
 }
 void MotionCycle(){if(!physicsReady)return;
  // Original main executable passes locomotive cylinder pressure * cylinder count * sections;
  // for wagons it passes cylinder pressure directly. POEZD performs the force conversion.
  float locomotiveBrakeInput=FloatAt(mem["TLocParam"],13)*8;SelectNativeLocomotiveBrakeInput(ref locomotiveBrakeInput);
  for(int n=0;n<76;n++)brakeTrain(n==0?locomotiveBrakeInput:FloatAt(cars[n],4),76);
  float force=FloatAt(mem["TResTyga"],0);if(!Finite(force))throw new Exception("Тяговая модель передала недопустимую силу");
  motionStep(force,.1f,0,0);
  // Verified against the original caller: absolute position, velocity, coupler force, acceleration, deformation.
  double measuredMaximum=0;
  for(int n=0;n<76;n++){float x,v,coupler,a,deformation;readTrain(out x,out v,out coupler,out a,out deformation,n);if(!Finite(x)||!Finite(v)||!Finite(coupler)||!Finite(a)||!Finite(deformation))throw new Exception("Недопустимое состояние модели вагона "+n);measuredMaximum=Math.Max(measuredMaximum,Math.Abs((double)v)*3.6);F(cars[n],0xc,coupler);F(cars[n],0x10,a);F(cars[n],0x14,x);if(n==0){position=x;signedVelocity=v;acceleration=a;F(mem["TInputParamPult"],1,Math.Abs(v)*3.6f);F(mem["TInputParamPult"],0xd,position);I(mem["TInputParamPult"],0x11,(int)position);F(mem["TInputParamPult"],0x19,a);}}
  maximumConsistSpeedKmh=measuredMaximum;
 }
 void SetInitialSpeed(float v){CancelAutovedenie("Изменены начальные условия движения; снова нажмите 5");if(!Finite(v)||v<0||v>120){Log("Rejected initial speed="+v);return;}if(physicsReady){stateTrain(position,v,2,76);maximumConsistSpeedKmh=Double.NaN;signedVelocity=v/3.6f;Log("Native train initial speed="+v);}F(mem["TInputParamPult"],1,v);}
 string telemetryFile;System.Globalization.CultureInfo inv=System.Globalization.CultureInfo.InvariantCulture;
 void RecordTelemetry(){if(!physicsReady||ticks%10!=0)return;if(telemetryFile==null)telemetryFile=TrainerSession.TelemetryPath;
  var lines=new System.Text.StringBuilder();for(int n=0;n<76;n++)lines.AppendFormat(inv,"{0},{1:F1},{2},{3:F4},{4:F4},{5:F6},{6:F5},{7:F5},{8:F5},{9:F5},{10:F5},{11:F3},{12:F3},{13:F5},{14:F5},{15:F5}\n",ticks,ticks*.1,n,FloatAt(mem["TInputParamPult"],1),FloatAt(cars[n],0x14),FloatAt(cars[n],0x10),FloatAt(cars[n],0),n==0?FloatAt(mem["TLocParam"],13):FloatAt(cars[n],4),FloatAt(cars[n],8),FloatAt(cars[n],0xc),FloatAt(mem["TResTyga"],0),FloatAt(mem["TTygaParam"],0x5b),FloatAt(mem["TTygaParam"],0x9b),FloatAt(mem["TLocParam"],5),FloatAt(mem["TLocParam"],9),FloatAt(cars[n],4));TrainerSession.AppendTelemetry("tick,sim_seconds,vehicle,head_v_kmh,x_m,a_m_s2,tm_bar,tc_bar,zr_bar,coupler_native,traction_native,current_A,excitation_A,gr_bar,ur_bar,vr_tc_bar\n",lines.ToString());}
 void LogPhysics(){if(!physicsReady)return;Log("Physics: GR="+FloatAt(mem["TLocParam"],5)+" UR="+FloatAt(mem["TLocParam"],9)+" TM="+FloatAt(cars[0],0)+" TC="+FloatAt(mem["TLocParam"],13)+" vrTC="+FloatAt(cars[0],4)+" tailTM="+FloatAt(cars[75],0)+" tailTC="+FloatAt(cars[75],4)+" x="+position+" a="+acceleration+" KM="+Marshal.ReadByte(mem["TLocParam"],0x7d)+" KVT="+Marshal.ReadByte(mem["TLocParam"],0x7e)+" VCU="+Marshal.ReadByte(mem["TLocParam"],0x8f)+" block="+Marshal.ReadByte(mem["TLocParam"],0x90)+" KK="+Marshal.ReadByte(mem["TLocParam"],0x91));}
}














