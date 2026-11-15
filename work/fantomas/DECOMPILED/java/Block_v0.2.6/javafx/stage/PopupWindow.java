/*
 * Decompiled with CFR 0.152.
 */
package javafx.stage;

import com.sun.javafx.FXPermissions;
import com.sun.javafx.event.DirectEvent;
import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.event.EventRedirector;
import com.sun.javafx.event.EventUtil;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.stage.FocusUngrabEvent;
import com.sun.javafx.stage.PopupWindowHelper;
import com.sun.javafx.stage.PopupWindowPeerListener;
import com.sun.javafx.stage.WindowCloseRequestHandler;
import com.sun.javafx.stage.WindowEventDispatcher;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public abstract class PopupWindow
extends Window {
    private final List<PopupWindow> children = new ArrayList<PopupWindow>();
    private final InvalidationListener popupWindowUpdater = new InvalidationListener(){

        @Override
        public void invalidated(Observable observable) {
            PopupWindow.this.cachedExtendedBounds = null;
            PopupWindow.this.cachedAnchorBounds = null;
            PopupWindow.this.updateWindow(PopupWindow.this.getAnchorX(), PopupWindow.this.getAnchorY());
        }
    };
    private ChangeListener<Boolean> changeListener = (observableValue, bl, bl2) -> {
        if (bl.booleanValue() && !bl2.booleanValue()) {
            this.hide();
        }
    };
    private WeakChangeListener<Boolean> weakOwnerNodeListener = new WeakChangeListener<Boolean>(this.changeListener);
    private ReadOnlyObjectWrapper<Window> ownerWindow = new ReadOnlyObjectWrapper(this, "ownerWindow");
    private ReadOnlyObjectWrapper<Node> ownerNode = new ReadOnlyObjectWrapper(this, "ownerNode");
    private BooleanProperty autoFix = new BooleanPropertyBase(true){

        @Override
        protected void invalidated() {
            PopupWindow.this.handleAutofixActivation(PopupWindow.this.isShowing(), this.get());
        }

        @Override
        public Object getBean() {
            return PopupWindow.this;
        }

        @Override
        public String getName() {
            return "autoFix";
        }
    };
    private BooleanProperty autoHide = new BooleanPropertyBase(){

        @Override
        protected void invalidated() {
            PopupWindow.this.handleAutohideActivation(PopupWindow.this.isShowing(), this.get());
        }

        @Override
        public Object getBean() {
            return PopupWindow.this;
        }

        @Override
        public String getName() {
            return "autoHide";
        }
    };
    private ObjectProperty<EventHandler<Event>> onAutoHide = new SimpleObjectProperty<EventHandler<Event>>(this, "onAutoHide");
    private BooleanProperty hideOnEscape = new SimpleBooleanProperty(this, "hideOnEscape", true);
    private BooleanProperty consumeAutoHidingEvents = new SimpleBooleanProperty(this, "consumeAutoHidingEvents", true);
    private Window rootWindow;
    private final ReadOnlyDoubleWrapper anchorX = new ReadOnlyDoubleWrapper(this, "anchorX", Double.NaN);
    private final ReadOnlyDoubleWrapper anchorY = new ReadOnlyDoubleWrapper(this, "anchorY", Double.NaN);
    private final ObjectProperty<AnchorLocation> anchorLocation = new ObjectPropertyBase<AnchorLocation>(AnchorLocation.WINDOW_TOP_LEFT){

        @Override
        protected void invalidated() {
            PopupWindow.this.cachedAnchorBounds = null;
            PopupWindow.this.updateWindow(PopupWindow.this.windowToAnchorX(PopupWindow.this.getX()), PopupWindow.this.windowToAnchorY(PopupWindow.this.getY()));
        }

        @Override
        public Object getBean() {
            return PopupWindow.this;
        }

        @Override
        public String getName() {
            return "anchorLocation";
        }
    };
    private Bounds cachedExtendedBounds;
    private Bounds cachedAnchorBounds;
    private ChangeListener<Boolean> ownerFocusedListener;
    private boolean autofixActive;
    private boolean autohideActive;

    public PopupWindow() {
        Pane pane = new Pane();
        pane.setBackground(Background.EMPTY);
        pane.getStyleClass().add("popup");
        final Scene scene = SceneHelper.createPopupScene(pane);
        scene.setFill(null);
        super.setScene(scene);
        pane.layoutBoundsProperty().addListener(this.popupWindowUpdater);
        pane.boundsInLocalProperty().addListener(this.popupWindowUpdater);
        scene.rootProperty().addListener(new InvalidationListener(){
            private Node oldRoot;
            {
                this.oldRoot = scene.getRoot();
            }

            @Override
            public void invalidated(Observable observable) {
                Parent parent = scene.getRoot();
                if (this.oldRoot != parent) {
                    if (this.oldRoot != null) {
                        this.oldRoot.layoutBoundsProperty().removeListener(PopupWindow.this.popupWindowUpdater);
                        this.oldRoot.boundsInLocalProperty().removeListener(PopupWindow.this.popupWindowUpdater);
                        this.oldRoot.getStyleClass().remove("popup");
                    }
                    if (parent != null) {
                        parent.layoutBoundsProperty().addListener(PopupWindow.this.popupWindowUpdater);
                        parent.boundsInLocalProperty().addListener(PopupWindow.this.popupWindowUpdater);
                        parent.getStyleClass().add("popup");
                    }
                    this.oldRoot = parent;
                    PopupWindow.this.cachedExtendedBounds = null;
                    PopupWindow.this.cachedAnchorBounds = null;
                    PopupWindow.this.updateWindow(PopupWindow.this.getAnchorX(), PopupWindow.this.getAnchorY());
                }
            }
        });
        PopupWindowHelper.initHelper(this);
    }

    ObservableList<Node> getContent() {
        Parent parent = this.getScene().getRoot();
        if (parent instanceof Group) {
            return ((Group)parent).getChildren();
        }
        if (parent instanceof Pane) {
            return ((Pane)parent).getChildren();
        }
        throw new IllegalStateException("The content of the Popup can't be accessed");
    }

    public final Window getOwnerWindow() {
        return (Window)this.ownerWindow.get();
    }

    public final ReadOnlyObjectProperty<Window> ownerWindowProperty() {
        return this.ownerWindow.getReadOnlyProperty();
    }

    public final Node getOwnerNode() {
        return (Node)this.ownerNode.get();
    }

    public final ReadOnlyObjectProperty<Node> ownerNodeProperty() {
        return this.ownerNode.getReadOnlyProperty();
    }

    @Override
    protected final void setScene(Scene scene) {
        throw new UnsupportedOperationException();
    }

    public final void setAutoFix(boolean bl) {
        this.autoFix.set(bl);
    }

    public final boolean isAutoFix() {
        return this.autoFix.get();
    }

    public final BooleanProperty autoFixProperty() {
        return this.autoFix;
    }

    public final void setAutoHide(boolean bl) {
        this.autoHide.set(bl);
    }

    public final boolean isAutoHide() {
        return this.autoHide.get();
    }

    public final BooleanProperty autoHideProperty() {
        return this.autoHide;
    }

    public final void setOnAutoHide(EventHandler<Event> eventHandler) {
        this.onAutoHide.set(eventHandler);
    }

    public final EventHandler<Event> getOnAutoHide() {
        return (EventHandler)this.onAutoHide.get();
    }

    public final ObjectProperty<EventHandler<Event>> onAutoHideProperty() {
        return this.onAutoHide;
    }

    public final void setHideOnEscape(boolean bl) {
        this.hideOnEscape.set(bl);
    }

    public final boolean isHideOnEscape() {
        return this.hideOnEscape.get();
    }

    public final BooleanProperty hideOnEscapeProperty() {
        return this.hideOnEscape;
    }

    public final void setConsumeAutoHidingEvents(boolean bl) {
        this.consumeAutoHidingEvents.set(bl);
    }

    public final boolean getConsumeAutoHidingEvents() {
        return this.consumeAutoHidingEvents.get();
    }

    public final BooleanProperty consumeAutoHidingEventsProperty() {
        return this.consumeAutoHidingEvents;
    }

    public void show(Window window) {
        this.validateOwnerWindow(window);
        this.showImpl(window);
    }

    public void show(Node node, double d, double d2) {
        if (node == null) {
            throw new NullPointerException("The owner node must not be null");
        }
        Scene scene = node.getScene();
        if (scene == null || scene.getWindow() == null) {
            throw new IllegalArgumentException("The owner node needs to be associated with a window");
        }
        Window window = scene.getWindow();
        this.validateOwnerWindow(window);
        this.ownerNode.set(node);
        if (node != null) {
            NodeHelper.treeShowingProperty(node).addListener(this.weakOwnerNodeListener);
        }
        this.updateWindow(d, d2);
        this.showImpl(window);
    }

    public void show(Window window, double d, double d2) {
        this.validateOwnerWindow(window);
        this.updateWindow(d, d2);
        this.showImpl(window);
    }

    private void showImpl(Window window) {
        this.ownerWindow.set(window);
        if (window instanceof PopupWindow) {
            ((PopupWindow)window).children.add(this);
        }
        if (window != null) {
            window.showingProperty().addListener(this.weakOwnerNodeListener);
        }
        Scene scene = this.getScene();
        SceneHelper.parentEffectiveOrientationInvalidated(scene);
        Scene scene2 = PopupWindow.getRootWindow(window).getScene();
        if (scene2 != null) {
            if (scene2.getUserAgentStylesheet() != null) {
                scene.setUserAgentStylesheet(scene2.getUserAgentStylesheet());
            }
            scene.getStylesheets().setAll((Collection<String>)scene2.getStylesheets());
            if (scene.getCursor() == null) {
                scene.setCursor(scene2.getCursor());
            }
        }
        if (PopupWindow.getRootWindow(window).isShowing()) {
            this.show();
        }
    }

    @Override
    public void hide() {
        for (PopupWindow popupWindow : this.children) {
            if (!popupWindow.isShowing()) continue;
            popupWindow.hide();
        }
        this.children.clear();
        super.hide();
        if (this.getOwnerWindow() != null) {
            this.getOwnerWindow().showingProperty().removeListener(this.weakOwnerNodeListener);
        }
        if (this.getOwnerNode() != null) {
            NodeHelper.treeShowingProperty(this.getOwnerNode()).removeListener(this.weakOwnerNodeListener);
        }
    }

    private void doVisibleChanging(boolean bl) {
        PerformanceTracker.logEvent("PopupWindow.storeVisible for [PopupWindow]");
        Toolkit toolkit = Toolkit.getToolkit();
        if (bl && this.getPeer() == null) {
            StageStyle stageStyle;
            try {
                SecurityManager securityManager = System.getSecurityManager();
                if (securityManager != null) {
                    securityManager.checkPermission(FXPermissions.CREATE_TRANSPARENT_WINDOW_PERMISSION);
                }
                stageStyle = StageStyle.TRANSPARENT;
            }
            catch (SecurityException securityException) {
                stageStyle = StageStyle.UNDECORATED;
            }
            this.setPeer(toolkit.createTKPopupStage(this, stageStyle, this.getOwnerWindow().getPeer(), this.acc));
            this.setPeerListener(new PopupWindowPeerListener(this));
        }
    }

    private void doVisibleChanged(boolean bl) {
        Window window = this.getOwnerWindow();
        if (bl) {
            this.rootWindow = PopupWindow.getRootWindow(window);
            this.startMonitorOwnerEvents(window);
            this.bindOwnerFocusedProperty(window);
            WindowHelper.setFocused(this, window.isFocused());
            this.handleAutofixActivation(true, this.isAutoFix());
            this.handleAutohideActivation(true, this.isAutoHide());
        } else {
            this.stopMonitorOwnerEvents(window);
            this.unbindOwnerFocusedProperty(window);
            WindowHelper.setFocused(this, false);
            this.handleAutofixActivation(false, this.isAutoFix());
            this.handleAutohideActivation(false, this.isAutoHide());
            this.rootWindow = null;
        }
        PerformanceTracker.logEvent("PopupWindow.storeVisible for [PopupWindow] finished");
    }

    public final void setAnchorX(double d) {
        this.updateWindow(d, this.getAnchorY());
    }

    public final double getAnchorX() {
        return this.anchorX.get();
    }

    public final ReadOnlyDoubleProperty anchorXProperty() {
        return this.anchorX.getReadOnlyProperty();
    }

    public final void setAnchorY(double d) {
        this.updateWindow(this.getAnchorX(), d);
    }

    public final double getAnchorY() {
        return this.anchorY.get();
    }

    public final ReadOnlyDoubleProperty anchorYProperty() {
        return this.anchorY.getReadOnlyProperty();
    }

    public final void setAnchorLocation(AnchorLocation anchorLocation) {
        this.anchorLocation.set(anchorLocation);
    }

    public final AnchorLocation getAnchorLocation() {
        return (AnchorLocation)((Object)this.anchorLocation.get());
    }

    public final ObjectProperty<AnchorLocation> anchorLocationProperty() {
        return this.anchorLocation;
    }

    @Override
    void setXInternal(double d) {
        this.updateWindow(this.windowToAnchorX(d), this.getAnchorY());
    }

    @Override
    void setYInternal(double d) {
        this.updateWindow(this.getAnchorX(), this.windowToAnchorY(d));
    }

    @Override
    void notifyLocationChanged(double d, double d2) {
        super.notifyLocationChanged(d, d2);
        this.anchorX.set(this.windowToAnchorX(d));
        this.anchorY.set(this.windowToAnchorY(d2));
    }

    private Bounds getExtendedBounds() {
        if (this.cachedExtendedBounds == null) {
            Parent parent = this.getScene().getRoot();
            this.cachedExtendedBounds = this.union(parent.getLayoutBounds(), parent.getBoundsInLocal());
        }
        return this.cachedExtendedBounds;
    }

    private Bounds getAnchorBounds() {
        if (this.cachedAnchorBounds == null) {
            this.cachedAnchorBounds = this.getAnchorLocation().isContentLocation() ? this.getScene().getRoot().getLayoutBounds() : this.getExtendedBounds();
        }
        return this.cachedAnchorBounds;
    }

    private void updateWindow(double d, double d2) {
        AnchorLocation anchorLocation = this.getAnchorLocation();
        Parent parent = this.getScene().getRoot();
        Bounds bounds = this.getExtendedBounds();
        Bounds bounds2 = this.getAnchorBounds();
        double d3 = anchorLocation.getXCoef();
        double d4 = anchorLocation.getYCoef();
        double d5 = d3 * bounds2.getWidth();
        double d6 = d4 * bounds2.getHeight();
        double d7 = d - d5;
        double d8 = d2 - d6;
        if (this.autofixActive) {
            Rectangle2D rectangle2D;
            Screen screen = Utils.getScreenForPoint(d, d2);
            Rectangle2D rectangle2D2 = rectangle2D = Utils.hasFullScreenStage(screen) ? screen.getBounds() : screen.getVisualBounds();
            if (d3 <= 0.5) {
                d7 = Math.min(d7, rectangle2D.getMaxX() - bounds2.getWidth());
                d7 = Math.max(d7, rectangle2D.getMinX());
            } else {
                d7 = Math.max(d7, rectangle2D.getMinX());
                d7 = Math.min(d7, rectangle2D.getMaxX() - bounds2.getWidth());
            }
            if (d4 <= 0.5) {
                d8 = Math.min(d8, rectangle2D.getMaxY() - bounds2.getHeight());
                d8 = Math.max(d8, rectangle2D.getMinY());
            } else {
                d8 = Math.max(d8, rectangle2D.getMinY());
                d8 = Math.min(d8, rectangle2D.getMaxY() - bounds2.getHeight());
            }
        }
        double d9 = d7 - bounds2.getMinX() + bounds.getMinX();
        double d10 = d8 - bounds2.getMinY() + bounds.getMinY();
        this.setWidth(bounds.getWidth());
        this.setHeight(bounds.getHeight());
        parent.setTranslateX(-bounds.getMinX());
        parent.setTranslateY(-bounds.getMinY());
        if (!Double.isNaN(d9)) {
            super.setXInternal(d9);
        }
        if (!Double.isNaN(d10)) {
            super.setYInternal(d10);
        }
        this.anchorX.set(d7 + d5);
        this.anchorY.set(d8 + d6);
    }

    private Bounds union(Bounds bounds, Bounds bounds2) {
        double d = Math.min(bounds.getMinX(), bounds2.getMinX());
        double d2 = Math.min(bounds.getMinY(), bounds2.getMinY());
        double d3 = Math.max(bounds.getMaxX(), bounds2.getMaxX());
        double d4 = Math.max(bounds.getMaxY(), bounds2.getMaxY());
        return new BoundingBox(d, d2, d3 - d, d4 - d2);
    }

    private double windowToAnchorX(double d) {
        Bounds bounds = this.getAnchorBounds();
        return d - this.getExtendedBounds().getMinX() + bounds.getMinX() + this.getAnchorLocation().getXCoef() * bounds.getWidth();
    }

    private double windowToAnchorY(double d) {
        Bounds bounds = this.getAnchorBounds();
        return d - this.getExtendedBounds().getMinY() + bounds.getMinY() + this.getAnchorLocation().getYCoef() * bounds.getHeight();
    }

    private static Window getRootWindow(Window window) {
        while (window instanceof PopupWindow) {
            window = ((PopupWindow)window).getOwnerWindow();
        }
        return window;
    }

    void doAutoHide() {
        this.hide();
        if (this.getOnAutoHide() != null) {
            this.getOnAutoHide().handle(new Event(this, this, Event.ANY));
        }
    }

    @Override
    WindowEventDispatcher createInternalEventDispatcher() {
        return new WindowEventDispatcher(new PopupEventRedirector(this), new WindowCloseRequestHandler(this), new EventHandlerManager(this));
    }

    @Override
    Window getWindowOwner() {
        return this.getOwnerWindow();
    }

    private void startMonitorOwnerEvents(Window window) {
        EventRedirector eventRedirector = window.getInternalEventDispatcher().getEventRedirector();
        eventRedirector.addEventDispatcher(this.getEventDispatcher());
    }

    private void stopMonitorOwnerEvents(Window window) {
        EventRedirector eventRedirector = window.getInternalEventDispatcher().getEventRedirector();
        eventRedirector.removeEventDispatcher(this.getEventDispatcher());
    }

    private void bindOwnerFocusedProperty(Window window) {
        this.ownerFocusedListener = (observableValue, bl, bl2) -> WindowHelper.setFocused(this, bl2);
        window.focusedProperty().addListener(this.ownerFocusedListener);
    }

    private void unbindOwnerFocusedProperty(Window window) {
        window.focusedProperty().removeListener(this.ownerFocusedListener);
        this.ownerFocusedListener = null;
    }

    private void handleAutofixActivation(boolean bl, boolean bl2) {
        boolean bl3;
        boolean bl4 = bl3 = bl && bl2;
        if (this.autofixActive != bl3) {
            this.autofixActive = bl3;
            if (bl3) {
                Screen.getScreens().addListener(this.popupWindowUpdater);
                this.updateWindow(this.getAnchorX(), this.getAnchorY());
            } else {
                Screen.getScreens().removeListener(this.popupWindowUpdater);
            }
        }
    }

    private void handleAutohideActivation(boolean bl, boolean bl2) {
        boolean bl3;
        boolean bl4 = bl3 = bl && bl2;
        if (this.autohideActive != bl3) {
            this.autohideActive = bl3;
            if (bl3) {
                this.rootWindow.increaseFocusGrabCounter();
            } else {
                this.rootWindow.decreaseFocusGrabCounter();
            }
        }
    }

    private void validateOwnerWindow(Window window) {
        if (window == null) {
            throw new NullPointerException("Owner window must not be null");
        }
        if (PopupWindow.wouldCreateCycle(window, this)) {
            throw new IllegalArgumentException("Specified owner window would create cycle in the window hierarchy");
        }
        if (this.isShowing() && this.getOwnerWindow() != window) {
            throw new IllegalStateException("Popup is already shown with different owner window");
        }
    }

    private static boolean wouldCreateCycle(Window window, Window window2) {
        while (window != null) {
            if (window == window2) {
                return true;
            }
            window = window.getWindowOwner();
        }
        return false;
    }

    static {
        PopupWindowHelper.setPopupWindowAccessor(new PopupWindowHelper.PopupWindowAccessor(){

            @Override
            public void doVisibleChanging(Window window, boolean bl) {
                ((PopupWindow)window).doVisibleChanging(bl);
            }

            @Override
            public void doVisibleChanged(Window window, boolean bl) {
                ((PopupWindow)window).doVisibleChanged(bl);
            }

            @Override
            public ObservableList<Node> getContent(PopupWindow popupWindow) {
                return popupWindow.getContent();
            }
        });
    }

    static class PopupEventRedirector
    extends EventRedirector {
        private static final KeyCombination ESCAPE_KEY_COMBINATION = KeyCombination.keyCombination("Esc");
        private final PopupWindow popupWindow;

        public PopupEventRedirector(PopupWindow popupWindow) {
            super(popupWindow);
            this.popupWindow = popupWindow;
        }

        @Override
        protected void handleRedirectedEvent(Object object, Event event) {
            if (event instanceof KeyEvent) {
                this.handleKeyEvent((KeyEvent)event);
                return;
            }
            EventType<? extends Event> eventType = event.getEventType();
            if (eventType == MouseEvent.MOUSE_PRESSED || eventType == ScrollEvent.SCROLL) {
                this.handleAutoHidingEvents(object, event);
                return;
            }
            if (eventType == FocusUngrabEvent.FOCUS_UNGRAB) {
                this.handleFocusUngrabEvent();
                return;
            }
        }

        private void handleKeyEvent(KeyEvent keyEvent) {
            if (keyEvent.isConsumed()) {
                return;
            }
            Scene scene = this.popupWindow.getScene();
            if (scene != null) {
                EventTarget eventTarget;
                Node node = scene.getFocusOwner();
                EventTarget eventTarget2 = eventTarget = node != null ? node : scene;
                if (EventUtil.fireEvent(eventTarget, new DirectEvent(keyEvent.copyFor(this.popupWindow, eventTarget))) == null) {
                    keyEvent.consume();
                    return;
                }
            }
            if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && ESCAPE_KEY_COMBINATION.match(keyEvent)) {
                this.handleEscapeKeyPressedEvent(keyEvent);
            }
        }

        private void handleEscapeKeyPressedEvent(Event event) {
            if (this.popupWindow.isHideOnEscape()) {
                this.popupWindow.doAutoHide();
                if (this.popupWindow.getConsumeAutoHidingEvents()) {
                    event.consume();
                }
            }
        }

        private void handleAutoHidingEvents(Object object, Event event) {
            if (this.popupWindow.getOwnerWindow() != object) {
                return;
            }
            if (this.popupWindow.isAutoHide() && !this.isOwnerNodeEvent(event)) {
                Event.fireEvent(this.popupWindow, new FocusUngrabEvent());
                this.popupWindow.doAutoHide();
                if (this.popupWindow.getConsumeAutoHidingEvents()) {
                    event.consume();
                }
            }
        }

        private void handleFocusUngrabEvent() {
            if (this.popupWindow.isAutoHide()) {
                this.popupWindow.doAutoHide();
            }
        }

        private boolean isOwnerNodeEvent(Event event) {
            Node node = this.popupWindow.getOwnerNode();
            if (node == null) {
                return false;
            }
            EventTarget eventTarget = event.getTarget();
            if (!(eventTarget instanceof Node)) {
                return false;
            }
            Node node2 = (Node)eventTarget;
            do {
                if (node2 != node) continue;
                return true;
            } while ((node2 = node2.getParent()) != null);
            return false;
        }
    }

    public static enum AnchorLocation {
        WINDOW_TOP_LEFT(0.0, 0.0, false),
        WINDOW_TOP_RIGHT(1.0, 0.0, false),
        WINDOW_BOTTOM_LEFT(0.0, 1.0, false),
        WINDOW_BOTTOM_RIGHT(1.0, 1.0, false),
        CONTENT_TOP_LEFT(0.0, 0.0, true),
        CONTENT_TOP_RIGHT(1.0, 0.0, true),
        CONTENT_BOTTOM_LEFT(0.0, 1.0, true),
        CONTENT_BOTTOM_RIGHT(1.0, 1.0, true);

        private final double xCoef;
        private final double yCoef;
        private final boolean contentLocation;

        private AnchorLocation(double d, double d2, boolean bl) {
            this.xCoef = d;
            this.yCoef = d2;
            this.contentLocation = bl;
        }

        double getXCoef() {
            return this.xCoef;
        }

        double getYCoef() {
            return this.yCoef;
        }

        boolean isContentLocation() {
            return this.contentLocation;
        }
    }
}

