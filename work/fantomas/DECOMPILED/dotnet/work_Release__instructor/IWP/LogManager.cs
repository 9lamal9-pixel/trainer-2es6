using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using Newtonsoft.Json;

namespace IWP;

internal class LogManager : BaseManager<LogManager>, IUpdateable
{
	private CSVWriter _paramsWriter;

	private CSVWriter _errorsWriter;

	private CSVWriter _schemeWriter;

	private double _sumTime;

	public bool Enable;

	private const bool Debug = false;

	private FileStream _debugViolationFile;

	private string _fileName = "";

	private Dictionary<string, float> _paramsToLog;

	private Dictionary<string, float> _errorsToLog;

	private Dictionary<string, string> _schemeToLog;

	private bool _violationsUpdated;

	private bool _whistled;

	private double _whistleResetTimer;

	private float _previouseTrainSpeed;

	private bool _sentOverSpeed;

	public void CreateDataFile()
	{
		using StreamWriter streamWriter = new StreamWriter("RTS\\reports\\" + _fileName + ".data", append: false);
		streamWriter.WriteLine(PlayerSostavConfig.instance.StudentFIO);
		streamWriter.WriteLine(_fileName);
		streamWriter.WriteLine(DateTime.Now.ToString(new CultureInfo("ru-RU")));
		streamWriter.WriteLine(DateTime.Now.TimeOfDay);
		streamWriter.WriteLine(PlayerSostavConfig.instance.InstructorFIO);
		streamWriter.WriteLine(PlayerSostavConfig.instance.StudentPersonnelNumber);
		streamWriter.Close();
	}

	public void DataFileFinish()
	{
		using StreamWriter streamWriter = new StreamWriter("RTS\\reports\\" + _fileName + ".finish", append: false);
		streamWriter.WriteLine(DateTime.Now.TimeOfDay);
		streamWriter.Close();
	}

	public void MachinistAnswerMessage(string symptoms, string possibleMalfunction, string remedies, string status)
	{
		_schemeToLog["symptoms"] = symptoms;
		_schemeToLog["possible_malfunction"] = possibleMalfunction;
		_schemeToLog["remedies"] = remedies;
		_schemeToLog["status"] = status;
		_schemeToLog["Time"] = _sumTime.ToString(CultureInfo.InvariantCulture);
		_schemeWriter.StartWriting(append: true);
		_schemeWriter.WriteLine(new List<string>(_schemeToLog.Values));
		_schemeWriter.StopWriting();
	}

	public void PlayerTrainParamsMessage(float speedLoc, float speedLimit, float locomotiveBrakeCylinders, float brakeMagistral, float surgeChamber, float powerMagistral, bool compressor, bool epkKey, bool rb, bool rbs, bool knVK, bool knF, bool typhon, bool whistle, int crane395, bool tyga)
	{
		_paramsToLog["Time"] = (float)_sumTime;
		_paramsToLog["SpeedLoc"] = speedLoc;
		_paramsToLog["SpeedLimit"] = speedLimit;
		_paramsToLog["locomotiveBrakeCylinders"] = locomotiveBrakeCylinders;
		_paramsToLog["brakeMagistral"] = brakeMagistral;
		_paramsToLog["surgeChamber"] = surgeChamber;
		_paramsToLog["powerMagistral"] = powerMagistral;
		_paramsToLog["compressor"] = (compressor ? 1 : 0);
		_paramsToLog["epkControl"] = (epkKey ? 1 : 0);
		_paramsToLog["epkKey"] = (epkKey ? 1 : 0);
		_paramsToLog["rb"] = (rb ? 1 : 0);
		_paramsToLog["rbs"] = (rbs ? 1 : 0);
		_paramsToLog["knVK"] = (knVK ? 1 : 0);
		_paramsToLog["knF"] = (knF ? 1 : 0);
		_paramsToLog["typhon"] = (typhon ? 1 : 0);
		_paramsToLog["crane395"] = crane395;
		_paramsToLog["klub"] = (epkKey ? 1 : 0);
		_paramsToLog["tyga"] = (tyga ? 1 : 0);
		_paramsToLog["whistle"] = (whistle ? 1 : 0);
		_paramsToLog["KON"] = 0f;
		_paramsWriter.StartWriting(append: true);
		_paramsWriter.WriteLine(new List<float>(_paramsToLog.Values));
		_paramsWriter.StopWriting();
	}

	public override void PostInit()
	{
		bool flag = false;
		Dictionary<string, object> data = JsonConvert.DeserializeObject<Dictionary<string, object>>(File.ReadAllText("RTS\\\\protocol.json"));
		_fileName = data.GetString("protocolNumber");
		_paramsWriter = new CSVWriter("RTS\\reports\\" + _fileName + ".txt");
		_paramsToLog = new Dictionary<string, float>
		{
			{ "Time", 0f },
			{ "SpeedLoc", 0f },
			{ "SpeedLimit", 0f },
			{ "locomotiveBrakeCylinders", 0f },
			{ "brakeMagistral", 0f },
			{ "surgeChamber", 0f },
			{ "powerMagistral", 0f },
			{ "compressor", 0f },
			{ "epkControl", 0f },
			{ "epkKey", 0f },
			{ "rb", 0f },
			{ "rbs", 0f },
			{ "knVK", 0f },
			{ "knF", 0f },
			{ "typhon", 0f },
			{ "crane395", 0f },
			{ "klub", 0f },
			{ "tyga", 0f },
			{ "whistle", 0f },
			{ "KON", 0f },
			{ "trafficLight", 0f },
			{ "notWhistleOnSign1", 0f },
			{ "notWhistleOnSign3", 0f },
			{ "notWhistleOnSign4", 0f },
			{ "notWhistleOnSign5", 0f }
		};
		_paramsWriter.StartWriting(append: false);
		_paramsWriter.WriteLine(new List<string>(_paramsToLog.Keys));
		_paramsWriter.StopWriting();
		_errorsWriter = new CSVWriter("RTS\\reports\\" + _fileName + ".error");
		_errorsToLog = new Dictionary<string, float>
		{
			{ "Time", 0f },
			{ "Coord", 0f },
			{ "MoveWithoutSpotlight", 0f },
			{ "MoveWithoutWhistle", 0f },
			{ "SwitchingOffEpkOnTheMove", 0f },
			{ "AutomaticBrakingFromEPK", 0f },
			{ "AutomaticBrakingFromKON", 0f },
			{ "EmergencyBrakingNotSupportingByKM", 0f },
			{ "EmergencyBrakingNotSupportingByKvt", 0f },
			{ "InterruptedEmergencyBraking", 0f },
			{ "EmergencyBraking", 0f },
			{ "OverSpeed", 0f },
			{ "StoppingTrainAtPermissiveSignalLights", 0f },
			{ "CollisionWithObstacle", 0f },
			{ "NotWhistleOnSign", 0f }
		};
		_errorsWriter.StartWriting(append: false);
		_errorsWriter.WriteLine(new List<string>(_errorsToLog.Keys));
		_errorsWriter.StopWriting();
		_schemeWriter = new CSVWriter("RTS\\reports\\" + _fileName + ".symptoms");
		_schemeToLog = new Dictionary<string, string>
		{
			{ "symptoms", "" },
			{ "possible_malfunction", "" },
			{ "remedies", "" },
			{ "status", "" },
			{ "Time", "" }
		};
		_schemeWriter.StartWriting(append: false);
		_schemeWriter.WriteLine(new List<string>(_schemeToLog.Keys));
		_schemeWriter.StopWriting();
		string text = "protocol.json";
		data = JsonConvert.DeserializeObject<Dictionary<string, object>>(File.ReadAllText("RTS\\" + text));
		int num = data.GetInt("protocolNumber");
		data.SetInt("protocolNumber", num + 1);
		File.WriteAllText("RTS\\" + text, JsonConvert.SerializeObject(data));
		PlayerTrainParamsMessage(0f, 0f, 0f, 0f, 0f, 0f, compressor: false, epkKey: false, rb: false, rbs: false, knVK: false, knF: false, typhon: false, whistle: false, 0, tyga: false);
		BaseManager<GameManager>.instance.playerTrain.OnSignIndexChanged.AddListener(1, (int oldValue, int newValue) =>
		{
			if (oldValue == 6 && !_whistled)
			{
				BaseManager<LogManager>.instance.SetViolation(ViolationType.NotWhistleOnSign, BaseManager<GameManager>.instance.playerTrain.Coordinate);
			}
		});
		BaseManager<GameManager>.instance.playerTrain.OnObstructionDataChanged.AddListener(1, (ObstructionData oldValue, ObstructionData newValue) =>
		{
			if (oldValue != null && oldValue.actively)
			{
				BaseManager<LogManager>.instance.SetViolation(ViolationType.CollisionWithObstacle, BaseManager<GameManager>.instance.playerTrain.Coordinate);
			}
		});
	}

	public void Update()
	{
		if (MainDataStorage.Instance.DataStorage[ControlPanelParameters.BtnWhistle].Data != 0f)
		{
			_whistled = true;
			_whistleResetTimer = 5.0;
		}
		if (_whistled)
		{
			_whistleResetTimer -= BaseManager<TimeManager>.instance.DeltaTime;
			if (_whistleResetTimer <= 0.0)
			{
				_whistled = false;
			}
		}
		_sumTime += BaseManager<TimeManager>.instance.DeltaTime;
		if (_previouseTrainSpeed != BaseManager<GameManager>.instance.playerTrain.Speed)
		{
			if (_previouseTrainSpeed > 0f && BaseManager<GameManager>.instance.playerTrain.Speed == 0f && BaseManager<GameManager>.instance.GetRailRoadController<TrainMovementController>().GetRailRoadData().lights[BaseManager<GameManager>.instance.playerTrain.TrafficLightIndex].light == 0)
			{
				BaseManager<LogManager>.instance.SetViolation(ViolationType.StoppingTrainAtPermissiveSignalLights, BaseManager<GameManager>.instance.playerTrain.Coordinate);
			}
			_previouseTrainSpeed = BaseManager<GameManager>.instance.playerTrain.Speed;
		}
		if (!_sentOverSpeed && BaseManager<GameManager>.instance.playerTrain.Speed > (float)BaseManager<BLOKManager>.instance.GetAllowableSpeed + 2f && MainDataStorage.Instance.DataStorage[ControlPanelParameters.SwEpk].Data != 0f)
		{
			_sentOverSpeed = true;
			BaseManager<LogManager>.instance.SetViolation(ViolationType.OverSpeed, BaseManager<GameManager>.instance.playerTrain.Coordinate);
		}
		if (_sentOverSpeed && (BaseManager<GameManager>.instance.playerTrain.Speed < (float)BaseManager<BLOKManager>.instance.GetAllowableSpeed - 2f || BaseManager<GameManager>.instance.playerTrain.Speed == 0f))
		{
			_sentOverSpeed = false;
		}
		if (_violationsUpdated)
		{
			WriteViolationsToReport();
			_violationsUpdated = false;
			string[] array = _errorsToLog.Keys.ToArray();
			foreach (string key in array)
			{
				_errorsToLog[key] = 0f;
			}
		}
	}

	public void SetViolation(ViolationType type, float coordinate)
	{
		bool flag = false;
		_violationsUpdated = true;
		_errorsToLog["Time"] = (float)_sumTime;
		_errorsToLog["Coord"] = coordinate;
		_errorsToLog[type.ToString()] = 1f;
	}

	private void WriteViolationsToReport()
	{
		_errorsWriter.StartWriting(append: true);
		_errorsWriter.WriteLine(new List<float>(_errorsToLog.Values));
		_errorsWriter.StopWriting();
	}
}
