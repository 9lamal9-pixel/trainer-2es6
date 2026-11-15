using Newtonsoft.Json;

namespace IWP;

public class TrafficLightData
{
	public int coord;

	public string name;

	public int index;

	public int coded;

	public int type;

	public int light;

	[JsonProperty("lights_combinations")]
	public int[] lightCombinations;
}
