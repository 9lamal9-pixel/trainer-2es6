using System.Linq;

namespace IWP;

internal class ALSNController : BaseController<ALSNController>
{
	private Train playerTrain;

	private RailRoadData railRoadData;

	private UIManager uiManager;

	public override void Init()
	{
		base.Init();
		playerTrain = BaseManager<GameManager>.instance.playerTrain;
		railRoadData = BaseManager<GameManager>.instance.GetRailRoadController<BaseRailRoadController>().GetRailRoadData();
		uiManager = BaseManager<UIManager>.instance;
		playerTrain.OnTrafficLightIndexChanged.AddListener(this, (int x) =>
		{
			if (playerTrain.Speed >= 0f)
			{
				if (x > 0)
				{
					if (railRoadData.lights[x - 1].lightCombinations.Contains(1))
					{
						railRoadData.SetTrafficLightCode(x - 1, 1);
						uiManager.SetTrafficLight(x - 1, 1);
					}
					if (x > 1)
					{
						if (railRoadData.lights[x - 2].lightCombinations.Contains(2))
						{
							railRoadData.SetTrafficLightCode(x - 2, 2);
							uiManager.SetTrafficLight(x - 2, 2);
						}
						if (x > 2 && railRoadData.lights[x - 3].lightCombinations.Contains(0))
						{
							railRoadData.SetTrafficLightCode(x - 3, 0);
							uiManager.SetTrafficLight(x - 3, 0);
						}
					}
				}
			}
			else
			{
				if (railRoadData.lights[x].light == 1)
				{
					railRoadData.SetTrafficLightCode(x, 0);
					uiManager.SetTrafficLight(x, 0);
				}
				if (x > 0)
				{
					if (railRoadData.lights[x - 1].light == 2)
					{
						railRoadData.SetTrafficLightCode(x - 1, 1);
						uiManager.SetTrafficLight(x - 1, 1);
					}
					if (x > 1 && railRoadData.lights[x - 2].light == 0)
					{
						railRoadData.SetTrafficLightCode(x - 2, 2);
						uiManager.SetTrafficLight(x - 2, 2);
					}
				}
			}
		});
	}
}
