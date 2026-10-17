using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Windows.Forms;using System.Web.Script.Serialization;using TrainerAir;
class PerPadNativeProbe {
 [DllImport("kernel32.dll")]static extern IntPtr GetModuleHandle(string name);
 [UnmanagedFunctionPointer(CallingConvention.Cdecl)]delegate void ForceCall();
 static BindingFlags f=BindingFlags.Instance|BindingFlags.NonPublic;
 static float Read(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 static void Write(IntPtr p,int o,float x){Marshal.Copy(BitConverter.GetBytes(x),0,IntPtr.Add(p,o),4);}
 static byte[] Bytes(IntPtr p,int n){byte[] b=new byte[n];Marshal.Copy(p,b,0,n);return b;}
 [STAThread]static int Main(){Application.EnableVisualStyles();var h=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);bool closed=false;try{
  var handle=h.Handle;typeof(TrainerIntegrationHost).GetMethod("StartNative",f).Invoke(h,null);((Timer)typeof(TrainerIntegrationHost).GetField("timer",f).GetValue(h)).Stop();
  typeof(TrainerIntegrationHost).GetMethod("ElectricalCycle",f).Invoke(h,null);
  var chain=(TwoSectionAirChain)typeof(TrainerIntegrationHost).GetField("airCircuit",f).GetValue(h);
  var module=GetModuleHandle("POEZD.DLL");var site=IntPtr.Add(module,0x1ebb);var hook=Bytes(site,6);byte[] original={0xd8,0x4d,0xf4,0x8b,0x55,0xf0};
  if(hook[0]!=0xe9)throw new Exception("Per-pad hook absent");
  var native=(ForceCall)Marshal.GetDelegateForFunctionPointer(IntPtr.Add(module,0x1dbc),typeof(ForceCall));
  var writer=typeof(TrainerIntegrationHost).GetMethod("WriteNativeBytes",BindingFlags.Static|BindingFlags.NonPublic);
  var rows=new List<object>();double maxError=0;int wagonChecks=0;
  foreach(double[] pressures in new[]{new double[]{0,0,0,0},new double[]{1,1,1,1},new double[]{3.9,3.9,3.9,3.9},new double[]{3.9,0,3.9,3.9},new double[]{3.9,1,3.9,1}}){
   for(int i=0;i<4;i++){var tc=chain.Sections[i/2].Cylinder[i%2];tc.mass=new Reservoir(tc.Volume*1000,pressures[i],tc.Temperature).Mass;}
   object[] args={0f};typeof(TrainerIntegrationHost).GetMethod("SelectNativeLocomotiveBrakeInput",f).Invoke(h,args);Write(module,0x1a674,(float)args[0]);
   for(int i=1;i<76;i++)Write(module,0x1a674+i*4,2f);
   foreach(float speed in new[]{0f,1f,14.166667f,25f,-14.166667f}){
    for(int i=0;i<76;i++)Write(module,0x18f04+i*4,speed);
    native();float actual=Read(module,0x1d0ac);var wagons=new float[75];for(int i=1;i<76;i++)wagons[i-1]=Read(module,0x1d0ac+i*4);
    double part=0;foreach(double pressure in pressures){double k=Math.PI*20.3*20.3/4*4.4*.00085*pressure;part+=8*.031*k*(k+6.25)/(k+1.92);}
    // Use the native Single table values to isolate coupling from coefficient rounding.
    double factor=(Math.Abs(speed)+Read(module,0x151fc+4))/(Math.Abs(speed)+Read(module,0x15204+4));
    double expected=-part*factor,error=Math.Abs(actual-expected);maxError=Math.Max(maxError,error);
    if(error>Math.Max(1e-5,Math.Abs(expected)*1e-6))throw new Exception("Per-pad native force mismatch");
    try{writer.Invoke(null,new object[]{site,original});native();for(int i=1;i<76;i++){if(Read(module,0x1d0ac+i*4)!=wagons[i-1])throw new Exception("Wagon friction altered");wagonChecks++;}}
    finally{writer.Invoke(null,new object[]{site,hook});}
    rows.Add(new{pressures=pressures,speed_ms=speed,native_force=actual,expected_native_force=expected});
   }
  }
  h.Close();h.Dispose();closed=true;var restored=Bytes(site,6);for(int i=0;i<6;i++)if(restored[i]!=original[i])throw new Exception("Per-pad hook restoration failed");
  File.WriteAllText(TrainerSession.ResultPath("Trainer_per_pad_native_validation.json"),new JavaScriptSerializer().Serialize(new{passed=true,candidate_only=true,calibrated_to_RE=false,cases=rows.Count,wagon_checks=wagonChecks,max_native_force_error=maxError,hook_restored=true,scope="Native force function, individual bogie pressures, unchanged wagon force, forward/reverse/zero speed; absolute units and material law remain pending",rows=rows}));
  Console.WriteLine("PASS per-pad native force: "+rows.Count+" cases, "+wagonChecks+" wagon checks, max error "+maxError+", hook restored");return 0;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{if(!closed){h.Close();h.Dispose();}}}
}
