/*
 * Decompiled with CFR 0.152.
 */
package javafx.stage;

import com.sun.javafx.FXPermissions;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.stage.WindowEventDispatcher;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.stage.WindowPeerListener;
import com.sun.javafx.tk.TKPulseListener;
import com.sun.javafx.tk.TKScene;
import com.sun.javafx.tk.TKStage;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Utils;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.HashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;

public class Window
implements EventTarget {
    private static ObservableList<Window> windows = FXCollections.observableArrayList();
    private static ObservableList<Window> unmodifiableWindows = FXCollections.unmodifiableObservableList(windows);
    private WindowHelper windowHelper = null;
    final AccessControlContext acc = AccessController.getContext();
    private WindowPeerListener peerListener;
    private TKStage peer;
    private TKBoundsConfigurator peerBoundsConfigurator = new TKBoundsConfigurator();
    private boolean sizeToScene = false;
    private static final float CENTER_ON_SCREEN_X_FRACTION = 0.5f;
    private static final float CENTER_ON_SCREEN_Y_FRACTION = 0.33333334f;
    private ReadOnlyDoubleWrapper outputScaleX = new ReadOnlyDoubleWrapper(this, "outputScaleX", 1.0);
    private ReadOnlyDoubleWrapper outputScaleY = new ReadOnlyDoubleWrapper(this, "outputScaleY", 1.0);
    private BooleanProperty forceIntegerRenderScale = new SimpleBooleanProperty(this, "forceIntegerRenderScale", false){

        @Override
        protected void invalidated() {
            Window.this.updateRenderScales(Window.this.getOutputScaleX(), Window.this.getOutputScaleY());
        }
    };
    private DoubleProperty renderScaleX = new SimpleDoubleProperty(this, "renderScaleX", 1.0){

        @Override
        protected void invalidated() {
            Window.this.peerBoundsConfigurator.setRenderScaleX(this.get());
        }
    };
    private DoubleProperty renderScaleY = new SimpleDoubleProperty(this, "renderScaleY", 1.0){

        @Override
        protected void invalidated() {
            Window.this.peerBoundsConfigurator.setRenderScaleY(this.get());
        }
    };
    private boolean xExplicit = false;
    private ReadOnlyDoubleWrapper x = new ReadOnlyDoubleWrapper(this, "x", Double.NaN);
    private boolean yExplicit = false;
    private ReadOnlyDoubleWrapper y = new ReadOnlyDoubleWrapper(this, "y", Double.NaN);
    private boolean widthExplicit = false;
    private ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper(this, "width", Double.NaN);
    private boolean heightExplicit = false;
    private ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper(this, "height", Double.NaN);
    private ReadOnlyBooleanWrapper focused = new ReadOnlyBooleanWrapper(){

        @Override
        protected void invalidated() {
            Window.this.focusChanged(this.get());
        }

        @Override
        public Object getBean() {
            return Window.this;
        }

        @Override
        public String getName() {
            return "focused";
        }
    };
    private static final Object USER_DATA_KEY;
    private ObservableMap<Object, Object> properties;
    private SceneModel scene = new SceneModel();
    private DoubleProperty opacity;
    private ObjectProperty<EventHandler<WindowEvent>> onCloseRequest;
    private ObjectProperty<EventHandler<WindowEvent>> onShowing;
    private ObjectProperty<EventHandler<WindowEvent>> onShown;
    private ObjectProperty<EventHandler<WindowEvent>> onHiding;
    private ObjectProperty<EventHandler<WindowEvent>> onHidden;
    private ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(){
        private boolean oldVisible;

        @Override
        protected void invalidated() {
            boolean bl = this.get();
            if (this.oldVisible == bl) {
                return;
            }
            if (!this.oldVisible && bl) {
                Window.this.fireEvent(new WindowEvent(Window.this, WindowEvent.WINDOW_SHOWING));
            } else {
                Window.this.fireEvent(new WindowEvent(Window.this, WindowEvent.WINDOW_HIDING));
            }
            this.oldVisible = bl;
            WindowHelper.visibleChanging(Window.this, bl);
            if (bl) {
                Window.this.hasBeenVisible = true;
                windows.add(Window.this);
            } else {
                windows.remove(Window.this);
            }
            Toolkit toolkit = Toolkit.getToolkit();
            if (Window.this.peer != null) {
                if (bl) {
                    if (Window.this.peerListener == null) {
                        Window.this.peerListener = new WindowPeerListener(Window.this);
                    }
                    Window.this.peer.setTKStageListener(Window.this.peerListener);
                    toolkit.addStageTkPulseListener(Window.this.peerBoundsConfigurator);
                    if (Window.this.getScene() != null) {
                        SceneHelper.initPeer(Window.this.getScene());
                        Window.this.peer.setScene(SceneHelper.getPeer(Window.this.getScene()));
                        SceneHelper.preferredSize(Window.this.getScene());
                    }
                    Window.this.updateOutputScales(Window.this.peer.getOutputScaleX(), Window.this.peer.getOutputScaleY());
                    Window.this.peerBoundsConfigurator.setRenderScaleX(Window.this.getRenderScaleX());
                    Window.this.peerBoundsConfigurator.setRenderScaleY(Window.this.getRenderScaleY());
                    if (!(Window.this.getScene() == null || Window.this.widthExplicit && Window.this.heightExplicit)) {
                        Window.this.adjustSize(true);
                    } else {
                        Window.this.peerBoundsConfigurator.setSize(Window.this.getWidth(), Window.this.getHeight(), -1.0, -1.0);
                    }
                    if (!Window.this.xExplicit && !Window.this.yExplicit) {
                        Window.this.centerOnScreen();
                    } else {
                        Window.this.peerBoundsConfigurator.setLocation(Window.this.getX(), Window.this.getY(), 0.0f, 0.0f);
                    }
                    Window.this.applyBounds();
                    Window.this.peer.setOpacity((float)Window.this.getOpacity());
                    Window.this.peer.setVisible(true);
                    Window.this.fireEvent(new WindowEvent(Window.this, WindowEvent.WINDOW_SHOWN));
                } else {
                    Window.this.peer.setVisible(false);
                    Window.this.fireEvent(new WindowEvent(Window.this, WindowEvent.WINDOW_HIDDEN));
                    if (Window.this.getScene() != null) {
                        Window.this.peer.setScene(null);
                        SceneHelper.disposePeer(Window.this.getScene());
                        StyleManager.getInstance().forget(Window.this.getScene());
                    }
                    toolkit.removeStageTkPulseListener(Window.this.peerBoundsConfigurator);
                    Window.this.peer.setTKStageListener(null);
                    Window.this.peer.close();
                }
            }
            if (bl) {
                toolkit.requestNextPulse();
            }
            WindowHelper.visibleChanged(Window.this, bl);
            if (Window.this.sizeToScene) {
                if (bl) {
                    Window.this.sizeToScene();
                }
                Window.this.sizeToScene = false;
            }
        }

        @Override
        public Object getBean() {
            return Window.this;
        }

        @Override
        public String getName() {
            return "showing";
        }
    };
    boolean hasBeenVisible = false;
    private ObjectProperty<EventDispatcher> eventDispatcher;
    private WindowEventDispatcher internalEventDispatcher;
    private int focusGrabCounter;
    private final ReadOnlyObjectWrapper<Screen> screen = new ReadOnlyObjectWrapper<Screen>(Screen.getPrimary());

    public static ObservableList<Window> getWindows() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(FXPermissions.ACCESS_WINDOW_LIST_PERMISSION);
        }
        return unmodifiableWindows;
    }

    protected Window() {
        this.initializeInternalEventDispatcher();
        WindowHelper.initHelper(this);
    }

    WindowPeerListener getPeerListener() {
        return this.peerListener;
    }

    void setPeerListener(WindowPeerListener windowPeerListener) {
        this.peerListener = windowPeerListener;
    }

    TKStage getPeer() {
        return this.peer;
    }

    void setPeer(TKStage tKStage) {
        this.peer = tKStage;
    }

    public void sizeToScene() {
        if (this.getScene() != null && this.peer != null) {
            SceneHelper.preferredSize(this.getScene());
            this.adjustSize(false);
        } else {
            this.sizeToScene = true;
        }
    }

    private void adjustSize(boolean bl) {
        if (this.getScene() == null) {
            return;
        }
        if (this.peer != null) {
            double d = this.getScene().getWidth();
            double d2 = d > 0.0 ? d : -1.0;
            double d3 = -1.0;
            if (bl && this.widthExplicit) {
                d3 = this.getWidth();
            } else if (d2 <= 0.0) {
                d3 = this.widthExplicit ? this.getWidth() : -1.0;
            } else {
                this.widthExplicit = false;
            }
            double d4 = this.getScene().getHeight();
            double d5 = d4 > 0.0 ? d4 : -1.0;
            double d6 = -1.0;
            if (bl && this.heightExplicit) {
                d6 = this.getHeight();
            } else if (d5 <= 0.0) {
                d6 = this.heightExplicit ? this.getHeight() : -1.0;
            } else {
                this.heightExplicit = false;
            }
            this.peerBoundsConfigurator.setSize(d3, d6, d2, d5);
            this.applyBounds();
        }
    }

    public void centerOnScreen() {
        this.xExplicit = false;
        this.yExplicit = false;
        if (this.peer != null) {
            Rectangle2D rectangle2D = this.getWindowScreen().getVisualBounds();
            double d = rectangle2D.getMinX() + (rectangle2D.getWidth() - this.getWidth()) * 0.5;
            double d2 = rectangle2D.getMinY() + (rectangle2D.getHeight() - this.getHeight()) * 0.3333333432674408;
            this.x.set(d);
            this.y.set(d2);
            this.peerBoundsConfigurator.setLocation(d, d2, 0.5f, 0.33333334f);
            this.applyBounds();
        }
    }

    private void updateOutputScales(double d, double d2) {
        this.updateRenderScales(d, d2);
        this.outputScaleX.set(d);
        this.outputScaleY.set(d2);
    }

    void updateRenderScales(double d, double d2) {
        boolean bl = this.forceIntegerRenderScale.get();
        if (!this.renderScaleX.isBound()) {
            this.renderScaleX.set(bl ? Math.ceil(d) : d);
        }
        if (!this.renderScaleY.isBound()) {
            this.renderScaleY.set(bl ? Math.ceil(d2) : d2);
        }
    }

    public final double getOutputScaleX() {
        return this.outputScaleX.get();
    }

    public final ReadOnlyDoubleProperty outputScaleXProperty() {
        return this.outputScaleX.getReadOnlyProperty();
    }

    public final double getOutputScaleY() {
        return this.outputScaleY.get();
    }

    public final ReadOnlyDoubleProperty outputScaleYProperty() {
        return this.outputScaleY.getReadOnlyProperty();
    }

    public final void setForceIntegerRenderScale(boolean bl) {
        this.forceIntegerRenderScale.set(bl);
    }

    public final boolean isForceIntegerRenderScale() {
        return this.forceIntegerRenderScale.get();
    }

    public final BooleanProperty forceIntegerRenderScaleProperty() {
        return this.forceIntegerRenderScale;
    }

    public final void setRenderScaleX(double d) {
        this.renderScaleX.set(d);
    }

    public final double getRenderScaleX() {
        return this.renderScaleX.get();
    }

    public final DoubleProperty renderScaleXProperty() {
        return this.renderScaleX;
    }

    public final void setRenderScaleY(double d) {
        this.renderScaleY.set(d);
    }

    public final double getRenderScaleY() {
        return this.renderScaleY.get();
    }

    public final DoubleProperty renderScaleYProperty() {
        return this.renderScaleY;
    }

    public final void setX(double d) {
        this.setXInternal(d);
    }

    public final double getX() {
        return this.x.get();
    }

    public final ReadOnlyDoubleProperty xProperty() {
        return this.x.getReadOnlyProperty();
    }

    void setXInternal(double d) {
        this.x.set(d);
        this.peerBoundsConfigurator.setX(d, 0.0f);
        this.xExplicit = true;
    }

    public final void setY(double d) {
        this.setYInternal(d);
    }

    public final double getY() {
        return this.y.get();
    }

    public final ReadOnlyDoubleProperty yProperty() {
        return this.y.getReadOnlyProperty();
    }

    void setYInternal(double d) {
        this.y.set(d);
        this.peerBoundsConfigurator.setY(d, 0.0f);
        this.yExplicit = true;
    }

    void notifyLocationChanged(double d, double d2) {
        this.x.set(d);
        this.y.set(d2);
    }

    public final void setWidth(double d) {
        this.width.set(d);
        this.peerBoundsConfigurator.setWindowWidth(d);
        this.widthExplicit = true;
    }

    public final double getWidth() {
        return this.width.get();
    }

    public final ReadOnlyDoubleProperty widthProperty() {
        return this.width.getReadOnlyProperty();
    }

    public final void setHeight(double d) {
        this.height.set(d);
        this.peerBoundsConfigurator.setWindowHeight(d);
        this.heightExplicit = true;
    }

    public final double getHeight() {
        return this.height.get();
    }

    public final ReadOnlyDoubleProperty heightProperty() {
        return this.height.getReadOnlyProperty();
    }

    void notifySizeChanged(double d, double d2) {
        this.width.set(d);
        this.height.set(d2);
    }

    final void setFocused(boolean bl) {
        this.focused.set(bl);
    }

    public final void requestFocus() {
        if (this.peer != null) {
            this.peer.requestFocus();
        }
    }

    public final boolean isFocused() {
        return this.focused.get();
    }

    public final ReadOnlyBooleanProperty focusedProperty() {
        return this.focused.getReadOnlyProperty();
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

    protected void setScene(Scene scene) {
        this.scene.set(scene);
    }

    public final Scene getScene() {
        return (Scene)this.scene.get();
    }

    public final ReadOnlyObjectProperty<Scene> sceneProperty() {
        return this.scene.getReadOnlyProperty();
    }

    public final void setOpacity(double d) {
        this.opacityProperty().set(d);
    }

    public final double getOpacity() {
        return this.opacity == null ? 1.0 : this.opacity.get();
    }

    public final DoubleProperty opacityProperty() {
        if (this.opacity == null) {
            this.opacity = new DoublePropertyBase(1.0){

                @Override
                protected void invalidated() {
                    if (Window.this.peer != null) {
                        Window.this.peer.setOpacity((float)this.get());
                    }
                }

                @Override
                public Object getBean() {
                    return Window.this;
                }

                @Override
                public String getName() {
                    return "opacity";
                }
            };
        }
        return this.opacity;
    }

    public final void setOnCloseRequest(EventHandler<WindowEvent> eventHandler) {
        this.onCloseRequestProperty().set(eventHandler);
    }

    public final EventHandler<WindowEvent> getOnCloseRequest() {
        return this.onCloseRequest != null ? (EventHandler)this.onCloseRequest.get() : null;
    }

    public final ObjectProperty<EventHandler<WindowEvent>> onCloseRequestProperty() {
        if (this.onCloseRequest == null) {
            this.onCloseRequest = new ObjectPropertyBase<EventHandler<WindowEvent>>(){

                @Override
                protected void invalidated() {
                    Window.this.setEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Window.this;
                }

                @Override
                public String getName() {
                    return "onCloseRequest";
                }
            };
        }
        return this.onCloseRequest;
    }

    public final void setOnShowing(EventHandler<WindowEvent> eventHandler) {
        this.onShowingProperty().set(eventHandler);
    }

    public final EventHandler<WindowEvent> getOnShowing() {
        return this.onShowing == null ? null : (EventHandler)this.onShowing.get();
    }

    public final ObjectProperty<EventHandler<WindowEvent>> onShowingProperty() {
        if (this.onShowing == null) {
            this.onShowing = new ObjectPropertyBase<EventHandler<WindowEvent>>(){

                @Override
                protected void invalidated() {
                    Window.this.setEventHandler(WindowEvent.WINDOW_SHOWING, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Window.this;
                }

                @Override
                public String getName() {
                    return "onShowing";
                }
            };
        }
        return this.onShowing;
    }

    public final void setOnShown(EventHandler<WindowEvent> eventHandler) {
        this.onShownProperty().set(eventHandler);
    }

    public final EventHandler<WindowEvent> getOnShown() {
        return this.onShown == null ? null : (EventHandler)this.onShown.get();
    }

    public final ObjectProperty<EventHandler<WindowEvent>> onShownProperty() {
        if (this.onShown == null) {
            this.onShown = new ObjectPropertyBase<EventHandler<WindowEvent>>(){

                @Override
                protected void invalidated() {
                    Window.this.setEventHandler(WindowEvent.WINDOW_SHOWN, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Window.this;
                }

                @Override
                public String getName() {
                    return "onShown";
                }
            };
        }
        return this.onShown;
    }

    public final void setOnHiding(EventHandler<WindowEvent> eventHandler) {
        this.onHidingProperty().set(eventHandler);
    }

    public final EventHandler<WindowEvent> getOnHiding() {
        return this.onHiding == null ? null : (EventHandler)this.onHiding.get();
    }

    public final ObjectProperty<EventHandler<WindowEvent>> onHidingProperty() {
        if (this.onHiding == null) {
            this.onHiding = new ObjectPropertyBase<EventHandler<WindowEvent>>(){

                @Override
                protected void invalidated() {
                    Window.this.setEventHandler(WindowEvent.WINDOW_HIDING, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Window.this;
                }

                @Override
                public String getName() {
                    return "onHiding";
                }
            };
        }
        return this.onHiding;
    }

    public final void setOnHidden(EventHandler<WindowEvent> eventHandler) {
        this.onHiddenProperty().set(eventHandler);
    }

    public final EventHandler<WindowEvent> getOnHidden() {
        return this.onHidden == null ? null : (EventHandler)this.onHidden.get();
    }

    public final ObjectProperty<EventHandler<WindowEvent>> onHiddenProperty() {
        if (this.onHidden == null) {
            this.onHidden = new ObjectPropertyBase<EventHandler<WindowEvent>>(){

                @Override
                protected void invalidated() {
                    Window.this.setEventHandler(WindowEvent.WINDOW_HIDDEN, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Window.this;
                }

                @Override
                public String getName() {
                    return "onHidden";
                }
            };
        }
        return this.onHidden;
    }

    private void setShowing(boolean bl) {
        Toolkit.getToolkit().checkFxUserThread();
        this.showing.set(bl);
    }

    public final boolean isShowing() {
        return this.showing.get();
    }

    public final ReadOnlyBooleanProperty showingProperty() {
        return this.showing.getReadOnlyProperty();
    }

    protected void show() {
        this.setShowing(true);
    }

    public void hide() {
        this.setShowing(false);
    }

    private void doVisibleChanging(boolean bl) {
        if (bl && this.getScene() != null) {
            NodeHelper.reapplyCSS(this.getScene().getRoot());
        }
    }

    private void doVisibleChanged(boolean bl) {
        assert (this.peer != null);
        if (!bl) {
            this.peerListener = null;
            this.peer = null;
        }
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

    WindowEventDispatcher getInternalEventDispatcher() {
        this.initializeInternalEventDispatcher();
        return this.internalEventDispatcher;
    }

    private void initializeInternalEventDispatcher() {
        if (this.internalEventDispatcher == null) {
            this.internalEventDispatcher = this.createInternalEventDispatcher();
            this.eventDispatcher = new SimpleObjectProperty<WindowEventDispatcher>(this, "eventDispatcher", this.internalEventDispatcher);
        }
    }

    WindowEventDispatcher createInternalEventDispatcher() {
        return new WindowEventDispatcher(this);
    }

    public final void fireEvent(Event event) {
        Event.fireEvent(this, event);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        EventDispatcher eventDispatcher;
        if (this.eventDispatcher != null && (eventDispatcher = (EventDispatcher)this.eventDispatcher.get()) != null) {
            eventDispatchChain = eventDispatchChain.prepend(eventDispatcher);
        }
        return eventDispatchChain;
    }

    void increaseFocusGrabCounter() {
        if (++this.focusGrabCounter == 1 && this.peer != null && this.isFocused()) {
            this.peer.grabFocus();
        }
    }

    void decreaseFocusGrabCounter() {
        if (--this.focusGrabCounter == 0 && this.peer != null) {
            this.peer.ungrabFocus();
        }
    }

    private void focusChanged(boolean bl) {
        if (this.focusGrabCounter > 0 && this.peer != null && bl) {
            this.peer.grabFocus();
        }
    }

    final void applyBounds() {
        this.peerBoundsConfigurator.apply();
    }

    Window getWindowOwner() {
        return null;
    }

    private Screen getWindowScreen() {
        Window window = this;
        do {
            if (Double.isNaN(window.getX()) || Double.isNaN(window.getY()) || Double.isNaN(window.getWidth()) || Double.isNaN(window.getHeight())) continue;
            return Utils.getScreenForRectangle(new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight()));
        } while ((window = window.getWindowOwner()) != null);
        return Screen.getPrimary();
    }

    private ReadOnlyObjectProperty<Screen> screenProperty() {
        return this.screen.getReadOnlyProperty();
    }

    private void notifyScreenChanged(Object object, Object object2) {
        this.screen.set(Screen.getScreenForNative(object2));
    }

    static {
        WindowHelper.setWindowAccessor(new WindowHelper.WindowAccessor(){

            @Override
            public WindowHelper getHelper(Window window) {
                return window.windowHelper;
            }

            @Override
            public void setHelper(Window window, WindowHelper windowHelper) {
                window.windowHelper = windowHelper;
            }

            @Override
            public void doVisibleChanging(Window window, boolean bl) {
                window.doVisibleChanging(bl);
            }

            @Override
            public void doVisibleChanged(Window window, boolean bl) {
                window.doVisibleChanged(bl);
            }

            @Override
            public TKStage getPeer(Window window) {
                return window.getPeer();
            }

            @Override
            public void setPeer(Window window, TKStage tKStage) {
                window.setPeer(tKStage);
            }

            @Override
            public WindowPeerListener getPeerListener(Window window) {
                return window.getPeerListener();
            }

            @Override
            public void setPeerListener(Window window, WindowPeerListener windowPeerListener) {
                window.setPeerListener(windowPeerListener);
            }

            @Override
            public void setFocused(Window window, boolean bl) {
                window.setFocused(bl);
            }

            @Override
            public void notifyLocationChanged(Window window, double d, double d2) {
                window.notifyLocationChanged(d, d2);
            }

            @Override
            public void notifySizeChanged(Window window, double d, double d2) {
                window.notifySizeChanged(d, d2);
            }

            @Override
            public void notifyScaleChanged(Window window, double d, double d2) {
                window.updateOutputScales(d, d2);
            }

            @Override
            public void notifyScreenChanged(Window window, Object object, Object object2) {
                window.notifyScreenChanged(object, object2);
            }

            @Override
            public float getPlatformScaleX(Window window) {
                TKStage tKStage = window.getPeer();
                return tKStage == null ? 1.0f : tKStage.getPlatformScaleX();
            }

            @Override
            public float getPlatformScaleY(Window window) {
                TKStage tKStage = window.getPeer();
                return tKStage == null ? 1.0f : tKStage.getPlatformScaleY();
            }

            @Override
            public ReadOnlyObjectProperty<Screen> screenProperty(Window window) {
                return window.screenProperty();
            }

            @Override
            public AccessControlContext getAccessControlContext(Window window) {
                return window.acc;
            }
        });
        USER_DATA_KEY = new Object();
    }

    private final class TKBoundsConfigurator
    implements TKPulseListener {
        private double renderScaleX;
        private double renderScaleY;
        private double x;
        private double y;
        private float xGravity;
        private float yGravity;
        private double windowWidth;
        private double windowHeight;
        private double clientWidth;
        private double clientHeight;
        private boolean dirty;

        public TKBoundsConfigurator() {
            this.reset();
        }

        public void setRenderScaleX(double d) {
            this.renderScaleX = d;
            this.setDirty();
        }

        public void setRenderScaleY(double d) {
            this.renderScaleY = d;
            this.setDirty();
        }

        public void setX(double d, float f) {
            this.x = d;
            this.xGravity = f;
            this.setDirty();
        }

        public void setY(double d, float f) {
            this.y = d;
            this.yGravity = f;
            this.setDirty();
        }

        public void setWindowWidth(double d) {
            this.windowWidth = d;
            this.setDirty();
        }

        public void setWindowHeight(double d) {
            this.windowHeight = d;
            this.setDirty();
        }

        public void setClientWidth(double d) {
            this.clientWidth = d;
            this.setDirty();
        }

        public void setClientHeight(double d) {
            this.clientHeight = d;
            this.setDirty();
        }

        public void setLocation(double d, double d2, float f, float f2) {
            this.x = d;
            this.y = d2;
            this.xGravity = f;
            this.yGravity = f2;
            this.setDirty();
        }

        public void setSize(double d, double d2, double d3, double d4) {
            this.windowWidth = d;
            this.windowHeight = d2;
            this.clientWidth = d3;
            this.clientHeight = d4;
            this.setDirty();
        }

        public void apply() {
            if (this.dirty) {
                boolean bl = !Double.isNaN(this.x);
                float f = bl ? (float)this.x : 0.0f;
                boolean bl2 = !Double.isNaN(this.y);
                float f2 = bl2 ? (float)this.y : 0.0f;
                float f3 = (float)this.windowWidth;
                float f4 = (float)this.windowHeight;
                float f5 = (float)this.clientWidth;
                float f6 = (float)this.clientHeight;
                float f7 = this.xGravity;
                float f8 = this.yGravity;
                float f9 = (float)this.renderScaleX;
                float f10 = (float)this.renderScaleY;
                this.reset();
                Window.this.peer.setBounds(f, f2, bl, bl2, f3, f4, f5, f6, f7, f8, f9, f10);
            }
        }

        @Override
        public void pulse() {
            this.apply();
        }

        private void reset() {
            this.renderScaleX = 0.0;
            this.renderScaleY = 0.0;
            this.x = Double.NaN;
            this.y = Double.NaN;
            this.xGravity = 0.0f;
            this.yGravity = 0.0f;
            this.windowWidth = -1.0;
            this.windowHeight = -1.0;
            this.clientWidth = -1.0;
            this.clientHeight = -1.0;
            this.dirty = false;
        }

        private void setDirty() {
            if (!this.dirty) {
                Toolkit.getToolkit().requestNextPulse();
                this.dirty = true;
            }
        }
    }

    private final class SceneModel
    extends ReadOnlyObjectWrapper<Scene> {
        private Scene oldScene;

        private SceneModel() {
        }

        @Override
        protected void invalidated() {
            Scene scene = (Scene)this.get();
            if (this.oldScene == scene) {
                return;
            }
            if (Window.this.peer != null) {
                Toolkit.getToolkit().checkFxUserThread();
            }
            this.updatePeerScene(null);
            if (this.oldScene != null) {
                SceneHelper.setWindow(this.oldScene, null);
                StyleManager.getInstance().forget(this.oldScene);
            }
            if (scene != null) {
                Window window = scene.getWindow();
                if (window != null) {
                    window.setScene(null);
                }
                SceneHelper.setWindow(scene, Window.this);
                this.updatePeerScene(SceneHelper.getPeer(scene));
                if (Window.this.isShowing()) {
                    NodeHelper.reapplyCSS(scene.getRoot());
                    if (!Window.this.widthExplicit || !Window.this.heightExplicit) {
                        SceneHelper.preferredSize(Window.this.getScene());
                        Window.this.adjustSize(true);
                    }
                }
            }
            this.oldScene = scene;
        }

        @Override
        public Object getBean() {
            return Window.this;
        }

        @Override
        public String getName() {
            return "scene";
        }

        private void updatePeerScene(TKScene tKScene) {
            if (Window.this.peer != null) {
                Window.this.peer.setScene(tKScene);
            }
        }
    }
}

