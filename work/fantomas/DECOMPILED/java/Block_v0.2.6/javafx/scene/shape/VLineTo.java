/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.scene.shape.VLineToHelper;
import com.sun.javafx.sg.prism.NGPath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.shape.PathElement;

public class VLineTo
extends PathElement {
    private DoubleProperty y = new DoublePropertyBase(){

        @Override
        public void invalidated() {
            VLineTo.this.u();
        }

        @Override
        public Object getBean() {
            return VLineTo.this;
        }

        @Override
        public String getName() {
            return "y";
        }
    };

    public VLineTo() {
        VLineToHelper.initHelper(this);
    }

    public VLineTo(double d) {
        this.setY(d);
        VLineToHelper.initHelper(this);
    }

    public final void setY(double d) {
        this.y.set(d);
    }

    public final double getY() {
        return this.y.get();
    }

    public final DoubleProperty yProperty() {
        return this.y;
    }

    @Override
    void addTo(NGPath nGPath) {
        if (this.isAbsolute()) {
            nGPath.addLineTo(nGPath.getCurrentX(), (float)this.getY());
        } else {
            nGPath.addLineTo(nGPath.getCurrentX(), (float)((double)nGPath.getCurrentY() + this.getY()));
        }
    }

    private void doAddTo(Path2D path2D) {
        if (this.isAbsolute()) {
            path2D.lineTo(path2D.getCurrentX(), (float)this.getY());
        } else {
            path2D.lineTo(path2D.getCurrentX(), (float)((double)path2D.getCurrentY() + this.getY()));
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("VLineTo[");
        stringBuilder.append("y=").append(this.getY());
        return stringBuilder.append("]").toString();
    }

    static {
        VLineToHelper.setVLineToAccessor(new VLineToHelper.VLineToAccessor(){

            @Override
            public void doAddTo(PathElement pathElement, Path2D path2D) {
                ((VLineTo)pathElement).doAddTo(path2D);
            }
        });
    }
}

