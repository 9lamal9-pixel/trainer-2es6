using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Input/holding acceptance only. RB/RBS use the actual native cab controls;
// RBP uses the explicit software operator input and its real native consumer.
// No physical RBP hardware end-to-end claim, auto-ACK, protection/output writes,
// speed assignment, synthetic pressure, force or native trip fixture.
class TrainerVigilanceProbe {
    static readonly BindingFlags HostFlags=BindingFlags.Instance|BindingFlags.NonPublic;
    static TrainerIntegrationHost host;
    static Dictionary<string,IntPtr> mem;
    static CheckBox choice;
    static Button[] buttons;
    static IntPtr position,cabForm,provider,originalCallback,communication;
    static readonly List<object> checks=new List<object>(),rows=new List<object>();
    static readonly Dictionary<IntPtr,CabInput> cabInputs=new Dictionary<IntPtr,CabInput>();
    static bool restored;
    static int electricalSteps;
    static string failure;
    sealed class CabInput { internal byte Original,Assigned; }
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern IntPtr GetModuleHandle(string name);
    [DllImport("kernel32.dll",CharSet=CharSet.Ansi)] static extern IntPtr GetProcAddress(IntPtr module,string name);
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode)] static extern uint GetModuleFileName(IntPtr module,StringBuilder path,int count);
    [StructLayout(LayoutKind.Sequential)] struct MemoryInfo {
        public IntPtr Base,AllocationBase;public uint AllocationProtect;public UIntPtr RegionSize;public uint State,Protect,Type;
    }
    [DllImport("kernel32.dll")] static extern UIntPtr VirtualQuery(IntPtr address,out MemoryInfo info,UIntPtr size);

    static object Field(string name) {
        var field=typeof(TrainerIntegrationHost).GetField(name,HostFlags);
        if(field==null)throw new Exception("Missing host field "+name);
        return field.GetValue(host);
    }
    static object Call(string name,params object[] args) {
        var method=typeof(TrainerIntegrationHost).GetMethod(name,HostFlags);
        if(method==null)throw new Exception("Missing host method "+name);
        try{return method.Invoke(host,args);}catch(TargetInvocationException error){throw new Exception(name+": "+error.InnerException,error.InnerException);}
    }
    static void Check(string name,bool passed,object expected,object actual) {
        checks.Add(new{name=name,passed=passed,expected=expected,actual=actual});
        if(!passed)throw new Exception(name+" expected="+expected+" actual="+actual);
    }
    static bool Range(IntPtr address,int count,bool writable=false) {
        MemoryInfo info;
        if(address==IntPtr.Zero||count<1||VirtualQuery(address,out info,(UIntPtr)Marshal.SizeOf(typeof(MemoryInfo)))==UIntPtr.Zero)return false;
        ulong a=unchecked((uint)address.ToInt32()),b=unchecked((uint)info.Base.ToInt32());uint p=info.Protect&0xff;
        return info.State==0x1000&&(info.Protect&0x101)==0&&a>=b&&a+(uint)count<=b+info.RegionSize.ToUInt32()&&
            (writable?p==4||p==8||p==0x40||p==0x80:p==2||p==4||p==8||p==0x20||p==0x40||p==0x80);
    }
    static string Name(IntPtr obj) {
        if(!Range(obj,4))throw new Exception("Native object pointer guard");
        var vmt=Marshal.ReadIntPtr(obj);if(!Range(IntPtr.Add(vmt,-56),60))throw new Exception("Native VMT guard");
        var text=Marshal.ReadIntPtr(vmt,-56);if(!Range(text,1))throw new Exception("Native class-name guard");
        int length=Marshal.ReadByte(text);if(length<2||length>150||!Range(text,length+1))throw new Exception("Native class-name range guard");
        byte[] bytes=new byte[length];Marshal.Copy(IntPtr.Add(text,1),bytes,0,length);return Encoding.ASCII.GetString(bytes);
    }
    static void Module(IntPtr module,string relative,string expectedSha) {
        Check("Loaded native module "+relative,module!=IntPtr.Zero,true,module.ToString("X"));
        var path=new StringBuilder(32768);uint count=GetModuleFileName(module,path,path.Capacity);
        string expected=Path.GetFullPath(Path.Combine((string)Field("root"),relative));
        Check("Native module path "+relative,count>0&&count<path.Capacity&&String.Equals(Path.GetFullPath(path.ToString()),expected,StringComparison.OrdinalIgnoreCase),expected,path.ToString());
        string actual;using(var sha=SHA256.Create())using(var file=File.OpenRead(path.ToString()))actual=BitConverter.ToString(sha.ComputeHash(file)).Replace("-","");
        Check("Native module SHA "+relative,actual==expectedSha,expectedSha,actual);
    }
    static byte PB(int index){return Marshal.ReadByte(position,index);}
    static byte Warning(){return Marshal.ReadByte(mem["TOutParamAllTRen"],0x30);}
    static byte[] Positions(){var bytes=new byte[6];Marshal.Copy(position,bytes,0,6);return bytes;}
    static bool Same(byte[] a,byte[] b){if(a.Length!=b.Length)return false;for(int n=0;n<a.Length;n++)if(a[n]!=b[n])return false;return true;}
    static void Row(string label){rows.Add(new{label=label,electrical_steps=electricalSteps,positions=Positions(),native_holding_warning=Warning(),software_enabled=choice.Checked,pending_leases=((System.Collections.ICollection)Field("vigilanceLeases")).Count,electrical_frame=Field("vigilanceElectricalFrame")});}
    static void Electrical() {
        int faults=Convert.ToInt32(Field("electricalFaults"));Call("ElectricalCycle");electricalSteps++;
        Check("Electrical frame completed "+electricalSteps,!(bool)Field("vigilanceElectricalFrame"),false,Field("vigilanceElectricalFrame"));
        Check("No new native electrical fault "+electricalSteps,Convert.ToInt32(Field("electricalFaults"))==faults,faults,Field("electricalFaults"));
    }
    static void Raise(Control control,string method,EventArgs args) {
        var callback=typeof(Control).GetMethod(method,BindingFlags.Instance|BindingFlags.NonPublic);
        if(callback==null)throw new Exception("Missing WinForms event method "+method);
        callback.Invoke(control,new object[]{args});
    }
    static void Space(int index,bool pressed){Raise(buttons[index],pressed?"OnKeyDown":"OnKeyUp",new KeyEventArgs(Keys.Space));}
    static void Mouse(int index,bool pressed){Raise(buttons[index],pressed?"OnMouseDown":"OnMouseUp",new MouseEventArgs(MouseButtons.Left,1,5,5,0));}
    static void Cab(int offset,byte value) {
        var control=Marshal.ReadIntPtr(cabForm,offset);
        Check("Native cab control "+offset.ToString("X"),Range(control,4),true,control.ToString("X"));
        // The source offset is connected by the SHA-verified UpdatePultMash code.
        IntPtr source=IntPtr.Add(control,0x1d9);Check("Native cab input range "+offset.ToString("X"),Range(source,1,true),true,source.ToString("X"));
        CabInput saved;if(!cabInputs.TryGetValue(source,out saved)){saved=new CabInput{Original=Marshal.ReadByte(source)};cabInputs.Add(source,saved);}
        saved.Assigned=value;Marshal.WriteByte(source,value);
    }
    static void Code(int rva,string expected) {
        var bytes=new byte[expected.Length/2];Marshal.Copy(IntPtr.Add(communication,rva),bytes,0,bytes.Length);
        string actual=BitConverter.ToString(bytes).Replace("-","");Check("Native input code "+rva.ToString("X"),actual==expected,expected,actual);
    }
    static void Setup() {
        Check("Native host ready",(bool)Field("commReady")&&(bool)Field("klubReady")&&(bool)Field("avpInitialized"),true,true);
        mem=(Dictionary<string,IntPtr>)Field("mem");position=mem["TPBBtnPos"];
        communication=GetModuleHandle("Communication1.dll");
        Module(communication,@"RESOURCES\LOCOMOTIVES\1\Communication1.dll","27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F");
        var pultModule=GetModuleHandle("Pult1.dll");
        Module(pultModule,@"RESOURCES\LOCOMOTIVES\1\Pult1.dll","7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5");
        var common=Marshal.ReadIntPtr(mem["TPult"]);
        Check("Actual PB button alias",Marshal.ReadIntPtr(common,0xf)==mem["TPBBtn"],mem["TPBBtn"].ToString("X"),Marshal.ReadIntPtr(common,0xf).ToString("X"));
        Check("Actual vigilance position alias",Marshal.ReadIntPtr(mem["TPBBtn"])==position,position.ToString("X"),Marshal.ReadIntPtr(mem["TPBBtn"]).ToString("X"));
        Check("Actual vigilance range",Range(position,6),true,Range(position,6));
        var cab=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);
        Check("Native cab class",Name(cab)=="TPultMash2ES6","TPultMash2ES6",Name(cab));cabForm=Marshal.ReadIntPtr(cab,0xc);
        Check("Native cab form class",Name(cabForm)=="TPultMashIndexForm","TPultMashIndexForm",Name(cabForm));
        Check("Native cab Pult alias",Marshal.ReadIntPtr(cabForm,0x530)==mem["TPult"],true,true);
        Code(0x2af3f9,"8B80D8030000");Code(0x2af41a,"8B800C050000");
        provider=Marshal.ReadIntPtr(mem["CommOwner"],0x264);originalCallback=GetProcAddress(pultModule,"ProceedPult");
        var existingBridge=(TrainerAutovedenieNativeBridge)Field("avpNative");
        IntPtr expectedCallback=existingBridge==null?originalCallback:(IntPtr)typeof(TrainerAutovedenieNativeBridge).GetField("observerPointer",HostFlags).GetValue(existingBridge);
        Check("Native callback ownership before opt-in",originalCallback!=IntPtr.Zero&&Marshal.ReadIntPtr(provider,8)==expectedCallback,expectedCallback.ToString("X"),Marshal.ReadIntPtr(provider,8).ToString("X"));
        choice=(CheckBox)Field("vigilanceSoftwareInputs");buttons=(Button[])Field("vigilanceButtons");Call("RefreshVigilanceControls");
        Check("Software vigilance default off",!choice.Checked,false,choice.Checked);
        Cab(0x3d8,0);Cab(0x50c,0);Electrical();
        Check("Neutral native vigilance inputs",PB(0)==0&&PB(1)==0&&PB(2)==0,"0,0,0",Positions());
        Check("Native prolonged-holding warning initially clear",Warning()==0,0,Warning());
    }
    static void DefaultAndPhysical() {
        var initial=Positions();using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Default input scope writes nothing",Same(initial,Positions()),initial,Positions());
        for(int n=0;n<2;n++) {
            Cab(n==0?0x3d8:0x50c,1);Electrical();
            Check("Physical cab "+(n==0?"RB":"RBS")+" reaches real native input",PB(n)==1,1,PB(n));
            initial=Positions();using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Disabled software preserves physical request "+n,Same(initial,Positions()),initial,Positions());
            Cab(n==0?0x3d8:0x50c,0);Electrical();Check("Physical native release "+n,PB(n)==0,0,PB(n));
        }
        Row("default_and_physical_inputs");
    }
    static void SoftwareEventsAndScopes() {
        choice.Checked=true;
        var bridge=(TrainerAutovedenieNativeBridge)Field("avpNative");
        Check("Explicit opt-in attaches generic native callback",bridge!=null&&Marshal.ReadIntPtr(provider,8)!=originalCallback,true,bridge!=null);
        Check("Source remains an input-only idle bridge",!bridge.OwnsCommands,false,bridge.OwnsCommands);
        byte[] initial=Positions();using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Enabled without a press writes nothing",Same(initial,Positions()),initial,Positions());
        for(int n=0;n<3;n++) {
            Space(n,true);using((IDisposable)Call("BorrowVigilanceOperatorInputs")) {
                Check("Space reaches native source cell "+n,PB(n)==1,1,PB(n));
                for(int other=0;other<3;other++)if(other!=n)Check("Other native cell preserved "+n+"/"+other,PB(other)==initial[other],initial[other],PB(other));
            }
            Check("Immediate lease restores source "+n,Same(initial,Positions()),initial,Positions());
            Space(n,false);using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Space key-up releases request "+n,Same(initial,Positions()),initial,Positions());
        }
        Mouse(0,true);using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Mouse down requests RB",PB(0)==1,1,PB(0));
        buttons[0].Capture=false;Raise(buttons[0],"OnMouseCaptureChanged",EventArgs.Empty);
        using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Capture loss releases RB",Same(initial,Positions()),initial,Positions());
        Space(1,true);Raise(buttons[1],"OnLostFocus",EventArgs.Empty);
        using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Lost focus releases RBS",Same(initial,Positions()),initial,Positions());
        Space(2,true);typeof(Form).GetMethod("OnDeactivate",BindingFlags.Instance|BindingFlags.NonPublic).Invoke(host,new object[]{EventArgs.Empty});
        using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Deactivation releases RBP",Same(initial,Positions()),initial,Positions());
        // Keep a real physical RB while requesting software RBS: no suppression.
        Cab(0x3d8,1);Electrical();Space(1,true);
        using((IDisposable)Call("BorrowVigilanceOperatorInputs"))Check("Software request ORs with physical RB",PB(0)==1&&PB(1)==1,"1,1",Positions());
        Check("Physical RB survives software restore",PB(0)==1,1,PB(0));Space(1,false);Cab(0x3d8,0);Electrical();
        Space(0,true);IDisposable lease=null;Call("BeginVigilanceElectricalInputs");
        try {
            lease=(IDisposable)Call("BorrowVigilanceOperatorInputs");lease.Dispose();
            Check("Callback-local Dispose is deferred",PB(0)==1,1,PB(0));
            Check("Deferred lease retained",((System.Collections.ICollection)Field("vigilanceLeases")).Count>0,">0",((System.Collections.ICollection)Field("vigilanceLeases")).Count);
        }finally{Call("EndVigilanceElectricalInputs");if(lease!=null)lease.Dispose();Space(0,false);}
        Check("Electrical End restores native source",PB(0)==0,0,PB(0));
        Check("Electrical End removes retained lease",((System.Collections.ICollection)Field("vigilanceLeases")).Count==0,0,((System.Collections.ICollection)Field("vigilanceLeases")).Count);
        Row("software_events_and_deferred_restore");
    }
    static void ProlongedHolding() {
        for(int n=0;n<3;n++) {
            Electrical();Check("Native warning clean before held source "+n,Warning()==0,0,Warning());Space(n,true);
            for(int step=1;step<=71;step++) {
                Electrical();Check("Scoped source restored after electrical step "+n+"/"+step,PB(n)==0,0,PB(n));
                if(step==70)Check("Native warning stays clear through step70 source "+n,Warning()==0,0,Warning());
            }
            Check("Native holding warning at step71 source "+n,Warning()==1,1,Warning());Row("native_hold71_"+n);
            Space(n,false);Electrical();Check("Actual release clears native warning source "+n,Warning()==0,0,Warning());
        }
    }
    static void Save(bool passed) {
        var serializer=new JavaScriptSerializer{MaxJsonLength=2000000};
        File.WriteAllText(TrainerSession.ResultPath("Trainer_vigilance_operator.json"),serializer.Serialize(new{passed=passed,error=failure,checks=checks,rows=rows,electrical_steps=electricalSteps,guards_restored=restored,
            scope="Fresh production host input acceptance: actual native cab RB/RBS, explicit software RBP to the real PBBPos consumer; default off, WinForms press/release/capture/focus/deactivation, physical OR, immediate/deferred restore, native 70/71-step holding warning and release. No physical RBP hardware end-to-end, protection/trip semantics, native force/speed/pressure output writes or automatic acknowledgement."}));
    }
    [STAThread] static int Main(string[] args) {
        Application.EnableVisualStyles();bool passed=false;
        try {
            host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);var handle=host.Handle;Call("StartNative");
            var timer=(Timer)Field("timer");Check("Native startup timer exists",timer!=null,true,timer!=null);timer.Stop();
            Setup();DefaultAndPhysical();SoftwareEventsAndScopes();ProlongedHolding();
            choice.Checked=false;Call("RestoreVigilanceOperatorInputs");
            Check("Final leases empty",((System.Collections.ICollection)Field("vigilanceLeases")).Count==0,0,((System.Collections.ICollection)Field("vigilanceLeases")).Count);
            Check("Source-frame flag cleared",!(bool)Field("vigilanceElectricalFrame"),false,Field("vigilanceElectricalFrame"));passed=true;
        }catch(Exception error){failure=error.ToString();Console.WriteLine(failure);}
        finally {
            if(host!=null) {
                try {
                    if((bool)Field("vigilanceElectricalFrame"))Call("EndVigilanceElectricalInputs");
                    if(choice!=null)choice.Checked=false;Call("RestoreVigilanceOperatorInputs");
                    foreach(var item in cabInputs)if(Marshal.ReadByte(item.Key)==item.Value.Assigned)Marshal.WriteByte(item.Key,item.Value.Original);
                    foreach(var item in cabInputs)Check("Physical cab source restored "+item.Key.ToString("X"),Marshal.ReadByte(item.Key)==item.Value.Original,item.Value.Original,Marshal.ReadByte(item.Key));
                    Call("CloseAutovedenie");
                    if(provider!=IntPtr.Zero)Check("Generic native callback restored",Marshal.ReadIntPtr(provider,8)==originalCallback,originalCallback.ToString("X"),Marshal.ReadIntPtr(provider,8).ToString("X"));
                    restored=true;
                }catch(Exception error){passed=false;if(failure==null)failure=error.ToString();}
                try{host.Close();host.Dispose();}catch(Exception error){passed=false;if(failure==null)failure=error.ToString();}
            }
            passed=passed&&restored;Save(passed);
        }
        Console.WriteLine("Vigilance operator input checks="+checks.Count+" passed="+passed);return passed?0:1;
    }
}
