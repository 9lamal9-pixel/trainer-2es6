/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.scene.input.InputEventUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point3D;
import javafx.scene.input.InputEvent;
import javafx.scene.input.PickResult;

public class ContextMenuEvent
extends InputEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<ContextMenuEvent> CONTEXT_MENU_REQUESTED = new EventType<InputEvent>(InputEvent.ANY, "CONTEXTMENUREQUESTED");
    public static final EventType<ContextMenuEvent> ANY = CONTEXT_MENU_REQUESTED;
    private final boolean keyboardTrigger;
    private transient double x;
    private transient double y;
    private transient double z;
    private final double screenX;
    private final double screenY;
    private final double sceneX;
    private final double sceneY;
    private PickResult pickResult;

    public ContextMenuEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<ContextMenuEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="keyboardTrigger") boolean bl, @NamedArg(value="pickResult") PickResult pickResult) {
        super(object, eventTarget, (EventType<? extends InputEvent>)eventType);
        this.screenX = d3;
        this.screenY = d4;
        this.sceneX = d;
        this.sceneY = d2;
        this.x = d;
        this.y = d2;
        this.pickResult = pickResult != null ? pickResult : new PickResult(eventTarget, d, d2);
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, null);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
        this.keyboardTrigger = bl;
    }

    public ContextMenuEvent(@NamedArg(value="eventType") EventType<ContextMenuEvent> eventType, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="keyboardTrigger") boolean bl, @NamedArg(value="pickResult") PickResult pickResult) {
        this(null, null, eventType, d, d2, d3, d4, bl, pickResult);
    }

    private void recomputeCoordinatesToSource(ContextMenuEvent contextMenuEvent, Object object) {
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, object);
        contextMenuEvent.x = point3D.getX();
        contextMenuEvent.y = point3D.getY();
        contextMenuEvent.z = point3D.getZ();
    }

    @Override
    public ContextMenuEvent copyFor(Object object, EventTarget eventTarget) {
        ContextMenuEvent contextMenuEvent = (ContextMenuEvent)super.copyFor(object, eventTarget);
        this.recomputeCoordinatesToSource(contextMenuEvent, object);
        return contextMenuEvent;
    }

    public EventType<ContextMenuEvent> getEventType() {
        return super.getEventType();
    }

    public boolean isKeyboardTrigger() {
        return this.keyboardTrigger;
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

    public final PickResult getPickResult() {
        return this.pickResult;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ContextMenuEvent [");
        stringBuilder.append("source = ").append(this.getSource());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", eventType = ").append(this.getEventType());
        stringBuilder.append(", consumed = ").append(this.isConsumed());
        stringBuilder.append(", x = ").append(this.getX()).append(", y = ").append(this.getY()).append(", z = ").append(this.getZ());
        stringBuilder.append(", pickResult = ").append(this.getPickResult());
        return stringBuilder.append("]").toString();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.x = this.sceneX;
        this.y = this.sceneY;
    }
}

