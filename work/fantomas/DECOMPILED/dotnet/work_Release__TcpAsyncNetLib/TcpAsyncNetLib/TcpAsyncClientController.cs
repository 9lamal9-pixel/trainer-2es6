using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace TcpAsyncNetLib;

public class TcpAsyncClientController : TcpAsyncBaseController
{
	private IPEndPoint remoteServerEndPoint;

	public TcpAsyncClientController(int bufferSize)
		: base(1, bufferSize)
	{
	}

	public override void Start(IPEndPoint remoteServerEndPoint)
	{
		isRunning = true;
		this.remoteServerEndPoint = remoteServerEndPoint;
		ConnectionAccept(null);
	}

	public override void Stop()
	{
		isRunning = false;
	}

	public void ConnectionAccept(SocketAsyncEventArgs acceptEventArg)
	{
		if (acceptEventArg == null)
		{
			acceptEventArg = new SocketAsyncEventArgs();
			acceptEventArg.RemoteEndPoint = remoteServerEndPoint;
			acceptEventArg.Completed += ConnectionEventArg_Completed;
		}
		m_maxNumberAcceptedClients.WaitOne();
		if (!isRunning)
		{
			m_maxNumberAcceptedClients.Release();
			return;
		}
		listenSocket = new Socket(remoteServerEndPoint.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
		if (!listenSocket.ConnectAsync(acceptEventArg))
		{
			ProcessConnection(acceptEventArg);
		}
	}

	private void ConnectionEventArg_Completed(object sender, SocketAsyncEventArgs e)
	{
		ProcessConnection(e);
	}

	private void ProcessConnection(SocketAsyncEventArgs e)
	{
		if (e.ConnectSocket != null)
		{
			Interlocked.Increment(ref m_numConnectedSockets);
			SocketAsyncEventArgs e2 = m_readWritePool.Pop();
			((AsyncUserToken)e2.UserToken).Socket = e.ConnectSocket;
			e.SetBuffer(0, 4);
			if (!e.ConnectSocket.SendAsync(e2))
			{
				ProcessSend(e2);
			}
		}
		else
		{
			m_maxNumberAcceptedClients.Release();
		}
		ConnectionAccept(e);
	}

	protected override void IO_Completed(object sender, SocketAsyncEventArgs e)
	{
		switch (e.LastOperation)
		{
		case SocketAsyncOperation.Receive:
			ProcessReceive(e);
			break;
		case SocketAsyncOperation.Send:
			ProcessSend(e);
			break;
		default:
			throw new ArgumentException("The last operation completed on the socket was not a receive or send");
		}
	}

	private void ProcessReceive(SocketAsyncEventArgs e)
	{
		AsyncUserToken asyncUserToken = (AsyncUserToken)e.UserToken;
		if (e.BytesTransferred > 0 && e.SocketError == SocketError.Success && isRunning)
		{
			if (OnBytesReceived != null)
			{
				networkReader.Replace(e.Buffer);
				short num = networkReader.ReadInt16();
				short arg = networkReader.ReadInt16();
				if (num > 0)
				{
					OnBytesReceived(arg, networkReader.ReadBytes(num));
				}
			}
			networkWriter.SeekZero();
			if (messages.TryDequeue(out var result) && result != null)
			{
				networkWriter.StartMessage(result.msgType);
				result.Serialize(networkWriter);
			}
			else
			{
				networkWriter.StartMessage(0);
			}
			networkWriter.FinishMessage();
			byte[] buffer = networkWriter.AsArray();
			e.SetBuffer(buffer, e.Offset, e.BytesTransferred);
			if (!asyncUserToken.Socket.SendAsync(e))
			{
				ProcessSend(e);
			}
		}
		else
		{
			CloseClientSocket(e);
		}
	}

	private void ProcessSend(SocketAsyncEventArgs e)
	{
		if (e.SocketError == SocketError.Success && isRunning)
		{
			if (!((AsyncUserToken)e.UserToken).Socket.ReceiveAsync(e))
			{
				ProcessReceive(e);
			}
		}
		else
		{
			CloseClientSocket(e);
		}
	}

	private void CloseClientSocket(SocketAsyncEventArgs e)
	{
		AsyncUserToken asyncUserToken = e.UserToken as AsyncUserToken;
		try
		{
			asyncUserToken.Socket.Shutdown(SocketShutdown.Both);
		}
		catch (Exception)
		{
		}
		asyncUserToken.Socket.Close();
		Interlocked.Decrement(ref m_numConnectedSockets);
		m_readWritePool.Push(e);
		m_maxNumberAcceptedClients.Release();
	}
}
