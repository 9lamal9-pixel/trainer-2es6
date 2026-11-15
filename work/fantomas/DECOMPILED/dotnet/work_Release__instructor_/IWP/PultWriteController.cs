using System;
using System.Collections.Generic;

namespace IWP;

public class PultWriteController
{
	public class Point
	{
		public ControlPanelParameters Name;

		public int Size;

		public float prevValue = -1f;

		public float Value;

		public Action<float, float> Command;

		public Func<float, float> OnBeforeDataReceivingToPult;
	}

	private const float megapascalsToKilograms = 10.197162f;

	public List<Point> Items;

	private byte[] buf_tx = new byte[256];

	private ushort tx_length;

	public ushort tx_buffer_lenght;

	public PultWriteController()
	{
		TrainVL80C();
	}

	private void UpdateItems()
	{
		for (int i = 0; i < Items.Count; i++)
		{
			Items[i].prevValue = Items[i].Value;
			Items[i].Value = MainDataStorage.Instance.DataStorage[Items[i].Name].Data;
			if (GameConfig.Instance.DebugPult && Items[i].prevValue != Items[i].Value)
			{
				Console.WriteLine("//Pult outcome//");
				Console.WriteLine("Item Index: {0}", i);
				Console.WriteLine("Item Name: {0}", Items[i].Name);
				Console.WriteLine("Item Value: {0}", Items[i].Value);
				Console.WriteLine();
			}
		}
	}

	public byte[] GetCommandString()
	{
		UpdateItems();
		tx_length = 0;
		for (int i = 0; i < Items.Count; i++)
		{
			if (Items[i].Command != null)
			{
				Items[i].Command(Items[i].prevValue, Items[i].Value);
			}
			if (Items[i].Size == 2)
			{
				tx_length += 2;
				if (Items[i].OnBeforeDataReceivingToPult != null)
				{
					buf_tx[tx_length - 2] = Convert.ToByte(Convert.ToUInt16(Items[i].OnBeforeDataReceivingToPult(Items[i].Value)) >> 8);
					buf_tx[tx_length - 1] = Convert.ToByte(Convert.ToUInt16(Items[i].OnBeforeDataReceivingToPult(Items[i].Value)) & 0xFF);
				}
				else
				{
					buf_tx[tx_length - 2] = Convert.ToByte(Convert.ToUInt16(Items[i].Value) >> 8);
					buf_tx[tx_length - 1] = Convert.ToByte(Convert.ToUInt16(Items[i].Value) & 0xFF);
				}
			}
			else
			{
				tx_length++;
				if (Items[i].OnBeforeDataReceivingToPult != null)
				{
					buf_tx[tx_length - 1] = Convert.ToByte(Items[i].OnBeforeDataReceivingToPult(Items[i].Value));
				}
				else
				{
					buf_tx[tx_length - 1] = Convert.ToByte(Items[i].Value);
				}
			}
		}
		tx_buffer_lenght = tx_length;
		return buf_tx;
	}

	public Point InitPoint(ControlPanelParameters name, int size, Func<float, float> listener)
	{
		return new Point
		{
			Name = name,
			Size = size,
			Value = MainDataStorage.Instance.DataStorage[name].Data,
			OnBeforeDataReceivingToPult = listener,
			Command = (float x, float y) =>
			{
			}
		};
	}

	public Point InitPoint(ControlPanelParameters name, int size)
	{
		return InitPoint(name, size, null);
	}

	private void TrainVL80C()
	{
		Items = new List<Point>
		{
			InitPoint(ControlPanelParameters.ValueOil1, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return 999f * x / 15f;
			}),
			InitPoint(ControlPanelParameters.ValueOil23, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return 999f * x / 15f;
			}),
			InitPoint(ControlPanelParameters.ValueOil1Temperature, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return x / 120f * 999f;
			}),
			InitPoint(ControlPanelParameters.ValueOil23Temperature, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return x / 120f * 999f;
			}),
			InitPoint(ControlPanelParameters.ValueWater1Temperature, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return x / 120f * 999f;
			}),
			InitPoint(ControlPanelParameters.ValueWater23Temperature, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return x / 120f * 999f;
			}),
			InitPoint(ControlPanelParameters.ValueTc, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return 999f * x * 10.197162f / 10f;
			}),
			InitPoint(ControlPanelParameters.ValueTc2, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return 999f * x * 10.197162f / 10f;
			}),
			InitPoint(ControlPanelParameters.ValueUr, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return 999f * x * 10.197162f / 10f;
			}),
			InitPoint(ControlPanelParameters.ValueTm, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return 999f * x * 10.197162f / 16f;
			}),
			InitPoint(ControlPanelParameters.ValueGr, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return 999f * x * 10.197162f / 16f;
			}),
			InitPoint(ControlPanelParameters.ValueBatteryPower, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return (150f + x) / 300f * 999f;
			}),
			InitPoint(ControlPanelParameters.ValueGeneratorVoltage, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return x * 999f;
			}),
			InitPoint(ControlPanelParameters.ValueGeneratorPower, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return x / 6000f * 999f;
			}),
			InitPoint(ControlPanelParameters.ValueDamageIndicator, 2, (float x) =>
			{
				if (x < 0f)
				{
					x = 0f;
				}
				return x / 10f * 999f;
			}),
			InitPoint(ControlPanelParameters.LampLoadShedding1, 1),
			InitPoint(ControlPanelParameters.LampLoadShedding2, 1),
			InitPoint(ControlPanelParameters.LampWaterLevel, 1),
			InitPoint(ControlPanelParameters.LampOilPressure, 1),
			InitPoint(ControlPanelParameters.LampNoLamp, 1),
			InitPoint(ControlPanelParameters.LampLineBrakeOpen, 1),
			InitPoint(ControlPanelParameters.LampBraked, 1),
			InitPoint(ControlPanelParameters.LampFire, 1),
			InitPoint(ControlPanelParameters.LampWorkDiesel2, 1),
			InitPoint(ControlPanelParameters.LampCooling, 1),
			InitPoint(ControlPanelParameters.LampEarthingRelay, 1),
			InitPoint(ControlPanelParameters.LampVigilance, 1),
			InitPoint(ControlPanelParameters.LampPss, 1)
		};
	}
}
