/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.Ellipse2D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.CircleHelper;
import com.sun.javafx.scene.shape.ShapeHelper;
import com.sun.javafx.sg.prism.NGCircle;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGShape;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

public class Circle
extends Shape {
    private final Ellipse2D shape = new Ellipse2D();
    private DoubleProperty centerX;
    private DoubleProperty centerY;
    private final DoubleProperty radius;

    public Circle(double d) {
        CircleHelper.initHelper(this);
        this.radius = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(Circle.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Circle.this);
            }

            @Override
            public Object getBean() {
                return Circle.this;
            }

            @Override
            public String getName() {
                return "radius";
            }
        };
        this.setRadius(d);
    }

    public Circle(double d, Paint paint) {
        CircleHelper.initHelper(this);
        this.radius = new /* invalid duplicate definition of identical inner class */;
        this.setRadius(d);
        this.setFill(paint);
    }

    public Circle() {
        CircleHelper.initHelper(this);
        this.radius = new /* invalid duplicate definition of identical inner class */;
    }

    public Circle(double d, double d2, double d3) {
        CircleHelper.initHelper(this);
        this.radius = new /* invalid duplicate definition of identical inner class */;
        this.setCenterX(d);
        this.setCenterY(d2);
        this.setRadius(d3);
    }

    public Circle(double d, double d2, double d3, Paint paint) {
        CircleHelper.initHelper(this);
        this.radius = new /* invalid duplicate definition of identical inner class */;
        this.setCenterX(d);
        this.setCenterY(d2);
        this.setRadius(d3);
        this.setFill(paint);
    }

    public final void setCenterX(double d) {
        if (this.centerX != null || d != 0.0) {
            this.centerXProperty().set(d);
        }
    }

    public final double getCenterX() {
        return this.centerX == null ? 0.0 : this.centerX.get();
    }

    public final DoubleProperty centerXProperty() {
        if (this.centerX == null) {
            this.centerX = new DoublePropertyBase(0.0){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Circle.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(Circle.this);
                }

                @Override
                public Object getBean() {
                    return Circle.this;
                }

                @Override
                public String getName() {
                    return "centerX";
                }
            };
        }
        return this.centerX;
    }

    public final void setCenterY(double d) {
        if (this.centerY != null || d != 0.0) {
            this.centerYProperty().set(d);
        }
    }

    public final double getCenterY() {
        return this.centerY == null ? 0.0 : this.centerY.get();
    }

    public final DoubleProperty centerYProperty() {
        if (this.centerY == null) {
            this.centerY = new DoublePropertyBase(0.0){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Circle.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(Circle.this);
                }

                @Override
                public Object getBean() {
                    return Circle.this;
                }

                @Override
                public String getName() {
                    return "centerY";
                }
            };
        }
        return this.centerY;
    }

    public final void setRadius(double d) {
        this.radius.set(d);
    }

    public final double getRadius() {
        return this.radius.get();
    }

    public final DoubleProperty radiusProperty() {
        return this.radius;
    }

    @Override
    StrokeLineJoin convertLineJoin(StrokeLineJoin strokeLineJoin) {
        return StrokeLineJoin.BEVEL;
    }

    private NGNode doCreatePeer() {
        return new NGCircle();
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        if (this.getMode() == NGShape.Mode.EMPTY) {
            return baseBounds.makeEmpty();
        }
        double d = this.getCenterX();
        double d2 = this.getCenterY();
        if ((baseTransform.getType() & 0xFFFFFFE6) == 0) {
            double d3 = d * baseTransform.getMxx() + d2 * baseTransform.getMxy() + baseTransform.getMxt();
            double d4 = d * baseTransform.getMyx() + d2 * baseTransform.getMyy() + baseTransform.getMyt();
            double d5 = this.getRadius();
            if (this.getMode() != NGShape.Mode.FILL && this.getStrokeType() != StrokeType.INSIDE) {
                double d6 = this.getStrokeWidth();
                if (this.getStrokeType() == StrokeType.CENTERED) {
                    d6 /= 2.0;
                }
                d5 += d6;
            }
            return baseBounds.deriveWithNewBounds((float)(d3 - d5), (float)(d4 - d5), 0.0f, (float)(d3 + d5), (float)(d4 + d5), 0.0f);
        }
        if ((baseTransform.getType() & 0xFFFFFFB8) == 0) {
            double d7;
            double d8 = this.getRadius();
            double d9 = this.getCenterX() - d8;
            double d10 = this.getCenterY() - d8;
            double d11 = d7 = 2.0 * d8;
            double d12 = this.getMode() == NGShape.Mode.FILL || this.getStrokeType() == StrokeType.INSIDE ? 0.0 : this.getStrokeWidth();
            return this.computeBounds(baseBounds, baseTransform, d12, 0.0, d9, d10, d7, d11);
        }
        return this.computeShapeBounds(baseBounds, baseTransform, ShapeHelper.configShape(this));
    }

    private Ellipse2D doConfigShape() {
        double d = this.getRadius();
        this.shape.setFrame((float)(this.getCenterX() - d), (float)(this.getCenterY() - d), (float)(d * 2.0), (float)(d * 2.0));
        return this.shape;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            NGCircle nGCircle = (NGCircle)NodeHelper.getPeer(this);
            nGCircle.updateCircle((float)this.getCenterX(), (float)this.getCenterY(), (float)this.getRadius());
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Circle[");
        String string = this.getId();
        if (string != null) {
            stringBuilder.append("id=").append(string).append(", ");
        }
        stringBuilder.append("centerX=").append(this.getCenterX());
        stringBuilder.append(", centerY=").append(this.getCenterY());
        stringBuilder.append(", radius=").append(this.getRadius());
        stringBuilder.append(", fill=").append(this.getFill());
        Paint paint = this.getStroke();
        if (paint != null) {
            stringBuilder.append(", stroke=").append(paint);
            stringBuilder.append(", strokeWidth=").append(this.getStrokeWidth());
        }
        return stringBuilder.append("]").toString();
    }

    static {
        CircleHelper.setCircleAccessor(new CircleHelper.CircleAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Circle)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Circle)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Circle)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public com.sun.javafx.geom.Shape doConfigShape(Shape shape) {
                return ((Circle)shape).doConfigShape();
            }
        });
    }
}

