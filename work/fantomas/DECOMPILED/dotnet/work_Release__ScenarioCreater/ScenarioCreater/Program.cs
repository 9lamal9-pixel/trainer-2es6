using System;
using System.Windows.Forms;

namespace ScenarioCreater;

internal static class Program
{
	[STAThread]
	private static void Main(string[] args)
	{
		RailRoadData railRoadData = RailRoadDataUtilities.LoadFromJsonConfig("RTS\\scenaries\\" + MarkupConfig.instance.MarkupFileName);
		Application.EnableVisualStyles();
		Application.SetCompatibleTextRenderingDefault(defaultValue: false);
		Application.Run(new PathProfileForm(railRoadData));
	}
}
