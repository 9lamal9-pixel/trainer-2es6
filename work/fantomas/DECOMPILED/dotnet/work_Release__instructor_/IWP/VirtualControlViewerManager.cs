using System;
using System.Collections.Generic;

namespace IWP;

internal class VirtualControlViewerManager : BaseManager<VirtualControlViewerManager>, IUpdateable
{
	public Action<string> OnMessageReceived;

	private UdpAsyncClient _udpAsyncClient;

	private VirtualControlViewerWriteController _writeController;

	private const double TargetDeltaTime = 0.025;

	private double _timeToNextRequest = 0.025;

	public override void Create()
	{
		base.Create();
		_udpAsyncClient = new UdpAsyncClient(256);
		_writeController = new VirtualControlViewerWriteController();
	}

	public override void PostInit()
	{
		base.PostInit();
		if (GameConfig.Instance.IsActiveVirtualControlViewer)
		{
			_udpAsyncClient.Init(GameConfig.Instance.VirtualControlViewerIp, GameConfig.Instance.VirtualControlViewerPort);
		}
	}

	public void Reset()
	{
	}

	public void Update()
	{
		_timeToNextRequest -= BaseManager<TimeManager>.instance.DeltaTime;
		if (_timeToNextRequest < 0.0)
		{
			_timeToNextRequest += 0.025;
			if (_udpAsyncClient.Initiated)
			{
				ProcessIO();
			}
		}
	}

	private void ProcessIO()
	{
		for (int i = 0; i < _writeController.Items.Count; i++)
		{
			if (_writeController.Items[i].Size == 1)
			{
				if (_writeController.Items[i].ConvertBeforeDataSend == null)
				{
					_writeController.Items[i].Value[0] = (byte)(int)MainDataStorage.Instance.DataStorage[_writeController.Items[i].Name].Data;
				}
				else
				{
					_writeController.Items[i].Value[0] = (byte)_writeController.Items[i].ConvertBeforeDataSend(MainDataStorage.Instance.DataStorage[_writeController.Items[i].Name].Data);
				}
			}
			else if (_writeController.Items[i].Size == 2)
			{
				if (_writeController.Items[i].ConvertBeforeDataSend == null)
				{
					_writeController.Items[i].Write((short)MainDataStorage.Instance.DataStorage[_writeController.Items[i].Name].Data);
				}
				else
				{
					_writeController.Items[i].Write(_writeController.Items[i].ConvertBeforeDataSend(MainDataStorage.Instance.DataStorage[_writeController.Items[i].Name].Data));
				}
			}
		}
		byte[] commandString = _writeController.GetCommandString();
		if (!_udpAsyncClient.Sending)
		{
			_udpAsyncClient.Send(commandString, commandString.Length);
		}
		if (!_udpAsyncClient.Receiving)
		{
			_udpAsyncClient.Receive();
		}
		OnReceiveMessageHandler(_udpAsyncClient.Response);
	}

	private void OnReceiveMessageHandler(List<byte> message)
	{
	}
}
