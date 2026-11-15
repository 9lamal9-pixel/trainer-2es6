namespace IWP;

internal enum ViolationType
{
	NoneType,
	MoveWithoutSpotlight,
	MoveWithoutWhistle,
	SwitchingOffEpkOnTheMove,
	AutomaticBrakingFromEPK,
	AutomaticBrakingFromKON,
	EmergencyBrakingNotSupportingByKM,
	EmergencyBrakingNotSupportingByKvt,
	InterruptedEmergencyBraking,
	EmergencyBraking,
	OverSpeed,
	StoppingTrainAtPermissiveSignalLights,
	CollisionWithObstacle,
	NotWhistleOnSign,
	ViolationCount
}
