using System;
using System.IO;
using System.Linq;
using System.Collections.Generic;
using System.Globalization;
using System.Runtime.InteropServices;
using System.Web.Script.Serialization;

public class Grade { public double km_start, km_end, grade_permille; }
public class Route { public double map_start_km, map_end_km; public List<Grade> profile; }
public class CoastingProbe {
 [DllImport("kernel32.dll", CharSet=CharSet.Unicode, SetLastError=true)] static extern IntPtr LoadLibrary(string path);
 [DllImport("kernel32.dll", CharSet=CharSet.Ansi, SetLastError=true)] static extern IntPtr GetProcAddress(IntPtr module, string name);
 [DllImport("kernel32.dll", CharSet=CharSet.Unicode)] static extern bool SetDllDirectory(string path);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void Sostav(float mass,float length,float elasticity,int k1,int k2,int axes,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void Prof(float length,float grade,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void Plan(float start,float length,float radius,int total);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void State(float x,float speed,int model,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void Step(float force,float dt,int locoIndex,float otherForce);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void Brake(float pressure,int count);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void Values(out float x,out float v,out float coupler,out float acceleration,out float deformation,int index);
 static T Export<T>(IntPtr module,string name) where T:class { IntPtr p=GetProcAddress(module,name);if(p==IntPtr.Zero)throw new Exception("Missing export "+name);return Marshal.GetDelegateForFunctionPointer(p,typeof(T)) as T; }
 static bool Finite(float v){return !float.IsNaN(v)&&!float.IsInfinity(v);}
 public static int Main(string[] args){
  try {
   string basePath=@"C:\Users\sibir\Documents\Codex\2026-10-07\e-launch-cam";
   string dll=Path.Combine(basePath,@"work\trenager_static\WORK_TRENAGER_3D\RESOURCES\POEZD.DLL");
   string routePath=Path.Combine(basePath,@"outputs\Voynovka_Koshkarovsky_route.json");
   string output=Path.Combine(basePath,@"work\dos_route\reference\physics_check");
   Route route=new JavaScriptSerializer().Deserialize<Route>(File.ReadAllText(routePath));
   string kind=args.Length>0?args[0]:"flat";
   double consistLength=31.93+75*14.59;
   var candidates=route.profile.Where(p=>(p.km_start-p.km_end)*1000>consistLength+150).ToList();
   Grade selected=kind=="uphill"?candidates.First(p=>p.grade_permille>3):kind=="downhill"?candidates.First(p=>p.grade_permille<-3):candidates.First(p=>Math.Abs(p.grade_permille)<0.001);
   double segStart=(route.map_start_km-selected.km_start)*1000;
   double segEnd=(route.map_start_km-selected.km_end)*1000;
   // The complete 1126.18 m consist stays inside this element for the short probe.
   float initialX=(float)(segStart+consistLength+100);
   SetDllDirectory(Path.GetDirectoryName(dll));
   IntPtr module=LoadLibrary(dll);
   if(module==IntPtr.Zero)throw new Exception("LoadLibrary failed "+Marshal.GetLastWin32Error());
   var set=Export<Sostav>(module,"SetSostav");
   set(200,31.93f,.065f,2,2,8,76);
   for(int n=1;n<76;n++)set(80,14.59f,.065f,1,1,4,76);
   var prof=Export<Prof>(module,"SetProf");
   foreach(var p in route.profile){int a=(int)Math.Round((route.map_start_km-p.km_start)*1000),b=(int)Math.Round((route.map_start_km-p.km_end)*1000);prof(b-a,(float)p.grade_permille,route.profile.Count);}
   Export<Plan>(module,"SetPlan")(0,(int)Math.Round((route.map_start_km-route.map_end_km)*1000),10000000,1);
   Export<State>(module,"SetInitState")(initialX,30,2,76);
   var step=Export<Step>(module,"Poezd");var brake=Export<Brake>(module,"SetTorm");var values=Export<Values>(module,"GetCurVal");
   var csv=new System.Text.StringBuilder("step,time_s,vehicle,x_m,v_m_s,coupler_native,a_m_s2,deformation_m\n");
   float firstV=0,lastV=0,firstX=0,lastX=0,headA=0,tailX=0,firstStepTail=0; bool finite=true;
   for(int tick=0;tick<=20;tick++){
    if(tick>0){for(int n=0;n<76;n++)brake(0,76);step(0,.1f,0,0);}
    for(int n=0;n<76;n++){
     float x,v,c,a,d;values(out x,out v,out c,out a,out d,n);
     finite &= Finite(x)&&Finite(v)&&Finite(c)&&Finite(a)&&Finite(d);
     csv.AppendFormat(CultureInfo.InvariantCulture,"{0},{1:F1},{2},{3:F6},{4:F6},{5:F6},{6:F6},{7:F6}\n",tick,tick*.1,n,x,v,c,a,d);
     if(n==0){if(tick==0){firstV=v;firstX=x;}if(tick==20){lastV=v;lastX=x;headA=a;}}
     if(n==75&&tick==0)tailX=x;
     if(n==75&&tick==1)firstStepTail=x;
    }
   }
   File.WriteAllText(Path.Combine(output,kind+".csv"),csv.ToString());
   var summary=new Dictionary<string,object>{{"case",kind},{"grade_permille",selected.grade_permille},{"segment_start_x_m",segStart},{"segment_end_x_m",segEnd},{"requested_initial_head_x_m",initialX},{"initial_observable_head_x_m",firstX},{"initial_observable_tail_x_m",tailX},{"first_step_tail_x_m",firstStepTail},{"consist_length_m",consistLength},{"initial_v_m_s",firstV},{"final_head_x_m",lastX},{"final_v_m_s",lastV},{"final_head_a_m_s2",headA},{"mean_dv_dt_m_s2",(lastV-firstV)/2},{"all_76_vehicles_finite",finite},{"profile_count",route.profile.Count},{"expected_initial_v_m_s",30/3.6},{"dll",dll}};
   string json=new JavaScriptSerializer().Serialize(summary);File.WriteAllText(Path.Combine(output,kind+".json"),json);Console.WriteLine(json);
   return finite&&lastX>initialX&&firstStepTail>segStart&&lastX<segEnd&&Math.Abs(firstV-30/3.6)<.01?0:2;
  } catch(Exception e){Console.WriteLine(e.ToString());return 1;}
 }
}
