/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.scene.input.InputEventUtils;
import com.sun.javafx.tk.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point3D;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.PickResult;

public class MouseEvent
extends InputEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<MouseEvent> ANY = new EventType<InputEvent>(InputEvent.ANY, "MOUSE");
    public static final EventType<MouseEvent> MOUSE_PRESSED = new EventType<MouseEvent>(ANY, "MOUSE_PRESSED");
    public static final EventType<MouseEvent> MOUSE_RELEASED = new EventType<MouseEvent>(ANY, "MOUSE_RELEASED");
    public static final EventType<MouseEvent> MOUSE_CLICKED = new EventType<MouseEvent>(ANY, "MOUSE_CLICKED");
    public static final EventType<MouseEvent> MOUSE_ENTERED_TARGET = new EventType<MouseEvent>(ANY, "MOUSE_ENTERED_TARGET");
    public static final EventType<MouseEvent> MOUSE_ENTERED = new EventType<MouseEvent>(MOUSE_ENTERED_TARGET, "MOUSE_ENTERED");
    public static final EventType<MouseEvent> MOUSE_EXITED_TARGET = new EventType<MouseEvent>(ANY, "MOUSE_EXITED_TARGET");
    public static final EventType<MouseEvent> MOUSE_EXITED = new EventType<MouseEvent>(MOUSE_EXITED_TARGET, "MOUSE_EXITED");
    public static final EventType<MouseEvent> MOUSE_MOVED = new EventType<MouseEvent>(ANY, "MOUSE_MOVED");
    public static final EventType<MouseEvent> MOUSE_DRAGGED = new EventType<MouseEvent>(ANY, "MOUSE_DRAGGED");
    public static final EventType<MouseEvent> DRAG_DETECTED = new EventType<MouseEvent>(ANY, "DRAG_DETECTED");
    private final Flags flags = new Flags();
    private transient double x;
    private transient double y;
    private transient double z;
    private final double screenX;
    private final double screenY;
    private final double sceneX;
    private final double sceneY;
    private final MouseButton button;
    private final int clickCount;
    private final boolean stillSincePress;
    private final boolean shiftDown;
    private final boolean controlDown;
    private final boolean altDown;
    private final boolean metaDown;
    private final boolean synthesized;
    private final boolean popupTrigger;
    private final boolean primaryButtonDown;
    private final boolean secondaryButtonDown;
    private final boolean middleButtonDown;
    private PickResult pickResult;

    void recomputeCoordinatesToSource(MouseEvent mouseEvent, Object object) {
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, object);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
    }

    public EventType<? extends MouseEvent> getEventType() {
        return super.getEventType();
    }

    @Override
    public MouseEvent copyFor(Object object, EventTarget eventTarget) {
        MouseEvent mouseEvent = (MouseEvent)super.copyFor(object, eventTarget);
        mouseEvent.recomputeCoordinatesToSource(this, object);
        return mouseEvent;
    }

    public MouseEvent copyFor(Object object, EventTarget eventTarget, EventType<? extends MouseEvent> eventType) {
        MouseEvent mouseEvent = this.copyFor(object, eventTarget);
        mouseEvent.eventType = eventType;
        return mouseEvent;
    }

    public MouseEvent(@NamedArg(value="eventType") EventType<? extends MouseEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="button") MouseButton mouseButton, @NamedArg(value="clickCount") int n, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="primaryButtonDown") boolean bl5, @NamedArg(value="middleButtonDown") boolean bl6, @NamedArg(value="secondaryButtonDown") boolean bl7, @NamedArg(value="synthesized") boolean bl8, @NamedArg(value="popupTrigger") boolean bl9, @NamedArg(value="stillSincePress") boolean bl10, @NamedArg(value="pickResult") PickResult pickResult) {
        this(null, null, eventType, d, d2, d3, d4, mouseButton, n, bl, bl2, bl3, bl4, bl5, bl6, bl7, bl8, bl9, bl10, pickResult);
    }

    public MouseEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<? extends MouseEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="button") MouseButton mouseButton, @NamedArg(value="clickCount") int n, @NamedArg(value="shiftDown") boolean bl, @NamedArg(value="controlDown") boolean bl2, @NamedArg(value="altDown") boolean bl3, @NamedArg(value="metaDown") boolean bl4, @NamedArg(value="primaryButtonDown") boolean bl5, @NamedArg(value="middleButtonDown") boolean bl6, @NamedArg(value="secondaryButtonDown") boolean bl7, @NamedArg(value="synthesized") boolean bl8, @NamedArg(value="popupTrigger") boolean bl9, @NamedArg(value="stillSincePress") boolean bl10, @NamedArg(value="pickResult") PickResult pickResult) {
        super(object, eventTarget, (EventType<? extends InputEvent>)eventType);
        this.x = d;
        this.y = d2;
        this.screenX = d3;
        this.screenY = d4;
        this.sceneX = d;
        this.sceneY = d2;
        this.button = mouseButton;
        this.clickCount = n;
        this.shiftDown = bl;
        this.controlDown = bl2;
        this.altDown = bl3;
        this.metaDown = bl4;
        this.primaryButtonDown = bl5;
        this.middleButtonDown = bl6;
        this.secondaryButtonDown = bl7;
        this.synthesized = bl8;
        this.stillSincePress = bl10;
        this.popupTrigger = bl9;
        this.pickResult = pickResult;
        this.pickResult = pickResult != null ? pickResult : new PickResult(eventTarget, d, d2);
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, null);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
    }

    public static MouseDragEvent copyForMouseDragEvent(MouseEvent mouseEvent, Object object, EventTarget eventTarget, EventType<MouseDragEvent> eventType, Object object2, PickResult pickResult) {
        MouseDragEvent mouseDragEvent = new MouseDragEvent(object, eventTarget, eventType, mouseEvent.sceneX, mouseEvent.sceneY, mouseEvent.screenX, mouseEvent.screenY, mouseEvent.button, mouseEvent.clickCount, mouseEvent.shiftDown, mouseEvent.controlDown, mouseEvent.altDown, mouseEvent.metaDown, mouseEvent.primaryButtonDown, mouseEvent.middleButtonDown, mouseEvent.secondaryButtonDown, mouseEvent.synthesized, mouseEvent.popupTrigger, pickResult, object2);
        mouseDragEvent.recomputeCoordinatesToSource(mouseEvent, object);
        return mouseDragEvent;
    }

    public boolean isDragDetect() {
        return this.flags.dragDetect;
    }

    public void setDragDetect(boolean bl) {
        this.flags.dragDetect = bl;
    }

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final double getZ() {
        return this.z;
    }

    public final double getScreenX() {
        return this.screenX;
    }

    public final double getScreenY() {
        return this.screenY;
    }

    public final double getSceneX() {
        return this.sceneX;
    }

    public final double getSceneY() {
        return this.sceneY;
    }

    public final MouseButton getButton() {
        return this.button;
    }

    public final int getClickCount() {
        return this.clickCount;
    }

    public final boolean isStillSincePress() {
        return this.stillSincePress;
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

    public boolean isSynthesized() {
        return this.synthesized;
    }

    public final boolean isShortcutDown() {
        switch (Toolkit.getToolkit().getPlatformShortcutKey()) {
            case SHIFT: {
                return this.shiftDown;
            }
            case CONTROL: {
                return this.controlDown;
            }
            case ALT: {
                return this.altDown;
            }
            case META: {
                return this.metaDown;
            }
        }
        return false;
    }

    public final boolean isPopupTrigger() {
        return this.popupTrigger;
    }

    public final boolean isPrimaryButtonDown() {
        return this.primaryButtonDown;
    }

    public final boolean isSecondaryButtonDown() {
        return this.secondaryButtonDown;
    }

    public final boolean isMiddleButtonDown() {
        return this.middleButtonDown;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("MouseEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
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

    public final PickResult getPickResult() {
        return this.pickResult;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.x = this.sceneX;
        this.y = this.sceneY;
    }

    private static class Flags
    implements Cloneable {
        boolean dragDetect = true;

        private Flags() {
        }

        public Flags clone() {
            try {
                return (Flags)super.clone();
            }
            catch (CloneNotSupportedException cloneNotSupportedException) {
                return null;
            }
        }
    }
}

