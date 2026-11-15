using System.Collections.Generic;

namespace IWP;

internal class UdpReadController<T>
{
	public List<UdpPoint<T>> Items;

	public UdpReadController()
	{
		Items = new List<UdpPoint<T>>();
	}

	public void Reset()
	{
		for (int i = 0; i < Items.Count; i++)
		{
			for (int j = 0; j < Items[i].Value.Length; j++)
			{
				Items[i].Value[j] = 0;
			}
			Items[i].IsUpdated = false;
		}
	}

	public void Read(List<byte> data)
	{
		if (data.Count == 0)
		{
			return;
		}
		int num = 0;
		for (int i = 0; i < Items.Count; i++)
		{
			int num2 = Items[i].Size - 1;
			for (int j = 0; j < Items[i].Size; j++)
			{
				if (Items[i].Value[num2] != data[num + j])
				{
					Items[i].IsUpdated = true;
					Items[i].Value[num2] = data[num + j];
				}
				num2--;
			}
			num += Items[i].Size;
			if (num >= data.Count)
			{
				break;
			}
		}
	}

	public UdpPoint<T> InitPoint(T name, int size = 1)
	{
		UdpPoint<T> udpPoint = new UdpPoint<T>();
		udpPoint.Name = name;
		udpPoint.Size = size;
		udpPoint.Value = new byte[size];
		return udpPoint;
	}
}
