using System.Collections.Generic;

namespace IWP;

internal class BLOKWriteController : UdpWriteController<string>
{
	public BLOKWriteController()
	{
		Items = new List<UdpPoint<string>>
		{
			InitPoint("blockPower"),
			InitPoint("epkButton"),
			InitPoint("rbButton"),
			InitPoint("rbsButton"),
			InitPoint("rbpButton"),
			InitPoint("coordinateGps", 4),
			InitPoint("picket"),
			InitPoint("tmValue", 4),
			InitPoint("tcValue", 4),
			InitPoint("ur1Value", 4),
			InitPoint("ur2Value", 4),
			InitPoint("pmValue", 4),
			InitPoint("acceleration", 4),
			InitPoint("actualSpeed", 4),
			InitPoint("roadBias", 4),
			InitPoint("brakingCoefficient", 4),
			InitPoint("ekEquipped"),
			InitPoint("cassetteEquipped"),
			InitPoint("traction"),
			InitPoint("reverse"),
			InitPoint("worldTime", 8),
			InitPoint("keyboardButton"),
			InitPoint("stationName", 16),
			InitPoint("targetСode"),
			InitPoint("distanceToTarget", 2),
			InitPoint("distanceToSaut", 2),
			InitPoint("targetName", 18),
			InitPoint("allowableSpeedByEK", 2),
			InitPoint("targetSpeedByEK", 2),
			InitPoint("distanceToNextSectionEK", 2),
			InitPoint("trafficLightCode"),
			InitPoint("distanceToTrafficLight", 2),
			InitPoint("cruiseControlCode"),
			InitPoint("cruiseControlSpeed", 2),
			InitPoint("neutralInsert"),
			InitPoint("tokorazdel"),
			InitPoint("distanceTokorazdelOrNeutralInsert", 2),
			InitPoint("serviceBraking"),
			InitPoint("malfunctionCode"),
			InitPoint("sautEquipped"),
			InitPoint("tskbmEquipped"),
			InitPoint("tskbmPower"),
			InitPoint("tskbmmDressed"),
			InitPoint("tskbmmStatus"),
			InitPoint("trafficLightName", 18),
			InitPoint("brakingValveOpenedFromKOH"),
			InitPoint("brakingValveOpenedFromEPK"),
			InitPoint("roadIndex"),
			InitPoint("videoCoord", 4)
		};
		Buffer = new byte[BufferLength];
	}
}
