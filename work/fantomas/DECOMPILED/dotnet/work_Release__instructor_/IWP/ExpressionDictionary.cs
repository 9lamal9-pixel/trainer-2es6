using System;
using System.Collections.Generic;

namespace IWP;

internal static class ExpressionDictionary
{
	public static bool GetBool(this Dictionary<string, object> data, string key)
	{
		if (!data.ContainsKey(key))
		{
			Console.WriteLine("Warning: Field " + key + " is out");
			return false;
		}
		return data[key].ToString() == "1";
	}

	public static int GetInt(this Dictionary<string, object> data, string key)
	{
		if (!data.ContainsKey(key))
		{
			return 0;
		}
		int.TryParse(data[key].ToString(), out var result);
		return result;
	}

	public static void SetInt(this Dictionary<string, object> data, string key, int value)
	{
		if (data.ContainsKey(key))
		{
			data[key] = value.ToString();
		}
	}

	public static float GetFloat(this Dictionary<string, object> data, string key)
	{
		if (!data.ContainsKey(key))
		{
			return 0f;
		}
		float.TryParse(data[key].ToString(), out var result);
		return result;
	}

	public static long GetLong(this Dictionary<string, object> data, string key)
	{
		if (!data.ContainsKey(key))
		{
			return 0L;
		}
		long.TryParse(data[key].ToString(), out var result);
		return result;
	}

	public static string GetString(this Dictionary<string, object> data, string key)
	{
		if (!data.ContainsKey(key))
		{
			return null;
		}
		return data[key].ToString();
	}

	public static bool Contain(this Dictionary<string, object> data, string key)
	{
		return data.ContainsKey(key);
	}

	public static string GetItemKeyByIdx(this Dictionary<string, object> data, int index)
	{
		int num = 0;
		foreach (KeyValuePair<string, object> datum in data)
		{
			if (num == index)
			{
				return datum.Key;
			}
			num++;
		}
		Console.WriteLine("Error: too much index  :" + index);
		return "";
	}
}
