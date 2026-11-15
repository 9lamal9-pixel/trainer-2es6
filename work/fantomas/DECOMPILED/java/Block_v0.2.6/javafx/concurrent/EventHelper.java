/*
 * Decompiled with CFR 0.152.
 */
package javafx.concurrent;

import com.sun.javafx.event.EventHandlerManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;

class EventHelper {
    private final EventTarget target;
    private final ObjectProperty<EventHandler<WorkerStateEvent>> onReady;
    private final ObjectProperty<EventHandler<WorkerStateEvent>> onScheduled;
    private final ObjectProperty<EventHandler<WorkerStateEvent>> onRunning;
    private final ObjectProperty<EventHandler<WorkerStateEvent>> onSucceeded;
    private final ObjectProperty<EventHandler<WorkerStateEvent>> onCancelled;
    private final ObjectProperty<EventHandler<WorkerStateEvent>> onFailed;
    private EventHandlerManager internalEventDispatcher;

    final ObjectProperty<EventHandler<WorkerStateEvent>> onReadyProperty() {
        return this.onReady;
    }

    final EventHandler<WorkerStateEvent> getOnReady() {
        return (EventHandler)this.onReady.get();
    }

    final void setOnReady(EventHandler<WorkerStateEvent> eventHandler) {
        this.onReady.set(eventHandler);
    }

    final ObjectProperty<EventHandler<WorkerStateEvent>> onScheduledProperty() {
        return this.onScheduled;
    }

    final EventHandler<WorkerStateEvent> getOnScheduled() {
        return (EventHandler)this.onScheduled.get();
    }

    final void setOnScheduled(EventHandler<WorkerStateEvent> eventHandler) {
        this.onScheduled.set(eventHandler);
    }

    final ObjectProperty<EventHandler<WorkerStateEvent>> onRunningProperty() {
        return this.onRunning;
    }

    final EventHandler<WorkerStateEvent> getOnRunning() {
        return (EventHandler)this.onRunning.get();
    }

    final void setOnRunning(EventHandler<WorkerStateEvent> eventHandler) {
        this.onRunning.set(eventHandler);
    }

    final ObjectProperty<EventHandler<WorkerStateEvent>> onSucceededProperty() {
        return this.onSucceeded;
    }

    final EventHandler<WorkerStateEvent> getOnSucceeded() {
        return (EventHandler)this.onSucceeded.get();
    }

    final void setOnSucceeded(EventHandler<WorkerStateEvent> eventHandler) {
        this.onSucceeded.set(eventHandler);
    }

    final ObjectProperty<EventHandler<WorkerStateEvent>> onCancelledProperty() {
        return this.onCancelled;
    }

    final EventHandler<WorkerStateEvent> getOnCancelled() {
        return (EventHandler)this.onCancelled.get();
    }

    final void setOnCancelled(EventHandler<WorkerStateEvent> eventHandler) {
        this.onCancelled.set(eventHandler);
    }

    final ObjectProperty<EventHandler<WorkerStateEvent>> onFailedProperty() {
        return this.onFailed;
    }

    final EventHandler<WorkerStateEvent> getOnFailed() {
        return (EventHandler)this.onFailed.get();
    }

    final void setOnFailed(EventHandler<WorkerStateEvent> eventHandler) {
        this.onFailed.set(eventHandler);
    }

    EventHelper(EventTarget eventTarget) {
        this.target = eventTarget;
        this.onReady = new SimpleObjectProperty<EventHandler<WorkerStateEvent>>((Object)eventTarget, "onReady"){

            @Override
            protected void invalidated() {
                EventHandler eventHandler = (EventHandler)this.get();
                EventHelper.this.setEventHandler(WorkerStateEvent.WORKER_STATE_READY, eventHandler);
            }
        };
        this.onScheduled = new SimpleObjectProperty<EventHandler<WorkerStateEvent>>((Object)eventTarget, "onScheduled"){

            @Override
            protected void invalidated() {
                EventHandler eventHandler = (EventHandler)this.get();
                EventHelper.this.setEventHandler(WorkerStateEvent.WORKER_STATE_SCHEDULED, eventHandler);
            }
        };
        this.onRunning = new SimpleObjectProperty<EventHandler<WorkerStateEvent>>((Object)eventTarget, "onRunning"){

            @Override
            protected void invalidated() {
                EventHandler eventHandler = (EventHandler)this.get();
                EventHelper.this.setEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, eventHandler);
            }
        };
        this.onSucceeded = new SimpleObjectProperty<EventHandler<WorkerStateEvent>>((Object)eventTarget, "onSucceeded"){

            @Override
            protected void invalidated() {
                EventHandler eventHandler = (EventHandler)this.get();
                EventHelper.this.setEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, eventHandler);
            }
        };
        this.onCancelled = new SimpleObjectProperty<EventHandler<WorkerStateEvent>>((Object)eventTarget, "onCancelled"){

            @Override
            protected void invalidated() {
                EventHandler eventHandler = (EventHandler)this.get();
                EventHelper.this.setEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, eventHandler);
            }
        };
        this.onFailed = new SimpleObjectProperty<EventHandler<WorkerStateEvent>>((Object)eventTarget, "onFailed"){

            @Override
            protected void invalidated() {
                EventHandler eventHandler = (EventHandler)this.get();
                EventHelper.this.setEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, eventHandler);
            }
        };
    }

    final <T extends Event> void addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().addEventHandler(eventType, eventHandler);
    }

    final <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().removeEventHandler(eventType, eventHandler);
    }

    final <T extends Event> void addEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().addEventFilter(eventType, eventHandler);
    }

    final <T extends Event> void removeEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().removeEventFilter(eventType, eventHandler);
    }

    final <T extends Event> void setEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().setEventHandler(eventType, eventHandler);
    }

    private EventHandlerManager getInternalEventDispatcher() {
        if (this.internalEventDispatcher == null) {
            this.internalEventDispatcher = new EventHandlerManager(this.target);
        }
        return this.internalEventDispatcher;
    }

    final void fireEvent(Event event) {
        Event.fireEvent(this.target, event);
    }

    EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        return this.internalEventDispatcher == null ? eventDispatchChain : eventDispatchChain.append(this.getInternalEventDispatcher());
    }
}

