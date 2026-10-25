using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Globalization;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows.Forms;

// Presentation only. Immutable accepted-endpoint snapshots arrive from the host;
// this form never advances the solver or reads/writes native model memory.
public sealed class TrainForceForm : Form {
    internal static readonly Color Card=Color.FromArgb(27,34,44),Grid=Color.FromArgb(53,65,82),TextColor=Color.FromArgb(226,232,240),Muted=Color.FromArgb(154,170,190);
    internal static readonly Color Tension=Color.FromArgb(91,165,246),Compression=Color.FromArgb(242,102,112),Zero=Color.FromArgb(227,232,238),Gold=Color.FromArgb(245,201,99);
    TrainForceHistory history;
    TrainForceSample recorded,displayed;
    TrainForceSample[] shown=new TrainForceSample[0];
    Label summary;CheckBox pause;ComboBox vehicles,scale,heatQuantity,window,trendQuantity;NumericUpDown link;
    TabControl tabs;ForceProfile profile;ForceVectors vectors;ForceHeatmap heatmap;ForceTrend trend;
    bool selecting;int version;
    public bool Paused {get{return pause.Checked;}set{using(TrainerManagedFloatingScope.Enter()){pause.Checked=value;}}}
    public bool IsPaused {get{return Paused;}}
    public int SelectedVehicle {get{return Math.Max(0,vehicles.SelectedIndex);}set{using(TrainerManagedFloatingScope.Enter()){if(value<0||value>=vehicles.Items.Count)throw new ArgumentOutOfRangeException("value");vehicles.SelectedIndex=value;}}}
    public int SelectedVehicleIndex {get{return SelectedVehicle;}set{SelectedVehicle=value;}}
    public int SelectedLink {get{return (int)link.Value-1;}set{using(TrainerManagedFloatingScope.Enter()){if(value<0||value>=link.Maximum)throw new ArgumentOutOfRangeException("value");link.Value=value+1;}}}
    public int HistoryCount {get{return history.Count;}}
    public TrainForceSample DisplayedSample {get{return displayed;}}
    public TabControl ViewTabs {get{return tabs;}}
    internal TrainForceSample Current {get{return displayed;}}
    internal TrainForceSample[] Samples {get{return shown;}}
    internal int Version {get{return version;}}
    internal int HeatQuantity {get{return heatQuantity.SelectedIndex;}}
    internal int TrendQuantity {get{return trendQuantity.SelectedIndex;}}
    internal double WindowSeconds {get{return window.SelectedIndex==0?30:window.SelectedIndex==1?60:180;}}
    internal double FixedForceRangeKn {get{switch(scale.SelectedIndex){case 1:return 100;case 2:return 250;case 3:return 500;case 4:return 1000;case 5:return 2500;default:return 0;}}}

    public TrainForceForm(){using(TrainerManagedFloatingScope.Enter()){InitializeView();}}
    void InitializeView(){
        history=new TrainForceHistory(180,2400);
        Text="Силы по составу";BackColor=Color.FromArgb(24,29,37);ForeColor=TextColor;Font=new Font("Segoe UI",10);
        ClientSize=new Size(1280,860);MinimumSize=new Size(1000,700);StartPosition=FormStartPosition.CenterScreen;
        var header=new Panel{Dock=DockStyle.Top,Height=116,BackColor=Color.FromArgb(30,36,46)};
        header.Controls.Add(new Label{Text="Силы по составу",Left=15,Top=10,Width=270,Height=29,Font=new Font("Segoe UI",15,FontStyle.Bold)});
        header.Controls.Add(new Label{Text="Сцепки: растяжение + — синий   ·   сжатие − — красный",Left=300,Top=16,Width=700,Height=23,Anchor=AnchorStyles.Left|AnchorStyles.Top|AnchorStyles.Right});
        summary=new Label{Text="Ожидание принятого шага модели…",Left=15,Top=43,Width=1235,Height=24,Anchor=AnchorStyles.Left|AnchorStyles.Top|AnchorStyles.Right,AutoEllipsis=true};header.Controls.Add(summary);
        var tools=new FlowLayoutPanel{Dock=DockStyle.Bottom,Height=43,Padding=new Padding(10,3,0,0),WrapContents=false};
        tools.Controls.Add(ToolLabel("Экипаж",65));vehicles=Choice(155,new string[0]);tools.Controls.Add(vehicles);
        tools.Controls.Add(ToolLabel("Сцепка",65));link=new NumericUpDown{Minimum=1,Maximum=75,Value=1,Width=65,BackColor=Card,ForeColor=TextColor};tools.Controls.Add(link);
        tools.Controls.Add(ToolLabel("Шкала ±кН",95));scale=Choice(100,new[]{"Авто","100","250","500","1000","2500"});tools.Controls.Add(scale);
        pause=new CheckBox{Text="Пауза графиков",Width=165,Height=28,Margin=new Padding(22,0,0,0)};tools.Controls.Add(pause);header.Controls.Add(tools);
        var footer=new Label{Dock=DockStyle.Bottom,Height=33,Padding=new Padding(13,7,0,0),BackColor=Color.FromArgb(30,36,46),Text="Голова слева · физическое +X направлено к голове ← · пауза графиков не останавливает поезд.",AutoEllipsis=true};
        tabs=new TabControl{Dock=DockStyle.Fill,DrawMode=TabDrawMode.OwnerDrawFixed,ItemSize=new Size(215,32),SizeMode=TabSizeMode.Fixed};
        tabs.DrawItem+=(s,e)=>{using(TrainerManagedFloatingScope.Enter()){bool active=e.Index==tabs.SelectedIndex;using(var b=new SolidBrush(active?Color.FromArgb(48,62,80):Color.FromArgb(30,36,46)))e.Graphics.FillRectangle(b,e.Bounds);TextRenderer.DrawText(e.Graphics,tabs.TabPages[e.Index].Text,Font,e.Bounds,TextColor,TextFormatFlags.HorizontalCenter|TextFormatFlags.VerticalCenter);}};
        var spatial=Page("Состав и силы");var wave=Page("Волна по составу");var selected=Page("История экипажа");tabs.TabPages.AddRange(new[]{spatial,wave,selected});
        var layout=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=1,RowCount=2,Padding=new Padding(3)};layout.RowStyles.Add(new RowStyle(SizeType.Percent,44));layout.RowStyles.Add(new RowStyle(SizeType.Percent,56));
        profile=new ForceProfile(this);vectors=new ForceVectors(this);layout.Controls.Add(profile,0,0);layout.Controls.Add(vectors,0,1);spatial.Controls.Add(layout);
        var waveTools=new FlowLayoutPanel{Dock=DockStyle.Top,Height=42,Padding=new Padding(10,5,0,0),WrapContents=false};
        waveTools.Controls.Add(ToolLabel("Показатель",110));heatQuantity=Choice(220,new[]{"Силы в сцепках, кН","Скорость, км/ч","Ускорение, м/с²"});waveTools.Controls.Add(heatQuantity);
        waveTools.Controls.Add(ToolLabel("Окно",55));window=Choice(105,new[]{"30 секунд","60 секунд","180 секунд"});window.SelectedIndex=1;waveTools.Controls.Add(window);
        heatmap=new ForceHeatmap(this);wave.Controls.Add(heatmap);wave.Controls.Add(waveTools);
        var trendTools=new FlowLayoutPanel{Dock=DockStyle.Top,Height=42,Padding=new Padding(10,5,0,0)};
        trendTools.Controls.Add(ToolLabel("Показатель",110));trendQuantity=Choice(310,new[]{"Тяга, тормоза и сумма сил","Передняя и задняя сцепки","Скорость, км/ч","Ускорение, м/с²","Выбранная сцепка, кН"});trendTools.Controls.Add(trendQuantity);
        trend=new ForceTrend(this);selected.Controls.Add(trend);selected.Controls.Add(trendTools);
        Controls.Add(tabs);Controls.Add(footer);Controls.Add(header);
        vehicles.SelectedIndexChanged+=(s,e)=>RefreshPlots();link.ValueChanged+=(s,e)=>RefreshPlots();scale.SelectedIndexChanged+=(s,e)=>RefreshPlots();heatQuantity.SelectedIndexChanged+=(s,e)=>RefreshPlots();window.SelectedIndexChanged+=(s,e)=>RefreshPlots();trendQuantity.SelectedIndexChanged+=(s,e)=>RefreshPlots();
        pause.CheckedChanged+=(s,e)=>{using(TrainerManagedFloatingScope.Enter()){if(!pause.Checked)PublishView();else UpdateSummary();}};
    }
    static Label ToolLabel(string text,int width){return new Label{Text=text,Width=width,Height=27,TextAlign=ContentAlignment.MiddleLeft,Margin=new Padding(5,0,0,0)};}
    static ComboBox Choice(int width,string[] items){var c=new ComboBox{Width=width,DropDownStyle=ComboBoxStyle.DropDownList,BackColor=Card,ForeColor=TextColor,FlatStyle=FlatStyle.Flat};c.Items.AddRange(items);if(items.Length>0)c.SelectedIndex=0;return c;}
    static TabPage Page(string text){return new TabPage(text){BackColor=Color.FromArgb(24,29,37),ForeColor=TextColor};}
    public void Push(TrainForceSample snapshot){using(TrainerManagedFloatingScope.Enter()){
        if(snapshot==null)throw new ArgumentNullException("snapshot");if(IsDisposed||Disposing)return;
        if(InvokeRequired)throw new InvalidOperationException("Push must use the view's WinForms thread.");
        history.Push(snapshot);recorded=snapshot;
        // History continues while painting is paused. Resume displays real samples
        // received during the pause; no missing interval is manufactured.
        if(!Paused)PublishView();
    }}
    void PublishView(){if(recorded==null)return;displayed=recorded;shown=history.Samples;
        selecting=true;
        try{
            if(vehicles.Items.Count!=displayed.VehicleCount){vehicles.Items.Clear();for(int i=0;i<displayed.VehicleCount;i++)vehicles.Items.Add(i==0?"Локомотив":"Вагон "+i);vehicles.SelectedIndex=0;}
            link.Maximum=Math.Max(1,displayed.LinkCount);if(link.Value>link.Maximum)link.Value=link.Maximum;
        }finally{selecting=false;}
        version++;UpdateSummary();RefreshPlots();
    }
    void UpdateSummary(){
        if(displayed==null){summary.Text="Ожидание принятого шага модели…";return;}
        summary.Text=String.Format(CultureInfo.CurrentCulture,"{0}   ·   t = {1:0.0} с   ·   {2} экипажей / {3} сцепок   ·   {4}",Paused?"Графики на паузе":"Текущие показания",displayed.TimeSeconds,displayed.VehicleCount,displayed.LinkCount,displayed.HasAcceptedStep?"силы последнего принятого шага":"ожидание первого принятого шага; баланс ещё не определён");
    }
    void RefreshPlots(){using(TrainerManagedFloatingScope.Enter()){if(selecting)return;version++;if(profile!=null)profile.Invalidate();if(vectors!=null)vectors.Invalidate();if(heatmap!=null)heatmap.Invalidate();if(trend!=null)trend.Invalidate();}}
    public void SaveRender(string path,int tab){using(TrainerManagedFloatingScope.Enter()){
        if(String.IsNullOrWhiteSpace(path))throw new ArgumentException("Output path required.");if(tab<0||tab>=tabs.TabCount)throw new ArgumentOutOfRangeException("tab");
        int previous=tabs.SelectedIndex;try{tabs.SelectedIndex=tab;CreateControl();PerformLayout();tabs.SelectedTab.CreateControl();tabs.SelectedTab.PerformLayout();using(var bitmap=new Bitmap(Width,Height)){DrawToBitmap(bitmap,new Rectangle(Point.Empty,Size));bitmap.Save(path,ImageFormat.Png);}}finally{tabs.SelectedIndex=previous;}
    }}
    internal void PickColumn(int index,bool body){if(body)SelectedVehicle=index;else SelectedLink=index;}
    internal string VehicleName {get{return SelectedVehicle==0?"Локомотив":"Вагон "+SelectedVehicle;}}
    internal double FirstTime {get{return displayed==null?0:Math.Max(shown.Length==0?displayed.TimeSeconds:shown[0].TimeSeconds,displayed.TimeSeconds-WindowSeconds);}}
    internal double Cadence {
        get{double result=Double.MaxValue;for(int i=Math.Max(1,shown.Length-40);i<shown.Length;i++){double dt=shown[i].TimeSeconds-shown[i-1].TimeSeconds;if(dt>1e-6&&dt<result)result=dt;}return result==Double.MaxValue?.1:result;}
    }
    internal static string Number(double x,string format){return x.ToString(format,CultureInfo.CurrentCulture);}
    internal static string SignedKn(double value){return (value>0?"+":"")+Number(value/1000,"0.0")+" кН";}
    internal static bool Finite(double value){return !Double.IsNaN(value)&&!Double.IsInfinity(value);}
    internal static Color SignedColor(double value){return value>0?Tension:value<0?Compression:Muted;}

    abstract class Surface:Control {
        protected readonly TrainForceForm View;protected readonly Font Small,Bold;
        protected Surface(TrainForceForm view){View=view;Dock=DockStyle.Fill;Margin=new Padding(4);BackColor=Card;ForeColor=TextColor;Font=new Font("Segoe UI",10);Small=new Font("Segoe UI",9);Bold=new Font("Segoe UI",11,FontStyle.Bold);DoubleBuffered=true;SetStyle(ControlStyles.ResizeRedraw,true);}
        protected override void OnPaint(PaintEventArgs e){using(TrainerManagedFloatingScope.Enter()){base.OnPaint(e);e.Graphics.SmoothingMode=SmoothingMode.AntiAlias;e.Graphics.Clear(BackColor);PaintSurface(e.Graphics);}}
        protected abstract void PaintSurface(Graphics g);
        protected void DrawText(Graphics g,string text,Font font,Color color,RectangleF area,StringAlignment alignment){using(var b=new SolidBrush(color))using(var f=new StringFormat{Alignment=alignment,LineAlignment=StringAlignment.Center,Trimming=StringTrimming.EllipsisCharacter,FormatFlags=StringFormatFlags.NoWrap})g.DrawString(text,font,b,area,f);}
        protected void DrawText(Graphics g,string text,float x,float y,float width,float height){DrawText(g,text,Small,Muted,new RectangleF(x,y,width,height),StringAlignment.Near);}
        protected void Title(Graphics g,string text){DrawText(g,text,Bold,TextColor,new RectangleF(14,5,Width-28,27),StringAlignment.Near);}
        protected bool Ready(Graphics g){if(View.Current==null||!View.Current.HasAcceptedStep){DrawText(g,"Ожидание принятого шага модели. Силы и баланс ещё не определены.",18,50,Width-36,35);return false;}return true;}
        protected void GridAxes(Graphics g,RectangleF area,double min,double max,string unit){using(var p=new Pen(Grid)){for(int k=0;k<=4;k++){float y=area.Bottom-area.Height*k/4;g.DrawLine(p,area.Left,y,area.Right,y);DrawText(g,Number(min+(max-min)*k/4,"0.##"),3,y-10,area.Left-10,20);}}DrawText(g,unit,3,area.Top-24,area.Left-3,22);}
        protected void Arrow(Graphics g,float x,float y,double force,float length,Color color){if(Math.Abs(force)<1e-8){using(var b=new SolidBrush(Muted))g.FillEllipse(b,x-2,y-2,4,4);return;}float end=x+(force>0?-length:length);using(var pen=new Pen(color,2.3f))using(var cap=new AdjustableArrowCap(4,5)){pen.CustomEndCap=cap;g.DrawLine(pen,x,y,end,y);}}
        protected override void Dispose(bool disposing){if(disposing){Small.Dispose();Bold.Dispose();Font.Dispose();}base.Dispose(disposing);}
    }

    sealed class ForceProfile:Surface {
        RectangleF lastArea;
        internal ForceProfile(TrainForceForm v):base(v){}
        protected override void PaintSurface(Graphics g){
            Title(g,"Силы в сцепках · голова слева, хвост справа");if(!Ready(g))return;var sample=View.Current;if(sample.LinkCount<1)return;
            int chosen=Math.Min(View.SelectedLink,sample.LinkCount-1);double f=sample.GetLinkTensionN(chosen);
            DrawText(g,"Сцепка "+(chosen+1)+": "+SignedKn(f)+" · "+(f>0?"растяжение":f<0?"сжатие":"без продольной силы"),14,31,Width-28,23);
            var area=new RectangleF(75,76,Math.Max(1,Width-104),Math.Max(1,Height-119));lastArea=area;if(area.Height<25)return;
            double maximum=View.FixedForceRangeKn;if(maximum==0){maximum=10;for(int i=0;i<sample.LinkCount;i++)maximum=Math.Max(maximum,Math.Abs(sample.GetLinkTensionN(i))/1000);maximum=Nice(maximum);}
            GridAxes(g,area,-maximum,maximum,"кН");float zero=area.Top+area.Height/2;
            using(var pen=new Pen(Muted,1.2f))g.DrawLine(pen,area.Left,zero,area.Right,zero);
            PointF? previous=null;
            for(int i=0;i<sample.LinkCount;i++){
                double value=sample.GetLinkTensionN(i)/1000;float x=area.Left+(sample.LinkCount==1?.5f:(float)i/(sample.LinkCount-1))*area.Width;
                float y=zero-(float)Math.Max(-1,Math.Min(1,value/maximum))*area.Height/2;
                var pt=new PointF(x,y);using(var p=new Pen(SignedColor(value),1.6f)){g.DrawLine(p,x,zero,x,y);if(previous.HasValue)g.DrawLine(p,previous.Value,pt);}using(var b=new SolidBrush(SignedColor(value)))g.FillEllipse(b,x-2,y-2,4,4);previous=pt;
                if(i==chosen)using(var p=new Pen(Gold,1.5f)){p.DashStyle=DashStyle.Dash;g.DrawLine(p,x,area.Top,x,area.Bottom);}
            }
            for(int k=0;k<=4;k++){int i=(int)Math.Round((sample.LinkCount-1)*k/4.0);float x=area.Left+area.Width*k/4;DrawText(g,(i+1).ToString(),x-15,area.Bottom+3,40,20);}
            DrawText(g,"Номер сцепки вдоль состава · нажмите на профиль для выбора",area.Left,area.Bottom+23,area.Width,22);
        }
        protected override void OnMouseDown(MouseEventArgs e){using(TrainerManagedFloatingScope.Enter()){base.OnMouseDown(e);if(View.Current==null||lastArea.Width<1||!lastArea.Contains(e.Location))return;int i=(int)Math.Round((e.X-lastArea.Left)/lastArea.Width*(View.Current.LinkCount-1));View.PickColumn(Math.Max(0,Math.Min(View.Current.LinkCount-1,i)),false);}}
    }
    static double Nice(double value){double step=value<=100?10:value<=1000?100:500;return Math.Ceiling(value/step)*step;}

    sealed class ForceVectors:Surface {
        internal ForceVectors(TrainForceForm v):base(v){}
        protected override void PaintSurface(Graphics g){
            Title(g,"Силы на выбранном экипаже · "+View.VehicleName);if(!Ready(g))return;
            var v=View.Current.GetVehicle(View.SelectedVehicle);float centre=Width/2f,cy=83,half=93;
            DrawText(g,"+X к голове ←",14,36,175,22);
            DrawText(g,"v = "+Number(v.SpeedMps*3.6,"0.0")+" км/ч    a = "+Number(v.AccelerationMps2,"0.000")+" м/с²    ТЦ = "+Number(v.BrakeCylinderPressurePa/1000,"0")+" кПа",230,32,Width-250,25);
            var body=new RectangleF(centre-half,cy-22,2*half,44);using(var b=new SolidBrush(Color.FromArgb(49,66,85)))g.FillRectangle(b,body);using(var p=new Pen(Muted))g.DrawRectangle(p,body.X,body.Y,body.Width,body.Height);
            DrawText(g,View.VehicleName,Font,TextColor,new RectangleF(body.Left,body.Top+2,body.Width,24),StringAlignment.Center);
            double max=Math.Max(1,Math.Max(Math.Abs(v.FrontCouplerN),Math.Abs(v.RearCouplerN)));float reach=Math.Min(145,Width/7f);
            Color frontColor=View.SelectedVehicle>0?SignedColor(View.Current.GetLinkTensionN(View.SelectedVehicle-1)):Muted;
            Color rearColor=View.SelectedVehicle<View.Current.LinkCount?SignedColor(View.Current.GetLinkTensionN(View.SelectedVehicle)):Muted;
            Arrow(g,body.Left,cy+11,v.FrontCouplerN,(float)(Math.Abs(v.FrontCouplerN)/max)*reach,frontColor);
            Arrow(g,body.Right,cy+11,v.RearCouplerN,(float)(Math.Abs(v.RearCouplerN)/max)*reach,rearColor);
            DrawText(g,"Спереди: "+SignedKn(v.FrontCouplerN),14,cy+17,centre-half-25,25);
            DrawText(g,"Сзади: "+SignedKn(v.RearCouplerN),centre+half+15,cy+17,Width-centre-half-25,25);
            string[] names={"Тяга","Электротормоз","Пневмотормоз","Уклон","Основное сопротивление","Кривые","Сумма сил"};
            double[] values={v.TractionN,v.DynamicBrakeN,v.PneumaticBrakeN,v.GradeN,v.RollingN,v.CurveN,v.NetForceN};
            Color[] colors={Color.MediumAquamarine,Color.FromArgb(238,169,75),Compression,Gold,Muted,Color.FromArgb(190,150,229),TextColor};
            double scale=1;foreach(double x in values)scale=Math.Max(scale,Math.Abs(x));
            float rowHeight=Math.Min(26,Math.Max(21,(Height-169)/4f));
            for(int i=0;i<values.Length;i++){
                int column=i/4,row=i%4;float left=14+column*Width/2f,y=131+row*rowHeight,w=Width/2f-28;
                DrawText(g,names[i],left,y,w*.43f,21);float anchor=left+w*.64f;
                Arrow(g,anchor,y+11,values[i],(float)(Math.Abs(values[i])/scale)*w*.16f,colors[i]);
                DrawText(g,SignedKn(values[i]),Small,colors[i],new RectangleF(left+w*.78f,y,w*.22f,21),StringAlignment.Far);
            }
            string residual="ΣF = "+SignedKn(v.NetForceN)+"    mэфф·a = "+SignedKn(v.InertiaForceN)+"    невязка = "+Number(v.BalanceResidualN,"0.###")+" Н";
            DrawText(g,residual,14,Height-32,Width-28,25);
        }
    }

    sealed class ForceHeatmap:Surface {
        Bitmap cached;int cachedVersion=-1;double cachedRange;RectangleF lastArea;
        internal ForceHeatmap(TrainForceForm v):base(v){}
        protected override void PaintSurface(Graphics g){
            bool body=View.HeatQuantity!=0;string quantity=View.HeatQuantity==0?"Продольные силы в сцепках":View.HeatQuantity==1?"Скорость экипажей":"Ускорение экипажей";
            Title(g,quantity+" · время сверху вниз");if(View.Current==null||View.Samples.Length<1){DrawText(g,"Ожидание истории модели",18,50,Width-36,30);return;}
            var area=new RectangleF(85,65,Math.Max(1,Width-225),Math.Max(1,Height-175));lastArea=area;if(area.Height<20||area.Width<20)return;
            if(cachedVersion!=View.Version){Build();cachedVersion=View.Version;}
            if(cached==null)return;
            g.InterpolationMode=InterpolationMode.NearestNeighbor;g.PixelOffsetMode=PixelOffsetMode.Half;g.DrawImage(cached,area);g.PixelOffsetMode=PixelOffsetMode.Default;
            double first=View.FirstTime,last=View.Current.TimeSeconds,span=Math.Max(.1,last-first);
            for(int k=0;k<=4;k++){float y=area.Top+area.Height*k/4;DrawText(g,Number(first+span*k/4,"0.0")+" с",3,y-10,77,22);}
            int count=body?View.Current.VehicleCount:View.Current.LinkCount;
            for(int k=0;k<=4;k++){int index=(int)Math.Round((count-1)*k/4.0);float x=area.Left+(index+.5f)/count*area.Width;DrawText(g,body?(index==0?"Лок.":index.ToString()):(index+1).ToString(),x-22,area.Bottom+2,52,22);}
            int selected=body?View.SelectedVehicle:View.SelectedLink;float sx=area.Left+(selected+.5f)/count*area.Width;using(var p=new Pen(Gold,1.5f))g.DrawLine(p,sx,area.Top,sx,area.Bottom);
            DrawText(g,body?"Экипаж: локомотив → вагоны → хвост":"Сцепка: 1 у локомотива → 75 у хвоста",area.Left,area.Bottom+25,area.Width,22);
            float bx=area.Right+24;var bar=new RectangleF(bx,area.Top+24,20,Math.Max(40,area.Height-48));
            using(var brush=new LinearGradientBrush(bar,Tension,Compression,90)){brush.InterpolationColors=new ColorBlend{Colors=new[]{Tension,Zero,Compression},Positions=new[]{0f,.5f,1f}};g.FillRectangle(brush,bar);}
            string unit=View.HeatQuantity==0?"кН":View.HeatQuantity==1?"км/ч":"м/с²";
            DrawText(g,"+"+Number(cachedRange,"0.##")+" "+unit,bx-5,area.Top,110,22);DrawText(g,"0",bx+27,bar.Top+bar.Height/2-10,70,22);DrawText(g,"−"+Number(cachedRange,"0.##"),bx-5,area.Bottom-18,110,22);
            DrawText(g,"След вправо-вниз — возмущение к хвосту; влево-вниз — к голове. Время модели идёт вниз.",14,Height-50,Width-28,22);
            DrawText(g,"Серый: нет принятого шага / нет образцов. Полосы строятся только по фактическим образцам, без интерполяции фронта.",14,Height-28,Width-28,22);
        }
        double Value(TrainForceSample s,int column){if(View.HeatQuantity==0)return s.GetLinkTensionN(column)/1000;var v=s.GetVehicle(column);return View.HeatQuantity==1?v.SpeedMps*3.6:v.AccelerationMps2;}
        void Build(){
            if(cached!=null){cached.Dispose();cached=null;}var samples=View.Samples;var latest=View.Current;if(samples.Length<1||latest==null)return;
            int columns=View.HeatQuantity==0?latest.LinkCount:latest.VehicleCount;if(columns<1)return;
            double first=View.FirstTime,last=latest.TimeSeconds,span=Math.Max(.1,last-first),cadence=Math.Max(.001,View.Cadence);
            int rows=Math.Max(1,Math.Min(1800,(int)Math.Ceiling(span/cadence)));
            cachedRange=View.HeatQuantity==0?View.FixedForceRangeKn:0;
            if(cachedRange==0){cachedRange=View.HeatQuantity==2?.01:1;foreach(var s in samples)if(s.TimeSeconds>=first&&s.HasAcceptedStep)for(int c=0;c<columns;c++)cachedRange=Math.Max(cachedRange,Math.Abs(Value(s,c)));cachedRange=View.HeatQuantity==0?Nice(cachedRange):Math.Ceiling(cachedRange*(View.HeatQuantity==2?100:1))/(View.HeatQuantity==2?100:1);}
            var bytes=new byte[rows*columns*4];for(int n=0;n<bytes.Length;n+=4){bytes[n]=44;bytes[n+1]=34;bytes[n+2]=27;bytes[n+3]=255;}
            foreach(var s in samples){if(s.TimeSeconds<first||s.TimeSeconds>last||!s.HasAcceptedStep)continue;
                int top=Math.Max(0,(int)Math.Floor((s.TimeSeconds-cadence*.5-first)/span*rows)),bottom=Math.Min(rows-1,(int)Math.Floor((s.TimeSeconds+cadence*.5-first)/span*rows));
                for(int c=0;c<columns;c++){double value=Value(s,c),a=Math.Max(-1,Math.Min(1,value/cachedRange));Color end=a>=0?Tension:Compression;double t=Math.Abs(a);byte r=(byte)(Zero.R+(end.R-Zero.R)*t),gr=(byte)(Zero.G+(end.G-Zero.G)*t),b=(byte)(Zero.B+(end.B-Zero.B)*t);
                    for(int row=top;row<=bottom;row++){int offset=(row*columns+c)*4;bytes[offset]=b;bytes[offset+1]=gr;bytes[offset+2]=r;}}
            }
            cached=new Bitmap(columns,rows,PixelFormat.Format32bppArgb);var data=cached.LockBits(new Rectangle(0,0,columns,rows),ImageLockMode.WriteOnly,PixelFormat.Format32bppArgb);
            try{for(int row=0;row<rows;row++)Marshal.Copy(bytes,row*columns*4,IntPtr.Add(data.Scan0,row*data.Stride),columns*4);}finally{cached.UnlockBits(data);}
        }
        protected override void OnMouseDown(MouseEventArgs e){using(TrainerManagedFloatingScope.Enter()){base.OnMouseDown(e);if(View.Current==null||!lastArea.Contains(e.Location))return;bool body=View.HeatQuantity!=0;int count=body?View.Current.VehicleCount:View.Current.LinkCount;int index=(int)((e.X-lastArea.Left)/lastArea.Width*count);View.PickColumn(Math.Max(0,Math.Min(count-1,index)),body);}}
        protected override void Dispose(bool disposing){if(disposing&&cached!=null){cached.Dispose();cached=null;}base.Dispose(disposing);}
    }

    sealed class ForceTrend:Surface {
        internal ForceTrend(TrainForceForm v):base(v){}
        double[] Values(TrainForceSample sample){var v=sample.GetVehicle(View.SelectedVehicle);switch(View.TrendQuantity){case 1:return new[]{v.FrontCouplerN/1000,v.RearCouplerN/1000};case 2:return new[]{v.SpeedMps*3.6};case 3:return new[]{v.AccelerationMps2};case 4:return new[]{sample.GetLinkTensionN(View.SelectedLink)/1000};default:return new[]{v.TractionN/1000,v.PneumaticBrakeN/1000,v.DynamicBrakeN/1000,v.NetForceN/1000};}}
        protected override void PaintSurface(Graphics g){
            Title(g,View.TrendQuantity==4?"История сцепки "+(View.SelectedLink+1):"История · "+View.VehicleName);if(View.Current==null||View.Samples.Length<1)return;
            string[] names=View.TrendQuantity==1?new[]{"Спереди (+X)","Сзади (+X)"}:View.TrendQuantity==2?new[]{"Скорость"}:View.TrendQuantity==3?new[]{"Ускорение"}:View.TrendQuantity==4?new[]{"Растяжение + / сжатие −"}:new[]{"Тяга","Пневмотормоз","Электротормоз","Сумма"};
            Color[] colors=View.TrendQuantity==1?new[]{Tension,Gold}:View.TrendQuantity>=2?new[]{Tension}:new[]{Color.MediumAquamarine,Compression,Color.FromArgb(238,169,75),TextColor};
            float legend=14;for(int c=0;c<names.Length;c++){using(var p=new Pen(colors[c],2.5f))g.DrawLine(p,legend,44,legend+18,44);float w=g.MeasureString(names[c],Small).Width;DrawText(g,names[c],legend+23,32,w+12,24);legend+=w+47;}
            double first=View.FirstTime,last=View.Current.TimeSeconds,span=Math.Max(.1,last-first),maximum=.01;
            foreach(var s in View.Samples)if(s.TimeSeconds>=first&&s.HasAcceptedStep)foreach(double value in Values(s))maximum=Math.Max(maximum,Math.Abs(value));
            bool force=View.TrendQuantity<2||View.TrendQuantity==4;if(force)maximum=View.FixedForceRangeKn>0?View.FixedForceRangeKn:Nice(maximum);else maximum=Math.Ceiling(maximum*(View.TrendQuantity==3?100:1))/(View.TrendQuantity==3?100:1);
            string unit=View.TrendQuantity==2?"км/ч":View.TrendQuantity==3?"м/с²":"кН";
            var area=new RectangleF(80,83,Math.Max(1,Width-110),Math.Max(1,Height-143));if(area.Height<20)return;GridAxes(g,area,-maximum,maximum,unit);
            using(var p=new Pen(Muted))g.DrawLine(p,area.Left,area.Top+area.Height/2,area.Right,area.Top+area.Height/2);
            for(int c=0;c<names.Length;c++){
                PointF? previous=null;double previousTime=0;
                foreach(var s in View.Samples){if(s.TimeSeconds<first||!s.HasAcceptedStep){previous=null;continue;}double value=Values(s)[c];float x=area.Left+(float)((s.TimeSeconds-first)/span)*area.Width,y=area.Top+area.Height/2-(float)Math.Max(-1,Math.Min(1,value/maximum))*area.Height/2;
                    Color color=View.TrendQuantity==4?SignedColor(value):colors[c];using(var p=new Pen(color,2)){if(previous.HasValue&&s.TimeSeconds-previousTime<=Math.Max(.5,View.Cadence*3))g.DrawLine(p,previous.Value,new PointF(x,y));else using(var b=new SolidBrush(color))g.FillEllipse(b,x-2,y-2,4,4);}previous=new PointF(x,y);previousTime=s.TimeSeconds;
                }
            }
            for(int k=0;k<=4;k++){float x=area.Left+area.Width*k/4;DrawText(g,Number(first+span*k/4,"0.0"),x-15,area.Bottom+4,70,23);}
            DrawText(g,"Время модели, с · +X к голове ← · разрывы между отсутствующими образцами не соединяются",area.Left,area.Bottom+28,area.Width,24);
        }
    }
}
