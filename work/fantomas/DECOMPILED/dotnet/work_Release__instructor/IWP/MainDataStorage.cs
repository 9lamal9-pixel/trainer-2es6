using System;
using System.Collections.Generic;

namespace IWP;

internal class MainDataStorage
{
	public Dictionary<ControlPanelParameters, DataCell<float>> DataStorage = new Dictionary<ControlPanelParameters, DataCell<float>>();

	private static MainDataStorage _instance;

	public static MainDataStorage Instance
	{
		get
		{
			if (_instance == null)
			{
				_instance = new MainDataStorage();
			}
			return _instance;
		}
	}

	public MainDataStorage()
	{
		ControlPanelParameters[] array = (ControlPanelParameters[])Enum.GetValues(typeof(ControlPanelParameters));
		foreach (ControlPanelParameters name in array)
		{
			InitCell(name);
		}
	}

	private void InitCell(ControlPanelParameters name, float value = 0f)
	{
		DataStorage.Add(name, new DataCell<float>(name.ToString(), value));
	}

	public void OnGetDataFromNet(ControlPanelParameters name, float value)
	{
		DataStorage[name].Data = value;
	}

	public static void Init()
	{
		if (_instance == null)
		{
			_instance = new MainDataStorage();
		}
	}

	public void InvokeAll()
	{
		foreach (DataCell<float> value in DataStorage.Values)
		{
			value.OnDataChange.Invoke(value.Data);
		}
	}
}
