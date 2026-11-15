using System.Collections.Generic;
using System.Text;

namespace NetLib;

public class DataPack
{
	private const char SYMBOL_DEL = ';';

	private const char SYMBOL_SQ = '=';

	private Dictionary<string, string> Data = new Dictionary<string, string>();

	public string this[string name]
	{
		get
		{
			if (Data.ContainsKey(name))
			{
				return Data[name];
			}
			return null;
		}
		set
		{
			if (Data.ContainsKey(name))
			{
				Data[name] = value;
			}
			else
			{
				Data.Add(name, value);
			}
		}
	}

	public bool Empty => Data.Count == 0;

	public void Load(string text)
	{
		Data.Clear();
		string[] array = text.Split(';');
		for (int i = 0; i < array.Length; i++)
		{
			string[] array2 = array[i].Split(new char[1] { '=' }, 2);
			if (array2.Length >= 2)
			{
				Data.Add(array2[0], array2[1]);
			}
		}
	}

	public byte[] Save()
	{
		StringBuilder stringBuilder = new StringBuilder();
		bool flag = true;
		foreach (string key in Data.Keys)
		{
			if (!flag)
			{
				stringBuilder.Append(';');
			}
			else
			{
				flag = false;
			}
			stringBuilder.AppendFormat("{0}" + '=' + "{1}", key, Data[key]);
		}
		stringBuilder.Append('\r');
		string s = stringBuilder.ToString();
		return Encoding.Unicode.GetBytes(s);
	}

	public string[] getVals()
	{
		List<string> list = new List<string>();
		foreach (string key in Data.Keys)
		{
			list.Add(key);
		}
		return list.ToArray();
	}

	public void Clear()
	{
		Data.Clear();
	}

	public void Remove(string name)
	{
		Data.Remove(name);
	}
}
