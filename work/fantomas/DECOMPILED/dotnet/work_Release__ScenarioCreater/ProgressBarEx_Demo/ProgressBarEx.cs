using System;
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace ProgressBarEx_Demo;

public class ProgressBarEx : Control
{
	public enum GradiantArea
	{
		None,
		Top,
		Center,
		Bottom
	}

	public enum ImageLayoutType
	{
		None,
		Center,
		Stretch
	}

	public enum ProgressDir
	{
		Horizontal,
		Vertical
	}

	private Blend bBlend = new Blend();

	private int _Minimum;

	private int _Maximum = 100;

	private int _Value;

	private bool _Border = true;

	private Pen _BorderPen;

	private Color _BorderColor = Color.Black;

	private GradiantArea _GradiantPosition;

	private Color _GradiantColor = Color.White;

	private Color _BackColor = Color.DarkGray;

	private Color _ProgressColor = Color.Lime;

	private SolidBrush _ForeColorBrush;

	private bool _ShowPercentage;

	private bool _ShowText;

	private ImageLayoutType _ImageLayout;

	private Bitmap _Image;

	private bool _RoundedCorners = true;

	private ProgressDir _ProgressDirection;

	[Category("Appearance")]
	[Description("The foreground color of the ProgressBars text.")]
	[Browsable(true)]
	public override Color ForeColor
	{
		get
		{
			return base.ForeColor;
		}
		set
		{
			if (value == Color.Transparent)
			{
				value = _ForeColorBrush.Color;
			}
			base.ForeColor = value;
			_ForeColorBrush.Color = value;
		}
	}

	[Category("Appearance")]
	[Description("The background color of the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(typeof(Color), "DarkGray")]
	public Color BackgroundColor
	{
		get
		{
			return _BackColor;
		}
		set
		{
			if (value == Color.Transparent)
			{
				value = _BackColor;
			}
			_BackColor = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("The progress color of the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(typeof(Color), "Lime")]
	public Color ProgressColor
	{
		get
		{
			return _ProgressColor;
		}
		set
		{
			if (value == Color.Transparent)
			{
				value = _ProgressColor;
			}
			_ProgressColor = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("The gradiant highlight color of the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(typeof(Color), "White")]
	public Color GradiantColor
	{
		get
		{
			return _GradiantColor;
		}
		set
		{
			_GradiantColor = value;
			Refresh();
		}
	}

	[Category("Behavior")]
	[Description("The minimum value of the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(0)]
	public int Minimum
	{
		get
		{
			return _Minimum;
		}
		set
		{
			if (value > _Maximum)
			{
				value = _Maximum - 1;
			}
			_Minimum = value;
			Refresh();
		}
	}

	[Category("Behavior")]
	[Description("The maximum value of the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(100)]
	public int Maximum
	{
		get
		{
			return _Maximum;
		}
		set
		{
			if (value <= _Minimum)
			{
				value = _Minimum + 1;
			}
			_Maximum = value;
			Refresh();
		}
	}

	[Category("Behavior")]
	[Description("The current value of the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(0)]
	public int Value
	{
		get
		{
			return _Value;
		}
		set
		{
			if (value < _Minimum)
			{
				value = _Minimum;
			}
			if (value > _Maximum)
			{
				value = _Maximum;
			}
			_Value = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("Draw a border around the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(true)]
	public bool Border
	{
		get
		{
			return _Border;
		}
		set
		{
			_Border = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("The color of the border around the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(typeof(Color), "Black")]
	public Color BorderColor
	{
		get
		{
			return _BorderColor;
		}
		set
		{
			if (value == Color.Transparent)
			{
				value = _BorderColor;
			}
			_BorderColor = value;
			_BorderPen.Color = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("Shows the progress percentge as text in the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(false)]
	public bool ShowPercentage
	{
		get
		{
			return _ShowPercentage;
		}
		set
		{
			_ShowPercentage = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("Shows the text of the Text property in the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(false)]
	public bool ShowText
	{
		get
		{
			return _ShowText;
		}
		set
		{
			_ShowText = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("Determins the position of the gradiant shine in the ProgressBar.")]
	[Browsable(true)]
	[DefaultValue(typeof(GradiantArea), "Top")]
	public GradiantArea GradiantPosition
	{
		get
		{
			return _GradiantPosition;
		}
		set
		{
			_GradiantPosition = value;
			switch (value)
			{
			case GradiantArea.None:
				bBlend.Factors = new float[6];
				break;
			case GradiantArea.Top:
				bBlend.Factors = new float[6] { 0.8f, 0.7f, 0.6f, 0.4f, 0f, 0f };
				break;
			case GradiantArea.Center:
				bBlend.Factors = new float[6] { 0f, 0.4f, 0.6f, 0.6f, 0.4f, 0f };
				break;
			default:
				bBlend.Factors = new float[6] { 0f, 0f, 0.4f, 0.6f, 0.7f, 0.8f };
				break;
			}
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("An image to display on the ProgressBarEx.")]
	[Browsable(true)]
	public Bitmap Image
	{
		get
		{
			return _Image;
		}
		set
		{
			_Image = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("Determins how the image is displayed in the ProgressBarEx.")]
	[Browsable(true)]
	[DefaultValue(typeof(ImageLayoutType), "None")]
	public ImageLayoutType ImageLayout
	{
		get
		{
			return _ImageLayout;
		}
		set
		{
			_ImageLayout = value;
			if (_Image != null)
			{
				Refresh();
			}
		}
	}

	[Category("Appearance")]
	[Description("True to draw corners rounded. False to draw square corners.")]
	[Browsable(true)]
	[DefaultValue(true)]
	public bool RoundedCorners
	{
		get
		{
			return _RoundedCorners;
		}
		set
		{
			_RoundedCorners = value;
			Refresh();
		}
	}

	[Category("Appearance")]
	[Description("Determins the direction of progress displayed in the ProgressBarEx.")]
	[Browsable(true)]
	[DefaultValue(typeof(ProgressDir), "Horizontal")]
	public ProgressDir ProgressDirection
	{
		get
		{
			return _ProgressDirection;
		}
		set
		{
			_ProgressDirection = value;
			Refresh();
		}
	}

	[Browsable(false)]
	[EditorBrowsable(EditorBrowsableState.Never)]
	public override Color BackColor
	{
		get
		{
			return base.BackColor;
		}
		set
		{
			base.BackColor = Color.Transparent;
		}
	}

	[Browsable(false)]
	[EditorBrowsable(EditorBrowsableState.Never)]
	[DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
	[Obsolete("BackgroundImageLayout is not implemented.", true)]
	public new ImageLayout BackgroundImageLayout
	{
		get
		{
			return base.BackgroundImageLayout;
		}
		set
		{
			throw new NotImplementedException("BackgroundImageLayout is not implemented.");
		}
	}

	[Browsable(false)]
	[EditorBrowsable(EditorBrowsableState.Never)]
	[DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
	[Obsolete("BackgroundImage is not implemented.", true)]
	public new Image BackgroundImage
	{
		get
		{
			return null;
		}
		set
		{
			throw new NotImplementedException("BackgroundImage is not implemented.");
		}
	}

	[Browsable(false)]
	[EditorBrowsable(EditorBrowsableState.Never)]
	[DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
	[Obsolete("TabStop is not implemented.", true)]
	public new bool TabStop
	{
		get
		{
			return false;
		}
		set
		{
			throw new NotImplementedException("TabStop is not implemented.");
		}
	}

	[Browsable(false)]
	[EditorBrowsable(EditorBrowsableState.Never)]
	[DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
	[Obsolete("TabIndex is not implemented.", true)]
	public new int TabIndex
	{
		get
		{
			return base.TabIndex;
		}
		set
		{
			throw new NotImplementedException("TabIndex is not implemented.");
		}
	}

	public ProgressBarEx()
	{
		SetStyle(ControlStyles.SupportsTransparentBackColor | ControlStyles.OptimizedDoubleBuffer, value: true);
		base.TabStop = false;
		Size = new Size(200, 23);
		bBlend.Positions = new float[6] { 0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f };
		GradiantPosition = GradiantArea.Top;
		base.BackColor = Color.Transparent;
		_ForeColorBrush = new SolidBrush(base.ForeColor);
		_BorderPen = new Pen(Color.Black);
	}

	protected override void OnPaint(PaintEventArgs e)
	{
		e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
		Point point = new Point(0, 0);
		Point point2 = new Point(0, Height);
		if (_ProgressDirection == ProgressDir.Vertical)
		{
			point2 = new Point(Width, 0);
		}
		using GraphicsPath graphicsPath = new GraphicsPath();
		Rectangle rect = new Rectangle(0, 0, Width, Height);
		int num = Convert.ToInt32((double)rect.Height / 2.5);
		if (rect.Width < rect.Height)
		{
			num = Convert.ToInt32((double)rect.Width / 2.5);
		}
		using (LinearGradientBrush linearGradientBrush = new LinearGradientBrush(point, point2, _BackColor, _GradiantColor))
		{
			linearGradientBrush.Blend = bBlend;
			if (_RoundedCorners)
			{
				graphicsPath.AddArc(rect.X, rect.Y, num, num, 180f, 90f);
				graphicsPath.AddArc(rect.Right - num, rect.Y, num, num, 270f, 90f);
				graphicsPath.AddArc(rect.Right - num, rect.Bottom - num, num, num, 0f, 90f);
				graphicsPath.AddArc(rect.X, rect.Bottom - num, num, num, 90f, 90f);
				graphicsPath.CloseFigure();
				e.Graphics.FillPath(linearGradientBrush, graphicsPath);
			}
			else
			{
				e.Graphics.FillRectangle(linearGradientBrush, rect);
			}
		}
		if (_Value > _Minimum)
		{
			int num2 = Convert.ToInt32((double)Width / (double)(_Maximum - _Minimum) * (double)_Value);
			if (_ProgressDirection == ProgressDir.Vertical)
			{
				num2 = Convert.ToInt32((double)Height / (double)(_Maximum - _Minimum) * (double)_Value);
				rect.Y = rect.Height - num2;
				rect.Height = num2;
			}
			else
			{
				rect.Width = num2;
			}
			using LinearGradientBrush linearGradientBrush2 = new LinearGradientBrush(point, point2, _ProgressColor, _GradiantColor);
			linearGradientBrush2.Blend = bBlend;
			if (_RoundedCorners)
			{
				if (_ProgressDirection == ProgressDir.Horizontal)
				{
					rect.Height--;
				}
				else
				{
					rect.Width--;
				}
				using GraphicsPath graphicsPath2 = new GraphicsPath();
				graphicsPath2.AddArc(rect.X, rect.Y, num, num, 180f, 90f);
				graphicsPath2.AddArc(rect.Right - num, rect.Y, num, num, 270f, 90f);
				graphicsPath2.AddArc(rect.Right - num, rect.Bottom - num, num, num, 0f, 90f);
				graphicsPath2.AddArc(rect.X, rect.Bottom - num, num, num, 90f, 90f);
				graphicsPath2.CloseFigure();
				using GraphicsPath graphicsPath3 = new GraphicsPath();
				using (Region region = new Region(graphicsPath))
				{
					region.Intersect(graphicsPath2);
					graphicsPath3.AddRectangles(region.GetRegionScans(new Matrix()));
				}
				e.Graphics.FillPath(linearGradientBrush2, graphicsPath3);
			}
			else
			{
				e.Graphics.FillRectangle(linearGradientBrush2, rect);
			}
		}
		if (_Image != null)
		{
			if (_ImageLayout == ImageLayoutType.Stretch)
			{
				e.Graphics.DrawImage(_Image, 0, 0, Width, Height);
			}
			else if (_ImageLayout == ImageLayoutType.None)
			{
				e.Graphics.DrawImage(_Image, 0, 0);
			}
			else
			{
				int num3 = Convert.ToInt32(Width / 2 - _Image.Width / 2);
				int num4 = Convert.ToInt32(Height / 2 - _Image.Height / 2);
				e.Graphics.DrawImage(_Image, num3, num4);
			}
		}
		if (_ShowPercentage | _ShowText)
		{
			string text = "";
			if (_ShowText)
			{
				text = Text;
			}
			if (_ShowPercentage)
			{
				text = text + Convert.ToString(Convert.ToInt32(100.0 / (double)(_Maximum - _Minimum) * (double)_Value)) + "%";
			}
			using StringFormat format = new StringFormat
			{
				Alignment = StringAlignment.Center,
				LineAlignment = StringAlignment.Center
			};
			e.Graphics.DrawString(text, Font, _ForeColorBrush, new Rectangle(0, 0, Width, Height), format);
		}
		if (_Border)
		{
			rect = new Rectangle(0, 0, Width - 1, Height - 1);
			if (_RoundedCorners)
			{
				graphicsPath.Reset();
				graphicsPath.AddArc(rect.X, rect.Y, num, num, 180f, 90f);
				graphicsPath.AddArc(rect.Right - num, rect.Y, num, num, 270f, 90f);
				graphicsPath.AddArc(rect.Right - num, rect.Bottom - num, num, num, 0f, 90f);
				graphicsPath.AddArc(rect.X, rect.Bottom - num, num, num, 90f, 90f);
				graphicsPath.CloseFigure();
				e.Graphics.DrawPath(_BorderPen, graphicsPath);
			}
			else
			{
				e.Graphics.DrawRectangle(_BorderPen, rect);
			}
		}
	}

	protected override void OnTextChanged(EventArgs e)
	{
		Refresh();
		base.OnTextChanged(e);
	}

	protected override void Dispose(bool disposing)
	{
		_ForeColorBrush.Dispose();
		_BorderPen.Dispose();
		base.Dispose(disposing);
	}
}
