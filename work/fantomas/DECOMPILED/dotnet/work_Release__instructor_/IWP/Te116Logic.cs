using System.Collections.Generic;

namespace IWP;

public class Te116Logic
{
	private float _sendLogMassageStep;

	public static string[] eCircuitFaults = new string[15]
	{
		"Без нештатной ситуации", "Электродвигатель тн не работает", "Реле времени РВП1 неисправно", "Контактор крн включился, а зарядки аб нет", "При наборе первой позиции контроллера машиниста не включается рвз", "При включении тягового режима не нагружаются обе секции", "Не включилось реле РУ8", "Не включается РП1, перегорела катушка напряжения реле", "Не включаются мотор-вентиляторы холодильной камеры и не открываются жалюзи", "При нажатии на кнопку «ПУСК» маслопрокачивающий насос не работает",
		"При нажатии на кнопку «ПУСК» маслопрокачивающий насос включается, а при отпуске отключается", "Пуск дизеля происходит, но по истечении выдержки времени прокрутки вала дизель останавливается", "Дизель произвольно останавливается без срабатывания предельного выключателя", "Снижение давления масла в системе дизеля до предельной величины", "При автоматическом управлении холодильной камерой не включаются боковые жалюзи или электродвигатель вентилятора и жалюзи над ним"
	};

	public static string[] tBrakeFaults = new string[11]
	{
		"Без нештатной ситуации", "Прекращение подпитки ТМ в IV положении крана", "Повышение давления в УР во II положении крана", "Увеличение времени ликвидации сверхзарядного давления", "Занижение давления в УР в IV положении крана", "Ложное срабатывание СОТМ 418", "Обрыв ТМ в голове поезда", "Обрыв ТМ в хвосте поезда", "Недостаточная производительность компрессоров", "Завышенное зарядное давление",
		"Заниженное зарядное давление"
	};

	private ulong _activeSoundMask;

	public void Init(int numberOfLocomotiveSections, int wagonCount)
	{
		Te116ModuleImport.init(numberOfLocomotiveSections, wagonCount);
	}

	public void Update(float dt)
	{
		Dictionary<ControlPanelParameters, DataCell<float>> dataStorage = MainDataStorage.Instance.DataStorage;
		if (dataStorage == null)
		{
			return;
		}
		Te116ModuleImport.setMainController((int)dataStorage[ControlPanelParameters.MainController].Data);
		if (dataStorage[ControlPanelParameters.ReversController].Data == 2f)
		{
			Te116ModuleImport.setReversController(1);
		}
		else if (dataStorage[ControlPanelParameters.ReversController].Data == 1f)
		{
			Te116ModuleImport.setReversController(0);
		}
		else if (dataStorage[ControlPanelParameters.ReversController].Data == 0f)
		{
			Te116ModuleImport.setReversController(2);
		}
		Te116ModuleImport.setSwEpk(dataStorage[ControlPanelParameters.SwEpk].Data != 0f);
		Te116ModuleImport.setBtnSand(dataStorage[ControlPanelParameters.BtnSand].Data != 0f || dataStorage[ControlPanelParameters.SwPedal].Data != 0f);
		Te116ModuleImport.setBtnWhistle(dataStorage[ControlPanelParameters.BtnWhistle].Data != 0f);
		Te116ModuleImport.setBtnTyphon(dataStorage[ControlPanelParameters.BtnTyphon].Data != 0f);
		Te116ModuleImport.setSwGeneralControl(value: true);
		Te116ModuleImport.setSwHeater(value: true);
		Te116ModuleImport.setSwBlockingUnit(value: true);
		Te116ModuleImport.setSwSpotlightBright(dataStorage[ControlPanelParameters.SwSpotlightBright].Data != 0f);
		Te116ModuleImport.setSwSpotlightDull(dataStorage[ControlPanelParameters.SwSpotlightDull].Data != 0f);
		Te116ModuleImport.setBtnSandOnFirstAxis(dataStorage[ControlPanelParameters.BtnSandOnFirstAxis].Data != 0f);
		Te116ModuleImport.setSwCombinedCrane(0);
		Te116ModuleImport.setSwLocomotiveControl(dataStorage[ControlPanelParameters.SwLocomotiveControl].Data != 0f);
		Te116ModuleImport.setSwMotion(dataStorage[ControlPanelParameters.SwMotion].Data != 0f);
		Te116ModuleImport.setSwConversionManagement(dataStorage[ControlPanelParameters.SwConversionManagement].Data != 0f);
		Te116ModuleImport.setSwRefrigeratorManagement(dataStorage[ControlPanelParameters.SwRefrigeratorManagement].Data != 0f);
		Te116ModuleImport.setSwFan1(dataStorage[ControlPanelParameters.SwFan1].Data != 0f);
		Te116ModuleImport.setSwFan2(dataStorage[ControlPanelParameters.SwFan2].Data != 0f);
		Te116ModuleImport.setSwFan3(dataStorage[ControlPanelParameters.SwFan3].Data != 0f);
		Te116ModuleImport.setSwFan4(dataStorage[ControlPanelParameters.SwFan4].Data != 0f);
		Te116ModuleImport.setSwAlarmMonitoring(dataStorage[ControlPanelParameters.SwAlarmMonitoring].Data != 1f);
		Te116ModuleImport.setSwEmergencyStop(dataStorage[ControlPanelParameters.SwEmergencyStop].Data != 0f);
		Te116ModuleImport.setSwFuelPump1(dataStorage[ControlPanelParameters.SwFuelPump1].Data != 0f);
		Te116ModuleImport.setSwFuelPump2(dataStorage[ControlPanelParameters.SwFuelPump2].Data != 0f);
		Te116ModuleImport.setBtnLaunch1(dataStorage[ControlPanelParameters.BtnLaunch1].Data != 0f);
		Te116ModuleImport.setBtnLaunch2(dataStorage[ControlPanelParameters.BtnLaunch2].Data != 0f);
		if (dataStorage[ControlPanelParameters.SwDamageIndicator1].Data == 1f)
		{
			Te116ModuleImport.setSwDamageIndicator(1);
		}
		else
		{
			Te116ModuleImport.setSwDamageIndicator(2);
		}
		if (dataStorage[ControlPanelParameters.SwDamage1].Data == 1f)
		{
			Te116ModuleImport.setSwDamage(1);
		}
		else if (dataStorage[ControlPanelParameters.SwDamage2].Data == 1f)
		{
			Te116ModuleImport.setSwDamage(2);
		}
		else
		{
			Te116ModuleImport.setSwDamage(0);
		}
		Te116ModuleImport.setSwAutoFuelPump(value: true);
		Te116ModuleImport.setSwAutoDiesel(value: true);
		Te116ModuleImport.setSwAutoExciterManagement(value: true);
		Te116ModuleImport.setSwAutoCompressor(value: true);
		Te116ModuleImport.setSwAutoRefrigeratorManagement(value: true);
		Te116ModuleImport.setSwAutoBodyFan(value: true);
		Te116ModuleImport.setSwAutoLocomotiveAlarm(value: true);
		Te116ModuleImport.setSwKm((int)dataStorage[ControlPanelParameters.SwKm].Data);
		if (dataStorage[ControlPanelParameters.BtnBrakeRelease].Data == 1f)
		{
			Te116ModuleImport.setSwKvt(1);
		}
		else
		{
			if (dataStorage[ControlPanelParameters.SwKvtAnalog].Data == 152f)
			{
				Te116ModuleImport.setSwKvt(1);
			}
			if (dataStorage[ControlPanelParameters.SwKvtAnalog].Data < 152f && dataStorage[ControlPanelParameters.SwKvtAnalog].Data >= 145f)
			{
				Te116ModuleImport.setSwKvt(2);
			}
			if (dataStorage[ControlPanelParameters.SwKvtAnalog].Data < 145f && dataStorage[ControlPanelParameters.SwKvtAnalog].Data >= 132f)
			{
				Te116ModuleImport.setSwKvt(3);
			}
			if (dataStorage[ControlPanelParameters.SwKvtAnalog].Data < 132f && dataStorage[ControlPanelParameters.SwKvtAnalog].Data >= 119f)
			{
				Te116ModuleImport.setSwKvt(4);
			}
			if (dataStorage[ControlPanelParameters.SwKvtAnalog].Data < 119f && dataStorage[ControlPanelParameters.SwKvtAnalog].Data >= 101f)
			{
				Te116ModuleImport.setSwKvt(5);
			}
			if (dataStorage[ControlPanelParameters.SwKvtAnalog].Data < 101f)
			{
				Te116ModuleImport.setSwKvt(6);
			}
		}
		Te116ModuleImport.setHoldingCoilSignal(!GameConfig.Instance.IsActiveBlok || BaseManager<BLOKManager>.instance.GetEpkHoldingCoil != 0);
		Te116ModuleImport.update(dt);
		dataStorage[ControlPanelParameters.ValueTm].Data = Te116ModuleImport.getTmValueMPa();
		dataStorage[ControlPanelParameters.ValueTc].Data = Te116ModuleImport.getTcValueMPa();
		dataStorage[ControlPanelParameters.ValueTc2].Data = Te116ModuleImport.getTcValueMPa();
		dataStorage[ControlPanelParameters.ValueUr].Data = Te116ModuleImport.getUrValueMPa();
		dataStorage[ControlPanelParameters.ValueGr].Data = Te116ModuleImport.getPmValueMPa();
		dataStorage[ControlPanelParameters.ValueBatteryPower].Data = Te116ModuleImport.getBatteryPowerValue();
		dataStorage[ControlPanelParameters.ValueGeneratorVoltage].Data = Te116ModuleImport.getGeneratorVoltageValue();
		dataStorage[ControlPanelParameters.ValueGeneratorPower].Data = Te116ModuleImport.getGeneratorPowerValue();
		dataStorage[ControlPanelParameters.ValueDamageIndicator].Data = Te116ModuleImport.getDamageIndicatorValue();
		dataStorage[ControlPanelParameters.ValueWater1Temperature].Data = Te116ModuleImport.getWater1Temperature();
		dataStorage[ControlPanelParameters.ValueOil1Temperature].Data = Te116ModuleImport.getOil1Temperature();
		dataStorage[ControlPanelParameters.ValueWater23Temperature].Data = Te116ModuleImport.getWater23Temperature();
		dataStorage[ControlPanelParameters.ValueOil23Temperature].Data = Te116ModuleImport.getOil23Temperature();
		dataStorage[ControlPanelParameters.ValueOil1].Data = Te116ModuleImport.getOilPressure1();
		dataStorage[ControlPanelParameters.ValueOil23].Data = Te116ModuleImport.getOilPressure23();
		dataStorage[ControlPanelParameters.LampLoadShedding1].Data = (Te116ModuleImport.getLampLoadShedding1() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampLoadShedding2].Data = (Te116ModuleImport.getLampLoadShedding2() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampWaterLevel].Data = (Te116ModuleImport.getLampWaterLevel() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampOilPressure].Data = (Te116ModuleImport.getLampOilPressure() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampLineBrakeOpen].Data = (Te116ModuleImport.getLampLineBrakeOpen() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampBraked].Data = (Te116ModuleImport.getLampBraked() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampFire].Data = (Te116ModuleImport.getLampFire() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampWorkDiesel2].Data = (Te116ModuleImport.getLampWorkDiesel2() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampCooling].Data = (Te116ModuleImport.getLampCooling() ? 1 : 0);
		dataStorage[ControlPanelParameters.LampEarthingRelay].Data = (Te116ModuleImport.getLampEarthingRelay() ? 1 : 0);
		_activeSoundMask = Te116ModuleImport.getLocomotiveSoundMask();
		if ((_sendLogMassageStep += dt) >= 1f)
		{
			BaseManager<LogManager>.instance.PlayerTrainParamsMessage(BaseManager<GameManager>.instance.playerTrain.Speed, BaseManager<BLOKManager>.instance.GetAllowableSpeed, dataStorage[ControlPanelParameters.ValueTc2].Data, dataStorage[ControlPanelParameters.ValueTm].Data, dataStorage[ControlPanelParameters.ValueUr].Data, dataStorage[ControlPanelParameters.ValueGr].Data, Te116ModuleImport.getCompressorWorking(), dataStorage[ControlPanelParameters.SwEpk].Data != 0f, dataStorage[ControlPanelParameters.BtnRb].Data != 0f, dataStorage[ControlPanelParameters.BtnRbs].Data != 0f, knVK: false, knF: false, dataStorage[ControlPanelParameters.BtnTyphon].Data != 0f, dataStorage[ControlPanelParameters.BtnWhistle].Data != 0f, (int)dataStorage[ControlPanelParameters.SwKm].Data, BaseManager<GameManager>.instance.playerTrain.TractionForce > 0.1f);
			_sendLogMassageStep = 0f;
		}
		for (ViolationType violation = (ViolationType)Te116ModuleImport.getViolation(); violation != ViolationType.NoneType; violation = (ViolationType)Te116ModuleImport.getViolation())
		{
			BaseManager<LogManager>.instance.SetViolation(violation, BaseManager<GameManager>.instance.playerTrain.Coordinate);
		}
	}

	public bool ReadSound(int i)
	{
		return (_activeSoundMask & (ulong)(1L << i)) != 0;
	}
}
