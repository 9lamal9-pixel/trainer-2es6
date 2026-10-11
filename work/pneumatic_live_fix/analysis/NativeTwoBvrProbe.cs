using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Web.Script.Serialization;
class NativeTwoBvrProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetModuleHandle(string name);
 [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetProcAddress(IntPtr module,string name);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte InitFn(IntPtr train);
 static void Call(object h,string name){typeof(TrainerIntegrationHost).GetMethod(name,flags).Invoke(h,null);}
 static float Read(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static void Double(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}
 [STAThread]static int Main(string[] args){int mode=args.Length>0?int.Parse(args[0]):1;Application.EnableVisualStyles();var host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);var allocations=new List<IntPtr>();try{
  var handle=host.Handle;Call(host,"StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(host)).Stop();
  var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(host);var loc=mem["TLocParam"];var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",flags).GetValue(host);
  var train=(IntPtr)typeof(TrainerIntegrationHost).GetField("trainData",flags).GetValue(host);var originalArray=Marshal.ReadIntPtr(train,0x42);
  var second=Marshal.AllocHGlobal(243);allocations.Add(second);byte[] copy=new byte[243];Marshal.Copy(cars[0],copy,0,243);Marshal.Copy(copy,0,second,243);
  var secondVr=Marshal.AllocHGlobal(136);allocations.Add(secondVr);byte[] vr=new byte[136];Marshal.Copy(Marshal.ReadIntPtr(cars[0],0xe8),vr,0,136);Marshal.Copy(vr,0,secondVr,136);Marshal.WriteIntPtr(second,0xe8,secondVr);
  // Separate pneumatic sections, same total200t and same total20.8l native TM volume.
  foreach(var car in new[]{cars[0],second}){Marshal.WriteInt32(car,0x25,100);Double(car,0xcb,10.4);Double(car,0xd7,20);Double(car,0xdf,1);Marshal.WriteByte(car,0xe7,1);}
  Marshal.WriteByte(second,0x20,1);Marshal.WriteByte(second,0x22,1);Marshal.WriteByte(second,0x18,(byte)mode);Marshal.WriteByte(second,0x1a,0); // Empty load mode tests independent BVR setting.
  var array=Marshal.AllocHGlobal(77*8);allocations.Add(array);for(int i=0;i<77*8;i++)Marshal.WriteByte(array,i,0);
  Marshal.WriteIntPtr(array,0,loc);Marshal.WriteIntPtr(array,4,cars[0]);Marshal.WriteIntPtr(array,12,second);
  for(int i=1;i<76;i++)Marshal.WriteIntPtr(array,(i+1)*8+4,cars[i]);
  Marshal.WriteInt32(train,0,77);Marshal.WriteIntPtr(train,0x42,array);
  var fn=(InitFn)Marshal.GetDelegateForFunctionPointer(GetProcAddress(GetModuleHandle("TormModel.dll"),"InitSostavClass"),typeof(InitFn));if(fn(train)!=1)throw new Exception("Second BVR initialization failed");
  Marshal.WriteByte(loc,0x7d,2);for(int n=0;n<1000;n++)Call(host,"PneumaticCycle");
  Marshal.WriteByte(loc,0x7d,5);for(int n=0;n<100&&Read(loc,9)>3.5;n++)Call(host,"PneumaticCycle");
  Marshal.WriteByte(loc,0x7d,4);for(int n=0;n<600;n++)Call(host,"PneumaticCycle");
  float first=Read(cars[0],4),other=Read(second,4);if(float.IsNaN(first)||float.IsNaN(other)||first<0||other<0)throw new Exception("Invalid pressure");
  File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_native_two_BVR_mode_"+mode+".json"),new JavaScriptSerializer().Serialize(new{executed=true,independent_modes_verified=false,installed=false,calibrated_to_RE=false,scope="Two separate native483A records, independent output coefficients,77 pneumatic nodes only; motion/electrical/BTO coupling absent;10.4l TM split is a preservation assumption",wagons=75,total_loco_mass_t=200,pneumatic_nodes=77,first_load_mode=2,second_load_mode=mode,second_auto_regulator=false,first_output=first,second_output=other,first_zr=Read(cars[0],8),second_zr=Read(second,8)}));
  Console.WriteLine("Two BVR mode exploration: outputs="+first+" / "+other);return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{host.Close();host.Dispose();foreach(var p in allocations)Marshal.FreeHGlobal(p);}}
}
