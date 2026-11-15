using System;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace IWP;

internal class UIManager : BaseManager<UIManager>
{
	public Action OnCloseButtonClick;

	public Action<bool> OnPauseButtonStateChanged;

	public Action<int, bool> OnBrakeSystemFaultChanged;

	public Action<int, bool> OnElectricalCircuitMalfunctionChanged;

	private PathProfileForm pathProfileForm;

	private AutoResetEvent waitHandler = new AutoResetEvent(initialState: false);

	public bool OnPauseButtonState { get; private set; }

	public RailRoadData railRoadData { get; set; }

	~UIManager()
	{
		if (this.pathProfileForm != null)
		{
			PathProfileForm pathProfileForm = this.pathProfileForm;
			pathProfileForm.OnCloseButtonClick = (Action)Delegate.Remove(pathProfileForm.OnCloseButtonClick, new Action(OnCloseButtonClickHandler));
			PathProfileForm pathProfileForm2 = this.pathProfileForm;
			pathProfileForm2.OnCloseButtonClick = (Action)Delegate.Remove(pathProfileForm2.OnCloseButtonClick, new Action(OnLoadFormHandler));
			PathProfileForm pathProfileForm3 = this.pathProfileForm;
			pathProfileForm3.OnBrakeSystemFaultChanged = (Action<int, bool>)Delegate.Remove(pathProfileForm3.OnBrakeSystemFaultChanged, new Action<int, bool>(OnBrakeSystemFaultChangedHandler));
			PathProfileForm pathProfileForm4 = this.pathProfileForm;
			pathProfileForm4.OnElectricalCircuitMalfunctionChanged = (Action<int, bool>)Delegate.Remove(pathProfileForm4.OnElectricalCircuitMalfunctionChanged, new Action<int, bool>(OnElectricalCircuitMalfunctionChangedHandler));
			PathProfileForm pathProfileForm5 = this.pathProfileForm;
			pathProfileForm5.OnPauseButtonChanged = (Action<bool>)Delegate.Remove(pathProfileForm5.OnPauseButtonChanged, new Action<bool>(OnPauseButtonChangedHandler));
		}
	}

	public override void PostInit()
	{
		base.PostInit();
		Task.Run(() =>
		{
			Application.EnableVisualStyles();
			Application.SetCompatibleTextRenderingDefault(defaultValue: false);
			this.pathProfileForm = new PathProfileForm(railRoadData);
			PathProfileForm pathProfileForm = this.pathProfileForm;
			pathProfileForm.OnCloseButtonClick = (Action)Delegate.Combine(pathProfileForm.OnCloseButtonClick, new Action(OnCloseButtonClickHandler));
			PathProfileForm pathProfileForm2 = this.pathProfileForm;
			pathProfileForm2.OnLoadForm = (Action)Delegate.Combine(pathProfileForm2.OnLoadForm, new Action(OnLoadFormHandler));
			PathProfileForm pathProfileForm3 = this.pathProfileForm;
			pathProfileForm3.OnBrakeSystemFaultChanged = (Action<int, bool>)Delegate.Combine(pathProfileForm3.OnBrakeSystemFaultChanged, new Action<int, bool>(OnBrakeSystemFaultChangedHandler));
			PathProfileForm pathProfileForm4 = this.pathProfileForm;
			pathProfileForm4.OnElectricalCircuitMalfunctionChanged = (Action<int, bool>)Delegate.Combine(pathProfileForm4.OnElectricalCircuitMalfunctionChanged, new Action<int, bool>(OnElectricalCircuitMalfunctionChangedHandler));
			PathProfileForm pathProfileForm5 = this.pathProfileForm;
			pathProfileForm5.OnPauseButtonChanged = (Action<bool>)Delegate.Combine(pathProfileForm5.OnPauseButtonChanged, new Action<bool>(OnPauseButtonChangedHandler));
			Application.Run(this.pathProfileForm);
		});
		waitHandler.WaitOne();
	}

	public void SetTrainPosition(double coordinate)
	{
		if (pathProfileForm != null)
		{
			pathProfileForm.trainPosition = coordinate * 0.001;
		}
	}

	public void SetTrafficLight(int index, int lightCode)
	{
		if (pathProfileForm != null)
		{
			pathProfileForm.SetTrafficLight(index, lightCode);
		}
	}

	private void OnCloseButtonClickHandler()
	{
		OnCloseButtonClick();
	}

	private void OnPauseButtonChangedHandler(bool state)
	{
		OnPauseButtonState = state;
		OnPauseButtonStateChanged(state);
		if (state)
		{
			BaseManager<TimeManager>.instance.Pause();
			BaseManager<ClientVideoManager>.instance.MuteAllSounds();
		}
		else
		{
			BaseManager<TimeManager>.instance.Start();
		}
	}

	private void OnLoadFormHandler()
	{
		waitHandler.Set();
	}

	private void OnBrakeSystemFaultChangedHandler(int index, bool state)
	{
		if (OnBrakeSystemFaultChanged != null)
		{
			OnBrakeSystemFaultChanged(index, state);
		}
	}

	private void OnElectricalCircuitMalfunctionChangedHandler(int index, bool state)
	{
		if (OnElectricalCircuitMalfunctionChanged != null)
		{
			OnElectricalCircuitMalfunctionChanged(index, state);
		}
	}
}
