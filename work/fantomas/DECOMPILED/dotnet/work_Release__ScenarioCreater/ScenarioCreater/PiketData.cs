using Newtonsoft.Json;

namespace ScenarioCreater;

public class PiketData
{
	public int coord;

	public int km;

	[JsonProperty("piket")]
	public int name;
}
