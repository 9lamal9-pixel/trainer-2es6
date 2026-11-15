using System;
using System.Linq;
using System.IO;
using System.Text;
using System.Security.Cryptography;
using System.Web.Script.Serialization;
using Trainer2TE116;

static class ExportScheme
{
    static void Main(string[] args)
    {
        var s = new Scheme(SchemeData.Text);
        if (s.Errors.Count > 0) throw new Exception(string.Join("\n", s.Errors));
        string signature;
        using (var sha = SHA256.Create()) signature = BitConverter.ToString(sha.ComputeHash(Encoding.UTF8.GetBytes(SchemeData.Text))).Replace("-", "").ToLowerInvariant();
        var data = new { modelSignature = signature, elements = s.Els.Select(e => new {
            id = e.Id, name = e.Name, key = e.Key, kind = e.Kind.ToString(), sec = e.Sec, cab = e.Rung.Cab,
            rung = e.Rung.Name, a = s.NodeName(e.A), b = s.NodeName(e.B), sheet = e.Rung.Sheet,
            title = e.Title, source = e.Rung.Src
        }).ToArray() };
        File.WriteAllText(args[0], new JavaScriptSerializer { MaxJsonLength = 10000000 }.Serialize(data), new UTF8Encoding(false));
        Console.WriteLine("EXPORTED " + s.Els.Count + " elements, " + signature);
    }
}
