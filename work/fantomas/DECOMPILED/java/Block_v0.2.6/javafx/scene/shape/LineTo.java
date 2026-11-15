/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.scene.shape.LineToHelper;
import com.sun.javafx.sg.prism.NGPath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.shape.PathElement;

public class LineTo
extends PathElement {
    private DoubleProperty x;
    private DoubleProperty y;

    public LineTo() {
        LineToHelper.initHelper(this);
    }

    public LineTo(double d, double d2) {
        this.setX(d);
        this.setY(d2);
        LineToHelper.initHelper(this);
    }

    public final void setX(double d) {
        if (this.x != null || d != 0.0) {
            this.xProperty().set(d);
        }
    }

    public final double getX() {
        return this.x == null ? 0.0 : this.x.get();
    }

    public final DoubleProperty xProperty() {
        if (this.x == null) {
            this.x = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    LineTo.this.u();
                }

                @Override
                public Object getBean() {
                    return LineTo.this;
                }

                @Override
                public String getName() {
                    return "x";
                }
            };
        }
        return this.x;
    }

    public final void setY(double d) {
        if (this.y != null || d != 0.0) {
            this.yProperty().set(d);
        }
    }

    public final double getY() {
        return this.y == null ? 0.0 : this.y.get();
    }

    public final DoubleProperty yProperty() {
        if (this.y == null) {
            this.y = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    LineTo.this.u();
                }

                @Override
                public Object getBean() {
                    return LineTo.this;
                }

                @Override
                public String getName() {
                    return "y";
                }
            };
        }
        return this.y;
    }

    @Override
    void addTo(NGPath nGPath) {
        if (this.isAbsolute()) {
            nGPath.addLineTo((float)this.getX(), (float)this.getY());
        } else {
            nGPath.addLineTo((float)((double)nGPath.getCurrentX() + this.getX()), (float)((double)nGPath.getCurrentY() + this.getY()));
        }
    }

    private void doAddTo(Path2D path2D) {
        if (this.isAbsolute()) {
            path2D.lineTo((float)this.getX(), (float)this.getY());
        } else {
            path2D.lineTo((float)((double)path2D.getCurrentX() + this.getX()), (float)((double)path2D.getCurrentY() + this.getY()));
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("LineTo[");
        stringBuilder.append("x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        return stringBuilder.append("]").toString();
    }

    static {
        LineToHelper.setLineToAccessor(new LineToHelper.LineToAccessor(){

            @Override
            public void doAddTo(PathElement pathElement, Path2D path2D) {
                ((LineTo)pathElement).doAddTo(path2D);
            }
        });
    }
}

