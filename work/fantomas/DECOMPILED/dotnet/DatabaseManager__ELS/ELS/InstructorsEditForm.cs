using System;
using System.ComponentModel;
using System.Data;
using System.Data.SQLite;
using System.Drawing;
using System.Security.Cryptography;
using System.Text;
using System.Windows.Forms;
using ELS.Properties;

namespace ELS;

public class InstructorsEditForm : Form
{
	private MainForm mainForm;

	private string userID = "";

	private IContainer components = null;

	private Button CloseButton;

	private DataGridView UsersDataGridView;

	private Label label1;

	private Label label2;

	private TextBox userTextBox;

	private TextBox PasswordTextBox;

	private Button UserAddButton;

	private Button UserSaveButton;

	private Button UserDeleteButton;

	private DataGridViewTextBoxColumn user;

	private CheckBox isAdminCheckBox;

	public InstructorsEditForm()
	{
		InitializeComponent();
	}

	public InstructorsEditForm(MainForm _MainForm)
	{
		InitializeComponent();
		mainForm = _MainForm;
	}

	private void CloseButton_Click(object sender, EventArgs e)
	{
		Close();
	}

	private static string GenerateSalt()
	{
		RNGCryptoServiceProvider rNGCryptoServiceProvider = new RNGCryptoServiceProvider();
		byte[] array = new byte[16];
		rNGCryptoServiceProvider.GetNonZeroBytes(array);
		return Convert.ToBase64String(array);
	}

	public static string SHA1HashString(string input)
	{
		byte[] bytes = Encoding.UTF8.GetBytes(input);
		SHA1 sHA = SHA1.Create();
		byte[] array = sHA.ComputeHash(bytes);
		StringBuilder stringBuilder = new StringBuilder();
		byte[] array2 = array;
		foreach (byte b in array2)
		{
			string value = b.ToString("x2");
			stringBuilder.Append(value);
		}
		return stringBuilder.ToString();
	}

	private void refreshUsers()
	{
		UsersDataGridView.Rows.Clear();
		using SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand();
		sQLiteCommand.CommandText = "SELECT * FROM instructors";
		sQLiteCommand.CommandType = CommandType.Text;
		SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
		while (sQLiteDataReader.Read())
		{
			UsersDataGridView.Rows.Add(sQLiteDataReader["name"].ToString());
			UsersDataGridView.Rows[UsersDataGridView.Rows.Count - 1].Tag = sQLiteDataReader["id"].ToString();
		}
	}

	private void InstructorsEditForm_Load(object sender, EventArgs e)
	{
		refreshUsers();
		UserSaveButton.Enabled = false;
		UsersDataGridView.ClearSelection();
		userTextBox.Tag = "";
		PasswordTextBox.Tag = "";
		userTextBox.Enabled = false;
		PasswordTextBox.Enabled = false;
	}

	private void UsersDataGridView_SelectionChanged(object sender, EventArgs e)
	{
		if (UsersDataGridView.SelectedRows.Count > 0)
		{
			if (UsersDataGridView.SelectedRows[0].Cells[0].Value.ToString() != " ")
			{
				if (UsersDataGridView.SelectedRows[0].Tag != null)
				{
					using SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand();
					sQLiteCommand.CommandText = "SELECT * FROM instructors WHERE (id = @userID)";
					sQLiteCommand.Parameters.Add(new SQLiteParameter("@userID")
					{
						Value = UsersDataGridView.SelectedRows[0].Tag.ToString()
					});
					sQLiteCommand.CommandType = CommandType.Text;
					SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
					if (sQLiteDataReader.Read())
					{
						if (sQLiteDataReader["name"].ToString() == mainForm.userFIO)
						{
							UserDeleteButton.Enabled = false;
						}
						else
						{
							UserDeleteButton.Enabled = true;
						}
						userTextBox.Tag = sQLiteDataReader["name"].ToString();
						userTextBox.Text = sQLiteDataReader["name"].ToString();
						isAdminCheckBox.Checked = Convert.ToBoolean(Convert.ToInt32(sQLiteDataReader["is_admin"].ToString()));
						isAdminCheckBox.Tag = Convert.ToBoolean(Convert.ToInt32(sQLiteDataReader["is_admin"].ToString()));
						PasswordTextBox.Tag = "***";
						PasswordTextBox.Text = "***";
						UserSaveButton.Enabled = false;
						userTextBox.Enabled = true;
						PasswordTextBox.Enabled = true;
						userID = UsersDataGridView.SelectedRows[0].Tag.ToString();
					}
				}
			}
			else
			{
				userTextBox.Text = "";
				PasswordTextBox.Text = "";
				UserSaveButton.Enabled = false;
				userTextBox.Enabled = true;
				PasswordTextBox.Enabled = true;
			}
			UsersDataGridView.Tag = UsersDataGridView.SelectedRows[0].Index;
		}
		else
		{
			UserDeleteButton.Enabled = false;
		}
	}

	private void userTextBox_TextChanged(object sender, EventArgs e)
	{
		if (userTextBox.Text != "" && userTextBox.Text != userTextBox.Tag.ToString() && PasswordTextBox.Text != "" && userID != "")
		{
			UserSaveButton.Enabled = true;
		}
		else
		{
			UserSaveButton.Enabled = false;
		}
	}

	private void PasswordTextBox_TextChanged(object sender, EventArgs e)
	{
		if (PasswordTextBox.Text != "" && PasswordTextBox.Text != PasswordTextBox.Tag.ToString() && userTextBox.Text != "" && userID != "")
		{
			UserSaveButton.Enabled = true;
		}
		else
		{
			UserSaveButton.Enabled = false;
		}
	}

	private void UserSaveButton_Click(object sender, EventArgs e)
	{
		if ((!(userTextBox.Text != userTextBox.Tag.ToString()) && !(PasswordTextBox.Text != PasswordTextBox.Tag.ToString()) && !(isAdminCheckBox.Checked.ToString() != isAdminCheckBox.Tag.ToString())) || !(userTextBox.Text != "") || !(PasswordTextBox.Text != ""))
		{
			return;
		}
		try
		{
			if (UserAddButton.Enabled)
			{
				using (SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand())
				{
					string text = GenerateSalt();
					string text2 = "";
					text2 = ((!(PasswordTextBox.Text != "***")) ? "UPDATE instructors SET name = @userFIO, is_admin = @userIsAdmin WHERE (id = @userID)" : "UPDATE instructors SET name = @userFIO, is_admin = @userIsAdmin, salted_password_sha1 = @userPass, password_salt = @userSalt WHERE (id = @userID)");
					sQLiteCommand.CommandText = text2;
					sQLiteCommand.Parameters.Add(new SQLiteParameter("@userID")
					{
						Value = userID
					});
					sQLiteCommand.Parameters.Add(new SQLiteParameter("@userIsAdmin")
					{
						Value = isAdminCheckBox.Checked
					});
					sQLiteCommand.Parameters.Add(new SQLiteParameter("@userFIO")
					{
						Value = userTextBox.Text
					});
					if (PasswordTextBox.Text != "***")
					{
						sQLiteCommand.Parameters.Add(new SQLiteParameter("@userPass")
						{
							Value = SHA1HashString(PasswordTextBox.Text + text)
						});
						sQLiteCommand.Parameters.Add(new SQLiteParameter("@userSalt")
						{
							Value = text
						});
					}
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
				UserSaveButton.Enabled = false;
			}
			else
			{
				using (SQLiteCommand sQLiteCommand2 = mainForm.cnn.CreateCommand())
				{
					string text3 = GenerateSalt();
					sQLiteCommand2.CommandText = "INSERT INTO instructors (name, is_admin, salted_password_sha1, password_salt ) VALUES (@userFIO, @userIsAdmin, @userPass, @userSalt)";
					sQLiteCommand2.Parameters.Add(new SQLiteParameter("@userFIO")
					{
						Value = userTextBox.Text
					});
					sQLiteCommand2.Parameters.Add(new SQLiteParameter("@userIsAdmin")
					{
						Value = isAdminCheckBox.Checked
					});
					sQLiteCommand2.Parameters.Add(new SQLiteParameter("@userPass")
					{
						Value = SHA1HashString(PasswordTextBox.Text + text3)
					});
					sQLiteCommand2.Parameters.Add(new SQLiteParameter("@userSalt")
					{
						Value = text3
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
				UsersDataGridView.Enabled = true;
				UserAddButton.Enabled = true;
				UserSaveButton.Enabled = false;
			}
		}
		catch (Exception ex)
		{
			MessageBox.Show("Произошла ошибка : " + ex.Message);
		}
		int num3 = Convert.ToInt32(UsersDataGridView.Tag.ToString());
		refreshUsers();
		UsersDataGridView.FirstDisplayedScrollingRowIndex = num3;
		UsersDataGridView.Rows[num3].Selected = true;
		mainForm.RefreshUserComboBox();
	}

	private void UserAddButton_Click(object sender, EventArgs e)
	{
		UsersDataGridView.Rows.Add(" ");
		UsersDataGridView.FirstDisplayedScrollingRowIndex = UsersDataGridView.Rows.Count - 1;
		UsersDataGridView.Rows[UsersDataGridView.Rows.Count - 1].Selected = true;
		UsersDataGridView.Enabled = false;
		userID = "-1";
		UserAddButton.Enabled = false;
		UserDeleteButton.Enabled = true;
		userTextBox.Focus();
	}

	private void UserDeleteButton_Click(object sender, EventArgs e)
	{
		if (UserAddButton.Enabled)
		{
			if (MessageBox.Show("Вы хотите удалить пользователя ?", "Удаление пользователя", MessageBoxButtons.YesNo) != DialogResult.Yes)
			{
				return;
			}
			int num = 0;
			using (SQLiteCommand sQLiteCommand = mainForm.cnn.CreateCommand())
			{
				sQLiteCommand.CommandText = "SELECT COUNT(*) FROM student_groups WHERE instructor_id = @userID";
				sQLiteCommand.Parameters.Add(new SQLiteParameter("@userID")
				{
					Value = userID
				});
				sQLiteCommand.CommandType = CommandType.Text;
				SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
				if (sQLiteDataReader.Read())
				{
					num = Convert.ToInt32(sQLiteDataReader[0].ToString());
				}
			}
			if (num == 0)
			{
				using (SQLiteCommand sQLiteCommand2 = mainForm.cnn.CreateCommand())
				{
					sQLiteCommand2.CommandText = "DELETE FROM instructors WHERE id = @userID";
					sQLiteCommand2.Parameters.Add(new SQLiteParameter("@userID")
					{
						Value = userID
					});
					sQLiteCommand2.CommandType = CommandType.Text;
					int num2 = sQLiteCommand2.ExecuteNonQuery();
					if (num2 > 0)
					{
						MessageBox.Show("Пользователь удален !");
					}
					else
					{
						MessageBox.Show("Произошла ошибка. Данные не изменены !");
					}
					refreshUsers();
					mainForm.RefreshUserComboBox();
					userTextBox.Text = "";
					PasswordTextBox.Text = "";
					userTextBox.Enabled = false;
					PasswordTextBox.Enabled = false;
					UserSaveButton.Enabled = false;
					UsersDataGridView.ClearSelection();
					return;
				}
			}
			MessageBox.Show("Если Вам требуется удалить инструктора - предварительно укажите нового инструктора для всех групп, которые обучал удаляемый инструктор !");
		}
		else
		{
			UsersDataGridView.Rows.RemoveAt(UsersDataGridView.Rows.Count - 1);
			UsersDataGridView.FirstDisplayedScrollingRowIndex = 0;
			UsersDataGridView.Enabled = true;
			UserAddButton.Enabled = true;
			userTextBox.Text = "";
			PasswordTextBox.Text = "";
			userTextBox.Enabled = false;
			PasswordTextBox.Enabled = false;
			UserSaveButton.Enabled = false;
			UsersDataGridView.ClearSelection();
		}
	}

	private void PasswordTextBox_Click(object sender, EventArgs e)
	{
		PasswordTextBox.Text = "";
		PasswordTextBox.Focus();
	}

	private void isAdminCheckBox_CheckedChanged(object sender, EventArgs e)
	{
		if (isAdminCheckBox.Tag != null)
		{
			if (isAdminCheckBox.Checked.ToString() != isAdminCheckBox.Tag.ToString() && userTextBox.Text != "" && PasswordTextBox.Text != "")
			{
				UserSaveButton.Enabled = true;
			}
			else
			{
				UserSaveButton.Enabled = false;
			}
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
		System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle = new System.Windows.Forms.DataGridViewCellStyle();
		this.CloseButton = new System.Windows.Forms.Button();
		this.UsersDataGridView = new System.Windows.Forms.DataGridView();
		this.user = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.label1 = new System.Windows.Forms.Label();
		this.label2 = new System.Windows.Forms.Label();
		this.userTextBox = new System.Windows.Forms.TextBox();
		this.PasswordTextBox = new System.Windows.Forms.TextBox();
		this.UserDeleteButton = new System.Windows.Forms.Button();
		this.UserSaveButton = new System.Windows.Forms.Button();
		this.UserAddButton = new System.Windows.Forms.Button();
		this.isAdminCheckBox = new System.Windows.Forms.CheckBox();
		((System.ComponentModel.ISupportInitialize)this.UsersDataGridView).BeginInit();
		base.SuspendLayout();
		this.CloseButton.Location = new System.Drawing.Point(12, 362);
		this.CloseButton.Name = "CloseButton";
		this.CloseButton.Size = new System.Drawing.Size(282, 38);
		this.CloseButton.TabIndex = 0;
		this.CloseButton.Text = "Закрыть";
		this.CloseButton.UseVisualStyleBackColor = true;
		this.CloseButton.Click += new System.EventHandler(this.CloseButton_Click);
		this.UsersDataGridView.AllowUserToAddRows = false;
		this.UsersDataGridView.AllowUserToDeleteRows = false;
		this.UsersDataGridView.AllowUserToResizeColumns = false;
		this.UsersDataGridView.AllowUserToResizeRows = false;
		this.UsersDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
		dataGridViewCellStyle.Alignment = System.Windows.Forms.DataGridViewContentAlignment.MiddleCenter;
		dataGridViewCellStyle.BackColor = System.Drawing.SystemColors.Control;
		dataGridViewCellStyle.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25f, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, 204);
		dataGridViewCellStyle.ForeColor = System.Drawing.SystemColors.WindowText;
		dataGridViewCellStyle.SelectionBackColor = System.Drawing.SystemColors.Highlight;
		dataGridViewCellStyle.SelectionForeColor = System.Drawing.SystemColors.HighlightText;
		dataGridViewCellStyle.WrapMode = System.Windows.Forms.DataGridViewTriState.True;
		this.UsersDataGridView.ColumnHeadersDefaultCellStyle = dataGridViewCellStyle;
		this.UsersDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
		this.UsersDataGridView.Columns.AddRange(this.user);
		this.UsersDataGridView.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnKeystroke;
		this.UsersDataGridView.Location = new System.Drawing.Point(12, 12);
		this.UsersDataGridView.MultiSelect = false;
		this.UsersDataGridView.Name = "UsersDataGridView";
		this.UsersDataGridView.RowHeadersVisible = false;
		this.UsersDataGridView.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
		this.UsersDataGridView.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
		this.UsersDataGridView.ShowCellErrors = false;
		this.UsersDataGridView.ShowCellToolTips = false;
		this.UsersDataGridView.ShowEditingIcon = false;
		this.UsersDataGridView.ShowRowErrors = false;
		this.UsersDataGridView.Size = new System.Drawing.Size(282, 198);
		this.UsersDataGridView.StandardTab = true;
		this.UsersDataGridView.TabIndex = 1;
		this.UsersDataGridView.SelectionChanged += new System.EventHandler(this.UsersDataGridView_SelectionChanged);
		this.user.HeaderText = "Пользователь";
		this.user.Name = "user";
		this.user.ReadOnly = true;
		this.user.SortMode = System.Windows.Forms.DataGridViewColumnSortMode.NotSortable;
		this.label1.AutoSize = true;
		this.label1.Location = new System.Drawing.Point(9, 223);
		this.label1.Name = "label1";
		this.label1.Size = new System.Drawing.Size(86, 13);
		this.label1.TabIndex = 7;
		this.label1.Text = "Пользователь :";
		this.label2.AutoSize = true;
		this.label2.Location = new System.Drawing.Point(9, 250);
		this.label2.Name = "label2";
		this.label2.Size = new System.Drawing.Size(51, 13);
		this.label2.TabIndex = 8;
		this.label2.Text = "Пароль :";
		this.userTextBox.Location = new System.Drawing.Point(101, 220);
		this.userTextBox.Name = "userTextBox";
		this.userTextBox.Size = new System.Drawing.Size(193, 20);
		this.userTextBox.TabIndex = 2;
		this.userTextBox.TextChanged += new System.EventHandler(this.userTextBox_TextChanged);
		this.PasswordTextBox.Location = new System.Drawing.Point(101, 247);
		this.PasswordTextBox.Name = "PasswordTextBox";
		this.PasswordTextBox.Size = new System.Drawing.Size(193, 20);
		this.PasswordTextBox.TabIndex = 3;
		this.PasswordTextBox.Click += new System.EventHandler(this.PasswordTextBox_Click);
		this.PasswordTextBox.TextChanged += new System.EventHandler(this.PasswordTextBox_TextChanged);
		this.UserDeleteButton.Image = ELS.Properties.Resources.udalit1;
		this.UserDeleteButton.Location = new System.Drawing.Point(234, 296);
		this.UserDeleteButton.Name = "UserDeleteButton";
		this.UserDeleteButton.Size = new System.Drawing.Size(60, 60);
		this.UserDeleteButton.TabIndex = 6;
		this.UserDeleteButton.UseVisualStyleBackColor = true;
		this.UserDeleteButton.Click += new System.EventHandler(this.UserDeleteButton_Click);
		this.UserSaveButton.Image = ELS.Properties.Resources.sokhranit1;
		this.UserSaveButton.Location = new System.Drawing.Point(121, 296);
		this.UserSaveButton.Name = "UserSaveButton";
		this.UserSaveButton.Size = new System.Drawing.Size(60, 60);
		this.UserSaveButton.TabIndex = 5;
		this.UserSaveButton.UseVisualStyleBackColor = true;
		this.UserSaveButton.Click += new System.EventHandler(this.UserSaveButton_Click);
		this.UserAddButton.Image = ELS.Properties.Resources.dobavit1;
		this.UserAddButton.Location = new System.Drawing.Point(12, 296);
		this.UserAddButton.Name = "UserAddButton";
		this.UserAddButton.Size = new System.Drawing.Size(60, 60);
		this.UserAddButton.TabIndex = 4;
		this.UserAddButton.UseVisualStyleBackColor = true;
		this.UserAddButton.Click += new System.EventHandler(this.UserAddButton_Click);
		this.isAdminCheckBox.AutoSize = true;
		this.isAdminCheckBox.Location = new System.Drawing.Point(12, 273);
		this.isAdminCheckBox.Name = "isAdminCheckBox";
		this.isAdminCheckBox.Size = new System.Drawing.Size(114, 17);
		this.isAdminCheckBox.TabIndex = 9;
		this.isAdminCheckBox.Text = "Администратор ?";
		this.isAdminCheckBox.UseVisualStyleBackColor = true;
		this.isAdminCheckBox.CheckedChanged += new System.EventHandler(this.isAdminCheckBox_CheckedChanged);
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		base.ClientSize = new System.Drawing.Size(306, 409);
		base.Controls.Add(this.isAdminCheckBox);
		base.Controls.Add(this.UserDeleteButton);
		base.Controls.Add(this.UserSaveButton);
		base.Controls.Add(this.UserAddButton);
		base.Controls.Add(this.PasswordTextBox);
		base.Controls.Add(this.userTextBox);
		base.Controls.Add(this.label2);
		base.Controls.Add(this.label1);
		base.Controls.Add(this.UsersDataGridView);
		base.Controls.Add(this.CloseButton);
		base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
		base.MaximizeBox = false;
		base.MinimizeBox = false;
		base.Name = "InstructorsEditForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "Редактирование списка пользователей";
		base.Load += new System.EventHandler(this.InstructorsEditForm_Load);
		((System.ComponentModel.ISupportInitialize)this.UsersDataGridView).EndInit();
		base.ResumeLayout(false);
		base.PerformLayout();
	}
}
