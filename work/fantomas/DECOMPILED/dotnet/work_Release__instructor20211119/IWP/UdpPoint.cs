using System;
using System.Text;

namespace IWP;

internal class UdpPoint<T>
{
	private static Encoding s_Encoding = new UnicodeEncoding(bigEndian: true, byteOrderMark: false);

	public T Name;

	public int Size;

	public byte[] Value;

	public bool IsUpdated;

	public Func<float, short> ConvertBeforeDataSend;

	public void Write(short value)
	{
		Value[1] = (byte)(value & 0xFF);
		Value[0] = (byte)((value >> 8) & 0xFF);
	}

	public void Write(int value)
	{
		Value[3] = (byte)(value & 0xFF);
		Value[2] = (byte)((value >> 8) & 0xFF);
		Value[1] = (byte)((value >> 16) & 0xFF);
		Value[0] = (byte)((value >> 24) & 0xFF);
	}

	public void Write(long value)
	{
		Value[7] = (byte)(value & 0xFF);
		Value[6] = (byte)((value >> 8) & 0xFF);
		Value[5] = (byte)((value >> 16) & 0xFF);
		Value[4] = (byte)((value >> 24) & 0xFF);
		Value[3] = (byte)((value >> 32) & 0xFF);
		Value[2] = (byte)((value >> 40) & 0xFF);
		Value[1] = (byte)((value >> 48) & 0xFF);
		Value[0] = (byte)((value >> 56) & 0xFF);
	}

	public void Write(float value)
	{
		byte[] bytes = BitConverter.GetBytes(value);
		if (BitConverter.IsLittleEndian)
		{
			Array.Reverse(bytes);
		}
		for (int i = 0; i < Value.Length; i++)
		{
			Value[i] = bytes[i];
		}
	}

	public void Write(string value)
	{
		Value = new byte[Value.Length];
		int byteCount = s_Encoding.GetByteCount(value);
		int charCount = ((byteCount <= Value.Length) ? value.Length : (Value.Length / 2));
		s_Encoding.GetBytes(value, 0, charCount, Value, 0);
	}
}
