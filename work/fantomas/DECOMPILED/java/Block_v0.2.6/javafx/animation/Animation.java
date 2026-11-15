/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.javafx.animation.TickCalculation;
import com.sun.javafx.tk.Toolkit;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.animation.shared.ClipEnvelope;
import com.sun.scenario.animation.shared.PulseReceiver;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.HashMap;
import javafx.animation.AnimationAccessorImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoublePropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public abstract class Animation {
    public static final int INDEFINITE = -1;
    private static final double EPSILON = 1.0E-12;
    private long startTime;
    private long pauseTime;
    private boolean paused = false;
    private final AbstractMasterTimer timer;
    private AccessControlContext accessCtrlCtx = null;
    final PulseReceiver pulseReceiver = new PulseReceiver(){

        @Override
        public void timePulse(long l) {
            long l2 = l - Animation.this.startTime;
            if (l2 < 0L) {
                return;
            }
            if (Animation.this.accessCtrlCtx == null) {
                throw new IllegalStateException("Error: AccessControlContext not captured");
            }
            AccessController.doPrivileged(() -> {
                Animation.this.doTimePulse(l2);
                return null;
            }, Animation.this.accessCtrlCtx);
        }
    };
    Animation parent = null;
    ClipEnvelope clipEnvelope;
    private boolean lastPlayedFinished = false;
    private boolean lastPlayedForward = true;
    private DoubleProperty rate;
    private static final double DEFAULT_RATE = 1.0;
    private double oldRate = 1.0;
    private ReadOnlyDoubleProperty currentRate;
    private static final double DEFAULT_CURRENT_RATE = 0.0;
    private ReadOnlyObjectProperty<Duration> cycleDuration;
    private static final Duration DEFAULT_CYCLE_DURATION;
    private ReadOnlyObjectProperty<Duration> totalDuration;
    private static final Duration DEFAULT_TOTAL_DURATION;
    private CurrentTimeProperty currentTime;
    private long currentTicks;
    private ObjectProperty<Duration> delay;
    private static final Duration DEFAULT_DELAY;
    private IntegerProperty cycleCount;
    private static final int DEFAULT_CYCLE_COUNT = 1;
    private BooleanProperty autoReverse;
    private static final boolean DEFAULT_AUTO_REVERSE = false;
    private ReadOnlyObjectProperty<Status> status;
    private static final Status DEFAULT_STATUS;
    private final double targetFramerate;
    private final int resolution;
    private long lastPulse;
    private ObjectProperty<EventHandler<ActionEvent>> onFinished;
    private static final EventHandler<ActionEvent> DEFAULT_ON_FINISHED;
    private final ObservableMap<String, Duration> cuePoints = FXCollections.observableMap(new HashMap(0));

    private long now() {
        return TickCalculation.fromNano(this.timer.nanos());
    }

    private void addPulseReceiver() {
        this.accessCtrlCtx = AccessController.getContext();
        this.timer.addPulseReceiver(this.pulseReceiver);
    }

    void startReceiver(long l) {
        this.paused = false;
        this.startTime = this.now() + l;
        this.addPulseReceiver();
    }

    void pauseReceiver() {
        if (!this.paused) {
            this.pauseTime = this.now();
            this.paused = true;
            this.timer.removePulseReceiver(this.pulseReceiver);
        }
    }

    void resumeReceiver() {
        if (this.paused) {
            long l = this.now() - this.pauseTime;
            this.startTime += l;
            this.paused = false;
            this.addPulseReceiver();
        }
    }

    public final void setRate(double d) {
        if (this.rate != null || Math.abs(d - 1.0) > 1.0E-12) {
            this.rateProperty().set(d);
        }
    }

    public final double getRate() {
        return this.rate == null ? 1.0 : this.rate.get();
    }

    public final DoubleProperty rateProperty() {
        if (this.rate == null) {
            this.rate = new DoublePropertyBase(1.0){

                @Override
                public void invalidated() {
                    double d = Animation.this.getRate();
                    if (Animation.this.isRunningEmbedded()) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(Animation.this.oldRate);
                        throw new IllegalArgumentException("Cannot set rate of embedded animation while running.");
                    }
                    if (Math.abs(d) < 1.0E-12) {
                        if (Animation.this.getStatus() == Status.RUNNING) {
                            Animation.this.lastPlayedForward = Math.abs(Animation.this.getCurrentRate() - Animation.this.oldRate) < 1.0E-12;
                        }
                        Animation.this.doSetCurrentRate(0.0);
                        Animation.this.pauseReceiver();
                    } else {
                        if (Animation.this.getStatus() == Status.RUNNING) {
                            double d2 = Animation.this.getCurrentRate();
                            if (Math.abs(d2) < 1.0E-12) {
                                Animation.this.doSetCurrentRate(Animation.this.lastPlayedForward ? d : -d);
                                Animation.this.resumeReceiver();
                            } else {
                                boolean bl = Math.abs(d2 - Animation.this.oldRate) < 1.0E-12;
                                Animation.this.doSetCurrentRate(bl ? d : -d);
                            }
                        }
                        Animation.this.oldRate = d;
                    }
                    Animation.this.clipEnvelope.setRate(d);
                }

                @Override
                public Object getBean() {
                    return Animation.this;
                }

                @Override
                public String getName() {
                    return "rate";
                }
            };
        }
        return this.rate;
    }

    private boolean isRunningEmbedded() {
        if (this.parent == null) {
            return false;
        }
        return this.parent.getStatus() != Status.STOPPED || this.parent.isRunningEmbedded();
    }

    private void doSetCurrentRate(double d) {
        if (this.currentRate != null || Math.abs(d - 0.0) > 1.0E-12) {
            ((CurrentRateProperty)this.currentRateProperty()).set(d);
        }
    }

    public final double getCurrentRate() {
        return this.currentRate == null ? 0.0 : this.currentRate.get();
    }

    public final ReadOnlyDoubleProperty currentRateProperty() {
        if (this.currentRate == null) {
            this.currentRate = new CurrentRateProperty();
        }
        return this.currentRate;
    }

    protected final void setCycleDuration(Duration duration) {
        if (this.cycleDuration != null || !DEFAULT_CYCLE_DURATION.equals(duration)) {
            if (duration.lessThan(Duration.ZERO)) {
                throw new IllegalArgumentException("Cycle duration cannot be negative");
            }
            ((AnimationReadOnlyProperty)this.cycleDurationProperty()).set(duration);
            this.updateTotalDuration();
        }
    }

    public final Duration getCycleDuration() {
        return this.cycleDuration == null ? DEFAULT_CYCLE_DURATION : (Duration)this.cycleDuration.get();
    }

    public final ReadOnlyObjectProperty<Duration> cycleDurationProperty() {
        if (this.cycleDuration == null) {
            this.cycleDuration = new AnimationReadOnlyProperty<Duration>("cycleDuration", DEFAULT_CYCLE_DURATION);
        }
        return this.cycleDuration;
    }

    public final Duration getTotalDuration() {
        return this.totalDuration == null ? DEFAULT_TOTAL_DURATION : (Duration)this.totalDuration.get();
    }

    public final ReadOnlyObjectProperty<Duration> totalDurationProperty() {
        if (this.totalDuration == null) {
            this.totalDuration = new AnimationReadOnlyProperty<Duration>("totalDuration", DEFAULT_TOTAL_DURATION);
        }
        return this.totalDuration;
    }

    private void updateTotalDuration() {
        Duration duration;
        int n = this.getCycleCount();
        Duration duration2 = this.getCycleDuration();
        Duration duration3 = Duration.ZERO.equals(duration2) ? Duration.ZERO : (n == -1 ? Duration.INDEFINITE : (duration = n <= 1 ? duration2 : duration2.multiply(n)));
        if (this.totalDuration != null || !DEFAULT_TOTAL_DURATION.equals(duration)) {
            ((AnimationReadOnlyProperty)this.totalDurationProperty()).set(duration);
        }
        if (this.getStatus() == Status.STOPPED) {
            this.syncClipEnvelope();
            if (duration.lessThan(this.getCurrentTime())) {
                this.clipEnvelope.jumpTo(TickCalculation.fromDuration(duration));
            }
        }
    }

    public final Duration getCurrentTime() {
        return TickCalculation.toDuration(this.currentTicks);
    }

    public final ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        if (this.currentTime == null) {
            this.currentTime = new CurrentTimeProperty();
        }
        return this.currentTime;
    }

    public final void setDelay(Duration duration) {
        if (this.delay != null || !DEFAULT_DELAY.equals(duration)) {
            this.delayProperty().set(duration);
        }
    }

    public final Duration getDelay() {
        return this.delay == null ? DEFAULT_DELAY : (Duration)this.delay.get();
    }

    public final ObjectProperty<Duration> delayProperty() {
        if (this.delay == null) {
            this.delay = new ObjectPropertyBase<Duration>(DEFAULT_DELAY){

                @Override
                public Object getBean() {
                    return Animation.this;
                }

                @Override
                public String getName() {
                    return "delay";
                }

                @Override
                protected void invalidated() {
                    Duration duration = (Duration)this.get();
                    if (duration.lessThan(Duration.ZERO)) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(Duration.ZERO);
                        throw new IllegalArgumentException("Cannot set delay to negative value. Setting to Duration.ZERO");
                    }
                }
            };
        }
        return this.delay;
    }

    public final void setCycleCount(int n) {
        if (this.cycleCount != null || n != 1) {
            this.cycleCountProperty().set(n);
        }
    }

    public final int getCycleCount() {
        return this.cycleCount == null ? 1 : this.cycleCount.get();
    }

    public final IntegerProperty cycleCountProperty() {
        if (this.cycleCount == null) {
            this.cycleCount = new IntegerPropertyBase(1){

                @Override
                public void invalidated() {
                    Animation.this.updateTotalDuration();
                }

                @Override
                public Object getBean() {
                    return Animation.this;
                }

                @Override
                public String getName() {
                    return "cycleCount";
                }
            };
        }
        return this.cycleCount;
    }

    public final void setAutoReverse(boolean bl) {
        if (this.autoReverse != null || bl) {
            this.autoReverseProperty().set(bl);
        }
    }

    public final boolean isAutoReverse() {
        return this.autoReverse == null ? false : this.autoReverse.get();
    }

    public final BooleanProperty autoReverseProperty() {
        if (this.autoReverse == null) {
            this.autoReverse = new SimpleBooleanProperty(this, "autoReverse", false);
        }
        return this.autoReverse;
    }

    protected final void setStatus(Status status) {
        if (this.status != null || !DEFAULT_STATUS.equals((Object)status)) {
            ((AnimationReadOnlyProperty)this.statusProperty()).set((Object)status);
        }
    }

    public final Status getStatus() {
        return this.status == null ? DEFAULT_STATUS : (Status)((Object)this.status.get());
    }

    public final ReadOnlyObjectProperty<Status> statusProperty() {
        if (this.status == null) {
            this.status = new AnimationReadOnlyProperty<Status>("status", (Object)Status.STOPPED);
        }
        return this.status;
    }

    public final double getTargetFramerate() {
        return this.targetFramerate;
    }

    public final void setOnFinished(EventHandler<ActionEvent> eventHandler) {
        if (this.onFinished != null || eventHandler != null) {
            this.onFinishedProperty().set(eventHandler);
        }
    }

    public final EventHandler<ActionEvent> getOnFinished() {
        return this.onFinished == null ? DEFAULT_ON_FINISHED : (EventHandler)this.onFinished.get();
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onFinishedProperty() {
        if (this.onFinished == null) {
            this.onFinished = new SimpleObjectProperty<EventHandler<ActionEvent>>(this, "onFinished", DEFAULT_ON_FINISHED);
        }
        return this.onFinished;
    }

    public final ObservableMap<String, Duration> getCuePoints() {
        return this.cuePoints;
    }

    public void jumpTo(Duration duration) {
        if (duration == null) {
            throw new NullPointerException("Time needs to be specified.");
        }
        if (duration.isUnknown()) {
            throw new IllegalArgumentException("The time is invalid");
        }
        if (this.parent != null) {
            throw new IllegalStateException("Cannot jump when embedded in another animation");
        }
        this.lastPlayedFinished = false;
        Duration duration2 = this.getTotalDuration();
        duration = duration.lessThan(Duration.ZERO) ? Duration.ZERO : (duration.greaterThan(duration2) ? duration2 : duration);
        long l = TickCalculation.fromDuration(duration);
        if (this.getStatus() == Status.STOPPED) {
            this.syncClipEnvelope();
        }
        this.clipEnvelope.jumpTo(l);
    }

    public void jumpTo(String string) {
        if (string == null) {
            throw new NullPointerException("CuePoint needs to be specified");
        }
        if ("start".equalsIgnoreCase(string)) {
            this.jumpTo(Duration.ZERO);
        } else if ("end".equalsIgnoreCase(string)) {
            this.jumpTo(this.getTotalDuration());
        } else {
            Duration duration = (Duration)this.getCuePoints().get(string);
            if (duration != null) {
                this.jumpTo(duration);
            }
        }
    }

    public void playFrom(String string) {
        this.jumpTo(string);
        this.play();
    }

    public void playFrom(Duration duration) {
        this.jumpTo(duration);
        this.play();
    }

    public void play() {
        if (this.parent != null) {
            throw new IllegalStateException("Cannot start when embedded in another animation");
        }
        switch (this.getStatus()) {
            case STOPPED: {
                if (this.startable(true)) {
                    double d = this.getRate();
                    if (this.lastPlayedFinished) {
                        this.jumpTo(d < 0.0 ? this.getTotalDuration() : Duration.ZERO);
                    }
                    this.doStart(true);
                    this.startReceiver(TickCalculation.fromDuration(this.getDelay()));
                    if (!(Math.abs(d) < 1.0E-12)) break;
                    this.pauseReceiver();
                    break;
                }
                EventHandler<ActionEvent> eventHandler = this.getOnFinished();
                if (eventHandler == null) break;
                eventHandler.handle(new ActionEvent(this, null));
                break;
            }
            case PAUSED: {
                this.doResume();
                if (!(Math.abs(this.getRate()) >= 1.0E-12)) break;
                this.resumeReceiver();
            }
        }
    }

    public void playFromStart() {
        this.stop();
        this.setRate(Math.abs(this.getRate()));
        this.jumpTo(Duration.ZERO);
        this.play();
    }

    public void stop() {
        if (this.parent != null) {
            throw new IllegalStateException("Cannot stop when embedded in another animation");
        }
        if (this.getStatus() != Status.STOPPED) {
            this.clipEnvelope.abortCurrentPulse();
            this.doStop();
            this.jumpTo(Duration.ZERO);
        }
    }

    public void pause() {
        if (this.parent != null) {
            throw new IllegalStateException("Cannot pause when embedded in another animation");
        }
        if (this.getStatus() == Status.RUNNING) {
            this.clipEnvelope.abortCurrentPulse();
            this.pauseReceiver();
            this.doPause();
        }
    }

    protected Animation(double d) {
        this.targetFramerate = d;
        this.resolution = (int)Math.max(1L, Math.round(6000.0 / d));
        this.clipEnvelope = ClipEnvelope.create(this);
        this.timer = Toolkit.getToolkit().getMasterTimer();
    }

    protected Animation() {
        this.resolution = 1;
        this.targetFramerate = 6000 / Toolkit.getToolkit().getMasterTimer().getDefaultResolution();
        this.clipEnvelope = ClipEnvelope.create(this);
        this.timer = Toolkit.getToolkit().getMasterTimer();
    }

    Animation(AbstractMasterTimer abstractMasterTimer) {
        this.resolution = 1;
        this.targetFramerate = 6000 / abstractMasterTimer.getDefaultResolution();
        this.clipEnvelope = ClipEnvelope.create(this);
        this.timer = abstractMasterTimer;
    }

    Animation(AbstractMasterTimer abstractMasterTimer, ClipEnvelope clipEnvelope, int n) {
        this.resolution = n;
        this.targetFramerate = 6000 / n;
        this.clipEnvelope = clipEnvelope;
        this.timer = abstractMasterTimer;
    }

    boolean startable(boolean bl) {
        return TickCalculation.fromDuration(this.getCycleDuration()) > 0L || !bl && this.clipEnvelope.wasSynched();
    }

    void sync(boolean bl) {
        if (bl || !this.clipEnvelope.wasSynched()) {
            this.syncClipEnvelope();
        }
    }

    private void syncClipEnvelope() {
        int n = this.getCycleCount();
        int n2 = n <= 0 && n != -1 ? 1 : n;
        this.clipEnvelope = this.clipEnvelope.setCycleCount(n2);
        this.clipEnvelope.setCycleDuration(this.getCycleDuration());
        this.clipEnvelope.setAutoReverse(this.isAutoReverse());
    }

    void doStart(boolean bl) {
        this.sync(bl);
        this.setStatus(Status.RUNNING);
        this.clipEnvelope.start();
        this.doSetCurrentRate(this.clipEnvelope.getCurrentRate());
        this.lastPulse = 0L;
    }

    void doPause() {
        double d = this.getCurrentRate();
        if (Math.abs(d) >= 1.0E-12) {
            this.lastPlayedForward = Math.abs(this.getCurrentRate() - this.getRate()) < 1.0E-12;
        }
        this.doSetCurrentRate(0.0);
        this.setStatus(Status.PAUSED);
    }

    void doResume() {
        this.setStatus(Status.RUNNING);
        this.doSetCurrentRate(this.lastPlayedForward ? this.getRate() : -this.getRate());
    }

    void doStop() {
        if (!this.paused) {
            this.timer.removePulseReceiver(this.pulseReceiver);
        }
        this.setStatus(Status.STOPPED);
        this.doSetCurrentRate(0.0);
    }

    void doTimePulse(long l) {
        if (this.resolution == 1) {
            this.clipEnvelope.timePulse(l);
        } else if (l - this.lastPulse >= (long)this.resolution) {
            this.lastPulse = l / (long)this.resolution * (long)this.resolution;
            this.clipEnvelope.timePulse(l);
        }
    }

    abstract void doPlayTo(long var1, long var3);

    abstract void doJumpTo(long var1, long var3, boolean var5);

    void setCurrentTicks(long l) {
        this.currentTicks = l;
        if (this.currentTime != null) {
            this.currentTime.fireValueChangedEvent();
        }
    }

    void setCurrentRate(double d) {
        this.doSetCurrentRate(d);
    }

    final void finished() {
        this.lastPlayedFinished = true;
        this.doStop();
        EventHandler<ActionEvent> eventHandler = this.getOnFinished();
        if (eventHandler != null) {
            try {
                eventHandler.handle(new ActionEvent(this, null));
            }
            catch (Exception exception) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), exception);
            }
        }
    }

    static {
        AnimationAccessorImpl.DEFAULT = new AnimationAccessorImpl();
        DEFAULT_CYCLE_DURATION = Duration.ZERO;
        DEFAULT_TOTAL_DURATION = Duration.ZERO;
        DEFAULT_DELAY = Duration.ZERO;
        DEFAULT_STATUS = Status.STOPPED;
        DEFAULT_ON_FINISHED = null;
    }

    private class CurrentTimeProperty
    extends ReadOnlyObjectPropertyBase<Duration> {
        private CurrentTimeProperty() {
        }

        @Override
        public Object getBean() {
            return Animation.this;
        }

        @Override
        public String getName() {
            return "currentTime";
        }

        @Override
        public Duration get() {
            return Animation.this.getCurrentTime();
        }

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private class AnimationReadOnlyProperty<T>
    extends ReadOnlyObjectPropertyBase<T> {
        private final String name;
        private T value;

        private AnimationReadOnlyProperty(String string, T t) {
            this.name = string;
            this.value = t;
        }

        @Override
        public Object getBean() {
            return Animation.this;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public T get() {
            return this.value;
        }

        private void set(T t) {
            this.value = t;
            this.fireValueChangedEvent();
        }
    }

    private class CurrentRateProperty
    extends ReadOnlyDoublePropertyBase {
        private double value;

        private CurrentRateProperty() {
        }

        @Override
        public Object getBean() {
            return Animation.this;
        }

        @Override
        public String getName() {
            return "currentRate";
        }

        @Override
        public double get() {
            return this.value;
        }

        private void set(double d) {
            this.value = d;
            this.fireValueChangedEvent();
        }
    }

    public static enum Status {
        PAUSED,
        RUNNING,
        STOPPED;

    }
}

