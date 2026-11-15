using System;

namespace IWP;

internal static class Program
{
	[STAThread]
	private static void Main(string[] args)
	{
		if (GameConfig.Instance.IsActiveConsole)
		{
			ConsoleCall.StartConsole();
		}
		Console.WriteLine("Starting instructor...");
		Manager manager = new Manager();
		manager.Awake();
		manager.Start();
		while (manager.ContinueUpdating)
		{
			if (!BaseManager<UIManager>.instance.OnPauseButtonState)
			{
				manager.Update();
			}
		}
		if (GameConfig.Instance.IsActiveConsole)
		{
			ConsoleCall.CloseConsole();
		}
	}
}
