using System;
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace ScenarioCreater;

public class DoubleProgressBar : PictureBox
{
	private int _value;

	private Color _topColor;

	private Color _bottomColor;

	private int _zeroLevel;

	private int _topLevel;

	private int _bottomLevel;

	private int _etLevel;

	public int Value
	{
		get
		{
			return _value;
		}
		set
		{
			if (value > 0)
			{
				_value = ((value <= 100) ? value : 100);
			}
			if (value < 0 && value >= -100)
			{
				_value = value;
			}
			if (value < -100)
			{
				_value = -200;
			}
		}
	}

	public Color TopColor
	{
		get
		{
			return _topColor;
		}
		set
		{
			_topColor = value;
		}
	}

	public Color BottomColor
	{
		get
		{
			return _bottomColor;
		}
		set
		{
			_bottomColor = value;
		}
	}

	public int ZeroLevel
	{
		get
		{
			return _zeroLevel;
		}
		set
		{
			_zeroLevel = value;
		}
	}

	public int TopLevel
	{
		get
		{
			return _topLevel;
		}
		set
		{
			_topLevel = value;
		}
	}

	public int BottomLevel
	{
		get
		{
			return _bottomLevel;
		}
		set
		{
			_bottomLevel = value;
		}
	}

	public int EtLevel
	{
		get
		{
			return _etLevel;
		}
		set
		{
			_etLevel = value;
		}
	}

	protected override void OnPaint(PaintEventArgs e)
	{
		e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
		int x = Width;
		_ = Height;
		if (_value > 0)
		{
			int num = Convert.ToInt32((float)(_zeroLevel - _topLevel) / 100f * (float)_value);
			Rectangle rect = new Rectangle(0, _zeroLevel - num, x, num);
			SolidBrush brush = new SolidBrush(_topColor);
			e.Graphics.FillRectangle(brush, rect);
		}
		if (_value < 0 && _value != -200)
		{
			Rectangle rect2 = new Rectangle(0, _zeroLevel, x, Convert.ToInt32((float)(_bottomLevel - _zeroLevel) / 100f * (float)Math.Abs(_value)));
			SolidBrush brush2 = new SolidBrush(_bottomColor);
			e.Graphics.FillRectangle(brush2, rect2);
		}
		if (_value == -200)
		{
			Rectangle rect3 = new Rectangle(0, _zeroLevel, x, _etLevel - _zeroLevel);
			SolidBrush brush3 = new SolidBrush(_bottomColor);
			e.Graphics.FillRectangle(brush3, rect3);
		}
		Pen pen = new Pen(Color.Black);
		pen.Width = 2f;
		e.Graphics.DrawLine(pen, 0, _zeroLevel, x, _zeroLevel);
	}

	private void InitializeComponent()
	{
		((System.ComponentModel.ISupportInitialize)this).BeginInit();
		base.SuspendLayout();
		((System.ComponentModel.ISupportInitialize)this).EndInit();
		base.ResumeLayout(false);
	}
}
