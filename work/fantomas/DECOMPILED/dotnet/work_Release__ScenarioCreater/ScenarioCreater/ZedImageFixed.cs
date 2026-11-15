using System.Drawing;
using System.Drawing.Drawing2D;
using ZedGraph;

namespace ScenarioCreater;

internal class ZedImageFixed : ImageObj
{
	private int _heightInWindowCoord;

	private int _widthInWindowCoord;

	public int widthInWindowCoord
	{
		get
		{
			return _widthInWindowCoord;
		}
		set
		{
			_widthInWindowCoord = value;
		}
	}

	public int heightInWindowCoord
	{
		get
		{
			return _heightInWindowCoord;
		}
		set
		{
			_heightInWindowCoord = value;
		}
	}

	public ZedImageFixed()
	{
	}

	public ZedImageFixed(Image image, RectangleF rect, int WidthInWindowCoord, int HeightInWindowCoord)
		: base(image, rect)
	{
		_widthInWindowCoord = WidthInWindowCoord;
		_heightInWindowCoord = HeightInWindowCoord;
	}

	public ZedImageFixed(Image image, RectangleF rect, CoordType coordType, AlignH alignH, AlignV alignV, int WidthInWindowCoord, int HeightInWindowCoord)
		: base(image, rect, coordType, alignH, alignV)
	{
		_widthInWindowCoord = WidthInWindowCoord;
		_heightInWindowCoord = HeightInWindowCoord;
	}

	public override void Draw(Graphics g, PaneBase pane, float scaleFactor)
	{
		if (Image != null)
		{
			RectangleF rect = _location.TransformRect(pane);
			if (IsScaled)
			{
				g.DrawImage(Image, rect);
				return;
			}
			_ = g.Clip;
			g.SetClip(new RectangleF(rect.X, rect.Y, _widthInWindowCoord, _heightInWindowCoord), CombineMode.Intersect);
			g.DrawImage(Image, rect.X, rect.Y, _widthInWindowCoord, _heightInWindowCoord);
		}
	}
}
