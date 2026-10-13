using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

public sealed class TrainerAutovedenieSettings {
 public int Mode,Intensity,TrainNumber,MassT,Wagons,SelectedField,CommitVersion;
 public bool Empty,Editing,EditorOpen,Valid;public string Reason;
}

// Native MSUL interface only: it does not command traction/brakes or resize the train.
public sealed class TrainerAutovedenieInterface:IDisposable {
 const string MsulSha="95E3FB14CBC7FF2B5E9C61DC2215CFA7C2F23161FE21D075ADF04F45907B653B";
 const string PultSha="7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5";
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode)]static extern IntPtr GetModuleHandle(string name);
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode,SetLastError=true)]static extern uint GetModuleFileName(IntPtr module,StringBuilder path,int capacity);
 [DllImport("kernel32.dll",SetLastError=true)]static extern IntPtr VirtualAlloc(IntPtr address,UIntPtr size,uint kind,uint protect);
 [DllImport("kernel32.dll",SetLastError=true)]static extern bool VirtualFree(IntPtr address,UIntPtr size,uint kind);
 [DllImport("kernel32.dll",SetLastError=true)]static extern bool VirtualProtect(IntPtr address,UIntPtr size,uint protect,out uint oldProtect);
 [DllImport("kernel32.dll",SetLastError=true)]static extern bool FlushInstructionCache(IntPtr process,IntPtr address,UIntPtr size);
 [DllImport("kernel32.dll")]static extern IntPtr GetCurrentProcess();
 [DllImport("kernel32.dll")]static extern uint GetCurrentThreadId();
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int KeyCallback(IntPtr monitor,int key);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void RenderCallback(IntPtr frame);
 [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate void TextBridge(IntPtr control,IntPtr text,int length);
 sealed class Hook {public IntPtr Site;public byte[] Original,Patch,Current;public uint OldProtection;public bool ProtectionPending;}
 static readonly List<TrainerAutovedenieInterface> ActiveOwners=new List<TrainerAutovedenieInterface>();
 readonly Action<string> log;readonly uint thread;readonly IntPtr msul,pultModule,realPult,schema,input,shared;
 readonly IntPtr[] monitors=new IntPtr[2];readonly List<Hook> hooks=new List<Hook>();readonly List<Tuple<int,int,int>> defaults=new List<Tuple<int,int,int>>();
 KeyCallback keyCallback;RenderCallback renderCallback;TextBridge setText;IntPtr cave;
 int toggleCount,commitVersion,pendingField=-2,pendingValue,pendingSide;bool automatic,disposed;double limit=-1,distance=-1;string feedback="Советчик",inputError="",fault="",calculatedMode="—",calculatedPosition="—";
 public string Fault {get{return fault;}}public string FeedbackReason {get{return fault.Length>0?fault:inputError.Length>0?inputError:feedback;}}public IntPtr NativeSchema {get{return schema;}}public int CommitVersion {get{return commitVersion;}}
 public static TrainerAutovedenieInterface Attach(IntPtr realPult,Action<string> log){return new TrainerAutovedenieInterface(realPult,log);}
 TrainerAutovedenieInterface(IntPtr pult,Action<string> writer){
  TrainerSession.Log("AV UI attach entered");if(pult==IntPtr.Zero)throw new ArgumentException("Native cab pointer absent");log=writer??delegate{};thread=GetCurrentThreadId();realPult=pult;
  msul=GetModuleHandle("MSUL_MON.dll");pultModule=GetModuleHandle("Pult1.dll");CheckModule(msul,"MSUL_MON.dll",MsulSha);CheckModule(pultModule,"Pult1.dll",PultSha);
  log("AV UI modules verified");var pultSlot=Marshal.ReadIntPtr(pultModule,0x1a2e60);if(pultSlot==IntPtr.Zero)throw new Exception("AV UI Pult owner slot absent");var owner=Marshal.ReadIntPtr(pultSlot);if(owner==IntPtr.Zero||Marshal.ReadIntPtr(owner,4)!=realPult)throw new Exception("AV UI Pult owner guard");schema=IntPtr.Add(owner,0x18);
  log("AV UI Pult schema owner verified");var msulSlot=Marshal.ReadIntPtr(msul,0x20bb9c);if(msulSlot==IntPtr.Zero)throw new Exception("AV UI MSUL owner slot absent");var msulOwner=Marshal.ReadIntPtr(msulSlot);if(msulOwner==IntPtr.Zero||ClassName(msulOwner)!="TMSULClass")throw new Exception("AV UI MSUL owner guard");
  log("AV UI MSUL owner verified");for(int side=0;side<2;side++){monitors[side]=Marshal.ReadIntPtr(msulOwner,0x20+4*side);if(ClassName(monitors[side])!="TWindowMonitor")throw new Exception("AV UI monitor class guard");}
  shared=Marshal.ReadIntPtr(monitors[0],0xc);if(shared==IntPtr.Zero||Marshal.ReadIntPtr(shared,1)!=realPult||Marshal.ReadIntPtr(shared,9)!=schema||Marshal.ReadIntPtr(monitors[1],0xc)!=shared)throw new Exception("AV UI authoritative shared-schema guard");input=Marshal.ReadIntPtr(realPult,0xc);if(input==IntPtr.Zero)throw new Exception("AV UI input pointer absent");
  Guard(IntPtr.Add(msul,0x1f036c),Hex("558bec33c951"));Guard(IntPtr.Add(msul,0x1cbfca),Hex("e89d160000"));Guard(IntPtr.Add(msul,0x1c695c),Hex("558bec83c4f88955"));Guard(IntPtr.Add(msul,0x6e2c),Hex("53565789c389d689"));Guard(IntPtr.Add(msul,0x6ca4),Hex("8b1085d2741cc700"));
  log("AV UI module/schema/instruction guards verified");keyCallback=OnKey;renderCallback=OnRender;cave=VirtualAlloc(IntPtr.Zero,(UIntPtr)1024,0x3000,0x40);if(cave==IntPtr.Zero)throw new Exception("AV UI executable bridge allocation failed");
  lock(ActiveOwners)ActiveOwners.Add(this);
  try{
   var key=new List<byte>(Hex("9c605250b8"));key.AddRange(BitConverter.GetBytes(Marshal.GetFunctionPointerForDelegate(keyCallback).ToInt32()));key.AddRange(Hex("ffd088442414619d558bec33c951"));Rel(key,0xe9,cave,IntPtr.Add(msul,0x1f0372));WriteCave(cave,key.ToArray());
   var renderStart=IntPtr.Add(cave,128);var render=new List<byte>(Hex("50"));Rel(render,0xe8,renderStart,IntPtr.Add(msul,0x1cd66c));render.AddRange(Hex("9c60ff742424b8"));render.AddRange(BitConverter.GetBytes(Marshal.GetFunctionPointerForDelegate(renderCallback).ToInt32()));render.AddRange(Hex("ffd0619d8d642404c3"));WriteCave(renderStart,render.ToArray());
   var textStart=IntPtr.Add(cave,256);var text=new List<byte>(Hex("558bec83ec04c745fc000000008d45fc8b550c8b4d10"));Rel(text,0xe8,textStart,IntPtr.Add(msul,0x6e2c));text.AddRange(Hex("8b45088b55fc"));Rel(text,0xe8,textStart,IntPtr.Add(msul,0x1c695c));text.AddRange(Hex("8d45fc"));Rel(text,0xe8,textStart,IntPtr.Add(msul,0x6ca4));text.AddRange(Hex("8be55dc20c00"));WriteCave(textStart,text.ToArray());setText=(TextBridge)Marshal.GetDelegateForFunctionPointer(textStart,typeof(TextBridge));
   Install(0x1f036c,Hex("558bec33c951"),cave,false);Install(0x1cbfca,Hex("e89d160000"),renderStart,true);
   if(Marshal.ReadByte(schema,0x328)==0){DefaultInt(0x33d,Marshal.ReadInt32(input,0x30));DefaultInt(0x341,Marshal.ReadInt32(input,0x2c));}log("AV UI native key observer and post-UpdatePos feedback attached; physical train remains unchanged");
  }catch{Dispose();throw;}
 }
 static string ClassName(IntPtr obj){if(obj==IntPtr.Zero)return "null";var vmt=Marshal.ReadIntPtr(obj);var name=Marshal.ReadIntPtr(vmt,-56);int length=Marshal.ReadByte(name);var bytes=new byte[length];Marshal.Copy(IntPtr.Add(name,1),bytes,0,length);return Encoding.ASCII.GetString(bytes);}
 static byte[] Hex(string value){var bytes=new byte[value.Length/2];for(int i=0;i<bytes.Length;i++)bytes[i]=Convert.ToByte(value.Substring(i*2,2),16);return bytes;}
 static bool Equal(byte[] a,byte[] b){if(a.Length!=b.Length)return false;for(int i=0;i<a.Length;i++)if(a[i]!=b[i])return false;return true;}
 static byte[] Read(IntPtr p,int count){var b=new byte[count];Marshal.Copy(p,b,0,count);return b;}
 static void Guard(IntPtr p,byte[] bytes){if(!Equal(Read(p,bytes.Length),bytes))throw new Exception("AV UI instruction guard at "+p.ToString("X"));}
 static void Rel(List<byte> code,byte opcode,IntPtr start,IntPtr target){code.Add(opcode);code.AddRange(BitConverter.GetBytes(unchecked(target.ToInt32()-start.ToInt32()-code.Count-4)));}
 static void CheckModule(IntPtr module,string name,string expectedHash){
  if(module==IntPtr.Zero)throw new Exception("AV UI module absent: "+name);var b=new StringBuilder(32768);uint n=GetModuleFileName(module,b,b.Capacity);if(n==0||n>=b.Capacity)throw new Exception("AV UI loaded path unavailable");
  var expected=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,@"..\work\trenager_static\WORK_TRENAGER_3D\RESOURCES\LOCOMOTIVES\1",name));if(!String.Equals(Path.GetFullPath(b.ToString()),expected,StringComparison.OrdinalIgnoreCase))throw new Exception("AV UI unexpected loaded module path");
  string hash;using(var sha=SHA256.Create())using(var f=File.OpenRead(b.ToString()))hash=BitConverter.ToString(sha.ComputeHash(f)).Replace("-","");if(hash!=expectedHash)throw new Exception("AV UI unknown module SHA: "+name);
 }
 static void WriteCave(IntPtr p,byte[] bytes){Marshal.Copy(bytes,0,p,bytes.Length);if(!FlushInstructionCache(GetCurrentProcess(),p,(UIntPtr)bytes.Length))throw new Exception("AV UI bridge cache flush failed");}
 void Install(int rva,byte[] original,IntPtr target,bool call){var site=IntPtr.Add(msul,rva);Guard(site,original);var code=new List<byte>();Rel(code,call?(byte)0xe8:(byte)0xe9,site,target);while(code.Count<original.Length)code.Add(0x90);var hook=new Hook{Site=site,Original=original,Patch=code.ToArray(),Current=(byte[])original.Clone()};hooks.Add(hook);WriteHook(hook,hook.Patch);}
 static void RestoreProtection(Hook hook){if(!hook.ProtectionPending)return;uint unused;if(!VirtualProtect(hook.Site,(UIntPtr)hook.Original.Length,hook.OldProtection,out unused))throw new Exception("AV UI protection restore failed");hook.ProtectionPending=false;}
 static void WriteHook(Hook hook,byte[] bytes){uint previous;if(!VirtualProtect(hook.Site,(UIntPtr)bytes.Length,0x40,out previous))throw new Exception("AV UI VirtualProtect failed");if(!hook.ProtectionPending){hook.OldProtection=previous;hook.ProtectionPending=true;}try{for(int i=0;i<bytes.Length;i++){Marshal.WriteByte(hook.Site,i,bytes[i]);hook.Current[i]=bytes[i];}if(!FlushInstructionCache(GetCurrentProcess(),hook.Site,(UIntPtr)bytes.Length))throw new Exception("AV UI instruction cache flush failed");Guard(hook.Site,bytes);}finally{RestoreProtection(hook);}}
 void DefaultInt(int offset,int value){int old=Marshal.ReadInt32(schema,offset);if(old==0&&value>0){defaults.Add(Tuple.Create(offset,old,value));Marshal.WriteInt32(schema,offset,value);}}
 void ThreadGuard(){if(disposed)throw new ObjectDisposedException("TrainerAutovedenieInterface");if(GetCurrentThreadId()!=thread)throw new Exception("AV UI must run on its native owner thread");}
 int Side(IntPtr monitor){return monitor==monitors[0]?0:monitor==monitors[1]?1:-1;}
 int OnKey(IntPtr monitor,int raw){int key=raw&255;try{
  if(disposed)return key;if(GetCurrentThreadId()!=thread)throw new Exception("AV UI callback thread guard");int side=Side(monitor);if(side<0)return key;
  // Native SetButton already debounces by monitor+0x15 and ignores zero keys.
  int window=Marshal.ReadByte(monitor,0x14);
  if(window==2&&key==11){toggleCount++;return 0;}
  if(window!=10)return key;int selected=Marshal.ReadInt32(schema,0x324);bool editing=Marshal.ReadByte(schema,0x328)!=0;
  if(key==1&&editing&&selected>=2&&selected<=4){int value;string reason;if(!ValidateEditor(selected,out value,out reason)){inputError=reason;log("AV UI Enter rejected: "+reason);return 0;}inputError="";pendingField=selected;pendingValue=value;pendingSide=side;}
  else if(key==1&&selected==5){inputError="";pendingField=5;pendingValue=0;pendingSide=side;}
  else if(selected==0&&key>=14&&key<=16){pendingField=0;pendingValue=key-14;pendingSide=side;}
  else if(selected==1&&key>=7&&key<=16){pendingField=1;pendingValue=16-key;pendingSide=side;}
  return key;
 }catch(Exception error){fault="Ошибка интерфейса АВ: "+error.Message;try{log(fault);}catch{}return key==1?0:key;}}
 bool ValidateEditor(int selected,out int value,out string reason){value=0;reason="";int count=Marshal.ReadByte(schema,0x329);if(count<1||count>10){reason="Введите число из 1–10 цифр";return false;}var b=Read(IntPtr.Add(schema,0x32a),count);foreach(byte c in b)if(c<48||c>57){reason="Допустимы только цифры";return false;}if(!Int32.TryParse(Encoding.ASCII.GetString(b),out value)||value<=0){reason="Число должно быть положительным и помещаться в Int32";return false;}
  int physical=Marshal.ReadInt32(input,selected==3?0x30:0x2c);if(selected==3&&value!=physical){reason="Масса должна соответствовать физическому составу: "+physical+" т";return false;}if(selected==4&&value!=physical){reason="В составе "+physical+" вагонов; ввод не меняет физический поезд";return false;}return true;
 }
 public TrainerAutovedenieSettings ReadSettings(){ThreadGuard();var result=new TrainerAutovedenieSettings{SelectedField=Marshal.ReadInt32(schema,0x324),Editing=Marshal.ReadByte(schema,0x328)!=0,Mode=Marshal.ReadByte(schema,0x334),Intensity=Marshal.ReadInt32(schema,0x335),TrainNumber=Marshal.ReadInt32(schema,0x339),MassT=Marshal.ReadInt32(schema,0x33d),Wagons=Marshal.ReadInt32(schema,0x341),Empty=Marshal.ReadByte(schema,0x345)!=0};
  result.EditorOpen=Marshal.ReadByte(monitors[0],0x14)==10||Marshal.ReadByte(monitors[1],0x14)==10;
  if(pendingField>=0){bool observed=pendingField==0?result.Mode==pendingValue:pendingField==1?result.Intensity==pendingValue:pendingField==2?!result.Editing&&result.TrainNumber==pendingValue:pendingField==3?!result.Editing&&result.MassT==pendingValue:pendingField==4?!result.Editing&&result.Wagons==pendingValue:Marshal.ReadByte(monitors[pendingSide],0x14)==2;if(observed){if(pendingField==3||pendingField==4){int committedOffset=pendingField==3?0x33d:0x341;defaults.RemoveAll(item=>item.Item1==committedOffset);}commitVersion++;pendingField=-2;}}
  result.CommitVersion=commitVersion;result.Reason=fault.Length>0?fault:inputError.Length>0?inputError:result.Editing||result.EditorOpen?"Завершите ввод Enter":result.TrainNumber<=0?"Введите номер поезда":result.MassT!=Marshal.ReadInt32(input,0x30)?"Проверьте массу физического состава":result.Wagons!=Marshal.ReadInt32(input,0x2c)?"Проверьте число физических вагонов":result.Mode>2?"Неизвестный режим":result.Intensity<0||result.Intensity>9?"Интенсивность должна быть 0–9":result.Empty?"Порожний состав не настроен в физической модели":"";result.Valid=result.Reason.Length==0;return result;
 }
 public bool DrainToggle(){ThreadGuard();if(toggleCount==0)return false;toggleCount--;return true;}
 public void BeforeCycle(TrainerAvpDecision decision){ThreadGuard();if(decision==null)throw new ArgumentNullException("decision");automatic=decision.DriveEnabled&&(decision.State==TrainerAvpState.Automatic||decision.State==TrainerAvpState.Finished);feedback=decision.Reason??"";limit=decision.CurrentLimitKmh;distance=decision.PreviewDistanceM;calculatedMode=decision.SuggestedBrakePosition==4?"Перекрыша":decision.SuggestedBrakePosition==5?"Пневм. торм.":decision.SuggestedTractionCommand==5?"Выбег":decision.SuggestedTractionCommand==1?"Тяга":"Удержание";calculatedPosition=decision.SuggestedBrakePosition==4?"IV":decision.SuggestedBrakePosition==5?"V":decision.SuggestedTractionCommand==5?"0":decision.SuggestedTractionCommand==1?"+1":"—";ReadSettings();}
 public void BeforeCycle(bool auto,string status){ThreadGuard();automatic=auto;feedback=status??"";calculatedMode="—";calculatedPosition="—";ReadSettings();}
 void Text(IntPtr frame,int offset,string value){var control=Marshal.ReadIntPtr(frame,offset);if(ClassName(control)!="TDataCaption")throw new Exception("AV UI DataCaption class guard");var utf=Marshal.StringToHGlobalUni(value);try{setText(control,utf,value.Length);}finally{Marshal.FreeHGlobal(utf);}}
 void OnRender(IntPtr frame){try{if(disposed||setText==null)return;if(GetCurrentThreadId()!=thread)throw new Exception("AV UI render thread guard");int side=Marshal.ReadInt32(frame,0x490)-1;if(side<0||side>1)return;var wizard=Marshal.ReadIntPtr(monitors[side],0x20);if(wizard==IntPtr.Zero||Marshal.ReadIntPtr(wizard,0xc)!=frame)return;if(Marshal.ReadIntPtr(frame)!=IntPtr.Add(msul,0x1c9dfc)||Marshal.ReadIntPtr(frame,0x460)!=shared)throw new Exception("AV UI Mash frame layout guard");
  Text(frame,0x408,automatic?"АВ":"С");Text(frame,0x3ec,"ОС");Text(frame,0x40c,limit>0&&limit<121?Math.Round(limit).ToString(System.Globalization.CultureInfo.InvariantCulture):"—");Text(frame,0x424,distance>=0&&!Double.IsInfinity(distance)&&!Double.IsNaN(distance)?Math.Round(distance).ToString(System.Globalization.CultureInfo.InvariantCulture)+" м":"—");Text(frame,0x42c,inputError.Length>0?"Ошибка ввода":calculatedMode);Text(frame,0x430,calculatedPosition);Text(frame,0x434,"—");
 }catch(Exception error){fault="Ошибка отображения АВ: "+error.Message;try{log(fault);}catch{}}}
 public void Dispose(){if(disposed)return;if(GetCurrentThreadId()!=thread)throw new Exception("AV UI detach thread guard");
  if(hooks.Count>0&&GetModuleHandle("MSUL_MON.dll")!=msul)throw new Exception("AV UI detach module guard");
  for(int i=hooks.Count-1;i>=0;i--){var h=hooks[i];var actual=Read(h.Site,h.Original.Length);if(!Equal(actual,h.Current)&&!Equal(actual,h.Original))throw new Exception("AV UI refuses to restore an instruction it does not own");if(!Equal(actual,h.Original))WriteHook(h,h.Original);else RestoreProtection(h);Guard(h.Site,h.Original);}
  hooks.Clear();for(int i=defaults.Count-1;i>=0;i--){var d=defaults[i];if(Marshal.ReadInt32(schema,d.Item1)==d.Item3)Marshal.WriteInt32(schema,d.Item1,d.Item2);}defaults.Clear();if(cave!=IntPtr.Zero){if(!VirtualFree(cave,UIntPtr.Zero,0x8000))throw new Exception("AV UI bridge free failed");cave=IntPtr.Zero;}disposed=true;setText=null;keyCallback=null;renderCallback=null;lock(ActiveOwners)ActiveOwners.Remove(this);log("AV UI key/render instructions restored, executable bridges released");
 }
}
