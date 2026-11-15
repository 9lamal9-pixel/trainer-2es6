using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace NetLib;

public class NetServ
{
	private Thread sockThread;

	private Socket listener = null;

	private Socket client = null;

	private object syncObj = new object();

	private ProcessDataPack _callback = null;

	private ChangeSrvState _css = null;

	public NetServ(ProcessDataPack callback, ChangeSrvState css = null, int port = 11000)
	{
		NetServ netServ = this;
		_callback = callback;
		_css = css;
		ThreadStart start = () =>
		{
			IPEndPoint localEP = new IPEndPoint(IPAddress.Any, port);
			netServ.listener = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
			netServ.listener.Bind(localEP);
			netServ.listener.Listen(10);
			try
			{
				while (true)
				{
					netServ.client = netServ.listener.Accept();
					netServ.sockLoop(netServ.client);
				}
			}
			catch
			{
			}
		};
		sockThread = new Thread(start);
		sockThread.Start();
	}

	private void sockLoop(object obj)
	{
		Socket socket = (Socket)obj;
		byte[] array = new byte[2];
		List<char> list = new List<char>(1024);
		if (_css != null)
		{
			_css(SrvState.ssStart);
		}
		try
		{
			try
			{
				while (socket.Receive(array) > 1)
				{
					char[] chars = Encoding.Unicode.GetChars(array);
					if (chars[0] == '\r')
					{
						DataPack dataPack = new DataPack();
						dataPack.Load(new string(list.ToArray()));
						list.Clear();
						_callback(dataPack);
					}
					else
					{
						list.Add(chars[0]);
					}
				}
			}
			finally
			{
				socket.Shutdown(SocketShutdown.Both);
				socket.Close();
			}
		}
		catch
		{
		}
		if (_css != null)
		{
			_css(SrvState.ssFinish);
		}
	}

	public void StopServer()
	{
		if (client != null)
		{
			client.Close();
		}
		listener.Close();
	}
}
