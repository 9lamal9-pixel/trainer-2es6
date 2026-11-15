/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

final class PredefinedMeshManager {
    private static final int INITAL_CAPACITY = 17;
    private static final float LOAD_FACTOR = 0.75f;
    private static final PredefinedMeshManager INSTANCE = new PredefinedMeshManager();
    private TriangleMeshCache boxCache = null;
    private TriangleMeshCache sphereCache = null;
    private TriangleMeshCache cylinderCache = null;

    private PredefinedMeshManager() {
    }

    static PredefinedMeshManager getInstance() {
        return INSTANCE;
    }

    synchronized TriangleMesh getBoxMesh(float f, float f2, float f3, Shape3D.Key key) {
        TriangleMesh triangleMesh;
        if (this.boxCache == null) {
            this.boxCache = BoxCacheLoader.INSTANCE;
        }
        if ((triangleMesh = this.boxCache.get(key)) == null) {
            triangleMesh = Box.createMesh(f, f2, f3);
            this.boxCache.put(key, triangleMesh);
        } else {
            triangleMesh.incRef();
        }
        return triangleMesh;
    }

    synchronized TriangleMesh getSphereMesh(float f, int n, Shape3D.Key key) {
        TriangleMesh triangleMesh;
        if (this.sphereCache == null) {
            this.sphereCache = SphereCacheLoader.INSTANCE;
        }
        if ((triangleMesh = this.sphereCache.get(key)) == null) {
            triangleMesh = Sphere.createMesh(n, f);
            this.sphereCache.put(key, triangleMesh);
        } else {
            triangleMesh.incRef();
        }
        return triangleMesh;
    }

    synchronized TriangleMesh getCylinderMesh(float f, float f2, int n, Shape3D.Key key) {
        TriangleMesh triangleMesh;
        if (this.cylinderCache == null) {
            this.cylinderCache = CylinderCacheLoader.INSTANCE;
        }
        if ((triangleMesh = this.cylinderCache.get(key)) == null) {
            triangleMesh = Cylinder.createMesh(n, f, f2);
            this.cylinderCache.put(key, triangleMesh);
        } else {
            triangleMesh.incRef();
        }
        return triangleMesh;
    }

    synchronized void invalidateBoxMesh(Shape3D.Key key) {
        if (this.boxCache != null) {
            this.boxCache.invalidateMesh(key);
        }
    }

    synchronized void invalidateSphereMesh(Shape3D.Key key) {
        if (this.sphereCache != null) {
            this.sphereCache.invalidateMesh(key);
        }
    }

    synchronized void invalidateCylinderMesh(Shape3D.Key key) {
        if (this.cylinderCache != null) {
            this.cylinderCache.invalidateMesh(key);
        }
    }

    synchronized void dispose() {
        if (this.boxCache != null) {
            this.boxCache.clear();
        }
        if (this.sphereCache != null) {
            this.sphereCache.clear();
        }
        if (this.cylinderCache != null) {
            this.cylinderCache.clear();
        }
    }

    synchronized void printStats() {
        if (this.boxCache != null) {
            this.boxCache.printStats("BoxCache");
        }
        if (this.sphereCache != null) {
            this.sphereCache.printStats("SphereCache");
        }
        if (this.cylinderCache != null) {
            this.cylinderCache.printStats("CylinderCache");
        }
    }

    void test_clearCaches() {
        INSTANCE.dispose();
    }

    int test_getBoxCacheSize() {
        return PredefinedMeshManager.INSTANCE.boxCache.size();
    }

    int test_getSphereCacheSize() {
        return PredefinedMeshManager.INSTANCE.sphereCache.size();
    }

    int test_getCylinderCacheSize() {
        return PredefinedMeshManager.INSTANCE.cylinderCache.size();
    }

    private static final class CylinderCacheLoader {
        private static final TriangleMeshCache INSTANCE = new TriangleMeshCache();

        private CylinderCacheLoader() {
        }
    }

    private static final class SphereCacheLoader {
        private static final TriangleMeshCache INSTANCE = new TriangleMeshCache();

        private SphereCacheLoader() {
        }
    }

    private static final class BoxCacheLoader {
        private static final TriangleMeshCache INSTANCE = new TriangleMeshCache();

        private BoxCacheLoader() {
        }
    }

    private static class TriangleMeshCache {
        Map<Shape3D.Key, SoftReference<TriangleMesh>> cache = new HashMap<Shape3D.Key, SoftReference<TriangleMesh>>(17, 0.75f);

        private TriangleMeshCache() {
        }

        private TriangleMesh get(Shape3D.Key key) {
            this.cleanCache();
            return this.cache.containsKey(key) ? this.cache.get(key).get() : null;
        }

        private void put(Shape3D.Key key, TriangleMesh triangleMesh) {
            this.cleanCache();
            if (triangleMesh != null) {
                this.cache.put(key, new SoftReference<TriangleMesh>(triangleMesh));
            }
        }

        private void cleanCache() {
            this.cache.values().removeIf(softReference -> softReference.get() == null);
        }

        private void clear() {
            this.cache.clear();
        }

        private int size() {
            this.cleanCache();
            return this.cache.size();
        }

        private void printStats(String string) {
            System.out.println(string + " size:    " + this.size());
        }

        private void invalidateMesh(Shape3D.Key key) {
            if (this.cache.containsKey(key)) {
                TriangleMesh triangleMesh = this.cache.get(key).get();
                if (triangleMesh != null) {
                    triangleMesh.decRef();
                    int n = triangleMesh.getRefCount();
                    if (n == 0) {
                        this.cache.remove(key);
                    }
                } else {
                    this.cache.remove(key);
                }
            }
        }
    }
}

