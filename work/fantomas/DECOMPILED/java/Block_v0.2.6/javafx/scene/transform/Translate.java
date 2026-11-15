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
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

public class Translate
extends Transform {
    private DoubleProperty x;
    private DoubleProperty y;
    private DoubleProperty z;

    public Translate() {
    }

    public Translate(double d, double d2) {
        this.setX(d);
        this.setY(d2);
    }

    public Translate(double d, double d2, double d3) {
        this(d, d2);
        this.setZ(d3);
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
                    Translate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Translate.this;
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
                    Translate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Translate.this;
                }

                @Override
                public String getName() {
                    return "y";
                }
            };
        }
        return this.y;
    }

    public final void setZ(double d) {
        this.zProperty().set(d);
    }

    public final double getZ() {
        return this.z == null ? 0.0 : this.z.get();
    }

    public final DoubleProperty zProperty() {
        if (this.z == null) {
            this.z = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Translate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Translate.this;
                }

                @Override
                public String getName() {
                    return "z";
                }
            };
        }
        return this.z;
    }

    @Override
    public double getTx() {
        return this.getX();
    }

    @Override
    public double getTy() {
        return this.getY();
    }

    @Override
    public double getTz() {
        return this.getZ();
    }

    @Override
    boolean computeIs2D() {
        return this.getZ() == 0.0;
    }

    @Override
    boolean computeIsIdentity() {
        return this.getX() == 0.0 && this.getY() == 0.0 && this.getZ() == 0.0;
    }

    @Override
    void fill2DArray(double[] dArray) {
        dArray[0] = 1.0;
        dArray[1] = 0.0;
        dArray[2] = this.getX();
        dArray[3] = 0.0;
        dArray[4] = 1.0;
        dArray[5] = this.getY();
    }

    @Override
    void fill3DArray(double[] dArray) {
        dArray[0] = 1.0;
        dArray[1] = 0.0;
        dArray[2] = 0.0;
        dArray[3] = this.getX();
        dArray[4] = 0.0;
        dArray[5] = 1.0;
        dArray[6] = 0.0;
        dArray[7] = this.getY();
        dArray[8] = 0.0;
        dArray[9] = 0.0;
        dArray[10] = 1.0;
        dArray[11] = this.getZ();
    }

    @Override
    public Transform createConcatenation(Transform transform) {
        Transform transform2;
        if (transform instanceof Translate) {
            Translate translate = (Translate)transform;
            return new Translate(this.getX() + translate.getX(), this.getY() + translate.getY(), this.getZ() + translate.getZ());
        }
        if (transform instanceof Scale) {
            transform2 = (Scale)transform;
            double d = ((Scale)transform2).getX();
            double d2 = ((Scale)transform2).getY();
            double d3 = ((Scale)transform2).getZ();
            double d4 = this.getX();
            double d5 = this.getY();
            double d6 = this.getZ();
            if (!(d4 != 0.0 && d == 1.0 || d5 != 0.0 && d2 == 1.0 || d6 != 0.0 && d3 == 1.0)) {
                return new Scale(d, d2, d3, ((Scale)transform2).getPivotX() + (d == 1.0 ? 0.0 : d4 / (1.0 - d)), ((Scale)transform2).getPivotY() + (d2 == 1.0 ? 0.0 : d5 / (1.0 - d2)), ((Scale)transform2).getPivotZ() + (d3 == 1.0 ? 0.0 : d6 / (1.0 - d3)));
            }
        }
        if (transform instanceof Affine) {
            transform2 = (Affine)transform.clone();
            ((Affine)transform2).prepend(this);
            return transform2;
        }
        double d = transform.getMxx();
        double d7 = transform.getMxy();
        double d8 = transform.getMxz();
        double d9 = transform.getTx();
        double d10 = transform.getMyx();
        double d11 = transform.getMyy();
        double d12 = transform.getMyz();
        double d13 = transform.getTy();
        double d14 = transform.getMzx();
        double d15 = transform.getMzy();
        double d16 = transform.getMzz();
        double d17 = transform.getTz();
        return new Affine(d, d7, d8, d9 + this.getX(), d10, d11, d12, d13 + this.getY(), d14, d15, d16, d17 + this.getZ());
    }

    @Override
    public Translate createInverse() {
        return new Translate(-this.getX(), -this.getY(), -this.getZ());
    }

    @Override
    public Translate clone() {
        return new Translate(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public Point2D transform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        return new Point2D(d + this.getX(), d2 + this.getY());
    }

    @Override
    public Point3D transform(double d, double d2, double d3) {
        return new Point3D(d + this.getX(), d2 + this.getY(), d3 + this.getZ());
    }

    @Override
    void transform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getX();
        double d2 = this.getY();
        while (--n3 >= 0) {
            double d3 = dArray[n++];
            double d4 = dArray[n++];
            dArray2[n2++] = d3 + d;
            dArray2[n2++] = d4 + d2;
        }
    }

    @Override
    void transform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        while (--n3 >= 0) {
            double d4 = dArray[n++];
            double d5 = dArray[n++];
            double d6 = dArray[n++];
            dArray2[n2++] = d4 + d;
            dArray2[n2++] = d5 + d2;
            dArray2[n2++] = d6 + d3;
        }
    }

    @Override
    public Point2D deltaTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        return new Point2D(d, d2);
    }

    @Override
    public Point2D deltaTransform(Point2D point2D) {
        if (point2D == null) {
            throw new NullPointerException();
        }
        this.ensureCanTransform2DPoint();
        return point2D;
    }

    @Override
    public Point3D deltaTransform(double d, double d2, double d3) {
        return new Point3D(d, d2, d3);
    }

    @Override
    public Point3D deltaTransform(Point3D point3D) {
        if (point3D == null) {
            throw new NullPointerException();
        }
        return point3D;
    }

    @Override
    public Point2D inverseTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        return new Point2D(d - this.getX(), d2 - this.getY());
    }

    @Override
    public Point3D inverseTransform(double d, double d2, double d3) {
        return new Point3D(d - this.getX(), d2 - this.getY(), d3 - this.getZ());
    }

    @Override
    void inverseTransform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getX();
        double d2 = this.getY();
        while (--n3 >= 0) {
            dArray2[n2++] = dArray[n++] - d;
            dArray2[n2++] = dArray[n++] - d2;
        }
    }

    @Override
    void inverseTransform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        while (--n3 >= 0) {
            dArray2[n2++] = dArray[n++] - d;
            dArray2[n2++] = dArray[n++] - d2;
            dArray2[n2++] = dArray[n++] - d3;
        }
    }

    @Override
    public Point2D inverseDeltaTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        return new Point2D(d, d2);
    }

    @Override
    public Point2D inverseDeltaTransform(Point2D point2D) {
        if (point2D == null) {
            throw new NullPointerException();
        }
        this.ensureCanTransform2DPoint();
        return point2D;
    }

    @Override
    public Point3D inverseDeltaTransform(double d, double d2, double d3) {
        return new Point3D(d, d2, d3);
    }

    @Override
    public Point3D inverseDeltaTransform(Point3D point3D) {
        if (point3D == null) {
            throw new NullPointerException();
        }
        return point3D;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Translate [");
        stringBuilder.append("x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        stringBuilder.append(", z=").append(this.getZ());
        return stringBuilder.append("]").toString();
    }

    @Override
    void apply(Affine3D affine3D) {
        affine3D.translate(this.getX(), this.getY(), this.getZ());
    }

    @Override
    BaseTransform derive(BaseTransform baseTransform) {
        return baseTransform.deriveWithTranslation(this.getX(), this.getY(), this.getZ());
    }

    @Override
    void validate() {
        this.getX();
        this.getY();
        this.getZ();
    }

    @Override
    void appendTo(Affine affine) {
        affine.appendTranslation(this.getTx(), this.getTy(), this.getTz());
    }

    @Override
    void prependTo(Affine affine) {
        affine.prependTranslation(this.getTx(), this.getTy(), this.getTz());
    }
}

