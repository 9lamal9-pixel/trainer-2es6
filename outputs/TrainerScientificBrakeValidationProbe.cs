using System;
using System.Collections.Generic;
using System.IO;
using System.Web.Script.Serialization;
using RailwayDynamics.Braking;
using RailwayDynamics.Simulation;

/// <summary>Pure arithmetic validation. No native modules, host, buffers or output forcing.</summary>
public sealed class TrainerScientificBrakeValidationProbe {
    static readonly List<object> checks = new List<object>();
    static readonly List<object> samples = new List<object>();
    static void Check(string id, bool ok, object detail) {
        checks.Add(new { id, passed = ok, detail });
        if (!ok) throw new InvalidOperationException("Scientific brake assertion failed: " + id);
    }
    static void Near(string id, double actual, double expected, double tolerance) {
        Check(id, !Double.IsNaN(actual) && !Double.IsInfinity(actual) && Math.Abs(actual - expected) <= tolerance,
            new { actual, expected, tolerance });
    }
    static void Reject(string id, Action action) {
        bool rejected = false;
        try { action(); } catch (ArgumentException) { rejected = true; }
        Check(id, rejected, null);
    }
    static BrakeForceResult Sample(string id, BrakeRigging rig, BrakeShoeType type, double p, double v) {
        var result = ScientificBrakeForces.Compute(rig, type, p, v);
        samples.Add(new { id, profile = rig.Id, type = type.ToString(), speed_m_s = v, result });
        return result;
    }

    public static int Main() {
        int exit = 1; string error = null;
        try {
            var wagon = BrakeRigging.GostAppendixGWagon();
            var bogie = BrakeRigging.Typical2Es6Bogie670V();
            Near("gost_geometry_area_m2", wagon.PistonAreaM2, .05067074790974978, 1e-15);
            Check("gost_two_cylinders_eight_shoes", wagon.CylinderCount == 2 && wagon.ShoeCount == 8, null);
            Near("gost_spring_force_per_cylinder_N", wagon.ReturnSpringForcePerCylinderN, 1032.5, 1e-10);
            Near("gost_regulator_force_per_cylinder_N", wagon.AutoRegulatorForcePerCylinderN, 512.77, 1e-10);
            // Independent published values: GOST34434-2018 AppendixG tableG.9, minimum cases.
            var empty = Sample("G9_empty_130kPa", wagon, BrakeShoeType.Composite, 130000, 0);
            var loaded = Sample("G9_loaded_300kPa", wagon, BrakeShoeType.Composite, 300000, 0);
            Near("gost_G9_empty_Kd_6_65kN_rounded", empty.NormalForcePerShoeN / 1000, 6.65, .005);
            Near("gost_G9_loaded_Kd_18_08kN_rounded", loaded.NormalForcePerShoeN / 1000, 18.08, .005);
            var w80 = Sample("wagon_340kPa_80kmh", wagon, BrakeShoeType.Composite, 340000, 80.0 / 3.6);
            Near("wagon_piston_N", w80.PistonForcePerCylinderN, 17228.054289314925, 1e-8);
            Near("wagon_net_rod_N", w80.NetRodForcePerCylinderN, 15338.223203528625, 1e-8);
            Near("wagon_one_shoe_N", w80.NormalForcePerShoeN, 20764.119661776873, 1e-8);
            Near("wagon_total_shoe_N", w80.TotalNormalForceN, 166112.95729421498, 1e-7);
            Near("wagon_composite_mu_SI", w80.FrictionCoefficient, .25460926836653514, 1e-13);
            Near("wagon_brake_N", w80.BrakeForceN, 42293.89852288157, 1e-7);
            // At v=0, Kd=28.14kN: independently measured example in Zverev etal.2023 p451/452,
            // actual static composite mu is reported as .32 (rounded to two decimal places).
            Near("published_composite_static_28_14kN", BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.Composite, 0, 28140), .3211594573841822, 1e-12);
            var l80 = Sample("670V_bogie_300kPa_80kmh_M_reference", bogie, BrakeShoeType.PhosphorusCastIronP10, 300000, 80.0 / 3.6);
            Near("bogie_4cyl_8shoes_one_shoe_N", l80.NormalForcePerShoeN, 34758.57697524923, 1e-8);
            Near("bogie_M_reference_mu_SI", l80.FrictionCoefficient, .09977207092070163, 1e-13);
            Near("bogie_brake_N", l80.BrakeForceN, 27743.48165661786, 1e-7);
            Near("bogie_per_shoe_ratio_4_4", l80.NormalForcePerShoeN / (l80.NetRodForcePerCylinderN * .95), 4.4, 1e-13);
            Near("one_loco_16cyl_32shoes_four_equal_bogies", l80.BrakeForceN * 4, 110973.92662647144, 1e-6);
            Near("wagon_contact_threshold_Pa", wagon.ShoeContactThresholdPa, 31118.666834313288, 1e-8);
            Near("bogie_contact_threshold_Pa", bogie.ShoeContactThresholdPa, 37833.21192671393, 1e-8);
            Near("below_threshold_no_brake", Sample("below_contact", wagon, BrakeShoeType.Composite, wagon.ShoeContactThresholdPa * .99, 20).BrakeForceN, 0, 0);
            Near("zero_pressure_no_brake", Sample("released", bogie, BrakeShoeType.StandardCastIron, 0, 0).BrakeForceN, 0, 0);
            Near("reverse_same_capacity", ScientificBrakeForces.Compute(wagon, BrakeShoeType.Composite, 340000, -80.0 / 3.6).BrakeForceN, w80.BrakeForceN, 1e-9);
            double totalFour = 0; var ps = new double[] { 0, 100000, 200000, 300000 };
            for (int i = 0; i < ps.Length; i++) totalFour += ScientificBrakeForces.Compute(bogie, BrakeShoeType.StandardCastIron, ps[i], 10).BrakeForceN;
            double averaged = 4 * ScientificBrakeForces.Compute(bogie, BrakeShoeType.StandardCastIron, 150000, 10).BrakeForceN;
            Check("four_pressures_must_not_be_averaged_before_nonlinear_friction", Math.Abs(totalFour - averaged) > 1, new { totalFour, averaged });
            Near("PTR_standard_castiron_K20kN_v60", BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.StandardCastIron, 60.0 / 3.6, 20000), .12184615384615384, 1e-13);
            Near("PTR_phosphorus_K20kN_v60", BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.PhosphorusCastIronP10, 60.0 / 3.6, 20000), .12941176470588237, 1e-13);
            Near("PTR_calculated_composite_is_separate", BrakeTribologyPtr.ComputeCalculatedShoeFriction(BrakeShoeType.Composite, 80.0 / 3.6), .2670967741935484, 1e-13);
            Reject("reject_negative_pressure", delegate { ScientificBrakeForces.Compute(wagon, BrakeShoeType.Composite, -1, 0); });
            Reject("reject_NaN_pressure", delegate { ScientificBrakeForces.Compute(wagon, BrakeShoeType.Composite, Double.NaN, 0); });
            Reject("reject_infinite_speed", delegate { ScientificBrakeForces.Compute(wagon, BrakeShoeType.Composite, 100000, Double.PositiveInfinity); });
            Reject("reject_NaN_shoe_force", delegate { BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.Composite, 0, Double.NaN); });
            Reject("reject_negative_shoe_force", delegate { BrakeTribologyPtr.ComputeBrakeShoeFriction(BrakeShoeType.Composite, 0, -1); });
            Reject("reject_unknown_shoe_type", delegate { ScientificBrakeForces.Compute(wagon, (BrakeShoeType)99, 100000, 0); });
            Reject("reject_unknown_calculated_type", delegate { BrakeTribologyPtr.ComputeCalculatedShoeFriction((BrakeShoeType)0, 0); });
            Reject("reject_invalid_efficiency", delegate { new BrakeRigging("bad", .2, 1, 2, 4, 1.01, .95, 0, 0); });
            Reject("reject_fractional_shoes_per_cylinder", delegate { new BrakeRigging("bad", .2, 3, 8, 4, .98, .95, 0, 0); });
            Reject("reject_overflowing_output", delegate {
                var excessive = new BrakeRigging("overflow-check", 10, 1, 2, 100, .98, .95, 0, 0);
                ScientificBrakeForces.Compute(excessive, BrakeShoeType.Composite, Double.MaxValue, 0);
            });
            exit = 0;
        } catch (Exception ex) { error = ex.ToString(); Console.WriteLine(error); }
        finally {
            string directory = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");
            if (String.IsNullOrWhiteSpace(directory)) directory = AppDomain.CurrentDomain.BaseDirectory;
            Directory.CreateDirectory(directory);
            File.WriteAllText(Path.Combine(directory, "Trainer_scientific_brake_validation.json"), new JavaScriptSerializer { MaxJsonLength = 4000000 }.Serialize(new {
                passed = exit == 0, exit_code = exit, error, checks, samples,
                scope = "Pure SI pressure-to-shoe-to-braking-capacity arithmetic; GOST AppendixG effectiveness reference and source-based670V bogie. No native calls or train movement.",
                limits = "Generic reference profiles, not an installed wagon/OEM2ES6 calibration. Fixed stroke/spring forces; no thermal/wet/wear fit. Loco uses the published M/phosphorus reference curve; historical enumP10 does not identify OEM or UIC material. Adhesion, stiction and direction belong to the motion solver. GOSTG9 maximum anti-slide cases use different loss assumptions and are outside the fixed effectiveness profile."
            }));
        }
        Console.WriteLine(exit == 0 ? "PASS: scientific brake force arithmetic" : "FAIL: scientific brake force arithmetic");
        return exit;
    }
}
