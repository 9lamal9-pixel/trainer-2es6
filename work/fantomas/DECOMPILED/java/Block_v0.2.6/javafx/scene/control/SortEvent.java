/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class SortEvent<C>
extends Event {
    public static final EventType<SortEvent> ANY = new EventType<Event>(Event.ANY, "SORT");
    private static final EventType<?> SORT_EVENT = new EventType<SortEvent>(ANY, "SORT_EVENT");

    public static <C> EventType<SortEvent<C>> sortEvent() {
        return SORT_EVENT;
    }

    public SortEvent(@NamedArg(value="source") C c, @NamedArg(value="target") EventTarget eventTarget) {
        super(c, eventTarget, SortEvent.sortEvent());
    }

    public C getSource() {
        return (C)super.getSource();
    }
}

