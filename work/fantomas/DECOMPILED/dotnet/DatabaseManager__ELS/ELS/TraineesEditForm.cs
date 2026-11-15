using System;
using System.ComponentModel;
using System.Data;
using System.Data.SQLite;
using System.Drawing;
using System.Windows.Forms;

namespace ELS;

public class TraineesEditForm : Form
{
	private MainForm mainForm;

	private string traineeID = "";

	private string groupID = "";

	private IContainer components = null;

	private TextBox FIOTextBox;

	private Label FIOLabel;

	private new Button CancelButton;

	private Button SaveButton;

	private Label DateOfBirthLabel;

	private ComboBox GroupComboBox;

	private Label GroupLabel;

	private DateTimePicker DateOfBirthDTPicker;

	public TraineesEditForm()
	{
		InitializeComponent();
	}

	public TraineesEditForm(MainForm _MainForm, string _groupID)
	{
		InitializeComponent();
		mainForm = _MainForm;
		groupID = _groupID;
	}

	public TraineesEditForm(MainForm _MainForm, string _groupID, string _traineeID)
	{
		InitializeComponent();
		mainForm = _MainForm;
		groupID = _groupID;
		traineeID = _traineeID;
	}

	private void TraineesEditForm_Load(object sender, EventArgs e)
	{
		GroupComboBox.Items.Clear();
		GroupComboBox.DisplayMember = "Text";
		GroupComboBox.ValueMember = "Value";
		using (SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand())
		{
			sQLiteCommand.CommandText = "SELECT * FROM student_groups";
			sQLiteCommand.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
			while (sQLiteDataReader.Read())
			{
				GroupComboBox.Items.Add(new
				{
					Text = sQLiteDataReader["name"].ToString(),
					Value = sQLiteDataReader["id"].ToString()
				});
			}
		}
		for (int i = 0; i < GroupComboBox.Items.Count; i++)
		{
			if (((dynamic)GroupComboBox.Items[i]).Value.ToString() == groupID)
			{
				GroupComboBox.SelectedIndex = i;
			}
		}
		if (mainForm.userIsAdmin == 1)
		{
			GroupComboBox.Enabled = true;
		}
		if (traineeID == "")
		{
			Text = "Добавление обучающегося";
			return;
		}
		Text = "Редактирование обучающегося";
		using SQLiteCommand sQLiteCommand2 = mainForm.cnn.CreateCommand();
		sQLiteCommand2.CommandText = "SELECT * FROM students WHERE id = " + traineeID;
		sQLiteCommand2.CommandType = CommandType.Text;
		SQLiteDataReader sQLiteDataReader2 = sQLiteCommand2.ExecuteReader();
		if (sQLiteDataReader2.Read())
		{
			FIOTextBox.Text = sQLiteDataReader2["name"].ToString();
			DateOfBirthDTPicker.Text = sQLiteDataReader2["date_of_birth"].ToString();
		}
	}

	private void CancelButton_Click(object sender, EventArgs e)
	{
		Close();
	}

	private void SaveButton_Click(object sender, EventArgs e)
	{
		if (FIOTextBox.Text != "" && DateOfBirthDTPicker.Text != "" && GroupComboBox.SelectedIndex != -1)
		{
			if (traineeID == "")
			{
				using SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand();
				sQLiteCommand.CommandText = "INSERT INTO students (name, date_of_birth, group_id) VALUES (@name, @date_of_birth, @group_id)";
				sQLiteCommand.Parameters.Add(new SQLiteParameter("@name")
				{
					Value = FIOTextBox.Text
				});
				sQLiteCommand.Parameters.Add(new SQLiteParameter("@date_of_birth")
				{
					Value = DateOfBirthDTPicker.Value.ToString("yyyy-MM-dd")
				});
				sQLiteCommand.Parameters.Add(new SQLiteParameter("@group_id")
				{
					Value = ((dynamic)GroupComboBox.SelectedItem).Value
				});
				sQLiteCommand.CommandType = CommandType.Text;
				int num = sQLiteCommand.ExecuteNonQuery();
				if (num > 0)
				{
					MessageBox.Show("Данные сохранены !");
				}
				else
				{
					MessageBox.Show("Произошла ошибка. Данные не изменены !");
				}
			}
			else
			{
				using SQLiteCommand sQLiteCommand2 = mainForm.cnn.CreateCommand();
				sQLiteCommand2.CommandText = "UPDATE students SET name = @name, date_of_birth = @date_of_birth, group_id = @group_id WHERE (id = @traineeID)";
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@name")
				{
					Value = FIOTextBox.Text
				});
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@date_of_birth")
				{
					Value = DateOfBirthDTPicker.Value.ToString("yyyy-MM-dd")
				});
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@group_id")
				{
					Value = ((dynamic)GroupComboBox.SelectedItem).Value
				});
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@traineeID")
				{
					Value = traineeID
				});
				sQLiteCommand2.CommandType = CommandType.Text;
				int num2 = sQLiteCommand2.ExecuteNonQuery();
				if (num2 > 0)
				{
					MessageBox.Show("Данные сохранены !");
				}
				else
				{
					MessageBox.Show("Произошла ошибка. Данные не изменены !");
				}
			}
			mainForm.RefreshGroups();
			Close();
		}
		else
		{
			MessageBox.Show("Необходимо задать ФИО, Тип и Табельный номер !");
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
		this.FIOTextBox = new System.Windows.Forms.TextBox();
		this.FIOLabel = new System.Windows.Forms.Label();
		this.CancelButton = new System.Windows.Forms.Button();
		this.SaveButton = new System.Windows.Forms.Button();
		this.DateOfBirthLabel = new System.Windows.Forms.Label();
		this.GroupComboBox = new System.Windows.Forms.ComboBox();
		this.GroupLabel = new System.Windows.Forms.Label();
		this.DateOfBirthDTPicker = new System.Windows.Forms.DateTimePicker();
		base.SuspendLayout();
		this.FIOTextBox.Location = new System.Drawing.Point(15, 25);
		this.FIOTextBox.Name = "FIOTextBox";
		this.FIOTextBox.Size = new System.Drawing.Size(258, 20);
		this.FIOTextBox.TabIndex = 10;
		this.FIOLabel.AutoSize = true;
		this.FIOLabel.Location = new System.Drawing.Point(12, 9);
		this.FIOLabel.Name = "FIOLabel";
		this.FIOLabel.Size = new System.Drawing.Size(43, 13);
		this.FIOLabel.TabIndex = 8;
		this.FIOLabel.Text = "ФИО : ";
		this.CancelButton.Location = new System.Drawing.Point(149, 150);
		this.CancelButton.Name = "CancelButton";
		this.CancelButton.Size = new System.Drawing.Size(122, 32);
		this.CancelButton.TabIndex = 7;
		this.CancelButton.Text = "Отмена";
		this.CancelButton.UseVisualStyleBackColor = true;
		this.CancelButton.Click += new System.EventHandler(this.CancelButton_Click);
		this.SaveButton.Location = new System.Drawing.Point(14, 150);
		this.SaveButton.Name = "SaveButton";
		this.SaveButton.Size = new System.Drawing.Size(122, 32);
		this.SaveButton.TabIndex = 6;
		this.SaveButton.Text = "Сохранить";
		this.SaveButton.UseVisualStyleBackColor = true;
		this.SaveButton.Click += new System.EventHandler(this.SaveButton_Click);
		this.DateOfBirthLabel.AutoSize = true;
		this.DateOfBirthLabel.Location = new System.Drawing.Point(12, 50);
		this.DateOfBirthLabel.Name = "DateOfBirthLabel";
		this.DateOfBirthLabel.Size = new System.Drawing.Size(95, 13);
		this.DateOfBirthLabel.TabIndex = 12;
		this.DateOfBirthLabel.Text = "Дата рождения : ";
		this.GroupComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
		this.GroupComboBox.Enabled = false;
		this.GroupComboBox.FormattingEnabled = true;
		this.GroupComboBox.Location = new System.Drawing.Point(14, 110);
		this.GroupComboBox.Name = "GroupComboBox";
		this.GroupComboBox.Size = new System.Drawing.Size(257, 21);
		this.GroupComboBox.TabIndex = 15;
		this.GroupLabel.AutoSize = true;
		this.GroupLabel.Location = new System.Drawing.Point(11, 94);
		this.GroupLabel.Name = "GroupLabel";
		this.GroupLabel.Size = new System.Drawing.Size(48, 13);
		this.GroupLabel.TabIndex = 14;
		this.GroupLabel.Text = "Группа :";
		this.DateOfBirthDTPicker.Location = new System.Drawing.Point(15, 71);
		this.DateOfBirthDTPicker.Name = "DateOfBirthDTPicker";
		this.DateOfBirthDTPicker.Size = new System.Drawing.Size(257, 20);
		this.DateOfBirthDTPicker.TabIndex = 16;
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		base.ClientSize = new System.Drawing.Size(284, 192);
		base.Controls.Add(this.DateOfBirthDTPicker);
		base.Controls.Add(this.GroupComboBox);
		base.Controls.Add(this.GroupLabel);
		base.Controls.Add(this.DateOfBirthLabel);
		base.Controls.Add(this.FIOTextBox);
		base.Controls.Add(this.FIOLabel);
		base.Controls.Add(this.CancelButton);
		base.Controls.Add(this.SaveButton);
		base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
		base.MaximizeBox = false;
		base.MinimizeBox = false;
		base.Name = "TraineesEditForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "Редактирование обучающихся";
		base.Load += new System.EventHandler(this.TraineesEditForm_Load);
		base.ResumeLayout(false);
		base.PerformLayout();
	}
}
