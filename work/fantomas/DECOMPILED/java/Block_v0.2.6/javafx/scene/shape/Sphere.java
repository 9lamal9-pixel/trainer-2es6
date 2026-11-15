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
import com.sun.javafx.scene.shape.MeshHelper;
import com.sun.javafx.scene.shape.SphereHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGSphere;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

public class Sphere
extends Shape3D {
    static final int DEFAULT_DIVISIONS = 64;
    static final double DEFAULT_RADIUS = 1.0;
    private int divisions = 64;
    private TriangleMesh mesh;
    private DoubleProperty radius;

    public Sphere() {
        this(1.0, 64);
    }

    public Sphere(double d) {
        this(d, 64);
    }

    public Sphere(double d, int n) {
        SphereHelper.initHelper(this);
        this.divisions = n < 1 ? 1 : n;
        this.setRadius(d);
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
                    NodeHelper.markDirty(Sphere.this, DirtyBits.MESH_GEOM);
                    Sphere.this.manager.invalidateSphereMesh(Sphere.this.key);
                    Sphere.this.key = null;
                    NodeHelper.geomChanged(Sphere.this);
                }
            };
        }
        return this.radius;
    }

    public int getDivisions() {
        return this.divisions;
    }

    private NGNode doCreatePeer() {
        return new NGSphere();
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.MESH_GEOM)) {
            NGSphere nGSphere = (NGSphere)NodeHelper.getPeer(this);
            float f = (float)this.getRadius();
            if (f < 0.0f) {
                nGSphere.updateMesh(null);
            } else {
                if (this.key == null) {
                    this.key = new SphereKey(f, this.divisions);
                }
                this.mesh = this.manager.getSphereMesh(f, this.divisions, this.key);
                this.mesh.updatePG();
                nGSphere.updateMesh(this.mesh.getPGTriangleMesh());
            }
        }
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        float f = (float)this.getRadius();
        if (f < 0.0f) {
            return baseBounds.makeEmpty();
        }
        baseBounds = baseBounds.deriveWithNewBounds(-f, -f, -f, f, f, f);
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private boolean doComputeContains(double d, double d2) {
        double d3 = d * d + d2 * d2;
        double d4 = this.getRadius();
        return d3 <= d4 * d4;
    }

    private boolean doComputeIntersects(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d;
        double d2;
        boolean bl = this.divisions < 64 && this.mesh != null;
        double d3 = this.getRadius();
        Vec3d vec3d = pickRay.getDirectionNoClone();
        double d4 = vec3d.x;
        double d5 = vec3d.y;
        double d6 = vec3d.z;
        Vec3d vec3d2 = pickRay.getOriginNoClone();
        double d7 = vec3d2.x;
        double d8 = vec3d2.y;
        double d9 = vec3d2.z;
        double d10 = 2.0 * (d4 * d7 + d5 * d8 + d6 * d9);
        double d11 = d4 * d4 + d5 * d5 + d6 * d6;
        double d12 = d7 * d7 + d8 * d8 + d9 * d9 - d3 * d3;
        double d13 = d10 * d10 - 4.0 * d11 * d12;
        if (d13 < 0.0) {
            return false;
        }
        double d14 = Math.sqrt(d13);
        double d15 = d10 < 0.0 ? (-d10 - d14) / 2.0 : (-d10 + d14) / 2.0;
        double d16 = d15 / d11;
        if (d16 > (d2 = d12 / d15)) {
            d = d16;
            d16 = d2;
            d2 = d;
        }
        d = pickRay.getNearClip();
        double d17 = pickRay.getFarClip();
        if (d2 < d || d16 > d17) {
            return false;
        }
        double d18 = d16;
        CullFace cullFace = this.getCullFace();
        if (d16 < d || cullFace == CullFace.FRONT) {
            if (d2 <= d17 && this.getCullFace() != CullFace.BACK) {
                d18 = d2;
            } else if (!bl) {
                return false;
            }
        }
        if (Double.isInfinite(d18) || Double.isNaN(d18)) {
            return false;
        }
        if (bl) {
            return MeshHelper.computeIntersects(this.mesh, pickRay, pickResultChooser, this, cullFace, false);
        }
        if (pickResultChooser != null && pickResultChooser.isCloser(d18)) {
            Point3D point3D = PickResultChooser.computePoint(pickRay, d18);
            Point3D point3D2 = new Point3D(point3D.getX(), 0.0, point3D.getZ());
            Point3D point3D3 = point3D2.crossProduct(Rotate.Z_AXIS);
            double d19 = point3D2.angle(Rotate.Z_AXIS);
            if (point3D3.getY() > 0.0) {
                d19 = 360.0 - d19;
            }
            Point2D point2D = new Point2D(1.0 - d19 / 360.0, 0.5 + point3D.getY() / (2.0 * d3));
            pickResultChooser.offer(this, d18, -1, point3D, point2D);
        }
        return true;
    }

    private static int correctDivisions(int n) {
        return (n + 3) / 4 * 4;
    }

    static TriangleMesh createMesh(int n, float f) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        float f2;
        int n8;
        n = Sphere.correctDivisions(n);
        int n9 = n / 2;
        int n10 = n * (n9 - 1) + 2;
        int n11 = (n + 1) * (n9 - 1) + n * 2;
        int n12 = n * (n9 - 2) * 2 + n * 2;
        float f3 = 1.0f / (float)n;
        float[] fArray = new float[n10 * 3];
        float[] fArray2 = new float[n11 * 2];
        int[] nArray = new int[n12 * 6];
        int n13 = 0;
        int n14 = 0;
        for (n8 = 0; n8 < n9 - 1; ++n8) {
            f2 = f3 * (float)(n8 + 1 - n9 / 2) * 2.0f * (float)Math.PI;
            float f4 = (float)Math.sin(f2);
            float f5 = (float)Math.cos(f2);
            float f6 = 0.5f + f4 * 0.5f;
            for (n7 = 0; n7 < n; ++n7) {
                double d = f3 * (float)n7 * 2.0f * (float)Math.PI;
                float f7 = (float)Math.sin(d);
                float f8 = (float)Math.cos(d);
                fArray[n13 + 0] = f7 * f5 * f;
                fArray[n13 + 2] = f8 * f5 * f;
                fArray[n13 + 1] = f4 * f;
                fArray2[n14 + 0] = 1.0f - f3 * (float)n7;
                fArray2[n14 + 1] = f6;
                n13 += 3;
                n14 += 2;
            }
            fArray2[n14 + 0] = 0.0f;
            fArray2[n14 + 1] = f6;
            n14 += 2;
        }
        fArray[n13 + 0] = 0.0f;
        fArray[n13 + 1] = -f;
        fArray[n13 + 2] = 0.0f;
        fArray[n13 + 3] = 0.0f;
        fArray[n13 + 4] = f;
        fArray[n13 + 5] = 0.0f;
        n13 += 6;
        n8 = (n9 - 1) * n;
        f2 = 0.00390625f;
        for (n6 = 0; n6 < n; ++n6) {
            fArray2[n14 + 0] = 1.0f - f3 * (0.5f + (float)n6);
            fArray2[n14 + 1] = f2;
            n14 += 2;
        }
        for (n6 = 0; n6 < n; ++n6) {
            fArray2[n14 + 0] = 1.0f - f3 * (0.5f + (float)n6);
            fArray2[n14 + 1] = 1.0f - f2;
            n14 += 2;
        }
        n6 = 0;
        for (n5 = 0; n5 < n9 - 2; ++n5) {
            for (int i = 0; i < n; ++i) {
                n7 = n5 * n + i;
                int n15 = n7 + 1;
                n4 = n7 + n;
                int n16 = n15 + n;
                int n17 = n7 + n5;
                n3 = n17 + 1;
                n2 = n17 + (n + 1);
                int n18 = n3 + (n + 1);
                nArray[n6 + 0] = n7;
                nArray[n6 + 1] = n17;
                nArray[n6 + 2] = n15 % n == 0 ? n15 - n : n15;
                nArray[n6 + 3] = n3;
                nArray[n6 + 4] = n4;
                nArray[n6 + 5] = n2;
                nArray[(n6 += 6) + 0] = n16 % n == 0 ? n16 - n : n16;
                nArray[n6 + 1] = n18;
                nArray[n6 + 2] = n4;
                nArray[n6 + 3] = n2;
                nArray[n6 + 4] = n15 % n == 0 ? n15 - n : n15;
                nArray[n6 + 5] = n3;
                n6 += 6;
            }
        }
        n5 = n8;
        int n19 = (n9 - 1) * (n + 1);
        for (n7 = 0; n7 < n; ++n7) {
            int n20 = n7;
            n4 = n7 + 1;
            int n21 = n19 + n7;
            nArray[n6 + 0] = n5;
            nArray[n6 + 1] = n21;
            nArray[n6 + 2] = n4 == n ? 0 : n4;
            nArray[n6 + 3] = n4;
            nArray[n6 + 4] = n20;
            nArray[n6 + 5] = n20;
            n6 += 6;
        }
        ++n5;
        n19 += n;
        n7 = (n9 - 2) * n;
        for (int i = 0; i < n; ++i) {
            n4 = n7 + i;
            int n22 = n7 + i + 1;
            int n23 = n19 + i;
            n3 = (n9 - 2) * (n + 1) + i;
            n2 = n3 + 1;
            nArray[n6 + 0] = n5;
            nArray[n6 + 1] = n23;
            nArray[n6 + 2] = n4;
            nArray[n6 + 3] = n3;
            nArray[n6 + 4] = n22 % n == 0 ? n22 - n : n22;
            nArray[n6 + 5] = n2;
            n6 += 6;
        }
        TriangleMesh triangleMesh = new TriangleMesh(true);
        triangleMesh.getPoints().setAll(fArray);
        triangleMesh.getTexCoords().setAll(fArray2);
        triangleMesh.getFaces().setAll(nArray);
        return triangleMesh;
    }

    static {
        SphereHelper.setSphereAccessor(new SphereHelper.SphereAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Sphere)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Sphere)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Sphere)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Sphere)node).doComputeContains(d, d2);
            }

            @Override
            public boolean doComputeIntersects(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                return ((Sphere)node).doComputeIntersects(pickRay, pickResultChooser);
            }
        });
    }

    private static class SphereKey
    extends Shape3D.Key {
        final double radius;
        final int divisions;

        private SphereKey(double d, int n) {
            this.radius = d;
            this.divisions = n;
        }

        @Override
        public int hashCode() {
            long l = 7L;
            l = 31L * l + Double.doubleToLongBits(this.radius);
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
            if (!(object instanceof SphereKey)) {
                return false;
            }
            SphereKey sphereKey = (SphereKey)object;
            if (this.divisions != sphereKey.divisions) {
                return false;
            }
            return Double.compare(this.radius, sphereKey.radius) == 0;
        }
    }
}

