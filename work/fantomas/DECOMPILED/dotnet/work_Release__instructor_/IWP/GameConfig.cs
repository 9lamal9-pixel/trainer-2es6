using System;
using System.IO;
using Newtonsoft.Json;

namespace IWP;

public class GameConfig
{
	private static GameConfig _instance;

	[JsonProperty("is_active_console")]
	public bool IsActiveConsole { get; private set; }

	[JsonProperty("is_active_pult")]
	public bool IsActivePult { get; private set; }

	[JsonProperty("ip_pult")]
	public string PultIp { get; private set; }

	[JsonProperty("port_pult")]
	public int PultPort { get; private set; }

	[JsonProperty("debug_pult")]
	public bool DebugPult { get; private set; }

	[JsonProperty("is_active_video")]
	public bool IsActiveVideo { get; private set; }

	[JsonProperty("ip_video")]
	public string VideoIp { get; private set; }

	[JsonProperty("port_video")]
	public int VideoPort { get; private set; }

	[JsonProperty("debug_video")]
	public bool DebugVideo { get; private set; }

	[JsonProperty("is_active_blok")]
	public bool IsActiveBlok { get; private set; }

	[JsonProperty("ip_blok")]
	public string BlokIp { get; private set; }

	[JsonProperty("port_blok")]
	public int BlokPort { get; private set; }

	[JsonProperty("debug_blok")]
	public bool DebugBlok { get; private set; }

	[JsonProperty("is_active_virtual_control_viewer")]
	public bool IsActiveVirtualControlViewer { get; private set; }

	[JsonProperty("ip_virtual_control_viewer")]
	public string VirtualControlViewerIp { get; private set; }

	[JsonProperty("port_virtual_control_viewer")]
	public int VirtualControlViewerPort { get; private set; }

	public static GameConfig Instance
	{
		get
		{
			if (_instance == null)
			{
				_instance = new GameConfig();
				_instance.LoadConfig();
			}
			return _instance;
		}
	}

	private void LoadConfig()
	{
		string path = "Data/logic_config.json";
		if (File.Exists(path))
		{
			string value;
			using (StreamReader streamReader = new StreamReader(path))
			{
				value = streamReader.ReadToEnd();
				streamReader.Close();
			}
			try
			{
				Console.WriteLine("start sonConvert.DeserializeObject<GameConfig>");
				_instance = JsonConvert.DeserializeObject<GameConfig>(value);
				Console.WriteLine("end sonConvert.DeserializeObject<GameConfig>");
				return;
			}
			catch (Exception value2)
			{
				Console.WriteLine(value2);
				return;
			}
		}
		Console.WriteLine("Error: Can't find config file at path 'Data/logic_config.json'");
	}
}
