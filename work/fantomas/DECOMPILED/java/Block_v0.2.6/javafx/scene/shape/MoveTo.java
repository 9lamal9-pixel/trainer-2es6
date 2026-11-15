/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.scene.shape.MoveToHelper;
import com.sun.javafx.sg.prism.NGPath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.shape.PathElement;

public class MoveTo
extends PathElement {
    private DoubleProperty x;
    private DoubleProperty y;

    public MoveTo() {
        MoveToHelper.initHelper(this);
    }

    public MoveTo(double d, double d2) {
        this.setX(d);
        this.setY(d2);
        MoveToHelper.initHelper(this);
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
                    MoveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return MoveTo.this;
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
                    MoveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return MoveTo.this;
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
            nGPath.addMoveTo((float)this.getX(), (float)this.getY());
        } else {
            nGPath.addMoveTo((float)((double)nGPath.getCurrentX() + this.getX()), (float)((double)nGPath.getCurrentY() + this.getY()));
        }
    }

    private void doAddTo(Path2D path2D) {
        if (this.isAbsolute()) {
            path2D.moveTo((float)this.getX(), (float)this.getY());
        } else {
            path2D.moveTo((float)((double)path2D.getCurrentX() + this.getX()), (float)((double)path2D.getCurrentY() + this.getY()));
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("MoveTo[");
        stringBuilder.append("x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        return stringBuilder.append("]").toString();
    }

    static {
        MoveToHelper.setMoveToAccessor(new MoveToHelper.MoveToAccessor(){

            @Override
            public void doAddTo(PathElement pathElement, Path2D path2D) {
                ((MoveTo)pathElement).doAddTo(path2D);
            }
        });
    }
}

