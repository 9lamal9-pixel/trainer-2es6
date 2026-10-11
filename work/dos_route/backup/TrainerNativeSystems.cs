using System;using System.IO;using System.Runtime.InteropServices;using System.Collections.Generic;
partial class TrainerIntegrationHost {
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte BoolInitFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte TrainInitFn(IntPtr train);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void TrainStepFn(IntPtr train);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void SostavFn(float mass,float length,float elasticity,int k1,int k2,int axes,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void ProfFn(int length,float grade,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void PlanFn(int start,int length,float radius,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void StateFn(float x,float speed,int model,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void PoezdFn(float force,float dt,int locoIndex,float otherForce);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void BrakeFn(float force,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void ValuesFn(out float coupler,out float velocity,out float accel,out float brake,out float x,int index);
 TrainStepFn pneumoStep;StateFn stateTrain;PoezdFn motionStep;BrakeFn brakeTrain;ValuesFn readTrain;IntPtr trainData;IntPtr[] cars;
 List<StepFn> otherSteps=new List<StepFn>(),otherCloses=new List<StepFn>();
 bool physicsReady;float position,acceleration;
 static void Dbl(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}
 static double DoubleAt(IntPtr p,int o){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,o),b,0,8);return BitConverter.ToDouble(b,0);}
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
   var vp=Alloc(vr.Length);Marshal.Copy(vr,0,vp,vr.Length);B(w,0xe7,0);Ptr(w,0xe8,vp);B(w,0xf1,1);B(w,0xf2,n==0?0:1);
  }
  var loc=mem["TLocParam"];F(loc,5,8);F(loc,9,5);F(loc,13,0);Dbl(loc,0x13,5);I(loc,0x1b,100);
  Dbl(loc,0x3f,9);Dbl(loc,0x47,7.5);Dbl(loc,0x4f,5.300000190734863);Dbl(loc,0x57,2000);Dbl(loc,0x5f,5.300000190734863);I(loc,0x79,150);
  B(loc,0x7d,2);B(loc,0x7e,2);B(loc,0x80,1);B(loc,0x81,1);B(loc,0x8e,1);B(loc,0x8f,1);B(loc,0x90,1);B(loc,0xa8,1);F(loc,0xa1,8);
  var th=Library("TormModel.dll");byte init=Export<BoolInitFn>(th,"InitNew")();byte trainInit=Export<TrainInitFn>(th,"InitSostavClass")(trainData);Log("Pneumatics InitNew="+init+" InitSostavClass="+trainInit+" vehicles=76");if(init!=1||trainInit!=1)throw new Exception("Pneumatic initialization failed");pneumoStep=Export<TrainStepFn>(th,"ProceedTormClass");
  var ph=Library("POEZD.DLL");var set=Export<SostavFn>(ph,"SetSostav");set(200,31.93f,.065f,2,2,8,76);for(int n=1;n<76;n++)set(80,14.59f,.065f,1,1,4,76);
  Export<ProfFn>(ph,"SetProf")(200000,0,1);Export<PlanFn>(ph,"SetPlan")(0,200000,10000000,1);
  stateTrain=Export<StateFn>(ph,"SetInitState");stateTrain(0,0,2,76);motionStep=Export<PoezdFn>(ph,"Poezd");brakeTrain=Export<BrakeFn>(ph,"SetTorm");readTrain=Export<ValuesFn>(ph,"GetCurVal");physicsReady=true;
  Log("POEZD native model initialized: 6000t wagons + 200t two-section locomotive, 76 vehicles, 308 axles, level test track");
 }
 static byte[] Hex(string x){var b=new byte[x.Length/2];for(int n=0;n<b.Length;n++)b[n]=Convert.ToByte(x.Substring(n*2,2),16);return b;}
 void PneumaticCycle(){if(!physicsReady)return;Dbl(trainData,0x2f,FloatAt(mem["TInputParamPult"],1));pneumoStep(trainData);}
 void MotionCycle(){if(!physicsReady)return;
  // Original main executable passes locomotive cylinder pressure * cylinder count * sections;
  // for wagons it passes cylinder pressure directly. POEZD performs the force conversion.
  for(int n=0;n<76;n++)brakeTrain(n==0?FloatAt(mem["TLocParam"],13)*8:FloatAt(cars[n],4),76);
  motionStep(FloatAt(mem["TResTyga"],0),.1f,0,0);
  // Verified against the original caller: absolute position, velocity, coupler force, acceleration, deformation.
  for(int n=0;n<76;n++){float x,v,coupler,a,deformation;readTrain(out x,out v,out coupler,out a,out deformation,n);F(cars[n],0xc,coupler);F(cars[n],0x10,a);F(cars[n],0x14,x);if(n==0){position=x;acceleration=a;F(mem["TInputParamPult"],1,Math.Abs(v)*3.6f);F(mem["TInputParamPult"],0xd,position);I(mem["TInputParamPult"],0x11,(int)position);F(mem["TInputParamPult"],0x19,a);}}
 }
 void SetInitialSpeed(float v){if(physicsReady){stateTrain(position,v,2,76);Log("Native train initial speed="+v);}F(mem["TInputParamPult"],1,v);}
 string telemetryFile;System.Globalization.CultureInfo inv=System.Globalization.CultureInfo.InvariantCulture;
 void RecordTelemetry(){if(!physicsReady||ticks%10!=0)return;if(telemetryFile==null){telemetryFile=Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_telemetry_"+DateTime.Now.ToString("yyyyMMdd_HHmmss")+".csv");File.WriteAllText(telemetryFile,"tick,sim_seconds,vehicle,head_v_kmh,x_m,a_m_s2,tm_bar,tc_bar,zr_bar,coupler_native,traction_native,current_A,excitation_A,gr_bar,ur_bar\n");}
  var lines=new System.Text.StringBuilder();for(int n=0;n<76;n++)lines.AppendFormat(inv,"{0},{1:F1},{2},{3:F4},{4:F4},{5:F6},{6:F5},{7:F5},{8:F5},{9:F5},{10:F5},{11:F3},{12:F3},{13:F5},{14:F5}\n",ticks,ticks*.1,n,FloatAt(mem["TInputParamPult"],1),FloatAt(cars[n],0x14),FloatAt(cars[n],0x10),FloatAt(cars[n],0),FloatAt(cars[n],4),FloatAt(cars[n],8),FloatAt(cars[n],0xc),FloatAt(mem["TResTyga"],0),FloatAt(mem["TTygaParam"],0x5b),FloatAt(mem["TTygaParam"],0x9b),FloatAt(mem["TLocParam"],5),FloatAt(mem["TLocParam"],9));File.AppendAllText(telemetryFile,lines.ToString());}
 void LogPhysics(){if(!physicsReady)return;Log("Physics: GR="+FloatAt(mem["TLocParam"],5)+" UR="+FloatAt(mem["TLocParam"],9)+" TM="+FloatAt(cars[0],0)+" TC="+FloatAt(cars[0],4)+" tailTM="+FloatAt(cars[75],0)+" tailTC="+FloatAt(cars[75],4)+" x="+position+" a="+acceleration+" KM="+Marshal.ReadByte(mem["TLocParam"],0x7d)+" VCU="+Marshal.ReadByte(mem["TLocParam"],0x8f)+" block="+Marshal.ReadByte(mem["TLocParam"],0x90)+" KK="+Marshal.ReadByte(mem["TLocParam"],0x91));}
}
