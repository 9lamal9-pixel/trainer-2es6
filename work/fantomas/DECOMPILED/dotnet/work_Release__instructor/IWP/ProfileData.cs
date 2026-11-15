using Newtonsoft.Json;

namespace IWP;

public class ProfileData
{
	[JsonProperty(Required = Required.Default)]
	public int coord;

	[JsonProperty("dist")]
	public int distance;

	[JsonProperty("uklon")]
	public double slope;
}
