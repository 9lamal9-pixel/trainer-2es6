// Longitudinal dynamics of the train: every locomotive section and wagon is a body; between them an automatic coupler
// SA-3 with a free gap and two friction draft gears in series (the scheme of Blokhin and Manashkin, as in Pudovikov and
// Murov, Mir Transporta 2015 No 2):
//  - inside the gap no force;
//  - beyond it the draft gears: preload F0, loading branch F0 + k s, unloading branch eta (F0 + k s), between the
//    branches a stiff elastic transition (the force stays inside the friction loop), closed gear: stiffness of the body;
//  - viscous damping of the structure beta * relative speed outside the gap;
//  - a coupler breaks above BreakKn (tension), the brake pipe there is torn (the pneumatics vents it).
// Forces on a body: traction (locomotive), braking by PTR from its cylinder, main resistance by PTR for its kind, grade and
// curve where the body is. Braking and resistance act as friction: they stop a body and hold it, never push it back.
// Draft gear Sh-1-TM: stroke 70 mm per gear, preload 50 kN, closing force ~1.5 MN, energy ~45-55 kJ (estimates within the
// published ranges; to be checked with the passport curve).
using System;

namespace Trainer2TE116
{
    public sealed class Body
    {
        public double Mass, MassEff, Length, X, V, A;       // kg, kg, m, m (centre), m/s, m/s2
        public double Traction, Brake, Resist, Grade, Curve; // N (Brake, Resist magnitudes)
        public bool Loco;
        public double Axles, Q0;                              // axles, t per axle
    }

    public sealed class Coupler
    {
        public double Neutral, Force, S, Prev, MaxTension, MaxCompression;
        public bool Broken;
    }

    public sealed class Dynamics
    {
        public Body[] B = new Body[0];
        public Coupler[] Cp = new Coupler[0];
        public double Gap = 0.025, Stroke = 0.14, F0 = 50e3, Fclose = 1.5e6, Eta = 0.25, Kbody = 1.0e8, Beta = 4e4, BreakKn = 2500;
        public bool Cwr;
        public event Action<int> CouplerBroken;
        const double G = 9.81;

        public void Build(Train t, double headX)
        {
            int n = t.Sections + t.Wagons.Count;
            B = new Body[n];
            double x = headX;
            for (int i = 0; i < n; i++)
            {
                bool loco = i < t.Sections;
                double m = loco ? t.SectionMassT : t.Wagons[i - t.Sections].MassT, len = loco ? t.SectionLengthM : t.Wagons[i - t.Sections].Length;
                double ax = loco ? t.SectionAxles : t.Wagons[i - t.Sections].Axles;
                B[i] = new Body { Mass = m * 1000, MassEff = m * 1000 * (loco ? 1.06 : 1.04), Length = len, Loco = loco, Axles = ax, Q0 = m / ax, X = x - len / 2 };
                x -= len;
            }
            Cp = new Coupler[Math.Max(0, n - 1)];
            for (int i = 0; i + 1 < n; i++) Cp[i] = new Coupler { Neutral = B[i].X - B[i + 1].X };
        }

        public double HeadX { get { return B.Length > 0 ? B[0].X + B[0].Length / 2 : 0; } }
        public double TailX { get { return B.Length > 0 ? B[B.Length - 1].X - B[B.Length - 1].Length / 2 : 0; } }
        public double Speed { get { return B.Length > 0 ? B[0].V : 0; } }
        public double LengthM { get { return HeadX - TailX; } }

        // PTR main resistance, N/kN; below 10 km/h the value at 10, from standstill to 5 km/h from the starting resistance
        public double ResistNkN(Body b, double vKmh, bool traction)
        {
            double v = Math.Max(10, Math.Abs(vKmh)), run;
            if (b.Loco) run = traction ? (Cwr ? 1.9 + 0.008 * v + 0.00025 * v * v : 1.9 + 0.01 * v + 0.0003 * v * v)
                                       : (Cwr ? 2.4 + 0.009 * v + 0.00035 * v * v : 2.4 + 0.011 * v + 0.00035 * v * v);
            else if (b.Q0 < 6) run = 1.0 + 0.044 * v + 0.00024 * v * v;
            else run = Cwr ? 0.7 + (3 + 0.09 * v + 0.002 * v * v) / b.Q0 : 0.7 + (3 + 0.1 * v + 0.0025 * v * v) / b.Q0;
            double av = Math.Abs(vKmh);
            if (av >= 5) return run;
            double st = 28.0 / (b.Q0 + 7.0);
            return st + (run - st) * av / 5.0;
        }

        // coupler force, N (positive: tension), for an extension d of the coupling from neutral and its rate
        double CouplerForce(Coupler c, double d, double dd, double dt)
        {
            double half = Gap / 2;
            if (Math.Abs(d) <= half) { c.S = 0; c.Prev = 0; return 0; }
            double sgn = Math.Sign(d), s = Math.Abs(d) - half;
            double kl = (Fclose - F0) / Stroke;
            double load = s <= Stroke ? F0 + kl * s : Fclose + Kbody * (s - Stroke);
            double unload = s <= Stroke ? Eta * (F0 + kl * s) : Eta * Fclose + Kbody * (s - Stroke);
            // inside the friction loop: elastic from the previous force with the stiffness of the body
            double f = Math.Abs(c.Prev) + Kbody * (s - c.S);
            if (c.S == 0) f = Math.Min(load, Kbody * s);
            f = Math.Max(Math.Min(f, load), Math.Min(unload, Kbody * s));
            c.S = s; c.Prev = f;
            return sgn * f + Beta * dd;
        }

        // one step: forces of the bodies already set (Traction, Brake, Resist, Grade, Curve), dt ~ 1-2 ms
        public void Step(double dt)
        {
            int n = B.Length;
            var fc = new double[Math.Max(0, n - 1)];
            for (int i = 0; i + 1 < n; i++)
            {
                var c = Cp[i];
                if (c.Broken) { fc[i] = 0; c.Force = 0; continue; }
                double d = (B[i].X - B[i + 1].X) - c.Neutral, dd = B[i].V - B[i + 1].V;
                double f = CouplerForce(c, d, dd, dt);
                fc[i] = f; c.Force = f;
                if (f > c.MaxTension) c.MaxTension = f;
                if (-f > c.MaxCompression) c.MaxCompression = -f;
                if (f > BreakKn * 1000) { c.Broken = true; fc[i] = 0; if (CouplerBroken != null) CouplerBroken(i); }
            }
            for (int i = 0; i < n; i++)
            {
                var b = B[i];
                // active forces: traction, grade, the coupler in front (i-1) pulls the body forward when stretched, the
                // coupler behind (i) pulls it back; curve, braking and resistance act as friction below
                double active = b.Traction + b.Grade + (i > 0 ? fc[i - 1] : 0) - (i < n - 1 ? fc[i] : 0);
                double fric = b.Brake + b.Resist + b.Curve;
                double v = b.V;
                if (Math.Abs(v) > 1e-4)
                {
                    double a = (active - Math.Sign(v) * fric) / b.MassEff;
                    double nv = v + a * dt;
                    if (Math.Sign(nv) != Math.Sign(v) && Math.Abs(active) <= fric) { nv = 0; a = -v / dt; }
                    b.V = nv; b.A = a;
                }
                else
                {
                    if (Math.Abs(active) <= fric) { b.V = 0; b.A = 0; }
                    else { double a = (active - Math.Sign(active) * fric) / b.MassEff; b.V = v + a * dt; b.A = a; }
                }
                b.X += b.V * dt;
            }
        }

        // potential of the forces of a body at its place
        public void SetTrackForces(Route r, Func<int, bool> traction)
        {
            for (int i = 0; i < B.Length; i++)
            {
                var b = B[i];
                double kn = b.Mass / 1000.0 * G;                          // kN of weight
                double slope = r != null ? r.SlopeAt(b.X) : 0, rad = r != null ? r.CurveRadiusAt(b.X) : 0;
                b.Grade = -slope * kn;                                     // N: per mille * kN = N
                b.Curve = rad != 0 ? 700.0 / Math.Abs(rad) * kn : 0;
                b.Resist = ResistNkN(b, b.V * 3.6, traction(i)) * kn;
            }
        }
    }
}
