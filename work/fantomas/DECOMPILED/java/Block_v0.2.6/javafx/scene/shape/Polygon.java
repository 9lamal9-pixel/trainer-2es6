/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.PolygonHelper;
import com.sun.javafx.scene.shape.ShapeHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGPolygon;
import com.sun.javafx.sg.prism.NGShape;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

public class Polygon
extends Shape {
    private final Path2D shape = new Path2D();
    private final ObservableList<Double> points;

    public Polygon() {
        PolygonHelper.initHelper(this);
        this.points = new TrackableObservableList<Double>(){

            @Override
            protected void onChanged(ListChangeListener.Change<Double> change) {
                NodeHelper.markDirty(Polygon.this, DirtyBits.NODE_GEOMETRY);
                NodeHelper.geomChanged(Polygon.this);
            }
        };
    }

    public Polygon(double ... dArray) {
        PolygonHelper.initHelper(this);
        this.points = new /* invalid duplicate definition of identical inner class */;
        if (dArray != null) {
            for (double d : dArray) {
                this.getPoints().add(d);
            }
        }
    }

    public final ObservableList<Double> getPoints() {
        return this.points;
    }

    private NGNode doCreatePeer() {
        return new NGPolygon();
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        if (this.getMode() == NGShape.Mode.EMPTY || this.getPoints().size() <= 1) {
            return baseBounds.makeEmpty();
        }
        if (this.getPoints().size() == 2) {
            if (this.getMode() == NGShape.Mode.FILL || this.getStrokeType() == StrokeType.INSIDE) {
                return baseBounds.makeEmpty();
            }
            double d = this.getStrokeWidth();
            if (this.getStrokeType() == StrokeType.CENTERED) {
                d /= 2.0;
            }
            return this.computeBounds(baseBounds, baseTransform, d, 0.5, (Double)this.getPoints().get(0), (Double)this.getPoints().get(1), 0.0, 0.0);
        }
        return this.computeShapeBounds(baseBounds, baseTransform, ShapeHelper.configShape(this));
    }

    private Path2D doConfigShape() {
        double d = (Double)this.getPoints().get(0);
        double d2 = (Double)this.getPoints().get(1);
        this.shape.reset();
        this.shape.moveTo((float)d, (float)d2);
        int n = this.getPoints().size() & 0xFFFFFFFE;
        for (int i = 2; i < n; i += 2) {
            d = (Double)this.getPoints().get(i);
            d2 = (Double)this.getPoints().get(i + 1);
            this.shape.lineTo((float)d, (float)d2);
        }
        this.shape.closePath();
        return this.shape;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            int n = this.getPoints().size() & 0xFFFFFFFE;
            float[] fArray = new float[n];
            for (int i = 0; i < n; ++i) {
                fArray[i] = (float)((Double)this.getPoints().get(i)).doubleValue();
            }
            NGPolygon nGPolygon = (NGPolygon)NodeHelper.getPeer(this);
            nGPolygon.updatePolygon(fArray);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Polygon[");
        String string = this.getId();
        if (string != null) {
            stringBuilder.append("id=").append(string).append(", ");
        }
        stringBuilder.append("points=").append(this.getPoints());
        stringBuilder.append(", fill=").append(this.getFill());
        Paint paint = this.getStroke();
        if (paint != null) {
            stringBuilder.append(", stroke=").append(paint);
            stringBuilder.append(", strokeWidth=").append(this.getStrokeWidth());
        }
        return stringBuilder.append("]").toString();
    }

    static {
        PolygonHelper.setPolygonAccessor(new PolygonHelper.PolygonAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Polygon)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Polygon)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Polygon)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public com.sun.javafx.geom.Shape doConfigShape(Shape shape) {
                return ((Polygon)shape).doConfigShape();
            }
        });
    }
}

