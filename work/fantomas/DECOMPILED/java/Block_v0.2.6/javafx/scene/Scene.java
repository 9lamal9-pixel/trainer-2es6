/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.glass.ui.Accessible;
import com.sun.glass.ui.Application;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.event.EventQueue;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.logging.PulseLogger;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.scene.CssFlags;
import com.sun.javafx.scene.LayoutFlags;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneEventDispatcher;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.input.ClipboardHelper;
import com.sun.javafx.scene.input.DragboardHelper;
import com.sun.javafx.scene.input.ExtendedInputMethodRequests;
import com.sun.javafx.scene.input.InputEventUtils;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.scene.input.TouchPointHelper;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.SceneTraversalEngine;
import com.sun.javafx.scene.traversal.TopMostTraversalEngine;
import com.sun.javafx.sg.prism.NGCamera;
import com.sun.javafx.sg.prism.NGLightBase;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.TKClipboard;
import com.sun.javafx.tk.TKDragGestureListener;
import com.sun.javafx.tk.TKDragSourceListener;
import com.sun.javafx.tk.TKDropTargetListener;
import com.sun.javafx.tk.TKPulseListener;
import com.sun.javafx.tk.TKScene;
import com.sun.javafx.tk.TKSceneListener;
import com.sun.javafx.tk.TKScenePaintListener;
import com.sun.javafx.tk.TKStage;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Logging;
import com.sun.javafx.util.Utils;
import com.sun.prism.impl.PrismSettings;
import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.CssMetaData;
import javafx.css.StyleableObjectProperty;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Camera;
import javafx.scene.Cursor;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotResult;
import javafx.scene.SubScene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.GestureEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Mnemonic;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ZoomEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;

@DefaultProperty(value="root")
public class Scene
implements EventTarget {
    private double widthSetByUser = -1.0;
    private double heightSetByUser = -1.0;
    private boolean sizeInitialized = false;
    private final boolean depthBuffer;
    private final SceneAntialiasing antiAliasing;
    private int dirtyBits;
    final AccessControlContext acc = AccessController.getContext();
    private Camera defaultCamera;
    private Node transientFocusContainer;
    private static final int MIN_DIRTY_CAPACITY = 30;
    private static boolean inSynchronizer;
    private static boolean inMousePick;
    private static boolean allowPGAccess;
    private static int pgAccessCount;
    private static final boolean PLATFORM_DRAG_GESTURE_INITIATION = false;
    private Node[] dirtyNodes;
    private int dirtyNodesSize;
    private TKScene peer;
    ScenePulseListener scenePulseListener = new ScenePulseListener();
    private List<Runnable> preLayoutPulseListeners;
    private List<Runnable> postLayoutPulseListeners;
    private ReadOnlyObjectWrapper<Window> window;
    DnDGesture dndGesture = null;
    DragGestureListener dragGestureListener;
    private ReadOnlyDoubleWrapper x;
    private ReadOnlyDoubleWrapper y;
    private ReadOnlyDoubleWrapper width;
    private ReadOnlyDoubleWrapper height;
    private TargetWrapper tmpTargetWrapper = new TargetWrapper();
    private ObjectProperty<Camera> camera;
    private ObjectProperty<Paint> fill;
    private ObjectProperty<Parent> root;
    Parent oldRoot;
    private static TKPulseListener snapshotPulseListener;
    private static List<Runnable> snapshotRunnableListA;
    private static List<Runnable> snapshotRunnableListB;
    private static List<Runnable> snapshotRunnableList;
    private ObjectProperty<Cursor> cursor;
    private final ObservableList<String> stylesheets = new TrackableObservableList<String>(){

        @Override
        protected void onChanged(ListChangeListener.Change<String> change) {
            StyleManager.getInstance().stylesheetsChanged(Scene.this, change);
            change.reset();
            while (change.next() && !change.wasRemoved()) {
            }
            Scene.this.getRoot().reapplyCSS();
        }
    };
    private ObjectProperty<String> userAgentStylesheet = null;
    private PerformanceTracker tracker;
    private static final Object trackerMonitor;
    private MouseHandler mouseHandler;
    private ClickGenerator clickGenerator;
    private Point2D cursorScreenPos;
    private Point2D cursorScenePos;
    private final TouchGesture scrollGesture = new TouchGesture();
    private final TouchGesture zoomGesture = new TouchGesture();
    private final TouchGesture rotateGesture = new TouchGesture();
    private final TouchGesture swipeGesture = new TouchGesture();
    private TouchMap touchMap = new TouchMap();
    private TouchEvent nextTouchEvent = null;
    private TouchPoint[] touchPoints = null;
    private int touchEventSetId = 0;
    private int touchPointIndex = 0;
    private Map<Integer, EventTarget> touchTargets = new HashMap<Integer, EventTarget>();
    private KeyHandler keyHandler = null;
    private boolean focusDirty = true;
    private TopMostTraversalEngine traversalEngine = new SceneTraversalEngine(this);
    private Node oldFocusOwner;
    private ReadOnlyObjectWrapper<Node> focusOwner = new ReadOnlyObjectWrapper<Node>((Object)this, "focusOwner"){

        @Override
        protected void invalidated() {
            PlatformLogger platformLogger;
            Node node;
            if (Scene.this.oldFocusOwner != null) {
                ((Node.FocusedProperty)Scene.this.oldFocusOwner.focusedProperty()).store(false);
            }
            if ((node = (Node)this.get()) != null) {
                ((Node.FocusedProperty)node.focusedProperty()).store(Scene.this.keyHandler.windowFocused);
                if (node != Scene.this.oldFocusOwner) {
                    node.getScene().enableInputMethodEvents(node.getInputMethodRequests() != null && node.getOnInputMethodTextChanged() != null);
                }
            }
            Node node2 = Scene.this.oldFocusOwner;
            Scene.this.oldFocusOwner = node;
            if (node2 != null) {
                ((Node.FocusedProperty)node2.focusedProperty()).notifyListeners();
            }
            if (node != null) {
                ((Node.FocusedProperty)node.focusedProperty()).notifyListeners();
            }
            if ((platformLogger = Logging.getFocusLogger()).isLoggable(PlatformLogger.Level.FINE)) {
                if (node == this.get()) {
                    platformLogger.fine("Changed focus from " + node2 + " to " + node);
                } else {
                    platformLogger.fine("Changing focus from " + node2 + " to " + node + " canceled by nested requestFocus");
                }
            }
            if (Scene.this.accessible != null) {
                Scene.this.accessible.sendNotification(AccessibleAttribute.FOCUS_NODE);
            }
        }
    };
    Runnable testPulseListener = null;
    private List<LightBase> lights = new ArrayList<LightBase>();
    private ObjectProperty<EventDispatcher> eventDispatcher;
    private SceneEventDispatcher internalEventDispatcher;
    private ObjectProperty<EventHandler<? super ContextMenuEvent>> onContextMenuRequested;
    private ObjectProperty<EventHandler<? super MouseEvent>> onMouseClicked;
    private ObjectProperty<EventHandler<? super MouseEvent>> onMouseDragged;
    private ObjectProperty<EventHandler<? super MouseEvent>> onMouseEntered;
    private ObjectProperty<EventHandler<? super MouseEvent>> onMouseExited;
    private ObjectProperty<EventHandler<? super MouseEvent>> onMouseMoved;
    private ObjectProperty<EventHandler<? super MouseEvent>> onMousePressed;
    private ObjectProperty<EventHandler<? super MouseEvent>> onMouseReleased;
    private ObjectProperty<EventHandler<? super MouseEvent>> onDragDetected;
    private ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragOver;
    private ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragReleased;
    private ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragEntered;
    private ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragExited;
    private ObjectProperty<EventHandler<? super ScrollEvent>> onScrollStarted;
    private ObjectProperty<EventHandler<? super ScrollEvent>> onScroll;
    private ObjectProperty<EventHandler<? super ScrollEvent>> onScrollFinished;
    private ObjectProperty<EventHandler<? super RotateEvent>> onRotationStarted;
    private ObjectProperty<EventHandler<? super RotateEvent>> onRotate;
    private ObjectProperty<EventHandler<? super RotateEvent>> onRotationFinished;
    private ObjectProperty<EventHandler<? super ZoomEvent>> onZoomStarted;
    private ObjectProperty<EventHandler<? super ZoomEvent>> onZoom;
    private ObjectProperty<EventHandler<? super ZoomEvent>> onZoomFinished;
    private ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeUp;
    private ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeDown;
    private ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeLeft;
    private ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeRight;
    private ObjectProperty<EventHandler<? super TouchEvent>> onTouchPressed;
    private ObjectProperty<EventHandler<? super TouchEvent>> onTouchMoved;
    private ObjectProperty<EventHandler<? super TouchEvent>> onTouchReleased;
    private ObjectProperty<EventHandler<? super TouchEvent>> onTouchStationary;
    private ObjectProperty<EventHandler<? super DragEvent>> onDragEntered;
    private ObjectProperty<EventHandler<? super DragEvent>> onDragExited;
    private ObjectProperty<EventHandler<? super DragEvent>> onDragOver;
    private ObjectProperty<EventHandler<? super DragEvent>> onDragDropped;
    private ObjectProperty<EventHandler<? super DragEvent>> onDragDone;
    private ObjectProperty<EventHandler<? super KeyEvent>> onKeyPressed;
    private ObjectProperty<EventHandler<? super KeyEvent>> onKeyReleased;
    private ObjectProperty<EventHandler<? super KeyEvent>> onKeyTyped;
    private ObjectProperty<EventHandler<? super InputMethodEvent>> onInputMethodTextChanged;
    private static final Object USER_DATA_KEY;
    private ObservableMap<Object, Object> properties;
    private static final NodeOrientation defaultNodeOrientation;
    private ObjectProperty<NodeOrientation> nodeOrientation;
    private EffectiveOrientationProperty effectiveNodeOrientationProperty;
    private NodeOrientation effectiveNodeOrientation;
    private Map<Node, Accessible> accMap;
    private Accessible accessible;

    public Scene(@NamedArg(value="root") Parent parent) {
        this(parent, -1.0, -1.0, Color.WHITE, false, SceneAntialiasing.DISABLED);
    }

    public Scene(@NamedArg(value="root") Parent parent, @NamedArg(value="width") double d, @NamedArg(value="height") double d2) {
        this(parent, d, d2, Color.WHITE, false, SceneAntialiasing.DISABLED);
    }

    public Scene(@NamedArg(value="root") Parent parent, @NamedArg(value="fill", defaultValue="WHITE") Paint paint) {
        this(parent, -1.0, -1.0, paint, false, SceneAntialiasing.DISABLED);
    }

    public Scene(@NamedArg(value="root") Parent parent, @NamedArg(value="width") double d, @NamedArg(value="height") double d2, @NamedArg(value="fill", defaultValue="WHITE") Paint paint) {
        this(parent, d, d2, paint, false, SceneAntialiasing.DISABLED);
    }

    public Scene(@NamedArg(value="root") Parent parent, @NamedArg(value="width", defaultValue="-1") double d, @NamedArg(value="height", defaultValue="-1") double d2, @NamedArg(value="depthBuffer") boolean bl) {
        this(parent, d, d2, Color.WHITE, bl, SceneAntialiasing.DISABLED);
    }

    public Scene(@NamedArg(value="root") Parent parent, @NamedArg(value="width", defaultValue="-1") double d, @NamedArg(value="height", defaultValue="-1") double d2, @NamedArg(value="depthBuffer") boolean bl, @NamedArg(value="antiAliasing", defaultValue="DISABLED") SceneAntialiasing sceneAntialiasing) {
        this(parent, d, d2, Color.WHITE, bl, sceneAntialiasing);
        if (sceneAntialiasing != null && sceneAntialiasing != SceneAntialiasing.DISABLED && !Toolkit.getToolkit().isMSAASupported()) {
            String string = Scene.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support antiAliasing");
        }
    }

    private Scene(Parent parent, double d, double d2, Paint paint, boolean bl, SceneAntialiasing sceneAntialiasing) {
        this.depthBuffer = bl;
        this.antiAliasing = sceneAntialiasing;
        if (parent == null) {
            throw new NullPointerException("Root cannot be null");
        }
        if ((bl || sceneAntialiasing != null && sceneAntialiasing != SceneAntialiasing.DISABLED) && !Platform.isSupported(ConditionalFeature.SCENE3D)) {
            String string = Scene.class.getName();
            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
        }
        this.init();
        this.setRoot(parent);
        this.init(d, d2);
        this.setFill(paint);
    }

    static boolean isPGAccessAllowed() {
        return inSynchronizer || inMousePick || allowPGAccess;
    }

    static void setAllowPGAccess(boolean bl) {
        if (Utils.assertionEnabled()) {
            if (bl) {
                ++pgAccessCount;
                allowPGAccess = true;
            } else {
                if (pgAccessCount <= 0) {
                    throw new AssertionError((Object)"*** pgAccessCount underflow");
                }
                if (--pgAccessCount == 0) {
                    allowPGAccess = false;
                }
            }
        }
    }

    void addToDirtyList(Node node) {
        if ((this.dirtyNodes == null || this.dirtyNodesSize == 0) && this.peer != null) {
            Toolkit.getToolkit().requestNextPulse();
        }
        if (this.dirtyNodes != null) {
            if (this.dirtyNodesSize == this.dirtyNodes.length) {
                Node[] nodeArray = new Node[this.dirtyNodesSize + (this.dirtyNodesSize >> 1)];
                System.arraycopy(this.dirtyNodes, 0, nodeArray, 0, this.dirtyNodesSize);
                this.dirtyNodes = nodeArray;
            }
            this.dirtyNodes[this.dirtyNodesSize++] = node;
        }
    }

    private void doCSSPass() {
        Parent parent = this.getRoot();
        if (parent.cssFlag != CssFlags.CLEAN) {
            parent.clearDirty(com.sun.javafx.scene.DirtyBits.NODE_CSS);
            parent.processCSS();
        }
    }

    void doLayoutPass() {
        Parent parent = this.getRoot();
        if (parent != null) {
            parent.layout();
        }
    }

    TKScene getPeer() {
        return this.peer;
    }

    public final void addPreLayoutPulseListener(Runnable runnable) {
        Toolkit.getToolkit().checkFxUserThread();
        if (runnable == null) {
            throw new NullPointerException("Scene pulse listener should not be null");
        }
        if (this.preLayoutPulseListeners == null) {
            this.preLayoutPulseListeners = new CopyOnWriteArrayList<Runnable>();
        }
        this.preLayoutPulseListeners.add(runnable);
    }

    public final void removePreLayoutPulseListener(Runnable runnable) {
        Toolkit.getToolkit().checkFxUserThread();
        if (this.preLayoutPulseListeners == null) {
            return;
        }
        this.preLayoutPulseListeners.remove(runnable);
    }

    public final void addPostLayoutPulseListener(Runnable runnable) {
        Toolkit.getToolkit().checkFxUserThread();
        if (runnable == null) {
            throw new NullPointerException("Scene pulse listener should not be null");
        }
        if (this.postLayoutPulseListeners == null) {
            this.postLayoutPulseListeners = new CopyOnWriteArrayList<Runnable>();
        }
        this.postLayoutPulseListeners.add(runnable);
    }

    public final void removePostLayoutPulseListener(Runnable runnable) {
        Toolkit.getToolkit().checkFxUserThread();
        if (this.postLayoutPulseListeners == null) {
            return;
        }
        this.postLayoutPulseListeners.remove(runnable);
    }

    public final SceneAntialiasing getAntiAliasing() {
        return this.antiAliasing;
    }

    private boolean getAntiAliasingInternal() {
        return this.antiAliasing != null && Toolkit.getToolkit().isMSAASupported() && Platform.isSupported(ConditionalFeature.SCENE3D) ? this.antiAliasing != SceneAntialiasing.DISABLED : false;
    }

    void setWindow(Window window) {
        this.windowPropertyImpl().set(window);
    }

    public final Window getWindow() {
        return this.window == null ? null : (Window)this.window.get();
    }

    public final ReadOnlyObjectProperty<Window> windowProperty() {
        return this.windowPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Window> windowPropertyImpl() {
        if (this.window == null) {
            this.window = new ReadOnlyObjectWrapper<Window>(){
                private Window oldWindow;

                @Override
                protected void invalidated() {
                    Window window = (Window)this.get();
                    Scene.this.getKeyHandler().windowForSceneChanged(this.oldWindow, window);
                    if (this.oldWindow != null) {
                        Scene.this.disposePeer();
                    }
                    if (window != null) {
                        Scene.this.initPeer();
                    }
                    Scene.this.parentEffectiveOrientationInvalidated();
                    this.oldWindow = window;
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "window";
                }
            };
        }
        return this.window;
    }

    void initPeer() {
        assert (this.peer == null);
        Window window = this.getWindow();
        assert (window != null);
        TKStage tKStage = WindowHelper.getPeer(window);
        if (tKStage == null) {
            return;
        }
        boolean bl = Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW);
        if (!bl) {
            PlatformImpl.addNoTransparencyStylesheetToScene(this);
        }
        PerformanceTracker.logEvent("Scene.initPeer started");
        Scene.setAllowPGAccess(true);
        Toolkit toolkit = Toolkit.getToolkit();
        this.peer = tKStage.createTKScene(this.isDepthBufferInternal(), this.getAntiAliasingInternal(), this.acc);
        PerformanceTracker.logEvent("Scene.initPeer TKScene created");
        this.peer.setTKSceneListener(new ScenePeerListener());
        this.peer.setTKScenePaintListener(new ScenePeerPaintListener());
        PerformanceTracker.logEvent("Scene.initPeer TKScene set");
        this.peer.setRoot((NGNode)this.getRoot().getPeer());
        this.peer.setFillPaint(this.getFill() == null ? null : toolkit.getPaint(this.getFill()));
        NodeHelper.updatePeer(this.getEffectiveCamera());
        this.peer.setCamera((NGCamera)this.getEffectiveCamera().getPeer());
        this.peer.markDirty();
        PerformanceTracker.logEvent("Scene.initPeer TKScene initialized");
        Scene.setAllowPGAccess(false);
        toolkit.addSceneTkPulseListener(this.scenePulseListener);
        toolkit.enableDrop(this.peer, new DropTargetListener());
        toolkit.installInputMethodRequests(this.peer, new InputMethodRequestsDelegate());
        PerformanceTracker.logEvent("Scene.initPeer finished");
    }

    public void disposePeer() {
        if (this.peer == null) {
            return;
        }
        PerformanceTracker.logEvent("Scene.disposePeer started");
        Toolkit toolkit = Toolkit.getToolkit();
        toolkit.removeSceneTkPulseListener(this.scenePulseListener);
        if (this.accessible != null) {
            this.disposeAccessibles();
            Parent parent = this.getRoot();
            if (parent != null) {
                ((Node)parent).releaseAccessible();
            }
            this.accessible.dispose();
            this.accessible = null;
        }
        this.peer.dispose();
        this.peer = null;
        PerformanceTracker.logEvent("Scene.disposePeer finished");
    }

    private final void setX(double d) {
        this.xPropertyImpl().set(d);
    }

    public final double getX() {
        return this.x == null ? 0.0 : this.x.get();
    }

    public final ReadOnlyDoubleProperty xProperty() {
        return this.xPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyDoubleWrapper xPropertyImpl() {
        if (this.x == null) {
            this.x = new ReadOnlyDoubleWrapper(this, "x");
        }
        return this.x;
    }

    private final void setY(double d) {
        this.yPropertyImpl().set(d);
    }

    public final double getY() {
        return this.y == null ? 0.0 : this.y.get();
    }

    public final ReadOnlyDoubleProperty yProperty() {
        return this.yPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyDoubleWrapper yPropertyImpl() {
        if (this.y == null) {
            this.y = new ReadOnlyDoubleWrapper(this, "y");
        }
        return this.y;
    }

    private final void setWidth(double d) {
        this.widthPropertyImpl().set(d);
    }

    public final double getWidth() {
        return this.width == null ? 0.0 : this.width.get();
    }

    public final ReadOnlyDoubleProperty widthProperty() {
        return this.widthPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyDoubleWrapper widthPropertyImpl() {
        if (this.width == null) {
            this.width = new ReadOnlyDoubleWrapper(){

                @Override
                protected void invalidated() {
                    Parent parent = Scene.this.getRoot();
                    if (parent.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                        NodeHelper.transformsChanged(parent);
                    }
                    if (parent.isResizable()) {
                        Scene.this.resizeRootOnSceneSizeChange(this.get() - parent.getLayoutX() - parent.getTranslateX(), parent.getLayoutBounds().getHeight());
                    }
                    Scene.this.getEffectiveCamera().setViewWidth(this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "width";
                }
            };
        }
        return this.width;
    }

    private final void setHeight(double d) {
        this.heightPropertyImpl().set(d);
    }

    public final double getHeight() {
        return this.height == null ? 0.0 : this.height.get();
    }

    public final ReadOnlyDoubleProperty heightProperty() {
        return this.heightPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyDoubleWrapper heightPropertyImpl() {
        if (this.height == null) {
            this.height = new ReadOnlyDoubleWrapper(){

                @Override
                protected void invalidated() {
                    Parent parent = Scene.this.getRoot();
                    if (parent.isResizable()) {
                        Scene.this.resizeRootOnSceneSizeChange(parent.getLayoutBounds().getWidth(), this.get() - parent.getLayoutY() - parent.getTranslateY());
                    }
                    Scene.this.getEffectiveCamera().setViewHeight(this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "height";
                }
            };
        }
        return this.height;
    }

    void resizeRootOnSceneSizeChange(double d, double d2) {
        this.getRoot().resize(d, d2);
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
                        if (camera instanceof PerspectiveCamera && !Platform.isSupported(ConditionalFeature.SCENE3D)) {
                            String string = Scene.class.getName();
                            PlatformLogger.getLogger(string).warning("System can't support ConditionalFeature.SCENE3D");
                        }
                        if (camera.getScene() != null && camera.getScene() != Scene.this || camera.getSubScene() != null) {
                            throw new IllegalArgumentException(camera + "is already part of other scene or subscene");
                        }
                        camera.setOwnerScene(Scene.this);
                        camera.setViewWidth(Scene.this.getWidth());
                        camera.setViewHeight(Scene.this.getHeight());
                    }
                    if (this.oldCamera != null && this.oldCamera != camera) {
                        this.oldCamera.setOwnerScene(null);
                    }
                    this.oldCamera = camera;
                }

                @Override
                public Object getBean() {
                    return Scene.this;
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
        if (camera == null || camera instanceof PerspectiveCamera && !Platform.isSupported(ConditionalFeature.SCENE3D)) {
            if (this.defaultCamera == null) {
                this.defaultCamera = new ParallelCamera();
                this.defaultCamera.setOwnerScene(this);
                this.defaultCamera.setViewWidth(this.getWidth());
                this.defaultCamera.setViewHeight(this.getHeight());
            }
            return this.defaultCamera;
        }
        return camera;
    }

    void markCameraDirty() {
        this.markDirty(DirtyBits.CAMERA_DIRTY);
        this.setNeedsRepaint();
    }

    void markCursorDirty() {
        this.markDirty(DirtyBits.CURSOR_DIRTY);
    }

    public final void setFill(Paint paint) {
        this.fillProperty().set(paint);
    }

    public final Paint getFill() {
        return this.fill == null ? Color.WHITE : (Paint)this.fill.get();
    }

    public final ObjectProperty<Paint> fillProperty() {
        if (this.fill == null) {
            this.fill = new ObjectPropertyBase<Paint>((Paint)Color.WHITE){

                @Override
                protected void invalidated() {
                    Scene.this.markDirty(DirtyBits.FILL_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "fill";
                }
            };
        }
        return this.fill;
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
                    if (parent.getScene() != null && parent.getScene().getRoot() == parent && parent.getScene() != Scene.this) {
                        if (this.isBound()) {
                            this.forceUnbind();
                        }
                        throw new IllegalArgumentException(parent + "is already set as root of another scene");
                    }
                    if (Scene.this.oldRoot != null) {
                        Scene.this.oldRoot.setScenes(null, null);
                    }
                    Scene.this.oldRoot = parent;
                    parent.getStyleClass().add(0, "root");
                    parent.setScenes(Scene.this, null);
                    Scene.this.markDirty(DirtyBits.ROOT_DIRTY);
                    parent.resize(Scene.this.getWidth(), Scene.this.getHeight());
                    parent.requestLayout();
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "root";
                }
            };
        }
        return this.root;
    }

    void setNeedsRepaint() {
        if (this.peer != null) {
            this.peer.entireSceneNeedsRepaint();
        }
    }

    void doCSSLayoutSyncForSnapshot(Node node) {
        if (!this.sizeInitialized) {
            this.preferredSize();
        } else {
            this.doCSSPass();
        }
        this.doLayoutPass();
        this.getRoot().updateBounds();
        if (this.peer != null) {
            this.peer.waitForRenderingToComplete();
            this.peer.waitForSynchronization();
            try {
                this.scenePulseListener.synchronizeSceneNodes();
            }
            finally {
                this.peer.releaseSynchronization(false);
            }
        } else {
            this.scenePulseListener.synchronizeSceneNodes();
        }
    }

    static WritableImage doSnapshot(Scene scene, double d, double d2, double d3, double d4, Node node, BaseTransform baseTransform, boolean bl, Paint paint, Camera camera, WritableImage writableImage) {
        Toolkit toolkit = Toolkit.getToolkit();
        Toolkit.ImageRenderingContext imageRenderingContext = new Toolkit.ImageRenderingContext();
        int n = (int)Math.floor(d);
        int n2 = (int)Math.floor(d2);
        int n3 = (int)Math.ceil(d + d3);
        int n4 = (int)Math.ceil(d2 + d4);
        int n5 = Math.max(n3 - n, 1);
        int n6 = Math.max(n4 - n2, 1);
        if (writableImage == null) {
            writableImage = new WritableImage(n5, n6);
        } else {
            n5 = (int)writableImage.getWidth();
            n6 = (int)writableImage.getHeight();
        }
        Scene.setAllowPGAccess(true);
        imageRenderingContext.x = n;
        imageRenderingContext.y = n2;
        imageRenderingContext.width = n5;
        imageRenderingContext.height = n6;
        imageRenderingContext.transform = baseTransform;
        imageRenderingContext.depthBuffer = bl;
        imageRenderingContext.root = node.getPeer();
        imageRenderingContext.platformPaint = paint == null ? null : toolkit.getPaint(paint);
        double d5 = 1.0;
        double d6 = 1.0;
        if (camera != null) {
            d5 = camera.getViewWidth();
            d6 = camera.getViewHeight();
            camera.setViewWidth(n5);
            camera.setViewHeight(n6);
            NodeHelper.updatePeer(camera);
            imageRenderingContext.camera = (NGCamera)camera.getPeer();
        } else {
            imageRenderingContext.camera = null;
        }
        imageRenderingContext.lights = null;
        if (scene != null && !scene.lights.isEmpty()) {
            imageRenderingContext.lights = new NGLightBase[scene.lights.size()];
            for (int i = 0; i < scene.lights.size(); ++i) {
                imageRenderingContext.lights[i] = (NGLightBase)scene.lights.get(i).getPeer();
            }
        }
        Toolkit.WritableImageAccessor writableImageAccessor = Toolkit.getWritableImageAccessor();
        imageRenderingContext.platformImage = writableImageAccessor.getTkImageLoader(writableImage);
        Scene.setAllowPGAccess(false);
        Object object = toolkit.renderToImage(imageRenderingContext);
        writableImageAccessor.loadTkImage(writableImage, object);
        if (camera != null) {
            Scene.setAllowPGAccess(true);
            camera.setViewWidth(d5);
            camera.setViewHeight(d6);
            NodeHelper.updatePeer(camera);
            Scene.setAllowPGAccess(false);
        }
        if (scene != null && scene.peer != null) {
            scene.setNeedsRepaint();
        }
        return writableImage;
    }

    private WritableImage doSnapshot(WritableImage writableImage) {
        this.doCSSLayoutSyncForSnapshot(this.getRoot());
        double d = this.getWidth();
        double d2 = this.getHeight();
        BaseTransform baseTransform = BaseTransform.IDENTITY_TRANSFORM;
        return Scene.doSnapshot(this, 0.0, 0.0, d, d2, this.getRoot(), baseTransform, this.isDepthBufferInternal(), this.getFill(), this.getEffectiveCamera(), writableImage);
    }

    static void addSnapshotRunnable(Runnable runnable) {
        Toolkit.getToolkit().checkFxUserThread();
        if (snapshotPulseListener == null) {
            snapshotRunnableListA = new ArrayList<Runnable>();
            snapshotRunnableListB = new ArrayList<Runnable>();
            snapshotRunnableList = snapshotRunnableListA;
            snapshotPulseListener = () -> {
                if (snapshotRunnableList.size() > 0) {
                    List<Runnable> list = snapshotRunnableList;
                    snapshotRunnableList = snapshotRunnableList == snapshotRunnableListA ? snapshotRunnableListB : snapshotRunnableListA;
                    for (Runnable runnable : list) {
                        try {
                            runnable.run();
                        }
                        catch (Throwable throwable) {
                            System.err.println("Exception in snapshot runnable");
                            throwable.printStackTrace(System.err);
                        }
                    }
                    list.clear();
                }
            };
            Toolkit.getToolkit().addPostSceneTkPulseListener(snapshotPulseListener);
        }
        AccessControlContext accessControlContext = AccessController.getContext();
        snapshotRunnableList.add(() -> AccessController.doPrivileged(() -> {
            runnable.run();
            return null;
        }, accessControlContext));
        Toolkit.getToolkit().requestNextPulse();
    }

    public WritableImage snapshot(WritableImage writableImage) {
        Toolkit.getToolkit().checkFxUserThread();
        return this.doSnapshot(writableImage);
    }

    public void snapshot(Callback<SnapshotResult, Void> callback, WritableImage writableImage) {
        Toolkit.getToolkit().checkFxUserThread();
        if (callback == null) {
            throw new NullPointerException("The callback must not be null");
        }
        Callback<SnapshotResult, Void> callback2 = callback;
        WritableImage writableImage2 = writableImage;
        Runnable runnable = () -> {
            WritableImage writableImage2 = this.doSnapshot(writableImage2);
            SnapshotResult snapshotResult = new SnapshotResult(writableImage2, this, null);
            try {
                Void void_ = (Void)callback2.call(snapshotResult);
            }
            catch (Throwable throwable) {
                System.err.println("Exception in snapshot callback");
                throwable.printStackTrace(System.err);
            }
        };
        Scene.addSnapshotRunnable(runnable);
    }

    public final void setCursor(Cursor cursor) {
        this.cursorProperty().set(cursor);
    }

    public final Cursor getCursor() {
        return this.cursor == null ? null : (Cursor)this.cursor.get();
    }

    public final ObjectProperty<Cursor> cursorProperty() {
        if (this.cursor == null) {
            this.cursor = new ObjectPropertyBase<Cursor>(){

                @Override
                protected void invalidated() {
                    Scene.this.markCursorDirty();
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "cursor";
                }
            };
        }
        return this.cursor;
    }

    public Node lookup(String string) {
        return this.getRoot().lookup(string);
    }

    public final ObservableList<String> getStylesheets() {
        return this.stylesheets;
    }

    public final ObjectProperty<String> userAgentStylesheetProperty() {
        if (this.userAgentStylesheet == null) {
            this.userAgentStylesheet = new SimpleObjectProperty<String>((Object)this, "userAgentStylesheet", null){

                @Override
                protected void invalidated() {
                    StyleManager.getInstance().forget(Scene.this);
                    Scene.this.getRoot().reapplyCSS();
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

    public final boolean isDepthBuffer() {
        return this.depthBuffer;
    }

    boolean isDepthBufferInternal() {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            return false;
        }
        return this.depthBuffer;
    }

    private void init(double d, double d2) {
        if (d >= 0.0) {
            this.widthSetByUser = d;
            this.setWidth((float)d);
        }
        if (d2 >= 0.0) {
            this.heightSetByUser = d2;
            this.setHeight((float)d2);
        }
        this.sizeInitialized = this.widthSetByUser >= 0.0 && this.heightSetByUser >= 0.0;
    }

    private void init() {
        if (PerformanceTracker.isLoggingEnabled()) {
            PerformanceTracker.logEvent("Scene.init for [" + this + "]");
        }
        this.mouseHandler = new MouseHandler();
        this.clickGenerator = new ClickGenerator();
        if (PerformanceTracker.isLoggingEnabled()) {
            PerformanceTracker.logEvent("Scene.init for [" + this + "] - finished");
        }
    }

    void preferredSize() {
        Parent parent = this.getRoot();
        this.doCSSPass();
        this.resizeRootToPreferredSize(parent);
        this.doLayoutPass();
        if (this.widthSetByUser < 0.0) {
            this.setWidth(parent.isResizable() ? parent.getLayoutX() + parent.getTranslateX() + parent.getLayoutBounds().getWidth() : parent.getBoundsInParent().getMaxX());
        } else {
            this.setWidth(this.widthSetByUser);
        }
        if (this.heightSetByUser < 0.0) {
            this.setHeight(parent.isResizable() ? parent.getLayoutY() + parent.getTranslateY() + parent.getLayoutBounds().getHeight() : parent.getBoundsInParent().getMaxY());
        } else {
            this.setHeight(this.heightSetByUser);
        }
        this.sizeInitialized = this.getWidth() > 0.0 && this.getHeight() > 0.0;
        PerformanceTracker.logEvent("Scene preferred bounds computation complete");
    }

    final void resizeRootToPreferredSize(Parent parent) {
        double d;
        double d2;
        Orientation orientation = parent.getContentBias();
        if (orientation == null) {
            d2 = Scene.getPreferredWidth(parent, this.widthSetByUser, -1.0);
            d = Scene.getPreferredHeight(parent, this.heightSetByUser, -1.0);
        } else if (orientation == Orientation.HORIZONTAL) {
            d2 = Scene.getPreferredWidth(parent, this.widthSetByUser, -1.0);
            d = Scene.getPreferredHeight(parent, this.heightSetByUser, d2);
        } else {
            d = Scene.getPreferredHeight(parent, this.heightSetByUser, -1.0);
            d2 = Scene.getPreferredWidth(parent, this.widthSetByUser, d);
        }
        parent.resize(d2, d);
    }

    private static double getPreferredWidth(Parent parent, double d, double d2) {
        if (d >= 0.0) {
            return d;
        }
        double d3 = d2 >= 0.0 ? d2 : -1.0;
        return parent.boundedSize(parent.prefWidth(d3), parent.minWidth(d3), parent.maxWidth(d3));
    }

    private static double getPreferredHeight(Parent parent, double d, double d2) {
        if (d >= 0.0) {
            return d;
        }
        double d3 = d2 >= 0.0 ? d2 : -1.0;
        return parent.boundedSize(parent.prefHeight(d3), parent.minHeight(d3), parent.maxHeight(d3));
    }

    void processMouseEvent(MouseEvent mouseEvent) {
        this.mouseHandler.process(mouseEvent, false);
    }

    private void processMenuEvent(double d, double d2, double d3, double d4, boolean bl) {
        Object object;
        Object object2 = null;
        inMousePick = true;
        if (bl) {
            object = this.getFocusOwner();
            double d5 = d3 - d;
            double d6 = d4 - d2;
            if (object != null) {
                Bounds bounds = ((Node)object).localToScene(((Node)object).getBoundsInLocal());
                d = bounds.getMinX() + bounds.getWidth() / 4.0;
                d2 = bounds.getMinY() + bounds.getHeight() / 2.0;
                object2 = object;
            } else {
                d = this.getWidth() / 4.0;
                d2 = this.getWidth() / 2.0;
                object2 = this;
            }
            d3 = d + d5;
            d4 = d2 + d6;
        }
        object = this.pick(d, d2);
        if (!bl && (object2 = ((PickResult)object).getIntersectedNode()) == null) {
            object2 = this;
        }
        if (object2 != null) {
            ContextMenuEvent contextMenuEvent = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED, d, d2, d3, d4, bl, (PickResult)object);
            Event.fireEvent((EventTarget)object2, contextMenuEvent);
        }
        inMousePick = false;
    }

    private void processGestureEvent(GestureEvent gestureEvent, TouchGesture touchGesture) {
        EventTarget eventTarget = null;
        if (gestureEvent.getEventType() == ZoomEvent.ZOOM_STARTED || gestureEvent.getEventType() == RotateEvent.ROTATION_STARTED || gestureEvent.getEventType() == ScrollEvent.SCROLL_STARTED) {
            touchGesture.target = null;
            touchGesture.finished = false;
        }
        if (touchGesture.target != null && (!touchGesture.finished || gestureEvent.isInertia())) {
            eventTarget = (EventTarget)touchGesture.target.get();
        } else {
            eventTarget = gestureEvent.getPickResult().getIntersectedNode();
            if (eventTarget == null) {
                eventTarget = this;
            }
        }
        if (gestureEvent.getEventType() == ZoomEvent.ZOOM_STARTED || gestureEvent.getEventType() == RotateEvent.ROTATION_STARTED || gestureEvent.getEventType() == ScrollEvent.SCROLL_STARTED) {
            touchGesture.target = new WeakReference<Node>((Node)eventTarget);
        }
        if (gestureEvent.getEventType() != ZoomEvent.ZOOM_FINISHED && gestureEvent.getEventType() != RotateEvent.ROTATION_FINISHED && gestureEvent.getEventType() != ScrollEvent.SCROLL_FINISHED && !gestureEvent.isInertia()) {
            touchGesture.sceneCoords = new Point2D(gestureEvent.getSceneX(), gestureEvent.getSceneY());
            touchGesture.screenCoords = new Point2D(gestureEvent.getScreenX(), gestureEvent.getScreenY());
        }
        if (eventTarget != null) {
            Event.fireEvent(eventTarget, gestureEvent);
        }
        if (gestureEvent.getEventType() == ZoomEvent.ZOOM_FINISHED || gestureEvent.getEventType() == RotateEvent.ROTATION_FINISHED || gestureEvent.getEventType() == ScrollEvent.SCROLL_FINISHED) {
            touchGesture.finished = true;
        }
    }

    private void processTouchEvent(TouchEvent touchEvent, TouchPoint[] touchPointArray) {
        Object object;
        inMousePick = true;
        ++this.touchEventSetId;
        List<TouchPoint> list = Arrays.asList(touchPointArray);
        for (TouchPoint touchPoint : touchPointArray) {
            if (touchPoint.getTarget() == null) continue;
            object = null;
            switch (touchPoint.getState()) {
                case MOVED: {
                    object = TouchEvent.TOUCH_MOVED;
                    break;
                }
                case PRESSED: {
                    object = TouchEvent.TOUCH_PRESSED;
                    break;
                }
                case RELEASED: {
                    object = TouchEvent.TOUCH_RELEASED;
                    break;
                }
                case STATIONARY: {
                    object = TouchEvent.TOUCH_STATIONARY;
                }
            }
            for (TouchPoint touchPoint2 : touchPointArray) {
                TouchPointHelper.reset(touchPoint2);
            }
            TouchEvent object2 = new TouchEvent((EventType<TouchEvent>)object, touchPoint, list, this.touchEventSetId, touchEvent.isShiftDown(), touchEvent.isControlDown(), touchEvent.isAltDown(), touchEvent.isMetaDown());
            Event.fireEvent(touchPoint.getTarget(), object2);
        }
        for (TouchPoint touchPoint : touchPointArray) {
            object = touchPoint.getGrabbed();
            if (object != null) {
                this.touchTargets.put(touchPoint.getId(), (EventTarget)object);
            }
            if (object != null && touchPoint.getState() != TouchPoint.State.RELEASED) continue;
            this.touchTargets.remove(touchPoint.getId());
        }
        inMousePick = false;
    }

    Node test_pick(double d, double d2) {
        inMousePick = true;
        PickResult pickResult = this.mouseHandler.pickNode(new PickRay(d, d2, 1.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        inMousePick = false;
        if (pickResult != null) {
            return pickResult.getIntersectedNode();
        }
        return null;
    }

    private PickResult pick(double d, double d2) {
        this.pick(this.tmpTargetWrapper, d, d2);
        return this.tmpTargetWrapper.getResult();
    }

    private boolean isInScene(double d, double d2) {
        if (d < 0.0 || d2 < 0.0 || d > this.getWidth() || d2 > this.getHeight()) {
            return false;
        }
        Window window = this.getWindow();
        return !(window instanceof Stage) || ((Stage)window).getStyle() != StageStyle.TRANSPARENT || this.getFill() != null;
    }

    private void pick(TargetWrapper targetWrapper, double d, double d2) {
        PickRay pickRay = this.getEffectiveCamera().computePickRay(d, d2, null);
        double d3 = pickRay.getDirectionNoClone().length();
        pickRay.getDirectionNoClone().normalize();
        PickResult pickResult = this.mouseHandler.pickNode(pickRay);
        if (pickResult != null) {
            targetWrapper.setNodeResult(pickResult);
        } else {
            Vec3d vec3d = pickRay.getOriginNoClone();
            Vec3d vec3d2 = pickRay.getDirectionNoClone();
            targetWrapper.setSceneResult(new PickResult(null, new Point3D(vec3d.x + d3 * vec3d2.x, vec3d.y + d3 * vec3d2.y, vec3d.z + d3 * vec3d2.z), d3), this.isInScene(d, d2) ? this : null);
        }
    }

    private KeyHandler getKeyHandler() {
        if (this.keyHandler == null) {
            this.keyHandler = new KeyHandler();
        }
        return this.keyHandler;
    }

    final void setFocusDirty(boolean bl) {
        if (!this.focusDirty) {
            Toolkit.getToolkit().requestNextPulse();
        }
        this.focusDirty = bl;
    }

    final boolean isFocusDirty() {
        return this.focusDirty;
    }

    boolean traverse(Node node, Direction direction) {
        if (node.getSubScene() != null) {
            return node.getSubScene().traverse(node, direction);
        }
        return this.traversalEngine.trav(node, direction) != null;
    }

    private void focusInitial() {
        this.traversalEngine.traverseToFirst();
    }

    private void focusIneligible(Node node) {
        this.traverse(node, Direction.NEXT);
    }

    public void processKeyEvent(KeyEvent keyEvent) {
        if (this.dndGesture != null && !this.dndGesture.processKey(keyEvent)) {
            this.dndGesture = null;
        }
        this.getKeyHandler().process(keyEvent);
    }

    void requestFocus(Node node) {
        this.getKeyHandler().requestFocus(node);
    }

    public final Node getFocusOwner() {
        return (Node)this.focusOwner.get();
    }

    public final ReadOnlyObjectProperty<Node> focusOwnerProperty() {
        return this.focusOwner.getReadOnlyProperty();
    }

    void focusCleanup() {
        this.scenePulseListener.focusCleanup();
    }

    private void processInputMethodEvent(InputMethodEvent inputMethodEvent) {
        Node node = this.getFocusOwner();
        if (node != null) {
            node.fireEvent(inputMethodEvent);
        }
    }

    public void enableInputMethodEvents(boolean bl) {
        if (this.peer != null) {
            this.peer.enableInputMethodEvents(bl);
        }
    }

    boolean isQuiescent() {
        Parent parent = this.getRoot();
        return !this.isFocusDirty() && (parent == null || parent.cssFlag == CssFlags.CLEAN && parent.layoutFlag == LayoutFlags.CLEAN);
    }

    private void markDirty(DirtyBits dirtyBits) {
        this.setDirty(dirtyBits);
        if (this.peer != null) {
            Toolkit.getToolkit().requestNextPulse();
        }
    }

    private void setDirty(DirtyBits dirtyBits) {
        this.dirtyBits |= dirtyBits.getMask();
    }

    private boolean isDirty(DirtyBits dirtyBits) {
        return (this.dirtyBits & dirtyBits.getMask()) != 0;
    }

    private boolean isDirtyEmpty() {
        return this.dirtyBits == 0;
    }

    private void clearDirty() {
        this.dirtyBits = 0;
    }

    final void addLight(LightBase lightBase) {
        if (!this.lights.contains(lightBase)) {
            this.lights.add(lightBase);
            this.markDirty(DirtyBits.LIGHTS_DIRTY);
        }
    }

    final void removeLight(LightBase lightBase) {
        if (this.lights.remove(lightBase)) {
            this.markDirty(DirtyBits.LIGHTS_DIRTY);
        }
    }

    private void syncLights() {
        if (!this.isDirty(DirtyBits.LIGHTS_DIRTY)) {
            return;
        }
        inSynchronizer = true;
        NGLightBase[] nGLightBaseArray = this.peer.getLights();
        if (!this.lights.isEmpty() || nGLightBaseArray != null) {
            if (this.lights.isEmpty()) {
                this.peer.setLights(null);
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
                this.peer.setLights(nGLightBaseArray);
            }
        }
        inSynchronizer = false;
    }

    void generateMouseExited(Node node) {
        this.mouseHandler.handleNodeRemoval(node);
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

    private SceneEventDispatcher getInternalEventDispatcher() {
        this.initializeInternalEventDispatcher();
        return this.internalEventDispatcher;
    }

    final void initializeInternalEventDispatcher() {
        if (this.internalEventDispatcher == null) {
            this.internalEventDispatcher = this.createInternalEventDispatcher();
            this.eventDispatcher = new SimpleObjectProperty<SceneEventDispatcher>(this, "eventDispatcher", this.internalEventDispatcher);
        }
    }

    private SceneEventDispatcher createInternalEventDispatcher() {
        return new SceneEventDispatcher(this);
    }

    public void addMnemonic(Mnemonic mnemonic) {
        this.getInternalEventDispatcher().getKeyboardShortcutsHandler().addMnemonic(mnemonic);
    }

    public void removeMnemonic(Mnemonic mnemonic) {
        this.getInternalEventDispatcher().getKeyboardShortcutsHandler().removeMnemonic(mnemonic);
    }

    final void clearNodeMnemonics(Node node) {
        this.getInternalEventDispatcher().getKeyboardShortcutsHandler().clearNodeMnemonics(node);
    }

    public ObservableMap<KeyCombination, ObservableList<Mnemonic>> getMnemonics() {
        return this.getInternalEventDispatcher().getKeyboardShortcutsHandler().getMnemonics();
    }

    public ObservableMap<KeyCombination, Runnable> getAccelerators() {
        return this.getInternalEventDispatcher().getKeyboardShortcutsHandler().getAccelerators();
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        EventDispatcher eventDispatcher;
        if (this.eventDispatcher != null && (eventDispatcher = (EventDispatcher)this.eventDispatcher.get()) != null) {
            eventDispatchChain = eventDispatchChain.prepend(eventDispatcher);
        }
        if (this.getWindow() != null) {
            eventDispatchChain = this.getWindow().buildEventDispatchChain(eventDispatchChain);
        }
        return eventDispatchChain;
    }

    public final void setOnContextMenuRequested(EventHandler<? super ContextMenuEvent> eventHandler) {
        this.onContextMenuRequestedProperty().set(eventHandler);
    }

    public final EventHandler<? super ContextMenuEvent> getOnContextMenuRequested() {
        return this.onContextMenuRequested == null ? null : (EventHandler)this.onContextMenuRequested.get();
    }

    public final ObjectProperty<EventHandler<? super ContextMenuEvent>> onContextMenuRequestedProperty() {
        if (this.onContextMenuRequested == null) {
            this.onContextMenuRequested = new ObjectPropertyBase<EventHandler<? super ContextMenuEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onContextMenuRequested";
                }
            };
        }
        return this.onContextMenuRequested;
    }

    public final void setOnMouseClicked(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseClickedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseClicked() {
        return this.onMouseClicked == null ? null : (EventHandler)this.onMouseClicked.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseClickedProperty() {
        if (this.onMouseClicked == null) {
            this.onMouseClicked = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseClicked";
                }
            };
        }
        return this.onMouseClicked;
    }

    public final void setOnMouseDragged(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseDraggedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseDragged() {
        return this.onMouseDragged == null ? null : (EventHandler)this.onMouseDragged.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseDraggedProperty() {
        if (this.onMouseDragged == null) {
            this.onMouseDragged = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.MOUSE_DRAGGED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseDragged";
                }
            };
        }
        return this.onMouseDragged;
    }

    public final void setOnMouseEntered(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseEnteredProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseEntered() {
        return this.onMouseEntered == null ? null : (EventHandler)this.onMouseEntered.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseEnteredProperty() {
        if (this.onMouseEntered == null) {
            this.onMouseEntered = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.MOUSE_ENTERED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseEntered";
                }
            };
        }
        return this.onMouseEntered;
    }

    public final void setOnMouseExited(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseExitedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseExited() {
        return this.onMouseExited == null ? null : (EventHandler)this.onMouseExited.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseExitedProperty() {
        if (this.onMouseExited == null) {
            this.onMouseExited = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.MOUSE_EXITED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseExited";
                }
            };
        }
        return this.onMouseExited;
    }

    public final void setOnMouseMoved(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseMovedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseMoved() {
        return this.onMouseMoved == null ? null : (EventHandler)this.onMouseMoved.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseMovedProperty() {
        if (this.onMouseMoved == null) {
            this.onMouseMoved = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.MOUSE_MOVED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseMoved";
                }
            };
        }
        return this.onMouseMoved;
    }

    public final void setOnMousePressed(EventHandler<? super MouseEvent> eventHandler) {
        this.onMousePressedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMousePressed() {
        return this.onMousePressed == null ? null : (EventHandler)this.onMousePressed.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMousePressedProperty() {
        if (this.onMousePressed == null) {
            this.onMousePressed = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.MOUSE_PRESSED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMousePressed";
                }
            };
        }
        return this.onMousePressed;
    }

    public final void setOnMouseReleased(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnMouseReleased() {
        return this.onMouseReleased == null ? null : (EventHandler)this.onMouseReleased.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onMouseReleasedProperty() {
        if (this.onMouseReleased == null) {
            this.onMouseReleased = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.MOUSE_RELEASED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseReleased";
                }
            };
        }
        return this.onMouseReleased;
    }

    public final void setOnDragDetected(EventHandler<? super MouseEvent> eventHandler) {
        this.onDragDetectedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseEvent> getOnDragDetected() {
        return this.onDragDetected == null ? null : (EventHandler)this.onDragDetected.get();
    }

    public final ObjectProperty<EventHandler<? super MouseEvent>> onDragDetectedProperty() {
        if (this.onDragDetected == null) {
            this.onDragDetected = new ObjectPropertyBase<EventHandler<? super MouseEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseEvent.DRAG_DETECTED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onDragDetected";
                }
            };
        }
        return this.onDragDetected;
    }

    public final void setOnMouseDragOver(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragOverProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragOver() {
        return this.onMouseDragOver == null ? null : (EventHandler)this.onMouseDragOver.get();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragOverProperty() {
        if (this.onMouseDragOver == null) {
            this.onMouseDragOver = new ObjectPropertyBase<EventHandler<? super MouseDragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseDragOver";
                }
            };
        }
        return this.onMouseDragOver;
    }

    public final void setOnMouseDragReleased(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragReleased() {
        return this.onMouseDragReleased == null ? null : (EventHandler)this.onMouseDragReleased.get();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragReleasedProperty() {
        if (this.onMouseDragReleased == null) {
            this.onMouseDragReleased = new ObjectPropertyBase<EventHandler<? super MouseDragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseDragReleased";
                }
            };
        }
        return this.onMouseDragReleased;
    }

    public final void setOnMouseDragEntered(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragEnteredProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragEntered() {
        return this.onMouseDragEntered == null ? null : (EventHandler)this.onMouseDragEntered.get();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragEnteredProperty() {
        if (this.onMouseDragEntered == null) {
            this.onMouseDragEntered = new ObjectPropertyBase<EventHandler<? super MouseDragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseDragEntered";
                }
            };
        }
        return this.onMouseDragEntered;
    }

    public final void setOnMouseDragExited(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragExitedProperty().set(eventHandler);
    }

    public final EventHandler<? super MouseDragEvent> getOnMouseDragExited() {
        return this.onMouseDragExited == null ? null : (EventHandler)this.onMouseDragExited.get();
    }

    public final ObjectProperty<EventHandler<? super MouseDragEvent>> onMouseDragExitedProperty() {
        if (this.onMouseDragExited == null) {
            this.onMouseDragExited = new ObjectPropertyBase<EventHandler<? super MouseDragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onMouseDragExited";
                }
            };
        }
        return this.onMouseDragExited;
    }

    public final void setOnScrollStarted(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollStartedProperty().set(eventHandler);
    }

    public final EventHandler<? super ScrollEvent> getOnScrollStarted() {
        return this.onScrollStarted == null ? null : (EventHandler)this.onScrollStarted.get();
    }

    public final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollStartedProperty() {
        if (this.onScrollStarted == null) {
            this.onScrollStarted = new ObjectPropertyBase<EventHandler<? super ScrollEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(ScrollEvent.SCROLL_STARTED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onScrollStarted";
                }
            };
        }
        return this.onScrollStarted;
    }

    public final void setOnScroll(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollProperty().set(eventHandler);
    }

    public final EventHandler<? super ScrollEvent> getOnScroll() {
        return this.onScroll == null ? null : (EventHandler)this.onScroll.get();
    }

    public final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollProperty() {
        if (this.onScroll == null) {
            this.onScroll = new ObjectPropertyBase<EventHandler<? super ScrollEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(ScrollEvent.SCROLL, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onScroll";
                }
            };
        }
        return this.onScroll;
    }

    public final void setOnScrollFinished(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollFinishedProperty().set(eventHandler);
    }

    public final EventHandler<? super ScrollEvent> getOnScrollFinished() {
        return this.onScrollFinished == null ? null : (EventHandler)this.onScrollFinished.get();
    }

    public final ObjectProperty<EventHandler<? super ScrollEvent>> onScrollFinishedProperty() {
        if (this.onScrollFinished == null) {
            this.onScrollFinished = new ObjectPropertyBase<EventHandler<? super ScrollEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(ScrollEvent.SCROLL_FINISHED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onScrollFinished";
                }
            };
        }
        return this.onScrollFinished;
    }

    public final void setOnRotationStarted(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotationStartedProperty().set(eventHandler);
    }

    public final EventHandler<? super RotateEvent> getOnRotationStarted() {
        return this.onRotationStarted == null ? null : (EventHandler)this.onRotationStarted.get();
    }

    public final ObjectProperty<EventHandler<? super RotateEvent>> onRotationStartedProperty() {
        if (this.onRotationStarted == null) {
            this.onRotationStarted = new ObjectPropertyBase<EventHandler<? super RotateEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(RotateEvent.ROTATION_STARTED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onRotationStarted";
                }
            };
        }
        return this.onRotationStarted;
    }

    public final void setOnRotate(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotateProperty().set(eventHandler);
    }

    public final EventHandler<? super RotateEvent> getOnRotate() {
        return this.onRotate == null ? null : (EventHandler)this.onRotate.get();
    }

    public final ObjectProperty<EventHandler<? super RotateEvent>> onRotateProperty() {
        if (this.onRotate == null) {
            this.onRotate = new ObjectPropertyBase<EventHandler<? super RotateEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(RotateEvent.ROTATE, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onRotate";
                }
            };
        }
        return this.onRotate;
    }

    public final void setOnRotationFinished(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotationFinishedProperty().set(eventHandler);
    }

    public final EventHandler<? super RotateEvent> getOnRotationFinished() {
        return this.onRotationFinished == null ? null : (EventHandler)this.onRotationFinished.get();
    }

    public final ObjectProperty<EventHandler<? super RotateEvent>> onRotationFinishedProperty() {
        if (this.onRotationFinished == null) {
            this.onRotationFinished = new ObjectPropertyBase<EventHandler<? super RotateEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(RotateEvent.ROTATION_FINISHED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onRotationFinished";
                }
            };
        }
        return this.onRotationFinished;
    }

    public final void setOnZoomStarted(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomStartedProperty().set(eventHandler);
    }

    public final EventHandler<? super ZoomEvent> getOnZoomStarted() {
        return this.onZoomStarted == null ? null : (EventHandler)this.onZoomStarted.get();
    }

    public final ObjectProperty<EventHandler<? super ZoomEvent>> onZoomStartedProperty() {
        if (this.onZoomStarted == null) {
            this.onZoomStarted = new ObjectPropertyBase<EventHandler<? super ZoomEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(ZoomEvent.ZOOM_STARTED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onZoomStarted";
                }
            };
        }
        return this.onZoomStarted;
    }

    public final void setOnZoom(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomProperty().set(eventHandler);
    }

    public final EventHandler<? super ZoomEvent> getOnZoom() {
        return this.onZoom == null ? null : (EventHandler)this.onZoom.get();
    }

    public final ObjectProperty<EventHandler<? super ZoomEvent>> onZoomProperty() {
        if (this.onZoom == null) {
            this.onZoom = new ObjectPropertyBase<EventHandler<? super ZoomEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(ZoomEvent.ZOOM, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onZoom";
                }
            };
        }
        return this.onZoom;
    }

    public final void setOnZoomFinished(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomFinishedProperty().set(eventHandler);
    }

    public final EventHandler<? super ZoomEvent> getOnZoomFinished() {
        return this.onZoomFinished == null ? null : (EventHandler)this.onZoomFinished.get();
    }

    public final ObjectProperty<EventHandler<? super ZoomEvent>> onZoomFinishedProperty() {
        if (this.onZoomFinished == null) {
            this.onZoomFinished = new ObjectPropertyBase<EventHandler<? super ZoomEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(ZoomEvent.ZOOM_FINISHED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onZoomFinished";
                }
            };
        }
        return this.onZoomFinished;
    }

    public final void setOnSwipeUp(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeUpProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeUp() {
        return this.onSwipeUp == null ? null : (EventHandler)this.onSwipeUp.get();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeUpProperty() {
        if (this.onSwipeUp == null) {
            this.onSwipeUp = new ObjectPropertyBase<EventHandler<? super SwipeEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(SwipeEvent.SWIPE_UP, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onSwipeUp";
                }
            };
        }
        return this.onSwipeUp;
    }

    public final void setOnSwipeDown(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeDownProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeDown() {
        return this.onSwipeDown == null ? null : (EventHandler)this.onSwipeDown.get();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeDownProperty() {
        if (this.onSwipeDown == null) {
            this.onSwipeDown = new ObjectPropertyBase<EventHandler<? super SwipeEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(SwipeEvent.SWIPE_DOWN, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onSwipeDown";
                }
            };
        }
        return this.onSwipeDown;
    }

    public final void setOnSwipeLeft(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeLeftProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeLeft() {
        return this.onSwipeLeft == null ? null : (EventHandler)this.onSwipeLeft.get();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeLeftProperty() {
        if (this.onSwipeLeft == null) {
            this.onSwipeLeft = new ObjectPropertyBase<EventHandler<? super SwipeEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(SwipeEvent.SWIPE_LEFT, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onSwipeLeft";
                }
            };
        }
        return this.onSwipeLeft;
    }

    public final void setOnSwipeRight(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeRightProperty().set(eventHandler);
    }

    public final EventHandler<? super SwipeEvent> getOnSwipeRight() {
        return this.onSwipeRight == null ? null : (EventHandler)this.onSwipeRight.get();
    }

    public final ObjectProperty<EventHandler<? super SwipeEvent>> onSwipeRightProperty() {
        if (this.onSwipeRight == null) {
            this.onSwipeRight = new ObjectPropertyBase<EventHandler<? super SwipeEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(SwipeEvent.SWIPE_RIGHT, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onSwipeRight";
                }
            };
        }
        return this.onSwipeRight;
    }

    public final void setOnTouchPressed(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchPressedProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchPressed() {
        return this.onTouchPressed == null ? null : (EventHandler)this.onTouchPressed.get();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchPressedProperty() {
        if (this.onTouchPressed == null) {
            this.onTouchPressed = new ObjectPropertyBase<EventHandler<? super TouchEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(TouchEvent.TOUCH_PRESSED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onTouchPressed";
                }
            };
        }
        return this.onTouchPressed;
    }

    public final void setOnTouchMoved(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchMovedProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchMoved() {
        return this.onTouchMoved == null ? null : (EventHandler)this.onTouchMoved.get();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchMovedProperty() {
        if (this.onTouchMoved == null) {
            this.onTouchMoved = new ObjectPropertyBase<EventHandler<? super TouchEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(TouchEvent.TOUCH_MOVED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onTouchMoved";
                }
            };
        }
        return this.onTouchMoved;
    }

    public final void setOnTouchReleased(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchReleased() {
        return this.onTouchReleased == null ? null : (EventHandler)this.onTouchReleased.get();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchReleasedProperty() {
        if (this.onTouchReleased == null) {
            this.onTouchReleased = new ObjectPropertyBase<EventHandler<? super TouchEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(TouchEvent.TOUCH_RELEASED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onTouchReleased";
                }
            };
        }
        return this.onTouchReleased;
    }

    public final void setOnTouchStationary(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchStationaryProperty().set(eventHandler);
    }

    public final EventHandler<? super TouchEvent> getOnTouchStationary() {
        return this.onTouchStationary == null ? null : (EventHandler)this.onTouchStationary.get();
    }

    public final ObjectProperty<EventHandler<? super TouchEvent>> onTouchStationaryProperty() {
        if (this.onTouchStationary == null) {
            this.onTouchStationary = new ObjectPropertyBase<EventHandler<? super TouchEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(TouchEvent.TOUCH_STATIONARY, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onTouchStationary";
                }
            };
        }
        return this.onTouchStationary;
    }

    public final void setOnDragEntered(EventHandler<? super DragEvent> eventHandler) {
        this.onDragEnteredProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragEntered() {
        return this.onDragEntered == null ? null : (EventHandler)this.onDragEntered.get();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragEnteredProperty() {
        if (this.onDragEntered == null) {
            this.onDragEntered = new ObjectPropertyBase<EventHandler<? super DragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(DragEvent.DRAG_ENTERED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onDragEntered";
                }
            };
        }
        return this.onDragEntered;
    }

    public final void setOnDragExited(EventHandler<? super DragEvent> eventHandler) {
        this.onDragExitedProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragExited() {
        return this.onDragExited == null ? null : (EventHandler)this.onDragExited.get();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragExitedProperty() {
        if (this.onDragExited == null) {
            this.onDragExited = new ObjectPropertyBase<EventHandler<? super DragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(DragEvent.DRAG_EXITED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onDragExited";
                }
            };
        }
        return this.onDragExited;
    }

    public final void setOnDragOver(EventHandler<? super DragEvent> eventHandler) {
        this.onDragOverProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragOver() {
        return this.onDragOver == null ? null : (EventHandler)this.onDragOver.get();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragOverProperty() {
        if (this.onDragOver == null) {
            this.onDragOver = new ObjectPropertyBase<EventHandler<? super DragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(DragEvent.DRAG_OVER, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onDragOver";
                }
            };
        }
        return this.onDragOver;
    }

    public final void setOnDragDropped(EventHandler<? super DragEvent> eventHandler) {
        this.onDragDroppedProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragDropped() {
        return this.onDragDropped == null ? null : (EventHandler)this.onDragDropped.get();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragDroppedProperty() {
        if (this.onDragDropped == null) {
            this.onDragDropped = new ObjectPropertyBase<EventHandler<? super DragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(DragEvent.DRAG_DROPPED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onDragDropped";
                }
            };
        }
        return this.onDragDropped;
    }

    public final void setOnDragDone(EventHandler<? super DragEvent> eventHandler) {
        this.onDragDoneProperty().set(eventHandler);
    }

    public final EventHandler<? super DragEvent> getOnDragDone() {
        return this.onDragDone == null ? null : (EventHandler)this.onDragDone.get();
    }

    public final ObjectProperty<EventHandler<? super DragEvent>> onDragDoneProperty() {
        if (this.onDragDone == null) {
            this.onDragDone = new ObjectPropertyBase<EventHandler<? super DragEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(DragEvent.DRAG_DONE, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onDragDone";
                }
            };
        }
        return this.onDragDone;
    }

    public Dragboard startDragAndDrop(TransferMode ... transferModeArray) {
        return this.startDragAndDrop(this, transferModeArray);
    }

    public void startFullDrag() {
        this.startFullDrag(this);
    }

    Dragboard startDragAndDrop(EventTarget eventTarget, TransferMode ... transferModeArray) {
        Toolkit.getToolkit().checkFxUserThread();
        if (this.dndGesture == null || this.dndGesture.dragDetected != DragDetectedState.PROCESSING) {
            throw new IllegalStateException("Cannot start drag and drop outside of DRAG_DETECTED event handler");
        }
        EnumSet<TransferMode> enumSet = EnumSet.noneOf(TransferMode.class);
        for (TransferMode transferMode : InputEventUtils.safeTransferModes(transferModeArray)) {
            enumSet.add(transferMode);
        }
        return this.dndGesture.startDrag(eventTarget, enumSet);
    }

    void startFullDrag(EventTarget eventTarget) {
        Toolkit.getToolkit().checkFxUserThread();
        if (this.dndGesture.dragDetected != DragDetectedState.PROCESSING) {
            throw new IllegalStateException("Cannot start full drag outside of DRAG_DETECTED event handler");
        }
        if (this.dndGesture != null) {
            this.dndGesture.startFullPDR(eventTarget);
            return;
        }
        throw new IllegalStateException("Cannot start full drag when mouse button is not pressed");
    }

    public final void setOnKeyPressed(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyPressedProperty().set(eventHandler);
    }

    public final EventHandler<? super KeyEvent> getOnKeyPressed() {
        return this.onKeyPressed == null ? null : (EventHandler)this.onKeyPressed.get();
    }

    public final ObjectProperty<EventHandler<? super KeyEvent>> onKeyPressedProperty() {
        if (this.onKeyPressed == null) {
            this.onKeyPressed = new ObjectPropertyBase<EventHandler<? super KeyEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(KeyEvent.KEY_PRESSED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onKeyPressed";
                }
            };
        }
        return this.onKeyPressed;
    }

    public final void setOnKeyReleased(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyReleasedProperty().set(eventHandler);
    }

    public final EventHandler<? super KeyEvent> getOnKeyReleased() {
        return this.onKeyReleased == null ? null : (EventHandler)this.onKeyReleased.get();
    }

    public final ObjectProperty<EventHandler<? super KeyEvent>> onKeyReleasedProperty() {
        if (this.onKeyReleased == null) {
            this.onKeyReleased = new ObjectPropertyBase<EventHandler<? super KeyEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(KeyEvent.KEY_RELEASED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onKeyReleased";
                }
            };
        }
        return this.onKeyReleased;
    }

    public final void setOnKeyTyped(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyTypedProperty().set(eventHandler);
    }

    public final EventHandler<? super KeyEvent> getOnKeyTyped() {
        return this.onKeyTyped == null ? null : (EventHandler)this.onKeyTyped.get();
    }

    public final ObjectProperty<EventHandler<? super KeyEvent>> onKeyTypedProperty() {
        if (this.onKeyTyped == null) {
            this.onKeyTyped = new ObjectPropertyBase<EventHandler<? super KeyEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(KeyEvent.KEY_TYPED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onKeyTyped";
                }
            };
        }
        return this.onKeyTyped;
    }

    public final void setOnInputMethodTextChanged(EventHandler<? super InputMethodEvent> eventHandler) {
        this.onInputMethodTextChangedProperty().set(eventHandler);
    }

    public final EventHandler<? super InputMethodEvent> getOnInputMethodTextChanged() {
        return this.onInputMethodTextChanged == null ? null : (EventHandler)this.onInputMethodTextChanged.get();
    }

    public final ObjectProperty<EventHandler<? super InputMethodEvent>> onInputMethodTextChangedProperty() {
        if (this.onInputMethodTextChanged == null) {
            this.onInputMethodTextChanged = new ObjectPropertyBase<EventHandler<? super InputMethodEvent>>(){

                @Override
                protected void invalidated() {
                    Scene.this.setEventHandler(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Scene.this;
                }

                @Override
                public String getName() {
                    return "onInputMethodTextChanged";
                }
            };
        }
        return this.onInputMethodTextChanged;
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

    public final void setNodeOrientation(NodeOrientation nodeOrientation) {
        this.nodeOrientationProperty().set(nodeOrientation);
    }

    public final NodeOrientation getNodeOrientation() {
        return this.nodeOrientation == null ? defaultNodeOrientation : (NodeOrientation)((Object)this.nodeOrientation.get());
    }

    public final ObjectProperty<NodeOrientation> nodeOrientationProperty() {
        if (this.nodeOrientation == null) {
            this.nodeOrientation = new StyleableObjectProperty<NodeOrientation>(defaultNodeOrientation){

                @Override
                protected void invalidated() {
                    Scene.this.sceneEffectiveOrientationInvalidated();
                    Scene.this.getRoot().applyCss();
                }

                @Override
                public Object getBean() {
                    return Scene.this;
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
        if (this.effectiveNodeOrientation == null) {
            this.effectiveNodeOrientation = this.calcEffectiveNodeOrientation();
        }
        return this.effectiveNodeOrientation;
    }

    public final ReadOnlyObjectProperty<NodeOrientation> effectiveNodeOrientationProperty() {
        if (this.effectiveNodeOrientationProperty == null) {
            this.effectiveNodeOrientationProperty = new EffectiveOrientationProperty();
        }
        return this.effectiveNodeOrientationProperty;
    }

    private void parentEffectiveOrientationInvalidated() {
        if (this.getNodeOrientation() == NodeOrientation.INHERIT) {
            this.sceneEffectiveOrientationInvalidated();
        }
    }

    private void sceneEffectiveOrientationInvalidated() {
        this.effectiveNodeOrientation = null;
        if (this.effectiveNodeOrientationProperty != null) {
            this.effectiveNodeOrientationProperty.invalidate();
        }
        this.getRoot().parentResolvedOrientationInvalidated();
    }

    private NodeOrientation calcEffectiveNodeOrientation() {
        NodeOrientation nodeOrientation = this.getNodeOrientation();
        if (nodeOrientation == NodeOrientation.INHERIT) {
            Window window = this.getWindow();
            if (window != null) {
                Scene scene;
                Window window2 = null;
                if (window instanceof Stage) {
                    window2 = ((Stage)window).getOwner();
                } else if (window instanceof PopupWindow) {
                    window2 = ((PopupWindow)window).getOwnerWindow();
                }
                if (window2 != null && (scene = window2.getScene()) != null) {
                    return scene.getEffectiveNodeOrientation();
                }
            }
            return NodeOrientation.LEFT_TO_RIGHT;
        }
        return nodeOrientation;
    }

    Accessible removeAccessible(Node node) {
        if (this.accMap == null) {
            return null;
        }
        return this.accMap.remove(node);
    }

    void addAccessible(Node node, Accessible accessible) {
        if (this.accMap == null) {
            this.accMap = new HashMap<Node, Accessible>();
        }
        this.accMap.put(node, accessible);
    }

    private void disposeAccessibles() {
        if (this.accMap != null) {
            for (Map.Entry<Node, Accessible> entry : this.accMap.entrySet()) {
                Node node = entry.getKey();
                Accessible accessible = entry.getValue();
                if (node.accessible != null) {
                    if (node.accessible == accessible) {
                        System.err.println("[A11y] 'node.accessible == acc' should never happen.");
                    }
                    if (node.getScene() == this) {
                        System.err.println("[A11y] 'node.getScene() == this' should never happen.");
                    }
                    accessible.dispose();
                    continue;
                }
                if (node.getScene() == this) {
                    node.accessible = accessible;
                    continue;
                }
                accessible.dispose();
            }
            this.accMap.clear();
        }
    }

    Accessible getAccessible() {
        if (this.peer == null) {
            return null;
        }
        if (this.accessible == null) {
            this.accessible = Application.GetApplication().createAccessible();
            this.accessible.setEventHandler(new Accessible.EventHandler(){

                @Override
                public AccessControlContext getAccessControlContext() {
                    return Scene.this.getPeer().getAccessControlContext();
                }

                @Override
                public Object getAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
                    switch (accessibleAttribute) {
                        case CHILDREN: {
                            Parent parent = Scene.this.getRoot();
                            if (parent == null) break;
                            return FXCollections.observableArrayList(parent);
                        }
                        case TEXT: {
                            Window window = Scene.this.getWindow();
                            if (!(window instanceof Stage)) break;
                            return ((Stage)window).getTitle();
                        }
                        case NODE_AT_POINT: {
                            Node node;
                            Window window = Scene.this.getWindow();
                            Point2D point2D = (Point2D)objectArray[0];
                            PickResult pickResult = Scene.this.pick(point2D.getX() - Scene.this.getX() - window.getX(), point2D.getY() - Scene.this.getY() - window.getY());
                            if (pickResult != null && (node = pickResult.getIntersectedNode()) != null) {
                                return node;
                            }
                            return Scene.this.getRoot();
                        }
                        case ROLE: {
                            return AccessibleRole.PARENT;
                        }
                        case SCENE: {
                            return Scene.this;
                        }
                        case FOCUS_NODE: {
                            if (Scene.this.transientFocusContainer != null) {
                                return Scene.this.transientFocusContainer.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE, new Object[0]);
                            }
                            return Scene.this.getFocusOwner();
                        }
                    }
                    return super.getAttribute(accessibleAttribute, objectArray);
                }
            });
            PlatformImpl.accessibilityActiveProperty().set(true);
        }
        return this.accessible;
    }

    static /* synthetic */ Node[] access$2302(Scene scene, Node[] nodeArray) {
        scene.dirtyNodes = nodeArray;
        return nodeArray;
    }

    static /* synthetic */ TouchPoint[] access$5502(Scene scene, TouchPoint[] touchPointArray) {
        scene.touchPoints = touchPointArray;
        return touchPointArray;
    }

    static {
        PerformanceTracker.setSceneAccessor(new PerformanceTracker.SceneAccessor(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void setPerfTracker(Scene scene, PerformanceTracker performanceTracker) {
                Object object = trackerMonitor;
                synchronized (object) {
                    scene.tracker = performanceTracker;
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public PerformanceTracker getPerfTracker(Scene scene) {
                Object object = trackerMonitor;
                synchronized (object) {
                    return scene.tracker;
                }
            }
        });
        SceneHelper.setSceneAccessor(new SceneHelper.SceneAccessor(){

            @Override
            public void enableInputMethodEvents(Scene scene, boolean bl) {
                scene.enableInputMethodEvents(bl);
            }

            @Override
            public void processKeyEvent(Scene scene, KeyEvent keyEvent) {
                scene.processKeyEvent(keyEvent);
            }

            @Override
            public void processMouseEvent(Scene scene, MouseEvent mouseEvent) {
                scene.processMouseEvent(mouseEvent);
            }

            @Override
            public void preferredSize(Scene scene) {
                scene.preferredSize();
            }

            @Override
            public void disposePeer(Scene scene) {
                scene.disposePeer();
            }

            @Override
            public void initPeer(Scene scene) {
                scene.initPeer();
            }

            @Override
            public void setWindow(Scene scene, Window window) {
                scene.setWindow(window);
            }

            @Override
            public TKScene getPeer(Scene scene) {
                return scene.getPeer();
            }

            @Override
            public void setAllowPGAccess(boolean bl) {
                Scene.setAllowPGAccess(bl);
            }

            @Override
            public void parentEffectiveOrientationInvalidated(Scene scene) {
                scene.parentEffectiveOrientationInvalidated();
            }

            @Override
            public Camera getEffectiveCamera(Scene scene) {
                return scene.getEffectiveCamera();
            }

            @Override
            public Scene createPopupScene(Parent parent) {
                return new Scene(parent){

                    @Override
                    void doLayoutPass() {
                        this.resizeRootToPreferredSize(this.getRoot());
                        super.doLayoutPass();
                    }

                    @Override
                    void resizeRootOnSceneSizeChange(double d, double d2) {
                    }
                };
            }

            @Override
            public void setTransientFocusContainer(Scene scene, Node node) {
                if (scene != null) {
                    scene.transientFocusContainer = node;
                }
            }

            @Override
            public Accessible getAccessible(Scene scene) {
                return scene.getAccessible();
            }
        });
        inSynchronizer = false;
        inMousePick = false;
        allowPGAccess = false;
        pgAccessCount = 0;
        snapshotPulseListener = null;
        trackerMonitor = new Object();
        USER_DATA_KEY = new Object();
        defaultNodeOrientation = AccessController.doPrivileged(() -> Boolean.getBoolean("javafx.scene.nodeOrientation.RTL")) != false ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.INHERIT;
    }

    private final class EffectiveOrientationProperty
    extends ReadOnlyObjectPropertyBase<NodeOrientation> {
        private EffectiveOrientationProperty() {
        }

        @Override
        public NodeOrientation get() {
            return Scene.this.getEffectiveNodeOrientation();
        }

        @Override
        public Object getBean() {
            return Scene.this;
        }

        @Override
        public String getName() {
            return "effectiveNodeOrientation";
        }

        public void invalidate() {
            this.fireValueChangedEvent();
        }
    }

    private static class TargetWrapper {
        private Scene scene;
        private Node node;
        private PickResult result;

        private TargetWrapper() {
        }

        public void fillHierarchy(List<EventTarget> list) {
            list.clear();
            Node node = this.node;
            while (node != null) {
                list.add(node);
                Parent parent = node.getParent();
                node = parent != null ? parent : node.getSubScene();
            }
            if (this.scene != null) {
                list.add(this.scene);
            }
        }

        public EventTarget getEventTarget() {
            return this.node != null ? this.node : this.scene;
        }

        public Cursor getCursor() {
            Cursor cursor = null;
            if (this.node != null) {
                cursor = this.node.getCursor();
                Parent parent = this.node.getParent();
                while (cursor == null && parent != null) {
                    cursor = parent.getCursor();
                    Parent parent2 = parent.getParent();
                    parent = parent2 != null ? parent2 : parent.getSubScene();
                }
            }
            return cursor;
        }

        public void clear() {
            this.set(null, null);
            this.result = null;
        }

        public void setNodeResult(PickResult pickResult) {
            if (pickResult != null) {
                this.result = pickResult;
                Node node = pickResult.getIntersectedNode();
                this.set(node, node.getScene());
            }
        }

        public void setSceneResult(PickResult pickResult, Scene scene) {
            if (pickResult != null) {
                this.result = pickResult;
                this.set(null, scene);
            }
        }

        public PickResult getResult() {
            return this.result;
        }

        public void copy(TargetWrapper targetWrapper) {
            this.node = targetWrapper.node;
            this.scene = targetWrapper.scene;
            this.result = targetWrapper.result;
        }

        private void set(Node node, Scene scene) {
            this.node = node;
            this.scene = scene;
        }
    }

    private static class TouchMap {
        private static final int FAST_THRESHOLD = 10;
        int[] fastMap = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Map<Long, Integer> slowMap = new HashMap<Long, Integer>();
        List<Integer> order = new LinkedList<Integer>();
        List<Long> removed = new ArrayList<Long>(10);
        int counter = 0;
        int active = 0;

        private TouchMap() {
        }

        public int add(long l) {
            ++this.counter;
            ++this.active;
            if (l < 10L) {
                this.fastMap[(int)l] = this.counter;
            } else {
                this.slowMap.put(l, this.counter);
            }
            this.order.add(this.counter);
            return this.counter;
        }

        public void remove(long l) {
            this.removed.add(l);
        }

        public int get(long l) {
            if (l < 10L) {
                int n = this.fastMap[(int)l];
                if (n == 0) {
                    throw new RuntimeException("Platform reported wrong touch point ID");
                }
                return n;
            }
            try {
                return this.slowMap.get(l);
            }
            catch (NullPointerException nullPointerException) {
                throw new RuntimeException("Platform reported wrong touch point ID");
            }
        }

        public int getOrder(int n) {
            return this.order.indexOf(n);
        }

        public boolean cleanup() {
            for (long l : this.removed) {
                --this.active;
                this.order.remove((Object)this.get(l));
                if (l < 10L) {
                    this.fastMap[(int)l] = 0;
                } else {
                    this.slowMap.remove(l);
                }
                if (this.active != 0) continue;
                this.counter = 0;
            }
            this.removed.clear();
            return this.active == 0;
        }
    }

    class InputMethodRequestsDelegate
    implements ExtendedInputMethodRequests {
        InputMethodRequestsDelegate() {
        }

        @Override
        public Point2D getTextLocation(int n) {
            InputMethodRequests inputMethodRequests = this.getClientRequests();
            if (inputMethodRequests != null) {
                return inputMethodRequests.getTextLocation(n);
            }
            return new Point2D(0.0, 0.0);
        }

        @Override
        public int getLocationOffset(int n, int n2) {
            InputMethodRequests inputMethodRequests = this.getClientRequests();
            if (inputMethodRequests != null) {
                return inputMethodRequests.getLocationOffset(n, n2);
            }
            return 0;
        }

        @Override
        public void cancelLatestCommittedText() {
            InputMethodRequests inputMethodRequests = this.getClientRequests();
            if (inputMethodRequests != null) {
                inputMethodRequests.cancelLatestCommittedText();
            }
        }

        @Override
        public String getSelectedText() {
            InputMethodRequests inputMethodRequests = this.getClientRequests();
            if (inputMethodRequests != null) {
                return inputMethodRequests.getSelectedText();
            }
            return null;
        }

        @Override
        public int getInsertPositionOffset() {
            InputMethodRequests inputMethodRequests = this.getClientRequests();
            if (inputMethodRequests != null && inputMethodRequests instanceof ExtendedInputMethodRequests) {
                return ((ExtendedInputMethodRequests)inputMethodRequests).getInsertPositionOffset();
            }
            return 0;
        }

        @Override
        public String getCommittedText(int n, int n2) {
            InputMethodRequests inputMethodRequests = this.getClientRequests();
            if (inputMethodRequests != null && inputMethodRequests instanceof ExtendedInputMethodRequests) {
                return ((ExtendedInputMethodRequests)inputMethodRequests).getCommittedText(n, n2);
            }
            return null;
        }

        @Override
        public int getCommittedTextLength() {
            InputMethodRequests inputMethodRequests = this.getClientRequests();
            if (inputMethodRequests != null && inputMethodRequests instanceof ExtendedInputMethodRequests) {
                return ((ExtendedInputMethodRequests)inputMethodRequests).getCommittedTextLength();
            }
            return 0;
        }

        private InputMethodRequests getClientRequests() {
            Node node = Scene.this.getFocusOwner();
            if (node != null) {
                return node.getInputMethodRequests();
            }
            return null;
        }
    }

    class KeyHandler {
        private boolean windowFocused;
        private final InvalidationListener sceneWindowFocusedListener = observable -> this.setWindowFocused(((ReadOnlyBooleanProperty)observable).get());

        KeyHandler() {
        }

        private void setFocusOwner(Node node) {
            TKScene tKScene;
            Scene scene;
            if (Scene.this.oldFocusOwner != null && (scene = Scene.this.oldFocusOwner.getScene()) != null && (tKScene = scene.getPeer()) != null) {
                tKScene.finishInputMethodComposition();
            }
            Scene.this.focusOwner.set(node);
        }

        protected boolean isWindowFocused() {
            return this.windowFocused;
        }

        protected void setWindowFocused(boolean bl) {
            this.windowFocused = bl;
            if (Scene.this.getFocusOwner() != null) {
                Scene.this.getFocusOwner().setFocused(this.windowFocused);
            }
            if (this.windowFocused && Scene.this.accessible != null) {
                Scene.this.accessible.sendNotification(AccessibleAttribute.FOCUS_NODE);
            }
        }

        private void windowForSceneChanged(Window window, Window window2) {
            if (window != null) {
                window.focusedProperty().removeListener(this.sceneWindowFocusedListener);
            }
            if (window2 != null) {
                window2.focusedProperty().addListener(this.sceneWindowFocusedListener);
                this.setWindowFocused(window2.isFocused());
            } else {
                this.setWindowFocused(false);
            }
        }

        private void process(KeyEvent keyEvent) {
            Node node = Scene.this.getFocusOwner();
            Scene scene = node != null && node.getScene() == Scene.this ? node : Scene.this;
            Event.fireEvent(scene, keyEvent);
        }

        private void requestFocus(Node node) {
            if (Scene.this.getFocusOwner() == node || node != null && !node.isCanReceiveFocus()) {
                return;
            }
            this.setFocusOwner(node);
        }
    }

    class MouseHandler {
        private TargetWrapper pdrEventTarget = new TargetWrapper();
        private boolean pdrInProgress = false;
        private boolean fullPDREntered = false;
        private EventTarget currentEventTarget = null;
        private MouseEvent lastEvent;
        private boolean hover = false;
        private boolean primaryButtonDown = false;
        private boolean secondaryButtonDown = false;
        private boolean middleButtonDown = false;
        private EventTarget fullPDRSource = null;
        private TargetWrapper fullPDRTmpTargetWrapper = new TargetWrapper();
        private final List<EventTarget> pdrEventTargets = new ArrayList<EventTarget>();
        private final List<EventTarget> currentEventTargets = new ArrayList<EventTarget>();
        private final List<EventTarget> newEventTargets = new ArrayList<EventTarget>();
        private final List<EventTarget> fullPDRCurrentEventTargets = new ArrayList<EventTarget>();
        private final List<EventTarget> fullPDRNewEventTargets = new ArrayList<EventTarget>();
        private EventTarget fullPDRCurrentTarget = null;
        private Cursor currCursor;
        private CursorFrame currCursorFrame;
        private EventQueue queue = new EventQueue();
        private Runnable pickProcess = new Runnable(){

            @Override
            public void run() {
                if (Scene.this.peer != null && MouseHandler.this.lastEvent != null) {
                    MouseHandler.this.process(MouseHandler.this.lastEvent, true);
                }
            }
        };

        MouseHandler() {
        }

        private void pulse() {
            if (this.hover && this.lastEvent != null) {
                Platform.runLater(this.pickProcess);
            }
        }

        private void clearPDREventTargets() {
            this.pdrInProgress = false;
            this.currentEventTarget = this.currentEventTargets.size() > 0 ? this.currentEventTargets.get(0) : null;
            this.pdrEventTarget.clear();
        }

        public void enterFullPDR(EventTarget eventTarget) {
            this.fullPDREntered = true;
            this.fullPDRSource = eventTarget;
            this.fullPDRCurrentTarget = null;
            this.fullPDRCurrentEventTargets.clear();
        }

        public void exitFullPDR(MouseEvent mouseEvent) {
            if (!this.fullPDREntered) {
                return;
            }
            this.fullPDREntered = false;
            for (int i = this.fullPDRCurrentEventTargets.size() - 1; i >= 0; --i) {
                EventTarget eventTarget = this.fullPDRCurrentEventTargets.get(i);
                Event.fireEvent(eventTarget, MouseEvent.copyForMouseDragEvent(mouseEvent, eventTarget, eventTarget, MouseDragEvent.MOUSE_DRAG_EXITED_TARGET, this.fullPDRSource, mouseEvent.getPickResult()));
            }
            this.fullPDRSource = null;
            this.fullPDRCurrentEventTargets.clear();
            this.fullPDRCurrentTarget = null;
        }

        private void handleNodeRemoval(Node node) {
            EventTarget eventTarget;
            int n;
            if (this.lastEvent == null) {
                return;
            }
            if (this.currentEventTargets.contains(node)) {
                n = 0;
                eventTarget = null;
                while (eventTarget != node) {
                    eventTarget = this.currentEventTargets.get(n++);
                    this.queue.postEvent(this.lastEvent.copyFor(eventTarget, eventTarget, MouseEvent.MOUSE_EXITED_TARGET));
                }
                this.currentEventTargets.subList(0, n).clear();
            }
            if (this.fullPDREntered && this.fullPDRCurrentEventTargets.contains(node)) {
                n = 0;
                eventTarget = null;
                while (eventTarget != node) {
                    eventTarget = this.fullPDRCurrentEventTargets.get(n++);
                    this.queue.postEvent(MouseEvent.copyForMouseDragEvent(this.lastEvent, eventTarget, eventTarget, MouseDragEvent.MOUSE_DRAG_EXITED_TARGET, this.fullPDRSource, this.lastEvent.getPickResult()));
                }
                this.fullPDRCurrentEventTargets.subList(0, n).clear();
            }
            this.queue.fire();
            if (this.pdrInProgress && this.pdrEventTargets.contains(node)) {
                n = 0;
                eventTarget = null;
                while (eventTarget != node) {
                    eventTarget = this.pdrEventTargets.get(n++);
                    ((Node)eventTarget).setPressed(false);
                }
                this.pdrEventTargets.subList(0, n).clear();
                eventTarget = this.pdrEventTargets.get(0);
                PickResult pickResult = this.pdrEventTarget.getResult();
                if (eventTarget instanceof Node) {
                    this.pdrEventTarget.setNodeResult(new PickResult((Node)eventTarget, pickResult.getIntersectedPoint(), pickResult.getIntersectedDistance()));
                } else {
                    this.pdrEventTarget.setSceneResult(new PickResult(null, pickResult.getIntersectedPoint(), pickResult.getIntersectedDistance()), (Scene)eventTarget);
                }
            }
        }

        private void handleEnterExit(MouseEvent mouseEvent, TargetWrapper targetWrapper) {
            if (targetWrapper.getEventTarget() != this.currentEventTarget || mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED) {
                EventTarget eventTarget;
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED) {
                    this.newEventTargets.clear();
                } else {
                    targetWrapper.fillHierarchy(this.newEventTargets);
                }
                int n = this.newEventTargets.size();
                int n2 = this.currentEventTargets.size() - 1;
                int n3 = n - 1;
                int n4 = this.pdrEventTargets.size() - 1;
                while (n2 >= 0 && n3 >= 0 && this.currentEventTargets.get(n2) == this.newEventTargets.get(n3)) {
                    --n2;
                    --n3;
                    --n4;
                }
                int n5 = n4;
                while (n2 >= 0) {
                    eventTarget = this.currentEventTargets.get(n2);
                    if (this.pdrInProgress && (n4 < 0 || eventTarget != this.pdrEventTargets.get(n4))) break;
                    this.queue.postEvent(mouseEvent.copyFor(eventTarget, eventTarget, MouseEvent.MOUSE_EXITED_TARGET));
                    --n2;
                    --n4;
                }
                n4 = n5;
                while (n3 >= 0) {
                    eventTarget = this.newEventTargets.get(n3);
                    if (this.pdrInProgress && (n4 < 0 || eventTarget != this.pdrEventTargets.get(n4))) break;
                    this.queue.postEvent(mouseEvent.copyFor(eventTarget, eventTarget, MouseEvent.MOUSE_ENTERED_TARGET));
                    --n3;
                    --n4;
                }
                this.currentEventTarget = targetWrapper.getEventTarget();
                this.currentEventTargets.clear();
                ++n3;
                while (n3 < n) {
                    this.currentEventTargets.add(this.newEventTargets.get(n3));
                    ++n3;
                }
            }
            this.queue.fire();
        }

        private void process(MouseEvent mouseEvent, boolean bl) {
            Toolkit.getToolkit().checkFxUserThread();
            inMousePick = true;
            Scene.this.cursorScreenPos = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY());
            Scene.this.cursorScenePos = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            boolean bl2 = false;
            if (!bl) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    if (!(this.primaryButtonDown || this.secondaryButtonDown || this.middleButtonDown)) {
                        bl2 = true;
                        Scene.this.dndGesture = new DnDGesture();
                        this.clearPDREventTargets();
                    }
                } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED) {
                    this.clearPDREventTargets();
                } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    this.hover = true;
                } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED) {
                    this.hover = false;
                }
                this.primaryButtonDown = mouseEvent.isPrimaryButtonDown();
                this.secondaryButtonDown = mouseEvent.isSecondaryButtonDown();
                this.middleButtonDown = mouseEvent.isMiddleButtonDown();
            }
            Scene.this.pick(Scene.this.tmpTargetWrapper, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            PickResult pickResult = Scene.this.tmpTargetWrapper.getResult();
            if (pickResult != null) {
                mouseEvent = new MouseEvent(mouseEvent.getEventType(), mouseEvent.getSceneX(), mouseEvent.getSceneY(), mouseEvent.getScreenX(), mouseEvent.getScreenY(), mouseEvent.getButton(), mouseEvent.getClickCount(), mouseEvent.isShiftDown(), mouseEvent.isControlDown(), mouseEvent.isAltDown(), mouseEvent.isMetaDown(), mouseEvent.isPrimaryButtonDown(), mouseEvent.isMiddleButtonDown(), mouseEvent.isSecondaryButtonDown(), mouseEvent.isSynthesized(), mouseEvent.isPopupTrigger(), mouseEvent.isStillSincePress(), pickResult);
            }
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED) {
                Scene.this.tmpTargetWrapper.clear();
            }
            TargetWrapper targetWrapper = this.pdrInProgress ? this.pdrEventTarget : Scene.this.tmpTargetWrapper;
            if (bl2) {
                this.pdrEventTarget.copy(targetWrapper);
                this.pdrEventTarget.fillHierarchy(this.pdrEventTargets);
            }
            if (!bl) {
                mouseEvent = Scene.this.clickGenerator.preProcess(mouseEvent);
            }
            this.handleEnterExit(mouseEvent, Scene.this.tmpTargetWrapper);
            if (Scene.this.dndGesture != null) {
                Scene.this.dndGesture.processDragDetection(mouseEvent);
            }
            if (this.fullPDREntered && mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                this.processFullPDR(mouseEvent, bl);
            }
            if (targetWrapper.getEventTarget() != null && mouseEvent.getEventType() != MouseEvent.MOUSE_ENTERED && mouseEvent.getEventType() != MouseEvent.MOUSE_EXITED && !bl) {
                Event.fireEvent(targetWrapper.getEventTarget(), mouseEvent);
            }
            if (this.fullPDREntered && mouseEvent.getEventType() != MouseEvent.MOUSE_RELEASED) {
                this.processFullPDR(mouseEvent, bl);
            }
            if (!bl) {
                Scene.this.clickGenerator.postProcess(mouseEvent, targetWrapper, Scene.this.tmpTargetWrapper);
            }
            if (!bl && Scene.this.dndGesture != null && !Scene.this.dndGesture.process(mouseEvent, targetWrapper.getEventTarget())) {
                Scene.this.dndGesture = null;
            }
            Cursor cursor = targetWrapper.getCursor();
            if (mouseEvent.getEventType() != MouseEvent.MOUSE_EXITED) {
                if (cursor == null && this.hover) {
                    cursor = Scene.this.getCursor();
                }
                this.updateCursor(cursor);
                this.updateCursorFrame();
            }
            if (bl2) {
                this.pdrInProgress = true;
            }
            if (this.pdrInProgress && !this.primaryButtonDown && !this.secondaryButtonDown && !this.middleButtonDown) {
                this.clearPDREventTargets();
                this.exitFullPDR(mouseEvent);
                Scene.this.pick(Scene.this.tmpTargetWrapper, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                this.handleEnterExit(mouseEvent, Scene.this.tmpTargetWrapper);
            }
            this.lastEvent = mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED ? null : mouseEvent;
            inMousePick = false;
        }

        private void processFullPDR(MouseEvent mouseEvent, boolean bl) {
            Scene.this.pick(this.fullPDRTmpTargetWrapper, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            PickResult pickResult = this.fullPDRTmpTargetWrapper.getResult();
            EventTarget eventTarget = this.fullPDRTmpTargetWrapper.getEventTarget();
            if (eventTarget != this.fullPDRCurrentTarget) {
                EventTarget eventTarget2;
                int n;
                this.fullPDRTmpTargetWrapper.fillHierarchy(this.fullPDRNewEventTargets);
                int n2 = this.fullPDRNewEventTargets.size();
                int n3 = this.fullPDRCurrentEventTargets.size() - 1;
                for (n = n2 - 1; n3 >= 0 && n >= 0 && this.fullPDRCurrentEventTargets.get(n3) == this.fullPDRNewEventTargets.get(n); --n3, --n) {
                }
                while (n3 >= 0) {
                    eventTarget2 = this.fullPDRCurrentEventTargets.get(n3);
                    Event.fireEvent(eventTarget2, MouseEvent.copyForMouseDragEvent(mouseEvent, eventTarget2, eventTarget2, MouseDragEvent.MOUSE_DRAG_EXITED_TARGET, this.fullPDRSource, pickResult));
                    --n3;
                }
                while (n >= 0) {
                    eventTarget2 = this.fullPDRNewEventTargets.get(n);
                    Event.fireEvent(eventTarget2, MouseEvent.copyForMouseDragEvent(mouseEvent, eventTarget2, eventTarget2, MouseDragEvent.MOUSE_DRAG_ENTERED_TARGET, this.fullPDRSource, pickResult));
                    --n;
                }
                this.fullPDRCurrentTarget = eventTarget;
                this.fullPDRCurrentEventTargets.clear();
                this.fullPDRCurrentEventTargets.addAll(this.fullPDRNewEventTargets);
                this.fullPDRNewEventTargets.clear();
            }
            if (eventTarget != null && !bl) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    Event.fireEvent(eventTarget, MouseEvent.copyForMouseDragEvent(mouseEvent, eventTarget, eventTarget, MouseDragEvent.MOUSE_DRAG_OVER, this.fullPDRSource, pickResult));
                }
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    Event.fireEvent(eventTarget, MouseEvent.copyForMouseDragEvent(mouseEvent, eventTarget, eventTarget, MouseDragEvent.MOUSE_DRAG_RELEASED, this.fullPDRSource, pickResult));
                }
            }
        }

        private void updateCursor(Cursor cursor) {
            if (this.currCursor != cursor) {
                if (this.currCursor != null) {
                    this.currCursor.deactivate();
                }
                if (cursor != null) {
                    cursor.activate();
                }
                this.currCursor = cursor;
            }
        }

        public void updateCursorFrame() {
            CursorFrame cursorFrame;
            CursorFrame cursorFrame2 = cursorFrame = this.currCursor != null ? this.currCursor.getCurrentFrame() : Cursor.DEFAULT.getCurrentFrame();
            if (this.currCursorFrame != cursorFrame) {
                if (Scene.this.peer != null) {
                    Scene.this.peer.setCursor(cursorFrame);
                }
                this.currCursorFrame = cursorFrame;
            }
        }

        private PickResult pickNode(PickRay pickRay) {
            PickResultChooser pickResultChooser = new PickResultChooser();
            Scene.this.getRoot().pickNode(pickRay, pickResultChooser);
            return pickResultChooser.toPickResult();
        }
    }

    static class ClickGenerator {
        private ClickCounter lastPress = null;
        private Map<MouseButton, ClickCounter> counters = new EnumMap<MouseButton, ClickCounter>(MouseButton.class);
        private List<EventTarget> pressedTargets = new ArrayList<EventTarget>();
        private List<EventTarget> releasedTargets = new ArrayList<EventTarget>();

        public ClickGenerator() {
            for (MouseButton mouseButton : MouseButton.values()) {
                if (mouseButton == MouseButton.NONE) continue;
                this.counters.put(mouseButton, new ClickCounter());
            }
        }

        private MouseEvent preProcess(MouseEvent mouseEvent) {
            boolean bl;
            for (ClickCounter clickCounter : this.counters.values()) {
                clickCounter.moved(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            }
            ClickCounter clickCounter = this.counters.get((Object)mouseEvent.getButton());
            boolean bl2 = bl = this.lastPress != null ? this.lastPress.isStill() : false;
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    this.counters.get((Object)MouseButton.PRIMARY).clear();
                }
                if (!mouseEvent.isSecondaryButtonDown()) {
                    this.counters.get((Object)MouseButton.SECONDARY).clear();
                }
                if (!mouseEvent.isMiddleButtonDown()) {
                    this.counters.get((Object)MouseButton.MIDDLE).clear();
                }
                clickCounter.applyOut();
                clickCounter.inc();
                clickCounter.start(mouseEvent.getSceneX(), mouseEvent.getSceneY());
                this.lastPress = clickCounter;
            }
            return new MouseEvent(mouseEvent.getEventType(), mouseEvent.getSceneX(), mouseEvent.getSceneY(), mouseEvent.getScreenX(), mouseEvent.getScreenY(), mouseEvent.getButton(), clickCounter != null && mouseEvent.getEventType() != MouseEvent.MOUSE_MOVED ? clickCounter.get() : 0, mouseEvent.isShiftDown(), mouseEvent.isControlDown(), mouseEvent.isAltDown(), mouseEvent.isMetaDown(), mouseEvent.isPrimaryButtonDown(), mouseEvent.isMiddleButtonDown(), mouseEvent.isSecondaryButtonDown(), mouseEvent.isSynthesized(), mouseEvent.isPopupTrigger(), bl, mouseEvent.getPickResult());
        }

        private void postProcess(MouseEvent mouseEvent, TargetWrapper targetWrapper, TargetWrapper targetWrapper2) {
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                ClickCounter clickCounter = this.counters.get((Object)mouseEvent.getButton());
                targetWrapper.fillHierarchy(this.pressedTargets);
                targetWrapper2.fillHierarchy(this.releasedTargets);
                int n = this.pressedTargets.size() - 1;
                EventTarget eventTarget = null;
                for (int i = this.releasedTargets.size() - 1; n >= 0 && i >= 0 && this.pressedTargets.get(n) == this.releasedTargets.get(i); --n, --i) {
                    eventTarget = this.pressedTargets.get(n);
                }
                this.pressedTargets.clear();
                this.releasedTargets.clear();
                if (eventTarget != null && this.lastPress != null) {
                    MouseEvent mouseEvent2 = new MouseEvent(null, eventTarget, MouseEvent.MOUSE_CLICKED, mouseEvent.getSceneX(), mouseEvent.getSceneY(), mouseEvent.getScreenX(), mouseEvent.getScreenY(), mouseEvent.getButton(), clickCounter.get(), mouseEvent.isShiftDown(), mouseEvent.isControlDown(), mouseEvent.isAltDown(), mouseEvent.isMetaDown(), mouseEvent.isPrimaryButtonDown(), mouseEvent.isMiddleButtonDown(), mouseEvent.isSecondaryButtonDown(), mouseEvent.isSynthesized(), mouseEvent.isPopupTrigger(), this.lastPress.isStill(), mouseEvent.getPickResult());
                    Event.fireEvent(eventTarget, mouseEvent2);
                }
            }
        }
    }

    static class ClickCounter {
        Toolkit toolkit = Toolkit.getToolkit();
        private int count;
        private boolean out;
        private boolean still;
        private Timeline timeout;
        private double pressedX;
        private double pressedY;

        ClickCounter() {
        }

        private void inc() {
            ++this.count;
        }

        private int get() {
            return this.count;
        }

        private boolean isStill() {
            return this.still;
        }

        private void clear() {
            this.count = 0;
            this.stopTimeout();
        }

        private void out() {
            this.out = true;
            this.stopTimeout();
        }

        private void applyOut() {
            if (this.out) {
                this.clear();
            }
            this.out = false;
        }

        private void moved(double d, double d2) {
            if (Math.abs(d - this.pressedX) > (double)this.toolkit.getMultiClickMaxX() || Math.abs(d2 - this.pressedY) > (double)this.toolkit.getMultiClickMaxY()) {
                this.out();
                this.still = false;
            }
        }

        private void start(double d, double d2) {
            this.pressedX = d;
            this.pressedY = d2;
            this.out = false;
            if (this.timeout != null) {
                this.timeout.stop();
            }
            this.timeout = new Timeline();
            this.timeout.getKeyFrames().add(new KeyFrame(new Duration(this.toolkit.getMultiClickTime()), actionEvent -> {
                this.out = true;
                this.timeout = null;
            }, new KeyValue[0]));
            this.timeout.play();
            this.still = true;
        }

        private void stopTimeout() {
            if (this.timeout != null) {
                this.timeout.stop();
                this.timeout = null;
            }
        }
    }

    class DragSourceListener
    implements TKDragSourceListener {
        DragSourceListener() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void dragDropEnd(double d, double d2, double d3, double d4, TransferMode transferMode) {
            if (Scene.this.dndGesture != null) {
                if (Scene.this.dndGesture.dragboard == null) {
                    throw new RuntimeException("dndGesture.dragboard is null in dragDropEnd");
                }
                DragEvent dragEvent = new DragEvent(DragEvent.ANY, Scene.this.dndGesture.dragboard, d, d2, d3, d4, transferMode, null, null, null);
                DragboardHelper.setDataAccessRestriction(Scene.this.dndGesture.dragboard, false);
                try {
                    Scene.this.dndGesture.processDropEnd(dragEvent);
                }
                finally {
                    DragboardHelper.setDataAccessRestriction(Scene.this.dndGesture.dragboard, true);
                }
                Scene.this.dndGesture = null;
            }
        }
    }

    private static enum DragDetectedState {
        NOT_YET,
        PROCESSING,
        DONE;

    }

    class DnDGesture {
        private final double hysteresisSizeX = Toolkit.getToolkit().getMultiClickMaxX();
        private final double hysteresisSizeY = Toolkit.getToolkit().getMultiClickMaxY();
        private EventTarget source = null;
        private Set<TransferMode> sourceTransferModes = null;
        private TransferMode acceptedTransferMode = null;
        private Dragboard dragboard = null;
        private EventTarget potentialTarget = null;
        private EventTarget target = null;
        private DragDetectedState dragDetected = DragDetectedState.NOT_YET;
        private double pressedX;
        private double pressedY;
        private List<EventTarget> currentTargets = new ArrayList<EventTarget>();
        private List<EventTarget> newTargets = new ArrayList<EventTarget>();
        private EventTarget fullPDRSource = null;

        DnDGesture() {
        }

        private void fireEvent(EventTarget eventTarget, Event event) {
            if (eventTarget != null) {
                Event.fireEvent(eventTarget, event);
            }
        }

        private void processingDragDetected() {
            this.dragDetected = DragDetectedState.PROCESSING;
        }

        private void dragDetectedProcessed() {
            boolean bl;
            this.dragDetected = DragDetectedState.DONE;
            boolean bl2 = bl = this.dragboard != null && ClipboardHelper.contentPut(this.dragboard);
            if (bl) {
                Toolkit.getToolkit().startDrag(Scene.this.peer, this.sourceTransferModes, new DragSourceListener(), this.dragboard);
            } else if (this.fullPDRSource != null) {
                Scene.this.mouseHandler.enterFullPDR(this.fullPDRSource);
            }
            this.fullPDRSource = null;
        }

        private void processDragDetection(MouseEvent mouseEvent) {
            if (this.dragDetected != DragDetectedState.NOT_YET) {
                mouseEvent.setDragDetect(false);
                return;
            }
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                this.pressedX = mouseEvent.getSceneX();
                this.pressedY = mouseEvent.getSceneY();
                mouseEvent.setDragDetect(false);
            } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                double d = Math.abs(mouseEvent.getSceneX() - this.pressedX);
                double d2 = Math.abs(mouseEvent.getSceneY() - this.pressedY);
                mouseEvent.setDragDetect(d > this.hysteresisSizeX || d2 > this.hysteresisSizeY);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private boolean process(MouseEvent mouseEvent, EventTarget eventTarget) {
            boolean bl = true;
            if (this.dragDetected != DragDetectedState.DONE && (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED || mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) && mouseEvent.isDragDetect()) {
                this.processingDragDetected();
                if (eventTarget != null) {
                    MouseEvent mouseEvent2 = mouseEvent.copyFor(mouseEvent.getSource(), eventTarget, MouseEvent.DRAG_DETECTED);
                    try {
                        this.fireEvent(eventTarget, mouseEvent2);
                    }
                    finally {
                        if (this.dragboard != null) {
                            DragboardHelper.setDataAccessRestriction(this.dragboard, true);
                        }
                    }
                }
                this.dragDetectedProcessed();
            }
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                bl = false;
            }
            return bl;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private boolean processRecognized(DragEvent dragEvent) {
            MouseEvent mouseEvent = new MouseEvent(MouseEvent.DRAG_DETECTED, dragEvent.getX(), dragEvent.getY(), dragEvent.getSceneX(), dragEvent.getScreenY(), MouseButton.PRIMARY, 1, false, false, false, false, false, true, false, false, false, false, dragEvent.getPickResult());
            this.processingDragDetected();
            Node node = dragEvent.getPickResult().getIntersectedNode();
            try {
                this.fireEvent(node != null ? node : Scene.this, mouseEvent);
            }
            finally {
                if (this.dragboard != null) {
                    DragboardHelper.setDataAccessRestriction(this.dragboard, true);
                }
            }
            this.dragDetectedProcessed();
            boolean bl = this.dragboard != null && !this.dragboard.getContentTypes().isEmpty();
            return bl;
        }

        private void processDropEnd(DragEvent dragEvent) {
            if (this.source == null) {
                System.out.println("Scene.DnDGesture.processDropEnd() - UNEXPECTD - source is NULL");
                return;
            }
            dragEvent = new DragEvent(dragEvent.getSource(), this.source, DragEvent.DRAG_DONE, dragEvent.getDragboard(), dragEvent.getSceneX(), dragEvent.getSceneY(), dragEvent.getScreenX(), dragEvent.getScreenY(), dragEvent.getTransferMode(), this.source, this.target, dragEvent.getPickResult());
            Event.fireEvent(this.source, dragEvent);
            Scene.this.tmpTargetWrapper.clear();
            this.handleExitEnter(dragEvent, Scene.this.tmpTargetWrapper);
            Toolkit.getToolkit().stopDrag(this.dragboard);
        }

        private TransferMode processTargetEnterOver(DragEvent dragEvent) {
            Scene.this.pick(Scene.this.tmpTargetWrapper, dragEvent.getSceneX(), dragEvent.getSceneY());
            EventTarget eventTarget = Scene.this.tmpTargetWrapper.getEventTarget();
            if (this.dragboard == null) {
                this.dragboard = this.createDragboard(dragEvent, false);
            }
            dragEvent = new DragEvent(dragEvent.getSource(), eventTarget, dragEvent.getEventType(), this.dragboard, dragEvent.getSceneX(), dragEvent.getSceneY(), dragEvent.getScreenX(), dragEvent.getScreenY(), dragEvent.getTransferMode(), this.source, this.potentialTarget, dragEvent.getPickResult());
            this.handleExitEnter(dragEvent, Scene.this.tmpTargetWrapper);
            dragEvent = new DragEvent(dragEvent.getSource(), eventTarget, DragEvent.DRAG_OVER, dragEvent.getDragboard(), dragEvent.getSceneX(), dragEvent.getSceneY(), dragEvent.getScreenX(), dragEvent.getScreenY(), dragEvent.getTransferMode(), this.source, this.potentialTarget, dragEvent.getPickResult());
            this.fireEvent(eventTarget, dragEvent);
            Object object = dragEvent.getAcceptingObject();
            this.potentialTarget = object instanceof EventTarget ? (EventTarget)object : null;
            this.acceptedTransferMode = dragEvent.getAcceptedTransferMode();
            return this.acceptedTransferMode;
        }

        private void processTargetActionChanged(DragEvent dragEvent) {
        }

        private void processTargetExit(DragEvent dragEvent) {
            if (this.dragboard == null) {
                throw new NullPointerException("dragboard is null in processTargetExit()");
            }
            if (this.currentTargets.size() > 0) {
                this.potentialTarget = null;
                Scene.this.tmpTargetWrapper.clear();
                this.handleExitEnter(dragEvent, Scene.this.tmpTargetWrapper);
            }
        }

        private TransferMode processTargetDrop(DragEvent dragEvent) {
            Scene.this.pick(Scene.this.tmpTargetWrapper, dragEvent.getSceneX(), dragEvent.getSceneY());
            EventTarget eventTarget = Scene.this.tmpTargetWrapper.getEventTarget();
            dragEvent = new DragEvent(dragEvent.getSource(), eventTarget, DragEvent.DRAG_DROPPED, dragEvent.getDragboard(), dragEvent.getSceneX(), dragEvent.getSceneY(), dragEvent.getScreenX(), dragEvent.getScreenY(), this.acceptedTransferMode, this.source, this.potentialTarget, dragEvent.getPickResult());
            if (this.dragboard == null) {
                throw new NullPointerException("dragboard is null in processTargetDrop()");
            }
            this.handleExitEnter(dragEvent, Scene.this.tmpTargetWrapper);
            this.fireEvent(eventTarget, dragEvent);
            Object object = dragEvent.getAcceptingObject();
            this.target = this.potentialTarget = object instanceof EventTarget ? (EventTarget)object : null;
            TransferMode transferMode = dragEvent.isDropCompleted() ? dragEvent.getAcceptedTransferMode() : null;
            Scene.this.tmpTargetWrapper.clear();
            this.handleExitEnter(dragEvent, Scene.this.tmpTargetWrapper);
            return transferMode;
        }

        private void handleExitEnter(DragEvent dragEvent, TargetWrapper targetWrapper) {
            EventTarget eventTarget;
            EventTarget eventTarget2 = eventTarget = this.currentTargets.size() > 0 ? this.currentTargets.get(0) : null;
            if (targetWrapper.getEventTarget() != eventTarget) {
                EventTarget eventTarget3;
                int n;
                targetWrapper.fillHierarchy(this.newTargets);
                int n2 = this.currentTargets.size() - 1;
                for (n = this.newTargets.size() - 1; n2 >= 0 && n >= 0 && this.currentTargets.get(n2) == this.newTargets.get(n); --n2, --n) {
                }
                while (n2 >= 0) {
                    eventTarget3 = this.currentTargets.get(n2);
                    if (this.potentialTarget == eventTarget3) {
                        this.potentialTarget = null;
                    }
                    dragEvent = dragEvent.copyFor(dragEvent.getSource(), eventTarget3, this.source, this.potentialTarget, DragEvent.DRAG_EXITED_TARGET);
                    Event.fireEvent(eventTarget3, dragEvent);
                    --n2;
                }
                this.potentialTarget = null;
                while (n >= 0) {
                    eventTarget3 = this.newTargets.get(n);
                    Object object = (dragEvent = dragEvent.copyFor(dragEvent.getSource(), eventTarget3, this.source, this.potentialTarget, DragEvent.DRAG_ENTERED_TARGET)).getAcceptingObject();
                    if (object instanceof EventTarget) {
                        this.potentialTarget = (EventTarget)object;
                    }
                    Event.fireEvent(eventTarget3, dragEvent);
                    --n;
                }
                this.currentTargets.clear();
                this.currentTargets.addAll(this.newTargets);
                this.newTargets.clear();
            }
        }

        private boolean processKey(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && keyEvent.getCode() == KeyCode.ESCAPE) {
                DragEvent dragEvent = new DragEvent(this.source, this.source, DragEvent.DRAG_DONE, this.dragboard, 0.0, 0.0, 0.0, 0.0, null, this.source, null, null);
                if (this.source != null) {
                    Event.fireEvent(this.source, dragEvent);
                }
                Scene.this.tmpTargetWrapper.clear();
                this.handleExitEnter(dragEvent, Scene.this.tmpTargetWrapper);
                return false;
            }
            return true;
        }

        private Dragboard startDrag(EventTarget eventTarget, Set<TransferMode> set) {
            if (this.dragDetected != DragDetectedState.PROCESSING) {
                throw new IllegalStateException("Cannot start drag and drop outside of DRAG_DETECTED event handler");
            }
            if (set.isEmpty()) {
                this.dragboard = null;
            } else if (this.dragboard == null) {
                this.dragboard = this.createDragboard(null, true);
            }
            DragboardHelper.setDataAccessRestriction(this.dragboard, false);
            this.source = eventTarget;
            this.potentialTarget = eventTarget;
            this.sourceTransferModes = set;
            return this.dragboard;
        }

        private void startFullPDR(EventTarget eventTarget) {
            this.fullPDRSource = eventTarget;
        }

        private Dragboard createDragboard(DragEvent dragEvent, boolean bl) {
            Dragboard dragboard = null;
            if (dragEvent != null && (dragboard = dragEvent.getDragboard()) != null) {
                return dragboard;
            }
            TKClipboard tKClipboard = Scene.this.peer.createDragboard(bl);
            return DragboardHelper.createDragboard(tKClipboard);
        }
    }

    class DragGestureListener
    implements TKDragGestureListener {
        DragGestureListener() {
        }

        @Override
        public void dragGestureRecognized(double d, double d2, double d3, double d4, int n, TKClipboard tKClipboard) {
            Dragboard dragboard = DragboardHelper.createDragboard(tKClipboard);
            Scene.this.dndGesture = new DnDGesture();
            Scene.this.dndGesture.dragboard = dragboard;
            DragEvent dragEvent = new DragEvent(DragEvent.ANY, dragboard, d, d2, d3, d4, null, null, null, Scene.this.pick(d, d2));
            Scene.this.dndGesture.processRecognized(dragEvent);
            Scene.this.dndGesture = null;
        }
    }

    class DropTargetListener
    implements TKDropTargetListener {
        DropTargetListener() {
        }

        @Override
        public TransferMode dragEnter(double d, double d2, double d3, double d4, TransferMode transferMode, TKClipboard tKClipboard) {
            if (Scene.this.dndGesture == null) {
                Scene.this.dndGesture = new DnDGesture();
            }
            Dragboard dragboard = DragboardHelper.createDragboard(tKClipboard);
            Scene.this.dndGesture.dragboard = dragboard;
            DragEvent dragEvent = new DragEvent(DragEvent.ANY, Scene.this.dndGesture.dragboard, d, d2, d3, d4, transferMode, null, null, Scene.this.pick(d, d2));
            return Scene.this.dndGesture.processTargetEnterOver(dragEvent);
        }

        @Override
        public TransferMode dragOver(double d, double d2, double d3, double d4, TransferMode transferMode) {
            if (Scene.this.dndGesture == null) {
                System.err.println("GOT A dragOver when dndGesture is null!");
                return null;
            }
            if (Scene.this.dndGesture.dragboard == null) {
                throw new RuntimeException("dndGesture.dragboard is null in dragOver");
            }
            DragEvent dragEvent = new DragEvent(DragEvent.ANY, Scene.this.dndGesture.dragboard, d, d2, d3, d4, transferMode, null, null, Scene.this.pick(d, d2));
            return Scene.this.dndGesture.processTargetEnterOver(dragEvent);
        }

        @Override
        public void dragExit(double d, double d2, double d3, double d4) {
            if (Scene.this.dndGesture == null) {
                System.err.println("GOT A dragExit when dndGesture is null!");
            } else {
                if (Scene.this.dndGesture.dragboard == null) {
                    throw new RuntimeException("dndGesture.dragboard is null in dragExit");
                }
                DragEvent dragEvent = new DragEvent(DragEvent.ANY, Scene.this.dndGesture.dragboard, d, d2, d3, d4, null, null, null, Scene.this.pick(d, d2));
                Scene.this.dndGesture.processTargetExit(dragEvent);
                if (Scene.this.dndGesture.source == null) {
                    Scene.this.dndGesture.dragboard = null;
                    Scene.this.dndGesture = null;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public TransferMode drop(double d, double d2, double d3, double d4, TransferMode transferMode) {
            TransferMode transferMode2;
            if (Scene.this.dndGesture == null) {
                System.err.println("GOT A drop when dndGesture is null!");
                return null;
            }
            if (Scene.this.dndGesture.dragboard == null) {
                throw new RuntimeException("dndGesture.dragboard is null in dragDrop");
            }
            DragEvent dragEvent = new DragEvent(DragEvent.ANY, Scene.this.dndGesture.dragboard, d, d2, d3, d4, transferMode, null, null, Scene.this.pick(d, d2));
            DragboardHelper.setDataAccessRestriction(Scene.this.dndGesture.dragboard, false);
            try {
                transferMode2 = Scene.this.dndGesture.processTargetDrop(dragEvent);
            }
            finally {
                DragboardHelper.setDataAccessRestriction(Scene.this.dndGesture.dragboard, true);
            }
            if (Scene.this.dndGesture.source == null) {
                Scene.this.dndGesture.dragboard = null;
                Scene.this.dndGesture = null;
            }
            return transferMode2;
        }
    }

    private class ScenePeerPaintListener
    implements TKScenePaintListener {
        private ScenePeerPaintListener() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void frameRendered() {
            Object object = trackerMonitor;
            synchronized (object) {
                if (Scene.this.tracker != null) {
                    Scene.this.tracker.frameRendered();
                }
            }
        }
    }

    class ScenePeerListener
    implements TKSceneListener {
        ScenePeerListener() {
        }

        @Override
        public void changedLocation(float f, float f2) {
            if ((double)f != Scene.this.getX()) {
                Scene.this.setX(f);
            }
            if ((double)f2 != Scene.this.getY()) {
                Scene.this.setY(f2);
            }
        }

        @Override
        public void changedSize(float f, float f2) {
            if ((double)f != Scene.this.getWidth()) {
                Scene.this.setWidth(f);
            }
            if ((double)f2 != Scene.this.getHeight()) {
                Scene.this.setHeight(f2);
            }
        }

        @Override
        public void mouseEvent(EventType<MouseEvent> eventType, double d, double d2, double d3, double d4, MouseButton mouseButton, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7, boolean bl8, boolean bl9) {
            MouseEvent mouseEvent = new MouseEvent(eventType, d, d2, d3, d4, mouseButton, 0, bl3, bl4, bl5, bl6, bl7, bl8, bl9, bl2, bl, false, null);
            Scene.this.processMouseEvent(mouseEvent);
        }

        @Override
        public void keyEvent(KeyEvent keyEvent) {
            Scene.this.processKeyEvent(keyEvent);
        }

        @Override
        public void inputMethodEvent(EventType<InputMethodEvent> eventType, ObservableList<InputMethodTextRun> observableList, String string, int n) {
            InputMethodEvent inputMethodEvent = new InputMethodEvent(eventType, observableList, string, n);
            Scene.this.processInputMethodEvent(inputMethodEvent);
        }

        @Override
        public void menuEvent(double d, double d2, double d3, double d4, boolean bl) {
            Scene.this.processMenuEvent(d, d2, d3, d4, bl);
        }

        @Override
        public void scrollEvent(EventType<ScrollEvent> eventType, double d, double d2, double d3, double d4, double d5, double d6, int n, int n2, int n3, int n4, int n5, double d7, double d8, double d9, double d10, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
            double d11;
            ScrollEvent.HorizontalTextScrollUnits horizontalTextScrollUnits = n2 > 0 ? ScrollEvent.HorizontalTextScrollUnits.CHARACTERS : ScrollEvent.HorizontalTextScrollUnits.NONE;
            double d12 = d11 = n2 < 0 ? 0.0 : (double)n2 * d;
            ScrollEvent.VerticalTextScrollUnits verticalTextScrollUnits = n3 > 0 ? ScrollEvent.VerticalTextScrollUnits.LINES : (n3 < 0 ? ScrollEvent.VerticalTextScrollUnits.PAGES : ScrollEvent.VerticalTextScrollUnits.NONE);
            double d13 = n3 < 0 ? d2 : (double)n3 * d2;
            d5 = n4 > 0 && n2 >= 0 ? (double)Math.round(d5 * (double)n2 / (double)n4) : d5;
            double d14 = d6 = n5 > 0 && n3 >= 0 ? (double)Math.round(d6 * (double)n3 / (double)n5) : d6;
            if (eventType == ScrollEvent.SCROLL_FINISHED) {
                d7 = ((Scene)Scene.this).scrollGesture.sceneCoords.getX();
                d8 = ((Scene)Scene.this).scrollGesture.sceneCoords.getY();
                d9 = ((Scene)Scene.this).scrollGesture.screenCoords.getX();
                d10 = ((Scene)Scene.this).scrollGesture.screenCoords.getY();
            } else if (Double.isNaN(d7) || Double.isNaN(d8) || Double.isNaN(d9) || Double.isNaN(d10)) {
                if (Scene.this.cursorScenePos == null || Scene.this.cursorScreenPos == null) {
                    return;
                }
                d7 = Scene.this.cursorScenePos.getX();
                d8 = Scene.this.cursorScenePos.getY();
                d9 = Scene.this.cursorScreenPos.getX();
                d10 = Scene.this.cursorScreenPos.getY();
            }
            inMousePick = true;
            Scene.this.processGestureEvent(new ScrollEvent(eventType, d7, d8, d9, d10, bl, bl2, bl3, bl4, bl5, bl6, d * d5, d2 * d6, d3 * d5, d4 * d6, d5, d6, horizontalTextScrollUnits, d11, verticalTextScrollUnits, d13, n, Scene.this.pick(d7, d8)), Scene.this.scrollGesture);
            inMousePick = false;
        }

        @Override
        public void zoomEvent(EventType<ZoomEvent> eventType, double d, double d2, double d3, double d4, double d5, double d6, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
            if (eventType == ZoomEvent.ZOOM_FINISHED) {
                d3 = ((Scene)Scene.this).zoomGesture.sceneCoords.getX();
                d4 = ((Scene)Scene.this).zoomGesture.sceneCoords.getY();
                d5 = ((Scene)Scene.this).zoomGesture.screenCoords.getX();
                d6 = ((Scene)Scene.this).zoomGesture.screenCoords.getY();
            } else if (Double.isNaN(d3) || Double.isNaN(d4) || Double.isNaN(d5) || Double.isNaN(d6)) {
                if (Scene.this.cursorScenePos == null || Scene.this.cursorScreenPos == null) {
                    return;
                }
                d3 = Scene.this.cursorScenePos.getX();
                d4 = Scene.this.cursorScenePos.getY();
                d5 = Scene.this.cursorScreenPos.getX();
                d6 = Scene.this.cursorScreenPos.getY();
            }
            inMousePick = true;
            Scene.this.processGestureEvent(new ZoomEvent(eventType, d3, d4, d5, d6, bl, bl2, bl3, bl4, bl5, bl6, d, d2, Scene.this.pick(d3, d4)), Scene.this.zoomGesture);
            inMousePick = false;
        }

        @Override
        public void rotateEvent(EventType<RotateEvent> eventType, double d, double d2, double d3, double d4, double d5, double d6, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
            if (eventType == RotateEvent.ROTATION_FINISHED) {
                d3 = ((Scene)Scene.this).rotateGesture.sceneCoords.getX();
                d4 = ((Scene)Scene.this).rotateGesture.sceneCoords.getY();
                d5 = ((Scene)Scene.this).rotateGesture.screenCoords.getX();
                d6 = ((Scene)Scene.this).rotateGesture.screenCoords.getY();
            } else if (Double.isNaN(d3) || Double.isNaN(d4) || Double.isNaN(d5) || Double.isNaN(d6)) {
                if (Scene.this.cursorScenePos == null || Scene.this.cursorScreenPos == null) {
                    return;
                }
                d3 = Scene.this.cursorScenePos.getX();
                d4 = Scene.this.cursorScenePos.getY();
                d5 = Scene.this.cursorScreenPos.getX();
                d6 = Scene.this.cursorScreenPos.getY();
            }
            inMousePick = true;
            Scene.this.processGestureEvent(new RotateEvent(eventType, d3, d4, d5, d6, bl, bl2, bl3, bl4, bl5, bl6, d, d2, Scene.this.pick(d3, d4)), Scene.this.rotateGesture);
            inMousePick = false;
        }

        @Override
        public void swipeEvent(EventType<SwipeEvent> eventType, int n, double d, double d2, double d3, double d4, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
            if (Double.isNaN(d) || Double.isNaN(d2) || Double.isNaN(d3) || Double.isNaN(d4)) {
                if (Scene.this.cursorScenePos == null || Scene.this.cursorScreenPos == null) {
                    return;
                }
                d = Scene.this.cursorScenePos.getX();
                d2 = Scene.this.cursorScenePos.getY();
                d3 = Scene.this.cursorScreenPos.getX();
                d4 = Scene.this.cursorScreenPos.getY();
            }
            inMousePick = true;
            Scene.this.processGestureEvent(new SwipeEvent(eventType, d, d2, d3, d4, bl, bl2, bl3, bl4, bl5, n, Scene.this.pick(d, d2)), Scene.this.swipeGesture);
            inMousePick = false;
        }

        @Override
        public void touchEventBegin(long l, int n, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
            if (!bl) {
                Scene.this.nextTouchEvent = null;
                return;
            }
            Scene.this.nextTouchEvent = new TouchEvent(TouchEvent.ANY, null, null, 0, bl2, bl3, bl4, bl5);
            if (Scene.this.touchPoints == null || Scene.this.touchPoints.length != n) {
                Scene.access$5502(Scene.this, new TouchPoint[n]);
            }
            Scene.this.touchPointIndex = 0;
        }

        @Override
        public void touchEventNext(TouchPoint.State state, long l, double d, double d2, double d3, double d4) {
            TouchPoint touchPoint;
            int n;
            int n2;
            inMousePick = true;
            if (Scene.this.nextTouchEvent == null) {
                return;
            }
            Scene.this.touchPointIndex++;
            int n3 = n2 = state == TouchPoint.State.PRESSED ? Scene.this.touchMap.add(l) : Scene.this.touchMap.get(l);
            if (state == TouchPoint.State.RELEASED) {
                Scene.this.touchMap.remove(l);
            }
            if ((n = Scene.this.touchMap.getOrder(n2)) >= Scene.this.touchPoints.length) {
                throw new RuntimeException("Too many touch points reported");
            }
            boolean bl = false;
            PickResult pickResult = Scene.this.pick(d, d2);
            EventTarget eventTarget = (EventTarget)Scene.this.touchTargets.get(n2);
            if (eventTarget == null) {
                eventTarget = pickResult.getIntersectedNode();
                if (eventTarget == null) {
                    eventTarget = Scene.this;
                }
            } else {
                bl = true;
            }
            ((Scene)Scene.this).touchPoints[n] = touchPoint = new TouchPoint(n2, state, d, d2, d3, d4, eventTarget, pickResult);
            if (bl) {
                touchPoint.grab(eventTarget);
            }
            if (touchPoint.getState() == TouchPoint.State.PRESSED) {
                touchPoint.grab(eventTarget);
                Scene.this.touchTargets.put(touchPoint.getId(), eventTarget);
            } else if (touchPoint.getState() == TouchPoint.State.RELEASED) {
                Scene.this.touchTargets.remove(touchPoint.getId());
            }
            inMousePick = false;
        }

        @Override
        public void touchEventEnd() {
            if (Scene.this.nextTouchEvent == null) {
                return;
            }
            if (Scene.this.touchPointIndex != Scene.this.touchPoints.length) {
                throw new RuntimeException("Wrong number of touch points reported");
            }
            Scene.this.processTouchEvent(Scene.this.nextTouchEvent, Scene.this.touchPoints);
            if (Scene.this.touchMap.cleanup()) {
                Scene.this.touchEventSetId = 0;
            }
        }

        @Override
        public Accessible getSceneAccessible() {
            return Scene.this.getAccessible();
        }
    }

    class ScenePulseListener
    implements TKPulseListener {
        private boolean firstPulse = true;

        ScenePulseListener() {
        }

        private void synchronizeSceneNodes() {
            Toolkit.getToolkit().checkFxUserThread();
            inSynchronizer = true;
            if (Scene.this.dirtyNodes == null) {
                this.syncAll(Scene.this.getRoot());
                Scene.access$2302(Scene.this, new Node[30]);
            } else {
                for (int i = 0; i < Scene.this.dirtyNodesSize; ++i) {
                    Node node = Scene.this.dirtyNodes[i];
                    ((Scene)Scene.this).dirtyNodes[i] = null;
                    if (node.getScene() != Scene.this) continue;
                    node.syncPeer();
                }
                Scene.this.dirtyNodesSize = 0;
            }
            inSynchronizer = false;
        }

        private int syncAll(Node node) {
            node.syncPeer();
            int n = 1;
            if (node instanceof Parent) {
                Parent parent = (Parent)node;
                int n2 = parent.getChildren().size();
                for (int i = 0; i < n2; ++i) {
                    Node node2 = (Node)parent.getChildren().get(i);
                    if (node2 == null) continue;
                    n += this.syncAll(node2);
                }
            } else if (node instanceof SubScene) {
                SubScene subScene = (SubScene)node;
                n += this.syncAll(subScene.getRoot());
            }
            if (node.getClip() != null) {
                n += this.syncAll(node.getClip());
            }
            return n;
        }

        private void synchronizeSceneProperties() {
            Object object;
            inSynchronizer = true;
            if (Scene.this.isDirty(DirtyBits.ROOT_DIRTY)) {
                Scene.this.peer.setRoot((NGNode)Scene.this.getRoot().getPeer());
            }
            if (Scene.this.isDirty(DirtyBits.FILL_DIRTY)) {
                object = Toolkit.getToolkit();
                Scene.this.peer.setFillPaint(Scene.this.getFill() == null ? null : ((Toolkit)object).getPaint(Scene.this.getFill()));
            }
            object = Scene.this.getEffectiveCamera();
            if (Scene.this.isDirty(DirtyBits.CAMERA_DIRTY)) {
                NodeHelper.updatePeer((Node)object);
                Scene.this.peer.setCamera((NGCamera)((Node)object).getPeer());
            }
            if (Scene.this.isDirty(DirtyBits.CURSOR_DIRTY)) {
                Scene.this.mouseHandler.updateCursor(Scene.this.getCursor());
                Scene.this.mouseHandler.updateCursorFrame();
            }
            Scene.this.clearDirty();
            inSynchronizer = false;
        }

        private void focusCleanup() {
            if (Scene.this.isFocusDirty()) {
                Node node = Scene.this.getFocusOwner();
                if (node == null) {
                    Scene.this.focusInitial();
                } else if (node.getScene() != Scene.this) {
                    Scene.this.requestFocus(null);
                    Scene.this.focusInitial();
                } else if (!node.isCanReceiveFocus()) {
                    Scene.this.requestFocus(null);
                    Scene.this.focusIneligible(node);
                }
                Scene.this.setFocusDirty(false);
            }
        }

        @Override
        public void pulse() {
            boolean bl;
            if (Scene.this.tracker != null) {
                Scene.this.tracker.pulse();
            }
            if (this.firstPulse) {
                PerformanceTracker.logEvent("Scene - first repaint");
            }
            this.focusCleanup();
            Scene.this.disposeAccessibles();
            if (Scene.this.preLayoutPulseListeners != null) {
                for (Runnable runnable : Scene.this.preLayoutPulseListeners) {
                    runnable.run();
                }
            }
            if (PulseLogger.PULSE_LOGGING_ENABLED) {
                PulseLogger.newPhase("CSS Pass");
            }
            Scene.this.doCSSPass();
            if (PulseLogger.PULSE_LOGGING_ENABLED) {
                PulseLogger.newPhase("Layout Pass");
            }
            Scene.this.doLayoutPass();
            if (Scene.this.postLayoutPulseListeners != null) {
                for (Runnable runnable : Scene.this.postLayoutPulseListeners) {
                    runnable.run();
                }
            }
            boolean bl2 = bl = Scene.this.dirtyNodes == null || Scene.this.dirtyNodesSize != 0 || !Scene.this.isDirtyEmpty();
            if (bl) {
                if (PulseLogger.PULSE_LOGGING_ENABLED) {
                    PulseLogger.newPhase("Update bounds");
                }
                Scene.this.getRoot().updateBounds();
                if (Scene.this.peer != null) {
                    try {
                        if (PulseLogger.PULSE_LOGGING_ENABLED) {
                            PulseLogger.newPhase("Waiting for previous rendering");
                        }
                        Scene.this.peer.waitForRenderingToComplete();
                        Scene.this.peer.waitForSynchronization();
                        if (PulseLogger.PULSE_LOGGING_ENABLED) {
                            PulseLogger.newPhase("Copy state to render graph");
                        }
                        Scene.this.syncLights();
                        this.synchronizeSceneProperties();
                        this.synchronizeSceneNodes();
                        Scene.this.mouseHandler.pulse();
                        Scene.this.peer.markDirty();
                    }
                    finally {
                        Scene.this.peer.releaseSynchronization(true);
                    }
                } else {
                    if (PulseLogger.PULSE_LOGGING_ENABLED) {
                        PulseLogger.newPhase("Synchronize with null peer");
                    }
                    this.synchronizeSceneNodes();
                    Scene.this.mouseHandler.pulse();
                }
                if (Scene.this.getRoot().cssFlag != CssFlags.CLEAN) {
                    NodeHelper.markDirty(Scene.this.getRoot(), com.sun.javafx.scene.DirtyBits.NODE_CSS);
                }
            }
            Scene.this.mouseHandler.updateCursorFrame();
            if (this.firstPulse) {
                if (PerformanceTracker.isLoggingEnabled()) {
                    PerformanceTracker.logEvent("Scene - first repaint - layout complete");
                    if (PrismSettings.perfLogFirstPaintFlush) {
                        PerformanceTracker.outputLog();
                    }
                    if (PrismSettings.perfLogFirstPaintExit) {
                        System.exit(0);
                    }
                }
                this.firstPulse = false;
            }
            if (Scene.this.testPulseListener != null) {
                Scene.this.testPulseListener.run();
            }
        }
    }

    private static enum DirtyBits {
        FILL_DIRTY,
        ROOT_DIRTY,
        CAMERA_DIRTY,
        LIGHTS_DIRTY,
        CURSOR_DIRTY;

        private int mask = 1 << this.ordinal();

        public final int getMask() {
            return this.mask;
        }
    }

    private static class TouchGesture {
        WeakReference<EventTarget> target;
        Point2D sceneCoords;
        Point2D screenCoords;
        boolean finished;

        private TouchGesture() {
        }
    }
}

