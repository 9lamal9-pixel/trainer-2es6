using System.Collections.Generic;
using Newtonsoft.Json;

namespace ScenarioCreater;

public class RailRoadData
{
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

	[JsonProperty("prep")]
	public Dictionary<string, List<ObstructionData>> obstructions { get; set; }
}
