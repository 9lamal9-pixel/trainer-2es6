using System;
using System.Drawing;
using System.Windows.Forms;
using ScenarioCreater.Properties;
using ZedGraph;

namespace ScenarioCreater;

internal class PathProfile
{
	private ZedGraphControl _ZGC;

	private LineObj _line_begin;

	private LineObj _line_end;

	private ZedImageFixed _swallow;

	public PathProfile(ZedGraphControl ZGC)
	{
		_ZGC = ZGC;
	}

	public void Init()
	{
		MasterPane masterPane = _ZGC.MasterPane;
		masterPane.PaneList.Clear();
		GraphPane pane = new GraphPane(new Rectangle(5, 5, 10, 10), "", "Time in Minutes", "");
		masterPane.Add(pane);
		GraphPane pane2 = new GraphPane(new Rectangle(5, 5, 10, 10), "", "Time in Minutes", "");
		masterPane.Add(pane2);
		masterPane.InnerPaneGap = 0f;
		_ZGC.IsSynchronizeXAxes = true;
		_ZGC.MasterPane.IsCommonScaleFactor = true;
		masterPane.Fill.Type = FillType.Solid;
		masterPane.Fill.Color = Color.FromArgb(82, 82, 82);
		using (Graphics g = _ZGC.CreateGraphics())
		{
			masterPane.SetLayout(g, PaneLayout.SingleColumn);
		}
		for (int i = 0; i < masterPane.PaneList.Count; i++)
		{
			GraphPane graphPane = masterPane.PaneList[i];
			graphPane.Border.Color = Color.Red;
			graphPane.Chart.Border.Color = Color.Black;
			graphPane.Fill.Type = FillType.Solid;
			graphPane.Fill.Color = Color.FromArgb(82, 82, 82);
			graphPane.Chart.Fill.Type = FillType.Solid;
			graphPane.Chart.Fill.Color = Color.LightGray;
			graphPane.XAxis.MajorGrid.IsZeroLine = true;
			graphPane.YAxis.MajorGrid.IsZeroLine = true;
			graphPane.XAxis.Color = Color.Gray;
			graphPane.YAxis.Color = Color.Gray;
			graphPane.XAxis.MajorGrid.IsVisible = true;
			graphPane.YAxis.MajorGrid.IsVisible = true;
			graphPane.XAxis.MajorGrid.Color = Color.Black;
			graphPane.YAxis.MajorGrid.Color = Color.Black;
			graphPane.XAxis.Scale.FontSpec.FontColor = Color.GreenYellow;
			graphPane.YAxis.Scale.FontSpec.FontColor = Color.GreenYellow;
			graphPane.Title.FontSpec.FontColor = Color.Blue;
			graphPane.IsFontsScaled = false;
			graphPane.XAxis.Scale.FontSpec.Size = 10f;
			graphPane.YAxis.Scale.FontSpec.Size = 10f;
			graphPane.Title.IsVisible = false;
			graphPane.Legend.IsVisible = false;
			graphPane.YAxis.Title.IsVisible = false;
			graphPane.XAxis.Title.IsVisible = false;
			graphPane.XAxis.Title.IsVisible = false;
			graphPane.XAxis.Scale.IsVisible = false;
			graphPane.Legend.IsVisible = false;
			graphPane.Border.IsVisible = false;
			graphPane.Title.IsVisible = false;
			graphPane.XAxis.MajorTic.IsOutside = false;
			graphPane.XAxis.MinorTic.IsOutside = false;
			graphPane.Margin.All = 0f;
			graphPane.YAxis.MinSpace = 10f;
			graphPane.Y2Axis.MinSpace = 10f;
			graphPane.YAxis.Scale.IsVisible = false;
			masterPane.PaneList[masterPane.PaneList.Count - 1].XAxis.MajorTic.IsAllTics = false;
			masterPane.PaneList[masterPane.PaneList.Count - 1].XAxis.MinorTic.IsAllTics = false;
		}
		masterPane.PaneList[masterPane.PaneList.Count - 1].YAxis.Scale.IsVisible = false;
		masterPane.PaneList[masterPane.PaneList.Count - 1].XAxis.Scale.IsVisible = false;
		_ZGC.IsShowHScrollBar = true;
		_ZGC.IsAutoScrollRange = true;
	}

	public void Update(RailRoadData _RD, ImageList _TLIL, ImageList _SIL, ImageList _PrepIL)
	{
		if (_ZGC == null)
		{
			return;
		}
		GraphPane graphPane = _ZGC.MasterPane.PaneList[0];
		graphPane.CurveList.Clear();
		_ZGC.MasterPane.PaneList[1].GraphObjList.Clear();
		double num = 0.0;
		double num2 = 0.0;
		PointPairList pointPairList = new PointPairList();
		pointPairList.Add(0.0, (double)_RD.profile[0].distance * _RD.profile[0].slope * 0.001);
		foreach (ProfileData item in _RD.profile)
		{
			num += (double)item.distance * 0.001;
			num2 += (double)item.distance * item.slope * 0.001;
			pointPairList.Add(num, num2);
		}
		double num3 = 0.0;
		for (int i = 0; i < pointPairList.Count; i++)
		{
			if (pointPairList[i].Y < num3)
			{
				num3 = pointPairList[i].Y;
			}
		}
		if (num3 < 0.0)
		{
			for (int j = 0; j < pointPairList.Count; j++)
			{
				pointPairList[j].Y = pointPairList[j].Y + Math.Abs(num3);
			}
		}
		LineItem lineItem = graphPane.AddCurve("Профиль пути", pointPairList, Color.Black, SymbolType.None);
		lineItem.Line.Width = 2f;
		lineItem.Line.IsAntiAlias = true;
		lineItem.Line.Fill = new Fill(Color.White, Color.Yellow, Color.Green, 90f);
		foreach (TrafficLightData light in _RD.lights)
		{
			ZedImageFixed zedImageFixed = new ZedImageFixed(_TLIL.Images[light.light + ".png"], new RectangleF((float)((double)light.coord * 0.001), 4.2f, 1f, 1.6f), CoordType.AxisXYScale, AlignH.Right, AlignV.Center, _TLIL.ImageSize.Width, _TLIL.ImageSize.Height);
			zedImageFixed.IsVisible = true;
			zedImageFixed.IsScaled = false;
			zedImageFixed.IsClippedToChartRect = true;
			zedImageFixed.ZOrder = ZOrder.B_BehindLegend;
			zedImageFixed.Tag = new
			{
				type = "TrafficLight",
				value = light
			};
			_ZGC.MasterPane.PaneList[1].GraphObjList.Add(zedImageFixed);
			TextObj textObj = new TextObj(light.name, (double)light.coord * 0.001, 2.5, CoordType.AxisXYScale, AlignH.Left, AlignV.Top);
			textObj.IsVisible = true;
			textObj.FontSpec.IsAntiAlias = true;
			textObj.FontSpec.Border.Width = 2f;
			textObj.ZOrder = ZOrder.D_BehindAxis;
			textObj.IsClippedToChartRect = true;
			textObj.ZOrder = ZOrder.B_BehindLegend;
			_ZGC.MasterPane.PaneList[1].GraphObjList.Add(textObj);
		}
		foreach (SignData signal in _RD.signals)
		{
			ZedImageFixed zedImageFixed2 = new ZedImageFixed(_SIL.Images[signal.type + ".BMP"], new RectangleF((float)((double)signal.coord * 0.001), 6f, 1f, 4f), CoordType.AxisXYScale, AlignH.Right, AlignV.Center, _SIL.ImageSize.Width, _SIL.ImageSize.Height);
			zedImageFixed2.IsVisible = true;
			zedImageFixed2.IsScaled = false;
			zedImageFixed2.IsClippedToChartRect = true;
			zedImageFixed2.Tag = new
			{
				type = "Signal",
				value = signal
			};
			zedImageFixed2.ZOrder = ZOrder.B_BehindLegend;
			_ZGC.MasterPane.PaneList[1].GraphObjList.Add(zedImageFixed2);
		}
		foreach (StationData station in _RD.stations)
		{
			TextObj textObj2 = new TextObj(station.name, (double)station.coord * 0.001, 1.0, CoordType.AxisXYScale, AlignH.Left, AlignV.Top);
			textObj2.IsVisible = true;
			textObj2.FontSpec.IsAntiAlias = true;
			textObj2.FontSpec.Border.Width = 1f;
			textObj2.IsClippedToChartRect = true;
			textObj2.Tag = new
			{
				type = "Station",
				value = station
			};
			textObj2.ZOrder = ZOrder.B_BehindLegend;
			_ZGC.MasterPane.PaneList[1].GraphObjList.Add(textObj2);
		}
		foreach (string key2 in _RD.obstructions.Keys)
		{
			foreach (ObstructionData item2 in _RD.obstructions[key2])
			{
				string key = "";
				if (key2 == "cars")
				{
					key = (item2.actively ? "002.png" : "001.png");
				}
				else if (!item2.actively)
				{
					key = "003.png";
				}
				else
				{
					switch (key2)
					{
					case "rocks":
						key = "005.png";
						break;
					case "threes":
						key = "004.png";
						break;
					case "vagons":
						key = "007.png";
						break;
					case "liters":
						key = "006.png";
						break;
					}
				}
				ZedImageFixed zedImageFixed3 = new ZedImageFixed(_PrepIL.Images[key], new RectangleF((float)((double)item2.coord * 0.001), 8f, 1f, 4f), CoordType.AxisXYScale, AlignH.Right, AlignV.Center, _PrepIL.ImageSize.Width, _PrepIL.ImageSize.Height);
				zedImageFixed3.IsVisible = true;
				zedImageFixed3.IsScaled = false;
				zedImageFixed3.IsClippedToChartRect = true;
				zedImageFixed3.Tag = new
				{
					type = key2,
					value = item2
				};
				zedImageFixed3.ZOrder = ZOrder.B_BehindLegend;
				_ZGC.MasterPane.PaneList[1].GraphObjList.Add(zedImageFixed3);
			}
		}
		graphPane.XAxis.Scale.Max = num;
		_ZGC.AxisChange();
		foreach (GraphPane pane in _ZGC.MasterPane.PaneList)
		{
			pane.XAxis.Scale.Min = 0.0;
			pane.XAxis.Scale.Max = 10.0;
			pane.AxisChange();
		}
		graphPane.Rect = new RectangleF(0f, 0f, graphPane.Rect.Width, 600f);
		graphPane.XAxis.Scale.IsVisible = false;
		GraphPane graphPane2 = _ZGC.MasterPane.PaneList[1];
		graphPane2.Rect = new RectangleF(0f, 600f, graphPane2.Rect.Width, 225f);
		graphPane2.Chart.IsRectAuto = false;
		graphPane2.Chart.Rect = new RectangleF(10f, 600f, graphPane2.Rect.Width - 20f, 215f);
		graphPane2.YAxis.Scale.Min = 0.0;
		graphPane2.YAxis.Scale.Max = 10.0;
		graphPane2.AxisChange();
		foreach (PiketData piket in _RD.pikets)
		{
			if (piket.name == 0)
			{
				TextObj textObj3 = new TextObj(piket.km.ToString(), (double)piket.coord * 0.001, -0.27);
				textObj3.Location.AlignH = AlignH.Center;
				textObj3.FontSpec.Border.IsVisible = false;
				textObj3.FontSpec.Fill.IsVisible = false;
				textObj3.FontSpec.FontColor = Color.Beige;
				textObj3.FontSpec.Size = 10f;
				textObj3.ZOrder = ZOrder.A_InFront;
				textObj3.Tag = "Piket";
				_ZGC.MasterPane.PaneList[1].GraphObjList.Add(textObj3);
				LineObj lineObj = new LineObj((double)piket.coord * 0.001, _ZGC.MasterPane.PaneList[1].YAxis.Scale.Min, (double)piket.coord * 0.001, 0.2);
				lineObj.Line.Width = 1f;
				lineObj.IsClippedToChartRect = true;
				_ZGC.MasterPane.PaneList[1].GraphObjList.Add(lineObj);
			}
			else
			{
				LineObj lineObj2 = new LineObj((double)piket.coord * 0.001, _ZGC.MasterPane.PaneList[1].YAxis.Scale.Min, (double)piket.coord * 0.001, 0.1);
				lineObj2.Line.Width = 1f;
				lineObj2.IsClippedToChartRect = true;
				_ZGC.MasterPane.PaneList[1].GraphObjList.Add(lineObj2);
			}
		}
		_ZGC.Invalidate();
	}

	private static double GetAngle(PointD P1, PointD P2)
	{
		return Math.Acos(Math.Round((P1.X * P2.X + P1.Y * P2.Y) / (Math.Sqrt(P1.X * P1.X + P1.Y * P1.Y) * Math.Sqrt(P2.X * P2.X + P2.Y * P2.Y)), 9));
	}

	private static double ToDegrees(double Angle)
	{
		return Angle * 180.0 / Math.PI;
	}

	public void SetVerticalLine(double xPos, bool isNeedFollow)
	{
		GraphPane graphPane = _ZGC.MasterPane.PaneList[1];
		if (xPos >= graphPane.XAxis.Scale.Min && xPos <= graphPane.XAxis.Scale.Max)
		{
			if (_line_begin == null)
			{
				_line_begin = new LineObj(Color.Black, xPos, 0.0, xPos, 1.0);
				_line_begin.Location.CoordinateFrame = CoordType.XScaleYChartFraction;
				_line_begin.IsClippedToChartRect = true;
				_line_begin.Line.Width = 2f;
				_line_begin.Line.Color = Color.Red;
				_line_begin.IsVisible = true;
				_swallow = new ZedImageFixed(Resources.swallow, new RectangleF((float)xPos - 0.126f, 1f, 0.126f, 1f), CoordType.AxisXYScale, AlignH.Right, AlignV.Center, 400, 18);
				_swallow.IsVisible = true;
				_swallow.IsScaled = true;
				_swallow.IsClippedToChartRect = true;
				_swallow.ZOrder = ZOrder.B_BehindLegend;
				_line_end = new LineObj(Color.Black, xPos - _swallow.Location.Width, 0.0, xPos - _swallow.Location.Width, 1.0);
				_line_end.Location.CoordinateFrame = CoordType.XScaleYChartFraction;
				_line_end.IsClippedToChartRect = true;
				_line_end.Line.Width = 1f;
				_line_end.Line.Color = Color.Red;
				_line_end.IsVisible = true;
				_ZGC.MasterPane.PaneList[0].GraphObjList.Add(_swallow);
				_ZGC.MasterPane.PaneList[0].GraphObjList.Add(_line_begin);
				_ZGC.MasterPane.PaneList[1].GraphObjList.Add(_line_begin);
				_ZGC.MasterPane.PaneList[0].GraphObjList.Add(_line_end);
				_ZGC.MasterPane.PaneList[1].GraphObjList.Add(_line_end);
			}
			else
			{
				_line_begin.IsVisible = true;
				_line_begin.Location.X = xPos;
				_line_end.IsVisible = true;
				_line_end.Location.X = xPos - _swallow.Location.Width;
				_ = _ZGC.MasterPane.PaneList[0].CurveList[0];
				_ = graphPane.XAxis.Scale.Max;
				_ = graphPane.XAxis.Scale.Min;
				double x = xPos - _swallow.Location.Width;
				_swallow.Location.X = x;
				_swallow.Location.Y = 1.0;
				_swallow.IsVisible = true;
			}
		}
		else if (_line_begin != null && !isNeedFollow)
		{
			_line_begin.IsVisible = false;
			_line_end.IsVisible = false;
			_swallow.IsVisible = false;
		}
		if (isNeedFollow)
		{
			GraphPane graphPane2 = _ZGC.MasterPane.PaneList[0];
			double num = graphPane.XAxis.Scale.Max - graphPane.XAxis.Scale.Min;
			graphPane2.XAxis.Scale.Min = xPos - num / 2.0;
			graphPane2.XAxis.Scale.Max = xPos + num / 2.0;
			graphPane.XAxis.Scale.Min = xPos - num / 2.0;
			graphPane.XAxis.Scale.Max = xPos + num / 2.0;
		}
		_ZGC.Invalidate();
	}
}
