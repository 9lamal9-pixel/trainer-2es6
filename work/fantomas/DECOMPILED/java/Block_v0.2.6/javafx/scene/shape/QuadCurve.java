/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.QuadCurve2D;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.QuadCurveHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGQuadCurve;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

public class QuadCurve
extends Shape {
    private final QuadCurve2D shape = new QuadCurve2D();
    private DoubleProperty startX;
    private DoubleProperty startY;
    private DoubleProperty controlX;
    private DoubleProperty controlY;
    private DoubleProperty endX;
    private DoubleProperty endY;

    public QuadCurve() {
        QuadCurveHelper.initHelper(this);
        this.controlX = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(QuadCurve.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(QuadCurve.this);
            }

            @Override
            public Object getBean() {
                return QuadCurve.this;
            }

            @Override
            public String getName() {
                return "controlX";
            }
        };
        this.controlY = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(QuadCurve.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(QuadCurve.this);
            }

            @Override
            public Object getBean() {
                return QuadCurve.this;
            }

            @Override
            public String getName() {
                return "controlY";
            }
        };
    }

    public QuadCurve(double d, double d2, double d3, double d4, double d5, double d6) {
        QuadCurveHelper.initHelper(this);
        this.controlX = new /* invalid duplicate definition of identical inner class */;
        this.controlY = new /* invalid duplicate definition of identical inner class */;
        this.setStartX(d);
        this.setStartY(d2);
        this.setControlX(d3);
        this.setControlY(d4);
        this.setEndX(d5);
        this.setEndY(d6);
    }

    public final void setStartX(double d) {
        if (this.startX != null || d != 0.0) {
            this.startXProperty().set(d);
        }
    }

    public final double getStartX() {
        return this.startX == null ? 0.0 : this.startX.get();
    }

    public final DoubleProperty startXProperty() {
        if (this.startX == null) {
            this.startX = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(QuadCurve.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(QuadCurve.this);
                }

                @Override
                public Object getBean() {
                    return QuadCurve.this;
                }

                @Override
                public String getName() {
                    return "startX";
                }
            };
        }
        return this.startX;
    }

    public final void setStartY(double d) {
        if (this.startY != null || d != 0.0) {
            this.startYProperty().set(d);
        }
    }

    public final double getStartY() {
        return this.startY == null ? 0.0 : this.startY.get();
    }

    public final DoubleProperty startYProperty() {
        if (this.startY == null) {
            this.startY = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(QuadCurve.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(QuadCurve.this);
                }

                @Override
                public Object getBean() {
                    return QuadCurve.this;
                }

                @Override
                public String getName() {
                    return "startY";
                }
            };
        }
        return this.startY;
    }

    public final void setControlX(double d) {
        this.controlX.set(d);
    }

    public final double getControlX() {
        return this.controlX.get();
    }

    public final DoubleProperty controlXProperty() {
        return this.controlX;
    }

    public final void setControlY(double d) {
        this.controlY.set(d);
    }

    public final double getControlY() {
        return this.controlY.get();
    }

    public final DoubleProperty controlYProperty() {
        return this.controlY;
    }

    public final void setEndX(double d) {
        if (this.endX != null || d != 0.0) {
            this.endXProperty().set(d);
        }
    }

    public final double getEndX() {
        return this.endX == null ? 0.0 : this.endX.get();
    }

    public final DoubleProperty endXProperty() {
        if (this.endX == null) {
            this.endX = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(QuadCurve.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(QuadCurve.this);
                }

                @Override
                public Object getBean() {
                    return QuadCurve.this;
                }

                @Override
                public String getName() {
                    return "endX";
                }
            };
        }
        return this.endX;
    }

    public final void setEndY(double d) {
        if (this.endY != null || d != 0.0) {
            this.endYProperty().set(d);
        }
    }

    public final double getEndY() {
        return this.endY == null ? 0.0 : this.endY.get();
    }

    public final DoubleProperty endYProperty() {
        if (this.endY == null) {
            this.endY = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(QuadCurve.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(QuadCurve.this);
                }

                @Override
                public Object getBean() {
                    return QuadCurve.this;
                }

                @Override
                public String getName() {
                    return "endY";
                }
            };
        }
        return this.endY;
    }

    private NGNode doCreatePeer() {
        return new NGQuadCurve();
    }

    private QuadCurve2D doConfigShape() {
        this.shape.x1 = (float)this.getStartX();
        this.shape.y1 = (float)this.getStartY();
        this.shape.ctrlx = (float)this.getControlX();
        this.shape.ctrly = (float)this.getControlY();
        this.shape.x2 = (float)this.getEndX();
        this.shape.y2 = (float)this.getEndY();
        return this.shape;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            NGQuadCurve nGQuadCurve = (NGQuadCurve)NodeHelper.getPeer(this);
            nGQuadCurve.updateQuadCurve((float)this.getStartX(), (float)this.getStartY(), (float)this.getEndX(), (float)this.getEndY(), (float)this.getControlX(), (float)this.getControlY());
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("QuadCurve[");
        String string = this.getId();
        if (string != null) {
            stringBuilder.append("id=").append(string).append(", ");
        }
        stringBuilder.append("startX=").append(this.getStartX());
        stringBuilder.append(", startY=").append(this.getStartY());
        stringBuilder.append(", controlX=").append(this.getControlX());
        stringBuilder.append(", controlY=").append(this.getControlY());
        stringBuilder.append(", endX=").append(this.getEndX());
        stringBuilder.append(", endY=").append(this.getEndY());
        stringBuilder.append(", fill=").append(this.getFill());
        Paint paint = this.getStroke();
        if (paint != null) {
            stringBuilder.append(", stroke=").append(paint);
            stringBuilder.append(", strokeWidth=").append(this.getStrokeWidth());
        }
        return stringBuilder.append("]").toString();
    }

    static {
        QuadCurveHelper.setQuadCurveAccessor(new QuadCurveHelper.QuadCurveAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((QuadCurve)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((QuadCurve)node).doUpdatePeer();
            }

            @Override
            public com.sun.javafx.geom.Shape doConfigShape(Shape shape) {
                return ((QuadCurve)shape).doConfigShape();
            }
        });
    }
}

