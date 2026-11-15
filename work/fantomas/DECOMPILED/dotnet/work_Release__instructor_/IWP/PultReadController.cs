using System;
using System.Collections.Generic;

namespace IWP;

internal class PultReadController
{
	public class Point
	{
		public ControlPanelParameters Name;

		public int Size;

		public string Value;

		public bool IsUpdated;

		public Action<string> Command;

		public bool Listen;
	}

	public List<Point> Items;

	public PultReadController()
	{
		TrainVL80C();
		Reset();
	}

	public void Reset()
	{
		for (int i = 0; i < Items.Count; i++)
		{
			Items[i].Value = PultController.GetFormatString("0", Items[i].Size);
			if (Items[i].Listen)
			{
				Items[i].IsUpdated = false;
			}
		}
	}

	public string Read(List<byte> data)
	{
		if (data.Count == 0)
		{
			return null;
		}
		string text = "";
		for (int i = 0; i < Items.Count && i < data.Count; i++)
		{
			string text2 = Convert.ToString(Convert.ToUInt16(data[i]));
			if (Items[i].Value != text2)
			{
				Items[i].IsUpdated = true;
				Items[i].Value = text2;
				text = string.Concat(text, Items[i].Name, " > ", Items[i].Value, ", ");
			}
		}
		return text;
	}

	public Point InitPoint(ControlPanelParameters name, int size, bool listen = true)
	{
		return new Point
		{
			Name = name,
			Size = size,
			Listen = listen,
			Command = (string x) =>
			{
				float num = float.Parse(x);
				if (num != MainDataStorage.Instance.DataStorage[name].Data)
				{
					MainDataStorage.Instance.DataStorage[name].Data = num;
				}
			}
		};
	}

	private void TrainVL80C()
	{
		Items = new List<Point>
		{
			InitPoint(ControlPanelParameters.BtnRb, 1),
			InitPoint(ControlPanelParameters.BtnRbs, 1),
			InitPoint(ControlPanelParameters.SwEpk, 1),
			InitPoint(ControlPanelParameters.SwPedal, 1),
			InitPoint(ControlPanelParameters.BtnSand, 1),
			InitPoint(ControlPanelParameters.BtnTyphon, 1),
			InitPoint(ControlPanelParameters.BtnWhistle, 1),
			InitPoint(ControlPanelParameters.ReversController, 1),
			InitPoint(ControlPanelParameters.MainController, 1),
			InitPoint(ControlPanelParameters.SwBufferLightLeftFront, 1),
			InitPoint(ControlPanelParameters.SwBufferLightRightFront, 1),
			InitPoint(ControlPanelParameters.SwBufferLightLeftRear, 1),
			InitPoint(ControlPanelParameters.SwBufferLightRightRear, 1),
			InitPoint(ControlPanelParameters.SwSpotlightBright, 1),
			InitPoint(ControlPanelParameters.SwSpotlightDull, 1),
			InitPoint(ControlPanelParameters.BtnSandOnFirstAxis, 1),
			InitPoint(ControlPanelParameters.BtnBrakeRelease, 1),
			InitPoint(ControlPanelParameters.SwFilter, 1),
			InitPoint(ControlPanelParameters.SwVigilance, 1),
			InitPoint(ControlPanelParameters.BtnCheckAlsn, 1),
			InitPoint(ControlPanelParameters.BtnSkz, 1),
			InitPoint(ControlPanelParameters.SwCabinLightBright, 1),
			InitPoint(ControlPanelParameters.SwLocomotiveControl, 1),
			InitPoint(ControlPanelParameters.SwMotion, 1),
			InitPoint(ControlPanelParameters.SwConversionManagement, 1),
			InitPoint(ControlPanelParameters.SwRefrigeratorManagement, 1),
			InitPoint(ControlPanelParameters.SwFan1, 1),
			InitPoint(ControlPanelParameters.SwFan2, 1),
			InitPoint(ControlPanelParameters.SwFan3, 1),
			InitPoint(ControlPanelParameters.SwFan4, 1),
			InitPoint(ControlPanelParameters.BtnCall, 1),
			InitPoint(ControlPanelParameters.SwDamageIndicator1, 1),
			InitPoint(ControlPanelParameters.SwDamage1, 1),
			InitPoint(ControlPanelParameters.SwDamage2, 1),
			InitPoint(ControlPanelParameters.SwAlarmMonitoring, 1),
			InitPoint(ControlPanelParameters.SwBoxing, 1),
			InitPoint(ControlPanelParameters.SwEmergencyStop, 1),
			InitPoint(ControlPanelParameters.SwFuelPump1, 1),
			InitPoint(ControlPanelParameters.BtnLaunch1, 1),
			InitPoint(ControlPanelParameters.SwFuelPump2, 1),
			InitPoint(ControlPanelParameters.BtnLaunch2, 1),
			InitPoint(ControlPanelParameters.nobutton1, 1),
			InitPoint(ControlPanelParameters.nobutton2, 1),
			InitPoint(ControlPanelParameters.nobutton3, 1),
			InitPoint(ControlPanelParameters.SwKm, 1),
			InitPoint(ControlPanelParameters.SwKvtAnalog, 1),
			InitPoint(ControlPanelParameters.SwClubDecoder, 1),
			InitPoint(ControlPanelParameters.SwKvt, 1)
		};
	}
}
