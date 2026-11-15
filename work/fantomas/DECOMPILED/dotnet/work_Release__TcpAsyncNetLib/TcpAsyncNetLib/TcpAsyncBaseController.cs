using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace TcpAsyncNetLib;

public abstract class TcpAsyncBaseController
{
	protected class SocketAsyncEventArgsPool
	{
		private Stack<SocketAsyncEventArgs> m_pool;

		public int Count => m_pool.Count;

		public SocketAsyncEventArgsPool(int capacity)
		{
			m_pool = new Stack<SocketAsyncEventArgs>(capacity);
		}

		public void Push(SocketAsyncEventArgs item)
		{
			if (item == null)
			{
				throw new ArgumentNullException("Items added to a SocketAsyncEventArgsPool cannot be null");
			}
			lock (m_pool)
			{
				m_pool.Push(item);
			}
		}

		public SocketAsyncEventArgs Pop()
		{
			lock (m_pool)
			{
				return m_pool.Pop();
			}
		}
	}

	public Action<short, byte[]> OnBytesReceived;

	protected int m_numConnections;

	protected int m_bufferSize;

	protected Socket listenSocket;

	protected SocketAsyncEventArgsPool m_readWritePool;

	protected int m_totalBytesRead;

	protected int m_numConnectedSockets;

	protected Semaphore m_maxNumberAcceptedClients;

	protected NetworkWriter networkWriter;

	protected NetworkReader networkReader;

	protected ConcurrentQueue<NetMessageBase> messages;

	protected bool isRunning;

	public int NumberOfConnections => m_numConnectedSockets;

	public TcpAsyncBaseController(int numConnections, int bufferSize)
	{
		m_totalBytesRead = 0;
		m_numConnectedSockets = 0;
		m_numConnections = numConnections;
		m_bufferSize = bufferSize;
		m_readWritePool = new SocketAsyncEventArgsPool(numConnections);
		m_maxNumberAcceptedClients = new Semaphore(numConnections, numConnections);
		networkWriter = new NetworkWriter(new byte[bufferSize]);
		networkReader = new NetworkReader();
		messages = new ConcurrentQueue<NetMessageBase>();
	}

	public void Init()
	{
		for (int i = 0; i < m_numConnections; i++)
		{
			SocketAsyncEventArgs e = new SocketAsyncEventArgs();
			e.Completed += IO_Completed;
			e.UserToken = new AsyncUserToken();
			e.SetBuffer(new byte[m_bufferSize], 0, m_bufferSize);
			m_readWritePool.Push(e);
		}
	}

	public abstract void Start(IPEndPoint localEndPoint);

	public abstract void Stop();

	protected abstract void IO_Completed(object sender, SocketAsyncEventArgs e);

	public void SendMessage(NetMessageBase message)
	{
		messages.Enqueue(message);
	}
}
