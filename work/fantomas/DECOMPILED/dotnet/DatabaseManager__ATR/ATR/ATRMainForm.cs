using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Data.SQLite;
using System.Drawing;
using System.IO;
using System.Windows.Forms;

namespace ATR;

public class ATRMainForm : Form
{
	private class RowComparer : IComparer
	{
		private static int sortOrderModifier = 1;

		private static int colIndex = 0;

		public RowComparer(SortOrder sortOrder, int _colIndex)
		{
			switch (sortOrder)
			{
			case SortOrder.Descending:
				sortOrderModifier = -1;
				break;
			case SortOrder.Ascending:
				sortOrderModifier = 1;
				break;
			}
			colIndex = _colIndex;
		}

		public int Compare(object x, object y)
		{
			DataGridViewRow dataGridViewRow = (DataGridViewRow)x;
			DataGridViewRow dataGridViewRow2 = (DataGridViewRow)y;
			string text = dataGridViewRow.Cells[colIndex].Value.ToString();
			string text2 = dataGridViewRow2.Cells[colIndex].Value.ToString();
			int num = 0;
			if (!double.TryParse(text, out var result) || !double.TryParse(text2, out var result2))
			{
				num = text.ToString().CompareTo(text2.ToString());
			}
			else
			{
				num = ((result < result2) ? (-1) : ((result > result2) ? 1 : 0));
			}
			if (num != 0)
			{
				if (sortOrderModifier == 1)
				{
					return num;
				}
				return -num;
			}
			return 0;
		}
	}

	public SQLiteConnection cnn;

	public string connectionString = "";

	public int ImmersionLevel;

	public string GroupID;

	public string traineeID;

	public string GroupCaption;

	public string traineeName;

	private DataGridViewColumnSortMode sortMode = DataGridViewColumnSortMode.Programmatic;

	private IContainer components;

	private DataGridView mainDataGridView;

	private Button ChartButton;

	private Button BackButton;

	private Button EnterButton;

	private Button ExitButton;

	private DataGridViewTextBoxColumn GroupCaptionColumn;

	private DataGridViewTextBoxColumn InstructorColumn;

	private DataGridViewTextBoxColumn TraineesCountColumn;

	private DataGridViewTextBoxColumn TripsCountColumn;

	private DataGridViewTextBoxColumn TotalScoreColumn;

	private Button ExportButton;

	private SaveFileDialog saveFileDialog1;

	public ATRMainForm()
	{
		InitializeComponent();
	}

	public List<Dictionary<string, string>> GetSQLArray(string SQL)
	{
		List<Dictionary<string, string>> list = new List<Dictionary<string, string>>();
		using SQLiteCommand sQLiteCommand = cnn.CreateCommand();
		sQLiteCommand.CommandText = SQL;
		sQLiteCommand.CommandType = CommandType.Text;
		SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
		while (sQLiteDataReader.Read())
		{
			Dictionary<string, string> dictionary = new Dictionary<string, string>();
			for (int i = 0; i < sQLiteDataReader.FieldCount; i++)
			{
				dictionary.Add(sQLiteDataReader.GetName(i), sQLiteDataReader[i].ToString());
			}
			list.Add(dictionary);
		}
		return list;
	}

	public List<Dictionary<string, string>> GetSQLArray2(string SQL)
	{
		List<Dictionary<string, string>> list = new List<Dictionary<string, string>>();
		using SQLiteCommand sQLiteCommand = cnn.CreateCommand();
		sQLiteCommand.CommandText = SQL;
		sQLiteCommand.CommandType = CommandType.Text;
		SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
		while (sQLiteDataReader.Read())
		{
			Dictionary<string, string> dictionary = new Dictionary<string, string>();
			dictionary.Add(sQLiteDataReader.GetName(0), sQLiteDataReader.GetInt32(0).ToString());
			list.Add(dictionary);
			dictionary.Add(sQLiteDataReader.GetName(1), sQLiteDataReader[1].ToString());
			list.Add(dictionary);
		}
		return list;
	}

	private int ExecuteQuery(string SQL)
	{
		using SQLiteCommand sQLiteCommand = cnn.CreateCommand();
		sQLiteCommand.CommandText = SQL;
		sQLiteCommand.CommandType = CommandType.Text;
		if (sQLiteCommand.ExecuteNonQuery() > 0)
		{
			return 1;
		}
		return 0;
	}

	public string GetSQLValue(string SQL)
	{
		string result = "";
		using (SQLiteCommand sQLiteCommand = cnn.CreateCommand())
		{
			sQLiteCommand.CommandText = SQL;
			sQLiteCommand.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
			if (sQLiteDataReader.Read())
			{
				result = sQLiteDataReader[0].ToString();
			}
		}
		return result;
	}

	public static string Get2DigitsFromNumber(string num)
	{
		return $"{Convert.ToDouble(num):F2}";
	}

	private void GenerateGroupResults()
	{
		mainDataGridView.Rows.Clear();
		((ISupportInitialize)mainDataGridView).BeginInit();
		mainDataGridView.Columns.Clear();
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "GroupCaptionColumn",
			HeaderText = "Группа",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "InstructorColumn",
			HeaderText = "ФИО Инструктора",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "TraineesCountColumn",
			HeaderText = "Кол-во обучаемых",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "TripsCountColumn",
			HeaderText = "Кол-во поездок",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "TotalScoreColumn",
			HeaderText = "Средний штрафной балл",
			ReadOnly = true,
			SortMode = sortMode
		});
		((ISupportInitialize)mainDataGridView).EndInit();
		List<Dictionary<string, string>> sQLArray = GetSQLArray("SELECT id, name, instructor_id FROM student_groups");
		for (int i = 0; i < sQLArray.Count; i++)
		{
			string sQLValue = GetSQLValue("SELECT name FROM instructors WHERE instructors.id = " + sQLArray[i]["instructor_id"].ToString());
			string sQLValue2 = GetSQLValue("select distinct COUNT(*) from students where (group_id = " + sQLArray[i]["id"].ToString() + ")");
			string sQLValue3 = GetSQLValue("SELECT DISTINCT count(driving_sessions.id) FROM driving_sessions, students WHERE(driving_sessions.student_id = students.id) AND(students.group_id = " + sQLArray[i]["id"].ToString() + ")");
			string sQLValue4 = GetSQLValue("SELECT IFNULL(CAST(sum(driving_sessions.total_fault_points) as REAL) / count(driving_sessions.id), 0) FROM driving_sessions, students WHERE(driving_sessions.student_id = students.id) AND(students.group_id = " + sQLArray[i]["id"].ToString() + ")");
			mainDataGridView.Rows.Add(sQLArray[i]["name"].ToString(), sQLValue, sQLValue2, sQLValue3, Get2DigitsFromNumber(sQLValue4));
			mainDataGridView.Rows[mainDataGridView.Rows.Count - 1].Tag = sQLArray[i]["id"].ToString();
		}
		mainDataGridView.ClearSelection();
		ImmersionLevel = 0;
		Text = "Анализ результатов поездок.";
	}

	private void GenerateTraineesResults(string id_group)
	{
		mainDataGridView.Rows.Clear();
		((ISupportInitialize)mainDataGridView).BeginInit();
		mainDataGridView.Columns.Clear();
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "FIOColumn",
			HeaderText = "ФИО",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "TripsCountColumn",
			HeaderText = "Кол-во поездок",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "TripsDistanceColumn",
			HeaderText = "Общая дистанция",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "TotalPointsColumn",
			HeaderText = "Общее кол-во штрафных баллов по нарушениям",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "AvgPointsColumn",
			HeaderText = "Среднее кол-во штрафных баллов за поездку",
			ReadOnly = true,
			SortMode = sortMode
		});
		((ISupportInitialize)mainDataGridView).EndInit();
		List<Dictionary<string, string>> sQLArray = GetSQLArray("SELECT id, name FROM students WHERE group_id = " + id_group);
		for (int i = 0; i < sQLArray.Count; i++)
		{
			string sQLValue = GetSQLValue("SELECT DISTINCT count(driving_sessions.id) FROM driving_sessions WHERE (driving_sessions.student_id = " + sQLArray[i]["id"].ToString() + ")");
			string sQLValue2 = GetSQLValue("SELECT DISTINCT IFNULL(CAST(sum(driving_sessions.distance_travelled_km) as REAL),0) FROM driving_sessions WHERE (driving_sessions.student_id = " + sQLArray[i]["id"].ToString() + ")");
			string sQLValue3 = GetSQLValue("SELECT IFNULL(CAST(sum(driving_sessions.total_fault_points) as REAL), 0) FROM driving_sessions WHERE (driving_sessions.student_id = " + sQLArray[i]["id"].ToString() + ")");
			string sQLValue4 = GetSQLValue("SELECT IFNULL(CAST(sum(driving_sessions.total_fault_points) as REAL) / count(driving_sessions.id), 0) FROM driving_sessions WHERE (driving_sessions.student_id = " + sQLArray[i]["id"].ToString() + ")");
			mainDataGridView.Rows.Add(sQLArray[i]["name"].ToString(), sQLValue, Get2DigitsFromNumber(sQLValue2), sQLValue3, Get2DigitsFromNumber(sQLValue4));
			mainDataGridView.Rows[mainDataGridView.Rows.Count - 1].Tag = sQLArray[i]["id"].ToString();
		}
		mainDataGridView.ClearSelection();
		ImmersionLevel = 1;
		Text = "Анализ результатов поездок. Группа: " + (GroupCaption = GetSQLValue("SELECT name FROM student_groups WHERE id = " + id_group));
	}

	private void GenerateTraineeResults(string id_trainee)
	{
		mainDataGridView.Rows.Clear();
		((ISupportInitialize)mainDataGridView).BeginInit();
		mainDataGridView.Columns.Clear();
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "DateColumn",
			HeaderText = "Дата поездки",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "SectionColumn",
			HeaderText = "Участок",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "DistanceColumn",
			HeaderText = "Расстояние",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "NumberOfAbnormalSituationsColumn",
			HeaderText = "Кол-во нарушений для данной поездки",
			ReadOnly = true,
			SortMode = sortMode
		});
		mainDataGridView.Columns.Add(new DataGridViewTextBoxColumn
		{
			Name = "TotalPointsColumn",
			HeaderText = "Сумма штрафных баллов по нарушениям за поездку",
			ReadOnly = true,
			SortMode = sortMode
		});
		((ISupportInitialize)mainDataGridView).EndInit();
		using (SQLiteCommand sQLiteCommand = cnn.CreateCommand())
		{
			sQLiteCommand.CommandText = "SELECT CAST(driving_sessions.time AS TEXT) as ds_time, world_name, distance_travelled_km, num_driver_faults, total_fault_points FROM driving_sessions WHERE student_id = " + id_trainee;
			sQLiteCommand.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
			while (sQLiteDataReader.Read())
			{
				mainDataGridView.Rows.Add(sQLiteDataReader["ds_time"].ToString(), sQLiteDataReader["world_name"].ToString(), Get2DigitsFromNumber(sQLiteDataReader["distance_travelled_km"].ToString()), sQLiteDataReader["num_driver_faults"].ToString(), sQLiteDataReader["total_fault_points"].ToString());
			}
		}
		mainDataGridView.ClearSelection();
		ImmersionLevel = 2;
		string sQLValue = GetSQLValue("SELECT name FROM students WHERE id = " + id_trainee);
		traineeName = sQLValue;
		Text = "Анализ результатов поездок. Группа: " + GroupCaption + " Обучающийся: " + traineeName;
	}

	private void ATRMainForm_Load(object sender, EventArgs e)
	{
		if (File.Exists(Application.StartupPath + "\\\\ELS.db"))
		{
			try
			{
				connectionString = "Data Source=" + Application.StartupPath + "\\\\ELS.db";
				cnn = new SQLiteConnection(connectionString);
				cnn.Open();
				ExecuteQuery("PRAGMA journal_mode=WAL");
				mainDataGridView.ClearSelection();
				GenerateGroupResults();
				return;
			}
			catch (Exception ex)
			{
				MessageBox.Show(ex.Message);
				return;
			}
		}
		MessageBox.Show("Не найдена БД !");
		Close();
	}

	private void ExitButton_Click(object sender, EventArgs e)
	{
		Close();
	}

	private void ATRMainForm_FormClosed(object sender, FormClosedEventArgs e)
	{
		if (cnn != null && cnn.State == ConnectionState.Open)
		{
			cnn.Close();
		}
	}

	private void EnterFunc()
	{
		if (mainDataGridView.SelectedRows.Count > 0 && mainDataGridView.SelectedRows[0].Tag != null)
		{
			switch (ImmersionLevel)
			{
			case 0:
				GroupID = mainDataGridView.SelectedRows[0].Tag.ToString();
				GenerateTraineesResults(mainDataGridView.SelectedRows[0].Tag.ToString());
				BackButton.Enabled = true;
				break;
			case 1:
				traineeID = mainDataGridView.SelectedRows[0].Tag.ToString();
				GenerateTraineeResults(mainDataGridView.SelectedRows[0].Tag.ToString());
				BackButton.Enabled = true;
				EnterButton.Enabled = false;
				break;
			}
		}
	}

	private void EnterButton_Click(object sender, EventArgs e)
	{
		EnterFunc();
	}

	private void BackFunc()
	{
		switch (ImmersionLevel)
		{
		case 1:
			GenerateGroupResults();
			ImmersionLevel = 0;
			BackButton.Enabled = false;
			break;
		case 2:
			GenerateTraineesResults(GroupID);
			ImmersionLevel = 1;
			EnterButton.Enabled = true;
			break;
		}
	}

	private void BackButton_Click(object sender, EventArgs e)
	{
		BackFunc();
	}

	private void mainDataGridView_CellDoubleClick(object sender, DataGridViewCellEventArgs e)
	{
		if (e.RowIndex >= 0)
		{
			EnterFunc();
		}
	}

	private void ATRMainForm_Resize(object sender, EventArgs e)
	{
		mainDataGridView.Width = ClientSize.Width - 23;
		mainDataGridView.Height = ClientSize.Height - 62;
		ChartButton.Top = ClientSize.Height - 44;
		BackButton.Top = ClientSize.Height - 44;
		EnterButton.Top = ClientSize.Height - 44;
		ExportButton.Top = ClientSize.Height - 44;
		ExitButton.Top = ClientSize.Height - 44;
		ChartButton.Width = (ClientSize.Width - 24 - 23) / 5;
		BackButton.Width = (ClientSize.Width - 24 - 23) / 5;
		EnterButton.Width = (ClientSize.Width - 24 - 23) / 5;
		ExportButton.Width = (ClientSize.Width - 24 - 23) / 5;
		ExitButton.Width = (ClientSize.Width - 24 - 23) / 5;
		BackButton.Left = ChartButton.Left + ChartButton.Width + 6;
		EnterButton.Left = BackButton.Left + BackButton.Width + 7;
		ExportButton.Left = EnterButton.Left + EnterButton.Width + 7;
		ExitButton.Left = ExportButton.Left + ExportButton.Width + 6;
	}

	private void mainDataGridView_MouseUp(object sender, MouseEventArgs e)
	{
		if (e.Button == MouseButtons.Right)
		{
			BackFunc();
		}
	}

	private void ChartButton_Click(object sender, EventArgs e)
	{
		new ChartForm(this).Show();
	}

	private void mainDataGridView_ColumnHeaderMouseClick(object sender, DataGridViewCellMouseEventArgs e)
	{
		int columnIndex = e.ColumnIndex;
		for (int i = 0; i < mainDataGridView.Columns.Count; i++)
		{
			if (i != columnIndex)
			{
				mainDataGridView.Columns[i].HeaderCell.SortGlyphDirection = SortOrder.None;
			}
		}
		if (mainDataGridView.Columns[e.ColumnIndex].HeaderCell.SortGlyphDirection == SortOrder.Ascending)
		{
			mainDataGridView.Sort(new RowComparer(SortOrder.Descending, columnIndex));
			mainDataGridView.Columns[e.ColumnIndex].HeaderCell.SortGlyphDirection = SortOrder.Descending;
		}
		else
		{
			mainDataGridView.Sort(new RowComparer(SortOrder.Ascending, columnIndex));
			mainDataGridView.Columns[e.ColumnIndex].HeaderCell.SortGlyphDirection = SortOrder.Ascending;
		}
	}

	private void ExportButton_Click(object sender, EventArgs e)
	{
		if (saveFileDialog1.ShowDialog() == DialogResult.OK && !string.IsNullOrWhiteSpace(saveFileDialog1.FileName))
		{
			ExportToFile.Export(mainDataGridView, saveFileDialog1.FileName, Path.GetExtension(saveFileDialog1.FileName), Text);
		}
	}

	protected override void Dispose(bool disposing)
	{
		if (disposing && components != null)
		{
			components.Dispose();
		}
		base.Dispose(disposing);
	}

	private void InitializeComponent()
	{
		this.mainDataGridView = new System.Windows.Forms.DataGridView();
		this.GroupCaptionColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.InstructorColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.TraineesCountColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.TripsCountColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.TotalScoreColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.ChartButton = new System.Windows.Forms.Button();
		this.BackButton = new System.Windows.Forms.Button();
		this.EnterButton = new System.Windows.Forms.Button();
		this.ExitButton = new System.Windows.Forms.Button();
		this.ExportButton = new System.Windows.Forms.Button();
		this.saveFileDialog1 = new System.Windows.Forms.SaveFileDialog();
		((System.ComponentModel.ISupportInitialize)this.mainDataGridView).BeginInit();
		base.SuspendLayout();
		this.mainDataGridView.AllowUserToAddRows = false;
		this.mainDataGridView.AllowUserToDeleteRows = false;
		this.mainDataGridView.AllowUserToResizeColumns = false;
		this.mainDataGridView.AllowUserToResizeRows = false;
		this.mainDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
		this.mainDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
		this.mainDataGridView.Columns.AddRange(this.GroupCaptionColumn, this.InstructorColumn, this.TraineesCountColumn, this.TripsCountColumn, this.TotalScoreColumn);
		this.mainDataGridView.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnKeystroke;
		this.mainDataGridView.Location = new System.Drawing.Point(12, 12);
		this.mainDataGridView.MultiSelect = false;
		this.mainDataGridView.Name = "mainDataGridView";
		this.mainDataGridView.RowHeadersVisible = false;
		this.mainDataGridView.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
		this.mainDataGridView.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
		this.mainDataGridView.ShowCellErrors = false;
		this.mainDataGridView.ShowCellToolTips = false;
		this.mainDataGridView.ShowEditingIcon = false;
		this.mainDataGridView.ShowRowErrors = false;
		this.mainDataGridView.Size = new System.Drawing.Size(1170, 567);
		this.mainDataGridView.TabIndex = 0;
		this.mainDataGridView.CellDoubleClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.mainDataGridView_CellDoubleClick);
		this.mainDataGridView.ColumnHeaderMouseClick += new System.Windows.Forms.DataGridViewCellMouseEventHandler(this.mainDataGridView_ColumnHeaderMouseClick);
		this.mainDataGridView.MouseUp += new System.Windows.Forms.MouseEventHandler(this.mainDataGridView_MouseUp);
		this.GroupCaptionColumn.HeaderText = "Группа";
		this.GroupCaptionColumn.Name = "GroupCaptionColumn";
		this.GroupCaptionColumn.ReadOnly = true;
		this.InstructorColumn.HeaderText = "ФИО Инструктора";
		this.InstructorColumn.Name = "InstructorColumn";
		this.InstructorColumn.ReadOnly = true;
		this.TraineesCountColumn.HeaderText = "Кол-во обучаемых";
		this.TraineesCountColumn.Name = "TraineesCountColumn";
		this.TraineesCountColumn.ReadOnly = true;
		this.TripsCountColumn.HeaderText = "Кол-во поездок";
		this.TripsCountColumn.Name = "TripsCountColumn";
		this.TripsCountColumn.ReadOnly = true;
		this.TotalScoreColumn.HeaderText = "Общий балл";
		this.TotalScoreColumn.Name = "TotalScoreColumn";
		this.TotalScoreColumn.ReadOnly = true;
		this.ChartButton.Location = new System.Drawing.Point(11, 585);
		this.ChartButton.Name = "ChartButton";
		this.ChartButton.Size = new System.Drawing.Size(229, 35);
		this.ChartButton.TabIndex = 1;
		this.ChartButton.Text = "Диаграмма";
		this.ChartButton.UseVisualStyleBackColor = true;
		this.ChartButton.Click += new System.EventHandler(this.ChartButton_Click);
		this.BackButton.Enabled = false;
		this.BackButton.Location = new System.Drawing.Point(246, 585);
		this.BackButton.Name = "BackButton";
		this.BackButton.Size = new System.Drawing.Size(229, 35);
		this.BackButton.TabIndex = 2;
		this.BackButton.Text = "Назад";
		this.BackButton.UseVisualStyleBackColor = true;
		this.BackButton.Click += new System.EventHandler(this.BackButton_Click);
		this.EnterButton.Location = new System.Drawing.Point(482, 585);
		this.EnterButton.Name = "EnterButton";
		this.EnterButton.Size = new System.Drawing.Size(229, 35);
		this.EnterButton.TabIndex = 3;
		this.EnterButton.Text = "Войти";
		this.EnterButton.UseVisualStyleBackColor = true;
		this.EnterButton.Click += new System.EventHandler(this.EnterButton_Click);
		this.ExitButton.Location = new System.Drawing.Point(953, 585);
		this.ExitButton.Name = "ExitButton";
		this.ExitButton.Size = new System.Drawing.Size(229, 35);
		this.ExitButton.TabIndex = 4;
		this.ExitButton.Text = "Выход";
		this.ExitButton.UseVisualStyleBackColor = true;
		this.ExitButton.Click += new System.EventHandler(this.ExitButton_Click);
		this.ExportButton.Location = new System.Drawing.Point(718, 585);
		this.ExportButton.Name = "ExportButton";
		this.ExportButton.Size = new System.Drawing.Size(229, 35);
		this.ExportButton.TabIndex = 5;
		this.ExportButton.Text = "Экспорт";
		this.ExportButton.UseVisualStyleBackColor = true;
		this.ExportButton.Click += new System.EventHandler(this.ExportButton_Click);
		this.saveFileDialog1.DefaultExt = "csv";
		this.saveFileDialog1.Filter = "CSV|*.csv|PDF|*.pdf|HTML|*.html|Excel|*.xlsx";
		this.saveFileDialog1.Title = "Экспорт данных в файл";
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		base.ClientSize = new System.Drawing.Size(1193, 629);
		base.Controls.Add(this.ExportButton);
		base.Controls.Add(this.ExitButton);
		base.Controls.Add(this.EnterButton);
		base.Controls.Add(this.BackButton);
		base.Controls.Add(this.ChartButton);
		base.Controls.Add(this.mainDataGridView);
		base.Name = "ATRMainForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "Анализ результатов поездок";
		base.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.ATRMainForm_FormClosed);
		base.Load += new System.EventHandler(this.ATRMainForm_Load);
		base.Resize += new System.EventHandler(this.ATRMainForm_Resize);
		((System.ComponentModel.ISupportInitialize)this.mainDataGridView).EndInit();
		base.ResumeLayout(false);
	}
}
