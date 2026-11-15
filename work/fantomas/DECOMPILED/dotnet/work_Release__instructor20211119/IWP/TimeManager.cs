using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading;

namespace IWP;

internal class TimeManager : BaseManager<TimeManager>, IUpdateable
{
	private class TimerHolder
	{
		public Timer obj;
	}

	private Stopwatch _stopwatch;

	private List<Timer> timerHolder;

	private int timerCount = 0;

	public double DeltaTime { get; private set; }

	public override void Create()
	{
		base.Create();
		_stopwatch = new Stopwatch();
		timerHolder = new List<Timer>();
		DeltaTime = 0.0;
	}

	public void Update()
	{
		if (_stopwatch.IsRunning)
		{
			_stopwatch.Stop();
			DeltaTime = (double)_stopwatch.ElapsedTicks / (double)Stopwatch.Frequency;
		}
		_stopwatch.Restart();
	}

	public void Pause()
	{
		if (_stopwatch.IsRunning)
		{
			_stopwatch.Stop();
		}
	}

	public void Start()
	{
		if (!_stopwatch.IsRunning)
		{
			_stopwatch.Restart();
		}
	}

	public void StartAfterDelay(float time, Action function)
	{
		TimerHolder timerHolder = new TimerHolder();
		int dueTime = (int)(time * 1000f);
		timerHolder.obj = new Timer((object x) =>
		{
			function();
			GC.KeepAlive(x);
		}, timerHolder, dueTime, -1);
	}
}
