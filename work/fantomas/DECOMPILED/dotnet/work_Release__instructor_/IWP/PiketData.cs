using Newtonsoft.Json;

namespace IWP;

public class PiketData
{
	public int coord;

	public int km;

	[JsonProperty("piket")]
	public int name;
}
