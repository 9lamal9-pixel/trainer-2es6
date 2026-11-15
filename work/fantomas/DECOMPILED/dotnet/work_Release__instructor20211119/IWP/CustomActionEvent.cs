using System;
using System.Collections.Generic;

namespace IWP;

internal class CustomActionEvent : ICustomActionEvent
{
	private Dictionary<object, List<Action>> listeners = new Dictionary<object, List<Action>>();

	private event Action customEvent;

	public void AddListener(object classExample, Action listener)
	{
		if (!listeners.ContainsKey(classExample))
		{
			listeners.Add(classExample, new List<Action>());
		}
		listeners[classExample].Add(listener);
		customEvent += listener;
	}

	public void Clear()
	{
		List<object> list = new List<object>(listeners.Keys);
		for (int i = 0; i < list.Count; i++)
		{
			for (int j = 0; j < listeners[list[i]].Count; j++)
			{
				customEvent -= listeners[list[i]][j];
			}
		}
		listeners.Clear();
	}

	public void Clear(object classExample)
	{
		if (listeners.ContainsKey(classExample))
		{
			for (int i = 0; i < listeners[classExample].Count; i++)
			{
				customEvent -= listeners[classExample][i];
			}
		}
		listeners.Remove(classExample);
	}

	public void Invoke()
	{
		if (customEvent != null)
		{
			customEvent();
		}
	}

	public void RemoveListener(object classExample, Action listener)
	{
		if (listeners.ContainsKey(classExample) && listeners[classExample].Remove(listener))
		{
			customEvent -= listener;
		}
	}
}
public class CustomActionEvent<T> : ICustomActionEvent<T>
{
	private Dictionary<object, List<Action<T>>> listeners = new Dictionary<object, List<Action<T>>>();

	private event Action<T> customEvent;

	public void AddListener(object classExample, Action<T> listener)
	{
		if (!listeners.ContainsKey(classExample))
		{
			listeners.Add(classExample, new List<Action<T>>());
		}
		listeners[classExample].Add(listener);
		customEvent += listener;
	}

	public void Clear()
	{
		List<object> list = new List<object>(listeners.Keys);
		for (int i = 0; i < list.Count; i++)
		{
			for (int j = 0; j < listeners[list[i]].Count; j++)
			{
				customEvent -= listeners[list[i]][j];
			}
		}
		listeners.Clear();
	}

	public void Clear(object classExample)
	{
		if (listeners.ContainsKey(classExample))
		{
			for (int i = 0; i < listeners[classExample].Count; i++)
			{
				customEvent -= listeners[classExample][i];
			}
		}
		listeners.Remove(classExample);
	}

	public void Invoke(T arg)
	{
		if (customEvent != null)
		{
			customEvent(arg);
		}
	}

	public void RemoveListener(object classExample, Action<T> listener)
	{
		if (listeners.ContainsKey(classExample) && listeners[classExample].Remove(listener))
		{
			customEvent -= listener;
		}
	}
}
internal class CustomActionEvent<T, TU> : ICustomActionEvent<T, TU>
{
	private Dictionary<object, List<Action<T, TU>>> listeners = new Dictionary<object, List<Action<T, TU>>>();

	private event Action<T, TU> customEvent;

	public void AddListener(object classExample, Action<T, TU> listener)
	{
		if (!listeners.ContainsKey(classExample))
		{
			listeners.Add(classExample, new List<Action<T, TU>>());
		}
		listeners[classExample].Add(listener);
		customEvent += listener;
	}

	public void Clear()
	{
		List<object> list = new List<object>(listeners.Keys);
		for (int i = 0; i < list.Count; i++)
		{
			for (int j = 0; j < listeners[list[i]].Count; j++)
			{
				customEvent -= listeners[list[i]][j];
			}
		}
		listeners.Clear();
	}

	public void Clear(object classExample)
	{
		if (listeners.ContainsKey(classExample))
		{
			for (int i = 0; i < listeners[classExample].Count; i++)
			{
				customEvent -= listeners[classExample][i];
			}
		}
		listeners.Remove(classExample);
	}

	public void Invoke(T arg1, TU arg2)
	{
		if (customEvent != null)
		{
			customEvent(arg1, arg2);
		}
	}

	public void RemoveListener(object classExample, Action<T, TU> listener)
	{
		if (listeners.ContainsKey(classExample) && listeners[classExample].Remove(listener))
		{
			customEvent -= listener;
		}
	}
}
internal class CustomActionEvent<T, U, V> : ICustomActionEvent<T, U, V>
{
	private Dictionary<object, List<Action<T, U, V>>> listeners = new Dictionary<object, List<Action<T, U, V>>>();

	private event Action<T, U, V> customEvent;

	public void AddListener(object classExample, Action<T, U, V> listener)
	{
		if (!listeners.ContainsKey(classExample))
		{
			listeners.Add(classExample, new List<Action<T, U, V>>());
		}
		listeners[classExample].Add(listener);
		customEvent += listener;
	}

	public void Clear()
	{
		List<object> list = new List<object>(listeners.Keys);
		for (int i = 0; i < list.Count; i++)
		{
			for (int j = 0; j < listeners[list[i]].Count; j++)
			{
				customEvent -= listeners[list[i]][j];
			}
		}
		listeners.Clear();
	}

	public void Clear(object classExample)
	{
		if (listeners.ContainsKey(classExample))
		{
			for (int i = 0; i < listeners[classExample].Count; i++)
			{
				customEvent -= listeners[classExample][i];
			}
		}
		listeners.Remove(classExample);
	}

	public void Invoke(T arg1, U arg2, V arg3)
	{
		if (customEvent != null)
		{
			customEvent(arg1, arg2, arg3);
		}
	}

	public void RemoveListener(object classExample, Action<T, U, V> listener)
	{
		if (listeners.ContainsKey(classExample) && listeners[classExample].Remove(listener))
		{
			customEvent -= listener;
		}
	}
}
internal class CustomActionEvent<T, U, V, R> : ICustomActionEvent<T, U, V, R>
{
	private Dictionary<object, List<Action<T, U, V, R>>> listeners = new Dictionary<object, List<Action<T, U, V, R>>>();

	private event Action<T, U, V, R> customEvent;

	public void AddListener(object classExample, Action<T, U, V, R> listener)
	{
		if (!listeners.ContainsKey(classExample))
		{
			listeners.Add(classExample, new List<Action<T, U, V, R>>());
		}
		listeners[classExample].Add(listener);
		customEvent += listener;
	}

	public void Clear()
	{
		List<object> list = new List<object>(listeners.Keys);
		for (int i = 0; i < list.Count; i++)
		{
			for (int j = 0; j < listeners[list[i]].Count; j++)
			{
				customEvent -= listeners[list[i]][j];
			}
		}
		listeners.Clear();
	}

	public void Clear(object classExample)
	{
		if (listeners.ContainsKey(classExample))
		{
			for (int i = 0; i < listeners[classExample].Count; i++)
			{
				customEvent -= listeners[classExample][i];
			}
		}
		listeners.Remove(classExample);
	}

	public void Invoke(T arg1, U arg2, V arg3, R arg4)
	{
		if (customEvent != null)
		{
			customEvent(arg1, arg2, arg3, arg4);
		}
	}

	public void RemoveListener(object classExample, Action<T, U, V, R> listener)
	{
		if (listeners.ContainsKey(classExample) && listeners[classExample].Remove(listener))
		{
			customEvent -= listener;
		}
	}
}
