using System.Collections.Generic;

namespace IWP;

internal class Train
{
	public CustomActionEvent<int> OnPicketIndexChanged;

	public CustomActionEvent<int> OnStationIndexChanged;

	public CustomActionEvent<int> OnTrafficLightIndexChanged;

	public CustomActionEvent<int, int> OnSignIndexChanged;

	public CustomActionEvent<int> OnDangerousSectionSignIndexChanged;

	public CustomActionEvent<ObstructionData, ObstructionData> OnObstructionDataChanged;

	public List<Wagon> wagons;

	private int _piketIndex;

	private int _stationIndex;

	private int _trafficLightIndex;

	private int _signIndex;

	private int _dangerousSectionSignIndex;

	private ObstructionData _obstruction;

	public Te116Logic TrainLogic;

	public int Id { get; }

	public float Coordinate => TrainModuleImport.TrainGetPosition(Id);

	public float Speed => TrainModuleImport.TrainGetVelocity(Id);

	public float Acceleration => TrainModuleImport.TrainGetAcceleration(Id);

	public int PiketIndex
	{
		get
		{
			return _piketIndex;
		}
		set
		{
			_piketIndex = value;
			OnPicketIndexChanged.Invoke(_piketIndex);
		}
	}

	public int StationIndex
	{
		get
		{
			return _stationIndex;
		}
		set
		{
			_stationIndex = value;
			OnStationIndexChanged.Invoke(_stationIndex);
		}
	}

	public int TrafficLightIndex
	{
		get
		{
			return _trafficLightIndex;
		}
		set
		{
			_trafficLightIndex = value;
			OnTrafficLightIndexChanged.Invoke(_trafficLightIndex);
		}
	}

	public int SignIndex
	{
		get
		{
			return _signIndex;
		}
		set
		{
			OnSignIndexChanged.Invoke(_signIndex, value);
			_signIndex = value;
		}
	}

	public int DangerousSectionSignIndex
	{
		get
		{
			return _dangerousSectionSignIndex;
		}
		set
		{
			_dangerousSectionSignIndex = value;
			OnDangerousSectionSignIndexChanged.Invoke(_dangerousSectionSignIndex);
		}
	}

	public ObstructionData Obstruction
	{
		get
		{
			return _obstruction;
		}
		set
		{
			OnObstructionDataChanged.Invoke(_obstruction, value);
			_obstruction = value;
		}
	}

	public float TractionForce => Te116ModuleImport.getTractionForce();

	public float GetTrainLength()
	{
		float length = 0f;
		wagons.ForEach((Wagon wagon) =>
		{
			length += wagon.Length;
		});
		return length;
	}

	public Train(int id)
	{
		Id = id;
		wagons = new List<Wagon>();
		TrainLogic = new Te116Logic();
		OnPicketIndexChanged = new CustomActionEvent<int>();
		OnStationIndexChanged = new CustomActionEvent<int>();
		OnTrafficLightIndexChanged = new CustomActionEvent<int>();
		OnSignIndexChanged = new CustomActionEvent<int, int>();
		OnDangerousSectionSignIndexChanged = new CustomActionEvent<int>();
		OnObstructionDataChanged = new CustomActionEvent<ObstructionData, ObstructionData>();
	}

	public void AddWagon(Wagon wagon)
	{
		wagons.Add(wagon);
	}

	public void AddWagon(List<Wagon> wagon)
	{
		wagons.AddRange(wagon);
	}

	public float GetTrainPosition()
	{
		return wagons[0].Coordinate;
	}

	public void Update(double dt)
	{
		if (TrainLogic != null)
		{
			TrainModuleImport.WagonSetPull(0, TractionForce);
			for (int i = 0; i < wagons.Count; i++)
			{
				TrainModuleImport.WagonSetBraking(i, Te116ModuleImport.getBrakingPower(i));
			}
			Te116ModuleImport.setAbsTrainSpeedKmH(Speed);
			TrainLogic.Update((float)dt);
		}
	}
}
