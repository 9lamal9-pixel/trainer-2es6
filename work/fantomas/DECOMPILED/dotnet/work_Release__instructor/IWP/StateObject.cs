using System.Net;
using System.Net.Sockets;
using System.Text;

namespace IWP;

internal class StateObject
{
	public UdpClient WorkSocket;

	public IPEndPoint RemoteEp;

	public const int BufferSize = 256;

	public byte[] Buffer = new byte[256];

	public StringBuilder Sb = new StringBuilder();
}
