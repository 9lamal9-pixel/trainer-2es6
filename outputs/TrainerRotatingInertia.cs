using System;

namespace RailwayDynamics.Inertia {
    /// <summary>A rigidly wheel-coupled rotating part. Ratio is the magnitude
    /// d(angular speed)/d(train speed), rad/m; it is not a transmission efficiency.</summary>
    public sealed class RotatingComponent {
        readonly double inertia, ratio, mass;
        readonly int count;
        public double JKgM2 { get { return inertia; } }
        public double AngularSpeedPerTrainSpeedRadPerM { get { return ratio; } }
        public int Count { get { return count; } }
        public double EquivalentRotatingMassKg { get { return mass; } }

        public RotatingComponent(double jKgM2, double angularSpeedPerTrainSpeedRadPerM, int count) {
            RotatingInertiaProfile.Nonnegative(jKgM2, "jKgM2");
            RotatingInertiaProfile.Nonnegative(angularSpeedPerTrainSpeedRadPerM, "angularSpeedPerTrainSpeedRadPerM");
            if (count < 0) throw new ArgumentOutOfRangeException("count");
            inertia = jKgM2; ratio = angularSpeedPerTrainSpeedRadPerM; this.count = count;
            // Multiplication order avoids needlessly squaring a huge ratio first.
            mass = count == 0 || inertia == 0 || ratio == 0 ? 0 :
                RotatingInertiaProfile.Product(
                    RotatingInertiaProfile.Product(
                        RotatingInertiaProfile.Product(inertia, ratio), ratio), count);
        }

        public static RotatingComponent Wheel(double jKgM2, double rollingRadiusM, int count) {
            RotatingInertiaProfile.Positive(rollingRadiusM, "rollingRadiusM");
            return new RotatingComponent(jKgM2, RotatingInertiaProfile.Quotient(1, rollingRadiusM), count);
        }

        /// <summary>gearRatio = motor angular speed / axle angular speed.
        /// A permanently coupled rotor still contributes in coast and braking.</summary>
        public static RotatingComponent Motor(double jKgM2, double gearRatio, double rollingRadiusM, int count) {
            RotatingInertiaProfile.Positive(gearRatio, "gearRatio");
            RotatingInertiaProfile.Positive(rollingRadiusM, "rollingRadiusM");
            return new RotatingComponent(jKgM2, RotatingInertiaProfile.Quotient(gearRatio, rollingRadiusM), count);
        }
    }

    /// <summary>Immutable equivalent rotating mass. No OEM rotor inertia is
    /// inferred from a literature-based reference gamma. Valid for rigid rolling
    /// kinematics; explicit wheel-spin dynamics must not count these parts twice.</summary>
    public sealed class RotatingInertiaProfile {
        readonly RotatingComponent[] components;
        readonly double equivalentMass, referenceMass, referenceGamma;
        readonly bool referenceEstimate;
        public double EquivalentRotatingMassKg { get { return equivalentMass; } }
        public bool IsReferenceEstimate { get { return referenceEstimate; } }
        public double ReferenceGrossMassKg { get { return referenceMass; } }
        public double ReferenceGamma { get { return referenceGamma; } }
        public RotatingComponent[] Components { get { return (RotatingComponent[])components.Clone(); } }

        RotatingInertiaProfile(RotatingComponent[] parts, double mass, bool estimated, double referenceGrossMassKg, double gamma) {
            components = (RotatingComponent[])parts.Clone(); equivalentMass = mass;
            referenceEstimate = estimated; referenceMass = referenceGrossMassKg; referenceGamma = gamma;
        }

        public static RotatingInertiaProfile FromComponents(params RotatingComponent[] parts) {
            if (parts == null) throw new ArgumentNullException("parts");
            var copy = (RotatingComponent[])parts.Clone(); double sum = 0;
            for (int i = 0; i < copy.Length; i++) {
                if (copy[i] == null) throw new ArgumentException("Null rotating component.", "parts");
                sum = Sum(sum, copy[i].EquivalentRotatingMassKg);
            }
            return new RotatingInertiaProfile(copy, sum, false, 0, 0);
        }

        /// <summary>Returns fixed additional mass, not physical gross mass.
        /// Keep this mass when only the payload of the same vehicle changes.</summary>
        public static double ReferenceScaledRotaryMass(double referenceGrossMassKg, double referenceGamma) {
            Positive(referenceGrossMassKg, "referenceGrossMassKg");
            Nonnegative(referenceGamma, "referenceGamma");
            return Product(referenceGrossMassKg, referenceGamma);
        }

        public static RotatingInertiaProfile FromReferenceMass(double referenceGrossMassKg, double referenceGamma) {
            return new RotatingInertiaProfile(new RotatingComponent[0],
                ReferenceScaledRotaryMass(referenceGrossMassKg, referenceGamma), true,
                referenceGrossMassKg, referenceGamma);
        }

        public double Gamma(double physicalMassKg) {
            Positive(physicalMassKg, "physicalMassKg");
            return Quotient(equivalentMass, physicalMassKg);
        }

        public double EffectiveMassKg(double physicalMassKg) {
            Positive(physicalMassKg, "physicalMassKg");
            return Sum(physicalMassKg, equivalentMass);
        }

        public double RotationalKineticEnergyJ(double signedSpeedMps) {
            Finite(signedSpeedMps, "signedSpeedMps"); double v = Math.Abs(signedSpeedMps);
            return Energy(equivalentMass, v);
        }

        public double TotalKineticEnergyJ(double physicalMassKg, double signedSpeedMps) {
            Positive(physicalMassKg, "physicalMassKg"); Finite(signedSpeedMps, "signedSpeedMps");
            double v = Math.Abs(signedSpeedMps);
            return Energy(EffectiveMassKg(physicalMassKg), v);
        }

        static double Energy(double mass, double speedMagnitude) {
            // Put the half on the larger factor: avoid losing a representable
            // tiny inertia before a large speed restores its energy scale.
            return speedMagnitude > 1 ? Product(Product(mass, .5 * speedMagnitude), speedMagnitude) :
                Product(Product(.5 * mass, speedMagnitude), speedMagnitude);
        }

        internal static void Finite(double value, string name) {
            // Do not construct a NaN sentinel under a native trapping FPU context.
            if ((unchecked((ulong)BitConverter.DoubleToInt64Bits(value)) & 0x7ff0000000000000UL) == 0x7ff0000000000000UL)
                throw new ArgumentOutOfRangeException(name, "Finite value required.");
        }
        internal static void Nonnegative(double value, string name) {
            Finite(value, name); if (value < 0) throw new ArgumentOutOfRangeException(name, "Nonnegative value required.");
        }
        internal static void Positive(double value, string name) {
            Finite(value, name); if (value <= 0) throw new ArgumentOutOfRangeException(name, "Positive value required.");
        }
        internal static double Product(double a, double b) {
            if (a == 0 || b == 0) return 0;
            if (b > 1 && a > Double.MaxValue / b) throw new OverflowException("Equivalent inertia product overflow.");
            double result = a * b; Finite(result, "product"); return result;
        }
        internal static double Sum(double a, double b) {
            if (a > Double.MaxValue - b) throw new OverflowException("Equivalent inertia sum overflow.");
            double result = a + b; Finite(result, "sum"); return result;
        }
        internal static double Quotient(double numerator, double denominator) {
            if (denominator < 1 && numerator > Double.MaxValue * denominator) throw new OverflowException("Equivalent inertia quotient overflow.");
            double result = numerator / denominator; Finite(result, "quotient"); return result;
        }
    }
}
