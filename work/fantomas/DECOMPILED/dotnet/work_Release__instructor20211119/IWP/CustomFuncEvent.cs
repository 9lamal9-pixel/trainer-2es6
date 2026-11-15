using System;
using System.Collections.Generic;

namespace IWP;

internal class CustomFuncEvent<T> : ICustomFuncEvent<T>
{
	private Dictionary<object, List<Func<T, T>>> listeners = new Dictionary<object, List<Func<T, T>>>();

	private event Func<T, T> customEvent;

	public void AddListener(object classExample, Func<T, T> listener)
	{
		if (!listeners.ContainsKey(classExample))
		{
			listeners.Add(classExample, new List<Func<T, T>>());
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

	public List<T> Invoke(T arg)
	{
		if (customEvent != null)
		{
			Delegate[] invocationList = customEvent.GetInvocationList();
			List<T> list = new List<T>();
			for (int i = 0; i < invocationList.Length; i++)
			{
				T item = ((Func<T, T>)invocationList[i])(arg);
				list.Add(item);
			}
			return list;
		}
		return new List<T> { arg };
	}

	public void RemoveListener(object classExample, Func<T, T> listener)
	{
		if (listeners.ContainsKey(classExample) && listeners[classExample].Remove(listener))
		{
			customEvent -= listener;
		}
	}
}
