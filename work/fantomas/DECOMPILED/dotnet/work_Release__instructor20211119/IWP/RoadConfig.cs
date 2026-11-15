using System;
using System.IO;
using Newtonsoft.Json;

namespace IWP;

internal class RoadConfig
{
	private static RoadConfig _instance;

	[JsonProperty("markup_index")]
	public int RoadIndex { get; set; }

	[JsonProperty("markup_file_name")]
	public string MarkupFileName { get; set; }

	public static RoadConfig instance
	{
		get
		{
			if (_instance == null)
			{
				string text = "start_markup_data.json";
				if (File.Exists("RTS/" + text))
				{
					string value;
					using (StreamReader streamReader = new StreamReader("RTS/" + text))
					{
						value = streamReader.ReadToEnd();
						streamReader.Close();
					}
					try
					{
						Console.WriteLine("start sonConvert.DeserializeObject<PlayerSostavConfig>");
						_instance = JsonConvert.DeserializeObject<RoadConfig>(value);
						Console.WriteLine("end sonConvert.DeserializeObject<PlayerSostavConfig>");
					}
					catch (Exception value2)
					{
						Console.WriteLine(value2);
					}
				}
				else
				{
					Console.WriteLine("Error: Can't find config file at path 'RTS/start_config.json'");
				}
			}
			return _instance;
		}
	}
}
