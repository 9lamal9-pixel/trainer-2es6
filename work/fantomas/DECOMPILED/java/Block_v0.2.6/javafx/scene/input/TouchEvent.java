/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import java.util.Collections;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.InputEvent;
import javafx.scene.input.TouchPoint;

public final class TouchEvent
extends InputEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<TouchEvent> ANY = new EventType<InputEvent>(InputEvent.ANY, "TOUCH");
    public static final EventType<TouchEvent> TOUCH_PRESSED = new EventType<TouchEvent>(ANY, "TOUCH_PRESSED");
    public static final EventType<TouchEvent> TOUCH_MOVED = new EventType<TouchEvent>(ANY, "TOUCH_MOVED");
    public static final EventType<TouchEvent> TOUCH_RELEASED = new EventType<TouchEvent>(ANY, "TOUCH_RELEASED");
    public static final EventType<TouchEvent> TOUCH_STATIONARY = new EventType<TouchEvent>(ANY, "TOUCH_STATIONARY");
    private final int eventSetId;
    private final boolean shiftDown;
    private final boolean controlDown;
    private final boolean altDown;
    private final boolean metaDown;
    private final TouchPoint touchPoint;
    private final List<TouchPoint> touchPoints;

    public TouchEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<TouchEvent> eventType, @NamedArg(value="touchPoint") TouchPoint touchPoint, @NamedArg(value="touchPoints") List<TouchPoint> list, @NamedArg(value="eventSetId") int n, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4) {
        super(object, eventTarget, (EventType<? extends InputEvent>)eventType);
        this.touchPoints = list != null ? Collections.unmodifiableList(list) : null;
        this.eventSetId = n;
        this.shiftDown = bl;
        this.controlDown = bl2;
        this.altDown = bl3;
        this.metaDown = bl4;
        this.touchPoint = touchPoint;
    }

    public TouchEvent(@NamedArg(value="eventType") EventType<TouchEvent> eventType, @NamedArg(value="touchPoint") TouchPoint touchPoint, @NamedArg(value="touchPoints") List<TouchPoint> list, @NamedArg(value="eventSetId") int n, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4) {
        this(null, null, eventType, touchPoint, list, n, bl, bl2, bl3, bl4);
    }

    public int getTouchCount() {
        return this.touchPoints.size();
    }

    private static void recomputeToSource(TouchEvent touchEvent, Object object, Object object2) {
        for (TouchPoint touchPoint : touchEvent.touchPoints) {
            touchPoint.recomputeToSource(object, object2);
        }
    }

    @Override
    public TouchEvent copyFor(Object object, EventTarget eventTarget) {
        TouchEvent touchEvent = (TouchEvent)super.copyFor(object, eventTarget);
        TouchEvent.recomputeToSource(touchEvent, this.getSource(), object);
        return touchEvent;
    }

    public TouchEvent copyFor(Object object, EventTarget eventTarget, EventType<TouchEvent> eventType) {
        TouchEvent touchEvent = this.copyFor(object, eventTarget);
        touchEvent.eventType = eventType;
        return touchEvent;
    }

    public EventType<TouchEvent> getEventType() {
        return super.getEventType();
    }

    public final int getEventSetId() {
        return this.eventSetId;
    }

    public final boolean isShiftDown() {
        return this.shiftDown;
    }

    public final boolean isControlDown() {
        return this.controlDown;
    }

    public final boolean isAltDown() {
        return this.altDown;
    }

    public final boolean isMetaDown() {
        return this.metaDown;
    }

    public TouchPoint getTouchPoint() {
        return this.touchPoint;
    }

    public List<TouchPoint> getTouchPoints() {
        return this.touchPoints;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("TouchEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        stringBuilder.append(", touchCount = ").append(this.getTouchCount());
        stringBuilder.append(", eventSetId = ").append(this.getEventSetId());
        stringBuilder.append(", touchPoint = ").append(this.getTouchPoint().toString());
        return stringBuilder.append("]").toString();
    }
}

