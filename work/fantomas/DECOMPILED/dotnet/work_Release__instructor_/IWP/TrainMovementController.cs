using System.Collections.Generic;

namespace IWP;

internal class TrainMovementController : BaseRailRoadController
{
	private List<ObstructionData> _obstructionDatas = new List<ObstructionData>();

	protected override void Init()
	{
		base.Init();
		TrainModuleImport.Init();
		Dictionary<int, ObstructionData> dictionary = new Dictionary<int, ObstructionData>();
		foreach (KeyValuePair<string, List<ObstructionData>> obstruction in railRoadData.obstructions)
		{
			foreach (ObstructionData item in obstruction.Value)
			{
				dictionary.Add(item.id, item);
			}
		}
		for (int i = 0; i < dictionary.Count; i++)
		{
			_obstructionDatas.Add(dictionary[i]);
		}
	}

	public override void Update()
	{
		if (BaseManager<TimeManager>.instance.DeltaTime != 0.0)
		{
			TrainModuleImport.Update((float)BaseManager<TimeManager>.instance.DeltaTime);
		}
		for (int i = 0; i < trains.Count; i++)
		{
			Train train = trains[i];
			train.Update(BaseManager<TimeManager>.instance.DeltaTime);
			int[] array = CheckProfile(train);
			int[] array2 = CheckRadius(train);
			for (int j = 0; j < train.wagons.Count; j++)
			{
				train.wagons[j].profileIndex = array[j];
				train.wagons[j].profile = (float)railRoadData.profile[array[j]].slope;
				TrainModuleImport.WagonSetSlope(train.wagons[j].Id, train.wagons[j].profile);
				train.wagons[j].radiusIndex = array2[j];
				train.wagons[j].radius = railRoadData.plan[array2[j]].radius;
				TrainModuleImport.WagonSetCurve(train.wagons[j].Id, railRoadData.plan[array2[j]].radius);
			}
			int num = CheckPiket(train);
			int num2 = CheckStation(train);
			int num3 = CheckTrafficLight(train);
			int num4 = CheckSign(train);
			int num5 = CheckDangerousSection(train);
			ObstructionData obstructionData = CheckObstruction(train);
			if (num != train.PiketIndex)
			{
				train.PiketIndex = num;
			}
			if (num2 != train.StationIndex)
			{
				train.StationIndex = num2;
			}
			if (num3 != train.TrafficLightIndex)
			{
				train.TrafficLightIndex = num3;
			}
			if (num4 != train.SignIndex)
			{
				train.SignIndex = num4;
			}
			if (num5 != train.DangerousSectionSignIndex)
			{
				train.DangerousSectionSignIndex = num5;
			}
			if (obstructionData != train.Obstruction)
			{
				train.Obstruction = obstructionData;
			}
		}
	}

	public Train SpawnTrain(int coordinate)
	{
		int trainId = CreateTrain();
		Train train = GetTrain(trainId);
		PlayerTrainStartConfig instance = PlayerTrainStartConfig.instance;
		train.AddWagon(new Wagon(-1, new WagonData(WagonType.TE116, instance.Weight, (float)instance.Length * 0.01f / (float)instance.NumberSections, autoMode: true, 1, airDiffuserOn: true, AirDiffuserMode.Medium, AirDiffuserRegime.Plain)));
		train.AddWagon(new Wagon(0, new WagonData(WagonType.TE116, instance.Weight, (float)instance.Length * 0.01f / (float)instance.NumberSections, autoMode: true, 1, airDiffuserOn: true, AirDiffuserMode.Medium, AirDiffuserRegime.Plain)));
		DbReader dbReader = new DbReader();
		train.AddWagon(dbReader.GetWagons(PlayerSostavConfig.instance.SostavCompozitionId));
		train.wagons.ForEach((Wagon wagon) =>
		{
			TrainModuleImport.CreateWagon(wagon.Id);
			TrainModuleImport.WagonSetLength(wagon.Id, wagon.Length);
			TrainModuleImport.WagonSetMass(wagon.Id, wagon.WeightBrutto);
			TrainModuleImport.TrainAddWagon(trainId, wagon.Id, 0);
		});
		TrainModuleImport.TrainSetMainWagon(trainId, -1);
		TrainModuleImport.TrainSetPosition(trainId, coordinate);
		return train;
	}

	public void DestroyTrain(int trainId)
	{
		Train train = GetTrain(trainId);
		RemoveTrain(trainId);
	}

	private int[] CheckProfile(Train train)
	{
		int[] array = new int[train.wagons.Count];
		for (int i = 0; i < array.Length; i++)
		{
			array[i] = ((train.wagons[i].profileIndex == 0) ? train.wagons[i].profileIndex : (train.wagons[i].profileIndex - 1));
			if (array[i] < railRoadData.profile.Count)
			{
				ProfileData profileData = railRoadData.profile[array[i]];
				float num = train.wagons[0].Coordinate + train.wagons[i].Length * (float)i;
				while (num >= (float)(profileData.coord + profileData.distance) && array[i] < railRoadData.profile.Count)
				{
					array[i]++;
					profileData = railRoadData.profile[array[i]];
				}
			}
		}
		return array;
	}

	private int[] CheckRadius(Train train)
	{
		int[] array = new int[train.wagons.Count];
		for (int i = 0; i < array.Length; i++)
		{
			array[i] = ((train.wagons[i].radiusIndex == 0) ? train.wagons[i].radiusIndex : (train.wagons[i].radiusIndex - 1));
			if (array[i] < railRoadData.plan.Count - 1)
			{
				CurveData curveData = railRoadData.plan[array[i]];
				CurveData curveData2 = railRoadData.plan[array[i] + 1];
				float num = train.wagons[0].Coordinate + train.wagons[i].Length * (float)i;
				while (num >= (float)curveData2.coord && array[i] < railRoadData.plan.Count - 2)
				{
					array[i]++;
					curveData = railRoadData.plan[array[i]];
					curveData2 = railRoadData.plan[array[i] + 1];
				}
			}
			else
			{
				array[i] = railRoadData.plan.Count - 1;
			}
		}
		return array;
	}

	private int CheckPiket(Train train)
	{
		int i = train.PiketIndex;
		if (i > 0)
		{
			i--;
		}
		for (; i < railRoadData.pikets.Count; i++)
		{
			PiketData piketData = railRoadData.pikets[i];
			float coordinate = train.Coordinate;
			if (!(coordinate > (float)piketData.coord))
			{
				break;
			}
		}
		if (i >= railRoadData.pikets.Count)
		{
			i = railRoadData.pikets.Count - 1;
		}
		return i;
	}

	private int CheckStation(Train train)
	{
		int i = train.StationIndex;
		if (i > 0)
		{
			i--;
		}
		for (; i < railRoadData.stations.Count; i++)
		{
			StationData stationData = railRoadData.stations[i];
			float coordinate = train.Coordinate;
			if (!(coordinate > (float)stationData.coord))
			{
				break;
			}
		}
		if (i >= railRoadData.stations.Count)
		{
			i = railRoadData.stations.Count - 1;
		}
		return i;
	}

	private int CheckTrafficLight(Train train)
	{
		int i = train.TrafficLightIndex;
		if (i > 0)
		{
			i--;
		}
		for (; i < railRoadData.lights.Count; i++)
		{
			TrafficLightData trafficLightData = railRoadData.lights[i];
			float coordinate = train.Coordinate;
			if (!(coordinate > (float)trafficLightData.coord))
			{
				break;
			}
		}
		if (i >= railRoadData.lights.Count)
		{
			i = railRoadData.lights.Count - 1;
		}
		return i;
	}

	private int CheckSign(Train train)
	{
		int i = train.SignIndex;
		if (i > 0)
		{
			i--;
		}
		for (; i < railRoadData.signals.Count; i++)
		{
			SignData signData = railRoadData.signals[i];
			float coordinate = train.Coordinate;
			if (!(coordinate > (float)signData.coord))
			{
				break;
			}
		}
		if (i >= railRoadData.signals.Count)
		{
			i = railRoadData.signals.Count - 1;
		}
		return i;
	}

	private int CheckDangerousSection(Train train)
	{
		int i = train.DangerousSectionSignIndex;
		if (i > 0)
		{
			i--;
		}
		for (; i < railRoadData.dangerousSectionSigns.Count; i++)
		{
			SignData signData = railRoadData.dangerousSectionSigns[i];
			float coordinate = train.Coordinate;
			if (!(coordinate > (float)signData.coord))
			{
				break;
			}
		}
		if (i >= railRoadData.dangerousSectionSigns.Count)
		{
			i = railRoadData.dangerousSectionSigns.Count - 1;
		}
		return i;
	}

	private ObstructionData CheckObstruction(Train train)
	{
		int i;
		for (i = train.Obstruction?.id ?? 0; i < _obstructionDatas.Count; i++)
		{
			ObstructionData obstructionData = _obstructionDatas[i];
			float coordinate = train.Coordinate;
			if (!(coordinate > (float)obstructionData.coord))
			{
				break;
			}
		}
		if (i >= _obstructionDatas.Count)
		{
			i = _obstructionDatas.Count - 1;
		}
		return _obstructionDatas[i];
	}
}
