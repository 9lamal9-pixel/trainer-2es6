using System.Net;
using System.Net.Sockets;

namespace NetLib;

public class NetClient
{
	private Socket sock = null;

	public NetClient(string host_name, int Port = 11000)
	{
		IPEndPoint remoteEP = new IPEndPoint(IPAddress.Parse(host_name), Port);
		sock = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
		sock.Connect(remoteEP);
	}

	public void Stop()
	{
		sock.Shutdown(SocketShutdown.Both);
		sock.Close();
	}

	public void ExchangeData(DataPack dt)
	{
		if (!dt.Empty)
		{
			sock.Send(dt.Save());
			dt.Clear();
		}
	}
}
