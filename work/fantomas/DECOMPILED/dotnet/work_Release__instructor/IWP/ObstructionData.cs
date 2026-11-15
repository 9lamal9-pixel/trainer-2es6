using Newtonsoft.Json;

namespace IWP;

public class ObstructionData
{
	[JsonProperty(Required = Required.Default)]
	public string type;

	public int id;

	public int coord;

	public bool actively;

	public int index;
}
