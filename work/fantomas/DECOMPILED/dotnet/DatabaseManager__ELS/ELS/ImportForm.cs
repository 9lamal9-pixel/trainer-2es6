using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Data.SQLite;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace ELS;

public class ImportForm : Form
{
	private MainForm mainForm;

	private ImportData importData;

	private IContainer components = null;

	private DataGridView MainDataGridView;

	private Button LoadFileButton;

	private OpenFileDialog openFileDialog1;

	private Label label1;

	private ComboBox SeparatorCB;

	private ComboBox EncodingCB;

	private Label label2;

	private CheckBox IgnoreFileHeaderCB;

	private Button DeleteButton;

	private DataGridViewTextBoxColumn user;

	private DataGridViewTextBoxColumn Column1;

	private DataGridViewTextBoxColumn Column2;

	private DataGridViewTextBoxColumn Column3;

	private Button ImportButton;

	private Button CloseButton;

	public ImportForm()
	{
		InitializeComponent();
	}

	public ImportForm(MainForm _MainForm)
	{
		InitializeComponent();
		mainForm = _MainForm;
	}

	private void LoadFileButton_Click(object sender, EventArgs e)
	{
		if (openFileDialog1.ShowDialog() != DialogResult.OK)
		{
			return;
		}
		MainDataGridView.Rows.Clear();
		importData = new ImportData(encoding: (!(EncodingCB.SelectedItem.ToString() == "UTF-8")) ? Encoding.GetEncoding("windows-1251") : Encoding.UTF8, separator: (SeparatorCB.SelectedIndex != 0) ? ";" : ",", filePath: openFileDialog1.FileName, IgnoreFileHeader: IgnoreFileHeaderCB.Checked);
		for (int i = 0; i < importData.Data.Count; i++)
		{
			MainDataGridView.Rows.Add(importData.Data[i].ID, importData.Data[i].FIO, importData.Data[i].Group, importData.Data[i].DateOfBirth);
			if (importData.Data[i].isError)
			{
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[0].Style.BackColor = Color.Red;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[1].Style.BackColor = Color.Red;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[2].Style.BackColor = Color.Red;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[3].Style.BackColor = Color.Red;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Tag = 1;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].ReadOnly = true;
			}
			else
			{
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Tag = 0;
			}
		}
		DeleteButton.Enabled = true;
		ImportButton.Enabled = true;
	}

	private void ImportForm_Load(object sender, EventArgs e)
	{
		SeparatorCB.SelectedIndex = 0;
		EncodingCB.SelectedIndex = 0;
		DeleteButton.Enabled = false;
		ImportButton.Enabled = false;
	}

	private void MainDataGridView_CellValueChanged(object sender, DataGridViewCellEventArgs e)
	{
		if (e.ColumnIndex >= 0 && e.RowIndex >= 0)
		{
			if (e.ColumnIndex == 1)
			{
				importData.Data[e.RowIndex].FIO = MainDataGridView[e.ColumnIndex, e.RowIndex].Value.ToString();
			}
			if (e.ColumnIndex == 2)
			{
				importData.Data[e.RowIndex].Group = MainDataGridView[e.ColumnIndex, e.RowIndex].Value.ToString();
			}
			if (e.ColumnIndex == 3)
			{
				DateTime dateTime = DateTime.Parse(MainDataGridView[e.ColumnIndex, e.RowIndex].Value.ToString());
				importData.Data[e.RowIndex].DateOfBirth = dateTime.ToString("yyyy-MM-dd");
			}
		}
	}

	private void DeleteButton_Click(object sender, EventArgs e)
	{
		if (MainDataGridView.SelectedRows.Count <= 0)
		{
			return;
		}
		int num = Convert.ToInt32(MainDataGridView.SelectedRows[0].Cells[0].Value.ToString());
		MainDataGridView.Rows.RemoveAt(MainDataGridView.SelectedRows[0].Index);
		for (int i = 0; i < importData.Data.Count; i++)
		{
			if (importData.Data[i].ID == num)
			{
				importData.Data.RemoveAt(i);
				break;
			}
		}
		MainDataGridView.Refresh();
	}

	private void ImportButton_Click(object sender, EventArgs e)
	{
		Dictionary<string, string> dictionary = new Dictionary<string, string>();
		using (SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand())
		{
			sQLiteCommand.CommandText = "SELECT * FROM student_groups";
			sQLiteCommand.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
			while (sQLiteDataReader.Read())
			{
				dictionary.Add(sQLiteDataReader["name"].ToString(), sQLiteDataReader["id"].ToString());
			}
		}
		for (int i = 0; i < MainDataGridView.Rows.Count; i++)
		{
			if (Convert.ToInt32(MainDataGridView.Rows[i].Tag) != 0)
			{
				continue;
			}
			string text = MainDataGridView.Rows[i].Cells[2].Value.ToString();
			if (dictionary.ContainsKey(text))
			{
				continue;
			}
			if (MessageBox.Show("Создать группу '" + text + "' ?", "Не найдена группа !", MessageBoxButtons.YesNo) == DialogResult.Yes)
			{
				using SQLiteCommand sQLiteCommand2 = mainForm.cnn.CreateCommand();
				sQLiteCommand2.CommandText = "INSERT INTO student_groups (name) VALUES (@group_name)";
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@group_name")
				{
					Value = text
				});
				sQLiteCommand2.CommandType = CommandType.Text;
				int num = sQLiteCommand2.ExecuteNonQuery();
				if (num > 0)
				{
					MessageBox.Show("Группа " + text + " создана !");
					dictionary.Add(text, mainForm.GetSQLValue("SELECT id FROM student_groups WHERE name = '" + text + "'"));
					continue;
				}
				MessageBox.Show("Произошла ошибка. Данные не изменены. Импорт отменен !");
			}
			else
			{
				MessageBox.Show("Импорт отменен !");
			}
			break;
		}
		for (int num2 = importData.Data.Count - 1; num2 >= 0; num2--)
		{
			if (!importData.Data[num2].isError)
			{
				if (mainForm.GetSQLValue("SELECT DISTINCT COUNT(*) FROM students WHERE (name = '" + importData.Data[num2].FIO + "') and (date_of_birth = '" + importData.Data[num2].DateOfBirth + "') and (group_id = '" + dictionary[importData.Data[num2].Group] + "')") == "0")
				{
					using SQLiteCommand sQLiteCommand3 = mainForm.cnn.CreateCommand();
					sQLiteCommand3.CommandText = "INSERT INTO students (name, date_of_birth, group_id) VALUES (@student_name, @student_date, @student_group)";
					sQLiteCommand3.Parameters.Add(new SQLiteParameter("@student_name")
					{
						Value = importData.Data[num2].FIO
					});
					sQLiteCommand3.Parameters.Add(new SQLiteParameter("@student_date")
					{
						Value = importData.Data[num2].DateOfBirth
					});
					sQLiteCommand3.Parameters.Add(new SQLiteParameter("@student_group")
					{
						Value = dictionary[importData.Data[num2].Group]
					});
					sQLiteCommand3.CommandType = CommandType.Text;
					int num3 = sQLiteCommand3.ExecuteNonQuery();
					if (num3 > 0)
					{
						importData.Data.RemoveAt(num2);
					}
					else
					{
						importData.Data[num2].isError = true;
					}
				}
				else if (MessageBox.Show("Продолжить импорт ?", "В группе '" + importData.Data[num2].Group + "' уже есть учащийся с такими ФИО и датой рождения", MessageBoxButtons.YesNo) == DialogResult.No)
				{
					break;
				}
			}
		}
		MainDataGridView.Rows.Clear();
		for (int j = 0; j < importData.Data.Count; j++)
		{
			MainDataGridView.Rows.Add(importData.Data[j].ID, importData.Data[j].FIO, importData.Data[j].Group, importData.Data[j].DateOfBirth);
			if (importData.Data[j].isError)
			{
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[0].Style.BackColor = Color.Red;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[1].Style.BackColor = Color.Red;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[2].Style.BackColor = Color.Red;
				MainDataGridView.Rows[MainDataGridView.Rows.Count - 1].Cells[3].Style.BackColor = Color.Red;
			}
		}
		mainForm.RefreshGroups();
	}

	private void MainDataGridView_KeyDown(object sender, KeyEventArgs e)
	{
		if (e.KeyCode == Keys.Delete)
		{
			DeleteButton_Click(sender, e);
		}
	}

	private void CloseButton_Click(object sender, EventArgs e)
	{
		mainForm.RefreshGroups();
		Close();
	}

	private void MainDataGridView_CellValidating(object sender, DataGridViewCellValidatingEventArgs e)
	{
		if (e.ColumnIndex != 3)
		{
			return;
		}
		if (e.FormattedValue.ToString().Length > 0)
		{
			try
			{
				DateTime.Parse(e.FormattedValue.ToString());
				return;
			}
			catch (Exception ex)
			{
				e.Cancel = true;
				MainDataGridView.CancelEdit();
				MainDataGridView.EndEdit();
				MessageBox.Show("Дата введена не верно : " + ex.Message);
				return;
			}
		}
		e.Cancel = true;
		MainDataGridView.CancelEdit();
		MainDataGridView.EndEdit();
	}

	private void MainDataGridView_CellEndEdit(object sender, DataGridViewCellEventArgs e)
	{
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
		System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle = new System.Windows.Forms.DataGridViewCellStyle();
		this.MainDataGridView = new System.Windows.Forms.DataGridView();
		this.user = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.Column1 = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.Column2 = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.Column3 = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.LoadFileButton = new System.Windows.Forms.Button();
		this.openFileDialog1 = new System.Windows.Forms.OpenFileDialog();
		this.label1 = new System.Windows.Forms.Label();
		this.SeparatorCB = new System.Windows.Forms.ComboBox();
		this.EncodingCB = new System.Windows.Forms.ComboBox();
		this.label2 = new System.Windows.Forms.Label();
		this.IgnoreFileHeaderCB = new System.Windows.Forms.CheckBox();
		this.DeleteButton = new System.Windows.Forms.Button();
		this.ImportButton = new System.Windows.Forms.Button();
		this.CloseButton = new System.Windows.Forms.Button();
		((System.ComponentModel.ISupportInitialize)this.MainDataGridView).BeginInit();
		base.SuspendLayout();
		this.MainDataGridView.AllowUserToAddRows = false;
		this.MainDataGridView.AllowUserToDeleteRows = false;
		this.MainDataGridView.AllowUserToResizeColumns = false;
		this.MainDataGridView.AllowUserToResizeRows = false;
		this.MainDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
		dataGridViewCellStyle.Alignment = System.Windows.Forms.DataGridViewContentAlignment.MiddleCenter;
		dataGridViewCellStyle.BackColor = System.Drawing.SystemColors.Control;
		dataGridViewCellStyle.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25f, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, 204);
		dataGridViewCellStyle.ForeColor = System.Drawing.SystemColors.WindowText;
		dataGridViewCellStyle.SelectionBackColor = System.Drawing.SystemColors.Highlight;
		dataGridViewCellStyle.SelectionForeColor = System.Drawing.SystemColors.HighlightText;
		dataGridViewCellStyle.WrapMode = System.Windows.Forms.DataGridViewTriState.True;
		this.MainDataGridView.ColumnHeadersDefaultCellStyle = dataGridViewCellStyle;
		this.MainDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
		this.MainDataGridView.Columns.AddRange(this.user, this.Column1, this.Column2, this.Column3);
		this.MainDataGridView.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnKeystroke;
		this.MainDataGridView.Location = new System.Drawing.Point(12, 12);
		this.MainDataGridView.MultiSelect = false;
		this.MainDataGridView.Name = "MainDataGridView";
		this.MainDataGridView.RowHeadersVisible = false;
		this.MainDataGridView.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
		this.MainDataGridView.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
		this.MainDataGridView.ShowCellErrors = false;
		this.MainDataGridView.ShowCellToolTips = false;
		this.MainDataGridView.ShowEditingIcon = false;
		this.MainDataGridView.ShowRowErrors = false;
		this.MainDataGridView.Size = new System.Drawing.Size(920, 481);
		this.MainDataGridView.StandardTab = true;
		this.MainDataGridView.TabIndex = 2;
		this.MainDataGridView.CellEndEdit += new System.Windows.Forms.DataGridViewCellEventHandler(this.MainDataGridView_CellEndEdit);
		this.MainDataGridView.CellValidating += new System.Windows.Forms.DataGridViewCellValidatingEventHandler(this.MainDataGridView_CellValidating);
		this.MainDataGridView.CellValueChanged += new System.Windows.Forms.DataGridViewCellEventHandler(this.MainDataGridView_CellValueChanged);
		this.MainDataGridView.KeyDown += new System.Windows.Forms.KeyEventHandler(this.MainDataGridView_KeyDown);
		this.user.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.None;
		this.user.FillWeight = 166f / (float)System.Math.PI;
		this.user.HeaderText = "№";
		this.user.Name = "user";
		this.user.ReadOnly = true;
		this.user.Resizable = System.Windows.Forms.DataGridViewTriState.False;
		this.user.Width = 50;
		this.Column1.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.Fill;
		this.Column1.FillWeight = 203.0457f;
		this.Column1.HeaderText = "ФИО";
		this.Column1.Name = "Column1";
		this.Column1.Resizable = System.Windows.Forms.DataGridViewTriState.False;
		this.Column2.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.None;
		this.Column2.FillWeight = 143.8084f;
		this.Column2.HeaderText = "Группа";
		this.Column2.Name = "Column2";
		this.Column2.Resizable = System.Windows.Forms.DataGridViewTriState.False;
		this.Column2.Width = 300;
		this.Column3.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.None;
		this.Column3.FillWeight = 0.3065151f;
		this.Column3.HeaderText = "Дата рождения";
		this.Column3.Name = "Column3";
		this.Column3.Resizable = System.Windows.Forms.DataGridViewTriState.False;
		this.Column3.Width = 150;
		this.LoadFileButton.Location = new System.Drawing.Point(241, 505);
		this.LoadFileButton.Name = "LoadFileButton";
		this.LoadFileButton.Size = new System.Drawing.Size(175, 71);
		this.LoadFileButton.TabIndex = 3;
		this.LoadFileButton.Text = "Загрузить файл";
		this.LoadFileButton.UseVisualStyleBackColor = true;
		this.LoadFileButton.Click += new System.EventHandler(this.LoadFileButton_Click);
		this.openFileDialog1.FileName = "openFileDialog1";
		this.openFileDialog1.Filter = "*.csv|*.csv";
		this.label1.AutoSize = true;
		this.label1.Location = new System.Drawing.Point(12, 508);
		this.label1.Name = "label1";
		this.label1.Size = new System.Drawing.Size(79, 13);
		this.label1.TabIndex = 4;
		this.label1.Text = "Разделитель :";
		this.SeparatorCB.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
		this.SeparatorCB.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25f, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, 204);
		this.SeparatorCB.FormattingEnabled = true;
		this.SeparatorCB.Items.AddRange(new object[2] { "Запятая(,)", "Точка с запятой(;)" });
		this.SeparatorCB.Location = new System.Drawing.Point(97, 505);
		this.SeparatorCB.Name = "SeparatorCB";
		this.SeparatorCB.Size = new System.Drawing.Size(138, 21);
		this.SeparatorCB.TabIndex = 5;
		this.EncodingCB.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
		this.EncodingCB.FormattingEnabled = true;
		this.EncodingCB.Items.AddRange(new object[2] { "UTF-8", "Windows-1251" });
		this.EncodingCB.Location = new System.Drawing.Point(97, 532);
		this.EncodingCB.Name = "EncodingCB";
		this.EncodingCB.Size = new System.Drawing.Size(138, 21);
		this.EncodingCB.TabIndex = 7;
		this.label2.AutoSize = true;
		this.label2.Location = new System.Drawing.Point(12, 535);
		this.label2.Name = "label2";
		this.label2.Size = new System.Drawing.Size(68, 13);
		this.label2.TabIndex = 6;
		this.label2.Text = "Кодировка :";
		this.IgnoreFileHeaderCB.AutoSize = true;
		this.IgnoreFileHeaderCB.Checked = true;
		this.IgnoreFileHeaderCB.CheckState = System.Windows.Forms.CheckState.Checked;
		this.IgnoreFileHeaderCB.Location = new System.Drawing.Point(15, 559);
		this.IgnoreFileHeaderCB.Name = "IgnoreFileHeaderCB";
		this.IgnoreFileHeaderCB.Size = new System.Drawing.Size(189, 17);
		this.IgnoreFileHeaderCB.TabIndex = 8;
		this.IgnoreFileHeaderCB.Text = "Игнорировать заголовок файла";
		this.IgnoreFileHeaderCB.UseVisualStyleBackColor = true;
		this.DeleteButton.Location = new System.Drawing.Point(422, 505);
		this.DeleteButton.Name = "DeleteButton";
		this.DeleteButton.Size = new System.Drawing.Size(175, 71);
		this.DeleteButton.TabIndex = 9;
		this.DeleteButton.Text = "Удалить";
		this.DeleteButton.UseVisualStyleBackColor = true;
		this.DeleteButton.Click += new System.EventHandler(this.DeleteButton_Click);
		this.ImportButton.Location = new System.Drawing.Point(603, 505);
		this.ImportButton.Name = "ImportButton";
		this.ImportButton.Size = new System.Drawing.Size(175, 71);
		this.ImportButton.TabIndex = 10;
		this.ImportButton.Text = "Импортировать данные";
		this.ImportButton.UseVisualStyleBackColor = true;
		this.ImportButton.Click += new System.EventHandler(this.ImportButton_Click);
		this.CloseButton.Location = new System.Drawing.Point(784, 505);
		this.CloseButton.Name = "CloseButton";
		this.CloseButton.Size = new System.Drawing.Size(148, 71);
		this.CloseButton.TabIndex = 11;
		this.CloseButton.Text = "Закрыть";
		this.CloseButton.UseVisualStyleBackColor = true;
		this.CloseButton.Click += new System.EventHandler(this.CloseButton_Click);
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		base.ClientSize = new System.Drawing.Size(944, 585);
		base.Controls.Add(this.CloseButton);
		base.Controls.Add(this.ImportButton);
		base.Controls.Add(this.DeleteButton);
		base.Controls.Add(this.IgnoreFileHeaderCB);
		base.Controls.Add(this.EncodingCB);
		base.Controls.Add(this.label2);
		base.Controls.Add(this.SeparatorCB);
		base.Controls.Add(this.label1);
		base.Controls.Add(this.LoadFileButton);
		base.Controls.Add(this.MainDataGridView);
		base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
		base.MaximizeBox = false;
		base.MinimizeBox = false;
		base.Name = "ImportForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "Импорт данных из файла";
		base.Load += new System.EventHandler(this.ImportForm_Load);
		((System.ComponentModel.ISupportInitialize)this.MainDataGridView).EndInit();
		base.ResumeLayout(false);
		base.PerformLayout();
	}
}
