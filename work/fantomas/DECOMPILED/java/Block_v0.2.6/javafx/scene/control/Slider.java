/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.SliderSkin;
import javafx.util.StringConverter;

public class Slider
extends Control {
    private DoubleProperty max;
    private DoubleProperty min;
    private DoubleProperty value;
    private BooleanProperty valueChanging;
    private ObjectProperty<Orientation> orientation;
    private BooleanProperty showTickLabels;
    private BooleanProperty showTickMarks;
    private DoubleProperty majorTickUnit;
    private IntegerProperty minorTickCount;
    private BooleanProperty snapToTicks;
    private ObjectProperty<StringConverter<Double>> labelFormatter;
    private DoubleProperty blockIncrement;
    private static final String DEFAULT_STYLE_CLASS = "slider";
    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

    public Slider() {
        this.initialize();
    }

    public Slider(double d, double d2, double d3) {
        this.setMax(d2);
        this.setMin(d);
        this.setValue(d3);
        this.adjustValues();
        this.initialize();
    }

    private void initialize() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.SLIDER);
    }

    public final void setMax(double d) {
        this.maxProperty().set(d);
    }

    public final double getMax() {
        return this.max == null ? 100.0 : this.max.get();
    }

    public final DoubleProperty maxProperty() {
        if (this.max == null) {
            this.max = new DoublePropertyBase(100.0){

                @Override
                protected void invalidated() {
                    if (this.get() < Slider.this.getMin()) {
                        Slider.this.setMin(this.get());
                    }
                    Slider.this.adjustValues();
                    Slider.this.notifyAccessibleAttributeChanged(AccessibleAttribute.MAX_VALUE);
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "max";
                }
            };
        }
        return this.max;
    }

    public final void setMin(double d) {
        this.minProperty().set(d);
    }

    public final double getMin() {
        return this.min == null ? 0.0 : this.min.get();
    }

    public final DoubleProperty minProperty() {
        if (this.min == null) {
            this.min = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    if (this.get() > Slider.this.getMax()) {
                        Slider.this.setMax(this.get());
                    }
                    Slider.this.adjustValues();
                    Slider.this.notifyAccessibleAttributeChanged(AccessibleAttribute.MIN_VALUE);
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "min";
                }
            };
        }
        return this.min;
    }

    public final void setValue(double d) {
        if (!this.valueProperty().isBound()) {
            this.valueProperty().set(d);
        }
    }

    public final double getValue() {
        return this.value == null ? 0.0 : this.value.get();
    }

    public final DoubleProperty valueProperty() {
        if (this.value == null) {
            this.value = new DoublePropertyBase(0.0){

                @Override
                protected void invalidated() {
                    Slider.this.adjustValues();
                    Slider.this.notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "value";
                }
            };
        }
        return this.value;
    }

    public final void setValueChanging(boolean bl) {
        this.valueChangingProperty().set(bl);
    }

    public final boolean isValueChanging() {
        return this.valueChanging == null ? false : this.valueChanging.get();
    }

    public final BooleanProperty valueChangingProperty() {
        if (this.valueChanging == null) {
            this.valueChanging = new SimpleBooleanProperty(this, "valueChanging", false);
        }
        return this.valueChanging;
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
                protected void invalidated() {
                    boolean bl = this.get() == Orientation.VERTICAL;
                    Slider.this.pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, bl);
                    Slider.this.pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, !bl);
                }

                @Override
                public CssMetaData<Slider, Orientation> getCssMetaData() {
                    return StyleableProperties.ORIENTATION;
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "orientation";
                }
            };
        }
        return this.orientation;
    }

    public final void setShowTickLabels(boolean bl) {
        this.showTickLabelsProperty().set(bl);
    }

    public final boolean isShowTickLabels() {
        return this.showTickLabels == null ? false : this.showTickLabels.get();
    }

    public final BooleanProperty showTickLabelsProperty() {
        if (this.showTickLabels == null) {
            this.showTickLabels = new StyleableBooleanProperty(false){

                @Override
                public CssMetaData<Slider, Boolean> getCssMetaData() {
                    return StyleableProperties.SHOW_TICK_LABELS;
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "showTickLabels";
                }
            };
        }
        return this.showTickLabels;
    }

    public final void setShowTickMarks(boolean bl) {
        this.showTickMarksProperty().set(bl);
    }

    public final boolean isShowTickMarks() {
        return this.showTickMarks == null ? false : this.showTickMarks.get();
    }

    public final BooleanProperty showTickMarksProperty() {
        if (this.showTickMarks == null) {
            this.showTickMarks = new StyleableBooleanProperty(false){

                @Override
                public CssMetaData<Slider, Boolean> getCssMetaData() {
                    return StyleableProperties.SHOW_TICK_MARKS;
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "showTickMarks";
                }
            };
        }
        return this.showTickMarks;
    }

    public final void setMajorTickUnit(double d) {
        if (d <= 0.0) {
            throw new IllegalArgumentException("MajorTickUnit cannot be less than or equal to 0.");
        }
        this.majorTickUnitProperty().set(d);
    }

    public final double getMajorTickUnit() {
        return this.majorTickUnit == null ? 25.0 : this.majorTickUnit.get();
    }

    public final DoubleProperty majorTickUnitProperty() {
        if (this.majorTickUnit == null) {
            this.majorTickUnit = new StyleableDoubleProperty(25.0){

                @Override
                public void invalidated() {
                    if (this.get() <= 0.0) {
                        throw new IllegalArgumentException("MajorTickUnit cannot be less than or equal to 0.");
                    }
                }

                @Override
                public CssMetaData<Slider, Number> getCssMetaData() {
                    return StyleableProperties.MAJOR_TICK_UNIT;
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "majorTickUnit";
                }
            };
        }
        return this.majorTickUnit;
    }

    public final void setMinorTickCount(int n) {
        this.minorTickCountProperty().set(n);
    }

    public final int getMinorTickCount() {
        return this.minorTickCount == null ? 3 : this.minorTickCount.get();
    }

    public final IntegerProperty minorTickCountProperty() {
        if (this.minorTickCount == null) {
            this.minorTickCount = new StyleableIntegerProperty(3){

                @Override
                public CssMetaData<Slider, Number> getCssMetaData() {
                    return StyleableProperties.MINOR_TICK_COUNT;
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "minorTickCount";
                }
            };
        }
        return this.minorTickCount;
    }

    public final void setSnapToTicks(boolean bl) {
        this.snapToTicksProperty().set(bl);
    }

    public final boolean isSnapToTicks() {
        return this.snapToTicks == null ? false : this.snapToTicks.get();
    }

    public final BooleanProperty snapToTicksProperty() {
        if (this.snapToTicks == null) {
            this.snapToTicks = new StyleableBooleanProperty(false){

                @Override
                public CssMetaData<Slider, Boolean> getCssMetaData() {
                    return StyleableProperties.SNAP_TO_TICKS;
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "snapToTicks";
                }
            };
        }
        return this.snapToTicks;
    }

    public final void setLabelFormatter(StringConverter<Double> stringConverter) {
        this.labelFormatterProperty().set(stringConverter);
    }

    public final StringConverter<Double> getLabelFormatter() {
        return this.labelFormatter == null ? null : (StringConverter)this.labelFormatter.get();
    }

    public final ObjectProperty<StringConverter<Double>> labelFormatterProperty() {
        if (this.labelFormatter == null) {
            this.labelFormatter = new SimpleObjectProperty<StringConverter<Double>>(this, "labelFormatter");
        }
        return this.labelFormatter;
    }

    public final void setBlockIncrement(double d) {
        this.blockIncrementProperty().set(d);
    }

    public final double getBlockIncrement() {
        return this.blockIncrement == null ? 10.0 : this.blockIncrement.get();
    }

    public final DoubleProperty blockIncrementProperty() {
        if (this.blockIncrement == null) {
            this.blockIncrement = new StyleableDoubleProperty(10.0){

                @Override
                public CssMetaData<Slider, Number> getCssMetaData() {
                    return StyleableProperties.BLOCK_INCREMENT;
                }

                @Override
                public Object getBean() {
                    return Slider.this;
                }

                @Override
                public String getName() {
                    return "blockIncrement";
                }
            };
        }
        return this.blockIncrement;
    }

    public void adjustValue(double d) {
        double d2 = this.getMin();
        double d3 = this.getMax();
        if (d3 <= d2) {
            return;
        }
        d = d < d2 ? d2 : d;
        d = d > d3 ? d3 : d;
        this.setValue(this.snapValueToTicks(d));
    }

    public void increment() {
        this.adjustValue(this.getValue() + this.getBlockIncrement());
    }

    public void decrement() {
        this.adjustValue(this.getValue() - this.getBlockIncrement());
    }

    private void adjustValues() {
        if (this.getValue() < this.getMin() || this.getValue() > this.getMax()) {
            this.setValue(Utils.clamp(this.getMin(), this.getValue(), this.getMax()));
        }
    }

    private double snapValueToTicks(double d) {
        double d2 = d;
        if (this.isSnapToTicks()) {
            double d3 = 0.0;
            d3 = this.getMinorTickCount() != 0 ? this.getMajorTickUnit() / (double)(Math.max(this.getMinorTickCount(), 0) + 1) : this.getMajorTickUnit();
            int n = (int)((d2 - this.getMin()) / d3);
            double d4 = (double)n * d3 + this.getMin();
            double d5 = (double)(n + 1) * d3 + this.getMin();
            d2 = Utils.nearest(d4, d2, d5);
        }
        return Utils.clamp(this.getMin(), d2, this.getMax());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SliderSkin(this);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return Slider.getClassCssMetaData();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case VALUE: {
                return this.getValue();
            }
            case MAX_VALUE: {
                return this.getMax();
            }
            case MIN_VALUE: {
                return this.getMin();
            }
            case ORIENTATION: {
                return this.getOrientation();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case INCREMENT: {
                this.increment();
                break;
            }
            case DECREMENT: {
                this.decrement();
                break;
            }
            case SET_VALUE: {
                Double d = (Double)objectArray[0];
                if (d == null) break;
                this.setValue(d);
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<Slider, Number> BLOCK_INCREMENT = new CssMetaData<Slider, Number>("-fx-block-increment", SizeConverter.getInstance(), (Number)10.0){

            @Override
            public boolean isSettable(Slider slider) {
                return slider.blockIncrement == null || !slider.blockIncrement.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Slider slider) {
                return (StyleableProperty)((Object)slider.blockIncrementProperty());
            }
        };
        private static final CssMetaData<Slider, Boolean> SHOW_TICK_LABELS = new CssMetaData<Slider, Boolean>("-fx-show-tick-labels", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(Slider slider) {
                return slider.showTickLabels == null || !slider.showTickLabels.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Slider slider) {
                return (StyleableProperty)((Object)slider.showTickLabelsProperty());
            }
        };
        private static final CssMetaData<Slider, Boolean> SHOW_TICK_MARKS = new CssMetaData<Slider, Boolean>("-fx-show-tick-marks", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(Slider slider) {
                return slider.showTickMarks == null || !slider.showTickMarks.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Slider slider) {
                return (StyleableProperty)((Object)slider.showTickMarksProperty());
            }
        };
        private static final CssMetaData<Slider, Boolean> SNAP_TO_TICKS = new CssMetaData<Slider, Boolean>("-fx-snap-to-ticks", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(Slider slider) {
                return slider.snapToTicks == null || !slider.snapToTicks.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Slider slider) {
                return (StyleableProperty)((Object)slider.snapToTicksProperty());
            }
        };
        private static final CssMetaData<Slider, Number> MAJOR_TICK_UNIT = new CssMetaData<Slider, Number>("-fx-major-tick-unit", SizeConverter.getInstance(), (Number)25.0){

            @Override
            public boolean isSettable(Slider slider) {
                return slider.majorTickUnit == null || !slider.majorTickUnit.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Slider slider) {
                return (StyleableProperty)((Object)slider.majorTickUnitProperty());
            }
        };
        private static final CssMetaData<Slider, Number> MINOR_TICK_COUNT = new CssMetaData<Slider, Number>("-fx-minor-tick-count", SizeConverter.getInstance(), (Number)3.0){

            @Override
            public boolean isSettable(Slider slider) {
                return slider.minorTickCount == null || !slider.minorTickCount.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Slider slider) {
                return (StyleableProperty)((Object)slider.minorTickCountProperty());
            }
        };
        private static final CssMetaData<Slider, Orientation> ORIENTATION = new CssMetaData<Slider, Orientation>("-fx-orientation", new EnumConverter<Orientation>(Orientation.class), Orientation.HORIZONTAL){

            @Override
            public Orientation getInitialValue(Slider slider) {
                return slider.getOrientation();
            }

            @Override
            public boolean isSettable(Slider slider) {
                return slider.orientation == null || !slider.orientation.isBound();
            }

            @Override
            public StyleableProperty<Orientation> getStyleableProperty(Slider slider) {
                return (StyleableProperty)((Object)slider.orientationProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(BLOCK_INCREMENT);
            arrayList.add(SHOW_TICK_LABELS);
            arrayList.add(SHOW_TICK_MARKS);
            arrayList.add(SNAP_TO_TICKS);
            arrayList.add(MAJOR_TICK_UNIT);
            arrayList.add(MINOR_TICK_COUNT);
            arrayList.add(ORIENTATION);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

