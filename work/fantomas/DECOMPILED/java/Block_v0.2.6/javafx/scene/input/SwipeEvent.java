/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.GestureEvent;
import javafx.scene.input.PickResult;

public final class SwipeEvent
extends GestureEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<SwipeEvent> ANY = new EventType<GestureEvent>(GestureEvent.ANY, "ANY_SWIPE");
    public static final EventType<SwipeEvent> SWIPE_LEFT = new EventType<SwipeEvent>(ANY, "SWIPE_LEFT");
    public static final EventType<SwipeEvent> SWIPE_RIGHT = new EventType<SwipeEvent>(ANY, "SWIPE_RIGHT");
    public static final EventType<SwipeEvent> SWIPE_UP = new EventType<SwipeEvent>(ANY, "SWIPE_UP");
    public static final EventType<SwipeEvent> SWIPE_DOWN = new EventType<SwipeEvent>(ANY, "SWIPE_DOWN");
    private final int touchCount;

    public SwipeEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<SwipeEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="direct") boolean bl5, @NamedArg(value="touchCount") int n, @NamedArg(value="pickResult") PickResult pickResult) {
        super(object, eventTarget, eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, false, pickResult);
        this.touchCount = n;
    }

    public SwipeEvent(@NamedArg(value="eventType") EventType<SwipeEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="direct") boolean bl5, @NamedArg(value="touchCount") int n, @NamedArg(value="pickResult") PickResult pickResult) {
        this(null, null, eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, n, pickResult);
    }

    public int getTouchCount() {
        return this.touchCount;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("SwipeEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        stringBuilder.append(", touchCount = ").append(this.getTouchCount());
        stringBuilder.append(", x = ").append(this.getX()).append(", y = ").append(this.getY()).append(", z = ").append(this.getZ());
        stringBuilder.append(this.isDirect() ? ", direct" : ", indirect");
        if (this.isShiftDown()) {
            stringBuilder.append(", shiftDown");
        }
        if (this.isControlDown()) {
            stringBuilder.append(", controlDown");
        }
        if (this.isAltDown()) {
            stringBuilder.append(", altDown");
        }
        if (this.isMetaDown()) {
            stringBuilder.append(", metaDown");
        }
        if (this.isShortcutDown()) {
            stringBuilder.append(", shortcutDown");
        }
        stringBuilder.append(", pickResult = ").append(this.getPickResult());
        return stringBuilder.append("]").toString();
    }

    @Override
    public SwipeEvent copyFor(Object object, EventTarget eventTarget) {
        return (SwipeEvent)super.copyFor(object, eventTarget);
    }

    public SwipeEvent copyFor(Object object, EventTarget eventTarget, EventType<SwipeEvent> eventType) {
        SwipeEvent swipeEvent = this.copyFor(object, eventTarget);
        swipeEvent.eventType = eventType;
        return swipeEvent;
    }

    public EventType<SwipeEvent> getEventType() {
        return super.getEventType();
    }
}

