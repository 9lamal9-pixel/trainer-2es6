/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.scene.shape.CylinderHelper;
import com.sun.javafx.scene.shape.MeshHelper;
import com.sun.javafx.sg.prism.NGCylinder;
import com.sun.javafx.sg.prism.NGNode;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

public class Cylinder
extends Shape3D {
    static final int DEFAULT_DIVISIONS = 64;
    static final double DEFAULT_RADIUS = 1.0;
    static final double DEFAULT_HEIGHT = 2.0;
    private int divisions = 64;
    private TriangleMesh mesh;
    private DoubleProperty height;
    private DoubleProperty radius;

    public Cylinder() {
        this(1.0, 2.0, 64);
    }

    public Cylinder(double d, double d2) {
        this(d, d2, 64);
    }

    public Cylinder(double d, double d2, int n) {
        CylinderHelper.initHelper(this);
        this.divisions = n < 3 ? 3 : n;
        this.setRadius(d);
        this.setHeight(d2);
    }

    public final void setHeight(double d) {
        this.heightProperty().set(d);
    }

    public final double getHeight() {
        return this.height == null ? 2.0 : this.height.get();
    }

    public final DoubleProperty heightProperty() {
        if (this.height == null) {
            this.height = new SimpleDoubleProperty(this, "height", 2.0){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Cylinder.this, DirtyBits.MESH_GEOM);
                    Cylinder.this.manager.invalidateCylinderMesh(Cylinder.this.key);
                    Cylinder.this.key = null;
                    NodeHelper.geomChanged(Cylinder.this);
                }
            };
        }
        return this.height;
    }

    public final void setRadius(double d) {
        this.radiusProperty().set(d);
    }

    public final double getRadius() {
        return this.radius == null ? 1.0 : this.radius.get();
    }

    public final DoubleProperty radiusProperty() {
        if (this.radius == null) {
            this.radius = new SimpleDoubleProperty(this, "radius", 1.0){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Cylinder.this, DirtyBits.MESH_GEOM);
                    Cylinder.this.manager.invalidateCylinderMesh(Cylinder.this.key);
                    Cylinder.this.key = null;
                    NodeHelper.geomChanged(Cylinder.this);
                }
            };
        }
        return this.radius;
    }

    public int getDivisions() {
        return this.divisions;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.MESH_GEOM)) {
            NGCylinder nGCylinder = (NGCylinder)NodeHelper.getPeer(this);
            float f = (float)this.getHeight();
            float f2 = (float)this.getRadius();
            if (f < 0.0f || f2 < 0.0f) {
                nGCylinder.updateMesh(null);
            } else {
                if (this.key == null) {
                    this.key = new CylinderKey(f, f2, this.divisions);
                }
                this.mesh = this.manager.getCylinderMesh(f, f2, this.divisions, this.key);
                this.mesh.updatePG();
                nGCylinder.updateMesh(this.mesh.getPGTriangleMesh());
            }
        }
    }

    private NGNode doCreatePeer() {
        return new NGCylinder();
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        float f = (float)this.getHeight();
        float f2 = (float)this.getRadius();
        if (f2 < 0.0f || f < 0.0f) {
            return baseBounds.makeEmpty();
        }
        float f3 = f * 0.5f;
        baseBounds = baseBounds.deriveWithNewBounds(-f2, -f3, -f2, f2, f3, f2);
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private boolean doComputeContains(double d, double d2) {
        double d3 = this.getRadius();
        double d4 = this.getHeight() * 0.5;
        return -d3 <= d && d <= d3 && -d4 <= d2 && d2 <= d4;
    }

    private boolean doComputeIntersects(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        boolean bl = this.divisions < 64 && this.mesh != null;
        double d6 = this.getRadius();
        Vec3d vec3d = pickRay.getDirectionNoClone();
        double d7 = vec3d.x;
        double d8 = vec3d.y;
        double d9 = vec3d.z;
        Vec3d vec3d2 = pickRay.getOriginNoClone();
        double d10 = vec3d2.x;
        double d11 = vec3d2.y;
        double d12 = vec3d2.z;
        double d13 = this.getHeight();
        double d14 = d13 / 2.0;
        CullFace cullFace = this.getCullFace();
        double d15 = d7 * d7 + d9 * d9;
        double d16 = 2.0 * (d7 * d10 + d9 * d12);
        double d17 = d10 * d10 + d12 * d12 - d6 * d6;
        double d18 = d16 * d16 - 4.0 * d15 * d17;
        double d19 = Double.POSITIVE_INFINITY;
        double d20 = pickRay.getNearClip();
        double d21 = pickRay.getFarClip();
        if (d18 >= 0.0 && (d7 != 0.0 || d9 != 0.0)) {
            double d22 = Math.sqrt(d18);
            d5 = d16 < 0.0 ? (-d16 - d22) / 2.0 : (-d16 + d22) / 2.0;
            d4 = d5 / d15;
            if (d4 > (d3 = d17 / d5)) {
                d2 = d4;
                d4 = d3;
                d3 = d2;
            }
            d2 = d11 + d4 * d8;
            if (d4 < d20 || d2 < -d14 || d2 > d14 || cullFace == CullFace.FRONT) {
                d = d11 + d3 * d8;
                if (d3 >= d20 && d3 <= d21 && d >= -d14 && d <= d14 && (cullFace != CullFace.BACK || bl)) {
                    d19 = d3;
                }
            } else if (d4 <= d21) {
                d19 = d4;
            }
        }
        boolean bl2 = false;
        boolean bl3 = false;
        if (d19 == Double.POSITIVE_INFINITY || !bl) {
            double d23;
            double d24;
            d5 = (-d14 - d11) / d8;
            d2 = (d14 - d11) / d8;
            boolean bl4 = false;
            if (d5 < d2) {
                d4 = d5;
                d3 = d2;
                bl4 = true;
            } else {
                d4 = d2;
                d3 = d5;
            }
            if (d4 >= d20 && d4 <= d21 && d4 < d19 && cullFace != CullFace.FRONT && (d24 = d10 + d7 * d4) * d24 + (d23 = d12 + d9 * d4) * d23 <= d6 * d6) {
                bl3 = bl4;
                bl2 = !bl4;
                d19 = d4;
            }
            if (d3 >= d20 && d3 <= d21 && d3 < d19 && (cullFace != CullFace.BACK || bl) && (d24 = d10 + d7 * d3) * d24 + (d23 = d12 + d9 * d3) * d23 <= d6 * d6) {
                bl2 = bl4;
                bl3 = !bl4;
                d19 = d3;
            }
        }
        if (Double.isInfinite(d19) || Double.isNaN(d19)) {
            return false;
        }
        if (bl) {
            return MeshHelper.computeIntersects(this.mesh, pickRay, pickResultChooser, this, cullFace, false);
        }
        if (pickResultChooser != null && pickResultChooser.isCloser(d19)) {
            Point2D point2D;
            Point3D point3D = PickResultChooser.computePoint(pickRay, d19);
            if (bl2) {
                point2D = new Point2D(0.5 + point3D.getX() / (2.0 * d6), 0.5 + point3D.getZ() / (2.0 * d6));
            } else if (bl3) {
                point2D = new Point2D(0.5 + point3D.getX() / (2.0 * d6), 0.5 - point3D.getZ() / (2.0 * d6));
            } else {
                Point3D point3D2 = new Point3D(point3D.getX(), 0.0, point3D.getZ());
                Point3D point3D3 = point3D2.crossProduct(Rotate.Z_AXIS);
                d = point3D2.angle(Rotate.Z_AXIS);
                if (point3D3.getY() > 0.0) {
                    d = 360.0 - d;
                }
                point2D = new Point2D(1.0 - d / 360.0, 0.5 + point3D.getY() / d13);
            }
            pickResultChooser.offer(this, d19, -1, point3D, point2D);
        }
        return true;
    }

    static TriangleMesh createMesh(int n, float f, float f2) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        int n8;
        double d;
        int n9;
        int n10 = n * 2 + 2;
        int n11 = (n + 1) * 4 + 1;
        int n12 = n * 4;
        float f3 = 0.00390625f;
        float f4 = 1.0f / (float)n;
        f *= 0.5f;
        float[] fArray = new float[n10 * 3];
        float[] fArray2 = new float[n11 * 2];
        int[] nArray = new int[n12 * 6];
        int[] nArray2 = new int[n12];
        int n13 = 0;
        int n14 = 0;
        for (n9 = 0; n9 < n; ++n9) {
            d = (double)(f4 * (float)n9 * 2.0f) * Math.PI;
            fArray[n13 + 0] = (float)(Math.sin(d) * (double)f2);
            fArray[n13 + 2] = (float)(Math.cos(d) * (double)f2);
            fArray[n13 + 1] = f;
            fArray2[n14 + 0] = 1.0f - f4 * (float)n9;
            fArray2[n14 + 1] = 1.0f - f3;
            n13 += 3;
            n14 += 2;
        }
        fArray2[n14 + 0] = 0.0f;
        fArray2[n14 + 1] = 1.0f - f3;
        n14 += 2;
        for (n9 = 0; n9 < n; ++n9) {
            d = (double)(f4 * (float)n9 * 2.0f) * Math.PI;
            fArray[n13 + 0] = (float)(Math.sin(d) * (double)f2);
            fArray[n13 + 2] = (float)(Math.cos(d) * (double)f2);
            fArray[n13 + 1] = -f;
            fArray2[n14 + 0] = 1.0f - f4 * (float)n9;
            fArray2[n14 + 1] = f3;
            n13 += 3;
            n14 += 2;
        }
        fArray2[n14 + 0] = 0.0f;
        fArray2[n14 + 1] = f3;
        n14 += 2;
        fArray[n13 + 0] = 0.0f;
        fArray[n13 + 1] = f;
        fArray[n13 + 2] = 0.0f;
        fArray[n13 + 3] = 0.0f;
        fArray[n13 + 4] = -f;
        fArray[n13 + 5] = 0.0f;
        n13 += 6;
        for (n9 = 0; n9 <= n; ++n9) {
            d = n9 < n ? (double)(f4 * (float)n9 * 2.0f) * Math.PI : 0.0;
            fArray2[n14 + 0] = (float)(Math.sin(d) * 0.5) + 0.5f;
            fArray2[n14 + 1] = (float)(Math.cos(d) * 0.5) + 0.5f;
            n14 += 2;
        }
        for (n9 = 0; n9 <= n; ++n9) {
            d = n9 < n ? (double)(f4 * (float)n9 * 2.0f) * Math.PI : 0.0;
            fArray2[n14 + 0] = 0.5f + (float)(Math.sin(d) * 0.5);
            fArray2[n14 + 1] = 0.5f - (float)(Math.cos(d) * 0.5);
            n14 += 2;
        }
        fArray2[n14 + 0] = 0.5f;
        fArray2[n14 + 1] = 0.5f;
        n14 += 2;
        n9 = 0;
        for (n8 = 0; n8 < n; ++n8) {
            n7 = n8 + 1;
            n6 = n8 + n;
            n5 = n7 + n;
            nArray[n9 + 0] = n8;
            nArray[n9 + 1] = n8;
            nArray[n9 + 2] = n6;
            nArray[n9 + 3] = n6 + 1;
            nArray[n9 + 4] = n7 == n ? 0 : n7;
            nArray[n9 + 5] = n7;
            nArray[(n9 += 6) + 0] = n5 % n == 0 ? n5 - n : n5;
            nArray[n9 + 1] = n5 + 1;
            nArray[n9 + 2] = n7 == n ? 0 : n7;
            nArray[n9 + 3] = n7;
            nArray[n9 + 4] = n6;
            nArray[n9 + 5] = n6 + 1;
            n9 += 6;
        }
        n8 = (n + 1) * 2;
        n7 = (n + 1) * 4;
        n6 = n * 2;
        for (n5 = 0; n5 < n; ++n5) {
            n4 = n5 + 1;
            n3 = n8 + n5;
            n2 = n3 + 1;
            nArray[n9 + 0] = n5;
            nArray[n9 + 1] = n3;
            nArray[n9 + 2] = n4 == n ? 0 : n4;
            nArray[n9 + 3] = n2;
            nArray[n9 + 4] = n6;
            nArray[n9 + 5] = n7;
            n9 += 6;
        }
        n6 = n * 2 + 1;
        n8 = (n + 1) * 3;
        for (n5 = 0; n5 < n; ++n5) {
            n4 = n5 + 1 + n;
            n3 = n8 + n5;
            n2 = n3 + 1;
            nArray[n9 + 0] = n5 + n;
            nArray[n9 + 1] = n3;
            nArray[n9 + 2] = n6;
            nArray[n9 + 3] = n7;
            nArray[n9 + 4] = n4 % n == 0 ? n4 - n : n4;
            nArray[n9 + 5] = n2;
            n9 += 6;
        }
        for (n5 = 0; n5 < n * 2; ++n5) {
            nArray2[n5] = 1;
        }
        for (n5 = n * 2; n5 < n * 4; ++n5) {
            nArray2[n5] = 2;
        }
        TriangleMesh triangleMesh = new TriangleMesh(true);
        triangleMesh.getPoints().setAll(fArray);
        triangleMesh.getTexCoords().setAll(fArray2);
        triangleMesh.getFaces().setAll(nArray);
        triangleMesh.getFaceSmoothingGroups().setAll(nArray2);
        return triangleMesh;
    }

    static {
        CylinderHelper.setCylinderAccessor(new CylinderHelper.CylinderAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Cylinder)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Cylinder)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Cylinder)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Cylinder)node).doComputeContains(d, d2);
            }

            @Override
            public boolean doComputeIntersects(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                return ((Cylinder)node).doComputeIntersects(pickRay, pickResultChooser);
            }
        });
    }

    private static class CylinderKey
    extends Shape3D.Key {
        final double radius;
        final double height;
        final int divisions;

        private CylinderKey(double d, double d2, int n) {
            this.radius = d;
            this.height = d2;
            this.divisions = n;
        }

        @Override
        public int hashCode() {
            long l = 7L;
            l = 31L * l + Double.doubleToLongBits(this.radius);
            l = 31L * l + Double.doubleToLongBits(this.height);
            l = 31L * l + (long)this.divisions;
            return Long.hashCode(l);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (!(object instanceof CylinderKey)) {
                return false;
            }
            CylinderKey cylinderKey = (CylinderKey)object;
            if (this.divisions != cylinderKey.divisions) {
                return false;
            }
            if (Double.compare(this.radius, cylinderKey.radius) != 0) {
                return false;
            }
            return Double.compare(this.height, cylinderKey.height) == 0;
        }
    }
}

