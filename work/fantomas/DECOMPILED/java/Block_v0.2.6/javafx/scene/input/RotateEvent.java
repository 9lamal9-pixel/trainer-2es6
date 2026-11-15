/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.GestureEvent;
import javafx.scene.input.PickResult;

public final class RotateEvent
extends GestureEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<RotateEvent> ANY = new EventType<GestureEvent>(GestureEvent.ANY, "ANY_ROTATE");
    public static final EventType<RotateEvent> ROTATE = new EventType<RotateEvent>(ANY, "ROTATE");
    public static final EventType<RotateEvent> ROTATION_STARTED = new EventType<RotateEvent>(ANY, "ROTATION_STARTED");
    public static final EventType<RotateEvent> ROTATION_FINISHED = new EventType<RotateEvent>(ANY, "ROTATION_FINISHED");
    private final double angle;
    private final double totalAngle;

    public RotateEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<RotateEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="direct") boolean bl5, @NamedArg(value="inertia") boolean bl6, @NamedArg(value="angle") double d5, @NamedArg(value="totalAngle") double d6, @NamedArg(value="pickResult") PickResult pickResult) {
        super(object, eventTarget, eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, bl6, pickResult);
        this.angle = d5;
        this.totalAngle = d6;
    }

    public RotateEvent(@NamedArg(value="eventType") EventType<RotateEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="direct") boolean bl5, @NamedArg(value="inertia") boolean bl6, @NamedArg(value="angle") double d5, @NamedArg(value="totalAngle") double d6, @NamedArg(value="pickResult") PickResult pickResult) {
        this(null, null, eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, bl6, d5, d6, pickResult);
    }

    public double getAngle() {
        return this.angle;
    }

    public double getTotalAngle() {
        return this.totalAngle;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("RotateEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        stringBuilder.append(", angle = ").append(this.getAngle());
        stringBuilder.append(", totalAngle = ").append(this.getTotalAngle());
        stringBuilder.append(", x = ").append(this.getX()).append(", y = ").append(this.getY()).append(", z = ").append(this.getZ());
        stringBuilder.append(this.isDirect() ? ", direct" : ", indirect");
        if (this.isInertia()) {
            stringBuilder.append(", inertia");
        }
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
    public RotateEvent copyFor(Object object, EventTarget eventTarget) {
        return (RotateEvent)super.copyFor(object, eventTarget);
    }

    public RotateEvent copyFor(Object object, EventTarget eventTarget, EventType<RotateEvent> eventType) {
        RotateEvent rotateEvent = this.copyFor(object, eventTarget);
        rotateEvent.eventType = eventType;
        return rotateEvent;
    }

    public EventType<RotateEvent> getEventType() {
        return super.getEventType();
    }
}

