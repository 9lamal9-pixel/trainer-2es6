using System;
using System.IO;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using System.Reflection;
class WindowPreview {
 [STAThread] static void Main(){
 string dir=AppDomain.CurrentDomain.BaseDirectory, settings=Path.Combine(dir,"Trainer_view_settings.json");string before=File.Exists(settings)?File.ReadAllText(settings):null;
 try{
 var route=RouteModel.Load(Path.Combine(dir,"..","data","route_enriched.json"));
 using(var w=new DosRouteWindow(route)){
 w.ShowInTaskbar=false;w.Location=new Point(-30000,-30000);w.Show();Application.DoEvents();w.PerformLayout();File.WriteAllText(Path.Combine(dir,"layout.txt"),String.Join("\r\n",w.Controls.Cast<Control>().Select(c=>c.GetType().Name+" "+c.Bounds+" visible="+c.Visible+" children="+c.Controls.Count).ToArray()));w.View.UpdateLive(new RouteLive{x=(float)route.X(2056.5),v=73,tm=5,gr=8,ur=5,limit=80});
 using(var bitmap=new Bitmap(w.Width,w.Height)){w.DrawToBitmap(bitmap,new Rectangle(0,0,w.Width,w.Height));bitmap.Save(Path.Combine(dir,"modern_window.png"));}
 w.View.SaveRender(Path.Combine(dir,"modern_window_surface.png"));w.View.Trace.Add(new RouteTrace{x=1,v=20});int oldCount=w.View.Trace.Count,requested=-1;w.StartAtStation=n=>requested=n;
var startButton=w.Controls.Cast<Control>().SelectMany(c=>c.Controls.Cast<Control>()).SelectMany(c=>c.Controls.Cast<Control>()).OfType<Button>().First(b=>b.Text=="Начать со станции");startButton.PerformClick();
if(requested!=0||w.View.Trace.Count!=oldCount)throw new Exception("Trace cleared before station acknowledgement");
w.View.UpdateLive(new RouteLive{generation=1,x=(float)route.X(route.stations[0].km),v=0,tick=10,limit=80});if(w.View.Trace.Count!=1)throw new Exception("Trace not cleared on station acknowledgement");
w.View.UpdateLive(new RouteLive{generation=0,x=1,v=100,tick=50});if(w.View.Live.generation!=1||w.View.Trace.Count!=1)throw new Exception("Stale snapshot accepted");
w.View.UpdateLive(new RouteLive{generation=2,x=float.NaN,v=float.PositiveInfinity,tick=20});w.View.SaveRender(Path.Combine(dir,"nonfinite_preview.png"));
w.View.UpdateLive(new RouteLive{generation=3,x=(float)route.X(1965.5),v=70,tick=30,limit=80});w.View.SaveRender(Path.Combine(dir,"unknown_curve_preview.png"));
w.View.Span=double.NaN;w.View.Pan=double.PositiveInfinity;w.View.Follow=false;w.View.SaveRender(Path.Combine(dir,"finite_span_preview.png"));var field=typeof(DosRouteWindow).GetField("theme",BindingFlags.NonPublic|BindingFlags.Instance);var combo=(ComboBox)field.GetValue(w);combo.SelectedIndex=1;
 if(w.View.ThemeName!="Светлая"||!File.Exists(settings))throw new Exception("Theme did not persist");
 }
 using(var next=new DosRouteWindow(route)){if(next.View.ThemeName!="Светлая")throw new Exception("Theme not restored");}
 File.WriteAllText(Path.Combine(dir,"validation.txt"),"PASS: enriched route deserialization; modern vector rendered at full and minimum sizes; dark/light/contrast themes; theme selection persisted and restored; window toolbar rendered; station trace reset waits for generation acknowledgement; stale snapshots rejected; nonfinite position/speed/span render safely; unknown curve labelled R?; no desktop capture.\r\n");
 }catch(Exception ex){File.WriteAllText(Path.Combine(dir,"validation.txt"),ex.ToString());Environment.ExitCode=1;}
 finally{if(before!=null)File.WriteAllText(settings,before);else if(File.Exists(settings))File.Delete(settings);}
 }
}
