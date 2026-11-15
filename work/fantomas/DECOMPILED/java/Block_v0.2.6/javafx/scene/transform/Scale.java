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
import javafx.scene.transform.Translate;

public class Scale
extends Transform {
    private DoubleProperty x;
    private DoubleProperty y;
    private DoubleProperty z;
    private DoubleProperty pivotX;
    private DoubleProperty pivotY;
    private DoubleProperty pivotZ;

    public Scale() {
    }

    public Scale(double d, double d2) {
        this.setX(d);
        this.setY(d2);
    }

    public Scale(double d, double d2, double d3, double d4) {
        this(d, d2);
        this.setPivotX(d3);
        this.setPivotY(d4);
    }

    public Scale(double d, double d2, double d3) {
        this(d, d2);
        this.setZ(d3);
    }

    public Scale(double d, double d2, double d3, double d4, double d5, double d6) {
        this(d, d2, d4, d5);
        this.setZ(d3);
        this.setPivotZ(d6);
    }

    public final void setX(double d) {
        this.xProperty().set(d);
    }

    public final double getX() {
        return this.x == null ? 1.0 : this.x.get();
    }

    public final DoubleProperty xProperty() {
        if (this.x == null) {
            this.x = new DoublePropertyBase(1.0){

                @Override
                public void invalidated() {
                    Scale.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Scale.this;
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
        return this.y == null ? 1.0 : this.y.get();
    }

    public final DoubleProperty yProperty() {
        if (this.y == null) {
            this.y = new DoublePropertyBase(1.0){

                @Override
                public void invalidated() {
                    Scale.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Scale.this;
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
        return this.z == null ? 1.0 : this.z.get();
    }

    public final DoubleProperty zProperty() {
        if (this.z == null) {
            this.z = new DoublePropertyBase(1.0){

                @Override
                public void invalidated() {
                    Scale.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Scale.this;
                }

                @Override
                public String getName() {
                    return "z";
                }
            };
        }
        return this.z;
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
                    Scale.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Scale.this;
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
                    Scale.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Scale.this;
                }

                @Override
                public String getName() {
                    return "pivotY";
                }
            };
        }
        return this.pivotY;
    }

    public final void setPivotZ(double d) {
        this.pivotZProperty().set(d);
    }

    public final double getPivotZ() {
        return this.pivotZ == null ? 0.0 : this.pivotZ.get();
    }

    public final DoubleProperty pivotZProperty() {
        if (this.pivotZ == null) {
            this.pivotZ = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Scale.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Scale.this;
                }

                @Override
                public String getName() {
                    return "pivotZ";
                }
            };
        }
        return this.pivotZ;
    }

    @Override
    public double getMxx() {
        return this.getX();
    }

    @Override
    public double getMyy() {
        return this.getY();
    }

    @Override
    public double getMzz() {
        return this.getZ();
    }

    @Override
    public double getTx() {
        return (1.0 - this.getX()) * this.getPivotX();
    }

    @Override
    public double getTy() {
        return (1.0 - this.getY()) * this.getPivotY();
    }

    @Override
    public double getTz() {
        return (1.0 - this.getZ()) * this.getPivotZ();
    }

    @Override
    boolean computeIs2D() {
        return this.getZ() == 1.0;
    }

    @Override
    boolean computeIsIdentity() {
        return this.getX() == 1.0 && this.getY() == 1.0 && this.getZ() == 1.0;
    }

    @Override
    void fill2DArray(double[] dArray) {
        double d = this.getX();
        double d2 = this.getY();
        dArray[0] = d;
        dArray[1] = 0.0;
        dArray[2] = (1.0 - d) * this.getPivotX();
        dArray[3] = 0.0;
        dArray[4] = d2;
        dArray[5] = (1.0 - d2) * this.getPivotY();
    }

    @Override
    void fill3DArray(double[] dArray) {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        dArray[0] = d;
        dArray[1] = 0.0;
        dArray[2] = 0.0;
        dArray[3] = (1.0 - d) * this.getPivotX();
        dArray[4] = 0.0;
        dArray[5] = d2;
        dArray[6] = 0.0;
        dArray[7] = (1.0 - d2) * this.getPivotY();
        dArray[8] = 0.0;
        dArray[9] = 0.0;
        dArray[10] = d3;
        dArray[11] = (1.0 - d3) * this.getPivotZ();
    }

    @Override
    public Transform createConcatenation(Transform transform) {
        Transform transform2;
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        if (transform instanceof Scale && ((Scale)(transform2 = (Scale)transform)).getPivotX() == this.getPivotX() && ((Scale)transform2).getPivotY() == this.getPivotY() && ((Scale)transform2).getPivotZ() == this.getPivotZ()) {
            return new Scale(d * ((Scale)transform2).getX(), d2 * ((Scale)transform2).getY(), d3 * ((Scale)transform2).getZ(), this.getPivotX(), this.getPivotY(), this.getPivotZ());
        }
        if (transform instanceof Translate) {
            transform2 = (Translate)transform;
            double d4 = ((Translate)transform2).getX();
            double d5 = ((Translate)transform2).getY();
            double d6 = ((Translate)transform2).getZ();
            if ((d4 == 0.0 || d != 1.0 && d != 0.0) && (d5 == 0.0 || d2 != 1.0 && d2 != 0.0) && (d6 == 0.0 || d3 != 1.0 && d3 != 0.0)) {
                return new Scale(d, d2, d3, (d != 1.0 ? d * d4 / (1.0 - d) : 0.0) + this.getPivotX(), (d2 != 1.0 ? d2 * d5 / (1.0 - d2) : 0.0) + this.getPivotY(), (d3 != 1.0 ? d3 * d6 / (1.0 - d3) : 0.0) + this.getPivotZ());
            }
        }
        if (transform instanceof Affine) {
            transform2 = (Affine)transform.clone();
            ((Affine)transform2).prepend(this);
            return transform2;
        }
        double d7 = transform.getMxx();
        double d8 = transform.getMxy();
        double d9 = transform.getMxz();
        double d10 = transform.getTx();
        double d11 = transform.getMyx();
        double d12 = transform.getMyy();
        double d13 = transform.getMyz();
        double d14 = transform.getTy();
        double d15 = transform.getMzx();
        double d16 = transform.getMzy();
        double d17 = transform.getMzz();
        double d18 = transform.getTz();
        return new Affine(d * d7, d * d8, d * d9, d * d10 + (1.0 - d) * this.getPivotX(), d2 * d11, d2 * d12, d2 * d13, d2 * d14 + (1.0 - d2) * this.getPivotY(), d3 * d15, d3 * d16, d3 * d17, d3 * d18 + (1.0 - d3) * this.getPivotZ());
    }

    @Override
    public Scale createInverse() throws NonInvertibleTransformException {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        if (d == 0.0 || d2 == 0.0 || d3 == 0.0) {
            throw new NonInvertibleTransformException("Zero scale is not invertible");
        }
        return new Scale(1.0 / d, 1.0 / d2, 1.0 / d3, this.getPivotX(), this.getPivotY(), this.getPivotZ());
    }

    @Override
    public Scale clone() {
        return new Scale(this.getX(), this.getY(), this.getZ(), this.getPivotX(), this.getPivotY(), this.getPivotZ());
    }

    @Override
    public Point2D transform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        double d3 = this.getX();
        double d4 = this.getY();
        return new Point2D(d3 * d + (1.0 - d3) * this.getPivotX(), d4 * d2 + (1.0 - d4) * this.getPivotY());
    }

    @Override
    public Point3D transform(double d, double d2, double d3) {
        double d4 = this.getX();
        double d5 = this.getY();
        double d6 = this.getZ();
        return new Point3D(d4 * d + (1.0 - d4) * this.getPivotX(), d5 * d2 + (1.0 - d5) * this.getPivotY(), d6 * d3 + (1.0 - d6) * this.getPivotZ());
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
            dArray2[n2++] = d * d5 + (1.0 - d) * d3;
            dArray2[n2++] = d2 * d6 + (1.0 - d2) * d4;
        }
    }

    @Override
    void transform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        double d4 = this.getPivotX();
        double d5 = this.getPivotY();
        double d6 = this.getPivotZ();
        while (--n3 >= 0) {
            dArray2[n2++] = d * dArray[n++] + (1.0 - d) * d4;
            dArray2[n2++] = d2 * dArray[n++] + (1.0 - d2) * d5;
            dArray2[n2++] = d3 * dArray[n++] + (1.0 - d3) * d6;
        }
    }

    @Override
    public Point2D deltaTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        return new Point2D(this.getX() * d, this.getY() * d2);
    }

    @Override
    public Point3D deltaTransform(double d, double d2, double d3) {
        return new Point3D(this.getX() * d, this.getY() * d2, this.getZ() * d3);
    }

    @Override
    public Point2D inverseTransform(double d, double d2) throws NonInvertibleTransformException {
        this.ensureCanTransform2DPoint();
        double d3 = this.getX();
        double d4 = this.getY();
        if (d3 == 0.0 || d4 == 0.0) {
            throw new NonInvertibleTransformException("Zero scale is not invertible");
        }
        double d5 = 1.0 / d3;
        double d6 = 1.0 / d4;
        return new Point2D(d5 * d + (1.0 - d5) * this.getPivotX(), d6 * d2 + (1.0 - d6) * this.getPivotY());
    }

    @Override
    public Point3D inverseTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        double d4 = this.getX();
        double d5 = this.getY();
        double d6 = this.getZ();
        if (d4 == 0.0 || d5 == 0.0 || d6 == 0.0) {
            throw new NonInvertibleTransformException("Zero scale is not invertible");
        }
        double d7 = 1.0 / d4;
        double d8 = 1.0 / d5;
        double d9 = 1.0 / d6;
        return new Point3D(d7 * d + (1.0 - d7) * this.getPivotX(), d8 * d2 + (1.0 - d8) * this.getPivotY(), d9 * d3 + (1.0 - d9) * this.getPivotZ());
    }

    @Override
    void inverseTransform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        double d = this.getX();
        double d2 = this.getY();
        if (d == 0.0 || d2 == 0.0) {
            throw new NonInvertibleTransformException("Zero scale is not invertible");
        }
        double d3 = 1.0 / d;
        double d4 = 1.0 / d2;
        double d5 = this.getPivotX();
        double d6 = this.getPivotY();
        while (--n3 >= 0) {
            dArray2[n2++] = d3 * dArray[n++] + (1.0 - d3) * d5;
            dArray2[n2++] = d4 * dArray[n++] + (1.0 - d4) * d6;
        }
    }

    @Override
    void inverseTransform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        if (d == 0.0 || d2 == 0.0 || d3 == 0.0) {
            throw new NonInvertibleTransformException("Zero scale is not invertible");
        }
        double d4 = 1.0 / d;
        double d5 = 1.0 / d2;
        double d6 = 1.0 / d3;
        double d7 = this.getPivotX();
        double d8 = this.getPivotY();
        double d9 = this.getPivotZ();
        while (--n3 >= 0) {
            dArray2[n2++] = d4 * dArray[n++] + (1.0 - d4) * d7;
            dArray2[n2++] = d5 * dArray[n++] + (1.0 - d5) * d8;
            dArray2[n2++] = d6 * dArray[n++] + (1.0 - d6) * d9;
        }
    }

    @Override
    public Point2D inverseDeltaTransform(double d, double d2) throws NonInvertibleTransformException {
        this.ensureCanTransform2DPoint();
        double d3 = this.getX();
        double d4 = this.getY();
        if (d3 == 0.0 || d4 == 0.0) {
            throw new NonInvertibleTransformException("Zero scale is not invertible");
        }
        return new Point2D(1.0 / d3 * d, 1.0 / d4 * d2);
    }

    @Override
    public Point3D inverseDeltaTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        double d4 = this.getX();
        double d5 = this.getY();
        double d6 = this.getZ();
        if (d4 == 0.0 || d5 == 0.0 || d6 == 0.0) {
            throw new NonInvertibleTransformException("Zero scale is not invertible");
        }
        return new Point3D(1.0 / d4 * d, 1.0 / d5 * d2, 1.0 / d6 * d3);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Scale [");
        stringBuilder.append("x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        stringBuilder.append(", z=").append(this.getZ());
        stringBuilder.append(", pivotX=").append(this.getPivotX());
        stringBuilder.append(", pivotY=").append(this.getPivotY());
        stringBuilder.append(", pivotZ=").append(this.getPivotZ());
        return stringBuilder.append("]").toString();
    }

    @Override
    void apply(Affine3D affine3D) {
        if (this.getPivotX() != 0.0 || this.getPivotY() != 0.0 || this.getPivotZ() != 0.0) {
            affine3D.translate(this.getPivotX(), this.getPivotY(), this.getPivotZ());
            affine3D.scale(this.getX(), this.getY(), this.getZ());
            affine3D.translate(-this.getPivotX(), -this.getPivotY(), -this.getPivotZ());
        } else {
            affine3D.scale(this.getX(), this.getY(), this.getZ());
        }
    }

    @Override
    BaseTransform derive(BaseTransform baseTransform) {
        if (this.isIdentity()) {
            return baseTransform;
        }
        if (this.getPivotX() != 0.0 || this.getPivotY() != 0.0 || this.getPivotZ() != 0.0) {
            baseTransform = baseTransform.deriveWithTranslation(this.getPivotX(), this.getPivotY(), this.getPivotZ());
            baseTransform = baseTransform.deriveWithScale(this.getX(), this.getY(), this.getZ());
            return baseTransform.deriveWithTranslation(-this.getPivotX(), -this.getPivotY(), -this.getPivotZ());
        }
        return baseTransform.deriveWithScale(this.getX(), this.getY(), this.getZ());
    }

    @Override
    void validate() {
        this.getX();
        this.getPivotX();
        this.getY();
        this.getPivotY();
        this.getZ();
        this.getPivotZ();
    }

    @Override
    void appendTo(Affine affine) {
        affine.appendScale(this.getX(), this.getY(), this.getZ(), this.getPivotX(), this.getPivotY(), this.getPivotZ());
    }

    @Override
    void prependTo(Affine affine) {
        affine.prependScale(this.getX(), this.getY(), this.getZ(), this.getPivotX(), this.getPivotY(), this.getPivotZ());
    }
}

