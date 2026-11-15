/*
 * Decompiled with CFR 0.152.
 */
package javafx.concurrent;

import com.sun.javafx.logging.PlatformLogger;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;

public abstract class Service<V>
implements Worker<V>,
EventTarget {
    private static final PlatformLogger LOG = PlatformLogger.getLogger(Service.class.getName());
    private static final int THREAD_POOL_SIZE = 32;
    private static final long THREAD_TIME_OUT = 1000L;
    private static final BlockingQueue<Runnable> IO_QUEUE = new LinkedBlockingQueue<Runnable>(){

        @Override
        public boolean offer(Runnable runnable) {
            if (EXECUTOR.getPoolSize() < 32) {
                return false;
            }
            return super.offer(runnable);
        }
    };
    private static final ThreadGroup THREAD_GROUP = AccessController.doPrivileged(() -> new ThreadGroup("javafx concurrent thread pool"));
    private static final Thread.UncaughtExceptionHandler UNCAUGHT_HANDLER = (thread, throwable) -> {
        if (!(throwable instanceof IllegalMonitorStateException)) {
            LOG.warning("Uncaught throwable in " + THREAD_GROUP.getName(), throwable);
        }
    };
    private static final ThreadFactory THREAD_FACTORY = runnable -> AccessController.doPrivileged(() -> {
        Thread thread = new Thread(THREAD_GROUP, runnable);
        thread.setUncaughtExceptionHandler(UNCAUGHT_HANDLER);
        thread.setPriority(1);
        thread.setDaemon(true);
        return thread;
    });
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 32, 1000L, TimeUnit.MILLISECONDS, IO_QUEUE, THREAD_FACTORY, new ThreadPoolExecutor.AbortPolicy());
    private final ObjectProperty<Worker.State> state = new SimpleObjectProperty<Worker.State>(this, "state", Worker.State.READY);
    private final ObjectProperty<V> value = new SimpleObjectProperty<V>(this, "value");
    private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<Throwable>(this, "exception");
    private final DoubleProperty workDone = new SimpleDoubleProperty(this, "workDone", -1.0);
    private final DoubleProperty totalWorkToBeDone = new SimpleDoubleProperty(this, "totalWork", -1.0);
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1.0);
    private final BooleanProperty running = new SimpleBooleanProperty(this, "running", false);
    private final StringProperty message = new SimpleStringProperty(this, "message", "");
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final ObjectProperty<Executor> executor = new SimpleObjectProperty<Executor>(this, "executor");
    private Task<V> task;
    private volatile boolean startedOnce = false;
    private EventHelper eventHelper = null;

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

    @Override
    public final double getTotalWork() {
        this.checkThread();
        return this.totalWorkToBeDone.get();
    }

    @Override
    public final ReadOnlyDoubleProperty totalWorkProperty() {
        this.checkThread();
        return this.totalWorkToBeDone;
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

    public final void setExecutor(Executor executor) {
        this.checkThread();
        this.executor.set(executor);
    }

    public final Executor getExecutor() {
        this.checkThread();
        return (Executor)this.executor.get();
    }

    public final ObjectProperty<Executor> executorProperty() {
        this.checkThread();
        return this.executor;
    }

    public final ObjectProperty<EventHandler<WorkerStateEvent>> onReadyProperty() {
        this.checkThread();
        return this.getEventHelper().onReadyProperty();
    }

    public final EventHandler<WorkerStateEvent> getOnReady() {
        this.checkThread();
        return this.eventHelper == null ? null : this.eventHelper.getOnReady();
    }

    public final void setOnReady(EventHandler<WorkerStateEvent> eventHandler) {
        this.checkThread();
        this.getEventHelper().setOnReady(eventHandler);
    }

    protected void ready() {
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

    protected Service() {
        this.state.addListener((observableValue, state, state2) -> {
            switch (state2) {
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
                    this.fireEvent(new WorkerStateEvent(this, WorkerStateEvent.WORKER_STATE_READY));
                    this.ready();
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
        });
    }

    @Override
    public boolean cancel() {
        this.checkThread();
        if (this.task == null) {
            if (this.state.get() == Worker.State.CANCELLED || this.state.get() == Worker.State.SUCCEEDED) {
                return false;
            }
            this.state.set(Worker.State.CANCELLED);
            return true;
        }
        return this.task.cancel(true);
    }

    public void restart() {
        this.checkThread();
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
            this.state.unbind();
            this.state.set(Worker.State.CANCELLED);
        }
        this.reset();
        this.start();
    }

    public void reset() {
        this.checkThread();
        Worker.State state = this.getState();
        if (state == Worker.State.SCHEDULED || state == Worker.State.RUNNING) {
            throw new IllegalStateException();
        }
        this.task = null;
        this.state.unbind();
        this.state.set(Worker.State.READY);
        this.value.unbind();
        this.value.set(null);
        this.exception.unbind();
        this.exception.set(null);
        this.workDone.unbind();
        this.workDone.set(-1.0);
        this.totalWorkToBeDone.unbind();
        this.totalWorkToBeDone.set(-1.0);
        this.progress.unbind();
        this.progress.set(-1.0);
        this.running.unbind();
        this.running.set(false);
        this.message.unbind();
        this.message.set("");
        this.title.unbind();
        this.title.set("");
    }

    public void start() {
        this.checkThread();
        if (this.getState() != Worker.State.READY) {
            throw new IllegalStateException("Can only start a Service in the READY state. Was in state " + this.getState());
        }
        this.task = this.createTask();
        this.state.bind(this.task.stateProperty());
        this.value.bind(this.task.valueProperty());
        this.exception.bind(this.task.exceptionProperty());
        this.workDone.bind(this.task.workDoneProperty());
        this.totalWorkToBeDone.bind(this.task.totalWorkProperty());
        this.progress.bind(this.task.progressProperty());
        this.running.bind(this.task.runningProperty());
        this.message.bind(this.task.messageProperty());
        this.title.bind(this.task.titleProperty());
        this.startedOnce = true;
        if (!this.isFxApplicationThread()) {
            this.runLater(() -> {
                this.task.setState(Worker.State.SCHEDULED);
                this.executeTask(this.task);
            });
        } else {
            this.task.setState(Worker.State.SCHEDULED);
            this.executeTask(this.task);
        }
    }

    void cancelFromReadyState() {
        this.state.set(Worker.State.SCHEDULED);
        this.state.set(Worker.State.CANCELLED);
    }

    protected void executeTask(Task<V> task) {
        AccessControlContext accessControlContext = AccessController.getContext();
        Executor executor = this.getExecutor() != null ? this.getExecutor() : EXECUTOR;
        executor.execute(() -> AccessController.doPrivileged(() -> {
            task.run();
            return null;
        }, accessControlContext));
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

    protected final void fireEvent(Event event) {
        this.checkThread();
        this.getEventHelper().fireEvent(event);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        this.checkThread();
        return this.getEventHelper().buildEventDispatchChain(eventDispatchChain);
    }

    protected abstract Task<V> createTask();

    void checkThread() {
        if (this.startedOnce && !this.isFxApplicationThread()) {
            throw new IllegalStateException("Service must only be used from the FX Application Thread");
        }
    }

    void runLater(Runnable runnable) {
        Platform.runLater(runnable);
    }

    boolean isFxApplicationThread() {
        return Platform.isFxApplicationThread();
    }

    static {
        EXECUTOR.allowCoreThreadTimeOut(true);
    }
}

