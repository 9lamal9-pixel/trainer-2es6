// Trains: wagon types (Basic_wagons of the instructor database plus the built-in ones), wagons with load, pads, air
// distributor mode, and the locomotive 2TE116. Sources:
//  - RTS/database/Program_data.db of the present instructor, read only (Composition, Wagons, Basic_wagons; VR_mode digits
//    [on][mode 1 empty / 2 medium / 3 loaded][1 plain / 2 mountain], Wagons_weight = cargo, Basic_wagons.Length in cm);
//  - own trains in trains/*.json (the editor of the instructor window, the generators below).
// Pressing per axle (tf) by PTR / Rules of brake maintenance 2025 app. 2: cast iron 7.0 / 5.0 / 3.5 (loaded / medium / empty),
// composite 4.25 / 3.0 / 1.75, composite in cast iron terms 8.5 / 7.0 / 3.5; locomotive 12.0 per axle.
using System;
using System.Collections.Generic;
using System.Data.SQLite;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Web.Script.Serialization;

namespace Trainer2TE116
{
    public sealed class WagonType
    {
        public int Id; public string Name = ""; public int Vr = 483; public double Tare = 25, Length = 14, MaxCargo = 70; public int Axles = 4;
        public override string ToString() { return Name; }
    }

    public sealed class Wagon
    {
        public int TypeId; public string TypeName = "", Number = "";
        public double Tare = 25, Cargo, Length = 14; public int Axles = 4, Vr = 483;
        public bool Composite, Mountain, Auto = true, VrOn = true, HandBrake;
        public int Mode = 2;                 // 1 empty, 2 medium, 3 loaded
        public double MassT { get { return Tare + Cargo; } }
        public double LoadPerAxle { get { return MassT / Math.Max(1, Axles); } }
        public bool Loaded { get { return Cargo >= 1; } }
        // the mode the driver / inspector should set (Rules 2025 app. 2): by the load per axle for cast iron, by load for composite
        public int ProperMode()
        {
            double q = LoadPerAxle;
            if (Composite) return q > 6.0 ? (q > 15 ? 3 : 2) : 1;
            return q > 15 ? 3 : q > 6 ? 2 : 1;
        }
        public double ModeCylinderMPa { get { return Mode == 1 ? 0.16 : Mode == 3 ? 0.42 : 0.30; } }
        public double KpAxleTf { get { return Composite ? (Mode == 3 ? 4.25 : Mode == 1 ? 1.75 : 3.0) : (Mode == 3 ? 7.0 : Mode == 1 ? 3.5 : 5.0); } }
        public double KpCastAxleTf { get { return Composite ? (Mode == 3 ? 8.5 : Mode == 1 ? 3.5 : 7.0) : KpAxleTf; } }
        public Wagon Clone() { return (Wagon)MemberwiseClone(); }
    }

    public sealed class Train
    {
        public string Name = "", Number = "2001";
        public int Sections = 2;
        public double SectionMassT = 138, SectionLengthM = 18.15;
        public int SectionAxles = 6;
        public double LocoKpAxleTf = 12.0;
        public List<Wagon> Wagons = new List<Wagon>();

        public double WagonsMassT { get { return Wagons.Sum(w => w.MassT); } }
        public double MassT { get { return WagonsMassT + Sections * SectionMassT; } }
        public int WagonAxles { get { return Wagons.Sum(w => w.Axles); } }
        public int Axles { get { return WagonAxles + Sections * SectionAxles; } }
        public double LengthM { get { return Wagons.Sum(w => w.Length) + Sections * SectionLengthM; } }
        public double EmptyShare { get { return Wagons.Count == 0 ? 0 : (double)Wagons.Count(w => !w.Loaded) / Wagons.Count; } }
        public bool IsEmpty { get { return EmptyShare > 0.5; } }
        // charging pressure by Rules 2025 table 1 (gauge, MPa): loaded 0.53 (5.4 kgf), medium 0.50 (5.1), empty 0.48 (4.9)
        public double ChargeMPa
        {
            get
            {
                if (Wagons.Count == 0) return 0.50;
                int loaded = Wagons.Count(w => w.Mode == 3), empty = Wagons.Count(w => w.Mode == 1);
                double kgf = loaded * 2 > Wagons.Count ? 5.4 : empty * 2 > Wagons.Count ? 4.9 : 5.1;
                return kgf / 10.197;
            }
        }
        // pressing of the train in cast iron terms, tf (wagons with VR on; locomotive counted when withLoco)
        public double PressingCastTf(bool withLoco)
        {
            double kp = Wagons.Where(w => w.VrOn).Sum(w => w.KpCastAxleTf * w.Axles);
            return withLoco ? kp + LocoKpAxleTf * SectionAxles * Sections : kp;
        }
        // calculated pressing per 100 t (Rules: freight loaded at least 33 tf/100 t, empty 55, in cast iron terms)
        public double PressingPer100t { get { return PressingCastTf(false) / Math.Max(1, WagonsMassT) * 100.0; } }
        public double NormPer100t { get { return IsEmpty ? 55 : 33; } }

        public override string ToString() { return string.Format(CultureInfo.InvariantCulture, "{0}: {1} ваг., {2:0} т, {3} осей", Name, Wagons.Count, MassT, Axles); }

        // ---- own trains
        public void Save(string path)
        {
            Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(path)));
            File.WriteAllText(path, new JavaScriptSerializer().Serialize(this), new System.Text.UTF8Encoding(false));
        }
        public static Train Load(string path)
        {
            var t = new JavaScriptSerializer().Deserialize<Train>(File.ReadAllText(path));
            if (t.Wagons == null) t.Wagons = new List<Wagon>();
            return t;
        }

        // uniform train of one wagon type
        public static Train Uniform(string name, WagonType type, int count, double cargo, bool composite, int mode = 0, bool mountain = false)
        {
            var t = new Train { Name = name };
            for (int i = 0; i < count; i++)
            {
                var w = new Wagon { TypeId = type.Id, TypeName = type.Name, Tare = type.Tare, Cargo = cargo, Length = type.Length, Axles = type.Axles, Vr = type.Vr, Composite = composite, Mountain = mountain };
                w.Mode = mode > 0 ? mode : w.ProperMode();
                t.Wagons.Add(w);
            }
            t.NumberWagons();
            return t;
        }
        // wagon numbers for the forms (8 digits with the check digit of the RZD numbering: weights 2,1,2,1,... sum of digits)
        public void NumberWagons(int seed = 5100000)
        {
            for (int i = 0; i < Wagons.Count; i++)
            {
                string body = (seed + i * 37).ToString("0000000", CultureInfo.InvariantCulture);
                int sum = 0;
                for (int k = 0; k < 7; k++) { int p = (body[k] - '0') * (k % 2 == 0 ? 2 : 1); sum += p / 10 + p % 10; }
                Wagons[i].Number = body + ((10 - sum % 10) % 10).ToString(CultureInfo.InvariantCulture);
            }
        }
    }

    public static class Catalog
    {
        // built-in types; the database ones are added from Basic_wagons when it is there
        public static readonly List<WagonType> Types = new List<WagonType>
        {
            new WagonType { Id = 600, Name = "Полувагон", Tare = 23, Length = 13.92, MaxCargo = 69 },
            new WagonType { Id = 748, Name = "Цистерна", Tare = 27, Length = 12.02, MaxCargo = 66 },
            new WagonType { Id = 207, Name = "Крытый", Tare = 26, Length = 15.72, MaxCargo = 68 },
            new WagonType { Id = 302, Name = "Хоппер", Tare = 23, Length = 12.10, MaxCargo = 70 },
            new WagonType { Id = 400, Name = "Платформа", Tare = 23, Length = 13.30, MaxCargo = 70 },
            new WagonType { Id = 330, Name = "Думпкар", Tare = 29, Length = 17.63, MaxCargo = 66 },
            new WagonType { Id = 928, Name = "Автомобилевоз", Tare = 30, Length = 23.30, MaxCargo = 20 },
        };
        public static WagonType Type(int id) { return Types.FirstOrDefault(t => t.Id == id) ?? Types[0]; }

        // ---- database of the present instructor, read only
        public static string LastError = "";
        public static List<string> Compositions(string db)
        {
            var res = new List<string>();
            try
            {
                using (var c = Open(db))
                using (var cmd = new SQLiteCommand("SELECT Composition_id FROM Composition", c))
                using (var r = cmd.ExecuteReader()) while (r.Read()) res.Add(r.GetValue(0).ToString());
            }
            catch (Exception ex) { LastError = ex.Message; }
            return res;
        }
        static SQLiteConnection Open(string db) { var c = new SQLiteConnection("Data Source=" + db + ";Read Only=True;FailIfMissing=True"); c.Open(); return c; }

        // area (the route of the video) of a scenario file by the table of the original launcher; -1 when it is not there
        public static int ScenarioArea(string db, string fileName)
        {
            try
            {
                if (!System.IO.File.Exists(db)) return -1;
                using (var c = Open(db))
                using (var cmd = new SQLiteCommand("SELECT FileName, area FROM Scenario", c))
                using (var rd = cmd.ExecuteReader())
                    while (rd.Read())
                        if (string.Equals(Convert.ToString(rd[0]), fileName, StringComparison.CurrentCultureIgnoreCase)) return Convert.ToInt32(rd[1], System.Globalization.CultureInfo.InvariantCulture);
            }
            catch { }
            return -1;
        }

        public static void LoadTypes(string db)
        {
            try
            {
                using (var c = Open(db))
                using (var cmd = new SQLiteCommand("SELECT Wagon_id, Name, Type_VR, Mass, Length, Axis FROM Basic_wagons", c))
                using (var r = cmd.ExecuteReader())
                    while (r.Read())
                    {
                        var t = new WagonType { Id = Convert.ToInt32(r.GetValue(0)), Name = (r.GetValue(1).ToString() ?? "").Trim().Replace('_', ' '), Vr = Convert.ToInt32(r.GetValue(2)),
                            Tare = Convert.ToDouble(r.GetValue(3)), Length = Convert.ToDouble(r.GetValue(4)) / 100.0, Axles = Convert.ToInt32(r.GetValue(5)) };
                        if (t.Vr != 483) continue;   // passenger cars are not for a freight 2TE116
                        int k = Types.FindIndex(x => x.Id == t.Id);
                        if (k >= 0) { t.MaxCargo = Types[k].MaxCargo; Types[k] = t; } else Types.Add(t);
                    }
            }
            catch (Exception ex) { LastError = ex.Message; }
        }

        public static Train FromDatabase(string db, string compositionId)
        {
            var t = new Train { Name = "Состав " + compositionId + " (база инструктора)", Number = compositionId };
            using (var c = Open(db))
            using (var cmd = new SQLiteCommand("SELECT w.Wagon_id, w.Wagons_weight, w.Brake_pads, w.VR_mode, w.Auto_mode, b.Mass, b.Length, b.Axis, b.Name, b.Type_VR FROM Wagons w LEFT JOIN Basic_wagons b ON b.Wagon_id = w.Wagon_id WHERE w.Composition_id = @id ORDER BY w.Wagons_id", c))
            {
                cmd.Parameters.AddWithValue("@id", compositionId);
                using (var r = cmd.ExecuteReader())
                    while (r.Read())
                    {
                        string digits = r.GetValue(3).ToString();
                        var w = new Wagon
                        {
                            TypeId = Convert.ToInt32(r.GetValue(0)), Cargo = Convert.ToDouble(r.GetValue(1)), Composite = Convert.ToInt32(r.GetValue(2)) == 1,
                            Auto = Convert.ToInt32(r.GetValue(4)) == 1, Tare = r.IsDBNull(5) ? 25 : Convert.ToDouble(r.GetValue(5)),
                            Length = r.IsDBNull(6) ? 14 : Convert.ToDouble(r.GetValue(6)) / 100.0, Axles = r.IsDBNull(7) ? 4 : Convert.ToInt32(r.GetValue(7)),
                            TypeName = r.IsDBNull(8) ? "" : r.GetValue(8).ToString().Trim().Replace('_', ' '), Vr = r.IsDBNull(9) ? 483 : Convert.ToInt32(r.GetValue(9)),
                            VrOn = digits.Length == 0 || digits[0] != '0', Mode = digits.Length > 1 ? digits[1] - '0' : 2, Mountain = digits.Length > 2 && digits[2] == '2'
                        };
                        if (w.Mode < 1 || w.Mode > 3) w.Mode = 2;
                        t.Wagons.Add(w);
                    }
            }
            t.NumberWagons();
            return t;
        }

        // trains of the earlier runs: 40 x 67 t (bench), 71 empty, 6300 t loaded, 5600 t loaded
        public static List<Train> BuiltIn()
        {
            return new List<Train>
            {
                Train.Uniform("40 цистерн по 67 т (стенд)", Type(748), 40, 40, false, 2),
                Train.Uniform("71 порожний полувагон, композит", Type(600), 71, 0, true),
                Train.Uniform("68 полувагонов 6300 т, композит", Type(600), 68, 69.6, true),
                Train.Uniform("60 полувагонов 5600 т, чугун", Type(600), 60, 70.3, false),
            };
        }
    }
}
