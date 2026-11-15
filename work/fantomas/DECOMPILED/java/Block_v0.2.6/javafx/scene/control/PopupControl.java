/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.Logging;
import com.sun.javafx.scene.layout.PaneHelper;
import com.sun.javafx.stage.PopupWindowHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.css.CssMetaData;
import javafx.css.CssParser;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.StringConverter;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

public class PopupControl
extends PopupWindow
implements Skinnable,
Styleable {
    public static final double USE_PREF_SIZE = Double.NEGATIVE_INFINITY;
    public static final double USE_COMPUTED_SIZE = -1.0;
    protected CSSBridge bridge;
    private final ObjectProperty<Skin<?>> skin = new ObjectPropertyBase<Skin<?>>(){
        private Skin<?> oldValue;

        @Override
        public void set(Skin<?> skin) {
            if (skin == null ? this.oldValue == null : this.oldValue != null && skin.getClass().equals(this.oldValue.getClass())) {
                return;
            }
            super.set(skin);
        }

        @Override
        protected void invalidated() {
            Skin skin = (Skin)this.get();
            PopupControl.this.currentSkinClassName = skin == null ? null : skin.getClass().getName();
            PopupControl.this.skinClassNameProperty().set(PopupControl.this.currentSkinClassName);
            if (this.oldValue != null) {
                this.oldValue.dispose();
            }
            this.oldValue = (Skin)this.getValue();
            PopupControl.this.prefWidthCache = -1.0;
            PopupControl.this.prefHeightCache = -1.0;
            PopupControl.this.minWidthCache = -1.0;
            PopupControl.this.minHeightCache = -1.0;
            PopupControl.this.maxWidthCache = -1.0;
            PopupControl.this.maxHeightCache = -1.0;
            PopupControl.this.skinSizeComputed = false;
            Node node = PopupControl.this.getSkinNode();
            if (node != null) {
                PopupControl.this.bridge.getChildren().setAll((Node[])new Node[]{node});
            } else {
                PopupControl.this.bridge.getChildren().clear();
            }
            NodeHelper.reapplyCSS(PopupControl.this.bridge);
            PlatformLogger platformLogger = Logging.getControlsLogger();
            if (platformLogger.isLoggable(PlatformLogger.Level.FINEST)) {
                platformLogger.finest("Stored skin[" + this.getValue() + "] on " + this);
            }
        }

        @Override
        public Object getBean() {
            return PopupControl.this;
        }

        @Override
        public String getName() {
            return "skin";
        }
    };
    private String currentSkinClassName = null;
    private StringProperty skinClassName = null;
    private DoubleProperty minWidth;
    private DoubleProperty minHeight;
    private DoubleProperty prefWidth;
    private DoubleProperty prefHeight;
    private DoubleProperty maxWidth;
    private DoubleProperty maxHeight;
    private double prefWidthCache = -1.0;
    private double prefHeightCache = -1.0;
    private double minWidthCache = -1.0;
    private double minHeightCache = -1.0;
    private double maxWidthCache = -1.0;
    private double maxHeightCache = -1.0;
    private boolean skinSizeComputed = false;
    private static final CssMetaData<CSSBridge, String> SKIN;
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    public PopupControl() {
        this.bridge = new CSSBridge();
        this.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_LEFT);
        PopupWindowHelper.getContent(this).add(this.bridge);
    }

    public final StringProperty idProperty() {
        return this.bridge.idProperty();
    }

    public final void setId(String string) {
        this.idProperty().set(string);
    }

    @Override
    public final String getId() {
        return (String)this.idProperty().get();
    }

    @Override
    public final ObservableList<String> getStyleClass() {
        return this.bridge.getStyleClass();
    }

    public final void setStyle(String string) {
        this.styleProperty().set(string);
    }

    @Override
    public final String getStyle() {
        return (String)this.styleProperty().get();
    }

    public final StringProperty styleProperty() {
        return this.bridge.styleProperty();
    }

    @Override
    public final ObjectProperty<Skin<?>> skinProperty() {
        return this.skin;
    }

    @Override
    public final void setSkin(Skin<?> skin) {
        this.skinProperty().setValue(skin);
    }

    @Override
    public final Skin<?> getSkin() {
        return (Skin)this.skinProperty().getValue();
    }

    private StringProperty skinClassNameProperty() {
        if (this.skinClassName == null) {
            this.skinClassName = new StyleableStringProperty(){

                @Override
                public void set(String string) {
                    if (string == null || string.isEmpty() || string.equals(this.get())) {
                        return;
                    }
                    super.set(string);
                }

                @Override
                public void invalidated() {
                    if (this.get() != null && !this.get().equals(PopupControl.this.currentSkinClassName)) {
                        Control.loadSkinClass(PopupControl.this, this.get());
                    }
                }

                @Override
                public Object getBean() {
                    return PopupControl.this;
                }

                @Override
                public String getName() {
                    return "skinClassName";
                }

                @Override
                public CssMetaData<CSSBridge, String> getCssMetaData() {
                    return SKIN;
                }
            };
        }
        return this.skinClassName;
    }

    private Node getSkinNode() {
        return this.getSkin() == null ? null : this.getSkin().getNode();
    }

    public final void setMinWidth(double d) {
        this.minWidthProperty().set(d);
    }

    public final double getMinWidth() {
        return this.minWidth == null ? -1.0 : this.minWidth.get();
    }

    public final DoubleProperty minWidthProperty() {
        if (this.minWidth == null) {
            this.minWidth = new DoublePropertyBase(-1.0){

                @Override
                public void invalidated() {
                    if (PopupControl.this.isShowing()) {
                        PopupControl.this.bridge.requestLayout();
                    }
                }

                @Override
                public Object getBean() {
                    return PopupControl.this;
                }

                @Override
                public String getName() {
                    return "minWidth";
                }
            };
        }
        return this.minWidth;
    }

    public final void setMinHeight(double d) {
        this.minHeightProperty().set(d);
    }

    public final double getMinHeight() {
        return this.minHeight == null ? -1.0 : this.minHeight.get();
    }

    public final DoubleProperty minHeightProperty() {
        if (this.minHeight == null) {
            this.minHeight = new DoublePropertyBase(-1.0){

                @Override
                public void invalidated() {
                    if (PopupControl.this.isShowing()) {
                        PopupControl.this.bridge.requestLayout();
                    }
                }

                @Override
                public Object getBean() {
                    return PopupControl.this;
                }

                @Override
                public String getName() {
                    return "minHeight";
                }
            };
        }
        return this.minHeight;
    }

    public void setMinSize(double d, double d2) {
        this.setMinWidth(d);
        this.setMinHeight(d2);
    }

    public final void setPrefWidth(double d) {
        this.prefWidthProperty().set(d);
    }

    public final double getPrefWidth() {
        return this.prefWidth == null ? -1.0 : this.prefWidth.get();
    }

    public final DoubleProperty prefWidthProperty() {
        if (this.prefWidth == null) {
            this.prefWidth = new DoublePropertyBase(-1.0){

                @Override
                public void invalidated() {
                    if (PopupControl.this.isShowing()) {
                        PopupControl.this.bridge.requestLayout();
                    }
                }

                @Override
                public Object getBean() {
                    return PopupControl.this;
                }

                @Override
                public String getName() {
                    return "prefWidth";
                }
            };
        }
        return this.prefWidth;
    }

    public final void setPrefHeight(double d) {
        this.prefHeightProperty().set(d);
    }

    public final double getPrefHeight() {
        return this.prefHeight == null ? -1.0 : this.prefHeight.get();
    }

    public final DoubleProperty prefHeightProperty() {
        if (this.prefHeight == null) {
            this.prefHeight = new DoublePropertyBase(-1.0){

                @Override
                public void invalidated() {
                    if (PopupControl.this.isShowing()) {
                        PopupControl.this.bridge.requestLayout();
                    }
                }

                @Override
                public Object getBean() {
                    return PopupControl.this;
                }

                @Override
                public String getName() {
                    return "prefHeight";
                }
            };
        }
        return this.prefHeight;
    }

    public void setPrefSize(double d, double d2) {
        this.setPrefWidth(d);
        this.setPrefHeight(d2);
    }

    public final void setMaxWidth(double d) {
        this.maxWidthProperty().set(d);
    }

    public final double getMaxWidth() {
        return this.maxWidth == null ? -1.0 : this.maxWidth.get();
    }

    public final DoubleProperty maxWidthProperty() {
        if (this.maxWidth == null) {
            this.maxWidth = new DoublePropertyBase(-1.0){

                @Override
                public void invalidated() {
                    if (PopupControl.this.isShowing()) {
                        PopupControl.this.bridge.requestLayout();
                    }
                }

                @Override
                public Object getBean() {
                    return PopupControl.this;
                }

                @Override
                public String getName() {
                    return "maxWidth";
                }
            };
        }
        return this.maxWidth;
    }

    public final void setMaxHeight(double d) {
        this.maxHeightProperty().set(d);
    }

    public final double getMaxHeight() {
        return this.maxHeight == null ? -1.0 : this.maxHeight.get();
    }

    public final DoubleProperty maxHeightProperty() {
        if (this.maxHeight == null) {
            this.maxHeight = new DoublePropertyBase(-1.0){

                @Override
                public void invalidated() {
                    if (PopupControl.this.isShowing()) {
                        PopupControl.this.bridge.requestLayout();
                    }
                }

                @Override
                public Object getBean() {
                    return PopupControl.this;
                }

                @Override
                public String getName() {
                    return "maxHeight";
                }
            };
        }
        return this.maxHeight;
    }

    public void setMaxSize(double d, double d2) {
        this.setMaxWidth(d);
        this.setMaxHeight(d2);
    }

    public final double minWidth(double d) {
        double d2 = this.getMinWidth();
        if (d2 == -1.0) {
            if (this.minWidthCache == -1.0) {
                this.minWidthCache = this.recalculateMinWidth(d);
            }
            return this.minWidthCache;
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefWidth(d);
        }
        return d2;
    }

    public final double minHeight(double d) {
        double d2 = this.getMinHeight();
        if (d2 == -1.0) {
            if (this.minHeightCache == -1.0) {
                this.minHeightCache = this.recalculateMinHeight(d);
            }
            return this.minHeightCache;
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefHeight(d);
        }
        return d2;
    }

    public final double prefWidth(double d) {
        double d2 = this.getPrefWidth();
        if (d2 == -1.0) {
            if (this.prefWidthCache == -1.0) {
                this.prefWidthCache = this.recalculatePrefWidth(d);
            }
            return this.prefWidthCache;
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefWidth(d);
        }
        return d2;
    }

    public final double prefHeight(double d) {
        double d2 = this.getPrefHeight();
        if (d2 == -1.0) {
            if (this.prefHeightCache == -1.0) {
                this.prefHeightCache = this.recalculatePrefHeight(d);
            }
            return this.prefHeightCache;
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefHeight(d);
        }
        return d2;
    }

    public final double maxWidth(double d) {
        double d2 = this.getMaxWidth();
        if (d2 == -1.0) {
            if (this.maxWidthCache == -1.0) {
                this.maxWidthCache = this.recalculateMaxWidth(d);
            }
            return this.maxWidthCache;
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefWidth(d);
        }
        return d2;
    }

    public final double maxHeight(double d) {
        double d2 = this.getMaxHeight();
        if (d2 == -1.0) {
            if (this.maxHeightCache == -1.0) {
                this.maxHeightCache = this.recalculateMaxHeight(d);
            }
            return this.maxHeightCache;
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefHeight(d);
        }
        return d2;
    }

    private double recalculateMinWidth(double d) {
        this.recomputeSkinSize();
        return this.getSkinNode() == null ? 0.0 : this.getSkinNode().minWidth(d);
    }

    private double recalculateMinHeight(double d) {
        this.recomputeSkinSize();
        return this.getSkinNode() == null ? 0.0 : this.getSkinNode().minHeight(d);
    }

    private double recalculateMaxWidth(double d) {
        this.recomputeSkinSize();
        return this.getSkinNode() == null ? 0.0 : this.getSkinNode().maxWidth(d);
    }

    private double recalculateMaxHeight(double d) {
        this.recomputeSkinSize();
        return this.getSkinNode() == null ? 0.0 : this.getSkinNode().maxHeight(d);
    }

    private double recalculatePrefWidth(double d) {
        this.recomputeSkinSize();
        return this.getSkinNode() == null ? 0.0 : this.getSkinNode().prefWidth(d);
    }

    private double recalculatePrefHeight(double d) {
        this.recomputeSkinSize();
        return this.getSkinNode() == null ? 0.0 : this.getSkinNode().prefHeight(d);
    }

    private void recomputeSkinSize() {
        if (!this.skinSizeComputed) {
            this.bridge.applyCss();
            this.skinSizeComputed = true;
        }
    }

    protected Skin<?> createDefaultSkin() {
        return null;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return PopupControl.getClassCssMetaData();
    }

    public final void pseudoClassStateChanged(PseudoClass pseudoClass, boolean bl) {
        this.bridge.pseudoClassStateChanged(pseudoClass, bl);
    }

    @Override
    public String getTypeSelector() {
        return "PopupControl";
    }

    @Override
    public Styleable getStyleableParent() {
        Scene scene;
        Node node = this.getOwnerNode();
        if (node != null) {
            return node;
        }
        Window window = this.getOwnerWindow();
        if (window != null && (scene = window.getScene()) != null) {
            return scene.getRoot();
        }
        return this.bridge.getParent();
    }

    @Override
    public final ObservableSet<PseudoClass> getPseudoClassStates() {
        return FXCollections.emptyObservableSet();
    }

    @Override
    public Node getStyleableNode() {
        return this.bridge;
    }

    static {
        if (Application.getUserAgentStylesheet() == null) {
            PlatformImpl.setDefaultPlatformUserAgentStylesheet();
        }
        SKIN = new CssMetaData<CSSBridge, String>("-fx-skin", StringConverter.getInstance()){

            @Override
            public boolean isSettable(CSSBridge cSSBridge) {
                return !cSSBridge.popupControl.skinProperty().isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(CSSBridge cSSBridge) {
                return (StyleableProperty)((Object)cSSBridge.popupControl.skinClassNameProperty());
            }
        };
        ArrayList arrayList = new ArrayList();
        Collections.addAll(arrayList, SKIN);
        STYLEABLES = Collections.unmodifiableList(arrayList);
    }

    static final class CSSBridgeHelper
    extends PaneHelper {
        private static final CSSBridgeHelper theInstance = new CSSBridgeHelper();

        CSSBridgeHelper() {
        }

        private static CSSBridgeHelper getInstance() {
            return theInstance;
        }

        public static void initHelper(CSSBridge cSSBridge) {
            CSSBridgeHelper.setHelper(cSSBridge, CSSBridgeHelper.getInstance());
        }

        public static void superProcessCSS(Node node) {
            ((CSSBridgeHelper)CSSBridgeHelper.getHelper(node)).superProcessCSSImpl(node);
        }

        void superProcessCSSImpl(Node node) {
            super.processCSSImpl(node);
        }

        @Override
        protected void processCSSImpl(Node node) {
            ((CSSBridge)node).doProcessCSS();
        }

        @Override
        protected List<String> getAllParentStylesheetsImpl(Parent parent) {
            return ((CSSBridge)parent).doGetAllParentStylesheets();
        }
    }

    protected class CSSBridge
    extends Pane {
        private final PopupControl popupControl;

        protected CSSBridge() {
            this.popupControl = PopupControl.this;
            CSSBridgeHelper.initHelper(this);
        }

        @Override
        public void requestLayout() {
            PopupControl.this.prefWidthCache = -1.0;
            PopupControl.this.prefHeightCache = -1.0;
            PopupControl.this.minWidthCache = -1.0;
            PopupControl.this.minHeightCache = -1.0;
            PopupControl.this.maxWidthCache = -1.0;
            PopupControl.this.maxHeightCache = -1.0;
            super.requestLayout();
        }

        @Override
        public Styleable getStyleableParent() {
            return PopupControl.this.getStyleableParent();
        }

        @Override
        public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
            return PopupControl.this.getCssMetaData();
        }

        private List<String> doGetAllParentStylesheets() {
            Styleable styleable = this.getStyleableParent();
            if (styleable instanceof Parent) {
                return ParentHelper.getAllParentStylesheets((Parent)styleable);
            }
            return null;
        }

        private void doProcessCSS() {
            CSSBridgeHelper.superProcessCSS(this);
            if (PopupControl.this.getSkin() == null) {
                Skin<?> skin = PopupControl.this.createDefaultSkin();
                if (skin != null) {
                    PopupControl.this.skinProperty().set(skin);
                    CSSBridgeHelper.superProcessCSS(this);
                } else {
                    String string = "The -fx-skin property has not been defined in CSS for " + this + " and createDefaultSkin() returned null.";
                    ObservableList<CssParser.ParseError> observableList = StyleManager.getErrors();
                    if (observableList != null) {
                        CssParser.ParseError parseError = new CssParser.ParseError(string);
                        observableList.add(parseError);
                    }
                    Logging.getControlsLogger().severe(string);
                }
            }
        }
    }
}

