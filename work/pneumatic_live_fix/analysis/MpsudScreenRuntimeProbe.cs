using System;using System.Collections.Generic;using System.Diagnostics;using System.Drawing;using System.Drawing.Imaging;using System.IO;using System.Reflection;using System.Runtime.InteropServices;using System.Security.Cryptography;using System.Text;using System.Web.Script.Serialization;using System.Windows.Forms;
class MpsudScreenRuntimeProbe {
 static readonly BindingFlags Flags=BindingFlags.Instance|BindingFlags.NonPublic;static TrainerIntegrationHost host;static Dictionary<string,IntPtr> mem;static IntPtr msul;static IntPtr[] monitors=new IntPtr[2],forms=new IntPtr[2],hwnds=new IntPtr[2];static StepFn msulStep;static bool renderAllowed;static IntPtr[] originalProc=new IntPtr[2];static WndProcFn[] windowProc=new WndProcFn[2];static List<object> captures=new List<object>();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void StepFn();
 static List<object> checks=new List<object>();
 static void Check(string name,bool ok,object expected,object actual){checks.Add(new{name=name,passed=ok,expected=expected,actual=actual});if(!ok)throw new Exception("Assertion failed: "+name+" expected="+expected+" actual="+actual);}
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)]static extern IntPtr GetModuleHandle(string name);
 [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetProcAddress(IntPtr module,string name);
 [DllImport("user32.dll")]static extern bool IsWindow(IntPtr hwnd);
 [DllImport("user32.dll",CharSet=CharSet.Unicode)]static extern int GetClassName(IntPtr hwnd,StringBuilder text,int max);
 [DllImport("user32.dll")]static extern IntPtr SendMessage(IntPtr hwnd,uint message,IntPtr wp,IntPtr lp);
 [DllImport("user32.dll")]static extern bool PeekMessage(out MSG message,IntPtr hwnd,uint first,uint last,uint flags);
 [StructLayout(LayoutKind.Sequential)]struct MSG{public IntPtr hwnd;public uint message;public IntPtr wp,lp;public uint time;public int x,y;public uint lPrivate;}
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate IntPtr WndProcFn(IntPtr hwnd,uint message,IntPtr wp,IntPtr lp);
 [DllImport("user32.dll",EntryPoint="SetWindowLongW",SetLastError=true)]static extern IntPtr SetWindowLong(IntPtr hwnd,int index,IntPtr value);
 [DllImport("user32.dll",EntryPoint="GetWindowLongW")]static extern IntPtr GetWindowLong(IntPtr hwnd,int index);
 [DllImport("user32.dll",CharSet=CharSet.Unicode)]static extern IntPtr CallWindowProc(IntPtr proc,IntPtr hwnd,uint message,IntPtr wp,IntPtr lp);
 [DllImport("user32.dll",CharSet=CharSet.Unicode)]static extern IntPtr DispatchMessage(ref MSG message);
 [DllImport("user32.dll",SetLastError=true)]static extern IntPtr CopyImage(IntPtr handle,uint kind,int width,int height,uint flags);
 [DllImport("gdi32.dll",SetLastError=true)]static extern int GetObject(IntPtr handle,int bytes,out BITMAP bitmap);
 [DllImport("gdi32.dll")]static extern uint GetObjectType(IntPtr handle);
 [DllImport("gdi32.dll")]static extern IntPtr CreateCompatibleDC(IntPtr dc);
 [DllImport("gdi32.dll")]static extern bool DeleteDC(IntPtr dc);
 [DllImport("gdi32.dll")]static extern bool DeleteObject(IntPtr item);
 [DllImport("gdi32.dll",SetLastError=true)]static extern int GetDIBits(IntPtr dc,IntPtr bitmap,uint start,uint lines,byte[] bits,ref BITMAPINFOHEADER info,uint usage);
 [StructLayout(LayoutKind.Sequential)]struct BITMAP{public int type,width,height,widthBytes;public ushort planes,bitsPixel;public IntPtr bits;}
 [StructLayout(LayoutKind.Sequential)]struct BITMAPINFOHEADER{public uint size;public int width,height;public ushort planes,bits;public uint compression,imageSize;public int xp,yp;public uint colors,important;}
 static object Field(string name){return typeof(TrainerIntegrationHost).GetField(name,Flags).GetValue(host);}
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,Flags,null,Type.EmptyTypes,null).Invoke(host,null);}
 static string Hash(string path){using(var sha=SHA256.Create())using(var input=File.OpenRead(path))return BitConverter.ToString(sha.ComputeHash(input)).Replace("-","");}
 static string ClassName(IntPtr obj){if(obj==IntPtr.Zero)return "null";var vmt=Marshal.ReadIntPtr(obj);var name=Marshal.ReadIntPtr(vmt,-56);int n=Marshal.ReadByte(name);byte[] bytes=new byte[n];Marshal.Copy(IntPtr.Add(name,1),bytes,0,n);return Encoding.ASCII.GetString(bytes);}
 static byte Current(int s){return Marshal.ReadByte(monitors[s],0x14);}
 static IntPtr Frame(int s){var wizard=Marshal.ReadIntPtr(monitors[s],0x20);return wizard==IntPtr.Zero?IntPtr.Zero:Marshal.ReadIntPtr(wizard,0xc);}
 static Dictionary<string,object> fieldMap;
 static object ReadWidgets(IntPtr frame){var result=new Dictionary<string,object>();string cls=ClassName(frame);if(fieldMap==null){string path=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"..","dev","docs","audit_parts","MPSUD_SCREEN_FIELDS.json"));fieldMap=new JavaScriptSerializer().Deserialize<Dictionary<string,object>>(File.ReadAllText(path));}if(!fieldMap.ContainsKey(cls))return result;var fields=(Dictionary<string,object>)fieldMap[cls];foreach(var entry in (System.Collections.IEnumerable)fields["published_fields"]){var field=(Dictionary<string,object>)entry;string kind=(string)field["class"];int offset=Convert.ToInt32(((string)field["offset"]).Substring(2),16);string name=(string)field["name"];if(kind=="TDataCaption")result[name]=new{text=MpsudRuntimeValueHelpers.DataCaptionText(frame,offset),state=MpsudRuntimeValueHelpers.DataCaptionState(frame,offset)};else if(kind=="TIndicatorPribor")result[name]=new{value=MpsudRuntimeValueHelpers.IndicatorDouble(frame,offset)};else if(kind=="TLabel")result[name]=new{text=MpsudRuntimeValueHelpers.LabelCaption(frame,offset)};}if(cls=="TwtVspomMashineFrame"){result["underlying_window_type"]=Marshal.ReadByte(frame,0x29f1);result["contact_view"]=Marshal.ReadByte(frame,0x29f2);}if(cls=="TwtContactorsFrame")result["contact_view"]=Marshal.ReadInt32(frame,0xf04);if(cls=="TwtMashinistFrame")result["messages_expanded"]=Marshal.ReadByte(mem["TShema"],0x322+(frame==Frame(0)?0:1))==1;if(cls=="TwtCIFrame"){for(int b=1;b<=2;b++)for(int s=1;s<=2;s++)for(int r=0;r<=11;r++)result["CI.b"+b+".s"+s+".r"+r]=MpsudRuntimeValueHelpers.CiCell(frame,b,s,r).Trim();}return result;}
 static void DiscardRenderMessages(int s){MSG message;while(PeekMessage(out message,hwnds[s],0x664,0x665,1)){} }
 static void ClearByOwner(){
  Application.DoEvents();Call("ElectricalCycle");Application.DoEvents();Call("ElectricalCycle");
  for(int s=0;s<2;s++){DiscardRenderMessages(s);if(Marshal.ReadIntPtr(mem["TPult"],0x105+s*4)!=IntPtr.Zero)throw new Exception("Producer slot still live; will not overwrite or free owner bitmap,side="+s);}
 }
 static object CopyBitmap(IntPtr source,string name){
  if(GetObjectType(source)!=7)throw new Exception("Producer did not return a GDI bitmap");
  IntPtr bitmap=CopyImage(source,0,0,0,0x2000);if(bitmap==IntPtr.Zero||bitmap==source)throw new Exception("Cannot create independently owned DIB copy,error="+Marshal.GetLastWin32Error());
  IntPtr dc=IntPtr.Zero;try{BITMAP bm;if(GetObject(bitmap,Marshal.SizeOf(typeof(BITMAP)),out bm)==0)throw new Exception("GetObject failed");if(bm.width!=800||Math.Abs(bm.height)!=600)throw new Exception("Unexpected MSUL dimensions="+bm.width+"x"+bm.height);
   var header=new BITMAPINFOHEADER{size=40,width=bm.width,height=Math.Abs(bm.height),planes=1,bits=32,imageSize=(uint)(bm.width*Math.Abs(bm.height)*4)};
   byte[] pixels=new byte[header.imageSize];dc=CreateCompatibleDC(IntPtr.Zero);if(dc==IntPtr.Zero)throw new Exception("Memory DC unavailable");int lines=GetDIBits(dc,bitmap,0,(uint)header.height,pixels,ref header,0);if(lines!=header.height)throw new Exception("GetDIBits failed,lines="+lines+",error="+Marshal.GetLastWin32Error());
   using(var stream=new MemoryStream()){using(var writer=new BinaryWriter(stream)){writer.Write((ushort)0x4d42);writer.Write((uint)(54+pixels.Length));writer.Write((uint)0);writer.Write((uint)54);writer.Write((uint)40);writer.Write(header.width);writer.Write(header.height);writer.Write((ushort)1);writer.Write((ushort)32);writer.Write((uint)0);writer.Write((uint)pixels.Length);writer.Write(0);writer.Write(0);writer.Write((uint)0);writer.Write((uint)0);writer.Write(pixels);writer.Flush();stream.Position=0;using(var image=Image.FromStream(stream)){string path=TrainerSession.ResultPath(name);image.Save(path,ImageFormat.Png);return new{file=name,width=image.Width,height=image.Height,sha256=Hash(path)};}}}
  }finally{if(dc!=IntPtr.Zero)DeleteDC(dc);DeleteObject(bitmap);}
 }
 static void Snapshot(string label){
  renderAllowed=true;try{for(int s=0;s<2;s++){MSG message;int processed=0;while(PeekMessage(out message,hwnds[s],0x664,0x665,1)){if(++processed>30)throw new Exception("MSUL message queue did not settle");DispatchMessage(ref message);}IntPtr bitmap=Marshal.ReadIntPtr(mem["TPult"],0x105+s*4);if(bitmap==IntPtr.Zero){SendMessage(hwnds[s],0x665,IntPtr.Zero,IntPtr.Zero);bitmap=Marshal.ReadIntPtr(mem["TPult"],0x105+s*4);}if(bitmap==IntPtr.Zero)throw new Exception("No synchronous producer frame,side="+s+",enum="+Current(s));
   captures.Add(new{label=label,side=s==0?"left":"right",window_type=Current(s),frame_class=ClassName(Frame(s)),widgets=ReadWidgets(Frame(s)),bitmap=CopyBitmap(bitmap,"MSUL_runtime_"+label+"_"+s+".png")});}
  }finally{renderAllowed=false;}
 } static void Press(int side,byte code,string label){ClearByOwner();Marshal.WriteByte(mem["TPult"],0x53,0);Marshal.WriteByte(mem["TPult"],0x54,0);msulStep();for(int s=0;s<2;s++)DiscardRenderMessages(s);Marshal.WriteByte(mem["TPult"],0x53,(byte)(side==0?code:0));Marshal.WriteByte(mem["TPult"],0x54,(byte)(side==1?code:0));msulStep();if(Marshal.ReadByte(mem["TPult"],0x53)!=0||Marshal.ReadByte(mem["TPult"],0x54)!=0)throw new Exception("Key not consumed");Snapshot(label);Console.WriteLine("FRAME "+label+" "+Current(side));}
 static void RunSelected(string[] args){int index=args.Length==0?0:Int32.Parse(args[0]);int[] types={1,2,3,4,5,6,7,8,9,10,11,13,14,15,16,17,18,4,4,4,2,16,16,16,1,2,9};string[] cls={"TwtClockFrame","TwtMashinistFrame","TwtVspomMashineFrame","TwtVspomMashineFrame","TwtVspomMashineFrame","TwtVspomMashineFrame","TwtContactorsFrame","TwtCIFrame","TwtDiagnostFrame","TwtAutovedFrame","TwtOgranFrame","TwtSautFrame","TwtOborudovanieFrame","TwtSMErame","TwtPCConnectFrame","TwtRN3000Frame","TwtSTPRFrame","TwtContactorsFrame","TwtVspomMashineFrame","TwtVspomMashineFrame","TwtMashinistFrame","TwtPCConnectFrame","TwtPCConnectFrame","TwtPCConnectFrame","TwtClockFrame","TwtMashinistFrame","TwtDiagnostFrame"};byte[][] keys={new byte[0],new byte[]{14},new byte[]{13},new byte[]{13,14},new byte[]{13,12},new byte[]{13,11},new byte[]{13,10},new byte[]{13,9},new byte[]{12},new byte[]{14,13},new byte[]{14,12},new byte[]{11},new byte[]{10},new byte[]{13,8},new byte[]{13,7},new byte[]{13,7,13},new byte[]{13,7,12},new byte[]{13,10,14},new byte[]{13,12,14},new byte[]{13,11,14},new byte[]{14,14},new byte[]{13,7,11},new byte[]{13,7,10},new byte[]{13,7,9},new byte[]{7},new byte[]{14,11},new byte[]{12,14}};if(index<0||index>=types.Length)throw new Exception("Route index out of range");for(int s=0;s<2;s++){for(int k=0;k<keys[index].Length;k++)Press(s,keys[index][k],"route"+index+"_key"+k+"_side"+s);if(keys[index].Length==0){ClearByOwner();Snapshot("route"+index+"_side"+s);}Check("route"+index+" type side"+s,Current(s)==types[index],types[index],Current(s));Check("route"+index+" class side"+s,ClassName(Frame(s))==cls[index],cls[index],ClassName(Frame(s)));if(cls[index]=="TwtVspomMashineFrame"){int mode=index==18?5:index==19?6:types[index]==4?3:types[index];int view=index==3||index==18||index==19?1:0;Check("Vspom underlying mode route"+index+" side"+s,Marshal.ReadByte(Frame(s),0x29f1)==mode,mode,Marshal.ReadByte(Frame(s),0x29f1));Check("Vspom contact view route"+index+" side"+s,Marshal.ReadByte(Frame(s),0x29f2)==view,view,Marshal.ReadByte(Frame(s),0x29f2));}if(cls[index]=="TwtContactorsFrame"){int view=index==17?1:0;Check("Contactors contact view route"+index+" side"+s,Marshal.ReadInt32(Frame(s),0xf04)==view,view,Marshal.ReadInt32(Frame(s),0xf04));}if(index==20)Check("Driver expanded messages side"+s,Marshal.ReadByte(mem["TShema"],0x322+s)==1,1,Marshal.ReadByte(mem["TShema"],0x322+s));Check("route"+index+" independent side"+s,Current(1-s)==(s==0?1:types[index]),s==0?1:types[index],Current(1-s));}if(args.Length>1)ValueFixtures(args[1]);else if(index>0&&types[index]!=10&&types[index]!=11){int previous=Current(1);Press(0,15,"exit_key1_route"+index);int expected=types[index]>=16&&types[index]<=18?3:1;Check("Exit key1 route"+index,Current(0)==expected,expected,Current(0));Check("Exit independent route"+index,Current(1)==previous,previous,Current(1));}}
 sealed class Journal:IDisposable{List<Tuple<IntPtr,byte[]>> entries=new List<Tuple<IntPtr,byte[]>>();public void Write(IntPtr p,int offset,byte[] value){var address=IntPtr.Add(p,offset);var old=new byte[value.Length];Marshal.Copy(address,old,0,old.Length);entries.Add(Tuple.Create(address,old));Marshal.Copy(value,0,address,value.Length);}public void I(IntPtr p,int offset,int value){Write(p,offset,BitConverter.GetBytes(value));}public void F(IntPtr p,int offset,float value){Write(p,offset,BitConverter.GetBytes(value));}public void D(IntPtr p,int offset,double value){Write(p,offset,BitConverter.GetBytes(value));}public void B(IntPtr p,int offset,byte value){Write(p,offset,new byte[]{value});}public void Dispose(){for(int i=entries.Count-1;i>=0;i--)Marshal.Copy(entries[i].Item2,0,entries[i].Item1,entries[i].Item2.Length);}}
 static void Numeric(string field,double expected,string actual){double number;bool parsed=Double.TryParse(actual.Trim().Replace(",","."),System.Globalization.NumberStyles.Float,System.Globalization.CultureInfo.InvariantCulture,out number);Check(field,parsed&&Math.Abs(number-expected)<0.001,expected,actual);}
 static void ValueFixtures(string selected){var tyga=mem["TTygaParam"];var shema=mem["TShema"];if(selected=="driver"){ClearByOwner();using(var j=new Journal()){var res=Marshal.ReadIntPtr(tyga,0x27);Check("ResTyga fixture pointer",res!=IntPtr.Zero,"nonzero",res.ToString("X"));j.F(res,0x11,3000);int[] a={0x5b,0x5f,0x63,0x67,0x6b,0x6f,0x73,0x77};int[] v={0x9b,0x9f,0xa3,0xa7,0xab,0xaf,0xb3,0xb7};float[] av={-111,222,311,322,433,444,555,566};float[] vv={-51,62,71,82,93,104,115,126};for(int i=0;i<a.Length;i++){j.F(tyga,a[i],av[i]);j.F(tyga,v[i],vv[i]);}foreach(int x in new int[]{0x43,0x4b,0x53})j.F(tyga,x,1);Snapshot("driver_known_values");var frame=Frame(0);var values=MpsudRuntimeValueHelpers.MashSnapshot(frame);foreach(var pair in values)checks.Add(new{name="observed.driver."+pair.Key,passed=true,expected="observation",actual=pair.Value});Numeric("Driver Uks actual",3000,(string)values["Uks.text"]);Numeric("Driver Ia1 absolute",111,(string)values["Ia1_12.text"]);Numeric("Driver Ia1 group34",222,(string)values["Ia1_34.text"]);Numeric("Driver Iv1 absolute",51,(string)values["Iv1_12.text"]);Numeric("Driver Ia2 source selection",311,(string)values["Ia2.text"]);Numeric("Driver Ia3 source selection",433,(string)values["Ia3.text"]);Numeric("Driver Ia4 source selection",555,(string)values["Ia4.text"]);}
 } if(selected=="ci"){ClearByOwner();using(var j=new Journal()){j.I(tyga,0xc,23);j.D(tyga,0x33,1800);j.F(tyga,0x5b,400);j.F(tyga,0x5f,600);j.F(tyga,0x7b,100);j.F(tyga,0x7f,200);j.I(shema,0x242,0);j.I(shema,0x246,1);Snapshot("ci_known_values");var frame=Frame(0);for(int b=1;b<=2;b++)for(int s=1;s<=2;s++)for(int r=0;r<=11;r++)checks.Add(new{name="observed.CI.b"+b+".s"+s+".r"+r,passed=true,expected="observation",actual=MpsudRuntimeValueHelpers.CiCell(frame,b,s,r).Trim()});Numeric("CI Uedt synthetic Pos23 despite Udvig1800",991,MpsudRuntimeValueHelpers.CiCell(frame,1,1,5));Numeric("CI own current stub",0,MpsudRuntimeValueHelpers.CiCell(frame,1,1,6));Numeric("CI fan1 synthetic Ia/4",100,MpsudRuntimeValueHelpers.CiCell(frame,1,1,7));Numeric("CI fan2 synthetic Ia/4",150,MpsudRuntimeValueHelpers.CiCell(frame,1,1,8));Numeric("CI generator off stub",0,MpsudRuntimeValueHelpers.CiCell(frame,2,1,1));Numeric("CI generator on stub",5,MpsudRuntimeValueHelpers.CiCell(frame,2,2,1));Numeric("CI battery off stub",110,MpsudRuntimeValueHelpers.CiCell(frame,2,1,3));Numeric("CI battery on stub",140,MpsudRuntimeValueHelpers.CiCell(frame,2,2,3));Numeric("CI isolation stub",33.3,MpsudRuntimeValueHelpers.CiCell(frame,2,1,5));for(int s=1;s<=2;s++){Numeric("CI consumed energy stub sec"+s,0,MpsudRuntimeValueHelpers.CiEnergy(frame,s,false));Numeric("CI recovered energy stub sec"+s,0,MpsudRuntimeValueHelpers.CiEnergy(frame,s,true));}}
 } if(selected=="av"){using(var j=new Journal()){j.B(shema,0x328,0);j.I(shema,0x339,2319);j.I(shema,0x33d,6000);j.I(shema,0x341,75);for(int p=2;p<=4;p++){ClearByOwner();j.I(shema,0x324,p);j.B(shema,0x328,0);j.I(shema,0x339,2319);j.I(shema,0x33d,6000);j.I(shema,0x341,75);Snapshot("av_selected"+p);var frame=Frame(0);Numeric("AV selected field"+p,p==2?2319:p==3?6000:75,MpsudRuntimeValueHelpers.LabelCaption(frame,p==2?0x2dc:p==3?0x2e8:0x2f4));}ClearByOwner();j.I(shema,0x324,4);j.B(shema,0x328,1);j.Write(shema,0x329,new byte[]{2,55,55,0});Snapshot("av_edit_wagons77");var f=Frame(0);Numeric("AV CONFIRMED bug entered wagon77 appears in mass",77,MpsudRuntimeValueHelpers.LabelCaption(f,0x2e8));Numeric("AV CONFIRMED bug wagon label remains75",75,MpsudRuntimeValueHelpers.LabelCaption(f,0x2f4));}
 } if(selected=="sme"){for(int state=0;state<2;state++){ClearByOwner();using(var j=new Journal()){j.I(tyga,0x10d,state);Snapshot("sme_VklTD"+state);var f=Frame(0);for(int c=1;c<=2;c++){Check("SME CONFIRMED MV follows VklTD state"+state+" c"+c,MpsudRuntimeValueHelpers.SmeState(f,12,c)==state,state,MpsudRuntimeValueHelpers.SmeState(f,12,c));Check("SME CONFIRMED louvers inverse VklTD state"+state+" c"+c,MpsudRuntimeValueHelpers.SmeState(f,21,c)==1-state,1-state,MpsudRuntimeValueHelpers.SmeState(f,21,c));}}}
 } if(selected=="diag"){Press(0,14,"diag_buk_key2");Check("Diagnostic digit2 does not open BUK",Current(0)==9,9,Current(0));} if(selected=="diag_arrows"){Save(false,"Partial diagnostic before unaccepted arrow recreation; not final PASS");Press(0,6,"diag_up");Check("Diagnostic Up remains same enum",Current(0)==9,9,Current(0));Press(0,5,"diag_down");Check("Diagnostic Down remains same enum",Current(0)==9,9,Current(0));}}
 static void Save(bool passed,string error){File.WriteAllText(TrainerSession.ResultPath("MPSUD_screens_runtime.json"),new JavaScriptSerializer().Serialize(new{passed=passed,error=error,module_sha256=msul==IntPtr.Zero?null:Hash(Path.Combine((string)Field("root"),"RESOURCES","LOCOMOTIVES","1","MSUL_MON.dll")),captures=captures,checks=checks,electrical_faults=host==null?-1:(int)Field("electricalFaults"),normative_mpsuid_accepted=false,scope="Fresh-process selected native key route on both monitors, actual producer frames and direct widget readings. Controlled fixtures use actual shared native pointers, restore written bytes, and perform no physics between fixture input and rendering. Reversible own-window subclass pauses asynchronous WMUPDATE during owner consumption. Assertions reproduce observed behavior/defects; no full MPSUID/RE or refresh-rate/physics acceptance."}));}
 [STAThread]static int Main(string[] args){Application.EnableVisualStyles();Console.WriteLine("PROBE entered");try{
  host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);host.Show();Console.WriteLine("HOST shown");Application.DoEvents();Console.WriteLine("HOST event queue drained");var timer=(Timer)Field("timer");if(timer==null)throw new Exception("Host not initialized");timer.Stop();mem=(Dictionary<string,IntPtr>)Field("mem");msul=GetModuleHandle("MSUL_MON.dll");if(msul==IntPtr.Zero)throw new Exception("MSUL not loaded");if(Hash(Path.Combine((string)Field("root"),"RESOURCES","LOCOMOTIVES","1","MSUL_MON.dll"))!="95E3FB14CBC7FF2B5E9C61DC2215CFA7C2F23161FE21D075ADF04F45907B653B")throw new Exception("MSUL SHA guard failed");
  var ptr=GetProcAddress(msul,"ProceedMsul");if(ptr==IntPtr.Zero)throw new Exception("ProceedMsul export absent");msulStep=(StepFn)Marshal.GetDelegateForFunctionPointer(ptr,typeof(StepFn));
  var slot=Marshal.ReadIntPtr(msul,0x20bb9c);var owner=Marshal.ReadIntPtr(slot);if(owner==IntPtr.Zero||ClassName(owner)!="TMSULClass")throw new Exception("MSUL owner class guard");
  for(int s=0;s<2;s++){monitors[s]=Marshal.ReadIntPtr(owner,0x20+s*4);if(ClassName(monitors[s])!="TWindowMonitor")throw new Exception("Monitor class guard");forms[s]=Marshal.ReadIntPtr(monitors[s],0x1c);hwnds[s]=Marshal.ReadIntPtr(forms[s],0x260);var name=new StringBuilder(100);GetClassName(hwnds[s],name,100);if(!IsWindow(hwnds[s])||name.ToString()!="TMainMSULForm")throw new Exception("MSUL window guard: "+name);}
  for(int s=0;s<2;s++){int side=s;originalProc[s]=GetWindowLong(hwnds[s],-4);if(originalProc[s]==IntPtr.Zero)throw new Exception("Native window proc absent");windowProc[s]=(hwnd,message,wp,lp)=>{if(message==0x665&&!renderAllowed)return IntPtr.Zero;return CallWindowProc(originalProc[side],hwnd,message,wp,lp);};IntPtr previous=SetWindowLong(hwnds[s],-4,Marshal.GetFunctionPointerForDelegate(windowProc[s]));if(previous!=originalProc[s])throw new Exception("Window proc guard changed");}
  var shared=Marshal.ReadIntPtr(monitors[0],0xc);Check("Native shared pult pointer",Marshal.ReadIntPtr(shared,1)==mem["TPult"],mem["TPult"].ToString("X"),Marshal.ReadIntPtr(shared,1).ToString("X"));Check("Native shared Tyga pointer",Marshal.ReadIntPtr(shared,5)==mem["TTygaParam"],mem["TTygaParam"].ToString("X"),Marshal.ReadIntPtr(shared,5).ToString("X"));mem["TShema"]=Marshal.ReadIntPtr(shared,9);Check("Native shared Shema pointer",mem["TShema"]!=IntPtr.Zero,"nonzero",mem["TShema"].ToString("X"));Console.WriteLine("MSUL owner and window guards ready");RunSelected(args);Check("Electrical faults",(int)Field("electricalFaults")==0,0,Field("electricalFaults"));Save(true,null);Console.WriteLine("PASS native screen coverage; frames="+captures.Count+",assertions="+checks.Count);return 0;
 }catch(Exception error){try{Save(false,error.ToString());}catch(Exception save){Console.WriteLine("Result save failed:"+save);}Console.WriteLine(error);return 1;}finally{for(int s=0;s<2;s++)if(originalProc[s]!=IntPtr.Zero&&IsWindow(hwnds[s]))SetWindowLong(hwnds[s],-4,originalProc[s]);if(host!=null){host.Close();host.Dispose();}}}
}

// Read-only helpers for the isolated x86 fixture host. Not production code.
// Apply only after validating MSUL_MON SHA256 and frame VMT/instance size.
// The caller owns pointer/range validation, the UI-thread update, and restoration
// of fixture input bytes. These helpers never write memory or call physics.

internal static class MpsudRuntimeValueHelpers
{
    internal static IntPtr Add(IntPtr p, int offset) { return IntPtr.Add(p, offset); }
    internal static IntPtr Ptr(IntPtr p, int offset)
    {
        if (IntPtr.Size != 4) throw new InvalidOperationException("x86 ABI required");
        if (p == IntPtr.Zero) throw new InvalidOperationException("Null object pointer");
        return new IntPtr(Marshal.ReadInt32(p, offset));
    }
    internal static IntPtr Component(IntPtr frame, int fieldOffset)
    {
        IntPtr p = Ptr(frame, fieldOffset);
        if (p == IntPtr.Zero) throw new InvalidOperationException("Null component pointer");
        return p;
    }
    internal static double DoubleAt(IntPtr p, int offset)
    {
        byte[] bytes = new byte[8];
        Marshal.Copy(Add(p, offset), bytes, 0, bytes.Length);
        return BitConverter.ToDouble(bytes, 0);
    }
    internal static string UnicodeStringAt(IntPtr owner, int fieldOffset)
    {
        IntPtr p = Ptr(owner, fieldOffset);
        if (p == IntPtr.Zero) return String.Empty;
        int length = Marshal.ReadInt32(p, -4);
        if (length < 0 || length > 4096) throw new InvalidOperationException("Invalid UnicodeString length");
        return Marshal.PtrToStringUni(p, length);
    }
    internal static string PWideCharAt(IntPtr owner, int fieldOffset)
    {
        IntPtr p = Ptr(owner, fieldOffset);
        if (p == IntPtr.Zero) return String.Empty;
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 4096; ++i)
        {
            char c = (char)(ushort)Marshal.ReadInt16(p, i * 2);
            if (c == '\0') return text.ToString();
            text.Append(c);
        }
        throw new InvalidOperationException("PWideChar lacks bounded NUL terminator");
    }
    internal static string DataCaptionText(IntPtr frame, int fieldOffset)
    { return UnicodeStringAt(Component(frame, fieldOffset), 0x1EC); }
    internal static int DataCaptionInteger(IntPtr frame, int fieldOffset)
    { return Marshal.ReadInt32(Component(frame, fieldOffset), 0x1F0); }
    internal static double DataCaptionDouble(IntPtr frame, int fieldOffset)
    { return DoubleAt(Component(frame, fieldOffset), 0x1F8); }
    internal static byte DataCaptionState(IntPtr frame, int fieldOffset)
    { return Marshal.ReadByte(Component(frame, fieldOffset), 0x239); }
    internal static double IndicatorDouble(IntPtr frame, int fieldOffset)
    { return DoubleAt(Component(frame, fieldOffset), 0x1F8); }
    internal static string LabelCaption(IntPtr frame, int fieldOffset)
    { return PWideCharAt(Component(frame, fieldOffset), 0x17C); }
    internal static string CiCell(IntPtr frame, int block, int section, int row)
    {
        if (section < 1 || section > 2 || row < 0 || row > 11 || block < 1 || block > 2)
            throw new ArgumentOutOfRangeException();
        // 48 DECIMAL bytes = 0x30, not 0x48.
        int baseOffset = block == 1 ? 0x2B4 : 0x344;
        return UnicodeStringAt(frame, baseOffset + 0x30 * section + 4 * row);
    }
    internal static string CiEnergy(IntPtr frame, int section, bool recuperated)
    {
        if (section < 1 || section > 2) throw new ArgumentOutOfRangeException();
        return UnicodeStringAt(frame, 0x404 + 8 * (section - 1) + (recuperated ? 4 : 0));
    }
    internal static byte SmeState(IntPtr frame, int row, int column)
    {
        if (row < 1 || row > 27 || column < 1 || column > 8)
            throw new ArgumentOutOfRangeException();
        return Marshal.ReadByte(frame, 0x2BB + 8 * row + column);
    }
    internal static Dictionary<string, object> MashSnapshot(IntPtr frame)
    {
        var result = new Dictionary<string, object>();
        string[] names = { "Uks", "Ia1_12", "Ia1_34", "Iv1_12", "Iv1_34", "Ia2", "Iv2", "Ia3", "Iv3", "Ia4", "Iv4" };
        int[] indicator = { 0x2A8, 0x2AC, 0x2B0, 0x2B4, 0x2B8, 0x2BC, 0x2C0, 0x2C4, 0x2C8, 0x368, 0x36C };
        int[] caption = { 0x3CC, 0x3A4, 0x3A8, 0x3AC, 0x3B0, 0x3B4, 0x3B8, 0x3BC, 0x3C0, 0x3C4, 0x3C8 };
        for (int i = 0; i < names.Length; ++i)
        {
            result[names[i] + ".value"] = IndicatorDouble(frame, indicator[i]);
            result[names[i] + ".text"] = DataCaptionText(frame, caption[i]);
        }
        result["mode.text"] = DataCaptionText(frame, 0x414);
        result["position.text"] = DataCaptionText(frame, 0x418);
        result["actualPercent.text"] = DataCaptionText(frame, 0x41C);
        result["fanPercent.text"] = DataCaptionText(frame, 0x420);
        result["setPercent.text"] = DataCaptionText(frame, 0x434);
        return result;
    }
    internal static Dictionary<string, object> SautSnapshot(IntPtr frame)
    {
        IntPtr arrow = Component(frame, 0x298);
        IntPtr indicator = Ptr(arrow, 0x30C);
        IntPtr speedLabel = Ptr(arrow, 0x310);
        return new Dictionary<string, object> {
            { "speed.arrow", DoubleAt(arrow, 0x298) },
            { "speed.indicator", DoubleAt(indicator, 0x20) },
            { "speed.text", UnicodeStringAt(speedLabel, 8) },
            { "limit.indicator", DoubleAt(indicator, 0x28) },
            { "limit.text", LabelCaption(frame, 0x2BC) },
            { "distance.indicator", IndicatorDouble(frame, 0x2C0) },
            { "distance.text", DataCaptionText(frame, 0x2C4) },
            { "theta.value", DataCaptionDouble(frame, 0x2D0) },
            { "theta.text", DataCaptionText(frame, 0x2D0) },
            { "saut.state", DataCaptionState(frame, 0x2C8) },
            { "epk.state", DataCaptionState(frame, 0x2CC) }
        };
    }
    internal static Dictionary<string, object> AutovedSnapshot(IntPtr frame)
    {
        return new Dictionary<string, object> {
            { "intensity.text", LabelCaption(frame, 0x2D0) },
            { "trainNumber.text", LabelCaption(frame, 0x2DC) },
            { "mass.text", LabelCaption(frame, 0x2E8) },
            { "wagonCount.text", LabelCaption(frame, 0x2F4) }
        };
    }
    // Each returned row must be persisted by the fixture host alongside input bytes.
    internal static Dictionary<string, object> Expect(string field, object expected, object observed)
    {
        bool pass = expected is double && observed is double
            ? Math.Abs((double)expected - (double)observed) <= 0.000001
            : Object.Equals(expected, observed);
        return new Dictionary<string, object> {
            { "field", field }, { "expected", expected }, { "observed", observed }, { "pass", pass }
        };
    }
}
