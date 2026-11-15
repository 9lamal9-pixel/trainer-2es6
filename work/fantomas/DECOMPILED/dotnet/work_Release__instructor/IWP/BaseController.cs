namespace IWP;

internal class BaseController<T> : IController where T : new()
{
	public static T Factory()
	{
		return new T();
	}

	public virtual void Init()
	{
	}
}
