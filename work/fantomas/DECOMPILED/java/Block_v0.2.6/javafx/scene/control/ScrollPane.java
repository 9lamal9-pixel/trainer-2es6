/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ScrollPaneSkin;

@DefaultProperty(value="content")
public class ScrollPane
extends Control {
    private ObjectProperty<ScrollBarPolicy> hbarPolicy;
    private ObjectProperty<ScrollBarPolicy> vbarPolicy;
    private ObjectProperty<Node> content;
    private DoubleProperty hvalue;
    private DoubleProperty vvalue;
    private DoubleProperty hmin;
    private DoubleProperty vmin;
    private DoubleProperty hmax;
    private DoubleProperty vmax;
    private BooleanProperty fitToWidth;
    private BooleanProperty fitToHeight;
    private BooleanProperty pannable;
    private DoubleProperty prefViewportWidth;
    private DoubleProperty prefViewportHeight;
    private DoubleProperty minViewportWidth;
    private DoubleProperty minViewportHeight;
    private ObjectProperty<Bounds> viewportBounds;
    private static final String DEFAULT_STYLE_CLASS = "scroll-pane";
    private static final PseudoClass PANNABLE_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("pannable");
    private static final PseudoClass FIT_TO_WIDTH_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("fitToWidth");
    private static final PseudoClass FIT_TO_HEIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("fitToHeight");

    public ScrollPane() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.SCROLL_PANE);
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
    }

    public ScrollPane(Node node) {
        this();
        this.setContent(node);
    }

    public final void setHbarPolicy(ScrollBarPolicy scrollBarPolicy) {
        this.hbarPolicyProperty().set(scrollBarPolicy);
    }

    public final ScrollBarPolicy getHbarPolicy() {
        return this.hbarPolicy == null ? ScrollBarPolicy.AS_NEEDED : (ScrollBarPolicy)((Object)this.hbarPolicy.get());
    }

    public final ObjectProperty<ScrollBarPolicy> hbarPolicyProperty() {
        if (this.hbarPolicy == null) {
            this.hbarPolicy = new StyleableObjectProperty<ScrollBarPolicy>(ScrollBarPolicy.AS_NEEDED){

                @Override
                public CssMetaData<ScrollPane, ScrollBarPolicy> getCssMetaData() {
                    return StyleableProperties.HBAR_POLICY;
                }

                @Override
                public Object getBean() {
                    return ScrollPane.this;
                }

                @Override
                public String getName() {
                    return "hbarPolicy";
                }
            };
        }
        return this.hbarPolicy;
    }

    public final void setVbarPolicy(ScrollBarPolicy scrollBarPolicy) {
        this.vbarPolicyProperty().set(scrollBarPolicy);
    }

    public final ScrollBarPolicy getVbarPolicy() {
        return this.vbarPolicy == null ? ScrollBarPolicy.AS_NEEDED : (ScrollBarPolicy)((Object)this.vbarPolicy.get());
    }

    public final ObjectProperty<ScrollBarPolicy> vbarPolicyProperty() {
        if (this.vbarPolicy == null) {
            this.vbarPolicy = new StyleableObjectProperty<ScrollBarPolicy>(ScrollBarPolicy.AS_NEEDED){

                @Override
                public CssMetaData<ScrollPane, ScrollBarPolicy> getCssMetaData() {
                    return StyleableProperties.VBAR_POLICY;
                }

                @Override
                public Object getBean() {
                    return ScrollPane.this;
                }

                @Override
                public String getName() {
                    return "vbarPolicy";
                }
            };
        }
        return this.vbarPolicy;
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

    public final void setHvalue(double d) {
        this.hvalueProperty().set(d);
    }

    public final double getHvalue() {
        return this.hvalue == null ? 0.0 : this.hvalue.get();
    }

    public final DoubleProperty hvalueProperty() {
        if (this.hvalue == null) {
            this.hvalue = new SimpleDoubleProperty(this, "hvalue");
        }
        return this.hvalue;
    }

    public final void setVvalue(double d) {
        this.vvalueProperty().set(d);
    }

    public final double getVvalue() {
        return this.vvalue == null ? 0.0 : this.vvalue.get();
    }

    public final DoubleProperty vvalueProperty() {
        if (this.vvalue == null) {
            this.vvalue = new SimpleDoubleProperty(this, "vvalue");
        }
        return this.vvalue;
    }

    public final void setHmin(double d) {
        this.hminProperty().set(d);
    }

    public final double getHmin() {
        return this.hmin == null ? 0.0 : this.hmin.get();
    }

    public final DoubleProperty hminProperty() {
        if (this.hmin == null) {
            this.hmin = new SimpleDoubleProperty(this, "hmin", 0.0);
        }
        return this.hmin;
    }

    public final void setVmin(double d) {
        this.vminProperty().set(d);
    }

    public final double getVmin() {
        return this.vmin == null ? 0.0 : this.vmin.get();
    }

    public final DoubleProperty vminProperty() {
        if (this.vmin == null) {
            this.vmin = new SimpleDoubleProperty(this, "vmin", 0.0);
        }
        return this.vmin;
    }

    public final void setHmax(double d) {
        this.hmaxProperty().set(d);
    }

    public final double getHmax() {
        return this.hmax == null ? 1.0 : this.hmax.get();
    }

    public final DoubleProperty hmaxProperty() {
        if (this.hmax == null) {
            this.hmax = new SimpleDoubleProperty(this, "hmax", 1.0);
        }
        return this.hmax;
    }

    public final void setVmax(double d) {
        this.vmaxProperty().set(d);
    }

    public final double getVmax() {
        return this.vmax == null ? 1.0 : this.vmax.get();
    }

    public final DoubleProperty vmaxProperty() {
        if (this.vmax == null) {
            this.vmax = new SimpleDoubleProperty(this, "vmax", 1.0);
        }
        return this.vmax;
    }

    public final void setFitToWidth(boolean bl) {
        this.fitToWidthProperty().set(bl);
    }

    public final boolean isFitToWidth() {
        return this.fitToWidth == null ? false : this.fitToWidth.get();
    }

    public final BooleanProperty fitToWidthProperty() {
        if (this.fitToWidth == null) {
            this.fitToWidth = new StyleableBooleanProperty(false){

                @Override
                public void invalidated() {
                    ScrollPane.this.pseudoClassStateChanged(FIT_TO_WIDTH_PSEUDOCLASS_STATE, this.get());
                }

                @Override
                public CssMetaData<ScrollPane, Boolean> getCssMetaData() {
                    return StyleableProperties.FIT_TO_WIDTH;
                }

                @Override
                public Object getBean() {
                    return ScrollPane.this;
                }

                @Override
                public String getName() {
                    return "fitToWidth";
                }
            };
        }
        return this.fitToWidth;
    }

    public final void setFitToHeight(boolean bl) {
        this.fitToHeightProperty().set(bl);
    }

    public final boolean isFitToHeight() {
        return this.fitToHeight == null ? false : this.fitToHeight.get();
    }

    public final BooleanProperty fitToHeightProperty() {
        if (this.fitToHeight == null) {
            this.fitToHeight = new StyleableBooleanProperty(false){

                @Override
                public void invalidated() {
                    ScrollPane.this.pseudoClassStateChanged(FIT_TO_HEIGHT_PSEUDOCLASS_STATE, this.get());
                }

                @Override
                public CssMetaData<ScrollPane, Boolean> getCssMetaData() {
                    return StyleableProperties.FIT_TO_HEIGHT;
                }

                @Override
                public Object getBean() {
                    return ScrollPane.this;
                }

                @Override
                public String getName() {
                    return "fitToHeight";
                }
            };
        }
        return this.fitToHeight;
    }

    public final void setPannable(boolean bl) {
        this.pannableProperty().set(bl);
    }

    public final boolean isPannable() {
        return this.pannable == null ? false : this.pannable.get();
    }

    public final BooleanProperty pannableProperty() {
        if (this.pannable == null) {
            this.pannable = new StyleableBooleanProperty(false){

                @Override
                public void invalidated() {
                    ScrollPane.this.pseudoClassStateChanged(PANNABLE_PSEUDOCLASS_STATE, this.get());
                }

                @Override
                public CssMetaData<ScrollPane, Boolean> getCssMetaData() {
                    return StyleableProperties.PANNABLE;
                }

                @Override
                public Object getBean() {
                    return ScrollPane.this;
                }

                @Override
                public String getName() {
                    return "pannable";
                }
            };
        }
        return this.pannable;
    }

    public final void setPrefViewportWidth(double d) {
        this.prefViewportWidthProperty().set(d);
    }

    public final double getPrefViewportWidth() {
        return this.prefViewportWidth == null ? 0.0 : this.prefViewportWidth.get();
    }

    public final DoubleProperty prefViewportWidthProperty() {
        if (this.prefViewportWidth == null) {
            this.prefViewportWidth = new SimpleDoubleProperty(this, "prefViewportWidth");
        }
        return this.prefViewportWidth;
    }

    public final void setPrefViewportHeight(double d) {
        this.prefViewportHeightProperty().set(d);
    }

    public final double getPrefViewportHeight() {
        return this.prefViewportHeight == null ? 0.0 : this.prefViewportHeight.get();
    }

    public final DoubleProperty prefViewportHeightProperty() {
        if (this.prefViewportHeight == null) {
            this.prefViewportHeight = new SimpleDoubleProperty(this, "prefViewportHeight");
        }
        return this.prefViewportHeight;
    }

    public final void setMinViewportWidth(double d) {
        this.minViewportWidthProperty().set(d);
    }

    public final double getMinViewportWidth() {
        return this.minViewportWidth == null ? 0.0 : this.minViewportWidth.get();
    }

    public final DoubleProperty minViewportWidthProperty() {
        if (this.minViewportWidth == null) {
            this.minViewportWidth = new SimpleDoubleProperty(this, "minViewportWidth");
        }
        return this.minViewportWidth;
    }

    public final void setMinViewportHeight(double d) {
        this.minViewportHeightProperty().set(d);
    }

    public final double getMinViewportHeight() {
        return this.minViewportHeight == null ? 0.0 : this.minViewportHeight.get();
    }

    public final DoubleProperty minViewportHeightProperty() {
        if (this.minViewportHeight == null) {
            this.minViewportHeight = new SimpleDoubleProperty(this, "minViewportHeight");
        }
        return this.minViewportHeight;
    }

    public final void setViewportBounds(Bounds bounds) {
        this.viewportBoundsProperty().set(bounds);
    }

    public final Bounds getViewportBounds() {
        return this.viewportBounds == null ? new BoundingBox(0.0, 0.0, 0.0, 0.0) : (Bounds)this.viewportBounds.get();
    }

    public final ObjectProperty<Bounds> viewportBoundsProperty() {
        if (this.viewportBounds == null) {
            this.viewportBounds = new SimpleObjectProperty<BoundingBox>(this, "viewportBounds", new BoundingBox(0.0, 0.0, 0.0, 0.0));
        }
        return this.viewportBounds;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ScrollPaneSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return ScrollPane.getClassCssMetaData();
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case CONTENTS: {
                return this.getContent();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    public static enum ScrollBarPolicy {
        NEVER,
        ALWAYS,
        AS_NEEDED;

    }

    private static class StyleableProperties {
        private static final CssMetaData<ScrollPane, ScrollBarPolicy> HBAR_POLICY = new CssMetaData<ScrollPane, ScrollBarPolicy>("-fx-hbar-policy", new EnumConverter<ScrollBarPolicy>(ScrollBarPolicy.class), ScrollBarPolicy.AS_NEEDED){

            @Override
            public boolean isSettable(ScrollPane scrollPane) {
                return scrollPane.hbarPolicy == null || !scrollPane.hbarPolicy.isBound();
            }

            @Override
            public StyleableProperty<ScrollBarPolicy> getStyleableProperty(ScrollPane scrollPane) {
                return (StyleableProperty)((Object)scrollPane.hbarPolicyProperty());
            }
        };
        private static final CssMetaData<ScrollPane, ScrollBarPolicy> VBAR_POLICY = new CssMetaData<ScrollPane, ScrollBarPolicy>("-fx-vbar-policy", new EnumConverter<ScrollBarPolicy>(ScrollBarPolicy.class), ScrollBarPolicy.AS_NEEDED){

            @Override
            public boolean isSettable(ScrollPane scrollPane) {
                return scrollPane.vbarPolicy == null || !scrollPane.vbarPolicy.isBound();
            }

            @Override
            public StyleableProperty<ScrollBarPolicy> getStyleableProperty(ScrollPane scrollPane) {
                return (StyleableProperty)((Object)scrollPane.vbarPolicyProperty());
            }
        };
        private static final CssMetaData<ScrollPane, Boolean> FIT_TO_WIDTH = new CssMetaData<ScrollPane, Boolean>("-fx-fit-to-width", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(ScrollPane scrollPane) {
                return scrollPane.fitToWidth == null || !scrollPane.fitToWidth.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(ScrollPane scrollPane) {
                return (StyleableProperty)((Object)scrollPane.fitToWidthProperty());
            }
        };
        private static final CssMetaData<ScrollPane, Boolean> FIT_TO_HEIGHT = new CssMetaData<ScrollPane, Boolean>("-fx-fit-to-height", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(ScrollPane scrollPane) {
                return scrollPane.fitToHeight == null || !scrollPane.fitToHeight.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(ScrollPane scrollPane) {
                return (StyleableProperty)((Object)scrollPane.fitToHeightProperty());
            }
        };
        private static final CssMetaData<ScrollPane, Boolean> PANNABLE = new CssMetaData<ScrollPane, Boolean>("-fx-pannable", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(ScrollPane scrollPane) {
                return scrollPane.pannable == null || !scrollPane.pannable.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(ScrollPane scrollPane) {
                return (StyleableProperty)((Object)scrollPane.pannableProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(HBAR_POLICY);
            arrayList.add(VBAR_POLICY);
            arrayList.add(FIT_TO_WIDTH);
            arrayList.add(FIT_TO_HEIGHT);
            arrayList.add(PANNABLE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

