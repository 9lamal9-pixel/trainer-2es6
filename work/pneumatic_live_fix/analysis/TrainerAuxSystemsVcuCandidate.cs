using System;using System.Linq;using System.IO;using System.Runtime.InteropServices;
partial class TrainerIntegrationHost {
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)]struct SautConfig{public IntPtr Path;public byte TipDv;public uint Sound;public byte Bitmap,Green,Yellow,Red,White;public int MinDistance;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)]struct CalcConfig{public IntPtr Path;public byte TipDv;public uint Sound;public byte Green,Yellow,Red,White;public int MinDistance;}
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=20)]struct KpdConfig{public IntPtr Path;public int X,Y;public byte Type,Rotate,Visible;public int Green;public byte Cassette;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte SautInitFn(SautConfig c,IntPtr pb);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte CalcInitFn(CalcConfig c,IntPtr pb);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte AvpInitFn(IntPtr pb,KlubInit c);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte KpdInitFn(IntPtr pb,KpdConfig c);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte SoundsInitFn(IntPtr path,uint parent);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte AddRecordFn(IntPtr record);
 [StructLayout(LayoutKind.Sequential,Pack=1,Size=52)]struct RouteLimit{public int Start,End,Pass,Freight,Suburban,Special;public byte Flag,Klub,Saut,StartFlag;public int Distance,Allowed,Target,AllowedSaut,TargetSaut;public byte SautOS;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void AvpLimitFn(RouteLimit record);
 string commandStamp="";int? commandedKm,fileKm;bool safetyPower,momentaryCharge;
 void AddAux(IntPtr h,string step,string close,string label){otherSteps.Add(Export<StepFn>(h,step));otherCloses.Add(Export<StepFn>(h,close));Log(label+" native initialization successful; connected to shared train records");}
 void InitializeAux(){
  var pb=mem["TPribBezAllTren"];var path=Marshal.StringToHGlobalUni(root+"\\");
  var sound=Library("SoundsTren.dll");if(Export<SoundsInitFn>(sound,"Init_SoundsTrenDLL")(IntPtr.Zero,(uint)Handle.ToInt32())!=1)throw new Exception("SoundsTren initialization failed");otherCloses.Add(Export<StepFn>(sound,"CloseSounds"));
  var talk=Library("TalkSound.dll");if(Export<SoundsInitFn>(talk,"Init_SoundsTrenDLL")(IntPtr.Zero,(uint)Handle.ToInt32())!=1)throw new Exception("TalkSound initialization failed");otherCloses.Add(Export<StepFn>(talk,"CloseSounds"));Log("Dispatcher sound channel initialized");
  var calc=Library("CalcOgr.dll");if(Export<CalcInitFn>(calc,"Init_CalcOgr_Dll")(new CalcConfig{Path=path,TipDv=1,Sound=(uint)sound.ToInt32(),Green=(byte)route.speed_limits.ConvertAll(x=>x.speed_kmh).Max(),Yellow=60,Red=20,White=40},pb)!=1)throw new Exception("CalcOgr initialization failed");AddAux(calc,"Proceed_CalcOgr","CloseCalcOgrDll","CalcOgr");
  var saut=Library("Saut_Dll.dll");if(Export<SautInitFn>(saut,"Init_Saut_Dll")(new SautConfig{Path=path,TipDv=1,Sound=(uint)sound.ToInt32(),Green=(byte)route.speed_limits.ConvertAll(x=>x.speed_kmh).Max(),Yellow=60,Red=20,White=40},pb)!=1)throw new Exception("SAUT initialization failed");AddAux(saut,"Proceed_saut","CloseSAUTDll","SAUT");
  var avp=Library("AVP.dll");if(Export<AvpInitFn>(avp,"InitAVP")(pb,new KlubInit{Path=path,Type=2,TipDv=1})!=1)throw new Exception("AVP initialization failed");AddAux(avp,"ProceedAVP","CloseAVP","AVP freight");
  ConfigureAuxRoute(calc,saut,avp);
  // KPD is an alternative recorder. Its software model cycles but power remains off while KLUB-U is selected.
  var kpd=Library("KPD_3PV.dll");if(Export<KpdInitFn>(kpd,"InitKPD")(pb,new KpdConfig{Path=path,Type=2,Green=(byte)route.speed_limits.ConvertAll(x=>x.speed_kmh).Max(),Cassette=1})!=1)throw new Exception("KPD initialization failed");AddAux(kpd,"ProceedKPD","CloseKPD","KPD-3PV standby");
 }
 void AuxCycle(){foreach(var step in otherSteps)step();}
 void CloseAux(){for(int n=otherCloses.Count-1;n>=0;n--)otherCloses[n]();}
 void PollCommands(){
  var file=Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_commands.txt");if(!File.Exists(file)){if(commandStamp.Length>0){fileKm=null;safetyPower=false;commandStamp="";Log("Command file removed: test overrides released");}return;}var text=File.ReadAllText(file);if(text!=commandStamp){commandStamp=text;fileKm=null;safetyPower=false;foreach(var line in text.Split('\n')){var kv=line.Trim().Split('=');if(kv.Length!=2)continue;float v;int k;if(kv[0]=="speed"&&float.TryParse(kv[1],System.Globalization.NumberStyles.Float,System.Globalization.CultureInfo.InvariantCulture,out v))SetInitialSpeed(v);if(kv[0]=="km"&&int.TryParse(kv[1],out k)&&k>=1&&k<=7)fileKm=k;if(kv[0]=="safety")safetyPower=kv[1]=="1";}Log("Applied test inputs: "+text.Replace('\n',' '));}
 }
 void ApplyTestInputs(){
  ApplyBrakeInput();
  if(safetyPower){B(mem["TPBBtn"],4,1);B(mem["TPBBtn"],5,1);B(mem["TPBBtn"],6,1);B(mem["TPBBtn"],7,0);B(mem["TPBBtn"],8,0);B(mem["TPBBtn"],9,1);B(mem["TLocParam"],0x99,1);B(mem["TLocParam"],0x9a,1);}
  // Explicit stand control for the selected display. No EPK feedback or ALSN output is overridden.
  if(klubPowerMode!=2)B(mem["TPBBtn"],4,klubPowerMode==0?1:0);
 }
 void ApplyBrakeInput(){int? km=commandedKm.HasValue?commandedKm:fileKm;if(!km.HasValue)return;
  // A KM command must not bypass the independent VCU/blocking/combined-cock inputs.
  // Communication has already applied the native emergency-button priority.
  if(Marshal.ReadByte(mem["TPult"],0x23)==1||Marshal.ReadByte(mem["TPult"],0x24)==1)return;
  B(mem["TLocParam"],0x7d,km.Value);
 }
 void BeginCharge(){if(momentaryCharge)return;momentaryCharge=true;TestBrake(1);}
 void EndCharge(){if(!momentaryCharge)return;momentaryCharge=false;if(commandedKm==1)TestBrake(2);}
 void TestBrake(int km){commandedKm=km;Log("Test brake input KM="+km);}
 void ReleaseTestInputs(){momentaryCharge=false;commandedKm=null;fileKm=null;safetyPower=false;Log("Test inputs released: native cab controls active");}
 [DllImport("msvcrt.dll",CallingConvention=CallingConvention.Cdecl,EntryPoint="_clearfp")]static extern uint ClearFloatingStatus();
 int electricalFaults;string electricalFault="";
 void ElectricalCycle(){UpdateVcuInterlock();try{Dbl(mem["TTygaParam"],0,signedVelocity);commStep();UpdateVcuInterlock();electricalFault="";}catch(ArithmeticException ex){ClearFloatingStatus();electricalFault=ex.GetType().Name;electricalFaults++;F(mem["TResTyga"],0,0);if(electricalFaults==1||ticks%100==0){var t=mem["TTygaParam"];var flags=new System.Text.StringBuilder();for(int n=0;n<8;n++)flags.Append(Marshal.ReadInt32(t,0x10d+n*4)+",");Log("Native electrical fault, traction removed: "+ex+"; sections="+Marshal.ReadInt32(t,8)+" pos="+Marshal.ReadInt32(t,0xc)+" soed="+Marshal.ReadByte(t,0xdc)+" mode="+Marshal.ReadInt32(t,0x1b)+" U="+FloatAt(mem["TResTyga"],0x11)+" TD flags="+flags);}}}
}

