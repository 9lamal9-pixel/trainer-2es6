using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Security.Cryptography;
using System.Text.RegularExpressions;
using System.Web.Script.Serialization;

// Pure byte generation: never construct the patch, invoke Install/SetVoltage/
// Validate/Dispose, allocate executable memory, or load trainer native DLLs.
public static class ContactVoltageCaveByteGenerator {
    const string ExpectedSha = "267D313214901309B98A6A0BE00FD784D75C25DEF0DC2A473801009654DFAE57";
    static string Hash(string path) {
        using (var f=File.OpenRead(path)) using(var h=SHA256.Create())
            return BitConverter.ToString(h.ComputeHash(f)).Replace("-", "");
    }
    static string Hex(byte[] bytes) { return BitConverter.ToString(bytes).Replace("-", ""); }
    public static int Main(string[] args) {
        if(args.Length!=2 || IntPtr.Size!=4) throw new Exception("source output-directory; x86 required");
        string source=Path.GetFullPath(args[0]), output=Path.GetFullPath(args[1]);
        if(Hash(source)!=ExpectedSha) throw new Exception("Final source SHA changed");
        Directory.CreateDirectory(output);
        var type=typeof(TrainerContactVoltagePatch);
        var flags=BindingFlags.Public|BindingFlags.NonPublic|BindingFlags.Instance;
        object patch=FormatterServices.GetUninitializedObject(type);
        var pointers=new Dictionary<string,IntPtr> {
            {"pultModule",new IntPtr(0x10000000)}, {"tygaModule",new IntPtr(0x20000000)},
            {"pult",new IntPtr(0x30000000)}, {"tyga",new IntPtr(0x30001000)},
            {"pultOwner",new IntPtr(0x30010000)}, {"calc",new IntPtr(0x30020000)},
            {"schema",new IntPtr(0x30010018)}, {"result",new IntPtr(0x30030000)},
            {"data",new IntPtr(0x40000000)}, {"eset",new IntPtr(0x300403fc)}
        };
        foreach(var pair in pointers) type.GetField(pair.Key,flags).SetValue(patch,pair.Value);
        type.GetField("thread",flags).SetValue(patch,(uint)0x12345678);
        var make=type.GetMethod("Make",BindingFlags.Static|BindingFlags.NonPublic);
        var build=type.GetMethod("BuildCave",flags);
        var jump=type.GetMethod("Jump",BindingFlags.Static|BindingFlags.NonPublic);
        string pattern="Make\\(\"(?<name>[^\"]+)\",(?<index>[0-9]+),(?<module>pultModule|tygaModule),(?<owner>pultOwner|calc),(?<destination>schema|result),0x(?<rva>[0-9a-f]+),0x(?<window>[0-9a-f]+),0x(?<member>[0-9a-f]+),(?<floating>true|false),(?<sequential>true|false),\"(?<original>[0-9a-f]+)\",\"(?<windowbytes>[0-9a-f]+)\"\\)";
        var matches=Regex.Matches(File.ReadAllText(source),pattern,RegexOptions.IgnoreCase);
        if(matches.Count!=5) throw new Exception("Exactly five final Make recipes required");
        var rows=new List<object>();
        foreach(Match m in matches) {
            Func<string,int> number=k=>Convert.ToInt32(m.Groups[k].Value,16);
            int index=Int32.Parse(m.Groups["index"].Value),rva=number("rva");
            var module=pointers[m.Groups["module"].Value];
            object site=make.Invoke(null,new object[] { m.Groups["name"].Value,index,module,
                pointers[m.Groups["owner"].Value],pointers[m.Groups["destination"].Value],
                rva,number("window"),number("member"),Boolean.Parse(m.Groups["floating"].Value),
                Boolean.Parse(m.Groups["sequential"].Value),m.Groups["original"].Value,m.Groups["windowbytes"].Value });
            var st=site.GetType();
            IntPtr cave=new IntPtr(0x50000000+index*0x10000);
            st.GetField("Cave",flags).SetValue(site,cave);
            byte[] original=(byte[])st.GetField("Original",flags).GetValue(site);
            byte[] bytes=(byte[])build.Invoke(patch,new object[]{site});
            byte[] entry=(byte[])jump.Invoke(null,new object[]{IntPtr.Add(module,rva),cave,original.Length});
            string name=m.Groups["name"].Value;
            File.WriteAllBytes(Path.Combine(output,name+".bin"),bytes);
            rows.Add(new { name,index,module_base=module.ToInt32(),owner=pointers[m.Groups["owner"].Value].ToInt32(),
                destination=pointers[m.Groups["destination"].Value].ToInt32(),rva,window_rva=number("window"),
                member_offset=number("member"),floating_store=Boolean.Parse(m.Groups["floating"].Value),
                sequential=Boolean.Parse(m.Groups["sequential"].Value),original_hex=Hex(original),entry_jump_hex=Hex(entry),
                cave=cave.ToInt32(),continuation=module.ToInt32()+rva+original.Length,
                length=bytes.Length,hex=Hex(bytes),binary_sha256=Hash(Path.Combine(output,name+".bin")) });
        }
        string[] forbidden={"Tyga1.dll","Pult1.dll","Communication1.dll","POEZD.DLL","TormModel.dll"};
        var loaded=Process.GetCurrentProcess().Modules.Cast<ProcessModule>().Select(x=>x.ModuleName).ToArray();
        if(loaded.Any(x=>forbidden.Contains(x,StringComparer.OrdinalIgnoreCase))) throw new Exception("Trainer native module unexpectedly loaded");
        var pointerValues=pointers.ToDictionary(x=>x.Key,x=>x.Value.ToInt32());
        File.WriteAllText(Path.Combine(output,"generated-caves.json"),new JavaScriptSerializer().Serialize(new {
            schema_version=1,source_sha256=Hash(source),source_last_write_utc=File.GetLastWriteTimeUtc(source),
            x86=true,constructor_invoked=false,trainer_native_loaded=false,native_code_executed=false,
            fake_addresses=pointerValues,fake_thread=0x12345678,sites=rows,
            allowed_invocations=new[]{"FormatterServices.GetUninitializedObject","reflection field assignment","Make (managed)","BuildCave (managed)","Jump (managed)"},
            scope="Generated bytes at fake addresses only; no instruction emulation or live/native behavioral acceptance."
        }));
        Console.WriteLine("Generated exactly five managed byte blocks; trainer native modules absent.");
        return 0;
    }
}
