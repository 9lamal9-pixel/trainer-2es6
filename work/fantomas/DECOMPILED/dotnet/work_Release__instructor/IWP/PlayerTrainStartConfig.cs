using System;
using System.IO;
using Newtonsoft.Json;

namespace IWP;

internal class PlayerTrainStartConfig
{
	private static PlayerTrainStartConfig _instance;

	[JsonProperty("Charging_pressure")]
	public float ChargingPressure { get; set; }

	[JsonProperty("Compressor_feed")]
	public float CompressorFeed { get; set; }

	public int Length { get; set; }

	[JsonProperty("Liquidation_time")]
	public float LiquidationTime { get; set; }

	[JsonProperty("Pressure_max")]
	public float PressureMax { get; set; }

	[JsonProperty("Pressure_min")]
	public float PressureMin { get; set; }

	[JsonProperty("Max_speed")]
	public int MaxSpeed { get; set; }

	[JsonProperty("Number_sections")]
	public int NumberSections { get; set; }

	[JsonProperty("Weight_section")]
	public int Weight { get; set; }

	public static PlayerTrainStartConfig instance
	{
		get
		{
			if (_instance == null)
			{
				string text = "locomotiv_config.json";
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
						Console.WriteLine("start sonConvert.DeserializeObject<PlayerTrainStartConfig>");
						_instance = JsonConvert.DeserializeObject<PlayerTrainStartConfig>(value);
						Console.WriteLine("end sonConvert.DeserializeObject<PlayerTrainStartConfig>");
					}
					catch (Exception value2)
					{
						Console.WriteLine(value2);
					}
				}
				else
				{
					Console.WriteLine("Error: Can't find config file at path 'RTS/locomotiv_config.json'");
				}
			}
			return _instance;
		}
	}
}
