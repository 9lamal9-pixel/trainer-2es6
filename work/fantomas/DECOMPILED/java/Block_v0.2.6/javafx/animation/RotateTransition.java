/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.util.Duration;

public final class RotateTransition
extends Transition {
    private static final double EPSILON = 1.0E-12;
    private double start;
    private double delta;
    private ObjectProperty<Node> node;
    private static final Node DEFAULT_NODE = null;
    private Node cachedNode;
    private ObjectProperty<Duration> duration;
    private static final Duration DEFAULT_DURATION = Duration.millis(400.0);
    private ObjectProperty<Point3D> axis;
    private static final Point3D DEFAULT_AXIS = null;
    private DoubleProperty fromAngle;
    private static final double DEFAULT_FROM_ANGLE = Double.NaN;
    private DoubleProperty toAngle;
    private static final double DEFAULT_TO_ANGLE = Double.NaN;
    private DoubleProperty byAngle;
    private static final double DEFAULT_BY_ANGLE = 0.0;

    public final void setNode(Node node) {
        if (this.node != null || node != null) {
            this.nodeProperty().set(node);
        }
    }

    public final Node getNode() {
        return this.node == null ? DEFAULT_NODE : (Node)this.node.get();
    }

    public final ObjectProperty<Node> nodeProperty() {
        if (this.node == null) {
            this.node = new SimpleObjectProperty<Node>(this, "node", DEFAULT_NODE);
        }
        return this.node;
    }

    public final void setDuration(Duration duration) {
        if (this.duration != null || !DEFAULT_DURATION.equals(duration)) {
            this.durationProperty().set(duration);
        }
    }

    public final Duration getDuration() {
        return this.duration == null ? DEFAULT_DURATION : (Duration)this.duration.get();
    }

    public final ObjectProperty<Duration> durationProperty() {
        if (this.duration == null) {
            this.duration = new ObjectPropertyBase<Duration>(DEFAULT_DURATION){

                @Override
                public void invalidated() {
                    try {
                        RotateTransition.this.setCycleDuration(RotateTransition.this.getDuration());
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(RotateTransition.this.getCycleDuration());
                        throw illegalArgumentException;
                    }
                }

                @Override
                public Object getBean() {
                    return RotateTransition.this;
                }

                @Override
                public String getName() {
                    return "duration";
                }
            };
        }
        return this.duration;
    }

    public final void setAxis(Point3D point3D) {
        if (this.axis != null || point3D != null) {
            this.axisProperty().set(point3D);
        }
    }

    public final Point3D getAxis() {
        return this.axis == null ? DEFAULT_AXIS : (Point3D)this.axis.get();
    }

    public final ObjectProperty<Point3D> axisProperty() {
        if (this.axis == null) {
            this.axis = new SimpleObjectProperty<Point3D>(this, "axis", DEFAULT_AXIS);
        }
        return this.axis;
    }

    public final void setFromAngle(double d) {
        if (this.fromAngle != null || !Double.isNaN(d)) {
            this.fromAngleProperty().set(d);
        }
    }

    public final double getFromAngle() {
        return this.fromAngle == null ? Double.NaN : this.fromAngle.get();
    }

    public final DoubleProperty fromAngleProperty() {
        if (this.fromAngle == null) {
            this.fromAngle = new SimpleDoubleProperty(this, "fromAngle", Double.NaN);
        }
        return this.fromAngle;
    }

    public final void setToAngle(double d) {
        if (this.toAngle != null || !Double.isNaN(d)) {
            this.toAngleProperty().set(d);
        }
    }

    public final double getToAngle() {
        return this.toAngle == null ? Double.NaN : this.toAngle.get();
    }

    public final DoubleProperty toAngleProperty() {
        if (this.toAngle == null) {
            this.toAngle = new SimpleDoubleProperty(this, "toAngle", Double.NaN);
        }
        return this.toAngle;
    }

    public final void setByAngle(double d) {
        if (this.byAngle != null || Math.abs(d - 0.0) > 1.0E-12) {
            this.byAngleProperty().set(d);
        }
    }

    public final double getByAngle() {
        return this.byAngle == null ? 0.0 : this.byAngle.get();
    }

    public final DoubleProperty byAngleProperty() {
        if (this.byAngle == null) {
            this.byAngle = new SimpleDoubleProperty(this, "byAngle", 0.0);
        }
        return this.byAngle;
    }

    public RotateTransition(Duration duration, Node node) {
        this.setDuration(duration);
        this.setNode(node);
        this.setCycleDuration(duration);
    }

    public RotateTransition(Duration duration) {
        this(duration, null);
    }

    public RotateTransition() {
        this(DEFAULT_DURATION, null);
    }

    @Override
    protected void interpolate(double d) {
        this.cachedNode.setRotate(this.start + d * this.delta);
    }

    private Node getTargetNode() {
        Node node = this.getNode();
        return node != null ? node : this.getParentTargetNode();
    }

    @Override
    boolean startable(boolean bl) {
        return super.startable(bl) && (this.getTargetNode() != null || !bl && this.cachedNode != null);
    }

    @Override
    void sync(boolean bl) {
        super.sync(bl);
        if (bl || this.cachedNode == null) {
            this.cachedNode = this.getTargetNode();
            double d = this.getFromAngle();
            double d2 = this.getToAngle();
            this.start = !Double.isNaN(d) ? d : this.cachedNode.getRotate();
            this.delta = !Double.isNaN(d2) ? d2 - this.start : this.getByAngle();
            Point3D point3D = this.getAxis();
            if (point3D != null) {
                ((Node)this.node.get()).setRotationAxis(point3D);
            }
        }
    }
}

