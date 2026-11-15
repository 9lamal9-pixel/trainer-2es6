using System.Runtime.InteropServices;

public class TrainModuleImport
{
	[DllImport("Lib/TrainModule.dll")]
	public static extern int Init();

	[DllImport("Lib/TrainModule.dll")]
	public static extern int Update(float dt);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int CreateTrain(int trainId);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int TrainAddWagon(int trainId, int wagonId, int side);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int TrainSetMainWagon(int trainId, int wagonId);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int TrainSetPosition(int trainId, float position);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int TrainSetDrag(int trainId, float drag);

	[DllImport("Lib/TrainModule.dll")]
	public static extern float TrainGetPosition(int trainId);

	[DllImport("Lib/TrainModule.dll")]
	public static extern float TrainGetVelocity(int trainId);

	[DllImport("Lib/TrainModule.dll")]
	public static extern float TrainGetAcceleration(int trainId);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int CreateWagon(int wagonId);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int WagonSetMass(int wagonId, float mass);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int WagonSetReaction(int wagonId, float reaction);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int WagonSetLength(int wagonId, float length);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int WagonSetPull(int wagonId, float pull);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int WagonSetBraking(int wagonId, float braking);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int WagonSetSlope(int wagonId, float slope);

	[DllImport("Lib/TrainModule.dll")]
	public static extern int WagonSetCurve(int wagonId, float curve);

	[DllImport("Lib/TrainModule.dll")]
	public static extern float WagonGetPosition(int wagonId);
}
