namespace IWP;

public class Wagon
{
	private WagonData _wagonData;

	private bool _boksovanie;

	private bool _uz;

	public int Id { get; }

	public float Coordinate => TrainModuleImport.WagonGetPosition(Id);

	public WagonType WagonType => _wagonData.wagonType;

	public float WeightBrutto => _wagonData.weightBrutto;

	public float Length => _wagonData.length;

	public int profileIndex { get; set; } = 0;

	public int radiusIndex { get; set; } = 0;

	public float profile { get; set; } = 0f;

	public int radius { get; set; } = 10000;

	public bool Boksovanie
	{
		get
		{
			return _boksovanie;
		}
		set
		{
			_boksovanie = value;
		}
	}

	public bool Uz
	{
		get
		{
			return _uz;
		}
		set
		{
			_uz = value;
		}
	}

	public Wagon(int id, WagonData wagonData)
	{
		Id = id;
		_wagonData = wagonData;
	}

	public bool Move(float speed, float deltaTime)
	{
		return true;
	}
}
