using System.CodeDom.Compiler;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.Globalization;
using System.Resources;
using System.Runtime.CompilerServices;

namespace IWP.Properties;

[GeneratedCode("System.Resources.Tools.StronglyTypedResourceBuilder", "16.0.0.0")]
[DebuggerNonUserCode]
[CompilerGenerated]
internal class Resources
{
	private static ResourceManager resourceMan;

	private static CultureInfo resourceCulture;

	[EditorBrowsable(EditorBrowsableState.Advanced)]
	internal static ResourceManager ResourceManager
	{
		get
		{
			if (resourceMan == null)
			{
				ResourceManager resourceManager = new ResourceManager("IWP.Properties.Resources", typeof(Resources).Assembly);
				resourceMan = resourceManager;
			}
			return resourceMan;
		}
	}

	[EditorBrowsable(EditorBrowsableState.Advanced)]
	internal static CultureInfo Culture
	{
		get
		{
			return resourceCulture;
		}
		set
		{
			resourceCulture = value;
		}
	}

	internal static Bitmap Exit_on
	{
		get
		{
			object obj = ResourceManager.GetObject("Exit_on", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap ExitButton
	{
		get
		{
			object obj = ResourceManager.GetObject("ExitButton", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap pause
	{
		get
		{
			object obj = ResourceManager.GetObject("pause", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap pause_on
	{
		get
		{
			object obj = ResourceManager.GetObject("pause_on", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap play1
	{
		get
		{
			object obj = ResourceManager.GetObject("play1", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap play1_on
	{
		get
		{
			object obj = ResourceManager.GetObject("play1_on", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap PultBack
	{
		get
		{
			object obj = ResourceManager.GetObject("PultBack", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap Save_off
	{
		get
		{
			object obj = ResourceManager.GetObject("Save_off", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap Save_on
	{
		get
		{
			object obj = ResourceManager.GetObject("Save_on", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap SearchTrain
	{
		get
		{
			object obj = ResourceManager.GetObject("SearchTrain", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap SearchTrain_on
	{
		get
		{
			object obj = ResourceManager.GetObject("SearchTrain_on", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap swallow
	{
		get
		{
			object obj = ResourceManager.GetObject("swallow", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap swallow_lite
	{
		get
		{
			object obj = ResourceManager.GetObject("swallow_lite", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal Resources()
	{
	}
}
