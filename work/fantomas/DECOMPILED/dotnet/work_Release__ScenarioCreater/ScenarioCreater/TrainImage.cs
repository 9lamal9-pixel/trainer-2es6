using System.Drawing;
using System.Drawing.Drawing2D;
using ZedGraph;

namespace ScenarioCreater;

internal class TrainImage : ImageObj
{
	private int _heightInWindowCoord;

	private int _widthInWindowCoord;

	private float _angle;

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

	public float angle
	{
		get
		{
			return _angle;
		}
		set
		{
			_angle = value;
		}
	}

	public TrainImage()
	{
	}

	public TrainImage(Image image, RectangleF rect, int WidthInWindowCoord, int HeightInWindowCoord)
		: base(image, rect)
	{
		_widthInWindowCoord = WidthInWindowCoord;
		_heightInWindowCoord = HeightInWindowCoord;
	}

	public TrainImage(Image image, RectangleF rect, CoordType coordType, AlignH alignH, AlignV alignV, int WidthInWindowCoord, int HeightInWindowCoord)
		: base(image, rect, coordType, alignH, alignV)
	{
		_widthInWindowCoord = WidthInWindowCoord;
		_heightInWindowCoord = HeightInWindowCoord;
	}

	public static Bitmap RotateImage(Bitmap b, float angle)
	{
		Bitmap bitmap = new Bitmap(b.Width, b.Height);
		using Graphics graphics = Graphics.FromImage(bitmap);
		graphics.TranslateTransform((float)b.Width / 2f, (float)b.Height / 2f);
		graphics.RotateTransform(angle);
		graphics.TranslateTransform((0f - (float)b.Width) / 2f, (0f - (float)b.Height) / 2f);
		graphics.DrawImage(b, new Point(0, 0));
		return bitmap;
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
			g.DrawImage(RotateImage((Bitmap)Image, _angle), rect.X, rect.Y, _widthInWindowCoord, _heightInWindowCoord);
		}
	}
}
