/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class InputEvent
extends Event {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<InputEvent> ANY = new EventType<Event>(Event.ANY, "INPUT");

    public InputEvent(@NamedArg(value="eventType") EventType<? extends InputEvent> eventType) {
        super(eventType);
    }

    public InputEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<? extends InputEvent> eventType) {
        super(object, eventTarget, eventType);
    }

    public EventType<? extends InputEvent> getEventType() {
        return super.getEventType();
    }
}

