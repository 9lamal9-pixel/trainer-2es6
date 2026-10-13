using System;
using System.Runtime.InteropServices;

partial class TrainerIntegrationHost {
    TrainerSafetyInputContext sautInputContext, klubInputContext;

    TrainerSafetyInputContext CreateSafetyContext(string label, IntPtr output) {
        var canonical=mem["TPribBezAllTren"];
        var owner=mem["CommOwner"];
        var communication=AvpGetModuleHandle("Communication1.dll");
        if (!commReady || communication==IntPtr.Zero) throw new Exception("Safety source not initialized");
        var context=TrainerSafetyInputContext.Create(canonical,output,delegate {
            return commReady && AvpGetModuleHandle("Communication1.dll")==communication &&
                mem["TPribBezAllTren"]==canonical && mem["CommOwner"]==owner;
        });
        mem.Add(label+"SafetyInput",context.Input);
        mem.Add(label+"SafetyLight",context.Light);
        mem.Add(label+"SafetySharedRecord",context.SharedRecord);
        return context;
    }
    void ReleaseSafetyContext(string label, TrainerSafetyInputContext context) {
        if (context==null) return;
        IntPtr input=context.Input,light=context.Light,shared=context.SharedRecord;
        context.Dispose();
        foreach (var pair in new[] {
            new System.Collections.Generic.KeyValuePair<string,IntPtr>(label+"SafetyInput",input),
            new System.Collections.Generic.KeyValuePair<string,IntPtr>(label+"SafetyLight",light),
            new System.Collections.Generic.KeyValuePair<string,IntPtr>(label+"SafetySharedRecord",shared) })
            if(mem.ContainsKey(pair.Key)&&mem[pair.Key]==pair.Value)mem.Remove(pair.Key);
    }
    void InitializeAuxContext(IntPtr module,string label,string step,string close,Func<IntPtr,byte> initialize) {
        var context=CreateSafetyContext(label,mem["TOutParamAllTRen"]);
        var nativeStep=Export<StepFn>(module,step);
        var nativeClose=Export<StepFn>(module,close);
        bool attempted=false,closed=false;
        otherCloses.Add(delegate {
            if(closed)return;
            if(attempted)nativeClose();
            closed=true;
            ReleaseSafetyContext(label,context);
        });
        attempted=true;
        if(initialize(context.SharedRecord)!=1)throw new Exception(label+" initialization failed");
        otherSteps.Add(delegate {
            if(closed)throw new ObjectDisposedException(label);
            context.Sync();
            try{nativeStep();}catch(Exception error){throw new Exception(label+" native step failed",error);}
        });
        Log(label+" initialized: actual sensor inputs, private native light calculations");
    }
}
