using System.Runtime.InteropServices;

namespace IWP;

public class Te116ModuleImport
{
	[DllImport("Lib/2Te116.dll")]
	public static extern int init(int numberOfLocomotiveSections, int wagonCount);

	[DllImport("Lib/2Te116.dll")]
	public static extern int update(float dt);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setMainController(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setReversController(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwEpk(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setBtnSand(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setBtnWhistle(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setBtnTyphon(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwGeneralControl(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwHeater(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwBlockingUnit(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwSpotlightBright(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwSpotlightDull(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setBtnSandOnFirstAxis(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwCombinedCrane(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwLocomotiveControl(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwMotion(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwConversionManagement(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwRefrigeratorManagement(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwFan1(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwFan2(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwFan3(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwFan4(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAlarmMonitoring(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwEmergencyStop(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwFuelPump1(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwFuelPump2(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setBtnLaunch1(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setBtnLaunch2(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwDamageIndicator(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwDamage(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAutoFuelPump(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAutoDiesel(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAutoExciterManagement(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAutoCompressor(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAutoRefrigeratorManagement(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAutoBodyFan(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwAutoLocomotiveAlarm(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwKm(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setSwKvt(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setAbsTrainSpeedKmH(float value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setHoldingCoilSignal(bool value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setBrakeFault(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern void setElectricFault(int value);

	[DllImport("Lib/2Te116.dll")]
	public static extern float getTmValueMPa();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getTcValueMPa();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getUrValueMPa();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getPmValueMPa();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getBatteryPowerValue();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getGeneratorVoltageValue();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getGeneratorPowerValue();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getDamageIndicatorValue();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getWater1Temperature();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getOil1Temperature();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getWater23Temperature();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getOil23Temperature();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getOilPressure1();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getOilPressure23();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampLoadShedding1();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampLoadShedding2();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampWaterLevel();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampOilPressure();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampLineBrakeOpen();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampBraked();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampFire();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampWorkDiesel2();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampCooling();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getLampEarthingRelay();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getCompressorWorking();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getTractionForce();

	[DllImport("Lib/2Te116.dll")]
	public static extern float getBrakingPower(int index);

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getAutomaticBrakingFromEPK();

	[DllImport("Lib/2Te116.dll")]
	public static extern bool getAutomaticBrakingFromKON();

	[DllImport("Lib/2Te116.dll")]
	public static extern ulong getLocomotiveSoundMask();

	[DllImport("Lib/2Te116.dll")]
	public static extern int getViolation();
}
