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

public final class TranslateTransition
extends Transition {
    private static final double EPSILON = 1.0E-12;
    private double startX;
    private double startY;
    private double startZ;
    private double deltaX;
    private double deltaY;
    private double deltaZ;
    private ObjectProperty<Node> node;
    private static final Node DEFAULT_NODE = null;
    private Node cachedNode;
    private ObjectProperty<Duration> duration;
    private static final Duration DEFAULT_DURATION = Duration.millis(400.0);
    private DoubleProperty fromX;
    private static final double DEFAULT_FROM_X = Double.NaN;
    private DoubleProperty fromY;
    private static final double DEFAULT_FROM_Y = Double.NaN;
    private DoubleProperty fromZ;
    private static final double DEFAULT_FROM_Z = Double.NaN;
    private DoubleProperty toX;
    private static final double DEFAULT_TO_X = Double.NaN;
    private DoubleProperty toY;
    private static final double DEFAULT_TO_Y = Double.NaN;
    private DoubleProperty toZ;
    private static final double DEFAULT_TO_Z = Double.NaN;
    private DoubleProperty byX;
    private static final double DEFAULT_BY_X = 0.0;
    private DoubleProperty byY;
    private static final double DEFAULT_BY_Y = 0.0;
    private DoubleProperty byZ;
    private static final double DEFAULT_BY_Z = 0.0;

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
                        TranslateTransition.this.setCycleDuration(TranslateTransition.this.getDuration());
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(TranslateTransition.this.getCycleDuration());
                        throw illegalArgumentException;
                    }
                }

                @Override
                public Object getBean() {
                    return TranslateTransition.this;
                }

                @Override
                public String getName() {
                    return "duration";
                }
            };
        }
        return this.duration;
    }

    public final void setFromX(double d) {
        if (this.fromX != null || !Double.isNaN(d)) {
            this.fromXProperty().set(d);
        }
    }

    public final double getFromX() {
        return this.fromX == null ? Double.NaN : this.fromX.get();
    }

    public final DoubleProperty fromXProperty() {
        if (this.fromX == null) {
            this.fromX = new SimpleDoubleProperty(this, "fromX", Double.NaN);
        }
        return this.fromX;
    }

    public final void setFromY(double d) {
        if (this.fromY != null || !Double.isNaN(d)) {
            this.fromYProperty().set(d);
        }
    }

    public final double getFromY() {
        return this.fromY == null ? Double.NaN : this.fromY.get();
    }

    public final DoubleProperty fromYProperty() {
        if (this.fromY == null) {
            this.fromY = new SimpleDoubleProperty(this, "fromY", Double.NaN);
        }
        return this.fromY;
    }

    public final void setFromZ(double d) {
        if (this.fromZ != null || !Double.isNaN(d)) {
            this.fromZProperty().set(d);
        }
    }

    public final double getFromZ() {
        return this.fromZ == null ? Double.NaN : this.fromZ.get();
    }

    public final DoubleProperty fromZProperty() {
        if (this.fromZ == null) {
            this.fromZ = new SimpleDoubleProperty(this, "fromZ", Double.NaN);
        }
        return this.fromZ;
    }

    public final void setToX(double d) {
        if (this.toX != null || !Double.isNaN(d)) {
            this.toXProperty().set(d);
        }
    }

    public final double getToX() {
        return this.toX == null ? Double.NaN : this.toX.get();
    }

    public final DoubleProperty toXProperty() {
        if (this.toX == null) {
            this.toX = new SimpleDoubleProperty(this, "toX", Double.NaN);
        }
        return this.toX;
    }

    public final void setToY(double d) {
        if (this.toY != null || !Double.isNaN(d)) {
            this.toYProperty().set(d);
        }
    }

    public final double getToY() {
        return this.toY == null ? Double.NaN : this.toY.get();
    }

    public final DoubleProperty toYProperty() {
        if (this.toY == null) {
            this.toY = new SimpleDoubleProperty(this, "toY", Double.NaN);
        }
        return this.toY;
    }

    public final void setToZ(double d) {
        if (this.toZ != null || !Double.isNaN(d)) {
            this.toZProperty().set(d);
        }
    }

    public final double getToZ() {
        return this.toZ == null ? Double.NaN : this.toZ.get();
    }

    public final DoubleProperty toZProperty() {
        if (this.toZ == null) {
            this.toZ = new SimpleDoubleProperty(this, "toZ", Double.NaN);
        }
        return this.toZ;
    }

    public final void setByX(double d) {
        if (this.byX != null || Math.abs(d - 0.0) > 1.0E-12) {
            this.byXProperty().set(d);
        }
    }

    public final double getByX() {
        return this.byX == null ? 0.0 : this.byX.get();
    }

    public final DoubleProperty byXProperty() {
        if (this.byX == null) {
            this.byX = new SimpleDoubleProperty(this, "byX", 0.0);
        }
        return this.byX;
    }

    public final void setByY(double d) {
        if (this.byY != null || Math.abs(d - 0.0) > 1.0E-12) {
            this.byYProperty().set(d);
        }
    }

    public final double getByY() {
        return this.byY == null ? 0.0 : this.byY.get();
    }

    public final DoubleProperty byYProperty() {
        if (this.byY == null) {
            this.byY = new SimpleDoubleProperty(this, "byY", 0.0);
        }
        return this.byY;
    }

    public final void setByZ(double d) {
        if (this.byZ != null || Math.abs(d - 0.0) > 1.0E-12) {
            this.byZProperty().set(d);
        }
    }

    public final double getByZ() {
        return this.byZ == null ? 0.0 : this.byZ.get();
    }

    public final DoubleProperty byZProperty() {
        if (this.byZ == null) {
            this.byZ = new SimpleDoubleProperty(this, "byZ", 0.0);
        }
        return this.byZ;
    }

    public TranslateTransition(Duration duration, Node node) {
        this.setDuration(duration);
        this.setNode(node);
        this.setCycleDuration(duration);
    }

    public TranslateTransition(Duration duration) {
        this(duration, null);
    }

    public TranslateTransition() {
        this(DEFAULT_DURATION, null);
    }

    @Override
    public void interpolate(double d) {
        if (!Double.isNaN(this.startX)) {
            this.cachedNode.setTranslateX(this.startX + d * this.deltaX);
        }
        if (!Double.isNaN(this.startY)) {
            this.cachedNode.setTranslateY(this.startY + d * this.deltaY);
        }
        if (!Double.isNaN(this.startZ)) {
            this.cachedNode.setTranslateZ(this.startZ + d * this.deltaZ);
        }
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
            double d = this.getFromX();
            double d2 = this.getFromY();
            double d3 = this.getFromZ();
            double d4 = this.getToX();
            double d5 = this.getToY();
            double d6 = this.getToZ();
            double d7 = this.getByX();
            double d8 = this.getByY();
            double d9 = this.getByZ();
            if (Double.isNaN(d) && Double.isNaN(d4) && Math.abs(d7) < 1.0E-12) {
                this.startX = Double.NaN;
            } else {
                this.startX = !Double.isNaN(d) ? d : this.cachedNode.getTranslateX();
                double d10 = this.deltaX = !Double.isNaN(d4) ? d4 - this.startX : d7;
            }
            if (Double.isNaN(d2) && Double.isNaN(d5) && Math.abs(d8) < 1.0E-12) {
                this.startY = Double.NaN;
            } else {
                this.startY = !Double.isNaN(d2) ? d2 : this.cachedNode.getTranslateY();
                double d11 = this.deltaY = !Double.isNaN(d5) ? d5 - this.startY : this.getByY();
            }
            if (Double.isNaN(d3) && Double.isNaN(d6) && Math.abs(d9) < 1.0E-12) {
                this.startZ = Double.NaN;
            } else {
                this.startZ = !Double.isNaN(d3) ? d3 : this.cachedNode.getTranslateZ();
                this.deltaZ = !Double.isNaN(d6) ? d6 - this.startZ : this.getByZ();
            }
        }
    }
}

