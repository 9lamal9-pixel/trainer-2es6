using System;
using System.IO;
using System.Web;
using System.Windows.Forms;

namespace ATR;

internal static class ExportToFileHTML
{
	public static void Export(DataGridView dataGridView, string filename, string caption)
	{
		if (filename == null)
		{
			throw new ArgumentNullException("Не задан файл !");
		}
		using StreamWriter streamWriter = new StreamWriter(filename);
		WriteDocumentType(streamWriter);
		streamWriter.WriteLine("<html>");
		streamWriter.WriteLine("<body>");
		streamWriter.WriteLine("<div>");
		streamWriter.WriteLine("<span style='text-align: center; display: block; font-size: 19pt; margin-bottom: 10px;'>" + caption + "</span>");
		WriteStartTable(streamWriter);
		WriteHeader(streamWriter, dataGridView);
		for (int i = 0; i < dataGridView.RowCount; i++)
		{
			if (!dataGridView.Rows[i].IsNewRow)
			{
				WriteRow(streamWriter, i, dataGridView);
			}
		}
		WriteEndTable(streamWriter);
		streamWriter.WriteLine("</div>");
		streamWriter.WriteLine("</body>");
		streamWriter.WriteLine("</html>");
	}

	private static void WriteDocumentType(StreamWriter writer)
	{
		if (writer == null)
		{
			throw new ArgumentNullException("Ошибка: Не удается записать файл. writer=null");
		}
		writer.WriteLine("<!DOCTYPE html>");
	}

	private static void WriteStartTable(StreamWriter writer)
	{
		if (writer == null)
		{
			throw new ArgumentNullException("Ошибка: Не удается записать файл. writer=null");
		}
		writer.WriteLine("<table border='1' cellspacing='1' cellpadding='5px' style='text-align: center; margin-right: auto; margin-left: auto;' > ");
	}

	private static void WriteEndTable(StreamWriter writer)
	{
		if (writer == null)
		{
			throw new ArgumentNullException("Ошибка: Не удается записать файл. writer=null");
		}
		writer.WriteLine("</table>");
	}

	private static void WriteHeader(StreamWriter writer, DataGridView dataGridView)
	{
		if (writer == null)
		{
			throw new ArgumentNullException("Ошибка: Не удается записать файл. writer=null");
		}
		writer.WriteLine("<tr>");
		for (int i = 0; i < dataGridView.Columns.Count; i++)
		{
			string headerText = dataGridView.Columns[i].HeaderText;
			writer.WriteLine("<th>" + HttpUtility.HtmlEncode(headerText) + "</th>");
		}
		writer.WriteLine("</tr>");
	}

	private static void WriteRow(StreamWriter writer, int rowIndex, DataGridView dataGridView)
	{
		if (writer == null)
		{
			throw new ArgumentNullException("Ошибка: Не удается записать файл. writer=null");
		}
		writer.WriteLine("<tr>");
		for (int i = 0; i < dataGridView.ColumnCount; i++)
		{
			string s = (dataGridView[i, rowIndex].Value ?? "").ToString();
			writer.WriteLine("<td>" + HttpUtility.HtmlEncode(s) + "</td>");
		}
		writer.WriteLine("</tr>");
	}
}
