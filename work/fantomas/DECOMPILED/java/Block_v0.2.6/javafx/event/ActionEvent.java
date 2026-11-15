/*
 * Decompiled with CFR 0.152.
 */
package javafx.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class ActionEvent
extends Event {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<ActionEvent> ACTION = new EventType<Event>(Event.ANY, "ACTION");
    public static final EventType<ActionEvent> ANY = ACTION;

    public ActionEvent() {
        super(ACTION);
    }

    public ActionEvent(Object object, EventTarget eventTarget) {
        super(object, eventTarget, ACTION);
    }

    @Override
    public ActionEvent copyFor(Object object, EventTarget eventTarget) {
        return (ActionEvent)super.copyFor(object, eventTarget);
    }

    public EventType<? extends ActionEvent> getEventType() {
        return super.getEventType();
    }
}

