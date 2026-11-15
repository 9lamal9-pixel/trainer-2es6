/*
 * Decompiled with CFR 0.152.
 */
package javafx.concurrent;

import java.security.AccessController;
import java.security.Permission;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.EventHelper;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;

public abstract class Task<V>
extends FutureTask<V>
implements Worker<V>,
EventTarget {
    private AtomicReference<ProgressUpdate> progressUpdate = new AtomicReference();
    private AtomicReference<String> messageUpdate = new AtomicReference();
    private AtomicReference<String> titleUpdate = new AtomicReference();
    private AtomicReference<V> valueUpdate = new AtomicReference();
    private volatile boolean started = false;
    private ObjectProperty<Worker.State> state = new SimpleObjectProperty<Worker.State>(this, "state", Worker.State.READY);
    private final ObjectProperty<V> value = new SimpleObjectProperty<V>(this, "value");
    private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<Throwable>(this, "exception");
    private final DoubleProperty workDone = new SimpleDoubleProperty(this, "workDone", -1.0);
    private final DoubleProperty totalWork = new SimpleDoubleProperty(this, "totalWork", -1.0);
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1.0);
    private final BooleanProperty running = new SimpleBooleanProperty(this, "running", false);
    private final StringProperty message = new SimpleStringProperty(this, "message", "");
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private static final Permission modifyThreadPerm = new RuntimePermission("modifyThread");
    private EventHelper eventHelper = null;

    public Task() {
        this(new TaskCallable());
    }

    private Task(TaskCallable<V> taskCallable) {
        super(taskCallable);
        ((TaskCallable)taskCallable).task = this;
    }

    protected abstract V call() throws Exception;

    final void setState(Worker.State state) {
        this.checkThread();
        Worker.State state2 = this.getState();
        if (state2 != Worker.State.CANCELLED) {
            this.state.set(state);
            this.setRunning(state == Worker.State.SCHEDULED || state == Worker.State.RUNNING);
            switch ((Worker.State)((Object)this.state.get())) {
                case CANCELLED: {
                    this.fireEvent(new WorkerStateEvent(this, WorkerStateEvent.WORKER_STATE_CANCELLED));
                    this.cancelled();
                    break;
                }
                case FAILED: {
                    this.fireEvent(new WorkerStateEvent(this, WorkerStateEvent.WORKER_STATE_FAILED));
                    this.failed();
                    break;
                }
                case READY: {
                    break;
                }
                case RUNNING: {
                    this.fireEvent(new WorkerStateEvent(this, WorkerStateEvent.WORKER_STATE_RUNNING));
                    this.running();
                    break;
                }
                case SCHEDULED: {
                    this.fireEvent(new WorkerStateEvent(this, WorkerStateEvent.WORKER_STATE_SCHEDULED));
                    this.scheduled();
                    break;
                }
                case SUCCEEDED: {
                    this.fireEvent(new WorkerStateEvent(this, WorkerStateEvent.WORKER_STATE_SUCCEEDED));
                    this.succeeded();
                    break;
                }
                default: {
                    throw new AssertionError((Object)"Should be unreachable");
                }
            }
        }
    }

    @Override
    public final Worker.State getState() {
        this.checkThread();
        return (Worker.State)((Object)this.state.get());
    }

    @Override
    public final ReadOnlyObjectProperty<Worker.State> stateProperty() {
        this.checkThread();
        return this.state;
    }

    public final ObjectProperty<EventHandler<WorkerStateEvent>> onScheduledProperty() {
        this.checkThread();
        return this.getEventHelper().onScheduledProperty();
    }

    public final EventHandler<WorkerStateEvent> getOnScheduled() {
        this.checkThread();
        return this.eventHelper == null ? null : this.eventHelper.getOnScheduled();
    }

    public final void setOnScheduled(EventHandler<WorkerStateEvent> eventHandler) {
        this.checkThread();
        this.getEventHelper().setOnScheduled(eventHandler);
    }

    protected void scheduled() {
    }

    public final ObjectProperty<EventHandler<WorkerStateEvent>> onRunningProperty() {
        this.checkThread();
        return this.getEventHelper().onRunningProperty();
    }

    public final EventHandler<WorkerStateEvent> getOnRunning() {
        this.checkThread();
        return this.eventHelper == null ? null : this.eventHelper.getOnRunning();
    }

    public final void setOnRunning(EventHandler<WorkerStateEvent> eventHandler) {
        this.checkThread();
        this.getEventHelper().setOnRunning(eventHandler);
    }

    protected void running() {
    }

    public final ObjectProperty<EventHandler<WorkerStateEvent>> onSucceededProperty() {
        this.checkThread();
        return this.getEventHelper().onSucceededProperty();
    }

    public final EventHandler<WorkerStateEvent> getOnSucceeded() {
        this.checkThread();
        return this.eventHelper == null ? null : this.eventHelper.getOnSucceeded();
    }

    public final void setOnSucceeded(EventHandler<WorkerStateEvent> eventHandler) {
        this.checkThread();
        this.getEventHelper().setOnSucceeded(eventHandler);
    }

    protected void succeeded() {
    }

    public final ObjectProperty<EventHandler<WorkerStateEvent>> onCancelledProperty() {
        this.checkThread();
        return this.getEventHelper().onCancelledProperty();
    }

    public final EventHandler<WorkerStateEvent> getOnCancelled() {
        this.checkThread();
        return this.eventHelper == null ? null : this.eventHelper.getOnCancelled();
    }

    public final void setOnCancelled(EventHandler<WorkerStateEvent> eventHandler) {
        this.checkThread();
        this.getEventHelper().setOnCancelled(eventHandler);
    }

    protected void cancelled() {
    }

    public final ObjectProperty<EventHandler<WorkerStateEvent>> onFailedProperty() {
        this.checkThread();
        return this.getEventHelper().onFailedProperty();
    }

    public final EventHandler<WorkerStateEvent> getOnFailed() {
        this.checkThread();
        return this.eventHelper == null ? null : this.eventHelper.getOnFailed();
    }

    public final void setOnFailed(EventHandler<WorkerStateEvent> eventHandler) {
        this.checkThread();
        this.getEventHelper().setOnFailed(eventHandler);
    }

    protected void failed() {
    }

    private void setValue(V v) {
        this.checkThread();
        this.value.set(v);
    }

    @Override
    public final V getValue() {
        this.checkThread();
        return (V)this.value.get();
    }

    @Override
    public final ReadOnlyObjectProperty<V> valueProperty() {
        this.checkThread();
        return this.value;
    }

    private void _setException(Throwable throwable) {
        this.checkThread();
        this.exception.set(throwable);
    }

    @Override
    public final Throwable getException() {
        this.checkThread();
        return (Throwable)this.exception.get();
    }

    @Override
    public final ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        this.checkThread();
        return this.exception;
    }

    private void setWorkDone(double d) {
        this.checkThread();
        this.workDone.set(d);
    }

    @Override
    public final double getWorkDone() {
        this.checkThread();
        return this.workDone.get();
    }

    @Override
    public final ReadOnlyDoubleProperty workDoneProperty() {
        this.checkThread();
        return this.workDone;
    }

    private void setTotalWork(double d) {
        this.checkThread();
        this.totalWork.set(d);
    }

    @Override
    public final double getTotalWork() {
        this.checkThread();
        return this.totalWork.get();
    }

    @Override
    public final ReadOnlyDoubleProperty totalWorkProperty() {
        this.checkThread();
        return this.totalWork;
    }

    private void setProgress(double d) {
        this.checkThread();
        this.progress.set(d);
    }

    @Override
    public final double getProgress() {
        this.checkThread();
        return this.progress.get();
    }

    @Override
    public final ReadOnlyDoubleProperty progressProperty() {
        this.checkThread();
        return this.progress;
    }

    private void setRunning(boolean bl) {
        this.checkThread();
        this.running.set(bl);
    }

    @Override
    public final boolean isRunning() {
        this.checkThread();
        return this.running.get();
    }

    @Override
    public final ReadOnlyBooleanProperty runningProperty() {
        this.checkThread();
        return this.running;
    }

    @Override
    public final String getMessage() {
        this.checkThread();
        return (String)this.message.get();
    }

    @Override
    public final ReadOnlyStringProperty messageProperty() {
        this.checkThread();
        return this.message;
    }

    @Override
    public final String getTitle() {
        this.checkThread();
        return (String)this.title.get();
    }

    @Override
    public final ReadOnlyStringProperty titleProperty() {
        this.checkThread();
        return this.title;
    }

    @Override
    public final boolean cancel() {
        return this.cancel(true);
    }

    @Override
    public boolean cancel(boolean bl) {
        boolean bl2 = AccessController.doPrivileged(() -> super.cancel(bl), null, modifyThreadPerm);
        if (bl2) {
            if (this.isFxApplicationThread()) {
                this.setState(Worker.State.CANCELLED);
            } else {
                this.runLater(() -> this.setState(Worker.State.CANCELLED));
            }
        }
        return bl2;
    }

    protected void updateProgress(long l, long l2) {
        this.updateProgress((double)l, (double)l2);
    }

    protected void updateProgress(double d, double d2) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            d = -1.0;
        }
        if (Double.isInfinite(d2) || Double.isNaN(d2)) {
            d2 = -1.0;
        }
        if (d < 0.0) {
            d = -1.0;
        }
        if (d2 < 0.0) {
            d2 = -1.0;
        }
        if (d > d2) {
            d = d2;
        }
        if (this.isFxApplicationThread()) {
            this._updateProgress(d, d2);
        } else if (this.progressUpdate.getAndSet(new ProgressUpdate(d, d2)) == null) {
            this.runLater(() -> {
                ProgressUpdate progressUpdate = this.progressUpdate.getAndSet(null);
                this._updateProgress(progressUpdate.workDone, progressUpdate.totalWork);
            });
        }
    }

    private void _updateProgress(double d, double d2) {
        this.setTotalWork(d2);
        this.setWorkDone(d);
        if (d == -1.0) {
            this.setProgress(-1.0);
        } else {
            this.setProgress(d / d2);
        }
    }

    protected void updateMessage(String string) {
        if (this.isFxApplicationThread()) {
            this.message.set(string);
        } else if (this.messageUpdate.getAndSet(string) == null) {
            this.runLater(new Runnable(){

                @Override
                public void run() {
                    String string = Task.this.messageUpdate.getAndSet(null);
                    Task.this.message.set(string);
                }
            });
        }
    }

    protected void updateTitle(String string) {
        if (this.isFxApplicationThread()) {
            this.title.set(string);
        } else if (this.titleUpdate.getAndSet(string) == null) {
            this.runLater(new Runnable(){

                @Override
                public void run() {
                    String string = Task.this.titleUpdate.getAndSet(null);
                    Task.this.title.set(string);
                }
            });
        }
    }

    protected void updateValue(V v) {
        if (this.isFxApplicationThread()) {
            this.value.set(v);
        } else if (this.valueUpdate.getAndSet(v) == null) {
            this.runLater(() -> this.value.set(this.valueUpdate.getAndSet(null)));
        }
    }

    private void checkThread() {
        if (this.started && !this.isFxApplicationThread()) {
            throw new IllegalStateException("Task must only be used from the FX Application Thread");
        }
    }

    void runLater(Runnable runnable) {
        Platform.runLater(runnable);
    }

    boolean isFxApplicationThread() {
        return Platform.isFxApplicationThread();
    }

    private EventHelper getEventHelper() {
        if (this.eventHelper == null) {
            this.eventHelper = new EventHelper(this);
        }
        return this.eventHelper;
    }

    public final <T extends Event> void addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.checkThread();
        this.getEventHelper().addEventHandler(eventType, eventHandler);
    }

    public final <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.checkThread();
        this.getEventHelper().removeEventHandler(eventType, eventHandler);
    }

    public final <T extends Event> void addEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.checkThread();
        this.getEventHelper().addEventFilter(eventType, eventHandler);
    }

    public final <T extends Event> void removeEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.checkThread();
        this.getEventHelper().removeEventFilter(eventType, eventHandler);
    }

    protected final <T extends Event> void setEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.checkThread();
        this.getEventHelper().setEventHandler(eventType, eventHandler);
    }

    public final void fireEvent(Event event) {
        this.checkThread();
        this.getEventHelper().fireEvent(event);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        this.checkThread();
        return this.getEventHelper().buildEventDispatchChain(eventDispatchChain);
    }

    private static final class TaskCallable<V>
    implements Callable<V> {
        private Task<V> task;

        private TaskCallable() {
        }

        @Override
        public V call() throws Exception {
            ((Task)this.task).started = true;
            this.task.runLater(() -> {
                this.task.setState(Worker.State.SCHEDULED);
                this.task.setState(Worker.State.RUNNING);
            });
            try {
                V v = this.task.call();
                if (!this.task.isCancelled()) {
                    this.task.runLater(() -> {
                        this.task.updateValue(v);
                        this.task.setState(Worker.State.SUCCEEDED);
                    });
                    return v;
                }
                return null;
            }
            catch (Throwable throwable) {
                this.task.runLater(() -> {
                    ((Task)this.task)._setException(throwable);
                    this.task.setState(Worker.State.FAILED);
                });
                if (throwable instanceof Exception) {
                    throw (Exception)throwable;
                }
                throw new Exception(throwable);
            }
        }
    }

    private static final class ProgressUpdate {
        private final double workDone;
        private final double totalWork;

        private ProgressUpdate(double d, double d2) {
            this.workDone = d;
            this.totalWork = d2;
        }
    }
}

