/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.Ellipse2D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.EllipseHelper;
import com.sun.javafx.scene.shape.ShapeHelper;
import com.sun.javafx.sg.prism.NGEllipse;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGShape;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

public class Ellipse
extends Shape {
    private final Ellipse2D shape = new Ellipse2D();
    private static final int NON_RECTILINEAR_TYPE_MASK = -80;
    private DoubleProperty centerX;
    private DoubleProperty centerY;
    private final DoubleProperty radiusX;
    private final DoubleProperty radiusY;

    public Ellipse() {
        EllipseHelper.initHelper(this);
        this.radiusX = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(Ellipse.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Ellipse.this);
            }

            @Override
            public Object getBean() {
                return Ellipse.this;
            }

            @Override
            public String getName() {
                return "radiusX";
            }
        };
        this.radiusY = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(Ellipse.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Ellipse.this);
            }

            @Override
            public Object getBean() {
                return Ellipse.this;
            }

            @Override
            public String getName() {
                return "radiusY";
            }
        };
    }

    public Ellipse(double d, double d2) {
        EllipseHelper.initHelper(this);
        this.radiusX = new /* invalid duplicate definition of identical inner class */;
        this.radiusY = new /* invalid duplicate definition of identical inner class */;
        this.setRadiusX(d);
        this.setRadiusY(d2);
    }

    public Ellipse(double d, double d2, double d3, double d4) {
        this(d3, d4);
        this.setCenterX(d);
        this.setCenterY(d2);
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
            this.centerX = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Ellipse.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(Ellipse.this);
                }

                @Override
                public Object getBean() {
                    return Ellipse.this;
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
            this.centerY = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Ellipse.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(Ellipse.this);
                }

                @Override
                public Object getBean() {
                    return Ellipse.this;
                }

                @Override
                public String getName() {
                    return "centerY";
                }
            };
        }
        return this.centerY;
    }

    public final void setRadiusX(double d) {
        this.radiusX.set(d);
    }

    public final double getRadiusX() {
        return this.radiusX.get();
    }

    public final DoubleProperty radiusXProperty() {
        return this.radiusX;
    }

    public final void setRadiusY(double d) {
        this.radiusY.set(d);
    }

    public final double getRadiusY() {
        return this.radiusY.get();
    }

    public final DoubleProperty radiusYProperty() {
        return this.radiusY;
    }

    private NGNode doCreatePeer() {
        return new NGEllipse();
    }

    @Override
    StrokeLineJoin convertLineJoin(StrokeLineJoin strokeLineJoin) {
        return StrokeLineJoin.BEVEL;
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        double d;
        double d2;
        if (this.getMode() == NGShape.Mode.EMPTY) {
            return baseBounds.makeEmpty();
        }
        if ((baseTransform.getType() & 0xFFFFFFB0) != 0) {
            return this.computeShapeBounds(baseBounds, baseTransform, ShapeHelper.configShape(this));
        }
        double d3 = this.getCenterX() - this.getRadiusX();
        double d4 = this.getCenterY() - this.getRadiusY();
        double d5 = 2.0 * this.getRadiusX();
        double d6 = 2.0 * this.getRadiusY();
        if (this.getMode() == NGShape.Mode.FILL || this.getStrokeType() == StrokeType.INSIDE) {
            d2 = 0.0;
            d = 0.0;
        } else {
            d = this.getStrokeWidth();
            if (this.getStrokeType() == StrokeType.CENTERED) {
                d /= 2.0;
            }
            d2 = 0.0;
        }
        return this.computeBounds(baseBounds, baseTransform, d, d2, d3, d4, d5, d6);
    }

    private Ellipse2D doConfigShape() {
        this.shape.setFrame((float)(this.getCenterX() - this.getRadiusX()), (float)(this.getCenterY() - this.getRadiusY()), (float)(this.getRadiusX() * 2.0), (float)(this.getRadiusY() * 2.0));
        return this.shape;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            NGEllipse nGEllipse = (NGEllipse)NodeHelper.getPeer(this);
            nGEllipse.updateEllipse((float)this.getCenterX(), (float)this.getCenterY(), (float)this.getRadiusX(), (float)this.getRadiusY());
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Ellipse[");
        String string = this.getId();
        if (string != null) {
            stringBuilder.append("id=").append(string).append(", ");
        }
        stringBuilder.append("centerX=").append(this.getCenterX());
        stringBuilder.append(", centerY=").append(this.getCenterY());
        stringBuilder.append(", radiusX=").append(this.getRadiusX());
        stringBuilder.append(", radiusY=").append(this.getRadiusY());
        stringBuilder.append(", fill=").append(this.getFill());
        Paint paint = this.getStroke();
        if (paint != null) {
            stringBuilder.append(", stroke=").append(paint);
            stringBuilder.append(", strokeWidth=").append(this.getStrokeWidth());
        }
        return stringBuilder.append("]").toString();
    }

    static {
        EllipseHelper.setEllipseAccessor(new EllipseHelper.EllipseAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Ellipse)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Ellipse)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Ellipse)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public com.sun.javafx.geom.Shape doConfigShape(Shape shape) {
                return ((Ellipse)shape).doConfigShape();
            }
        });
    }
}

