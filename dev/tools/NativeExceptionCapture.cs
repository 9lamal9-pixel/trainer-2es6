using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Web.Script.Serialization;

// An x86 debugger for this workspace's uniquely deployed diagnostic probe only.
// It reads process memory. It never writes memory, registers, registry, or DLL files.
class NativeExceptionCapture
{
    const uint Continue = 0x00010002, NotHandled = 0x80010001;
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool DebugActiveProcess(uint pid);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool DebugActiveProcessStop(uint pid);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool DebugSetProcessKillOnExit(bool value);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool WaitForDebugEvent([Out] byte[] data, uint timeout);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool ContinueDebugEvent(uint pid, uint tid, uint status);
    [DllImport("kernel32.dll", SetLastError = true)] static extern IntPtr OpenProcess(uint access, bool inherit, uint pid);
    [DllImport("kernel32.dll", SetLastError = true)] static extern IntPtr OpenThread(uint access, bool inherit, uint tid);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool GetThreadContext(IntPtr thread, [In, Out] byte[] context);
    [DllImport("kernel32.dll", SetLastError = true)] static extern bool ReadProcessMemory(IntPtr process, IntPtr address, [Out] byte[] data, UIntPtr size, out UIntPtr read);
    [DllImport("kernel32.dll")] static extern bool CloseHandle(IntPtr handle);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] static extern bool QueryFullProcessImageName(IntPtr process, uint flags, StringBuilder name, ref uint size);
    [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)] static extern uint GetFinalPathNameByHandle(IntPtr file, StringBuilder name, uint size, uint flags);

    class Module { public uint Base, Size; public string Path; }
    static readonly Dictionary<uint, Module> Modules = new Dictionary<uint, Module>();
    static readonly JavaScriptSerializer Json = new JavaScriptSerializer();
    static IntPtr process;
    static StreamWriter log;
    static uint U(byte[] bytes, int offset) { return BitConverter.ToUInt32(bytes, offset); }
    static IntPtr P(uint address) { return new IntPtr(unchecked((int)address)); }
    static string Hex(uint number) { return "0x" + number.ToString("X8"); }
    static void Write(object value)
    {
        log.WriteLine(Json.Serialize(new { utc = DateTime.UtcNow.ToString("O"), data = value }));
        log.Flush();
    }
    static byte[] Read(uint address, int count)
    {
        if (address < 0x10000 || count < 1 || count > 8192) return null;
        var data = new byte[count]; UIntPtr read;
        return ReadProcessMemory(process, P(address), data, (UIntPtr)count, out read) && read.ToUInt32() == (uint)count ? data : null;
    }
    static uint ReadU(uint address) { var data = Read(address, 4); return data == null ? 0 : U(data, 0); }
    static object Where(uint address)
    {
        foreach (var module in Modules.Values)
            if (address >= module.Base && (ulong)address < (ulong)module.Base + module.Size)
                return new { address = Hex(address), module = module.Path, module_base = Hex(module.Base), rva = Hex(address - module.Base) };
        return new { address = Hex(address), module = (string)null, module_base = (string)null, rva = (string)null };
    }
    static string FilePath(uint handle)
    {
        if (handle == 0 || handle == UInt32.MaxValue) return null;
        var text = new StringBuilder(2048);
        uint length = GetFinalPathNameByHandle(P(handle), text, (uint)text.Capacity, 0);
        return length > 0 && length < text.Capacity ? text.ToString() : null;
    }
    static void LoadModule(byte[] data, bool main)
    {
        uint file = U(data, 12), address = U(data, main ? 24 : 16);
        string path = FilePath(file);
        uint size = 0; var dos = Read(address, 64);
        if (dos != null && dos[0] == 0x4d && dos[1] == 0x5a)
        {
            uint offset = U(dos, 60);
            if (offset < 1048576)
            {
                var pe = Read(address + offset, 96);
                if (pe != null && U(pe, 0) == 0x4550) size = U(pe, 80);
            }
        }
        Modules[address] = new Module { Base = address, Size = size, Path = path };
        Write(new { kind = main ? "process_image" : "module_load", module_base = Hex(address), image_size = size, path = path });
        if (file != 0 && file != UInt32.MaxValue) CloseHandle(P(file));
        if (main)
        {
            if (U(data, 16) != 0) CloseHandle(P(U(data, 16)));
            if (U(data, 20) != 0) CloseHandle(P(U(data, 20)));
        }
    }
    static object Delphi(uint exceptionObject)
    {
        uint vmt = ReadU(exceptionObject), namePointer = vmt > 56 ? ReadU(vmt - 56) : 0;
        string className = null, message = null;
        var first = Read(namePointer, 1);
        if (first != null && first[0] > 0 && first[0] < 128)
        {
            var bytes = Read(namePointer + 1, first[0]);
            if (bytes != null) className = Encoding.ASCII.GetString(bytes);
        }
        // This native build uses Delphi UnicodeString Exception.FMessage at object+4.
        uint textPointer = ReadU(exceptionObject + 4);
        uint length = textPointer > 4 ? ReadU(textPointer - 4) : 0;
        if (length > 0 && length <= 2048)
        {
            var bytes = Read(textPointer, (int)length * 2);
            if (bytes != null) message = Encoding.Unicode.GetString(bytes);
        }
        return new { object_address = Hex(exceptionObject), vmt = Where(vmt), class_name = className, message = message };
    }
    static object Stack(uint tid)
    {
        IntPtr thread = OpenThread(0x0008, false, tid);
        if (thread == IntPtr.Zero) return new { context_error = Marshal.GetLastWin32Error() };
        try
        {
            var context = new byte[716]; Array.Copy(BitConverter.GetBytes(0x10003U), context, 4);
            if (!GetThreadContext(thread, context)) return new { context_error = Marshal.GetLastWin32Error() };
            uint frame = U(context, 180), ip = U(context, 184), sp = U(context, 196);
            var frames = new List<object>();
            for (int i = 0; i < 16 && frame != 0; i++)
            {
                var pair = Read(frame, 8); if (pair == null) break;
                uint next = U(pair, 0), returnAddress = U(pair, 4);
                frames.Add(new { frame = Hex(frame), return_address = Where(returnAddress) });
                if (next <= frame || next - frame > 1048576 || (next & 3) != 0) break;
                frame = next;
            }
            var raw = Read(sp, 64); var values = new List<object>();
            if (raw != null) for (int i = 0; i < raw.Length; i += 4) values.Add(Where(U(raw, i)));
            return new { instruction = Where(ip), stack_pointer = Hex(sp), frame_chain = frames, stack_words = values };
        }
        finally { CloseHandle(thread); }
    }
    static bool IsNtdll(uint address)
    {
        foreach (var module in Modules.Values)
            if (address >= module.Base && (ulong)address < (ulong)module.Base + module.Size)
                return module.Path != null && module.Path.EndsWith("\\ntdll.dll", StringComparison.OrdinalIgnoreCase);
        return false;
    }

    static int Main(string[] args)
    {
        uint pid = 0; bool attached = false, exited = false; int exit = 1;
        try
        {
            if (IntPtr.Size != 4) throw new Exception("Compile this helper with /platform:x86.");
            if (args.Length < 3 || args.Length > 4 || !UInt32.TryParse(args[0], out pid))
                throw new Exception("Usage: NativeExceptionCapture.exe PID capture.jsonl workspace-root [timeout-seconds]");
            string root = Path.GetFullPath(args[2]).TrimEnd('\\', '/');
            string output = Path.GetFullPath(args[1]);
            if (!output.StartsWith(root + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase)) throw new Exception("Capture output must be inside this workspace.");
            process = OpenProcess(0x1010, false, pid);
            if (process == IntPtr.Zero) throw new Win32Exception(Marshal.GetLastWin32Error(), "OpenProcess");
            var name = new StringBuilder(2048); uint length = (uint)name.Capacity;
            if (!QueryFullProcessImageName(process, 0, name, ref length)) throw new Win32Exception(Marshal.GetLastWin32Error(), "QueryFullProcessImageName");
            string target = Path.GetFullPath(name.ToString());
            if (!target.StartsWith(Path.Combine(root, "outputs") + "\\", StringComparison.OrdinalIgnoreCase) ||
                !Path.GetFileName(target).StartsWith("NativeInitializationCapture.test-", StringComparison.OrdinalIgnoreCase))
                throw new Exception("Only this workspace's NativeInitializationCapture.test-* probe is permitted.");
            int timeout = args.Length == 4 ? Int32.Parse(args[3]) : 75;
            if (timeout < 1 || timeout > 180) throw new Exception("Timeout must be 1..180 seconds.");
            Directory.CreateDirectory(Path.GetDirectoryName(output));
            // CreateNew prevents accidentally replacing evidence from a previous capture.
            log = new StreamWriter(new FileStream(output, FileMode.CreateNew, FileAccess.Write, FileShare.Read), new UTF8Encoding(false));
            Write(new { kind = "capture_start", diagnostic_only = true, pid = pid, target = target, debugger_bits = 32,
                policy = "Read-only debugger; application exceptions receive DBG_EXCEPTION_NOT_HANDLED; no memory/register/DLL mutations; no process kill on debugger exit" });
            if (!DebugActiveProcess(pid)) throw new Win32Exception(Marshal.GetLastWin32Error(), "DebugActiveProcess");
            attached = true;
            if (!DebugSetProcessKillOnExit(false)) throw new Win32Exception(Marshal.GetLastWin32Error(), "DebugSetProcessKillOnExit(false)");
            var elapsed = Stopwatch.StartNew(); bool initialBreakpoint = true;
            int eventCount = 0, exceptionCount = 0;
            var repetitions = new Dictionary<string, int>();
            string stopReason = null;
            while (elapsed.Elapsed.TotalSeconds < timeout)
            {
                var data = new byte[96];
                if (!WaitForDebugEvent(data, 250))
                {
                    int error = Marshal.GetLastWin32Error();
                    if (error == 121 || error == 258) continue;
                    throw new Win32Exception(error, "WaitForDebugEvent");
                }
                uint kind = U(data, 0), eventPid = U(data, 4), tid = U(data, 8);
                // Even a metadata-decoding failure must never swallow an application exception.
                uint disposition = kind == 1 ? NotHandled : Continue;
                eventCount++;
                if (eventCount >= 4096) stopReason = "debug_event_cap";
                try
                {
                    if (kind == 3) LoadModule(data, true);
                    else if (kind == 6) LoadModule(data, false);
                    else if (kind == 2) { uint handle = U(data, 12); if (handle != 0) CloseHandle(P(handle)); }
                    else if (kind == 7) { uint address = U(data, 12); Write(new { kind = "module_unload", module = Where(address) }); Modules.Remove(address); }
                    else if (kind == 1)
                    {
                        uint code = U(data, 12), address = U(data, 24), count = Math.Min(U(data, 28), 15U);
                        bool first = U(data, 92) != 0;
                        bool attachBreakpoint = initialBreakpoint && first && code == 0x80000003 && IsNtdll(address);
                        if (attachBreakpoint) { initialBreakpoint = false; disposition = Continue; }
                        string exceptionKey = Hex(code) + ":" + Hex(address) + ":" + first + ":" + tid;
                        int repeated; repetitions.TryGetValue(exceptionKey, out repeated); repetitions[exceptionKey] = ++repeated;
                        if (!attachBreakpoint) exceptionCount++;
                        if (exceptionCount >= 64) stopReason = "exception_cap";
                        if (repeated >= 3 && !attachBreakpoint) stopReason = "repeated_exception_cap";
                        if (!attachBreakpoint && code == 0x0eedfade) stopReason = "first_delphi_exception_captured";
                        if (code == 0xc0000025) stopReason = "noncontinuable_exception_captured";
                        var parameters = new List<string>();
                        for (int i = 0; i < count; i++) parameters.Add(Hex(U(data, 32 + i * 4)));
                        Write(new { kind = repeated == 1 ? "exception" : "exception_repeat", pid = eventPid, tid = tid, code = Hex(code), flags = Hex(U(data, 16)),
                            first_chance = first, address = Where(address), parameters = parameters,
                            delphi_throw_site = repeated == 1 && code == 0x0eedfade && count >= 1 ? Where(U(data, 32)) : null,
                            delphi = repeated == 1 && code == 0x0eedfade && count >= 2 ? Delphi(U(data, 36)) : null,
                            stack = attachBreakpoint || repeated > 1 ? null : Stack(tid),
                            repetition = repeated, stop_after_event = stopReason,
                            initial_attach_breakpoint = attachBreakpoint, continue_status = Hex(disposition) });
                    }
                    else if (kind == 5)
                    {
                        Write(new { kind = "process_exit", pid = eventPid, exit_code = Hex(U(data, 12)) });
                        exited = true; exit = 0;
                    }
                    else if (kind == 9) Write(new { kind = "rip", error = U(data, 12), type = U(data, 16) });
                }
                catch (Exception error) { Write(new { kind = "capture_event_error", event_code = kind, error = error.ToString() }); }
                finally
                {
                    if (!ContinueDebugEvent(eventPid, tid, disposition)) throw new Win32Exception(Marshal.GetLastWin32Error(), "ContinueDebugEvent");
                }
                if (exited) break;
                if (stopReason != null)
                {
                    Write(new { kind = "capture_stopped", reason = stopReason, event_count = eventCount, exception_count = exceptionCount,
                        target_will_not_be_killed = true, diagnostic_only = true });
                    exit = stopReason == "first_delphi_exception_captured" ? 0 : 2;
                    break;
                }
            }
            if (!exited && stopReason == null) { Write(new { kind = "capture_timeout", timeout_seconds = timeout, target_will_not_be_killed = true }); exit = 2; }
        }
        catch (Exception error)
        {
            if (log != null) Write(new { kind = "capture_error", error = error.ToString() });
            Console.Error.WriteLine(error); exit = 1;
        }
        finally
        {
            if (attached && !exited)
            {
                bool detached = DebugActiveProcessStop(pid);
                if (log != null) Write(new { kind = "detach", success = detached, error = detached ? 0 : Marshal.GetLastWin32Error() });
            }
            if (process != IntPtr.Zero) CloseHandle(process);
            if (log != null) log.Dispose();
        }
        return exit;
    }
}
