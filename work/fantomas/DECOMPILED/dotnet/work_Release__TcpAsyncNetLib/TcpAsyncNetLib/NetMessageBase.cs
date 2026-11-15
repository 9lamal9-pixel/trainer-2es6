namespace TcpAsyncNetLib;

public abstract class NetMessageBase
{
	public short msgType;

	public abstract void Serialize(NetworkWriter writer);

	public abstract void Deserialize(NetworkReader reader);
}
