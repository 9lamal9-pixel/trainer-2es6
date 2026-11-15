using System.Collections.Generic;

namespace IWP;

internal class Manager
{
	private List<IManager> _managers;

	private List<IUpdateable> _iUpdateableList;

	public bool ContinueUpdating { get; private set; } = true;

	public static Manager Instance { get; private set; }

	public Manager()
	{
		Reset();
	}

	public void Awake()
	{
		Instance = this;
		CreateManager<TimeManager>();
		CreateManager<UIManager>();
		CreateManager<ClientVideoManager>();
		CreateManager<BLOKManager>();
		CreateManager<PultManager>();
		CreateManager<LogManager>();
		CreateManager<VirtualControlViewerManager>();
		CreateManager<GameManager>();
	}

	public void Start()
	{
		for (int i = 0; i < _managers.Count; i++)
		{
			_managers[i].Init();
			if (_managers[i] is IUpdateable)
			{
				_iUpdateableList.Add(_managers[i] as IUpdateable);
			}
		}
	}

	public void Reset()
	{
		_managers = new List<IManager>();
		_iUpdateableList = new List<IUpdateable>();
	}

	public void Update()
	{
		for (int i = 0; i < _iUpdateableList.Count; i++)
		{
			_iUpdateableList[i].Update();
		}
	}

	public void CreateManager<T>() where T : BaseManager<T>, new()
	{
		T val = BaseManager<T>.Factory();
		val.Create();
		_managers.Add(val);
	}

	public void Destroy()
	{
		ContinueUpdating = false;
		for (int i = 0; i < _managers.Count; i++)
		{
			_managers[i].Destroy();
		}
	}
}
