using System;
using System.Collections.Generic;

namespace IWP;

internal class BaseRailRoadController : IUpdateable
{
	private int nextTrainId;

	protected List<Train> trains;

	protected RailRoadData railRoadData;

	protected CustomActionEvent<int> OnAddTrain;

	protected CustomActionEvent<int> OnRemoveTrain;

	protected Dictionary<Type, BaseRailRoadController> controllers;

	public BaseRailRoadController()
	{
		trains = new List<Train>();
		controllers = new Dictionary<Type, BaseRailRoadController>();
		OnAddTrain = new CustomActionEvent<int>();
		OnRemoveTrain = new CustomActionEvent<int>();
	}

	protected virtual void Init()
	{
	}

	protected int CreateTrain()
	{
		Train train = new Train(nextTrainId);
		trains.Add(train);
		nextTrainId++;
		TrainModuleImport.CreateTrain(train.Id);
		OnAddTrain.Invoke(train.Id);
		return train.Id;
	}

	protected void RemoveTrain(int trainId)
	{
		trains.RemoveAt(trainId);
		OnRemoveTrain.Invoke(trainId);
	}

	public int GetTrainsCount()
	{
		return trains.Count;
	}

	public Train GetTrain(int id)
	{
		return trains[id];
	}

	public T CreateController<T>() where T : BaseRailRoadController, new()
	{
		Type typeFromHandle = typeof(T);
		T val;
		if (controllers.ContainsKey(typeFromHandle))
		{
			val = (T)controllers[typeFromHandle];
		}
		else if (CheckControllerType(typeFromHandle))
		{
			val = new T();
			val.PrepareDataForCreation(controllers[typeFromHandle.BaseType]);
			val.Init();
			controllers.Add(typeFromHandle, val);
		}
		else
		{
			val = null;
		}
		return val;
	}

	protected virtual void PrepareDataForCreation<T>(T baseController) where T : BaseRailRoadController, new()
	{
		railRoadData = baseController.railRoadData;
		trains = baseController.trains;
		controllers = baseController.controllers;
		OnAddTrain = baseController.OnAddTrain;
		OnRemoveTrain = baseController.OnRemoveTrain;
	}

	private bool CheckControllerType(Type controllerType)
	{
		if (controllerType != typeof(BaseRailRoadController) && !controllers.ContainsKey(controllerType.BaseType))
		{
			Console.WriteLine("You need to create base controller first - " + controllerType.BaseType.Name);
			return false;
		}
		return true;
	}

	public T GetController<T>() where T : BaseRailRoadController
	{
		Type typeFromHandle;
		if (controllers.ContainsKey(typeFromHandle = typeof(T)))
		{
			return (T)controllers[typeFromHandle];
		}
		return null;
	}

	public static BaseRailRoadController LoadFromResources(string path)
	{
		RailRoadData railRoadData = RailRoadDataUtilities.LoadFromJsonConfig(path);
		BaseRailRoadController baseRailRoadController = new BaseRailRoadController();
		baseRailRoadController.railRoadData = railRoadData;
		baseRailRoadController.controllers.Add(baseRailRoadController.GetType(), baseRailRoadController);
		return baseRailRoadController;
	}

	public RailRoadData GetRailRoadData()
	{
		return railRoadData;
	}

	public virtual void Update()
	{
	}
}
