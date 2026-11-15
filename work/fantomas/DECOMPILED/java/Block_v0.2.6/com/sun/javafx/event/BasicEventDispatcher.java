/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.event;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;

public abstract class BasicEventDispatcher
implements EventDispatcher {
    private BasicEventDispatcher previousDispatcher;
    private BasicEventDispatcher nextDispatcher;

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain eventDispatchChain) {
        if ((event = this.dispatchCapturingEvent(event)).isConsumed()) {
            return null;
        }
        if ((event = eventDispatchChain.dispatchEvent(event)) != null && (event = this.dispatchBubblingEvent(event)).isConsumed()) {
            return null;
        }
        return event;
    }

    public Event dispatchCapturingEvent(Event event) {
        return event;
    }

    public Event dispatchBubblingEvent(Event event) {
        return event;
    }

    public final BasicEventDispatcher getPreviousDispatcher() {
        return this.previousDispatcher;
    }

    public final BasicEventDispatcher getNextDispatcher() {
        return this.nextDispatcher;
    }

    public final void insertNextDispatcher(BasicEventDispatcher basicEventDispatcher) {
        if (this.nextDispatcher != null) {
            this.nextDispatcher.previousDispatcher = basicEventDispatcher;
        }
        basicEventDispatcher.nextDispatcher = this.nextDispatcher;
        basicEventDispatcher.previousDispatcher = this;
        this.nextDispatcher = basicEventDispatcher;
    }
}

