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
import com.sun.javafx.scene.shape.BoxHelper;
import com.sun.javafx.sg.prism.NGBox;
import com.sun.javafx.sg.prism.NGNode;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;

public class Box
extends Shape3D {
    private TriangleMesh mesh;
    public static final double DEFAULT_SIZE = 2.0;
    private DoubleProperty depth;
    private DoubleProperty height;
    private DoubleProperty width;

    public Box() {
        this(2.0, 2.0, 2.0);
    }

    public Box(double d, double d2, double d3) {
        BoxHelper.initHelper(this);
        this.setWidth(d);
        this.setHeight(d2);
        this.setDepth(d3);
    }

    public final void setDepth(double d) {
        this.depthProperty().set(d);
    }

    public final double getDepth() {
        return this.depth == null ? 2.0 : this.depth.get();
    }

    public final DoubleProperty depthProperty() {
        if (this.depth == null) {
            this.depth = new SimpleDoubleProperty(this, "depth", 2.0){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Box.this, DirtyBits.MESH_GEOM);
                    Box.this.manager.invalidateBoxMesh(Box.this.key);
                    Box.this.key = null;
                    NodeHelper.geomChanged(Box.this);
                }
            };
        }
        return this.depth;
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
                    NodeHelper.markDirty(Box.this, DirtyBits.MESH_GEOM);
                    Box.this.manager.invalidateBoxMesh(Box.this.key);
                    Box.this.key = null;
                    NodeHelper.geomChanged(Box.this);
                }
            };
        }
        return this.height;
    }

    public final void setWidth(double d) {
        this.widthProperty().set(d);
    }

    public final double getWidth() {
        return this.width == null ? 2.0 : this.width.get();
    }

    public final DoubleProperty widthProperty() {
        if (this.width == null) {
            this.width = new SimpleDoubleProperty(this, "width", 2.0){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Box.this, DirtyBits.MESH_GEOM);
                    Box.this.manager.invalidateBoxMesh(Box.this.key);
                    Box.this.key = null;
                    NodeHelper.geomChanged(Box.this);
                }
            };
        }
        return this.width;
    }

    private NGNode doCreatePeer() {
        return new NGBox();
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.MESH_GEOM)) {
            NGBox nGBox = (NGBox)NodeHelper.getPeer(this);
            float f = (float)this.getWidth();
            float f2 = (float)this.getHeight();
            float f3 = (float)this.getDepth();
            if (f < 0.0f || f2 < 0.0f || f3 < 0.0f) {
                nGBox.updateMesh(null);
            } else {
                if (this.key == null) {
                    this.key = new BoxKey(f, f2, f3);
                }
                this.mesh = this.manager.getBoxMesh(f, f2, f3, this.key);
                this.mesh.updatePG();
                nGBox.updateMesh(this.mesh.getPGTriangleMesh());
            }
        }
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        float f = (float)this.getWidth();
        float f2 = (float)this.getHeight();
        float f3 = (float)this.getDepth();
        if (f < 0.0f || f2 < 0.0f || f3 < 0.0f) {
            return baseBounds.makeEmpty();
        }
        float f4 = f * 0.5f;
        float f5 = f2 * 0.5f;
        float f6 = f3 * 0.5f;
        baseBounds = baseBounds.deriveWithNewBounds(-f4, -f5, -f6, f4, f5, f6);
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private boolean doComputeContains(double d, double d2) {
        double d3 = this.getWidth();
        double d4 = this.getHeight();
        return -d3 <= d && d <= d3 && -d4 <= d2 && d2 <= d4;
    }

    private boolean doComputeIntersects(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d;
        double d2;
        double d3 = this.getWidth();
        double d4 = this.getHeight();
        double d5 = this.getDepth();
        double d6 = d3 / 2.0;
        double d7 = d4 / 2.0;
        double d8 = d5 / 2.0;
        Vec3d vec3d = pickRay.getDirectionNoClone();
        double d9 = vec3d.x == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / vec3d.x;
        double d10 = vec3d.y == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / vec3d.y;
        double d11 = vec3d.z == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / vec3d.z;
        Vec3d vec3d2 = pickRay.getOriginNoClone();
        double d12 = vec3d2.x;
        double d13 = vec3d2.y;
        double d14 = vec3d2.z;
        boolean bl = d9 < 0.0;
        boolean bl2 = d10 < 0.0;
        boolean bl3 = d11 < 0.0;
        double d15 = Double.NEGATIVE_INFINITY;
        double d16 = Double.POSITIVE_INFINITY;
        int n = 48;
        int n2 = 48;
        if (Double.isInfinite(d9)) {
            if (!(-d6 <= d12) || !(d6 >= d12)) {
                return false;
            }
        } else {
            d15 = ((bl ? d6 : -d6) - d12) * d9;
            d16 = ((bl ? -d6 : d6) - d12) * d9;
            n = bl ? 88 : 120;
            int n3 = n2 = bl ? 120 : 88;
        }
        if (Double.isInfinite(d10)) {
            if (!(-d7 <= d13) || !(d7 >= d13)) {
                return false;
            }
        } else {
            d2 = ((bl2 ? d7 : -d7) - d13) * d10;
            d = ((bl2 ? -d7 : d7) - d13) * d10;
            if (d15 > d || d2 > d16) {
                return false;
            }
            if (d2 > d15) {
                n = bl2 ? 89 : 121;
                d15 = d2;
            }
            if (d < d16) {
                n2 = bl2 ? 121 : 89;
                d16 = d;
            }
        }
        if (Double.isInfinite(d11)) {
            if (!(-d8 <= d14) || !(d8 >= d14)) {
                return false;
            }
        } else {
            d2 = ((bl3 ? d8 : -d8) - d14) * d11;
            d = ((bl3 ? -d8 : d8) - d14) * d11;
            if (d15 > d || d2 > d16) {
                return false;
            }
            if (d2 > d15) {
                n = bl3 ? 90 : 122;
                d15 = d2;
            }
            if (d < d16) {
                n2 = bl3 ? 122 : 90;
                d16 = d;
            }
        }
        int n4 = n;
        double d17 = d15;
        CullFace cullFace = this.getCullFace();
        double d18 = pickRay.getNearClip();
        double d19 = pickRay.getFarClip();
        if (d15 > d19) {
            return false;
        }
        if (d15 < d18 || cullFace == CullFace.FRONT) {
            if (d16 >= d18 && d16 <= d19 && cullFace != CullFace.BACK) {
                n4 = n2;
                d17 = d16;
            } else {
                return false;
            }
        }
        if (Double.isInfinite(d17) || Double.isNaN(d17)) {
            return false;
        }
        if (pickResultChooser != null && pickResultChooser.isCloser(d17)) {
            Point3D point3D = PickResultChooser.computePoint(pickRay, d17);
            Point2D point2D = null;
            switch (n4) {
                case 120: {
                    point2D = new Point2D(0.5 - point3D.getZ() / d5, 0.5 + point3D.getY() / d4);
                    break;
                }
                case 88: {
                    point2D = new Point2D(0.5 + point3D.getZ() / d5, 0.5 + point3D.getY() / d4);
                    break;
                }
                case 121: {
                    point2D = new Point2D(0.5 + point3D.getX() / d3, 0.5 - point3D.getZ() / d5);
                    break;
                }
                case 89: {
                    point2D = new Point2D(0.5 + point3D.getX() / d3, 0.5 + point3D.getZ() / d5);
                    break;
                }
                case 122: {
                    point2D = new Point2D(0.5 + point3D.getX() / d3, 0.5 + point3D.getY() / d4);
                    break;
                }
                case 90: {
                    point2D = new Point2D(0.5 - point3D.getX() / d3, 0.5 + point3D.getY() / d4);
                    break;
                }
                default: {
                    return false;
                }
            }
            pickResultChooser.offer(this, d17, -1, point3D, point2D);
        }
        return true;
    }

    static TriangleMesh createMesh(float f, float f2, float f3) {
        float f4 = f / 2.0f;
        float f5 = f2 / 2.0f;
        float f6 = f3 / 2.0f;
        float[] fArray = new float[]{-f4, -f5, -f6, f4, -f5, -f6, f4, f5, -f6, -f4, f5, -f6, -f4, -f5, f6, f4, -f5, f6, f4, f5, f6, -f4, f5, f6};
        float[] fArray2 = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        int[] nArray = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] nArray2 = new int[]{0, 0, 2, 2, 1, 1, 2, 2, 0, 0, 3, 3, 1, 0, 6, 2, 5, 1, 6, 2, 1, 0, 2, 3, 5, 0, 7, 2, 4, 1, 7, 2, 5, 0, 6, 3, 4, 0, 3, 2, 0, 1, 3, 2, 4, 0, 7, 3, 3, 0, 6, 2, 2, 1, 6, 2, 3, 0, 7, 3, 4, 0, 1, 2, 5, 1, 1, 2, 4, 0, 0, 3};
        TriangleMesh triangleMesh = new TriangleMesh(true);
        triangleMesh.getPoints().setAll(fArray);
        triangleMesh.getTexCoords().setAll(fArray2);
        triangleMesh.getFaces().setAll(nArray2);
        triangleMesh.getFaceSmoothingGroups().setAll(nArray);
        return triangleMesh;
    }

    static {
        BoxHelper.setBoxAccessor(new BoxHelper.BoxAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Box)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Box)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Box)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Box)node).doComputeContains(d, d2);
            }

            @Override
            public boolean doComputeIntersects(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                return ((Box)node).doComputeIntersects(pickRay, pickResultChooser);
            }
        });
    }

    private static class BoxKey
    extends Shape3D.Key {
        final double width;
        final double height;
        final double depth;

        private BoxKey(double d, double d2, double d3) {
            this.width = d;
            this.height = d2;
            this.depth = d3;
        }

        @Override
        public int hashCode() {
            long l = 7L;
            l = 31L * l + Double.doubleToLongBits(this.depth);
            l = 31L * l + Double.doubleToLongBits(this.height);
            l = 31L * l + Double.doubleToLongBits(this.width);
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
            if (!(object instanceof BoxKey)) {
                return false;
            }
            BoxKey boxKey = (BoxKey)object;
            if (Double.compare(this.depth, boxKey.depth) != 0) {
                return false;
            }
            if (Double.compare(this.height, boxKey.height) != 0) {
                return false;
            }
            return Double.compare(this.width, boxKey.width) == 0;
        }
    }
}

