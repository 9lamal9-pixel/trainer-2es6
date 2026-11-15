/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.control.Dialog;

public class DialogEvent
extends Event {
    private static final long serialVersionUID = 20140716L;
    public static final EventType<DialogEvent> ANY = new EventType<Event>(Event.ANY, "DIALOG");
    public static final EventType<DialogEvent> DIALOG_SHOWING = new EventType<DialogEvent>(ANY, "DIALOG_SHOWING");
    public static final EventType<DialogEvent> DIALOG_SHOWN = new EventType<DialogEvent>(ANY, "DIALOG_SHOWN");
    public static final EventType<DialogEvent> DIALOG_HIDING = new EventType<DialogEvent>(ANY, "DIALOG_HIDING");
    public static final EventType<DialogEvent> DIALOG_HIDDEN = new EventType<DialogEvent>(ANY, "DIALOG_HIDDEN");
    public static final EventType<DialogEvent> DIALOG_CLOSE_REQUEST = new EventType<DialogEvent>(ANY, "DIALOG_CLOSE_REQUEST");

    public DialogEvent(@NamedArg(value="source") Dialog<?> dialog, @NamedArg(value="eventType") EventType<? extends Event> eventType) {
        super(dialog, dialog, eventType);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("DialogEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        return stringBuilder.append("]").toString();
    }

    @Override
    public DialogEvent copyFor(Object object, EventTarget eventTarget) {
        return (DialogEvent)super.copyFor(object, eventTarget);
    }

    public DialogEvent copyFor(Object object, EventTarget eventTarget, EventType<DialogEvent> eventType) {
        DialogEvent dialogEvent = this.copyFor(object, eventTarget);
        dialogEvent.eventType = eventType;
        return dialogEvent;
    }

    public EventType<DialogEvent> getEventType() {
        return super.getEventType();
    }
}

