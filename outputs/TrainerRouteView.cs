using System;
using System.IO;
using System.Linq;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Collections.Generic;
using System.Globalization;
using System.Windows.Forms;
using System.Web.Script.Serialization;

// The chart uses PDF coordinates and document symbols. The original page is
// available separately as a reference, never as a source of live ALSN codes.
public sealed class RoutePalette {
 public Color Background,Card,Inset,Grid,Text,Muted,Accent,Traction,Braking,Gold;
 public static RoutePalette For(string name) {
  if(name=="Светлая")return new RoutePalette{Background=Color.FromArgb(237,242,248),Card=Color.White,Inset=Color.FromArgb(243,247,252),Grid=Color.FromArgb(216,226,237),Text=Color.FromArgb(27,42,61),Muted=Color.FromArgb(93,112,135),Accent=Color.FromArgb(0,118,117),Traction=Color.FromArgb(190,50,79),Braking=Color.FromArgb(30,104,201),Gold=Color.FromArgb(153,105,0)};
  if(name=="Контрастная")return new RoutePalette{Background=Color.Black,Card=Color.Black,Inset=Color.FromArgb(14,14,14),Grid=Color.FromArgb(99,99,99),Text=Color.White,Muted=Color.FromArgb(219,219,219),Accent=Color.Cyan,Traction=Color.FromArgb(255,103,113),Braking=Color.FromArgb(97,164,255),Gold=Color.Yellow};
  return new RoutePalette{Background=Color.FromArgb(9,15,25),Card=Color.FromArgb(18,28,43),Inset=Color.FromArgb(23,35,52),Grid=Color.FromArgb(44,60,79),Text=Color.FromArgb(235,243,254),Muted=Color.FromArgb(152,172,196),Accent=Color.FromArgb(93,225,203),Traction=Color.FromArgb(255,116,137),Braking=Color.FromArgb(117,174,255),Gold=Color.FromArgb(255,205,111)};
 }
}
public class RouteViewSettings {
 public string theme="Тёмная",mode="Как в PDF";
 public bool follow=true;public double span=18000;
}
public class DosRouteSurface:Control {
 public RouteModel Route;public RouteLive Live=new RouteLive();
 public double Span=18000,Pan=-1;public bool Follow=true,PdfMode=false;
 public int DocumentPage=3;public List<RouteTrace> Trace=new List<RouteTrace>();
 string themeName="Тёмная";RoutePalette palette=RoutePalette.For("Тёмная");
 readonly Dictionary<int,Bitmap> pages=new Dictionary<int,Bitmap>();
 readonly ToolTip tips=new ToolTip{AutoPopDelay=15000,InitialDelay=350,ReshowDelay=150};
 readonly List<RouteMapHit> hits=new List<RouteMapHit>();string tipText="";int lastGeneration=-1;
 readonly Font title=new Font("Segoe UI",17,FontStyle.Bold),heading=new Font("Segoe UI",11,FontStyle.Bold),body=new Font("Segoe UI",10),small=new Font("Segoe UI",8.5f),metric=new Font("Segoe UI",30,FontStyle.Bold);
 public string ThemeName {get{return themeName;}set{themeName=value;palette=RoutePalette.For(value);ClearPages();BackColor=palette.Background;ForeColor=palette.Text;Invalidate();}}
 public DosRouteSurface(){SetStyle(ControlStyles.AllPaintingInWmPaint|ControlStyles.UserPaint|ControlStyles.OptimizedDoubleBuffer|ControlStyles.ResizeRedraw,true);BackColor=palette.Background;ForeColor=palette.Text;MouseMove+=(s,e)=>{var hit=hits.LastOrDefault(p=>p.Rect.Contains(e.Location));string text=hit==null?"":hit.Text;if(text!=tipText){tipText=text;tips.SetToolTip(this,text);}};MouseLeave+=(s,e)=>{tipText="";tips.SetToolTip(this,"");};}
 sealed class RouteMapHit{public RectangleF Rect;public string Text;}
 void Hit(RectangleF rect,string text){hits.Add(new RouteMapHit{Rect=rect,Text=text});}
 static bool Finite(float value){return !float.IsNaN(value)&&!float.IsInfinity(value);}
 public void UpdateLive(RouteLive value){if(value==null||value.generation<lastGeneration)return;if(value.generation!=lastGeneration){Trace.Clear();lastGeneration=value.generation;}Live=value;if(value.tick%10==0&&Finite(value.x)&&Finite(value.v)){if(Trace.Count==0||Trace[Trace.Count-1].x!=value.x||Math.Abs(Trace[Trace.Count-1].v-value.v)>.005){Trace.Add(new RouteTrace{x=value.x,v=value.v,force=value.force,tc=value.tc});if(Trace.Count>12000)Trace.RemoveAt(0);}}Invalidate();}
 public int PageForPosition(){if(Route==null)return 3;if(!Finite(Live.x))return Math.Max(3,Math.Min(10,DocumentPage));double points=112.791+Math.Max(0,Math.Min(Route.Length,Live.x))/1000*28.3465;return Math.Max(3,Math.Min(10,3+(int)Math.Floor(points/805.86)));}
 protected override void OnPaint(PaintEventArgs e){base.OnPaint(e);Render(e.Graphics,ClientSize);}
 static Color Blend(Color a,Color b,float factor){factor=Math.Max(0,Math.Min(1,factor));return Color.FromArgb((int)(a.R+(b.R-a.R)*factor),(int)(a.G+(b.G-a.G)*factor),(int)(a.B+(b.B-a.B)*factor));}
 static GraphicsPath Round(RectangleF r,float radius){var p=new GraphicsPath();radius=Math.Max(1,Math.Min(radius,Math.Min(r.Width,r.Height)/2));float d=radius*2;p.AddArc(r.X,r.Y,d,d,180,90);p.AddArc(r.Right-d,r.Y,d,d,270,90);p.AddArc(r.Right-d,r.Bottom-d,d,d,0,90);p.AddArc(r.X,r.Bottom-d,d,d,90,90);p.CloseFigure();return p;}
 void Card(Graphics g,RectangleF r,Color fill){using(var path=Round(r,12))using(var brush=new SolidBrush(fill))g.FillPath(brush,path);using(var path=Round(r,12))using(var pen=new Pen(palette.Grid,.8f))g.DrawPath(pen,path);}
 void DrawText(Graphics g,string text,RectangleF r,Font font,Color c,StringAlignment align){using(var b=new SolidBrush(c))using(var f=new StringFormat{Alignment=align,LineAlignment=StringAlignment.Center,Trimming=StringTrimming.EllipsisCharacter,FormatFlags=StringFormatFlags.NoWrap})g.DrawString(text??"",font,b,r,f);}
 void DrawText(Graphics g,string text,float x,float y,float w,float h,Font font,Color color){DrawText(g,text,new RectangleF(x,y,w,h),font,color,StringAlignment.Near);}
 void Line(Graphics g,Color c,float x1,float y1,float x2,float y2,float width){using(var p=new Pen(c,width)){p.StartCap=LineCap.Round;p.EndCap=LineCap.Round;g.DrawLine(p,x1,y1,x2,y2);}}
 void Pill(Graphics g,string text,RectangleF r,Color color){using(var path=Round(r,6))using(var b=new SolidBrush(Blend(palette.Card,color,.13f)))g.FillPath(b,path);DrawText(g,text,r,small,color,StringAlignment.Center);}
 string Connection(){return Live.connection==1?"С":Live.connection==2?"СП":Live.connection==3?"П":"—";}
 public void Render(Graphics g,Size size){
  hits.Clear();g.Clear(palette.Background);g.SmoothingMode=SmoothingMode.AntiAlias;g.TextRenderingHint=TextRenderingHint.ClearTypeGridFit;
  if(Route==null){DrawText(g,"Загрузка режимной карты",24,24,size.Width-48,40,title,palette.Text);return;}
  float margin=18,gap=16,hudW=Math.Max(234,Math.Min(268,size.Width*.21f)),mainW=size.Width-margin*2-gap-hudW,hudX=margin+mainW+gap;
  DrawText(g,"Войновка → Кокшаровский",margin,10,mainW,34,title,palette.Text);
  DrawText(g,"2ЭС6 · режимная карта 2018 · километраж убывает",margin,43,mainW,25,body,palette.Muted);
  Pill(g,"75 вагонов · 6000 т",new RectangleF(hudX,14,hudW,28),palette.Accent);
  DrawText(g,"Электровоз 200 т · всего 6200 т",hudX,45,hudW,20,small,palette.Muted);
  var mapRect=new RectangleF(margin,82,mainW,Math.Max(420,size.Height-82-139));
  Card(g,mapRect,palette.Card);
  if(!Finite(Live.x))DrawText(g,"Модель передала недопустимую координату. Отображение пути приостановлено.",mapRect.X+18,mapRect.Y+20,mapRect.Width-36,35,body,palette.Traction);
  else if(PdfMode)RenderDocument(g,mapRect);else RenderVector(g,mapRect);
  RenderAdvice(g,new RectangleF(margin,mapRect.Bottom+12,mainW,106));
  RenderHud(g,new RectangleF(hudX,82,hudW,size.Height-100));
 }
 Bitmap GetPage(int page){
  Bitmap found;if(pages.TryGetValue(page,out found))return found;
  string path=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"..","work","dos_route","pdf","page-"+page.ToString("00")+".png"));
  if(!File.Exists(path))return null;
  var result=new Bitmap(1,1);try{
   using(var original=Image.FromFile(path)){result.Dispose();result=new Bitmap(original.Width,original.Height,PixelFormat.Format32bppArgb);using(var g=Graphics.FromImage(result))g.DrawImageUnscaled(original,0,0);}
   pages[page]=result;return result;
  }catch{result.Dispose();return null;}
 }
 void RenderDocument(Graphics g,RectangleF r){
  int page=Follow?PageForPosition():Math.Max(3,Math.Min(10,DocumentPage));DocumentPage=page;
  DrawText(g,"Режимная карта — исходный лист",r.X+16,r.Y+10,r.Width-192,27,heading,palette.Text);
  Pill(g,"PDF · стр. "+page+" / 10",new RectangleF(r.Right-158,r.Y+12,141,23),palette.Muted);
  Bitmap img=GetPage(page);if(img==null){DrawText(g,"Лист PDF недоступен — показан профиль пути",r.X+16,r.Y+43,r.Width-32,24,small,palette.Gold);RenderVector(g,new RectangleF(r.X,r.Y+64,r.Width,r.Height-64));return;}
  var area=new RectangleF(r.X+10,r.Y+46,r.Width-20,r.Height-78);float scale=Math.Min(area.Width/img.Width,area.Height/img.Height);
  float iw=img.Width*scale,ih=img.Height*scale;var target=new RectangleF(area.X+(area.Width-iw)/2,area.Y+(area.Height-ih)/2,iw,ih);
  g.InterpolationMode=InterpolationMode.HighQualityBicubic;g.DrawImage(img,target);g.InterpolationMode=InterpolationMode.Default;
  double localPoints=112.791+Live.x/1000*28.3465-805.86*(page-3),x=target.X+localPoints/842*target.Width;
  if(x>=target.X&&x<=target.Right){Line(g,palette.Accent,(float)x,target.Y+20,(float)x,target.Bottom-15,1.7f);using(var b=new SolidBrush(palette.Accent))g.FillPolygon(b,new PointF[]{new PointF((float)x,target.Y+23),new PointF((float)x-5,target.Y+13),new PointF((float)x+5,target.Y+13)});Pill(g,Route.Km(Live.x).ToString("0.000",CultureInfo.InvariantCulture)+" км",new RectangleF((float)Math.Max(target.X,Math.Min(target.Right-103,x-51)),target.Y-8,103,20),palette.Accent);}
  DrawText(g,"Все знаки сохранены из PDF. Цветные светофоры — схема документа.",r.X+16,r.Bottom-26,r.Width-32,18,small,palette.Muted);
 }
 void RenderVector(Graphics g,RectangleF r){
  float axis=63,left=r.X+axis,right=r.Right-18,w=right-left;
  double requestedSpan=double.IsNaN(Span)||double.IsInfinity(Span)?18000:Span;
  double span=Math.Max(Math.Min(3000,Route.Length),Math.Min(Route.Length,requestedSpan)),center=Follow?Live.x:Pan;if(center<0||double.IsNaN(center)||double.IsInfinity(center))center=Live.x;
  double start=Math.Max(0,Math.Min(Route.Length-span,center-span*.18)),end=Math.Min(Route.Length,start+span);if(end<=start)end=start+1;
  Func<double,float> px=x=>left+(float)((x-start)/(end-start)*w);
  float top=r.Y+12,kmH=29,speedH=Math.Max(99,r.Height*.23f),rowH=Math.Max(33,r.Height*.067f),stationH=29,gradeH=25;
  float speedTop=top+kmH+6,speedBottom=speedTop+speedH,recTop=speedBottom+11,stationTop=recTop+rowH*3+7,profTop=stationTop+stationH,profBottom=r.Bottom-gradeH-88;
  using(var b=new SolidBrush(palette.Inset))g.FillRectangle(b,left,top,w,kmH);
  int kmStep=span<=12000?1:span<=22000?2:span<=45000?5:10;
  for(int km=(int)Math.Floor(Route.Km(start));km>=Route.Km(end);km--){double sx=Route.X(km);float xx=px(sx);Line(g,palette.Grid,xx,top,xx,profBottom,.6f);if(km%kmStep==0)DrawText(g,km.ToString(),new RectangleF(xx-24,top,48,kmH),small,palette.Text,StringAlignment.Center);}
  DrawText(g,"км",r.X+15,top,42,kmH,small,palette.Muted);
  Func<double,float> vy=v=>speedBottom-(float)(v/100*speedH);
  for(int v=0;v<=90;v+=10){float y=vy(v);Line(g,palette.Grid,left,y,right,y,.7f);DrawText(g,v.ToString(),new RectangleF(r.X+14,y-10,35,20),small,palette.Muted,StringAlignment.Far);}
  RouteSpeed previous=null;foreach(var p in Route.speed_limits){double a=Route.X(p.km_start),b=Route.X(p.km_end);if(b>=start&&a<=end){float x1=px(Math.Max(start,a)),x2=px(Math.Min(end,b)),y=vy(p.speed_kmh);Line(g,palette.Traction,x1,y,x2,y,2.3f);if(previous!=null&&a>=start&&a<=end)Line(g,palette.Traction,x1,vy(previous.speed_kmh),x1,y,2.3f);if(x2-x1>42)DrawText(g,p.speed_kmh.ToString(),new RectangleF((x1+x2)/2-20,y-24,40,20),small,palette.Traction,StringAlignment.Center);}previous=p;}
  RouteTrace old=null;foreach(var p in Trace){if(!Finite(p.x)||!Finite(p.v)||p.x<start||p.x>end){old=null;continue;}if(old!=null&&Math.Abs(p.x-old.x)<500)Line(g,palette.Accent,px(old.x),vy(old.v),px(p.x),vy(p.v),2);old=p;}
  DrawText(g,"км/ч",r.X+12,speedTop-9,45,20,small,palette.Muted);
  int[] weights={8000,6300,2500};for(int row=0;row<3;row++){
   float y=recTop+row*rowH;if(row==1){using(var b=new SolidBrush(Blend(palette.Card,palette.Gold,.055f)))g.FillRectangle(b,r.X+8,y-3,r.Width-16,rowH);}
   DrawText(g,weights[row].ToString(),r.X+12,y,47,22,small,row==1?palette.Gold:palette.Muted);DrawText(g,"т",r.X+12,y+19,35,16,small,palette.Muted);
   Line(g,palette.Grid,left,y+rowH-8,right,y+rowH-8,.7f);
   foreach(var a in Route.recommendations.Where(a=>a.weight_t==weights[row])){double x1=Route.X(a.km_start),x2=Route.X(a.km_end);if(x2<start||x1>end)continue;float xx1=px(Math.Max(start,x1)),xx2=px(Math.Min(end,x2)),yy=y+rowH-8;bool braking=a.text!=null&&a.text.IndexOf("тормож",StringComparison.OrdinalIgnoreCase)>=0;Color color=braking?palette.Braking:palette.Traction;using(var pen=new Pen(color,3)){pen.StartCap=LineCap.Round;pen.EndCap=LineCap.Round;if(braking)pen.DashStyle=DashStyle.Dash;g.DrawLine(pen,xx1,yy,xx2,yy);}if(xx2-xx1>53){string txt=(a.text??"").Split(';')[0];DrawText(g,txt,new RectangleF(xx1+2,y,xx2-xx1-4,rowH-13),small,color,StringAlignment.Center);}Hit(new RectangleF(xx1,y,Math.Max(6,xx2-xx1),rowH),weights[row]+" т · "+a.text+"\nPDF стр. "+a.page+" · границы токовых рекомендаций приблизительные");}
  }
  Line(g,palette.Grid,left,stationTop-1,right,stationTop-1,.8f);
  double lo=double.MaxValue,hi=double.MinValue;var xs=new List<double>{start,end};foreach(var p in Route.profile){double x=Route.X(p.km_end);if(x>start&&x<end)xs.Add(x);}foreach(double x in xs){double z=Route.Height(x);lo=Math.Min(lo,z);hi=Math.Max(hi,z);}hi=Math.Max(lo+8,hi);
  Func<double,float> py=x=>profBottom-14-(float)((Route.Height(x)-lo)/(hi-lo)*Math.Max(30,profBottom-profTop-25));
  var points=xs.OrderBy(x=>x).Select(x=>new PointF(px(x),py(x))).ToList();if(points.Count>1){var area=points.ToList();area.Add(new PointF(px(end),profBottom));area.Add(new PointF(px(start),profBottom));using(var brush=new SolidBrush(Color.FromArgb(22,palette.Gold)))g.FillPolygon(brush,area.ToArray());using(var pen=new Pen(palette.Gold,2.7f))g.DrawLines(pen,points.ToArray());}
  DrawText(g,"путь",r.X+12,profTop+9,45,20,small,palette.Muted);
  foreach(var p in Route.profile){double a=Math.Max(start,Route.X(p.km_start)),b=Math.Min(end,Route.X(p.km_end));if(b<=a)continue;float x1=px(a),x2=px(b);Line(g,palette.Grid,x1,py(a),x1,profBottom+gradeH,.7f);if(x2-x1>34){string txt=p.grade_permille.ToString("+0.0;-0.0;0.0",CultureInfo.InvariantCulture);DrawText(g,txt,new RectangleF(x1,profBottom+2,x2-x1,gradeH-3),small,palette.Gold,StringAlignment.Center);}}
  foreach(var p in Route.stations){double x=Route.X(p.km);if(x<start||x>end)continue;float xx=px(x);Line(g,palette.Muted,xx,stationTop+20,xx,py(x),.9f);using(var b=new SolidBrush(palette.Card))g.FillEllipse(b,xx-4,py(x)-4,8,8);using(var pen=new Pen(palette.Gold,1.5f))g.DrawEllipse(pen,xx-4,py(x)-4,8,8);DrawText(g,p.name,new RectangleF(Math.Max(left,Math.Min(right-100,xx-50)),stationTop,100,23),small,palette.Text,StringAlignment.Center);}
  foreach(var signal in Route.signals){double sx=Route.X(signal.km);if(sx<start||sx>end)continue;float xx=px(sx),yy=py(sx)-18;bool station=signal.type=="station_signal";int count=station?3:1;Line(g,palette.Muted,xx,yy+4,xx,py(sx),.8f);for(int n=0;n<count;n++){using(var b=new SolidBrush(palette.Card))g.FillEllipse(b,xx-3,yy-n*7-3,6,6);using(var pen=new Pen(palette.Muted,1.1f))g.DrawEllipse(pen,xx-3,yy-n*7-3,6,6);}DrawText(g,signal.label,new RectangleF(xx-19,yy-count*7-15,38,16),small,palette.Muted,StringAlignment.Center);Hit(new RectangleF(xx-8,yy-count*7-17,16,count*7+35),"Схема светофора "+signal.label+" · "+signal.km.ToString("0.000")+" км\nPDF стр. "+signal.page+". Кружки не отображают текущее показание АЛСН.");}
  float curveY=profBottom+gradeH+9;Line(g,palette.Grid,left,curveY+18,right,curveY+18,.6f);DrawText(g,"R, м",r.X+10,curveY-2,44,20,small,palette.Muted);
  float lastCurveLabel=left-60;foreach(var curve in Route.curves.OrderByDescending(p=>p.km_start)){double a=Route.X(curve.km_start),b=Route.X(curve.km_end);if(b<start||a>end)continue;float x1=px(Math.Max(start,a)),x2=px(Math.Min(end,b));using(var pen=new Pen(palette.Braking,1.3f))g.DrawLines(pen,new PointF[]{new PointF(x1,curveY+18),new PointF(x1,curveY+9),new PointF(x2,curveY+9),new PointF(x2,curveY+18)});float middle=(x1+x2)/2;if(middle-lastCurveLabel>50){DrawText(g,curve.radius_m.ToString("0"),new RectangleF(middle-24,curveY-8,48,17),small,palette.Braking,StringAlignment.Center);lastCurveLabel=middle;}Hit(new RectangleF(x1-3,curveY-8,Math.Max(7,x2-x1+6),30),"Кривая R="+curve.radius_m.ToString("0")+" м · длина по схеме ≈ "+curve.length_m.ToString("0")+" м\nPDF стр. "+curve.page);}
  foreach(var curve in Route.unknown_curves){double a=Route.X(curve.km_start),b=Route.X(curve.km_end);if(b<start||a>end)continue;float x1=px(Math.Max(start,a)),x2=px(Math.Min(end,b));using(var pen=new Pen(palette.Gold,1.5f){DashStyle=DashStyle.Dash})g.DrawLines(pen,new PointF[]{new PointF(x1,curveY+18),new PointF(x1,curveY+9),new PointF(x2,curveY+9),new PointF(x2,curveY+18)});DrawText(g,"R?",new RectangleF((x1+x2)/2-16,curveY-8,32,17),small,palette.Gold,StringAlignment.Center);Hit(new RectangleF(x1-4,curveY-8,Math.Max(8,x2-x1+8),30),"Радиус в PDF не подписан; сопротивление этой кривой не рассчитано.\nPDF стр. "+curve.page+" · "+curve.km_start.ToString("0.000")+"–"+curve.km_end.ToString("0.000")+" км");}
  float eventY=curveY+28;Line(g,palette.Grid,left,eventY+17,right,eventY+17,.6f);DrawText(g,"знаки",r.X+10,eventY-1,44,20,small,palette.Muted);float lastEventX=left-30;int eventRow=0;
  foreach(var item in Route.events.OrderByDescending(p=>p.km)){double sx=Route.X(item.km);if(sx<start||sx>end)continue;float xx=px(sx);eventRow=xx-lastEventX<20?1-eventRow:0;float yy=eventY+5-eventRow*10;Line(g,palette.Muted,xx,yy+9,xx,eventY+17,.7f);DrawEvent(g,item.type,new RectangleF(xx-7,yy-6,14,14));Hit(new RectangleF(xx-9,yy-8,18,22),item.label+" · "+item.km.ToString("0.000")+" км\nСтатический знак карты · PDF стр. "+item.page);lastEventX=xx;}
  if(Live.x>=start&&Live.x<=end){float xx=px(Live.x);Line(g,palette.Accent,xx,top,xx,profBottom+gradeH,1.5f);using(var b=new SolidBrush(palette.Accent))g.FillPolygon(b,new PointF[]{new PointF(xx,top+6),new PointF(xx-5,top-2),new PointF(xx+5,top-2)});if(Finite(Live.v))using(var b=new SolidBrush(palette.Accent))g.FillEllipse(b,xx-4,vy(Math.Max(0,Math.Min(100,Live.v)))-4,8,8);}
  DrawText(g,"‰",r.X+15,profBottom,40,gradeH,small,palette.Muted);
  float legendY=r.Bottom-23;Line(g,palette.Traction,left,legendY+8,left+20,legendY+8,3);DrawText(g,"тяга / допуск PDF",left+26,legendY-2,148,22,small,palette.Muted);using(var pen=new Pen(palette.Braking,3){DashStyle=DashStyle.Dash})g.DrawLine(pen,left+176,legendY+8,left+196,legendY+8);DrawText(g,"электротормоз",left+202,legendY-2,112,22,small,palette.Muted);Line(g,palette.Accent,left+334,legendY+8,left+354,legendY+8,2);DrawText(g,"поезд",left+360,legendY-2,70,22,small,palette.Muted);DrawText(g,"Знаки: наведите мышь",right-160,legendY-2,160,22,small,palette.Muted);
 }
 void DrawEvent(Graphics g,string type,RectangleF r){Color color=palette.Muted;using(var pen=new Pen(color,1.25f)){
  if(type=="bridge"){g.DrawRectangle(pen,r.X,r.Y+4,r.Width,6);Line(g,color,r.X+3,r.Bottom-1,r.X+3,r.Y+10,1);Line(g,color,r.Right-3,r.Bottom-1,r.Right-3,r.Y+10,1);}
  else if(type=="guarded_crossing"||type=="unguarded_crossing"){g.DrawLine(pen,r.X+1,r.Y+1,r.Right-1,r.Bottom-1);g.DrawLine(pen,r.Right-1,r.Y+1,r.X+1,r.Bottom-1);if(type=="guarded_crossing")g.DrawLine(pen,r.X,r.Y+7,r.Right,r.Y+7);}
  else if(type=="suburban_stop"){g.DrawRectangle(pen,r.X+1,r.Y+3,r.Width-2,r.Height-6);g.DrawLine(pen,r.X,r.Bottom,r.Right,r.Bottom);}
  else if(type=="uksps"||type=="ktsm"){g.DrawRectangle(pen,r.X,r.Y,r.Width,r.Height);DrawText(g,type=="uksps"?"У":"К",r,small,color,StringAlignment.Center);}
  else if(type=="brake_test"){g.DrawEllipse(pen,r);DrawText(g,"Т",r,small,palette.Gold,StringAlignment.Center);}
  else if(type=="blocking_signal"){g.DrawRectangle(pen,r.X+2,r.Y+1,r.Width-4,r.Height-3);using(var brush=new SolidBrush(palette.Card))g.FillRectangle(brush,r.X+3,r.Y+2,(r.Width-5)/2,r.Height-5);}
  else {g.DrawPolygon(pen,new PointF[]{new PointF(r.X+r.Width/2,r.Y),new PointF(r.Right,r.Bottom),new PointF(r.X,r.Bottom)});string text=type=="inspection_driver"?"М":type=="inspection_assistant"?"П":type=="inspection_crew"?"Б":"!";DrawText(g,text,new RectangleF(r.X,r.Y+2,r.Width,r.Height),small,color,StringAlignment.Center);}
 }}
 void RenderAdvice(Graphics g,RectangleF r){
  Card(g,r,palette.Card);DrawText(g,"Рекомендация карты · строка 6300 т",r.X+15,r.Y+10,r.Width-30,24,heading,palette.Gold);
  var advice=Route.Advice(Live.x);string text=advice==null?"На этом участке ток и соединение ТЭД в карте не указаны":advice.text;
  DrawText(g,text,r.X+15,r.Y+40,r.Width-30,27,body,palette.Text);
  DrawText(g,"Для состава 6000 т — ближайшая строка PDF; управление остаётся на штатном пульте.",r.X+15,r.Y+74,r.Width-30,19,small,palette.Muted);
 }
 void MetricRow(Graphics g,string label,string value,float x,float y,float w){DrawText(g,label,x,y,w*.53f,27,small,palette.Muted);DrawText(g,value,new RectangleF(x+w*.53f,y,w*.47f,27),body,palette.Text,StringAlignment.Far);}
 void RenderHud(Graphics g,RectangleF r){
  Card(g,r,palette.Card);float x=r.X+16,w=r.Width-32,y=r.Y+13;
  DrawText(g,"Данные штатной модели",x,y,w,23,heading,palette.Text);y+=31;
  DrawText(g,Live.v.ToString("0.0",CultureInfo.InvariantCulture),x,y,w-55,49,metric,Live.limit>0&&Live.v>Live.limit+.1?palette.Traction:palette.Accent);DrawText(g,"км/ч",r.Right-62,y+19,48,24,body,palette.Muted);y+=59;
  Pill(g,"Допуск карты  "+(Live.limit<0?"—":Live.limit.ToString())+" км/ч",new RectangleF(x,y,w,28),palette.Traction);y+=39;
  MetricRow(g,"Километр ~",Route.Km(Live.x).ToString("0.000",CultureInfo.InvariantCulture),x,y,w);y+=27;
  var grade=Route.Grade(Live.x);MetricRow(g,"Уклон",grade==null?"—":grade.grade_permille.ToString("+0.0;-0.0;0.0",CultureInfo.InvariantCulture)+" ‰",x,y,w);y+=27;
  MetricRow(g,"Позиция / ТЭД",Live.position+" / "+Connection(),x,y,w);y+=32;
  Line(g,palette.Grid,x,y,x+w,y,.8f);y+=9;
  MetricRow(g,"Ток якоря",Live.current.ToString("0")+" А",x,y,w);y+=27;
  MetricRow(g,"Возбуждение",Live.excitation.ToString("0")+" А",x,y,w);y+=27;
  MetricRow(g,"Тяга / ЭДТ",Live.force.ToString("0.0")+" кН",x,y,w);y+=32;
  Line(g,palette.Grid,x,y,x+w,y,.8f);y+=9;
  MetricRow(g,"ТМ / ТЦ",Live.tm.ToString("0.00")+" / "+Live.tc.ToString("0.00"),x,y,w);y+=27;
  MetricRow(g,"Хвост: ТМ / ТЦ",Live.tailTm.ToString("0.00")+" / "+Live.tailTc.ToString("0.00"),x,y,w);y+=27;
  MetricRow(g,"ГР / УР, бар",Live.gr.ToString("0.0")+" / "+Live.ur.ToString("0.0"),x,y,w);y+=27;
  MetricRow(g,"Ускорение",Live.accel.ToString("+0.000;-0.000;0.000",CultureInfo.InvariantCulture)+" м/с²",x,y,w);y+=32;
  var next=Route.stations.OrderBy(p=>Route.X(p.km)).FirstOrDefault(p=>Route.X(p.km)>Live.x+2);
  float bottom=r.Bottom-24;float stationY=Math.Max(y,Math.Min(bottom-76,y+24));
  if(stationY+68<bottom){Line(g,palette.Grid,x,stationY-5,x+w,stationY-5,.8f);DrawText(g,"Следующая станция",x,stationY,w,21,small,palette.Muted);DrawText(g,next==null?"Конец карты":next.name,x,stationY+25,w,25,heading,palette.Text);if(next!=null)DrawText(g,((Route.X(next.km)-Live.x)/1000).ToString("0.00")+" км",x,stationY+51,w,22,body,palette.Accent);}
  if(!String.IsNullOrEmpty(Live.fault)){Pill(g,"Сбой: "+Live.fault,new RectangleF(x,r.Bottom-57,w,26),palette.Traction);}else DrawText(g,Live.finished?"Конец карты · выберите станцию":TimeSpan.FromSeconds(Math.Max(0,Live.tick*.1)).ToString(@"hh\:mm\:ss")+" · "+(Follow?"следуем за поездом":"обзор карты"),x,r.Bottom-30,w,18,small,Live.finished?palette.Gold:palette.Muted);
 }
 public void SaveRender(string path){using(var b=new Bitmap(Math.Max(100,Width),Math.Max(100,Height)))using(var g=Graphics.FromImage(b)){Render(g,b.Size);b.Save(path,ImageFormat.Png);}}
 void ClearPages(){foreach(var b in pages.Values)b.Dispose();pages.Clear();}
 protected override void Dispose(bool disposing){if(disposing){ClearPages();tips.Dispose();title.Dispose();heading.Dispose();body.Dispose();small.Dispose();metric.Dispose();}base.Dispose(disposing);}
}
public class DosRouteWindow:Form {
 public DosRouteSurface View;public Action<int> StartAtStation;
 FlowLayoutPanel row1,row2;ComboBox theme,mode,zoom,page;CheckBox follow;bool loading=true;
 string settingsPath=Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_view_settings.json");
 public DosRouteWindow(RouteModel route){
  Text="2ЭС6 · режимная карта Войновка–Кокшаровский";Font=new Font("Segoe UI",10);ClientSize=new Size(1400,930);MinimumSize=new Size(1120,850);StartPosition=FormStartPosition.Manual;Location=new Point(50,70);
  View=new DosRouteSurface{Dock=DockStyle.Fill,Route=route};Controls.Add(View);
  var tools=new Panel{Dock=DockStyle.Top,Height=83,Padding=new Padding(14,7,14,6)};Controls.Add(tools);View.BringToFront();
  row1=new FlowLayoutPanel{Dock=DockStyle.Top,Height=34,WrapContents=false};row2=new FlowLayoutPanel{Dock=DockStyle.Bottom,Height=33,WrapContents=false};tools.Controls.Add(row1);tools.Controls.Add(row2);
  AddLabel(row1,"Тема");theme=Combo(119);theme.Items.AddRange(new object[]{"Тёмная","Светлая","Контрастная"});row1.Controls.Add(theme);
  AddLabel(row1,"Вид");mode=Combo(129);mode.Items.AddRange(new object[]{"Как в PDF","Лист PDF"});row1.Controls.Add(mode);
  follow=new CheckBox{Text="За поездом",AutoSize=true,Margin=new Padding(11,5,13,0),Checked=true};row1.Controls.Add(follow);
  var prev=Button("←",39);var next=Button("→",39);row1.Controls.Add(prev);row1.Controls.Add(next);
  AddLabel(row1,"Масштаб");zoom=Combo(88);zoom.Items.AddRange(new object[]{"8 км","18 км","40 км","Весь путь"});row1.Controls.Add(zoom);
  AddLabel(row1,"Лист");page=Combo(99);for(int p=3;p<=10;p++)page.Items.Add("PDF "+p);row1.Controls.Add(page);
  AddLabel(row2,"Станция");var station=Combo(216);foreach(var x in route.stations)station.Items.Add(x.name);if(station.Items.Count>0)station.SelectedIndex=0;row2.Controls.Add(station);
  var start=Button("Начать со станции",167);row2.Controls.Add(start);
  AddLabel(row2,"Управление — штатный пульт · режимная карта — отдельное окно");
  RouteViewSettings settings=new RouteViewSettings();try{if(File.Exists(settingsPath))settings=new JavaScriptSerializer().Deserialize<RouteViewSettings>(File.ReadAllText(settingsPath))??settings;}catch{}
  if(double.IsNaN(settings.span)||double.IsInfinity(settings.span)||settings.span<=0)settings.span=18000;
  theme.SelectedIndex=Math.Max(0,theme.Items.IndexOf(settings.theme));mode.SelectedIndex=Math.Max(0,mode.Items.IndexOf(settings.mode));follow.Checked=settings.follow;zoom.SelectedIndex=settings.span>=route.Length-1?3:settings.span>30000?2:settings.span<12000?0:1;page.SelectedIndex=0;
  View.Follow=follow.Checked;View.PdfMode=mode.SelectedIndex==1;View.Span=new double[]{8000,18000,40000,route.Length}[zoom.SelectedIndex];View.ThemeName=theme.Text;ApplyTheme();ApplyMode();loading=false;
  theme.SelectedIndexChanged+=(s,e)=>{ApplyTheme();SaveSettings();};
  mode.SelectedIndexChanged+=(s,e)=>{View.PdfMode=mode.SelectedIndex==1;ApplyMode();View.Invalidate();SaveSettings();};
  follow.CheckedChanged+=(s,e)=>{View.Follow=follow.Checked;View.Invalidate();SaveSettings();};
  zoom.SelectedIndexChanged+=(s,e)=>{View.Span=new double[]{8000,18000,40000,route.Length}[zoom.SelectedIndex];View.Invalidate();SaveSettings();};
  page.SelectedIndexChanged+=(s,e)=>{if(page.SelectedIndex<0)return;View.DocumentPage=3+page.SelectedIndex;follow.Checked=false;View.Invalidate();};
  prev.Click+=(s,e)=>Browse(-1);next.Click+=(s,e)=>Browse(1);
  start.Click+=(s,e)=>{if(station.SelectedIndex>=0&&StartAtStation!=null){StartAtStation(station.SelectedIndex);follow.Checked=true;}};
  FormClosing+=(s,e)=>SaveSettings();
 }
 ComboBox Combo(int width){return new ComboBox{Width=width,Height=28,DropDownStyle=ComboBoxStyle.DropDownList,FlatStyle=FlatStyle.Flat,Margin=new Padding(5,1,6,0)};}
 Button Button(string text,int width){return new Button{Text=text,Width=width,Height=29,FlatStyle=FlatStyle.Flat,Margin=new Padding(3,0,3,0),Cursor=Cursors.Hand};}
 void AddLabel(FlowLayoutPanel p,string text){p.Controls.Add(new Label{Text=text,AutoSize=true,Margin=new Padding(5,6,0,0)});}
 void Browse(int direction){if(View.PdfMode){int current=View.Follow?View.PageForPosition():View.DocumentPage;follow.Checked=false;View.DocumentPage=Math.Max(3,Math.Min(10,current+direction));page.SelectedIndex=View.DocumentPage-3;}else{if(View.Follow)View.Pan=View.Live.x;follow.Checked=false;View.Pan=Math.Max(0,Math.Min(View.Route.Length,View.Pan+direction*View.Span*.6));}View.Invalidate();}
 void ApplyMode(){zoom.Enabled=!View.PdfMode;page.Enabled=View.PdfMode;}
 void ApplyTheme(){RoutePalette p=RoutePalette.For(theme.Text);BackColor=p.Background;PaintControls(this,p);View.ThemeName=theme.Text;}
 void PaintControls(Control parent,RoutePalette p){foreach(Control c in parent.Controls){if(c==View)continue;c.BackColor=c is Button||c is ComboBox?p.Inset:p.Background;c.ForeColor=p.Text;var b=c as Button;if(b!=null)b.FlatAppearance.BorderColor=p.Grid;PaintControls(c,p);}}
 void SaveSettings(){if(loading)return;try{double savedSpan=View.Span;if(double.IsNaN(savedSpan)||double.IsInfinity(savedSpan)||savedSpan<=0)savedSpan=18000;var settings=new RouteViewSettings{theme=theme.Text,mode=mode.Text,follow=follow.Checked,span=savedSpan};File.WriteAllText(settingsPath,new JavaScriptSerializer().Serialize(settings));}catch(IOException){}catch(UnauthorizedAccessException){}}
}
