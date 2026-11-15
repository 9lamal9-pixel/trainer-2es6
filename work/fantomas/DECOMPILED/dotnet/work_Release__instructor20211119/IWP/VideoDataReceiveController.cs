using System;
using System.Collections.Generic;
using System.Threading;

namespace IWP;

internal class VideoDataReceiveController : BaseController<VideoDataReceiveController>, IUpdateable
{
	public enum SoundIdx
	{
		WhistleSoundIdx,
		SignalSoundIdx,
		SandSoundIdx,
		CompressorSoundIdx,
		FuelPumpSoundIdx,
		OilPumpSoundIdx,
		DieselPos0SoundIdx,
		DieselPos1SoundIdx,
		DieselPos2SoundIdx,
		DieselPos3SoundIdx,
		DieselPos4SoundIdx,
		DieselPos5SoundIdx,
		DieselPos6SoundIdx,
		DieselPos7SoundIdx,
		DieselPos8SoundIdx,
		DieselPos9SoundIdx,
		DieselPos10SoundIdx,
		DieselPos11SoundIdx,
		DieselPos12SoundIdx,
		DieselPos13SoundIdx,
		DieselPos14SoundIdx,
		DieselPos15SoundIdx,
		Diesel2Pos0SoundIdx,
		Diesel2Pos1SoundIdx,
		Diesel2Pos2SoundIdx,
		Diesel2Pos3SoundIdx,
		Diesel2Pos4SoundIdx,
		Diesel2Pos5SoundIdx,
		Diesel2Pos6SoundIdx,
		Diesel2Pos7SoundIdx,
		Diesel2Pos8SoundIdx,
		Diesel2Pos9SoundIdx,
		Diesel2Pos10SoundIdx,
		Diesel2Pos11SoundIdx,
		Diesel2Pos12SoundIdx,
		Diesel2Pos13SoundIdx,
		Diesel2Pos14SoundIdx,
		Diesel2Pos15SoundIdx,
		HeaterSoundIdx,
		Blinds1OpenSoundIdx,
		Blinds1CloseSoundIdx,
		Blinds2OpenSoundIdx,
		Blinds2CloseSoundIdx,
		DieselStart1SoundIdx,
		DieselStop1SoundIdx,
		DieselStart2SoundIdx,
		DieselStop2SoundIdx,
		Km254InflateSoundIdx,
		Km254DeflateSoundIdx,
		Km395OverchargingSoundIdx,
		Km395OverchargingLiquidationSoundIdx,
		Km395_2Inflate1SoundIdx,
		Km395_2Inflate2SoundIdx,
		Km395_2Inflate3SoundIdx,
		Km395_5aSoundIdx,
		Km395_5SoundIdx,
		Km395_6SoundIdx,
		WhistleEPKLoud,
		WhistleEPKQuiet,
		SrivEPKIdx,
		NumSoundIndices,
		Knok1
	}

	private ClientVideoManager videoManager;

	private BLOKManager blokManager;

	private Train playerTrain;

	private RailRoadData railRoadData;

	private bool[] soundStates;

	private bool[] oldSoundStates;

	private float speedLimit = 80f;

	private float trainCoord;

	public override void Init()
	{
		base.Init();
		videoManager = BaseManager<ClientVideoManager>.instance;
		blokManager = BaseManager<BLOKManager>.instance;
		playerTrain = BaseManager<GameManager>.instance.playerTrain;
		railRoadData = BaseManager<GameManager>.instance.GetRailRoadController<TrainMovementController>().GetRailRoadData();
		soundStates = new bool[60];
		oldSoundStates = new bool[60];
		if (!GameConfig.Instance.IsActiveVideo)
		{
			return;
		}
		railRoadData.OnTrafficLightCodeChanged.AddListener(this, (TrafficLightData x) =>
		{
			videoManager.SendTrafficLightCodeByQueue(x.index, (byte)x.light);
		});
		railRoadData.OnTimeOfDayChanged.AddListener(this, (bool x) =>
		{
			videoManager.SendWeather(Weather.Night, !x);
		});
		railRoadData.OnSetRain.AddListener(this, (bool x) =>
		{
			videoManager.SendWeather(Weather.Rain, x);
		});
		railRoadData.OnSetSnow.AddListener(this, (bool x) =>
		{
			videoManager.SendWeather(Weather.Snow, x);
		});
		railRoadData.OnSetFog.AddListener(this, (bool x) =>
		{
			videoManager.SendWeather(Weather.Fog, x);
		});
		railRoadData.OnAddSign.AddListener(this, (SignData x) =>
		{
			videoManager.SendSign((byte)x.type, x.coord);
		});
		railRoadData.OnSetObstructionActive.AddListener(this, (ObstructionData x) =>
		{
			string type = x.type;
			if (type == "cars")
			{
				videoManager.Send3DObjectState(x.id, x.actively, 1);
			}
			else
			{
				videoManager.Send3DObjectState(x.id, x.actively, 0);
			}
		});
		for (int num = 0; num < railRoadData.lights.Count; num++)
		{
			videoManager.SendTrafficLightCodeByQueue(railRoadData.lights[num].index, (byte)railRoadData.lights[num].light);
		}
		for (int num2 = 0; num2 < railRoadData.signals.Count; num2++)
		{
			videoManager.SendSignByQueue((byte)railRoadData.signals[num2].type, railRoadData.signals[num2].coord);
		}
		foreach (KeyValuePair<string, List<ObstructionData>> obstruction in railRoadData.obstructions)
		{
			foreach (ObstructionData item in obstruction.Value)
			{
				if (item.actively)
				{
					videoManager.Send3DObjectState(item.id, item.actively, 0);
				}
			}
		}
		UIManager instance = BaseManager<UIManager>.instance;
		instance.OnPauseButtonStateChanged = (Action<bool>)Delegate.Combine(instance.OnPauseButtonStateChanged, new Action<bool>(Pause));
	}

	public void Update()
	{
		if (!GameConfig.Instance.IsActiveVideo)
		{
			return;
		}
		for (int i = 0; i < soundStates.Length; i++)
		{
			soundStates[i] = playerTrain.TrainLogic.ReadSound(i);
		}
		for (int j = 0; j < 60; j++)
		{
			if (soundStates[j] != oldSoundStates[j])
			{
				videoManager.SendPlaySoundByQueue(j, soundStates[j]);
				oldSoundStates[j] = soundStates[j];
			}
		}
		if ((int)MainDataStorage.Instance.DataStorage[ControlPanelParameters.SwSpotlightBright].Data == 1 || (int)MainDataStorage.Instance.DataStorage[ControlPanelParameters.SwSpotlightDull].Data == 1)
		{
			videoManager.SendSpotlightState(1);
		}
		else
		{
			videoManager.SendSpotlightState(0);
		}
		speedLimit = blokManager.GetAllowableSpeed;
		if (playerTrain.Coordinate - trainCoord > 5000f)
		{
			trainCoord += 5000f;
			videoManager.SendTrainPositionByQueue(trainCoord, playerTrain.Speed, speedLimit);
			Thread.Sleep(200);
		}
		else
		{
			videoManager.SendTrainPosition(playerTrain.Coordinate, playerTrain.Speed, speedLimit);
			trainCoord = playerTrain.Coordinate;
		}
	}

	private void Pause(bool state)
	{
		if (!state)
		{
			return;
		}
		for (int i = 0; i < 60; i++)
		{
			if (soundStates[i])
			{
				videoManager.SendPlaySoundByQueue(i, state: false);
			}
			oldSoundStates[i] = false;
		}
	}
}
