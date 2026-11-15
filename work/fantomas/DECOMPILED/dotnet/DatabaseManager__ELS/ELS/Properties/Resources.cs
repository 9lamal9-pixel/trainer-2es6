using System.CodeDom.Compiler;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.Globalization;
using System.Resources;
using System.Runtime.CompilerServices;

namespace ELS.Properties;

[GeneratedCode("System.Resources.Tools.StronglyTypedResourceBuilder", "4.0.0.0")]
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
				ResourceManager resourceManager = new ResourceManager("ELS.Properties.Resources", typeof(Resources).Assembly);
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

	internal static Bitmap dobavit1
	{
		get
		{
			object obj = ResourceManager.GetObject("dobavit1", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap izmenit1
	{
		get
		{
			object obj = ResourceManager.GetObject("izmenit1", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap sokhranit1
	{
		get
		{
			object obj = ResourceManager.GetObject("sokhranit1", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal static Bitmap udalit1
	{
		get
		{
			object obj = ResourceManager.GetObject("udalit1", resourceCulture);
			return (Bitmap)obj;
		}
	}

	internal Resources()
	{
	}
}
