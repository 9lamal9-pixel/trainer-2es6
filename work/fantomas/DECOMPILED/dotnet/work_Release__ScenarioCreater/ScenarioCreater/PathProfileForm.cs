using System;
using System.ComponentModel;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using ScenarioCreater.Properties;
using ZedGraph;

namespace ScenarioCreater;

public class PathProfileForm : Form
{
	private PathProfile pathProfile;

	private RailRoadData railRoadData;

	private double _trainPosition;

	private double oldScaleMin;

	private double oldScaleMax;

	private IContainer components;

	private ZedGraphControl ZGC;

	private ImageList TimeOfDayImageList;

	private ImageList WeatherImageList;

	private GroupBox BrakeSystemFaultsGroupBox;

	private ImageList TrafficLightsImageList;

	private ImageList SignsImageList;

	private ImageList PrepImageList;

	private GroupBox DayNightGroupBox;

	private CheckBox nighttimeCheckBox;

	private CheckBox daytimeCheckBox;

	private System.Windows.Forms.Label DayNightLabel;

	private GroupBox WeatherGroupBox;

	private CheckBox FogCheckBox;

	private CheckBox SnowCheckBox;

	private CheckBox RainCheckBox;

	private System.Windows.Forms.Label WeatherLabel;

	private System.Windows.Forms.Label BrakeSystemFaultsLabel;

	private DataGridView BrakeSystemFaultsDataGridView;

	private DataGridViewCheckBoxColumn IsDisplayColumn;

	private DataGridViewTextBoxColumn MainColumn;

	private System.Windows.Forms.Label ElectricalCircuitMalfunctionsLabel;

	private DataGridView ElectricalCircuitMalfunctionsDataGridView;

	private DataGridViewCheckBoxColumn dataGridViewCheckBoxColumn1;

	private DataGridViewTextBoxColumn dataGridViewTextBoxColumn1;

	private GroupBox ElectricalCircuitMalfunctionsGroupBox;

	private CheckBox checkBox1;

	private Timer timer1;

	private CheckBox checkBox3;

	private Button SaveButton;

	private Button CloseButton;

	private CheckBox SearchTrainCheckBox;

	public double trainPosition
	{
		get
		{
			return _trainPosition;
		}
		set
		{
			_trainPosition = value;
			if (ZGC.InvokeRequired)
			{
				ZGC.Invoke((Action)(() =>
				{
					pathProfile.SetVerticalLine(_trainPosition, SearchTrainCheckBox.Checked);
					if (SearchTrainCheckBox.Checked)
					{
						oldScaleMin = ZGC.MasterPane.PaneList[1].XAxis.Scale.Min;
						oldScaleMax = ZGC.MasterPane.PaneList[1].XAxis.Scale.Max;
					}
				}));
			}
			else
			{
				pathProfile.SetVerticalLine(_trainPosition, SearchTrainCheckBox.Checked);
				if (SearchTrainCheckBox.Checked)
				{
					oldScaleMin = ZGC.MasterPane.PaneList[1].XAxis.Scale.Min;
					oldScaleMax = ZGC.MasterPane.PaneList[1].XAxis.Scale.Max;
				}
			}
		}
	}

	public PathProfileForm(RailRoadData railRoadData)
	{
		this.railRoadData = railRoadData;
		InitializeComponent();
	}

	private void PathProfileForm_Load(object sender, EventArgs e)
	{
		Config config = ConfigReader.Read("Data\\config.json");
		Left = config.Main.X;
		Top = config.Main.Y;
		Width = 1920;
		Height = 1080;
		if (config.Main.Windowed)
		{
			FormBorderStyle = FormBorderStyle.FixedSingle;
		}
		BrakeSystemFaultsDataGridView.Enabled = false;
		ElectricalCircuitMalfunctionsDataGridView.Enabled = false;
		SaveButton.Visible = true;
		if (railRoadData.day)
		{
			daytimeCheckBox.ImageIndex = 0;
			nighttimeCheckBox.ImageIndex = 3;
		}
		else
		{
			daytimeCheckBox.ImageIndex = 1;
			nighttimeCheckBox.ImageIndex = 2;
		}
		RainCheckBox.Checked = railRoadData.raining;
		SnowCheckBox.Checked = railRoadData.snow;
		FogCheckBox.Checked = railRoadData.fog;
		BackColor = Color.FromArgb(82, 82, 82);
		ZGC.Left = 2;
		ZGC.Width = ClientSize.Width - 4;
		ZGC.Top = 152;
		ZGC.Height = ClientSize.Height - ZGC.Top - 2;
		ZGC.ContextMenuStrip.ItemClicked += ZGC_ItemClick;
		pathProfile = new PathProfile(ZGC);
		pathProfile.Init();
		pathProfile.Update(railRoadData, TrafficLightsImageList, SignsImageList, PrepImageList);
		oldScaleMin = ZGC.MasterPane.PaneList[1].XAxis.Scale.Min;
		oldScaleMax = ZGC.MasterPane.PaneList[1].XAxis.Scale.Max;
		_trainPosition = (double)railRoadData.startCoord * 0.001;
		BrakeSystemFaultsDataGridView.Rows.Add(0, " ");
		BrakeSystemFaultsDataGridView.Rows.Add(0, " ");
		BrakeSystemFaultsDataGridView.Rows.Add(0, " ");
		BrakeSystemFaultsDataGridView.Rows.Add(0, " ");
		BrakeSystemFaultsDataGridView.Rows.Add(0, " ");
		ElectricalCircuitMalfunctionsDataGridView.Rows.Add(0, " ");
		ElectricalCircuitMalfunctionsDataGridView.Rows.Add(0, " ");
		ElectricalCircuitMalfunctionsDataGridView.Rows.Add(0, " ");
		ElectricalCircuitMalfunctionsDataGridView.Rows.Add(0, " ");
		ElectricalCircuitMalfunctionsDataGridView.Rows.Add(0, " ");
	}

	private void DataGridView_EnabledChanged(object sender, EventArgs e)
	{
		DataGridView dataGridView = sender as DataGridView;
		if (!dataGridView.Enabled)
		{
			dataGridView.DefaultCellStyle.BackColor = SystemColors.Control;
			dataGridView.DefaultCellStyle.ForeColor = SystemColors.GrayText;
			dataGridView.ColumnHeadersDefaultCellStyle.BackColor = SystemColors.Control;
			dataGridView.ColumnHeadersDefaultCellStyle.ForeColor = SystemColors.GrayText;
			dataGridView.CurrentCell = null;
			dataGridView.ReadOnly = true;
			dataGridView.EnableHeadersVisualStyles = false;
			dataGridView.ClearSelection();
		}
		else
		{
			dataGridView.DefaultCellStyle.BackColor = SystemColors.Window;
			dataGridView.DefaultCellStyle.ForeColor = SystemColors.ControlText;
			dataGridView.ColumnHeadersDefaultCellStyle.BackColor = SystemColors.Window;
			dataGridView.ColumnHeadersDefaultCellStyle.ForeColor = SystemColors.ControlText;
			dataGridView.ReadOnly = false;
			dataGridView.EnableHeadersVisualStyles = true;
		}
	}

	private void TimeOfDayCheckBox_CheckedChanged(object sender, EventArgs e)
	{
		if ((sender as CheckBox).Name == "daytimeCheckBox")
		{
			daytimeCheckBox.ImageIndex = 0;
			nighttimeCheckBox.ImageIndex = 3;
			railRoadData.day = true;
		}
		if ((sender as CheckBox).Name == "nighttimeCheckBox")
		{
			daytimeCheckBox.ImageIndex = 1;
			nighttimeCheckBox.ImageIndex = 2;
			railRoadData.day = false;
		}
	}

	private void WeatherCheckBox_CheckedChanged(object sender, EventArgs e)
	{
		if ((sender as CheckBox).Name == "RainCheckBox")
		{
			if (!(sender as CheckBox).Checked)
			{
				(sender as CheckBox).ImageIndex = 0;
			}
			else
			{
				(sender as CheckBox).ImageIndex = 1;
			}
			railRoadData.raining = (sender as CheckBox).Checked;
		}
		if ((sender as CheckBox).Name == "SnowCheckBox")
		{
			if (!(sender as CheckBox).Checked)
			{
				(sender as CheckBox).ImageIndex = 2;
			}
			else
			{
				(sender as CheckBox).ImageIndex = 3;
			}
			railRoadData.snow = (sender as CheckBox).Checked;
		}
		if ((sender as CheckBox).Name == "FogCheckBox")
		{
			if (!(sender as CheckBox).Checked)
			{
				(sender as CheckBox).ImageIndex = 4;
			}
			else
			{
				(sender as CheckBox).ImageIndex = 5;
			}
			railRoadData.fog = (sender as CheckBox).Checked;
		}
	}

	private void ZGC_Paint(object sender, PaintEventArgs e)
	{
		if (ZGC.MasterPane.PaneList.Count != 2)
		{
			return;
		}
		GraphPane graphPane = ZGC.MasterPane.PaneList[1];
		for (int i = 0; i < graphPane.GraphObjList.Count; i++)
		{
			if (graphPane.GraphObjList[i].Tag != null && graphPane.GraphObjList[i].Tag.ToString() == "Piket")
			{
				if (graphPane.XAxis.Scale.Min < graphPane.GraphObjList[i].Location.X && graphPane.GraphObjList[i].Location.X + graphPane.GraphObjList[i].Location.Width < graphPane.XAxis.Scale.Max)
				{
					graphPane.GraphObjList[i].IsVisible = true;
				}
				else
				{
					graphPane.GraphObjList[i].IsVisible = false;
				}
			}
		}
	}

	private void ZGC_MouseClick(object sender, MouseEventArgs e)
	{
	}

	private void ZGC_ItemClick(object sender, ToolStripItemClickedEventArgs e)
	{
		if (((dynamic)e.ClickedItem.Owner.Tag).type == "TrafficLight")
		{
			(ZGC.MasterPane.PaneList[1].GraphObjList[Convert.ToInt32(e.ClickedItem.Tag)] as ZedImageFixed).Image = TrafficLightsImageList.Images[e.ClickedItem.ImageKey];
			railRoadData.lights[(((dynamic)e.ClickedItem.Owner.Tag).value as TrafficLightData).index].light = Convert.ToInt32(e.ClickedItem.ImageKey.Substring(0, e.ClickedItem.ImageKey.IndexOf('.')));
		}
		if (new string[5] { "cars", "rocks", "threes", "vagons", "liters" }.Any((string x) => x == ((dynamic)e.ClickedItem.Owner.Tag).type))
		{
			(ZGC.MasterPane.PaneList[1].GraphObjList[Convert.ToInt32(e.ClickedItem.Tag)] as ZedImageFixed).Image = PrepImageList.Images[e.ClickedItem.ImageKey];
			if (((dynamic)e.ClickedItem.Owner.Tag).type == "cars")
			{
				if (e.ClickedItem.ImageKey == "001.png")
				{
					(((dynamic)e.ClickedItem.Owner.Tag).value as ObstructionData).actively = false;
				}
				else
				{
					(((dynamic)e.ClickedItem.Owner.Tag).value as ObstructionData).actively = true;
				}
			}
			else if (e.ClickedItem.ImageKey == "003.png")
			{
				(((dynamic)e.ClickedItem.Owner.Tag).value as ObstructionData).actively = false;
			}
			else
			{
				(((dynamic)e.ClickedItem.Owner.Tag).value as ObstructionData).actively = true;
			}
		}
		if (((dynamic)e.ClickedItem.Owner.Tag).type == "Signal" && (((dynamic)e.ClickedItem.Owner.Tag).value as SignData).coord != -255)
		{
			railRoadData.signals.Remove(((dynamic)e.ClickedItem.Owner.Tag).value as SignData);
			ZGC.MasterPane.PaneList[1].GraphObjList.RemoveAt(Convert.ToInt32(e.ClickedItem.Tag));
		}
		ZGC.Invalidate();
	}

	private void PutSignal(string _imageKey, ImageList _imageList, double _coord, int _signalIndex)
	{
		ZedImageFixed zedImageFixed = new ZedImageFixed(_imageList.Images[_imageKey], new RectangleF((float)_coord, 6f, 1f, 4f), CoordType.AxisXYScale, AlignH.Right, AlignV.Center, _imageList.ImageSize.Width, _imageList.ImageSize.Height);
		zedImageFixed.IsVisible = true;
		zedImageFixed.IsScaled = false;
		zedImageFixed.IsClippedToChartRect = true;
		zedImageFixed.Tag = new
		{
			type = "Signal",
			value = railRoadData.signals[_signalIndex]
		};
		zedImageFixed.ZOrder = ZOrder.B_BehindLegend;
		ZGC.MasterPane.PaneList[1].GraphObjList.Add(zedImageFixed);
	}

	private int GetSignalItemId(int _coord)
	{
		int result = -1;
		if (_coord < railRoadData.signals[railRoadData.signals.Count - 1].coord)
		{
			if (_coord < railRoadData.signals[0].coord)
			{
				result = 0;
			}
			else
			{
				for (int i = 0; i < railRoadData.signals.Count - 1; i++)
				{
					if (railRoadData.signals[i].coord <= _coord && railRoadData.signals[i + 1].coord >= _coord)
					{
						result = i + 1;
						break;
					}
				}
			}
		}
		else
		{
			result = railRoadData.signals.Count;
		}
		return result;
	}

	private void ZGC_DropDownItemClick(object sender, ToolStripItemClickedEventArgs e)
	{
		if (((dynamic)e.ClickedItem.OwnerItem.Tag).type == "Signal")
		{
			int coord = (((dynamic)e.ClickedItem.OwnerItem.Tag).value as SignData).coord;
			if (e.ClickedItem.Text == "Установить знак в текушую точку")
			{
				if (e.ClickedItem.OwnerItem.ImageKey != "39.BMP")
				{
					if (e.ClickedItem.OwnerItem.ImageKey == "7.BMP" || e.ClickedItem.OwnerItem.ImageKey == "8.BMP")
					{
						string text = InputBox.ShowDialog("Введите ограничение скорости (км/ч)", "", OnlyDigits: true, AllowMinus: false, AllowPoint: false, "0");
						if (text != "" && Convert.ToInt32(text) != 0)
						{
							int signalItemId = GetSignalItemId(coord);
							railRoadData.signals.Insert(signalItemId, new SignData
							{
								type = Convert.ToInt32(e.ClickedItem.OwnerItem.ImageKey.Substring(0, e.ClickedItem.OwnerItem.ImageKey.IndexOf('.'))),
								coord = (((dynamic)e.ClickedItem.OwnerItem.Tag).value as SignData).coord,
								speedLimit = Convert.ToInt32(text)
							});
							PutSignal(e.ClickedItem.OwnerItem.ImageKey, SignsImageList, (double)coord * 0.001, signalItemId);
						}
					}
					else
					{
						int signalItemId2 = GetSignalItemId(coord);
						railRoadData.signals.Insert(signalItemId2, ((dynamic)e.ClickedItem.OwnerItem.Tag).value as SignData);
						railRoadData.signals[signalItemId2].type = Convert.ToInt32(e.ClickedItem.OwnerItem.ImageKey.Substring(0, e.ClickedItem.OwnerItem.ImageKey.IndexOf('.')));
						PutSignal(e.ClickedItem.OwnerItem.ImageKey, SignsImageList, (double)coord * 0.001, signalItemId2);
					}
				}
				else
				{
					int signalItemId3 = GetSignalItemId(coord);
					railRoadData.signals.Insert(signalItemId3, new SignData
					{
						type = 20,
						coord = coord
					});
					PutSignal("20.BMP", SignsImageList, (double)coord * 0.001, signalItemId3);
					int signalItemId4 = GetSignalItemId(coord + 300);
					railRoadData.signals.Insert(signalItemId4, new SignData
					{
						type = 11,
						coord = coord + 300
					});
					PutSignal("11.BMP", SignsImageList, (double)(coord + 300) * 0.001, signalItemId4);
					int signalItemId5 = GetSignalItemId(coord + 800);
					railRoadData.signals.Insert(signalItemId5, new SignData
					{
						type = 19,
						coord = coord + 800
					});
					PutSignal("19.BMP", SignsImageList, (double)(coord + 800) * 0.001, signalItemId5);
				}
			}
			if (e.ClickedItem.Text == "Установить знак через 1000 метров")
			{
				if (e.ClickedItem.OwnerItem.ImageKey != "39.BMP")
				{
					if (e.ClickedItem.OwnerItem.ImageKey == "7.BMP" || e.ClickedItem.OwnerItem.ImageKey == "8.BMP")
					{
						string text2 = InputBox.ShowDialog("Введите ограничение скорости (км/ч)", "", OnlyDigits: true, AllowMinus: false, AllowPoint: false, "0");
						if (text2 != "" && Convert.ToInt32(text2) != 0)
						{
							int signalItemId6 = GetSignalItemId(coord + 1000);
							railRoadData.signals.Insert(signalItemId6, new SignData
							{
								type = Convert.ToInt32(e.ClickedItem.OwnerItem.ImageKey.Substring(0, e.ClickedItem.OwnerItem.ImageKey.IndexOf('.'))),
								coord = (((dynamic)e.ClickedItem.OwnerItem.Tag).value as SignData).coord,
								speedLimit = Convert.ToInt32(text2)
							});
							PutSignal(e.ClickedItem.OwnerItem.ImageKey, SignsImageList, (double)(coord + 1000) * 0.001, signalItemId6);
						}
					}
					else
					{
						int signalItemId7 = GetSignalItemId(coord + 1000);
						railRoadData.signals.Insert(signalItemId7, ((dynamic)e.ClickedItem.OwnerItem.Tag).value as SignData);
						railRoadData.signals[signalItemId7].type = Convert.ToInt32(e.ClickedItem.OwnerItem.ImageKey.Substring(0, e.ClickedItem.OwnerItem.ImageKey.IndexOf('.')));
						PutSignal(e.ClickedItem.OwnerItem.ImageKey, SignsImageList, (double)(coord + 1000) * 0.001, signalItemId7);
					}
				}
				else
				{
					coord += 1000;
					int signalItemId8 = GetSignalItemId(coord);
					railRoadData.signals.Insert(signalItemId8, new SignData
					{
						type = 20,
						coord = coord
					});
					PutSignal("20.BMP", SignsImageList, (double)coord * 0.001, signalItemId8);
					int signalItemId9 = GetSignalItemId(coord + 300);
					railRoadData.signals.Insert(signalItemId9, new SignData
					{
						type = 11,
						coord = coord + 300
					});
					PutSignal("11.BMP", SignsImageList, (double)(coord + 300) * 0.001, signalItemId9);
					int signalItemId10 = GetSignalItemId(coord + 800);
					railRoadData.signals.Insert(signalItemId10, new SignData
					{
						type = 19,
						coord = coord + 800
					});
					PutSignal("19.BMP", SignsImageList, (double)(coord + 800) * 0.001, signalItemId10);
				}
			}
			ZGC.Invalidate();
		}
		if (((dynamic)e.ClickedItem.OwnerItem.Tag).type == "TrainPos")
		{
			double num = ((dynamic)e.ClickedItem.OwnerItem.Tag).value;
			trainPosition = num * 0.001;
			railRoadData.startCoord = Convert.ToInt32(num);
		}
	}

	private void ZGC_ContextMenuBuilder(ZedGraphControl sender, ContextMenuStrip menuStrip, Point mousePt, ZedGraphControl.ContextMenuObjectState objState)
	{
		GraphPane secondPane = ZGC.MasterPane.PaneList[1];
		menuStrip.Items.Clear();
		bool flag = false;
		int i;
		for (i = 0; i < secondPane.GraphObjList.Count; i++)
		{
			if (!(secondPane.GraphObjList[i].GetType() == typeof(ZedImageFixed)))
			{
				continue;
			}
			PointF pointF = secondPane.GeneralTransform(secondPane.GraphObjList[i].Location.TopLeft, CoordType.AxisXYScale);
			SizeF sizeF = new SizeF((secondPane.GraphObjList[i] as ZedImageFixed).widthInWindowCoord, (secondPane.GraphObjList[i] as ZedImageFixed).heightInWindowCoord);
			if (!(pointF.X < (float)mousePt.X) || !(pointF.X + sizeF.Width > (float)mousePt.X) || !(pointF.Y < (float)mousePt.Y) || !(pointF.Y + sizeF.Height > (float)mousePt.Y))
			{
				continue;
			}
			flag = true;
			menuStrip.AutoSize = false;
			menuStrip.ShowImageMargin = true;
			if (((dynamic)secondPane.GraphObjList[i].Tag).type == "TrafficLight")
			{
				TrafficLightData trafficLightData = ((dynamic)secondPane.GraphObjList[i].Tag).value as TrafficLightData;
				menuStrip.Tag = secondPane.GraphObjList[i].Tag;
				menuStrip.ImageList = TrafficLightsImageList;
				for (int j = 0; j < trafficLightData.lightCombinations.Length; j++)
				{
					menuStrip.Items.Add("");
					menuStrip.Items[menuStrip.Items.Count - 1].ImageScaling = ToolStripItemImageScaling.None;
					menuStrip.Items[menuStrip.Items.Count - 1].ImageAlign = ContentAlignment.MiddleLeft;
					menuStrip.Items[menuStrip.Items.Count - 1].DisplayStyle = ToolStripItemDisplayStyle.Image;
					menuStrip.Items[menuStrip.Items.Count - 1].Tag = i;
					menuStrip.Items[menuStrip.Items.Count - 1].ImageKey = trafficLightData.lightCombinations[j] + ".png";
				}
				menuStrip.Size = new Size(30, trafficLightData.lightCombinations.Length * menuStrip.Items[menuStrip.Items.Count - 1].Height + 4);
			}
			if (new string[5] { "cars", "rocks", "threes", "vagons", "liters" }.Any((string x) => x == ((dynamic)secondPane.GraphObjList[i].Tag).type))
			{
				menuStrip.Size = new Size(PrepImageList.ImageSize.Width + 10, PrepImageList.ImageSize.Height * 2 + 20);
				menuStrip.Tag = secondPane.GraphObjList[i].Tag;
				menuStrip.ImageList = PrepImageList;
				for (int num = 0; num < 2; num++)
				{
					menuStrip.Items.Add("");
					menuStrip.Items[menuStrip.Items.Count - 1].ImageScaling = ToolStripItemImageScaling.None;
					menuStrip.Items[menuStrip.Items.Count - 1].ImageAlign = ContentAlignment.MiddleLeft;
					menuStrip.Items[menuStrip.Items.Count - 1].DisplayStyle = ToolStripItemDisplayStyle.Image;
					menuStrip.Items[menuStrip.Items.Count - 1].Tag = i;
				}
				if (((dynamic)secondPane.GraphObjList[i].Tag).type == "cars")
				{
					menuStrip.Items[0].ImageKey = "001.png";
					menuStrip.Items[1].ImageKey = "002.png";
				}
				if (((dynamic)secondPane.GraphObjList[i].Tag).type == "rocks")
				{
					menuStrip.Items[0].ImageKey = "003.png";
					menuStrip.Items[1].ImageKey = "005.png";
				}
				if (((dynamic)secondPane.GraphObjList[i].Tag).type == "threes")
				{
					menuStrip.Items[0].ImageKey = "003.png";
					menuStrip.Items[1].ImageKey = "004.png";
				}
				if (((dynamic)secondPane.GraphObjList[i].Tag).type == "vagons")
				{
					menuStrip.Items[0].ImageKey = "003.png";
					menuStrip.Items[1].ImageKey = "007.png";
				}
				if (((dynamic)secondPane.GraphObjList[i].Tag).type == "liters")
				{
					menuStrip.Items[0].ImageKey = "003.png";
					menuStrip.Items[1].ImageKey = "006.png";
				}
			}
			if (((dynamic)secondPane.GraphObjList[i].Tag).type == "Signal")
			{
				menuStrip.Size = new Size(40, 40);
				menuStrip.Tag = secondPane.GraphObjList[i].Tag;
				menuStrip.Items.Add("");
				menuStrip.Items[menuStrip.Items.Count - 1].ImageScaling = ToolStripItemImageScaling.None;
				menuStrip.Items[menuStrip.Items.Count - 1].ImageAlign = ContentAlignment.MiddleLeft;
				menuStrip.Items[menuStrip.Items.Count - 1].DisplayStyle = ToolStripItemDisplayStyle.Image;
				menuStrip.Items[menuStrip.Items.Count - 1].Tag = i;
				menuStrip.Items[0].Image = Resources.delete_image;
			}
			break;
		}
		if (!flag)
		{
			int num2 = 10;
			menuStrip.AutoSize = false;
			menuStrip.ShowImageMargin = true;
			menuStrip.Tag = new
			{
				type = "Signal",
				value = new SignData
				{
					type = 0,
					coord = -255
				}
			};
			secondPane.ReverseTransform(mousePt, out var num3, out var _);
			menuStrip.ImageList = SignsImageList;
			int num5 = 1;
			menuStrip.Size = new Size(SignsImageList.ImageSize.Width + 10, (SignsImageList.ImageSize.Height + 4) * (num2 + 1) + 4);
			ToolStripMenuItem toolStripMenuItem = new ToolStripMenuItem();
			toolStripMenuItem.DropDownItems.Add("Переместить поезд");
			toolStripMenuItem.DropDownDirection = ToolStripDropDownDirection.Right;
			toolStripMenuItem.ImageScaling = ToolStripItemImageScaling.None;
			toolStripMenuItem.ImageAlign = ContentAlignment.MiddleLeft;
			toolStripMenuItem.DisplayStyle = ToolStripItemDisplayStyle.Image;
			toolStripMenuItem.Alignment = ToolStripItemAlignment.Right;
			toolStripMenuItem.AutoSize = false;
			toolStripMenuItem.Width = menuStrip.Width;
			toolStripMenuItem.Height = SignsImageList.ImageSize.Height + 4;
			toolStripMenuItem.Tag = new
			{
				type = "TrainPos",
				value = Convert.ToInt32(num3 * 1000.0)
			};
			toolStripMenuItem.DropDownItemClicked += ZGC_DropDownItemClick;
			menuStrip.Items.Add(toolStripMenuItem);
			menuStrip.Items[0].ImageKey = "lokomotiv.png";
			for (int num6 = 0; num6 < num2; num6++)
			{
				toolStripMenuItem = new ToolStripMenuItem();
				toolStripMenuItem.DropDownItems.Add("Установить знак в текушую точку");
				toolStripMenuItem.DropDownItems.Add("Установить знак через 1000 метров");
				toolStripMenuItem.DropDownDirection = ToolStripDropDownDirection.Right;
				toolStripMenuItem.ImageScaling = ToolStripItemImageScaling.None;
				toolStripMenuItem.ImageAlign = ContentAlignment.MiddleLeft;
				toolStripMenuItem.DisplayStyle = ToolStripItemDisplayStyle.Image;
				toolStripMenuItem.Alignment = ToolStripItemAlignment.Right;
				toolStripMenuItem.AutoSize = false;
				toolStripMenuItem.Width = menuStrip.Width;
				toolStripMenuItem.Height = SignsImageList.ImageSize.Height + 4;
				toolStripMenuItem.Tag = new
				{
					type = "Signal",
					value = new SignData
					{
						type = 0,
						coord = Convert.ToInt32(num3 * 1000.0)
					}
				};
				toolStripMenuItem.DropDownItemClicked += ZGC_DropDownItemClick;
				menuStrip.Items.Add(toolStripMenuItem);
			}
			menuStrip.Items[num5].ImageKey = "5.BMP";
			menuStrip.Items[num5 + 1].ImageKey = "2.BMP";
			menuStrip.Items[num5 + 2].ImageKey = "4.BMP";
			menuStrip.Items[num5 + 3].ImageKey = "8.BMP";
			menuStrip.Items[num5 + 4].ImageKey = "9.BMP";
			menuStrip.Items[num5 + 5].ImageKey = "11.BMP";
			menuStrip.Items[num5 + 6].ImageKey = "19.BMP";
			menuStrip.Items[num5 + 7].ImageKey = "37.BMP";
			menuStrip.Items[num5 + 8].ImageKey = "38.BMP";
			menuStrip.Items[num5 + 9].ImageKey = "39.BMP";
		}
	}

	private void ZGC_ZoomEvent(ZedGraphControl sender, ZoomState oldState, ZoomState newState)
	{
		GraphPane graphPane = ZGC.MasterPane.PaneList[0];
		GraphPane graphPane2 = ZGC.MasterPane.PaneList[1];
		bool flag = true;
		if (graphPane2.XAxis.Scale.Max - graphPane2.XAxis.Scale.Min >= 10.0)
		{
			graphPane.XAxis.Scale.Min = oldScaleMin;
			graphPane.XAxis.Scale.Max = oldScaleMax;
			graphPane2.XAxis.Scale.Min = oldScaleMin;
			graphPane2.XAxis.Scale.Max = oldScaleMax;
			flag = false;
		}
		if (graphPane2.XAxis.Scale.Max - graphPane2.XAxis.Scale.Min <= 1.0)
		{
			graphPane.XAxis.Scale.Min = oldScaleMin;
			graphPane.XAxis.Scale.Max = oldScaleMax;
			graphPane2.XAxis.Scale.Min = oldScaleMin;
			graphPane2.XAxis.Scale.Max = oldScaleMax;
			flag = false;
		}
		if (flag)
		{
			oldScaleMin = graphPane.XAxis.Scale.Min;
			oldScaleMax = graphPane.XAxis.Scale.Max;
		}
		ZGC.Invalidate();
	}

	private void checkBox1_CheckedChanged(object sender, EventArgs e)
	{
		timer1.Enabled = checkBox1.Checked;
	}

	private void timer1_Tick(object sender, EventArgs e)
	{
		if (trainPosition + 0.05 < ZGC.MasterPane.PaneList[0].CurveList[0].Points[ZGC.MasterPane.PaneList[0].CurveList[0].Points.Count - 1].X)
		{
			trainPosition += 0.05;
		}
		else
		{
			pathProfile.SetVerticalLine(ZGC.MasterPane.PaneList[0].CurveList[0].Points[ZGC.MasterPane.PaneList[0].CurveList[0].Points.Count - 1].X, SearchTrainCheckBox.Checked);
		}
	}

	private bool ZGC_MouseUpEvent(ZedGraphControl sender, MouseEventArgs e)
	{
		oldScaleMin = ZGC.MasterPane.PaneList[1].XAxis.Scale.Min;
		oldScaleMax = ZGC.MasterPane.PaneList[1].XAxis.Scale.Max;
		return false;
	}

	private void checkBox3_CheckedChanged(object sender, EventArgs e)
	{
		SetTrafficLight(5, 7);
	}

	private void CloseButton_Click(object sender, EventArgs e)
	{
		Close();
	}

	private void SaveButton_Click(object sender, EventArgs e)
	{
		if (RailRoadDataUtilities.SaveJsonConfig("RTS\\scenaries\\" + MarkupConfig.instance.MarkupFileName, railRoadData))
		{
			MessageBox.Show("Данные сохранены !");
		}
		else
		{
			MessageBox.Show("Ошибка сохранения файла !");
		}
	}

	private void CloseButton_MouseDown(object sender, MouseEventArgs e)
	{
		CloseButton.Image = Resources.Exit_on;
	}

	private void CloseButton_MouseUp(object sender, MouseEventArgs e)
	{
		CloseButton.Image = Resources.ExitButton;
	}

	private void SaveButton_MouseDown(object sender, MouseEventArgs e)
	{
		SaveButton.Image = Resources.Save_on;
	}

	private void SaveButton_MouseUp(object sender, MouseEventArgs e)
	{
		SaveButton.Image = Resources.Save_off;
	}

	private void SearchTrainCheckBox_CheckedChanged(object sender, EventArgs e)
	{
		if (SearchTrainCheckBox.Checked)
		{
			SearchTrainCheckBox.Image = Resources.SearchTrain_on;
			pathProfile.SetVerticalLine(trainPosition, SearchTrainCheckBox.Checked);
			oldScaleMin = ZGC.MasterPane.PaneList[1].XAxis.Scale.Min;
			oldScaleMax = ZGC.MasterPane.PaneList[1].XAxis.Scale.Max;
		}
		else
		{
			SearchTrainCheckBox.Image = Resources.SearchTrain;
		}
	}

	public void SetTrafficLight(int id, int light)
	{
		int i;
		for (i = 0; i < ZGC.MasterPane.PaneList[1].GraphObjList.Count; i++)
		{
			if (!(ZGC.MasterPane.PaneList[1].GraphObjList[i].GetType() == typeof(ZedImageFixed)) || !((((dynamic)ZGC.MasterPane.PaneList[1].GraphObjList[i].Tag).type == "TrafficLight") ? true : false))
			{
				continue;
			}
			TrafficLightData trafficLightData = ((dynamic)ZGC.MasterPane.PaneList[1].GraphObjList[i].Tag).value as TrafficLightData;
			if (trafficLightData.index != railRoadData.lights[id].index || trafficLightData.coord != railRoadData.lights[id].coord)
			{
				continue;
			}
			if (ZGC.InvokeRequired)
			{
				ZGC.Invoke((Action)(() =>
				{
					(ZGC.MasterPane.PaneList[1].GraphObjList[i] as ZedImageFixed).Image = TrafficLightsImageList.Images[Convert.ToString(light) + ".png"];
					ZGC.Invalidate();
				}));
			}
			else
			{
				(ZGC.MasterPane.PaneList[1].GraphObjList[i] as ZedImageFixed).Image = TrafficLightsImageList.Images[Convert.ToString(light) + ".png"];
				ZGC.Invalidate();
			}
			break;
		}
	}

	private void checkBox2_CheckedChanged(object sender, EventArgs e)
	{
	}

	private bool ZGC_MouseMoveEvent(ZedGraphControl sender, MouseEventArgs e)
	{
		if (e.Button == MouseButtons.Left && SearchTrainCheckBox.Checked)
		{
			SearchTrainCheckBox.Checked = false;
		}
		return false;
	}

	private void ZGC_ScrollProgressEvent(ZedGraphControl sender, ScrollBar scrollBar, ZoomState oldState, ZoomState newState)
	{
		if (SearchTrainCheckBox.Checked)
		{
			SearchTrainCheckBox.Checked = false;
		}
	}

	private void PathProfileForm_Shown(object sender, EventArgs e)
	{
		SearchTrainCheckBox.Checked = true;
		pathProfile.SetVerticalLine(trainPosition, SearchTrainCheckBox.Checked);
		oldScaleMin = ZGC.MasterPane.PaneList[1].XAxis.Scale.Min;
		oldScaleMax = ZGC.MasterPane.PaneList[1].XAxis.Scale.Max;
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
		System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ScenarioCreater.PathProfileForm));
		System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle = new System.Windows.Forms.DataGridViewCellStyle();
		System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle2 = new System.Windows.Forms.DataGridViewCellStyle();
		System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle3 = new System.Windows.Forms.DataGridViewCellStyle();
		System.Windows.Forms.DataGridViewCellStyle dataGridViewCellStyle4 = new System.Windows.Forms.DataGridViewCellStyle();
		this.ZGC = new ZedGraph.ZedGraphControl();
		this.TimeOfDayImageList = new System.Windows.Forms.ImageList(this.components);
		this.WeatherImageList = new System.Windows.Forms.ImageList(this.components);
		this.BrakeSystemFaultsGroupBox = new System.Windows.Forms.GroupBox();
		this.BrakeSystemFaultsDataGridView = new System.Windows.Forms.DataGridView();
		this.IsDisplayColumn = new System.Windows.Forms.DataGridViewCheckBoxColumn();
		this.MainColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.TrafficLightsImageList = new System.Windows.Forms.ImageList(this.components);
		this.SignsImageList = new System.Windows.Forms.ImageList(this.components);
		this.PrepImageList = new System.Windows.Forms.ImageList(this.components);
		this.DayNightGroupBox = new System.Windows.Forms.GroupBox();
		this.nighttimeCheckBox = new System.Windows.Forms.CheckBox();
		this.daytimeCheckBox = new System.Windows.Forms.CheckBox();
		this.DayNightLabel = new System.Windows.Forms.Label();
		this.WeatherGroupBox = new System.Windows.Forms.GroupBox();
		this.FogCheckBox = new System.Windows.Forms.CheckBox();
		this.SnowCheckBox = new System.Windows.Forms.CheckBox();
		this.RainCheckBox = new System.Windows.Forms.CheckBox();
		this.WeatherLabel = new System.Windows.Forms.Label();
		this.BrakeSystemFaultsLabel = new System.Windows.Forms.Label();
		this.ElectricalCircuitMalfunctionsLabel = new System.Windows.Forms.Label();
		this.ElectricalCircuitMalfunctionsDataGridView = new System.Windows.Forms.DataGridView();
		this.dataGridViewCheckBoxColumn1 = new System.Windows.Forms.DataGridViewCheckBoxColumn();
		this.dataGridViewTextBoxColumn1 = new System.Windows.Forms.DataGridViewTextBoxColumn();
		this.ElectricalCircuitMalfunctionsGroupBox = new System.Windows.Forms.GroupBox();
		this.checkBox1 = new System.Windows.Forms.CheckBox();
		this.timer1 = new System.Windows.Forms.Timer(this.components);
		this.checkBox3 = new System.Windows.Forms.CheckBox();
		this.SaveButton = new System.Windows.Forms.Button();
		this.CloseButton = new System.Windows.Forms.Button();
		this.SearchTrainCheckBox = new System.Windows.Forms.CheckBox();
		this.BrakeSystemFaultsGroupBox.SuspendLayout();
		((System.ComponentModel.ISupportInitialize)this.BrakeSystemFaultsDataGridView).BeginInit();
		this.DayNightGroupBox.SuspendLayout();
		this.WeatherGroupBox.SuspendLayout();
		((System.ComponentModel.ISupportInitialize)this.ElectricalCircuitMalfunctionsDataGridView).BeginInit();
		this.ElectricalCircuitMalfunctionsGroupBox.SuspendLayout();
		base.SuspendLayout();
		this.ZGC.BackColor = System.Drawing.Color.White;
		this.ZGC.EditButtons = System.Windows.Forms.MouseButtons.None;
		this.ZGC.ForeColor = System.Drawing.SystemColors.ControlText;
		this.ZGC.IsEnableVPan = false;
		this.ZGC.IsEnableVZoom = false;
		this.ZGC.Location = new System.Drawing.Point(9, 150);
		this.ZGC.Margin = new System.Windows.Forms.Padding(0);
		this.ZGC.Name = "ZGC";
		this.ZGC.PanButtons = System.Windows.Forms.MouseButtons.None;
		this.ZGC.PanButtons2 = System.Windows.Forms.MouseButtons.Left;
		this.ZGC.ScrollGrace = 0.0;
		this.ZGC.ScrollMaxX = 0.0;
		this.ZGC.ScrollMaxY = 0.0;
		this.ZGC.ScrollMaxY2 = 0.0;
		this.ZGC.ScrollMinX = 0.0;
		this.ZGC.ScrollMinY = 0.0;
		this.ZGC.ScrollMinY2 = 0.0;
		this.ZGC.Size = new System.Drawing.Size(1599, 624);
		this.ZGC.TabIndex = 0;
		this.ZGC.UseExtendedPrintDialog = true;
		this.ZGC.ContextMenuBuilder += new ZedGraph.ZedGraphControl.ContextMenuBuilderEventHandler(this.ZGC_ContextMenuBuilder);
		this.ZGC.ZoomEvent += new ZedGraph.ZedGraphControl.ZoomEventHandler(this.ZGC_ZoomEvent);
		this.ZGC.ScrollProgressEvent += new ZedGraph.ZedGraphControl.ScrollProgressHandler(this.ZGC_ScrollProgressEvent);
		this.ZGC.MouseUpEvent += new ZedGraph.ZedGraphControl.ZedMouseEventHandler(this.ZGC_MouseUpEvent);
		this.ZGC.MouseMoveEvent += new ZedGraph.ZedGraphControl.ZedMouseEventHandler(this.ZGC_MouseMoveEvent);
		this.ZGC.Paint += new System.Windows.Forms.PaintEventHandler(this.ZGC_Paint);
		this.ZGC.MouseClick += new System.Windows.Forms.MouseEventHandler(this.ZGC_MouseClick);
		this.TimeOfDayImageList.ImageStream = (System.Windows.Forms.ImageListStreamer)resources.GetObject("TimeOfDayImageList.ImageStream");
		this.TimeOfDayImageList.TransparentColor = System.Drawing.Color.Transparent;
		this.TimeOfDayImageList.Images.SetKeyName(0, "01.png");
		this.TimeOfDayImageList.Images.SetKeyName(1, "00.png");
		this.TimeOfDayImageList.Images.SetKeyName(2, "11.png");
		this.TimeOfDayImageList.Images.SetKeyName(3, "10.png");
		this.WeatherImageList.ImageStream = (System.Windows.Forms.ImageListStreamer)resources.GetObject("WeatherImageList.ImageStream");
		this.WeatherImageList.TransparentColor = System.Drawing.Color.Transparent;
		this.WeatherImageList.Images.SetKeyName(0, "00.png");
		this.WeatherImageList.Images.SetKeyName(1, "01.png");
		this.WeatherImageList.Images.SetKeyName(2, "10.png");
		this.WeatherImageList.Images.SetKeyName(3, "11.png");
		this.WeatherImageList.Images.SetKeyName(4, "20.png");
		this.WeatherImageList.Images.SetKeyName(5, "21.png");
		this.BrakeSystemFaultsGroupBox.Controls.Add(this.BrakeSystemFaultsDataGridView);
		this.BrakeSystemFaultsGroupBox.Location = new System.Drawing.Point(577, 8);
		this.BrakeSystemFaultsGroupBox.Margin = new System.Windows.Forms.Padding(0);
		this.BrakeSystemFaultsGroupBox.Name = "BrakeSystemFaultsGroupBox";
		this.BrakeSystemFaultsGroupBox.Padding = new System.Windows.Forms.Padding(0);
		this.BrakeSystemFaultsGroupBox.Size = new System.Drawing.Size(312, 131);
		this.BrakeSystemFaultsGroupBox.TabIndex = 8;
		this.BrakeSystemFaultsGroupBox.TabStop = false;
		this.BrakeSystemFaultsDataGridView.AllowUserToAddRows = false;
		this.BrakeSystemFaultsDataGridView.AllowUserToDeleteRows = false;
		this.BrakeSystemFaultsDataGridView.AllowUserToResizeColumns = false;
		this.BrakeSystemFaultsDataGridView.AllowUserToResizeRows = false;
		this.BrakeSystemFaultsDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
		this.BrakeSystemFaultsDataGridView.BackgroundColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.BrakeSystemFaultsDataGridView.BorderStyle = System.Windows.Forms.BorderStyle.None;
		this.BrakeSystemFaultsDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
		this.BrakeSystemFaultsDataGridView.ColumnHeadersVisible = false;
		this.BrakeSystemFaultsDataGridView.Columns.AddRange(this.IsDisplayColumn, this.MainColumn);
		this.BrakeSystemFaultsDataGridView.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnEnter;
		this.BrakeSystemFaultsDataGridView.Location = new System.Drawing.Point(6, 11);
		this.BrakeSystemFaultsDataGridView.MultiSelect = false;
		this.BrakeSystemFaultsDataGridView.Name = "BrakeSystemFaultsDataGridView";
		this.BrakeSystemFaultsDataGridView.RowHeadersVisible = false;
		this.BrakeSystemFaultsDataGridView.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
		this.BrakeSystemFaultsDataGridView.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
		this.BrakeSystemFaultsDataGridView.ShowCellErrors = false;
		this.BrakeSystemFaultsDataGridView.ShowCellToolTips = false;
		this.BrakeSystemFaultsDataGridView.ShowEditingIcon = false;
		this.BrakeSystemFaultsDataGridView.ShowRowErrors = false;
		this.BrakeSystemFaultsDataGridView.Size = new System.Drawing.Size(300, 109);
		this.BrakeSystemFaultsDataGridView.TabIndex = 0;
		this.BrakeSystemFaultsDataGridView.EnabledChanged += new System.EventHandler(this.DataGridView_EnabledChanged);
		dataGridViewCellStyle.Alignment = System.Windows.Forms.DataGridViewContentAlignment.MiddleCenter;
		dataGridViewCellStyle.BackColor = System.Drawing.SystemColors.AppWorkspace;
		dataGridViewCellStyle.NullValue = false;
		dataGridViewCellStyle.SelectionBackColor = System.Drawing.SystemColors.ControlDarkDark;
		this.IsDisplayColumn.DefaultCellStyle = dataGridViewCellStyle;
		this.IsDisplayColumn.FillWeight = 25.38071f;
		this.IsDisplayColumn.HeaderText = "IsDisplayColumn";
		this.IsDisplayColumn.Name = "IsDisplayColumn";
		dataGridViewCellStyle2.BackColor = System.Drawing.SystemColors.AppWorkspace;
		dataGridViewCellStyle2.SelectionBackColor = System.Drawing.SystemColors.ControlDarkDark;
		this.MainColumn.DefaultCellStyle = dataGridViewCellStyle2;
		this.MainColumn.FillWeight = 174.6193f;
		this.MainColumn.HeaderText = "MainColumn";
		this.MainColumn.Name = "MainColumn";
		this.MainColumn.ReadOnly = true;
		this.MainColumn.SortMode = System.Windows.Forms.DataGridViewColumnSortMode.NotSortable;
		this.TrafficLightsImageList.ImageStream = (System.Windows.Forms.ImageListStreamer)resources.GetObject("TrafficLightsImageList.ImageStream");
		this.TrafficLightsImageList.TransparentColor = System.Drawing.Color.Transparent;
		this.TrafficLightsImageList.Images.SetKeyName(0, "0.png");
		this.TrafficLightsImageList.Images.SetKeyName(1, "1.png");
		this.TrafficLightsImageList.Images.SetKeyName(2, "2.png");
		this.TrafficLightsImageList.Images.SetKeyName(3, "4.png");
		this.TrafficLightsImageList.Images.SetKeyName(4, "6.png");
		this.TrafficLightsImageList.Images.SetKeyName(5, "7.png");
		this.TrafficLightsImageList.Images.SetKeyName(6, "8.png");
		this.TrafficLightsImageList.Images.SetKeyName(7, "9.png");
		this.TrafficLightsImageList.Images.SetKeyName(8, "10.png");
		this.TrafficLightsImageList.Images.SetKeyName(9, "11.png");
		this.TrafficLightsImageList.Images.SetKeyName(10, "13.png");
		this.TrafficLightsImageList.Images.SetKeyName(11, "14.png");
		this.TrafficLightsImageList.Images.SetKeyName(12, "15.png");
		this.TrafficLightsImageList.Images.SetKeyName(13, "16.png");
		this.TrafficLightsImageList.Images.SetKeyName(14, "17.png");
		this.TrafficLightsImageList.Images.SetKeyName(15, "18.png");
		this.TrafficLightsImageList.Images.SetKeyName(16, "19.png");
		this.TrafficLightsImageList.Images.SetKeyName(17, "20.png");
		this.SignsImageList.ImageStream = (System.Windows.Forms.ImageListStreamer)resources.GetObject("SignsImageList.ImageStream");
		this.SignsImageList.TransparentColor = System.Drawing.Color.Transparent;
		this.SignsImageList.Images.SetKeyName(0, "0.BMP");
		this.SignsImageList.Images.SetKeyName(1, "1.BMP");
		this.SignsImageList.Images.SetKeyName(2, "2.BMP");
		this.SignsImageList.Images.SetKeyName(3, "3.BMP");
		this.SignsImageList.Images.SetKeyName(4, "4.BMP");
		this.SignsImageList.Images.SetKeyName(5, "5.BMP");
		this.SignsImageList.Images.SetKeyName(6, "6.BMP");
		this.SignsImageList.Images.SetKeyName(7, "7.BMP");
		this.SignsImageList.Images.SetKeyName(8, "8.BMP");
		this.SignsImageList.Images.SetKeyName(9, "9.BMP");
		this.SignsImageList.Images.SetKeyName(10, "10.BMP");
		this.SignsImageList.Images.SetKeyName(11, "11.BMP");
		this.SignsImageList.Images.SetKeyName(12, "12.BMP");
		this.SignsImageList.Images.SetKeyName(13, "13.BMP");
		this.SignsImageList.Images.SetKeyName(14, "14.BMP");
		this.SignsImageList.Images.SetKeyName(15, "15.BMP");
		this.SignsImageList.Images.SetKeyName(16, "16.BMP");
		this.SignsImageList.Images.SetKeyName(17, "17.BMP");
		this.SignsImageList.Images.SetKeyName(18, "18.BMP");
		this.SignsImageList.Images.SetKeyName(19, "19.BMP");
		this.SignsImageList.Images.SetKeyName(20, "20.BMP");
		this.SignsImageList.Images.SetKeyName(21, "21.BMP");
		this.SignsImageList.Images.SetKeyName(22, "22.BMP");
		this.SignsImageList.Images.SetKeyName(23, "23.BMP");
		this.SignsImageList.Images.SetKeyName(24, "24.BMP");
		this.SignsImageList.Images.SetKeyName(25, "25.BMP");
		this.SignsImageList.Images.SetKeyName(26, "26.BMP");
		this.SignsImageList.Images.SetKeyName(27, "27.BMP");
		this.SignsImageList.Images.SetKeyName(28, "28.BMP");
		this.SignsImageList.Images.SetKeyName(29, "29.BMP");
		this.SignsImageList.Images.SetKeyName(30, "30.BMP");
		this.SignsImageList.Images.SetKeyName(31, "31.BMP");
		this.SignsImageList.Images.SetKeyName(32, "32.BMP");
		this.SignsImageList.Images.SetKeyName(33, "33.BMP");
		this.SignsImageList.Images.SetKeyName(34, "34.BMP");
		this.SignsImageList.Images.SetKeyName(35, "35.BMP");
		this.SignsImageList.Images.SetKeyName(36, "36.BMP");
		this.SignsImageList.Images.SetKeyName(37, "37.BMP");
		this.SignsImageList.Images.SetKeyName(38, "38.BMP");
		this.SignsImageList.Images.SetKeyName(39, "39.BMP");
		this.SignsImageList.Images.SetKeyName(40, "lokomotiv.png");
		this.PrepImageList.ImageStream = (System.Windows.Forms.ImageListStreamer)resources.GetObject("PrepImageList.ImageStream");
		this.PrepImageList.TransparentColor = System.Drawing.Color.Transparent;
		this.PrepImageList.Images.SetKeyName(0, "001.png");
		this.PrepImageList.Images.SetKeyName(1, "002.png");
		this.PrepImageList.Images.SetKeyName(2, "003.png");
		this.PrepImageList.Images.SetKeyName(3, "004.png");
		this.PrepImageList.Images.SetKeyName(4, "005.png");
		this.PrepImageList.Images.SetKeyName(5, "006.png");
		this.PrepImageList.Images.SetKeyName(6, "007.png");
		this.DayNightGroupBox.Controls.Add(this.nighttimeCheckBox);
		this.DayNightGroupBox.Controls.Add(this.daytimeCheckBox);
		this.DayNightGroupBox.Location = new System.Drawing.Point(12, 8);
		this.DayNightGroupBox.Name = "DayNightGroupBox";
		this.DayNightGroupBox.Size = new System.Drawing.Size(218, 131);
		this.DayNightGroupBox.TabIndex = 9;
		this.DayNightGroupBox.TabStop = false;
		this.nighttimeCheckBox.Appearance = System.Windows.Forms.Appearance.Button;
		this.nighttimeCheckBox.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
		this.nighttimeCheckBox.CheckAlign = System.Drawing.ContentAlignment.MiddleCenter;
		this.nighttimeCheckBox.FlatAppearance.BorderColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.nighttimeCheckBox.FlatAppearance.CheckedBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.nighttimeCheckBox.FlatAppearance.MouseDownBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.nighttimeCheckBox.FlatAppearance.MouseOverBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.nighttimeCheckBox.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.nighttimeCheckBox.ForeColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.nighttimeCheckBox.ImageIndex = 3;
		this.nighttimeCheckBox.ImageList = this.TimeOfDayImageList;
		this.nighttimeCheckBox.Location = new System.Drawing.Point(112, 17);
		this.nighttimeCheckBox.Name = "nighttimeCheckBox";
		this.nighttimeCheckBox.Size = new System.Drawing.Size(100, 100);
		this.nighttimeCheckBox.TabIndex = 4;
		this.nighttimeCheckBox.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageBeforeText;
		this.nighttimeCheckBox.UseVisualStyleBackColor = false;
		this.nighttimeCheckBox.CheckedChanged += new System.EventHandler(this.TimeOfDayCheckBox_CheckedChanged);
		this.daytimeCheckBox.Appearance = System.Windows.Forms.Appearance.Button;
		this.daytimeCheckBox.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
		this.daytimeCheckBox.CheckAlign = System.Drawing.ContentAlignment.MiddleCenter;
		this.daytimeCheckBox.Checked = true;
		this.daytimeCheckBox.CheckState = System.Windows.Forms.CheckState.Checked;
		this.daytimeCheckBox.FlatAppearance.BorderColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.daytimeCheckBox.FlatAppearance.CheckedBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.daytimeCheckBox.FlatAppearance.MouseDownBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.daytimeCheckBox.FlatAppearance.MouseOverBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.daytimeCheckBox.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.daytimeCheckBox.ForeColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.daytimeCheckBox.ImageIndex = 0;
		this.daytimeCheckBox.ImageList = this.TimeOfDayImageList;
		this.daytimeCheckBox.Location = new System.Drawing.Point(6, 17);
		this.daytimeCheckBox.Name = "daytimeCheckBox";
		this.daytimeCheckBox.Size = new System.Drawing.Size(100, 100);
		this.daytimeCheckBox.TabIndex = 3;
		this.daytimeCheckBox.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageBeforeText;
		this.daytimeCheckBox.UseVisualStyleBackColor = false;
		this.daytimeCheckBox.CheckedChanged += new System.EventHandler(this.TimeOfDayCheckBox_CheckedChanged);
		this.DayNightLabel.AutoSize = true;
		this.DayNightLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 10f, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, 204);
		this.DayNightLabel.ForeColor = System.Drawing.SystemColors.Control;
		this.DayNightLabel.Location = new System.Drawing.Point(79, 129);
		this.DayNightLabel.Name = "DayNightLabel";
		this.DayNightLabel.Size = new System.Drawing.Size(87, 17);
		this.DayNightLabel.TabIndex = 0;
		this.DayNightLabel.Text = "День / Ночь";
		this.WeatherGroupBox.Controls.Add(this.FogCheckBox);
		this.WeatherGroupBox.Controls.Add(this.SnowCheckBox);
		this.WeatherGroupBox.Controls.Add(this.RainCheckBox);
		this.WeatherGroupBox.Location = new System.Drawing.Point(243, 8);
		this.WeatherGroupBox.Name = "WeatherGroupBox";
		this.WeatherGroupBox.Size = new System.Drawing.Size(322, 131);
		this.WeatherGroupBox.TabIndex = 10;
		this.WeatherGroupBox.TabStop = false;
		this.FogCheckBox.Appearance = System.Windows.Forms.Appearance.Button;
		this.FogCheckBox.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
		this.FogCheckBox.CheckAlign = System.Drawing.ContentAlignment.MiddleCenter;
		this.FogCheckBox.FlatAppearance.BorderColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.FogCheckBox.FlatAppearance.CheckedBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.FogCheckBox.FlatAppearance.MouseDownBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.FogCheckBox.FlatAppearance.MouseOverBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.FogCheckBox.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.FogCheckBox.ForeColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.FogCheckBox.ImageIndex = 4;
		this.FogCheckBox.ImageList = this.WeatherImageList;
		this.FogCheckBox.Location = new System.Drawing.Point(218, 17);
		this.FogCheckBox.Name = "FogCheckBox";
		this.FogCheckBox.Size = new System.Drawing.Size(100, 100);
		this.FogCheckBox.TabIndex = 8;
		this.FogCheckBox.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageBeforeText;
		this.FogCheckBox.UseVisualStyleBackColor = false;
		this.FogCheckBox.CheckedChanged += new System.EventHandler(this.WeatherCheckBox_CheckedChanged);
		this.SnowCheckBox.Appearance = System.Windows.Forms.Appearance.Button;
		this.SnowCheckBox.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
		this.SnowCheckBox.CheckAlign = System.Drawing.ContentAlignment.MiddleCenter;
		this.SnowCheckBox.FlatAppearance.BorderColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SnowCheckBox.FlatAppearance.CheckedBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SnowCheckBox.FlatAppearance.MouseDownBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SnowCheckBox.FlatAppearance.MouseOverBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SnowCheckBox.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.SnowCheckBox.ForeColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SnowCheckBox.ImageIndex = 2;
		this.SnowCheckBox.ImageList = this.WeatherImageList;
		this.SnowCheckBox.Location = new System.Drawing.Point(112, 17);
		this.SnowCheckBox.Name = "SnowCheckBox";
		this.SnowCheckBox.Size = new System.Drawing.Size(100, 100);
		this.SnowCheckBox.TabIndex = 7;
		this.SnowCheckBox.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageBeforeText;
		this.SnowCheckBox.UseVisualStyleBackColor = false;
		this.SnowCheckBox.CheckedChanged += new System.EventHandler(this.WeatherCheckBox_CheckedChanged);
		this.RainCheckBox.Appearance = System.Windows.Forms.Appearance.Button;
		this.RainCheckBox.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
		this.RainCheckBox.CheckAlign = System.Drawing.ContentAlignment.MiddleCenter;
		this.RainCheckBox.FlatAppearance.BorderColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.RainCheckBox.FlatAppearance.CheckedBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.RainCheckBox.FlatAppearance.MouseDownBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.RainCheckBox.FlatAppearance.MouseOverBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.RainCheckBox.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.RainCheckBox.ForeColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.RainCheckBox.ImageIndex = 0;
		this.RainCheckBox.ImageList = this.WeatherImageList;
		this.RainCheckBox.Location = new System.Drawing.Point(6, 17);
		this.RainCheckBox.Name = "RainCheckBox";
		this.RainCheckBox.Size = new System.Drawing.Size(100, 100);
		this.RainCheckBox.TabIndex = 6;
		this.RainCheckBox.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageBeforeText;
		this.RainCheckBox.UseVisualStyleBackColor = false;
		this.RainCheckBox.CheckedChanged += new System.EventHandler(this.WeatherCheckBox_CheckedChanged);
		this.WeatherLabel.AutoSize = true;
		this.WeatherLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 10f, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, 204);
		this.WeatherLabel.ForeColor = System.Drawing.SystemColors.Control;
		this.WeatherLabel.Location = new System.Drawing.Point(378, 128);
		this.WeatherLabel.Name = "WeatherLabel";
		this.WeatherLabel.Size = new System.Drawing.Size(55, 17);
		this.WeatherLabel.TabIndex = 11;
		this.WeatherLabel.Text = "Погода";
		this.BrakeSystemFaultsLabel.AutoSize = true;
		this.BrakeSystemFaultsLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 10f, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, 204);
		this.BrakeSystemFaultsLabel.ForeColor = System.Drawing.SystemColors.Control;
		this.BrakeSystemFaultsLabel.Location = new System.Drawing.Point(609, 128);
		this.BrakeSystemFaultsLabel.Name = "BrakeSystemFaultsLabel";
		this.BrakeSystemFaultsLabel.Size = new System.Drawing.Size(245, 17);
		this.BrakeSystemFaultsLabel.TabIndex = 12;
		this.BrakeSystemFaultsLabel.Text = "Неисправности тормозной системы";
		this.ElectricalCircuitMalfunctionsLabel.AutoSize = true;
		this.ElectricalCircuitMalfunctionsLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 10f, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, 204);
		this.ElectricalCircuitMalfunctionsLabel.ForeColor = System.Drawing.SystemColors.Control;
		this.ElectricalCircuitMalfunctionsLabel.Location = new System.Drawing.Point(934, 128);
		this.ElectricalCircuitMalfunctionsLabel.Name = "ElectricalCircuitMalfunctionsLabel";
		this.ElectricalCircuitMalfunctionsLabel.Size = new System.Drawing.Size(255, 17);
		this.ElectricalCircuitMalfunctionsLabel.TabIndex = 14;
		this.ElectricalCircuitMalfunctionsLabel.Text = "Неисправности электрических цепей";
		this.ElectricalCircuitMalfunctionsDataGridView.AllowUserToAddRows = false;
		this.ElectricalCircuitMalfunctionsDataGridView.AllowUserToDeleteRows = false;
		this.ElectricalCircuitMalfunctionsDataGridView.AllowUserToResizeColumns = false;
		this.ElectricalCircuitMalfunctionsDataGridView.AllowUserToResizeRows = false;
		this.ElectricalCircuitMalfunctionsDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
		this.ElectricalCircuitMalfunctionsDataGridView.BackgroundColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.ElectricalCircuitMalfunctionsDataGridView.BorderStyle = System.Windows.Forms.BorderStyle.None;
		this.ElectricalCircuitMalfunctionsDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
		this.ElectricalCircuitMalfunctionsDataGridView.ColumnHeadersVisible = false;
		this.ElectricalCircuitMalfunctionsDataGridView.Columns.AddRange(this.dataGridViewCheckBoxColumn1, this.dataGridViewTextBoxColumn1);
		this.ElectricalCircuitMalfunctionsDataGridView.EditMode = System.Windows.Forms.DataGridViewEditMode.EditOnEnter;
		this.ElectricalCircuitMalfunctionsDataGridView.Location = new System.Drawing.Point(6, 11);
		this.ElectricalCircuitMalfunctionsDataGridView.MultiSelect = false;
		this.ElectricalCircuitMalfunctionsDataGridView.Name = "ElectricalCircuitMalfunctionsDataGridView";
		this.ElectricalCircuitMalfunctionsDataGridView.RowHeadersVisible = false;
		this.ElectricalCircuitMalfunctionsDataGridView.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
		this.ElectricalCircuitMalfunctionsDataGridView.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
		this.ElectricalCircuitMalfunctionsDataGridView.ShowCellErrors = false;
		this.ElectricalCircuitMalfunctionsDataGridView.ShowCellToolTips = false;
		this.ElectricalCircuitMalfunctionsDataGridView.ShowEditingIcon = false;
		this.ElectricalCircuitMalfunctionsDataGridView.ShowRowErrors = false;
		this.ElectricalCircuitMalfunctionsDataGridView.Size = new System.Drawing.Size(300, 109);
		this.ElectricalCircuitMalfunctionsDataGridView.TabIndex = 0;
		this.ElectricalCircuitMalfunctionsDataGridView.EnabledChanged += new System.EventHandler(this.DataGridView_EnabledChanged);
		dataGridViewCellStyle3.Alignment = System.Windows.Forms.DataGridViewContentAlignment.MiddleCenter;
		dataGridViewCellStyle3.BackColor = System.Drawing.SystemColors.AppWorkspace;
		dataGridViewCellStyle3.NullValue = false;
		dataGridViewCellStyle3.SelectionBackColor = System.Drawing.SystemColors.ControlDarkDark;
		this.dataGridViewCheckBoxColumn1.DefaultCellStyle = dataGridViewCellStyle3;
		this.dataGridViewCheckBoxColumn1.FillWeight = 25.38071f;
		this.dataGridViewCheckBoxColumn1.HeaderText = "IsDisplayColumn";
		this.dataGridViewCheckBoxColumn1.Name = "dataGridViewCheckBoxColumn1";
		dataGridViewCellStyle4.BackColor = System.Drawing.SystemColors.AppWorkspace;
		dataGridViewCellStyle4.SelectionBackColor = System.Drawing.SystemColors.ControlDarkDark;
		this.dataGridViewTextBoxColumn1.DefaultCellStyle = dataGridViewCellStyle4;
		this.dataGridViewTextBoxColumn1.FillWeight = 174.6193f;
		this.dataGridViewTextBoxColumn1.HeaderText = "MainColumn";
		this.dataGridViewTextBoxColumn1.Name = "dataGridViewTextBoxColumn1";
		this.dataGridViewTextBoxColumn1.ReadOnly = true;
		this.dataGridViewTextBoxColumn1.SortMode = System.Windows.Forms.DataGridViewColumnSortMode.NotSortable;
		this.ElectricalCircuitMalfunctionsGroupBox.Controls.Add(this.ElectricalCircuitMalfunctionsDataGridView);
		this.ElectricalCircuitMalfunctionsGroupBox.Location = new System.Drawing.Point(902, 8);
		this.ElectricalCircuitMalfunctionsGroupBox.Margin = new System.Windows.Forms.Padding(0);
		this.ElectricalCircuitMalfunctionsGroupBox.Name = "ElectricalCircuitMalfunctionsGroupBox";
		this.ElectricalCircuitMalfunctionsGroupBox.Padding = new System.Windows.Forms.Padding(0);
		this.ElectricalCircuitMalfunctionsGroupBox.Size = new System.Drawing.Size(312, 131);
		this.ElectricalCircuitMalfunctionsGroupBox.TabIndex = 13;
		this.ElectricalCircuitMalfunctionsGroupBox.TabStop = false;
		this.checkBox1.Appearance = System.Windows.Forms.Appearance.Button;
		this.checkBox1.Location = new System.Drawing.Point(1391, 45);
		this.checkBox1.Name = "checkBox1";
		this.checkBox1.Size = new System.Drawing.Size(137, 42);
		this.checkBox1.TabIndex = 15;
		this.checkBox1.Text = "Line test";
		this.checkBox1.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
		this.checkBox1.UseVisualStyleBackColor = true;
		this.checkBox1.CheckedChanged += new System.EventHandler(this.checkBox1_CheckedChanged);
		this.timer1.Interval = 50;
		this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
		this.checkBox3.AutoSize = true;
		this.checkBox3.Location = new System.Drawing.Point(1391, 93);
		this.checkBox3.Name = "checkBox3";
		this.checkBox3.Size = new System.Drawing.Size(169, 17);
		this.checkBox3.TabIndex = 1;
		this.checkBox3.Text = "Тест переключения режима";
		this.checkBox3.UseVisualStyleBackColor = true;
		this.checkBox3.CheckedChanged += new System.EventHandler(this.checkBox3_CheckedChanged);
		this.SaveButton.FlatAppearance.BorderSize = 0;
		this.SaveButton.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.SaveButton.Image = (System.Drawing.Image)resources.GetObject("SaveButton.Image");
		this.SaveButton.Location = new System.Drawing.Point(1702, 25);
		this.SaveButton.Name = "SaveButton";
		this.SaveButton.Size = new System.Drawing.Size(100, 100);
		this.SaveButton.TabIndex = 23;
		this.SaveButton.UseVisualStyleBackColor = true;
		this.SaveButton.Click += new System.EventHandler(this.SaveButton_Click);
		this.SaveButton.MouseDown += new System.Windows.Forms.MouseEventHandler(this.SaveButton_MouseDown);
		this.SaveButton.MouseUp += new System.Windows.Forms.MouseEventHandler(this.SaveButton_MouseUp);
		this.CloseButton.FlatAppearance.BorderSize = 0;
		this.CloseButton.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.CloseButton.Image = ScenarioCreater.Properties.Resources.ExitButton;
		this.CloseButton.Location = new System.Drawing.Point(1808, 25);
		this.CloseButton.Name = "CloseButton";
		this.CloseButton.Size = new System.Drawing.Size(100, 100);
		this.CloseButton.TabIndex = 22;
		this.CloseButton.UseVisualStyleBackColor = true;
		this.CloseButton.Click += new System.EventHandler(this.CloseButton_Click);
		this.CloseButton.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CloseButton_MouseDown);
		this.CloseButton.MouseUp += new System.Windows.Forms.MouseEventHandler(this.CloseButton_MouseUp);
		this.SearchTrainCheckBox.Appearance = System.Windows.Forms.Appearance.Button;
		this.SearchTrainCheckBox.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
		this.SearchTrainCheckBox.CheckAlign = System.Drawing.ContentAlignment.MiddleCenter;
		this.SearchTrainCheckBox.FlatAppearance.BorderColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SearchTrainCheckBox.FlatAppearance.CheckedBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SearchTrainCheckBox.FlatAppearance.MouseDownBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SearchTrainCheckBox.FlatAppearance.MouseOverBackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SearchTrainCheckBox.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
		this.SearchTrainCheckBox.ForeColor = System.Drawing.Color.FromArgb(81, 81, 81);
		this.SearchTrainCheckBox.Image = (System.Drawing.Image)resources.GetObject("SearchTrainCheckBox.Image");
		this.SearchTrainCheckBox.Location = new System.Drawing.Point(1228, 25);
		this.SearchTrainCheckBox.Name = "SearchTrainCheckBox";
		this.SearchTrainCheckBox.Size = new System.Drawing.Size(100, 100);
		this.SearchTrainCheckBox.TabIndex = 24;
		this.SearchTrainCheckBox.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageBeforeText;
		this.SearchTrainCheckBox.UseVisualStyleBackColor = false;
		this.SearchTrainCheckBox.CheckedChanged += new System.EventHandler(this.SearchTrainCheckBox_CheckedChanged);
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		this.BackColor = System.Drawing.Color.FromArgb(81, 81, 81);
		base.ClientSize = new System.Drawing.Size(1920, 1080);
		base.Controls.Add(this.SearchTrainCheckBox);
		base.Controls.Add(this.SaveButton);
		base.Controls.Add(this.CloseButton);
		base.Controls.Add(this.checkBox3);
		base.Controls.Add(this.checkBox1);
		base.Controls.Add(this.ElectricalCircuitMalfunctionsLabel);
		base.Controls.Add(this.ElectricalCircuitMalfunctionsGroupBox);
		base.Controls.Add(this.BrakeSystemFaultsLabel);
		base.Controls.Add(this.WeatherLabel);
		base.Controls.Add(this.WeatherGroupBox);
		base.Controls.Add(this.DayNightLabel);
		base.Controls.Add(this.DayNightGroupBox);
		base.Controls.Add(this.BrakeSystemFaultsGroupBox);
		base.Controls.Add(this.ZGC);
		base.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
		base.MaximizeBox = false;
		base.MinimizeBox = false;
		base.Name = "PathProfileForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "ScenarioCreater";
		base.Load += new System.EventHandler(this.PathProfileForm_Load);
		base.Shown += new System.EventHandler(this.PathProfileForm_Shown);
		this.BrakeSystemFaultsGroupBox.ResumeLayout(false);
		((System.ComponentModel.ISupportInitialize)this.BrakeSystemFaultsDataGridView).EndInit();
		this.DayNightGroupBox.ResumeLayout(false);
		this.WeatherGroupBox.ResumeLayout(false);
		((System.ComponentModel.ISupportInitialize)this.ElectricalCircuitMalfunctionsDataGridView).EndInit();
		this.ElectricalCircuitMalfunctionsGroupBox.ResumeLayout(false);
		base.ResumeLayout(false);
		base.PerformLayout();
	}
}
