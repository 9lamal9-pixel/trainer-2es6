using System;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;
using System.Drawing;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Web.Script.Serialization;

partial class TrainerIntegrationHost {
    // The executable entry point selects the original markup. A fixture that
    // instantiates the form selects its source explicitly before StartNative.
    bool nativeMarkupEnabled;
    uint nativeNameCodePage;
    [DllImport("kernel32.dll")] static extern uint GetACP();
    [DllImport("kernel32.dll",CharSet=CharSet.Unicode,SetLastError=true)]
    static extern int WideCharToMultiByte(uint codePage,uint flags,string source,int characters,byte[] destination,int size,IntPtr defaultChar,IntPtr usedDefaultChar);
    TrainerMarkupSignalSource markupSource;
    TrainerMarkupSourceFrame markupFrame;
    ComboBox markupSignalChoice,markupStateChoice;
    Label markupStatus;
    const string NativeMarkupSha="BF11DB8D35391B7A3CBF3DC02F96C8AB08B0B6237A7DD5AEE4049C093F809728";

    bool LoadNativeMarkupRoute() {
        if(!nativeMarkupEnabled)return false;
        string path=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,
            @"..\dev\config\native-markup\Tumen_Ekaterinburg.source.json"));
        markupSource=TrainerMarkupSignalSource.Load(path,NativeMarkupSha);nativeNameCodePage=GetACP();
        route=new RouteModel {native_axis=true,direction=1,map_start_km=0,
            map_end_km=markupSource.NativeCourseEnd/1000,initial_km=2.6,
            source_name="Исходная разметка: "+markupSource.SourceName};
        foreach(var p in markupSource.Profiles)route.profile.Add(new RouteGrade {
            km_start=p.Nach/1000,km_end=(p.Nach+p.Len)/1000,grade_permille=p.Uklon});
        // The original .ogran contains zero entries. This declared teaching cap
        // is not imported from the PDF or inferred from a sign parameter.
        route.speed_limits.Add(new RouteSpeed {km_start=0,km_end=route.map_end_km,speed_kmh=80});
        foreach(var p in markupSource.Stations.Where(p=>p.Meter>=1127&&p.Meter<=markupSource.NativeCourseEnd))
            route.stations.Add(new RouteStation {km=p.Meter/1000.0,name=p.Name});
        foreach(var p in markupSource.Signals)route.signals.Add(new RouteSignal {
            km=p.Meter/1000.0,type="native_signal",label=p.Name,static_scheme=true});
        LoadMarkupPlan(path);
        route.Validate();markupSource.ResetPosition(route.X(route.initial_km));
        Log("Original markup route selected: "+markupSource.SourceName+", native axis 0.."+
            markupSource.NativeCourseEnd+" m, signals="+markupSource.Signals.Length+
            "; saved states / explicit teaching changes; operational cap80, no live occupancy");
        InstallMarkupControls();return true;
    }
    void LoadMarkupPlan(string path) {
        var bytes=File.ReadAllBytes(path);using(var h=SHA256.Create())
            if(BitConverter.ToString(h.ComputeHash(bytes)).Replace("-","")!=NativeMarkupSha)throw new Exception("Markup plan SHA guard");
        var data=(Dictionary<string,object>)new JavaScriptSerializer{MaxJsonLength=8000000}.DeserializeObject(Encoding.UTF8.GetString(bytes));
        var tables=(Dictionary<string,object>)data["additional_tables"];var plan=(Dictionary<string,object>)tables["plan"];
        var records=(object[])plan["records"];if(records.Length!=177)throw new Exception("Markup plan count guard");
        // Original text loader2647C8/RTTI TPlanE: Nach,Long,Radius,Hkr.
        // POEZD SetPlan27F8 applies Abs(radius); raw Hkr is recomputed by the original loader and not used here.
        foreach(var item in records){var p=(Dictionary<string,object>)item;
            double start=Convert.ToDouble(p["Nach"],CultureInfo.InvariantCulture),length=Convert.ToDouble(p["Long"],CultureInfo.InvariantCulture),radius=Convert.ToDouble(p["Radius"],CultureInfo.InvariantCulture);
            route.curves.Add(new RouteCurve{km_start=start/1000,km_end=(start+length)/1000,radius_m=Math.Abs(radius),length_m=length,chainage_kind="native"});}
    }
    void InstallMarkupControls() {
        ClientSize=new Size(Math.Max(800,ClientSize.Width),790);Location=new Point(40,40);
        markupStatus=new Label {Left=10,Top=687,Width=775,Height=42};Controls.Add(markupStatus);
        markupSignalChoice=new ComboBox {Left=10,Top=737,Width=220,DropDownStyle=ComboBoxStyle.DropDownList};
        foreach(var p in markupSource.Signals)markupSignalChoice.Items.Add(p.Id+": "+p.Name+" · "+p.Meter+" м");
        markupSignalChoice.SelectedIndex=1;Controls.Add(markupSignalChoice);
        markupStateChoice=new ComboBox {Left=240,Top=737,Width=235,DropDownStyle=ComboBoxStyle.DropDownList};
        markupStateChoice.Items.AddRange(new object[]{"Зелёный","Жёлтый","Красный","Белый / без кода"});
        markupStateChoice.SelectedIndex=0;Controls.Add(markupStateChoice);
        var apply=new Button {Left=485,Top=734,Width=145,Height=32,Text="Задать сценарий"};
        apply.Click+=delegate{var signals=markupSource.Signals;int n=markupSignalChoice.SelectedIndex;
            if(n<0)return;int[] states={0,2,1,4};markupSource.SetTeachingSignalState(signals[n].Id,states[markupStateChoice.SelectedIndex]);
            RouteInputCycle();Log("Teaching signal change: id="+signals[n].Id+" state="+states[markupStateChoice.SelectedIndex]);};
        Controls.Add(apply);
        var restore=new Button {Left=640,Top=734,Width=145,Height=32,Text="Исходные сигналы"};
        restore.Click+=delegate{markupSource.RestorePersistedSignalStates();RouteInputCycle();};Controls.Add(restore);
    }
    string NativeLocomotiveSignalName() {
        if(klubSafetyBinding==null)return "инициализация";
        switch(klubSafetyBinding.Signal){case 1:return "З";case 2:return "Ж";case 3:return "КЖ";case 4:return "К";case 5:return "Б";default:return "код "+klubSafetyBinding.Signal;}
    }
    byte[] EncodeMarkupNativeName(string value) {
        var name=new byte[21];if(String.IsNullOrEmpty(value))return name;
        var encoded=new byte[511];int count=WideCharToMultiByte(nativeNameCodePage,0,value,Math.Min(255,value.Length),encoded,encoded.Length,IntPtr.Zero,IntPtr.Zero);
        if(count<=0)throw new Exception("Native markup ANSI conversion failed="+Marshal.GetLastWin32Error());
        int length=Math.Min(20,Math.Min(255,count));name[0]=(byte)length;Array.Copy(encoded,0,name,1,length);return name;
    }
    void PublishMarkupSignalInput() {
        if(markupSource==null)return;
        var input=mem["TInputParamPult"];var light=mem["TCurLighterRec"];
        if(Marshal.ReadIntPtr(input,0x1f)!=light)throw new Exception("Markup source light alias changed");
        AvpRequireWritable(input,52);AvpRequireWritable(light,59);
        markupFrame=markupSource.Sample(position);
        I(light,0,markupFrame.Index);
        // Original 43B090 gets Windows ACP; 9468/88B0 converts with flags0 and
        // 8E30 stores the length byte plus at most20 ANSI bytes.
        Marshal.Copy(EncodeMarkupNativeName(markupFrame.Name),0,IntPtr.Add(light,4),21);
        I(light,0x19,markupFrame.Xlight);I(light,0x1d,markupFrame.XlastLight);
        B(light,0x21,markupFrame.SendCode?1:0);B(light,0x22,markupFrame.BlocUch);
        B(input,0x1e,markupFrame.CodeWay);
        // ALSN+1D and Delphi RealKM are different sources, not guessed here.
        // Derived light fields +23..3A belong to each native consumer context.
        if(markupStatus!=null)markupStatus.Text="Разметка · "+markupFrame.Name+" · "+markupFrame.Xlight+
            " м · "+markupFrame.StateName+". "+(markupFrame.TeachingOverride?"Учебное показание":"Исходное показание")+
            (markupFrame.PassedStop?". Зафиксирован проезд запрещающего; выберите новую начальную станцию.":"")+"\n"+
            "КЛУБ: "+NativeLocomotiveSignalName()+". Координата разметки "+position.ToString("F1",CultureInfo.InvariantCulture)+" м; учебный предел80 км/ч.";
    }
}
