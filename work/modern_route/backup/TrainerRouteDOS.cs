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
public class RouteCurve { public double km_start,km_end,radius_m; public int page; }
public class RouteModel {
 public double map_start_km,map_end_km,initial_km; public int direction;
 public List<RouteGrade> profile=new List<RouteGrade>(); public List<RouteSpeed> speed_limits=new List<RouteSpeed>();
 public List<RouteAdvice> recommendations=new List<RouteAdvice>(); public List<RouteStation> stations=new List<RouteStation>();
 public List<RouteCurve> curves=new List<RouteCurve>();
 public double X(double km){return (map_start_km-km)*1000;}
 public double Km(double x){return map_start_km-x/1000;}
 public double Length {get{return X(map_end_km);}}
 bool Covers(double x,double a,double b){return x>=X(a)-0.000001&&(x<X(b)-0.000001||(Math.Abs(b-map_end_km)<0.000001&&x<=Length+0.000001));}
 public RouteGrade Grade(double x){return profile.FirstOrDefault(p=>Covers(x,p.km_start,p.km_end));}
 public RouteSpeed Limit(double x){return speed_limits.FirstOrDefault(p=>Covers(x,p.km_start,p.km_end));}
 public RouteAdvice Advice(double x){return recommendations.FirstOrDefault(p=>p.weight_t==6300&&Covers(x,p.km_start,p.km_end));}
 public double Height(double x){double sum=0;foreach(var p in profile){double a=X(p.km_start),b=X(p.km_end);if(x>a)sum+=Math.Max(0,Math.Min(x,b)-a)*p.grade_permille/1000;}return sum;}
 public static RouteModel Load(string path){var r=new JavaScriptSerializer{MaxJsonLength=8000000}.Deserialize<RouteModel>(File.ReadAllText(path));r.Validate();return r;}
 public void Validate(){if(direction!=-1||Length<=0||initial_km>map_start_km||initial_km<map_end_km)throw new Exception("Неверное направление/границы режимной карты");if(profile.Count==0||speed_limits.Count==0)throw new Exception("Нет профиля или скоростей PDF");profile=profile.OrderByDescending(x=>x.km_start).ToList();speed_limits=speed_limits.OrderByDescending(x=>x.km_start).ToList();CheckCoverage(profile.Select(x=>new double[]{x.km_start,x.km_end}),"profile");CheckCoverage(speed_limits.Select(x=>new double[]{x.km_start,x.km_end}),"speed");foreach(var s in speed_limits)if(s.speed_kmh<1||s.speed_kmh>120)throw new Exception("Недопустимое ограничение PDF");}
 void CheckCoverage(IEnumerable<double[]> items,string label){double end=map_start_km;foreach(var p in items){if(p[0]<=p[1]||Math.Abs(p[0]-end)>0.003)throw new Exception("Разрыв/перекрытие "+label+" у км "+p[0]);end=p[1];}if(Math.Abs(end-map_end_km)>0.003)throw new Exception("Неполный "+label);}
}
public class RouteLive {
 public float x,v,force,current,excitation,tm,tc,tailTm,tailTc,gr,ur,accel; public int position,connection,tick,limit; public bool finished;public string fault="";
}
public class RouteTrace {public float x,v,force,tc;}
public class DosRouteSurface:Control {
 public RouteModel Route;public RouteLive Live=new RouteLive(); public double Span=18000,Pan=-1; public bool Follow=true;
 public List<RouteTrace> Trace=new List<RouteTrace>();
 readonly Color cyan=Color.FromArgb(85,255,255),yellow=Color.FromArgb(255,255,85),green=Color.FromArgb(85,255,85),red=Color.FromArgb(255,85,85),gray=Color.FromArgb(170,170,170);
 Font font=new Font("Consolas",12,FontStyle.Regular),small=new Font("Consolas",10),big=new Font("Consolas",23,FontStyle.Bold);
 public DosRouteSurface(){SetStyle(ControlStyles.AllPaintingInWmPaint|ControlStyles.UserPaint|ControlStyles.OptimizedDoubleBuffer,true);BackColor=Color.Black;ForeColor=Color.White;}
 public void UpdateLive(RouteLive v){Live=v;if(v.tick%10==0){Trace.Add(new RouteTrace{x=v.x,v=v.v,force=v.force,tc=v.tc});if(Trace.Count>12000)Trace.RemoveAt(0);}Invalidate();}
 protected override void OnPaint(PaintEventArgs e){base.OnPaint(e);Render(e.Graphics,ClientSize);}
 void TextAt(Graphics g,string text,float x,float y,Color c,Font f){using(var b=new SolidBrush(c))g.DrawString(text,f,b,x,y);}
 void Line(Graphics g,Color c,float x,float y,float xx,float yy){using(var p=new Pen(c))g.DrawLine(p,x,y,xx,yy);}
 void Frame(Graphics g,RectangleF r){using(var p=new Pen(gray))g.DrawRectangle(p,r.X,r.Y,r.Width,r.Height);}
 public void Render(Graphics g,Size size){g.Clear(Color.Black);g.SmoothingMode=SmoothingMode.None;g.TextRenderingHint=TextRenderingHint.SingleBitPerPixelGridFit;if(Route==null){TextAt(g,"Загрузка режимной карты…",20,20,yellow,font);return;}
  float w=size.Width,h=size.Height,left=12,plotRight=Math.Max(520,w-326),plotW=plotRight-left-10,hudX=plotRight+14;
  double center=Follow?Live.x:Pan;if(center<0)center=Live.x;double start=Math.Max(0,Math.Min(Route.Length-Span,center-Span*0.18)),end=Math.Min(Route.Length,start+Span);if(end<=start)end=start+1;
  Func<double,float> px=x=>left+(float)((x-start)/(end-start)*plotW);
  TextAt(g,"2ЭС6  ВОЙНОВКА > КОКШАРОВСКИЙ",left,8,cyan,font);TextAt(g,"РЕЖИМНАЯ КАРТА / 2018",hudX,8,cyan,small);
  var speedRect=new RectangleF(left,45,plotW,153);Frame(g,speedRect);TextAt(g,"СКОРОСТЬ: красная — карта, голубая — поезд",left+6,48,gray,small);
  Func<double,float> vy=v=>183-(float)(v*1.12);
  for(int v=0;v<=100;v+=20){Line(g,Color.FromArgb(42,42,42),left,vy(v),plotRight-10,vy(v));TextAt(g,v.ToString(),left+3,vy(v)-14,gray,small);}
  RouteSpeed previous=null;foreach(var p in Route.speed_limits){double a=Route.X(p.km_start),b=Route.X(p.km_end);if(b>=start&&a<=end){float x1=px(Math.Max(start,a)),x2=px(Math.Min(end,b)),y=vy(p.speed_kmh);Line(g,red,x1,y,x2,y);if(previous!=null&&a>=start&&a<=end)Line(g,red,x1,vy(previous.speed_kmh),x1,y);if(x2-x1>30)TextAt(g,p.speed_kmh.ToString(),(x1+x2)/2-10,y-18,red,small);}previous=p;}
  RouteTrace old=null;foreach(var p in Trace){if(p.x>=start&&p.x<=end){if(old!=null&&Math.Abs(p.x-old.x)<500)Line(g,cyan,px(old.x),vy(old.v),px(p.x),vy(p.v));old=p;}else old=null;}
  float profileTop=226,profileBottom=Math.Max(330,h-230);var pr=new RectangleF(left,profileTop,plotW,profileBottom-profileTop);Frame(g,pr);
  double lo=double.MaxValue,hi=double.MinValue;for(double x=start;x<=end;x+=Math.Max(50,(end-start)/400)){double z=Route.Height(x);lo=Math.Min(lo,z);hi=Math.Max(hi,z);}hi=Math.Max(hi,lo+12);double range=hi-lo;
  Func<double,float> py=x=>profileBottom-32-(float)((Route.Height(x)-lo)/range*(profileBottom-profileTop-65));
  TextAt(g,"ПРОФИЛЬ ПУТИ / уклон в промилле",left+6,profileTop+3,gray,small);
  int step=Span<=12000?1:Span<=30000?2:5;for(int km=(int)Math.Floor(Route.Km(start));km>=Route.Km(end);km--){if(km%step!=0)continue;float xx=px(Route.X(km));Line(g,Color.FromArgb(0,100,0),xx,profileTop+24,xx,profileBottom);TextAt(g,km.ToString(),xx-18,profileBottom-20,green,small);}
  foreach(var p in Route.profile){double a=Math.Max(start,Route.X(p.km_start)),b=Math.Min(end,Route.X(p.km_end));if(b<a)continue;Line(g,yellow,px(a),py(a),px(b),py(b));if(px(b)-px(a)>44)TextAt(g,p.grade_permille.ToString("+0.0;-0.0;0.0",System.Globalization.CultureInfo.InvariantCulture),px((a+b)/2)-20,Math.Min(py(a),py(b))-18,yellow,small);}
  foreach(var p in Route.stations){double x=Route.X(p.km);if(x<start||x>end)continue;float xx=px(x),yy=py(x);Line(g,Color.White,xx,yy-10,xx,yy+10);TextAt(g,p.name,Math.Min(plotRight-130,Math.Max(left+2,xx-45)),profileTop+24,Color.White,small);}
  if(Live.x>=start&&Live.x<=end){float xx=px(Live.x);Line(g,cyan,xx,70,xx,profileBottom);using(var b=new SolidBrush(cyan))g.FillPolygon(b,new PointF[]{new PointF(xx,py(Live.x)-1),new PointF(xx-7,py(Live.x)-12),new PointF(xx+7,py(Live.x)-12)});}
  float adviceY=profileBottom+10;Frame(g,new RectangleF(left,adviceY,plotW,70));TextAt(g,"РЕКОМЕНДАЦИЯ ДЛЯ 6300 т (состав: 6000 т + 200 т)",left+6,adviceY+4,gray,small);var advice=Route.Advice(Live.x);using(var brush=new SolidBrush(yellow))g.DrawString(advice==null?"Нет рекомендации в этой точке":advice.text,font,brush,new RectangleF(left+6,adviceY+26,plotW-140,42));if(advice!=null)TextAt(g,"PDF стр. "+advice.page,plotRight-115,adviceY+49,gray,small);
  float graphY=adviceY+80,graphH=Math.Max(30,h-graphY-31);Frame(g,new RectangleF(left,graphY,plotW,graphH));TextAt(g,"ТЯГА/ЭДТ: -600..+800 кН (пурпур)  ТЦ: 0..6 бар (голуб)",left+5,graphY+2,gray,small);
  Func<float,float> fy=f=>graphY+20+(800-Math.Max(-600,Math.Min(800,f)))/1400*(graphH-25);Line(g,Color.FromArgb(70,70,70),left,fy(0),plotRight-10,fy(0));old=null;foreach(var p in Trace){if(p.x<start||p.x>end){old=null;continue;}if(old!=null&&Math.Abs(p.x-old.x)<500){Line(g,Color.Magenta,px(old.x),fy(old.force),px(p.x),fy(p.force));Line(g,cyan,px(old.x),graphY+graphH-5-Math.Max(0,Math.Min(6,old.tc))/6*(graphH-25),px(p.x),graphY+graphH-5-Math.Max(0,Math.Min(6,p.tc))/6*(graphH-25));}old=p;}
  Frame(g,new RectangleF(hudX-4,45,w-hudX-9,h-70));float yHud=55;TextAt(g,Live.v.ToString("000.0")+" км/ч",hudX+8,yHud,Live.v>Live.limit?red:green,big);yHud+=43;
  string coord=Route.Km(Live.x).ToString("0.000",System.Globalization.CultureInfo.InvariantCulture);var grade=Route.Grade(Live.x);var next=Route.stations.OrderBy(x=>Route.X(x.km)).FirstOrDefault(x=>Route.X(x.km)>Live.x+2);
  string con=Live.connection==1?"С":Live.connection==2?"СП":Live.connection==3?"П":"—";
  var values=new string[]{"КМ ~     "+coord,"ЛИМИТ    "+Live.limit+" км/ч","УКЛОН    "+(grade==null?"—":grade.grade_permille.ToString("+0.0;-0.0;0.0"))+" ‰","ПОЗ. ТЭД "+Live.position+" / "+con,"ТОК ЯК.  "+Live.current.ToString("0")+" А","ТОК ВОЗ. "+Live.excitation.ToString("0")+" А","ТЯГА     "+Live.force.ToString("0.0")+" кН","ТМ       "+Live.tm.ToString("0.00")+" бар","ТЦ       "+Live.tc.ToString("0.00")+" бар","ТМ ХВОСТ "+Live.tailTm.ToString("0.00")+" бар","ТЦ ХВОСТ "+Live.tailTc.ToString("0.00")+" бар","ГР / УР  "+Live.gr.ToString("0.0")+" / "+Live.ur.ToString("0.0"),"УСКОР.   "+Live.accel.ToString("+0.000;-0.000;0.000"),"ВРЕМЯ    "+TimeSpan.FromSeconds(Live.tick*.1).ToString(@"hh\:mm\:ss")};
  foreach(var s in values){TextAt(g,s,hudX+7,yHud,Color.White,font);yHud+=24;}yHud+=6;
  TextAt(g,"СЛЕДУЮЩАЯ СТАНЦИЯ",hudX+7,yHud,gray,small);yHud+=23;TextAt(g,next==null?"Конец карты":next.name,hudX+7,yHud,yellow,font);yHud+=26;if(next!=null)TextAt(g,((Route.X(next.km)-Live.x)/1000).ToString("0.00")+" км",hudX+7,yHud,green,font);
  if(Live.fault.Length>0){TextAt(g,"СБОЙ ТЯГОВОЙ DLL",hudX+7,h-111,red,font);TextAt(g,Live.fault,hudX+7,h-88,red,small);}
  TextAt(g,Live.finished?"КОНЕЦ КАРТЫ — ВЫБЕРИТЕ СТАНЦИЮ ДЛЯ НОВОГО ПРОЕЗДА":Follow?"ПУТЬ ДВИЖЕТСЯ ПО КООРДИНАТЕ ШТАТНОЙ МОДЕЛИ":"ОБЗОР КАРТЫ — ПОЛОЖЕНИЕ ПОЕЗДА НЕ МЕНЯЕТСЯ",left,h-23,Follow&&!Live.finished?green:yellow,small);
 }
 public void SaveRender(string path){using(var b=new Bitmap(Math.Max(100,Width),Math.Max(100,Height)))using(var g=Graphics.FromImage(b)){Render(g,b.Size);b.Save(path,System.Drawing.Imaging.ImageFormat.Png);}}
 protected override void Dispose(bool disposing){if(disposing){font.Dispose();small.Dispose();big.Dispose();}base.Dispose(disposing);}
}
public class DosRouteWindow:Form {
 public DosRouteSurface View;public Action<int> StartAtStation;
 public DosRouteWindow(RouteModel route){Text="2ЭС6 — режимная карта Войновка–Кокшаровский";ClientSize=new Size(1200,720);MinimumSize=new Size(1000,690);StartPosition=FormStartPosition.Manual;Location=new Point(70,70);
  var tools=new FlowLayoutPanel{Dock=DockStyle.Top,Height=34,BackColor=Color.FromArgb(30,30,30)};Controls.Add(tools);View=new DosRouteSurface{Dock=DockStyle.Fill,Route=route};Controls.Add(View);View.BringToFront();
  var follow=new CheckBox{Text="Следовать за поездом",Checked=true,ForeColor=Color.White,AutoSize=true,Margin=new Padding(7)};tools.Controls.Add(follow);follow.CheckedChanged+=(s,e)=>{View.Follow=follow.Checked;View.Invalidate();};
  var prev=new Button{Text="← Обзор",Width=94};var next=new Button{Text="Обзор →",Width=94};tools.Controls.Add(prev);tools.Controls.Add(next);prev.Click+=(s,e)=>{if(View.Follow)View.Pan=View.Live.x;follow.Checked=false;View.Pan=Math.Max(0,View.Pan-View.Span*.6);View.Invalidate();};next.Click+=(s,e)=>{if(View.Follow)View.Pan=View.Live.x;follow.Checked=false;View.Pan=Math.Min(route.Length,View.Pan+View.Span*.6);View.Invalidate();};
  var zoom=new ComboBox{Width=92,DropDownStyle=ComboBoxStyle.DropDownList};zoom.Items.AddRange(new object[]{"8 км","18 км","40 км"});zoom.SelectedIndex=1;tools.Controls.Add(zoom);zoom.SelectedIndexChanged+=(s,e)=>{View.Span=new double[]{8000,18000,40000}[zoom.SelectedIndex];View.Invalidate();};
  var station=new ComboBox{Width=208,DropDownStyle=ComboBoxStyle.DropDownList};foreach(var x in route.stations)station.Items.Add(x.name);if(station.Items.Count>0)station.SelectedIndex=0;tools.Controls.Add(station);var reset=new Button{Text="Начать со станции",Width=156};tools.Controls.Add(reset);reset.Click+=(s,e)=>{if(station.SelectedIndex>=0&&StartAtStation!=null)StartAtStation(station.SelectedIndex);follow.Checked=true;View.Trace.Clear();};
  tools.Controls.Add(new Label{Text="Управление — штатный пульт",ForeColor=Color.White,AutoSize=true,Margin=new Padding(7)});
 }
}
partial class TrainerIntegrationHost {
 RouteModel route;DosRouteWindow routeWindow;int lastRouteLimit=-1;IntPtr klubModule;bool klubBasisReady,klubWasPowered,routeFinished;
 void LoadRoute(){route=RouteModel.Load(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Voynovka_Koshkarovsky_route.json"));Log("PDF route loaded: km "+route.map_start_km+" -> "+route.map_end_km+", profile="+route.profile.Count+", speed limits="+route.speed_limits.Count);}
 void OpenRoute(){var uiThread=new System.Threading.Thread(()=>{try{var window=new DosRouteWindow(route);window.StartAtStation=n=>BeginInvoke(new Action(()=>{position=(float)route.X(route.stations[n].km);stateTrain(position,0,2,76);F(mem["TInputParamPult"],1,0);klubBasisReady=false;if(routeFinished){routeFinished=false;timer.Start();}RouteInputCycle();Log("Initial station="+route.stations[n].name+" physical x="+position);}));window.FormClosing+=(s,e)=>{if(e.CloseReason==CloseReason.UserClosing){e.Cancel=true;window.Hide();}};routeWindow=window;Application.Run(window);}catch(Exception ex){Log("Route window error: "+ex);}});uiThread.IsBackground=true;uiThread.SetApartmentState(System.Threading.ApartmentState.STA);uiThread.Start();var b=new Button{Text="Режимная карта",Left=610,Top=188,Width=175};b.Click+=(s,e)=>{var w=routeWindow;if(w!=null&&w.IsHandleCreated)w.BeginInvoke(new Action(()=>{w.Show();w.Activate();}));};Controls.Add(b);}
 void CloseRoute(){var w=routeWindow;if(w!=null&&w.IsHandleCreated)w.BeginInvoke(new Action(()=>{w.Dispose();Application.ExitThread();}));}
 void ConfigureNativeRoute(IntPtr ph){var set=Export<ProfFn>(ph,"SetProf");foreach(var p in route.profile){int a=(int)Math.Round(route.X(p.km_start)),b=(int)Math.Round(route.X(p.km_end));set(b-a,(float)p.grade_permille,route.profile.Count);}Export<PlanFn>(ph,"SetPlan")(0,(int)Math.Round(route.Length),10000000,1);position=(float)route.X(route.initial_km);Log("POEZD: PDF gradients loaded, initial km="+route.initial_km+"; curves not modeled unless verified separately");}
 void ConfigureAuxRoute(IntPtr calc,IntPtr saut,IntPtr avp){foreach(var p in route.profile){var rec=Alloc(12);F(rec,0,(float)route.X(p.km_start));F(rec,4,(float)(route.X(p.km_end)-route.X(p.km_start)));F(rec,8,(float)p.grade_permille);Export<AddRecordFn>(calc,"AddProfilCalcOgr")(rec);Export<AddRecordFn>(saut,"AddProfilSaut")(rec);}foreach(var p in route.speed_limits){int a=(int)Math.Round(route.X(p.km_start)),b=(int)Math.Round(route.X(p.km_end));var limit=new RouteLimit{Start=a,End=b,Pass=p.speed_kmh,Freight=p.speed_kmh,Suburban=p.speed_kmh,Special=p.speed_kmh,Flag=1,Klub=1,Saut=1,Allowed=p.speed_kmh,Target=p.speed_kmh,AllowedSaut=p.speed_kmh,TargetSaut=p.speed_kmh};var rec=Alloc(52);Marshal.StructureToPtr(limit,rec,false);Export<AddRecordFn>(calc,"AddOgranCalcOgr")(rec);Export<AddRecordFn>(saut,"AddOgranSaut")(rec);Export<AvpLimitFn>(avp,"AddOgran")(limit);}Log("PDF route profiles/limits sent to CalcOgr, SAUT, AVP; advice does not drive controller");}
 void RouteInputCycle(){if(route==null)return;if((position<0||position>route.Length)&&!routeFinished){routeFinished=true;if(timer!=null)timer.Stop();Log("Route boundary reached; simulation paused at x="+position);}var lim=route.Limit(position);if(lim!=null){I(mem["TInputParamPult"],5,lim.speed_kmh);I(mem["TInputParamPult"],9,lim.speed_kmh);if(lastRouteLimit!=lim.speed_kmh){lastRouteLimit=lim.speed_kmh;Log("PDF speed limit="+lastRouteLimit+" at km="+route.Km(position));}}}
 // Layout verified for the bundled KLUB DLL through RTTI and GetParamInToKLUB.
 // Keep electronic-map mode off: the source is a graphical PDF, not a KLUB map.
 void KlubCoordinateCycle(){bool powered=Marshal.ReadByte(mem["TPBBtn"],4)==1;if(!powered){klubBasisReady=false;klubWasPowered=false;return;}if(klubBasisReady&&klubWasPowered)return;IntPtr current,initial;Export<KlubPointersFn>(klubModule,"GetParamInToKLUB")(out current,out initial);if(current==IntPtr.Zero||initial==IntPtr.Zero)throw new Exception("KLUB coordinate state absent");IntPtr owner=IntPtr.Subtract(initial,8);if(current!=IntPtr.Add(owner,0x91))throw new Exception("KLUB coordinate layout differs from verified build");var coord=IntPtr.Add(initial,0x34);int railMeters=(int)Math.Round(route.Km(position)*1000),physicalMeters=(int)Math.Round(position);B(initial,0,0);I(coord,0,railMeters);I(coord,4,railMeters);I(coord,8,physicalMeters);B(coord,0xc,1);I(coord,0xd,physicalMeters);Dbl(coord,0x11,0);I(coord,0x19,railMeters);I(owner,0xfd,1);B(owner,0x101,1);klubBasisReady=true;klubWasPowered=true;Log("KLUB native coordinate basis: railway="+railMeters+" physical="+physicalMeters+" decreasing km, no electronic map");}
 bool routePreviewSaved;
 void RouteViewCycle(){var w=routeWindow;if(w==null||!w.IsHandleCreated)return;var t=mem["TTygaParam"];var live=new RouteLive{x=position,v=FloatAt(mem["TInputParamPult"],1),force=FloatAt(mem["TResTyga"],0)*10,current=FloatAt(t,0x5b),excitation=FloatAt(t,0x9b),position=Marshal.ReadInt32(t,0xc),connection=Marshal.ReadByte(t,0xdc),tm=FloatAt(cars[0],0),tc=FloatAt(cars[0],4),tailTm=FloatAt(cars[75],0),tailTc=FloatAt(cars[75],4),gr=FloatAt(mem["TLocParam"],5),ur=FloatAt(mem["TLocParam"],9),accel=acceleration,tick=ticks,limit=lastRouteLimit,fault=electricalFault,finished=routeFinished};w.BeginInvoke(new Action(()=>{w.View.UpdateLive(live);if((live.tick>=20&&!routePreviewSaved)||live.tick%100==0){w.View.SaveRender(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Route_DOS_preview.png"));routePreviewSaved=true;}}));}

}
