/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.GeneralTransform3D;
import com.sun.javafx.scene.ParallelCameraHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGParallelCamera;
import javafx.scene.Camera;
import javafx.scene.Node;

public class ParallelCamera
extends Camera {
    public ParallelCamera() {
        ParallelCameraHelper.initHelper(this);
    }

    @Override
    Camera copy() {
        ParallelCamera parallelCamera = new ParallelCamera();
        parallelCamera.setNearClip(this.getNearClip());
        parallelCamera.setFarClip(this.getFarClip());
        return parallelCamera;
    }

    private NGNode doCreatePeer() {
        NGParallelCamera nGParallelCamera = new NGParallelCamera();
        nGParallelCamera.setNearClip((float)this.getNearClip());
        nGParallelCamera.setFarClip((float)this.getFarClip());
        return nGParallelCamera;
    }

    @Override
    final PickRay computePickRay(double d, double d2, PickRay pickRay) {
        return PickRay.computeParallelPickRay(d, d2, this.getViewHeight(), this.getCameraTransform(), this.getNearClip(), this.getFarClip(), pickRay);
    }

    @Override
    void computeProjectionTransform(GeneralTransform3D generalTransform3D) {
        double d;
        double d2 = this.getViewWidth();
        double d3 = d2 > (d = this.getViewHeight()) ? d2 / 2.0 : d / 2.0;
        generalTransform3D.ortho(0.0, d2, d, 0.0, -d3, d3);
    }

    @Override
    void computeViewTransform(Affine3D affine3D) {
        affine3D.setToIdentity();
    }

    @Override
    Vec3d computePosition(Vec3d vec3d) {
        if (vec3d == null) {
            vec3d = new Vec3d();
        }
        double d = this.getViewWidth() / 2.0;
        double d2 = this.getViewHeight() / 2.0;
        double d3 = d2 / Math.tan(Math.toRadians(15.0));
        vec3d.set(d, d2, -d3);
        return vec3d;
    }

    static {
        ParallelCameraHelper.setParallelCameraAccessor(new ParallelCameraHelper.ParallelCameraAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((ParallelCamera)node).doCreatePeer();
            }
        });
    }
}

