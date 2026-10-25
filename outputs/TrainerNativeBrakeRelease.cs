using System;
using System.IO;
using System.Text;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Windows.Forms;
using System.Drawing;

// Pult1 publishes an enum input and a separate processed Boolean. TormModel
// consumes a Boolean at the same input offset. Borrow it only during its call.
public sealed class TrainerNativeBrakeRelease : IDisposable {
    const string PultSha="7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5";
    // Current native gauges and Flow.Gauge use kgf/cm2. RE1 §3.16.2 prints
    // 0.48MPa (5.0kgf/cm2); this explicitly selects the printed kgf profile,
    // not an exact conversion from0.48MPa. See sa47-release-policy.json.
    public const float TmCancelThresholdKgf=5.0f;
    const int Global=0x1a2e60,Cell=0x1bc028;
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {public IntPtr Base,Allocation;public uint AllocationProtect;public UIntPtr Size;public uint State,Protect,Type;}
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr p,out MemoryInfo info,UIntPtr size);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module,StringBuilder path,int count);
    readonly IntPtr module,tormModule,cell,owner,vmt,pult,loc,schema,headTm;
    readonly TrainerSa47MemoryPatch memoryPatch;
    readonly int thread;
    readonly string communicationPath;
    int snapshotTick=-1;
    byte snapshotRaw;
    bool request,fresh,disposed;
    bool electricalEpochPending;uint producerEntriesBefore;
    InputScope active;
    IntPtr communication,cabOwner,cab,form,widget;
    bool cabSourceWasNative,nativeButtonArmed;
    CabInputScope cabInput;
    static void Readable(IntPtr p,int count){MemoryInfo m;if(p==IntPtr.Zero||count<1||VirtualQuery(p,out m,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)throw new Exception("SA47 absent memory range");
        ulong start=unchecked((uint)p.ToInt32()),first=unchecked((uint)m.Base.ToInt32());uint protection=m.Protect&255;
        if(m.State!=0x1000||(m.Protect&0x101)!=0||start<first||start+(uint)count>first+m.Size.ToUInt32()||protection!=2&&protection!=4&&protection!=8&&protection!=0x20&&protection!=0x40&&protection!=0x80)throw new Exception("SA47 unreadable memory range");}
    static IntPtr Pointer(IntPtr p,int offset){var at=IntPtr.Add(p,offset);Readable(at,4);return Marshal.ReadIntPtr(at);}
    static void Writable(IntPtr p){Readable(p,1);MemoryInfo info;VirtualQuery(p,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)));uint value=info.Protect&255;if(value!=4&&value!=8&&value!=0x40&&value!=0x80)throw new Exception("SA47 input is not writable");}
    static void Bytes(IntPtr p,string hex){Readable(p,hex.Length/2);for(int i=0;i<hex.Length/2;i++)if(Marshal.ReadByte(p,i)!=Convert.ToByte(hex.Substring(i*2,2),16))throw new Exception("SA47 code/layout guard");}
    public TrainerNativeBrakeRelease(string resources,IntPtr realPult,IntPtr realLoc){
        if(IntPtr.Size!=4)throw new Exception("SA47 requires native x86 ABI");thread=System.Threading.Thread.CurrentThread.ManagedThreadId;
        communicationPath=Path.GetFullPath(Path.Combine(resources,@"LOCOMOTIVES\1\Communication1.dll"));
        module=GetModuleHandle("Pult1.dll");if(module==IntPtr.Zero)throw new Exception("SA47 native module absent");
        var path=new StringBuilder(32768);uint length=GetModuleFileName(module,path,path.Capacity);
        if(length==0||length>=path.Capacity||!String.Equals(Path.GetFullPath(path.ToString()),Path.GetFullPath(Path.Combine(resources,@"LOCOMOTIVES\1\Pult1.dll")),StringComparison.OrdinalIgnoreCase))throw new Exception("SA47 loaded module path guard");
        using(var file=File.OpenRead(path.ToString()))using(var sha=SHA256.Create())if(BitConverter.ToString(sha.ComputeHash(file)).Replace("-","")!=PultSha)throw new Exception("SA47 module SHA guard");
        Bytes(IntPtr.Add(module,0x18b92c),"558bec83c4f88945fc8b45fc83c0188945f88b45f859595dc3");
        Bytes(IntPtr.Add(module,0x18c676),"0fb6928c000000899004010000");
        Bytes(IntPtr.Add(module,0x18c79f),"c6800301000000");
        tormModule=GetModuleHandle("TormModel.dll");if(tormModule==IntPtr.Zero)throw new Exception("SA47 pneumatic consumer absent");
        path.Length=0;length=GetModuleFileName(tormModule,path,path.Capacity);
        if(length==0||length>=path.Capacity||!String.Equals(Path.GetFullPath(path.ToString()),Path.GetFullPath(Path.Combine(resources,"TormModel.dll")),StringComparison.OrdinalIgnoreCase))throw new Exception("SA47 pneumatic consumer path guard");
        using(var file=File.OpenRead(path.ToString()))using(var sha=SHA256.Create())if(BitConverter.ToString(sha.ComputeHash(file)).Replace("-","")!="F871640C5B7699901E3AFBF755BB2B797B659524C29BAA046B311CA6ECC875B9")throw new Exception("SA47 pneumatic consumer SHA guard");
        Bytes(IntPtr.Add(tormModule,0x17a265),"80b88c00000001");Bytes(IntPtr.Add(tormModule,0x17e34a),"80b88c00000001");
        cell=Pointer(module,Global);if(cell!=IntPtr.Add(module,Cell))throw new Exception("SA47 global cell guard");
        owner=Pointer(cell,0);vmt=Pointer(owner,0);
        MemoryInfo vmtInfo;if(vmt==IntPtr.Zero||vmt!=Pointer(module,0x18b5c8)||VirtualQuery(vmt,out vmtInfo,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero||vmtInfo.Allocation!=module)throw new Exception("SA47 initial native class guard");
        pult=realPult;loc=realLoc;headTm=Pointer(pult,8);schema=IntPtr.Add(owner,0x18);
        Validate();
        memoryPatch=new TrainerSa47MemoryPatch(module,owner,pult,loc,headTm,TmCancelThresholdKgf);
    }
    void Validate(bool checkProducer=true){
        if(disposed||System.Threading.Thread.CurrentThread.ManagedThreadId!=thread)throw new Exception("SA47 lifetime/thread guard");
        if(GetModuleHandle("Pult1.dll")!=module||GetModuleHandle("TormModel.dll")!=tormModule||Pointer(module,Global)!=cell||Pointer(cell,0)!=owner||Pointer(owner,0)!=vmt||Pointer(owner,4)!=pult||Pointer(pult,4)!=loc||Pointer(pult,8)!=headTm)throw new Exception("SA47 owner graph changed");
        Readable(schema,0x10b);Readable(IntPtr.Add(loc,0x7d),0x1f);
        if(checkProducer&&memoryPatch!=null)memoryPatch.Validate();
    }
    public void Invalidate(){fresh=false;electricalEpochPending=false;}
    public IDisposable BorrowCabInput(IntPtr realCommunicationOwner,int? position){
        Validate();if(cabInput!=null)throw new Exception("SA47 nested cab input scope");
        if(communication==IntPtr.Zero){
            var found=GetModuleHandle("Communication1.dll");if(found==IntPtr.Zero)throw new Exception("SA47 cab module absent");
            var path=new StringBuilder(32768);uint length=GetModuleFileName(found,path,path.Capacity);
            if(length==0||length>=path.Capacity||!String.Equals(Path.GetFullPath(path.ToString()),communicationPath,StringComparison.OrdinalIgnoreCase))throw new Exception("SA47 cab module path guard");
            using(var input=File.OpenRead(path.ToString()))using(var hash=SHA256.Create())if(BitConverter.ToString(hash.ComputeHash(input)).Replace("-","")!=TrainerNativeVirtualPult.CommunicationSha256)throw new Exception("SA47 cab SHA guard");
            if(IntPtr.Add(realCommunicationOwner,0x24)!=pult)throw new Exception("SA47 cab/pult owner mismatch");
            var foundCab=Pointer(realCommunicationOwner,0x2bc);var foundForm=Pointer(foundCab,0xc);var foundWidget=Pointer(foundForm,0x3e4);
            if(Pointer(foundWidget,0)!=IntPtr.Add(found,0x29ea70))throw new Exception("SA47 initial native widget class guard");
            cabOwner=realCommunicationOwner;cab=foundCab;form=foundForm;widget=foundWidget;communication=found;
        }
        if(realCommunicationOwner!=cabOwner)throw new Exception("SA47 cab owner mismatch");ValidateCab();
        byte original=Marshal.ReadByte(widget,0x1d9);if(original>2||position.HasValue&&(position.Value<0||position.Value>2))throw new Exception("SA47 cab position domain guard");
        // RE1: fixed readiness1, momentary impulse2, explicit cancellation0.
        // Physical button-up is readiness, not a cancellation. On source handover
        // a held native button must first be released before it can produce2.
        byte assigned;
        if(position.HasValue){cabSourceWasNative=false;assigned=(byte)position.Value;}
        else{
            if(!cabSourceWasNative)nativeButtonArmed=original==0;
            if(original==0)nativeButtonArmed=true;
            assigned=(byte)(original==2?2:original==1&&nativeButtonArmed?2:1);cabSourceWasNative=true;
        }
        // A managed impulse selected from cancellation traverses readiness1.
        // Publish that intermediate position once even when the UI event was
        // quicker than the electrical clock, then publish the held impulse2.
        if(assigned==2&&snapshotRaw==0)assigned=1;
        producerEntriesBefore=(uint)memoryPatch.Snapshot()["Entries"];electricalEpochPending=true;
        Writable(IntPtr.Add(widget,0x1d9));cabInput=new CabInputScope(this,original,assigned);return cabInput;
    }
    void ValidateCab(bool checkProducer=true){
        Validate(checkProducer);if(communication==IntPtr.Zero||communication!=GetModuleHandle("Communication1.dll")||Pointer(cabOwner,0x2bc)!=cab||Pointer(cab,0xc)!=form||Pointer(form,0x3e4)!=widget||Pointer(widget,0)!=IntPtr.Add(communication,0x29ea70))throw new Exception("SA47 native widget graph guard");
        Readable(IntPtr.Add(widget,0x1d8),2);if(Marshal.ReadByte(widget,0x1d8)!=0)throw new Exception("SA47 native button Fixed guard");
    }
    sealed class CabInputScope : IDisposable {
        readonly TrainerNativeBrakeRelease parent;readonly byte original,assigned;bool done;
        public CabInputScope(TrainerNativeBrakeRelease p,byte before,byte value){parent=p;original=before;assigned=value;Marshal.WriteByte(p.widget,0x1d9,value);}
        public void Dispose(){if(done)return;parent.ValidateCab(false);if(parent.cabInput!=this)throw new Exception("SA47 cab scope ownership changed");
            byte current=Marshal.ReadByte(parent.widget,0x1d9);
            if(current==assigned){Writable(IntPtr.Add(parent.widget,0x1d9));Marshal.WriteByte(parent.widget,0x1d9,original);}
            else if(current>1)throw new Exception("SA47 cab foreign state outside physical button domain");
            // A native mouse-up/down may arrive reentrantly. Preserve its new0/1.
            done=true;parent.cabInput=null;
        }
    }
    public void Capture(int tick){bool pending=electricalEpochPending;electricalEpochPending=false;fresh=false;Validate();if(active!=null)throw new Exception("SA47 capture during native input borrow");
        byte value=Marshal.ReadByte(schema,0x103),raw=Marshal.ReadByte(loc,0x8c);
        if(value>1||raw>2||Marshal.ReadInt32(schema,0x104)!=raw)throw new Exception("SA47 processed-state domain guard");
        if(!pending||(uint)memoryPatch.Snapshot()["Entries"]==producerEntriesBefore)throw new Exception("SA47 producer did not execute in this electrical cycle");
        snapshotTick=tick;snapshotRaw=raw;request=value==1;fresh=true;
    }
    public IDisposable Borrow(int tick,bool inhibit,out bool selected){
        bool wasFresh=fresh;fresh=false;Validate();if(active!=null)throw new Exception("SA47 nested input borrow");
        byte raw=Marshal.ReadByte(loc,0x8c);if(raw>2)throw new Exception("SA47 raw position domain guard");
        // Commands may have changed since the electrical cycle (SAUT/EPK/test
        // arbitration). Never reuse a release snapshot against a new brake.
        selected=wasFresh&&snapshotTick==tick&&raw==snapshotRaw&&request&&!inhibit&&raw!=0&&
            Marshal.ReadByte(loc,0x7d)<5&&Marshal.ReadByte(loc,0x7e)<=2&&
            Marshal.ReadByte(loc,0x8f)==1&&Marshal.ReadByte(loc,0x90)==1;
        Writable(IntPtr.Add(loc,0x8c));fresh=false;active=new InputScope(this,raw,(byte)(selected?1:0));return active;
    }
    sealed class InputScope : IDisposable {
        readonly TrainerNativeBrakeRelease parent;readonly byte original,assigned;bool done;
        public InputScope(TrainerNativeBrakeRelease p,byte before,byte value){parent=p;original=before;assigned=value;Marshal.WriteByte(p.loc,0x8c,value);}
        public void Dispose(){if(done)return;parent.Validate(false);
            if(parent.active!=this)throw new Exception("SA47 input scope ownership guard");
            if(Marshal.ReadByte(parent.loc,0x8c)!=assigned){done=true;parent.active=null;throw new Exception("SA47 foreign input write; not overwritten");}
            Writable(IntPtr.Add(parent.loc,0x8c));Marshal.WriteByte(parent.loc,0x8c,original);done=true;parent.active=null;
        }
    }
    public void Dispose(){if(disposed)return;if(cabInput!=null)cabInput.Dispose();if(active!=null)active.Dispose();if(memoryPatch!=null)memoryPatch.Dispose();fresh=false;disposed=true;}
}

partial class TrainerIntegrationHost {
    TrainerNativeBrakeRelease nativeBrakeRelease;
    bool nativeAutomaticReleaseForStep;
    IDisposable nativeReleaseCabInput;
    byte previousSa47Input;
    ComboBox sa47Source;Button sa47ImpulseButton;bool sa47Momentary;
    void InitializeBrakeReleaseOperatorControls(){
        int top=0;foreach(Control control in Controls)top=Math.Max(top,control.Bottom);top+=8;
        Controls.Add(new Label{Left=10,Top=top+5,Width=230,Text="SA47 — отпуск локомотива"});
        sa47Source=new ComboBox{Left=245,Top=top,Width=305,DropDownStyle=ComboBoxStyle.DropDownList};
        sa47Source.Items.AddRange(new object[]{"С кнопки пульта","0 — отмена отпуска","1 — готовность"});sa47Source.SelectedIndex=0;
        sa47Source.SelectedIndexChanged+=delegate{sa47Momentary=false;CancelAutovedenie("Изменено управление отпуском локомотива");};Controls.Add(sa47Source);
        sa47ImpulseButton=new Button{Left=560,Top=top-1,Width=210,Height=30,Text="2 — импульс отпуска"};
        sa47ImpulseButton.MouseDown+=(s,e)=>{if(e.Button==MouseButtons.Left)SetSa47OperatorPosition(2);};
        sa47ImpulseButton.MouseUp+=(s,e)=>{if(e.Button==MouseButtons.Left&&sa47Momentary)SetSa47OperatorPosition(1);};
        sa47ImpulseButton.MouseCaptureChanged+=delegate{if(!sa47ImpulseButton.Capture&&sa47Momentary)SetSa47OperatorPosition(1);};
        sa47ImpulseButton.KeyDown+=(s,e)=>{if(e.KeyCode==Keys.Space||e.KeyCode==Keys.Enter)SetSa47OperatorPosition(2);};
        sa47ImpulseButton.KeyUp+=(s,e)=>{if((e.KeyCode==Keys.Space||e.KeyCode==Keys.Enter)&&sa47Momentary)SetSa47OperatorPosition(1);};
        sa47ImpulseButton.LostFocus+=delegate{if(sa47Momentary)SetSa47OperatorPosition(1);};
        Deactivate+=delegate{if(sa47Momentary)SetSa47OperatorPosition(1);};Controls.Add(sa47ImpulseButton);
        AutoScroll=true;ClientSize=new Size(ClientSize.Width,Math.Max(ClientSize.Height,Math.Min(top+38,760)));
    }
    void SetSa47OperatorPosition(int? position){
        if(position.HasValue&&(position.Value<0||position.Value>2))throw new ArgumentOutOfRangeException("position");
        if(sa47Source==null)throw new Exception("SA47 operator controls not initialized");
        if(Sa47OperatorPosition()!=position)CancelAutovedenie("Ручное управление отпуском локомотива");
        sa47Source.SelectedIndex=!position.HasValue?0:position.Value==0?1:2;sa47Momentary=position==2;
    }
    int? Sa47OperatorPosition(){return sa47Momentary?2:sa47Source==null||sa47Source.SelectedIndex==0?(int?)null:sa47Source.SelectedIndex==1?0:1;}
    partial void BeforeReleaseElectricalCycle(){
        // Bind before commStep so its first arithmetic failure still leaves a
        // validated neutral input scope for the following pneumatic step.
        if(commReady&&nativeBrakeRelease==null)nativeBrakeRelease=new TrainerNativeBrakeRelease(resources,mem["TPult"],mem["TLocParam"]);
        if(nativeBrakeRelease!=null)nativeBrakeRelease.Invalidate();nativeAutomaticReleaseForStep=false;
        if(nativeReleaseCabInput!=null)throw new Exception("SA47 previous cab input not restored");
        if(nativeBrakeRelease!=null)nativeReleaseCabInput=nativeBrakeRelease.BorrowCabInput(mem["CommOwner"],Sa47OperatorPosition());
    }
    partial void AfterReleaseElectricalCycle(){
        if(!commReady)return;
        if(nativeBrakeRelease==null)throw new Exception("SA47 binding absent after electrical cycle");
        nativeBrakeRelease.Capture(ticks);
        byte raw=Marshal.ReadByte(mem["TLocParam"],0x8c);
        if(raw==2&&previousSa47Input!=2)CancelAutovedenie("Импульс отпуска локомотива SA47");previousSa47Input=raw;
    }
    partial void EndReleaseElectricalCycle(){if(nativeReleaseCabInput!=null){try{nativeReleaseCabInput.Dispose();nativeReleaseCabInput=null;}catch{nativeBrakeRelease.Invalidate();nativeAutomaticReleaseForStep=false;throw;}}}
    partial void BorrowNativeReleaseInput(bool inhibit,ref IDisposable scope){
        nativeAutomaticReleaseForStep=false;
        if(nativeBrakeRelease==null)throw new Exception("SA47 binding absent before native pneumatic call");
        scope=nativeBrakeRelease.Borrow(ticks,inhibit,out nativeAutomaticReleaseForStep);
    }
    void CloseNativeBrakeRelease(){if(nativeBrakeRelease!=null){nativeBrakeRelease.Dispose();nativeBrakeRelease=null;}nativeAutomaticReleaseForStep=false;}
}
