/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.Arc2D;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.PathIterator;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.shape.ArcToHelper;
import com.sun.javafx.sg.prism.NGPath;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.shape.PathElement;

public class ArcTo
extends PathElement {
    private DoubleProperty radiusX = new DoublePropertyBase(){

        @Override
        public void invalidated() {
            ArcTo.this.u();
        }

        @Override
        public Object getBean() {
            return ArcTo.this;
        }

        @Override
        public String getName() {
            return "radiusX";
        }
    };
    private DoubleProperty radiusY = new DoublePropertyBase(){

        @Override
        public void invalidated() {
            ArcTo.this.u();
        }

        @Override
        public Object getBean() {
            return ArcTo.this;
        }

        @Override
        public String getName() {
            return "radiusY";
        }
    };
    private DoubleProperty xAxisRotation;
    private BooleanProperty largeArcFlag;
    private BooleanProperty sweepFlag;
    private DoubleProperty x;
    private DoubleProperty y;

    public ArcTo() {
        ArcToHelper.initHelper(this);
    }

    public ArcTo(double d, double d2, double d3, double d4, double d5, boolean bl, boolean bl2) {
        this.setRadiusX(d);
        this.setRadiusY(d2);
        this.setXAxisRotation(d3);
        this.setX(d4);
        this.setY(d5);
        this.setLargeArcFlag(bl);
        this.setSweepFlag(bl2);
        ArcToHelper.initHelper(this);
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

    public final void setXAxisRotation(double d) {
        if (this.xAxisRotation != null || d != 0.0) {
            this.XAxisRotationProperty().set(d);
        }
    }

    public final double getXAxisRotation() {
        return this.xAxisRotation == null ? 0.0 : this.xAxisRotation.get();
    }

    public final DoubleProperty XAxisRotationProperty() {
        if (this.xAxisRotation == null) {
            this.xAxisRotation = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    ArcTo.this.u();
                }

                @Override
                public Object getBean() {
                    return ArcTo.this;
                }

                @Override
                public String getName() {
                    return "XAxisRotation";
                }
            };
        }
        return this.xAxisRotation;
    }

    public final void setLargeArcFlag(boolean bl) {
        if (this.largeArcFlag != null || bl) {
            this.largeArcFlagProperty().set(bl);
        }
    }

    public final boolean isLargeArcFlag() {
        return this.largeArcFlag == null ? false : this.largeArcFlag.get();
    }

    public final BooleanProperty largeArcFlagProperty() {
        if (this.largeArcFlag == null) {
            this.largeArcFlag = new BooleanPropertyBase(){

                @Override
                public void invalidated() {
                    ArcTo.this.u();
                }

                @Override
                public Object getBean() {
                    return ArcTo.this;
                }

                @Override
                public String getName() {
                    return "largeArcFlag";
                }
            };
        }
        return this.largeArcFlag;
    }

    public final void setSweepFlag(boolean bl) {
        if (this.sweepFlag != null || bl) {
            this.sweepFlagProperty().set(bl);
        }
    }

    public final boolean isSweepFlag() {
        return this.sweepFlag == null ? false : this.sweepFlag.get();
    }

    public final BooleanProperty sweepFlagProperty() {
        if (this.sweepFlag == null) {
            this.sweepFlag = new BooleanPropertyBase(){

                @Override
                public void invalidated() {
                    ArcTo.this.u();
                }

                @Override
                public Object getBean() {
                    return ArcTo.this;
                }

                @Override
                public String getName() {
                    return "sweepFlag";
                }
            };
        }
        return this.sweepFlag;
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
                    ArcTo.this.u();
                }

                @Override
                public Object getBean() {
                    return ArcTo.this;
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
                    ArcTo.this.u();
                }

                @Override
                public Object getBean() {
                    return ArcTo.this;
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
        this.addArcTo(nGPath, null, nGPath.getCurrentX(), nGPath.getCurrentY());
    }

    private void doAddTo(Path2D path2D) {
        this.addArcTo(null, path2D, path2D.getCurrentX(), path2D.getCurrentY());
    }

    private void addArcTo(NGPath nGPath, Path2D path2D, double d, double d2) {
        double d3;
        double d4;
        double d5;
        double d6 = this.getX();
        double d7 = this.getY();
        boolean bl = this.isSweepFlag();
        boolean bl2 = this.isLargeArcFlag();
        double d8 = this.isAbsolute() ? d6 : d6 + d;
        double d9 = this.isAbsolute() ? d7 : d7 + d2;
        double d10 = (d - d8) / 2.0;
        double d11 = (d2 - d9) / 2.0;
        double d12 = Math.toRadians(this.getXAxisRotation());
        double d13 = Math.cos(d12);
        double d14 = Math.sin(d12);
        double d15 = d13 * d10 + d14 * d11;
        double d16 = -d14 * d10 + d13 * d11;
        double d17 = d15 * d15;
        double d18 = Math.abs(this.getRadiusX());
        double d19 = d18 * d18;
        double d20 = d17 / d19 + (d5 = d16 * d16) / (d4 = (d3 = Math.abs(this.getRadiusY())) * d3);
        if (d20 > 1.0) {
            d18 = Math.sqrt(d20) * d18;
            d3 = Math.sqrt(d20) * d3;
            if (d18 != d18 || d3 != d3) {
                if (nGPath == null) {
                    path2D.lineTo((float)d8, (float)d9);
                } else {
                    nGPath.addLineTo((float)d8, (float)d9);
                }
                return;
            }
            d19 = d18 * d18;
            d4 = d3 * d3;
        }
        double d21 = bl2 == bl ? -1.0 : 1.0;
        double d22 = (d19 * d4 - d19 * d5 - d4 * d17) / (d19 * d5 + d4 * d17);
        d22 = d22 < 0.0 ? 0.0 : d22;
        double d23 = d21 * Math.sqrt(d22);
        double d24 = d23 * (d18 * d16 / d3);
        double d25 = d23 * -(d3 * d15 / d18);
        double d26 = (d + d8) / 2.0;
        double d27 = (d2 + d9) / 2.0;
        double d28 = d26 + (d13 * d24 - d14 * d25);
        double d29 = d27 + (d14 * d24 + d13 * d25);
        double d30 = (d15 - d24) / d18;
        double d31 = (d16 - d25) / d3;
        double d32 = (-d15 - d24) / d18;
        double d33 = (-d16 - d25) / d3;
        double d34 = Math.sqrt(d30 * d30 + d31 * d31);
        double d35 = d30;
        d21 = d31 < 0.0 ? -1.0 : 1.0;
        double d36 = Math.toDegrees(d21 * Math.acos(d35 / d34));
        d34 = Math.sqrt((d30 * d30 + d31 * d31) * (d32 * d32 + d33 * d33));
        d35 = d30 * d32 + d31 * d33;
        d21 = d30 * d33 - d31 * d32 < 0.0 ? -1.0 : 1.0;
        double d37 = Math.toDegrees(d21 * Math.acos(d35 / d34));
        if (!bl && d37 > 0.0) {
            d37 -= 360.0;
        } else if (bl && d37 < 0.0) {
            d37 += 360.0;
        }
        d37 %= 360.0;
        float f = (float)(d28 - d18);
        float f2 = (float)(d29 - d3);
        float f3 = (float)(d18 * 2.0);
        float f4 = (float)(d3 * 2.0);
        float f5 = (float)(-(d36 %= 360.0));
        float f6 = (float)(-d37);
        if (nGPath == null) {
            Arc2D arc2D = new Arc2D(f, f2, f3, f4, f5, f6, 0);
            BaseTransform baseTransform = d12 == 0.0 ? null : BaseTransform.getRotateInstance(d12, d28, d29);
            PathIterator pathIterator = arc2D.getPathIterator(baseTransform);
            pathIterator.next();
            path2D.append(pathIterator, true);
        } else {
            nGPath.addArcTo(f, f2, f3, f4, f5, f6, (float)d12);
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ArcTo[");
        stringBuilder.append("x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        stringBuilder.append(", radiusX=").append(this.getRadiusX());
        stringBuilder.append(", radiusY=").append(this.getRadiusY());
        stringBuilder.append(", xAxisRotation=").append(this.getXAxisRotation());
        if (this.isLargeArcFlag()) {
            stringBuilder.append(", lartArcFlag");
        }
        if (this.isSweepFlag()) {
            stringBuilder.append(", sweepFlag");
        }
        return stringBuilder.append("]").toString();
    }

    static {
        ArcToHelper.setArcToAccessor(new ArcToHelper.ArcToAccessor(){

            @Override
            public void doAddTo(PathElement pathElement, Path2D path2D) {
                ((ArcTo)pathElement).doAddTo(path2D);
            }
        });
    }
}

