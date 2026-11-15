using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Text.RegularExpressions;

namespace ELS;

internal class ImportData
{
	public class CSVData
	{
		public int ID { get; }

		public string FIO { get; set; }

		public string Group { get; set; }

		public string DateOfBirth { get; set; }

		public bool isError { get; set; }

		public CSVData(string _rawData, string sep, int _id)
		{
			ID = _id;
			Regex regex = ((!(sep == ",")) ? new Regex(";(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))") : new Regex(",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))"));
			try
			{
				string[] array = regex.Split(_rawData);
				if (array.Length == 3)
				{
					FIO = array[0];
					Group = array[1];
					DateOfBirth = array[2];
					isError = false;
				}
				else
				{
					FIO = _rawData;
					isError = true;
				}
			}
			catch (Exception ex)
			{
				FIO = _rawData;
				Group = ex.Message.ToString();
				isError = true;
			}
		}
	}

	public List<CSVData> Data;

	public List<string> RawData;

	public ImportData(string filePath, string separator, Encoding encoding, bool IgnoreFileHeader)
	{
		if (File.Exists(filePath))
		{
			string[] collection = File.ReadAllLines(filePath, encoding);
			RawData = new List<string>();
			RawData.AddRange(collection);
			Data = new List<CSVData>();
			for (int i = (IgnoreFileHeader ? 1 : 0); i < RawData.Count; i++)
			{
				Data.Add(new CSVData(RawData[i], separator, i));
			}
			return;
		}
		throw new Exception("Не найден файл: " + filePath);
	}
}
