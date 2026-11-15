using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;

namespace IWP;

internal class PultAsyncClient
{
	private string ip;

	private int port = 2000;

	private UdpClient clientSocket;

	private IPEndPoint remoteEP;

	private bool _connecting;

	private bool _sending;

	private bool _receiveing;

	public List<byte> response;

	public bool changeKey;

	public bool connecting => _connecting;

	public bool sending => _sending;

	public bool receiveing => _receiveing;

	public bool connected
	{
		get
		{
			if (clientSocket == null)
			{
				return false;
			}
			return true;
		}
	}

	public PultAsyncClient()
	{
		response = new List<byte>();
	}

	public void Connect(string ip, int port)
	{
		try
		{
			_connecting = true;
			this.port = port;
			this.ip = ip;
			IPAddress address = IPAddress.Parse(ip);
			remoteEP = new IPEndPoint(address, port);
			clientSocket = new UdpClient(2001);
		}
		catch (Exception value)
		{
			if (GameConfig.Instance.DebugPult)
			{
				Console.WriteLine(value);
			}
			_connecting = false;
		}
	}

	public void Receive()
	{
		try
		{
			_receiveing = true;
			StateObject stateObject = new StateObject();
			stateObject.WorkSocket = clientSocket;
			stateObject.RemoteEp = remoteEP;
			clientSocket.BeginReceive(ReceiveCallback, stateObject);
		}
		catch (Exception)
		{
			_receiveing = false;
		}
	}

	private void ReceiveCallback(IAsyncResult ar)
	{
		try
		{
			StateObject stateObject = (StateObject)ar.AsyncState;
			UdpClient workSocket = stateObject.WorkSocket;
			IPEndPoint remoteEp = stateObject.RemoteEp;
			byte[] array = workSocket.EndReceive(ar, ref remoteEp);
			if (array.Length != 0)
			{
				for (int i = 0; i < array.Length; i++)
				{
					if (response.Count < array.Length)
					{
						response.Add(array[i]);
						continue;
					}
					changeKey = true;
					response[i] = array[i];
				}
				_receiveing = false;
			}
			else
			{
				_receiveing = false;
			}
		}
		catch (Exception)
		{
			_receiveing = false;
		}
	}

	public void Send(byte[] data, int tx_buffer_lenght)
	{
		_sending = true;
		clientSocket.BeginSend(data, tx_buffer_lenght, remoteEP, SendCallback, clientSocket);
	}

	private void SendCallback(IAsyncResult ar)
	{
		try
		{
			UdpClient udpClient = (UdpClient)ar.AsyncState;
			int num = udpClient.EndSend(ar);
			_sending = false;
		}
		catch (Exception)
		{
			_sending = false;
		}
	}

	public void Shutdown()
	{
		clientSocket.Close();
	}
}
