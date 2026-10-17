using System;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Web.Script.Serialization;
class BrakeRateScopeProbe {
static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
static void Call(object h,string n){typeof(TrainerIntegrationHost).GetMethod(n,flags).Invoke(h,null);}
static float F(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
[STAThread]static int Main(string[] args){Application.EnableVisualStyles();var h=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);RelayDeadbandExperiment experiment=null;try{
IntPtr handle=h.Handle;Call(h,"StartNative");((Timer)typeof(TrainerIntegrationHost).GetField("timer",flags).GetValue(h)).Stop();var mem=(Dictionary<string,IntPtr>)typeof(TrainerIntegrationHost).GetField("mem",flags).GetValue(h);var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",flags).GetValue(h);var train=(IntPtr)typeof(TrainerIntegrationHost).GetField("trainData",flags).GetValue(h);var loc=mem["TLocParam"];int count=Marshal.ReadInt32(train);int km=int.Parse(args[0]);
if(count!=1&&count!=76)throw new Exception("Unexpected train count");if(count==1)foreach(int o in new int[]{0x20,0x21,0x22,0x23})if(Marshal.ReadByte(cars[0],o)!=0)throw new Exception("Isolated fixture has an open exterior connection");
if(args.Length>1)experiment=new RelayDeadbandExperiment(double.Parse(args[1],System.Globalization.CultureInfo.InvariantCulture),args.Length>2);for(int o=0x80;o<=0x87;o++)Marshal.WriteByte(loc,o,0);Marshal.WriteByte(loc,0x7d,(byte)km);Marshal.WriteByte(loc,0x89,0);Marshal.WriteByte(loc,0x9b,0);
float target=km==7?4.5f:km==6?1.5f:4f;int tmCross=-1,urCross=-1;var rows=new List<object>();
for(int n=0;n<=1000;n++){float tm=F(cars[0],0),ur=F(loc,9);if(float.IsNaN(tm)||float.IsInfinity(tm)||tm<0||float.IsNaN(ur)||float.IsInfinity(ur)||ur<0)throw new Exception("Invalid pressure");if(tmCross<0&&tm<=target)tmCross=n;if(urCross<0&&ur<=target)urCross=n;if(n<5||n%10==0)rows.Add(new{cycle=n,tm=tm,ur=ur,pm=F(loc,5)});if(tmCross>=0&&urCross>=0)break;if(n<1000)Call(h,"PneumaticCycle");}
var result=new{scope="Pneumatic-only scope comparison, no proof of locomotive equipment volume or full RE conformity",vehicles=count,km=km,initial_tm=5,initial_ur=5,target=target,tm_cross_s=tmCross<0?(double?)null:tmCross*.1,ur_cross_s=urCross<0?(double?)null:urCross*.1,compressor_inputs_off=true,rows=rows};string json=new JavaScriptSerializer().Serialize(result);File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_brake_rate_"+count+"_"+km+(args.Length>1?"_deadband_"+args[1]+(args.Length>2?"_coherent":""):"")+".json"),json);Console.WriteLine("vehicles="+count+" KM="+km+" TMcross="+tmCross*.1+" URcross="+urCross*.1);return 0;
}catch(Exception ex){Console.WriteLine(ex);return 1;}finally{if(experiment!=null)experiment.Dispose();h.Close();h.Dispose();}}
}


