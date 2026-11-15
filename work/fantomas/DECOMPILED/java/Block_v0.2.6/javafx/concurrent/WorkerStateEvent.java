/*
 * Decompiled with CFR 0.152.
 */
package javafx.concurrent;

import javafx.beans.NamedArg;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class WorkerStateEvent
extends Event {
    public static final EventType<WorkerStateEvent> ANY = new EventType<Event>(Event.ANY, "WORKER_STATE");
    public static final EventType<WorkerStateEvent> WORKER_STATE_READY = new EventType<WorkerStateEvent>(ANY, "WORKER_STATE_READY");
    public static final EventType<WorkerStateEvent> WORKER_STATE_SCHEDULED = new EventType<WorkerStateEvent>(ANY, "WORKER_STATE_SCHEDULED");
    public static final EventType<WorkerStateEvent> WORKER_STATE_RUNNING = new EventType<WorkerStateEvent>(ANY, "WORKER_STATE_RUNNING");
    public static final EventType<WorkerStateEvent> WORKER_STATE_SUCCEEDED = new EventType<WorkerStateEvent>(ANY, "WORKER_STATE_SUCCEEDED");
    public static final EventType<WorkerStateEvent> WORKER_STATE_CANCELLED = new EventType<WorkerStateEvent>(ANY, "WORKER_STATE_CANCELLED");
    public static final EventType<WorkerStateEvent> WORKER_STATE_FAILED = new EventType<WorkerStateEvent>(ANY, "WORKER_STATE_FAILED");

    public WorkerStateEvent(@NamedArg(value="worker") Worker worker, @NamedArg(value="eventType") EventType<? extends WorkerStateEvent> eventType) {
        super(worker, worker instanceof EventTarget ? (EventTarget)((Object)worker) : null, eventType);
    }

    @Override
    public Worker getSource() {
        return (Worker)super.getSource();
    }
}

