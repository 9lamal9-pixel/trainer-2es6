/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.transform;

import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

public class Shear
extends Transform {
    private DoubleProperty x;
    private DoubleProperty y;
    private DoubleProperty pivotX;
    private DoubleProperty pivotY;

    public Shear() {
    }

    public Shear(double d, double d2) {
        this.setX(d);
        this.setY(d2);
    }

    public Shear(double d, double d2, double d3, double d4) {
        this.setX(d);
        this.setY(d2);
        this.setPivotX(d3);
        this.setPivotY(d4);
    }

    public final void setX(double d) {
        this.xProperty().set(d);
    }

    public final double getX() {
        return this.x == null ? 0.0 : this.x.get();
    }

    public final DoubleProperty xProperty() {
        if (this.x == null) {
            this.x = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Shear.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Shear.this;
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
        this.yProperty().set(d);
    }

    public final double getY() {
        return this.y == null ? 0.0 : this.y.get();
    }

    public final DoubleProperty yProperty() {
        if (this.y == null) {
            this.y = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Shear.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Shear.this;
                }

                @Override
                public String getName() {
                    return "y";
                }
            };
        }
        return this.y;
    }

    public final void setPivotX(double d) {
        this.pivotXProperty().set(d);
    }

    public final double getPivotX() {
        return this.pivotX == null ? 0.0 : this.pivotX.get();
    }

    public final DoubleProperty pivotXProperty() {
        if (this.pivotX == null) {
            this.pivotX = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Shear.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Shear.this;
                }

                @Override
                public String getName() {
                    return "pivotX";
                }
            };
        }
        return this.pivotX;
    }

    public final void setPivotY(double d) {
        this.pivotYProperty().set(d);
    }

    public final double getPivotY() {
        return this.pivotY == null ? 0.0 : this.pivotY.get();
    }

    public final DoubleProperty pivotYProperty() {
        if (this.pivotY == null) {
            this.pivotY = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Shear.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Shear.this;
                }

                @Override
                public String getName() {
                    return "pivotY";
                }
            };
        }
        return this.pivotY;
    }

    @Override
    public double getMxy() {
        return this.getX();
    }

    @Override
    public double getMyx() {
        return this.getY();
    }

    @Override
    public double getTx() {
        return -this.getX() * this.getPivotY();
    }

    @Override
    public double getTy() {
        return -this.getY() * this.getPivotX();
    }

    @Override
    boolean computeIs2D() {
        return true;
    }

    @Override
    boolean computeIsIdentity() {
        return this.getX() == 0.0 && this.getY() == 0.0;
    }

    @Override
    void fill2DArray(double[] dArray) {
        double d = this.getX();
        double d2 = this.getY();
        dArray[0] = 1.0;
        dArray[1] = d;
        dArray[2] = -d * this.getPivotY();
        dArray[3] = d2;
        dArray[4] = 1.0;
        dArray[5] = -d2 * this.getPivotX();
    }

    @Override
    void fill3DArray(double[] dArray) {
        double d = this.getX();
        double d2 = this.getY();
        dArray[0] = 1.0;
        dArray[1] = d;
        dArray[2] = 0.0;
        dArray[3] = -d * this.getPivotY();
        dArray[4] = d2;
        dArray[5] = 1.0;
        dArray[6] = 0.0;
        dArray[7] = -d2 * this.getPivotX();
        dArray[8] = 0.0;
        dArray[9] = 0.0;
        dArray[10] = 1.0;
        dArray[11] = 0.0;
    }

    @Override
    public Transform createConcatenation(Transform transform) {
        if (transform instanceof Affine) {
            Affine affine = (Affine)transform.clone();
            affine.prepend(this);
            return affine;
        }
        double d = this.getX();
        double d2 = this.getY();
        double d3 = transform.getMxx();
        double d4 = transform.getMxy();
        double d5 = transform.getMxz();
        double d6 = transform.getTx();
        double d7 = transform.getMyx();
        double d8 = transform.getMyy();
        double d9 = transform.getMyz();
        double d10 = transform.getTy();
        return new Affine(d3 + d * d7, d4 + d * d8, d5 + d * d9, d6 + d * d10 - d * this.getPivotY(), d2 * d3 + d7, d2 * d4 + d8, d2 * d5 + d9, d2 * d6 + d10 - d2 * this.getPivotX(), transform.getMzx(), transform.getMzy(), transform.getMzz(), transform.getTz());
    }

    @Override
    public Transform createInverse() {
        double d = this.getX();
        double d2 = this.getY();
        if (d2 == 0.0) {
            return new Shear(-d, 0.0, 0.0, this.getPivotY());
        }
        if (d == 0.0) {
            return new Shear(0.0, -d2, this.getPivotX(), 0.0);
        }
        double d3 = this.getPivotX();
        double d4 = this.getPivotY();
        double d5 = 1.0 / (1.0 - d * d2);
        return new Affine(d5, -d * d5, 0.0, d * (d4 - d2 * d3) * d5, -d2 * d5, 1.0 + d * d2 * d5, 0.0, d2 * d3 + d2 * (d * d2 * d3 - d * d4) * d5, 0.0, 0.0, 1.0, 0.0);
    }

    @Override
    public Shear clone() {
        return new Shear(this.getX(), this.getY(), this.getPivotX(), this.getPivotY());
    }

    @Override
    public Point2D transform(double d, double d2) {
        double d3 = this.getX();
        double d4 = this.getY();
        return new Point2D(d + d3 * d2 - d3 * this.getPivotY(), d4 * d + d2 - d4 * this.getPivotX());
    }

    @Override
    public Point3D transform(double d, double d2, double d3) {
        double d4 = this.getX();
        double d5 = this.getY();
        return new Point3D(d + d4 * d2 - d4 * this.getPivotY(), d5 * d + d2 - d5 * this.getPivotX(), d3);
    }

    @Override
    void transform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getPivotX();
        double d4 = this.getPivotY();
        while (--n3 >= 0) {
            double d5 = dArray[n++];
            double d6 = dArray[n++];
            dArray2[n2++] = d5 + d * d6 - d * d4;
            dArray2[n2++] = d2 * d5 + d6 - d2 * d3;
        }
    }

    @Override
    void transform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getPivotX();
        double d4 = this.getPivotY();
        while (--n3 >= 0) {
            double d5 = dArray[n++];
            double d6 = dArray[n++];
            dArray2[n2++] = d5 + d * d6 - d * d4;
            dArray2[n2++] = d2 * d5 + d6 - d2 * d3;
            dArray2[n2++] = dArray[n++];
        }
    }

    @Override
    public Point2D deltaTransform(double d, double d2) {
        return new Point2D(d + this.getX() * d2, this.getY() * d + d2);
    }

    @Override
    public Point3D deltaTransform(double d, double d2, double d3) {
        return new Point3D(d + this.getX() * d2, this.getY() * d + d2, d3);
    }

    @Override
    public Point2D inverseTransform(double d, double d2) throws NonInvertibleTransformException {
        double d3 = this.getX();
        double d4 = this.getY();
        if (d4 == 0.0) {
            double d5 = -this.getX();
            return new Point2D(d + d5 * d2 - d5 * this.getPivotY(), d2);
        }
        if (d3 == 0.0) {
            double d6 = -this.getY();
            return new Point2D(d, d6 * d + d2 - d6 * this.getPivotX());
        }
        return super.inverseTransform(d, d2);
    }

    @Override
    public Point3D inverseTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        double d4 = this.getX();
        double d5 = this.getY();
        if (d5 == 0.0) {
            double d6 = -this.getX();
            return new Point3D(d + d6 * d2 - d6 * this.getPivotY(), d2, d3);
        }
        if (d4 == 0.0) {
            double d7 = -this.getY();
            return new Point3D(d, d7 * d + d2 - d7 * this.getPivotX(), d3);
        }
        return super.inverseTransform(d, d2, d3);
    }

    @Override
    void inverseTransform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        double d = this.getPivotX();
        double d2 = this.getPivotY();
        double d3 = this.getX();
        double d4 = this.getY();
        if (d4 == 0.0) {
            double d5 = -d3;
            while (--n3 >= 0) {
                double d6 = dArray[n++];
                double d7 = dArray[n++];
                dArray2[n2++] = d6 + d5 * d7 - d5 * d2;
                dArray2[n2++] = d7;
            }
            return;
        }
        if (d3 == 0.0) {
            double d8 = -d4;
            while (--n3 >= 0) {
                double d9 = dArray[n++];
                double d10 = dArray[n++];
                dArray2[n2++] = d9;
                dArray2[n2++] = d8 * d9 + d10 - d8 * d;
            }
            return;
        }
        super.inverseTransform2DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    @Override
    void inverseTransform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        double d = this.getPivotX();
        double d2 = this.getPivotY();
        double d3 = this.getX();
        double d4 = this.getY();
        if (d4 == 0.0) {
            double d5 = -d3;
            while (--n3 >= 0) {
                double d6 = dArray[n++];
                double d7 = dArray[n++];
                dArray2[n2++] = d6 + d5 * d7 - d5 * d2;
                dArray2[n2++] = d7;
                dArray2[n2++] = dArray[n++];
            }
            return;
        }
        if (d3 == 0.0) {
            double d8 = -d4;
            while (--n3 >= 0) {
                double d9 = dArray[n++];
                double d10 = dArray[n++];
                dArray2[n2++] = d9;
                dArray2[n2++] = d8 * d9 + d10 - d8 * d;
                dArray2[n2++] = dArray[n++];
            }
            return;
        }
        super.inverseTransform3DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    @Override
    public Point2D inverseDeltaTransform(double d, double d2) throws NonInvertibleTransformException {
        double d3 = this.getX();
        double d4 = this.getY();
        if (d4 == 0.0) {
            return new Point2D(d - this.getX() * d2, d2);
        }
        if (d3 == 0.0) {
            return new Point2D(d, -this.getY() * d + d2);
        }
        return super.inverseDeltaTransform(d, d2);
    }

    @Override
    public Point3D inverseDeltaTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        double d4 = this.getX();
        double d5 = this.getY();
        if (d5 == 0.0) {
            return new Point3D(d - this.getX() * d2, d2, d3);
        }
        if (d4 == 0.0) {
            return new Point3D(d, -this.getY() * d + d2, d3);
        }
        return super.inverseDeltaTransform(d, d2, d3);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Shear [");
        stringBuilder.append("x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        stringBuilder.append(", pivotX=").append(this.getPivotX());
        stringBuilder.append(", pivotY=").append(this.getPivotY());
        return stringBuilder.append("]").toString();
    }

    @Override
    void apply(Affine3D affine3D) {
        if (this.getPivotX() != 0.0 || this.getPivotY() != 0.0) {
            affine3D.translate(this.getPivotX(), this.getPivotY());
            affine3D.shear(this.getX(), this.getY());
            affine3D.translate(-this.getPivotX(), -this.getPivotY());
        } else {
            affine3D.shear(this.getX(), this.getY());
        }
    }

    @Override
    BaseTransform derive(BaseTransform baseTransform) {
        return baseTransform.deriveWithConcatenation(1.0, this.getY(), this.getX(), 1.0, this.getTx(), this.getTy());
    }

    @Override
    void validate() {
        this.getX();
        this.getPivotX();
        this.getY();
        this.getPivotY();
    }

    @Override
    void appendTo(Affine affine) {
        affine.appendShear(this.getX(), this.getY(), this.getPivotX(), this.getPivotY());
    }

    @Override
    void prependTo(Affine affine) {
        affine.prependShear(this.getX(), this.getY(), this.getPivotX(), this.getPivotY());
    }
}

