/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventType;

public final class WebEvent<T>
extends Event {
    public static final EventType<WebEvent> ANY = new EventType<Event>(Event.ANY, "WEB");
    public static final EventType<WebEvent> RESIZED = new EventType<WebEvent>(ANY, "WEB_RESIZED");
    public static final EventType<WebEvent> STATUS_CHANGED = new EventType<WebEvent>(ANY, "WEB_STATUS_CHANGED");
    public static final EventType<WebEvent> VISIBILITY_CHANGED = new EventType<WebEvent>(ANY, "WEB_VISIBILITY_CHANGED");
    public static final EventType<WebEvent> ALERT = new EventType<WebEvent>(ANY, "WEB_ALERT");
    private final T data;

    public WebEvent(@NamedArg(value="source") Object object, @NamedArg(value="type") EventType<WebEvent> eventType, @NamedArg(value="data") T t) {
        super(object, null, eventType);
        this.data = t;
    }

    public T getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return String.format("WebEvent [source = %s, eventType = %s, data = %s]", this.getSource(), this.getEventType(), this.getData());
    }
}

