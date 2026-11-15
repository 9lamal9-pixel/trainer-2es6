namespace IWP;

internal class PultController
{
	public PultAsyncClient m_PultClient;

	private static PultController _instance;

	public static PultController Instance
	{
		get
		{
			if (_instance == null)
			{
				_instance = new PultController();
			}
			return _instance;
		}
	}

	public bool IsActive => m_PultClient != null && m_PultClient.connected;

	public PultController()
	{
		m_PultClient = new PultAsyncClient();
	}

	public static string GetFormatString(string data, int size)
	{
		string text = data;
		while (text.Length < size)
		{
			text = "0" + text;
		}
		return text;
	}

	public void Connect()
	{
		m_PultClient.Connect(GameConfig.Instance.PultIp, GameConfig.Instance.PultPort);
	}

	public void SendCmd(byte[] cmd, int tx_buffer_lenght)
	{
		if (IsActive)
		{
			m_PultClient.Send(cmd, tx_buffer_lenght);
		}
	}

	public void Receive()
	{
		m_PultClient.Receive();
	}
}
