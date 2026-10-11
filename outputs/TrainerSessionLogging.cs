using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;
using System.Windows.Forms;

// Shared by the normal host and diagnostic entry points. No native/model calls.
static class TrainerSession
{
    static readonly object Gate = new object();
    static readonly Encoding Utf8 = new UTF8Encoding(false);
    static readonly JavaScriptSerializer Json = new JavaScriptSerializer();
    static readonly string SessionId = DateTime.UtcNow.ToString("yyyyMMddTHHmmssfffZ") + "_p" + Process.GetCurrentProcess().Id + "_" + Guid.NewGuid().ToString("N");
    static readonly string StartedUtc = DateTime.UtcNow.ToString("O");
    static string sessionDirectory;
    static string executablePath;
    static string executableHash;
    static string sourceBuildId;
    static string buildManifestPath;
    static string telemetryHeader;
    static bool started, finished;
    static int exceptionCount;
    static int loggingFailureCount;

    public static string DirectoryPath { get { EnsureStarted(); return sessionDirectory; } }
    public static string LogPath { get { return Path.Combine(DirectoryPath, "integration.log"); } }
    public static string TelemetryPath { get { return Path.Combine(DirectoryPath, "telemetry.csv"); } }
    public static string ResultPath(string fileName)
    {
        if (String.IsNullOrEmpty(fileName) || Path.GetFileName(fileName) != fileName)
            throw new ArgumentException("Result filename must not contain a directory", "fileName");
        string resultRoot = Environment.GetEnvironmentVariable("TRAINER_RESULT_DIR");
        if (String.IsNullOrWhiteSpace(resultRoot)) return Path.Combine(AppDomain.CurrentDomain.BaseDirectory, fileName);
        Directory.CreateDirectory(resultRoot);
        return Path.Combine(resultRoot, fileName);
    }

    public static void EnsureStarted()
    {
        lock (Gate)
        {
            if (started) return;
            try
            {
                string runsRoot = Environment.GetEnvironmentVariable("TRAINER_SESSION_DIR");
                if (String.IsNullOrWhiteSpace(runsRoot)) runsRoot = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, @"..\dev\runs");
                // The environment variable names the parent; every process gets its own child.
                sessionDirectory = Path.Combine(Path.GetFullPath(runsRoot), SessionId);
                Directory.CreateDirectory(sessionDirectory);
                string runKind = Environment.GetEnvironmentVariable("TRAINER_RUN_KIND");
                if (String.Equals(runKind, "probe", StringComparison.OrdinalIgnoreCase) || String.Equals(runKind, "interactive", StringComparison.OrdinalIgnoreCase))
                {
                    // Direct writes to redirected streams do not require a Windows console/code page.
                    var stdout = new StreamWriter(Console.OpenStandardOutput(), Utf8);
                    var stderr = new StreamWriter(Console.OpenStandardError(), Utf8);
                    stdout.AutoFlush = true;
                    stderr.AutoFlush = true;
                    Console.SetOut(stdout);
                    Console.SetError(stderr);
                }
                Assembly entry = Assembly.GetEntryAssembly();
                executablePath = entry == null ? Process.GetCurrentProcess().MainModule.FileName : entry.Location;
                using (SHA256 sha = SHA256.Create())
                using (FileStream input = File.OpenRead(executablePath))
                    executableHash = BitConverter.ToString(sha.ComputeHash(input)).Replace("-", "");
                sourceBuildId = Environment.GetEnvironmentVariable("TRAINER_SOURCE_BUILD_ID");
                if (String.IsNullOrWhiteSpace(sourceBuildId)) sourceBuildId = Environment.GetEnvironmentVariable("TRAINER_BUILD_ID");
                string adjacentManifest = executablePath + ".build.json";
                if (File.Exists(adjacentManifest))
                {
                    var build = Json.Deserialize<Dictionary<string, object>>(File.ReadAllText(adjacentManifest, Utf8));
                    object buildId;
                    if (String.IsNullOrWhiteSpace(sourceBuildId) && build.TryGetValue("build_id", out buildId)) sourceBuildId = Convert.ToString(buildId);
                    buildManifestPath = Path.Combine(sessionDirectory, "build.json");
                    File.Copy(adjacentManifest, buildManifestPath, false);
                }
                if (String.IsNullOrWhiteSpace(sourceBuildId)) sourceBuildId = "unrecorded";
                WriteManifest("running", null, null);
                AppendLog("Session started: " + SessionId + "; sourceBuildId=" + sourceBuildId + "; exe=" + executablePath);
                AppDomain.CurrentDomain.UnhandledException += OnUnhandledException;
                AppDomain.CurrentDomain.ProcessExit += delegate { Complete("process-exit"); };
                Application.ApplicationExit += delegate { Complete("application-exit"); };
                Application.ThreadException += delegate(object sender, System.Threading.ThreadExceptionEventArgs args)
                {
                    RecordException("winforms-thread", args.Exception);
                    // A failed event handler cannot be assumed safe to continue.
                    Environment.ExitCode = 1;
                    Application.Exit();
                };
                started = true;
            }
            catch (Exception error)
            {
                ReportLoggingFailure("start session", error);
                throw new IOException("Cannot create trainer session logs at " + sessionDirectory, error);
            }
        }
    }

    public static void Log(string message)
    {
        EnsureStarted();
        lock (Gate)
        {
            try { AppendLog(message); }
            catch (Exception error)
            {
                ReportLoggingFailure("append integration.log", error);
                throw new IOException("Cannot save trainer log " + Path.Combine(sessionDirectory, "integration.log"), error);
            }
        }
    }

    public static void AppendTelemetry(string header, string rows)
    {
        EnsureStarted();
        lock (Gate)
        {
            try
            {
                string path = Path.Combine(sessionDirectory, "telemetry.csv");
                if (telemetryHeader == null)
                {
                    // This file belongs only to this process/session; never truncate an old run.
                    using (FileStream output = new FileStream(path, FileMode.CreateNew, FileAccess.Write, FileShare.Read))
                    using (StreamWriter writer = new StreamWriter(output, Utf8)) writer.Write(header);
                    telemetryHeader = header;
                }
                else if (!String.Equals(telemetryHeader, header, StringComparison.Ordinal))
                    throw new InvalidOperationException("Telemetry columns changed inside one session.");
                File.AppendAllText(path, rows, Utf8);
            }
            catch (Exception error)
            {
                ReportLoggingFailure("append telemetry.csv", error);
                throw new IOException("Cannot save trainer telemetry " + Path.Combine(sessionDirectory, "telemetry.csv"), error);
            }
        }
    }

    public static void RecordException(string context, Exception error)
    {
        EnsureStarted();
        lock (Gate)
        {
            exceptionCount++;
            try
            {
                AppendLog("EXCEPTION [" + context + "] " + error);
            }
            catch (Exception loggingError)
            {
                // An exception hook must not recursively throw while handling the first failure.
                ReportLoggingFailure("save exception [" + context + "]: " + error, loggingError);
            }
            // A locked/full integration log must not prevent recording the failure in the manifest.
            try { WriteManifest(finished ? "closed-with-errors" : "running-with-errors", finished ? DateTime.UtcNow.ToString("O") : null, context); }
            catch (Exception loggingError) { ReportLoggingFailure("save exception manifest [" + context + "]", loggingError); }
        }
    }

    public static void Complete(string reason)
    {
        lock (Gate)
        {
            // ApplicationExit can precede the int Main return. ProcessExit records the final code.
            if (!started || (finished && reason != "process-exit")) return;
            try
            {
                AppendLog("Session completed: " + reason + "; exitCode=" + Environment.ExitCode + "; exceptions=" + exceptionCount);
            }
            catch (Exception error) { ReportLoggingFailure("append completion log", error); }
            try
            {
                WriteManifest(exceptionCount > 0 || loggingFailureCount > 0 || Environment.ExitCode != 0 ? "closed-with-errors" : "closed", DateTime.UtcNow.ToString("O"), reason);
                finished = true;
            }
            catch (Exception error) { ReportLoggingFailure("complete session manifest", error); }
        }
    }

    static void OnUnhandledException(object sender, UnhandledExceptionEventArgs args)
    {
        Exception error = args.ExceptionObject as Exception;
        RecordException("appdomain-unhandled; terminating=" + args.IsTerminating, error ?? new Exception(Convert.ToString(args.ExceptionObject)));
        if (args.IsTerminating) Environment.ExitCode = 1;
    }

    static void AppendLog(string message)
    {
        File.AppendAllText(Path.Combine(sessionDirectory, "integration.log"), DateTime.UtcNow.ToString("O") + " " + message + Environment.NewLine, Utf8);
    }

    static void WriteManifest(string state, string endedUtc, string reason)
    {
        var manifest = new Dictionary<string, object>();
        manifest["schemaVersion"] = 1;
        manifest["sessionId"] = SessionId;
        manifest["pid"] = Process.GetCurrentProcess().Id;
        manifest["startedUtc"] = StartedUtc;
        manifest["endedUtc"] = endedUtc;
        manifest["state"] = state;
        manifest["completionReason"] = reason;
        manifest["exitCode"] = finished || endedUtc != null ? (object)Environment.ExitCode : null;
        manifest["exceptionCount"] = exceptionCount;
        manifest["loggingFailureCount"] = loggingFailureCount;
        manifest["executablePath"] = executablePath;
        manifest["executableSha256"] = executableHash;
        manifest["sourceBuildId"] = sourceBuildId;
        manifest["buildManifest"] = buildManifestPath == null ? null : "build.json";
        manifest["runKind"] = Environment.GetEnvironmentVariable("TRAINER_RUN_KIND") ?? "direct";
        manifest["runtimeVersion"] = Environment.Version.ToString();
        manifest["processBits"] = IntPtr.Size * 8;
        manifest["sessionDirectory"] = sessionDirectory;
        manifest["integrationLog"] = "integration.log";
        manifest["telemetryFile"] = "telemetry.csv";
        string path = Path.Combine(sessionDirectory, "session.json");
        string temporary = path + ".tmp";
        File.WriteAllText(temporary, Json.Serialize(manifest) + Environment.NewLine, Utf8);
        if (File.Exists(path)) File.Replace(temporary, path, null); else File.Move(temporary, path);
    }

    static void ReportLoggingFailure(string action, Exception error)
    {
        loggingFailureCount++;
        string message = DateTime.UtcNow.ToString("O") + " TRAINER LOGGING FAILURE: " + action + "; session=" + SessionId + Environment.NewLine + error;
        Trace.TraceError(message);
        try { Console.Error.WriteLine(message); } catch { /* A WinExe may have no console. */ }
        try
        {
            if (sessionDirectory != null && Directory.Exists(sessionDirectory))
                File.AppendAllText(Path.Combine(sessionDirectory, "logging_failure.log"), message + Environment.NewLine, Utf8);
        }
        catch (Exception fallbackError) { Trace.TraceError("Session emergency logging failed: " + fallbackError); }
        try
        {
            string fallback = Path.Combine(Path.GetTempPath(), "Trainer_logging_failure_" + SessionId + ".txt");
            File.AppendAllText(fallback, message + Environment.NewLine, Utf8);
        }
        catch (Exception fallbackError) { Trace.TraceError("Emergency logging failed: " + fallbackError); }
    }
}
