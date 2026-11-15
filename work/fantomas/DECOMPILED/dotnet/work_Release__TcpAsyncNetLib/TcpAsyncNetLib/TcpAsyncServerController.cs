using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace TcpAsyncNetLib;

public class TcpAsyncServerController : TcpAsyncBaseController
{
	public TcpAsyncServerController(int numConnections, int receiveBufferSize)
		: base(numConnections, receiveBufferSize)
	{
	}

	public override void Start(IPEndPoint localEndPoint)
	{
		isRunning = true;
		listenSocket = new Socket(localEndPoint.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
		listenSocket.Bind(localEndPoint);
		listenSocket.Listen(100);
		StartAccept(null);
	}

	public override void Stop()
	{
		isRunning = false;
	}

	public void StartAccept(SocketAsyncEventArgs acceptEventArg)
	{
		if (acceptEventArg == null)
		{
			acceptEventArg = new SocketAsyncEventArgs();
			acceptEventArg.Completed += AcceptEventArg_Completed;
		}
		else
		{
			acceptEventArg.AcceptSocket = null;
		}
		m_maxNumberAcceptedClients.WaitOne();
		if (!isRunning)
		{
			m_maxNumberAcceptedClients.Release();
		}
		else if (!listenSocket.AcceptAsync(acceptEventArg))
		{
			ProcessAccept(acceptEventArg);
		}
	}

	private void AcceptEventArg_Completed(object sender, SocketAsyncEventArgs e)
	{
		ProcessAccept(e);
	}

	private void ProcessAccept(SocketAsyncEventArgs e)
	{
		Interlocked.Increment(ref m_numConnectedSockets);
		SocketAsyncEventArgs e2 = m_readWritePool.Pop();
		((AsyncUserToken)e2.UserToken).Socket = e.AcceptSocket;
		if (!e.AcceptSocket.ReceiveAsync(e2))
		{
			ProcessReceive(e2);
		}
		StartAccept(e);
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
		if (e.BytesTransferred > 0 && e.SocketError == SocketError.Success)
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
		if (e.SocketError == SocketError.Success)
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
			asyncUserToken.Socket.Shutdown(SocketShutdown.Send);
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
