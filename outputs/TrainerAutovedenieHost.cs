using System;
using System.Collections.Generic;
using System.Drawing;
using System.Globalization;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Educational adaptation: the planner supplies native operator requests only.
// PDF restrictions are route data; they do not grant live signalling authority.
partial class TrainerIntegrationHost {
    TrainerAutovedenieInterface avpUi;
    TrainerAutovedenieNativeBridge avpNative;
    TrainerAutovedenieController avpController;
    TrainerAvpDecision avpDecision;
    ComboBox monitorCombo;
    CheckBox avpMovementPermission, avpSoftwareTd, avpTd1, avpTd2;
    NumericUpDown avpTargetLimit;
    Label avpStatus;
    bool avpInitialized, avpClosing;
    int avpCommitVersion, avpRouteGeneration;
    long avpCycle, avpToggleCycle = -1;
    bool avpEditorOpen;
    string avpSettingsSignature = "", avpUnavailable = "Инициализация", avpLastStatus = "";
    RouteModel avpRoute;
    double avpReferenceMass, avpReferenceLength;
    int avpReferenceWagons;
    readonly Dictionary<string, IntPtr> avpCalibrationModules = new Dictionary<string, IntPtr>();
    readonly List<AutovedenieTdScope> avpTdScopes = new List<AutovedenieTdScope>();
    Func<double>[] avpBogiePressures;

    const string AvpCalibrationProofSha = "F42F304E42E3169ADC100183A01252D5FDFB79A380927869E023A5F5FFE4B90B";
    static readonly string[] AvpCalibrationNames = { "Pult1.dll", "Communication1.dll", "Tyga1.dll", "TormModel.dll", "POEZD.DLL" };
    static readonly string[] AvpCalibrationKeys = { "pult_sha256", "communication_sha256", "tyga_sha256", "tormmodel_sha256", "poezd_sha256" };
    static readonly string[] AvpCalibrationHashes = {
        "7DAC59BF94276309E012A6EB72602C08A921FE1910A4169F9F0EC27008F0F8F5",
        "27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F",
        "8EC6A82E618A1D75CBB31242784084B530BB87308E601DEFC5263D5279270491",
        "F871640C5B7699901E3AFBF755BB2B797B659524C29BAA046B311CA6ECC875B9",
        "822B6D17CE45F80179A512EE6C70A35D5AE29BF187530BDA5EBD11446CFB3553"
    };
    [DllImport("kernel32.dll", EntryPoint="GetModuleHandleW", CharSet=CharSet.Unicode)]
    static extern IntPtr AvpGetModuleHandle(string name);
    [DllImport("kernel32.dll", EntryPoint="GetModuleFileNameW", CharSet=CharSet.Unicode, SetLastError=true)]
    static extern uint AvpGetModuleFileName(IntPtr module, StringBuilder name, int capacity);
    [StructLayout(LayoutKind.Sequential)] struct AvpMemoryInfo {
        public IntPtr Base, AllocationBase; public uint AllocationProtect;
        public UIntPtr RegionSize; public uint State, Protect, Type;
    }
    [DllImport("kernel32.dll", EntryPoint="VirtualQuery")]
    static extern UIntPtr AvpVirtualQuery(IntPtr address, out AvpMemoryInfo info, UIntPtr size);

    void InstallAutovedenieControls() {
        ClientSize = new Size(Math.Max(800, ClientSize.Width), Math.Max(490, ClientSize.Height));
        var workingArea=Screen.FromPoint(Location).WorkingArea;Location=new Point(Location.X,Math.Max(workingArea.Top,Math.Min(Location.Y,workingArea.Bottom-Height)));
        Controls.Add(new Label { Text="МПСУиД и автоведение — учебная адаптация алгоритма", Left=10, Top=228, Width=775, Height=24 });
        Controls.Add(new Label { Text="Монитор", Left=10, Top=258, Width=75 });
        monitorCombo = new ComboBox { Left=85, Top=254, Width=145, DropDownStyle=ComboBoxStyle.DropDownList };
        monitorCombo.Items.AddRange(new object[] { "Левый", "Правый" }); monitorCombo.SelectedIndex=0; Controls.Add(monitorCombo);
        AddAutovedenieKey("0", 16, 245, 70); AddAutovedenieKey("Enter", 1, 320, 100);
        AddAutovedenieKey("Удалить", 2, 425, 110); AddAutovedenieKey("Вверх", 6, 540, 115); AddAutovedenieKey("Вниз", 5, 660, 115);
        avpSoftwareTd = new CheckBox { Text="Программный пульт ТЭД", Left=10, Top=294, Width=310, Checked=false };
        avpTd1 = new CheckBox { Text="ТЭД 1 включены", Left=335, Top=294, Width=205, Checked=false, Enabled=false };
        avpTd2 = new CheckBox { Text="ТЭД 2 включены", Left=555, Top=294, Width=220, Checked=false, Enabled=false };
        avpSoftwareTd.CheckedChanged += delegate {
            avpTd1.Enabled=avpTd2.Enabled=avpSoftwareTd.Checked;
            CancelAutovedenie("Изменён источник команд ТЭД; снова нажмите 5");
        };
        avpTd1.CheckedChanged += delegate { CancelAutovedenie("Изменено включение ТЭД 1; снова нажмите 5"); };
        avpTd2.CheckedChanged += delegate { CancelAutovedenie("Изменено включение ТЭД 2; снова нажмите 5"); };
        Controls.Add(avpSoftwareTd); Controls.Add(avpTd1); Controls.Add(avpTd2);
        avpMovementPermission = new CheckBox { Text="Учебное разрешение движения", Left=10, Top=328, Width=355, Checked=false };
        avpMovementPermission.CheckedChanged += delegate { CancelAutovedenie("Изменено учебное разрешение; снова нажмите 5"); };
        Controls.Add(avpMovementPermission);
        Controls.Add(new Label { Text="Заданная скорость, км/ч", Left=380, Top=330, Width=235 });
        avpTargetLimit = new NumericUpDown { Left=630, Top=325, Width=145, Minimum=1, Maximum=120, Value=80 };
        avpTargetLimit.ValueChanged += delegate { CancelAutovedenie("Изменена заданная скорость; снова нажмите 5"); }; Controls.Add(avpTargetLimit);
        avpStatus = new Label { Left=10, Top=366, Width=775, Height=112, Text="Автоведение: инициализация. Разрешение движения отключено." };
        Controls.Add(avpStatus);
    }
    void AddAutovedenieKey(string text, byte key, int left, int width) {
        var button=new Button { Text=text, Left=left, Top=252, Width=width, Height=32 };
        button.Click += delegate { SendMpsudKey(key); }; Controls.Add(button);
    }
    // Both native monitors share one settings record: a software key has one source.
    void SendMpsudKey(byte key) {
        if (key>16) throw new ArgumentOutOfRangeException("key");
        if (!commReady || avpClosing || !mem.ContainsKey("TPult")) return;
        int side=monitorCombo==null || monitorCombo.SelectedIndex!=1 ? 0 : 1;
        B(mem["TPult"],0x53+side,key); Log("MSUL operator key="+key+" monitor="+(side+1));
    }

    void InitializeAutovedenie() {
        if (avpInitialized) throw new InvalidOperationException("Автоведение уже инициализировано");
        avpInitialized=true; avpUnavailable=""; avpRoute=route; avpRouteGeneration=routeGeneration;
        avpDecision=AutovedenieIdle("Советчик");
        try {
            avpUi=TrainerAutovedenieInterface.Attach(mem["TPult"],Log);
            var settings=avpUi.ReadSettings(); avpCommitVersion=settings.CommitVersion;
            avpEditorOpen=settings.EditorOpen; avpSettingsSignature=AutovedenieSettingsSignature(settings);
        } catch (Exception error) { avpUnavailable="Интерфейс МПСУиД не подключён: "+error.Message; Log(avpUnavailable); }
        try {
            double coefficient=LoadAutovedenieCalibration();
            string model=AutovedenieModelReason(); if (model.Length!=0) throw new Exception(model);
            InitializeAutovedenieBrakeSource();
            avpController=new TrainerAutovedenieController(route,coefficient,avpReferenceMass);
        } catch (Exception error) {
            if (avpUnavailable.Length!=0) avpUnavailable+="; ";
            avpUnavailable+="Калибровка автоведения недоступна: "+error.Message; Log(avpUnavailable);
        }
        if (avpUnavailable.Length!=0) avpDecision=AutovedenieIdle(avpUnavailable);
        PublishAutovedenie();
        Log("AVP host initialized: educational adaptation, native bridge lazy, permission default off, ordinary mode only");
    }
    static TrainerAvpDecision AutovedenieIdle(string reason) {
        return new TrainerAvpDecision { State=TrainerAvpState.Advisor, Reason=reason,
            CurrentLimitKmh=-1, TargetSpeedKmh=-1, PreviewLimitKmh=-1, PreviewDistanceM=-1,
            SuggestedBrakePosition=2, DriveEnabled=false };
    }

    void BeforeAutovedenieCycle() {
        if (!avpInitialized || avpClosing) return;
        avpCycle++;
        try {
            if (avpNative==null && StandaloneSautSafetyReason().Length!=0) EnsureAutovedenieNativeBridge();
            var settings=ReadAutovedenieSettings(); CheckAutovedenieContext(settings);
            ProcessAutovedenieToggle(settings);
            var input=ReadAutovedenieInput(settings);
            if (avpController!=null) {
                bool owned=AutovedenieOwnsController() || avpDecision!=null && avpDecision.DriveEnabled;
                var next=avpController.Step(input);
                // Step can cancel ownership itself. Release the prior native
                // traction before replacing the decision that proves ownership.
                if (owned && !next.DriveEnabled) CancelAutovedenie(next.Reason);
                avpDecision=next;
            }
            else avpDecision=AutovedenieIdle(avpUnavailable);
            if (avpNative!=null) avpNative.SubmitDecision(avpDecision);
            PublishAutovedenie();
        } catch (Exception error) { AutovedenieHostFault(error); }
    }
    void AfterAutovedenieElectricalCycle() {
        if (!avpInitialized || avpClosing) return;
        try {
            var settings=ReadAutovedenieSettings(); CheckAutovedenieContext(settings);
            // Native key callbacks arrive during ExecuteCommunication. Do not run
            // Step a second time: a second evaluation would consume a +1 pulse.
            ProcessAutovedenieToggle(settings);
            var input=ReadAutovedenieInput(settings); string gate=AutovedenieActiveGate(input);
            if (gate.Length!=0) CancelAutovedenie(gate);
            if (avpNative!=null) avpNative.SubmitDecision(avpDecision);
            PublishAutovedenie();
        } catch (Exception error) { AutovedenieHostFault(error); }
    }
    TrainerAutovedenieSettings ReadAutovedenieSettings() {
        return avpUi==null ? new TrainerAutovedenieSettings { Valid=false, Reason=avpUnavailable } : avpUi.ReadSettings();
    }
    static string AutovedenieSettingsSignature(TrainerAutovedenieSettings s) {
        return s.Mode+":"+s.Intensity+":"+s.TrainNumber+":"+s.MassT+":"+s.Wagons+":"+s.Empty;
    }
    void CheckAutovedenieContext(TrainerAutovedenieSettings settings) {
        string signature=AutovedenieSettingsSignature(settings);
        if (avpRoute!=route) {
            avpUnavailable="Маршрут перезагружен; требуется новая инициализация автоведения";
            CancelAutovedenie(avpUnavailable);
        } else if (avpRouteGeneration!=routeGeneration)
            CancelAutovedenie("Изменён маршрут; снова нажмите 5");
        else if (settings.EditorOpen && !avpEditorOpen || settings.CommitVersion!=avpCommitVersion || signature!=avpSettingsSignature)
            CancelAutovedenie("Изменены данные поезда; завершите ввод и снова нажмите 5");
        avpRouteGeneration=routeGeneration; avpEditorOpen=settings.EditorOpen;
        avpCommitVersion=settings.CommitVersion; avpSettingsSignature=signature;
    }
    void ProcessAutovedenieToggle(TrainerAutovedenieSettings settings) {
        if (avpUi==null) return; bool requested=false;
        while (avpUi.DrainToggle()) requested=true;
        if (!requested || avpToggleCycle==avpCycle) return;
        avpToggleCycle=avpCycle; // A paired key from both monitors is one action.
        if (avpController==null || avpUnavailable.Length!=0) { avpDecision=AutovedenieIdle(avpUnavailable); return; }
        if (avpController.State==TrainerAvpState.Automatic || avpController.State==TrainerAvpState.Finished) {
            if(avpNative!=null)avpNative.ReleaseCommands();avpController.RequestAdvisor(); avpDecision.DriveEnabled=false;
            avpDecision.State=TrainerAvpState.Advisor; avpDecision.Reason="Советчик";
            Log("AVP explicit request: advisor"); return;
        }
        // Only an explicit native key 5 may attach the provider or request entry.
        try { EnsureAutovedenieNativeBridge(); }
        catch (Exception error) { avpUnavailable="Штатный мост АВП недоступен: "+error.Message; avpDecision=AutovedenieIdle(avpUnavailable); Log(avpUnavailable); return; }
        var input=ReadAutovedenieInput(settings); avpController.RequestAutomatic(input);
        avpDecision.State=avpController.State;
        avpDecision.DriveEnabled=avpController.State==TrainerAvpState.Automatic;
        avpDecision.SuggestedTractionCommand=0; avpDecision.SuggestedBrakePosition=2;
        avpDecision.Reason=avpDecision.DriveEnabled ? "Автоведение" : AutovedenieActiveGate(input);
        if (String.IsNullOrEmpty(avpDecision.Reason)) avpDecision.Reason=settings.Reason;
        Log("AVP explicit request: "+avpDecision.State+"; "+avpDecision.Reason);
    }
    void EnsureAutovedenieNativeBridge() {
        if (avpNative!=null) return;
        avpNative=TrainerAutovedenieNativeBridge.Attach(mem["CommOwner"],mem["TPult"],mem["TLocParam"],CancelAutovedenie);
        avpNative.SetOperatorInputSource(AutovedenieOperatorInputScope);
        avpNative.SetAdditionalSafetySource(StandaloneSautSafetyReason);
    }
    TrainerAvpInput ReadAutovedenieInput(TrainerAutovedenieSettings settings) {
        string model=AutovedenieModelReason(), manual=AutovedenieManualReason(), safety=AutovedenieSafetyReason();
        string ready=avpUnavailable.Length!=0 ? avpUnavailable : model;
        if (ready.Length==0 && (Marshal.ReadByte(mem["TLocParam"],0x90)!=1 || Marshal.ReadByte(mem["TLocParam"],0x91)!=1)) ready="Не подтверждён штатный путь крана машиниста";
        if (ready.Length==0 && !avpMovementPermission.Checked) ready="Нет учебного разрешения движения";
        if (ready.Length==0 && electricalFault.Length!=0) ready="Сбой штатной тяговой DLL: "+electricalFault;
        if (ready.Length==0 && avpNative==null) ready="Для запроса автоведения нажмите 5 на экране машиниста";
        if (ready.Length==0) ready=avpNative.ReadyReason;
        // Native longitudinal transients can move the head against stationary
        // braked wagons. Detect reverse movement from the measured consist;
        // retain the head sensor for speed control and all native outputs.
        if (ready.Length==0 && signedVelocity<-(.3/3.6) && ReadAutovedenieConsistVelocity()<-(.3/3.6))
            ready="Движение назад не поддержано автоведением";
        bool brakesReleased, automaticBrakesReleased; string brakeState=ReadAutovedenieBrakeState(out brakesReleased,out automaticBrakesReleased);
        if (ready.Length==0 && brakeState.Length!=0) ready=brakeState;
        return new TrainerAvpInput {
            X=position, SpeedKmh=FloatAt(mem["TInputParamPult"],1), TrainLengthM=avpReferenceLength,
            TrainMassT=AutovedeniePhysicalMass(), Seconds=ticks*.1,
            OperationalLimitKmh=avpTargetLimit==null ? 80 : (double)avpTargetLimit.Value,
            TractionPosition=Marshal.ReadInt32(mem["TTygaParam"],0xc), Ready=ready.Length==0,
            SettingsValid=settings.Valid && avpUi!=null && avpUi.Fault.Length==0,
            NotReadyReason=ready, ManualOverride=manual.Length!=0, SafetyIntervention=safety.Length!=0,
            BrakesReleased=brakesReleased, AutomaticBrakesReleased=automaticBrakesReleased, CanApplyTraction=Marshal.ReadByte(mem["TLocParam"],0x11)==0,
            HasBrakeFeedback=true, EqualizingReservoirPressure=FloatAt(mem["TLocParam"],9),
            Operation=(TrainerAvpOperation)settings.Mode,
            HasSignalPreview=false, SignalStopX=-1, SignalPermittedSpeedKmh=-1
        };
    }
    // Resolve the candidate's fixed two-section shape once. Getter delegates read
    // the same four physical reservoirs that supply locomotive brake force.
    // Integration76 has no candidate fields and fails its model guard before here.
    void InitializeAutovedenieBrakeSource() {
        var member=typeof(TrainerIntegrationHost).GetField("airCircuit",System.Reflection.BindingFlags.Instance|System.Reflection.BindingFlags.NonPublic);
        object circuit=member==null ? null : member.GetValue(this);
        if (circuit==null || circuit.GetType().FullName!="TrainerAir.TwoSectionAirChain") throw new Exception("Нет подтверждённого контура тормозов двух секций");
        var sectionField=circuit.GetType().GetField("Sections");
        var sections=sectionField==null ? null : sectionField.GetValue(circuit) as Array;
        if (sections==null || sections.Length!=2) throw new Exception("Не подтверждены две секции тормозов");
        var readers=new Func<double>[4];
        for (int section=0;section<2;section++) {
            object model=sections.GetValue(section);
            if (model==null || model.GetType().FullName!="TrainerAir.BtoSection") throw new Exception("Не подтверждён БТО секции");
            var cylinderField=model.GetType().GetField("Cylinder");
            var cylinders=cylinderField==null ? null : cylinderField.GetValue(model) as Array;
            if (cylinders==null || cylinders.Length!=2) throw new Exception("Не подтверждены цилиндры обеих тележек");
            for (int bogie=0;bogie<2;bogie++) {
                object reservoir=cylinders.GetValue(bogie);
                if (reservoir==null || reservoir.GetType().FullName!="TrainerAir.Reservoir") throw new Exception("Нет физического давления тележки");
                var gauge=reservoir.GetType().GetProperty("Gauge");
                if (gauge==null || gauge.PropertyType!=typeof(double) || !gauge.CanRead) throw new Exception("Не подтверждён источник давления тележки");
                readers[section*2+bogie]=(Func<double>)Delegate.CreateDelegate(typeof(Func<double>),reservoir,gauge.GetGetMethod());
            }
        }
        avpBogiePressures=readers;
    }
    string ReadAutovedenieBrakeState(out bool released,out bool automaticReleased) {
        released=false; automaticReleased=false;
        if (avpBogiePressures==null || avpBogiePressures.Length!=4 || trainData==IntPtr.Zero || Marshal.ReadInt32(trainData)!=77 || cars==null || cars.Length!=76)
            return "Нет подтверждённых давлений тормозов двух секций";
        var array=Marshal.ReadIntPtr(trainData,0x42); var second=array==IntPtr.Zero ? IntPtr.Zero : Marshal.ReadIntPtr(array,12);
        if (second==IntPtr.Zero) return "Нет второго пневмоэлемента электровоза";
        // Own service braking is a normal waiting state, not loss of readiness.
        // A tiny negative roundoff is tolerated; no pressure is clamped or written.
        var pressures=new double[] { FloatAt(cars[0],4), FloatAt(second,4), FloatAt(cars[cars.Length-1],4), FloatAt(mem["TLocParam"],13),
            avpBogiePressures[0](),avpBogiePressures[1](),avpBogiePressures[2](),avpBogiePressures[3]() };
        bool allReleased=true;
        foreach (double pressure in pressures) {
            if (!AvpFinite(pressure) || pressure<-.001) return "Недопустимые давления тормозных цилиндров";
            if (pressure>.1) allReleased=false;
        }
        // Intermediate wagons can retain pressure after the head and tail
        // gauges clear. Wait for the entire measured consist before traction.
        foreach (var car in cars) {
            double pressure=FloatAt(car,4);
            if (!AvpFinite(pressure) || pressure<-.001) return "Недопустимое давление цилиндра вагона";
            if (pressure>.1) allReleased=false;
        }
        automaticReleased=true;
        if (FloatAt(second,4)>.1) automaticReleased=false;
        foreach (var car in cars) if (FloatAt(car,4)>.1) automaticReleased=false;
        released=allReleased; return "";
    }
    double ReadAutovedenieConsistVelocity() {
        if (readTrain==null || cars==null || cars.Length!=76 || avpReferenceMass!=6200) throw new Exception("Нет подтверждённой скорости состава");
        double momentum=0;
        for (int n=0;n<76;n++) {
            float x,v,coupler,a,deformation; readTrain(out x,out v,out coupler,out a,out deformation,n);
            if (!Finite(v)) throw new Exception("Недопустимая native скорость вагона");
            momentum+=v*(n==0?200:80);
        }
        return momentum/6200;
    }
    string AutovedenieActiveGate(TrainerAvpInput input) {
        if (!AvpFinite(input.X) || !AvpFinite(input.SpeedKmh) || input.SpeedKmh<0 || !AvpFinite(input.Seconds) ||
            !AvpFinite(input.TrainLengthM) || input.TrainLengthM<=0 || !AvpFinite(input.TrainMassT) || input.TrainMassT<=0 ||
            !AvpFinite(input.OperationalLimitKmh) || input.OperationalLimitKmh<=0 || input.OperationalLimitKmh>120)
            return "Недопустимые данные движения";
        if (input.SafetyIntervention) return AutovedenieSafetyReason();
        if (input.ManualOverride) return AutovedenieManualReason();
        if (!input.SettingsValid) return avpUi==null ? "Нет интерфейса МПСУиД" : avpUi.ReadSettings().Reason;
        if (!input.Ready) return input.NotReadyReason;
        if (!input.CanApplyTraction && !AutovedenieOwnsController()) return "Сигнал ТМ: включение тяги запрещено";
        if (input.Operation==TrainerAvpOperation.SignalFollowing) return "Нет подтверждённого живого сигнального задания для режима по удалению";
        if (input.Operation==TrainerAvpOperation.CatchUp) return "Алгоритм режима нагон не подтверждён документацией";
        if (input.Operation!=TrainerAvpOperation.Ordinary) return "Неизвестный режим автоведения";
        if (input.X-input.TrainLengthM<-.01 || route==null || input.X>route.Length+.01) return "Состав вне подтверждённого маршрута";
        return "";
    }
    string AutovedenieManualReason() {
        var p=mem["TPult"]; var loc=mem["TLocParam"];
        if (commandedKm.HasValue || fileKm.HasValue || safetyPower || momentaryCharge)
            return "Активны ручные проверочные команды";
        if (Marshal.ReadByte(loc,0x7d)!=2) return "Ручное управление краном машиниста";
        if (Marshal.ReadByte(loc,0x7e)!=2) return "Ручное управление КВТ";
        if (Marshal.ReadInt32(p,0x69)!=0 || Marshal.ReadInt32(p,0x65)!=0 || Marshal.ReadByte(p,0x26)!=0)
            return "Ручное управление тягой";
        return "";
    }
    string StandaloneSautSafetyReason() {
        if (standaloneSautBinding==null) return "";
        try {
            if (standaloneSautBinding.BrakeRequested || standaloneSautBinding.LapRequested || standaloneSautBinding.TractionCutRequested) return "Команда программной САУТ";
            return "";
        } catch (Exception error) { return "Источник программной САУТ недоступен: "+error.Message; }
    }
    string AutovedenieSafetyReason() {
        var p=mem["TPult"]; var loc=mem["TLocParam"];
        if (Marshal.ReadByte(p,0x23)!=0 || Marshal.ReadByte(p,0x24)!=0 || Marshal.ReadByte(loc,0x7d)>=6)
            return "Экстренное торможение";
        if (Marshal.ReadByte(loc,0x9b)!=0) return "Срыв ЭПК";
        if (Marshal.ReadByte(loc,0x9d)!=0) return "Срыв КОН";
        string standalone=StandaloneSautSafetyReason(); if (standalone.Length!=0) return standalone;
        if (Marshal.ReadByte(p,0xc6)!=0 || Marshal.ReadByte(loc,0x89)!=0 && (Marshal.ReadByte(loc,0x8a)!=0 || Marshal.ReadByte(loc,0x8b)!=0))
            return "Команда САУТ";
        return "";
    }
    bool AutovedenieOwnsController() {
        return avpController!=null && (avpController.State==TrainerAvpState.Automatic || avpController.State==TrainerAvpState.Finished);
    }
    // This callback can run INSIDE ProceedPult. Clear the decision immediately;
    // pneumatic arbitration later in the same cycle must see no AVP ownership.
    void CancelAutovedenie(string reason) {
        bool owned=AutovedenieOwnsController() || avpDecision!=null && avpDecision.DriveEnabled;
        if (avpDecision!=null) avpDecision.DriveEnabled=false;
        if (!owned) return;
        if(avpNative!=null && avpNative.Fault.Length==0 && AutovedenieManualReason().Length==0 && AutovedenieSafetyReason().Length==0)avpNative.ReleaseCommands();
        if (avpController!=null) avpController.Cancel(reason);
        avpDecision.State=TrainerAvpState.Cancelled; avpDecision.Reason=reason;
        avpDecision.SuggestedTractionCommand=0; avpDecision.SuggestedBrakePosition=2;
        Log("AVP cancelled: "+reason);
    }
    void AutovedenieHostFault(Exception error) {
        avpUnavailable="Ошибка интеграции автоведения: "+error.Message;
        CancelAutovedenie(avpUnavailable);
        if (avpDecision==null) avpDecision=AutovedenieIdle(avpUnavailable);
        avpDecision.DriveEnabled=false; avpDecision.Reason=avpUnavailable;
        if (avpNative!=null) try { avpNative.SubmitDecision(avpDecision); } catch (Exception nested) { Log("AVP disable failed: "+nested.Message); }
        Log(avpUnavailable); PublishAutovedenie();
    }
    // Root wires this BELOW native emergency/SAUT priority in PneumaticCycle,
    // and restores the physical KM byte after the scoped native pneumatic step.
    int? AutovedenieBrakeRequest() {
        if (!avpInitialized || avpClosing || avpDecision==null || !avpDecision.DriveEnabled ||
            !AutovedenieOwnsController() || avpNative==null || !avpNative.OwnsCommands) return null;
        try {
            var settings=ReadAutovedenieSettings(); CheckAutovedenieContext(settings);
            string gate=AutovedenieActiveGate(ReadAutovedenieInput(settings));
            if (gate.Length!=0) { CancelAutovedenie(gate); return null; }
            if (!avpDecision.DriveEnabled) return null;
            int km=avpDecision.BrakePosition;
            if (km!=2 && km!=4 && km!=5) { CancelAutovedenie("Неподдерживаемая команда крана автоведения"); return null; }
            return km;
        } catch (Exception error) { AutovedenieHostFault(error); return null; }
    }
    int? AutovedenieKvtRequest() {
        if (!avpInitialized || avpClosing || avpDecision==null || !avpDecision.DriveEnabled || avpNative==null || !avpNative.OwnsCommands) return null;
        int kvt=avpDecision.KvtPosition;
        if (kvt!=2 && kvt!=3) { CancelAutovedenie("Неподдерживаемая команда независимого тормоза АВП"); return null; }
        return kvt;
    }
    void PublishAutovedenie() {
        if (avpDecision==null) return;
        if (avpUi!=null) {
            try { avpUi.BeforeCycle(avpDecision); }
            catch (Exception error) { CancelAutovedenie("Интерфейс АВ недоступен: "+error.Message); Log("AV UI feedback failed: "+error.Message); }
        }
        string reason=avpDecision.Reason ?? "";
        if (avpUi!=null && avpUi.Fault.Length!=0) { CancelAutovedenie(avpUi.Fault); reason=avpUi.Fault; }
        string mode=avpDecision.State==TrainerAvpState.Automatic ? "АВ" : avpDecision.State==TrainerAvpState.Finished ? "Остановка удерживается" : "С";
        string target=avpDecision.TargetSpeedKmh>=0 ? avpDecision.TargetSpeedKmh.ToString("F1",CultureInfo.InvariantCulture)+" км/ч" : "—";
        string text="Учебная адаптация: "+mode+". "+reason+"\r\nРасчётная скорость: "+target+". Фактическая тяга и скорость определяются штатной моделью.\r\nКлавиша 5 на экране машиниста: советчик / автоведение. PDF не содержит живого сигнального разрешения.";
        if (avpStatus!=null) avpStatus.Text=text;
        if (text!=avpLastStatus) { Log("AVP status: "+mode+"; "+reason); avpLastStatus=text; }
    }
    void CloseAutovedenie() {
        if (!avpInitialized) return; avpClosing=true;
        CancelAutovedenie("Закрытие тренажёра"); Exception failure=null;
        if (avpNative!=null) try { avpNative.SubmitDecision(avpDecision); RestoreAutovedenieOperatorScopes(); avpNative.Dispose(); avpNative=null; } catch (Exception error) { failure=error; }
        try { RestoreAutovedenieOperatorScopes(); } catch (Exception error) { if (failure==null) failure=error; }
        if (avpUi!=null) try { avpUi.Dispose(); avpUi=null; } catch (Exception error) { if (failure==null) failure=error; }
        if (failure!=null) { Log("AVP restoration failed: "+failure.Message); throw failure; }
        avpBogiePressures=null; avpInitialized=false; Log("AVP host closed: native callback and MSUL key/render ownership restored before native close");
    }

    // Explicit alternative hardware source. Neither the controller nor Attach
    // enables a motor: the operator must opt in and select each TD switch.
    IDisposable AutovedenieOperatorInputScope(IntPtr pult) {
        if (!avpInitialized || avpClosing || pult!=mem["TPult"]) throw new Exception("AVP operator source owner guard");
        RestoreAutovedenieOperatorScopes(true);
        var vigilance=BorrowVigilanceOperatorInputs();
        try {
            IDisposable td=(avpSoftwareTd==null || !avpSoftwareTd.Checked) ? (IDisposable)new AutovedenieNoInputScope() : new AutovedenieTdScope(avpTdScopes,pult,avpTd1.Checked,avpTd2.Checked);
            return new SafetyOperatorInputScope(td,vigilance);
        } catch { vigilance.Dispose(); throw; }
    }
    void RestoreAutovedenieOperatorScopes(bool failedOnly=false) {
        for (int n=avpTdScopes.Count-1;n>=0;n--)
            if (!failedOnly || avpTdScopes[n].PendingRestoration) avpTdScopes[n].Dispose();
    }
    sealed class AutovedenieNoInputScope : IDisposable { public void Dispose() {} }
    sealed class AutovedenieTdScope : IDisposable {
        readonly IntPtr pult; readonly int thread; readonly List<AutovedenieTdScope> owners;
        readonly byte old1, old2, assigned1, assigned2;
        bool own1, own2, disposed;
        public bool PendingRestoration { get; private set; }
        internal AutovedenieTdScope(List<AutovedenieTdScope> active,IntPtr ptr,bool td1,bool td2) {
            owners=active; pult=ptr; thread=System.Threading.Thread.CurrentThread.ManagedThreadId;
            AvpRequireWritable(IntPtr.Add(pult,0x34),1); AvpRequireWritable(IntPtr.Add(pult,0x38),1);
            old1=Marshal.ReadByte(pult,0x34); old2=Marshal.ReadByte(pult,0x38);
            assigned1=(byte)(td1?1:0); assigned2=(byte)(td2?1:0);
            owners.Add(this); // Retain a partially written scope until restoration succeeds.
            try {
                own1=true; Marshal.WriteByte(pult,0x34,assigned1);
                own2=true; Marshal.WriteByte(pult,0x38,assigned2);
                if (Marshal.ReadByte(pult,0x34)!=assigned1 || Marshal.ReadByte(pult,0x38)!=assigned2) throw new Exception("AVP TD operator input write guard");
            } catch { Dispose(); throw; }
        }
        public void Dispose() {
            if (disposed) return;
            PendingRestoration=true;
            if (System.Threading.Thread.CurrentThread.ManagedThreadId!=thread) throw new Exception("AVP TD source restoration thread guard");
            Exception error=null;
            if (own2) try { AvpRequireWritable(IntPtr.Add(pult,0x38),1); if (Marshal.ReadByte(pult,0x38)==assigned2) Marshal.WriteByte(pult,0x38,old2); own2=false; } catch (Exception e) { error=e; }
            if (own1) try { AvpRequireWritable(IntPtr.Add(pult,0x34),1); if (Marshal.ReadByte(pult,0x34)==assigned1) Marshal.WriteByte(pult,0x34,old1); own1=false; } catch (Exception e) { if (error==null) error=e; }
            if (error!=null) throw error; disposed=true; owners.Remove(this);
        }
    }
    static void AvpRequireWritable(IntPtr p,int count) {
        AvpMemoryInfo info;
        if (p==IntPtr.Zero || count<1 || AvpVirtualQuery(p,out info,(UIntPtr)Marshal.SizeOf(typeof(AvpMemoryInfo)))==UIntPtr.Zero) throw new Exception("AVP operator writable-range guard");
        ulong address=unchecked((uint)p.ToInt32()), start=unchecked((uint)info.Base.ToInt32()); uint protect=info.Protect&0xff;
        if (info.State!=0x1000 || (info.Protect&0x101)!=0 || address<start || address+(uint)count>start+info.RegionSize.ToUInt32() ||
            protect!=4 && protect!=8 && protect!=0x40 && protect!=0x80) throw new Exception("AVP operator writable-range guard");
    }

    double AutovedeniePhysicalMass() {
        if(trainData==IntPtr.Zero || Marshal.ReadInt32(trainData)!=77)return -1;
        var array=Marshal.ReadIntPtr(trainData,0x42);if(array==IntPtr.Zero)return -1;double mass=0;
        for(int n=0;n<77;n++){var car=Marshal.ReadIntPtr(array,n*8+4);if(car==IntPtr.Zero)return -1;mass+=Marshal.ReadInt32(car,0x25);}
        return mass;
    }
    string AutovedenieModelReason() {
        if (!physicsReady || trainData==IntPtr.Zero || cars==null || cars.Length!=76 || Marshal.ReadInt32(trainData)!=77)
            return "Калибровка подтверждена только для пневматической модели 77 элементов и POEZD 76 транспортных единиц";
        var airArray=Marshal.ReadIntPtr(trainData,0x42);
        var secondBvrPointer=airArray==IntPtr.Zero ? IntPtr.Zero : Marshal.ReadIntPtr(airArray,12);
        if (airArray==IntPtr.Zero || secondBvrPointer==IntPtr.Zero || Marshal.ReadIntPtr(airArray,4)!=cars[0] || Marshal.ReadInt32(secondBvrPointer,0x25)!=100)
            return "Не подтверждены две пневматические секции по 100 т";
        if (avpReferenceWagons!=75 || avpReferenceMass!=6200 || Math.Abs(avpReferenceLength-1126.18)>.001 ||
            Marshal.ReadInt32(mem["TInputParamPult"],0x2c)!=75 || Marshal.ReadInt32(mem["TInputParamPult"],0x30)!=6000 ||
            Marshal.ReadInt32(cars[0],0x25)!=100 || Math.Abs(AutovedeniePhysicalMass()-6200)>.001)
            return "Для этого состава тормозная калибровка не подтверждена (6200 т, 75 вагонов)";
        for (int n=1;n<cars.Length;n++) if (Marshal.ReadInt32(cars[n],0x25)!=80) return "Изменена физическая масса вагона; требуется новая калибровка";
        foreach (var module in avpCalibrationModules) if (AvpGetModuleHandle(module.Key)!=module.Value) return "Изменился срок жизни DLL тормозной калибровки";
        return "";
    }
    double LoadAutovedenieCalibration() {
        string workspace=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,".."));
        string path=Path.Combine(workspace,@"dev\config\autovedenie-brake-calibration.json");
        var serializer=new JavaScriptSerializer { MaxJsonLength=8000000 };
        var config=AvpDictionary(serializer.DeserializeObject(File.ReadAllText(path)));
        if (AvpInteger(config,"schema_version")!=1 || AvpString(config,"status")!="model_specific_candidate") throw new Exception("Неизвестный контракт калибровки");
        var calibration=AvpDictionary(AvpValue(config,"calibration"));
        if (!AvpBoolean(calibration,"accepted")) throw new Exception("Калибровка не принята");
        avpReferenceMass=AvpNumber(calibration,"reference_mass_t"); avpReferenceWagons=AvpInteger(calibration,"reference_wagons"); avpReferenceLength=AvpNumber(calibration,"reference_length_m");
        double coefficient=AvpNumber(calibration,"conservative_service_deceleration");
        if (Math.Abs(coefficient-.07624053955078124)>1e-12 || avpReferenceMass!=6200 || avpReferenceWagons!=75 || Math.Abs(avpReferenceLength-1126.18)>.001) throw new Exception("Другая модель тормозной калибровки");
        var bindings=AvpDictionary(AvpValue(config,"native_bindings"));
        for (int n=0;n<AvpCalibrationNames.Length;n++) {
            string name=AvpCalibrationNames[n];
            if (!String.Equals(AvpString(bindings,AvpCalibrationKeys[n]),AvpCalibrationHashes[n],StringComparison.OrdinalIgnoreCase)) throw new Exception("Неизвестная SHA привязка "+name);
            var handle=AvpGetModuleHandle(name); if (handle==IntPtr.Zero) throw new Exception("DLL не загружена: "+name);
            var loaded=new StringBuilder(32768); uint count=AvpGetModuleFileName(handle,loaded,loaded.Capacity);
            if (count==0 || count>=loaded.Capacity) throw new Exception("Нет пути загруженной DLL: "+name);
            string expected=Path.Combine(resources,n<3 ? @"LOCOMOTIVES\1\"+name : name);
            if (!String.Equals(Path.GetFullPath(loaded.ToString()),Path.GetFullPath(expected),StringComparison.OrdinalIgnoreCase) ||
                AvpFileSha(loaded.ToString())!=AvpCalibrationHashes[n]) throw new Exception("Загруженная DLL не соответствует калибровке: "+name);
            avpCalibrationModules.Add(name,handle);
        }
        string proofSha=AvpString(config,"source_artifact_sha256");
        if (!String.Equals(proofSha,AvpCalibrationProofSha,StringComparison.OrdinalIgnoreCase)) throw new Exception("Неизвестный артефакт калибровки");
        const string evidenceRelative="dev/config/autovedenie-calibration-evidence";
        if (AvpString(config,"versioned_evidence")!=evidenceRelative) throw new Exception("Неизвестный каталог доказательства калибровки");
        string run=Path.GetFullPath(Path.Combine(workspace,evidenceRelative));
        string proofPath=Path.Combine(run,"Autovedenie_graduated_brake.json");
        string resultPath=Path.Combine(run,"result.json");
        const string resultSha="278C6D56031614099D041A6CA045E0FFDCA39DE734EEFDCCBB3260A7E8200861";
        if (AvpString(config,"source_result_sha256")!=resultSha || AvpFileSha(resultPath)!=resultSha) throw new Exception("SHA результата калибровки не совпадает");
        if (AvpFileSha(proofPath)!=AvpCalibrationProofSha) throw new Exception("SHA доказательства калибровки не совпадает");
        var proof=AvpDictionary(serializer.DeserializeObject(File.ReadAllText(proofPath)));
        var result=AvpDictionary(serializer.DeserializeObject(File.ReadAllText(resultPath)));
        if (!AvpBoolean(proof,"passed") || AvpInteger(proof,"electrical_faults")!=0 || !AvpBoolean(proof,"native_motion_observed") ||
            !AvpBoolean(proof,"positive_native_force_observed") || AvpInteger(result,"exit_code")!=0 || AvpString(result,"status")!="passed")
            throw new Exception("Нет успешного штатного прогона калибровки");
        var measured=AvpDictionary(AvpValue(proof,"graduated_brake_calibration"));
        if (!AvpBoolean(measured,"accepted") || AvpNumber(measured,"reference_mass_t")!=avpReferenceMass || AvpInteger(measured,"reference_wagons")!=avpReferenceWagons ||
            Math.Abs(AvpNumber(measured,"reference_length_m")-avpReferenceLength)>.001 || Math.Abs(AvpNumber(measured,"conservative_service_deceleration")-coefficient)>1e-12)
            throw new Exception("Конфигурация не соответствует измеренному торможению");
        if (!AvpBoolean(proof,"guard_restoration_complete") || Math.Abs(AvpNumber(measured,"ur_reduction_requested_bar")-TrainerAutovedenieController.FirstBrakeReduction)>1e-12 ||
            AvpNumber(measured,"settled_windows")<1 || AvpNumber(measured,"release_seconds")>90 || AvpNumber(measured,"maximum_ur_drift_in_hold_bar")>.05)
            throw new Exception("Не подтверждены ступень УР, перекрыша или отпуск калибровки");
        Log("AVP model_reason: calibrated pneumatic candidate 77 elements / POEZD 76 vehicles; 6000t wagons + 200t locomotive, 75 wagons, 1126.18m; integration pneumatic 76 is uncalibrated");
        Log("AVP calibration verified: coefficient="+coefficient.ToString("R",CultureInfo.InvariantCulture)+"; 6200t/75; proof SHA="+AvpCalibrationProofSha);
        return coefficient;
    }
    static object AvpValue(Dictionary<string,object> dictionary,string key) {
        object value; if (!dictionary.TryGetValue(key,out value) || value==null) throw new Exception("Нет поля калибровки: "+key); return value;
    }
    static Dictionary<string,object> AvpDictionary(object value) {
        var dictionary=value as Dictionary<string,object>; if (dictionary==null) throw new Exception("Неверный объект калибровки"); return dictionary;
    }
    static string AvpString(Dictionary<string,object> dictionary,string key) {
        var value=AvpValue(dictionary,key) as string; if (String.IsNullOrEmpty(value)) throw new Exception("Неверная строка калибровки: "+key); return value;
    }
    static bool AvpBoolean(Dictionary<string,object> dictionary,string key) {
        var value=AvpValue(dictionary,key); if (!(value is bool)) throw new Exception("Неверный флаг калибровки: "+key); return (bool)value;
    }
    static bool AvpFinite(double value) { return !Double.IsNaN(value) && !Double.IsInfinity(value); }
    static double AvpNumber(Dictionary<string,object> dictionary,string key) {
        object raw=AvpValue(dictionary,key); if (!(raw is int) && !(raw is long) && !(raw is double) && !(raw is decimal)) throw new Exception("Неверное число калибровки: "+key);
        double value=Convert.ToDouble(raw,CultureInfo.InvariantCulture); if (Double.IsNaN(value) || Double.IsInfinity(value)) throw new Exception("Нечисловое значение калибровки: "+key); return value;
    }
    static int AvpInteger(Dictionary<string,object> dictionary,string key) {
        double number=AvpNumber(dictionary,key); if (number!=Math.Truncate(number) || number<Int32.MinValue || number>Int32.MaxValue) throw new Exception("Неверное целое калибровки: "+key); return (int)number;
    }
    static string AvpFileSha(string path) {
        using (var hash=SHA256.Create()) using (var file=File.OpenRead(path)) return BitConverter.ToString(hash.ComputeHash(file)).Replace("-","");
    }
}
