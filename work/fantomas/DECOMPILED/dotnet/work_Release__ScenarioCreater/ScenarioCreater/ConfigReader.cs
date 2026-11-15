using System.IO;
using Newtonsoft.Json;

namespace ScenarioCreater;

internal static class ConfigReader
{
	public static Config Read(string Path)
	{
		StreamReader streamReader = new StreamReader(Path);
		string value = streamReader.ReadToEnd();
		streamReader.Close();
		return JsonConvert.DeserializeObject<Config>(value);
	}
}
