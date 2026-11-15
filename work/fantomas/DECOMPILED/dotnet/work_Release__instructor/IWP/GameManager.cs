using System;
using System.Collections.Generic;

namespace IWP;

internal class GameManager : BaseManager<GameManager>, IUpdateable
{
	public Train playerTrain;

	private BaseRailRoadController railRoadController;

	private List<IController> _controllers;

	private List<IUpdateable> _updatableControllers;

	public float oldTrainPosition;

	public override void Create()
	{
		base.Create();
		_controllers = new List<IController>();
		_updatableControllers = new List<IUpdateable>();
	}

	public override void Init()
	{
		railRoadController = BaseRailRoadController.LoadFromResources("RTS/scenaries/" + RoadConfig.instance.MarkupFileName);
		CreateRailRoadController<TrainMovementController>();
		playerTrain = GetRailRoadController<TrainMovementController>().SpawnTrain(railRoadController.GetRailRoadData().startCoord);
		playerTrain.TrainLogic.Init(2, playerTrain.wagons.Count - 2);
		if (GameConfig.Instance.IsActiveVideo)
		{
			BaseManager<ClientVideoManager>.instance.PostInit();
		}
		if (GameConfig.Instance.IsActiveBlok)
		{
			BaseManager<BLOKManager>.instance.PostInit();
		}
		if (GameConfig.Instance.IsActivePult)
		{
			BaseManager<PultManager>.instance.PostInit();
		}
		if (GameConfig.Instance.IsActiveVirtualControlViewer)
		{
			BaseManager<VirtualControlViewerManager>.instance.PostInit();
		}
		BaseManager<LogManager>.instance.PostInit();
		BaseManager<LogManager>.instance.Enable = true;
		BaseManager<LogManager>.instance.CreateDataFile();
		BaseManager<UIManager>.instance.railRoadData = railRoadController.GetRailRoadData();
		BaseManager<UIManager>.instance.PostInit();
		UIManager uIManager = BaseManager<UIManager>.instance;
		uIManager.OnCloseButtonClick = (Action)Delegate.Combine(uIManager.OnCloseButtonClick, (Action)(() =>
		{
			BaseManager<LogManager>.instance.DataFileFinish();
		}));
		CreateController<BLOKDataReceiveController>();
		CreateController<VideoDataReceiveController>();
		CreateController<ALSNController>();
		MainDataStorage.Instance.InvokeAll();
		UIManager uIManager2 = BaseManager<UIManager>.instance;
		uIManager2.OnBrakeSystemFaultChanged = (Action<int, bool>)Delegate.Combine(uIManager2.OnBrakeSystemFaultChanged, (Action<int, bool>)((int faultId, bool state) =>
		{
			if (state)
			{
				Te116ModuleImport.setBrakeFault(faultId + 1);
				if (faultId + 1 >= 0 && Te116Logic.tBrakeFaults.Length > faultId + 1)
				{
					BaseManager<LogManager>.instance.MachinistAnswerMessage(Te116Logic.tBrakeFaults[faultId + 1], "-", "-", "-");
				}
			}
			else
			{
				Te116ModuleImport.setBrakeFault(0);
			}
		}));
		UIManager uIManager3 = BaseManager<UIManager>.instance;
		uIManager3.OnElectricalCircuitMalfunctionChanged = (Action<int, bool>)Delegate.Combine(uIManager3.OnElectricalCircuitMalfunctionChanged, (Action<int, bool>)((int faultId, bool state) =>
		{
			if (state)
			{
				Te116ModuleImport.setElectricFault(faultId + 1);
				if (faultId + 1 >= 0 && Te116Logic.eCircuitFaults.Length > faultId + 1)
				{
					BaseManager<LogManager>.instance.MachinistAnswerMessage(Te116Logic.eCircuitFaults[faultId + 1], "-", "-", "-");
				}
			}
			else
			{
				Te116ModuleImport.setElectricFault(0);
			}
		}));
		UIManager uIManager4 = BaseManager<UIManager>.instance;
		uIManager4.OnCloseButtonClick = (Action)Delegate.Combine(uIManager4.OnCloseButtonClick, new Action(CloseApplication));
	}

	public void Update()
	{
		for (int i = 0; i < _updatableControllers.Count; i++)
		{
			_updatableControllers[i].Update();
		}
		BaseManager<UIManager>.instance.SetTrainPosition(playerTrain.wagons[0].Coordinate);
		int num = 800;
		if ((int)playerTrain.wagons[0].Coordinate / num > (int)oldTrainPosition / num)
		{
			BaseManager<ClientVideoManager>.instance.SendPlaySoundByQueue(60, state: true);
		}
		if ((int)(playerTrain.wagons[0].Coordinate + 3f) / num > ((int)oldTrainPosition + 3) / num)
		{
			BaseManager<ClientVideoManager>.instance.SendPlaySoundByQueue(60, state: true);
		}
		oldTrainPosition = playerTrain.wagons[0].Coordinate;
	}

	private void CreateRailRoadController<T>() where T : BaseRailRoadController, new()
	{
		T val = railRoadController.CreateController<T>();
		if (val != null)
		{
			_updatableControllers.Add(val);
		}
	}

	public T GetRailRoadController<T>() where T : BaseRailRoadController
	{
		return railRoadController.GetController<T>();
	}

	private void CreateController<T>() where T : BaseController<T>, new()
	{
		T val = new T();
		_controllers.Add(val);
		if (val is IUpdateable)
		{
			_updatableControllers.Add((IUpdateable)val);
		}
		val.Init();
	}

	public T GetController<T>() where T : BaseController<T>, new()
	{
		for (int i = 0; i < _controllers.Count; i++)
		{
			if (_controllers[i] is T)
			{
				return (T)_controllers[i];
			}
		}
		return null;
	}

	public void CloseApplication()
	{
		Manager.Instance.Destroy();
	}
}
