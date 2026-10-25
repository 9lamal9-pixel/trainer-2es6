using System;using System.IO;using System.Runtime.InteropServices;using System.Collections.Generic;using System.Windows.Forms;using System.Drawing;
partial class TrainerIntegrationHost:Form {
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
 Dictionary<string,IntPtr> mem=new Dictionary<string,IntPtr>();StepFn commStep,commClose,klubStep,klubClose;NotifyFn notifyFn;FaultFn faultFn;
 bool commReady,klubReady;Timer timer;Label status;NumericUpDown speed;ComboBox klubPowerChoice;int klubPowerMode;int ticks,centerFrames;bool leftSaved,rightSaved,centerSaved;string root,resources;
 static IntPtr Alloc(int n){var p=Marshal.AllocHGlobal(n);Marshal.Copy(new byte[n],0,p,n);return p;}
 static void Ptr(IntPtr p,int o,IntPtr v){Marshal.WriteIntPtr(p,o,v);}static void I(IntPtr p,int o,int v){Marshal.WriteInt32(p,o,v);}static void B(IntPtr p,int o,int v){Marshal.WriteByte(p,o,(byte)v);}static void F(IntPtr p,int o,float v){Marshal.Copy(BitConverter.GetBytes(v),0,IntPtr.Add(p,o),4);}
 static float FloatAt(IntPtr p,int o){byte[] b=new byte[4];Marshal.Copy(IntPtr.Add(p,o),b,0,4);return BitConverter.ToSingle(b,0);}
 void Log(string s){TrainerSession.Log(s);}
 IntPtr Library(string name){string p=Path.Combine(resources,name);Log("Loading "+p);var h=LoadLibrary(p);if(h==IntPtr.Zero)throw new Exception("LoadLibrary="+Marshal.GetLastWin32Error()+" "+p);return h;}
 T Export<T>(IntPtr h,string name){var p=GetProcAddress(h,name);if(p==IntPtr.Zero)throw new Exception("Export absent "+name);return (T)(object)Marshal.GetDelegateForFunctionPointer(p,typeof(T));}
 TrainerIntegrationHost(){TrainerSession.EnsureStarted();Text="2ЭС6 — проверка штатных систем тренажёра";ClientSize=new Size(800,240);StartPosition=FormStartPosition.Manual;Location=new Point(40,670);Font=new Font("Segoe UI",10);InstallTrainPressureMode();InstallTrainForceMode();InstallAutovedenieControls();InstallStandaloneSautControls();InstallVigilanceControls();
 Controls.Add(new Label{Text="Начальная скорость поезда, км/ч",Left=10,Top=10,Width=280});speed=new NumericUpDown{Left=300,Top=8,Width=100,Maximum=120,Value=0};Controls.Add(speed);
 var apply=new Button{Text="Задать скорость",Left=420,Top=5,Width=170};apply.Click+=(s,e)=>{if(commReady){SetInitialSpeed((float)speed.Value);Log("Speed sensor="+speed.Value);}};Controls.Add(apply);
 Controls.Add(new Label{Text="КЛУБ-У",Left=602,Top=10,Width=70});klubPowerChoice=new ComboBox{Left=674,Top=7,Width=115,DropDownStyle=ComboBoxStyle.DropDownList};klubPowerChoice.Items.AddRange(new object[]{"Включён","Выключен","С пульта"});klubPowerChoice.SelectedIndex=0;klubPowerChoice.SelectedIndexChanged+=(s,e)=>{klubPowerMode=klubPowerChoice.SelectedIndex;Log("KLUB power control="+klubPowerChoice.Text);};Controls.Add(klubPowerChoice);
 status=new Label{Left=10,Top=48,Width=780,Height=85,Text="Подготовка штатной цепочки…"};Controls.Add(status);
 for(int n=1;n<=9;n++){int key=n;var b=new Button{Text=n.ToString(),Left=10+(n-1)*85,Top=140,Width=80,Height=35};b.Click+=(s,e)=>{if(commReady){SendMpsudKey((byte)(16-key));Log("MSUL key="+key);}};Controls.Add(b);}
 var charge=new Button{Text="Удерживать I",Left=10,Top=188,Width=180};charge.MouseDown+=(s,e)=>{if(e.Button==MouseButtons.Left)BeginCharge();};charge.MouseUp+=(s,e)=>{if(e.Button==MouseButtons.Left)EndCharge();};charge.MouseCaptureChanged+=(s,e)=>{if(!charge.Capture)EndCharge();};charge.KeyDown+=(s,e)=>{if(e.KeyCode==Keys.Space||e.KeyCode==Keys.Enter)BeginCharge();};charge.KeyUp+=(s,e)=>{if(e.KeyCode==Keys.Space||e.KeyCode==Keys.Enter)EndCharge();};charge.LostFocus+=(s,e)=>EndCharge();Deactivate+=(s,e)=>EndCharge();Controls.Add(charge);var service=new Button{Text="Торможение (V)",Left=200,Top=188,Width=180};service.Click+=(s,e)=>TestBrake(5);Controls.Add(service);var release=new Button{Text="Управление пультом",Left=390,Top=188,Width=210};release.Click+=(s,e)=>ReleaseTestInputs();Controls.Add(release);var running=new Button{Text="Поездное (II)",Left=610,Top=188,Width=180};running.Click+=(s,e)=>TestBrake(2);Controls.Add(running);Shown+=(s,e)=>StartNative();FormClosed+=(s,e)=>{if(timer!=null)timer.Stop();RestoreVigilanceOperatorInputs();CloseNativeSafetyAdapter();CloseAutovedenie();CloseNativeBrakeRelease();CloseContactNetwork();RestoreNativeSandAirInputs();RestoreMpsudAvWagonFieldFix();RestoreSignedTractionSpeed();RestorePneumaticMainTiming();CloseManagedMotion();CloseNativeAirCircuit();CloseAux();CloseRoute();if(klubClose!=null)klubClose();CloseNativeCommunication();};}
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
 AllocateData();root=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,@"..\work\trenager_static\WORK_TRENAGER_3D"));resources=Path.Combine(root,"RESOURCES");Directory.SetCurrentDirectory(root);SetDllDirectory(resources);LoadRoute();
 I(mem["TLocParam"],0,1);B(mem["TLocParam"],4,2);I(mem["TInputParamPult"],0x2c,75);I(mem["TInputParamPult"],0x30,6000);F(mem["TInputParamPult"],0x23,1);I(mem["TInputParamPult"],5,80);I(mem["TInputParamPult"],9,80);
 mem["TCommunicationRec"]=Alloc(37);mem["TPultParamOutRecord"]=Alloc(15);mem["TProfilCPE"]=Alloc(1024);var c=mem["TCommunicationRec"];Ptr(c,0,mem["TCommonBtn"]);Ptr(c,4,mem["TResTyga"]);Ptr(c,8,mem["TLocParam"]);Ptr(c,12,mem["TVagonParam"]);Ptr(c,16,mem["TInputParamPult"]);Ptr(c,20,mem["TOutParamAllTRen"]);Ptr(c,24,mem["TProfilCPE"]);Ptr(c,33,mem["TPultParamOutRecord"]);
 mem["PWagonArray"]=Alloc(75*8);for(int w=0;w<75;w++){var wagon=Alloc(243);Ptr(mem["PWagonArray"],w*8+4,wagon);I(wagon,0x25,80);}Ptr(c,0x1d,mem["PWagonArray"]);InitializePhysics();F(mem["TInputParamPult"],0xd,position);I(mem["TInputParamPult"],0x11,(int)position);

 notifyFn=n=>Log("Communication notification="+n);faultFn=(code,data,value)=>Log("Communication fault="+code+" value="+value);
 var ch=Library(@"LOCOMOTIVES\1\Communication1.dll");InitializeVirtualPultSource(ch);commClose=Export<StepFn>(ch,"CloseCommunicationDll");communicationInitializationAttempted=true;nativeCommunicationInactive=false;int cr=Export<CommInitFn>(ch,"InitCommunication")((uint)Handle.ToInt32(),c,notifyFn,faultFn);Log("InitCommunication result="+cr);if(cr!=0)throw new Exception("Communication initialization failed="+cr);commReady=true;commStep=Export<StepFn>(ch,"ExecuteCommunication");commClose=Export<StepFn>(ch,"CloseCommunicationDll");
 // Verified read-only layout for this exact Communication1.dll build: its owner embeds TPult and TTygaParam.
 IntPtr slot=Marshal.ReadIntPtr(IntPtr.Add(ch,0x2c7068));IntPtr owner=Marshal.ReadIntPtr(slot);if(owner==IntPtr.Zero)throw new Exception("Native owner absent");mem["CommOwner"]=owner;if(!nativeVirtualPult.Enabled||Marshal.ReadIntPtr(owner,0x10)!=IntPtr.Zero)throw new Exception("Native virtual-pult provider selection failed");Log("Native virtual-pult active; actual hardware provider absent");mem["TPult"]=IntPtr.Add(owner,0x24);I(mem["TPult"],0x71,2);mem["TTygaParam"]=IntPtr.Add(owner,0x131);Log("Native traction section count before setup="+Marshal.ReadInt32(mem["TTygaParam"],8));Log("Native owner connected; Pult="+mem["TPult"].ToString("X")+" Tyga="+mem["TTygaParam"].ToString("X"));
 InitializeContactNetwork();EnableSignedTractionSpeed(ch);EnableMpsudAvWagonFieldFix();
 mem["TPribBezAllTren"]=Alloc(24);var pb=mem["TPribBezAllTren"];Ptr(pb,0,mem["TKLUBButton"]);Ptr(pb,4,mem["TAVPButton"]);Ptr(pb,8,mem["TInputParamPult"]);Ptr(pb,12,mem["TOutParamAllTRen"]);Ptr(pb,16,mem["TLocParam"]);Ptr(pb,20,mem["TVagonParam"]);
 var kh=Library("KLUB_U_LCD_XE2.dll");klubModule=kh;klubInputContext=CreateSafetyContext("Klub",mem["TOutParamAllTRen"]);
 var originalKlubClose=Export<StepFn>(kh,"CloseKlub");bool klubClosed=false;klubClose=delegate{if(klubClosed)return;originalKlubClose();klubClosed=true;ReleaseSafetyContext("Klub",klubInputContext);klubInputContext=null;klubReady=false;};
 int kr=Export<KlubInitFn>(kh,"InitKlub_U_LCD")(klubInputContext.SharedRecord,new KlubInit{X=40,Y=400,Path=Marshal.StringToHGlobalUni(root+"\\"),Type=2,Show=0,TipDv=1,HandleFlag=1},new KlubState{Cassette=1});Log("InitKlub_U_LCD result="+kr);if(kr!=1)throw new Exception("KLUB initialization failed="+kr);klubReady=true;var originalKlubStep=Export<StepFn>(kh,"ProceedKlub");klubStep=delegate{if(klubClosed)throw new ObjectDisposedException("KLUB");klubInputContext.Sync();using(BorrowStandaloneSautModeInputs())using(BorrowVigilanceOperatorInputs()){originalKlubStep();}PublishNativeKlubAlsn();};
 IntPtr input,state;Export<KlubPointersFn>(kh,"GetParamInToKLUB")(out input,out state);if(input==IntPtr.Zero||state==IntPtr.Zero)throw new Exception("KLUB state absent");Log("KLUB internal state valid");
 InitializeAux();InitializeNativeSafetyAdapter();InitializeAutovedenie();InitializeBrakeReleaseOperatorControls();RouteInputCycle();OpenRoute();timer=new Timer{Interval=100};timer.Tick+=(s,e)=>{try{var pending=Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32);PollCommands();BeforeAutovedenieCycle();ElectricalCycle();AfterAutovedenieElectricalCycle();ApplyTestInputs();RouteInputCycle();AuxCycle();KlubCoordinateCycle();klubStep();PneumaticCycle();MotionCycle();RouteInputCycle();if(pending!=IntPtr.Zero&&Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32)==IntPtr.Zero)centerFrames++;ticks++;RouteViewCycle();UpdateTrainPressureMode();RecordTelemetry();if(ticks==10||ticks%100==0){CheckDisplays();LogPhysics();Log("Cycle="+ticks+" Ia="+FloatAt(mem["TTygaParam"],0x5b)+" Iv="+FloatAt(mem["TTygaParam"],0x9b)+" Force="+FloatAt(mem["TResTyga"],0)+" V="+FloatAt(mem["TInputParamPult"],1));}status.Text="Штатные электрические цепи, пульт, тяга, МСУЛ, звуки; КЛУБ-У, САУТ, автоведение. "+(route.native_axis?"Исходная разметка. ":"Карта PDF. ")+"Цикл "+ticks+". КЛУБ-У: "+(Marshal.ReadByte(mem["TPBBtn"],4)==1?"включён":"выключен")+".\nРежимная карта отдельным окном; 6000 т вагонов + 200 т электровоза."+(electricalFault.Length>0 ? "\nСбой штатной тяговой DLL: "+electricalFault+". Тяга снята; остальные контуры работают." : "")+(routeFinished?"\nГраница режимной карты: выберите станцию для нового проезда.":"");}catch(Exception ex){timer.Stop();status.Text=ex.ToString();TrainerSession.RecordException("native-cycle",ex);}};timer.Start();
 }catch(Exception ex){try{CloseContactNetwork();}catch(Exception cleanup){TrainerSession.RecordException("native-start-voltage-cleanup",cleanup);}try{CloseAutovedenie();}catch(Exception cleanup){TrainerSession.RecordException("native-start-avp-cleanup",cleanup);}try{RestoreMpsudAvWagonFieldFix();}catch(Exception cleanup){TrainerSession.RecordException("native-start-sc09-cleanup",cleanup);}status.Text=ex.ToString();TrainerSession.RecordException("native-start",ex);}}
 void SaveDisplay(string name,IntPtr bitmap){if(bitmap==IntPtr.Zero){Log(name+" frame absent");return;}using(var picture=Image.FromHbitmap(bitmap)){picture.Save(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,name+".png"),System.Drawing.Imaging.ImageFormat.Png);Log(name+" native frame="+picture.Width+"x"+picture.Height);}}
 void CheckDisplays(){
  if(!leftSaved&&Marshal.ReadIntPtr(mem["TPult"],0x105)!=IntPtr.Zero){SaveDisplay("MSUL_left",Marshal.ReadIntPtr(mem["TPult"],0x105));leftSaved=true;}
  if(!rightSaved&&Marshal.ReadIntPtr(mem["TPult"],0x109)!=IntPtr.Zero){SaveDisplay("MSUL_right",Marshal.ReadIntPtr(mem["TPult"],0x109));rightSaved=true;}
  if(!centerSaved&&Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32)!=IntPtr.Zero){SaveDisplay("KLUB_center",Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32));centerSaved=true;}
  Log("Center frames consumed by native cab="+centerFrames);
  var wrap=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);var form=wrap==IntPtr.Zero?IntPtr.Zero:Marshal.ReadIntPtr(wrap,0xc);
  Log("Display feeds: MSUL1="+Marshal.ReadIntPtr(mem["TPult"],0x105).ToString("X")+" MSUL2="+Marshal.ReadIntPtr(mem["TPult"],0x109).ToString("X")+" PB="+Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32).ToString("X")+" BLOK="+Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x3e).ToString("X"));
  if(form!=IntPtr.Zero)for(int i=0;i<3;i++){var view=Marshal.ReadIntPtr(form,0x500+4*i);Log("Cab display "+i+" bitmap="+Marshal.ReadIntPtr(view,0x298).ToString("X"));}
 }
 [STAThread]static void Main(){TrainerSession.EnsureStarted();try{Application.EnableVisualStyles();Application.SetCompatibleTextRenderingDefault(false);var host=new TrainerIntegrationHost();host.nativeMarkupEnabled=true;Application.Run(host);}catch(Exception ex){Environment.ExitCode=1;TrainerSession.RecordException("main",ex);throw;}finally{TrainerSession.Complete("main-finally");}}
}



