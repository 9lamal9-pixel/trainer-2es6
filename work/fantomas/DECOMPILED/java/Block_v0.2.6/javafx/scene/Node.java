/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.glass.ui.Accessible;
import com.sun.glass.ui.Application;
import com.sun.javafx.beans.IDProperty;
import com.sun.javafx.beans.event.AbstractNotifyListener;
import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.UnmodifiableListSet;
import com.sun.javafx.css.PseudoClassState;
import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.BoxBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.geom.transform.GeneralTransform3D;
import com.sun.javafx.geom.transform.NoninvertibleTransformException;
import com.sun.javafx.geometry.BoundsUtils;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.scene.BoundsAccessor;
import com.sun.javafx.scene.CameraHelper;
import com.sun.javafx.scene.CssFlags;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.EventHandlerProperties;
import com.sun.javafx.scene.LayoutFlags;
import com.sun.javafx.scene.NodeEventDispatcher;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.SceneUtils;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.scene.transform.TransformHelper;
import com.sun.javafx.scene.transform.TransformUtils;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Logging;
import com.sun.javafx.util.TempState;
import com.sun.javafx.util.Utils;
import com.sun.prism.impl.PrismSettings;
import com.sun.scenario.effect.EffectHelper;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.PseudoClass;
import javafx.css.Selector;
import javafx.css.Style;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.CursorConverter;
import javafx.css.converter.EffectConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.CacheHint;
import javafx.scene.Camera;
import javafx.scene.CssStyleHelper;
import javafx.scene.Cursor;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.SubScene;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ZoomEvent;
import javafx.scene.shape.Shape3D;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Window;
import javafx.util.Callback;

@IDProperty(value="id")
public abstract class Node
implements EventTarget,
Styleable {
    private NodeHelper nodeHelper = null;
    private int dirtyBits;
    private BaseBounds _geomBounds = new RectBounds(0.0f, 0.0f, -1.0f, -1.0f);
    private BaseBounds _txBounds = new RectBounds(0.0f, 0.0f, -1.0f, -1.0f);
    private boolean pendingUpdateBounds = false;
    private static final Object USER_DATA_KEY;
    private ObservableMap<Object, Object> properties;
    private ReadOnlyObjectWrapper<Parent> parent;
    private final InvalidationListener parentDisabledChangedListener = observable -> this.updateDisabled();
    private final InvalidationListener parentTreeVisibleChangedListener = observable -> this.updateTreeVisible(true);
    private final ChangeListener<Boolean> windowShowingChangedListener = (observableValue, bl, bl2) -> this.updateTreeShowing();
    private final ChangeListener<Window> sceneWindowChangedListener = (observableValue, window, window2) -> {
        if (window != null) {
            window.showingProperty().removeListener(this.windowShowingChangedListener);
        }
        if (window2 != null) {
            window2.showingProperty().addListener(this.windowShowingChangedListener);
        }
        this.updateTreeShowing();
    };
    private SubScene subScene = null;
    private ReadOnlyObjectWrapperManualFire<Scene> scene = new ReadOnlyObjectWrapperManualFire();
    private StringProperty id;
    private ObservableList<String> styleClass = new TrackableObservableList<String>(){

        @Override
        protected void onChanged(ListChangeListener.Change<String> change) {
            Node.this.reapplyCSS();
        }

        @Override
        public String toString() {
            if (this.size() == 0) {
                return "";
            }
            if (this.size() == 1) {
                return (String)this.get(0);
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < this.size(); ++i) {
                stringBuilder.append((String)this.get(i));
                if (i + 1 >= this.size()) continue;
                stringBuilder.append(' ');
            }
            return stringBuilder.toString();
        }
    };
    private StringProperty style;
    private BooleanProperty visible;
    private DoubleProperty opacity;
    private ObjectProperty<BlendMode> blendMode;
    private boolean derivedDepthTest = true;
    private BooleanProperty pickOnBounds;
    private ReadOnlyBooleanWrapper disabled;
    private Node clipParent;
    private NGNode peer;
    private BooleanProperty managed;
    private DoubleProperty layoutX;
    private DoubleProperty layoutY;
    public static final double BASELINE_OFFSET_SAME_AS_HEIGHT = Double.NEGATIVE_INFINITY;
    private LazyBoundsProperty layoutBounds = new LazyBoundsProperty(){

        @Override
        protected Bounds computeBounds() {
            return NodeHelper.computeLayoutBounds(Node.this);
        }

        @Override
        public Object getBean() {
            return Node.this;
        }

        @Override
        public String getName() {
            return "layoutBounds";
        }
    };
    private BaseTransform localToParentTx = BaseTransform.IDENTITY_TRANSFORM;
    private boolean transformDirty = true;
    private BaseBounds txBounds = new RectBounds();
    private BaseBounds geomBounds = new RectBounds();
    private BaseBounds localBounds = null;
    boolean boundsChanged;
    private boolean geomBoundsInvalid = true;
    private boolean localBoundsInvalid = true;
    private boolean txBoundsInvalid = true;
    private static final double EPSILON_ABSOLUTE = 1.0E-5;
    private NodeTransformation nodeTransformation;
    private static final double DEFAULT_TRANSLATE_X = 0.0;
    private static final double DEFAULT_TRANSLATE_Y = 0.0;
    private static final double DEFAULT_TRANSLATE_Z = 0.0;
    private static final double DEFAULT_SCALE_X = 1.0;
    private static final double DEFAULT_SCALE_Y = 1.0;
    private static final double DEFAULT_SCALE_Z = 1.0;
    private static final double DEFAULT_ROTATE = 0.0;
    private static final Point3D DEFAULT_ROTATION_AXIS;
    private EventHandlerProperties eventHandlerProperties;
    private ObjectProperty<NodeOrientation> nodeOrientation;
    private EffectiveOrientationProperty effectiveNodeOrientationProperty;
    private static final byte EFFECTIVE_ORIENTATION_LTR = 0;
    private static final byte EFFECTIVE_ORIENTATION_RTL = 1;
    private static final byte EFFECTIVE_ORIENTATION_MASK = 1;
    private static final byte AUTOMATIC_ORIENTATION_LTR = 0;
    private static final byte AUTOMATIC_ORIENTATION_RTL = 2;
    private static final byte AUTOMATIC_ORIENTATION_MASK = 2;
    private byte resolvedNodeOrientation = 0;
    private MiscProperties miscProperties;
    private static final double DEFAULT_VIEW_ORDER = 0.0;
    private static final boolean DEFAULT_CACHE = false;
    private static final CacheHint DEFAULT_CACHE_HINT;
    private static final Node DEFAULT_CLIP;
    private static final Cursor DEFAULT_CURSOR;
    private static final DepthTest DEFAULT_DEPTH_TEST;
    private static final boolean DEFAULT_DISABLE = false;
    private static final Effect DEFAULT_EFFECT;
    private static final InputMethodRequests DEFAULT_INPUT_METHOD_REQUESTS;
    private static final boolean DEFAULT_MOUSE_TRANSPARENT = false;
    private ReadOnlyBooleanWrapper hover;
    private ReadOnlyBooleanWrapper pressed;
    private FocusedProperty focused;
    private BooleanProperty focusTraversable;
    private boolean treeShowing;
    private TreeShowingPropertyReadOnly treeShowingRO;
    private boolean treeVisible;
    private TreeVisiblePropertyReadOnly treeVisibleRO;
    private boolean canReceiveFocus = false;
    private BooleanProperty showMnemonics;
    private Node labeledBy = null;
    private ObjectProperty<EventDispatcher> eventDispatcher;
    private NodeEventDispatcher internalEventDispatcher;
    private EventDispatcher preprocessMouseEventDispatcher;
    CssFlags cssFlag = CssFlags.CLEAN;
    final ObservableSet<PseudoClass> pseudoClassStates = new PseudoClassState();
    CssStyleHelper styleHelper;
    private static final PseudoClass HOVER_PSEUDOCLASS_STATE;
    private static final PseudoClass PRESSED_PSEUDOCLASS_STATE;
    private static final PseudoClass DISABLED_PSEUDOCLASS_STATE;
    private static final PseudoClass FOCUSED_PSEUDOCLASS_STATE;
    private static final PseudoClass SHOW_MNEMONICS_PSEUDOCLASS_STATE;
    private static final BoundsAccessor boundsAccessor;
    private ObjectProperty<AccessibleRole> accessibleRole;
    AccessibilityProperties accessibilityProperties;
    Accessible accessible;

    private void doMarkDirty(DirtyBits dirtyBits) {
        if (this.isDirtyEmpty()) {
            this.addToSceneDirtyList();
        }
        this.dirtyBits = (int)((long)this.dirtyBits | dirtyBits.getMask());
    }

    private void addToSceneDirtyList() {
        Scene scene = this.getScene();
        if (scene != null) {
            scene.addToDirtyList(this);
            if (this.getSubScene() != null) {
                this.getSubScene().setDirty(this);
            }
        }
    }

    final boolean isDirty(DirtyBits dirtyBits) {
        return ((long)this.dirtyBits & dirtyBits.getMask()) != 0L;
    }

    final void clearDirty(DirtyBits dirtyBits) {
        this.dirtyBits = (int)((long)this.dirtyBits & (dirtyBits.getMask() ^ 0xFFFFFFFFFFFFFFFFL));
    }

    private void setDirty() {
        this.dirtyBits = -1;
    }

    private void clearDirty() {
        this.dirtyBits = 0;
    }

    final boolean isDirtyEmpty() {
        return this.dirtyBits == 0;
    }

    final void syncPeer() {
        if (!this.isDirtyEmpty() && (this.treeVisible || this.isDirty(DirtyBits.NODE_VISIBLE) || this.isDirty(DirtyBits.NODE_FORCE_SYNC))) {
            NodeHelper.updatePeer(this);
            this.clearDirty();
        }
    }

    void updateBounds() {
        Node node = this.getClip();
        if (node != null) {
            node.updateBounds();
        }
        if (!this.treeVisible && !this.isDirty(DirtyBits.NODE_VISIBLE)) {
            if (this.isDirty(DirtyBits.NODE_TRANSFORM) || this.isDirty(DirtyBits.NODE_TRANSFORMED_BOUNDS) || this.isDirty(DirtyBits.NODE_BOUNDS)) {
                this.pendingUpdateBounds = true;
            }
            return;
        }
        if (this.pendingUpdateBounds) {
            NodeHelper.markDirty(this, DirtyBits.NODE_TRANSFORM);
            NodeHelper.markDirty(this, DirtyBits.NODE_TRANSFORMED_BOUNDS);
            NodeHelper.markDirty(this, DirtyBits.NODE_BOUNDS);
            this.pendingUpdateBounds = false;
        }
        if (this.isDirty(DirtyBits.NODE_TRANSFORM) || this.isDirty(DirtyBits.NODE_TRANSFORMED_BOUNDS)) {
            if (this.isDirty(DirtyBits.NODE_TRANSFORM)) {
                this.updateLocalToParentTransform();
            }
            this._txBounds = this.getTransformedBounds(this._txBounds, BaseTransform.IDENTITY_TRANSFORM);
        }
        if (this.isDirty(DirtyBits.NODE_BOUNDS)) {
            this._geomBounds = this.getGeomBounds(this._geomBounds, BaseTransform.IDENTITY_TRANSFORM);
        }
    }

    private void doUpdatePeer() {
        Object object;
        Object p = this.getPeer();
        if (PrismSettings.printRenderGraph && this.isDirty(DirtyBits.DEBUG)) {
            object = this.getId();
            String string = this.getClass().getSimpleName();
            if (string.isEmpty()) {
                string = this.getClass().getName();
            }
            ((NGNode)p).setName((String)(object == null ? string : object + "(" + string + ")"));
        }
        if (this.isDirty(DirtyBits.NODE_TRANSFORM)) {
            ((NGNode)p).setTransformMatrix(this.localToParentTx);
        }
        if (this.isDirty(DirtyBits.NODE_VIEW_ORDER)) {
            ((NGNode)p).setViewOrder(this.getViewOrder());
        }
        if (this.isDirty(DirtyBits.NODE_BOUNDS)) {
            ((NGNode)p).setContentBounds(this._geomBounds);
        }
        if (this.isDirty(DirtyBits.NODE_TRANSFORMED_BOUNDS)) {
            ((NGNode)p).setTransformedBounds(this._txBounds, !this.isDirty(DirtyBits.NODE_BOUNDS));
        }
        if (this.isDirty(DirtyBits.NODE_OPACITY)) {
            ((NGNode)p).setOpacity((float)Utils.clamp(0.0, this.getOpacity(), 1.0));
        }
        if (this.isDirty(DirtyBits.NODE_CACHE)) {
            ((NGNode)p).setCachedAsBitmap(this.isCache(), this.getCacheHint());
        }
        if (this.isDirty(DirtyBits.NODE_CLIP)) {
            ((NGNode)p).setClipNode(this.getClip() != null ? (NGNode)this.getClip().getPeer() : null);
        }
        if (this.isDirty(DirtyBits.EFFECT_EFFECT) && this.getEffect() != null) {
            EffectHelper.sync(this.getEffect());
            ((NGNode)p).effectChanged();
        }
        if (this.isDirty(DirtyBits.NODE_EFFECT)) {
            ((NGNode)p).setEffect(this.getEffect() != null ? EffectHelper.getPeer(this.getEffect()) : null);
        }
        if (this.isDirty(DirtyBits.NODE_VISIBLE)) {
            ((NGNode)p).setVisible(this.isVisible());
        }
        if (this.isDirty(DirtyBits.NODE_DEPTH_TEST)) {
            ((NGNode)p).setDepthTest(this.isDerivedDepthTest());
        }
        if (this.isDirty(DirtyBits.NODE_BLENDMODE)) {
            object = this.getBlendMode();
            ((NGNode)p).setNodeBlendMode(object == null ? null : EffectHelper.getToolkitBlendMode((BlendMode)((Object)object)));
        }
    }

    public final ObservableMap<Object, Object> getProperties() {
        if (this.properties == null) {
            this.properties = FXCollections.observableMap(new HashMap());
        }
        return this.properties;
    }

    public boolean hasProperties() {
        return this.properties != null && !this.properties.isEmpty();
    }

    public void setUserData(Object object) {
        this.getProperties().put(USER_DATA_KEY, object);
    }

    public Object getUserData() {
        return this.getProperties().get(USER_DATA_KEY);
    }

    final void setParent(Parent parent) {
        this.parentPropertyImpl().set(parent);
    }

    public final Parent getParent() {
        return this.parent == null ? null : (Parent)this.parent.get();
    }

    public final ReadOnlyObjectProperty<Parent> parentProperty() {
        return this.parentPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Parent> parentPropertyImpl() {
        if (this.parent == null) {
            this.parent = new ReadOnlyObjectWrapper<Parent>(){
                private Parent oldParent;

                @Override
                protected void invalidated() {
                    if (this.oldParent != null) {
                        this.oldParent.disabledProperty().removeListener(Node.this.parentDisabledChangedListener);
                        this.oldParent.treeVisibleProperty().removeListener(Node.this.parentTreeVisibleChangedListener);
                        if (Node.this.nodeTransformation != null && Node.this.nodeTransformation.listenerReasons > 0) {
                            this.oldParent.localToSceneTransformProperty().removeListener(Node.this.nodeTransformation.getLocalToSceneInvalidationListener());
                        }
                    }
                    Node.this.updateDisabled();
                    Node.this.computeDerivedDepthTest();
                    Parent parent = (Parent)this.get();
                    if (parent != null) {
                        parent.disabledProperty().addListener(Node.this.parentDisabledChangedListener);
                        parent.treeVisibleProperty().addListener(Node.this.parentTreeVisibleChangedListener);
                        if (Node.this.nodeTransformation != null && Node.this.nodeTransformation.listenerReasons > 0) {
                            parent.localToSceneTransformProperty().addListener(Node.this.nodeTransformation.getLocalToSceneInvalidationListener());
                        }
                        Node.this.reapplyCSS();
                    } else {
                        Node.this.cssFlag = CssFlags.CLEAN;
                    }
                    Node.this.updateTreeVisible(true);
                    this.oldParent = parent;
                    Node.this.invalidateLocalToSceneTransform();
                    Node.this.parentResolvedOrientationInvalidated();
                    Node.this.notifyAccessibleAttributeChanged(AccessibleAttribute.PARENT);
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "parent";
                }
            };
        }
        return this.parent;
    }

    private void invalidatedScenes(Scene scene, SubScene subScene) {
        Scene scene2 = (Scene)this.sceneProperty().get();
        boolean bl = scene != scene2;
        SubScene subScene2 = this.subScene;
        if (this.getClip() != null) {
            this.getClip().setScenes(scene2, subScene2);
        }
        if (bl) {
            this.updateCanReceiveFocus();
            if (this.isFocusTraversable() && scene2 != null) {
                scene2.initializeInternalEventDispatcher();
            }
            this.focusSetDirty(scene);
            this.focusSetDirty(scene2);
        }
        this.scenesChanged(scene2, subScene2, scene, subScene);
        if (scene != null) {
            scene.windowProperty().removeListener(this.sceneWindowChangedListener);
        }
        if (scene2 != null) {
            scene2.windowProperty().addListener(this.sceneWindowChangedListener);
        }
        this.updateTreeShowing();
        if (bl) {
            this.reapplyCSS();
        }
        if (bl && !this.isDirtyEmpty()) {
            this.addToSceneDirtyList();
        }
        if (scene2 == null && this.peer != null) {
            this.peer.release();
        }
        if (scene != null) {
            scene.clearNodeMnemonics(this);
        }
        if (this.getParent() == null) {
            this.parentResolvedOrientationInvalidated();
        }
        if (bl) {
            this.scene.fireSuperValueChangedEvent();
        }
        if (this.accessible != null) {
            if (scene != null && scene != scene2 && scene2 == null) {
                scene.addAccessible(this, this.accessible);
            } else {
                this.accessible.dispose();
            }
            this.accessible = null;
        }
    }

    final void setScenes(Scene scene, SubScene subScene) {
        Scene scene2 = (Scene)this.sceneProperty().get();
        if (scene != scene2 || subScene != this.subScene) {
            this.scene.set(scene);
            SubScene subScene2 = this.subScene;
            this.subScene = subScene;
            this.invalidatedScenes(scene2, subScene2);
            if (this instanceof SubScene) {
                SubScene subScene3 = (SubScene)this;
                subScene3.getRoot().setScenes(scene, subScene3);
            }
        }
    }

    final SubScene getSubScene() {
        return this.subScene;
    }

    public final Scene getScene() {
        return (Scene)this.scene.get();
    }

    public final ReadOnlyObjectProperty<Scene> sceneProperty() {
        return this.scene.getReadOnlyProperty();
    }

    void scenesChanged(Scene scene, SubScene subScene, Scene scene2, SubScene subScene2) {
    }

    public final void setId(String string) {
        this.idProperty().set(string);
    }

    @Override
    public final String getId() {
        return this.id == null ? null : (String)this.id.get();
    }

    public final StringProperty idProperty() {
        if (this.id == null) {
            this.id = new StringPropertyBase(){

                @Override
                protected void invalidated() {
                    Node.this.reapplyCSS();
                    if (PrismSettings.printRenderGraph) {
                        NodeHelper.markDirty(Node.this, DirtyBits.DEBUG);
                    }
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "id";
                }
            };
        }
        return this.id;
    }

    @Override
    public final ObservableList<String> getStyleClass() {
        return this.styleClass;
    }

    public final void setStyle(String string) {
        this.styleProperty().set(string);
    }

    @Override
    public final String getStyle() {
        return this.style == null ? "" : (String)this.style.get();
    }

    public final StringProperty styleProperty() {
        if (this.style == null) {
            this.style = new StringPropertyBase(""){

                @Override
                public void set(String string) {
                    super.set(string != null ? string : "");
                }

                @Override
                protected void invalidated() {
                    Node.this.reapplyCSS();
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "style";
                }
            };
        }
        return this.style;
    }

    public final void setVisible(boolean bl) {
        this.visibleProperty().set(bl);
    }

    public final boolean isVisible() {
        return this.visible == null ? true : this.visible.get();
    }

    public final BooleanProperty visibleProperty() {
        if (this.visible == null) {
            this.visible = new StyleableBooleanProperty(true){
                boolean oldValue;
                {
                    this.oldValue = true;
                }

                @Override
                protected void invalidated() {
                    if (this.oldValue != this.get()) {
                        NodeHelper.markDirty(Node.this, DirtyBits.NODE_VISIBLE);
                        NodeHelper.geomChanged(Node.this);
                        Node.this.updateTreeVisible(false);
                        if (Node.this.getParent() != null) {
                            Node.this.getParent().childVisibilityChanged(Node.this);
                        }
                        this.oldValue = this.get();
                    }
                }

                @Override
                public CssMetaData getCssMetaData() {
                    return StyleableProperties.VISIBILITY;
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "visible";
                }
            };
        }
        return this.visible;
    }

    public final void setCursor(Cursor cursor) {
        this.cursorProperty().set(cursor);
    }

    public final Cursor getCursor() {
        return this.miscProperties == null ? DEFAULT_CURSOR : this.miscProperties.getCursor();
    }

    public final ObjectProperty<Cursor> cursorProperty() {
        return this.getMiscProperties().cursorProperty();
    }

    public final void setOpacity(double d) {
        this.opacityProperty().set(d);
    }

    public final double getOpacity() {
        return this.opacity == null ? 1.0 : this.opacity.get();
    }

    public final DoubleProperty opacityProperty() {
        if (this.opacity == null) {
            this.opacity = new StyleableDoubleProperty(1.0){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Node.this, DirtyBits.NODE_OPACITY);
                }

                @Override
                public CssMetaData getCssMetaData() {
                    return StyleableProperties.OPACITY;
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "opacity";
                }
            };
        }
        return this.opacity;
    }

    public final void setBlendMode(BlendMode blendMode) {
        this.blendModeProperty().set(blendMode);
    }

    public final BlendMode getBlendMode() {
        return this.blendMode == null ? null : (BlendMode)((Object)this.blendMode.get());
    }

    public final ObjectProperty<BlendMode> blendModeProperty() {
        if (this.blendMode == null) {
            this.blendMode = new StyleableObjectProperty<BlendMode>(null){

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Node.this, DirtyBits.NODE_BLENDMODE);
                }

                @Override
                public CssMetaData getCssMetaData() {
                    return StyleableProperties.BLEND_MODE;
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "blendMode";
                }
            };
        }
        return this.blendMode;
    }

    public final void setClip(Node node) {
        this.clipProperty().set(node);
    }

    public final Node getClip() {
        return this.miscProperties == null ? DEFAULT_CLIP : this.miscProperties.getClip();
    }

    public final ObjectProperty<Node> clipProperty() {
        return this.getMiscProperties().clipProperty();
    }

    public final void setCache(boolean bl) {
        this.cacheProperty().set(bl);
    }

    public final boolean isCache() {
        return this.miscProperties == null ? false : this.miscProperties.isCache();
    }

    public final BooleanProperty cacheProperty() {
        return this.getMiscProperties().cacheProperty();
    }

    public final void setCacheHint(CacheHint cacheHint) {
        this.cacheHintProperty().set(cacheHint);
    }

    public final CacheHint getCacheHint() {
        return this.miscProperties == null ? DEFAULT_CACHE_HINT : this.miscProperties.getCacheHint();
    }

    public final ObjectProperty<CacheHint> cacheHintProperty() {
        return this.getMiscProperties().cacheHintProperty();
    }

    public final void setEffect(Effect effect) {
        this.effectProperty().set(effect);
    }

    public final Effect getEffect() {
        return this.miscProperties == null ? DEFAULT_EFFECT : this.miscProperties.getEffect();
    }

    public final ObjectProperty<Effect> effectProperty() {
        return this.getMiscProperties().effectProperty();
    }

    public final void setDepthTest(DepthTest depthTest) {
        this.depthTestProperty().set(depthTest);
    }

    public final DepthTest getDepthTest() {
        return this.miscProperties == null ? DEFAULT_DEPTH_TEST : this.miscProperties.getDepthTest();
    }

    public final ObjectProperty<DepthTest> depthTestProperty() {
        return this.getMiscProperties().depthTestProperty();
    }

    void computeDerivedDepthTest() {
        boolean bl = this.getDepthTest() == DepthTest.INHERIT ? (this.getParent() != null ? this.getParent().isDerivedDepthTest() : true) : this.getDepthTest() == DepthTest.ENABLE;
        if (this.isDerivedDepthTest() != bl) {
            NodeHelper.markDirty(this, DirtyBits.NODE_DEPTH_TEST);
            this.setDerivedDepthTest(bl);
        }
    }

    void setDerivedDepthTest(boolean bl) {
        this.derivedDepthTest = bl;
    }

    boolean isDerivedDepthTest() {
        return this.derivedDepthTest;
    }

    public final void setDisable(boolean bl) {
        this.disableProperty().set(bl);
    }

    public final boolean isDisable() {
        return this.miscProperties == null ? false : this.miscProperties.isDisable();
    }

    public final BooleanProperty disableProperty() {
        return this.getMiscProperties().disableProperty();
    }

    public final void setPickOnBounds(boolean bl) {
        this.pickOnBoundsProperty().set(bl);
    }

    public final boolean isPickOnBounds() {
        return this.pickOnBounds == null ? false : this.pickOnBounds.get();
    }

    public final BooleanProperty pickOnBoundsProperty() {
        if (this.pickOnBounds == null) {
            this.pickOnBounds = new SimpleBooleanProperty(this, "pickOnBounds");
        }
        return this.pickOnBounds;
    }

    protected final void setDisabled(boolean bl) {
        this.disabledPropertyImpl().set(bl);
    }

    public final boolean isDisabled() {
        return this.disabled == null ? false : this.disabled.get();
    }

    public final ReadOnlyBooleanProperty disabledProperty() {
        return this.disabledPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper disabledPropertyImpl() {
        if (this.disabled == null) {
            this.disabled = new ReadOnlyBooleanWrapper(){

                @Override
                protected void invalidated() {
                    Node.this.pseudoClassStateChanged(DISABLED_PSEUDOCLASS_STATE, this.get());
                    Node.this.updateCanReceiveFocus();
                    Node.this.focusSetDirty(Node.this.getScene());
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "disabled";
                }
            };
        }
        return this.disabled;
    }

    private void updateDisabled() {
        boolean bl = this.isDisable();
        if (!bl) {
            bl = this.getParent() != null ? this.getParent().isDisabled() : this.getSubScene() != null && this.getSubScene().isDisabled();
        }
        this.setDisabled(bl);
        if (this instanceof SubScene) {
            ((SubScene)this).getRoot().setDisabled(bl);
        }
    }

    public Node lookup(String string) {
        if (string == null) {
            return null;
        }
        Selector selector = Selector.createSelector(string);
        return selector != null && selector.applies(this) ? this : null;
    }

    public Set<Node> lookupAll(String string) {
        Selector selector = Selector.createSelector(string);
        UnmodifiableListSet<Node> unmodifiableListSet = Collections.emptySet();
        if (selector == null) {
            return unmodifiableListSet;
        }
        List<Node> list = this.lookupAll(selector, null);
        return list == null ? unmodifiableListSet : new UnmodifiableListSet<Node>(list);
    }

    List<Node> lookupAll(Selector selector, List<Node> list) {
        if (selector.applies(this)) {
            if (list == null) {
                list = new LinkedList<Node>();
            }
            list.add(this);
        }
        return list;
    }

    public void toBack() {
        if (this.getParent() != null) {
            this.getParent().toBack(this);
        }
    }

    public void toFront() {
        if (this.getParent() != null) {
            this.getParent().toFront(this);
        }
    }

    private void doCSSPass() {
        if (this.cssFlag != CssFlags.CLEAN) {
            this.processCSS();
        }
    }

    private static void syncAll(Node node) {
        node.syncPeer();
        if (node instanceof Parent) {
            Parent parent = (Parent)node;
            int n = parent.getChildren().size();
            for (int i = 0; i < n; ++i) {
                Node node2 = (Node)parent.getChildren().get(i);
                if (node2 == null) continue;
                Node.syncAll(node2);
            }
        }
        if (node.getClip() != null) {
            Node.syncAll(node.getClip());
        }
    }

    private void doLayoutPass() {
        if (this instanceof Parent) {
            Parent parent = (Parent)this;
            for (int i = 0; i < 3; ++i) {
                parent.layout();
            }
        }
    }

    private void doCSSLayoutSyncForSnapshot() {
        this.doCSSPass();
        this.doLayoutPass();
        this.updateBounds();
        Scene.setAllowPGAccess(true);
        Node.syncAll(this);
        Scene.setAllowPGAccess(false);
    }

    private WritableImage doSnapshot(SnapshotParameters snapshotParameters, WritableImage writableImage) {
        Object object;
        double d;
        double d2;
        double d3;
        double d4;
        Rectangle2D rectangle2D;
        if (this.getScene() != null) {
            this.getScene().doCSSLayoutSyncForSnapshot(this);
        } else {
            this.doCSSLayoutSyncForSnapshot();
        }
        BaseTransform baseTransform = BaseTransform.IDENTITY_TRANSFORM;
        if (snapshotParameters.getTransform() != null) {
            Affine3D affine3D = new Affine3D();
            TransformHelper.apply(snapshotParameters.getTransform(), affine3D);
            baseTransform = affine3D;
        }
        if ((rectangle2D = snapshotParameters.getViewport()) != null) {
            d4 = rectangle2D.getMinX();
            d3 = rectangle2D.getMinY();
            d2 = rectangle2D.getWidth();
            d = rectangle2D.getHeight();
        } else {
            object = TempState.getInstance().bounds;
            object = this.getTransformedBounds((BaseBounds)object, baseTransform);
            d4 = ((BaseBounds)object).getMinX();
            d3 = ((BaseBounds)object).getMinY();
            d2 = ((BaseBounds)object).getWidth();
            d = ((BaseBounds)object).getHeight();
        }
        object = Scene.doSnapshot(this.getScene(), d4, d3, d2, d, this, baseTransform, snapshotParameters.isDepthBufferInternal(), snapshotParameters.getFill(), snapshotParameters.getEffectiveCamera(), writableImage);
        return object;
    }

    public WritableImage snapshot(SnapshotParameters snapshotParameters, WritableImage writableImage) {
        Toolkit.getToolkit().checkFxUserThread();
        if (snapshotParameters == null) {
            snapshotParameters = new SnapshotParameters();
            Scene scene = this.getScene();
            if (scene != null) {
                snapshotParameters.setCamera(scene.getEffectiveCamera());
                snapshotParameters.setDepthBuffer(scene.isDepthBufferInternal());
                snapshotParameters.setFill(scene.getFill());
            }
        }
        return this.doSnapshot(snapshotParameters, writableImage);
    }

    public void snapshot(Callback<SnapshotResult, Void> callback, SnapshotParameters snapshotParameters, WritableImage writableImage) {
        Object object;
        Toolkit.getToolkit().checkFxUserThread();
        if (callback == null) {
            throw new NullPointerException("The callback must not be null");
        }
        if (snapshotParameters == null) {
            snapshotParameters = new SnapshotParameters();
            object = this.getScene();
            if (object != null) {
                snapshotParameters.setCamera(((Scene)object).getEffectiveCamera());
                snapshotParameters.setDepthBuffer(((Scene)object).isDepthBufferInternal());
                snapshotParameters.setFill(((Scene)object).getFill());
            }
        } else {
            snapshotParameters = snapshotParameters.copy();
        }
        object = snapshotParameters;
        Callback<SnapshotResult, Void> callback2 = callback;
        WritableImage writableImage2 = writableImage;
        Runnable runnable = () -> this.lambda$snapshot$4((SnapshotParameters)object, writableImage2, callback2);
        Scene.addSnapshotRunnable(runnable);
    }

    public final void setOnDragEntered(EventHandler<? super DragEvent> eventHandler) {
        this.onDragEnteredProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragEntered() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnDragEntered();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragEnteredProperty() {
        return this.getEventHandlerProperties().onDragEnteredProperty();
    }

    public final void setOnDragExited(EventHandler<? super DragEvent> eventHandler) {
        this.onDragExitedProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragExited() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnDragExited();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragExitedProperty() {
        return this.getEventHandlerProperties().onDragExitedProperty();
    }

    public final void setOnDragOver(EventHandler<? super DragEvent> eventHandler) {
        this.onDragOverProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragOver() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnDragOver();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragOverProperty() {
        return this.getEventHandlerProperties().onDragOverProperty();
    }

    public final void setOnDragDropped(EventHandler<? super DragEvent> eventHandler) {
        this.onDragDroppedProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragDropped() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnDragDropped();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragDroppedProperty() {
        return this.getEventHandlerProperties().onDragDroppedProperty();
    }

    public final void setOnDragDone(EventHandler<? super DragEvent> eventHandler) {
        this.onDragDoneProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragDone() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnDragDone();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragDoneProperty() {
        return this.getEventHandlerProperties().onDragDoneProperty();
    }

    public Dragboard startDragAndDrop(TransferMode ... transferModeArray) {
        if (this.getScene() != null) {
            return this.getScene().startDragAndDrop(this, transferModeArray);
        }
        throw new IllegalStateException("Cannot start drag and drop on node that is not in scene");
    }

    public void startFullDrag() {
        if (this.getScene() != null) {
            this.getScene().startFullDrag(this);
            return;
        }
        throw new IllegalStateException("Cannot start full drag on node that is not in scene");
    }

    final Node getClipParent() {
        return this.clipParent;
    }

    boolean isConnected() {
        return this.getParent() != null || this.clipParent != null;
    }

    boolean wouldCreateCycle(Node node, Node node2) {
        if (node2 != null && node2.getClip() == null && !(node2 instanceof Parent)) {
            return false;
        }
        Node node3 = node;
        while (node3 != node2) {
            if (node3.getParent() != null) {
                node3 = node3.getParent();
                continue;
            }
            if (node3.getSubScene() != null) {
                node3 = node3.getSubScene();
                continue;
            }
            if (node3.clipParent != null) {
                node3 = node3.clipParent;
                continue;
            }
            return false;
        }
        return true;
    }

    <P extends NGNode> P getPeer() {
        if (Utils.assertionEnabled() && this.getScene() != null && !Scene.isPGAccessAllowed()) {
            System.err.println();
            System.err.println("*** unexpected PG access");
            Thread.dumpStack();
        }
        if (this.peer == null) {
            this.peer = NodeHelper.createPeer(this);
        }
        return (P)this.peer;
    }

    protected Node() {
        this.setDirty();
        this.updateTreeVisible(false);
    }

    public final void setManaged(boolean bl) {
        this.managedProperty().set(bl);
    }

    public final boolean isManaged() {
        return this.managed == null ? true : this.managed.get();
    }

    public final BooleanProperty managedProperty() {
        if (this.managed == null) {
            this.managed = new BooleanPropertyBase(true){

                @Override
                protected void invalidated() {
                    Parent parent = Node.this.getParent();
                    if (parent != null) {
                        parent.managedChildChanged();
                    }
                    Node.this.notifyManagedChanged();
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "managed";
                }
            };
        }
        return this.managed;
    }

    void notifyManagedChanged() {
    }

    public final void setLayoutX(double d) {
        this.layoutXProperty().set(d);
    }

    public final double getLayoutX() {
        return this.layoutX == null ? 0.0 : this.layoutX.get();
    }

    public final DoubleProperty layoutXProperty() {
        if (this.layoutX == null) {
            this.layoutX = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    NodeHelper.transformsChanged(Node.this);
                    Parent parent = Node.this.getParent();
                    if (parent != null && !parent.isCurrentLayoutChild(Node.this)) {
                        if (Node.this.isManaged()) {
                            parent.requestLayout(true);
                        } else {
                            parent.clearSizeCache();
                            parent.requestParentLayout();
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "layoutX";
                }
            };
        }
        return this.layoutX;
    }

    public final void setLayoutY(double d) {
        this.layoutYProperty().set(d);
    }

    public final double getLayoutY() {
        return this.layoutY == null ? 0.0 : this.layoutY.get();
    }

    public final DoubleProperty layoutYProperty() {
        if (this.layoutY == null) {
            this.layoutY = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    NodeHelper.transformsChanged(Node.this);
                    Parent parent = Node.this.getParent();
                    if (parent != null && !parent.isCurrentLayoutChild(Node.this)) {
                        if (Node.this.isManaged()) {
                            parent.requestLayout(true);
                        } else {
                            parent.clearSizeCache();
                            parent.requestParentLayout();
                        }
                    }
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "layoutY";
                }
            };
        }
        return this.layoutY;
    }

    public void relocate(double d, double d2) {
        this.setLayoutX(d - this.getLayoutBounds().getMinX());
        this.setLayoutY(d2 - this.getLayoutBounds().getMinY());
        PlatformLogger platformLogger = Logging.getLayoutLogger();
        if (platformLogger.isLoggable(PlatformLogger.Level.FINER)) {
            platformLogger.finer(this.toString() + " moved to (" + d + "," + d2 + ")");
        }
    }

    public boolean isResizable() {
        return false;
    }

    public Orientation getContentBias() {
        return null;
    }

    public double minWidth(double d) {
        return this.prefWidth(d);
    }

    public double minHeight(double d) {
        return this.prefHeight(d);
    }

    public double prefWidth(double d) {
        double d2 = this.getLayoutBounds().getWidth();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    public double prefHeight(double d) {
        double d2 = this.getLayoutBounds().getHeight();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    public double maxWidth(double d) {
        return this.prefWidth(d);
    }

    public double maxHeight(double d) {
        return this.prefHeight(d);
    }

    public void resize(double d, double d2) {
    }

    public final void autosize() {
        if (this.isResizable()) {
            double d;
            double d2;
            Orientation orientation = this.getContentBias();
            if (orientation == null) {
                d2 = this.boundedSize(this.prefWidth(-1.0), this.minWidth(-1.0), this.maxWidth(-1.0));
                d = this.boundedSize(this.prefHeight(-1.0), this.minHeight(-1.0), this.maxHeight(-1.0));
            } else if (orientation == Orientation.HORIZONTAL) {
                d2 = this.boundedSize(this.prefWidth(-1.0), this.minWidth(-1.0), this.maxWidth(-1.0));
                d = this.boundedSize(this.prefHeight(d2), this.minHeight(d2), this.maxHeight(d2));
            } else {
                d = this.boundedSize(this.prefHeight(-1.0), this.minHeight(-1.0), this.maxHeight(-1.0));
                d2 = this.boundedSize(this.prefWidth(d), this.minWidth(d), this.maxWidth(d));
            }
            this.resize(d2, d);
        }
    }

    double boundedSize(double d, double d2, double d3) {
        return Math.min(Math.max(d, d2), Math.max(d2, d3));
    }

    public void resizeRelocate(double d, double d2, double d3, double d4) {
        this.resize(d3, d4);
        this.relocate(d, d2);
    }

    public double getBaselineOffset() {
        if (this.isResizable()) {
            return Double.NEGATIVE_INFINITY;
        }
        return this.getLayoutBounds().getHeight();
    }

    public double computeAreaInScreen() {
        return this.doComputeAreaInScreen();
    }

    private double doComputeAreaInScreen() {
        Scene scene = this.getScene();
        if (scene != null) {
            Object object;
            Bounds bounds = this.getBoundsInLocal();
            Camera camera = scene.getEffectiveCamera();
            boolean bl = camera instanceof PerspectiveCamera;
            Transform transform = this.getLocalToSceneTransform();
            Affine3D affine3D = TempState.getInstance().tempTx;
            BaseBounds baseBounds = new BoxBounds((float)bounds.getMinX(), (float)bounds.getMinY(), (float)bounds.getMinZ(), (float)bounds.getMaxX(), (float)bounds.getMaxY(), (float)bounds.getMaxZ());
            if (bl) {
                object = camera.getLocalToSceneTransform();
                if (((Transform)object).getMxx() == 1.0 && ((Transform)object).getMxy() == 0.0 && ((Transform)object).getMxz() == 0.0 && ((Transform)object).getMyx() == 0.0 && ((Transform)object).getMyy() == 1.0 && ((Transform)object).getMyz() == 0.0 && ((Transform)object).getMzx() == 0.0 && ((Transform)object).getMzy() == 0.0 && ((Transform)object).getMzz() == 1.0) {
                    double d;
                    double d2;
                    if (transform.getMxx() == 1.0 && transform.getMxy() == 0.0 && transform.getMxz() == 0.0 && transform.getMyx() == 0.0 && transform.getMyy() == 1.0 && transform.getMyz() == 0.0 && transform.getMzx() == 0.0 && transform.getMzy() == 0.0 && transform.getMzz() == 1.0) {
                        Vec3d vec3d = TempState.getInstance().vec3d;
                        vec3d.set(0.0, 0.0, bounds.getMinZ());
                        this.localToScene(vec3d);
                        d2 = vec3d.z;
                        vec3d.set(0.0, 0.0, bounds.getMaxZ());
                        this.localToScene(vec3d);
                        d = vec3d.z;
                    } else {
                        Bounds bounds2 = this.localToScene(bounds);
                        d2 = bounds2.getMinZ();
                        d = bounds2.getMaxZ();
                    }
                    if (d2 > camera.getFarClipInScene() || d < camera.getNearClipInScene()) {
                        return 0.0;
                    }
                } else {
                    BoxBounds boxBounds = new BoxBounds();
                    affine3D.setToIdentity();
                    TransformHelper.apply(transform, affine3D);
                    affine3D.preConcatenate(camera.getSceneToLocalTransform());
                    affine3D.transform(baseBounds, boxBounds);
                    if ((double)((BaseBounds)boxBounds).getMinZ() > camera.getFarClip() || (double)((BaseBounds)boxBounds).getMaxZ() < camera.getNearClip()) {
                        return 0.0;
                    }
                }
            }
            object = TempState.getInstance().projViewTx;
            ((GeneralTransform3D)object).set(camera.getProjViewTransform());
            affine3D.setToIdentity();
            TransformHelper.apply(transform, affine3D);
            GeneralTransform3D generalTransform3D = ((GeneralTransform3D)object).mul(affine3D);
            baseBounds = generalTransform3D.transform(baseBounds, baseBounds);
            double d = baseBounds.getWidth() * baseBounds.getHeight();
            if (bl) {
                baseBounds.intersectWith(-1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                d = baseBounds.getWidth() < 0.0f || baseBounds.getHeight() < 0.0f ? 0.0 : d;
            }
            return d * (camera.getViewWidth() / 2.0 * camera.getViewHeight() / 2.0);
        }
        return 0.0;
    }

    public final Bounds getBoundsInParent() {
        return (Bounds)this.boundsInParentProperty().get();
    }

    public final ReadOnlyObjectProperty<Bounds> boundsInParentProperty() {
        return this.getMiscProperties().boundsInParentProperty();
    }

    private void invalidateBoundsInParent() {
        if (this.miscProperties != null) {
            this.miscProperties.invalidateBoundsInParent();
        }
    }

    public final Bounds getBoundsInLocal() {
        return (Bounds)this.boundsInLocalProperty().get();
    }

    public final ReadOnlyObjectProperty<Bounds> boundsInLocalProperty() {
        return this.getMiscProperties().boundsInLocalProperty();
    }

    private void invalidateBoundsInLocal() {
        if (this.miscProperties != null) {
            this.miscProperties.invalidateBoundsInLocal();
        }
    }

    public final Bounds getLayoutBounds() {
        return (Bounds)this.layoutBoundsProperty().get();
    }

    public final ReadOnlyObjectProperty<Bounds> layoutBoundsProperty() {
        return this.layoutBounds;
    }

    private Bounds doComputeLayoutBounds() {
        BaseBounds baseBounds = TempState.getInstance().bounds;
        baseBounds = this.getGeomBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM);
        return new BoundingBox(baseBounds.getMinX(), baseBounds.getMinY(), baseBounds.getMinZ(), baseBounds.getWidth(), baseBounds.getHeight(), baseBounds.getDepth());
    }

    final void layoutBoundsChanged() {
        if (!this.layoutBounds.valid) {
            return;
        }
        this.layoutBounds.invalidate();
        if (this.nodeTransformation != null && this.nodeTransformation.hasScaleOrRotate() || this.hasMirroring()) {
            NodeHelper.transformsChanged(this);
        }
    }

    BaseBounds getTransformedBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        this.updateLocalToParentTransform();
        if (baseTransform.isTranslateOrIdentity()) {
            this.updateTxBounds();
            baseBounds = baseBounds.deriveWithNewBounds(this.txBounds);
            if (!baseTransform.isIdentity()) {
                double d = baseTransform.getMxt();
                double d2 = baseTransform.getMyt();
                double d3 = baseTransform.getMzt();
                baseBounds = baseBounds.deriveWithNewBounds((float)((double)baseBounds.getMinX() + d), (float)((double)baseBounds.getMinY() + d2), (float)((double)baseBounds.getMinZ() + d3), (float)((double)baseBounds.getMaxX() + d), (float)((double)baseBounds.getMaxY() + d2), (float)((double)baseBounds.getMaxZ() + d3));
            }
            return baseBounds;
        }
        if (this.localToParentTx.isIdentity()) {
            return this.getLocalBounds(baseBounds, baseTransform);
        }
        double d = baseTransform.getMxx();
        double d4 = baseTransform.getMxy();
        double d5 = baseTransform.getMxz();
        double d6 = baseTransform.getMxt();
        double d7 = baseTransform.getMyx();
        double d8 = baseTransform.getMyy();
        double d9 = baseTransform.getMyz();
        double d10 = baseTransform.getMyt();
        double d11 = baseTransform.getMzx();
        double d12 = baseTransform.getMzy();
        double d13 = baseTransform.getMzz();
        double d14 = baseTransform.getMzt();
        BaseTransform baseTransform2 = baseTransform.deriveWithConcatenation(this.localToParentTx);
        baseBounds = this.getLocalBounds(baseBounds, baseTransform2);
        if (baseTransform2 == baseTransform) {
            baseTransform.restoreTransform(d, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14);
        }
        return baseBounds;
    }

    BaseBounds getLocalBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        if (this.getEffect() == null && this.getClip() == null) {
            return this.getGeomBounds(baseBounds, baseTransform);
        }
        if (baseTransform.isTranslateOrIdentity()) {
            this.updateLocalBounds();
            baseBounds = baseBounds.deriveWithNewBounds(this.localBounds);
            if (!baseTransform.isIdentity()) {
                double d = baseTransform.getMxt();
                double d2 = baseTransform.getMyt();
                double d3 = baseTransform.getMzt();
                baseBounds = baseBounds.deriveWithNewBounds((float)((double)baseBounds.getMinX() + d), (float)((double)baseBounds.getMinY() + d2), (float)((double)baseBounds.getMinZ() + d3), (float)((double)baseBounds.getMaxX() + d), (float)((double)baseBounds.getMaxY() + d2), (float)((double)baseBounds.getMaxZ() + d3));
            }
            return baseBounds;
        }
        if (baseTransform.is2D() && (baseTransform.getType() & 0xFFFFFFB4) != 0) {
            return this.computeLocalBounds(baseBounds, baseTransform);
        }
        this.updateLocalBounds();
        return baseTransform.transform(this.localBounds, baseBounds);
    }

    BaseBounds getGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        if (baseTransform.isTranslateOrIdentity()) {
            this.updateGeomBounds();
            baseBounds = baseBounds.deriveWithNewBounds(this.geomBounds);
            if (!baseTransform.isIdentity()) {
                double d = baseTransform.getMxt();
                double d2 = baseTransform.getMyt();
                double d3 = baseTransform.getMzt();
                baseBounds = baseBounds.deriveWithNewBounds((float)((double)baseBounds.getMinX() + d), (float)((double)baseBounds.getMinY() + d2), (float)((double)baseBounds.getMinZ() + d3), (float)((double)baseBounds.getMaxX() + d), (float)((double)baseBounds.getMaxY() + d2), (float)((double)baseBounds.getMaxZ() + d3));
            }
            return baseBounds;
        }
        if (baseTransform.is2D() && (baseTransform.getType() & 0xFFFFFFB4) != 0) {
            return NodeHelper.computeGeomBounds(this, baseBounds, baseTransform);
        }
        this.updateGeomBounds();
        return baseTransform.transform(this.geomBounds, baseBounds);
    }

    void updateGeomBounds() {
        if (this.geomBoundsInvalid) {
            this.geomBounds = NodeHelper.computeGeomBounds(this, this.geomBounds, BaseTransform.IDENTITY_TRANSFORM);
            this.geomBoundsInvalid = false;
        }
    }

    private BaseBounds computeLocalBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        if (this.getEffect() != null) {
            BaseBounds baseBounds2 = EffectHelper.getBounds(this.getEffect(), baseBounds, baseTransform, this, boundsAccessor);
            baseBounds = baseBounds.deriveWithNewBounds(baseBounds2);
        } else {
            baseBounds = this.getGeomBounds(baseBounds, baseTransform);
        }
        if (this.getClip() != null && !(this instanceof Shape3D) && !(this.getClip() instanceof Shape3D)) {
            double d = baseBounds.getMinX();
            double d2 = baseBounds.getMinY();
            double d3 = baseBounds.getMaxX();
            double d4 = baseBounds.getMaxY();
            double d5 = baseBounds.getMinZ();
            double d6 = baseBounds.getMaxZ();
            baseBounds = this.getClip().getTransformedBounds(baseBounds, baseTransform);
            baseBounds.intersectWith((float)d, (float)d2, (float)d5, (float)d3, (float)d4, (float)d6);
        }
        return baseBounds;
    }

    private void updateLocalBounds() {
        if (this.localBoundsInvalid) {
            this.localBounds = this.getClip() != null || this.getEffect() != null ? this.computeLocalBounds(this.localBounds == null ? new RectBounds() : this.localBounds, BaseTransform.IDENTITY_TRANSFORM) : null;
            this.localBoundsInvalid = false;
        }
    }

    void updateTxBounds() {
        if (this.txBoundsInvalid) {
            this.updateLocalToParentTransform();
            this.txBounds = this.getLocalBounds(this.txBounds, this.localToParentTx);
            this.txBoundsInvalid = false;
        }
    }

    private void doGeomChanged() {
        if (this.geomBoundsInvalid) {
            NodeHelper.notifyLayoutBoundsChanged(this);
            this.transformedBoundsChanged();
            return;
        }
        this.geomBounds.makeEmpty();
        this.geomBoundsInvalid = true;
        NodeHelper.markDirty(this, DirtyBits.NODE_BOUNDS);
        NodeHelper.notifyLayoutBoundsChanged(this);
        this.localBoundsChanged();
    }

    void localBoundsChanged() {
        this.localBoundsInvalid = true;
        this.invalidateBoundsInLocal();
        this.transformedBoundsChanged();
    }

    void transformedBoundsChanged() {
        if (!this.txBoundsInvalid) {
            this.txBounds.makeEmpty();
            this.txBoundsInvalid = true;
            this.invalidateBoundsInParent();
            NodeHelper.markDirty(this, DirtyBits.NODE_TRANSFORMED_BOUNDS);
        }
        if (this.isVisible()) {
            this.notifyParentOfBoundsChange();
        }
    }

    private void doNotifyLayoutBoundsChanged() {
        this.layoutBoundsChanged();
        Parent parent = this.getParent();
        if (this.isManaged() && parent != null && (!(parent instanceof Group) || this.isResizable()) && !parent.isPerformingLayout()) {
            parent.requestLayout(true);
        }
    }

    void notifyParentOfBoundsChange() {
        Parent parent = this.getParent();
        if (parent != null) {
            parent.childBoundsChanged(this);
        }
        if (this.clipParent != null) {
            this.clipParent.localBoundsChanged();
        }
    }

    public boolean contains(double d, double d2) {
        if (this.containsBounds(d, d2)) {
            return this.isPickOnBounds() || NodeHelper.computeContains(this, d, d2);
        }
        return false;
    }

    private boolean containsBounds(double d, double d2) {
        TempState tempState = TempState.getInstance();
        BaseBounds baseBounds = tempState.bounds;
        if ((baseBounds = this.getLocalBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM)).contains((float)d, (float)d2)) {
            if (this.getClip() != null) {
                tempState.point.x = (float)d;
                tempState.point.y = (float)d2;
                try {
                    this.getClip().parentToLocal(tempState.point);
                }
                catch (NoninvertibleTransformException noninvertibleTransformException) {
                    return false;
                }
                if (!this.getClip().contains(tempState.point.x, tempState.point.y)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean contains(Point2D point2D) {
        return this.contains(point2D.getX(), point2D.getY());
    }

    public boolean intersects(double d, double d2, double d3, double d4) {
        BaseBounds baseBounds = TempState.getInstance().bounds;
        baseBounds = this.getLocalBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM);
        return baseBounds.intersects((float)d, (float)d2, (float)d3, (float)d4);
    }

    public boolean intersects(Bounds bounds) {
        return this.intersects(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }

    public Point2D screenToLocal(double d, double d2) {
        Object object;
        Scene scene = this.getScene();
        if (scene == null) {
            return null;
        }
        Window window = scene.getWindow();
        if (window == null) {
            return null;
        }
        com.sun.javafx.geom.Point2D point2D = TempState.getInstance().point;
        point2D.setLocation((float)(d - scene.getX() - window.getX()), (float)(d2 - scene.getY() - window.getY()));
        SubScene subScene = this.getSubScene();
        if (subScene != null) {
            object = SceneUtils.sceneToSubScenePlane(subScene, new Point2D(point2D.x, point2D.y));
            if (object == null) {
                return null;
            }
            point2D.setLocation((float)((Point2D)object).getX(), (float)((Point2D)object).getY());
        }
        object = scene.getEffectiveCamera().pickProjectPlane(point2D.x, point2D.y);
        point2D.setLocation((float)((Point3D)object).getX(), (float)((Point3D)object).getY());
        try {
            this.sceneToLocal(point2D);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
        return new Point2D(point2D.x, point2D.y);
    }

    public Point2D screenToLocal(Point2D point2D) {
        return this.screenToLocal(point2D.getX(), point2D.getY());
    }

    public Bounds screenToLocal(Bounds bounds) {
        Point2D point2D = this.screenToLocal(bounds.getMinX(), bounds.getMinY());
        Point2D point2D2 = this.screenToLocal(bounds.getMinX(), bounds.getMaxY());
        Point2D point2D3 = this.screenToLocal(bounds.getMaxX(), bounds.getMinY());
        Point2D point2D4 = this.screenToLocal(bounds.getMaxX(), bounds.getMaxY());
        return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
    }

    public Point2D sceneToLocal(double d, double d2, boolean bl) {
        if (!bl) {
            return this.sceneToLocal(d, d2);
        }
        com.sun.javafx.geom.Point2D point2D = TempState.getInstance().point;
        point2D.setLocation((float)d, (float)d2);
        SubScene subScene = this.getSubScene();
        if (subScene != null) {
            Point2D point2D2 = SceneUtils.sceneToSubScenePlane(subScene, new Point2D(point2D.x, point2D.y));
            if (point2D2 == null) {
                return null;
            }
            point2D.setLocation((float)point2D2.getX(), (float)point2D2.getY());
        }
        try {
            this.sceneToLocal(point2D);
            return new Point2D(point2D.x, point2D.y);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
    }

    public Point2D sceneToLocal(Point2D point2D, boolean bl) {
        return this.sceneToLocal(point2D.getX(), point2D.getY(), bl);
    }

    public Bounds sceneToLocal(Bounds bounds, boolean bl) {
        if (!bl) {
            return this.sceneToLocal(bounds);
        }
        if (bounds.getMinZ() != 0.0 || bounds.getMaxZ() != 0.0) {
            return null;
        }
        Point2D point2D = this.sceneToLocal(bounds.getMinX(), bounds.getMinY(), true);
        Point2D point2D2 = this.sceneToLocal(bounds.getMinX(), bounds.getMaxY(), true);
        Point2D point2D3 = this.sceneToLocal(bounds.getMaxX(), bounds.getMinY(), true);
        Point2D point2D4 = this.sceneToLocal(bounds.getMaxX(), bounds.getMaxY(), true);
        return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
    }

    public Point2D sceneToLocal(double d, double d2) {
        com.sun.javafx.geom.Point2D point2D = TempState.getInstance().point;
        point2D.setLocation((float)d, (float)d2);
        try {
            this.sceneToLocal(point2D);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
        return new Point2D(point2D.x, point2D.y);
    }

    public Point2D sceneToLocal(Point2D point2D) {
        return this.sceneToLocal(point2D.getX(), point2D.getY());
    }

    public Point3D sceneToLocal(Point3D point3D) {
        return this.sceneToLocal(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D sceneToLocal(double d, double d2, double d3) {
        try {
            return this.sceneToLocal0(d, d2, d3);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
    }

    private Point3D sceneToLocal0(double d, double d2, double d3) throws NoninvertibleTransformException {
        Vec3d vec3d = TempState.getInstance().vec3d;
        vec3d.set(d, d2, d3);
        this.sceneToLocal(vec3d);
        return new Point3D(vec3d.x, vec3d.y, vec3d.z);
    }

    public Bounds sceneToLocal(Bounds bounds) {
        this.updateLocalToParentTransform();
        if (this.localToParentTx.is2D() && bounds.getMinZ() == 0.0 && bounds.getMaxZ() == 0.0) {
            Point2D point2D = this.sceneToLocal(bounds.getMinX(), bounds.getMinY());
            Point2D point2D2 = this.sceneToLocal(bounds.getMaxX(), bounds.getMinY());
            Point2D point2D3 = this.sceneToLocal(bounds.getMaxX(), bounds.getMaxY());
            Point2D point2D4 = this.sceneToLocal(bounds.getMinX(), bounds.getMaxY());
            return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
        }
        try {
            Point3D point3D = this.sceneToLocal0(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
            Point3D point3D2 = this.sceneToLocal0(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ());
            Point3D point3D3 = this.sceneToLocal0(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ());
            Point3D point3D4 = this.sceneToLocal0(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ());
            Point3D point3D5 = this.sceneToLocal0(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ());
            Point3D point3D6 = this.sceneToLocal0(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
            Point3D point3D7 = this.sceneToLocal0(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ());
            Point3D point3D8 = this.sceneToLocal0(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ());
            return BoundsUtils.createBoundingBox(point3D, point3D2, point3D3, point3D4, point3D5, point3D6, point3D7, point3D8);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
    }

    public Point2D localToScreen(double d, double d2) {
        return this.localToScreen(d, d2, 0.0);
    }

    public Point2D localToScreen(Point2D point2D) {
        return this.localToScreen(point2D.getX(), point2D.getY());
    }

    public Point2D localToScreen(double d, double d2, double d3) {
        Scene scene = this.getScene();
        if (scene == null) {
            return null;
        }
        Window window = scene.getWindow();
        if (window == null) {
            return null;
        }
        Point3D point3D = this.localToScene(d, d2, d3);
        SubScene subScene = this.getSubScene();
        if (subScene != null) {
            point3D = SceneUtils.subSceneToScene(subScene, point3D);
        }
        Point2D point2D = CameraHelper.project(SceneHelper.getEffectiveCamera(this.getScene()), point3D);
        return new Point2D(point2D.getX() + scene.getX() + window.getX(), point2D.getY() + scene.getY() + window.getY());
    }

    public Point2D localToScreen(Point3D point3D) {
        return this.localToScreen(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Bounds localToScreen(Bounds bounds) {
        Point2D point2D = this.localToScreen(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Point2D point2D2 = this.localToScreen(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ());
        Point2D point2D3 = this.localToScreen(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ());
        Point2D point2D4 = this.localToScreen(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ());
        Point2D point2D5 = this.localToScreen(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ());
        Point2D point2D6 = this.localToScreen(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        Point2D point2D7 = this.localToScreen(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ());
        Point2D point2D8 = this.localToScreen(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ());
        return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4, point2D5, point2D6, point2D7, point2D8);
    }

    public Point2D localToScene(double d, double d2) {
        com.sun.javafx.geom.Point2D point2D = TempState.getInstance().point;
        point2D.setLocation((float)d, (float)d2);
        this.localToScene(point2D);
        return new Point2D(point2D.x, point2D.y);
    }

    public Point2D localToScene(Point2D point2D) {
        return this.localToScene(point2D.getX(), point2D.getY());
    }

    public Point3D localToScene(Point3D point3D) {
        return this.localToScene(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D localToScene(double d, double d2, double d3) {
        Vec3d vec3d = TempState.getInstance().vec3d;
        vec3d.set(d, d2, d3);
        this.localToScene(vec3d);
        return new Point3D(vec3d.x, vec3d.y, vec3d.z);
    }

    public Point3D localToScene(Point3D point3D, boolean bl) {
        SubScene subScene;
        Point3D point3D2 = this.localToScene(point3D);
        if (bl && (subScene = this.getSubScene()) != null) {
            point3D2 = SceneUtils.subSceneToScene(subScene, point3D2);
        }
        return point3D2;
    }

    public Point3D localToScene(double d, double d2, double d3, boolean bl) {
        return this.localToScene(new Point3D(d, d2, d3), bl);
    }

    public Point2D localToScene(Point2D point2D, boolean bl) {
        if (!bl) {
            return this.localToScene(point2D);
        }
        Point3D point3D = this.localToScene(point2D.getX(), point2D.getY(), 0.0, bl);
        return new Point2D(point3D.getX(), point3D.getY());
    }

    public Point2D localToScene(double d, double d2, boolean bl) {
        return this.localToScene(new Point2D(d, d2), bl);
    }

    public Bounds localToScene(Bounds bounds, boolean bl) {
        if (!bl) {
            return this.localToScene(bounds);
        }
        Point3D point3D = this.localToScene(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(), true);
        Point3D point3D2 = this.localToScene(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ(), true);
        Point3D point3D3 = this.localToScene(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ(), true);
        Point3D point3D4 = this.localToScene(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ(), true);
        Point3D point3D5 = this.localToScene(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ(), true);
        Point3D point3D6 = this.localToScene(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ(), true);
        Point3D point3D7 = this.localToScene(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ(), true);
        Point3D point3D8 = this.localToScene(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ(), true);
        return BoundsUtils.createBoundingBox(point3D, point3D2, point3D3, point3D4, point3D5, point3D6, point3D7, point3D8);
    }

    public Bounds localToScene(Bounds bounds) {
        this.updateLocalToParentTransform();
        if (this.localToParentTx.is2D() && bounds.getMinZ() == 0.0 && bounds.getMaxZ() == 0.0) {
            Point2D point2D = this.localToScene(bounds.getMinX(), bounds.getMinY());
            Point2D point2D2 = this.localToScene(bounds.getMaxX(), bounds.getMinY());
            Point2D point2D3 = this.localToScene(bounds.getMaxX(), bounds.getMaxY());
            Point2D point2D4 = this.localToScene(bounds.getMinX(), bounds.getMaxY());
            return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
        }
        Point3D point3D = this.localToScene(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D2 = this.localToScene(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ());
        Point3D point3D3 = this.localToScene(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D4 = this.localToScene(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D5 = this.localToScene(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D6 = this.localToScene(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D7 = this.localToScene(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D8 = this.localToScene(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ());
        return BoundsUtils.createBoundingBox(point3D, point3D2, point3D3, point3D4, point3D5, point3D6, point3D7, point3D8);
    }

    public Point2D parentToLocal(double d, double d2) {
        com.sun.javafx.geom.Point2D point2D = TempState.getInstance().point;
        point2D.setLocation((float)d, (float)d2);
        try {
            this.parentToLocal(point2D);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
        return new Point2D(point2D.x, point2D.y);
    }

    public Point2D parentToLocal(Point2D point2D) {
        return this.parentToLocal(point2D.getX(), point2D.getY());
    }

    public Point3D parentToLocal(Point3D point3D) {
        return this.parentToLocal(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D parentToLocal(double d, double d2, double d3) {
        Vec3d vec3d = TempState.getInstance().vec3d;
        vec3d.set(d, d2, d3);
        try {
            this.parentToLocal(vec3d);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            return null;
        }
        return new Point3D(vec3d.x, vec3d.y, vec3d.z);
    }

    public Bounds parentToLocal(Bounds bounds) {
        this.updateLocalToParentTransform();
        if (this.localToParentTx.is2D() && bounds.getMinZ() == 0.0 && bounds.getMaxZ() == 0.0) {
            Point2D point2D = this.parentToLocal(bounds.getMinX(), bounds.getMinY());
            Point2D point2D2 = this.parentToLocal(bounds.getMaxX(), bounds.getMinY());
            Point2D point2D3 = this.parentToLocal(bounds.getMaxX(), bounds.getMaxY());
            Point2D point2D4 = this.parentToLocal(bounds.getMinX(), bounds.getMaxY());
            return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
        }
        Point3D point3D = this.parentToLocal(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D2 = this.parentToLocal(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ());
        Point3D point3D3 = this.parentToLocal(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D4 = this.parentToLocal(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D5 = this.parentToLocal(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D6 = this.parentToLocal(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D7 = this.parentToLocal(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D8 = this.parentToLocal(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ());
        return BoundsUtils.createBoundingBox(point3D, point3D2, point3D3, point3D4, point3D5, point3D6, point3D7, point3D8);
    }

    public Point2D localToParent(double d, double d2) {
        com.sun.javafx.geom.Point2D point2D = TempState.getInstance().point;
        point2D.setLocation((float)d, (float)d2);
        this.localToParent(point2D);
        return new Point2D(point2D.x, point2D.y);
    }

    public Point2D localToParent(Point2D point2D) {
        return this.localToParent(point2D.getX(), point2D.getY());
    }

    public Point3D localToParent(Point3D point3D) {
        return this.localToParent(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D localToParent(double d, double d2, double d3) {
        Vec3d vec3d = TempState.getInstance().vec3d;
        vec3d.set(d, d2, d3);
        this.localToParent(vec3d);
        return new Point3D(vec3d.x, vec3d.y, vec3d.z);
    }

    public Bounds localToParent(Bounds bounds) {
        this.updateLocalToParentTransform();
        if (this.localToParentTx.is2D() && bounds.getMinZ() == 0.0 && bounds.getMaxZ() == 0.0) {
            Point2D point2D = this.localToParent(bounds.getMinX(), bounds.getMinY());
            Point2D point2D2 = this.localToParent(bounds.getMaxX(), bounds.getMinY());
            Point2D point2D3 = this.localToParent(bounds.getMaxX(), bounds.getMaxY());
            Point2D point2D4 = this.localToParent(bounds.getMinX(), bounds.getMaxY());
            return BoundsUtils.createBoundingBox(point2D, point2D2, point2D3, point2D4);
        }
        Point3D point3D = this.localToParent(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D2 = this.localToParent(bounds.getMinX(), bounds.getMinY(), bounds.getMaxZ());
        Point3D point3D3 = this.localToParent(bounds.getMinX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D4 = this.localToParent(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D5 = this.localToParent(bounds.getMaxX(), bounds.getMaxY(), bounds.getMinZ());
        Point3D point3D6 = this.localToParent(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());
        Point3D point3D7 = this.localToParent(bounds.getMaxX(), bounds.getMinY(), bounds.getMinZ());
        Point3D point3D8 = this.localToParent(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxZ());
        return BoundsUtils.createBoundingBox(point3D, point3D2, point3D3, point3D4, point3D5, point3D6, point3D7, point3D8);
    }

    BaseTransform getLocalToParentTransform(BaseTransform baseTransform) {
        this.updateLocalToParentTransform();
        baseTransform.setTransform(this.localToParentTx);
        return baseTransform;
    }

    final BaseTransform getLeafTransform() {
        return this.getLocalToParentTransform(TempState.getInstance().leafTx);
    }

    private void doTransformsChanged() {
        if (!this.transformDirty) {
            NodeHelper.markDirty(this, DirtyBits.NODE_TRANSFORM);
            this.transformDirty = true;
            this.transformedBoundsChanged();
        }
        this.invalidateLocalToParentTransform();
        this.invalidateLocalToSceneTransform();
    }

    final double getPivotX() {
        Bounds bounds = this.getLayoutBounds();
        return bounds.getMinX() + bounds.getWidth() / 2.0;
    }

    final double getPivotY() {
        Bounds bounds = this.getLayoutBounds();
        return bounds.getMinY() + bounds.getHeight() / 2.0;
    }

    final double getPivotZ() {
        Bounds bounds = this.getLayoutBounds();
        return bounds.getMinZ() + bounds.getDepth() / 2.0;
    }

    void updateLocalToParentTransform() {
        if (this.transformDirty) {
            this.localToParentTx.setToIdentity();
            boolean bl = false;
            double d = 0.0;
            if (this.hasMirroring()) {
                Scene scene = this.getScene();
                if (scene != null && scene.getRoot() == this) {
                    d = scene.getWidth() / 2.0;
                    if (d == 0.0) {
                        d = this.getPivotX();
                    }
                    this.localToParentTx = this.localToParentTx.deriveWithTranslation(d, 0.0);
                    this.localToParentTx = this.localToParentTx.deriveWithScale(-1.0, 1.0, 1.0);
                    this.localToParentTx = this.localToParentTx.deriveWithTranslation(-d, 0.0);
                } else {
                    bl = true;
                    d = this.getPivotX();
                }
            }
            if (this.getScaleX() != 1.0 || this.getScaleY() != 1.0 || this.getScaleZ() != 1.0 || this.getRotate() != 0.0) {
                double d2 = this.getPivotX();
                double d3 = this.getPivotY();
                double d4 = this.getPivotZ();
                this.localToParentTx = this.localToParentTx.deriveWithTranslation(this.getTranslateX() + this.getLayoutX() + d2, this.getTranslateY() + this.getLayoutY() + d3, this.getTranslateZ() + d4);
                this.localToParentTx = this.localToParentTx.deriveWithRotation(Math.toRadians(this.getRotate()), this.getRotationAxis().getX(), this.getRotationAxis().getY(), this.getRotationAxis().getZ());
                this.localToParentTx = this.localToParentTx.deriveWithScale(this.getScaleX(), this.getScaleY(), this.getScaleZ());
                this.localToParentTx = this.localToParentTx.deriveWithTranslation(-d2, -d3, -d4);
            } else {
                this.localToParentTx = this.localToParentTx.deriveWithTranslation(this.getTranslateX() + this.getLayoutX(), this.getTranslateY() + this.getLayoutY(), this.getTranslateZ());
            }
            if (this.hasTransforms()) {
                for (Transform transform : this.getTransforms()) {
                    this.localToParentTx = TransformHelper.derive(transform, this.localToParentTx);
                }
            }
            if (bl) {
                this.localToParentTx = this.localToParentTx.deriveWithTranslation(d, 0.0);
                this.localToParentTx = this.localToParentTx.deriveWithScale(-1.0, 1.0, 1.0);
                this.localToParentTx = this.localToParentTx.deriveWithTranslation(-d, 0.0);
            }
            this.transformDirty = false;
        }
    }

    void parentToLocal(com.sun.javafx.geom.Point2D point2D) throws NoninvertibleTransformException {
        this.updateLocalToParentTransform();
        this.localToParentTx.inverseTransform(point2D, point2D);
    }

    void parentToLocal(Vec3d vec3d) throws NoninvertibleTransformException {
        this.updateLocalToParentTransform();
        this.localToParentTx.inverseTransform(vec3d, vec3d);
    }

    void sceneToLocal(com.sun.javafx.geom.Point2D point2D) throws NoninvertibleTransformException {
        if (this.getParent() != null) {
            this.getParent().sceneToLocal(point2D);
        }
        this.parentToLocal(point2D);
    }

    void sceneToLocal(Vec3d vec3d) throws NoninvertibleTransformException {
        if (this.getParent() != null) {
            this.getParent().sceneToLocal(vec3d);
        }
        this.parentToLocal(vec3d);
    }

    void localToScene(com.sun.javafx.geom.Point2D point2D) {
        this.localToParent(point2D);
        if (this.getParent() != null) {
            this.getParent().localToScene(point2D);
        }
    }

    void localToScene(Vec3d vec3d) {
        this.localToParent(vec3d);
        if (this.getParent() != null) {
            this.getParent().localToScene(vec3d);
        }
    }

    void localToParent(com.sun.javafx.geom.Point2D point2D) {
        this.updateLocalToParentTransform();
        this.localToParentTx.transform(point2D, point2D);
    }

    void localToParent(Vec3d vec3d) {
        this.updateLocalToParentTransform();
        this.localToParentTx.transform(vec3d, vec3d);
    }

    private void doPickNodeLocal(PickRay pickRay, PickResultChooser pickResultChooser) {
        this.intersects(pickRay, pickResultChooser);
    }

    final void pickNode(PickRay pickRay, PickResultChooser pickResultChooser) {
        if (!this.isVisible() || this.isDisable() || this.isMouseTransparent()) {
            return;
        }
        Vec3d vec3d = pickRay.getOriginNoClone();
        double d = vec3d.x;
        double d2 = vec3d.y;
        double d3 = vec3d.z;
        Vec3d vec3d2 = pickRay.getDirectionNoClone();
        double d4 = vec3d2.x;
        double d5 = vec3d2.y;
        double d6 = vec3d2.z;
        this.updateLocalToParentTransform();
        try {
            this.localToParentTx.inverseTransform(vec3d, vec3d);
            this.localToParentTx.inverseDeltaTransform(vec3d2, vec3d2);
            NodeHelper.pickNodeLocal(this, pickRay, pickResultChooser);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            // empty catch block
        }
        pickRay.setOrigin(d, d2, d3);
        pickRay.setDirection(d4, d5, d6);
    }

    final boolean intersects(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d = this.intersectsBounds(pickRay);
        if (!Double.isNaN(d)) {
            if (this.isPickOnBounds()) {
                if (pickResultChooser != null) {
                    pickResultChooser.offer(this, d, PickResultChooser.computePoint(pickRay, d));
                }
                return true;
            }
            return NodeHelper.computeIntersects(this, pickRay, pickResultChooser);
        }
        return false;
    }

    private boolean doComputeIntersects(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d = pickRay.getOriginNoClone().z;
        double d2 = pickRay.getDirectionNoClone().z;
        if (Node.almostZero(d2)) {
            return false;
        }
        double d3 = -d / d2;
        if (d3 < pickRay.getNearClip() || d3 > pickRay.getFarClip()) {
            return false;
        }
        double d4 = pickRay.getOriginNoClone().x + pickRay.getDirectionNoClone().x * d3;
        double d5 = pickRay.getOriginNoClone().y + pickRay.getDirectionNoClone().y * d3;
        if (this.contains((float)d4, (float)d5)) {
            if (pickResultChooser != null) {
                pickResultChooser.offer(this, d3, PickResultChooser.computePoint(pickRay, d3));
            }
            return true;
        }
        return false;
    }

    final double intersectsBounds(PickRay pickRay) {
        double d;
        boolean bl;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        Vec3d vec3d = pickRay.getDirectionNoClone();
        Vec3d vec3d2 = pickRay.getOriginNoClone();
        double d7 = vec3d2.x;
        double d8 = vec3d2.y;
        double d9 = vec3d2.z;
        TempState tempState = TempState.getInstance();
        BaseBounds baseBounds = tempState.bounds;
        baseBounds = this.getLocalBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM);
        if (vec3d.x == 0.0 && vec3d.y == 0.0) {
            if (vec3d.z == 0.0) {
                return Double.NaN;
            }
            if (d7 < (double)baseBounds.getMinX() || d7 > (double)baseBounds.getMaxX() || d8 < (double)baseBounds.getMinY() || d8 > (double)baseBounds.getMaxY()) {
                return Double.NaN;
            }
            d6 = 1.0 / vec3d.z;
            boolean bl2 = d6 < 0.0;
            d5 = baseBounds.getMinZ();
            d4 = baseBounds.getMaxZ();
            d3 = ((bl2 ? d4 : d5) - d9) * d6;
            d2 = ((bl2 ? d5 : d4) - d9) * d6;
        } else if ((double)baseBounds.getDepth() == 0.0) {
            if (Node.almostZero(vec3d.z)) {
                return Double.NaN;
            }
            d6 = ((double)baseBounds.getMinZ() - d9) / vec3d.z;
            double d10 = d7 + vec3d.x * d6;
            double d11 = d8 + vec3d.y * d6;
            if (d10 < (double)baseBounds.getMinX() || d10 > (double)baseBounds.getMaxX() || d11 < (double)baseBounds.getMinY() || d11 > (double)baseBounds.getMaxY()) {
                return Double.NaN;
            }
            d3 = d2 = d6;
        } else {
            double d12;
            double d13;
            d6 = vec3d.x == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / vec3d.x;
            double d14 = vec3d.y == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / vec3d.y;
            double d15 = vec3d.z == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / vec3d.z;
            boolean bl3 = d6 < 0.0;
            bl = d14 < 0.0;
            boolean bl4 = d15 < 0.0;
            double d16 = baseBounds.getMinX();
            double d17 = baseBounds.getMinY();
            double d18 = baseBounds.getMaxX();
            double d19 = baseBounds.getMaxY();
            d3 = Double.NEGATIVE_INFINITY;
            d2 = Double.POSITIVE_INFINITY;
            if (Double.isInfinite(d6)) {
                if (!(d16 <= d7) || !(d18 >= d7)) {
                    return Double.NaN;
                }
            } else {
                d3 = ((bl3 ? d18 : d16) - d7) * d6;
                d2 = ((bl3 ? d16 : d18) - d7) * d6;
            }
            if (Double.isInfinite(d14)) {
                if (!(d17 <= d8) || !(d19 >= d8)) {
                    return Double.NaN;
                }
            } else {
                d13 = ((bl ? d19 : d17) - d8) * d14;
                d12 = ((bl ? d17 : d19) - d8) * d14;
                if (d3 > d12 || d13 > d2) {
                    return Double.NaN;
                }
                if (d13 > d3) {
                    d3 = d13;
                }
                if (d12 < d2) {
                    d2 = d12;
                }
            }
            d13 = baseBounds.getMinZ();
            d12 = baseBounds.getMaxZ();
            if (Double.isInfinite(d15)) {
                if (!(d13 <= d9) || !(d12 >= d9)) {
                    return Double.NaN;
                }
            } else {
                double d20 = ((bl4 ? d12 : d13) - d9) * d15;
                double d21 = ((bl4 ? d13 : d12) - d9) * d15;
                if (d3 > d21 || d20 > d2) {
                    return Double.NaN;
                }
                if (d20 > d3) {
                    d3 = d20;
                }
                if (d21 < d2) {
                    d2 = d21;
                }
            }
        }
        Node node = this.getClip();
        if (node != null && !(this instanceof Shape3D) && !(node instanceof Shape3D)) {
            d = vec3d.x;
            d5 = vec3d.y;
            d4 = vec3d.z;
            node.updateLocalToParentTransform();
            bl = true;
            try {
                node.localToParentTx.inverseTransform(vec3d2, vec3d2);
                node.localToParentTx.inverseDeltaTransform(vec3d, vec3d);
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
                bl = false;
            }
            bl = bl && node.intersects(pickRay, null);
            pickRay.setOrigin(d7, d8, d9);
            pickRay.setDirection(d, d5, d4);
            if (!bl) {
                return Double.NaN;
            }
        }
        if (Double.isInfinite(d3) || Double.isNaN(d3)) {
            return Double.NaN;
        }
        d = pickRay.getNearClip();
        d5 = pickRay.getFarClip();
        if (d3 < d) {
            if (d2 >= d) {
                return 0.0;
            }
            return Double.NaN;
        }
        if (d3 > d5) {
            return Double.NaN;
        }
        return d3;
    }

    static boolean almostZero(double d) {
        return d < 1.0E-5 && d > -1.0E-5;
    }

    public final DoubleProperty viewOrderProperty() {
        return this.getMiscProperties().viewOrderProperty();
    }

    public final void setViewOrder(double d) {
        this.viewOrderProperty().set(d);
    }

    public final double getViewOrder() {
        return this.miscProperties == null ? 0.0 : this.miscProperties.getViewOrder();
    }

    public final ObservableList<Transform> getTransforms() {
        return this.transformsProperty();
    }

    private ObservableList<Transform> transformsProperty() {
        return this.getNodeTransformation().getTransforms();
    }

    public final void setTranslateX(double d) {
        this.translateXProperty().set(d);
    }

    public final double getTranslateX() {
        return this.nodeTransformation == null ? 0.0 : this.nodeTransformation.getTranslateX();
    }

    public final DoubleProperty translateXProperty() {
        return this.getNodeTransformation().translateXProperty();
    }

    public final void setTranslateY(double d) {
        this.translateYProperty().set(d);
    }

    public final double getTranslateY() {
        return this.nodeTransformation == null ? 0.0 : this.nodeTransformation.getTranslateY();
    }

    public final DoubleProperty translateYProperty() {
        return this.getNodeTransformation().translateYProperty();
    }

    public final void setTranslateZ(double d) {
        this.translateZProperty().set(d);
    }

    public final double getTranslateZ() {
        return this.nodeTransformation == null ? 0.0 : this.nodeTransformation.getTranslateZ();
    }

    public final DoubleProperty translateZProperty() {
        return this.getNodeTransformation().translateZProperty();
    }

    public final void setScaleX(double d) {
        this.scaleXProperty().set(d);
    }

    public final double getScaleX() {
        return this.nodeTransformation == null ? 1.0 : this.nodeTransformation.getScaleX();
    }

    public final DoubleProperty scaleXProperty() {
        return this.getNodeTransformation().scaleXProperty();
    }

    public final void setScaleY(double d) {
        this.scaleYProperty().set(d);
    }

    public final double getScaleY() {
        return this.nodeTransformation == null ? 1.0 : this.nodeTransformation.getScaleY();
    }

    public final DoubleProperty scaleYProperty() {
        return this.getNodeTransformation().scaleYProperty();
    }

    public final void setScaleZ(double d) {
        this.scaleZProperty().set(d);
    }

    public final double getScaleZ() {
        return this.nodeTransformation == null ? 1.0 : this.nodeTransformation.getScaleZ();
    }

    public final DoubleProperty scaleZProperty() {
        return this.getNodeTransformation().scaleZProperty();
    }

    public final void setRotate(double d) {
        this.rotateProperty().set(d);
    }

    public final double getRotate() {
        return this.nodeTransformation == null ? 0.0 : this.nodeTransformation.getRotate();
    }

    public final DoubleProperty rotateProperty() {
        return this.getNodeTransformation().rotateProperty();
    }

    public final void setRotationAxis(Point3D point3D) {
        this.rotationAxisProperty().set(point3D);
    }

    public final Point3D getRotationAxis() {
        return this.nodeTransformation == null ? DEFAULT_ROTATION_AXIS : this.nodeTransformation.getRotationAxis();
    }

    public final ObjectProperty<Point3D> rotationAxisProperty() {
        return this.getNodeTransformation().rotationAxisProperty();
    }

    public final ReadOnlyObjectProperty<Transform> localToParentTransformProperty() {
        return this.getNodeTransformation().localToParentTransformProperty();
    }

    private void invalidateLocalToParentTransform() {
        if (this.nodeTransformation != null) {
            this.nodeTransformation.invalidateLocalToParentTransform();
        }
    }

    public final Transform getLocalToParentTransform() {
        return (Transform)this.localToParentTransformProperty().get();
    }

    public final ReadOnlyObjectProperty<Transform> localToSceneTransformProperty() {
        return this.getNodeTransformation().localToSceneTransformProperty();
    }

    private void invalidateLocalToSceneTransform() {
        if (this.nodeTransformation != null) {
            this.nodeTransformation.invalidateLocalToSceneTransform();
        }
    }

    public final Transform getLocalToSceneTransform() {
        return (Transform)this.localToSceneTransformProperty().get();
    }

    private NodeTransformation getNodeTransformation() {
        if (this.nodeTransformation == null) {
            this.nodeTransformation = new NodeTransformation();
        }
        return this.nodeTransformation;
    }

    private boolean hasTransforms() {
        return this.nodeTransformation != null && this.nodeTransformation.hasTransforms();
    }

    Transform getCurrentLocalToSceneTransformState() {
        if (this.nodeTransformation == null || this.nodeTransformation.localToSceneTransform == null) {
            return null;
        }
        return this.nodeTransformation.localToSceneTransform.transform;
    }

    private EventHandlerProperties getEventHandlerProperties() {
        if (this.eventHandlerProperties == null) {
            this.eventHandlerProperties = new EventHandlerProperties(this.getInternalEventDispatcher().getEventHandlerManager(), this);
        }
        return this.eventHandlerProperties;
    }

    public final void setNodeOrientation(NodeOrientation nodeOrientation) {
        this.nodeOrientationProperty().set(nodeOrientation);
    }

    public final NodeOrientation getNodeOrientation() {
        return this.nodeOrientation == null ? NodeOrientation.INHERIT : (NodeOrientation)((Object)this.nodeOrientation.get());
    }

    public final ObjectProperty<NodeOrientation> nodeOrientationProperty() {
        if (this.nodeOrientation == null) {
            this.nodeOrientation = new StyleableObjectProperty<NodeOrientation>(NodeOrientation.INHERIT){

                @Override
                protected void invalidated() {
                    Node.this.nodeResolvedOrientationInvalidated();
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "nodeOrientation";
                }

                @Override
                public CssMetaData getCssMetaData() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
        return this.nodeOrientation;
    }

    public final NodeOrientation getEffectiveNodeOrientation() {
        return Node.getEffectiveOrientation(this.resolvedNodeOrientation) == 0 ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT;
    }

    public final ReadOnlyObjectProperty<NodeOrientation> effectiveNodeOrientationProperty() {
        if (this.effectiveNodeOrientationProperty == null) {
            this.effectiveNodeOrientationProperty = new EffectiveOrientationProperty();
        }
        return this.effectiveNodeOrientationProperty;
    }

    public boolean usesMirroring() {
        return true;
    }

    final void parentResolvedOrientationInvalidated() {
        if (this.getNodeOrientation() == NodeOrientation.INHERIT) {
            this.nodeResolvedOrientationInvalidated();
        } else {
            NodeHelper.transformsChanged(this);
        }
    }

    final void nodeResolvedOrientationInvalidated() {
        byte by = this.resolvedNodeOrientation;
        this.resolvedNodeOrientation = (byte)(this.calcEffectiveNodeOrientation() | this.calcAutomaticNodeOrientation());
        if (this.effectiveNodeOrientationProperty != null && Node.getEffectiveOrientation(this.resolvedNodeOrientation) != Node.getEffectiveOrientation(by)) {
            this.effectiveNodeOrientationProperty.invalidate();
        }
        NodeHelper.transformsChanged(this);
        if (this.resolvedNodeOrientation != by) {
            this.nodeResolvedOrientationChanged();
        }
    }

    void nodeResolvedOrientationChanged() {
    }

    private Node getMirroringOrientationParent() {
        for (Parent parent = this.getParent(); parent != null; parent = parent.getParent()) {
            if (!parent.usesMirroring()) continue;
            return parent;
        }
        SubScene subScene = this.getSubScene();
        if (subScene != null) {
            return subScene;
        }
        return null;
    }

    private Node getOrientationParent() {
        Parent parent = this.getParent();
        if (parent != null) {
            return parent;
        }
        SubScene subScene = this.getSubScene();
        if (subScene != null) {
            return subScene;
        }
        return null;
    }

    private byte calcEffectiveNodeOrientation() {
        NodeOrientation nodeOrientation = this.getNodeOrientation();
        if (nodeOrientation != NodeOrientation.INHERIT) {
            return nodeOrientation == NodeOrientation.LEFT_TO_RIGHT ? (byte)0 : 1;
        }
        Node node = this.getOrientationParent();
        if (node != null) {
            return Node.getEffectiveOrientation(node.resolvedNodeOrientation);
        }
        Scene scene = this.getScene();
        if (scene != null) {
            return scene.getEffectiveNodeOrientation() == NodeOrientation.LEFT_TO_RIGHT ? (byte)0 : 1;
        }
        return 0;
    }

    private byte calcAutomaticNodeOrientation() {
        if (!this.usesMirroring()) {
            return 0;
        }
        NodeOrientation nodeOrientation = this.getNodeOrientation();
        if (nodeOrientation != NodeOrientation.INHERIT) {
            return nodeOrientation == NodeOrientation.LEFT_TO_RIGHT ? (byte)0 : 2;
        }
        Node node = this.getMirroringOrientationParent();
        if (node != null) {
            return Node.getAutomaticOrientation(node.resolvedNodeOrientation);
        }
        Scene scene = this.getScene();
        if (scene != null) {
            return scene.getEffectiveNodeOrientation() == NodeOrientation.LEFT_TO_RIGHT ? (byte)0 : 2;
        }
        return 0;
    }

    final boolean hasMirroring() {
        Node node = this.getOrientationParent();
        byte by = Node.getAutomaticOrientation(this.resolvedNodeOrientation);
        byte by2 = node != null ? Node.getAutomaticOrientation(node.resolvedNodeOrientation) : (byte)0;
        return by != by2;
    }

    private static byte getEffectiveOrientation(byte by) {
        return (byte)(by & 1);
    }

    private static byte getAutomaticOrientation(byte by) {
        return (byte)(by & 2);
    }

    private MiscProperties getMiscProperties() {
        if (this.miscProperties == null) {
            this.miscProperties = new MiscProperties();
        }
        return this.miscProperties;
    }

    public final void setMouseTransparent(boolean bl) {
        this.mouseTransparentProperty().set(bl);
    }

    public final boolean isMouseTransparent() {
        return this.miscProperties == null ? false : this.miscProperties.isMouseTransparent();
    }

    public final BooleanProperty mouseTransparentProperty() {
        return this.getMiscProperties().mouseTransparentProperty();
    }

    protected final void setHover(boolean bl) {
        this.hoverPropertyImpl().set(bl);
    }

    public final boolean isHover() {
        return this.hover == null ? false : this.hover.get();
    }

    public final ReadOnlyBooleanProperty hoverProperty() {
        return this.hoverPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper hoverPropertyImpl() {
        if (this.hover == null) {
            this.hover = new ReadOnlyBooleanWrapper(){

                @Override
                protected void invalidated() {
                    PlatformLogger platformLogger = Logging.getInputLogger();
                    if (platformLogger.isLoggable(PlatformLogger.Level.FINER)) {
                        platformLogger.finer(this + " hover=" + this.get());
                    }
                    Node.this.pseudoClassStateChanged(HOVER_PSEUDOCLASS_STATE, this.get());
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "hover";
                }
            };
        }
        return this.hover;
    }

    protected final void setPressed(boolean bl) {
        this.pressedPropertyImpl().set(bl);
    }

    public final boolean isPressed() {
        return this.pressed == null ? false : this.pressed.get();
    }

    public final ReadOnlyBooleanProperty pressedProperty() {
        return this.pressedPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper pressedPropertyImpl() {
        if (this.pressed == null) {
            this.pressed = new ReadOnlyBooleanWrapper(){

                @Override
                protected void invalidated() {
                    PlatformLogger platformLogger = Logging.getInputLogger();
                    if (platformLogger.isLoggable(PlatformLogger.Level.FINER)) {
                        platformLogger.finer(this + " pressed=" + this.get());
                    }
                    Node.this.pseudoClassStateChanged(PRESSED_PSEUDOCLASS_STATE, this.get());
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "pressed";
                }
            };
        }
        return this.pressed;
    }

    public final void setOnContextMenuRequested(EventHandler<? super ContextMenuEvent> eventHandler) {
        this.onContextMenuRequestedProperty().set(eventHandler);
    }

    public final EventHandler<? super ContextMenuEvent> getOnContextMenuRequested() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.onContextMenuRequested();
    }

    public final ObjectProperty<EventHandler<? super ContextMenuEvent>> onContextMenuRequestedProperty() {
        return this.getEventHandlerProperties().onContextMenuRequestedProperty();
    }

    public final void setOnMouseClicked(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseClickedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseClicked() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseClicked();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseClickedProperty() {
        return this.getEventHandlerProperties().onMouseClickedProperty();
    }

    public final void setOnMouseDragged(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseDraggedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseDragged() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseDragged();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseDraggedProperty() {
        return this.getEventHandlerProperties().onMouseDraggedProperty();
    }

    public final void setOnMouseEntered(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseEnteredProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseEntered() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseEntered();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseEnteredProperty() {
        return this.getEventHandlerProperties().onMouseEnteredProperty();
    }

    public final void setOnMouseExited(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseExitedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseExited() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseExited();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseExitedProperty() {
        return this.getEventHandlerProperties().onMouseExitedProperty();
    }

    public final void setOnMouseMoved(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseMovedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseMoved() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseMoved();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseMovedProperty() {
        return this.getEventHandlerProperties().onMouseMovedProperty();
    }

    public final void setOnMousePressed(EventHandler<? super MouseEvent> eventHandler) {
        this.onMousePressedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMousePressed() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMousePressed();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMousePressedProperty() {
        return this.getEventHandlerProperties().onMousePressedProperty();
    }

    public final void setOnMouseReleased(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseReleased() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseReleased();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseReleasedProperty() {
        return this.getEventHandlerProperties().onMouseReleasedProperty();
    }

    public final void setOnDragDetected(EventHandler<? super MouseEvent> eventHandler) {
        this.onDragDetectedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnDragDetected() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnDragDetected();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onDragDetectedProperty() {
        return this.getEventHandlerProperties().onDragDetectedProperty();
    }

    public final void setOnMouseDragOver(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragOverProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragOver() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseDragOver();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragOverProperty() {
        return this.getEventHandlerProperties().onMouseDragOverProperty();
    }

    public final void setOnMouseDragReleased(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragReleased() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseDragReleased();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragReleasedProperty() {
        return this.getEventHandlerProperties().onMouseDragReleasedProperty();
    }

    public final void setOnMouseDragEntered(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragEnteredProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragEntered() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseDragEntered();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragEnteredProperty() {
        return this.getEventHandlerProperties().onMouseDragEnteredProperty();
    }

    public final void setOnMouseDragExited(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragExitedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragExited() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnMouseDragExited();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragExitedProperty() {
        return this.getEventHandlerProperties().onMouseDragExitedProperty();
    }

    public final void setOnScrollStarted(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollStartedProperty().set(eventHandler);
    }

    public final EventHandler<? super ScrollEvent> getOnScrollStarted() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnScrollStarted();
    }

    public final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollStartedProperty() {
        return this.getEventHandlerProperties().onScrollStartedProperty();
    }

    public final void setOnScroll(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollProperty().set(eventHandler);
    }

    public final EventHandler<? super ScrollEvent> getOnScroll() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnScroll();
    }

    public final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollProperty() {
        return this.getEventHandlerProperties().onScrollProperty();
    }

    public final void setOnScrollFinished(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollFinishedProperty().set(eventHandler);
    }

    public final EventHandler<? super ScrollEvent> getOnScrollFinished() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnScrollFinished();
    }

    public final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollFinishedProperty() {
        return this.getEventHandlerProperties().onScrollFinishedProperty();
    }

    public final void setOnRotationStarted(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotationStartedProperty().set(eventHandler);
    }

    public final EventHandler<? super RotateEvent> getOnRotationStarted() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnRotationStarted();
    }

    public final ObjectProperty<EventHandler<? super RotateEvent>> onRotationStartedProperty() {
        return this.getEventHandlerProperties().onRotationStartedProperty();
    }

    public final void setOnRotate(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotateProperty().set(eventHandler);
    }

    public final EventHandler<? super RotateEvent> getOnRotate() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnRotate();
    }

    public final ObjectProperty<EventHandler<? super RotateEvent>> onRotateProperty() {
        return this.getEventHandlerProperties().onRotateProperty();
    }

    public final void setOnRotationFinished(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotationFinishedProperty().set(eventHandler);
    }

    public final EventHandler<? super RotateEvent> getOnRotationFinished() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnRotationFinished();
    }

    public final ObjectProperty<EventHandler<? super RotateEvent>> onRotationFinishedProperty() {
        return this.getEventHandlerProperties().onRotationFinishedProperty();
    }

    public final void setOnZoomStarted(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomStartedProperty().set(eventHandler);
    }

    public final EventHandler<? super ZoomEvent> getOnZoomStarted() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnZoomStarted();
    }

    public final ObjectProperty<EventHandler<? super ZoomEvent>> onZoomStartedProperty() {
        return this.getEventHandlerProperties().onZoomStartedProperty();
    }

    public final void setOnZoom(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomProperty().set(eventHandler);
    }

    public final EventHandler<? super ZoomEvent> getOnZoom() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnZoom();
    }

    public final ObjectProperty<EventHandler<? super ZoomEvent>> onZoomProperty() {
        return this.getEventHandlerProperties().onZoomProperty();
    }

    public final void setOnZoomFinished(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomFinishedProperty().set(eventHandler);
    }

    public final EventHandler<? super ZoomEvent> getOnZoomFinished() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnZoomFinished();
    }

    public final ObjectProperty<EventHandler<? super ZoomEvent>> onZoomFinishedProperty() {
        return this.getEventHandlerProperties().onZoomFinishedProperty();
    }

    public final void setOnSwipeUp(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeUpProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeUp() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnSwipeUp();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeUpProperty() {
        return this.getEventHandlerProperties().onSwipeUpProperty();
    }

    public final void setOnSwipeDown(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeDownProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeDown() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnSwipeDown();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeDownProperty() {
        return this.getEventHandlerProperties().onSwipeDownProperty();
    }

    public final void setOnSwipeLeft(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeLeftProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeLeft() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnSwipeLeft();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeLeftProperty() {
        return this.getEventHandlerProperties().onSwipeLeftProperty();
    }

    public final void setOnSwipeRight(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeRightProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeRight() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnSwipeRight();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeRightProperty() {
        return this.getEventHandlerProperties().onSwipeRightProperty();
    }

    public final void setOnTouchPressed(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchPressedProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchPressed() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnTouchPressed();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchPressedProperty() {
        return this.getEventHandlerProperties().onTouchPressedProperty();
    }

    public final void setOnTouchMoved(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchMovedProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchMoved() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnTouchMoved();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchMovedProperty() {
        return this.getEventHandlerProperties().onTouchMovedProperty();
    }

    public final void setOnTouchReleased(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchReleased() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnTouchReleased();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchReleasedProperty() {
        return this.getEventHandlerProperties().onTouchReleasedProperty();
    }

    public final void setOnTouchStationary(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchStationaryProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchStationary() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnTouchStationary();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchStationaryProperty() {
        return this.getEventHandlerProperties().onTouchStationaryProperty();
    }

    public final void setOnKeyPressed(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyPressedProperty().set(eventHandler);
    }

    public final EventHandler<? super KeyEvent> getOnKeyPressed() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnKeyPressed();
    }

    public final ObjectProperty<EventHandler<? super KeyEvent>> onKeyPressedProperty() {
        return this.getEventHandlerProperties().onKeyPressedProperty();
    }

    public final void setOnKeyReleased(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super KeyEvent> getOnKeyReleased() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnKeyReleased();
    }

    public final ObjectProperty<EventHandler<? super KeyEvent>> onKeyReleasedProperty() {
        return this.getEventHandlerProperties().onKeyReleasedProperty();
    }

    public final void setOnKeyTyped(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyTypedProperty().set(eventHandler);
    }

    public final EventHandler<? super KeyEvent> getOnKeyTyped() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnKeyTyped();
    }

    public final ObjectProperty<EventHandler<? super KeyEvent>> onKeyTypedProperty() {
        return this.getEventHandlerProperties().onKeyTypedProperty();
    }

    public final void setOnInputMethodTextChanged(EventHandler<? super InputMethodEvent> eventHandler) {
        this.onInputMethodTextChangedProperty().set(eventHandler);
    }

    public final EventHandler<? super InputMethodEvent> getOnInputMethodTextChanged() {
        return this.eventHandlerProperties == null ? null : this.eventHandlerProperties.getOnInputMethodTextChanged();
    }

    public final ObjectProperty<EventHandler<? super InputMethodEvent>> onInputMethodTextChangedProperty() {
        return this.getEventHandlerProperties().onInputMethodTextChangedProperty();
    }

    public final void setInputMethodRequests(InputMethodRequests inputMethodRequests) {
        this.inputMethodRequestsProperty().set(inputMethodRequests);
    }

    public final InputMethodRequests getInputMethodRequests() {
        return this.miscProperties == null ? DEFAULT_INPUT_METHOD_REQUESTS : this.miscProperties.getInputMethodRequests();
    }

    public final ObjectProperty<InputMethodRequests> inputMethodRequestsProperty() {
        return this.getMiscProperties().inputMethodRequestsProperty();
    }

    protected final void setFocused(boolean bl) {
        FocusedProperty focusedProperty = this.focusedPropertyImpl();
        if (focusedProperty.value != bl) {
            focusedProperty.store(bl);
            focusedProperty.notifyListeners();
        }
    }

    public final boolean isFocused() {
        return this.focused == null ? false : this.focused.get();
    }

    public final ReadOnlyBooleanProperty focusedProperty() {
        return this.focusedPropertyImpl();
    }

    private FocusedProperty focusedPropertyImpl() {
        if (this.focused == null) {
            this.focused = new FocusedProperty();
        }
        return this.focused;
    }

    public final void setFocusTraversable(boolean bl) {
        this.focusTraversableProperty().set(bl);
    }

    public final boolean isFocusTraversable() {
        return this.focusTraversable == null ? false : this.focusTraversable.get();
    }

    public final BooleanProperty focusTraversableProperty() {
        if (this.focusTraversable == null) {
            this.focusTraversable = new StyleableBooleanProperty(false){

                @Override
                public void invalidated() {
                    Scene scene = Node.this.getScene();
                    if (scene != null) {
                        if (this.get()) {
                            scene.initializeInternalEventDispatcher();
                        }
                        Node.this.focusSetDirty(scene);
                    }
                }

                @Override
                public CssMetaData getCssMetaData() {
                    return StyleableProperties.FOCUS_TRAVERSABLE;
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "focusTraversable";
                }
            };
        }
        return this.focusTraversable;
    }

    private void focusSetDirty(Scene scene) {
        if (scene != null && (this == scene.getFocusOwner() || this.isFocusTraversable())) {
            scene.setFocusDirty(true);
        }
    }

    public void requestFocus() {
        if (this.getScene() != null) {
            this.getScene().requestFocus(this);
        }
    }

    final boolean traverse(Direction direction) {
        if (this.getScene() == null) {
            return false;
        }
        return this.getScene().traverse(this, direction);
    }

    public String toString() {
        boolean bl;
        String string = this.getClass().getName();
        String string2 = string.substring(string.lastIndexOf(46) + 1);
        StringBuilder stringBuilder = new StringBuilder(string2);
        boolean bl2 = this.id != null && !"".equals(this.getId());
        boolean bl3 = bl = !this.getStyleClass().isEmpty();
        if (!bl2) {
            stringBuilder.append('@');
            stringBuilder.append(Integer.toHexString(this.hashCode()));
        } else {
            stringBuilder.append("[id=");
            stringBuilder.append(this.getId());
            if (!bl) {
                stringBuilder.append("]");
            }
        }
        if (bl) {
            if (!bl2) {
                stringBuilder.append('[');
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append("styleClass=");
            stringBuilder.append(this.getStyleClass());
            stringBuilder.append("]");
        }
        return stringBuilder.toString();
    }

    private void preprocessMouseEvent(MouseEvent mouseEvent) {
        EventType<? extends MouseEvent> eventType = mouseEvent.getEventType();
        if (eventType == MouseEvent.MOUSE_PRESSED) {
            for (Node node = this; node != null; node = node.getParent()) {
                node.setPressed(mouseEvent.isPrimaryButtonDown());
            }
            return;
        }
        if (eventType == MouseEvent.MOUSE_RELEASED) {
            for (Node node = this; node != null; node = node.getParent()) {
                node.setPressed(mouseEvent.isPrimaryButtonDown());
            }
            return;
        }
        if (mouseEvent.getTarget() == this) {
            if (eventType == MouseEvent.MOUSE_ENTERED || eventType == MouseEvent.MOUSE_ENTERED_TARGET) {
                this.setHover(true);
                return;
            }
            if (eventType == MouseEvent.MOUSE_EXITED || eventType == MouseEvent.MOUSE_EXITED_TARGET) {
                this.setHover(false);
                return;
            }
        }
    }

    void markDirtyLayoutBranch() {
        for (Parent parent = this.getParent(); parent != null && parent.layoutFlag == LayoutFlags.CLEAN; parent = parent.getParent()) {
            parent.setLayoutFlag(LayoutFlags.DIRTY_BRANCH);
            if (!parent.isSceneRoot()) continue;
            Toolkit.getToolkit().requestNextPulse();
            if (this.getSubScene() == null) continue;
            this.getSubScene().setDirtyLayout(parent);
        }
    }

    private boolean isWindowShowing() {
        Scene scene = this.getScene();
        if (scene == null) {
            return false;
        }
        Window window = scene.getWindow();
        return window != null && window.isShowing();
    }

    private void updateTreeShowing() {
        this.setTreeShowing(this.isTreeVisible() && this.isWindowShowing());
    }

    final void setTreeShowing(boolean bl) {
        if (this.treeShowing != bl) {
            this.treeShowing = bl;
            ((TreeShowingPropertyReadOnly)this.treeShowingProperty()).invalidate();
        }
    }

    final boolean isTreeShowing() {
        return this.treeShowingProperty().get();
    }

    final BooleanExpression treeShowingProperty() {
        if (this.treeShowingRO == null) {
            this.treeShowingRO = new TreeShowingPropertyReadOnly();
        }
        return this.treeShowingRO;
    }

    private void updateTreeVisible(boolean bl) {
        Parent parent;
        boolean bl2 = this.isVisible();
        Node node = this.getParent() != null ? this.getParent() : (this.clipParent != null ? this.clipParent : (parent = this.getSubScene() != null ? this.getSubScene() : null));
        if (bl2) {
            boolean bl3 = bl2 = parent == null || parent.isTreeVisible();
        }
        if (bl && parent != null && parent.isTreeVisible() && this.isDirty(DirtyBits.NODE_VISIBLE)) {
            this.addToSceneDirtyList();
        }
        this.setTreeVisible(bl2);
        this.updateTreeShowing();
    }

    final void setTreeVisible(boolean bl) {
        if (this.treeVisible != bl) {
            Parent parent;
            this.treeVisible = bl;
            this.updateCanReceiveFocus();
            this.focusSetDirty(this.getScene());
            if (this.getClip() != null) {
                this.getClip().updateTreeVisible(true);
            }
            if (this.treeVisible && !this.isDirtyEmpty()) {
                this.addToSceneDirtyList();
            }
            ((TreeVisiblePropertyReadOnly)this.treeVisibleProperty()).invalidate();
            if (this instanceof SubScene && (parent = ((SubScene)this).getRoot()) != null) {
                parent.setTreeVisible(bl && parent.isVisible());
            }
        }
    }

    final boolean isTreeVisible() {
        return this.treeVisibleProperty().get();
    }

    final BooleanExpression treeVisibleProperty() {
        if (this.treeVisibleRO == null) {
            this.treeVisibleRO = new TreeVisiblePropertyReadOnly();
        }
        return this.treeVisibleRO;
    }

    private void setCanReceiveFocus(boolean bl) {
        this.canReceiveFocus = bl;
    }

    final boolean isCanReceiveFocus() {
        return this.canReceiveFocus;
    }

    private void updateCanReceiveFocus() {
        this.setCanReceiveFocus(this.getScene() != null && !this.isDisabled() && this.isTreeVisible());
    }

    String indent() {
        Object object = "";
        for (Parent parent = this.getParent(); parent != null; parent = parent.getParent()) {
            object = (String)object + "  ";
        }
        return object;
    }

    final void setShowMnemonics(boolean bl) {
        this.showMnemonicsProperty().set(bl);
    }

    final boolean isShowMnemonics() {
        return this.showMnemonics == null ? false : this.showMnemonics.get();
    }

    final BooleanProperty showMnemonicsProperty() {
        if (this.showMnemonics == null) {
            this.showMnemonics = new BooleanPropertyBase(false){

                @Override
                protected void invalidated() {
                    Node.this.pseudoClassStateChanged(SHOW_MNEMONICS_PSEUDOCLASS_STATE, this.get());
                }

                @Override
                public Object getBean() {
                    return Node.this;
                }

                @Override
                public String getName() {
                    return "showMnemonics";
                }
            };
        }
        return this.showMnemonics;
    }

    public final void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcherProperty().set(eventDispatcher);
    }

    public final EventDispatcher getEventDispatcher() {
        return (EventDispatcher)this.eventDispatcherProperty().get();
    }

    public final ObjectProperty<EventDispatcher> eventDispatcherProperty() {
        this.initializeInternalEventDispatcher();
        return this.eventDispatcher;
    }

    public final <T extends Event> void addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().getEventHandlerManager().addEventHandler(eventType, eventHandler);
    }

    public final <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().getEventHandlerManager().removeEventHandler(eventType, eventHandler);
    }

    public final <T extends Event> void addEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().getEventHandlerManager().addEventFilter(eventType, eventHandler);
    }

    public final <T extends Event> void removeEventFilter(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().getEventHandlerManager().removeEventFilter(eventType, eventHandler);
    }

    protected final <T extends Event> void setEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        this.getInternalEventDispatcher().getEventHandlerManager().setEventHandler(eventType, eventHandler);
    }

    private NodeEventDispatcher getInternalEventDispatcher() {
        this.initializeInternalEventDispatcher();
        return this.internalEventDispatcher;
    }

    private void initializeInternalEventDispatcher() {
        if (this.internalEventDispatcher == null) {
            this.internalEventDispatcher = this.createInternalEventDispatcher();
            this.eventDispatcher = new SimpleObjectProperty<NodeEventDispatcher>(this, "eventDispatcher", this.internalEventDispatcher);
        }
    }

    private NodeEventDispatcher createInternalEventDispatcher() {
        return new NodeEventDispatcher(this);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain2) {
        Object object;
        if (this.preprocessMouseEventDispatcher == null) {
            this.preprocessMouseEventDispatcher = (event, eventDispatchChain) -> {
                if ((event = eventDispatchChain.dispatchEvent(event)) instanceof MouseEvent) {
                    this.preprocessMouseEvent((MouseEvent)event);
                }
                return event;
            };
        }
        eventDispatchChain2 = eventDispatchChain2.prepend(this.preprocessMouseEventDispatcher);
        Object object2 = this;
        do {
            if (((Node)object2).eventDispatcher == null || (object = (EventDispatcher)((Node)object2).eventDispatcher.get()) == null) continue;
            eventDispatchChain2 = eventDispatchChain2.prepend((EventDispatcher)object);
        } while ((object2 = (object = ((Node)object2).getParent()) != null ? object : ((Node)object2).getSubScene()) != null);
        if (this.getScene() != null) {
            eventDispatchChain2 = this.getScene().buildEventDispatchChain(eventDispatchChain2);
        }
        return eventDispatchChain2;
    }

    public final void fireEvent(Event event) {
        PlatformLogger platformLogger;
        if (event instanceof InputEvent && (platformLogger = Logging.getInputLogger()).isLoggable(PlatformLogger.Level.FINE)) {
            EventType<? extends Event> eventType = event.getEventType();
            if (eventType == MouseEvent.MOUSE_ENTERED || eventType == MouseEvent.MOUSE_EXITED) {
                platformLogger.finer(event.toString());
            } else if (eventType == MouseEvent.MOUSE_MOVED || eventType == MouseEvent.MOUSE_DRAGGED) {
                platformLogger.finest(event.toString());
            } else {
                platformLogger.fine(event.toString());
            }
        }
        Event.fireEvent(this, event);
    }

    @Override
    public String getTypeSelector() {
        Class<?> clazz = this.getClass();
        Package package_ = clazz.getPackage();
        int n = 0;
        if (package_ != null) {
            n = package_.getName().length();
        }
        int n2 = clazz.getName().length();
        int n3 = 0 < n && n < n2 ? n + 1 : 0;
        return clazz.getName().substring(n3);
    }

    @Override
    public Styleable getStyleableParent() {
        return this.getParent();
    }

    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    protected Cursor getInitialCursor() {
        return null;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return Node.getClassCssMetaData();
    }

    static List<Style> getMatchingStyles(CssMetaData cssMetaData, Styleable styleable) {
        return CssStyleHelper.getMatchingStyles(styleable, cssMetaData);
    }

    final ObservableMap<StyleableProperty<?>, List<Style>> getStyleMap() {
        ObservableMap observableMap = (ObservableMap)this.getProperties().get("STYLEMAP");
        Map<StyleableProperty<?>, List<Style>> map = CssStyleHelper.getMatchingStyles(observableMap, this);
        if (map != null) {
            if (map instanceof ObservableMap) {
                return (ObservableMap)map;
            }
            return FXCollections.observableMap(map);
        }
        return FXCollections.emptyObservableMap();
    }

    final void setStyleMap(ObservableMap<StyleableProperty<?>, List<Style>> observableMap) {
        if (observableMap != null) {
            this.getProperties().put("STYLEMAP", observableMap);
        } else {
            this.getProperties().remove("STYLEMAP");
        }
    }

    Map<StyleableProperty<?>, List<Style>> findStyles(Map<StyleableProperty<?>, List<Style>> map) {
        Map<StyleableProperty<?>, List<Style>> map2 = CssStyleHelper.getMatchingStyles(map, this);
        return map2 != null ? map2 : Collections.emptyMap();
    }

    final CssFlags getCSSFlags() {
        return this.cssFlag;
    }

    private void requestCssStateTransition() {
        if (this.getScene() == null) {
            return;
        }
        if (this.cssFlag == CssFlags.CLEAN || this.cssFlag == CssFlags.DIRTY_BRANCH) {
            this.cssFlag = CssFlags.UPDATE;
            this.notifyParentsOfInvalidatedCSS();
        }
    }

    public final void pseudoClassStateChanged(PseudoClass pseudoClass, boolean bl) {
        boolean bl2;
        boolean bl3;
        boolean bl4 = bl3 = bl ? this.pseudoClassStates.add(pseudoClass) : this.pseudoClassStates.remove(pseudoClass);
        if (bl3 && this.styleHelper != null && (bl2 = this.styleHelper.pseudoClassStateChanged(pseudoClass))) {
            this.requestCssStateTransition();
        }
    }

    @Override
    public final ObservableSet<PseudoClass> getPseudoClassStates() {
        return FXCollections.unmodifiableObservableSet(this.pseudoClassStates);
    }

    final void notifyParentsOfInvalidatedCSS() {
        Parent parent;
        SubScene subScene = this.getSubScene();
        Parent parent2 = parent = subScene != null ? subScene.getRoot() : this.getScene().getRoot();
        if (!parent.isDirty(DirtyBits.NODE_CSS)) {
            NodeHelper.markDirty(parent, DirtyBits.NODE_CSS);
            if (subScene != null) {
                subScene.cssFlag = CssFlags.UPDATE;
                subScene.notifyParentsOfInvalidatedCSS();
            }
        }
        Parent parent3 = this.getParent();
        while (parent3 != null) {
            if (parent3.cssFlag == CssFlags.CLEAN) {
                parent3.cssFlag = CssFlags.DIRTY_BRANCH;
                parent3 = parent3.getParent();
                continue;
            }
            parent3 = null;
        }
    }

    final void reapplyCSS() {
        if (this.getScene() == null) {
            return;
        }
        if (this.cssFlag == CssFlags.REAPPLY) {
            return;
        }
        if (this.cssFlag == CssFlags.UPDATE) {
            this.cssFlag = CssFlags.REAPPLY;
            this.notifyParentsOfInvalidatedCSS();
            return;
        }
        this.reapplyCss();
        if (this.getParent() != null && this.getParent().isPerformingLayout()) {
            NodeHelper.processCSS(this);
        } else {
            this.notifyParentsOfInvalidatedCSS();
        }
    }

    private void reapplyCss() {
        CssStyleHelper cssStyleHelper = this.styleHelper;
        this.cssFlag = CssFlags.REAPPLY;
        this.styleHelper = CssStyleHelper.createStyleHelper(this);
        if (this instanceof Parent) {
            boolean bl;
            boolean bl2 = bl = this.styleHelper == null || cssStyleHelper != this.styleHelper || this.getParent() == null || this.getParent().cssFlag != CssFlags.CLEAN;
            if (bl) {
                ObservableList<Node> observableList = ((Parent)this).getChildren();
                int n = observableList.size();
                for (int i = 0; i < n; ++i) {
                    Node node = (Node)observableList.get(i);
                    node.reapplyCss();
                }
            }
        } else if (this instanceof SubScene) {
            Parent parent = ((SubScene)this).getRoot();
            if (parent != null) {
                super.reapplyCss();
            }
        } else if (this.styleHelper == null) {
            this.cssFlag = CssFlags.CLEAN;
            return;
        }
        this.cssFlag = CssFlags.UPDATE;
    }

    void processCSS() {
        switch (this.cssFlag) {
            case CLEAN: {
                break;
            }
            case DIRTY_BRANCH: {
                Parent parent = (Parent)this;
                parent.cssFlag = CssFlags.CLEAN;
                ObservableList<Node> observableList = parent.getChildren();
                int n = observableList.size();
                for (int i = 0; i < n; ++i) {
                    ((Node)observableList.get(i)).processCSS();
                }
                break;
            }
            default: {
                NodeHelper.processCSS(this);
            }
        }
    }

    public final void applyCss() {
        if (this.getScene() == null) {
            return;
        }
        if (this.cssFlag != CssFlags.REAPPLY) {
            this.cssFlag = CssFlags.UPDATE;
        }
        Node node = this;
        boolean bl = this.getScene().getRoot().isDirty(DirtyBits.NODE_CSS);
        if (bl) {
            for (Parent parent = this.getParent(); parent != null; parent = parent.getParent()) {
                if (parent.cssFlag != CssFlags.UPDATE && parent.cssFlag != CssFlags.REAPPLY) continue;
                node = parent;
            }
            if (node == this.getScene().getRoot()) {
                this.getScene().getRoot().clearDirty(DirtyBits.NODE_CSS);
            }
        }
        node.processCSS();
    }

    private void doProcessCSS() {
        if (this.cssFlag == CssFlags.CLEAN) {
            return;
        }
        if (this.cssFlag == CssFlags.REAPPLY) {
            this.reapplyCss();
        }
        this.cssFlag = CssFlags.CLEAN;
        if (this.styleHelper != null && this.getScene() != null) {
            this.styleHelper.transitionToState(this);
        }
    }

    public final void setAccessibleRole(AccessibleRole accessibleRole) {
        if (accessibleRole == null) {
            accessibleRole = AccessibleRole.NODE;
        }
        this.accessibleRoleProperty().set(accessibleRole);
    }

    public final AccessibleRole getAccessibleRole() {
        if (this.accessibleRole == null) {
            return AccessibleRole.NODE;
        }
        return (AccessibleRole)((Object)this.accessibleRoleProperty().get());
    }

    public final ObjectProperty<AccessibleRole> accessibleRoleProperty() {
        if (this.accessibleRole == null) {
            this.accessibleRole = new SimpleObjectProperty<AccessibleRole>(this, "accessibleRole", AccessibleRole.NODE);
        }
        return this.accessibleRole;
    }

    public final void setAccessibleRoleDescription(String string) {
        this.accessibleRoleDescriptionProperty().set(string);
    }

    public final String getAccessibleRoleDescription() {
        if (this.accessibilityProperties == null) {
            return null;
        }
        if (this.accessibilityProperties.accessibleRoleDescription == null) {
            return null;
        }
        return (String)this.accessibleRoleDescriptionProperty().get();
    }

    public final ObjectProperty<String> accessibleRoleDescriptionProperty() {
        return this.getAccessibilityProperties().getAccessibleRoleDescription();
    }

    public final void setAccessibleText(String string) {
        this.accessibleTextProperty().set(string);
    }

    public final String getAccessibleText() {
        if (this.accessibilityProperties == null) {
            return null;
        }
        if (this.accessibilityProperties.accessibleText == null) {
            return null;
        }
        return (String)this.accessibleTextProperty().get();
    }

    public final ObjectProperty<String> accessibleTextProperty() {
        return this.getAccessibilityProperties().getAccessibleText();
    }

    public final void setAccessibleHelp(String string) {
        this.accessibleHelpProperty().set(string);
    }

    public final String getAccessibleHelp() {
        if (this.accessibilityProperties == null) {
            return null;
        }
        if (this.accessibilityProperties.accessibleHelp == null) {
            return null;
        }
        return (String)this.accessibleHelpProperty().get();
    }

    public final ObjectProperty<String> accessibleHelpProperty() {
        return this.getAccessibilityProperties().getAccessibleHelp();
    }

    private AccessibilityProperties getAccessibilityProperties() {
        if (this.accessibilityProperties == null) {
            this.accessibilityProperties = new AccessibilityProperties();
        }
        return this.accessibilityProperties;
    }

    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case ROLE: {
                return this.getAccessibleRole();
            }
            case ROLE_DESCRIPTION: {
                return this.getAccessibleRoleDescription();
            }
            case TEXT: {
                return this.getAccessibleText();
            }
            case HELP: {
                return this.getAccessibleHelp();
            }
            case PARENT: {
                return this.getParent();
            }
            case SCENE: {
                return this.getScene();
            }
            case BOUNDS: {
                return this.localToScreen(this.getBoundsInLocal());
            }
            case DISABLED: {
                return this.isDisabled();
            }
            case FOCUSED: {
                return this.isFocused();
            }
            case VISIBLE: {
                return this.isVisible();
            }
            case LABELED_BY: {
                return this.labeledBy;
            }
        }
        return null;
    }

    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case REQUEST_FOCUS: {
                if (!this.isFocusTraversable()) break;
                this.requestFocus();
                break;
            }
            case SHOW_MENU: {
                Bounds bounds = this.getBoundsInLocal();
                Point2D point2D = this.localToScreen(bounds.getMaxX(), bounds.getMaxY());
                ContextMenuEvent contextMenuEvent = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED, bounds.getMaxX(), bounds.getMaxY(), point2D.getX(), point2D.getY(), false, new PickResult((EventTarget)this, bounds.getMaxX(), bounds.getMaxY()));
                Event.fireEvent(this, contextMenuEvent);
                break;
            }
        }
    }

    public final void notifyAccessibleAttributeChanged(AccessibleAttribute accessibleAttribute) {
        Scene scene;
        if (this.accessible == null && (scene = this.getScene()) != null) {
            this.accessible = scene.removeAccessible(this);
        }
        if (this.accessible != null) {
            this.accessible.sendNotification(accessibleAttribute);
        }
    }

    Accessible getAccessible() {
        Scene scene;
        if (this.accessible == null && (scene = this.getScene()) != null) {
            this.accessible = scene.removeAccessible(this);
        }
        if (this.accessible == null) {
            this.accessible = Application.GetApplication().createAccessible();
            this.accessible.setEventHandler(new Accessible.EventHandler(){

                @Override
                public AccessControlContext getAccessControlContext() {
                    Scene scene = Node.this.getScene();
                    if (scene == null) {
                        throw new RuntimeException("Accessbility requested for node not on a scene");
                    }
                    if (scene.getPeer() != null) {
                        return scene.getPeer().getAccessControlContext();
                    }
                    return scene.acc;
                }

                @Override
                public Object getAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
                    return Node.this.queryAccessibleAttribute(accessibleAttribute, objectArray);
                }

                @Override
                public void executeAction(AccessibleAction accessibleAction, Object ... objectArray) {
                    Node.this.executeAccessibleAction(accessibleAction, objectArray);
                }

                public String toString() {
                    String string = Node.this.getClass().getName();
                    return string.substring(string.lastIndexOf(46) + 1);
                }
            });
        }
        return this.accessible;
    }

    void releaseAccessible() {
        Accessible accessible = this.accessible;
        if (accessible != null) {
            this.accessible = null;
            accessible.dispose();
        }
    }

    private /* synthetic */ void lambda$snapshot$4(SnapshotParameters snapshotParameters, WritableImage writableImage, Callback callback) {
        WritableImage writableImage2 = this.doSnapshot(snapshotParameters, writableImage);
        SnapshotResult snapshotResult = new SnapshotResult(writableImage2, this, snapshotParameters);
        try {
            Void void_ = (Void)callback.call(snapshotResult);
        }
        catch (Throwable throwable) {
            System.err.println("Exception in snapshot callback");
            throwable.printStackTrace(System.err);
        }
    }

    static {
        PerformanceTracker.logEvent("Node class loaded");
        NodeHelper.setNodeAccessor(new NodeHelper.NodeAccessor(){

            @Override
            public NodeHelper getHelper(Node node) {
                return node.nodeHelper;
            }

            @Override
            public void setHelper(Node node, NodeHelper nodeHelper) {
                node.nodeHelper = nodeHelper;
            }

            @Override
            public void doMarkDirty(Node node, DirtyBits dirtyBits) {
                node.doMarkDirty(dirtyBits);
            }

            @Override
            public void doUpdatePeer(Node node) {
                node.doUpdatePeer();
            }

            @Override
            public BaseTransform getLeafTransform(Node node) {
                return node.getLeafTransform();
            }

            @Override
            public Bounds doComputeLayoutBounds(Node node) {
                return node.doComputeLayoutBounds();
            }

            @Override
            public void doTransformsChanged(Node node) {
                node.doTransformsChanged();
            }

            @Override
            public void doPickNodeLocal(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                node.doPickNodeLocal(pickRay, pickResultChooser);
            }

            @Override
            public boolean doComputeIntersects(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                return node.doComputeIntersects(pickRay, pickResultChooser);
            }

            @Override
            public void doGeomChanged(Node node) {
                node.doGeomChanged();
            }

            @Override
            public void doNotifyLayoutBoundsChanged(Node node) {
                node.doNotifyLayoutBoundsChanged();
            }

            @Override
            public void doProcessCSS(Node node) {
                node.doProcessCSS();
            }

            @Override
            public boolean isDirty(Node node, DirtyBits dirtyBits) {
                return node.isDirty(dirtyBits);
            }

            @Override
            public boolean isDirtyEmpty(Node node) {
                return node.isDirtyEmpty();
            }

            @Override
            public void syncPeer(Node node) {
                node.syncPeer();
            }

            @Override
            public void layoutBoundsChanged(Node node) {
                node.layoutBoundsChanged();
            }

            @Override
            public <P extends NGNode> P getPeer(Node node) {
                return node.getPeer();
            }

            @Override
            public void setShowMnemonics(Node node, boolean bl) {
                node.setShowMnemonics(bl);
            }

            @Override
            public boolean isShowMnemonics(Node node) {
                return node.isShowMnemonics();
            }

            @Override
            public BooleanProperty showMnemonicsProperty(Node node) {
                return node.showMnemonicsProperty();
            }

            @Override
            public boolean traverse(Node node, Direction direction) {
                return node.traverse(direction);
            }

            @Override
            public double getPivotX(Node node) {
                return node.getPivotX();
            }

            @Override
            public double getPivotY(Node node) {
                return node.getPivotY();
            }

            @Override
            public double getPivotZ(Node node) {
                return node.getPivotZ();
            }

            @Override
            public void pickNode(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                node.pickNode(pickRay, pickResultChooser);
            }

            @Override
            public boolean intersects(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                return node.intersects(pickRay, pickResultChooser);
            }

            @Override
            public double intersectsBounds(Node node, PickRay pickRay) {
                return node.intersectsBounds(pickRay);
            }

            @Override
            public void layoutNodeForPrinting(Node node) {
                node.doCSSLayoutSyncForSnapshot();
            }

            @Override
            public boolean isDerivedDepthTest(Node node) {
                return node.isDerivedDepthTest();
            }

            @Override
            public SubScene getSubScene(Node node) {
                return node.getSubScene();
            }

            @Override
            public void setLabeledBy(Node node, Node node2) {
                node.labeledBy = node2;
            }

            @Override
            public Accessible getAccessible(Node node) {
                return node.getAccessible();
            }

            @Override
            public void reapplyCSS(Node node) {
                node.reapplyCSS();
            }

            @Override
            public boolean isTreeVisible(Node node) {
                return node.isTreeVisible();
            }

            @Override
            public BooleanExpression treeVisibleProperty(Node node) {
                return node.treeVisibleProperty();
            }

            @Override
            public boolean isTreeShowing(Node node) {
                return node.isTreeShowing();
            }

            @Override
            public BooleanExpression treeShowingProperty(Node node) {
                return node.treeShowingProperty();
            }

            @Override
            public List<Style> getMatchingStyles(CssMetaData cssMetaData, Styleable styleable) {
                return Node.getMatchingStyles(cssMetaData, styleable);
            }

            @Override
            public Map<StyleableProperty<?>, List<Style>> findStyles(Node node, Map<StyleableProperty<?>, List<Style>> map) {
                return node.findStyles(map);
            }
        });
        USER_DATA_KEY = new Object();
        DEFAULT_ROTATION_AXIS = Rotate.Z_AXIS;
        DEFAULT_CACHE_HINT = CacheHint.DEFAULT;
        DEFAULT_CLIP = null;
        DEFAULT_CURSOR = null;
        DEFAULT_DEPTH_TEST = DepthTest.INHERIT;
        DEFAULT_EFFECT = null;
        DEFAULT_INPUT_METHOD_REQUESTS = null;
        HOVER_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("hover");
        PRESSED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("pressed");
        DISABLED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("disabled");
        FOCUSED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("focused");
        SHOW_MNEMONICS_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("show-mnemonics");
        boundsAccessor = (baseBounds, baseTransform, node) -> node.getGeomBounds(baseBounds, baseTransform);
    }

    private class AccessibilityProperties {
        ObjectProperty<String> accessibleRoleDescription;
        ObjectProperty<String> accessibleText;
        ObjectProperty<String> accessibleHelp;

        private AccessibilityProperties() {
        }

        ObjectProperty<String> getAccessibleRoleDescription() {
            if (this.accessibleRoleDescription == null) {
                this.accessibleRoleDescription = new SimpleObjectProperty<Object>(Node.this, "accessibleRoleDescription", null);
            }
            return this.accessibleRoleDescription;
        }

        ObjectProperty<String> getAccessibleText() {
            if (this.accessibleText == null) {
                this.accessibleText = new SimpleObjectProperty<Object>(Node.this, "accessibleText", null);
            }
            return this.accessibleText;
        }

        ObjectProperty<String> getAccessibleHelp() {
            if (this.accessibleHelp == null) {
                this.accessibleHelp = new SimpleObjectProperty<Object>(Node.this, "accessibleHelp", null);
            }
            return this.accessibleHelp;
        }
    }

    private static abstract class LazyBoundsProperty
    extends ReadOnlyObjectProperty<Bounds> {
        private ExpressionHelper<Bounds> helper;
        private boolean valid;
        private Bounds bounds;

        private LazyBoundsProperty() {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }

        @Override
        public void addListener(ChangeListener<? super Bounds> changeListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super Bounds> changeListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
        }

        @Override
        public Bounds get() {
            if (!this.valid) {
                this.bounds = this.computeBounds();
                this.valid = true;
            }
            return this.bounds;
        }

        public void invalidate() {
            if (this.valid) {
                this.valid = false;
                ExpressionHelper.fireValueChangedEvent(this.helper);
            }
        }

        protected abstract Bounds computeBounds();
    }

    private static abstract class LazyTransformProperty
    extends ReadOnlyObjectProperty<Transform> {
        protected static final int VALID = 0;
        protected static final int INVALID = 1;
        protected static final int VALIDITY_UNKNOWN = 2;
        protected int valid = 1;
        private ExpressionHelper<Transform> helper;
        private Transform transform;
        private boolean canReuse = false;

        private LazyTransformProperty() {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }

        @Override
        public void addListener(ChangeListener<? super Transform> changeListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super Transform> changeListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
        }

        protected Transform getInternalValue() {
            if (this.valid == 1 || this.valid == 2 && this.computeValidity() == 1) {
                this.transform = this.computeTransform(this.canReuse ? this.transform : null);
                this.canReuse = true;
                this.valid = this.validityKnown() ? 0 : 2;
            }
            return this.transform;
        }

        @Override
        public Transform get() {
            this.transform = this.getInternalValue();
            this.canReuse = false;
            return this.transform;
        }

        public void validityUnknown() {
            if (this.valid == 0) {
                this.valid = 2;
            }
        }

        public void invalidate() {
            if (this.valid != 1) {
                this.valid = 1;
                ExpressionHelper.fireValueChangedEvent(this.helper);
            }
        }

        protected abstract boolean validityKnown();

        protected abstract int computeValidity();

        protected abstract Transform computeTransform(Transform var1);
    }

    private static class StyleableProperties {
        private static final CssMetaData<Node, Cursor> CURSOR = new CssMetaData<Node, Cursor>("-fx-cursor", CursorConverter.getInstance()){

            @Override
            public boolean isSettable(Node node) {
                return node.miscProperties == null || node.miscProperties.canSetCursor();
            }

            @Override
            public StyleableProperty<Cursor> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.cursorProperty());
            }

            @Override
            public Cursor getInitialValue(Node node) {
                return node.getInitialCursor();
            }
        };
        private static final CssMetaData<Node, Effect> EFFECT = new CssMetaData<Node, Effect>("-fx-effect", EffectConverter.getInstance()){

            @Override
            public boolean isSettable(Node node) {
                return node.miscProperties == null || node.miscProperties.canSetEffect();
            }

            @Override
            public StyleableProperty<Effect> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.effectProperty());
            }
        };
        private static final CssMetaData<Node, Boolean> FOCUS_TRAVERSABLE = new CssMetaData<Node, Boolean>("-fx-focus-traversable", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(Node node) {
                return node.focusTraversable == null || !node.focusTraversable.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.focusTraversableProperty());
            }

            @Override
            public Boolean getInitialValue(Node node) {
                return node.getInitialFocusTraversable();
            }
        };
        private static final CssMetaData<Node, Number> OPACITY = new CssMetaData<Node, Number>("-fx-opacity", SizeConverter.getInstance(), (Number)1.0){

            @Override
            public boolean isSettable(Node node) {
                return node.opacity == null || !node.opacity.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.opacityProperty());
            }
        };
        private static final CssMetaData<Node, BlendMode> BLEND_MODE = new CssMetaData<Node, BlendMode>("-fx-blend-mode", new EnumConverter<BlendMode>(BlendMode.class)){

            @Override
            public boolean isSettable(Node node) {
                return node.blendMode == null || !node.blendMode.isBound();
            }

            @Override
            public StyleableProperty<BlendMode> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.blendModeProperty());
            }
        };
        private static final CssMetaData<Node, Number> ROTATE = new CssMetaData<Node, Number>("-fx-rotate", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(Node node) {
                return node.nodeTransformation == null || node.nodeTransformation.rotate == null || node.nodeTransformation.canSetRotate();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.rotateProperty());
            }
        };
        private static final CssMetaData<Node, Number> SCALE_X = new CssMetaData<Node, Number>("-fx-scale-x", SizeConverter.getInstance(), (Number)1.0){

            @Override
            public boolean isSettable(Node node) {
                return node.nodeTransformation == null || node.nodeTransformation.scaleX == null || node.nodeTransformation.canSetScaleX();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.scaleXProperty());
            }
        };
        private static final CssMetaData<Node, Number> SCALE_Y = new CssMetaData<Node, Number>("-fx-scale-y", SizeConverter.getInstance(), (Number)1.0){

            @Override
            public boolean isSettable(Node node) {
                return node.nodeTransformation == null || node.nodeTransformation.scaleY == null || node.nodeTransformation.canSetScaleY();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.scaleYProperty());
            }
        };
        private static final CssMetaData<Node, Number> SCALE_Z = new CssMetaData<Node, Number>("-fx-scale-z", SizeConverter.getInstance(), (Number)1.0){

            @Override
            public boolean isSettable(Node node) {
                return node.nodeTransformation == null || node.nodeTransformation.scaleZ == null || node.nodeTransformation.canSetScaleZ();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.scaleZProperty());
            }
        };
        private static final CssMetaData<Node, Number> TRANSLATE_X = new CssMetaData<Node, Number>("-fx-translate-x", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(Node node) {
                return node.nodeTransformation == null || node.nodeTransformation.translateX == null || node.nodeTransformation.canSetTranslateX();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.translateXProperty());
            }
        };
        private static final CssMetaData<Node, Number> TRANSLATE_Y = new CssMetaData<Node, Number>("-fx-translate-y", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(Node node) {
                return node.nodeTransformation == null || node.nodeTransformation.translateY == null || node.nodeTransformation.canSetTranslateY();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.translateYProperty());
            }
        };
        private static final CssMetaData<Node, Number> TRANSLATE_Z = new CssMetaData<Node, Number>("-fx-translate-z", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(Node node) {
                return node.nodeTransformation == null || node.nodeTransformation.translateZ == null || node.nodeTransformation.canSetTranslateZ();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.translateZProperty());
            }
        };
        private static final CssMetaData<Node, Number> VIEW_ORDER = new CssMetaData<Node, Number>("-fx-view-order", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(Node node) {
                return node.miscProperties == null || node.miscProperties.viewOrder == null || !node.miscProperties.viewOrder.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.viewOrderProperty());
            }
        };
        private static final CssMetaData<Node, Boolean> VISIBILITY = new CssMetaData<Node, Boolean>("visibility", (StyleConverter)new StyleConverter<String, Boolean>(){

            @Override
            public Boolean convert(ParsedValue<String, Boolean> parsedValue, Font font) {
                String string = parsedValue != null ? parsedValue.getValue() : null;
                return "visible".equalsIgnoreCase(string);
            }
        }, Boolean.TRUE){

            @Override
            public boolean isSettable(Node node) {
                return node.visible == null || !node.visible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Node node) {
                return (StyleableProperty)((Object)node.visibleProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList<CssMetaData<Node, Object>> arrayList = new ArrayList<CssMetaData<Node, Object>>();
            arrayList.add(CURSOR);
            arrayList.add(EFFECT);
            arrayList.add(FOCUS_TRAVERSABLE);
            arrayList.add(OPACITY);
            arrayList.add(BLEND_MODE);
            arrayList.add(ROTATE);
            arrayList.add(SCALE_X);
            arrayList.add(SCALE_Y);
            arrayList.add(SCALE_Z);
            arrayList.add(VIEW_ORDER);
            arrayList.add(TRANSLATE_X);
            arrayList.add(TRANSLATE_Y);
            arrayList.add(TRANSLATE_Z);
            arrayList.add(VISIBILITY);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    class TreeVisiblePropertyReadOnly
    extends BooleanExpression {
        private ExpressionHelper<Boolean> helper;
        private boolean valid;

        TreeVisiblePropertyReadOnly() {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> changeListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super Boolean> changeListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
        }

        protected void invalidate() {
            if (this.valid) {
                this.valid = false;
                ExpressionHelper.fireValueChangedEvent(this.helper);
            }
        }

        @Override
        public boolean get() {
            this.valid = true;
            return Node.this.treeVisible;
        }
    }

    class TreeShowingPropertyReadOnly
    extends BooleanExpression {
        private ExpressionHelper<Boolean> helper;
        private boolean valid;

        TreeShowingPropertyReadOnly() {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> changeListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super Boolean> changeListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
        }

        protected void invalidate() {
            if (this.valid) {
                this.valid = false;
                ExpressionHelper.fireValueChangedEvent(this.helper);
            }
        }

        @Override
        public boolean get() {
            this.valid = true;
            return Node.this.treeShowing;
        }
    }

    final class FocusedProperty
    extends ReadOnlyBooleanPropertyBase {
        private boolean value;
        private boolean valid = true;
        private boolean needsChangeEvent = false;

        FocusedProperty() {
        }

        public void store(boolean bl) {
            if (bl != this.value) {
                this.value = bl;
                this.markInvalid();
            }
        }

        public void notifyListeners() {
            if (this.needsChangeEvent) {
                this.fireValueChangedEvent();
                this.needsChangeEvent = false;
            }
        }

        private void markInvalid() {
            if (this.valid) {
                this.valid = false;
                Node.this.pseudoClassStateChanged(FOCUSED_PSEUDOCLASS_STATE, this.get());
                PlatformLogger platformLogger = Logging.getFocusLogger();
                if (platformLogger.isLoggable(PlatformLogger.Level.FINE)) {
                    platformLogger.fine(this + " focused=" + this.get());
                }
                this.needsChangeEvent = true;
                Node.this.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUSED);
            }
        }

        @Override
        public boolean get() {
            this.valid = true;
            return this.value;
        }

        @Override
        public Object getBean() {
            return Node.this;
        }

        @Override
        public String getName() {
            return "focused";
        }
    }

    private final class MiscProperties {
        private LazyBoundsProperty boundsInParent;
        private LazyBoundsProperty boundsInLocal;
        private BooleanProperty cache;
        private ObjectProperty<CacheHint> cacheHint;
        private ObjectProperty<Node> clip;
        private ObjectProperty<Cursor> cursor;
        private ObjectProperty<DepthTest> depthTest;
        private BooleanProperty disable;
        private ObjectProperty<Effect> effect;
        private ObjectProperty<InputMethodRequests> inputMethodRequests;
        private BooleanProperty mouseTransparent;
        private DoubleProperty viewOrder;

        private MiscProperties() {
        }

        public double getViewOrder() {
            return this.viewOrder == null ? 0.0 : this.viewOrder.get();
        }

        public final DoubleProperty viewOrderProperty() {
            if (this.viewOrder == null) {
                this.viewOrder = new StyleableDoubleProperty(0.0){

                    @Override
                    public void invalidated() {
                        Parent parent = Node.this.getParent();
                        if (parent != null) {
                            parent.markViewOrderChildrenDirty();
                        }
                        NodeHelper.markDirty(Node.this, DirtyBits.NODE_VIEW_ORDER);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.VIEW_ORDER;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "viewOrder";
                    }
                };
            }
            return this.viewOrder;
        }

        public final Bounds getBoundsInParent() {
            return (Bounds)this.boundsInParentProperty().get();
        }

        public final ReadOnlyObjectProperty<Bounds> boundsInParentProperty() {
            if (this.boundsInParent == null) {
                this.boundsInParent = new LazyBoundsProperty(){

                    @Override
                    protected Bounds computeBounds() {
                        BaseBounds baseBounds = TempState.getInstance().bounds;
                        baseBounds = Node.this.getTransformedBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM);
                        return new BoundingBox(baseBounds.getMinX(), baseBounds.getMinY(), baseBounds.getMinZ(), baseBounds.getWidth(), baseBounds.getHeight(), baseBounds.getDepth());
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "boundsInParent";
                    }
                };
            }
            return this.boundsInParent;
        }

        public void invalidateBoundsInParent() {
            if (this.boundsInParent != null) {
                this.boundsInParent.invalidate();
            }
        }

        public final Bounds getBoundsInLocal() {
            return (Bounds)this.boundsInLocalProperty().get();
        }

        public final ReadOnlyObjectProperty<Bounds> boundsInLocalProperty() {
            if (this.boundsInLocal == null) {
                this.boundsInLocal = new LazyBoundsProperty(){

                    @Override
                    protected Bounds computeBounds() {
                        BaseBounds baseBounds = TempState.getInstance().bounds;
                        baseBounds = Node.this.getLocalBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM);
                        return new BoundingBox(baseBounds.getMinX(), baseBounds.getMinY(), baseBounds.getMinZ(), baseBounds.getWidth(), baseBounds.getHeight(), baseBounds.getDepth());
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "boundsInLocal";
                    }
                };
            }
            return this.boundsInLocal;
        }

        public void invalidateBoundsInLocal() {
            if (this.boundsInLocal != null) {
                this.boundsInLocal.invalidate();
            }
        }

        public final boolean isCache() {
            return this.cache == null ? false : this.cache.get();
        }

        public final BooleanProperty cacheProperty() {
            if (this.cache == null) {
                this.cache = new BooleanPropertyBase(false){

                    @Override
                    protected void invalidated() {
                        NodeHelper.markDirty(Node.this, DirtyBits.NODE_CACHE);
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "cache";
                    }
                };
            }
            return this.cache;
        }

        public final CacheHint getCacheHint() {
            return this.cacheHint == null ? DEFAULT_CACHE_HINT : (CacheHint)((Object)this.cacheHint.get());
        }

        public final ObjectProperty<CacheHint> cacheHintProperty() {
            if (this.cacheHint == null) {
                this.cacheHint = new ObjectPropertyBase<CacheHint>(DEFAULT_CACHE_HINT){

                    @Override
                    protected void invalidated() {
                        NodeHelper.markDirty(Node.this, DirtyBits.NODE_CACHE);
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "cacheHint";
                    }
                };
            }
            return this.cacheHint;
        }

        public final Node getClip() {
            return this.clip == null ? DEFAULT_CLIP : (Node)this.clip.get();
        }

        public final ObjectProperty<Node> clipProperty() {
            if (this.clip == null) {
                this.clip = new ObjectPropertyBase<Node>(DEFAULT_CLIP){
                    private Node oldClip;

                    @Override
                    protected void invalidated() {
                        Node node = (Node)this.get();
                        if (node != null && (node.isConnected() && node.clipParent != Node.this || Node.this.wouldCreateCycle(Node.this, node))) {
                            String string;
                            String string2 = string = node.isConnected() && node.clipParent != Node.this ? "node already connected" : "cycle detected";
                            if (this.isBound()) {
                                this.unbind();
                                this.set(this.oldClip);
                                throw new IllegalArgumentException("Node's clip set to incorrect value  through binding (" + string + ", node  = " + Node.this + ", clip = " + MiscProperties.this.clip + "). Binding has been removed.");
                            }
                            this.set(this.oldClip);
                            throw new IllegalArgumentException("Node's clip set to incorrect value (" + string + ", node  = " + Node.this + ", clip = " + MiscProperties.this.clip + ").");
                        }
                        if (this.oldClip != null) {
                            this.oldClip.clipParent = null;
                            this.oldClip.setScenes(null, null);
                            this.oldClip.updateTreeVisible(false);
                        }
                        if (node != null) {
                            node.clipParent = Node.this;
                            node.setScenes(Node.this.getScene(), Node.this.getSubScene());
                            node.updateTreeVisible(true);
                        }
                        NodeHelper.markDirty(Node.this, DirtyBits.NODE_CLIP);
                        Node.this.localBoundsChanged();
                        this.oldClip = node;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "clip";
                    }
                };
            }
            return this.clip;
        }

        public final Cursor getCursor() {
            return this.cursor == null ? DEFAULT_CURSOR : (Cursor)this.cursor.get();
        }

        public final ObjectProperty<Cursor> cursorProperty() {
            if (this.cursor == null) {
                this.cursor = new StyleableObjectProperty<Cursor>(DEFAULT_CURSOR){

                    @Override
                    protected void invalidated() {
                        Scene scene = Node.this.getScene();
                        if (scene != null) {
                            scene.markCursorDirty();
                        }
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.CURSOR;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "cursor";
                    }
                };
            }
            return this.cursor;
        }

        public final DepthTest getDepthTest() {
            return this.depthTest == null ? DEFAULT_DEPTH_TEST : (DepthTest)((Object)this.depthTest.get());
        }

        public final ObjectProperty<DepthTest> depthTestProperty() {
            if (this.depthTest == null) {
                this.depthTest = new ObjectPropertyBase<DepthTest>(DEFAULT_DEPTH_TEST){

                    @Override
                    protected void invalidated() {
                        Node.this.computeDerivedDepthTest();
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "depthTest";
                    }
                };
            }
            return this.depthTest;
        }

        public final boolean isDisable() {
            return this.disable == null ? false : this.disable.get();
        }

        public final BooleanProperty disableProperty() {
            if (this.disable == null) {
                this.disable = new BooleanPropertyBase(false){

                    @Override
                    protected void invalidated() {
                        Node.this.updateDisabled();
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "disable";
                    }
                };
            }
            return this.disable;
        }

        public final Effect getEffect() {
            return this.effect == null ? DEFAULT_EFFECT : (Effect)this.effect.get();
        }

        public final ObjectProperty<Effect> effectProperty() {
            if (this.effect == null) {
                this.effect = new StyleableObjectProperty<Effect>(DEFAULT_EFFECT){
                    private Effect oldEffect;
                    private int oldBits;
                    private final AbstractNotifyListener effectChangeListener;
                    {
                        this.oldEffect = null;
                        this.effectChangeListener = new AbstractNotifyListener(){

                            @Override
                            public void invalidated(Observable observable) {
                                int n = ((IntegerProperty)observable).get();
                                int n2 = n ^ oldBits;
                                oldBits = n;
                                if (EffectDirtyBits.isSet(n2, EffectDirtyBits.EFFECT_DIRTY) && EffectDirtyBits.isSet(n, EffectDirtyBits.EFFECT_DIRTY)) {
                                    NodeHelper.markDirty(Node.this, DirtyBits.EFFECT_EFFECT);
                                }
                                if (EffectDirtyBits.isSet(n2, EffectDirtyBits.BOUNDS_CHANGED)) {
                                    Node.this.localBoundsChanged();
                                }
                            }
                        };
                    }

                    @Override
                    protected void invalidated() {
                        Effect effect = (Effect)this.get();
                        if (this.oldEffect != null) {
                            EffectHelper.effectDirtyProperty(this.oldEffect).removeListener(this.effectChangeListener.getWeakListener());
                        }
                        this.oldEffect = effect;
                        if (effect != null) {
                            EffectHelper.effectDirtyProperty(effect).addListener(this.effectChangeListener.getWeakListener());
                            if (EffectHelper.isEffectDirty(effect)) {
                                NodeHelper.markDirty(Node.this, DirtyBits.EFFECT_EFFECT);
                            }
                            this.oldBits = EffectHelper.effectDirtyProperty(effect).get();
                        }
                        NodeHelper.markDirty(Node.this, DirtyBits.NODE_EFFECT);
                        Node.this.localBoundsChanged();
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.EFFECT;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "effect";
                    }
                };
            }
            return this.effect;
        }

        public final InputMethodRequests getInputMethodRequests() {
            return this.inputMethodRequests == null ? DEFAULT_INPUT_METHOD_REQUESTS : (InputMethodRequests)this.inputMethodRequests.get();
        }

        public ObjectProperty<InputMethodRequests> inputMethodRequestsProperty() {
            if (this.inputMethodRequests == null) {
                this.inputMethodRequests = new SimpleObjectProperty<InputMethodRequests>(Node.this, "inputMethodRequests", DEFAULT_INPUT_METHOD_REQUESTS);
            }
            return this.inputMethodRequests;
        }

        public final boolean isMouseTransparent() {
            return this.mouseTransparent == null ? false : this.mouseTransparent.get();
        }

        public final BooleanProperty mouseTransparentProperty() {
            if (this.mouseTransparent == null) {
                this.mouseTransparent = new SimpleBooleanProperty(Node.this, "mouseTransparent", false);
            }
            return this.mouseTransparent;
        }

        public boolean canSetCursor() {
            return this.cursor == null || !this.cursor.isBound();
        }

        public boolean canSetEffect() {
            return this.effect == null || !this.effect.isBound();
        }
    }

    private final class EffectiveOrientationProperty
    extends ReadOnlyObjectPropertyBase<NodeOrientation> {
        private EffectiveOrientationProperty() {
        }

        @Override
        public NodeOrientation get() {
            return Node.this.getEffectiveNodeOrientation();
        }

        @Override
        public Object getBean() {
            return Node.this;
        }

        @Override
        public String getName() {
            return "effectiveNodeOrientation";
        }

        public void invalidate() {
            this.fireValueChangedEvent();
        }
    }

    private final class NodeTransformation {
        private DoubleProperty translateX;
        private DoubleProperty translateY;
        private DoubleProperty translateZ;
        private DoubleProperty scaleX;
        private DoubleProperty scaleY;
        private DoubleProperty scaleZ;
        private DoubleProperty rotate;
        private ObjectProperty<Point3D> rotationAxis;
        private ObservableList<Transform> transforms;
        private LazyTransformProperty localToParentTransform;
        private LazyTransformProperty localToSceneTransform;
        private int listenerReasons = 0;
        private InvalidationListener localToSceneInvLstnr;

        private NodeTransformation() {
        }

        private InvalidationListener getLocalToSceneInvalidationListener() {
            if (this.localToSceneInvLstnr == null) {
                this.localToSceneInvLstnr = observable -> this.invalidateLocalToSceneTransform();
            }
            return this.localToSceneInvLstnr;
        }

        public void incListenerReasons() {
            Parent parent;
            if (this.listenerReasons == 0 && (parent = Node.this.getParent()) != null) {
                parent.localToSceneTransformProperty().addListener(this.getLocalToSceneInvalidationListener());
            }
            ++this.listenerReasons;
        }

        public void decListenerReasons() {
            --this.listenerReasons;
            if (this.listenerReasons == 0) {
                Parent parent = Node.this.getParent();
                if (parent != null) {
                    parent.localToSceneTransformProperty().removeListener(this.getLocalToSceneInvalidationListener());
                }
                if (this.localToSceneTransform != null) {
                    this.localToSceneTransform.validityUnknown();
                }
            }
        }

        public final Transform getLocalToParentTransform() {
            return (Transform)this.localToParentTransformProperty().get();
        }

        public final ReadOnlyObjectProperty<Transform> localToParentTransformProperty() {
            if (this.localToParentTransform == null) {
                this.localToParentTransform = new LazyTransformProperty(){

                    @Override
                    protected Transform computeTransform(Transform transform) {
                        Node.this.updateLocalToParentTransform();
                        return TransformUtils.immutableTransform(transform, Node.this.localToParentTx.getMxx(), Node.this.localToParentTx.getMxy(), Node.this.localToParentTx.getMxz(), Node.this.localToParentTx.getMxt(), Node.this.localToParentTx.getMyx(), Node.this.localToParentTx.getMyy(), Node.this.localToParentTx.getMyz(), Node.this.localToParentTx.getMyt(), Node.this.localToParentTx.getMzx(), Node.this.localToParentTx.getMzy(), Node.this.localToParentTx.getMzz(), Node.this.localToParentTx.getMzt());
                    }

                    @Override
                    protected boolean validityKnown() {
                        return true;
                    }

                    @Override
                    protected int computeValidity() {
                        return this.valid;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "localToParentTransform";
                    }
                };
            }
            return this.localToParentTransform;
        }

        public void invalidateLocalToParentTransform() {
            if (this.localToParentTransform != null) {
                this.localToParentTransform.invalidate();
            }
        }

        public final Transform getLocalToSceneTransform() {
            return (Transform)this.localToSceneTransformProperty().get();
        }

        public final ReadOnlyObjectProperty<Transform> localToSceneTransformProperty() {
            if (this.localToSceneTransform == null) {
                this.localToSceneTransform = new LocalToSceneTransformProperty();
            }
            return this.localToSceneTransform;
        }

        public void invalidateLocalToSceneTransform() {
            if (this.localToSceneTransform != null) {
                this.localToSceneTransform.invalidate();
            }
        }

        public double getTranslateX() {
            return this.translateX == null ? 0.0 : this.translateX.get();
        }

        public final DoubleProperty translateXProperty() {
            if (this.translateX == null) {
                this.translateX = new StyleableDoubleProperty(0.0){

                    @Override
                    public void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.TRANSLATE_X;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "translateX";
                    }
                };
            }
            return this.translateX;
        }

        public double getTranslateY() {
            return this.translateY == null ? 0.0 : this.translateY.get();
        }

        public final DoubleProperty translateYProperty() {
            if (this.translateY == null) {
                this.translateY = new StyleableDoubleProperty(0.0){

                    @Override
                    public void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.TRANSLATE_Y;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "translateY";
                    }
                };
            }
            return this.translateY;
        }

        public double getTranslateZ() {
            return this.translateZ == null ? 0.0 : this.translateZ.get();
        }

        public final DoubleProperty translateZProperty() {
            if (this.translateZ == null) {
                this.translateZ = new StyleableDoubleProperty(0.0){

                    @Override
                    public void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.TRANSLATE_Z;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "translateZ";
                    }
                };
            }
            return this.translateZ;
        }

        public double getScaleX() {
            return this.scaleX == null ? 1.0 : this.scaleX.get();
        }

        public final DoubleProperty scaleXProperty() {
            if (this.scaleX == null) {
                this.scaleX = new StyleableDoubleProperty(1.0){

                    @Override
                    public void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.SCALE_X;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "scaleX";
                    }
                };
            }
            return this.scaleX;
        }

        public double getScaleY() {
            return this.scaleY == null ? 1.0 : this.scaleY.get();
        }

        public final DoubleProperty scaleYProperty() {
            if (this.scaleY == null) {
                this.scaleY = new StyleableDoubleProperty(1.0){

                    @Override
                    public void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.SCALE_Y;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "scaleY";
                    }
                };
            }
            return this.scaleY;
        }

        public double getScaleZ() {
            return this.scaleZ == null ? 1.0 : this.scaleZ.get();
        }

        public final DoubleProperty scaleZProperty() {
            if (this.scaleZ == null) {
                this.scaleZ = new StyleableDoubleProperty(1.0){

                    @Override
                    public void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.SCALE_Z;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "scaleZ";
                    }
                };
            }
            return this.scaleZ;
        }

        public double getRotate() {
            return this.rotate == null ? 0.0 : this.rotate.get();
        }

        public final DoubleProperty rotateProperty() {
            if (this.rotate == null) {
                this.rotate = new StyleableDoubleProperty(0.0){

                    @Override
                    public void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.ROTATE;
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "rotate";
                    }
                };
            }
            return this.rotate;
        }

        public Point3D getRotationAxis() {
            return this.rotationAxis == null ? DEFAULT_ROTATION_AXIS : (Point3D)this.rotationAxis.get();
        }

        public final ObjectProperty<Point3D> rotationAxisProperty() {
            if (this.rotationAxis == null) {
                this.rotationAxis = new ObjectPropertyBase<Point3D>(DEFAULT_ROTATION_AXIS){

                    @Override
                    protected void invalidated() {
                        NodeHelper.transformsChanged(Node.this);
                    }

                    @Override
                    public Object getBean() {
                        return Node.this;
                    }

                    @Override
                    public String getName() {
                        return "rotationAxis";
                    }
                };
            }
            return this.rotationAxis;
        }

        public ObservableList<Transform> getTransforms() {
            if (this.transforms == null) {
                this.transforms = new TrackableObservableList<Transform>(){

                    @Override
                    protected void onChanged(ListChangeListener.Change<Transform> change) {
                        while (change.next()) {
                            for (Transform transform : change.getRemoved()) {
                                TransformHelper.remove(transform, Node.this);
                            }
                            for (Transform transform : change.getAddedSubList()) {
                                TransformHelper.add(transform, Node.this);
                            }
                        }
                        NodeHelper.transformsChanged(Node.this);
                    }
                };
            }
            return this.transforms;
        }

        public boolean canSetTranslateX() {
            return this.translateX == null || !this.translateX.isBound();
        }

        public boolean canSetTranslateY() {
            return this.translateY == null || !this.translateY.isBound();
        }

        public boolean canSetTranslateZ() {
            return this.translateZ == null || !this.translateZ.isBound();
        }

        public boolean canSetScaleX() {
            return this.scaleX == null || !this.scaleX.isBound();
        }

        public boolean canSetScaleY() {
            return this.scaleY == null || !this.scaleY.isBound();
        }

        public boolean canSetScaleZ() {
            return this.scaleZ == null || !this.scaleZ.isBound();
        }

        public boolean canSetRotate() {
            return this.rotate == null || !this.rotate.isBound();
        }

        public boolean hasTransforms() {
            return this.transforms != null && !this.transforms.isEmpty();
        }

        public boolean hasScaleOrRotate() {
            if (this.scaleX != null && this.scaleX.get() != 1.0) {
                return true;
            }
            if (this.scaleY != null && this.scaleY.get() != 1.0) {
                return true;
            }
            if (this.scaleZ != null && this.scaleZ.get() != 1.0) {
                return true;
            }
            return this.rotate != null && this.rotate.get() != 0.0;
        }

        class LocalToSceneTransformProperty
        extends LazyTransformProperty {
            private List localToSceneListeners;
            private long stamp;
            private long parentStamp;

            LocalToSceneTransformProperty() {
            }

            @Override
            protected Transform computeTransform(Transform transform) {
                ++this.stamp;
                Node.this.updateLocalToParentTransform();
                Parent parent = Node.this.getParent();
                if (parent != null) {
                    LocalToSceneTransformProperty localToSceneTransformProperty = (LocalToSceneTransformProperty)parent.localToSceneTransformProperty();
                    Transform transform2 = localToSceneTransformProperty.getInternalValue();
                    this.parentStamp = localToSceneTransformProperty.stamp;
                    return TransformUtils.immutableTransform(transform, transform2, ((LazyTransformProperty)NodeTransformation.this.localToParentTransformProperty()).getInternalValue());
                }
                return TransformUtils.immutableTransform(transform, ((LazyTransformProperty)NodeTransformation.this.localToParentTransformProperty()).getInternalValue());
            }

            @Override
            public Object getBean() {
                return Node.this;
            }

            @Override
            public String getName() {
                return "localToSceneTransform";
            }

            @Override
            protected boolean validityKnown() {
                return NodeTransformation.this.listenerReasons > 0;
            }

            @Override
            protected int computeValidity() {
                if (this.valid != 2) {
                    return this.valid;
                }
                Node node = (Node)this.getBean();
                Parent parent = node.getParent();
                if (parent != null) {
                    LocalToSceneTransformProperty localToSceneTransformProperty = (LocalToSceneTransformProperty)parent.localToSceneTransformProperty();
                    if (this.parentStamp != localToSceneTransformProperty.stamp) {
                        this.valid = 1;
                        return 1;
                    }
                    int n = localToSceneTransformProperty.computeValidity();
                    if (n == 1) {
                        this.valid = 1;
                    }
                    return n;
                }
                return 0;
            }

            @Override
            public void addListener(InvalidationListener invalidationListener) {
                NodeTransformation.this.incListenerReasons();
                if (this.localToSceneListeners == null) {
                    this.localToSceneListeners = new LinkedList();
                }
                this.localToSceneListeners.add(invalidationListener);
                super.addListener(invalidationListener);
            }

            @Override
            public void addListener(ChangeListener<? super Transform> changeListener) {
                NodeTransformation.this.incListenerReasons();
                if (this.localToSceneListeners == null) {
                    this.localToSceneListeners = new LinkedList();
                }
                this.localToSceneListeners.add(changeListener);
                super.addListener(changeListener);
            }

            @Override
            public void removeListener(InvalidationListener invalidationListener) {
                if (this.localToSceneListeners != null && this.localToSceneListeners.remove(invalidationListener)) {
                    NodeTransformation.this.decListenerReasons();
                }
                super.removeListener(invalidationListener);
            }

            @Override
            public void removeListener(ChangeListener<? super Transform> changeListener) {
                if (this.localToSceneListeners != null && this.localToSceneListeners.remove(changeListener)) {
                    NodeTransformation.this.decListenerReasons();
                }
                super.removeListener(changeListener);
            }
        }
    }

    private class ReadOnlyObjectWrapperManualFire<T>
    extends ReadOnlyObjectWrapper<T> {
        private ReadOnlyObjectWrapperManualFire() {
        }

        @Override
        public Object getBean() {
            return Node.this;
        }

        @Override
        public String getName() {
            return "scene";
        }

        @Override
        protected void fireValueChangedEvent() {
        }

        public void fireSuperValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }
}

