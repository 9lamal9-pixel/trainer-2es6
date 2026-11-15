using System;
using System.Collections.Generic;

namespace IWP;

internal interface ICustomFuncEvent<T>
{
	void AddListener(object classExample, Func<T, T> listener);

	void RemoveListener(object classExample, Func<T, T> listener);

	void Clear();

	void Clear(object classExample);

	List<T> Invoke(T arg);
}
