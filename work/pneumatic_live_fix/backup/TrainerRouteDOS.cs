using System;
using System.IO;
using System.Linq;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;
using System.Web.Script.Serialization;
using System.Runtime.InteropServices;

public class RouteGrade { public double km_start,km_end,grade_permille; public int page; }
public class RouteSpeed { public double km_start,km_end; public int speed_kmh,page; }
public class RouteAdvice { public double km_start,km_end; public int weight_t,page; public string text; }
public class RouteStation { public double km; public int page; public string name; }
public class RouteCurve { public double km_start,km_end,radius_m,length_m; public int page; public string chainage_kind; }
public class RouteUnknownCurve { public double km_start,km_end,length_m; public double? radius_m; public int page; public string chainage_kind,reason; }
public class RouteEvent { public double km; public string type,label,chainage_kind; public int page; }
public class RouteSignal { public double km; public string type,label; public int page; public bool static_scheme; }
public class RouteModel {
 public double map_start_km,map_end_km,initial_km; public int direction;
 public List<RouteGrade> profile=new List<RouteGrade>(); public List<RouteSpeed> speed_limits=new List<RouteSpeed>();
 public List<RouteAdvice> recommendations=new List<RouteAdvice>(); public List<RouteStation> stations=new List<RouteStation>();
 public List<RouteCurve> curves=new List<RouteCurve>(); public List<RouteUnknownCurve> unknown_curves=new List<RouteUnknownCurve>();
 public List<RouteEvent> events=new List<RouteEvent>(); public List<RouteSignal> signals=new List<RouteSignal>();
 public double X(double km){return (map_start_km-km)*1000;}
 public double Km(double x){return map_start_km-x/1000;}
 public double Length {get{return X(map_end_km);}}
 bool Covers(double x,double a,double b){return x>=X(a)-0.000001&&(x<X(b)-0.000001||(Math.Abs(b-map_end_km)<0.000001&&x<=Length+0.000001));}
 public RouteGrade Grade(double x){return profile.FirstOrDefault(p=>Covers(x,p.km_start,p.km_end));}
 public RouteSpeed Limit(double x){return speed_limits.FirstOrDefault(p=>Covers(x,p.km_start,p.km_end));}
 public RouteAdvice Advice(double x){return recommendations.FirstOrDefault(p=>p.weight_t==6300&&Covers(x,p.km_start,p.km_end));}
 public double Height(double x){double sum=0;foreach(var p in profile){double a=X(p.km_start),b=X(p.km_end);if(x>a)sum+=Math.Max(0,Math.Min(x,b)-a)*p.grade_permille/1000;}return sum;}
 static bool Finite(double x){return !double.IsNaN(x)&&!double.IsInfinity(x);}
 public static RouteModel Load(string path){
  var serializer=new JavaScriptSerializer{MaxJsonLength=8000000};string text=File.ReadAllText(path);
  var raw=serializer.DeserializeObject(text) as Dictionary<string,object>;
  if(raw==null||!raw.ContainsKey("profile"))throw new Exception("Неверный файл режимной карты");
  foreach(var item in (object[])raw["profile"]){var row=item as Dictionary<string,object>;if(row==null||!row.ContainsKey("grade_permille")||row["grade_permille"]==null)throw new Exception("Уклон не указан: ноль не подставляется");}
  var r=serializer.Deserialize<RouteModel>(text);r.Validate();return r;
 }
 public void Validate(){
  if(!Finite(map_start_km)||!Finite(map_end_km)||!Finite(initial_km)||direction!=-1||Length<=0||initial_km>map_start_km||initial_km<map_end_km)throw new Exception("Неверное направление/границы режимной карты");
  if(profile==null||speed_limits==null||profile.Count==0||speed_limits.Count==0)throw new Exception("Нет профиля или скоростей PDF");
  profile=profile.OrderByDescending(x=>x.km_start).ToList();speed_limits=speed_limits.OrderByDescending(x=>x.km_start).ToList();
  CheckCoverage(profile.Select(x=>new double[]{x.km_start,x.km_end}),"profile");CheckCoverage(speed_limits.Select(x=>new double[]{x.km_start,x.km_end}),"speed");
  foreach(var p in profile)if(!Finite(p.grade_permille)||Math.Abs(p.grade_permille)>100)throw new Exception("Недопустимый уклон PDF");
  foreach(var p in speed_limits)if(p.speed_kmh<1||p.speed_kmh>120)throw new Exception("Недопустимое ограничение PDF");
  foreach(var p in stations)if(!Finite(p.km)||X(p.km)<0||X(p.km)>Length)throw new Exception("Станция вне карты");
  foreach(var p in recommendations){CheckInterval(p.km_start,p.km_end,"Рекомендация");if(p.weight_t!=8000&&p.weight_t!=6300&&p.weight_t!=2500)throw new Exception("Неизвестная весовая строка PDF");}
  curves=curves.OrderByDescending(x=>x.km_start).ToList();double last=0;
  foreach(var p in curves){CheckInterval(p.km_start,p.km_end,"Кривая");double a=X(p.km_start),b=X(p.km_end);if(a<last-.001||!Finite(p.radius_m)||p.radius_m<=0)throw new Exception("Перекрытие/неверный радиус кривой");last=b;}
  foreach(var p in unknown_curves)CheckInterval(p.km_start,p.km_end,"Кривая без радиуса");
  foreach(var p in events)if(!Finite(p.km)||X(p.km)<0||X(p.km)>Length)throw new Exception("Путевой знак вне карты");
  foreach(var p in signals)if(!Finite(p.km)||X(p.km)<0||X(p.km)>Length||!p.static_scheme)throw new Exception("Не подтверждён статический светофор PDF");
 }
 void CheckInterval(double a,double b,string label){if(!Finite(a)||!Finite(b)||a<=b||X(a)<-.001||X(b)>Length+.001)throw new Exception(label+" вне карты");}
 void CheckCoverage(IEnumerable<double[]> items,string label){double end=map_start_km;foreach(var p in items){if(!Finite(p[0])||!Finite(p[1])||p[0]<=p[1]||Math.Abs(p[0]-end)>0.000001)throw new Exception("Разрыв/перекрытие "+label+" у км "+p[0]);end=p[1];}if(Math.Abs(end-map_end_km)>0.000001)throw new Exception("Неполный "+label);}

}
public class RouteLive {
 public float x,v,force,current,excitation,tm,tc,tailTm,tailTc,gr,ur,accel; public int position,connection,tick,limit,generation; public bool finished;public string fault="";
}
public class RouteTrace {public float x,v,force,tc;}
partial class TrainerIntegrationHost {
 RouteModel route;volatile DosRouteWindow routeWindow;int routeGeneration;readonly object routeUpdateGate=new object();RouteLive latestRouteLive;bool routeUpdatePending;int lastRouteLimit=-1;IntPtr klubModule;bool klubBasisReady,klubWasPowered,routeFinished;
 void LoadRoute(){route=RouteModel.Load(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Voynovka_Koshkarovsky_route.json"));Log("PDF route loaded: km "+route.map_start_km+" -> "+route.map_end_km+", profile="+route.profile.Count+", speed limits="+route.speed_limits.Count);}
 void StartAtRouteStation(int n){
  if(n<0||n>=route.stations.Count)return;
  position=(float)route.X(route.stations[n].km);stateTrain(position,0,2,76);signedVelocity=0;acceleration=0;routeGeneration++;
  F(mem["TInputParamPult"],1,0);F(mem["TInputParamPult"],0xd,position);I(mem["TInputParamPult"],0x11,(int)position);klubBasisReady=false;
  if(routeFinished){routeFinished=false;timer.Start();}RouteInputCycle();Log("Initial station="+route.stations[n].name+" physical x="+position);
 }
 static void OnRouteUi(DosRouteWindow w,Action action){if(w==null||w.IsDisposed||!w.IsHandleCreated)return;try{w.BeginInvoke(action);}catch(InvalidOperationException){} }
 void OpenRoute(){
  var uiThread=new System.Threading.Thread(()=>{try{
   var window=new DosRouteWindow(route);window.StartAtStation=n=>{if(!IsDisposed&&IsHandleCreated)try{BeginInvoke(new Action(()=>StartAtRouteStation(n)));}catch(InvalidOperationException){}};
   window.FormClosing+=(s,e)=>{if(e.CloseReason==CloseReason.UserClosing){e.Cancel=true;window.Hide();}};routeWindow=window;Application.Run(window);
  }catch(Exception ex){Log("Route window error: "+ex);}});
  uiThread.IsBackground=true;uiThread.SetApartmentState(System.Threading.ApartmentState.STA);uiThread.Start();
  var b=new Button{Text="Режимная карта",Left=610,Top=188,Width=175};b.Click+=(s,e)=>{var w=routeWindow;OnRouteUi(w,()=>{w.Show();w.Activate();});};Controls.Add(b);
 }
 void CloseRoute(){var w=routeWindow;routeWindow=null;OnRouteUi(w,()=>{w.Dispose();Application.ExitThread();});}
 void ConfigureNativeRoute(IntPtr ph){
  var set=Export<ProfFn>(ph,"SetProf");foreach(var p in route.profile)set((float)(route.X(p.km_end)-route.X(p.km_start)),(float)p.grade_permille,route.profile.Count);
  var plan=new List<double[]>();double end=0;
  foreach(var curve in route.curves){double a=route.X(curve.km_start),b=route.X(curve.km_end);if(a>end)plan.Add(new double[]{end,a-end,10000000});plan.Add(new double[]{a,b-a,curve.radius_m});end=b;}
  if(end<route.Length)plan.Add(new double[]{end,route.Length-end,10000000});
  var setPlan=Export<PlanFn>(ph,"SetPlan");foreach(var p in plan)setPlan((float)p[0],(float)p[1],(float)p[2],plan.Count);
  position=(float)route.X(route.initial_km);
  Log("POEZD: PDF gradients="+route.profile.Count+", curves="+route.curves.Count+", plan elements="+plan.Count+", initial km="+route.initial_km);
  if(route.unknown_curves.Count>0)Log("PDF has "+route.unknown_curves.Count+" curve without radius; its additional resistance is unknown and omitted");
 }
 void ConfigureAuxRoute(IntPtr calc,IntPtr saut,IntPtr avp){foreach(var p in route.profile){var rec=Alloc(12);F(rec,0,(float)route.X(p.km_start));F(rec,4,(float)(route.X(p.km_end)-route.X(p.km_start)));F(rec,8,(float)p.grade_permille);Export<AddRecordFn>(calc,"AddProfilCalcOgr")(rec);Export<AddRecordFn>(saut,"AddProfilSaut")(rec);}foreach(var p in route.speed_limits){int a=(int)Math.Round(route.X(p.km_start)),b=(int)Math.Round(route.X(p.km_end));var limit=new RouteLimit{Start=a,End=b,Pass=p.speed_kmh,Freight=p.speed_kmh,Suburban=p.speed_kmh,Special=p.speed_kmh,Flag=1,Klub=1,Saut=1,Allowed=p.speed_kmh,Target=p.speed_kmh,AllowedSaut=p.speed_kmh,TargetSaut=p.speed_kmh};var rec=Alloc(52);Marshal.StructureToPtr(limit,rec,false);Export<AddRecordFn>(calc,"AddOgranCalcOgr")(rec);Export<AddRecordFn>(saut,"AddOgranSaut")(rec);Export<AvpLimitFn>(avp,"AddOgran")(limit);}Log("PDF route profiles/limits sent to CalcOgr, SAUT, AVP; advice does not drive controller");}
 void RouteInputCycle(){if(route==null)return;if((position<1126.18f||position>route.Length)&&!routeFinished){routeFinished=true;if(timer!=null)timer.Stop();Log("Complete consist reached route boundary; simulation paused at head x="+position);}var lim=route.Limit(position);if(lim!=null){I(mem["TInputParamPult"],5,lim.speed_kmh);I(mem["TInputParamPult"],9,lim.speed_kmh);if(lastRouteLimit!=lim.speed_kmh){lastRouteLimit=lim.speed_kmh;Log("PDF speed limit="+lastRouteLimit+" at km="+route.Km(position));}}}
 // Layout verified for the bundled KLUB DLL through RTTI and GetParamInToKLUB.
 // Keep electronic-map mode off: the source is a graphical PDF, not a KLUB map.
 void KlubCoordinateCycle(){bool powered=Marshal.ReadByte(mem["TPBBtn"],4)==1;if(!powered){klubBasisReady=false;klubWasPowered=false;return;}if(klubBasisReady&&klubWasPowered)return;IntPtr current,initial;Export<KlubPointersFn>(klubModule,"GetParamInToKLUB")(out current,out initial);if(current==IntPtr.Zero||initial==IntPtr.Zero)throw new Exception("KLUB coordinate state absent");IntPtr owner=IntPtr.Subtract(initial,8);if(current!=IntPtr.Add(owner,0x91))throw new Exception("KLUB coordinate layout differs from verified build");var coord=IntPtr.Add(initial,0x34);int railMeters=(int)Math.Round(route.Km(position)*1000),physicalMeters=(int)Math.Round(position);B(initial,0,0);I(coord,0,railMeters);I(coord,4,railMeters);I(coord,8,physicalMeters);B(coord,0xc,1);I(coord,0xd,physicalMeters);Dbl(coord,0x11,0);I(coord,0x19,railMeters);I(owner,0xfd,1);B(owner,0x101,1);klubBasisReady=true;klubWasPowered=true;Log("KLUB native coordinate basis: railway="+railMeters+" physical="+physicalMeters+" decreasing km, no electronic map");}
 bool routePreviewSaved;
 void RouteViewCycle(){var w=routeWindow;if(w==null||!w.IsHandleCreated)return;var t=mem["TTygaParam"];var live=new RouteLive{x=position,v=FloatAt(mem["TInputParamPult"],1),force=FloatAt(mem["TResTyga"],0)*10,current=FloatAt(t,0x5b),excitation=FloatAt(t,0x9b),position=Marshal.ReadInt32(t,0xc),connection=Marshal.ReadByte(t,0xdc),tm=FloatAt(cars[0],0),tc=FloatAt(cars[0],4),tailTm=FloatAt(cars[75],0),tailTc=FloatAt(cars[75],4),gr=FloatAt(mem["TLocParam"],5),ur=FloatAt(mem["TLocParam"],9),accel=acceleration,tick=ticks,limit=lastRouteLimit,fault=electricalFault,finished=routeFinished,generation=routeGeneration};
  lock(routeUpdateGate){latestRouteLive=live;if(routeUpdatePending)return;routeUpdatePending=true;}
  try{w.BeginInvoke(new Action(()=>{try{
   RouteLive newest;lock(routeUpdateGate){newest=latestRouteLive;routeUpdatePending=false;}
   if(w.IsDisposed)return;w.View.UpdateLive(newest);
   if((newest.tick>=20&&!routePreviewSaved)||newest.tick%100==0){w.View.SaveRender(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Route_modern_preview.png"));routePreviewSaved=true;}
  }catch(Exception ex){Log("Route drawing error: "+ex);}}));}catch(InvalidOperationException){lock(routeUpdateGate)routeUpdatePending=false;}
 }

}
