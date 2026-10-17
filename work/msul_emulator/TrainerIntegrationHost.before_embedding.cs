using System;using System.IO;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Drawing;
class TrainerIntegrationHost:Form {
 [DllImport("kernel32",CharSet=CharSet.Unicode,SetLastError=true)] static extern IntPtr LoadLibrary(string path);
 [DllImport("kernel32",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr h,string name);
 [DllImport("kernel32",CharSet=CharSet.Unicode)] static extern bool SetDllDirectory(string path);
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)]struct KlubInit{public int X,Y;public IntPtr Path;public byte Type,Rotate,Show,TipDv,HandleFlag;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=16)]struct KlubState{public byte Map;public int MapNumber,X,InitTime;public byte Cassette;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=16)]struct BlokInit{public int X,Y;public uint KlubHandle;public byte Visible,Size1024,HandleFlag;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int KlubInitFn(IntPtr p,KlubInit config,KlubState initial);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int BlokInitFn(IntPtr p,BlokInit config);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void StepFn();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void NotifyFn(int code);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void FaultFn(byte code,IntPtr data,long value);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int CommInitFn(uint parent,IntPtr data,NotifyFn notify,FaultFn fault);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void KlubPointersFn(out IntPtr input,out IntPtr state);
 Dictionary<string,IntPtr> mem=new Dictionary<string,IntPtr>();StepFn commStep,commClose,klubStep,klubClose,blokStep,blokClose;NotifyFn notifyFn;FaultFn faultFn;
 bool commReady,klubReady,blokReady;Timer timer;Label status;NumericUpDown speed;int ticks;string root,resources;
 static IntPtr Alloc(int n){var p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);return p;}
 static void Ptr(IntPtr p,int o,IntPtr v){Marshal.WriteIntPtr(p,o,v);}static void I(IntPtr p,int o,int v){Marshal.WriteInt32(p,o,v);}static void B(IntPtr p,int o,int v){Marshal.WriteByte(p,o,(byte)v);}static void F(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 static float FloatAt(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 void Log(string s){File.AppendAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_integration_log.txt"),DateTime.Now.ToString("O")+" "+s+Environment.NewLine);}
 IntPtr Library(string name){string p=Path.Combine(resources,name);Log("Loading "+p);var h=LoadLibrary(p);if(h==IntPtr.Zero)throw new Exception("LoadLibrary="+Marshal.GetLastWin32Error()+" "+p);return h;}
 T Export<T>(IntPtr h,string name){var p=GetProcAddress(h,name);if(p==IntPtr.Zero)throw new Exception("Export absent "+name);return (T)(object)Marshal.GetDelegateForFunctionPointer(p,typeof(T));}
 TrainerIntegrationHost(){Text="2ЭС6 — проверка штатных систем тренажёра";ClientSize=new Size(800,190);StartPosition=FormStartPosition.Manual;Location=new Point(40,670);Font=new Font("Segoe UI",10);
 Controls.Add(new Label{Text="Тестовый датчик скорости, км/ч",Left=10,Top=10,Width=280});speed=new NumericUpDown{Left=300,Top=8,Width=100,Maximum=120,Value=0};Controls.Add(speed);
 var apply=new Button{Text="Передать датчик",Left=420,Top=5,Width=170};apply.Click+=(s,e)=>{if(commReady){F(mem["TInputParamPult"],1,(float)speed.Value);Log("Speed sensor="+speed.Value);}};Controls.Add(apply);
 status=new Label{Left=10,Top=48,Width=780,Height=85,Text="Подготовка штатной цепочки…"};Controls.Add(status);
 for(int n=1;n<=9;n++){int key=n;var b=new Button{Text=n.ToString(),Left=10+(n-1)*85,Top=140,Width=80,Height=35};b.Click+=(s,e)=>{if(commReady){B(mem["TPult"],0x53,16-key);B(mem["TPult"],0x54,16-key);Log("MSUL key="+key);}};Controls.Add(b);}
 Shown+=(s,e)=>StartNative();FormClosed+=(s,e)=>{if(timer!=null)timer.Stop();if(blokReady)blokClose();if(klubReady)klubClose();if(commReady)commClose();};}
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

 void StartNative(){try{
 AllocateData();root=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,@"..\work\trenager_static\WORK_TRENAGER_3D"));resources=Path.Combine(root,"RESOURCES");Directory.SetCurrentDirectory(root);SetDllDirectory(resources);
 I(mem["TLocParam"],0,1);B(mem["TLocParam"],4,2);I(mem["TInputParamPult"],0x2c,75);I(mem["TInputParamPult"],0x30,6000);F(mem["TInputParamPult"],0x23,1);I(mem["TInputParamPult"],5,80);I(mem["TInputParamPult"],9,80);
 mem["TCommunicationRec"]=Alloc(37);mem["TPultParamOutRecord"]=Alloc(15);mem["TProfilCPE"]=Alloc(1024);var c=mem["TCommunicationRec"];Ptr(c,0,mem["TCommonBtn"]);Ptr(c,4,mem["TResTyga"]);Ptr(c,8,mem["TLocParam"]);Ptr(c,12,mem["TVagonParam"]);Ptr(c,16,mem["TInputParamPult"]);Ptr(c,20,mem["TOutParamAllTRen"]);Ptr(c,24,mem["TProfilCPE"]);Ptr(c,33,mem["TPultParamOutRecord"]);
 mem["PWagonArray"]=Alloc(75*8);for(int w=0;w<75;w++){var wagon=Alloc(243);Ptr(mem["PWagonArray"],w*8+4,wagon);F(wagon,0x25,80);}Ptr(c,0x1d,mem["PWagonArray"]);

 notifyFn=n=>Log("Communication notification="+n);faultFn=(code,data,value)=>Log("Communication fault="+code+" value="+value);
 var ch=Library(@"LOCOMOTIVES\1\Communication1.dll");int cr=Export<CommInitFn>(ch,"InitCommunication")((uint)Handle.ToInt32(),c,notifyFn,faultFn);Log("InitCommunication result="+cr);if(cr!=0)throw new Exception("Communication initialization failed="+cr);commReady=true;commStep=Export<StepFn>(ch,"ExecuteCommunication");commClose=Export<StepFn>(ch,"CloseCommunicationDll");
 // Verified read-only layout for this exact Communication1.dll build: its owner embeds TPult and TTygaParam.
 IntPtr slot=Marshal.ReadIntPtr(IntPtr.Add(ch,0x2c7068));IntPtr owner=Marshal.ReadIntPtr(slot);if(owner==IntPtr.Zero)throw new Exception("Native owner absent");mem["TPult"]=IntPtr.Add(owner,0x24);mem["TTygaParam"]=IntPtr.Add(owner,0x131);Log("Native owner connected; Pult="+mem["TPult"].ToString("X")+" Tyga="+mem["TTygaParam"].ToString("X"));
 mem["TPribBezAllTren"]=Alloc(24);var pb=mem["TPribBezAllTren"];Ptr(pb,0,mem["TKLUBButton"]);Ptr(pb,4,mem["TAVPButton"]);Ptr(pb,8,mem["TInputParamPult"]);Ptr(pb,12,mem["TOutParamAllTRen"]);Ptr(pb,16,mem["TLocParam"]);Ptr(pb,20,mem["TVagonParam"]);
 var kh=Library("KLUB_U_LCD_XE2.dll");int kr=Export<KlubInitFn>(kh,"InitKlub_U_LCD")(pb,new KlubInit{X=40,Y=400,Path=Marshal.StringToHGlobalUni(root+"\\"),Type=4,Show=0,TipDv=1},new KlubState{Cassette=1});Log("InitKlub_U_LCD result="+kr);if(kr!=1)throw new Exception("KLUB initialization failed="+kr);klubReady=true;klubStep=Export<StepFn>(kh,"ProceedKlub");klubClose=Export<StepFn>(kh,"CloseKlub");
 IntPtr input,state;Export<KlubPointersFn>(kh,"GetParamInToKLUB")(out input,out state);if(input==IntPtr.Zero||state==IntPtr.Zero)throw new Exception("KLUB state absent");Log("KLUB internal state valid");
 var bh=Library("BLOK.dll");int br=Export<BlokInitFn>(bh,"InitBLOK")(pb,new BlokInit{X=850,Y=650,KlubHandle=(uint)kh.ToInt32(),Visible=1});Log("InitBLOK result="+br);if(br!=1)throw new Exception("BLOK initialization failed="+br);blokReady=true;blokStep=Export<StepFn>(bh,"ProceedBLOK");blokClose=Export<StepFn>(bh,"CloseBLOK");
 timer=new Timer{Interval=100};timer.Tick+=(s,e)=>{try{commStep();klubStep();blokStep();ticks++;if(ticks==10||ticks%100==0)Log("Cycle="+ticks+" Ia="+FloatAt(mem["TTygaParam"],0x5b)+" Iv="+FloatAt(mem["TTygaParam"],0x9b)+" Force="+FloatAt(mem["TResTyga"],0)+" V="+FloatAt(mem["TInputParamPult"],1));status.Text="Штатные электрические цепи, пульт, тяга, МСУЛ, звуки; КЛУБ → БЛОК. Цикл "+ticks+".\nМасса в данных: 6000 т, 75 вагонов. Пневматика и поездная динамика ещё не подключены.";}catch(Exception ex){timer.Stop();status.Text=ex.ToString();Log(ex.ToString());}};timer.Start();
 }catch(Exception ex){status.Text=ex.ToString();Log(ex.ToString());}}
 [STAThread]static void Main(){Application.EnableVisualStyles();Application.SetCompatibleTextRenderingDefault(false);Application.Run(new TrainerIntegrationHost());}
}
