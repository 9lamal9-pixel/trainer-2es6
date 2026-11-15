/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import com.sun.javafx.scene.input.InputEventUtils;
import com.sun.javafx.scene.input.TouchPointHelper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.PickResult;

public final class TouchPoint
implements Serializable {
    private transient EventTarget target;
    private transient Object source;
    private EventTarget grabbed = null;
    private int id;
    private State state;
    private transient double x;
    private transient double y;
    private transient double z;
    private double screenX;
    private double screenY;
    private double sceneX;
    private double sceneY;
    private PickResult pickResult;

    public TouchPoint(@NamedArg(value="id") int n, @NamedArg(value="state") State state, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="screenX") double d3, @NamedArg(value="screenY") double d4, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="pickResult") PickResult pickResult) {
        this.target = eventTarget;
        this.id = n;
        this.state = state;
        this.x = d;
        this.y = d2;
        this.sceneX = d;
        this.sceneY = d2;
        this.screenX = d3;
        this.screenY = d4;
        this.pickResult = pickResult != null ? pickResult : new PickResult(eventTarget, d, d2);
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, null);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
    }

    void recomputeToSource(Object object, Object object2) {
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, object2);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
        this.source = object2;
    }

    public boolean belongsTo(EventTarget eventTarget) {
        if (this.target instanceof Node) {
            Node node;
            if (eventTarget instanceof Scene) {
                return node.getScene() == eventTarget;
            }
            for (node = (Node)this.target; node != null; node = node.getParent()) {
                if (node != eventTarget) continue;
                return true;
            }
        }
        return eventTarget == this.target;
    }

    void reset() {
        Point3D point3D = InputEventUtils.recomputeCoordinates(this.pickResult, null);
        this.x = point3D.getX();
        this.y = point3D.getY();
        this.z = point3D.getZ();
    }

    public EventTarget getGrabbed() {
        return this.grabbed;
    }

    public void grab() {
        if (!(this.source instanceof EventTarget)) {
            throw new IllegalStateException("Cannot grab touch point, source is not an instance of EventTarget: " + this.source);
        }
        this.grabbed = (EventTarget)this.source;
    }

    public void grab(EventTarget eventTarget) {
        this.grabbed = eventTarget;
    }

    public void ungrab() {
        this.grabbed = null;
    }

    public final int getId() {
        return this.id;
    }

    public final State getState() {
        return this.state;
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

    public EventTarget getTarget() {
        return this.target;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("TouchPoint [");
        stringBuilder.append("state = ").append((Object)this.getState());
        stringBuilder.append(", id = ").append(this.getId());
        stringBuilder.append(", target = ").append(this.getTarget());
        stringBuilder.append(", x = ").append(this.getX()).append(", y = ").append(this.getY()).append(", z = ").append(this.getZ());
        stringBuilder.append(", pickResult = ").append(this.getPickResult());
        return stringBuilder.append("]").toString();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.x = this.sceneX;
        this.y = this.sceneY;
    }

    static {
        TouchPointHelper.setTouchPointAccessor(new TouchPointHelper.TouchPointAccessor(){

            @Override
            public void reset(TouchPoint touchPoint) {
                touchPoint.reset();
            }
        });
    }

    public static enum State {
        PRESSED,
        MOVED,
        STATIONARY,
        RELEASED;

    }
}

