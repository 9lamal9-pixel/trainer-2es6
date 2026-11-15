/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.scene.input.InputEventUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.EnumSet;
import java.util.Set;
import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point3D;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;

public final class DragEvent
extends InputEvent {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<DragEvent> ANY = new EventType<InputEvent>(InputEvent.ANY, "DRAG");
    public static final EventType<DragEvent> DRAG_ENTERED_TARGET = new EventType<DragEvent>(ANY, "DRAG_ENTERED_TARGET");
    public static final EventType<DragEvent> DRAG_ENTERED = new EventType<DragEvent>(DRAG_ENTERED_TARGET, "DRAG_ENTERED");
    public static final EventType<DragEvent> DRAG_EXITED_TARGET = new EventType<DragEvent>(ANY, "DRAG_EXITED_TARGET");
    public static final EventType<DragEvent> DRAG_EXITED = new EventType<DragEvent>(DRAG_EXITED_TARGET, "DRAG_EXITED");
    public static final EventType<DragEvent> DRAG_OVER = new EventType<DragEvent>(ANY, "DRAG_OVER");
    public static final EventType<DragEvent> DRAG_DROPPED = new EventType<DragEvent>(ANY, "DRAG_DROPPED");
    public static final EventType<DragEvent> DRAG_DONE = new EventType<DragEvent>(ANY, "DRAG_DONE");
    private transient double x;
    private transient double y;
    private transient double z;
    private final double screenX;
    private final double screenY;
    private final double sceneX;
    private final double sceneY;
    private PickResult pickResult;
    private Object gestureSource;
    private Object gestureTarget;
    private TransferMode transferMode;
    private final State state = new State();
    private transient Dragboard dragboard;

    public DragEvent copyFor(Object object, EventTarget eventTarget, Object object2, Object object3, EventType<DragEvent> eventType) {
        DragEvent dragEvent = this.copyFor(object, eventTarget, eventType);
        this.recomputeCoordinatesToSource(dragEvent, object);
        dragEvent.gestureSource = object2;
        dragEvent.gestureTarget = object3;
        return dragEvent;
    }

    public DragEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<DragEvent> eventType, @NamedArg(value="dragboard") Dragboard dragboard, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="transferMode") TransferMode transferMode, @NamedArg(value="gestureSource") Object object2, @NamedArg(value="gestureTarget") Object object3, @NamedArg(value="pickResult") PickResult pickResult) {
        super(object, eventTarget, (EventType<? extends InputEvent>)eventType);
        this.gestureSource = object2;
        this.gestureTarget = object3;
        this.x = d;
        this.y = d2;
        this.screenX = d3;
        this.screenY = d4;
        this.sceneX = d;
        this.sceneY = d2;
        this.transferMode = transferMode;
        this.dragboard = dragboard;
        if (eventType == DRAG_DROPPED || eventType == DRAG_DONE) {
            this.state.accepted = transferMode != null;
            this.state.acceptedTransferMode = transferMode;
            Object object4 = this.state.acceptingObject = this.state.accepted ? object : null;
        }
        this.pickResult = pickResult != null ? pickResult : new PickResult(eventType == DRAG_DONE ? null : eventTarget, d, d2);
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, null);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
    }

    public DragEvent(@NamedArg(value="eventType") EventType<DragEvent> eventType, @NamedArg(value="dragboard") Dragboard dragboard, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="transferMode") TransferMode transferMode, @NamedArg(value="gestureSource") Object object, @NamedArg(value="gestureTarget") Object object2, @NamedArg(value="pickResult") PickResult pickResult) {
        this(null, null, eventType, dragboard, d, d2, d3, d4, transferMode, object, object2, pickResult);
    }

    private void recomputeCoordinatesToSource(DragEvent dragEvent, Object object) {
        if (dragEvent.getEventType() == DRAG_DONE) {
            return;
        }
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, object);
        dragEvent.x = point3D.getX();
        dragEvent.y = point3D.getY();
        dragEvent.z = point3D.getZ();
    }

    @Override
    public DragEvent copyFor(Object object, EventTarget eventTarget) {
        DragEvent dragEvent = (DragEvent)super.copyFor(object, eventTarget);
        this.recomputeCoordinatesToSource(dragEvent, object);
        return dragEvent;
    }

    public DragEvent copyFor(Object object, EventTarget eventTarget, EventType<DragEvent> eventType) {
        DragEvent dragEvent = this.copyFor(object, eventTarget);
        dragEvent.eventType = eventType;
        return dragEvent;
    }

    public EventType<DragEvent> getEventType() {
        return super.getEventType();
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

    public final Object getGestureSource() {
        return this.gestureSource;
    }

    public final Object getGestureTarget() {
        return this.gestureTarget;
    }

    public final TransferMode getTransferMode() {
        return this.transferMode;
    }

    public final boolean isAccepted() {
        return this.state.accepted;
    }

    public final TransferMode getAcceptedTransferMode() {
        return this.state.acceptedTransferMode;
    }

    public final Object getAcceptingObject() {
        return this.state.acceptingObject;
    }

    public final Dragboard getDragboard() {
        return this.dragboard;
    }

    private static TransferMode chooseTransferMode(Set<TransferMode> set, TransferMode[] transferModeArray, TransferMode transferMode) {
        TransferMode transferMode2 = null;
        EnumSet<TransferMode> enumSet = EnumSet.noneOf(TransferMode.class);
        for (TransferMode transferMode3 : InputEventUtils.safeTransferModes(transferModeArray)) {
            if (!set.contains((Object)transferMode3)) continue;
            enumSet.add(transferMode3);
        }
        if (enumSet.contains((Object)transferMode)) {
            transferMode2 = transferMode;
        } else if (enumSet.contains((Object)TransferMode.MOVE)) {
            transferMode2 = TransferMode.MOVE;
        } else if (enumSet.contains((Object)TransferMode.COPY)) {
            transferMode2 = TransferMode.COPY;
        } else if (enumSet.contains((Object)TransferMode.LINK)) {
            transferMode2 = TransferMode.LINK;
        }
        return transferMode2;
    }

    public void acceptTransferModes(TransferMode ... transferModeArray) {
        if (this.dragboard == null || this.dragboard.getTransferModes() == null || this.transferMode == null) {
            this.state.accepted = false;
            return;
        }
        TransferMode transferMode = DragEvent.chooseTransferMode(this.dragboard.getTransferModes(), transferModeArray, this.transferMode);
        if (transferMode == null && this.getEventType() == DRAG_DROPPED) {
            throw new IllegalStateException("Accepting unsupported transfer modes inside DRAG_DROPPED handler");
        }
        this.state.accepted = transferMode != null;
        this.state.acceptedTransferMode = transferMode;
        this.state.acceptingObject = this.state.accepted ? this.source : null;
    }

    public void setDropCompleted(boolean bl) {
        if (this.getEventType() != DRAG_DROPPED) {
            throw new IllegalStateException("setDropCompleted can be called only from DRAG_DROPPED handler");
        }
        this.state.dropCompleted = bl;
    }

    public boolean isDropCompleted() {
        return this.state.dropCompleted;
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.x = this.sceneX;
        this.y = this.sceneY;
    }

    private static class State {
        boolean accepted = false;
        boolean dropCompleted = false;
        TransferMode acceptedTransferMode = null;
        Object acceptingObject = null;

        private State() {
        }
    }
}

