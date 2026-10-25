using System;
using System.Collections.Generic;
using RailwayDynamics.Simulation;

/// <summary>Immutable actual applied forces on one body, N in fixed +X toward
/// the head. Brake channels are applied forces, not available capacities.</summary>
public sealed class VehicleForceSample {
    public int Index { get; private set; }
    public bool IsLocomotive { get; private set; }
    public double PhysicalMassKg { get; private set; }
    public double EffectiveMassKg { get; private set; }
    public double LengthM { get; private set; }
    public double CentrePositionM { get; private set; }
    public double FrontPositionM { get; private set; }
    public double DistanceBehindHeadM { get; private set; }
    public double SpeedMps { get; private set; }
    public double AccelerationMps2 { get; private set; }
    public double BrakeCylinderPressurePa { get; private set; }
    public double TractionN { get; private set; }
    public double DynamicBrakeN { get; private set; }
    public double PneumaticBrakeN { get; private set; }
    public double RollingN { get; private set; }
    public double CurveN { get; private set; }
    public double GradeN { get; private set; }
    public double FrontCouplerN { get; private set; }
    public double RearCouplerN { get; private set; }
    public double NetForceN { get; private set; }
    public double InertiaForceN { get; private set; }
    public double BalanceResidualN { get; private set; }
    public double BalanceToleranceN { get; private set; }

    internal VehicleForceSample(int index, VehicleDynamicsNode node, double headFront,
        double frontCoupler, double rearCoupler, bool actual, double stepSeconds, double velocityScale) {
        Index = index; IsLocomotive = node.IsLocomotive; PhysicalMassKg = node.MassKg;
        EffectiveMassKg = node.MassKg * (1 + node.RotaryInertiaGamma);
        LengthM = node.LengthM; CentrePositionM = node.PositionX;
        FrontPositionM = node.PositionX + node.LengthM * .5;
        DistanceBehindHeadM = headFront - FrontPositionM;
        SpeedMps = node.VelocityV; AccelerationMps2 = actual ? node.AccelerationA : 0;
        BrakeCylinderPressurePa = node.BrakeCylinderPressurePa;
        if (actual) {
            TractionN = node.TractiveForceN; GradeN = node.GradeForceN;
            // The kernel stores these as signed reactions that it SUBTRACTS.
            DynamicBrakeN = -node.DynamicBrakeForceN; PneumaticBrakeN = -node.BrakeForceN;
            RollingN = -node.RunningResistanceN; CurveN = -node.CurveResistanceN;
            FrontCouplerN = frontCoupler; RearCouplerN = rearCoupler;
            NetForceN = TractionN + GradeN + DynamicBrakeN + PneumaticBrakeN +
                RollingN + CurveN + FrontCouplerN + RearCouplerN;
            InertiaForceN = EffectiveMassKg * AccelerationMps2;
            BalanceResidualN = NetForceN - InertiaForceN;
            double magnitude = Math.Abs(TractionN) + Math.Abs(GradeN) + Math.Abs(DynamicBrakeN) +
                Math.Abs(PneumaticBrakeN) + Math.Abs(RollingN) + Math.Abs(CurveN) +
                Math.Abs(FrontCouplerN) + Math.Abs(RearCouplerN) + Math.Abs(InertiaForceN);
            // The end-velocity solver accepts 1e-10*max(1,max|v|).
            // Dividing the residual momentum by h gives a force bound. Additional
            // binary64 rounding is small but explicit; this is not a zeroing rule.
            BalanceToleranceN = EffectiveMassKg * 1e-10 * Math.Max(1, velocityScale) / stepSeconds
                + 64 * 1.1102230246251565e-16 * Math.Max(1, magnitude);
        }
        foreach (double value in new[] { PhysicalMassKg, EffectiveMassKg, LengthM, CentrePositionM,
            FrontPositionM, DistanceBehindHeadM, SpeedMps, AccelerationMps2, BrakeCylinderPressurePa,
            TractionN, DynamicBrakeN, PneumaticBrakeN, RollingN, CurveN, GradeN, FrontCouplerN,
            RearCouplerN, NetForceN, InertiaForceN, BalanceResidualN, BalanceToleranceN })
            TrainForceSample.RequireFinite(value);
    }
}

/// <summary>One immutable last-accepted-substep force/geometry frame. On a chart
/// with the head at the left, physical +X points left. Link tension is positive;
/// its applied force on the leading body is therefore negative.</summary>
public sealed class TrainForceSample {
    readonly VehicleForceSample[] vehicles;
    readonly double[] linkTension;
    public long Epoch { get; private set; }
    public double TimeSeconds { get; private set; }
    public double AcceptedSubstepSeconds { get; private set; }
    public bool HasAcceptedStep { get; private set; }
    public double TotalEnergyJ { get; private set; }
    public double CumulativeExternalWorkJ { get; private set; }
    public double CumulativeDissipatedEnergyJ { get; private set; }
    public double TotalPhysicalMassKg { get; private set; }
    public double TotalEffectiveMassKg { get; private set; }
    public double MaximumBalanceResidualN { get; private set; }
    public int VehicleCount { get { return vehicles.Length; } }
    public int LinkCount { get { return linkTension.Length; } }
    public VehicleForceSample[] Vehicles { get { return (VehicleForceSample[])vehicles.Clone(); } }
    public double[] LinkTensionN { get { return (double[])linkTension.Clone(); } }

    TrainForceSample(LongitudinalTrainDynamicsSolver kernel, long epoch) {
        if (kernel == null) throw new ArgumentNullException("kernel");
        if (epoch < 0) throw new ArgumentOutOfRangeException("epoch");
        Epoch = epoch;
        VehicleDynamicsNode[] nodes; double[] rear; double h, time;
        HasAcceptedStep = kernel.TryGetLastAcceptedForceStep(out nodes, out rear, out h, out time);
        if (!HasAcceptedStep) {
            nodes = new VehicleDynamicsNode[kernel.VehicleCount];
            for (int i = 0; i < nodes.Length; i++) nodes[i] = kernel.GetVehicle(i);
            rear = new double[Math.Max(0, nodes.Length - 1)]; h = 0; time = kernel.SimulationTimeSeconds;
        }
        TimeSeconds = time; AcceptedSubstepSeconds = h;
        TotalEnergyJ = kernel.TotalEnergyJ; CumulativeExternalWorkJ = kernel.CumulativeExternalWorkJ;
        CumulativeDissipatedEnergyJ = kernel.CumulativeDissipatedEnergyJ;
        RequireFinite(time); RequireFinite(h); RequireFinite(TotalEnergyJ);
        RequireFinite(CumulativeExternalWorkJ); RequireFinite(CumulativeDissipatedEnergyJ);
        if (nodes.Length == 0 || rear.Length != nodes.Length - 1 || h < 0 || HasAcceptedStep && h <= 0)
            throw new InvalidOperationException("Inconsistent accepted force endpoint.");
        vehicles = new VehicleForceSample[nodes.Length]; linkTension = new double[rear.Length];
        double headFront = nodes[0].PositionX + nodes[0].LengthM * .5, velocityScale = 1;
        foreach (var node in nodes) velocityScale = Math.Max(velocityScale, Math.Abs(node.VelocityV));
        for (int i = 0; i < rear.Length; i++) { RequireFinite(rear[i]); linkTension[i] = HasAcceptedStep ? -rear[i] : 0; }
        for (int i = 0; i < nodes.Length; i++) {
            double front = i == 0 ? 0 : -rear[i - 1], back = i == rear.Length ? 0 : rear[i];
            vehicles[i] = new VehicleForceSample(i, nodes[i], headFront, front, back, HasAcceptedStep, h, velocityScale);
            TotalPhysicalMassKg += vehicles[i].PhysicalMassKg; TotalEffectiveMassKg += vehicles[i].EffectiveMassKg;
            MaximumBalanceResidualN = Math.Max(MaximumBalanceResidualN, Math.Abs(vehicles[i].BalanceResidualN));
        }
        RequireFinite(TotalPhysicalMassKg); RequireFinite(TotalEffectiveMassKg);
    }
    public static TrainForceSample Capture(LongitudinalTrainDynamicsSolver kernel, long epoch) {
        return new TrainForceSample(kernel, epoch);
    }
    public VehicleForceSample GetVehicle(int index) {
        if (index < 0 || index >= vehicles.Length) throw new ArgumentOutOfRangeException("index");
        return vehicles[index];
    }
    public double GetLinkTensionN(int index) {
        if (index < 0 || index >= linkTension.Length) throw new ArgumentOutOfRangeException("index");
        return linkTension[index];
    }
    public double GetLinkDistanceBehindHeadM(int index) {
        if (index < 0 || index >= linkTension.Length) throw new ArgumentOutOfRangeException("index");
        double connection = .5 * (vehicles[index].FrontPositionM - vehicles[index].LengthM + vehicles[index + 1].FrontPositionM);
        return vehicles[0].FrontPositionM - connection;
    }
    internal static void RequireFinite(double value) {
        if ((unchecked((ulong)BitConverter.DoubleToInt64Bits(value)) & 0x7ff0000000000000UL) == 0x7ff0000000000000UL)
            throw new InvalidOperationException("Nonfinite train-force observation.");
    }
}

/// <summary>Bounded actual-sample history. No interpolation, inferred wave
/// direction, synthetic arrows or wall-clock time is introduced.</summary>
public sealed class TrainForceHistory {
    readonly List<TrainForceSample> samples = new List<TrainForceSample>();
    readonly double windowSeconds;
    readonly int maxSamples;
    public TrainForceHistory(double windowSeconds = 180, int maxSamples = 2400) {
        TrainForceSample.RequireFinite(windowSeconds);
        if (windowSeconds <= 0) throw new ArgumentOutOfRangeException("windowSeconds");
        if (maxSamples < 1) throw new ArgumentOutOfRangeException("maxSamples");
        this.windowSeconds = windowSeconds; this.maxSamples = maxSamples;
    }
    public int Count { get { return samples.Count; } }
    public TrainForceSample Latest { get { return samples.Count == 0 ? null : samples[samples.Count - 1]; } }
    public TrainForceSample[] Samples { get { return ToArray(); } }
    public void Clear() { samples.Clear(); }
    public void Add(TrainForceSample sample) { Push(sample); }
    public void Push(TrainForceSample sample) {
        if (sample == null) throw new ArgumentNullException("sample");
        var last = Latest;
        if (last != null && sample.Epoch != last.Epoch) { Clear(); last = null; }
        if (last != null && sample.TimeSeconds < last.TimeSeconds)
            throw new InvalidOperationException("A time reset requires a new train-force epoch.");
        if (last != null && sample.TimeSeconds == last.TimeSeconds) samples[samples.Count - 1] = sample;
        else samples.Add(sample);
        double firstTime = sample.TimeSeconds - windowSeconds;
        int remove = 0;
        while (remove < samples.Count && (samples[remove].TimeSeconds < firstTime || samples.Count - remove > maxSamples)) remove++;
        if (remove != 0) samples.RemoveRange(0, remove);
    }
    public TrainForceSample[] ToArray() { return samples.ToArray(); }
}
