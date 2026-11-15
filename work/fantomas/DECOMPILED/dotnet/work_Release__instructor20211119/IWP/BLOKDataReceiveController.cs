using System;
using System.Collections.Generic;

namespace IWP;

internal class BLOKDataReceiveController : BaseController<BLOKDataReceiveController>, IUpdateable
{
	private enum Keys
	{
		NONE,
		P,
		K,
		R,
		OTM,
		VK,
		_1,
		_2,
		_3,
		RMP,
		_4,
		_5,
		_6,
		F,
		_7,
		_8,
		_9,
		BRIGHTNESS,
		STR,
		_0,
		INPUT,
		PULL,
		OTPR,
		OS,
		K20
	}

	private BLOKManager _blokManager;

	private Train _playerTrain;

	private Dictionary<ControlPanelParameters, DataCell<float>> _pult;

	private RailRoadData _railRoadData;

	private int _currentAllowableSpeedByEk = 1000;

	private int _currentTargetSpeedByEk = 1000;

	~BLOKDataReceiveController()
	{
		if (_railRoadData != null)
		{
			_railRoadData.OnTrafficLightCodeChanged.Clear(this);
		}
		if (_playerTrain != null)
		{
			_playerTrain.OnTrafficLightIndexChanged.Clear(this);
		}
	}

	public override void Init()
	{
		base.Init();
		_blokManager = BaseManager<BLOKManager>.instance;
		_playerTrain = BaseManager<GameManager>.instance.playerTrain;
		_pult = MainDataStorage.Instance.DataStorage;
		_railRoadData = BaseManager<GameManager>.instance.GetRailRoadController<TrainMovementController>().GetRailRoadData();
		if (!GameConfig.Instance.IsActiveBlok)
		{
			return;
		}
		_railRoadData.OnTrafficLightCodeChanged.AddListener(this, (TrafficLightData x) =>
		{
			if (x.index == _playerTrain.TrafficLightIndex)
			{
				TrafficLight.GetMode(x.light, out var _, out var blokMode2);
				_blokManager.SendTrafficLightCode = (byte)blokMode2;
			}
		});
		_playerTrain.OnTrafficLightIndexChanged.AddListener(this, (int x) =>
		{
			TrafficLight.GetMode(_railRoadData.lights[x].light, out var _, out var blokMode2);
			_blokManager.SendTrafficLightCode = (byte)blokMode2;
			_blokManager.SendTrafficLightName = _railRoadData.lights[x].name;
			_blokManager.SendTargetName = _railRoadData.lights[x].name;
		});
		_playerTrain.OnStationIndexChanged.AddListener(this, (int x) =>
		{
			if (x < _railRoadData.stations.Count)
			{
				_blokManager.SendStationName = _railRoadData.stations[x].name.ToUpper();
			}
		});
		_playerTrain.OnDangerousSectionSignIndexChanged.AddListener(this, (int x) =>
		{
			if (_railRoadData.dangerousSectionSigns.Count != 0 && _railRoadData.dangerousSectionSigns.Count > x)
			{
				SignData signData = _railRoadData.dangerousSectionSigns[x];
				if (signData.type == 7 || signData.type == 8)
				{
					if (x > 0)
					{
						if (_railRoadData.dangerousSectionSigns[x - 1].type == 7 || _railRoadData.dangerousSectionSigns[x - 1].type == 8)
						{
							_currentAllowableSpeedByEk = _railRoadData.dangerousSectionSigns[x - 1].speedLimit;
						}
						else
						{
							_currentAllowableSpeedByEk = 80;
						}
					}
					else
					{
						_currentAllowableSpeedByEk = 80;
					}
					_blokManager.SendAllowableSpeedByEK = (short)_currentAllowableSpeedByEk;
					_currentTargetSpeedByEk = 1000;
					_blokManager.SendTargetSpeedByEK = (short)_currentTargetSpeedByEk;
				}
				else if (signData.type == 3 || signData.type == 9)
				{
					if (x > 0)
					{
						if (_railRoadData.dangerousSectionSigns[x - 1].type == 7 || _railRoadData.dangerousSectionSigns[x - 1].type == 8)
						{
							_currentAllowableSpeedByEk = _railRoadData.dangerousSectionSigns[x - 1].speedLimit;
						}
						else
						{
							_currentAllowableSpeedByEk = 1000;
						}
					}
					else
					{
						_currentAllowableSpeedByEk = 1000;
					}
					_blokManager.SendAllowableSpeedByEK = (short)_currentAllowableSpeedByEk;
					_currentTargetSpeedByEk = 1000;
					_blokManager.SendTargetSpeedByEK = (short)_currentTargetSpeedByEk;
				}
			}
		});
		_railRoadData.OnAddSign.AddListener(this, (SignData x) =>
		{
			if (x.type == 7 || x.type == 8)
			{
				if (_railRoadData.dangerousSectionSigns.Count > 0)
				{
					if ((float)x.coord > _playerTrain.Coordinate && _railRoadData.signals[_playerTrain.DangerousSectionSignIndex].coord > x.coord)
					{
						_currentTargetSpeedByEk = x.speedLimit;
						_railRoadData.dangerousSectionSigns.Add(x);
						_blokManager.SendTargetSpeedByEK = (short)_currentTargetSpeedByEk;
					}
				}
				else
				{
					_currentTargetSpeedByEk = x.speedLimit;
					_blokManager.SendTargetSpeedByEK = (short)_currentTargetSpeedByEk;
				}
			}
			else if (x.type == 3 || x.type == 9)
			{
				if (_railRoadData.dangerousSectionSigns.Count > 0)
				{
					if ((float)x.coord > _playerTrain.Coordinate && _railRoadData.signals[_playerTrain.DangerousSectionSignIndex].coord > x.coord)
					{
						_currentTargetSpeedByEk = 1000;
						_blokManager.SendTargetSpeedByEK = (short)_currentTargetSpeedByEk;
					}
				}
				else
				{
					_currentTargetSpeedByEk = 1000;
					_blokManager.SendTargetSpeedByEK = (short)_currentTargetSpeedByEk;
				}
			}
		});
		TrafficLight.GetMode(_railRoadData.lights[_playerTrain.TrafficLightIndex].light, out var _, out var blokMode);
		_blokManager.SendTrafficLightCode = (byte)blokMode;
		_blokManager.SendTrafficLightName = _railRoadData.lights[_playerTrain.TrafficLightIndex].name;
		_blokManager.SendTargetName = _railRoadData.lights[_playerTrain.TrafficLightIndex].name;
		_blokManager.SendStationName = _railRoadData.stations[_playerTrain.StationIndex].name;
		_blokManager.SendSautEquipped = 0;
		_blokManager.SendEkEquipped = 1;
		_blokManager.SendCassetteEquipped = 1;
		_blokManager.SendBrakingCoefficient = 0.33f;
	}

	public void Update()
	{
		if (GameConfig.Instance.IsActiveBlok)
		{
			_blokManager.SendBlockPower = 1;
			_blokManager.SendEpkButton = (byte)_pult[ControlPanelParameters.SwEpk].Data;
			_blokManager.SendTraction = ((_playerTrain.TractionForce > 0.5f) ? ((byte)1) : ((byte)0));
			_blokManager.SendPmValue = _pult[ControlPanelParameters.ValueGr].Data;
			_blokManager.SendTmValue = _pult[ControlPanelParameters.ValueTm].Data;
			_blokManager.SendUr1Value = _pult[ControlPanelParameters.ValueUr].Data;
			_blokManager.SendUr2Value = _pult[ControlPanelParameters.ValueUr].Data;
			_blokManager.SendTcValue = _pult[ControlPanelParameters.ValueTc].Data;
			int num = 0;
			if (_pult[ControlPanelParameters.ReversController].Data == 2f)
			{
				num = 1;
			}
			else if (_pult[ControlPanelParameters.ReversController].Data == 1f)
			{
				num = 0;
			}
			else if (_pult[ControlPanelParameters.ReversController].Data == 0f)
			{
				num = 2;
			}
			_blokManager.SendReverse = (byte)((_playerTrain.Speed != 0f) ? ((uint)num) : 0u);
			_blokManager.SendWorldTime = DateTime.UtcNow.Millisecond;
			_blokManager.SendKeyboardButton = (byte)KeyboardFromPultToBlock((int)_pult[ControlPanelParameters.SwClubDecoder].Data);
			_blokManager.SendWorldTime = (long)DateTime.Now.Subtract(new DateTime(1970, 1, 1)).TotalMilliseconds;
			_blokManager.SendRbButton = (byte)_pult[ControlPanelParameters.BtnRb].Data;
			_blokManager.SendRbsButton = (byte)_pult[ControlPanelParameters.BtnRbs].Data;
			_blokManager.SendRbpButton = 0;
			_blokManager.SendActualSpeed = _playerTrain.Speed;
			_blokManager.SendAcceleration = _playerTrain.Acceleration;
			_blokManager.SendRoadBias = _playerTrain.wagons[0].profile;
			_blokManager.SendDistanceToTrafficLight = (short)(_railRoadData.lights[_playerTrain.TrafficLightIndex].coord - (int)_playerTrain.Coordinate);
			_blokManager.SendDistanceToTarget = (short)(_railRoadData.lights[_playerTrain.TrafficLightIndex].coord - (int)_playerTrain.Coordinate);
			_blokManager.SendTargetCode = 1;
			_blokManager.SendRoadIndex = 0;
			_blokManager.SendVideoCoord = 0;
			if (_playerTrain.PiketIndex < _railRoadData.pikets.Count)
			{
				_blokManager.SendCoordinateGps = _railRoadData.pikets[_playerTrain.PiketIndex].km * 1000 + _railRoadData.pikets[_playerTrain.PiketIndex].name * 100 + ((int)_playerTrain.Coordinate - _railRoadData.pikets[_playerTrain.PiketIndex].coord);
				_blokManager.SendPicket = (byte)_railRoadData.pikets[_playerTrain.PiketIndex].name;
			}
			_blokManager.SendBrakingValveOpenedFromEPK = (byte)(Te116ModuleImport.getAutomaticBrakingFromEPK() ? 1u : 0u);
			_blokManager.SendBrakingValveOpenedFromKOH = (byte)(Te116ModuleImport.getAutomaticBrakingFromKON() ? 1u : 0u);
			if (MainDataStorage.Instance.DataStorage[ControlPanelParameters.SwEpk].Data == 1f)
			{
				MainDataStorage.Instance.DataStorage[ControlPanelParameters.LampPss].Data = (int)_blokManager.GetLampPssPass;
				MainDataStorage.Instance.DataStorage[ControlPanelParameters.LampVigilance].Data = (int)_blokManager.GetLampAttention;
			}
			else
			{
				DataCell<float> dataCell = MainDataStorage.Instance.DataStorage[ControlPanelParameters.LampPss];
				float data = (MainDataStorage.Instance.DataStorage[ControlPanelParameters.LampVigilance].Data = 0f);
				dataCell.Data = data;
			}
		}
	}

	public int TrafficLightFromBlockToPult(int code)
	{
		return code switch
		{
			0 => 8, 
			1 => 1, 
			2 => 5, 
			3 => 6, 
			4 => 7, 
			_ => 0, 
		};
	}

	private int KeyboardFromPultToBlock(int key)
	{
		return key switch
		{
			1 => 1, 
			2 => 20, 
			4 => 14, 
			5 => 15, 
			6 => 16, 
			9 => 4, 
			11 => 10, 
			12 => 11, 
			13 => 12, 
			14 => 2, 
			15 => 18, 
			16 => 19, 
			17 => 6, 
			18 => 7, 
			19 => 8, 
			20 => 5, 
			21 => 9, 
			22 => 13, 
			23 => 21, 
			24 => 22, 
			25 => 23, 
			26 => 24, 
			_ => 0, 
		};
	}
}
