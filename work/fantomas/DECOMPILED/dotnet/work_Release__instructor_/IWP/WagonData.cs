namespace IWP;

public class WagonData
{
	public WagonType wagonType;

	public float weightBrutto;

	public float length;

	public bool autoMode;

	public int padsType;

	public bool airDiffuserOn;

	public AirDiffuserMode airDiffuserMode;

	public AirDiffuserRegime airDiffuserRegime;

	public WagonData(WagonType wagonType, float weightBrutto, float length, bool autoMode, int padsType, bool airDiffuserOn, AirDiffuserMode airDiffuserMode, AirDiffuserRegime airDiffuserRegime)
	{
		this.wagonType = wagonType;
		this.weightBrutto = weightBrutto;
		this.length = length;
		this.autoMode = autoMode;
		this.padsType = padsType;
		this.airDiffuserOn = airDiffuserOn;
		this.airDiffuserMode = airDiffuserMode;
		this.airDiffuserRegime = airDiffuserRegime;
	}
}
