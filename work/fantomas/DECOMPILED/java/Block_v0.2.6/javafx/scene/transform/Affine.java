/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.transform;

import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.MatrixType;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

public class Affine
extends Transform {
    AffineAtomicChange atomicChange = new AffineAtomicChange();
    private static final int APPLY_IDENTITY = 0;
    private static final int APPLY_TRANSLATE = 1;
    private static final int APPLY_SCALE = 2;
    private static final int APPLY_SHEAR = 4;
    private static final int APPLY_NON_3D = 0;
    private static final int APPLY_3D_COMPLEX = 4;
    private transient int state2d;
    private transient int state3d;
    private double xx;
    private double xy;
    private double xz;
    private double yx;
    private double yy;
    private double yz;
    private double zx;
    private double zy;
    private double zz;
    private double xt;
    private double yt;
    private double zt;
    private AffineElementProperty mxx;
    private AffineElementProperty mxy;
    private AffineElementProperty mxz;
    private AffineElementProperty tx;
    private AffineElementProperty myx;
    private AffineElementProperty myy;
    private AffineElementProperty myz;
    private AffineElementProperty ty;
    private AffineElementProperty mzx;
    private AffineElementProperty mzy;
    private AffineElementProperty mzz;
    private AffineElementProperty tz;
    private static final int[] rot90conversion = new int[]{4, 5, 4, 5, 2, 3, 6, 7};

    public Affine() {
        this.zz = 1.0;
        this.yy = 1.0;
        this.xx = 1.0;
    }

    public Affine(Transform transform) {
        this(transform.getMxx(), transform.getMxy(), transform.getMxz(), transform.getTx(), transform.getMyx(), transform.getMyy(), transform.getMyz(), transform.getTy(), transform.getMzx(), transform.getMzy(), transform.getMzz(), transform.getTz());
    }

    public Affine(double d, double d2, double d3, double d4, double d5, double d6) {
        this.xx = d;
        this.xy = d2;
        this.xt = d3;
        this.yx = d4;
        this.yy = d5;
        this.yt = d6;
        this.zz = 1.0;
        this.updateState2D();
    }

    public Affine(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        this.xx = d;
        this.xy = d2;
        this.xz = d3;
        this.xt = d4;
        this.yx = d5;
        this.yy = d6;
        this.yz = d7;
        this.yt = d8;
        this.zx = d9;
        this.zy = d10;
        this.zz = d11;
        this.zt = d12;
        this.updateState();
    }

    public Affine(double[] dArray, MatrixType matrixType, int n) {
        if (dArray.length < n + matrixType.elements()) {
            throw new IndexOutOfBoundsException("The array is too short.");
        }
        switch (matrixType) {
            default: {
                Affine.stateError();
            }
            case MT_2D_3x3: {
                if (dArray[n + 6] != 0.0 || dArray[n + 7] != 0.0 || dArray[n + 8] != 1.0) {
                    throw new IllegalArgumentException("The matrix is not affine");
                }
            }
            case MT_2D_2x3: {
                this.xx = dArray[n++];
                this.xy = dArray[n++];
                this.xt = dArray[n++];
                this.yx = dArray[n++];
                this.yy = dArray[n++];
                this.yt = dArray[n];
                this.zz = 1.0;
                this.updateState2D();
                return;
            }
            case MT_3D_4x4: {
                if (dArray[n + 12] == 0.0 && dArray[n + 13] == 0.0 && dArray[n + 14] == 0.0 && dArray[n + 15] == 1.0) break;
                throw new IllegalArgumentException("The matrix is not affine");
            }
            case MT_3D_3x4: 
        }
        this.xx = dArray[n++];
        this.xy = dArray[n++];
        this.xz = dArray[n++];
        this.xt = dArray[n++];
        this.yx = dArray[n++];
        this.yy = dArray[n++];
        this.yz = dArray[n++];
        this.yt = dArray[n++];
        this.zx = dArray[n++];
        this.zy = dArray[n++];
        this.zz = dArray[n++];
        this.zt = dArray[n];
        this.updateState();
    }

    public final void setMxx(double d) {
        if (this.mxx == null) {
            if (this.xx != d) {
                this.xx = d;
                this.postProcessChange();
            }
        } else {
            this.mxxProperty().set(d);
        }
    }

    @Override
    public final double getMxx() {
        return this.mxx == null ? this.xx : this.mxx.get();
    }

    public final DoubleProperty mxxProperty() {
        if (this.mxx == null) {
            this.mxx = new AffineElementProperty(this.xx){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "mxx";
                }
            };
        }
        return this.mxx;
    }

    public final void setMxy(double d) {
        if (this.mxy == null) {
            if (this.xy != d) {
                this.xy = d;
                this.postProcessChange();
            }
        } else {
            this.mxyProperty().set(d);
        }
    }

    @Override
    public final double getMxy() {
        return this.mxy == null ? this.xy : this.mxy.get();
    }

    public final DoubleProperty mxyProperty() {
        if (this.mxy == null) {
            this.mxy = new AffineElementProperty(this.xy){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "mxy";
                }
            };
        }
        return this.mxy;
    }

    public final void setMxz(double d) {
        if (this.mxz == null) {
            if (this.xz != d) {
                this.xz = d;
                this.postProcessChange();
            }
        } else {
            this.mxzProperty().set(d);
        }
    }

    @Override
    public final double getMxz() {
        return this.mxz == null ? this.xz : this.mxz.get();
    }

    public final DoubleProperty mxzProperty() {
        if (this.mxz == null) {
            this.mxz = new AffineElementProperty(this.xz){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "mxz";
                }
            };
        }
        return this.mxz;
    }

    public final void setTx(double d) {
        if (this.tx == null) {
            if (this.xt != d) {
                this.xt = d;
                this.postProcessChange();
            }
        } else {
            this.txProperty().set(d);
        }
    }

    @Override
    public final double getTx() {
        return this.tx == null ? this.xt : this.tx.get();
    }

    public final DoubleProperty txProperty() {
        if (this.tx == null) {
            this.tx = new AffineElementProperty(this.xt){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "tx";
                }
            };
        }
        return this.tx;
    }

    public final void setMyx(double d) {
        if (this.myx == null) {
            if (this.yx != d) {
                this.yx = d;
                this.postProcessChange();
            }
        } else {
            this.myxProperty().set(d);
        }
    }

    @Override
    public final double getMyx() {
        return this.myx == null ? this.yx : this.myx.get();
    }

    public final DoubleProperty myxProperty() {
        if (this.myx == null) {
            this.myx = new AffineElementProperty(this.yx){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "myx";
                }
            };
        }
        return this.myx;
    }

    public final void setMyy(double d) {
        if (this.myy == null) {
            if (this.yy != d) {
                this.yy = d;
                this.postProcessChange();
            }
        } else {
            this.myyProperty().set(d);
        }
    }

    @Override
    public final double getMyy() {
        return this.myy == null ? this.yy : this.myy.get();
    }

    public final DoubleProperty myyProperty() {
        if (this.myy == null) {
            this.myy = new AffineElementProperty(this.yy){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "myy";
                }
            };
        }
        return this.myy;
    }

    public final void setMyz(double d) {
        if (this.myz == null) {
            if (this.yz != d) {
                this.yz = d;
                this.postProcessChange();
            }
        } else {
            this.myzProperty().set(d);
        }
    }

    @Override
    public final double getMyz() {
        return this.myz == null ? this.yz : this.myz.get();
    }

    public final DoubleProperty myzProperty() {
        if (this.myz == null) {
            this.myz = new AffineElementProperty(this.yz){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "myz";
                }
            };
        }
        return this.myz;
    }

    public final void setTy(double d) {
        if (this.ty == null) {
            if (this.yt != d) {
                this.yt = d;
                this.postProcessChange();
            }
        } else {
            this.tyProperty().set(d);
        }
    }

    @Override
    public final double getTy() {
        return this.ty == null ? this.yt : this.ty.get();
    }

    public final DoubleProperty tyProperty() {
        if (this.ty == null) {
            this.ty = new AffineElementProperty(this.yt){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "ty";
                }
            };
        }
        return this.ty;
    }

    public final void setMzx(double d) {
        if (this.mzx == null) {
            if (this.zx != d) {
                this.zx = d;
                this.postProcessChange();
            }
        } else {
            this.mzxProperty().set(d);
        }
    }

    @Override
    public final double getMzx() {
        return this.mzx == null ? this.zx : this.mzx.get();
    }

    public final DoubleProperty mzxProperty() {
        if (this.mzx == null) {
            this.mzx = new AffineElementProperty(this.zx){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "mzx";
                }
            };
        }
        return this.mzx;
    }

    public final void setMzy(double d) {
        if (this.mzy == null) {
            if (this.zy != d) {
                this.zy = d;
                this.postProcessChange();
            }
        } else {
            this.mzyProperty().set(d);
        }
    }

    @Override
    public final double getMzy() {
        return this.mzy == null ? this.zy : this.mzy.get();
    }

    public final DoubleProperty mzyProperty() {
        if (this.mzy == null) {
            this.mzy = new AffineElementProperty(this.zy){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "mzy";
                }
            };
        }
        return this.mzy;
    }

    public final void setMzz(double d) {
        if (this.mzz == null) {
            if (this.zz != d) {
                this.zz = d;
                this.postProcessChange();
            }
        } else {
            this.mzzProperty().set(d);
        }
    }

    @Override
    public final double getMzz() {
        return this.mzz == null ? this.zz : this.mzz.get();
    }

    public final DoubleProperty mzzProperty() {
        if (this.mzz == null) {
            this.mzz = new AffineElementProperty(this.zz){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "mzz";
                }
            };
        }
        return this.mzz;
    }

    public final void setTz(double d) {
        if (this.tz == null) {
            if (this.zt != d) {
                this.zt = d;
                this.postProcessChange();
            }
        } else {
            this.tzProperty().set(d);
        }
    }

    @Override
    public final double getTz() {
        return this.tz == null ? this.zt : this.tz.get();
    }

    public final DoubleProperty tzProperty() {
        if (this.tz == null) {
            this.tz = new AffineElementProperty(this.zt){

                @Override
                public Object getBean() {
                    return Affine.this;
                }

                @Override
                public String getName() {
                    return "tz";
                }
            };
        }
        return this.tz;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void setElement(MatrixType matrixType, int n, int n2, double d) {
        if (n < 0 || n >= matrixType.rows() || n2 < 0 || n2 >= matrixType.columns()) {
            throw new IndexOutOfBoundsException("Index outside of affine matrix " + matrixType + ": [" + n + ", " + n2 + "]");
        }
        switch (matrixType) {
            default: {
                Affine.stateError();
            }
            case MT_2D_3x3: 
            case MT_2D_2x3: {
                if (!this.isType2D()) {
                    throw new IllegalArgumentException("Cannot access 2D matrix of a 3D transform");
                }
                switch (n) {
                    case 0: {
                        switch (n2) {
                            case 0: {
                                this.setMxx(d);
                                return;
                            }
                            case 1: {
                                this.setMxy(d);
                                return;
                            }
                            case 2: {
                                this.setTx(d);
                                return;
                            }
                        }
                    }
                    case 1: {
                        switch (n2) {
                            case 0: {
                                this.setMyx(d);
                                return;
                            }
                            case 1: {
                                this.setMyy(d);
                                return;
                            }
                            case 2: {
                                this.setTy(d);
                                return;
                            }
                        }
                    }
                    case 2: {
                        switch (n2) {
                            case 0: {
                                if (d != 0.0) throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
                                return;
                            }
                            case 1: {
                                if (d != 0.0) throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
                                return;
                            }
                            case 2: {
                                if (d != 1.0) throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
                                return;
                            }
                        }
                    }
                }
                throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
            }
            case MT_3D_4x4: 
            case MT_3D_3x4: {
                switch (n) {
                    case 0: {
                        switch (n2) {
                            case 0: {
                                this.setMxx(d);
                                return;
                            }
                            case 1: {
                                this.setMxy(d);
                                return;
                            }
                            case 2: {
                                this.setMxz(d);
                                return;
                            }
                            case 3: {
                                this.setTx(d);
                                return;
                            }
                        }
                    }
                    case 1: {
                        switch (n2) {
                            case 0: {
                                this.setMyx(d);
                                return;
                            }
                            case 1: {
                                this.setMyy(d);
                                return;
                            }
                            case 2: {
                                this.setMyz(d);
                                return;
                            }
                            case 3: {
                                this.setTy(d);
                                return;
                            }
                        }
                    }
                    case 2: {
                        switch (n2) {
                            case 0: {
                                this.setMzx(d);
                                return;
                            }
                            case 1: {
                                this.setMzy(d);
                                return;
                            }
                            case 2: {
                                this.setMzz(d);
                                return;
                            }
                            case 3: {
                                this.setTz(d);
                                return;
                            }
                        }
                    }
                    case 3: {
                        switch (n2) {
                            case 0: {
                                if (d != 0.0) throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
                                return;
                            }
                            case 1: {
                                if (d != 0.0) throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
                                return;
                            }
                            case 2: {
                                if (d != 0.0) throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
                                return;
                            }
                            case 3: {
                                if (d != 1.0) throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
                                return;
                            }
                        }
                    }
                }
            }
        }
        throw new IllegalArgumentException("Cannot set affine matrix " + matrixType + " element [" + n + ", " + n2 + "] to " + d);
    }

    private void postProcessChange() {
        if (!this.atomicChange.runs()) {
            this.updateState();
            this.transformChanged();
        }
    }

    @Override
    boolean computeIs2D() {
        return this.state3d == 0;
    }

    @Override
    boolean computeIsIdentity() {
        return this.state3d == 0 && this.state2d == 0;
    }

    @Override
    public double determinant() {
        if (this.state3d == 0) {
            return this.getDeterminant2D();
        }
        return this.getDeterminant3D();
    }

    private double getDeterminant2D() {
        switch (this.state2d) {
            default: {
                Affine.stateError();
            }
            case 6: 
            case 7: {
                return this.getMxx() * this.getMyy() - this.getMxy() * this.getMyx();
            }
            case 4: 
            case 5: {
                return -(this.getMxy() * this.getMyx());
            }
            case 2: 
            case 3: {
                return this.getMxx() * this.getMyy();
            }
            case 0: 
            case 1: 
        }
        return 1.0;
    }

    private double getDeterminant3D() {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 1: {
                return 1.0;
            }
            case 2: 
            case 3: {
                return this.getMxx() * this.getMyy() * this.getMzz();
            }
            case 4: 
        }
        double d = this.getMyx();
        double d2 = this.getMyy();
        double d3 = this.getMyz();
        double d4 = this.getMzx();
        double d5 = this.getMzy();
        double d6 = this.getMzz();
        return this.getMxx() * (d2 * d6 - d5 * d3) + this.getMxy() * (d3 * d4 - d6 * d) + this.getMxz() * (d * d5 - d4 * d2);
    }

    @Override
    public Transform createConcatenation(Transform transform) {
        Affine affine = this.clone();
        affine.append(transform);
        return affine;
    }

    @Override
    public Affine createInverse() throws NonInvertibleTransformException {
        Affine affine = this.clone();
        affine.invert();
        return affine;
    }

    @Override
    public Affine clone() {
        return new Affine(this);
    }

    public void setToTransform(Transform transform) {
        this.setToTransform(transform.getMxx(), transform.getMxy(), transform.getMxz(), transform.getTx(), transform.getMyx(), transform.getMyy(), transform.getMyz(), transform.getTy(), transform.getMzx(), transform.getMzy(), transform.getMzz(), transform.getTz());
    }

    public void setToTransform(double d, double d2, double d3, double d4, double d5, double d6) {
        this.setToTransform(d, d2, 0.0, d3, d4, d5, 0.0, d6, 0.0, 0.0, 1.0, 0.0);
    }

    public void setToTransform(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        this.atomicChange.start();
        this.setMxx(d);
        this.setMxy(d2);
        this.setMxz(d3);
        this.setTx(d4);
        this.setMyx(d5);
        this.setMyy(d6);
        this.setMyz(d7);
        this.setTy(d8);
        this.setMzx(d9);
        this.setMzy(d10);
        this.setMzz(d11);
        this.setTz(d12);
        this.updateState();
        this.atomicChange.end();
    }

    public void setToTransform(double[] dArray, MatrixType matrixType, int n) {
        if (dArray.length < n + matrixType.elements()) {
            throw new IndexOutOfBoundsException("The array is too short.");
        }
        switch (matrixType) {
            default: {
                Affine.stateError();
            }
            case MT_2D_3x3: {
                if (dArray[n + 6] != 0.0 || dArray[n + 7] != 0.0 || dArray[n + 8] != 1.0) {
                    throw new IllegalArgumentException("The matrix is not affine");
                }
            }
            case MT_2D_2x3: {
                this.setToTransform(dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++]);
                return;
            }
            case MT_3D_4x4: {
                if (dArray[n + 12] == 0.0 && dArray[n + 13] == 0.0 && dArray[n + 14] == 0.0 && dArray[n + 15] == 1.0) break;
                throw new IllegalArgumentException("The matrix is not affine");
            }
            case MT_3D_3x4: 
        }
        this.setToTransform(dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++]);
    }

    public void setToIdentity() {
        this.atomicChange.start();
        if (this.state3d != 0) {
            this.setMxx(1.0);
            this.setMxy(0.0);
            this.setMxz(0.0);
            this.setTx(0.0);
            this.setMyx(0.0);
            this.setMyy(1.0);
            this.setMyz(0.0);
            this.setTy(0.0);
            this.setMzx(0.0);
            this.setMzy(0.0);
            this.setMzz(1.0);
            this.setTz(0.0);
            this.state3d = 0;
            this.state2d = 0;
        } else if (this.state2d != 0) {
            this.setMxx(1.0);
            this.setMxy(0.0);
            this.setTx(0.0);
            this.setMyx(0.0);
            this.setMyy(1.0);
            this.setTy(0.0);
            this.state2d = 0;
        }
        this.atomicChange.end();
    }

    public void invert() throws NonInvertibleTransformException {
        this.atomicChange.start();
        if (this.state3d == 0) {
            this.invert2D();
            this.updateState2D();
        } else {
            this.invert3D();
            this.updateState();
        }
        this.atomicChange.end();
    }

    private void invert2D() throws NonInvertibleTransformException {
        switch (this.state2d) {
            default: {
                Affine.stateError();
            }
            case 7: {
                double d = this.getMxx();
                double d2 = this.getMxy();
                double d3 = this.getTx();
                double d4 = this.getMyx();
                double d5 = this.getMyy();
                double d6 = this.getTy();
                double d7 = this.getDeterminant2D();
                if (d7 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMxx(d5 / d7);
                this.setMyx(-d4 / d7);
                this.setMxy(-d2 / d7);
                this.setMyy(d / d7);
                this.setTx((d2 * d6 - d5 * d3) / d7);
                this.setTy((d4 * d3 - d * d6) / d7);
                return;
            }
            case 6: {
                double d = this.getMxx();
                double d8 = this.getMxy();
                double d9 = this.getMyx();
                double d10 = this.getMyy();
                double d11 = this.getDeterminant2D();
                if (d11 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMxx(d10 / d11);
                this.setMyx(-d9 / d11);
                this.setMxy(-d8 / d11);
                this.setMyy(d / d11);
                return;
            }
            case 5: {
                double d = this.getMxy();
                double d12 = this.getTx();
                double d13 = this.getMyx();
                double d14 = this.getTy();
                if (d == 0.0 || d13 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMyx(1.0 / d);
                this.setMxy(1.0 / d13);
                this.setTx(-d14 / d13);
                this.setTy(-d12 / d);
                return;
            }
            case 4: {
                double d = this.getMxy();
                double d15 = this.getMyx();
                if (d == 0.0 || d15 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMyx(1.0 / d);
                this.setMxy(1.0 / d15);
                return;
            }
            case 3: {
                double d = this.getMxx();
                double d16 = this.getTx();
                double d17 = this.getMyy();
                double d18 = this.getTy();
                if (d == 0.0 || d17 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMxx(1.0 / d);
                this.setMyy(1.0 / d17);
                this.setTx(-d16 / d);
                this.setTy(-d18 / d17);
                return;
            }
            case 2: {
                double d = this.getMxx();
                double d19 = this.getMyy();
                if (d == 0.0 || d19 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMxx(1.0 / d);
                this.setMyy(1.0 / d19);
                return;
            }
            case 1: {
                this.setTx(-this.getTx());
                this.setTy(-this.getTy());
                return;
            }
            case 0: 
        }
    }

    private void invert3D() throws NonInvertibleTransformException {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 1: {
                this.setTx(-this.getTx());
                this.setTy(-this.getTy());
                this.setTz(-this.getTz());
                return;
            }
            case 2: {
                double d = this.getMxx();
                double d2 = this.getMyy();
                double d3 = this.getMzz();
                if (d == 0.0 || d2 == 0.0 || d3 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMxx(1.0 / d);
                this.setMyy(1.0 / d2);
                this.setMzz(1.0 / d3);
                return;
            }
            case 3: {
                double d = this.getMxx();
                double d4 = this.getTx();
                double d5 = this.getMyy();
                double d6 = this.getTy();
                double d7 = this.getMzz();
                double d8 = this.getTz();
                if (d == 0.0 || d5 == 0.0 || d7 == 0.0) {
                    this.atomicChange.cancel();
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                this.setMxx(1.0 / d);
                this.setMyy(1.0 / d5);
                this.setMzz(1.0 / d7);
                this.setTx(-d4 / d);
                this.setTy(-d6 / d5);
                this.setTz(-d8 / d7);
                return;
            }
            case 4: 
        }
        double d = this.getMxx();
        double d9 = this.getMxy();
        double d10 = this.getMxz();
        double d11 = this.getTx();
        double d12 = this.getMyx();
        double d13 = this.getMyy();
        double d14 = this.getMyz();
        double d15 = this.getTy();
        double d16 = this.getMzy();
        double d17 = this.getMzx();
        double d18 = this.getMzz();
        double d19 = this.getTz();
        double d20 = d * (d13 * d18 - d16 * d14) + d9 * (d14 * d17 - d18 * d12) + d10 * (d12 * d16 - d17 * d13);
        if (d20 == 0.0) {
            this.atomicChange.cancel();
            throw new NonInvertibleTransformException("Determinant is 0");
        }
        double d21 = d13 * d18 - d14 * d16;
        double d22 = -d12 * d18 + d14 * d17;
        double d23 = d12 * d16 - d13 * d17;
        double d24 = -d9 * (d14 * d19 - d18 * d15) - d10 * (d15 * d16 - d19 * d13) - d11 * (d13 * d18 - d16 * d14);
        double d25 = -d9 * d18 + d10 * d16;
        double d26 = d * d18 - d10 * d17;
        double d27 = -d * d16 + d9 * d17;
        double d28 = d * (d14 * d19 - d18 * d15) + d10 * (d15 * d17 - d19 * d12) + d11 * (d12 * d18 - d17 * d14);
        double d29 = d9 * d14 - d10 * d13;
        double d30 = -d * d14 + d10 * d12;
        double d31 = d * d13 - d9 * d12;
        double d32 = -d * (d13 * d19 - d16 * d15) - d9 * (d15 * d17 - d19 * d12) - d11 * (d12 * d16 - d17 * d13);
        this.setMxx(d21 / d20);
        this.setMxy(d25 / d20);
        this.setMxz(d29 / d20);
        this.setTx(d24 / d20);
        this.setMyx(d22 / d20);
        this.setMyy(d26 / d20);
        this.setMyz(d30 / d20);
        this.setTy(d28 / d20);
        this.setMzx(d23 / d20);
        this.setMzy(d27 / d20);
        this.setMzz(d31 / d20);
        this.setTz(d32 / d20);
    }

    public void append(Transform transform) {
        transform.appendTo(this);
    }

    public void append(double d, double d2, double d3, double d4, double d5, double d6) {
        if (this.state3d == 0) {
            this.atomicChange.start();
            double d7 = this.getMxx();
            double d8 = this.getMxy();
            double d9 = this.getMyx();
            double d10 = this.getMyy();
            this.setMxx(d7 * d + d8 * d4);
            this.setMxy(d7 * d2 + d8 * d5);
            this.setTx(d7 * d3 + d8 * d6 + this.getTx());
            this.setMyx(d9 * d + d10 * d4);
            this.setMyy(d9 * d2 + d10 * d5);
            this.setTy(d9 * d3 + d10 * d6 + this.getTy());
            this.updateState();
            this.atomicChange.end();
        } else {
            this.append(d, d2, 0.0, d3, d4, d5, 0.0, d6, 0.0, 0.0, 1.0, 0.0);
        }
    }

    public void append(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        this.atomicChange.start();
        double d13 = this.getMxx();
        double d14 = this.getMxy();
        double d15 = this.getMxz();
        double d16 = this.getTx();
        double d17 = this.getMyx();
        double d18 = this.getMyy();
        double d19 = this.getMyz();
        double d20 = this.getTy();
        double d21 = this.getMzx();
        double d22 = this.getMzy();
        double d23 = this.getMzz();
        double d24 = this.getTz();
        this.setMxx(d13 * d + d14 * d5 + d15 * d9);
        this.setMxy(d13 * d2 + d14 * d6 + d15 * d10);
        this.setMxz(d13 * d3 + d14 * d7 + d15 * d11);
        this.setTx(d13 * d4 + d14 * d8 + d15 * d12 + d16);
        this.setMyx(d17 * d + d18 * d5 + d19 * d9);
        this.setMyy(d17 * d2 + d18 * d6 + d19 * d10);
        this.setMyz(d17 * d3 + d18 * d7 + d19 * d11);
        this.setTy(d17 * d4 + d18 * d8 + d19 * d12 + d20);
        this.setMzx(d21 * d + d22 * d5 + d23 * d9);
        this.setMzy(d21 * d2 + d22 * d6 + d23 * d10);
        this.setMzz(d21 * d3 + d22 * d7 + d23 * d11);
        this.setTz(d21 * d4 + d22 * d8 + d23 * d12 + d24);
        this.updateState();
        this.atomicChange.end();
    }

    public void append(double[] dArray, MatrixType matrixType, int n) {
        if (dArray.length < n + matrixType.elements()) {
            throw new IndexOutOfBoundsException("The array is too short.");
        }
        switch (matrixType) {
            default: {
                Affine.stateError();
            }
            case MT_2D_3x3: {
                if (dArray[n + 6] != 0.0 || dArray[n + 7] != 0.0 || dArray[n + 8] != 1.0) {
                    throw new IllegalArgumentException("The matrix is not affine");
                }
            }
            case MT_2D_2x3: {
                this.append(dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++]);
                return;
            }
            case MT_3D_4x4: {
                if (dArray[n + 12] == 0.0 && dArray[n + 13] == 0.0 && dArray[n + 14] == 0.0 && dArray[n + 15] == 1.0) break;
                throw new IllegalArgumentException("The matrix is not affine");
            }
            case MT_3D_3x4: 
        }
        this.append(dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++]);
    }

    @Override
    void appendTo(Affine affine) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    case 0: {
                        return;
                    }
                    case 1: {
                        affine.appendTranslation(this.getTx(), this.getTy());
                        return;
                    }
                    case 2: {
                        affine.appendScale(this.getMxx(), this.getMyy());
                        return;
                    }
                    case 3: {
                        affine.appendTranslation(this.getTx(), this.getTy());
                        affine.appendScale(this.getMxx(), this.getMyy());
                        return;
                    }
                }
                affine.append(this.getMxx(), this.getMxy(), this.getTx(), this.getMyx(), this.getMyy(), this.getTy());
                return;
            }
            case 1: {
                affine.appendTranslation(this.getTx(), this.getTy(), this.getTz());
                return;
            }
            case 2: {
                affine.appendScale(this.getMxx(), this.getMyy(), this.getMzz());
                return;
            }
            case 3: {
                affine.appendTranslation(this.getTx(), this.getTy(), this.getTz());
                affine.appendScale(this.getMxx(), this.getMyy(), this.getMzz());
                return;
            }
            case 4: 
        }
        affine.append(this.getMxx(), this.getMxy(), this.getMxz(), this.getTx(), this.getMyx(), this.getMyy(), this.getMyz(), this.getTy(), this.getMzx(), this.getMzy(), this.getMzz(), this.getTz());
    }

    public void prepend(Transform transform) {
        transform.prependTo(this);
    }

    public void prepend(double d, double d2, double d3, double d4, double d5, double d6) {
        if (this.state3d == 0) {
            this.atomicChange.start();
            double d7 = this.getMxx();
            double d8 = this.getMxy();
            double d9 = this.getTx();
            double d10 = this.getMyx();
            double d11 = this.getMyy();
            double d12 = this.getTy();
            this.setMxx(d * d7 + d2 * d10);
            this.setMxy(d * d8 + d2 * d11);
            this.setTx(d * d9 + d2 * d12 + d3);
            this.setMyx(d4 * d7 + d5 * d10);
            this.setMyy(d4 * d8 + d5 * d11);
            this.setTy(d4 * d9 + d5 * d12 + d6);
            this.updateState2D();
            this.atomicChange.end();
        } else {
            this.prepend(d, d2, 0.0, d3, d4, d5, 0.0, d6, 0.0, 0.0, 1.0, 0.0);
        }
    }

    public void prepend(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        this.atomicChange.start();
        double d13 = this.getMxx();
        double d14 = this.getMxy();
        double d15 = this.getMxz();
        double d16 = this.getTx();
        double d17 = this.getMyx();
        double d18 = this.getMyy();
        double d19 = this.getMyz();
        double d20 = this.getTy();
        double d21 = this.getMzx();
        double d22 = this.getMzy();
        double d23 = this.getMzz();
        double d24 = this.getTz();
        this.setMxx(d * d13 + d2 * d17 + d3 * d21);
        this.setMxy(d * d14 + d2 * d18 + d3 * d22);
        this.setMxz(d * d15 + d2 * d19 + d3 * d23);
        this.setTx(d * d16 + d2 * d20 + d3 * d24 + d4);
        this.setMyx(d5 * d13 + d6 * d17 + d7 * d21);
        this.setMyy(d5 * d14 + d6 * d18 + d7 * d22);
        this.setMyz(d5 * d15 + d6 * d19 + d7 * d23);
        this.setTy(d5 * d16 + d6 * d20 + d7 * d24 + d8);
        this.setMzx(d9 * d13 + d10 * d17 + d11 * d21);
        this.setMzy(d9 * d14 + d10 * d18 + d11 * d22);
        this.setMzz(d9 * d15 + d10 * d19 + d11 * d23);
        this.setTz(d9 * d16 + d10 * d20 + d11 * d24 + d12);
        this.updateState();
        this.atomicChange.end();
    }

    public void prepend(double[] dArray, MatrixType matrixType, int n) {
        if (dArray.length < n + matrixType.elements()) {
            throw new IndexOutOfBoundsException("The array is too short.");
        }
        switch (matrixType) {
            default: {
                Affine.stateError();
            }
            case MT_2D_3x3: {
                if (dArray[n + 6] != 0.0 || dArray[n + 7] != 0.0 || dArray[n + 8] != 1.0) {
                    throw new IllegalArgumentException("The matrix is not affine");
                }
            }
            case MT_2D_2x3: {
                this.prepend(dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++]);
                return;
            }
            case MT_3D_4x4: {
                if (dArray[n + 12] == 0.0 && dArray[n + 13] == 0.0 && dArray[n + 14] == 0.0 && dArray[n + 15] == 1.0) break;
                throw new IllegalArgumentException("The matrix is not affine");
            }
            case MT_3D_3x4: 
        }
        this.prepend(dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++], dArray[n++]);
    }

    @Override
    void prependTo(Affine affine) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    case 0: {
                        return;
                    }
                    case 1: {
                        affine.prependTranslation(this.getTx(), this.getTy());
                        return;
                    }
                    case 2: {
                        affine.prependScale(this.getMxx(), this.getMyy());
                        return;
                    }
                    case 3: {
                        affine.prependScale(this.getMxx(), this.getMyy());
                        affine.prependTranslation(this.getTx(), this.getTy());
                        return;
                    }
                }
                affine.prepend(this.getMxx(), this.getMxy(), this.getTx(), this.getMyx(), this.getMyy(), this.getTy());
                return;
            }
            case 1: {
                affine.prependTranslation(this.getTx(), this.getTy(), this.getTz());
                return;
            }
            case 2: {
                affine.prependScale(this.getMxx(), this.getMyy(), this.getMzz());
                return;
            }
            case 3: {
                affine.prependScale(this.getMxx(), this.getMyy(), this.getMzz());
                affine.prependTranslation(this.getTx(), this.getTy(), this.getTz());
                return;
            }
            case 4: 
        }
        affine.prepend(this.getMxx(), this.getMxy(), this.getMxz(), this.getTx(), this.getMyx(), this.getMyy(), this.getMyz(), this.getTy(), this.getMzx(), this.getMzy(), this.getMzz(), this.getTz());
    }

    public void appendTranslation(double d, double d2) {
        this.atomicChange.start();
        this.translate2D(d, d2);
        this.atomicChange.end();
    }

    public void appendTranslation(double d, double d2, double d3) {
        this.atomicChange.start();
        this.translate3D(d, d2, d3);
        this.atomicChange.end();
    }

    private void translate2D(double d, double d2) {
        if (this.state3d != 0) {
            this.translate3D(d, d2, 0.0);
            return;
        }
        switch (this.state2d) {
            default: {
                Affine.stateError();
            }
            case 7: {
                this.setTx(d * this.getMxx() + d2 * this.getMxy() + this.getTx());
                this.setTy(d * this.getMyx() + d2 * this.getMyy() + this.getTy());
                if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = 6;
                }
                return;
            }
            case 6: {
                this.setTx(d * this.getMxx() + d2 * this.getMxy());
                this.setTy(d * this.getMyx() + d2 * this.getMyy());
                if (this.getTx() != 0.0 || this.getTy() != 0.0) {
                    this.state2d = 7;
                }
                return;
            }
            case 5: {
                this.setTx(d2 * this.getMxy() + this.getTx());
                this.setTy(d * this.getMyx() + this.getTy());
                if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = 4;
                }
                return;
            }
            case 4: {
                this.setTx(d2 * this.getMxy());
                this.setTy(d * this.getMyx());
                if (this.getTx() != 0.0 || this.getTy() != 0.0) {
                    this.state2d = 5;
                }
                return;
            }
            case 3: {
                this.setTx(d * this.getMxx() + this.getTx());
                this.setTy(d2 * this.getMyy() + this.getTy());
                if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = 2;
                }
                return;
            }
            case 2: {
                this.setTx(d * this.getMxx());
                this.setTy(d2 * this.getMyy());
                if (this.getTx() != 0.0 || this.getTy() != 0.0) {
                    this.state2d = 3;
                }
                return;
            }
            case 1: {
                this.setTx(d + this.getTx());
                this.setTy(d2 + this.getTy());
                if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = 0;
                }
                return;
            }
            case 0: 
        }
        this.setTx(d);
        this.setTy(d2);
        if (d != 0.0 || d2 != 0.0) {
            this.state2d = 1;
        }
    }

    private void translate3D(double d, double d2, double d3) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                this.translate2D(d, d2);
                if (d3 != 0.0) {
                    this.setTz(d3);
                    this.state3d = (this.state2d & 4) == 0 ? this.state2d & 2 | 1 : 4;
                }
                return;
            }
            case 1: {
                this.setTx(d + this.getTx());
                this.setTy(d2 + this.getTy());
                this.setTz(d3 + this.getTz());
                if (this.getTz() == 0.0) {
                    this.state3d = 0;
                    this.state2d = this.getTx() == 0.0 && this.getTy() == 0.0 ? 0 : 1;
                }
                return;
            }
            case 2: {
                this.setTx(d * this.getMxx());
                this.setTy(d2 * this.getMyy());
                this.setTz(d3 * this.getMzz());
                if (this.getTx() != 0.0 || this.getTy() != 0.0 || this.getTz() != 0.0) {
                    this.state3d |= 1;
                }
                return;
            }
            case 3: {
                this.setTx(d * this.getMxx() + this.getTx());
                this.setTy(d2 * this.getMyy() + this.getTy());
                this.setTz(d3 * this.getMzz() + this.getTz());
                if (this.getTz() == 0.0) {
                    if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                        this.state3d = 2;
                    }
                    if (this.getMzz() == 1.0) {
                        this.state2d = this.state3d;
                        this.state3d = 0;
                    }
                }
                return;
            }
            case 4: 
        }
        this.setTx(d * this.getMxx() + d2 * this.getMxy() + d3 * this.getMxz() + this.getTx());
        this.setTy(d * this.getMyx() + d2 * this.getMyy() + d3 * this.getMyz() + this.getTy());
        this.setTz(d * this.getMzx() + d2 * this.getMzy() + d3 * this.getMzz() + this.getTz());
        this.updateState();
    }

    public void prependTranslation(double d, double d2, double d3) {
        this.atomicChange.start();
        this.preTranslate3D(d, d2, d3);
        this.atomicChange.end();
    }

    public void prependTranslation(double d, double d2) {
        this.atomicChange.start();
        this.preTranslate2D(d, d2);
        this.atomicChange.end();
    }

    private void preTranslate2D(double d, double d2) {
        if (this.state3d != 0) {
            this.preTranslate3D(d, d2, 0.0);
            return;
        }
        this.setTx(this.getTx() + d);
        this.setTy(this.getTy() + d2);
        this.state2d = this.getTx() == 0.0 && this.getTy() == 0.0 ? (this.state2d &= 0xFFFFFFFE) : (this.state2d |= 1);
    }

    private void preTranslate3D(double d, double d2, double d3) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                this.preTranslate2D(d, d2);
                if (d3 != 0.0) {
                    this.setTz(d3);
                    this.state3d = (this.state2d & 4) == 0 ? this.state2d & 2 | 1 : 4;
                }
                return;
            }
            case 1: {
                this.setTx(this.getTx() + d);
                this.setTy(this.getTy() + d2);
                this.setTz(this.getTz() + d3);
                if (this.getTz() == 0.0) {
                    this.state3d = 0;
                    this.state2d = this.getTx() == 0.0 && this.getTy() == 0.0 ? 0 : 1;
                }
                return;
            }
            case 2: {
                this.setTx(d);
                this.setTy(d2);
                this.setTz(d3);
                if (d != 0.0 || d2 != 0.0 || d3 != 0.0) {
                    this.state3d |= 1;
                }
                return;
            }
            case 3: {
                this.setTx(this.getTx() + d);
                this.setTy(this.getTy() + d2);
                this.setTz(this.getTz() + d3);
                if (this.getTz() == 0.0) {
                    if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                        this.state3d = 2;
                    }
                    if (this.getMzz() == 1.0) {
                        this.state2d = this.state3d;
                        this.state3d = 0;
                    }
                }
                return;
            }
            case 4: 
        }
        this.setTx(this.getTx() + d);
        this.setTy(this.getTy() + d2);
        this.setTz(this.getTz() + d3);
        if (this.getTz() == 0.0 && this.getMxz() == 0.0 && this.getMyz() == 0.0 && this.getMzx() == 0.0 && this.getMzy() == 0.0 && this.getMzz() == 1.0) {
            this.state3d = 0;
            this.updateState2D();
        }
    }

    public void appendScale(double d, double d2) {
        this.atomicChange.start();
        this.scale2D(d, d2);
        this.atomicChange.end();
    }

    public void appendScale(double d, double d2, double d3, double d4) {
        this.atomicChange.start();
        if (d3 != 0.0 || d4 != 0.0) {
            this.translate2D(d3, d4);
            this.scale2D(d, d2);
            this.translate2D(-d3, -d4);
        } else {
            this.scale2D(d, d2);
        }
        this.atomicChange.end();
    }

    public void appendScale(double d, double d2, Point2D point2D) {
        this.appendScale(d, d2, point2D.getX(), point2D.getY());
    }

    public void appendScale(double d, double d2, double d3) {
        this.atomicChange.start();
        this.scale3D(d, d2, d3);
        this.atomicChange.end();
    }

    public void appendScale(double d, double d2, double d3, double d4, double d5, double d6) {
        this.atomicChange.start();
        if (d4 != 0.0 || d5 != 0.0 || d6 != 0.0) {
            this.translate3D(d4, d5, d6);
            this.scale3D(d, d2, d3);
            this.translate3D(-d4, -d5, -d6);
        } else {
            this.scale3D(d, d2, d3);
        }
        this.atomicChange.end();
    }

    public void appendScale(double d, double d2, double d3, Point3D point3D) {
        this.appendScale(d, d2, d3, point3D.getX(), point3D.getY(), point3D.getZ());
    }

    private void scale2D(double d, double d2) {
        if (this.state3d != 0) {
            this.scale3D(d, d2, 1.0);
            return;
        }
        int n = this.state2d;
        switch (n) {
            default: {
                Affine.stateError();
            }
            case 6: 
            case 7: {
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
            }
            case 4: 
            case 5: {
                this.setMxy(this.getMxy() * d2);
                this.setMyx(this.getMyx() * d);
                if (this.getMxy() == 0.0 && this.getMyx() == 0.0) {
                    n &= 1;
                    if (this.getMxx() != 1.0 || this.getMyy() != 1.0) {
                        n |= 2;
                    }
                    this.state2d = n;
                } else if (this.getMxx() == 0.0 && this.getMyy() == 0.0) {
                    this.state2d &= 0xFFFFFFFD;
                }
                return;
            }
            case 2: 
            case 3: {
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
                if (this.getMxx() == 1.0 && this.getMyy() == 1.0) {
                    this.state2d = n &= 1;
                }
                return;
            }
            case 0: 
            case 1: 
        }
        this.setMxx(d);
        this.setMyy(d2);
        if (d != 1.0 || d2 != 1.0) {
            this.state2d = n | 2;
        }
    }

    private void scale3D(double d, double d2, double d3) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                this.scale2D(d, d2);
                if (d3 != 1.0) {
                    this.setMzz(d3);
                    this.state3d = (this.state2d & 4) == 0 ? this.state2d & 1 | 2 : 4;
                }
                return;
            }
            case 1: {
                this.setMxx(d);
                this.setMyy(d2);
                this.setMzz(d3);
                if (d != 1.0 || d2 != 1.0 || d3 != 1.0) {
                    this.state3d |= 2;
                }
                return;
            }
            case 2: {
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
                this.setMzz(this.getMzz() * d3);
                if (this.getMzz() == 1.0) {
                    this.state3d = 0;
                    this.state2d = this.getMxx() == 1.0 && this.getMyy() == 1.0 ? 0 : 2;
                }
                return;
            }
            case 3: {
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
                this.setMzz(this.getMzz() * d3);
                if (this.getMxx() == 1.0 && this.getMyy() == 1.0 && this.getMzz() == 1.0) {
                    this.state3d &= 0xFFFFFFFD;
                }
                if (this.getTz() == 0.0 && this.getMzz() == 1.0) {
                    this.state2d = this.state3d;
                    this.state3d = 0;
                }
                return;
            }
            case 4: 
        }
        this.setMxx(this.getMxx() * d);
        this.setMxy(this.getMxy() * d2);
        this.setMxz(this.getMxz() * d3);
        this.setMyx(this.getMyx() * d);
        this.setMyy(this.getMyy() * d2);
        this.setMyz(this.getMyz() * d3);
        this.setMzx(this.getMzx() * d);
        this.setMzy(this.getMzy() * d2);
        this.setMzz(this.getMzz() * d3);
        if (d == 0.0 || d2 == 0.0 || d3 == 0.0) {
            this.updateState();
        }
    }

    public void prependScale(double d, double d2) {
        this.atomicChange.start();
        this.preScale2D(d, d2);
        this.atomicChange.end();
    }

    public void prependScale(double d, double d2, double d3, double d4) {
        this.atomicChange.start();
        if (d3 != 0.0 || d4 != 0.0) {
            this.preTranslate2D(-d3, -d4);
            this.preScale2D(d, d2);
            this.preTranslate2D(d3, d4);
        } else {
            this.preScale2D(d, d2);
        }
        this.atomicChange.end();
    }

    public void prependScale(double d, double d2, Point2D point2D) {
        this.prependScale(d, d2, point2D.getX(), point2D.getY());
    }

    public void prependScale(double d, double d2, double d3) {
        this.atomicChange.start();
        this.preScale3D(d, d2, d3);
        this.atomicChange.end();
    }

    public void prependScale(double d, double d2, double d3, double d4, double d5, double d6) {
        this.atomicChange.start();
        if (d4 != 0.0 || d5 != 0.0 || d6 != 0.0) {
            this.preTranslate3D(-d4, -d5, -d6);
            this.preScale3D(d, d2, d3);
            this.preTranslate3D(d4, d5, d6);
        } else {
            this.preScale3D(d, d2, d3);
        }
        this.atomicChange.end();
    }

    public void prependScale(double d, double d2, double d3, Point3D point3D) {
        this.prependScale(d, d2, d3, point3D.getX(), point3D.getY(), point3D.getZ());
    }

    private void preScale2D(double d, double d2) {
        if (this.state3d != 0) {
            this.preScale3D(d, d2, 1.0);
            return;
        }
        int n = this.state2d;
        switch (n) {
            default: {
                Affine.stateError();
            }
            case 7: {
                this.setTx(this.getTx() * d);
                this.setTy(this.getTy() * d2);
                if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = n &= 0xFFFFFFFE;
                }
            }
            case 6: {
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
            }
            case 4: {
                this.setMxy(this.getMxy() * d);
                this.setMyx(this.getMyx() * d2);
                if (this.getMxy() == 0.0 && this.getMyx() == 0.0) {
                    n &= 1;
                    if (this.getMxx() != 1.0 || this.getMyy() != 1.0) {
                        n |= 2;
                    }
                    this.state2d = n;
                }
                return;
            }
            case 5: {
                this.setTx(this.getTx() * d);
                this.setTy(this.getTy() * d2);
                this.setMxy(this.getMxy() * d);
                this.setMyx(this.getMyx() * d2);
                if (this.getMxy() == 0.0 && this.getMyx() == 0.0) {
                    this.state2d = this.getTx() == 0.0 && this.getTy() == 0.0 ? 2 : 3;
                } else if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = 4;
                }
                return;
            }
            case 3: {
                this.setTx(this.getTx() * d);
                this.setTy(this.getTy() * d2);
                if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = n &= 0xFFFFFFFE;
                }
            }
            case 2: {
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
                if (this.getMxx() == 1.0 && this.getMyy() == 1.0) {
                    this.state2d = n &= 1;
                }
                return;
            }
            case 1: {
                this.setTx(this.getTx() * d);
                this.setTy(this.getTy() * d2);
                if (this.getTx() != 0.0 || this.getTy() != 0.0) break;
                this.state2d = n &= 0xFFFFFFFE;
            }
            case 0: 
        }
        this.setMxx(d);
        this.setMyy(d2);
        if (d != 1.0 || d2 != 1.0) {
            this.state2d = n | 2;
        }
    }

    private void preScale3D(double d, double d2, double d3) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                this.preScale2D(d, d2);
                if (d3 != 1.0) {
                    this.setMzz(d3);
                    this.state3d = (this.state2d & 4) == 0 ? this.state2d & 1 | 2 : 4;
                }
                return;
            }
            case 3: {
                this.setTx(this.getTx() * d);
                this.setTy(this.getTy() * d2);
                this.setTz(this.getTz() * d3);
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
                this.setMzz(this.getMzz() * d3);
                if (this.getTx() == 0.0 && this.getTy() == 0.0 && this.getTz() == 0.0) {
                    this.state3d &= 0xFFFFFFFE;
                }
                if (this.getMxx() == 1.0 && this.getMyy() == 1.0 && this.getMzz() == 1.0) {
                    this.state3d &= 0xFFFFFFFD;
                }
                if (this.getTz() == 0.0 && this.getMzz() == 1.0) {
                    this.state2d = this.state3d;
                    this.state3d = 0;
                }
                return;
            }
            case 2: {
                this.setMxx(this.getMxx() * d);
                this.setMyy(this.getMyy() * d2);
                this.setMzz(this.getMzz() * d3);
                if (this.getMzz() == 1.0) {
                    this.state3d = 0;
                    this.state2d = this.getMxx() == 1.0 && this.getMyy() == 1.0 ? 0 : 2;
                }
                return;
            }
            case 1: {
                this.setTx(this.getTx() * d);
                this.setTy(this.getTy() * d2);
                this.setTz(this.getTz() * d3);
                this.setMxx(d);
                this.setMyy(d2);
                this.setMzz(d3);
                if (this.getTx() == 0.0 && this.getTy() == 0.0 && this.getTz() == 0.0) {
                    this.state3d &= 0xFFFFFFFE;
                }
                if (d != 1.0 || d2 != 1.0 || d3 != 1.0) {
                    this.state3d |= 2;
                }
                return;
            }
            case 4: 
        }
        this.setMxx(this.getMxx() * d);
        this.setMxy(this.getMxy() * d);
        this.setMxz(this.getMxz() * d);
        this.setTx(this.getTx() * d);
        this.setMyx(this.getMyx() * d2);
        this.setMyy(this.getMyy() * d2);
        this.setMyz(this.getMyz() * d2);
        this.setTy(this.getTy() * d2);
        this.setMzx(this.getMzx() * d3);
        this.setMzy(this.getMzy() * d3);
        this.setMzz(this.getMzz() * d3);
        this.setTz(this.getTz() * d3);
        if (d == 0.0 || d2 == 0.0 || d3 == 0.0) {
            this.updateState();
        }
    }

    public void appendShear(double d, double d2) {
        this.atomicChange.start();
        this.shear2D(d, d2);
        this.atomicChange.end();
    }

    public void appendShear(double d, double d2, double d3, double d4) {
        this.atomicChange.start();
        if (d3 != 0.0 || d4 != 0.0) {
            this.translate2D(d3, d4);
            this.shear2D(d, d2);
            this.translate2D(-d3, -d4);
        } else {
            this.shear2D(d, d2);
        }
        this.atomicChange.end();
    }

    public void appendShear(double d, double d2, Point2D point2D) {
        this.appendShear(d, d2, point2D.getX(), point2D.getY());
    }

    private void shear2D(double d, double d2) {
        if (this.state3d != 0) {
            this.shear3D(d, d2);
            return;
        }
        int n = this.state2d;
        switch (n) {
            default: {
                Affine.stateError();
            }
            case 6: 
            case 7: {
                double d3 = this.getMxx();
                double d4 = this.getMxy();
                this.setMxx(d3 + d4 * d2);
                this.setMxy(d3 * d + d4);
                d3 = this.getMyx();
                d4 = this.getMyy();
                this.setMyx(d3 + d4 * d2);
                this.setMyy(d3 * d + d4);
                this.updateState2D();
                return;
            }
            case 4: 
            case 5: {
                this.setMxx(this.getMxy() * d2);
                this.setMyy(this.getMyx() * d);
                if (this.getMxx() != 0.0 || this.getMyy() != 0.0) {
                    this.state2d = n | 2;
                }
                return;
            }
            case 2: 
            case 3: {
                this.setMxy(this.getMxx() * d);
                this.setMyx(this.getMyy() * d2);
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state2d = n | 4;
                }
                return;
            }
            case 0: 
            case 1: 
        }
        this.setMxy(d);
        this.setMyx(d2);
        if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
            this.state2d = n | 2 | 4;
        }
    }

    private void shear3D(double d, double d2) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                this.shear2D(d, d2);
                return;
            }
            case 1: {
                this.setMxy(d);
                this.setMyx(d2);
                if (d != 0.0 || d2 != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 2: 
            case 3: {
                this.setMxy(this.getMxx() * d);
                this.setMyx(this.getMyy() * d2);
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 4: 
        }
        double d3 = this.getMxx();
        double d4 = this.getMxy();
        double d5 = this.getMyx();
        double d6 = this.getMyy();
        double d7 = this.getMzx();
        double d8 = this.getMzy();
        this.setMxx(d3 + d4 * d2);
        this.setMxy(d4 + d3 * d);
        this.setMyx(d5 + d6 * d2);
        this.setMyy(d6 + d5 * d);
        this.setMzx(d7 + d8 * d2);
        this.setMzy(d8 + d7 * d);
        this.updateState();
    }

    public void prependShear(double d, double d2) {
        this.atomicChange.start();
        this.preShear2D(d, d2);
        this.atomicChange.end();
    }

    public void prependShear(double d, double d2, double d3, double d4) {
        this.atomicChange.start();
        if (d3 != 0.0 || d4 != 0.0) {
            this.preTranslate2D(-d3, -d4);
            this.preShear2D(d, d2);
            this.preTranslate2D(d3, d4);
        } else {
            this.preShear2D(d, d2);
        }
        this.atomicChange.end();
    }

    public void prependShear(double d, double d2, Point2D point2D) {
        this.prependShear(d, d2, point2D.getX(), point2D.getY());
    }

    private void preShear2D(double d, double d2) {
        if (this.state3d != 0) {
            this.preShear3D(d, d2);
            return;
        }
        int n = this.state2d;
        switch (n) {
            default: {
                Affine.stateError();
            }
            case 5: 
            case 7: {
                double d3 = this.getTx();
                double d4 = this.getTy();
                this.setTx(d3 + d * d4);
                this.setTy(d4 + d2 * d3);
            }
            case 4: 
            case 6: {
                double d5 = this.getMxx();
                double d6 = this.getMxy();
                double d7 = this.getMyx();
                double d8 = this.getMyy();
                this.setMxx(d5 + d * d7);
                this.setMxy(d6 + d * d8);
                this.setMyx(d2 * d5 + d7);
                this.setMyy(d2 * d6 + d8);
                this.updateState2D();
                return;
            }
            case 3: {
                double d9 = this.getTx();
                double d10 = this.getTy();
                this.setTx(d9 + d * d10);
                this.setTy(d10 + d2 * d9);
                if (this.getTx() == 0.0 && this.getTy() == 0.0) {
                    this.state2d = n &= 0xFFFFFFFE;
                }
            }
            case 2: {
                this.setMxy(d * this.getMyy());
                this.setMyx(d2 * this.getMxx());
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state2d = n | 4;
                }
                return;
            }
            case 1: {
                double d11 = this.getTx();
                double d12 = this.getTy();
                this.setTx(d11 + d * d12);
                this.setTy(d12 + d2 * d11);
                if (this.getTx() != 0.0 || this.getTy() != 0.0) break;
                this.state2d = n &= 0xFFFFFFFE;
            }
            case 0: 
        }
        this.setMxy(d);
        this.setMyx(d2);
        if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
            this.state2d = n | 2 | 4;
        }
    }

    private void preShear3D(double d, double d2) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                this.preShear2D(d, d2);
                return;
            }
            case 1: {
                double d3 = this.getTx();
                this.setMxy(d);
                this.setTx(d3 + this.getTy() * d);
                this.setMyx(d2);
                this.setTy(d3 * d2 + this.getTy());
                if (d != 0.0 || d2 != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 2: {
                this.setMxy(this.getMyy() * d);
                this.setMyx(this.getMxx() * d2);
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 3: {
                double d4 = this.getTx();
                this.setMxy(this.getMyy() * d);
                this.setTx(d4 + this.getTy() * d);
                this.setMyx(this.getMxx() * d2);
                this.setTy(d4 * d2 + this.getTy());
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 4: 
        }
        double d5 = this.getMxx();
        double d6 = this.getMxy();
        double d7 = this.getMyx();
        double d8 = this.getTx();
        double d9 = this.getMyy();
        double d10 = this.getMxz();
        double d11 = this.getMyz();
        double d12 = this.getTy();
        this.setMxx(d5 + d7 * d);
        this.setMxy(d6 + d9 * d);
        this.setMxz(d10 + d11 * d);
        this.setTx(d8 + d12 * d);
        this.setMyx(d5 * d2 + d7);
        this.setMyy(d6 * d2 + d9);
        this.setMyz(d10 * d2 + d11);
        this.setTy(d8 * d2 + d12);
        this.updateState();
    }

    public void appendRotation(double d) {
        this.atomicChange.start();
        this.rotate2D(d);
        this.atomicChange.end();
    }

    public void appendRotation(double d, double d2, double d3) {
        this.atomicChange.start();
        if (d2 != 0.0 || d3 != 0.0) {
            this.translate2D(d2, d3);
            this.rotate2D(d);
            this.translate2D(-d2, -d3);
        } else {
            this.rotate2D(d);
        }
        this.atomicChange.end();
    }

    public void appendRotation(double d, Point2D point2D) {
        this.appendRotation(d, point2D.getX(), point2D.getY());
    }

    public void appendRotation(double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        this.atomicChange.start();
        if (d2 != 0.0 || d3 != 0.0 || d4 != 0.0) {
            this.translate3D(d2, d3, d4);
            this.rotate3D(d, d5, d6, d7);
            this.translate3D(-d2, -d3, -d4);
        } else {
            this.rotate3D(d, d5, d6, d7);
        }
        this.atomicChange.end();
    }

    public void appendRotation(double d, double d2, double d3, double d4, Point3D point3D) {
        this.appendRotation(d, d2, d3, d4, point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public void appendRotation(double d, Point3D point3D, Point3D point3D2) {
        this.appendRotation(d, point3D.getX(), point3D.getY(), point3D.getZ(), point3D2.getX(), point3D2.getY(), point3D2.getZ());
    }

    private void rotate3D(double d, double d2, double d3, double d4) {
        if (d2 == 0.0 && d3 == 0.0) {
            if (d4 > 0.0) {
                this.rotate3D(d);
            } else if (d4 < 0.0) {
                this.rotate3D(-d);
            }
            return;
        }
        double d5 = Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
        if (d5 == 0.0) {
            return;
        }
        d5 = 1.0 / d5;
        double d6 = d2 * d5;
        double d7 = d3 * d5;
        double d8 = d4 * d5;
        double d9 = Math.sin(Math.toRadians(d));
        double d10 = Math.cos(Math.toRadians(d));
        double d11 = 1.0 - d10;
        double d12 = d6 * d8;
        double d13 = d6 * d7;
        double d14 = d7 * d8;
        double d15 = d11 * d6 * d6 + d10;
        double d16 = d11 * d13 - d9 * d8;
        double d17 = d11 * d12 + d9 * d7;
        double d18 = d11 * d13 + d9 * d8;
        double d19 = d11 * d7 * d7 + d10;
        double d20 = d11 * d14 - d9 * d6;
        double d21 = d11 * d12 - d9 * d7;
        double d22 = d11 * d14 + d9 * d6;
        double d23 = d11 * d8 * d8 + d10;
        block0 : switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        Affine.stateError();
                    }
                    case 6: 
                    case 7: {
                        double d24 = this.getMxx();
                        double d25 = this.getMxy();
                        double d26 = this.getMyx();
                        double d27 = this.getMyy();
                        this.setMxx(d24 * d15 + d25 * d18);
                        this.setMxy(d24 * d16 + d25 * d19);
                        this.setMxz(d24 * d17 + d25 * d20);
                        this.setMyx(d26 * d15 + d27 * d18);
                        this.setMyy(d26 * d16 + d27 * d19);
                        this.setMyz(d26 * d17 + d27 * d20);
                        this.setMzx(d21);
                        this.setMzy(d22);
                        this.setMzz(d23);
                        break block0;
                    }
                    case 4: 
                    case 5: {
                        double d28 = this.getMxy();
                        double d29 = this.getMyx();
                        this.setMxx(d28 * d18);
                        this.setMxy(d28 * d19);
                        this.setMxz(d28 * d20);
                        this.setMyx(d29 * d15);
                        this.setMyy(d29 * d16);
                        this.setMyz(d29 * d17);
                        this.setMzx(d21);
                        this.setMzy(d22);
                        this.setMzz(d23);
                        break block0;
                    }
                    case 2: 
                    case 3: {
                        double d30 = this.getMxx();
                        double d31 = this.getMyy();
                        this.setMxx(d30 * d15);
                        this.setMxy(d30 * d16);
                        this.setMxz(d30 * d17);
                        this.setMyx(d31 * d18);
                        this.setMyy(d31 * d19);
                        this.setMyz(d31 * d20);
                        this.setMzx(d21);
                        this.setMzy(d22);
                        this.setMzz(d23);
                        break block0;
                    }
                    case 0: 
                    case 1: 
                }
                this.setMxx(d15);
                this.setMxy(d16);
                this.setMxz(d17);
                this.setMyx(d18);
                this.setMyy(d19);
                this.setMyz(d20);
                this.setMzx(d21);
                this.setMzy(d22);
                this.setMzz(d23);
                break;
            }
            case 1: {
                this.setMxx(d15);
                this.setMxy(d16);
                this.setMxz(d17);
                this.setMyx(d18);
                this.setMyy(d19);
                this.setMyz(d20);
                this.setMzx(d21);
                this.setMzy(d22);
                this.setMzz(d23);
                break;
            }
            case 2: 
            case 3: {
                double d32 = this.getMxx();
                double d33 = this.getMyy();
                double d34 = this.getMzz();
                this.setMxx(d32 * d15);
                this.setMxy(d32 * d16);
                this.setMxz(d32 * d17);
                this.setMyx(d33 * d18);
                this.setMyy(d33 * d19);
                this.setMyz(d33 * d20);
                this.setMzx(d34 * d21);
                this.setMzy(d34 * d22);
                this.setMzz(d34 * d23);
                break;
            }
            case 4: {
                double d35 = this.getMxx();
                double d36 = this.getMxy();
                double d37 = this.getMxz();
                double d38 = this.getMyx();
                double d39 = this.getMyy();
                double d40 = this.getMyz();
                double d41 = this.getMzx();
                double d42 = this.getMzy();
                double d43 = this.getMzz();
                this.setMxx(d35 * d15 + d36 * d18 + d37 * d21);
                this.setMxy(d35 * d16 + d36 * d19 + d37 * d22);
                this.setMxz(d35 * d17 + d36 * d20 + d37 * d23);
                this.setMyx(d38 * d15 + d39 * d18 + d40 * d21);
                this.setMyy(d38 * d16 + d39 * d19 + d40 * d22);
                this.setMyz(d38 * d17 + d39 * d20 + d40 * d23);
                this.setMzx(d41 * d15 + d42 * d18 + d43 * d21);
                this.setMzy(d41 * d16 + d42 * d19 + d43 * d22);
                this.setMzz(d41 * d17 + d42 * d20 + d43 * d23);
            }
        }
        this.updateState();
    }

    private void rotate2D(double d) {
        if (this.state3d != 0) {
            this.rotate3D(d);
            return;
        }
        double d2 = Math.sin(Math.toRadians(d));
        if (d2 == 1.0) {
            this.rotate2D_90();
        } else if (d2 == -1.0) {
            this.rotate2D_270();
        } else {
            double d3 = Math.cos(Math.toRadians(d));
            if (d3 == -1.0) {
                this.rotate2D_180();
            } else if (d3 != 1.0) {
                double d4 = this.getMxx();
                double d5 = this.getMxy();
                this.setMxx(d3 * d4 + d2 * d5);
                this.setMxy(-d2 * d4 + d3 * d5);
                d4 = this.getMyx();
                d5 = this.getMyy();
                this.setMyx(d3 * d4 + d2 * d5);
                this.setMyy(-d2 * d4 + d3 * d5);
                this.updateState2D();
            }
        }
    }

    private void rotate2D_90() {
        double d = this.getMxx();
        this.setMxx(this.getMxy());
        this.setMxy(-d);
        d = this.getMyx();
        this.setMyx(this.getMyy());
        this.setMyy(-d);
        int n = rot90conversion[this.state2d];
        if ((n & 6) == 2 && this.getMxx() == 1.0 && this.getMyy() == 1.0) {
            n -= 2;
        } else if ((n & 6) == 4 && this.getMxy() == 0.0 && this.getMyx() == 0.0) {
            n = n & 0xFFFFFFFB | 2;
        }
        this.state2d = n;
    }

    private void rotate2D_180() {
        this.setMxx(-this.getMxx());
        this.setMyy(-this.getMyy());
        int n = this.state2d;
        if ((n & 4) != 0) {
            this.setMxy(-this.getMxy());
            this.setMyx(-this.getMyx());
        } else {
            this.state2d = this.getMxx() == 1.0 && this.getMyy() == 1.0 ? n & 0xFFFFFFFD : n | 2;
        }
    }

    private void rotate2D_270() {
        double d = this.getMxx();
        this.setMxx(-this.getMxy());
        this.setMxy(d);
        d = this.getMyx();
        this.setMyx(-this.getMyy());
        this.setMyy(d);
        int n = rot90conversion[this.state2d];
        if ((n & 6) == 2 && this.getMxx() == 1.0 && this.getMyy() == 1.0) {
            n -= 2;
        } else if ((n & 6) == 4 && this.getMxy() == 0.0 && this.getMyx() == 0.0) {
            n = n & 0xFFFFFFFB | 2;
        }
        this.state2d = n;
    }

    private void rotate3D(double d) {
        if (this.state3d == 0) {
            this.rotate2D(d);
            return;
        }
        double d2 = Math.sin(Math.toRadians(d));
        if (d2 == 1.0) {
            this.rotate3D_90();
        } else if (d2 == -1.0) {
            this.rotate3D_270();
        } else {
            double d3 = Math.cos(Math.toRadians(d));
            if (d3 == -1.0) {
                this.rotate3D_180();
            } else if (d3 != 1.0) {
                double d4 = this.getMxx();
                double d5 = this.getMxy();
                this.setMxx(d3 * d4 + d2 * d5);
                this.setMxy(-d2 * d4 + d3 * d5);
                d4 = this.getMyx();
                d5 = this.getMyy();
                this.setMyx(d3 * d4 + d2 * d5);
                this.setMyy(-d2 * d4 + d3 * d5);
                d4 = this.getMzx();
                d5 = this.getMzy();
                this.setMzx(d3 * d4 + d2 * d5);
                this.setMzy(-d2 * d4 + d3 * d5);
                this.updateState();
            }
        }
    }

    private void rotate3D_90() {
        double d = this.getMxx();
        this.setMxx(this.getMxy());
        this.setMxy(-d);
        d = this.getMyx();
        this.setMyx(this.getMyy());
        this.setMyy(-d);
        d = this.getMzx();
        this.setMzx(this.getMzy());
        this.setMzy(-d);
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 1: {
                this.state3d = 4;
                return;
            }
            case 2: 
            case 3: {
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 4: 
        }
        this.updateState();
    }

    private void rotate3D_180() {
        double d = this.getMxx();
        double d2 = this.getMyy();
        this.setMxx(-d);
        this.setMyy(-d2);
        if (this.state3d == 4) {
            this.setMxy(-this.getMxy());
            this.setMyx(-this.getMyx());
            this.setMzx(-this.getMzx());
            this.setMzy(-this.getMzy());
            this.updateState();
            return;
        }
        this.state3d = d == -1.0 && d2 == -1.0 && this.getMzz() == 1.0 ? (this.state3d &= 0xFFFFFFFD) : (this.state3d |= 2);
    }

    private void rotate3D_270() {
        double d = this.getMxx();
        this.setMxx(-this.getMxy());
        this.setMxy(d);
        d = this.getMyx();
        this.setMyx(-this.getMyy());
        this.setMyy(d);
        d = this.getMzx();
        this.setMzx(-this.getMzy());
        this.setMzy(d);
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 1: {
                this.state3d = 4;
                return;
            }
            case 2: 
            case 3: {
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 4: 
        }
        this.updateState();
    }

    public void prependRotation(double d) {
        this.atomicChange.start();
        this.preRotate2D(d);
        this.atomicChange.end();
    }

    public void prependRotation(double d, double d2, double d3) {
        this.atomicChange.start();
        if (d2 != 0.0 || d3 != 0.0) {
            this.preTranslate2D(-d2, -d3);
            this.preRotate2D(d);
            this.preTranslate2D(d2, d3);
        } else {
            this.preRotate2D(d);
        }
        this.atomicChange.end();
    }

    public void prependRotation(double d, Point2D point2D) {
        this.prependRotation(d, point2D.getX(), point2D.getY());
    }

    public void prependRotation(double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        this.atomicChange.start();
        if (d2 != 0.0 || d3 != 0.0 || d4 != 0.0) {
            this.preTranslate3D(-d2, -d3, -d4);
            this.preRotate3D(d, d5, d6, d7);
            this.preTranslate3D(d2, d3, d4);
        } else {
            this.preRotate3D(d, d5, d6, d7);
        }
        this.atomicChange.end();
    }

    public void prependRotation(double d, double d2, double d3, double d4, Point3D point3D) {
        this.prependRotation(d, d2, d3, d4, point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public void prependRotation(double d, Point3D point3D, Point3D point3D2) {
        this.prependRotation(d, point3D.getX(), point3D.getY(), point3D.getZ(), point3D2.getX(), point3D2.getY(), point3D2.getZ());
    }

    private void preRotate3D(double d, double d2, double d3, double d4) {
        if (d2 == 0.0 && d3 == 0.0) {
            if (d4 > 0.0) {
                this.preRotate3D(d);
            } else if (d4 < 0.0) {
                this.preRotate3D(-d);
            }
            return;
        }
        double d5 = Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
        if (d5 == 0.0) {
            return;
        }
        d5 = 1.0 / d5;
        double d6 = d2 * d5;
        double d7 = d3 * d5;
        double d8 = d4 * d5;
        double d9 = Math.sin(Math.toRadians(d));
        double d10 = Math.cos(Math.toRadians(d));
        double d11 = 1.0 - d10;
        double d12 = d6 * d8;
        double d13 = d6 * d7;
        double d14 = d7 * d8;
        double d15 = d11 * d6 * d6 + d10;
        double d16 = d11 * d13 - d9 * d8;
        double d17 = d11 * d12 + d9 * d7;
        double d18 = d11 * d13 + d9 * d8;
        double d19 = d11 * d7 * d7 + d10;
        double d20 = d11 * d14 - d9 * d6;
        double d21 = d11 * d12 - d9 * d7;
        double d22 = d11 * d14 + d9 * d6;
        double d23 = d11 * d8 * d8 + d10;
        block0 : switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        Affine.stateError();
                    }
                    case 7: {
                        double d24 = this.getMxx();
                        double d25 = this.getMxy();
                        double d26 = this.getTx();
                        double d27 = this.getMyx();
                        double d28 = this.getMyy();
                        double d29 = this.getTy();
                        this.setMxx(d15 * d24 + d16 * d27);
                        this.setMxy(d15 * d25 + d16 * d28);
                        this.setMxz(d17);
                        this.setTx(d15 * d26 + d16 * d29);
                        this.setMyx(d18 * d24 + d19 * d27);
                        this.setMyy(d18 * d25 + d19 * d28);
                        this.setMyz(d20);
                        this.setTy(d18 * d26 + d19 * d29);
                        this.setMzx(d21 * d24 + d22 * d27);
                        this.setMzy(d21 * d25 + d22 * d28);
                        this.setMzz(d23);
                        this.setTz(d21 * d26 + d22 * d29);
                        break block0;
                    }
                    case 6: {
                        double d30 = this.getMxx();
                        double d31 = this.getMxy();
                        double d32 = this.getMyx();
                        double d33 = this.getMyy();
                        this.setMxx(d15 * d30 + d16 * d32);
                        this.setMxy(d15 * d31 + d16 * d33);
                        this.setMxz(d17);
                        this.setMyx(d18 * d30 + d19 * d32);
                        this.setMyy(d18 * d31 + d19 * d33);
                        this.setMyz(d20);
                        this.setMzx(d21 * d30 + d22 * d32);
                        this.setMzy(d21 * d31 + d22 * d33);
                        this.setMzz(d23);
                        break block0;
                    }
                    case 5: {
                        double d34 = this.getMxy();
                        double d35 = this.getTx();
                        double d36 = this.getMyx();
                        double d37 = this.getTy();
                        this.setMxx(d16 * d36);
                        this.setMxy(d15 * d34);
                        this.setMxz(d17);
                        this.setTx(d15 * d35 + d16 * d37);
                        this.setMyx(d19 * d36);
                        this.setMyy(d18 * d34);
                        this.setMyz(d20);
                        this.setTy(d18 * d35 + d19 * d37);
                        this.setMzx(d22 * d36);
                        this.setMzy(d21 * d34);
                        this.setMzz(d23);
                        this.setTz(d21 * d35 + d22 * d37);
                        break block0;
                    }
                    case 4: {
                        double d38 = this.getMxy();
                        double d39 = this.getMyx();
                        this.setMxx(d16 * d39);
                        this.setMxy(d15 * d38);
                        this.setMxz(d17);
                        this.setMyx(d19 * d39);
                        this.setMyy(d18 * d38);
                        this.setMyz(d20);
                        this.setMzx(d22 * d39);
                        this.setMzy(d21 * d38);
                        this.setMzz(d23);
                        break block0;
                    }
                    case 3: {
                        double d40 = this.getMxx();
                        double d41 = this.getTx();
                        double d42 = this.getMyy();
                        double d43 = this.getTy();
                        this.setMxx(d15 * d40);
                        this.setMxy(d16 * d42);
                        this.setMxz(d17);
                        this.setTx(d15 * d41 + d16 * d43);
                        this.setMyx(d18 * d40);
                        this.setMyy(d19 * d42);
                        this.setMyz(d20);
                        this.setTy(d18 * d41 + d19 * d43);
                        this.setMzx(d21 * d40);
                        this.setMzy(d22 * d42);
                        this.setMzz(d23);
                        this.setTz(d21 * d41 + d22 * d43);
                        break block0;
                    }
                    case 2: {
                        double d44 = this.getMxx();
                        double d45 = this.getMyy();
                        this.setMxx(d15 * d44);
                        this.setMxy(d16 * d45);
                        this.setMxz(d17);
                        this.setMyx(d18 * d44);
                        this.setMyy(d19 * d45);
                        this.setMyz(d20);
                        this.setMzx(d21 * d44);
                        this.setMzy(d22 * d45);
                        this.setMzz(d23);
                        break block0;
                    }
                    case 1: {
                        double d46 = this.getTx();
                        double d47 = this.getTy();
                        this.setMxx(d15);
                        this.setMxy(d16);
                        this.setMxz(d17);
                        this.setTx(d15 * d46 + d16 * d47);
                        this.setMyx(d18);
                        this.setMyy(d19);
                        this.setMyz(d20);
                        this.setTy(d18 * d46 + d19 * d47);
                        this.setMzx(d21);
                        this.setMzy(d22);
                        this.setMzz(d23);
                        this.setTz(d21 * d46 + d22 * d47);
                        break block0;
                    }
                    case 0: 
                }
                this.setMxx(d15);
                this.setMxy(d16);
                this.setMxz(d17);
                this.setMyx(d18);
                this.setMyy(d19);
                this.setMyz(d20);
                this.setMzx(d21);
                this.setMzy(d22);
                this.setMzz(d23);
                break;
            }
            case 1: {
                double d48 = this.getTx();
                double d49 = this.getTy();
                double d50 = this.getTz();
                this.setMxx(d15);
                this.setMxy(d16);
                this.setMxz(d17);
                this.setMyx(d18);
                this.setMyy(d19);
                this.setMyz(d20);
                this.setMzx(d21);
                this.setMzy(d22);
                this.setMzz(d23);
                this.setTx(d15 * d48 + d16 * d49 + d17 * d50);
                this.setTy(d18 * d48 + d19 * d49 + d20 * d50);
                this.setTz(d21 * d48 + d22 * d49 + d23 * d50);
                break;
            }
            case 2: {
                double d51 = this.getMxx();
                double d52 = this.getMyy();
                double d53 = this.getMzz();
                this.setMxx(d15 * d51);
                this.setMxy(d16 * d52);
                this.setMxz(d17 * d53);
                this.setMyx(d18 * d51);
                this.setMyy(d19 * d52);
                this.setMyz(d20 * d53);
                this.setMzx(d21 * d51);
                this.setMzy(d22 * d52);
                this.setMzz(d23 * d53);
                break;
            }
            case 3: {
                double d54 = this.getMxx();
                double d55 = this.getTx();
                double d56 = this.getMyy();
                double d57 = this.getTy();
                double d58 = this.getMzz();
                double d59 = this.getTz();
                this.setMxx(d15 * d54);
                this.setMxy(d16 * d56);
                this.setMxz(d17 * d58);
                this.setTx(d15 * d55 + d16 * d57 + d17 * d59);
                this.setMyx(d18 * d54);
                this.setMyy(d19 * d56);
                this.setMyz(d20 * d58);
                this.setTy(d18 * d55 + d19 * d57 + d20 * d59);
                this.setMzx(d21 * d54);
                this.setMzy(d22 * d56);
                this.setMzz(d23 * d58);
                this.setTz(d21 * d55 + d22 * d57 + d23 * d59);
                break;
            }
            case 4: {
                double d60 = this.getMxx();
                double d61 = this.getMxy();
                double d62 = this.getMxz();
                double d63 = this.getTx();
                double d64 = this.getMyx();
                double d65 = this.getMyy();
                double d66 = this.getMyz();
                double d67 = this.getTy();
                double d68 = this.getMzx();
                double d69 = this.getMzy();
                double d70 = this.getMzz();
                double d71 = this.getTz();
                this.setMxx(d15 * d60 + d16 * d64 + d17 * d68);
                this.setMxy(d15 * d61 + d16 * d65 + d17 * d69);
                this.setMxz(d15 * d62 + d16 * d66 + d17 * d70);
                this.setTx(d15 * d63 + d16 * d67 + d17 * d71);
                this.setMyx(d18 * d60 + d19 * d64 + d20 * d68);
                this.setMyy(d18 * d61 + d19 * d65 + d20 * d69);
                this.setMyz(d18 * d62 + d19 * d66 + d20 * d70);
                this.setTy(d18 * d63 + d19 * d67 + d20 * d71);
                this.setMzx(d21 * d60 + d22 * d64 + d23 * d68);
                this.setMzy(d21 * d61 + d22 * d65 + d23 * d69);
                this.setMzz(d21 * d62 + d22 * d66 + d23 * d70);
                this.setTz(d21 * d63 + d22 * d67 + d23 * d71);
            }
        }
        this.updateState();
    }

    private void preRotate2D(double d) {
        if (this.state3d != 0) {
            this.preRotate3D(d);
            return;
        }
        double d2 = Math.sin(Math.toRadians(d));
        if (d2 == 1.0) {
            this.preRotate2D_90();
        } else if (d2 == -1.0) {
            this.preRotate2D_270();
        } else {
            double d3 = Math.cos(Math.toRadians(d));
            if (d3 == -1.0) {
                this.preRotate2D_180();
            } else if (d3 != 1.0) {
                double d4 = this.getMxx();
                double d5 = this.getMyx();
                this.setMxx(d3 * d4 - d2 * d5);
                this.setMyx(d2 * d4 + d3 * d5);
                d4 = this.getMxy();
                d5 = this.getMyy();
                this.setMxy(d3 * d4 - d2 * d5);
                this.setMyy(d2 * d4 + d3 * d5);
                d4 = this.getTx();
                d5 = this.getTy();
                this.setTx(d3 * d4 - d2 * d5);
                this.setTy(d2 * d4 + d3 * d5);
                this.updateState2D();
            }
        }
    }

    private void preRotate2D_90() {
        double d = this.getMxx();
        this.setMxx(-this.getMyx());
        this.setMyx(d);
        d = this.getMxy();
        this.setMxy(-this.getMyy());
        this.setMyy(d);
        d = this.getTx();
        this.setTx(-this.getTy());
        this.setTy(d);
        int n = rot90conversion[this.state2d];
        if ((n & 6) == 2 && this.getMxx() == 1.0 && this.getMyy() == 1.0) {
            n -= 2;
        } else if ((n & 6) == 4 && this.getMxy() == 0.0 && this.getMyx() == 0.0) {
            n = n & 0xFFFFFFFB | 2;
        }
        this.state2d = n;
    }

    private void preRotate2D_180() {
        this.setMxx(-this.getMxx());
        this.setMxy(-this.getMxy());
        this.setTx(-this.getTx());
        this.setMyx(-this.getMyx());
        this.setMyy(-this.getMyy());
        this.setTy(-this.getTy());
        this.state2d = (this.state2d & 4) != 0 ? (this.getMxx() == 0.0 && this.getMyy() == 0.0 ? (this.state2d &= 0xFFFFFFFD) : (this.state2d |= 2)) : (this.getMxx() == 1.0 && this.getMyy() == 1.0 ? (this.state2d &= 0xFFFFFFFD) : (this.state2d |= 2));
    }

    private void preRotate2D_270() {
        double d = this.getMxx();
        this.setMxx(this.getMyx());
        this.setMyx(-d);
        d = this.getMxy();
        this.setMxy(this.getMyy());
        this.setMyy(-d);
        d = this.getTx();
        this.setTx(this.getTy());
        this.setTy(-d);
        int n = rot90conversion[this.state2d];
        if ((n & 6) == 2 && this.getMxx() == 1.0 && this.getMyy() == 1.0) {
            n -= 2;
        } else if ((n & 6) == 4 && this.getMxy() == 0.0 && this.getMyx() == 0.0) {
            n = n & 0xFFFFFFFB | 2;
        }
        this.state2d = n;
    }

    private void preRotate3D(double d) {
        if (this.state3d == 0) {
            this.preRotate2D(d);
            return;
        }
        double d2 = Math.sin(Math.toRadians(d));
        if (d2 == 1.0) {
            this.preRotate3D_90();
        } else if (d2 == -1.0) {
            this.preRotate3D_270();
        } else {
            double d3 = Math.cos(Math.toRadians(d));
            if (d3 == -1.0) {
                this.preRotate3D_180();
            } else if (d3 != 1.0) {
                double d4 = this.getMxx();
                double d5 = this.getMyx();
                this.setMxx(d3 * d4 - d2 * d5);
                this.setMyx(d2 * d4 + d3 * d5);
                d4 = this.getMxy();
                d5 = this.getMyy();
                this.setMxy(d3 * d4 - d2 * d5);
                this.setMyy(d2 * d4 + d3 * d5);
                d4 = this.getMxz();
                d5 = this.getMyz();
                this.setMxz(d3 * d4 - d2 * d5);
                this.setMyz(d2 * d4 + d3 * d5);
                d4 = this.getTx();
                d5 = this.getTy();
                this.setTx(d3 * d4 - d2 * d5);
                this.setTy(d2 * d4 + d3 * d5);
                this.updateState();
            }
        }
    }

    private void preRotate3D_90() {
        double d = this.getMxx();
        this.setMxx(-this.getMyx());
        this.setMyx(d);
        d = this.getMxy();
        this.setMxy(-this.getMyy());
        this.setMyy(d);
        d = this.getMxz();
        this.setMxz(-this.getMyz());
        this.setMyz(d);
        d = this.getTx();
        this.setTx(-this.getTy());
        this.setTy(d);
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 1: {
                this.state3d = 4;
                return;
            }
            case 2: 
            case 3: {
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 4: 
        }
        this.updateState();
    }

    private void preRotate3D_180() {
        double d = this.getMxx();
        double d2 = this.getMyy();
        this.setMxx(-d);
        this.setMyy(-d2);
        this.setTx(-this.getTx());
        this.setTy(-this.getTy());
        if (this.state3d == 4) {
            this.setMxy(-this.getMxy());
            this.setMxz(-this.getMxz());
            this.setMyx(-this.getMyx());
            this.setMyz(-this.getMyz());
            this.updateState();
            return;
        }
        this.state3d = d == -1.0 && d2 == -1.0 && this.getMzz() == 1.0 ? (this.state3d &= 0xFFFFFFFD) : (this.state3d |= 2);
    }

    private void preRotate3D_270() {
        double d = this.getMxx();
        this.setMxx(this.getMyx());
        this.setMyx(-d);
        d = this.getMxy();
        this.setMxy(this.getMyy());
        this.setMyy(-d);
        d = this.getMxz();
        this.setMxz(this.getMyz());
        this.setMyz(-d);
        d = this.getTx();
        this.setTx(this.getTy());
        this.setTy(-d);
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 1: {
                this.state3d = 4;
                return;
            }
            case 2: 
            case 3: {
                if (this.getMxy() != 0.0 || this.getMyx() != 0.0) {
                    this.state3d = 4;
                }
                return;
            }
            case 4: 
        }
        this.updateState();
    }

    @Override
    public Point2D transform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        switch (this.state2d) {
            default: {
                Affine.stateError();
            }
            case 7: {
                return new Point2D(this.getMxx() * d + this.getMxy() * d2 + this.getTx(), this.getMyx() * d + this.getMyy() * d2 + this.getTy());
            }
            case 6: {
                return new Point2D(this.getMxx() * d + this.getMxy() * d2, this.getMyx() * d + this.getMyy() * d2);
            }
            case 5: {
                return new Point2D(this.getMxy() * d2 + this.getTx(), this.getMyx() * d + this.getTy());
            }
            case 4: {
                return new Point2D(this.getMxy() * d2, this.getMyx() * d);
            }
            case 3: {
                return new Point2D(this.getMxx() * d + this.getTx(), this.getMyy() * d2 + this.getTy());
            }
            case 2: {
                return new Point2D(this.getMxx() * d, this.getMyy() * d2);
            }
            case 1: {
                return new Point2D(d + this.getTx(), d2 + this.getTy());
            }
            case 0: 
        }
        return new Point2D(d, d2);
    }

    @Override
    public Point3D transform(double d, double d2, double d3) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        Affine.stateError();
                    }
                    case 7: {
                        return new Point3D(this.getMxx() * d + this.getMxy() * d2 + this.getTx(), this.getMyx() * d + this.getMyy() * d2 + this.getTy(), d3);
                    }
                    case 6: {
                        return new Point3D(this.getMxx() * d + this.getMxy() * d2, this.getMyx() * d + this.getMyy() * d2, d3);
                    }
                    case 5: {
                        return new Point3D(this.getMxy() * d2 + this.getTx(), this.getMyx() * d + this.getTy(), d3);
                    }
                    case 4: {
                        return new Point3D(this.getMxy() * d2, this.getMyx() * d, d3);
                    }
                    case 3: {
                        return new Point3D(this.getMxx() * d + this.getTx(), this.getMyy() * d2 + this.getTy(), d3);
                    }
                    case 2: {
                        return new Point3D(this.getMxx() * d, this.getMyy() * d2, d3);
                    }
                    case 1: {
                        return new Point3D(d + this.getTx(), d2 + this.getTy(), d3);
                    }
                    case 0: 
                }
                return new Point3D(d, d2, d3);
            }
            case 1: {
                return new Point3D(d + this.getTx(), d2 + this.getTy(), d3 + this.getTz());
            }
            case 2: {
                return new Point3D(this.getMxx() * d, this.getMyy() * d2, this.getMzz() * d3);
            }
            case 3: {
                return new Point3D(this.getMxx() * d + this.getTx(), this.getMyy() * d2 + this.getTy(), this.getMzz() * d3 + this.getTz());
            }
            case 4: 
        }
        return new Point3D(this.getMxx() * d + this.getMxy() * d2 + this.getMxz() * d3 + this.getTx(), this.getMyx() * d + this.getMyy() * d2 + this.getMyz() * d3 + this.getTy(), this.getMzx() * d + this.getMzy() * d2 + this.getMzz() * d3 + this.getTz());
    }

    @Override
    void transform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        switch (this.state2d) {
            default: {
                Affine.stateError();
            }
            case 7: {
                double d = this.getMxx();
                double d2 = this.getMxy();
                double d3 = this.getTx();
                double d4 = this.getMyx();
                double d5 = this.getMyy();
                double d6 = this.getTy();
                while (--n3 >= 0) {
                    double d7 = dArray[n++];
                    double d8 = dArray[n++];
                    dArray2[n2++] = d * d7 + d2 * d8 + d3;
                    dArray2[n2++] = d4 * d7 + d5 * d8 + d6;
                }
                return;
            }
            case 6: {
                double d = this.getMxx();
                double d9 = this.getMxy();
                double d10 = this.getMyx();
                double d11 = this.getMyy();
                while (--n3 >= 0) {
                    double d12 = dArray[n++];
                    double d13 = dArray[n++];
                    dArray2[n2++] = d * d12 + d9 * d13;
                    dArray2[n2++] = d10 * d12 + d11 * d13;
                }
                return;
            }
            case 5: {
                double d = this.getMxy();
                double d14 = this.getTx();
                double d15 = this.getMyx();
                double d16 = this.getTy();
                while (--n3 >= 0) {
                    double d17 = dArray[n++];
                    dArray2[n2++] = d * dArray[n++] + d14;
                    dArray2[n2++] = d15 * d17 + d16;
                }
                return;
            }
            case 4: {
                double d = this.getMxy();
                double d18 = this.getMyx();
                while (--n3 >= 0) {
                    double d19 = dArray[n++];
                    dArray2[n2++] = d * dArray[n++];
                    dArray2[n2++] = d18 * d19;
                }
                return;
            }
            case 3: {
                double d = this.getMxx();
                double d20 = this.getTx();
                double d21 = this.getMyy();
                double d22 = this.getTy();
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++] + d20;
                    dArray2[n2++] = d21 * dArray[n++] + d22;
                }
                return;
            }
            case 2: {
                double d = this.getMxx();
                double d23 = this.getMyy();
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++];
                    dArray2[n2++] = d23 * dArray[n++];
                }
                return;
            }
            case 1: {
                double d = this.getTx();
                double d24 = this.getTy();
                while (--n3 >= 0) {
                    dArray2[n2++] = dArray[n++] + d;
                    dArray2[n2++] = dArray[n++] + d24;
                }
                return;
            }
            case 0: 
        }
        if (dArray != dArray2 || n != n2) {
            System.arraycopy(dArray, n, dArray2, n2, n3 * 2);
        }
    }

    @Override
    void transform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        Affine.stateError();
                    }
                    case 7: {
                        double d = this.getMxx();
                        double d2 = this.getMxy();
                        double d3 = this.getTx();
                        double d4 = this.getMyx();
                        double d5 = this.getMyy();
                        double d6 = this.getTy();
                        while (--n3 >= 0) {
                            double d7 = dArray[n++];
                            double d8 = dArray[n++];
                            dArray2[n2++] = d * d7 + d2 * d8 + d3;
                            dArray2[n2++] = d4 * d7 + d5 * d8 + d6;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 6: {
                        double d = this.getMxx();
                        double d9 = this.getMxy();
                        double d10 = this.getMyx();
                        double d11 = this.getMyy();
                        while (--n3 >= 0) {
                            double d12 = dArray[n++];
                            double d13 = dArray[n++];
                            dArray2[n2++] = d * d12 + d9 * d13;
                            dArray2[n2++] = d10 * d12 + d11 * d13;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 5: {
                        double d = this.getMxy();
                        double d14 = this.getTx();
                        double d15 = this.getMyx();
                        double d16 = this.getTy();
                        while (--n3 >= 0) {
                            double d17 = dArray[n++];
                            dArray2[n2++] = d * dArray[n++] + d14;
                            dArray2[n2++] = d15 * d17 + d16;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 4: {
                        double d = this.getMxy();
                        double d18 = this.getMyx();
                        while (--n3 >= 0) {
                            double d19 = dArray[n++];
                            dArray2[n2++] = d * dArray[n++];
                            dArray2[n2++] = d18 * d19;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 3: {
                        double d = this.getMxx();
                        double d20 = this.getTx();
                        double d21 = this.getMyy();
                        double d22 = this.getTy();
                        while (--n3 >= 0) {
                            dArray2[n2++] = d * dArray[n++] + d20;
                            dArray2[n2++] = d21 * dArray[n++] + d22;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 2: {
                        double d = this.getMxx();
                        double d23 = this.getMyy();
                        while (--n3 >= 0) {
                            dArray2[n2++] = d * dArray[n++];
                            dArray2[n2++] = d23 * dArray[n++];
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 1: {
                        double d = this.getTx();
                        double d24 = this.getTy();
                        while (--n3 >= 0) {
                            dArray2[n2++] = dArray[n++] + d;
                            dArray2[n2++] = dArray[n++] + d24;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 0: 
                }
                if (dArray != dArray2 || n != n2) {
                    System.arraycopy(dArray, n, dArray2, n2, n3 * 3);
                }
                return;
            }
            case 1: {
                double d = this.getTx();
                double d25 = this.getTy();
                double d26 = this.getTz();
                while (--n3 >= 0) {
                    dArray2[n2++] = dArray[n++] + d;
                    dArray2[n2++] = dArray[n++] + d25;
                    dArray2[n2++] = dArray[n++] + d26;
                }
                return;
            }
            case 2: {
                double d = this.getMxx();
                double d27 = this.getMyy();
                double d28 = this.getMzz();
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++];
                    dArray2[n2++] = d27 * dArray[n++];
                    dArray2[n2++] = d28 * dArray[n++];
                }
                return;
            }
            case 3: {
                double d = this.getMxx();
                double d29 = this.getTx();
                double d30 = this.getMyy();
                double d31 = this.getTy();
                double d32 = this.getMzz();
                double d33 = this.getTz();
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++] + d29;
                    dArray2[n2++] = d30 * dArray[n++] + d31;
                    dArray2[n2++] = d32 * dArray[n++] + d33;
                }
                return;
            }
            case 4: 
        }
        double d = this.getMxx();
        double d34 = this.getMxy();
        double d35 = this.getMxz();
        double d36 = this.getTx();
        double d37 = this.getMyx();
        double d38 = this.getMyy();
        double d39 = this.getMyz();
        double d40 = this.getTy();
        double d41 = this.getMzx();
        double d42 = this.getMzy();
        double d43 = this.getMzz();
        double d44 = this.getTz();
        while (--n3 >= 0) {
            double d45 = dArray[n++];
            double d46 = dArray[n++];
            double d47 = dArray[n++];
            dArray2[n2++] = d * d45 + d34 * d46 + d35 * d47 + d36;
            dArray2[n2++] = d37 * d45 + d38 * d46 + d39 * d47 + d40;
            dArray2[n2++] = d41 * d45 + d42 * d46 + d43 * d47 + d44;
        }
    }

    @Override
    public Point2D deltaTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        switch (this.state2d) {
            default: {
                Affine.stateError();
            }
            case 6: 
            case 7: {
                return new Point2D(this.getMxx() * d + this.getMxy() * d2, this.getMyx() * d + this.getMyy() * d2);
            }
            case 4: 
            case 5: {
                return new Point2D(this.getMxy() * d2, this.getMyx() * d);
            }
            case 2: 
            case 3: {
                return new Point2D(this.getMxx() * d, this.getMyy() * d2);
            }
            case 0: 
            case 1: 
        }
        return new Point2D(d, d2);
    }

    @Override
    public Point3D deltaTransform(double d, double d2, double d3) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        Affine.stateError();
                    }
                    case 6: 
                    case 7: {
                        return new Point3D(this.getMxx() * d + this.getMxy() * d2, this.getMyx() * d + this.getMyy() * d2, d3);
                    }
                    case 4: 
                    case 5: {
                        return new Point3D(this.getMxy() * d2, this.getMyx() * d, d3);
                    }
                    case 2: 
                    case 3: {
                        return new Point3D(this.getMxx() * d, this.getMyy() * d2, d3);
                    }
                    case 0: 
                    case 1: 
                }
                return new Point3D(d, d2, d3);
            }
            case 1: {
                return new Point3D(d, d2, d3);
            }
            case 2: 
            case 3: {
                return new Point3D(this.getMxx() * d, this.getMyy() * d2, this.getMzz() * d3);
            }
            case 4: 
        }
        return new Point3D(this.getMxx() * d + this.getMxy() * d2 + this.getMxz() * d3, this.getMyx() * d + this.getMyy() * d2 + this.getMyz() * d3, this.getMzx() * d + this.getMzy() * d2 + this.getMzz() * d3);
    }

    @Override
    public Point2D inverseTransform(double d, double d2) throws NonInvertibleTransformException {
        this.ensureCanTransform2DPoint();
        switch (this.state2d) {
            default: {
                return super.inverseTransform(d, d2);
            }
            case 5: {
                double d3 = this.getMxy();
                double d4 = this.getMyx();
                if (d3 == 0.0 || d4 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point2D(1.0 / d4 * d2 - this.getTy() / d4, 1.0 / d3 * d - this.getTx() / d3);
            }
            case 4: {
                double d5 = this.getMxy();
                double d6 = this.getMyx();
                if (d5 == 0.0 || d6 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point2D(1.0 / d6 * d2, 1.0 / d5 * d);
            }
            case 3: {
                double d7 = this.getMxx();
                double d8 = this.getMyy();
                if (d7 == 0.0 || d8 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point2D(1.0 / d7 * d - this.getTx() / d7, 1.0 / d8 * d2 - this.getTy() / d8);
            }
            case 2: {
                double d9 = this.getMxx();
                double d10 = this.getMyy();
                if (d9 == 0.0 || d10 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point2D(1.0 / d9 * d, 1.0 / d10 * d2);
            }
            case 1: {
                return new Point2D(d - this.getTx(), d2 - this.getTy());
            }
            case 0: 
        }
        return new Point2D(d, d2);
    }

    @Override
    public Point3D inverseTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        return super.inverseTransform(d, d2, d3);
                    }
                    case 5: {
                        double d4 = this.getMxy();
                        double d5 = this.getMyx();
                        if (d4 == 0.0 || d5 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        return new Point3D(1.0 / d5 * d2 - this.getTy() / d5, 1.0 / d4 * d - this.getTx() / d4, d3);
                    }
                    case 4: {
                        double d6 = this.getMxy();
                        double d7 = this.getMyx();
                        if (d6 == 0.0 || d7 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        return new Point3D(1.0 / d7 * d2, 1.0 / d6 * d, d3);
                    }
                    case 3: {
                        double d8 = this.getMxx();
                        double d9 = this.getMyy();
                        if (d8 == 0.0 || d9 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        return new Point3D(1.0 / d8 * d - this.getTx() / d8, 1.0 / d9 * d2 - this.getTy() / d9, d3);
                    }
                    case 2: {
                        double d10 = this.getMxx();
                        double d11 = this.getMyy();
                        if (d10 == 0.0 || d11 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        return new Point3D(1.0 / d10 * d, 1.0 / d11 * d2, d3);
                    }
                    case 1: {
                        return new Point3D(d - this.getTx(), d2 - this.getTy(), d3);
                    }
                    case 0: 
                }
                return new Point3D(d, d2, d3);
            }
            case 1: {
                return new Point3D(d - this.getTx(), d2 - this.getTy(), d3 - this.getTz());
            }
            case 2: {
                double d12 = this.getMxx();
                double d13 = this.getMyy();
                double d14 = this.getMzz();
                if (d12 == 0.0 || d13 == 0.0 || d14 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point3D(1.0 / d12 * d, 1.0 / d13 * d2, 1.0 / d14 * d3);
            }
            case 3: {
                double d15 = this.getMxx();
                double d16 = this.getMyy();
                double d17 = this.getMzz();
                if (d15 == 0.0 || d16 == 0.0 || d17 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point3D(1.0 / d15 * d - this.getTx() / d15, 1.0 / d16 * d2 - this.getTy() / d16, 1.0 / d17 * d3 - this.getTz() / d17);
            }
            case 4: 
        }
        return super.inverseTransform(d, d2, d3);
    }

    @Override
    void inverseTransform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        switch (this.state2d) {
            default: {
                super.inverseTransform2DPointsImpl(dArray, n, dArray2, n2, n3);
                return;
            }
            case 5: {
                double d = this.getMxy();
                double d2 = this.getTx();
                double d3 = this.getMyx();
                double d4 = this.getTy();
                if (d == 0.0 || d3 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                double d5 = d2;
                d2 = -d4 / d3;
                d4 = -d5 / d;
                d5 = d3;
                d3 = 1.0 / d;
                d = 1.0 / d5;
                while (--n3 >= 0) {
                    double d6 = dArray[n++];
                    dArray2[n2++] = d * dArray[n++] + d2;
                    dArray2[n2++] = d3 * d6 + d4;
                }
                return;
            }
            case 4: {
                double d = this.getMxy();
                double d7 = this.getMyx();
                if (d == 0.0 || d7 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                double d8 = d7;
                d7 = 1.0 / d;
                d = 1.0 / d8;
                while (--n3 >= 0) {
                    double d9 = dArray[n++];
                    dArray2[n2++] = d * dArray[n++];
                    dArray2[n2++] = d7 * d9;
                }
                return;
            }
            case 3: {
                double d = this.getMxx();
                double d10 = this.getTx();
                double d11 = this.getMyy();
                double d12 = this.getTy();
                if (d == 0.0 || d11 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                d10 = -d10 / d;
                d12 = -d12 / d11;
                d = 1.0 / d;
                d11 = 1.0 / d11;
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++] + d10;
                    dArray2[n2++] = d11 * dArray[n++] + d12;
                }
                return;
            }
            case 2: {
                double d = this.getMxx();
                double d13 = this.getMyy();
                if (d == 0.0 || d13 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                d = 1.0 / d;
                d13 = 1.0 / d13;
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++];
                    dArray2[n2++] = d13 * dArray[n++];
                }
                return;
            }
            case 1: {
                double d = this.getTx();
                double d14 = this.getTy();
                while (--n3 >= 0) {
                    dArray2[n2++] = dArray[n++] - d;
                    dArray2[n2++] = dArray[n++] - d14;
                }
                return;
            }
            case 0: 
        }
        if (dArray != dArray2 || n != n2) {
            System.arraycopy(dArray, n, dArray2, n2, n3 * 2);
        }
    }

    @Override
    void inverseTransform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        super.inverseTransform3DPointsImpl(dArray, n, dArray2, n2, n3);
                        return;
                    }
                    case 5: {
                        double d = this.getMxy();
                        double d2 = this.getTx();
                        double d3 = this.getMyx();
                        double d4 = this.getTy();
                        if (d == 0.0 || d3 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        double d5 = d2;
                        d2 = -d4 / d3;
                        d4 = -d5 / d;
                        d5 = d3;
                        d3 = 1.0 / d;
                        d = 1.0 / d5;
                        while (--n3 >= 0) {
                            double d6 = dArray[n++];
                            dArray2[n2++] = d * dArray[n++] + d2;
                            dArray2[n2++] = d3 * d6 + d4;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 4: {
                        double d = this.getMxy();
                        double d7 = this.getMyx();
                        if (d == 0.0 || d7 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        double d8 = d7;
                        d7 = 1.0 / d;
                        d = 1.0 / d8;
                        while (--n3 >= 0) {
                            double d9 = dArray[n++];
                            dArray2[n2++] = d * dArray[n++];
                            dArray2[n2++] = d7 * d9;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 3: {
                        double d = this.getMxx();
                        double d10 = this.getTx();
                        double d11 = this.getMyy();
                        double d12 = this.getTy();
                        if (d == 0.0 || d11 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        d10 = -d10 / d;
                        d12 = -d12 / d11;
                        d = 1.0 / d;
                        d11 = 1.0 / d11;
                        while (--n3 >= 0) {
                            dArray2[n2++] = d * dArray[n++] + d10;
                            dArray2[n2++] = d11 * dArray[n++] + d12;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 2: {
                        double d = this.getMxx();
                        double d13 = this.getMyy();
                        if (d == 0.0 || d13 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        d = 1.0 / d;
                        d13 = 1.0 / d13;
                        while (--n3 >= 0) {
                            dArray2[n2++] = d * dArray[n++];
                            dArray2[n2++] = d13 * dArray[n++];
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 1: {
                        double d = this.getTx();
                        double d14 = this.getTy();
                        while (--n3 >= 0) {
                            dArray2[n2++] = dArray[n++] - d;
                            dArray2[n2++] = dArray[n++] - d14;
                            dArray2[n2++] = dArray[n++];
                        }
                        return;
                    }
                    case 0: 
                }
                if (dArray != dArray2 || n != n2) {
                    System.arraycopy(dArray, n, dArray2, n2, n3 * 3);
                }
                return;
            }
            case 1: {
                double d = this.getTx();
                double d15 = this.getTy();
                double d16 = this.getTz();
                while (--n3 >= 0) {
                    dArray2[n2++] = dArray[n++] - d;
                    dArray2[n2++] = dArray[n++] - d15;
                    dArray2[n2++] = dArray[n++] - d16;
                }
                return;
            }
            case 2: {
                double d = this.getMxx();
                double d17 = this.getMyy();
                double d18 = this.getMzz();
                if (d == 0.0 || d17 == 0.0 | d18 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                d = 1.0 / d;
                d17 = 1.0 / d17;
                d18 = 1.0 / d18;
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++];
                    dArray2[n2++] = d17 * dArray[n++];
                    dArray2[n2++] = d18 * dArray[n++];
                }
                return;
            }
            case 3: {
                double d = this.getMxx();
                double d19 = this.getTx();
                double d20 = this.getMyy();
                double d21 = this.getTy();
                double d22 = this.getMzz();
                double d23 = this.getTz();
                if (d == 0.0 || d20 == 0.0 || d22 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                d19 = -d19 / d;
                d21 = -d21 / d20;
                d23 = -d23 / d22;
                d = 1.0 / d;
                d20 = 1.0 / d20;
                d22 = 1.0 / d22;
                while (--n3 >= 0) {
                    dArray2[n2++] = d * dArray[n++] + d19;
                    dArray2[n2++] = d20 * dArray[n++] + d21;
                    dArray2[n2++] = d22 * dArray[n++] + d23;
                }
                return;
            }
            case 4: 
        }
        super.inverseTransform3DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    @Override
    public Point2D inverseDeltaTransform(double d, double d2) throws NonInvertibleTransformException {
        this.ensureCanTransform2DPoint();
        switch (this.state2d) {
            default: {
                return super.inverseDeltaTransform(d, d2);
            }
            case 4: 
            case 5: {
                double d3 = this.getMxy();
                double d4 = this.getMyx();
                if (d3 == 0.0 || d4 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point2D(1.0 / d4 * d2, 1.0 / d3 * d);
            }
            case 2: 
            case 3: {
                double d5 = this.getMxx();
                double d6 = this.getMyy();
                if (d5 == 0.0 || d6 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point2D(1.0 / d5 * d, 1.0 / d6 * d2);
            }
            case 0: 
            case 1: 
        }
        return new Point2D(d, d2);
    }

    @Override
    public Point3D inverseDeltaTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    default: {
                        return super.inverseDeltaTransform(d, d2, d3);
                    }
                    case 4: 
                    case 5: {
                        double d4 = this.getMxy();
                        double d5 = this.getMyx();
                        if (d4 == 0.0 || d5 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        return new Point3D(1.0 / d5 * d2, 1.0 / d4 * d, d3);
                    }
                    case 2: 
                    case 3: {
                        double d6 = this.getMxx();
                        double d7 = this.getMyy();
                        if (d6 == 0.0 || d7 == 0.0) {
                            throw new NonInvertibleTransformException("Determinant is 0");
                        }
                        return new Point3D(1.0 / d6 * d, 1.0 / d7 * d2, d3);
                    }
                    case 0: 
                    case 1: 
                }
                return new Point3D(d, d2, d3);
            }
            case 1: {
                return new Point3D(d, d2, d3);
            }
            case 2: 
            case 3: {
                double d8 = this.getMxx();
                double d9 = this.getMyy();
                double d10 = this.getMzz();
                if (d8 == 0.0 || d9 == 0.0 || d10 == 0.0) {
                    throw new NonInvertibleTransformException("Determinant is 0");
                }
                return new Point3D(1.0 / d8 * d, 1.0 / d9 * d2, 1.0 / d10 * d3);
            }
            case 4: 
        }
        return super.inverseDeltaTransform(d, d2, d3);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Affine [\n");
        stringBuilder.append("\t").append(this.getMxx());
        stringBuilder.append(", ").append(this.getMxy());
        stringBuilder.append(", ").append(this.getMxz());
        stringBuilder.append(", ").append(this.getTx());
        stringBuilder.append('\n');
        stringBuilder.append("\t").append(this.getMyx());
        stringBuilder.append(", ").append(this.getMyy());
        stringBuilder.append(", ").append(this.getMyz());
        stringBuilder.append(", ").append(this.getTy());
        stringBuilder.append('\n');
        stringBuilder.append("\t").append(this.getMzx());
        stringBuilder.append(", ").append(this.getMzy());
        stringBuilder.append(", ").append(this.getMzz());
        stringBuilder.append(", ").append(this.getTz());
        return stringBuilder.append("\n]").toString();
    }

    private void updateState() {
        this.updateState2D();
        this.state3d = 0;
        if (this.getMxz() != 0.0 || this.getMyz() != 0.0 || this.getMzx() != 0.0 || this.getMzy() != 0.0) {
            this.state3d = 4;
        } else if ((this.state2d & 4) == 0) {
            if (this.getTz() != 0.0) {
                this.state3d |= 1;
            }
            if (this.getMzz() != 1.0) {
                this.state3d |= 2;
            }
            if (this.state3d != 0) {
                this.state3d |= this.state2d & 3;
            }
        } else if (this.getMzz() != 1.0 || this.getTz() != 0.0) {
            this.state3d = 4;
        }
    }

    private void updateState2D() {
        this.state2d = this.getMxy() == 0.0 && this.getMyx() == 0.0 ? (this.getMxx() == 1.0 && this.getMyy() == 1.0 ? (this.getTx() == 0.0 && this.getTy() == 0.0 ? 0 : 1) : (this.getTx() == 0.0 && this.getTy() == 0.0 ? 2 : 3)) : (this.getMxx() == 0.0 && this.getMyy() == 0.0 ? (this.getTx() == 0.0 && this.getTy() == 0.0 ? 4 : 5) : (this.getTx() == 0.0 && this.getTy() == 0.0 ? 6 : 7));
    }

    private static void stateError() {
        throw new InternalError("missing case in a switch");
    }

    @Override
    void apply(Affine3D affine3D) {
        affine3D.concatenate(this.getMxx(), this.getMxy(), this.getMxz(), this.getTx(), this.getMyx(), this.getMyy(), this.getMyz(), this.getTy(), this.getMzx(), this.getMzy(), this.getMzz(), this.getTz());
    }

    @Override
    BaseTransform derive(BaseTransform baseTransform) {
        switch (this.state3d) {
            default: {
                Affine.stateError();
            }
            case 0: {
                switch (this.state2d) {
                    case 0: {
                        return baseTransform;
                    }
                    case 1: {
                        return baseTransform.deriveWithTranslation(this.getTx(), this.getTy());
                    }
                    case 2: {
                        return baseTransform.deriveWithScale(this.getMxx(), this.getMyy(), 1.0);
                    }
                }
                return baseTransform.deriveWithConcatenation(this.getMxx(), this.getMyx(), this.getMxy(), this.getMyy(), this.getTx(), this.getTy());
            }
            case 1: {
                return baseTransform.deriveWithTranslation(this.getTx(), this.getTy(), this.getTz());
            }
            case 2: {
                return baseTransform.deriveWithScale(this.getMxx(), this.getMyy(), this.getMzz());
            }
            case 3: 
            case 4: 
        }
        return baseTransform.deriveWithConcatenation(this.getMxx(), this.getMxy(), this.getMxz(), this.getTx(), this.getMyx(), this.getMyy(), this.getMyz(), this.getTy(), this.getMzx(), this.getMzy(), this.getMzz(), this.getTz());
    }

    int getState2d() {
        return this.state2d;
    }

    int getState3d() {
        return this.state3d;
    }

    boolean atomicChangeRuns() {
        return this.atomicChange.runs();
    }

    private class AffineAtomicChange {
        private boolean running = false;

        private AffineAtomicChange() {
        }

        private void start() {
            if (this.running) {
                throw new InternalError("Affine internal error: trying to run inner atomic operation");
            }
            if (Affine.this.mxx != null) {
                Affine.this.mxx.preProcessAtomicChange();
            }
            if (Affine.this.mxy != null) {
                Affine.this.mxy.preProcessAtomicChange();
            }
            if (Affine.this.mxz != null) {
                Affine.this.mxz.preProcessAtomicChange();
            }
            if (Affine.this.tx != null) {
                Affine.this.tx.preProcessAtomicChange();
            }
            if (Affine.this.myx != null) {
                Affine.this.myx.preProcessAtomicChange();
            }
            if (Affine.this.myy != null) {
                Affine.this.myy.preProcessAtomicChange();
            }
            if (Affine.this.myz != null) {
                Affine.this.myz.preProcessAtomicChange();
            }
            if (Affine.this.ty != null) {
                Affine.this.ty.preProcessAtomicChange();
            }
            if (Affine.this.mzx != null) {
                Affine.this.mzx.preProcessAtomicChange();
            }
            if (Affine.this.mzy != null) {
                Affine.this.mzy.preProcessAtomicChange();
            }
            if (Affine.this.mzz != null) {
                Affine.this.mzz.preProcessAtomicChange();
            }
            if (Affine.this.tz != null) {
                Affine.this.tz.preProcessAtomicChange();
            }
            this.running = true;
        }

        private void end() {
            this.running = false;
            Affine.this.transformChanged();
            if (Affine.this.mxx != null) {
                Affine.this.mxx.postProcessAtomicChange();
            }
            if (Affine.this.mxy != null) {
                Affine.this.mxy.postProcessAtomicChange();
            }
            if (Affine.this.mxz != null) {
                Affine.this.mxz.postProcessAtomicChange();
            }
            if (Affine.this.tx != null) {
                Affine.this.tx.postProcessAtomicChange();
            }
            if (Affine.this.myx != null) {
                Affine.this.myx.postProcessAtomicChange();
            }
            if (Affine.this.myy != null) {
                Affine.this.myy.postProcessAtomicChange();
            }
            if (Affine.this.myz != null) {
                Affine.this.myz.postProcessAtomicChange();
            }
            if (Affine.this.ty != null) {
                Affine.this.ty.postProcessAtomicChange();
            }
            if (Affine.this.mzx != null) {
                Affine.this.mzx.postProcessAtomicChange();
            }
            if (Affine.this.mzy != null) {
                Affine.this.mzy.postProcessAtomicChange();
            }
            if (Affine.this.mzz != null) {
                Affine.this.mzz.postProcessAtomicChange();
            }
            if (Affine.this.tz != null) {
                Affine.this.tz.postProcessAtomicChange();
            }
        }

        private void cancel() {
            this.running = false;
        }

        private boolean runs() {
            return this.running;
        }
    }

    private class AffineElementProperty
    extends SimpleDoubleProperty {
        private boolean needsValueChangedEvent;
        private double oldValue;

        public AffineElementProperty(double d) {
            super(d);
            this.needsValueChangedEvent = false;
        }

        @Override
        public void invalidated() {
            if (!Affine.this.atomicChange.runs()) {
                Affine.this.updateState();
                Affine.this.transformChanged();
            }
        }

        @Override
        protected void fireValueChangedEvent() {
            if (!Affine.this.atomicChange.runs()) {
                super.fireValueChangedEvent();
            } else {
                this.needsValueChangedEvent = true;
            }
        }

        private void preProcessAtomicChange() {
            this.oldValue = this.get();
        }

        private void postProcessAtomicChange() {
            if (this.needsValueChangedEvent) {
                this.needsValueChangedEvent = false;
                if (this.oldValue != this.get()) {
                    super.fireValueChangedEvent();
                }
            }
        }
    }
}

