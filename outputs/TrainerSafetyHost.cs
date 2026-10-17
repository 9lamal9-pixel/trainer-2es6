using System;
using System.IO;
using System.Runtime.InteropServices;

partial class TrainerIntegrationHost {
    sealed class SafetyOperatorInputScope : IDisposable {
        readonly IDisposable td,vigilance;
        internal SafetyOperatorInputScope(IDisposable motor,IDisposable button){td=motor;vigilance=button;}
        public void Dispose(){try{td.Dispose();}finally{vigilance.Dispose();}}
    }
    TrainerNativeSafetyAdapter nativeSafetyAdapter;
    TrainerNativeVirtualPult nativeVirtualPult;
    bool nativeCommunicationInactive=true,communicationInitializationAttempted;
    void InitializeVirtualPultSource(IntPtr communicationModule) {
        nativeVirtualPult=TrainerNativeVirtualPult.Attach(communicationModule,root,delegate{return nativeCommunicationInactive;},Log);
        Log("Original virtual-pult configuration="+nativeVirtualPult.OriginalVirtualPult+"; selected screen pult before native initialization");
    }
    void CloseNativeCommunication() {
        if(communicationInitializationAttempted){commClose();commReady=false;nativeCommunicationInactive=true;communicationInitializationAttempted=false;}
        if(nativeVirtualPult!=null){nativeVirtualPult.Dispose();nativeVirtualPult=null;}
    }
    TrainerKlubSafetyBinding klubSafetyBinding;
    bool nativeAlsnOwned;byte nativeAlsnOriginal,nativeAlsnAssigned;
    void InitializeNativeSafetyAdapter() {
        if(nativeSafetyAdapter!=null)throw new Exception("Native safety adapter already owned");
        var communicationOwner=mem["CommOwner"];
        var klub=GetModuleHandle("KLUB_U_LCD_XE2.dll");
        nativeSafetyAdapter=TrainerNativeSafetyAdapter.Attach(
            Path.Combine(root,"Trenager_3D.exe"),mem["TPribBezAllTren"],
            standaloneSautOutput,communicationOwner,klub,
            delegate{return commReady&&klubReady&&mem["CommOwner"]==communicationOwner&&
                standaloneSautBinding!=null&&standaloneSautBinding.Connected;},Log);
        klubSafetyBinding=new TrainerKlubSafetyBinding(klub,Path.Combine(resources,"KLUB_U_LCD_XE2.dll"),mem["TOutParamAllTRen"],delegate{return commReady&&klubReady;});
        BindVigilanceStatusReader(ReadVigilanceNativeStatus);
        mem.Add("NativeSafetyEnvelope",nativeSafetyAdapter.Envelope);
        mem.Add("NativeSafetyInputOutput",nativeSafetyAdapter.InputOutput);
        mem.Add("NativeSafetyCode",nativeSafetyAdapter.CodePointer);
    }
    string ReadVigilanceNativeStatus() {
        if(klubSafetyBinding==null)return "";
        var loc=mem["TLocParam"];
        bool heldWarning=Marshal.ReadByte(mem["TOutParamAllTRen"],0x30)==1;
        string request=klubSafetyBinding.Alarm||klubSafetyBinding.Pisk ? "КЛУБ требует "+klubSafetyBinding.RequestedButton : "КЛУБ: проверки нет";
        if(standaloneSautBinding!=null&&Marshal.ReadByte(standaloneSautOutput,9)==1)request+="; САУТ требует РБ / РБС";
        if(heldWarning)request+="; отпустите все РБ — длительное удержание";
        return request+"; ЭПК="+Marshal.ReadByte(loc,0x9b)+", камера="+FloatAt(loc,0x9f).ToString("F2")+" атм";
    }
    void PublishNativeKlubAlsn() {
        if(klubSafetyBinding==null)return;
        var input=mem["TInputParamPult"];AvpRequireWritable(IntPtr.Add(input,0x1d),1);
        if(!nativeAlsnOwned){nativeAlsnOriginal=Marshal.ReadByte(input,0x1d);nativeAlsnOwned=true;}
        // Original EXE3E4D60/3E3B24 selects the loaded KLUB provider and
        // projects actual native PBOut+20 to Input+1D, including power OFF.
        // This is a primary sensor projection, not a guessed CodeWay conversion.
        nativeAlsnAssigned=klubSafetyBinding.Signal;B(input,0x1d,nativeAlsnAssigned);
    }
    void RestoreNativeKlubAlsn() {
        if(!nativeAlsnOwned)return;var input=mem["TInputParamPult"];AvpRequireWritable(IntPtr.Add(input,0x1d),1);
        if(Marshal.ReadByte(input,0x1d)==nativeAlsnAssigned)B(input,0x1d,nativeAlsnOriginal);nativeAlsnOwned=false;
    }
    void NativeSafetyInterfaceCycle() {
        if(nativeSafetyAdapter==null)return;
        nativeSafetyAdapter.InterfaceInvoke();
    }
    void CloseNativeSafetyAdapter() {
        if(nativeSafetyAdapter==null)return;
        RestoreNativeKlubAlsn();BindVigilanceStatusReader(null);klubSafetyBinding=null;
        nativeSafetyAdapter.Dispose();nativeSafetyAdapter=null;
        mem.Remove("NativeSafetyEnvelope");mem.Remove("NativeSafetyInputOutput");mem.Remove("NativeSafetyCode");
    }
}
