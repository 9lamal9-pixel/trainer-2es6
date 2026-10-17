using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Debugger diagnostic only. It adds no patches and executes no air scenario.
// Ordinary candidate StartNative retains its existing guarded runtime hooks.
class NativeInitializationCaptureProbe
{
    [DllImport("kernel32.dll")] static extern bool IsDebuggerPresent();
    static readonly BindingFlags PrivateInstance = BindingFlags.Instance | BindingFlags.NonPublic;

    [STAThread]
    static int Main()
    {
        TrainerIntegrationHost host = null;
        var json = new JavaScriptSerializer();
        try
        {
            Application.EnableVisualStyles();
            // The constructor creates managed controls; do not request Handle before attachment.
            host = (TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost), true);
            int pid = Process.GetCurrentProcess().Id;
            string ready = json.Serialize(new {
                diagnostic_only = true, state = "waiting_for_debugger", pid = pid,
                session_directory = TrainerSession.DirectoryPath, debugger_wait_seconds = 20,
                native_started = false, utc = DateTime.UtcNow.ToString("O")
            });
            File.WriteAllText(TrainerSession.ResultPath("Trainer_native_initialization_capture_ready.json"), ready);
            TrainerSession.Log("Native initialization capture ready; PID=" + pid + "; awaiting debugger before Handle/StartNative");
            Console.WriteLine(ready);
            var wait = Stopwatch.StartNew();
            while (!IsDebuggerPresent() && wait.Elapsed.TotalSeconds < 20) Thread.Sleep(50);
            if (!IsDebuggerPresent())
            {
                File.WriteAllText(TrainerSession.ResultPath("Trainer_native_initialization_capture_result.json"), json.Serialize(new {
                    passed = false, diagnostic_only = true, state = "debugger_not_attached",
                    pid = pid, native_started = false, utc = DateTime.UtcNow.ToString("O")
                }));
                return 2;
            }
            TrainerSession.Log("External debugger attached; entering ordinary StartNative");
            var handle = host.Handle;
            typeof(TrainerIntegrationHost).GetMethod("StartNative", PrivateInstance).Invoke(host, null);
            var timer = (System.Windows.Forms.Timer)typeof(TrainerIntegrationHost).GetField("timer", PrivateInstance).GetValue(host);
            if (timer != null) timer.Stop();
            bool communication = (bool)typeof(TrainerIntegrationHost).GetField("commReady", PrivateInstance).GetValue(host);
            bool klub = (bool)typeof(TrainerIntegrationHost).GetField("klubReady", PrivateInstance).GetValue(host);
            bool success = communication && klub;
            string result = json.Serialize(new {
                passed = success, diagnostic_only = true, state = success ? "native_start_completed" : "native_start_failed",
                pid = pid, native_started = true, communication_ready = communication, klub_ready = klub,
                scope = "First-chance native initialization capture under an external debugger; no pneumatic scenario; not acceptance evidence",
                utc = DateTime.UtcNow.ToString("O")
            });
            File.WriteAllText(TrainerSession.ResultPath("Trainer_native_initialization_capture_result.json"), result);
            Console.WriteLine(result);
            return success ? 0 : 1;
        }
        catch (Exception error)
        {
            TrainerSession.RecordException("native-initialization-capture-probe", error);
            Console.Error.WriteLine(error);
            return 1;
        }
        finally
        {
            if (host != null) { host.Close(); host.Dispose(); }
        }
    }
}
