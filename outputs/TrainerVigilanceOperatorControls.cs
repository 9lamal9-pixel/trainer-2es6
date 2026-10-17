using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Windows.Forms;

// Explicit alternative operator source for the three native vigilance buttons.
// Production never presses a button automatically. Out+30 is the native warning
// about prolonged holding, not an acknowledgement request to be cleared here.
partial class TrainerIntegrationHost {
    CheckBox vigilanceSoftwareInputs;
    readonly Button[] vigilanceButtons = new Button[3];
    readonly bool[] vigilanceMouseHeld = new bool[3], vigilanceKeyHeld = new bool[3];
    Label vigilanceInputStatus;
    Func<string> vigilanceStatusReader;
    uint vigilanceInputThread;
    bool vigilanceElectricalFrame, vigilanceChangingChoice;
    string vigilanceInputFault = "";
    IntPtr vigilanceCommunicationModule;
    string vigilanceCommunicationPath;
    readonly List<VigilanceInputLease> vigilanceLeases = new List<VigilanceInputLease>();
    const string VigilanceCommunicationSha = "27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F";

    [DllImport("kernel32.dll", EntryPoint="GetCurrentThreadId")]
    static extern uint VigilanceThreadId();
    [DllImport("kernel32.dll", EntryPoint="GetModuleHandleW", CharSet=CharSet.Unicode)]
    static extern IntPtr VigilanceModuleHandle(string name);
    [DllImport("kernel32.dll", EntryPoint="GetModuleFileNameW", CharSet=CharSet.Unicode)]
    static extern uint VigilanceModulePath(IntPtr module, StringBuilder path, int size);
    [StructLayout(LayoutKind.Sequential)] struct VigilanceMemoryInfo {
        public IntPtr Base, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll", EntryPoint="VirtualQuery")]
    static extern UIntPtr VigilanceQuery(IntPtr address, out VigilanceMemoryInfo info, UIntPtr size);

    void InstallVigilanceControls() {
        if (vigilanceSoftwareInputs != null) throw new InvalidOperationException("Ввод РБ уже установлен");
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Ввод РБ требует x86");
        vigilanceInputThread = VigilanceThreadId();
        ClientSize = new Size(Math.Max(800, ClientSize.Width), Math.Max(680, ClientSize.Height));
        vigilanceSoftwareInputs = new CheckBox {
            Text="Программные РБ/РБС/РБП", Left=10, Top=580, Width=475,
            Checked=false, Enabled=false
        };
        vigilanceSoftwareInputs.CheckedChanged += VigilanceChoiceChanged;
        Controls.Add(vigilanceSoftwareInputs);
        string[] names = { "РБ", "РБС", "РБП" };
        for (int n=0; n<3; n++) {
            int index=n;
            var button=new Button { Text=names[n]+" — удерживать", Left=10+n*260, Top=612, Width=245, Height=35, Enabled=false };
            button.MouseDown += delegate(object sender, MouseEventArgs e) {
                if (e.Button==MouseButtons.Left) SetVigilanceHeld(index,true,true);
            };
            button.MouseUp += delegate(object sender, MouseEventArgs e) {
                if (e.Button==MouseButtons.Left) SetVigilanceHeld(index,true,false);
            };
            button.MouseCaptureChanged += delegate { if (!button.Capture) ClearVigilanceButton(index); };
            button.KeyDown += delegate(object sender, KeyEventArgs e) {
                if (e.KeyCode==Keys.Space) { SetVigilanceHeld(index,false,true); e.Handled=true; e.SuppressKeyPress=true; }
            };
            button.KeyUp += delegate(object sender, KeyEventArgs e) {
                if (e.KeyCode==Keys.Space) { SetVigilanceHeld(index,false,false); e.Handled=true; e.SuppressKeyPress=true; }
            };
            button.LostFocus += delegate { ClearVigilanceButton(index); };
            vigilanceButtons[n]=button; Controls.Add(button);
        }
        Deactivate += delegate { ClearVigilanceHeld(); RefreshVigilanceControls(); };
        vigilanceInputStatus = new Label {
            Text="Штатные входы РБ. Автоматического подтверждения бдительности нет.",
            Left=10, Top=654, Width=775, Height=23
        };
        Controls.Add(vigilanceInputStatus);
        var area=Screen.FromPoint(Location).WorkingArea;
        Location=new Point(Location.X, Math.Max(area.Top, Math.Min(Location.Y, area.Bottom-Height)));
    }
    void VigilanceChoiceChanged(object sender, EventArgs e) {
        VigilanceThreadGuard();
        if (vigilanceChangingChoice) return;
        ClearVigilanceHeld(); vigilanceInputFault="";
        // Selecting this source or pressing RB never cancels automatic driving.
        if (vigilanceSoftwareInputs.Checked && avpInitialized) {
            try { EnsureAutovedenieNativeBridge(); }
            catch (Exception error) {
                vigilanceInputFault="Ввод РБ недоступен: "+error.Message;
                vigilanceChangingChoice=true;
                try { vigilanceSoftwareInputs.Checked=false; } finally { vigilanceChangingChoice=false; }
                Log(vigilanceInputFault);
            }
        }
        RefreshVigilanceControls();
    }
    void SetVigilanceHeld(int index, bool mouse, bool held) {
        VigilanceThreadGuard();
        if (index<0 || index>2) throw new ArgumentOutOfRangeException("index");
        if (held && (!commReady || !avpInitialized || vigilanceSoftwareInputs==null || !vigilanceSoftwareInputs.Checked)) return;
        if (mouse) vigilanceMouseHeld[index]=held; else vigilanceKeyHeld[index]=held;
        RefreshVigilanceControls();
    }
    void ClearVigilanceButton(int index) {
        VigilanceThreadGuard(); vigilanceMouseHeld[index]=false; vigilanceKeyHeld[index]=false;
        RefreshVigilanceControls();
    }
    void ClearVigilanceHeld() {
        for (int n=0; n<3; n++) { vigilanceMouseHeld[n]=false; vigilanceKeyHeld[n]=false; }
    }
    void BindVigilanceStatusReader(Func<string> reader) {
        VigilanceThreadGuard(); vigilanceStatusReader=reader; RefreshVigilanceControls();
    }
    void RefreshVigilanceControls() {
        VigilanceThreadGuard();
        if (vigilanceSoftwareInputs==null) return;
        bool ready=commReady && avpInitialized && !avpClosing;
        vigilanceSoftwareInputs.Enabled=ready;
        bool enabled=ready && vigilanceSoftwareInputs.Checked;
        if (!enabled) ClearVigilanceHeld();
        for (int n=0; n<3; n++) if (vigilanceButtons[n]!=null) vigilanceButtons[n].Enabled=enabled;
        string text=vigilanceInputFault.Length!=0 ? vigilanceInputFault :
            !enabled ? "Штатные входы РБ. Автоматического подтверждения бдительности нет." :
            "Ввод оператора: РБ="+(VigilanceHeld(0)?1:0)+" РБС="+(VigilanceHeld(1)?1:0)+" РБП="+(VigilanceHeld(2)?1:0);
        if (ready && vigilanceStatusReader!=null) {
            try { string actual=vigilanceStatusReader(); if (!String.IsNullOrEmpty(actual)) text+=". "+actual; }
            catch (Exception error) { text+=". Диагностика недоступна: "+error.Message; }
        }
        if (vigilanceInputStatus!=null) vigilanceInputStatus.Text=text;
    }
    bool VigilanceHeld(int index) { return vigilanceMouseHeld[index] || vigilanceKeyHeld[index]; }

    // Root calls Begin/End around the ENTIRE original ExecuteCommunication.
    // A callback-local Dispose must not release a held button before the later
    // native UpdateOutParam holding counter has sampled it.
    void BeginVigilanceElectricalInputs() {
        VigilanceThreadGuard();
        if (vigilanceElectricalFrame) throw new InvalidOperationException("Повторный электрический кадр ввода РБ");
        RestoreVigilanceLeases();
        vigilanceElectricalFrame=true;
    }
    void EndVigilanceElectricalInputs() {
        VigilanceThreadGuard(); vigilanceElectricalFrame=false;
        RestoreVigilanceLeases();
    }
    IDisposable BorrowVigilanceOperatorInputs() {
        VigilanceThreadGuard();
        bool enabled=commReady && avpInitialized && !avpClosing && vigilanceSoftwareInputs!=null && vigilanceSoftwareInputs.Checked;
        if (!enabled || !VigilanceHeld(0) && !VigilanceHeld(1) && !VigilanceHeld(2)) return VigilanceNoInputLease.Instance;
        // Retry failures between frames. During a frame, keep already borrowed
        // leases alive for the later native holding check.
        if (!vigilanceElectricalFrame) RestoreVigilanceLeases();
        IntPtr position=VigilancePositionPointer();
        var cells=new List<VigilanceInputCell>();
        for (int n=0; n<3; n++) if (VigilanceHeld(n)) {
            VigilanceRequireRange(IntPtr.Add(position,n),1,true);
            byte physical=Marshal.ReadByte(position,n);
            if (physical>1) throw new Exception("Неизвестная кодировка штатного входа РБ");
            // An already active physical request requires no software write.
            if (physical==0) cells.Add(new VigilanceInputCell(n,physical));
        }
        if (cells.Count==0) return VigilanceNoInputLease.Instance;
        return new VigilanceInputLease(this,position,cells);
    }
    void RestoreVigilanceOperatorInputs() {
        VigilanceThreadGuard(); ClearVigilanceHeld();
        if (vigilanceElectricalFrame) throw new InvalidOperationException("Завершите электрический кадр перед восстановлением РБ");
        RestoreVigilanceLeases();
    }
    void RestoreVigilanceLeases() {
        Exception failure=null;
        for (int n=vigilanceLeases.Count-1; n>=0; n--) {
            try { vigilanceLeases[n].RestoreNow(); }
            catch (Exception error) { if (failure==null) failure=error; }
        }
        if (failure!=null) throw failure;
    }

    void VigilanceThreadGuard() {
        if (IntPtr.Size!=4) throw new PlatformNotSupportedException("Ввод РБ требует x86");
        if (vigilanceInputThread==0 || VigilanceThreadId()!=vigilanceInputThread)
            throw new Exception("Ввод РБ требует штатный поток владельца");
    }
    IntPtr VigilancePositionPointer() {
        VigilanceThreadGuard();
        IntPtr owner,pult,pb,position;
        if (!commReady || !mem.TryGetValue("CommOwner",out owner) || !mem.TryGetValue("TPult",out pult) ||
            !mem.TryGetValue("TPBBtn",out pb) || !mem.TryGetValue("TPBBtnPos",out position))
            throw new Exception("Штатные входы РБ не инициализированы");
        IntPtr module=VigilanceModuleHandle("Communication1.dll");
        if (module==IntPtr.Zero || String.IsNullOrEmpty(root)) throw new Exception("Штатный модуль ввода РБ отсутствует");
        string expected=Path.GetFullPath(Path.Combine(root,@"RESOURCES\LOCOMOTIVES\1\Communication1.dll"));
        var path=new StringBuilder(32768);
        uint count=VigilanceModulePath(module,path,path.Capacity);
        if (count==0 || count>=path.Capacity || !String.Equals(Path.GetFullPath(path.ToString()),expected,StringComparison.OrdinalIgnoreCase))
            throw new Exception("Путь штатного модуля ввода РБ не совпадает");
        if (vigilanceCommunicationModule==IntPtr.Zero) {
            string hash;
            using (var sha=SHA256.Create()) using (var file=File.OpenRead(expected))
                hash=BitConverter.ToString(sha.ComputeHash(file)).Replace("-","");
            if (hash!=VigilanceCommunicationSha) throw new Exception("SHA штатного модуля ввода РБ не подтверждён");
            vigilanceCommunicationModule=module; vigilanceCommunicationPath=expected;
        }
        if (module!=vigilanceCommunicationModule || !String.Equals(expected,vigilanceCommunicationPath,StringComparison.OrdinalIgnoreCase))
            throw new Exception("Изменён владелец штатных входов РБ");
        IntPtr slotAddress=IntPtr.Add(module,0x2c7068);
        VigilanceRequireRange(slotAddress,4,false);
        IntPtr slot=Marshal.ReadIntPtr(slotAddress); VigilanceRequireRange(slot,4,false);
        if (Marshal.ReadIntPtr(slot)!=owner || pult!=IntPtr.Add(owner,0x24))
            throw new Exception("Владелец штатного пульта РБ не совпадает");
        VigilanceRequireRange(pult,4,false);
        IntPtr common=Marshal.ReadIntPtr(pult); VigilanceRequireRange(common,19,false);
        if (Marshal.ReadIntPtr(common,0xf)!=pb) throw new Exception("Штатный PB-пульт РБ не совпадает");
        VigilanceRequireRange(pb,4,false);
        if (Marshal.ReadIntPtr(pb)!=position) throw new Exception("Штатный PBBPos РБ не совпадает");
        VigilanceRequireRange(position,6,false);
        return position;
    }
    static void VigilanceRequireRange(IntPtr address,int count,bool write) {
        VigilanceMemoryInfo info;
        if (address==IntPtr.Zero || count<1 || VigilanceQuery(address,out info,(UIntPtr)Marshal.SizeOf(typeof(VigilanceMemoryInfo)))==UIntPtr.Zero)
            throw new Exception("Vigilance input memory-range guard");
        ulong first=unchecked((uint)address.ToInt32()), region=unchecked((uint)info.Base.ToInt32());
        uint protection=info.Protect&0xff;
        if (info.State!=0x1000 || (info.Protect&0x101)!=0 || first<region || first+(uint)count>region+info.RegionSize.ToUInt32())
            throw new Exception("Vigilance input memory-range guard");
        bool allowed=write ? protection==4 || protection==8 || protection==0x40 || protection==0x80 :
            protection==2 || protection==4 || protection==8 || protection==0x20 || protection==0x40 || protection==0x80;
        if (!allowed) throw new Exception("Vigilance input memory-protection guard");
    }
    sealed class VigilanceNoInputLease : IDisposable {
        internal static readonly VigilanceNoInputLease Instance=new VigilanceNoInputLease();
        public void Dispose() {}
    }
    sealed class VigilanceInputCell {
        internal readonly int Offset;
        internal readonly byte Original;
        internal bool Owned;
        internal VigilanceInputCell(int offset,byte original) {
            if (offset<0 || offset>2) throw new ArgumentOutOfRangeException("offset");
            Offset=offset; Original=original;
        }
    }
    sealed class VigilanceInputLease : IDisposable {
        readonly TrainerIntegrationHost owner;
        readonly IntPtr position;
        readonly List<VigilanceInputCell> cells;
        bool disposed;
        internal VigilanceInputLease(TrainerIntegrationHost host,IntPtr source,List<VigilanceInputCell> requests) {
            owner=host; position=source; cells=requests;
            // All source ranges/old values were checked before this first write.
            owner.vigilanceLeases.Add(this);
            try {
                foreach (var cell in cells) {
                    cell.Owned=true;
                    Marshal.WriteByte(position,cell.Offset,1);
                    if (Marshal.ReadByte(position,cell.Offset)!=1) throw new Exception("Vigilance input assignment verification failed");
                }
            } catch (Exception original) {
                try { RestoreNow(); }
                catch (Exception restore) { throw new AggregateException("Vigilance preparation/restoration failed",original,restore); }
                throw;
            }
        }
        public void Dispose() {
            if (disposed) return;
            owner.VigilanceThreadGuard();
            if (owner.vigilanceElectricalFrame) return; // End restores the retained lease.
            RestoreNow();
        }
        internal void RestoreNow() {
            if (disposed) return;
            owner.VigilanceThreadGuard();
            if (owner.VigilancePositionPointer()!=position) throw new Exception("Vigilance restoration source ownership guard");
            Exception failure=null;
            for (int n=cells.Count-1; n>=0; n--) {
                var cell=cells[n]; if (!cell.Owned) continue;
                try {
                    VigilanceRequireRange(IntPtr.Add(position,cell.Offset),1,true);
                    // Preserve consumed requests or a different physical/native value.
                    if (Marshal.ReadByte(position,cell.Offset)==1) {
                        Marshal.WriteByte(position,cell.Offset,cell.Original);
                        if (Marshal.ReadByte(position,cell.Offset)!=cell.Original) throw new Exception("Vigilance input restoration verification failed");
                    }
                    cell.Owned=false;
                } catch (Exception error) { if (failure==null) failure=error; }
            }
            if (failure!=null) throw failure;
            owner.vigilanceLeases.Remove(this); disposed=true;
        }
    }
}
