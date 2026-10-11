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
