using System;
using System.ComponentModel;
using System.Data;
using System.Data.SQLite;
using System.Drawing;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Windows.Forms;
using ELS.Properties;

namespace ELS;

public class MainForm : Form
{
	public SQLiteConnection cnn;

	public string connectionString = "";

	public string userFIO = "";

	public int userIsAdmin = 1000;

	public int userID = -1;

	public bool isLogin = false;

	private InstructorsEditForm IEForm;

	private ImportForm IForm;

	private IContainer components = null;

	private GroupBox groupBox1;

	private DataGridView groupDataGridView;

	private GroupBox groupBox2;

	private DataGridView traineesDataGridView;

	private Button GroupAddButton;

	private Button GroupChangeButton;

	private Button GroupDeleteButton;

	private Button TraineesDeleteButton;

	private Button TraineesChangeButton;

	private Button TraineesAddButton;

	private Label userLabel;

	private ComboBox UserComboBox;

	private Label passLabel;

	private TextBox PasswordTextBox;

	private Button LoginButton;

	private Button InstructorsEditButton;

	private DataGridViewTextBoxColumn nameColumn;

	private DataGridViewTextBoxColumn TraineeNameColumn;

	private Button ImportFromFileButton;

	public MainForm()
	{
		InitializeComponent();
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

	private int ExecuteQuery(string SQL)
	{
		using SQLiteCommand sQLiteCommand = cnn.CreateCommand();
		sQLiteCommand.CommandText = SQL;
		sQLiteCommand.CommandType = CommandType.Text;
		int num = sQLiteCommand.ExecuteNonQuery();
		if (num > 0)
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

	public void RefreshUserComboBox()
	{
		UserComboBox.Items.Clear();
		using SQLiteCommand sQLiteCommand = cnn.CreateCommand();
		sQLiteCommand.CommandText = "SELECT * FROM instructors";
		sQLiteCommand.CommandType = CommandType.Text;
		SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
		while (sQLiteDataReader.Read())
		{
			UserComboBox.Items.Add(sQLiteDataReader["name"].ToString());
		}
	}

	private void MainForm_Load(object sender, EventArgs e)
	{
		if (File.Exists(Application.StartupPath + "\\\\ELS.db"))
		{
			try
			{
				connectionString = "Data Source=" + Application.StartupPath + "\\\\ELS.db";
				cnn = new SQLiteConnection(connectionString);
				cnn.Open();
				ExecuteQuery("PRAGMA journal_mode=WAL");
				RefreshUserComboBox();
				ApplyRight();
				RefreshGroups();
				groupDataGridView.ClearSelection();
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

	public void RefreshGroups()
	{
		int num = -100;
		if (groupDataGridView.SelectedRows.Count > 0)
		{
			num = groupDataGridView.SelectedRows[0].Index;
		}
		groupDataGridView.Rows.Clear();
		using (SQLiteCommand sQLiteCommand = cnn.CreateCommand())
		{
			sQLiteCommand.CommandText = "SELECT * FROM student_groups";
			sQLiteCommand.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
			while (sQLiteDataReader.Read())
			{
				groupDataGridView.Rows.Add(sQLiteDataReader["name"].ToString());
				groupDataGridView.Rows[groupDataGridView.Rows.Count - 1].Tag = sQLiteDataReader["id"].ToString();
			}
		}
		groupDataGridView.ClearSelection();
		if (num != -100 && num < groupDataGridView.Rows.Count)
		{
			groupDataGridView.Rows[num].Selected = true;
			groupDataGridView_SelectionChanged(this, null);
		}
	}

	private void MainForm_FormClosed(object sender, FormClosedEventArgs e)
	{
		if (cnn != null && cnn.State == ConnectionState.Open)
		{
			cnn.Close();
		}
	}

	private void LoginButton_Click(object sender, EventArgs e)
	{
		try
		{
			if (LoginButton.Text == "Войти")
			{
				if (UserComboBox.SelectedIndex != -1 && PasswordTextBox.Text != "")
				{
					using (SQLiteCommand sQLiteCommand = cnn.CreateCommand())
					{
						string sQLValue = GetSQLValue("SELECT password_salt FROM instructors WHERE (name = '" + UserComboBox.Text + "')");
						sQLiteCommand.CommandText = "SELECT * FROM instructors WHERE (name = @userFIO) AND (salted_password_sha1 = @userPass)";
						sQLiteCommand.Parameters.Add(new SQLiteParameter("@userFIO")
						{
							Value = UserComboBox.Text
						});
						sQLiteCommand.Parameters.Add(new SQLiteParameter("@userPass")
						{
							Value = SHA1HashString(PasswordTextBox.Text + sQLValue)
						});
						sQLiteCommand.CommandType = CommandType.Text;
						SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
						if (sQLiteDataReader.Read())
						{
							LoginButton.Text = "Выйти";
							userLabel.Visible = false;
							passLabel.Visible = false;
							UserComboBox.Visible = false;
							PasswordTextBox.Visible = false;
							InstructorsEditButton.Visible = true;
							ImportFromFileButton.Visible = true;
							isLogin = true;
							userFIO = sQLiteDataReader["name"].ToString();
							userID = Convert.ToInt32(sQLiteDataReader["id"]);
							userIsAdmin = Convert.ToInt32(sQLiteDataReader["is_admin"]);
							if (userIsAdmin == 1)
							{
								ImportFromFileButton.Enabled = true;
							}
							else
							{
								ImportFromFileButton.Enabled = false;
							}
							if (groupDataGridView.SelectedRows.Count > 0)
							{
								int index = groupDataGridView.SelectedRows[0].Index;
								groupDataGridView.ClearSelection();
								groupDataGridView.Rows[index].Selected = true;
							}
							ApplyRight();
						}
						else
						{
							MessageBox.Show("Введен неверный пароль !");
						}
						return;
					}
				}
				MessageBox.Show("Не выбран пользователь или не введен пароль !");
			}
			else
			{
				if (IEForm != null)
				{
					IEForm.Close();
				}
				LoginButton.Text = "Войти";
				userLabel.Visible = true;
				passLabel.Visible = true;
				UserComboBox.Visible = true;
				PasswordTextBox.Visible = true;
				PasswordTextBox.Text = "";
				UserComboBox.SelectedIndex = -1;
				InstructorsEditButton.Visible = false;
				ImportFromFileButton.Visible = false;
				isLogin = false;
				userFIO = "";
				userID = -1;
				userIsAdmin = 1000;
				ApplyRight();
			}
		}
		catch (Exception ex)
		{
			MessageBox.Show(ex.Message);
		}
	}

	private void ApplyRight()
	{
		if (userIsAdmin == 0)
		{
			GroupAddButton.Enabled = false;
			GroupChangeButton.Enabled = false;
			GroupDeleteButton.Enabled = false;
			TraineesAddButton.Enabled = false;
			TraineesChangeButton.Enabled = false;
			TraineesDeleteButton.Enabled = false;
			InstructorsEditButton.Enabled = false;
		}
		if (userIsAdmin == 1)
		{
			GroupAddButton.Enabled = true;
			InstructorsEditButton.Enabled = true;
		}
	}

	private void InstructorsEditButton_Click(object sender, EventArgs e)
	{
		IEForm = new InstructorsEditForm(this);
		IEForm.Show();
	}

	private void GroupAddButton_Click(object sender, EventArgs e)
	{
		EditGroupsForm editGroupsForm = new EditGroupsForm(this);
		editGroupsForm.Show();
	}

	private void GroupChangeButton_Click(object sender, EventArgs e)
	{
		EditGroupsForm editGroupsForm = new EditGroupsForm(this, groupDataGridView.SelectedRows[0].Tag.ToString());
		editGroupsForm.Show();
	}

	private void groupDataGridView_SelectionChanged(object sender, EventArgs e)
	{
		GroupAddButton.Enabled = false;
		GroupChangeButton.Enabled = false;
		GroupDeleteButton.Enabled = false;
		if (userIsAdmin == 1)
		{
			GroupAddButton.Enabled = true;
			if (groupDataGridView.SelectedRows.Count > 0 && groupDataGridView.SelectedRows[0].Tag != null)
			{
				GroupChangeButton.Enabled = true;
				GroupDeleteButton.Enabled = true;
			}
		}
		if (groupDataGridView.SelectedRows.Count <= 0 || groupDataGridView.SelectedRows[0].Tag == null)
		{
			return;
		}
		try
		{
			traineesDataGridView.Rows.Clear();
			using (SQLiteCommand sQLiteCommand = cnn.CreateCommand())
			{
				sQLiteCommand.CommandText = "SELECT * FROM students WHERE group_id = @groupID";
				sQLiteCommand.Parameters.Add(new SQLiteParameter("@groupID")
				{
					Value = groupDataGridView.SelectedRows[0].Tag.ToString()
				});
				sQLiteCommand.CommandType = CommandType.Text;
				SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
				while (sQLiteDataReader.Read())
				{
					traineesDataGridView.Rows.Add(sQLiteDataReader["name"].ToString());
					traineesDataGridView.Rows[traineesDataGridView.Rows.Count - 1].Tag = sQLiteDataReader["id"].ToString();
				}
			}
			traineesDataGridView.ClearSelection();
		}
		catch (Exception ex)
		{
			MessageBox.Show("Ошибка удаления данных о поездках. " + ex.Message);
		}
		if (userIsAdmin == 1)
		{
			TraineesAddButton.Enabled = true;
		}
		else if (GetSQLValue("SELECT instructor_id FROM student_groups WHERE id = " + groupDataGridView.SelectedRows[0].Tag.ToString()) == userID.ToString())
		{
			TraineesAddButton.Enabled = true;
		}
		else
		{
			TraineesAddButton.Enabled = false;
		}
		TraineesChangeButton.Enabled = false;
		TraineesDeleteButton.Enabled = false;
	}

	private void GroupDeleteButton_Click(object sender, EventArgs e)
	{
		if (MessageBox.Show("Вы хотите удалить группу ? Все обучаемые этой группы, и все данные об их поездках будут удалены.", "Удаление группы", MessageBoxButtons.YesNo) != DialogResult.Yes)
		{
			return;
		}
		try
		{
			using SQLiteCommand sQLiteCommand = cnn.CreateCommand();
			sQLiteCommand.CommandText = "SELECT * FROM students WHERE group_id = @groupID";
			sQLiteCommand.Parameters.Add(new SQLiteParameter("@groupID")
			{
				Value = groupDataGridView.SelectedRows[0].Tag.ToString()
			});
			sQLiteCommand.CommandType = CommandType.Text;
			SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
			while (sQLiteDataReader.Read())
			{
				ExecuteQuery("DELETE FROM driving_sessions WHERE student_id = " + sQLiteDataReader["id"].ToString());
			}
		}
		catch (Exception ex)
		{
			MessageBox.Show("Ошибка удаления данных о поездках. " + ex.Message);
		}
		try
		{
			ExecuteQuery("DELETE FROM students WHERE group_id = " + groupDataGridView.SelectedRows[0].Tag.ToString());
		}
		catch (Exception ex2)
		{
			MessageBox.Show("Ошибка удаления обучаемых. " + ex2.Message);
		}
		try
		{
			using SQLiteCommand sQLiteCommand2 = cnn.CreateCommand();
			sQLiteCommand2.CommandText = "DELETE FROM student_groups WHERE id = @groupID";
			sQLiteCommand2.Parameters.Add(new SQLiteParameter("@groupID")
			{
				Value = groupDataGridView.SelectedRows[0].Tag.ToString()
			});
			sQLiteCommand2.CommandType = CommandType.Text;
			int num = sQLiteCommand2.ExecuteNonQuery();
			if (num > 0)
			{
				MessageBox.Show("Группа удалена !");
			}
			else
			{
				MessageBox.Show("Произошла ошибка. Данные не изменены !");
			}
		}
		catch (Exception ex3)
		{
			MessageBox.Show("Ошибка удаления группы. " + ex3.Message);
		}
		RefreshGroups();
		traineesDataGridView.Rows.Clear();
	}

	private void TraineesAddButton_Click(object sender, EventArgs e)
	{
		if (groupDataGridView.SelectedRows.Count > 0 && groupDataGridView.SelectedRows[0].Tag != null)
		{
			TraineesEditForm traineesEditForm = new TraineesEditForm(this, groupDataGridView.SelectedRows[0].Tag.ToString());
			traineesEditForm.Show();
		}
	}

	private void TraineesChangeButton_Click(object sender, EventArgs e)
	{
		if (groupDataGridView.SelectedRows.Count > 0 && groupDataGridView.SelectedRows[0].Tag != null && traineesDataGridView.SelectedRows.Count > 0 && traineesDataGridView.SelectedRows[0].Tag != null)
		{
			TraineesEditForm traineesEditForm = new TraineesEditForm(this, groupDataGridView.SelectedRows[0].Tag.ToString(), traineesDataGridView.SelectedRows[0].Tag.ToString());
			traineesEditForm.Show();
		}
	}

	private void traineesDataGridView_SelectionChanged(object sender, EventArgs e)
	{
		if (traineesDataGridView.SelectedRows.Count <= 0 || traineesDataGridView.SelectedRows[0].Tag == null)
		{
			return;
		}
		try
		{
			if (userIsAdmin == 1)
			{
				TraineesChangeButton.Enabled = true;
				TraineesDeleteButton.Enabled = true;
			}
			else if (GetSQLValue("SELECT instructor_id FROM student_groups WHERE id = " + groupDataGridView.SelectedRows[0].Tag.ToString()) == userID.ToString())
			{
				TraineesChangeButton.Enabled = true;
				TraineesDeleteButton.Enabled = true;
			}
			else
			{
				TraineesChangeButton.Enabled = false;
				TraineesDeleteButton.Enabled = false;
			}
		}
		catch (Exception ex)
		{
			MessageBox.Show("Ошибка удаления обучаемого. " + ex.Message);
		}
	}

	private void TraineesDeleteButton_Click(object sender, EventArgs e)
	{
		if (MessageBox.Show("Вы хотите удалить обучаемого ? Все данные об его поездках будут удалены.", "Удаление обучаемого", MessageBoxButtons.YesNo) == DialogResult.Yes)
		{
			try
			{
				ExecuteQuery("DELETE FROM driving_sessions WHERE student_id = " + traineesDataGridView.SelectedRows[0].Tag.ToString());
			}
			catch (Exception ex)
			{
				MessageBox.Show("Ошибка удаления данных о поездках. " + ex.Message);
			}
			try
			{
				ExecuteQuery("DELETE FROM students WHERE id = " + traineesDataGridView.SelectedRows[0].Tag.ToString());
				MessageBox.Show("Данные удалены !");
				RefreshGroups();
			}
			catch (Exception ex2)
			{
				MessageBox.Show("Ошибка удаления обучаемых. " + ex2.Message);
			}
		}
	}

	private void ImportFromFileButton_Click(object sender, EventArgs e)
	{
		if (userIsAdmin != 1)
		{
			return;
		}
		if (IForm == null || IForm.IsDisposed)
		{
			IForm = new ImportForm(this);
			IForm.Show();
			return;
		}
		if (!IForm.Visible)
		{
			IForm.Show();
		}
		IForm.Activate();
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
		System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle2 = new System.Windows.Forms.DataGridViewCellStyle();
		this.groupBox1 = new System.Windows.Forms.GroupBox();
		this.groupDataGridView = new System.Windows.Forms.DataGridView();
		this.nameColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.groupBox2 = new System.Windows.Forms.GroupBox();
		this.traineesDataGridView = new System.Windows.Forms.DataGridView();
		this.TraineeNameColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.userLabel = new System.Windows.Forms.Label();
		this.UserComboBox = new System.Windows.Forms.ComboBox();
		this.passLabel = new System.Windows.Forms.Label();
		this.PasswordTextBox = new System.Windows.Forms.TextBox();
		this.LoginButton = new System.Windows.Forms.Button();
		this.InstructorsEditButton = new System.Windows.Forms.Button();
		this.TraineesDeleteButton = new System.Windows.Forms.Button();
		this.TraineesChangeButton = new System.Windows.Forms.Button();
		this.TraineesAddButton = new System.Windows.Forms.Button();
		this.GroupDeleteButton = new System.Windows.Forms.Button();
		this.GroupChangeButton = new System.Windows.Forms.Button();
		this.GroupAddButton = new System.Windows.Forms.Button();
		this.ImportFromFileButton = new System.Windows.Forms.Button();
		this.groupBox1.SuspendLayout();
		((System.ComponentModel.ISupportInitialize)this.groupDataGridView).BeginInit();
		this.groupBox2.SuspendLayout();
		((System.ComponentModel.ISupportInitialize)this.traineesDataGridView).BeginInit();
		base.SuspendLayout();
		this.groupBox1.Controls.Add(this.groupDataGridView);
		this.groupBox1.Location = new System.Drawing.Point(4, 47);
		this.groupBox1.Name = "groupBox1";
		this.groupBox1.Size = new System.Drawing.Size(298, 404);
		this.groupBox1.TabIndex = 0;
		this.groupBox1.TabStop = false;
		this.groupBox1.Text = "Группы";
		this.groupDataGridView.AllowUserToAddRows = false;
		this.groupDataGridView.AllowUserToDeleteRows = false;
		this.groupDataGridView.AllowUserToResizeColumns = false;
		this.groupDataGridView.AllowUserToResizeRows = false;
		this.groupDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
		dataGridViewCellStyle.Alignment = System.Windows.Forms.DataGridViewContentAlignment.MiddleCenter;
		dataGridViewCellStyle.BackColor = System.Drawing.SystemColors.Control;
		dataGridViewCellStyle.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25f, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, 204);
		dataGridViewCellStyle.ForeColor = System.Drawing.SystemColors.WindowText;
		dataGridViewCellStyle.SelectionBackColor = System.Drawing.SystemColors.Highlight;
		dataGridViewCellStyle.SelectionForeColor = System.Drawing.SystemColors.HighlightText;
		dataGridViewCellStyle.WrapMode = System.Windows.Forms.DataGridViewTriState.True;
		this.groupDataGridView.ColumnHeadersDefaultCellStyle = dataGridViewCellStyle;
		this.groupDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
		this.groupDataGridView.Columns.AddRange(this.nameColumn);
		this.groupDataGridView.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnKeystroke;
		this.groupDataGridView.Location = new System.Drawing.Point(6, 19);
		this.groupDataGridView.MultiSelect = false;
		this.groupDataGridView.Name = "groupDataGridView";
		this.groupDataGridView.RowHeadersVisible = false;
		this.groupDataGridView.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
		this.groupDataGridView.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
		this.groupDataGridView.ShowCellErrors = false;
		this.groupDataGridView.ShowCellToolTips = false;
		this.groupDataGridView.ShowEditingIcon = false;
		this.groupDataGridView.ShowRowErrors = false;
		this.groupDataGridView.Size = new System.Drawing.Size(286, 379);
		this.groupDataGridView.StandardTab = true;
		this.groupDataGridView.TabIndex = 0;
		this.groupDataGridView.SelectionChanged += new System.EventHandler(this.groupDataGridView_SelectionChanged);
		this.nameColumn.HeaderText = "Название";
		this.nameColumn.Name = "nameColumn";
		this.nameColumn.ReadOnly = true;
		this.nameColumn.SortMode = System.Windows.Forms.DataGridViewColumnSortMode.NotSortable;
		this.groupBox2.Controls.Add(this.traineesDataGridView);
		this.groupBox2.Location = new System.Drawing.Point(320, 47);
		this.groupBox2.Name = "groupBox2";
		this.groupBox2.Size = new System.Drawing.Size(298, 404);
		this.groupBox2.TabIndex = 1;
		this.groupBox2.TabStop = false;
		this.groupBox2.Text = "Состав группы";
		this.traineesDataGridView.AllowUserToAddRows = false;
		this.traineesDataGridView.AllowUserToDeleteRows = false;
		this.traineesDataGridView.AllowUserToResizeColumns = false;
		this.traineesDataGridView.AllowUserToResizeRows = false;
		this.traineesDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
		dataGridViewCellStyle2.Alignment = System.Windows.Forms.DataGridViewContentAlignment.MiddleCenter;
		dataGridViewCellStyle2.BackColor = System.Drawing.SystemColors.Control;
		dataGridViewCellStyle2.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25f, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, 204);
		dataGridViewCellStyle2.ForeColor = System.Drawing.SystemColors.WindowText;
		dataGridViewCellStyle2.SelectionBackColor = System.Drawing.SystemColors.Highlight;
		dataGridViewCellStyle2.SelectionForeColor = System.Drawing.SystemColors.HighlightText;
		dataGridViewCellStyle2.WrapMode = System.Windows.Forms.DataGridViewTriState.True;
		this.traineesDataGridView.ColumnHeadersDefaultCellStyle = dataGridViewCellStyle2;
		this.traineesDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
		this.traineesDataGridView.Columns.AddRange(this.TraineeNameColumn);
		this.traineesDataGridView.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnKeystroke;
		this.traineesDataGridView.Location = new System.Drawing.Point(6, 19);
		this.traineesDataGridView.MultiSelect = false;
		this.traineesDataGridView.Name = "traineesDataGridView";
		this.traineesDataGridView.RowHeadersVisible = false;
		this.traineesDataGridView.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
		this.traineesDataGridView.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
		this.traineesDataGridView.ShowCellErrors = false;
		this.traineesDataGridView.ShowCellToolTips = false;
		this.traineesDataGridView.ShowEditingIcon = false;
		this.traineesDataGridView.ShowRowErrors = false;
		this.traineesDataGridView.Size = new System.Drawing.Size(286, 379);
		this.traineesDataGridView.StandardTab = true;
		this.traineesDataGridView.TabIndex = 1;
		this.traineesDataGridView.SelectionChanged += new System.EventHandler(this.traineesDataGridView_SelectionChanged);
		this.TraineeNameColumn.HeaderText = "ФИО";
		this.TraineeNameColumn.Name = "TraineeNameColumn";
		this.TraineeNameColumn.ReadOnly = true;
		this.TraineeNameColumn.SortMode = System.Windows.Forms.DataGridViewColumnSortMode.NotSortable;
		this.userLabel.AutoSize = true;
		this.userLabel.Location = new System.Drawing.Point(7, 18);
		this.userLabel.Name = "userLabel";
		this.userLabel.Size = new System.Drawing.Size(89, 13);
		this.userLabel.TabIndex = 8;
		this.userLabel.Text = "Пользователь : ";
		this.UserComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
		this.UserComboBox.FormattingEnabled = true;
		this.UserComboBox.Location = new System.Drawing.Point(102, 15);
		this.UserComboBox.Name = "UserComboBox";
		this.UserComboBox.Size = new System.Drawing.Size(200, 21);
		this.UserComboBox.TabIndex = 9;
		this.passLabel.AutoSize = true;
		this.passLabel.Location = new System.Drawing.Point(317, 18);
		this.passLabel.Name = "passLabel";
		this.passLabel.Size = new System.Drawing.Size(51, 13);
		this.passLabel.TabIndex = 10;
		this.passLabel.Text = "Пароль :";
		this.PasswordTextBox.Location = new System.Drawing.Point(374, 15);
		this.PasswordTextBox.Name = "PasswordTextBox";
		this.PasswordTextBox.Size = new System.Drawing.Size(121, 20);
		this.PasswordTextBox.TabIndex = 11;
		this.PasswordTextBox.UseSystemPasswordChar = true;
		this.LoginButton.Location = new System.Drawing.Point(501, 13);
		this.LoginButton.Name = "LoginButton";
		this.LoginButton.Size = new System.Drawing.Size(117, 23);
		this.LoginButton.TabIndex = 0;
		this.LoginButton.Text = "Войти";
		this.LoginButton.UseVisualStyleBackColor = true;
		this.LoginButton.Click += new System.EventHandler(this.LoginButton_Click);
		this.InstructorsEditButton.Location = new System.Drawing.Point(10, 13);
		this.InstructorsEditButton.Name = "InstructorsEditButton";
		this.InstructorsEditButton.Size = new System.Drawing.Size(292, 23);
		this.InstructorsEditButton.TabIndex = 13;
		this.InstructorsEditButton.Text = "Редактировать список инструкторов";
		this.InstructorsEditButton.UseVisualStyleBackColor = true;
		this.InstructorsEditButton.Visible = false;
		this.InstructorsEditButton.Click += new System.EventHandler(this.InstructorsEditButton_Click);
		this.TraineesDeleteButton.Image = ELS.Properties.Resources.udalit1;
		this.TraineesDeleteButton.Location = new System.Drawing.Point(558, 457);
		this.TraineesDeleteButton.Name = "TraineesDeleteButton";
		this.TraineesDeleteButton.Size = new System.Drawing.Size(60, 60);
		this.TraineesDeleteButton.TabIndex = 7;
		this.TraineesDeleteButton.UseVisualStyleBackColor = true;
		this.TraineesDeleteButton.Click += new System.EventHandler(this.TraineesDeleteButton_Click);
		this.TraineesChangeButton.Image = ELS.Properties.Resources.izmenit1;
		this.TraineesChangeButton.Location = new System.Drawing.Point(435, 457);
		this.TraineesChangeButton.Name = "TraineesChangeButton";
		this.TraineesChangeButton.Size = new System.Drawing.Size(60, 60);
		this.TraineesChangeButton.TabIndex = 6;
		this.TraineesChangeButton.UseVisualStyleBackColor = true;
		this.TraineesChangeButton.Click += new System.EventHandler(this.TraineesChangeButton_Click);
		this.TraineesAddButton.Image = ELS.Properties.Resources.dobavit1;
		this.TraineesAddButton.Location = new System.Drawing.Point(320, 457);
		this.TraineesAddButton.Name = "TraineesAddButton";
		this.TraineesAddButton.Size = new System.Drawing.Size(60, 60);
		this.TraineesAddButton.TabIndex = 5;
		this.TraineesAddButton.UseVisualStyleBackColor = true;
		this.TraineesAddButton.Click += new System.EventHandler(this.TraineesAddButton_Click);
		this.GroupDeleteButton.Image = ELS.Properties.Resources.udalit1;
		this.GroupDeleteButton.Location = new System.Drawing.Point(242, 457);
		this.GroupDeleteButton.Name = "GroupDeleteButton";
		this.GroupDeleteButton.Size = new System.Drawing.Size(60, 60);
		this.GroupDeleteButton.TabIndex = 4;
		this.GroupDeleteButton.UseVisualStyleBackColor = true;
		this.GroupDeleteButton.Click += new System.EventHandler(this.GroupDeleteButton_Click);
		this.GroupChangeButton.Image = ELS.Properties.Resources.izmenit1;
		this.GroupChangeButton.Location = new System.Drawing.Point(119, 457);
		this.GroupChangeButton.Name = "GroupChangeButton";
		this.GroupChangeButton.Size = new System.Drawing.Size(60, 60);
		this.GroupChangeButton.TabIndex = 3;
		this.GroupChangeButton.UseVisualStyleBackColor = true;
		this.GroupChangeButton.Click += new System.EventHandler(this.GroupChangeButton_Click);
		this.GroupAddButton.Image = ELS.Properties.Resources.dobavit1;
		this.GroupAddButton.Location = new System.Drawing.Point(4, 457);
		this.GroupAddButton.Name = "GroupAddButton";
		this.GroupAddButton.Size = new System.Drawing.Size(60, 60);
		this.GroupAddButton.TabIndex = 2;
		this.GroupAddButton.UseVisualStyleBackColor = true;
		this.GroupAddButton.Click += new System.EventHandler(this.GroupAddButton_Click);
		this.ImportFromFileButton.Location = new System.Drawing.Point(320, 13);
		this.ImportFromFileButton.Name = "ImportFromFileButton";
		this.ImportFromFileButton.Size = new System.Drawing.Size(175, 23);
		this.ImportFromFileButton.TabIndex = 14;
		this.ImportFromFileButton.Text = "Импорт данных из файла";
		this.ImportFromFileButton.UseVisualStyleBackColor = true;
		this.ImportFromFileButton.Visible = false;
		this.ImportFromFileButton.Click += new System.EventHandler(this.ImportFromFileButton_Click);
		base.AcceptButton = this.LoginButton;
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		base.ClientSize = new System.Drawing.Size(623, 529);
		base.Controls.Add(this.ImportFromFileButton);
		base.Controls.Add(this.LoginButton);
		base.Controls.Add(this.PasswordTextBox);
		base.Controls.Add(this.passLabel);
		base.Controls.Add(this.UserComboBox);
		base.Controls.Add(this.userLabel);
		base.Controls.Add(this.TraineesDeleteButton);
		base.Controls.Add(this.TraineesChangeButton);
		base.Controls.Add(this.TraineesAddButton);
		base.Controls.Add(this.GroupDeleteButton);
		base.Controls.Add(this.GroupChangeButton);
		base.Controls.Add(this.GroupAddButton);
		base.Controls.Add(this.groupBox2);
		base.Controls.Add(this.groupBox1);
		base.Controls.Add(this.InstructorsEditButton);
		base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
		base.MaximizeBox = false;
		base.Name = "MainForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "Редактирование групп";
		base.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.MainForm_FormClosed);
		base.Load += new System.EventHandler(this.MainForm_Load);
		this.groupBox1.ResumeLayout(false);
		((System.ComponentModel.ISupportInitialize)this.groupDataGridView).EndInit();
		this.groupBox2.ResumeLayout(false);
		((System.ComponentModel.ISupportInitialize)this.traineesDataGridView).EndInit();
		base.ResumeLayout(false);
		base.PerformLayout();
	}
}
