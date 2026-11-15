using Newtonsoft.Json;

namespace IWP;

public class ArrowData
{
	[JsonProperty("coord_start")]
	public int startCoord;

	[JsonProperty("coord_center")]
	public int centerCoord;

	[JsonProperty("coord_finish")]
	public int finishCoord;
}
