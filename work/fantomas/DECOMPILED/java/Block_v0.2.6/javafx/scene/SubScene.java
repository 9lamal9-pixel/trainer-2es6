/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.CssFlags;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SubSceneHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.SubSceneTraversalEngine;
import com.sun.javafx.scene.traversal.TopMostTraversalEngine;
import com.sun.javafx.sg.prism.NGCamera;
import com.sun.javafx.sg.prism.NGLightBase;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGSubScene;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Paint;

public class SubScene
extends Node {
    private static boolean is3DSupported;
    private final SceneAntialiasing antiAliasing;
    private final boolean depthBuffer;
    private ObjectProperty<Parent> root;
    private ObjectProperty<Camera> camera;
    private Camera defaultCamera;
    private DoubleProperty width;
    private DoubleProperty height;
    private ObjectProperty<Paint> fill;
    private ObjectProperty<String> userAgentStylesheet;
    boolean dirtyLayout;
    private boolean dirtyNodes;
    private TopMostTraversalEngine traversalEngine;
    private int dirtyBits;
    private List<LightBase> lights;

    public SubScene(@NamedArg(value="root") Parent parent, @NamedArg(value="width") double d, @NamedArg(value="height") double d2) {
        this(parent, d, d2, false, SceneAntialiasing.DISABLED);
    }

    public SubScene(@NamedArg(value="root") Parent parent, @NamedArg(value="width") double d, @NamedArg(value="height") double d2, @NamedArg(value="depthBuffer") boolean bl, @NamedArg(value="antiAliasing") SceneAntialiasing sceneAntialiasing) {
        String string;
        SubSceneHelper.initHelper(this);
        this.userAgentStylesheet = null;
        this.dirtyLayout = false;
        this.dirtyNodes = false;
        this.traversalEngine = new SubSceneTraversalEngine(this);
        this.dirtyBits = -1;
        this.lights = new ArrayList<LightBase>();
        this.depthBuffer = bl;
        this.antiAliasing = sceneAntialiasing;
        boolean bl2 = sceneAntialiasing != null && sceneAntialiasing != SceneAntialiasing.DISABLED;
        this.setRoot(parent);
        this.setWidth(d);
        this.setHeight(d2);
        if ((bl || bl2) && !is3DSupported) {
            string = SubScene.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
        if (bl2 && !Toolkit.getToolkit().isMSAASupported()) {
            string = SubScene.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support antiAliasing");
        }
    }

    public final SceneAntialiasing getAntiAliasing() {
        return this.antiAliasing;
    }

    public final boolean isDepthBuffer() {
        return this.depthBuffer;
    }

    private boolean isDepthBufferInternal() {
        return is3DSupported ? this.depthBuffer : false;
    }

    public final void setRoot(Parent parent) {
        this.rootProperty().set(parent);
    }

    public final Parent getRoot() {
        return this.root == null ? null : (Parent)this.root.get();
    }

    public final ObjectProperty<Parent> rootProperty() {
        if (this.root == null) {
            this.root = new ObjectPropertyBase<Parent>(){
                private Parent oldRoot;

                private void forceUnbind() {
                    System.err.println("Unbinding illegal root.");
                    this.unbind();
                }

                @Override
                protected void invalidated() {
                    Parent parent = (Parent)this.get();
                    if (parent == null) {
                        if (this.isBound()) {
                            this.forceUnbind();
                        }
                        throw new NullPointerException("Scene's root cannot be null");
                    }
                    if (parent.getParent() != null) {
                        if (this.isBound()) {
                            this.forceUnbind();
                        }
                        throw new IllegalArgumentException(parent + "is already inside a scene-graph and cannot be set as root");
                    }
                    if (parent.getClipParent() != null) {
                        if (this.isBound()) {
                            this.forceUnbind();
                        }
                        throw new IllegalArgumentException(parent + "is set as a clip on another node, so cannot be set as root");
                    }
                    if (parent.getScene() != null && parent.getScene().getRoot() == parent || parent.getSubScene() != null && parent.getSubScene().getRoot() == parent && parent.getSubScene() != SubScene.this) {
                        if (this.isBound()) {
                            this.forceUnbind();
                        }
                        throw new IllegalArgumentException(parent + "is already set as root of another scene or subScene");
                    }
                    parent.setTreeVisible(SubScene.this.isTreeVisible());
                    parent.setDisabled(SubScene.this.isDisabled());
                    parent.setTreeShowing(SubScene.this.isTreeShowing());
                    if (this.oldRoot != null) {
                        StyleManager.getInstance().forget(SubScene.this);
                        this.oldRoot.setScenes(null, null);
                    }
                    this.oldRoot = parent;
                    parent.getStyleClass().add(0, "root");
                    parent.setScenes(SubScene.this.getScene(), SubScene.this);
                    SubScene.this.markDirty(SubSceneDirtyBits.ROOT_SG_DIRTY);
                    parent.resize(SubScene.this.getWidth(), SubScene.this.getHeight());
                    parent.requestLayout();
                }

                @Override
                public Object getBean() {
                    return SubScene.this;
                }

                @Override
                public String getName() {
                    return "root";
                }
            };
        }
        return this.root;
    }

    public final void setCamera(Camera camera) {
        this.cameraProperty().set(camera);
    }

    public final Camera getCamera() {
        return this.camera == null ? null : (Camera)this.camera.get();
    }

    public final ObjectProperty<Camera> cameraProperty() {
        if (this.camera == null) {
            this.camera = new ObjectPropertyBase<Camera>(){
                Camera oldCamera = null;

                @Override
                protected void invalidated() {
                    Camera camera = (Camera)this.get();
                    if (camera != null) {
                        if (camera instanceof PerspectiveCamera && !is3DSupported) {
                            String string = SubScene.class.getName();
                            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
                        }
                        if (!(camera.getScene() == null && camera.getSubScene() == null || camera.getScene() == SubScene.this.getScene() && camera.getSubScene() == SubScene.this)) {
                            throw new IllegalArgumentException(camera + "is already part of other scene or subscene");
                        }
                        camera.setOwnerSubScene(SubScene.this);
                        camera.setViewWidth(SubScene.this.getWidth());
                        camera.setViewHeight(SubScene.this.getHeight());
                    }
                    SubScene.this.markDirty(SubSceneDirtyBits.CAMERA_DIRTY);
                    if (this.oldCamera != null && this.oldCamera != camera) {
                        this.oldCamera.setOwnerSubScene(null);
                    }
                    this.oldCamera = camera;
                }

                @Override
                public Object getBean() {
                    return SubScene.this;
                }

                @Override
                public String getName() {
                    return "camera";
                }
            };
        }
        return this.camera;
    }

    Camera getEffectiveCamera() {
        Camera camera = this.getCamera();
        if (camera == null || camera instanceof PerspectiveCamera && !is3DSupported) {
            if (this.defaultCamera == null) {
                this.defaultCamera = new ParallelCamera();
                this.defaultCamera.setOwnerSubScene(this);
                this.defaultCamera.setViewWidth(this.getWidth());
                this.defaultCamera.setViewHeight(this.getHeight());
            }
            return this.defaultCamera;
        }
        return camera;
    }

    final void markContentDirty() {
        this.markDirty(SubSceneDirtyBits.CONTENT_DIRTY);
    }

    public final void setWidth(double d) {
        this.widthProperty().set(d);
    }

    public final double getWidth() {
        return this.width == null ? 0.0 : this.width.get();
    }

    public final DoubleProperty widthProperty() {
        if (this.width == null) {
            this.width = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Parent parent = SubScene.this.getRoot();
                    if (parent.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                        NodeHelper.transformsChanged(parent);
                    }
                    if (parent.isResizable()) {
                        parent.resize(this.get() - parent.getLayoutX() - parent.getTranslateX(), parent.getLayoutBounds().getHeight());
                    }
                    SubScene.this.markDirty(SubSceneDirtyBits.SIZE_DIRTY);
                    NodeHelper.geomChanged(SubScene.this);
                    SubScene.this.getEffectiveCamera().setViewWidth(this.get());
                }

                @Override
                public Object getBean() {
                    return SubScene.this;
                }

                @Override
                public String getName() {
                    return "width";
                }
            };
        }
        return this.width;
    }

    public final void setHeight(double d) {
        this.heightProperty().set(d);
    }

    public final double getHeight() {
        return this.height == null ? 0.0 : this.height.get();
    }

    public final DoubleProperty heightProperty() {
        if (this.height == null) {
            this.height = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    Parent parent = SubScene.this.getRoot();
                    if (parent.isResizable()) {
                        parent.resize(parent.getLayoutBounds().getWidth(), this.get() - parent.getLayoutY() - parent.getTranslateY());
                    }
                    SubScene.this.markDirty(SubSceneDirtyBits.SIZE_DIRTY);
                    NodeHelper.geomChanged(SubScene.this);
                    SubScene.this.getEffectiveCamera().setViewHeight(this.get());
                }

                @Override
                public Object getBean() {
                    return SubScene.this;
                }

                @Override
                public String getName() {
                    return "height";
                }
            };
        }
        return this.height;
    }

    public final void setFill(Paint paint) {
        this.fillProperty().set(paint);
    }

    public final Paint getFill() {
        return this.fill == null ? null : (Paint)this.fill.get();
    }

    public final ObjectProperty<Paint> fillProperty() {
        if (this.fill == null) {
            this.fill = new ObjectPropertyBase<Paint>(null){

                @Override
                protected void invalidated() {
                    SubScene.this.markDirty(SubSceneDirtyBits.FILL_DIRTY);
                }

                @Override
                public Object getBean() {
                    return SubScene.this;
                }

                @Override
                public String getName() {
                    return "fill";
                }
            };
        }
        return this.fill;
    }

    private void doUpdatePeer() {
        this.dirtyNodes = false;
        if (this.isDirty()) {
            NGSubScene nGSubScene = (NGSubScene)this.getPeer();
            Camera camera = this.getEffectiveCamera();
            boolean bl = false;
            if (camera.getSubScene() == null && this.isDirty(SubSceneDirtyBits.CONTENT_DIRTY)) {
                camera.syncPeer();
            }
            if (this.isDirty(SubSceneDirtyBits.FILL_DIRTY)) {
                Object object = this.getFill() == null ? null : Toolkit.getPaintAccessor().getPlatformPaint(this.getFill());
                nGSubScene.setFillPaint(object);
                bl = true;
            }
            if (this.isDirty(SubSceneDirtyBits.SIZE_DIRTY)) {
                nGSubScene.setWidth((float)this.getWidth());
                nGSubScene.setHeight((float)this.getHeight());
            }
            if (this.isDirty(SubSceneDirtyBits.CAMERA_DIRTY)) {
                nGSubScene.setCamera((NGCamera)camera.getPeer());
                bl = true;
            }
            if (this.isDirty(SubSceneDirtyBits.ROOT_SG_DIRTY)) {
                nGSubScene.setRoot((NGNode)this.getRoot().getPeer());
                bl = true;
            }
            if ((bl |= this.syncLights()) || this.isDirty(SubSceneDirtyBits.CONTENT_DIRTY)) {
                nGSubScene.markContentDirty();
            }
            this.clearDirtyBits();
        }
    }

    @Override
    void nodeResolvedOrientationChanged() {
        this.getRoot().parentResolvedOrientationInvalidated();
    }

    private void doProcessCSS() {
        if (this.cssFlag == CssFlags.CLEAN) {
            return;
        }
        if (this.getRoot().cssFlag == CssFlags.CLEAN) {
            this.getRoot().cssFlag = this.cssFlag;
        }
        SubSceneHelper.superProcessCSS(this);
        this.getRoot().processCSS();
    }

    @Override
    void processCSS() {
        Parent parent = this.getRoot();
        if (parent.isDirty(DirtyBits.NODE_CSS)) {
            parent.clearDirty(DirtyBits.NODE_CSS);
            if (this.cssFlag == CssFlags.CLEAN) {
                this.cssFlag = CssFlags.UPDATE;
            }
        }
        super.processCSS();
    }

    public final ObjectProperty<String> userAgentStylesheetProperty() {
        if (this.userAgentStylesheet == null) {
            this.userAgentStylesheet = new SimpleObjectProperty<String>((Object)this, "userAgentStylesheet", null){

                @Override
                protected void invalidated() {
                    StyleManager.getInstance().forget(SubScene.this);
                    SubScene.this.reapplyCSS();
                }
            };
        }
        return this.userAgentStylesheet;
    }

    public final String getUserAgentStylesheet() {
        return this.userAgentStylesheet == null ? null : (String)this.userAgentStylesheet.get();
    }

    public final void setUserAgentStylesheet(String string) {
        this.userAgentStylesheetProperty().set(string);
    }

    @Override
    void updateBounds() {
        super.updateBounds();
        this.getRoot().updateBounds();
    }

    private NGNode doCreatePeer() {
        if (!is3DSupported) {
            return new NGSubScene(false, false);
        }
        boolean bl = this.antiAliasing != null && this.antiAliasing != SceneAntialiasing.DISABLED;
        return new NGSubScene(this.depthBuffer, bl && Toolkit.getToolkit().isMSAASupported());
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        int n = (int)Math.ceil(this.width.get());
        int n2 = (int)Math.ceil(this.height.get());
        baseBounds = baseBounds.deriveWithNewBounds(0.0f, 0.0f, 0.0f, n, n2, 0.0f);
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    void setDirtyLayout(Parent parent) {
        if (!this.dirtyLayout && parent != null && parent.getSubScene() == this && this.getScene() != null) {
            this.dirtyLayout = true;
            this.markDirtyLayoutBranch();
            this.markDirty(SubSceneDirtyBits.CONTENT_DIRTY);
        }
    }

    void setDirty(Node node) {
        if (!this.dirtyNodes && node != null && node.getSubScene() == this && this.getScene() != null) {
            this.dirtyNodes = true;
            this.markDirty(SubSceneDirtyBits.CONTENT_DIRTY);
        }
    }

    void layoutPass() {
        if (this.dirtyLayout) {
            Parent parent = this.getRoot();
            if (parent != null) {
                parent.layout();
            }
            this.dirtyLayout = false;
        }
    }

    boolean traverse(Node node, Direction direction) {
        return this.traversalEngine.trav(node, direction) != null;
    }

    private void clearDirtyBits() {
        this.dirtyBits = 0;
    }

    private boolean isDirty() {
        return this.dirtyBits != 0;
    }

    private void setDirty(SubSceneDirtyBits subSceneDirtyBits) {
        this.dirtyBits |= subSceneDirtyBits.getMask();
    }

    private boolean isDirty(SubSceneDirtyBits subSceneDirtyBits) {
        return (this.dirtyBits & subSceneDirtyBits.getMask()) != 0;
    }

    private void markDirty(SubSceneDirtyBits subSceneDirtyBits) {
        if (!this.isDirty()) {
            NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
        }
        this.setDirty(subSceneDirtyBits);
    }

    private boolean doComputeContains(double d, double d2) {
        if (this.subSceneComputeContains(d, d2)) {
            return true;
        }
        return NodeHelper.computeContains(this.getRoot(), d, d2);
    }

    private boolean subSceneComputeContains(double d, double d2) {
        if (d < 0.0 || d2 < 0.0 || d > this.getWidth() || d2 > this.getHeight()) {
            return false;
        }
        return this.getFill() != null;
    }

    private PickResult pickRootSG(double d, double d2) {
        double d3 = this.getWidth();
        double d4 = this.getHeight();
        if (d < 0.0 || d2 < 0.0 || d > d3 || d2 > d4) {
            return null;
        }
        PickResultChooser pickResultChooser = new PickResultChooser();
        PickRay pickRay = this.getEffectiveCamera().computePickRay(d, d2, new PickRay());
        pickRay.getDirectionNoClone().normalize();
        this.getRoot().pickNode(pickRay, pickResultChooser);
        return pickResultChooser.toPickResult();
    }

    private void doPickNodeLocal(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d = this.intersectsBounds(pickRay);
        if (!Double.isNaN(d) && pickResultChooser.isCloser(d)) {
            Point3D point3D = PickResultChooser.computePoint(pickRay, d);
            PickResult pickResult = this.pickRootSG(point3D.getX(), point3D.getY());
            if (pickResult != null) {
                pickResultChooser.offerSubScenePickResult(this, pickResult, d);
            } else if (this.isPickOnBounds() || this.subSceneComputeContains(point3D.getX(), point3D.getY())) {
                pickResultChooser.offer(this, d, point3D);
            }
        }
    }

    final void addLight(LightBase lightBase) {
        if (!this.lights.contains(lightBase)) {
            this.markDirty(SubSceneDirtyBits.LIGHTS_DIRTY);
            this.lights.add(lightBase);
        }
    }

    final void removeLight(LightBase lightBase) {
        if (this.lights.remove(lightBase)) {
            this.markDirty(SubSceneDirtyBits.LIGHTS_DIRTY);
        }
    }

    private boolean syncLights() {
        boolean bl = false;
        if (!this.isDirty(SubSceneDirtyBits.LIGHTS_DIRTY)) {
            return bl;
        }
        NGSubScene nGSubScene = (NGSubScene)this.getPeer();
        NGLightBase[] nGLightBaseArray = nGSubScene.getLights();
        if (!this.lights.isEmpty() || nGLightBaseArray != null) {
            if (this.lights.isEmpty()) {
                nGSubScene.setLights(null);
            } else {
                int n;
                if (nGLightBaseArray == null || nGLightBaseArray.length < this.lights.size()) {
                    nGLightBaseArray = new NGLightBase[this.lights.size()];
                }
                for (n = 0; n < this.lights.size(); ++n) {
                    nGLightBaseArray[n] = (NGLightBase)this.lights.get(n).getPeer();
                }
                while (n < nGLightBaseArray.length && nGLightBaseArray[n] != null) {
                    nGLightBaseArray[n++] = null;
                }
                nGSubScene.setLights(nGLightBaseArray);
            }
            bl = true;
        }
        return bl;
    }

    static {
        SubSceneHelper.setSubSceneAccessor(new SubSceneHelper.SubSceneAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((SubScene)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((SubScene)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((SubScene)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((SubScene)node).doComputeContains(d, d2);
            }

            @Override
            public void doProcessCSS(Node node) {
                ((SubScene)node).doProcessCSS();
            }

            @Override
            public void doPickNodeLocal(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                ((SubScene)node).doPickNodeLocal(pickRay, pickResultChooser);
            }

            @Override
            public boolean isDepthBuffer(SubScene subScene) {
                return subScene.isDepthBufferInternal();
            }

            @Override
            public Camera getEffectiveCamera(SubScene subScene) {
                return subScene.getEffectiveCamera();
            }
        });
        is3DSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
    }

    private static enum SubSceneDirtyBits {
        SIZE_DIRTY,
        FILL_DIRTY,
        ROOT_SG_DIRTY,
        CAMERA_DIRTY,
        LIGHTS_DIRTY,
        CONTENT_DIRTY;

        private int mask = 1 << this.ordinal();

        public final int getMask() {
            return this.mask;
        }
    }
}

