using System;
using System.Collections.Generic;

namespace IWP;

internal class BLOKManager : BaseManager<BLOKManager>, IUpdateable
{
	public Action<string> OnMessageReceived;

	private UdpAsyncClient blokClient;

	private BLOKWriteController writeController;

	private BLOKReadController readController;

	private double targetDeltaTime = 0.025;

	private double timeToNextRequest = 0.025;

	public byte SendBlockPower
	{
		set
		{
			writeController.Items[0].Value[0] = value;
		}
	}

	public byte SendEpkButton
	{
		set
		{
			writeController.Items[1].Value[0] = value;
		}
	}

	public byte SendRbButton
	{
		set
		{
			writeController.Items[2].Value[0] = value;
		}
	}

	public byte SendRbsButton
	{
		set
		{
			writeController.Items[3].Value[0] = value;
		}
	}

	public byte SendRbpButton
	{
		set
		{
			writeController.Items[4].Value[0] = value;
		}
	}

	public int SendCoordinateGps
	{
		set
		{
			writeController.Items[5].Write(value);
		}
	}

	public byte SendPicket
	{
		set
		{
			writeController.Items[6].Value[0] = value;
		}
	}

	public float SendTmValue
	{
		set
		{
			writeController.Items[7].Write(value);
		}
	}

	public float SendTcValue
	{
		set
		{
			writeController.Items[8].Write(value);
		}
	}

	public float SendUr1Value
	{
		set
		{
			writeController.Items[9].Write(value);
		}
	}

	public float SendUr2Value
	{
		set
		{
			writeController.Items[10].Write(value);
		}
	}

	public float SendPmValue
	{
		set
		{
			writeController.Items[11].Write(value);
		}
	}

	public float SendAcceleration
	{
		set
		{
			writeController.Items[12].Write(value);
		}
	}

	public float SendActualSpeed
	{
		set
		{
			writeController.Items[13].Write(value);
		}
	}

	public float SendRoadBias
	{
		set
		{
			writeController.Items[14].Write(value);
		}
	}

	public float SendBrakingCoefficient
	{
		set
		{
			writeController.Items[15].Write(value);
		}
	}

	public byte SendEkEquipped
	{
		set
		{
			writeController.Items[16].Value[0] = value;
		}
	}

	public byte SendCassetteEquipped
	{
		set
		{
			writeController.Items[17].Value[0] = value;
		}
	}

	public byte SendTraction
	{
		set
		{
			writeController.Items[18].Value[0] = value;
		}
	}

	public byte SendReverse
	{
		set
		{
			writeController.Items[19].Value[0] = value;
		}
	}

	public long SendWorldTime
	{
		set
		{
			writeController.Items[20].Write(value);
		}
	}

	public byte SendKeyboardButton
	{
		set
		{
			writeController.Items[21].Value[0] = value;
		}
	}

	public string SendStationName
	{
		set
		{
			writeController.Items[22].Write(value);
		}
	}

	public byte SendTargetCode
	{
		set
		{
			writeController.Items[23].Value[0] = value;
		}
	}

	public short SendDistanceToTarget
	{
		set
		{
			writeController.Items[24].Write(value);
		}
	}

	public short SendDistanceToSaut
	{
		set
		{
			writeController.Items[25].Write(value);
		}
	}

	public string SendTargetName
	{
		set
		{
			writeController.Items[26].Write(value);
		}
	}

	public short SendAllowableSpeedByEK
	{
		set
		{
			writeController.Items[27].Write(value);
		}
	}

	public short SendTargetSpeedByEK
	{
		set
		{
			writeController.Items[28].Write(value);
		}
	}

	public short SendDistanceToNextSectionEK
	{
		set
		{
			writeController.Items[29].Write(value);
		}
	}

	public byte SendTrafficLightCode
	{
		set
		{
			writeController.Items[30].Value[0] = value;
		}
	}

	public short SendDistanceToTrafficLight
	{
		set
		{
			writeController.Items[31].Write(value);
		}
	}

	public byte SendCruiseControlCode
	{
		set
		{
			writeController.Items[32].Value[0] = value;
		}
	}

	public short SendCruiseControlSpeed
	{
		set
		{
			writeController.Items[33].Write(value);
		}
	}

	public byte SendNeutralInsert
	{
		set
		{
			writeController.Items[34].Value[0] = value;
		}
	}

	public byte SendTokorazdel
	{
		set
		{
			writeController.Items[35].Value[0] = value;
		}
	}

	public short SendDistanceTokorazdelOrNeutralInsert
	{
		set
		{
			writeController.Items[36].Write(value);
		}
	}

	public byte SendServiceBraking
	{
		set
		{
			writeController.Items[37].Value[0] = value;
		}
	}

	public byte SendMalfunctionCode
	{
		set
		{
			writeController.Items[38].Value[0] = value;
		}
	}

	public byte SendSautEquipped
	{
		set
		{
			writeController.Items[39].Value[0] = value;
		}
	}

	public byte SendTskbmEquipped
	{
		set
		{
			writeController.Items[40].Value[0] = value;
		}
	}

	public byte SendTskbmPower
	{
		set
		{
			writeController.Items[41].Value[0] = value;
		}
	}

	public byte SendTskbmmDressed
	{
		set
		{
			writeController.Items[42].Value[0] = value;
		}
	}

	public byte SendTskbmmStatus
	{
		set
		{
			writeController.Items[43].Value[0] = value;
		}
	}

	public string SendTrafficLightName
	{
		set
		{
			writeController.Items[44].Write(value);
		}
	}

	public byte SendBrakingValveOpenedFromKOH
	{
		set
		{
			writeController.Items[45].Value[0] = value;
		}
	}

	public byte SendBrakingValveOpenedFromEPK
	{
		set
		{
			writeController.Items[46].Value[0] = value;
		}
	}

	public byte SendRoadIndex
	{
		set
		{
			writeController.Items[47].Value[0] = value;
		}
	}

	public int SendVideoCoord
	{
		set
		{
			writeController.Items[48].Write(value);
		}
	}

	public byte GetEpkHoldingCoil => readController.Items[0].Value[0];

	public short GetAllowableSpeed => BitConverter.ToInt16(readController.Items[1].Value, 0);

	public short GetDistanceToAimingPoint => BitConverter.ToInt16(readController.Items[2].Value, 0);

	public byte GetTrafficLightCode => readController.Items[3].Value[0];

	public byte GetDisassembleTheTraction => readController.Items[4].Value[0];

	public byte GetUseServiceBrakingOfSaut => readController.Items[5].Value[0];

	public byte GetLampAttention => readController.Items[6].Value[0];

	public byte GetLampPssPass => readController.Items[7].Value[0];

	public override void Create()
	{
		base.Create();
		blokClient = new UdpAsyncClient(256);
		writeController = new BLOKWriteController();
		readController = new BLOKReadController();
	}

	public override void PostInit()
	{
		base.PostInit();
		blokClient.Init(GameConfig.Instance.BlokIp, GameConfig.Instance.BlokPort);
	}

	public void Reset()
	{
		readController.Reset();
	}

	public void Update()
	{
		timeToNextRequest -= BaseManager<TimeManager>.instance.DeltaTime;
		if (timeToNextRequest < 0.0)
		{
			timeToNextRequest += targetDeltaTime;
			if (blokClient.Initiated)
			{
				ProcessIO();
			}
		}
	}

	private void ProcessIO()
	{
		byte[] commandString = writeController.GetCommandString();
		if (!blokClient.Sending)
		{
			blokClient.Send(commandString, commandString.Length);
		}
		if (!blokClient.Receiving)
		{
			blokClient.Receive();
		}
		OnReceiveMessageHandler(blokClient.Response);
	}

	private void OnReceiveMessageHandler(List<byte> message)
	{
		readController.Read(message);
		for (int i = 0; i < readController.Items.Count; i++)
		{
			if (readController.Items[i].IsUpdated)
			{
				if (OnMessageReceived != null)
				{
					OnMessageReceived(readController.Items[i].Name);
				}
				if (GameConfig.Instance.DebugBlok)
				{
					Console.WriteLine("//Blok income//");
					Console.WriteLine("Item Index: {0}", i);
					Console.WriteLine("Item Name: {0}", readController.Items[i].Name);
					Console.WriteLine("Item Value: {0}", readController.Items[i].Value);
					Console.WriteLine();
				}
				readController.Items[i].IsUpdated = false;
			}
		}
	}
}
