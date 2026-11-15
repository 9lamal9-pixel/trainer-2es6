/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.stage;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public final class FocusUngrabEvent
extends Event {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<FocusUngrabEvent> FOCUS_UNGRAB = new EventType<Event>(Event.ANY, "FOCUS_UNGRAB");
    public static final EventType<FocusUngrabEvent> ANY = FOCUS_UNGRAB;

    public FocusUngrabEvent() {
        super(FOCUS_UNGRAB);
    }

    public FocusUngrabEvent(Object object, EventTarget eventTarget) {
        super(object, eventTarget, FOCUS_UNGRAB);
    }
}

