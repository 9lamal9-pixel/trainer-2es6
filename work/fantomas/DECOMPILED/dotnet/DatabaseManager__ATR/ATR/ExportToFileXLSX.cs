using System;
using System.Windows.Forms;
using ClosedXML.Excel;

namespace ATR;

internal static class ExportToFileXLSX
{
	public static void Export(DataGridView dataGridView, string filename, string caption)
	{
		if (filename == null)
		{
			throw new ArgumentNullException("Не задан файл !");
		}
		XLWorkbook xLWorkbook = new XLWorkbook();
		IXLWorksheet iXLWorksheet = xLWorkbook.Worksheets.Add("Лист1");
		iXLWorksheet.Cell(1, 1).Value = caption;
		IXLRange iXLRange = iXLWorksheet.Range(1, 1, 1, dataGridView.Columns.Count).Row(1).Merge();
		iXLRange.Style.Alignment.Horizontal = XLAlignmentHorizontalValues.Center;
		iXLRange.Style.Font.Bold = true;
		iXLRange.Style.Font.FontSize = 14.0;
		WriteHeader(dataGridView, iXLWorksheet);
		for (int i = 0; i < dataGridView.RowCount; i++)
		{
			if (!dataGridView.Rows[i].IsNewRow)
			{
				WriteRow(i, dataGridView, iXLWorksheet);
			}
		}
		for (int j = 1; j <= dataGridView.Columns.Count; j++)
		{
			iXLWorksheet.Column(j).AdjustToContents();
		}
		WriteBorder(dataGridView, iXLWorksheet);
		xLWorkbook.SaveAs(filename);
	}

	private static void WriteHeader(DataGridView dataGridView, IXLWorksheet ws)
	{
		for (int i = 0; i < dataGridView.Columns.Count; i++)
		{
			string headerText = dataGridView.Columns[i].HeaderText;
			ws.Cell(2, i + 1).Value = headerText;
			ws.Cell(2, i + 1).Style.Font.Bold = true;
		}
	}

	private static void WriteRow(int rowIndex, DataGridView dataGridView, IXLWorksheet ws)
	{
		for (int i = 0; i < dataGridView.ColumnCount; i++)
		{
			string value = (dataGridView[i, rowIndex].Value ?? "").ToString();
			ws.Cell(rowIndex + 1 + 2, i + 1).SetValue(value);
		}
	}

	private static void WriteBorder(DataGridView dataGridView, IXLWorksheet ws)
	{
		IXLRange iXLRange = ws.Range(1, 1, dataGridView.RowCount + 2, dataGridView.Columns.Count);
		iXLRange.Style.Border.OutsideBorder = XLBorderStyleValues.Thin;
		iXLRange.Style.Border.TopBorder = XLBorderStyleValues.Thin;
		iXLRange.Style.Border.InsideBorder = XLBorderStyleValues.Thin;
		iXLRange.Style.Border.OutsideBorder = XLBorderStyleValues.Thin;
		iXLRange.Style.Border.LeftBorder = XLBorderStyleValues.Thin;
		iXLRange.Style.Border.RightBorder = XLBorderStyleValues.Thin;
		iXLRange.Style.Border.TopBorder = XLBorderStyleValues.Thin;
	}
}
