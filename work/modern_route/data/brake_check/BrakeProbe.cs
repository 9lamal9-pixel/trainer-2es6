using System;
using System.IO;
using System.Text;
using System.Globalization;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;

class BrakeProbe {
 [DllImport("kernel32.dll", CharSet=CharSet.Unicode, SetLastError=true)] static extern IntPtr LoadLibrary(string path);
 [DllImport("kernel32.dll", CharSet=CharSet.Ansi, SetLastError=true)] static extern IntPtr GetProcAddress(IntPtr h,string n);
 [DllImport("kernel32.dll", CharSet=CharSet.Unicode)] static extern bool SetDllDirectory(string p);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void Sostav(float mass,float length,float elasticity,int kind1,int kind2,int pad,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void Profile(float length,float grade,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void Plan(float start,float length,float radius,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void State(float x,float speed,int model,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void Step(float force,float dt,int loco,float other);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void Brake(float pressure,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void Values(out float x,out float v,out float coupler,out float acceleration,out float deformation,int index);
 static T Export<T>(IntPtr h,string name) where T:class{return Marshal.GetDelegateForFunctionPointer(GetProcAddress(h,name),typeof(T)) as T;}
 static bool Finite(float v){return !(float.IsNaN(v)||float.IsInfinity(v));}
 static int Main(string[] args){
  string cwd=AppDomain.CurrentDomain.BaseDirectory;
  string tag=args.Length>0?args[0]:"case";
  try {
   string dll=@"C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\POEZD.DLL";
   int pad=int.Parse(args[1]);float grade=float.Parse(args[2],CultureInfo.InvariantCulture),pressure=float.Parse(args[3],CultureInfo.InvariantCulture),initial=float.Parse(args[4],CultureInfo.InvariantCulture);
   SetDllDirectory(Path.GetDirectoryName(dll));IntPtr h=LoadLibrary(dll);if(h==IntPtr.Zero)throw new Exception("DLL load failed "+Marshal.GetLastWin32Error());
   var sostav=Export<Sostav>(h,"SetSostav");sostav(200,31.93f,.065f,2,2,pad==8?8:pad,76);for(int n=1;n<76;n++)sostav(80,14.59f,.065f,1,1,pad==8?4:pad,76);
   Export<Profile>(h,"SetProf")(500000,grade,1);Export<Plan>(h,"SetPlan")(0,500000,10000000,1);Export<State>(h,"SetInitState")(10000,initial,2,76);
   var step=Export<Step>(h,"Poezd");var brake=Export<Brake>(h,"SetTorm");var values=Export<Values>(h,"GetCurVal");
   bool finite=true;int negativeSteps=0, firstBadTick=-1,stopTick=-1;float firstA=0,firstV=0,lastV=0,lastX=0,minV=float.PositiveInfinity,maxV=float.NegativeInfinity,holdMax=0,maxDeformation=0,maxCoupler=0;int signChanges=0;float lastSignV=0;
   var csv=new StringBuilder("tick,time_s,vehicle,x_m,v_kmh,a_m_s2,coupler_native,fifth_native_value\n");
   for(int tick=1;tick<=1200;tick++){
    for(int n=0;n<76;n++)brake(n==0?pressure*8:pressure,76);
    step(0,.1f,0,0);
    for(int n=0;n<76;n++){
     float x,v,c,a,d;values(out x,out v,out c,out a,out d,n);bool ok=Finite(x)&&Finite(v)&&Finite(c)&&Finite(a)&&Finite(d);if(!ok&&firstBadTick<0)firstBadTick=tick;finite&=ok;
     if(n>0)maxDeformation=Math.Max(maxDeformation,Math.Abs(d));maxCoupler=Math.Max(maxCoupler,Math.Abs(c));
     if(tick==1||tick%10==0)csv.AppendFormat(CultureInfo.InvariantCulture,"{0},{1:F1},{2},{3:F4},{4:F6},{5:F8},{6:F6},{7:F6}\n",tick,tick*.1,n,x,v*3.6,a,c,d);
     if(n==0){
      if(tick==1){firstA=a;firstV=v*3.6f;}
      lastV=v*3.6f;lastX=x;minV=Math.Min(minV,lastV);maxV=Math.Max(maxV,lastV);
      if(lastV<-.001f)negativeSteps++;
      if(initial>0&&stopTick<0&&Math.Abs(lastV)<.15f)stopTick=tick;
      if(tick>900)holdMax=Math.Max(holdMax,Math.Abs(lastV));
      if(Math.Abs(lastSignV)>.0001f&&Math.Abs(lastV)>.0001f&&Math.Sign(lastSignV)!=Math.Sign(lastV))signChanges++;
      lastSignV=lastV;
     }
    }
    if(!finite)break;
   }
   File.WriteAllText(Path.Combine(cwd,tag+".csv"),csv.ToString());
   var summary=new Dictionary<string,object>{{"case",tag},{"pad_index",pad},{"old_wrong_axes_as_pad",pad==8},{"grade_permille",grade},{"tc_pressure_input",pressure},{"locomotive_pressure_multiplier",8},{"initial_speed_kmh",initial},{"elapsed_sim_seconds",120},{"vehicles",76},{"mass_t",6200},{"first_step_v_kmh",firstV},{"first_step_a_m_s2",firstA},{"last_head_v_kmh",lastV},{"last_head_x_m",lastX},{"min_head_v_kmh",minV},{"max_head_v_kmh",maxV},{"max_abs_head_v_last30sec_kmh",holdMax},{"first_below015_kmh_seconds",stopTick<0?(object)null:stopTick*.1},{"negative_head_steps",negativeSteps},{"head_velocity_sign_changes",signChanges},{"all_76_outputs_finite",finite},{"first_bad_tick",firstBadTick},{"max_abs_coupler_native",maxCoupler},{"max_abs_wagon_fifth_native_value",maxDeformation},{"dll",dll}};
   string json=new JavaScriptSerializer().Serialize(summary);File.WriteAllText(Path.Combine(cwd,tag+".json"),json);Console.WriteLine(json);return finite?0:2;
  }catch(Exception e){var s=new Dictionary<string,object>{{"case",tag},{"exception",e.ToString()}};string json=new JavaScriptSerializer().Serialize(s);File.WriteAllText(Path.Combine(cwd,tag+".json"),json);Console.WriteLine(json);return 1;}
 }
}
