using System;
using System.ComponentModel;
using System.Data;
using System.Data.SQLite;
using System.Drawing;
using System.Windows.Forms;

namespace ELS;

public class EditGroupsForm : Form
{
	private MainForm mainForm;

	private string groupID = "";

	private IContainer components = null;

	private Button SaveButton;

	private new Button CancelButton;

	private Label label1;

	private Label label2;

	private TextBox GroupTextBox;

	private ComboBox InstructorComboBox;

	public EditGroupsForm()
	{
		InitializeComponent();
	}

	public EditGroupsForm(MainForm _mainForm)
	{
		InitializeComponent();
		mainForm = _mainForm;
	}

	public EditGroupsForm(MainForm _mainForm, string _groupID)
	{
		InitializeComponent();
		mainForm = _mainForm;
		groupID = _groupID;
	}

	private void CancelButton_Click(object sender, EventArgs e)
	{
		Close();
	}

	private void SaveButton_Click(object sender, EventArgs e)
	{
		if (GroupTextBox.Text != "" && InstructorComboBox.SelectedIndex != -1)
		{
			if (groupID == "")
			{
				using SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand();
				sQLiteCommand.CommandText = "INSERT INTO student_groups (name, instructor_id) VALUES (@groupCaption, @groupInstructor)";
				sQLiteCommand.Parameters.Add(new SQLiteParameter("@groupCaption")
				{
					Value = GroupTextBox.Text
				});
				sQLiteCommand.Parameters.Add(new SQLiteParameter("@groupInstructor")
				{
					Value = ((dynamic)InstructorComboBox.SelectedItem).Value
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
				sQLiteCommand2.CommandText = "UPDATE student_groups SET name = @groupCaption, instructor_id = @groupInstructor WHERE (id = @groupID)";
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@groupCaption")
				{
					Value = GroupTextBox.Text
				});
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@groupInstructor")
				{
					Value = ((dynamic)InstructorComboBox.SelectedItem).Value
				});
				sQLiteCommand2.Parameters.Add(new SQLiteParameter("@groupID")
				{
					Value = groupID
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
			MessageBox.Show("Необходимо задать наименование группы или не выбран инструктор !");
		}
	}

	private void EditGroups_Load(object sender, EventArgs e)
	{
		InstructorComboBox.Items.Clear();
		InstructorComboBox.DisplayMember = "Text";
		InstructorComboBox.ValueMember = "Value";
		using (SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand())
		{
			sQLiteCommand.CommandText = "SELECT * FROM instructors";
			sQLiteCommand.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
			while (sQLiteDataReader.Read())
			{
				InstructorComboBox.Items.Add(new
				{
					Text = sQLiteDataReader["name"].ToString(),
					Value = sQLiteDataReader["id"].ToString()
				});
			}
		}
		if (groupID != "")
		{
			Text = "Редактирование группы";
			using SQLiteCommand sQLiteCommand2 = mainForm.cnn.CreateCommand();
			sQLiteCommand2.CommandText = "SELECT * FROM student_groups WHERE id = " + groupID;
			sQLiteCommand2.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader2 = sQLiteCommand2.ExecuteReader();
			if (!sQLiteDataReader2.Read())
			{
				return;
			}
			GroupTextBox.Text = sQLiteDataReader2["name"].ToString();
			for (int i = 0; i < InstructorComboBox.Items.Count; i++)
			{
				if (((dynamic)InstructorComboBox.Items[i]).Value.ToString() == sQLiteDataReader2["instructor_id"].ToString())
				{
					InstructorComboBox.SelectedIndex = i;
				}
			}
			return;
		}
		Text = "Создание группы";
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
		this.SaveButton = new System.Windows.Forms.Button();
		this.CancelButton = new System.Windows.Forms.Button();
		this.label1 = new System.Windows.Forms.Label();
		this.label2 = new System.Windows.Forms.Label();
		this.GroupTextBox = new System.Windows.Forms.TextBox();
		this.InstructorComboBox = new System.Windows.Forms.ComboBox();
		base.SuspendLayout();
		this.SaveButton.Location = new System.Drawing.Point(15, 145);
		this.SaveButton.Name = "SaveButton";
		this.SaveButton.Size = new System.Drawing.Size(122, 32);
		this.SaveButton.TabIndex = 0;
		this.SaveButton.Text = "Сохранить";
		this.SaveButton.UseVisualStyleBackColor = true;
		this.SaveButton.Click += new System.EventHandler(this.SaveButton_Click);
		this.CancelButton.Location = new System.Drawing.Point(150, 145);
		this.CancelButton.Name = "CancelButton";
		this.CancelButton.Size = new System.Drawing.Size(122, 32);
		this.CancelButton.TabIndex = 1;
		this.CancelButton.Text = "Отмена";
		this.CancelButton.UseVisualStyleBackColor = true;
		this.CancelButton.Click += new System.EventHandler(this.CancelButton_Click);
		this.label1.AutoSize = true;
		this.label1.Location = new System.Drawing.Point(12, 19);
		this.label1.Name = "label1";
		this.label1.Size = new System.Drawing.Size(102, 13);
		this.label1.TabIndex = 2;
		this.label1.Text = "Название группы :";
		this.label2.AutoSize = true;
		this.label2.Location = new System.Drawing.Point(12, 81);
		this.label2.Name = "label2";
		this.label2.Size = new System.Drawing.Size(72, 13);
		this.label2.TabIndex = 3;
		this.label2.Text = "Инструктор :";
		this.GroupTextBox.Location = new System.Drawing.Point(14, 45);
		this.GroupTextBox.Name = "GroupTextBox";
		this.GroupTextBox.Size = new System.Drawing.Size(258, 20);
		this.GroupTextBox.TabIndex = 4;
		this.InstructorComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
		this.InstructorComboBox.FormattingEnabled = true;
		this.InstructorComboBox.Location = new System.Drawing.Point(15, 107);
		this.InstructorComboBox.Name = "InstructorComboBox";
		this.InstructorComboBox.Size = new System.Drawing.Size(257, 21);
		this.InstructorComboBox.TabIndex = 5;
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		base.ClientSize = new System.Drawing.Size(283, 183);
		base.Controls.Add(this.InstructorComboBox);
		base.Controls.Add(this.GroupTextBox);
		base.Controls.Add(this.label2);
		base.Controls.Add(this.label1);
		base.Controls.Add(this.CancelButton);
		base.Controls.Add(this.SaveButton);
		base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
		base.Name = "EditGroupsForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "Редактирование группы";
		base.Load += new System.EventHandler(this.EditGroups_Load);
		base.ResumeLayout(false);
		base.PerformLayout();
	}
}
