/*
 * Decompiled with CFR 0.152.
 */
package javafx.embed.swing;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.embed.swing.Disposer;
import com.sun.javafx.embed.swing.InteropFactory;
import com.sun.javafx.embed.swing.SwingEvents;
import com.sun.javafx.embed.swing.SwingNodeHelper;
import com.sun.javafx.embed.swing.SwingNodeInterop;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.sg.prism.NGExternalNode;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.stage.FocusUngrabEvent;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.TKStage;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowFocusListener;
import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javax.swing.JComponent;
import javax.swing.Timer;

public class SwingNode
extends Node {
    private static boolean isThreadMerged;
    private SwingNodeInterop swNodeIOP;
    private static InteropFactory iopFactoryInstance;
    private double fxWidth;
    private double fxHeight;
    private int swingPrefWidth;
    private int swingPrefHeight;
    private int swingMaxWidth;
    private int swingMaxHeight;
    private int swingMinWidth;
    private int swingMinHeight;
    private volatile JComponent content;
    private volatile Object lwFrame;
    private NGExternalNode peer;
    private final ReentrantLock paintLock = new ReentrantLock();
    private boolean skipBackwardUnrgabNotification;
    private boolean grabbed;
    private Timer deactivate;
    private EventHandler windowHiddenHandler;
    private Window hWindow;
    private List<Runnable> peerRequests;
    private final InvalidationListener locationListener;
    private final EventHandler<FocusUngrabEvent> ungrabHandler;
    private final ChangeListener<Boolean> windowVisibleListener;
    private final ChangeListener<Window> sceneWindowListener;

    private final Object getLightweightFrame() {
        return this.lwFrame;
    }

    private ReentrantLock getPaintLock() {
        return this.paintLock;
    }

    public SwingNode() {
        SwingNodeHelper.initHelper(this);
        this.windowHiddenHandler = event -> {
            Window window;
            TKStage tKStage;
            if (this.lwFrame != null && event.getTarget() instanceof Window && (tKStage = WindowHelper.getPeer(window = (Window)event.getTarget())) != null) {
                if (isThreadMerged) {
                    this.swNodeIOP.overrideNativeWindowHandle(this.lwFrame, 0L, null);
                } else {
                    tKStage.postponeClose();
                    SwingNodeHelper.runOnEDT(() -> this.swNodeIOP.overrideNativeWindowHandle(this.lwFrame, 0L, () -> SwingNodeHelper.runOnFxThread(() -> tKStage.closePostponed())));
                }
            }
        };
        this.hWindow = null;
        this.peerRequests = new ArrayList<Runnable>();
        this.locationListener = observable -> this.locateLwFrame();
        this.ungrabHandler = focusUngrabEvent -> {
            if (!this.skipBackwardUnrgabNotification && this.lwFrame != null) {
                AccessController.doPrivileged(new PostEventAction(this.swNodeIOP.createUngrabEvent(this.lwFrame)));
            }
        };
        this.windowVisibleListener = (observableValue, bl, bl2) -> {
            if (!bl2.booleanValue()) {
                this.disposeLwFrame();
            } else {
                this.setContent(this.content);
            }
        };
        this.sceneWindowListener = (observableValue, window, window2) -> {
            if (window != null) {
                this.removeWindowListeners((Window)window);
            }
            this.notifyNativeHandle((Window)window2);
            if (window2 != null) {
                this.addWindowListeners((Window)window2);
            }
        };
        this.swNodeIOP = iopFactoryInstance.createSwingNodeImpl();
        this.setFocusTraversable(true);
        this.setEventHandler(javafx.scene.input.MouseEvent.ANY, new SwingMouseEventHandler());
        this.setEventHandler(KeyEvent.ANY, new SwingKeyEventHandler());
        this.setEventHandler(ScrollEvent.SCROLL, new SwingScrollEventHandler());
        this.focusedProperty().addListener((observableValue, bl, bl2) -> this.activateLwFrame((boolean)bl2));
        Font.getFamilies();
    }

    private void notifyNativeHandle(Window window) {
        if (this.hWindow != window) {
            if (this.hWindow != null) {
                this.hWindow.removeEventHandler(WindowEvent.WINDOW_HIDDEN, this.windowHiddenHandler);
            }
            if (window != null) {
                window.addEventHandler(WindowEvent.WINDOW_HIDDEN, this.windowHiddenHandler);
            }
            this.hWindow = window;
        }
        if (this.lwFrame != null) {
            TKStage tKStage;
            long l = 0L;
            if (window != null && (tKStage = WindowHelper.getPeer(window)) != null) {
                l = tKStage.getRawHandle();
            }
            this.swNodeIOP.overrideNativeWindowHandle(this.lwFrame, l, null);
        }
    }

    public void setContent(JComponent jComponent) {
        this.content = jComponent;
        SwingNodeHelper.runOnEDT(() -> this.setContentImpl(jComponent));
    }

    public JComponent getContent() {
        return this.content;
    }

    private void setContentImpl(JComponent jComponent) {
        if (this.lwFrame != null) {
            this.swNodeIOP.disposeFrame(this.lwFrame);
            this.lwFrame = null;
        }
        if (jComponent != null) {
            Window window;
            this.lwFrame = this.swNodeIOP.createLightweightFrame();
            SwingNodeWindowFocusListener swingNodeWindowFocusListener = new SwingNodeWindowFocusListener(this);
            this.swNodeIOP.addWindowFocusListener(this.lwFrame, swingNodeWindowFocusListener);
            if (this.getScene() != null && (window = this.getScene().getWindow()) != null) {
                this.swNodeIOP.notifyDisplayChanged(this.lwFrame, window.getRenderScaleX(), window.getRenderScaleY());
            }
            this.swNodeIOP.setContent(this.lwFrame, this.swNodeIOP.createSwingNodeContent(jComponent, this));
            this.swNodeIOP.setVisible(this.lwFrame, true);
            Disposer.addRecord(this, this.swNodeIOP.createSwingNodeDisposer(this.lwFrame));
            if (this.getScene() != null) {
                this.notifyNativeHandle(this.getScene().getWindow());
            }
            SwingNodeHelper.runOnFxThread(() -> {
                this.locateLwFrame();
                if (this.focusedProperty().get()) {
                    this.activateLwFrame(true);
                }
            });
        }
    }

    void setImageBuffer(int[] nArray, int n, int n2, int n3, int n4, int n5, double d, double d2) {
        Runnable runnable = () -> this.peer.setImageBuffer(IntBuffer.wrap(nArray), n, n2, n3, n4, n3, n4, n5, d, d2);
        SwingNodeHelper.runOnFxThread(() -> {
            if (this.peer != null) {
                runnable.run();
            } else {
                this.peerRequests.clear();
                this.peerRequests.add(runnable);
            }
        });
    }

    void setImageBounds(int n, int n2, int n3, int n4) {
        Runnable runnable = () -> this.peer.setImageBounds(n, n2, n3, n4, n3, n4);
        SwingNodeHelper.runOnFxThread(() -> {
            if (this.peer != null) {
                runnable.run();
            } else {
                this.peerRequests.add(runnable);
            }
        });
    }

    void repaintDirtyRegion(int n, int n2, int n3, int n4) {
        Runnable runnable = () -> {
            this.peer.repaintDirtyRegion(n, n2, n3, n4);
            NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
        };
        SwingNodeHelper.runOnFxThread(() -> {
            if (this.peer != null) {
                runnable.run();
            } else {
                this.peerRequests.add(runnable);
            }
        });
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double d, double d2) {
        super.resize(d, d2);
        if (d != this.fxWidth || d2 != this.fxHeight) {
            this.fxWidth = d;
            this.fxHeight = d2;
            NodeHelper.geomChanged(this);
            NodeHelper.markDirty(this, DirtyBits.NODE_GEOMETRY);
            SwingNodeHelper.runOnEDT(() -> {
                if (this.lwFrame != null) {
                    this.locateLwFrame();
                }
            });
        }
    }

    @Override
    public double prefWidth(double d) {
        return this.swingPrefWidth;
    }

    @Override
    public double prefHeight(double d) {
        return this.swingPrefHeight;
    }

    @Override
    public double maxWidth(double d) {
        return this.swingMaxWidth;
    }

    @Override
    public double maxHeight(double d) {
        return this.swingMaxHeight;
    }

    @Override
    public double minWidth(double d) {
        return this.swingMinWidth;
    }

    @Override
    public double minHeight(double d) {
        return this.swingMinHeight;
    }

    private boolean doComputeContains(double d, double d2) {
        return true;
    }

    private void removeSceneListeners(Scene scene) {
        Window window = scene.getWindow();
        if (window != null) {
            this.removeWindowListeners(window);
        }
        scene.windowProperty().removeListener(this.sceneWindowListener);
    }

    private void addSceneListeners(Scene scene) {
        Window window = scene.getWindow();
        if (window != null) {
            this.addWindowListeners(window);
            this.notifyNativeHandle(window);
        }
        scene.windowProperty().addListener(this.sceneWindowListener);
    }

    private void addWindowListeners(Window window) {
        window.xProperty().addListener(this.locationListener);
        window.yProperty().addListener(this.locationListener);
        window.widthProperty().addListener(this.locationListener);
        window.heightProperty().addListener(this.locationListener);
        window.renderScaleXProperty().addListener(this.locationListener);
        window.addEventHandler(FocusUngrabEvent.FOCUS_UNGRAB, this.ungrabHandler);
        window.showingProperty().addListener(this.windowVisibleListener);
        this.setLwFrameScale(window.getRenderScaleX(), window.getRenderScaleY());
    }

    private void removeWindowListeners(Window window) {
        window.xProperty().removeListener(this.locationListener);
        window.yProperty().removeListener(this.locationListener);
        window.widthProperty().removeListener(this.locationListener);
        window.heightProperty().removeListener(this.locationListener);
        window.renderScaleXProperty().removeListener(this.locationListener);
        window.removeEventHandler(FocusUngrabEvent.FOCUS_UNGRAB, this.ungrabHandler);
        window.showingProperty().removeListener(this.windowVisibleListener);
    }

    private NGNode doCreatePeer() {
        this.peer = new NGExternalNode();
        this.peer.setLock(this.paintLock);
        for (Runnable runnable : this.peerRequests) {
            runnable.run();
        }
        this.peerRequests = null;
        if (this.getScene() != null) {
            this.addSceneListeners(this.getScene());
        }
        this.sceneProperty().addListener((observableValue, scene, scene2) -> {
            if (scene != null) {
                this.removeSceneListeners((Scene)scene);
                this.disposeLwFrame();
            }
            if (scene2 != null) {
                if (this.content != null && this.lwFrame == null) {
                    this.setContent(this.content);
                }
                this.addSceneListeners((Scene)scene2);
            }
        });
        NodeHelper.treeVisibleProperty(this).addListener((observableValue, bl, bl2) -> this.setLwFrameVisible((boolean)bl2));
        return this.peer;
    }

    private void doUpdatePeer() {
        if (NodeHelper.isDirty(this, DirtyBits.NODE_VISIBLE) || NodeHelper.isDirty(this, DirtyBits.NODE_BOUNDS)) {
            this.locateLwFrame();
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            this.peer.markContentDirty();
        }
    }

    private void locateLwFrame() {
        if (this.getScene() == null || this.lwFrame == null || this.getScene().getWindow() == null || !this.getScene().getWindow().isShowing()) {
            return;
        }
        Window window = this.getScene().getWindow();
        double d = window.getRenderScaleX();
        double d2 = window.getRenderScaleY();
        Point2D point2D = this.localToScene(0.0, 0.0);
        int n = (int)window.getX();
        int n2 = (int)window.getY();
        int n3 = (int)window.getWidth();
        int n4 = (int)window.getHeight();
        int n5 = (int)Math.round(window.getX() + this.getScene().getX() + point2D.getX());
        int n6 = (int)Math.round(window.getY() + this.getScene().getY() + point2D.getY());
        int n7 = (int)this.fxWidth;
        int n8 = (int)this.fxHeight;
        SwingNodeHelper.runOnEDT(() -> {
            if (this.lwFrame != null) {
                this.swNodeIOP.notifyDisplayChanged(this.lwFrame, d, d2);
                this.swNodeIOP.setBounds(this.lwFrame, n5, n6, n7, n8);
                this.swNodeIOP.setHostBounds(this.lwFrame, n, n2, n3, n4);
            }
        });
    }

    private void activateLwFrame(boolean bl) {
        if (this.lwFrame == null) {
            return;
        }
        if (PlatformUtil.isLinux()) {
            if (this.deactivate == null || !this.deactivate.isRunning()) {
                if (!bl) {
                    this.deactivate = new Timer(50, actionEvent -> {
                        if (this.lwFrame != null) {
                            this.swNodeIOP.emulateActivation(this.lwFrame, false);
                        }
                    });
                    this.deactivate.start();
                    return;
                }
            } else {
                this.deactivate.stop();
            }
        }
        SwingNodeHelper.runOnEDT(() -> {
            if (this.lwFrame != null) {
                this.swNodeIOP.emulateActivation(this.lwFrame, bl);
            }
        });
    }

    private void disposeLwFrame() {
        if (this.lwFrame == null) {
            return;
        }
        SwingNodeHelper.runOnEDT(() -> {
            if (this.lwFrame != null) {
                this.swNodeIOP.disposeFrame(this.lwFrame);
                this.lwFrame = null;
            }
        });
    }

    private void setLwFrameVisible(boolean bl) {
        if (this.lwFrame == null) {
            return;
        }
        SwingNodeHelper.runOnEDT(() -> {
            if (this.lwFrame != null) {
                this.swNodeIOP.setVisible(this.lwFrame, bl);
            }
        });
    }

    private void setLwFrameScale(double d, double d2) {
        if (this.lwFrame == null) {
            return;
        }
        SwingNodeHelper.runOnEDT(() -> {
            if (this.lwFrame != null) {
                this.swNodeIOP.notifyDisplayChanged(this.lwFrame, d, d2);
            }
        });
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        baseBounds.deriveWithNewBounds(0.0f, 0.0f, 0.0f, (float)this.fxWidth, (float)this.fxHeight, 0.0f);
        baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private void ungrabFocus(boolean bl) {
        if (PlatformUtil.isLinux()) {
            return;
        }
        if (this.grabbed && this.getScene() != null && this.getScene().getWindow() != null && WindowHelper.getPeer(this.getScene().getWindow()) != null) {
            this.skipBackwardUnrgabNotification = !bl;
            WindowHelper.getPeer(this.getScene().getWindow()).ungrabFocus();
            this.skipBackwardUnrgabNotification = false;
            this.grabbed = false;
        }
    }

    static {
        iopFactoryInstance = null;
        try {
            iopFactoryInstance = InteropFactory.getInstance();
        }
        catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                isThreadMerged = Boolean.valueOf(System.getProperty("javafx.embed.singleThread"));
                return null;
            }
        });
        SwingNodeHelper.setSwingNodeAccessor(new SwingNodeHelper.SwingNodeAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((SwingNode)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((SwingNode)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((SwingNode)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((SwingNode)node).doComputeContains(d, d2);
            }

            @Override
            public Object getLightweightFrame(SwingNode swingNode) {
                return swingNode.getLightweightFrame();
            }

            @Override
            public ReentrantLock getPaintLock(SwingNode swingNode) {
                return swingNode.getPaintLock();
            }

            @Override
            public void setImageBuffer(SwingNode swingNode, int[] nArray, int n, int n2, int n3, int n4, int n5, double d, double d2) {
                swingNode.setImageBuffer(nArray, n, n2, n3, n4, n5, d, d2);
            }

            @Override
            public void setImageBounds(SwingNode swingNode, int n, int n2, int n3, int n4) {
                swingNode.setImageBounds(n, n2, n3, n4);
            }

            @Override
            public void repaintDirtyRegion(SwingNode swingNode, int n, int n2, int n3, int n4) {
                swingNode.repaintDirtyRegion(n, n2, n3, n4);
            }

            @Override
            public void ungrabFocus(SwingNode swingNode, boolean bl) {
                swingNode.ungrabFocus(bl);
            }

            @Override
            public void setSwingPrefWidth(SwingNode swingNode, int n) {
                swingNode.swingPrefWidth = n;
            }

            @Override
            public void setSwingPrefHeight(SwingNode swingNode, int n) {
                swingNode.swingPrefHeight = n;
            }

            @Override
            public void setSwingMaxWidth(SwingNode swingNode, int n) {
                swingNode.swingMaxWidth = n;
            }

            @Override
            public void setSwingMaxHeight(SwingNode swingNode, int n) {
                swingNode.swingMaxHeight = n;
            }

            @Override
            public void setSwingMinWidth(SwingNode swingNode, int n) {
                swingNode.swingMinWidth = n;
            }

            @Override
            public void setSwingMinHeight(SwingNode swingNode, int n) {
                swingNode.swingMinHeight = n;
            }

            @Override
            public void setGrabbed(SwingNode swingNode, boolean bl) {
                swingNode.grabbed = bl;
            }
        });
    }

    private class SwingKeyEventHandler
    implements EventHandler<KeyEvent> {
        private SwingKeyEventHandler() {
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            String string;
            int n;
            Object object = SwingNode.this.swNodeIOP.getLightweightFrame();
            if (object == null) {
                return;
            }
            if (keyEvent.getCharacter().isEmpty()) {
                return;
            }
            if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.TAB) {
                keyEvent.consume();
            }
            if ((n = SwingEvents.fxKeyEventTypeToKeyID(keyEvent)) < 0) {
                return;
            }
            int n2 = SwingEvents.fxKeyModsToKeyMods(keyEvent);
            int n3 = keyEvent.getCode().getCode();
            char c = keyEvent.getCharacter().charAt(0);
            if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && (string = keyEvent.getText()).length() == 1) {
                c = string.charAt(0);
            }
            long l = System.currentTimeMillis();
            java.awt.event.KeyEvent keyEvent2 = SwingNode.this.swNodeIOP.createKeyEvent(object, n, l, n2, n3, c);
            AccessController.doPrivileged(new PostEventAction(keyEvent2));
        }
    }

    private class SwingScrollEventHandler
    implements EventHandler<ScrollEvent> {
        private SwingScrollEventHandler() {
        }

        @Override
        public void handle(ScrollEvent scrollEvent) {
            double d;
            boolean bl;
            Object object = SwingNode.this.swNodeIOP.getLightweightFrame();
            if (object == null) {
                return;
            }
            int n = SwingEvents.fxScrollModsToMouseWheelMods(scrollEvent);
            boolean bl2 = bl = (n & 0x40) != 0;
            if (!bl && scrollEvent.getDeltaY() != 0.0) {
                this.sendMouseWheelEvent(object, scrollEvent.getX(), scrollEvent.getY(), n, scrollEvent.getDeltaY() / scrollEvent.getMultiplierY());
            }
            double d2 = d = bl && scrollEvent.getDeltaY() != 0.0 ? scrollEvent.getDeltaY() / scrollEvent.getMultiplierY() : scrollEvent.getDeltaX() / scrollEvent.getMultiplierX();
            if (d != 0.0) {
                this.sendMouseWheelEvent(object, scrollEvent.getX(), scrollEvent.getY(), n |= 0x40, d);
            }
        }

        private void sendMouseWheelEvent(Object object, double d, double d2, int n, double d3) {
            int n2 = (int)d3;
            int n3 = (int)Math.signum(d3);
            if ((double)n3 * d3 < 1.0) {
                n2 = n3;
            }
            int n4 = (int)Math.round(d);
            int n5 = (int)Math.round(d2);
            MouseWheelEvent mouseWheelEvent = SwingNode.this.swNodeIOP.createMouseWheelEvent(object, n, n4, n5, -n2);
            AccessController.doPrivileged(new PostEventAction(mouseWheelEvent));
        }
    }

    private class SwingMouseEventHandler
    implements EventHandler<javafx.scene.input.MouseEvent> {
        private final Set<MouseButton> mouseClickedAllowed = new HashSet<MouseButton>();

        private SwingMouseEventHandler() {
        }

        @Override
        public void handle(javafx.scene.input.MouseEvent mouseEvent) {
            Object object = SwingNode.this.swNodeIOP.getLightweightFrame();
            if (object == null) {
                return;
            }
            int n = SwingEvents.fxMouseEventTypeToMouseID(mouseEvent);
            if (n < 0) {
                return;
            }
            mouseEvent.consume();
            EventType<? extends javafx.scene.input.MouseEvent> eventType = mouseEvent.getEventType();
            if (eventType == javafx.scene.input.MouseEvent.MOUSE_PRESSED) {
                this.mouseClickedAllowed.add(mouseEvent.getButton());
            } else if (eventType != javafx.scene.input.MouseEvent.MOUSE_RELEASED) {
                if (eventType == javafx.scene.input.MouseEvent.MOUSE_DRAGGED) {
                    this.mouseClickedAllowed.clear();
                } else if (eventType == javafx.scene.input.MouseEvent.MOUSE_CLICKED) {
                    if (mouseEvent.getClickCount() == 1 && !this.mouseClickedAllowed.contains((Object)mouseEvent.getButton())) {
                        return;
                    }
                    this.mouseClickedAllowed.remove((Object)mouseEvent.getButton());
                }
            }
            int n2 = SwingEvents.fxMouseModsToMouseMods(mouseEvent);
            boolean bl = mouseEvent.isPopupTrigger();
            int n3 = SwingEvents.fxMouseButtonToMouseButton(mouseEvent);
            long l = System.currentTimeMillis();
            int n4 = (int)Math.round(mouseEvent.getX());
            int n5 = (int)Math.round(mouseEvent.getY());
            int n6 = (int)Math.round(mouseEvent.getScreenX());
            int n7 = (int)Math.round(mouseEvent.getScreenY());
            MouseEvent mouseEvent2 = SwingNode.this.swNodeIOP.createMouseEvent(object, n, l, n2, n4, n5, n6, n7, mouseEvent.getClickCount(), bl, n3);
            AccessController.doPrivileged(new PostEventAction(mouseEvent2));
        }
    }

    private class PostEventAction
    implements PrivilegedAction<Void> {
        private AWTEvent event;

        PostEventAction(AWTEvent aWTEvent) {
            this.event = aWTEvent;
        }

        @Override
        public Void run() {
            EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eventQueue.postEvent(this.event);
            return null;
        }
    }

    private static class SwingNodeWindowFocusListener
    implements WindowFocusListener {
        private WeakReference<SwingNode> swingNodeRef;

        SwingNodeWindowFocusListener(SwingNode swingNode) {
            this.swingNodeRef = new WeakReference<SwingNode>(swingNode);
        }

        @Override
        public void windowGainedFocus(java.awt.event.WindowEvent windowEvent) {
            SwingNodeHelper.runOnFxThread(() -> {
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null) {
                    swingNode.requestFocus();
                }
            });
        }

        @Override
        public void windowLostFocus(java.awt.event.WindowEvent windowEvent) {
            SwingNodeHelper.runOnFxThread(() -> {
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null) {
                    swingNode.ungrabFocus(true);
                }
            });
        }
    }
}

