using System;
using RailwayDynamics.Simulation;

namespace RailwayDynamics.Braking {
    /// <summary>
    /// A group of identical cylinders feeding equally loaded shoes at one measured pressure.
    /// The lever ratio is the sum of shoe normal forces per cylinder divided by rod force.
    /// Spring forces are referred to ONE cylinder rod before the lever and group count.
    /// These are explicit calculation profiles, not an assertion of an installed vehicle's build.
    /// </summary>
    public sealed class BrakeRigging {
        public string Id { get; private set; }
        public double DiameterM { get; private set; }
        public int CylinderCount { get; private set; }
        public int ShoeCount { get; private set; }
        public double RatioTotalPerCylinder { get; private set; }
        public double CylinderEfficiency { get; private set; }
        public double RiggingEfficiency { get; private set; }
        public double ReturnSpringForcePerCylinderN { get; private set; }
        public double AutoRegulatorForcePerCylinderN { get; private set; }
        public double PistonAreaM2 { get; private set; }
        public double ShoeContactThresholdPa { get; private set; }

        public BrakeRigging(string id, double diameterM, int cylinderCount, int shoeCount,
            double ratioTotalPerCylinder, double cylinderEfficiency, double riggingEfficiency,
            double returnSpringForcePerCylinderN, double autoRegulatorForcePerCylinderN) {
            if (String.IsNullOrWhiteSpace(id)) throw new ArgumentException("Profile id is required", "id");
            Positive(diameterM, "diameterM"); Positive(ratioTotalPerCylinder, "ratioTotalPerCylinder");
            if (cylinderCount <= 0) throw new ArgumentOutOfRangeException("cylinderCount");
            if (shoeCount <= 0 || shoeCount % cylinderCount != 0)
                throw new ArgumentOutOfRangeException("shoeCount", "An identical-cylinder group requires an integral number of shoes per cylinder");
            Efficiency(cylinderEfficiency, "cylinderEfficiency"); Efficiency(riggingEfficiency, "riggingEfficiency");
            Nonnegative(returnSpringForcePerCylinderN, "returnSpringForcePerCylinderN");
            Nonnegative(autoRegulatorForcePerCylinderN, "autoRegulatorForcePerCylinderN");
            double area = Math.PI * diameterM * diameterM / 4.0;
            Positive(area, "pistonAreaM2");
            double threshold = (returnSpringForcePerCylinderN + autoRegulatorForcePerCylinderN) / (area * cylinderEfficiency);
            Nonnegative(threshold, "shoeContactThresholdPa");
            Id = id; DiameterM = diameterM; CylinderCount = cylinderCount; ShoeCount = shoeCount;
            RatioTotalPerCylinder = ratioTotalPerCylinder; CylinderEfficiency = cylinderEfficiency;
            RiggingEfficiency = riggingEfficiency; ReturnSpringForcePerCylinderN = returnSpringForcePerCylinderN;
            AutoRegulatorForcePerCylinderN = autoRegulatorForcePerCylinderN;
            PistonAreaM2 = area; ShoeContactThresholdPa = threshold;
        }

        /// <summary>GOST 34434-2018 Appendix G: effectiveness case, maximum 65 mm rod stroke.</summary>
        public static BrakeRigging GostAppendixGWagon() {
            return new BrakeRigging("GOST34434-2018-AppendixG-2x254-effectiveness", .254, 2, 8, 5.70,
                .98, .95, 883.0 + 2300.0 * .065, (883.0 + 20800.0 * .010) * .47);
        }

        /// <summary>
        /// Source-based typical 2ES6 bogie: 4 x 203 mm 670V, two shoes per cylinder, n=8.8 total.
        /// 1200 N at 65 mm stroke and efficiencies .98/.95 are the 670V research reference
        /// (Sinitsyn/Kobishchanov 2020, DOI 10.30987/1999-8775-2020-3-21-28, p23).
        /// No separate external rod-type regulator is assumed; this is not OEM 2ES6 calibration.
        /// </summary>
        public static BrakeRigging Typical2Es6Bogie670V() {
            return new BrakeRigging("Typical2ES6-bogie-670V-203mm-reference65mm", .203, 4, 8, 8.8,
                .98, .95, 1200.0, 0.0);
        }

        internal static void Finite(double value, string name) {
            if (Double.IsNaN(value) || Double.IsInfinity(value)) throw new ArgumentOutOfRangeException(name, "A finite value is required");
        }
        internal static void Nonnegative(double value, string name) {
            Finite(value, name); if (value < 0) throw new ArgumentOutOfRangeException(name, "A nonnegative value is required");
        }
        static void Positive(double value, string name) {
            Finite(value, name); if (value <= 0) throw new ArgumentOutOfRangeException(name, "A positive value is required");
        }
        static void Efficiency(double value, string name) {
            Positive(value, name); if (value > 1) throw new ArgumentOutOfRangeException(name, "Efficiency must not exceed one");
        }
    }

    public sealed class BrakeForceResult {
        public double PressureGaugePa { get; private set; }
        public double PistonForcePerCylinderN { get; private set; }
        public double NetRodForcePerCylinderN { get; private set; }
        public double NormalForcePerShoeN { get; private set; }
        public double TotalNormalForceN { get; private set; }
        public double FrictionCoefficient { get; private set; }
        public double BrakeForceN { get; private set; }

        internal BrakeForceResult(double pressure, double piston, double rod, double shoe, double total, double mu, double force) {
            PressureGaugePa = pressure; PistonForcePerCylinderN = piston; NetRodForcePerCylinderN = rod;
            NormalForcePerShoeN = shoe; TotalNormalForceN = total; FrictionCoefficient = mu; BrakeForceN = force;
        }
    }

    /// <summary>
    /// SI chain: gauge Pa -> piston N -> net rod N -> per-shoe normal N -> friction capacity N.
    /// Returns a nonnegative tread-brake capacity. The motion solver owns direction, stiction,
    /// wheel/rail adhesion, electrical braking and timing; this class does not advance pneumatics.
    /// </summary>
    public static class ScientificBrakeForces {
        public static BrakeForceResult Compute(BrakeRigging rig, BrakeShoeType type, double pressureGaugePa, double speedMps) {
            if (rig == null) throw new ArgumentNullException("rig");
            BrakeRigging.Nonnegative(pressureGaugePa, "pressureGaugePa"); BrakeRigging.Finite(speedMps, "speedMps");
            double piston = rig.PistonAreaM2 * pressureGaugePa;
            double rod = Math.Max(0.0, piston * rig.CylinderEfficiency
                - rig.ReturnSpringForcePerCylinderN - rig.AutoRegulatorForcePerCylinderN);
            double total = rod * rig.RatioTotalPerCylinder * rig.RiggingEfficiency * rig.CylinderCount;
            double perShoe = total / rig.ShoeCount;
            BrakeRigging.Nonnegative(piston, "pistonForceN"); BrakeRigging.Nonnegative(rod, "netRodForceN");
            BrakeRigging.Nonnegative(total, "totalNormalForceN"); BrakeRigging.Nonnegative(perShoe, "normalForcePerShoeN");
            double mu = BrakeTribologyPtr.ComputeBrakeShoeFriction(type, speedMps, perShoe);
            double force = total * mu;
            BrakeRigging.Nonnegative(force, "brakeForceN");
            return new BrakeForceResult(pressureGaugePa, piston, rod, perShoe, total, mu, force);
        }
    }
}
