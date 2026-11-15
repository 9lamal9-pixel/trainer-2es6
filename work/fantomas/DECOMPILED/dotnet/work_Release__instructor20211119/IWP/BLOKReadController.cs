using System.Collections.Generic;

namespace IWP;

internal class BLOKReadController : UdpReadController<string>
{
	public BLOKReadController()
	{
		Items = new List<UdpPoint<string>>
		{
			InitPoint("epkHoldingCoil"),
			InitPoint("allowableSpeed", 2),
			InitPoint("distanceToAimingPoint", 2),
			InitPoint("trafficLightCode"),
			InitPoint("disassembleTheTraction"),
			InitPoint("useServiceBrakingOfSaut"),
			InitPoint("lampAttention"),
			InitPoint("lampPssPass")
		};
		Reset();
	}
}
