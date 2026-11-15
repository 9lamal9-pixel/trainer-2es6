/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.web;

import com.sun.java.scene.web.WebViewHelper;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.web.NGWebView;
import com.sun.javafx.tk.TKPulseListener;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.webkit.InputMethodClientImpl;
import com.sun.javafx.webkit.KeyCodeMap;
import com.sun.webkit.WebPage;
import com.sun.webkit.event.WCFocusEvent;
import com.sun.webkit.event.WCInputMethodEvent;
import com.sun.webkit.event.WCKeyEvent;
import com.sun.webkit.event.WCMouseEvent;
import com.sun.webkit.event.WCMouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class WebView
extends Parent {
    private static final Map<Object, Integer> idMap = new HashMap<Object, Integer>();
    private static final boolean DEFAULT_CONTEXT_MENU_ENABLED = true;
    private static final FontSmoothingType DEFAULT_FONT_SMOOTHING_TYPE = FontSmoothingType.LCD;
    private static final double DEFAULT_ZOOM = 1.0;
    private static final double DEFAULT_FONT_SCALE = 1.0;
    private static final double DEFAULT_MIN_WIDTH = 0.0;
    private static final double DEFAULT_MIN_HEIGHT = 0.0;
    private static final double DEFAULT_PREF_WIDTH = 800.0;
    private static final double DEFAULT_PREF_HEIGHT = 600.0;
    private static final double DEFAULT_MAX_WIDTH = Double.MAX_VALUE;
    private static final double DEFAULT_MAX_HEIGHT = Double.MAX_VALUE;
    private final WebPage page;
    private final WebEngine engine;
    private volatile InputMethodClientImpl imClient;
    private final TKPulseListener stagePulseListener;
    private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper(this, "width");
    private final ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper(this, "height");
    private DoubleProperty zoom;
    private DoubleProperty fontScale;
    private DoubleProperty minWidth;
    private DoubleProperty minHeight;
    private DoubleProperty prefWidth;
    private DoubleProperty prefHeight;
    private DoubleProperty maxWidth;
    private DoubleProperty maxHeight;
    private ObjectProperty<FontSmoothingType> fontSmoothingType;
    private BooleanProperty contextMenuEnabled;
    private static final int WK_DND_ACTION_NONE = 0;
    private static final int WK_DND_ACTION_COPY = 1;
    private static final int WK_DND_ACTION_MOVE = 2;
    private static final int WK_DND_ACTION_LINK = 0x40000000;

    public final WebEngine getEngine() {
        return this.engine;
    }

    public final double getWidth() {
        return this.width.get();
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return this.width.getReadOnlyProperty();
    }

    public final double getHeight() {
        return this.height.get();
    }

    public ReadOnlyDoubleProperty heightProperty() {
        return this.height.getReadOnlyProperty();
    }

    public final void setZoom(double d) {
        WebEngine.checkThread();
        this.zoomProperty().set(d);
    }

    public final double getZoom() {
        return this.zoom != null ? this.zoom.get() : 1.0;
    }

    public final DoubleProperty zoomProperty() {
        if (this.zoom == null) {
            this.zoom = new StyleableDoubleProperty(1.0){

                @Override
                public void invalidated() {
                    Toolkit.getToolkit().checkFxUserThread();
                    WebView.this.page.setZoomFactor((float)this.get(), false);
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.ZOOM;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "zoom";
                }
            };
        }
        return this.zoom;
    }

    public final void setFontScale(double d) {
        WebEngine.checkThread();
        this.fontScaleProperty().set(d);
    }

    public final double getFontScale() {
        return this.fontScale != null ? this.fontScale.get() : 1.0;
    }

    public DoubleProperty fontScaleProperty() {
        if (this.fontScale == null) {
            this.fontScale = new StyleableDoubleProperty(1.0){

                @Override
                public void invalidated() {
                    Toolkit.getToolkit().checkFxUserThread();
                    WebView.this.page.setZoomFactor((float)this.get(), true);
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.FONT_SCALE;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "fontScale";
                }
            };
        }
        return this.fontScale;
    }

    public WebView() {
        WebViewHelper.initHelper(this);
        this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        this.getStyleClass().add("web-view");
        this.engine = new WebEngine();
        this.engine.setView(this);
        this.page = this.engine.getPage();
        this.page.setFontSmoothingType(DEFAULT_FONT_SMOOTHING_TYPE.ordinal());
        this.registerEventHandlers();
        this.stagePulseListener = () -> this.handleStagePulse();
        this.focusedProperty().addListener((observableValue, bl, bl2) -> {
            if (this.page != null) {
                WCFocusEvent wCFocusEvent = new WCFocusEvent(this.isFocused() ? 2 : 3, -1);
                this.page.dispatchFocusEvent(wCFocusEvent);
            }
        });
        this.setFocusTraversable(true);
        Toolkit.getToolkit().addStageTkPulseListener(this.stagePulseListener);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double d, double d2) {
        if (d != this.width.get() || d2 != this.height.get()) {
            this.width.set(d);
            this.height.set(d2);
            NodeHelper.markDirty(this, DirtyBits.NODE_GEOMETRY);
            NodeHelper.geomChanged(this);
        }
    }

    @Override
    public final double minWidth(double d) {
        double d2 = this.getMinWidth();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double minHeight(double d) {
        double d2 = this.getMinHeight();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double prefWidth(double d) {
        double d2 = this.getPrefWidth();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double prefHeight(double d) {
        double d2 = this.getPrefHeight();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double maxWidth(double d) {
        double d2 = this.getMaxWidth();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double maxHeight(double d) {
        double d2 = this.getMaxHeight();
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    public DoubleProperty minWidthProperty() {
        if (this.minWidth == null) {
            this.minWidth = new StyleableDoubleProperty(0.0){

                @Override
                public void invalidated() {
                    if (WebView.this.getParent() != null) {
                        WebView.this.getParent().requestLayout();
                    }
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.MIN_WIDTH;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "minWidth";
                }
            };
        }
        return this.minWidth;
    }

    public final void setMinWidth(double d) {
        this.minWidthProperty().set(d);
    }

    public final double getMinWidth() {
        return this.minWidth != null ? this.minWidth.get() : 0.0;
    }

    public DoubleProperty minHeightProperty() {
        if (this.minHeight == null) {
            this.minHeight = new StyleableDoubleProperty(0.0){

                @Override
                public void invalidated() {
                    if (WebView.this.getParent() != null) {
                        WebView.this.getParent().requestLayout();
                    }
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.MIN_HEIGHT;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "minHeight";
                }
            };
        }
        return this.minHeight;
    }

    public final void setMinHeight(double d) {
        this.minHeightProperty().set(d);
    }

    public final double getMinHeight() {
        return this.minHeight != null ? this.minHeight.get() : 0.0;
    }

    public void setMinSize(double d, double d2) {
        this.setMinWidth(d);
        this.setMinHeight(d2);
    }

    public DoubleProperty prefWidthProperty() {
        if (this.prefWidth == null) {
            this.prefWidth = new StyleableDoubleProperty(800.0){

                @Override
                public void invalidated() {
                    if (WebView.this.getParent() != null) {
                        WebView.this.getParent().requestLayout();
                    }
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.PREF_WIDTH;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "prefWidth";
                }
            };
        }
        return this.prefWidth;
    }

    public final void setPrefWidth(double d) {
        this.prefWidthProperty().set(d);
    }

    public final double getPrefWidth() {
        return this.prefWidth != null ? this.prefWidth.get() : 800.0;
    }

    public DoubleProperty prefHeightProperty() {
        if (this.prefHeight == null) {
            this.prefHeight = new StyleableDoubleProperty(600.0){

                @Override
                public void invalidated() {
                    if (WebView.this.getParent() != null) {
                        WebView.this.getParent().requestLayout();
                    }
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.PREF_HEIGHT;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "prefHeight";
                }
            };
        }
        return this.prefHeight;
    }

    public final void setPrefHeight(double d) {
        this.prefHeightProperty().set(d);
    }

    public final double getPrefHeight() {
        return this.prefHeight != null ? this.prefHeight.get() : 600.0;
    }

    public void setPrefSize(double d, double d2) {
        this.setPrefWidth(d);
        this.setPrefHeight(d2);
    }

    public DoubleProperty maxWidthProperty() {
        if (this.maxWidth == null) {
            this.maxWidth = new StyleableDoubleProperty(Double.MAX_VALUE){

                @Override
                public void invalidated() {
                    if (WebView.this.getParent() != null) {
                        WebView.this.getParent().requestLayout();
                    }
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.MAX_WIDTH;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "maxWidth";
                }
            };
        }
        return this.maxWidth;
    }

    public final void setMaxWidth(double d) {
        this.maxWidthProperty().set(d);
    }

    public final double getMaxWidth() {
        return this.maxWidth != null ? this.maxWidth.get() : Double.MAX_VALUE;
    }

    public DoubleProperty maxHeightProperty() {
        if (this.maxHeight == null) {
            this.maxHeight = new StyleableDoubleProperty(Double.MAX_VALUE){

                @Override
                public void invalidated() {
                    if (WebView.this.getParent() != null) {
                        WebView.this.getParent().requestLayout();
                    }
                }

                @Override
                public CssMetaData<WebView, Number> getCssMetaData() {
                    return StyleableProperties.MAX_HEIGHT;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "maxHeight";
                }
            };
        }
        return this.maxHeight;
    }

    public final void setMaxHeight(double d) {
        this.maxHeightProperty().set(d);
    }

    public final double getMaxHeight() {
        return this.maxHeight != null ? this.maxHeight.get() : Double.MAX_VALUE;
    }

    public void setMaxSize(double d, double d2) {
        this.setMaxWidth(d);
        this.setMaxHeight(d2);
    }

    public final void setFontSmoothingType(FontSmoothingType fontSmoothingType) {
        this.fontSmoothingTypeProperty().set(fontSmoothingType);
    }

    public final FontSmoothingType getFontSmoothingType() {
        return this.fontSmoothingType != null ? (FontSmoothingType)((Object)this.fontSmoothingType.get()) : DEFAULT_FONT_SMOOTHING_TYPE;
    }

    public final ObjectProperty<FontSmoothingType> fontSmoothingTypeProperty() {
        if (this.fontSmoothingType == null) {
            this.fontSmoothingType = new StyleableObjectProperty<FontSmoothingType>(DEFAULT_FONT_SMOOTHING_TYPE){

                @Override
                public void invalidated() {
                    Toolkit.getToolkit().checkFxUserThread();
                    WebView.this.page.setFontSmoothingType(((FontSmoothingType)((Object)this.get())).ordinal());
                }

                @Override
                public CssMetaData<WebView, FontSmoothingType> getCssMetaData() {
                    return StyleableProperties.FONT_SMOOTHING_TYPE;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "fontSmoothingType";
                }
            };
        }
        return this.fontSmoothingType;
    }

    public final void setContextMenuEnabled(boolean bl) {
        this.contextMenuEnabledProperty().set(bl);
    }

    public final boolean isContextMenuEnabled() {
        return this.contextMenuEnabled == null ? true : this.contextMenuEnabled.get();
    }

    public final BooleanProperty contextMenuEnabledProperty() {
        if (this.contextMenuEnabled == null) {
            this.contextMenuEnabled = new StyleableBooleanProperty(true){

                @Override
                public void invalidated() {
                    Toolkit.getToolkit().checkFxUserThread();
                    WebView.this.page.setContextMenuEnabled(this.get());
                }

                @Override
                public CssMetaData<WebView, Boolean> getCssMetaData() {
                    return StyleableProperties.CONTEXT_MENU_ENABLED;
                }

                @Override
                public Object getBean() {
                    return WebView.this;
                }

                @Override
                public String getName() {
                    return "contextMenuEnabled";
                }
            };
        }
        return this.contextMenuEnabled;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return WebView.getClassCssMetaData();
    }

    private boolean isTreeReallyVisible() {
        if (this.getScene() == null) {
            return false;
        }
        Window window = this.getScene().getWindow();
        if (window == null) {
            return false;
        }
        boolean bl = window instanceof Stage ? ((Stage)window).isIconified() : false;
        return NodeHelper.isTreeVisible(this) && window.isShowing() && window.getWidth() > 0.0 && window.getHeight() > 0.0 && !bl;
    }

    private void handleStagePulse() {
        if (this.page == null) {
            return;
        }
        boolean bl = this.isTreeReallyVisible();
        if (bl) {
            if (this.page.isDirty()) {
                SceneHelper.setAllowPGAccess(true);
                NGWebView nGWebView = (NGWebView)NodeHelper.getPeer(this);
                nGWebView.update();
                if (this.page.isRepaintPending()) {
                    NodeHelper.markDirty(this, DirtyBits.WEBVIEW_VIEW);
                }
                SceneHelper.setAllowPGAccess(false);
            }
        } else {
            this.page.dropRenderFrames();
        }
    }

    private void processMouseEvent(MouseEvent mouseEvent) {
        Object object;
        if (this.page == null) {
            return;
        }
        EventType<? extends MouseEvent> eventType = mouseEvent.getEventType();
        double d = mouseEvent.getX();
        double d2 = mouseEvent.getY();
        double d3 = mouseEvent.getScreenX();
        double d4 = mouseEvent.getScreenY();
        if (eventType == MouseEvent.MOUSE_EXITED) {
            eventType = MouseEvent.MOUSE_MOVED;
            d = -32768.0;
            d2 = -32768.0;
            object = this.localToScreen(d, d2);
            if (object == null) {
                return;
            }
            d3 = ((Point2D)object).getX();
            d4 = ((Point2D)object).getY();
        }
        if ((object = idMap.get(eventType)) == null) {
            return;
        }
        WCMouseEvent wCMouseEvent = new WCMouseEvent((Integer)object, idMap.get((Object)mouseEvent.getButton()), mouseEvent.getClickCount(), (int)d, (int)d2, (int)d3, (int)d4, System.currentTimeMillis(), mouseEvent.isShiftDown(), mouseEvent.isControlDown(), mouseEvent.isAltDown(), mouseEvent.isMetaDown(), mouseEvent.isPopupTrigger());
        this.page.dispatchMouseEvent(wCMouseEvent);
        mouseEvent.consume();
    }

    private void processScrollEvent(ScrollEvent scrollEvent) {
        if (this.page == null) {
            return;
        }
        double d = -scrollEvent.getDeltaX() * this.getFontScale() * this.getScaleX();
        double d2 = -scrollEvent.getDeltaY() * this.getFontScale() * this.getScaleY();
        WCMouseWheelEvent wCMouseWheelEvent = new WCMouseWheelEvent((int)scrollEvent.getX(), (int)scrollEvent.getY(), (int)scrollEvent.getScreenX(), (int)scrollEvent.getScreenY(), System.currentTimeMillis(), scrollEvent.isShiftDown(), scrollEvent.isControlDown(), scrollEvent.isAltDown(), scrollEvent.isMetaDown(), (float)d, (float)d2);
        this.page.dispatchMouseWheelEvent(wCMouseWheelEvent);
        scrollEvent.consume();
    }

    private void processKeyEvent(KeyEvent keyEvent) {
        Object object;
        if (this.page == null) {
            return;
        }
        String string = null;
        String string2 = null;
        int n = 0;
        if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
            string = keyEvent.getCharacter();
        } else {
            object = KeyCodeMap.lookup(keyEvent.getCode());
            string2 = ((KeyCodeMap.Entry)object).getKeyIdentifier();
            n = ((KeyCodeMap.Entry)object).getWindowsVirtualKeyCode();
        }
        object = new WCKeyEvent(idMap.get(keyEvent.getEventType()), string, string2, n, keyEvent.isShiftDown(), keyEvent.isControlDown(), keyEvent.isAltDown(), keyEvent.isMetaDown(), System.currentTimeMillis());
        if (this.page.dispatchKeyEvent((WCKeyEvent)object)) {
            keyEvent.consume();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private InputMethodClientImpl getInputMethodClient() {
        if (this.imClient == null) {
            WebView webView = this;
            synchronized (webView) {
                if (this.imClient == null) {
                    this.imClient = new InputMethodClientImpl(this, this.page);
                }
            }
        }
        return this.imClient;
    }

    private void processInputMethodEvent(InputMethodEvent inputMethodEvent) {
        if (this.page == null) {
            return;
        }
        if (!this.getInputMethodClient().getInputMethodState()) {
            inputMethodEvent.consume();
            return;
        }
        WCInputMethodEvent wCInputMethodEvent = InputMethodClientImpl.convertToWCInputMethodEvent(inputMethodEvent);
        if (this.page.dispatchInputMethodEvent(wCInputMethodEvent)) {
            inputMethodEvent.consume();
            return;
        }
    }

    private static int getWKDndEventType(EventType eventType) {
        int n = 0;
        if (eventType == DragEvent.DRAG_ENTERED) {
            n = 0;
        } else if (eventType == DragEvent.DRAG_EXITED) {
            n = 3;
        } else if (eventType == DragEvent.DRAG_OVER) {
            n = 1;
        } else if (eventType == DragEvent.DRAG_DROPPED) {
            n = 4;
        }
        return n;
    }

    private static int getWKDndAction(TransferMode ... transferModeArray) {
        int n = 0;
        for (TransferMode transferMode : transferModeArray) {
            if (transferMode == TransferMode.COPY) {
                n |= 1;
                continue;
            }
            if (transferMode == TransferMode.MOVE) {
                n |= 2;
                continue;
            }
            if (transferMode != TransferMode.LINK) continue;
            n |= 0x40000000;
        }
        return n;
    }

    private static TransferMode[] getFXDndAction(int n) {
        LinkedList<TransferMode> linkedList = new LinkedList<TransferMode>();
        if ((n & 1) != 0) {
            linkedList.add(TransferMode.COPY);
        }
        if ((n & 2) != 0) {
            linkedList.add(TransferMode.MOVE);
        }
        if ((n & 0x40000000) != 0) {
            linkedList.add(TransferMode.LINK);
        }
        return linkedList.toArray(new TransferMode[0]);
    }

    private void registerEventHandlers() {
        this.addEventHandler(KeyEvent.ANY, keyEvent -> this.processKeyEvent((KeyEvent)keyEvent));
        this.addEventHandler(MouseEvent.ANY, mouseEvent -> {
            this.processMouseEvent((MouseEvent)mouseEvent);
            if (mouseEvent.isDragDetect() && !this.page.isDragConfirmed()) {
                mouseEvent.setDragDetect(false);
            }
        });
        this.addEventHandler(ScrollEvent.SCROLL, scrollEvent -> this.processScrollEvent((ScrollEvent)scrollEvent));
        this.setOnInputMethodTextChanged(inputMethodEvent -> this.processInputMethodEvent((InputMethodEvent)inputMethodEvent));
        EventHandler<DragEvent> eventHandler = dragEvent -> {
            try {
                Dragboard dragboard = dragEvent.getDragboard();
                LinkedList<String> linkedList = new LinkedList<String>();
                LinkedList<String> linkedList2 = new LinkedList<String>();
                for (DataFormat dataFormat : dragboard.getContentTypes()) {
                    Object object = dragboard.getContent(dataFormat);
                    if (object == null) continue;
                    for (String string : dataFormat.getIdentifiers()) {
                        linkedList.add(string);
                        linkedList2.add(object.toString());
                    }
                }
                if (!linkedList.isEmpty()) {
                    int n = WebView.getWKDndEventType(dragEvent.getEventType());
                    int n2 = this.page.dispatchDragOperation(n, linkedList.toArray(new String[0]), linkedList2.toArray(new String[0]), (int)dragEvent.getX(), (int)dragEvent.getY(), (int)dragEvent.getScreenX(), (int)dragEvent.getScreenY(), WebView.getWKDndAction(dragboard.getTransferModes().toArray(new TransferMode[0])));
                    if (n != 4 || n2 != 0) {
                        dragEvent.acceptTransferModes(WebView.getFXDndAction(n2));
                    }
                    dragEvent.consume();
                }
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
        };
        this.setOnDragEntered(eventHandler);
        this.setOnDragExited(eventHandler);
        this.setOnDragOver(eventHandler);
        this.setOnDragDropped(eventHandler);
        this.setOnDragDetected(mouseEvent -> {
            if (this.page.isDragConfirmed()) {
                this.page.confirmStartDrag();
                mouseEvent.consume();
            }
        });
        this.setOnDragDone(dragEvent -> {
            this.page.dispatchDragOperation(104, null, null, (int)dragEvent.getX(), (int)dragEvent.getY(), (int)dragEvent.getScreenX(), (int)dragEvent.getScreenY(), WebView.getWKDndAction(dragEvent.getAcceptedTransferMode()));
            dragEvent.consume();
        });
        this.setInputMethodRequests(this.getInputMethodClient());
    }

    private void doPickNodeLocal(PickRay pickRay, PickResultChooser pickResultChooser) {
        NodeHelper.intersects(this, pickRay, pickResultChooser);
    }

    @Override
    protected ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    private NGNode doCreatePeer() {
        return new NGWebView();
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        baseBounds.deriveWithNewBounds(0.0f, 0.0f, 0.0f, (float)this.getWidth(), (float)this.getHeight(), 0.0f);
        baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private void doTransformsChanged() {
    }

    private boolean doComputeContains(double d, double d2) {
        return true;
    }

    private void doUpdatePeer() {
        NGWebView nGWebView = (NGWebView)NodeHelper.getPeer(this);
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            nGWebView.setPage(this.page);
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            nGWebView.resize((float)this.getWidth(), (float)this.getHeight());
        }
        if (NodeHelper.isDirty(this, DirtyBits.WEBVIEW_VIEW)) {
            nGWebView.requestRender();
        }
    }

    static /* synthetic */ FontSmoothingType access$1200() {
        return DEFAULT_FONT_SMOOTHING_TYPE;
    }

    static {
        WebViewHelper.setWebViewAccessor(new WebViewHelper.WebViewAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((WebView)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((WebView)node).doUpdatePeer();
            }

            @Override
            public void doTransformsChanged(Node node) {
                ((WebView)node).doTransformsChanged();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((WebView)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((WebView)node).doComputeContains(d, d2);
            }

            @Override
            public void doPickNodeLocal(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                ((WebView)node).doPickNodeLocal(pickRay, pickResultChooser);
            }
        });
        idMap.put((Object)MouseButton.NONE, 0);
        idMap.put((Object)MouseButton.PRIMARY, 1);
        idMap.put((Object)MouseButton.MIDDLE, 2);
        idMap.put((Object)MouseButton.SECONDARY, 4);
        idMap.put(MouseEvent.MOUSE_PRESSED, 0);
        idMap.put(MouseEvent.MOUSE_RELEASED, 1);
        idMap.put(MouseEvent.MOUSE_MOVED, 2);
        idMap.put(MouseEvent.MOUSE_DRAGGED, 3);
        idMap.put(KeyEvent.KEY_PRESSED, 1);
        idMap.put(KeyEvent.KEY_RELEASED, 2);
        idMap.put(KeyEvent.KEY_TYPED, 0);
    }

    private static final class StyleableProperties {
        private static final CssMetaData<WebView, Boolean> CONTEXT_MENU_ENABLED = new CssMetaData<WebView, Boolean>("-fx-context-menu-enabled", BooleanConverter.getInstance(), Boolean.valueOf(true)){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.contextMenuEnabled == null || !webView.contextMenuEnabled.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.contextMenuEnabledProperty());
            }
        };
        private static final CssMetaData<WebView, FontSmoothingType> FONT_SMOOTHING_TYPE = new CssMetaData<WebView, FontSmoothingType>("-fx-font-smoothing-type", new EnumConverter<FontSmoothingType>(FontSmoothingType.class), WebView.access$1200()){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.fontSmoothingType == null || !webView.fontSmoothingType.isBound();
            }

            @Override
            public StyleableProperty<FontSmoothingType> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.fontSmoothingTypeProperty());
            }
        };
        private static final CssMetaData<WebView, Number> ZOOM = new CssMetaData<WebView, Number>("-fx-zoom", SizeConverter.getInstance(), (Number)1.0){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.zoom == null || !webView.zoom.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.zoomProperty());
            }
        };
        private static final CssMetaData<WebView, Number> FONT_SCALE = new CssMetaData<WebView, Number>("-fx-font-scale", SizeConverter.getInstance(), (Number)1.0){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.fontScale == null || !webView.fontScale.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.fontScaleProperty());
            }
        };
        private static final CssMetaData<WebView, Number> MIN_WIDTH = new CssMetaData<WebView, Number>("-fx-min-width", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.minWidth == null || !webView.minWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.minWidthProperty());
            }
        };
        private static final CssMetaData<WebView, Number> MIN_HEIGHT = new CssMetaData<WebView, Number>("-fx-min-height", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.minHeight == null || !webView.minHeight.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.minHeightProperty());
            }
        };
        private static final CssMetaData<WebView, Number> MAX_WIDTH = new CssMetaData<WebView, Number>("-fx-max-width", SizeConverter.getInstance(), (Number)Double.MAX_VALUE){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.maxWidth == null || !webView.maxWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.maxWidthProperty());
            }
        };
        private static final CssMetaData<WebView, Number> MAX_HEIGHT = new CssMetaData<WebView, Number>("-fx-max-height", SizeConverter.getInstance(), (Number)Double.MAX_VALUE){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.maxHeight == null || !webView.maxHeight.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.maxHeightProperty());
            }
        };
        private static final CssMetaData<WebView, Number> PREF_WIDTH = new CssMetaData<WebView, Number>("-fx-pref-width", SizeConverter.getInstance(), (Number)800.0){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.prefWidth == null || !webView.prefWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.prefWidthProperty());
            }
        };
        private static final CssMetaData<WebView, Number> PREF_HEIGHT = new CssMetaData<WebView, Number>("-fx-pref-height", SizeConverter.getInstance(), (Number)600.0){

            @Override
            public boolean isSettable(WebView webView) {
                return webView.prefHeight == null || !webView.prefHeight.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(WebView webView) {
                return (StyleableProperty)((Object)webView.prefHeightProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Parent.getClassCssMetaData());
            arrayList.add(CONTEXT_MENU_ENABLED);
            arrayList.add(FONT_SMOOTHING_TYPE);
            arrayList.add(ZOOM);
            arrayList.add(FONT_SCALE);
            arrayList.add(MIN_WIDTH);
            arrayList.add(PREF_WIDTH);
            arrayList.add(MAX_WIDTH);
            arrayList.add(MIN_HEIGHT);
            arrayList.add(PREF_HEIGHT);
            arrayList.add(MAX_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

