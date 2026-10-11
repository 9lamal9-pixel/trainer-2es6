using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Globalization;
using System.Windows.Forms;

partial class TrainerIntegrationHost {
 TrainPressureForm trainPressureView;
 void InstallTrainPressureMode(){
  FormClosed+=(s,e)=>{if(trainPressureView!=null){trainPressureView.Dispose();trainPressureView=null;}};
 }
 void OpenTrainPressureMode(){
  if(trainPressureView==null||trainPressureView.IsDisposed){trainPressureView=new TrainPressureForm();trainPressureView.Show(this);}
  else {if(trainPressureView.WindowState==FormWindowState.Minimized)trainPressureView.WindowState=FormWindowState.Normal;trainPressureView.Activate();}
  UpdateTrainPressureMode(true);
 }
 void UpdateTrainPressureMode(){if(ticks%2==0)UpdateTrainPressureMode(false);}
 void UpdateTrainPressureMode(bool immediate){
  if(trainPressureView==null||trainPressureView.IsDisposed||!physicsReady||cars==null)return;
  if(!immediate&&trainPressureView.IsPaused)return;
  trainPressureView.Push(ReadTrainPressureSample());
 }
 TrainPressureSample ReadTrainPressureSample(){
  var values=new double[cars.Length-1,3];
  for(int w=1;w<cars.Length;w++)for(int channel=0;channel<3;channel++)values[w-1,channel]=FloatAt(cars[w],channel*4);
  var loc=mem["TLocParam"];
  return new TrainPressureSample(ticks*.1,values,FloatAt(cars[0],0),FloatAt(loc,5),FloatAt(loc,9));
 }
}

// A snapshot is read after the native cycle, on the WinForms thread; this view never writes model memory.
sealed class TrainPressureSample {
 internal readonly double Seconds,LocomotiveTm,Pm,Ur;
 readonly double[,] values;
 internal int WagonCount{get{return values.GetLength(0);}}
 internal TrainPressureSample(double seconds,double[,] pressures,double locomotiveTm,double pm,double ur){
  if(pressures==null||pressures.GetLength(0)<1||pressures.GetLength(1)!=3)throw new ArgumentException("Expected wagon TM/TC/ZR pressures");
  Seconds=seconds;values=(double[,])pressures.Clone();LocomotiveTm=locomotiveTm;Pm=pm;Ur=ur;
 }
 internal double Pressure(int wagon,int channel){return values[wagon,channel];}
 internal static bool Valid(double value){return !double.IsNaN(value)&&!double.IsInfinity(value)&&value>=0;}
 internal static string Format(double value){return Valid(value)?value.ToString("0.00",CultureInfo.CurrentCulture):"нет данных";}
}

sealed class TrainPressureForm:Form {
 internal const int HistoryLimit=900;
 readonly List<TrainPressureSample> history=new List<TrainPressureSample>();
 readonly DataGridView grid;
 readonly Label summary;
 readonly CheckBox pause;
 readonly PressurePlot profile,trend;
 TrainPressureSample latest;
 internal int SelectedWagon{get;private set;}
 internal bool IsPaused{get{return pause.Checked;}}
 internal int HistoryCount{get{return history.Count;}}
 internal DataGridView PressureTable{get{return grid;}}
 internal TrainPressureForm(){
  Text="Режим давления — все вагоны";Font=new Font("Segoe UI",10);ClientSize=new Size(1180,730);MinimumSize=new Size(940,580);StartPosition=FormStartPosition.CenterScreen;
  var header=new Panel{Size=new Size(1180,86),Dock=DockStyle.Top,Height=86,BackColor=Color.FromArgb(238,243,249)};
  var heading=new Label{Text="Давления поезда · кгс/см²",Left=14,Top=10,Width=480,Height=25,Font=new Font("Segoe UI",13,FontStyle.Bold)};
  summary=new Label{Text="Ожидание данных модели…",Left=14,Top=44,Width=940,Height=29,Anchor=AnchorStyles.Left|AnchorStyles.Right|AnchorStyles.Top};
  pause=new CheckBox{Text="Пауза показаний",Left=990,Top=14,Width=175,Anchor=AnchorStyles.Right|AnchorStyles.Top};header.Controls.Add(heading);header.Controls.Add(summary);header.Controls.Add(pause);
  var footer=new Label{Dock=DockStyle.Bottom,Height=29,Padding=new Padding(12,5,0,0),Text="Выберите вагон в таблице. Графики: ТМ — синий, ТЦ — красный, ЗР — золотой. Время — время модели.",BackColor=Color.FromArgb(238,243,249)};
  var split=new SplitContainer{Size=new Size(1180,615),Dock=DockStyle.Fill,FixedPanel=FixedPanel.Panel1,SplitterWidth=8,Panel1MinSize=310,Panel2MinSize=460};
  // Set width before distance: the WinForms default width is smaller than the desired divider.
  split.Size=new Size(1180,615);split.SplitterDistance=340;
  grid=new DataGridView{Dock=DockStyle.Fill,ReadOnly=true,AllowUserToAddRows=false,AllowUserToDeleteRows=false,AllowUserToResizeRows=false,RowHeadersVisible=false,MultiSelect=false,SelectionMode=DataGridViewSelectionMode.FullRowSelect,AutoSizeColumnsMode=DataGridViewAutoSizeColumnsMode.Fill,BackgroundColor=Color.White,BorderStyle=BorderStyle.None};
  grid.Columns.Add("wagon","Вагон");grid.Columns.Add("tm","ТМ");grid.Columns.Add("tc","ТЦ");grid.Columns.Add("zr","ЗР");
  foreach(DataGridViewColumn c in grid.Columns){c.SortMode=DataGridViewColumnSortMode.NotSortable;c.DefaultCellStyle.Alignment=DataGridViewContentAlignment.MiddleRight;}
  grid.Columns[0].FillWeight=65;grid.Columns[1].DefaultCellStyle.ForeColor=PressurePlot.ChannelColors[0];grid.Columns[2].DefaultCellStyle.ForeColor=PressurePlot.ChannelColors[1];grid.Columns[3].DefaultCellStyle.ForeColor=PressurePlot.ChannelColors[2];
  grid.RowTemplate.Height=25;grid.AlternatingRowsDefaultCellStyle.BackColor=Color.FromArgb(247,249,252);
  grid.CurrentCellChanged+=(s,e)=>{if(grid.CurrentCell!=null){SelectedWagon=grid.CurrentCell.RowIndex;RefreshPlots();}};
  split.Panel1.Controls.Add(grid);
  var charts=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=2,BackColor=Color.FromArgb(224,230,239)};
  charts.RowStyles.Add(new RowStyle(SizeType.Percent,50));charts.RowStyles.Add(new RowStyle(SizeType.Percent,50));
  profile=new PressurePlot(false);trend=new PressurePlot(true);charts.Controls.Add(profile,0,0);charts.Controls.Add(trend,0,1);split.Panel2.Controls.Add(charts);
  Controls.Add(split);Controls.Add(footer);Controls.Add(header);
 }
 internal void Push(TrainPressureSample sample){
  if(sample==null)throw new ArgumentNullException("sample");
  if(IsPaused)return;
  if(latest!=null&&(sample.WagonCount!=latest.WagonCount||sample.Seconds<latest.Seconds))history.Clear();
  if(history.Count>0&&sample.Seconds==history[history.Count-1].Seconds)history[history.Count-1]=sample;
  else history.Add(sample);
  if(history.Count>HistoryLimit)history.RemoveRange(0,history.Count-HistoryLimit);
  latest=sample;
  if(grid.Rows.Count!=sample.WagonCount){grid.Rows.Clear();grid.Rows.Add(sample.WagonCount);SelectedWagon=0;}
  for(int w=0;w<sample.WagonCount;w++){
   var row=grid.Rows[w];row.Cells[0].Value=w+1;
   for(int c=0;c<3;c++)row.Cells[c+1].Value=TrainPressureSample.Format(sample.Pressure(w,c));
  }
  summary.Text=String.Format(CultureInfo.CurrentCulture,"{0} вагонов   ·   t = {1:0.0} с   ·   ТМ локомотива {2}   ·   ПМ {3}   ·   УР {4}",sample.WagonCount,sample.Seconds,TrainPressureSample.Format(sample.LocomotiveTm),TrainPressureSample.Format(sample.Pm),TrainPressureSample.Format(sample.Ur));
  RefreshPlots();
 }
 void RefreshPlots(){if(latest==null)return;SelectedWagon=Math.Min(SelectedWagon,latest.WagonCount-1);profile.SetData(latest,history,SelectedWagon);trend.SetData(latest,history,SelectedWagon);}
}

sealed class PressurePlot:Control {
 internal static readonly Color[] ChannelColors={Color.FromArgb(27,105,191),Color.FromArgb(204,57,65),Color.FromArgb(177,125,19)};
 static readonly string[] Names={"ТМ","ТЦ","ЗР"};
 readonly bool timePlot;
 TrainPressureSample latest;IList<TrainPressureSample> history;int selected;
 internal PressurePlot(bool time){timePlot=time;Dock=DockStyle.Fill;Margin=new Padding(5);BackColor=Color.White;DoubleBuffered=true;SetStyle(ControlStyles.ResizeRedraw,true);}
 internal void SetData(TrainPressureSample sample,IList<TrainPressureSample> samples,int wagon){latest=sample;history=samples;selected=wagon;Invalidate();}
 protected override void OnPaint(PaintEventArgs e){
  base.OnPaint(e);var g=e.Graphics;g.SmoothingMode=SmoothingMode.AntiAlias;
  string title=timePlot?"История давления · вагон "+(selected+1):"Давление вдоль состава · голова → хвост";
  using(var bold=new Font(Font,FontStyle.Bold))g.DrawString(title,bold,Brushes.Black,12,9);
  for(int c=0;c<3;c++)using(var brush=new SolidBrush(ChannelColors[c]))g.DrawString(Names[c],Font,brush,12+c*65,34);
  if(latest==null){g.DrawString("Ожидание данных",Font,Brushes.Gray,20,75);return;}
  var area=new RectangleF(58,65,Math.Max(1,Width-83),Math.Max(1,Height-108));if(area.Width<80||area.Height<40)return;
  double maximum=6;
  if(timePlot){foreach(var s in history)for(int c=0;c<3;c++){double v=s.Pressure(selected,c);if(TrainPressureSample.Valid(v))maximum=Math.Max(maximum,v);}}
  else for(int w=0;w<latest.WagonCount;w++)for(int c=0;c<3;c++){double v=latest.Pressure(w,c);if(TrainPressureSample.Valid(v))maximum=Math.Max(maximum,v);}
  maximum=Math.Ceiling(maximum);
  double first=timePlot?history[0].Seconds:1,last=timePlot?latest.Seconds:latest.WagonCount;
  double span=Math.Max(timePlot?.2:1,last-first);
  using(var line=new Pen(Color.FromArgb(227,233,241))){
   for(int k=0;k<=4;k++){float y=area.Bottom-area.Height*k/4;g.DrawLine(line,area.Left,y,area.Right,y);string label=(maximum*k/4).ToString("0.0",CultureInfo.CurrentCulture);g.DrawString(label,Font,Brushes.DimGray,3,y-8);}
   for(int k=0;k<=4;k++){float x=area.Left+area.Width*k/4;g.DrawLine(line,x,area.Top,x,area.Bottom);double tick=first+(last-first)*k/4;g.DrawString(tick.ToString(timePlot?"0.0":"0",CultureInfo.CurrentCulture),Font,Brushes.DimGray,x-10,area.Bottom+4);}
  }
  if(!timePlot)using(var mark=new Pen(Color.FromArgb(139,150,168))){mark.DashStyle=DashStyle.Dash;float x=area.Left+(float)((selected+1-first)/span)*area.Width;g.DrawLine(mark,x,area.Top,x,area.Bottom);}
  for(int c=0;c<3;c++)using(var pen=new Pen(ChannelColors[c],2))using(var brush=new SolidBrush(ChannelColors[c])){
   PointF? previous=null;int count=timePlot?history.Count:latest.WagonCount;
   for(int n=0;n<count;n++){
    double xvalue=timePlot?history[n].Seconds:n+1,value=timePlot?history[n].Pressure(selected,c):latest.Pressure(n,c);
    if(!TrainPressureSample.Valid(value)){previous=null;continue;}
    var point=new PointF(area.Left+(float)((xvalue-first)/span)*area.Width,area.Bottom-(float)(value/maximum)*area.Height);
    if(previous.HasValue)g.DrawLine(pen,previous.Value,point);else g.FillEllipse(brush,point.X-2,point.Y-2,4,4);previous=point;
   }
  }
  g.DrawString(timePlot?"Время модели, с (последние 180 с при непрерывном обновлении)":"Номер вагона",Font,Brushes.DimGray,area.Left,area.Bottom+23);
 }
}
