using System.Net;
using System.Net.Sockets;
using System.Text;

namespace IWP;

internal class _StateObject
{
	public UdpClient workSocket = null;

	public IPEndPoint remoteEP = null;

	public const int BufferSize = 256;

	public byte[] buffer = new byte[256];

	public StringBuilder sb = new StringBuilder();
}
