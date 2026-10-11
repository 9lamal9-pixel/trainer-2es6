using System;using System.IO;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Drawing;
class NativeMsulHost:Form {
 [DllImport("kernel32",CharSet=CharSet.Unicode,SetLastError=true)] static extern IntPtr LoadLibrary(string p);
 [DllImport("kernel32",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr h,string n);
 [DllImport("kernel32",CharSet=CharSet.Unicode)] static extern bool SetDllDirectory(string p);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate byte InitFn(IntPtr path,IntPtr points,IntPtr data,uint parent);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate void StepFn();
 Dictionary<string,IntPtr> mem=new Dictionary<string,IntPtr>(); StepFn step,close; Timer timer; Label status;NumericUpDown pos,spd,ia,iv;bool ready;int ticks;
 static IntPtr Alloc(int n){IntPtr p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);return p;}
 static void Ptr(IntPtr p,int o,IntPtr v){Marshal.WriteIntPtr(p,o,v);} static void I(IntPtr p,int o,int v){Marshal.WriteInt32(p,o,v);} static void B(IntPtr p,int o,int v){Marshal.WriteByte(p,o,(byte)v);}static void D(IntPtr p,int o,double v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),8);}static void F(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 void Log(string s){File.AppendAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"MSUL_native_log.txt"),DateTime.Now.ToString("O")+" "+s+Environment.NewLine);}
 NumericUpDown Number(string title,int x,int value,int max){Controls.Add(new Label{Text=title,Left=x,Top=10,Width=125});var n=new NumericUpDown{Left=x,Top=31,Width=125,Minimum=0,Maximum=max,Value=value};Controls.Add(n);return n;}
 NativeMsulHost(){Text="МСУЛ 2ЭС6 — штатная DLL тренажёра / входные данные";StartPosition=FormStartPosition.Manual;Location=new Point(40,670);ClientSize=new Size(790,190);Font=new Font("Segoe UI",10);pos=Number("Позиция ТЭД",10,44,65);spd=Number("Скорость, км/ч",150,51,120);ia=Number("Ток якоря, А",290,540,1500);iv=Number("Возбуждение, А",430,540,700);
 var apply=new Button{Text="Передать данные",Left=575,Top=25,Width=195,Height=35};apply.Click+=(s,e)=>Apply();Controls.Add(apply);
 for(int digit=1;digit<=9;digit++){int code=16-digit;var b=new Button{Text=digit==2?"2: Машинист":digit.ToString(),Left=10+(digit-1)*85,Top=135,Width=80,Height=35};b.Click+=(ss,ee)=>{if(ready){B(mem["TPult"],0x53,code);B(mem["TPult"],0x54,code);Log("MSUL key="+(16-code));}};Controls.Add(b);}status=new Label{Text="Подготовка…",Left=10,Top=75,Width=770,Height=55};Controls.Add(status);Shown+=(s,e)=>StartNative();FormClosed+=(s,e)=>{if(timer!=null)timer.Stop();if(ready){try{close();}catch{}}};}
 void AllocateData(){ mem["TMSUL2ES6"]=Alloc(16);
mem["TPult"]=Alloc(269);
mem["TCommonBtn"]=Alloc(32);
mem["TComBtnPos"]=Alloc(16);
mem["TPBBtn"]=Alloc(26);
mem["TPBBtnPos"]=Alloc(16);
mem["TKLUBButton"]=Alloc(92);
mem["TProgrammButton"]=Alloc(16);
mem["TAVPButton"]=Alloc(19);
mem["TEngineParam"]=Alloc(16);
mem["TLocParam"]=Alloc(264);
mem["TPnevmo"]=Alloc(120);
mem["TPnevmoSend"]=Alloc(16);
mem["TGRParam"]=Alloc(81);
mem["TPositionLoc"]=Alloc(28);
mem["TBlocked367"]=Alloc(16);
mem["TEPK_Param"]=Alloc(16);
mem["TEPKPos"]=Alloc(16);
mem["TEPKSend"]=Alloc(16);
mem["TEPT_Param"]=Alloc(16);
mem["TEPTPos"]=Alloc(16);
mem["TEPTSend"]=Alloc(16);
mem["TElectroDinTormoz"]=Alloc(16);
mem["TEDTPos"]=Alloc(16);
mem["TEDTSend"]=Alloc(16);
mem["TPrig"]=Alloc(16);
mem["TPrigPos"]=Alloc(16);
mem["TFault"]=Alloc(45);
mem["TDopParam"]=Alloc(16);
mem["TVagonParam"]=Alloc(243);
mem["TVagonSend"]=Alloc(37);
mem["TStateVagon"]=Alloc(16);
mem["TKonKranState"]=Alloc(16);
mem["TRukavConnect"]=Alloc(16);
mem["TUpdateParamVagon"]=Alloc(162);
mem["TEPTWag"]=Alloc(18);
mem["TVRParam"]=Alloc(16);
mem["TWagonTypeParam"]=Alloc(16);
mem["TInputParamPult"]=Alloc(52);
mem["TCurLighterRec"]=Alloc(59);
mem["TTygaParam"]=Alloc(305);
mem["TKontroller"]=Alloc(27);
mem["TResTyga"]=Alloc(26);
mem["TTygaResInto"]=Alloc(222);
mem["TTygaApparatFlag"]=Alloc(40);
mem["TReg_Info_Tren"]=Alloc(773);
mem["TPoint"]=Alloc(16);
mem["TPriborBezop"]=Alloc(16);
mem["TVideoRegParam"]=Alloc(68);
mem["TTren3DParam"]=Alloc(469);
mem["TOutParamAllTRen"]=Alloc(106);
mem["TPBOut"]=Alloc(50);
mem["THndlBmp"]=Alloc(28);
mem["THandleDllPB"]=Alloc(28);
mem["TButton_2ES6"]=Alloc(57);
mem["TLight_2ES6"]=Alloc(16);
mem["TPribors_2ES6"]=Alloc(16);
mem["TPosition_2ES6"]=Alloc(40);
mem["TApparatFlags"]=Alloc(72);
mem["TLastParam"]=Alloc(25);
mem["TNeisprFlags"]=Alloc(27);
mem["TNeisprFlagsDop"]=Alloc(16);
mem["TShema"]=Alloc(402472);
mem["TAutovedNastr"]=Alloc(34);
mem["TEditPreduprScreen"]=Alloc(401634);
mem["TPredupStr"]=Alloc(105);
mem["TPredup"]=Alloc(20);
Ptr(mem["TMSUL2ES6"],1,mem["TPult"]);
Ptr(mem["TMSUL2ES6"],5,mem["TTygaParam"]);
Ptr(mem["TMSUL2ES6"],9,mem["TShema"]);
Ptr(mem["TPult"],0,mem["TCommonBtn"]);
Ptr(mem["TPult"],4,mem["TLocParam"]);
Ptr(mem["TPult"],8,mem["TVagonParam"]);
Ptr(mem["TPult"],12,mem["TInputParamPult"]);
Ptr(mem["TPult"],16,mem["TTygaParam"]);
Ptr(mem["TPult"],20,mem["TReg_Info_Tren"]);
Ptr(mem["TPult"],24,mem["TOutParamAllTRen"]);
Ptr(mem["TCommonBtn"],0,mem["TComBtnPos"]);
Ptr(mem["TCommonBtn"],15,mem["TPBBtn"]);
Ptr(mem["TCommonBtn"],19,mem["TKLUBButton"]);
Ptr(mem["TCommonBtn"],28,mem["TAVPButton"]);
Ptr(mem["TPBBtn"],0,mem["TPBBtnPos"]);
Ptr(mem["TKLUBButton"],88,mem["TPBBtn"]);
Ptr(mem["TEngineParam"],0,mem["TLocParam"]);
Ptr(mem["TEngineParam"],4,mem["TVagonParam"]);
Ptr(mem["TInputParamPult"],31,mem["TCurLighterRec"]);
Ptr(mem["TTygaParam"],39,mem["TResTyga"]); }
 void Apply(){if(!ready)return;int n=(int)pos.Value;byte soed=(byte)(n<=23?1:n<=44?2:3);var t=mem["TTygaParam"];D(t,0,(double)spd.Value);I(t,8,2);I(t,0xc,n);I(t,0x1b,1);B(t,0x16,1);B(t,0xdc,soed);I(t,0xdd,100);I(t,0xe1,100);D(t,0x33,n<=23?375:n<=44?750:1500);
 // Native MSUL reads eight 32-bit Single values in each current array.
 for(int g=0;g<8;g++){float arm=g<4?(float)ia.Value:0;float field=g<4?(float)iv.Value:0;F(t,0x3b+g*4,arm);F(t,0x5b+g*4,arm);F(t,0x7b+g*4,field);F(t,0x9b+g*4,field);}
 F(mem["TInputParamPult"],1,(float)spd.Value);I(mem["TInputParamPult"],0x30,6000);I(mem["TInputParamPult"],0x2c,75);I(mem["TPult"],0x61+0x18,n);I(mem["TPult"],0x61+0x1c,(int)iv.Value);B(mem["TShema"],0x28,n);B(mem["TShema"],0x29,1);I(mem["TShema"],0x125,3000);I(mem["TShema"],0,110);I(mem["TShema"],4,20);F(mem["TResTyga"],0x11,3000);F(mem["TResTyga"],4,(float)ia.Value);F(mem["TResTyga"],0xd,(float)iv.Value);
 status.Text="Штатные окна МСУЛ открыты. Вводим тестовые показания, масса вагонов 6000 т.\nЭто эмуляция входов экрана; расчёт полной поездной динамики пока не подключён.";Log("Inputs position="+n+" speed="+spd.Value+" Ia="+ia.Value+" Iv="+iv.Value);}
 void StartNative(){try{AllocateData();B(mem["TMSUL2ES6"],0,1);B(mem["TLocParam"],4,2);IntPtr points=Alloc(16);I(points,0,40);I(points,4,40);I(points,8,870);I(points,12,40);
 string dll=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,@"..\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1\MSUL_MON.dll"));string folder=Path.GetDirectoryName(dll);SetDllDirectory(folder);Directory.SetCurrentDirectory(folder);Log("Loading "+dll);IntPtr h=LoadLibrary(dll);if(h==IntPtr.Zero)throw new Exception("LoadLibrary error "+Marshal.GetLastWin32Error());var init=(InitFn)Marshal.GetDelegateForFunctionPointer(GetProcAddress(h,"InitMSUL"),typeof(InitFn));step=(StepFn)Marshal.GetDelegateForFunctionPointer(GetProcAddress(h,"ProceedMsul"),typeof(StepFn));close=(StepFn)Marshal.GetDelegateForFunctionPointer(GetProcAddress(h,"CloseDll"),typeof(StepFn));byte ok=init(Marshal.StringToHGlobalUni(folder+"\\"),points,mem["TMSUL2ES6"],0);Log("InitMSUL="+ok);ready=ok!=0;Apply();B(mem["TPult"],0x53,14);B(mem["TPult"],0x54,14);timer=new Timer{Interval=100};timer.Tick+=(s,e)=>{try{step();if(++ticks==10)Log("ProceedMsul 10 calls completed");}catch(Exception ex){timer.Stop();status.Text=ex.ToString();Log(ex.ToString());}};timer.Start();}catch(Exception ex){status.Text=ex.ToString();Log(ex.ToString());}}
 [STAThread]static void Main(){Application.EnableVisualStyles();Application.SetCompatibleTextRenderingDefault(false);Application.Run(new NativeMsulHost());}
}