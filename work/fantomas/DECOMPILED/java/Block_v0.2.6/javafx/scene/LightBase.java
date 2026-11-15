/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.BoxBounds;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.LightBaseHelper;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.transform.TransformHelper;
import com.sun.javafx.sg.prism.NGLightBase;
import com.sun.javafx.tk.Toolkit;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;

public abstract class LightBase
extends Node {
    private Affine3D localToSceneTx = new Affine3D();
    private ObjectProperty<Color> color;
    private BooleanProperty lightOn;
    private ObservableList<Node> scope;

    protected LightBase() {
        this(Color.WHITE);
    }

    protected LightBase(Color color) {
        LightBaseHelper.initHelper(this);
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = LightBase.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
        this.setColor(color);
        this.localToSceneTransformProperty().addListener(observable -> NodeHelper.markDirty(this, DirtyBits.NODE_LIGHT_TRANSFORM));
    }

    public final void setColor(Color color) {
        this.colorProperty().set(color);
    }

    public final Color getColor() {
        return this.color == null ? null : (Color)this.color.get();
    }

    public final ObjectProperty<Color> colorProperty() {
        if (this.color == null) {
            this.color = new SimpleObjectProperty<Color>((Object)this, "color"){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(LightBase.this, DirtyBits.NODE_LIGHT);
                }
            };
        }
        return this.color;
    }

    public final void setLightOn(boolean bl) {
        this.lightOnProperty().set(bl);
    }

    public final boolean isLightOn() {
        return this.lightOn == null ? true : this.lightOn.get();
    }

    public final BooleanProperty lightOnProperty() {
        if (this.lightOn == null) {
            this.lightOn = new SimpleBooleanProperty(this, "lightOn", true){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(LightBase.this, DirtyBits.NODE_LIGHT);
                }
            };
        }
        return this.lightOn;
    }

    public ObservableList<Node> getScope() {
        if (this.scope == null) {
            this.scope = new TrackableObservableList<Node>(){

                @Override
                protected void onChanged(ListChangeListener.Change<Node> change) {
                    NodeHelper.markDirty(LightBase.this, DirtyBits.NODE_LIGHT_SCOPE);
                    while (change.next()) {
                        for (Node node : change.getRemoved()) {
                            if (!(node instanceof Parent) && !(node instanceof Shape3D)) continue;
                            LightBase.this.markChildrenDirty(node);
                        }
                        for (Node node : change.getAddedSubList()) {
                            if (!(node instanceof Parent) && !(node instanceof Shape3D)) continue;
                            LightBase.this.markChildrenDirty(node);
                        }
                    }
                }
            };
        }
        return this.scope;
    }

    @Override
    void scenesChanged(Scene scene, SubScene subScene, Scene scene2, SubScene subScene2) {
        if (subScene2 != null) {
            subScene2.removeLight(this);
        } else if (scene2 != null) {
            scene2.removeLight(this);
        }
        if (subScene != null) {
            subScene.addLight(this);
        } else if (scene != null) {
            scene.addLight(this);
        }
    }

    private void markOwnerDirty() {
        SubScene subScene = this.getSubScene();
        if (subScene != null) {
            subScene.markContentDirty();
        } else {
            Scene scene = this.getScene();
            if (scene != null) {
                scene.setNeedsRepaint();
            }
        }
    }

    private void markChildrenDirty(Node node) {
        if (node instanceof Shape3D) {
            NodeHelper.markDirty((Shape3D)node, DirtyBits.NODE_DRAWMODE);
        } else if (node instanceof Parent) {
            for (Node node2 : ((Parent)node).getChildren()) {
                this.markChildrenDirty(node2);
            }
        }
    }

    private void doMarkDirty(DirtyBits dirtyBits) {
        if (this.scope == null || this.getScope().isEmpty()) {
            this.markOwnerDirty();
        } else if (dirtyBits != DirtyBits.NODE_LIGHT_SCOPE) {
            ObservableList<Node> observableList = this.getScope();
            int n = observableList.size();
            for (int i = 0; i < n; ++i) {
                this.markChildrenDirty((Node)observableList.get(i));
            }
        }
    }

    private void doUpdatePeer() {
        NGLightBase nGLightBase = (NGLightBase)this.getPeer();
        if (this.isDirty(DirtyBits.NODE_LIGHT)) {
            nGLightBase.setColor(this.getColor() == null ? Toolkit.getPaintAccessor().getPlatformPaint(Color.WHITE) : Toolkit.getPaintAccessor().getPlatformPaint(this.getColor()));
            nGLightBase.setLightOn(this.isLightOn());
        }
        if (this.isDirty(DirtyBits.NODE_LIGHT_SCOPE) && this.scope != null) {
            ObservableList<Node> observableList = this.getScope();
            if (observableList.isEmpty()) {
                nGLightBase.setScope(null);
            } else {
                Object[] objectArray = new Object[observableList.size()];
                for (int i = 0; i < observableList.size(); ++i) {
                    Node node = (Node)observableList.get(i);
                    objectArray[i] = node.getPeer();
                }
                nGLightBase.setScope(objectArray);
            }
        }
        if (this.isDirty(DirtyBits.NODE_LIGHT_TRANSFORM)) {
            this.localToSceneTx.setToIdentity();
            TransformHelper.apply(this.getLocalToSceneTransform(), this.localToSceneTx);
            nGLightBase.setWorldTransform(this.localToSceneTx);
        }
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        return new BoxBounds();
    }

    private boolean doComputeContains(double d, double d2) {
        return false;
    }

    static {
        LightBaseHelper.setLightBaseAccessor(new LightBaseHelper.LightBaseAccessor(){

            @Override
            public void doMarkDirty(Node node, DirtyBits dirtyBits) {
                ((LightBase)node).doMarkDirty(dirtyBits);
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((LightBase)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((LightBase)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((LightBase)node).doComputeContains(d, d2);
            }
        });
    }
}

