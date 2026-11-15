// Cassette of the trip, like the KLUB-U registration cassette: parameters every frame (0.1 s) and the events with time and
// coordinate. File *.kr2: "KR2TE116", version, JSON header (route, train, start), then blocks
//   [type 1 frames | 2 event][payload length][payload][CRC-32 of the payload],
// a block of frames every 5 s and every event at once, flushed to disk, so a crash loses at most 5 s. A block whose CRC does
// not match is reported by the reader (the record was changed or damaged); export to CSV for own analysis.
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Text;
using System.Web.Script.Serialization;

namespace Trainer2TE116
{
    public struct CassetteFrame
    {
        public float T, X, V, Allowed, Tm, Ur, TcLoco, Pm, TailTm, TailTc, Force, Tension, Compression;
        public byte Position, Crane, Kvt, Reverser, Flags;   // flags: 1 EPK key, 2 sand, 4 typhon, 8 whistle, 16 spotlight, 32 EPK tripped
        public const int Size = 13 * 4 + 5;
    }

    public sealed class Cassette : IDisposable
    {
        public const string Magic = "KR2TE116";
        readonly FileStream fs; readonly List<CassetteFrame> pending = new List<CassetteFrame>();
        public string Path;
        public int Frames, EventsCount;

        public Cassette(string path, Dictionary<string, object> header)
        {
            Path = path;
            Directory.CreateDirectory(System.IO.Path.GetDirectoryName(System.IO.Path.GetFullPath(path)));
            fs = new FileStream(path, FileMode.Create, FileAccess.Write, FileShare.Read);
            var h = Encoding.UTF8.GetBytes(new JavaScriptSerializer().Serialize(header));
            var w = new BinaryWriter(fs, Encoding.UTF8, true);
            w.Write(Encoding.ASCII.GetBytes(Magic)); w.Write(1); w.Write(h.Length); w.Write(h); w.Write(Crc32(h, 0, h.Length));
            w.Flush(); fs.Flush(true);
        }

        public void Frame(Simulation s)
        {
            int tAt, cAt;
            var f = new CassetteFrame
            {
                T = (float)s.T, X = (float)s.Dyn.HeadX, V = (float)s.SpeedKmh, Allowed = (float)s.Allowed, Tm = (float)s.Air.TmLoco.MPa, Ur = (float)s.Air.Ur.MPa,
                TcLoco = (float)s.Air.LocoTcMPa, Pm = (float)s.Air.Mr.MPa, TailTm = (float)s.Air.TailTmMPa, TailTc = (float)s.Air.TailTcMPa, Force = (float)s.Tr.TotalForceKn,
                Tension = (float)s.MaxCouplerKn(out tAt, true), Compression = (float)s.MaxCouplerKn(out cAt, false),
                Position = (byte)s.C.Position, Crane = (byte)s.C.Crane, Kvt = (byte)s.C.Kvt, Reverser = (byte)s.C.Reverser,
                Flags = (byte)((s.C.EpkKey ? 1 : 0) | (s.Tr.Sanding ? 2 : 0) | (s.C.Typhon ? 4 : 0) | (s.C.Whistle ? 8 : 0) | (s.Tr.SpotlightOn ? 16 : 0) | (s.Air.EpkTripped ? 32 : 0))
            };
            pending.Add(f); Frames++;
            if (pending.Count >= 50) FlushFrames();
        }

        public void Event(double t, double x, string text)
        {
            FlushFrames();
            var b = Encoding.UTF8.GetBytes(text ?? "");
            var ms = new MemoryStream(); var w = new BinaryWriter(ms);
            w.Write((float)t); w.Write((float)x); w.Write(b.Length); w.Write(b);
            Block(2, ms.ToArray()); EventsCount++;
        }

        void FlushFrames()
        {
            if (pending.Count == 0) return;
            var ms = new MemoryStream(); var w = new BinaryWriter(ms);
            foreach (var f in pending)
            {
                foreach (var v in new[] { f.T, f.X, f.V, f.Allowed, f.Tm, f.Ur, f.TcLoco, f.Pm, f.TailTm, f.TailTc, f.Force, f.Tension, f.Compression }) w.Write(v);
                w.Write(f.Position); w.Write(f.Crane); w.Write(f.Kvt); w.Write(f.Reverser); w.Write(f.Flags);
            }
            pending.Clear();
            Block(1, ms.ToArray());
        }

        void Block(byte type, byte[] p)
        {
            lock (fs)
            {
                var w = new BinaryWriter(fs, Encoding.UTF8, true);
                w.Write(type); w.Write(p.Length); w.Write(p); w.Write(Crc32(p, 0, p.Length));
                w.Flush(); fs.Flush(true);
            }
        }

        public void Flush() { FlushFrames(); }
        public void Dispose() { try { FlushFrames(); } catch { } fs.Dispose(); }

        // ---- reading (decoding)
        public sealed class Record
        {
            public Dictionary<string, object> Header = new Dictionary<string, object>();
            public List<CassetteFrame> Frames = new List<CassetteFrame>();
            public List<Tuple<float, float, string>> Events = new List<Tuple<float, float, string>>();
            public int Blocks, BadBlocks; public bool HeaderOk;
        }
        public static Record Read(string path)
        {
            var rec = new Record();
            using (var r = new BinaryReader(new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.ReadWrite)))
            {
                var magic = Encoding.ASCII.GetString(r.ReadBytes(8));
                if (magic != Magic) throw new InvalidDataException("не кассета 2ТЭ116");
                r.ReadInt32(); int hl = r.ReadInt32(); var h = r.ReadBytes(hl); uint hc = r.ReadUInt32();
                rec.HeaderOk = hc == Crc32(h, 0, h.Length);
                rec.Header = new JavaScriptSerializer().Deserialize<Dictionary<string, object>>(Encoding.UTF8.GetString(h));
                while (r.BaseStream.Position < r.BaseStream.Length)
                {
                    try
                    {
                        byte type = r.ReadByte(); int len = r.ReadInt32(); var p = r.ReadBytes(len); uint c = r.ReadUInt32();
                        rec.Blocks++;
                        if (p.Length != len || c != Crc32(p, 0, p.Length)) { rec.BadBlocks++; continue; }
                        var br = new BinaryReader(new MemoryStream(p));
                        if (type == 1)
                            while (br.BaseStream.Position + CassetteFrame.Size <= p.Length)
                            {
                                var f = new CassetteFrame
                                {
                                    T = br.ReadSingle(), X = br.ReadSingle(), V = br.ReadSingle(), Allowed = br.ReadSingle(), Tm = br.ReadSingle(), Ur = br.ReadSingle(), TcLoco = br.ReadSingle(),
                                    Pm = br.ReadSingle(), TailTm = br.ReadSingle(), TailTc = br.ReadSingle(), Force = br.ReadSingle(), Tension = br.ReadSingle(), Compression = br.ReadSingle(),
                                    Position = br.ReadByte(), Crane = br.ReadByte(), Kvt = br.ReadByte(), Reverser = br.ReadByte(), Flags = br.ReadByte()
                                };
                                rec.Frames.Add(f);
                            }
                        else if (type == 2) { float t = br.ReadSingle(), x = br.ReadSingle(); int n = br.ReadInt32(); rec.Events.Add(Tuple.Create(t, x, Encoding.UTF8.GetString(br.ReadBytes(n)))); }
                    }
                    catch (EndOfStreamException) { rec.BadBlocks++; break; }
                }
            }
            return rec;
        }

        public static void ExportCsv(Record rec, string path)
        {
            var c = CultureInfo.InvariantCulture;
            using (var w = new StreamWriter(path, false, new UTF8Encoding(true)))
            {
                w.WriteLine("t_s;x_m;v_kmh;allowed_kmh;tm_mpa;ur_mpa;tc_loco_mpa;pm_mpa;tail_tm_mpa;tail_tc_mpa;force_kn;max_tension_kn;max_compression_kn;position;crane;kvt;reverser;flags");
                foreach (var f in rec.Frames)
                    w.WriteLine(string.Join(";", new[] { f.T.ToString("0.0", c), f.X.ToString("0.0", c), f.V.ToString("0.00", c), f.Allowed.ToString("0", c), f.Tm.ToString("0.000", c), f.Ur.ToString("0.000", c),
                        f.TcLoco.ToString("0.000", c), f.Pm.ToString("0.000", c), f.TailTm.ToString("0.000", c), f.TailTc.ToString("0.000", c), f.Force.ToString("0.0", c), f.Tension.ToString("0", c),
                        f.Compression.ToString("0", c), f.Position.ToString(c), f.Crane.ToString(c), f.Kvt.ToString(c), f.Reverser.ToString(c), f.Flags.ToString(c) }));
            }
        }

        static uint[] table;
        public static uint Crc32(byte[] b, int off, int len)
        {
            if (table == null)
            {
                var t = new uint[256];
                for (uint i = 0; i < 256; i++) { uint c = i; for (int k = 0; k < 8; k++) c = (c & 1) != 0 ? 0xEDB88320u ^ (c >> 1) : c >> 1; t[i] = c; }
                table = t;
            }
            uint crc = 0xFFFFFFFFu;
            for (int i = off; i < off + len; i++) crc = table[(crc ^ b[i]) & 0xFF] ^ (crc >> 8);
            return ~crc;
        }
    }
}
