using System;
using System.IO;
using System.Drawing;
using System.Windows.Forms;
class ViewPreview {
 [STAThread] static void Main(){
  var r=RouteModel.Load(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"..","data","route_enriched.json"));
  var v=new DosRouteSurface{Route=r,Width=1360,Height=838,PdfMode=false};
  v.UpdateLive(new RouteLive{x=(float)r.X(2116.2),v=62.7f,force=474,current=505,excitation=550,position=23,connection=2,tm=5,tc=0,tailTm=4.98f,tailTc=0,gr=8.4f,ur=5,accel=.03f,tick=4100,limit=80});
  for(float x=v.Live.x-7000;x<v.Live.x;x+=40)v.Trace.Add(new RouteTrace{x=x,v=(float)(60+9*Math.Sin(x/1700))});
  string dir=AppDomain.CurrentDomain.BaseDirectory;
  v.SaveRender(Path.Combine(dir,"modern_dark.png"));v.ThemeName="Светлая";v.SaveRender(Path.Combine(dir,"modern_light.png"));v.ThemeName="Контрастная";v.SaveRender(Path.Combine(dir,"modern_contrast.png"));
  v.ThemeName="Тёмная";v.Width=1080;v.Height=728;v.SaveRender(Path.Combine(dir,"modern_minimum.png"));v.Dispose();
 }
}
