using System;

partial class TrainerIntegrationHost {
    // The experimental F(v) module remains available for isolated research.
    // It failed energy/zero-voltage/current consistency checks and must not
    // replace native electrical results or instruments after commStep.
    bool modernTractionEnabled = false;
    void ReadModernTractionCommand(string key, string value) {
        if(key!="moderntyga" && key!="moderntraction")return;
        modernTractionEnabled=false;
        Log("Experimental traction override refused: electrical energy/current calibration is not accepted; native Tyga remains the producer");
    }
    partial void AfterCommStepTractionHook() { }
    void ApplyModernTractionPhysics() { }
}