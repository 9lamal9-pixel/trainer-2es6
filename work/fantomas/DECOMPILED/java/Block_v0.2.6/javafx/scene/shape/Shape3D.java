/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.BoxBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.paint.MaterialHelper;
import com.sun.javafx.scene.shape.Shape3DHelper;
import com.sun.javafx.sg.prism.NGShape3D;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.PredefinedMeshManager;

public abstract class Shape3D
extends Node {
    private static final PhongMaterial DEFAULT_MATERIAL;
    PredefinedMeshManager manager = PredefinedMeshManager.getInstance();
    Key key;
    private ObjectProperty<Material> material;
    private ObjectProperty<DrawMode> drawMode;
    private ObjectProperty<CullFace> cullFace;

    protected Shape3D() {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = Shape3D.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
    }

    public final void setMaterial(Material material) {
        this.materialProperty().set(material);
    }

    public final Material getMaterial() {
        return this.material == null ? null : (Material)this.material.get();
    }

    public final ObjectProperty<Material> materialProperty() {
        if (this.material == null) {
            this.material = new SimpleObjectProperty<Material>((Object)this, "material"){
                private Material old;
                private final ChangeListener<Boolean> materialChangeListener;
                private final WeakChangeListener<Boolean> weakMaterialChangeListener;
                {
                    this.old = null;
                    this.materialChangeListener = (observableValue, bl, bl2) -> {
                        if (bl2.booleanValue()) {
                            NodeHelper.markDirty(Shape3D.this, DirtyBits.MATERIAL);
                        }
                    };
                    this.weakMaterialChangeListener = new WeakChangeListener<Boolean>(this.materialChangeListener);
                }

                @Override
                protected void invalidated() {
                    Material material;
                    if (this.old != null) {
                        MaterialHelper.dirtyProperty(this.old).removeListener(this.weakMaterialChangeListener);
                    }
                    if ((material = (Material)this.get()) != null) {
                        MaterialHelper.dirtyProperty(material).addListener(this.weakMaterialChangeListener);
                    }
                    NodeHelper.markDirty(Shape3D.this, DirtyBits.MATERIAL);
                    NodeHelper.geomChanged(Shape3D.this);
                    this.old = material;
                }
            };
        }
        return this.material;
    }

    public final void setDrawMode(DrawMode drawMode) {
        this.drawModeProperty().set(drawMode);
    }

    public final DrawMode getDrawMode() {
        return this.drawMode == null ? DrawMode.FILL : (DrawMode)((Object)this.drawMode.get());
    }

    public final ObjectProperty<DrawMode> drawModeProperty() {
        if (this.drawMode == null) {
            this.drawMode = new SimpleObjectProperty<DrawMode>((Object)this, "drawMode", DrawMode.FILL){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(Shape3D.this, DirtyBits.NODE_DRAWMODE);
                }
            };
        }
        return this.drawMode;
    }

    public final void setCullFace(CullFace cullFace) {
        this.cullFaceProperty().set(cullFace);
    }

    public final CullFace getCullFace() {
        return this.cullFace == null ? CullFace.BACK : (CullFace)((Object)this.cullFace.get());
    }

    public final ObjectProperty<CullFace> cullFaceProperty() {
        if (this.cullFace == null) {
            this.cullFace = new SimpleObjectProperty<CullFace>((Object)this, "cullFace", CullFace.BACK){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(Shape3D.this, DirtyBits.NODE_CULLFACE);
                }
            };
        }
        return this.cullFace;
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        return new BoxBounds(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    private boolean doComputeContains(double d, double d2) {
        return false;
    }

    private void doUpdatePeer() {
        NGShape3D nGShape3D = (NGShape3D)NodeHelper.getPeer(this);
        if (NodeHelper.isDirty(this, DirtyBits.MATERIAL)) {
            Material material = this.getMaterial() == null ? DEFAULT_MATERIAL : this.getMaterial();
            MaterialHelper.updatePG(material);
            nGShape3D.setMaterial(MaterialHelper.getNGMaterial(material));
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_DRAWMODE)) {
            nGShape3D.setDrawMode((Object)(this.getDrawMode() == null ? DrawMode.FILL : this.getDrawMode()));
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CULLFACE)) {
            nGShape3D.setCullFace((Object)(this.getCullFace() == null ? CullFace.BACK : this.getCullFace()));
        }
    }

    static {
        Shape3DHelper.setShape3DAccessor(new Shape3DHelper.Shape3DAccessor(){

            @Override
            public void doUpdatePeer(Node node) {
                ((Shape3D)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Shape3D)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Shape3D)node).doComputeContains(d, d2);
            }
        });
        DEFAULT_MATERIAL = new PhongMaterial();
    }

    static abstract class Key {
        Key() {
        }

        public abstract boolean equals(Object var1);

        public abstract int hashCode();
    }
}

