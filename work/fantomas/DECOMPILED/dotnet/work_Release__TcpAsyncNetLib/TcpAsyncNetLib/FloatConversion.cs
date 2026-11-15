namespace TcpAsyncNetLib;

internal class FloatConversion
{
	public static float ToSingle(uint value)
	{
		UIntFloat uIntFloat = new UIntFloat
		{
			intValue = value
		};
		return uIntFloat.floatValue;
	}

	public static double ToDouble(ulong value)
	{
		UIntFloat uIntFloat = new UIntFloat
		{
			longValue = value
		};
		return uIntFloat.doubleValue;
	}
}
