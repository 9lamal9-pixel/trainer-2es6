using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;

namespace IWP;

internal class UdpAsyncClient
{
	private UdpClient _clientSocket;

	private IPEndPoint _remoteEp;

	public List<byte> Response;

	public bool Sending { get; private set; }

	public bool Receiving { get; private set; }

	public bool Initiated => _clientSocket != null;

	public UdpAsyncClient(int bufferSize)
	{
		Response = new List<byte>(bufferSize);
	}

	public void Init(string ip, int port)
	{
		try
		{
			IPAddress address = IPAddress.Parse(ip);
			_remoteEp = new IPEndPoint(address, port);
			_clientSocket = new UdpClient();
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	public void Receive()
	{
		try
		{
			Receiving = true;
			StateObject stateObject = new StateObject();
			stateObject.WorkSocket = _clientSocket;
			stateObject.RemoteEp = _remoteEp;
			_clientSocket.BeginReceive(ReceiveCallback, stateObject);
		}
		catch (Exception)
		{
			Receiving = false;
		}
	}

	private void ReceiveCallback(IAsyncResult ar)
	{
		try
		{
			StateObject stateObject = (StateObject)ar.AsyncState;
			UdpClient workSocket = stateObject.WorkSocket;
			IPEndPoint remoteEP = stateObject.RemoteEp;
			byte[] array = workSocket.EndReceive(ar, ref remoteEP);
			for (int i = 0; i < array.Length; i++)
			{
				if (Response.Count < array.Length)
				{
					Response.Add(array[i]);
				}
				else
				{
					Response[i] = array[i];
				}
			}
			Receiving = false;
		}
		catch (Exception)
		{
			Receiving = false;
		}
	}

	public void Send(byte[] buffer, int bufferLength)
	{
		Sending = true;
		_clientSocket.BeginSend(buffer, bufferLength, _remoteEp, SendCallback, _clientSocket);
	}

	private void SendCallback(IAsyncResult ar)
	{
		try
		{
			UdpClient udpClient = (UdpClient)ar.AsyncState;
			int num = udpClient.EndSend(ar);
			Sending = false;
		}
		catch (Exception)
		{
			Sending = false;
		}
	}

	public void Shutdown()
	{
		_clientSocket.Close();
	}
}
