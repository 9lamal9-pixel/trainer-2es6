using System;
using System.Collections.Generic;

namespace IWP;

internal class UdpWriteController<T>
{
	public List<UdpPoint<T>> Items;

	protected byte[] Buffer;

	protected int BufferLength;

	public UdpWriteController()
	{
		Items = new List<UdpPoint<T>>();
	}

	public byte[] GetCommandString()
	{
		int num = 0;
		for (int i = 0; i < Items.Count; i++)
		{
			for (int j = 0; j < Items[i].Size; j++)
			{
				Buffer[num + j] = Items[i].Value[j];
			}
			num += Items[i].Size;
		}
		return Buffer;
	}

	public UdpPoint<T> InitPoint(T name, int size = 1)
	{
		BufferLength += size;
		UdpPoint<T> udpPoint = new UdpPoint<T>();
		udpPoint.Name = name;
		udpPoint.Size = size;
		udpPoint.Value = new byte[size];
		udpPoint.ConvertBeforeDataSend = null;
		return udpPoint;
	}

	public UdpPoint<T> InitPoint(T name, int size, Func<float, short> listener)
	{
		BufferLength += size;
		UdpPoint<T> udpPoint = new UdpPoint<T>();
		udpPoint.Name = name;
		udpPoint.Size = size;
		udpPoint.Value = new byte[size];
		udpPoint.ConvertBeforeDataSend = listener;
		return udpPoint;
	}
}
