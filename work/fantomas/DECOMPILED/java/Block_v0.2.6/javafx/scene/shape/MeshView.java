/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.scene.shape.MeshHelper;
import com.sun.javafx.scene.shape.MeshViewHelper;
import com.sun.javafx.sg.prism.NGMeshView;
import com.sun.javafx.sg.prism.NGNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.Shape3D;

public class MeshView
extends Shape3D {
    private ObjectProperty<Mesh> mesh;

    public MeshView() {
        MeshViewHelper.initHelper(this);
    }

    public MeshView(Mesh mesh) {
        MeshViewHelper.initHelper(this);
        this.setMesh(mesh);
    }

    public final void setMesh(Mesh mesh) {
        this.meshProperty().set(mesh);
    }

    public final Mesh getMesh() {
        return this.mesh == null ? null : (Mesh)this.mesh.get();
    }

    public final ObjectProperty<Mesh> meshProperty() {
        if (this.mesh == null) {
            this.mesh = new SimpleObjectProperty<Mesh>((Object)this, "mesh"){
                private Mesh old;
                private final ChangeListener<Boolean> meshChangeListener;
                private final WeakChangeListener<Boolean> weakMeshChangeListener;
                {
                    this.old = null;
                    this.meshChangeListener = (observableValue, bl, bl2) -> {
                        if (bl2.booleanValue()) {
                            NodeHelper.markDirty(MeshView.this, DirtyBits.MESH_GEOM);
                            NodeHelper.geomChanged(MeshView.this);
                        }
                    };
                    this.weakMeshChangeListener = new WeakChangeListener<Boolean>(this.meshChangeListener);
                }

                @Override
                protected void invalidated() {
                    Mesh mesh;
                    if (this.old != null) {
                        this.old.dirtyProperty().removeListener(this.weakMeshChangeListener);
                    }
                    if ((mesh = (Mesh)this.get()) != null) {
                        mesh.dirtyProperty().addListener(this.weakMeshChangeListener);
                    }
                    NodeHelper.markDirty(MeshView.this, DirtyBits.MESH);
                    NodeHelper.markDirty(MeshView.this, DirtyBits.MESH_GEOM);
                    NodeHelper.geomChanged(MeshView.this);
                    this.old = mesh;
                }
            };
        }
        return this.mesh;
    }

    private void doUpdatePeer() {
        NGMeshView nGMeshView = (NGMeshView)NodeHelper.getPeer(this);
        if (NodeHelper.isDirty(this, DirtyBits.MESH_GEOM) && this.getMesh() != null) {
            this.getMesh().updatePG();
        }
        if (NodeHelper.isDirty(this, DirtyBits.MESH)) {
            nGMeshView.setMesh(this.getMesh() == null ? null : this.getMesh().getPGMesh());
        }
    }

    private NGNode doCreatePeer() {
        return new NGMeshView();
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        if (this.getMesh() != null) {
            baseBounds = this.getMesh().computeBounds(baseBounds);
            baseBounds = baseTransform.transform(baseBounds, baseBounds);
        } else {
            baseBounds.makeEmpty();
        }
        return baseBounds;
    }

    private boolean doComputeContains(double d, double d2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean doComputeIntersects(PickRay pickRay, PickResultChooser pickResultChooser) {
        return MeshHelper.computeIntersects(this.getMesh(), pickRay, pickResultChooser, this, this.getCullFace(), true);
    }

    static {
        MeshViewHelper.setMeshViewAccessor(new MeshViewHelper.MeshViewAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((MeshView)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((MeshView)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((MeshView)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((MeshView)node).doComputeContains(d, d2);
            }

            @Override
            public boolean doComputeIntersects(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                return ((MeshView)node).doComputeIntersects(pickRay, pickResultChooser);
            }
        });
    }
}

