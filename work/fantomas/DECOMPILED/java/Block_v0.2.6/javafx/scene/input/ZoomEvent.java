/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.GestureEvent;
import javafx.scene.input.PickResult;

public final class ZoomEvent
extends GestureEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<ZoomEvent> ANY = new EventType<GestureEvent>(GestureEvent.ANY, "ANY_ZOOM");
    public static final EventType<ZoomEvent> ZOOM = new EventType<ZoomEvent>(ANY, "ZOOM");
    public static final EventType<ZoomEvent> ZOOM_STARTED = new EventType<ZoomEvent>(ANY, "ZOOM_STARTED");
    public static final EventType<ZoomEvent> ZOOM_FINISHED = new EventType<ZoomEvent>(ANY, "ZOOM_FINISHED");
    private final double zoomFactor;
    private final double totalZoomFactor;

    public ZoomEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<ZoomEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="direct") boolean bl5, @NamedArg(value="inertia") boolean bl6, @NamedArg(value="zoomFactor") double d5, @NamedArg(value="totalZoomFactor") double d6, @NamedArg(value="pickResult") PickResult pickResult) {
        super(object, eventTarget, eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, bl6, pickResult);
        this.zoomFactor = d5;
        this.totalZoomFactor = d6;
    }

    public ZoomEvent(@NamedArg(value="eventType") EventType<ZoomEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="direct") boolean bl5, @NamedArg(value="inertia") boolean bl6, @NamedArg(value="zoomFactor") double d5, @NamedArg(value="totalZoomFactor") double d6, @NamedArg(value="pickResult") PickResult pickResult) {
        this(null, null, eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, bl6, d5, d6, pickResult);
    }

    public double getZoomFactor() {
        return this.zoomFactor;
    }

    public double getTotalZoomFactor() {
        return this.totalZoomFactor;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ZoomEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        stringBuilder.append(", zoomFactor = ").append(this.getZoomFactor());
        stringBuilder.append(", totalZoomFactor = ").append(this.getTotalZoomFactor());
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
    public ZoomEvent copyFor(Object object, EventTarget eventTarget) {
        return (ZoomEvent)super.copyFor(object, eventTarget);
    }

    public ZoomEvent copyFor(Object object, EventTarget eventTarget, EventType<ZoomEvent> eventType) {
        ZoomEvent zoomEvent = this.copyFor(object, eventTarget);
        zoomEvent.eventType = eventType;
        return zoomEvent;
    }

    public EventType<ZoomEvent> getEventType() {
        return super.getEventType();
    }
}

