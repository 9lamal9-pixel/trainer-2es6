/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.transform;

import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

public class Rotate
extends Transform {
    public static final Point3D X_AXIS = new Point3D(1.0, 0.0, 0.0);
    public static final Point3D Y_AXIS = new Point3D(0.0, 1.0, 0.0);
    public static final Point3D Z_AXIS = new Point3D(0.0, 0.0, 1.0);
    private MatrixCache cache;
    private MatrixCache inverseCache;
    private DoubleProperty angle;
    private DoubleProperty pivotX;
    private DoubleProperty pivotY;
    private DoubleProperty pivotZ;
    private ObjectProperty<Point3D> axis;

    public Rotate() {
    }

    public Rotate(double d) {
        this.setAngle(d);
    }

    public Rotate(double d, Point3D point3D) {
        this.setAngle(d);
        this.setAxis(point3D);
    }

    public Rotate(double d, double d2, double d3) {
        this.setAngle(d);
        this.setPivotX(d2);
        this.setPivotY(d3);
    }

    public Rotate(double d, double d2, double d3, double d4) {
        this(d, d2, d3);
        this.setPivotZ(d4);
    }

    public Rotate(double d, double d2, double d3, double d4, Point3D point3D) {
        this(d, d2, d3);
        this.setPivotZ(d4);
        this.setAxis(point3D);
    }

    public final void setAngle(double d) {
        this.angleProperty().set(d);
    }

    public final double getAngle() {
        return this.angle == null ? 0.0 : this.angle.get();
    }

    public final DoubleProperty angleProperty() {
        if (this.angle == null) {
            this.angle = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Rotate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Rotate.this;
                }

                @Override
                public String getName() {
                    return "angle";
                }
            };
        }
        return this.angle;
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
                    Rotate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Rotate.this;
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
                    Rotate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Rotate.this;
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
                    Rotate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Rotate.this;
                }

                @Override
                public String getName() {
                    return "pivotZ";
                }
            };
        }
        return this.pivotZ;
    }

    public final void setAxis(Point3D point3D) {
        this.axisProperty().set(point3D);
    }

    public final Point3D getAxis() {
        return this.axis == null ? Z_AXIS : (Point3D)this.axis.get();
    }

    public final ObjectProperty<Point3D> axisProperty() {
        if (this.axis == null) {
            this.axis = new ObjectPropertyBase<Point3D>(Z_AXIS){

                @Override
                public void invalidated() {
                    Rotate.this.transformChanged();
                }

                @Override
                public Object getBean() {
                    return Rotate.this;
                }

                @Override
                public String getName() {
                    return "axis";
                }
            };
        }
        return this.axis;
    }

    @Override
    public double getMxx() {
        this.updateCache();
        return this.cache.mxx;
    }

    @Override
    public double getMxy() {
        this.updateCache();
        return this.cache.mxy;
    }

    @Override
    public double getMxz() {
        this.updateCache();
        return this.cache.mxz;
    }

    @Override
    public double getTx() {
        this.updateCache();
        return this.cache.tx;
    }

    @Override
    public double getMyx() {
        this.updateCache();
        return this.cache.myx;
    }

    @Override
    public double getMyy() {
        this.updateCache();
        return this.cache.myy;
    }

    @Override
    public double getMyz() {
        this.updateCache();
        return this.cache.myz;
    }

    @Override
    public double getTy() {
        this.updateCache();
        return this.cache.ty;
    }

    @Override
    public double getMzx() {
        this.updateCache();
        return this.cache.mzx;
    }

    @Override
    public double getMzy() {
        this.updateCache();
        return this.cache.mzy;
    }

    @Override
    public double getMzz() {
        this.updateCache();
        return this.cache.mzz;
    }

    @Override
    public double getTz() {
        this.updateCache();
        return this.cache.tz;
    }

    @Override
    boolean computeIs2D() {
        Point3D point3D = this.getAxis();
        return point3D.getX() == 0.0 && point3D.getY() == 0.0 || this.getAngle() == 0.0;
    }

    @Override
    boolean computeIsIdentity() {
        if (this.getAngle() == 0.0) {
            return true;
        }
        Point3D point3D = this.getAxis();
        return point3D.getX() == 0.0 && point3D.getY() == 0.0 && point3D.getZ() == 0.0;
    }

    @Override
    void fill2DArray(double[] dArray) {
        this.updateCache();
        dArray[0] = this.cache.mxx;
        dArray[1] = this.cache.mxy;
        dArray[2] = this.cache.tx;
        dArray[3] = this.cache.myx;
        dArray[4] = this.cache.myy;
        dArray[5] = this.cache.ty;
    }

    @Override
    void fill3DArray(double[] dArray) {
        this.updateCache();
        dArray[0] = this.cache.mxx;
        dArray[1] = this.cache.mxy;
        dArray[2] = this.cache.mxz;
        dArray[3] = this.cache.tx;
        dArray[4] = this.cache.myx;
        dArray[5] = this.cache.myy;
        dArray[6] = this.cache.myz;
        dArray[7] = this.cache.ty;
        dArray[8] = this.cache.mzx;
        dArray[9] = this.cache.mzy;
        dArray[10] = this.cache.mzz;
        dArray[11] = this.cache.tz;
    }

    @Override
    public Transform createConcatenation(Transform transform) {
        Transform transform2;
        if (transform instanceof Rotate) {
            transform2 = (Rotate)transform;
            double d = this.getPivotX();
            double d2 = this.getPivotY();
            double d3 = this.getPivotZ();
            if ((((Rotate)transform2).getAxis() == this.getAxis() || ((Rotate)transform2).getAxis().normalize().equals(this.getAxis().normalize())) && d == ((Rotate)transform2).getPivotX() && d2 == ((Rotate)transform2).getPivotY() && d3 == ((Rotate)transform2).getPivotZ()) {
                return new Rotate(this.getAngle() + ((Rotate)transform2).getAngle(), d, d2, d3, this.getAxis());
            }
        }
        if (transform instanceof Affine) {
            transform2 = (Affine)transform.clone();
            ((Affine)transform2).prepend(this);
            return transform2;
        }
        return super.createConcatenation(transform);
    }

    @Override
    public Transform createInverse() throws NonInvertibleTransformException {
        return new Rotate(-this.getAngle(), this.getPivotX(), this.getPivotY(), this.getPivotZ(), this.getAxis());
    }

    @Override
    public Rotate clone() {
        return new Rotate(this.getAngle(), this.getPivotX(), this.getPivotY(), this.getPivotZ(), this.getAxis());
    }

    @Override
    public Point2D transform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        this.updateCache();
        return new Point2D(this.cache.mxx * d + this.cache.mxy * d2 + this.cache.tx, this.cache.myx * d + this.cache.myy * d2 + this.cache.ty);
    }

    @Override
    public Point3D transform(double d, double d2, double d3) {
        this.updateCache();
        return new Point3D(this.cache.mxx * d + this.cache.mxy * d2 + this.cache.mxz * d3 + this.cache.tx, this.cache.myx * d + this.cache.myy * d2 + this.cache.myz * d3 + this.cache.ty, this.cache.mzx * d + this.cache.mzy * d2 + this.cache.mzz * d3 + this.cache.tz);
    }

    @Override
    void transform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        this.updateCache();
        while (--n3 >= 0) {
            double d = dArray[n++];
            double d2 = dArray[n++];
            dArray2[n2++] = this.cache.mxx * d + this.cache.mxy * d2 + this.cache.tx;
            dArray2[n2++] = this.cache.myx * d + this.cache.myy * d2 + this.cache.ty;
        }
    }

    @Override
    void transform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        this.updateCache();
        while (--n3 >= 0) {
            double d = dArray[n++];
            double d2 = dArray[n++];
            double d3 = dArray[n++];
            dArray2[n2++] = this.cache.mxx * d + this.cache.mxy * d2 + this.cache.mxz * d3 + this.cache.tx;
            dArray2[n2++] = this.cache.myx * d + this.cache.myy * d2 + this.cache.myz * d3 + this.cache.ty;
            dArray2[n2++] = this.cache.mzx * d + this.cache.mzy * d2 + this.cache.mzz * d3 + this.cache.tz;
        }
    }

    @Override
    public Point2D deltaTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        this.updateCache();
        return new Point2D(this.cache.mxx * d + this.cache.mxy * d2, this.cache.myx * d + this.cache.myy * d2);
    }

    @Override
    public Point3D deltaTransform(double d, double d2, double d3) {
        this.updateCache();
        return new Point3D(this.cache.mxx * d + this.cache.mxy * d2 + this.cache.mxz * d3, this.cache.myx * d + this.cache.myy * d2 + this.cache.myz * d3, this.cache.mzx * d + this.cache.mzy * d2 + this.cache.mzz * d3);
    }

    @Override
    public Point2D inverseTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        this.updateInverseCache();
        return new Point2D(this.inverseCache.mxx * d + this.inverseCache.mxy * d2 + this.inverseCache.tx, this.inverseCache.myx * d + this.inverseCache.myy * d2 + this.inverseCache.ty);
    }

    @Override
    public Point3D inverseTransform(double d, double d2, double d3) {
        this.updateInverseCache();
        return new Point3D(this.inverseCache.mxx * d + this.inverseCache.mxy * d2 + this.inverseCache.mxz * d3 + this.inverseCache.tx, this.inverseCache.myx * d + this.inverseCache.myy * d2 + this.inverseCache.myz * d3 + this.inverseCache.ty, this.inverseCache.mzx * d + this.inverseCache.mzy * d2 + this.inverseCache.mzz * d3 + this.inverseCache.tz);
    }

    @Override
    void inverseTransform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        this.updateInverseCache();
        while (--n3 >= 0) {
            double d = dArray[n++];
            double d2 = dArray[n++];
            dArray2[n2++] = this.inverseCache.mxx * d + this.inverseCache.mxy * d2 + this.inverseCache.tx;
            dArray2[n2++] = this.inverseCache.myx * d + this.inverseCache.myy * d2 + this.inverseCache.ty;
        }
    }

    @Override
    void inverseTransform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        this.updateInverseCache();
        while (--n3 >= 0) {
            double d = dArray[n++];
            double d2 = dArray[n++];
            double d3 = dArray[n++];
            dArray2[n2++] = this.inverseCache.mxx * d + this.inverseCache.mxy * d2 + this.inverseCache.mxz * d3 + this.inverseCache.tx;
            dArray2[n2++] = this.inverseCache.myx * d + this.inverseCache.myy * d2 + this.inverseCache.myz * d3 + this.inverseCache.ty;
            dArray2[n2++] = this.inverseCache.mzx * d + this.inverseCache.mzy * d2 + this.inverseCache.mzz * d3 + this.inverseCache.tz;
        }
    }

    @Override
    public Point2D inverseDeltaTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        this.updateInverseCache();
        return new Point2D(this.inverseCache.mxx * d + this.inverseCache.mxy * d2, this.inverseCache.myx * d + this.inverseCache.myy * d2);
    }

    @Override
    public Point3D inverseDeltaTransform(double d, double d2, double d3) {
        this.updateInverseCache();
        return new Point3D(this.inverseCache.mxx * d + this.inverseCache.mxy * d2 + this.inverseCache.mxz * d3, this.inverseCache.myx * d + this.inverseCache.myy * d2 + this.inverseCache.myz * d3, this.inverseCache.mzx * d + this.inverseCache.mzy * d2 + this.inverseCache.mzz * d3);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Rotate [");
        stringBuilder.append("angle=").append(this.getAngle());
        stringBuilder.append(", pivotX=").append(this.getPivotX());
        stringBuilder.append(", pivotY=").append(this.getPivotY());
        stringBuilder.append(", pivotZ=").append(this.getPivotZ());
        stringBuilder.append(", axis=").append(this.getAxis());
        return stringBuilder.append("]").toString();
    }

    @Override
    void apply(Affine3D affine3D) {
        double d = this.getPivotX();
        double d2 = this.getPivotY();
        double d3 = this.getPivotZ();
        double d4 = this.getAngle();
        if (d != 0.0 || d2 != 0.0 || d3 != 0.0) {
            affine3D.translate(d, d2, d3);
            affine3D.rotate(Math.toRadians(d4), this.getAxis().getX(), this.getAxis().getY(), this.getAxis().getZ());
            affine3D.translate(-d, -d2, -d3);
        } else {
            affine3D.rotate(Math.toRadians(d4), this.getAxis().getX(), this.getAxis().getY(), this.getAxis().getZ());
        }
    }

    @Override
    BaseTransform derive(BaseTransform baseTransform) {
        if (this.isIdentity()) {
            return baseTransform;
        }
        double d = this.getPivotX();
        double d2 = this.getPivotY();
        double d3 = this.getPivotZ();
        double d4 = this.getAngle();
        if (d != 0.0 || d2 != 0.0 || d3 != 0.0) {
            baseTransform = baseTransform.deriveWithTranslation(d, d2, d3);
            baseTransform = baseTransform.deriveWithRotation(Math.toRadians(d4), this.getAxis().getX(), this.getAxis().getY(), this.getAxis().getZ());
            return baseTransform.deriveWithTranslation(-d, -d2, -d3);
        }
        return baseTransform.deriveWithRotation(Math.toRadians(d4), this.getAxis().getX(), this.getAxis().getY(), this.getAxis().getZ());
    }

    @Override
    void validate() {
        this.getAxis();
        this.getAngle();
        this.getPivotX();
        this.getPivotY();
        this.getPivotZ();
    }

    @Override
    protected void transformChanged() {
        if (this.cache != null) {
            this.cache.invalidate();
        }
        super.transformChanged();
    }

    @Override
    void appendTo(Affine affine) {
        affine.appendRotation(this.getAngle(), this.getPivotX(), this.getPivotY(), this.getPivotZ(), this.getAxis());
    }

    @Override
    void prependTo(Affine affine) {
        affine.prependRotation(this.getAngle(), this.getPivotX(), this.getPivotY(), this.getPivotZ(), this.getAxis());
    }

    private void updateCache() {
        if (this.cache == null) {
            this.cache = new MatrixCache();
        }
        if (!this.cache.valid) {
            this.cache.update(this.getAngle(), this.getAxis(), this.getPivotX(), this.getPivotY(), this.getPivotZ());
        }
    }

    private void updateInverseCache() {
        if (this.inverseCache == null) {
            this.inverseCache = new MatrixCache();
        }
        if (!this.inverseCache.valid) {
            this.inverseCache.update(-this.getAngle(), this.getAxis(), this.getPivotX(), this.getPivotY(), this.getPivotZ());
        }
    }

    private static class MatrixCache {
        boolean valid = false;
        boolean is3D = false;
        double mxx;
        double mxy;
        double mxz;
        double tx;
        double myx;
        double myy;
        double myz;
        double ty;
        double mzx;
        double mzy;
        double mzz = 1.0;
        double tz;

        public void update(double d, Point3D point3D, double d2, double d3, double d4) {
            double d5;
            double d6;
            double d7;
            double d8 = Math.toRadians(d);
            double d9 = Math.sin(d8);
            double d10 = Math.cos(d8);
            if (point3D == Z_AXIS || point3D.getX() == 0.0 && point3D.getY() == 0.0 && point3D.getZ() > 0.0) {
                this.mxx = d10;
                this.mxy = -d9;
                this.tx = d2 * (1.0 - d10) + d3 * d9;
                this.myx = d9;
                this.myy = d10;
                this.ty = d3 * (1.0 - d10) - d2 * d9;
                if (this.is3D) {
                    this.mxz = 0.0;
                    this.myz = 0.0;
                    this.mzx = 0.0;
                    this.mzy = 0.0;
                    this.mzz = 1.0;
                    this.tz = 0.0;
                    this.is3D = false;
                }
                this.valid = true;
                return;
            }
            this.is3D = true;
            if (point3D == X_AXIS || point3D == Y_AXIS || point3D == Z_AXIS) {
                d7 = point3D.getX();
                d6 = point3D.getY();
                d5 = point3D.getZ();
            } else {
                double d11 = Math.sqrt(point3D.getX() * point3D.getX() + point3D.getY() * point3D.getY() + point3D.getZ() * point3D.getZ());
                if (d11 == 0.0) {
                    this.mxx = 1.0;
                    this.mxy = 0.0;
                    this.mxz = 0.0;
                    this.tx = 0.0;
                    this.myx = 0.0;
                    this.myy = 1.0;
                    this.myz = 0.0;
                    this.ty = 0.0;
                    this.mzx = 0.0;
                    this.mzy = 0.0;
                    this.mzz = 1.0;
                    this.tz = 0.0;
                    this.valid = true;
                    return;
                }
                d7 = point3D.getX() / d11;
                d6 = point3D.getY() / d11;
                d5 = point3D.getZ() / d11;
            }
            this.mxx = d10 + d7 * d7 * (1.0 - d10);
            this.mxy = d7 * d6 * (1.0 - d10) - d5 * d9;
            this.mxz = d7 * d5 * (1.0 - d10) + d6 * d9;
            this.tx = d2 * (1.0 - this.mxx) - d3 * this.mxy - d4 * this.mxz;
            this.myx = d6 * d7 * (1.0 - d10) + d5 * d9;
            this.myy = d10 + d6 * d6 * (1.0 - d10);
            this.myz = d6 * d5 * (1.0 - d10) - d7 * d9;
            this.ty = d3 * (1.0 - this.myy) - d2 * this.myx - d4 * this.myz;
            this.mzx = d5 * d7 * (1.0 - d10) - d6 * d9;
            this.mzy = d5 * d6 * (1.0 - d10) + d7 * d9;
            this.mzz = d10 + d5 * d5 * (1.0 - d10);
            this.tz = d4 * (1.0 - this.mzz) - d2 * this.mzx - d3 * this.mzy;
            this.valid = true;
        }

        public void invalidate() {
            this.valid = false;
        }
    }
}

