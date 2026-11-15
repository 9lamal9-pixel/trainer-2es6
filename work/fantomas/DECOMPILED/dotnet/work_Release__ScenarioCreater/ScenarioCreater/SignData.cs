using Newtonsoft.Json;

namespace ScenarioCreater;

public class SignData
{
	public int coord;

	public int type;

	[JsonProperty("speed_limit")]
	public int speedLimit;
}
