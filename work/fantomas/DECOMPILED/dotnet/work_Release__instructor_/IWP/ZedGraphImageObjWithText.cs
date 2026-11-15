using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Runtime.InteropServices;
using ZedGraph;

namespace IWP;

public class ZedGraphImageObjWithText : ImageObj
{
	[StructLayout(LayoutKind.Sequential, Size = 1)]
	public new struct Default
	{
		public static float SuperSize = 0.85f;

		public static float SuperShift = 0.4f;

		public static Color FillColor = Color.White;

		public static Brush FillBrush = null;

		public static FillType FillType = FillType.Solid;

		public static StringAlignment StringAlignment = StringAlignment.Center;

		public static bool IsDropShadow = false;

		public static bool IsAntiAlias = false;

		public static Color DropShadowColor = Color.Black;

		public static float DropShadowAngle = 45f;

		public static float DropShadowOffset = 0.05f;
	}

	private Color _fontColor;

	private string _family;

	private bool _isBold;

	private bool _isItalic;

	private bool _isUnderline;

	private Fill _fill;

	private Border _border;

	private float _angle;

	private StringAlignment _stringAlignment;

	private float _size;

	private Font _font;

	private bool _isAntiAlias;

	private bool _isDropShadow;

	private Color _dropShadowColor;

	private float _dropShadowAngle;

	private float _dropShadowOffset;

	private Font _superScriptFont;

	private float _scaledSize;

	public Color FontColor
	{
		get
		{
			return _fontColor;
		}
		set
		{
			_fontColor = value;
		}
	}

	public string Family
	{
		get
		{
			return _family;
		}
		set
		{
			if (value != _family)
			{
				_family = value;
				Remake(_scaledSize / _size, Size, ref _scaledSize, ref _font);
			}
		}
	}

	public bool IsBold
	{
		get
		{
			return _isBold;
		}
		set
		{
			if (value != _isBold)
			{
				_isBold = value;
				Remake(_scaledSize / _size, Size, ref _scaledSize, ref _font);
			}
		}
	}

	public bool IsItalic
	{
		get
		{
			return _isItalic;
		}
		set
		{
			if (value != _isItalic)
			{
				_isItalic = value;
				Remake(_scaledSize / _size, Size, ref _scaledSize, ref _font);
			}
		}
	}

	public bool IsUnderline
	{
		get
		{
			return _isUnderline;
		}
		set
		{
			if (value != _isUnderline)
			{
				_isUnderline = value;
				Remake(_scaledSize / _size, Size, ref _scaledSize, ref _font);
			}
		}
	}

	public float Angle
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

	public StringAlignment StringAlignment
	{
		get
		{
			return _stringAlignment;
		}
		set
		{
			_stringAlignment = value;
		}
	}

	public float Size
	{
		get
		{
			return _size;
		}
		set
		{
			if (value != _size)
			{
				Remake(_scaledSize / _size * value, _size, ref _scaledSize, ref _font);
				_size = value;
			}
		}
	}

	public Border Border
	{
		get
		{
			return _border;
		}
		set
		{
			_border = value;
		}
	}

	public Fill Fill
	{
		get
		{
			return _fill;
		}
		set
		{
			_fill = value;
		}
	}

	public bool IsAntiAlias
	{
		get
		{
			return _isAntiAlias;
		}
		set
		{
			_isAntiAlias = value;
		}
	}

	public bool IsDropShadow
	{
		get
		{
			return _isDropShadow;
		}
		set
		{
			_isDropShadow = value;
		}
	}

	public Color DropShadowColor
	{
		get
		{
			return _dropShadowColor;
		}
		set
		{
			_dropShadowColor = value;
		}
	}

	public float DropShadowAngle
	{
		get
		{
			return _dropShadowAngle;
		}
		set
		{
			_dropShadowAngle = value;
		}
	}

	public float DropShadowOffset
	{
		get
		{
			return _dropShadowOffset;
		}
		set
		{
			_dropShadowOffset = value;
		}
	}

	public ZedGraphImageObjWithText()
	{
	}

	public ZedGraphImageObjWithText(Image image, RectangleF rect)
		: base(image, rect)
	{
	}

	public ZedGraphImageObjWithText(Image image, RectangleF rect, CoordType coordType, AlignH alignH, AlignV alignV)
		: base(image, rect, coordType, alignH, alignV)
	{
		Init("Arial", 12f, Color.Black, isBold: false, isItalic: false, isUnderline: false, Color.White, null, FillType.Solid);
	}

	private void Remake(float scaleFactor, float size, ref float scaledSize, ref Font font)
	{
		float num = size * scaleFactor;
		float num2 = ((font == null) ? 0f : font.Size);
		if (font == null || (double)Math.Abs(num - num2) > 0.1 || font.Name != Family || font.Bold != _isBold || font.Italic != _isItalic || font.Underline != _isUnderline)
		{
			FontStyle fontStyle = FontStyle.Regular;
			fontStyle = (_isBold ? FontStyle.Bold : fontStyle) | (_isItalic ? FontStyle.Italic : fontStyle) | (_isUnderline ? FontStyle.Underline : fontStyle);
			scaledSize = size * scaleFactor;
			font = new Font(_family, scaledSize, fontStyle, GraphicsUnit.World);
		}
	}

	public Font GetFont(float scaleFactor)
	{
		Remake(scaleFactor, Size, ref _scaledSize, ref _font);
		return _font;
	}

	private void Init(string family, float size, Color color, bool isBold, bool isItalic, bool isUnderline, Color fillColor, Brush fillBrush, FillType fillType)
	{
		_fontColor = color;
		_family = family;
		_isBold = isBold;
		_isItalic = isItalic;
		_isUnderline = isUnderline;
		_size = size;
		_angle = 0f;
		_isAntiAlias = Default.IsAntiAlias;
		_stringAlignment = Default.StringAlignment;
		_isDropShadow = Default.IsDropShadow;
		_dropShadowColor = Default.DropShadowColor;
		_dropShadowAngle = Default.DropShadowAngle;
		_dropShadowOffset = Default.DropShadowOffset;
		_fill = new Fill(fillColor, fillBrush, fillType);
		_border = new Border(isVisible: true, Color.Black, 1f);
		_scaledSize = -1f;
		Remake(1f, _size, ref _scaledSize, ref _font);
	}

	private Matrix SetupMatrix(Matrix matrix, float x, float y, SizeF sizeF, AlignH alignH, AlignV alignV, float angle)
	{
		matrix.Translate(x, y, MatrixOrder.Prepend);
		if (_angle != 0f)
		{
			matrix.Rotate(0f - angle, MatrixOrder.Prepend);
		}
		matrix.Translate(alignH switch
		{
			AlignH.Left => sizeF.Width / 2f, 
			AlignH.Right => (0f - sizeF.Width) / 2f, 
			_ => 0f, 
		}, alignV switch
		{
			AlignV.Center => (0f - sizeF.Height) / 2f, 
			AlignV.Bottom => 0f - sizeF.Height, 
			_ => 0f, 
		}, MatrixOrder.Prepend);
		return matrix;
	}

	public override void Draw(Graphics g, PaneBase pane, float scaleFactor)
	{
		if (Image != null)
		{
			RectangleF rectangleF = _location.TransformRect(pane);
			SizeF sizeF = g.MeasureString("12345", new Font("Arial", 8f, FontStyle.Regular));
			RectangleF rectangleF2 = new RectangleF(rectangleF.X, rectangleF.Y, rectangleF.Width + sizeF.Width, rectangleF.Height + sizeF.Height);
			sizeF.Height = rectangleF2.Height;
			sizeF.Width = rectangleF2.Width;
			if (IsScaled)
			{
				g.DrawImage(Image, rectangleF2);
			}
			else
			{
				Region clip = g.Clip;
				g.SetClip(rectangleF2);
				g.DrawImageUnscaled(Image, Rectangle.Round(rectangleF2));
				g.SetClip(clip, CombineMode.Replace);
			}
			SmoothingMode smoothingMode = g.SmoothingMode;
			Matrix transform = g.Transform;
			g.Transform = SetupMatrix(g.Transform, 0f, 0f, sizeF, AlignH.Center, AlignV.Center, _angle);
			RectangleF rectangleF3 = new RectangleF((0f - sizeF.Width) / 2f, 0f, sizeF.Width, sizeF.Height);
			_fill.Draw(g, rectangleF3);
			_border.Draw(g, pane, scaleFactor, rectangleF3);
			StringFormat stringFormat = new StringFormat();
			stringFormat.Alignment = _stringAlignment;
			using (SolidBrush brush = new SolidBrush(_fontColor))
			{
				g.DrawString("12345", _font, brush, rectangleF3, stringFormat);
			}
			g.Transform = transform;
			g.SmoothingMode = smoothingMode;
		}
	}
}
