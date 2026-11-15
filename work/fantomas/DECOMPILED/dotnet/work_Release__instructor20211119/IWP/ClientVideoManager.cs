using System.Collections.Generic;
using System.Threading;

namespace IWP;

internal class ClientVideoManager : BaseManager<ClientVideoManager>, IUpdateable
{
	private class TrafficLightVideoData
	{
		private int _trafficLightId;

		private byte _trafficLightState;

		public bool isDataUpdated { get; set; }

		public int trafficLightId
		{
			get
			{
				return _trafficLightId;
			}
			set
			{
				if (value != _trafficLightId)
				{
					isDataUpdated = true;
				}
				_trafficLightId = value;
			}
		}

		public byte trafficLightState
		{
			get
			{
				return _trafficLightState;
			}
			set
			{
				if (value != _trafficLightState)
				{
					isDataUpdated = true;
				}
				_trafficLightState = value;
			}
		}
	}

	private class TrainVideoData
	{
		private float _trainPosition;

		private float _trainSpeed;

		private float _speedLimit;

		public bool isDataUpdated { get; set; }

		public float trainPosition
		{
			get
			{
				return _trainPosition;
			}
			set
			{
				if (value != _trainPosition)
				{
					isDataUpdated = true;
				}
				_trainPosition = value;
			}
		}

		public float trainSpeed
		{
			get
			{
				return _trainSpeed;
			}
			set
			{
				if (value != _trainSpeed)
				{
					isDataUpdated = true;
				}
				_trainSpeed = value;
			}
		}

		public float speedLimit
		{
			get
			{
				return _speedLimit;
			}
			set
			{
				if (value != _speedLimit)
				{
					isDataUpdated = true;
				}
				_speedLimit = value;
			}
		}
	}

	private class WeatherVideoData
	{
		private Weather _weather;

		private bool _weatherState;

		public bool isDataUpdated { get; set; }

		public Weather weather
		{
			get
			{
				return _weather;
			}
			set
			{
				if (value != _weather)
				{
					isDataUpdated = true;
				}
				_weather = value;
			}
		}

		public bool weatherState
		{
			get
			{
				return _weatherState;
			}
			set
			{
				if (value != _weatherState)
				{
					isDataUpdated = true;
				}
				_weatherState = value;
			}
		}
	}

	private class SpotlightVideoData
	{
		private int _spotlightState;

		public bool isDataUpdated { get; set; }

		public int spotlightState
		{
			get
			{
				return _spotlightState;
			}
			set
			{
				if (value != _spotlightState)
				{
					isDataUpdated = true;
				}
				_spotlightState = value;
			}
		}
	}

	private class SoundVideoData
	{
		private int _soundId;

		private bool _soundState;

		public bool isDataUpdated { get; set; }

		public int soundId
		{
			get
			{
				return _soundId;
			}
			set
			{
				if (value != _soundId)
				{
					isDataUpdated = true;
				}
				_soundId = value;
			}
		}

		public bool soundState
		{
			get
			{
				return _soundState;
			}
			set
			{
				if (value != _soundState)
				{
					isDataUpdated = true;
				}
				_soundState = value;
			}
		}
	}

	private class SignVideoData
	{
		private byte _signId;

		private float _signCoordinate;

		public bool isDataUpdated { get; set; }

		public byte signId
		{
			get
			{
				return _signId;
			}
			set
			{
				if (value != _signId)
				{
					isDataUpdated = true;
				}
				_signId = value;
			}
		}

		public float signCoordinate
		{
			get
			{
				return _signCoordinate;
			}
			set
			{
				if (value != _signCoordinate)
				{
					isDataUpdated = true;
				}
				_signCoordinate = value;
			}
		}
	}

	private class ObjectVideoData
	{
		private int _objectId;

		private bool _objectState;

		private byte _objectAnimation;

		public bool isDataUpdated { get; set; }

		public int objectId
		{
			get
			{
				return _objectId;
			}
			set
			{
				if (value != _objectId)
				{
					isDataUpdated = true;
				}
				_objectId = value;
			}
		}

		public bool objectState
		{
			get
			{
				return _objectState;
			}
			set
			{
				if (value != _objectState)
				{
					isDataUpdated = true;
				}
				_objectState = value;
			}
		}

		public byte objectAnimation
		{
			get
			{
				return _objectAnimation;
			}
			set
			{
				if (value != _objectAnimation)
				{
					isDataUpdated = true;
				}
				_objectAnimation = value;
			}
		}
	}

	private class ReglamentData
	{
		private List<string> _message;

		private int _line;

		private int _rightChoice;

		public bool isDataUpdated1 { get; set; }

		public bool isDataUpdated2 { get; set; }

		public List<string> message
		{
			get
			{
				return _message;
			}
			set
			{
				if (_message != null && value.ToArray() != _message.ToArray())
				{
					isDataUpdated1 = true;
				}
				_message = value;
			}
		}

		public int line
		{
			get
			{
				return _line;
			}
			set
			{
				if (value != _line)
				{
					isDataUpdated2 = true;
				}
				_line = value;
			}
		}

		public int rightChoice
		{
			get
			{
				return _rightChoice;
			}
			set
			{
				_rightChoice = value;
			}
		}
	}

	private ClientVideo clientVideo;

	private double targetDeltaTime = 0.05;

	private double timeToNextRequest = 0.05;

	private TrafficLightVideoData trafficLightData;

	private TrainVideoData trainData;

	private WeatherVideoData weatherData;

	private SpotlightVideoData spotlightData;

	private SoundVideoData soundData;

	private SignVideoData signData;

	private ObjectVideoData objectData;

	private ReglamentData reglamentData;

	private Queue<TrafficLightVideoData> trafficLightSendQueue;

	private Queue<SignVideoData> signSendQueue;

	private Queue<SoundVideoData> soundSendQueue;

	private Queue<TrainVideoData> trainDataSendQueue;

	~ClientVideoManager()
	{
		clientVideo.Stop();
	}

	public override void Create()
	{
		base.Create();
		clientVideo = new ClientVideo(GameConfig.Instance.VideoIp, GameConfig.Instance.VideoPort);
		trafficLightData = new TrafficLightVideoData();
		trainData = new TrainVideoData();
		weatherData = new WeatherVideoData();
		spotlightData = new SpotlightVideoData();
		soundData = new SoundVideoData();
		signData = new SignVideoData();
		objectData = new ObjectVideoData();
		reglamentData = new ReglamentData();
		trafficLightSendQueue = new Queue<TrafficLightVideoData>();
		signSendQueue = new Queue<SignVideoData>();
		soundSendQueue = new Queue<SoundVideoData>();
		trainDataSendQueue = new Queue<TrainVideoData>();
	}

	public override void Destroy()
	{
		base.Destroy();
		if (GameConfig.Instance.IsActiveVideo)
		{
			clientVideo.Stop();
		}
	}

	public override void PostInit()
	{
		base.PostInit();
		clientVideo.Connect();
		byte uchastok = (byte)RoadConfig.instance.RoadIndex;
		clientVideo.Start(uchastok);
		Thread.Sleep(5000);
	}

	public void Update()
	{
		if (!GameConfig.Instance.IsActiveVideo)
		{
			return;
		}
		timeToNextRequest -= BaseManager<TimeManager>.instance.DeltaTime;
		if (!(timeToNextRequest < 0.0))
		{
			return;
		}
		timeToNextRequest += targetDeltaTime;
		if (!clientVideo.IsConnected)
		{
			clientVideo.Connect();
			return;
		}
		if (!clientVideo.isVideoStarted)
		{
			clientVideo.Start((byte)RoadConfig.instance.RoadIndex);
			return;
		}
		if (trafficLightData.isDataUpdated)
		{
			clientVideo.setLight(trafficLightData.trafficLightId, trafficLightData.trafficLightState);
			trafficLightData.isDataUpdated = false;
		}
		if (trainData.isDataUpdated)
		{
			clientVideo.setPos(trainData.trainPosition, trainData.trainSpeed, trainData.speedLimit);
			trainData.isDataUpdated = false;
		}
		if (weatherData.isDataUpdated)
		{
			clientVideo.setPogoda(weatherData.weather, weatherData.weatherState);
			weatherData.isDataUpdated = false;
		}
		if (spotlightData.isDataUpdated)
		{
			clientVideo.setProjector(spotlightData.spotlightState);
			spotlightData.isDataUpdated = false;
		}
		if (soundData.isDataUpdated)
		{
			clientVideo.setSound(soundData.soundId, soundData.soundState);
			soundData.isDataUpdated = false;
		}
		if (signData.isDataUpdated)
		{
			clientVideo.setZnak(signData.signId, signData.signCoordinate);
			signData.isDataUpdated = false;
		}
		if (objectData.isDataUpdated)
		{
			clientVideo.set3D(objectData.objectId, objectData.objectState, objectData.objectAnimation);
		}
		if (reglamentData.isDataUpdated1 || reglamentData.isDataUpdated2)
		{
			reglamentData.isDataUpdated1 = false;
			reglamentData.isDataUpdated2 = false;
			clientVideo.sendReglamentData(reglamentData.message.ToArray(), reglamentData.line, reglamentData.rightChoice);
			objectData.isDataUpdated = false;
		}
		if (trafficLightSendQueue.Count > 0)
		{
			TrafficLightVideoData trafficLightVideoData = trafficLightSendQueue.Dequeue();
			clientVideo.setLight(trafficLightVideoData.trafficLightId, trafficLightVideoData.trafficLightState);
		}
		if (signSendQueue.Count > 0)
		{
			SignVideoData signVideoData = signSendQueue.Dequeue();
			clientVideo.setZnak(signVideoData.signId, signVideoData.signCoordinate);
		}
		if (soundSendQueue.Count > 0)
		{
			SoundVideoData soundVideoData = soundSendQueue.Dequeue();
			clientVideo.setSound(soundVideoData.soundId, soundVideoData.soundState);
		}
		if (trainDataSendQueue.Count > 0)
		{
			TrainVideoData trainVideoData = trainDataSendQueue.Dequeue();
			clientVideo.setPos(trainVideoData.trainPosition, trainVideoData.trainSpeed, trainVideoData.speedLimit);
		}
	}

	public void SendTrafficLightCode(int trafficLightId, byte state)
	{
		trafficLightData.trafficLightId = trafficLightId;
		trafficLightData.trafficLightState = state;
	}

	public void SendTrafficLightCodeByQueue(int trafficLightId, byte state)
	{
		TrafficLightVideoData trafficLightVideoData = new TrafficLightVideoData();
		trafficLightVideoData.trafficLightId = trafficLightId;
		trafficLightVideoData.trafficLightState = state;
		trafficLightSendQueue.Enqueue(trafficLightVideoData);
	}

	public void SendTrainPosition(float position, float speed, float speedLimit)
	{
		trainData.trainPosition = position;
		trainData.trainSpeed = speed;
		trainData.speedLimit = speedLimit;
	}

	public void SendTrainPositionByQueue(float position, float speed, float speedLimit)
	{
		TrainVideoData trainVideoData = new TrainVideoData();
		trainVideoData.trainPosition = position;
		trainVideoData.trainSpeed = speed;
		trainVideoData.speedLimit = speedLimit;
		trainDataSendQueue.Enqueue(trainVideoData);
	}

	public void SendWeather(Weather weather, bool state)
	{
		weatherData.weather = weather;
		weatherData.weatherState = state;
	}

	public void SendSpotlightState(int state)
	{
		spotlightData.spotlightState = state;
	}

	public void SendPlaySound(int soundId, bool state)
	{
		soundData.soundId = soundId;
		soundData.soundState = state;
	}

	public void SendPlaySoundByQueue(int soundId, bool state)
	{
		SoundVideoData soundVideoData = new SoundVideoData();
		soundVideoData.soundId = soundId;
		soundVideoData.soundState = state;
		soundSendQueue.Enqueue(soundVideoData);
	}

	public void SendSign(byte signId, float signCoordinate)
	{
		signData.signId = signId;
		signData.signCoordinate = signCoordinate;
	}

	public void SendSignByQueue(byte signId, float signCoordinate)
	{
		SignVideoData signVideoData = new SignVideoData();
		signVideoData.signId = signId;
		signVideoData.signCoordinate = signCoordinate;
		signSendQueue.Enqueue(signVideoData);
	}

	public void Send3DObjectState(int objectId, bool state, byte animation)
	{
		objectData.objectId = objectId;
		objectData.objectState = state;
		objectData.objectAnimation = animation;
	}

	public void SendReglamentMessages(List<string> msg, int line, int rC)
	{
		reglamentData.message = msg;
		reglamentData.line = line;
		reglamentData.rightChoice = rC;
	}

	public void MuteAllSounds()
	{
		if (!clientVideo.IsConnected)
		{
			clientVideo.Connect();
		}
		while (soundSendQueue.Count > 0)
		{
			SoundVideoData soundVideoData = soundSendQueue.Dequeue();
			clientVideo.setSound(soundVideoData.soundId, soundVideoData.soundState);
		}
	}
}
