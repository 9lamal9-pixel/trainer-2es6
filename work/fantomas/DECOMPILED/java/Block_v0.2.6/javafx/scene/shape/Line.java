/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.Line2D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.LineHelper;
import com.sun.javafx.sg.prism.NGLine;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGShape;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

public class Line
extends Shape {
    private final Line2D shape = new Line2D();
    private final DoubleProperty startX;
    private final DoubleProperty startY;
    private final DoubleProperty endX;
    private final DoubleProperty endY;

    public Line() {
        LineHelper.initHelper(this);
        ((StyleableProperty)((Object)this.fillProperty())).applyStyle(null, null);
        ((StyleableProperty)((Object)this.strokeProperty())).applyStyle(null, Color.BLACK);
        this.startX = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(Line.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Line.this);
            }

            @Override
            public Object getBean() {
                return Line.this;
            }

            @Override
            public String getName() {
                return "startX";
            }
        };
        this.startY = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(Line.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Line.this);
            }

            @Override
            public Object getBean() {
                return Line.this;
            }

            @Override
            public String getName() {
                return "startY";
            }
        };
        this.endX = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(Line.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Line.this);
            }

            @Override
            public Object getBean() {
                return Line.this;
            }

            @Override
            public String getName() {
                return "endX";
            }
        };
        this.endY = new DoublePropertyBase(){

            @Override
            public void invalidated() {
                NodeHelper.markDirty(Line.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Line.this);
            }

            @Override
            public Object getBean() {
                return Line.this;
            }

            @Override
            public String getName() {
                return "endY";
            }
        };
    }

    public Line(double d, double d2, double d3, double d4) {
        LineHelper.initHelper(this);
        ((StyleableProperty)((Object)this.fillProperty())).applyStyle(null, null);
        ((StyleableProperty)((Object)this.strokeProperty())).applyStyle(null, Color.BLACK);
        this.startX = new /* invalid duplicate definition of identical inner class */;
        this.startY = new /* invalid duplicate definition of identical inner class */;
        this.endX = new /* invalid duplicate definition of identical inner class */;
        this.endY = new /* invalid duplicate definition of identical inner class */;
        this.setStartX(d);
        this.setStartY(d2);
        this.setEndX(d3);
        this.setEndY(d4);
    }

    public final void setStartX(double d) {
        this.startX.set(d);
    }

    public final double getStartX() {
        return this.startX.get();
    }

    public final DoubleProperty startXProperty() {
        return this.startX;
    }

    public final void setStartY(double d) {
        this.startY.set(d);
    }

    public final double getStartY() {
        return this.startY.get();
    }

    public final DoubleProperty startYProperty() {
        return this.startY;
    }

    public final void setEndX(double d) {
        this.endX.set(d);
    }

    public final double getEndX() {
        return this.endX.get();
    }

    public final DoubleProperty endXProperty() {
        return this.endX;
    }

    public final void setEndY(double d) {
        this.endY.set(d);
    }

    public final double getEndY() {
        return this.endY.get();
    }

    public final DoubleProperty endYProperty() {
        return this.endY;
    }

    private NGNode doCreatePeer() {
        return new NGLine();
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        double d;
        double d2;
        if (this.getMode() == NGShape.Mode.FILL || this.getMode() == NGShape.Mode.EMPTY || this.getStrokeType() == StrokeType.INSIDE) {
            return baseBounds.makeEmpty();
        }
        double d3 = this.getStartX();
        double d4 = this.getEndX();
        double d5 = this.getStartY();
        double d6 = this.getEndY();
        double d7 = this.getStrokeWidth();
        if (this.getStrokeType() == StrokeType.CENTERED) {
            d7 /= 2.0;
        }
        if (baseTransform.isTranslateOrIdentity()) {
            double d8;
            double d9;
            double d10;
            d7 = Math.max(d7, 0.5);
            if (baseTransform.getType() == 1) {
                d10 = baseTransform.getMxt();
                double d11 = baseTransform.getMyt();
                d3 += d10;
                d5 += d11;
                d4 += d10;
                d6 += d11;
            }
            if (d5 == d6 && d3 != d4) {
                d9 = d7;
                d8 = this.getStrokeLineCap() == StrokeLineCap.BUTT ? 0.0 : d7;
            } else if (d3 == d4 && d5 != d6) {
                d8 = d7;
                d9 = this.getStrokeLineCap() == StrokeLineCap.BUTT ? 0.0 : d7;
            } else {
                if (this.getStrokeLineCap() == StrokeLineCap.SQUARE) {
                    d7 *= Math.sqrt(2.0);
                }
                d8 = d9 = d7;
            }
            if (d3 > d4) {
                d10 = d3;
                d3 = d4;
                d4 = d10;
            }
            if (d5 > d6) {
                d10 = d5;
                d5 = d6;
                d6 = d10;
            }
            baseBounds = baseBounds.deriveWithNewBounds((float)(d3 -= d8), (float)(d5 -= d9), 0.0f, (float)(d4 += d8), (float)(d6 += d9), 0.0f);
            return baseBounds;
        }
        double d12 = d4 - d3;
        double d13 = d6 - d5;
        double d14 = Math.sqrt(d12 * d12 + d13 * d13);
        if (d14 == 0.0) {
            d12 = d7;
            d13 = 0.0;
        } else {
            d12 = d7 * d12 / d14;
            d13 = d7 * d13 / d14;
        }
        if (this.getStrokeLineCap() != StrokeLineCap.BUTT) {
            d2 = d12;
            d = d13;
        } else {
            d = 0.0;
            d2 = 0.0;
        }
        double[] dArray = new double[]{d3 - d13 - d2, d5 + d12 - d, d3 + d13 - d2, d5 - d12 - d, d4 + d13 + d2, d6 - d12 + d, d4 - d13 + d2, d6 + d12 + d};
        baseTransform.transform(dArray, 0, dArray, 0, 4);
        d3 = Math.min(Math.min(dArray[0], dArray[2]), Math.min(dArray[4], dArray[6]));
        d5 = Math.min(Math.min(dArray[1], dArray[3]), Math.min(dArray[5], dArray[7]));
        d4 = Math.max(Math.max(dArray[0], dArray[2]), Math.max(dArray[4], dArray[6]));
        d6 = Math.max(Math.max(dArray[1], dArray[3]), Math.max(dArray[5], dArray[7]));
        baseBounds = baseBounds.deriveWithNewBounds((float)(d3 -= 0.5), (float)(d5 -= 0.5), 0.0f, (float)(d4 += 0.5), (float)(d6 += 0.5), 0.0f);
        return baseBounds;
    }

    private Line2D doConfigShape() {
        this.shape.setLine((float)this.getStartX(), (float)this.getStartY(), (float)this.getEndX(), (float)this.getEndY());
        return this.shape;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            NGLine nGLine = (NGLine)NodeHelper.getPeer(this);
            nGLine.updateLine((float)this.getStartX(), (float)this.getStartY(), (float)this.getEndX(), (float)this.getEndY());
        }
    }

    private Paint doCssGetFillInitialValue() {
        return null;
    }

    private Paint doCssGetStrokeInitialValue() {
        return Color.BLACK;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Line[");
        String string = this.getId();
        if (string != null) {
            stringBuilder.append("id=").append(string).append(", ");
        }
        stringBuilder.append("startX=").append(this.getStartX());
        stringBuilder.append(", startY=").append(this.getStartY());
        stringBuilder.append(", endX=").append(this.getEndX());
        stringBuilder.append(", endY=").append(this.getEndY());
        Paint paint = this.getStroke();
        if (paint != null) {
            stringBuilder.append(", stroke=").append(paint);
            stringBuilder.append(", strokeWidth=").append(this.getStrokeWidth());
        }
        return stringBuilder.append("]").toString();
    }

    static {
        LineHelper.setLineAccessor(new LineHelper.LineAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Line)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Line)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Line)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public Paint doCssGetFillInitialValue(Shape shape) {
                return ((Line)shape).doCssGetFillInitialValue();
            }

            @Override
            public Paint doCssGetStrokeInitialValue(Shape shape) {
                return ((Line)shape).doCssGetStrokeInitialValue();
            }

            @Override
            public com.sun.javafx.geom.Shape doConfigShape(Shape shape) {
                return ((Line)shape).doConfigShape();
            }
        });
    }
}

