/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.shape.MeshHelper;
import com.sun.javafx.sg.prism.NGTriangleMesh;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class Mesh {
    private MeshHelper meshHelper = null;
    private final BooleanProperty dirty = new SimpleBooleanProperty(true);

    protected Mesh() {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = Mesh.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
    }

    final boolean isDirty() {
        return this.dirty.getValue();
    }

    void setDirty(boolean bl) {
        this.dirty.setValue(bl);
    }

    final BooleanProperty dirtyProperty() {
        return this.dirty;
    }

    abstract NGTriangleMesh getPGMesh();

    abstract void updatePG();

    abstract BaseBounds computeBounds(BaseBounds var1);

    static {
        MeshHelper.setMeshAccessor(new MeshHelper.MeshAccessor(){

            @Override
            public MeshHelper getHelper(Mesh mesh) {
                return mesh.meshHelper;
            }

            @Override
            public void setHelper(Mesh mesh, MeshHelper meshHelper) {
                mesh.meshHelper = meshHelper;
            }
        });
    }
}

