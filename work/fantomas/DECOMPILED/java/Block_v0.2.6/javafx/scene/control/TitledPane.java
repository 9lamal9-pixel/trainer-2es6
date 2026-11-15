/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.TitledPaneSkin;

@DefaultProperty(value="content")
public class TitledPane
extends Labeled {
    private ObjectProperty<Node> content;
    private BooleanProperty expanded = new BooleanPropertyBase(true){

        @Override
        protected void invalidated() {
            boolean bl = this.get();
            TitledPane.this.pseudoClassStateChanged(PSEUDO_CLASS_EXPANDED, bl);
            TitledPane.this.pseudoClassStateChanged(PSEUDO_CLASS_COLLAPSED, !bl);
            TitledPane.this.notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        }

        @Override
        public Object getBean() {
            return TitledPane.this;
        }

        @Override
        public String getName() {
            return "expanded";
        }
    };
    private BooleanProperty animated = new StyleableBooleanProperty(true){

        @Override
        public Object getBean() {
            return TitledPane.this;
        }

        @Override
        public String getName() {
            return "animated";
        }

        @Override
        public CssMetaData<TitledPane, Boolean> getCssMetaData() {
            return StyleableProperties.ANIMATED;
        }
    };
    private BooleanProperty collapsible = new StyleableBooleanProperty(true){

        @Override
        public Object getBean() {
            return TitledPane.this;
        }

        @Override
        public String getName() {
            return "collapsible";
        }

        @Override
        public CssMetaData<TitledPane, Boolean> getCssMetaData() {
            return StyleableProperties.COLLAPSIBLE;
        }
    };
    private static final String DEFAULT_STYLE_CLASS = "titled-pane";
    private static final PseudoClass PSEUDO_CLASS_EXPANDED = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass PSEUDO_CLASS_COLLAPSED = PseudoClass.getPseudoClass("collapsed");

    public TitledPane() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TITLED_PANE);
        this.pseudoClassStateChanged(PSEUDO_CLASS_EXPANDED, true);
    }

    public TitledPane(String string, Node node) {
        this();
        this.setText(string);
        this.setContent(node);
    }

    public final void setContent(Node node) {
        this.contentProperty().set(node);
    }

    public final Node getContent() {
        return this.content == null ? null : (Node)this.content.get();
    }

    public final ObjectProperty<Node> contentProperty() {
        if (this.content == null) {
            this.content = new SimpleObjectProperty<Node>(this, "content");
        }
        return this.content;
    }

    public final void setExpanded(boolean bl) {
        this.expandedProperty().set(bl);
    }

    public final boolean isExpanded() {
        return this.expanded.get();
    }

    public final BooleanProperty expandedProperty() {
        return this.expanded;
    }

    public final void setAnimated(boolean bl) {
        this.animatedProperty().set(bl);
    }

    public final boolean isAnimated() {
        return this.animated.get();
    }

    public final BooleanProperty animatedProperty() {
        return this.animated;
    }

    public final void setCollapsible(boolean bl) {
        this.collapsibleProperty().set(bl);
    }

    public final boolean isCollapsible() {
        return this.collapsible.get();
    }

    public final BooleanProperty collapsibleProperty() {
        return this.collapsible;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TitledPaneSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return TitledPane.getClassCssMetaData();
    }

    @Override
    public Orientation getContentBias() {
        Node node = this.getContent();
        return node == null ? super.getContentBias() : node.getContentBias();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                String string = this.getAccessibleText();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                return this.getText();
            }
            case EXPANDED: {
                return this.isExpanded();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case EXPAND: {
                this.setExpanded(true);
                break;
            }
            case COLLAPSE: {
                this.setExpanded(false);
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<TitledPane, Boolean> COLLAPSIBLE = new CssMetaData<TitledPane, Boolean>("-fx-collapsible", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(TitledPane titledPane) {
                return titledPane.collapsible == null || !titledPane.collapsible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(TitledPane titledPane) {
                return (StyleableProperty)((Object)titledPane.collapsibleProperty());
            }
        };
        private static final CssMetaData<TitledPane, Boolean> ANIMATED = new CssMetaData<TitledPane, Boolean>("-fx-animated", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(TitledPane titledPane) {
                return titledPane.animated == null || !titledPane.animated.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(TitledPane titledPane) {
                return (StyleableProperty)((Object)titledPane.animatedProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Labeled.getClassCssMetaData());
            arrayList.add(COLLAPSIBLE);
            arrayList.add(ANIMATED);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

