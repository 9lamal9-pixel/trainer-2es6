using System.Collections.Generic;

namespace IWP;

internal class VirtualControlViewerWriteController : UdpWriteController<ControlPanelParameters>
{
	private const float MegapascalsToKilograms = 10.197162f;

	public VirtualControlViewerWriteController()
	{
		Items = new List<UdpPoint<ControlPanelParameters>>
		{
			InitPoint(ControlPanelParameters.BtnRb),
			InitPoint(ControlPanelParameters.BtnRbs),
			InitPoint(ControlPanelParameters.SwEpk),
			InitPoint(ControlPanelParameters.SwPedal),
			InitPoint(ControlPanelParameters.BtnSand),
			InitPoint(ControlPanelParameters.BtnTyphon),
			InitPoint(ControlPanelParameters.BtnWhistle),
			InitPoint(ControlPanelParameters.ReversController),
			InitPoint(ControlPanelParameters.MainController),
			InitPoint(ControlPanelParameters.SwBufferLightLeftFront),
			InitPoint(ControlPanelParameters.SwBufferLightRightFront),
			InitPoint(ControlPanelParameters.SwBufferLightLeftRear),
			InitPoint(ControlPanelParameters.SwBufferLightRightRear),
			InitPoint(ControlPanelParameters.SwSpotlightBright),
			InitPoint(ControlPanelParameters.SwSpotlightDull),
			InitPoint(ControlPanelParameters.BtnSandOnFirstAxis),
			InitPoint(ControlPanelParameters.BtnBrakeRelease),
			InitPoint(ControlPanelParameters.SwFilter),
			InitPoint(ControlPanelParameters.SwVigilance),
			InitPoint(ControlPanelParameters.BtnCheckAlsn),
			InitPoint(ControlPanelParameters.BtnSkz),
			InitPoint(ControlPanelParameters.SwCabinLightBright),
			InitPoint(ControlPanelParameters.SwLocomotiveControl),
			InitPoint(ControlPanelParameters.SwMotion),
			InitPoint(ControlPanelParameters.SwConversionManagement),
			InitPoint(ControlPanelParameters.SwRefrigeratorManagement),
			InitPoint(ControlPanelParameters.SwFan1),
			InitPoint(ControlPanelParameters.SwFan2),
			InitPoint(ControlPanelParameters.SwFan3),
			InitPoint(ControlPanelParameters.SwFan4),
			InitPoint(ControlPanelParameters.BtnCall),
			InitPoint(ControlPanelParameters.SwDamageIndicator1),
			InitPoint(ControlPanelParameters.SwDamage1),
			InitPoint(ControlPanelParameters.SwDamage2),
			InitPoint(ControlPanelParameters.SwAlarmMonitoring),
			InitPoint(ControlPanelParameters.SwBoxing),
			InitPoint(ControlPanelParameters.SwEmergencyStop),
			InitPoint(ControlPanelParameters.SwFuelPump1),
			InitPoint(ControlPanelParameters.BtnLaunch1),
			InitPoint(ControlPanelParameters.SwFuelPump2),
			InitPoint(ControlPanelParameters.BtnLaunch2),
			InitPoint(ControlPanelParameters.SwKm),
			InitPoint(ControlPanelParameters.SwKvtAnalog),
			InitPoint(ControlPanelParameters.SwClubDecoder),
			InitPoint(ControlPanelParameters.SwKvt),
			InitPoint(ControlPanelParameters.ValueOil1, 2, (float x) => (short)(999f * x / 15f)),
			InitPoint(ControlPanelParameters.ValueOil23, 2, (float x) => (short)(999f * x / 15f)),
			InitPoint(ControlPanelParameters.ValueOil1Temperature, 2, (float x) => (short)(x / 120f * 999f)),
			InitPoint(ControlPanelParameters.ValueOil23Temperature, 2, (float x) => (short)(x / 120f * 999f)),
			InitPoint(ControlPanelParameters.ValueWater1Temperature, 2, (float x) => (short)(x / 120f * 999f)),
			InitPoint(ControlPanelParameters.ValueWater23Temperature, 2, (float x) => (short)(x / 120f * 999f)),
			InitPoint(ControlPanelParameters.ValueTc, 2, (float x) => (short)(999f * x * 10.197162f / 10f)),
			InitPoint(ControlPanelParameters.ValueUr, 2, (float x) => (short)(999f * x * 10.197162f / 10f)),
			InitPoint(ControlPanelParameters.ValueGr, 2, (float x) => (short)(999f * x * 10.197162f / 16f)),
			InitPoint(ControlPanelParameters.ValueTm, 2, (float x) => (short)(999f * x * 10.197162f / 16f)),
			InitPoint(ControlPanelParameters.ValueBatteryPower, 2, (float x) => (short)((150f + x) / 300f * 999f)),
			InitPoint(ControlPanelParameters.ValueGeneratorVoltage, 2, (float x) => (short)(x * 999f)),
			InitPoint(ControlPanelParameters.ValueGeneratorPower, 2, (float x) => (short)(x / 6000f * 999f)),
			InitPoint(ControlPanelParameters.ValueDamageIndicator, 2, (float x) => (short)(x / 10f * 999f)),
			InitPoint(ControlPanelParameters.LampLoadShedding1),
			InitPoint(ControlPanelParameters.LampLoadShedding2),
			InitPoint(ControlPanelParameters.LampWaterLevel),
			InitPoint(ControlPanelParameters.LampOilPressure),
			InitPoint(ControlPanelParameters.LampLineBrakeOpen),
			InitPoint(ControlPanelParameters.LampBraked),
			InitPoint(ControlPanelParameters.LampFire),
			InitPoint(ControlPanelParameters.LampWorkDiesel2),
			InitPoint(ControlPanelParameters.LampCooling),
			InitPoint(ControlPanelParameters.LampEarthingRelay),
			InitPoint(ControlPanelParameters.LampVigilance),
			InitPoint(ControlPanelParameters.LampPss)
		};
		Buffer = new byte[BufferLength];
	}
}
