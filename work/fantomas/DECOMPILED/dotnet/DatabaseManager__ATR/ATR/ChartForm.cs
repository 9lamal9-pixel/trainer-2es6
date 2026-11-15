using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;
using System.Windows.Forms.DataVisualization.Charting;

namespace ATR;

public class ChartForm : Form
{
	public class DataCollection
	{
		public string X { get; set; }

		public double Y { get; set; }

		public string Text { get; set; }
	}

	private ATRMainForm mainForm;

	private bool isNeedAxisXLabelsUpdate;

	private List<DataCollection> data = new List<DataCollection>();

	private IContainer components;

	private Button ExitButton;

	private Chart mainChart;

	public ChartForm()
	{
		InitializeComponent();
	}

	public ChartForm(ATRMainForm _mainForm)
	{
		InitializeComponent();
		mainForm = _mainForm;
	}

	private void ExitButton_Click(object sender, EventArgs e)
	{
		Close();
	}

	private void PrintButton_Click(object sender, EventArgs e)
	{
	}

	private void ChartForm_Load(object sender, EventArgs e)
	{
		mainChart.BeginInit();
		mainChart.Titles.Clear();
		mainChart.Annotations.Clear();
		mainChart.Legends.Clear();
		mainChart.ChartAreas[0].AxisX.MajorGrid.Enabled = false;
		mainChart.ChartAreas[0].AxisX.MinorGrid.Enabled = false;
		mainChart.ChartAreas[0].AxisX.MajorTickMark.Enabled = false;
		List<Dictionary<string, string>> list = new List<Dictionary<string, string>>();
		switch (mainForm.ImmersionLevel)
		{
		case 0:
		{
			list = mainForm.GetSQLArray("SELECT id, name FROM student_groups");
			for (int k = 0; k < list.Count; k++)
			{
				string sQLValue2 = mainForm.GetSQLValue("SELECT IFNULL(CAST(sum(driving_sessions.total_fault_points) as REAL) / count(driving_sessions.id), 0) FROM driving_sessions, students WHERE(driving_sessions.student_id = students.id) AND(students.group_id = " + list[k]["id"].ToString() + ")");
				data.Add(new DataCollection
				{
					X = k.ToString(),
					Y = Convert.ToDouble(ATRMainForm.Get2DigitsFromNumber(sQLValue2)),
					Text = list[k]["name"].ToString()
				});
			}
			Text = "Анализ результатов по группам.";
			mainChart.Titles.Add("Средний штрафной балл");
			break;
		}
		case 1:
		{
			list = mainForm.GetSQLArray("SELECT id, name FROM students WHERE group_id = " + mainForm.GroupID);
			for (int j = 0; j < list.Count; j++)
			{
				string sQLValue = mainForm.GetSQLValue("SELECT IFNULL(CAST(sum(driving_sessions.total_fault_points) as REAL), 0) FROM driving_sessions WHERE (driving_sessions.student_id = " + list[j]["id"].ToString() + ")");
				data.Add(new DataCollection
				{
					X = j.ToString(),
					Y = Convert.ToDouble(sQLValue),
					Text = list[j]["name"].ToString()
				});
			}
			Text = "Анализ результатов по обучаемым. Группа: " + mainForm.GroupCaption;
			mainChart.Titles.Add("Общее кол-во штрафных баллов по нарушениям");
			break;
		}
		case 2:
		{
			mainChart.ChartAreas[0].AxisX.LabelStyle.Angle = -90;
			list = mainForm.GetSQLArray("SELECT CAST(time AS TEXT) AS time, total_fault_points FROM driving_sessions WHERE student_id = " + mainForm.traineeID);
			for (int i = 0; i < list.Count; i++)
			{
				data.Add(new DataCollection
				{
					X = i.ToString(),
					Y = Convert.ToDouble(list[i]["total_fault_points"].ToString()),
					Text = list[i]["time"].ToString()
				});
			}
			Text = "Анализ результатов по выбранному обучаемому. Группа: " + mainForm.GroupCaption + " Обучающийся: " + mainForm.traineeName;
			mainChart.Titles.Add("Сумма штрафных баллов по нарушениям за поездку");
			break;
		}
		}
		mainChart.Series[0] = new Series();
		mainChart.Series[0].XValueMember = "X";
		mainChart.Series[0].YValueMembers = "Y";
		mainChart.Series[0].IsValueShownAsLabel = true;
		mainChart.ChartAreas[0].AxisX.LabelStyle.Interval = 1.0;
		mainChart.ChartAreas[0].AxisX.Interval = 1.0;
		mainChart.ChartAreas[0].AxisX.Minimum = Convert.ToDouble(0);
		mainChart.ChartAreas[0].AxisX.Maximum = Convert.ToDouble(data[data.Count - 1].X) + 2.0;
		mainChart.ChartAreas[0].CursorX.AutoScroll = true;
		mainChart.ChartAreas[0].AxisX.ScaleView.Zoomable = true;
		mainChart.ChartAreas[0].AxisX.ScaleView.SizeType = DateTimeIntervalType.Number;
		mainChart.ChartAreas[0].AxisX.ScrollBar.ButtonStyle = ScrollBarButtonStyles.SmallScroll;
		double viewMinimum = mainChart.ChartAreas[0].AxisX.ScaleView.ViewMinimum;
		double viewMaximum = mainChart.ChartAreas[0].AxisX.ScaleView.ViewMaximum;
		mainChart.ChartAreas[0].AxisX.ScaleView.SmallScrollSize = (viewMaximum - viewMinimum) / 100.0;
		mainChart.ChartAreas[0].AxisX.ScaleView.MinSizeType = DateTimeIntervalType.Number;
		mainChart.ChartAreas[0].AxisX.ScaleView.Zoom(0.0, mainChart.Width / 17);
		mainChart.ChartAreas[0].AxisY.MajorGrid.Enabled = false;
		mainChart.ChartAreas[0].AxisY.MinorGrid.Enabled = false;
		mainChart.ChartAreas[0].AxisY.LabelStyle.Enabled = false;
		mainChart.ChartAreas[0].AxisY.MajorTickMark.Enabled = false;
		mainChart.ChartAreas[0].AxisY.LineColor = Color.Transparent;
		mainChart.ChartAreas[0].Position.Auto = false;
		mainChart.ChartAreas[0].Position.X = 1f;
		mainChart.ChartAreas[0].Position.Width = 98f;
		mainChart.ChartAreas[0].AxisX.IsMarginVisible = false;
		mainChart.ChartAreas[0].InnerPlotPosition.Auto = false;
		mainChart.ChartAreas[0].InnerPlotPosition.X = 0f;
		mainChart.ChartAreas[0].InnerPlotPosition.Width = 100f;
		mainChart.ChartAreas[0].Position.Y = 0f;
		mainChart.ChartAreas[0].Position.Height = 100f;
		mainChart.ChartAreas[0].InnerPlotPosition.Y = 0f;
		mainChart.ChartAreas[0].InnerPlotPosition.Height = 79f;
		mainChart.ChartAreas[0].AxisX.ScrollBar.Enabled = true;
		mainChart.DataSource = data;
		mainChart.DataBind();
		Random random = new Random();
		for (int l = 0; l < mainChart.Series[0].Points.Count; l++)
		{
			mainChart.Series[0].Points[l].Color = Color.FromArgb(random.Next(256), random.Next(256), random.Next(256));
		}
		mainChart.EndInit();
		isNeedAxisXLabelsUpdate = true;
	}

	private void generateAxisXSign()
	{
		mainChart.ChartAreas[0].AxisX.CustomLabels.Clear();
		double num = 0.1;
		CustomLabel customLabel = new CustomLabel(0.0, 1.0, "", 0, LabelMarkStyle.None, GridTickTypes.None);
		for (int i = 0; i < data.Count; i++)
		{
			double fromPosition = (double)(i + 1) - 0.1;
			num = (double)(i + 1) + 0.1;
			customLabel = new CustomLabel(fromPosition, num, data[i].Text, 0, LabelMarkStyle.None, GridTickTypes.TickMark);
			mainChart.ChartAreas[0].AxisX.CustomLabels.Add(customLabel);
		}
	}

	private void ChartForm_Resize(object sender, EventArgs e)
	{
		mainChart.Width = ClientSize.Width - 23;
		mainChart.Height = ClientSize.Height - 69;
		ExitButton.Top = ClientSize.Height - 51;
		ExitButton.Width = ClientSize.Width - 23;
		double viewMinimum = mainChart.ChartAreas[0].AxisX.ScaleView.ViewMinimum;
		double num = viewMinimum + (double)(mainChart.Width / 17);
		mainChart.ChartAreas[0].AxisX.ScaleView.SmallScrollSize = (num - viewMinimum) / 100.0;
		mainChart.ChartAreas[0].AxisX.ScaleView.Zoom(viewMinimum, num);
	}

	private void mainChart_Paint(object sender, PaintEventArgs e)
	{
		if (isNeedAxisXLabelsUpdate)
		{
			generateAxisXSign();
			isNeedAxisXLabelsUpdate = false;
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
		System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
		System.Windows.Forms.DataVisualization.Charting.Legend legend = new System.Windows.Forms.DataVisualization.Charting.Legend();
		System.Windows.Forms.DataVisualization.Charting.Series series = new System.Windows.Forms.DataVisualization.Charting.Series();
		this.ExitButton = new System.Windows.Forms.Button();
		this.mainChart = new System.Windows.Forms.DataVisualization.Charting.Chart();
		((System.ComponentModel.ISupportInitialize)this.mainChart).BeginInit();
		base.SuspendLayout();
		this.ExitButton.Location = new System.Drawing.Point(12, 627);
		this.ExitButton.Name = "ExitButton";
		this.ExitButton.Size = new System.Drawing.Size(887, 39);
		this.ExitButton.TabIndex = 0;
		this.ExitButton.Text = "Закрыть";
		this.ExitButton.UseVisualStyleBackColor = true;
		this.ExitButton.Click += new System.EventHandler(this.ExitButton_Click);
		chartArea.Name = "ChartArea1";
		this.mainChart.ChartAreas.Add(chartArea);
		legend.Name = "Legend1";
		this.mainChart.Legends.Add(legend);
		this.mainChart.Location = new System.Drawing.Point(12, 12);
		this.mainChart.Name = "mainChart";
		series.ChartArea = "ChartArea1";
		series.Legend = "Legend1";
		series.Name = "Series1";
		this.mainChart.Series.Add(series);
		this.mainChart.Size = new System.Drawing.Size(887, 609);
		this.mainChart.TabIndex = 1;
		this.mainChart.Text = "chart1";
		this.mainChart.Paint += new System.Windows.Forms.PaintEventHandler(this.mainChart_Paint);
		base.AutoScaleDimensions = new System.Drawing.SizeF(6f, 13f);
		base.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
		base.ClientSize = new System.Drawing.Size(911, 678);
		base.Controls.Add(this.mainChart);
		base.Controls.Add(this.ExitButton);
		this.MinimumSize = new System.Drawing.Size(250, 700);
		base.Name = "ChartForm";
		base.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
		this.Text = "Диаграмма";
		base.Load += new System.EventHandler(this.ChartForm_Load);
		base.Resize += new System.EventHandler(this.ChartForm_Resize);
		((System.ComponentModel.ISupportInitialize)this.mainChart).EndInit();
		base.ResumeLayout(false);
	}
}
