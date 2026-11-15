/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.scene.input.InputEventUtils;
import com.sun.javafx.tk.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point3D;
import javafx.scene.input.InputEvent;
import javafx.scene.input.PickResult;

public class GestureEvent
extends InputEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<GestureEvent> ANY = new EventType<InputEvent>(InputEvent.ANY, "GESTURE");
    private transient double x;
    private transient double y;
    private transient double z;
    private final double screenX;
    private final double screenY;
    private final double sceneX;
    private final double sceneY;
    private final boolean shiftDown;
    private final boolean controlDown;
    private final boolean altDown;
    private final boolean metaDown;
    private final boolean direct;
    private final boolean inertia;
    private PickResult pickResult;

    @Deprecated(since="8")
    protected GestureEvent(EventType<? extends GestureEvent> eventType) {
        this(eventType, 0.0, 0.0, 0.0, 0.0, false, false, false, false, false, false, null);
    }

    @Deprecated(since="8")
    protected GestureEvent(Object object, EventTarget eventTarget, EventType<? extends GestureEvent> eventType) {
        super(object, eventTarget, (EventType<? extends InputEvent>)eventType);
        this.sceneY = 0.0;
        this.sceneX = 0.0;
        this.screenY = 0.0;
        this.screenX = 0.0;
        this.y = 0.0;
        this.x = 0.0;
        this.inertia = false;
        this.direct = false;
        this.metaDown = false;
        this.altDown = false;
        this.controlDown = false;
        this.shiftDown = false;
    }

    protected GestureEvent(Object object, EventTarget eventTarget, EventType<? extends GestureEvent> eventType, double d, double d2, double d3, double d4, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, PickResult pickResult) {
        super(object, eventTarget, (EventType<? extends InputEvent>)eventType);
        this.x = d;
        this.y = d2;
        this.screenX = d3;
        this.screenY = d4;
        this.sceneX = d;
        this.sceneY = d2;
        this.shiftDown = bl;
        this.controlDown = bl2;
        this.altDown = bl3;
        this.metaDown = bl4;
        this.direct = bl5;
        this.inertia = bl6;
        this.pickResult = pickResult != null ? pickResult : new PickResult(eventTarget, d, d2);
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, null);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
    }

    protected GestureEvent(EventType<? extends GestureEvent> eventType, double d, double d2, double d3, double d4, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, PickResult pickResult) {
        this(null, null, eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, bl6, pickResult);
    }

    private void recomputeCoordinatesToSource(GestureEvent gestureEvent, Object object) {
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, object);
        gestureEvent.x = point3D.getX();
        gestureEvent.y = point3D.getY();
        gestureEvent.z = point3D.getZ();
    }

    @Override
    public GestureEvent copyFor(Object object, EventTarget eventTarget) {
        GestureEvent gestureEvent = (GestureEvent)super.copyFor(object, eventTarget);
        this.recomputeCoordinatesToSource(gestureEvent, object);
        return gestureEvent;
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

    public final boolean isDirect() {
        return this.direct;
    }

    public boolean isInertia() {
        return this.inertia;
    }

    public final PickResult getPickResult() {
        return this.pickResult;
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("GestureEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
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

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.x = this.sceneX;
        this.y = this.sceneY;
    }

    public EventType<? extends GestureEvent> getEventType() {
        return super.getEventType();
    }
}

