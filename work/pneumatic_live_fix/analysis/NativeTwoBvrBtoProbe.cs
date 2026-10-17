using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class NativeTwoBvrBtoProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static object host;static IntPtr loc,car;static Reservoir pm,tm,automatic,pilot,signal;static BtoSection bto;static PressureSelector selector;static IdealRelay kvt;static IntPtr second;static Reservoir automatic2,signal2;static BtoSection bto2;static PressureSelector selector2;static List<IntPtr> allocations=new List<IntPtr>();
 [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetModuleHandle(string name);
 [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetProcAddress(IntPtr module,string name);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte InitFn(IntPtr train);
 static TwoSectionAirChain circuit;static double initial,boundary,vent,maxError;static int count;static List<object> rows=new List<object>();
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(host,null);}
 static float Read(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static double ReadDouble(IntPtr p,int o){byte[] b=new byte[8];Marshal.Copy(IntPtr.Add(p,o),b,0,8);return BitConverter.ToDouble(b,0);}
 static void Write(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 static void Double(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}
 static double Mass(){return pm.Mass+tm.Mass+automatic.Mass+pilot.Mass+signal.Mass+bto.TotalMass+automatic2.Mass+signal2.Mass+bto2.TotalMass;}
 static void Cycle(int km,int code){
  Marshal.WriteByte(loc,0x7d,(byte)km);Marshal.WriteByte(loc,0x7e,(byte)code);
  Write(loc,5,(float)pm.Gauge);Write(car,4,(float)automatic.Gauge);Write(second,4,(float)automatic2.Gauge);float beforeAuto2=Read(second,4);
  float beforePm=Read(loc,5),beforeAuto=Read(car,4);Call("PneumaticCycle");
  circuit.ApplyNativePressureChanges(Read(loc,5)-beforePm,Read(car,4)-beforeAuto,Read(second,4)-beforeAuto2);
  circuit.StepIndependent(code,.1,true);
  boundary=circuit.ExternalMass;vent=circuit.ExhaustedMass;
  // Feedback of the loaded pilot outlet to the native483A branch, not locomotive TC.
  Write(car,4,(float)automatic.Gauge);Write(second,4,(float)automatic2.Gauge);Write(loc,5,(float)pm.Gauge);
  maxError=Math.Max(maxError,Math.Abs(Mass()+vent-initial-boundary));if(maxError>1e-8)throw new Exception("BVR boundary mass balance");
  count++;if(count%100==0)rows.Add(new{seconds=count*.1,km=km,kvt=code,pm=pm.Gauge,ur=Read(loc,9),native_zr=Read(car,8),auto_pilot=automatic.Gauge,independent_pilot=pilot.Gauge,tc1=bto.Cylinder[0].Gauge,tc2=bto.Cylinder[1].Gauge,section2_tc1=bto2.Cylinder[0].Gauge,section2_tc2=bto2.Cylinder[1].Gauge});
 }
 static void Phase(int cycles,int km,int code){for(int n=0;n<cycles;n++)Cycle(km,code);}
 [STAThread]static int Main(string[] args){bool baseline=args.Length>0&&args[0]=="baseline";Application.EnableVisualStyles();host=Activator.CreateInstance(typeof(TrainerIntegrationHost),true);try{
  var h=(TrainerIntegrationHost)host;var handle=h.Handle;Call("StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host)).Stop();
  var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);loc=mem["TLocParam"];car=((IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",flags).GetValue(host))[0];
  var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",flags).GetValue(host);
  var train=(IntPtr)typeof(TrainerIntegrationHost).GetField("trainData",flags).GetValue(host);
  second=Marshal.AllocHGlobal(243);allocations.Add(second);byte[] copy=new byte[243];Marshal.Copy(car,copy,0,243);Marshal.Copy(copy,0,second,243);
  var vr=Marshal.AllocHGlobal(136);allocations.Add(vr);copy=new byte[136];Marshal.Copy(Marshal.ReadIntPtr(car,0xe8),copy,0,136);Marshal.Copy(copy,0,vr,136);Marshal.WriteIntPtr(second,0xe8,vr);
  foreach(var c in new[]{car,second}){Marshal.WriteInt32(c,0x25,100);Double(c,0xcb,10.4);Double(c,0xd7,20);Double(c,0xdf,1);Marshal.WriteByte(c,0x18,1);Marshal.WriteByte(c,0x1a,0);}
  Marshal.WriteByte(second,0x20,1);Marshal.WriteByte(second,0x22,1);
  var array=Marshal.AllocHGlobal(77*8);allocations.Add(array);for(int i=0;i<77*8;i++)Marshal.WriteByte(array,i,0);
  Marshal.WriteIntPtr(array,0,loc);Marshal.WriteIntPtr(array,4,car);Marshal.WriteIntPtr(array,12,second);
  for(int i=1;i<76;i++)Marshal.WriteIntPtr(array,(i+1)*8+4,cars[i]);
  Marshal.WriteInt32(train,0,77);Marshal.WriteIntPtr(train,0x42,array);
  var init=(InitFn)Marshal.GetDelegateForFunctionPointer(GetProcAddress(GetModuleHandle("TormModel.dll"),"InitSostavClass"),typeof(InitFn));if(init(train)!=1)throw new Exception("Two BVR initialization");
  Double(car,0xd7,20);Double(car,0xdf,1);for(int o=0x80;o<=0x87;o++)Marshal.WriteByte(loc,o,0);
  circuit=new TwoSectionAirChain(new TwoSectionAirParameters(2000,100,1,20,1,1,1,12,6,.7,.001),8,5,8);
  pm=circuit.Pm;tm=circuit.Tm;pilot=circuit.Independent;automatic=circuit.Automatic[0];automatic2=circuit.Automatic[1];
  signal=circuit.Signal[0];signal2=circuit.Signal[1];bto=circuit.Sections[0];bto2=circuit.Sections[1];initial=Mass();
  Phase(1000,2,2);int brake=0;while(Read(loc,9)>4.5&&brake++<100)Cycle(5,2);Phase(600,4,2);double autoTc=bto.Cylinder[0].Gauge,autoTc2=bto2.Cylinder[0].Gauge;
  Phase(300,4,baseline?2:6);double combinedTc=bto.Cylinder[0].Gauge,combinedTc2=bto2.Cylinder[0].Gauge;
  Phase(300,4,2);double retainedTc=bto.Cylinder[0].Gauge,retainedTc2=bto2.Cylinder[0].Gauge;
  Phase(600,2,2);double releasedTc=bto.Cylinder[0].Gauge,releasedTc2=bto2.Cylinder[0].Gauge;
  if(autoTc2<.5||(!baseline&&combinedTc2<3.8)||retainedTc2<.5||releasedTc2>.05)throw new Exception("Second section combination failed");
  if(autoTc<.5||(!baseline&&combinedTc<3.8)||retainedTc<.5||releasedTc>.05)throw new Exception("BVR/BTO combination failed: "+autoTc+" / "+combinedTc+" / "+retainedTc+" / "+releasedTc);
  var result=new{passed=true,baseline=baseline,installed=false,calibrated_to_RE=false,scope="Two native483A records with20l ZR each feeding two BTO sections and four TC groups; native boundary flux accounted explicitly, not whole native mass balance; chamber volumes and passages are test assumptions; electrical cycle and motion absent",native_zr_volume=ReadDouble(car,0xd7),native_outlet_volume=ReadDouble(car,0xdf),second_automatic_tc=autoTc2,second_combined_tc=combinedTc2,second_retained_tc=retainedTc2,second_released_tc=releasedTc2,automatic_tc=autoTc,combined_tc=combinedTc,tc_after_kvt_release=retainedTc,released_tc=releasedTc,final_pm=pm.Gauge,shared_circuit=true,max_boundary_mass_error=maxError,rows=rows};
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,(baseline?"Trainer_native_two_BVR_BTO_baseline.json":"Trainer_native_two_BVR_BTO_validation.json")),new JavaScriptSerializer().Serialize(result));
  Console.WriteLine("PASS native BVR->BTO: "+autoTc+" / "+combinedTc+" / "+retainedTc+" / "+releasedTc);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{((Form)host).Close();((Form)host).Dispose();foreach(var p in allocations)Marshal.FreeHGlobal(p);}}
}
