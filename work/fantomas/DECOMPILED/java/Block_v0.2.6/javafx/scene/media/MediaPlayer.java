/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import com.sun.javafx.tk.TKPulseListener;
import com.sun.javafx.tk.Toolkit;
import com.sun.media.jfxmedia.MediaManager;
import com.sun.media.jfxmedia.control.VideoDataBuffer;
import com.sun.media.jfxmedia.events.AudioSpectrumEvent;
import com.sun.media.jfxmedia.events.BufferListener;
import com.sun.media.jfxmedia.events.BufferProgressEvent;
import com.sun.media.jfxmedia.events.MarkerEvent;
import com.sun.media.jfxmedia.events.MarkerListener;
import com.sun.media.jfxmedia.events.MediaErrorListener;
import com.sun.media.jfxmedia.events.NewFrameEvent;
import com.sun.media.jfxmedia.events.PlayerStateEvent;
import com.sun.media.jfxmedia.events.PlayerStateListener;
import com.sun.media.jfxmedia.events.PlayerTimeListener;
import com.sun.media.jfxmedia.events.VideoRendererListener;
import com.sun.media.jfxmedia.events.VideoTrackSizeListener;
import com.sun.media.jfxmedia.locator.Locator;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaMarkerEvent;
import javafx.scene.media.MediaPlayerShutdownHook;
import javafx.scene.media.MediaTimerTask;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.util.Pair;

public final class MediaPlayer {
    public static final int INDEFINITE = -1;
    private static final double RATE_MIN = 0.0;
    private static final double RATE_MAX = 8.0;
    private static final int AUDIOSPECTRUM_THRESHOLD_MAX = 0;
    private static final double AUDIOSPECTRUM_INTERVAL_MIN = 1.0E-9;
    private static final int AUDIOSPECTRUM_NUMBANDS_MIN = 2;
    private com.sun.media.jfxmedia.MediaPlayer jfxPlayer;
    private MapChangeListener<String, Duration> markerMapListener = null;
    private MarkerListener markerEventListener = null;
    private PlayerStateListener stateListener = null;
    private PlayerTimeListener timeListener = null;
    private VideoTrackSizeListener sizeListener = null;
    private MediaErrorListener errorListener = null;
    private BufferListener bufferListener = null;
    private com.sun.media.jfxmedia.events.AudioSpectrumListener spectrumListener = null;
    private RendererListener rendererListener = null;
    private boolean rateChangeRequested = false;
    private boolean volumeChangeRequested = false;
    private boolean balanceChangeRequested = false;
    private boolean startTimeChangeRequested = false;
    private boolean stopTimeChangeRequested = false;
    private boolean muteChangeRequested = false;
    private boolean playRequested = false;
    private boolean audioSpectrumNumBandsChangeRequested = false;
    private boolean audioSpectrumIntervalChangeRequested = false;
    private boolean audioSpectrumThresholdChangeRequested = false;
    private boolean audioSpectrumEnabledChangeRequested = false;
    private MediaTimerTask mediaTimerTask = null;
    private double prevTimeMs = -1.0;
    private boolean isUpdateTimeEnabled = false;
    private BufferProgressEvent lastBufferEvent = null;
    private Duration startTimeAtStop = null;
    private boolean isEOS = false;
    private final Object disposeLock = new Object();
    private static final int DEFAULT_SPECTRUM_BAND_COUNT = 128;
    private static final double DEFAULT_SPECTRUM_INTERVAL = 0.1;
    private static final int DEFAULT_SPECTRUM_THRESHOLD = -60;
    private final Set<WeakReference<MediaView>> viewRefs = new HashSet<WeakReference<MediaView>>();
    private AudioEqualizer audioEqualizer;
    private ReadOnlyObjectWrapper<MediaException> error;
    private ObjectProperty<Runnable> onError;
    private Media media;
    private BooleanProperty autoPlay;
    private boolean playerReady;
    private DoubleProperty rate;
    private ReadOnlyDoubleWrapper currentRate;
    private DoubleProperty volume;
    private DoubleProperty balance;
    private ObjectProperty<Duration> startTime;
    private ObjectProperty<Duration> stopTime;
    private ReadOnlyObjectWrapper<Duration> cycleDuration;
    private ReadOnlyObjectWrapper<Duration> totalDuration;
    private ReadOnlyObjectWrapper<Duration> currentTime;
    private ReadOnlyObjectWrapper<Status> status;
    private ReadOnlyObjectWrapper<Duration> bufferProgressTime;
    private IntegerProperty cycleCount;
    private ReadOnlyIntegerWrapper currentCount;
    private BooleanProperty mute;
    private ObjectProperty<EventHandler<MediaMarkerEvent>> onMarker;
    private ObjectProperty<Runnable> onEndOfMedia;
    private ObjectProperty<Runnable> onReady;
    private ObjectProperty<Runnable> onPlaying;
    private ObjectProperty<Runnable> onPaused;
    private ObjectProperty<Runnable> onStopped;
    private ObjectProperty<Runnable> onHalted;
    private ObjectProperty<Runnable> onRepeat;
    private ObjectProperty<Runnable> onStalled;
    private IntegerProperty audioSpectrumNumBands;
    private DoubleProperty audioSpectrumInterval;
    private IntegerProperty audioSpectrumThreshold;
    private ObjectProperty<AudioSpectrumListener> audioSpectrumListener;
    private final Object renderLock = new Object();
    private VideoDataBuffer currentRenderFrame;
    private VideoDataBuffer nextRenderFrame;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    com.sun.media.jfxmedia.MediaPlayer retrieveJfxPlayer() {
        Object object = this.disposeLock;
        synchronized (object) {
            return this.jfxPlayer;
        }
    }

    private static double clamp(double d, double d2, double d3) {
        if (d2 != Double.MIN_VALUE && d < d2) {
            return d2;
        }
        if (d3 != Double.MAX_VALUE && d > d3) {
            return d3;
        }
        return d;
    }

    private static int clamp(int n, int n2, int n3) {
        if (n2 != Integer.MIN_VALUE && n < n2) {
            return n2;
        }
        if (n3 != Integer.MAX_VALUE && n > n3) {
            return n3;
        }
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final AudioEqualizer getAudioEqualizer() {
        Object object = this.disposeLock;
        synchronized (object) {
            if (this.getStatus() == Status.DISPOSED) {
                return null;
            }
            if (this.audioEqualizer == null) {
                this.audioEqualizer = new AudioEqualizer();
                if (this.jfxPlayer != null) {
                    this.audioEqualizer.setAudioEqualizer(this.jfxPlayer.getEqualizer());
                }
                this.audioEqualizer.setEnabled(true);
            }
            return this.audioEqualizer;
        }
    }

    public MediaPlayer(@NamedArg(value="media") Media media) {
        if (null == media) {
            throw new NullPointerException("media == null!");
        }
        this.media = media;
        this.errorListener = new _MediaErrorListener();
        MediaManager.addMediaErrorListener(this.errorListener);
        try {
            Locator locator = media.retrieveJfxLocator();
            if (locator.canBlock()) {
                InitMediaPlayer initMediaPlayer = new InitMediaPlayer();
                Thread thread = new Thread(initMediaPlayer);
                thread.setDaemon(true);
                thread.start();
            } else {
                this.init();
            }
        }
        catch (com.sun.media.jfxmedia.MediaException mediaException) {
            throw MediaException.exceptionToMediaException(mediaException);
        }
        catch (MediaException mediaException) {
            throw mediaException;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void registerListeners() {
        Object object = this.disposeLock;
        synchronized (object) {
            if (this.getStatus() == Status.DISPOSED) {
                return;
            }
            if (this.jfxPlayer != null) {
                MediaManager.registerMediaPlayerForDispose(this, this.jfxPlayer);
                this.jfxPlayer.addMediaErrorListener(this.errorListener);
                this.jfxPlayer.addMediaTimeListener(this.timeListener);
                this.jfxPlayer.addVideoTrackSizeListener(this.sizeListener);
                this.jfxPlayer.addBufferListener(this.bufferListener);
                this.jfxPlayer.addMarkerListener(this.markerEventListener);
                this.jfxPlayer.addAudioSpectrumListener(this.spectrumListener);
                this.jfxPlayer.getVideoRenderControl().addVideoRendererListener(this.rendererListener);
                this.jfxPlayer.addMediaPlayerListener(this.stateListener);
            }
            if (null != this.rendererListener) {
                Toolkit.getToolkit().addStageTkPulseListener(this.rendererListener);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void init() throws MediaException {
        try {
            Locator locator = this.media.retrieveJfxLocator();
            locator.waitForReadySignal();
            Object object = this.disposeLock;
            synchronized (object) {
                if (this.getStatus() == Status.DISPOSED) {
                    return;
                }
                this.jfxPlayer = MediaManager.getPlayer(locator);
                if (this.jfxPlayer != null) {
                    MediaPlayerShutdownHook.addMediaPlayer(this);
                    this.jfxPlayer.setBalance((float)this.getBalance());
                    this.jfxPlayer.setMute(this.isMute());
                    this.jfxPlayer.setVolume((float)this.getVolume());
                    this.sizeListener = new _VideoTrackSizeListener();
                    this.stateListener = new _PlayerStateListener();
                    this.timeListener = new _PlayerTimeListener();
                    this.bufferListener = new _BufferListener();
                    this.markerEventListener = new _MarkerListener();
                    this.spectrumListener = new _SpectrumListener();
                    this.rendererListener = new RendererListener();
                }
                this.markerMapListener = new MarkerMapChangeListener();
                ObservableMap<String, Duration> observableMap = this.media.getMarkers();
                observableMap.addListener(this.markerMapListener);
                com.sun.media.jfxmedia.Media media = this.jfxPlayer.getMedia();
                for (Map.Entry entry : observableMap.entrySet()) {
                    double d;
                    Duration duration;
                    String string = (String)entry.getKey();
                    if (string == null || (duration = (Duration)entry.getValue()) == null || !((d = duration.toMillis()) >= 0.0)) continue;
                    media.addMarker(string, d / 1000.0);
                }
            }
        }
        catch (com.sun.media.jfxmedia.MediaException mediaException) {
            throw MediaException.exceptionToMediaException(mediaException);
        }
        Platform.runLater(() -> this.registerListeners());
    }

    private void setError(MediaException mediaException) {
        if (this.getError() == null) {
            this.errorPropertyImpl().set(mediaException);
        }
    }

    public final MediaException getError() {
        return this.error == null ? null : (MediaException)this.error.get();
    }

    public ReadOnlyObjectProperty<MediaException> errorProperty() {
        return this.errorPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<MediaException> errorPropertyImpl() {
        if (this.error == null) {
            this.error = new ReadOnlyObjectWrapper<MediaException>(){

                @Override
                protected void invalidated() {
                    if (MediaPlayer.this.getOnError() != null) {
                        Platform.runLater(MediaPlayer.this.getOnError());
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "error";
                }
            };
        }
        return this.error;
    }

    public final void setOnError(Runnable runnable) {
        this.onErrorProperty().set(runnable);
    }

    public final Runnable getOnError() {
        return this.onError == null ? null : (Runnable)this.onError.get();
    }

    public ObjectProperty<Runnable> onErrorProperty() {
        if (this.onError == null) {
            this.onError = new ObjectPropertyBase<Runnable>(){

                @Override
                protected void invalidated() {
                    if (this.get() != null && MediaPlayer.this.getError() != null) {
                        Platform.runLater((Runnable)this.get());
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "onError";
                }
            };
        }
        return this.onError;
    }

    public final Media getMedia() {
        return this.media;
    }

    public final void setAutoPlay(boolean bl) {
        this.autoPlayProperty().set(bl);
    }

    public final boolean isAutoPlay() {
        return this.autoPlay == null ? false : this.autoPlay.get();
    }

    public BooleanProperty autoPlayProperty() {
        if (this.autoPlay == null) {
            this.autoPlay = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    if (MediaPlayer.this.autoPlay.get()) {
                        MediaPlayer.this.play();
                    } else {
                        MediaPlayer.this.playRequested = false;
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "autoPlay";
                }
            };
        }
        return this.autoPlay;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void play() {
        Object object = this.disposeLock;
        synchronized (object) {
            if (this.getStatus() != Status.DISPOSED) {
                if (this.playerReady) {
                    this.jfxPlayer.play();
                } else {
                    this.playRequested = true;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void pause() {
        Object object = this.disposeLock;
        synchronized (object) {
            if (this.getStatus() != Status.DISPOSED) {
                if (this.playerReady) {
                    this.jfxPlayer.pause();
                } else {
                    this.playRequested = false;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void stop() {
        Object object = this.disposeLock;
        synchronized (object) {
            if (this.getStatus() != Status.DISPOSED) {
                if (this.playerReady) {
                    this.jfxPlayer.stop();
                    this.setCurrentCount(0);
                    this.destroyMediaTimer();
                } else {
                    this.playRequested = false;
                }
            }
        }
    }

    public final void setRate(double d) {
        this.rateProperty().set(d);
    }

    public final double getRate() {
        return this.rate == null ? 1.0 : this.rate.get();
    }

    public DoubleProperty rateProperty() {
        if (this.rate == null) {
            this.rate = new DoublePropertyBase(1.0){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                if (MediaPlayer.this.jfxPlayer.getDuration() != Double.POSITIVE_INFINITY) {
                                    MediaPlayer.this.jfxPlayer.setRate((float)MediaPlayer.clamp(MediaPlayer.this.rate.get(), 0.0, 8.0));
                                }
                            } else {
                                MediaPlayer.this.rateChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "rate";
                }
            };
        }
        return this.rate;
    }

    private void setCurrentRate(double d) {
        this.currentRatePropertyImpl().set(d);
    }

    public final double getCurrentRate() {
        return this.currentRate == null ? 0.0 : this.currentRate.get();
    }

    public ReadOnlyDoubleProperty currentRateProperty() {
        return this.currentRatePropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyDoubleWrapper currentRatePropertyImpl() {
        if (this.currentRate == null) {
            this.currentRate = new ReadOnlyDoubleWrapper(this, "currentRate");
        }
        return this.currentRate;
    }

    public final void setVolume(double d) {
        this.volumeProperty().set(d);
    }

    public final double getVolume() {
        return this.volume == null ? 1.0 : this.volume.get();
    }

    public DoubleProperty volumeProperty() {
        if (this.volume == null) {
            this.volume = new DoublePropertyBase(1.0){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.jfxPlayer.setVolume((float)MediaPlayer.clamp(MediaPlayer.this.volume.get(), 0.0, 1.0));
                            } else {
                                MediaPlayer.this.volumeChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "volume";
                }
            };
        }
        return this.volume;
    }

    public final void setBalance(double d) {
        this.balanceProperty().set(d);
    }

    public final double getBalance() {
        return this.balance == null ? 0.0 : this.balance.get();
    }

    public DoubleProperty balanceProperty() {
        if (this.balance == null) {
            this.balance = new DoublePropertyBase(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.jfxPlayer.setBalance((float)MediaPlayer.clamp(MediaPlayer.this.balance.get(), -1.0, 1.0));
                            } else {
                                MediaPlayer.this.balanceChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "balance";
                }
            };
        }
        return this.balance;
    }

    private double[] calculateStartStopTimes(Duration duration, Duration duration2) {
        double d;
        double d2 = duration == null || duration.lessThan(Duration.ZERO) || duration.equals(Duration.UNKNOWN) ? 0.0 : (duration.equals(Duration.INDEFINITE) ? Double.MAX_VALUE : duration.toMillis() / 1000.0);
        double d3 = duration2 == null || duration2.equals(Duration.UNKNOWN) || duration2.equals(Duration.INDEFINITE) ? Double.MAX_VALUE : (duration2.lessThan(Duration.ZERO) ? 0.0 : duration2.toMillis() / 1000.0);
        Duration duration3 = this.media.getDuration();
        double d4 = duration3 == Duration.UNKNOWN ? Double.MAX_VALUE : duration3.toMillis() / 1000.0;
        double d5 = MediaPlayer.clamp(d2, 0.0, d4);
        if (d5 > (d = MediaPlayer.clamp(d3, 0.0, d4))) {
            d = d5;
        }
        return new double[]{d5, d};
    }

    private void setStartStopTimes(Duration duration, boolean bl, Duration duration2, boolean bl2) {
        if (this.jfxPlayer.getDuration() == Double.POSITIVE_INFINITY) {
            return;
        }
        double[] dArray = this.calculateStartStopTimes(duration, duration2);
        if (bl) {
            this.jfxPlayer.setStartTime(dArray[0]);
            if (this.getStatus() == Status.READY || this.getStatus() == Status.PAUSED) {
                Platform.runLater(() -> this.setCurrentTime(this.getStartTime()));
            }
        }
        if (bl2) {
            this.jfxPlayer.setStopTime(dArray[1]);
        }
    }

    public final void setStartTime(Duration duration) {
        this.startTimeProperty().set(duration);
    }

    public final Duration getStartTime() {
        return this.startTime == null ? Duration.ZERO : (Duration)this.startTime.get();
    }

    public ObjectProperty<Duration> startTimeProperty() {
        if (this.startTime == null) {
            this.startTime = new ObjectPropertyBase<Duration>(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.setStartStopTimes((Duration)MediaPlayer.this.startTime.get(), true, MediaPlayer.this.getStopTime(), false);
                            } else {
                                MediaPlayer.this.startTimeChangeRequested = true;
                            }
                            MediaPlayer.this.calculateCycleDuration();
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "startTime";
                }
            };
        }
        return this.startTime;
    }

    public final void setStopTime(Duration duration) {
        this.stopTimeProperty().set(duration);
    }

    public final Duration getStopTime() {
        return this.stopTime == null ? this.media.getDuration() : (Duration)this.stopTime.get();
    }

    public ObjectProperty<Duration> stopTimeProperty() {
        if (this.stopTime == null) {
            this.stopTime = new ObjectPropertyBase<Duration>(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.setStartStopTimes(MediaPlayer.this.getStartTime(), false, (Duration)MediaPlayer.this.stopTime.get(), true);
                            } else {
                                MediaPlayer.this.stopTimeChangeRequested = true;
                            }
                            MediaPlayer.this.calculateCycleDuration();
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "stopTime";
                }
            };
        }
        return this.stopTime;
    }

    private void setCycleDuration(Duration duration) {
        this.cycleDurationPropertyImpl().set(duration);
    }

    public final Duration getCycleDuration() {
        return this.cycleDuration == null ? Duration.UNKNOWN : (Duration)this.cycleDuration.get();
    }

    public ReadOnlyObjectProperty<Duration> cycleDurationProperty() {
        return this.cycleDurationPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Duration> cycleDurationPropertyImpl() {
        if (this.cycleDuration == null) {
            this.cycleDuration = new ReadOnlyObjectWrapper(this, "cycleDuration");
        }
        return this.cycleDuration;
    }

    private void calculateCycleDuration() {
        Duration duration = this.media.getDuration();
        Duration duration2 = !this.getStopTime().isUnknown() ? this.getStopTime() : duration;
        if (duration2.greaterThan(duration)) {
            duration2 = duration;
        }
        if ((duration2.isUnknown() || this.getStartTime().isUnknown() || this.getStartTime().isIndefinite()) && !this.getCycleDuration().isUnknown()) {
            this.setCycleDuration(Duration.UNKNOWN);
        }
        this.setCycleDuration(duration2.subtract(this.getStartTime()));
        this.calculateTotalDuration();
    }

    private void setTotalDuration(Duration duration) {
        this.totalDurationPropertyImpl().set(duration);
    }

    public final Duration getTotalDuration() {
        return this.totalDuration == null ? Duration.UNKNOWN : (Duration)this.totalDuration.get();
    }

    public ReadOnlyObjectProperty<Duration> totalDurationProperty() {
        return this.totalDurationPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Duration> totalDurationPropertyImpl() {
        if (this.totalDuration == null) {
            this.totalDuration = new ReadOnlyObjectWrapper(this, "totalDuration");
        }
        return this.totalDuration;
    }

    private void calculateTotalDuration() {
        if (this.getCycleCount() == -1) {
            this.setTotalDuration(Duration.INDEFINITE);
        } else if (this.getCycleDuration().isUnknown()) {
            this.setTotalDuration(Duration.UNKNOWN);
        } else {
            this.setTotalDuration(this.getCycleDuration().multiply(this.getCycleCount()));
        }
    }

    private void setCurrentTime(Duration duration) {
        this.currentTimePropertyImpl().set(duration);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Duration getCurrentTime() {
        Object object = this.disposeLock;
        synchronized (object) {
            double d;
            Duration duration;
            if (this.getStatus() == Status.DISPOSED) {
                return Duration.ZERO;
            }
            if (this.getStatus() == Status.STOPPED) {
                return Duration.millis(this.getStartTime().toMillis());
            }
            if (this.isEOS) {
                duration = this.media.getDuration();
                Duration duration2 = this.getStopTime();
                if (duration2 != Duration.UNKNOWN && duration != Duration.UNKNOWN) {
                    if (duration2.greaterThan(duration)) {
                        return Duration.millis(duration.toMillis());
                    }
                    return Duration.millis(duration2.toMillis());
                }
            }
            duration = (Duration)this.currentTimeProperty().get();
            if (this.playerReady && (d = this.jfxPlayer.getPresentationTime()) >= 0.0) {
                duration = Duration.seconds(d);
            }
            return duration;
        }
    }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return this.currentTimePropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Duration> currentTimePropertyImpl() {
        if (this.currentTime == null) {
            this.currentTime = new ReadOnlyObjectWrapper(this, "currentTime");
            this.currentTime.setValue(Duration.ZERO);
            this.updateTime();
        }
        return this.currentTime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void seek(Duration duration) {
        Object object = this.disposeLock;
        synchronized (object) {
            if (this.getStatus() == Status.DISPOSED) {
                return;
            }
            if (this.playerReady && duration != null && !duration.isUnknown()) {
                double d;
                Object object2;
                if (this.jfxPlayer.getDuration() == Double.POSITIVE_INFINITY) {
                    return;
                }
                if (duration.isIndefinite()) {
                    object2 = this.media.getDuration();
                    if (object2 == null || ((Duration)object2).isUnknown() || ((Duration)object2).isIndefinite()) {
                        object2 = Duration.millis(Double.MAX_VALUE);
                    }
                    d = ((Duration)object2).toMillis() / 1000.0;
                } else {
                    d = duration.toMillis() / 1000.0;
                    if (d < (object2 = (Object)this.calculateStartStopTimes(this.getStartTime(), this.getStopTime()))[0]) {
                        d = (double)object2[0];
                    } else if (d > object2[1]) {
                        d = (double)object2[1];
                    }
                }
                if (!this.isUpdateTimeEnabled && ((object2 = this.getStatus()) == Status.PLAYING || object2 == Status.PAUSED) && this.getStartTime().toSeconds() <= d && d <= this.getStopTime().toSeconds()) {
                    this.isEOS = false;
                    this.isUpdateTimeEnabled = true;
                    this.setCurrentRate(this.getRate());
                }
                this.jfxPlayer.seek(d);
            }
        }
    }

    private void setStatus(Status status) {
        this.statusPropertyImpl().set(status);
    }

    public final Status getStatus() {
        return this.status == null ? Status.UNKNOWN : (Status)((Object)this.status.get());
    }

    public ReadOnlyObjectProperty<Status> statusProperty() {
        return this.statusPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Status> statusPropertyImpl() {
        if (this.status == null) {
            this.status = new ReadOnlyObjectWrapper<Status>(){

                @Override
                protected void invalidated() {
                    if (this.get() == Status.PLAYING) {
                        MediaPlayer.this.setCurrentRate(MediaPlayer.this.getRate());
                    } else {
                        MediaPlayer.this.setCurrentRate(0.0);
                    }
                    if (this.get() == Status.READY) {
                        if (MediaPlayer.this.getOnReady() != null) {
                            Platform.runLater(MediaPlayer.this.getOnReady());
                        }
                    } else if (this.get() == Status.PLAYING) {
                        if (MediaPlayer.this.getOnPlaying() != null) {
                            Platform.runLater(MediaPlayer.this.getOnPlaying());
                        }
                    } else if (this.get() == Status.PAUSED) {
                        if (MediaPlayer.this.getOnPaused() != null) {
                            Platform.runLater(MediaPlayer.this.getOnPaused());
                        }
                    } else if (this.get() == Status.STOPPED) {
                        if (MediaPlayer.this.getOnStopped() != null) {
                            Platform.runLater(MediaPlayer.this.getOnStopped());
                        }
                    } else if (this.get() == Status.STALLED && MediaPlayer.this.getOnStalled() != null) {
                        Platform.runLater(MediaPlayer.this.getOnStalled());
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "status";
                }
            };
        }
        return this.status;
    }

    private void setBufferProgressTime(Duration duration) {
        this.bufferProgressTimePropertyImpl().set(duration);
    }

    public final Duration getBufferProgressTime() {
        return this.bufferProgressTime == null ? null : (Duration)this.bufferProgressTime.get();
    }

    public ReadOnlyObjectProperty<Duration> bufferProgressTimeProperty() {
        return this.bufferProgressTimePropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Duration> bufferProgressTimePropertyImpl() {
        if (this.bufferProgressTime == null) {
            this.bufferProgressTime = new ReadOnlyObjectWrapper(this, "bufferProgressTime");
        }
        return this.bufferProgressTime;
    }

    public final void setCycleCount(int n) {
        this.cycleCountProperty().set(n);
    }

    public final int getCycleCount() {
        return this.cycleCount == null ? 1 : this.cycleCount.get();
    }

    public IntegerProperty cycleCountProperty() {
        if (this.cycleCount == null) {
            this.cycleCount = new IntegerPropertyBase(1){

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "cycleCount";
                }
            };
        }
        return this.cycleCount;
    }

    private void setCurrentCount(int n) {
        this.currentCountPropertyImpl().set(n);
    }

    public final int getCurrentCount() {
        return this.currentCount == null ? 0 : this.currentCount.get();
    }

    public ReadOnlyIntegerProperty currentCountProperty() {
        return this.currentCountPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyIntegerWrapper currentCountPropertyImpl() {
        if (this.currentCount == null) {
            this.currentCount = new ReadOnlyIntegerWrapper(this, "currentCount");
        }
        return this.currentCount;
    }

    public final void setMute(boolean bl) {
        this.muteProperty().set(bl);
    }

    public final boolean isMute() {
        return this.mute == null ? false : this.mute.get();
    }

    public BooleanProperty muteProperty() {
        if (this.mute == null) {
            this.mute = new BooleanPropertyBase(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.jfxPlayer.setMute(this.get());
                            } else {
                                MediaPlayer.this.muteChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "mute";
                }
            };
        }
        return this.mute;
    }

    public final void setOnMarker(EventHandler<MediaMarkerEvent> eventHandler) {
        this.onMarkerProperty().set(eventHandler);
    }

    public final EventHandler<MediaMarkerEvent> getOnMarker() {
        return this.onMarker == null ? null : (EventHandler)this.onMarker.get();
    }

    public ObjectProperty<EventHandler<MediaMarkerEvent>> onMarkerProperty() {
        if (this.onMarker == null) {
            this.onMarker = new SimpleObjectProperty<EventHandler<MediaMarkerEvent>>(this, "onMarker");
        }
        return this.onMarker;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addView(MediaView mediaView) {
        WeakReference<MediaView> weakReference = new WeakReference<MediaView>(mediaView);
        Set<WeakReference<MediaView>> set = this.viewRefs;
        synchronized (set) {
            this.viewRefs.add(weakReference);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void removeView(MediaView mediaView) {
        Set<WeakReference<MediaView>> set = this.viewRefs;
        synchronized (set) {
            for (WeakReference<MediaView> weakReference : this.viewRefs) {
                MediaView mediaView2 = (MediaView)weakReference.get();
                if (mediaView2 == null || !mediaView2.equals(mediaView)) continue;
                this.viewRefs.remove(weakReference);
            }
        }
    }

    void handleError(MediaException mediaException) {
        Platform.runLater(() -> {
            this.setError(mediaException);
            if (mediaException.getType() == MediaException.Type.MEDIA_CORRUPTED || mediaException.getType() == MediaException.Type.MEDIA_UNSUPPORTED || mediaException.getType() == MediaException.Type.MEDIA_INACCESSIBLE || mediaException.getType() == MediaException.Type.MEDIA_UNAVAILABLE) {
                this.media._setError(mediaException.getType(), mediaException.getMessage());
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void createMediaTimer() {
        Object object = MediaTimerTask.timerLock;
        synchronized (object) {
            if (this.mediaTimerTask == null) {
                this.mediaTimerTask = new MediaTimerTask(this);
                this.mediaTimerTask.start();
            }
            this.isUpdateTimeEnabled = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void destroyMediaTimer() {
        Object object = MediaTimerTask.timerLock;
        synchronized (object) {
            if (this.mediaTimerTask != null) {
                this.isUpdateTimeEnabled = false;
                this.mediaTimerTask.stop();
                this.mediaTimerTask = null;
            }
        }
    }

    void updateTime() {
        double d;
        double d2;
        if (this.playerReady && this.isUpdateTimeEnabled && this.jfxPlayer != null && (d2 = this.jfxPlayer.getPresentationTime()) >= 0.0 && Double.compare(d = d2 * 1000.0, this.prevTimeMs) != 0) {
            this.setCurrentTime(Duration.millis(d));
            this.prevTimeMs = d;
        }
    }

    void loopPlayback() {
        this.seek(this.getStartTime());
    }

    void handleRequestedChanges() {
        if (this.rateChangeRequested) {
            if (this.jfxPlayer.getDuration() != Double.POSITIVE_INFINITY) {
                this.jfxPlayer.setRate((float)MediaPlayer.clamp(this.getRate(), 0.0, 8.0));
            }
            this.rateChangeRequested = false;
        }
        if (this.volumeChangeRequested) {
            this.jfxPlayer.setVolume((float)MediaPlayer.clamp(this.getVolume(), 0.0, 1.0));
            this.volumeChangeRequested = false;
        }
        if (this.balanceChangeRequested) {
            this.jfxPlayer.setBalance((float)MediaPlayer.clamp(this.getBalance(), -1.0, 1.0));
            this.balanceChangeRequested = false;
        }
        if (this.startTimeChangeRequested || this.stopTimeChangeRequested) {
            this.setStartStopTimes(this.getStartTime(), this.startTimeChangeRequested, this.getStopTime(), this.stopTimeChangeRequested);
            this.stopTimeChangeRequested = false;
            this.startTimeChangeRequested = false;
        }
        if (this.muteChangeRequested) {
            this.jfxPlayer.setMute(this.isMute());
            this.muteChangeRequested = false;
        }
        if (this.audioSpectrumNumBandsChangeRequested) {
            this.jfxPlayer.getAudioSpectrum().setBandCount(MediaPlayer.clamp(this.getAudioSpectrumNumBands(), 2, Integer.MAX_VALUE));
            this.audioSpectrumNumBandsChangeRequested = false;
        }
        if (this.audioSpectrumIntervalChangeRequested) {
            this.jfxPlayer.getAudioSpectrum().setInterval(MediaPlayer.clamp(this.getAudioSpectrumInterval(), 1.0E-9, Double.MAX_VALUE));
            this.audioSpectrumIntervalChangeRequested = false;
        }
        if (this.audioSpectrumThresholdChangeRequested) {
            this.jfxPlayer.getAudioSpectrum().setSensitivityThreshold(MediaPlayer.clamp(this.getAudioSpectrumThreshold(), Integer.MIN_VALUE, 0));
            this.audioSpectrumThresholdChangeRequested = false;
        }
        if (this.audioSpectrumEnabledChangeRequested) {
            boolean bl = this.getAudioSpectrumListener() != null;
            this.jfxPlayer.getAudioSpectrum().setEnabled(bl);
            this.audioSpectrumEnabledChangeRequested = false;
        }
        if (this.playRequested) {
            this.jfxPlayer.play();
            this.playRequested = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void preReady() {
        double d;
        WeakReference<MediaView> weakReference2;
        Set<WeakReference<MediaView>> set = this.viewRefs;
        synchronized (set) {
            for (WeakReference<MediaView> weakReference2 : this.viewRefs) {
                MediaView mediaView = (MediaView)weakReference2.get();
                if (mediaView == null) continue;
                mediaView._mediaPlayerOnReady();
            }
        }
        if (this.audioEqualizer != null) {
            this.audioEqualizer.setAudioEqualizer(this.jfxPlayer.getEqualizer());
        }
        weakReference2 = (d = this.jfxPlayer.getDuration()) >= 0.0 && !Double.isNaN(d) ? Duration.millis(d * 1000.0) : Duration.UNKNOWN;
        this.playerReady = true;
        this.media.setDuration((Duration)((Object)weakReference2));
        this.media._updateMedia(this.jfxPlayer.getMedia());
        this.handleRequestedChanges();
        this.calculateCycleDuration();
        if (this.lastBufferEvent != null && ((Duration)((Object)weakReference2)).toMillis() > 0.0) {
            double d2 = this.lastBufferEvent.getBufferPosition();
            double d3 = this.lastBufferEvent.getBufferStop();
            double d4 = d2 / d3 * ((Duration)((Object)weakReference2)).toMillis();
            this.lastBufferEvent = null;
            this.setBufferProgressTime(Duration.millis(d4));
        }
        this.setStatus(Status.READY);
    }

    public final void setOnEndOfMedia(Runnable runnable) {
        this.onEndOfMediaProperty().set(runnable);
    }

    public final Runnable getOnEndOfMedia() {
        return this.onEndOfMedia == null ? null : (Runnable)this.onEndOfMedia.get();
    }

    public ObjectProperty<Runnable> onEndOfMediaProperty() {
        if (this.onEndOfMedia == null) {
            this.onEndOfMedia = new SimpleObjectProperty<Runnable>(this, "onEndOfMedia");
        }
        return this.onEndOfMedia;
    }

    public final void setOnReady(Runnable runnable) {
        this.onReadyProperty().set(runnable);
    }

    public final Runnable getOnReady() {
        return this.onReady == null ? null : (Runnable)this.onReady.get();
    }

    public ObjectProperty<Runnable> onReadyProperty() {
        if (this.onReady == null) {
            this.onReady = new SimpleObjectProperty<Runnable>(this, "onReady");
        }
        return this.onReady;
    }

    public final void setOnPlaying(Runnable runnable) {
        this.onPlayingProperty().set(runnable);
    }

    public final Runnable getOnPlaying() {
        return this.onPlaying == null ? null : (Runnable)this.onPlaying.get();
    }

    public ObjectProperty<Runnable> onPlayingProperty() {
        if (this.onPlaying == null) {
            this.onPlaying = new SimpleObjectProperty<Runnable>(this, "onPlaying");
        }
        return this.onPlaying;
    }

    public final void setOnPaused(Runnable runnable) {
        this.onPausedProperty().set(runnable);
    }

    public final Runnable getOnPaused() {
        return this.onPaused == null ? null : (Runnable)this.onPaused.get();
    }

    public ObjectProperty<Runnable> onPausedProperty() {
        if (this.onPaused == null) {
            this.onPaused = new SimpleObjectProperty<Runnable>(this, "onPaused");
        }
        return this.onPaused;
    }

    public final void setOnStopped(Runnable runnable) {
        this.onStoppedProperty().set(runnable);
    }

    public final Runnable getOnStopped() {
        return this.onStopped == null ? null : (Runnable)this.onStopped.get();
    }

    public ObjectProperty<Runnable> onStoppedProperty() {
        if (this.onStopped == null) {
            this.onStopped = new SimpleObjectProperty<Runnable>(this, "onStopped");
        }
        return this.onStopped;
    }

    public final void setOnHalted(Runnable runnable) {
        this.onHaltedProperty().set(runnable);
    }

    public final Runnable getOnHalted() {
        return this.onHalted == null ? null : (Runnable)this.onHalted.get();
    }

    public ObjectProperty<Runnable> onHaltedProperty() {
        if (this.onHalted == null) {
            this.onHalted = new SimpleObjectProperty<Runnable>(this, "onHalted");
        }
        return this.onHalted;
    }

    public final void setOnRepeat(Runnable runnable) {
        this.onRepeatProperty().set(runnable);
    }

    public final Runnable getOnRepeat() {
        return this.onRepeat == null ? null : (Runnable)this.onRepeat.get();
    }

    public ObjectProperty<Runnable> onRepeatProperty() {
        if (this.onRepeat == null) {
            this.onRepeat = new SimpleObjectProperty<Runnable>(this, "onRepeat");
        }
        return this.onRepeat;
    }

    public final void setOnStalled(Runnable runnable) {
        this.onStalledProperty().set(runnable);
    }

    public final Runnable getOnStalled() {
        return this.onStalled == null ? null : (Runnable)this.onStalled.get();
    }

    public ObjectProperty<Runnable> onStalledProperty() {
        if (this.onStalled == null) {
            this.onStalled = new SimpleObjectProperty<Runnable>(this, "onStalled");
        }
        return this.onStalled;
    }

    public final void setAudioSpectrumNumBands(int n) {
        this.audioSpectrumNumBandsProperty().setValue(n);
    }

    public final int getAudioSpectrumNumBands() {
        return this.audioSpectrumNumBandsProperty().getValue();
    }

    public IntegerProperty audioSpectrumNumBandsProperty() {
        if (this.audioSpectrumNumBands == null) {
            this.audioSpectrumNumBands = new IntegerPropertyBase(128){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.jfxPlayer.getAudioSpectrum().setBandCount(MediaPlayer.clamp(MediaPlayer.this.audioSpectrumNumBands.get(), 2, Integer.MAX_VALUE));
                            } else {
                                MediaPlayer.this.audioSpectrumNumBandsChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "audioSpectrumNumBands";
                }
            };
        }
        return this.audioSpectrumNumBands;
    }

    public final void setAudioSpectrumInterval(double d) {
        this.audioSpectrumIntervalProperty().set(d);
    }

    public final double getAudioSpectrumInterval() {
        return this.audioSpectrumIntervalProperty().get();
    }

    public DoubleProperty audioSpectrumIntervalProperty() {
        if (this.audioSpectrumInterval == null) {
            this.audioSpectrumInterval = new DoublePropertyBase(0.1){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.jfxPlayer.getAudioSpectrum().setInterval(MediaPlayer.clamp(MediaPlayer.this.audioSpectrumInterval.get(), 1.0E-9, Double.MAX_VALUE));
                            } else {
                                MediaPlayer.this.audioSpectrumIntervalChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "audioSpectrumInterval";
                }
            };
        }
        return this.audioSpectrumInterval;
    }

    public final void setAudioSpectrumThreshold(int n) {
        this.audioSpectrumThresholdProperty().set(n);
    }

    public final int getAudioSpectrumThreshold() {
        return this.audioSpectrumThresholdProperty().get();
    }

    public IntegerProperty audioSpectrumThresholdProperty() {
        if (this.audioSpectrumThreshold == null) {
            this.audioSpectrumThreshold = new IntegerPropertyBase(-60){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                MediaPlayer.this.jfxPlayer.getAudioSpectrum().setSensitivityThreshold(MediaPlayer.clamp(MediaPlayer.this.audioSpectrumThreshold.get(), Integer.MIN_VALUE, 0));
                            } else {
                                MediaPlayer.this.audioSpectrumThresholdChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "audioSpectrumThreshold";
                }
            };
        }
        return this.audioSpectrumThreshold;
    }

    public final void setAudioSpectrumListener(AudioSpectrumListener audioSpectrumListener) {
        this.audioSpectrumListenerProperty().set(audioSpectrumListener);
    }

    public final AudioSpectrumListener getAudioSpectrumListener() {
        return (AudioSpectrumListener)this.audioSpectrumListenerProperty().get();
    }

    public ObjectProperty<AudioSpectrumListener> audioSpectrumListenerProperty() {
        if (this.audioSpectrumListener == null) {
            this.audioSpectrumListener = new ObjectPropertyBase<AudioSpectrumListener>(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                protected void invalidated() {
                    Object object = MediaPlayer.this.disposeLock;
                    synchronized (object) {
                        if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                            if (MediaPlayer.this.playerReady) {
                                boolean bl = MediaPlayer.this.audioSpectrumListener.get() != null;
                                MediaPlayer.this.jfxPlayer.getAudioSpectrum().setEnabled(bl);
                            } else {
                                MediaPlayer.this.audioSpectrumEnabledChangeRequested = true;
                            }
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return MediaPlayer.this;
                }

                @Override
                public String getName() {
                    return "audioSpectrumListener";
                }
            };
        }
        return this.audioSpectrumListener;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void dispose() {
        Object object = this.disposeLock;
        synchronized (object) {
            this.setStatus(Status.DISPOSED);
            this.destroyMediaTimer();
            if (this.audioEqualizer != null) {
                this.audioEqualizer.setAudioEqualizer(null);
                this.audioEqualizer = null;
            }
            if (this.jfxPlayer != null) {
                this.jfxPlayer.dispose();
                Object object2 = this.renderLock;
                synchronized (object2) {
                    if (this.rendererListener != null) {
                        Toolkit.getToolkit().removeStageTkPulseListener(this.rendererListener);
                        this.rendererListener = null;
                    }
                }
                this.jfxPlayer = null;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    VideoDataBuffer getLatestFrame() {
        Object object = this.renderLock;
        synchronized (object) {
            if (null != this.currentRenderFrame) {
                this.currentRenderFrame.holdFrame();
            }
            return this.currentRenderFrame;
        }
    }

    private class RendererListener
    implements VideoRendererListener,
    TKPulseListener {
        boolean updateMediaViews;

        private RendererListener() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void videoFrameUpdated(NewFrameEvent newFrameEvent) {
            VideoDataBuffer videoDataBuffer = newFrameEvent.getFrameData();
            if (null != videoDataBuffer) {
                Duration duration = new Duration(videoDataBuffer.getTimestamp() * 1000.0);
                Duration duration2 = MediaPlayer.this.getStopTime();
                if (duration.greaterThanOrEqualTo(MediaPlayer.this.getStartTime()) && (duration2.isUnknown() || duration.lessThanOrEqualTo(duration2))) {
                    this.updateMediaViews = true;
                    Object object = MediaPlayer.this.renderLock;
                    synchronized (object) {
                        videoDataBuffer.holdFrame();
                        if (null != MediaPlayer.this.nextRenderFrame) {
                            MediaPlayer.this.nextRenderFrame.releaseFrame();
                        }
                        MediaPlayer.this.nextRenderFrame = videoDataBuffer;
                    }
                    Toolkit.getToolkit().requestNextPulse();
                } else {
                    videoDataBuffer.releaseFrame();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void releaseVideoFrames() {
            Object object = MediaPlayer.this.renderLock;
            synchronized (object) {
                if (null != MediaPlayer.this.currentRenderFrame) {
                    MediaPlayer.this.currentRenderFrame.releaseFrame();
                    MediaPlayer.this.currentRenderFrame = null;
                }
                if (null != MediaPlayer.this.nextRenderFrame) {
                    MediaPlayer.this.nextRenderFrame.releaseFrame();
                    MediaPlayer.this.nextRenderFrame = null;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void pulse() {
            if (this.updateMediaViews) {
                this.updateMediaViews = false;
                Object object = MediaPlayer.this.renderLock;
                synchronized (object) {
                    if (null != MediaPlayer.this.nextRenderFrame) {
                        if (null != MediaPlayer.this.currentRenderFrame) {
                            MediaPlayer.this.currentRenderFrame.releaseFrame();
                        }
                        MediaPlayer.this.currentRenderFrame = MediaPlayer.this.nextRenderFrame;
                        MediaPlayer.this.nextRenderFrame = null;
                    }
                }
                object = MediaPlayer.this.viewRefs;
                synchronized (object) {
                    Iterator iterator = MediaPlayer.this.viewRefs.iterator();
                    while (iterator.hasNext()) {
                        MediaView mediaView = (MediaView)((WeakReference)iterator.next()).get();
                        if (null != mediaView) {
                            mediaView.notifyMediaFrameUpdated();
                            continue;
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

    private class _SpectrumListener
    implements com.sun.media.jfxmedia.events.AudioSpectrumListener {
        private float[] magnitudes;
        private float[] phases;

        private _SpectrumListener() {
        }

        @Override
        public void onAudioSpectrumEvent(AudioSpectrumEvent audioSpectrumEvent) {
            Platform.runLater(() -> {
                AudioSpectrumListener audioSpectrumListener = MediaPlayer.this.getAudioSpectrumListener();
                if (audioSpectrumListener != null) {
                    this.magnitudes = audioSpectrumEvent.getSource().getMagnitudes(this.magnitudes);
                    this.phases = audioSpectrumEvent.getSource().getPhases(this.phases);
                    audioSpectrumListener.spectrumDataUpdate(audioSpectrumEvent.getTimestamp(), audioSpectrumEvent.getDuration(), this.magnitudes, this.phases);
                }
            });
        }
    }

    private class _BufferListener
    implements BufferListener {
        double bufferedTime;

        private _BufferListener() {
        }

        @Override
        public void onBufferProgress(BufferProgressEvent bufferProgressEvent) {
            if (MediaPlayer.this.media != null) {
                if (bufferProgressEvent.getDuration() > 0.0) {
                    double d = bufferProgressEvent.getBufferPosition();
                    double d2 = bufferProgressEvent.getBufferStop();
                    this.bufferedTime = d / d2 * bufferProgressEvent.getDuration() * 1000.0;
                    MediaPlayer.this.lastBufferEvent = null;
                    Platform.runLater(() -> MediaPlayer.this.setBufferProgressTime(Duration.millis(this.bufferedTime)));
                } else {
                    MediaPlayer.this.lastBufferEvent = bufferProgressEvent;
                }
            }
        }
    }

    private class _MediaErrorListener
    implements MediaErrorListener {
        private _MediaErrorListener() {
        }

        @Override
        public void onError(Object object, int n, String string) {
            MediaException mediaException = MediaException.getMediaException(object, n, string);
            MediaPlayer.this.handleError(mediaException);
        }
    }

    private class _VideoTrackSizeListener
    implements VideoTrackSizeListener {
        int trackWidth;
        int trackHeight;

        private _VideoTrackSizeListener() {
        }

        @Override
        public void onSizeChanged(int n, int n2) {
            Platform.runLater(() -> {
                if (MediaPlayer.this.media != null) {
                    this.trackWidth = n;
                    this.trackHeight = n2;
                    this.setSize();
                }
            });
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setSize() {
            MediaPlayer.this.media.setWidth(this.trackWidth);
            MediaPlayer.this.media.setHeight(this.trackHeight);
            Set set = MediaPlayer.this.viewRefs;
            synchronized (set) {
                for (WeakReference weakReference : MediaPlayer.this.viewRefs) {
                    MediaView mediaView = (MediaView)weakReference.get();
                    if (mediaView == null) continue;
                    mediaView.notifyMediaSizeChange();
                }
            }
        }
    }

    private class _PlayerTimeListener
    implements PlayerTimeListener {
        double theDuration;

        private _PlayerTimeListener() {
        }

        void handleDurationChanged() {
            MediaPlayer.this.media.setDuration(Duration.millis(this.theDuration * 1000.0));
        }

        @Override
        public void onDurationChanged(double d) {
            Platform.runLater(() -> {
                this.theDuration = d;
                this.handleDurationChanged();
            });
        }
    }

    private class _PlayerStateListener
    implements PlayerStateListener {
        private _PlayerStateListener() {
        }

        @Override
        public void onReady(PlayerStateEvent playerStateEvent) {
            Platform.runLater(() -> {
                Object object = MediaPlayer.this.disposeLock;
                synchronized (object) {
                    if (MediaPlayer.this.getStatus() == Status.DISPOSED) {
                        return;
                    }
                    MediaPlayer.this.preReady();
                }
            });
        }

        @Override
        public void onPlaying(PlayerStateEvent playerStateEvent) {
            MediaPlayer.this.startTimeAtStop = null;
            Platform.runLater(() -> {
                MediaPlayer.this.createMediaTimer();
                MediaPlayer.this.setStatus(Status.PLAYING);
            });
        }

        @Override
        public void onPause(PlayerStateEvent playerStateEvent) {
            Platform.runLater(() -> {
                MediaPlayer.this.isUpdateTimeEnabled = false;
                MediaPlayer.this.setStatus(Status.PAUSED);
            });
            if (MediaPlayer.this.startTimeAtStop != null && MediaPlayer.this.startTimeAtStop != MediaPlayer.this.getStartTime()) {
                MediaPlayer.this.startTimeAtStop = null;
                Platform.runLater(() -> MediaPlayer.this.setCurrentTime(MediaPlayer.this.getStartTime()));
            }
        }

        @Override
        public void onStop(PlayerStateEvent playerStateEvent) {
            Platform.runLater(() -> {
                MediaPlayer.this.destroyMediaTimer();
                MediaPlayer.this.startTimeAtStop = MediaPlayer.this.getStartTime();
                MediaPlayer.this.setCurrentTime(MediaPlayer.this.getStartTime());
                MediaPlayer.this.setStatus(Status.STOPPED);
            });
        }

        @Override
        public void onStall(PlayerStateEvent playerStateEvent) {
            Platform.runLater(() -> {
                MediaPlayer.this.isUpdateTimeEnabled = false;
                MediaPlayer.this.setStatus(Status.STALLED);
            });
        }

        void handleFinish() {
            MediaPlayer.this.setCurrentCount(MediaPlayer.this.getCurrentCount() + 1);
            if (MediaPlayer.this.getCurrentCount() < MediaPlayer.this.getCycleCount() || MediaPlayer.this.getCycleCount() == -1) {
                if (MediaPlayer.this.getOnEndOfMedia() != null) {
                    Platform.runLater(MediaPlayer.this.getOnEndOfMedia());
                }
                MediaPlayer.this.loopPlayback();
                if (MediaPlayer.this.getOnRepeat() != null) {
                    Platform.runLater(MediaPlayer.this.getOnRepeat());
                }
            } else {
                MediaPlayer.this.isUpdateTimeEnabled = false;
                MediaPlayer.this.setCurrentRate(0.0);
                MediaPlayer.this.isEOS = true;
                if (MediaPlayer.this.getOnEndOfMedia() != null) {
                    Platform.runLater(MediaPlayer.this.getOnEndOfMedia());
                }
            }
        }

        @Override
        public void onFinish(PlayerStateEvent playerStateEvent) {
            MediaPlayer.this.startTimeAtStop = null;
            Platform.runLater(() -> this.handleFinish());
        }

        @Override
        public void onHalt(PlayerStateEvent playerStateEvent) {
            Platform.runLater(() -> {
                MediaPlayer.this.setStatus(Status.HALTED);
                MediaPlayer.this.handleError(MediaException.haltException(playerStateEvent.getMessage()));
                MediaPlayer.this.isUpdateTimeEnabled = false;
            });
        }
    }

    private class _MarkerListener
    implements MarkerListener {
        private _MarkerListener() {
        }

        @Override
        public void onMarker(MarkerEvent markerEvent) {
            Platform.runLater(() -> {
                Duration duration = Duration.millis(markerEvent.getPresentationTime() * 1000.0);
                if (MediaPlayer.this.getOnMarker() != null) {
                    MediaPlayer.this.getOnMarker().handle(new MediaMarkerEvent(new Pair<String, Duration>(markerEvent.getMarkerName(), duration)));
                }
            });
        }
    }

    private class MarkerMapChangeListener
    implements MapChangeListener<String, Duration> {
        private MarkerMapChangeListener() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void onChanged(MapChangeListener.Change<? extends String, ? extends Duration> change) {
            Object object = MediaPlayer.this.disposeLock;
            synchronized (object) {
                if (MediaPlayer.this.getStatus() != Status.DISPOSED) {
                    String string = change.getKey();
                    if (string == null) {
                        return;
                    }
                    com.sun.media.jfxmedia.Media media = MediaPlayer.this.jfxPlayer.getMedia();
                    if (change.wasAdded()) {
                        Duration duration;
                        if (change.wasRemoved()) {
                            media.removeMarker(string);
                        }
                        if ((duration = change.getValueAdded()) != null && duration.greaterThanOrEqualTo(Duration.ZERO)) {
                            media.addMarker(string, change.getValueAdded().toMillis() / 1000.0);
                        }
                    } else if (change.wasRemoved()) {
                        media.removeMarker(string);
                    }
                }
            }
        }
    }

    private class InitMediaPlayer
    implements Runnable {
        private InitMediaPlayer() {
        }

        @Override
        public void run() {
            try {
                MediaPlayer.this.init();
            }
            catch (com.sun.media.jfxmedia.MediaException mediaException) {
                MediaPlayer.this.handleError(MediaException.exceptionToMediaException(mediaException));
            }
            catch (MediaException mediaException) {
                if (MediaPlayer.this.media.getError() != null) {
                    MediaPlayer.this.handleError(MediaPlayer.this.media.getError());
                } else {
                    MediaPlayer.this.handleError(mediaException);
                }
            }
            catch (Exception exception) {
                MediaPlayer.this.handleError(new MediaException(MediaException.Type.UNKNOWN, exception.getMessage()));
            }
        }
    }

    public static enum Status {
        UNKNOWN,
        READY,
        PAUSED,
        PLAYING,
        STOPPED,
        STALLED,
        HALTED,
        DISPOSED;

    }
}

