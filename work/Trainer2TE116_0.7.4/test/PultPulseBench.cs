// Real UDP input regression: button press and release both arrive before the model reads the latest packet.
// Compile separately with /platform:x64 /r:Trainer2TE116.exe (C# 5 / .NET Framework).
using System;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using Trainer2TE116;

static class PultPulseBench
{
    static int pass, fail;
    static void Check(bool ok, string name)
    {
        if (!ok) { fail++; throw new InvalidOperationException(name); }
        pass++; Console.WriteLine("PASS " + name);
    }
    static void Send(UdpClient sender, IPEndPoint target, Pult pult, byte[] packet)
    {
        long before = Interlocked.Read(ref pult.Received);
        sender.Send(packet, packet.Length, target);
        var clock = Stopwatch.StartNew();
        while (Interlocked.Read(ref pult.Received) <= before && clock.ElapsedMilliseconds < 3000) Thread.Sleep(2);
        Check(Interlocked.Read(ref pult.Received) > before, "UDP packet received before next test action");
    }
    static void Buttons(byte[] packet, byte value)
    { packet[Pult.CEmergencyStop] = packet[Pult.CLaunch1] = packet[Pult.CLaunch2] = value; }
    static bool Pulses(Controls c) { return c.EmergencyStop && c.Launch[0] && c.Launch[1]; }
    static bool NoPulses(Controls c) { return !c.EmergencyStop && !c.Launch[0] && !c.Launch[1]; }
    static bool Held(Controls c) { return c.EmergencyHeld && c.StartHeld1 && c.StartHeld2; }
    static bool Released(Controls c) { return !c.EmergencyHeld && !c.StartHeld1 && !c.StartHeld2; }
    static void Consumed(Controls c) { c.EmergencyStop = c.Launch[0] = c.Launch[1] = false; }

    static int Main()
    {
        try
        {
            int localPort;
            using (var probe = new UdpClient(new IPEndPoint(IPAddress.Loopback, 0)))
                localPort = ((IPEndPoint)probe.Client.LocalEndPoint).Port;
            using (var sender = new UdpClient(new IPEndPoint(IPAddress.Loopback, 0)))
            using (var pult = new Pult { Enabled = true, Ip = "127.0.0.1", LocalPort = localPort,
                Port = ((IPEndPoint)sender.Client.LocalEndPoint).Port })
            {
                pult.Start(); Check(pult.Bound, "real UDP Pult.Start binds");
                var target = new IPEndPoint(IPAddress.Loopback, localPort);
                var packet = new byte[Pult.InSize];
                packet[Pult.CCrane] = 2; packet[Pult.CKvtAnalog] = 148;
                packet[Pult.CPump1] = packet[Pult.CPump2] = 1;
                Buttons(packet, 1); Send(sender, target, pult, packet);
                var c = new Controls(); pult.Apply(c);
                Check(NoPulses(c) && Held(c), "first received full packet supplies held levels without synthetic fronts");
                Buttons(packet, 0); Send(sender, target, pult, packet); pult.Apply(c);
                Check(NoPulses(c) && Released(c), "button release alone does not dispatch a command");

                // No Apply between these two received packets: this is the lost-pulse regression.
                Buttons(packet, 1); packet[Pult.CController] = 3; Send(sender, target, pult, packet);
                Buttons(packet, 0); packet[Pult.CController] = 6; packet[Pult.CCrane] = 4;
                Send(sender, target, pult, packet);
                Check(pult.Apply(c) && Pulses(c), "released START 1 / START 2 / emergency pulses each reach Apply");
                Check(Released(c) && c.Position == 6 && c.Crane == 4 && c.FuelPump[0] && c.FuelPump[1],
                    "held levels and ordinary controls use the newest released packet");
                Consumed(c); Check(!pult.Apply(c) && NoPulses(c), "next Apply does not repeat consumed fronts");
                c.Crane = 3; pult.Apply(c);
                Check(c.Crane == 3, "unchanged physical handle preserves the instructor UI override");

                Buttons(packet, 1); Send(sender, target, pult, packet); pult.Apply(c);
                Check(Pulses(c) && Held(c), "fresh press dispatches and retains its current held level");
                Consumed(c); Send(sender, target, pult, packet); pult.Apply(c);
                Check(NoPulses(c) && Held(c), "repeated pressed packets do not create extra fronts");
                Buttons(packet, 0); Send(sender, target, pult, packet); pult.Apply(c);

                Buttons(packet, 1); Send(sender, target, pult, packet); // pending old-trip fronts
                pult.Reset(); c = new Controls();
                Check(!pult.Apply(c) && NoPulses(c), "Reset drops old pending fronts and awaits a fresh packet");
                Send(sender, target, pult, packet); pult.Apply(c);
                Check(NoPulses(c) && Held(c) && c.PultFirst, "first packet of new trip is a level snapshot, not an old edge");
                Buttons(packet, 0); Send(sender, target, pult, packet); pult.Apply(c);

                // A front after the first receive must survive even if the first Apply has not happened yet.
                pult.Reset(); c = new Controls();
                Send(sender, target, pult, packet);
                Buttons(packet, 1); Send(sender, target, pult, packet);
                Buttons(packet, 0); Send(sender, target, pult, packet);
                Check(pult.Apply(c) && Pulses(c) && Released(c) && c.PultFirst,
                    "fronts after first receive survive until the first Apply");
                Consumed(c); Check(!pult.Apply(c) && NoPulses(c), "first-Apply delivery also consumes fronts exactly once");
            }
        }
        catch (Exception ex) { if (fail == 0) fail++; Console.WriteLine("FAIL " + ex.Message); }
        Console.WriteLine("PULT PULSE RESULT pass " + pass + " fail " + fail);
        return fail == 0 ? 0 : 1;
    }
}
