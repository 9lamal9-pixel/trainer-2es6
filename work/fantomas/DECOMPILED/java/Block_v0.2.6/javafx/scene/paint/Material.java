/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.paint;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.paint.MaterialHelper;
import com.sun.javafx.sg.prism.NGPhongMaterial;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class Material {
    private final BooleanProperty dirty = new SimpleBooleanProperty(true);

    protected Material() {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = Material.class.getName();
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

    abstract void updatePG();

    abstract NGPhongMaterial getNGMaterial();

    static {
        MaterialHelper.setMaterialAccessor(new MaterialHelper.MaterialAccessor(){

            @Override
            public BooleanProperty dirtyProperty(Material material) {
                return material.dirtyProperty();
            }

            @Override
            public void updatePG(Material material) {
                material.updatePG();
            }

            @Override
            public NGPhongMaterial getNGMaterial(Material material) {
                return material.getNGMaterial();
            }
        });
    }
}

