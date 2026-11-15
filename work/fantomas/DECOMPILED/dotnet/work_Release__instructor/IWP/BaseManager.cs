namespace IWP;

internal class BaseManager<T> : IManager where T : new()
{
	public static T instance;

	public static T Factory()
	{
		instance = new T();
		return instance;
	}

	public virtual void Create()
	{
	}

	public virtual void Init()
	{
	}

	public virtual void PostInit()
	{
	}

	public virtual void Destroy()
	{
	}
}
