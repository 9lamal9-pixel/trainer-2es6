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
import javafx.scene.Node;
import javafx.util.Duration;

public final class FadeTransition
extends Transition {
    private static final double EPSILON = 1.0E-12;
    private double start;
    private double delta;
    private ObjectProperty<Node> node;
    private static final Node DEFAULT_NODE = null;
    private Node cachedNode;
    private ObjectProperty<Duration> duration;
    private static final Duration DEFAULT_DURATION = Duration.millis(400.0);
    private DoubleProperty fromValue;
    private static final double DEFAULT_FROM_VALUE = Double.NaN;
    private DoubleProperty toValue;
    private static final double DEFAULT_TO_VALUE = Double.NaN;
    private DoubleProperty byValue;
    private static final double DEFAULT_BY_VALUE = 0.0;

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
                        FadeTransition.this.setCycleDuration(FadeTransition.this.getDuration());
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(FadeTransition.this.getCycleDuration());
                        throw illegalArgumentException;
                    }
                }

                @Override
                public Object getBean() {
                    return FadeTransition.this;
                }

                @Override
                public String getName() {
                    return "duration";
                }
            };
        }
        return this.duration;
    }

    public final void setFromValue(double d) {
        if (this.fromValue != null || !Double.isNaN(d)) {
            this.fromValueProperty().set(d);
        }
    }

    public final double getFromValue() {
        return this.fromValue == null ? Double.NaN : this.fromValue.get();
    }

    public final DoubleProperty fromValueProperty() {
        if (this.fromValue == null) {
            this.fromValue = new SimpleDoubleProperty(this, "fromValue", Double.NaN);
        }
        return this.fromValue;
    }

    public final void setToValue(double d) {
        if (this.toValue != null || !Double.isNaN(d)) {
            this.toValueProperty().set(d);
        }
    }

    public final double getToValue() {
        return this.toValue == null ? Double.NaN : this.toValue.get();
    }

    public final DoubleProperty toValueProperty() {
        if (this.toValue == null) {
            this.toValue = new SimpleDoubleProperty(this, "toValue", Double.NaN);
        }
        return this.toValue;
    }

    public final void setByValue(double d) {
        if (this.byValue != null || Math.abs(d - 0.0) > 1.0E-12) {
            this.byValueProperty().set(d);
        }
    }

    public final double getByValue() {
        return this.byValue == null ? 0.0 : this.byValue.get();
    }

    public final DoubleProperty byValueProperty() {
        if (this.byValue == null) {
            this.byValue = new SimpleDoubleProperty(this, "byValue", 0.0);
        }
        return this.byValue;
    }

    public FadeTransition(Duration duration, Node node) {
        this.setDuration(duration);
        this.setNode(node);
        this.setCycleDuration(duration);
    }

    public FadeTransition(Duration duration) {
        this(duration, null);
    }

    public FadeTransition() {
        this(DEFAULT_DURATION, null);
    }

    @Override
    protected void interpolate(double d) {
        double d2 = Math.max(0.0, Math.min(this.start + d * this.delta, 1.0));
        this.cachedNode.setOpacity(d2);
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
            double d = this.getFromValue();
            double d2 = this.getToValue();
            this.start = !Double.isNaN(d) ? Math.max(0.0, Math.min(d, 1.0)) : this.cachedNode.getOpacity();
            double d3 = this.delta = !Double.isNaN(d2) ? d2 - this.start : this.getByValue();
            if (this.start + this.delta > 1.0) {
                this.delta = 1.0 - this.start;
            } else if (this.start + this.delta < 0.0) {
                this.delta = -this.start;
            }
        }
    }
}

