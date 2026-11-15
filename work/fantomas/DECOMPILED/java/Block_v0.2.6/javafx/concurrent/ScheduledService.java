/*
 * Decompiled with CFR 0.152.
 */
package javafx.concurrent;

import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Callback;
import javafx.util.Duration;

public abstract class ScheduledService<V>
extends Service<V> {
    public static final Callback<ScheduledService<?>, Duration> EXPONENTIAL_BACKOFF_STRATEGY = new Callback<ScheduledService<?>, Duration>(){

        @Override
        public Duration call(ScheduledService<?> scheduledService) {
            if (scheduledService == null) {
                return Duration.ZERO;
            }
            double d = scheduledService.getPeriod() == null ? 0.0 : scheduledService.getPeriod().toMillis();
            double d2 = scheduledService.getCurrentFailureCount();
            return Duration.millis(d == 0.0 ? Math.exp(d2) : d + d * Math.exp(d2));
        }
    };
    public static final Callback<ScheduledService<?>, Duration> LOGARITHMIC_BACKOFF_STRATEGY = new Callback<ScheduledService<?>, Duration>(){

        @Override
        public Duration call(ScheduledService<?> scheduledService) {
            if (scheduledService == null) {
                return Duration.ZERO;
            }
            double d = scheduledService.getPeriod() == null ? 0.0 : scheduledService.getPeriod().toMillis();
            double d2 = scheduledService.getCurrentFailureCount();
            return Duration.millis(d == 0.0 ? Math.log1p(d2) : d + d * Math.log1p(d2));
        }
    };
    public static final Callback<ScheduledService<?>, Duration> LINEAR_BACKOFF_STRATEGY = new Callback<ScheduledService<?>, Duration>(){

        @Override
        public Duration call(ScheduledService<?> scheduledService) {
            if (scheduledService == null) {
                return Duration.ZERO;
            }
            double d = scheduledService.getPeriod() == null ? 0.0 : scheduledService.getPeriod().toMillis();
            double d2 = scheduledService.getCurrentFailureCount();
            return Duration.millis(d == 0.0 ? d2 : d + d * d2);
        }
    };
    private static final Timer DELAY_TIMER = new Timer("ScheduledService Delay Timer", true);
    private ObjectProperty<Duration> delay = new SimpleObjectProperty<Duration>(this, "delay", Duration.ZERO);
    private ObjectProperty<Duration> period = new SimpleObjectProperty<Duration>(this, "period", Duration.ZERO);
    private ObjectProperty<Callback<ScheduledService<?>, Duration>> backoffStrategy = new SimpleObjectProperty(this, "backoffStrategy", LOGARITHMIC_BACKOFF_STRATEGY);
    private BooleanProperty restartOnFailure = new SimpleBooleanProperty(this, "restartOnFailure", true);
    private IntegerProperty maximumFailureCount = new SimpleIntegerProperty(this, "maximumFailureCount", Integer.MAX_VALUE);
    private ReadOnlyIntegerWrapper currentFailureCount = new ReadOnlyIntegerWrapper(this, "currentFailureCount", 0);
    private ReadOnlyObjectWrapper<Duration> cumulativePeriod = new ReadOnlyObjectWrapper<Duration>(this, "cumulativePeriod", Duration.ZERO);
    private ObjectProperty<Duration> maximumCumulativePeriod = new SimpleObjectProperty<Duration>(this, "maximumCumulativePeriod", Duration.INDEFINITE);
    private ReadOnlyObjectWrapper<V> lastValue = new ReadOnlyObjectWrapper<Object>(this, "lastValue", null);
    private long lastRunTime = 0L;
    private boolean freshStart = true;
    private TimerTask delayTask = null;
    private boolean stop = false;

    public final Duration getDelay() {
        return (Duration)this.delay.get();
    }

    public final void setDelay(Duration duration) {
        this.delay.set(duration);
    }

    public final ObjectProperty<Duration> delayProperty() {
        return this.delay;
    }

    public final Duration getPeriod() {
        return (Duration)this.period.get();
    }

    public final void setPeriod(Duration duration) {
        this.period.set(duration);
    }

    public final ObjectProperty<Duration> periodProperty() {
        return this.period;
    }

    public final Callback<ScheduledService<?>, Duration> getBackoffStrategy() {
        return (Callback)this.backoffStrategy.get();
    }

    public final void setBackoffStrategy(Callback<ScheduledService<?>, Duration> callback) {
        this.backoffStrategy.set(callback);
    }

    public final ObjectProperty<Callback<ScheduledService<?>, Duration>> backoffStrategyProperty() {
        return this.backoffStrategy;
    }

    public final boolean getRestartOnFailure() {
        return this.restartOnFailure.get();
    }

    public final void setRestartOnFailure(boolean bl) {
        this.restartOnFailure.set(bl);
    }

    public final BooleanProperty restartOnFailureProperty() {
        return this.restartOnFailure;
    }

    public final int getMaximumFailureCount() {
        return this.maximumFailureCount.get();
    }

    public final void setMaximumFailureCount(int n) {
        this.maximumFailureCount.set(n);
    }

    public final IntegerProperty maximumFailureCountProperty() {
        return this.maximumFailureCount;
    }

    public final int getCurrentFailureCount() {
        return this.currentFailureCount.get();
    }

    public final ReadOnlyIntegerProperty currentFailureCountProperty() {
        return this.currentFailureCount.getReadOnlyProperty();
    }

    private void setCurrentFailureCount(int n) {
        this.currentFailureCount.set(n);
    }

    public final Duration getCumulativePeriod() {
        return (Duration)this.cumulativePeriod.get();
    }

    public final ReadOnlyObjectProperty<Duration> cumulativePeriodProperty() {
        return this.cumulativePeriod.getReadOnlyProperty();
    }

    void setCumulativePeriod(Duration duration) {
        Duration duration2 = duration == null || duration.toMillis() < 0.0 ? Duration.ZERO : duration;
        Duration duration3 = (Duration)this.maximumCumulativePeriod.get();
        if (duration3 != null && !duration3.isUnknown() && !duration2.isUnknown()) {
            if (duration3.toMillis() < 0.0) {
                duration2 = Duration.ZERO;
            } else if (!duration3.isIndefinite() && duration2.greaterThan(duration3)) {
                duration2 = duration3;
            }
        }
        this.cumulativePeriod.set(duration2);
    }

    public final Duration getMaximumCumulativePeriod() {
        return (Duration)this.maximumCumulativePeriod.get();
    }

    public final void setMaximumCumulativePeriod(Duration duration) {
        this.maximumCumulativePeriod.set(duration);
    }

    public final ObjectProperty<Duration> maximumCumulativePeriodProperty() {
        return this.maximumCumulativePeriod;
    }

    public final V getLastValue() {
        return (V)this.lastValue.get();
    }

    public final ReadOnlyObjectProperty<V> lastValueProperty() {
        return this.lastValue.getReadOnlyProperty();
    }

    @Override
    protected void executeTask(Task<V> task) {
        assert (task != null);
        this.checkThread();
        if (this.freshStart) {
            assert (this.delayTask == null);
            this.setCumulativePeriod(this.getPeriod());
            long l = (long)ScheduledService.normalize(this.getDelay());
            if (l == 0L) {
                this.executeTaskNow(task);
            } else {
                this.delayTask = this.createTimerTask(task);
                this.schedule(this.delayTask, l);
            }
        } else {
            double d = ScheduledService.normalize(this.getCumulativePeriod());
            double d2 = this.clock() - this.lastRunTime;
            if (d2 < d) {
                assert (this.delayTask == null);
                this.delayTask = this.createTimerTask(task);
                this.schedule(this.delayTask, (long)(d - d2));
            } else {
                this.executeTaskNow(task);
            }
        }
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        this.lastValue.set(this.getValue());
        Duration duration = this.getPeriod();
        this.setCumulativePeriod(duration);
        boolean bl = this.stop;
        this.superReset();
        assert (!this.freshStart);
        if (bl) {
            this.cancelFromReadyState();
        } else {
            this.start();
        }
    }

    @Override
    protected void failed() {
        super.failed();
        assert (this.delayTask == null);
        this.setCurrentFailureCount(this.getCurrentFailureCount() + 1);
        if (this.getRestartOnFailure() && this.getMaximumFailureCount() > this.getCurrentFailureCount()) {
            Callback<ScheduledService<?>, Duration> callback = this.getBackoffStrategy();
            if (callback != null) {
                Duration duration = callback.call(this);
                this.setCumulativePeriod(duration);
            }
            this.superReset();
            assert (!this.freshStart);
            this.start();
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.stop = false;
        this.setCumulativePeriod(this.getPeriod());
        this.lastValue.set(null);
        this.setCurrentFailureCount(0);
        this.lastRunTime = 0L;
        this.freshStart = true;
    }

    @Override
    public boolean cancel() {
        boolean bl = super.cancel();
        this.stop = true;
        if (this.delayTask != null) {
            this.delayTask.cancel();
            this.delayTask = null;
        }
        return bl;
    }

    void schedule(TimerTask timerTask, long l) {
        DELAY_TIMER.schedule(timerTask, l);
    }

    boolean isFreshStart() {
        return this.freshStart;
    }

    long clock() {
        return System.currentTimeMillis();
    }

    private void superReset() {
        super.reset();
    }

    private TimerTask createTimerTask(final Task<V> task) {
        assert (task != null);
        return new TimerTask(){

            @Override
            public void run() {
                Runnable runnable = () -> {
                    ScheduledService.this.executeTaskNow(task);
                    ScheduledService.this.delayTask = null;
                };
                if (ScheduledService.this.isFxApplicationThread()) {
                    runnable.run();
                } else {
                    ScheduledService.this.runLater(runnable);
                }
            }
        };
    }

    private void executeTaskNow(Task<V> task) {
        assert (task != null);
        this.lastRunTime = this.clock();
        this.freshStart = false;
        super.executeTask(task);
    }

    private static double normalize(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return 0.0;
        }
        if (duration.isIndefinite()) {
            return Double.MAX_VALUE;
        }
        return duration.toMillis();
    }
}

