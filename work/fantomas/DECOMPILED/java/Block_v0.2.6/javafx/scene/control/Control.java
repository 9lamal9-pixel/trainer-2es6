/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.ControlAcceleratorSupport;
import com.sun.javafx.scene.control.ControlHelper;
import com.sun.javafx.scene.control.Logging;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.CssParser;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.StringConverter;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Skinnable;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Region;

public abstract class Control
extends Region
implements Skinnable {
    private List<CssMetaData<? extends Styleable, ?>> styleableProperties;
    private SkinBase<?> skinBase;
    private static final EventHandler<ContextMenuEvent> contextMenuHandler;
    private ObjectProperty<Skin<?>> skin = new StyleableObjectProperty<Skin<?>>(){
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
            Object object;
            Skin skin = (Skin)this.get();
            Control.this.currentSkinClassName = skin == null ? null : skin.getClass().getName();
            Control.this.skinClassNameProperty().set(Control.this.currentSkinClassName);
            if (this.oldValue != null) {
                this.oldValue.dispose();
            }
            this.oldValue = skin;
            Control.this.skinBase = null;
            if (skin instanceof SkinBase) {
                Control.this.skinBase = (SkinBase)skin;
            } else {
                object = Control.this.getSkinNode();
                if (object != null) {
                    Control.this.getChildren().setAll(object);
                } else {
                    Control.this.getChildren().clear();
                }
            }
            Control.this.styleableProperties = null;
            NodeHelper.reapplyCSS(Control.this);
            object = Logging.getControlsLogger();
            if (((PlatformLogger)object).isLoggable(PlatformLogger.Level.FINEST)) {
                ((PlatformLogger)object).finest("Stored skin[" + this.getValue() + "] on " + this);
            }
        }

        @Override
        public CssMetaData getCssMetaData() {
            return StyleableProperties.SKIN;
        }

        @Override
        public Object getBean() {
            return Control.this;
        }

        @Override
        public String getName() {
            return "skin";
        }
    };
    private ObjectProperty<Tooltip> tooltip;
    private ObjectProperty<ContextMenu> contextMenu = new SimpleObjectProperty<ContextMenu>((Object)this, "contextMenu"){
        private WeakReference<ContextMenu> contextMenuRef;

        @Override
        protected void invalidated() {
            ContextMenu contextMenu;
            ContextMenu contextMenu2 = contextMenu = this.contextMenuRef == null ? null : (ContextMenu)this.contextMenuRef.get();
            if (contextMenu != null) {
                ControlAcceleratorSupport.removeAcceleratorsFromScene(contextMenu.getItems(), Control.this);
            }
            ContextMenu contextMenu3 = (ContextMenu)this.get();
            this.contextMenuRef = new WeakReference<ContextMenu>(contextMenu3);
            if (contextMenu3 != null) {
                contextMenu3.setShowRelativeToWindow(true);
                ControlAcceleratorSupport.addAcceleratorsIntoScene(contextMenu3.getItems(), Control.this);
            }
        }
    };
    private String currentSkinClassName;
    private StringProperty skinClassName;
    private boolean skinCreationLocked;

    private static Class<?> loadClass(String string, Object object) throws ClassNotFoundException {
        try {
            return Class.forName(string, false, Control.class.getClassLoader());
        }
        catch (ClassNotFoundException classNotFoundException) {
            if (Thread.currentThread().getContextClassLoader() != null) {
                try {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    return Class.forName(string, false, classLoader);
                }
                catch (ClassNotFoundException classNotFoundException2) {
                    // empty catch block
                }
            }
            if (object != null) {
                for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                    try {
                        ClassLoader classLoader = clazz.getClassLoader();
                        return Class.forName(string, false, classLoader);
                    }
                    catch (ClassNotFoundException classNotFoundException3) {
                        continue;
                    }
                }
            }
            throw classNotFoundException;
        }
    }

    @Override
    public final ObjectProperty<Skin<?>> skinProperty() {
        return this.skin;
    }

    @Override
    public final void setSkin(Skin<?> skin) {
        this.skinProperty().set(skin);
    }

    @Override
    public final Skin<?> getSkin() {
        return (Skin)this.skinProperty().getValue();
    }

    public final ObjectProperty<Tooltip> tooltipProperty() {
        if (this.tooltip == null) {
            this.tooltip = new ObjectPropertyBase<Tooltip>(){
                private Tooltip old = null;

                @Override
                protected void invalidated() {
                    Tooltip tooltip = (Tooltip)this.get();
                    if (tooltip != this.old) {
                        if (this.old != null) {
                            Tooltip.uninstall(Control.this, this.old);
                        }
                        if (tooltip != null) {
                            Tooltip.install(Control.this, tooltip);
                        }
                        this.old = tooltip;
                    }
                }

                @Override
                public Object getBean() {
                    return Control.this;
                }

                @Override
                public String getName() {
                    return "tooltip";
                }
            };
        }
        return this.tooltip;
    }

    public final void setTooltip(Tooltip tooltip) {
        this.tooltipProperty().setValue(tooltip);
    }

    public final Tooltip getTooltip() {
        return this.tooltip == null ? null : (Tooltip)this.tooltip.getValue();
    }

    public final ObjectProperty<ContextMenu> contextMenuProperty() {
        return this.contextMenu;
    }

    public final void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu.setValue(contextMenu);
    }

    public final ContextMenu getContextMenu() {
        return this.contextMenu == null ? null : (ContextMenu)this.contextMenu.getValue();
    }

    protected Control() {
        ControlHelper.initHelper(this);
        this.currentSkinClassName = null;
        this.skinCreationLocked = false;
        StyleableProperty styleableProperty = (StyleableProperty)((Object)this.focusTraversableProperty());
        styleableProperty.applyStyle(null, Boolean.TRUE);
        this.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuHandler);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected double computeMinWidth(double d) {
        if (this.skinBase != null) {
            return this.skinBase.computeMinWidth(d, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset());
        }
        Node node = this.getSkinNode();
        return node == null ? 0.0 : node.minWidth(d);
    }

    @Override
    protected double computeMinHeight(double d) {
        if (this.skinBase != null) {
            return this.skinBase.computeMinHeight(d, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset());
        }
        Node node = this.getSkinNode();
        return node == null ? 0.0 : node.minHeight(d);
    }

    @Override
    protected double computeMaxWidth(double d) {
        if (this.skinBase != null) {
            return this.skinBase.computeMaxWidth(d, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset());
        }
        Node node = this.getSkinNode();
        return node == null ? 0.0 : node.maxWidth(d);
    }

    @Override
    protected double computeMaxHeight(double d) {
        if (this.skinBase != null) {
            return this.skinBase.computeMaxHeight(d, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset());
        }
        Node node = this.getSkinNode();
        return node == null ? 0.0 : node.maxHeight(d);
    }

    @Override
    protected double computePrefWidth(double d) {
        if (this.skinBase != null) {
            return this.skinBase.computePrefWidth(d, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset());
        }
        Node node = this.getSkinNode();
        return node == null ? 0.0 : node.prefWidth(d);
    }

    @Override
    protected double computePrefHeight(double d) {
        if (this.skinBase != null) {
            return this.skinBase.computePrefHeight(d, this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset());
        }
        Node node = this.getSkinNode();
        return node == null ? 0.0 : node.prefHeight(d);
    }

    @Override
    public double getBaselineOffset() {
        if (this.skinBase != null) {
            return this.skinBase.computeBaselineOffset(this.snappedTopInset(), this.snappedRightInset(), this.snappedBottomInset(), this.snappedLeftInset());
        }
        Node node = this.getSkinNode();
        return node == null ? 0.0 : node.getBaselineOffset();
    }

    @Override
    protected void layoutChildren() {
        if (this.skinBase != null) {
            double d = this.snappedLeftInset();
            double d2 = this.snappedTopInset();
            double d3 = this.snapSizeX(this.getWidth()) - d - this.snappedRightInset();
            double d4 = this.snapSizeY(this.getHeight()) - d2 - this.snappedBottomInset();
            this.skinBase.layoutChildren(d, d2, d3, d4);
        } else {
            Node node = this.getSkinNode();
            if (node != null) {
                node.resizeRelocate(0.0, 0.0, this.getWidth(), this.getHeight());
            }
        }
    }

    protected Skin<?> createDefaultSkin() {
        return null;
    }

    ObservableList<Node> getControlChildren() {
        return this.getChildren();
    }

    private Node getSkinNode() {
        assert (this.skinBase == null);
        Skin<?> skin = this.getSkin();
        return skin == null ? null : skin.getNode();
    }

    StringProperty skinClassNameProperty() {
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
                    if (this.get() != null && !this.get().equals(Control.this.currentSkinClassName)) {
                        Control.loadSkinClass(Control.this, (String)Control.this.skinClassName.get());
                    }
                }

                @Override
                public Object getBean() {
                    return Control.this;
                }

                @Override
                public String getName() {
                    return "skinClassName";
                }

                @Override
                public CssMetaData<Control, String> getCssMetaData() {
                    return StyleableProperties.SKIN;
                }
            };
        }
        return this.skinClassName;
    }

    static void loadSkinClass(Skinnable skinnable, String string) {
        if (string == null || string.isEmpty()) {
            String string2 = "Empty -fx-skin property specified for control " + skinnable;
            ObservableList<CssParser.ParseError> observableList = StyleManager.getErrors();
            if (observableList != null) {
                CssParser.ParseError parseError = new CssParser.ParseError(string2);
                observableList.add(parseError);
            }
            Logging.getControlsLogger().severe(string2);
            return;
        }
        try {
            Class<?> clazz = Control.loadClass(string, skinnable);
            if (!Skin.class.isAssignableFrom(clazz)) {
                String string3 = "'" + string + "' is not a valid Skin class for control " + skinnable;
                ObservableList<CssParser.ParseError> observableList = StyleManager.getErrors();
                if (observableList != null) {
                    CssParser.ParseError parseError = new CssParser.ParseError(string3);
                    observableList.add(parseError);
                }
                Logging.getControlsLogger().severe(string3);
                return;
            }
            Constructor<?>[] constructorArray = clazz.getConstructors();
            Constructor<?> constructor = null;
            for (Constructor<?> constructor2 : constructorArray) {
                Class<?>[] classArray = constructor2.getParameterTypes();
                if (classArray.length != 1 || !Skinnable.class.isAssignableFrom(classArray[0])) continue;
                constructor = constructor2;
                break;
            }
            if (constructor == null) {
                String parseError = "No valid constructor defined in '" + string + "' for control " + skinnable + ".\r\nYou must provide a constructor that accepts a single Skinnable (e.g. Control or PopupControl) parameter in " + string + ".";
                ObservableList<CssParser.ParseError> observableList = StyleManager.getErrors();
                if (observableList != null) {
                    CssParser.ParseError parseError2 = new CssParser.ParseError(parseError);
                    observableList.add(parseError2);
                }
                Logging.getControlsLogger().severe(parseError);
            } else {
                Skin parseError = (Skin)constructor.newInstance(skinnable);
                skinnable.skinProperty().set(parseError);
            }
        }
        catch (InvocationTargetException invocationTargetException) {
            String string4 = "Failed to load skin '" + string + "' for control " + skinnable;
            ObservableList<CssParser.ParseError> observableList = StyleManager.getErrors();
            if (observableList != null) {
                CssParser.ParseError parseError = new CssParser.ParseError(string4 + " :" + invocationTargetException.getLocalizedMessage());
                observableList.add(parseError);
            }
            Logging.getControlsLogger().severe(string4, invocationTargetException.getCause());
        }
        catch (Exception exception) {
            String string5 = "Failed to load skin '" + string + "' for control " + skinnable;
            ObservableList<CssParser.ParseError> observableList = StyleManager.getErrors();
            if (observableList != null) {
                CssParser.ParseError parseError = new CssParser.ParseError(string5 + " :" + exception.getLocalizedMessage());
                observableList.add(parseError);
            }
            Logging.getControlsLogger().severe(string5, exception);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public final List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        if (this.styleableProperties == null) {
            CssMetaData<Styleable, ?> cssMetaData;
            int n;
            int n2;
            HashMap hashMap = new HashMap();
            List<CssMetaData<Styleable, ?>> list = this.getControlCssMetaData();
            int n3 = n2 = list != null ? list.size() : 0;
            for (n = 0; n < n2; ++n) {
                cssMetaData = list.get(n);
                if (cssMetaData == null) continue;
                hashMap.put(cssMetaData.getProperty(), cssMetaData);
            }
            list = this.skinBase != null ? this.skinBase.getCssMetaData() : null;
            int n4 = n2 = list != null ? list.size() : 0;
            for (n = 0; n < n2; ++n) {
                cssMetaData = list.get(n);
                if (cssMetaData == null) continue;
                hashMap.put(cssMetaData.getProperty(), cssMetaData);
            }
            this.styleableProperties = new ArrayList();
            this.styleableProperties.addAll(hashMap.values());
        }
        return this.styleableProperties;
    }

    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return Control.getClassCssMetaData();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doProcessCSS() {
        ControlHelper.superProcessCSS(this);
        if (this.getSkin() == null) {
            if (this.skinCreationLocked) {
                return;
            }
            try {
                this.skinCreationLocked = true;
                Skin<?> skin = this.createDefaultSkin();
                if (skin != null) {
                    this.skinProperty().set(skin);
                    ControlHelper.superProcessCSS(this);
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
            finally {
                this.skinCreationLocked = false;
            }
        }
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.TRUE;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        Object object;
        switch (accessibleAttribute) {
            case HELP: {
                String string = this.getAccessibleHelp();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                Tooltip tooltip = this.getTooltip();
                return tooltip == null ? "" : tooltip.getText();
            }
        }
        if (this.skinBase != null && (object = this.skinBase.queryAccessibleAttribute(accessibleAttribute, objectArray)) != null) {
            return object;
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        if (this.skinBase != null) {
            this.skinBase.executeAccessibleAction(accessibleAction, objectArray);
        }
        super.executeAccessibleAction(accessibleAction, objectArray);
    }

    static {
        ControlHelper.setControlAccessor(new ControlHelper.ControlAccessor(){

            @Override
            public void doProcessCSS(Node node) {
                ((Control)node).doProcessCSS();
            }

            @Override
            public StringProperty skinClassNameProperty(Control control) {
                return control.skinClassNameProperty();
            }
        });
        if (Application.getUserAgentStylesheet() == null) {
            PlatformImpl.setDefaultPlatformUserAgentStylesheet();
        }
        contextMenuHandler = contextMenuEvent -> {
            Control control;
            if (contextMenuEvent.isConsumed()) {
                return;
            }
            Object object = contextMenuEvent.getSource();
            if (object instanceof Control && (control = (Control)object).getContextMenu() != null) {
                control.getContextMenu().show(control, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                contextMenuEvent.consume();
            }
        };
    }

    private static class StyleableProperties {
        private static final CssMetaData<Control, String> SKIN = new CssMetaData<Control, String>("-fx-skin", StringConverter.getInstance()){

            @Override
            public boolean isSettable(Control control) {
                return control.skin == null || !control.skin.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(Control control) {
                return (StyleableProperty)((Object)control.skinClassNameProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Region.getClassCssMetaData());
            arrayList.add(SKIN);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

