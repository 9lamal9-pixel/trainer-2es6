using System.CodeDom.Compiler;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.Globalization;
using System.Resources;
using System.Runtime.CompilerServices;

namespace ScenarioCreater.Properties;

[GeneratedCode("System.Resources.Tools.StronglyTypedResourceBuilder", "15.0.0.0")]
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
				resourceMan = new ResourceManager("ScenarioCreater.Properties.Resources", typeof(Resources).Assembly);
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

	internal static Bitmap delete_image => (Bitmap)ResourceManager.GetObject("delete_image", resourceCulture);

	internal static Bitmap Exit_on => (Bitmap)ResourceManager.GetObject("Exit_on", resourceCulture);

	internal static Bitmap ExitButton => (Bitmap)ResourceManager.GetObject("ExitButton", resourceCulture);

	internal static Bitmap Save_off => (Bitmap)ResourceManager.GetObject("Save_off", resourceCulture);

	internal static Bitmap Save_on => (Bitmap)ResourceManager.GetObject("Save_on", resourceCulture);

	internal static Bitmap SearchTrain => (Bitmap)ResourceManager.GetObject("SearchTrain", resourceCulture);

	internal static Bitmap SearchTrain_on => (Bitmap)ResourceManager.GetObject("SearchTrain_on", resourceCulture);

	internal static Bitmap swallow => (Bitmap)ResourceManager.GetObject("swallow", resourceCulture);

	internal static Bitmap swallow_lite => (Bitmap)ResourceManager.GetObject("swallow_lite", resourceCulture);

	internal Resources()
	{
	}
}
