/*
 * Decompiled with CFR 0.152.
 */
package javafx.stage;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.stage.Window;

public class WindowEvent
extends Event {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<WindowEvent> ANY = new EventType<Event>(Event.ANY, "WINDOW");
    public static final EventType<WindowEvent> WINDOW_SHOWING = new EventType<WindowEvent>(ANY, "WINDOW_SHOWING");
    public static final EventType<WindowEvent> WINDOW_SHOWN = new EventType<WindowEvent>(ANY, "WINDOW_SHOWN");
    public static final EventType<WindowEvent> WINDOW_HIDING = new EventType<WindowEvent>(ANY, "WINDOW_HIDING");
    public static final EventType<WindowEvent> WINDOW_HIDDEN = new EventType<WindowEvent>(ANY, "WINDOW_HIDDEN");
    public static final EventType<WindowEvent> WINDOW_CLOSE_REQUEST = new EventType<WindowEvent>(ANY, "WINDOW_CLOSE_REQUEST");

    public WindowEvent(@NamedArg(value="source") Window window, @NamedArg(value="eventType") EventType<? extends Event> eventType) {
        super(window, window, eventType);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("WindowEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        return stringBuilder.append("]").toString();
    }

    @Override
    public WindowEvent copyFor(Object object, EventTarget eventTarget) {
        return (WindowEvent)super.copyFor(object, eventTarget);
    }

    public WindowEvent copyFor(Object object, EventTarget eventTarget, EventType<WindowEvent> eventType) {
        WindowEvent windowEvent = this.copyFor(object, eventTarget);
        windowEvent.eventType = eventType;
        return windowEvent;
    }

    public EventType<WindowEvent> getEventType() {
        return super.getEventType();
    }
}

