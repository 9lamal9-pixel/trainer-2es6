using System.Runtime.InteropServices;

namespace IWP;

public static class ConsoleCall
{
	private const int ATTACH_PARENT_PROCESS = -1;

	public static bool isAllocConsole { get; } = false;

	public static void StartConsole()
	{
		if (!AttachConsole(-1))
		{
			AllocConsole();
		}
	}

	public static void CloseConsole()
	{
		FreeConsole();
	}

	[DllImport("kernel32.dll", SetLastError = true)]
	[return: MarshalAs(UnmanagedType.Bool)]
	private static extern bool AllocConsole();

	[DllImport("kernel32.dll", SetLastError = true)]
	[return: MarshalAs(UnmanagedType.Bool)]
	private static extern bool FreeConsole();

	[DllImport("kernel32.dll")]
	private static extern bool AttachConsole(int dwProcessId);
}
