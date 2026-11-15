using System;
using System.Text;

namespace TcpAsyncNetLib;

public class NetworkReader
{
	private NetBuffer m_buf;

	private const int k_MaxStringLength = 32768;

	private const int k_InitialStringBufferSize = 1024;

	private static byte[] s_StringReaderBuffer;

	private static Encoding s_Encoding;

	public uint Position => m_buf.Position;

	public NetworkReader()
	{
		m_buf = new NetBuffer();
		Initialize();
	}

	public NetworkReader(NetworkWriter writer)
	{
		m_buf = new NetBuffer(writer.AsArray());
		Initialize();
	}

	public NetworkReader(byte[] buffer)
	{
		m_buf = new NetBuffer(buffer);
		Initialize();
	}

	private static void Initialize()
	{
		if (s_Encoding == null)
		{
			s_StringReaderBuffer = new byte[1024];
			s_Encoding = new UTF8Encoding();
		}
	}

	public void SeekZero()
	{
		m_buf.SeekZero();
	}

	internal void Replace(byte[] buffer)
	{
		m_buf.Replace(buffer);
	}

	public uint ReadPackedUInt32()
	{
		byte b = ReadByte();
		if (b < 241)
		{
			return b;
		}
		byte b2 = ReadByte();
		if (b >= 241 && b <= 248)
		{
			return (uint)(240 + 256 * (b - 241) + b2);
		}
		byte b3 = ReadByte();
		if (b == 249)
		{
			return (uint)(2288 + 256 * b2 + b3);
		}
		byte b4 = ReadByte();
		if (b == 250)
		{
			return (uint)(b2 + (b3 << 8) + (b4 << 16));
		}
		byte b5 = ReadByte();
		if (b >= 251)
		{
			return (uint)(b2 + (b3 << 8) + (b4 << 16) + (b5 << 24));
		}
		throw new IndexOutOfRangeException("ReadPackedUInt32() failure: " + b);
	}

	public ulong ReadPackedUInt64()
	{
		byte b = ReadByte();
		if (b < 241)
		{
			return b;
		}
		byte b2 = ReadByte();
		if (b >= 241 && b <= 248)
		{
			return (ulong)(240 + 256 * ((long)b - 241L) + b2);
		}
		byte b3 = ReadByte();
		if (b == 249)
		{
			return (ulong)(2288 + 256L * (long)b2 + b3);
		}
		byte b4 = ReadByte();
		if (b == 250)
		{
			return b2 + ((ulong)b3 << 8) + ((ulong)b4 << 16);
		}
		byte b5 = ReadByte();
		if (b == 251)
		{
			return b2 + ((ulong)b3 << 8) + ((ulong)b4 << 16) + ((ulong)b5 << 24);
		}
		byte b6 = ReadByte();
		if (b == 252)
		{
			return b2 + ((ulong)b3 << 8) + ((ulong)b4 << 16) + ((ulong)b5 << 24) + ((ulong)b6 << 32);
		}
		byte b7 = ReadByte();
		if (b == 253)
		{
			return b2 + ((ulong)b3 << 8) + ((ulong)b4 << 16) + ((ulong)b5 << 24) + ((ulong)b6 << 32) + ((ulong)b7 << 40);
		}
		byte b8 = ReadByte();
		if (b == 254)
		{
			return b2 + ((ulong)b3 << 8) + ((ulong)b4 << 16) + ((ulong)b5 << 24) + ((ulong)b6 << 32) + ((ulong)b7 << 40) + ((ulong)b8 << 48);
		}
		byte b9 = ReadByte();
		if (b == byte.MaxValue)
		{
			return b2 + ((ulong)b3 << 8) + ((ulong)b4 << 16) + ((ulong)b5 << 24) + ((ulong)b6 << 32) + ((ulong)b7 << 40) + ((ulong)b8 << 48) + ((ulong)b9 << 56);
		}
		throw new IndexOutOfRangeException("ReadPackedUInt64() failure: " + b);
	}

	public byte ReadByte()
	{
		return m_buf.ReadByte();
	}

	public sbyte ReadSByte()
	{
		return (sbyte)m_buf.ReadByte();
	}

	public short ReadInt16()
	{
		return (short)(ushort)((ushort)(0 | m_buf.ReadByte()) | (ushort)(m_buf.ReadByte() << 8));
	}

	public ushort ReadUInt16()
	{
		return (ushort)((ushort)(0 | m_buf.ReadByte()) | (ushort)(m_buf.ReadByte() << 8));
	}

	public int ReadInt32()
	{
		return 0 | m_buf.ReadByte() | (m_buf.ReadByte() << 8) | (m_buf.ReadByte() << 16) | (m_buf.ReadByte() << 24);
	}

	public uint ReadUInt32()
	{
		return (uint)(0 | m_buf.ReadByte() | (m_buf.ReadByte() << 8) | (m_buf.ReadByte() << 16) | (m_buf.ReadByte() << 24));
	}

	public long ReadInt64()
	{
		long num = 0L;
		ulong num2 = m_buf.ReadByte();
		long num3 = num | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 8;
		long num4 = num3 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 16;
		long num5 = num4 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 24;
		long num6 = num5 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 32;
		long num7 = num6 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 40;
		long num8 = num7 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 48;
		long num9 = num8 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 56;
		return num9 | (long)num2;
	}

	public ulong ReadUInt64()
	{
		long num = 0L;
		ulong num2 = m_buf.ReadByte();
		long num3 = num | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 8;
		long num4 = num3 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 16;
		long num5 = num4 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 24;
		long num6 = num5 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 32;
		long num7 = num6 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 40;
		long num8 = num7 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 48;
		long num9 = num8 | (long)num2;
		num2 = (ulong)m_buf.ReadByte() << 56;
		return (ulong)num9 | num2;
	}

	public float ReadSingle()
	{
		return FloatConversion.ToSingle(ReadUInt32());
	}

	public double ReadDouble()
	{
		return FloatConversion.ToDouble(ReadUInt64());
	}

	public string ReadString()
	{
		ushort num = ReadUInt16();
		if (num == 0)
		{
			return "";
		}
		if (num >= 32768)
		{
			throw new IndexOutOfRangeException("ReadString() too long: " + num);
		}
		while (num > s_StringReaderBuffer.Length)
		{
			s_StringReaderBuffer = new byte[s_StringReaderBuffer.Length * 2];
		}
		m_buf.ReadBytes(s_StringReaderBuffer, num);
		return new string(s_Encoding.GetChars(s_StringReaderBuffer, 0, num));
	}

	public char ReadChar()
	{
		return (char)m_buf.ReadByte();
	}

	public bool ReadBoolean()
	{
		return m_buf.ReadByte() == 1;
	}

	public byte[] ReadBytes(int count)
	{
		if (count < 0)
		{
			throw new IndexOutOfRangeException("NetworkReader ReadBytes " + count);
		}
		byte[] array = new byte[count];
		m_buf.ReadBytes(array, (uint)count);
		return array;
	}

	public byte[] ReadBytesAndSize()
	{
		ushort num = ReadUInt16();
		if (num == 0)
		{
			return null;
		}
		return ReadBytes(num);
	}

	public override string ToString()
	{
		return m_buf.ToString();
	}

	public TMsg ReadMessage<TMsg>() where TMsg : NetMessageBase, new()
	{
		TMsg val = new TMsg();
		val.Deserialize(this);
		return val;
	}
}
