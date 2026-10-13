using System;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;

// Explicit alternative operator input for the separately loaded SAUT module.
// Only PB input bytes 5, 6 and 13 are borrowed. No SAUT/safety outputs are assigned.
partial class TrainerIntegrationHost {
    ComboBox standaloneSautChoice;
    Button standaloneSautK20Button;
    Label standaloneSautInputStatus;
    uint standaloneSautInputThread;
    int standaloneSautK20Cycles;
    bool standaloneSautK20ReleaseCycle;
    Func<int> standaloneSautNativeMode;
    readonly List<StandaloneSautInputScope> standaloneSautScopes = new List<StandaloneSautInputScope>();

    [DllImport("kernel32.dll", EntryPoint="GetCurrentThreadId")]
    static extern uint SautInputThreadId();
    [StructLayout(LayoutKind.Sequential)] struct SautInputMemoryInfo {
        public IntPtr Base, AllocationBase;
        public uint AllocationProtect;
        public UIntPtr RegionSize;
        public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll", EntryPoint="VirtualQuery")]
    static extern UIntPtr SautInputVirtualQuery(IntPtr address, out SautInputMemoryInfo info, UIntPtr size);

    void InstallStandaloneSautControls() {
        if (standaloneSautChoice != null) throw new InvalidOperationException("Ввод САУТ уже установлен");
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Ввод САУТ требует x86");
        standaloneSautInputThread = SautInputThreadId();
        // AVP feedback ends at y478 in InstallAutovedenieControls.
        ClientSize = new Size(Math.Max(800, ClientSize.Width), Math.Max(570, ClientSize.Height));
        Controls.Add(new Label { Text="Ввод САУТ", Left=10, Top=500, Width=125, Height=25 });
        standaloneSautChoice = new ComboBox {
            Left=140, Top=495, Width=445, DropDownStyle=ComboBoxStyle.DropDownList
        };
        standaloneSautChoice.Items.AddRange(new object[] {
            "Штатный ввод", "Программная САУТ — включена", "Питание САУТ; команды выключены", "Выключен"
        });
        // Install the event after selecting the default: startup sends no command.
        standaloneSautChoice.SelectedIndex = 0;
        standaloneSautChoice.SelectedIndexChanged += StandaloneSautSelectionChanged;
        Controls.Add(standaloneSautChoice);
        standaloneSautK20Button = new Button {
            Text="К20", Left=600, Top=493, Width=175, Height=32, Enabled=false
        };
        standaloneSautK20Button.Click += delegate { RequestStandaloneSautK20(); };
        Controls.Add(standaloneSautK20Button);
        standaloneSautInputStatus = new Label {
            Left=10, Top=534, Width=775, Height=32,
            Text="Штатные входы. К20: только на стоянке при К; для КЖ нужен источник расстояния."
        };
        Controls.Add(standaloneSautInputStatus);
        var area=Screen.FromPoint(Location).WorkingArea;
        Location=new Point(Location.X,Math.Max(area.Top,Math.Min(Location.Y,area.Bottom-Height)));
    }

    void StandaloneSautSelectionChanged(object sender, EventArgs e) {
        StandaloneSautThreadGuard();
        if (standaloneSautK20Cycles > 0) standaloneSautK20ReleaseCycle=true;
        standaloneSautK20Cycles = 0;
        CancelAutovedenie("Изменён ввод САУТ; снова нажмите 5 для автоведения");
        // Attachment is authorized by an explicit operator On selection only.
        if (avpInitialized && (standaloneSautChoice.SelectedIndex == 1 || standaloneSautChoice.SelectedIndex == 2))
            EnsureAutovedenieNativeBridge();
        RefreshStandaloneSautControls();
        Log("Standalone SAUT operator selection="+standaloneSautChoice.Text);
    }

    // Supply this reader ONLY from the separately initialized, SHA/owner-guarded
    // native SAUT binding. The actual K20 consumer accepts native modes 3 or 4.
    void BindStandaloneSautK20ModeReader(Func<int> reader) {
        StandaloneSautThreadGuard();
        if (reader == null) throw new ArgumentNullException("reader");
        standaloneSautNativeMode = reader;
        RefreshStandaloneSautControls();
    }
    bool StandaloneSautK20Available() {
        if (!avpInitialized || !commReady || standaloneSautNativeMode == null ||
            standaloneSautChoice == null || standaloneSautChoice.SelectedIndex == 3) return false;
        int nativeMode=standaloneSautNativeMode();
        // RE §4.5.9: single K20 only at standstill; KZH also needs Sost<300.
        // No confirmed remaining-distance source is connected for KZH.
        int rawCode=Marshal.ReadByte(mem["TInputParamPult"],0x1e);
        int currentCode=rawCode<10?rawCode:rawCode/10;
        return nativeMode == 4 && currentCode==4 && physicsReady && Math.Abs(signedVelocity)<.01 && FloatAt(mem["TInputParamPult"],1)<.036;
    }
    void RefreshStandaloneSautControls() {
        bool available=false;
        try { available=StandaloneSautK20Available(); }
        catch (Exception error) { Log("SAUT K20 mode unavailable: "+error.Message); }
        if (standaloneSautK20Button != null) standaloneSautK20Button.Enabled=available && standaloneSautK20Cycles == 0 && !standaloneSautK20ReleaseCycle;
        if (standaloneSautInputStatus != null) {
            standaloneSautInputStatus.Text=standaloneSautChoice.SelectedIndex == 0
                ? "Штатные входы; программный выбор режима не применяется."
                : "Режим выбран оператором. К20 "+(available ? "доступна." : "недоступна в текущем штатном режиме.");
        }
    }
    void RequestStandaloneSautK20() {
        StandaloneSautThreadGuard();
        if (!StandaloneSautK20Available()) throw new InvalidOperationException("К20 не подтверждена для текущего режима САУТ");
        if (standaloneSautK20Cycles > 0 || standaloneSautK20ReleaseCycle) throw new InvalidOperationException("Дождитесь отпускания кнопки К20");
        CancelAutovedenie("Нажата К20; снова нажмите 5 для автоведения");
        EnsureAutovedenieNativeBridge();
        standaloneSautK20Cycles=2; standaloneSautK20ReleaseCycle=false;
        RefreshStandaloneSautControls();
        Log("Standalone SAUT operator K20 requested for two native SAUT cycles");
    }
    int PendingStandaloneSautK20Cycles {
        get { StandaloneSautThreadGuard(); return standaloneSautK20Cycles; }
    }
    bool StandaloneSautOperatorSelected {
        get {
            StandaloneSautThreadGuard();
            return standaloneSautChoice.SelectedIndex == 1 || standaloneSautChoice.SelectedIndex == 2;
        }
    }

    // Root surrounds exactly one original native SAUT step with this scope.
    // forceOff borrows PB6=0 for a native exit; all old input values are restored.
    IDisposable BorrowStandaloneSautOperatorInputs(bool forceOff=false) {
        StandaloneSautThreadGuard();
        RestoreFailedStandaloneSautScopes();
        IntPtr pb=StandaloneSautInputPointer();
        int selection=standaloneSautChoice.SelectedIndex;
        if (selection < 0 || selection > 3) throw new Exception("Неизвестный ввод САУТ");
        bool modeAllowsK20=!forceOff && StandaloneSautK20Available();
        if (!forceOff && standaloneSautK20Cycles>0 && !modeAllowsK20) {
            standaloneSautK20Cycles=0; standaloneSautK20ReleaseCycle=true;
            Log("Pending K20 cancelled: native mode no longer accepts it");
        }
        bool pulse=!forceOff && standaloneSautK20Cycles > 0 && modeAllowsK20;
        bool releaseK20=!forceOff && standaloneSautK20ReleaseCycle;
        var requests=new List<StandaloneSautCell>();
        if (forceOff) requests.Add(new StandaloneSautCell(6,0));
        else {
            // Native Saut_Dll 1ACC88 reads PB5==1; PB5==0 clears its cut/brake outputs.
            if (selection == 1) { requests.Add(new StandaloneSautCell(5,1)); requests.Add(new StandaloneSautCell(6,1)); }
            else if (selection == 2) { requests.Add(new StandaloneSautCell(5,0)); requests.Add(new StandaloneSautCell(6,1)); }
            else if (selection == 3) requests.Add(new StandaloneSautCell(6,0));
            if (pulse) requests.Add(new StandaloneSautCell(13,1));
            else if (releaseK20) requests.Add(new StandaloneSautCell(13,0));
        }
        // In default mode, with no explicit K20 request, requests stays empty.
        return new StandaloneSautInputScope(this,pb,requests,pulse,releaseK20);
    }
    IDisposable BorrowStandaloneSautModeInputs() {
        StandaloneSautThreadGuard();RestoreFailedStandaloneSautScopes();
        var requests=new System.Collections.Generic.List<StandaloneSautCell>();
        int selection=standaloneSautChoice.SelectedIndex;
        if(selection==1){requests.Add(new StandaloneSautCell(5,1));requests.Add(new StandaloneSautCell(6,1));}
        else if(selection==2){requests.Add(new StandaloneSautCell(5,0));requests.Add(new StandaloneSautCell(6,1));}
        else if(selection==3)requests.Add(new StandaloneSautCell(6,0));
        else if(selection!=0)throw new Exception("Unknown SAUT mode source");
        // Mode is a common operator input. K20 remains a single SAUT-only pulse.
        return new StandaloneSautInputScope(this,StandaloneSautInputPointer(),requests,false,false);
    }
    void RestoreFailedStandaloneSautScopes() {
        for (int n=standaloneSautScopes.Count-1;n>=0;n--)
            if (standaloneSautScopes[n].RestorationPending) standaloneSautScopes[n].Dispose();
    }
    void RestoreStandaloneSautOperatorInputs() {
        StandaloneSautThreadGuard();
        for (int n=standaloneSautScopes.Count-1;n>=0;n--) standaloneSautScopes[n].Dispose();
        standaloneSautK20Cycles=0; standaloneSautK20ReleaseCycle=false;
    }

    void StandaloneSautThreadGuard() {
        if (IntPtr.Size != 4) throw new PlatformNotSupportedException("Ввод САУТ требует x86");
        if (standaloneSautInputThread == 0 || SautInputThreadId() != standaloneSautInputThread)
            throw new Exception("Ввод САУТ должен выполняться на native потоке владельца");
    }
    IntPtr StandaloneSautInputPointer() {
        StandaloneSautThreadGuard();
        IntPtr pb,pult;
        if (!commReady || !mem.TryGetValue("TPBBtn",out pb) || !mem.TryGetValue("TPult",out pult))
            throw new Exception("Штатные входы САУТ не инициализированы");
        SautRequireRange(pult,4,false);
        IntPtr common=Marshal.ReadIntPtr(pult);
        SautRequireRange(common,19,false);
        if (pb == IntPtr.Zero || Marshal.ReadIntPtr(common,15) != pb)
            throw new Exception("Вход САУТ не принадлежит штатному пульту");
        SautRequireRange(pb,26,false);
        return pb;
    }
    static void SautRequireRange(IntPtr address,int count,bool write) {
        SautInputMemoryInfo info;
        if (address == IntPtr.Zero || count < 1 || SautInputVirtualQuery(address,out info,(UIntPtr)Marshal.SizeOf(typeof(SautInputMemoryInfo))) == UIntPtr.Zero)
            throw new Exception("SAUT input memory-range guard");
        ulong first=unchecked((uint)address.ToInt32()), start=unchecked((uint)info.Base.ToInt32());
        uint protect=info.Protect & 0xff;
        if (info.State != 0x1000 || (info.Protect & 0x101) != 0 || first < start || first+(uint)count > start+info.RegionSize.ToUInt32())
            throw new Exception("SAUT input memory-range guard");
        bool allowed=write
            ? protect == 4 || protect == 8 || protect == 0x40 || protect == 0x80
            : protect == 2 || protect == 4 || protect == 8 || protect == 0x20 || protect == 0x40 || protect == 0x80;
        if (!allowed) throw new Exception("SAUT input memory-protection guard");
    }

    sealed class StandaloneSautCell {
        internal readonly int Offset;
        internal readonly byte Assigned;
        internal byte Original;
        internal bool Owned;
        internal StandaloneSautCell(int offset,byte value) {
            if (offset != 5 && offset != 6 && offset != 13) throw new ArgumentOutOfRangeException("offset");
            Offset=offset; Assigned=value;
        }
    }
    sealed class StandaloneSautInputScope : IDisposable {
        readonly TrainerIntegrationHost owner;
        readonly IntPtr pb;
        readonly List<StandaloneSautCell> cells;
        readonly bool k20Pulse,k20Release;
        bool disposed,borrowed;
        internal bool RestorationPending { get; private set; }
        internal StandaloneSautInputScope(TrainerIntegrationHost host,IntPtr pointer,List<StandaloneSautCell> requests,bool pulse,bool release) {
            owner=host; pb=pointer; cells=requests; k20Pulse=pulse; k20Release=release;
            // Save every old value and validate every range before the first write.
            foreach (var cell in cells) {
                SautRequireRange(IntPtr.Add(pb,cell.Offset),1,true);
                cell.Original=Marshal.ReadByte(pb,cell.Offset);
            }
            owner.standaloneSautScopes.Add(this);
            try {
                foreach (var cell in cells) {
                    cell.Owned=true;
                    Marshal.WriteByte(pb,cell.Offset,cell.Assigned);
                    if (Marshal.ReadByte(pb,cell.Offset) != cell.Assigned) throw new Exception("SAUT input assignment guard");
                }
                borrowed=true;
            } catch { Dispose(); throw; }
        }
        public void Dispose() {
            if (disposed) return;
            RestorationPending=true;
            owner.StandaloneSautThreadGuard();
            if (owner.StandaloneSautInputPointer() != pb) throw new Exception("SAUT input restoration owner guard");
            Exception failure=null;
            for (int n=cells.Count-1;n>=0;n--) {
                var cell=cells[n]; if (!cell.Owned) continue;
                try {
                    SautRequireRange(IntPtr.Add(pb,cell.Offset),1,true);
                    byte current=Marshal.ReadByte(pb,cell.Offset);
                    // Preserve a consumed input or a new physical operator value.
                    if (current == cell.Assigned) {
                        Marshal.WriteByte(pb,cell.Offset,cell.Original);
                        if (Marshal.ReadByte(pb,cell.Offset) != cell.Original) throw new Exception("SAUT input restoration verification failed");
                    }
                    cell.Owned=false;
                } catch (Exception error) { if (failure == null) failure=error; }
            }
            if (failure != null) throw failure;
            if (borrowed && k20Pulse && owner.standaloneSautK20Cycles > 0) {
                owner.standaloneSautK20Cycles--;
                if (owner.standaloneSautK20Cycles == 0) owner.standaloneSautK20ReleaseCycle=true;
            }
            if (borrowed && k20Release) owner.standaloneSautK20ReleaseCycle=false;
            owner.standaloneSautScopes.Remove(this);
            disposed=true;
            owner.RefreshStandaloneSautControls();
        }
    }
}
