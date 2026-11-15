using System;
using System.IO;
using Newtonsoft.Json;

namespace ScenarioCreater;

internal class MarkupConfig
{
	private static MarkupConfig _instance;

	[JsonProperty("markup_index")]
	public int RoadIndex { get; set; }

	[JsonProperty("markup_file_name")]
	public string MarkupFileName { get; set; }

	public static MarkupConfig instance
	{
		get
		{
			if (_instance == null)
			{
				string text = "start_markup_data.json";
				if (File.Exists("RTS\\" + text))
				{
					string value;
					using (StreamReader streamReader = new StreamReader("RTS\\" + text))
					{
						value = streamReader.ReadToEnd();
						streamReader.Close();
					}
					try
					{
						Console.WriteLine("start sonConvert.DeserializeObject<MarkupConfig>");
						_instance = JsonConvert.DeserializeObject<MarkupConfig>(value);
						Console.WriteLine("end sonConvert.DeserializeObject<MarkupConfig>");
					}
					catch (Exception value2)
					{
						Console.WriteLine(value2);
					}
				}
				else
				{
					Console.WriteLine("Error: Can't find config file at path 'RTS/start_markup_data.json'");
				}
			}
			return _instance;
		}
	}
}
