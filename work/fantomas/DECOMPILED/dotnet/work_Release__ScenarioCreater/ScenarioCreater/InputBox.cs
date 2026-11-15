using System.Drawing;
using System.Windows.Forms;

namespace ScenarioCreater;

internal class InputBox
{
	private class InputBoxDialog : Form
	{
		private Label _lblPrompt;

		private TextBox _txtInput;

		private Button _btnOk;

		private Button _btnCancel;

		private bool _allowMinus = true;

		private bool _allowPoint = true;

		public string Value => _txtInput.Text;

		public InputBoxDialog(string prompt, string title, bool OnlyDigits, bool AllowMinus, bool AllowPoint, string defaultValue = null, int? xPos = null, int? yPos = null)
		{
			_allowMinus = AllowMinus;
			_allowPoint = AllowPoint;
			if (!xPos.HasValue && !yPos.HasValue)
			{
				StartPosition = FormStartPosition.CenterParent;
			}
			else
			{
				StartPosition = FormStartPosition.Manual;
				if (!xPos.HasValue)
				{
					xPos = Screen.PrimaryScreen.WorkingArea.Width - Width >> 1;
				}
				if (!yPos.HasValue)
				{
					yPos = Screen.PrimaryScreen.WorkingArea.Height - Height >> 1;
				}
				Location = new Point(xPos.Value, yPos.Value);
			}
			InitializeComponent();
			if (title == null)
			{
				title = Application.ProductName;
			}
			Text = title;
			_lblPrompt.Text = prompt;
			Graphics graphics = CreateGraphics();
			_lblPrompt.Size = graphics.MeasureString(prompt, _lblPrompt.Font).ToSize();
			int num = _lblPrompt.Size.Width;
			int num2 = _lblPrompt.Size.Height;
			_txtInput.Location = new Point(8, 30 + num2);
			int num3 = ((num < 206) ? 206 : num);
			_txtInput.Size = new Size(num3, 21);
			_txtInput.Text = defaultValue;
			_txtInput.SelectAll();
			_txtInput.Focus();
			if (OnlyDigits)
			{
				_txtInput.KeyPress += textBox_KeyPress;
			}
			Height = 135 + num2;
			Width = num3 + 34;
			_btnOk.Location = new Point(8, 60 + num2);
			_btnOk.Size = new Size(100, 26);
			_btnCancel.Location = new Point(114, 60 + num2);
			_btnCancel.Size = new Size(100, 26);
		}

		protected void textBox_KeyPress(object sender, KeyPressEventArgs e)
		{
			TextBox textBox = (TextBox)sender;
			if (_allowMinus && e.KeyChar.ToString().Equals("-"))
			{
				e.Handled = textBox.SelectionStart != 0 || textBox.Text.IndexOf("-") != -1;
				if (!e.Handled)
				{
					return;
				}
			}
			if (_allowPoint)
			{
				string value = ".";
				if (e.KeyChar.ToString().Equals(value))
				{
					e.Handled = textBox.Text.Length == 0 || textBox.Text.IndexOf(value) != -1;
					if (!e.Handled)
					{
						return;
					}
				}
			}
			e.Handled = !char.IsControl(e.KeyChar) && !char.IsDigit(e.KeyChar);
		}

		protected void InitializeComponent()
		{
			this._lblPrompt = new System.Windows.Forms.Label();
			this._lblPrompt.Location = new System.Drawing.Point(12, 9);
			this._lblPrompt.TabIndex = 0;
			this._lblPrompt.BackColor = System.Drawing.Color.Transparent;
			this._txtInput = new System.Windows.Forms.TextBox();
			this._txtInput.Size = new System.Drawing.Size(156, 20);
			this._txtInput.TabIndex = 1;
			this._btnOk = new System.Windows.Forms.Button();
			this._btnOk.TabIndex = 2;
			this._btnOk.Size = new System.Drawing.Size(75, 26);
			this._btnOk.Text = "&Ок";
			this._btnOk.DialogResult = System.Windows.Forms.DialogResult.OK;
			this._btnCancel = new System.Windows.Forms.Button();
			this._btnCancel.TabIndex = 3;
			this._btnCancel.Size = new System.Drawing.Size(75, 26);
			this._btnCancel.Text = "&Отмена";
			this._btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
			base.AcceptButton = this._btnOk;
			base.CancelButton = this._btnCancel;
			base.Controls.Add(this._lblPrompt);
			base.Controls.Add(this._txtInput);
			base.Controls.Add(this._btnOk);
			base.Controls.Add(this._btnCancel);
			base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
			base.MaximizeBox = false;
			base.MinimizeBox = false;
		}
	}

	public static string ShowDialog(string prompt, string title, bool OnlyDigits, bool AllowMinus, bool AllowPoint, string defaultValue = null, int? xPos = null, int? yPos = null)
	{
		InputBoxDialog inputBoxDialog = new InputBoxDialog(prompt, title, OnlyDigits, AllowMinus, AllowPoint, defaultValue, xPos, yPos);
		if (inputBoxDialog.ShowDialog() == DialogResult.Cancel)
		{
			return null;
		}
		return inputBoxDialog.Value;
	}
}
