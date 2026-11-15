using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Text;

namespace IWP;

internal class CSVWriter
{
	private string path;

	private StreamWriter writer;

	public CSVWriter(string path)
	{
		this.path = path;
	}

	public void StartWriting(bool append)
	{
		writer = new StreamWriter(path, append);
	}

	public void StopWriting()
	{
		writer.Close();
	}

	public void WriteLine(params string[] data)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.Append(data[0]);
		for (int i = 1; i < data.Length; i++)
		{
			stringBuilder.Append(';');
			stringBuilder.Append(data[i]);
		}
		writer.WriteLine(stringBuilder.ToString());
	}

	public void WriteLine(List<float> data)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.Append(data[0].ToString(CultureInfo.InvariantCulture));
		for (int i = 1; i < data.Count; i++)
		{
			stringBuilder.Append(';');
			stringBuilder.Append(data[i].ToString(CultureInfo.InvariantCulture));
		}
		writer.WriteLine(stringBuilder.ToString());
	}

	public void WriteLine(List<string> data)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.Append(data[0]);
		for (int i = 1; i < data.Count; i++)
		{
			stringBuilder.Append(';');
			stringBuilder.Append(data[i]);
		}
		writer.WriteLine(stringBuilder.ToString());
	}
}
