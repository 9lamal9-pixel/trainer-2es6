using System;
using System.Text;
using System.Web;
using System.Windows.Forms;
using PdfSharp;
using TheArtOfDev.HtmlRenderer.PdfSharp;

namespace ATR;

internal class ExportToFilePDF
{
	public static void Export(DataGridView dataGridView, string filename, string caption)
	{
		if (filename == null)
		{
			throw new ArgumentNullException("Не задан файл !");
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.AppendLine("<!DOCTYPE html>");
		stringBuilder.AppendLine("<html>");
		stringBuilder.AppendLine("<body>");
		stringBuilder.AppendLine("<div>");
		stringBuilder.AppendLine("<span style='text-align: center; display: block; font-size: 19pt; margin-bottom: 10px;'>" + caption + "</span>");
		stringBuilder.AppendLine("<table border='1' cellspacing='1' cellpadding='5px' style='text-align: center; margin-right: auto; margin-left: auto;' > ");
		WriteHeader(stringBuilder, dataGridView);
		for (int i = 0; i < dataGridView.RowCount; i++)
		{
			if (!dataGridView.Rows[i].IsNewRow)
			{
				WriteRow(stringBuilder, i, dataGridView);
			}
		}
		stringBuilder.AppendLine("</table>");
		stringBuilder.AppendLine("</div>");
		stringBuilder.AppendLine("</body>");
		stringBuilder.AppendLine("</html>");
		PdfGenerator.GeneratePdf(stringBuilder.ToString(), PageSize.A4).Save(filename);
	}

	private static void WriteHeader(StringBuilder sb, DataGridView dataGridView)
	{
		sb.AppendLine("<tr>");
		for (int i = 0; i < dataGridView.Columns.Count; i++)
		{
			string headerText = dataGridView.Columns[i].HeaderText;
			sb.AppendLine("<td>" + HttpUtility.HtmlEncode(headerText) + "</td>");
		}
		sb.AppendLine("</tr>");
	}

	private static void WriteRow(StringBuilder sb, int rowIndex, DataGridView dataGridView)
	{
		sb.AppendLine("<tr>");
		for (int i = 0; i < dataGridView.ColumnCount; i++)
		{
			string s = (dataGridView[i, rowIndex].Value ?? "").ToString();
			sb.AppendLine("<td>" + HttpUtility.HtmlEncode(s) + "</td>");
		}
		sb.AppendLine("</tr>");
	}
}
