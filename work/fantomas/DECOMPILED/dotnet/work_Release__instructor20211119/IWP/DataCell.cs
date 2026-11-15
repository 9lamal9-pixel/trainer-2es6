namespace IWP;

public class DataCell<T>
{
	public CustomActionEvent<T> OnDataChange;

	protected T _data;

	public string Label { get; }

	public virtual T Data
	{
		get
		{
			return GetData();
		}
		set
		{
			SetData(value);
		}
	}

	public void SetData(T data)
	{
		_data = data;
		OnDataChange.Invoke(_data);
	}

	public T GetData()
	{
		return _data;
	}

	public DataCell(string label, T data)
	{
		OnDataChange = new CustomActionEvent<T>();
		Label = label;
		SetData(data);
	}

	~DataCell()
	{
		OnDataChange.Clear();
	}

	public void Invoke()
	{
		OnDataChange.Invoke(_data);
	}
}
