using System;
using System.IO;
using System.Text;
using System.Threading.Tasks;

namespace TrainerDevelopment
{
    // C# owns the worker thread; PowerShell scriptblocks have no runspace there.
    public static class ConsoleCapture
    {
        public static Task Start(TextReader input, string path)
        {
            return Task.Factory.StartNew(delegate
            {
                using (var file = new FileStream(path, FileMode.CreateNew, FileAccess.Write,
                           FileShare.Read, 4096, FileOptions.WriteThrough))
                using (var writer = new StreamWriter(file, new UTF8Encoding(false)))
                {
                    writer.AutoFlush = true;
                    string line;
                    while ((line = input.ReadLine()) != null) writer.WriteLine(line);
                }
            }, TaskCreationOptions.LongRunning);
        }
    }
}
