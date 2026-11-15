unit Videofrm;

interface

uses
IO_Data, video, Dialogs, Controls,  Classes,
  Windows, Messages, SysUtils,  Graphics,
  Forms, DX8DI, DirectXGraphics, DX8GFX, D3DX8,AVI,DIB,
  DXDRAWS,DirectInput,DX8D3DX,StdCtrls,exfiles, ComCtrls,lighters,
  ExtCtrls, DXClass,dinmodel,Tyga_vl11K, Spin,DataStructs_BIMSUL,MSULClasses;




const
        tlGreen=220;
        tlRed=221;
        tlYellow=222;
        tlBlue=223;
        tlWhite=224;
        tlBlack=225;
        tlBlinkGreen=240;
        tlBlinkYellow=241;
        tlDoubleYellow=250;
        tlDoubleYellowBlink=251;
        tlRedAndWhite=260;
        tlYellowAndGreen=270;
        tlYellowAndWhite=271;
        TlOnSignal=300;
        TlOffSignal=301;

 dataPath:string='data\';
 texPath:string='data\tex\';
 SignPath:string='data\signs\';
 SoundPath:string='data\sounds\';
 ppath:string='';
 RotateObjectStep:single=0.005;
 SnowStep:single=1000;
 MirrorDraw:boolean=false;
 Simulated:boolean=false;
 BBoxDraw:boolean=false;
 isNight:boolean=false;
 isVideo:boolean=false;
 isFog:boolean=false;
 isSkies:boolean=false;
 isSnow:boolean=false;
 isSnow2:boolean=false;
 isRain:boolean=false;
 isVideoPlayed:boolean=false;
 //1S-------------- Прожектор ярко тускло --------------------------------------
 isJectorPro:Integer=0;
 //1S---------------------------------------------------------------------------
 ViewWidth:integer=800;
 ViewHeight:integer=600;
 SpisokFlag11:boolean=true;
// scr1FILENAME = 'sscr1.bmp';
 scr1FILENAME = 'nature04.bmp';
 scr2FILENAME = '256.bmp';
  //1S------ Прожектор далеко ---------------------------------------------------
 scr21FILENAME = '2561.bmp';
 //1S---------------------------------------------------------------------------
 scr3FILENAME = 'fog.bmp';
 scr4FILENAME = 'NightSky.bmp';
 scr5FILENAME = 'snow.bmp';
 scr6FILENAME = 'Snow_fog.bmp';
 FOG_DAY_COLOR=$e1e1e1;
 FOG_NIGHT_COLOR=$060606;
  WRN00 = 'Error initializing 3D Hardware.'#10+
          'Now software will be used...';
  ERR00 = 'Could not initialize DirectX Graphics.'#10+
          'You must have installed DirectX 8 or better '#10+
          'and a 16, 24 or 32 bpp desktop to run this sample.';
  ERR01 = 'Error initializing Vertex Buffer.';
  ERR02 = 'Error loading file: ';

  FRAMEAT = 0; // Set to 0 to full speed.
  // Flexible vector format (FVF) description.
  // Describes the custom vertex structure (TCustomVertex).
  D3DFVF_CUSTOMVERTEX = D3DFVF_XYZ or D3DFVF_TEX1 or {D3DFVF_SPECULAR or }D3DFVF_DIFFUSE; //D3DFVF_DIFFUSE

  // Automatic texture coordinate generation.
  SHOW_HOW_TO_USE_TCI   = false;
  // Texture filtering.
  USE_TEXTURE_FILTERING = true;
  FPS_CHECK  = 1000;
type

HelperState=record
x0,x1,x2,x3,y0,y1,v0,v1,u0,u1,u2,u3:single;
end;

THelper=class
name:string;
Left:boolean;
StartFrame,EndFrame:longint;
PosState:array of HelperState;
Procedure Draw(frame:longint);
end;

Spoint=record
Stype:byte; //0 - stik, 1 - skrip
Sframe:Longint;
SValue:word; // if stik then meters, if sprip then veroyatnost'
             // >100 - bezstik 25- stick 0 - strelka.
end;

TSoundPoint=class
public
   KnockMeter:single;
   KnockCount:byte;
   curpoint:word;
   points:array of spoint;
   count:integer;
   procedure SearchCurPoint;
   procedure ProceedPoint;
end;

MySign=record
Name:string;
FileName:string;
Picture:IDirect3DTexture8;
end;

HW=record
case dword of
1:(i:dword);
2:(s:single);
end;

Trgb=record
case dword of
1:(a:array[1..4] of byte);
2:(d:dword);
end;



  TVideoForm = class(TForm)
    GroupBox2: TGroupBox;
    spnTi1: TSpinEdit;
    StaticText25: TStaticText;
    spnTi2: TSpinEdit;
    StaticText30: TStaticText;
    spnTi3: TSpinEdit;
    StaticText33: TStaticText;
    spnTi4: TSpinEdit;
    StaticText23: TStaticText;
    spnTi5: TSpinEdit;
    StaticText22: TStaticText;
    spnTi6: TSpinEdit;
    StaticText39: TStaticText;
    spnTi7: TSpinEdit;
    StaticText38: TStaticText;
    spnVal1: TSpinEdit;
    StaticText31: TStaticText;
    spnVal2: TSpinEdit;
    StaticText32: TStaticText;
    spnVal4: TSpinEdit;
    StaticText29: TStaticText;
    spnVal5: TSpinEdit;
    StaticText18: TStaticText;
    GroupBox18: TGroupBox;
    btnStartS: TButton;
    btnStopS: TButton;
    btnMoveR: TButton;
    btnRKrivS: TButton;
    btnLKrivS: TButton;
    btnRStrelS: TButton;
    btnLStrelS: TButton;
    btnMoveL: TButton;
    edtModeSet: TEdit;
    StaticText3: TStaticText;
    pnlAChSet: TPanel;
    btnLoadSet: TButton;
    btnSaveSet: TButton;
    SaveDialog1: TSaveDialog;
    OpenDialog1: TOpenDialog;
    StaticText4: TStaticText;
    edit1: TEdit;
    btnHide: TButton;
    edtdA: TEdit;
    StaticText5: TStaticText;
    edtStrel: TEdit;
    StaticText6: TStaticText;
    btnStykS: TButton;
    btnStrelUp: TButton;
    btnSndTest: TButton;
    GroupBox1: TGroupBox;
    Label1: TLabel;
    Label2: TLabel;
    Label3: TLabel;
    Label4: TLabel;
    Label5: TLabel;
    Label6: TLabel;
    edtSetTime12: TEdit;
    edtSetTime3: TEdit;
    edtSetDist2: TEdit;
    edtSetDist1: TEdit;
    edtSetDist3: TEdit;
    edtDist1: TEdit;
    edtDist2: TEdit;
    edtDist3: TEdit;
    GroupBox6: TGroupBox;
    btnStart: TButton;
    btnStop: TButton;
    spnTi8: TSpinEdit;
    StaticText1: TStaticText;
    spnVal7: TSpinEdit;
    StaticText2: TStaticText;
    btnVertS: TButton;
    GroupBox3: TGroupBox;
    Label7: TLabel;
    Label8: TLabel;
    Label9: TLabel;
    Label10: TLabel;
    Label11: TLabel;
    Label20: TLabel;
    Label21: TLabel;
    Label22: TLabel;
    Label23: TLabel;
    Label29: TLabel;
    Label28: TLabel;
    Label27: TLabel;
    Label26: TLabel;
    Label25: TLabel;
    Label24: TLabel;
    Label19: TLabel;
    Label18: TLabel;
    Label17: TLabel;
    Label16: TLabel;
    Label15: TLabel;
    Label14: TLabel;
    Label13: TLabel;
    Label12: TLabel;
    Label30: TLabel;
    Label31: TLabel;
    Label32: TLabel;
    Label33: TLabel;
    Label34: TLabel;
    lpos: TLabel;
    Panel1: TPanel;
    GroupBox9: TGroupBox;
    pnl_1: TPanel;
    Button1: TButton;
    GroupBox10: TGroupBox;
    pnl_2: TPanel;
    Button2: TButton;
    GroupBox4: TGroupBox;
    pnl_3: TPanel;
    Button3: TButton;
    GroupBox5: TGroupBox;
    pnl_R: TPanel;
    Button4: TButton;
    GroupBox7: TGroupBox;
    pnl_L: TPanel;
    Button5: TButton;
    GroupBox8: TGroupBox;
    pnl_6: TPanel;
    Button6: TButton;
    GroupBox11: TGroupBox;
    pnl_5: TPanel;
    Button7: TButton;
    GroupBox12: TGroupBox;
    pnl_4: TPanel;
    Button8: TButton;
    GroupBox13: TGroupBox;
    pnl_T: TPanel;
    Button9: TButton;
    GroupBox14: TGroupBox;
    pnl_9: TPanel;
    Button10: TButton;
    GroupBox15: TGroupBox;
    pnl_8: TPanel;
    Button11: TButton;
    GroupBox16: TGroupBox;
    pnl_7: TPanel;
    Button12: TButton;
    GroupBox17: TGroupBox;
    pnl_M: TPanel;
    Button13: TButton;
    GroupBox19: TGroupBox;
    pnl_RR: TPanel;
    Button14: TButton;
    GroupBox20: TGroupBox;
    pnl_0: TPanel;
    Button15: TButton;
    GroupBox21: TGroupBox;
    pnl_Z: TPanel;
    Button16: TButton;
    GroupBox22: TGroupBox;
    CheckBox1: TCheckBox;
    CheckBox2: TCheckBox;
    CheckBox3: TCheckBox;
    CheckBox4: TCheckBox;
    CheckBox5: TCheckBox;
    CheckBox6: TCheckBox;
    CheckBox7: TCheckBox;
    CheckBox8: TCheckBox;
    procedure FormActivate(Sender: TObject);
    procedure FormDestroy(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure FormDeactivate(Sender: TObject);
    procedure AppOnIdle(Sender: TObject; var Done: Boolean);
    procedure AppOnActivate(Sender: TObject);
    procedure AppOnDeactivate(Sender: TObject);
    procedure FormKeyDown(Sender: TObject; var Key: Word;
      Shift: TShiftState);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);

  private

    { Private declarations }
  public
    { Public declarations }
    

//    procedure CreateCurNeispr(Num:integer);
  end;

  PCustomVertex = ^TCustomVertex;
  TCustomVertex = record
                    Position : TD3DXVector3; // Position.
//                    Normal   : TD3DXVector3; // Normal.
                    DColor   : TD3DColor;    // Diffuse Color.
                    U, V     : single;       // Texture coordinates.
                  end;

   MyChartValue=record
    Value:integer;
    Color:byte; //0-green //2-red  /1-yellow ???
   end;
function GetCustomVertex(_px, _py, _pz:single; _u, _v: single): TCustomVertex;
function MakeD3DVertex(hv, nv: TD3DVector; tu, tv: single): TCustomVertex;
procedure UpdateScreen;
Function XtoScr(x:single):single;
Function YtoScr(y:single):single;
Procedure SetNight;
procedure CreateCurNeispr(Num:integer);
procedure Projector(State:Integer);


var
    Tmp1,Tmp2,Tmp3,Tmp4:Single; //Временная переменная для отображения реального интервала опроса приводов платформы


  Cursorindex,startindex,stopindex:integer;

// Chart variables
  Pos_Form_MSUL_MON: TPoint;  /////////UPR-MSUL_MON////////
  Pos_Form_BIMSUL: TPoint; /////////UPR-_BIMSUL////////
  form_MSul_mon:boolean; /////////UPR-MSUL_MON////////
  SignIndex:integer;
  OldColorLight:Word;
  ChartCount:integer;
  ChartData:array of MyChartValue;
  MaxChartValue:integer;

// end of chart variables
  prct:trect;
  LAstFrame:integer;
  IdleLagP:Variant;
  IdleLag:word;
  MotionDirection:byte;
  SnowSpeed:Single;
  TLighterslist:Tlist;
  CurTrafficLighter,NextLighter:TTLighter;
  CurAviFile:TmyAviFile;
  curAviFileIndex:integer;
  aliaslist:tstringlist;
  AviFileList:Tlist;
  OveralFrame,RealFrame, RealFrameOld:integer;
  realMeter:Single;
//  realKM:single;
  Avilenght:integer;
  SoundList,SoundObjects,SoundEffects,VisualSounds : Tlist;
  Mset_Global_Index:byte;
   HelpersList:Tlist;
  tempalpha:single;
  movetox,movetoy,xrot,yrot:integer;
  Screenclick:Byte=0;
  Obj:TEXFile;
  ObjList:tlist;
  T3dObjList:Tlist;
  Screenmat,Screenmat2 : TD3DMaterial8;
  Nightmat : TD3DMaterial8;
//1D------------Изменение прозрачности окна динамики состава ( tl_black_mat_Chart )-------
  tl_black_mat,tl_black_mat_Chart,tl_red_mat,tl_green_mat,tl_yellow_mat,tl_blue_mat,tl_white_mat: TD3DMaterial8;
//1D----------------------------------------------------------------------------
  Played:boolean=false;
  Font0 : ID3DXFont = NIL;
  FPS           : single = 0;
  FRAMECOUNT    : dWord = 0;
  FPSCONTROL    : dWord;
  VideoForm: TVideoForm;
  FRAMECONTROL : dWord;
  LASTFRAMETIME : dWord = 1;
  DXReady      : boolean = false;
  WindowReady  : boolean = false;

  Scr1,Scr2,NightScr,FogScr,NSKY,SnowBuf,ScreenSnowBuf,Lense,ChartBuf,DarkSquad,DarkSquad2,DarkSquad3,Sign,Helper: IDirect3DVertexBuffer8;
  dHelper  : array[0..3] of TCustomVertex;
  Snow  : array[0..3999] of TCustomVertex;
  ScreenSnow  : array[0..3999] of TCustomVertex;
  MyChart  : array[0..920] of TCustomVertex;
  ScreenSnowCount:integer;
  Tl_LenseTex,TextureScr1,TextureScr2,WallTex,EnvTex,NightTex,NightTex1,FogTex,NskyTEx,SnowTex,SnowFogTex,DropTex,PanelTex:  IDirect3DTexture8;

  Imagescr1,Imagescr2: TDIB;
  Surface      : IDirect3DSurface8;
  // Matrices
  matWorld : TD3DXMatrix; // World
  matView  : TD3DXMatrix; // View
  matProj  : TD3DXMatrix; // Projection
  ScreenScale: TD3DXMatrix;
  Signs:Array of MySign;
  //14M-- Переменная для отображения или скрытия графика динамики --------------
  DinamicChartDraw : Boolean = True;
  //14M-------------------------------------------------------------------------
procedure hHalt(_errMsg: pChar);
function GetD3DXVector3(_x, _y, _z: single): TD3DXVector3;
Procedure InitChart(Vagons:integer);

implementation

uses Unit1,unit2, ZadNeispr, Instr, Tren_Classes2, SoundsVar,UpdateKLUb,UpdateSaut;

{$R *.DFM}


Procedure DeleteSigns;
var i:integer;
begin
if Length(Signs)>0 then
for i:=0 to Length(Signs)-1 do Signs[i].Picture:=nil;
SetLength(Signs,0);
end;


Procedure LoadSigns;
var i,p:integer;
f:textfile;
begin
deleteSigns;
 assignfile(f,Signpath+'signs.dat');
 reset(f);
  readln(f);
   readln(f,i);
    if i>0 then begin
       SetLength(Signs,i);
      for p:=0 to i-1 do begin
       Signs[p].Picture:=nil;
       readln(f,Signs[p].Name);
       readln(f,Signs[p].FileName);
       D3DXCreateTextureFromFileExA(D3DDEV8, pchar(Signpath+Signs[p].FileName),64,64,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,Signs[p].Picture);
    end;
   end;
 CloseFile(f);
end;

Procedure THelper.Draw(frame:longint);
var oldWorld:td3dxmatrix;
   _ptr : pByte;
   n0: TD3DxVector3;
begin
if (frame>=startframe) and (frame<=endframe) then begin
//  UpdateHelper;
dHelper[0].Position.x:=PosState[OveralFRame-StartFrame].x0;
dHelper[2].Position.x:=PosState[OveralFRame-StartFrame].x2;
dHelper[1].Position.x:=PosState[OveralFRame-StartFrame].x1;
dHelper[3].Position.x:=PosState[OveralFRame-StartFrame].x3;
dHelper[0].U:=PosState[OveralFRame-StartFrame].u0;
dHelper[2].U:=PosState[OveralFRame-StartFrame].u2;
dHelper[1].U:=PosState[OveralFRame-StartFrame].u1;
dHelper[3].U:=PosState[OveralFRame-StartFrame].u3;
dHelper[0].Position.y:=PosState[OveralFRame-StartFrame].y0;
dHelper[1].Position.y:=PosState[OveralFRame-StartFrame].y1;
dHelper[0].V:=PosState[OveralFRame-StartFrame].v0;
dHelper[1].V:=PosState[OveralFRame-StartFrame].v1;

  n0 := GetD3DXVector3( 0.0, 0.0,-1.0 ); // Front face
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(dHelper), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,Helper)) then exit;
  if failed(Helper.Lock(0, SizeOf(dHelper), _ptr, 0)) then exit;
  Move(dHelper, _ptr^, SizeOf(dHelper));
  Helper.Unlock;



  D3DDEV8.GetTransform(D3DTS_WORLD,oldworld);
  D3DDEV8.SetTransform(D3DTS_WORLD,ScreenScale);
  D3DDEV8.SetMaterial(Screenmat);

  D3DDEV8.SetTextureStageState(0, D3DTSS_TEXCOORDINDEX, 0);
  D3DDEV8.SetTextureStageState(0, D3DTSS_TEXTURETRANSFORMFLAGS, D3DTTFF_DISABLE);




    // Set the stream source.
  D3DDEV8.SetStreamSource(0, helper, sizeof(TCustomVertex));
  // Set the vertex shader.
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);

//  D3DDEV8.SetTextureStageState(0, D3DTSS_TEXCOORDINDEX, 0);
  D3DDEV8.SetTextureStageState(0, D3DTSS_COLORARG1, D3DTA_TEXTURE);
  D3DDEV8.SetTextureStageState(0, D3DTSS_COLOROP, D3DTOP_MODULATE);
if left then
if isvideo then  D3DDEV8.SetTexture(0,texturescr1) else D3DDEV8.SetTexture(0,walltex)
else
if isvideo then  D3DDEV8.SetTexture(0,texturescr2) else D3DDEV8.SetTexture(0,walltex);
  // Render the vertices.
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);


  D3DDEV8.SetTransform(D3DTS_WORLD,oldworld);

end;
end;

Procedure DrawHelpers;
var i:integer;
begin
if helperslist.count>0 then
for i:=0 to helperslist.count -1 do
  Thelper(helperslist.items[i]).Draw(OveralFRame);
end;


Procedure TSoundPoint.ProceedPoint;
begin
if overalframe<points[curpoint+1].SFRame then begin

if KnockMeter-realmeter<=0 then begin  // stuchim

Case points[curpoint].SValue of
25:begin
Case KnockCount of
1:begin
 PlaySound(Knock1_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
2:begin
  PlaySound(Knock2_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
3:begin PlaySound(Knock3_snd);inc(KnockCount);Knockmeter:=realmeter+8;end;
4:begin PlaySound(Knock4_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
5:begin PlaySound(Knock5_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
6:begin PlaySound(Knock6_snd);KnockCount:=1;Knockmeter:=realmeter+13;end;
end ;//25
end;
800..1500 :begin
Case KnockCount of
1:begin  {AChPar.HUp:=1;} inc(KnockCount);Knockmeter:=realmeter+points[curpoint].SValue;end;
2:begin   PlaySound(Knock1_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
3:begin PlaySound(Knock2_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
4:begin PlaySound(Knock3_snd);inc(KnockCount);Knockmeter:=realmeter+8;end;
5:begin PlaySound(Knock4_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
6:begin PlaySound(Knock5_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
7:begin PlaySound(Knock6_snd);inc(KnockCount);Knockmeter:=realmeter+13;end;

8:begin PlaySound(Knock1_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
9:begin PlaySound(Knock2_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
10:begin PlaySound(Knock3_snd);inc(KnockCount);Knockmeter:=realmeter+8;end;
11:begin PlaySound(Knock4_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
12:begin PlaySound(Knock5_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
13:begin PlaySound(Knock6_snd);inc(KnockCount);Knockmeter:=realmeter+13;end;

14:begin PlaySound(Knock1_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
15:begin PlaySound(Knock2_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
16:begin PlaySound(Knock3_snd);inc(KnockCount);Knockmeter:=realmeter+8;end;
17:begin PlaySound(Knock4_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
18:begin PlaySound(Knock5_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
19:begin PlaySound(Knock6_snd);inc(KnockCount);Knockmeter:=realmeter+13;end;

20:begin PlaySound(Knock1_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
21:begin PlaySound(Knock2_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
22:begin PlaySound(Knock3_snd);inc(KnockCount);Knockmeter:=realmeter+8;end;
23:begin PlaySound(Knock4_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
24:begin PlaySound(Knock5_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
25:begin PlaySound(Knock6_snd);inc(KnockCount);Knockmeter:=realmeter+13;end;

26:begin PlaySound(Knock1_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
27:begin PlaySound(Knock2_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
28:begin PlaySound(Knock3_snd);inc(KnockCount);Knockmeter:=realmeter+8;end;
29:begin PlaySound(Knock4_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
30:begin PlaySound(Knock5_snd);inc(KnockCount);Knockmeter:=realmeter+1;end;
31:begin PlaySound(Knock6_snd);KnockCount:=1;Knockmeter:=realmeter+13;end;

end;
end;
end;
end;
end else begin   //!!!  vibiraem type of knocking
inc(curpoint);
KnockCount:=1;
Knockmeter:=realmeter;
if points[curpoint].Stype=1 then begin
// squeak
inc(curpoint);
end;


end;            //!!!

end;



Procedure TSoundPOint.SearchCurPOint;
var i:integer;
loopflag:boolean;
begin
KnockCount:=1;
Knockmeter:=0;
loopflag:=false;
i:=0;
while (i<=Length(points)-1) and (not loopflag) do begin
If points[i].Sframe>=OveralFRame  then
loopflag:=true else inc(i);
end;
if LoopFlag then begin // nashli nuzhnij point
LoopFlag:=false;
  while (i>=0) and (not loopflag) do begin
   If (points[i].Sframe<OveralFRame) and (points[i].Stype=0) and (points[i].Svalue<>0) then
   loopflag:=true else dec(i);
 end;
if LoopFlag then Curpoint:=i else CurPoint:=0;
end else begin // ishem poslednij point with stype=0
dec(i);
LoopFlag:=false;
  while (i>=0) and (not loopflag) do begin
   If (points[i].Stype=0) and (points[i].Svalue<>0) then
   loopflag:=true else dec(i);
end;
if LoopFlag then Curpoint:=i;
end;

end;


Procedure InitChart(Vagons:integer);
var i:integer;
begin
ChartCount:=Vagons;
SetLength(ChartData,Vagons);
for i:=0 to Vagons-1 do
ChartData[i].Value:=0;
end;

Procedure SetNight;
begin
case isNight of
false:begin
Mset_Global_Index:=0;
ScreenMat.Emissive.r:=1;
ScreenMat.Emissive.g:=1;
ScreenMat.Emissive.b:=1;

   d3ddev8.LightEnable(0, true);
   d3ddev8.LightEnable(1, false);
   isNight:=false;
   d3ddev8.SetRenderState(D3DRS_FOGCOLOR,FOG_DAY_COLOR);
  end;
true:begin
Mset_Global_Index:=1;
ScreenMat.Emissive.r:=0.96;
ScreenMat.Emissive.g:=0.96;
ScreenMat.Emissive.b:=0.96;

   d3ddev8.LightEnable(0, false);
   d3ddev8.LightEnable(1, true);
   isNight:=true;
   d3ddev8.SetRenderState(D3DRS_FOGCOLOR,FOG_NIGHT_COLOR);
  end;
end;


end;


procedure UpdateScreen;
var  TexRect,TexRect2 : TD3DLocked_Rect;
i:integer;
DstCur,DstCur2 : pByte;


procedure ColorToBW(Source,Dest:Pointer;Count:integer);assembler;
label JFK;
asm
     push esi
     push edi

     mov esi,eax
     mov edi,edx

JFK: xor dh,dh
     xor ah,ah
     mov al,[esi]
     mov dl,[esi+1]
     add ax,dx
     mov dl,[esi+2]
     add ax,dx  //r+g+b
     mov dl,3
     div dl  //(r+g+b)/3

     mov [edi],al
     mov [edi+1],al
     mov [edi+2],al
     mov dh,$FF
     sub dh,al
     mov [edi+3],dh
     add esi,4
     add edi,4
     loop JFK

     pop edi
     pop esi
end;

begin
if played then begin
playavi2(realframe,ImageScr1.canvas.Handle,ImageScr2.canvas.Handle);
Texturescr1.LockRect(0, TexRect, NIL, 0);
Texturescr2.LockRect(0, TexRect2, @prct, 0);
  DstCur:=TexRect.Pbits;
  DstCur2:=TexRect2.Pbits;
   For i:=0 to ImageScr1.Height-1 do begin
if isSnow2 then ColorToBw(ImageScr1.ScanLine[i],DstCur,ImageScr1.Width) else
     Move(ImageScr1.ScanLine[i]^,DstCur^,ImageScr1.WidthBytes);
if isSnow2 then ColorToBw(ImageScr2.ScanLine[i],DstCur2,ImageScr2.Width) else
 Move(ImageScr2.ScanLine[i]^,DstCur2^,ImageScr2.WidthBytes);
     inc(DstCur, TexREct.Pitch);
     inc(DstCur2, TexREct2.Pitch);
    end;
 Texturescr1.UnLockRect(0);
Texturescr2.UnLockRect(0);

 end;
end;


function GetCustomVertex(_px, _py, _pz:single; _u, _v: single): TCustomVertex;

begin
  with Result do
   begin
     Position.x := _px;
     Position.y := _py;
     Position.z := _pz;
//     Normal:=n;
     U := _u;
     V := _v;
     Dcolor:=$FFFFFFFF;
   end;
end;


function MakeD3DVertex(hv, nv: TD3DVector; tu, tv: single): TCustomVertex;
begin
  Result.position.x := hv.x;
  Result.position.y := hv.y;
  Result.position.z := hv.z;
{  Result.Normal.x := nv.x;
  Result.ny := nv.y;
  Result.nz := nv.z;}
  Result.Dcolor:=$FFFFFFFF;
  Result.u := tu;
  Result.v := tv;
end;

function InitScreenVB: boolean;

var _vb  : array[0..3] of TCustomVertex;
    _vb2  : array[0..99] of TCustomVertex;
    _ptr : pByte;
    n0: TD3DxVector3;
   _cnt : longint;
   x,y,z, _theta : single;
    offset,scale:single;

begin
  Result := false;

  n0 := GetD3DXVector3( 0.0, 0.0,-1.0 ); // Front face
 dHelper[0]:=GetCustomVertex(-0.50, 1.0, 150.0, 0.303,0.0);
 dHelper[1]:=GetCustomVertex( 0.6515, 1.0, 150.0, 1.0,0.0);
 dHelper[2]:=GetCustomVertex(-0.50,-1.0, 150.0, 0.303,1.0);
 dHelper[3]:=GetCustomVertex( 0.6515,-1.0, 150.0, 1.0,1.0);
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(dHelper), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,Helper)) then exit;
  if failed(Helper.Lock(0, SizeOf(dHelper), _ptr, 0)) then exit;
  Move(dhelper, _ptr^, SizeOf(dhelper));
  Helper.Unlock;

  n0 := GetD3DXVector3( 0.0, 0.0,-1.0 ); // Front face
 _vb[0]:=GetCustomVertex(-1.0, 1.0, 150.0,0.0,0.0);
 _vb[1]:=GetCustomVertex( 0.6515, 1.0, 150.0,1.0,0.0);
 _vb[2]:=GetCustomVertex(-1.0,-1.0, 150.0,0.0,1.0);
 _vb[3]:=GetCustomVertex( 0.6515,-1.0, 150.0,1.0,1.0);
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,Scr1)) then exit;
  if failed(Scr1.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  Scr1.Unlock;

  _vb[0]:=GetCustomVertex(0.64895, 1.0, 150.0,0.0,0.0);
  _vb[1]:=GetCustomVertex( 2.46895, 1.0, 150.0,1.0,0.0);
  _vb[2]:=GetCustomVertex(0.64895,-1.0, 150.0,0.0,1.0);
  _vb[3]:=GetCustomVertex( 2.46895,-1.0, 150.0,1.0,1.0);
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,Scr2)) then exit;
  if failed(Scr2.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  Scr2.Unlock;

  _vb[0]:=GetCustomVertex(-1, 1.0, 149.9,0.0,0.0);
  _vb[1]:=GetCustomVertex( 1, 1.0, 149.9,1.0,0.0);
  _vb[2]:=GetCustomVertex(-1,-1.0, 149.9,0.0,1.0);
  _vb[3]:=GetCustomVertex(1,-1.0, 149.9,1.0,1.0);

  // Create a vertex buffer.
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,NightScr)) then exit;

  // Lock, fill and unlock vertex buffer.
  if failed(NightScr.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  NightScr.Unlock;


  _vb[0]:=GetCustomVertex(-1, 1.0, 149.7,0.0,0.0);
  _vb[1]:=GetCustomVertex( 1, 1.0, 149.7,1.0,0.0);
  _vb[2]:=GetCustomVertex(-1,-1.0, 149.7,0.0,1.0);
  _vb[3]:=GetCustomVertex(1,-1.0, 149.7,1.0,1.0);

  // Create a vertex buffer.
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,FogScr)) then exit;

  // Lock, fill and unlock vertex buffer.
  if failed(FogScr.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  fogScr.Unlock;


 _vb2[0] := GetCustomVertex(0,0,{149.8}0,0,0);
for _cnt :=1 to 20 do begin
 _theta:=(2*pi*_cnt)/19;
 _vb2[_cnt] := GetCustomVertex(sin(_theta),cos(_theta),{149.8}0, 1,1);
end;
  // Create a vertex buffer.
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb2), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,Lense)) then exit;

  // Lock, fill and unlock vertex buffer.
  if failed(Lense.Lock(0, SizeOf(_vb2), _ptr, 0)) then exit;
  Move(_vb2, _ptr^, SizeOf(_vb2));
  Lense.Unlock;

  _vb[0]:=GetCustomVertex(-0.267, 0.267, 1,0.0,0.0);
  _vb[1]:=GetCustomVertex( -0.034, 0.267, 1,1.0,0.0);
  _vb[2]:=GetCustomVertex(-0.267,0.085, 1,0.0,1.0);
  _vb[3]:=GetCustomVertex( -0.034,0.085, 1,1.0,1.0);
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,DarkSquad)) then exit;
  if failed(DarkSquad.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  DarkSquad.Unlock;

  _vb[0]:=GetCustomVertex(-0.265, -0.155, 1,0.0,0.0);
  _vb[1]:=GetCustomVertex( -0.134, -0.155, 1,1.0,0.0);
  _vb[2]:=GetCustomVertex(-0.265,-0.31, 1,0.0,1);
  _vb[3]:=GetCustomVertex( -0.134,-0.31, 1,1.0,1);
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,DarkSquad2)) then exit;
  if failed(DarkSquad2.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  DarkSquad2.Unlock;


  _vb[0]:=GetCustomVertex(-0.2367, 0.2367, 1,0.0,0.0);
  _vb[1]:=GetCustomVertex( 0.2367, 0.2367, 1,1.0,0.0);
  _vb[2]:=GetCustomVertex(-0.2367,-0.2367, 1,0.0,1.0);
  _vb[3]:=GetCustomVertex( 0.2367,-0.2367, 1,1.0,1.0);
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,DarkSquad3)) then exit;
  if failed(DarkSquad3.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  DarkSquad3.Unlock;


   _vb[0]:=GetCustomVertex(-0.1964, -0.164, 1,0.0,0.0);
  _vb[1]:=GetCustomVertex( -0.143, -0.164, 1,1.0,0.0);
  _vb[2]:=GetCustomVertex(-0.1964,-0.226, 1,0.0,1);
  _vb[3]:=GetCustomVertex( -0.143,-0.226, 1,1.0,1);
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,Sign)) then exit;
  if failed(Sign.Lock(0, SizeOf(_vb), _ptr, 0)) then exit;
  Move(_vb, _ptr^, SizeOf(_vb));
  Sign.Unlock;



scale:=0.2;
offset:=0;
  for _cnt := 0 to 49 do
    begin
      _theta := (2 * PI * _cnt) /30;
      _vb2[2 * _cnt] := GetCustomVertex(offset+scale*sin(_theta), 0.13, offset+scale*cos(_theta),
                                       _cnt/4.9, 1.0);
      _vb2[2 * _cnt + 1] := GetCustomVertex(offset+scale*sin(_theta), 0.3, offset+scale*cos(_theta),
                                           _cnt/4.9, 0.0);
    end;


  // Create a vertex buffer.
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(_vb2), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,NSKY)) then exit;

  // Lock, fill and unlock vertex buffer.
  if failed(NSKY.Lock(0, SizeOf(_vb2), _ptr, 0)) then exit;
  Move(_vb2, _ptr^, SizeOf(_vb2));
  NSKY.Unlock;


  for _cnt:=0 to 3999 do begin
z:=2+_cnt/300;
x:=z*((1000-Random(2000))/6600);
y:=z*((1000-Random(2000))/6600);

      Snow[_cnt]:=GetCustomvertex(x,y,z,0,0);
  end;

  // Create a vertex buffer.
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(Snow), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,SnowBuf)) then exit;
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(Snow), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,ScreenSnowBuf)) then exit;
  // Lock, fill and unlock vertex buffer.
  if failed(SnowBuf.Lock(0, SizeOf(Snow), _ptr, 0)) then exit;
  Move(Snow, _ptr^, SizeOf(Snow));
  SnowBuf.Unlock;

  // All right
  Result := true;
end;


function GetD3DXVector3(_x, _y, _z: single): TD3DXVector3;

begin
  Result.x := _x;
  Result.y := _y;
  Result.z := _z;
end;




procedure SetupMatrices;

var _v1, _v2, _v3 : TD3DXVector3;

begin
  // World.
//  D3DXMatrixRotationX(matWorld, sin(GetTickCount/2100) * 14.3);
//  D3DDEV8.SetTransform(D3DTS_WORLD, matWorld);

  // View.
  _v1 := GetD3DXVector3( 0.0, 0.0, -1.0);
  _v2 := GetD3DXVector3( 0.0, 0.0,  0.0);
  _v3 := GetD3DXVector3( 0.0, 1.0,  0.0);
  D3DXMatrixLookAtLH(matView, _v1, _v2, _v3);
  D3DDEV8.SetTransform(D3DTS_VIEW, matView);

  // Projection.
  D3DXMatrixPerspectiveFovLH(matProj, PI/12, 1.0, 1.0, 200.0);
  D3DDEV8.SetTransform(D3DTS_PROJECTION, matProj);
end;

procedure SetupMaterialAndLight;

var
    DayLight,NightLight : TD3DLight8;
    _vecDir : TD3DXVector3;

begin
  // Set Material
  FillChar(Screenmat, sizeof(Screenmat), 0 );
  with Screenmat do
    begin

      Emissive.r := 1;
      Emissive.g := 1;
      Emissive.b := 1;

    end;

  FillChar(Screenmat2, sizeof(Screenmat2), 0 );
  with Screenmat2 do
    begin
      Emissive.r := 1;
      Emissive.g := 1;
      Emissive.b := 1;

    end;

  FillChar(Nightmat, sizeof(Nightmat), 0 );
  with Nightmat do
    begin
     Emissive.r := 1;
     Emissive.g := 1;
     Emissive.b := 1;
    end;

// LIGHTERS ******************************
  FillChar(tl_green_mat, sizeof(tl_green_mat), 0 );
  with tl_green_mat do
    begin
      Emissive.r := 0;
      Emissive.g := 1;
      Emissive.b := 0;
    end;

  FillChar(tl_Yellow_mat, sizeof(tl_Yellow_mat), 0 );
  with tl_yellow_mat do
    begin
      Emissive.r := 1;
      Emissive.g := 1;
      Emissive.b := 0;
    end;


  FillChar(tl_red_mat, sizeof(tl_red_mat), 0 );
  with tl_red_mat do
    begin
      Emissive.r := 1;
      Emissive.g := 0;
      Emissive.b := 0;
    end;

  FillChar(tl_Blue_mat, sizeof(tl_Blue_mat), 0 );
  with tl_Blue_mat do
    begin
      Emissive.r := 0;
      Emissive.g := 0;
      Emissive.b := 1;
    end;

  FillChar(tl_White_mat, sizeof(tl_White_mat), 0 );
  with tl_White_mat do
    begin
      Emissive.r := 1;
      Emissive.g := 1;
      Emissive.b := 1;
    end;


  FillChar(tl_Black_mat, sizeof(tl_Black_mat), 0 );
  with tl_Black_mat do
    begin
      Emissive.r := 0.5;
      Emissive.g := 0.5;
      Emissive.b := 0.5;
    end;
//1D------------Изменение прозрачности окна динамики состава--------------------
    FillChar(tl_Black_mat_chart, sizeof(tl_Black_mat_chart), 0 );
  with tl_Black_mat_chart do
    begin
      Emissive.r :=0.9;// 0.95;
      Emissive.g :=0.9;// 0.95;
      Emissive.b :=0.9;// 0.95;
    end;
//1D----------------------------------------------------------------------------

// END of LIGHTERS ******************************


  FillChar(DayLight, sizeof(DayLight), 0 );
  with DayLight do
    begin

        _Type := D3DLIGHT_spot ;
      Ambient.r := 0.01;
      Ambient.g := 0.01;
      Ambient.b := 0.01;
      Diffuse.r := 0.1;
      Diffuse.g := 0.1;
      Diffuse.b := 0.1;
      Specular.r := 1.0;
      Specular.g := 1.0;
      Specular.b := 1.0;
      Attenuation0:=10;
      Theta:=1;
      Phi:=4;
      Falloff:=10;
      Range   := 1000.0;
      _vecDir := D3DXVECTOR3(0,10,-2);
       D3DXVec3Normalize(Position, _vecDir);
      _vecDir := D3DXVECTOR3(0, -0.013, 0.6569);
      D3DXVec3Normalize(Direction, _vecDir);

end;
      D3DDEV8.SetLight(0, DayLight);
      d3ddev8.LightEnable(0, true);

  FillChar(NightLight, sizeof(NightLight), 0 );
  with NightLight do
    begin

      _Type := D3DLIGHT_spot ;
      Ambient.r := 0.01;
      Ambient.g := 0.01;
      Ambient.b := 0.01;
      Diffuse.r := 0.1;
      Diffuse.g := 0.1;
      Diffuse.b := 0.1;
//      Specular.r := 1.0;
//      Specular.g := 1.0;
//      Specular.b := 1.0;
      Attenuation0:=10;
      Theta:=0.15;
      Phi:=0.4;
      Falloff:=10;
      Range   := 1000.0;
      _vecDir := D3DXVECTOR3(0,0,0);
       D3DXVec3Normalize(Position, _vecDir);
//      Position:=_vecdir;
//      _tim := GetTickCount / 100;
//      _tim:=main.trackbar10.position/100;
      _vecDir := D3DXVECTOR3(0, -0.013, 0.6569);
//form1.caption:=floattostr(sin(7));
//      _vecDir := D3DXVECTOR3(0,0,0);
      D3DXVec3Normalize(Direction, _vecDir);
//      direction:=_vecdir;
      D3DDEV8.SetLight(1, NightLight);

//      D3DDEV8.LightEnable(0, true);

    end;
end;


procedure TVideoForm.FormActivate(Sender: TObject);
begin
dxready:=true;
btnStartS.Click;
end;

procedure DrawScreen;
var oldWorld:td3dxmatrix;
begin
  D3DDEV8.GetTransform(D3DTS_WORLD,oldworld);
  D3DDEV8.SetTransform(D3DTS_WORLD,ScreenScale);
  D3DDEV8.SetMaterial(Screenmat);

  D3DDEV8.SetTextureStageState(0, D3DTSS_TEXCOORDINDEX, 0);
  D3DDEV8.SetTextureStageState(0, D3DTSS_TEXTURETRANSFORMFLAGS, D3DTTFF_DISABLE);




    // Set the stream source.
  D3DDEV8.SetStreamSource(0, Scr1, sizeof(TCustomVertex));
  // Set the vertex shader.
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);

//  D3DDEV8.SetTextureStageState(0, D3DTSS_TEXCOORDINDEX, 0);
  D3DDEV8.SetTextureStageState(0, D3DTSS_COLORARG1, D3DTA_TEXTURE);
  D3DDEV8.SetTextureStageState(0, D3DTSS_COLOROP, D3DTOP_MODULATE);
if isvideo then  D3DDEV8.SetTexture(0,texturescr1) else D3DDEV8.SetTexture(0,walltex);


  // Render the vertices.
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);

{  D3DDEV8.SetMaterial(Screenmat2);

  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,D3DBLEND_srcalpha);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_invsrccolor);
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);
 }
   D3DDEV8.SetStreamSource(0, Scr2, sizeof(TCustomVertex));
  // Set the vertex shader.
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);

if isvideo then  D3DDEV8.SetTexture(0,texturescr2) else D3DDEV8.SetTexture(0,walltex);

  // Render the vertices.
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);

  D3DDEV8.SetTransform(D3DTS_WORLD,oldworld);

end;

Procedure DrawLense;
begin
  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,D3DBLEND_srccolor);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_srccolor);
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
  D3DDEV8.SetMaterial(tl_red_mat);
  D3DDEV8.SetStreamSource(0, Lense, sizeof(TCustomVertex));
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);
  D3DDEV8.SetTexture(0,tl_LenseTex);
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLEFAN, 0,19);
  D3DDEV8.SetRenderState( D3DRS_ALPHABLENDENABLE, 0);

end;

Procedure DrawNight;
var oldWorld:td3dxmatrix;
begin
// Draw Night
  D3DDEV8.GetTransform(D3DTS_WORLD,oldworld);
  D3DDEV8.SetTransform(D3DTS_WORLD,ScreenScale);

  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,  D3DBLEND_ZERO);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_SRCCOLOR);



//1S--------------- Прожектор ярко тускло --------------------------------------
if isJectorPro>0 then begin
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
  D3DDEV8.SetMaterial(Nightmat);
  D3DDEV8.SetStreamSource(0, NightScr, sizeof(TCustomVertex));
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);
  if isJectorPro=1 then  D3DDEV8.SetTexture(0,NightTex1);
  if isJectorPro=2 then  D3DDEV8.SetTexture(0,NightTex);
//1S----------------------------------------------------------------------------

  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);

D3DDEV8.SetRenderState( D3DRS_ALPHABLENDENABLE, 0);

end;

D3DDEV8.SetTransform(D3DTS_WORLD,oldworld);

end;

Procedure drawSky;
var oldWorld:td3dxmatrix;
mmm2,mmm:td3dmatrix;
begin
 D3DDEV8.GetTransform(D3DTS_WORLD,oldworld);
  D3DXMatrixIdentity(mmm);
  D3DXMatrixIdentity(mmm2);
  D3DXMatrixTranslation(mmm2,0,0,0);
  D3DXMatrixRotationY(mmm,(pi*tempalpha)/180);
D3DDEV8.SetTransform(D3DTS_WORLD,mmm);


  D3DDEV8.SetMaterial(Nightmat);

  D3DDEV8.SetTexture(0,Nskytex);
  D3DDEV8.SetStreamSource(0, NSKY, sizeof(TCustomVertex));
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,98);


D3DDEV8.SetTransform(D3DTS_WORLD,oldworld);
end;

Procedure DrawFog;
var oldWorld:td3dxmatrix;
begin
// Draw Night
  D3DDEV8.GetTransform(D3DTS_WORLD,oldworld);
  D3DDEV8.SetTransform(D3DTS_WORLD,ScreenScale);
  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,D3DBLEND_srccolor);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_invsrccolor);
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
  D3DDEV8.SetMaterial(Nightmat);
  D3DDEV8.SetStreamSource(0, FogScr, sizeof(TCustomVertex));
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);
  D3DDEV8.SetTexture(0,FogTex);

  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);
  D3DDEV8.SetRenderState( D3DRS_ALPHABLENDENABLE, 0);

D3DDEV8.SetTransform(D3DTS_WORLD,oldworld);
end;

Procedure DrawSnowFog;
var oldWorld:td3dxmatrix;
begin
// Draw Night
  D3DDEV8.GetTransform(D3DTS_WORLD,oldworld);
  D3DDEV8.SetTransform(D3DTS_WORLD,ScreenScale);
  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,D3DBLEND_srccolor);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_invsrccolor);
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
  D3DDEV8.SetMaterial(Nightmat);
  D3DDEV8.SetStreamSource(0, FogScr, sizeof(TCustomVertex));
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);
  D3DDEV8.SetTexture(0,SnowFogTex);

  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);
  D3DDEV8.SetRenderState( D3DRS_ALPHABLENDENABLE, 0);

D3DDEV8.SetTransform(D3DTS_WORLD,oldworld);
end;

Procedure DrawList;
var i,p11,rr:integer;
s,ss:string;
begin

  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,  D3DBLEND_ZERO);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_SRCCOLOR);

  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
  D3DDEV8.SetMaterial(tl_blue_mat);
  D3DDEV8.SetStreamSource(0, DarkSquad3, sizeof(TCustomVertex));
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);
  D3DDEV8.SetTexture(0,tl_lensetex);
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);


 Font0._Begin;
  if SpisokFlag11 then D3DXFontWrite(Font0, 'УКАЖИТЕ НЕИСПРАВНОСТЬ', Rect(300,45, 0, 0), 0, $FFFFFFFF) else
    D3DXFontWrite(Font0, 'УКАЖИТЕ ПРИЧИНУ', Rect(300,45, 0, 0), 0, $FFFFFFFF);

i:=1;
for p11:=Startindex to stopindex do begin
s:=form1.listbox11.items[p11];

  if length(s)<90 then begin
   if CursorIndex=p11 then begin
      D3DXFontWrite(Font0, s, Rect(50,60+i*20, 0, 0), 0, $FF000000);
      D3DXFontWrite(Font0, s, Rect(46,56+i*20, 0, 0), 0, $ffff0000);
     end
      else
      D3DXFontWrite(Font0, s, Rect(50,60+i*20, 0, 0), 0, $ffffff00) ;
      inc(i,2);
  end else begin

rr:=90;
 while s[rr]<>' ' do dec(rr);
   ss:=copy(s,1,rr);
   s:=copy(s,rr,length(s));

   if CursorIndex=p11 then begin
      D3DXFontWrite(Font0, ss, Rect(50,60+i*20, 0, 0), 0, $FF000000);
      D3DXFontWrite(Font0, ss, Rect(46,56+i*20, 0, 0), 0, $ffff0000);
      inc(i);
      D3DXFontWrite(Font0, s, Rect(50,60+i*20, 0, 0), 0, $FF000000);
      D3DXFontWrite(Font0, s, Rect(46,56+i*20, 0, 0), 0, $ffff0000);
      inc(i);
     end
      else begin
      D3DXFontWrite(Font0, ss, Rect(50,60+i*20, 0, 0), 0, $ffffff00) ;
      inc(i);
      D3DXFontWrite(Font0, s, Rect(50,60+i*20, 0, 0), 0, $ffffff00) ;
      inc(i);
     end;


  end;


end;

 Font0._End;

   D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 0);
end;


Procedure DrawChart;
var
  w,h,wbar,hbar:single;
  _Cnt:Integer;
  _ptr : pByte;
  a:single;
  Thin:single;
  ScaleFactor:integer;
  tempi:integer;
  s:string;
  t_ChartCount:string;
  t_ChartCount_2:string;
begin
  w:=0.2;
  h:=0.15;
  if MaxChartValue<50
    then ScaleFactor:=50
    else if MaxChartValue<100
           then ScaleFactor:=100
           else ScaleFactor:=200;

  t_ChartCount:=inttostr(ChartCount);
  t_ChartCount_2:=inttostr(round(ChartCount/2));
  for _Cnt:=0 to ChartCount-1 do
    begin
      wbar:=w/ChartCount;
      Thin:=Wbar/7;
      a:=ChartData[_cnt].value;
      hbar:=a*(((h/2)*(1-(1/(ChartCount/2))))/ScaleFactor);

      MyChart[_Cnt*6]:=GetCustomVertex(_Cnt*wbar-0.242,0.18+Hbar,1,0,-1);               // 1
      MyChart[_Cnt*6+1]:=GetCustomVertex(_Cnt*wbar+wbar-Thin-0.242,0.18+hbar,1,0,-1);   // 2
      MyChart[_Cnt*6+2]:=GetCustomVertex(_Cnt*wbar-0.242,0.18,1,0,-1);                  // 3
      MyChart[_Cnt*6+3]:=MyChart[_Cnt*6+2];                                             // 4
      MyChart[_Cnt*6+4]:=MyChart[_Cnt*6+1];                                             // 5
      MyChart[_Cnt*6+5]:=GetCustomVertex(_Cnt*wbar+wbar-Thin-0.242,0.18,1,0,-1);        // 6
    end; // for

  MyChart[900]:=GetCustomVertex(-0.242,0.18,1,0,-1);
  MyChart[901]:=GetCustomVertex(-0.04,0.18,1,0,-1);

  MyChart[902]:=GetCustomVertex(-0.242,0.18+(((h/2)*50)/ScaleFactor),1,0,-1);
  MyChart[903]:=GetCustomVertex(-0.04,0.18+(((h/2)*50)/ScaleFactor),1,0,-1);
  MyChart[904]:=GetCustomVertex(-0.242,0.18+(((h/2)*100)/ScaleFactor),1,0,-1);
  MyChart[905]:=GetCustomVertex(-0.04,0.18+(((h/2)*100)/ScaleFactor),1,0,-1);
  MyChart[906]:=GetCustomVertex(-0.242,0.18+(((h/2)*200)/ScaleFactor),1,0,-1);
  MyChart[907]:=GetCustomVertex(-0.04,0.18+(((h/2)*200)/ScaleFactor),1,0,-1);

  MyChart[908]:=GetCustomVertex(-0.242,0.18-(((h/2)*50)/ScaleFactor),1,0,-1);
  MyChart[909]:=GetCustomVertex(-0.04,0.18-(((h/2)*50)/ScaleFactor),1,0,-1);
  MyChart[910]:=GetCustomVertex(-0.242,0.18-(((h/2)*100)/ScaleFactor),1,0,-1);
  MyChart[911]:=GetCustomVertex(-0.04,0.18-(((h/2)*100)/ScaleFactor),1,0,-1);
  MyChart[912]:=GetCustomVertex(-0.242,0.18-(((h/2)*200)/ScaleFactor),1,0,-1);
  MyChart[913]:=GetCustomVertex(-0.04,0.18-(((h/2)*200)/ScaleFactor),1,0,-1);

  // Create a vertex buffer.
  if failed(D3DDEV8.CreateVertexBuffer(SizeOf(MyChart), 0,D3DFVF_CUSTOMVERTEX,D3DPOOL_DEFAULT,ChartBuf)) then exit;
  // Lock, fill and unlock vertex buffer.
  if failed(ChartBuf.Lock(0, SizeOf(MyChart), _ptr, 0)) then exit;
  Move(MyChart, _ptr^, SizeOf(MyChart));
  ChartBuf.Unlock;

  //---------------- Отрисовка темного квадрата --------------------------------
  //14M--- Отображение или скрытие графика динамики  по нажатию кнопки ---------
  if DinamicChartDraw then begin
  //14M-------------------------------------------------------------------------

  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,  D3DBLEND_ZERO);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_SRCCOLOR);

  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
//1D------------Изменение прозрачности окна динамики состава--------------------
  D3DDEV8.SetMaterial(tl_Black_mat_chart);
//1D----------------------------------------------------------------------------
  D3DDEV8.SetStreamSource(0, DarkSquad, sizeof(TCustomVertex));
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);
  D3DDEV8.SetTexture(0,tl_lensetex);

  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);
  //14M--- Отображение или скрытие графика динамики  по нажатию кнопки ---------
  end; // if
  //14M-------------------------------------------------------------------------

  //------------------ Отрисовка квадрата информации ---------------------------
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 0);
  D3DDEV8.SetTexture(0,PanelTEx);
  D3DDEV8.SetMaterial(tl_white_mat);
  D3DDEV8.SetStreamSource(0, DarkSquad2, sizeof(TCustomVertex));
  D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);

  //------------ Отрисовка знаков в квадрате информации ------------------------
  If pathsigns.visible then
    begin
      D3DDEV8.SetTexture(0,Signs[pathsigns.dItems[pathsigns.cursign].id].Picture);
      D3DDEV8.SetStreamSource(0, Sign, sizeof(TCustomVertex));
      D3DDEV8.DrawPrimitive(D3DPT_TRIANGLESTRIP, 0,2);
    end;

  D3DDEV8.SetTexture(0,tl_lensetex);
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);
  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,  D3DBLEND_SRCCOLOR);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_SRCALPHA);

  D3DDEV8.SetStreamSource(0, ChartBuf, sizeof(TCustomVertex));

  //14M--- Отображение или скрытие графика динамики  по нажатию кнопки ---------
  if DinamicChartDraw then begin
  //14M-------------------------------------------------------------------------
  for _Cnt:=0 to ChartCount-1 do
    begin
      if ChartData[_Cnt].Color=0
        then D3DDEV8.SetMaterial(tl_Green_Mat)
        else if ChartData[_Cnt].Color=2
               then D3DDEV8.SetMaterial(tl_Red_Mat)
               else if ChartData[_Cnt].Color=1
                      then D3DDEV8.SetMaterial(tl_Blue_Mat);
      D3DDEV8.DrawPrimitive(D3DPT_TRIANGLEList, _Cnt*6,2);
    end;
  D3DDEV8.SetMaterial(tl_White_mat);

  case ScaleFactor of
    50:  D3DDEV8.DrawPrimitive(D3DPT_LineList, 900,5);
    100: D3DDEV8.DrawPrimitive(D3DPT_LineList, 900,6);
    200: D3DDEV8.DrawPrimitive(D3DPT_LineList, 900,7);
  end;
  //14M--- Отображение или скрытие графика динамики  по нажатию кнопки ---------
  end; // if
  //14M-------------------------------------------------------------------------

  Font0._Begin;
    //14M--- Отображение или скрытие графика динамики  по нажатию кнопки -------
    if DinamicChartDraw then begin
    //14M-----------------------------------------------------------------------
    case ScaleFactor of
      50: begin
            ///  D3DDEV8.DrawPrimitive(D3DPT_LineList, 900,5);
            D3DXFontWrite(Font0, '0', Rect(22,88, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '50', Rect(15,2, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '-50', Rect(10,172, 0, 0), 0, $FFFFFFFF);
          end;
     100: begin
            ///   D3DDEV8.DrawPrimitive(D3DPT_LineList, 900,6);
            D3DXFontWrite(Font0, '0', Rect(22,88, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '50', Rect(15,45, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '100', Rect(6,2, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '-50', Rect(10,130, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '-100', Rect(2,172, 0, 0), 0, $FFFFFFFF);
          end;
     200: begin
            ///    D3DDEV8.DrawPrimitive(D3DPT_LineList, 900,7);
            D3DXFontWrite(Font0, '0', Rect(22,88, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '50', Rect(15,65, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '100', Rect(6,45, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '200', Rect(6,2, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '-50', Rect(10,108, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '-100', Rect(2,130, 0, 0), 0, $FFFFFFFF);
            D3DXFontWrite(Font0, '-200', Rect(2,172, 0, 0), 0, $FFFFFFFF);
          end;
    end; // case  

    D3DXFontWrite(Font0, '1', Rect(29,186, 0, 0), 0, $FFFFFFFF);
    D3DXFontWrite(Font0,t_ChartCount_2 , Rect(175,186, 0, 0), 0, $FFFFFFFF);
    D3DXFontWrite(Font0,t_ChartCount , Rect(328,186, 0, 0), 0, $FFFFFFFF);
    //14M--- Отображение или скрытие графика динамики  по нажатию кнопки -------
    end; // if
    //14M-----------------------------------------------------------------------

    s:=FloattostrF(MyMove.ukl,ffnumber,4,1);//inttostr(round(p.spd));
    D3DXFontWrite(Font0,s , Rect(64-Length(s)*3,487, 0, 0), 0, $FFFFFFFF);

//1I---------------- Отображение визуальных пикетов ----------------------------
           tempi:=round(RealKM-0.5);
           s:=SKM;

           if VDE_Version='VDS27' then  s:=inttostr(VisPikets.CurVisKm);
           D3DXFontWrite(Font0,s , Rect(64-Length(s)*3,514, 0, 0), 0, $FFFFFFFF);

           s:=SPIK;
           if VDE_Version='VDS27' then  s:=inttostr(VisPikets.CurVisPk);
           D3DXFontWrite(Font0,s , Rect(64-Length(s)*3,543, 0, 0), 0, $FFccFFFF);
//1I----------------- Конец Отображения визуальных пикетов ---------------------


    s:=SVogr;
    D3DXFontWrite(Font0,s , Rect(72-Length(s)*3,572, 0, 0), 0, $FFFF0000);

    if pathsigns.visible then
      begin
        s:=inttostr(pathsigns.Distance)+'m';
        D3DXFontWrite(Font0,s , Rect(142-Length(s)*3,572, 0, 0), 0, $FFFFff00);
      end;
  Font0._End;
  D3DDEV8.SetRenderState( D3DRS_ALPHABLENDENABLE, 0);
end;


Procedure DrawSnow;
var i:integer;
    _ptr : pByte;

begin
// UpdateSnow *************************************************
if isVideoPlayed then Snowstep:=0.02 else Snowstep:=0.008;

//rand:=random(10)+1;
//rand:=4;
for i:=0 to 3999 do begin
Snow[i].Position.z:=Snow[i].Position.z-SnowSpeed;
if Snow[i].Position.z<2 then begin
   Snow[i].Position.z:=4+i/100;
{   if ScreenSnowCount<4000 then sccount:=ScreenSnowCount else sccount:=ScreenSnowCount-4000;
   ScreenSnow[sccount].Position.x:=0.5-Random;
   ScreenSnow[sccount].Position.y:=0.5-Random;
   ScreenSnow[sccount].Position.z:=1;

   inc(ScreenSnowCount); if ScreenSnowCount=6000 then ScreenSnowCount:=0;}
end;



Snow[i].Position.y:=Snow[i].Position.y-Snowstep;
if Snow[i].Position.y<-0.6 then Snow[i].Position.y:=Snow[i].Position.z/3;

end;

  if failed(SnowBuf.Lock(0, SizeOf(Snow), _ptr, 0)) then exit;
  Move(Snow, _ptr^, SizeOf(Snow));
  SnowBuf.Unlock;

{  if failed(ScreenSnowBuf.Lock(0, SizeOf(ScreenSnow), _ptr, 0)) then exit;
  Move(ScreenSnow, _ptr^, SizeOf(ScreenSnow));
  ScreenSnowBuf.Unlock;
 }

// *************************************************************
  D3DDEV8.SetMaterial(Nightmat);
 d3ddev8.SetRenderState(D3DRS_POINTSPRITEENABLE,1);
 d3ddev8.SetRenderState(D3DRS_POINTSCALEENABLE,1);
  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,D3DBLEND_srccolor);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_srcalpha);
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);


  D3DDEV8.SetTexture(0,SnowTex);
    // Set the stream source.
  D3DDEV8.SetStreamSource(0, SnowBuf, sizeof(TCustomVertex));
  // Set the vertex shader.
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);

  D3DDEV8.DrawPrimitive(D3DPT_POINTLIST, 0,4000);

{  D3DDEV8.SetStreamSource(0, ScreenSnowBuf, sizeof(TCustomVertex));
  if ScreenSnowCount<4000 then
  D3DDEV8.DrawPrimitive(D3DPT_POINTLIST, 0,ScreenSnowCount) else
  D3DDEV8.DrawPrimitive(D3DPT_POINTLIST, 0,4000);}
  D3DDEV8.SetRenderState( D3DRS_ALPHABLENDENABLE, 0);
  d3ddev8.SetRenderState(D3DRS_POINTSPRITEENABLE,0);
  d3ddev8.SetRenderState(D3DRS_POINTSCALEENABLE,0);

end;

Procedure DrawRain;
var i:integer;
    _ptr : pByte;

begin
// UpdateSnow *************************************************
if isVideo then Snowstep:=0.07 else Snowstep:=0.02;

//rand:=random(10)+1;
//rand:=4;
for i:=0 to 3999 do begin
Snow[i].Position.z:=Snow[i].Position.z-SnowSpeed;
if Snow[i].Position.z<2 then Snow[i].Position.z:=4+i/100;

Snow[i].Position.y:=Snow[i].Position.y-Snowstep;
if Snow[i].Position.y<-0.6 then Snow[i].Position.y:=Snow[i].Position.z/5;

end;

  if failed(SnowBuf.Lock(0, SizeOf(Snow), _ptr, 0)) then exit;
  Move(Snow, _ptr^, SizeOf(Snow));
  SnowBuf.Unlock;



// *************************************************************
  D3DDEV8.SetMaterial(Nightmat);
 d3ddev8.SetRenderState(D3DRS_POINTSPRITEENABLE,1);
 d3ddev8.SetRenderState(D3DRS_POINTSCALEENABLE,1);
  D3DDEV8.SetRenderState(D3DRS_SRCBLEND,D3DBLEND_srccolor);
  D3DDEV8.SetRenderState(D3DRS_DESTBLEND, D3DBLEND_srcalpha);
  D3DDEV8.SetRenderState(D3DRS_ALPHABLENDENABLE, 1);


  D3DDEV8.SetTexture(0,dropTex);
    // Set the stream source.
  D3DDEV8.SetStreamSource(0, SnowBuf, sizeof(TCustomVertex));
  // Set the vertex shader.
  D3DDEV8.SetVertexShader(D3DFVF_CUSTOMVERTEX);

  D3DDEV8.DrawPrimitive(D3DPT_POINTLIST, 0,4000);
  D3DDEV8.SetRenderState( D3DRS_ALPHABLENDENABLE, 0);
  d3ddev8.SetRenderState(D3DRS_POINTSPRITEENABLE,0);
  d3ddev8.SetRenderState(D3DRS_POINTSCALEENABLE,0);

end;

Procedure DrawObjects;
var i:integer;
begin
if T3DObjList.count>0 then
  for i:=0 to T3DObjList.count-1 do if TSimpleObj(t3dObjList[i]).visible then TSimpleObj(t3dObjList[i]).draw(OveralFRame);

end;

procedure RenderFrame;
begin
  D3DDEV8.Clear(0, NIL, D3DCLEAR_TARGET or D3DCLEAR_ZBUFFER,
                $FF000000, 1.0, 0);

  D3DDEV8.BeginScene;

  SetupMatrices;

if (isVideo) and(Realframe<>RealFrameOld) then UpdateScreen;
if RealFrameOld<0 then
 RealFrameOld:=RealFrameOld+1
else  RealFrameOld:=RealFrame;
DrawScreen;
if isNight then DrawNight;
if NextLighter<>nil Then NextLighter.Draw(Overalframe-NextLighter.StartFrame);
if CurTrafficLighter<>nil Then CurTrafficLighter.Draw(Overalframe-CurTrafficLighter.StartFrame);
SnowSpeed:=v[0]/40;
//Videoform.Edit2.Text:=floattostr(AVISpeed);

{if isSnow then if not isNight then DrawSnowFog ;
if isRain then if not isNight then DrawSnowFog;}
{if isFog then DrawFog;
if isNight then DrawNight;
if isSkies then drawsky;
 }
{if isFog then d3ddev8.SetRenderState(D3DRS_FOGENABLE,1);
if isSnow then begin
if not isNight then begin DrawSnowFog;d3ddev8.SetRenderState(D3DRS_FOGENABLE,1);end;
     DrawSnow;
  end;

if isRain then begin
if not isNight then begin DrawSnowFog;d3ddev8.SetRenderState(D3DRS_FOGENABLE,1);end;
     Drawrain;
  end;}

if (isSnow or IsRain or isFog) and (not isNight) then d3ddev8.SetRenderState(D3DRS_FOGENABLE,1);



//1
  d3ddev8.SetRenderState(D3DRS_ALPHATESTENABLE ,1);
  d3ddev8.SetRenderState(D3DRS_ALPHAFUNC ,D3DCMP_GREATEREQUAL);
  d3ddev8.SetRenderState(D3DRS_ALPHAREF ,$FF);

{  if obj<>nil then begin

     if main.checkbox19.checked then begin
               Obj.Rot.y:=Obj.Rot.y+0.01;
               Obj.CompileMatrix;
     end;

  obj.render;
  end;
 }
DrawObjects;
//2
d3ddev8.SetRenderState(D3DRS_ALPHATESTENABLE ,0);
  d3ddev8.SetRenderState(D3DRS_ALPHAFUNC ,6);

  d3ddev8.SetRenderState(D3DRS_FOGENABLE,0);


  d3ddev8.SetRenderState(D3DRS_ZENABLE,D3DZB_FALSE);
//  if main.checkbox25.checked then
if not isNight then  DrawHelpers;




if isFog then DrawFog;
//if isNight then DrawNight;
if isSkies then drawsky;

if isSnow then begin
if not isNight then DrawSnowFog ; DrawSnow;
  end;

if isRain then begin
if not isNight then DrawSnowFog; Drawrain;
  end;

  d3ddev8.SetRenderState(D3DRS_ZENABLE,D3DZB_TRUE);

  d3ddev8.SetRenderState(D3DRS_FOGENABLE,0);


{if main.checkbox21.checked then }DrawChart;
 //Вывод и отображение на экране заданных и действительных перемещений рам платформы
///Form1.SEWDrive.SetCurDist(HLr,HRr,HMr);
 Font0._Begin;
   D3DXFontWrite(Font0, timetostr(time), Rect(627, 581, 200, 25), 0, $aa000000);
  D3DXFontWrite(Font0, Timetostr(time), Rect(625, 580, 200, 25), 0, $ffFFFFFF);

 // D3DXFontWrite(Font0, Format('FPS: %.1f' , [FPS]), Rect(7, 581, 200, 25), 0, $aa000000);
 // D3DXFontWrite(Font0, Format('FPS: %.1f' , [FPS]), Rect(5, 580, 200, 25), 0, $ffFFFFFF);

  Font0._End;
 
 if isDrawList then DrawList;

  // End the scene.
  D3DDEV8.EndScene;

  // Display scene.
  D3DDEV8.Present(NIL, NIL, 0, NIL);
end;




procedure TVideoForm.AppOnIdle(Sender: TObject; var Done: Boolean);
var
s1:word;
kfc:single;
f:textfile;
tempSendCode:boolean;
begin
  Done := false;
  if (not DXReady) or (not WindowReady) then exit;

if overalframe>=TrainEndPosition then
VideoForm.close;

{// переключение светофоров
if CurTrafficLighter<>nil then
if v[0]>=0 then
If CurTrafficLighter.EndFrame<OveralFrame then
begin
 TempSendcode:=Videofrm.CurTrafficLighter.SendCode;
 lastLight:=Videofrm.CurTrafficLighter.activelight;
 CurTrafficLighter:=CurTrafficLighter.NextLighter;
 XAlsnInstr:=kader2meter(CurTrafficLighter.EndFrame);//Instruktor
 InstrForm.UpdateLightersInstruktor;//Instruktor

 if Videofrm.CurTrafficLighter.SendCode<>tempsendcode then
  if Videofrm.CurTrafficLighter.SendCode=false then
  begin
   T3dSound(SoundObjects.items[23]).play;
   MainTimer.StartTimerDomain(4);
   LightRB:=true;

  end else T3dSound(SoundObjects.items[25]).play;      }
//Вставить





if CurTrafficLighter<>nil then
if v[0]>=0 then
If CurTrafficLighter.EndFrame<OveralFrame then begin
 TempSendcode:=Videofrm.CurTrafficLighter.SendCode;
 lastLight:=Videofrm.CurTrafficLighter.activelight;
 CurTrafficLighter:=CurTrafficLighter.NextLighter;
 XAlsnInstr:=kader2meter(CurTrafficLighter.EndFrame);//Instruktor
 InstrForm.UpdateLightersInstruktor;//Instruktor

if LastLight=3 then
 begin
  if Uprava.count=0 then
   begin
    Uprava.Add('Проезд запрещающего сигнала на '+SKM+' километре '+SPIK+' пк. со скоростью '+inttostr(Round(V[0]*3.6)));
   CoorFaults.Add(floattostr(realmeter));//Save lent
   end;
  if (Uprava.Strings[Uprava.count-1]<>'Проезд запрещающего сигнала на '+SKM+' километре '+SPIK+' пк. со скоростью '+inttostr(Round(V[0]*3.6)))or (Uprava.count=0) then
  begin
   Uprava.Add('Проезд запрещающего сигнала на '+SKM+' километре '+SPIK+' пк. со скоростью '+inttostr(Round(V[0]*3.6)));
   CoorFaults.Add(floattostr(realmeter));//Save lent
  end;
 end;
//Вставить


if CurTrafficLighter.NextLighter<>nil then
if CurTrafficLighter.NextLighter.startframe<=CurTrafficLighter.EndFRame then NextLighter:=CurTrafficLighter.NextLighter else NextLighter:=nil;
if VideoFrm.CurTrafficLighter.ActiveLight=3 then
    begin
     if lastredmetter=-1 then begin lastredmetter:=round(kader2meter(overalframe)); end;
    end else lastredmetter:=-1;
  SelectCurActiveLight(false);
  CurSemaphoreKm:=kader2meter(VideoFRM.CurTrafficLighter.EndFrame)/1000; // вставил для клуба
end else
 if v[0]<-0.2 then
 if CurTrafficLighter.StartFrame>OveralFrame then begin
  CurTrafficLighter:=CurTrafficLighter.PrevLighter;
   SelectCurActiveLight(false);
   CurSemaphoreKm:=kader2meter(VideoFRM.CurTrafficLighter.EndFrame)/1000;
   end;

  SoundPOints.ProceedPoint;
  RenderFrame;

  // Speed control
  while GetTickCount - FRAMECONTROL < FRAMEAT do Sleep(0);
  LASTFRAMETIME := GetTickCount - FRAMECONTROL;
  FRAMECONTROL := GetTickCount;
  inc(FRAMECOUNT);

MainTimer.proceed;

CalculateAviSpeed;
if fps=0 then fps:=25;
REalframe2:=RealFRame2+(((AviFPS*3.6)/AviSpeed)*TrainSpd)/fps;
Overalframe:=round(realframe2-0.5);
if (Overalframe>CurAviFile.Length+CurAviFile.offset) or (Overalframe<CurAviFile.offset) then
SelectAvifile;
realframe:=overalFRame-CurAviFile.offset+CurAviFile.startpos;
TrainPosM:=kader2meter(Overalframe);
realmeter:=trainPosM;
if trainposM=0 then kfc:=1 else
begin
if v[0]>=0 then
kfc:=abs(1+(x[0]-TrainPosM)/3) else
  kfc:=abs(1+(TrainPosM-x[0])/3);
end;
Trainspd:=v[0]*kfc;
lastframe:=Overalframe;

//Objects instruktor   //Instruktor
if OveralFrame>CurObj.EndFrame then
 begin
  if CurObjIndex<t3dObjList.Count-2 then     //Instruktor
   begin
    CurObjIndex:=CurObjIndex+1;
    CurObj:=TSimpleObj(t3dObjList[CurObjIndex]);  //Instruktor
    InstrForm.CheckCurObjInstruktor;   //Instruktor
   end else CurObjIndex:=-1;
 end;   //Instruktor

  if (FRAMECONTROL - FPSCONTROL > FPS_CHECK) then
  begin

    FPS := (1000 * FRAMECOUNT) / (FRAMECONTROL - FPSCONTROL);
    FPSCONTROL := FRAMECONTROL;
    FRAMECOUNT := 0;
{    trmem:=GetheapStatus;
    main.stat.panels[3].text:='Memory used: '+Inttostr(round(trmem.TotalAllocated/1024))+'k';}
  end;
end;

// ------------------------------------------------------------------

procedure TVideoForm.AppOnActivate(Sender: TObject);

begin
  WindowReady := true;
end;

// ------------------------------------------------------------------

procedure TVideoForm.AppOnDeactivate(Sender: TObject);

begin
///  WindowReady := false;
end;

// -------------------------------------------------------------------

procedure hHalt(_errMsg: pChar);

begin
  MessageBox(0, _errMsg, 'Error', MB_ICONERROR);
  Application.Terminate;
end;

// -------------------------------------------------------------------


procedure TVideoForm.FormDestroy(Sender: TObject);
begin
  played:=false;
  DXReady := false;
  if obj<>nil then begin obj.Destroy;obj:=nil; end;
  Scr1 := NIL; // Free Vertex Buffer
  Scr2 := NIL; // Free Vertex Buffer
  NSKY := NIL;
  NightScr:=nil;
  FogScr:=nil;
  Lense:=nil;
  Snowbuf:=nil;
  Texturescr1 := NIL; // Free Texture;
  Texturescr2 := NIL; // Free Texture;
  WallTex := NIL;
  EnvTex:=nil;
  NightTex:=nil;
  FogTex:=nil;
  NskyTex:=nil;
  SnowTex:=nil;
  SnowFogTex:=nil;
  tl_lenseTex:=nil;
  CloseDGfx; // Free DX
  ImageScr1:=nil;
  ImageScr2:=nil;
  CloseAvi;
end;


procedure TVideoForm.FormCreate(Sender: TObject);
var bbb:hw;
begin

Cursorindex:=0;
startindex:=0;
stopindex:=11;
if form1.ListBox11.Items.count<12 then StopIndex:=form1.ListBox11.Items.count-1 else StopIndex:=11;

videoform.top:=0;
videoform.Height:=600;
videoform.Width:=800;
prct.Left:=0;
prct.Top:=0;
prct.Right:=90;
prct.Bottom:=512;


AviFPS:=25;
  ImageScr1 := TDIB.Create;
  ImageScr1.PixelFormat := MakeDIBPixelFormat(8, 8, 8);
  ImageScr1.SetSize(512,512, 32);
  ImageScr2 := TDIB.Create;
  ImageScr2.PixelFormat := MakeDIBPixelFormat(8, 8, 8);
  ImageScr2.SetSize(512,512, 32);

{FTextureImage := tbitmap.create;
FTextureImage.width := 620;
FTextureImage.Height := 512;}
played:=true;
//TlightersList:=Tlist.create;
ObjList:=Tlist.create;
T3DObjList:=Tlist.create;
  FRAMECONTROL := GetTickCount;

Init_SoundsTrenDLL(PChar(Projectpath+'DATA\SOUNDS\SoundLib.snds'),VideoForm.Handle);

  // Initialize application events
  Application.OnIdle       := AppOnIdle;
  Application.OnActivate   := AppOnActivate;
  Application.OnDeactivate := AppOnDeactivate;
  Application.OnRestore    := AppOnActivate;
  Application.OnMinimize   := AppOnDeActivate;

  // Initialize Direct Graphics (first try with Hardware Accel. (HAL))
  if not InitDGfx(Handle, false,ViewWidth,ViewHeight, 0, 0, true, true) then
    begin
      MessageBox(0, WRN00, 'Warning.', MB_ICONWARNING);
      CloseDGfx;
      if not InitDGfx(Handle, false, 0, 0, 0, 0, false, true) then
        begin hHalt(ERR00); exit; end;
    end;


  d3dxMatrixIdentity(ScreenScale);
  d3dxMatrixScaling(ScreenScale,21,21,1);

  Font0 := D3DXCreateFont('Arial Cyr', 17, true, false, false);

  // Load Screen Texture.
  if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr1FILENAME),512,512,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  Texturescr1)) then
    begin hHalt(ERR02 + scr1FILENAME); exit; end;
  if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr1FILENAME),512,512,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE ,$FFFFFF,nil,nil,  Texturescr2)) then
    begin hHalt(ERR02 + scr1FILENAME); exit; end;
  if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr1FILENAME),512,512,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE ,$FFFFFF,nil,nil,  walltex)) then
    begin hHalt(ERR02 + scr1FILENAME); exit; end;

  if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr2FILENAME),256,256,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  NightTex)) then
   begin hHalt(ERR02 + scr2FILENAME); exit; end;

  //1S--------------------- Проектор ярко --------------------------------------
  if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr21FILENAME),256,256,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  NightTex1)) then
   begin hHalt(ERR02 + scr21FILENAME); exit; end;
  //1S--------------------------------------------------------------------------

 if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr3FILENAME),256,256,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  FogTex)) then
   begin hHalt(ERR02 + scr3FILENAME); exit; end;

 if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr4FILENAME),256,256,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  NskyTEx)) then
   begin hHalt(ERR02 + scr4FILENAME); exit; end;

 if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr5FILENAME),16,16,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  SnowTEx)) then
   begin hHalt(ERR02 + scr5FILENAME); exit; end;

 if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+scr6FILENAME),256,256,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  SnowFogTEx)) then
   begin hHalt(ERR02 + scr6FILENAME); exit; end;

 if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+'Drop.bmp'),16,16,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  dropTEx)) then
   begin hHalt(ERR02 + 'Drop.bmp'); exit; end;

 if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+'tl.bmp'),16,16,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  tl_lenseTex)) then
   begin hHalt(ERR02 + 'tl.bmp'); exit; end;

 if failed(D3DXCreateTextureFromFileExA(D3DDEV8, pchar(texpath+'panel.bmp'),128,128,1,0,D3DFMT_UNKNOWN,1,D3DX_FILTER_LINEAR,D3DX_FILTER_NONE  ,$FFFFFF,nil,nil,  PanelTex)) then
   begin hHalt(ERR02 + 'panel.bmp'); exit; end;

   //   UpdateScreen;

  // Initialize VertexBuffer
  if not InitScreenVB then begin hHalt(ERR01); exit; end;

UpdateScreen;
  // Disable culling.
  D3DDEV8.SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);

  // Set ambiental light.
  D3DDEV8.SetRenderState(D3DRS_AMBIENT, $999999);

  // Enable dithering
  D3DDEV8.SetRenderState(D3DRS_DITHERENABLE, 1);
//  D3DDEV8.SetRenderState(D3DRS_SPECULARENABLE, 1);


d3ddev8.SetRenderState(D3DRS_FOGCOLOR,FOG_DAY_COLOR);
d3ddev8.SetRenderState(D3DRS_RANGEFOGENABLE,1);
//d3ddev8.SetRenderState(D3DRS_FOGstart,0);
//d3ddev8.SetRenderState(D3DRS_FOGEnd,1);
d3ddev8.SetRenderState(D3DRS_FOGTABLEMODE,D3DFOG_EXP);
//d3ddev8.SetRenderState(D3DRS_FOGTABLEMODE,D3DFOG_LINEAR);
bbb.s:=0.013;
d3ddev8.SetRenderState(D3DRS_FOGDENSITY,bbb.i);

  bbb.s:=0.008;
 d3ddev8.SetRenderState(D3DRS_POINTSIZE,bbb.i);
 d3ddev8.SetRenderState(D3DRS_POINTSIZE_MIN,0);
 d3ddev8.SetRenderState(D3DRS_POINTSCALE_A,0);
 bbb.s:=0.03;
 d3ddev8.SetRenderState(D3DRS_POINTSCALE_B,bbb.i);
 bbb.s:=0.03;
 d3ddev8.SetRenderstate(D3DRS_POINTSCALE_C,bbb.i);

  // Set texture filtering
  if USE_TEXTURE_FILTERING then
    begin
      D3DDEV8.SetTextureStageState(0, D3DTSS_MAGFILTER, D3DTEXF_ANISOTROPIC);
      D3DDEV8.SetTextureStageState(0, D3DTSS_MINFILTER, D3DTEXF_ANISOTROPIC);
      D3DDEV8.SetTextureStageState(1, D3DTSS_MAGFILTER, D3DTEXF_ANISOTROPIC);
      D3DDEV8.SetTextureStageState(1, D3DTSS_MINFILTER, D3DTEXF_ANISOTROPIC);

    end;
  SetupMaterialAndLight;
  // All ok
LoadSigns;
end;

Function XtoScr(x:single):single;
begin
//result:=(((2*x)/ViewWidth)-1)*(main.TrackBar7.position/10+1)/5;
end;

Function YtoScr(y:single):single;
begin
//result:=(1-((2*y)/ViewHeight))*(main.TrackBar7.position/10+1)/5;
end;

procedure TVideoForm.FormDeactivate(Sender: TObject);
begin
///dxready:=false; //убрал, чтобы не висла при переключении окон
end;

procedure TVideoForm.FormKeyDown(Sender: TObject; var Key: Word;
  Shift: TShiftState);
var
i:integer;
begin
//14M--- Отображение или скрытие графика динамики  по нажатию кнопки -----------
if (chr(key)='d') or (chr(key)='D') or (chr(key)='в') or (chr(key)='В') then
  begin
    DinamicChartDraw := not DinamicChartDraw;
  end;
//14M---------------------------------------------------------------------------
 if chr(key)='0' then begin
  //CreateCurNeispr(12); // Opustilis' tokopriemniki

 end;

if chr(key)='9' then begin
  //CreateCurNeispr(18); // Razbiraetsa sxema v dvizhenii
 // fignya:=not fignya;
end;

if chr(key)='8' then begin
//videoform.pnlAChSet.Visible:=not videoform.pnlAChSet.Visible;
// CreateCurNeispr(17); //Otkluchaetsa BV
end;

if chr(key)='7' then begin
//  CreateCurNeispr(16); //Otkl vspom mashiny

end;

if chr(key)='6' then begin
 // CreateCurNeispr(15);//Sgoraet vstavka VU45 na 17 posicii
end;

if chr(key)='5' then begin
 if CurTrafficLighter.GColor<>tlRed then
 begin
  OldColorLight:=CurTrafficLighter.GColor;
  CurTrafficLighter.GColor:=tlRed;
  SelectedTl:=CurTrafficLighter;
  RedSignalSuka:=true;//Вставить
  SKM_Suka:=SKM;//Вставить
  SPIK_Suka:=SPik;//Вставить
  switchTL(tlRed);
  if VideoFrm.CurTrafficLighter.ActiveLight=3 then
    begin
     if lastredmetter=-1 then lastredmetter:=round(kader2meter(overalframe));
    end else lastredmetter:=-1;
 end else
 begin
  CurTrafficLighter.GColor:=OldColorLight;
  SelectedTl:=CurTrafficLighter;
  switchTL(CurTrafficLighter.GColor);
 end;
end;

If chr(key)='1' then begin IsNight:= not IsNight;SetNight; end;
If chr(key)='2' then IsFog:= not IsFog;
If chr(key)='3' then IsSnow:= not IsSnow;
If chr(key)='4' then IsRain:= not IsRain;
end;

procedure TVideoForm.FormClose(Sender: TObject; var Action: TCloseAction);
var
f:textFile;//save lent
i:integer;
begin
CurTrafficLighter.activelight:=0;
ProceedLentaFile;
SetNull;
 ///     Form1.SEWDrive.SetCurDist(HLr,HRr,HMr);
 ///     Form1.SEWDrive.SetDistTime(0,5,0,5,0,5,1,1);

dxready:=false;
FinalKM:=SKM+' км. '+SPIK+' пк.'
;
Protokol.TimeAll:=Time-Protokol.TimeBegin;
CloseLentaFile;
InstrForm.CloseLentaFile1;//save lenta
//Alx_KLUb
UpdateKLUB.CloseKlub;
CloseSAUTDll();
//Alx_KLUB
Form1.Tabsheet1.TabVisible:=false;
Form1.tabsheet2.TabVisible:=false;
Form1.tabsheet3.TabVisible:=false;
Form1.tabsheet4.TabVisible:=false;

form1.show;
form1.repaint;
CloseSounds();

FORM1.tabsheet5.TabVisible:=true;
Form1.Memo1.Clear;
AssignFile(f,FileNameSaveLent+'.nar');     //save lent
Rewrite(f);                                //save lent
writeln(f,Uprava.count);//save lent
if Uprava.Count<>0 then
 for i:=0 to Uprava.Count-1 do
  begin
   Form1.Memo1.Lines.Add(Uprava.Strings[i]);
      writeln(f,Uprava.Strings[i]);//save lent
   writeln(f,CoorFaults.strings[i]);//save lent
  end else Form1.Memo1.Lines.Add('Поездка выполнена без нарушений');
form1.RadioGroup3Click(nil);
//IO_Data.SetPrivodControl(0);//Выключение реле, разрешающего включение шкафов приводов
//CloseMSUL;
end;

procedure CreateCurNeispr(Num:integer);
label M2;
 var
y,c,k,a,f:integer;
e: array[0..5] of integer;
begin
  if CurNeispr=nil then
  begin
   CurNeispr:=TGenNeispr.Create;
   NeispravnostFlag:=true;
   CurNeispr.Point:=StartStantion;
   CurNeispr.NumNeispr:=Num;
   if TNeisprEl(NList.items[CurNeispr.NumNeispr]).Prich.Count>1 then CurNeispr.NumRightAns:=Form3_1.GenRightAns(TNeisprEl(NList.items[CurNeispr.NumNeispr]).Prich.Count) else
    CurNeispr.NumRightAns:=0;
    for y:=0 to 5 do
        begin
M2:      c:=random(NList.count);
         if c=CurNeispr.NumNeispr then goto M2;
          for k:=0 to 5 do
           begin
            if c=e[k] then goto M2;
           end;
         e[y]:=c;
         if TNeisprEl(NList.items[c]).Prich.Count>1 then a:=random(TNeisprEl(NList.items[c]).Prich.Count) else
         a:=0;
         CurNeispr.SpisokAns.Add(TNeisprEl(NList.items[c]).Prich.Strings[a]);
        end;
       f:=random(CurNeispr.SpisokAns.count);
       CurNeispr.NumRightInSp:=f;
       CurNeispr.prisnakUstr:=0;
       CurNeispr.SpisokAns.Insert(f,TNeisprEl(NList.items[CurNeispr.NumNeispr]).Prich.Strings[CurNeispr.NumRightAns]);
       TimeNAchUstr:=Time;
  end;
end;



procedure Projector(State:Integer);
begin
//1S--------------- Прожектор ярко и тускло ------------------------------------
if State=2 then begin
ScreenMat.Emissive.r:=0.96;
ScreenMat.Emissive.g:=0.96;
ScreenMat.Emissive.b:=0.96;
isJectorPro:=2;

end;
if State=1 then  begin
ScreenMat.Emissive.r:=0.75;
ScreenMat.Emissive.g:=0.75;
ScreenMat.Emissive.b:=0.75;
isJectorPro:=1;
end;
if State=0 then begin
ScreenMat.Emissive.r:=0.1;
ScreenMat.Emissive.g:=0.1;
ScreenMat.Emissive.b:=0.1;
isJectorPro:=0;
end;
//1S----------------------------------------------------------------------------
end;

end.
