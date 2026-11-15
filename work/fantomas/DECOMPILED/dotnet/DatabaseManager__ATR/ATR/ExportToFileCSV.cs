using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace ATR;

internal static class ExportToFileCSV
{
	public static void Export(DataGridView dataGridView, string filename, string caption)
	{
		if (filename == null)
		{
			throw new ArgumentNullException("Не задан файл !");
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.AppendLine(GetHeader(dataGridView));
		for (int i = 0; i < dataGridView.RowCount; i++)
		{
			if (!dataGridView.Rows[i].IsNewRow)
			{
				stringBuilder.AppendLine(GetRow(i, dataGridView));
			}
		}
		File.WriteAllText(filename, stringBuilder.ToString(), Encoding.UTF8);
	}

	private static string GetHeader(DataGridView dataGridView)
	{
		List<string> list = new List<string>();
		for (int i = 0; i < dataGridView.Columns.Count; i++)
		{
			string headerText = dataGridView.Columns[i].HeaderText;
			list.Add(headerText);
		}
		return string.Join(";", list);
	}

	private static string GetRow(int rowIndex, DataGridView dataGridView)
	{
		List<string> list = new List<string>();
		for (int i = 0; i < dataGridView.ColumnCount; i++)
		{
			string text = (dataGridView[i, rowIndex].Value ?? "").ToString();
			list.Add(text.Contains(';') ? $"\"{text}\"" : text);
		}
		return string.Join(";", list);
	}
}
