/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.javafx.geom.PathIterator;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.ShapeHelper;
import java.util.ArrayList;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public final class PathTransition
extends Transition {
    private ObjectProperty<Node> node;
    private double totalLength = 0.0;
    private final ArrayList<Segment> segments = new ArrayList();
    private static final Node DEFAULT_NODE = null;
    private static final int SMOOTH_ZONE = 10;
    private Node cachedNode;
    private ObjectProperty<Duration> duration;
    private static final Duration DEFAULT_DURATION = Duration.millis(400.0);
    private ObjectProperty<Shape> path;
    private static final Shape DEFAULT_PATH = null;
    private ObjectProperty<OrientationType> orientation;
    private static final OrientationType DEFAULT_ORIENTATION = OrientationType.NONE;
    private boolean cachedIsNormalRequired;

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
                        PathTransition.this.setCycleDuration(PathTransition.this.getDuration());
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(PathTransition.this.getCycleDuration());
                        throw illegalArgumentException;
                    }
                }

                @Override
                public Object getBean() {
                    return PathTransition.this;
                }

                @Override
                public String getName() {
                    return "duration";
                }
            };
        }
        return this.duration;
    }

    public final void setPath(Shape shape) {
        if (this.path != null || shape != null) {
            this.pathProperty().set(shape);
        }
    }

    public final Shape getPath() {
        return this.path == null ? DEFAULT_PATH : (Shape)this.path.get();
    }

    public final ObjectProperty<Shape> pathProperty() {
        if (this.path == null) {
            this.path = new SimpleObjectProperty<Shape>(this, "path", DEFAULT_PATH);
        }
        return this.path;
    }

    public final void setOrientation(OrientationType orientationType) {
        if (this.orientation != null || !DEFAULT_ORIENTATION.equals((Object)orientationType)) {
            this.orientationProperty().set(orientationType);
        }
    }

    public final OrientationType getOrientation() {
        return this.orientation == null ? OrientationType.NONE : (OrientationType)((Object)this.orientation.get());
    }

    public final ObjectProperty<OrientationType> orientationProperty() {
        if (this.orientation == null) {
            this.orientation = new SimpleObjectProperty<OrientationType>(this, "orientation", DEFAULT_ORIENTATION);
        }
        return this.orientation;
    }

    public PathTransition(Duration duration, Shape shape, Node node) {
        this.setDuration(duration);
        this.setPath(shape);
        this.setNode(node);
        this.setCycleDuration(duration);
    }

    public PathTransition(Duration duration, Shape shape) {
        this(duration, shape, null);
    }

    public PathTransition() {
        this(DEFAULT_DURATION, null, null);
    }

    @Override
    public void interpolate(double d) {
        double d2 = this.totalLength * Math.min(1.0, Math.max(0.0, d));
        int n = this.findSegment(0, this.segments.size() - 1, d2);
        Segment segment = this.segments.get(n);
        double d3 = segment.accumLength - segment.length;
        double d4 = d2 - d3;
        double d5 = d4 / segment.length;
        Segment segment2 = segment.prevSeg;
        double d6 = segment2.toX + (segment.toX - segment2.toX) * d5;
        double d7 = segment2.toY + (segment.toY - segment2.toY) * d5;
        double d8 = segment.rotateAngle;
        double d9 = Math.min(10.0, segment.length / 2.0);
        if (d4 < d9 && !segment2.isMoveTo) {
            d8 = PathTransition.interpolate(segment2.rotateAngle, segment.rotateAngle, d4 / d9 / 2.0 + 0.5);
        } else {
            double d10 = segment.length - d4;
            Segment segment3 = segment.nextSeg;
            if (d10 < d9 && segment3 != null && !segment3.isMoveTo) {
                d8 = PathTransition.interpolate(segment.rotateAngle, segment3.rotateAngle, (d9 - d10) / d9 / 2.0);
            }
        }
        this.cachedNode.setTranslateX(d6 - NodeHelper.getPivotX(this.cachedNode));
        this.cachedNode.setTranslateY(d7 - NodeHelper.getPivotY(this.cachedNode));
        if (this.cachedIsNormalRequired) {
            this.cachedNode.setRotate(d8);
        }
    }

    private Node getTargetNode() {
        Node node = this.getNode();
        return node != null ? node : this.getParentTargetNode();
    }

    @Override
    boolean startable(boolean bl) {
        return super.startable(bl) && (this.getTargetNode() != null && this.getPath() != null && !this.getPath().getLayoutBounds().isEmpty() || !bl && this.cachedNode != null);
    }

    @Override
    void sync(boolean bl) {
        super.sync(bl);
        if (bl || this.cachedNode == null) {
            this.cachedNode = this.getTargetNode();
            this.recomputeSegments();
            this.cachedIsNormalRequired = this.getOrientation() == OrientationType.ORTHOGONAL_TO_TANGENT;
        }
    }

    private void recomputeSegments() {
        this.segments.clear();
        Shape shape = this.getPath();
        Segment segment = Segment.getZeroSegment();
        Segment segment2 = Segment.getZeroSegment();
        float[] fArray = new float[6];
        PathIterator pathIterator = ShapeHelper.configShape(shape).getPathIterator(NodeHelper.getLeafTransform(shape), 1.0f);
        while (!pathIterator.isDone()) {
            Segment segment3 = null;
            int n = pathIterator.currentSegment(fArray);
            double d = fArray[0];
            double d2 = fArray[1];
            switch (n) {
                case 0: {
                    segment3 = segment = Segment.newMoveTo(d, d2, segment2.accumLength);
                    break;
                }
                case 4: {
                    segment3 = Segment.newClosePath(segment2, segment);
                    if (segment3 != null) break;
                    segment2.convertToClosePath(segment);
                    break;
                }
                case 1: {
                    segment3 = Segment.newLineTo(segment2, d, d2);
                }
            }
            if (segment3 != null) {
                this.segments.add(segment3);
                segment2 = segment3;
            }
            pathIterator.next();
        }
        this.totalLength = segment2.accumLength;
    }

    private int findSegment(int n, int n2, double d) {
        if (n == n2) {
            return this.segments.get((int)n).isMoveTo && n > 0 ? this.findSegment(n - 1, n - 1, d) : n;
        }
        int n3 = n + (n2 - n) / 2;
        return this.segments.get((int)n3).accumLength > d ? this.findSegment(n, n3, d) : this.findSegment(n3 + 1, n2, d);
    }

    private static double interpolate(double d, double d2, double d3) {
        double d4 = d2 - d;
        if (Math.abs(d4) > 180.0) {
            d2 += d4 > 0.0 ? -360.0 : 360.0;
        }
        return PathTransition.normalize(d + d3 * (d2 - d));
    }

    private static double normalize(double d) {
        while (d > 360.0) {
            d -= 360.0;
        }
        while (d < 0.0) {
            d += 360.0;
        }
        return d;
    }

    private static class Segment {
        private static final Segment zeroSegment = new Segment(true, 0.0, 0.0, 0.0, 0.0, 0.0);
        boolean isMoveTo;
        double length;
        double accumLength;
        double toX;
        double toY;
        double rotateAngle;
        Segment prevSeg;
        Segment nextSeg;

        private Segment(boolean bl, double d, double d2, double d3, double d4, double d5) {
            this.isMoveTo = bl;
            this.toX = d;
            this.toY = d2;
            this.length = d3;
            this.accumLength = d4 + d3;
            this.rotateAngle = d5;
        }

        public static Segment getZeroSegment() {
            return zeroSegment;
        }

        public static Segment newMoveTo(double d, double d2, double d3) {
            return new Segment(true, d, d2, 0.0, d3, 0.0);
        }

        public static Segment newLineTo(Segment segment, double d, double d2) {
            double d3 = d - segment.toX;
            double d4 = d2 - segment.toY;
            double d5 = Math.sqrt(d3 * d3 + d4 * d4);
            if (d5 >= 1.0 || segment.isMoveTo) {
                Segment segment2;
                double d6 = Math.signum(d4 == 0.0 ? d3 : d4);
                double d7 = d6 * Math.acos(d3 / d5);
                d7 = PathTransition.normalize(d7 / Math.PI * 180.0);
                segment.nextSeg = segment2 = new Segment(false, d, d2, d5, segment.accumLength, d7);
                segment2.prevSeg = segment;
                return segment2;
            }
            return null;
        }

        public static Segment newClosePath(Segment segment, Segment segment2) {
            Segment segment3 = Segment.newLineTo(segment, segment2.toX, segment2.toY);
            if (segment3 != null) {
                segment3.convertToClosePath(segment2);
            }
            return segment3;
        }

        public void convertToClosePath(Segment segment) {
            Segment segment2;
            this.nextSeg = segment2 = segment.nextSeg;
            segment2.prevSeg = this;
        }
    }

    public static enum OrientationType {
        NONE,
        ORTHOGONAL_TO_TANGENT;

    }
}

