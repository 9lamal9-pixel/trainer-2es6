using System;

namespace IWP;

internal interface ICustomActionEvent
{
	void AddListener(object classExample, Action listener);

	void RemoveListener(object classExample, Action listener);

	void Clear();

	void Clear(object classExample);

	void Invoke();
}
internal interface ICustomActionEvent<T>
{
	void AddListener(object classExample, Action<T> listener);

	void RemoveListener(object classExample, Action<T> listener);

	void Clear();

	void Clear(object classExample);

	void Invoke(T arg);
}
internal interface ICustomActionEvent<T, U>
{
	void AddListener(object classExample, Action<T, U> listener);

	void RemoveListener(object classExample, Action<T, U> listener);

	void Clear();

	void Clear(object classExample);

	void Invoke(T arg1, U arg2);
}
internal interface ICustomActionEvent<T, U, V>
{
	void AddListener(object classExample, Action<T, U, V> listener);

	void RemoveListener(object classExample, Action<T, U, V> listener);

	void Clear();

	void Clear(object classExample);

	void Invoke(T arg1, U arg2, V arg3);
}
internal interface ICustomActionEvent<T, U, V, R>
{
	void AddListener(object classExample, Action<T, U, V, R> listener);

	void RemoveListener(object classExample, Action<T, U, V, R> listener);

	void Clear();

	void Clear(object classExample);

	void Invoke(T arg1, U arg2, V arg3, R arg4);
}
