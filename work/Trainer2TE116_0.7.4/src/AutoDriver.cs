// Automatic driver for the bench runs: drives by the Rules the way the journal checks them (stage by the norm, lap with
// feed at least 9 s, release with an overcharge of 0.04 MPa in trains over 100 axles, traction not before 65 s after the
// release, sand before a reduction over 0.10 MPa, brake test at the signs "НТ"), keeps 3 km/h under the allowed speed,
// brakes ahead of lower limits and of the stop at the last station on a curve with 0.12 m/s2.
using System;

namespace Trainer2TE116
{
    public sealed class AutoDriver
    {
        enum Ph { Charging, Run, Braking, Holding }
        Ph ph = Ph.Charging;
        public double Cruise = 75, Decel = 0.12;
        public double StopX = -1;               // stop of the head (default: the last station)
        public bool BrakeTests = true, Done;
        public string State = "";
        double hornUntil = -1e9, tractionWait = 65, tPh, urStart, depthWanted, lastPosT, releaseT = -1e9, lastSignX = double.NaN, bt0 = -1;
        bool releasing, testing; int stages;

        static bool AllUnassembled(Simulation s)
        {
            bool any = false;
            for (int i = 0; i < s.Tr.Sections; i++) { if (!s.Tr.S[i].Running) continue; any = true; if (s.Tr.S[i].Assembled) return false; }
            return any;
        }

        public void Step(Simulation s)
        {
            var c = s.C; double t = s.T, v = s.SpeedKmh, head = s.Dyn.HeadX, ur = s.Air.Ur.MPa, charge = s.Air.ChargeMPa;
            if (StopX < 0) StopX = s.Route.StationX.Length > 0 ? Math.Min(s.Route.Length - 50, s.Route.StationX[s.Route.StationX.Length - 1] + 300) : s.Route.Length - 100;
            int axles = s.Train.WagonAxles; bool empty = s.Train.IsEmpty;
            double need = RequiredSpeed(s, head);
            // release in progress: I until charge + 0.04 (over 100 axles), then II
            if (releasing)
            {
                c.Crane = ur >= charge + (axles > 100 ? 0.04 : 0.0) - 0.002 ? 2 : 1;
                if (c.Crane == 2) releasing = false;
            }
            c.Sand = false;
            // typhon from 300 m before a sign "С" (whistle)
            bool horn = false;
            foreach (var sg in s.Route.Signs) if (sg.Type == Route.SignWhistle && sg.X > head && sg.X - head < 250) { horn = true; break; }
            c.Typhon = horn;
            switch (ph)
            {
                case Ph.Charging:
                    c.Position = 0; c.Kvt = 6; c.Crane = 2; c.Spotlight = true;
                    if (s.Air.TailTmMPa >= charge - 0.02 && t > 10) { c.Kvt = 2; c.Whistle = true; if (s.Air.LocoTcMPa < 0.03) { c.Whistle = false; ph = Ph.Run; tPh = t; } }
                    break;
                case Ph.Run:
                    // brake test at the sign "НТ" (start of braking)
                    if (BrakeTests && !testing)
                        foreach (var sg in s.Route.Signs)
                            // from a low speed a loaded train is tested by the smallest stage (0.06), not to stop under the release
                            if (sg.Type == Route.SignBrakeStart && sg.X <= head && sg.X > head - 30 && sg.X != lastSignX && v >= 20)
                            { lastSignX = sg.X; testing = true; bt0 = v; StartBraking(s, empty ? 0.05 : v < 40 ? 0.06 : 0.07); break; }
                    if (ph == Ph.Braking) break;
                    if (v > need + 2.5 && head < StopX + 50) { StartBraking(s, empty ? 0.05 : 0.065); break; }
                    // traction
                    // traction: 65 s after a release in motion (10.3.1 k), 125 s after a release at a stop (10.3.1 r)
                    bool allowedTraction = t - releaseT >= tractionWait && s.Air.TailTcMPa < 0.02 && !releasing && head < StopX - 30;
                    int want = !allowedTraction ? 0 : need - v > 6 ? 15 : need - v > 3 ? Math.Min(15, c.Position + 1) : need - v > 0.5 ? c.Position : Math.Max(0, c.Position - 2);
                    // standing without traction: hold the train with the 254, release it when the traction starts
                    if (v < 0.5 && want == 0) c.Kvt = 4; else if (want > 0) c.Kvt = 2;
                    if (v < 0.5 && want > 0 && t > hornUntil + 20) hornUntil = t + 3;     // sound signal before moving off
                    // the circuit did not assemble (a protection held it on the 1st position): back to 0, then from the 1st again
                    if (c.Position >= 2 && t - lastPosT >= 1.0 && AllUnassembled(s)) { c.Position = 0; lastPosT = t; }
                    if (t - lastPosT >= 1.0 && want != c.Position) { c.Position += Math.Sign(want - c.Position); lastPosT = t; }
                    if (head >= StopX - 30 && v < 0.3) { ph = Ph.Holding; c.Position = 0; }
                    break;
                case Ph.Braking:
                    c.Position = 0;
                    double depth = urStart - ur;
                    if (depthWanted > 0.10 || depth > 0.08) c.Sand = v > 10;
                    if (c.Crane == 5 && depth >= depthWanted - 0.003) { c.Crane = 4; tPh = t; }
                    if (c.Crane == 4 && t - tPh >= 9)
                    {
                        bool stopping = head > StopX - 1500;
                        if (testing)
                        {
                            if (v <= bt0 - (empty ? 4 : 10) - 1.0 || v < 5) { testing = false; Release(s); }
                        }
                        else if (stopping)
                        {
                            if (v < 0.3 && head > StopX - 200) { ph = Ph.Holding; c.Kvt = 5; }
                            else if (v < 0.3) { c.Kvt = 4; if (t - tPh >= 20) Release(s); }
                            // the train would stop far before the place: release in motion and come up to it again
                            else if (v > 15 && v * v / (12.96 * 2 * 0.27) < StopX - head - 300) Release(s);
                            else if (v > need + 2 && depth < 0.14 && t - tPh >= 15 && stages < 4) { depthWanted = depth + 0.035; stages++; c.Crane = 5; }
                        }
                        else if (v <= need - 4 && !(axles > 300 && v < 20)) Release(s);
                        else if (v > need + 4 && t - tPh >= 15 && depth < 0.14 && stages < 4) { depthWanted = depth + 0.035; stages++; c.Crane = 5; }
                        else if (v < 0.3) { Release(s); }
                    }
                    break;
                case Ph.Holding:
                    c.Position = 0; c.Kvt = 5; Done = true;
                    break;
            }
            State = ph + (testing ? " (проба тормозов)" : "") + string.Format(" V={0:0} надо {1:0}", v, need);
            if (ph == Ph.Run) c.Whistle = t < hornUntil;
        }

        void StartBraking(Simulation s, double stage)
        {
            ph = Ph.Braking; urStart = s.Air.Ur.MPa; depthWanted = stage; stages = 1; s.C.Crane = 5; s.C.Position = 0; releasing = false;
        }
        void Release(Simulation s)
        {
            ph = Ph.Run; releasing = true; s.C.Crane = 1; releaseT = s.T; tractionWait = Math.Abs(s.SpeedKmh) < 0.5 ? 125 : 65;
        }

        // speed needed now: the allowed one minus 3, the cruise, and braking curves to the lower limits ahead and to the stop
        double RequiredSpeed(Simulation s, double head)
        {
            double need = Math.Min(Cruise, s.Allowed - 3);
            var r = s.Route;
            for (double d = 100; d <= 4000; d += 100)
            {
                double x = head + d, lim = r.StaticLimitAt(x) - 3;
                need = Math.Min(need, 3.6 * Math.Sqrt(Math.Pow(lim / 3.6, 2) + 2 * Decel * d));
            }
            double ds = StopX - head;
            if (ds < 6000) need = Math.Min(need, ds <= 0 ? 0 : 3.6 * Math.Sqrt(2 * Decel * ds));
            return Math.Max(0, need);
        }
    }
}
