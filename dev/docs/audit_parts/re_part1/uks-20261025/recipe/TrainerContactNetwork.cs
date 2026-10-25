using System;

partial class TrainerIntegrationHost {
    // RE1 table 1: nominal contact network 3000 V. This is an external
    // scenario input, shared by all native voltage producers, not a gauge fix.
    const int NominalContactVoltage = 3000;
    int? contactVoltageOverride;
    TrainerContactVoltagePatch contactVoltagePatch;

    void SetContactNetworkVoltage(int? volts) {
        // The existing native measured channel is Int32, with a documented
        // 5040 V full scale. This input profile resolves whole volts; its range
        // is not a statement of the permissible operating voltage.
        if (volts.HasValue && (volts.Value < 0 || volts.Value > 5040))
            throw new ArgumentOutOfRangeException("volts", "Contact voltage input must be 0..5040 V");
        contactVoltageOverride = volts;
    }

    void ReadContactNetworkCommand(string key, string value) {
        if (key != "uks") return;
        int volts;
        if (!Int32.TryParse(value, System.Globalization.NumberStyles.Integer,
                System.Globalization.CultureInfo.InvariantCulture, out volts) || volts < 0 || volts > 5040) {
            Log("Rejected contact voltage input: " + value);
            return;
        }
        SetContactNetworkVoltage(volts);
    }

    void InitializeContactNetwork() {
        if (contactVoltagePatch != null) throw new InvalidOperationException("Contact voltage already attached");
        contactVoltagePatch = new TrainerContactVoltagePatch(resources, mem["TPult"], mem["TTygaParam"]);
        BeforeContactNetworkElectricalCycle();
        Log("Native contact voltage source attached: " + (contactVoltageOverride ?? NominalContactVoltage) + " V");
    }

    void BeforeContactNetworkElectricalCycle() {
        if (contactVoltagePatch == null) throw new InvalidOperationException("Native contact voltage source absent");
        contactVoltagePatch.SetVoltage(contactVoltageOverride ?? NominalContactVoltage);
    }

    void AfterContactNetworkElectricalCycle() {
        if (contactVoltagePatch == null) throw new InvalidOperationException("Native contact voltage source absent");
        // A producer may detect a graph/source fault during ExecuteCommunication.
        // Reject that frame before any pneumatic or movement step consumes it.
        contactVoltagePatch.Validate();
    }

    void CloseContactNetwork() {
        if (contactVoltagePatch != null) {
            contactVoltagePatch.Dispose();
            contactVoltagePatch = null;
        }
        if (TrainerContactVoltagePatch.HasRetainedOwners)
            throw new InvalidOperationException("Native close refused while contact voltage patch ownership remains");
    }
}
