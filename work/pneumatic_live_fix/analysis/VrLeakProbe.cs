using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class VrLeakProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
[DllImport("kernel32.dll")]static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint kind,uint protection);[DllImport("kernel32.dll")]static extern bool VirtualFree(IntPtr address,UIntPtr size,uint kind);[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void RegisterCall();
[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int NativeCall();
static int InvokeRegister(IntPtr self,int edx,IntPtr target){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)32,0x3000,0x40);if(p==IntPtr.Zero)throw new Exception("Thunk allocation");try{var b=new List<byte>();b.Add(0xb8);b.AddRange(BitConverter.GetBytes(self.ToInt32()));b.Add(0xba);b.AddRange(BitConverter.GetBytes(edx));b.Add(0xb9);b.AddRange(BitConverter.GetBytes(target.ToInt32()));b.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(b.ToArray(),0,p,b.Count);return ((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{VirtualFree(p,UIntPtr.Zero,0x8000);}}static void SetPosition(IntPtr self,IntPtr target,double value){var p=VirtualAlloc(IntPtr.Zero,(UIntPtr)64,0x3000,0x40);try{var b=new List<byte>();b.Add(0xb8);b.AddRange(BitConverter.GetBytes(self.ToInt32()));byte[] d=BitConverter.GetBytes(value);b.Add(0x68);b.AddRange(new byte[]{d[4],d[5],d[6],d[7]});b.Add(0x68);b.AddRange(new byte[]{d[0],d[1],d[2],d[3]});b.AddRange(new byte[]{0x6a,0,0x6a,0});b.Add(0xb9);b.AddRange(BitConverter.GetBytes(target.ToInt32()));b.AddRange(new byte[]{0xff,0xd1,0xc3});Marshal.Copy(b.ToArray(),0,p,b.Count);((NativeCall)Marshal.GetDelegateForFunctionPointer(p,typeof(NativeCall)))();}finally{VirtualFree(p,UIntPtr.Zero,0x8000);}}
 [DllImport("kernel32.dll")]static extern IntPtr GetProcAddress(IntPtr module,string name);[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte TrainInit(IntPtr data);
 static double ReadD(IntPtr p,int o){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,o),b,0,8);return BitConverter.ToDouble(b,0);}
 static BindingFlags f=BindingFlags.Instance|BindingFlags.NonPublic;
 static void Call(object h,string n){typeof(TrainerIntegrationHost).GetMethod(n,f).Invoke(h,null);}
 static float Read(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 [STAThread]static int Main(string[] args){int loadMode=args.Length>0?int.Parse(args[0]):2;bool useAuto=args.Length>1&&args[1]=="ar";int wagonMass=args.Length>2?int.Parse(args[2]):80;bool noLeak=args.Length>3&&args[3]=="noleak";bool shortTrain=true;bool disableVr=args.Length>0&&args[0]=="novr";Application.EnableVisualStyles();var h=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  var handle=h.Handle;Call(h,"StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",f).GetValue(h)).Stop();
  var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",f).GetValue(h);var loc=mem["TLocParam"];
  var chain=(TwoSectionAirChain)typeof(TrainerIntegrationHost).GetField("airCircuit",f).GetValue(h);
  var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);var form=Marshal.ReadIntPtr(cab,0xc);
  if(Marshal.ReadIntPtr(Marshal.ReadIntPtr(form,0x530),4)!=loc)throw new Exception("Cab layout guard");
  // Communication RVA2af4b0..2af4c5: form+3c8.state -> TPult+28.
  // form+3bc drives TPult.Pos_Revers(+81), not compressors.
  var control=Marshal.ReadIntPtr(form,0x3c8);if(control==IntPtr.Zero)throw new Exception("Compressor control absent");
  foreach(int field in new[]{0x3a0,0x3a4})Marshal.WriteByte(Marshal.ReadIntPtr(form,field),0x1d9,1); var rows=new List<object>();
  var pultModule=GetModuleHandle("Pult1.dll");if(pultModule==IntPtr.Zero)throw new Exception("Pult1 absent");
  var pultOwner=Marshal.ReadIntPtr(Marshal.ReadIntPtr(pultModule,0x1a2e60));
  if(pultOwner==IntPtr.Zero||Marshal.ReadIntPtr(pultOwner,4)!=mem["TPult"])throw new Exception("Native electrical owner guard");
  var schema=IntPtr.Add(pultOwner,0x18);
  var communicationModule=GetModuleHandle("Communication1.dll");int auxiliaryHardwareId=Marshal.ReadInt32(Marshal.ReadIntPtr(communicationModule,0x2c74c0));if(auxiliaryHardwareId!=48)throw new Exception("Auxiliary hardware identifier mismatch");
  for(int i=0;i<100;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}
  var communication=GetModuleHandle("Communication1.dll");InvokeRegister(cab,1,IntPtr.Add(communication,0x2ae994));var tumba=Marshal.ReadIntPtr(cab,0x10);var tumbaForm=Marshal.ReadIntPtr(tumba,0xc);var vcu=Marshal.ReadIntPtr(tumbaForm,0x3a4);
  var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",f).GetValue(h);int tailIndex=shortTrain?1:75;Marshal.WriteInt32(cars[1],0x25,wagonMass);Marshal.WriteByte(cars[1],0x18,(byte)loadMode);Marshal.WriteByte(cars[1],0x1a,(byte)(useAuto?1:0));
  if(shortTrain){var data=(IntPtr)typeof(TrainerIntegrationHost).GetField("trainData",f).GetValue(h);Marshal.WriteInt32(data,0,3);Marshal.WriteByte(cars[1],0x21,0);Marshal.WriteByte(cars[1],0x23,0);var init=(TrainInit)Marshal.GetDelegateForFunctionPointer(GetProcAddress(GetModuleHandle("TormModel.dll"),"InitSostavClass"),typeof(TrainInit));if(init(data)!=1)throw new Exception("Short chain reinit failed");}
  if(disableVr){foreach(var car in cars)Marshal.WriteByte(car,0x1b,0);Marshal.WriteByte((IntPtr)typeof(TrainerIntegrationHost).GetField("secondBvr",f).GetValue(h),0x1b,0);}
  // Diagnostic charging setpoint, not a pressure fixture: supply remains finite and native flows charge TM/ZR.
  Marshal.Copy(BitConverter.GetBytes(5.4),0,IntPtr.Add(loc,0x13),8);
  Action<int> select=km=>typeof(TrainerIntegrationHost).GetMethod("TestBrake",f).Invoke(h,new object[]{km});
  Action step=()=>{Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Marshal.WriteByte(loc,0x7e,2);for(int c=0x80;c<=0x87;c++)Marshal.WriteByte(loc,c,0);Call(h,"PneumaticCycle");};
  var second=(IntPtr)typeof(TrainerIntegrationHost).GetField("secondBvr",f).GetValue(h);var leakage=new List<object>();foreach(var c in new[]{cars[0],second,cars[1]}){leakage.Add(new{dut=ReadD(c,0x51),ut_tc=ReadD(c,0x81),ut_zr=ReadD(c,0x89)});if(noLeak)foreach(int off in new[]{0x51,0x81,0x89})Marshal.Copy(BitConverter.GetBytes(0.0),0,IntPtr.Add(c,off),8);}
  select(2);for(int i=0;i<8000;i++)step();double chargeTm=Read(cars[1],0),chargeZr=Read(cars[1],8),chargeUr=Read(loc,9);
  if(chargeTm<5.3||chargeZr<chargeTm-.11)throw new Exception("Charge incomplete: TM="+chargeTm+" ZR="+chargeZr);
  select(5);int drain=0;double tc35Time=-1;while(Read(loc,9)>3.5&&drain<1000){step();drain++;if(tc35Time<0&&Read(cars[1],4)>=3.5)tc35Time=drain*.1;}
  select(4);for(int i=0;i<1000;i++){step();if(tc35Time<0&&Read(cars[1],4)>=3.5)tc35Time=(drain+i+1)*.1;}
  double fullTc=Read(cars[1],4),serviceTm=Read(cars[1],0);select(2);int release=0;while(Read(cars[1],4)>.4&&release<2000){step();release++;}
  var result=new{initial_native_leak_fields=leakage,diagnostic_no_leak=noLeak,final_dut=ReadD(cars[1],0x51),diagnostic_wagon_gross_t=wagonMass,wagon_load_mode=loadMode,auto_regulator=useAuto,charge_tm=chargeTm,charge_zr=chargeZr,charge_ur=chargeUr,service_tm=serviceTm,full_service_tc=fullTc,tc_reaches3_5_seconds=tc35Time,release_to0_4_seconds=release*.1,diagnostic_chain_nodes=3,charge_matches_5_4_bench=false,exact_equipment_bench=false,scope="Native KM2 charge setpoint5.4,KM5 toUR3.5,KM4 hold100s,KM2 release; three-node chain, compressorsoff; no pressure fixtures; equipment bench reservoir volumes not reproduced"};
  string text=new JavaScriptSerializer().Serialize(result);File.WriteAllText(TrainerSession.ResultPath("Trainer_vr_leak_"+loadMode+(useAuto?"_ar":"")+"_"+wagonMass+(noLeak?"_noleak":"")+".json"),text);Console.WriteLine(text);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{h.Close();h.Dispose();}}
}


















