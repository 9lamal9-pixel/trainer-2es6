using System.Windows.Forms;

namespace ATR;

internal static class ExportToFile
{
	public static void Export(DataGridView dataGridView, string fileName, string fileFormat, string caption)
	{
		switch (fileFormat)
		{
		case ".html":
			ExportToFileHTML.Export(dataGridView, fileName, caption);
			break;
		case ".csv":
			ExportToFileCSV.Export(dataGridView, fileName, caption);
			break;
		case ".xlsx":
			ExportToFileXLSX.Export(dataGridView, fileName, caption);
			break;
		case ".pdf":
			ExportToFilePDF.Export(dataGridView, fileName, caption);
			break;
		}
	}
}
