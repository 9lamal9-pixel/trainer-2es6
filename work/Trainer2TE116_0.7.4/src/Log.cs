// Journal of the work of the program and of its links (the pult of the cab, the video system, BLOK): a text file
// data\logs\trainer_<date>_<time>.log, one per start, UTF-8, a line = local time with milliseconds, a section, a text.
// What is written: the start (version, keys, settings, folders, addresses of the computer), the settings and the state
// of every link, the first packet of the pult and its sender, the rejected packets, every changed cell of the pult
// (number, name, old and new value), the changes of the lamps and a dump of the needles sent to the pult, the statistics
// of the exchange every 10 s (packets per second, the longest gap), the errors of the sockets, the trips, the events of
// a trip, the state of the model every 10 s and the unexpected errors.
// settings.json "log" (or the key --log): "on" (default), "raw" (also every changed packet of the pult in hex), "off".
// The 20 newest journals are kept; a journal stops at 50 MB. Nothing is written until Open is called (the benches).
using System;
using System.Globalization;
using System.IO;
using System.Text;

namespace Trainer2TE116
{
    public static class AppLog
    {
        const long MaxBytes = 50L * 1024 * 1024; const int Keep = 20;
        static readonly object gate = new object();
        static StreamWriter w; static long bytes;
        public static bool Raw;
        public static string FilePath = "";
        public static bool Enabled { get { return w != null; } }

        public static void Open(string dir, string mode)
        {
            mode = (mode ?? "on").Trim().ToLowerInvariant();
            if (mode == "off" || mode == "0") return;
            Raw = mode == "raw" || mode == "full";
            try
            {
                Directory.CreateDirectory(dir);
                try
                {
                    var old = Directory.GetFiles(dir, "trainer_*.log"); Array.Sort(old, StringComparer.Ordinal);
                    for (int i = 0; i < old.Length - (Keep - 1); i++) File.Delete(old[i]);
                }
                catch { }
                FilePath = Path.Combine(dir, "trainer_" + DateTime.Now.ToString("yyyyMMdd_HHmmss", CultureInfo.InvariantCulture) + ".log");
                var fs = new FileStream(FilePath, FileMode.Append, FileAccess.Write, FileShare.ReadWrite);   // can be read while the program works
                lock (gate) { w = new StreamWriter(fs, new UTF8Encoding(true)); bytes = 0; }
            }
            catch { w = null; }
        }

        public static void Write(string section, string text)
        {
            if (w == null) return;
            string line = DateTime.Now.ToString("HH:mm:ss.fff", CultureInfo.InvariantCulture) + "  " + (section ?? "").PadRight(10) + "  " + (text ?? "").Replace("\r", "").Replace("\n", " | ");
            lock (gate)
            {
                if (w == null) return;
                try
                {
                    w.WriteLine(line); w.Flush(); bytes += line.Length * 2;
                    if (bytes > MaxBytes) { w.WriteLine("журнал остановлен: достигнут предел размера"); w.Flush(); w.Dispose(); w = null; }
                }
                catch { }
            }
        }
        public static void Write(string section, string format, params object[] args) { if (w != null) Write(section, string.Format(CultureInfo.InvariantCulture, format, args)); }

        public static string Hex(byte[] b)
        {
            if (b == null) return "(нет)";
            var sb = new StringBuilder(b.Length * 3);
            for (int i = 0; i < b.Length && i < 256; i++) { if (i > 0) sb.Append(i % 8 == 0 ? "  " : " "); sb.Append(b[i].ToString("X2")); }
            if (b.Length > 256) sb.Append(" …");
            return sb.ToString();
        }

        public static void Close()
        {
            lock (gate) { try { if (w != null) { w.Flush(); w.Dispose(); } } catch { } w = null; }
        }
    }
}
