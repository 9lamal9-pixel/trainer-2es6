/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

public final class MouseDragEvent
extends MouseEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<MouseDragEvent> ANY = new EventType<MouseEvent>(MouseEvent.ANY, "MOUSE-DRAG");
    public static final EventType<MouseDragEvent> MOUSE_DRAG_OVER = new EventType<MouseDragEvent>(ANY, "MOUSE-DRAG_OVER");
    public static final EventType<MouseDragEvent> MOUSE_DRAG_RELEASED = new EventType<MouseDragEvent>(ANY, "MOUSE-DRAG_RELEASED");
    public static final EventType<MouseDragEvent> MOUSE_DRAG_ENTERED_TARGET = new EventType<MouseDragEvent>(ANY, "MOUSE-DRAG_ENTERED_TARGET");
    public static final EventType<MouseDragEvent> MOUSE_DRAG_ENTERED = new EventType<MouseDragEvent>(MOUSE_DRAG_ENTERED_TARGET, "MOUSE-DRAG_ENTERED");
    public static final EventType<MouseDragEvent> MOUSE_DRAG_EXITED_TARGET = new EventType<MouseDragEvent>(ANY, "MOUSE-DRAG_EXITED_TARGET");
    public static final EventType<MouseDragEvent> MOUSE_DRAG_EXITED = new EventType<MouseDragEvent>(MOUSE_DRAG_EXITED_TARGET, "MOUSE-DRAG_EXITED");
    private final transient Object gestureSource;

    public MouseDragEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<MouseDragEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="button") MouseButton mouseButton, @NamedArg(value="clickCount") int n, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="primaryButtonDown") boolean bl5, @NamedArg(value="middleButtonDown") boolean bl6, @NamedArg(value="secondaryButtonDown") boolean bl7, @NamedArg(value="synthesized") boolean bl8, @NamedArg(value="popupTrigger") boolean bl9, @NamedArg(value="pickResult") PickResult pickResult, @NamedArg(value="gestureSource") Object object2) {
        super(object, eventTarget, eventType, d, d2, d3, d4, mouseButton, n, bl, bl2, bl3, bl4, bl5, bl6, bl7, bl8, bl9, false, pickResult);
        this.gestureSource = object2;
    }

    public MouseDragEvent(@NamedArg(value="eventType") EventType<MouseDragEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="button") MouseButton mouseButton, @NamedArg(value="clickCount") int n, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="primaryButtonDown") boolean bl5, @NamedArg(value="middleButtonDown") boolean bl6, @NamedArg(value="secondaryButtonDown") boolean bl7, @NamedArg(value="synthesized") boolean bl8, @NamedArg(value="popupTrigger") boolean bl9, @NamedArg(value="pickResult") PickResult pickResult, @NamedArg(value="gestureSource") Object object) {
        this(null, null, eventType, d, d2, d3, d4, mouseButton, n, bl, bl2, bl3, bl4, bl5, bl6, bl7, bl8, bl9, pickResult, object);
    }

    public Object getGestureSource() {
        return this.gestureSource;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("MouseDragEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", gestureSource = ").append(this.getGestureSource());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        stringBuilder.append(", x = ").append(this.getX()).append(", y = ").append(this.getY()).append(", z = ").append(this.getZ());
        if (this.getButton() != null) {
            stringBuilder.append(", button = ").append((Object)this.getButton());
        }
        if (this.getClickCount() > 1) {
            stringBuilder.append(", clickCount = ").append(this.getClickCount());
        }
        if (this.isPrimaryButtonDown()) {
            stringBuilder.append(", primaryButtonDown");
        }
        if (this.isMiddleButtonDown()) {
            stringBuilder.append(", middleButtonDown");
        }
        if (this.isSecondaryButtonDown()) {
            stringBuilder.append(", secondaryButtonDown");
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
        if (this.isSynthesized()) {
            stringBuilder.append(", synthesized");
        }
        stringBuilder.append(", pickResult = ").append(this.getPickResult());
        return stringBuilder.append("]").toString();
    }

    @Override
    public MouseDragEvent copyFor(Object object, EventTarget eventTarget) {
        return (MouseDragEvent)super.copyFor(object, eventTarget);
    }

    @Override
    public MouseDragEvent copyFor(Object object, EventTarget eventTarget, EventType<? extends MouseEvent> eventType) {
        return (MouseDragEvent)super.copyFor(object, eventTarget, eventType);
    }

    public EventType<MouseDragEvent> getEventType() {
        return super.getEventType();
    }
}

