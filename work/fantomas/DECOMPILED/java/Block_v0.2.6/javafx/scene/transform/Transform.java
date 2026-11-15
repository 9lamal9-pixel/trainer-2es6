/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.transform;

import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.geometry.BoundsUtils;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.transform.TransformHelper;
import com.sun.javafx.scene.transform.TransformUtils;
import com.sun.javafx.util.WeakReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Shear;
import javafx.scene.transform.TransformChangedEvent;
import javafx.scene.transform.Translate;

public abstract class Transform
implements Cloneable,
EventTarget {
    private SoftReference<Transform> inverseCache = null;
    private WeakReferenceQueue nodes = new WeakReferenceQueue();
    private LazyBooleanProperty type2D;
    private LazyBooleanProperty identity;
    private EventHandlerManager internalEventDispatcher;
    private ObjectProperty<EventHandler<? super TransformChangedEvent>> onTransformChanged;

    public static Affine affine(double d, double d2, double d3, double d4, double d5, double d6) {
        Affine affine = new Affine();
        affine.setMxx(d);
        affine.setMxy(d3);
        affine.setTx(d5);
        affine.setMyx(d2);
        affine.setMyy(d4);
        affine.setTy(d6);
        return affine;
    }

    public static Affine affine(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        Affine affine = new Affine();
        affine.setMxx(d);
        affine.setMxy(d2);
        affine.setMxz(d3);
        affine.setTx(d4);
        affine.setMyx(d5);
        affine.setMyy(d6);
        affine.setMyz(d7);
        affine.setTy(d8);
        affine.setMzx(d9);
        affine.setMzy(d10);
        affine.setMzz(d11);
        affine.setTz(d12);
        return affine;
    }

    public static Translate translate(double d, double d2) {
        Translate translate = new Translate();
        translate.setX(d);
        translate.setY(d2);
        return translate;
    }

    public static Rotate rotate(double d, double d2, double d3) {
        Rotate rotate = new Rotate();
        rotate.setAngle(d);
        rotate.setPivotX(d2);
        rotate.setPivotY(d3);
        return rotate;
    }

    public static Scale scale(double d, double d2) {
        Scale scale = new Scale();
        scale.setX(d);
        scale.setY(d2);
        return scale;
    }

    public static Scale scale(double d, double d2, double d3, double d4) {
        Scale scale = new Scale();
        scale.setX(d);
        scale.setY(d2);
        scale.setPivotX(d3);
        scale.setPivotY(d4);
        return scale;
    }

    public static Shear shear(double d, double d2) {
        Shear shear = new Shear();
        shear.setX(d);
        shear.setY(d2);
        return shear;
    }

    public static Shear shear(double d, double d2, double d3, double d4) {
        Shear shear = new Shear();
        shear.setX(d);
        shear.setY(d2);
        shear.setPivotX(d3);
        shear.setPivotY(d4);
        return shear;
    }

    public double getMxx() {
        return 1.0;
    }

    public double getMxy() {
        return 0.0;
    }

    public double getMxz() {
        return 0.0;
    }

    public double getTx() {
        return 0.0;
    }

    public double getMyx() {
        return 0.0;
    }

    public double getMyy() {
        return 1.0;
    }

    public double getMyz() {
        return 0.0;
    }

    public double getTy() {
        return 0.0;
    }

    public double getMzx() {
        return 0.0;
    }

    public double getMzy() {
        return 0.0;
    }

    public double getMzz() {
        return 1.0;
    }

    public double getTz() {
        return 0.0;
    }

    public double getElement(MatrixType matrixType, int n, int n2) {
        if (n < 0 || n >= matrixType.rows() || n2 < 0 || n2 >= matrixType.columns()) {
            throw new IndexOutOfBoundsException("Index outside of affine matrix " + matrixType + ": [" + n + ", " + n2 + "]");
        }
        switch (matrixType) {
            case MT_2D_2x3: 
            case MT_2D_3x3: {
                if (!this.isType2D()) {
                    throw new IllegalArgumentException("Cannot access 2D matrix of a 3D transform");
                }
                switch (n) {
                    case 0: {
                        switch (n2) {
                            case 0: {
                                return this.getMxx();
                            }
                            case 1: {
                                return this.getMxy();
                            }
                            case 2: {
                                return this.getTx();
                            }
                        }
                    }
                    case 1: {
                        switch (n2) {
                            case 0: {
                                return this.getMyx();
                            }
                            case 1: {
                                return this.getMyy();
                            }
                            case 2: {
                                return this.getTy();
                            }
                        }
                    }
                    case 2: {
                        switch (n2) {
                            case 0: {
                                return 0.0;
                            }
                            case 1: {
                                return 0.0;
                            }
                            case 2: {
                                return 1.0;
                            }
                        }
                    }
                }
                break;
            }
            case MT_3D_3x4: 
            case MT_3D_4x4: {
                switch (n) {
                    case 0: {
                        switch (n2) {
                            case 0: {
                                return this.getMxx();
                            }
                            case 1: {
                                return this.getMxy();
                            }
                            case 2: {
                                return this.getMxz();
                            }
                            case 3: {
                                return this.getTx();
                            }
                        }
                    }
                    case 1: {
                        switch (n2) {
                            case 0: {
                                return this.getMyx();
                            }
                            case 1: {
                                return this.getMyy();
                            }
                            case 2: {
                                return this.getMyz();
                            }
                            case 3: {
                                return this.getTy();
                            }
                        }
                    }
                    case 2: {
                        switch (n2) {
                            case 0: {
                                return this.getMzx();
                            }
                            case 1: {
                                return this.getMzy();
                            }
                            case 2: {
                                return this.getMzz();
                            }
                            case 3: {
                                return this.getTz();
                            }
                        }
                    }
                    case 3: {
                        switch (n2) {
                            case 0: {
                                return 0.0;
                            }
                            case 1: {
                                return 0.0;
                            }
                            case 2: {
                                return 0.0;
                            }
                            case 3: {
                                return 1.0;
                            }
                        }
                    }
                }
            }
        }
        throw new InternalError("Unsupported matrix type " + matrixType);
    }

    boolean computeIs2D() {
        return this.getMxz() == 0.0 && this.getMzx() == 0.0 && this.getMzy() == 0.0 && this.getMzz() == 1.0 && this.getTz() == 0.0;
    }

    boolean computeIsIdentity() {
        return this.getMxx() == 1.0 && this.getMxy() == 0.0 && this.getMxz() == 0.0 && this.getTx() == 0.0 && this.getMyx() == 0.0 && this.getMyy() == 1.0 && this.getMyz() == 0.0 && this.getTy() == 0.0 && this.getMzx() == 0.0 && this.getMzy() == 0.0 && this.getMzz() == 1.0 && this.getTz() == 0.0;
    }

    public double determinant() {
        double d = this.getMyx();
        double d2 = this.getMyy();
        double d3 = this.getMyz();
        double d4 = this.getMzx();
        double d5 = this.getMzy();
        double d6 = this.getMzz();
        return this.getMxx() * (d2 * d6 - d5 * d3) + this.getMxy() * (d3 * d4 - d6 * d) + this.getMxz() * (d * d5 - d4 * d2);
    }

    public final boolean isType2D() {
        return this.type2D == null ? this.computeIs2D() : this.type2D.get();
    }

    public final ReadOnlyBooleanProperty type2DProperty() {
        if (this.type2D == null) {
            this.type2D = new LazyBooleanProperty(){

                @Override
                protected boolean computeValue() {
                    return Transform.this.computeIs2D();
                }

                @Override
                public Object getBean() {
                    return Transform.this;
                }

                @Override
                public String getName() {
                    return "type2D";
                }
            };
        }
        return this.type2D;
    }

    public final boolean isIdentity() {
        return this.identity == null ? this.computeIsIdentity() : this.identity.get();
    }

    public final ReadOnlyBooleanProperty identityProperty() {
        if (this.identity == null) {
            this.identity = new LazyBooleanProperty(){

                @Override
                protected boolean computeValue() {
                    return Transform.this.computeIsIdentity();
                }

                @Override
                public Object getBean() {
                    return Transform.this;
                }

                @Override
                public String getName() {
                    return "identity";
                }
            };
        }
        return this.identity;
    }

    private double transformDiff(Transform transform, double d, double d2) {
        Point2D point2D = this.transform(d, d2);
        Point2D point2D2 = transform.transform(d, d2);
        return point2D.distance(point2D2);
    }

    private double transformDiff(Transform transform, double d, double d2, double d3) {
        Point3D point3D = this.transform(d, d2, d3);
        Point3D point3D2 = transform.transform(d, d2, d3);
        return point3D.distance(point3D2);
    }

    public boolean similarTo(Transform transform, Bounds bounds, double d) {
        double d2;
        double d3;
        if (this.isType2D() && transform.isType2D()) {
            double d4;
            double d5 = bounds.getMinX();
            if (this.transformDiff(transform, d5, d4 = bounds.getMinY()) > d) {
                return false;
            }
            d4 = bounds.getMaxY();
            if (this.transformDiff(transform, d5, d4) > d) {
                return false;
            }
            d5 = bounds.getMaxX();
            if (this.transformDiff(transform, d5, d4 = bounds.getMinY()) > d) {
                return false;
            }
            d4 = bounds.getMaxY();
            return !(this.transformDiff(transform, d5, d4) > d);
        }
        double d6 = bounds.getMinX();
        if (this.transformDiff(transform, d6, d3 = bounds.getMinY(), d2 = bounds.getMinZ()) > d) {
            return false;
        }
        d3 = bounds.getMaxY();
        if (this.transformDiff(transform, d6, d3, d2) > d) {
            return false;
        }
        d6 = bounds.getMaxX();
        if (this.transformDiff(transform, d6, d3 = bounds.getMinY(), d2) > d) {
            return false;
        }
        d3 = bounds.getMaxY();
        if (this.transformDiff(transform, d6, d3, d2) > d) {
            return false;
        }
        if (bounds.getDepth() != 0.0) {
            d6 = bounds.getMinX();
            if (this.transformDiff(transform, d6, d3 = bounds.getMinY(), d2 = bounds.getMaxZ()) > d) {
                return false;
            }
            d3 = bounds.getMaxY();
            if (this.transformDiff(transform, d6, d3, d2) > d) {
                return false;
            }
            d6 = bounds.getMaxX();
            if (this.transformDiff(transform, d6, d3 = bounds.getMinY(), d2) > d) {
                return false;
            }
            d3 = bounds.getMaxY();
            if (this.transformDiff(transform, d6, d3, d2) > d) {
                return false;
            }
        }
        return true;
    }

    void fill2DArray(double[] dArray) {
        dArray[0] = this.getMxx();
        dArray[1] = this.getMxy();
        dArray[2] = this.getTx();
        dArray[3] = this.getMyx();
        dArray[4] = this.getMyy();
        dArray[5] = this.getTy();
    }

    void fill3DArray(double[] dArray) {
        dArray[0] = this.getMxx();
        dArray[1] = this.getMxy();
        dArray[2] = this.getMxz();
        dArray[3] = this.getTx();
        dArray[4] = this.getMyx();
        dArray[5] = this.getMyy();
        dArray[6] = this.getMyz();
        dArray[7] = this.getTy();
        dArray[8] = this.getMzx();
        dArray[9] = this.getMzy();
        dArray[10] = this.getMzz();
        dArray[11] = this.getTz();
    }

    public double[] toArray(MatrixType matrixType, double[] dArray) {
        this.checkRequestedMAT(matrixType);
        if (dArray == null || dArray.length < matrixType.elements()) {
            dArray = new double[matrixType.elements()];
        }
        switch (matrixType) {
            case MT_2D_3x3: {
                dArray[6] = 0.0;
                dArray[7] = 0.0;
                dArray[8] = 1.0;
            }
            case MT_2D_2x3: {
                this.fill2DArray(dArray);
                break;
            }
            case MT_3D_4x4: {
                dArray[12] = 0.0;
                dArray[13] = 0.0;
                dArray[14] = 0.0;
                dArray[15] = 1.0;
            }
            case MT_3D_3x4: {
                this.fill3DArray(dArray);
                break;
            }
            default: {
                throw new InternalError("Unsupported matrix type " + matrixType);
            }
        }
        return dArray;
    }

    public double[] toArray(MatrixType matrixType) {
        return this.toArray(matrixType, null);
    }

    public double[] row(MatrixType matrixType, int n, double[] dArray) {
        this.checkRequestedMAT(matrixType);
        if (n < 0 || n >= matrixType.rows()) {
            throw new IndexOutOfBoundsException("Cannot get row " + n + " from " + matrixType);
        }
        if (dArray == null || dArray.length < matrixType.columns()) {
            dArray = new double[matrixType.columns()];
        }
        switch (matrixType) {
            case MT_2D_2x3: 
            case MT_2D_3x3: {
                switch (n) {
                    case 0: {
                        dArray[0] = this.getMxx();
                        dArray[1] = this.getMxy();
                        dArray[2] = this.getTx();
                        break;
                    }
                    case 1: {
                        dArray[0] = this.getMyx();
                        dArray[1] = this.getMyy();
                        dArray[2] = this.getTy();
                        break;
                    }
                    case 2: {
                        dArray[0] = 0.0;
                        dArray[1] = 0.0;
                        dArray[2] = 1.0;
                    }
                }
                break;
            }
            case MT_3D_3x4: 
            case MT_3D_4x4: {
                switch (n) {
                    case 0: {
                        dArray[0] = this.getMxx();
                        dArray[1] = this.getMxy();
                        dArray[2] = this.getMxz();
                        dArray[3] = this.getTx();
                        break;
                    }
                    case 1: {
                        dArray[0] = this.getMyx();
                        dArray[1] = this.getMyy();
                        dArray[2] = this.getMyz();
                        dArray[3] = this.getTy();
                        break;
                    }
                    case 2: {
                        dArray[0] = this.getMzx();
                        dArray[1] = this.getMzy();
                        dArray[2] = this.getMzz();
                        dArray[3] = this.getTz();
                        break;
                    }
                    case 3: {
                        dArray[0] = 0.0;
                        dArray[1] = 0.0;
                        dArray[2] = 0.0;
                        dArray[3] = 1.0;
                    }
                }
                break;
            }
            default: {
                throw new InternalError("Unsupported row " + n + " of " + matrixType);
            }
        }
        return dArray;
    }

    public double[] row(MatrixType matrixType, int n) {
        return this.row(matrixType, n, null);
    }

    public double[] column(MatrixType matrixType, int n, double[] dArray) {
        this.checkRequestedMAT(matrixType);
        if (n < 0 || n >= matrixType.columns()) {
            throw new IndexOutOfBoundsException("Cannot get row " + n + " from " + matrixType);
        }
        if (dArray == null || dArray.length < matrixType.rows()) {
            dArray = new double[matrixType.rows()];
        }
        switch (matrixType) {
            case MT_2D_2x3: {
                switch (n) {
                    case 0: {
                        dArray[0] = this.getMxx();
                        dArray[1] = this.getMyx();
                        break;
                    }
                    case 1: {
                        dArray[0] = this.getMxy();
                        dArray[1] = this.getMyy();
                        break;
                    }
                    case 2: {
                        dArray[0] = this.getTx();
                        dArray[1] = this.getTy();
                    }
                }
                break;
            }
            case MT_2D_3x3: {
                switch (n) {
                    case 0: {
                        dArray[0] = this.getMxx();
                        dArray[1] = this.getMyx();
                        dArray[2] = 0.0;
                        break;
                    }
                    case 1: {
                        dArray[0] = this.getMxy();
                        dArray[1] = this.getMyy();
                        dArray[2] = 0.0;
                        break;
                    }
                    case 2: {
                        dArray[0] = this.getTx();
                        dArray[1] = this.getTy();
                        dArray[2] = 1.0;
                    }
                }
                break;
            }
            case MT_3D_3x4: {
                switch (n) {
                    case 0: {
                        dArray[0] = this.getMxx();
                        dArray[1] = this.getMyx();
                        dArray[2] = this.getMzx();
                        break;
                    }
                    case 1: {
                        dArray[0] = this.getMxy();
                        dArray[1] = this.getMyy();
                        dArray[2] = this.getMzy();
                        break;
                    }
                    case 2: {
                        dArray[0] = this.getMxz();
                        dArray[1] = this.getMyz();
                        dArray[2] = this.getMzz();
                        break;
                    }
                    case 3: {
                        dArray[0] = this.getTx();
                        dArray[1] = this.getTy();
                        dArray[2] = this.getTz();
                    }
                }
                break;
            }
            case MT_3D_4x4: {
                switch (n) {
                    case 0: {
                        dArray[0] = this.getMxx();
                        dArray[1] = this.getMyx();
                        dArray[2] = this.getMzx();
                        dArray[3] = 0.0;
                        break;
                    }
                    case 1: {
                        dArray[0] = this.getMxy();
                        dArray[1] = this.getMyy();
                        dArray[2] = this.getMzy();
                        dArray[3] = 0.0;
                        break;
                    }
                    case 2: {
                        dArray[0] = this.getMxz();
                        dArray[1] = this.getMyz();
                        dArray[2] = this.getMzz();
                        dArray[3] = 0.0;
                        break;
                    }
                    case 3: {
                        dArray[0] = this.getTx();
                        dArray[1] = this.getTy();
                        dArray[2] = this.getTz();
                        dArray[3] = 1.0;
                    }
                }
                break;
            }
            default: {
                throw new InternalError("Unsupported column " + n + " of " + matrixType);
            }
        }
        return dArray;
    }

    public double[] column(MatrixType matrixType, int n) {
        return this.column(matrixType, n, null);
    }

    public Transform createConcatenation(Transform transform) {
        double d = transform.getMxx();
        double d2 = transform.getMxy();
        double d3 = transform.getMxz();
        double d4 = transform.getTx();
        double d5 = transform.getMyx();
        double d6 = transform.getMyy();
        double d7 = transform.getMyz();
        double d8 = transform.getTy();
        double d9 = transform.getMzx();
        double d10 = transform.getMzy();
        double d11 = transform.getMzz();
        double d12 = transform.getTz();
        return new Affine(this.getMxx() * d + this.getMxy() * d5 + this.getMxz() * d9, this.getMxx() * d2 + this.getMxy() * d6 + this.getMxz() * d10, this.getMxx() * d3 + this.getMxy() * d7 + this.getMxz() * d11, this.getMxx() * d4 + this.getMxy() * d8 + this.getMxz() * d12 + this.getTx(), this.getMyx() * d + this.getMyy() * d5 + this.getMyz() * d9, this.getMyx() * d2 + this.getMyy() * d6 + this.getMyz() * d10, this.getMyx() * d3 + this.getMyy() * d7 + this.getMyz() * d11, this.getMyx() * d4 + this.getMyy() * d8 + this.getMyz() * d12 + this.getTy(), this.getMzx() * d + this.getMzy() * d5 + this.getMzz() * d9, this.getMzx() * d2 + this.getMzy() * d6 + this.getMzz() * d10, this.getMzx() * d3 + this.getMzy() * d7 + this.getMzz() * d11, this.getMzx() * d4 + this.getMzy() * d8 + this.getMzz() * d12 + this.getTz());
    }

    public Transform createInverse() throws NonInvertibleTransformException {
        return this.getInverseCache().clone();
    }

    public Transform clone() {
        return TransformUtils.immutableTransform(this);
    }

    public Point2D transform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        return new Point2D(this.getMxx() * d + this.getMxy() * d2 + this.getTx(), this.getMyx() * d + this.getMyy() * d2 + this.getTy());
    }

    public Point2D transform(Point2D point2D) {
        return this.transform(point2D.getX(), point2D.getY());
    }

    public Point3D transform(double d, double d2, double d3) {
        return new Point3D(this.getMxx() * d + this.getMxy() * d2 + this.getMxz() * d3 + this.getTx(), this.getMyx() * d + this.getMyy() * d2 + this.getMyz() * d3 + this.getTy(), this.getMzx() * d + this.getMzy() * d2 + this.getMzz() * d3 + this.getTz());
    }

    public Point3D transform(Point3D point3D) {
        return this.transform(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Bounds transform(Bounds bounds) {
        if (this.isType2D() && bounds.getMinZ() == 0.0 && bounds.getMaxZ() == 0.0) {
            Point2D point2D = this.transform(bounds.getMinX(), bounds.getMinY());
            Point2D point2D2 = this.transform(bounds.getMaxX(), bounds.getMinY());
            Point2D point2D3 = this.transform(bounds.getMaxX(), bounds.getMaxY());
            Point2D point2D4 = this.transform(bounds.getMinX(), bounds.getMaxY());
            return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
        }
        Point3D point3D = this.transform(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D2 = this.transform(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ());
        Point3D point3D3 = this.transform(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D4 = this.transform(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D5 = this.transform(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D6 = this.transform(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D7 = this.transform(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D8 = this.transform(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ());
        return BoundsUtils.createBoundingBox(point3D, point3D2, point3D3, point3D4, point3D5, point3D6, point3D7, point3D8);
    }

    void transform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
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
    }

    void transform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        double d = this.getMxx();
        double d2 = this.getMxy();
        double d3 = this.getMxz();
        double d4 = this.getTx();
        double d5 = this.getMyx();
        double d6 = this.getMyy();
        double d7 = this.getMyz();
        double d8 = this.getTy();
        double d9 = this.getMzx();
        double d10 = this.getMzy();
        double d11 = this.getMzz();
        double d12 = this.getTz();
        while (--n3 >= 0) {
            double d13 = dArray[n++];
            double d14 = dArray[n++];
            double d15 = dArray[n++];
            dArray2[n2++] = d * d13 + d2 * d14 + d3 * d15 + d4;
            dArray2[n2++] = d5 * d13 + d6 * d14 + d7 * d15 + d8;
            dArray2[n2++] = d9 * d13 + d10 * d14 + d11 * d15 + d12;
        }
    }

    public void transform2DPoints(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        if (dArray == null || dArray2 == null) {
            throw new NullPointerException();
        }
        if (!this.isType2D()) {
            throw new IllegalStateException("Cannot transform 2D points with a 3D transform");
        }
        n = this.getFixedSrcOffset(dArray, n, dArray2, n2, n3, 2);
        this.transform2DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    public void transform3DPoints(double[] dArray, int n, double[] dArray2, int n2, int n3) {
        if (dArray == null || dArray2 == null) {
            throw new NullPointerException();
        }
        n = this.getFixedSrcOffset(dArray, n, dArray2, n2, n3, 3);
        this.transform3DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    public Point2D deltaTransform(double d, double d2) {
        this.ensureCanTransform2DPoint();
        return new Point2D(this.getMxx() * d + this.getMxy() * d2, this.getMyx() * d + this.getMyy() * d2);
    }

    public Point2D deltaTransform(Point2D point2D) {
        return this.deltaTransform(point2D.getX(), point2D.getY());
    }

    public Point3D deltaTransform(double d, double d2, double d3) {
        return new Point3D(this.getMxx() * d + this.getMxy() * d2 + this.getMxz() * d3, this.getMyx() * d + this.getMyy() * d2 + this.getMyz() * d3, this.getMzx() * d + this.getMzy() * d2 + this.getMzz() * d3);
    }

    public Point3D deltaTransform(Point3D point3D) {
        return this.deltaTransform(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point2D inverseTransform(double d, double d2) throws NonInvertibleTransformException {
        this.ensureCanTransform2DPoint();
        return this.getInverseCache().transform(d, d2);
    }

    public Point2D inverseTransform(Point2D point2D) throws NonInvertibleTransformException {
        return this.inverseTransform(point2D.getX(), point2D.getY());
    }

    public Point3D inverseTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        return this.getInverseCache().transform(d, d2, d3);
    }

    public Point3D inverseTransform(Point3D point3D) throws NonInvertibleTransformException {
        return this.inverseTransform(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Bounds inverseTransform(Bounds bounds) throws NonInvertibleTransformException {
        if (this.isType2D() && bounds.getMinZ() == 0.0 && bounds.getMaxZ() == 0.0) {
            Point2D point2D = this.inverseTransform(bounds.getMinX(), bounds.getMinY());
            Point2D point2D2 = this.inverseTransform(bounds.getMaxX(), bounds.getMinY());
            Point2D point2D3 = this.inverseTransform(bounds.getMaxX(), bounds.getMaxY());
            Point2D point2D4 = this.inverseTransform(bounds.getMinX(), bounds.getMaxY());
            return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
        }
        Point3D point3D = this.inverseTransform(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D2 = this.inverseTransform(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ());
        Point3D point3D3 = this.inverseTransform(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D4 = this.inverseTransform(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D5 = this.inverseTransform(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D6 = this.inverseTransform(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D7 = this.inverseTransform(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D8 = this.inverseTransform(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ());
        return BoundsUtils.createBoundingBox(point3D, point3D2, point3D3, point3D4, point3D5, point3D6, point3D7, point3D8);
    }

    void inverseTransform2DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        this.getInverseCache().transform2DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    void inverseTransform3DPointsImpl(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        this.getInverseCache().transform3DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    public void inverseTransform2DPoints(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        if (dArray == null || dArray2 == null) {
            throw new NullPointerException();
        }
        if (!this.isType2D()) {
            throw new IllegalStateException("Cannot transform 2D points with a 3D transform");
        }
        n = this.getFixedSrcOffset(dArray, n, dArray2, n2, n3, 2);
        this.inverseTransform2DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    public void inverseTransform3DPoints(double[] dArray, int n, double[] dArray2, int n2, int n3) throws NonInvertibleTransformException {
        if (dArray == null || dArray2 == null) {
            throw new NullPointerException();
        }
        n = this.getFixedSrcOffset(dArray, n, dArray2, n2, n3, 3);
        this.inverseTransform3DPointsImpl(dArray, n, dArray2, n2, n3);
    }

    public Point2D inverseDeltaTransform(double d, double d2) throws NonInvertibleTransformException {
        this.ensureCanTransform2DPoint();
        return this.getInverseCache().deltaTransform(d, d2);
    }

    public Point2D inverseDeltaTransform(Point2D point2D) throws NonInvertibleTransformException {
        return this.inverseDeltaTransform(point2D.getX(), point2D.getY());
    }

    public Point3D inverseDeltaTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
        return this.getInverseCache().deltaTransform(d, d2, d3);
    }

    public Point3D inverseDeltaTransform(Point3D point3D) throws NonInvertibleTransformException {
        return this.inverseDeltaTransform(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    private int getFixedSrcOffset(double[] dArray, int n, double[] dArray2, int n2, int n3, int n4) {
        if (dArray2 == dArray && n2 > n && n2 < n + n3 * n4) {
            System.arraycopy(dArray, n, dArray2, n2, n3 * n4);
            return n2;
        }
        return n;
    }

    private EventHandlerManager getInternalEventDispatcher() {
        if (this.internalEventDispatcher == null) {
            this.internalEventDispatcher = new EventHandlerManager(this);
        }
        return this.internalEventDispatcher;
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        return this.internalEventDispatcher == null ? eventDispatchChain : eventDispatchChain.append(this.getInternalEventDispatcher());
    }

    public final <T extends Event> void addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().addEventHandler(eventType, eventHandler);
        this.validate();
    }

    public final <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().removeEventHandler(eventType, eventHandler);
    }

    public final <T extends Event> void addEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().addEventFilter(eventType, eventHandler);
        this.validate();
    }

    public final <T extends Event> void removeEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().removeEventFilter(eventType, eventHandler);
    }

    public final void setOnTransformChanged(EventHandler<? super TransformChangedEvent> eventHandler) {
        this.onTransformChangedProperty().set(eventHandler);
        this.validate();
    }

    public final EventHandler<? super TransformChangedEvent> getOnTransformChanged() {
        return this.onTransformChanged == null ? null : (EventHandler)this.onTransformChanged.get();
    }

    public final ObjectProperty<EventHandler<? super TransformChangedEvent>> onTransformChangedProperty() {
        if (this.onTransformChanged == null) {
            this.onTransformChanged = new SimpleObjectProperty<EventHandler<? super TransformChangedEvent>>((Object)this, "onTransformChanged"){

                @Override
                protected void invalidated() {
                    Transform.this.getInternalEventDispatcher().setEventHandler(TransformChangedEvent.TRANSFORM_CHANGED, (EventHandler)this.get());
                }
            };
        }
        return this.onTransformChanged;
    }

    void checkRequestedMAT(MatrixType matrixType) throws IllegalArgumentException {
        if (matrixType.is2D() && !this.isType2D()) {
            throw new IllegalArgumentException("Cannot access 2D matrix for a 3D transform");
        }
    }

    void ensureCanTransform2DPoint() throws IllegalStateException {
        if (!this.isType2D()) {
            throw new IllegalStateException("Cannot transform 2D point with a 3D transform");
        }
    }

    void validate() {
        this.getMxx();
        this.getMxy();
        this.getMxz();
        this.getTx();
        this.getMyx();
        this.getMyy();
        this.getMyz();
        this.getTy();
        this.getMzx();
        this.getMzy();
        this.getMzz();
        this.getTz();
    }

    abstract void apply(Affine3D var1);

    abstract BaseTransform derive(BaseTransform var1);

    void add(Node node) {
        this.nodes.add(node);
    }

    void remove(Node node) {
        this.nodes.remove(node);
    }

    protected void transformChanged() {
        this.inverseCache = null;
        Iterator iterator = this.nodes.iterator();
        while (iterator.hasNext()) {
            NodeHelper.transformsChanged((Node)iterator.next());
        }
        if (this.type2D != null) {
            this.type2D.invalidate();
        }
        if (this.identity != null) {
            this.identity.invalidate();
        }
        if (this.internalEventDispatcher != null) {
            this.validate();
            Event.fireEvent(this, new TransformChangedEvent(this, this));
        }
    }

    void appendTo(Affine affine) {
        affine.append(this.getMxx(), this.getMxy(), this.getMxz(), this.getTx(), this.getMyx(), this.getMyy(), this.getMyz(), this.getTy(), this.getMzx(), this.getMzy(), this.getMzz(), this.getTz());
    }

    void prependTo(Affine affine) {
        affine.prepend(this.getMxx(), this.getMxy(), this.getMxz(), this.getTx(), this.getMyx(), this.getMyy(), this.getMyz(), this.getTy(), this.getMzx(), this.getMzy(), this.getMzz(), this.getTz());
    }

    private Transform getInverseCache() throws NonInvertibleTransformException {
        if (this.inverseCache == null || this.inverseCache.get() == null) {
            Affine affine = new Affine(this.getMxx(), this.getMxy(), this.getMxz(), this.getTx(), this.getMyx(), this.getMyy(), this.getMyz(), this.getTy(), this.getMzx(), this.getMzy(), this.getMzz(), this.getTz());
            affine.invert();
            this.inverseCache = new SoftReference<Affine>(affine);
            return affine;
        }
        return this.inverseCache.get();
    }

    void clearInverseCache() {
        if (this.inverseCache != null) {
            this.inverseCache.clear();
        }
    }

    static Transform createImmutableTransform() {
        return new ImmutableTransform();
    }

    static Transform createImmutableTransform(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        return new ImmutableTransform(d, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);
    }

    static Transform createImmutableTransform(Transform transform, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
        if (transform == null) {
            return new ImmutableTransform(d, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);
        }
        ((ImmutableTransform)transform).setToTransform(d, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);
        return transform;
    }

    static Transform createImmutableTransform(Transform transform, Transform transform2, Transform transform3) {
        if (transform == null) {
            transform = new ImmutableTransform();
        }
        ((ImmutableTransform)transform).setToConcatenation((ImmutableTransform)transform2, (ImmutableTransform)transform3);
        return transform;
    }

    static {
        TransformHelper.setTransformAccessor(new TransformHelper.TransformAccessor(){

            @Override
            public void add(Transform transform, Node node) {
                transform.add(node);
            }

            @Override
            public void remove(Transform transform, Node node) {
                transform.remove(node);
            }

            @Override
            public void apply(Transform transform, Affine3D affine3D) {
                transform.apply(affine3D);
            }

            @Override
            public BaseTransform derive(Transform transform, BaseTransform baseTransform) {
                return transform.derive(baseTransform);
            }

            @Override
            public Transform createImmutableTransform() {
                return Transform.createImmutableTransform();
            }

            @Override
            public Transform createImmutableTransform(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
                return Transform.createImmutableTransform(d, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);
            }

            @Override
            public Transform createImmutableTransform(Transform transform, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
                return Transform.createImmutableTransform(transform, d, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12);
            }

            @Override
            public Transform createImmutableTransform(Transform transform, Transform transform2, Transform transform3) {
                return Transform.createImmutableTransform(transform, transform2, transform3);
            }
        });
    }

    static class ImmutableTransform
    extends Transform {
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

        ImmutableTransform() {
            this.zz = 1.0;
            this.yy = 1.0;
            this.xx = 1.0;
        }

        ImmutableTransform(Transform transform) {
            this(transform.getMxx(), transform.getMxy(), transform.getMxz(), transform.getTx(), transform.getMyx(), transform.getMyy(), transform.getMyz(), transform.getTy(), transform.getMzx(), transform.getMzy(), transform.getMzz(), transform.getTz());
        }

        ImmutableTransform(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
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

        private void setToTransform(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12) {
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

        private void setToConcatenation(ImmutableTransform immutableTransform, ImmutableTransform immutableTransform2) {
            if (immutableTransform.state3d == 0 && immutableTransform2.state3d == 0) {
                this.xx = immutableTransform.xx * immutableTransform2.xx + immutableTransform.xy * immutableTransform2.yx;
                this.xy = immutableTransform.xx * immutableTransform2.xy + immutableTransform.xy * immutableTransform2.yy;
                this.xt = immutableTransform.xx * immutableTransform2.xt + immutableTransform.xy * immutableTransform2.yt + immutableTransform.xt;
                this.yx = immutableTransform.yx * immutableTransform2.xx + immutableTransform.yy * immutableTransform2.yx;
                this.yy = immutableTransform.yx * immutableTransform2.xy + immutableTransform.yy * immutableTransform2.yy;
                this.yt = immutableTransform.yx * immutableTransform2.xt + immutableTransform.yy * immutableTransform2.yt + immutableTransform.yt;
                if (this.state3d != 0) {
                    this.zt = 0.0;
                    this.zy = 0.0;
                    this.zx = 0.0;
                    this.yz = 0.0;
                    this.xz = 0.0;
                    this.zz = 1.0;
                    this.state3d = 0;
                }
                this.updateState2D();
            } else {
                this.xx = immutableTransform.xx * immutableTransform2.xx + immutableTransform.xy * immutableTransform2.yx + immutableTransform.xz * immutableTransform2.zx;
                this.xy = immutableTransform.xx * immutableTransform2.xy + immutableTransform.xy * immutableTransform2.yy + immutableTransform.xz * immutableTransform2.zy;
                this.xz = immutableTransform.xx * immutableTransform2.xz + immutableTransform.xy * immutableTransform2.yz + immutableTransform.xz * immutableTransform2.zz;
                this.xt = immutableTransform.xx * immutableTransform2.xt + immutableTransform.xy * immutableTransform2.yt + immutableTransform.xz * immutableTransform2.zt + immutableTransform.xt;
                this.yx = immutableTransform.yx * immutableTransform2.xx + immutableTransform.yy * immutableTransform2.yx + immutableTransform.yz * immutableTransform2.zx;
                this.yy = immutableTransform.yx * immutableTransform2.xy + immutableTransform.yy * immutableTransform2.yy + immutableTransform.yz * immutableTransform2.zy;
                this.yz = immutableTransform.yx * immutableTransform2.xz + immutableTransform.yy * immutableTransform2.yz + immutableTransform.yz * immutableTransform2.zz;
                this.yt = immutableTransform.yx * immutableTransform2.xt + immutableTransform.yy * immutableTransform2.yt + immutableTransform.yz * immutableTransform2.zt + immutableTransform.yt;
                this.zx = immutableTransform.zx * immutableTransform2.xx + immutableTransform.zy * immutableTransform2.yx + immutableTransform.zz * immutableTransform2.zx;
                this.zy = immutableTransform.zx * immutableTransform2.xy + immutableTransform.zy * immutableTransform2.yy + immutableTransform.zz * immutableTransform2.zy;
                this.zz = immutableTransform.zx * immutableTransform2.xz + immutableTransform.zy * immutableTransform2.yz + immutableTransform.zz * immutableTransform2.zz;
                this.zt = immutableTransform.zx * immutableTransform2.xt + immutableTransform.zy * immutableTransform2.yt + immutableTransform.zz * immutableTransform2.zt + immutableTransform.zt;
                this.updateState();
            }
        }

        @Override
        public double getMxx() {
            return this.xx;
        }

        @Override
        public double getMxy() {
            return this.xy;
        }

        @Override
        public double getMxz() {
            return this.xz;
        }

        @Override
        public double getTx() {
            return this.xt;
        }

        @Override
        public double getMyx() {
            return this.yx;
        }

        @Override
        public double getMyy() {
            return this.yy;
        }

        @Override
        public double getMyz() {
            return this.yz;
        }

        @Override
        public double getTy() {
            return this.yt;
        }

        @Override
        public double getMzx() {
            return this.zx;
        }

        @Override
        public double getMzy() {
            return this.zy;
        }

        @Override
        public double getMzz() {
            return this.zz;
        }

        @Override
        public double getTz() {
            return this.zt;
        }

        @Override
        public double determinant() {
            switch (this.state3d) {
                default: {
                    ImmutableTransform.stateError();
                }
                case 0: {
                    switch (this.state2d) {
                        default: {
                            ImmutableTransform.stateError();
                        }
                        case 6: 
                        case 7: {
                            return this.xx * this.yy - this.xy * this.yx;
                        }
                        case 4: 
                        case 5: {
                            return -(this.xy * this.yx);
                        }
                        case 2: 
                        case 3: {
                            return this.xx * this.yy;
                        }
                        case 0: 
                        case 1: 
                    }
                    return 1.0;
                }
                case 1: {
                    return 1.0;
                }
                case 2: 
                case 3: {
                    return this.xx * this.yy * this.zz;
                }
                case 4: 
            }
            return this.xx * (this.yy * this.zz - this.zy * this.yz) + this.xy * (this.yz * this.zx - this.zz * this.yx) + this.xz * (this.yx * this.zy - this.zx * this.yy);
        }

        @Override
        public Transform createConcatenation(Transform transform) {
            Affine affine = new Affine(this);
            affine.append(transform);
            return affine;
        }

        @Override
        public Affine createInverse() throws NonInvertibleTransformException {
            Affine affine = new Affine(this);
            affine.invert();
            return affine;
        }

        @Override
        public Transform clone() {
            return new ImmutableTransform(this);
        }

        @Override
        public Point2D transform(double d, double d2) {
            this.ensureCanTransform2DPoint();
            switch (this.state2d) {
                default: {
                    ImmutableTransform.stateError();
                }
                case 7: {
                    return new Point2D(this.xx * d + this.xy * d2 + this.xt, this.yx * d + this.yy * d2 + this.yt);
                }
                case 6: {
                    return new Point2D(this.xx * d + this.xy * d2, this.yx * d + this.yy * d2);
                }
                case 5: {
                    return new Point2D(this.xy * d2 + this.xt, this.yx * d + this.yt);
                }
                case 4: {
                    return new Point2D(this.xy * d2, this.yx * d);
                }
                case 3: {
                    return new Point2D(this.xx * d + this.xt, this.yy * d2 + this.yt);
                }
                case 2: {
                    return new Point2D(this.xx * d, this.yy * d2);
                }
                case 1: {
                    return new Point2D(d + this.xt, d2 + this.yt);
                }
                case 0: 
            }
            return new Point2D(d, d2);
        }

        @Override
        public Point3D transform(double d, double d2, double d3) {
            switch (this.state3d) {
                default: {
                    ImmutableTransform.stateError();
                }
                case 0: {
                    switch (this.state2d) {
                        default: {
                            ImmutableTransform.stateError();
                        }
                        case 7: {
                            return new Point3D(this.xx * d + this.xy * d2 + this.xt, this.yx * d + this.yy * d2 + this.yt, d3);
                        }
                        case 6: {
                            return new Point3D(this.xx * d + this.xy * d2, this.yx * d + this.yy * d2, d3);
                        }
                        case 5: {
                            return new Point3D(this.xy * d2 + this.xt, this.yx * d + this.yt, d3);
                        }
                        case 4: {
                            return new Point3D(this.xy * d2, this.yx * d, d3);
                        }
                        case 3: {
                            return new Point3D(this.xx * d + this.xt, this.yy * d2 + this.yt, d3);
                        }
                        case 2: {
                            return new Point3D(this.xx * d, this.yy * d2, d3);
                        }
                        case 1: {
                            return new Point3D(d + this.xt, d2 + this.yt, d3);
                        }
                        case 0: 
                    }
                    return new Point3D(d, d2, d3);
                }
                case 1: {
                    return new Point3D(d + this.xt, d2 + this.yt, d3 + this.zt);
                }
                case 2: {
                    return new Point3D(this.xx * d, this.yy * d2, this.zz * d3);
                }
                case 3: {
                    return new Point3D(this.xx * d + this.xt, this.yy * d2 + this.yt, this.zz * d3 + this.zt);
                }
                case 4: 
            }
            return new Point3D(this.xx * d + this.xy * d2 + this.xz * d3 + this.xt, this.yx * d + this.yy * d2 + this.yz * d3 + this.yt, this.zx * d + this.zy * d2 + this.zz * d3 + this.zt);
        }

        @Override
        public Point2D deltaTransform(double d, double d2) {
            this.ensureCanTransform2DPoint();
            switch (this.state2d) {
                default: {
                    ImmutableTransform.stateError();
                }
                case 6: 
                case 7: {
                    return new Point2D(this.xx * d + this.xy * d2, this.yx * d + this.yy * d2);
                }
                case 4: 
                case 5: {
                    return new Point2D(this.xy * d2, this.yx * d);
                }
                case 2: 
                case 3: {
                    return new Point2D(this.xx * d, this.yy * d2);
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
                    ImmutableTransform.stateError();
                }
                case 0: {
                    switch (this.state2d) {
                        default: {
                            ImmutableTransform.stateError();
                        }
                        case 6: 
                        case 7: {
                            return new Point3D(this.xx * d + this.xy * d2, this.yx * d + this.yy * d2, d3);
                        }
                        case 4: 
                        case 5: {
                            return new Point3D(this.xy * d2, this.yx * d, d3);
                        }
                        case 2: 
                        case 3: {
                            return new Point3D(this.xx * d, this.yy * d2, d3);
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
                    return new Point3D(this.xx * d, this.yy * d2, this.zz * d3);
                }
                case 4: 
            }
            return new Point3D(this.xx * d + this.xy * d2 + this.xz * d3, this.yx * d + this.yy * d2 + this.yz * d3, this.zx * d + this.zy * d2 + this.zz * d3);
        }

        @Override
        public Point2D inverseTransform(double d, double d2) throws NonInvertibleTransformException {
            this.ensureCanTransform2DPoint();
            switch (this.state2d) {
                default: {
                    return super.inverseTransform(d, d2);
                }
                case 5: {
                    if (this.xy == 0.0 || this.yx == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point2D(1.0 / this.yx * d2 - this.yt / this.yx, 1.0 / this.xy * d - this.xt / this.xy);
                }
                case 4: {
                    if (this.xy == 0.0 || this.yx == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point2D(1.0 / this.yx * d2, 1.0 / this.xy * d);
                }
                case 3: {
                    if (this.xx == 0.0 || this.yy == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point2D(1.0 / this.xx * d - this.xt / this.xx, 1.0 / this.yy * d2 - this.yt / this.yy);
                }
                case 2: {
                    if (this.xx == 0.0 || this.yy == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point2D(1.0 / this.xx * d, 1.0 / this.yy * d2);
                }
                case 1: {
                    return new Point2D(d - this.xt, d2 - this.yt);
                }
                case 0: 
            }
            return new Point2D(d, d2);
        }

        @Override
        public Point3D inverseTransform(double d, double d2, double d3) throws NonInvertibleTransformException {
            switch (this.state3d) {
                default: {
                    ImmutableTransform.stateError();
                }
                case 0: {
                    switch (this.state2d) {
                        default: {
                            return super.inverseTransform(d, d2, d3);
                        }
                        case 5: {
                            if (this.xy == 0.0 || this.yx == 0.0) {
                                throw new NonInvertibleTransformException("Determinant is 0");
                            }
                            return new Point3D(1.0 / this.yx * d2 - this.yt / this.yx, 1.0 / this.xy * d - this.xt / this.xy, d3);
                        }
                        case 4: {
                            if (this.xy == 0.0 || this.yx == 0.0) {
                                throw new NonInvertibleTransformException("Determinant is 0");
                            }
                            return new Point3D(1.0 / this.yx * d2, 1.0 / this.xy * d, d3);
                        }
                        case 3: {
                            if (this.xx == 0.0 || this.yy == 0.0) {
                                throw new NonInvertibleTransformException("Determinant is 0");
                            }
                            return new Point3D(1.0 / this.xx * d - this.xt / this.xx, 1.0 / this.yy * d2 - this.yt / this.yy, d3);
                        }
                        case 2: {
                            if (this.xx == 0.0 || this.yy == 0.0) {
                                throw new NonInvertibleTransformException("Determinant is 0");
                            }
                            return new Point3D(1.0 / this.xx * d, 1.0 / this.yy * d2, d3);
                        }
                        case 1: {
                            return new Point3D(d - this.xt, d2 - this.yt, d3);
                        }
                        case 0: 
                    }
                    return new Point3D(d, d2, d3);
                }
                case 1: {
                    return new Point3D(d - this.xt, d2 - this.yt, d3 - this.zt);
                }
                case 2: {
                    if (this.xx == 0.0 || this.yy == 0.0 || this.zz == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point3D(1.0 / this.xx * d, 1.0 / this.yy * d2, 1.0 / this.zz * d3);
                }
                case 3: {
                    if (this.xx == 0.0 || this.yy == 0.0 || this.zz == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point3D(1.0 / this.xx * d - this.xt / this.xx, 1.0 / this.yy * d2 - this.yt / this.yy, 1.0 / this.zz * d3 - this.zt / this.zz);
                }
                case 4: 
            }
            return super.inverseTransform(d, d2, d3);
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
                    if (this.xy == 0.0 || this.yx == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point2D(1.0 / this.yx * d2, 1.0 / this.xy * d);
                }
                case 2: 
                case 3: {
                    if (this.xx == 0.0 || this.yy == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point2D(1.0 / this.xx * d, 1.0 / this.yy * d2);
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
                    ImmutableTransform.stateError();
                }
                case 0: {
                    switch (this.state2d) {
                        default: {
                            return super.inverseDeltaTransform(d, d2, d3);
                        }
                        case 4: 
                        case 5: {
                            if (this.xy == 0.0 || this.yx == 0.0) {
                                throw new NonInvertibleTransformException("Determinant is 0");
                            }
                            return new Point3D(1.0 / this.yx * d2, 1.0 / this.xy * d, d3);
                        }
                        case 2: 
                        case 3: {
                            if (this.xx == 0.0 || this.yy == 0.0) {
                                throw new NonInvertibleTransformException("Determinant is 0");
                            }
                            return new Point3D(1.0 / this.xx * d, 1.0 / this.yy * d2, d3);
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
                    if (this.xx == 0.0 || this.yy == 0.0 || this.zz == 0.0) {
                        throw new NonInvertibleTransformException("Determinant is 0");
                    }
                    return new Point3D(1.0 / this.xx * d, 1.0 / this.yy * d2, 1.0 / this.zz * d3);
                }
                case 4: 
            }
            return super.inverseDeltaTransform(d, d2, d3);
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("Transform [\n");
            stringBuilder.append("\t").append(this.xx);
            stringBuilder.append(", ").append(this.xy);
            stringBuilder.append(", ").append(this.xz);
            stringBuilder.append(", ").append(this.xt);
            stringBuilder.append('\n');
            stringBuilder.append("\t").append(this.yx);
            stringBuilder.append(", ").append(this.yy);
            stringBuilder.append(", ").append(this.yz);
            stringBuilder.append(", ").append(this.yt);
            stringBuilder.append('\n');
            stringBuilder.append("\t").append(this.zx);
            stringBuilder.append(", ").append(this.zy);
            stringBuilder.append(", ").append(this.zz);
            stringBuilder.append(", ").append(this.zt);
            return stringBuilder.append("\n]").toString();
        }

        private void updateState() {
            this.updateState2D();
            this.state3d = 0;
            if (this.xz != 0.0 || this.yz != 0.0 || this.zx != 0.0 || this.zy != 0.0) {
                this.state3d = 4;
            } else if ((this.state2d & 4) == 0) {
                if (this.zt != 0.0) {
                    this.state3d |= 1;
                }
                if (this.zz != 1.0) {
                    this.state3d |= 2;
                }
                if (this.state3d != 0) {
                    this.state3d |= this.state2d & 3;
                }
            } else if (this.zz != 1.0 || this.zt != 0.0) {
                this.state3d = 4;
            }
        }

        private void updateState2D() {
            this.state2d = this.xy == 0.0 && this.yx == 0.0 ? (this.xx == 1.0 && this.yy == 1.0 ? (this.xt == 0.0 && this.yt == 0.0 ? 0 : 1) : (this.xt == 0.0 && this.yt == 0.0 ? 2 : 3)) : (this.xx == 0.0 && this.yy == 0.0 ? (this.xt == 0.0 && this.yt == 0.0 ? 4 : 5) : (this.xt == 0.0 && this.yt == 0.0 ? 6 : 7));
        }

        @Override
        void ensureCanTransform2DPoint() throws IllegalStateException {
            if (this.state3d != 0) {
                throw new IllegalStateException("Cannot transform 2D point with a 3D transform");
            }
        }

        private static void stateError() {
            throw new InternalError("missing case in a switch");
        }

        @Override
        void apply(Affine3D affine3D) {
            affine3D.concatenate(this.xx, this.xy, this.xz, this.xt, this.yx, this.yy, this.yz, this.yt, this.zx, this.zy, this.zz, this.zt);
        }

        @Override
        BaseTransform derive(BaseTransform baseTransform) {
            return baseTransform.deriveWithConcatenation(this.xx, this.xy, this.xz, this.xt, this.yx, this.yy, this.yz, this.yt, this.zx, this.zy, this.zz, this.zt);
        }

        int getState2d() {
            return this.state2d;
        }

        int getState3d() {
            return this.state3d;
        }
    }

    private static abstract class LazyBooleanProperty
    extends ReadOnlyBooleanProperty {
        private ExpressionHelper<Boolean> helper;
        private boolean valid;
        private boolean value;

        private LazyBooleanProperty() {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> changeListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super Boolean> changeListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
        }

        @Override
        public boolean get() {
            if (!this.valid) {
                this.value = this.computeValue();
                this.valid = true;
            }
            return this.value;
        }

        public void invalidate() {
            if (this.valid) {
                this.valid = false;
                ExpressionHelper.fireValueChangedEvent(this.helper);
            }
        }

        protected abstract boolean computeValue();
    }
}

