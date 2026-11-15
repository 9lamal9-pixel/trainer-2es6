/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.scene.shape.CubicCurveToHelper;
import com.sun.javafx.sg.prism.NGPath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.shape.PathElement;

public class CubicCurveTo
extends PathElement {
    private DoubleProperty controlX1;
    private DoubleProperty controlY1;
    private DoubleProperty controlX2;
    private DoubleProperty controlY2;
    private DoubleProperty x;
    private DoubleProperty y;

    public CubicCurveTo() {
        CubicCurveToHelper.initHelper(this);
    }

    public CubicCurveTo(double d, double d2, double d3, double d4, double d5, double d6) {
        this.setControlX1(d);
        this.setControlY1(d2);
        this.setControlX2(d3);
        this.setControlY2(d4);
        this.setX(d5);
        this.setY(d6);
        CubicCurveToHelper.initHelper(this);
    }

    public final void setControlX1(double d) {
        if (this.controlX1 != null || d != 0.0) {
            this.controlX1Property().set(d);
        }
    }

    public final double getControlX1() {
        return this.controlX1 == null ? 0.0 : this.controlX1.get();
    }

    public final DoubleProperty controlX1Property() {
        if (this.controlX1 == null) {
            this.controlX1 = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    CubicCurveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return CubicCurveTo.this;
                }

                @Override
                public String getName() {
                    return "controlX1";
                }
            };
        }
        return this.controlX1;
    }

    public final void setControlY1(double d) {
        if (this.controlY1 != null || d != 0.0) {
            this.controlY1Property().set(d);
        }
    }

    public final double getControlY1() {
        return this.controlY1 == null ? 0.0 : this.controlY1.get();
    }

    public final DoubleProperty controlY1Property() {
        if (this.controlY1 == null) {
            this.controlY1 = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    CubicCurveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return CubicCurveTo.this;
                }

                @Override
                public String getName() {
                    return "controlY1";
                }
            };
        }
        return this.controlY1;
    }

    public final void setControlX2(double d) {
        if (this.controlX2 != null || d != 0.0) {
            this.controlX2Property().set(d);
        }
    }

    public final double getControlX2() {
        return this.controlX2 == null ? 0.0 : this.controlX2.get();
    }

    public final DoubleProperty controlX2Property() {
        if (this.controlX2 == null) {
            this.controlX2 = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    CubicCurveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return CubicCurveTo.this;
                }

                @Override
                public String getName() {
                    return "controlX2";
                }
            };
        }
        return this.controlX2;
    }

    public final void setControlY2(double d) {
        if (this.controlY2 != null || d != 0.0) {
            this.controlY2Property().set(d);
        }
    }

    public final double getControlY2() {
        return this.controlY2 == null ? 0.0 : this.controlY2.get();
    }

    public final DoubleProperty controlY2Property() {
        if (this.controlY2 == null) {
            this.controlY2 = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    CubicCurveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return CubicCurveTo.this;
                }

                @Override
                public String getName() {
                    return "controlY2";
                }
            };
        }
        return this.controlY2;
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
                    CubicCurveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return CubicCurveTo.this;
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
                    CubicCurveTo.this.u();
                }

                @Override
                public Object getBean() {
                    return CubicCurveTo.this;
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
            nGPath.addCubicTo((float)this.getControlX1(), (float)this.getControlY1(), (float)this.getControlX2(), (float)this.getControlY2(), (float)this.getX(), (float)this.getY());
        } else {
            double d = nGPath.getCurrentX();
            double d2 = nGPath.getCurrentY();
            nGPath.addCubicTo((float)(this.getControlX1() + d), (float)(this.getControlY1() + d2), (float)(this.getControlX2() + d), (float)(this.getControlY2() + d2), (float)(this.getX() + d), (float)(this.getY() + d2));
        }
    }

    private void doAddTo(Path2D path2D) {
        if (this.isAbsolute()) {
            path2D.curveTo((float)this.getControlX1(), (float)this.getControlY1(), (float)this.getControlX2(), (float)this.getControlY2(), (float)this.getX(), (float)this.getY());
        } else {
            double d = path2D.getCurrentX();
            double d2 = path2D.getCurrentY();
            path2D.curveTo((float)(this.getControlX1() + d), (float)(this.getControlY1() + d2), (float)(this.getControlX2() + d), (float)(this.getControlY2() + d2), (float)(this.getX() + d), (float)(this.getY() + d2));
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("CubicCurveTo[");
        stringBuilder.append("x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        stringBuilder.append(", controlX1=").append(this.getControlX1());
        stringBuilder.append(", controlY1=").append(this.getControlY1());
        stringBuilder.append(", controlX2=").append(this.getControlX2());
        stringBuilder.append(", controlY2=").append(this.getControlY2());
        return stringBuilder.append("]").toString();
    }

    static {
        CubicCurveToHelper.setCubicCurveToAccessor(new CubicCurveToHelper.CubicCurveToAccessor(){

            @Override
            public void doAddTo(PathElement pathElement, Path2D path2D) {
                ((CubicCurveTo)pathElement).doAddTo(path2D);
            }
        });
    }
}

