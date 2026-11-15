using Newtonsoft.Json;

namespace ScenarioCreater;

public class ProfileData
{
	[JsonProperty(Required = Required.Default)]
	public int coord;

	[JsonProperty("dist")]
	public int distance;

	[JsonProperty("uklon")]
	public double slope;
}
