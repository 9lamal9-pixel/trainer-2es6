/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.logging.PlatformLogger;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Camera;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Transform;

public class SnapshotParameters {
    private boolean depthBuffer;
    private Camera camera;
    private Transform transform;
    private Paint fill;
    private Rectangle2D viewport;
    Camera defaultCamera;

    public boolean isDepthBuffer() {
        return this.depthBuffer;
    }

    boolean isDepthBufferInternal() {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            return false;
        }
        return this.depthBuffer;
    }

    public void setDepthBuffer(boolean bl) {
        if (bl && !Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = SnapshotParameters.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
        this.depthBuffer = bl;
    }

    public Camera getCamera() {
        return this.camera;
    }

    Camera getEffectiveCamera() {
        if (this.camera instanceof PerspectiveCamera && !Platform.isSupported(ConditionalFeature.SCENE3D)) {
            if (this.defaultCamera == null) {
                this.defaultCamera = new ParallelCamera();
            }
            return this.defaultCamera;
        }
        return this.camera;
    }

    public void setCamera(Camera camera) {
        if (camera instanceof PerspectiveCamera && !Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = SnapshotParameters.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
        this.camera = camera;
    }

    public Transform getTransform() {
        return this.transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Paint getFill() {
        return this.fill;
    }

    public void setFill(Paint paint) {
        this.fill = paint;
    }

    public Rectangle2D getViewport() {
        return this.viewport;
    }

    public void setViewport(Rectangle2D rectangle2D) {
        this.viewport = rectangle2D;
    }

    SnapshotParameters copy() {
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.camera = this.camera == null ? null : this.camera.copy();
        snapshotParameters.depthBuffer = this.depthBuffer;
        snapshotParameters.fill = this.fill;
        snapshotParameters.viewport = this.viewport;
        snapshotParameters.transform = this.transform == null ? null : this.transform.clone();
        return snapshotParameters;
    }
}

