using System;
using System.Collections.Generic;

namespace IWP;

internal class PultManager : BaseManager<PultManager>, IUpdateable
{
	public Action OnConnected;

	public Action OnDisconnected;

	public bool sendData = false;

	private bool _connected;

	private PultWriteController m_WriteController = new PultWriteController();

	private PultReadController m_ReadController = new PultReadController();

	private double targetDeltaTime = 0.025;

	private double timeToNextRequest;

	private List<byte> response = new List<byte>();

	public bool connected
	{
		get
		{
			return _connected;
		}
		private set
		{
			if (value && value != _connected && OnConnected != null)
			{
				OnConnected();
			}
			if (!value && value != _connected && OnDisconnected != null)
			{
				OnDisconnected();
			}
			_connected = value;
		}
	}

	public override void PostInit()
	{
		base.PostInit();
		PultController.Instance.Connect();
	}

	public void Reset()
	{
		m_ReadController.Reset();
	}

	public void Update()
	{
		if (GameConfig.Instance.IsActivePult)
		{
			ProcessInput();
			timeToNextRequest -= BaseManager<TimeManager>.instance.DeltaTime;
			if (timeToNextRequest < 0.0)
			{
				timeToNextRequest += targetDeltaTime;
				ProcessOutput();
			}
		}
	}

	private void ProcessInput()
	{
		if (!PultController.Instance.m_PultClient.receiveing)
		{
			PultController.Instance.Receive();
		}
		response = PultController.Instance.m_PultClient.response;
		OnReceiveMessageHandler(response);
	}

	private void ProcessOutput()
	{
		byte[] commandString = m_WriteController.GetCommandString();
		if (!PultController.Instance.m_PultClient.sending)
		{
			PultController.Instance.SendCmd(commandString, m_WriteController.tx_buffer_lenght);
		}
	}

	public void OnReceiveMessageHandler(List<byte> message)
	{
		m_ReadController.Read(message);
		for (int i = 0; i < m_ReadController.Items.Count; i++)
		{
			if (m_ReadController.Items[i].IsUpdated)
			{
				if (m_ReadController.Items[i].Name == ControlPanelParameters.BtnWhistle)
				{
					Console.WriteLine("whistle");
				}
				if (m_ReadController.Items[i].Name == ControlPanelParameters.BtnTyphon)
				{
					Console.WriteLine("typhon");
				}
				if (GameConfig.Instance.DebugPult)
				{
					Console.WriteLine("//Pult income//");
					Console.WriteLine("Item Index: {0}", i);
					Console.WriteLine("Item Name: {0}", m_ReadController.Items[i].Name);
					Console.WriteLine("Item Value: {0}", m_ReadController.Items[i].Value);
					Console.WriteLine();
				}
				m_ReadController.Items[i].IsUpdated = false;
				if (m_ReadController.Items[i].Command != null)
				{
					m_ReadController.Items[i].Command(m_ReadController.Items[i].Value);
				}
			}
		}
	}
}
