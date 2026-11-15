/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.scene.shape.HLineToHelper;
import com.sun.javafx.sg.prism.NGPath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.shape.PathElement;

public class HLineTo
extends PathElement {
    private DoubleProperty x = new DoublePropertyBase(){

        @Override
        public void invalidated() {
            HLineTo.this.u();
        }

        @Override
        public Object getBean() {
            return HLineTo.this;
        }

        @Override
        public String getName() {
            return "x";
        }
    };

    public HLineTo() {
        HLineToHelper.initHelper(this);
    }

    public HLineTo(double d) {
        this.setX(d);
        HLineToHelper.initHelper(this);
    }

    public final void setX(double d) {
        this.x.set(d);
    }

    public final double getX() {
        return this.x.get();
    }

    public final DoubleProperty xProperty() {
        return this.x;
    }

    @Override
    void addTo(NGPath nGPath) {
        if (this.isAbsolute()) {
            nGPath.addLineTo((float)this.getX(), nGPath.getCurrentY());
        } else {
            nGPath.addLineTo((float)((double)nGPath.getCurrentX() + this.getX()), nGPath.getCurrentY());
        }
    }

    private void doAddTo(Path2D path2D) {
        if (this.isAbsolute()) {
            path2D.lineTo((float)this.getX(), path2D.getCurrentY());
        } else {
            path2D.lineTo((float)((double)path2D.getCurrentX() + this.getX()), path2D.getCurrentY());
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("HLineTo[");
        stringBuilder.append("x=").append(this.getX());
        return stringBuilder.append("]").toString();
    }

    static {
        HLineToHelper.setHLineToAccessor(new HLineToHelper.HLineToAccessor(){

            @Override
            public void doAddTo(PathElement pathElement, Path2D path2D) {
                ((HLineTo)pathElement).doAddTo(path2D);
            }
        });
    }
}

