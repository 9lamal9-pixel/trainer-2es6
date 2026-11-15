/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class DirectEvent
extends Event {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<DirectEvent> DIRECT = new EventType<Event>(Event.ANY, "DIRECT");
    private final Event originalEvent;

    public DirectEvent(Event event) {
        this(event, (Object)null, null);
    }

    public DirectEvent(Event event, Object object, EventTarget eventTarget) {
        super(object, eventTarget, DIRECT);
        this.originalEvent = event;
    }

    public Event getOriginalEvent() {
        return this.originalEvent;
    }
}

