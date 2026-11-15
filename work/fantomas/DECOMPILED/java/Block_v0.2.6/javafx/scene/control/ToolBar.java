/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ToolBarSkin;

@DefaultProperty(value="items")
public class ToolBar
extends Control {
    private final ObservableList<Node> items = FXCollections.observableArrayList();
    private ObjectProperty<Orientation> orientation;
    private static final String DEFAULT_STYLE_CLASS = "tool-bar";
    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

    public ToolBar() {
        this.initialize();
    }

    public ToolBar(Node ... nodeArray) {
        this.initialize();
        this.items.addAll((Node[])nodeArray);
    }

    private void initialize() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TOOL_BAR);
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
        this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, true);
    }

    public final ObservableList<Node> getItems() {
        return this.items;
    }

    public final void setOrientation(Orientation orientation) {
        this.orientationProperty().set(orientation);
    }

    public final Orientation getOrientation() {
        return this.orientation == null ? Orientation.HORIZONTAL : (Orientation)((Object)this.orientation.get());
    }

    public final ObjectProperty<Orientation> orientationProperty() {
        if (this.orientation == null) {
            this.orientation = new StyleableObjectProperty<Orientation>(Orientation.HORIZONTAL){

                @Override
                public void invalidated() {
                    boolean bl = this.get() == Orientation.VERTICAL;
                    ToolBar.this.pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, bl);
                    ToolBar.this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, !bl);
                }

                @Override
                public Object getBean() {
                    return ToolBar.this;
                }

                @Override
                public String getName() {
                    return "orientation";
                }

                @Override
                public CssMetaData<ToolBar, Orientation> getCssMetaData() {
                    return StyleableProperties.ORIENTATION;
                }
            };
        }
        return this.orientation;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ToolBarSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return ToolBar.getClassCssMetaData();
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    private static class StyleableProperties {
        private static final CssMetaData<ToolBar, Orientation> ORIENTATION = new CssMetaData<ToolBar, Orientation>("-fx-orientation", new EnumConverter<Orientation>(Orientation.class), Orientation.HORIZONTAL){

            @Override
            public Orientation getInitialValue(ToolBar toolBar) {
                return toolBar.getOrientation();
            }

            @Override
            public boolean isSettable(ToolBar toolBar) {
                return toolBar.orientation == null || !toolBar.orientation.isBound();
            }

            @Override
            public StyleableProperty<Orientation> getStyleableProperty(ToolBar toolBar) {
                return (StyleableProperty)((Object)toolBar.orientationProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(ORIENTATION);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

