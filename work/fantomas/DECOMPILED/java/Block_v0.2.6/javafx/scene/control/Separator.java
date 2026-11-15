/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.SeparatorSkin;

public class Separator
extends Control {
    private ObjectProperty<Orientation> orientation = new StyleableObjectProperty<Orientation>(Orientation.HORIZONTAL){

        @Override
        protected void invalidated() {
            boolean bl = this.get() == Orientation.VERTICAL;
            Separator.this.pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, bl);
            Separator.this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, !bl);
        }

        @Override
        public CssMetaData<Separator, Orientation> getCssMetaData() {
            return StyleableProperties.ORIENTATION;
        }

        @Override
        public Object getBean() {
            return Separator.this;
        }

        @Override
        public String getName() {
            return "orientation";
        }
    };
    private ObjectProperty<HPos> halignment;
    private ObjectProperty<VPos> valignment;
    private static final String DEFAULT_STYLE_CLASS = "separator";
    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

    public Separator() {
        this(Orientation.HORIZONTAL);
    }

    public Separator(Orientation orientation) {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
        this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, orientation != Orientation.VERTICAL);
        this.pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, orientation == Orientation.VERTICAL);
        ((StyleableProperty)((Object)this.orientationProperty())).applyStyle(null, orientation != null ? orientation : Orientation.HORIZONTAL);
    }

    public final void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    public final Orientation getOrientation() {
        return (Orientation)((Object)this.orientation.get());
    }

    public final ObjectProperty<Orientation> orientationProperty() {
        return this.orientation;
    }

    public final void setHalignment(HPos hPos) {
        this.halignmentProperty().set(hPos);
    }

    public final HPos getHalignment() {
        return this.halignment == null ? HPos.CENTER : (HPos)((Object)this.halignment.get());
    }

    public final ObjectProperty<HPos> halignmentProperty() {
        if (this.halignment == null) {
            this.halignment = new StyleableObjectProperty<HPos>(HPos.CENTER){

                @Override
                public Object getBean() {
                    return Separator.this;
                }

                @Override
                public String getName() {
                    return "halignment";
                }

                @Override
                public CssMetaData<Separator, HPos> getCssMetaData() {
                    return StyleableProperties.HALIGNMENT;
                }
            };
        }
        return this.halignment;
    }

    public final void setValignment(VPos vPos) {
        this.valignmentProperty().set(vPos);
    }

    public final VPos getValignment() {
        return this.valignment == null ? VPos.CENTER : (VPos)((Object)this.valignment.get());
    }

    public final ObjectProperty<VPos> valignmentProperty() {
        if (this.valignment == null) {
            this.valignment = new StyleableObjectProperty<VPos>(VPos.CENTER){

                @Override
                public Object getBean() {
                    return Separator.this;
                }

                @Override
                public String getName() {
                    return "valignment";
                }

                @Override
                public CssMetaData<Separator, VPos> getCssMetaData() {
                    return StyleableProperties.VALIGNMENT;
                }
            };
        }
        return this.valignment;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SeparatorSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return Separator.getClassCssMetaData();
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    private static class StyleableProperties {
        private static final CssMetaData<Separator, Orientation> ORIENTATION = new CssMetaData<Separator, Orientation>("-fx-orientation", new EnumConverter<Orientation>(Orientation.class), Orientation.HORIZONTAL){

            @Override
            public Orientation getInitialValue(Separator separator) {
                return separator.getOrientation();
            }

            @Override
            public boolean isSettable(Separator separator) {
                return separator.orientation == null || !separator.orientation.isBound();
            }

            @Override
            public StyleableProperty<Orientation> getStyleableProperty(Separator separator) {
                return (StyleableProperty)((Object)separator.orientationProperty());
            }
        };
        private static final CssMetaData<Separator, HPos> HALIGNMENT = new CssMetaData<Separator, HPos>("-fx-halignment", new EnumConverter<HPos>(HPos.class), HPos.CENTER){

            @Override
            public boolean isSettable(Separator separator) {
                return separator.halignment == null || !separator.halignment.isBound();
            }

            @Override
            public StyleableProperty<HPos> getStyleableProperty(Separator separator) {
                return (StyleableProperty)((Object)separator.halignmentProperty());
            }
        };
        private static final CssMetaData<Separator, VPos> VALIGNMENT = new CssMetaData<Separator, VPos>("-fx-valignment", new EnumConverter<VPos>(VPos.class), VPos.CENTER){

            @Override
            public boolean isSettable(Separator separator) {
                return separator.valignment == null || !separator.valignment.isBound();
            }

            @Override
            public StyleableProperty<VPos> getStyleableProperty(Separator separator) {
                return (StyleableProperty)((Object)separator.valignmentProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(ORIENTATION);
            arrayList.add(HALIGNMENT);
            arrayList.add(VALIGNMENT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

