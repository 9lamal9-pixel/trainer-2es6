/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.BoxBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.geom.transform.GeneralTransform3D;
import com.sun.javafx.geom.transform.NoninvertibleTransformException;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.CameraHelper;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.transform.TransformHelper;
import com.sun.javafx.sg.prism.NGCamera;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.transform.Transform;

public abstract class Camera
extends Node {
    private Affine3D localToSceneTx = new Affine3D();
    private double farClipInScene;
    private double nearClipInScene;
    private Scene ownerScene;
    private SubScene ownerSubScene;
    private GeneralTransform3D projViewTx;
    private GeneralTransform3D projTx;
    private Affine3D viewTx;
    private double viewWidth;
    private double viewHeight;
    private Vec3d position;
    private boolean clipInSceneValid;
    private boolean projViewTxValid;
    private boolean localToSceneValid;
    private boolean sceneToLocalValid;
    private Affine3D sceneToLocalTx;
    private DoubleProperty nearClip;
    private DoubleProperty farClip;

    protected Camera() {
        CameraHelper.initHelper(this);
        this.ownerScene = null;
        this.ownerSubScene = null;
        this.projViewTx = new GeneralTransform3D();
        this.projTx = new GeneralTransform3D();
        this.viewTx = new Affine3D();
        this.viewWidth = 1.0;
        this.viewHeight = 1.0;
        this.position = new Vec3d();
        this.clipInSceneValid = false;
        this.projViewTxValid = false;
        this.localToSceneValid = false;
        this.sceneToLocalValid = false;
        this.sceneToLocalTx = new Affine3D();
        InvalidationListener invalidationListener = observable -> NodeHelper.markDirty(this, DirtyBits.NODE_CAMERA_TRANSFORM);
        this.localToSceneTransformProperty().addListener(invalidationListener);
        this.sceneProperty().addListener(invalidationListener);
    }

    double getFarClipInScene() {
        this.updateClipPlane();
        return this.farClipInScene;
    }

    double getNearClipInScene() {
        this.updateClipPlane();
        return this.nearClipInScene;
    }

    private void updateClipPlane() {
        if (!this.clipInSceneValid) {
            Transform transform = this.getLocalToSceneTransform();
            this.nearClipInScene = transform.transform(0.0, 0.0, this.getNearClip()).getZ();
            this.farClipInScene = transform.transform(0.0, 0.0, this.getFarClip()).getZ();
            this.clipInSceneValid = true;
        }
    }

    Affine3D getSceneToLocalTransform() {
        if (!this.sceneToLocalValid) {
            this.sceneToLocalTx.setTransform(this.getCameraTransform());
            try {
                this.sceneToLocalTx.invert();
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
                String string = Camera.class.getName();
                PlatformLogger.getLogger(string).severe("getSceneToLocalTransform", noninvertibleTransformException);
                this.sceneToLocalTx.setToIdentity();
            }
            this.sceneToLocalValid = true;
        }
        return this.sceneToLocalTx;
    }

    public final void setNearClip(double d) {
        this.nearClipProperty().set(d);
    }

    public final double getNearClip() {
        return this.nearClip == null ? 0.1 : this.nearClip.get();
    }

    public final DoubleProperty nearClipProperty() {
        if (this.nearClip == null) {
            this.nearClip = new SimpleDoubleProperty(this, "nearClip", 0.1){

                @Override
                protected void invalidated() {
                    Camera.this.clipInSceneValid = false;
                    NodeHelper.markDirty(Camera.this, DirtyBits.NODE_CAMERA);
                }
            };
        }
        return this.nearClip;
    }

    public final void setFarClip(double d) {
        this.farClipProperty().set(d);
    }

    public final double getFarClip() {
        return this.farClip == null ? 100.0 : this.farClip.get();
    }

    public final DoubleProperty farClipProperty() {
        if (this.farClip == null) {
            this.farClip = new SimpleDoubleProperty(this, "farClip", 100.0){

                @Override
                protected void invalidated() {
                    Camera.this.clipInSceneValid = false;
                    NodeHelper.markDirty(Camera.this, DirtyBits.NODE_CAMERA);
                }
            };
        }
        return this.farClip;
    }

    Camera copy() {
        return this;
    }

    private void doUpdatePeer() {
        NGCamera nGCamera = (NGCamera)this.getPeer();
        if (!NodeHelper.isDirtyEmpty(this)) {
            if (this.isDirty(DirtyBits.NODE_CAMERA)) {
                nGCamera.setNearClip((float)this.getNearClip());
                nGCamera.setFarClip((float)this.getFarClip());
                nGCamera.setViewWidth(this.getViewWidth());
                nGCamera.setViewHeight(this.getViewHeight());
            }
            if (this.isDirty(DirtyBits.NODE_CAMERA_TRANSFORM)) {
                nGCamera.setWorldTransform(this.getCameraTransform());
            }
            nGCamera.setProjViewTransform(this.getProjViewTransform());
            this.position = this.computePosition(this.position);
            this.getCameraTransform().transform(this.position, this.position);
            nGCamera.setPosition(this.position);
        }
    }

    void setViewWidth(double d) {
        this.viewWidth = d;
        NodeHelper.markDirty(this, DirtyBits.NODE_CAMERA);
    }

    double getViewWidth() {
        return this.viewWidth;
    }

    void setViewHeight(double d) {
        this.viewHeight = d;
        NodeHelper.markDirty(this, DirtyBits.NODE_CAMERA);
    }

    double getViewHeight() {
        return this.viewHeight;
    }

    void setOwnerScene(Scene scene) {
        if (scene == null) {
            this.ownerScene = null;
        } else if (scene != this.ownerScene) {
            if (this.ownerScene != null || this.ownerSubScene != null) {
                throw new IllegalArgumentException(this + "is already set as camera in other scene or subscene");
            }
            this.ownerScene = scene;
            this.markOwnerDirty();
        }
    }

    void setOwnerSubScene(SubScene subScene) {
        if (subScene == null) {
            this.ownerSubScene = null;
        } else if (subScene != this.ownerSubScene) {
            if (this.ownerScene != null || this.ownerSubScene != null) {
                throw new IllegalArgumentException(this + "is already set as camera in other scene or subscene");
            }
            this.ownerSubScene = subScene;
            this.markOwnerDirty();
        }
    }

    private void doMarkDirty(DirtyBits dirtyBits) {
        if (dirtyBits == DirtyBits.NODE_CAMERA_TRANSFORM) {
            this.localToSceneValid = false;
            this.sceneToLocalValid = false;
            this.clipInSceneValid = false;
            this.projViewTxValid = false;
        } else if (dirtyBits == DirtyBits.NODE_CAMERA) {
            this.projViewTxValid = false;
        }
        this.markOwnerDirty();
    }

    private void markOwnerDirty() {
        if (this.ownerScene != null) {
            this.ownerScene.markCameraDirty();
        }
        if (this.ownerSubScene != null) {
            this.ownerSubScene.markContentDirty();
        }
    }

    Affine3D getCameraTransform() {
        if (!this.localToSceneValid) {
            this.localToSceneTx.setToIdentity();
            TransformHelper.apply(this.getLocalToSceneTransform(), this.localToSceneTx);
            this.localToSceneValid = true;
        }
        return this.localToSceneTx;
    }

    abstract void computeProjectionTransform(GeneralTransform3D var1);

    abstract void computeViewTransform(Affine3D var1);

    GeneralTransform3D getProjViewTransform() {
        if (!this.projViewTxValid) {
            this.computeProjectionTransform(this.projTx);
            this.computeViewTransform(this.viewTx);
            this.projViewTx.set(this.projTx);
            this.projViewTx.mul(this.viewTx);
            this.projViewTx.mul(this.getSceneToLocalTransform());
            this.projViewTxValid = true;
        }
        return this.projViewTx;
    }

    private Point2D project(Point3D point3D) {
        Vec3d vec3d = this.getProjViewTransform().transform(new Vec3d(point3D.getX(), point3D.getY(), point3D.getZ()));
        double d = this.getViewWidth() / 2.0;
        double d2 = this.getViewHeight() / 2.0;
        return new Point2D(d * (1.0 + vec3d.x), d2 * (1.0 - vec3d.y));
    }

    private Point2D pickNodeXYPlane(Node node, double d, double d2) {
        PickRay pickRay = this.computePickRay(d, d2, null);
        Affine3D affine3D = new Affine3D();
        TransformHelper.apply(node.getLocalToSceneTransform(), affine3D);
        Vec3d vec3d = pickRay.getOriginNoClone();
        Vec3d vec3d2 = pickRay.getDirectionNoClone();
        try {
            affine3D.inverseTransform(vec3d, vec3d);
            affine3D.inverseDeltaTransform(vec3d2, vec3d2);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
        if (Camera.almostZero(vec3d2.z)) {
            return null;
        }
        double d3 = -vec3d.z / vec3d2.z;
        return new Point2D(vec3d.x + vec3d2.x * d3, vec3d.y + vec3d2.y * d3);
    }

    Point3D pickProjectPlane(double d, double d2) {
        PickRay pickRay = this.computePickRay(d, d2, null);
        Vec3d vec3d = new Vec3d();
        vec3d.add(pickRay.getOriginNoClone(), pickRay.getDirectionNoClone());
        return new Point3D(vec3d.x, vec3d.y, vec3d.z);
    }

    abstract PickRay computePickRay(double var1, double var3, PickRay var5);

    abstract Vec3d computePosition(Vec3d var1);

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        return new BoxBounds(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    private boolean doComputeContains(double d, double d2) {
        return false;
    }

    static {
        CameraHelper.setCameraAccessor(new CameraHelper.CameraAccessor(){

            @Override
            public void doMarkDirty(Node node, DirtyBits dirtyBits) {
                ((Camera)node).doMarkDirty(dirtyBits);
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Camera)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Camera)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Camera)node).doComputeContains(d, d2);
            }

            @Override
            public Point2D project(Camera camera, Point3D point3D) {
                return camera.project(point3D);
            }

            @Override
            public Point2D pickNodeXYPlane(Camera camera, Node node, double d, double d2) {
                return camera.pickNodeXYPlane(node, d, d2);
            }

            @Override
            public Point3D pickProjectPlane(Camera camera, double d, double d2) {
                return camera.pickProjectPlane(d, d2);
            }
        });
    }
}

