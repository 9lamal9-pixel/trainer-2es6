using System;
using System.IO;
using Newtonsoft.Json;

namespace IWP;

internal class PlayerSostavConfig
{
	private static PlayerSostavConfig _instance;

	[JsonProperty("Instructor_FIO")]
	public string InstructorFIO { get; set; }

	[JsonProperty("Student_PersonnelNumber")]
	public int StudentPersonnelNumber { get; set; }

	[JsonProperty("Student_FIO")]
	public string StudentFIO { get; set; }

	[JsonProperty("Composition_id")]
	public int SostavCompozitionId { get; set; }

	[JsonProperty("brutto")]
	public int SostavBrutto { get; set; }

	[JsonProperty("kolOsei")]
	public int SostavKolOsei { get; set; }

	[JsonProperty("kolWagons")]
	public int SostavWagonCount { get; set; }

	public static PlayerSostavConfig instance
	{
		get
		{
			if (_instance == null)
			{
				string text = "start_data.json";
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
						_instance = JsonConvert.DeserializeObject<PlayerSostavConfig>(value);
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
