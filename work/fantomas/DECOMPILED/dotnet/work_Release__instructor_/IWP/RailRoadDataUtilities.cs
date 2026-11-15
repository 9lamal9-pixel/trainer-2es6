using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using Newtonsoft.Json;

namespace IWP;

internal class RailRoadDataUtilities
{
	public static RailRoadData LoadFromJsonConfig(string Path)
	{
		string value;
		using (StreamReader streamReader = new StreamReader(Path))
		{
			value = streamReader.ReadToEnd();
			streamReader.Close();
		}
		RailRoadData railRoadData = null;
		try
		{
			railRoadData = JsonConvert.DeserializeObject<RailRoadData>(value);
		}
		catch (Exception value2)
		{
			Console.WriteLine(value2);
		}
		int num = 0;
		for (int i = 1; i < railRoadData.profile.Count; i++)
		{
			num += railRoadData.profile[i - 1].distance;
			railRoadData.profile[i].coord = num;
		}
		foreach (KeyValuePair<string, List<ObstructionData>> obstruction in railRoadData.obstructions)
		{
			foreach (ObstructionData item in obstruction.Value)
			{
				item.type = obstruction.Key;
			}
		}
		return railRoadData;
	}

	public static bool SaveJsonConfig(string Path, RailRoadData railRoadData)
	{
		try
		{
			Console.WriteLine("start jsonConvert.SerializeObject(railRoadData)");
			string contents = JsonConvert.SerializeObject(railRoadData, Formatting.Indented);
			Console.WriteLine("end jsonConvert.SerializeObject(railRoadData)");
			Console.WriteLine("start File.WriteAllText");
			File.WriteAllText(Path, contents, Encoding.GetEncoding("UTF-8"));
			Console.WriteLine("end File.WriteAllText");
			return true;
		}
		catch (Exception value)
		{
			Console.WriteLine(value);
			return false;
		}
	}
}
