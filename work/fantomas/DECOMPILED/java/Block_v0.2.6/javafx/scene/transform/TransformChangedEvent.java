/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.transform;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public final class TransformChangedEvent
extends Event {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<TransformChangedEvent> TRANSFORM_CHANGED = new EventType<Event>(Event.ANY, "TRANSFORM_CHANGED");
    public static final EventType<TransformChangedEvent> ANY = TRANSFORM_CHANGED;

    public TransformChangedEvent() {
        super(TRANSFORM_CHANGED);
    }

    public TransformChangedEvent(Object object, EventTarget eventTarget) {
        super(object, eventTarget, TRANSFORM_CHANGED);
    }
}

