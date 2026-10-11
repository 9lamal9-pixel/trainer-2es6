using System;using System.IO;using System.Drawing;using System.Collections.Generic;using System.Reflection;using System.Runtime.InteropServices;using System.Web.Script.Serialization;using System.Windows.Forms;
class TrainPressureViewProbe {
 static readonly BindingFlags Flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static object Call(object host,string name){return host.GetType().GetMethod(name,Flags,null,Type.EmptyTypes,null).Invoke(host,null);}
 static object Field(object host,string name){return host.GetType().GetField(name,Flags).GetValue(host);}
 static void Assert(bool condition,string message){if(!condition)throw new Exception(message);}
 static byte[] Bytes(IntPtr ptr,int count){var data=new byte[count];Marshal.Copy(ptr,data,0,count);return data;}
 static float Float(IntPtr ptr,int offset){return BitConverter.ToSingle(Bytes(IntPtr.Add(ptr,offset),4),0);}
 [STAThread]static int Main(){Application.EnableVisualStyles();TrainerIntegrationHost host=null;TrainPressureForm view=null;try{
  host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);var handle=host.Handle;Call(host,"StartNative");((Timer)Field(host,"timer")).Stop();
  var cars=(IntPtr[])Field(host,"cars");var mem=(Dictionary<string,IntPtr>)Field(host,"mem");
  var before=new List<byte[]>();foreach(var car in cars)before.Add(Bytes(car,243));var beforeLoc=Bytes(mem["TLocParam"],264);
  var sample=(TrainPressureSample)Call(host,"ReadTrainPressureSample");Assert(sample.WagonCount==75,"All75 wagons required");
  int checks=0;for(int w=0;w<75;w++)for(int c=0;c<3;c++){Assert(sample.Pressure(w,c)==Float(cars[w+1],c*4),"Native pressure mapping mismatch");checks++;}
  Assert(sample.LocomotiveTm==Float(cars[0],0)&&sample.Pm==Float(mem["TLocParam"],5)&&sample.Ur==Float(mem["TLocParam"],9),"Locomotive pressure mapping");
  Call(host,"OpenTrainPressureMode");view=(TrainPressureForm)Field(host,"trainPressureView");Assert(view.Visible&&view.PressureTable.Rows.Count==75,"Mode did not open all wagon rows");
  var synthetic=new double[75,3];for(int w=0;w<75;w++){synthetic[w,0]=5-w*.003;synthetic[w,1]=1.1+w*.02;synthetic[w,2]=5.1-w*.001;}
  var immutable=new TrainPressureSample(1,synthetic,5,8,5);synthetic[0,0]=99;Assert(immutable.Pressure(0,0)==5,"Snapshot must own copied values");synthetic[0,0]=5;
  view.Push(immutable);for(int w=0;w<75;w++)for(int c=0;c<3;c++)Assert((string)view.PressureTable.Rows[w].Cells[c+1].Value==TrainPressureSample.Format(synthetic[w,c]),"Table mapping");
  view.PressureTable.CurrentCell=view.PressureTable.Rows[74].Cells[0];Assert(view.SelectedWagon==74,"Tail selection");
  for(int n=2;n<=1005;n++){synthetic[74,1]=Math.Max(0,2.6-1.3*Math.Cos(n*.015));view.Push(new TrainPressureSample(n*.2,synthetic,5,8,5));}
  Assert(view.HistoryCount==TrainPressureForm.HistoryLimit,"Bounded history");int retained=view.HistoryCount;
  var pause=(CheckBox)view.GetType().GetField("pause",Flags).GetValue(view);Assert(pause.Left>=0&&pause.Right<=pause.Parent.ClientSize.Width,"Pause control clipped");pause.Checked=true;view.Push(new TrainPressureSample(9999,synthetic,5,8,5));Assert(view.HistoryCount==retained,"View pause changes history");pause.Checked=false;
  using(var bitmap=new Bitmap(view.Width,view.Height)){view.DrawToBitmap(bitmap,new Rectangle(Point.Empty,bitmap.Size));bitmap.Save(TrainerSession.ResultPath("Trainer_train_pressure_view.png"));}
  synthetic[0,0]=double.NaN;synthetic[1,1]=double.PositiveInfinity;synthetic[2,2]=-1;view.Push(new TrainPressureSample(202,synthetic,5,8,5));Assert((string)view.PressureTable.Rows[0].Cells[1].Value=="нет данных","Invalid pressure must not become zero");
  using(var bitmap=new Bitmap(view.Width,view.Height))view.DrawToBitmap(bitmap,new Rectangle(Point.Empty,bitmap.Size));
  view.Close();Call(host,"OpenTrainPressureMode");view=(TrainPressureForm)Field(host,"trainPressureView");Assert(view.Visible&&view.PressureTable.Rows.Count==75,"Mode reopen");
  for(int w=0;w<cars.Length;w++){var after=Bytes(cars[w],243);for(int i=0;i<after.Length;i++)Assert(after[i]==before[w][i],"View wrote native wagon memory");}
  var afterLoc=Bytes(mem["TLocParam"],264);for(int i=0;i<afterLoc.Length;i++)Assert(afterLoc[i]==beforeLoc[i],"View wrote locomotive memory");
  File.WriteAllText(TrainerSession.ResultPath("Trainer_train_pressure_view_validation.json"),new JavaScriptSerializer().Serialize(new{passed=true,wagons=75,native_pressure_checks=checks,native_read_only_verified=true,table_all_wagons_verified=true,tail_selection_verified=true,history_limit=TrainPressureForm.HistoryLimit,history_bounds_verified=true,invalid_values_verified=true,pause_verified=true,reopen_verified=true,rendered_png="Trainer_train_pressure_view.png",scope="Native snapshot mapping and memory nonmutation; synthetic brake/release history renders. Native pressure dynamics accepted separately; this probe does not certify brake norms."}));
  Console.WriteLine("PASS pressure mode:75 wagons/225 native readings,table,plots,history,pause,reopen,read-only");return 0;
 }catch(Exception ex){Console.WriteLine(ex);return 1;}finally{if(view!=null)view.Dispose();if(host!=null){host.Close();host.Dispose();}}}
}
