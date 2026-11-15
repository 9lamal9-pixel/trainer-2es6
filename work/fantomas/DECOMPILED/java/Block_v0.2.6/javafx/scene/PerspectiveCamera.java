/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.GeneralTransform3D;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.PerspectiveCameraHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGPerspectiveCamera;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Camera;
import javafx.scene.Node;

public class PerspectiveCamera
extends Camera {
    private boolean fixedEyeAtCameraZero = false;
    private static final Affine3D LOOK_AT_TX = new Affine3D();
    private static final Affine3D LOOK_AT_TX_FIXED_EYE = new Affine3D();
    private DoubleProperty fieldOfView;
    private BooleanProperty verticalFieldOfView;

    public final void setFieldOfView(double d) {
        this.fieldOfViewProperty().set(d);
    }

    public final double getFieldOfView() {
        return this.fieldOfView == null ? 30.0 : this.fieldOfView.get();
    }

    public final DoubleProperty fieldOfViewProperty() {
        if (this.fieldOfView == null) {
            this.fieldOfView = new SimpleDoubleProperty(this, "fieldOfView", 30.0){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(PerspectiveCamera.this, DirtyBits.NODE_CAMERA);
                }
            };
        }
        return this.fieldOfView;
    }

    public final void setVerticalFieldOfView(boolean bl) {
        this.verticalFieldOfViewProperty().set(bl);
    }

    public final boolean isVerticalFieldOfView() {
        return this.verticalFieldOfView == null ? true : this.verticalFieldOfView.get();
    }

    public final BooleanProperty verticalFieldOfViewProperty() {
        if (this.verticalFieldOfView == null) {
            this.verticalFieldOfView = new SimpleBooleanProperty(this, "verticalFieldOfView", true){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(PerspectiveCamera.this, DirtyBits.NODE_CAMERA);
                }
            };
        }
        return this.verticalFieldOfView;
    }

    public PerspectiveCamera() {
        this(false);
    }

    public PerspectiveCamera(boolean bl) {
        PerspectiveCameraHelper.initHelper(this);
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = PerspectiveCamera.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
        this.fixedEyeAtCameraZero = bl;
    }

    public final boolean isFixedEyeAtCameraZero() {
        return this.fixedEyeAtCameraZero;
    }

    @Override
    final PickRay computePickRay(double d, double d2, PickRay pickRay) {
        return PickRay.computePerspectivePickRay(d, d2, this.fixedEyeAtCameraZero, this.getViewWidth(), this.getViewHeight(), Math.toRadians(this.getFieldOfView()), this.isVerticalFieldOfView(), this.getCameraTransform(), this.getNearClip(), this.getFarClip(), pickRay);
    }

    @Override
    Camera copy() {
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera(this.fixedEyeAtCameraZero);
        perspectiveCamera.setNearClip(this.getNearClip());
        perspectiveCamera.setFarClip(this.getFarClip());
        perspectiveCamera.setFieldOfView(this.getFieldOfView());
        return perspectiveCamera;
    }

    private NGNode doCreatePeer() {
        NGPerspectiveCamera nGPerspectiveCamera = new NGPerspectiveCamera(this.fixedEyeAtCameraZero);
        nGPerspectiveCamera.setNearClip((float)this.getNearClip());
        nGPerspectiveCamera.setFarClip((float)this.getFarClip());
        nGPerspectiveCamera.setFieldOfView((float)this.getFieldOfView());
        return nGPerspectiveCamera;
    }

    private void doUpdatePeer() {
        NGPerspectiveCamera nGPerspectiveCamera = (NGPerspectiveCamera)this.getPeer();
        if (this.isDirty(DirtyBits.NODE_CAMERA)) {
            nGPerspectiveCamera.setVerticalFieldOfView(this.isVerticalFieldOfView());
            nGPerspectiveCamera.setFieldOfView((float)this.getFieldOfView());
        }
    }

    @Override
    void computeProjectionTransform(GeneralTransform3D generalTransform3D) {
        generalTransform3D.perspective(this.isVerticalFieldOfView(), Math.toRadians(this.getFieldOfView()), this.getViewWidth() / this.getViewHeight(), this.getNearClip(), this.getFarClip());
    }

    @Override
    void computeViewTransform(Affine3D affine3D) {
        if (this.isFixedEyeAtCameraZero()) {
            affine3D.setTransform(LOOK_AT_TX_FIXED_EYE);
        } else {
            double d = this.getViewWidth();
            double d2 = this.getViewHeight();
            boolean bl = this.isVerticalFieldOfView();
            double d3 = d / d2;
            double d4 = Math.tan(Math.toRadians(this.getFieldOfView()) / 2.0);
            double d5 = -d4 * (bl ? d3 : 1.0);
            double d6 = d4 * (bl ? 1.0 : 1.0 / d3);
            double d7 = 2.0 * d4 / (bl ? d2 : d);
            affine3D.setToTranslation(d5, d6, 0.0);
            affine3D.concatenate(LOOK_AT_TX);
            affine3D.scale(d7, d7, d7);
        }
    }

    @Override
    Vec3d computePosition(Vec3d vec3d) {
        if (vec3d == null) {
            vec3d = new Vec3d();
        }
        if (this.fixedEyeAtCameraZero) {
            vec3d.set(0.0, 0.0, 0.0);
        } else {
            double d = this.getViewWidth() / 2.0;
            double d2 = this.getViewHeight() / 2.0;
            double d3 = this.isVerticalFieldOfView() ? d2 : d;
            double d4 = d3 / Math.tan(Math.toRadians(this.getFieldOfView() / 2.0));
            vec3d.set(d, d2, -d4);
        }
        return vec3d;
    }

    static {
        PerspectiveCameraHelper.setPerspectiveCameraAccessor(new PerspectiveCameraHelper.PerspectiveCameraAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((PerspectiveCamera)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((PerspectiveCamera)node).doUpdatePeer();
            }
        });
        LOOK_AT_TX.setToTranslation(0.0, 0.0, -1.0);
        LOOK_AT_TX.rotate(Math.PI, 1.0, 0.0, 0.0);
        LOOK_AT_TX_FIXED_EYE.rotate(Math.PI, 1.0, 0.0, 0.0);
    }
}

