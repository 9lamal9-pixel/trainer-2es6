using System.Collections.Generic;
using Newtonsoft.Json;

namespace IWP;

internal class RailRoadData
{
	[JsonProperty(Required = Required.Default)]
	public CustomActionEvent<TrafficLightData> OnTrafficLightCodeChanged = new CustomActionEvent<TrafficLightData>();

	[JsonProperty(Required = Required.Default)]
	public CustomActionEvent<bool> OnTimeOfDayChanged = new CustomActionEvent<bool>();

	[JsonProperty(Required = Required.Default)]
	public CustomActionEvent<bool> OnSetRain = new CustomActionEvent<bool>();

	[JsonProperty(Required = Required.Default)]
	public CustomActionEvent<bool> OnSetSnow = new CustomActionEvent<bool>();

	[JsonProperty(Required = Required.Default)]
	public CustomActionEvent<bool> OnSetFog = new CustomActionEvent<bool>();

	[JsonProperty(Required = Required.Default)]
	public CustomActionEvent<SignData> OnAddSign = new CustomActionEvent<SignData>();

	[JsonProperty(Required = Required.Default)]
	public CustomActionEvent<ObstructionData> OnSetObstructionActive = new CustomActionEvent<ObstructionData>();

	[JsonProperty("start_coord")]
	public int startCoord { get; set; }

	public bool day { get; set; }

	public bool raining { get; set; }

	public bool snow { get; set; }

	public bool fog { get; set; }

	[JsonProperty("profil")]
	public List<ProfileData> profile { get; set; }

	public List<CurveData> plan { get; set; }

	public List<PiketData> pikets { get; set; }

	public List<StationData> stations { get; set; }

	public List<TrafficLightData> lights { get; set; }

	public List<SignData> signals { get; set; }

	[JsonProperty(Required = Required.Default)]
	public List<SignData> dangerousSectionSigns { get; set; } = new List<SignData>();

	[JsonProperty("prep")]
	public Dictionary<string, List<ObstructionData>> obstructions { get; set; }

	public List<ArrowData> arrows { get; set; }

	public void SetTrafficLightCode(int index, int mode)
	{
		lights[index].light = mode;
		OnTrafficLightCodeChanged.Invoke(lights[index]);
	}

	public void SetTimeOfDay(bool value)
	{
		day = value;
		OnTimeOfDayChanged.Invoke(value);
	}

	public void SetRain(bool value)
	{
		raining = value;
		OnSetRain.Invoke(value);
	}

	public void SetSnow(bool value)
	{
		snow = value;
		OnSetSnow.Invoke(value);
	}

	public void SetFog(bool value)
	{
		fog = value;
		OnSetFog.Invoke(value);
	}

	public void AddSign(int index, SignData signData)
	{
		signals.Insert(index, signData);
		if (signData.type == 7 || signData.type == 8 || signData.type == 3 || signData.type == 9)
		{
			if (dangerousSectionSigns.Count == 0)
			{
				dangerousSectionSigns.Add(signData);
			}
			else
			{
				for (int i = 0; i < dangerousSectionSigns.Count; i++)
				{
					if (signData.coord < dangerousSectionSigns[i].coord)
					{
						dangerousSectionSigns.Insert(i, signData);
						break;
					}
				}
			}
		}
		OnAddSign.Invoke(signData);
	}

	public void SetObstructionActive(string type, int index, bool value)
	{
		ObstructionData obstructionData = obstructions[type][index];
		obstructionData.actively = value;
		OnSetObstructionActive.Invoke(obstructionData);
	}
}
