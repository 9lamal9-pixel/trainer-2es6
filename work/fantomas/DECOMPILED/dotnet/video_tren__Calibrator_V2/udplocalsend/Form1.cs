using System;
using System.ComponentModel;
using System.Drawing;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Windows.Forms;

namespace udplocalsend;

public class Form1 : Form
{
	private delegate void ShowMessageCallback(string message);

	private byte[] buf_tx = new byte[256];

	private ushort i;

	private Thread rec;

	private string message_new;

	private string message_new_1;

	private ushort tx_lenght;

	private ushort tx_buffer_lenght;

	private bool flag_checkBox1;

	private bool flag_checkBox2;

	private bool flag_checkBox3;

	private bool flag_checkBox4;

	private bool flag_checkBox5;

	private bool flag_checkBox6;

	private bool flag_checkBox7;

	private bool flag_checkBox8;

	private bool flag_checkBox9;

	private bool flag_checkBox10;

	private bool flag_checkBox11;

	private bool flag_checkBox12;

	private bool flag_checkBox13;

	private bool flag_checkBox14;

	private bool flag_checkBox15;

	private bool flag_checkBox16;

	private bool flag_checkBox17;

	private bool flag_checkBox18;

	private bool flag_checkBox19;

	private bool flag_checkBox20;

	private bool flag_checkBox21;

	private bool flag_checkBox22;

	private bool flag_checkBox23;

	private bool flag_checkBox24;

	private bool flag_checkBox25;

	private bool flag_checkBox26;

	private bool flag_checkBox27;

	private bool flag_checkBox28;

	private bool flag_checkBox29;

	private bool flag_checkBox30;

	private bool flag_checkBox31;

	private bool flag_checkBox32;

	private bool flag_checkBox33;

	private bool flag_checkBox34;

	private bool flag_checkBox35;

	private bool flag_checkBox36;

	private bool flag_checkBox37;

	private bool flag_checkBox38;

	private bool flag_checkBox39;

	private bool flag_checkBox40;

	private bool flag_checkBox41;

	private bool flag_checkBox42;

	private bool flag_checkBox43;

	private bool flag_checkBox44;

	private bool flag_checkBox45;

	private bool flag_checkBox46;

	private bool flag_checkBox47;

	private bool flag_checkBox48;

	private bool flag_checkBox49;

	private bool flag_checkBox50;

	private bool flag_checkBox51;

	private bool flag_checkBox52;

	private bool flag_checkBox53;

	private bool flag_checkBox54;

	private UdpClient udp = new UdpClient(2000);

	private IContainer components;

	private Label label3;

	private TextBox textBoxAddress;

	private System.Windows.Forms.Timer timer1;

	private TextBox textBox1;

	private Button button1;

	private TextBox textBox2;

	private Button button4;

	private Label label1;

	private NumericUpDown numericUpDown2;

	private NumericUpDown numericUpDown3;

	private NumericUpDown numericUpDown4;

	private Label label2;

	private NumericUpDown numericUpDown5;

	private NumericUpDown numericUpDown6;

	private Label label4;

	private NumericUpDown numericUpDown7;

	private NumericUpDown numericUpDown8;

	private Label label5;

	private NumericUpDown numericUpDown9;

	private NumericUpDown numericUpDown10;

	private Label label6;

	private NumericUpDown numericUpDown11;

	private NumericUpDown numericUpDown12;

	private Label label7;

	private NumericUpDown numericUpDown13;

	private NumericUpDown numericUpDown14;

	private Label label8;

	private NumericUpDown numericUpDown15;

	private NumericUpDown numericUpDown16;

	private Label label9;

	private NumericUpDown numericUpDown17;

	private NumericUpDown numericUpDown18;

	private Label label10;

	private NumericUpDown numericUpDown1;

	private NumericUpDown numericUpDown19;

	private NumericUpDown numericUpDown20;

	private Label label11;

	private NumericUpDown numericUpDown21;

	private NumericUpDown numericUpDown22;

	private Label label12;

	private NumericUpDown numericUpDown23;

	private NumericUpDown numericUpDown24;

	private Label label13;

	private NumericUpDown numericUpDown25;

	private NumericUpDown numericUpDown26;

	private Label label14;

	private NumericUpDown numericUpDown27;

	private NumericUpDown numericUpDown28;

	private Label label15;

	private NumericUpDown numericUpDown29;

	private NumericUpDown numericUpDown30;

	private Label label16;

	private NumericUpDown numericUpDown31;

	private NumericUpDown numericUpDown32;

	private Label label17;

	private NumericUpDown numericUpDown33;

	private NumericUpDown numericUpDown34;

	private Label label18;

	private NumericUpDown numericUpDown35;

	private NumericUpDown numericUpDown36;

	private Label label19;

	private NumericUpDown numericUpDown37;

	private NumericUpDown numericUpDown38;

	private Label label20;

	private NumericUpDown numericUpDown39;

	private NumericUpDown numericUpDown40;

	private Label label21;

	private NumericUpDown numericUpDown41;

	private NumericUpDown numericUpDown42;

	private Label label22;

	private NumericUpDown numericUpDown43;

	private NumericUpDown numericUpDown44;

	private Label label23;

	private NumericUpDown numericUpDown45;

	private NumericUpDown numericUpDown46;

	private Label label24;

	private NumericUpDown numericUpDown47;

	private NumericUpDown numericUpDown48;

	private Label label25;

	private NumericUpDown numericUpDown49;

	private NumericUpDown numericUpDown50;

	private Label label26;

	private NumericUpDown numericUpDown51;

	private NumericUpDown numericUpDown52;

	private Label label27;

	private NumericUpDown numericUpDown53;

	private NumericUpDown numericUpDown54;

	private Label label28;

	private CheckBox checkBox1;

	private CheckBox checkBox2;

	private CheckBox checkBox3;

	private CheckBox checkBox4;

	private CheckBox checkBox5;

	private CheckBox checkBox6;

	private CheckBox checkBox7;

	private CheckBox checkBox8;

	private CheckBox checkBox9;

	private CheckBox checkBox10;

	private CheckBox checkBox11;

	private CheckBox checkBox12;

	private CheckBox checkBox13;

	private CheckBox checkBox14;

	private CheckBox checkBox15;

	private CheckBox checkBox16;

	private CheckBox checkBox17;

	private CheckBox checkBox18;

	private CheckBox checkBox19;

	private CheckBox checkBox20;

	private CheckBox checkBox21;

	private CheckBox checkBox23;

	private CheckBox checkBox24;

	private CheckBox checkBox25;

	private CheckBox checkBox26;

	private CheckBox checkBox27;

	private CheckBox checkBox28;

	private TextBox textBox3;

	private TextBox textBox4;

	private TextBox textBox5;

	private TextBox textBox6;

	private TextBox textBox7;

	private TextBox textBox8;

	private TextBox textBox9;

	private TextBox textBox10;

	private TextBox textBox11;

	private TextBox textBox12;

	private TextBox textBox13;

	private TextBox textBox14;

	private TextBox textBox15;

	private TextBox textBox16;

	private TextBox textBox17;

	private TextBox textBox18;

	private TextBox textBox19;

	private TextBox textBox20;

	private TextBox textBox21;

	private TextBox textBox22;

	private TextBox textBox23;

	private TextBox textBox24;

	private TextBox textBox25;

	private TextBox textBox26;

	private TextBox textBox32;

	private TextBox textBox33;

	private TextBox textBox34;

	private TextBox textBox27;

	private TextBox textBox28;

	private TextBox textBox29;

	private TextBox textBox30;

	private TextBox textBox31;

	private TextBox textBox35;

	private TextBox textBox36;

	private TextBox textBox37;

	private TextBox textBox38;

	private TextBox textBox39;

	private TextBox textBox40;

	private TextBox textBox41;

	private TextBox textBox42;

	private TextBox textBox43;

	private TextBox textBox44;

	private TextBox textBox45;

	private TextBox textBox46;

	private TextBox textBox47;

	private TextBox textBox48;

	private TextBox textBox49;

	private TextBox textBox50;

	private TextBox textBox51;

	private TextBox textBox52;

	private TextBox textBox53;

	private TextBox textBox54;

	private TextBox textBox55;

	private TextBox textBox56;

	private CheckBox checkBox22;

	private CheckBox checkBox29;

	private CheckBox checkBox30;

	private CheckBox checkBox31;

	private CheckBox checkBox32;

	private CheckBox checkBox33;

	private CheckBox checkBox34;

	private CheckBox checkBox35;

	private CheckBox checkBox36;

	private CheckBox checkBox37;

	private CheckBox checkBox38;

	private CheckBox checkBox39;

	private CheckBox checkBox40;

	private CheckBox checkBox41;

	private CheckBox checkBox42;

	private CheckBox checkBox43;

	private CheckBox checkBox44;

	private CheckBox checkBox45;

	private CheckBox checkBox46;

	private CheckBox checkBox47;

	private CheckBox checkBox48;

	private CheckBox checkBox49;

	private CheckBox checkBox50;

	private CheckBox checkBox51;

	private CheckBox checkBox52;

	private CheckBox checkBox53;

	private CheckBox checkBox54;

	private NumericUpDown numericUpDown55;

	private NumericUpDown numericUpDown56;

	private Label label29;

	private NumericUpDown numericUpDown57;

	private NumericUpDown numericUpDown58;

	private Label label30;

	private NumericUpDown numericUpDown59;

	private NumericUpDown numericUpDown60;

	private Label label31;

	private NumericUpDown numericUpDown61;

	private NumericUpDown numericUpDown62;

	private Label label32;

	private NumericUpDown numericUpDown63;

	private NumericUpDown numericUpDown64;

	private Label label33;

	private NumericUpDown numericUpDown65;

	private NumericUpDown numericUpDown66;

	private Label label34;

	private NumericUpDown numericUpDown67;

	private NumericUpDown numericUpDown68;

	private Label label35;

	private NumericUpDown numericUpDown69;

	private NumericUpDown numericUpDown70;

	private Label label36;

	private NumericUpDown numericUpDown71;

	private NumericUpDown numericUpDown72;

	private Label label37;

	private NumericUpDown numericUpDown73;

	private NumericUpDown numericUpDown74;

	private Label label38;

	private NumericUpDown numericUpDown75;

	private NumericUpDown numericUpDown76;

	private Label label39;

	private NumericUpDown numericUpDown77;

	private NumericUpDown numericUpDown78;

	private Label label40;

	private NumericUpDown numericUpDown79;

	private NumericUpDown numericUpDown80;

	private Label label41;

	private NumericUpDown numericUpDown81;

	private NumericUpDown numericUpDown82;

	private Label label42;

	private NumericUpDown numericUpDown83;

	private NumericUpDown numericUpDown84;

	private Label label43;

	private NumericUpDown numericUpDown85;

	private NumericUpDown numericUpDown86;

	private Label label44;

	private NumericUpDown numericUpDown87;

	private NumericUpDown numericUpDown88;

	private Label label45;

	private NumericUpDown numericUpDown89;

	private NumericUpDown numericUpDown90;

	private Label label46;

	private NumericUpDown numericUpDown91;

	private NumericUpDown numericUpDown92;

	private Label label47;

	private NumericUpDown numericUpDown93;

	private NumericUpDown numericUpDown94;

	private Label label48;

	private NumericUpDown numericUpDown95;

	private NumericUpDown numericUpDown96;

	private Label label49;

	private NumericUpDown numericUpDown97;

	private NumericUpDown numericUpDown98;

	private Label label50;

	private NumericUpDown numericUpDown99;

	private NumericUpDown numericUpDown100;

	private Label label51;

	private NumericUpDown numericUpDown101;

	private NumericUpDown numericUpDown102;

	private Label label52;

	private NumericUpDown numericUpDown103;

	private NumericUpDown numericUpDown104;

	private Label label53;

	private NumericUpDown numericUpDown105;

	private NumericUpDown numericUpDown106;

	private Label label54;

	private NumericUpDown numericUpDown107;

	private NumericUpDown numericUpDown108;

	private Label label55;

	private System.Windows.Forms.Timer timer2;

	private Label label56;

	private Label label57;

	private NumericUpDown numericUpDown109;

	public Form1()
	{
		InitializeComponent();
		rec = new Thread(Receive);
		rec.Start();
	}

	private void button1_Click_1(object sender, EventArgs e)
	{
		IPEndPoint endPoint = new IPEndPoint(IPAddress.Parse(textBoxAddress.Text), 2000);
		udp.Send(buf_tx, tx_buffer_lenght, endPoint);
		i = 0;
		for (message_new_1 = ""; i < tx_buffer_lenght; i++)
		{
			message_new_1 = message_new_1 + "*" + i + "-" + Convert.ToString(Convert.ToUInt16(buf_tx[i])) + " |";
		}
		textBox1.Text = message_new_1;
	}

	private void timer1_Tick(object sender, EventArgs e)
	{
		button1.PerformClick();
	}

	private void Receive()
	{
		try
		{
			while (true)
			{
				IPEndPoint remoteEP = null;
				byte[] array = udp.Receive(ref remoteEP);
				i = 0;
				for (message_new = ""; i < array.Length; i++)
				{
					message_new = message_new + "*" + i + "-" + Convert.ToString(Convert.ToUInt16(array[i])) + " |";
				}
				ShowMessage(message_new);
			}
		}
		catch (Exception)
		{
		}
	}

	private void ShowMessage(string message)
	{
		if (textBox2.InvokeRequired)
		{
			ShowMessageCallback method = ShowMessage;
			Invoke(method, message);
		}
		else
		{
			textBox2.Text = message;
		}
	}

	private ushort HexCharToUInt16(char a)
	{
		ushort num = 0;
		return a switch
		{
			'0' => (ushort)0, 
			'1' => (ushort)1, 
			'2' => (ushort)2, 
			'3' => (ushort)3, 
			'4' => (ushort)4, 
			'5' => (ushort)5, 
			'6' => (ushort)6, 
			'7' => (ushort)7, 
			'8' => (ushort)8, 
			'9' => (ushort)9, 
			'A' => (ushort)10, 
			'B' => (ushort)11, 
			'C' => (ushort)12, 
			'D' => (ushort)13, 
			'E' => (ushort)14, 
			'F' => (ushort)15, 
			'a' => (ushort)10, 
			'b' => (ushort)11, 
			'c' => (ushort)12, 
			'd' => (ushort)13, 
			'e' => (ushort)14, 
			'f' => (ushort)15, 
			_ => (ushort)0, 
		};
	}

	private void button4_Click(object sender, EventArgs e)
	{
		if (!timer1.Enabled)
		{
			timer1.Enabled = true;
			button4.Text = "Откл.Таймер";
		}
		else
		{
			timer1.Enabled = false;
			button4.Text = "Вкл.Таймер";
		}
	}

	private void timer2_Tick(object sender, EventArgs e)
	{
		tx_lenght = 0;
		if (numericUpDown2.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox1)
			{
				flag_checkBox1 = false;
			}
			if (tx_lenght < 10)
			{
				label2.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label2.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown1.Maximum = 999m;
			checkBox1.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown1.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown1.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label2.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label2.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown1.Maximum = 255m;
			if (flag_checkBox1)
			{
				if (checkBox1.Checked)
				{
					numericUpDown1.Value = 1m;
				}
				else
				{
					numericUpDown1.Value = 0m;
				}
				flag_checkBox1 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown1.Value);
		}
		if (numericUpDown3.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox2)
			{
				flag_checkBox2 = false;
			}
			if (tx_lenght < 10)
			{
				label4.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label4.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown4.Maximum = 999m;
			checkBox2.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown4.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown4.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label4.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label4.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown4.Maximum = 255m;
			if (flag_checkBox2)
			{
				if (checkBox2.Checked)
				{
					numericUpDown4.Value = 1m;
				}
				else
				{
					numericUpDown4.Value = 0m;
				}
				flag_checkBox2 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown4.Value);
		}
		if (numericUpDown5.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox3)
			{
				flag_checkBox3 = false;
			}
			if (tx_lenght < 10)
			{
				label5.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label5.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown6.Maximum = 999m;
			checkBox3.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown6.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown6.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label5.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label5.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown6.Maximum = 255m;
			if (flag_checkBox3)
			{
				if (checkBox3.Checked)
				{
					numericUpDown6.Value = 1m;
				}
				else
				{
					numericUpDown6.Value = 0m;
				}
				flag_checkBox3 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown6.Value);
		}
		if (numericUpDown7.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox4)
			{
				flag_checkBox4 = false;
			}
			if (tx_lenght < 10)
			{
				label6.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label6.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown8.Maximum = 999m;
			checkBox4.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown8.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown8.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label6.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label6.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown8.Maximum = 255m;
			if (flag_checkBox4)
			{
				if (checkBox4.Checked)
				{
					numericUpDown8.Value = 1m;
				}
				else
				{
					numericUpDown8.Value = 0m;
				}
				flag_checkBox4 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown8.Value);
		}
		if (numericUpDown9.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox5)
			{
				flag_checkBox5 = false;
			}
			if (tx_lenght < 10)
			{
				label7.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label7.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown10.Maximum = 999m;
			checkBox5.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown10.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown10.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label7.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label7.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown10.Maximum = 255m;
			if (flag_checkBox5)
			{
				if (checkBox5.Checked)
				{
					numericUpDown10.Value = 1m;
				}
				else
				{
					numericUpDown10.Value = 0m;
				}
				flag_checkBox5 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown10.Value);
		}
		if (numericUpDown11.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox6)
			{
				flag_checkBox6 = false;
			}
			if (tx_lenght < 10)
			{
				label8.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label8.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown12.Maximum = 999m;
			checkBox6.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown12.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown12.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label8.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label8.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown12.Maximum = 255m;
			if (flag_checkBox6)
			{
				if (checkBox6.Checked)
				{
					numericUpDown12.Value = 1m;
				}
				else
				{
					numericUpDown12.Value = 0m;
				}
				flag_checkBox6 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown12.Value);
		}
		if (numericUpDown13.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox7)
			{
				flag_checkBox7 = false;
			}
			if (tx_lenght < 10)
			{
				label9.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label9.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown14.Maximum = 999m;
			checkBox7.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown14.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown14.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label9.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label9.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown14.Maximum = 255m;
			if (flag_checkBox7)
			{
				if (checkBox7.Checked)
				{
					numericUpDown14.Value = 1m;
				}
				else
				{
					numericUpDown14.Value = 0m;
				}
				flag_checkBox7 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown14.Value);
		}
		if (numericUpDown15.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox14)
			{
				flag_checkBox14 = false;
			}
			if (tx_lenght < 10)
			{
				label10.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label10.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown16.Maximum = 999m;
			checkBox14.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown16.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown16.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label10.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label10.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown16.Maximum = 255m;
			if (flag_checkBox14)
			{
				if (checkBox14.Checked)
				{
					numericUpDown16.Value = 1m;
				}
				else
				{
					numericUpDown16.Value = 0m;
				}
				flag_checkBox14 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown16.Value);
		}
		if (numericUpDown17.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox13)
			{
				flag_checkBox13 = false;
			}
			if (tx_lenght < 10)
			{
				label19.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label19.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown18.Maximum = 999m;
			checkBox13.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown18.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown18.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label19.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label19.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown18.Maximum = 255m;
			if (flag_checkBox13)
			{
				if (checkBox13.Checked)
				{
					numericUpDown18.Value = 1m;
				}
				else
				{
					numericUpDown18.Value = 0m;
				}
				flag_checkBox13 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown18.Value);
		}
		if (numericUpDown35.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox12)
			{
				flag_checkBox12 = false;
			}
			if (tx_lenght < 10)
			{
				label18.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label18.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown36.Maximum = 999m;
			checkBox12.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown36.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown36.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label18.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label18.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown36.Maximum = 255m;
			if (flag_checkBox12)
			{
				if (checkBox12.Checked)
				{
					numericUpDown36.Value = 1m;
				}
				else
				{
					numericUpDown36.Value = 0m;
				}
				flag_checkBox12 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown36.Value);
		}
		if (numericUpDown33.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox11)
			{
				flag_checkBox11 = false;
			}
			if (tx_lenght < 10)
			{
				label17.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label17.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown34.Maximum = 999m;
			checkBox11.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown34.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown34.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label17.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label17.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown34.Maximum = 255m;
			if (flag_checkBox11)
			{
				if (checkBox11.Checked)
				{
					numericUpDown34.Value = 1m;
				}
				else
				{
					numericUpDown34.Value = 0m;
				}
				flag_checkBox11 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown34.Value);
		}
		if (numericUpDown31.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox10)
			{
				flag_checkBox10 = false;
			}
			if (tx_lenght < 10)
			{
				label16.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label16.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown32.Maximum = 999m;
			checkBox10.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown32.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown32.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label16.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label16.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown32.Maximum = 255m;
			if (flag_checkBox10)
			{
				if (checkBox10.Checked)
				{
					numericUpDown32.Value = 1m;
				}
				else
				{
					numericUpDown32.Value = 0m;
				}
				flag_checkBox10 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown32.Value);
		}
		if (numericUpDown29.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox9)
			{
				flag_checkBox9 = false;
			}
			if (tx_lenght < 10)
			{
				label15.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label15.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown30.Maximum = 999m;
			checkBox9.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown30.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown30.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label15.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label15.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown30.Maximum = 255m;
			if (flag_checkBox9)
			{
				if (checkBox9.Checked)
				{
					numericUpDown30.Value = 1m;
				}
				else
				{
					numericUpDown30.Value = 0m;
				}
				flag_checkBox9 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown30.Value);
		}
		if (numericUpDown27.Value == 2m)
		{
			tx_lenght += 2;
			if (flag_checkBox8)
			{
				flag_checkBox8 = false;
			}
			if (tx_lenght < 10)
			{
				label14.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label14.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown28.Maximum = 999m;
			checkBox8.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown28.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown28.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			if (tx_lenght < 10)
			{
				label14.Text = "№0" + Convert.ToString(tx_lenght);
			}
			else
			{
				label14.Text = "№" + Convert.ToString(tx_lenght);
			}
			numericUpDown28.Maximum = 255m;
			if (flag_checkBox8)
			{
				if (checkBox8.Checked)
				{
					numericUpDown28.Value = 1m;
				}
				else
				{
					numericUpDown28.Value = 0m;
				}
				flag_checkBox8 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown28.Value);
		}
		if (numericUpDown25.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox21 = false;
			label13.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown26.Maximum = 999m;
			checkBox21.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown26.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown26.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label13.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown26.Maximum = 255m;
			if (flag_checkBox21)
			{
				if (checkBox21.Checked)
				{
					numericUpDown26.Value = 1m;
				}
				else
				{
					numericUpDown26.Value = 0m;
				}
				flag_checkBox21 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown26.Value);
		}
		if (numericUpDown23.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox20 = false;
			label12.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown24.Maximum = 999m;
			checkBox20.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown24.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown24.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label12.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown24.Maximum = 255m;
			if (flag_checkBox20)
			{
				if (checkBox20.Checked)
				{
					numericUpDown24.Value = 1m;
				}
				else
				{
					numericUpDown24.Value = 0m;
				}
				flag_checkBox20 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown24.Value);
		}
		if (numericUpDown21.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox19 = false;
			label11.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown22.Maximum = 999m;
			checkBox19.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown22.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown22.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label11.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown22.Maximum = 255m;
			if (flag_checkBox19)
			{
				if (checkBox19.Checked)
				{
					numericUpDown22.Value = 1m;
				}
				else
				{
					numericUpDown22.Value = 0m;
				}
				flag_checkBox19 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown22.Value);
		}
		if (numericUpDown19.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox18 = false;
			label28.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown20.Maximum = 999m;
			checkBox18.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown20.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown20.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label28.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown20.Maximum = 255m;
			if (flag_checkBox18)
			{
				if (checkBox18.Checked)
				{
					numericUpDown20.Value = 1m;
				}
				else
				{
					numericUpDown20.Value = 0m;
				}
				flag_checkBox18 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown20.Value);
		}
		if (numericUpDown53.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox17 = false;
			label27.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown54.Maximum = 999m;
			checkBox17.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown54.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown54.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label27.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown54.Maximum = 255m;
			if (flag_checkBox17)
			{
				if (checkBox17.Checked)
				{
					numericUpDown54.Value = 1m;
				}
				else
				{
					numericUpDown54.Value = 0m;
				}
				flag_checkBox17 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown54.Value);
		}
		if (numericUpDown51.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox16 = false;
			label26.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown52.Maximum = 999m;
			checkBox16.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown52.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown52.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label26.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown52.Maximum = 255m;
			if (flag_checkBox16)
			{
				if (checkBox16.Checked)
				{
					numericUpDown52.Value = 1m;
				}
				else
				{
					numericUpDown52.Value = 0m;
				}
				flag_checkBox16 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown52.Value);
		}
		if (numericUpDown49.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox15 = false;
			label25.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown50.Maximum = 999m;
			checkBox15.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown50.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown50.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label25.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown50.Maximum = 255m;
			if (flag_checkBox15)
			{
				if (checkBox15.Checked)
				{
					numericUpDown50.Value = 1m;
				}
				else
				{
					numericUpDown50.Value = 0m;
				}
				flag_checkBox15 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown50.Value);
		}
		if (numericUpDown47.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox28 = false;
			label24.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown48.Maximum = 999m;
			checkBox28.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown48.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown48.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label24.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown48.Maximum = 255m;
			if (flag_checkBox28)
			{
				if (checkBox28.Checked)
				{
					numericUpDown48.Value = 1m;
				}
				else
				{
					numericUpDown48.Value = 0m;
				}
				flag_checkBox28 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown48.Value);
		}
		if (numericUpDown45.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox27 = false;
			label23.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown46.Maximum = 999m;
			checkBox27.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown46.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown46.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label23.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown46.Maximum = 255m;
			if (flag_checkBox27)
			{
				if (checkBox27.Checked)
				{
					numericUpDown46.Value = 1m;
				}
				else
				{
					numericUpDown46.Value = 0m;
				}
				flag_checkBox27 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown46.Value);
		}
		if (numericUpDown43.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox26 = false;
			label22.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown44.Maximum = 999m;
			checkBox26.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown44.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown44.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label22.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown44.Maximum = 255m;
			if (flag_checkBox26)
			{
				if (checkBox26.Checked)
				{
					numericUpDown44.Value = 1m;
				}
				else
				{
					numericUpDown44.Value = 0m;
				}
				flag_checkBox26 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown44.Value);
		}
		if (numericUpDown41.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox25 = false;
			label21.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown42.Maximum = 999m;
			checkBox25.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown42.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown42.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label21.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown42.Maximum = 255m;
			if (flag_checkBox25)
			{
				if (checkBox25.Checked)
				{
					numericUpDown42.Value = 1m;
				}
				else
				{
					numericUpDown42.Value = 0m;
				}
				flag_checkBox25 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown42.Value);
		}
		if (numericUpDown39.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox24 = false;
			label20.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown40.Maximum = 999m;
			checkBox24.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown40.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown40.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label20.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown40.Maximum = 255m;
			if (flag_checkBox24)
			{
				if (checkBox24.Checked)
				{
					numericUpDown40.Value = 1m;
				}
				else
				{
					numericUpDown40.Value = 0m;
				}
				flag_checkBox24 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown40.Value);
		}
		if (numericUpDown37.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox23 = false;
			label55.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown38.Maximum = 999m;
			checkBox23.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown38.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown38.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label55.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown38.Maximum = 255m;
			if (flag_checkBox23)
			{
				if (checkBox23.Checked)
				{
					numericUpDown38.Value = 1m;
				}
				else
				{
					numericUpDown38.Value = 0m;
				}
				flag_checkBox23 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown38.Value);
		}
		if (numericUpDown107.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox54 = false;
			label54.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown108.Maximum = 999m;
			checkBox54.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown108.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown108.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label54.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown108.Maximum = 255m;
			if (flag_checkBox54)
			{
				if (checkBox54.Checked)
				{
					numericUpDown108.Value = 1m;
				}
				else
				{
					numericUpDown108.Value = 0m;
				}
				flag_checkBox54 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown108.Value);
		}
		if (numericUpDown105.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox53 = false;
			label53.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown106.Maximum = 999m;
			checkBox53.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown106.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown106.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label53.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown106.Maximum = 255m;
			if (flag_checkBox53)
			{
				if (checkBox53.Checked)
				{
					numericUpDown106.Value = 1m;
				}
				else
				{
					numericUpDown106.Value = 0m;
				}
				flag_checkBox53 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown106.Value);
		}
		if (numericUpDown103.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox52 = false;
			label52.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown104.Maximum = 999m;
			checkBox52.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown104.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown104.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label52.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown104.Maximum = 255m;
			if (flag_checkBox52)
			{
				if (checkBox52.Checked)
				{
					numericUpDown104.Value = 1m;
				}
				else
				{
					numericUpDown104.Value = 0m;
				}
				flag_checkBox52 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown104.Value);
		}
		if (numericUpDown101.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox51 = false;
			label51.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown102.Maximum = 999m;
			checkBox51.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown102.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown102.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label51.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown102.Maximum = 255m;
			if (flag_checkBox51)
			{
				if (checkBox51.Checked)
				{
					numericUpDown102.Value = 1m;
				}
				else
				{
					numericUpDown102.Value = 0m;
				}
				flag_checkBox51 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown102.Value);
		}
		if (numericUpDown99.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox50 = false;
			label50.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown100.Maximum = 999m;
			checkBox50.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown100.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown100.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label50.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown100.Maximum = 255m;
			if (flag_checkBox50)
			{
				if (checkBox50.Checked)
				{
					numericUpDown100.Value = 1m;
				}
				else
				{
					numericUpDown100.Value = 0m;
				}
				flag_checkBox50 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown100.Value);
		}
		if (numericUpDown97.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox49 = false;
			label49.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown98.Maximum = 999m;
			checkBox49.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown98.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown98.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label49.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown98.Maximum = 255m;
			if (flag_checkBox49)
			{
				if (checkBox49.Checked)
				{
					numericUpDown98.Value = 1m;
				}
				else
				{
					numericUpDown98.Value = 0m;
				}
				flag_checkBox49 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown98.Value);
		}
		if (numericUpDown95.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox48 = false;
			label48.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown96.Maximum = 999m;
			checkBox48.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown96.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown96.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label48.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown96.Maximum = 255m;
			if (flag_checkBox48)
			{
				if (checkBox48.Checked)
				{
					numericUpDown96.Value = 1m;
				}
				else
				{
					numericUpDown96.Value = 0m;
				}
				flag_checkBox48 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown96.Value);
		}
		if (numericUpDown91.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox46 = false;
			label46.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown92.Maximum = 999m;
			checkBox46.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown92.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown92.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label46.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown92.Maximum = 255m;
			if (flag_checkBox46)
			{
				if (checkBox46.Checked)
				{
					numericUpDown92.Value = 1m;
				}
				else
				{
					numericUpDown92.Value = 0m;
				}
				flag_checkBox46 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown92.Value);
		}
		if (numericUpDown89.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox45 = false;
			label45.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown90.Maximum = 999m;
			checkBox45.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown90.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown90.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label45.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown90.Maximum = 255m;
			if (flag_checkBox45)
			{
				if (checkBox45.Checked)
				{
					numericUpDown90.Value = 1m;
				}
				else
				{
					numericUpDown90.Value = 0m;
				}
				flag_checkBox45 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown90.Value);
		}
		if (numericUpDown87.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox44 = false;
			label44.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown88.Maximum = 999m;
			checkBox44.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown88.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown88.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label44.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown88.Maximum = 255m;
			if (flag_checkBox44)
			{
				if (checkBox44.Checked)
				{
					numericUpDown88.Value = 1m;
				}
				else
				{
					numericUpDown88.Value = 0m;
				}
				flag_checkBox44 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown88.Value);
		}
		if (numericUpDown85.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox43 = false;
			label43.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown86.Maximum = 999m;
			checkBox43.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown86.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown86.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label43.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown86.Maximum = 255m;
			if (flag_checkBox43)
			{
				if (checkBox43.Checked)
				{
					numericUpDown86.Value = 1m;
				}
				else
				{
					numericUpDown86.Value = 0m;
				}
				flag_checkBox43 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown86.Value);
		}
		if (numericUpDown83.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox42 = false;
			label42.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown84.Maximum = 999m;
			checkBox42.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown84.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown84.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label42.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown84.Maximum = 255m;
			if (flag_checkBox42)
			{
				if (checkBox42.Checked)
				{
					numericUpDown84.Value = 1m;
				}
				else
				{
					numericUpDown84.Value = 0m;
				}
				flag_checkBox42 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown84.Value);
		}
		if (numericUpDown81.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox41 = false;
			label41.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown82.Maximum = 999m;
			checkBox41.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown82.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown82.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label41.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown82.Maximum = 255m;
			if (flag_checkBox41)
			{
				if (checkBox41.Checked)
				{
					numericUpDown82.Value = 1m;
				}
				else
				{
					numericUpDown82.Value = 0m;
				}
				flag_checkBox41 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown82.Value);
		}
		if (numericUpDown79.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox40 = false;
			label40.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown80.Maximum = 999m;
			checkBox40.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown80.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown80.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label40.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown80.Maximum = 255m;
			if (flag_checkBox40)
			{
				if (checkBox40.Checked)
				{
					numericUpDown80.Value = 1m;
				}
				else
				{
					numericUpDown80.Value = 0m;
				}
				flag_checkBox40 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown80.Value);
		}
		if (numericUpDown77.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox39 = false;
			label39.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown78.Maximum = 999m;
			checkBox39.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown78.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown78.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label39.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown78.Maximum = 255m;
			if (flag_checkBox39)
			{
				if (checkBox39.Checked)
				{
					numericUpDown78.Value = 1m;
				}
				else
				{
					numericUpDown78.Value = 0m;
				}
				flag_checkBox39 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown78.Value);
		}
		if (numericUpDown75.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox38 = false;
			label38.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown76.Maximum = 999m;
			checkBox38.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown76.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown76.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label38.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown76.Maximum = 255m;
			if (flag_checkBox38)
			{
				if (checkBox38.Checked)
				{
					numericUpDown76.Value = 1m;
				}
				else
				{
					numericUpDown76.Value = 0m;
				}
				flag_checkBox38 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown76.Value);
		}
		if (numericUpDown73.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox37 = false;
			label37.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown74.Maximum = 999m;
			checkBox37.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown74.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown74.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label37.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown74.Maximum = 255m;
			if (flag_checkBox37)
			{
				if (checkBox37.Checked)
				{
					numericUpDown74.Value = 1m;
				}
				else
				{
					numericUpDown74.Value = 0m;
				}
				flag_checkBox37 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown74.Value);
		}
		if (numericUpDown71.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox36 = false;
			label36.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown72.Maximum = 999m;
			checkBox36.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown72.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown72.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label36.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown72.Maximum = 255m;
			if (flag_checkBox36)
			{
				if (checkBox36.Checked)
				{
					numericUpDown72.Value = 1m;
				}
				else
				{
					numericUpDown72.Value = 0m;
				}
				flag_checkBox36 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown72.Value);
		}
		if (numericUpDown69.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox35 = false;
			label35.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown70.Maximum = 999m;
			checkBox35.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown70.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown70.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label35.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown70.Maximum = 255m;
			if (flag_checkBox35)
			{
				if (checkBox35.Checked)
				{
					numericUpDown70.Value = 1m;
				}
				else
				{
					numericUpDown70.Value = 0m;
				}
				flag_checkBox35 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown70.Value);
		}
		if (numericUpDown67.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox34 = false;
			label34.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown68.Maximum = 999m;
			checkBox34.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown68.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown68.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label34.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown68.Maximum = 255m;
			if (flag_checkBox34)
			{
				if (checkBox34.Checked)
				{
					numericUpDown68.Value = 1m;
				}
				else
				{
					numericUpDown68.Value = 0m;
				}
				flag_checkBox34 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown68.Value);
		}
		if (numericUpDown65.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox33 = false;
			label33.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown66.Maximum = 999m;
			checkBox33.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown66.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown66.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label33.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown66.Maximum = 255m;
			if (flag_checkBox33)
			{
				if (checkBox33.Checked)
				{
					numericUpDown66.Value = 1m;
				}
				else
				{
					numericUpDown66.Value = 0m;
				}
				flag_checkBox33 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown66.Value);
		}
		if (numericUpDown63.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox32 = false;
			label32.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown64.Maximum = 999m;
			checkBox32.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown64.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown64.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label32.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown64.Maximum = 255m;
			if (flag_checkBox32)
			{
				if (checkBox32.Checked)
				{
					numericUpDown64.Value = 1m;
				}
				else
				{
					numericUpDown64.Value = 0m;
				}
				flag_checkBox32 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown64.Value);
		}
		if (numericUpDown61.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox31 = false;
			label31.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown62.Maximum = 999m;
			checkBox31.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown62.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown62.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label31.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown62.Maximum = 255m;
			if (flag_checkBox31)
			{
				if (checkBox31.Checked)
				{
					numericUpDown62.Value = 1m;
				}
				else
				{
					numericUpDown62.Value = 0m;
				}
				flag_checkBox31 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown62.Value);
		}
		if (numericUpDown59.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox30 = false;
			label30.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown60.Maximum = 999m;
			checkBox30.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown60.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown60.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label30.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown60.Maximum = 255m;
			if (flag_checkBox30)
			{
				if (checkBox30.Checked)
				{
					numericUpDown60.Value = 1m;
				}
				else
				{
					numericUpDown60.Value = 0m;
				}
				flag_checkBox30 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown60.Value);
		}
		if (numericUpDown57.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox29 = false;
			label29.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown58.Maximum = 999m;
			checkBox29.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown58.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown58.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			label29.Text = "№" + Convert.ToString(tx_lenght);
			numericUpDown58.Maximum = 255m;
			if (flag_checkBox29)
			{
				if (checkBox29.Checked)
				{
					numericUpDown58.Value = 1m;
				}
				else
				{
					numericUpDown58.Value = 0m;
				}
				flag_checkBox29 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown58.Value);
		}
		if (numericUpDown55.Value == 2m)
		{
			tx_lenght += 2;
			flag_checkBox22 = false;
			numericUpDown56.Maximum = 999m;
			checkBox22.Checked = false;
			buf_tx[tx_lenght - 2] = Convert.ToByte(Convert.ToUInt16(numericUpDown56.Value) >> 8);
			buf_tx[tx_lenght - 1] = Convert.ToByte(Convert.ToUInt16(numericUpDown56.Value) & 0xFF);
		}
		else
		{
			tx_lenght++;
			numericUpDown56.Maximum = 255m;
			if (flag_checkBox22)
			{
				if (checkBox22.Checked)
				{
					numericUpDown56.Value = 1m;
				}
				else
				{
					numericUpDown56.Value = 0m;
				}
				flag_checkBox22 = false;
			}
			buf_tx[tx_lenght - 1] = Convert.ToByte(numericUpDown56.Value);
		}
		tx_buffer_lenght = tx_lenght;
	}

	private void checkBox1_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox1 = true;
	}

	private void checkBox2_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox2 = true;
	}

	private void checkBox3_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox3 = true;
	}

	private void checkBox4_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox4 = true;
	}

	private void checkBox5_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox5 = true;
	}

	private void checkBox6_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox6 = true;
	}

	private void checkBox7_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox7 = true;
	}

	private void checkBox8_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox8 = true;
	}

	private void checkBox9_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox9 = true;
	}

	private void checkBox10_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox10 = true;
	}

	private void checkBox11_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox11 = true;
	}

	private void checkBox12_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox12 = true;
	}

	private void checkBox13_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox13 = true;
	}

	private void checkBox14_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox14 = true;
	}

	private void checkBox15_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox15 = true;
	}

	private void checkBox16_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox16 = true;
	}

	private void checkBox17_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox17 = true;
	}

	private void checkBox18_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox18 = true;
	}

	private void checkBox19_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox19 = true;
	}

	private void checkBox20_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox20 = true;
	}

	private void checkBox21_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox21 = true;
	}

	private void checkBox22_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox22 = true;
	}

	private void checkBox23_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox23 = true;
	}

	private void checkBox24_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox24 = true;
	}

	private void checkBox25_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox25 = true;
	}

	private void checkBox26_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox26 = true;
	}

	private void checkBox27_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox27 = true;
	}

	private void checkBox28_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox28 = true;
	}

	private void checkBox29_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox29 = true;
	}

	private void checkBox30_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox30 = true;
	}

	private void checkBox31_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox31 = true;
	}

	private void checkBox32_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox32 = true;
	}

	private void checkBox33_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox33 = true;
	}

	private void checkBox34_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox34 = true;
	}

	private void checkBox35_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox35 = true;
	}

	private void checkBox36_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox36 = true;
	}

	private void checkBox37_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox37 = true;
	}

	private void checkBox38_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox38 = true;
	}

	private void checkBox39_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox39 = true;
	}

	private void checkBox40_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox40 = true;
	}

	private void checkBox41_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox41 = true;
	}

	private void checkBox42_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox42 = true;
	}

	private void checkBox43_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox43 = true;
	}

	private void checkBox44_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox44 = true;
	}

	private void checkBox45_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox45 = true;
	}

	private void checkBox46_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox46 = true;
	}

	private void checkBox47_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox47 = true;
	}

	private void checkBox48_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox48 = true;
	}

	private void checkBox49_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox49 = true;
	}

	private void checkBox50_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox50 = true;
	}

	private void checkBox51_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox51 = true;
	}

	private void checkBox52_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox52 = true;
	}

	private void checkBox53_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox53 = true;
	}

	private void checkBox54_CheckedChanged(object sender, EventArgs e)
	{
		flag_checkBox54 = true;
	}

	private void numericUpDown109_ValueChanged(object sender, EventArgs e)
	{
		System.Windows.Forms.Timer timer = timer1;
		int interval = (timer1.Interval = 1000 / Convert.ToUInt16(numericUpDown109.Value));
		timer.Interval = interval;
	}

	private void Form1_FormClosing(object sender, FormClosingEventArgs e)
	{
		if (udp != null)
		{
			udp.Close();
		}
		if (rec != null)
		{
			rec.Join();
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
		this.components = new System.ComponentModel.Container();
		this.label3 = new System.Windows.Forms.Label();
		this.textBoxAddress = new System.Windows.Forms.TextBox();
		this.timer1 = new System.Windows.Forms.Timer(this.components);
		this.textBox1 = new System.Windows.Forms.TextBox();
		this.button1 = new System.Windows.Forms.Button();
		this.textBox2 = new System.Windows.Forms.TextBox();
		this.button4 = new System.Windows.Forms.Button();
		this.label1 = new System.Windows.Forms.Label();
		this.numericUpDown1 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown2 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown3 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown4 = new System.Windows.Forms.NumericUpDown();
		this.label2 = new System.Windows.Forms.Label();
		this.numericUpDown5 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown6 = new System.Windows.Forms.NumericUpDown();
		this.label4 = new System.Windows.Forms.Label();
		this.numericUpDown7 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown8 = new System.Windows.Forms.NumericUpDown();
		this.label5 = new System.Windows.Forms.Label();
		this.numericUpDown9 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown10 = new System.Windows.Forms.NumericUpDown();
		this.label6 = new System.Windows.Forms.Label();
		this.numericUpDown11 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown12 = new System.Windows.Forms.NumericUpDown();
		this.label7 = new System.Windows.Forms.Label();
		this.numericUpDown13 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown14 = new System.Windows.Forms.NumericUpDown();
		this.label8 = new System.Windows.Forms.Label();
		this.numericUpDown15 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown16 = new System.Windows.Forms.NumericUpDown();
		this.label9 = new System.Windows.Forms.Label();
		this.numericUpDown17 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown18 = new System.Windows.Forms.NumericUpDown();
		this.label10 = new System.Windows.Forms.Label();
		this.numericUpDown19 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown20 = new System.Windows.Forms.NumericUpDown();
		this.label11 = new System.Windows.Forms.Label();
		this.numericUpDown21 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown22 = new System.Windows.Forms.NumericUpDown();
		this.label12 = new System.Windows.Forms.Label();
		this.numericUpDown23 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown24 = new System.Windows.Forms.NumericUpDown();
		this.label13 = new System.Windows.Forms.Label();
		this.numericUpDown25 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown26 = new System.Windows.Forms.NumericUpDown();
		this.label14 = new System.Windows.Forms.Label();
		this.numericUpDown27 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown28 = new System.Windows.Forms.NumericUpDown();
		this.label15 = new System.Windows.Forms.Label();
		this.numericUpDown29 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown30 = new System.Windows.Forms.NumericUpDown();
		this.label16 = new System.Windows.Forms.Label();
		this.numericUpDown31 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown32 = new System.Windows.Forms.NumericUpDown();
		this.label17 = new System.Windows.Forms.Label();
		this.numericUpDown33 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown34 = new System.Windows.Forms.NumericUpDown();
		this.label18 = new System.Windows.Forms.Label();
		this.numericUpDown35 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown36 = new System.Windows.Forms.NumericUpDown();
		this.label19 = new System.Windows.Forms.Label();
		this.numericUpDown37 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown38 = new System.Windows.Forms.NumericUpDown();
		this.label20 = new System.Windows.Forms.Label();
		this.numericUpDown39 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown40 = new System.Windows.Forms.NumericUpDown();
		this.label21 = new System.Windows.Forms.Label();
		this.numericUpDown41 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown42 = new System.Windows.Forms.NumericUpDown();
		this.label22 = new System.Windows.Forms.Label();
		this.numericUpDown43 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown44 = new System.Windows.Forms.NumericUpDown();
		this.label23 = new System.Windows.Forms.Label();
		this.numericUpDown45 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown46 = new System.Windows.Forms.NumericUpDown();
		this.label24 = new System.Windows.Forms.Label();
		this.numericUpDown47 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown48 = new System.Windows.Forms.NumericUpDown();
		this.label25 = new System.Windows.Forms.Label();
		this.numericUpDown49 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown50 = new System.Windows.Forms.NumericUpDown();
		this.label26 = new System.Windows.Forms.Label();
		this.numericUpDown51 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown52 = new System.Windows.Forms.NumericUpDown();
		this.label27 = new System.Windows.Forms.Label();
		this.numericUpDown53 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown54 = new System.Windows.Forms.NumericUpDown();
		this.label28 = new System.Windows.Forms.Label();
		this.checkBox1 = new System.Windows.Forms.CheckBox();
		this.checkBox2 = new System.Windows.Forms.CheckBox();
		this.checkBox3 = new System.Windows.Forms.CheckBox();
		this.checkBox4 = new System.Windows.Forms.CheckBox();
		this.checkBox5 = new System.Windows.Forms.CheckBox();
		this.checkBox6 = new System.Windows.Forms.CheckBox();
		this.checkBox7 = new System.Windows.Forms.CheckBox();
		this.checkBox8 = new System.Windows.Forms.CheckBox();
		this.checkBox9 = new System.Windows.Forms.CheckBox();
		this.checkBox10 = new System.Windows.Forms.CheckBox();
		this.checkBox11 = new System.Windows.Forms.CheckBox();
		this.checkBox12 = new System.Windows.Forms.CheckBox();
		this.checkBox13 = new System.Windows.Forms.CheckBox();
		this.checkBox14 = new System.Windows.Forms.CheckBox();
		this.checkBox15 = new System.Windows.Forms.CheckBox();
		this.checkBox16 = new System.Windows.Forms.CheckBox();
		this.checkBox17 = new System.Windows.Forms.CheckBox();
		this.checkBox18 = new System.Windows.Forms.CheckBox();
		this.checkBox19 = new System.Windows.Forms.CheckBox();
		this.checkBox20 = new System.Windows.Forms.CheckBox();
		this.checkBox21 = new System.Windows.Forms.CheckBox();
		this.checkBox23 = new System.Windows.Forms.CheckBox();
		this.checkBox24 = new System.Windows.Forms.CheckBox();
		this.checkBox25 = new System.Windows.Forms.CheckBox();
		this.checkBox26 = new System.Windows.Forms.CheckBox();
		this.checkBox27 = new System.Windows.Forms.CheckBox();
		this.checkBox28 = new System.Windows.Forms.CheckBox();
		this.textBox3 = new System.Windows.Forms.TextBox();
		this.textBox4 = new System.Windows.Forms.TextBox();
		this.textBox5 = new System.Windows.Forms.TextBox();
		this.textBox6 = new System.Windows.Forms.TextBox();
		this.textBox7 = new System.Windows.Forms.TextBox();
		this.textBox8 = new System.Windows.Forms.TextBox();
		this.textBox9 = new System.Windows.Forms.TextBox();
		this.textBox10 = new System.Windows.Forms.TextBox();
		this.textBox11 = new System.Windows.Forms.TextBox();
		this.textBox12 = new System.Windows.Forms.TextBox();
		this.textBox13 = new System.Windows.Forms.TextBox();
		this.textBox14 = new System.Windows.Forms.TextBox();
		this.textBox15 = new System.Windows.Forms.TextBox();
		this.textBox16 = new System.Windows.Forms.TextBox();
		this.textBox17 = new System.Windows.Forms.TextBox();
		this.textBox18 = new System.Windows.Forms.TextBox();
		this.textBox19 = new System.Windows.Forms.TextBox();
		this.textBox20 = new System.Windows.Forms.TextBox();
		this.textBox21 = new System.Windows.Forms.TextBox();
		this.textBox22 = new System.Windows.Forms.TextBox();
		this.textBox23 = new System.Windows.Forms.TextBox();
		this.textBox24 = new System.Windows.Forms.TextBox();
		this.textBox25 = new System.Windows.Forms.TextBox();
		this.textBox26 = new System.Windows.Forms.TextBox();
		this.textBox32 = new System.Windows.Forms.TextBox();
		this.textBox33 = new System.Windows.Forms.TextBox();
		this.textBox34 = new System.Windows.Forms.TextBox();
		this.textBox27 = new System.Windows.Forms.TextBox();
		this.textBox28 = new System.Windows.Forms.TextBox();
		this.textBox29 = new System.Windows.Forms.TextBox();
		this.textBox30 = new System.Windows.Forms.TextBox();
		this.textBox31 = new System.Windows.Forms.TextBox();
		this.textBox35 = new System.Windows.Forms.TextBox();
		this.textBox36 = new System.Windows.Forms.TextBox();
		this.textBox37 = new System.Windows.Forms.TextBox();
		this.textBox38 = new System.Windows.Forms.TextBox();
		this.textBox39 = new System.Windows.Forms.TextBox();
		this.textBox40 = new System.Windows.Forms.TextBox();
		this.textBox41 = new System.Windows.Forms.TextBox();
		this.textBox42 = new System.Windows.Forms.TextBox();
		this.textBox43 = new System.Windows.Forms.TextBox();
		this.textBox44 = new System.Windows.Forms.TextBox();
		this.textBox45 = new System.Windows.Forms.TextBox();
		this.textBox46 = new System.Windows.Forms.TextBox();
		this.textBox47 = new System.Windows.Forms.TextBox();
		this.textBox48 = new System.Windows.Forms.TextBox();
		this.textBox49 = new System.Windows.Forms.TextBox();
		this.textBox50 = new System.Windows.Forms.TextBox();
		this.textBox51 = new System.Windows.Forms.TextBox();
		this.textBox52 = new System.Windows.Forms.TextBox();
		this.textBox53 = new System.Windows.Forms.TextBox();
		this.textBox54 = new System.Windows.Forms.TextBox();
		this.textBox55 = new System.Windows.Forms.TextBox();
		this.textBox56 = new System.Windows.Forms.TextBox();
		this.checkBox22 = new System.Windows.Forms.CheckBox();
		this.checkBox29 = new System.Windows.Forms.CheckBox();
		this.checkBox30 = new System.Windows.Forms.CheckBox();
		this.checkBox31 = new System.Windows.Forms.CheckBox();
		this.checkBox32 = new System.Windows.Forms.CheckBox();
		this.checkBox33 = new System.Windows.Forms.CheckBox();
		this.checkBox34 = new System.Windows.Forms.CheckBox();
		this.checkBox35 = new System.Windows.Forms.CheckBox();
		this.checkBox36 = new System.Windows.Forms.CheckBox();
		this.checkBox37 = new System.Windows.Forms.CheckBox();
		this.checkBox38 = new System.Windows.Forms.CheckBox();
		this.checkBox39 = new System.Windows.Forms.CheckBox();
		this.checkBox40 = new System.Windows.Forms.CheckBox();
		this.checkBox41 = new System.Windows.Forms.CheckBox();
		this.checkBox42 = new System.Windows.Forms.CheckBox();
		this.checkBox43 = new System.Windows.Forms.CheckBox();
		this.checkBox44 = new System.Windows.Forms.CheckBox();
		this.checkBox45 = new System.Windows.Forms.CheckBox();
		this.checkBox46 = new System.Windows.Forms.CheckBox();
		this.checkBox47 = new System.Windows.Forms.CheckBox();
		this.checkBox48 = new System.Windows.Forms.CheckBox();
		this.checkBox49 = new System.Windows.Forms.CheckBox();
		this.checkBox50 = new System.Windows.Forms.CheckBox();
		this.checkBox51 = new System.Windows.Forms.CheckBox();
		this.checkBox52 = new System.Windows.Forms.CheckBox();
		this.checkBox53 = new System.Windows.Forms.CheckBox();
		this.checkBox54 = new System.Windows.Forms.CheckBox();
		this.numericUpDown55 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown56 = new System.Windows.Forms.NumericUpDown();
		this.label29 = new System.Windows.Forms.Label();
		this.numericUpDown57 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown58 = new System.Windows.Forms.NumericUpDown();
		this.label30 = new System.Windows.Forms.Label();
		this.numericUpDown59 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown60 = new System.Windows.Forms.NumericUpDown();
		this.label31 = new System.Windows.Forms.Label();
		this.numericUpDown61 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown62 = new System.Windows.Forms.NumericUpDown();
		this.label32 = new System.Windows.Forms.Label();
		this.numericUpDown63 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown64 = new System.Windows.Forms.NumericUpDown();
		this.label33 = new System.Windows.Forms.Label();
		this.numericUpDown65 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown66 = new System.Windows.Forms.NumericUpDown();
		this.label34 = new System.Windows.Forms.Label();
		this.numericUpDown67 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown68 = new System.Windows.Forms.NumericUpDown();
		this.label35 = new System.Windows.Forms.Label();
		this.numericUpDown69 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown70 = new System.Windows.Forms.NumericUpDown();
		this.label36 = new System.Windows.Forms.Label();
		this.numericUpDown71 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown72 = new System.Windows.Forms.NumericUpDown();
		this.label37 = new System.Windows.Forms.Label();
		this.numericUpDown73 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown74 = new System.Windows.Forms.NumericUpDown();
		this.label38 = new System.Windows.Forms.Label();
		this.numericUpDown75 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown76 = new System.Windows.Forms.NumericUpDown();
		this.label39 = new System.Windows.Forms.Label();
		this.numericUpDown77 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown78 = new System.Windows.Forms.NumericUpDown();
		this.label40 = new System.Windows.Forms.Label();
		this.numericUpDown79 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown80 = new System.Windows.Forms.NumericUpDown();
		this.label41 = new System.Windows.Forms.Label();
		this.numericUpDown81 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown82 = new System.Windows.Forms.NumericUpDown();
		this.label42 = new System.Windows.Forms.Label();
		this.numericUpDown83 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown84 = new System.Windows.Forms.NumericUpDown();
		this.label43 = new System.Windows.Forms.Label();
		this.numericUpDown85 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown86 = new System.Windows.Forms.NumericUpDown();
		this.label44 = new System.Windows.Forms.Label();
		this.numericUpDown87 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown88 = new System.Windows.Forms.NumericUpDown();
		this.label45 = new System.Windows.Forms.Label();
		this.numericUpDown89 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown90 = new System.Windows.Forms.NumericUpDown();
		this.label46 = new System.Windows.Forms.Label();
		this.numericUpDown91 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown92 = new System.Windows.Forms.NumericUpDown();
		this.label47 = new System.Windows.Forms.Label();
		this.numericUpDown93 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown94 = new System.Windows.Forms.NumericUpDown();
		this.label48 = new System.Windows.Forms.Label();
		this.numericUpDown95 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown96 = new System.Windows.Forms.NumericUpDown();
		this.label49 = new System.Windows.Forms.Label();
		this.numericUpDown97 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown98 = new System.Windows.Forms.NumericUpDown();
		this.label50 = new System.Windows.Forms.Label();
		this.numericUpDown99 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown100 = new System.Windows.Forms.NumericUpDown();
		this.label51 = new System.Windows.Forms.Label();
		this.numericUpDown101 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown102 = new System.Windows.Forms.NumericUpDown();
		this.label52 = new System.Windows.Forms.Label();
		this.numericUpDown103 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown104 = new System.Windows.Forms.NumericUpDown();
		this.label53 = new System.Windows.Forms.Label();
		this.numericUpDown105 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown106 = new System.Windows.Forms.NumericUpDown();
		this.label54 = new System.Windows.Forms.Label();
		this.numericUpDown107 = new System.Windows.Forms.NumericUpDown();
		this.numericUpDown108 = new System.Windows.Forms.NumericUpDown();
		this.label55 = new System.Windows.Forms.Label();
		this.timer2 = new System.Windows.Forms.Timer(this.components);
		this.label56 = new System.Windows.Forms.Label();
		this.label57 = new System.Windows.Forms.Label();
		this.numericUpDown109 = new System.Windows.Forms.NumericUpDown();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown1).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown2).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown3).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown4).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown5).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown6).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown7).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown8).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown9).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown10).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown11).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown12).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown13).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown14).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown15).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown16).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown17).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown18).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown19).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown20).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown21).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown22).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown23).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown24).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown25).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown26).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown27).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown28).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown29).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown30).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown31).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown32).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown33).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown34).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown35).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown36).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown37).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown38).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown39).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown40).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown41).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown42).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown43).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown44).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown45).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown46).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown47).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown48).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown49).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown50).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown51).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown52).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown53).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown54).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown55).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown56).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown57).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown58).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown59).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown60).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown61).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown62).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown63).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown64).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown65).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown66).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown67).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown68).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown69).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown70).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown71).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown72).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown73).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown74).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown75).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown76).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown77).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown78).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown79).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown80).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown81).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown82).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown83).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown84).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown85).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown86).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown87).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown88).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown89).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown90).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown91).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown92).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown93).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown94).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown95).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown96).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown97).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown98).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown99).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown100).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown101).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown102).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown103).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown104).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown105).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown106).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown107).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown108).BeginInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown109).BeginInit();
		base.SuspendLayout();
		this.label3.AutoSize = true;
		this.label3.Location = new System.Drawing.Point(242, 533);
		this.label3.Name = "label3";
		this.label3.Size = new System.Drawing.Size(179, 13);
		this.label3.TabIndex = 4;
		this.label3.Text = "IP адрес головного контроллера :";
		this.textBoxAddress.Location = new System.Drawing.Point(427, 530);
		this.textBoxAddress.Name = "textBoxAddress";
		this.textBoxAddress.Size = new System.Drawing.Size(78, 20);
		this.textBoxAddress.TabIndex = 5;
		this.textBoxAddress.Text = "192.168.0.176";
		this.timer1.Interval = 10;
		this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
		this.textBox1.Location = new System.Drawing.Point(600, 346);
		this.textBox1.MaxLength = 1024;
		this.textBox1.Multiline = true;
		this.textBox1.Name = "textBox1";
		this.textBox1.Size = new System.Drawing.Size(532, 208);
		this.textBox1.TabIndex = 26;
		this.button1.Location = new System.Drawing.Point(3, 521);
		this.button1.Name = "button1";
		this.button1.Size = new System.Drawing.Size(104, 33);
		this.button1.TabIndex = 28;
		this.button1.Text = "Отправить пакет";
		this.button1.UseVisualStyleBackColor = true;
		this.button1.Click += new System.EventHandler(this.button1_Click_1);
		this.textBox2.Location = new System.Drawing.Point(600, 23);
		this.textBox2.MaxLength = 1024;
		this.textBox2.Multiline = true;
		this.textBox2.Name = "textBox2";
		this.textBox2.ReadOnly = true;
		this.textBox2.Size = new System.Drawing.Size(532, 302);
		this.textBox2.TabIndex = 30;
		this.button4.Location = new System.Drawing.Point(109, 521);
		this.button4.Name = "button4";
		this.button4.Size = new System.Drawing.Size(123, 33);
		this.button4.TabIndex = 31;
		this.button4.Text = "Вкл. Таймер";
		this.button4.UseVisualStyleBackColor = true;
		this.button4.Click += new System.EventHandler(this.button4_Click);
		this.label1.AutoSize = true;
		this.label1.Location = new System.Drawing.Point(0, 3);
		this.label1.Name = "label1";
		this.label1.Size = new System.Drawing.Size(30, 13);
		this.label1.TabIndex = 32;
		this.label1.Text = "№00";
		this.numericUpDown1.Location = new System.Drawing.Point(194, 1);
		this.numericUpDown1.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown1.Name = "numericUpDown1";
		this.numericUpDown1.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown1.TabIndex = 33;
		this.numericUpDown2.Location = new System.Drawing.Point(233, 1);
		this.numericUpDown2.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown2.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown2.Name = "numericUpDown2";
		this.numericUpDown2.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown2.TabIndex = 34;
		this.numericUpDown2.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown3.Location = new System.Drawing.Point(233, 20);
		this.numericUpDown3.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown3.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown3.Name = "numericUpDown3";
		this.numericUpDown3.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown3.TabIndex = 37;
		this.numericUpDown3.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown4.Location = new System.Drawing.Point(194, 20);
		this.numericUpDown4.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown4.Name = "numericUpDown4";
		this.numericUpDown4.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown4.TabIndex = 36;
		this.label2.AutoSize = true;
		this.label2.Location = new System.Drawing.Point(0, 22);
		this.label2.Name = "label2";
		this.label2.Size = new System.Drawing.Size(30, 13);
		this.label2.TabIndex = 35;
		this.label2.Text = "№01";
		this.numericUpDown5.Location = new System.Drawing.Point(233, 39);
		this.numericUpDown5.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown5.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown5.Name = "numericUpDown5";
		this.numericUpDown5.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown5.TabIndex = 40;
		this.numericUpDown5.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown6.Location = new System.Drawing.Point(194, 39);
		this.numericUpDown6.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown6.Name = "numericUpDown6";
		this.numericUpDown6.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown6.TabIndex = 39;
		this.label4.AutoSize = true;
		this.label4.Location = new System.Drawing.Point(0, 41);
		this.label4.Name = "label4";
		this.label4.Size = new System.Drawing.Size(30, 13);
		this.label4.TabIndex = 38;
		this.label4.Text = "№02";
		this.numericUpDown7.Location = new System.Drawing.Point(233, 58);
		this.numericUpDown7.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown7.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown7.Name = "numericUpDown7";
		this.numericUpDown7.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown7.TabIndex = 43;
		this.numericUpDown7.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown8.Location = new System.Drawing.Point(194, 58);
		this.numericUpDown8.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown8.Name = "numericUpDown8";
		this.numericUpDown8.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown8.TabIndex = 42;
		this.label5.AutoSize = true;
		this.label5.Location = new System.Drawing.Point(0, 60);
		this.label5.Name = "label5";
		this.label5.Size = new System.Drawing.Size(30, 13);
		this.label5.TabIndex = 41;
		this.label5.Text = "№03";
		this.numericUpDown9.Location = new System.Drawing.Point(233, 77);
		this.numericUpDown9.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown9.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown9.Name = "numericUpDown9";
		this.numericUpDown9.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown9.TabIndex = 46;
		this.numericUpDown9.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown10.Location = new System.Drawing.Point(194, 77);
		this.numericUpDown10.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown10.Name = "numericUpDown10";
		this.numericUpDown10.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown10.TabIndex = 45;
		this.label6.AutoSize = true;
		this.label6.Location = new System.Drawing.Point(0, 79);
		this.label6.Name = "label6";
		this.label6.Size = new System.Drawing.Size(30, 13);
		this.label6.TabIndex = 44;
		this.label6.Text = "№04";
		this.numericUpDown11.Location = new System.Drawing.Point(233, 96);
		this.numericUpDown11.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown11.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown11.Name = "numericUpDown11";
		this.numericUpDown11.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown11.TabIndex = 49;
		this.numericUpDown11.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown12.Location = new System.Drawing.Point(194, 96);
		this.numericUpDown12.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown12.Name = "numericUpDown12";
		this.numericUpDown12.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown12.TabIndex = 48;
		this.label7.AutoSize = true;
		this.label7.Location = new System.Drawing.Point(0, 98);
		this.label7.Name = "label7";
		this.label7.Size = new System.Drawing.Size(30, 13);
		this.label7.TabIndex = 47;
		this.label7.Text = "№05";
		this.numericUpDown13.Location = new System.Drawing.Point(233, 115);
		this.numericUpDown13.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown13.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown13.Name = "numericUpDown13";
		this.numericUpDown13.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown13.TabIndex = 52;
		this.numericUpDown13.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown14.Location = new System.Drawing.Point(194, 115);
		this.numericUpDown14.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown14.Name = "numericUpDown14";
		this.numericUpDown14.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown14.TabIndex = 51;
		this.label8.AutoSize = true;
		this.label8.Location = new System.Drawing.Point(0, 117);
		this.label8.Name = "label8";
		this.label8.Size = new System.Drawing.Size(30, 13);
		this.label8.TabIndex = 50;
		this.label8.Text = "№06";
		this.numericUpDown15.Location = new System.Drawing.Point(233, 134);
		this.numericUpDown15.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown15.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown15.Name = "numericUpDown15";
		this.numericUpDown15.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown15.TabIndex = 55;
		this.numericUpDown15.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown16.Location = new System.Drawing.Point(194, 134);
		this.numericUpDown16.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown16.Name = "numericUpDown16";
		this.numericUpDown16.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown16.TabIndex = 54;
		this.label9.AutoSize = true;
		this.label9.Location = new System.Drawing.Point(0, 136);
		this.label9.Name = "label9";
		this.label9.Size = new System.Drawing.Size(30, 13);
		this.label9.TabIndex = 53;
		this.label9.Text = "№07";
		this.numericUpDown17.Location = new System.Drawing.Point(233, 153);
		this.numericUpDown17.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown17.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown17.Name = "numericUpDown17";
		this.numericUpDown17.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown17.TabIndex = 58;
		this.numericUpDown17.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown18.Location = new System.Drawing.Point(194, 153);
		this.numericUpDown18.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown18.Name = "numericUpDown18";
		this.numericUpDown18.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown18.TabIndex = 57;
		this.label10.AutoSize = true;
		this.label10.Location = new System.Drawing.Point(0, 155);
		this.label10.Name = "label10";
		this.label10.Size = new System.Drawing.Size(30, 13);
		this.label10.TabIndex = 56;
		this.label10.Text = "№08";
		this.numericUpDown19.Location = new System.Drawing.Point(233, 324);
		this.numericUpDown19.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown19.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown19.Name = "numericUpDown19";
		this.numericUpDown19.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown19.TabIndex = 85;
		this.numericUpDown19.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown20.Location = new System.Drawing.Point(194, 324);
		this.numericUpDown20.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown20.Name = "numericUpDown20";
		this.numericUpDown20.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown20.TabIndex = 84;
		this.label11.AutoSize = true;
		this.label11.Location = new System.Drawing.Point(0, 328);
		this.label11.Name = "label11";
		this.label11.Size = new System.Drawing.Size(30, 13);
		this.label11.TabIndex = 83;
		this.label11.Text = "№17";
		this.numericUpDown21.Location = new System.Drawing.Point(233, 305);
		this.numericUpDown21.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown21.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown21.Name = "numericUpDown21";
		this.numericUpDown21.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown21.TabIndex = 82;
		this.numericUpDown21.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown22.Location = new System.Drawing.Point(194, 305);
		this.numericUpDown22.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown22.Name = "numericUpDown22";
		this.numericUpDown22.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown22.TabIndex = 81;
		this.label12.AutoSize = true;
		this.label12.Location = new System.Drawing.Point(0, 309);
		this.label12.Name = "label12";
		this.label12.Size = new System.Drawing.Size(30, 13);
		this.label12.TabIndex = 80;
		this.label12.Text = "№16";
		this.numericUpDown23.Location = new System.Drawing.Point(233, 286);
		this.numericUpDown23.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown23.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown23.Name = "numericUpDown23";
		this.numericUpDown23.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown23.TabIndex = 79;
		this.numericUpDown23.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown24.Location = new System.Drawing.Point(194, 286);
		this.numericUpDown24.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown24.Name = "numericUpDown24";
		this.numericUpDown24.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown24.TabIndex = 78;
		this.label13.AutoSize = true;
		this.label13.Location = new System.Drawing.Point(0, 290);
		this.label13.Name = "label13";
		this.label13.Size = new System.Drawing.Size(30, 13);
		this.label13.TabIndex = 77;
		this.label13.Text = "№15";
		this.numericUpDown25.Location = new System.Drawing.Point(233, 267);
		this.numericUpDown25.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown25.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown25.Name = "numericUpDown25";
		this.numericUpDown25.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown25.TabIndex = 76;
		this.numericUpDown25.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown26.Location = new System.Drawing.Point(194, 267);
		this.numericUpDown26.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown26.Name = "numericUpDown26";
		this.numericUpDown26.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown26.TabIndex = 75;
		this.label14.AutoSize = true;
		this.label14.Location = new System.Drawing.Point(0, 270);
		this.label14.Name = "label14";
		this.label14.Size = new System.Drawing.Size(30, 13);
		this.label14.TabIndex = 74;
		this.label14.Text = "№14";
		this.numericUpDown27.Location = new System.Drawing.Point(233, 248);
		this.numericUpDown27.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown27.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown27.Name = "numericUpDown27";
		this.numericUpDown27.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown27.TabIndex = 73;
		this.numericUpDown27.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown28.Location = new System.Drawing.Point(194, 248);
		this.numericUpDown28.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown28.Name = "numericUpDown28";
		this.numericUpDown28.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown28.TabIndex = 72;
		this.label15.AutoSize = true;
		this.label15.Location = new System.Drawing.Point(0, 251);
		this.label15.Name = "label15";
		this.label15.Size = new System.Drawing.Size(30, 13);
		this.label15.TabIndex = 71;
		this.label15.Text = "№13";
		this.numericUpDown29.Location = new System.Drawing.Point(233, 229);
		this.numericUpDown29.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown29.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown29.Name = "numericUpDown29";
		this.numericUpDown29.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown29.TabIndex = 70;
		this.numericUpDown29.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown30.Location = new System.Drawing.Point(194, 229);
		this.numericUpDown30.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown30.Name = "numericUpDown30";
		this.numericUpDown30.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown30.TabIndex = 69;
		this.label16.AutoSize = true;
		this.label16.Location = new System.Drawing.Point(0, 231);
		this.label16.Name = "label16";
		this.label16.Size = new System.Drawing.Size(30, 13);
		this.label16.TabIndex = 68;
		this.label16.Text = "№12";
		this.numericUpDown31.Location = new System.Drawing.Point(233, 210);
		this.numericUpDown31.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown31.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown31.Name = "numericUpDown31";
		this.numericUpDown31.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown31.TabIndex = 67;
		this.numericUpDown31.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown32.Location = new System.Drawing.Point(194, 210);
		this.numericUpDown32.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown32.Name = "numericUpDown32";
		this.numericUpDown32.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown32.TabIndex = 66;
		this.label17.AutoSize = true;
		this.label17.Location = new System.Drawing.Point(0, 212);
		this.label17.Name = "label17";
		this.label17.Size = new System.Drawing.Size(30, 13);
		this.label17.TabIndex = 65;
		this.label17.Text = "№11";
		this.numericUpDown33.Location = new System.Drawing.Point(233, 191);
		this.numericUpDown33.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown33.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown33.Name = "numericUpDown33";
		this.numericUpDown33.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown33.TabIndex = 64;
		this.numericUpDown33.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown34.Location = new System.Drawing.Point(194, 191);
		this.numericUpDown34.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown34.Name = "numericUpDown34";
		this.numericUpDown34.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown34.TabIndex = 63;
		this.label18.AutoSize = true;
		this.label18.Location = new System.Drawing.Point(0, 193);
		this.label18.Name = "label18";
		this.label18.Size = new System.Drawing.Size(30, 13);
		this.label18.TabIndex = 62;
		this.label18.Text = "№10";
		this.numericUpDown35.Location = new System.Drawing.Point(233, 172);
		this.numericUpDown35.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown35.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown35.Name = "numericUpDown35";
		this.numericUpDown35.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown35.TabIndex = 61;
		this.numericUpDown35.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown36.Location = new System.Drawing.Point(194, 172);
		this.numericUpDown36.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown36.Name = "numericUpDown36";
		this.numericUpDown36.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown36.TabIndex = 60;
		this.label19.AutoSize = true;
		this.label19.Location = new System.Drawing.Point(0, 174);
		this.label19.Name = "label19";
		this.label19.Size = new System.Drawing.Size(30, 13);
		this.label19.TabIndex = 59;
		this.label19.Text = "№09";
		this.numericUpDown37.Location = new System.Drawing.Point(233, 495);
		this.numericUpDown37.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown37.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown37.Name = "numericUpDown37";
		this.numericUpDown37.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown37.TabIndex = 112;
		this.numericUpDown37.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown38.Location = new System.Drawing.Point(194, 495);
		this.numericUpDown38.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown38.Name = "numericUpDown38";
		this.numericUpDown38.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown38.TabIndex = 111;
		this.label20.AutoSize = true;
		this.label20.Location = new System.Drawing.Point(0, 498);
		this.label20.Name = "label20";
		this.label20.Size = new System.Drawing.Size(30, 13);
		this.label20.TabIndex = 110;
		this.label20.Text = "№26";
		this.numericUpDown39.Location = new System.Drawing.Point(233, 476);
		this.numericUpDown39.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown39.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown39.Name = "numericUpDown39";
		this.numericUpDown39.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown39.TabIndex = 109;
		this.numericUpDown39.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown40.Location = new System.Drawing.Point(194, 476);
		this.numericUpDown40.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown40.Name = "numericUpDown40";
		this.numericUpDown40.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown40.TabIndex = 108;
		this.label21.AutoSize = true;
		this.label21.Location = new System.Drawing.Point(0, 480);
		this.label21.Name = "label21";
		this.label21.Size = new System.Drawing.Size(30, 13);
		this.label21.TabIndex = 107;
		this.label21.Text = "№25";
		this.numericUpDown41.Location = new System.Drawing.Point(233, 457);
		this.numericUpDown41.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown41.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown41.Name = "numericUpDown41";
		this.numericUpDown41.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown41.TabIndex = 106;
		this.numericUpDown41.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown42.Location = new System.Drawing.Point(194, 457);
		this.numericUpDown42.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown42.Name = "numericUpDown42";
		this.numericUpDown42.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown42.TabIndex = 105;
		this.label22.AutoSize = true;
		this.label22.Location = new System.Drawing.Point(0, 461);
		this.label22.Name = "label22";
		this.label22.Size = new System.Drawing.Size(30, 13);
		this.label22.TabIndex = 104;
		this.label22.Text = "№24";
		this.numericUpDown43.Location = new System.Drawing.Point(233, 438);
		this.numericUpDown43.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown43.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown43.Name = "numericUpDown43";
		this.numericUpDown43.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown43.TabIndex = 103;
		this.numericUpDown43.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown44.Location = new System.Drawing.Point(194, 438);
		this.numericUpDown44.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown44.Name = "numericUpDown44";
		this.numericUpDown44.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown44.TabIndex = 102;
		this.label23.AutoSize = true;
		this.label23.Location = new System.Drawing.Point(0, 442);
		this.label23.Name = "label23";
		this.label23.Size = new System.Drawing.Size(30, 13);
		this.label23.TabIndex = 101;
		this.label23.Text = "№23";
		this.numericUpDown45.Location = new System.Drawing.Point(233, 419);
		this.numericUpDown45.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown45.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown45.Name = "numericUpDown45";
		this.numericUpDown45.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown45.TabIndex = 100;
		this.numericUpDown45.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown46.Location = new System.Drawing.Point(194, 419);
		this.numericUpDown46.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown46.Name = "numericUpDown46";
		this.numericUpDown46.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown46.TabIndex = 99;
		this.label24.AutoSize = true;
		this.label24.Location = new System.Drawing.Point(0, 423);
		this.label24.Name = "label24";
		this.label24.Size = new System.Drawing.Size(30, 13);
		this.label24.TabIndex = 98;
		this.label24.Text = "№22";
		this.numericUpDown47.Location = new System.Drawing.Point(233, 400);
		this.numericUpDown47.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown47.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown47.Name = "numericUpDown47";
		this.numericUpDown47.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown47.TabIndex = 97;
		this.numericUpDown47.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown48.Location = new System.Drawing.Point(194, 400);
		this.numericUpDown48.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown48.Name = "numericUpDown48";
		this.numericUpDown48.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown48.TabIndex = 96;
		this.label25.AutoSize = true;
		this.label25.Location = new System.Drawing.Point(0, 404);
		this.label25.Name = "label25";
		this.label25.Size = new System.Drawing.Size(30, 13);
		this.label25.TabIndex = 95;
		this.label25.Text = "№21";
		this.numericUpDown49.Location = new System.Drawing.Point(233, 381);
		this.numericUpDown49.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown49.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown49.Name = "numericUpDown49";
		this.numericUpDown49.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown49.TabIndex = 94;
		this.numericUpDown49.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown50.Location = new System.Drawing.Point(194, 381);
		this.numericUpDown50.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown50.Name = "numericUpDown50";
		this.numericUpDown50.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown50.TabIndex = 93;
		this.label26.AutoSize = true;
		this.label26.Location = new System.Drawing.Point(0, 385);
		this.label26.Name = "label26";
		this.label26.Size = new System.Drawing.Size(30, 13);
		this.label26.TabIndex = 92;
		this.label26.Text = "№20";
		this.numericUpDown51.Location = new System.Drawing.Point(233, 362);
		this.numericUpDown51.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown51.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown51.Name = "numericUpDown51";
		this.numericUpDown51.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown51.TabIndex = 91;
		this.numericUpDown51.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown52.Location = new System.Drawing.Point(194, 362);
		this.numericUpDown52.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown52.Name = "numericUpDown52";
		this.numericUpDown52.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown52.TabIndex = 90;
		this.label27.AutoSize = true;
		this.label27.Location = new System.Drawing.Point(0, 366);
		this.label27.Name = "label27";
		this.label27.Size = new System.Drawing.Size(30, 13);
		this.label27.TabIndex = 89;
		this.label27.Text = "№19";
		this.numericUpDown53.Location = new System.Drawing.Point(233, 343);
		this.numericUpDown53.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown53.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown53.Name = "numericUpDown53";
		this.numericUpDown53.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown53.TabIndex = 88;
		this.numericUpDown53.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown54.Location = new System.Drawing.Point(194, 343);
		this.numericUpDown54.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown54.Name = "numericUpDown54";
		this.numericUpDown54.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown54.TabIndex = 87;
		this.label28.AutoSize = true;
		this.label28.Location = new System.Drawing.Point(0, 346);
		this.label28.Name = "label28";
		this.label28.Size = new System.Drawing.Size(30, 13);
		this.label28.TabIndex = 86;
		this.label28.Text = "№18";
		this.checkBox1.AutoSize = true;
		this.checkBox1.Location = new System.Drawing.Point(264, 4);
		this.checkBox1.Name = "checkBox1";
		this.checkBox1.Size = new System.Drawing.Size(15, 14);
		this.checkBox1.TabIndex = 113;
		this.checkBox1.UseVisualStyleBackColor = true;
		this.checkBox1.CheckedChanged += new System.EventHandler(this.checkBox1_CheckedChanged);
		this.checkBox2.AutoSize = true;
		this.checkBox2.Location = new System.Drawing.Point(264, 23);
		this.checkBox2.Name = "checkBox2";
		this.checkBox2.Size = new System.Drawing.Size(15, 14);
		this.checkBox2.TabIndex = 114;
		this.checkBox2.UseVisualStyleBackColor = true;
		this.checkBox2.CheckedChanged += new System.EventHandler(this.checkBox2_CheckedChanged);
		this.checkBox3.AutoSize = true;
		this.checkBox3.Location = new System.Drawing.Point(264, 42);
		this.checkBox3.Name = "checkBox3";
		this.checkBox3.Size = new System.Drawing.Size(15, 14);
		this.checkBox3.TabIndex = 115;
		this.checkBox3.UseVisualStyleBackColor = true;
		this.checkBox3.CheckedChanged += new System.EventHandler(this.checkBox3_CheckedChanged);
		this.checkBox4.AutoSize = true;
		this.checkBox4.Location = new System.Drawing.Point(264, 61);
		this.checkBox4.Name = "checkBox4";
		this.checkBox4.Size = new System.Drawing.Size(15, 14);
		this.checkBox4.TabIndex = 116;
		this.checkBox4.UseVisualStyleBackColor = true;
		this.checkBox4.CheckedChanged += new System.EventHandler(this.checkBox4_CheckedChanged);
		this.checkBox5.AutoSize = true;
		this.checkBox5.Location = new System.Drawing.Point(264, 80);
		this.checkBox5.Name = "checkBox5";
		this.checkBox5.Size = new System.Drawing.Size(15, 14);
		this.checkBox5.TabIndex = 117;
		this.checkBox5.UseVisualStyleBackColor = true;
		this.checkBox5.CheckedChanged += new System.EventHandler(this.checkBox5_CheckedChanged);
		this.checkBox6.AutoSize = true;
		this.checkBox6.Location = new System.Drawing.Point(264, 99);
		this.checkBox6.Name = "checkBox6";
		this.checkBox6.Size = new System.Drawing.Size(15, 14);
		this.checkBox6.TabIndex = 118;
		this.checkBox6.UseVisualStyleBackColor = true;
		this.checkBox6.CheckedChanged += new System.EventHandler(this.checkBox6_CheckedChanged);
		this.checkBox7.AutoSize = true;
		this.checkBox7.Location = new System.Drawing.Point(264, 118);
		this.checkBox7.Name = "checkBox7";
		this.checkBox7.Size = new System.Drawing.Size(15, 14);
		this.checkBox7.TabIndex = 119;
		this.checkBox7.UseVisualStyleBackColor = true;
		this.checkBox7.CheckedChanged += new System.EventHandler(this.checkBox7_CheckedChanged);
		this.checkBox8.AutoSize = true;
		this.checkBox8.Location = new System.Drawing.Point(264, 251);
		this.checkBox8.Name = "checkBox8";
		this.checkBox8.Size = new System.Drawing.Size(15, 14);
		this.checkBox8.TabIndex = 126;
		this.checkBox8.UseVisualStyleBackColor = true;
		this.checkBox8.CheckedChanged += new System.EventHandler(this.checkBox8_CheckedChanged);
		this.checkBox9.AutoSize = true;
		this.checkBox9.Location = new System.Drawing.Point(264, 232);
		this.checkBox9.Name = "checkBox9";
		this.checkBox9.Size = new System.Drawing.Size(15, 14);
		this.checkBox9.TabIndex = 125;
		this.checkBox9.UseVisualStyleBackColor = true;
		this.checkBox9.CheckedChanged += new System.EventHandler(this.checkBox9_CheckedChanged);
		this.checkBox10.AutoSize = true;
		this.checkBox10.Location = new System.Drawing.Point(264, 213);
		this.checkBox10.Name = "checkBox10";
		this.checkBox10.Size = new System.Drawing.Size(15, 14);
		this.checkBox10.TabIndex = 124;
		this.checkBox10.UseVisualStyleBackColor = true;
		this.checkBox10.CheckedChanged += new System.EventHandler(this.checkBox10_CheckedChanged);
		this.checkBox11.AutoSize = true;
		this.checkBox11.Location = new System.Drawing.Point(264, 194);
		this.checkBox11.Name = "checkBox11";
		this.checkBox11.Size = new System.Drawing.Size(15, 14);
		this.checkBox11.TabIndex = 123;
		this.checkBox11.UseVisualStyleBackColor = true;
		this.checkBox11.CheckedChanged += new System.EventHandler(this.checkBox11_CheckedChanged);
		this.checkBox12.AutoSize = true;
		this.checkBox12.Location = new System.Drawing.Point(264, 175);
		this.checkBox12.Name = "checkBox12";
		this.checkBox12.Size = new System.Drawing.Size(15, 14);
		this.checkBox12.TabIndex = 122;
		this.checkBox12.UseVisualStyleBackColor = true;
		this.checkBox12.CheckedChanged += new System.EventHandler(this.checkBox12_CheckedChanged);
		this.checkBox13.AutoSize = true;
		this.checkBox13.Location = new System.Drawing.Point(264, 156);
		this.checkBox13.Name = "checkBox13";
		this.checkBox13.Size = new System.Drawing.Size(15, 14);
		this.checkBox13.TabIndex = 121;
		this.checkBox13.UseVisualStyleBackColor = true;
		this.checkBox13.CheckedChanged += new System.EventHandler(this.checkBox13_CheckedChanged);
		this.checkBox14.AutoSize = true;
		this.checkBox14.Location = new System.Drawing.Point(264, 137);
		this.checkBox14.Name = "checkBox14";
		this.checkBox14.Size = new System.Drawing.Size(15, 14);
		this.checkBox14.TabIndex = 120;
		this.checkBox14.UseVisualStyleBackColor = true;
		this.checkBox14.CheckedChanged += new System.EventHandler(this.checkBox14_CheckedChanged);
		this.checkBox15.AutoSize = true;
		this.checkBox15.Location = new System.Drawing.Point(264, 384);
		this.checkBox15.Name = "checkBox15";
		this.checkBox15.Size = new System.Drawing.Size(15, 14);
		this.checkBox15.TabIndex = 133;
		this.checkBox15.UseVisualStyleBackColor = true;
		this.checkBox15.CheckedChanged += new System.EventHandler(this.checkBox15_CheckedChanged);
		this.checkBox16.AutoSize = true;
		this.checkBox16.Location = new System.Drawing.Point(264, 365);
		this.checkBox16.Name = "checkBox16";
		this.checkBox16.Size = new System.Drawing.Size(15, 14);
		this.checkBox16.TabIndex = 132;
		this.checkBox16.UseVisualStyleBackColor = true;
		this.checkBox16.CheckedChanged += new System.EventHandler(this.checkBox16_CheckedChanged);
		this.checkBox17.AutoSize = true;
		this.checkBox17.Location = new System.Drawing.Point(264, 346);
		this.checkBox17.Name = "checkBox17";
		this.checkBox17.Size = new System.Drawing.Size(15, 14);
		this.checkBox17.TabIndex = 131;
		this.checkBox17.UseVisualStyleBackColor = true;
		this.checkBox17.CheckedChanged += new System.EventHandler(this.checkBox17_CheckedChanged);
		this.checkBox18.AutoSize = true;
		this.checkBox18.Location = new System.Drawing.Point(264, 327);
		this.checkBox18.Name = "checkBox18";
		this.checkBox18.Size = new System.Drawing.Size(15, 14);
		this.checkBox18.TabIndex = 130;
		this.checkBox18.UseVisualStyleBackColor = true;
		this.checkBox18.CheckedChanged += new System.EventHandler(this.checkBox18_CheckedChanged);
		this.checkBox19.AutoSize = true;
		this.checkBox19.Location = new System.Drawing.Point(264, 308);
		this.checkBox19.Name = "checkBox19";
		this.checkBox19.Size = new System.Drawing.Size(15, 14);
		this.checkBox19.TabIndex = 129;
		this.checkBox19.UseVisualStyleBackColor = true;
		this.checkBox19.CheckedChanged += new System.EventHandler(this.checkBox19_CheckedChanged);
		this.checkBox20.AutoSize = true;
		this.checkBox20.Location = new System.Drawing.Point(264, 289);
		this.checkBox20.Name = "checkBox20";
		this.checkBox20.Size = new System.Drawing.Size(15, 14);
		this.checkBox20.TabIndex = 128;
		this.checkBox20.UseVisualStyleBackColor = true;
		this.checkBox20.CheckedChanged += new System.EventHandler(this.checkBox20_CheckedChanged);
		this.checkBox21.AutoSize = true;
		this.checkBox21.Location = new System.Drawing.Point(264, 270);
		this.checkBox21.Name = "checkBox21";
		this.checkBox21.Size = new System.Drawing.Size(15, 14);
		this.checkBox21.TabIndex = 127;
		this.checkBox21.UseVisualStyleBackColor = true;
		this.checkBox21.CheckedChanged += new System.EventHandler(this.checkBox21_CheckedChanged);
		this.checkBox23.AutoSize = true;
		this.checkBox23.Location = new System.Drawing.Point(264, 498);
		this.checkBox23.Name = "checkBox23";
		this.checkBox23.Size = new System.Drawing.Size(15, 14);
		this.checkBox23.TabIndex = 139;
		this.checkBox23.UseVisualStyleBackColor = true;
		this.checkBox23.CheckedChanged += new System.EventHandler(this.checkBox23_CheckedChanged);
		this.checkBox24.AutoSize = true;
		this.checkBox24.Location = new System.Drawing.Point(264, 479);
		this.checkBox24.Name = "checkBox24";
		this.checkBox24.Size = new System.Drawing.Size(15, 14);
		this.checkBox24.TabIndex = 138;
		this.checkBox24.UseVisualStyleBackColor = true;
		this.checkBox24.CheckedChanged += new System.EventHandler(this.checkBox24_CheckedChanged);
		this.checkBox25.AutoSize = true;
		this.checkBox25.Location = new System.Drawing.Point(264, 460);
		this.checkBox25.Name = "checkBox25";
		this.checkBox25.Size = new System.Drawing.Size(15, 14);
		this.checkBox25.TabIndex = 137;
		this.checkBox25.UseVisualStyleBackColor = true;
		this.checkBox25.CheckedChanged += new System.EventHandler(this.checkBox25_CheckedChanged);
		this.checkBox26.AutoSize = true;
		this.checkBox26.Location = new System.Drawing.Point(264, 441);
		this.checkBox26.Name = "checkBox26";
		this.checkBox26.Size = new System.Drawing.Size(15, 14);
		this.checkBox26.TabIndex = 136;
		this.checkBox26.UseVisualStyleBackColor = true;
		this.checkBox26.CheckedChanged += new System.EventHandler(this.checkBox26_CheckedChanged);
		this.checkBox27.AutoSize = true;
		this.checkBox27.Location = new System.Drawing.Point(264, 422);
		this.checkBox27.Name = "checkBox27";
		this.checkBox27.Size = new System.Drawing.Size(15, 14);
		this.checkBox27.TabIndex = 135;
		this.checkBox27.UseVisualStyleBackColor = true;
		this.checkBox27.CheckedChanged += new System.EventHandler(this.checkBox27_CheckedChanged);
		this.checkBox28.AutoSize = true;
		this.checkBox28.Location = new System.Drawing.Point(264, 403);
		this.checkBox28.Name = "checkBox28";
		this.checkBox28.Size = new System.Drawing.Size(15, 14);
		this.checkBox28.TabIndex = 134;
		this.checkBox28.UseVisualStyleBackColor = true;
		this.checkBox28.CheckedChanged += new System.EventHandler(this.checkBox28_CheckedChanged);
		this.textBox3.Location = new System.Drawing.Point(30, 1);
		this.textBox3.Name = "textBox3";
		this.textBox3.Size = new System.Drawing.Size(165, 20);
		this.textBox3.TabIndex = 248;
		this.textBox4.Location = new System.Drawing.Point(30, 20);
		this.textBox4.Name = "textBox4";
		this.textBox4.Size = new System.Drawing.Size(165, 20);
		this.textBox4.TabIndex = 249;
		this.textBox5.Location = new System.Drawing.Point(30, 39);
		this.textBox5.Name = "textBox5";
		this.textBox5.Size = new System.Drawing.Size(165, 20);
		this.textBox5.TabIndex = 250;
		this.textBox6.Location = new System.Drawing.Point(30, 58);
		this.textBox6.Name = "textBox6";
		this.textBox6.Size = new System.Drawing.Size(165, 20);
		this.textBox6.TabIndex = 251;
		this.textBox7.Location = new System.Drawing.Point(30, 77);
		this.textBox7.Name = "textBox7";
		this.textBox7.Size = new System.Drawing.Size(165, 20);
		this.textBox7.TabIndex = 252;
		this.textBox8.Location = new System.Drawing.Point(30, 96);
		this.textBox8.Name = "textBox8";
		this.textBox8.Size = new System.Drawing.Size(165, 20);
		this.textBox8.TabIndex = 253;
		this.textBox9.Location = new System.Drawing.Point(30, 115);
		this.textBox9.Name = "textBox9";
		this.textBox9.Size = new System.Drawing.Size(165, 20);
		this.textBox9.TabIndex = 254;
		this.textBox10.Location = new System.Drawing.Point(30, 134);
		this.textBox10.Name = "textBox10";
		this.textBox10.Size = new System.Drawing.Size(165, 20);
		this.textBox10.TabIndex = 255;
		this.textBox11.Location = new System.Drawing.Point(30, 286);
		this.textBox11.Name = "textBox11";
		this.textBox11.Size = new System.Drawing.Size(165, 20);
		this.textBox11.TabIndex = 263;
		this.textBox12.Location = new System.Drawing.Point(30, 267);
		this.textBox12.Name = "textBox12";
		this.textBox12.Size = new System.Drawing.Size(165, 20);
		this.textBox12.TabIndex = 262;
		this.textBox13.Location = new System.Drawing.Point(30, 248);
		this.textBox13.Name = "textBox13";
		this.textBox13.Size = new System.Drawing.Size(165, 20);
		this.textBox13.TabIndex = 261;
		this.textBox14.Location = new System.Drawing.Point(30, 229);
		this.textBox14.Name = "textBox14";
		this.textBox14.Size = new System.Drawing.Size(165, 20);
		this.textBox14.TabIndex = 260;
		this.textBox15.Location = new System.Drawing.Point(30, 210);
		this.textBox15.Name = "textBox15";
		this.textBox15.Size = new System.Drawing.Size(165, 20);
		this.textBox15.TabIndex = 259;
		this.textBox16.Location = new System.Drawing.Point(30, 191);
		this.textBox16.Name = "textBox16";
		this.textBox16.Size = new System.Drawing.Size(165, 20);
		this.textBox16.TabIndex = 258;
		this.textBox17.Location = new System.Drawing.Point(30, 172);
		this.textBox17.Name = "textBox17";
		this.textBox17.Size = new System.Drawing.Size(165, 20);
		this.textBox17.TabIndex = 257;
		this.textBox18.Location = new System.Drawing.Point(30, 153);
		this.textBox18.Name = "textBox18";
		this.textBox18.Size = new System.Drawing.Size(165, 20);
		this.textBox18.TabIndex = 256;
		this.textBox19.Location = new System.Drawing.Point(30, 438);
		this.textBox19.Name = "textBox19";
		this.textBox19.Size = new System.Drawing.Size(165, 20);
		this.textBox19.TabIndex = 271;
		this.textBox20.Location = new System.Drawing.Point(30, 419);
		this.textBox20.Name = "textBox20";
		this.textBox20.Size = new System.Drawing.Size(165, 20);
		this.textBox20.TabIndex = 270;
		this.textBox21.Location = new System.Drawing.Point(30, 400);
		this.textBox21.Name = "textBox21";
		this.textBox21.Size = new System.Drawing.Size(165, 20);
		this.textBox21.TabIndex = 269;
		this.textBox22.Location = new System.Drawing.Point(30, 381);
		this.textBox22.Name = "textBox22";
		this.textBox22.Size = new System.Drawing.Size(165, 20);
		this.textBox22.TabIndex = 268;
		this.textBox23.Location = new System.Drawing.Point(30, 362);
		this.textBox23.Name = "textBox23";
		this.textBox23.Size = new System.Drawing.Size(165, 20);
		this.textBox23.TabIndex = 267;
		this.textBox24.Location = new System.Drawing.Point(30, 343);
		this.textBox24.Name = "textBox24";
		this.textBox24.Size = new System.Drawing.Size(165, 20);
		this.textBox24.TabIndex = 266;
		this.textBox25.Location = new System.Drawing.Point(30, 324);
		this.textBox25.Name = "textBox25";
		this.textBox25.Size = new System.Drawing.Size(165, 20);
		this.textBox25.TabIndex = 265;
		this.textBox26.Location = new System.Drawing.Point(30, 305);
		this.textBox26.Name = "textBox26";
		this.textBox26.Size = new System.Drawing.Size(165, 20);
		this.textBox26.TabIndex = 264;
		this.textBox32.Location = new System.Drawing.Point(30, 495);
		this.textBox32.Name = "textBox32";
		this.textBox32.Size = new System.Drawing.Size(165, 20);
		this.textBox32.TabIndex = 274;
		this.textBox33.Location = new System.Drawing.Point(30, 476);
		this.textBox33.Name = "textBox33";
		this.textBox33.Size = new System.Drawing.Size(165, 20);
		this.textBox33.TabIndex = 273;
		this.textBox34.Location = new System.Drawing.Point(30, 457);
		this.textBox34.Name = "textBox34";
		this.textBox34.Size = new System.Drawing.Size(165, 20);
		this.textBox34.TabIndex = 272;
		this.textBox27.Location = new System.Drawing.Point(336, 495);
		this.textBox27.Name = "textBox27";
		this.textBox27.Size = new System.Drawing.Size(165, 20);
		this.textBox27.TabIndex = 409;
		this.textBox28.Location = new System.Drawing.Point(336, 476);
		this.textBox28.Name = "textBox28";
		this.textBox28.Size = new System.Drawing.Size(165, 20);
		this.textBox28.TabIndex = 408;
		this.textBox29.Location = new System.Drawing.Point(336, 457);
		this.textBox29.Name = "textBox29";
		this.textBox29.Size = new System.Drawing.Size(165, 20);
		this.textBox29.TabIndex = 407;
		this.textBox30.Location = new System.Drawing.Point(336, 438);
		this.textBox30.Name = "textBox30";
		this.textBox30.Size = new System.Drawing.Size(165, 20);
		this.textBox30.TabIndex = 406;
		this.textBox31.Location = new System.Drawing.Point(336, 419);
		this.textBox31.Name = "textBox31";
		this.textBox31.Size = new System.Drawing.Size(165, 20);
		this.textBox31.TabIndex = 405;
		this.textBox35.Location = new System.Drawing.Point(336, 400);
		this.textBox35.Name = "textBox35";
		this.textBox35.Size = new System.Drawing.Size(165, 20);
		this.textBox35.TabIndex = 404;
		this.textBox36.Location = new System.Drawing.Point(336, 381);
		this.textBox36.Name = "textBox36";
		this.textBox36.Size = new System.Drawing.Size(165, 20);
		this.textBox36.TabIndex = 403;
		this.textBox37.Location = new System.Drawing.Point(336, 362);
		this.textBox37.Name = "textBox37";
		this.textBox37.Size = new System.Drawing.Size(165, 20);
		this.textBox37.TabIndex = 402;
		this.textBox38.Location = new System.Drawing.Point(336, 343);
		this.textBox38.Name = "textBox38";
		this.textBox38.Size = new System.Drawing.Size(165, 20);
		this.textBox38.TabIndex = 401;
		this.textBox39.Location = new System.Drawing.Point(336, 324);
		this.textBox39.Name = "textBox39";
		this.textBox39.Size = new System.Drawing.Size(165, 20);
		this.textBox39.TabIndex = 400;
		this.textBox40.Location = new System.Drawing.Point(336, 305);
		this.textBox40.Name = "textBox40";
		this.textBox40.Size = new System.Drawing.Size(165, 20);
		this.textBox40.TabIndex = 399;
		this.textBox41.Location = new System.Drawing.Point(336, 286);
		this.textBox41.Name = "textBox41";
		this.textBox41.Size = new System.Drawing.Size(165, 20);
		this.textBox41.TabIndex = 398;
		this.textBox42.Location = new System.Drawing.Point(336, 267);
		this.textBox42.Name = "textBox42";
		this.textBox42.Size = new System.Drawing.Size(165, 20);
		this.textBox42.TabIndex = 397;
		this.textBox43.Location = new System.Drawing.Point(336, 248);
		this.textBox43.Name = "textBox43";
		this.textBox43.Size = new System.Drawing.Size(165, 20);
		this.textBox43.TabIndex = 396;
		this.textBox44.Location = new System.Drawing.Point(336, 229);
		this.textBox44.Name = "textBox44";
		this.textBox44.Size = new System.Drawing.Size(165, 20);
		this.textBox44.TabIndex = 395;
		this.textBox45.Location = new System.Drawing.Point(336, 210);
		this.textBox45.Name = "textBox45";
		this.textBox45.Size = new System.Drawing.Size(165, 20);
		this.textBox45.TabIndex = 394;
		this.textBox46.Location = new System.Drawing.Point(336, 191);
		this.textBox46.Name = "textBox46";
		this.textBox46.Size = new System.Drawing.Size(165, 20);
		this.textBox46.TabIndex = 393;
		this.textBox47.Location = new System.Drawing.Point(336, 172);
		this.textBox47.Name = "textBox47";
		this.textBox47.Size = new System.Drawing.Size(165, 20);
		this.textBox47.TabIndex = 392;
		this.textBox48.Location = new System.Drawing.Point(336, 153);
		this.textBox48.Name = "textBox48";
		this.textBox48.Size = new System.Drawing.Size(165, 20);
		this.textBox48.TabIndex = 391;
		this.textBox49.Location = new System.Drawing.Point(336, 134);
		this.textBox49.Name = "textBox49";
		this.textBox49.Size = new System.Drawing.Size(165, 20);
		this.textBox49.TabIndex = 390;
		this.textBox50.Location = new System.Drawing.Point(336, 115);
		this.textBox50.Name = "textBox50";
		this.textBox50.Size = new System.Drawing.Size(165, 20);
		this.textBox50.TabIndex = 389;
		this.textBox51.Location = new System.Drawing.Point(336, 96);
		this.textBox51.Name = "textBox51";
		this.textBox51.Size = new System.Drawing.Size(165, 20);
		this.textBox51.TabIndex = 388;
		this.textBox52.Location = new System.Drawing.Point(336, 77);
		this.textBox52.Name = "textBox52";
		this.textBox52.Size = new System.Drawing.Size(165, 20);
		this.textBox52.TabIndex = 387;
		this.textBox53.Location = new System.Drawing.Point(336, 58);
		this.textBox53.Name = "textBox53";
		this.textBox53.Size = new System.Drawing.Size(165, 20);
		this.textBox53.TabIndex = 386;
		this.textBox54.Location = new System.Drawing.Point(336, 39);
		this.textBox54.Name = "textBox54";
		this.textBox54.Size = new System.Drawing.Size(165, 20);
		this.textBox54.TabIndex = 385;
		this.textBox55.Location = new System.Drawing.Point(336, 20);
		this.textBox55.Name = "textBox55";
		this.textBox55.Size = new System.Drawing.Size(165, 20);
		this.textBox55.TabIndex = 384;
		this.textBox56.Location = new System.Drawing.Point(336, 1);
		this.textBox56.Name = "textBox56";
		this.textBox56.Size = new System.Drawing.Size(165, 20);
		this.textBox56.TabIndex = 383;
		this.checkBox22.AutoSize = true;
		this.checkBox22.Location = new System.Drawing.Point(570, 498);
		this.checkBox22.Name = "checkBox22";
		this.checkBox22.Size = new System.Drawing.Size(15, 14);
		this.checkBox22.TabIndex = 382;
		this.checkBox22.UseVisualStyleBackColor = true;
		this.checkBox22.CheckedChanged += new System.EventHandler(this.checkBox22_CheckedChanged);
		this.checkBox29.AutoSize = true;
		this.checkBox29.Location = new System.Drawing.Point(570, 479);
		this.checkBox29.Name = "checkBox29";
		this.checkBox29.Size = new System.Drawing.Size(15, 14);
		this.checkBox29.TabIndex = 381;
		this.checkBox29.UseVisualStyleBackColor = true;
		this.checkBox29.CheckedChanged += new System.EventHandler(this.checkBox29_CheckedChanged);
		this.checkBox30.AutoSize = true;
		this.checkBox30.Location = new System.Drawing.Point(570, 460);
		this.checkBox30.Name = "checkBox30";
		this.checkBox30.Size = new System.Drawing.Size(15, 14);
		this.checkBox30.TabIndex = 380;
		this.checkBox30.UseVisualStyleBackColor = true;
		this.checkBox30.CheckedChanged += new System.EventHandler(this.checkBox30_CheckedChanged);
		this.checkBox31.AutoSize = true;
		this.checkBox31.Location = new System.Drawing.Point(570, 441);
		this.checkBox31.Name = "checkBox31";
		this.checkBox31.Size = new System.Drawing.Size(15, 14);
		this.checkBox31.TabIndex = 379;
		this.checkBox31.UseVisualStyleBackColor = true;
		this.checkBox31.CheckedChanged += new System.EventHandler(this.checkBox31_CheckedChanged);
		this.checkBox32.AutoSize = true;
		this.checkBox32.Location = new System.Drawing.Point(570, 422);
		this.checkBox32.Name = "checkBox32";
		this.checkBox32.Size = new System.Drawing.Size(15, 14);
		this.checkBox32.TabIndex = 378;
		this.checkBox32.UseVisualStyleBackColor = true;
		this.checkBox32.CheckedChanged += new System.EventHandler(this.checkBox32_CheckedChanged);
		this.checkBox33.AutoSize = true;
		this.checkBox33.Location = new System.Drawing.Point(570, 403);
		this.checkBox33.Name = "checkBox33";
		this.checkBox33.Size = new System.Drawing.Size(15, 14);
		this.checkBox33.TabIndex = 377;
		this.checkBox33.UseVisualStyleBackColor = true;
		this.checkBox33.CheckedChanged += new System.EventHandler(this.checkBox33_CheckedChanged);
		this.checkBox34.AutoSize = true;
		this.checkBox34.Location = new System.Drawing.Point(570, 384);
		this.checkBox34.Name = "checkBox34";
		this.checkBox34.Size = new System.Drawing.Size(15, 14);
		this.checkBox34.TabIndex = 376;
		this.checkBox34.UseVisualStyleBackColor = true;
		this.checkBox34.CheckedChanged += new System.EventHandler(this.checkBox34_CheckedChanged);
		this.checkBox35.AutoSize = true;
		this.checkBox35.Location = new System.Drawing.Point(570, 365);
		this.checkBox35.Name = "checkBox35";
		this.checkBox35.Size = new System.Drawing.Size(15, 14);
		this.checkBox35.TabIndex = 375;
		this.checkBox35.UseVisualStyleBackColor = true;
		this.checkBox35.CheckedChanged += new System.EventHandler(this.checkBox35_CheckedChanged);
		this.checkBox36.AutoSize = true;
		this.checkBox36.Location = new System.Drawing.Point(570, 346);
		this.checkBox36.Name = "checkBox36";
		this.checkBox36.Size = new System.Drawing.Size(15, 14);
		this.checkBox36.TabIndex = 374;
		this.checkBox36.UseVisualStyleBackColor = true;
		this.checkBox36.CheckedChanged += new System.EventHandler(this.checkBox36_CheckedChanged);
		this.checkBox37.AutoSize = true;
		this.checkBox37.Location = new System.Drawing.Point(570, 327);
		this.checkBox37.Name = "checkBox37";
		this.checkBox37.Size = new System.Drawing.Size(15, 14);
		this.checkBox37.TabIndex = 373;
		this.checkBox37.UseVisualStyleBackColor = true;
		this.checkBox37.CheckedChanged += new System.EventHandler(this.checkBox37_CheckedChanged);
		this.checkBox38.AutoSize = true;
		this.checkBox38.Location = new System.Drawing.Point(570, 308);
		this.checkBox38.Name = "checkBox38";
		this.checkBox38.Size = new System.Drawing.Size(15, 14);
		this.checkBox38.TabIndex = 372;
		this.checkBox38.UseVisualStyleBackColor = true;
		this.checkBox38.CheckedChanged += new System.EventHandler(this.checkBox38_CheckedChanged);
		this.checkBox39.AutoSize = true;
		this.checkBox39.Location = new System.Drawing.Point(570, 289);
		this.checkBox39.Name = "checkBox39";
		this.checkBox39.Size = new System.Drawing.Size(15, 14);
		this.checkBox39.TabIndex = 371;
		this.checkBox39.UseVisualStyleBackColor = true;
		this.checkBox39.CheckedChanged += new System.EventHandler(this.checkBox39_CheckedChanged);
		this.checkBox40.AutoSize = true;
		this.checkBox40.Location = new System.Drawing.Point(570, 270);
		this.checkBox40.Name = "checkBox40";
		this.checkBox40.Size = new System.Drawing.Size(15, 14);
		this.checkBox40.TabIndex = 370;
		this.checkBox40.UseVisualStyleBackColor = true;
		this.checkBox40.CheckedChanged += new System.EventHandler(this.checkBox40_CheckedChanged);
		this.checkBox41.AutoSize = true;
		this.checkBox41.Location = new System.Drawing.Point(570, 251);
		this.checkBox41.Name = "checkBox41";
		this.checkBox41.Size = new System.Drawing.Size(15, 14);
		this.checkBox41.TabIndex = 369;
		this.checkBox41.UseVisualStyleBackColor = true;
		this.checkBox41.CheckedChanged += new System.EventHandler(this.checkBox41_CheckedChanged);
		this.checkBox42.AutoSize = true;
		this.checkBox42.Location = new System.Drawing.Point(570, 232);
		this.checkBox42.Name = "checkBox42";
		this.checkBox42.Size = new System.Drawing.Size(15, 14);
		this.checkBox42.TabIndex = 368;
		this.checkBox42.UseVisualStyleBackColor = true;
		this.checkBox42.CheckedChanged += new System.EventHandler(this.checkBox42_CheckedChanged);
		this.checkBox43.AutoSize = true;
		this.checkBox43.Location = new System.Drawing.Point(570, 213);
		this.checkBox43.Name = "checkBox43";
		this.checkBox43.Size = new System.Drawing.Size(15, 14);
		this.checkBox43.TabIndex = 367;
		this.checkBox43.UseVisualStyleBackColor = true;
		this.checkBox43.CheckedChanged += new System.EventHandler(this.checkBox43_CheckedChanged);
		this.checkBox44.AutoSize = true;
		this.checkBox44.Location = new System.Drawing.Point(570, 194);
		this.checkBox44.Name = "checkBox44";
		this.checkBox44.Size = new System.Drawing.Size(15, 14);
		this.checkBox44.TabIndex = 366;
		this.checkBox44.UseVisualStyleBackColor = true;
		this.checkBox44.CheckedChanged += new System.EventHandler(this.checkBox44_CheckedChanged);
		this.checkBox45.AutoSize = true;
		this.checkBox45.Location = new System.Drawing.Point(570, 175);
		this.checkBox45.Name = "checkBox45";
		this.checkBox45.Size = new System.Drawing.Size(15, 14);
		this.checkBox45.TabIndex = 365;
		this.checkBox45.UseVisualStyleBackColor = true;
		this.checkBox45.CheckedChanged += new System.EventHandler(this.checkBox45_CheckedChanged);
		this.checkBox46.AutoSize = true;
		this.checkBox46.Location = new System.Drawing.Point(570, 156);
		this.checkBox46.Name = "checkBox46";
		this.checkBox46.Size = new System.Drawing.Size(15, 14);
		this.checkBox46.TabIndex = 364;
		this.checkBox46.UseVisualStyleBackColor = true;
		this.checkBox46.CheckedChanged += new System.EventHandler(this.checkBox46_CheckedChanged);
		this.checkBox47.AutoSize = true;
		this.checkBox47.Location = new System.Drawing.Point(570, 137);
		this.checkBox47.Name = "checkBox47";
		this.checkBox47.Size = new System.Drawing.Size(15, 14);
		this.checkBox47.TabIndex = 363;
		this.checkBox47.UseVisualStyleBackColor = true;
		this.checkBox47.CheckedChanged += new System.EventHandler(this.checkBox47_CheckedChanged);
		this.checkBox48.AutoSize = true;
		this.checkBox48.Location = new System.Drawing.Point(570, 118);
		this.checkBox48.Name = "checkBox48";
		this.checkBox48.Size = new System.Drawing.Size(15, 14);
		this.checkBox48.TabIndex = 362;
		this.checkBox48.UseVisualStyleBackColor = true;
		this.checkBox48.CheckedChanged += new System.EventHandler(this.checkBox48_CheckedChanged);
		this.checkBox49.AutoSize = true;
		this.checkBox49.Location = new System.Drawing.Point(570, 99);
		this.checkBox49.Name = "checkBox49";
		this.checkBox49.Size = new System.Drawing.Size(15, 14);
		this.checkBox49.TabIndex = 361;
		this.checkBox49.UseVisualStyleBackColor = true;
		this.checkBox49.CheckedChanged += new System.EventHandler(this.checkBox49_CheckedChanged);
		this.checkBox50.AutoSize = true;
		this.checkBox50.Location = new System.Drawing.Point(570, 80);
		this.checkBox50.Name = "checkBox50";
		this.checkBox50.Size = new System.Drawing.Size(15, 14);
		this.checkBox50.TabIndex = 360;
		this.checkBox50.UseVisualStyleBackColor = true;
		this.checkBox50.CheckedChanged += new System.EventHandler(this.checkBox50_CheckedChanged);
		this.checkBox51.AutoSize = true;
		this.checkBox51.Location = new System.Drawing.Point(570, 61);
		this.checkBox51.Name = "checkBox51";
		this.checkBox51.Size = new System.Drawing.Size(15, 14);
		this.checkBox51.TabIndex = 359;
		this.checkBox51.UseVisualStyleBackColor = true;
		this.checkBox51.CheckedChanged += new System.EventHandler(this.checkBox51_CheckedChanged);
		this.checkBox52.AutoSize = true;
		this.checkBox52.Location = new System.Drawing.Point(570, 42);
		this.checkBox52.Name = "checkBox52";
		this.checkBox52.Size = new System.Drawing.Size(15, 14);
		this.checkBox52.TabIndex = 358;
		this.checkBox52.UseVisualStyleBackColor = true;
		this.checkBox52.CheckedChanged += new System.EventHandler(this.checkBox52_CheckedChanged);
		this.checkBox53.AutoSize = true;
		this.checkBox53.Location = new System.Drawing.Point(570, 23);
		this.checkBox53.Name = "checkBox53";
		this.checkBox53.Size = new System.Drawing.Size(15, 14);
		this.checkBox53.TabIndex = 357;
		this.checkBox53.UseVisualStyleBackColor = true;
		this.checkBox53.CheckedChanged += new System.EventHandler(this.checkBox53_CheckedChanged);
		this.checkBox54.AutoSize = true;
		this.checkBox54.Location = new System.Drawing.Point(570, 4);
		this.checkBox54.Name = "checkBox54";
		this.checkBox54.Size = new System.Drawing.Size(15, 14);
		this.checkBox54.TabIndex = 356;
		this.checkBox54.UseVisualStyleBackColor = true;
		this.checkBox54.CheckedChanged += new System.EventHandler(this.checkBox54_CheckedChanged);
		this.numericUpDown55.Location = new System.Drawing.Point(539, 495);
		this.numericUpDown55.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown55.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown55.Name = "numericUpDown55";
		this.numericUpDown55.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown55.TabIndex = 355;
		this.numericUpDown55.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown56.Location = new System.Drawing.Point(500, 495);
		this.numericUpDown56.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown56.Name = "numericUpDown56";
		this.numericUpDown56.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown56.TabIndex = 354;
		this.label29.AutoSize = true;
		this.label29.Location = new System.Drawing.Point(306, 498);
		this.label29.Name = "label29";
		this.label29.Size = new System.Drawing.Size(30, 13);
		this.label29.TabIndex = 353;
		this.label29.Text = "№53";
		this.numericUpDown57.Location = new System.Drawing.Point(539, 476);
		this.numericUpDown57.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown57.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown57.Name = "numericUpDown57";
		this.numericUpDown57.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown57.TabIndex = 352;
		this.numericUpDown57.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown58.Location = new System.Drawing.Point(500, 476);
		this.numericUpDown58.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown58.Name = "numericUpDown58";
		this.numericUpDown58.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown58.TabIndex = 351;
		this.label30.AutoSize = true;
		this.label30.Location = new System.Drawing.Point(306, 480);
		this.label30.Name = "label30";
		this.label30.Size = new System.Drawing.Size(30, 13);
		this.label30.TabIndex = 350;
		this.label30.Text = "№52";
		this.numericUpDown59.Location = new System.Drawing.Point(539, 457);
		this.numericUpDown59.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown59.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown59.Name = "numericUpDown59";
		this.numericUpDown59.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown59.TabIndex = 349;
		this.numericUpDown59.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown60.Location = new System.Drawing.Point(500, 457);
		this.numericUpDown60.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown60.Name = "numericUpDown60";
		this.numericUpDown60.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown60.TabIndex = 348;
		this.label31.AutoSize = true;
		this.label31.Location = new System.Drawing.Point(306, 461);
		this.label31.Name = "label31";
		this.label31.Size = new System.Drawing.Size(30, 13);
		this.label31.TabIndex = 347;
		this.label31.Text = "№51";
		this.numericUpDown61.Location = new System.Drawing.Point(539, 438);
		this.numericUpDown61.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown61.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown61.Name = "numericUpDown61";
		this.numericUpDown61.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown61.TabIndex = 346;
		this.numericUpDown61.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown62.Location = new System.Drawing.Point(500, 438);
		this.numericUpDown62.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown62.Name = "numericUpDown62";
		this.numericUpDown62.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown62.TabIndex = 345;
		this.label32.AutoSize = true;
		this.label32.Location = new System.Drawing.Point(306, 442);
		this.label32.Name = "label32";
		this.label32.Size = new System.Drawing.Size(30, 13);
		this.label32.TabIndex = 344;
		this.label32.Text = "№50";
		this.numericUpDown63.Location = new System.Drawing.Point(539, 419);
		this.numericUpDown63.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown63.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown63.Name = "numericUpDown63";
		this.numericUpDown63.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown63.TabIndex = 343;
		this.numericUpDown63.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown64.Location = new System.Drawing.Point(500, 419);
		this.numericUpDown64.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown64.Name = "numericUpDown64";
		this.numericUpDown64.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown64.TabIndex = 342;
		this.label33.AutoSize = true;
		this.label33.Location = new System.Drawing.Point(306, 423);
		this.label33.Name = "label33";
		this.label33.Size = new System.Drawing.Size(30, 13);
		this.label33.TabIndex = 341;
		this.label33.Text = "№49";
		this.numericUpDown65.Location = new System.Drawing.Point(539, 400);
		this.numericUpDown65.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown65.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown65.Name = "numericUpDown65";
		this.numericUpDown65.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown65.TabIndex = 340;
		this.numericUpDown65.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown66.Location = new System.Drawing.Point(500, 400);
		this.numericUpDown66.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown66.Name = "numericUpDown66";
		this.numericUpDown66.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown66.TabIndex = 339;
		this.label34.AutoSize = true;
		this.label34.Location = new System.Drawing.Point(306, 404);
		this.label34.Name = "label34";
		this.label34.Size = new System.Drawing.Size(30, 13);
		this.label34.TabIndex = 338;
		this.label34.Text = "№48";
		this.numericUpDown67.Location = new System.Drawing.Point(539, 381);
		this.numericUpDown67.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown67.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown67.Name = "numericUpDown67";
		this.numericUpDown67.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown67.TabIndex = 337;
		this.numericUpDown67.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown68.Location = new System.Drawing.Point(500, 381);
		this.numericUpDown68.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown68.Name = "numericUpDown68";
		this.numericUpDown68.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown68.TabIndex = 336;
		this.label35.AutoSize = true;
		this.label35.Location = new System.Drawing.Point(306, 385);
		this.label35.Name = "label35";
		this.label35.Size = new System.Drawing.Size(30, 13);
		this.label35.TabIndex = 335;
		this.label35.Text = "№47";
		this.numericUpDown69.Location = new System.Drawing.Point(539, 362);
		this.numericUpDown69.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown69.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown69.Name = "numericUpDown69";
		this.numericUpDown69.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown69.TabIndex = 334;
		this.numericUpDown69.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown70.Location = new System.Drawing.Point(500, 362);
		this.numericUpDown70.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown70.Name = "numericUpDown70";
		this.numericUpDown70.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown70.TabIndex = 333;
		this.label36.AutoSize = true;
		this.label36.Location = new System.Drawing.Point(306, 366);
		this.label36.Name = "label36";
		this.label36.Size = new System.Drawing.Size(30, 13);
		this.label36.TabIndex = 332;
		this.label36.Text = "№46";
		this.numericUpDown71.Location = new System.Drawing.Point(539, 343);
		this.numericUpDown71.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown71.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown71.Name = "numericUpDown71";
		this.numericUpDown71.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown71.TabIndex = 331;
		this.numericUpDown71.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown72.Location = new System.Drawing.Point(500, 343);
		this.numericUpDown72.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown72.Name = "numericUpDown72";
		this.numericUpDown72.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown72.TabIndex = 330;
		this.label37.AutoSize = true;
		this.label37.Location = new System.Drawing.Point(306, 346);
		this.label37.Name = "label37";
		this.label37.Size = new System.Drawing.Size(30, 13);
		this.label37.TabIndex = 329;
		this.label37.Text = "№45";
		this.numericUpDown73.Location = new System.Drawing.Point(539, 324);
		this.numericUpDown73.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown73.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown73.Name = "numericUpDown73";
		this.numericUpDown73.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown73.TabIndex = 328;
		this.numericUpDown73.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown74.Location = new System.Drawing.Point(500, 324);
		this.numericUpDown74.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown74.Name = "numericUpDown74";
		this.numericUpDown74.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown74.TabIndex = 327;
		this.label38.AutoSize = true;
		this.label38.Location = new System.Drawing.Point(306, 328);
		this.label38.Name = "label38";
		this.label38.Size = new System.Drawing.Size(30, 13);
		this.label38.TabIndex = 326;
		this.label38.Text = "№44";
		this.numericUpDown75.Location = new System.Drawing.Point(539, 305);
		this.numericUpDown75.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown75.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown75.Name = "numericUpDown75";
		this.numericUpDown75.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown75.TabIndex = 325;
		this.numericUpDown75.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown76.Location = new System.Drawing.Point(500, 305);
		this.numericUpDown76.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown76.Name = "numericUpDown76";
		this.numericUpDown76.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown76.TabIndex = 324;
		this.label39.AutoSize = true;
		this.label39.Location = new System.Drawing.Point(306, 309);
		this.label39.Name = "label39";
		this.label39.Size = new System.Drawing.Size(30, 13);
		this.label39.TabIndex = 323;
		this.label39.Text = "№43";
		this.numericUpDown77.Location = new System.Drawing.Point(539, 286);
		this.numericUpDown77.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown77.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown77.Name = "numericUpDown77";
		this.numericUpDown77.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown77.TabIndex = 322;
		this.numericUpDown77.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown78.Location = new System.Drawing.Point(500, 286);
		this.numericUpDown78.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown78.Name = "numericUpDown78";
		this.numericUpDown78.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown78.TabIndex = 321;
		this.label40.AutoSize = true;
		this.label40.Location = new System.Drawing.Point(306, 290);
		this.label40.Name = "label40";
		this.label40.Size = new System.Drawing.Size(30, 13);
		this.label40.TabIndex = 320;
		this.label40.Text = "№42";
		this.numericUpDown79.Location = new System.Drawing.Point(539, 267);
		this.numericUpDown79.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown79.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown79.Name = "numericUpDown79";
		this.numericUpDown79.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown79.TabIndex = 319;
		this.numericUpDown79.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown80.Location = new System.Drawing.Point(500, 267);
		this.numericUpDown80.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown80.Name = "numericUpDown80";
		this.numericUpDown80.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown80.TabIndex = 318;
		this.label41.AutoSize = true;
		this.label41.Location = new System.Drawing.Point(306, 270);
		this.label41.Name = "label41";
		this.label41.Size = new System.Drawing.Size(30, 13);
		this.label41.TabIndex = 317;
		this.label41.Text = "№41";
		this.numericUpDown81.Location = new System.Drawing.Point(539, 248);
		this.numericUpDown81.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown81.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown81.Name = "numericUpDown81";
		this.numericUpDown81.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown81.TabIndex = 316;
		this.numericUpDown81.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown82.Location = new System.Drawing.Point(500, 248);
		this.numericUpDown82.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown82.Name = "numericUpDown82";
		this.numericUpDown82.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown82.TabIndex = 315;
		this.label42.AutoSize = true;
		this.label42.Location = new System.Drawing.Point(306, 251);
		this.label42.Name = "label42";
		this.label42.Size = new System.Drawing.Size(30, 13);
		this.label42.TabIndex = 314;
		this.label42.Text = "№40";
		this.numericUpDown83.Location = new System.Drawing.Point(539, 229);
		this.numericUpDown83.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown83.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown83.Name = "numericUpDown83";
		this.numericUpDown83.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown83.TabIndex = 313;
		this.numericUpDown83.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown84.Location = new System.Drawing.Point(500, 229);
		this.numericUpDown84.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown84.Name = "numericUpDown84";
		this.numericUpDown84.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown84.TabIndex = 312;
		this.label43.AutoSize = true;
		this.label43.Location = new System.Drawing.Point(306, 231);
		this.label43.Name = "label43";
		this.label43.Size = new System.Drawing.Size(30, 13);
		this.label43.TabIndex = 311;
		this.label43.Text = "№39";
		this.numericUpDown85.Location = new System.Drawing.Point(539, 210);
		this.numericUpDown85.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown85.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown85.Name = "numericUpDown85";
		this.numericUpDown85.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown85.TabIndex = 310;
		this.numericUpDown85.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown86.Location = new System.Drawing.Point(500, 210);
		this.numericUpDown86.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown86.Name = "numericUpDown86";
		this.numericUpDown86.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown86.TabIndex = 309;
		this.label44.AutoSize = true;
		this.label44.Location = new System.Drawing.Point(306, 212);
		this.label44.Name = "label44";
		this.label44.Size = new System.Drawing.Size(30, 13);
		this.label44.TabIndex = 308;
		this.label44.Text = "№38";
		this.numericUpDown87.Location = new System.Drawing.Point(539, 191);
		this.numericUpDown87.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown87.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown87.Name = "numericUpDown87";
		this.numericUpDown87.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown87.TabIndex = 307;
		this.numericUpDown87.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown88.Location = new System.Drawing.Point(500, 191);
		this.numericUpDown88.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown88.Name = "numericUpDown88";
		this.numericUpDown88.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown88.TabIndex = 306;
		this.label45.AutoSize = true;
		this.label45.Location = new System.Drawing.Point(306, 193);
		this.label45.Name = "label45";
		this.label45.Size = new System.Drawing.Size(30, 13);
		this.label45.TabIndex = 305;
		this.label45.Text = "№37";
		this.numericUpDown89.Location = new System.Drawing.Point(539, 172);
		this.numericUpDown89.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown89.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown89.Name = "numericUpDown89";
		this.numericUpDown89.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown89.TabIndex = 304;
		this.numericUpDown89.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown90.Location = new System.Drawing.Point(500, 172);
		this.numericUpDown90.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown90.Name = "numericUpDown90";
		this.numericUpDown90.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown90.TabIndex = 303;
		this.label46.AutoSize = true;
		this.label46.Location = new System.Drawing.Point(306, 174);
		this.label46.Name = "label46";
		this.label46.Size = new System.Drawing.Size(30, 13);
		this.label46.TabIndex = 302;
		this.label46.Text = "№36";
		this.numericUpDown91.Location = new System.Drawing.Point(539, 153);
		this.numericUpDown91.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown91.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown91.Name = "numericUpDown91";
		this.numericUpDown91.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown91.TabIndex = 301;
		this.numericUpDown91.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown92.Location = new System.Drawing.Point(500, 153);
		this.numericUpDown92.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown92.Name = "numericUpDown92";
		this.numericUpDown92.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown92.TabIndex = 300;
		this.label47.AutoSize = true;
		this.label47.Location = new System.Drawing.Point(306, 155);
		this.label47.Name = "label47";
		this.label47.Size = new System.Drawing.Size(30, 13);
		this.label47.TabIndex = 299;
		this.label47.Text = "№35";
		this.numericUpDown93.Location = new System.Drawing.Point(539, 134);
		this.numericUpDown93.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown93.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown93.Name = "numericUpDown93";
		this.numericUpDown93.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown93.TabIndex = 298;
		this.numericUpDown93.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown94.Location = new System.Drawing.Point(500, 134);
		this.numericUpDown94.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown94.Name = "numericUpDown94";
		this.numericUpDown94.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown94.TabIndex = 297;
		this.label48.AutoSize = true;
		this.label48.Location = new System.Drawing.Point(306, 136);
		this.label48.Name = "label48";
		this.label48.Size = new System.Drawing.Size(30, 13);
		this.label48.TabIndex = 296;
		this.label48.Text = "№34";
		this.numericUpDown95.Location = new System.Drawing.Point(539, 115);
		this.numericUpDown95.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown95.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown95.Name = "numericUpDown95";
		this.numericUpDown95.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown95.TabIndex = 295;
		this.numericUpDown95.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown96.Location = new System.Drawing.Point(500, 115);
		this.numericUpDown96.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown96.Name = "numericUpDown96";
		this.numericUpDown96.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown96.TabIndex = 294;
		this.label49.AutoSize = true;
		this.label49.Location = new System.Drawing.Point(306, 117);
		this.label49.Name = "label49";
		this.label49.Size = new System.Drawing.Size(30, 13);
		this.label49.TabIndex = 293;
		this.label49.Text = "№33";
		this.numericUpDown97.Location = new System.Drawing.Point(539, 96);
		this.numericUpDown97.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown97.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown97.Name = "numericUpDown97";
		this.numericUpDown97.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown97.TabIndex = 292;
		this.numericUpDown97.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown98.Location = new System.Drawing.Point(500, 96);
		this.numericUpDown98.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown98.Name = "numericUpDown98";
		this.numericUpDown98.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown98.TabIndex = 291;
		this.label50.AutoSize = true;
		this.label50.Location = new System.Drawing.Point(306, 98);
		this.label50.Name = "label50";
		this.label50.Size = new System.Drawing.Size(30, 13);
		this.label50.TabIndex = 290;
		this.label50.Text = "№32";
		this.numericUpDown99.Location = new System.Drawing.Point(539, 77);
		this.numericUpDown99.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown99.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown99.Name = "numericUpDown99";
		this.numericUpDown99.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown99.TabIndex = 289;
		this.numericUpDown99.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown100.Location = new System.Drawing.Point(500, 77);
		this.numericUpDown100.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown100.Name = "numericUpDown100";
		this.numericUpDown100.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown100.TabIndex = 288;
		this.label51.AutoSize = true;
		this.label51.Location = new System.Drawing.Point(306, 79);
		this.label51.Name = "label51";
		this.label51.Size = new System.Drawing.Size(30, 13);
		this.label51.TabIndex = 287;
		this.label51.Text = "№31";
		this.numericUpDown101.Location = new System.Drawing.Point(539, 58);
		this.numericUpDown101.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown101.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown101.Name = "numericUpDown101";
		this.numericUpDown101.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown101.TabIndex = 286;
		this.numericUpDown101.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown102.Location = new System.Drawing.Point(500, 58);
		this.numericUpDown102.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown102.Name = "numericUpDown102";
		this.numericUpDown102.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown102.TabIndex = 285;
		this.label52.AutoSize = true;
		this.label52.Location = new System.Drawing.Point(306, 60);
		this.label52.Name = "label52";
		this.label52.Size = new System.Drawing.Size(30, 13);
		this.label52.TabIndex = 284;
		this.label52.Text = "№30";
		this.numericUpDown103.Location = new System.Drawing.Point(539, 39);
		this.numericUpDown103.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown103.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown103.Name = "numericUpDown103";
		this.numericUpDown103.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown103.TabIndex = 283;
		this.numericUpDown103.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown104.Location = new System.Drawing.Point(500, 39);
		this.numericUpDown104.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown104.Name = "numericUpDown104";
		this.numericUpDown104.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown104.TabIndex = 282;
		this.label53.AutoSize = true;
		this.label53.Location = new System.Drawing.Point(306, 41);
		this.label53.Name = "label53";
		this.label53.Size = new System.Drawing.Size(30, 13);
		this.label53.TabIndex = 281;
		this.label53.Text = "№29";
		this.numericUpDown105.Location = new System.Drawing.Point(539, 20);
		this.numericUpDown105.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown105.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown105.Name = "numericUpDown105";
		this.numericUpDown105.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown105.TabIndex = 280;
		this.numericUpDown105.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown106.Location = new System.Drawing.Point(500, 20);
		this.numericUpDown106.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown106.Name = "numericUpDown106";
		this.numericUpDown106.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown106.TabIndex = 279;
		this.label54.AutoSize = true;
		this.label54.Location = new System.Drawing.Point(306, 22);
		this.label54.Name = "label54";
		this.label54.Size = new System.Drawing.Size(30, 13);
		this.label54.TabIndex = 278;
		this.label54.Text = "№28";
		this.numericUpDown107.Location = new System.Drawing.Point(539, 1);
		this.numericUpDown107.Maximum = new decimal(new int[4] { 2, 0, 0, 0 });
		this.numericUpDown107.Minimum = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown107.Name = "numericUpDown107";
		this.numericUpDown107.Size = new System.Drawing.Size(29, 20);
		this.numericUpDown107.TabIndex = 277;
		this.numericUpDown107.Value = new decimal(new int[4] { 1, 0, 0, 0 });
		this.numericUpDown108.Location = new System.Drawing.Point(500, 1);
		this.numericUpDown108.Maximum = new decimal(new int[4] { 255, 0, 0, 0 });
		this.numericUpDown108.Name = "numericUpDown108";
		this.numericUpDown108.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown108.TabIndex = 276;
		this.label55.AutoSize = true;
		this.label55.Location = new System.Drawing.Point(306, 3);
		this.label55.Name = "label55";
		this.label55.Size = new System.Drawing.Size(30, 13);
		this.label55.TabIndex = 275;
		this.label55.Text = "№27";
		this.timer2.Enabled = true;
		this.timer2.Interval = 10;
		this.timer2.Tick += new System.EventHandler(this.timer2_Tick);
		this.label56.AutoSize = true;
		this.label56.Location = new System.Drawing.Point(601, 8);
		this.label56.Name = "label56";
		this.label56.Size = new System.Drawing.Size(203, 13);
		this.label56.TabIndex = 410;
		this.label56.Text = "Строка ответа головного контроллера";
		this.label57.AutoSize = true;
		this.label57.Location = new System.Drawing.Point(600, 331);
		this.label57.Name = "label57";
		this.label57.Size = new System.Drawing.Size(305, 13);
		this.label57.TabIndex = 411;
		this.label57.Text = "Строка сообщения посылаемого на  головной контроллер";
		this.numericUpDown109.Location = new System.Drawing.Point(528, 529);
		this.numericUpDown109.Name = "numericUpDown109";
		this.numericUpDown109.Size = new System.Drawing.Size(40, 20);
		this.numericUpDown109.TabIndex = 412;
		this.numericUpDown109.Value = new decimal(new int[4] { 50, 0, 0, 0 });
		this.numericUpDown109.ValueChanged += new System.EventHandler(this.numericUpDown109_ValueChanged);
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		this.BackColor = System.Drawing.Color.PaleGreen;
		base.ClientSize = new System.Drawing.Size(1135, 562);
		base.Controls.Add(this.numericUpDown109);
		base.Controls.Add(this.label57);
		base.Controls.Add(this.label56);
		base.Controls.Add(this.textBox27);
		base.Controls.Add(this.textBox28);
		base.Controls.Add(this.textBox29);
		base.Controls.Add(this.textBox30);
		base.Controls.Add(this.textBox31);
		base.Controls.Add(this.textBox35);
		base.Controls.Add(this.textBox36);
		base.Controls.Add(this.textBox37);
		base.Controls.Add(this.textBox38);
		base.Controls.Add(this.textBox39);
		base.Controls.Add(this.textBox40);
		base.Controls.Add(this.textBox41);
		base.Controls.Add(this.textBox42);
		base.Controls.Add(this.textBox43);
		base.Controls.Add(this.textBox44);
		base.Controls.Add(this.textBox45);
		base.Controls.Add(this.textBox46);
		base.Controls.Add(this.textBox47);
		base.Controls.Add(this.textBox48);
		base.Controls.Add(this.textBox49);
		base.Controls.Add(this.textBox50);
		base.Controls.Add(this.textBox51);
		base.Controls.Add(this.textBox52);
		base.Controls.Add(this.textBox53);
		base.Controls.Add(this.textBox54);
		base.Controls.Add(this.textBox55);
		base.Controls.Add(this.textBox56);
		base.Controls.Add(this.checkBox22);
		base.Controls.Add(this.checkBox29);
		base.Controls.Add(this.checkBox30);
		base.Controls.Add(this.checkBox31);
		base.Controls.Add(this.checkBox32);
		base.Controls.Add(this.checkBox33);
		base.Controls.Add(this.checkBox34);
		base.Controls.Add(this.checkBox35);
		base.Controls.Add(this.checkBox36);
		base.Controls.Add(this.checkBox37);
		base.Controls.Add(this.checkBox38);
		base.Controls.Add(this.checkBox39);
		base.Controls.Add(this.checkBox40);
		base.Controls.Add(this.checkBox41);
		base.Controls.Add(this.checkBox42);
		base.Controls.Add(this.checkBox43);
		base.Controls.Add(this.checkBox44);
		base.Controls.Add(this.checkBox45);
		base.Controls.Add(this.checkBox46);
		base.Controls.Add(this.checkBox47);
		base.Controls.Add(this.checkBox48);
		base.Controls.Add(this.checkBox49);
		base.Controls.Add(this.checkBox50);
		base.Controls.Add(this.checkBox51);
		base.Controls.Add(this.checkBox52);
		base.Controls.Add(this.checkBox53);
		base.Controls.Add(this.checkBox54);
		base.Controls.Add(this.numericUpDown55);
		base.Controls.Add(this.numericUpDown56);
		base.Controls.Add(this.label29);
		base.Controls.Add(this.numericUpDown57);
		base.Controls.Add(this.numericUpDown58);
		base.Controls.Add(this.label30);
		base.Controls.Add(this.numericUpDown59);
		base.Controls.Add(this.numericUpDown60);
		base.Controls.Add(this.label31);
		base.Controls.Add(this.numericUpDown61);
		base.Controls.Add(this.numericUpDown62);
		base.Controls.Add(this.label32);
		base.Controls.Add(this.numericUpDown63);
		base.Controls.Add(this.numericUpDown64);
		base.Controls.Add(this.label33);
		base.Controls.Add(this.numericUpDown65);
		base.Controls.Add(this.numericUpDown66);
		base.Controls.Add(this.label34);
		base.Controls.Add(this.numericUpDown67);
		base.Controls.Add(this.numericUpDown68);
		base.Controls.Add(this.label35);
		base.Controls.Add(this.numericUpDown69);
		base.Controls.Add(this.numericUpDown70);
		base.Controls.Add(this.label36);
		base.Controls.Add(this.numericUpDown71);
		base.Controls.Add(this.numericUpDown72);
		base.Controls.Add(this.label37);
		base.Controls.Add(this.numericUpDown73);
		base.Controls.Add(this.numericUpDown74);
		base.Controls.Add(this.label38);
		base.Controls.Add(this.numericUpDown75);
		base.Controls.Add(this.numericUpDown76);
		base.Controls.Add(this.label39);
		base.Controls.Add(this.numericUpDown77);
		base.Controls.Add(this.numericUpDown78);
		base.Controls.Add(this.label40);
		base.Controls.Add(this.numericUpDown79);
		base.Controls.Add(this.numericUpDown80);
		base.Controls.Add(this.label41);
		base.Controls.Add(this.numericUpDown81);
		base.Controls.Add(this.numericUpDown82);
		base.Controls.Add(this.label42);
		base.Controls.Add(this.numericUpDown83);
		base.Controls.Add(this.numericUpDown84);
		base.Controls.Add(this.label43);
		base.Controls.Add(this.numericUpDown85);
		base.Controls.Add(this.numericUpDown86);
		base.Controls.Add(this.label44);
		base.Controls.Add(this.numericUpDown87);
		base.Controls.Add(this.numericUpDown88);
		base.Controls.Add(this.label45);
		base.Controls.Add(this.numericUpDown89);
		base.Controls.Add(this.numericUpDown90);
		base.Controls.Add(this.label46);
		base.Controls.Add(this.numericUpDown91);
		base.Controls.Add(this.numericUpDown92);
		base.Controls.Add(this.label47);
		base.Controls.Add(this.numericUpDown93);
		base.Controls.Add(this.numericUpDown94);
		base.Controls.Add(this.label48);
		base.Controls.Add(this.numericUpDown95);
		base.Controls.Add(this.numericUpDown96);
		base.Controls.Add(this.label49);
		base.Controls.Add(this.numericUpDown97);
		base.Controls.Add(this.numericUpDown98);
		base.Controls.Add(this.label50);
		base.Controls.Add(this.numericUpDown99);
		base.Controls.Add(this.numericUpDown100);
		base.Controls.Add(this.label51);
		base.Controls.Add(this.numericUpDown101);
		base.Controls.Add(this.numericUpDown102);
		base.Controls.Add(this.label52);
		base.Controls.Add(this.numericUpDown103);
		base.Controls.Add(this.numericUpDown104);
		base.Controls.Add(this.label53);
		base.Controls.Add(this.numericUpDown105);
		base.Controls.Add(this.numericUpDown106);
		base.Controls.Add(this.label54);
		base.Controls.Add(this.numericUpDown107);
		base.Controls.Add(this.numericUpDown108);
		base.Controls.Add(this.label55);
		base.Controls.Add(this.textBox32);
		base.Controls.Add(this.textBox33);
		base.Controls.Add(this.textBox34);
		base.Controls.Add(this.textBox19);
		base.Controls.Add(this.textBox20);
		base.Controls.Add(this.textBox21);
		base.Controls.Add(this.textBox22);
		base.Controls.Add(this.textBox23);
		base.Controls.Add(this.textBox24);
		base.Controls.Add(this.textBox25);
		base.Controls.Add(this.textBox26);
		base.Controls.Add(this.textBox11);
		base.Controls.Add(this.textBox12);
		base.Controls.Add(this.textBox13);
		base.Controls.Add(this.textBox14);
		base.Controls.Add(this.textBox15);
		base.Controls.Add(this.textBox16);
		base.Controls.Add(this.textBox17);
		base.Controls.Add(this.textBox18);
		base.Controls.Add(this.textBox10);
		base.Controls.Add(this.textBox9);
		base.Controls.Add(this.textBox8);
		base.Controls.Add(this.textBox7);
		base.Controls.Add(this.textBox6);
		base.Controls.Add(this.textBox5);
		base.Controls.Add(this.textBox4);
		base.Controls.Add(this.textBox3);
		base.Controls.Add(this.checkBox23);
		base.Controls.Add(this.checkBox24);
		base.Controls.Add(this.checkBox25);
		base.Controls.Add(this.checkBox26);
		base.Controls.Add(this.checkBox27);
		base.Controls.Add(this.checkBox28);
		base.Controls.Add(this.checkBox15);
		base.Controls.Add(this.checkBox16);
		base.Controls.Add(this.checkBox17);
		base.Controls.Add(this.checkBox18);
		base.Controls.Add(this.checkBox19);
		base.Controls.Add(this.checkBox20);
		base.Controls.Add(this.checkBox21);
		base.Controls.Add(this.checkBox8);
		base.Controls.Add(this.checkBox9);
		base.Controls.Add(this.checkBox10);
		base.Controls.Add(this.checkBox11);
		base.Controls.Add(this.checkBox12);
		base.Controls.Add(this.checkBox13);
		base.Controls.Add(this.checkBox14);
		base.Controls.Add(this.checkBox7);
		base.Controls.Add(this.checkBox6);
		base.Controls.Add(this.checkBox5);
		base.Controls.Add(this.checkBox4);
		base.Controls.Add(this.checkBox3);
		base.Controls.Add(this.checkBox2);
		base.Controls.Add(this.checkBox1);
		base.Controls.Add(this.numericUpDown37);
		base.Controls.Add(this.numericUpDown38);
		base.Controls.Add(this.label20);
		base.Controls.Add(this.numericUpDown39);
		base.Controls.Add(this.numericUpDown40);
		base.Controls.Add(this.label21);
		base.Controls.Add(this.numericUpDown41);
		base.Controls.Add(this.numericUpDown42);
		base.Controls.Add(this.label22);
		base.Controls.Add(this.numericUpDown43);
		base.Controls.Add(this.numericUpDown44);
		base.Controls.Add(this.label23);
		base.Controls.Add(this.numericUpDown45);
		base.Controls.Add(this.numericUpDown46);
		base.Controls.Add(this.label24);
		base.Controls.Add(this.numericUpDown47);
		base.Controls.Add(this.numericUpDown48);
		base.Controls.Add(this.label25);
		base.Controls.Add(this.numericUpDown49);
		base.Controls.Add(this.numericUpDown50);
		base.Controls.Add(this.label26);
		base.Controls.Add(this.numericUpDown51);
		base.Controls.Add(this.numericUpDown52);
		base.Controls.Add(this.label27);
		base.Controls.Add(this.numericUpDown53);
		base.Controls.Add(this.numericUpDown54);
		base.Controls.Add(this.label28);
		base.Controls.Add(this.numericUpDown19);
		base.Controls.Add(this.numericUpDown20);
		base.Controls.Add(this.label11);
		base.Controls.Add(this.numericUpDown21);
		base.Controls.Add(this.numericUpDown22);
		base.Controls.Add(this.label12);
		base.Controls.Add(this.numericUpDown23);
		base.Controls.Add(this.numericUpDown24);
		base.Controls.Add(this.label13);
		base.Controls.Add(this.numericUpDown25);
		base.Controls.Add(this.numericUpDown26);
		base.Controls.Add(this.label14);
		base.Controls.Add(this.numericUpDown27);
		base.Controls.Add(this.numericUpDown28);
		base.Controls.Add(this.label15);
		base.Controls.Add(this.numericUpDown29);
		base.Controls.Add(this.numericUpDown30);
		base.Controls.Add(this.label16);
		base.Controls.Add(this.numericUpDown31);
		base.Controls.Add(this.numericUpDown32);
		base.Controls.Add(this.label17);
		base.Controls.Add(this.numericUpDown33);
		base.Controls.Add(this.numericUpDown34);
		base.Controls.Add(this.label18);
		base.Controls.Add(this.numericUpDown35);
		base.Controls.Add(this.numericUpDown36);
		base.Controls.Add(this.label19);
		base.Controls.Add(this.numericUpDown17);
		base.Controls.Add(this.numericUpDown18);
		base.Controls.Add(this.label10);
		base.Controls.Add(this.numericUpDown15);
		base.Controls.Add(this.numericUpDown16);
		base.Controls.Add(this.label9);
		base.Controls.Add(this.numericUpDown13);
		base.Controls.Add(this.numericUpDown14);
		base.Controls.Add(this.label8);
		base.Controls.Add(this.numericUpDown11);
		base.Controls.Add(this.numericUpDown12);
		base.Controls.Add(this.label7);
		base.Controls.Add(this.numericUpDown9);
		base.Controls.Add(this.numericUpDown10);
		base.Controls.Add(this.label6);
		base.Controls.Add(this.numericUpDown7);
		base.Controls.Add(this.numericUpDown8);
		base.Controls.Add(this.label5);
		base.Controls.Add(this.numericUpDown5);
		base.Controls.Add(this.numericUpDown6);
		base.Controls.Add(this.label4);
		base.Controls.Add(this.numericUpDown3);
		base.Controls.Add(this.numericUpDown4);
		base.Controls.Add(this.label2);
		base.Controls.Add(this.numericUpDown2);
		base.Controls.Add(this.numericUpDown1);
		base.Controls.Add(this.label1);
		base.Controls.Add(this.button4);
		base.Controls.Add(this.textBox2);
		base.Controls.Add(this.button1);
		base.Controls.Add(this.textBox1);
		base.Controls.Add(this.textBoxAddress);
		base.Controls.Add(this.label3);
		base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
		base.Name = "Form1";
		this.Text = "UDP программатор";
		base.TopMost = true;
		base.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
		((System.ComponentModel.ISupportInitialize)this.numericUpDown1).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown2).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown3).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown4).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown5).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown6).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown7).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown8).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown9).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown10).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown11).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown12).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown13).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown14).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown15).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown16).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown17).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown18).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown19).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown20).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown21).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown22).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown23).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown24).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown25).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown26).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown27).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown28).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown29).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown30).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown31).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown32).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown33).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown34).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown35).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown36).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown37).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown38).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown39).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown40).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown41).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown42).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown43).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown44).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown45).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown46).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown47).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown48).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown49).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown50).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown51).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown52).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown53).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown54).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown55).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown56).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown57).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown58).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown59).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown60).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown61).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown62).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown63).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown64).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown65).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown66).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown67).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown68).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown69).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown70).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown71).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown72).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown73).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown74).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown75).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown76).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown77).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown78).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown79).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown80).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown81).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown82).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown83).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown84).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown85).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown86).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown87).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown88).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown89).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown90).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown91).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown92).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown93).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown94).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown95).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown96).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown97).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown98).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown99).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown100).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown101).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown102).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown103).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown104).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown105).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown106).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown107).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown108).EndInit();
		((System.ComponentModel.ISupportInitialize)this.numericUpDown109).EndInit();
		base.ResumeLayout(false);
		base.PerformLayout();
	}
}
