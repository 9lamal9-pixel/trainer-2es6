using System;
using System.Text;

namespace TcpAsyncNetLib;

public class NetworkWriter
{
	private const int k_MaxStringLength = 32768;

	private NetBuffer m_Buffer;

	private static Encoding s_Encoding;

	private static byte[] s_StringWriteBuffer;

	private static UIntFloat s_FloatConverter;

	public short Position => (short)m_Buffer.Position;

	public NetworkWriter()
	{
		m_Buffer = new NetBuffer();
		if (s_Encoding == null)
		{
			s_Encoding = new UTF8Encoding();
			s_StringWriteBuffer = new byte[32768];
		}
	}

	public NetworkWriter(byte[] buffer)
	{
		m_Buffer = new NetBuffer(buffer);
		if (s_Encoding == null)
		{
			s_Encoding = new UTF8Encoding();
			s_StringWriteBuffer = new byte[32768];
		}
	}

	public byte[] ToArray()
	{
		byte[] array = new byte[m_Buffer.AsArraySegment().Count];
		Array.Copy(m_Buffer.AsArraySegment().Array, array, m_Buffer.AsArraySegment().Count);
		return array;
	}

	public byte[] AsArray()
	{
		return AsArraySegment().Array;
	}

	internal ArraySegment<byte> AsArraySegment()
	{
		return m_Buffer.AsArraySegment();
	}

	public void WritePackedUInt32(uint value)
	{
		if (value <= 240)
		{
			Write((byte)value);
		}
		else if (value <= 2287)
		{
			Write((byte)((value - 240) / 256 + 241));
			Write((byte)((value - 240) % 256));
		}
		else if (value <= 67823)
		{
			Write((byte)249);
			Write((byte)((value - 2288) / 256));
			Write((byte)((value - 2288) % 256));
		}
		else if (value <= 16777215)
		{
			Write((byte)250);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
		}
		else
		{
			Write((byte)251);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
			Write((byte)((value >> 24) & 0xFF));
		}
	}

	public void WritePackedUInt64(ulong value)
	{
		if (value <= 240)
		{
			Write((byte)value);
		}
		else if (value <= 2287)
		{
			Write((byte)((value - 240) / 256 + 241));
			Write((byte)((value - 240) % 256));
		}
		else if (value <= 67823)
		{
			Write((byte)249);
			Write((byte)((value - 2288) / 256));
			Write((byte)((value - 2288) % 256));
		}
		else if (value <= 16777215)
		{
			Write((byte)250);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
		}
		else if (value <= uint.MaxValue)
		{
			Write((byte)251);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
			Write((byte)((value >> 24) & 0xFF));
		}
		else if (value <= 1099511627775L)
		{
			Write((byte)252);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
			Write((byte)((value >> 24) & 0xFF));
			Write((byte)((value >> 32) & 0xFF));
		}
		else if (value <= 281474976710655L)
		{
			Write((byte)253);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
			Write((byte)((value >> 24) & 0xFF));
			Write((byte)((value >> 32) & 0xFF));
			Write((byte)((value >> 40) & 0xFF));
		}
		else if (value <= 72057594037927935L)
		{
			Write((byte)254);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
			Write((byte)((value >> 24) & 0xFF));
			Write((byte)((value >> 32) & 0xFF));
			Write((byte)((value >> 40) & 0xFF));
			Write((byte)((value >> 48) & 0xFF));
		}
		else
		{
			Write(byte.MaxValue);
			Write((byte)(value & 0xFF));
			Write((byte)((value >> 8) & 0xFF));
			Write((byte)((value >> 16) & 0xFF));
			Write((byte)((value >> 24) & 0xFF));
			Write((byte)((value >> 32) & 0xFF));
			Write((byte)((value >> 40) & 0xFF));
			Write((byte)((value >> 48) & 0xFF));
			Write((byte)((value >> 56) & 0xFF));
		}
	}

	public void Write(char value)
	{
		m_Buffer.WriteByte((byte)value);
	}

	public void Write(byte value)
	{
		m_Buffer.WriteByte(value);
	}

	public void Write(sbyte value)
	{
		m_Buffer.WriteByte((byte)value);
	}

	public void Write(short value)
	{
		m_Buffer.WriteByte2((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF));
	}

	public void Write(ushort value)
	{
		m_Buffer.WriteByte2((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF));
	}

	public void Write(int value)
	{
		m_Buffer.WriteByte4((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF), (byte)((value >> 16) & 0xFF), (byte)((value >> 24) & 0xFF));
	}

	public void Write(uint value)
	{
		m_Buffer.WriteByte4((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF), (byte)((value >> 16) & 0xFF), (byte)((value >> 24) & 0xFF));
	}

	public void Write(long value)
	{
		m_Buffer.WriteByte8((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF), (byte)((value >> 16) & 0xFF), (byte)((value >> 24) & 0xFF), (byte)((value >> 32) & 0xFF), (byte)((value >> 40) & 0xFF), (byte)((value >> 48) & 0xFF), (byte)((value >> 56) & 0xFF));
	}

	public void Write(ulong value)
	{
		m_Buffer.WriteByte8((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF), (byte)((value >> 16) & 0xFF), (byte)((value >> 24) & 0xFF), (byte)((value >> 32) & 0xFF), (byte)((value >> 40) & 0xFF), (byte)((value >> 48) & 0xFF), (byte)((value >> 56) & 0xFF));
	}

	public void Write(float value)
	{
		s_FloatConverter.floatValue = value;
		Write(s_FloatConverter.intValue);
	}

	public void Write(double value)
	{
		s_FloatConverter.doubleValue = value;
		Write(s_FloatConverter.longValue);
	}

	public void Write(string value)
	{
		if (value == null)
		{
			m_Buffer.WriteByte2(0, 0);
			return;
		}
		int byteCount = s_Encoding.GetByteCount(value);
		if (byteCount >= 32768)
		{
			throw new IndexOutOfRangeException("Serialize(string) too long: " + value.Length);
		}
		Write((ushort)byteCount);
		int bytes = s_Encoding.GetBytes(value, 0, value.Length, s_StringWriteBuffer, 0);
		m_Buffer.WriteBytes(s_StringWriteBuffer, (ushort)bytes);
	}

	public void Write(bool value)
	{
		if (value)
		{
			m_Buffer.WriteByte(1);
		}
		else
		{
			m_Buffer.WriteByte(0);
		}
	}

	public void Write(byte[] buffer, int count)
	{
		m_Buffer.WriteBytes(buffer, (ushort)count);
	}

	public void Write(byte[] buffer, int offset, int count)
	{
		m_Buffer.WriteBytesAtOffset(buffer, (ushort)offset, (ushort)count);
	}

	public void WriteBytesAndSize(byte[] buffer, int count)
	{
		if (buffer == null || count == 0)
		{
			Write((ushort)0);
			return;
		}
		Write((ushort)count);
		m_Buffer.WriteBytes(buffer, (ushort)count);
	}

	public void WriteBytesFull(byte[] buffer)
	{
		if (buffer == null)
		{
			Write((ushort)0);
			return;
		}
		Write((ushort)buffer.Length);
		m_Buffer.WriteBytes(buffer, (ushort)buffer.Length);
	}

	public void Write(NetMessageBase msg)
	{
		msg.Serialize(this);
	}

	public void SeekZero()
	{
		m_Buffer.SeekZero();
	}

	public void StartMessage(short msgType)
	{
		SeekZero();
		m_Buffer.WriteByte2(0, 0);
		Write(msgType);
	}

	public void FinishMessage()
	{
		m_Buffer.FinishMessage();
	}
}
