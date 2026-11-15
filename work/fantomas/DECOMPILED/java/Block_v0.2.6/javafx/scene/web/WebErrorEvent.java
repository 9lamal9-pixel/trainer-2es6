/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventType;

public final class WebErrorEvent
extends Event {
    public static final EventType<WebErrorEvent> ANY = new EventType<Event>(Event.ANY, "WEB_ERROR");
    public static final EventType<WebErrorEvent> USER_DATA_DIRECTORY_ALREADY_IN_USE = new EventType<WebErrorEvent>(ANY, "USER_DATA_DIRECTORY_ALREADY_IN_USE");
    public static final EventType<WebErrorEvent> USER_DATA_DIRECTORY_IO_ERROR = new EventType<WebErrorEvent>(ANY, "USER_DATA_DIRECTORY_IO_ERROR");
    public static final EventType<WebErrorEvent> USER_DATA_DIRECTORY_SECURITY_ERROR = new EventType<WebErrorEvent>(ANY, "USER_DATA_DIRECTORY_SECURITY_ERROR");
    private final String message;
    private final Throwable exception;

    public WebErrorEvent(@NamedArg(value="source") Object object, @NamedArg(value="type") EventType<WebErrorEvent> eventType, @NamedArg(value="message") String string, @NamedArg(value="exception") Throwable throwable) {
        super(object, null, eventType);
        this.message = string;
        this.exception = throwable;
    }

    public String getMessage() {
        return this.message;
    }

    public Throwable getException() {
        return this.exception;
    }

    @Override
    public String toString() {
        return String.format("WebErrorEvent [source = %s, eventType = %s, message = \"%s\", exception = %s]", this.getSource(), this.getEventType(), this.getMessage(), this.getException());
    }
}

