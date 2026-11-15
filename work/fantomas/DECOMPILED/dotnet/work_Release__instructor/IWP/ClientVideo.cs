using System;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace IWP;

internal class ClientVideo
{
	private int port;

	private string hostIpAddress;

	private TcpClient cVideo;

	public bool IsConnected => cVideo.Connected;

	public bool IsConnecting { get; private set; }

	public bool isVideoStarted { get; private set; }

	public ClientVideo(string hostIpAddress, int port = 12700)
	{
		cVideo = new TcpClient();
		cVideo.NoDelay = true;
		this.hostIpAddress = hostIpAddress;
		this.port = port;
	}

	public void Connect()
	{
		if (IsConnecting || IsConnected)
		{
			return;
		}
		IsConnecting = true;
		Task.Run(() =>
		{
			while (true)
			{
				try
				{
					cVideo.Connect(hostIpAddress, port);
					IsConnecting = false;
					break;
				}
				catch (Exception value)
				{
					if (GameConfig.Instance.DebugVideo)
					{
						Console.WriteLine(value);
					}
				}
			}
		});
	}

	private byte[] PrepareMessage(short msgType)
	{
		byte[] bytes = BitConverter.GetBytes(0);
		byte[] bytes2 = BitConverter.GetBytes(msgType);
		return new byte[4]
		{
			bytes[0],
			bytes[1],
			bytes2[0],
			bytes2[1]
		};
	}

	private byte[] PrepareMessage(short msgType, byte[] data)
	{
		byte[] bytes = BitConverter.GetBytes(data.Length);
		byte[] bytes2 = BitConverter.GetBytes(msgType);
		byte[] array = new byte[data.Length + 4];
		array[0] = bytes[1];
		array[1] = bytes[0];
		array[2] = bytes2[1];
		array[3] = bytes2[0];
		for (int i = 0; i < data.Length; i++)
		{
			array[i + 4] = data[i];
		}
		return array;
	}

	public void Start(byte uchastok)
	{
		if (isVideoStarted)
		{
			return;
		}
		try
		{
			Thread.Sleep(3000);
			byte[] buffer = PrepareMessage(0, new byte[1] { uchastok });
			cVideo.Client.Send(buffer);
			Thread.Sleep(3000);
			isVideoStarted = true;
		}
		catch (Exception value)
		{
			if (GameConfig.Instance.DebugVideo)
			{
				Console.WriteLine(value);
			}
		}
	}

	public void Stop()
	{
		if (IsConnected)
		{
			Task.Run(() =>
			{
				byte[] buffer = PrepareMessage(1);
				cVideo.Client.Send(buffer);
			});
		}
	}

	public void setLight(int idx, byte state)
	{
		Task.Run(() =>
		{
			byte[] array = new byte[5];
			BitConverter.GetBytes(idx).CopyTo(array, 0);
			array[4] = state;
			byte[] buffer = PrepareMessage(3, array);
			cVideo.Client.Send(buffer);
		});
	}

	public void setPos(float position, float speed, float speedLimit)
	{
		Task.Run(() =>
		{
			byte[] array = new byte[12];
			BitConverter.GetBytes(position).CopyTo(array, 0);
			BitConverter.GetBytes(speed).CopyTo(array, 4);
			BitConverter.GetBytes(speedLimit).CopyTo(array, 8);
			byte[] buffer = PrepareMessage(2, array);
			cVideo.Client.Send(buffer);
		});
	}

	public void setPogoda(Weather pog, bool state)
	{
		Task.Run(() =>
		{
			byte[] data = new byte[2]
			{
				(byte)pog,
				(byte)(state ? 1u : 0u)
			};
			byte[] buffer = PrepareMessage(4, data);
			cVideo.Client.Send(buffer);
		});
	}

	public void setProjector(int state)
	{
		Task.Run(() =>
		{
			byte[] data = new byte[1] { (byte)state };
			byte[] buffer = PrepareMessage(5, data);
			cVideo.Client.Send(buffer);
		});
	}

	public void setSound(int idx, bool state)
	{
		if (IsConnected)
		{
			Task.Run(() =>
			{
				byte[] array = new byte[5];
				BitConverter.GetBytes(idx).CopyTo(array, 0);
				BitConverter.GetBytes(state).CopyTo(array, 4);
				byte[] buffer = PrepareMessage(7, array);
				cVideo.Client.Send(buffer);
			});
		}
	}

	public void setZnak(byte idx, float meter)
	{
		Task.Run(() =>
		{
			byte[] array = new byte[5];
			BitConverter.GetBytes(idx).CopyTo(array, 0);
			BitConverter.GetBytes(meter).CopyTo(array, 1);
			byte[] buffer = PrepareMessage(6, array);
			cVideo.Client.Send(buffer);
		});
	}

	public void set3D(int idx, bool visual, byte anim)
	{
		Task.Run(() =>
		{
			byte[] array = new byte[6];
			BitConverter.GetBytes(idx).CopyTo(array, 0);
			array[4] = anim;
			BitConverter.GetBytes(visual).CopyTo(array, 5);
			byte[] buffer = PrepareMessage(8, array);
			cVideo.Client.Send(buffer);
		});
	}

	public void sendReglamentData(string[] msg, int ln, int right)
	{
		Task.Run(() =>
		{
			MemoryStream memoryStream = new MemoryStream();
			WriteIntToStream(memoryStream, ln);
			string[] array = msg;
			foreach (string s in array)
			{
				WriteStringToStream(memoryStream, s);
			}
			byte[] buffer = PrepareMessage(10, memoryStream.ToArray());
			cVideo.Client.Send(buffer);
		});
	}

	private void WriteStringToStream(Stream stream, string s)
	{
		byte[] bytes = BitConverter.GetBytes(s.Length);
		stream.Write(bytes, 0, bytes.Length);
		bytes = Encoding.Unicode.GetBytes(s);
		stream.Write(bytes, 0, bytes.Length);
	}

	private void WriteIntToStream(Stream stream, int val)
	{
		byte[] bytes = BitConverter.GetBytes(val);
		stream.Write(bytes, 0, bytes.Length);
	}

	public void Close()
	{
		Task.Run(() =>
		{
			cVideo.Close();
		});
	}

	private static byte[] StringToByte(string s)
	{
		return Encoding.Default.GetBytes(s);
	}
}
