using Newtonsoft.Json;

namespace IWP;

public class SignData
{
	public int coord;

	public int type;

	[JsonProperty("speed_limit")]
	public int speedLimit;
}
